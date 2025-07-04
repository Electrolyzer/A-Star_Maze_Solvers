package src;

// MazeAnalyzer.java
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Analyzes maze solving algorithms and collects performance data.
 * Compares different A* algorithm variants and generates heatmaps of explored
 * cells.
 */
public class MazeAnalyzer {
    private DataCollectorUI ui;
    private List<Integer> expandedForwardG = new ArrayList<>();
    private List<Integer> expandedForwardH = new ArrayList<>();
    private List<Integer> expandedBackwardG = new ArrayList<>();
    private List<Integer> expandedAdaptiveG = new ArrayList<>();
    private int[][][] explorationCounts;
    private String[] algorithmNames = Constants.ALGORITHM_NAMES;

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
    public void runAnalysis() {
        // Determine maze source and load/generate mazes
        List<int[][]> mazesToAnalyze;
        int mazeSize;

        if (ui.isUsingPreloadedMazes()) {
            ui.getResultsArea().append("Loading preloaded mazes...\n");
            mazesToAnalyze = loadPreloadedMazes();
            mazeSize = Constants.DEFAULT_MAZE_SIZE; // Fixed size for preloaded mazes
        } else {
            mazeSize = ui.getMazeSize();
            int mazeCount = ui.getMazeCount();
            ui.getResultsArea().append(String.format("Generating %d new mazes of size %dx%d...\n",
                    mazeCount, mazeSize, mazeSize));
            mazesToAnalyze = generateNewMazes(mazeSize, mazeCount);
        }

        // Initialize exploration counts
        explorationCounts = new int[algorithmNames.length][mazeSize][mazeSize];

        // Update progress bar
        ui.getProgressBar().setMaximum(mazesToAnalyze.size() * algorithmNames.length);

        // Clear result lists
        expandedForwardG.clear();
        expandedForwardH.clear();
        expandedBackwardG.clear();
        expandedAdaptiveG.clear();

        ui.getProgressBar().setString("Processing mazes...");

        // Run analysis in background
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                individualResults(mazesToAnalyze, mazeSize);
                publish("\nCalculating averages...\n");
                averages();
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    ui.getResultsArea().append(chunk);
                }
            }

            @Override
            protected void done() {
                createHeatmaps();
                ui.getResultsArea().append("\nAnalysis complete! Heatmaps show exploration frequency.\n");
                ui.getProgressBar().setString("Complete!");
                ui.enableRunButton();
            }

            private void individualResults(List<int[][]> mazesToAnalyze, int mazeSize) {
                processAlgorithm(mazesToAnalyze, Constants.FORWARD_G, algorithmNames[Constants.FORWARD_G],
                        'g', expandedForwardG, MazeSolver::solveForward, mazeSize);
                processAlgorithm(mazesToAnalyze, Constants.FORWARD_H, algorithmNames[Constants.FORWARD_H],
                        'h', expandedForwardH, MazeSolver::solveForward, mazeSize);
                processAlgorithm(mazesToAnalyze, Constants.BACKWARD_G, algorithmNames[Constants.BACKWARD_G],
                        'g', expandedBackwardG, MazeSolver::solveBackward, mazeSize);
                processAlgorithm(mazesToAnalyze, Constants.ADAPTIVE_G, algorithmNames[Constants.ADAPTIVE_G],
                        'g', expandedAdaptiveG, MazeSolver::solveAdaptive, mazeSize);
            }

            private void processAlgorithm(List<int[][]> mazesToAnalyze, int algIndex, String algName, char tiebreaker,
                    List<Integer> expandedList, Function<MazeSolver, SolveResult> solveMethod, int mazeSize) {
                publish("Expanded cells for " + algName + ":\n[");

                AtomicInteger progress = new AtomicInteger(0);
                AtomicInteger counter = new AtomicInteger(0);
                int totalMazes = mazesToAnalyze.size();
                int sightRadius = ui.getSightRadius(); // Get sight radius from UI

                mazesToAnalyze.parallelStream().forEach(knownMaze -> {
                    MazeSolver solver = new MazeSolver(knownMaze, tiebreaker, sightRadius);
                    SolveResult result = solveMethod.apply(solver);
                    expandedList.add(result.expandedCells);
                    updateExplorationCounts(algIndex, solver.getUnknownMaze(), mazeSize);

                    int currentPos = counter.incrementAndGet();
                    String separator = (currentPos < totalMazes) ? ", " : "";

                    publish(result.expandedCells + separator);

                    int currentProgress = progress.incrementAndGet() + (algIndex * totalMazes);
                    SwingUtilities.invokeLater(() -> ui.getProgressBar().setValue(currentProgress));
                });

                publish("]\n\n");
            }        

            private void averages() {
                // NEW: Compute from stored lists (no solver re-runs)
                double avgForwardG = expandedForwardG.stream().mapToInt(Integer::intValue).average().orElse(0);
                publish("Average cells expanded for Forward A* (g): " + avgForwardG + "\n");

                double avgForwardH = expandedForwardH.stream().mapToInt(Integer::intValue).average().orElse(0);
                publish("Average cells expanded for Forward A* (h): " + avgForwardH + "\n");

                double avgBackwardG = expandedBackwardG.stream().mapToInt(Integer::intValue).average().orElse(0);
                publish("Average cells expanded for Backward A* (g): " + avgBackwardG + "\n");

                double avgAdaptiveG = expandedAdaptiveG.stream().mapToInt(Integer::intValue).average().orElse(0);
                publish("Average cells expanded for Adaptive A* (g): " + avgAdaptiveG + "\n");
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
    private void updateExplorationCounts(int algorithmIndex, int[][] unknownMaze, int mazeSize) {
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                if (unknownMaze[i][j] == Constants.EXPLORED || unknownMaze[i][j] == Constants.START_CELL
                        || unknownMaze[i][j] == Constants.TARGET_CELL) {
                    explorationCounts[algorithmIndex][i][j]++;
                }
            }
        }
    }

    private void createHeatmaps() {
        ui.getHeatmapPanel().removeAll();

        for (int alg = 0; alg < 4; alg++) {
            HeatmapPanel heatmap = new HeatmapPanel(explorationCounts[alg], algorithmNames[alg]);
            ui.getHeatmapPanel().add(heatmap);
        }

        ui.getHeatmapPanel().revalidate();
        ui.getHeatmapPanel().repaint();
    }
}