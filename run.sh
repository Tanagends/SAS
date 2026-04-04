#!/usr/bin/env bash
set -e
# Unified run script: prefers runnable jar if present, otherwise uses compiled classes
# Defaults to SQLite for easy local demos. Use environment variables to override.

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

# Defaults
: ${SAS_DB_URL:="jdbc:sqlite:$ROOT_DIR/absents.db?foreign_keys=on"}
: ${SAS_DB_DRIVER:="org.sqlite.JDBC"}
: ${SAS_DB_USER:="java_admin"}
: ${SAS_DB_PASS:="admin"}

echo "Using SAS_DB_URL=$SAS_DB_URL"

# Ensure sqlite driver exists when using sqlite URL
if [[ "$SAS_DB_URL" == jdbc:sqlite:* ]]; then
  if [ ! -f "$ROOT_DIR/lib/sqlite-jdbc.jar" ]; then
    echo "sqlite-jdbc.jar missing, downloading..."
    (cd "$ROOT_DIR/lib" && wget -q -O sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.40.1.0/sqlite-jdbc-3.40.1.0.jar)
  fi
fi

if [ -f "$ROOT_DIR/SAS.jar" ]; then
  echo "Found SAS.jar — running main class with classpath including lib jars"
  # Build classpath with SAS.jar + all jars in lib/
  CLASSPATH="$ROOT_DIR/SAS.jar"
  for j in "$ROOT_DIR"/lib/*.jar; do CLASSPATH="$CLASSPATH:$j"; done
  exec java -cp "$CLASSPATH" com.attendance.Main
else
  echo "SAS.jar not found — running compiled classes from bin"
  # Ensure bin exists and is populated
  if [ ! -d "$ROOT_DIR/bin" ]; then
    echo "Compiling sources into bin..."
    mkdir -p "$ROOT_DIR/bin"
    find src -name '*.java' -print0 | xargs -0 javac -cp 'lib/*' -d bin
  fi
  CLASSPATH="bin"
  for j in "$ROOT_DIR"/lib/*.jar; do CLASSPATH="$CLASSPATH:$j"; done
  exec java -cp "$CLASSPATH" com.attendance.Main
fi
