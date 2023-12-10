package org.intellij.sdk.toolWindow.uiPanel;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import org.intellij.sdk.toolWindow.Utils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Objects;

/**
 * This class generates UI for Test Case Generation Panel
 */
public class TestCasePanel extends JPanel {
    private JComboBox<String> fileComboBox;
    private Editor editor;
    @lombok.Getter
    JPanel testCasePanel = new JPanel();

    public TestCasePanel(Utils utils, Editor parentEditor, Project project) {
        this.editor = parentEditor;
        genTestCasePanel(utils, project);

    }

    /**
     * @param utils
     * @param project
     */
    private void genTestCasePanel(Utils utils, Project project) {
        testCasePanel.setMinimumSize(new Dimension(790, 290));
        testCasePanel.setMaximumSize(new Dimension(790, 290));
        testCasePanel.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();


        JCheckBox useFileCheckbox = new JCheckBox("Select File");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        contentPanel.add(useFileCheckbox, constraints);


        fileComboBox = new JComboBox<String>();
        utils.populateFileList(fileComboBox, project);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(fileComboBox, constraints);
        fileComboBox.setVisible(false);

        JTextArea textArea = new JTextArea(10, 10);
        useFileCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileComboBox.setVisible(useFileCheckbox.isSelected());
                textArea.setEnabled(!useFileCheckbox.isSelected());
            }
        });

        JLabel label1 = new JLabel("Input");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.WEST;
        contentPanel.add(label1, constraints);


        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(true);
        JScrollPane scrollPaneTA = new JBScrollPane(textArea);
        scrollPaneTA.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(scrollPaneTA, constraints);

        ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/icons8-loading-circle.gif"));
        JButton testCaseBtn = new JButton("Generate TestCase", loading);
        testCaseBtn.setIcon(null);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.WEST;
        contentPanel.add(testCaseBtn, constraints);

        JTextArea testCasetextArea = new JTextArea(25, 15);
        testCasetextArea.setLineWrap(true);
        testCasetextArea.setWrapStyleWord(true);
        testCasetextArea.setEditable(true);
// Create a JScrollPane for the JTextArea
        JScrollPane scrollPane = new JBScrollPane(testCasetextArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.BOTH;
        contentPanel.add(scrollPane, constraints);
        //button
        JButton saveAsBtn = new JButton("Save As");
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.WEST;
        contentPanel.add(saveAsBtn, constraints);

        testCasePanel.add(contentPanel, BorderLayout.CENTER);


        testCaseBtn.addActionListener(new ActionListener() {
            String userInput = "";
            Boolean isFromFile = true;

            @Override
            public void actionPerformed(ActionEvent e) {

//                    loadTestLabel.setVisible(true);
                testCaseBtn.setIcon(loading);
                if (Objects.isNull(editor)) {
                    editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                }

                if (useFileCheckbox.isSelected()) {
                    String selectedFileName = (String) fileComboBox.getSelectedItem();
                    utils.selectAndReadFileFromComboBox(selectedFileName);
                    String fileData = utils.readFileFromProject();
                    userInput = fileData;
                    isFromFile = true;
//                        if (Objects.nonNull(textArea.getText()) && !textArea.getText().isEmpty()) {
//                            systemInput = textArea.getText();
//                        } else {
//                            systemInput = null;
//                        }
                } else {
                    String selectedText = editor.getSelectionModel().getSelectedText();
                    isFromFile = false;
                    if (Objects.nonNull(textArea.getText()) && !textArea.getText().isEmpty()) {
                        userInput = textArea.getText();
                    } else if (Objects.nonNull(selectedText) && !selectedText.equals("")) {
                        userInput = selectedText;
                    } else {
                        String fileContent = utils.getFileContent(editor);
                        System.out.println("fileContent - " + fileContent);
                        if (Objects.nonNull(fileContent) && !fileContent.equals("")) {
                            userInput = fileContent;
                        } else {
                            userInput = null;
                        }
                    }
                }

                // Use a SwingWorker to perform the test case generation in the background
                SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        String genTestCase = utils.testCaseGeneration(userInput, isFromFile);
                        return genTestCase;
                    }

                    @Override
                    protected void done() {
                        try {
                            String genTestCase = get();
                            testCasetextArea.setText(genTestCase);
                            testCaseBtn.setIcon(null);
//                                loadTestLabel.setVisible(false);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            testCaseBtn.setIcon(null);
//                                loadTestLabel.setVisible(false);
                            // Handle any exceptions that occur during test case generation
                        }
                    }
                };

                worker.execute(); // Start the SwingWorker
            }
        });
        saveAsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

//                    saveToFile(true);
            }
        });
    }

}
