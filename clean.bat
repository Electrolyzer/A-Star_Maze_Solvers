@echo off
echo Cleaning A-Star Maze Solver build files...
echo.

REM Remove build directory
if exist "build" (
    echo Removing build directory...
    rmdir /s /q "build"
)

REM Remove any stray class files (legacy cleanup)
if exist "src\*.class" (
    echo Removing stray source class files...
    del "src\*.class"
)

if exist "*.class" (
    echo Removing stray main class files...
    del "*.class"
)

REM Remove Results directory if it exists
if exist "Results" (
    echo Removing Results directory...
    rmdir /s /q "Results"
)

echo.
echo Clean completed successfully!
echo All compiled files and build directory have been removed.
echo.
pause
