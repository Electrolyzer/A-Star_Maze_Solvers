@echo off
echo Starting A-Star Data Collector...
echo.
java -cp "lib/*;build" DataCollector
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to start Data Collector
    echo Make sure you have run build.bat first
    pause
    exit /b 1
)
