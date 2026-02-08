# ScheduleMC Developer Guide

Comprehensive guide for developers who want to build, extend, and contribute to the
ScheduleMC Minecraft Forge 1.20.1 mod.

**Mod Version:** 3.6.0-beta
**API Version:** 3.0.0
**Forge Version:** 1.20.1-47.4.0
**Java Version:** 17
**Author:** Luckas R. Schneider (Minecraft425HD)

---

## Table of Contents

1. [Development Environment Setup](#1-development-environment-setup)
2. [Project Structure](#2-project-structure)
3. [Adding Items and Blocks](#3-adding-items-and-blocks)
4. [Using the ScheduleMC API](#4-using-the-schedulemc-api)
5. [Adding a New Production System](#5-adding-a-new-production-system)
6. [Adding NPC Behaviors](#6-adding-npc-behaviors)
7. [Creating Smartphone Apps](#7-creating-smartphone-apps)
8. [Data Persistence](#8-data-persistence)
9. [Networking](#9-networking)
10. [Testing](#10-testing)
11. [Commands](#11-commands)
12. [Configuration](#12-configuration)
13. [Code Style and Conventions](#13-code-style-and-conventions)

---

## 1. Development Environment Setup

### Prerequisites

- **Java 17** (Eclipse Temurin recommended). The Forge 1.20.1 toolchain requires
  exactly Java 17. Newer versions may cause build failures.
- **Git** for version control.
- **IntelliJ IDEA** (recommended) or **Eclipse** as IDE.

### Gradle Configuration

The project uses Gradle with the following JVM settings defined in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx3G
org.gradle.daemon=false
```

The Gradle daemon is disabled to prevent memory issues during long development sessions.
The heap is set to 3 GB to accommodate Minecraft decompilation.

### Forge MDK

ScheduleMC is built on Forge MDK 1.20.1-47.4.0. The `build.gradle` declares:

```groovy
plugins {
    id 'net.minecraftforge.gradle' version '[6.0,6.2)'
    id 'jacoco'
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.4.0'
}
```

Mappings use the official Mojang channel for Minecraft 1.20.1:

```properties
mapping_channel=official
mapping_version=1.20.1
```

### Clone and Build

```bash
# Clone the repository
git clone https://github.com/Minecraft425HD/ScheduleMC.git
cd ScheduleMC

# Build the mod (produces jar in build/libs/)
./gradlew build

# Run the Minecraft client with the mod loaded
./gradlew runClient

# Run a dedicated server with the mod loaded
./gradlew runServer

# Run unit tests
./gradlew test

# Generate JaCoCo coverage report (output in build/reports/jacoco/)
./gradlew jacocoTestReport
```

### IDE Setup: IntelliJ IDEA

1. Open IntelliJ IDEA and select **File > Open**, then navigate to the cloned
   ScheduleMC directory.
2. IntelliJ will detect the Gradle project automatically. Wait for the import to
   complete and all dependencies to download.
3. Run `./gradlew genIntellijRuns` from the terminal to generate run configurations.
4. In the Run Configurations dropdown, you will see **runClient**, **runServer**, and
   **runData** targets.
5. Set the Project SDK to Java 17 (Temurin) under **File > Project Structure > Project**.

### IDE Setup: Eclipse

1. Run `./gradlew genEclipseRuns` from the terminal.
2. Open Eclipse and select **File > Import > Existing Gradle Project**.
3. Navigate to the ScheduleMC directory and complete the import.
4. Run configurations for client and server will be available in the Run menu.

### Key Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| Minecraft Forge | 1.20.1-47.4.0 | Mod loader |
| Gson | 2.10.1 | JSON serialization for persistence |
| CoreLib | 1.20.1-1.1.1 | Networking, config, OBJ models, GUI |
| Mixin / MixinExtras | 0.8.5 / 0.5.0 | MapView integration |
| JUnit 5 | 5.10.1 | Unit testing |
| Mockito | 5.8.0 | Mocking framework |
| AssertJ | 3.24.2 | Fluent test assertions |

Optional compile-only integrations (not required at runtime):
- JEI 15.2.0.27
- Jade 11.8.0
- The One Probe 1.20.1-10.0.2-forge

---

## 2. Project Structure

The main source root is `src/main/java/de/rolandsw/schedulemc/`. The mod ID is
`schedulemc` and the base package is `de.rolandsw.schedulemc`.

```
src/main/java/de/rolandsw/schedulemc/
├── ScheduleMC.java              Main mod class (@Mod entry point)
├── ModCreativeTabs.java         Creative mode tab registration
├── package-info.java
│
├── api/                         Public API (11 interfaces + implementations)
│   ├── ScheduleMCAPI.java       Central singleton entry point
│   ├── economy/                 IEconomyAPI
│   ├── plot/                    IPlotAPI
│   ├── production/              IProductionAPI
│   ├── npc/                     INPCAPI
│   ├── police/                  IPoliceAPI
│   ├── warehouse/               IWarehouseAPI
│   ├── messaging/               IMessagingAPI
│   ├── smartphone/              ISmartphoneAPI
│   ├── vehicle/                 IVehicleAPI
│   ├── achievement/             IAchievementAPI
│   ├── market/                  IMarketAPI
│   └── impl/                    API implementation classes
│
├── economy/                     Economy system (11 managers)
│   ├── EconomyManager.java      Bank accounts and balances
│   ├── WalletManager.java       Physical cash system
│   ├── TransactionHistory.java  Transaction log
│   ├── InterestManager.java     Interest calculations
│   ├── LoanManager.java         Loan system
│   ├── CreditScoreManager.java  Credit scoring
│   ├── CreditLoanManager.java   NPC-based loans
│   ├── TaxManager.java          Tax collection
│   ├── SavingsAccountManager.java Savings accounts
│   ├── OverdraftManager.java    Overdraft (Dispo)
│   ├── RecurringPaymentManager.java Standing orders
│   └── ...
│
├── region/                      Plot system
│   ├── PlotManager.java         Plot CRUD, spatial index
│   ├── PlotRegion.java          Plot data model
│   ├── PlotType.java            RESIDENTIAL, COMMERCIAL, SHOP, etc.
│   └── ...
│
├── npc/                         NPC AI system (173 files)
│   ├── entity/
│   │   ├── CustomNPCEntity.java PathfinderMob with schedules, goals
│   │   └── NPCEntities.java    Entity type registration
│   ├── data/                    NPCData, NPCType, NPCPersonality
│   ├── goals/                   AI goals (MoveToWork, Home, Leisure, Police)
│   ├── life/                    NPC Life System (needs, emotions, memory)
│   ├── personality/             Personality and relationship system
│   ├── pathfinding/             Custom NPCPathNavigation
│   ├── crime/                   Crime, bounty, prison systems
│   ├── bank/                    NPC bank interactions
│   └── ...
│
├── vehicle/                     Vehicle system (137 files)
│   └── Main.java                Vehicle mod integration entry point
│
├── production/                  Generic production framework
│   ├── config/                  ProductionConfig (Builder pattern)
│   ├── blocks/                  AbstractPlantBlock, AbstractProcessingBlock
│   ├── blockentity/             Processing block entities
│   ├── core/                    ProductionType, GenericQuality, PotType
│   ├── items/                   PackagedDrugItem
│   ├── growth/                  Growth stage logic
│   ├── data/                    Production data persistence
│   └── nbt/                     NBT serialization helpers
│
├── tobacco/                     Tobacco production chain
├── cannabis/                    Cannabis production chain
├── coca/                        Coca production chain
├── poppy/                       Poppy production chain
├── meth/                        Meth synthesis chain
├── lsd/                         LSD synthesis chain
├── mdma/                        MDMA synthesis chain
├── mushroom/                    Mushroom cultivation chain
├── coffee/                      Coffee production chain
├── wine/                        Wine production chain
├── cheese/                      Cheese production chain
├── honey/                       Honey production chain
├── chocolate/                   Chocolate production chain
├── beer/                        Beer production chain
│
├── warehouse/                   Warehouse system
├── messaging/                   In-game messaging system
├── achievement/                 Achievement tracking
├── level/                       Producer level progression
├── lock/                        Door lock, key, combination system
├── gang/                        Gang system with missions/scenarios
├── territory/                   Territory control system
├── towing/                      Towing and membership system
├── market/                      Dynamic market pricing (UDPS)
├── utility/                     Plot utility management (power, water)
│
├── commands/                    Command system (139 commands)
│   ├── CommandExecutor.java     Unified command error handling
│   ├── PlotCommand.java         /plot commands
│   ├── MoneyCommand.java        /money commands
│   ├── AdminCommand.java        /admin commands
│   └── HealthCommand.java       /health diagnostics
│
├── client/                      Client-side code
│   ├── screen/
│   │   ├── SmartphoneScreen.java Main smartphone GUI
│   │   └── apps/                18 smartphone app screens
│   ├── network/                 Client-bound packet handlers
│   ├── KeyBindings.java         Keybind registration
│   └── ...HUD overlays, handlers
│
├── mapview/                     Map/minimap system (122 files)
│
├── network/                     Base networking
│   └── AbstractPacket.java      Abstract packet base class
│
├── util/                        Utilities and infrastructure
│   ├── AbstractPersistenceManager.java  Base for all persistent managers
│   ├── IncrementalSaveManager.java      Background save orchestration
│   ├── BackupManager.java               Backup rotation
│   ├── PacketHandler.java               Packet handling utilities
│   ├── EventHelper.java                 Event handling boilerplate reducer
│   ├── ThreadPoolManager.java           Thread pool lifecycle
│   ├── InputValidation.java             Parameter validation
│   ├── HealthCheckManager.java          System health monitoring
│   ├── GsonHelper.java                  Gson utilities
│   ├── ConfigCache.java                 Configuration caching
│   ├── PerformanceMonitor.java          Performance tracking
│   ├── RateLimiter.java                 Rate limiting
│   └── TickThrottler.java               Tick-based throttling
│
├── config/                      Configuration
│   └── ModConfigHandler.java    Forge config (COMMON + CLIENT specs)
│
├── managers/                    Shared managers
│   ├── NPCNameRegistry.java     NPC name management
│   ├── NPCEntityRegistry.java   NPC entity tracking
│   ├── DailyRewardManager.java  Daily login rewards
│   └── RentManager.java         Plot rent system
│
├── items/                       Core mod items
│   ├── ModItems.java            DeferredRegister for items
│   └── PlotSelectionTool.java   Plot selection tool item
│
├── events/                      Global event handlers
│   ├── BlockProtectionHandler.java
│   ├── InventoryRestrictionHandler.java
│   └── PlayerDisconnectHandler.java
│
├── player/                      Player data systems
│   ├── PlayerTracker.java       Player activity tracking
│   └── PlayerSettingsManager.java
│
├── data/                        Data generation
└── gui/                         Shared GUI components
```

### Resource Structure

```
src/main/resources/
├── META-INF/
│   └── mods.toml                Mod metadata
├── assets/schedulemc/
│   ├── blockstates/             Block state JSON files
│   ├── models/
│   │   ├── block/               Block model JSON files
│   │   └── item/                Item model JSON files
│   ├── textures/
│   │   ├── block/               Block textures
│   │   ├── item/                Item textures
│   │   └── gui/                 GUI textures (smartphone app icons, etc.)
│   ├── lang/
│   │   ├── de_de.json           German translations (primary)
│   │   └── en_us.json           English translations
│   ├── skins/                   NPC skin files
│   ├── sounds.json              Sound definitions
│   └── sounds/                  Sound files
└── data/schedulemc/             Data-driven content (recipes, loot tables)
```

---

## 3. Adding Items and Blocks

### DeferredRegister Pattern

ScheduleMC uses Forge's `DeferredRegister` for all registry objects. Each subsystem
has its own registration class.

**Registering a new item:**

```java
package de.rolandsw.schedulemc.myfeature.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MyFeatureItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    public static final RegistryObject<Item> MY_ITEM =
        ITEMS.register("my_item", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MY_SPECIAL_ITEM =
        ITEMS.register("my_special_item", MySpecialItem::new);
}
```

**Registering a new block with BlockEntity:**

```java
package de.rolandsw.schedulemc.myfeature.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class MyFeatureBlocks {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    // Block registration
    public static final RegistryObject<Block> MY_MACHINE = registerBlockWithItem(
        "my_machine",
        () -> new MyMachineBlock(Block.Properties.of().strength(3.5f))
    );

    // BlockEntity registration
    public static final RegistryObject<BlockEntityType<MyMachineBlockEntity>> MY_MACHINE_BE =
        BLOCK_ENTITIES.register("my_machine",
            () -> BlockEntityType.Builder.of(
                MyMachineBlockEntity::new, MY_MACHINE.get()
            ).build(null));

    // Helper: register block + corresponding BlockItem
    private static RegistryObject<Block> registerBlockWithItem(
            String name, Supplier<Block> blockSupplier) {
        RegistryObject<Block> block = BLOCKS.register(name, blockSupplier);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
```

**Wiring up registries in `ScheduleMC.java`:**

All `DeferredRegister` instances must be registered on the mod event bus in the
`ScheduleMC` constructor:

```java
// In ScheduleMC constructor:
MyFeatureItems.ITEMS.register(modEventBus);
MyFeatureBlocks.BLOCKS.register(modEventBus);
MyFeatureBlocks.ITEMS.register(modEventBus);
MyFeatureBlocks.BLOCK_ENTITIES.register(modEventBus);
```

### Resource Files

For every new item or block, you must create corresponding JSON resource files.

**Item model** (`src/main/resources/assets/schedulemc/models/item/my_item.json`):

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "schedulemc:item/my_item"
  }
}
```

**Block model** (`src/main/resources/assets/schedulemc/models/block/my_machine.json`):

```json
{
  "parent": "block/cube_all",
  "textures": {
    "all": "schedulemc:block/my_machine"
  }
}
```

**Blockstate** (`src/main/resources/assets/schedulemc/blockstates/my_machine.json`):

```json
{
  "variants": {
    "": { "model": "schedulemc:block/my_machine" }
  }
}
```

**Item model for BlockItem** (`src/main/resources/assets/schedulemc/models/item/my_machine.json`):

```json
{
  "parent": "schedulemc:block/my_machine"
}
```

**Textures:**
- Place item textures in `src/main/resources/assets/schedulemc/textures/item/`
- Place block textures in `src/main/resources/assets/schedulemc/textures/block/`

### Language Files

ScheduleMC ships with German (primary) and English translations. Add entries to both:

**`src/main/resources/assets/schedulemc/lang/de_de.json`:**
```json
{
  "item.schedulemc.my_item": "Mein Gegenstand",
  "block.schedulemc.my_machine": "Meine Maschine"
}
```

**`src/main/resources/assets/schedulemc/lang/en_us.json`:**
```json
{
  "item.schedulemc.my_item": "My Item",
  "block.schedulemc.my_machine": "My Machine"
}
```

---

## 4. Using the ScheduleMC API

The ScheduleMC API allows external mods to interact with all major subsystems. The API
consists of 11 interface modules accessed through a central singleton.

### Adding the Dependency

In your mod's `build.gradle`, add ScheduleMC as a dependency:

```groovy
dependencies {
    // Compile against the ScheduleMC API
    compileOnly files('libs/schedulemc-3.6.0-beta.jar')
}
```

In your `mods.toml`, declare the dependency:

```toml
[[dependencies.yourmodid]]
    modId = "schedulemc"
    mandatory = true
    versionRange = "[3.6.0,)"
    ordering = "AFTER"
    side = "BOTH"
```

### Accessing the API

The central entry point is `ScheduleMCAPI.getInstance()`. The API is initialized during
the `ServerStartedEvent` phase, so it is only available after the server has fully started.

```java
import de.rolandsw.schedulemc.api.ScheduleMCAPI;

// Check if API is ready
ScheduleMCAPI api = ScheduleMCAPI.getInstance();
if (!api.isInitialized()) {
    // API not yet ready - server has not fully started
    return;
}
```

### Available API Modules

| Module | Getter | Description |
|---|---|---|
| Economy | `api.getEconomyAPI()` | Bank accounts, deposits, withdrawals, transfers |
| Plot | `api.getPlotAPI()` | Plot creation, ownership, permissions |
| Production | `api.getProductionAPI()` | Production chains, quality system |
| NPC | `api.getNPCAPI()` | NPC entities, data, schedules |
| Police | `api.getPoliceAPI()` | Wanted levels, crime, prison |
| Warehouse | `api.getWarehouseAPI()` | Storage, deliveries |
| Messaging | `api.getMessagingAPI()` | In-game messages |
| Smartphone | `api.getSmartphoneAPI()` | Smartphone apps, notifications |
| Vehicle | `api.getVehicleAPI()` | Vehicle management, fuel |
| Achievement | `api.getAchievementAPI()` | Achievement tracking, rewards |
| Market | `api.getMarketAPI()` | Dynamic pricing, supply/demand |

### Example: Economy Integration

```java
import de.rolandsw.schedulemc.api.ScheduleMCAPI;
import de.rolandsw.schedulemc.api.economy.IEconomyAPI;
import java.util.UUID;

public class MyModEconomyIntegration {

    public void rewardPlayer(UUID playerUUID, double amount) {
        IEconomyAPI economy = ScheduleMCAPI.getInstance().getEconomyAPI();

        // Check if player has an account
        if (!economy.hasAccount(playerUUID)) {
            economy.createAccount(playerUUID);
        }

        // Check current balance
        double balance = economy.getBalance(playerUUID);

        // Deposit money with a description for the transaction log
        economy.deposit(playerUUID, amount, "Reward from MyMod");

        // Check if player can afford something
        if (economy.canAfford(playerUUID, 500.0)) {
            economy.withdraw(playerUUID, 500.0, "MyMod purchase");
        }

        // Transfer between players
        UUID otherPlayer = UUID.randomUUID();
        boolean success = economy.transfer(playerUUID, otherPlayer, 100.0, "Trade");

        // Get top 10 richest players
        var topPlayers = economy.getTopBalances(10);

        // Get full transaction history
        var history = economy.getTransactionHistory(playerUUID, 50);
    }
}
```

### Example: Plot Integration

```java
import de.rolandsw.schedulemc.api.ScheduleMCAPI;
import de.rolandsw.schedulemc.api.plot.IPlotAPI;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.PlotType;
import net.minecraft.core.BlockPos;
import java.util.List;
import java.util.UUID;

public class MyModPlotIntegration {

    public void checkPlotPermissions(BlockPos pos, UUID playerUUID) {
        IPlotAPI plots = ScheduleMCAPI.getInstance().getPlotAPI();

        // Check if a position is inside a plot
        PlotRegion plot = plots.getPlotAt(pos);
        if (plot != null) {
            // Plot exists at this position
            String plotId = plot.getId();

            // Check ownership
            List<PlotRegion> playerPlots = plots.getPlotsByOwner(playerUUID);

            // Get all shop plots
            List<PlotRegion> shops = plots.getPlotsByType(PlotType.SHOP);

            // Find nearby plots
            List<PlotRegion> nearby = plots.getPlotsInRadius(pos, 50.0);

            // Get trusted players
            var trusted = plots.getTrustedPlayers(plotId);
        }

        // Create a new plot programmatically
        PlotRegion newPlot = plots.createPlot(
            new BlockPos(0, 64, 0),    // corner 1
            new BlockPos(15, 80, 15),  // corner 2
            "MyModPlot",               // name
            PlotType.COMMERCIAL,       // type
            5000.0                     // price
        );
    }
}
```

### Example: NPC Interaction

```java
import de.rolandsw.schedulemc.api.ScheduleMCAPI;
import de.rolandsw.schedulemc.api.npc.INPCAPI;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.data.NPCType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import java.util.Collection;

public class MyModNPCIntegration {

    public void interactWithNPCs(ServerLevel level) {
        INPCAPI npcAPI = ScheduleMCAPI.getInstance().getNPCAPI();

        // Get all NPCs in the world
        Collection<CustomNPCEntity> allNPCs = npcAPI.getAllNPCs(level);

        // Find NPCs by type (e.g., all shop keepers)
        Collection<CustomNPCEntity> shopKeepers =
            npcAPI.getNPCsByType(NPCType.VERKAEUFER);

        // Find NPCs near a position (within 20 blocks)
        Collection<CustomNPCEntity> nearbyNPCs =
            npcAPI.getNPCsInRadius(level, new BlockPos(100, 64, 200), 20.0);

        // Modify NPC properties
        for (CustomNPCEntity npc : nearbyNPCs) {
            npcAPI.setNPCName(npc, "Custom Name");
            npcAPI.setNPCHome(npc, new BlockPos(100, 64, 200));
            npcAPI.setNPCWork(npc, new BlockPos(110, 64, 210));
            npcAPI.setNPCSchedule(npc, "workstart", 700);  // 07:00
            npcAPI.setNPCSchedule(npc, "workend", 1800);   // 18:00
        }
    }
}
```

### Example: Market Price Queries

```java
import de.rolandsw.schedulemc.api.ScheduleMCAPI;
import de.rolandsw.schedulemc.api.market.IMarketAPI;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.Map;

public class MyModMarketIntegration {

    public void queryMarket() {
        IMarketAPI market = ScheduleMCAPI.getInstance().getMarketAPI();

        // Get current dynamic price for an item
        double price = market.getCurrentPrice(Items.DIAMOND);

        // Get the base price (without dynamic adjustments)
        double basePrice = market.getBasePrice(Items.DIAMOND);

        // Get the price multiplier (1.0 = base, >1.0 = higher, <1.0 = lower)
        double multiplier = market.getPriceMultiplier(Items.DIAMOND);

        // Check supply and demand levels (0-100)
        int demand = market.getDemandLevel(Items.DIAMOND);
        int supply = market.getSupplyLevel(Items.DIAMOND);

        // Record transactions (affects dynamic pricing)
        market.recordPurchase(Items.DIAMOND, 5);  // Price rises
        market.recordSale(Items.DIAMOND, 10);      // Price drops

        // Get all market prices
        Map<Item, Double> allPrices = market.getAllPrices();
    }
}
```

### Thread Safety

All API methods are thread-safe. The internal implementations use `ConcurrentHashMap`
and synchronized access where required. You can safely call API methods from any thread.

### Error Handling

API methods throw `IllegalArgumentException` for null parameters and invalid values.
Some methods throw `IllegalStateException` if the API has not been initialized yet.
Always check `api.isInitialized()` before using the API, or catch these exceptions.

---

## 5. Adding a New Production System

ScheduleMC provides a generic production framework that handles plant growth, processing
stages, and quality tiers. Existing systems (tobacco, cannabis, coffee, wine, etc.) are
all built on this framework.

### Step 1: Define the Production Type

Create an enum implementing `ProductionType` for your production variants:

```java
package de.rolandsw.schedulemc.myproduct;

import de.rolandsw.schedulemc.production.core.ProductionType;

public enum MyProductType implements ProductionType {
    VARIANT_A("Variant A", "variant_a"),
    VARIANT_B("Variant B", "variant_b");

    private final String displayName;
    private final String id;

    MyProductType(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
    }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getId() { return id; }
}
```

### Step 2: Create a ProductionConfig

Use the `ProductionConfig.Builder` to define production parameters:

```java
import de.rolandsw.schedulemc.production.config.ProductionConfig;
import de.rolandsw.schedulemc.production.config.ProductionConfig.ProductionCategory;
import de.rolandsw.schedulemc.production.config.ProductionConfig.ProcessingStageConfig;

ProductionConfig myConfig = new ProductionConfig.Builder("my_product_a", "My Product A")
    .colorCode("§a")                           // Minecraft color code
    .basePrice(25.0)                           // Base price per unit in Euro
    .growthTicks(4800)                         // Ticks to fully grow (0 to 7)
    .baseYield(4)                              // Base harvest yield
    .category(ProductionCategory.PLANT)        // PLANT, MUSHROOM, CHEMICAL, EXTRACT, PROCESSED
    .requiresLight(true)                       // Needs light to grow
    .minLightLevel(10)                         // Minimum light level
    .requiresWater(true)                       // Needs water nearby
    .requiresTemperature(false)                // Temperature check
    .addProcessingStage("drying",              // Processing stage definition
        new ProcessingStageConfig(
            "Drying",                          // Stage name
            2400,                              // Processing time in ticks
            "schedulemc:fresh_my_product",     // Input item
            "schedulemc:dried_my_product",     // Output item
            true                               // Quality carries over
        ))
    .addProcessingStage("refining",
        new ProcessingStageConfig(
            "Refining",                        // Stage name
            3600,                              // Processing time in ticks
            "schedulemc:dried_my_product",     // Input item
            "schedulemc:refined_my_product",   // Output item
            true,                              // Quality carries over
            "diesel",                          // Required resource
            10                                 // Resource amount per process
        ))
    .build();
```

### Step 3: Create the Plant Block

Extend `AbstractPlantBlock` for your crop:

```java
package de.rolandsw.schedulemc.myproduct.blocks;

import de.rolandsw.schedulemc.myproduct.MyProductType;
import de.rolandsw.schedulemc.production.blocks.AbstractPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class MyProductPlantBlock extends AbstractPlantBlock<MyProductType> {

    public MyProductPlantBlock(MyProductType type) {
        super(type);
        // AbstractPlantBlock provides:
        // - AGE property (0-7 growth stages)
        // - HALF property (LOWER/UPPER for two-block-tall plants at stage 4+)
        // - VoxelShape definitions for each stage
        // - Automatic randomTick-based growth
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos,
                           RandomSource random) {
        // Call super for default growth behavior, or override for custom logic
        super.randomTick(state, level, pos, random);
    }

    // Override getGrowthChance() to customize growth speed
    // Override getHarvestDrops() to customize harvest results
}
```

### Step 4: Create Processing Blocks

Extend `AbstractProcessingBlock` for processing machinery:

```java
package de.rolandsw.schedulemc.myproduct.blocks;

import de.rolandsw.schedulemc.production.blocks.AbstractProcessingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MyProcessingBlock extends AbstractProcessingBlock {

    public MyProcessingBlock(Properties properties) {
        super(properties);
        // AbstractProcessingBlock provides:
        // - Right-click to open GUI (MenuProvider)
        // - Inventory drop on block break
        // - BlockEntity support via EntityBlock
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MyProcessingBlockEntity(pos, state);
    }
}
```

### Step 5: Register Items, Blocks, and BlockEntities

Follow the patterns in Section 3 to create registration classes, then wire them into
the `ScheduleMC` constructor.

### Step 6: Register via the Production API

Register your production config so the API knows about it:

```java
// During server startup or mod initialization:
IProductionAPI productionAPI = ScheduleMCAPI.getInstance().getProductionAPI();
productionAPI.registerProduction(myConfig);
```

### Step 7: Create Resource Files

Create all necessary model, texture, blockstate, and language files as described in
Section 3. For plant blocks with multiple growth stages, you need blockstate variants
for each AGE value (0-7) and each HALF (lower/upper).

### Step 8: Wire Up Networking

If your processing blocks need client-server communication (e.g., for GUI updates),
create network packets and register them in `commonSetup`. See Section 9 for details.

### Quality System

The production framework includes a built-in quality system with tiers defined by
`GenericQuality`. The default 4-tier system provides Bronze, Silver, Gold, and Diamond
quality levels. Quality affects:

- Sale price multiplier
- Visual indicators (color codes)
- Processing outcomes

You can define custom quality tiers:

```java
GenericQuality[] customTiers = {
    new GenericQuality("Common", "§7", 1.0),
    new GenericQuality("Rare", "§9", 1.5),
    new GenericQuality("Epic", "§5", 2.5),
    new GenericQuality("Legendary", "§6", 4.0)
};

ProductionConfig config = new ProductionConfig.Builder("my_product", "My Product")
    .qualityTiers(customTiers)
    .build();
```

---

## 6. Adding NPC Behaviors

NPCs in ScheduleMC use Forge's `Goal` system. `CustomNPCEntity` extends `PathfinderMob`
and registers goals with priorities in `registerGoals()`.

### Goal Priority Layout

| Priority | Goal | Active For |
|---|---|---|
| 0 | `FloatGoal` | All NPCs (swimming) |
| 1 | `OpenDoorGoal` | All NPCs |
| 2 | `PolicePatrolGoal` | POLIZEI NPCs only |
| 3 | `PoliceStationGoal` | POLIZEI NPCs only |
| 4 | `MoveToHomeGoal` | BEWOHNER, VERKAEUFER |
| 5 | `MoveToWorkGoal` | VERKAEUFER, BANK |
| 6 | `MoveToLeisureGoal` | BEWOHNER |
| 7 | `LookAtPlayerGoal` | All NPCs |
| 8 | `RandomLookAroundGoal` | All NPCs |

### Creating a New Goal

Create a class extending Forge's `Goal`:

```java
package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class MyCustomGoal extends Goal {

    private final CustomNPCEntity npc;
    private BlockPos targetPos;
    private static final double ARRIVAL_THRESHOLD = 2.0D;
    private static final int RECALCULATE_INTERVAL = 100; // Ticks
    private int tickCounter = 0;

    public MyCustomGoal(CustomNPCEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Check NPC type - only run for specific types
        if (npc.getNpcData().getNpcType() !=
                de.rolandsw.schedulemc.npc.data.NPCType.BEWOHNER) {
            return false;
        }

        // Check if movement is enabled in NPC behavior settings
        if (!npc.getNpcData().getBehavior().canMove()) {
            return false;
        }

        // Check time of day (Minecraft day = 24000 ticks)
        long dayTime = npc.level().getDayTime() % 24000;
        // Example: only active between 12000-18000 (evening)
        if (dayTime < 12000 || dayTime > 18000) {
            return false;
        }

        // Set target position
        this.targetPos = calculateTargetPosition();
        return targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null) return false;
        double distance = npc.distanceToSqr(
            targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
        return distance > ARRIVAL_THRESHOLD * ARRIVAL_THRESHOLD;
    }

    @Override
    public void start() {
        if (targetPos != null) {
            npc.getNavigation().moveTo(
                targetPos.getX() + 0.5,
                targetPos.getY(),
                targetPos.getZ() + 0.5,
                1.0  // speed modifier
            );
        }
    }

    @Override
    public void tick() {
        tickCounter++;
        if (tickCounter >= RECALCULATE_INTERVAL) {
            tickCounter = 0;
            // Recalculate path periodically
            if (targetPos != null) {
                npc.getNavigation().moveTo(
                    targetPos.getX() + 0.5,
                    targetPos.getY(),
                    targetPos.getZ() + 0.5,
                    1.0
                );
            }
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
        targetPos = null;
        tickCounter = 0;
    }

    private BlockPos calculateTargetPosition() {
        // Your logic to determine where the NPC should go
        return null;
    }
}
```

### Registering Goals in CustomNPCEntity

To add a new goal, register it in the `registerGoals()` method of `CustomNPCEntity`.
Choose an appropriate priority number (lower = higher priority):

```java
@Override
protected void registerGoals() {
    // ... existing goals ...

    // Add your custom goal at an appropriate priority
    this.goalSelector.addGoal(6, new MyCustomGoal(this));
}
```

### Personality-Based Behavior

NPCs have a `NPCPersonality` enum that influences behavior selection. Access it via:

```java
NPCPersonality personality = npc.getNpcData().getPersonality();
```

Use the personality in your goal's `canUse()` method to vary behavior:

```java
@Override
public boolean canUse() {
    NPCPersonality personality = npc.getNpcData().getPersonality();
    // Example: only adventurous NPCs use this goal
    if (personality != NPCPersonality.ABENTEUERLICH) {
        return false;
    }
    // ...
}
```

### Schedule Integration

NPCs have configurable work schedules accessible through `NPCData`:

```java
long workStart = npc.getNpcData().getWorkStartTime(); // e.g., 6000 (6:00 AM)
long workEnd = npc.getNpcData().getWorkEndTime();       // e.g., 18000 (6:00 PM)

// Check if current time falls within a range
long dayTime = npc.level().getDayTime() % 24000;
boolean isWorkTime = isTimeBetween(dayTime, workStart, workEnd);
```

### NPC Life System

CustomNPCEntity also integrates with the NPC Life System, which provides:
- **Needs** (`NPCNeeds`): hunger, energy, social, entertainment
- **Emotions** (`NPCEmotions`, `EmotionState`): happy, sad, angry, scared, etc.
- **Memory** (`NPCMemory`): remembers interactions and events
- **Traits** (`NPCTraits`): persistent personality modifiers
- **Behavior Engine** (`NPCBehaviorEngine`): state machine for complex behaviors

Access via:
```java
NPCLifeData lifeData = npc.getLifeData();
NPCNeeds needs = lifeData.getNeeds();
NPCEmotions emotions = lifeData.getEmotions();
```

---

## 7. Creating Smartphone Apps

The smartphone system provides an in-game phone with multiple app screens. Players
open the phone with a keybind, and each app is a separate `Screen` implementation.

### App Registration via API

The `ISmartphoneAPI` allows external mods to register custom apps:

```java
ISmartphoneAPI smartphoneAPI = ScheduleMCAPI.getInstance().getSmartphoneAPI();

// Register a new app
boolean registered = smartphoneAPI.registerApp(
    "mymod_tracker",     // Unique app ID
    "Player Tracker",    // Display name
    "§bBlue"             // Icon color code
);

// Send notifications to a player's phone
smartphoneAPI.sendNotification(
    playerUUID,
    "mymod_tracker",
    "New tracking data available!"
);

// Check if player has the smartphone item
if (smartphoneAPI.hasSmartphone(playerUUID)) {
    // Player can receive notifications
}

// List all registered apps
Set<String> apps = smartphoneAPI.getRegisteredApps();

// Unregister when your mod unloads
smartphoneAPI.unregisterApp("mymod_tracker");
```

### Creating an App Screen

App screens are client-side only and extend Minecraft's `Screen` class. They are
located in `de.rolandsw.schedulemc.client.screen.apps/`.

Existing apps include:
- `BankAppScreen` - Bank account and transactions
- `MessagesAppScreen` - In-game messaging
- `PlotAppScreen` - Plot management
- `AchievementAppScreen` - Achievement progress
- `DealerAppScreen` - Dealer network
- `ProductsAppScreen` - Product catalog
- `OrderAppScreen` - Order management
- `ContactsAppScreen` - Contact list
- `SettingsAppScreen` - Phone settings
- `CrimeStatsAppScreen` - Crime statistics
- `GangAppScreen` - Gang management
- `TowingServiceAppScreen` - Towing service
- `ProducerLevelAppScreen` - Producer level progression

To create a new app screen:

```java
package de.rolandsw.schedulemc.client.screen.apps;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MyAppScreen extends Screen {

    private final Screen parentScreen; // The SmartphoneScreen to return to

    public MyAppScreen(Screen parent) {
        super(Component.translatable("gui.smartphone.app.myapp.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Add buttons, text fields, etc.
        // Use Component.translatable() for all user-facing text

        // Back button to return to smartphone home
        this.addRenderableWidget(
            net.minecraft.client.gui.components.Button.builder(
                Component.translatable("gui.smartphone.back"),
                button -> this.minecraft.setScreen(parentScreen)
            ).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY,
                       float partialTick) {
        this.renderBackground(graphics);

        // Draw app content
        graphics.drawCenteredString(
            this.font,
            this.title,
            this.width / 2,
            20,
            0xFFFFFF
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        // Return to smartphone screen instead of closing entirely
        this.minecraft.setScreen(parentScreen);
    }
}
```

### Network Packets for App Data

If your app needs server-side data, create a request/response packet pair:

```java
// Client sends request when app opens
public class MyAppDataRequestPacket {
    public static void encode(MyAppDataRequestPacket msg, FriendlyByteBuf buf) {
        // Nothing to encode for a simple request
    }

    public static MyAppDataRequestPacket decode(FriendlyByteBuf buf) {
        return new MyAppDataRequestPacket();
    }

    public static void handle(MyAppDataRequestPacket msg,
                              Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Gather data on server and send response
            // MyNetworking.INSTANCE.send(
            //     PacketDistributor.PLAYER.with(() -> player),
            //     new MyAppDataResponsePacket(data));
        });
    }
}
```

### Integrating into SmartphoneScreen

To add your app to the smartphone home screen, it must be wired into
`SmartphoneScreen.java`. The screen uses a grid layout with app icons. Add your app's
icon `ResourceLocation` and connect it in the click handler to open your `MyAppScreen`.

---

## 8. Data Persistence

ScheduleMC uses JSON-based file persistence with automatic backup, atomic writes, and
incremental save optimization.

### AbstractPersistenceManager

The base class for all persistent managers. Extend it to add persistence to your system:

```java
package de.rolandsw.schedulemc.myfeature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MyFeatureManager extends AbstractPersistenceManager<Map<UUID, MyFeatureData>> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile MyFeatureManager instance;
    private final Map<UUID, MyFeatureData> dataMap = new ConcurrentHashMap<>();

    private MyFeatureManager(File dataFile) {
        super(dataFile, GSON);
    }

    public static MyFeatureManager getInstance(net.minecraft.server.MinecraftServer server) {
        if (instance == null) {
            synchronized (MyFeatureManager.class) {
                if (instance == null) {
                    File file = new File(
                        server.getServerDirectory(), "config/schedulemc/myfeature.json");
                    instance = new MyFeatureManager(file);
                    instance.load();
                }
            }
        }
        return instance;
    }

    // ===== AbstractPersistenceManager Implementation =====

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, MyFeatureData>>() {}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, MyFeatureData> data) {
        dataMap.clear();
        dataMap.putAll(data);
    }

    @Override
    protected Map<UUID, MyFeatureData> getCurrentData() {
        return new ConcurrentHashMap<>(dataMap);
    }

    @Override
    protected String getComponentName() {
        return "MyFeatureManager";
    }

    @Override
    protected String getHealthDetails() {
        return dataMap.size() + " entries";
    }

    @Override
    protected void onCriticalLoadFailure() {
        dataMap.clear(); // Start fresh on corruption
    }

    // ===== ISaveable Implementation (for IncrementalSaveManager) =====

    @Override
    public int getPriority() {
        return 5; // 0 = highest priority, 7 = lowest
    }

    // ===== Business Logic =====

    public void setData(UUID playerUUID, MyFeatureData data) {
        dataMap.put(playerUUID, data);
        markDirty(); // Flags data for next save cycle
    }

    public MyFeatureData getData(UUID playerUUID) {
        return dataMap.get(playerUUID);
    }
}
```

### Key Features of AbstractPersistenceManager

- **Automatic backup rotation**: Creates backups before each save via `BackupManager`
- **Atomic file writes**: Writes to a `.tmp` file first, then atomically replaces
- **Backup recovery**: On load failure, automatically attempts to restore from backup
- **Corrupt file preservation**: Saves corrupted files as `.CORRUPT_<timestamp>` for
  forensic analysis
- **Health monitoring**: `isHealthy()`, `getLastError()`, `getHealthInfo()`
- **Dirty flag tracking**: `markDirty()` and `saveIfNeeded()` for efficient saving
- **ISaveable interface**: Integrates with the `IncrementalSaveManager` for background saves

### IncrementalSaveManager Integration

Register your manager with the `IncrementalSaveManager` in `ScheduleMC.onServerStarted()`:

```java
// If your manager extends AbstractPersistenceManager:
saveManager.register(MyFeatureManager.getInstance(server));

// If your manager does not extend AbstractPersistenceManager, use SaveableWrapper:
saveManager.register(new SaveableWrapper(
    "MyFeatureManager",                   // Name for logging
    MyFeatureManager::saveIfNeeded,       // Save action
    5                                     // Priority (0=highest, 7=lowest)
));
```

Priority levels in use:
- 0-2: Critical managers (EconomyManager, PlotManager, BountyManager, TerritoryManager)
- 3: Economy advanced systems, Market, Gangs, Transaction History
- 4: Player systems, Wallet, Daily Rewards, Gang Missions
- 5: Messaging, NPC Life System, NPC Registry, Lock, Vehicle systems
- 6: Warehouse, Towing, Utility systems
- 7: State Account

### Gson Serialization

Use `Gson` with `GsonBuilder` for JSON serialization. The project uses Gson 2.10.1:

```java
private static final Gson GSON = new GsonBuilder()
    .setPrettyPrinting()        // Human-readable JSON
    .serializeNulls()           // Include null fields (optional)
    .create();
```

For custom serialization of Minecraft types (BlockPos, UUID, etc.), use custom
`TypeAdapter` implementations or the helpers in `GsonHelper`.

---

## 9. Networking

ScheduleMC uses Forge's `SimpleChannel` networking system. There are two patterns:
the legacy encode/decode/handle pattern and the newer `AbstractPacket` base class.

### Creating Network Channels

Each subsystem creates its own `SimpleChannel`:

```java
package de.rolandsw.schedulemc.myfeature.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MyFeatureNetworking {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "myfeature_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        // Server-bound packet (client -> server)
        INSTANCE.messageBuilder(MyRequestPacket.class, id(),
                NetworkDirection.PLAY_TO_SERVER)
            .decoder(MyRequestPacket::decode)
            .encoder(MyRequestPacket::encode)
            .consumerMainThread(MyRequestPacket::handle)
            .add();

        // Client-bound packet (server -> client)
        INSTANCE.messageBuilder(MyResponsePacket.class, id(),
                NetworkDirection.PLAY_TO_CLIENT)
            .decoder(MyResponsePacket::decode)
            .encoder(MyResponsePacket::encode)
            .consumerMainThread(MyResponsePacket::handle)
            .add();
    }
}
```

### Pattern A: Standard Packet (encode/decode/handle)

```java
package de.rolandsw.schedulemc.myfeature.network;

import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class MyRequestPacket {

    private final String itemId;
    private final int amount;

    public MyRequestPacket(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    // Encode: write data to the buffer
    public static void encode(MyRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.itemId, 256); // Max 256 chars for security
        buf.writeInt(msg.amount);
    }

    // Decode: read data from the buffer
    // SECURITY: Always use max-length variants to prevent DoS/memory attacks
    public static MyRequestPacket decode(FriendlyByteBuf buf) {
        String itemId = buf.readUtf(256);
        int amount = buf.readInt();
        return new MyRequestPacket(itemId, amount);
    }

    // Handle: process the packet on the server
    public static void handle(MyRequestPacket msg,
                              Supplier<NetworkEvent.Context> ctx) {
        // Use PacketHandler utility for consistent error handling
        PacketHandler.handleServerPacket(ctx, player -> {
            // Server-side logic here
            // player is guaranteed non-null
        });
    }
}
```

### Pattern B: AbstractPacket Base Class

For simpler packets, extend `AbstractPacket`:

```java
package de.rolandsw.schedulemc.myfeature.network;

import de.rolandsw.schedulemc.network.AbstractPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class MyDataSyncPacket extends AbstractPacket {

    private final String data;

    public MyDataSyncPacket(String data) {
        this.data = data;
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        buf.writeUtf(data, 256);
    }

    public static MyDataSyncPacket read(FriendlyByteBuf buf) {
        return new MyDataSyncPacket(buf.readUtf(256));
    }

    @Override
    protected void handleOnClient() {
        // Client-side logic (for server -> client packets)
    }

    @Override
    protected void handleOnServer(NetworkEvent.Context ctx) {
        // Server-side logic (for client -> server packets)
    }
}
```

`AbstractPacket` provides helper methods for common data types:
- `writeUUID()` / `readUUID()`
- `writeStringList()` / `readStringList()` (with size limits)
- `writeStringSet()` / `readStringSet()` (with size limits)
- `writeUUIDList()` / `readUUIDList()`
- `writeOptionalBlockPos()` / `readOptionalBlockPos()`

### PacketHandler Utility

The `PacketHandler` utility class provides consistent packet handling with error
recovery:

```java
// Standard server packet with automatic player null-check
PacketHandler.handleServerPacket(ctx, player -> {
    // player is guaranteed non-null
    // Exceptions are caught, logged, and reported to the player
});

// Admin packet with permission check
PacketHandler.handleAdminPacket(ctx, 2, player -> {
    // Only executed if player has permission level >= 2
});

// Client packet (no player context)
PacketHandler.handleClientPacket(ctx, () -> {
    // Client-side logic
});
```

### Registering Packets

Register your networking channel in `commonSetup` within `ScheduleMC.java`:

```java
private void commonSetup(final FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
        // ... existing registrations ...
        MyFeatureNetworking.register();
    });
}
```

### Sending Packets

```java
// Send to server (from client)
MyFeatureNetworking.INSTANCE.sendToServer(new MyRequestPacket("item_id", 5));

// Send to specific player (from server)
MyFeatureNetworking.INSTANCE.send(
    PacketDistributor.PLAYER.with(() -> serverPlayer),
    new MyResponsePacket(data)
);

// Send to all players
MyFeatureNetworking.INSTANCE.send(
    PacketDistributor.ALL.noArg(),
    new MyBroadcastPacket(data)
);
```

### Security Best Practices

- Always use max-length string reads: `buf.readUtf(256)` instead of `buf.readUtf()`
- Limit collection sizes: `Math.min(buf.readInt(), 1000)` for list/set sizes
- Validate all incoming data on the server side
- Use `PacketHandler.handleAdminPacket()` for admin-only operations
- Never trust client-sent data without validation

---

## 10. Testing

ScheduleMC uses JUnit 5 with Mockito and AssertJ for unit testing.

### Test Dependencies

Defined in `build.gradle`:

```groovy
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.10.1'
    testImplementation 'org.junit.jupiter:junit-jupiter-params:5.10.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.10.1'
    testImplementation 'org.mockito:mockito-core:5.8.0'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.8.0'
    testImplementation 'org.assertj:assertj-core:3.24.2'
}
```

### Test Configuration

```groovy
test {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }
    maxHeapSize = '2G'
}
```

### MinecraftTestBootstrap

For tests that need to mock Minecraft classes, use `MinecraftTestBootstrap`:

```java
import de.rolandsw.schedulemc.test.MinecraftTestBootstrap;
import org.junit.jupiter.api.BeforeAll;

class MyFeatureTest {

    @BeforeAll
    static void setup() {
        MinecraftTestBootstrap.init();
    }

    // Tests can now mock Minecraft classes via Mockito inline mock maker
}
```

Mockito's inline mock maker (configured in `mockito-extensions`) allows mocking of
final classes without full Minecraft bootstrap. `MinecraftTestBootstrap.init()` is
idempotent and can be called multiple times safely.

### Unit Test Example

```java
package de.rolandsw.schedulemc.myfeature;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class MyFeatureManagerTest {

    @TempDir
    Path tempDir;

    private UUID testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = UUID.randomUUID();
        // Reset singleton state if needed via reflection
    }

    @AfterEach
    void tearDown() {
        // Clean up state
    }

    @Test
    void testCreateData() {
        // Given
        MyFeatureData data = new MyFeatureData("test_value");

        // When
        // manager.setData(testPlayer, data);

        // Then
        // assertThat(manager.getData(testPlayer)).isNotNull();
        // assertThat(manager.getData(testPlayer).getValue()).isEqualTo("test_value");
    }

    @Test
    void testNullParameterThrowsException() {
        assertThatThrownBy(() -> {
            // Call method with null parameter
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
```

### Test Patterns Used in the Project

**EconomyManagerTest** demonstrates the common pattern for testing singleton managers:

1. Use `@TempDir` for temporary file storage
2. Reset singleton state via reflection in `@BeforeEach`
3. Redirect file paths to temp directory
4. Create test data directly via reflection (bypassing config)
5. Restore original state in `@AfterEach`

**InputValidationTest** demonstrates pure unit testing with AssertJ:

```java
@Test
void testValidatePrice_Valid() {
    assertThat(InputValidation.validatePrice(100.0).isValid()).isTrue();
}

@Test
void testValidatePrice_Invalid() {
    assertThat(InputValidation.validatePrice(-100.0).isFailure()).isTrue();
    assertThat(InputValidation.validatePrice(Double.NaN).isFailure()).isTrue();
}
```

### Integration Test Example

```java
package de.rolandsw.schedulemc.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.file.Path;

class PersistenceIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoad() {
        File dataFile = tempDir.resolve("test_data.json").toFile();

        // Create manager, add data, save
        // MyManager manager = new MyManager(dataFile);
        // manager.setData(...);
        // manager.save();

        // Create new instance, load, verify
        // MyManager loaded = new MyManager(dataFile);
        // loaded.load();
        // assertThat(loaded.getData(...)).isEqualTo(expected);
    }

    @Test
    void testCorruptFileRecovery() {
        File dataFile = tempDir.resolve("test_data.json").toFile();

        // Write corrupt JSON
        // Files.writeString(dataFile.toPath(), "{invalid json");

        // Load should fall back gracefully
        // MyManager manager = new MyManager(dataFile);
        // manager.load();
        // assertThat(manager.isHealthy()).isFalse();
    }
}
```

### JaCoCo Code Coverage

JaCoCo is configured with the following thresholds:

```groovy
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.60 // 60% minimum global coverage
            }
        }
        rule {
            element = 'CLASS'
            includes = ['de.rolandsw.schedulemc.util.*']
            limit {
                counter = 'LINE'
                minimum = 0.80 // 80% minimum for utility classes
            }
        }
    }
}
```

Excluded from coverage (hard to unit test):
- `**/generated/**` - Generated code
- `**/*Event*.class` - Minecraft event handlers
- `**/client/gui/**` - GUI classes requiring game world
- `**/blockentity/**` - Block entities requiring game world

Run coverage:
```bash
./gradlew jacocoTestReport
# HTML report: build/reports/jacoco/test/html/index.html
# XML report:  build/reports/jacoco/test/jacocoTestReport.xml
```

Coverage verification runs as part of the `check` task:
```bash
./gradlew check  # Runs tests + coverage verification
```

---

## 11. Commands

### CommandExecutor Pattern

ScheduleMC uses the `CommandExecutor` utility to eliminate boilerplate in command
methods. It provides unified error handling, player extraction, and permission checking.

**Before (boilerplate):**
```java
private static int myCommand(CommandContext<CommandSourceStack> ctx) {
    try {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        // Command logic...
        ctx.getSource().sendSuccess(
            () -> Component.literal("Success!"), false);
        return 1;
    } catch (Exception e) {
        LOGGER.error("Error", e);
        ctx.getSource().sendFailure(Component.literal("Error!"));
        return 0;
    }
}
```

**After (with CommandExecutor):**
```java
private static int myCommand(CommandContext<CommandSourceStack> ctx) {
    return CommandExecutor.executePlayerCommand(ctx, "Error in myCommand",
        player -> {
            // Command logic...
            ctx.getSource().sendSuccess(
                () -> Component.literal("Success!"), false);
        });
}
```

### CommandExecutor Methods

| Method | Purpose |
|---|---|
| `executePlayerCommand(ctx, errorMsg, handler)` | Standard player command |
| `executeSourceCommand(ctx, errorMsg, handler)` | Command without player requirement |
| `executePlayerCommandWithMessage(ctx, errorMsg, successKey, handler)` | Auto success message |
| `executeAdminCommand(ctx, errorMsg, level, handler)` | Permission-checked command |

Helper methods:
- `CommandExecutor.sendSuccess(source, message)` - Green success message
- `CommandExecutor.sendFailure(source, message)` - Red error message
- `CommandExecutor.sendInfo(source, message)` - Yellow info message

### Registering New Commands

Commands are registered in the `onRegisterCommands` event handler in `ScheduleMC.java`.
Use Brigadier's command tree:

```java
package de.rolandsw.schedulemc.myfeature.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.rolandsw.schedulemc.commands.CommandExecutor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MyFeatureCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("myfeature")
            // /myfeature info
            .then(Commands.literal("info")
                .executes(MyFeatureCommand::showInfo))

            // /myfeature set <name> <value>
            .then(Commands.literal("set")
                .requires(source -> source.hasPermission(2)) // OP level 2
                .then(Commands.argument("name", StringArgumentType.word())
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                        .executes(MyFeatureCommand::setValue))))

            // /myfeature admin reset (requires OP level 4)
            .then(Commands.literal("admin")
                .then(Commands.literal("reset")
                    .executes(MyFeatureCommand::adminReset)))
        );
    }

    private static int showInfo(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Error showing info",
            player -> {
                player.sendSystemMessage(
                    Component.translatable("myfeature.info.header"));
                // ... display info ...
            });
    }

    private static int setValue(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Error setting value",
            player -> {
                String name = StringArgumentType.getString(ctx, "name");
                double value = DoubleArgumentType.getDouble(ctx, "value");
                // ... set value ...
                CommandExecutor.sendSuccess(ctx.getSource(),
                    "Value set: " + name + " = " + value);
            });
    }

    private static int adminReset(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executeAdminCommand(ctx, "Error resetting", 4,
            source -> {
                // ... admin reset logic ...
                CommandExecutor.sendSuccess(source, "Reset complete");
            });
    }
}
```

Then register in `ScheduleMC.onRegisterCommands()`:

```java
@SubscribeEvent
public void onRegisterCommands(RegisterCommandsEvent event) {
    EventHelper.handleEvent(() -> {
        // ... existing registrations ...
        MyFeatureCommand.register(event.getDispatcher());
    }, "onRegisterCommands");
}
```

### Permission Levels

Minecraft/Forge uses numeric permission levels:

| Level | Description | Example Commands |
|---|---|---|
| 0 | All players | `/myfeature info` |
| 1 | Moderators | - |
| 2 | Game masters | `/gamemode`, `/myfeature set` |
| 3 | Admins | `/ban`, `/kick` |
| 4 | Server owner | `/stop`, `/myfeature admin reset` |

Use `.requires(source -> source.hasPermission(level))` in Brigadier registration to
enforce permission checks.

---

## 12. Configuration

### ModConfigHandler

ScheduleMC uses Forge's `ForgeConfigSpec` system with two config specs:

- **COMMON** (`ModConfigHandler.SPEC`): Server-side configuration shared between
  client and server. Registered as `ModConfig.Type.COMMON`.
- **CLIENT** (`ModConfigHandler.CLIENT_SPEC`): Client-only settings (vehicle rendering,
  etc.). Registered as `ModConfig.Type.CLIENT`.

```java
// In ScheduleMC constructor:
ModLoadingContext context = ModLoadingContext.get();
context.registerConfig(ModConfig.Type.COMMON, ModConfigHandler.SPEC);
context.registerConfig(ModConfig.Type.CLIENT, ModConfigHandler.CLIENT_SPEC);
```

### Accessing Configuration Values

Configuration values are accessed through the `ModConfigHandler.COMMON` instance:

```java
// Economy settings
double startBalance = ModConfigHandler.COMMON.START_BALANCE.get();
int saveInterval = ModConfigHandler.COMMON.SAVE_INTERVAL_MINUTES.get();

// Plot settings
long minPlotSize = ModConfigHandler.COMMON.MIN_PLOT_SIZE.get();
double minPlotPrice = ModConfigHandler.COMMON.MIN_PLOT_PRICE.get();

// Shop settings
boolean shopEnabled = ModConfigHandler.COMMON.SHOP_ENABLED.get();
double buyMultiplier = ModConfigHandler.COMMON.BUY_MULTIPLIER.get();
```

### Configuration Categories

The COMMON config is organized into sections:

| Section | Key Config Values |
|---|---|
| Economy | `START_BALANCE`, `SAVE_INTERVAL_MINUTES`, `SAVINGS_INTEREST_RATE`, `OVERDRAFT_INTEREST_RATE`, `TAX_SALES_RATE` |
| Plots | `MIN_PLOT_SIZE`, `MAX_PLOT_SIZE`, `MIN_PLOT_PRICE`, `MAX_PLOT_PRICE`, `MAX_TRUSTED_PLAYERS` |
| Daily Rewards | `DAILY_REWARD`, `DAILY_REWARD_STREAK_BONUS`, `MAX_STREAK_DAYS` |
| Rent | `RENT_ENABLED`, `MIN_RENT_PRICE`, `MIN_RENT_DAYS`, `MAX_RENT_DAYS` |
| Shop | `SHOP_ENABLED`, `BUY_MULTIPLIER`, `SELL_MULTIPLIER` |
| NPC | `NPC_WALKABLE_BLOCKS` |
| Warehouse | `WAREHOUSE_DEFAULT_DELIVERY_PRICE` |

### Adding New Configuration Values

To add a new config value, modify the `Common` class in `ModConfigHandler`:

```java
public static class Common {
    // In the existing Common class:

    public final ForgeConfigSpec.DoubleValue MY_FEATURE_RATE;
    public final ForgeConfigSpec.BooleanValue MY_FEATURE_ENABLED;

    Common(ForgeConfigSpec.Builder builder) {
        // ... existing config values ...

        builder.push("myfeature");

        MY_FEATURE_ENABLED = builder
            .comment("Enable or disable My Feature")
            .define("enabled", true);

        MY_FEATURE_RATE = builder
            .comment("Rate for My Feature (0.0 - 1.0)")
            .defineInRange("rate", 0.5, 0.0, 1.0);

        builder.pop();
    }
}
```

### Runtime Configuration

Some features use runtime configuration that can be changed via commands or the
admin interface without restarting the server. Examples include:

- `DeliveryPriceConfig`: Delivery pricing loaded from main config at startup
- `ConfigCache`: Cached config values for performance-critical paths
- Dynamic market parameters adjusted by `EconomyController`

The `ConfigCache` utility provides thread-safe caching of frequently-accessed config
values to avoid repeated `ForgeConfigSpec` lookups in hot paths.

---

## 13. Code Style and Conventions

### Language

- **Legacy code comments**: German (inherited from early development)
- **API documentation (Javadoc)**: English for all public API interfaces
- **New code**: English is preferred for comments and documentation
- **Translation keys**: Both `de_de.json` and `en_us.json` must be maintained

### Singleton Pattern

Manager classes use the double-checked locking singleton pattern with `volatile`:

```java
private static volatile MyManager instance;

public static MyManager getInstance(MinecraftServer server) {
    if (instance == null) {
        synchronized (MyManager.class) {
            if (instance == null) {
                instance = new MyManager(server);
            }
        }
    }
    return instance;
}
```

Some managers also provide a `resetInstance()` method called during server shutdown to
prevent stale state across server restarts:

```java
public static void resetInstance() {
    if (instance != null) {
        instance.save();
        instance = null;
    }
}
```

### Thread Safety

- Use `ConcurrentHashMap` for all shared mutable state
- Use `ConcurrentHashMap.newKeySet()` for concurrent sets
- Use `volatile` for singleton instance fields
- All API implementations must be thread-safe
- Use `synchronized` blocks only when `ConcurrentHashMap` is insufficient
- The `ThreadPoolManager` provides managed thread pools for async I/O operations

### Parameter Validation

All public API methods must validate parameters:

```java
@Override
public double getBalance(UUID playerUUID) {
    if (playerUUID == null) {
        throw new IllegalArgumentException("playerUUID cannot be null");
    }
    return economyManager.getBalance(playerUUID);
}
```

For commands, use `InputValidation` utilities:

```java
InputValidation.ValidationResult result = InputValidation.validatePrice(price);
if (result.isFailure()) {
    player.sendSystemMessage(Component.literal(result.getErrorMessage()));
    return;
}
```

### Event Handling

Use `EventHelper` to wrap event handlers with consistent error handling:

```java
@SubscribeEvent
public void onServerTick(TickEvent.ServerTickEvent event) {
    EventHelper.handleServerTickEnd(event, server -> {
        // Only runs at tick END, with try-catch wrapping
    });
}

@SubscribeEvent
public void onRegisterCommands(RegisterCommandsEvent event) {
    EventHelper.handleEvent(() -> {
        // Wrapped with try-catch and logging
    }, "onRegisterCommands");
}
```

### Naming Conventions

| Type | Convention | Example |
|---|---|---|
| Packages | Lowercase, feature-based | `de.rolandsw.schedulemc.economy` |
| Classes | PascalCase | `EconomyManager`, `PlotRegion` |
| Interfaces | PascalCase with I-prefix (API) | `IEconomyAPI`, `INPCAPI` |
| Constants | UPPER_SNAKE_CASE | `MOD_ID`, `SAVE_INTERVAL` |
| Methods | camelCase | `getBalance()`, `markDirty()` |
| Fields | camelCase | `needsSave`, `tickCounter` |
| Registry IDs | snake_case | `"fresh_virginia_leaf"`, `"terracotta_pot"` |

### Registration Pattern

Follow the established pattern for DeferredRegister usage:
1. Create a static `DeferredRegister` field in the registration class
2. Register all objects as `RegistryObject` fields
3. Call `.register(modEventBus)` in the `ScheduleMC` constructor
4. Network channels are registered in `commonSetup` via `FMLCommonSetupEvent`

### Performance Conventions

- Use `TickThrottler` or manual tick counters for periodic operations
- Use `RateLimiter` for player-triggered actions
- Cache expensive computations (e.g., `ConfigCache`, `SmartphoneScreen` label caching)
- Use `ThreadPoolManager.getIOPool()` for async file I/O
- Profile with `PerformanceMonitor` during development
- Batch database/file operations where possible
- Use `IncrementalSaveManager` instead of manual save calls

### Error Handling

- Never crash the server on non-critical errors
- Log errors with `LOGGER.error()` and continue operation (graceful degradation)
- Wrap event handlers with `EventHelper` for automatic error catching
- Wrap packet handlers with `PacketHandler` for automatic error reporting to players
- Preserve corrupt data files for forensic analysis
- Provide health monitoring via `HealthCheckManager`

---

## Appendix: Quick Reference

### Common Build Commands

```bash
./gradlew build              # Full build with tests and coverage
./gradlew runClient          # Launch Minecraft client
./gradlew runServer          # Launch dedicated server (--nogui)
./gradlew test               # Run unit tests only
./gradlew jacocoTestReport   # Generate coverage HTML report
./gradlew check              # Tests + coverage verification
./gradlew clean build        # Clean rebuild
```

### File Locations

| What | Where |
|---|---|
| Main mod class | `src/main/java/de/rolandsw/schedulemc/ScheduleMC.java` |
| API interfaces | `src/main/java/de/rolandsw/schedulemc/api/` |
| API implementations | `src/main/java/de/rolandsw/schedulemc/api/impl/` |
| Forge config | `src/main/java/de/rolandsw/schedulemc/config/ModConfigHandler.java` |
| Language files | `src/main/resources/assets/schedulemc/lang/` |
| Block models | `src/main/resources/assets/schedulemc/models/block/` |
| Item models | `src/main/resources/assets/schedulemc/models/item/` |
| Blockstates | `src/main/resources/assets/schedulemc/blockstates/` |
| Textures | `src/main/resources/assets/schedulemc/textures/` |
| Test sources | `src/test/java/de/rolandsw/schedulemc/` |
| Coverage reports | `build/reports/jacoco/test/html/index.html` |
| Build output | `build/libs/schedulemc-3.6.0-beta.jar` |
| Mod metadata | `src/main/resources/META-INF/mods.toml` |
| Gradle properties | `gradle.properties` |

### API Entry Point Cheat Sheet

```java
ScheduleMCAPI api = ScheduleMCAPI.getInstance();

api.getEconomyAPI()       // Bank, transfers, transactions
api.getPlotAPI()          // Plots, ownership, permissions
api.getProductionAPI()    // Production chains, quality
api.getNPCAPI()           // NPCs, schedules, types
api.getPoliceAPI()        // Wanted, crime, prison
api.getWarehouseAPI()     // Storage, deliveries
api.getMessagingAPI()     // In-game messages
api.getSmartphoneAPI()    // Phone apps, notifications
api.getVehicleAPI()       // Vehicles, fuel
api.getAchievementAPI()   // Achievements, rewards
api.getMarketAPI()        // Dynamic pricing
```
