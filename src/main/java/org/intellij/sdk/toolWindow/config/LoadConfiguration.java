package org.intellij.sdk.toolWindow.config;

import lombok.Data;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

/**
 * This Class loads Configurations from properties file
 */
@Data
public class LoadConfiguration {
    public String jiraUsername;
    public String jiraApiToken;
    public Integer jiraBoardId;
    public String jiraBaseURI;
    public String chatGptEndpoint;
    public String chatGptApiKey;
    public Integer temperature;
    public Integer topP;
    public Integer max_token;
    public String model;

    public LoadConfiguration() {
        loadConfigurations();
    }

    /**
     * Loads Configuration from application.properties file
     */
    private void loadConfigurations() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Unable to find the config.properties file");
                return;
            }
            properties.load(input);
            //Jira Config
            setJiraUsername(properties.getProperty("jira.username"));
            setJiraApiToken(properties.getProperty("jira.apiToken"));
            setJiraBoardId(Integer.valueOf(properties.getProperty("jira.boardId")));
            //ChatGPT Config
            setChatGptApiKey(properties.getProperty("chatgpt.api_key"));
            setChatGptEndpoint(properties.getProperty("chatgpt.endpoint"));
            setModel(properties.getProperty("chatgpt.model"));
            setTemperature(Integer.valueOf(properties.getProperty("chatgpt.temperature")));
            setTopP(Integer.valueOf(properties.getProperty("chatgpt.topP")));
            setMax_token(Integer.valueOf(properties.getProperty("chatgpt.maxTokens")));

            System.out.println("Jira Username: " + jiraUsername);
            System.out.println("Jira API Token: " + jiraApiToken);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LoadConfigResult loadConfigFromEnv() {
        LoadConfigResult result = new LoadConfigResult();

        String jiraUsername = System.getenv("jira.username");
        String jiraApiToken = System.getenv("jira.apiToken");
        String jiraBoardIdStr = System.getenv("jira.boardId");
        String jiraBaseURI = System.getenv("jira.baseURI");

        if (jiraUsername == null || jiraUsername.isEmpty()) {
            result.setSuccess(false);
            result.setError("Jira Username is missing in environment variables.");
            return result;
        } else {
            setJiraUsername(jiraUsername);
        }

        if (jiraApiToken == null || jiraApiToken.isEmpty()) {
            result.setSuccess(false);
            result.setError("Jira API Token is missing in environment variables.");
            return result;
        } else {
            setJiraApiToken(jiraApiToken);
        }

        if (jiraBaseURI == null || jiraBaseURI.isEmpty()) {
            result.setSuccess(false);
            result.setError("Jira Base URI is missing in environment variables.");
            return result;
        } else {
            setJiraBaseURI(jiraBaseURI);
        }

        if (jiraBoardIdStr != null) {
            try {
                Integer jiraBoardId = Integer.valueOf(jiraBoardIdStr);
                setJiraBoardId(jiraBoardId);
            } catch (NumberFormatException e) {
                result.setSuccess(false);
                result.setError("Invalid value for Jira Board ID in environment variables.");
                return result;
            }
        }

        result.setSuccess(true);
        return result;
    }

}
