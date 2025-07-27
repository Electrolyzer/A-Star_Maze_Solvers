#!/bin/bash
echo "Building A-Star Maze Solver..."
echo

# Create build directory
mkdir -p build

# Clean previous builds
rm -rf build/*

# Compile source files to build directory
echo "Compiling source files..."
javac -cp "lib/*:src" -d build src/*.java
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to compile source files"
    exit 1
fi

# Compile main classes to build directory
echo "Compiling main classes..."
javac -cp "lib/*:build" -d build *.java
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to compile main classes"
    exit 1
fi

echo
echo "Build completed successfully!"
echo "Compiled files are in the 'build' directory."
echo
echo "To run the applications:"
echo "  - Data Collector: ./run-datacollector.sh"
echo "  - Maze Solver Tester: ./run-mazetester.sh"
echo
