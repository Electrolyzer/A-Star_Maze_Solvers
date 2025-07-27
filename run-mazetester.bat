@echo off
echo Starting A-Star Maze Solver Tester...
echo.
java -cp "lib/*;build" MazeSolverTester
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to start Maze Solver Tester
    echo Make sure you have run build.bat first
    pause
    exit /b 1
)
