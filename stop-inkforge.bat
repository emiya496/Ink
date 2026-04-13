@echo off
setlocal

powershell.exe -ExecutionPolicy Bypass -File "%~dp0stop-inkforge.ps1"
if errorlevel 1 (
    echo.
    echo Stop failed. Please check the message above.
    pause
)
