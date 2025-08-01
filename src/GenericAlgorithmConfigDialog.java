// src/GenericAlgorithmConfigDialog.java
package src;

import javax.swing.*;
import java.awt.*;

/**
 * Generic dialog for creating and editing algorithm configurations.
 * Works with any AlgorithmConfiguration implementation.
 */
public class GenericAlgorithmConfigDialog extends JDialog {
    private JTextField nameField;
    private JComboBox<String> algorithmTypeCombo;
    private JComboBox<Character> tiebreakerCombo;
    private JSpinner sightRadiusSpinner;
    private AlgorithmConfiguration result;
    private boolean confirmed = false;
    private ConfigFactory factory;

    /**
     * Factory interface for creating configuration instances.
     */
    public interface ConfigFactory {
        AlgorithmConfiguration createConfig(String name, String algorithmType, char tiebreaker, int sightRadius);
    }

    public GenericAlgorithmConfigDialog(JFrame parent, String title, AlgorithmConfiguration existing, ConfigFactory factory) {
        super(parent, title, true);
        this.factory = factory;

        createComponents();
        layoutComponents();

        if (existing != null) {
            populateFields(existing);
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void createComponents() {
        nameField = new JTextField(20);

        algorithmTypeCombo = new JComboBox<>(new String[] { "Forward", "Backward", "Adaptive" });

        tiebreakerCombo = new JComboBox<>(new Character[] { 'g', 'h' });

        sightRadiusSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Algorithm:"), gbc);
        gbc.gridx = 1;
        add(algorithmTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Tiebreaker:"), gbc);
        gbc.gridx = 1;
        add(tiebreakerCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Sight Radius:"), gbc);
        gbc.gridx = 1;
        add(sightRadiusSpinner, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                result = factory.createConfig(
                        nameField.getText(),
                        (String) algorithmTypeCombo.getSelectedItem(),
                        (Character) tiebreakerCombo.getSelectedItem(),
                        (Integer) sightRadiusSpinner.getValue());
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }

    private void populateFields(AlgorithmConfiguration config) {
        nameField.setText(config.getName());
        algorithmTypeCombo.setSelectedItem(config.getAlgorithmType());
        tiebreakerCombo.setSelectedItem(config.getTiebreaker());
        sightRadiusSpinner.setValue(config.getSightRadius());
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a name for the configuration.");
            return false;
        }
        return true;
    }

    public AlgorithmConfiguration getResult() {
        return confirmed ? result : null;
    }
}
