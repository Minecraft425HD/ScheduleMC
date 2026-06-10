#!/usr/bin/env bash
# I18N-Guard: schlägt fehl, wenn neue deutsche String-Literale in src/main/java auftauchen.
# Deutsche Code-Kommentare sind erlaubt. Siehe docs/I18N_MIGRATION_PLAN.md.
cd "$(dirname "$0")/.."
exec python3 - <<'PYEOF'
import re, os, sys
gword = re.compile(r'[äöüÄÖÜß]|\b(nicht|wurde|werden|fuer|beim|wird|keine?|bereits|erfolgreich|verfuegbar|gefunden|geladen|gespeichert)\b')
allow = re.compile(r'äöüÄÖÜß')  # Validierungs-Regexe, die Umlaute erlauben
slit = re.compile(r'"((?:[^"\\\n]|\\.){3,})"')
hits = []
for root, _, fs in os.walk('src/main/java'):
    for f in fs:
        if not f.endswith('.java'): continue
        p = os.path.join(root, f)
        s = re.sub(r'//[^\n]*|/\*.*?\*/', ' ', open(p, encoding='utf-8').read(), flags=re.DOTALL)
        for m in slit.findall(s):
            if gword.search(m) and not allow.search(m[:0] if 'a-zA-Z' not in m else m):
                if 'a-zA-Z' in m: continue  # Regex-Literale
                hits.append((p, m))
if hits:
    print(f"FEHLER: {len(hits)} deutsche String-Literale gefunden (Sprachkonvention: alles Englisch):")
    for p, m in hits[:20]:
        print(f"  {p}: \"{m[:80]}\"")
    sys.exit(1)
print("OK: keine deutschen String-Literale in src/main/java.")
PYEOF
