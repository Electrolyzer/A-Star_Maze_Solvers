package src;

// MazeAnalyzer.java
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Analyzes maze solving algorithms and collects performance data.
 * Compares different A* algorithm variants and generates heatmaps of explored
 * cells.
 */
public class MazeAnalyzer {
    private DataCollectorUI ui;
    private Map<String, int[][]> explorationCounts;

    /**
     * Constructs a MazeAnalyzer with the specified UI for displaying results.
     * 
     * @param ui The user interface component for displaying results
     */
    public MazeAnalyzer(DataCollectorUI ui) {
        this.ui = ui;
    }

    /**
     * Runs the analysis of maze solving algorithms based on UI parameters.
     * Processes mazes either from files or generates new ones, then analyzes
     * each with different algorithm variants and displays results.
     */
    public void runEnhancedAnalysis(java.util.List<DataCollectorUI.AlgorithmConfig> configs) {
        // Determine maze source and load/generate mazes
        List<int[][]> mazesToAnalyze;
        int mazeSize;

        if (ui.isUsingPreloadedMazes()) {
            mazesToAnalyze = loadPreloadedMazes();
            mazeSize = Constants.DEFAULT_MAZE_SIZE;
            explorationCounts = new HashMap<>();
            for (DataCollectorUI.AlgorithmConfig config : configs) {
                explorationCounts.put(config.getName(), new int[Constants.DEFAULT_MAZE_SIZE][Constants.DEFAULT_MAZE_SIZE]);
            }
        } else {
            mazeSize = ui.getMazeSize();
            int mazeCount = ui.getMazeCount();
            mazesToAnalyze = generateNewMazes(mazeSize, mazeCount);
            explorationCounts = new HashMap<>();
            for (DataCollectorUI.AlgorithmConfig config : configs) {
                explorationCounts.put(config.getName(), new int[mazeSize][mazeSize]);
            }
        }

        ui.getProgressBar().setMaximum(mazesToAnalyze.size() * configs.size());

        SwingWorker<Map<String, java.util.List<SolveResult>>, String> worker = new SwingWorker<>() {
            @Override
            protected Map<String, java.util.List<SolveResult>> doInBackground() {
                Map<String, java.util.List<SolveResult>> results = new HashMap<>();

                AtomicInteger progress = new AtomicInteger(0);

                for (DataCollectorUI.AlgorithmConfig config : configs) {
                    java.util.List<SolveResult> configResults = new ArrayList<>();

                    for (int[][] maze : mazesToAnalyze) {
                        MazeSolver solver = new MazeSolver(maze, config.getTiebreaker(), config.getSightRadius());

                        SolveResult result = switch (config.getAlgorithmType()) {
                            case "Forward" -> solver.solveForward();
                            case "Backward" -> solver.solveBackward();
                            case "Adaptive" -> solver.solveAdaptive();
                            default -> throw new IllegalArgumentException(
                                    "Unknown algorithm type: " + config.getAlgorithmType());
                        };

                        configResults.add(result);
                        updateExplorationCounts(config.getName(), result.solutionSteps().getLast(), mazeSize);

                        int currentProgress = progress.incrementAndGet();
                        SwingUtilities.invokeLater(() -> {
                            ui.getProgressBar().setValue(currentProgress);
                            ui.getProgressBar().setString(String.format("Processing %s... (%d/%d)",
                                    config.getName(), currentProgress, mazesToAnalyze.size() * configs.size()));
                        });
                    }

                    results.put(config.getName(), configResults);
                }

                return results;
            }

            @Override
            protected void done() {
                try {
                    Map<String, java.util.List<SolveResult>> results = get();
                    ui.displayResults(results);
                    ui.getProgressBar().setString("Analysis Complete!");
                    ui.enableRunButton();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ui, "Error during analysis: " + e.getMessage());
                    ui.enableRunButton();
                }
            }
        };

        worker.execute();
    }

    // NEW: Load preloaded mazes from files
    private List<int[][]> loadPreloadedMazes() {
        List<int[][]> preloadedMazes = new ArrayList<>();
        for (int i = 0; i < Constants.DEFAULT_MAZE_COUNT; i++) {
            String file = String.format("%02d", i);
            preloadedMazes
                    .add(MazeReader.readMaze(Constants.MAZE_FOLDER + file + Constants.MAZE_FILE_EXTENSION));
        }
        return preloadedMazes;
    }

    // NEW: Generate new mazes using MazeGenerator
    private List<int[][]> generateNewMazes(int size, int count) {
        List<int[][]> generatedMazes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            generatedMazes.add(MazeGenerator.generateMaze(size));
        }
        return generatedMazes;
    }

    // Updated to handle variable maze sizes
    private void updateExplorationCounts(String algorithm, int[][] unknownMaze, int mazeSize) {
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                if (unknownMaze[i][j] == Constants.EXPLORED || unknownMaze[i][j] == Constants.START_CELL
                        || unknownMaze[i][j] == Constants.TARGET_CELL) {
                    explorationCounts.get(algorithm)[i][j]++;
                }
            }
        }
    }

    public Map<String, int[][]> getExplorationCounts() {
        return explorationCounts;
    }
}