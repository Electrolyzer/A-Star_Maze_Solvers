@echo off

REM Create build directory
if not exist "build" mkdir build

REM Clean previous builds
if exist "build\*.class" del /q "build\*.class" >nul 2>&1
if exist "build\src" rmdir /s /q "build\src" >nul 2>&1

REM Compile source files to build directory
start "" /B javac -cp "lib/*;src" -d build src/*.java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    exit /b 1
)

REM Compile main classes to build directory
start "" /B javac -cp "lib/*;build" -d build *.java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    exit /b 1
)
