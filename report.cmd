@echo off
rem Runs all tests, builds the Allure report and opens it in the browser.
rem Usage (from the project folder): report
cd /d "%~dp0"

rem Keep going when a test fails, so the report still shows the failure
call mvn clean test -Dmaven.test.failure.ignore=true
if errorlevel 1 exit /b 1

call mvn allure:report
if errorlevel 1 exit /b 1

start "" "target\site\allure-maven-plugin\index.html"
