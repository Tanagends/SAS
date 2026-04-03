Set-Location "\\wsl.localhost\Ubuntu\root\practice\jav\project\SAS"
$env:SAS_DB_URL = "jdbc:sqlite:\\wsl.localhost\Ubuntu\root\practice\jav\project\SAS\absents.db?foreign_keys=on"
$env:SAS_DB_DRIVER = "org.sqlite.JDBC"
# Ensure sqlite-jdbc exists in lib
if (-Not (Test-Path lib\sqlite-jdbc.jar)) {
    Write-Output "Downloading sqlite-jdbc.jar to lib..."
    wsl -d Ubuntu -- bash -lc "cd /root/practice/jav/project/SAS/lib; wget -q -O sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.40.1.0/sqlite-jdbc-3.40.1.0.jar"
}
# Run using WSL java classpath (works cross-platform)
wsl -d Ubuntu -- bash -lc "cd /root/practice/jav/project/SAS; java -cp 'SAS.jar:lib/sqlite-jdbc.jar' com.attendance.Main" | Out-Default
