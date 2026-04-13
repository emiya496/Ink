@echo off
setlocal

python "%~dp0run_ui_tests.py" %*
if errorlevel 1 (
    echo.
    echo UI tests finished with failures. Check the HTML report in ZDHtest\reports.
)
