#!/usr/bin/env bash
# PMD 7 Bug-Scan für ScheduleMC
# Verwendung: ./run-pmd.sh [--text] [--xml-only]
# Voraussetzung: PMD 7.x unter /tmp/pmd-bin-7.*/bin/pmd oder im PATH
#
# Erzeugt:
#   build/reports/pmd/main.xml  – maschinenlesbar
#   build/reports/pmd/main.html – menschenlesbar

set -e

PMD_BIN=$(find /tmp -name "pmd" -path "*/bin/pmd" 2>/dev/null | head -1)
if [[ -z "$PMD_BIN" ]]; then
    PMD_BIN=$(which pmd 2>/dev/null || true)
fi
if [[ -z "$PMD_BIN" ]]; then
    echo "PMD nicht gefunden. Bitte herunterladen:"
    echo "  curl -L -o /tmp/pmd.zip https://github.com/pmd/pmd/releases/download/pmd_releases%2F7.0.0/pmd-dist-7.0.0-bin.zip"
    echo "  unzip -q /tmp/pmd.zip -d /tmp"
    exit 1
fi

SRC="$(dirname "$0")/src/main/java"
RULES="$(dirname "$0")/pmd-ruleset.xml"
mkdir -p "$(dirname "$0")/build/reports/pmd"
XML_OUT="$(dirname "$0")/build/reports/pmd/main.xml"
HTML_OUT="$(dirname "$0")/build/reports/pmd/main.html"

echo "Starte PMD-Scan auf $(find "$SRC" -name '*.java' | wc -l) Dateien..."

"$PMD_BIN" check \
    --dir "$SRC" \
    --rulesets "$RULES" \
    --format xml \
    --report-file "$XML_OUT" \
    --no-fail-on-violation

"$PMD_BIN" check \
    --dir "$SRC" \
    --rulesets "$RULES" \
    --format html \
    --report-file "$HTML_OUT" \
    --no-fail-on-violation

# Zusammenfassung
TOTAL=$(grep -c "<violation" "$XML_OUT" || true)
echo "Fertig! $TOTAL Violations gefunden."
echo "  XML:  $XML_OUT"
echo "  HTML: $HTML_OUT"
echo ""
echo "Top-Regeln:"
grep 'rule=' "$XML_OUT" | sed 's/.*rule="\([^"]*\)".*/\1/' | sort | uniq -c | sort -rn | head -15
