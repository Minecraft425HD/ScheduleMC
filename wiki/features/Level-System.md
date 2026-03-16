# Produzenten-Level-System (Level System)

<div align="center">

**Stufenbasierte Progression mit Freischaltungen von Level 0 bis 30**

53 freischaltbare Features, XP aus Produktion und Verkauf, Progression fuer Anfaenger bis Endgame

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Ueberblick](#ueberblick)
2. [XP-Progression](#xp-progression)
3. [XP-Quellen](#xp-quellen)
4. [Freischaltungen](#freischaltungen)
5. [Level-Up Benachrichtigung](#level-up-benachrichtigung)
6. [Smartphone-App](#smartphone-app)
7. [Netzwerk-Synchronisation](#netzwerk-synchronisation)
8. [Datenspeicherung](#datenspeicherung)
9. [Admin-Befehle](#admin-befehle)
10. [Best Practices](#best-practices)
11. [Fehlerbehebung](#fehlerbehebung)

---

## Ueberblick

Das Produzenten-Level-System ist ein mehrstufiges Progressionssystem (Level 0-30), das Gameplay-Features durch XP-Akkumulation freischaltet. Spieler verdienen XP durch das Produzieren und Verkaufen von Waren, wobei riskantere illegale Produkte mehr XP liefern.

## XP-Progression

### Formel

```
XP fuer Level = BASE_XP x Level^EXPONENT
BASE_XP = 100
EXPONENT = 1.8
MAX_LEVEL = 30
```

### XP-Tabelle

| Level | Benoetigt (kumulativ) |
|-------|----------------------|
| 1 | 100 XP |
| 5 | 3.373 XP |
| 10 | 23.101 XP |
| 15 | 77.625 XP |
| 20 | 193.071 XP |
| 25 | 388.235 XP |
| 30 | 656.059 XP |

## XP-Quellen

### Illegale Produktion (Hoch-XP, Hohes Risiko)

| Quelle | Basis-XP |
|--------|----------|
| Cannabis verkaufen | 8 XP |
| Tabak verkaufen | 5 XP |
| Kokain verkaufen | 15 XP |
| Heroin verkaufen | 18 XP |
| Meth verkaufen | 16 XP |
| MDMA verkaufen | 12 XP |
| LSD verkaufen | 14 XP |
| Pilze verkaufen | 10 XP |

### Legale Produktion (Niedrig-XP, Kein Risiko)

| Quelle | Basis-XP |
|--------|----------|
| Wein verkaufen | 6 XP |
| Bier verkaufen | 4 XP |
| Kaffee verkaufen | 5 XP |
| Kaese verkaufen | 5 XP |
| Schokolade verkaufen | 4 XP |
| Honig verkaufen | 4 XP |

### Verarbeitung und Sonstiges

| Quelle | Basis-XP |
|--------|----------|
| Ernte | 2 XP |
| Verarbeitung | 3 XP |
| Maschine herstellen | 10 XP |
| Taegliches Login | 5 XP |
| Erfolg abgeschlossen | 25 XP |
| Razzia ueberlebt | 50 XP |

### XP-Berechnung

```
Ergebnis = Basis-XP x Menge x Qualitaets-Multiplikator
```

## Freischaltungen

### Level 1-5: Einstieg

| Feature | Level | Kategorie |
|---------|-------|-----------|
| Tabak Virginia | 1 | Produktionskette |
| Cannabis Autoflower | 1 | Produktionskette |
| Bier Pilsner | 1 | Produktionskette |
| Honig Akazie | 1 | Produktionskette |
| Terrakotta-Topf | 1 | Topf |
| Tabak Burley | 3 | Produktionskette |
| Kaffee Arabica | 3 | Produktionskette |
| Kaese Gouda | 3 | Produktionskette |
| Schokolade Milch | 3 | Produktionskette |
| Gang beitreten | 5 | Gang |
| Bier Weizen | 5 | Produktionskette |
| Wein Riesling | 5 | Produktionskette |
| Honig Wildblume | 5 | Produktionskette |
| Cannabis Indica | 5 | Produktionskette |

### Level 6-10: Erweiterung

| Feature | Level | Kategorie |
|---------|-------|-----------|
| Keramik-Topf (4 Pflanzen) | 6 | Topf |
| Tabak Oriental | 6 | Produktionskette |
| Kaffee Robusta | 6 | Produktionskette |
| Kaese Emmentaler | 6 | Produktionskette |
| Schokolade Dunkel | 6 | Produktionskette |
| Cannabis Sativa | 8 | Produktionskette |
| Pilze Mexicana | 8 | Produktionskette |
| Bier Ale | 8 | Produktionskette |
| Wein Chardonnay | 8 | Produktionskette |
| Honig Wald | 8 | Produktionskette |
| Kaffee Roestung Medium | 10 | Verarbeitung |
| Kaese geraeuchert | 10 | Verarbeitung |
| Wein halbtrocken | 10 | Verarbeitung |

### Level 11-15: Harte Drogen

| Feature | Level | Kategorie |
|---------|-------|-----------|
| Eisen-Topf (5 Pflanzen) | 11 | Topf |
| Coca Bolivianisch | 11 | Produktionskette |
| MDMA-Produktion | 11 | Produktionskette |
| Kaese Camembert | 11 | Produktionskette |
| Bier Stout | 11 | Produktionskette |
| Cannabis Hybrid | 13 | Produktionskette |
| Pilze Cubensis | 13 | Produktionskette |
| LSD-Produktion | 13 | Produktionskette |
| Wein Spaetburgunder | 13 | Produktionskette |
| Kaffee Liberica | 13 | Produktionskette |
| **Gang gruenden (25.000 EUR)** | 15 | Gang |
| Mohn Indisch (Heroin) | 15 | Produktionskette |
| Coca Peruanisch | 15 | Produktionskette |
| Meth-Produktion | 15 | Produktionskette |
| Schokolade Ruby | 15 | Produktionskette |

### Level 16-20: Premium

| Feature | Level | Kategorie |
|---------|-------|-----------|
| Gold-Topf (Qualitaetsboost) | 16 | Topf |
| Tabak Havana | 16 | Produktionskette |
| Wein Merlot | 16 | Produktionskette |
| Kaffee Excelsa | 16 | Produktionskette |
| Kaese Parmesan | 16 | Produktionskette |
| Mohn Tuerkisch | 18 | Produktionskette |
| Coca Kolumbianisch | 18 | Produktionskette |
| Pilze Azurescens | 18 | Produktionskette |
| Honig Manuka | 18 | Produktionskette |
| Kaffee Espresso-Roestung | 20 | Verarbeitung |
| Wein Dessert | 20 | Verarbeitung |
| Kaese mit Kraeutern | 20 | Verarbeitung |
| Schokolade Gefuellt | 20 | Verarbeitung |

### Level 21-25: Experte

| Feature | Level | Kategorie |
|---------|-------|-----------|
| Mohn Afghanisch (hoechste Potenz) | 22 | Produktionskette |
| Mittlere Maschinen | 22 | Maschine |
| SUV-Fahrzeug | 22 | Fahrzeug |
| Transporter-Fahrzeug | 22 | Fahrzeug |
| Grosse Maschinen | 25 | Maschine |
| Sport-Fahrzeug | 25 | Fahrzeug |

### Level 26-30: Endgame

| Feature | Level | Kategorie |
|---------|-------|-----------|
| Marktzugang (Angebot/Nachfrage einsehen) | 26 | Wirtschaft |
| Preis-Benachrichtigungen | 28 | Wirtschaft |
| Grosshandel (Mengenrabatte) | 30 | Wirtschaft |

## Level-Up Benachrichtigung

Beim Level-Aufstieg erhaelt der Spieler:
- System-Nachricht mit neuem Level
- Liste aller neu freigeschalteten Features (gruen markiert)
- Smartphone-Benachrichtigung

### Fortschrittsanzeige

```
[||||||||..........] 45% - Level 12 → 13
```

## Smartphone-App

Die **Produzenten-Level-App** zeigt:
- Aktuelles Level und XP
- Fortschrittsbalken zum naechsten Level
- Liste aller freigeschalteten Features
- Naechste Freischaltungen

## Netzwerk-Synchronisation

| Paket | Richtung | Beschreibung |
|-------|----------|-------------|
| RequestProducerLevelDataPacket | Client zu Server | Level-Daten anfordern |
| SyncProducerLevelDataPacket | Server zu Client | Vollstaendige Synchronisation |
| LevelUpNotificationPacket | Server zu Client | Level-Up Benachrichtigung |

## Datenspeicherung

**Datei**: `config/schedulemc_producer_levels.json`

### Gespeicherte Daten pro Spieler

| Feld | Beschreibung |
|------|-------------|
| `playerUUID` | Eindeutiger Spieler-Identifikator |
| `level` | Aktuelles Level (0–30) |
| `totalXP` | Gesamt-XP aller Zeiten |
| `unlockedItems` | Set freigeschalteter Item-IDs |
| `legalSales` | Anzahl legaler Verkauefe |
| `illegalSales` | Anzahl illegaler Verkauefe |
| `totalRevenue` | Kumulativer Gesamtumsatz in EUR |

---

## Admin-Befehle

| Befehl | Berechtigung | Beschreibung |
|--------|-------------|-------------|
| `/level set <spieler> <level>` | OP Level 2 | Spieler-Level direkt setzen |
| `/level addxp <spieler> <xp>` | OP Level 2 | XP hinzufuegen |
| `/level reset <spieler>` | OP Level 2 | Level und XP zuruecksetzen |
| `/level info <spieler>` | OP Level 2 | Detaillierte Level-Informationen |

---

## Best Practices

### Fuer Spieler

1. **Illegale Produktion priorisieren** — Illegale Verkauefe geben 1,5x mehr XP (konfigurierbar). Das beschleunigt die Progression erheblich, aber mit hoeherem Risiko.
2. **Qualitaets-Multiplikator nutzen** — Bessere Qualitaet gibt mehr XP pro Verkauf (`XP = Basis-XP × Menge × Qualitaets-Multiplikator`).
3. **Taeglich einloggen** — 5 XP/Tag klingt wenig, aber ueber 30 Tage sind das 150 XP = 1,5 frueher erreichte Level in den Anfangsstufen.
4. **Maschinen herstellen** — Jede hergestellte Maschine gibt 10 XP. Beim Aufbau einer Produktionsanlage koennen schnell 100–200 XP zusammenkommen.
5. **Achievements abschliessen** — Abgeschlossene Achievements geben 25 XP und EUR-Belohnungen.

### Fuer Server-Admins

1. **XP-Kurve anpassen** — Standard `EXPONENT = 1.8` erzeugt eine steile Kurve (Level 30 = 656.059 XP). Setze auf `1.5` fuer schnellere Progression.
2. **Multiplier balancieren** — `illegal_xp_multiplier = 1.5` incentiviert risikoreichere Produktion. Auf `1.0` setzen fuer gleiche XP bei legalem und illegalem Spielstil.
3. **Level-Requirements fuer Gangs** — Gang-Gruendung erfordert Level 15, Beitreten Level 5. Diese Werte sind Teil der Spielbalance und sollten nicht zu niedrig sein.
4. **Admin-Reset** — Nutze `/level reset <spieler>` nur auf explizite Anfrage des Spielers oder bei technischen Problemen.

---

## Fehlerbehebung

### Level steigt nicht

1. **Synchronisation abwarten** — Level-Updates kommen nach dem Verkauf. Pruefe die Smartphone-App nach 1–2 Sekunden.
2. **XP-Quelle pruefen** — Nicht alle Aktionen geben XP. Pruefe die XP-Quellen-Tabelle oben.
3. **Datei pruefen** — Oeffne `config/schedulemc_producer_levels.json` auf korrupte Eintraege.

### Freischaltung nicht verfuegbar

1. **Level-Anforderung** — Pruefe das genaue Level fuer das Feature in der Freischaltungs-Tabelle.
2. **Neu laden** — Trenne dich und verbinde dich neu. Das Level-System synchronisiert beim Join.
3. **Admin-Pruefung** — Nutze `/level info <spieler>` um den aktuellen State zu sehen.

### Daten nach Neustart verloren

1. **Datei pruefen** — `config/schedulemc_producer_levels.json` auf gueltiges JSON pruefen.
2. **Backup wiederherstellen** — Aus `config/backups/schedulemc_producer_levels_<timestamp>.json`.
3. **Schreibrechte** — Server braucht Schreibzugriff auf das `config/`-Verzeichnis.
