// src/ResultViewer.java
package src;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import static src.Constants.*;

/**
 * Result viewer for step-by-step maze solving visualization
 */
public class ResultViewer extends JPanel {
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
            // Create title with algorithm abbreviation format
            String algorithmAbbrev = switch (result.algorithmName()) {
                case "Forward A*" -> "f";
                case "Backward A*" -> "b";
                case "Adaptive A*" -> "a";
                default -> "?";
            };
            
            String combinedTitle = String.format("%s (a:%s, t:%c, r:%d) - Expanded: %d, Time: %dms",
                    result.getDisplayName(),
                    algorithmAbbrev,
                    result.tiebreaker(),
                    result.sightRadius(),
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
