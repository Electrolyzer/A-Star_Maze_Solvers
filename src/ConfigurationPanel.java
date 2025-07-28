// src/ConfigurationPanel.java
package src;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * Shared configuration management panel with inline editing and checkbox
 * support
 */
public class ConfigurationPanel extends JPanel {
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

    // Callbacks
    private Runnable configChangeCallback;
    private JTabbedPane mainTabbedPane;

    public ConfigurationPanel(JTabbedPane mainTabbedPane) {
        this.mainTabbedPane = mainTabbedPane;
        this.algorithmConfigs = new ArrayList<>();
        this.configListModel = new DefaultListModel<>();
        this.configCheckboxes = new HashMap<>();
        
        loadConfigurations();
        setupUI();
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

    private void loadConfigurations() {
        File configFile = new File("config/configs.dat");
        if (configFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(configFile))) {
                @SuppressWarnings("unchecked")
                java.util.List<AlgorithmConfig> loadedConfigs = (java.util.List<AlgorithmConfig>) ois.readObject();
                
                // Check if default configurations are present
                boolean hasDefaults = loadedConfigs.stream().anyMatch(config -> 
                    config.getName().equals("Forward A* (g-tie)") ||
                    config.getName().equals("Forward A* (h-tie)") ||
                    config.getName().equals("Backward A* (g-tie)") ||
                    config.getName().equals("Adaptive A* (g-tSie)")
                );
                
                if (!hasDefaults) {
                    // Add default configurations if not present
                    loadedConfigs.add(0, new AlgorithmConfig("Forward A* (g-tie)", "Forward", 'g', 1));
                    loadedConfigs.add(1, new AlgorithmConfig("Forward A* (h-tie)", "Forward", 'h', 1));
                    loadedConfigs.add(2, new AlgorithmConfig("Backward A* (g-tie)", "Backward", 'g', 1));
                    loadedConfigs.add(3, new AlgorithmConfig("Adaptive A* (g-tie)", "Adaptive", 'g', 1));
                }
                
                algorithmConfigs.addAll(loadedConfigs);
                for (AlgorithmConfig config : algorithmConfigs) {
                    configListModel.addElement(config);
                }
                
                // Set default solver configuration
                currentSolverConfig = algorithmConfigs.get(0);
                
            } catch (IOException | ClassNotFoundException e) {
                // If loading fails, initialize with defaults
                initializeDefaultConfigurations();
            }
        } else {
            // No saved configurations, initialize with defaults
            initializeDefaultConfigurations();
        }
    }

    private void autoSaveConfigurations() {
        // Ensure config directory exists
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("config/configs.dat"))) {
            oos.writeObject(algorithmConfigs);
        } catch (IOException e) {
            // Silent failure for auto-save
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Algorithm Configurations"));
        setPreferredSize(new Dimension(350, 0));

        // Inline configuration editor
        JPanel editorPanel = createInlineConfigEditor();
        add(editorPanel, BorderLayout.NORTH);

        // Configuration list that adapts based on current tab
        configList = new JList<>(configListModel);
        configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configList.setCellRenderer(new AdaptiveConfigRenderer());
        configList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                AlgorithmConfig selected = configList.getSelectedValue();
                if (selected != null) {
                    currentSolverConfig = selected;
                    if (configChangeCallback != null) {
                        configChangeCallback.run();
                    }
                }
            }
        });

        // Add mouse listener to handle both checkbox clicks and double-click editing
        configList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = configList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    AlgorithmConfig config = configListModel.getElementAt(index);

                    if (e.getClickCount() == 1 && mainTabbedPane.getSelectedIndex() == 2) {
                        if (configCheckboxes != null) {
                            JCheckBox checkbox = configCheckboxes.get(config);
                            if (checkbox != null) {
                                checkbox.setSelected(!checkbox.isSelected());
                                configList.repaint();
                            }
                        }
                    } else if (e.getClickCount() == 2) {
                        // Double click - load into editor
                        loadConfigIntoEditor(config);
                    }
                }
            }
        });

        JScrollPane configScrollPane = new JScrollPane(configList);
        configScrollPane.setPreferredSize(new Dimension(330, 150));
        add(configScrollPane, BorderLayout.CENTER);

        // Add tab change listener to update the renderer
        mainTabbedPane.addChangeListener(e -> {
            configList.repaint(); // Refresh to show/hide checkboxes
        });

        // Configuration management buttons
        JPanel configButtonPanel = createButtonPanel();
        add(configButtonPanel, BorderLayout.SOUTH);
    }

    private JPanel createButtonPanel() {
        JPanel configButtonPanel = new JPanel(new BorderLayout());

        // Regular management buttons (always visible)
        JPanel managementButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton removeConfigButton = new JButton("Remove Selected");
        removeConfigButton.addActionListener(e -> removeSelectedConfig());
        JButton editSelectedButton = new JButton("Edit Selected");
        editSelectedButton.addActionListener(e -> editSelectedConfig());
        managementButtonPanel.add(removeConfigButton);
        managementButtonPanel.add(editSelectedButton);

        // Select/Deselect buttons (only visible in Performance Analysis mode)
        JPanel selectionButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(e -> {
            if (configCheckboxes != null) {
                for (JCheckBox checkbox : configCheckboxes.values()) {
                    checkbox.setSelected(true);
                }
                configList.repaint();
            }
        });
        JButton deselectAllButton = new JButton("Deselect All");
        deselectAllButton.addActionListener(e -> {
            if (configCheckboxes != null) {
                for (JCheckBox checkbox : configCheckboxes.values()) {
                    checkbox.setSelected(false);
                }
                configList.repaint();
            }
        });
        selectionButtonPanel.add(selectAllButton);
        selectionButtonPanel.add(deselectAllButton);

        // Initially hide selection buttons
        selectionButtonPanel.setVisible(false);

        configButtonPanel.add(managementButtonPanel, BorderLayout.NORTH);
        configButtonPanel.add(selectionButtonPanel, BorderLayout.SOUTH);

        // Update tab change listener to show/hide selection buttons
        mainTabbedPane.addChangeListener(e -> {
            boolean isPerformanceMode = mainTabbedPane.getSelectedIndex() == 2;
            selectionButtonPanel.setVisible(isPerformanceMode);
            configList.repaint(); // Refresh to show/hide checkboxes
        });

        return configButtonPanel;
    }

    private JPanel createInlineConfigEditor() {
        JPanel editorPanel = new JPanel();
        editorPanel.setBorder(BorderFactory.createTitledBorder("Configuration Editor"));
        editorPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        editorPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        configNameField = new JTextField(15);
        editorPanel.add(configNameField, gbc);

        // Algorithm combo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        editorPanel.add(new JLabel("Algorithm:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        configAlgorithmCombo = new JComboBox<>(new String[] { "Forward", "Backward", "Adaptive" });
        editorPanel.add(configAlgorithmCombo, gbc);

        // Tiebreaker combo
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        editorPanel.add(new JLabel("Tiebreaker:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        configTiebreakerCombo = new JComboBox<>(new Character[] { 'g', 'h' });
        editorPanel.add(configTiebreakerCombo, gbc);

        // Sight radius spinner
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        editorPanel.add(new JLabel("Sight Radius:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        configSightRadiusSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        editorPanel.add(configSightRadiusSpinner, gbc);

        // Add/Edit button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addEditButton = new JButton("Add Configuration");
        addEditButton.addActionListener(e -> addConfigurationFromEditor());

        // Add listener to name field to update button text
        configNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateButtonText();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateButtonText();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateButtonText();
            }
        });

        editorPanel.add(addEditButton, gbc);

        return editorPanel;
    }

    // Public interface methods
    public void setConfigChangeCallback(Runnable callback) {
        this.configChangeCallback = callback;
    }

    public AlgorithmConfig getCurrentSolverConfig() {
        return currentSolverConfig;
    }

    public java.util.List<AlgorithmConfig> getSelectedConfigs() {
        java.util.List<AlgorithmConfig> selectedConfigs = new ArrayList<>();

        if (mainTabbedPane.getSelectedIndex() == 2) { // Performance Analysis tab
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

        return selectedConfigs;
    }

    // Private helper methods
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
            JOptionPane.showMessageDialog(this, "Please enter a configuration name.", "Missing Name",
                    JOptionPane.WARNING_MESSAGE);
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
            if (currentSolverConfig == existingConfig && configChangeCallback != null) {
                configChangeCallback.run();
            }
        } else {
            // Add new configuration
            AlgorithmConfig newConfig = new AlgorithmConfig(name, algorithmType, tiebreaker, sightRadius);
            algorithmConfigs.add(newConfig);
            configListModel.addElement(newConfig);
        }

        updateButtonText();
        
        // Auto-save configurations
        autoSaveConfigurations();
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
            if (configChangeCallback != null) {
                configChangeCallback.run();
            }
        }
        
        // Auto-save configurations
        autoSaveConfigurations();
    }

    private void editSelectedConfig() {
        AlgorithmConfig selected = configList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a configuration to edit.");
            return;
        }
        
        loadConfigIntoEditor(selected);
    }

    // Adaptive renderer for showing checkboxes in performance analysis mode
    private class AdaptiveConfigRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            if (value instanceof AlgorithmConfig) {
                AlgorithmConfig config = (AlgorithmConfig) value;

                // Check if we're in performance analysis mode
                boolean isPerformanceMode = mainTabbedPane.getSelectedIndex() == 2;

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
}
