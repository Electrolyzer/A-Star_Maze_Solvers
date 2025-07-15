# A-Star
This is an implementation of A* search, a graph search algorithm. It creates random mazes and then runs different variations of A* search to solve them, calculating the efficiency of each and comparing them against each other. MazeSolverTester is used to create and track a single solved maze to make sure everything is working as intended before running DataCollector to collect large scale data spanning many different mazes and variations.

Steps to run DataCollector
    - Download codebase
    - Run DataCollector
    - Click on generate new mazes and adjust size, number and sight radius.
    - Click 'run analysis'
    - During the next few seconds the results should populate the left side text area while the progress bar fills.
    - Once the mazes are done processing the heatmaps will populate the right side panel.


Steps to run MazeSolverTester
    - Download codebase
    - Run MazeSolverTester
    - Edit the maze. Instructions show up in the GUI.
    - View solution, try out the various controls such as step-by-step, rerun, change algorithm, etc.
    - Save the maze using the save button
    - Try loading the mazes (some come pre-saved). You can select multiple mazes by using either ctrl-lclick or shift-click.
    - Try out the unified step-by-step

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
