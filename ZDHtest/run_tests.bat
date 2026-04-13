@echo off
setlocal

python "%~dp0run_tests.py" %*
if errorlevel 1 (
    echo.
    echo Tests finished with failures. Check the HTML report in ZDHtest\reports.
)
