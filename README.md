# A-Star
This is an implementation of A* search, a graph search algorithm. It creates random mazes and then runs different variations of A* search to solve them, calculating the efficiency of each and comparing them against each other. MazeSolverTester is used to create and track a single solved maze to make sure everything is working as intended before running DataCollector to collect large scale data spanning many different mazes and variations.

For this submission we are adding a new feature, sight radius. Previously, the algorithm only knew the contents of squares which it had been adjacent to at some point. Now, it can know squares up to sightRadius away (Manhattan Distance).

Steps to run DataCollector
    - Download codebase
    - Run DataCollector
    - Click on generate new mazes and adjust size, number and sight radius.
    - Click 'run analysis'
    - During the next few seconds the results should populate the left side text area while the progress bar fills.
        Expected averages (for maze size 101): 
        - Forward A* with g being the tiebreaker is: 9591.82
        - Forward A* with h being the tiebreaker is: 258773.76
        - Backward A* with g being the tiebreaker is: 123503.74
        - Adaptive A* with g being the tiebreaker is: 9483.16
        (These are run on the mazes saved in the mazes folder so we can check that the algorithms still work properly)
        If you raise the sight radius, these numbers should be expected to go down.
    - Once the mazes are done processing the heatmaps will populate the right side panel.


Steps to run MazeSolverTester
    - Download codebase
    - Run MazeSolverTester
    - Edit the maze. Instructions show up in the GUI.
    - View solution, try out the various controls such as step-by-step, rerun, change algorithm, etc.

Steps to run unit tests (if IDE does not let you automatically)
# Compile (from project folder)
    javac -cp "lib/*" src/*.java test/*.java
# Run all tests
    java -cp ".;lib/junit.jar" org.junit.platform.console.ConsoleLauncher --select-class UnitTests
# Example: Run only testForwardAStarWithGTiebreaker
    java -cp ".;lib/junit.jar" org.junit.platform.console.ConsoleLauncher --select-method UnitTests#testForwardAStarWithGTiebreaker

---

## Model Comparison Project

**Project Date:** 2025-07-03 13:10:38

This project contains model comparison results using codebase switcher:

- `preedit` branch: Original baseline codebase
- `beetle` branch: Beetle model's response
- `sonnet` branch: Sonnet model's response
- `rewrite` branch: Rewritten codebase
