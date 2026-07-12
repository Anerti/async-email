#!/bin/sh
JAVA_HOME=${JAVA_HOME:-$HOME/.jdks/ms-21.0.11}
find src -name "*.java" -print0 | xargs -0 -n 500 $JAVA_HOME/bin/java -jar google-java-format-1.25.2-all-deps.jar --replace
