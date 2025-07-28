#!/bin/bash

# Clean build artifacts for Unix/Linux/macOS

echo "Cleaning build directory..."

# Remove build directory if it exists
if [ -d "build" ]; then
    rm -rf build
    echo "Build directory removed"
else
    echo "Build directory already clean"
fi

echo "Clean complete"
