#!/bin/bash
echo "Starting A-Star Maze Solver Tester..."
echo
java -cp "lib/*:build" MazeSolverTester
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start Maze Solver Tester"
    echo "Make sure you have run ./build.sh first"
    exit 1
fi
