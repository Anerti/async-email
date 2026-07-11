#!/bin/sh
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
find src -name "*.java" -print0 | xargs -0 -n 500 "$JAVA" -jar google-java-format-1.23.0-all-deps.jar --replace
