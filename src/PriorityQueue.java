package src;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Our implementation of a Heap
 * Heap Stores States and Prioritizes Minimum F Value
 * 
 */
public class PriorityQueue {

    private State[] elements;
    private int maxSize;
    private int currSize;
    private char tiebreaker;

    private HashMap<Integer, Integer> keyMap;

    private State nullState = new State(-1);

    /**
     * Creates a Heap Object
     * Tiebreaker defaults to favoring larger g value
     * 
     * @param capacity The Maximum Amount of States that can be stored in the Heap
     *                 at a time
     */
    public PriorityQueue(int capacity) {
        maxSize = capacity;
        currSize = 0;
        elements = new State[capacity];
        keyMap = new HashMap<Integer, Integer>(capacity);
        Arrays.fill(elements, nullState);
        this.tiebreaker = 'g';
    }

    /**
     * Creates a Heap Object
     * 
     * @param capacity   The Maximum Amount of States that can be stored in the Heap
     *                   at a time
     * @param tiebreaker Used For Breaking Ties between States with same f values
     */
    public PriorityQueue(int capacity, char tiebreaker) {
        maxSize = capacity;
        currSize = 0;
        elements = new State[capacity];
        keyMap = new HashMap<Integer, Integer>(capacity);
        Arrays.fill(elements, nullState);
        this.tiebreaker = tiebreaker;
    }

    /**
     * Method to Check if Priority Queue is Empty
     * 
     * @return True if PQ Is Empty, False otherwise
     */
    public boolean isEmpty() {
        return currSize == 0;
    }

    /**
     * Swaps the Position of Two States In the Heap
     * 
     * @param a The index of the first State to Swap
     * @param b The index of the second State to Swap
     */
    private void swap(int a, int b) {
        State temp = elements[a];
        elements[a] = elements[b];
        elements[b] = temp;
        keyMap.replace(elements[a].getCoordinate(), a);
        keyMap.replace(elements[b].getCoordinate(), b);
    }

    /**
     * Helper method to find parent given key
     * 
     * @param key the index you want to find the parent of
     * @return the index of the parent
     */
    private int parent(int key) {
        return (key - 1) / 2;
    }

    /**
     * Helper method to find left child given key
     * 
     * @param key the index you want to find the left child of
     * @return the index of the left child
     */
    private int left(int key) {
        return 2 * key + 1;
    }

    /**
     * Helper method to find right child given key
     * 
     * @param key the index you want to find the right child of
     * @return the index of the right child
     */
    private int right(int key) {
        return 2 * key + 2;
    }

    /**
     * Swim Method for Heap
     * 
     * @param key The index of the State you want to Swim up
     */
    private void swim(int key) {
        while (elements[key].compareTo(elements[parent(key)], tiebreaker) < 0) {
            swap(key, parent(key));
            key = parent(key);
            if (key == 0)
                break;
        }
    }

    /**
     * Sink Method for Heap
     * 
     * @param key the index of the State you want to Sink down
     */
    private void sink(int key) {
        while (elements[key].compareTo(elements[left(key)], tiebreaker) > 0
                || elements[key].compareTo(elements[right(key)], tiebreaker) > 0) {
            int left = left(key);
            int right = right(key);

            int childToSwitch = key;
            if (left <= currSize && elements[childToSwitch].compareTo(elements[left(key)], tiebreaker) > 0)
                childToSwitch = left;
            if (right <= currSize && elements[childToSwitch].compareTo(elements[right(key)], tiebreaker) > 0)
                childToSwitch = right;

            swap(key, childToSwitch);
            key = childToSwitch;
        }
    }

    /**
     * Inserts a State into the Heap
     * Will Only Insert if current size is less than maximum size (capacity)
     * 
     * @param value the State to Insert into the Heap
     * @return True if the State was inserted into the Heap, False Otherwise
     */
    public boolean insert(State value) {
        if (currSize == maxSize)
            return false;
        elements[currSize] = value;
        keyMap.put(value.getCoordinate(), currSize);
        swim(currSize);
        currSize++;
        return true;
    }

    /**
     * Informs the priority queue that the F-value of the input state has been
     * reduced and so it should be swum.
     * 
     * @param state the State to swim
     * @return True if the state was found in the heap, false if it was not
     */
    public boolean decreaseKey(State state) {
        Integer key = keyMap.get(state.getCoordinate());
        if (key != null) {
            swim(key);
            return true;
        }
        return false;
    }

    /**
     * Informs the priority queue that the F-value of the input state has been
     * increased and so it should be sunk.
     * 
     * @param state the State to sink
     * @return True if the state was found in the heap, false if it was not
     */
    public boolean increaseKey(State state) {
        Integer key = keyMap.get(state.getCoordinate());
        if (key != null) {
            sink(key);
            return true;
        }
        return false;
    }

    /**
     * Removes a given State from the Heap
     *
     * @param state the State to Remove from the Heap
     * @return True if State was Removed, False otherwise
     */
    public boolean remove(State state) {
        Integer key = keyMap.get(state.getCoordinate());
        if (key != null) {
            deleteKey(key);
            return true;
        }
        return false;
    }

    /**
     * Returns the Top of the Heap without Removing it
     * 
     * @return the Top of the Heap
     */
    public State peek() {
        if (currSize == 0)
            return nullState;
        return elements[0];
    }

    /**
     * Returns the Top of the Heap with Removing it
     * 
     * @return the Top of the Heap
     */
    public State pop() {
        State min = peek();
        deleteKey(0);
        return min;
    }

    /**
     * Removes the State at a given index
     * 
     * @param key the index of the State to Remove from the Heap
     */
    public void deleteKey(int key) {
        currSize--;
        keyMap.remove(elements[key].getCoordinate());
        swap(key, currSize);
        elements[currSize] = nullState;
        sink(key);
    }

    /**
     * Getter Method for current size
     * 
     * @return current size
     */
    public int getCurrSize() {
        return currSize;
    }

    /**
     * Getter Method for List of States
     * 
     * @return List of States
     */
    public State[] getElements() {
        return elements;
    }
}