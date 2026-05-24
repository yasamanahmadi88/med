$env:JAVA_HOME="C:\Program Files\Java\jdk-11.0.8"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
java -version
mvn -version

mvn clean package -Pprod -DskipTests -Dfile.encoding=UTF-8