# Territorium-System

> Chunk-basierte Gebietskontrolle fuer Gangs

## Ueberblick

Das Territorium-System ermoeglicht Gangs die Kontrolle ueber Chunks (16x16-Block-Gebiete) auf der Karte. Territorien bieten wirtschaftliche Vorteile, Schutz vor Verbrechen und strategische Kontrolle. Territorien werden visuell auf einer Karte mit farbcodierten Zonen dargestellt.

## Grundlagen

### Territorium-Eigenschaften

| Eigenschaft | Beschreibung |
|---|---|
| Groesse | 1 Chunk (16x16 Bloecke) |
| Besitzer | Gang-UUID oder Spieler-UUID |
| Name | Benutzerdefinierter Name |
| Typ | Farbcodiert (10 Farben) |
| Erstellungszeitpunkt | Zeitstempel |

### Territorium-Farben

| Farbe | Hex-Code | Chat-Code |
|-------|----------|-----------|
| Rot | #FF4444 | &c |
| Gruen | #44FF44 | &a |
| Orange | #FFAA00 | &6 |
| Blau | #4444FF | &9 |
| Gelb | #FFFF44 | &e |
| Lila | #FF44FF | &d |
| Cyan | #44FFFF | &b |
| Grau | #AAAAAA | &7 |
| Dunkelrot | #AA0000 | &4 |
| Limette | #88FF44 | &2 |

## Territorium-Limits

Die Anzahl kontrollierbarer Chunks haengt vom Gang-Level und freigeschalteten Perks ab:

| Gang-Level | Basis-Chunks | Mit Perks |
|-----------|-------------|-----------|
| 1-7 | 1 | 1 |
| 8-14 | 4 | 9 (TERRITORY_EXPAND) |
| 15-21 | 9 | 16 (TERRITORY_DOMINANCE) |
| 22-27 | 16 | 25 (TERRITORY_STRONGHOLD) |
| 28-30 | 25 | Unbegrenzt (TERRITORY_EMPIRE) |

## Vorteile

### Wirtschaftliche Vorteile

- **ECONOMY_TAX Perk**: +5% Einnahmen aus Shops im eigenen Gebiet
- **ECONOMY_MONOPOLY Perk**: +15% Verkaufsbonus im eigenen Gebiet
- **Territorium-XP**: 2 XP pro gehaltenem Chunk pro Tag

### Kriminalitaets-Vorteile

- **CRIME_PROTECTION Perk**: Fahndungslevel sinkt 20% schneller
- **CRIME_INTIMIDATION Perk**: NPCs melden keine Verbrechen
- **CRIME_UNTOUCHABLE Perk**: Max Fahndungslevel auf 3 begrenzt

### Benachrichtigungen

- **TERRITORY_FORTIFY Perk**: Benachrichtigung wenn fremde Gang-Mitglieder das Gebiet betreten

## Karten-Editor

Der Karten-Editor ist ein visuelles Werkzeug zum Verwalten von Territorien:

```
/map edit    # Karten-Editor oeffnen (OP Level 2)
/map info    # Territorium-Statistiken anzeigen
```

### Editor-Funktionen

- Visueller Chunk-basierter Gebietseditor
- Territorium-Typ Auswahl (10 Farben)
- Benutzerdefinierte Benennung
- Echtzeit-Synchronisation zwischen Server und Client
- Anzeige vorhandener Grundstuecke, NPCs und Schloesser

## Netzwerk-Synchronisation

| Paket | Richtung | Beschreibung |
|-------|----------|-------------|
| OpenMapEditorPacket | Server zu Client | Editor auf Client oeffnen |
| SyncTerritoriesPacket | Server zu Client | Vollstaendige Territorium-Synchronisation |
| SyncTerritoryDeltaPacket | Server zu Client | Inkrementelle Updates |

## Datenspeicherung

**Datei**: `config/plotmod_territories.json`

Territorien werden mit Position, Typ, Name, Besitzer und Zeitstempeln gespeichert.
