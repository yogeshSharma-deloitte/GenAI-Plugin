package org.intellij.sdk.toolWindow.openAI;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.intellij.sdk.toolWindow.config.LoadConfiguration;

import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * Utility to connect OpenAI APIs
 */
public class openAiClientImpl implements openAiClient {
    private static final ObjectMapper mapper = new ObjectMapper();
    private LoadConfiguration configuration;

    public openAiClientImpl(LoadConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @param SystemMessage
     * @param InputTextFromUser
     * @return
     * @throws Exception
     */
    @Override
    public String openAiAPiEndPoint(String SystemMessage, String InputTextFromUser) throws Exception {

        JSONObject jsonItem = new JSONObject();
        jsonItem.put("model", configuration.getModel());
        jsonItem.put("temperature", configuration.getTemperature());
        jsonItem.put("max_tokens", configuration.getMax_token());
        jsonItem.put("top_p", configuration.getTopP());
        JSONObject systemMsg = new JSONObject();
        JSONArray messages = new JSONArray();
        systemMsg.put("role", "system");
        systemMsg.put("content", SystemMessage);
        messages.put(systemMsg);
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", InputTextFromUser);
        messages.put(userMsg);
        jsonItem.put("messages", messages);
        String requestBody = jsonItem.toString();

        RequestEntity<String> requestEntity;
        requestEntity = RequestEntity
                .post(new URI(configuration.getChatGptEndpoint()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + configuration.getChatGptApiKey())
                .body(requestBody);

        // Create a RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Send the POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        // Read the response body as a string
        String responseBody = responseEntity.getBody();
        String result = parseGptResponse(responseBody);

        // Handle the response as per your requirements
        return result;


    }

    /**
     * @param systemMessage
     * @param inputTextFromUser
     * @return openAI response
     * @throws Exception
     */
    @Override
    public String openAiAPiEndPointForAzure(String systemMessage, String inputTextFromUser) throws Exception {
        String azureApiKey = "eeba52b90f994c8d83a099cca0a2411a";
        String azureResourceName = "deloitte-bot-openai";
        String azureEngine = "chat_bot_16k";
        String azureApiVersion = "2023-07-01-preview";
        int azureMaxTokens = 2048;
        JSONObject jsonItem = new JSONObject();
        jsonItem.put("temperature", configuration.getTemperature());
        jsonItem.put("max_tokens", azureMaxTokens);
        jsonItem.put("top_p", configuration.getTopP());
        JSONObject systemMsg = new JSONObject();
        JSONArray messages = new JSONArray();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemMessage);
        messages.put(systemMsg);
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", inputTextFromUser);
        messages.put(userMsg);
        jsonItem.put("messages", messages);
        String requestBody = jsonItem.toString();
        String azureURI = "https://" + azureResourceName + ".openai.azure.com/openai/deployments/" + azureEngine + "/chat/completions?api-version=" + azureApiVersion;
        // Build and send the request
        RequestEntity<String> requestEntity = RequestEntity
                .post(new URI(azureURI))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("api-key", azureApiKey)
                .body(requestBody);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        String result = parseGptResponse(responseBody);
        return result;

    }

    /**
     * @param responseBody
     * @return
     * @throws Exception
     */
    private String parseGptResponse(String responseBody) throws Exception {
        if (responseBody == null || responseBody.trim().isBlank()) {
            throw new Exception("Exception in getting response");
        }
        try (JsonParser parser = mapper.getFactory().createParser(responseBody)) {
            JsonNode rootNode = parser.readValueAsTree();
            JsonNode choicesNode = rootNode.get("choices");

            if (!rootNode.has("choices")) {
                throw new Exception("Response is missing 'choices' field: " + responseBody);
            }

            if (choicesNode.size() < 1) {
                throw new Exception("Response is missing 'choices' array: " + responseBody);
            }

            String tagsOrPrompt = choicesNode.get(0).get("message").get("content").asText();

            if (tagsOrPrompt.startsWith("Prompt:")) {
                tagsOrPrompt = tagsOrPrompt.substring("Prompt:".length()).trim();
            }

            if (tagsOrPrompt.startsWith("\"") && tagsOrPrompt.endsWith("\"")) {
                tagsOrPrompt = tagsOrPrompt.substring(1, tagsOrPrompt.length() - 1);
            }

            if (Character.isLowerCase(tagsOrPrompt.charAt(0))) {
                tagsOrPrompt = Character.toUpperCase(tagsOrPrompt.charAt(0)) + tagsOrPrompt.substring(1);
            }

            return tagsOrPrompt;
        }
    }

}
