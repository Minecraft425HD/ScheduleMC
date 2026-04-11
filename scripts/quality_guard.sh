#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

BASELINE_FILE="scripts/quality_baseline.env"
if [[ ! -f "$BASELINE_FILE" ]]; then
  echo "[quality-guard] FEHLER: Baseline-Datei fehlt: $BASELINE_FILE"
  exit 1
fi

# shellcheck source=/dev/null
source "$BASELINE_FILE"

EMPTY_CATCH_PATTERN="catch \\([^)]*\\)\\s*\\{\\s*\\}"
IGNORED_CATCH_BINDING_PATTERN="catch \\([^)]*\\s+ignored\\)"
COMMENT_ONLY_CATCH_PATTERN="catch\\s*\\([^)]*\\)\\s*\\{\\s*(?://[^\\n]*\\n\\s*)*\\}"

empty_catches=$( (rg -nUP "$EMPTY_CATCH_PATTERN" src/main/java || true) | wc -l | tr -d ' ')
ignored_catch_bindings=$( (rg -nUP "$IGNORED_CATCH_BINDING_PATTERN" src/main/java || true) | wc -l | tr -d ' ')
comment_only_catches=$( (rg -nUP "$COMMENT_ONLY_CATCH_PATTERN" src/main/java || true) | wc -l | tr -d ' ')
thread_sleep_main=$( (rg -n "Thread\\.sleep\\(" src/main/java || true) | wc -l | tr -d ' ')
new_thread_main=$( (rg -n "new Thread\\(" src/main/java || true) | wc -l | tr -d ' ')

status=0

echo "[quality-guard] Empty catches (main): $empty_catches (baseline: ${BASE_EMPTY_CATCHES})"
if (( empty_catches > BASE_EMPTY_CATCHES )); then
  echo "[quality-guard] FEHLER: Empty-catch Anzahl über Baseline!"
  rg -nUP "$EMPTY_CATCH_PATTERN" src/main/java | head -n 30 || true
  status=1
fi

echo "[quality-guard] catch(... ignored) bindings (main): $ignored_catch_bindings (baseline: ${BASE_IGNORED_CATCH_BINDINGS})"
if (( ignored_catch_bindings > BASE_IGNORED_CATCH_BINDINGS )); then
  echo "[quality-guard] FEHLER: ignored-catch Bindings über Baseline!"
  rg -nUP "$IGNORED_CATCH_BINDING_PATTERN" src/main/java | head -n 30 || true
  status=1
fi

echo "[quality-guard] comment-only catches (main): $comment_only_catches (baseline: ${BASE_COMMENT_ONLY_CATCHES})"
if (( comment_only_catches > BASE_COMMENT_ONLY_CATCHES )); then
  echo "[quality-guard] FEHLER: comment-only catch Handler über Baseline!"
  rg -nUP "$COMMENT_ONLY_CATCH_PATTERN" src/main/java | head -n 30 || true
  status=1
fi

echo "[quality-guard] Thread.sleep in main: $thread_sleep_main (baseline: ${BASE_THREAD_SLEEP_MAIN})"
if (( thread_sleep_main > BASE_THREAD_SLEEP_MAIN )); then
  echo "[quality-guard] FEHLER: Thread.sleep Nutzung in main über Baseline!"
  rg -n "Thread\\.sleep\\(" src/main/java || true
  status=1
fi

echo "[quality-guard] new Thread in main: $new_thread_main (baseline: ${BASE_NEW_THREAD_MAIN})"
if (( new_thread_main > BASE_NEW_THREAD_MAIN )); then
  echo "[quality-guard] FEHLER: new Thread Nutzung in main über Baseline!"
  rg -n "new Thread\\(" src/main/java || true
  status=1
fi

if (( status != 0 )); then
  echo "[quality-guard] Ergebnis: FEHLER"
  exit 1
fi

echo "[quality-guard] Ergebnis: OK"
