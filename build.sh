#!/bin/bash
# Build script for Student Attendance System (SAS)
# Requirements: JDK 11+
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

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

# Create output dirs
mkdir -p bin

# Compile all source files
echo ""
echo "Compiling source files..."
find src -name "*.java" > sources.txt

# Compile (no external libs required at compile time for JDBC usage)
javac -encoding UTF-8 -d bin @sources.txt
if [ $? -ne 0 ]; then
    echo "❌ Compilation failed. Check javac_err.log (if any) / console output."
    rm -f sources.txt
    exit 1
fi

echo "✅ Compilation successful!"

# Create manifest
cat > manifest.txt <<EOF
Main-Class: com.attendance.Main
EOF

# -----------------------------------------------------------------------------
# DEMO FIX: make `java -jar SAS.jar` WORK
# We package a "fat jar" that includes sqlite-jdbc so the app runs out-of-box.
# (SAS-standalone.jar already exists, but developers keep running SAS.jar.)
# -----------------------------------------------------------------------------

# Ensure libs exist
mkdir -p lib
if [ ! -f "lib/sqlite-jdbc.jar" ]; then
  echo "sqlite-jdbc.jar missing in lib/." 
  echo "Run ./run.sh once (it auto-downloads) OR place org.xerial sqlite-jdbc jar at lib/sqlite-jdbc.jar"
fi

# Package into JAR
rm -f SAS.jar
jar cfm SAS.jar manifest.txt -C bin .

# If sqlite-jdbc jar exists, merge it into SAS.jar (fat jar)
if [ -f "lib/sqlite-jdbc.jar" ]; then
  echo "Merging sqlite-jdbc into SAS.jar..."
  TMP_DIR="build_tmp_fat"
  rm -rf "$TMP_DIR"
  mkdir -p "$TMP_DIR"
  (cd "$TMP_DIR" && jar xf ../SAS.jar)
  (cd "$TMP_DIR" && jar xf ../lib/sqlite-jdbc.jar)
  # Recreate jar with merged contents
  (cd "$TMP_DIR" && jar cfm ../SAS.jar ../manifest.txt .)
  rm -rf "$TMP_DIR"
fi

# Optional: merge MySQL connector if present (so switching DB just works)
if [ -f "lib/mysql-connector-j-8.4.0.jar" ]; then
  echo "Merging mysql-connector into SAS.jar..."
  TMP_DIR="build_tmp_fat"
  rm -rf "$TMP_DIR"
  mkdir -p "$TMP_DIR"
  (cd "$TMP_DIR" && jar xf ../SAS.jar)
  (cd "$TMP_DIR" && jar xf ../lib/mysql-connector-j-8.4.0.jar)
  (cd "$TMP_DIR" && jar cfm ../SAS.jar ../manifest.txt .)
  rm -rf "$TMP_DIR"
fi

echo "✅ JAR created: SAS.jar"
echo "To run: java -jar SAS.jar"

rm -f sources.txt manifest.txt
