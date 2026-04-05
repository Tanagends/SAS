#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

echo "=============================================="
echo "   SAS Standalone Builder (fat jar)"
echo "=============================================="

echo "Java version: $(javac -version 2>&1)"

mkdir -p bin

echo "Compiling sources..."
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
rm -f sources.txt

echo "Preparing manifest..."
cat > manifest-standalone.mf <<EOF
Main-Class: com.attendance.Main
EOF

# Create base jar from compiled classes
rm -f SAS-standalone.jar
jar cfm SAS-standalone.jar manifest-standalone.mf -C bin .

# Merge all dependency jars from lib/
if compgen -G "lib/*.jar" > /dev/null; then
  echo "Merging lib/*.jar into SAS-standalone.jar ..."
  TMP_DIR="build_tmp_standalone"
  rm -rf "$TMP_DIR"
  mkdir -p "$TMP_DIR"
  (cd "$TMP_DIR" && jar xf ../SAS-standalone.jar)
  for j in "$ROOT_DIR"/lib/*.jar; do
    echo "  + $(basename "$j")"
    (cd "$TMP_DIR" && jar xf "$j")
  done
  (cd "$TMP_DIR" && jar cfm ../SAS-standalone.jar ../manifest-standalone.mf .)
  rm -rf "$TMP_DIR"
fi

rm -f manifest-standalone.mf

echo "✅ Built: SAS-standalone.jar"
echo "Run: java -jar SAS-standalone.jar"
