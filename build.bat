@echo off
REM cspell:ignore javac errorlevel
REM =========================================================
REM  Student Attendance System (SAS) - Build & Run Script
REM =========================================================

echo ==============================================
echo   Student Attendance System (SAS) Builder
echo ==============================================

set SRC_DIR=src
set OUT_DIR=out
set LIB_DIR=lib
set JAR_FILE=SAS.jar
set MAIN_CLASS=com.attendance.Main
set MYSQL_JAR=%LIB_DIR%\mysql-connector-j-8.4.0.jar

REM Clean
if exist %OUT_DIR% rmdir /S /Q %OUT_DIR%
mkdir %OUT_DIR%

REM Find and compile
echo [1/3] Compiling source files...
dir /s /b %SRC_DIR%\*.java > sources.txt
rem ensure correct encoding when source files contain unicode characters
rem compile without external connector on classpath (connector only needed at runtime)
javac -encoding UTF-8 -d "%OUT_DIR%" @sources.txt
del sources.txt
if errorlevel 1 (echo Compilation FAILED. & pause & exit /b 1)
echo       Compilation successful.

REM Create JAR
echo [2/3] Creating executable JAR...
(
echo Main-Class: %MAIN_CLASS%
echo Class-Path: %MYSQL_JAR:\=/%
echo.
) > manifest.mf

REM attempt to locate jar tool
set JAR_CMD=jar
if not exist "%JAR_CMD%" (
    if defined JAVA_HOME (
        set JAR_CMD=%JAVA_HOME%\bin\jar
    )
)
if exist "%JAR_CMD%" (
    "%JAR_CMD%" cfm %JAR_FILE% manifest.mf -C %OUT_DIR% .
    if errorlevel 1 (
        echo Failed to create jar using %JAR_CMD%.
    ) else (
        echo       Created %JAR_FILE%
    )
) else (
    echo WARNING: jar tool not found in PATH or JAVA_HOME; skipping jar creation.
    echo You can run the app with: java -cp "%OUT_DIR%;%MYSQL_JAR%" %MAIN_CLASS%
)

del manifest.mf

echo.
echo ==============================================
echo   Build complete!
echo   Run with: java -jar SAS.jar
echo   Or: java -cp "SAS.jar;%MYSQL_JAR%" %MAIN_CLASS%
echo ==============================================
echo.

set /p ANSWER=Run the application now? (y/n): 
if /i "%ANSWER%"=="y" (
    java -cp "SAS.jar;%MYSQL_JAR%" %MAIN_CLASS%
)
