package src;
// MazeAnalysisConfig.java
/**
 * Configuration constants for maze analysis application.
 * Centralizes all magic numbers and configuration values.
 */
public final class Constants {
    // UI Constants
    public static final int DEFAULT_MAZE_SIZE = 101;
    public static final int MIN_MAZE_SIZE = 10;
    public static final int MAX_MAZE_SIZE = 200;
    public static final int DEFAULT_MAZE_COUNT = 50;
    public static final int MIN_MAZE_COUNT = 1;
    public static final int MAX_MAZE_COUNT = 500;

    // Analysis Constants
    public static final int ALGORITHM_COUNT = 4;
    public static final String[] ALGORITHM_NAMES = {
            "Forward A* (g)", "Forward A* (h)", "Backward A* (g)", "Adaptive A* (g)"
    };
    public static final int FORWARD_G = 0;
    public static final int FORWARD_H = 1;
    public static final int BACKWARD_G = 2;
    public static final int ADAPTIVE_G = 3;

    // Progress and UI
    public static final int PROGRESS_BAR_MAX = 200;
    public static final String PROGRESS_READY = "Ready";
    public static final String PROGRESS_INITIALIZING = "Initializing...";
    public static final String PROGRESS_PROCESSING = "Processing mazes...";
    public static final String PROGRESS_COMPLETE = "Complete!";

    // File paths
    public static final String MAZE_FILE_PATTERN = "%02d";
    public static final String MAZE_FOLDER = "mazes/";
    public static final String MAZE_FILE_EXTENSION = ".txt";

    // Colors for heatmaps (ARGB format)
    public static final int HEATMAP_BASE_COLOR = 0xFF0000FF; // Blue base

    // Cell types
    public static final int UNBLOCKED = 0;
    public static final int BLOCKED = 1;
    public static final int UNKNOWN = 2;
    public static final int EXPANDED = 3;
    public static final int ON_PATH_UNBLOCKED = 4;
    public static final int ON_PATH_UNKNOWN = 5;
    public static final int START_CELL = 6;
    public static final int TARGET_CELL = 7;
    public static final int CURRENT_POSITION = 8;

    // Prevent instantiation
    private Constants() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}