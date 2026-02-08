# Wein-System

> Weinbau, Kelterung und Reifung mit 4 Rebsorten und Alterungsstufen

## Ueberblick

Das Wein-System umfasst eine vollstaendige Produktionskette vom Rebstock bis zur abgefuellten Flasche. Es bietet 4 Rebsorten (2 Weissweine, 2 Rotweine), 4 Verarbeitungsmethoden nach Suessgrad, ein Temperaturbonus-System und 3 Alterungsstufen.

## Rebsorten

### Weissweine

| Sorte | Farbe | Basispreis/L | Wachstum | Qualitaetsfaktor | Ertrag | Optimal-Temp | Freischaltung |
|-------|-------|-------------|----------|------------------|--------|-------------|---------------|
| Riesling | Gelb | 15 EUR | 120 Tage | 0,8 | 10 | 18 Grad C | Level 5 |
| Chardonnay | Orange | 22 EUR | 110 Tage | 0,9 | 8 | 20 Grad C | Level 8 |

### Rotweine

| Sorte | Farbe | Basispreis/L | Wachstum | Qualitaetsfaktor | Ertrag | Optimal-Temp | Freischaltung |
|-------|-------|-------------|----------|------------------|--------|-------------|---------------|
| Spaetburgunder | Rot | 28 EUR | 140 Tage | 0,85 | 9 | 16 Grad C | Level 13 |
| Merlot | Dunkelrot | 35 EUR | 130 Tage | 0,95 | 7 | 22 Grad C | Level 16 |

## Qualitaetsstufen

| Qualitaet | Farbe | Preis-Multiplikator |
|-----------|-------|---------------------|
| SCHLECHT | Rot | 0,7x |
| GUT | Gelb | 1,0x |
| SEHR GUT | Gruen | 2,0x |
| LEGENDAER | Magenta | 4,0x |

## Temperaturbonus

| Abstand zur Optimal-Temperatur | Modifikator |
|-------------------------------|-------------|
| +/- 3 Grad C | +20% Qualitaet |
| +/- 6 Grad C | +10% Qualitaet |
| +/- 10 Grad C | Normal (1,0x) |
| Mehr als +/- 10 Grad C | -20% Qualitaet |

## Alterungsstufen

| Stufe | Mindest-Ticks | Preis-Multiplikator |
|-------|-------------|---------------------|
| JUNG | 0 | 1,0x |
| MITTEL | 2.400 (2 MC-Tage) | 1,3x |
| GEREIFT | 7.200 (6 MC-Tage) | 1,8x |

## Verarbeitungsmethoden (Suessgrad)

| Methode | Zuckergehalt | Preis-Multiplikator | Freischaltung |
|---------|-------------|---------------------|---------------|
| TROCKEN | 0-9 g/L | 1,0x | Standard |
| HALBTROCKEN | 9-18 g/L | 1,1x | Level 10 |
| LIEBLICH | 18-45 g/L | 1,3x | Standard |
| DESSERT | mehr als 45 g/L | 1,8x | Level 20 |

## Produktionskette

### 1. Anbau

Reben werden in sortenspezifische Rebstock-Toepfe gepflanzt:
- Riesling-Rebstock-Topf
- Spaetburgunder-Rebstock-Topf
- Chardonnay-Rebstock-Topf
- Merlot-Rebstock-Topf

### 2. Maische (Crushing Station)

- Eingabe: Trauben
- Ausgabe: Trauben-Maische
- Qualitaet wird bei der Maische bestimmt

### 3. Keltern (Wine Press)

| Groesse | Kapazitaet | Ticks/Maische |
|---------|-----------|---------------|
| Klein | 16 | 200 |
| Mittel | 24 | 150 |
| Gross | 32 | 100 |

- Eingabe: Trauben-Maische
- Ausgabe: Traubensaft (Qualitaet wird in NBT gespeichert)

### 4. Gaerung (Fermentation Tank)

| Groesse | Kapazitaet | Ticks/Saft |
|---------|-----------|-----------|
| Klein | 16 | 2.400 |
| Mittel | 24 | 1.800 |
| Gross | 32 | 1.200 |

- Eingabe: Traubensaft
- Ausgabe: Junger Wein

### 5. Reifung (Aging Barrel)

| Groesse | Kapazitaet |
|---------|-----------|
| Klein | 16 |
| Mittel | 24 |
| Gross | 32 |

- Wein altert automatisch (1 Tick pro Spiel-Tick)
- Automatische Qualitaetsverbesserung alle 2.400 Ticks
- Kann Legendaer-Qualitaet erreichen

### 6. Abfuellung (Bottling Station)

- Eingabe: Gereifter Wein
- Ausgabe: Weinflaschen (versiegelt, keine weitere Alterung)
- Alle Qualitaets- und Typeninformationen werden beibehalten

## Items

- Reben-Setzlinge (4 Sorten)
- Trauben
- Trauben-Maische (sortenspezifisch)
- Traubensaft (sortenspezifisch)
- Junger Wein (alle Sorten)
- Gereifter Wein (alle Sorten)
- Weinflaschen
- Weinglaeser

## Bloecke

| Block | Beschreibung |
|-------|-------------|
| Grapevine Pots (4 Sorten) | Rebstock-Toepfe |
| crushing_station | Maische-Station |
| small/medium/large_wine_press | Weinkelter (3 Groessen) |
| small/medium/large_fermentation_tank | Gaertank (3 Groessen) |
| small/medium/large_aging_barrel | Reifungsfass (3 Groessen) |
| wine_bottling_station | Abfuellstation |
