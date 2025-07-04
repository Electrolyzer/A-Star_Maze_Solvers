// src/DataCollectorUI.java
package src;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Enhanced data collector UI with configurable algorithm parameters,
 * tabbed results display, and comprehensive performance metrics.
 */
public class DataCollectorUI extends JFrame {
    // Core components
    private MazeAnalyzer analyzer;
    private JTabbedPane resultsTabbedPane;
    private JProgressBar progressBar;
    private JButton runAnalysisButton;

    // Configuration panels
    private JRadioButton usePreloadedMazesButton;
    private JRadioButton useGeneratedMazesButton;
    private JSpinner mazeSizeSpinner;
    private JSpinner mazeCountSpinner;

    // Algorithm configuration storage
    private java.util.List<AlgorithmConfig> algorithmConfigs;
    private DefaultListModel<AlgorithmConfig> configListModel;
    private JList<AlgorithmConfig> configList;

    // Results storage
    private Map<String, java.util.List<SolveResult>> results;

    public DataCollectorUI() {
        setTitle("Enhanced Maze Solver Data Collector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize data structures
        analyzer = new MazeAnalyzer(this);
        algorithmConfigs = new ArrayList<>();
        configListModel = new DefaultListModel<>();
        results = new HashMap<>();

        // Initialize default configurations
        initializeDefaultConfigurations();

        // Create UI components
        createComponents();
        layoutComponents();

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
    }

    private void createComponents() {
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(500, 30));
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");

        // Run button
        runAnalysisButton = new JButton("Run Analysis");
        runAnalysisButton.addActionListener(e -> runAnalysis());

        // Results tabbed pane
        resultsTabbedPane = new JTabbedPane();

        // Configuration list
        configList = new JList<>(configListModel);
        configList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        configList.setCellRenderer(new AlgorithmConfigRenderer());
    }

    private void layoutComponents() {
        // Control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // Main content area
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Left side: Algorithm configuration
        JPanel configPanel = createConfigurationPanel();
        mainSplitPane.setLeftComponent(configPanel);

        // Right side: Results
        mainSplitPane.setRightComponent(resultsTabbedPane);
        mainSplitPane.setDividerLocation(400);

        add(mainSplitPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

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

    private JPanel createConfigurationPanel() {
        JPanel configPanel = new JPanel(new BorderLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("Algorithm Configurations"));

        // Configuration list
        JScrollPane configScrollPane = new JScrollPane(configList);
        configScrollPane.setPreferredSize(new Dimension(350, 200));
        configPanel.add(configScrollPane, BorderLayout.CENTER);

        // Configuration management buttons
        JPanel configButtonPanel = new JPanel(new FlowLayout());

        JButton addConfigButton = new JButton("Add Configuration");
        addConfigButton.addActionListener(this::showAddConfigDialog);

        JButton removeConfigButton = new JButton("Remove Selected");
        removeConfigButton.addActionListener(e -> removeSelectedConfigs());

        JButton editConfigButton = new JButton("Edit Selected");
        editConfigButton.addActionListener(this::showEditConfigDialog);

        configButtonPanel.add(addConfigButton);
        configButtonPanel.add(editConfigButton);
        configButtonPanel.add(removeConfigButton);

        configPanel.add(configButtonPanel, BorderLayout.SOUTH);

        return configPanel;
    }

    private void showAddConfigDialog(ActionEvent e) {
        AlgorithmConfigDialog dialog = new AlgorithmConfigDialog(this, "Add Algorithm Configuration", null);
        dialog.setVisible(true);

        AlgorithmConfig newConfig = dialog.getResult();
        if (newConfig != null) {
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

        AlgorithmConfigDialog dialog = new AlgorithmConfigDialog(this, "Edit Algorithm Configuration", selected);
        dialog.setVisible(true);

        AlgorithmConfig editedConfig = dialog.getResult();
        if (editedConfig != null) {
            int index = configList.getSelectedIndex();
            algorithmConfigs.set(index, editedConfig);
            configListModel.setElementAt(editedConfig, index);
        }
    }

    private void removeSelectedConfigs() {
        java.util.List<AlgorithmConfig> selectedConfigs = configList.getSelectedValuesList();
        if (selectedConfigs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select configurations to remove.");
            return;
        }

        for (AlgorithmConfig config : selectedConfigs) {
            algorithmConfigs.remove(config);
            configListModel.removeElement(config);
        }
    }

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

        // Create tabs for different views
        createPerformanceComparisonTab();
        createDetailedResultsTab();
        createHeatmapTab();
        createStatisticsTab();

        resultsTabbedPane.revalidate();
        resultsTabbedPane.repaint();
    }

    private void createPerformanceComparisonTab() {
        JPanel performancePanel = new JPanel(new BorderLayout());

        // Create comparison table
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

        // Calculate grid layout based on number of algorithms
        int numAlgorithms = results.size();
        int cols = (int) Math.ceil(Math.sqrt(numAlgorithms));
        int rows = (int) Math.ceil((double) numAlgorithms / cols);

        heatmapTab.setLayout(new GridLayout(rows, cols, 10, 10));

        // Create heatmaps for each algorithm
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

    // Getters for analyzer
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

    // Inner classes for algorithm configuration
    public static class AlgorithmConfig {
        private String name;
        private String algorithmType; // "Forward", "Backward", "Adaptive"
        private char tiebreaker;
        private int sightRadius;

        public AlgorithmConfig(String name, String algorithmType, char tiebreaker, int sightRadius) {
            this.name = name;
            this.algorithmType = algorithmType;
            this.tiebreaker = tiebreaker;
            this.sightRadius = sightRadius;
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlgorithmType() {
            return algorithmType;
        }

        public void setAlgorithmType(String algorithmType) {
            this.algorithmType = algorithmType;
        }

        public char getTiebreaker() {
            return tiebreaker;
        }

        public void setTiebreaker(char tiebreaker) {
            this.tiebreaker = tiebreaker;
        }

        public int getSightRadius() {
            return sightRadius;
        }

        public void setSightRadius(int sightRadius) {
            this.sightRadius = sightRadius;
        }

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