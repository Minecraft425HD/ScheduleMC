# Auftrag an Claude #2 — unnötige Code-Dopplungen abbauen

**Repo:** `Minecraft425HD/ScheduleMC`  
**Vorbedingung:** Brief #1 (`docs/CLAUDE_IMPLEMENTATION_BRIEF.md`) ist unabhängig. Nicht in denselben PR mischen.  
**Autor der Analyse:** Grok, Quellscan `main` 2026-09-24  
**Ziel:** Duplikate entfernen, Verhalten unverändert.

Kein Gameplay-Change. Keine neuen Maschinen. Keine ID-Renames. `compileJava` muss grün bleiben.

---

## 0. Reihenfolge (verbindlich)

Drei PRs nacheinander:

| PR | Thema | Risiko | Wirkung |
|---|---|---|---|
| **2a** | Muster: **Brew Kettle** (Block + BE + Menu + Screen) | niedrig | Muster steht |
| **2b** | Dasselbe Muster auf alle S/M/L-Familien in Abschnitt 3 | mittel | ~195 Dateien weniger oder dünner |
| **2c** | `ProcessingMethodPacket` (5 Kopien) auf ein gemeinsames Paket | niedrig | 4 Dateien weniger |

Erst 2a mergen und im Spiel Small- + Large-Kettle prüfen. Dann 2b.

`registerBlockWithItem` ist **Brief #1**, hier nicht nochmal umbauen.

---

## 1. Befund

### 1.1 S/M/L-Klassen (Hauptproblem)

**195 Dateien** heißen `Small*` / `Medium*` / `Large*` / `Big*`.

BlockEntities haben oft schon ein `Abstract*BlockEntity`. Die drei Subklassen unterscheiden sich typischerweise nur:

```
capacity:  8 vs 16 vs 32
speed:     1.0 vs 1.5 vs 2.0
menu class / translation key / BlockEntityType
```

Beispiel `SmallBrewKettleBlockEntity` vs `LargeBrewKettleBlockEntity`: 43 Zeilen, ratio ~0.85.
Menus ratio ~0.97, Screens ~0.99.

Modul-Zählung (nur S/M/L/Big-Dateinamen): tobacco 39, beer 36, wine 36, cheese 24, chocolate 24, coca 12, coffee 12, honey 12.

Vorhandene Abstracts nutzen, nicht neu erfinden. U. a. `AbstractBrewKettleBlockEntity`, `AbstractBeerFermentationTankBlockEntity`, `AbstractConditioningTankBlockEntity`, `AbstractWinePressBlockEntity`, `AbstractFermentationTankBlockEntity`, `AbstractAgingBarrelBlockEntity`, `AbstractConchingMachineBlockEntity`, `AbstractMoldingStationBlockEntity`, `AbstractCheesePressBlockEntity`, `AbstractAgingCaveBlockEntity`, `AbstractExtractionVatBlockEntity`, `AbstractRefineryBlockEntity`, `AbstractCoffeeRoasterBlockEntity`, `AbstractAgingChamberBlockEntity`, `AbstractDryingRackBlockEntity`, `AbstractFermentationBarrelBlockEntity`, `AbstractPackagingTableBlockEntity`, plus die passenden `Abstract*Block` in tobacco.

### 1.2 ProcessingMethodPacket — 5 Kopien

`beer|wine|cheese|chocolate|honey /network/ProcessingMethodPacket.java`

Beer vs Wine: 42 Zeilen, ratio 0.91. Diff: Enum-Typ + Ziel-BE + Kommentar.

### 1.3 BottlingStation beer vs honey

Gleicher Dateiname, ratio ~0.75. **Nicht** in 2a/2b zusammenlegen (andere Rezepte/Enums). Höchstens später gemeinsame Basis.

### 1.4 Pflanzen — Brief #3

CannabisPlantBlock vs PoppyPlantBlock ~0.90. AbstractPlantBlock existiert schon. Nicht in 2a/2b.

### 1.5 Zwei RateLimiter — nicht mergen

`economy/RateLimiter.java` (Transaktionen) vs `util/RateLimiter.java` (DoS). Ratio ~0.09. Höchstens umbenennen.

### 1.6 Keine falschen Merges

`items/ModItems` vs `vehicle/items/ModItems` sind verschiedene Register.

---

## 2. Soll PR 2a — Brew Kettle

Eine Blockklasse, eine BE-Klasse, eine Menu-Klasse, eine Screen-Klasse. Stufe = Enum/Record.

```java
public enum MachineTier {
    SMALL(8, 1.0f),
    MEDIUM(/* Ist-Datei lesen, nicht raten */),
    LARGE(32, 2.0f);
    public final int capacity;
    public final float speed;
}
```

Werte aus dem Ist-Code kopieren. Small heute capacity 8 / speed 1.0, Large 32 / 2.0. Medium aus `MediumBrewKettleBlockEntity` abschreiben.

Registry-IDs bleiben: `small_brew_kettle`, `medium_brew_kettle`, `large_brew_kettle`.

Drei RegistryObjects sind erlaubt:

```java
SMALL_BREW_KETTLE  = register(..., () -> new BrewKettleBlock(props, MachineTier.SMALL));
```

**Empfehlung:** drei BlockEntityTypes behalten, eine BE-Klasse (weniger NBT-Risiko).

Titel weiter `block.schedulemc.small_brew_kettle` etc.

Alte Klassennamen nur löschen, wenn repo-weit keine Referenzen mehr (Tests, Datagen, Models). Sonst kurz als Deprecated-Subclass stehen lassen.

---

## 3. Familien für PR 2b

Dieselbe Operation wie 2a:

- Brew Kettle (small/medium/large_brew_kettle)
- Beer Fermentation Tank
- Conditioning Tank
- Wine Press
- Wine Fermentation Tank
- Aging Barrel
- Conching Machine
- Molding Station
- Cheese Press
- Aging Cave
- Extraction Vat (small/medium/**big**)
- Refinery (small/medium/**big**)
- Coffee Roaster
- Aging Chamber
- Drying Rack (small/medium/**big**)
- Fermentation Barrel (small/medium/**big**)
- Packaging Table

**Big vs Large:** IDs nicht umbenennen (`big_extraction_vat` bleibt).

Pro Familie vor dem Löschen notieren: capacity, speed, Menu-Slot-Koordinaten (können sich unterscheiden — dann Layout per Tier, nicht ein Layout erzwingen), Translation-Keys, BlockEntityType-Felder.

---

## 4. Soll PR 2c — Packet

Gemeinsames Packet + Interface, Enum bleibt modul-lokal:

```java
public interface ProcessingMethodTarget {
    void applyProcessingMethod(String name);
}
```

Fünf Registrar-Einträge sind ok. Netzwerk-Protokoll nicht brechen.

---

## 5. Harte Regeln

1. Registry-IDs unverändert.
2. NBT: Block-ID gleich. BE-Klassenname in gespeicherten Welten prüfen bevor 2a fertig ist.
3. Blockstates/Models nicht löschen.
4. Keine zusammengelegte ID `brew_kettle`.
5. Keine Rezept-Änderungen außer zwingend.
6. Kein README-Kürzen.
7. Brief #1 nicht in diesen Diff.
8. Pflanzen, RateLimiter, Bottling beer/honey nicht in 2a/2b.

---

## 6. Abnahme 2a

```
gradle compileJava
SmallBrewKettleMenu / LargeBrewKettleScreen nur noch tot oder weg
drei Block-IDs existieren
Kapazität/Speed = Ist-Werte vor dem PR
```

PR-Body: Tabelle der drei Ist-Zahlen.

---

## 7. Liegen lassen (Brief #3+)

Pflanzen auf AbstractPlantBlock, Beer- vs Honey-Bottling, Item-Strain-Wrapper, MenuTypes-Boilerplate.

---

## 8. Prompt für Claude

```
Lies docs/CLAUDE_IMPLEMENTATION_BRIEF_02_DEDUP.md.
Nur PR 2a: Brew-Kettle Small/Medium/Large → eine Klasse + MachineTier.
IDs, Kapazität, Speed, Models unverändert.
Danach stoppen und Nachweis in den PR-Body.
Nicht Brief #1 und nicht Pflanzen in denselben Diff.
```
