@echo off
REM =====================================================
REM  SAS — Quick Build and Run Script (no interactive prompt)
REM =====================================================
echo.
echo [1/2] Compiling all Java sources...
if not exist out mkdir out
dir /s /b src\*.java > _sources.txt 2>&1
javac -encoding UTF-8 -cp "lib\mysql-connector-j-8.4.0.jar" -d "out" @_sources.txt 2>&1
del _sources.txt
if errorlevel 1 (
    echo.
    echo ❌ Compilation FAILED — check errors above.
    pause
    exit /b 1
)
echo ✅ Compilation successful.
echo.
echo [2/2] Launching Student Attendance System...
java -cp "out;lib\mysql-connector-j-8.4.0.jar" com.attendance.Main
