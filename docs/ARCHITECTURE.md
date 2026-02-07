# ScheduleMC Architecture Documentation

> Minecraft 1.20.1 Forge mod implementing a complete roleplay/economy server ecosystem.
> **219,500+ LOC** across **1,407 Java files** organized into **35+ modules**.

---

## Table of Contents

1. [System Architecture Overview](#1-system-architecture-overview)
2. [Module Architecture](#2-module-architecture)
3. [Data Persistence Architecture](#3-data-persistence-architecture)
4. [Networking Architecture](#4-networking-architecture)
5. [Performance Architecture](#5-performance-architecture)
6. [Design Patterns](#6-design-patterns)
7. [API Architecture](#7-api-architecture)
8. [Security and Validation](#8-security-and-validation)
9. [CI/CD Pipeline](#9-cicd-pipeline)
10. [Dependency Graph](#10-dependency-graph)

---

## 1. System Architecture Overview

### Entry Point

The mod is bootstrapped through the Forge `@Mod` annotation on the main class:

```
Package:    de.rolandsw.schedulemc
Main class: ScheduleMC.java
Mod ID:     "schedulemc"
```

```java
@Mod(ScheduleMC.MOD_ID)
public class ScheduleMC {
    public static final String MOD_ID = "schedulemc";
    // ...
}
```

### Forge Event Bus Integration

ScheduleMC uses both Forge event buses:

| Bus | Purpose | Registration Method |
|-----|---------|-------------------|
| **Mod Event Bus** (`IEventBus`) | Lifecycle events, deferred registration | `modEventBus.addListener(...)` |
| **Forge Event Bus** (`MinecraftForge.EVENT_BUS`) | Runtime gameplay events | `MinecraftForge.EVENT_BUS.register(...)` |

**Mod Event Bus listeners** registered in the constructor:

- `FMLCommonSetupEvent` -- network handler registration, utility system initialization
- `FMLClientSetupEvent` -- client-side packet registration (via `DistExecutor`)
- `EntityAttributeCreationEvent` -- NPC entity attribute assignment

**Forge Event Bus handlers** registered in the constructor (26 handlers):

- `BlockProtectionHandler` -- plot-based block protection
- `PlayerJoinHandler` / `PlayerDisconnectHandler` -- session lifecycle
- `TobaccoBottleHandler`, `CashSlotRestrictionHandler`, `InventoryRestrictionHandler` -- item restrictions
- `NPCStealingHandler`, `EntityRemoverHandler`, `NPCKnockoutHandler`, `PoliceAIHandler`, `PoliceDoorBlockHandler` -- NPC behaviors
- `NPCNameSyncHandler`, `RespawnHandler`, `BusinessMetricsUpdateHandler` -- static class handlers
- `WarehouseManager`, `UtilityEventHandler` -- system event handlers
- `DoorLockHandler` -- lock system events
- The main `ScheduleMC` class itself (`this`) -- commands, server lifecycle, tick events

### Deferred Registration Pattern

All items, blocks, block entities, entities, menus, and creative tabs use Forge's `DeferredRegister` pattern. Each module exposes static `DeferredRegister` fields that are registered against the mod event bus in the `ScheduleMC` constructor:

```java
// Pattern used by every module:
TobaccoItems.ITEMS.register(modEventBus);
TobaccoBlocks.BLOCKS.register(modEventBus);
TobaccoBlocks.ITEMS.register(modEventBus);       // Block items
TobaccoBlockEntities.BLOCK_ENTITIES.register(modEventBus);
```

**Registration order in the constructor** (14 production systems + core systems):

1. Core: `ModItems`, `ModCreativeTabs`
2. Tobacco (items, blocks, block items, block entities)
3. Coca, Poppy, Mushroom, Meth, LSD, MDMA, Cannabis, Coffee, Wine, Cheese, Honey, Chocolate, Beer (each: items, blocks, block items, block entities, menus)
4. Economy (blocks, block items, block entities, menus)
5. Region/Plot (blocks, block items)
6. Warehouse (blocks, block items, block entities, menus)
7. Tobacco menus, entities
8. NPC (items, entities, menus)
9. Lock (items)
10. Towing (menus)
11. Vehicle (initialized via `new Main(modEventBus)` which handles its own registration)

### Server Lifecycle

```
Constructor
    |
    v
FMLCommonSetupEvent -----> Network handler registration (20+ handlers)
    |                      Utility system initialization
    v
ServerStartedEvent ------> Parallel data loading (16 managers via CompletableFuture)
    |                      Sequential initialization (economy, NPC, gang, lock, etc.)
    |                      IncrementalSaveManager setup (40+ registered saveables)
    |                      ScheduleMCAPI initialization (11 subsystems)
    |                      Health check
    v
ServerTickEvent ---------> Economy ticks (every tick: interest, loans, taxes, etc.)
    |                      UDPS periodic update (every 60s)
    |                      Gang sync broadcast (every 60s)
    |                      Lock code rotation (every 5 min)
    |                      Rent expiration check (every 5 min)
    |                      Police search cleanup (every 5 min)
    v
ServerStoppingEvent -----> UDPS save
                           Gang system save + singleton reset
                           Lock system save + singleton reset
                           IncrementalSaveManager final save + stop
                           ThreadPoolManager shutdown
```

### Startup Parallelization

On `ServerStartedEvent`, 16 independent managers are loaded in parallel using `CompletableFuture.runAsync()` on the IO thread pool:

```
PlotManager, EconomyManager, DailyRewardManager, WalletManager,
CrimeManager, NPCNameRegistry, MessageManager, PlayerTracker,
PlayerSettingsManager, MembershipManager, TowingYardManager,
StateAccount, VehicleSpawnRegistry, FuelStationRegistry,
FuelBillManager, PlotUtilityManager
```

Each load is wrapped with `exceptionally()` to prevent a single failure from blocking the entire startup.

---

## 2. Module Architecture

### 2.1 api/ -- Public API (24 files)

The external-facing API for other mods to interact with ScheduleMC.

**Structure:**

```
api/
  ScheduleMCAPI.java              -- Singleton entry point (v3.0.0)
  PlotModAPI.java                 -- Legacy facade (Economy, Plots, Daily, Util inner classes)
  economy/IEconomyAPI.java        -- Economy interface
  plot/IPlotAPI.java              -- Plot management interface
  production/IProductionAPI.java  -- Production system interface
  npc/INPCAPI.java                -- NPC management interface
  police/IPoliceAPI.java          -- Crime/wanted system interface
  warehouse/IWarehouseAPI.java    -- Warehouse interface
  messaging/IMessagingAPI.java    -- Message system interface
  smartphone/ISmartphoneAPI.java  -- Smartphone apps interface
  vehicle/IVehicleAPI.java        -- Vehicle system interface
  achievement/IAchievementAPI.java -- Achievement interface
  market/IMarketAPI.java          -- Dynamic market interface
  impl/
    EconomyAPIImpl.java           -- 11 implementation classes
    PlotAPIImpl.java
    ProductionAPIImpl.java
    NPCAPIImpl.java
    PoliceAPIImpl.java
    WarehouseAPIImpl.java
    MessagingAPIImpl.java
    SmartphoneAPIImpl.java
    VehicleAPIImpl.java
    AchievementAPIImpl.java
    MarketAPIImpl.java
```

**Initialization:** All 11 interface-implementation pairs are injected during `ServerStartedEvent`:

```java
ScheduleMCAPI.getInstance().initialize(
    new EconomyAPIImpl(),      new PlotAPIImpl(),
    new ProductionAPIImpl(),   new NPCAPIImpl(),
    new PoliceAPIImpl(),       new WarehouseAPIImpl(),
    new MessagingAPIImpl(),    new SmartphoneAPIImpl(),
    new VehicleAPIImpl(),      new AchievementAPIImpl(),
    new MarketAPIImpl()
);
```

### 2.2 economy/ -- Economy System (64 files)

The most complex manager system, implementing a full banking/financial ecosystem.

**Core Managers (11):**

| Manager | Responsibility |
|---------|---------------|
| `EconomyManager` | Bank accounts, balance, deposit/withdraw/transfer |
| `WalletManager` | Physical cash items in player inventory |
| `TransactionHistory` | Auditable transaction log |
| `InterestManager` | Automated interest calculations on day boundaries |
| `LoanManager` | Player loan lifecycle |
| `CreditScoreManager` | Credit scoring per player |
| `CreditLoanManager` | NPC credit advisor loans with credit score integration |
| `TaxManager` | Automated tax collection |
| `SavingsAccountManager` | High-interest savings accounts |
| `OverdraftManager` | Account overdraft protection |
| `RecurringPaymentManager` | Automated recurring payments |

**Additional Economy Components:**

- `ShopAccountManager` -- NPC shop revenue management
- `StateAccount` -- Government/state treasury
- `EconomyController` -- Unified Dynamic Pricing System (UDPS) controller
- `UnifiedPriceCalculator`, `PriceManager`, `PriceBounds` -- dynamic pricing
- `EconomyCycle`, `EconomyCyclePhase`, `EconomicEvent` -- economy simulation
- `GlobalEconomyTracker`, `DailyRevenueRecord` -- economic tracking
- `BatchTransactionManager` -- bulk transaction optimization
- `AntiExploitManager` -- exploit prevention (daily volume limits, mass-sell detection)
- `MemoryCleanupManager` -- offline player data cleanup
- `RateLimiter` -- request throttling
- `FeeManager` -- transaction fee calculation
- `WarehouseMarketBridge`, `WarehouseStockLevel` -- warehouse-economy integration

**Sub-packages:**
- `economy/blocks/` -- ATMBlock, CashBlock, EconomyBlocks
- `economy/blockentity/` -- ATMBlockEntity, CashBlockEntity
- `economy/commands/` -- HospitalCommand, StateCommand
- `economy/events/` -- CashSlotRestrictionHandler, CreditScoreEventHandler, RespawnHandler
- `economy/items/` -- CashItem
- `economy/menu/` -- ATMMenu, EconomyMenuTypes
- `economy/network/` -- EconomyNetworkHandler, ATM packets, bank data sync packets
- `economy/screen/` -- ATMScreen

**Tick-driven systems:** All time-based economy managers (Interest, Loan, Tax, Savings, Overdraft, RecurringPayment, CreditScore, CreditLoan) implement a `tick(dayTime)` method called every server tick to detect Minecraft day boundaries for daily processing.

### 2.3 region/ -- Plot/Land System (19 files)

Land ownership, protection, and spatial management.

**Core Classes:**

| Class | Purpose |
|-------|---------|
| `PlotManager` | Central plot CRUD, persistence, plot lookup |
| `PlotRegion` | Individual plot data (owner, bounds, trust list, rating, rent) |
| `PlotSpatialIndex` | Chunk-based spatial index for O(1) position-to-plot lookup |
| `PlotCache` | LRU cache (1000 entries) with chunk-indexed invalidation |
| `PlotChunkCache` | Chunk-level caching layer |
| `PlotProtectionHandler` | Block break/place protection enforcement |
| `PlotArea` | Area calculation utilities |
| `PlotType` | Plot type enumeration |

**Sub-packages:**
- `region/blocks/` -- PlotBlocks, PlotInfoBlock
- `region/network/` -- PlotNetworkHandler + 7 packet types (purchase, sale, rename, description, trust, rating, abandon)

### 2.4 npc/ -- NPC System (173 files)

The largest module, implementing autonomous NPCs with advanced AI, dialogue, quests, crime, and social systems.

**Top-level sub-packages:**

```
npc/
  entity/         -- CustomNPCEntity, NPCEntities, entity definitions
  goals/          -- 5 AI goal classes (base goals)
  life/
    behavior/     -- 5 behavior files (advanced behavioral AI)
    core/         -- Core life simulation
    companion/    -- CompanionManager, companion system
    dialogue/     -- DialogueManager, conversation trees
    economy/      -- DynamicPriceManager, NPC economy
    quest/        -- QuestManager, quest system
    social/       -- FactionManager, NPCInteractionManager
    witness/      -- WitnessManager, crime witness system
    world/        -- WorldEventManager, world events
  personality/    -- NPCRelationshipManager, personality traits
  pathfinding/    -- Custom NPC pathfinding
  crime/
    prison/       -- PrisonManager, prison system
      client/     -- Prison client screens
      network/    -- PrisonNetworkHandler
    CrimeManager, BountyManager, BountyCommand
  bank/           -- StockMarketData, TransferLimitTracker (NPC banker)
  client/         -- NPC client rendering
  commands/       -- NPCCommand, AdminToolsCommand
  data/           -- NPC data structures
  events/         -- NPCStealingHandler, EntityRemoverHandler, NPCKnockoutHandler,
                     PoliceAIHandler, PoliceDoorBlockHandler, NPCNameSyncHandler,
                     PoliceSearchBehavior
  items/          -- NPCItems (spawn eggs, tools)
  menu/           -- NPCMenuTypes
  network/        -- NPCNetworkHandler
```

**NPC Life System Managers (9 managers, all with JSON persistence):**

1. `FactionManager` -- NPC faction affiliations and relations
2. `WitnessManager` -- Crime witnesses tracking
3. `NPCRelationshipManager` -- NPC-to-player and NPC-to-NPC relationships
4. `CompanionManager` -- Companion/follower system
5. `QuestManager` -- Quest creation, tracking, completion
6. `DialogueManager` -- Dialogue trees and conversation state
7. `NPCInteractionManager` -- Interaction frequency and cooldown tracking
8. `WorldEventManager` -- World-scale events affecting NPCs
9. `DynamicPriceManager` -- NPC shop pricing influenced by supply/demand

### 2.5 vehicle/ -- Vehicle System (137 files)

A complete vehicle ecosystem integrated through a separate `Main` class that manages its own Forge event bus registration.

```
vehicle/
  Main.java              -- Vehicle mod bootstrap (registers own events)
  entity/                -- Vehicle entity classes
  vehicle/               -- VehicleSpawnRegistry, vehicle definitions
  blocks/                -- Vehicle-related blocks
  events/                -- Vehicle event handlers
  fluids/                -- Fluid system for fuel
  fuel/                  -- FuelStationRegistry, FuelBillManager
  gui/                   -- Vehicle GUI screens
  items/                 -- VehicleSpawnTool, vehicle items
  mixins/                -- Mixin support
  net/                   -- Vehicle networking
  sounds/                -- Vehicle sound system
  util/                  -- Vehicle utilities
```

**Dependencies:** CoreLib (`de.maxhenkel.corelib:corelib:1.20.1-1.1.1`) for networking, OBJ models, and GUI system.

### 2.6 production/ -- Generic Production Framework (29 files)

A reusable framework that all 14 production modules build upon.

```
production/
  ProductionSize.java
  blockentity/
    AbstractProcessingBlockEntity.java  -- Template method for processing
    PlantPotBlockEntity.java            -- Plant pot logic
    UnifiedProcessingBlockEntity.java   -- Unified processing system
  blocks/
    AbstractPlantBlock.java             -- Base plant growth block
    AbstractProcessingBlock.java        -- Base processing block
    PlantPotBlock.java                  -- Plant pot block
  config/
    ProductionConfig.java               -- Builder-pattern configuration
    ProductionRegistry.java             -- Central production type registry
  core/
    DrugType.java, GenericPlantData.java, GenericQuality.java,
    PotType.java, ProductionQuality.java, ProductionStage.java, ProductionType.java
  data/
    PlantPotData.java
  growth/
    AbstractPlantGrowthHandler.java     -- Template for growth logic
    PlantGrowthHandler.java             -- Growth handler interface
    PlantGrowthHandlerFactory.java      -- Factory for growth handlers
    TobaccoGrowthHandler, CannabisGrowthHandler, CocaGrowthHandler,
    PoppyGrowthHandler, MushroomGrowthHandler
  items/
    PackagedDrugItem.java
  nbt/
    PlantSerializer.java                -- NBT serialization interface
    PlantSerializerFactory.java         -- Factory for serializers
    TobaccoPlantSerializer.java
```

**ProductionConfig.Builder** enables declarative production type definition:

```java
new ProductionConfig.Builder("tobacco_virginia", "Virginia Tobacco")
    .category(ProductionCategory.PLANT)
    .basePrice(15.0)
    .growthTicks(4800)
    .baseYield(4)
    .requiresLight(true)
    .minLightLevel(10)
    .addProcessingStage("drying", new ProcessingStageConfig(...))
    .qualityTiers(GenericQuality.createStandard4TierSystem())
    .build();
```

### 2.7 Individual Production Modules (14 modules, 607 files total)

Each production module follows the same internal structure pattern:

```
<module>/
  items/<Module>Items.java     -- DeferredRegister<Item>
  blocks/<Module>Blocks.java   -- DeferredRegister<Block>
  blockentity/<Module>BlockEntities.java  -- DeferredRegister<BlockEntityType>
  menu/<Module>MenuTypes.java  -- DeferredRegister<MenuType>
  network/<Module>Networking.java  -- Network packet registration
  screen/                      -- Client GUI screens
  ...                          -- Module-specific items, blocks, recipes
```

| Module | Files | Category | Key Content |
|--------|-------|----------|-------------|
| `tobacco/` | 82 | Plant | Virginia, Burley varieties; drying, curing, rolling |
| `cannabis/` | 28 | Plant | Growth strains; drying, processing |
| `coca/` | 29 | Plant/Extract | Coca leaf cultivation; cocaine extraction |
| `poppy/` | 20 | Plant/Extract | Poppy cultivation; opium/heroin extraction |
| `meth/` | 23 | Chemical | Chemical synthesis process chain |
| `lsd/` | 22 | Chemical | Synthesis, blotter creation |
| `mdma/` | 21 | Chemical | Synthesis, pressing |
| `mushroom/` | 15 | Mushroom | Spore cultivation, special growth |
| `coffee/` | 60 | Plant | Bean cultivation, roasting, brewing |
| `wine/` | 63 | Plant | Grape cultivation, fermentation, aging |
| `cheese/` | 53 | Processed | Milk processing, aging caves |
| `honey/` | 60 | Processed | Beekeeping, honey extraction |
| `chocolate/` | 69 | Processed | Cacao cultivation, tempering, molding |
| `beer/` | 62 | Processed | Brewing, fermentation, kegging |

### 2.8 warehouse/ -- Warehouse System (20 files)

Centralized storage and delivery management.

```
warehouse/
  WarehouseManager.java         -- Central warehouse CRUD + persistence
  WarehouseBlocks.java          -- Block registration
  WarehouseSlot.java            -- Individual storage slot
  commands/WarehouseCommand.java
  menu/WarehouseMenuTypes.java
  network/WarehouseNetworkHandler.java
  ...
```

**Dependencies:** Economy system (delivery fees), Region system (plot-based access).

### 2.9 commands/ -- Command System (5 core files + distributed commands)

```
commands/
  CommandExecutor.java     -- Utility for consistent command execution + error handling
  AdminCommand.java        -- Admin-only server management commands
  HealthCommand.java       -- Server health diagnostics
  MoneyCommand.java        -- Economy commands (/money, /pay, /balance)
  PlotCommand.java         -- Plot management commands (34+ subcommands)
```

**CommandExecutor** eliminates boilerplate across 139 command methods:

```java
// Before: 8 lines per command method
// After: 5 lines per command method via CommandExecutor.executePlayerCommand()
return CommandExecutor.executePlayerCommand(ctx, "Error message",
    player -> {
        // Command logic
    });
```

**Additional commands distributed across modules:**
- `economy/commands/` -- HospitalCommand, StateCommand
- `npc/commands/` -- NPCCommand, AdminToolsCommand
- `warehouse/commands/` -- WarehouseCommand
- `utility/commands/` -- UtilityCommand
- `npc/crime/` -- BountyCommand
- `npc/crime/prison/` -- PrisonCommand
- `territory/` -- MapCommand
- `market/` -- MarketCommand
- `gang/` -- GangCommand
- `lock/` -- LockCommand

### 2.10 client/ -- Client-Side Systems (38 files)

**Smartphone System:**

```
client/
  SmartphoneKeyHandler.java        -- Keybind to open smartphone
  SmartphonePlayerHandler.java     -- Player smartphone state
  SmartphoneProtectionHandler.java -- Prevent theft
  SmartphoneTracker.java           -- Track smartphone ownership
  screen/SmartphoneScreen.java     -- Main smartphone UI
  screen/apps/
    BankAppScreen.java             -- Mobile banking
    MessagesAppScreen.java         -- SMS messaging
    ContactsAppScreen.java         -- Contact list
    ContactDetailScreen.java       -- Contact details
    PlotAppScreen.java             -- Plot management
    OrderAppScreen.java            -- Order placement
    DealerAppScreen.java           -- Dealer interface
    ProductsAppScreen.java         -- Product catalog
    ProducerLevelAppScreen.java    -- Producer level/XP
    AchievementAppScreen.java      -- Achievement tracking
    CrimeStatsAppScreen.java       -- Crime statistics
    GangAppScreen.java             -- Gang management
    TowingServiceAppScreen.java    -- Towing service
    SettingsAppScreen.java         -- Phone settings
    ScenarioEditorScreen.java      -- Gang scenario editor
    ChatScreen.java                -- Chat interface
  network/SmartphoneNetworkHandler.java
```

**HUD Overlays:**
- `PlotInfoHudOverlay` -- Shows current plot info
- `TobaccoPotHudOverlay` -- Shows plant pot status
- `WantedLevelOverlay` -- Shows wanted level (GTA-style)

**Other Client Systems:**
- `KeyBindings` -- Custom keybind registration
- `ClientModEvents` -- Client-side event registration
- `QualityItemColors` -- Color-coded quality tints on items
- `UpdateNotificationHandler` -- Mod update notifications
- `InventoryBlockHandler` -- Client inventory management

### 2.11 mapview/ -- Map Rendering Engine (122 files)

A comprehensive in-game map system.

```
mapview/
  config/          -- Map configuration
  core/            -- Core rendering pipeline
  data/            -- Map data structures
  entityrender/    -- Entity rendering on map
  integration/
    forge/ForgeEvents.java  -- Forge event integration + network packets
  navigation/      -- Waypoint/navigation system
  npc/             -- NPC markers on map
  presentation/    -- UI presentation layer
  service/         -- Map data services
  textures/        -- Texture management
  util/            -- Rendering utilities
```

### 2.12 Supplementary Modules

| Module | Files | Purpose |
|--------|-------|---------|
| `messaging/` | 9 | Player-to-player message system with persistence |
| `achievement/` | 11 | Achievement tracking, unlock conditions, network sync |
| `level/` | 12 | Producer level/XP system with unlockables |
| `lock/` | 17 | Door locks (key, code, lockpick, hacking tool, omnihack) |
| `gang/` | 32 | Gang creation, membership, weekly fees, missions, scenarios |
| `territory/` | 11 | Territory control, territory map |
| `towing/` | 15 | Towing service, yard management, membership |
| `utility/` | 8 | Plot utilities (electricity, water, etc.) |
| `market/` | 3 | DynamicMarketManager, MarketCommand |
| `managers/` | N/A | DailyRewardManager, NPCNameRegistry, RentManager |
| `events/` | N/A | BlockProtectionHandler, InventoryRestrictionHandler, PlayerDisconnectHandler |
| `player/` | N/A | PlayerTracker, PlayerSettingsManager, network handlers |
| `config/` | N/A | ModConfigHandler (COMMON + CLIENT specs), DeliveryPriceConfig |
| `data/` | N/A | Data generation |
| `gui/` | N/A | Shared GUI components |
| `items/` | N/A | ModItems, PlotSelectionTool |
| `network/` | N/A | AbstractPacket base class |
| `util/` | 19 | Cross-cutting infrastructure (see Section 5) |

---

## 3. Data Persistence Architecture

### Storage Location

All persistent data is stored as JSON files in `config/schedulemc/` using Google Gson for serialization/deserialization.

### AbstractPersistenceManager<T>

The template method base class that all persistence-enabled managers extend. Eliminates approximately 165 lines of duplicated code per manager.

```
AbstractPersistenceManager<T> implements ISaveable
  |
  |-- Abstract methods (subclass must implement):
  |     getDataType()           -- Gson Type for deserialization
  |     onDataLoaded(T data)    -- Called after successful load
  |     getCurrentData()        -- Returns current data for saving
  |     getComponentName()      -- Logging name
  |     getHealthDetails()      -- Health monitoring info
  |     onCriticalLoadFailure() -- Fallback on unrecoverable error
  |
  |-- Provided functionality:
        load()          -- Load with automatic backup recovery
        save()          -- Atomic write with backup creation
        saveIfNeeded()  -- Only save if dirty flag set
        markDirty()     -- Set dirty flag
        isHealthy()     -- Health status
        getHealthInfo() -- Detailed health info for monitoring
```

**Load flow with recovery:**

```
load()
  |
  |--> File exists? --NO--> onNoDataFileFound() (start fresh)
  |
  |--> Parse JSON via Gson
  |      |
  |      |--> Success --> onDataLoaded(data), healthy=true
  |      |
  |      |--> Failure --> BackupManager.restoreFromBackup()
  |              |
  |              |--> Backup exists --> Try loading backup
  |              |       |
  |              |       |--> Success --> onDataLoaded(data), "Recovered from backup"
  |              |       |
  |              |       |--> Failure --> onCriticalLoadFailure() (empty data fallback)
  |              |
  |              |--> No backup --> onCriticalLoadFailure() (empty data fallback)
  |                                 Save corrupt file as .CORRUPT_<timestamp>
```

**Save flow with atomic writes:**

```
save()
  |
  |--> Create parent directories
  |--> BackupManager.createBackup(dataFile) if file exists and non-empty
  |--> Write to temporary file (.tmp extension)
  |--> Atomic move: .tmp -> target (REPLACE_EXISTING + ATOMIC_MOVE)
  |--> needsSave = false, healthy = true
```

### IncrementalSaveManager

Replaces the old "save everything at once" approach. Instead of causing 100-500ms freezes every 5 minutes, it distributes saves across time.

**Configuration:**
- Save interval: 20 ticks (1 second)
- Batch size: 5 components per tick
- Background thread via `ThreadPoolManager.getScheduledPool()`

**ISaveable interface:**

```java
public interface ISaveable {
    boolean isDirty();      // Has data changed?
    void save();            // Persist data
    String getName();       // Component name for logging
    int getPriority();      // 0=highest, 10=lowest (default: 5)
}
```

**Registered saveables (40+ managers) with priority ordering:**

| Priority | Managers |
|----------|----------|
| 0-2 | EconomyManager, PlotManager, BountyManager, TerritoryManager |
| 3 | DynamicMarketManager, TransactionHistory, InterestManager, LoanManager, TaxManager, SavingsAccountManager, OverdraftManager, RecurringPaymentManager, CreditScoreManager, CreditLoanManager, GangManager |
| 4 | PlayerTracker, PlayerSettingsManager, WalletManager, DailyRewardManager, GangMissionManager |
| 5 | MessageManager, FactionManager, WitnessManager, NPCRelationshipManager, CompanionManager, QuestManager, DialogueManager, NPCInteractionManager, WorldEventManager, DynamicPriceManager, NPCNameRegistry, LockManager, FuelBillManager, VehicleSpawnRegistry, FuelStationRegistry |
| 6 | MembershipManager, TowingYardManager, WarehouseManager, PlotUtilityManager |
| 7 | StateAccount |

**Incremental save tick logic:**

```
incrementalSaveTick() {
    for each saveable (in priority order):
        if saved >= batchSize: break
        if saveable.isDirty():
            saveSingleComponent(saveable)
            saved++
}
```

### BackupManager

Centralized backup system used by all persistence managers.

- **Rotation:** Maximum 5 timestamped backups per file
- **Naming:** `<filename>.backup_yyyy-MM-dd_HH-mm-ss`
- **Cleanup:** Automatically removes oldest backups beyond limit
- **Recovery:** `restoreFromBackup()` finds the most recent backup by modification date

### VersionedData

Enables data format migration between mod versions.

```java
// Saving: wrap data with version
JsonObject wrapper = VersionedData.wrap(dataJson, 2);
// Result: { "dataVersion": 2, "data": { ... } }

// Loading: unwrap with automatic migration
VersionedData.Result result = VersionedData.unwrap(reader, 2, migrator);
```

**Migration flow:**
- Version matches current: return data as-is
- Version older: invoke migrator function with `MigrationContext(data, fromVersion, toVersion)`
- No version field: treat as legacy (version 0), apply migrator if available
- Version newer than current: return error (downgrade not supported)

---

## 4. Networking Architecture

### Network Handler Registration

All network handlers are registered during `FMLCommonSetupEvent.enqueueWork()`:

```java
EconomyNetworkHandler.register();
NPCNetworkHandler.register();
ModNetworking.register();           // Tobacco
CoffeeNetworking.register();
WineNetworking.register();
CheeseNetworking.register();
HoneyNetworking.register();
ChocolateNetworking.register();
BeerNetworking.register();
SmartphoneNetworkHandler.register();
MessageNetworkHandler.register();
WarehouseNetworkHandler.register();
AchievementNetworkHandler.register();
ProducerLevelNetworkHandler.register();
PlotNetworkHandler.register();
PrisonNetworkHandler.register();
PlayerSettingsNetworkHandler.register();
TowingNetworkHandler.register();
TerritoryNetworkHandler.register();
GangNetworkHandler.register();
LockNetworkHandler.register();
ForgeEvents.registerNetworkPackets(); // MapView
```

Client-only packets (referencing `Screen` classes) are registered separately during `FMLClientSetupEvent`:

```java
TerritoryNetworkHandler.registerClientPackets();
PrisonNetworkHandler.registerClientPackets();
```

### PacketHandler Utility

Central utility class that reduces boilerplate in packet `handle()` methods:

```java
// Server-side packet with automatic player check + error handling
PacketHandler.handleServerPacket(ctx, player -> {
    // Handle packet logic
});

// Admin packet with permission check
PacketHandler.handleAdminPacket(ctx, 2, player -> {
    // Requires permission level 2+
});

// Client-side packet
PacketHandler.handleClientPacket(ctx, () -> {
    // Client-only logic
});
```

All handlers wrap execution in `ctx.get().enqueueWork()` for thread safety and call `ctx.get().setPacketHandled(true)`.

### Network Packet Types (100)

**Economy packets:**
- `ATMTransactionPacket` -- ATM deposit/withdraw
- `RequestATMDataPacket` / `SyncATMDataPacket` -- ATM screen sync
- `RequestBankDataPacket` / `SyncBankDataPacket` / `SyncFullBankDataPacket` -- Banking sync

**Plot packets:**
- `PlotPurchasePacket`, `PlotSalePacket`, `PlotAbandonPacket`
- `PlotRenamePacket`, `PlotDescriptionPacket`
- `PlotTrustPacket`, `PlotRatingPacket`

**NPC packets:**
- Various NPC sync, interaction, and state packets via `NPCNetworkHandler`

**Level/Achievement packets:**
- `LevelUpNotificationPacket`, `SyncProducerLevelDataPacket`, `RequestProducerLevelDataPacket`
- Achievement sync packets via `AchievementNetworkHandler`

**Smartphone packets:**
- `SmartphoneStatePacket` -- Phone open/close state sync

**Production-specific packets:**
- Each production module (coffee, wine, cheese, honey, chocolate, beer) has its own networking class

**System packets:**
- Prison, territory, gang, lock, towing, player settings, message packets

### Client-Server Synchronization Pattern

The standard pattern for data synchronization:

```
Client                          Server
  |                               |
  |--- RequestDataPacket -------->|
  |                               |-- Load data from manager
  |<---- SyncDataPacket ----------|
  |                               |
  |-- Process/display data        |
  |                               |
  |--- ActionPacket ------------->|
  |                               |-- Validate + execute
  |                               |-- markDirty()
  |<---- SyncDataPacket ----------|  (updated data)
```

---

## 5. Performance Architecture

### PlotSpatialIndex -- O(1) Spatial Queries

Uses a chunk-based grid (16x16x16) to index plots for fast position-to-plot lookup.

**Data structures:**
- `Map<ChunkKey, Set<String>> chunkToPlots` -- Which plots overlap a chunk
- `Map<String, Set<ChunkKey>> plotToChunks` -- Which chunks a plot spans

**Lookup:** `getPlotsNear(BlockPos)` computes the chunk key via `Math.floorDiv(coord, 16)` and returns the plot set for that chunk in O(1).

**Thread safety:** Both maps use `ConcurrentHashMap`. The `ChunkKey` record caches its hash code.

### PlotCache -- LRU Cache with O(1) Lookups

`LinkedHashMap` in access-order mode (LRU) with synchronized wrapper.

- **Default capacity:** 1000 entries
- **Eviction:** Automatic LRU eviction via `removeEldestEntry()`
- **Chunk-indexed invalidation:** Secondary `Map<ChunkPos, Set<BlockPos>> chunkIndex` enables O(affected_chunks) invalidation instead of O(cache_size)
- **Validation on hit:** Each cache hit re-verifies `plot.contains(pos)` to detect stale entries
- **Statistics:** Thread-safe AtomicLong counters for hits, misses, invalidations, hit rate

### PlotChunkCache -- Chunk-Level Caching

Additional caching layer that operates at the chunk granularity, complementing the position-level PlotCache.

### ThreadPoolManager -- Centralized Thread Pools

Replaces 47 ad-hoc thread creations across the codebase with 5 managed pools:

| Pool | Size | Thread Priority | Purpose |
|------|------|----------------|---------|
| **IO** | 4 fixed | NORM | File I/O (save/load) |
| **Render** | 2 fixed | NORM-1 | MapView rendering |
| **Computation** | cores/2 (min 2) | NORM | CPU-intensive tasks |
| **Async** | 0-20 cached | NORM | Short async tasks (auto-shrink at 60s idle) |
| **Scheduled** | 2 fixed | NORM | Periodic tasks (IncrementalSaveManager, etc.) |

**Performance impact:**
- RAM: -100MB at 1000 players
- Thread count: 200+ reduced to ~50 (-75%)
- All threads are daemon threads with uncaught exception handlers
- Double-checked locking with volatile fields for lazy initialization
- Graceful shutdown with 10s timeout then force shutdown

### TickThrottler -- TPS Protection

Simple counter-based throttle that reduces tick processing frequency:

```java
private final TickThrottler throttler = new TickThrottler(20); // 1x/second

public void tick() {
    if (!throttler.shouldTick()) return;  // Skip 19 out of 20 ticks
    // Expensive logic runs only 1x/second
}
```

**Performance impact:** PlantPotBlockEntity calls reduced from 200,000/sec to 10,000/sec (-95%).

### RateLimiter -- Request Throttling

Located in `de.rolandsw.schedulemc.economy.RateLimiter` (economy-specific) and `de.rolandsw.schedulemc.util.RateLimiter` (general-purpose).

Prevents abuse by limiting how frequently players can perform certain actions.

### MemoryCleanupManager -- Periodic Memory Cleanup

Tracks players who disconnect and clears their in-memory economy data after a 5-minute delay (allowing reconnect):

```
Player disconnects --> markPlayerOffline(uuid)
  |
  |--> 5 minutes later (checked every 60 seconds):
  |     Player still offline? --> Clean up RAM cache
  |     Player reconnected? --> Cancel cleanup
```

### BatchTransactionManager -- Batch Economy Operations

Fluent API for batching multiple economy transactions:

```java
BatchTransactionManager.create()
    .deposit(uuid1, 100.0)
    .deposit(uuid2, 50.0)
    .withdraw(uuid3, 75.0)
    .transfer(uuid4, uuid5, 200.0)
    .execute();
// markDirty() called once instead of per-transaction
```

**Performance gain:** 66-90% improvement for bulk transactions.

---

## 6. Design Patterns

### Singleton

Used extensively for manager classes. Most use double-checked locking with `volatile`:

```java
private static volatile AntiExploitManager instance;

public static AntiExploitManager getInstance() {
    AntiExploitManager localRef = instance;
    if (localRef == null) {
        synchronized (AntiExploitManager.class) {
            localRef = instance;
            if (localRef == null) {
                instance = localRef = new AntiExploitManager();
            }
        }
    }
    return localRef;
}
```

**Singleton managers:** ScheduleMCAPI, EconomyManager, PlotManager, AntiExploitManager, WarehouseManager, AchievementManager, GangManager, TerritoryManager, BountyManager, LockManager, DynamicMarketManager, all 9 NPC Life System managers.

### Factory

- **`PlantSerializerFactory`** -- Creates plant-type-specific NBT serializers
- **`PlantGrowthHandlerFactory`** -- Creates growth handlers per plant species
- **`BatchTransactionManager.create()`** -- Static factory for batch builders
- **Entity factories** -- NPC entity creation through Forge entity type registration

### Template Method

- **`AbstractPersistenceManager<T>`** -- Defines save/load skeleton; subclasses implement `getDataType()`, `onDataLoaded()`, `getCurrentData()`, `onCriticalLoadFailure()`
- **`AbstractProcessingBlockEntity`** -- Defines processing tick skeleton; subclasses implement specific processing logic
- **`AbstractPlantBlock`** -- Defines growth tick skeleton; subclasses define growth stages
- **`AbstractPlantGrowthHandler`** -- Defines growth calculation skeleton

### Strategy

- **Production growth handlers** -- Different `PlantGrowthHandler` implementations per crop (tobacco, cannabis, coca, poppy, mushroom) selected at runtime
- **Processing stage configs** -- `ProcessingStageConfig` defines per-stage behavior, composed via `ProductionConfig.Builder`

### Observer

- **Forge Event Bus** -- The primary observer implementation. Event handlers subscribe via `@SubscribeEvent` or `MinecraftForge.EVENT_BUS.register()`. The mod registers 26+ event handlers covering gameplay, inventory, combat, and lifecycle events.
- **`EventHelper`** -- Wraps all event handlers in `safeExecute()` for error isolation.

### Registry

- **Forge `DeferredRegister`** -- Used for all Minecraft registrations (items, blocks, entities, menus, creative tabs)
- **`ProductionRegistry`** -- Custom registry for production configurations
- **`UtilityRegistry`** -- Registers default utility types and resolves block references

### Command

- **Brigadier Commands** -- All commands registered via `RegisterCommandsEvent`
- **`CommandExecutor`** -- Encapsulates command execution with consistent error handling, providing `PlayerCommand` and `SourceCommand` functional interfaces

### Facade

- **`ScheduleMCAPI`** -- Unified entry point to 11 subsystem APIs
- **`PlotModAPI`** -- Legacy simplified facade with `Economy`, `Plots`, `Daily`, `Util` inner classes

### Adapter

- **Production adapters** -- Each production module (coffee, wine, etc.) adapts the generic production framework to its specific item/block types
- **`SaveableWrapper`** -- Adapts any `Runnable` save function into the `ISaveable` interface for IncrementalSaveManager registration

### Builder

- **`ProductionConfig.Builder`** -- Fluent builder for production type definitions
- **`BatchTransactionManager`** -- Fluent builder for batch economy transactions

---

## 7. API Architecture

### ScheduleMCAPI -- Singleton Entry Point

```java
ScheduleMCAPI api = ScheduleMCAPI.getInstance();

// Economy operations
IEconomyAPI economy = api.getEconomyAPI();
economy.deposit(playerUUID, 1000.0);

// Plot queries
IPlotAPI plots = api.getPlotAPI();
Optional<PlotRegion> plot = plots.getPlotAt(blockPos);

// Production system
IProductionAPI production = api.getProductionAPI();
production.registerCustomPlant(...);
```

### 11 Interface-Implementation Pairs

| Interface | Implementation | Module |
|-----------|---------------|--------|
| `IEconomyAPI` | `EconomyAPIImpl` | Bank accounts, transactions |
| `IPlotAPI` | `PlotAPIImpl` | Plot management |
| `IProductionAPI` | `ProductionAPIImpl` | Production system |
| `INPCAPI` | `NPCAPIImpl` | NPC management |
| `IPoliceAPI` | `PoliceAPIImpl` | Crime/wanted system |
| `IWarehouseAPI` | `WarehouseAPIImpl` | Warehouse management |
| `IMessagingAPI` | `MessagingAPIImpl` | Player messaging |
| `ISmartphoneAPI` | `SmartphoneAPIImpl` | Smartphone apps |
| `IVehicleAPI` | `VehicleAPIImpl` | Vehicle system |
| `IAchievementAPI` | `AchievementAPIImpl` | Achievement tracking |
| `IMarketAPI` | `MarketAPIImpl` | Dynamic market pricing |

### Thread Safety

- `ScheduleMCAPI.instance` is `volatile` with double-checked locking
- All economy operations use `ConcurrentHashMap` for thread-safe account access
- API getter methods throw `IllegalStateException` if called before initialization
- `isInitialized()` checks all 11 fields for null

### Initialization Guard

Every API getter validates initialization:

```java
public IEconomyAPI getEconomyAPI() {
    if (economyAPI == null) {
        throw new IllegalStateException(
            "EconomyAPI not initialized! Call ScheduleMCAPI.initialize() first.");
    }
    return economyAPI;
}
```

### Legacy API -- PlotModAPI

`PlotModAPI` provides a simpler static API predating `ScheduleMCAPI`:

- `PlotModAPI.Economy.getBalance(player)`, `giveMoney()`, `takeMoney()`, `transferMoney()`
- `PlotModAPI.Plots.getPlotAt(pos)`, `hasPlotAccess()`, `getPlayerPlots()`
- `PlotModAPI.Daily.hasClaimedToday()`, `getStreak()`
- `PlotModAPI.Util.formatMoney()`, `isPlotModLoaded()`, `getVersion()`

---

## 8. Security and Validation

### InputValidation Utility

Centralized validation for all user inputs. Returns `Result` objects with i18n error keys.

**Validated inputs:**

| Method | Max Length | Checks |
|--------|-----------|--------|
| `validateNPCName()` | 32 chars | Regex whitelist, min 2 chars, dangerous patterns |
| `validatePlotName()` | 64 chars | Regex whitelist |
| `validateTerritoryName()` | 48 chars | Regex whitelist, dangerous patterns |
| `validateSkinFileName()` | 128 chars | Filename whitelist, path traversal, reserved names (CON, PRN, etc.) |
| `validateDialogText()` | 512 chars | No leading `/`, command injection, dangerous patterns |
| `validatePath()` | N/A | No `..`, no absolute paths, whitelist prefixes (config/, skins/, data/, backups/) |
| `validatePacketString()` | 1024 chars | Length limit |
| `validateBlockPos()` | N/A | Coordinate bounds (-30M to +30M, Y: -64 to 320) |
| `validatePlotRegion()` | N/A | Both positions valid, max 10000 blocks span |
| `validateAmount()` | 1 trillion | No NaN/Infinity, non-negative |
| `validatePercentage()` | 100 | Range 0-100 |

**Dangerous pattern detection:**
- SQL injection: `'--`, `'; drop`, `1=1`
- XSS: `<script`, `javascript:`
- Command injection: `/op`, `/gamemode`, `/execute`, `/give`, `/setblock`, `/kill`
- Control characters: any char < 32 except `\n`, `\r`, `\t`

### AntiExploitManager

Economic exploit prevention:

1. **Daily sell volume limit** -- Progressive penalty when approaching/exceeding configurable daily limit. Penalty multiplier: `max(0.3, 1.0 - overPercent * 0.5)`
2. **Mass-sell detection** -- Tracks sell frequency within cooldown windows. Applies `massSellPenalty` multiplier for rapid mass-selling
3. **Escalating warnings** -- Warning level 0-3 per player. Level 3+ applies additional 0.5x penalty
4. **Daily reset** -- Volume and cooldown counters reset each Minecraft day; warning levels persist

All thresholds are configurable via `ModConfigHandler`.

### RateLimiter

Per-player action throttling to prevent request flooding.

### EventHelper -- Error Isolation

All event handlers are wrapped in `safeExecute()`:

```java
private static void safeExecute(Runnable handler, String context) {
    try {
        handler.run();
    } catch (Exception e) {
        ScheduleMC.LOGGER.error("Error in event handler ({}): {}", context, e.getMessage(), e);
        // Log but don't crash
    }
}
```

This prevents any single event handler exception from crashing the server or breaking other handlers.

### PacketHandler -- Network Security

- `handleServerPacket()` -- Validates sender is non-null `ServerPlayer`
- `handleAdminPacket()` -- Adds permission level check before handler execution
- All handlers wrapped in try-catch with player-facing error messages

---

## 9. CI/CD Pipeline

### GitHub Actions Configuration

**File:** `.github/workflows/ci.yml`

**Triggers:**
- Push to: `main`, `master`, `develop`, `claude/**`
- Pull requests to: `main`, `master`, `develop`

**Environment:**
- Ubuntu latest
- Java 17 (Temurin distribution)
- Gradle caching enabled

### Pipeline Steps

```
1. Checkout code (actions/checkout@v4)
      |
2. Setup JDK 17 Temurin (actions/setup-java@v4)
      |
3. Grant gradlew execute permission
      |
4. Build with Gradle (./gradlew build --no-daemon)
      |
5. Run Unit Tests (./gradlew test --no-daemon)
   |  continue-on-error: false (build fails on test failure)
      |
6. Run Integration Tests (--tests "*.integration.*")
   |  continue-on-error: true (advisory only)
      |
7. Upload Test Results (retention: 30 days)
      |
8. Generate JaCoCo Coverage Report
   |  continue-on-error: true
      |
9. Upload Coverage Report (retention: 30 days)
      |
10. Check Code Coverage (jacocoTestCoverageVerification)
    |  continue-on-error: true
      |
11. Archive Build Artifacts (*.jar, retention: 7 days)
```

### Code Coverage Configuration (JaCoCo)

**Tool version:** 0.8.11

**Coverage thresholds:**

| Scope | Metric | Minimum |
|-------|--------|---------|
| Global | All counters | 60% |
| `de.rolandsw.schedulemc.util.*` | LINE coverage | 80% |

**Exclusions from coverage analysis:**
- `**/generated/**` -- Generated code
- `**/*Event*.class` -- Minecraft event handlers (require game world)
- `**/client/gui/**` -- GUI classes (require game world)
- `**/blockentity/**` -- Block entities (require game world)

### Build Dependencies

**Runtime:**
- `net.minecraftforge:forge:1.20.1-47.4.0`
- `com.google.code.gson:gson:2.10.1`
- `de.maxhenkel.corelib:corelib:1.20.1-1.1.1` (networking, config, OBJ models, GUI)

**Compile-only (optional integrations):**
- JEI 15.2.0.27
- Jade 11.8.0
- The One Probe 1.20.1-10.0.2

**MapView dependencies:**
- Mixin 0.8.5
- MixinExtras 0.5.0

**Testing:**
- JUnit Jupiter 5.10.1
- Mockito 5.8.0
- AssertJ 3.24.2

---

## 10. Dependency Graph

### Module Dependency Overview

```
                              ScheduleMC.java (Entry Point)
                                     |
                 +-------------------+-------------------+
                 |                   |                   |
              config/             util/              api/
           ModConfigHandler    (19 utilities)     ScheduleMCAPI
                 |                   |                   |
     +-----------+-----------+       |         +---------+---------+
     |           |           |       |         |                   |
  economy/   region/      npc/      |      PlotModAPI        impl/ (11)
  (64 files) (19 files) (173 files) |         |                   |
     |           |           |       |         +---delegates-to----+
     |           |           |       |                   |
     +-----+----+-----+-----+-------+         (all internal modules)
           |          |
     production/    vehicle/
     (29 files)    (137 files)
           |
  +--------+--------+--------+--------+--------+
  |        |        |        |        |        |
tobacco/ cannabis/ coca/  coffee/  wine/   beer/
(82)     (28)     (29)    (60)    (63)    (62)
  poppy/ meth/ lsd/ mdma/ mushroom/ cheese/ honey/ chocolate/
  (20)   (23)  (22) (21)   (15)     (53)   (60)    (69)
```

### Detailed Dependency Matrix

```
Module              Depends On
------              ----------
economy/         -> util/, config/
region/          -> util/, config/, economy/ (rent payments)
npc/             -> util/, config/, economy/, region/ (plot-aware NPCs)
vehicle/         -> util/, config/, economy/ (fuel costs), region/ (parking)
production/      -> util/, config/
tobacco/         -> production/, economy/ (selling)
cannabis/        -> production/, economy/
coca/            -> production/, economy/
poppy/           -> production/, economy/
meth/            -> production/, economy/
lsd/             -> production/, economy/
mdma/            -> production/, economy/
mushroom/        -> production/, economy/
coffee/          -> production/, economy/
wine/            -> production/, economy/
cheese/          -> production/, economy/
honey/           -> production/, economy/
chocolate/       -> production/, economy/
beer/            -> production/, economy/
warehouse/       -> util/, config/, economy/ (delivery fees), region/ (access)
commands/        -> economy/, region/, npc/, util/
client/          -> economy/, region/, npc/, messaging/, achievement/, gang/
mapview/         -> region/, npc/, vehicle/
messaging/       -> util/, config/
achievement/     -> util/, config/, economy/
level/           -> util/, config/
lock/            -> util/, config/, region/ (door protection)
gang/            -> util/, config/, economy/ (weekly fees), territory/
territory/       -> util/, config/, region/
towing/          -> util/, config/, economy/ (fees), vehicle/
utility/         -> util/, config/, region/ (plot utilities)
market/          -> util/, config/, economy/ (price system)
api/             -> economy/, region/, production/, npc/, warehouse/,
                    messaging/, vehicle/, achievement/, market/
```

### Cross-Cutting Concerns

The `util/` package provides infrastructure used by virtually every module:

```
util/
  AbstractPersistenceManager  --> Used by: economy, region, npc, gang, territory,
  |                               achievement, npc/life/* managers
  IncrementalSaveManager      --> Orchestrates all persistence managers
  |
  BackupManager               --> Used by AbstractPersistenceManager
  |
  ThreadPoolManager           --> Used by: ScheduleMC (startup), IncrementalSaveManager,
  |                               mapview (rendering), any async operation
  EventHelper                 --> Used by: ScheduleMC (all event handlers),
  |                               all event handler classes
  PacketHandler               --> Used by: all network handlers
  |
  CommandExecutor             --> Used by: all command classes
  |
  InputValidation             --> Used by: npc/ (names), region/ (plot names),
  |                               territory/ (names), lock/ (codes), commands
  TickThrottler               --> Used by: production block entities, npc tick logic
  |
  VersionedData               --> Used by: any manager needing data migration
  |
  HealthCheckManager          --> Used by: ScheduleMC (startup health check)
  |
  ConfigCache                 --> Used by: config/ (cached config reads)
  |
  GsonHelper                  --> Used by: all JSON serialization
  |
  SaveableWrapper             --> Used by: ScheduleMC (wrapping non-ISaveable managers)
  |
  PerformanceMonitor          --> Used by: diagnostics, admin commands
  |
  VersionChecker              --> Used by: client/ (update notifications)
  |
  PersistenceHelper           --> Used by: persistence utilities
  |
  LocaleHelper                --> Used by: i18n text formatting
```

### Data Flow: Player Sells Item to NPC Shop

This example traces a typical operation through the architecture:

```
1. Player right-clicks NPC shop
   --> NPCStealingHandler checks interaction type
   --> NPC entity opens shop GUI

2. Player selects item to sell
   --> Client sends sell packet via NPCNetworkHandler
   --> PacketHandler.handleServerPacket() validates player

3. Server processes sale
   --> InputValidation.validateAmount() checks sell price
   --> AntiExploitManager.checkAndGetMultiplier() applies penalties if needed
   --> RateLimiter checks action frequency

4. Economy transaction
   --> EconomyManager.deposit(playerUUID, adjustedPrice)
   --> TransactionHistory records transaction
   --> EconomyManager.markDirty()

5. Dynamic pricing update
   --> DynamicPriceManager adjusts NPC prices based on supply
   --> EconomyController.periodicUpdate() recalculates on next cycle

6. Persistence
   --> IncrementalSaveManager detects EconomyManager.isDirty()
   --> On next save tick: AbstractPersistenceManager.save()
       --> BackupManager.createBackup()
       --> Atomic write to JSON file

7. Client sync
   --> Server sends SyncBankDataPacket to player
   --> Client updates displayed balance
```

---

## Appendix: File Count Summary

| Module | Java Files |
|--------|-----------|
| economy/ | 64 |
| npc/ | 173 |
| vehicle/ | 137 |
| mapview/ | 122 |
| tobacco/ | 82 |
| chocolate/ | 69 |
| wine/ | 63 |
| beer/ | 62 |
| coffee/ | 60 |
| honey/ | 60 |
| cheese/ | 53 |
| gang/ | 32 |
| production/ | 29 |
| coca/ | 29 |
| cannabis/ | 28 |
| meth/ | 23 |
| lsd/ | 22 |
| mdma/ | 21 |
| warehouse/ | 20 |
| poppy/ | 20 |
| region/ | 19 |
| util/ | 19 |
| lock/ | 17 |
| mushroom/ | 15 |
| towing/ | 15 |
| api/ | 24 |
| client/ | 38 |
| level/ | 12 |
| achievement/ | 11 |
| territory/ | 11 |
| messaging/ | 9 |
| utility/ | 8 |
| commands/ | 5 |
| market/ | 3 |
| Other (events, managers, items, config, data, gui, network, player) | ~50 |
| **Total** | **1,407** |
