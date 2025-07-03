package src;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MazeGenerator is Class used to Generate Mazes
 *
 */
public class MazeGenerator {
    public static final int[][] NEIGHBORS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

    /**
     * Shouldn't make instance of this Class
     */
    private MazeGenerator() {}

    /**
     * Generates Maze
     * Uses Default Size of 30x30
     * Start Tile is Top Left Corner and Target Tile is Bottom Right Corner
     * @return generated maze
     */
    public static int[][] generateMaze(){
        return generateMaze(30);
    }

    /**
     * Generates Maze
     * Start Tile is Top Left Corner and Target Tile is Bottom Right Corner
     * @param size Maze is square with side length size
     * @return generated maze
     */
    public static int[][] generateMaze(int size){
        return generateMaze(size, 0, 0, size - 1, size - 1);
    }

    /**
     * Generates Maze
     * @param size Maze is square with side length size
     * @param start_x X Coordinate of Start Tile of Maze
     * @param start_y Y Coordinate of Start Tile of Maze
     * @param target_x X Coordinate of Target Tile of Maze
     * @param target_y Y Coordinate of Target Tile of Maze
     * @return generated maze
     */
    public static int[][] generateMaze(int size, int start_x, int start_y, int target_x, int target_y) {
        int[][] maze = new int[size][size];
        boolean[] visited = new boolean[size * size];                                           // Keeps track of which tiles have been visited during generation
        visited[0] = true;                                                                      // Start and end should not be blocked
        visited[size * size - 1] = true;
        List<Integer> placesToVisit = new ArrayList<>();                                        // Randomize order to check for tile filling
        for (int i = 0; i < size * size; i++) {
            placesToVisit.add(i);
        }
        Collections.shuffle(placesToVisit);


        for (int i = 0; i < size * size; i++) {
            if (visited[placesToVisit.get(i)])                                                  // If already visited, don't visit again
                continue;
            int[] posToVisit = Coordinates.get2DFrom1D(placesToVisit.get(i), size);             // Transform back into x, y coordinates
            fillMaze(maze, visited, size, posToVisit, true);                                // Start filling at place to visit
        }

        maze[start_y][start_x] = 0;                                                             //Ensures Start Tile is Unblocked
        maze[target_y][target_x] = 0;                                                           //Ensures Target Tile is Unblocked
        return maze;
    }

    /**
     * Helper Method that Recursively Fills in Maze
     * @param maze Generated Maze to Return
     * @param visited Keeps track of which tiles have been visited during generation
     * @param size Maze is square with side length size
     * @param pos Coordinates of Tile being looked at to fill/not fill
     * @param first This method guarantees 1 unfilled block on first call (not recursive calls though)
     */
    private static void fillMaze(int[][] maze, boolean[] visited, int size, int[] pos, boolean first) {
        visited[Coordinates.get1DFrom2D(pos[1], pos[0], size)] = true;                      // Set visited status to true
        if (Math.random() < .7 || first) {                                                  // 70% chance to be unfilled and added to stack
            for (int[] neighbor : getUnvisitedNeighbors(pos[0], pos[1], visited, size)) {   // If added to stack, add all neighbors
                if (visited[Coordinates.get1DFrom2D(neighbor[1], neighbor[0], size)])                              // If the neighbor has been visited before being dealt
                    continue;                                                               // with, skip this neighbor
                fillMaze(maze, visited, size, neighbor, false);                         // Else, add this neighbor to the stack
            }
        } else {
            maze[pos[0]][pos[1]] = 1;                                                       // 30% chance to be filled and not added to the stack
        }
    }

    /**
     * Helper Method that Given Tile, Returns List of Unvisited Neighbours
     * @param x X Coordinate of Given Tile
     * @param y Y Coordinate of Given Tile
     * @param visited Keeps track of which tiles have been visited during generation
     * @param size Maze is square with side length size
     * @return List of Unvisited Neighbours
     */
    private static List<int[]> getUnvisitedNeighbors(int x, int y, boolean[] visited, int size) {
        List<int[]> unvisitedNeighbors = new ArrayList<>();
        for (int[] neighbor : NEIGHBORS) {
            int nx = x + neighbor[1];
            int ny = y + neighbor[0];
            if (nx >= 0 && nx < size && ny >= 0 && ny < size && !visited[Coordinates.get1DFrom2D(nx, ny, size)]) {
                unvisitedNeighbors.add(new int[] { ny, nx });
            }
        }
        return unvisitedNeighbors;
    }
}
