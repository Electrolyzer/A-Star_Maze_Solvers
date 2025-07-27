#!/bin/bash
echo "Starting Unified A-Star Maze Solver..."
echo
java -cp "lib/*:build" UnifiedMazeSolver
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start Unified Maze Solver"
    echo "Make sure you have run ./build.sh first"
    exit 1
fi
