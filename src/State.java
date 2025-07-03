package src;
/**
 * State Class is a blueprint for state objects
 * Stores the coordinate, f, g, h, and Parent of State
 */
public class State {
    private State parent;
    private int coordinate;
    private int f;
    private int g;
    private int h;

    /**
     * Creates State Object
     * Mainly used to remove State from Priority Queue
     * @param coordinate The coordinate of the State
     */
    public State(int coordinate) {
        this.coordinate = coordinate;
        this.f = Integer.MAX_VALUE;
        this.g = Integer.MAX_VALUE;
        this.h = Integer.MAX_VALUE;
        this.parent = null;
    }

    /**
     * Creates State Object
     * @param coordinate The coordinate of the State
     * @param g The g value (path cost) of the State
     * @param h The h value (heuristic) of the State
     */
    public State(int coordinate, int g, int h) {
        this.coordinate = coordinate;
        this.f = g + h;
        this.g = g;
        this.h = h;
        this.parent = null;
    }

    /**
     * Creates State Object
     * @param coordinate The coordinate of the State
     * @param g The g value (path cost) of the State
     * @param h The h value (heuristic) of the State
     * @param parent The parent of the State (tree ptr)
     */
    public State(int coordinate, int g, int h, State parent) {
        this.coordinate = coordinate;
        this.f = g + h;
        this.g = g;
        this.h = h;
        this.parent = parent;
    }

    /**
     * Getter Method for coordinate of State
     * @return coordinate of State
     */
    public int getCoordinate() {
        return coordinate;
    }

    /**
     * Getter Method for F value of State
     * @return F value of State
     */
    public int getF() {
        return f;
    }

    /**
     * Getter Method for G value of State
     * @return G value of State
     */
    public int getG() {
        return g;
    }

    /**
     * Getter Method for H value of State
     * @return H value of State
     */
    public int getH() {
        return h;
    }

    /**
     * Increases H and updates F for a state.
     * @param h_new new H. Can be any number, but will only change H if h_New > h_old.
     * @return Returns true if h was updated, false otherwise.
     */
    public boolean increaseH(int h_new) {
        if (h_new > h) {
            h = h_new;
            f = g + h;
            return true;
        }
        return false;
    }

    /**
     * Getter Method for parent of State
     * @return parent of State
     */
    public State getParent() {
        return parent;
    }

    /**
     * Checks if this state is equal to given state
     * Two states are equal to each other if they refer to the same tile
     * @param o given state
     * @return True if states are equal, False otherwise
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof State) {
            State state = (State) o;
            return this.coordinate == state.getCoordinate();
        }
        return false;
    }

    /**
     * Compares the f values of this state and a given state
     * If the f values are equal, a tiebreaker commences where
     * 'g' - Favors Tiles with Larger g Values
     * 'h' - Favors Tiles with Larger h Values
     * @param state given state we're comparing with
     * @param tiebreaker 'g' or 'h', as shown above
     * @return > 0, If given state should be prioritized in heap
     *              in other words, this state has higher f value (or by tiebreaker)
     *         < 0, If this state should be prioritized in heap
     *              in other words, this state has lower f value (or by tiebreaker)
     *           0, otherwise
     */
    public int compareTo(State state, char tiebreaker){
        if (this.f != state.getF()){
            return (this.f - state.getF());
        }
        switch (tiebreaker){
            case 'g':
                return (state.getG() - this.g);
            case 'h':
                return (state.getH() - this.h);
            default:
                return 0;
        }
    }

    /**
     * Compares the f values of this state and a given state
     * This method doesn't care about tiebreaking
     * @param state given state we're comparing with
     * @return > 0, If given state should be prioritized in heap
     *              in other words, this state has higher f value
     *         < 0, If this state should be prioritized in heap
     *              in other words, this state has lower f value
     *           0, otherwise
     */
    public int compareTo(State state){
        return (this.f - state.getF());
    }
}
