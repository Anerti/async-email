#!/bin/sh
: "${JAVA_HOME:=$(dirname "$(dirname "$(readlink -f "$(which java)"))")}"
find src -name "*.java" -print0 | xargs -0 -n 500 "$JAVA_HOME/bin/java" -jar google-java-format-1.23.0-all-deps.jar --replace
