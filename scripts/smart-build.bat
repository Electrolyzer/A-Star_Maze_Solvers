@echo off

REM Simple but effective build check
set BUILD_NEEDED=0

REM Check if build directory exists
if not exist "build" (
    set BUILD_NEEDED=1
    goto :build
)

REM Check if main Application class exists
if not exist "build\src\Application.class" (
    set BUILD_NEEDED=1
    goto :build
)

REM Check if MazeSolverUI class exists (key UI component)
if not exist "build\src\MazeSolverUI.class" (
    set BUILD_NEEDED=1
    goto :build
)

REM Simple check: if any .java file is newer than 1 minute, rebuild
REM This catches most development scenarios without complex timestamp comparison
forfiles /p src /m *.java /c "cmd /c exit 0" /d +0 >nul 2>&1
if %ERRORLEVEL%==0 (
    set BUILD_NEEDED=1
    goto :build
)

:build
if %BUILD_NEEDED%==1 (
    echo Build needed - compiling...
    call scripts\build.bat
    exit /b %ERRORLEVEL%
) else (
    echo Build up to date - skipping compilation
    exit /b 0
)
