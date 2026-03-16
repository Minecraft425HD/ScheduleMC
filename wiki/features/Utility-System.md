# Utility System

**Navigation:** [Home](../Home.md) | [Features Overview](../Home.md#feature-systems) | [Player Settings](Player-Settings-System.md) | [Police System](Police-System.md)

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Core Components](#core-components)
   - [IUtilityConsumer Interface](#iutilityconsumer-interface)
   - [UtilityConsumptionData](#utilityconsumptiondata)
   - [UtilityCategory](#utilitycategory)
   - [PlotUtilityData](#plutilitydata)
   - [PlotUtilityManager](#plutilitymanager)
   - [UtilityRegistry](#utilityregistry)
   - [UtilityEventHandler](#utilityeventhandler)
4. [Consumption Model](#consumption-model)
5. [Default Consumption Values](#default-consumption-values)
6. [7-Day Rolling Average](#7-day-rolling-average)
7. [Category Breakdown](#category-breakdown)
8. [Police Integration](#police-integration)
9. [Commands](#commands)
10. [Implementing IUtilityConsumer](#implementing-iutilityconsumer)
11. [Troubleshooting](#troubleshooting)

---

## Overview

The **Utility System** tracks electricity (kWh) and water (liters) consumption for every block on each plot. It maintains a **7-day rolling average** using a circular buffer, provides per-category breakdowns, and detects **suspicious consumption patterns** that are automatically reported to the police system.

**Package:** `de.rolandsw.schedulemc.utility`

**Key features:**
- Per-plot tracking of electricity and water consumption
- Active vs. idle consumption states (active = 100%, idle = 50%)
- 7-day rolling average via optimized circular buffer (O(1) operations)
- 9 consumption categories for detailed breakdown reporting
- Police integration for anomaly detection
- Player-configurable warning thresholds via [Player Settings](Player-Settings-System.md)

---

## Architecture

```
utility/
├── IUtilityConsumer.java         ← Interface for blocks that consume utilities
├── UtilityConsumptionData.java   ← Record: electricity/water active+idle values
├── UtilityCategory.java          ← Enum: 9 categories (LIGHTING, CLIMATE, etc.)
├── PlotUtilityData.java          ← Per-plot data: consumers, history, current day
├── PlotUtilityManager.java       ← Singleton manager: all plot data + top consumers
├── UtilityRegistry.java          ← Registry: block → consumption data mapping
├── UtilityEventHandler.java      ← Forge events: block place/break, server tick
└── commands/
    └── UtilityCommand.java       ← /utility command with subcommands
```

**Data flow:**

```
Block placed in world
  → UtilityEventHandler catches PlaceEvent
    → checks UtilityRegistry.isConsumer(block)
      → PlotUtilityData.addConsumer(pos, block)

Every Minecraft hour (1000 ticks):
  → PlotUtilityData.calculateCurrentConsumption()
    → for each consumer: UtilityRegistry.getConsumptionById(blockId)
      → sum electricity + water by active/idle state

Every Minecraft day (24000 ticks):
  → PlotUtilityData.rolloverDay(currentDay)
    → pushes current day into circular buffer
    → resets current day counters
```

---

## Core Components

### IUtilityConsumer Interface

**File:** `IUtilityConsumer.java`

Any `BlockEntity` that consumes electricity or water should implement this interface. The Utility System queries it every tick to determine the block's active/idle status.

```java
public interface IUtilityConsumer {
    /**
     * Returns true when actively processing (100% consumption),
     * false when idle but on (50% consumption).
     *
     * Examples:
     *   - Grow Light: active when powered on
     *   - Drying Rack: active when items are inside
     *   - Meth Kettle: active when reaction is running
     *   - Pot: always active (constant water consumption)
     */
    boolean isActivelyConsuming();

    /**
     * Optional: override the registry value with a custom per-instance value.
     * Return null to use registry defaults.
     * Useful for upgrade-level-based variable consumption.
     */
    default UtilityConsumptionData getCustomConsumption() {
        return null;  // use UtilityRegistry defaults
    }

    /**
     * Called when the block's consumption is billed.
     * Useful for statistics or per-block events.
     */
    default void onUtilityBilled(double electricity, double water) {
        // default: no action
    }
}
```

---

### UtilityConsumptionData

**File:** `UtilityConsumptionData.java`

Immutable record holding consumption values for a block type. Units:
- **Electricity:** kWh per Minecraft day (20 real minutes)
- **Water:** liters per Minecraft day

```java
public record UtilityConsumptionData(
    double electricityActive,  // active state (100%)
    double electricityIdle,    // idle state (50%)
    double waterActive,
    double waterIdle,
    UtilityCategory category
)
```

**Factory methods:**

| Method | Description |
|--------|-------------|
| `UtilityConsumptionData.of(elec, water, cat)` | Standard: idle = 50% of active |
| `UtilityConsumptionData.constant(elec, water, cat)` | Idle = same as active (e.g., pots) |
| `UtilityConsumptionData.electricityOnly(active, cat)` | Water = 0, idle = 50% |
| `UtilityConsumptionData.waterOnly(active, cat)` | Electricity = 0, idle = 50% |

---

### UtilityCategory

**File:** `UtilityCategory.java`

Enum with 9 categories used for breakdown reporting:

| Category | Display Name | Icon |
|----------|-------------|------|
| `LIGHTING` | Beleuchtung | 💡 |
| `CLIMATE` | Klimatisierung | 🌡️ |
| `IRRIGATION` | Bewässerung | 💧 |
| `DRYING` | Trocknung | 🌬️ |
| `FERMENTATION` | Fermentierung | 🍺 |
| `CHEMICAL` | Chemie-Labor | ⚗️ |
| `MECHANICAL` | Mechanisch | ⚙️ |
| `PACKAGING` | Verpackung | 📦 |
| `OTHER` | Sonstiges | 📊 |

---

### PlotUtilityData

**File:** `PlotUtilityData.java`

Stores all utility data for a single plot. Uses an **optimized circular buffer** for O(1) day rollover operations instead of costly array shifting.

**Key fields:**

| Field | Type | Description |
|-------|------|-------------|
| `plotId` | `String` | Plot identifier |
| `consumers` | `Map<BlockPos, String>` | Position → block registry ID |
| `activeStatus` | `Map<BlockPos, Boolean>` | Position → isActive |
| `dailyElectricity[7]` | `double[]` | Circular buffer: 7 days electricity history |
| `dailyWater[7]` | `double[]` | Circular buffer: 7 days water history |
| `historyIndex` | `int` | Current "today" position in circular buffer |
| `currentDayElectricity` | `double` | Accumulator for current day |
| `currentDayWater` | `double` | Accumulator for current day |
| `categoryElectricity` | `EnumMap<UtilityCategory, Double>` | Breakdown by category |
| `categoryWater` | `EnumMap<UtilityCategory, Double>` | Breakdown by category |

**Key methods:**

| Method | Description |
|--------|-------------|
| `addConsumer(pos, block)` | Register a new consumer block |
| `removeConsumer(pos)` | Unregister a consumer block |
| `setActiveStatus(pos, isActive)` | Update active/idle state |
| `calculateCurrentConsumption()` | Recalculate current day totals |
| `rolloverDay(currentDay)` | Push current day into circular buffer |
| `get7DayAverageElectricity()` | Average of non-zero days in buffer |
| `get7DayAverageWater()` | Average of non-zero days in buffer |
| `getDailyElectricity()` | Array[7]: today=0, yesterday=1, etc. |
| `toJson()` / `fromJson()` | Serialization for persistence |

**Serialization format:**
```json
{
  "plotId": "plot_42",
  "lastUpdateDay": 12345,
  "currentDayElectricity": 450.0,
  "currentDayWater": 200.0,
  "historyIndex": 3,
  "electricityHistory": [450.0, 380.0, 520.0, 410.0, 0.0, 0.0, 0.0],
  "waterHistory": [200.0, 150.0, 230.0, 180.0, 0.0, 0.0, 0.0],
  "consumers": [
    { "x": 100, "y": 64, "z": 200, "blockId": "schedulemc:basic_grow_light_slab", "active": true },
    ...
  ]
}
```

---

### PlotUtilityManager

**File:** `PlotUtilityManager.java`

Central singleton managing all plot data. Handles save/load, top consumer lists, and scan operations.

**Key methods:**

| Method | Description |
|--------|-------------|
| `getPlotData(plotId)` | `Optional<PlotUtilityData>` for a specific plot |
| `getTopConsumers(n)` | Top N plots by 7-day average electricity |
| `scanPlotForConsumers(level, plot)` | Scan all blocks in a plot for IUtilityConsumer implementations |
| `formatElectricity(value)` | Format as `"450.0 kWh"` |
| `formatWater(value)` | Format as `"200.0 L"` |
| `getStatsSummary()` | Server-wide stats string |

---

### UtilityRegistry

**File:** `UtilityRegistry.java`

Thread-safe (ConcurrentHashMap) registry mapping block types to their consumption data.

**Registration:**
```java
// During mod initialization:
UtilityRegistry.registerDefaults();   // register all built-in blocks by ID
UtilityRegistry.resolveBlockReferences();  // resolve IDs to Block instances
```

**Custom registration:**
```java
// Register by Block instance:
UtilityRegistry.register(myBlock, UtilityConsumptionData.of(50, 10, UtilityCategory.LIGHTING));

// Register by registry ID (use during setup before block resolution):
UtilityRegistry.registerById("mymod:my_block",
    UtilityConsumptionData.of(50, 10, UtilityCategory.LIGHTING));
```

**Lookup:**
```java
Optional<UtilityConsumptionData> data = UtilityRegistry.getConsumption(block);
boolean isConsumer = UtilityRegistry.isConsumer(block);
```

---

### UtilityEventHandler

**File:** `UtilityEventHandler.java`

Forge event handler that integrates the utility system with the game loop:

| Event | Action |
|-------|--------|
| `BlockEvent.EntityPlaceEvent` | Add block to plot's consumer list if in registry |
| `BlockEvent.BreakEvent` | Remove block from plot's consumer list |
| `ServerTickEvent` | Every 1000 ticks: recalculate consumption; every 24000 ticks: rollover day |
| `PlayerTickEvent` | Check player's warning thresholds (from PlayerSettings) |

---

## Consumption Model

The utility system models two states per block:

```
Active state (isActivelyConsuming() = true):
  → 100% of registered values
  → Block is processing/running

Idle state (isActivelyConsuming() = false):
  → 50% of registered values (default)
  → Block is on but not processing
  → Constant blocks: 100% always (pots, tanks)
```

**Calculation per Minecraft day:**
```
total_electricity = Σ (per block: data.getCurrentElectricity(isActive))
total_water       = Σ (per block: data.getCurrentWater(isActive))
```

---

## Default Consumption Values

The following blocks are registered by default in `UtilityRegistry.registerDefaults()`:

### Grow Lights (Lighting)

| Block | Electricity Active | Electricity Idle | Water |
|-------|-------------------|-----------------|-------|
| `basic_grow_light_slab` | 50 kWh | 25 kWh | 0 |
| `advanced_grow_light_slab` | 100 kWh | 50 kWh | 0 |
| `premium_grow_light_slab` | 200 kWh | 100 kWh | 0 |

### Climate Lamps / Mushroom (Climate)

| Block | Electricity Active | Electricity Idle | Water Active | Water Idle |
|-------|-------------------|-----------------|-------------|-----------|
| `klimalampe_small` | 30 kWh | 15 kWh | 5 L | 2.5 L |
| `klimalampe_medium` | 60 kWh | 30 kWh | 10 L | 5 L |
| `klimalampe_large` | 120 kWh | 60 kWh | 20 L | 10 L |

### Irrigation (Constant)

| Block | Electricity | Water (constant) |
|-------|-------------|-----------------|
| `terracotta_pot` | 0 | 10 L |
| `ceramic_pot` | 0 | 15 L |
| `iron_pot` | 0 | 20 L |
| `golden_pot` | 0 | 25 L |
| `wassertank` | 5 kWh | 50 L |

### Drying Equipment

| Block | Electricity Active | Idle |
|-------|-------------------|------|
| `small_drying_rack` | 10 kWh | 5 kWh |
| `medium_drying_rack` | 20 kWh | 10 kWh |
| `big_drying_rack` | 40 kWh | 20 kWh |
| `vakuum_trockner` (Meth) | 120 kWh | 60 kWh |
| `trocknungs_ofen` (MDMA) | 150 kWh | 75 kWh |

### Fermentation

| Block | Electricity Active | Water Active |
|-------|-------------------|----|
| `small_fermentation_barrel` | 15 kWh | 5 L |
| `medium_fermentation_barrel` | 30 kWh | 10 L |
| `big_fermentation_barrel` | 60 kWh | 20 L |
| `fermentations_tank` (LSD) | 80 kWh | 30 L |
| `cannabis_curing_glas` | 5 kWh | 0 |

### Chemical Processing

| Block | Electricity Active | Water Active |
|-------|-------------------|-------------|
| `small_extraction_vat` (Coca) | 40 kWh | 30 L |
| `medium_extraction_vat` | 80 kWh | 60 L |
| `big_extraction_vat` | 160 kWh | 120 L |
| `small_refinery` (Coca) | 60 kWh | 20 L |
| `medium_refinery` | 120 kWh | 40 L |
| `big_refinery` | 240 kWh | 80 L |
| `crack_kocher` | 100 kWh | 10 L |
| `kochstation` (Poppy) | 80 kWh | 40 L |
| `heroin_raffinerie` | 150 kWh | 50 L |
| `chemie_mixer` (Meth) | 100 kWh | 30 L |
| `reduktionskessel` | 250 kWh | 50 L |
| `kristallisator` | 180 kWh | 100 L |
| `destillations_apparat` (LSD) | 150 kWh | 60 L |
| `mikro_dosierer` | 80 kWh | 10 L |
| `reaktions_kessel` (MDMA) | 200 kWh | 40 L |
| `cannabis_oel_extraktor` | 120 kWh | 20 L |

### Mechanical

| Block | Electricity Active | Water |
|-------|-------------------|-------|
| `ritzmaschine` (Poppy) | 20 kWh | 5 L |
| `opium_presse` | 50 kWh | 0 |
| `perforations_presse` (LSD) | 40 kWh | 0 |
| `pillen_presse` (MDMA) | 60 kWh | 0 |
| `cannabis_trimm_station` | 10 kWh | 5 L |
| `cannabis_hash_presse` | 80 kWh | 0 |

### Packaging

| Block | Electricity Active | Idle |
|-------|-------------------|----|
| `small_packaging_table` | 5 kWh | 2.5 kWh |
| `medium_packaging_table` | 10 kWh | 5 kWh |
| `large_packaging_table` | 20 kWh | 10 kWh |

### Other

| Block | Electricity Active | Water Active |
|-------|-------------------|----|
| `sink` | 5 kWh | 30 L |

---

## 7-Day Rolling Average

The circular buffer stores the last 7 Minecraft days of consumption:

```
Buffer layout (HISTORY_SIZE = 7):
  Index 0..6 in circular ring
  historyIndex = pointer to "today"

Access pattern:
  today     = dailyElectricity[historyIndex]
  yesterday = dailyElectricity[(historyIndex + 1) % 7]
  2 days ago = dailyElectricity[(historyIndex + 2) % 7]
  ...

7-day average:
  sum = Σ of all non-zero values
  count = number of non-zero days
  average = count > 0 ? sum/count : currentDayElectricity
```

This approach requires **no array shifting** on day rollover — only the index pointer advances. Performance: O(1) per day vs. O(n) for shifting.

---

## Category Breakdown

The `/utility breakdown <plotId>` command shows consumption per category:

```
═══ AUFSCHLÜSSELUNG ═══
Plot: plot_42

⚡ STROM nach Kategorie:
  ⚗️ Chemie-Labor: 580.0 kWh
  💡 Beleuchtung: 200.0 kWh
  🌬️ Trocknung: 120.0 kWh

💧 WASSER nach Kategorie:
  ⚗️ Chemie-Labor: 190.0 L
  💧 Bewässerung: 50.0 L
```

Categories with zero consumption are hidden from the breakdown.

---

## Police Integration

The Utility System integrates with the [Police System](Police-System.md) to detect suspicious consumption patterns. When a plot's consumption exceeds certain thresholds (e.g., a plot with extreme electricity usage typical of an illegal lab), the system can automatically:

1. Flag the plot as suspicious
2. Send an alert to online police officers
3. Log the event for investigation

Threshold configuration is handled in `PlotUtilityManager` and is configurable per server.

---

## Commands

### `/utility` (or `/strom`, `/wasser`)

Shows utility consumption for the plot you are currently standing on.

```
═══ UTILITY VERBRAUCH ═══
Plot: plot_42
Verbraucher: 15 Blöcke

⚡ STROM:
  Aktuell: 450.0 kWh/Tag
  7-Tage-Ø: 420.0 kWh/Tag

💧 WASSER:
  Aktuell: 200.0 L/Tag
  7-Tage-Ø: 185.0 L/Tag
```

---

### `/utility <plotId>`

Shows utility data for a specific plot by ID.

```
/utility plot_42
```

---

### `/utility top`

Shows the top 10 highest-consuming plots by 7-day average electricity.

```
═══ TOP 10 VERBRAUCHER ═══

1. plot_42
   ⚡ 450.0 kWh | 💧 200.0 L
2. plot_17
   ⚡ 380.0 kWh | 💧 150.0 L
...
```

---

### `/utility breakdown <plotId>`

Shows per-category breakdown for a specific plot.

```
/utility breakdown plot_42
```

---

### `/utility scan` *(Operator only)*

Scans all blocks in the current plot and registers any untracked `IUtilityConsumer` blocks. Useful after adding new block types or after manual block placement.

```
/utility scan
> Scanne Plot plot_42...
> Scan abgeschlossen: 15 Verbraucher gefunden.
```

---

### `/utility stats`

Shows server-wide utility statistics summary.

---

## Implementing IUtilityConsumer

To make a custom block entity consume utilities:

### Step 1: Implement the Interface

```java
public class MyMachineBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean isRunning = false;

    @Override
    public boolean isActivelyConsuming() {
        return isRunning;
    }

    // Optional: variable consumption based on upgrade level
    @Override
    public UtilityConsumptionData getCustomConsumption() {
        int upgradeLevel = getUpgradeLevel();
        double baseElec = 50 + (upgradeLevel * 25);
        return UtilityConsumptionData.electricityOnly(baseElec, UtilityCategory.MECHANICAL);
    }

    // Optional: react to billing events
    @Override
    public void onUtilityBilled(double electricity, double water) {
        // e.g., log statistics, update UI
    }
}
```

### Step 2: Register Consumption Data

```java
// In your mod's setup event (after block registration):
UtilityRegistry.register(
    MyBlocks.MY_MACHINE.get(),
    UtilityConsumptionData.of(75, 20, UtilityCategory.MECHANICAL)
);

// Or by ID (can be done earlier):
UtilityRegistry.registerById(
    "mymod:my_machine",
    UtilityConsumptionData.of(75, 20, UtilityCategory.MECHANICAL)
);
```

### Step 3: Update Active Status

Whenever the machine starts or stops:
```java
// When machine starts:
PlotUtilityData plotData = PlotUtilityManager.getPlotData(plotId).orElse(null);
if (plotData != null) {
    plotData.setActiveStatus(blockPos, true);
}

// When machine stops:
if (plotData != null) {
    plotData.setActiveStatus(blockPos, false);
}
```

---

## Troubleshooting

| Problem | Cause | Solution |
|---------|-------|----------|
| Block not tracked | Block not in UtilityRegistry | Call `UtilityRegistry.register()` in setup; use `/utility scan` to pick up existing blocks |
| Wrong consumption values | Custom consumption not returning null when expected | Ensure `getCustomConsumption()` returns `null` when using defaults |
| Data not persisting | PlotUtilityManager save not called | Ensure server shutdown hook calls `PlotUtilityManager.saveAll()` |
| 7-day average is 0 | No full days have passed yet | Normal behavior for new installations; average uses current day as fallback |
| Category not shown in breakdown | Category has 0 consumption | Only non-zero categories are displayed |
| `/utility scan` shows 0 consumers | Blocks placed before system initialized | Place blocks after server restart; re-scan if needed |

---

*Part of the ScheduleMC v3.6.9-beta documentation. For API integration, see the [API Reference](../../docs/API_REFERENCE.md).*
