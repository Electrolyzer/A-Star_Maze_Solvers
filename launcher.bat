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
echo  ║  2. Run Interactive Maze Editor                              ║
echo  ║  3. Run Performance Data Collector                           ║
echo  ║  4. Test Build System                                        ║
echo  ║  5. View README                                              ║
echo  ║  6. View Demo Guide                                          ║
echo  ║  7. Exit                                                     ║
echo  ║                                                              ║
echo  ╚══════════════════════════════════════════════════════════════╝
echo.
set /p choice="Enter your choice (1-7): "

if "%choice%"=="1" goto build
if "%choice%"=="2" goto mazetester
if "%choice%"=="3" goto datacollector
if "%choice%"=="4" goto test
if "%choice%"=="5" goto readme
if "%choice%"=="6" goto demoguide
if "%choice%"=="7" goto exit

echo Invalid choice. Please try again.
pause
goto menu

:build
cls
echo Building project...
call build.bat
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
