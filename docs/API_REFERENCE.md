# ScheduleMC API Reference

**Version:** 3.6.0-beta | **Minecraft:** 1.20.1 | **Forge:** 47.4.0 | **Java:** 17+

Complete API reference for external mod developers integrating with ScheduleMC.

---

## Table of Contents

- [Getting Started](#getting-started)
  - [Dependency Setup](#dependency-setup)
  - [Accessing the API](#accessing-the-api)
  - [API Initialization Check](#api-initialization-check)
- [Architecture Overview](#architecture-overview)
  - [Singleton Pattern](#singleton-pattern)
  - [Thread Safety](#thread-safety)
  - [Error Handling Patterns](#error-handling-patterns)
- [API Modules](#api-modules)
  1. [IEconomyAPI -- Economy System](#1-ieconomyapi----economy-system)
  2. [IPlotAPI -- Plot Management](#2-iplotapi----plot-management)
  3. [INPCAPI -- NPC System](#3-inpcapi----npc-system)
  4. [IPoliceAPI -- Police / Wanted System](#4-ipoliceapi----police--wanted-system)
  5. [IProductionAPI -- Production System](#5-iproductionapi----production-system)
  6. [IVehicleAPI -- Vehicle System](#6-ivehicleapi----vehicle-system)
  7. [IWarehouseAPI -- Warehouse System](#7-iwarehouseapi----warehouse-system)
  8. [IMessagingAPI -- Messaging System](#8-imessagingapi----messaging-system)
  9. [ISmartphoneAPI -- Smartphone GUI Tracking](#9-ismartphoneapi----smartphone-gui-tracking)
  10. [IAchievementAPI -- Achievement System](#10-iachievementapi----achievement-system)
  11. [IMarketAPI -- Dynamic Market](#11-imarketapi----dynamic-market)
  12. [PlotModAPI -- Legacy Static Utilities](#12-plotmodapi----legacy-static-utilities)
- [Event Integration](#event-integration)
- [Version Compatibility](#version-compatibility)

---

## Getting Started

### Dependency Setup

Add ScheduleMC as a compile-time dependency in your `build.gradle`:

```groovy
repositories {
    maven { url = 'https://maven.minecraftforge.net' }
    // ScheduleMC repository
    flatDir { dirs 'libs' }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.4.0'

    // ScheduleMC API - compile-only dependency
    compileOnly files('libs/schedulemc-3.6.0-beta.jar')

    // Or, if published to a Maven repository:
    // compileOnly 'de.rolandsw.schedulemc:schedulemc:3.6.0-beta'
}
```

In your `mods.toml`, declare ScheduleMC as an optional or required dependency:

```toml
[[dependencies.yourmodid]]
    modId = "schedulemc"
    mandatory = false
    versionRange = "[3.0.0,)"
    ordering = "AFTER"
    side = "BOTH"
```

### Accessing the API

All API modules are accessible through the central `ScheduleMCAPI` singleton:

```java
import de.rolandsw.schedulemc.api.ScheduleMCAPI;
import de.rolandsw.schedulemc.api.economy.IEconomyAPI;

public class MyModIntegration {

    public void onServerStarted() {
        ScheduleMCAPI api = ScheduleMCAPI.getInstance();

        if (!api.isInitialized()) {
            LOGGER.warn("ScheduleMC API not yet initialized");
            return;
        }

        IEconomyAPI economy = api.getEconomyAPI();
        double balance = economy.getBalance(playerUUID);
    }
}
```

### API Initialization Check

The API is initialized by the ScheduleMC mod during server startup. Always verify
before accessing sub-APIs:

```java
ScheduleMCAPI api = ScheduleMCAPI.getInstance();

// Full readiness check
if (api.isInitialized()) {
    // All 11 sub-APIs are ready
}

// Version check
String version = api.getVersion(); // "3.0.0"

// Diagnostic status (useful for logging)
String status = api.getStatus();
// Output:
// ScheduleMC API v3.0.0 - READY
//   Economy:      [check]
//   Plot:         [check]
//   Production:   [check]
//   ...
```

If you call any getter (e.g., `getEconomyAPI()`) before the API is initialized,
it throws `IllegalStateException` with a descriptive message.

---

## Architecture Overview

### Singleton Pattern

`ScheduleMCAPI` uses a thread-safe double-checked locking singleton:

```java
public class ScheduleMCAPI {
    private static volatile ScheduleMCAPI instance;

    public static ScheduleMCAPI getInstance() {
        if (instance == null) {
            synchronized (ScheduleMCAPI.class) {
                if (instance == null) {
                    instance = new ScheduleMCAPI();
                }
            }
        }
        return instance;
    }
}
```

The `volatile` keyword on the instance field ensures correct visibility across
threads, which is critical on the JVM memory model for the double-checked locking
pattern to work reliably.

### Thread Safety

All API methods are thread-safe. The underlying mechanisms vary by module:

| Module        | Thread-Safety Mechanism                              |
|---------------|------------------------------------------------------|
| Economy       | `EconomyManager` internal synchronization            |
| Plot          | `ConcurrentHashMap` + LRU cache                      |
| NPC           | `ConcurrentHashMap`-based registry                   |
| Police        | `ConcurrentHashMap` + atomic operations              |
| Production    | `ConcurrentHashMap`-based `ProductionRegistry`       |
| Vehicle       | `ConcurrentHashMap`-based registry                   |
| Warehouse     | `synchronized` operations                            |
| Messaging     | `ConcurrentHashMap`                                  |
| Smartphone    | `ConcurrentHashMap.newKeySet()`                      |
| Achievement   | `ConcurrentHashMap`-based data store                 |
| Market        | `ConcurrentHashMap` in `DynamicMarketManager`        |

All API methods can safely be called from any thread -- the server tick thread,
networking threads, or your own background threads.

### Error Handling Patterns

All API methods follow a consistent validation pattern:

1. **Null parameters** throw `IllegalArgumentException` immediately.
2. **Invalid state** (e.g., creating a duplicate account) throws `IllegalStateException`.
3. **Insufficient funds** or similar business logic failures return `false` rather
   than throwing.
4. **Lookup misses** return `null` (annotated with `@Nullable` from `javax.annotation`).

Recommended calling pattern:

```java
IEconomyAPI economy = ScheduleMCAPI.getInstance().getEconomyAPI();

try {
    // Validate inputs before calling
    if (playerUUID == null) {
        return;
    }

    // Operations that return boolean indicate success/failure
    boolean success = economy.withdraw(playerUUID, 100.0);
    if (!success) {
        // Insufficient funds -- handle gracefully
        player.sendSystemMessage(Component.literal("Not enough money!"));
        return;
    }

    economy.deposit(otherPlayerUUID, 100.0);

} catch (IllegalArgumentException e) {
    LOGGER.error("Invalid API call: {}", e.getMessage());
} catch (IllegalStateException e) {
    LOGGER.error("API not ready: {}", e.getMessage());
}
```

---

## API Modules

---

### 1. IEconomyAPI -- Economy System

**Package:** `de.rolandsw.schedulemc.api.economy`
**Implementation:** `de.rolandsw.schedulemc.api.impl.EconomyAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getEconomyAPI()`

Provides full access to the ScheduleMC economy: player accounts, deposits,
withdrawals, transfers, and administrative balance manipulation. Currency
is denominated in Euro. All amounts are `double` values.

#### Methods

---

##### `getBalance(UUID playerUUID)` -> `double`

Returns the current balance of a player's account.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** Balance in Euro. Returns `0.0` if no account exists.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
IEconomyAPI economy = api.getEconomyAPI();
double balance = economy.getBalance(playerUUID);
player.sendSystemMessage(Component.literal("Balance: " + balance + " Euro"));
```

---

##### `hasAccount(UUID playerUUID)` -> `boolean`

Checks whether a player has an existing economy account.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** `true` if the account exists, `false` otherwise.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
if (!economy.hasAccount(playerUUID)) {
    economy.createAccount(playerUUID);
}
```

---

##### `createAccount(UUID playerUUID)` -> `void`

Creates a new economy account with the configured starting balance.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Throws:**
- `IllegalArgumentException` if `playerUUID` is `null`.
- `IllegalStateException` if an account already exists for this UUID.

```java
if (!economy.hasAccount(playerUUID)) {
    economy.createAccount(playerUUID);
    double startBalance = economy.getStartBalance();
    LOGGER.info("Created account with starting balance: {} Euro", startBalance);
}
```

---

##### `deposit(UUID playerUUID, double amount)` -> `void`

Deposits money into a player's account.

| Parameter    | Type     | Description                      |
|-------------|----------|----------------------------------|
| `playerUUID` | `UUID`   | The player's UUID                |
| `amount`     | `double` | Amount to deposit (must be >= 0) |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null` or `amount` is negative.

```java
economy.deposit(playerUUID, 500.0);
```

---

##### `deposit(UUID playerUUID, double amount, @Nullable String description)` -> `void`

Deposits money with an optional transaction description for the log.

| Parameter     | Type     | Description                                  |
|--------------|----------|----------------------------------------------|
| `playerUUID`  | `UUID`   | The player's UUID                            |
| `amount`      | `double` | Amount to deposit (must be >= 0)             |
| `description` | `String` | Optional description for the transaction log |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null` or `amount` is negative.

```java
economy.deposit(playerUUID, 250.0, "Shop sale: 5x Diamond Sword");
```

---

##### `withdraw(UUID playerUUID, double amount)` -> `boolean`

Withdraws money from a player's account.

| Parameter    | Type     | Description                       |
|-------------|----------|-----------------------------------|
| `playerUUID` | `UUID`   | The player's UUID                 |
| `amount`     | `double` | Amount to withdraw (must be >= 0) |

**Returns:** `true` if the withdrawal succeeded, `false` if insufficient funds.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null` or `amount` is negative.

```java
boolean success = economy.withdraw(playerUUID, 100.0);
if (!success) {
    player.sendSystemMessage(Component.literal("Insufficient funds!"));
}
```

---

##### `withdraw(UUID playerUUID, double amount, @Nullable String description)` -> `boolean`

Withdraws money with an optional transaction description.

| Parameter     | Type     | Description                                  |
|--------------|----------|----------------------------------------------|
| `playerUUID`  | `UUID`   | The player's UUID                            |
| `amount`      | `double` | Amount to withdraw (must be >= 0)            |
| `description` | `String` | Optional description for the transaction log |

**Returns:** `true` if successful, `false` if insufficient funds.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null` or `amount` is negative.

```java
boolean success = economy.withdraw(playerUUID, 5000.0, "Vehicle purchase: Sports Car");
```

---

##### `transfer(UUID fromUUID, UUID toUUID, double amount)` -> `boolean`

Transfers money between two players atomically.

| Parameter  | Type     | Description                         |
|-----------|----------|-------------------------------------|
| `fromUUID` | `UUID`   | Sender's UUID                       |
| `toUUID`   | `UUID`   | Recipient's UUID                    |
| `amount`   | `double` | Amount to transfer (must be >= 0)   |

**Returns:** `true` if the transfer succeeded, `false` if the sender has insufficient funds.

**Throws:** `IllegalArgumentException` if any UUID is `null` or `amount` is negative.

```java
boolean success = economy.transfer(senderUUID, receiverUUID, 1000.0);
if (success) {
    // Both balances updated atomically
}
```

---

##### `transfer(UUID fromUUID, UUID toUUID, double amount, @Nullable String description)` -> `boolean`

Transfers money with an optional transaction description.

| Parameter     | Type     | Description                                  |
|--------------|----------|----------------------------------------------|
| `fromUUID`    | `UUID`   | Sender's UUID                                |
| `toUUID`      | `UUID`   | Recipient's UUID                             |
| `amount`      | `double` | Amount to transfer (must be >= 0)            |
| `description` | `String` | Optional description for the transaction log |

**Returns:** `true` if successful, `false` if insufficient funds.

**Throws:** `IllegalArgumentException` if any UUID is `null` or `amount` is negative.

```java
economy.transfer(buyerUUID, sellerUUID, 2500.0, "Plot purchase: Downtown Apartment");
```

---

##### `setBalance(UUID playerUUID, double amount)` -> `void`

**[Admin]** Sets a player's balance to an exact value. Negative values are clamped to `0`.

| Parameter    | Type     | Description                                   |
|-------------|----------|-----------------------------------------------|
| `playerUUID` | `UUID`   | The player's UUID                             |
| `amount`     | `double` | New balance (clamped to 0 if negative)        |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

> **Warning:** This is an administrative function. Use only for admin commands or
> correction scenarios. It bypasses normal transaction logging.

```java
// Admin command: reset a player's balance
economy.setBalance(playerUUID, 0.0);
```

---

##### `deleteAccount(UUID playerUUID)` -> `void`

**[Admin]** Deletes a player's economy account permanently.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

> **Warning:** All transaction history and the balance are permanently lost.

```java
economy.deleteAccount(playerUUID);
```

---

##### `getStartBalance()` -> `double`

Returns the configured starting balance for new accounts, as defined in the
mod configuration (`ModConfigHandler.COMMON.START_BALANCE`).

**Returns:** Starting balance in Euro.

```java
double startBal = economy.getStartBalance();
LOGGER.info("New players receive {} Euro", startBal);
```

---

### 2. IPlotAPI -- Plot Management

**Package:** `de.rolandsw.schedulemc.api.plot`
**Implementation:** `de.rolandsw.schedulemc.api.impl.PlotAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getPlotAPI()`

Manages land plots -- creation, lookup, ownership, buying, and selling.
Uses a spatial index with an LRU cache for fast position-based lookups.

#### Plot Types

```java
public enum PlotType {
    RESIDENTIAL(true, true),    // Purchasable, rentable
    COMMERCIAL(true, true),     // Purchasable, rentable
    SHOP(false, false),         // State-owned, not purchasable
    PUBLIC(false, false),       // Public area
    GOVERNMENT(false, false),   // Government property
    PRISON(false, false),       // Prison plot
    TOWING_YARD(true, true)     // Towing yard, purchasable
}
```

#### Methods

---

##### `getPlotAt(BlockPos pos)` -> `@Nullable PlotRegion`

Returns the plot at a specific world position.

| Parameter | Type       | Description           |
|----------|------------|-----------------------|
| `pos`     | `BlockPos` | The block position    |

**Returns:** The `PlotRegion` at that position, or `null` if no plot exists there.

**Throws:** `IllegalArgumentException` if `pos` is `null`.

**Performance:** O(1) via LRU cache hit; O(log n) on cache miss via spatial index.

```java
IPlotAPI plots = api.getPlotAPI();
PlotRegion plot = plots.getPlotAt(new BlockPos(100, 64, 200));
if (plot != null) {
    String name = plot.getPlotName();
    UUID owner = plot.getOwnerUUID();
}
```

---

##### `getPlot(String plotId)` -> `@Nullable PlotRegion`

Looks up a plot by its unique string identifier.

| Parameter | Type     | Description         |
|----------|----------|---------------------|
| `plotId`  | `String` | The plot identifier |

**Returns:** The `PlotRegion`, or `null` if not found.

**Throws:** `IllegalArgumentException` if `plotId` is `null`.

```java
PlotRegion plot = plots.getPlot("downtown_apt_42");
```

---

##### `hasPlot(String plotId)` -> `boolean`

Checks whether a plot with the given ID exists.

| Parameter | Type     | Description         |
|----------|----------|---------------------|
| `plotId`  | `String` | The plot identifier |

**Returns:** `true` if the plot exists, `false` otherwise.

**Throws:** `IllegalArgumentException` if `plotId` is `null`.

```java
if (plots.hasPlot("shop_plaza_1")) {
    // Plot exists
}
```

---

##### `getPlotsByOwner(UUID ownerUUID)` -> `List<PlotRegion>`

Returns all plots owned by a specific player.

| Parameter   | Type   | Description        |
|------------|--------|--------------------|
| `ownerUUID` | `UUID` | The owner's UUID   |

**Returns:** List of owned plots. May be empty, never `null`.

**Throws:** `IllegalArgumentException` if `ownerUUID` is `null`.

```java
List<PlotRegion> myPlots = plots.getPlotsByOwner(playerUUID);
player.sendSystemMessage(Component.literal("You own " + myPlots.size() + " plots"));
```

---

##### `getAvailablePlots()` -> `List<PlotRegion>`

Returns all plots that are unowned and available for purchase.

**Returns:** List of ownerless plots. May be empty, never `null`.

```java
List<PlotRegion> available = plots.getAvailablePlots();
for (PlotRegion p : available) {
    LOGGER.info("Available: {} - {} Euro", p.getPlotName(), p.getPrice());
}
```

---

##### `getPlotsForSale()` -> `List<PlotRegion>`

Returns all plots that are currently listed for sale by their owners.

**Returns:** List of plots for sale. May be empty, never `null`.

```java
List<PlotRegion> forSale = plots.getPlotsForSale();
```

---

##### `createPlot(BlockPos pos1, BlockPos pos2, @Nullable String plotName, PlotType type, double price)` -> `PlotRegion`

Creates a new plot region defined by two diagonal corner positions.

| Parameter  | Type       | Description                                                |
|-----------|------------|------------------------------------------------------------|
| `pos1`     | `BlockPos` | First corner of the region                                 |
| `pos2`     | `BlockPos` | Opposite diagonal corner                                   |
| `plotName` | `String`   | Optional name (auto-generated if `null`)                   |
| `type`     | `PlotType` | The plot type (RESIDENTIAL, COMMERCIAL, SHOP, etc.)        |
| `price`    | `double`   | Price in Euro (must be >= 0)                               |

**Returns:** The newly created `PlotRegion`.

**Throws:**
- `IllegalArgumentException` if positions are `null`, `type` is `null`, or `price` is negative.
- `IllegalArgumentException` if the plot region is too large or overlaps existing plots.

```java
PlotRegion newPlot = plots.createPlot(
    new BlockPos(0, 60, 0),
    new BlockPos(50, 120, 50),
    "My Residence",
    PlotType.RESIDENTIAL,
    5000.0
);
```

---

##### `removePlot(String plotId)` -> `boolean`

Removes a plot permanently.

| Parameter | Type     | Description         |
|----------|----------|---------------------|
| `plotId`  | `String` | The plot identifier |

**Returns:** `true` if the plot was removed, `false` if no plot with that ID existed.

**Throws:** `IllegalArgumentException` if `plotId` is `null`.

```java
boolean removed = plots.removePlot("old_plot_7");
```

---

##### `getPlotCount()` -> `int`

Returns the total number of registered plots.

**Returns:** Total plot count.

```java
int totalPlots = plots.getPlotCount();
```

---

### 3. INPCAPI -- NPC System

**Package:** `de.rolandsw.schedulemc.api.npc`
**Implementation:** `de.rolandsw.schedulemc.api.impl.NPCAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getNPCAPI()`

Provides access to custom NPCs -- lookup, data retrieval, and configuration of
home/work positions and types.

#### NPC Types

```java
public enum NPCType {
    BEWOHNER,       // Citizen (German)
    VERKAEUFER,     // Merchant (German)
    POLIZEI,        // Police (German)
    BANK,           // Banker (German)
    ABSCHLEPPER,    // Tow truck driver (German)
    // English aliases for API compatibility
    CITIZEN,
    MERCHANT,
    POLICE,
    BANKER,
    DRUG_DEALER
}
```

#### Methods

---

##### `getNPCByUUID(UUID npcUUID, ServerLevel level)` -> `@Nullable CustomNPCEntity`

Finds an NPC by UUID within a specific server level.

| Parameter | Type          | Description               |
|----------|---------------|---------------------------|
| `npcUUID` | `UUID`        | The NPC's unique ID       |
| `level`   | `ServerLevel` | The server level to search |

**Returns:** The `CustomNPCEntity`, or `null` if not found.

**Throws:** `IllegalArgumentException` if any parameter is `null`.

**Performance:** O(1) via HashMap lookup.

```java
INPCAPI npcAPI = api.getNPCAPI();
CustomNPCEntity npc = npcAPI.getNPCByUUID(npcUUID, serverLevel);
if (npc != null) {
    NPCData data = npcAPI.getNPCData(npc);
}
```

---

##### `getNPCByUUID(UUID npcUUID)` -> `@Nullable CustomNPCEntity`

Finds an NPC by UUID, searching across all loaded server levels.

| Parameter | Type   | Description           |
|----------|--------|-----------------------|
| `npcUUID` | `UUID` | The NPC's unique ID   |

**Returns:** The `CustomNPCEntity`, or `null` if not found in any level.

**Throws:** `IllegalArgumentException` if `npcUUID` is `null`.

**Performance:** O(w) where w = number of loaded worlds (typically 3: overworld, nether, end).

```java
CustomNPCEntity npc = npcAPI.getNPCByUUID(npcUUID);
```

---

##### `getAllNPCs(ServerLevel level)` -> `Collection<CustomNPCEntity>`

Returns all NPCs in a specific server level.

| Parameter | Type          | Description               |
|----------|---------------|---------------------------|
| `level`   | `ServerLevel` | The server level to query  |

**Returns:** An unmodifiable collection of NPCs. May be empty, never `null`.

**Throws:** `IllegalArgumentException` if `level` is `null`.

```java
Collection<CustomNPCEntity> overworldNPCs = npcAPI.getAllNPCs(overworld);
LOGGER.info("Overworld has {} NPCs", overworldNPCs.size());
```

---

##### `getAllNPCs()` -> `Collection<CustomNPCEntity>`

Returns all NPCs across all loaded server levels.

**Returns:** An unmodifiable collection of all NPCs. May be empty, never `null`.

```java
Collection<CustomNPCEntity> allNPCs = npcAPI.getAllNPCs();
```

---

##### `getNPCCount(ServerLevel level)` -> `int`

Returns the number of NPCs in a specific level.

| Parameter | Type          | Description               |
|----------|---------------|---------------------------|
| `level`   | `ServerLevel` | The server level to count  |

**Returns:** NPC count in that level.

**Throws:** `IllegalArgumentException` if `level` is `null`.

```java
int count = npcAPI.getNPCCount(serverLevel);
```

---

##### `getTotalNPCCount()` -> `int`

Returns the total number of NPCs across all levels.

**Returns:** Total NPC count.

```java
int total = npcAPI.getTotalNPCCount();
```

---

##### `getNPCData(CustomNPCEntity npc)` -> `NPCData`

Retrieves the data object associated with an NPC, containing all configuration,
schedule, and behavior data.

| Parameter | Type              | Description    |
|----------|-------------------|----------------|
| `npc`     | `CustomNPCEntity` | The NPC entity |

**Returns:** The `NPCData` object (never `null`).

**Throws:** `IllegalArgumentException` if `npc` is `null`.

```java
NPCData data = npcAPI.getNPCData(npc);
NPCType type = data.getNpcType();
BlockPos home = data.getHomeLocation();
```

---

##### `setNPCHome(CustomNPCEntity npc, BlockPos homePos)` -> `void`

Sets the home position for an NPC. The NPC will return to this position during
its idle schedule.

| Parameter | Type              | Description         |
|----------|-------------------|---------------------|
| `npc`     | `CustomNPCEntity` | The NPC entity      |
| `homePos` | `BlockPos`        | New home position   |

**Throws:** `IllegalArgumentException` if any parameter is `null`.

```java
npcAPI.setNPCHome(npc, new BlockPos(100, 64, 200));
```

---

##### `setNPCWork(CustomNPCEntity npc, BlockPos workPos)` -> `void`

Sets the work position for an NPC. The NPC will travel to this position during
its work schedule.

| Parameter | Type              | Description         |
|----------|-------------------|---------------------|
| `npc`     | `CustomNPCEntity` | The NPC entity      |
| `workPos` | `BlockPos`        | New work position   |

**Throws:** `IllegalArgumentException` if any parameter is `null`.

```java
npcAPI.setNPCWork(npc, new BlockPos(150, 64, 250));
```

---

##### `setNPCType(CustomNPCEntity npc, NPCType type)` -> `void`

Changes the type/role of an NPC.

| Parameter | Type              | Description         |
|----------|-------------------|---------------------|
| `npc`     | `CustomNPCEntity` | The NPC entity      |
| `type`    | `NPCType`         | New NPC type        |

**Throws:** `IllegalArgumentException` if any parameter is `null`.

```java
npcAPI.setNPCType(npc, NPCType.MERCHANT);
```

---

### 4. IPoliceAPI -- Police / Wanted System

**Package:** `de.rolandsw.schedulemc.api.police`
**Implementation:** `de.rolandsw.schedulemc.api.impl.PoliceAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getPoliceAPI()`

A GTA-style 5-star wanted system with automatic decay and escape mechanics.

#### Wanted Level Reference

| Stars | Severity              | Examples                         |
|-------|-----------------------|----------------------------------|
| 0     | Clean                 | No active warrant                |
| 1-2   | Minor crime           | Theft, property damage           |
| 3-4   | Serious crime         | Robbery, assault                 |
| 5     | Maximum alert         | Murder, terrorism                |

#### Escape Mechanics

- A player can start an escape timer (30 seconds).
- The player must remain at least 40 blocks from police for the full duration.
- On success, the wanted level is reduced by 1 star.
- If police re-discover the player, the timer is stopped.

#### Automatic Decay

- For each in-game Minecraft day without committing a crime, the wanted level
  decays by 1 star.

#### Methods

---

##### `getWantedLevel(UUID playerUUID)` -> `int`

Returns the current wanted level of a player.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** Wanted level from 0 (clean) to 5 (maximum).

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
IPoliceAPI police = api.getPoliceAPI();
int stars = police.getWantedLevel(playerUUID);
if (stars >= 3) {
    // Player is a serious criminal
}
```

---

##### `addWantedLevel(UUID playerUUID, int amount)` -> `void`

Adds wanted stars to a player (capped at 5).

| Parameter    | Type   | Description                       |
|-------------|--------|-----------------------------------|
| `playerUUID` | `UUID` | The player's UUID                 |
| `amount`     | `int`  | Stars to add (must be >= 1)       |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null` or `amount` < 1.

```java
// Player committed theft: +1 star
police.addWantedLevel(playerUUID, 1);

// Player committed murder: +3 stars
police.addWantedLevel(playerUUID, 3);
```

---

##### `setWantedLevel(UUID playerUUID, int level)` -> `void`

Sets the wanted level to an exact value. The value is clamped to the range 0-5.

| Parameter    | Type   | Description                       |
|-------------|--------|-----------------------------------|
| `playerUUID` | `UUID` | The player's UUID                 |
| `level`      | `int`  | New wanted level (clamped to 0-5) |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
police.setWantedLevel(playerUUID, 5); // Maximum alert
police.setWantedLevel(playerUUID, 0); // Clear (same as clearWantedLevel)
```

---

##### `clearWantedLevel(UUID playerUUID)` -> `void`

Resets the wanted level to 0 (clean). Typically called after arrest or after
serving a prison sentence.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
// After the player is arrested and serves their sentence
police.clearWantedLevel(playerUUID);
```

---

##### `decayWantedLevel(UUID playerUUID)` -> `void`

Reduces the wanted level by 1 star. Represents one Minecraft day of natural
decay. Typically called automatically by the server each Minecraft day, but
can be invoked manually.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
// Called once per Minecraft day by server tick
police.decayWantedLevel(playerUUID);
```

---

##### `startEscape(UUID playerUUID)` -> `void`

Starts a 30-second escape timer for the player. If the player remains hidden
(40+ blocks from police) for the full 30 seconds, their wanted level is
reduced by 1.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
// Player enters escape zone
police.startEscape(playerUUID);
```

---

##### `stopEscape(UUID playerUUID)` -> `void`

Stops the escape timer. Called when police re-discover the player.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
// Police spotted the player again
police.stopEscape(playerUUID);
```

---

##### `isHiding(UUID playerUUID)` -> `boolean`

Checks whether the player currently has an active escape timer.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** `true` if the escape timer is active.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
if (police.isHiding(playerUUID)) {
    long remaining = police.getEscapeTimeRemaining(playerUUID);
    // Show HUD timer to player
}
```

---

##### `getEscapeTimeRemaining(UUID playerUUID)` -> `long`

Returns the remaining escape time in milliseconds.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** Remaining time in milliseconds. Returns `0` if no timer is active.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
long remainingMs = police.getEscapeTimeRemaining(playerUUID);
int remainingSeconds = (int)(remainingMs / 1000);
```

---

##### `checkEscapeSuccess(UUID playerUUID)` -> `boolean`

Checks if the escape timer has expired (30 seconds elapsed while hiding).
If successful, automatically reduces the wanted level by 1 and stops the timer.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** `true` if the escape was successful and the wanted level was reduced.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
if (police.checkEscapeSuccess(playerUUID)) {
    player.sendSystemMessage(Component.literal("You escaped! Wanted level reduced."));
}
```

---

### 5. IProductionAPI -- Production System

**Package:** `de.rolandsw.schedulemc.api.production`
**Implementation:** `de.rolandsw.schedulemc.api.impl.ProductionAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getProductionAPI()`

Manages the production chain system -- registration, lookup, and runtime
control of production processes. Supports 8 built-in production chains with
dynamic registration of custom productions at runtime.

#### Production Categories

```java
public enum ProductionCategory {
    PLANT,       // Plant-based production (Tobacco, Cannabis, Coca, Poppy)
    MUSHROOM,    // Mushroom cultivation (Psilocybe Cubensis, Golden Teacher)
    CHEMICAL,    // Chemical synthesis (MDMA, LSD, Meth)
    EXTRACT,     // Extracted products (Cocaine, Heroin)
    PROCESSED    // Processed/refined products (fermented, dried, etc.)
}
```

#### ProductionConfig Builder

Custom productions are defined using the Builder pattern:

```java
ProductionConfig custom = new ProductionConfig.Builder("custom_herb", "Custom Herb")
    .basePrice(20.0)
    .growthTicks(3600)       // 3 minutes at 20 TPS
    .baseYield(3)
    .category(ProductionConfig.ProductionCategory.PLANT)
    .requiresLight(true)
    .minLightLevel(8)
    .requiresWater(false)
    .build();
```

#### Methods

---

##### `getProduction(String productionId)` -> `@Nullable ProductionConfig`

Retrieves a production configuration by its unique ID.

| Parameter      | Type     | Description                                  |
|---------------|----------|----------------------------------------------|
| `productionId` | `String` | Production identifier (e.g., `"cannabis_indica"`) |

**Returns:** The `ProductionConfig`, or `null` if not found.

**Throws:** `IllegalArgumentException` if `productionId` is `null`.

**Performance:** O(1) via HashMap.

```java
IProductionAPI production = api.getProductionAPI();
ProductionConfig cannabis = production.getProduction("cannabis_indica");
if (cannabis != null) {
    double price = cannabis.getBasePrice();
    int growthTime = cannabis.getGrowthTicks();
}
```

---

##### `hasProduction(String productionId)` -> `boolean`

Checks whether a production with the given ID is registered.

| Parameter      | Type     | Description            |
|---------------|----------|------------------------|
| `productionId` | `String` | Production identifier  |

**Returns:** `true` if the production exists.

**Throws:** `IllegalArgumentException` if `productionId` is `null`.

```java
if (production.hasProduction("meth_crystal")) {
    // Production is registered
}
```

---

##### `getAllProductions()` -> `Collection<ProductionConfig>`

Returns all registered production configurations.

**Returns:** An unmodifiable copy of all production configs.

```java
Collection<ProductionConfig> allProductions = production.getAllProductions();
for (ProductionConfig config : allProductions) {
    LOGGER.info("{}: {} Euro", config.getDisplayName(), config.getBasePrice());
}
```

---

##### `getProductionsByCategory(ProductionConfig.ProductionCategory category)` -> `List<ProductionConfig>`

Returns all productions in a specific category.

| Parameter  | Type                                       | Description            |
|-----------|--------------------------------------------|------------------------|
| `category` | `ProductionConfig.ProductionCategory`       | The category to filter |

**Returns:** List of matching productions. May be empty, never `null`.

**Throws:** `IllegalArgumentException` if `category` is `null`.

```java
List<ProductionConfig> plants = production.getProductionsByCategory(
    ProductionConfig.ProductionCategory.PLANT
);

List<ProductionConfig> chemicals = production.getProductionsByCategory(
    ProductionConfig.ProductionCategory.CHEMICAL
);
```

---

##### `registerProduction(ProductionConfig config)` -> `void`

Registers a new production configuration. If a production with the same ID
already exists, it is overwritten.

| Parameter | Type               | Description                    |
|----------|--------------------|--------------------------------|
| `config`  | `ProductionConfig` | The production to register     |

**Throws:** `IllegalArgumentException` if `config` is `null`.

> **Warning:** Overwrites any existing production with the same ID.

```java
ProductionConfig custom = new ProductionConfig.Builder("custom_plant", "Custom Plant")
    .basePrice(20.0)
    .growthTicks(3600)
    .category(ProductionConfig.ProductionCategory.PLANT)
    .build();

production.registerProduction(custom);
```

---

##### `unregisterProduction(String productionId)` -> `boolean`

Removes a registered production configuration.

| Parameter      | Type     | Description            |
|---------------|----------|------------------------|
| `productionId` | `String` | Production identifier  |

**Returns:** `true` if the production was removed, `false` if it did not exist.

**Throws:** `IllegalArgumentException` if `productionId` is `null`.

```java
boolean removed = production.unregisterProduction("custom_plant");
```

---

##### `getProductionCount()` -> `int`

Returns the total number of registered productions.

**Returns:** Production count.

```java
int count = production.getProductionCount();
```

---

##### `startProduction(BlockPos position, String productionId)` -> `boolean`

**[Stub]** Starts a production process at a block position. Requires a
`ProcessingBlockEntity` at the position.

| Parameter      | Type       | Description                     |
|---------------|------------|---------------------------------|
| `position`     | `BlockPos` | Position of the processing block |
| `productionId` | `String`   | Production to start             |

**Returns:** `true` if production started. Currently returns `false` (stub).

**Throws:** `IllegalArgumentException` if any parameter is `null`.

> **Note:** This method is a stub in the current implementation. It will be
> fully implemented in a future version when `BlockEntity` access is integrated
> into the API layer.

---

##### `stopProduction(BlockPos position)` -> `boolean`

**[Stub]** Stops an active production at a block position.

| Parameter  | Type       | Description                       |
|-----------|------------|-----------------------------------|
| `position` | `BlockPos` | Position of the processing block  |

**Returns:** `true` if stopped. Currently returns `false` (stub).

**Throws:** `IllegalArgumentException` if `position` is `null`.

---

##### `getProductionProgress(BlockPos position)` -> `double`

**[Stub]** Returns the progress of an active production.

| Parameter  | Type       | Description                       |
|-----------|------------|-----------------------------------|
| `position` | `BlockPos` | Position of the processing block  |

**Returns:** Progress as a percentage (0.0 to 100.0), or `-1.0` if no production
is active. Currently returns `-1.0` (stub).

**Throws:** `IllegalArgumentException` if `position` is `null`.

---

### 6. IVehicleAPI -- Vehicle System

**Package:** `de.rolandsw.schedulemc.api.vehicle`
**Implementation:** `de.rolandsw.schedulemc.api.impl.VehicleAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getVehicleAPI()`

Manages vehicles -- spawning, ownership, fuel, and removal.

#### Methods

---

##### `spawnVehicle(ServerLevel level, BlockPos position, String vehicleType)` -> `@Nullable EntityGenericVehicle`

**[Stub]** Spawns a vehicle at the given position.

| Parameter     | Type          | Description                                 |
|--------------|---------------|---------------------------------------------|
| `level`       | `ServerLevel` | The server level                            |
| `position`    | `BlockPos`    | Spawn position                              |
| `vehicleType` | `String`      | Vehicle type identifier (e.g., `"car_sports"`) |

**Returns:** The spawned `EntityGenericVehicle`, or `null` on failure.
Currently returns `null` (stub -- requires VehicleRegistry integration).

**Throws:** `IllegalArgumentException` if any parameter is `null`.

```java
IVehicleAPI vehicles = api.getVehicleAPI();
EntityGenericVehicle car = vehicles.spawnVehicle(level, spawnPos, "car_sports");
if (car != null) {
    vehicles.setVehicleOwner(car, playerUUID);
}
```

---

##### `setVehicleOwner(EntityGenericVehicle vehicle, UUID ownerUUID)` -> `void`

Sets the owner of a vehicle.

| Parameter   | Type                   | Description           |
|------------|------------------------|-----------------------|
| `vehicle`   | `EntityGenericVehicle` | The vehicle entity    |
| `ownerUUID` | `UUID`                 | New owner's UUID      |

**Throws:** `IllegalArgumentException` if any parameter is `null`.

```java
vehicles.setVehicleOwner(vehicle, playerUUID);
```

---

##### `getVehicleOwner(EntityGenericVehicle vehicle)` -> `@Nullable UUID`

Returns the UUID of the vehicle's owner.

| Parameter | Type                   | Description           |
|----------|------------------------|-----------------------|
| `vehicle` | `EntityGenericVehicle` | The vehicle entity    |

**Returns:** Owner's UUID, or `null` if no owner is set.

**Throws:** `IllegalArgumentException` if `vehicle` is `null`.

```java
UUID owner = vehicles.getVehicleOwner(vehicle);
if (owner != null && owner.equals(playerUUID)) {
    // Player owns this vehicle
}
```

---

##### `refuelVehicle(EntityGenericVehicle vehicle, double amount)` -> `boolean`

Adds fuel to a vehicle.

| Parameter | Type                   | Description                       |
|----------|------------------------|-----------------------------------|
| `vehicle` | `EntityGenericVehicle` | The vehicle entity                |
| `amount`  | `double`               | Fuel amount in liters (must be > 0) |

**Returns:** `true` if fuel was added, `false` if the tank was already full.

**Throws:** `IllegalArgumentException` if `vehicle` is `null` or `amount` <= 0.

```java
boolean fueled = vehicles.refuelVehicle(vehicle, 50.0);
if (!fueled) {
    player.sendSystemMessage(Component.literal("Tank is already full!"));
}
```

---

##### `getFuelLevel(EntityGenericVehicle vehicle)` -> `double`

Returns the current fuel level.

| Parameter | Type                   | Description           |
|----------|------------------------|-----------------------|
| `vehicle` | `EntityGenericVehicle` | The vehicle entity    |

**Returns:** Current fuel in liters.

**Throws:** `IllegalArgumentException` if `vehicle` is `null`.

```java
double fuel = vehicles.getFuelLevel(vehicle);
double capacity = vehicles.getFuelCapacity(vehicle);
double percentage = (fuel / capacity) * 100.0;
```

---

##### `getFuelCapacity(EntityGenericVehicle vehicle)` -> `double`

Returns the maximum fuel tank capacity.

| Parameter | Type                   | Description           |
|----------|------------------------|-----------------------|
| `vehicle` | `EntityGenericVehicle` | The vehicle entity    |

**Returns:** Maximum fuel capacity in liters.

**Throws:** `IllegalArgumentException` if `vehicle` is `null`.

```java
double maxFuel = vehicles.getFuelCapacity(vehicle);
```

---

##### `getPlayerVehicles(ServerLevel level, UUID ownerUUID)` -> `List<EntityGenericVehicle>`

Returns all vehicles owned by a player in a specific level.

| Parameter   | Type          | Description           |
|------------|---------------|-----------------------|
| `level`     | `ServerLevel` | The server level      |
| `ownerUUID` | `UUID`        | The owner's UUID      |

**Returns:** An unmodifiable list of owned vehicles. May be empty, never `null`.

**Throws:** `IllegalArgumentException` if any parameter is `null`.

```java
List<EntityGenericVehicle> myVehicles = vehicles.getPlayerVehicles(level, playerUUID);
```

---

##### `removeVehicle(EntityGenericVehicle vehicle)` -> `void`

Removes a vehicle from the world (calls `discard()` on the entity).

| Parameter | Type                   | Description           |
|----------|------------------------|-----------------------|
| `vehicle` | `EntityGenericVehicle` | The vehicle to remove |

**Throws:** `IllegalArgumentException` if `vehicle` is `null`.

```java
vehicles.removeVehicle(vehicle);
```

---

### 7. IWarehouseAPI -- Warehouse System

**Package:** `de.rolandsw.schedulemc.api.warehouse`
**Implementation:** `de.rolandsw.schedulemc.api.impl.WarehouseAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getWarehouseAPI()`

Manages warehouse inventory, item slots, capacities, and seller assignments.

> **Note:** All methods in this API are currently stubs. The underlying
> `WarehouseManager` uses plot IDs rather than `BlockPos` for lookups, so
> full integration is pending. Methods will validate inputs and return safe
> default values.

#### Methods

---

##### `hasWarehouse(BlockPos position)` -> `boolean`

Checks whether a warehouse exists at the given position.

| Parameter  | Type       | Description           |
|-----------|------------|-----------------------|
| `position` | `BlockPos` | The block position    |

**Returns:** `true` if a warehouse exists. Currently returns `false` (stub).

**Throws:** `IllegalArgumentException` if `position` is `null`.

```java
IWarehouseAPI warehouse = api.getWarehouseAPI();
if (warehouse.hasWarehouse(blockPos)) {
    // Warehouse exists
}
```

---

##### `addItemToWarehouse(BlockPos position, Item item, int amount)` -> `boolean`

Adds items to a warehouse's inventory.

| Parameter  | Type       | Description                    |
|-----------|------------|--------------------------------|
| `position` | `BlockPos` | Warehouse position             |
| `item`     | `Item`     | The item type to add           |
| `amount`   | `int`      | Quantity to add (must be >= 1) |

**Returns:** `true` if items were added. Currently returns `false` (stub).

**Throws:** `IllegalArgumentException` if parameters are `null` or `amount` < 1.

```java
warehouse.addItemToWarehouse(pos, Items.DIAMOND, 10);
```

---

##### `removeItemFromWarehouse(BlockPos position, Item item, int amount)` -> `boolean`

Removes items from a warehouse's inventory.

| Parameter  | Type       | Description                       |
|-----------|------------|-----------------------------------|
| `position` | `BlockPos` | Warehouse position                |
| `item`     | `Item`     | The item type to remove           |
| `amount`   | `int`      | Quantity to remove (must be >= 1) |

**Returns:** `true` if items were removed. Currently returns `false` (stub).

**Throws:** `IllegalArgumentException` if parameters are `null` or `amount` < 1.

```java
warehouse.removeItemFromWarehouse(pos, Items.DIAMOND, 5);
```

---

##### `getItemStock(BlockPos position, Item item)` -> `int`

Returns the current stock of an item in the warehouse.

| Parameter  | Type       | Description           |
|-----------|------------|-----------------------|
| `position` | `BlockPos` | Warehouse position    |
| `item`     | `Item`     | The item to query     |

**Returns:** Current quantity. Currently returns `0` (stub).

**Throws:** `IllegalArgumentException` if parameters are `null`.

```java
int diamonds = warehouse.getItemStock(pos, Items.DIAMOND);
```

---

##### `getItemCapacity(BlockPos position, Item item)` -> `int`

Returns the maximum capacity for an item in the warehouse.

| Parameter  | Type       | Description           |
|-----------|------------|-----------------------|
| `position` | `BlockPos` | Warehouse position    |
| `item`     | `Item`     | The item to query     |

**Returns:** Maximum capacity. Currently returns `0` (stub).

**Throws:** `IllegalArgumentException` if parameters are `null`.

```java
int maxDiamonds = warehouse.getItemCapacity(pos, Items.DIAMOND);
```

---

##### `getAllSlots(BlockPos position)` -> `List<WarehouseSlot>`

Returns all inventory slots of a warehouse.

| Parameter  | Type       | Description           |
|-----------|------------|-----------------------|
| `position` | `BlockPos` | Warehouse position    |

**Returns:** List of `WarehouseSlot` objects. Currently returns an empty list (stub).

**Throws:** `IllegalArgumentException` if `position` is `null`.

```java
List<WarehouseSlot> slots = warehouse.getAllSlots(pos);
for (WarehouseSlot slot : slots) {
    // Process slot data
}
```

---

##### `addSeller(BlockPos position, UUID sellerUUID)` -> `void`

Assigns a player as a seller for a warehouse.

| Parameter    | Type       | Description           |
|-------------|------------|-----------------------|
| `position`   | `BlockPos` | Warehouse position    |
| `sellerUUID` | `UUID`     | Seller's UUID         |

**Throws:** `IllegalArgumentException` if parameters are `null`.

```java
warehouse.addSeller(pos, playerUUID);
```

---

##### `removeSeller(BlockPos position, UUID sellerUUID)` -> `void`

Removes a seller from a warehouse.

| Parameter    | Type       | Description           |
|-------------|------------|-----------------------|
| `position`   | `BlockPos` | Warehouse position    |
| `sellerUUID` | `UUID`     | Seller's UUID         |

**Throws:** `IllegalArgumentException` if parameters are `null`.

```java
warehouse.removeSeller(pos, playerUUID);
```

---

##### `isSeller(BlockPos position, UUID sellerUUID)` -> `boolean`

Checks whether a player is a registered seller at a warehouse.

| Parameter    | Type       | Description           |
|-------------|------------|-----------------------|
| `position`   | `BlockPos` | Warehouse position    |
| `sellerUUID` | `UUID`     | Player's UUID         |

**Returns:** `true` if the player is a seller. Currently returns `false` (stub).

**Throws:** `IllegalArgumentException` if parameters are `null`.

```java
if (warehouse.isSeller(pos, playerUUID)) {
    // Player can sell at this warehouse
}
```

---

### 8. IMessagingAPI -- Messaging System

**Package:** `de.rolandsw.schedulemc.api.messaging`
**Implementation:** `de.rolandsw.schedulemc.api.impl.MessagingAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getMessagingAPI()`

Player-to-player messaging with message history, read/unread tracking, and
deletion.

#### Methods

---

##### `sendMessage(UUID fromUUID, UUID toUUID, String message)` -> `boolean`

Sends a message from one player to another.

| Parameter  | Type     | Description                           |
|-----------|----------|---------------------------------------|
| `fromUUID` | `UUID`   | Sender's UUID                         |
| `toUUID`   | `UUID`   | Recipient's UUID                      |
| `message`  | `String` | Message text (must not be empty)      |

**Returns:** `true` if the message was sent successfully.

**Throws:** `IllegalArgumentException` if UUIDs are `null` or message is `null`/empty.

```java
IMessagingAPI messaging = api.getMessagingAPI();
messaging.sendMessage(senderUUID, recipientUUID, "Hello! Want to trade?");
```

---

##### `getUnreadMessageCount(UUID playerUUID)` -> `int`

Returns the number of unread messages for a player.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** Number of unread messages.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
int unread = messaging.getUnreadMessageCount(playerUUID);
if (unread > 0) {
    player.sendSystemMessage(Component.literal("You have " + unread + " unread messages"));
}
```

---

##### `getMessages(UUID playerUUID, int limit)` -> `List<String>`

Returns the most recent messages for a player, sorted newest first.

| Parameter    | Type   | Description                        |
|-------------|--------|------------------------------------|
| `playerUUID` | `UUID` | The player's UUID                  |
| `limit`      | `int`  | Maximum number of messages (>= 1)  |

**Returns:** List of message content strings, newest first.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null` or `limit` < 1.

```java
List<String> recentMessages = messaging.getMessages(playerUUID, 10);
for (String msg : recentMessages) {
    LOGGER.info("Message: {}", msg);
}
```

---

##### `markAllAsRead(UUID playerUUID)` -> `void`

Marks all messages for a player as read.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
messaging.markAllAsRead(playerUUID);
```

---

##### `deleteMessage(UUID playerUUID, String messageId)` -> `boolean`

Deletes a specific message.

| Parameter    | Type     | Description          |
|-------------|----------|----------------------|
| `playerUUID` | `UUID`   | The player's UUID    |
| `messageId`  | `String` | The message ID       |

**Returns:** `true` if the message was deleted.

**Throws:** `IllegalArgumentException` if parameters are `null`.

```java
boolean deleted = messaging.deleteMessage(playerUUID, "msg_12345");
```

---

##### `deleteAllMessages(UUID playerUUID)` -> `void`

Deletes all messages for a player.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
messaging.deleteAllMessages(playerUUID);
```

---

##### `getTotalMessageCount(UUID playerUUID)` -> `int`

Returns the total number of messages for a player across all conversations.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** Total message count.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
int total = messaging.getTotalMessageCount(playerUUID);
```

---

### 9. ISmartphoneAPI -- Smartphone GUI Tracking

**Package:** `de.rolandsw.schedulemc.api.smartphone`
**Implementation:** `de.rolandsw.schedulemc.api.impl.SmartphoneAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getSmartphoneAPI()`

Tracks which players have the smartphone GUI open. While the smartphone is open,
a protection mechanism prevents the player from being attacked, taking damage,
or losing items. This prevents unfair deaths during GUI interaction.

#### Smartphone Apps

The in-game smartphone includes these apps:

- **Messaging** -- Send messages to other players
- **Achievements** -- View progress and unlocked achievements
- **Market** -- Browse market prices and trends
- **Plots** -- Manage owned plots
- **Bank** -- View account balance and transactions

#### Protection Mechanism

When the smartphone is open, the player cannot:
- Be attacked by NPCs
- Be attacked by other players
- Lose items

#### Methods

---

##### `setSmartphoneOpen(UUID playerUUID, boolean open)` -> `void`

Sets whether a player has the smartphone open. When `true`, the protection
mechanism activates. When `false`, it deactivates.

| Parameter    | Type      | Description                          |
|-------------|-----------|--------------------------------------|
| `playerUUID` | `UUID`    | The player's UUID                    |
| `open`       | `boolean` | `true` to open, `false` to close     |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
ISmartphoneAPI smartphone = api.getSmartphoneAPI();

// Player opens smartphone
smartphone.setSmartphoneOpen(playerUUID, true);

// Player closes smartphone
smartphone.setSmartphoneOpen(playerUUID, false);
```

---

##### `hasSmartphoneOpen(UUID playerUUID)` -> `boolean`

Checks whether a player currently has the smartphone open.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** `true` if the smartphone is open (protection active).

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
if (smartphone.hasSmartphoneOpen(targetUUID)) {
    // Player is protected -- do not apply damage
    return;
}
```

---

##### `removePlayer(UUID playerUUID)` -> `void`

Removes a player from the tracking set. Should be called on player disconnect
to free memory.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
// In your PlayerLoggedOutEvent handler
smartphone.removePlayer(playerUUID);
```

---

##### `getPlayersWithSmartphoneOpen()` -> `Set<UUID>`

Returns the set of all players who currently have the smartphone open.

**Returns:** An unmodifiable set of UUIDs. May be empty, never `null`.

```java
Set<UUID> openPhones = smartphone.getPlayersWithSmartphoneOpen();
LOGGER.info("{} players are using their smartphones", openPhones.size());
```

---

##### `clearAllTracking()` -> `void`

Clears all tracking data. Should only be used during server shutdown or testing.

> **Warning:** This removes all smartphone open/close state. Use only at server
> shutdown or in test environments.

```java
// During server shutdown
smartphone.clearAllTracking();
```

---

##### `getOpenSmartphoneCount()` -> `int`

Returns the number of players currently using the smartphone.

**Returns:** Count of players with open smartphones.

```java
int count = smartphone.getOpenSmartphoneCount();
```

---

### 10. IAchievementAPI -- Achievement System

**Package:** `de.rolandsw.schedulemc.api.achievement`
**Implementation:** `de.rolandsw.schedulemc.api.impl.AchievementAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getAchievementAPI()`

A full achievement system with categories, tiers, incremental progress tracking,
and automatic monetary rewards on unlock.

#### Achievement Categories

```java
public enum AchievementCategory {
    ECONOMY,       // Earning money, loans, saving
    CRIME,         // Wanted level, escapes, prison
    PRODUCTION,    // Growing plants, building empires
    SOCIAL,        // Plots, rentals, ratings
    EXPLORATION    // Exploration-related achievements
}
```

#### Achievement Tiers and Rewards

| Tier       | Reward      |
|-----------|-------------|
| BRONZE     | 100 Euro    |
| SILVER     | 500 Euro    |
| GOLD       | 2,500 Euro  |
| DIAMOND    | 10,000 Euro |
| PLATINUM   | 50,000 Euro |

When an achievement is unlocked (either via progress reaching the requirement
or via manual unlock), the corresponding reward is automatically deposited into
the player's economy account.

#### Methods

---

##### `getPlayerAchievements(UUID playerUUID)` -> `PlayerAchievements`

Returns the full achievement data for a player. Creates a new empty record if
the player has no existing data.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** `PlayerAchievements` object (never `null`).

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
IAchievementAPI achievements = api.getAchievementAPI();
PlayerAchievements playerAch = achievements.getPlayerAchievements(playerUUID);
int unlocked = playerAch.getUnlockedCount();
double totalRewards = playerAch.getTotalPointsEarned();
```

---

##### `addProgress(UUID playerUUID, String achievementId, double amount)` -> `void`

Adds incremental progress to an achievement. If the progress reaches or exceeds
the requirement, the achievement is automatically unlocked and the reward is paid.

| Parameter      | Type     | Description                                  |
|---------------|----------|----------------------------------------------|
| `playerUUID`   | `UUID`   | The player's UUID                            |
| `achievementId`| `String` | Achievement identifier (e.g., `"FIRST_EURO"`)  |
| `amount`       | `double` | Progress amount to add                       |

**Throws:** `IllegalArgumentException` if `playerUUID` or `achievementId` is `null`.

```java
// Player earned some money -- increment the "earn money" achievement
achievements.addProgress(playerUUID, "FIRST_EURO", 1.0);

// Player harvested plants
achievements.addProgress(playerUUID, "GREEN_THUMB", 5.0);
```

---

##### `setProgress(UUID playerUUID, String achievementId, double value)` -> `void`

Sets the progress of an achievement to an absolute value (overwrites previous
progress). Automatically unlocks if the requirement is met.

| Parameter      | Type     | Description                                |
|---------------|----------|--------------------------------------------|
| `playerUUID`   | `UUID`   | The player's UUID                          |
| `achievementId`| `String` | Achievement identifier                     |
| `value`        | `double` | New progress value (absolute)              |

**Throws:** `IllegalArgumentException` if `playerUUID` or `achievementId` is `null`.

```java
// Set balance-tracking achievement to current balance
achievements.setProgress(playerUUID, "RICH", economy.getBalance(playerUUID));
```

---

##### `unlockAchievement(UUID playerUUID, String achievementId)` -> `boolean`

Manually unlocks an achievement and pays the reward regardless of progress.

| Parameter      | Type     | Description              |
|---------------|----------|--------------------------|
| `playerUUID`   | `UUID`   | The player's UUID        |
| `achievementId`| `String` | Achievement identifier   |

**Returns:** `true` if the achievement was newly unlocked, `false` if it was
already unlocked.

**Throws:** `IllegalArgumentException` if `playerUUID` or `achievementId` is `null`.

```java
boolean newUnlock = achievements.unlockAchievement(playerUUID, "MILLIONAIRE");
if (newUnlock) {
    player.sendSystemMessage(Component.literal("Achievement unlocked: Millionaire!"));
}
```

---

##### `getAchievement(String achievementId)` -> `@Nullable Achievement`

Retrieves the definition of an achievement by ID.

| Parameter      | Type     | Description              |
|---------------|----------|--------------------------|
| `achievementId`| `String` | Achievement identifier   |

**Returns:** The `Achievement` object, or `null` if not found.

**Throws:** `IllegalArgumentException` if `achievementId` is `null`.

```java
Achievement ach = achievements.getAchievement("FIRST_EURO");
if (ach != null) {
    String name = ach.getName();
    AchievementCategory category = ach.getCategory();
}
```

---

##### `getAllAchievements()` -> `Collection<Achievement>`

Returns all registered achievements.

**Returns:** An unmodifiable collection of all achievements.

```java
Collection<Achievement> allAch = achievements.getAllAchievements();
LOGGER.info("Total achievements: {}", allAch.size());
```

---

##### `getAchievementsByCategory(AchievementCategory category)` -> `List<Achievement>`

Returns all achievements in a specific category.

| Parameter  | Type                  | Description             |
|-----------|------------------------|-------------------------|
| `category` | `AchievementCategory` | The category to filter  |

**Returns:** List of matching achievements. May be empty, never `null`.

**Throws:** `IllegalArgumentException` if `category` is `null`.

```java
List<Achievement> economyAch = achievements.getAchievementsByCategory(
    AchievementCategory.ECONOMY
);

List<Achievement> crimeAch = achievements.getAchievementsByCategory(
    AchievementCategory.CRIME
);
```

---

##### `getStatistics(UUID playerUUID)` -> `String`

Returns a formatted statistics string for a player's achievements.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** Formatted string, e.g., `"Achievements: 5/20 (25.0%) - 3100.00 Euro earned"`.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
String stats = achievements.getStatistics(playerUUID);
player.sendSystemMessage(Component.literal(stats));
// Output: "Achievements: 5/20 (25.0%) - €3100.00 verdient"
```

---

##### `getTotalAchievementCount()` -> `int`

Returns the total number of registered achievements.

**Returns:** Total achievement count.

```java
int total = achievements.getTotalAchievementCount();
```

---

##### `getUnlockedCount(UUID playerUUID)` -> `int`

Returns the number of achievements a player has unlocked.

| Parameter    | Type   | Description          |
|-------------|--------|----------------------|
| `playerUUID` | `UUID` | The player's UUID    |

**Returns:** Number of unlocked achievements.

**Throws:** `IllegalArgumentException` if `playerUUID` is `null`.

```java
int unlocked = achievements.getUnlockedCount(playerUUID);
int total = achievements.getTotalAchievementCount();
LOGGER.info("Player unlocked {}/{}", unlocked, total);
```

---

##### `getProgress(UUID playerUUID, String achievementId)` -> `double`

Returns the current progress of a specific achievement for a player.

| Parameter      | Type     | Description              |
|---------------|----------|--------------------------|
| `playerUUID`   | `UUID`   | The player's UUID        |
| `achievementId`| `String` | Achievement identifier   |

**Returns:** Current progress value. Returns `0.0` if no progress exists.

**Throws:** `IllegalArgumentException` if `playerUUID` or `achievementId` is `null`.

```java
double progress = achievements.getProgress(playerUUID, "RICH");
LOGGER.info("Progress towards RICH achievement: {}", progress);
```

---

##### `isUnlocked(UUID playerUUID, String achievementId)` -> `boolean`

Checks whether a specific achievement is unlocked for a player.

| Parameter      | Type     | Description              |
|---------------|----------|--------------------------|
| `playerUUID`   | `UUID`   | The player's UUID        |
| `achievementId`| `String` | Achievement identifier   |

**Returns:** `true` if the achievement is unlocked.

**Throws:** `IllegalArgumentException` if `playerUUID` or `achievementId` is `null`.

```java
if (achievements.isUnlocked(playerUUID, "MILLIONAIRE")) {
    // Grant special permissions or rewards
}
```

---

### 11. IMarketAPI -- Dynamic Market

**Package:** `de.rolandsw.schedulemc.api.market`
**Implementation:** `de.rolandsw.schedulemc.api.impl.MarketAPIImpl`
**Access:** `ScheduleMCAPI.getInstance().getMarketAPI()`

A supply-and-demand dynamic market system. Prices fluctuate based on player
purchases and sales.

#### Price Dynamics

- **Buying** items increases demand, which raises the price.
- **Selling** items increases supply, which lowers the price.
- The `priceMultiplier` ranges from approximately 0.5 to 2.0 (50% to 200%
  of the base price).
- Supply and demand levels are normalized to a 0-100 scale.

#### Methods

---

##### `getCurrentPrice(Item item)` -> `double`

Returns the current market price of an item, reflecting supply and demand.

| Parameter | Type   | Description      |
|----------|--------|------------------|
| `item`    | `Item` | The Minecraft item |

**Returns:** Current price in Euro.

**Throws:** `IllegalArgumentException` if `item` is `null`.

```java
IMarketAPI market = api.getMarketAPI();
double price = market.getCurrentPrice(Items.DIAMOND);
player.sendSystemMessage(Component.literal("Diamond price: " + price + " Euro"));
```

---

##### `getBasePrice(Item item)` -> `double`

Returns the base price of an item, before any dynamic adjustments.

| Parameter | Type   | Description      |
|----------|--------|------------------|
| `item`    | `Item` | The Minecraft item |

**Returns:** Base price in Euro.

**Throws:** `IllegalArgumentException` if `item` is `null`.

```java
double basePrice = market.getBasePrice(Items.DIAMOND);
```

---

##### `recordPurchase(Item item, int amount)` -> `void`

Records a purchase event, increasing demand and raising the price.

| Parameter | Type   | Description                    |
|----------|--------|--------------------------------|
| `item`    | `Item` | The purchased item             |
| `amount`  | `int`  | Quantity purchased (must be >= 1) |

**Throws:** `IllegalArgumentException` if `item` is `null` or `amount` < 1.

```java
// Player bought 10 diamonds from an NPC shop
market.recordPurchase(Items.DIAMOND, 10);
// Diamond price will increase
```

---

##### `recordSale(Item item, int amount)` -> `void`

Records a sale event, increasing supply and lowering the price.

| Parameter | Type   | Description                 |
|----------|--------|-----------------------------|
| `item`    | `Item` | The sold item               |
| `amount`  | `int`  | Quantity sold (must be >= 1) |

**Throws:** `IllegalArgumentException` if `item` is `null` or `amount` < 1.

```java
// Player sold 5 diamonds to an NPC shop
market.recordSale(Items.DIAMOND, 5);
// Diamond price will decrease
```

---

##### `getPriceMultiplier(Item item)` -> `double`

Returns the current price multiplier relative to the base price.

| Parameter | Type   | Description      |
|----------|--------|------------------|
| `item`    | `Item` | The Minecraft item |

**Returns:** Multiplier value. `1.0` = base price, `1.5` = 50% more expensive,
`0.8` = 20% cheaper. Typical range: 0.5 to 2.0.

**Throws:** `IllegalArgumentException` if `item` is `null`.

```java
double multiplier = market.getPriceMultiplier(Items.DIAMOND);
if (multiplier > 1.5) {
    LOGGER.info("Diamonds are expensive right now!");
}
```

---

##### `getDemandLevel(Item item)` -> `int`

Returns the current demand level for an item.

| Parameter | Type   | Description      |
|----------|--------|------------------|
| `item`    | `Item` | The Minecraft item |

**Returns:** Demand level from 0 (no demand) to 100 (maximum demand).

**Throws:** `IllegalArgumentException` if `item` is `null`.

```java
int demand = market.getDemandLevel(Items.DIAMOND);
```

---

##### `getSupplyLevel(Item item)` -> `int`

Returns the current supply level for an item.

| Parameter | Type   | Description      |
|----------|--------|------------------|
| `item`    | `Item` | The Minecraft item |

**Returns:** Supply level from 0 (no supply) to 100 (maximum supply).

**Throws:** `IllegalArgumentException` if `item` is `null`.

```java
int supply = market.getSupplyLevel(Items.DIAMOND);
```

---

##### `getAllPrices()` -> `Map<Item, Double>`

Returns all tracked market prices.

**Returns:** Map of item to current price.

```java
Map<Item, Double> prices = market.getAllPrices();
for (Map.Entry<Item, Double> entry : prices.entrySet()) {
    LOGGER.info("{}: {} Euro", entry.getKey(), entry.getValue());
}
```

---

##### `setBasePrice(Item item, double basePrice)` -> `void`

**[Admin]** Sets the base price of an item.

| Parameter   | Type     | Description                          |
|------------|----------|--------------------------------------|
| `item`      | `Item`   | The Minecraft item                   |
| `basePrice` | `double` | New base price (must be >= 0)        |

**Throws:** `IllegalArgumentException` if `item` is `null` or `basePrice` < 0.

```java
// Admin command: set diamond base price
market.setBasePrice(Items.DIAMOND, 100.0);
```

---

##### `resetMarketData(@Nullable Item item)` -> `void`

**[Admin]** Resets market data. Pass a specific item to reset only that item,
or pass `null` to reset all market data.

| Parameter | Type   | Description                             |
|----------|--------|-----------------------------------------|
| `item`    | `Item` | The item to reset, or `null` for all    |

```java
// Reset a specific item
market.resetMarketData(Items.DIAMOND);

// Reset all market data
market.resetMarketData(null);
```

---

### 12. PlotModAPI -- Legacy Static Utilities

**Package:** `de.rolandsw.schedulemc.api`
**Access:** Static methods via `PlotModAPI.Economy`, `PlotModAPI.Plots`,
`PlotModAPI.Daily`, `PlotModAPI.Util`

> **Deprecated:** This is the legacy v1/v2 API. New integrations should use the
> modern `ScheduleMCAPI.getInstance()` approach described above. `PlotModAPI`
> remains available for backward compatibility.

The legacy API uses static nested classes with `ServerPlayer` parameters instead
of `UUID` parameters.

#### PlotModAPI.Economy

```java
// Get balance
double balance = PlotModAPI.Economy.getBalance(serverPlayer);
double balance = PlotModAPI.Economy.getBalance(uuid);

// Give money
PlotModAPI.Economy.giveMoney(serverPlayer, 1000.0);

// Take money (returns false if insufficient funds)
boolean success = PlotModAPI.Economy.takeMoney(serverPlayer, 500.0);

// Check affordability
boolean canAfford = PlotModAPI.Economy.hasEnoughMoney(serverPlayer, 250.0);

// Set balance directly
PlotModAPI.Economy.setBalance(serverPlayer, 0.0);

// Transfer between players
boolean transferred = PlotModAPI.Economy.transferMoney(fromPlayer, toPlayer, 100.0);
```

#### PlotModAPI.Plots

```java
// Get plot at position
PlotRegion plot = PlotModAPI.Plots.getPlotAt(blockPos);
PlotRegion plot = PlotModAPI.Plots.getPlotAt(serverPlayer);

// Check if player is in a plot
boolean inPlot = PlotModAPI.Plots.isPlayerInPlot(serverPlayer);

// Check if player is in their own plot
boolean inOwn = PlotModAPI.Plots.isPlayerInOwnPlot(serverPlayer);

// Check plot access (owner, trusted, or renter)
boolean hasAccess = PlotModAPI.Plots.hasPlotAccess(serverPlayer, blockPos);

// Get all player's plots
List<PlotRegion> plots = PlotModAPI.Plots.getPlayerPlots(serverPlayer);
int plotCount = PlotModAPI.Plots.getPlayerPlotCount(serverPlayer);

// Check trusted status
boolean trusted = PlotModAPI.Plots.isPlayerTrusted(serverPlayer);

// Get plot metadata
String name = PlotModAPI.Plots.getPlotName(blockPos);
double rating = PlotModAPI.Plots.getPlotRating(blockPos);
```

#### PlotModAPI.Daily

```java
// Check if daily reward was claimed today
boolean claimed = PlotModAPI.Daily.hasClaimedToday(serverPlayer);

// Get current streak
int streak = PlotModAPI.Daily.getStreak(serverPlayer);

// Get longest streak ever
int longestStreak = PlotModAPI.Daily.getLongestStreak(serverPlayer);
```

#### PlotModAPI.Util

```java
// Format money
String formatted = PlotModAPI.Util.formatMoney(1234.56);
// Result: "1234.56 Euro"

// Check if ScheduleMC is loaded
boolean loaded = PlotModAPI.Util.isPlotModLoaded();

// Get version
String version = PlotModAPI.Util.getVersion(); // "3.0"
```

---

## Event Integration

ScheduleMC's internal systems fire Forge events that your mod can listen to.
Economy transactions, plot changes, and achievement unlocks are processed
through the standard Forge event bus.

### Recommended Event Patterns

Listen for changes by subscribing to Forge events in your mod:

```java
@Mod.EventBusSubscriber(modid = "yourmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScheduleMCEventListener {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ScheduleMCAPI api = ScheduleMCAPI.getInstance();
            if (api.isInitialized()) {
                IEconomyAPI economy = api.getEconomyAPI();

                // Ensure the player has an account
                if (!economy.hasAccount(player.getUUID())) {
                    economy.createAccount(player.getUUID());
                }

                // Show unread messages
                IMessagingAPI messaging = api.getMessagingAPI();
                int unread = messaging.getUnreadMessageCount(player.getUUID());
                if (unread > 0) {
                    player.sendSystemMessage(
                        Component.literal("You have " + unread + " unread messages!")
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ScheduleMCAPI api = ScheduleMCAPI.getInstance();
            if (api.isInitialized()) {
                // Clean up smartphone tracking
                api.getSmartphoneAPI().removePlayer(player.getUUID());
            }
        }
    }
}
```

### Cross-System Integration Example

Combining multiple APIs for a complex feature:

```java
public class DrugBustHandler {

    public void onPlayerCaught(ServerPlayer player, String productionId) {
        ScheduleMCAPI api = ScheduleMCAPI.getInstance();

        // Add wanted level based on production
        IPoliceAPI police = api.getPoliceAPI();
        IProductionAPI production = api.getProductionAPI();

        ProductionConfig config = production.getProduction(productionId);
        if (config != null) {
            int stars = switch (config.getCategory()) {
                case PLANT -> 1;
                case MUSHROOM -> 2;
                case CHEMICAL -> 3;
                default -> 1;
            };
            police.addWantedLevel(player.getUUID(), stars);
        }

        // Fine the player
        IEconomyAPI economy = api.getEconomyAPI();
        double fine = 500.0 * police.getWantedLevel(player.getUUID());
        economy.withdraw(player.getUUID(), fine, "Police fine: drug possession");

        // Track crime achievement
        IAchievementAPI achievements = api.getAchievementAPI();
        achievements.addProgress(player.getUUID(), "CRIME_SPREE", 1.0);
    }
}
```

---

## Version Compatibility

### API Version History

| API Version | Mod Version    | Minecraft | Forge  | Changes                                    |
|------------|----------------|-----------|--------|--------------------------------------------|
| 3.0.0      | 3.0.0          | 1.20.1    | 47.4.0 | Initial public API release. All 11 modules |
| 3.1.0      | 3.1.0 - 3.5.x | 1.20.1    | 47.4.0 | Javadoc improvements, minor fixes          |
| 3.1.0      | 3.6.0-beta     | 1.20.1    | 47.4.0 | Current release                            |

### Supported Environments

| Requirement      | Version              |
|-----------------|----------------------|
| Java             | 17+                  |
| Minecraft        | 1.20.1               |
| Minecraft Forge  | 47.4.0+              |
| ScheduleMC       | 3.0.0+               |

### Stub Methods

The following methods are implemented as stubs in the current version. They
perform full input validation but return default values. They will be fully
implemented in future releases.

| API            | Method                | Stub Behavior                  |
|---------------|------------------------|--------------------------------|
| IProductionAPI | `startProduction()`    | Returns `false`                |
| IProductionAPI | `stopProduction()`     | Returns `false`                |
| IProductionAPI | `getProductionProgress()` | Returns `-1.0`              |
| IVehicleAPI    | `spawnVehicle()`       | Returns `null`                 |
| IWarehouseAPI  | All methods            | Return defaults (`false`, `0`, empty lists) |
| IMarketAPI     | `getDemandLevel()`     | Returns `0`                    |
| IMarketAPI     | `getSupplyLevel()`     | Returns `0`                    |
| IMarketAPI     | `getAllPrices()`       | Returns empty map              |
| IMarketAPI     | `setBasePrice()`       | No-op                          |

### Package Structure

```
de.rolandsw.schedulemc.api
  +-- ScheduleMCAPI.java              (Central entry point)
  +-- PlotModAPI.java                 (Legacy static API)
  +-- economy/
  |     +-- IEconomyAPI.java          (Economy interface)
  +-- plot/
  |     +-- IPlotAPI.java             (Plot interface)
  +-- npc/
  |     +-- INPCAPI.java              (NPC interface)
  +-- police/
  |     +-- IPoliceAPI.java           (Police interface)
  +-- production/
  |     +-- IProductionAPI.java       (Production interface)
  +-- vehicle/
  |     +-- IVehicleAPI.java          (Vehicle interface)
  +-- warehouse/
  |     +-- IWarehouseAPI.java        (Warehouse interface)
  +-- messaging/
  |     +-- IMessagingAPI.java        (Messaging interface)
  +-- smartphone/
  |     +-- ISmartphoneAPI.java       (Smartphone interface)
  +-- achievement/
  |     +-- IAchievementAPI.java      (Achievement interface)
  +-- market/
  |     +-- IMarketAPI.java           (Market interface)
  +-- impl/
        +-- EconomyAPIImpl.java
        +-- PlotAPIImpl.java
        +-- NPCAPIImpl.java
        +-- PoliceAPIImpl.java
        +-- ProductionAPIImpl.java
        +-- VehicleAPIImpl.java
        +-- WarehouseAPIImpl.java
        +-- MessagingAPIImpl.java
        +-- SmartphoneAPIImpl.java
        +-- AchievementAPIImpl.java
        +-- MarketAPIImpl.java
```

### Key Domain Types

| Type                           | Package                                          | Description                        |
|-------------------------------|---------------------------------------------------|------------------------------------|
| `PlotRegion`                   | `de.rolandsw.schedulemc.region`                  | Plot region with bounds and owner  |
| `PlotType`                     | `de.rolandsw.schedulemc.region`                  | Enum: RESIDENTIAL, COMMERCIAL, SHOP, PUBLIC, GOVERNMENT, PRISON, TOWING_YARD |
| `CustomNPCEntity`              | `de.rolandsw.schedulemc.npc.entity`              | NPC entity class                   |
| `NPCData`                      | `de.rolandsw.schedulemc.npc.data`                | NPC configuration data             |
| `NPCType`                      | `de.rolandsw.schedulemc.npc.data`                | Enum: BEWOHNER, VERKAEUFER, POLIZEI, BANK, ABSCHLEPPER, CITIZEN, MERCHANT, POLICE, BANKER, DRUG_DEALER |
| `ProductionConfig`             | `de.rolandsw.schedulemc.production.config`       | Production definition (Builder)    |
| `ProductionConfig.ProductionCategory` | `de.rolandsw.schedulemc.production.config` | Enum: PLANT, MUSHROOM, CHEMICAL, EXTRACT, PROCESSED |
| `EntityGenericVehicle`         | `de.rolandsw.schedulemc.vehicle.entity.vehicle.base` | Base vehicle entity          |
| `WarehouseSlot`                | `de.rolandsw.schedulemc.warehouse`               | Warehouse inventory slot           |
| `Achievement`                  | `de.rolandsw.schedulemc.achievement`             | Achievement definition             |
| `AchievementCategory`          | `de.rolandsw.schedulemc.achievement`             | Enum: ECONOMY, CRIME, PRODUCTION, SOCIAL, EXPLORATION |
| `PlayerAchievements`           | `de.rolandsw.schedulemc.achievement`             | Per-player achievement data        |

---

*ScheduleMC API Reference -- Generated for version 3.6.0-beta*
*Author: Luckas R. Schneider (Minecraft425HD)*
*License: All Rights Reserved*
