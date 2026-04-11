#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

JAVA_BIN="${JAVA_HOME:-}/bin/java"
if [[ ! -x "$JAVA_BIN" ]]; then
  JAVA_BIN="$(command -v java || true)"
fi

if [[ -z "$JAVA_BIN" ]]; then
  echo "[toolchain] FEHLER: Kein java im PATH und JAVA_HOME ist nicht gesetzt."
  exit 2
fi

JAVA_VERSION_RAW="$($JAVA_BIN -version 2>&1 | head -n1)"
JAVA_MAJOR="$(echo "$JAVA_VERSION_RAW" | sed -E 's/.*version "([0-9]+).*/\1/' || true)"

GRADLE_VERSION="$(./gradlew -v 2>/dev/null | awk '/Gradle /{print $2; exit}' || true)"

echo "[toolchain] Java binary: $JAVA_BIN"
echo "[toolchain] Java version: $JAVA_VERSION_RAW"
if [[ -n "$GRADLE_VERSION" ]]; then
  echo "[toolchain] Gradle version: $GRADLE_VERSION"
else
  echo "[toolchain] Gradle version: (konnte nicht ermittelt werden)"
fi

if [[ "$JAVA_MAJOR" != "17" ]]; then
  echo "[toolchain] WARNUNG: Projekt erwartet Java 17 für stabile Gradle/Test-Ausführung."
  echo "[toolchain] Hinweis: nutze 'bash scripts/run_gradle_tests.sh' oder setze JAVA17_HOME/JDK17_HOME."
  exit 1
fi

echo "[toolchain] OK: Java 17 aktiv."
