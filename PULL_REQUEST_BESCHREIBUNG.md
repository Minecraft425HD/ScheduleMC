# Pull Request: Tabak-HUD & Pflanzen-Visualisierung

**Branch:** `claude/identify-optimizations-01Be6MZbYqD231A6sDJmCgY4`
**Base:** `main`

## PR erstellen:
1. Gehe zu: https://github.com/Minecraft425HD/ScheduleMC/compare/main...claude/identify-optimizations-01Be6MZbYqD231A6sDJmCgY4
2. Klicke "Create pull request"
3. Kopiere Titel + Beschreibung unten

---

## Titel
```
Tabak-HUD & Pflanzen-Visualisierung (8 Wachstumsstufen)
```

---

## Beschreibung

### 🌱 Tabak-Pflanzen Visualisierung & HUD

#### 📱 HUD-Overlay (TobaccoPotHudOverlay)
- **Position:** Zentriert im Bildschirm (Mitte, leicht darunter)
- **Design:** Klein, kompakt, halbtransparent (40% Opazität)
- **Größe:** 100×30 Pixel Box
- **Anzeige:**
  - Pflanzentyp (farbig)
  - Wachstums-Balken (80×6 Pixel)
  - Prozent-Anzeige
  - Grün wenn reif, Gelb während Wachstum
- **Trigger:** Erscheint automatisch beim Anschauen einer Pflanze

#### 🌿 Pflanzen-Blöcke (TobaccoPlantBlock)
- **8 Wachstumsstufen** (0-7) statt vorher 4
- **X-förmiges Modell** (Cross-Block wie normale Minecraft-Pflanzen)
- **Keine Kollision** - Spieler kann durchlaufen
- **Dynamische Höhe:**
  - Stufe 0-3: 1 Block hoch
  - Stufe 4-7: 2 Blöcke hoch (füllt exakt 2 Blöcke aus)
- **4 Pflanzentypen:** Virginia, Burley, Oriental, Havana
- **Wachsen nur in Töpfen** (TobaccoPotBlock required)
- **Wachstum:** Gesteuert durch TobaccoPotBlockEntity

#### 💎 Drop-Mechanik
- **Ernten durch Abbauen** (Linke Maustaste)
- **Ertrag nach Wachstumsstufe:**
  - Stufe 7: Voller Ertrag + Qualität GUT
  - Stufe 4-6: 50% Ertrag + Qualität SCHLECHT
  - Stufe 0-3: Kein Drop
- **Kein Shift+Rechtsklick mehr nötig**

---

### 🔧 Geänderte Systeme

#### TobaccoPlantData
- `growthStage`: 0-7 (war 0-3)
- `isFullyGrown()`: `>= 7` (war `>= 3`)
- `tick()`: `ticksPerStage = ticks / 8` (war `/4`)
- Prozent-Berechnung: `stage × 100 / 7`

#### TobaccoPotBlockEntity
- Update Pflanzen-Block bei Wachstums-Änderung
- Ruft `TobaccoPlantBlock.growToStage()` auf wenn Stufe sich ändert
- Synchronisiert visuellen Block mit Daten

#### TobaccoPotBlock
- **Chat-Übersicht entfernt** - Kein showInfo() mehr bei Rechtsklick
- Nur noch Item-Interaktionen:
  - Erde befüllen (SoilBagItem)
  - Gießen (WateringCanItem)
  - Pflanzen (TobaccoSeedItem)

---

### 📁 Neue Dateien

**Java-Klassen:**
- `TobaccoPlantBlock.java` - Pflanzen-Block mit AGE & DOUBLE_BLOCK_HALF Properties
- `TobaccoPotHudOverlay.java` - Client-seitiges HUD-Rendering

**Blockstates:**
- `virginia_plant.json`
- `burley_plant.json`
- `oriental_plant.json`
- `havana_plant.json`

**JSON-Modelle:** 64 Dateien
- 4 Pflanzen × 8 Stufen × 2 Höhen (lower/upper)
- Format: `[pflanze]_plant_stage[0-7].json` + `_top.json`

---

### ⚠️ Texturen benötigt

**64 PNG-Texturen fehlen noch:**
```
textures/block/virginia_plant_stage[0-7].png
textures/block/virginia_plant_stage[0-7]_top.png
textures/block/burley_plant_stage[0-7].png
textures/block/burley_plant_stage[0-7]_top.png
textures/block/oriental_plant_stage[0-7].png
textures/block/oriental_plant_stage[0-7]_top.png
textures/block/havana_plant_stage[0-7].png
textures/block/havana_plant_stage[0-7]_top.png
```

**Details siehe:** `textures/block/PFLANZEN_TEXTUREN_README.txt`

Ohne Texturen werden Pflanzen als **Missing Texture** (Magenta/Schwarz) angezeigt.

---

### 📊 Commits

1. **afca9bd** - Kritischer Bug behoben: PlotID-Mismatch
2. **6aa6ea5** - Tabak-HUD & Pflanzen-Visualisierung implementiert
3. **ccf6168** - Töpfe entfernt - Pflanzen wachsen direkt auf dem Boden
4. **f5b9965** - HOTFIX: Töpfe wieder als Pflanz-Voraussetzung
5. **11cb7eb** - Chat-Übersicht bei Topf-Rechtsklick entfernt

---

### ✅ Test-Checklist

- [x] HUD erscheint beim Anschauen von Pflanzen
- [x] HUD ist klein, zentriert, halbtransparent
- [x] Pflanzen wachsen nur in Töpfen
- [x] 8 Wachstumsstufen funktionieren (0-7)
- [x] Drop-Mechanik beim Abbauen
- [x] 2-Block-Höhe ab Stufe 4
- [x] Keine Kollision mit Pflanzen
- [x] Keine Chat-Übersicht mehr bei Topf-Rechtsklick
- [ ] Texturen hinzufügen (64 PNG-Dateien)

---

### 🎮 Gameplay-Flow

**Vorher:**
1. Samen in Topf pflanzen
2. Rechtsklick auf Topf → Chat-Übersicht
3. Shift+Rechtsklick zum Ernten

**Jetzt:**
1. Samen in Topf pflanzen
2. Auf Pflanze schauen → Kleines HUD zeigt Fortschritt
3. Pflanze abbauen → Blätter droppen automatisch

---

### 🔄 Breaking Changes

- **Shift+Rechtsklick Ernte entfernt** - Jetzt durch Abbauen
- **Chat-Übersicht entfernt** - Nur noch HUD
- **Wachstumsstufen geändert** - 0-7 statt 0-3 (bestehende Pflanzen müssen evtl. neu gepflanzt werden)
