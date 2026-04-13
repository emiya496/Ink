@echo off
setlocal

python "%~dp0run_api_tests.py" %*
if errorlevel 1 (
    echo.
    echo API tests finished with failures. Check the HTML report in ZDHtest\reports.
)
