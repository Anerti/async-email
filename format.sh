#!/bin/sh

# Use JDK 21 explicitly – the google-java-format jar relies on internal
# com.sun.tools.javac APIs that were removed in JDK 26+.
JAVA=${JAVA_HOME:+$JAVA_HOME/bin/java}
JAVA=${JAVA:-java}

find src -name "*.java" -print0 | xargs -0 -n 500 "$JAVA" -jar google-java-format-1.23.0-all-deps.jar --replace
