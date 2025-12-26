# LightMap Refactoring-Strategie

**Datum:** 26. Dezember 2025
**Ziel:** LightMap-Funktionalität beibehalten, rechtliche Risiken minimieren

---

## 🎯 Strategie-Übersicht

Es gibt drei Hauptstrategien, um mit dem LightMap-Problem umzugehen:

### Strategie A: Komplett-Entfernung ⭐ **EMPFOHLEN für sofortige Compliance**
### Strategie B: Clean-Room Reimplementation ⚠️ **Komplex, aber langfristig sicher**
### Strategie C: Minimal-Refactoring ⛔ **RISIKANT, nicht empfohlen**

---

## 📋 STRATEGIE A: Komplett-Entfernung + Alternative Integration

**Aufwand:** 1-2 Tage
**Rechtliches Risiko:** ✅ **KEIN Risiko**
**Funktionalität:** Minimap bleibt verfügbar über andere Mods

### Vorgehensweise:

#### 1. LightMap komplett entfernen

```bash
# Alle LightMap-Dateien löschen
rm -rf src/main/java/de/rolandsw/schedulemc/lightmap/
rm -rf src/main/resources/assets/schedulemc/lightmap/

# Alle Referenzen entfernen
grep -r "lightmap" --include="*.java" src/main/java/
# Diese Dateien manuell anpassen
```

#### 2. Integration mit existierenden Minimap-Mods

**Option A1: JourneyMap Integration (GPLv3-kompatibel)**
- JourneyMap hat öffentliche API
- Erlaubt Integration ohne Code-Kopie
- Große Community

**Option A2: Xaero's Minimap (Closed Source, aber API verfügbar)**
- Sehr beliebt
- API für Waypoints und Marker
- Keine Code-Integration nötig

**Option A3: VoxelMap als optionale Dependency**
- Nutzer installieren VoxelMap separat
- ScheduleMC registriert Waypoints via API
- Keine direkte Code-Integration

### Vorteile:
- ✅ 100% rechtlich sicher
- ✅ Keine Wartung des Minimap-Codes
- ✅ Nutzer können bevorzugten Minimap-Mod wählen
- ✅ Schnelle Umsetzung

### Nachteile:
- ❌ Keine eigene Minimap-Implementierung
- ❌ Abhängigkeit von Drittanbieter-Mods

---

## 🛠️ STRATEGIE B: Clean-Room Reimplementation

**Aufwand:** 3-6 Wochen
**Rechtliches Risiko:** 🟡 **NIEDRIG** (wenn korrekt durchgeführt)
**Funktionalität:** Volle Kontrolle, eigene Features möglich

### Was ist Clean-Room?

**Definition:** Code wird von Grund auf neu geschrieben, OHNE den Original-Code anzuschauen.

**Korrekte Durchführung:**

#### Team-Separation (idealerweise):
1. **Spezifikations-Team:** Analysiert Original, schreibt Feature-Specs
2. **Implementierungs-Team:** Liest NUR Specs, schreibt Code neu

**Solo-Variante (für dich):**
1. Funktions-Spezifikation aus Nutzer-Sicht schreiben
2. Original-Code NICHT mehr anschauen
3. Von Grund auf neu implementieren

### Schritt-für-Schritt Plan:

#### Phase 1: Spezifikation (1 Woche)

```markdown
# Minimap Feature-Spezifikation

## Kernfunktionen:
1. Live-Map Rendering
   - Zeigt Chunks um Spieler herum
   - Zoom-Level: 1x, 2x, 4x, 8x, 16x
   - Rotation: Nord immer oben ODER Spieler-orientiert

2. Block-Darstellung
   - Top-Down Ansicht der höchsten Blöcke
   - Biome-spezifische Farben
   - Wasser, Lava, Glas transparent

3. Entity-Anzeige
   - Spieler (grün)
   - NPCs (gelb)
   - Mobs (rot)
   - Tiere (weiß)

4. Waypoints
   - Erstellen, Benennen, Löschen
   - Farben und Icons
   - Distanz-Anzeige

5. World Map
   - Chunk-Persistence
   - Volle-Bildschirm-Ansicht
   - Pan und Zoom

6. Performance
   - Background Thread für Map-Generierung
   - Max 60 FPS mit Minimap aktiv
   - Max 100MB RAM-Nutzung
```

#### Phase 2: Architektur (3-5 Tage)

**Eigene Architektur entwerfen (NICHT VoxelMap kopieren!):**

```java
// Beispiel: Modulare Architektur

de.rolandsw.schedulemc.minimap/
├── core/
│   ├── MinimapCore.java           // Main Manager
│   ├── MapRenderer.java           // Rendering koordinieren
│   └── ChunkDataManager.java      // Chunk-Daten verwalten
├── rendering/
│   ├── MinimapWidget.java         // HUD Widget
│   ├── FullscreenMapScreen.java   // Fullscreen GUI
│   ├── BlockColorProvider.java    // Block -> Farbe
│   └── EntityIconRenderer.java    // Entity-Icons
├── data/
│   ├── MapChunk.java              // Chunk-Daten-Struktur
│   ├── Waypoint.java              // Waypoint-Daten
│   └── MapSaveManager.java        // Persistence
├── background/
│   ├── ChunkScanThread.java       // Background-Scanning
│   └── MapGenerationTask.java     // Map-Generierung
└── config/
    └── MinimapConfig.java         // Einstellungen
```

**Wichtig:** Diese Struktur ist KOMPLETT ANDERS als VoxelMap!

#### Phase 3: Implementierung (2-3 Wochen)

**Eigene Implementierungsansätze:**

**1. Block-Farben (ANDERS als VoxelMap):**

```java
// Statt komplexer Block-Cache: Einfaches Color-Mapping
public class BlockColorProvider {
    private static final Map<Block, Integer> BLOCK_COLORS = new HashMap<>();

    static {
        // Direkte Block -> Farbe Zuordnung
        BLOCK_COLORS.put(Blocks.GRASS_BLOCK, 0x7CBD6B);
        BLOCK_COLORS.put(Blocks.STONE, 0x808080);
        // ... etc
    }

    public static int getColor(BlockState state, Biome biome) {
        // Eigene Logik: Biome-Tinting
        int baseColor = BLOCK_COLORS.getOrDefault(state.getBlock(), 0xFF00FF);
        return applyBiomeTint(baseColor, biome);
    }
}
```

**2. Map-Rendering (ANDERS als VoxelMap):**

```java
// Statt komplexer DynamicTexture: Einfaches BufferedImage
public class MapRenderer {
    private BufferedImage mapImage;
    private int centerChunkX, centerChunkZ;
    private int zoom = 1;

    public void render(GuiGraphics graphics, int x, int y, int size) {
        // Eigene Logik: Direct pixel manipulation
        updateMapImage(); // Background thread hat Daten vorbereitet

        // Blit to screen
        NativeImage nativeImage = NativeImage.fromBufferedImage(mapImage);
        DynamicTexture texture = new DynamicTexture(nativeImage);

        RenderSystem.setShaderTexture(0, texture.getId());
        graphics.blit(x, y, 0, 0, size, size, size, size);
    }
}
```

**3. Chunk-Scanning (ANDERS als VoxelMap):**

```java
// Statt komplexer Heightmap-Berechnung: Einfacher Top-Down Scan
public class ChunkScanner {

    public MapChunk scanChunk(LevelChunk chunk) {
        MapChunk result = new MapChunk(chunk.getPos());

        // Eigene Logik: Simple iteration
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Von oben nach unten scannen
                int topY = findTopBlock(chunk, x, z);
                BlockState state = chunk.getBlockState(new BlockPos(x, topY, z));

                result.setPixel(x, z, BlockColorProvider.getColor(state,
                    chunk.getBiome(x, topY, z)));
            }
        }

        return result;
    }

    private int findTopBlock(LevelChunk chunk, int x, int z) {
        // Eigene Implementierung: Von oben scannen
        for (int y = chunk.getHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
            BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
            if (!state.isAir() && !state.is(BlockTags.LEAVES)) {
                return y;
            }
        }
        return chunk.getMinBuildHeight();
    }
}
```

#### Phase 4: Assets (3-5 Tage)

**EIGENE Texturen erstellen:**

- ❌ NICHT die VoxelMap-Icons kopieren!
- ✅ Eigene Icons in Pixelart erstellen
- ✅ Eigene GUI-Designs
- ✅ Eigene Übersetzungen schreiben

**Tools für eigene Assets:**
- Aseprite (Pixel Art)
- GIMP (Icons)
- Eigene Texte (nicht übersetzen von VoxelMap!)

#### Phase 5: Testing & Polish (1 Woche)

- Performance-Tests
- Edge-Case Handling
- Config-Integration

### Vorteile:
- ✅ Rechtlich sicher (wenn korrekt durchgeführt)
- ✅ Volle Kontrolle über Features
- ✅ Kann besser auf ScheduleMC zugeschnitten werden
- ✅ Eigene Performance-Optimierungen

### Nachteile:
- ❌ 3-6 Wochen Entwicklungszeit
- ❌ Muss alle Features selbst implementieren
- ❌ Potential für Bugs in neuer Implementation

### Rechtliche Absicherung:

**Dokumentation führen:**
```markdown
# Clean-Room Implementation Log

## 2025-12-27: Spezifikation
- Features basierend auf Nutzeranforderungen definiert
- KEINE Code-Analyse des Originals
- Nur Funktionalität aus Nutzer-Sicht beschrieben

## 2025-12-28: Architektur
- Eigene Paket-Struktur entworfen
- Modular aufgebaut: core, rendering, data, background
- Komplett anders als VoxelMap-Architektur

## 2025-12-29 - 2026-01-15: Implementation
- Code von Grund auf geschrieben
- Eigene Algorithmen für Chunk-Scanning
- Eigene Rendering-Pipeline
- KEIN Original-Code konsultiert
```

---

## ⚠️ STRATEGIE C: Minimal-Refactoring (NICHT EMPFOHLEN)

**Aufwand:** 1 Woche
**Rechtliches Risiko:** 🔴 **HOCH** - Immer noch derivative work
**Funktionalität:** Gleich wie jetzt

### Was viele denken, dass es hilft (tut es aber NICHT):

❌ **Umbenennen von Klassen**
```java
// VoxelMap: MinimapRenderer → ScheduleMinimapRenderer
// IMMER NOCH derivative work!
```

❌ **Code umstrukturieren**
```java
// Code in andere Methoden aufteilen, Variablen umbenennen
// IMMER NOCH derivative work!
```

❌ **Kommentare entfernen/ändern**
```java
// Hilft rechtlich GAR NICHT
```

### Warum das NICHT funktioniert:

**Copyright schützt:**
1. Die **Struktur** des Codes
2. Die **Architektur** und Organisation
3. Die **Algorithmen** und Logik
4. Die **kreative Auswahl** und Anordnung

**NICHT nur:**
- Variablennamen
- Kommentare
- Formatting

### Das Problem:

Der Git-Commit-Historie zeigt bereits:
```
151dbb8: "feat: Integrate LightMapmod - Replace Minimap/Map"
```

**Rechtlich beweist dies:**
- Absichtliches Kopieren
- Bewusstsein über die Quelle
- "Willful infringement" (absichtliche Verletzung)

→ **Schadenersatz kann verdreifacht werden** bei willful infringement!

### Warum ich davon abrate:

1. ⛔ **Rechtlich fast genauso riskant** wie Original behalten
2. ⛔ **Verschleierung** kann als bad faith gewertet werden
3. ⛔ **Git-Historie** bleibt als Beweis
4. ⛔ **Code-Ähnlichkeit** bleibt nachweisbar (Forensic-Tools)

---

## 🎯 Empfohlene Strategie: HYBRID-ANSATZ

**Kombination aus A + B für besten Outcome:**

### Phase 1: Sofort (Tag 1-7) - STRATEGIE A

1. **Aktuellen LightMap-Code komplett entfernen**
   - Eliminiert rechtliches Risiko SOFORT
   - Zeigt good faith

2. **Kurzzeitige Lösung: Empfehlung in README**
   ```markdown
   ## Empfohlene Companion-Mods

   ScheduleMC funktioniert optimal mit:
   - **JourneyMap** - Minimap und World Map
   - **Xaero's Minimap** - Leichtgewichtige Alternative
   ```

### Phase 2: Mittelfristig (Monat 1-2) - STRATEGIE B

3. **Clean-Room Minimap entwickeln**
   - Zeitdruck weg, da temporary solution existiert
   - Kann sorgfältig und korrekt gemacht werden
   - Fokus auf ScheduleMC-spezifische Features:
     - Plot-Grenzen anzeigen
     - Territory-Markierungen
     - NPC-Positionen
     - Illegal Activity-Zonen

4. **Eigene Features, die VoxelMap NICHT hat**
   - Integration mit ScheduleMC-Systeme
   - Plot-Management im Map-Screen
   - Wirtschafts-Overlay (Shops, ATMs)
   - Territory-Konflikte visualisieren

**Vorteil:** Wird BESSER als VoxelMap für ScheduleMC!

---

## 📋 Konkrete Umsetzungs-Schritte (HYBRID)

### Woche 1: Cleanup

```bash
# 1. Branch für Cleanup erstellen
git checkout -b feature/remove-lightmap

# 2. LightMap-Code entfernen
rm -rf src/main/java/de/rolandsw/schedulemc/lightmap/
rm -rf src/main/resources/assets/schedulemc/lightmap/

# 3. Alle Referenzen finden und entfernen
grep -r "import.*lightmap" --include="*.java" src/main/java/
# Betroffene Dateien anpassen

# 4. Commit mit klarer Message
git commit -m "refactor: Remove integrated LightMap code for license compliance

The LightMap/VoxelMap code was integrated without proper licensing.
Removed to ensure GPL-3.0 compliance.

Users should install a compatible minimap mod separately:
- JourneyMap (recommended)
- Xaero's Minimap
- VoxelMap

Future: Clean-room implementation of minimap tailored to ScheduleMC
is planned for Q1 2026."

# 5. Push und PR erstellen
git push origin feature/remove-lightmap
```

### Woche 2-3: README Update + Companion Mod Integration

```java
// Optional: JourneyMap API Integration (wenn gewünscht)
// src/main/java/de/rolandsw/schedulemc/integration/JourneyMapIntegration.java

@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class JourneyMapIntegration {

    // Nur API-Calls, kein Code-Kopie
    public static void registerPlotWaypoints() {
        if (isJourneyMapLoaded()) {
            for (PlotRegion plot : PlotManager.getPlots()) {
                // JourneyMap API nutzen
                JourneyMapAPI.addWaypoint(
                    plot.getCenter(),
                    "Plot: " + plot.getPlotId(),
                    plot.hasOwner() ? Color.GREEN : Color.RED
                );
            }
        }
    }
}
```

### Monat 2-3: Clean-Room Minimap

```java
// Komplett neue Implementation
// src/main/java/de/rolandsw/schedulemc/map/

// Eigene, ScheduleMC-spezifische Features:
public class ScheduleMCMap {

    // Feature 1: Plot-Overlay
    public void renderPlotBoundaries(GuiGraphics graphics) {
        // Zeigt Plot-Grenzen direkt auf Map
    }

    // Feature 2: Territory-System Integration
    public void renderTerritories(GuiGraphics graphics) {
        // Färbt Territorien basierend auf Type
    }

    // Feature 3: Economy-Overlay
    public void renderEconomyPoints(GuiGraphics graphics) {
        // Zeigt Shops, ATMs, Warehouses
    }

    // Feature 4: NPC-Tracking
    public void renderNPCPositions(GuiGraphics graphics) {
        // Zeigt NPCs mit Verhaltens-Icons
    }
}
```

---

## 💰 Kosten-Nutzen-Analyse

| Strategie | Aufwand | Risiko | Funktionalität | Langfristig |
|-----------|---------|--------|----------------|-------------|
| **A: Entfernung** | 1-2 Tage | ✅ Kein | ⚠️ Abhängig | 🟡 Mittel |
| **B: Clean-Room** | 3-6 Wochen | 🟡 Niedrig | ✅ Voll | ✅ Sehr gut |
| **C: Refactoring** | 1 Woche | 🔴 Hoch | ✅ Gleich | ❌ Schlecht |
| **HYBRID (A+B)** | 2 Wochen + 6 Wochen | ✅ Kein | ✅ Besser | ✅ Exzellent |

---

## 🎓 Rechtliche Fallstricke vermeiden

### ✅ RICHTIG - Clean Room:

```java
// 1. Spezifikation schreiben (ohne Code anzuschauen)
/**
 * Minimap soll folgendes können:
 * - Zeige Spieler-Position als grüner Punkt
 * - Zeige Chunks im Radius von 5 Chunks
 * - Update alle 500ms
 */

// 2. Implementation von Grund auf
public class MinimapWidget extends AbstractWidget {
    private final int radius = 5; // Eigene Entscheidung

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Komplett eigene Logik
        renderBackground(graphics);
        renderChunks(graphics);
        renderPlayer(graphics);
    }
}
```

### ❌ FALSCH - Derivative Work:

```java
// VoxelMap Code kopieren und nur ändern:
public class MinimapRenderer { // Gleicher Name
    private DynamicMoveableTexture mapImage; // Gleiche Struktur

    public void onTickInGame(GuiGraphics ctx) { // Gleiche Methode
        // Gleiche Logik, nur Variablen umbenannt
        this.calcLightAndSky(); // Gleicher Algorithmus
    }
}
```

---

## 📞 Nächste Schritte - DEINE ENTSCHEIDUNG

Du hast jetzt drei Optionen:

### Option 1: Schnell & Sicher (EMPFOHLEN)
```bash
# HEUTE: LightMap entfernen
# NÄCHSTE WOCHE: README mit Mod-Empfehlungen
# IN 2-3 MONATEN: Eigene Clean-Room Implementation
```
→ **Soll ich das jetzt umsetzen?**

### Option 2: Nur Clean-Room
```bash
# AB HEUTE: 6 Wochen Entwicklung
# Risiko bleibt bis fertig
```
→ **Möchtest du, dass ich Clean-Room Spezifikation erstelle?**

### Option 3: Erlaubnis einholen
```bash
# HEUTE: E-Mail an MamiyaOtaru
# WARTEN: Auf Antwort (kann Wochen dauern oder nie kommen)
```
→ **Soll ich E-Mail-Vorlage erstellen?**

---

## 🎯 Meine klare Empfehlung:

**HYBRID-Ansatz:**
1. **JETZT SOFORT:** LightMap entfernen (1 Tag)
2. **DIESE WOCHE:** README Update (1 Tag)
3. **AB NÄCHSTE WOCHE:** Clean-Room Spezifikation (1 Woche)
4. **AB MONAT 2:** Clean-Room Implementation (4-6 Wochen)

**Vorteile:**
- ✅ Rechtlich sicher AB SOFORT
- ✅ Nutzer haben temporary solution (andere Mods)
- ✅ Langfristig BESSERE Lösung (ScheduleMC-spezifisch)
- ✅ Zeigt Professionalität und good faith

---

**Was möchtest du tun?**
