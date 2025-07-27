#!/bin/bash
echo "Starting A-Star Data Collector..."
echo
java -cp "lib/*:build" DataCollector
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start Data Collector"
    echo "Make sure you have run ./build.sh first"
    exit 1
fi
