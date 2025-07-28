@echo off
title A-Star Maze Solver - Development Launcher
chcp 65001 > nul

echo.
echo  ╔══════════════════════════════════════════════════════════════╗
echo  ║              A* Maze Solver - Development Mode               ║
echo  ╠══════════════════════════════════════════════════════════════╣
echo  ║                                                              ║
echo  ║  Force rebuilding and launching for development...           ║
echo  ║                                                              ║
echo  ╚══════════════════════════════════════════════════════════════╝
echo.

echo Cleaning previous builds...
call scripts\clean.bat

echo Building project (forced rebuild)...
call scripts\build.bat
if %ERRORLEVEL% neq 0 (
    echo.
    echo ╔══════════════════════════════════════════════════════════════╗
    echo ║                        BUILD FAILED                          ║
    echo ╠══════════════════════════════════════════════════════════════╣
    echo ║  There was an error building the project.                    ║
    echo ║  Please check the source code for compilation errors.        ║
    echo ╚══════════════════════════════════════════════════════════════╝
    echo.
    pause
    exit /b 1
)

echo Launching A-Star Maze Solver (Development Mode)...
echo.
call scripts\run.bat

echo.
echo Development session ended.
