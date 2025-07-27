@echo off
title A-Star Maze Solver Launcher
color 0A

:menu
cls
echo.
echo  ╔══════════════════════════════════════════════════════════════╗
echo  ║                A* Maze Solver - Main Launcher                ║
echo  ╠══════════════════════════════════════════════════════════════╣
echo  ║                                                              ║
echo  ║  1. Build Project                                            ║
echo  ║  2. Run Unified Maze Solver (Recommended)                   ║
echo  ║  3. Run Interactive Maze Editor (Legacy)                    ║
echo  ║  4. Run Performance Data Collector (Legacy)                 ║
echo  ║  5. Test Build System                                        ║
echo  ║  6. View README                                              ║
echo  ║  7. View Demo Guide                                          ║
echo  ║  8. Exit                                                     ║
echo  ║                                                              ║
echo  ╚══════════════════════════════════════════════════════════════╝
echo.
set /p choice="Enter your choice (1-8): "

if "%choice%"=="1" goto build
if "%choice%"=="2" goto unified
if "%choice%"=="3" goto mazetester
if "%choice%"=="4" goto datacollector
if "%choice%"=="5" goto test
if "%choice%"=="6" goto readme
if "%choice%"=="7" goto demoguide
if "%choice%"=="8" goto exit

echo Invalid choice. Please try again.
pause
goto menu

:build
cls
echo Building project...
call build.bat
goto menu

:unified
cls
echo Starting Unified Maze Solver...
call run-unified.bat
goto menu

:mazetester
cls
echo Starting Interactive Maze Editor...
call run-mazetester.bat
goto menu

:datacollector
cls
echo Starting Performance Data Collector...
call run-datacollector.bat
goto menu

:test
cls
echo Testing build system...
call test-build.bat
goto menu

:readme
cls
echo Opening README.md...
start README.md
goto menu

:demoguide
cls
echo Opening Demo Guide...
start DEMO_GUIDE.md
goto menu

:exit
echo.
echo Thank you for using A* Maze Solver!
echo.
pause
exit
