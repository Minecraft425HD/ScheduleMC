# Honig-System

> Imkerei, Verarbeitung und Reifung mit 4 Honigsorten

## Ueberblick

Das Honig-System umfasst eine vollstaendige Produktionskette von der Bienenzucht bis zum abgefuellten Honig. Es bietet 4 Honigsorten, 3 Verarbeitungsmethoden, 4 Reifungsstufen und ein Temperaturbonus-System.

## Honigsorten

| Sorte | Farbe | Basispreis/kg | Reifezeit | Qualitaetsfaktor | Freischaltung |
|-------|-------|-------------|-----------|------------------|---------------|
| Akazie | Gelb | 12 EUR | 30 Tage | 0,9 | Level 1 |
| Wildblume | Orange | 15 EUR | 60 Tage | 1,0 | Level 5 |
| Wald | Rot | 20 EUR | 90 Tage | 1,2 | Level 8 |
| Manuka | Magenta | 35 EUR | 120 Tage | 1,5 | Level 18 |

## Qualitaetsstufen

| Qualitaet | Farbe | Preis-Multiplikator |
|-----------|-------|---------------------|
| SCHLECHT | Rot | 0,7x |
| GUT | Gelb | 1,0x |
| SEHR GUT | Gruen | 1,5x |
| LEGENDAER | Gold | 2,5x |

## Reifungsstufen

| Stufe | Zeitraum | Preis-Multiplikator | Beschreibung |
|-------|---------|---------------------|-------------|
| FRISCH | 0-30 Tage | 1,0x | Leichtes Blumenaroma |
| REIF | 31-90 Tage | 1,2x | Ausgewogene Aromen |
| GEALTERT | 91-180 Tage | 1,4x | Komplexe Aromen |
| VINTAGE | 181+ Tage | 1,7x | Aussergewoehnliche Tiefe |

## Verarbeitungsmethoden

| Methode | Preis-Multiplikator | Verarbeitungszeit | Beschreibung |
|---------|---------------------|-------------------|-------------|
| FLUESSIG | 1,0x | 200 Ticks (10 Sek.) | Standard-Extraktion |
| CREMIG | 1,2x | 600 Ticks (30 Sek.) | Kontrollierte Kristallisation |
| STUECK | 1,4x | 400 Ticks (20 Sek.) | Mit Wabenstuecken |

## Temperaturbonus

| Temperaturbereich | Modifikator |
|-------------------|-------------|
| 15-25 Grad C | +20% (perfekt) |
| 10-30 Grad C | +10% (gut) |
| 5-35 Grad C | Normal (1,0x) |
| Unter 5 oder ueber 35 Grad C | -20% (schlecht) |

## Produktionskette

### 1. Bienenzucht

| Block | Ertrag pro Zyklus |
|-------|-------------------|
| Bienenstock | 1-2 Waben |
| Erweiterter Bienenstock | 2-3 Waben |
| Imkerei-Station (Apiary) | 3-4 Waben |

### 2. Extraktion

#### Honig-Extraktor

| Parameter | Wert |
|-----------|------|
| Kapazitaet | 64 Waben |
| Verarbeitungszeit | 400 Ticks (20 Sek.) |
| Umrechnung | 4 Waben = 1 Honig-Eimer |
| Nebenprodukt | 2 Bienenwachs pro Charge |

#### Zentrifugal-Extraktor

| Parameter | Wert |
|-----------|------|
| Kapazitaet | 64 Waben |
| Verarbeitungszeit | 200 Ticks (10 Sek.) - 2x schneller |
| Umrechnung | 4 Waben = 1 Honig-Eimer |
| Nebenprodukt | 2 Bienenwachs pro Charge |

### 3. Verarbeitung

#### Filter-Station

- Entfernt Verunreinigungen
- Verbessert Klarheit

#### Verarbeitungs-Station

- Wendet gewaehlte Methode an (Fluessig, Cremig, Stueck)
- Methoden-Multiplikator wird angewendet

#### Cremig-Station

- Spezifisch fuer cremigen Honig
- Kontrollierte Kristallisation
- Erzeugt glatte Textur

### 4. Reifung (Aging Chamber)

| Groesse | Kapazitaet |
|---------|-----------|
| Klein | 12 |
| Mittel | 18 |
| Gross | 24 |

- Automatische Reifung (1 Tick = 1 Reifungs-Tick)
- Qualitaet verbessert sich ueber Zeit
- Durchlaeuft Reifungsstufen (Frisch → Reif → Gealtert → Vintage)

### 5. Abfuellung (Bottling Station)

- Eingabe: Gereifter Honig
- Ausgabe: Honigglas (versiegelt, keine weitere Reifung)

## Items

- Rohe Honigwabe
- Roher Honig-Eimer
- Bienenwachs
- Fluessiger Honig
- Cremiger Honig
- Stueck-Honig
- Honigglaeser (abgefuellt)

## Bloecke

| Block | Beschreibung |
|-------|-------------|
| beehive | Bienenstock |
| advanced_beehive | Erweiterter Bienenstock |
| apiary | Imkerei-Station |
| honey_extractor | Honig-Extraktor |
| centrifugal_extractor | Zentrifugal-Extraktor |
| filtering_station | Filter-Station |
| processing_station | Verarbeitungs-Station |
| creaming_station | Cremig-Station |
| small/medium/large_aging_chamber | Reifungskammer (3 Groessen) |
| bottling_station | Abfuellstation |
| honey_storage_barrel | Honig-Lagerfass |
| honey_display_case | Honig-Vitrine |
