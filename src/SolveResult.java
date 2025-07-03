// src/SolveResult.java
package src;

import java.io.Serializable;
import java.util.List;

public class SolveResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public boolean solved;
    public int expandedCells;

    // New fields for comprehensive result storage
    public String algorithmName;
    public char tiebreaker;
    public int sightRadius;
    public int mazeSize;
    public int[][] originalMaze;
    public int[] startPosition; // [row, col]
    public int[] targetPosition; // [row, col]
    public List<int[][]> solutionSteps;
    public long solutionTimeMs;

    // Original constructor for backward compatibility
    public SolveResult(boolean solved, int expandedCells) {
        this.solved = solved;
        this.expandedCells = expandedCells;
    }

    // New comprehensive constructor
    public SolveResult(boolean solved, int expandedCells, String algorithmName,
            char tiebreaker, int sightRadius, int mazeSize,
            int[][] originalMaze, int[] startPosition, int[] targetPosition,
            List<int[][]> solutionSteps, long solutionTimeMs) {
        this.solved = solved;
        this.expandedCells = expandedCells;
        this.algorithmName = algorithmName;
        this.tiebreaker = tiebreaker;
        this.sightRadius = sightRadius;
        this.mazeSize = mazeSize;
        this.originalMaze = deepCopyMaze(originalMaze);
        this.startPosition = startPosition.clone();
        this.targetPosition = targetPosition.clone();
        this.solutionSteps = solutionSteps;
        this.solutionTimeMs = solutionTimeMs;
    }

    private int[][] deepCopyMaze(int[][] maze) {
        if (maze == null)
            return null;
        int[][] copy = new int[maze.length][maze[0].length];
        for (int i = 0; i < maze.length; i++) {
            System.arraycopy(maze[i], 0, copy[i], 0, maze[i].length);
        }
        return copy;
    }

    public String getDisplayName() {
        return String.format("%s (t:%c, r:%d) - %s",
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