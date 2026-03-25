#!/bin/bash
# Build script for Student Attendance System (SAS)
# Requirements: JDK 11+, MySQL 8.x running

echo "=============================================="
echo "   Student Attendance System - Build Script"
echo "=============================================="

# Check for Java compiler
if ! command -v javac &> /dev/null; then
    echo "ERROR: javac not found. Please install JDK 11 or later."
    echo "  Ubuntu/Debian: sudo apt install default-jdk"
    echo "  Windows: Install from https://adoptium.net"
    exit 1
fi

echo "Java version: $(javac -version 2>&1)"

# Create output dirs
mkdir -p bin

# Compile all source files
echo ""
echo "Compiling source files..."
find src -name "*.java" > sources.txt
# compile with UTF-8 encoding to support emoji and other unicode
# compile without external connector on classpath; connector required only at runtime
javac -encoding UTF-8 -d bin @sources.txt

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    # Create manifest
    echo "Main-Class: com.attendance.Main" > manifest.txt
    echo "Class-Path: lib/mysql-connector-j-8.4.0.jar" >> manifest.txt
    echo "" >> manifest.txt
    # Package into JAR
    jar cfm SAS.jar manifest.txt -C bin .
    echo "✅ JAR created: SAS.jar"
    echo ""
    echo "To run: java -jar SAS.jar"
else
    echo "❌ Compilation failed. Check errors above."
fi

rm -f sources.txt manifest.txt
