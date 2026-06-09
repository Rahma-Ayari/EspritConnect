@echo off
setlocal
cd /d "%~dp0"
call .mvn\ensure-java.cmd
if errorlevel 1 exit /b 1
call mvnw.cmd %*
exit /b %ERRORLEVEL%
