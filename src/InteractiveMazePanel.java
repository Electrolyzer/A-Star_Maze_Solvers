// src/InteractiveMazePanel.java
package src;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;

import static src.Constants.*;

/**
 * Interactive maze panel for editing and solving mazes with step-by-step visualization
 */
public class InteractiveMazePanel extends JPanel {
    private int[][] maze;
    private int[][] unknownMaze;
    private int[] startPos;
    private int[] endPos;
    private static final int PADDING = 10;
    private EditMode currentEditMode = EditMode.SET_START;
    private SolveResult currentResult;
    private JPanel mazePanelContainer;
    private MazePanel editMazePanel;
    private ResultViewer stepMazeView;
    private boolean isInSolveMode = false;
    private AlgorithmConfig currentSolverConfig;
    private Runnable buttonUpdateCallback;

    public enum EditMode {
        SET_START("Set Start"),
        SET_END("Set End"),
        ADD_WALL("Add Wall"),
        REMOVE_WALL("Remove Wall");

        private final String displayName;

        EditMode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public InteractiveMazePanel() {
        setLayout(new BorderLayout());
        initializeMaze(30); // Default size
        setupMazeUI();
    }

    public void setCurrentSolverConfig(AlgorithmConfig config) {
        this.currentSolverConfig = config;
    }

    public void setButtonUpdateCallback(Runnable callback) {
        this.buttonUpdateCallback = callback;
    }

    private void initializeMaze(int size) {
        this.maze = MazeGenerator.generateMaze(size);
        this.unknownMaze = new int[size][size];
        for (int[] row : unknownMaze)
            Arrays.fill(row, UNKNOWN);

        // Set default start and end positions
        startPos = new int[] { 0, 0 };
        endPos = new int[] { size - 1, size - 1 };
        maze[startPos[0]][startPos[1]] = START_CELL;
        unknownMaze[startPos[0]][startPos[1]] = START_CELL;
        maze[endPos[0]][endPos[1]] = TARGET_CELL;
        unknownMaze[endPos[0]][endPos[1]] = TARGET_CELL;
    }

    private void setupMazeUI() {
        // Create maze panel for editing
        editMazePanel = new MazePanel();
        JScrollPane editScrollPane = new JScrollPane(editMazePanel);

        // Create step viewer panel
        stepMazeView = new ResultViewer();
        JScrollPane stepScrollPane = new JScrollPane(stepMazeView);

        // Container for switching between views
        mazePanelContainer = new JPanel(new CardLayout());
        mazePanelContainer.add(editScrollPane, "EDIT");
        mazePanelContainer.add(stepScrollPane, "STEPS");
        
        add(mazePanelContainer, BorderLayout.CENTER);
    }

    public void setEditMode(EditMode mode) {
        this.currentEditMode = mode;
    }

    public void resetMaze() {
        int size = maze.length;
        initializeMaze(size);
        editMazePanel.repaint();
        resetStepView();
        returnToEditMode();
    }

    public void clearStartEnd() {
        if (startPos != null) {
            maze[startPos[0]][startPos[1]] = UNBLOCKED;
            unknownMaze[startPos[0]][startPos[1]] = UNKNOWN;
            startPos = null;
        }
        if (endPos != null) {
            maze[endPos[0]][endPos[1]] = UNBLOCKED;
            unknownMaze[endPos[0]][endPos[1]] = UNKNOWN;
            endPos = null;
        }
        editMazePanel.repaint();
        resetStepView();
    }

    public void solveMaze() {
        if (startPos == null || endPos == null) {
            JOptionPane.showMessageDialog(this,
                    "Please set both start and end positions before solving!",
                    "Missing Positions", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentSolverConfig == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an algorithm configuration!",
                    "No Configuration", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int size = maze.length;
        MazeSolver runner = new MazeSolver(unknownMaze, maze,
                Coordinates.get1DFrom2D(startPos[1], startPos[0], size),
                Coordinates.get1DFrom2D(endPos[1], endPos[0], size), 
                currentSolverConfig.getTiebreaker(), true, currentSolverConfig.getSightRadius());

        SolveResult result;
        switch (currentSolverConfig.getAlgorithmType()) {
            case "Backward":
                result = runner.solveBackward();
                break;
            case "Adaptive":
                result = runner.solveAdaptive();
                break;
            default:
                result = runner.solveForward();
                break;
        }

        currentResult = result;
        if (!currentResult.solutionSteps().isEmpty()) {
            // Switch to step view
            CardLayout cl = (CardLayout) mazePanelContainer.getLayout();
            cl.show(mazePanelContainer, "STEPS");
            updateStepView();
            isInSolveMode = true;
            if (buttonUpdateCallback != null) {
                buttonUpdateCallback.run();
            }
        }
    }

    public void returnToEditMode() {
        CardLayout cl = (CardLayout) mazePanelContainer.getLayout();
        cl.show(mazePanelContainer, "EDIT");
        isInSolveMode = false;
        if (buttonUpdateCallback != null) {
            buttonUpdateCallback.run();
        }
        resetStepView();
        
        // Reset unknown maze
        unknownMaze = new int[maze.length][maze.length];
        for (int[] row : unknownMaze)
            Arrays.fill(row, UNKNOWN);
        
        // Restore start and end positions
        if (startPos != null) {
            unknownMaze[startPos[0]][startPos[1]] = START_CELL;
        }
        if (endPos != null) {
            unknownMaze[endPos[0]][endPos[1]] = TARGET_CELL;
        }
    }

    public boolean isInSolveMode() {
        return isInSolveMode;
    }

    public SolveResult getCurrentResult() {
        return currentResult;
    }

    private void resetStepView() {
        currentResult = null;
        stepMazeView.setResults(new ArrayList<>());
    }

    private void updateStepView() {
        if (currentResult != null && !currentResult.solutionSteps().isEmpty()) {
            stepMazeView.setResults(List.of(currentResult));
        }
    }

    private class MazePanel extends JPanel {
        int squareSize = 15;
        int startX = 0;
        int startY = 0;

        public MazePanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMouseClick(e.getX() - startX, e.getY() - startY);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int drawingHeight = getHeight() - 2 * PADDING;
            int drawingWidth = getWidth() - 2 * PADDING;

            // Calculate square size to fit
            squareSize = Math.min(
                    drawingWidth / maze[0].length,
                    drawingHeight / maze.length);
            squareSize = Math.max(1, squareSize);

            // Center the maze
            startX = (getWidth() - maze[0].length * squareSize) / 2;
            startY = (drawingHeight - maze.length * squareSize) / 2;

            for (int row = 0; row < maze.length; row++) {
                for (int col = 0; col < maze[0].length; col++) {
                    Color color = switch (maze[row][col]) {
                        case UNBLOCKED -> Color.WHITE;
                        case BLOCKED -> Color.BLACK;
                        case UNKNOWN -> Color.GRAY;
                        case EXPLORED -> Color.PINK;
                        case ON_PATH_UNBLOCKED, ON_PATH_UNKNOWN -> Color.YELLOW;
                        case START_CELL -> Color.GREEN;
                        case TARGET_CELL -> Color.RED;
                        case CURRENT_POSITION -> Color.YELLOW;
                        default -> Color.BLUE;
                    };

                    g.setColor(color);
                    g.fillRect(startX + col * squareSize, startY + row * squareSize,
                            squareSize, squareSize);

                    if (squareSize > 2) {
                        g.setColor(Color.BLACK);
                        g.drawRect(startX + col * squareSize, startY + row * squareSize,
                                squareSize, squareSize);
                    }
                }
            }
        }

        private void handleMouseClick(int x, int y) {
            int col = x / squareSize;
            int row = y / squareSize;

            if (row < 0 || row >= maze.length || col < 0 || col >= maze[0].length) {
                return;
            }

            switch (currentEditMode) {
                case SET_START:
                    setStart(row, col);
                    break;
                case SET_END:
                    setEnd(row, col);
                    break;
                case ADD_WALL:
                    if (maze[row][col] != START_CELL && maze[row][col] != TARGET_CELL) {
                        maze[row][col] = BLOCKED;
                    }
                    break;
                case REMOVE_WALL:
                    if (maze[row][col] == BLOCKED) {
                        maze[row][col] = UNBLOCKED;
                    }
                    break;
            }
            repaint();
        }
    }

    private void setStart(int row, int col) {
        if (endPos != null && endPos[0] == row && endPos[1] == col) {
            JOptionPane.showMessageDialog(this,
                    "Cannot set start on end position!",
                    "Invalid Position", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (startPos != null) {
            maze[startPos[0]][startPos[1]] = UNBLOCKED;
            unknownMaze[startPos[0]][startPos[1]] = UNKNOWN;
        }
        startPos = new int[] { row, col };
        maze[row][col] = START_CELL;
        unknownMaze[row][col] = START_CELL;
    }

    private void setEnd(int row, int col) {
        if (startPos != null && startPos[0] == row && startPos[1] == col) {
            JOptionPane.showMessageDialog(this,
                    "Cannot set end on start position!",
                    "Invalid Position", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (endPos != null) {
            maze[endPos[0]][endPos[1]] = UNBLOCKED;
            unknownMaze[endPos[0]][endPos[1]] = UNKNOWN;
        }
        endPos = new int[] { row, col };
        maze[row][col] = TARGET_CELL;
        unknownMaze[row][col] = TARGET_CELL;
    }
}
