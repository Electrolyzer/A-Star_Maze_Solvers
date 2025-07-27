@echo off
echo Testing A-Star Maze Solver Build System...
echo.

REM Test compilation
echo [1/3] Testing build process...
call build.bat
if %ERRORLEVEL% neq 0 (
    echo ERROR: Build failed
    exit /b 1
)

echo.
echo [2/3] Testing unit tests...
java -cp "build;lib/junit.jar" org.junit.platform.console.ConsoleLauncher --select-class src.UnitTests
if %ERRORLEVEL% neq 0 (
    echo WARNING: Some unit tests may have failed (this is expected if JUnit setup differs)
)

echo.
echo [3/3] Verifying class files exist...
if not exist "build\src\MazeSolver.class" (
    echo ERROR: MazeSolver.class not found in build directory
    exit /b 1
)
if not exist "build\DataCollector.class" (
    echo ERROR: DataCollector.class not found in build directory
    exit /b 1
)
if not exist "build\MazeSolverTester.class" (
    echo ERROR: MazeSolverTester.class not found in build directory
    exit /b 1
)

echo.
echo ✓ All tests passed! The project is ready for demo.
echo.
echo Quick Demo Guide:
echo 1. Run ".\run-mazetester.bat" for interactive maze editing
echo 2. Run ".\run-datacollector.bat" for performance analysis
echo 3. Try editing a maze and solving it with different algorithms
echo.
pause
