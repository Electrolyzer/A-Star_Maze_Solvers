package src;

// MazeReader.java
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Utility class for reading maze data from text files.
 * Converts text representations of mazes into 2D integer arrays.
 */
public class MazeReader {
    public static int SIZE = Constants.DEFAULT_MAZE_SIZE;

    /**
     * Reads a maze from a text file and converts it to a 2D array.
     * 
     * @param filename The path to the maze file
     * @return 2D array representing the maze (0=empty, 1=blocked)
     */
    public static int[][] readMaze(String filename) {
        int[][] maze = new int[SIZE][SIZE];
        String line = "";
        int s = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                int[] coordinates = Coordinates.get2DFrom1D(s, SIZE);
                maze[coordinates[0]][coordinates[1]] = Integer.parseInt(line);
                s++;
            }
            br.close();
        } catch (Exception ignored) {
        }
        return maze;
    }
}
