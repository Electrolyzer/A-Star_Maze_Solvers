#!/bin/bash

# Smart build script for Unix/Linux/macOS
# Only rebuilds when necessary

BUILD_NEEDED=0

# Check if build directory exists
if [ ! -d "build" ]; then
    BUILD_NEEDED=1
fi

# Check if main Application class exists
if [ ! -f "build/src/Application.class" ]; then
    BUILD_NEEDED=1
fi

# Check if MazeSolverUI class exists (key UI component)
if [ ! -f "build/src/MazeSolverUI.class" ]; then
    BUILD_NEEDED=1
fi

# Simple check: if any .java file is newer than the Application class
if [ $BUILD_NEEDED -eq 0 ] && [ -f "build/src/Application.class" ]; then
    # Find any .java file newer than the compiled Application class
    if find src -name "*.java" -newer "build/src/Application.class" | grep -q .; then
        BUILD_NEEDED=1
    fi
fi

# Build if needed
if [ $BUILD_NEEDED -eq 1 ]; then
    echo "Build needed - compiling..."
    ./scripts/build.sh
    exit $?
else
    echo "Build up to date - skipping compilation"
    exit 0
fi
