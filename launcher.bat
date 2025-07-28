@echo off
if "%1"=="silent" goto :silent

REM Launch silently in background
start /min "" "%~f0" silent
exit

:silent
REM Change to the directory containing this script
cd /d "%~dp0"

REM Clean, build, and run silently
start "" /B cmd /c clean.bat
start "" /B cmd /c build.bat
if %ERRORLEVEL% neq 0 exit /b 1

REM Launch the application and exit
call scripts\run.bat
exit
