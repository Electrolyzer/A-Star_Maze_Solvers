
// MazeSolverTester.java
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.*;
import java.util.ArrayList;

import src.Coordinates;
import src.MazeGenerator;
import src.MazeSolver;
import src.SolveResult;
import static src.Constants.*;

import java.awt.*;

/**
 * Driver class to run Repeated Forward A*
 */
public class MazeSolverTester {
    public static void main(String[] args) throws InterruptedException {
        SwingUtilities.invokeLater(() -> {
            InteractiveMazeEditor editor = new InteractiveMazeEditor(30);
            editor.setVisible(true);
        });
    }

    // Interactive Maze Editor Class
    private static class InteractiveMazeEditor extends JFrame {
        private int[][] maze;
        private int[][] unknownMaze;
        private int[] startPos;
        private int[] endPos;
        private static final int SQUARE_SIZE = 15; // Square size in pixels
        private static final int PADDING = 10; // Padding in pixels
        private EditMode currentMode = EditMode.SET_START;
        private String currentAlgorithm = "Forward";
        private char tiebreaker = 'g'; // Default to g
        private int sightRadius = 1; // Default sight radius
        private List<int[][]> currentSteps;
        private int currentStepIndex = 0;
        private JButton resetButton;
        private JButton clearButton;
        private JButton saveResultButton;
        private JButton loadResultsButton;
        private List<SolveResult> savedResults = new ArrayList<>();
        private MultiResultViewer multiResultViewer;

        private enum EditMode {
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

        private JComboBox<String> algorithmCombo;
        private JButton solveButton;
        private StepMazeView stepMazeView;
        private JPanel mazePanelContainer;
        private MazePanel editMazePanel;

        public InteractiveMazeEditor(int size) {
            this.maze = MazeGenerator.generateMaze(size);
            this.unknownMaze = new int[size][size];
            for (int[] row : unknownMaze)
                Arrays.fill(row, 2);

            // Set default start and end positions
            startPos = new int[] { 0, 0 };
            endPos = new int[] { size - 1, size - 1 };
            maze[startPos[0]][startPos[1]] = START_CELL;
            unknownMaze[startPos[0]][startPos[1]] = START_CELL;
            maze[endPos[0]][endPos[1]] = TARGET_CELL;
            unknownMaze[endPos[0]][endPos[1]] = TARGET_CELL;

            setupUI(size);
        }

        private void setupUI(int size) {
            setTitle("Maze Solver");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // Create maze panel for editing
            editMazePanel = new MazePanel();
            JScrollPane editScrollPane = new JScrollPane(editMazePanel);

            // Create step viewer panel (initially hidden)
            stepMazeView = new StepMazeView(new int[size][size]);
            JScrollPane stepScrollPane = new JScrollPane(stepMazeView);

            // Create multi-result viewer panel
            multiResultViewer = new MultiResultViewer();
            JScrollPane multiScrollPane = new JScrollPane(multiResultViewer);

            // Container for switching between edit, step, and multi views
            mazePanelContainer = new JPanel(new CardLayout());
            mazePanelContainer.add(editScrollPane, "EDIT");
            mazePanelContainer.add(stepScrollPane, "STEPS");
            mazePanelContainer.add(multiScrollPane, "MULTI");
            add(mazePanelContainer, BorderLayout.CENTER);

            // Create control panel
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Mode selection
            JPanel modePanel = new JPanel(new FlowLayout());
            modePanel.add(new JLabel("Edit Mode: "));
            JComboBox<EditMode> modeCombo = new JComboBox<>(EditMode.values());
            modeCombo.addActionListener(e -> currentMode = (EditMode) modeCombo.getSelectedItem());
            modePanel.add(modeCombo);
            controlPanel.add(modePanel);

            // Create a panel for all solver settings
            JPanel solverSettingsPanel = new JPanel();
            solverSettingsPanel.setLayout(new BoxLayout(solverSettingsPanel, BoxLayout.Y_AXIS));
            solverSettingsPanel.setBorder(BorderFactory.createTitledBorder("Solver Settings"));

            // Add algorithm selection
            JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            algorithmPanel.add(new JLabel("Algorithm: "));
            algorithmCombo = new JComboBox<>(new String[] { "Forward", "Backward", "Adaptive" });
            algorithmCombo.addActionListener(e -> currentAlgorithm = (String) algorithmCombo.getSelectedItem());
            algorithmPanel.add(algorithmCombo);
            solverSettingsPanel.add(algorithmPanel);

            // Add tiebreaker selection
            JPanel tiebreakerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            tiebreakerPanel.add(new JLabel("Tiebreaker: "));
            ButtonGroup tiebreakerGroup = new ButtonGroup();
            JRadioButton gButton = new JRadioButton("g");
            JRadioButton hButton = new JRadioButton("h");
            gButton.setSelected(true);
            gButton.addActionListener(e -> tiebreaker = 'g');
            hButton.addActionListener(e -> tiebreaker = 'h');
            tiebreakerGroup.add(gButton);
            tiebreakerGroup.add(hButton);
            tiebreakerPanel.add(gButton);
            tiebreakerPanel.add(hButton);
            solverSettingsPanel.add(tiebreakerPanel);

            // Add sight radius selection
            JPanel sightRadiusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            sightRadiusPanel.add(new JLabel("Sight Radius: "));
            JSpinner sightRadiusSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
            sightRadiusSpinner.addChangeListener(e -> sightRadius = (Integer) sightRadiusSpinner.getValue());
            sightRadiusPanel.add(sightRadiusSpinner);
            JLabel sightRadiusHelp = new JLabel("(Manhattan distance)");
            sightRadiusHelp.setFont(sightRadiusHelp.getFont().deriveFont(Font.ITALIC, 10f));
            sightRadiusPanel.add(sightRadiusHelp);
            solverSettingsPanel.add(sightRadiusPanel);

            // Add to control panel
            controlPanel.add(solverSettingsPanel);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            resetButton = new JButton("Reset Maze");
            resetButton.addActionListener(e -> resetMaze());

            clearButton = new JButton("Clear Start/End");
            clearButton.addActionListener(e -> clearStartEnd());

            solveButton = new JButton("Solve Maze");
            solveButton.addActionListener(e -> solveMaze());

            saveResultButton = new JButton("Save Result");
            saveResultButton.addActionListener(e -> saveCurrentResult());
            saveResultButton.setEnabled(false);

            loadResultsButton = new JButton("View Saved Results");
            loadResultsButton.addActionListener(e -> viewSavedResults());

            buttonPanel.add(resetButton);
            buttonPanel.add(clearButton);
            buttonPanel.add(solveButton);
            buttonPanel.add(saveResultButton);
            buttonPanel.add(loadResultsButton);
            controlPanel.add(buttonPanel);

            // Legend
            JPanel legendPanel = createLegendPanel();
            controlPanel.add(legendPanel);

            // Instructions
            JTextArea instructions = new JTextArea(
                    "Instructions:\n" +
                            "1. Select 'Set Start' and click to place start position (green)\n" +
                            "2. Select 'Set End' and click to place end position (red)\n" +
                            "3. Use 'Add Wall' to place walls (black) or 'Remove Wall' to remove them\n" +
                            "4. Set sight radius (how far agent can see using Manhattan distance)\n" +
                            "5. Choose algorithm and click 'Solve Maze' to run the selected algorithm");
            instructions.setEditable(false);
            instructions.setBackground(getBackground());
            instructions.setBorder(BorderFactory.createTitledBorder("Instructions"));
            controlPanel.add(instructions);

            add(controlPanel, BorderLayout.EAST);

            // Set size and center
            pack();
            setLocationRelativeTo(null);
            setMinimumSize(new Dimension(800, 600));
        }

        private JPanel createLegendPanel() {
            JPanel legendPanel = new JPanel();
            legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
            legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));

            addLegendItem(legendPanel, Color.GREEN, "Start Position");
            addLegendItem(legendPanel, Color.RED, "End Position");
            addLegendItem(legendPanel, Color.BLACK, "Wall");
            addLegendItem(legendPanel, Color.WHITE, "Open Path");
            addLegendItem(legendPanel, Color.GRAY, "Unknown");
            addLegendItem(legendPanel, Color.PINK, "Expanded (Visited)");
            addLegendItem(legendPanel, Color.YELLOW, "Planned Path");
            addLegendItemWithDot(legendPanel, Color.YELLOW, "Current Location");

            return legendPanel;
        }

        private void addLegendItem(JPanel panel, Color color, String description) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel colorBox = new JPanel();
            colorBox.setPreferredSize(new Dimension(20, 20));
            colorBox.setBackground(color);
            colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            item.add(colorBox);
            item.add(new JLabel(description));
            panel.add(item);
        }

        private void addLegendItemWithDot(JPanel panel, Color color, String description) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel colorBox = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw background color
                    g.setColor(color);
                    g.fillRect(0, 0, getWidth(), getHeight());

                    // Draw border (matches original)
                    g.setColor(Color.BLACK);
                    g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                    // Draw centered black dot
                    int padding = getWidth() / 4; // Same padding calculation as in StepMazeView
                    g.fillOval(padding, padding,
                            getWidth() - 2 * padding,
                            getHeight() - 2 * padding);
                }
            };
            colorBox.setPreferredSize(new Dimension(20, 20));
            item.add(colorBox);
            item.add(new JLabel(description));
            panel.add(item);
        }

        private class MazePanel extends JPanel {
            public MazePanel() {
                setPreferredSize(new Dimension(
                        maze[0].length * SQUARE_SIZE + 2 * PADDING,
                        maze.length * SQUARE_SIZE + 2 * PADDING));

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        handleMouseClick(e.getX() - PADDING, e.getY() - PADDING);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Apply padding
                g.translate(PADDING, PADDING);

                // Draw the maze
                for (int row = 0; row < maze.length; row++) {
                    for (int col = 0; col < maze[0].length; col++) {
                        Color color = switch (maze[row][col]) {
                            case UNBLOCKED -> Color.WHITE;
                            case BLOCKED -> Color.BLACK;
                            case START_CELL -> Color.GREEN;
                            case TARGET_CELL -> Color.RED;
                            default -> Color.WHITE;
                        };

                        g.setColor(color);
                        g.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                        g.setColor(Color.GRAY);
                        g.drawRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                    }
                }

                // Reset translation
                g.translate(-PADDING, -PADDING);
            }
        }

        private void handleMouseClick(int x, int y) {
            int col = x / SQUARE_SIZE;
            int row = y / SQUARE_SIZE;

            if (row < 0 || row >= maze.length || col < 0 || col >= maze[0].length) {
                return;
            }

            switch (currentMode) {
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

        private void setStart(int row, int col) {
            // Ensure start is not set on end position
            if (endPos != null && endPos[0] == row && endPos[1] == col) {
                JOptionPane.showMessageDialog(this,
                        "Cannot set start on end position!",
                        "Invalid Position", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Clear previous start
            if (startPos != null) {
                maze[startPos[0]][startPos[1]] = UNBLOCKED;
                unknownMaze[startPos[0]][startPos[1]] = UNKNOWN;
            }
            // Set new start
            startPos = new int[] { row, col };
            maze[row][col] = START_CELL;
            unknownMaze[row][col] = START_CELL;
        }

        private void setEnd(int row, int col) {
            // Ensure end is not set on start position
            if (startPos != null && startPos[0] == row && startPos[1] == col) {
                JOptionPane.showMessageDialog(this,
                        "Cannot set end on start position!",
                        "Invalid Position", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Clear previous end
            if (endPos != null) {
                maze[endPos[0]][endPos[1]] = UNBLOCKED;
                unknownMaze[endPos[0]][endPos[1]] = UNKNOWN;
            }
            // Set new end
            endPos = new int[] { row, col };
            maze[row][col] = TARGET_CELL;
            unknownMaze[row][col] = TARGET_CELL;
        }

        private void resetMaze() {
            // Regenerate maze
            int size = maze.length;
            maze = MazeGenerator.generateMaze(size);
            unknownMaze = new int[size][size];
            for (int[] row : unknownMaze)
                Arrays.fill(row, 2);

            // Reset to default positions
            startPos = new int[] { 0, 0 };
            endPos = new int[] { size - 1, size - 1 };
            maze[startPos[0]][startPos[1]] = START_CELL;
            unknownMaze[startPos[0]][startPos[1]] = START_CELL;
            maze[endPos[0]][endPos[1]] = TARGET_CELL;
            unknownMaze[endPos[0]][endPos[1]] = TARGET_CELL;

            editMazePanel.repaint();
            resetStepView();
        }

        private void clearStartEnd() {
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

        private SolveResult lastResult;

        private void solveMaze() {
            if (startPos == null || endPos == null) {
                JOptionPane.showMessageDialog(this,
                        "Please set both start and end positions before solving!",
                        "Missing Positions", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int size = maze.length;
            MazeSolver runner = new MazeSolver(unknownMaze, maze,
                    Coordinates.get1DFrom2D(startPos[1], startPos[0], size),
                    Coordinates.get1DFrom2D(endPos[1], endPos[0], size), tiebreaker, true, sightRadius);

            SolveResult result;
            switch (currentAlgorithm) {
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

            lastResult = result;
            currentSteps = runner.getSteps();
            if (!currentSteps.isEmpty()) {
                // Switch to step view
                CardLayout cl = (CardLayout) mazePanelContainer.getLayout();
                cl.show(mazePanelContainer, "STEPS");

                // Start at the last step for ease of viewing
                currentStepIndex = currentSteps.size() - 1; // Start at max index
                updateStepView();

                // Update button text to indicate we can go back to editing
                solveButton.setText("Edit Maze");
                solveButton.removeActionListener(solveButton.getActionListeners()[0]);
                solveButton.addActionListener(e -> returnToEditMode());

                // Disable edit controls, enable save
                resetButton.setEnabled(false);
                clearButton.setEnabled(false);
                saveResultButton.setEnabled(true);
            }

            String message = result.solved
                    ? String.format("Maze solved! Expanded cells: %d (Sight radius: %d)", result.expandedCells,
                            sightRadius)
                    : "No path found to target.";

            JOptionPane.showMessageDialog(this, message,
                    "Solve Result", JOptionPane.INFORMATION_MESSAGE);
        }

        private void returnToEditMode() {
            CardLayout cl = (CardLayout) mazePanelContainer.getLayout();
            cl.show(mazePanelContainer, "EDIT");

            // Reset button text and action
            solveButton.setText("Solve Maze");
            solveButton.removeActionListener(solveButton.getActionListeners()[0]);
            solveButton.addActionListener(e -> solveMaze());

            // Re-enable edit controls, disable save
            resetButton.setEnabled(true);
            clearButton.setEnabled(true);
            saveResultButton.setEnabled(false);

            resetStepView();
            unknownMaze = new int[maze.length][maze.length];
            for (int[] row : unknownMaze)
                Arrays.fill(row, 2);
        }

        private void saveCurrentResult() {
            if (lastResult == null) {
                JOptionPane.showMessageDialog(this, "No result to save!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Result files", "result"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".result")) {
                    file = new File(file.getAbsolutePath() + ".result");
                }

                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos.writeObject(lastResult);
                    JOptionPane.showMessageDialog(this, "Result saved successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error saving result: " + e.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void viewSavedResults() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Result files", "result"));
            fileChooser.setMultiSelectionEnabled(true);

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                List<SolveResult> loadedResults = new ArrayList<>();

                for (File file : files) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        SolveResult result = (SolveResult) ois.readObject();
                        loadedResults.add(result);
                    } catch (IOException | ClassNotFoundException e) {
                        JOptionPane.showMessageDialog(this, "Error loading " + file.getName() + ": " + e.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

                if (!loadedResults.isEmpty()) {
                    multiResultViewer.setResults(loadedResults);
                    CardLayout cl = (CardLayout) mazePanelContainer.getLayout();
                    cl.show(mazePanelContainer, "MULTI");

                    // Update button to return to edit mode
                    solveButton.setText("Edit Maze");
                    solveButton.removeActionListener(solveButton.getActionListeners()[0]);
                    solveButton.addActionListener(e -> returnToEditMode());

                    resetButton.setEnabled(false);
                    clearButton.setEnabled(false);
                    saveResultButton.setEnabled(false);
                }
            }
        }

        private void resetStepView() {
            currentSteps = null;
            currentStepIndex = 0;
            stepMazeView.updateMaze(new int[maze.length][maze.length]);
            stepMazeView.updateStepControls(0, 0);
        }

        private void updateStepView() {
            if (currentSteps != null && !currentSteps.isEmpty()) {
                stepMazeView.updateMaze(currentSteps.get(currentStepIndex));
                stepMazeView.updateStepControls(currentStepIndex, currentSteps.size());
            }
        }

        private class StepMazeView extends JPanel {
            private int[][] maze;
            private JButton prevButton;
            private JButton nextButton;
            private JLabel stepLabel;
            private JSlider stepSlider;

            public StepMazeView(int[][] maze) {
                this.maze = deepCopy(maze);
                setLayout(new BorderLayout());

                // Create the drawing panel
                JPanel drawingPanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        drawMaze(g);
                    }

                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(
                                maze[0].length * SQUARE_SIZE,
                                maze.length * SQUARE_SIZE);
                    }
                };

                // Create scroll pane for the drawing panel
                JScrollPane scrollPane = new JScrollPane(drawingPanel);
                add(scrollPane, BorderLayout.CENTER);

                // Create step navigation panel at the bottom
                JPanel stepPanel = new JPanel(new BorderLayout());
                JPanel buttonPanel = new JPanel(new FlowLayout());

                prevButton = new JButton("◀ Previous");
                prevButton.addActionListener(e -> {
                    if (currentStepIndex > 0) {
                        currentStepIndex--;
                        updateStepView();
                        stepSlider.setValue(currentStepIndex); // Update slider when button is pressed
                    }
                });
                prevButton.setEnabled(false);

                stepLabel = new JLabel("Step: 0 / 0");
                stepLabel.setPreferredSize(new Dimension(100, 20));

                nextButton = new JButton("Next ▶");
                nextButton.addActionListener(e -> {
                    if (currentStepIndex < currentSteps.size() - 1) {
                        currentStepIndex++;
                        updateStepView();
                        stepSlider.setValue(currentStepIndex); // Update slider when button is pressed
                    }
                });
                nextButton.setEnabled(false);

                // Create slider with improved tick settings
                stepSlider = new JSlider(0, 0, 0);
                stepSlider.setPaintTicks(true);
                stepSlider.setPaintLabels(true);

                // Configure tick spacing based on step count
                ChangeListener sliderChangeListener = new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        if (currentSteps != null && !currentSteps.isEmpty()) {
                            int newStep = stepSlider.getValue();
                            if (newStep != currentStepIndex) {
                                currentStepIndex = newStep;
                                updateStepView(); // Update maze immediately when slider moves
                            }
                        }
                    }
                };
                stepSlider.addChangeListener(sliderChangeListener);

                buttonPanel.add(prevButton);
                buttonPanel.add(stepLabel);
                buttonPanel.add(nextButton);

                stepPanel.add(buttonPanel, BorderLayout.NORTH);
                stepPanel.add(stepSlider, BorderLayout.CENTER);
                add(stepPanel, BorderLayout.SOUTH);
            }

            private void drawMaze(Graphics g) {
                // Apply padding for better visibility
                g.translate(PADDING, PADDING);

                for (int row = 0; row < maze.length; row++) {
                    for (int col = 0; col < maze[0].length; col++) {
                        Color color = switch (maze[row][col]) {
                            case UNBLOCKED -> Color.WHITE;
                            case BLOCKED -> Color.BLACK;
                            case UNKNOWN -> Color.GRAY;
                            case EXPANDED -> Color.PINK;
                            case ON_PATH_UNBLOCKED -> Color.YELLOW;
                            case ON_PATH_UNKNOWN -> Color.YELLOW;
                            case START_CELL -> Color.GREEN;
                            case TARGET_CELL -> Color.RED;
                            case CURRENT_POSITION -> Color.YELLOW;
                            default -> Color.BLUE;
                        };
                        g.setColor(color);
                        g.fillRect(SQUARE_SIZE * col, SQUARE_SIZE * row, SQUARE_SIZE, SQUARE_SIZE);
                        g.setColor(Color.BLACK);
                        g.drawRect(SQUARE_SIZE * col, SQUARE_SIZE * row, SQUARE_SIZE, SQUARE_SIZE);
                        if (maze[row][col] == CURRENT_POSITION) {
                            g.setColor(Color.BLACK);
                            int padding = SQUARE_SIZE / 4;
                            g.fillOval(
                                    SQUARE_SIZE * col + padding,
                                    SQUARE_SIZE * row + padding,
                                    SQUARE_SIZE - 2 * padding,
                                    SQUARE_SIZE - 2 * padding);
                        }
                    }
                }
                g.translate(-PADDING, -PADDING);
            }

            public void updateMaze(int[][] newMaze) {
                this.maze = deepCopy(newMaze);
                repaint();
            }

            private int[][] deepCopy(int[][] original) {
                int[][] copy = new int[original.length][original[0].length];
                for (int i = 0; i < original.length; i++) {
                    System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
                }
                return copy;
            }

            public void updateStepControls(int currentIndex, int totalSteps) {
                // Update label and buttons
                stepLabel.setText(String.format("Step: %d / %d", currentIndex + 1, totalSteps));
                prevButton.setEnabled(currentIndex > 0);
                nextButton.setEnabled(currentIndex < totalSteps - 1);

                // Update slider configuration
                stepSlider.setMaximum(Math.max(0, totalSteps - 1));
                System.out.println("Total Steps: " + totalSteps + ", Current Index: " + currentIndex);
                // Configure ticks based on step count
                if (totalSteps > 1) {
                    int minorTickSpacing = Math.max(1, totalSteps / 40); // Approximately 40 minor ticks
                    int majorTickSpacing = 2 * minorTickSpacing; // Approximately 20 major ticks
                    System.out.println(
                            "Minor Tick Spacing: " + minorTickSpacing + ", Major Tick Spacing: " + majorTickSpacing);
                    stepSlider.setMajorTickSpacing(majorTickSpacing);
                    stepSlider.setMinorTickSpacing(minorTickSpacing);
                    stepSlider.setPaintTicks(true);
                    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
                    for (int i = 0; i < totalSteps; i += majorTickSpacing) {
                        labelTable.put(i, new JLabel(String.valueOf(i + 1)));
                    }
                    stepSlider.setLabelTable(labelTable);
                }

                // Update slider position if needed
                if (stepSlider.getValue() != currentIndex) {
                    stepSlider.setValue(currentIndex);
                }

                // Force repaint to update ticks
                stepSlider.repaint();
            }
        }

        private class MultiResultViewer extends JPanel {
            private List<SolveResult> results = new ArrayList<>();
            private List<ResultPanel> resultPanels = new ArrayList<>();
            private JSlider masterSlider;
            private JButton prevButton;
            private JButton nextButton;
            private JLabel stepLabel;
            private int currentStep = 0;
            private int maxSteps = 0;

            public MultiResultViewer() {
                setLayout(new BorderLayout());
                setupControls();
            }

            private void setupControls() {
                JPanel controlPanel = new JPanel(new BorderLayout());
                JPanel buttonPanel = new JPanel(new FlowLayout());

                prevButton = new JButton("◀ Previous");
                prevButton.addActionListener(e -> {
                    if (currentStep > 0) {
                        currentStep--;
                        updateAllViews();
                    }
                });

                stepLabel = new JLabel("Step: 0 / 0");
                stepLabel.setPreferredSize(new Dimension(100, 20));

                nextButton = new JButton("Next ▶");
                nextButton.addActionListener(e -> {
                    if (currentStep < maxSteps - 1) {
                        currentStep++;
                        updateAllViews();
                    }
                });

                masterSlider = new JSlider(0, 0, 0);
                masterSlider.addChangeListener(e -> {
                    if (!masterSlider.getValueIsAdjusting()) {
                        currentStep = masterSlider.getValue();
                        updateAllViews();
                    }
                });

                buttonPanel.add(prevButton);
                buttonPanel.add(stepLabel);
                buttonPanel.add(nextButton);

                controlPanel.add(buttonPanel, BorderLayout.NORTH);
                controlPanel.add(masterSlider, BorderLayout.CENTER);
                add(controlPanel, BorderLayout.SOUTH);
            }

            public void setResults(List<SolveResult> newResults) {
                this.results = new ArrayList<>(newResults);
                this.resultPanels.clear();

                // Remove existing result panels
                Component[] components = getComponents();
                for (Component comp : components) {
                    if (comp instanceof JScrollPane) {
                        remove(comp);
                    }
                }

                if (results.isEmpty()) {
                    maxSteps = 0;
                    currentStep = 0;
                    updateControls();
                    return;
                }

                // Calculate grid layout
                int numResults = results.size();
                int cols = (int) Math.ceil(Math.sqrt(numResults));
                int rows = (int) Math.ceil((double) numResults / cols);

                JPanel gridPanel = new JPanel(new GridLayout(rows, cols, 5, 5));
                gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Find maximum steps across all results
                maxSteps = results.stream()
                        .filter(r -> r.solutionSteps != null)
                        .mapToInt(r -> r.solutionSteps.size())
                        .max()
                        .orElse(1);

                // Create result panels
                for (SolveResult result : results) {
                    ResultPanel panel = new ResultPanel(result);
                    resultPanels.add(panel);
                    gridPanel.add(panel);
                }

                JScrollPane scrollPane = new JScrollPane(gridPanel);
                add(scrollPane, BorderLayout.CENTER);

                currentStep = maxSteps - 1; // Start at the end
                updateControls();
                updateAllViews();

                revalidate();
                repaint();
            }

            private void updateAllViews() {
                for (ResultPanel panel : resultPanels) {
                    panel.updateToStep(currentStep);
                }
                updateControls();
            }

            private void updateControls() {
                stepLabel.setText(String.format("Step: %d / %d", currentStep + 1, maxSteps));
                prevButton.setEnabled(currentStep > 0);
                nextButton.setEnabled(currentStep < maxSteps - 1);

                masterSlider.setMaximum(Math.max(0, maxSteps - 1));
                masterSlider.setValue(currentStep);

                if (maxSteps > 1) {
                    int minorTickSpacing = Math.max(1, maxSteps / 40);
                    int majorTickSpacing = Math.max(2, maxSteps / 20);
                    masterSlider.setMajorTickSpacing(majorTickSpacing);
                    masterSlider.setMinorTickSpacing(minorTickSpacing);
                    masterSlider.setPaintTicks(true);
                    masterSlider.setPaintLabels(true);
                }
            }

            private class ResultPanel extends JPanel {
                private SolveResult result;
                private int[][] currentMaze;
                private JLabel titleLabel;
                private static final int MINI_SQUARE_SIZE = 8;

                public ResultPanel(SolveResult result) {
                    this.result = result;
                    this.currentMaze = result.originalMaze != null ? deepCopy(result.originalMaze) : new int[10][10];

                    setLayout(new BorderLayout());
                    setBorder(BorderFactory.createTitledBorder(result.getDisplayName()));

                    titleLabel = new JLabel(String.format("Expanded: %d, Time: %dms",
                            result.expandedCells, result.solutionTimeMs));
                    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    add(titleLabel, BorderLayout.NORTH);

                    setPreferredSize(new Dimension(
                            currentMaze[0].length * MINI_SQUARE_SIZE + 20,
                            currentMaze.length * MINI_SQUARE_SIZE + 60));
                }

                public void updateToStep(int step) {
                    if (result.solutionSteps != null && !result.solutionSteps.isEmpty()) {
                        int actualStep = Math.min(step, result.solutionSteps.size() - 1);
                        currentMaze = deepCopy(result.solutionSteps.get(actualStep));
                    }
                    repaint();
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    // Calculate drawing area
                    int titleHeight = titleLabel.getPreferredSize().height;
                    int drawingY = titleHeight + 10;
                    int drawingHeight = getHeight() - drawingY - 10;
                    int drawingWidth = getWidth() - 20;

                    // Calculate square size to fit
                    int squareSize = Math.min(
                            drawingWidth / currentMaze[0].length,
                            drawingHeight / currentMaze.length);
                    squareSize = Math.max(1, squareSize); // Minimum size of 1

                    // Center the maze
                    int startX = (getWidth() - currentMaze[0].length * squareSize) / 2;
                    int startY = drawingY + (drawingHeight - currentMaze.length * squareSize) / 2;

                    for (int row = 0; row < currentMaze.length; row++) {
                        for (int col = 0; col < currentMaze[0].length; col++) {
                            Color color = switch (currentMaze[row][col]) {
                                case UNBLOCKED -> Color.WHITE;
                                case BLOCKED -> Color.BLACK;
                                case UNKNOWN -> Color.GRAY;
                                case EXPANDED -> Color.PINK;
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

                            if (currentMaze[row][col] == CURRENT_POSITION && squareSize > 4) {
                                g.setColor(Color.BLACK);
                                int padding = squareSize / 4;
                                g.fillOval(startX + col * squareSize + padding,
                                        startY + row * squareSize + padding,
                                        squareSize - 2 * padding,
                                        squareSize - 2 * padding);
                            }
                        }
                    }
                }

                private int[][] deepCopy(int[][] original) {
                    int[][] copy = new int[original.length][original[0].length];
                    for (int i = 0; i < original.length; i++) {
                        System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
                    }
                    return copy;
                }
            }
        }
    }
}