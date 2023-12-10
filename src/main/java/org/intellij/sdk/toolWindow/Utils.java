package org.intellij.sdk.toolWindow;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.intellij.sdk.toolWindow.config.LoadConfigResult;
import org.intellij.sdk.toolWindow.config.LoadConfiguration;
import org.intellij.sdk.toolWindow.openAI.openAiClient;
import org.intellij.sdk.toolWindow.openAI.openAiClientImpl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.intellij.sdk.toolWindow.constants.Prompts.codeMigrationSystemPrompt;
import static org.intellij.sdk.toolWindow.constants.Prompts.genSqlCodeFromSubTaskSystemPrompt;
import static org.intellij.sdk.toolWindow.constants.Prompts.genSubTaskFromUserStorySystemPrompt;
import static org.intellij.sdk.toolWindow.constants.Prompts.systemPromptFileExists;
import static org.intellij.sdk.toolWindow.constants.Prompts.systemPromptFileNotExists;
import static org.intellij.sdk.toolWindow.constants.Prompts.systemPromptForFileAndExistence;
import static org.intellij.sdk.toolWindow.constants.Prompts.systemPromptHtmlTemplate;
import static org.intellij.sdk.toolWindow.constants.Prompts.testCaseGenSystemPrompt;
import static org.intellij.sdk.toolWindow.constants.Prompts.testCaseGenUserPromptSuffix;

/**
 * Utility Class for performing several operation and acts as a mediator to call openAI endpoints
 */
public class Utils {

    private Project project;
    public String selectedFilePath;
    private List<String> allProjectFileList = new ArrayList<>();
    private openAiClient openAiClient;
    private JiraIntegration jiraIntegration;
    private LoadConfiguration configuration;
    private File instructionFilePath;
    ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/Refresh4.gif"));
    JLabel contentForLoading = new JLabel();
    private Integer checkSaveSuccess;

    public Utils(JiraIntegration jiraIntegration, LoadConfiguration configuration, Project project) {
        this.jiraIntegration = jiraIntegration;
        this.configuration = configuration;
        this.project = project;
        openAiClient = new openAiClientImpl(configuration);
        saveProjectInstructionFile(project);
    }

    /**
     * @param selectedFileName
     */
    public void selectAndReadFileFromComboBox(String selectedFileName) {
        selectedFilePath = getFilePathByName(selectedFileName);
        System.out.println("selectedFilePath = " + selectedFilePath);

    }

    /**
     * @param fileName
     * @return
     */
    private String getFilePathByName(String fileName) {
        return allProjectFileList.stream()
                .filter(filePath -> getFileName(filePath).equals(fileName))
                .findFirst()
                .orElse(null);
    }


    /**
     * @param fileListComboBox
     * @param project
     */
    public void populateFileList(JComboBox<String> fileListComboBox, Project project) {
        allProjectFileList = getProjectFileList(project);

        // Sort the file list
        Collections.sort(allProjectFileList, new Comparator<String>() {
            @Override
            public int compare(String filePath1, String filePath2) {
                String fileName1 = getFileName(filePath1);
                String fileName2 = getFileName(filePath2);
                return fileName1.compareTo(fileName2);
            }
        });

        // Add items to the combo box
        for (String filePath : allProjectFileList) {
            String fileName = getFileName(filePath);
            fileListComboBox.addItem(fileName);
        }
    }

    /**
     * @param filePath
     * @return
     */
    private String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    /**
     * @param project
     * @return
     */
    public List<String> getProjectFileList(Project project) {
        VirtualFile baseDir = project.getBaseDir();
        List<String> fileList = new ArrayList<>();

        if (baseDir != null) {
            VfsUtilCore.visitChildrenRecursively(baseDir, new VirtualFileVisitor() {
                @Override
                public boolean visitFile(@SuppressWarnings("NullableProblems") VirtualFile file) {
                    if (!file.isDirectory()) {
                        String filePath = file.getPath();

                        // Define the extensions and folders to filter
                        List<String> allowedExtensions = Arrays.asList(".java", ".jsp", ".xml", ".jws", ".properties");
                        List<String> ignoredFolders = Arrays.asList("/target/", "/.idea/", "/.gradle/");

                        if (allowedExtensions.stream().anyMatch(filePath::endsWith) &&
                                ignoredFolders.stream().noneMatch(filePath::contains) &&
                                !filePath.contains("/.")) {
                            fileList.add(filePath);
                        }
                    }
                    return true;
                }
            });
        }

        return fileList;
    }

    /**
     * @param editor
     * @return
     */
    public String getFileContent(Editor editor) {
        return Optional.ofNullable(editor)
                .map(e -> e.getDocument().getText())
                .orElse("");
    }

    /**
     * @param systemMessage
     * @param userMessage
     * @return
     * @throws Exception
     */
    public String callEndPoint(String systemMessage, String userMessage) throws Exception {
        String openAPIProvider = System.getenv("openAPIProvider");

        if (openAPIProvider != null && !openAPIProvider.isEmpty()) {
            return openAiClient.openAiAPiEndPointForAzure(systemMessage, userMessage);
        } else {
            return openAiClient.openAiAPiEndPoint(systemMessage, userMessage);
        }
    }

    /**
     * @param userInput
     * @param textArea
     */
    public void findSummary(String userInput, JTextArea textArea) {
        String systemMessage = "Summarise the main points of the article in a list format:\n";

        try {
            String result = callEndPoint(systemMessage, userInput);
            textArea.setText(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return list of file in the current project dir
     */
    public String readFileFromProject() {
        if (Objects.isNull(selectedFilePath)) {
            selectedFilePath = allProjectFileList.get(0);
        }
        Path path = Paths.get(selectedFilePath);

        try {
            String fileContents = Files.lines(path)
                    .collect(Collectors.joining(System.lineSeparator()));
            System.out.println("File Contents:\n" + fileContents);
            return fileContents;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @param userInput
     * @param textArea
     * @param oldTech
     * @param migrationOption
     */
    public void migrateFileToSpringBoot(String userInput, JTextArea textArea, String oldTech, String migrationOption) {
        String userPrompt = "1. **Old Technology:** " + oldTech +
                "2. **File or Code to Migrate:** " + userInput +
                "3. **Migration Options:** " + migrationOption +
                " \n" +
                "Guidelines for Migration: \n" +
                "Guidelines for Migration:\n" +
                "- Double-check the file path and name to ensure accuracy.\n" +
                "- Select appropriate migration options based on the old technology.\n" +
                "- Provide clear additional configurations if required (optional).\n" +
                "- Ensure a backup of the original file is available before migration.\n" +
                "- Follow best practices of the target technology during migration.\n" +
                "- If uncertain, ask for clarifications before proceeding.\n" +
                " \n" +
                "Please provide accurate and detailed information for successful migration.\n";

        Supplier<String> endpointCaller = () -> {
            try {
                return callEndPoint(codeMigrationSystemPrompt, userInput);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        String result = endpointCaller.get();
        textArea.setText(result);
    }


    /**
     * @param userInput
     * @param isFromFile
     * @return
     */
    public String testCaseGeneration(String userInput, Boolean isFromFile) {
        String userInputText = isFromFile
                ? "1. **Controller Class:**'''" + userInput + "'''\n" +
                "2. **Testing Framework:** (Select the testing framework from,  Junit or TestNG)\n" +
                "3. **Mocking Framework:** Mockito"
                : "1. **Methods to Test:**'''" + userInput + "'''\n" +
                "2. **Testing Framework:** (Select the testing framework from,  Junit or TestNG)\n" +
                "3. **Mocking Framework:** Mockito";

        String finalUserPrompt = userInputText + "\n\n\n" + testCaseGenUserPromptSuffix;
        System.out.println(finalUserPrompt);

        String result = null;
        try {
            result = callEndPoint(testCaseGenSystemPrompt, finalUserPrompt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * @param userInput
     * @return
     */
    public String convertIntoHtml(String userInput) {
        String result = null;
        try {
            result = callEndPoint(systemPromptHtmlTemplate, userInput);
            Document document = Jsoup.parse(result);
            return document.html();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param project
     * @param userInput
     */
    public void saveGenCodeToFile(Project project, String userInput) {
        VirtualFile baseDir = project.getBaseDir();
        File docSummaryDirectory = new File(baseDir.getPath());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose a location to save the file");
        fileChooser.setSelectedFile(new File(docSummaryDirectory, String.valueOf(baseDir)));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName();
            selectedFile = new File(selectedFile.getParentFile(), fileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                writer.write(userInput);
                JOptionPane.showMessageDialog(null, "Data saved successfully on the path : \n" + selectedFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saving data.");
            }
        }
    }

    /**
     * @param filePath
     * @param resultant
     */
    public void saveGenCodeDirectlyToLocation(String filePath, String resultant) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(resultant);
        } catch (IOException ex) {
            checkSaveSuccess = 0;
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving data. Invalid FilePath");
        }
    }

    /**
     * @param selectedRow
     * @return
     */
    public String generateSqlCodeFromSubTask(String selectedRow, String projectPath) {
        try {
            File instructionFile = new File(projectPath);
            String result;
            if (!instructionFile.exists()) {
                result = callEndPoint(genSqlCodeFromSubTaskSystemPrompt, selectedRow);
            } else {
                StringBuffer contents = readInstructionFile(instructionFile);
                String instructionContents = contents.toString();
                String userPromptForGeneratingSubTasks = "Below is the subtask : " + selectedRow + "and below is the instructions, read carefully and take the schema as reference from the file and generate accurate SQL query from it with example:\n" + instructionContents;
                result = callEndPoint(genSqlCodeFromSubTaskSystemPrompt, userPromptForGeneratingSubTasks);
            }
            String regex = result.contains("```SQL") ? "```SQL(.*?)```" : "```(.*?)```";
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(result);

            while (matcher.find()) {
                String extractedCodeResult = matcher.group(1);
                System.out.println("Extracted Code:");
                System.out.println(extractedCodeResult);
                return extractedCodeResult;
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param jiraSprintComboBox
     */
    public void refreshButtonForJira(JComboBox<String> jiraSprintComboBox) {
        if (validateJiraEnvTokens()) {
            jiraSprintComboBox.removeAllItems();
            jiraIntegration.fetchJiraSprintList(jiraSprintComboBox);
        }
    }

    /**
     * @return Boolean
     */
    public Boolean validateJiraEnvTokens() {
        LoadConfigResult result = configuration.loadConfigFromEnv();

        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(null, "Configuration Error", result.getError(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * @param result
     * @return
     */
    public String generateCodeFromOpenAIResponse(String result) {
        String regex;

        try {
            if (result.contains("```java")) {
                regex = "```java(.*?)```";
            } else {
                regex = "```(.*?)```";
            }

            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(result);

            while (matcher.find()) {
                String extractedCodeResult = matcher.group(1);
                System.out.println("Extracted Code:");
                System.out.println(extractedCodeResult);
                return extractedCodeResult;
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param element
     * @param fileNameWithPath
     * @param fileContents
     * @return
     * @throws Exception
     */
    private String generateCodeIfFileExistsAndSave(String element, String fileNameWithPath, String fileContents) throws Exception {
        try {
            StringBuffer contents = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(new File(fileNameWithPath)));
            String line;
            while ((line = reader.readLine()) != null) {
                contents.append(line).append("\n");
            }
            reader.close();
            fileContents = contents.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String userPromptFileExist = "Analyse below description properly and generate the exact code based on the description given below :\n" +
                " \n" +
                "Description: " + element + "\n" +
                " \n" +
                "and existing code : \n" + fileContents +
                "Analyse the code and append the code based on description.";
        String genFileFromOpenAi = callEndPoint(systemPromptFileExists, userPromptFileExist);
        System.out.println(genFileFromOpenAi);
        return generateCodeFromOpenAIResponse(genFileFromOpenAi);
    }

    private Map<String, List<String>> iterateMapByTag(Map<String, List<String>> packageToFileMap, String[] result) {
        Map<String, List<String>> pkgWithFile = new HashMap<>();
        for (String tag : result) {
            for (String key1 : packageToFileMap.keySet()) {
                String[] parts = key1.split("\\.");
                if (parts.length > 0) {
                    String lastPart = parts[parts.length - 1];
                    if (lastPart.toLowerCase().contains(tag.toLowerCase())) {
                        List<String> fileList = packageToFileMap.get(key1);
                        pkgWithFile.put(key1, fileList);
                        return pkgWithFile;
                    }
                }
            }

        }
        return pkgWithFile;
    }

    /*
    New Code from here ----------------------------------------------------
     */

    /**
     * @param userStory
     * @param instructionFile
     * @return
     * @throws Exception
     */
    public String[] generateSubTaskFromUserStory(String userStory, File instructionFile) throws Exception {
        this.instructionFilePath = instructionFile;
        StringBuffer contents = readInstructionFile(instructionFile);
        String instructionContents = contents.toString();
        String userPromptForGeneratingSubTasks = "Below is the user story : " + userStory + ", and below is the instructions, read carefully and generate the steps which should be fully described:\n" + instructionContents;
        String endPointResult = callEndPoint(genSubTaskFromUserStorySystemPrompt, userPromptForGeneratingSubTasks);
        String[] finalResultForEndPointResult = endPointResult.replace("\n", "").split("\\|");
        return finalResultForEndPointResult;
    }

    /**
     * @param instructionFile
     * @return
     * @throws IOException
     */
    private StringBuffer readInstructionFile(File instructionFile) throws IOException {
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(instructionFile));
        String line;
        while ((line = reader.readLine()) != null) {
            contents.append(line).append("\n");
        }
        reader.close();
        return contents;
    }

    /**
     * @param selectedRow
     * @param project
     * @param loadingPanelForOutput
     * @throws Exception
     */
    public void generateCodeFromSubTask(JList<String> selectedRow, Project project, JPanel loadingPanelForOutput, Boolean genAllCode) throws Exception {
        DefaultListModel<String> model = (DefaultListModel<String>) selectedRow.getModel();
        int size;
        String element;
        StringBuffer contents = readInstructionFile(instructionFilePath);
        String instructionContents = contents.toString();
        checkSaveSuccess = 1;
        size = genAllCode ? model.size() : 1;

        GridBagConstraints constraints = new GridBagConstraints();
        for (int i = 0; i < size; i++) {
            element = (size > 1) ? model.getElementAt(i) : selectedRow.getSelectedValue();
            String userPromptForLabelTitle = "Check out the below user message and give me a summary which should always be less than 5 words to give me an idea what is happening there. \n" +
                    "Return me title which can be as main work.\n";
            String endPointResponse = callEndPoint(userPromptForLabelTitle, element);

            contentForLoading = new JLabel(endPointResponse);
            constraints.gridx = 0;
            constraints.gridy = 8;
            constraints.fill = GridBagConstraints.EAST;
            contentForLoading.setIcon(loading);
            loadingPanelForOutput.add(contentForLoading);
            loadingPanelForOutput.setVisible(true);

            generateCodeAndSaveFileToLocation(instructionContents, element);

            loadingPanelForOutput.setVisible(false);
            contentForLoading.setText("");
            contentForLoading.setIcon(null);
        }
        String successMsg;
        if (checkSaveSuccess == 1) {
            successMsg = "Code Generated and saved successfully.";
        } else {
            successMsg = "Code Generation and save isn't successful.";
        }
        JOptionPane.showMessageDialog(null, successMsg);
        if (Objects.nonNull(project)) {
            project.getBaseDir().refresh(false, true);
            if (project.isInitialized()) {
                //check if project is completely refreshed.
                saveProjectInstructionFile(project);
            }
            project.getBaseDir().refresh(false, true);
        }
    }

    /**
     * @param instructionContents
     * @param element
     * @throws Exception
     */
    private void generateCodeAndSaveFileToLocation(String instructionContents, String element) throws Exception {
        String userPrompt = "Imagine you are a senior Java developer. Providing you description and analyse properly and generate the response. Below are description: " + element + "\n" +
                "You have access to an existing codebase with the following package and file names. Your job is to identify which file the new code for the above description should go into. If it's not found in any of the existing files, provide the name of the new file it should be placed in.\n" +
                "Existing code packages and files:\n\n" + instructionContents;

        //Fetch FileName and existence
        String openAiResp = callEndPoint(systemPromptForFileAndExistence, userPrompt);
        String fileNameWithPath = null;
        boolean isExist = false;

        String[] lines = openAiResp.split("\n");
        for (String line : lines) {
            if (line.startsWith("FileName:")) {
                fileNameWithPath = line.substring("FileName: ".length());
            } else if (line.startsWith("IsExist:")) {
                isExist = Boolean.parseBoolean(line.substring("IsExist: ".length()));
            }
        }

        String fileContents = "";
        String resultant = "";
        //Now Checking on isExist Generating or appending the code.
        if (isExist) {
            resultant = generateCodeIfFileExistsAndSave(element, fileNameWithPath, fileContents);
        } else {
            File file = new File(fileNameWithPath);
            String userPromptFileNotExists = "Below are the complete detailed description analyse the description properly and generate the java code \n" +
                    "Description: " + element + "\n" +
                    "Class name should be: " + file.getName() + "\n" +
                    "Providing you existing code base controller complete path :\n" + fileNameWithPath + "\n" +
                    "extract the base package and add package on top of new generated class";
            String genFileFromOpenAi = callEndPoint(systemPromptFileNotExists, userPromptFileNotExists);
            resultant = generateCodeFromOpenAIResponse(genFileFromOpenAi);
            System.out.println(resultant);
        }
        saveGenCodeDirectlyToLocation(fileNameWithPath, resultant);
    }

    /**
     * @param textArea
     * @param placeholder
     */
    public void addPlaceholder(JTextArea textArea, String placeholder) {
        textArea.setText(placeholder);
        textArea.setForeground(Color.GRAY);
        textArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textArea.getText().equals(placeholder)) {
                    textArea.setText("");
                    textArea.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textArea.getText().isEmpty()) {
                    textArea.setText(placeholder);
                    textArea.setForeground(Color.GRAY);
                }
            }
        });
    }

    /**
     * @param project
     * @return
     */
    public Map<String, List<String>> getFilesGroupedByPackage(Project project) {
        VirtualFile baseDir = project.getBaseDir();
        String baseDirToString = baseDir.toString();
        Map<String, List<String>> packageToFileMap = new HashMap<>();

        if (baseDir != null) {
            VfsUtilCore.visitChildrenRecursively(baseDir, new VirtualFileVisitor() {
                @Override
                public boolean visitFile(@SuppressWarnings("NullableProblems") VirtualFile file) {
                    if (!file.isDirectory()) {
                        String filePath = file.getPath();

                        // Define the extensions and folders to filter
                        List<String> allowedExtensions = Arrays.asList(".java");
                        List<String> ignoredFolders = Arrays.asList("/target/", "/test/", "/.idea/", "/.gradle/", ".jsp", ".xml", ".jws", ".properties", "Application");

                        if (allowedExtensions.stream().anyMatch(filePath::endsWith) &&
                                ignoredFolders.stream().noneMatch(filePath::contains) &&
                                !filePath.contains("/.")) {
                            // Get the package name by locating the last "src" segment in the file path
                            String srcSegment = "/src/main/java/";
                            int srcIndex = filePath.lastIndexOf(srcSegment);
                            if (srcIndex >= 0) {
                                String packageName = filePath.substring(srcIndex + srcSegment.length(), filePath.lastIndexOf('/')).replace('/', '.');
                                packageToFileMap.computeIfAbsent(packageName, k -> new ArrayList<>()).add(filePath);
                            }
                        }
                    }
                    return true;
                }
            });
        }

        return packageToFileMap;
    }

    /**
     * @param project
     */
    public void saveProjectInstructionFile(Project project) {
        Map<String, List<String>> packageToFileMap = getFilesGroupedByPackage(project);
        File dataFolder = new File(project.getBasePath(), "deloitteGenAI");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        String filePath = dataFolder + File.separator + "instructionSpringBoot.txt";
        String content = packageToFileMap.toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Add the introductory sentence
            writer.write("There is a SpringBoot Project named as " + project.getName() + ". This contains SpringBoot APIs.\nBelow are the Folder structuring defined :");
            writer.newLine();
            writer.newLine(); // Add a blank line for separation
            for (Map.Entry<String, List<String>> entry : packageToFileMap.entrySet()) {
                String packageName = entry.getKey();
                // Extract the part of the package name after the last dot
                String packageShortName = packageName.substring(packageName.lastIndexOf('.') + 1);
                List<String> fileList = entry.getValue();

                writer.write(packageShortName + " :\n" + "1. All the " + packageShortName + " classes will be under " + packageName + "\n" +
                        "Files are as belows :");
                writer.newLine();

                for (String fileName : fileList) {
                    writer.write("" + fileName);
                    writer.newLine();
                }
                writer.newLine();
            }
            writer.write("\nNote :\n" +
                    "\n" +
                    "1. To inject other necessary classes used @Autowired.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

