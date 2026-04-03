#!/usr/bin/env bash
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
export SAS_DB_URL="jdbc:sqlite:$DIR/absents.db?foreign_keys=on"
export SAS_DB_DRIVER="org.sqlite.JDBC"
# ensure lib/sqlite-jdbc.jar exists
if [ ! -f "$DIR/lib/sqlite-jdbc.jar" ]; then
  echo "sqlite-jdbc.jar not found in lib/. Downloading..."
  (cd "$DIR/lib" && wget -q -O sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.40.1.0/sqlite-jdbc-3.40.1.0.jar)
fi
java -cp "$DIR/SAS.jar:$DIR/lib/sqlite-jdbc.jar" com.attendance.Main
