# MapView-System

> Ingame-Kartenrenderer mit Navigation und Gebietsanzeige

## Ueberblick

Das MapView-System bietet einen fortgeschrittenen Ingame-Kartenrenderer mit Minimap, Weltkarte, Wegpunkt-Navigation und Gebietsanzeige. Es verwendet eine modulare, service-orientierte Architektur mit ueber 120 Dateien.

## Funktionen

### Minimap

- Echtzeit-Darstellung der Umgebung in einer Ecke des Bildschirms
- Anzeige von Terrain, Bloecken, Biomen und Hoehe
- NPC-Positionen auf der Karte
- Territorium-Overlay (optional)
- Konfigurierbare Groesse und Position

### Weltkarte

- Vollbild-Kartenansicht
- Zoom-Stufen (0-4)
- Erkundete Regionen werden gespeichert
- NPC- und Entitaets-Rendering

### Navigation

- **Strassen-Navigation**: Automatische Wegfindung entlang von Strassen
- **A*-Pathfinding**: Optimale Route zwischen zwei Punkten
- **Wegpunkt-System**: Vereinfachte Pfadpunkte fuer die Anzeige
- **Echtzeitaktualisierung**: Pfad wird alle 1 Sekunde neu berechnet

#### Navigations-Parameter

| Parameter | Wert |
|-----------|------|
| Scan-Radius | 150 Bloecke |
| Pfad-Update-Intervall | 1.000 ms |
| Bewegungs-Schwelle | 10 Bloecke |
| Ankunftsentfernung | 5 Bloecke |
| Pfadabweichungs-Schwelle | 15 Bloecke |

#### Navigations-Events

- GRAPH_UPDATED: Strassennetz aktualisiert
- NAVIGATION_STARTED: Navigation gestartet
- NAVIGATION_STOPPED: Navigation gestoppt
- PATH_UPDATED: Pfad aktualisiert
- PATH_NOT_FOUND: Kein Pfad gefunden
- DESTINATION_REACHED: Ziel erreicht

## Rendering-Ebenen

| Ebene | Beschreibung |
|-------|-------------|
| Lightmap | Beleuchtungsschicht |
| Heightmap | Hoehenvisualisierung |
| Slopemap | Neigungsvisualisierung |
| Biomes | Biom-Farben |
| Water Transparency | Wasser-Durchsichtigkeit |
| Block Transparency | Block-Transparenz |
| World Border | Weltgrenze |
| Territories | Gebietsanzeige (optional) |

## Chunk-Scan-Strategien

Das System verwendet verschiedene Scan-Muster zum Verarbeiten von Chunks:

- **Grid-Scan**: Systematisches Raster-Muster
- **Spiral-Scan**: Spirale vom Zentrum nach aussen

Eine Factory waehlt automatisch die optimale Strategie.

## Tastenbelegung

| Taste | Funktion |
|-------|----------|
| Z | Zoom aendern |
| X | Vollbild umschalten |
| M | Menu oeffnen |

## Konfiguration

**Datei**: `config/mapview.properties`

### Anzeige-Einstellungen

| Einstellung | Standard | Beschreibung |
|-------------|---------|-------------|
| showUnderMenus | false | Karte unter Inventar anzeigen |
| zoom | 2 | Zoom-Stufe (0-4) |
| sizeModifier | 1 | Kartengroesse (-1 bis 4) |
| mapCorner | 1 | Ecken-Position (0-3) |
| oldNorth | false | Alte Nordrichtung |
| showTerritories | false | Gebiets-Overlay |

### Feature-Flags

| Flag | Standard | Beschreibung |
|------|---------|-------------|
| worldmapAllowed | true | Weltkarte aktiviert |
| minimapAllowed | true | Minimap aktiviert |
| multicore | System | Multi-Prozessor-Unterstuetzung |

## Architektur

### Service-Schicht

| Service | Aufgabe |
|---------|---------|
| MapDataManager | Zentrale Koordination aller Services |
| RenderCoordinationService | Rendering-Pipeline Verwaltung |
| WorldStateService | Welt-Status und Metadaten |
| LifecycleService | Lifecycle-Events (Join, Disconnect, Shutdown) |
| ColorCalculationService | Block- und Biom-Farbberechnung |
| DimensionService | Dimensions-Verwaltung (separate Karten pro Dimension) |
| ConfigNotificationService | Einstellungsaenderungen |

### Daten-Schicht

| Komponente | Aufgabe |
|-----------|---------|
| WorldMapData | Persistente Kartendaten |
| AsyncPersistenceManager | Hintergrund-Speicheroperationen |
| MapDataRepository | Datenzugriffs-Schicht |

### Navigations-Komponenten

| Komponente | Aufgabe |
|-----------|---------|
| RoadNavigationService | Haupt-Navigationskoordinator |
| RoadGraph | Strassennetzwerk mit A*-Algorithmus |
| RoadGraphBuilder | Welt-Scan fuer Strassenblöcke |
| RoadBlockDetector | Erkennung von Strassenblöcken |
| NavigationOverlay | Pfad-Anzeige auf der Karte |
| RoadPathRenderer | Detail-Pfad-Rendering |

### NPC-Integration

- NPCs werden auf der Minimap und Weltkarte angezeigt (NPCMapRenderer)
- Positionen werden in Echtzeit aktualisiert

### Dimensionen

- Separate Karten pro Dimension
- Automatische Dimensionserkennung
- Cache-Verwaltung pro Dimension

## Minecraft-Integration

Das System integriert sich ueber Mixins in Minecraft:
- MixinWorldRenderer (Welt-Rendering)
- MixinChatHud (Chat-Integration)
- MixinInGameHud (HUD-Integration)
- Forge-Event-System fuer Chunk-Updates und Weltwechsel
