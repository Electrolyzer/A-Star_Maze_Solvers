package src;

// DataCollectorUI.java
import java.awt.*;
import javax.swing.*;

/**
 * The main user interface for the Maze Solver Data Collector application.
 * Provides controls for configuring maze analysis and displays results
 * including performance statistics and heatmaps of explored cells.
 */
public class DataCollectorUI extends JFrame {
    private JTextArea resultsArea;
    private JPanel heatmapPanel;
    private JProgressBar progressBar = new JProgressBar();
    private JButton runAnalysisButton;
    private JRadioButton usePreloadedMazesButton;
    private JRadioButton useGeneratedMazesButton;
    private JSpinner mazeSizeSpinner;
    private JSpinner mazeCountSpinner;
    private JSpinner sightRadiusSpinner; // New sight radius spinner
    private MazeAnalyzer analyzer;

    /**
     * Constructs the main application window with all UI components.
     */
    public DataCollectorUI() {
        setTitle("Maze Solver Data Collector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize analyzer
        analyzer = new MazeAnalyzer(this);

        // Create results text area
        resultsArea = new JTextArea(20, 50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultsArea);

        // Create heatmap panel
        heatmapPanel = new JPanel();
        heatmapPanel.setLayout(new GridLayout(2, 2, 10, 10));

        // Initialize progress bar
        progressBar.setPreferredSize(new Dimension(500, 30));
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");

        // Create enhanced control panel
        JPanel controlPanel = createControlPanel();

        // Layout
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.WEST);
        add(heatmapPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
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

        // Generation options panel
        JPanel generationPanel = new JPanel();
        generationPanel.setBorder(BorderFactory.createTitledBorder("Generation Options"));

        generationPanel.add(new JLabel("Maze Size:"));
        mazeSizeSpinner = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_MAZE_SIZE, Constants.MIN_MAZE_SIZE,
                Constants.MAX_MAZE_SIZE, 1));
        generationPanel.add(mazeSizeSpinner);

        generationPanel.add(new JLabel("Number of Mazes:"));
        mazeCountSpinner = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_MAZE_COUNT, Constants.MIN_MAZE_COUNT,
                Constants.MAX_MAZE_COUNT, 1));
        generationPanel.add(mazeCountSpinner);

        generationPanel.add(new JLabel("Sight Radius:"));
        sightRadiusSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        generationPanel.add(sightRadiusSpinner);

        // Enable/disable generation options based on selection
        usePreloadedMazesButton.addActionListener(e -> {
            mazeSizeSpinner.setEnabled(false);
            mazeCountSpinner.setEnabled(false);
            sightRadiusSpinner.setEnabled(false);
        });

        useGeneratedMazesButton.addActionListener(e -> {
            mazeSizeSpinner.setEnabled(true);
            mazeCountSpinner.setEnabled(true);
            sightRadiusSpinner.setEnabled(true);
        });

        // Run button
        runAnalysisButton = new JButton("Run Analysis");
        runAnalysisButton.addActionListener(e -> runAnalysis());

        // Layout components
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(mazeSourcePanel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        controlPanel.add(generationPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        controlPanel.add(runAnalysisButton, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        controlPanel.add(progressBar, gbc);

        return controlPanel;
    }

    /**
     * Initiates the maze analysis process based on current UI settings.
     */
    public void runAnalysis() {
        // Disable button and reset UI
        runAnalysisButton.setEnabled(false);
        resultsArea.setText("Starting analysis...\n");
        progressBar.setValue(0);
        progressBar.setString("Initializing...");
        heatmapPanel.removeAll();
        heatmapPanel.revalidate();
        heatmapPanel.repaint();

        // Delegate to analyzer
        analyzer.runAnalysis();
    }

    /**
     * Gets the text area used to display analysis results.
     * 
     * @return The results text area component
     */
    public JTextArea getResultsArea() {
        return resultsArea;
    }

    /**
     * Gets the progress bar used to show analysis progress.
     * 
     * @return The progress bar component
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Gets the panel used to display heatmaps of explored cells.
     * 
     * @return The heatmap panel component
     */
    public JPanel getHeatmapPanel() {
        return heatmapPanel;
    }
    
    /**
     * Checks whether preloaded mazes should be used for analysis.
     * 
     * @return true if preloaded mazes are selected, false if new mazes should be
     *         generated
     */
    public boolean isUsingPreloadedMazes() {
        return usePreloadedMazesButton.isSelected();
    }

    /**
     * Gets the size of mazes to be analyzed or generated.
     * 
     * @return The maze size in cells (width and height)
     */
    public int getMazeSize() {
        return (Integer) mazeSizeSpinner.getValue();
    }

    /**
     * Gets the number of mazes to be analyzed or generated.
     * 
     * @return The number of mazes
     */
    public int getMazeCount() {
        return (Integer) mazeCountSpinner.getValue();
    }

    /**
     * Gets the sight radius for maze analysis.
     * 
     * @return The sight radius in Manhattan distance
     */
    public int getSightRadius() {
        return (Integer) sightRadiusSpinner.getValue();
    }

    /**
     * Enables the run analysis button after completion of an analysis.
     */
    public void enableRunButton() {
        runAnalysisButton.setEnabled(true);
    }
}
