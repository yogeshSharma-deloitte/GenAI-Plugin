package org.intellij.sdk.toolWindow.uiPanel;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import org.intellij.sdk.toolWindow.Utils;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Objects;

/**
 * This class generates UI for Code Migration Panel
 */
public class CodeMigrationPanel extends JPanel {
    @lombok.Getter
    JPanel codeMigratePanel = new JPanel();
    private JTextArea migrateResultTextArea;
    private JTextArea generateResultTextArea;
    private JComboBox<String> targetComboBox;
    private JComboBox<String> sourceComboBox;
    private JComboBox<String> fileListComboBox;
    private Editor editor;
    public CodeMigrationPanel(Editor parentEditor, Utils utils, Project project) {
        this.editor = parentEditor;
        genCodeMigrationPanel(utils, project);
    }

    /**
     * @param utils
     * @param project
     */
    private void genCodeMigrationPanel(Utils utils, Project project) {
        codeMigratePanel.setMinimumSize(new Dimension(790, 290));
        codeMigratePanel.setMaximumSize(new Dimension(790, 290));
        codeMigratePanel.setLayout(new BorderLayout());

        // Create a panel for the main content (excluding loading)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridBagLayout());

//            GridBagConstraints radioconstraints = new GridBagConstraints();

        JPanel sourceTargetPanel = new JPanel();
        sourceTargetPanel.setLayout(new GridBagLayout());
        constraints.insets = new Insets(0, 10, 0, 0);

        JLabel label1 = new JLabel("Source");
        constraints.gridx = 0;
        constraints.gridy = 0;
//            constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        sourceTargetPanel.add(label1, constraints);

        String[] sourceItems = {"Struts", "EJB", "MyBatis", "Axis", "Hibernate", "Servlet-JSP", "Spring MVC"};
        sourceComboBox = new JComboBox<>(sourceItems);
        constraints.gridx = 1;
        constraints.gridy = 0;
//            constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        sourceComboBox.setPreferredSize(new Dimension(150, 30));
        sourceTargetPanel.add(sourceComboBox, constraints);

        JLabel label2 = new JLabel("Target");
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        sourceTargetPanel.add(label2, constraints);

        String[] targetItems = {"Spring MVC", "Spring MVC Thymeleaf", "Spring Data JPA", "Spring MVC REST API"};
        targetComboBox = new JComboBox<>(targetItems);
        constraints.gridx = 3; // Place it next to the first ComboBox
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        targetComboBox.setPreferredSize(new Dimension(150, 30));
        sourceTargetPanel.add(targetComboBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(sourceTargetPanel, constraints);

        // Create a radio button group
        ButtonGroup radioButtonGroup = new ButtonGroup();
        JRadioButton fileRadioButton = new JRadioButton("Select File");
        JRadioButton manualRadioButton = new JRadioButton("Manual Input");

        // Add radio buttons to the group
        radioButtonGroup.add(fileRadioButton);
        radioButtonGroup.add(manualRadioButton);
        fileRadioButton.setSelected(true);

        JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioButtonPanel.add(fileRadioButton);
        radioButtonPanel.add(manualRadioButton);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(radioButtonPanel, constraints);


        // Create a JComboBox for file selection
        fileListComboBox = new JComboBox<>();
        utils.populateFileList(fileListComboBox, project);

        // Create a JTextArea for manual input
        JTextArea manualInputTextArea = new JTextArea(10, 40);
        manualInputTextArea.setLineWrap(true);
        manualInputTextArea.setWrapStyleWord(true);
        manualInputTextArea.setEditable(true);

        JScrollPane manualScrollPane = new JBScrollPane(manualInputTextArea);
        manualScrollPane.setPreferredSize(new Dimension(400, 200));
        manualScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.BOTH;
        manualScrollPane.setVisible(false);
        manualScrollPane.setEnabled(true);
        contentPanel.add(manualScrollPane, constraints);

        // Add components based on radio button selection
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weighty = 0.0;
//            constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(fileListComboBox, constraints);


        // Create a JButton for migration
        JButton migrateBtn = new JButton();
        constraints.gridx = 0;
        constraints.gridy = 3;
        migrateBtn.setText("Migrate");
//            constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.WEST;
        contentPanel.add(migrateBtn, constraints);

        migrateResultTextArea = new JTextArea(55, 15);
        migrateResultTextArea.setLineWrap(true);
        migrateResultTextArea.setWrapStyleWord(true);
        migrateResultTextArea.setEditable(true);

        JScrollPane scrollPane = new JBScrollPane(migrateResultTextArea);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.BOTH;
        contentPanel.add(scrollPane, constraints);

        generateResultTextArea = new JTextArea(55, 15);
        generateResultTextArea.setLineWrap(true);
        generateResultTextArea.setWrapStyleWord(true);
        generateResultTextArea.setEditable(false);

        JScrollPane generateScrollPane = new JBScrollPane(generateResultTextArea);
        generateScrollPane.setPreferredSize(new Dimension(400, 400));
        generateScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.BOTH;
        contentPanel.add(generateScrollPane, constraints);

        JButton acceptBtn = new JButton("<< Accept Changes");
        acceptBtn.setEnabled(false);
        constraints.gridx = 0;
        constraints.gridy = 5;
//            constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.EAST;
        contentPanel.add(acceptBtn, constraints);

        Icon icon = UIManager.getIcon("Tree.leafIcon");
        JButton copyCodeBtn = new JButton("Copy Selected Text", icon);
        copyCodeBtn.setVisible(false);
        constraints.gridx = 0;
        constraints.gridy = 5;
//            constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.EAST;
        contentPanel.add(copyCodeBtn, constraints);

        copyCodeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = generateResultTextArea.getSelectedText();
                if (selectedText != null && !selectedText.isEmpty()) {
                    StringSelection selection = new StringSelection(selectedText);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, null);
                    JOptionPane.showMessageDialog(null, "Selected Data copied to Clipboard");
                } else {
                    JOptionPane.showMessageDialog(null, "Please select any text!!");
                }
            }
        });

        acceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editor != null) {
                    String newText = migrateResultTextArea.getText();
                    if (newText.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No Value present!!");
                        return;
                    }
                    String selectedFileName = (String) fileListComboBox.getSelectedItem();
                    utils.selectAndReadFileFromComboBox(selectedFileName);
                    try (FileOutputStream fos = new FileOutputStream(utils.selectedFilePath)) {
                        byte[] bytes = newText.getBytes();
                        fos.write(bytes);

                        JOptionPane.showMessageDialog(null, "File updated successfully.");
                    } catch (IOException ee) {
                        ee.printStackTrace();
                        System.err.println("Error writing to the file: " + ee.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No editor is open to accept changes.");
                }
            }
        });


        JPanel loadingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/icons8-loading-circle.gif"));
        JLabel loadLabel = new JLabel("Loading... ", loading, JLabel.CENTER);
        loadLabel.setVisible(false);
        loadingPanel.add(loadLabel);

        codeMigratePanel.add(contentPanel, BorderLayout.CENTER);
        codeMigratePanel.add(loadingPanel, BorderLayout.SOUTH);

        // Add action listener for the Migrate button
        migrateBtn.addActionListener(e -> {
            if (Objects.isNull(editor)) {
                editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            }
            if (fileRadioButton.isSelected()) {
                migrateResultTextArea.setText("");
                migrateResultTextArea.setEditable(true);
                String selectedFileName = (String) fileListComboBox.getSelectedItem();
                utils.selectAndReadFileFromComboBox(selectedFileName);
            } else if (manualRadioButton.isSelected()) {
                String manualInput = manualInputTextArea.getText();
                // Perform migration based on manual input
                // utils.migrateFileToSpringBoot(manualInput, migrateResultTextArea);
            }


            loadLabel.setVisible(true);

            new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String sourceInput = sourceComboBox.getSelectedItem().toString();
                    String targetInput = targetComboBox.getSelectedItem().toString();

                    if (fileRadioButton.isSelected()) {
                        System.out.println("coming here");
                        String fileData = utils.readFileFromProject();
                        utils.migrateFileToSpringBoot(fileData, migrateResultTextArea, sourceInput, targetInput);
                        acceptBtn.setEnabled(true);
                    } else if (manualRadioButton.isSelected()) {
                        // Handle migration for manual input
                        generateResultTextArea.setText("");
                        generateResultTextArea.setEditable(true);
                        utils.migrateFileToSpringBoot(manualInputTextArea.getText(), generateResultTextArea, sourceInput, targetInput);
                        acceptBtn.setEnabled(true);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    // Hide loading indicator
                    loadLabel.setVisible(false);
                }
            }.execute();
        });

        // Add action listener for the manual radio button to toggle visibility of manual input textarea
        manualRadioButton.addActionListener(e -> {
            System.out.println("manualRadioButton.isSelected()" + manualRadioButton.isSelected());
            manualScrollPane.setVisible(manualRadioButton.isSelected());
            fileListComboBox.setVisible(!manualRadioButton.isSelected());
            migrateBtn.setText("Generate");
            acceptBtn.setVisible(!manualRadioButton.isSelected());
            copyCodeBtn.setVisible(manualRadioButton.isSelected());
            generateScrollPane.setVisible(manualRadioButton.isSelected());
            scrollPane.setVisible(!manualRadioButton.isSelected());
        });
        fileRadioButton.addActionListener(e -> {
            System.out.println("fileRadioButton.isSelected()" + fileRadioButton.isSelected());
            fileListComboBox.setVisible(fileRadioButton.isSelected());
            manualScrollPane.setVisible(!fileRadioButton.isSelected());
            migrateBtn.setText("Migrate");
            acceptBtn.setVisible(fileRadioButton.isSelected());
            copyCodeBtn.setVisible(!fileRadioButton.isSelected());
            generateScrollPane.setVisible(!manualRadioButton.isSelected());
            scrollPane.setVisible(!manualRadioButton.isSelected());

        });
    }

}
