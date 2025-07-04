// src/SolveResult.java
package src;

import java.io.Serializable;
import java.util.List;

public record SolveResult(
        boolean solved,
        int expandedCells,
        String algorithmName,
        char tiebreaker,
        int sightRadius,
        int mazeSize,
        int[][] originalMaze,
        int[] startPosition,
        int[] targetPosition,
        List<int[][]> solutionSteps,
        long solutionTimeMs,
        String fileName) implements Serializable {

    private static final long serialVersionUID = 1L;

    // Compact constructor for validation and defensive copying
    public SolveResult {
        if (originalMaze != null) {
            originalMaze = deepCopyMaze(originalMaze);
        }
        if (startPosition != null) {
            startPosition = startPosition.clone();
        }
        if (targetPosition != null) {
            targetPosition = targetPosition.clone();
        }
    }

    // Backward compatibility constructor
    public SolveResult(boolean solved, int expandedCells) {
        this(solved, expandedCells, null, 'g', 1, 0, null, null, null, null, 0L, null);
    }

    private static int[][] deepCopyMaze(int[][] maze) {
        if (maze == null)
            return null;
        int[][] copy = new int[maze.length][maze[0].length];
        for (int i = 0; i < maze.length; i++) {
            System.arraycopy(maze[i], 0, copy[i], 0, maze[i].length);
        }
        return copy;
    }

    public String getDisplayName() {
        return String.format("%s: %s (t:%c, r:%d) - %s",
                fileName != null ? fileName : "Maze",
                algorithmName != null ? algorithmName : "Unknown",
                tiebreaker,
                sightRadius,
                solved ? "Solved" : "Unsolved");
    }

    @Override
    public String toString() {
        String s = "Maze " +
                (solved ? "was solved" : "was found to be unsolvable") +
                " with " + expandedCells + " expansions";
        if (algorithmName != null) {
            s += " using " + algorithmName;
        }
        return s;
    }
}