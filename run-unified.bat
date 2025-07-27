@echo off
echo Starting Unified A-Star Maze Solver...
echo.
java -cp "lib/*;build" UnifiedMazeSolver
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to start Unified Maze Solver
    echo Make sure you have run build.bat first
    pause
    exit /b 1
)
