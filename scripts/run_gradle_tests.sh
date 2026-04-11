#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

CHECK_ONLY=0
if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  echo "Usage: bash scripts/run_gradle_tests.sh [--check-only] [gradle args...]"
  echo "  --check-only   only validate Java 17 detection and print selected JAVA_HOME"
  exit 0
fi
if [[ "${1:-}" == "--check-only" ]]; then
  CHECK_ONLY=1
  shift
fi
if [[ "$CHECK_ONLY" == "1" && $# -gt 0 ]]; then
  echo "[gradle-test] FEHLER: --check-only akzeptiert keine zusätzlichen Gradle-Argumente."
  echo "[gradle-test] Nutze entweder '--check-only' oder reguliere Gradle-Tasks/Flags."
  exit 64
fi

find_java17_home() {
  # 1) Explicit env vars first
  local candidates=(
    "${JAVA17_HOME:-}"
    "${JDK17_HOME:-}"
    "${JAVA_HOME:-}"
    "$HOME/.sdkman/candidates/java/current"
    "$HOME/.sdkman/candidates/java/17.0.*/"
    "$HOME/.asdf/installs/java/*17*"
    "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
    "/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
    "/usr/lib/jvm/java-17-openjdk"
    "/usr/lib/jvm/java-17-openjdk-amd64"
    "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
  )

  local c
  for c in "${candidates[@]}"; do
    [[ -z "$c" ]] && continue
    for resolved in $c; do
      if [[ -x "$resolved/bin/java" ]]; then
        local v
        v="$($resolved/bin/java -version 2>&1 | head -n1 || true)"
        if [[ "$v" == *"17."* || "$v" == *" 17"* ]]; then
          echo "$resolved"
          return 0
        fi
      fi
    done
  done

  # 2) macOS helper (if available)
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    local mac_java17
    mac_java17="$(/usr/libexec/java_home -v 17 2>/dev/null || true)"
    if [[ -n "$mac_java17" && -x "$mac_java17/bin/java" ]]; then
      echo "$mac_java17"
      return 0
    fi
  fi

  return 1
}

JAVA17="$(find_java17_home || true)"
if [[ -z "$JAVA17" ]]; then
  echo "[gradle-test] FEHLER: Kein Java 17 gefunden."
  echo "[gradle-test] Setze JAVA17_HOME (oder JDK17_HOME) auf ein JDK 17 und starte erneut."
  if [[ -x "$ROOT_DIR/scripts/toolchain_check.sh" ]]; then
    echo "[gradle-test] Starte optionalen Toolchain-Check für Details ..."
    bash "$ROOT_DIR/scripts/toolchain_check.sh" || true
  fi
  exit 2
fi

echo "[gradle-test] Nutze JAVA_HOME=$JAVA17"
if [[ "$CHECK_ONLY" == "1" ]]; then
  echo "[gradle-test] Check-only Modus: Gradle wird nicht gestartet."
  exit 0
fi

if [[ $# -eq 0 ]]; then
  JAVA_HOME="$JAVA17" PATH="$JAVA17/bin:$PATH" ./gradlew test -q
else
  JAVA_HOME="$JAVA17" PATH="$JAVA17/bin:$PATH" ./gradlew "$@"
fi
