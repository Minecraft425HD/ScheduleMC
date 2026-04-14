# Hardcoded String Scan (2026-04-14)

Requested follow-up: rescan the whole codebase for hardcoded strings, German text, and currency symbols.

## Commands used

```bash
rg --pcre2 -n '"[^\"]*(€|\$)[^\"]*"' src/main/java
rg --pcre2 -n '"[^\"]*(\b(und|oder|nicht|mit|für|ohne|zum|zur|der|die|das|ist|sind|neu|verkauft|umsatz|freigeschaltet|benötigt|über|größe|lösungsmittel|kategorie|zurück|übersicht|täglich|wöchentlich)\b|[äöüÄÖÜß])[^\"]*"' src/main/java src/main/resources/assets/schedulemc/lang/en_us.json
python - <<'PY'
import json,re
p='src/main/resources/assets/schedulemc/lang/en_us.json'
d=json.load(open(p))
bad=[(k,v) for k,v in d.items() if k.startswith('gui.app.level') and re.search(r'(Mohn|Kokain|Käse|Schokolade|Wein|Tabak|Honig|Freigeschaltet|benötigt|[äöüÄÖÜß])', str(v))]
print('bad',len(bad))
PY
```

## Immediate fixes applied in this pass

- Level package hardcoded German status messages translated to English:
  - `ProducerLevel` level-up + unlock messages.
  - `LevelUpNotificationPacket` title/message.
  - `XPSource` German source labels.
  - `LevelRequirements` “next level” info line.
  - `ProducerLevelData` summary lines switched to English.
- Currency formatting in `ProducerLevelAppScreen` externalized into translation key (`gui.app.level.revenue_value_pattern`) to remove inline symbol formatting in Java.

## Scan outcome (high-level)

- **Level app / level package:** major German hardcoded strings from previous feedback are now removed in touched files.
- **Codebase-wide:** there are still many hardcoded German user-facing strings in unrelated systems (for example `secretdoors/*`, many config comments/messages, etc.).
- **Recommendation:** migrate remaining user-facing literals to translation keys module-by-module (start with `secretdoors`, then configs/help text).

