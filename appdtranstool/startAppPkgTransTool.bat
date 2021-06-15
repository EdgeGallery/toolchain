@echo off

echo Welcom to application package transform tool. The default URL for the home page is http://127.0.0.1:8765

echo You can change the server port number 8765 to other port.

set port=8765

START java -Dserver.port=%port% -jar appPkgTransTool.jar

pause