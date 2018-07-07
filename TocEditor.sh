#!/bin/bash

echo $*

TEMP=/home/fischesn/simon/TocEditor
JAR=${TEMP}/TocEditor.jar
PROC=${TEMP}/home/pdftopng
WORK=${TEMP}/work/
FORMAT=${TEMP}/home/defaultFormat.json

/path/to/java -jar ${JAR} ${PROC} ${WORK} ${FORMAT} ${1}
