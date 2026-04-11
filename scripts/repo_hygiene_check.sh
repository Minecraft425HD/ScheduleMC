#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

status=0

echo "[repo-hygiene] Prüfe SecretDoors-Kerndateien (kanonischer Pfad) ..."
required=(
  "src/main/java/de/rolandsw/schedulemc/secretdoors/blocks/AbstractSecretDoorBlock.java"
  "src/main/java/de/rolandsw/schedulemc/secretdoors/blocks/HiddenSwitchBlock.java"
)
for f in "${required[@]}"; do
  if [[ ! -f "$f" ]]; then
    echo "[repo-hygiene] FEHLER: Pflichtdatei fehlt: $f"
    status=1
  fi
done
if [[ $status -eq 0 ]]; then
  echo "[repo-hygiene] OK: SecretDoors-Kerndateien sind vorhanden."
fi

echo "[repo-hygiene] Prüfe auf fehlplatzierte Root-Quelltexte ..."
root_secret_doors=(
  "AbstractSecretDoorBlock.java"
  "HiddenSwitchBlock.java"
)
for f in "${root_secret_doors[@]}"; do
  if [[ -f "$f" ]]; then
    echo "[repo-hygiene] FEHLER: Fehlplatzierte Root-Datei gefunden: $f"
    echo "[repo-hygiene]        Erwarteter Ort: src/main/java/de/rolandsw/schedulemc/secretdoors/blocks/"
    status=1
  fi
done
if [[ $status -eq 0 ]]; then
  echo "[repo-hygiene] OK: Keine fehlplatzierten SecretDoors-Dateien im Repo-Root."
fi

echo "[repo-hygiene] Prüfe temporäre Backup-/Temp-Dateien ..."
mapfile -t temp_lang < <(find src/main/resources/assets/schedulemc/lang -type f \( -name '*.tmp' -o -name '*.FULL_BACKUP' \) -print | sort)
if ((${#temp_lang[@]} > 0)); then
  echo "[repo-hygiene] FEHLER: Sprach-Temp/Backup-Dateien gefunden:"
  printf '  - %s\n' "${temp_lang[@]}"
  status=1
else
  echo "[repo-hygiene] OK: Keine Sprach-Temp/Backup-Dateien gefunden."
fi

if [[ $status -ne 0 ]]; then
  echo "[repo-hygiene] Ergebnis: FEHLER"
  exit 1
fi

echo "[repo-hygiene] Ergebnis: OK"
