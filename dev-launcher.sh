#!/bin/bash

# A* Maze Solver - Development Launcher (Unix/Linux/macOS)
# Force rebuild and launch for development

# Set terminal title and colors
echo -e "\033]0;A* Maze Solver - Development Launcher\007"

# Yellow color for development mode
echo -e "\033[1;33m"

echo ""
echo "  ╔══════════════════════════════════════════════════════════════╗"
echo "  ║              A* Maze Solver - Development Mode               ║"
echo "  ╠══════════════════════════════════════════════════════════════╣"
echo "  ║                                                              ║"
echo "  ║  Force rebuilding and launching for development...           ║"
echo "  ║                                                              ║"
echo "  ╚══════════════════════════════════════════════════════════════╝"
echo ""

# Reset color
echo -e "\033[0m"

# Change to the directory containing this script
cd "$(dirname "$0")"

echo "Cleaning previous builds..."
./scripts/clean.sh

echo "Building project (forced rebuild)..."
./scripts/build.sh
if [ $? -ne 0 ]; then
    echo ""
    echo -e "\033[1;31m"  # Red color for error
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                        BUILD FAILED                          ║"
    echo "╠══════════════════════════════════════════════════════════════╣"
    echo "║  There was an error building the project.                   ║"
    echo "║  Please check the source code for compilation errors.       ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "\033[0m"  # Reset color
    echo ""
    echo "Press Enter to continue..."
    read
    exit 1
fi

echo "Launching A* Maze Solver (Development Mode)..."
echo ""
./scripts/run.sh

echo ""
echo "Development session ended."
echo "Press Enter to continue..."
read
