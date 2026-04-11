#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

STRICT_TOOLCHAIN="${STRICT_TOOLCHAIN:-0}"
for arg in "$@"; do
  case "$arg" in
    --strict-toolchain) STRICT_TOOLCHAIN=1 ;;
    --help|-h)
      echo "Usage: bash scripts/local_quality_pipeline.sh [--strict-toolchain]"
      echo "  --strict-toolchain   fail fast if Java 17 preflight is not satisfied"
      exit 0
      ;;
    *)
      echo "[pipeline] FEHLER: Unbekanntes Argument: $arg"
      echo "Usage: bash scripts/local_quality_pipeline.sh [--strict-toolchain]"
      exit 64
      ;;
  esac
done

echo "[pipeline] 1/4 Toolchain preflight"
if ! bash scripts/toolchain_check.sh; then
  if [[ "$STRICT_TOOLCHAIN" == "1" ]]; then
    echo "[pipeline] FEHLER: Toolchain preflight fehlgeschlagen und STRICT_TOOLCHAIN=1 gesetzt."
    exit 2
  fi
  echo "[pipeline] WARNUNG: Toolchain nicht ideal (Java 17 fehlt) - fahre mit statischen Checks fort."
fi

echo "[pipeline] 2/4 Repo-Hygiene"
bash scripts/repo_hygiene_check.sh

echo "[pipeline] 3/4 Quality-Guard"
bash scripts/quality_guard.sh

echo "[pipeline] 4/4 Metrics regenerieren"
bash scripts/generate_repo_metrics.sh

echo "[pipeline] DONE"
