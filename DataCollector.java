
// DataCollector.java
import javax.swing.*;

import src.DataCollectorUI;

/**
 * The main entry point for the Maze Solver Data Collector application.
 * Creates and displays the user interface for the maze analysis tool.
 */
public class DataCollector {
    /**
     * Main method that launches the application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DataCollectorUI ui = new DataCollectorUI();
            ui.setVisible(true);
        });
    }
}