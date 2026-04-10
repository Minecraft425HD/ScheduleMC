#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

main_files=$(rg --files src/main/java | wc -l | tr -d ' ')
test_files=$(rg --files src/test/java | wc -l | tr -d ' ')
loc_total=$(wc -l $(rg --files src/main/java src/test/java | tr '\n' ' ') | tail -n 1 | awk '{print $1}')

largest_file_line=$(python - <<'PY'
import glob
best=(0,'')
for f in glob.glob('src/main/java/**/*.java', recursive=True):
    try:
        with open(f, encoding='utf-8') as fh:
            n=sum(1 for _ in fh)
    except Exception:
        continue
    if n>best[0]:
        best=(n,f)
print(f"{best[1]}|{best[0]}")
PY
)
largest_file=${largest_file_line%%|*}
largest_loc=${largest_file_line##*|}

cat > docs/REPO_METRICS.md <<EOM
# Repository Metrics (Auto-generated)

Generated at: $(date -u +"%Y-%m-%d %H:%M:%S UTC")

- Main Java files: ${main_files}
- Test Java files: ${test_files}
- LOC (src/main/java + src/test/java): ${loc_total}
- Largest main Java file: ${largest_file} (~${largest_loc} LOC)

> Source: scripts/generate_repo_metrics.sh
EOM

echo "[metrics] docs/REPO_METRICS.md aktualisiert"
