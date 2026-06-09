@echo off
setlocal EnableDelayedExpansion

REM Skip if JAVA_HOME already points to a JDK (javac present).
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\javac.exe" exit /b 0
)

REM Prefer JDK 17 from IntelliJ / Cursor .jdks folder.
if exist "%USERPROFILE%\.jdks\jbr-17.0.14\bin\javac.exe" (
  set "JAVA_HOME=%USERPROFILE%\.jdks\jbr-17.0.14"
  goto :apply
)

REM Any other JDK under .jdks (newest folders often sort last — scan all).
for /d %%J in ("%USERPROFILE%\.jdks\*") do (
  if exist "%%~J\bin\javac.exe" (
    set "JAVA_HOME=%%~J"
    goto :apply
  )
)

REM JetBrains IDE bundled JBR.
for /d %%I in ("C:\Program Files\JetBrains\*") do (
  if exist "%%~I\jbr\bin\javac.exe" (
    set "JAVA_HOME=%%~I\jbr"
    goto :apply
  )
)

REM Standard Oracle / OpenJDK installs.
for /d %%J in ("C:\Program Files\Java\jdk-*" "C:\Program Files\Eclipse Adoptium\jdk-*" "C:\Program Files\Microsoft\jdk-*") do (
  if exist "%%~J\bin\javac.exe" (
    set "JAVA_HOME=%%~J"
    goto :apply
  )
)

echo [EspritConnect] ERROR: No JDK found. Install JDK 17+ or set JAVA_HOME to a JDK path. >&2
echo [EspritConnect] Example: set JAVA_HOME=%USERPROFILE%\.jdks\jbr-17.0.14 >&2
exit /b 1

:apply
set "PATH=%JAVA_HOME%\bin;%PATH%"
endlocal & set "JAVA_HOME=%JAVA_HOME%" & set "PATH=%PATH%"
exit /b 0
