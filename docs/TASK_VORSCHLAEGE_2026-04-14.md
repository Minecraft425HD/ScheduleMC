# Task-Vorschläge aus Codebasis-Review (2026-04-14)

## 1) Tippfehler korrigieren: `OelExtraktort` → `OelExtraktor`

**Befund:** Mehrere Klassen/Typen enthalten konsistent den Schreibfehler `OelExtraktort` (zusätzliches `t`).

**Beispiele:**
- `OelExtraktortBlock`
- `OelExtraktortBlockEntity`
- `OelExtraktortMenu`
- `OelExtraktortScreen`

**Warum relevant:**
- Schlechtere Lesbarkeit und Suchbarkeit.
- Erhöhtes Risiko für Inkonsistenzen bei künftigen Refactorings/API-Nutzung.

**Task-Vorschlag:**
- Einheitliche Umbenennung auf `OelExtraktor*` in Java-Typen, Imports, Registries und Verweisen.
- Falls Netzwerk-/Savegame-IDs davon abhängen, Migrations-/Alias-Strategie definieren.

**Akzeptanzkriterien:**
- Keine Referenz auf `OelExtraktort` im Java-Code mehr.
- Build + betroffene Registrierungen/Screens laden weiterhin korrekt.

---

## 2) Programmierfehler korrigieren: Kennzeichen-Nummern können >99 werden

**Befund:**
- Dokumentation/Kommentare beschreiben das Kennzeichenformat als `XXX-YY` mit Bereich `01-99`.
- Die Implementierung erlaubt per Overflow-Pfad Kennzeichenwerte >99 (`nextOffset = prefixMap.size() * 10`), wodurch effektiv `XXX-100+` entstehen kann.

**Warum relevant:**
- Bricht implizite Format-Invariante (`YY` als zweistellige Nummer).
- Kann UI-/Validierungslogik und externe Integrationen stören, die `YY` erwarten.

**Task-Vorschlag:**
- Entweder harte Begrenzung + definiertes Verhalten ab 99 (z. B. Fehler/Neuvergabe)
  **oder**
- Format/Kommentar und alle Verbraucher sauber auf 3+ Ziffern erweitern.
- Entscheidung als explizite Produktregel dokumentieren.

**Akzeptanzkriterien:**
- Eindeutige, getestete Regel für den Grenzfall >99.
- Konsistenz zwischen Code, Kommentar und Nutzeranzeige.

---

## 3) Kommentar-/Doku-Unstimmigkeit korrigieren: „Sliding Window“ vs. tatsächliche Logik

**Befund:**
- `RateLimiter`-Kommentar spricht von „Sliding Window Rate Limiting“.
- Die Implementierung nutzt ein festes Zeitfenster mit Reset bei Fensterwechsel (`windowStart` + `count`), kein echtes Sliding-Window.

**Warum relevant:**
- Falsche Erwartung an Fairness/Burst-Verhalten.
- Erschwert Fehlersuche und Kapazitätsplanung bei Lastspitzen.

**Task-Vorschlag:**
- Entweder Kommentar auf „fixed window“ korrigieren
  **oder**
- Implementierung tatsächlich auf Sliding-Window (z. B. token bucket / rolling window) umstellen.

**Akzeptanzkriterien:**
- Kommentar, Klassenbeschreibung und Verhalten stimmen überein.
- Dokumentierte Beispiele zeigen korrektes Verhalten an Fenstergrenzen.

---

## 4) Test-Verbesserung: Deaktivierte Minecraft-nahe Tests reaktivierbar machen

**Befund:**
- Mehrere Tests sind mit `@Disabled` markiert, weil die volle Minecraft-Initialisierung im Unit-Test-Kontext Probleme verursacht.

**Warum relevant:**
- Kritische Pfade bleiben dauerhaft ungetestet.
- Regressionen werden spät entdeckt.

**Task-Vorschlag:**
- Teststrategie trennen:
  1. Reine Unit-Tests über Adapter/Fassaden ohne harte Minecraft-Static-Initialisierung.
  2. Separate Integration/GameTest-Suite für Forge/Minecraft-nahe Pfade.
- CI so konfigurieren, dass mindestens ein stabiler Pfad regelmäßig läuft.

**Akzeptanzkriterien:**
- Mindestens ein aktuell deaktivierter Test ist als ausführbarer Test zurückgeführt (Unit oder Integration).
- CI-Job dokumentiert, wann welcher Testtyp läuft.
