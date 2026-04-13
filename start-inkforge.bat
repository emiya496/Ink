@echo off
setlocal

powershell.exe -ExecutionPolicy Bypass -File "%~dp0start-inkforge.ps1" -OpenBrowser
if errorlevel 1 (
    echo.
    echo Launch failed. Please check the message above.
    pause
)
