// src/SavedResultsPanel.java
package src;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * Panel for loading and viewing previously saved maze solving results
 */
public class SavedResultsPanel extends JPanel {
    private ResultViewer savedResultsViewer;

    public SavedResultsPanel() {
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Control panel for loading results
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton loadResultsButton = new JButton("Load Saved Results");
        loadResultsButton.addActionListener(e -> loadSavedResults());
        controlPanel.add(loadResultsButton);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Results viewer for loaded results
        savedResultsViewer = new ResultViewer();
        add(savedResultsViewer, BorderLayout.CENTER);
    }

    private void loadSavedResults() {
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
                savedResultsViewer.setResults(loadedResults);
            }
        }
    }
}
