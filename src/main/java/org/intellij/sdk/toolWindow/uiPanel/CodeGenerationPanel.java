package org.intellij.sdk.toolWindow.uiPanel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBScrollPane;
import org.intellij.sdk.toolWindow.JiraIntegration;
import org.intellij.sdk.toolWindow.Utils;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class generates UI for Code Generation Panel
 */
public class CodeGenerationPanel extends JPanel {
    private Utils utils;
    public JiraIntegration jiraIntegration;
    public JComboBox<String> jiraUserStoryComboBox;
    public JComboBox<String> jiraSubTaskComboBox;
    public JComboBox<String> jiraSprintComboBox;
    public JTextArea jiraCodeGenerateResultTextArea;
    public JTextArea manualCodeGenerateResultTextArea;
    public List<String> subTaskDescription;
    @lombok.Getter
    JPanel codeGenPanel = new JPanel();
    private Project project;
    private String placeHolder = "Enter Your SubTask Here...";

    /**
     * @param project
     * @param utils
     * @param jiraIntegration
     */
    public CodeGenerationPanel(Project project, Utils utils, JiraIntegration jiraIntegration) {
        this.jiraIntegration = jiraIntegration;
        this.utils = utils;
        this.project = project;
        genCodeGenerationPanel(project, jiraIntegration);
    }

    /**
     * @param project
     * @param jiraIntegration
     */
    private void genCodeGenerationPanel(Project project, JiraIntegration jiraIntegration) {
        codeGenPanel.setLayout(new BorderLayout());

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

        jiraSprintComboBox = new JComboBox<String>();
        if (utils.validateJiraEnvTokens())
            jiraIntegration.fetchJiraSprintList(jiraSprintComboBox);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        jiraSprintComboBox.setPreferredSize(new Dimension(265, 30));
        jiraSprintComboBox.setVisible(true);
        sprintPanel.add(jiraSprintComboBox, constraints);

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

        jiraUserStoryComboBox = new JComboBox<String>();
        if (Objects.nonNull(jiraSprintComboBox.getSelectedItem()))
            jiraIntegration.fetchUserStoryFromSprint(jiraUserStoryComboBox, jiraSprintComboBox.getSelectedItem().toString());
        jiraUserStoryComboBox.setPreferredSize(new Dimension(330, 30));
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        userStoryComboBoxPanel.add(jiraUserStoryComboBox);

        JPanel subTaskComboBoxPanel = new JPanel();
        subTaskComboBoxPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel subTaskLabel = new JLabel("SubTask");
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        subTaskComboBoxPanel.add(subTaskLabel);

        jiraSubTaskComboBox = new JComboBox<String>();
        if (Objects.nonNull(jiraUserStoryComboBox.getSelectedItem()))
            jiraIntegration.fetchChildIssues(jiraSubTaskComboBox, jiraUserStoryComboBox.getSelectedItem().toString());
        jiraSubTaskComboBox.setPreferredSize(new Dimension(338, 30));
        constraints.fill = GridBagConstraints.HORIZONTAL;
        subTaskComboBoxPanel.add(jiraSubTaskComboBox);

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

        JButton genSubTaskBtn = new JButton("Generate Steps");
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.EAST;
        genSubTaskBtn.setVisible(true);
        contentPanel.add(genSubTaskBtn, constraints);

        JButton genManualSubTaskBtn = new JButton("Generate Steps");
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.EAST;
        genManualSubTaskBtn.setVisible(false);
        contentPanel.add(genManualSubTaskBtn, constraints);

        DefaultListModel<String> subTasksJiraModel = new DefaultListModel<>();
        JList<String> subTasksJiraList = new JList<>(subTasksJiraModel);
        subTasksJiraList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane subTasksJiraScrollPane = new JScrollPane(subTasksJiraList);
        subTasksJiraScrollPane.setPreferredSize(new Dimension(400, 200));
        subTasksJiraScrollPane.setVisible(true);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.fill = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        contentPanel.add(subTasksJiraScrollPane, constraints);

        DefaultListModel<String> model = (DefaultListModel<String>) subTasksJiraList.getModel();

        DefaultListModel<String> subTasksManualModel = new DefaultListModel<>();
        JList<String> subTasksManualList = new JList<>(subTasksManualModel);
        subTasksManualList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane subTasksManualScrollPane = new JScrollPane(subTasksManualList);
        subTasksManualScrollPane.setPreferredSize(new Dimension(400, 200));
        subTasksManualScrollPane.setVisible(false);
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.fill = GridBagConstraints.EAST;
        contentPanel.add(subTasksManualScrollPane, constraints);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton genCodeBtn = new JButton("Generate/Save Code");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.EAST;
        btnPanel.add(genCodeBtn, constraints);

        JButton genAllCodeBtn = new JButton("Generate/Save All Code");
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.EAST;
        btnPanel.add(genAllCodeBtn, constraints);


        JPanel btnPanelForManual = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton genCodeBtnForManual = new JButton("Generate/Save Code");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.EAST;
        btnPanelForManual.add(genCodeBtnForManual, constraints);

        JButton genAllCodeBtnForManual = new JButton("Generate/Save All Code");
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.EAST;
        btnPanelForManual.add(genAllCodeBtnForManual, constraints);

        btnPanelForManual.setVisible(false);
        JPanel combinedBtnPanel = new JPanel();
        combinedBtnPanel.setLayout(new BorderLayout()); // You can choose another layout manager if needed

// Add the buttons to the combined panel
        combinedBtnPanel.add(btnPanel, BorderLayout.CENTER);
        combinedBtnPanel.add(btnPanelForManual, BorderLayout.SOUTH);
        constraints.gridx = 0;
        constraints.gridy = 7; // Updated to position below the buttons
        constraints.fill = GridBagConstraints.HORIZONTAL; // Adjust this based on your layout needs
        contentPanel.add(combinedBtnPanel, constraints);

        JPanel loadingPanelForOutput = new JPanel();
        loadingPanelForOutput.setLayout(new BoxLayout(loadingPanelForOutput, BoxLayout.X_AXIS));
        constraints.gridx = 0;
        constraints.gridy = 8;
        contentPanel.add(loadingPanelForOutput, constraints);

        codeGenPanel.add(contentPanel, BorderLayout.CENTER);

        refreshButton.addActionListener(
                e -> {
                    SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

                        @Override
                        protected String doInBackground() throws Exception {
                            refreshButton.setIcon(refreshButtonIcon);
                            utils.refreshButtonForJira(jiraSprintComboBox);
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
            subTasksManualScrollPane.setVisible(manualRadioButton.isSelected());
            subTasksJiraScrollPane.setVisible(!manualRadioButton.isSelected());
            genSubTaskBtn.setVisible(!manualRadioButton.isSelected());
            genManualSubTaskBtn.setVisible(manualRadioButton.isSelected());
            btnPanel.setVisible(!manualRadioButton.isSelected());
            btnPanelForManual.setVisible(manualRadioButton.isSelected());
            loadingPanelForOutput.setVisible(manualRadioButton.isSelected());
        });
        jiraRadioButton.addActionListener(e -> {
            userStoryComboBoxPanel.setVisible(jiraRadioButton.isSelected());
            subTaskComboBoxPanel.setVisible(jiraRadioButton.isSelected());
            manualScrollPane.setVisible(!jiraRadioButton.isSelected());
            sprintPanel.setVisible(jiraRadioButton.isSelected());
            subTasksManualScrollPane.setVisible(!jiraRadioButton.isSelected());
            subTasksJiraScrollPane.setVisible(jiraRadioButton.isSelected());
            btnPanelForManual.setVisible(!jiraRadioButton.isSelected());
            btnPanel.setVisible(jiraRadioButton.isSelected());
            genSubTaskBtn.setVisible(jiraRadioButton.isSelected());
            genManualSubTaskBtn.setVisible(!jiraRadioButton.isSelected());
            loadingPanelForOutput.setVisible(jiraRadioButton.isSelected());
        });

        jiraSprintComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Objects.nonNull(jiraSprintComboBox.getSelectedItem())) {
                    String selectedSprintName = (String) jiraSprintComboBox.getSelectedItem();
                    jiraUserStoryComboBox.removeAllItems();
                    if (Objects.nonNull(selectedSprintName)) {
                        jiraIntegration.fetchUserStoryFromSprint(jiraUserStoryComboBox, selectedSprintName);
                        System.out.println("Selected Sprint ID: " + selectedSprintName);
                    }
                }
            }
        });

        jiraUserStoryComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Objects.nonNull(jiraUserStoryComboBox.getSelectedItem())) {
                    afterUserStoryComboBoxLoaded(jiraIntegration);
                    jiraUserStoryComboBox.setToolTipText(jiraUserStoryComboBox.getSelectedItem().toString());
                }
            }
        });
        jiraSubTaskComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Objects.nonNull(jiraSubTaskComboBox.getSelectedItem())) {

                    String selectedSubTaskName = (String) jiraSubTaskComboBox.getSelectedItem();
                    if (Objects.nonNull(selectedSubTaskName)) {
                        System.out.println("Selected SubTask: " + selectedSubTaskName);
                        subTaskDescription = null;
                        jiraSubTaskComboBox.setToolTipText(jiraSubTaskComboBox.getSelectedItem().toString());
                    }
                } else {
                    subTaskDescription = null;
                }
            }
        });


        genSubTaskBtn.addActionListener(e -> {
            if (jiraRadioButton.isSelected() && Objects.isNull(jiraSubTaskComboBox.getSelectedItem())) {
                JOptionPane.showMessageDialog(null, "Select any Subtask");
                return;
            }
            Project pj = ProjectManager.getInstance().getOpenProjects()[0];
            String projectPath = pj.getBasePath() + File.separator + "deloitteGenAI" + File.separator +  "instructionSpringBoot.txt";
            File instructionFile = new File(projectPath);
            if (!instructionFile.exists()) {
                JOptionPane.showMessageDialog(null, "Instruction File Doesn't Exists at location: " + projectPath);
                return;
            }

            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    genSubTaskBtn.setIcon(loading);
                    if (jiraRadioButton.isSelected()) {
                        DefaultListModel<String> model = (DefaultListModel<String>) subTasksJiraList.getModel();
                        model.removeAllElements();
                        String selectedUserStory = jiraSubTaskComboBox.getSelectedItem().toString();
                        String[] subTasks = utils.generateSubTaskFromUserStory(selectedUserStory, instructionFile);
                        model.clear();
                        for (String subTask : subTasks) {
                            model.addElement(subTask.trim());
                        }
                        genSubTaskBtn.setIcon(null);
                        subTasksJiraList.setCellRenderer(new DefaultListCellRenderer() {
                            @Override
                            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                                // Set tooltip for each item
                                setToolTipText(value.toString());

                                return this;
                            }
                        });
                        return Arrays.toString(subTasks);
                    }
                    return "";
                }

                @Override
                protected void done() {
                    genSubTaskBtn.setIcon(null);
                }
            };
            worker.execute();
        });

        genManualSubTaskBtn.addActionListener(e -> {
            if (manualRadioButton.isSelected() && Objects.isNull(manualInputTextArea.getText())) {
                JOptionPane.showMessageDialog(null, "Select any Subtask");
                return;
            } else if (manualRadioButton.isSelected() && manualInputTextArea.getText().equals(placeHolder)) {
                JOptionPane.showMessageDialog(null, "Select any Subtask");
                return;
            }
            String inputText = manualInputTextArea.getText();
            Project pj = ProjectManager.getInstance().getOpenProjects()[0];
            String projectPath = pj.getBasePath() + File.separator + "deloitteGenAI" + File.separator +  "instructionSpringBoot.txt";
            File instructionFile = new File(projectPath);
            if (!instructionFile.exists()) {
                JOptionPane.showMessageDialog(null, "Instruction File Doesn't Exists at location: " + projectPath);
                return;
            }
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    genManualSubTaskBtn.setIcon(loading);
                    if (manualRadioButton.isSelected()) {
                        DefaultListModel<String> model = (DefaultListModel<String>) subTasksManualList.getModel();
                        model.removeAllElements();
                        String[] subTasks = utils.generateSubTaskFromUserStory(inputText, instructionFile);
                        model.clear(); // Clear previous entries
                        for (String subTask : subTasks) {
                            model.addElement(subTask.trim());
                        }
                        genManualSubTaskBtn.setIcon(null);
                        subTasksManualList.setCellRenderer(new DefaultListCellRenderer() {
                            @Override
                            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                                // Set tooltip for each item
                                setToolTipText(value.toString());

                                return this;
                            }
                        });
                        return Arrays.toString(subTasks);
                    }
                    return "";
                }

                @Override
                protected void done() {
                    genManualSubTaskBtn.setIcon(null);
                }
            };
            worker.execute();
        });

        JFrame frame = new JFrame();
        MouseListener listDoubleClickListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    frame.setTitle("Edit Item");
                    editRowSteps(jiraRadioButton, subTasksJiraList, subTasksManualList, frame);
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu contextMenu = new JPopupMenu();
                    JMenuItem addItem = new JMenuItem("Add Step");
                    JMenuItem editItem = new JMenuItem("Edit Step");
                    JMenuItem removeItem = new JMenuItem("Remove");

                    addItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            frame.setTitle("Add Item");
                            DefaultListModel<String> model;
                            String newItem = JOptionPane.showInputDialog(frame, "Enter a new item:");
                            if (jiraRadioButton.isSelected()) {
                                model = (DefaultListModel<String>) subTasksJiraList.getModel();
                            } else {
                                model = (DefaultListModel<String>) subTasksManualList.getModel();
                            }
                            if (newItem != null) {
                                model.addElement(newItem);
                            }
                        }
                    });
                    editItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame.setTitle("Edit Item");
                            editRowSteps(jiraRadioButton, subTasksJiraList, subTasksManualList, frame);
                        }
                    });
                    removeItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
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
                                model.removeElementAt(selectedIndex);
                            }
                        }
                    });
                    contextMenu.add(addItem);
                    if (jiraRadioButton.isSelected() && !subTasksJiraModel.isEmpty()) {
                        contextMenu.add(editItem);
                        contextMenu.add(removeItem);
                    }
                    if (manualRadioButton.isSelected() && !subTasksManualModel.isEmpty()) {
                        contextMenu.add(editItem);
                        contextMenu.add(removeItem);
                    }
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }

            }
        };
        subTasksJiraList.addMouseListener(listDoubleClickListener);
        subTasksManualList.addMouseListener(listDoubleClickListener);

        genAllCodeBtn.addActionListener(e -> {
            genSaveCode(project, jiraRadioButton, subTasksJiraList, genAllCodeBtn, loadingPanelForOutput, true, genCodeBtn);
        });

        genCodeBtn.addActionListener(e -> {
            genSaveCode(project, jiraRadioButton, subTasksJiraList, genCodeBtn, loadingPanelForOutput, false, genAllCodeBtn);
        });

        genAllCodeBtnForManual.addActionListener(e -> {
            genSaveCode(project, manualRadioButton, subTasksManualList, genAllCodeBtnForManual, loadingPanelForOutput, true, genCodeBtnForManual);
        });

        genCodeBtnForManual.addActionListener(e -> {
            genSaveCode(project, manualRadioButton, subTasksManualList, genCodeBtnForManual, loadingPanelForOutput, false, genAllCodeBtnForManual);
        });
//            if (manualRadioButton.isSelected() && subTasksManualList.getModel().getSize() == 0) {
//                JOptionPane.showMessageDialog(null, "Create at least 1 sub task first");
//                return;
//            }
//            Project pj = ProjectManager.getInstance().getOpenProjects()[0];
//            String projectPath = pj.getBasePath() + File.separator + "deloitteGenAI" + File.separator +  "instructionSpringBoot.txt";
//            File instructionFile = new File(projectPath);
//            if (!instructionFile.exists()) {
//                JOptionPane.showMessageDialog(null, "Instruction File Doesn't Exists at location: " + projectPath);
//                return;
//            }
//            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
//                @Override
//                protected String doInBackground() throws Exception {
//
//                    genCodeBtnForManual.setEnabled(false);
//                    genCodeBtnForManual.setToolTipText("Execution In Progress");
//                    String selectedRow = subTasksManualList.getSelectedValue();
//                    utils.generateCodeFromSubTask(subTasksManualList, project, loadingPanelForOutput, true);
//                    return "codeResponse";
//                }
//
//                @Override
//                protected void done() {
//                    genCodeBtnForManual.setIcon(null);
//                    genCodeBtnForManual.setEnabled(true);
//                    genCodeBtnForManual.setToolTipText(null);
//
//                }
//            };
//            worker.execute();
//        });
        UIManager.getPropertyChangeListeners();
        UIManager.getLookAndFeel();
        codeGenPanel.revalidate();
        codeGenPanel.repaint();
    }

    /**
     * @param project
     * @param selectedRadioButton
     * @param subTasksList
     * @param genAllCodeBtn
     * @param loadingPanelForOutput
     * @param genAllCode
     * @param buttonToDeactivate
     */
    private void genSaveCode(Project project, JRadioButton selectedRadioButton, JList<String> subTasksList, JButton genAllCodeBtn, JPanel loadingPanelForOutput, Boolean genAllCode, JButton buttonToDeactivate) {
        if (selectedRadioButton.isSelected() && subTasksList.getModel().getSize() == 0) {
            JOptionPane.showMessageDialog(null, "Generate steps first!");
            return;
        }
        if(!genAllCode && subTasksList.getSelectedIndex()==-1){
            JOptionPane.showMessageDialog(null, "Select a subtask!");
            return;
        }
        Project pj = ProjectManager.getInstance().getOpenProjects()[0];
        String projectPath = pj.getBasePath() + File.separator + "deloitteGenAI" + File.separator +  "instructionSpringBoot.txt";
        File instructionFile = new File(projectPath);
        if (!instructionFile.exists()) {
            JOptionPane.showMessageDialog(null, "Instruction File Doesn't Exists at location: " + projectPath);
            return;
        }
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                genAllCodeBtn.setEnabled(false);
                buttonToDeactivate.setEnabled(false);
                genAllCodeBtn.setToolTipText("Execution In Progress");
                utils.generateCodeFromSubTask(subTasksList, project, loadingPanelForOutput, genAllCode);
                genAllCodeBtn.setToolTipText(null);
                return "codeResposne";
            }

            @Override
            protected void done() {
                genAllCodeBtn.setIcon(null);
                genAllCodeBtn.setEnabled(true);
                buttonToDeactivate.setEnabled(true);
                genAllCodeBtn.setToolTipText(null);
            }
        };
        worker.execute();
    }

    /**
     * @param jiraIntegration
     */
    private void afterUserStoryComboBoxLoaded(JiraIntegration jiraIntegration) {
        String selectedSprintName = (String) jiraUserStoryComboBox.getSelectedItem();
        jiraSubTaskComboBox.removeAllItems();
        if (Objects.nonNull(selectedSprintName)) {
            jiraIntegration.fetchChildIssues(jiraSubTaskComboBox, selectedSprintName);
            System.out.println("Selected User Story ID: " + selectedSprintName);

        }
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
            String currentItem = model.get(selectedIndex);

            // Create and show the custom dialog
            JTextArea textArea = new JTextArea(7, 20);
            textArea.setText(currentItem); // Set the text of the JTextArea to the current item
            JScrollPane scrollPane = new JScrollPane(textArea);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            int result = JOptionPane.showConfirmDialog(frame, scrollPane, "Edit Step", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String updatedItem = textArea.getText();
                model.setElementAt(updatedItem, selectedIndex);
            }
        }
    }


}
