# ScheduleMC Developer Guide

Comprehensive guide for developers who want to build, extend, and contribute to the
ScheduleMC Minecraft Forge 1.20.1 mod.

**Mod Version:** 3.9.0-beta
**API Version:** 3.2.0
**Forge Version:** 1.20.1-47.4.0
**Java Version:** 17
**Author:** Luckas R. Schneider (Minecraft425HD)

---

## Table of Contents

1. [Development Environment Setup](#1-development-environment-setup)
2. [Project Structure](#2-project-structure)
3. [Adding Items and Blocks](#3-adding-items-and-blocks)
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
| JUnit 5 | 5.10.1 | Unit testing |
| Mockito | 5.8.0 | Mocking framework |
| AssertJ | 3.24.2 | Fluent test assertions |

MapView's rendering hooks use standard Forge client events
(`RenderGuiOverlayEvent`, `ClientTickEvent`) — no Mixin dependency.
There are no optional compile-only integrations; JEI/Jade/The One Probe
were previously listed but had no integration code and have been removed.

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
├── production/                  Shared production infrastructure
│   ├── blocks/                  PlantPotBlock (shared pot/growth block used by real crops)
│   ├── blockentity/             AbstractItemHandlerBlockEntity (shared ItemStackHandler base)
│   ├── core/                    ProductionType, ProductionQuality, DrugType, PotType (shared interfaces)
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

## 5. Adding a New Production System

There is no generic, config-driven production framework in ScheduleMC — an earlier attempt
at one (`ProductionConfig`/`ProductionRegistry`/`UnifiedProcessingBlockEntity`/`GenericQuality`
and a few related classes) was built but never adopted by a single real block, and was
removed as dead code (see `CLAUDE.md`). Every real production chain (tobacco, cannabis,
coca, poppy, mushroom, meth, lsd, mdma, coffee, wine, cheese, chocolate, honey) is
hand-written instead, following the pattern below.

### The real pattern

1. **Quality tiers**: a dedicated enum per goods category implementing `ProductionQuality`
   (e.g. `TobaccoQuality`, `CannabisQuality`, `MDMAQuality` — 4 or 5 tiers each, not shared
   across categories).
2. **Production type/variant**: a dedicated enum or class per category implementing
   `ProductionType` (e.g. `TobaccoType`, `CannabisStrain`, `MethVariant`). This interface
   provides `getItemCategory()` (for `EconomyController` price/risk lookups) and
   `calculateDynamicPrice()` (delegates to `EconomyController.getSellPrice()`).
3. **Processing block entities**: extend the shared `AbstractItemHandlerBlockEntity`
   (`production/blockentity/`) for "legal" goods categories (beer, wine, cheese, chocolate,
   coffee, honey, tobacco) — it provides the `ItemStackHandler`/NBT plumbing, but each
   concrete class (e.g. `AbstractDryingRackBlockEntity`) hand-rolls its own `tick()` and
   progress logic; there is no shared generic tick loop to override. Illegal-drug categories
   (cannabis, coca, mdma, lsd, meth) instead extend `BlockEntity` directly with their own
   fields — there is currently no shared base across these five categories.
4. **Size variants without subclass duplication**: where a machine comes in
   Small/Medium/Large variants, use the `Supplier<Integer>`-based constructor pattern
   (see `AbstractDryingRackBlockEntity`/`AbstractFermentationBarrelBlockEntity`) instead of
   one subclass per size — this eliminates duplicated subclasses whose only job was
   overriding 1-2 config getters. See `CLAUDE.md` for when this pattern is and isn't worth
   applying (it was rejected for Beer/Wine, where subclasses also differ in display name and
   menu type).
5. **Plant blocks**: extend `Block` directly (see `TobaccoPlantBlock`, `CannabisPlantBlock`,
   `CocaPlantBlock`); `PlantPotBlock` (`production/blocks/`) is the one real shared
   building block used across several crop types for pot/growth-stage handling.
6. **Pricing**: register the product with `EconomyController` (see
   `EconomyController.initializeReferencePrices()`) rather than a standalone config object.

Follow an existing, similar category as your template (e.g. copy the tobacco or coffee
package structure) rather than starting from a shared abstract base — that is how every
real production chain in this codebase was built.

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

### Adding a New App

There is no runtime app-registration mechanism. The home screen grid in
`SmartphoneScreen` hard-codes its list of apps by index; adding an app means
adding a `case` there that opens your new `Screen`, the same way the existing
apps are wired.

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
| Interfaces | PascalCase | `ISaveable`, `PacketBridge` |
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
| Build output | `build/libs/schedulemc-3.9.0-beta.jar` |
| Mod metadata | `src/main/resources/META-INF/mods.toml` |
| Gradle properties | `gradle.properties` |

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-13**.
- Diese Datei wurde im Rahmen der Vollständigkeits-Aktualisierung überarbeitet.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

