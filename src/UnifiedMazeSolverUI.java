// src/UnifiedMazeSolverUI.java
package src;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

import static src.Constants.*;

/**
 * Unified Maze Solver UI that combines interactive solving and performance analysis
 * with shared configuration management.
 */
public class UnifiedMazeSolverUI extends JFrame implements GenericMazeAnalyzer.AnalysisUI {
    // Shared configuration management
    private java.util.List<AlgorithmConfig> algorithmConfigs;
    private DefaultListModel<AlgorithmConfig> configListModel;
    private JList<AlgorithmConfig> configList;
    private AlgorithmConfig currentSolverConfig;
    
    // Main tabbed interface
    private JTabbedPane mainTabbedPane;
    
    // Interactive Solver components
    private InteractiveMazePanel interactiveMazePanel;
    private JPanel solverControlPanel;
    
    // Performance Analysis components
    private GenericMazeAnalyzer analyzer;
    private JTabbedPane resultsTabbedPane;
    private JProgressBar progressBar;
    private JButton runAnalysisButton;
    private JRadioButton usePreloadedMazesButton;
    private JRadioButton useGeneratedMazesButton;
    private JSpinner mazeSizeSpinner;
    private JSpinner mazeCountSpinner;
    private Map<String, java.util.List<SolveResult>> results;

    public UnifiedMazeSolverUI() {
        setTitle("A* Maze Solver - Unified Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize shared data structures
        algorithmConfigs = new ArrayList<>();
        configListModel = new DefaultListModel<>();
        results = new HashMap<>();
        analyzer = new GenericMazeAnalyzer(this);

        // Initialize default configurations
        initializeDefaultConfigurations();

        // Create the main interface
        createMainInterface();
        
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void initializeDefaultConfigurations() {
        algorithmConfigs.add(new AlgorithmConfig("Forward A* (g-tie)", "Forward", 'g', 1));
        algorithmConfigs.add(new AlgorithmConfig("Forward A* (h-tie)", "Forward", 'h', 1));
        algorithmConfigs.add(new AlgorithmConfig("Backward A* (g-tie)", "Backward", 'g', 1));
        algorithmConfigs.add(new AlgorithmConfig("Adaptive A* (g-tie)", "Adaptive", 'g', 1));

        for (AlgorithmConfig config : algorithmConfigs) {
            configListModel.addElement(config);
        }
        
        // Set default solver configuration
        currentSolverConfig = algorithmConfigs.get(0);
    }

    private void createMainInterface() {
        // Create main tabbed pane
        mainTabbedPane = new JTabbedPane();
        
        // Create Interactive Solver tab
        JPanel solverTab = createInteractiveSolverTab();
        mainTabbedPane.addTab("Interactive Solver", solverTab);
        
        // Create Performance Analysis tab
        JPanel analysisTab = createPerformanceAnalysisTab();
        mainTabbedPane.addTab("Performance Analysis", analysisTab);
        
        add(mainTabbedPane, BorderLayout.CENTER);
        
        // Create shared configuration panel
        JPanel configPanel = createSharedConfigurationPanel();
        add(configPanel, BorderLayout.WEST);
    }

    private JPanel createSharedConfigurationPanel() {
        JPanel configPanel = new JPanel(new BorderLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("Algorithm Configurations"));
        configPanel.setPreferredSize(new Dimension(350, 0));

        // Configuration list
        configList = new JList<>(configListModel);
        configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configList.setCellRenderer(new AlgorithmConfigRenderer());
        configList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                AlgorithmConfig selected = configList.getSelectedValue();
                if (selected != null) {
                    currentSolverConfig = selected;
                    updateSolverConfigDisplay();
                }
            }
        });
        
        JScrollPane configScrollPane = new JScrollPane(configList);
        configScrollPane.setPreferredSize(new Dimension(330, 200));
        configPanel.add(configScrollPane, BorderLayout.CENTER);

        // Configuration management buttons
        JPanel configButtonPanel = new JPanel(new GridLayout(2, 2, 5, 5));

        JButton addConfigButton = new JButton("Add");
        addConfigButton.addActionListener(this::showAddConfigDialog);

        JButton editConfigButton = new JButton("Edit");
        editConfigButton.addActionListener(this::showEditConfigDialog);

        JButton removeConfigButton = new JButton("Remove");
        removeConfigButton.addActionListener(e -> removeSelectedConfig());

        JButton saveConfigsButton = new JButton("Save All");
        saveConfigsButton.addActionListener(e -> saveConfigurations());

        configButtonPanel.add(addConfigButton);
        configButtonPanel.add(editConfigButton);
        configButtonPanel.add(removeConfigButton);
        configButtonPanel.add(saveConfigsButton);

        configPanel.add(configButtonPanel, BorderLayout.SOUTH);

        return configPanel;
    }

    private JPanel createInteractiveSolverTab() {
        JPanel solverTab = new JPanel(new BorderLayout());
        
        // Create the interactive maze panel (simplified version of MazeSolverTester)
        interactiveMazePanel = new InteractiveMazePanel();
        JScrollPane mazeScrollPane = new JScrollPane(interactiveMazePanel);
        solverTab.add(mazeScrollPane, BorderLayout.CENTER);
        
        // Create solver control panel
        solverControlPanel = createSolverControlPanel();
        solverTab.add(solverControlPanel, BorderLayout.EAST);
        
        return solverTab;
    }

    private JPanel createSolverControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(300, 0));

        // Current configuration display
        JPanel currentConfigPanel = new JPanel(new BorderLayout());
        currentConfigPanel.setBorder(BorderFactory.createTitledBorder("Current Configuration"));
        
        JTextArea configDisplay = new JTextArea(3, 20);
        configDisplay.setEditable(false);
        configDisplay.setBackground(getBackground());
        updateConfigDisplay(configDisplay);
        currentConfigPanel.add(new JScrollPane(configDisplay), BorderLayout.CENTER);
        
        controlPanel.add(currentConfigPanel);
        controlPanel.add(Box.createVerticalStrut(10));

        // Maze editing controls
        JPanel editPanel = createMazeEditingPanel();
        controlPanel.add(editPanel);
        controlPanel.add(Box.createVerticalStrut(10));

        // Solver controls
        JPanel solverPanel = createSolverPanel();
        controlPanel.add(solverPanel);
        controlPanel.add(Box.createVerticalStrut(10));

        // Results and save controls
        JPanel resultsPanel = createResultsPanel();
        controlPanel.add(resultsPanel);

        return controlPanel;
    }

    private JPanel createMazeEditingPanel() {
        JPanel editPanel = new JPanel();
        editPanel.setBorder(BorderFactory.createTitledBorder("Maze Editing"));
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));

        // Edit mode selection
        JPanel modePanel = new JPanel(new FlowLayout());
        modePanel.add(new JLabel("Mode: "));
        JComboBox<String> modeCombo = new JComboBox<>(new String[]{"Set Start", "Set End", "Add Wall", "Remove Wall"});
        modePanel.add(modeCombo);
        editPanel.add(modePanel);

        // Maze control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton resetButton = new JButton("Reset Maze");
        JButton clearButton = new JButton("Clear Start/End");
        buttonPanel.add(resetButton);
        buttonPanel.add(clearButton);
        editPanel.add(buttonPanel);

        return editPanel;
    }

    private JPanel createSolverPanel() {
        JPanel solverPanel = new JPanel();
        solverPanel.setBorder(BorderFactory.createTitledBorder("Solver"));
        solverPanel.setLayout(new BoxLayout(solverPanel, BoxLayout.Y_AXIS));

        JButton solveButton = new JButton("Solve Maze");
        solveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        solverPanel.add(solveButton);

        return solverPanel;
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel();
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        JButton saveResultButton = new JButton("Save Result");
        JButton loadResultsButton = new JButton("Load Results");
        
        saveResultButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadResultsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        resultsPanel.add(saveResultButton);
        resultsPanel.add(Box.createVerticalStrut(5));
        resultsPanel.add(loadResultsButton);

        return resultsPanel;
    }

    private JPanel createPerformanceAnalysisTab() {
        JPanel analysisTab = new JPanel(new BorderLayout());
        
        // Control panel for analysis
        JPanel controlPanel = createAnalysisControlPanel();
        analysisTab.add(controlPanel, BorderLayout.NORTH);
        
        // Results tabbed pane
        resultsTabbedPane = new JTabbedPane();
        analysisTab.add(resultsTabbedPane, BorderLayout.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(500, 30));
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        analysisTab.add(progressBar, BorderLayout.SOUTH);
        
        return analysisTab;
    }

    private JPanel createAnalysisControlPanel() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Run analysis button
        runAnalysisButton = new JButton("Run Analysis");
        runAnalysisButton.addActionListener(e -> runAnalysis());
        
        // Maze source selection
        JPanel mazeSourcePanel = new JPanel();
        mazeSourcePanel.setBorder(BorderFactory.createTitledBorder("Maze Source"));

        usePreloadedMazesButton = new JRadioButton("Use Preloaded Mazes (50 mazes from files)");
        useGeneratedMazesButton = new JRadioButton("Generate New Mazes", true);

        ButtonGroup mazeSourceGroup = new ButtonGroup();
        mazeSourceGroup.add(usePreloadedMazesButton);
        mazeSourceGroup.add(useGeneratedMazesButton);

        mazeSourcePanel.add(usePreloadedMazesButton);
        mazeSourcePanel.add(useGeneratedMazesButton);

        // Generation options
        JPanel generationPanel = new JPanel();
        generationPanel.setBorder(BorderFactory.createTitledBorder("Generation Options"));

        generationPanel.add(new JLabel("Maze Size:"));
        mazeSizeSpinner = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_MAZE_SIZE,
                Constants.MIN_MAZE_SIZE, Constants.MAX_MAZE_SIZE, 1));
        generationPanel.add(mazeSizeSpinner);

        generationPanel.add(new JLabel("Number of Mazes:"));
        mazeCountSpinner = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_MAZE_COUNT,
                Constants.MIN_MAZE_COUNT, Constants.MAX_MAZE_COUNT, 1));
        generationPanel.add(mazeCountSpinner);

        // Enable/disable based on selection
        usePreloadedMazesButton.addActionListener(e -> {
            mazeSizeSpinner.setEnabled(false);
            mazeCountSpinner.setEnabled(false);
        });

        useGeneratedMazesButton.addActionListener(e -> {
            mazeSizeSpinner.setEnabled(true);
            mazeCountSpinner.setEnabled(true);
        });

        // Layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(runAnalysisButton, gbc);

        gbc.gridx = 1;
        controlPanel.add(mazeSourcePanel, gbc);

        gbc.gridx = 2;
        controlPanel.add(generationPanel, gbc);

        return controlPanel;
    }

    // Configuration management methods
    private void showAddConfigDialog(ActionEvent e) {
        GenericAlgorithmConfigDialog dialog = new GenericAlgorithmConfigDialog(
            this, "Add Algorithm Configuration", null, 
            (name, algorithmType, tiebreaker, sightRadius) -> 
                new AlgorithmConfig(name, algorithmType, tiebreaker, sightRadius));
        dialog.setVisible(true);

        AlgorithmConfiguration result = dialog.getResult();
        if (result != null) {
            AlgorithmConfig newConfig = (AlgorithmConfig) result;
            algorithmConfigs.add(newConfig);
            configListModel.addElement(newConfig);
        }
    }

    private void showEditConfigDialog(ActionEvent e) {
        AlgorithmConfig selected = configList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a configuration to edit.");
            return;
        }

        GenericAlgorithmConfigDialog dialog = new GenericAlgorithmConfigDialog(
            this, "Edit Algorithm Configuration", selected,
            (name, algorithmType, tiebreaker, sightRadius) -> 
                new AlgorithmConfig(name, algorithmType, tiebreaker, sightRadius));
        dialog.setVisible(true);

        AlgorithmConfiguration result = dialog.getResult();
        if (result != null) {
            AlgorithmConfig editedConfig = (AlgorithmConfig) result;
            int index = configList.getSelectedIndex();
            algorithmConfigs.set(index, editedConfig);
            configListModel.setElementAt(editedConfig, index);
            if (currentSolverConfig == selected) {
                currentSolverConfig = editedConfig;
                updateSolverConfigDisplay();
            }
        }
    }

    private void removeSelectedConfig() {
        AlgorithmConfig selected = configList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a configuration to remove.");
            return;
        }

        algorithmConfigs.remove(selected);
        configListModel.removeElement(selected);
        
        if (currentSolverConfig == selected && !algorithmConfigs.isEmpty()) {
            currentSolverConfig = algorithmConfigs.get(0);
            configList.setSelectedIndex(0);
            updateSolverConfigDisplay();
        }
    }

    private void saveConfigurations() {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Configuration files", "config"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".config")) {
                file = new File(file.getAbsolutePath() + ".config");
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(algorithmConfigs);
                JOptionPane.showMessageDialog(this, "Configurations saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving configurations: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateSolverConfigDisplay() {
        // Update the current configuration display in the solver tab
        Component[] components = solverControlPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getBorder() instanceof javax.swing.border.TitledBorder) {
                    javax.swing.border.TitledBorder border = (javax.swing.border.TitledBorder) panel.getBorder();
                    if ("Current Configuration".equals(border.getTitle())) {
                        Component scrollPane = panel.getComponent(0);
                        if (scrollPane instanceof JScrollPane) {
                            JTextArea textArea = (JTextArea) ((JScrollPane) scrollPane).getViewport().getView();
                            updateConfigDisplay(textArea);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void updateConfigDisplay(JTextArea textArea) {
        if (currentSolverConfig != null) {
            textArea.setText(String.format(
                "Name: %s\nAlgorithm: %s\nTiebreaker: %c\nSight Radius: %d",
                currentSolverConfig.getName(),
                currentSolverConfig.getAlgorithmType(),
                currentSolverConfig.getTiebreaker(),
                currentSolverConfig.getSightRadius()
            ));
        }
    }

    // Performance analysis methods (adapted from DataCollectorUI)
    public void runAnalysis() {
        java.util.List<AlgorithmConfig> selectedConfigs = configList.getSelectedValuesList();
        if (selectedConfigs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one algorithm configuration.");
            return;
        }

        runAnalysisButton.setEnabled(false);
        results.clear();
        resultsTabbedPane.removeAll();

        progressBar.setValue(0);
        progressBar.setString("Starting analysis...");

        analyzer.runEnhancedAnalysis(selectedConfigs);
    }

    public void displayResults(Map<String, java.util.List<SolveResult>> analysisResults) {
        this.results = analysisResults;

        // Create tabs for different views (same as DataCollectorUI)
        createPerformanceComparisonTab();
        createDetailedResultsTab();
        createHeatmapTab();
        createStatisticsTab();

        resultsTabbedPane.revalidate();
        resultsTabbedPane.repaint();
    }

    // Results display methods (copied from DataCollectorUI)
    private void createPerformanceComparisonTab() {
        JPanel performancePanel = new JPanel(new BorderLayout());

        String[] columnNames = { "Configuration", "Algorithm", "Tiebreaker", "Sight Radius", "Avg Expanded Cells", "Avg Time (ms)",
                "Min/Max Expanded" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        for (Map.Entry<String, java.util.List<SolveResult>> entry : results.entrySet()) {
            java.util.List<SolveResult> resultList = entry.getValue();
            if (resultList.isEmpty()) continue;
            double avgExpanded = resultList.stream().mapToInt(SolveResult::expandedCells).average().orElse(0);
            double avgTime = resultList.stream().mapToLong(SolveResult::solutionTimeMs).average().orElse(0);

            IntSummaryStatistics expandedStats = resultList.stream()
                    .mapToInt(SolveResult::expandedCells)
                    .summaryStatistics();

            String minMax = String.format("%d / %d", expandedStats.getMin(), expandedStats.getMax());

            tableModel.addRow(new Object[] {
                    entry.getKey(),
                    resultList.getFirst().algorithmName(),
                    resultList.getFirst().tiebreaker(),
                    resultList.getFirst().sightRadius(),
                    String.format("%.2f", avgExpanded),
                    String.format("%.2f", avgTime),
                    minMax
            });
        }

        JTable performanceTable = new JTable(tableModel);
        performanceTable.setAutoCreateRowSorter(true);

        performancePanel.add(new JScrollPane(performanceTable), BorderLayout.CENTER);
        resultsTabbedPane.addTab("Performance Comparison", performancePanel);
    }

    private void createDetailedResultsTab() {
        JPanel detailsPanel = new JPanel(new BorderLayout());

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        StringBuilder details = new StringBuilder();
        for (Map.Entry<String, java.util.List<SolveResult>> entry : results.entrySet()) {
            details.append("=== ").append(entry.getKey()).append(" ===\n");

            java.util.List<SolveResult> resultList = entry.getValue();
            for (int i = 0; i < resultList.size(); i++) {
                SolveResult result = resultList.get(i);
                details.append(String.format("Maze %d: %s, %d expanded, %dms\n",
                        i + 1, result.solved() ? "Solved" : "Failed",
                        result.expandedCells(), result.solutionTimeMs()));
            }
            details.append("\n");
        }

        detailsArea.setText(details.toString());
        detailsArea.setCaretPosition(0);

        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        resultsTabbedPane.addTab("Detailed Results", detailsPanel);
    }

    private void createHeatmapTab() {
        JPanel heatmapTab = new JPanel();

        int numAlgorithms = results.size();
        int cols = (int) Math.ceil(Math.sqrt(numAlgorithms));
        int rows = (int) Math.ceil((double) numAlgorithms / cols);

        heatmapTab.setLayout(new GridLayout(rows, cols, 10, 10));

        for (Map.Entry<String, java.util.List<SolveResult>> entry : results.entrySet()) {
            HeatmapPanel heatmap = new HeatmapPanel(analyzer.getExplorationCounts().get(entry.getKey()), entry.getKey());
            heatmapTab.add(heatmap);
        }

        JScrollPane heatmapScrollPane = new JScrollPane(heatmapTab);
        resultsTabbedPane.addTab("Exploration Heatmaps", heatmapScrollPane);
    }

    private void createStatisticsTab() {
        JPanel statsPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        for (Map.Entry<String, java.util.List<SolveResult>> entry : results.entrySet()) {
            JPanel algorithmStatsPanel = new JPanel(new BorderLayout());
            algorithmStatsPanel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));

            java.util.List<SolveResult> resultList = entry.getValue();
            IntSummaryStatistics expandedStats = resultList.stream()
                    .mapToInt(SolveResult::expandedCells)
                    .summaryStatistics();

            LongSummaryStatistics timeStats = resultList.stream()
                    .mapToLong(SolveResult::solutionTimeMs)
                    .summaryStatistics();

            JTextArea statsText = new JTextArea();
            statsText.setEditable(false);
            statsText.setText(String.format(
                    "Expanded Cells - Min: %d, Max: %d, Avg: %.2f, Total: %d\n" +
                            "Solution Time - Min: %dms, Max: %dms, Avg: %.2fms\n" +
                            "Success Rate: %.1f%% (%d/%d)\n" +
                            "Standard Deviation (Expanded): %.2f",
                    expandedStats.getMin(), expandedStats.getMax(), expandedStats.getAverage(), expandedStats.getSum(),
                    timeStats.getMin(), timeStats.getMax(), timeStats.getAverage(),
                    resultList.stream().mapToDouble(r -> r.solved() ? 1.0 : 0.0).average().orElse(0) * 100,
                    (int) resultList.stream().mapToDouble(r -> r.solved() ? 1.0 : 0.0).sum(), resultList.size(),
                    calculateStandardDeviation(resultList.stream().mapToInt(SolveResult::expandedCells).toArray())));

            algorithmStatsPanel.add(new JScrollPane(statsText), BorderLayout.CENTER);
            statsPanel.add(algorithmStatsPanel);
        }

        resultsTabbedPane.addTab("Statistics", new JScrollPane(statsPanel));
    }

    private double calculateStandardDeviation(int[] values) {
        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values)
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average().orElse(0);
        return Math.sqrt(variance);
    }

    // Getters for analyzer compatibility
    public boolean isUsingPreloadedMazes() {
        return usePreloadedMazesButton.isSelected();
    }

    public int getMazeSize() {
        return (Integer) mazeSizeSpinner.getValue();
    }

    public int getMazeCount() {
        return (Integer) mazeCountSpinner.getValue();
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void enableRunButton() {
        runAnalysisButton.setEnabled(true);
    }

    // Simplified interactive maze panel
    private class InteractiveMazePanel extends JPanel {
        public InteractiveMazePanel() {
            setPreferredSize(new Dimension(600, 600));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.drawString("Interactive Maze Solver", 20, 30);
            g.drawString("(Implementation in progress)", 20, 50);
            g.drawString("Selected Config: " + (currentSolverConfig != null ? currentSolverConfig.getName() : "None"), 20, 80);
        }
    }

    // Algorithm configuration classes implementing the common interface
    public static class AlgorithmConfig implements AlgorithmConfiguration {
        private String name;
        private String algorithmType;
        private char tiebreaker;
        private int sightRadius;

        public AlgorithmConfig(String name, String algorithmType, char tiebreaker, int sightRadius) {
            this.name = name;
            this.algorithmType = algorithmType;
            this.tiebreaker = tiebreaker;
            this.sightRadius = sightRadius;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAlgorithmType() { return algorithmType; }
        public void setAlgorithmType(String algorithmType) { this.algorithmType = algorithmType; }
        public char getTiebreaker() { return tiebreaker; }
        public void setTiebreaker(char tiebreaker) { this.tiebreaker = tiebreaker; }
        public int getSightRadius() { return sightRadius; }
        public void setSightRadius(int sightRadius) { this.sightRadius = sightRadius; }

        @Override
        public String toString() {
            return String.format("%s (t:%c, r:%d)", name, tiebreaker, sightRadius);
        }
    }

    private static class AlgorithmConfigRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof AlgorithmConfig) {
                AlgorithmConfig config = (AlgorithmConfig) value;
                setText(config.toString());
                setToolTipText(String.format("Algorithm: %s, Tiebreaker: %c, Sight Radius: %d",
                        config.getAlgorithmType(), config.getTiebreaker(), config.getSightRadius()));
            }

            return this;
        }
    }
}
