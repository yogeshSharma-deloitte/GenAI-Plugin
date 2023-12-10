package org.intellij.sdk.toolWindow;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.intellij.sdk.toolWindow.config.LoadConfiguration;
import org.intellij.sdk.toolWindow.uiPanel.CodeGenerationPanel;
import org.intellij.sdk.toolWindow.uiPanel.CodeMigrationPanel;
import org.intellij.sdk.toolWindow.uiPanel.SqlGenerationPanel;
import org.intellij.sdk.toolWindow.uiPanel.SummaryPanel;
import org.intellij.sdk.toolWindow.uiPanel.TestCasePanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;

/**
 * Main class for generating IntelliJ Plugin
 */
public class PluginToolWindowFactory implements ToolWindowFactory, DumbAware {

    private Editor editor;
    private Utils utils;
    private JiraIntegration jiraIntegration;
    private Project project;
    private CodeGenerationPanel codeGenerationPanel;
    private SqlGenerationPanel sqlGenerationPanel;
    private TestCasePanel testCasePanel;
    private CodeMigrationPanel codeMigrationPanel;
    private SummaryPanel summaryPanel;
    private LoadConfiguration configuration;


    /**
     * Initialize Classes
     */
    public void initialiseClass() {
        configuration = new LoadConfiguration();
        jiraIntegration = new JiraIntegration(configuration);
        utils = new Utils(jiraIntegration, configuration, project);
        codeGenerationPanel = new CodeGenerationPanel(project, utils, jiraIntegration);
        sqlGenerationPanel = new SqlGenerationPanel(project, utils, jiraIntegration);
        testCasePanel = new TestCasePanel(utils, editor, project);
        codeMigrationPanel = new CodeMigrationPanel(editor, utils, project);
        summaryPanel = new SummaryPanel(project, editor, utils);
    }

    /**
     * @param project
     * @param toolWindow
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        this.project = project;
        initialiseClass();

        SwingUtilities.invokeLater(() -> {
            PluginToolWindowContent toolWindowContent = new PluginToolWindowContent(toolWindow);
            if (Objects.isNull(editor)) {
                editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            }
            System.out.println("summary triggered");

            Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
            toolWindow.getContentManager().addContent(content);

        });

    }

    private class PluginToolWindowContent {
        @lombok.Getter
        private final JPanel contentPanel = new JPanel();

        public PluginToolWindowContent(ToolWindow toolWindow) {

            contentPanel.setMinimumSize(new Dimension(790, 290));
            contentPanel.setMaximumSize(new Dimension(790, 290));
            contentPanel.setLayout(new BorderLayout());

            SwingUtilities.invokeLater(() -> {
                JScrollPane scrollPane = new JScrollPane(createTabUI());
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                contentPanel.add(scrollPane, BorderLayout.CENTER);
            });
        }

        /**
         * This method generates tabs in IntelliJ Plugin
         *
         * @return JTabbedPane
         */
        private JTabbedPane createTabUI() {
            JTabbedPane tabbedPane = new JTabbedPane();

            // Create tabs with labels
            JPanel tab1 = new JPanel();
            JPanel tab2 = new JPanel();
            JPanel tab3 = new JPanel();
            JPanel tab4 = new JPanel();
            JPanel tab5 = new JPanel();
//            ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/icons8-loading-circle.gif"));

            tab1.add(summaryPanel.getSummaryPanel());
            tab2.add(codeMigrationPanel.getCodeMigratePanel());
            tab3.add(testCasePanel.getTestCasePanel());
            tab4.add(codeGenerationPanel.getCodeGenPanel());
            tab5.add(sqlGenerationPanel.getSqlGenPanel());

            tabbedPane.addTab("Code Summary", tab1);
            tabbedPane.addTab("Code Migration", tab2);
            tabbedPane.addTab("Test Suite", tab3);
            tabbedPane.addTab("Code Generator", tab4);
            tabbedPane.addTab("SQL Generator", tab5);
            return tabbedPane;

        }

    }


}