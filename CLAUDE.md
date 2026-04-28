# ScheduleMC — Hinweise für Claude

Diese Datei enthält Entscheidungen und Ausschlüsse, die bei zukünftigen Analysen
oder Refactoring-Scans NICHT erneut vorgeschlagen werden sollen.

---

## Bewusst übersprungene Refactorings

### Beer/Wine size-variant Konsolidierung (Supplier-Pattern)
**Status:** ABGELEHNT — nicht erneut vorschlagen

**Betrifft:**
- `beer/blockentity/Abstract*BlockEntity` + Small/Medium/Large-Subklassen
  (BrewKettle, BeerFermentationTank, ConditioningTank)
- `wine/blockentity/Abstract*BlockEntity` + Small/Medium/Large-Subklassen
  (WinePress, AgingBarrel, FermentationTank)

**Begründung:**
Das Supplier-Pattern wurde in Sprint 6 erfolgreich auf die Tobacco-Klassen
angewendet (DryingRack, FermentationBarrel), weil deren Subklassen **ausschließlich**
2 Config-Getter (@Override) enthielten und vollständig eliminierbar waren.

Die Beer/Wine-Subklassen überschreiben dagegen **4 Methoden**:
- `getCapacity()` / `getSpeedMultiplier()` — Werte
- `getDisplayName()` — klassenindividueller Übersetzungsschlüssel
- `createMenu()` — klassenindividueller Menü-Typ

Da `getDisplayName()` und `createMenu()` pro Größe unterschiedlich sind, blieben
die Subklassen auch nach dem Refactoring bestehen (nur kürzer). Der Basiskonstruktor
würde durch ein `String`-Argument und eine `TriFunction`-Fabrik aufgebläht. Der
Aufwand überwiegt den Nutzen bei weitem. Die ~43-zeiligen Subklassen sind klar und
wartbar — so sollen sie bleiben.

---

### NPC God-Class Aufteilung (NPCData + CustomNPCEntity)
**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:**
- `npc/data/NPCData.java` (971 → 409 Zeilen)
- `npc/entity/CustomNPCEntity.java` (968 Zeilen)

**Umgesetzte Aufteilung von NPCData:**
- `NPCLocationData` — home, work, leisure (bis 10), assignedWarehouse
- `NPCShopData` — buyShop, sellShop
- `NPCScheduleData` — workStartTime, workEndTime, homeTime + isWithinWorkingHours()
- `NPCPoliceData` — policeStation, patrolPoints (bis 16), currentPatrolIndex, Timing
- `ShopInventory` / `ShopEntry` — als Top-Level-Klassen ausgelagert

**Abgeschlossen in:** Sprint auf Branch `claude/deepscan-repository-3Vkpv` (2026-03-24)
Alle 29 Call-Site-Dateien migriert. NBT-Keys identisch — Spielstände bleiben kompatibel.

---

## Dimension-System

**ScheduleMC nutzt genau eine Dimension (Overworld).**

- Es gibt keinen Nether- oder End-Support.
- Die Dimension-Switch-Logik in `PlotAppScreen.renderCurrentPlotTab()` (Overworld/Nether/End-Labels)
  ist toter Code — Nether/End werden nie aktiv sein.
- **Konsequenz:** Nicht vorschlagen, Multi-Dimension-Support zu ergänzen oder den Switch-Block
  zu einem echten Feature auszubauen. Der Code kann bei Bedarf auf den Overworld-Fall vereinfacht werden.
- **Festgehalten am:** 2026-04-16.

---

## Cannabis-Blöcke — Interaktionsmodell (ab 2026-04-17)

**Status:** IMPLEMENTIERT — Architekturentscheidung festhalten

**Betrifft:**
- `cannabis/blocks/TrimStationBlock` + `TrimStationBlockEntity`
- `cannabis/blocks/HashPressBlock`
- `cannabis/blocks/OilExtractorBlock`
- `cannabis/blocks/CuringJarBlock`

**Entscheidungen:**
- **Alle 4 Blöcke** haben jetzt visuelle Input/Output-Slots im GUI (render-only, kein ItemStackHandler).
- Items werden weiterhin direkt in den BlockEntity-Feldern gespeichert (kein Refactoring auf ItemStackHandler).
- `CuringJarBlock`: Interaktion unverändert (RK fill, Shift+RK extract).
- `HashPressBlock` und `OilExtractorBlock`: Extraktion auf **Shift+RK** umgestellt (war: plain RK).
- `TrimStationBlock`: komplett auf Maschinen-Slot-Basis umgestellt — RK mit DriedBud = füllen, Shift+RK = entnehmen; Output wird in BE-Feldern gepuffert statt direkt ins Spieler-Inventar gegeben.
- **Nicht vorschlagen:** TrimStation auf ItemStackHandler umzustellen — bewusst direktes Feld-Storage beibehalten.

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-17**.
- Diese Datei wurde im Rahmen der Cannabis-GUI-Überarbeitung erweitert.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

