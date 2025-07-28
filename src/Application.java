// Application.java
package src;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * The main entry point for the A* Maze Solver application.
 * Combines interactive maze solving and performance analysis in one interface.
 */
public class Application {
    /**
     * Main method that launches the application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            MazeSolverUI ui = new MazeSolverUI();
            ui.setVisible(true);
        });
    }
}
