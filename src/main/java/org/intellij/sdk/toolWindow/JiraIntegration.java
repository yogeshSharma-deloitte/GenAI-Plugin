package org.intellij.sdk.toolWindow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.intellij.sdk.toolWindow.config.LoadConfiguration;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.swing.JComboBox;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Jira class for Jira APIs integration
 */
public class JiraIntegration {
    private Map<String, String> sprintIdMap = new HashMap<String, String>();
    private Map<String, String> userStoryIdMap = new HashMap<String, String>();
    private Map<String, String> subTaskIdMap = new HashMap<String, String>();
    private LoadConfiguration configuration;

    public JiraIntegration(LoadConfiguration configuration) {
        this.configuration = configuration;
    }


    /**
     * @param jiraCodeSprintComboBox
     */
    public void fetchJiraSprintList(JComboBox<String> jiraCodeSprintComboBox) {
        try {
            String jiraApiUrl = "https://" + configuration.getJiraBaseURI() +  ".atlassian.net/rest/agile/1.0/board/" + configuration.getJiraBoardId() + "/sprint";

            HttpURLConnection connection = (HttpURLConnection) new URL(jiraApiUrl).openConnection();
            connection.setRequestMethod("GET");

            String authHeader = "Basic " + Base64.getEncoder().encodeToString((configuration.getJiraUsername() + ":" + configuration.getJiraApiToken()).getBytes());
            connection.setRequestProperty("Authorization", authHeader);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    JSONObject jsonResponse = new JSONObject(in.lines().collect(Collectors.joining("\n")));
                    JSONArray sprints = jsonResponse.getJSONArray("values");

                    for (int i = 0; i < sprints.length(); i++) {
                        JSONObject sprint = sprints.getJSONObject(i);
                        String sprintName = sprint.getString("name");
                        String sprintId = sprint.getString("id");
                        sprintIdMap.put(sprintName, sprintId);
                        jiraCodeSprintComboBox.addItem(sprintName);
                    }
                }
            } else {
                System.out.println("Sprint list retrieval failed. Status code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param jiraUserStoryComboBox
     * @param selectedSprintName
     */
    public void fetchUserStoryFromSprint(JComboBox<String> jiraUserStoryComboBox, String selectedSprintName) {
        try {
            String selectedSprintId = sprintIdMap.get(selectedSprintName);
            String jiraApiUrl = String.format("https://%s.atlassian.net/rest/agile/1.0/board/%s/sprint/%s/issue",configuration.getJiraBaseURI(), configuration.getJiraBoardId(), selectedSprintId);

            HttpURLConnection connection = (HttpURLConnection) new URL(jiraApiUrl).openConnection();
            connection.setRequestMethod("GET");

            String authHeader = "Basic " + Base64.getEncoder().encodeToString((configuration.getJiraUsername() + ":" + configuration.getJiraApiToken()).getBytes());
            connection.setRequestProperty("Authorization", authHeader);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    JSONObject jsonResponse = new JSONObject(in.lines().collect(Collectors.joining("\n")));
                    JSONArray userStories = jsonResponse.getJSONArray("issues");

                    for (int i = 0; i < userStories.length(); i++) {
                        JSONObject userStory = userStories.getJSONObject(i);
                        String userStoryKey = userStory.getString("key");
                        String userStorySummary = userStory.getJSONObject("fields").getString("summary");
                        userStoryIdMap.put(userStorySummary, userStoryKey);
                        jiraUserStoryComboBox.addItem(userStorySummary);
                    }
                }
            } else {
                System.out.println("User story retrieval failed. Status code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param jiraSubTaskComboBox
     * @param selectedUserStoryKey
     */
    public void fetchChildIssues(JComboBox<String> jiraSubTaskComboBox, String selectedUserStoryKey) {
        try {
            String selectedUserStoryId = userStoryIdMap.get(selectedUserStoryKey);
            String jiraApiUrl = String.format("https://%s.atlassian.net/rest/api/%s/issue/%s?expand=names,renderedFields",configuration.getJiraBaseURI(), configuration.getJiraBoardId(), selectedUserStoryId);

            HttpURLConnection connection = (HttpURLConnection) new URL(jiraApiUrl).openConnection();
            connection.setRequestMethod("GET");

            String authHeader = "Basic " + Base64.getEncoder().encodeToString((configuration.getJiraUsername() + ":" + configuration.getJiraApiToken()).getBytes());
            connection.setRequestProperty("Authorization", authHeader);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    JSONObject userStoryDetails = new JSONObject(in.lines().collect(Collectors.joining("\n")));

                    // Extract and list child issues
                    JSONArray subTaskArray = userStoryDetails.getJSONObject("fields").getJSONArray("subtasks");
                    for (int i = 0; i < subTaskArray.length(); i++) {
                        JSONObject subTask = subTaskArray.getJSONObject(i);
                        String subTaskSummary = subTask.getJSONObject("fields").getString("summary");
                        subTaskIdMap.put(subTaskSummary, subTask.getString("key"));
                        jiraSubTaskComboBox.addItem(subTaskSummary);
                        System.out.println("Sub-Task Summary: " + subTaskSummary);
                    }
                }
            } else {
                System.out.println("User story details retrieval failed. Status code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param selectedSubTask
     * @return
     */
    public List<String> fetchDescription(String selectedSubTask) {
        try {
            String subtaskKey = subTaskIdMap.get(selectedSubTask);
            // Create the URL for the Jira REST API
            String apiUrl = String.format("https://my-app-jira.atlassian.net/rest/api/%s/issue/%s?fields=description",configuration.getJiraBoardId(), subtaskKey);
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            String authHeader = "Basic " + Base64.getEncoder().encodeToString((configuration.getJiraUsername() + ":" + configuration.getJiraApiToken()).getBytes());
            connection.setRequestProperty("Authorization", authHeader);
            connection.setDoInput(true);

            // Get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response using Jackson
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.toString());

                JsonNode fields = rootNode.path("fields");

                if (fields.has("description")) {
                    JsonNode description = fields.get("description");
                    List<String> textValues;

                    if (description.asText().equals("null")){
                        return null;
                    }
                    String[] descriptionRowArr = description.asText().trim().split("\n\n");
                    textValues = Arrays.asList(descriptionRowArr);
                    return textValues;
                }

                System.out.println("Description retrieval failed. Structure or text not found.");
                return Collections.emptyList();
            } else {
                System.out.println("Description retrieval failed. Status code: " + responseCode);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


}