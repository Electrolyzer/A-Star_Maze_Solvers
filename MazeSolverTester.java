
// MazeSolverTester.java
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
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
        private static final int PADDING = 10; // Padding in pixels
        private EditMode currentMode = EditMode.SET_START;
        private String currentAlgorithm = "Forward";
        private char tiebreaker = 'g'; // Default to g
        private int sightRadius = 1; // Default sight radius
        private SolveResult currentResult;
        private JButton resetButton;
        private JButton clearButton;
        private JButton saveResultButton;
        private JButton loadResultsButton;
        private ResultViewer multiResultViewer;
        private ResultViewer stepMazeView;

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

            setupUI();
        }

        private void setupUI() {
            setTitle("Maze Solver");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // Create maze panel for editing
            editMazePanel = new MazePanel();
            JScrollPane editScrollPane = new JScrollPane(editMazePanel);

            // Create step viewer panel (initially hidden)
            stepMazeView = new ResultViewer();
            JScrollPane stepScrollPane = new JScrollPane(stepMazeView);

            // Create multi-result viewer panel
            multiResultViewer = new ResultViewer();
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
            setExtendedState(JFrame.MAXIMIZED_BOTH);
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
            addLegendItem(legendPanel, Color.PINK, "Explored (Visited)");
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
            int squareSize = 15;
            int startX = 0;
            int startY = 0;

            public MazePanel() {

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        handleMouseClick(e.getX() - startX, e.getY()- startY);
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
                squareSize = Math.max(1, squareSize); // Minimum size of 1

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

            currentResult = result;
            if (!currentResult.solutionSteps().isEmpty()) {
                // Switch to step view
                CardLayout cl = (CardLayout) mazePanelContainer.getLayout();
                cl.show(mazePanelContainer, "STEPS");

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

            String message = result.solved()
                    ? String.format("Maze solved! Expanded cells: %d (Sight radius: %d)", result.expandedCells(),
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
            if (currentResult == null) {
                JOptionPane.showMessageDialog(this, "No result to save!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String currentDir = System.getProperty("user.dir") + "/Results";
            File resultsDir = new File(currentDir);
            if (!resultsDir.exists()) {
                resultsDir.mkdirs();
            }
            JFileChooser fileChooser = new JFileChooser(currentDir);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Result files", "result"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".result")) {
                    file = new File(file.getAbsolutePath() + ".result");
                }

                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos.writeObject(currentResult);
                    JOptionPane.showMessageDialog(this, "Result saved successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error saving result: " + e.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void viewSavedResults() {
            String currentDir = System.getProperty("user.dir") + "/Results";
            File resultsDir = new File(currentDir);
            if (!resultsDir.exists()) {
                resultsDir.mkdirs();
            }
            JFileChooser fileChooser = new JFileChooser(currentDir);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Result files", "result"));
            fileChooser.setMultiSelectionEnabled(true);

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                List<SolveResult> loadedResults = new ArrayList<>();
                List<String> filenames = new ArrayList<>();

                for (File file : files) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        SolveResult result = (SolveResult) ois.readObject();
                        String filename = file.getName();
                        loadedResults.add(result);
                        filenames.add(filename);
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
            currentResult = null;
            stepMazeView.setResults(new ArrayList<>());
        }

        private void updateStepView() {
            if (currentResult != null && !currentResult.solutionSteps().isEmpty()) {
                stepMazeView.setResults(List.of(currentResult));
            }
        }
    }

    private static class ResultViewer extends JPanel {
        private List<SolveResult> results = new ArrayList<>();
        private List<ResultPanel> resultPanels = new ArrayList<>();
        private JSlider masterSlider;
        private JButton prevButton;
        private JButton nextButton;
        private JLabel stepLabel;
        private int currentStep = 0;
        private int maxSteps = 0;

        public ResultViewer() {
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
                currentStep = masterSlider.getValue();
                updateAllViews();
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
                    .filter(r -> r.solutionSteps() != null)
                    .mapToInt(r -> r.solutionSteps().size())
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
            private static final int MINI_SQUARE_SIZE = 8;

            public ResultPanel(SolveResult result) {
                this.result = result;
                this.currentMaze = result.originalMaze() != null ? deepCopy(result.originalMaze()) : new int[10][10];

                setLayout(new BorderLayout());
                setBorder(BorderFactory.createTitledBorder(result.getDisplayName()));

                // Combine both in the border title
                String combinedTitle = String.format("%s (Expanded: %d, Time: %dms)",
                        result.getDisplayName(),
                        result.expandedCells(),
                        result.solutionTimeMs());

                setBorder(BorderFactory.createTitledBorder(combinedTitle));

                setPreferredSize(new Dimension(
                        currentMaze[0].length * MINI_SQUARE_SIZE + 20,
                        currentMaze.length * MINI_SQUARE_SIZE + 60));
            }

            public void updateToStep(int step) {
                if (result.solutionSteps() != null && !result.solutionSteps().isEmpty()) {
                    int actualStep = Math.min(step, result.solutionSteps().size() - 1);
                    currentMaze = deepCopy(result.solutionSteps().get(actualStep));
                }
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Calculate square size to fit
                int squareSize = Math.min(
                        (getWidth() - 40) / currentMaze[0].length,
                        (getHeight() - 40) / currentMaze.length);
                squareSize = Math.max(1, squareSize); // Minimum size of 1

                // Center the maze
                int startX = (getWidth() - currentMaze[0].length * squareSize) / 2;
                int startY = (getHeight() - currentMaze.length * squareSize) / 2;

                for (int row = 0; row < currentMaze.length; row++) {
                    for (int col = 0; col < currentMaze[0].length; col++) {
                        Color color = switch (currentMaze[row][col]) {
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