#!/usr/bin/env bash
# I18N-Guard: schlägt fehl, wenn neue deutsche String-Literale in src/main/java auftauchen.
# Deutsche Code-Kommentare sind erlaubt (werden hier nicht geprüft, da nur String-Inhalte matchen).
set -e
cd "$(dirname "$0")/.."
HITS=$(grep -rhoP '"[^"]*(?:\b(?:nicht|wurde|werden|fuer|und|oder|beim|wird|kein|keine|bereits|erfolgreich)\b|[äöüÄÖÜß])[^"]*"' \
    src/main/java --include="*.java" 2>/dev/null \
  | grep -vP '^"[\W\d_§¶©®×✓✔✗✘Øμ\\]+"$' \
  | grep -vF 'äöüÄÖÜßéèêëàâáãåçñ' \
  | grep -P '[äöüÄÖÜß]|\b(nicht|wurde|fuer|und|oder|kein|wird)\b' \
  | grep -vP '"[^"]*\{\}[^"]*"$' || true)
COUNT=$(echo "$HITS" | grep -c . || true)
if [ "$COUNT" -gt 0 ]; then
  echo "FEHLER: $COUNT deutsche String-Literale gefunden (Sprachkonvention: alles Englisch):"
  echo "$HITS" | head -20
  exit 1
fi
echo "OK: keine deutschen String-Literale in src/main/java."
