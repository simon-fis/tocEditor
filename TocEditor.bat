@echo off
echo %*
set TEMP=C:\some\path\tocEditor\
set JAR=%TEMP%TocEditor.jar
set PROC=%TEMP%home\pdftopng.exe
set WORK=%TEMP%work
set FORMAT=%TEMP%home\defaultFormat.json
set TOC=
set OUT=

call java -jar "%JAR%" "%PROC%" "%WORK%" "%FORMAT%" %1
