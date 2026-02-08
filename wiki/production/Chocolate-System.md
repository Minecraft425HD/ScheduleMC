# Schokoladen-System

> Kakaoverarbeitung mit Roestung, Conchierung, Temperierung und Formgebung

## Ueberblick

Das Schokoladen-System bietet die komplexeste legale Produktionskette mit ueber 10 Verarbeitungsschritten. Es umfasst 4 Schokoladensorten, 3 Verarbeitungsmethoden, 3 Reifungsstufen und zahlreiche spezialisierte Maschinen.

## Schokoladensorten

| Sorte | Farbe | Basispreis/kg | Reifezeit | Qualitaetsfaktor | Kakaoanteil | Freischaltung |
|-------|-------|-------------|-----------|------------------|-------------|---------------|
| Milch | Gelb | 15 EUR | 30 Tage | 1,0 | 30% | Level 3 |
| Weiss | Weiss | 12 EUR | 20 Tage | 0,9 | 0% | Standard |
| Dunkel | Orange | 20 EUR | 60 Tage | 1,3 | 70% | Level 6 |
| Ruby | Magenta | 30 EUR | 40 Tage | 1,5 | 47% | Level 15 |

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
| FRISCH | 0-30 Tage | 1,0x |
| GEALTERT | 31-90 Tage | 1,2x |
| VINTAGE | 91+ Tage | 1,5x |

## Verarbeitungsmethoden

| Methode | Preis-Multiplikator | Beschreibung | Freischaltung |
|---------|---------------------|-------------|---------------|
| NATUR | 1,0x | Reine Schokolade | Standard |
| GEFUELLT | 1,3x | Mit Karamell-/Nougat-Fuellung | Level 20 |
| GEMISCHT | 1,2x | Nuesse, Fruechte, Zutaten | Standard |

## Temperaturbonus

| Temperaturbereich | Modifikator |
|-------------------|-------------|
| 20-30 Grad C | +30% (perfekt) |
| 15-35 Grad C | +15% (gut) |
| 10-40 Grad C | Normal (1,0x) |
| Unter 10 oder ueber 40 Grad C | -30% (schlecht) |

## Produktionskette

### 1. Roestung (Roasting Station)

| Parameter | Wert |
|-----------|------|
| Kapazitaet | 64 rohe Kakaobohnen |
| Verarbeitungszeit | 400 Ticks (20 Sekunden) |
| Eingabe | Rohe Kakaobohnen |
| Ausgabe | Geroestete Kakaobohnen |

### 2. Schaelen (Winnowing Machine)

- Eingabe: Geroestete Kakaobohnen
- Ausgabe: Kakao-Nibs
- Entfernt Kakaoschale
- Qualitaet verbessert sich um 1 Stufe

### 3. Mahlen (Grinding Mill)

| Parameter | Wert |
|-----------|------|
| Kapazitaet | 64 Nibs |
| Verarbeitungszeit | 600 Ticks (30 Sekunden) |
| Eingabe | Kakao-Nibs |
| Ausgabe | Kakaomasse |
| Qualitaetsverbesserung | +1 Stufe (max. GUT) |

### 4. Pressen (Pressing Station)

- Eingabe: Kakaomasse
- Ausgabe: Kakaobutter (Nebenprodukt fuer Conchierung)

### 5. Conchierung (Conching Machine)

| Groesse | Kapazitaet | Geschwindigkeit |
|---------|-----------|----------------|
| Klein | 16 | 1,0x |
| Mittel | 24 | 1,5x |
| Gross | 32 | 2,0x |

- Eingabe: Kakaomasse + Zucker + Kakaobutter
- Ausgabe: Conchierte Schokolade (veredelte Mischung)
- Entwickelt Geschmackskomplexitaet

### 6. Temperierung (Tempering Station)

| Parameter | Wert |
|-----------|------|
| Verarbeitungszeit | 800 Ticks (40 Sekunden) |
| Eingabe | Conchierte Schokolade |
| Ausgabe | Temperierte Schokolade |

- Erzeugt kristalline Struktur
- Sorgt fuer glatte Textur und Bruch

### 7. Formgebung (Molding Station)

| Groesse | Kapazitaet | Ticks/Form |
|---------|-----------|-----------|
| Klein | 16 | 200 |
| Mittel | 24 | 150 |
| Gross | 32 | 100 |

- Eingabe: Temperierte Schokolade
- Ausgabe: Schokoladentafeln (geformt)

### 8. Kuehlung (Cooling Tunnel)

| Parameter | Wert |
|-----------|------|
| Verarbeitungszeit | 400 Ticks (20 Sekunden) |
| Eingabe | Geformte Schokolade |
| Ausgabe | Erstarrte Schokoladentafeln |

- Verhindert Fettreif (Blooming)

### 9. Ueberziehen (Enrobing Machine) - Optional

- Eingabe: Schokoladenkerne + Ueberzug-Schokolade
- Ausgabe: Ueberzogene Schokolade (beschichtet/gefuellt)
- Premium-Schicht hinzufuegen

### 10. Verpackung (Wrapping Station)

- Eingabe: Fertige Schokoladentafeln
- Ausgabe: Verpackte Schokolade (Praesentation)
- Finale Qualitaetsanzeige

## Items

- Kakaoschoten
- Rohe Kakaobohnen
- Geroestete Kakaobohnen
- Kakao-Nibs
- Kakaomasse
- Kakaobutter
- Conchierte Schokolade
- Temperierte Schokolade
- Schokoladentafeln (geformt)
- Verpackte Schokoladentafeln

## Bloecke

| Block | Beschreibung |
|-------|-------------|
| roasting_station | Roest-Station |
| winnowing_machine | Schaelmaschine |
| grinding_mill | Mahlwerk |
| pressing_station | Press-Station |
| small/medium/large_conching_machine | Conchiermaschine (3 Groessen) |
| tempering_station | Temperierer |
| small/medium/large_molding_station | Formstation (3 Groessen) |
| cooling_tunnel | Kuehltunnel |
| enrobing_machine | Ueberzugsmaschine |
| wrapping_station | Verpackungsstation |
| chocolate_storage_cabinet | Schokoladen-Lagerschrank |
