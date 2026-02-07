# Produzenten-Level-System

> Stufenbasierte Progression mit Freischaltungen von Level 0 bis 30

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

Pro Spieler gespeichert: Level, Gesamt-XP, freigeschaltete Items, Verkaufsstatistiken (legal/illegal), Gesamtumsatz.
