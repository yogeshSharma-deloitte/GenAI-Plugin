package org.intellij.sdk.toolWindow.uiPanel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBScrollPane;
import org.intellij.sdk.toolWindow.JiraIntegration;
import org.intellij.sdk.toolWindow.Utils;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * This class generates UI for Code Generation Panel
 */
public class SqlGenerationPanel extends JPanel {
    public JiraIntegration sqlJiraIntegration;
    public JComboBox<String> sqlJiraUserStoryComboBox;
    public JComboBox<String> sqlJiraSubTaskComboBox;
    public JComboBox<String> sqlJiraSprintComboBox;
    public JTextArea sqlJiraCodeGenerateResultTextArea;
    public JTextArea sqlManualCodeGenerateResultTextArea;
    public List<String> sqlSubTaskDescription;
    @lombok.Getter
    JPanel sqlGenPanel = new JPanel();
    private String placeHolder = "Enter Your Input Here...";

    public SqlGenerationPanel(Project project, Utils utils, JiraIntegration sqlJiraIntegration) {
        this.sqlJiraIntegration = sqlJiraIntegration;
        genSqlCodePanel(project, utils, sqlJiraIntegration);
    }

    /**
     * @param project
     * @param utils
     * @param sqlJiraIntegration
     */
    private void genSqlCodePanel(Project project, Utils utils, JiraIntegration sqlJiraIntegration) {
        sqlGenPanel.setMinimumSize(new Dimension(790, 290));
        sqlGenPanel.setMaximumSize(new Dimension(790, 290));
        sqlGenPanel.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        ButtonGroup radioButtonGroup = new ButtonGroup();
        JRadioButton jiraRadioButton = new JRadioButton("Jira Story");
        JRadioButton manualRadioButton = new JRadioButton("Manual Input");

        radioButtonGroup.add(jiraRadioButton);
        radioButtonGroup.add(manualRadioButton);
        jiraRadioButton.setSelected(true);

        JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioButtonPanel.add(jiraRadioButton);
        radioButtonPanel.add(manualRadioButton);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(radioButtonPanel, constraints);

        JPanel sprintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label1 = new JLabel("Sprint");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        sprintPanel.add(label1, constraints);

        sqlJiraSprintComboBox = new JComboBox<String>();
        if (utils.validateJiraEnvTokens())
            sqlJiraIntegration.fetchJiraSprintList(sqlJiraSprintComboBox);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        sqlJiraSprintComboBox.setPreferredSize(new Dimension(265, 30));
        sqlJiraSprintComboBox.setVisible(true);
        sprintPanel.add(sqlJiraSprintComboBox, constraints);

        JButton refreshButton = new JButton("Refresh");
        ImageIcon refreshButtonIcon = new ImageIcon(getClass().getResource("/toolWindow/Refresh4.gif"));
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.EAST;
        sprintPanel.add(refreshButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(sprintPanel, constraints);
        sprintPanel.setVisible(true);

        JPanel userStoryComboBoxPanel = new JPanel();
        userStoryComboBoxPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel userStoryLabel = new JLabel("UserStory");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        userStoryComboBoxPanel.add(userStoryLabel, constraints);

        sqlJiraUserStoryComboBox = new JComboBox<String>();
        if (Objects.nonNull(sqlJiraSprintComboBox.getSelectedItem()))
            sqlJiraIntegration.fetchUserStoryFromSprint(sqlJiraUserStoryComboBox, sqlJiraSprintComboBox.getSelectedItem().toString());
        sqlJiraUserStoryComboBox.setPreferredSize(new Dimension(330, 30));
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        userStoryComboBoxPanel.add(sqlJiraUserStoryComboBox);

        JPanel subTaskComboBoxPanel = new JPanel();
        subTaskComboBoxPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel subTaskLabel = new JLabel("SubTask");
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        subTaskComboBoxPanel.add(subTaskLabel);

        sqlJiraSubTaskComboBox = new JComboBox<String>();
        if (Objects.nonNull(sqlJiraUserStoryComboBox.getSelectedItem())) {
            sqlJiraIntegration.fetchChildIssues(sqlJiraSubTaskComboBox, sqlJiraUserStoryComboBox.getSelectedItem().toString());
        }
        sqlJiraSubTaskComboBox.setPreferredSize(new Dimension(338, 30));
        constraints.fill = GridBagConstraints.HORIZONTAL;
        subTaskComboBoxPanel.add(sqlJiraSubTaskComboBox);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(userStoryComboBoxPanel, constraints);
        userStoryComboBoxPanel.setVisible(true);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(subTaskComboBoxPanel, constraints);
        subTaskComboBoxPanel.setVisible(true);


        JTextArea manualInputTextArea = new JTextArea(10, 40);
        manualInputTextArea.setLineWrap(true);
        manualInputTextArea.setWrapStyleWord(true);
        manualInputTextArea.setEditable(true);
        utils.addPlaceholder(manualInputTextArea, placeHolder);

        JScrollPane manualScrollPane = new JBScrollPane(manualInputTextArea);
        manualScrollPane.setPreferredSize(new Dimension(400, 200));
        manualScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.BOTH;
        manualScrollPane.setVisible(false);
        manualScrollPane.setEnabled(true);
        contentPanel.add(manualScrollPane, constraints);

        ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/Progress10.gif"));

        JButton genCodeBtn = new JButton("Generate SQL Query");
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.EAST;
        genCodeBtn.setVisible(true);
        contentPanel.add(genCodeBtn, constraints);

        JButton genCodeBtnForManual = new JButton("Generate SQL Query");
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.EAST;
        contentPanel.add(genCodeBtnForManual, constraints);

        sqlJiraCodeGenerateResultTextArea = new JTextArea(55, 15);
        sqlJiraCodeGenerateResultTextArea.setLineWrap(true);
        sqlJiraCodeGenerateResultTextArea.setWrapStyleWord(true);
        sqlJiraCodeGenerateResultTextArea.setEditable(false);
        sqlJiraCodeGenerateResultTextArea.setVisible(true);

        JScrollPane resultJiraScrollPane = new JBScrollPane(sqlJiraCodeGenerateResultTextArea);
        resultJiraScrollPane.setPreferredSize(new Dimension(400, 300));
        resultJiraScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.BOTH;
        contentPanel.add(resultJiraScrollPane, constraints);

        sqlManualCodeGenerateResultTextArea = new JTextArea(55, 15);
        sqlManualCodeGenerateResultTextArea.setLineWrap(true);
        sqlManualCodeGenerateResultTextArea.setWrapStyleWord(true);
        sqlManualCodeGenerateResultTextArea.setEditable(false);
        sqlManualCodeGenerateResultTextArea.setVisible(true);

        JScrollPane resultManualScrollPane = new JBScrollPane(sqlManualCodeGenerateResultTextArea);
        resultManualScrollPane.setPreferredSize(new Dimension(400, 300));
        resultManualScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.fill = GridBagConstraints.BOTH;
        contentPanel.add(resultManualScrollPane, constraints);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Icon saveIcon = UIManager.getIcon("Tree.leafIcon");
        JButton saveBtn = new JButton("Save", saveIcon);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        btnPanel.add(saveBtn, constraints);

        JButton copyBtn = new JButton("Copy");
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.EAST;
        btnPanel.add(copyBtn, constraints);

        sqlGenPanel.add(contentPanel, BorderLayout.CENTER);
        sqlGenPanel.add(btnPanel, BorderLayout.SOUTH);

        copyBtn.addActionListener(e -> {
            String textToCopy;
            if (jiraRadioButton.isSelected()) {
                textToCopy = sqlJiraCodeGenerateResultTextArea.getText();
            } else {
                textToCopy = sqlManualCodeGenerateResultTextArea.getText();
            }
            if (Objects.isNull(textToCopy) || textToCopy.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No Content to copy!!");
                return;
            }

            StringSelection selection = new StringSelection(textToCopy);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            JOptionPane.showMessageDialog(null, "Data copied to Clipboard");
        });

        saveBtn.addActionListener(e -> {
            if (jiraRadioButton.isSelected()) {
                if (Objects.isNull(sqlJiraCodeGenerateResultTextArea.getText()) || sqlJiraCodeGenerateResultTextArea.getText().isEmpty()){
                    JOptionPane.showMessageDialog(null, "No Content to Save!!");
                    return;
                }
                utils.saveGenCodeToFile(project, sqlJiraCodeGenerateResultTextArea.getText());
            } else {
                if (Objects.isNull(sqlManualCodeGenerateResultTextArea.getText()) || sqlManualCodeGenerateResultTextArea.getText().isEmpty()){
                    JOptionPane.showMessageDialog(null, "No Content to Save!!");
                    return;
                }
                utils.saveGenCodeToFile(project, sqlManualCodeGenerateResultTextArea.getText());
            }
        });

        refreshButton.addActionListener(
                e -> {
                    SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

                        @Override
                        protected String doInBackground() throws Exception {
                            refreshButton.setIcon(refreshButtonIcon);
                            utils.refreshButtonForJira(sqlJiraSprintComboBox);
                            utils.saveProjectInstructionFile(project);
                            return "";
                        }

                        @Override
                        protected void done() {
                            refreshButton.setIcon(null);
                        }
                    };
                    worker.execute();
                });

        manualRadioButton.addActionListener(e -> {
            userStoryComboBoxPanel.setVisible(!manualRadioButton.isSelected());
            subTaskComboBoxPanel.setVisible(!manualRadioButton.isSelected());
            manualScrollPane.setVisible(manualRadioButton.isSelected());
            sprintPanel.setVisible(!manualRadioButton.isSelected());
            resultManualScrollPane.setVisible(manualRadioButton.isSelected());
            resultJiraScrollPane.setVisible(!manualRadioButton.isSelected());
            genCodeBtn.setVisible(!manualRadioButton.isSelected());
            genCodeBtnForManual.setVisible(manualRadioButton.isSelected());
        });
        jiraRadioButton.addActionListener(e -> {
            userStoryComboBoxPanel.setVisible(jiraRadioButton.isSelected());
            subTaskComboBoxPanel.setVisible(jiraRadioButton.isSelected());
            manualScrollPane.setVisible(!jiraRadioButton.isSelected());
            sprintPanel.setVisible(jiraRadioButton.isSelected());
            resultJiraScrollPane.setVisible(jiraRadioButton.isSelected());
            resultManualScrollPane.setVisible(!jiraRadioButton.isSelected());
            genCodeBtnForManual.setVisible(!jiraRadioButton.isSelected());
            genCodeBtn.setVisible(jiraRadioButton.isSelected());
        });

        sqlJiraSprintComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Objects.nonNull(sqlJiraSprintComboBox.getSelectedItem())) {
                    String selectedSprintName = (String) sqlJiraSprintComboBox.getSelectedItem();
                    sqlJiraUserStoryComboBox.removeAllItems();
                    if (Objects.nonNull(selectedSprintName)) {
                        sqlJiraIntegration.fetchUserStoryFromSprint(sqlJiraUserStoryComboBox, selectedSprintName);
                    }
                }
            }
        });

        sqlJiraUserStoryComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Objects.nonNull(sqlJiraUserStoryComboBox.getSelectedItem())) {
                    afterUserStoryComboBoxLoaded(sqlJiraIntegration);
                    sqlJiraUserStoryComboBox.setToolTipText(sqlJiraUserStoryComboBox.getSelectedItem().toString());
                }
            }
        });
        sqlJiraSubTaskComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Objects.nonNull(sqlJiraSubTaskComboBox.getSelectedItem())) {

                    String selectedSubTaskName = (String) sqlJiraSubTaskComboBox.getSelectedItem();
                    if (Objects.nonNull(selectedSubTaskName)) {
                        sqlSubTaskDescription = sqlJiraIntegration.fetchDescription(selectedSubTaskName);
                        sqlJiraSubTaskComboBox.setToolTipText(sqlJiraSubTaskComboBox.getSelectedItem().toString());
                    }
                } else {
                    sqlSubTaskDescription = null;
                }
            }
        });

        genCodeBtn.addActionListener(e -> {
            if (jiraRadioButton.isSelected() && Objects.isNull(sqlJiraSubTaskComboBox.getSelectedItem())) {
                JOptionPane.showMessageDialog(null, "Select any Subtask");
                return;
            }
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    genCodeBtn.setIcon(loading);
                    if (jiraRadioButton.isSelected()) {
                        Project pj = ProjectManager.getInstance().getOpenProjects()[0];
                        String projectPath = pj.getBasePath() + File.separator + "deloitteGenAI" + File.separator + "instructionSql.txt";
                        String selectedRow = (String) sqlJiraSubTaskComboBox.getSelectedItem();
                        String codeResposne = utils.generateSqlCodeFromSubTask(selectedRow, projectPath);
                        sqlJiraCodeGenerateResultTextArea.setText(codeResposne);
                        genCodeBtn.setIcon(null);
                        return codeResposne;
                    }
                    return "";
                }

                @Override
                protected void done() {
                    genCodeBtn.setIcon(null);
                }
            };
            worker.execute();
        });

        genCodeBtnForManual.addActionListener(e -> {
            if (manualRadioButton.isSelected() && Objects.isNull(manualInputTextArea.getText())) {
                JOptionPane.showMessageDialog(null, "Select any Subtask");
                return;
            } else if (manualRadioButton.isSelected() && manualInputTextArea.getText().equals(placeHolder)) {
                JOptionPane.showMessageDialog(null, "Select any Subtask");
                return;
            }
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    genCodeBtnForManual.setIcon(loading);
                    if (manualRadioButton.isSelected()) {
                        Project pj = ProjectManager.getInstance().getOpenProjects()[0];
                        String projectPath = pj.getBasePath() + File.separator + "deloitteGenAI" + File.separator + "instructionSql.txt";
                        String selectedRow = manualInputTextArea.getText();
                        String codeResponse = utils.generateSqlCodeFromSubTask(selectedRow, projectPath);
                        sqlManualCodeGenerateResultTextArea.setText(codeResponse);
                        genCodeBtnForManual.setIcon(null);
                        return codeResponse;
                    }
                    return "";
                }

                @Override
                protected void done() {
                    genCodeBtnForManual.setIcon(null);
                }
            };
            worker.execute();
        });
    }

    /**
     * @param jiraRadioButton
     * @param subTasksJiraList
     * @param subTasksManualList
     * @param frame
     */
    private void editRowSteps(JRadioButton jiraRadioButton, JList<String> subTasksJiraList, JList<String> subTasksManualList, JFrame frame) {
        int selectedIndex;
        DefaultListModel<String> model;
        if (jiraRadioButton.isSelected()) {
            model = (DefaultListModel<String>) subTasksJiraList.getModel();
            selectedIndex = subTasksJiraList.getSelectedIndex();
        } else {
            model = (DefaultListModel<String>) subTasksManualList.getModel();
            selectedIndex = subTasksManualList.getSelectedIndex();
        }
        if (selectedIndex != -1) {
            String updatedItem = JOptionPane.showInputDialog(frame, "Edit item:", model.get(selectedIndex));

            if (updatedItem != null) {
                model.setElementAt(updatedItem, selectedIndex);
            }
        }
    }

    /**
     * @param genSubTaskBtn
     * @param model
     */
    private void ShowSubTask(JButton genSubTaskBtn, DefaultListModel<String> model) {
        if (Objects.nonNull(sqlSubTaskDescription) && !sqlSubTaskDescription.isEmpty()) {
            genSubTaskBtn.setVisible(false);
            model.removeAllElements();
            model.clear();
            for (String subTask : sqlSubTaskDescription) {
                model.addElement(subTask.trim());
            }
        } else {
            genSubTaskBtn.setVisible(true);
            model.removeAllElements();
            model.clear();
        }
    }

    /**
     * @param sqlJiraIntegration
     */
    private void afterUserStoryComboBoxLoaded(JiraIntegration sqlJiraIntegration) {
        String selectedSprintName = (String) sqlJiraUserStoryComboBox.getSelectedItem();
        sqlJiraSubTaskComboBox.removeAllItems();
        if (Objects.nonNull(selectedSprintName)) {
            sqlJiraIntegration.fetchChildIssues(sqlJiraSubTaskComboBox, selectedSprintName);

        }
    }

}
