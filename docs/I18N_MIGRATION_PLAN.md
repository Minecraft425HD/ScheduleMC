# I18N-Migrationsplan: Verbleibende hartkodierte deutsche Strings

**Stand:** 2026-06-10 · **Gesamt: ~709 Strings** in `src/main/java`
**Architekturentscheidung (CLAUDE.md):** Alles im Code ist Englisch. Deutsche
Texte gehören ausschließlich als Werte in `de_de.json`.

## Ist-Zustand (gemessen)

| Kategorie | Anzahl | Strategie |
|---|---|---|
| Sonstige Strings (Dialoge, Shop-/Szenario-Texte, Messages in Helpern) | 458 | → Lang-Keys |
| Config-Kommentare (`builder.comment(...)`) | 106 | → Inline auf Englisch übersetzen (Forge-Configs sind nicht lokalisierbar) |
| Logger-Meldungen | 79 | → Inline auf Englisch übersetzen (Logs werden nie lokalisiert) |
| `Component.literal("deutsch")` | 66 | → `Component.translatable` + Lang-Keys |

| Modul | Strings | Priorität |
|---|---|---|
| npc | 261 | P1 (Dialoge, Quests, Polizei/Gefängnis — meiste Spielersichtbarkeit) |
| config | 108 | P2 (fast nur Kommentare → inline Englisch, kein Lang-Key nötig) |
| gang | 60 | P3 |
| secretdoors | 45 | P3 (in docs/HARDCODED_STRING_SCAN bereits als Start empfohlen — klein und abgeschlossen, guter Pilot) |
| mission | 41 | P3 |
| util + economy | 60 | P2 (zentrale Helper, viele Wiederverwender) |
| weapon, lock, vehicle | 60 | P4 |
| Rest (tobacco, region, warehouse, commands, …) | ~75 | P5 |

## Konventionen

- **Key-Schema:** `<bereich>.<modul>.<kontext>_<aussage>` analog zu Bestand
  (`message.…`, `gui.…`, `command.…`, `event.…`, `tooltip.…`, `validation.…`).
- **Platzhalter:** `%s` (Reihenfolge = Argumentreihenfolge im Code); Beträge
  vorformatiert mit `String.format("%.2f", x)` übergeben — wie Bestand.
- **Farbcodes (`§a` …):** nicht in den Lang-Wert, sondern `withStyle(...)` im
  Code; wo gemischt, Farbcode im Wert dulden (Bestandsmuster).
- **Jeder Key** bekommt **beide** Einträge: `en_us.json` (englisch) und
  `de_de.json` (bisheriger deutscher Text — verlustfrei umziehen).
- **Logger/Config-Kommentare:** niemals Lang-Keys; direkt englisch.
- Deutsche **Code-Kommentare** bleiben erlaubt (CLAUDE.md).

## Phasenplan (jede Phase = 1 Branch/PR, kompilierbar, einzeln mergebar)

1. **Pilot `secretdoors` (45)** — klein, isoliert; Muster etablieren:
   Literal → Key, beide Lang-Dateien, Audit-Skript laufen lassen.
2. **`config` (106 Kommentare + ~2 Strings)** — reine Inline-Übersetzung
   nach Englisch, kein Lang-Aufwand. Schnellster großer Brocken.
3. **Logger-Sweep quer (79)** — alle `LOGGER.*("deutsch…")` inline englisch.
   Mechanisch, modulübergreifend in einem PR vertretbar.
4. **`util` + `economy` (60)** — zentrale Helfer (z. B. Fehlertexte), dann
   profitieren alle Module.
5. **`npc` (261) in 3 Teil-PRs:** a) Polizei/Gefängnis/Crime,
   b) Dialoge/Quests/Trading, c) Screens/Sonstiges.
6. **`gang` (60) + `mission` (41)** — Szenario-/Objective-Texte.
7. **`weapon`/`lock`/`vehicle` (60)**, dann **Rest (~75)**.
8. **Abschluss:** Scan-Regex aus diesem Plan als Check in `run-pmd.sh` o. ä.
   aufnehmen, damit keine neuen deutschen Literale einsickern;
   CLAUDE.md-Abschnitt „Noch offen" entfernen.

## Audit nach jeder Phase

```bash
# deutsche Literale zählen (Ziel: fällt monoton, am Ende 0)
grep -rhoP '"[^"]*(?:[äöüÄÖÜß]|\b(?:nicht|wurde|werden|kann|muss|für|fuer|und|oder|beim|wird|kein|keine|bereits|erfolgreich)\b)[^"]*"' \
  src/main/java --include="*.java" | sort -u | wc -l
# referenzierte Lang-Keys vollständig? (Skript aus Session 2026-06-10:
# alle "x.y.z"-Literale gegen en_us.json prüfen — 0 fehlende erwartet)
```

**Definition of Done:** Audit-Zähler = 0, `compileJava` grün, alle neuen Keys
in beiden Lang-Dateien, kein `Component.literal` mit deutschem Text mehr.

## Fortschritt

- [x] **Phase 1** secretdoors (40 Strings → `message.secret_door.*`) — 2026-06-10
- [x] **Phase 2** config (113 Kommentare inline Englisch) — 2026-06-10
- [x] **Phase 3** Logger-Sweep (79 Strings, 23 Dateien) — 2026-06-10
- [x] **Phase 4** util + economy (inline Englisch; Hinweis: Transaktions-
  Beschreibungen und Markt-Event-Namen werden als Strings gespeichert/
  gematcht — echte Lang-Key-Externalisierung erfordert Storage-Refactor.
  Event-Namen-Matching in EconomyController konsistent mitübersetzt.)
- [ ] **Phase 5** npc (261, in 3 Teil-PRs)
- [ ] **Phase 6** gang + mission
- [ ] **Phase 7** weapon/lock/vehicle + Rest
- [ ] **Phase 8** Guard gegen neue deutsche Literale
