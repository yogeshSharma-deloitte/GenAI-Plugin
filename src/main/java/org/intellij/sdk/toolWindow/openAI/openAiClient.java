package org.intellij.sdk.toolWindow.openAI;

public interface openAiClient {

    String openAiAPiEndPoint(String SystemMessage, String InputTextFromUser) throws Exception;

    String openAiAPiEndPointForAzure(String systemMessage, String userMessage) throws Exception;
}
