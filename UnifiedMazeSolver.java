// UnifiedMazeSolver.java
import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

import src.UnifiedMazeSolverUI;

/**
 * The main entry point for the Unified Maze Solver application.
 * Combines interactive maze solving and performance analysis in one interface.
 */
public class UnifiedMazeSolver {
    /**
     * Main method that launches the unified application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            UnifiedMazeSolverUI ui = new UnifiedMazeSolverUI();
            ui.setVisible(true);
        });
    }
}
