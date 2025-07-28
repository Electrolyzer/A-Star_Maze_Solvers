@echo off

REM Remove build directory
if exist "build" (
    rmdir /s /q "build" >nul 2>&1
)

REM Remove any stray class files (legacy cleanup)
if exist "src\*.class" (
    del "src\*.class" >nul 2>&1
)

if exist "*.class" (
    del "*.class" >nul 2>&1
)
