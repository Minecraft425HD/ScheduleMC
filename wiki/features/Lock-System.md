# Schloss-System

> Tueren sichern mit Schloessern, Schluesseln und Hacking-Tools

## Ueberblick

Das Schloss-System erlaubt Spielern, Tueren mit verschiedenen Schloss-Typen zu sichern. Jeder Typ bietet unterschiedliche Sicherheitsstufen, Schluessel-Verwaltung und Hacking-Resistenz. Schloesser werden persistent in `schedulemc_locks.json` gespeichert.

## Schloss-Typen

| Typ | Schluessel-Dauer | Schluessel-Nutzungen | Dietrich-Chance | Code | Alarm |
|-----|-----------------|---------------------|-----------------|------|-------|
| **EINFACH** | 7 Tage | 100 | 80% | Nein | Nein |
| **SICHERHEIT** | 3 Tage | 30 | 40% | Nein | Warnung |
| **HOCHSICHERHEIT** | 12 Stunden | 10 | 10% | Nein | Ja (Alarm + Fahndung) |
| **KOMBINATION** | Kein Schluessel | - | 0% (immun) | 4-stellig | Nein |
| **DUAL** | 12 Stunden | 10 | 5% | 4-stellig | Ja (Alarm + Fahndung) |

## Schluessel-System

### Schluessel-Rohlinge

| Rohling | Tier | Fuer Schloss-Typ |
|---------|------|-----------------|
| Kupfer-Rohling | 0 | Einfach |
| Eisen-Rohling | 1 | Sicherheit |
| Netherite-Rohling | 2 | Hochsicherheit, Dual |

### Schluessel erstellen

Rechtsklick mit einem passenden Rohling auf eine verschlossene Tuer erstellt einen Schluessel. Nur der Besitzer oder autorisierte Spieler koennen Schluessel erstellen.

### Schluessel-Herkunft

| Herkunft | Dauer-Multiplikator | Nutzungs-Multiplikator |
|---------|---------------------|------------------------|
| ORIGINAL | 100% | 100% |
| KOPIE | 50% | 50% |
| GESTOHLEN | 25% | 25% |

### Schluesselring

- Fasst bis zu **8 Schluessel**
- Rechtsklick auf verschlossene Tuer: Findet und nutzt automatisch den passenden Schluessel
- Shift+Rechtsklick in die Luft: Zeigt alle gespeicherten Schluessel
- Shift+Rechtsklick mit Schluessel in Nebenhand: Fuegt Schluessel hinzu
- Entfernt automatisch abgelaufene Schluessel
- Herkunfts-Markierung: [O]=Original (gruen), [K]=Kopie (gelb), [G]=Gestohlen (rot)

## Hacking-Tools

### Dietrich

- **Haltbarkeit**: 15 Versuche
- Erfolgsrate entspricht dem Schloss-Typ (80% / 40% / 10% / 0% / 5%)
- Bei Fehlschlag an HOCHSICHERHEIT/DUAL: Alarm + Fahndung
- Nutzlos bei KOMBINATIONS-Schloessern

### Code-Cracker (Stufe: Gewoehnlich)

- **Haltbarkeit**: 10 Nutzungen
- **Erfolgsrate**: 50%
- **Ziel**: Nur KOMBINATIONS-Schloesser
- Kein Alarm bei Fehlschlag

### Bypass-Modul (Stufe: Ungewoehnlich)

- **Haltbarkeit**: 5 Nutzungen
- **Erfolgsrate**: 50%
- **Ziel**: Nur DUAL-Schloesser
- Alarm + Fahndung bei Fehlschlag

### Omni-Hack (Stufe: Selten)

- **Haltbarkeit**: 3 Nutzungen
- **Erfolgsrate**: 50%
- **Ziel**: KOMBINATION und DUAL-Schloesser
- Verzauberungs-Glanz
- Alarm nur bei DUAL-Schloss Fehlschlag

## Code-Eingabe

Bei KOMBINATIONS- und DUAL-Schloessern:

1. Rechtsklick auf die Tuer ohne gueltigen Schluessel oeffnet die Code-Eingabe-GUI
2. 4-stelligen Code eingeben (0000-9999)
3. Bei Erfolg: Tuer oeffnet sich fuer 3 Sekunden, dann schliesst sie automatisch
4. DUAL-Schloesser: Codes rotieren automatisch taeglich

## Schloss-Platzierung

- Rechtsklick mit einem Schloss-Item auf eine Tuer
- Schloss wird immer an der unteren Tuerhaelfte platziert
- Admin-platzierte Schloesser (OP Level 2+) haben keinen Besitzer (fuer Szenarien)
- Code-Schloesser generieren automatisch einen 4-stelligen Code

## Befehle

| Befehl | Berechtigung | Beschreibung |
|--------|-------------|-------------|
| `/lock code <lock-id> <code>` | Alle | Code eingeben zum Entsperren |
| `/lock setcode <lock-id> <code>` | Besitzer | Code aendern (4 Ziffern) |
| `/lock authorize <lock-id> <spieler>` | Besitzer | Spieler berechtigen |
| `/lock info <lock-id>` | Alle | Schloss-Details anzeigen |
| `/lock remove <lock-id>` | Besitzer | Eigenes Schloss entfernen |
| `/lock list` | Alle | Alle eigenen Schloesser auflisten |
| `/lock admin remove <lock-id>` | Admin (OP 2+) | Beliebiges Schloss entfernen |

## Schloss-Info

Shift+Rechtsklick auf eine verschlossene Tuer zeigt:
- Schloss-ID (noetig fuer Befehle)
- Schloss-Typ (farbcodiert)
- Besitzername oder "[Kein Besitzer]"
- Benoetigte Schluessel-Stufe
- Aktueller Code (nur fuer Besitzer/Autorisierte/OP sichtbar)
- Dietrich-Erfolgswahrscheinlichkeit
- Alarm-Status

## Datenspeicherung

**Datei**: `schedulemc_locks.json`

Gespeicherte Daten pro Schloss: ID, Typ, Besitzer-UUID, Position, Dimension, Code, autorisierte Spieler, Platzierungszeitpunkt.
