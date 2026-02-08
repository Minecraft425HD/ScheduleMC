# Bier-System

> Brauprozess mit 4 Biersorten, Gaerung und Konditionierung

## Ueberblick

Das Bier-System bietet einen vollstaendigen Brauprozess vom Malz bis zur fertigen Flasche. Es umfasst 4 Biersorten mit unterschiedlichen Alkoholgehalten, IBU-Werten und Plato-Graden, sowie 3 Reifungsstufen und 3 Verarbeitungsmethoden.

## Biersorten

| Sorte | Farbe | Basispreis/L | Reifezeit | Qualitaetsfaktor | ABV | IBU | Plato | Freischaltung |
|-------|-------|-------------|-----------|------------------|-----|-----|-------|---------------|
| Pilsner | Gelb | 8 EUR | 30 Tage | 1,0 | 4,8% | 35 | 11,5 | Level 1 |
| Weizen | Orange | 10 EUR | 35 Tage | 1,1 | 5,4% | 15 | 12,5 | Level 5 |
| Ale | Rot | 12 EUR | 40 Tage | 1,2 | 6,5% | 40 | 13,5 | Level 8 |
| Stout | Dunkelgrau | 15 EUR | 50 Tage | 1,3 | 7,2% | 45 | 16,0 | Level 11 |

## Qualitaetsstufen

| Qualitaet | Farbe | Preis-Multiplikator |
|-----------|-------|---------------------|
| SCHLECHT | Rot | 0,7x |
| GUT | Gelb | 1,0x |
| SEHR GUT | Gruen | 1,5x |
| LEGENDAER | Gold | 2,5x |

## Reifungsstufen

| Stufe | Zeitraum | Preis-Multiplikator |
|-------|---------|---------------------|
| JUNG | 0-14 Tage | 1,0x |
| GEREIFT | 15-60 Tage | 1,2x |
| GEALTERT | 61+ Tage | 1,4x |

## Verarbeitungsmethoden

| Methode | Preis-Multiplikator | Beschreibung |
|---------|---------------------|-------------|
| FASS | 1,2x | Frischeste Form im Fass |
| FLASCHE | 1,0x | Standard-Format |
| DOSE | 0,9x | Praktisch, aber weniger wertvoll |

## Temperaturbonus

| Temperaturbereich | Modifikator |
|-------------------|-------------|
| 8-15 Grad C | +30% (perfekt) |
| 5-20 Grad C | +15% (gut) |
| 0-25 Grad C | Normal (1,0x) |
| Unter 0 oder ueber 25 Grad C | -30% (schlecht) |

## Produktionskette

### 1. Maelzen (Malting Station)

- Eingabe: Getreide + Wasser
- Ausgabe: Wuerze
- Basisverarbeitungszeit variiert nach Getreidetyp

### 2. Brauen (Brew Kettle)

| Groesse | Geschwindigkeit | Ticks |
|---------|----------------|-------|
| Klein | 1,0x | 1.200 (60 Sek.) |
| Mittel | 1,5x | 800 |
| Gross | 2,0x | 600 |

- Eingabe: Wuerze-Eimer + Hopfen
- Ausgabe: Unvergorenes Bier
- Qualitaet kann um 1 Stufe verbessert werden

### 3. Gaerung (Fermentation Tank)

| Groesse | Geschwindigkeit | Ticks |
|---------|----------------|-------|
| Klein | 1,0x | 2.400 (120 Sek.) |
| Mittel | 1,5x | 1.600 |
| Gross | 2,0x | 1.200 |

- Eingabe: Unvergorenes Bier + Hefe (Brau-/Lager-/Ale-Hefe)
- Ausgabe: Jungbier (Gruenbier)
- Qualitaet bleibt erhalten

### 4. Konditionierung (Conditioning Tank)

| Groesse | Geschwindigkeit |
|---------|----------------|
| Klein | 1,0x |
| Mittel | 1,5x |
| Gross | 2,0x |

- Dauer variiert nach Biersorte (30-50 Tage)
- Automatische Qualitaetsverbesserung ueber Zeit

### 5. Abfuellung (Beer Bottling Station)

- Eingabe: Konditioniertes Bier
- Ausgabe: Bierflaschen
- Verarbeitungszeit: 200 Ticks pro Flasche

## Items

- Getreide (verschiedene Typen)
- Wuerze (in Eimern)
- Hopfen (Dolden, getrocknet, Extrakt, Pellets)
- Gaerungsbier
- Jungbier (Gruenbier)
- Konditioniertes Bier
- Bierflaschen
- Hefe (Standard, Brau-, Lager-, Ale-Hefe)

## Bloecke

| Block | Beschreibung |
|-------|-------------|
| malting_station | Maelzstation |
| mash_tun | Maischbottich |
| small/medium/large_brew_kettle | Braukessel (3 Groessen) |
| small/medium/large_beer_fermentation_tank | Gaertank (3 Groessen) |
| small/medium/large_conditioning_tank | Konditionierungstank (3 Groessen) |
| beer_bottling_station | Abfuellstation |
