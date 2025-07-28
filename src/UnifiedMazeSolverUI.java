// src/UnifiedMazeSolverUI.java
package src;

import java.awt.*;
import java.io.*;
import javax.swing.*;

/**
 * Unified Maze Solver UI that combines interactive solving and performance
 * analysis
 * with shared configuration management. Refactored into smaller, maintainable
 * components.
 */
public class UnifiedMazeSolverUI extends JFrame {
    // Main components
    private JTabbedPane mainTabbedPane;
    private ConfigurationPanel configPanel;
    private InteractiveMazePanel interactiveMazePanel;
    private PerformanceAnalysisPanel performanceAnalysisPanel;
    private SavedResultsPanel savedResultsPanel;
    private JPanel solverControlPanel;

    public UnifiedMazeSolverUI() {
        setTitle("A* Maze Solver - Unified Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the main interface
        createMainInterface();

        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void createMainInterface() {
        // Create main tabbed pane
        mainTabbedPane = new JTabbedPane();

        // Create shared configuration panel first (needed by other components)
        configPanel = new ConfigurationPanel(mainTabbedPane);
        configPanel.setConfigChangeCallback(this::updateSolverConfigDisplay);

        // Create Interactive Solver tab
        JPanel solverTab = createInteractiveSolverTab();
        mainTabbedPane.addTab("Interactive Solver", solverTab);

        // Create Saved Results tab
        savedResultsPanel = new SavedResultsPanel();
        mainTabbedPane.addTab("Saved Results", savedResultsPanel);

        // Create Performance Analysis tab
        performanceAnalysisPanel = new PerformanceAnalysisPanel(configPanel);
        mainTabbedPane.addTab("Performance Analysis", performanceAnalysisPanel);

        add(mainTabbedPane, BorderLayout.CENTER);
        add(configPanel, BorderLayout.WEST);
    }

    private JPanel createInteractiveSolverTab() {
        JPanel solverTab = new JPanel(new BorderLayout());

        // Create the interactive maze panel
        interactiveMazePanel = new InteractiveMazePanel();
        interactiveMazePanel.setCurrentSolverConfig(configPanel.getCurrentSolverConfig());
        interactiveMazePanel.setButtonUpdateCallback(this::updateSolverButtons);

        JScrollPane mazeScrollPane = new JScrollPane(interactiveMazePanel);
        solverTab.add(mazeScrollPane, BorderLayout.CENTER);

        // Create solver control panel
        solverControlPanel = createSolverControlPanel();
        solverTab.add(solverControlPanel, BorderLayout.EAST);

        return solverTab;
    }

    private JPanel createSolverControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(300, 0));

        // Current configuration display
        JPanel currentConfigPanel = new JPanel(new BorderLayout());
        currentConfigPanel.setBorder(BorderFactory.createTitledBorder("Current Configuration"));

        JTextArea configDisplay = new JTextArea(3, 20);
        configDisplay.setEditable(false);
        configDisplay.setBackground(getBackground());
        updateConfigDisplay(configDisplay);
        currentConfigPanel.add(new JScrollPane(configDisplay), BorderLayout.CENTER);

        controlPanel.add(currentConfigPanel);
        controlPanel.add(Box.createVerticalStrut(10));

        // Maze editing controls
        JPanel editPanel = createMazeEditingPanel();
        controlPanel.add(editPanel);
        controlPanel.add(Box.createVerticalStrut(10));

        // Solver controls
        JPanel solverPanel = createSolverPanel();
        controlPanel.add(solverPanel);

        return controlPanel;
    }

    private JPanel createMazeEditingPanel() {
        JPanel editPanel = new JPanel();
        editPanel.setBorder(BorderFactory.createTitledBorder("Maze Editing"));
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));

        // Edit mode selection
        JPanel modePanel = new JPanel(new FlowLayout());
        modePanel.add(new JLabel("Mode: "));
        JComboBox<InteractiveMazePanel.EditMode> modeCombo = new JComboBox<>(InteractiveMazePanel.EditMode.values());
        modeCombo.addActionListener(e -> {
            InteractiveMazePanel.EditMode selected = (InteractiveMazePanel.EditMode) modeCombo.getSelectedItem();
            interactiveMazePanel.setEditMode(selected);
        });
        modePanel.add(modeCombo);
        editPanel.add(modePanel);

        // Maze control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton resetButton = new JButton("Reset Maze");
        resetButton.addActionListener(e -> interactiveMazePanel.resetMaze());
        JButton clearButton = new JButton("Clear Start/End");
        clearButton.addActionListener(e -> interactiveMazePanel.clearStartEnd());
        buttonPanel.add(resetButton);
        buttonPanel.add(clearButton);
        editPanel.add(buttonPanel);

        return editPanel;
    }

    private JPanel createSolverPanel() {
        JPanel solverPanel = new JPanel();
        solverPanel.setBorder(BorderFactory.createTitledBorder("Solver"));
        solverPanel.setLayout(new BoxLayout(solverPanel, BoxLayout.X_AXIS));

        // Create button panel with side-by-side layout
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));

        JButton solveButton = new JButton("Solve Maze");
        solveButton.addActionListener(e -> {
            if (interactiveMazePanel.isInSolveMode()) {
                interactiveMazePanel.returnToEditMode();
            } else {
                interactiveMazePanel.solveMaze();
            }
        });

        JButton saveResultButton = new JButton("Save Result");
        saveResultButton.addActionListener(e -> saveCurrentResult());

        buttonPanel.add(solveButton);
        buttonPanel.add(saveResultButton);

        solverPanel.add(buttonPanel);

        return solverPanel;
    }

    // Configuration update methods
    private void updateSolverConfigDisplay() {
        interactiveMazePanel.setCurrentSolverConfig(configPanel.getCurrentSolverConfig());

        // Update the current configuration display in the solver tab
        Component[] components = solverControlPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getBorder() instanceof javax.swing.border.TitledBorder) {
                    javax.swing.border.TitledBorder border = (javax.swing.border.TitledBorder) panel.getBorder();
                    if ("Current Configuration".equals(border.getTitle())) {
                        Component scrollPane = panel.getComponent(0);
                        if (scrollPane instanceof JScrollPane) {
                            JTextArea textArea = (JTextArea) ((JScrollPane) scrollPane).getViewport().getView();
                            updateConfigDisplay(textArea);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void updateConfigDisplay(JTextArea textArea) {
        AlgorithmConfig currentConfig = configPanel.getCurrentSolverConfig();
        if (currentConfig != null) {
            textArea.setText(String.format(
                    "Name: %s\nAlgorithm: %s\nTiebreaker: %c\nSight Radius: %d",
                    currentConfig.getName(),
                    currentConfig.getAlgorithmType(),
                    currentConfig.getTiebreaker(),
                    currentConfig.getSightRadius()));
        }
    }

    private void updateSolverButtons() {
        updateSolverButtonInPanel(solverControlPanel);
    }

    private void updateSolverButtonInPanel(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getBorder() instanceof javax.swing.border.TitledBorder) {
                    javax.swing.border.TitledBorder border = (javax.swing.border.TitledBorder) panel.getBorder();
                    if ("Solver".equals(border.getTitle())) {
                        // Found solver panel, update the solve button
                        for (Component solverComp : panel.getComponents()) {
                            if (solverComp instanceof JPanel) {
                                JPanel buttonPanel = (JPanel) solverComp;
                                for (Component buttonComp : buttonPanel.getComponents()) {
                                    if (buttonComp instanceof JButton) {
                                        JButton button = (JButton) buttonComp;
                                        if (button.getText().contains("Solve") || button.getText().contains("Edit")) {
                                            if (interactiveMazePanel.isInSolveMode()) {
                                                button.setText("Edit Maze");
                                            } else {
                                                button.setText("Solve Maze");
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        return;
                    } else if ("Maze Editing".equals(border.getTitle())) {
                        // Found maze editing panel, disable/enable buttons based on mode
                        for (Component editComp : panel.getComponents()) {
                            if (editComp instanceof JPanel) {
                                JPanel buttonPanel = (JPanel) editComp;
                                for (Component buttonComp : buttonPanel.getComponents()) {
                                    if (buttonComp instanceof JButton) {
                                        JButton button = (JButton) buttonComp;
                                        if (button.getText().contains("Reset") || button.getText().contains("Clear")) {
                                            button.setEnabled(!interactiveMazePanel.isInSolveMode());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (comp instanceof Container) {
                updateSolverButtonInPanel((Container) comp);
            }
        }
    }

    // Save current result from interactive solver
    private void saveCurrentResult() {
        SolveResult currentResult = interactiveMazePanel.getCurrentResult();
        if (currentResult == null) {
            JOptionPane.showMessageDialog(this, "No result to save! Please solve a maze first.", "No Result",
                    JOptionPane.WARNING_MESSAGE);
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
}
