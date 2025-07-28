#!/bin/bash

# A* Maze Solver - Unix/Linux/macOS Launcher
# Cross-platform launcher with smart build detection

# Function to check if running in silent mode
if [ "$1" = "silent" ]; then
    # Change to the directory containing this script
    cd "$(dirname "$0")"
    
    # Smart build - only build if needed
    ./scripts/smart-build.sh
    if [ $? -ne 0 ]; then
        exit 1
    fi
    
    # Launch the application and exit
    ./scripts/run.sh
    exit 0
fi

# Launch silently in background (equivalent to Windows start /min)
if command -v gnome-terminal >/dev/null 2>&1; then
    # GNOME/Ubuntu
    gnome-terminal --window --hide-menubar --title="A* Maze Solver" -- bash -c "'$0' silent; exec bash"
elif command -v konsole >/dev/null 2>&1; then
    # KDE
    konsole --hide-menubar --title "A* Maze Solver" -e bash -c "'$0' silent; exec bash"
elif command -v xterm >/dev/null 2>&1; then
    # Generic X11
    xterm -title "A* Maze Solver" -e bash -c "'$0' silent; exec bash" &
elif [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    osascript -e "tell application \"Terminal\" to do script \"cd '$(pwd)' && '$0' silent\""
else
    # Fallback - run in current terminal
    echo "Launching A* Maze Solver..."
    "$0" silent
fi

exit 0
