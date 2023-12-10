package org.intellij.sdk.toolWindow.uiPanel;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import org.intellij.sdk.toolWindow.Utils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Objects;

/**
 * This class generates UI for Summary Generation Panel
 */
public class SummaryPanel extends JPanel {
    @lombok.Getter
    JPanel summaryPanel = new JPanel();
    private Editor editor;
    private JTextArea textArea1;
    private Project project;
    private Utils utils;

    public SummaryPanel(Project project, Editor parentEditor, Utils utils) {
        this.project = project;
        this.editor = parentEditor;
        this.utils = utils;
        genSummaryPanel(project, utils);

    }

    /**
     * @param project
     * @param utils
     */
    private void genSummaryPanel(Project project, Utils utils) {
        summaryPanel.setMinimumSize(new Dimension(790, 290));
        summaryPanel.setMaximumSize(new Dimension(790, 290));
        summaryPanel.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel label1 = new JLabel("Input");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        contentPanel.add(label1, constraints);

        JTextArea textArea = new JTextArea(10, 10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(true);
        JScrollPane scrollPaneTA = new JBScrollPane(textArea);
        scrollPaneTA.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(scrollPaneTA, constraints);

        JButton summarizeBtn = new JButton("Summarize");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.WEST;
        contentPanel.add(summarizeBtn, constraints);

        textArea1 = new JTextArea(25, 15);
        textArea1.setLineWrap(true);
        textArea1.setWrapStyleWord(true);
        textArea1.setEditable(true);
// Create a JScrollPane for the JTextArea
        JScrollPane scrollPane = new JBScrollPane(textArea1);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.BOTH;
        contentPanel.add(scrollPane, constraints);

        JButton saveButton = new JButton("Save"); // Create a "Save" button
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.WEST;
        contentPanel.add(saveButton, constraints);

        JPanel loadingSumPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/icons8-loading-circle.gif"));
        JLabel loadSumLabel = new JLabel("Loading... ", loading, JLabel.CENTER);
        loadSumLabel.setVisible(false);
        loadingSumPanel.add(loadSumLabel);


        summarizeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput;
                if (Objects.isNull(editor)) {
                    editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                }
                String selectedText = editor.getSelectionModel().getSelectedText();
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
                loadSumLabel.setVisible(true);
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        utils.findSummary(userInput, textArea1);
                        return null;
                    }

                    @Override
                    protected void done() {
                        loadSumLabel.setVisible(false);
                    }
                };

                // Execute the SwingWorker to start the background task
                worker.execute();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile(false); // Call a method to save the content to a file
            }
        });

        summaryPanel.add(contentPanel, BorderLayout.CENTER);
        summaryPanel.add(loadingSumPanel, BorderLayout.SOUTH);
    }

    /**
     * @param isFromTestSuite
     */
    private void saveToFile(Boolean isFromTestSuite) {
        if (isFromTestSuite) {


        } else {
            VirtualFile baseDir = project.getBaseDir();
            String docSummaryFolder = ".summaryDocuments";
            File docSummaryDirectory = new File(baseDir.getPath(), docSummaryFolder);
            if (!docSummaryDirectory.exists()) {
                if (docSummaryDirectory.mkdir()) {
                    // Directory created successfully
                } else {
                    // Directory creation failed
                    JOptionPane.showMessageDialog(null, "Error creating directory.");
                }
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose a location to save the file");
            fileChooser.setSelectedFile(new File(docSummaryDirectory, String.valueOf(baseDir)));
            FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("HTML Files (*.html)", "html");
            fileChooser.setFileFilter(fileFilter);
            fileChooser.setCurrentDirectory(docSummaryDirectory);
            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // Ensure the file has the ".html" extension
                String fileName = selectedFile.getName();
                if (fileChooser.getFileFilter() == fileFilter) {
                    if (!fileName.endsWith(".html")) {
                        selectedFile = new File(selectedFile.getParentFile(), fileName + ".html");
                    }
                } else {
                    selectedFile = new File(selectedFile.getParentFile(), fileName);
                }


//                File filePath = new File(docSummaryDirectory, fileName);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                    String data = textArea1.getText();
                    String convertsToHtml = utils.convertIntoHtml(data);
                    writer.write(convertsToHtml);
                    JOptionPane.showMessageDialog(null, "Data saved successfully on the path : \n" + selectedFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error saving data.");
                }
            }
        }
    }

}
