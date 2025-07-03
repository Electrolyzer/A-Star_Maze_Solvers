package src;

// UnitTests.java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MazeSolver using predetermined mazes with known expected
 * results.
 * These tests ensure that algorithm implementations remain consistent and
 * correct.
 */
public class UnitTests {

    // Expected expansion counts for each algorithm on the 50 predetermined mazes
    private static final int[] EXPECTED_FORWARD_G = {
            11250, 16002, 8990, 13791, 14143, 8562, 7380, 10005, 8685, 8516, 9379, 10570, 10690, 7624, 9276, 8203,
            10050, 9584, 7206, 12950, 7349, 10217, 7036, 8512, 11315, 9764, 8838, 6788, 7816, 9689, 5521, 6796, 8471,
            13216, 7915, 7966, 6896, 9327, 18161, 12068, 9299, 10401, 9348, 9181, 9262, 6499, 8476, 8196, 22211, 201
    };

    private static final int[] EXPECTED_FORWARD_H = {
            360130, 258904, 243255, 284299, 265041, 215272, 274272, 304233, 209762, 285987, 253263, 198057, 220592,
            178651, 215442, 262289, 288129, 292548, 218677, 346940, 275228, 241386, 254355, 272959, 297790, 335740,
            274051, 260176, 230818, 309746, 151329, 188508, 248952, 273267, 286783, 239405, 271045, 241613, 275067,
            292381, 279439, 215484, 322294, 274275, 233510, 195973, 286196, 259574, 465401, 10200
    };

    private static final int[] EXPECTED_BACKWARD_G = {
            61812, 52040, 124373, 109785, 107627, 184963, 83561, 77664, 119692, 115089, 157592, 198092, 249577, 148875,
            89086, 78547, 80582, 84933, 69963, 74223, 79468, 151850, 201161, 62415, 126546, 88044, 76903, 92949, 191108,
            178239, 237483, 97917, 136339, 85747, 59548, 94670, 210045, 133465, 133509, 104286, 146643, 147841, 65713,
            100727, 199751, 144222, 208329, 197535, 144260, 10398
    };

    private static final int[] EXPECTED_ADAPTIVE_G = {
            11090, 15667, 8841, 13631, 12247, 8501, 7374, 9952, 8381, 8494, 9285, 10531, 13679, 7564, 8950, 8193, 10058,
            9542, 7133, 12717, 7344, 10113, 7030, 8503, 10110, 9705, 8610, 6786, 7782, 9581, 5509, 6786, 8358, 11900,
            7912, 7960, 6893, 9068, 18145, 11718, 9205, 10203, 9325, 9152, 9188, 6488, 8442, 8148, 22163, 201
    };

    private int[][] loadTestMaze(int mazeIndex) {
        String file = String.format("%02d", mazeIndex);
        return MazeReader.readMaze("mazes/" + file + ".txt");
    }

    @Test
    public void testForwardAStarWithGTiebreaker() {
        System.out.println("Testing Forward A* with g-value tiebreaker...");

        for (int i = 0; i < Math.min(EXPECTED_FORWARD_G.length, 50); i++) {
            int[][] maze = loadTestMaze(i);
            MazeSolver solver = new MazeSolver(maze, 'g', 1);
            SolveResult result = solver.solveForward();

            assertEquals(EXPECTED_FORWARD_G[i], result.expandedCells,
                    "Forward A* (g) expansion count mismatch for maze " + i);
        }

        System.out.println("Forward A* (g) tests passed!");
    }

    @Test
    public void testForwardAStarWithHTiebreaker() {
        System.out.println("Testing Forward A* with h-value tiebreaker...");

        for (int i = 0; i < Math.min(EXPECTED_FORWARD_H.length, 50); i++) {
            int[][] maze = loadTestMaze(i);
            MazeSolver solver = new MazeSolver(maze, 'h', 1);
            SolveResult result = solver.solveForward();

            assertEquals(EXPECTED_FORWARD_H[i], result.expandedCells,
                    "Forward A* (h) expansion count mismatch for maze " + i);
        }

        System.out.println("Forward A* (h) tests passed!");
    }

    @Test
    public void testBackwardAStarWithGTiebreaker() {
        System.out.println("Testing Backward A* with g-value tiebreaker...");

        for (int i = 0; i < Math.min(EXPECTED_BACKWARD_G.length, 50); i++) {
            int[][] maze = loadTestMaze(i);
            MazeSolver solver = new MazeSolver(maze, 'g', 1);
            SolveResult result = solver.solveBackward();

            assertEquals(EXPECTED_BACKWARD_G[i], result.expandedCells,
                    "Backward A* (g) expansion count mismatch for maze " + i);
        }

        System.out.println("Backward A* (g) tests passed!");
    }

    @Test
    public void testAdaptiveAStarWithGTiebreaker() {
        System.out.println("Testing Adaptive A* with g-value tiebreaker...");

        for (int i = 0; i < Math.min(EXPECTED_ADAPTIVE_G.length, 50); i++) {
            int[][] maze = loadTestMaze(i);
            MazeSolver solver = new MazeSolver(maze, 'g', 1);
            SolveResult result = solver.solveAdaptive();

            assertEquals(EXPECTED_ADAPTIVE_G[i], result.expandedCells,
                    "Adaptive A* (g) expansion count mismatch for maze " + i);
        }

        System.out.println("Adaptive A* (g) tests passed!");
    }

}