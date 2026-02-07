# Kaese-System

> Kaeseherstellung mit Pasteurisierung, Reifung und 4 Sorten

## Ueberblick

Das Kaese-System bietet eine vollstaendige Produktionskette von der Milch bis zum verpackten Kaese. Es umfasst 4 Kaesesorten, 3 Verarbeitungsmethoden (Natur, Geraeuchert, Kraeuter), 4 Reifungsstufen und eine detaillierte Temperatur-Steuerung.

## Kaesesorten

| Sorte | Farbe | Basispreis/kg | Reifezeit | Qualitaetsfaktor | Freischaltung |
|-------|-------|-------------|-----------|------------------|---------------|
| Gouda | Gelb | 15 EUR | 30 Tage | 1,0 | Level 3 |
| Emmentaler | Orange | 22 EUR | 35 Tage | 1,1 | Level 6 |
| Camembert | Weiss | 28 EUR | 25 Tage | 1,2 | Level 11 |
| Parmesan | Rot | 35 EUR | 40 Tage | 1,3 | Level 16 |

## Qualitaetsstufen

| Qualitaet | Farbe | Preis-Multiplikator |
|-----------|-------|---------------------|
| SCHLECHT | Rot | 0,7x |
| GUT | Gelb | 1,0x |
| SEHR GUT | Gruen | 2,0x |
| LEGENDAER | Magenta | 4,0x |

## Reifungsstufen

| Stufe | Mindest-Ticks | Preis-Multiplikator |
|-------|-------------|---------------------|
| FRISCH | 0 | 1,0x |
| JUNG | 600 (30 Tage) | 1,3x |
| REIF | 1.800 (90 Tage) | 1,8x |
| GEALTERT | 3.600 (180 Tage) | 2,5x |

## Verarbeitungsmethoden

| Methode | Preis-Multiplikator | Freischaltung |
|---------|---------------------|---------------|
| NATUR | 1,0x | Standard |
| GERAEUCHERT | 1,4x | Level 10 |
| KRAEUTER | 1,6x | Level 20 |

## Produktionskette

### 1. Pasteurisierung (Pasteurization Station)

| Parameter | Wert |
|-----------|------|
| Eingabe | Rohe Milcheimer |
| Ausgabe | Pasteurisierte Milcheimer |
| Verarbeitungszeit | 400 Ticks (20 Sekunden) |
| Temperatur | 65 Grad C kontrolliert |

### 2. Dicklegung (Curdling Vat)

| Parameter | Wert |
|-----------|------|
| Kapazitaet | 16 Milcheimer |
| Zusatz | Lab (1 pro Charge) |
| Verarbeitungszeit | 600 Ticks (30 Sekunden) |
| Ausgabe | 4 Kaesereien pro Milcheimer |
| Qualitaet | Wird bei Fertigstellung bestimmt |

### 3. Pressen (Cheese Press)

| Groesse | Kapazitaet | Ticks/Bruch |
|---------|-----------|-------------|
| Klein | 16 | 400 |
| Mittel | 22 | 300 |
| Gross | 28 | 200 |

- Eingabe: Kaesereien
- Ausgabe: Frischer Kaeselaib (0,25 kg pro Bruch)
- Gewichtsberechnung: Anzahl x 0,25 kg

### 4. Reifung (Aging Cave)

| Groesse | Kapazitaet |
|---------|-----------|
| Klein | 16 |
| Mittel | 22 |
| Gross | 28 |

- Temperaturkontrollierte automatische Reifung
- Qualitaet wird ueber den Reifungsprozess erhalten
- Kaese durchlaeuft Reifungsstufen (Frisch → Jung → Reif → Gealtert)

### 5. Verpackung (Packaging Station)

- Eingabe: Gereifter Kaeselaib
- Ausgabe: Verpackter Kaese (Portionsgroessen)
- Finale Qualitaetsanzeige

## Items

- Milcheimer
- Pasteurisierte Milch
- Lab
- Kaesereien (qualitaetsabhaengig)
- Kaeselaib (Typ und Reifungsstufe gespeichert)
- Kaesekeil (verpackte Portionen)

## Bloecke

| Block | Beschreibung |
|-------|-------------|
| pasteurization_station | Pasteurisierungs-Station |
| curdling_vat | Dicklegungs-Bottich |
| small/medium/large_cheese_press | Kaesepresse (3 Groessen) |
| small/medium/large_aging_cave | Reifungshoehle (3 Groessen) |
| packaging_station | Verpackungsstation |
