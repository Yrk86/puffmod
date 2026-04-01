#!/bin/bash
set -euo pipefail

# Force Java 1.8 if available
if [ -z "${JAVA_HOME:-}" ]; then
  JAVA_HOME=$(/usr/libexec/java_home -v 1.8 2>/dev/null || true)
  if [ -n "$JAVA_HOME" ]; then
    export JAVA_HOME
  fi
fi

rm -rf build
./gradlew clean runClient --no-daemon
