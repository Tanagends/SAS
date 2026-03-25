#!/bin/bash
# Run the application
if [ -f "SAS.jar" ]; then
    java -jar SAS.jar
else
    echo "SAS.jar not found, running from compiled classes"
    java -cp "out:lib/mysql-connector-j-8.4.0.jar" com.attendance.Main
fi
