// src/MazeSolver.java
package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

import static src.Constants.*;

/**
 * MazeSolver implements three A* search algorithms for maze solving:
 * - Repeated Forward A*: Searches from start to goal repeatedly
 * - Repeated Backward A*: Searches from goal to start repeatedly  
 * - Adaptive A*: Forward A* with learned heuristics from previous searches
 * 
 * The solver works with partially observable mazes where the agent discovers
 * blocked cells as it explores the environment.
 */
public class MazeSolver {
    // === MAZE REPRESENTATION ===
    private int[][] unknownMaze;    // Agent's current knowledge of the maze
    private int[][] knownMaze;      // Complete maze (for simulation purposes)
    private int size;               // Maze dimensions (size x size)
    
    // === SEARCH PARAMETERS ===
    private int start;              // Start position (1D coordinate)
    private int target;             // Target position (1D coordinate)
    private char tiebreaker;        // Tiebreaker for equal f-values ('g' or 'h')
    private int sightRadius;        // Manhattan distance sight radius

    // === SEARCH STATE ===
    private int expandedCells;      // Counter for performance metrics
    private int[] search;           // Search counter for each cell
    private int[] g;                // G-values (cost from start)
    private HashMap<Integer, Integer> h; // Learned heuristics (for Adaptive A*)
    private PriorityQueue open;     // Open list for A*
    private ArrayList<Integer> closed; // Closed list for A*
    
    // === VISUALIZATION ===
    private List<int[][]> steps;    // Maze states for step-by-step visualization
    private boolean storeSteps;     // Whether to store steps for visualization
    
    // === CONSTANTS ===
    private static final int MOVEMENT_COST = 1;

    /**
     * Creates a MazeSolver with full configuration options.
     * 
     * @param unknownMaze Initial agent knowledge (typically all unknown)
     * @param knownMaze   Complete maze for simulation
     * @param start       Start position (1D coordinate)
     * @param target      Target position (1D coordinate)
     * @param tiebreaker  Tiebreaker for equal f-values ('g' or 'h')
     * @param storeSteps  Whether to store visualization steps
     * @param sightRadius Manhattan distance sight radius
     */
    public MazeSolver(int[][] unknownMaze, int[][] knownMaze, int start, int target,
            char tiebreaker, boolean storeSteps, int sightRadius) {
        this.unknownMaze = unknownMaze;
        this.knownMaze = knownMaze;
        this.size = unknownMaze.length;
        this.start = start;
        this.target = target;
        this.tiebreaker = tiebreaker;
        this.storeSteps = storeSteps;
        this.sightRadius = Math.max(1, sightRadius); // Ensure minimum radius of 1

        initializeSearchStructures();
    }

    /**
     * Creates a MazeSolver for batch testing (no visualization).
     * Assumes start at (0,0) and target at (size-1, size-1).
     * 
     * @param knownMaze   Complete maze to solve
     * @param tiebreaker  Tiebreaker for equal f-values ('g' or 'h')
     * @param sightRadius Manhattan distance sight radius
     */
    public MazeSolver(int[][] knownMaze, char tiebreaker, int sightRadius) {
        this.knownMaze = knownMaze;
        this.size = knownMaze.length;
        this.tiebreaker = tiebreaker;
        this.storeSteps = false;
        this.sightRadius = Math.max(1, sightRadius); // Ensure minimum radius of 1

        // Initialize unknown maze (all cells unknown initially)
        this.unknownMaze = new int[size][size];
        for (int[] row : unknownMaze) {
            Arrays.fill(row, UNKNOWN);
        }

        // Set default start and target positions
        this.start = Coordinates.get1DFrom2D(0, 0, size);
        this.target = Coordinates.get1DFrom2D(size - 1, size - 1, size);

        initializeSearchStructures();
    }
    
    /**
     * Initializes data structures used by all search algorithms.
     */
    private void initializeSearchStructures() {
        this.expandedCells = 0;
        this.search = new int[size * size];
        this.g = new int[size * size];
        this.steps = new ArrayList<>();
    }

    // === PUBLIC SOLVING METHODS ===
    
    /**
     * Solves the maze using Repeated Forward A*.
     * Repeatedly plans from current position to target until target is reached.
     * 
     * @return Result containing success status and number of expanded cells
     */
    public SolveResult solveForward() {
        long startTime = System.currentTimeMillis();
        initializeSolve();

        int counter = 0;
        int currentPosition = start;

        while (currentPosition != target) {
            counter++;

            // Initialize search for this iteration
            initializeSearchIteration(currentPosition, target, counter);

            // Plan path from current position to target
            State targetState = computeShortestPath(counter, target);

            if (open.isEmpty()) {
                long endTime = System.currentTimeMillis();
                return createEnhancedResult(false, expandedCells, "Forward A*", endTime - startTime);
            }

            // Extract and follow the planned path
            Deque<State> plannedPath = extractPath(targetState, true);
            unknownMaze[Coordinates.get2DFrom1D(target, size)[0]][Coordinates.get2DFrom1D(target,
                    size)[1]] = TARGET_CELL; // Mark target
            State newPosition = followPathUntilBlocked(plannedPath);

            resetPathVisualization();
            currentPosition = newPosition.getCoordinate();
        }

        long endTime = System.currentTimeMillis();
        return createEnhancedResult(true, expandedCells, "Forward A*", endTime - startTime);
    }

    /**
     * Solves the maze using Repeated Backward A*.
     * Repeatedly plans from target to current position until target is reached.
     * 
     * @return Result containing success status and number of expanded cells
     */
    public SolveResult solveBackward() {
        long startTime = System.currentTimeMillis();
        initializeSolve();

        int counter = 0;
        int currentPosition = start;

        while (currentPosition != target) {
            counter++;

            // Initialize search for this iteration (reversed: target to current)
            initializeSearchIteration(target, currentPosition, counter);

            // Plan path from target to current position
            State pathState = computeShortestPath(counter, currentPosition);

            if (open.isEmpty()) {
                long endTime = System.currentTimeMillis();
                return createEnhancedResult(false, expandedCells, "Backward A*", endTime - startTime);
            }

            // Extract and follow the planned path
            Deque<State> plannedPath = extractPath(pathState, false);
            unknownMaze[Coordinates.get2DFrom1D(target, size)[0]][Coordinates.get2DFrom1D(target,
                    size)[1]] = TARGET_CELL; // Mark target
            State newPosition = followPathUntilBlocked(plannedPath);

            resetPathVisualization();
            currentPosition = newPosition.getCoordinate();
        }

        long endTime = System.currentTimeMillis();
        return createEnhancedResult(true, expandedCells, "Backward A*", endTime - startTime);
    }

    /**
     * Solves the maze using Adaptive A*.
     * Like Forward A* but learns better heuristics from previous searches.
     * 
     * @return Result containing success status and number of expanded cells
     */
    public SolveResult solveAdaptive() {
        long startTime = System.currentTimeMillis();
        initializeSolve();
        h = new HashMap<>(); // Initialize learned heuristics

        int counter = 0;
        int currentPosition = start;

        while (currentPosition != target) {
            counter++;

            // Initialize search for this iteration
            initializeSearchIteration(currentPosition, target, counter);

            // Plan path using learned heuristics
            State targetState = computeShortestPath(counter, target);

            if (open.isEmpty()) {
                long endTime = System.currentTimeMillis();
                return createEnhancedResult(false, expandedCells, "Adaptive A*", endTime - startTime);
            }

            // Extract and follow the planned path
            Deque<State> plannedPath = extractPath(targetState, true);
            unknownMaze[Coordinates.get2DFrom1D(target, size)[0]][Coordinates.get2DFrom1D(target,
                    size)[1]] = TARGET_CELL; // Mark target
            State newPosition = followPathUntilBlocked(plannedPath);

            // Learn better heuristics from this search
            updateLearnedHeuristics();

            resetPathVisualization();
            currentPosition = newPosition.getCoordinate();
        }

        long endTime = System.currentTimeMillis();
        return createEnhancedResult(true, expandedCells, "Adaptive A*", endTime - startTime);
    }

    // === CORE SEARCH ALGORITHM ===
    
    /**
     * Initializes data structures for a new solve attempt.
     */
    private void initializeSolve() {
        steps.clear();
        if (storeSteps) {
            captureCurrentState();
        }
    }
    
    /**
     * Initializes search structures for one iteration of the repeated search.
     * 
     * @param startPos Start position for this search iteration
     * @param goalPos  Goal position for this search iteration  
     * @param counter  Search iteration counter
     */
    private void initializeSearchIteration(int startPos, int goalPos, int counter) {
        g[startPos] = 0;
        search[startPos] = counter;
        g[goalPos] = Integer.MAX_VALUE;
        search[goalPos] = counter;
        
        open = new PriorityQueue(size * size, tiebreaker);
        closed = new ArrayList<>();
        open.insert(new State(startPos, g[startPos], computeHeuristic(startPos, goalPos)));
    }

    /**
     * Computes the shortest presumed unblocked path using A*.
     * 
     * @param counter Search iteration counter
     * @param goal    Goal position for this search
     * @return State representing the goal (with parent pointers for path)
     */
    private State computeShortestPath(int counter, int goal) {
        State goalState = null;
        
        while (!open.isEmpty() && g[goal] > open.peek().getF()) {
            State currentState = open.pop();
            closed.add(currentState.getCoordinate());
            expandedCells++;
            
            // Explore all neighbors of current state
            int[] currentCoords = Coordinates.get2DFrom1D(currentState.getCoordinate(), size);
            
            for (int[] neighborOffset : MazeGenerator.NEIGHBORS) {
                int neighborX = currentCoords[1] + neighborOffset[1];
                int neighborY = currentCoords[0] + neighborOffset[0];
                
                // Check bounds and if neighbor is not known to be blocked
                if (isValidPosition(neighborX, neighborY) && 
                    unknownMaze[neighborY][neighborX] != BLOCKED) {
                    
                    int neighborCoord = Coordinates.get1DFrom2D(neighborX, neighborY, size);
                    
                    // Initialize neighbor if not seen in this search
                    if (search[neighborCoord] < counter) {
                        g[neighborCoord] = Integer.MAX_VALUE;
                        search[neighborCoord] = counter;
                    }
                    
                    // Update neighbor if we found a better path
                    int newCost = g[currentState.getCoordinate()] + MOVEMENT_COST;
                    if (g[neighborCoord] > newCost) {
                        g[neighborCoord] = newCost;
                        open.remove(new State(neighborCoord));
                        
                        State neighborState = new State(neighborCoord, g[neighborCoord], 
                                                      computeHeuristic(neighborCoord, goal), currentState);
                        
                        // Keep reference to goal state for path extraction
                        if (neighborCoord == goal) {
                            goalState = neighborState;
                        }
                        
                        open.insert(neighborState);
                    }
                }
            }
        }
        
        return goalState;
    }

    // === PATH EXTRACTION AND FOLLOWING ===
    
    /**
     * Extracts the path from start to goal using parent pointers.
     * 
     * @param pathState State representing the path using parent pointers
     * @return Deque containing the path (start at bottom, goal at top)
     */
    private Deque<State> extractPath(State pathState, boolean forward) {
        Deque<State> path = new ArrayDeque<>();
        State current = pathState;
        
        while (current != null) {
            if (forward) {
                path.addFirst(current);
            } else {
                path.addLast(current);
            }
            // Mark path cells for visualization
            int[] coords = Coordinates.get2DFrom1D(current.getCoordinate(), size);
            unknownMaze[coords[0]][coords[1]] = unknownMaze[coords[0]][coords[1]] == UNKNOWN ? ON_PATH_UNKNOWN : ON_PATH_UNBLOCKED;
            current = current.getParent();
        }
        
        return path;
    }
    
    /**
     * Follows the planned path until reaching the goal or encountering a blocked cell.
     * Updates the agent's knowledge of the maze as it moves.
     * 
     * @param plannedPath Path to follow
     * @return State where the agent stopped (either goal or last unblocked position)
     */
    private State followPathUntilBlocked(Deque<State> plannedPath) {
        State currentState = plannedPath.pop(); // Start position

        while (currentState.getCoordinate() != target && !plannedPath.isEmpty()) {
            // Mark current position and update visualization
            int[] currentCoords = Coordinates.get2DFrom1D(currentState.getCoordinate(), size);
            unknownMaze[currentCoords[0]][currentCoords[1]] = CURRENT_POSITION;

            if (!discoverNeighbors(currentCoords)) {
                if (storeSteps) {
                    captureCurrentState();
                }
                return currentState; // Stop if we hit a blocked cell
            }
            if (storeSteps) {
                captureCurrentState();
            }

            // Mark as expanded and discover neighbors
            unknownMaze[currentCoords[0]][currentCoords[1]] = EXPANDED;

            // Check if next position in path is blocked
            if (!plannedPath.isEmpty()) {
                State nextState = plannedPath.peek();
                int[] nextCoords = Coordinates.get2DFrom1D(nextState.getCoordinate(), size);

                if (unknownMaze[nextCoords[0]][nextCoords[1]] == BLOCKED) {
                    // Path is blocked, stop here
                    unknownMaze[currentCoords[0]][currentCoords[1]] = CURRENT_POSITION;
                    if (storeSteps) {
                        captureCurrentState();
                    }
                    unknownMaze[currentCoords[0]][currentCoords[1]] = EXPANDED;
                    return currentState;
                }

                currentState = plannedPath.pop();
            }
        }

        // Reached target or end of path
        if (currentState.getCoordinate() == target) {
            int[] targetCoords = Coordinates.get2DFrom1D(target, size);
            unknownMaze[targetCoords[0]][targetCoords[1]] = CURRENT_POSITION;
            if (storeSteps) {
                captureCurrentState();
            }
        }

        return currentState;
    }
    
    // === MAZE KNOWLEDGE UPDATE ===
    
    /**
     * Discovers the true state of cells within sight radius using Manhattan
     * distance.
     * @param currentCoords Current position coordinates [row, col]
     * @return True if path remains valid, false if blocked
     */
    private boolean discoverNeighbors(int[] currentCoords) {
        int currentRow = currentCoords[0];
        int currentCol = currentCoords[1];
        boolean unblocked = true;

        // Discover all cells within Manhattan distance of sightRadius
        for (int row = currentRow - sightRadius; row <= currentRow + sightRadius; row++) {
            for (int col = currentCol - sightRadius; col <= currentCol + sightRadius; col++) {
                int manhattanDistance = Math.abs(row - currentRow) + Math.abs(col - currentCol);

                if (manhattanDistance <= sightRadius && isValidPosition(row, col)) {
                    // Discover true state of this cell
                    if (unknownMaze[row][col] == UNKNOWN) {
                        unknownMaze[row][col] = knownMaze[row][col];
                    } else if (unknownMaze[row][col] == ON_PATH_UNKNOWN) {
                        if (knownMaze[row][col] == BLOCKED) {
                            unblocked = false; // Found a blocked cell
                            unknownMaze[row][col] = BLOCKED; // Update to blocked
                        } else {
                            unknownMaze[row][col] = ON_PATH_UNBLOCKED; // Update path cell
                        }
                    }
                }
            }
        }
        return unblocked;
    }

    // === HEURISTIC COMPUTATION ===
    
    /**
     * Computes the heuristic value for a given position.
     * Uses learned heuristics for Adaptive A*, Manhattan distance otherwise.
     * 
     * @param coordinate Position to compute heuristic for (1D coordinate)
     * @return Heuristic value (estimated cost to goal)
     */
    private int computeHeuristic(int coordinate, int target) {
        // Use learned heuristic if available (Adaptive A*)
        if (h != null && h.containsKey(coordinate)) {
            return h.get(coordinate);
        }
        
        // Default to Manhattan distance
        int[] currentCoords = Coordinates.get2DFrom1D(coordinate, size);
        int[] targetCoords = Coordinates.get2DFrom1D(target, size);
        
        return Math.abs(targetCoords[0] - currentCoords[0]) + 
               Math.abs(targetCoords[1] - currentCoords[1]);
    }
    
    /**
     * Updates learned heuristics based on the current search (Adaptive A* only).
     * For each expanded cell, learns h(s) = g*(goal) - g(s) where g*(goal) is
     * the optimal cost to reach the goal.
     */
    private void updateLearnedHeuristics() {
        for (Integer expandedCell : closed) {
            h.put(expandedCell, g[target] - g[expandedCell]);
        }
    }

    // === UTILITY METHODS ===
    
    /**
     * Checks if the given coordinates are within maze bounds.
     * 
     * @param x X coordinate (column)
     * @param y Y coordinate (row)
     * @return True if position is valid, false otherwise
     */
    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }
    
    /**
     * Resets path visualization by changing ON_PATH cells back to UNKNOWN.
     * Restores start and target cell markings.
     */
    private void resetPathVisualization() {
        for (int i = 0; i < unknownMaze.length; i++) {
            for (int j = 0; j < unknownMaze[0].length; j++) {
                if (unknownMaze[i][j] == ON_PATH_UNKNOWN) {
                    unknownMaze[i][j] = UNKNOWN;
                } else if (unknownMaze[i][j] == ON_PATH_UNBLOCKED) {
                    unknownMaze[i][j] = UNBLOCKED;
                }
            }
        }
        
        // Restore start and target markers
        int[] startCoords = Coordinates.get2DFrom1D(start, size);
        int[] targetCoords = Coordinates.get2DFrom1D(target, size);
        unknownMaze[startCoords[0]][startCoords[1]] = START_CELL;
        unknownMaze[targetCoords[0]][targetCoords[1]] = TARGET_CELL;
    }
    
    /**
     * Captures the current state of the maze for visualization.
     */
    private void captureCurrentState() {
        int[][] currentState = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(unknownMaze[i], 0, currentState[i], 0, size);
        }
        steps.add(currentState);
    }

    /**
     * Creates an enhanced SolveResult with all necessary data for saving and
     * replay.
     */
    private SolveResult createEnhancedResult(boolean solved, int expandedCells, String algorithmName,
            long solutionTimeMs) {
        int[] startPos = Coordinates.get2DFrom1D(start, size);
        int[] targetPos = Coordinates.get2DFrom1D(target, size);

        return new SolveResult(solved, expandedCells, algorithmName, tiebreaker, sightRadius,
                size, knownMaze, new int[] { startPos[0], startPos[1] },
                new int[] { targetPos[0], targetPos[1] },
                storeSteps ? new ArrayList<>(steps) : null, solutionTimeMs);
    }

    // === GETTERS ===
    
    /**
     * Gets the current state of the agent's maze knowledge.
     * 
     * @return 2D array representing the agent's current maze knowledge
     */
    public int[][] getUnknownMaze() {
        return unknownMaze;
    }
    
    /**
     * Gets the list of maze states captured during solving for visualization.
     * 
     * @return List of maze states at each step
     */
    public List<int[][]> getSteps() {
        return steps;
    }
}