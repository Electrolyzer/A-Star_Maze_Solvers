// src/UnifiedMazeSolverUI.java
package src;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

import static src.Constants.*;

/**
 * Unified Maze Solver UI that combines interactive solving and performance analysis
 * with shared configuration management.
 */
public class UnifiedMazeSolverUI extends JFrame implements GenericMazeAnalyzer.AnalysisUI {
    // Shared configuration management
    private java.util.List<AlgorithmConfig> algorithmConfigs;
    private DefaultListModel<AlgorithmConfig> configListModel;
    private JList<AlgorithmConfig> configList;
    private AlgorithmConfig currentSolverConfig;
    private Map<AlgorithmConfig, JCheckBox> configCheckboxes;
    
    // Inline editor components
    private JTextField configNameField;
    private JComboBox<String> configAlgorithmCombo;
    private JComboBox<Character> configTiebreakerCombo;
    private JSpinner configSightRadiusSpinner;
    private JButton addEditButton;
    
    // Main tabbed interface
    private JTabbedPane mainTabbedPane;
    
    // Interactive Solver components
    private InteractiveMazePanel interactiveMazePanel;
    private JPanel solverControlPanel;
    
    // Performance Analysis components
    private GenericMazeAnalyzer analyzer;
    private JTabbedPane resultsTabbedPane;
    private JProgressBar progressBar;
    private JButton runAnalysisButton;
    private JRadioButton usePreloadedMazesButton;
    private JRadioButton useGeneratedMazesButton;
    private JSpinner mazeSizeSpinner;
    private JSpinner mazeCountSpinner;
    private Map<String, java.util.List<SolveResult>> results;

    public UnifiedMazeSolverUI() {
        setTitle("A* Maze Solver - Unified Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize shared data structures
        algorithmConfigs = new ArrayList<>();
        configListModel = new DefaultListModel<>();
        results = new HashMap<>();
        analyzer = new GenericMazeAnalyzer(this);

        // Initialize default configurations
        initializeDefaultConfigurations();

        // Create the main interface
        createMainInterface();
        
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void initializeDefaultConfigurations() {
        algorithmConfigs.add(new AlgorithmConfig("Forward A* (g-tie)", "Forward", 'g', 1));
        algorithmConfigs.add(new AlgorithmConfig("Forward A* (h-tie)", "Forward", 'h', 1));
        algorithmConfigs.add(new AlgorithmConfig("Backward A* (g-tie)", "Backward", 'g', 1));
        algorithmConfigs.add(new AlgorithmConfig("Adaptive A* (g-tie)", "Adaptive", 'g', 1));

        for (AlgorithmConfig config : algorithmConfigs) {
            configListModel.addElement(config);
        }
        
        // Set default solver configuration
        currentSolverConfig = algorithmConfigs.get(0);
    }

    private void createMainInterface() {
        // Create main tabbed pane
        mainTabbedPane = new JTabbedPane();
        
        // Create Interactive Solver tab
        JPanel solverTab = createInteractiveSolverTab();
        mainTabbedPane.addTab("Interactive Solver", solverTab);
        
        // Create Performance Analysis tab
        JPanel analysisTab = createPerformanceAnalysisTab();
        mainTabbedPane.addTab("Performance Analysis", analysisTab);
        
        add(mainTabbedPane, BorderLayout.CENTER);
        
        // Create shared configuration panel
        JPanel configPanel = createSharedConfigurationPanel();
        add(configPanel, BorderLayout.WEST);
    }

    private JPanel createSharedConfigurationPanel() {
        JPanel configPanel = new JPanel(new BorderLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("Algorithm Configurations"));
        configPanel.setPreferredSize(new Dimension(350, 0));

        // Inline configuration editor
        JPanel editorPanel = createInlineConfigEditor();
        configPanel.add(editorPanel, BorderLayout.NORTH);

        // Configuration list that adapts based on current tab
        configList = new JList<>(configListModel);
        configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configList.setCellRenderer(new AdaptiveConfigRenderer());
        configList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                AlgorithmConfig selected = configList.getSelectedValue();
                if (selected != null) {
                    currentSolverConfig = selected;
                    updateSolverConfigDisplay();
                }
            }
        });
        
        // Add double-click listener to load configuration into editor (works in both modes)
        configList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    AlgorithmConfig selected = configList.getSelectedValue();
                    if (selected != null) {
                        loadConfigIntoEditor(selected);
                    }
                }
            }
        });
        
        JScrollPane configScrollPane = new JScrollPane(configList);
        configScrollPane.setPreferredSize(new Dimension(330, 150));
        configPanel.add(configScrollPane, BorderLayout.CENTER);
        
        // Add tab change listener to update the renderer
        mainTabbedPane.addChangeListener(e -> {
            configList.repaint(); // Refresh to show/hide checkboxes
        });

        // Configuration management buttons
        JPanel configButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));

        JButton removeConfigButton = new JButton("Remove Selected");
        removeConfigButton.addActionListener(e -> removeSelectedConfig());

        JButton saveConfigsButton = new JButton("Save All to File");
        saveConfigsButton.addActionListener(e -> saveConfigurations());

        configButtonPanel.add(removeConfigButton);
        configButtonPanel.add(saveConfigsButton);

        configPanel.add(configButtonPanel, BorderLayout.SOUTH);

        return configPanel;
    }

    private JPanel createInteractiveSolverTab() {
        JPanel solverTab = new JPanel(new BorderLayout());
        
        // Create the interactive maze panel (simplified version of MazeSolverTester)
        interactiveMazePanel = new InteractiveMazePanel();
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
        controlPanel.add(Box.createVerticalStrut(10));

        // Results and save controls
        JPanel resultsPanel = createResultsPanel();
        controlPanel.add(resultsPanel);

        return controlPanel;
    }

    private JPanel createMazeEditingPanel() {
        JPanel editPanel = new JPanel();
        editPanel.setBorder(BorderFactory.createTitledBorder("Maze Editing"));
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));

        // Edit mode selection
        JPanel modePanel = new JPanel(new FlowLayout());
        modePanel.add(new JLabel("Mode: "));
        JComboBox<String> modeCombo = new JComboBox<>(new String[]{"Set Start", "Set End", "Add Wall", "Remove Wall"});
        modeCombo.addActionListener(e -> {
            String selected = (String) modeCombo.getSelectedItem();
            InteractiveMazePanel.EditMode mode = switch (selected) {
                case "Set Start" -> InteractiveMazePanel.EditMode.SET_START;
                case "Set End" -> InteractiveMazePanel.EditMode.SET_END;
                case "Add Wall" -> InteractiveMazePanel.EditMode.ADD_WALL;
                case "Remove Wall" -> InteractiveMazePanel.EditMode.REMOVE_WALL;
                default -> InteractiveMazePanel.EditMode.SET_START;
            };
            interactiveMazePanel.setEditMode(mode);
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
        solverPanel.setLayout(new BoxLayout(solverPanel, BoxLayout.Y_AXIS));

        JButton solveButton = new JButton("Solve Maze");
        solveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        solveButton.addActionListener(e -> {
            if (interactiveMazePanel.isInSolveMode()) {
                interactiveMazePanel.returnToEditMode();
            } else {
                interactiveMazePanel.solveMaze();
            }
        });
        solverPanel.add(solveButton);

        return solverPanel;
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel();
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        JButton saveResultButton = new JButton("Save Result");
        JButton loadResultsButton = new JButton("Load Results");
        
        saveResultButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadResultsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        resultsPanel.add(saveResultButton);
        resultsPanel.add(Box.createVerticalStrut(5));
        resultsPanel.add(loadResultsButton);

        return resultsPanel;
    }

    private JPanel createPerformanceAnalysisTab() {
        JPanel analysisTab = new JPanel(new BorderLayout());
        
        // Control panel for analysis
        JPanel controlPanel = createAnalysisControlPanel();
        analysisTab.add(controlPanel, BorderLayout.NORTH);
        
        // Results tabbed pane
        resultsTabbedPane = new JTabbedPane();
        analysisTab.add(resultsTabbedPane, BorderLayout.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(500, 30));
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        analysisTab.add(progressBar, BorderLayout.SOUTH);
        
        return analysisTab;
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

    // Inline configuration editor methods
    private JPanel createInlineConfigEditor() {
        JPanel editorPanel = new JPanel();
        editorPanel.setBorder(BorderFactory.createTitledBorder("Configuration Editor"));
        editorPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);

        // Name field
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        editorPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        configNameField = new JTextField(15);
        editorPanel.add(configNameField, gbc);

        // Algorithm combo
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        editorPanel.add(new JLabel("Algorithm:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        configAlgorithmCombo = new JComboBox<>(new String[]{"Forward", "Backward", "Adaptive"});
        editorPanel.add(configAlgorithmCombo, gbc);

        // Tiebreaker combo
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        editorPanel.add(new JLabel("Tiebreaker:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        configTiebreakerCombo = new JComboBox<>(new Character[]{'g', 'h'});
        editorPanel.add(configTiebreakerCombo, gbc);

        // Sight radius spinner
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        editorPanel.add(new JLabel("Sight Radius:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        configSightRadiusSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        editorPanel.add(configSightRadiusSpinner, gbc);

        // Add/Edit button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        addEditButton = new JButton("Add Configuration");
        addEditButton.addActionListener(e -> addConfigurationFromEditor());
        
        // Add listener to name field to update button text
        configNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateButtonText(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateButtonText(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateButtonText(); }
        });
        
        editorPanel.add(addEditButton, gbc);

        return editorPanel;
    }

    private void loadConfigIntoEditor(AlgorithmConfig config) {
        configNameField.setText(config.getName());
        configAlgorithmCombo.setSelectedItem(config.getAlgorithmType());
        configTiebreakerCombo.setSelectedItem(config.getTiebreaker());
        configSightRadiusSpinner.setValue(config.getSightRadius());
    }

    private void updateButtonText() {
        String name = configNameField.getText().trim();
        boolean exists = algorithmConfigs.stream().anyMatch(config -> config.getName().equals(name));
        addEditButton.setText(exists ? "Edit Configuration" : "Add Configuration");
    }

    private void addConfigurationFromEditor() {
        String name = configNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a configuration name.", "Missing Name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String algorithmType = (String) configAlgorithmCombo.getSelectedItem();
        char tiebreaker = (Character) configTiebreakerCombo.getSelectedItem();
        int sightRadius = (Integer) configSightRadiusSpinner.getValue();

        // Check if configuration with this name already exists
        AlgorithmConfig existingConfig = algorithmConfigs.stream()
                .filter(config -> config.getName().equals(name))
                .findFirst()
                .orElse(null);

        if (existingConfig != null) {
            // Update existing configuration
            existingConfig.setAlgorithmType(algorithmType);
            existingConfig.setTiebreaker(tiebreaker);
            existingConfig.setSightRadius(sightRadius);
            
            // Update the list model
            int index = algorithmConfigs.indexOf(existingConfig);
            configListModel.setElementAt(existingConfig, index);
            
            // Update current solver config if it was the one being edited
            if (currentSolverConfig == existingConfig) {
                updateSolverConfigDisplay();
            }
            
            // Configuration updated silently
        } else {
            // Add new configuration
            AlgorithmConfig newConfig = new AlgorithmConfig(name, algorithmType, tiebreaker, sightRadius);
            algorithmConfigs.add(newConfig);
            configListModel.addElement(newConfig);
            // Configuration added silently
        }

        // Update checkboxes if they exist
        updateCheckboxesIfNeeded();

        // Clear the editor fields
        configNameField.setText("");
        configAlgorithmCombo.setSelectedIndex(0);
        configTiebreakerCombo.setSelectedIndex(0);
        configSightRadiusSpinner.setValue(1);
        updateButtonText();
    }

    // Configuration management methods
    private void showAddConfigDialog(ActionEvent e) {
        GenericAlgorithmConfigDialog dialog = new GenericAlgorithmConfigDialog(
            this, "Add Algorithm Configuration", null, 
            (name, algorithmType, tiebreaker, sightRadius) -> 
                new AlgorithmConfig(name, algorithmType, tiebreaker, sightRadius));
        dialog.setVisible(true);

        AlgorithmConfiguration result = dialog.getResult();
        if (result != null) {
            AlgorithmConfig newConfig = (AlgorithmConfig) result;
            algorithmConfigs.add(newConfig);
            configListModel.addElement(newConfig);
        }
    }

    private void showEditConfigDialog(ActionEvent e) {
        AlgorithmConfig selected = configList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a configuration to edit.");
            return;
        }

        GenericAlgorithmConfigDialog dialog = new GenericAlgorithmConfigDialog(
            this, "Edit Algorithm Configuration", selected,
            (name, algorithmType, tiebreaker, sightRadius) -> 
                new AlgorithmConfig(name, algorithmType, tiebreaker, sightRadius));
        dialog.setVisible(true);

        AlgorithmConfiguration result = dialog.getResult();
        if (result != null) {
            AlgorithmConfig editedConfig = (AlgorithmConfig) result;
            int index = configList.getSelectedIndex();
            algorithmConfigs.set(index, editedConfig);
            configListModel.setElementAt(editedConfig, index);
            if (currentSolverConfig == selected) {
                currentSolverConfig = editedConfig;
                updateSolverConfigDisplay();
            }
        }
    }

    private void removeSelectedConfig() {
        AlgorithmConfig selected = configList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a configuration to remove.");
            return;
        }

        algorithmConfigs.remove(selected);
        configListModel.removeElement(selected);
        
        if (currentSolverConfig == selected && !algorithmConfigs.isEmpty()) {
            currentSolverConfig = algorithmConfigs.get(0);
            configList.setSelectedIndex(0);
            updateSolverConfigDisplay();
        }
    }

    private void saveConfigurations() {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Configuration files", "config"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".config")) {
                file = new File(file.getAbsolutePath() + ".config");
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(algorithmConfigs);
                JOptionPane.showMessageDialog(this, "Configurations saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving configurations: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateSolverConfigDisplay() {
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
        if (currentSolverConfig != null) {
            textArea.setText(String.format(
                "Name: %s\nAlgorithm: %s\nTiebreaker: %c\nSight Radius: %d",
                currentSolverConfig.getName(),
                currentSolverConfig.getAlgorithmType(),
                currentSolverConfig.getTiebreaker(),
                currentSolverConfig.getSightRadius()
            ));
        }
    }

    // Create checkbox panel for performance analysis configuration selection
    private JPanel createConfigurationCheckboxPanel() {
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Initialize checkbox map
        configCheckboxes = new HashMap<>();

        // Add "Select All" / "Deselect All" buttons
        JPanel selectButtonPanel = new JPanel(new FlowLayout());
        JButton selectAllButton = new JButton("Select All");
        JButton deselectAllButton = new JButton("Deselect All");

        selectAllButton.addActionListener(e -> {
            for (JCheckBox checkbox : configCheckboxes.values()) {
                checkbox.setSelected(true);
            }
        });

        deselectAllButton.addActionListener(e -> {
            for (JCheckBox checkbox : configCheckboxes.values()) {
                checkbox.setSelected(false);
            }
        });

        selectButtonPanel.add(selectAllButton);
        selectButtonPanel.add(deselectAllButton);
        checkboxPanel.add(selectButtonPanel);

        // Add separator
        checkboxPanel.add(Box.createVerticalStrut(5));

        // Create checkboxes for each configuration
        updateConfigurationCheckboxes(checkboxPanel);

        return checkboxPanel;
    }

    private void updateConfigurationCheckboxes(JPanel checkboxPanel) {
        // Remove existing checkboxes (except the button panel)
        Component[] components = checkboxPanel.getComponents();
        for (int i = components.length - 1; i >= 2; i--) { // Keep first two components (button panel and strut)
            checkboxPanel.remove(i);
        }

        // Clear the checkbox map
        if (configCheckboxes != null) {
            configCheckboxes.clear();
        } else {
            configCheckboxes = new HashMap<>();
        }

        // Add checkboxes for each configuration
        for (AlgorithmConfig config : algorithmConfigs) {
            JCheckBox checkbox = new JCheckBox(config.toString());
            checkbox.setSelected(true); // Default to selected
            checkbox.setToolTipText(String.format("Algorithm: %s, Tiebreaker: %c, Sight Radius: %d",
                    config.getAlgorithmType(), config.getTiebreaker(), config.getSightRadius()));
            
            configCheckboxes.put(config, checkbox);
            checkboxPanel.add(checkbox);
        }

        checkboxPanel.revalidate();
        checkboxPanel.repaint();
    }

    private void updateCheckboxesIfNeeded() {
        // Find the checkbox panel and update it if it exists
        if (configCheckboxes != null) {
            // Find the checkbox panel in the UI hierarchy
            Component[] components = getContentPane().getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    if (panel.getBorder() instanceof javax.swing.border.TitledBorder) {
                        javax.swing.border.TitledBorder border = (javax.swing.border.TitledBorder) panel.getBorder();
                        if ("Algorithm Configurations".equals(border.getTitle())) {
                            // Found the config panel, now find the checkbox panel
                            updateCheckboxPanelInHierarchy(panel);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void updateCheckboxPanelInHierarchy(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JPanel) {
                    JPanel panel = (JPanel) view;
                    // Check if this is the checkbox panel by looking for checkboxes
                    Component[] children = panel.getComponents();
                    boolean hasCheckboxes = false;
                    for (Component child : children) {
                        if (child instanceof JCheckBox) {
                            hasCheckboxes = true;
                            break;
                        }
                    }
                    if (hasCheckboxes) {
                        updateConfigurationCheckboxes(panel);
                        return;
                    }
                }
            } else if (comp instanceof Container) {
                updateCheckboxPanelInHierarchy((Container) comp);
            }
        }
    }

    // Performance analysis methods with checkbox selection
    public void runAnalysis() {
        // Get selected configurations from checkboxes when in performance analysis tab
        java.util.List<AlgorithmConfig> selectedConfigs = new ArrayList<>();
        
        if (mainTabbedPane.getSelectedIndex() == 1) { // Performance Analysis tab
            // Use checkbox selection logic
            if (configCheckboxes != null) {
                for (Map.Entry<AlgorithmConfig, JCheckBox> entry : configCheckboxes.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        selectedConfigs.add(entry.getKey());
                    }
                }
            }
        } else {
            // Use list selection for other cases
            selectedConfigs = configList.getSelectedValuesList();
        }
        
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

        // Create tabs for different views (same as DataCollectorUI)
        createPerformanceComparisonTab();
        createDetailedResultsTab();
        createHeatmapTab();
        createStatisticsTab();

        resultsTabbedPane.revalidate();
        resultsTabbedPane.repaint();
    }

    // Results display methods (copied from DataCollectorUI)
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

    // Interactive maze panel with full functionality
    private class InteractiveMazePanel extends JPanel {
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
        private ResultViewer multiResultViewer;
        private boolean isInSolveMode = false;

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

        public InteractiveMazePanel() {
            setLayout(new BorderLayout());
            initializeMaze(30); // Default size
            setupMazeUI();
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

            // Create multi-result viewer panel
            multiResultViewer = new ResultViewer();
            JScrollPane multiScrollPane = new JScrollPane(multiResultViewer);

            // Container for switching between views
            mazePanelContainer = new JPanel(new CardLayout());
            mazePanelContainer.add(editScrollPane, "EDIT");
            mazePanelContainer.add(stepScrollPane, "STEPS");
            mazePanelContainer.add(multiScrollPane, "MULTI");
            
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
                JOptionPane.showMessageDialog(UnifiedMazeSolverUI.this,
                        "Please set both start and end positions before solving!",
                        "Missing Positions", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (currentSolverConfig == null) {
                JOptionPane.showMessageDialog(UnifiedMazeSolverUI.this,
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
                updateSolverButton();
            }

            String message = result.solved()
                    ? String.format("Maze solved! Expanded cells: %d (Sight radius: %d)", 
                            result.expandedCells(), currentSolverConfig.getSightRadius())
                    : "No path found to target.";

            JOptionPane.showMessageDialog(UnifiedMazeSolverUI.this, message,
                    "Solve Result", JOptionPane.INFORMATION_MESSAGE);
        }

        public void returnToEditMode() {
            CardLayout cl = (CardLayout) mazePanelContainer.getLayout();
            cl.show(mazePanelContainer, "EDIT");
            isInSolveMode = false;
            updateSolverButton();
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

        private void updateSolverButton() {
            // Find and update the solve button in the control panel
            updateSolverButtonInPanel(solverControlPanel);
        }

        private void updateSolverButtonInPanel(Container container) {
            for (Component comp : container.getComponents()) {
                if (comp instanceof JButton && ((JButton) comp).getText().contains("Solve")) {
                    JButton button = (JButton) comp;
                    if (isInSolveMode) {
                        button.setText("Edit Maze");
                    } else {
                        button.setText("Solve Maze");
                    }
                    return;
                } else if (comp instanceof Container) {
                    updateSolverButtonInPanel((Container) comp);
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

        private class MazePanel extends JPanel {
            int squareSize = 15;
            int startX = 0;
            int startY = 0;

            public MazePanel() {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
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
                JOptionPane.showMessageDialog(UnifiedMazeSolverUI.this,
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
                JOptionPane.showMessageDialog(UnifiedMazeSolverUI.this,
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

    // Algorithm configuration classes implementing the common interface
    public static class AlgorithmConfig implements AlgorithmConfiguration {
        private String name;
        private String algorithmType;
        private char tiebreaker;
        private int sightRadius;

        public AlgorithmConfig(String name, String algorithmType, char tiebreaker, int sightRadius) {
            this.name = name;
            this.algorithmType = algorithmType;
            this.tiebreaker = tiebreaker;
            this.sightRadius = sightRadius;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAlgorithmType() { return algorithmType; }
        public void setAlgorithmType(String algorithmType) { this.algorithmType = algorithmType; }
        public char getTiebreaker() { return tiebreaker; }
        public void setTiebreaker(char tiebreaker) { this.tiebreaker = tiebreaker; }
        public int getSightRadius() { return sightRadius; }
        public void setSightRadius(int sightRadius) { this.sightRadius = sightRadius; }

        @Override
        public String toString() {
            String algorithmAbbrev = switch (algorithmType) {
                case "Forward" -> "f";
                case "Backward" -> "b";
                case "Adaptive" -> "a";
                default -> "?";
            };
            return String.format("%s (a:%s, t:%c, r:%d)", name, algorithmAbbrev, tiebreaker, sightRadius);
        }
    }

    private class AdaptiveConfigRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof AlgorithmConfig) {
                AlgorithmConfig config = (AlgorithmConfig) value;
                
                // Check if we're in performance analysis mode
                boolean isPerformanceMode = mainTabbedPane.getSelectedIndex() == 1;
                
                if (isPerformanceMode) {
                    // Create a panel with checkbox and text
                    JPanel panel = new JPanel(new BorderLayout(5, 0));
                    panel.setOpaque(true);
                    
                    // Set background color based on selection
                    if (isSelected) {
                        panel.setBackground(list.getSelectionBackground());
                    } else {
                        panel.setBackground(list.getBackground());
                    }
                    
                    // Create or get existing checkbox
                    JCheckBox checkbox;
                    if (configCheckboxes != null && configCheckboxes.containsKey(config)) {
                        checkbox = configCheckboxes.get(config);
                    } else {
                        checkbox = new JCheckBox();
                        checkbox.setSelected(true); // Default to selected
                        if (configCheckboxes != null) {
                            configCheckboxes.put(config, checkbox);
                        }
                    }
                    
                    // Make checkbox clickable by adding mouse listener to the panel
                    panel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            // Check if click was on the checkbox area (right side)
                            int checkboxWidth = 20;
                            if (e.getX() > panel.getWidth() - checkboxWidth - 10) {
                                checkbox.setSelected(!checkbox.isSelected());
                                panel.repaint();
                            }
                        }
                    });
                    
                    checkbox.setOpaque(false);
                    checkbox.setFocusable(false); // Prevent focus issues
                    
                    // Create label for text with consistent width
                    JLabel textLabel = new JLabel(config.toString());
                    textLabel.setOpaque(false);
                    textLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // Consistent padding
                    if (isSelected) {
                        textLabel.setForeground(list.getSelectionForeground());
                    } else {
                        textLabel.setForeground(list.getForeground());
                    }
                    
                    // Add components with consistent spacing
                    panel.add(textLabel, BorderLayout.CENTER);
                    
                    // Wrap checkbox in a panel to ensure consistent positioning
                    JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                    checkboxPanel.setOpaque(false);
                    checkboxPanel.add(checkbox);
                    panel.add(checkboxPanel, BorderLayout.EAST);
                    
                    panel.setToolTipText(String.format("Algorithm: %s, Tiebreaker: %c, Sight Radius: %d",
                            config.getAlgorithmType(), config.getTiebreaker(), config.getSightRadius()));
                    
                    return panel;
                } else {
                    // Regular list mode for interactive solver
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setText(config.toString());
                    setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // Match padding
                    setToolTipText(String.format("Algorithm: %s, Tiebreaker: %c, Sight Radius: %d",
                            config.getAlgorithmType(), config.getTiebreaker(), config.getSightRadius()));
                    return this;
                }
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static class AlgorithmConfigRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof AlgorithmConfig) {
                AlgorithmConfig config = (AlgorithmConfig) value;
                setText(config.toString());
                setToolTipText(String.format("Algorithm: %s, Tiebreaker: %c, Sight Radius: %d",
                        config.getAlgorithmType(), config.getTiebreaker(), config.getSightRadius()));
            }

            return this;
        }
    }

    // Result viewer for step-by-step maze solving visualization
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
