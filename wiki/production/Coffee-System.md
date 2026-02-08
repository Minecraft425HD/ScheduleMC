# Kaffee-System

> Anbau, Verarbeitung und Verpackung von Kaffee mit 4 Sorten und Qualitaetsstufen

## Ueberblick

Das Kaffee-System bietet eine vollstaendige Produktionskette vom Setzling bis zur verpackten Ware. Es umfasst 4 Kaffeesorten, 2 Verarbeitungsmethoden, 4 Roestgrade, 4 Mahlgrade und ein Hoehenbonus-System.

## Kaffeesorten

| Sorte | Farbe | Basispreis | Wachstum | Wasserverbrauch | Ertrag | Optimale Hoehe | Freischaltung |
|-------|-------|-----------|----------|-----------------|--------|---------------|---------------|
| Arabica | Gelb | 12 EUR | 140 Ticks | 0,7/Stufe | 8 Kirschen | Y=1800 | Level 3 |
| Robusta | Orange | 18 EUR | 120 Ticks | 0,9/Stufe | 10 Kirschen | Y=1600 | Level 6 |
| Liberica | Magenta | 25 EUR | 160 Ticks | 0,8/Stufe | 6 Kirschen | Y=2000 | Level 13 |
| Excelsa | Dunkelviolett | 35 EUR | 180 Ticks | 1,0/Stufe | 7 Kirschen | Y=2200 | Level 16 |

## Qualitaetsstufen

| Qualitaet | Farbe | Preis-Multiplikator |
|-----------|-------|---------------------|
| SCHLECHT | Rot | 0,7x |
| GUT | Gelb | 1,0x |
| SEHR GUT | Gruen | 2,0x |
| LEGENDAER | Gold | 4,0x |

## Hoehenbonus

| Abstand zur optimalen Hoehe | Qualitaets-Modifikator |
|------------------------------|----------------------|
| +/- 10 Y-Level | +20% Qualitaet |
| +/- 30 Y-Level | Normal (1,0x) |
| +/- 50 Y-Level | -20% Qualitaet |
| Mehr als +/- 50 | -40% Qualitaet |

## Produktionskette

### 1. Anbau

Kaffee-Setzlinge werden in spezielle Toepfe gepflanzt:
- **Terrakotta-Topf** (Level 1)
- **Keramik-Topf** (Level 6, 4 Pflanzen)
- **Eisen-Topf** (Level 11, 5 Pflanzen)
- **Gold-Topf** (Level 16, Qualitaetsboost)

Jede Sorte hat eigene Pflanzenblöcke (Arabica, Robusta, Liberica, Excelsa).

### 2. Verarbeitung (Nassverarbeitung oder Trocknung)

#### Nassverarbeitung (Wet Processing Station)

| Parameter | Wert |
|-----------|------|
| Kapazitaet | 32 Kirschen |
| Stufen | PULPING (200 T) → FERMENTATION (1.200 T) → WASHING (400 T) |
| Gesamtzeit | 1.800 Ticks (90 Sekunden) |
| Qualitaets-Multiplikator | 1,3x |
| Ausgabe | 2 Gruene Bohnen pro Kirsche |

#### Trocknung (Drying Tray)

| Groesse | Kapazitaet | Ticks/Kirsche |
|---------|-----------|---------------|
| Klein | 15 | 300 |
| Mittel | 22 | 250 |
| Gross | 30 | 400 |

Qualitaets-Multiplikator: 1,1x

### 3. Roestung (Coffee Roaster)

| Groesse | Kapazitaet | Ticks/Bohne |
|---------|-----------|-------------|
| Klein | 16 | 300 |
| Mittel | 24 | 250 |
| Gross | 32 | 200 |

#### Roestgrade

| Roestgrad | Preis-Multiplikator | Temperatur | Freischaltung |
|-----------|---------------------|-----------|---------------|
| LIGHT | 1,0x | 200 Grad C | Standard |
| MEDIUM | 1,2x | 220 Grad C | Level 10 |
| DARK | 1,4x | 240 Grad C | Standard |
| ESPRESSO | 1,6x | 260 Grad C | Level 20 |

### 4. Mahlung (Coffee Grinder)

| Parameter | Wert |
|-----------|------|
| Kapazitaet | 64 geroestete Bohnen |
| Verarbeitungszeit | 100 Ticks (5 Sekunden) |

#### Mahlgrade

| Mahlgrad | Verwendung |
|----------|-----------|
| GROB | Pressstempelkanne |
| MITTEL | Standard |
| FEIN | Espressomaschine |
| EXTRA FEIN | Tuerkischer Kaffee |

### 5. Verpackung (Coffee Packaging Table)

| Parameter | Wert |
|-----------|------|
| Eingabe | Gemahlener Kaffee + Kaffeetasche |
| Verarbeitungszeit | 200 Ticks (10 Sekunden) |

#### Paketgroessen

| Groesse | Benoetigte Menge |
|---------|-----------------|
| KLEIN (250g) | 250 Items |
| MITTEL (500g) | 500 Items |
| GROSS (1kg) | 1.000 Items |

## Items

- Kaffee-Setzlinge
- Kaffeekirschen
- Gruene Kaffeebohnen
- Geroestete Kaffeebohnen
- Gemahlener Kaffee
- Gebruehter Kaffee
- Espresso
- Verpackter Kaffee (3 Groessen)

## Bloecke

| Block | Beschreibung |
|-------|-------------|
| coffee_terracotta_pot | Terrakotta-Topf |
| coffee_ceramic_pot | Keramik-Topf |
| coffee_iron_pot | Eisen-Topf |
| coffee_golden_pot | Gold-Topf |
| wet_processing_station | Nassverarbeitungs-Station |
| small/medium/large_coffee_drying_tray | Trocknungsschalen (3 Groessen) |
| small/medium/large_coffee_roaster | Roester (3 Groessen) |
| coffee_grinder | Kaffemuehle |
| coffee_packaging_table | Verpackungstisch |
