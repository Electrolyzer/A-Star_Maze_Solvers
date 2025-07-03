package src;
public class SolveResult {
    public boolean solved;
    public int expandedCells;

    public SolveResult(boolean solved, int expandedCells) {
        this.solved = solved;
        this.expandedCells = expandedCells;
    }

    @Override
    public String toString() {
        String s = "Maze " + 
                (solved ? "was solved" : "was found to be unsolvable") +
                " with " + expandedCells + " expansions";
        return s;
    }
}
