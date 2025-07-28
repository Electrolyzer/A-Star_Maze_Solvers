// src/PerformanceAnalysisPanel.java
package src;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Panel for performance analysis with batch testing capabilities
 */
public class PerformanceAnalysisPanel extends JPanel implements GenericMazeAnalyzer.AnalysisUI {
    private GenericMazeAnalyzer analyzer;
    private JTabbedPane resultsTabbedPane;
    private JProgressBar progressBar;
    private JButton runAnalysisButton;
    private JRadioButton usePreloadedMazesButton;
    private JRadioButton useGeneratedMazesButton;
    private JSpinner mazeSizeSpinner;
    private JSpinner mazeCountSpinner;
    private Map<String, java.util.List<SolveResult>> results;
    private ConfigurationPanel configPanel;

    public PerformanceAnalysisPanel(ConfigurationPanel configPanel) {
        this.configPanel = configPanel;
        this.results = new HashMap<>();
        this.analyzer = new GenericMazeAnalyzer(this);
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Control panel for analysis
        JPanel controlPanel = createAnalysisControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Results tabbed pane
        resultsTabbedPane = new JTabbedPane();
        add(resultsTabbedPane, BorderLayout.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(500, 30));
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        add(progressBar, BorderLayout.SOUTH);
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

    public void runAnalysis() {
        // Get selected configurations from the configuration panel
        java.util.List<AlgorithmConfig> selectedConfigs = configPanel.getSelectedConfigs();
        
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

    // Results display methods
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
}
