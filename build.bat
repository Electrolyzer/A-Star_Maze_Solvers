@echo off
echo Building A-Star Maze Solver...
echo.

REM Create build directory
if not exist "build" mkdir build

REM Clean previous builds
if exist "build\*.class" del /q "build\*.class"
if exist "build\src" rmdir /s /q "build\src"

REM Compile source files to build directory
echo Compiling source files...
javac -cp "lib/*;src" -d build src/*.java
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to compile source files
    pause
    exit /b 1
)

REM Compile main classes to build directory
echo Compiling main classes...
javac -cp "lib/*;build" -d build *.java
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to compile main classes
    pause
    exit /b 1
)

echo.
echo Build completed successfully!
echo Compiled files are in the 'build' directory.
echo.
echo To run the applications:
echo   - Data Collector: run-datacollector.bat
echo   - Maze Solver Tester: run-mazetester.bat
echo.
pause
