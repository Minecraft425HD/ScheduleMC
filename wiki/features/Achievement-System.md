# Achievement-System

> Erfolge freischalten und Belohnungen verdienen

## Ueberblick

Das Achievement-System bietet 24 Erfolge in 5 Kategorien mit 5 Schwierigkeitsstufen. Spieler schalten Erfolge durch verschiedene Ingame-Aktivitaeten frei und erhalten Geldbelohnungen. Der Fortschritt wird automatisch verfolgt und zwischen Server und Client synchronisiert.

## Kategorien

| Kategorie | Symbol | Beschreibung |
|-----------|--------|-------------|
| WIRTSCHAFT | - | Finanzielle Erfolge |
| VERBRECHEN | - | Kriminalitaets-Erfolge |
| PRODUKTION | - | Produktions-/Handwerks-Erfolge |
| SOZIAL | - | Soziale/Grundstuecks-Erfolge |
| ERKUNDUNG | - | Erkundungs-Erfolge |

## Schwierigkeitsstufen und Belohnungen

| Stufe | Belohnung |
|-------|----------|
| BRONZE | 100 EUR |
| SILBER | 500 EUR |
| GOLD | 2.000 EUR |
| DIAMANT | 10.000 EUR |
| PLATIN | 50.000 EUR |

## Alle Erfolge

### Wirtschaft (11 Erfolge)

| Erfolg | Anforderung | Stufe |
|--------|------------|-------|
| FIRST_EURO | 1 EUR Guthaben | Bronze |
| RICH | 10.000 EUR Guthaben | Bronze |
| WEALTHY | 100.000 EUR Guthaben | Silber |
| MILLIONAIRE | 1.000.000 EUR Guthaben | Gold |
| LOAN_MASTER | 10 Kredite zurueckgezahlt | Silber |
| SAVINGS_KING | 100.000 EUR Sparguthaben | Gold |
| BIG_SPENDER | 1.000.000 EUR ausgegeben | Diamant |
| FIRST_TRADE | Erster Boersenhandel | Bronze |
| FIRST_PROFIT | Erster profitabler Handel | Bronze |
| FIRST_LOSS | Erster Verlusthandel | Bronze |
| PROFIT_MASTER | Kumulativer Gewinn (mehrere Stufen) | Bronze bis Diamant |

### Verbrechen (6 Erfolge)

| Erfolg | Anforderung | Stufe |
|--------|------------|-------|
| FIRST_CRIME | Fahndungslevel erhalten | Bronze |
| WANTED | Fahndungslevel 3+ | Silber |
| MOST_WANTED | Fahndungslevel 5+ | Gold |
| ESCAPE_ARTIST | 10 Polizeifluechte | Silber |
| PRISON_VETERAN | 100 Tage im Gefaengnis | Gold |
| CLEAN_RECORD | 30 verbrechensfreie Tage | Diamant |

### Produktion (5 Erfolge)

| Erfolg | Anforderung | Stufe |
|--------|------------|-------|
| HOBBYIST | 100 Pflanzen geerntet | Bronze |
| FARMER | 100 kg Items produziert | Silber |
| PRODUCER | 1.000 kg Items produziert | Gold |
| DRUG_LORD | 10.000 kg Items produziert | Diamant |
| EMPIRE_BUILDER | 10 Produktionsstandorte | Platin |

### Sozial (4 Erfolge)

| Erfolg | Anforderung | Stufe |
|--------|------------|-------|
| FIRST_PLOT | Erstes Grundstueck gekauft | Bronze |
| PROPERTY_MOGUL | 5+ Grundstuecke im Besitz | Gold |
| LANDLORD | 100.000 EUR Mieteinnahmen | Diamant |
| POPULAR | 50 positive Bewertungen | Gold |

## Automatisches Tracking

Der Achievement-Tracker prueft alle 60 Sekunden folgende Werte:
- **Kontostand-basiert**: Guthaben fuer FIRST_EURO, RICH, WEALTHY, MILLIONAIRE
- **Kriminalitaets-basiert**: Fahndungslevel fuer FIRST_CRIME, WANTED, MOST_WANTED
- **Grundstuecks-basiert**: Anzahl Grundstuecke fuer FIRST_PLOT, PROPERTY_MOGUL

Zusaetzlich werden folgende Aktionen manuell getrackt:
- Kreditrueckzahlungen, Spareinlagen, Ausgaben
- Polizeifluechte, Gefaengnistage, verbrechensfreie Tage
- Pflanzungen, Produktionsmengen (kg), Produktionsstandorte
- Mieteinnahmen, positive Bewertungen

## Smartphone-App

Erfolge koennen ueber die **Achievements-App** auf dem Smartphone eingesehen werden:
- Alle Erfolge nach Kategorie sortiert
- Fortschrittsanzeige pro Erfolg
- Freigeschaltete vs. gesperrte Erfolge
- Gesamtstatistiken (freigeschaltete Anzahl, verdiente Belohnungen)

## Versteckte Erfolge

Einige Erfolge sind als "versteckt" markiert. Ihre Anforderungen werden erst sichtbar, wenn der Spieler sich der Freischaltung naehert.

## Netzwerk-Synchronisation

| Paket | Richtung | Beschreibung |
|-------|----------|-------------|
| RequestAchievementDataPacket | Client zu Server | Alle Erfolge anfordern |
| SyncAchievementDataPacket | Server zu Client | Vollstaendige Synchronisation |

## Datenspeicherung

**Datei**: `config/plotmod_achievements.json`

Pro Spieler gespeichert: Freigeschaltete Erfolge, Fortschritt, Zeitstempel, verdiente Gesamtpunkte.
