# Production Systems Overview

ScheduleMC features **14 complete production systems** -- 8 illegal and 6 legal -- each with unique
mechanics, processing chains, quality tiers, and market integration. All productions share a common
framework built on `AbstractPlantBlock`, `AbstractProcessingBlock`, `ProductionRegistry`, and the
universal `PackagedDrugItem` packaging system.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Generic Production Framework](#generic-production-framework)
3. [Quality System](#quality-system)
4. [Universal Packaging System](#universal-packaging-system)
5. [Illegal Productions (8)](#illegal-productions)
   - [1. Tobacco](#1-tobacco-system)
   - [2. Cannabis](#2-cannabis-system)
   - [3. Coca / Cocaine](#3-cocacocaine-system)
   - [4. Poppy / Opium](#4-poppyopium-system)
   - [5. Methamphetamine](#5-methamphetamine-system)
   - [6. LSD](#6-lsd-system)
   - [7. MDMA / Ecstasy](#7-mdmaecstasy-system)
   - [8. Psilocybin Mushrooms](#8-psilocybin-mushroom-system)
6. [Legal Productions (6)](#legal-productions)
   - [9. Coffee](#9-coffee-system)
   - [10. Wine](#10-wine-system)
   - [11. Cheese](#11-cheese-system)
   - [12. Honey](#12-honey-system)
   - [13. Chocolate](#13-chocolate-system)
   - [14. Beer](#14-beer-system)
7. [Production API](#production-api)
8. [Production Systems Comparison](#production-systems-comparison)

---

## Architecture Overview

```
                    +---------------------------+
                    |    ProductionRegistry      |
                    |  (Singleton, Thread-Safe)  |
                    +---------------------------+
                               |
              +----------------+----------------+
              |                |                |
       +------+------+ +------+------+ +-------+------+
       | PLANT       | | MUSHROOM    | | CHEMICAL     |
       | Category    | | Category    | | Category     |
       +------+------+ +------+------+ +-------+------+
              |                |                |
     Tobacco, Cannabis   Psilocybin      Meth, LSD, MDMA
     Coca, Poppy         Mushrooms
     Coffee, Wine                        EXTRACT Category
                                         +--------------+
                                         | Cocaine,     |
                                         | Heroin       |
                                         +--------------+

                                         PROCESSED Category
                                         +--------------+
                                         | Fermented,   |
                                         | Dried, etc.  |
                                         +--------------+
```

Every production type implements the `ProductionType` interface, which provides:
- `getProductId()` -- unique identifier for the economy system (e.g. `"CANNABIS_INDICA"`)
- `getBasePrice()` -- base price per unit
- `getGrowthTicks()` -- growth/processing duration in ticks
- `getBaseYield()` -- base harvest yield
- `calculatePrice(ProductionQuality, int)` -- static price calculation
- `calculateDynamicPrice(ProductionQuality, int, UUID)` -- dynamic price via EconomyController (UDPS)
- `getItemCategory()` -- economy category for price limits and risk modifiers

Every quality tier implements the `ProductionQuality` interface, which provides:
- `getLevel()` -- numeric tier (0 = worst)
- `getPriceMultiplier()` -- multiplier applied to sale price
- `upgrade()` / `downgrade()` -- tier transitions
- `getColorCode()` -- Minecraft formatting code

---

## Generic Production Framework

### Core Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `AbstractPlantBlock<T>` | `production.blocks` | Base for all plant blocks. 8 growth stages (AGE 0-7), double-block height at stage 4, randomTick-driven growth, parameterized by `ProductionType`. |
| `AbstractProcessingBlock` | `production.blocks` | Base for all processing blocks. EntityBlock with MenuProvider integration, automatic GUI opening, inventory drop on destroy. |
| `AbstractProcessingBlockEntity` | `production.blockentity` | Base block entity for processing. Tick-driven progress, fuel system, item handler capability. |
| `PlantPotBlock` / `PlantPotBlockEntity` | `production.blocks` / `production.blockentity` | Universal plant pot with soil/water capacity, pot-type modifiers. |
| `ProductionConfig` | `production.config` | Data-driven production definition. Builder pattern, processing stage configs, quality tiers. |
| `ProductionRegistry` | `production.config` | Singleton registry with `ConcurrentHashMap`. Runtime registration/unregistration. Thread-safe. |
| `ProductionSize` | `production` | SMALL (cap 6, 1.0x speed), MEDIUM (cap 12, 1.5x speed), BIG (cap 24, 2.0x speed). |
| `PackagedDrugItem` | `production.items` | Universal packaged item. All data in NBT: DrugType, Weight, Quality, Variant, PackageDate, ItemType. |
| `GenericQuality` | `production.core` | Configurable quality system. Factory methods for standard 4-tier, cannabis 5-tier, and custom N-tier systems. |
| `PotType` | `production.core` | TERRACOTTA (3 plants), CERAMIC (4), IRON (5), GOLDEN (5 + quality boost). 33 soil per plant. |
| `DrugType` | `production.core` | Enum: TOBACCO, COCAINE, HEROIN, METH, MUSHROOM, CANNABIS. Used by packaging system. |

### ProductionConfig.Builder

New production types can be defined entirely through configuration at runtime:

```java
ProductionConfig custom = new ProductionConfig.Builder("custom_plant", "Custom Plant")
    .colorCode("&a")
    .basePrice(20.0)
    .growthTicks(3600)
    .baseYield(3)
    .category(ProductionCategory.PLANT)
    .requiresLight(true)
    .minLightLevel(8)
    .requiresWater(true)
    .addProcessingStage("drying", new ProcessingStageConfig(
        "Trocknung", 1200, "fresh_leaf", "dried_leaf", true
    ))
    .addProcessingStage("extraction", new ProcessingStageConfig(
        "Extraktion", 2400, "dried_leaf", "extract", true, "diesel", 100
    ))
    .qualityTiers(GenericQuality.createStandard4TierSystem())
    .build();

ProductionRegistry.getInstance().register(custom);
```

### ProductionConfig.ProductionCategory

| Category | Code | Description | Systems |
|----------|------|-------------|---------|
| `PLANT` | `&a` | Plant-based growth in pots | Tobacco, Cannabis, Coca, Poppy, Coffee, Wine |
| `MUSHROOM` | `&d` | Special growth with climate control | Psilocybin Mushrooms |
| `CHEMICAL` | `&b` | Chemical synthesis in lab equipment | Meth, LSD, MDMA |
| `EXTRACT` | `&e` | Extracted from raw materials | Cocaine, Heroin |
| `PROCESSED` | `&6` | Fermented, dried, aged products | Cheese, Honey, Chocolate, Beer |

### Growth Handler Architecture

Each plant-based production has a dedicated growth handler extending `AbstractPlantGrowthHandler`:

```
AbstractPlantGrowthHandler
    +-- TobaccoGrowthHandler
    +-- CannabisGrowthHandler
    +-- CocaGrowthHandler
    +-- PoppyGrowthHandler
    +-- MushroomGrowthHandler
    +-- PlantGrowthHandler (generic)
```

The `PlantGrowthHandlerFactory` provides the correct handler for each `ProductionType`.

### Plant Serialization

Plant state (type, quality, growth stage, water level, soil level) is persisted via `PlantSerializer` and `TobaccoPlantSerializer`. The `PlantSerializerFactory` routes to the correct serializer for each production type.

---

## Quality System

### Standard 4-Tier System (Tobacco, Coca, Poppy, Meth, MDMA, Crack)

| Tier | Name | Color | Level | Price Multiplier |
|------|------|-------|-------|-----------------|
| 0 | Schlecht | `&c` (red) | 0 | 0.7x |
| 1 | Gut | `&e` (yellow) | 1 | 1.0x |
| 2 | Sehr Gut | `&a` (green) | 2 | 2.0x |
| 3 | Legendaer | `&6&l` (gold bold) | 3 | 4.0x |

Note: Meth Legendaer uses `&b&l` (aqua bold, "Blue Sky" reference). Crack uses `&b&l` as well.

### Cannabis 5-Tier System

| Tier | Name | Color | Level | Price Multiplier |
|------|------|-------|-------|-----------------|
| 0 | Schwag | `&8` (dark gray) | 0 | 0.5x |
| 1 | Mids | `&7` (gray) | 1 | 1.0x |
| 2 | Dank | `&e` (yellow) | 2 | 1.8x |
| 3 | Top Shelf | `&a` (green) | 3 | 3.0x |
| 4 | Exotic | `&d` (pink) | 4 | 5.0x |

### LSD Dosage-Based Quality

| Tier | Name | Micrograms | Level | Price Multiplier |
|------|------|-----------|-------|-----------------|
| 0 | Schlecht | 50 ug | 0 | 0.7x |
| 1 | Gut | 100 ug | 1 | 1.0x |
| 2 | Sehr Gut | 200 ug | 2 | 2.0x |
| 3 | Legendaer | 300 ug | 3 | 4.0x |

LSD dosage is determined by a GUI slider (0-100), interpolating between 50 and 300 ug.

### GenericQuality Custom Tier Builder

For addon developers, fully custom quality systems can be created:

```java
GenericQuality[] tiers = new GenericQuality.Builder(6)
    .names("Trash", "Low", "Mid", "High", "Premium", "Godlike")
    .colorCodes("&8", "&c", "&e", "&a", "&b", "&d")
    .priceMultipliers(0.3, 0.7, 1.0, 1.5, 2.5, 5.0)
    .descriptions("Worthless", "Below average", "Standard", "Good", "Excellent", "Perfect")
    .build();
```

Or using factory interpolation:

```java
GenericQuality[] tiers = GenericQuality.createCustomTierSystem(8, 0.5, 5.0);
// Creates 8 tiers with linearly interpolated multipliers from 0.5x to 5.0x
```

### Quality Factors

Quality is determined by multiple factors depending on the production system:

| Factor | Affects | Details |
|--------|---------|---------|
| Pot Type | All plant-based | Golden pot grants automatic +1 quality on harvest |
| Soil Level | All plant-based | Soil depletion reduces quality |
| Water Level | All plant-based | Dehydration reduces quality |
| Grow Light Tier | All plant-based + mushroom | SMALL: no bonus, MEDIUM: +10% growth, LARGE: +25% growth + 10% quality |
| Fertilizer | Tobacco, Cannabis | Applied during growth for quality boost |
| Processing Time | Fermentation, Curing, Aging | Longer processing = better quality |
| Temperature Performance | Meth | Percent of time in optimal range determines quality |
| Timing Score | MDMA, Crack | Minigame precision determines quality (0.0-1.0 score) |
| Trim Score | Cannabis | Trimming precision determines quality (0.0-1.0 score) |
| Curing Duration | Cannabis | 14+ days = upgrade, 28+ days = double upgrade |
| Slider Precision | LSD | Micro-dosing GUI slider determines dosage tier |
| Biome Temperature | Wine, Beer, Honey, Chocolate | Temperature bonus/penalty based on biome |
| Altitude | Coffee | Y-level proximity to optimal altitude affects quality |

Quality is preserved through the entire processing chain via `ProcessingStageConfig.preservesQuality`.

---

## Universal Packaging System

The `PackagedDrugItem` stores all product information in NBT tags:

```
CompoundTag {
    "DrugType":    String  -- TOBACCO, COCAINE, HEROIN, METH, MUSHROOM, CANNABIS
    "ItemType":    String  -- COCAINE, CRACK, CURED_CANNABIS, TRIMMED_CANNABIS, etc.
    "Weight":      Int     -- 1, 5, 10, or 20 (grams)
    "Quality":     String  -- "TobaccoQuality.LEGENDAER", "MethQuality.SEHR_GUT", etc.
    "Variant":     String  -- "TobaccoType.VIRGINIA", "CocaType.KOLUMBIANISCH", etc.
    "PackageDate": Long    -- Minecraft day number when packaged
}
```

Packageable items (checked by `isPackageableItem()`):
- `FermentedTobaccoLeafItem`
- `CocaineItem`, `CrackRockItem`
- `HeroinItem`
- `MethItem`
- `DriedMushroomItem`
- `TrimmedBudItem`, `CuredBudItem`

### Price Calculation Formula

```
Sale Price = Base Price Per Gram * Weight * Quality Multiplier
```

Static base prices per gram (fallback when no variant available):

| DrugType | Base Price/g |
|----------|-------------|
| TOBACCO | 0.75 |
| COCAINE | 2.75 |
| HEROIN | 3.50 |
| METH | 3.00 |
| MUSHROOM | 3.50 |
| CANNABIS | 2.75 |

When a variant is present, the base price comes from `ProductionType.getBasePrice()`:
- Virginia Tobacco: 0.50/g, Havana: 1.50/g
- Bolivianisch Coca: 2.00/g, Kolumbianisch: 3.50/g
- Cubensis: 3.00/g, Azurescens: 6.00/g

Dynamic pricing delegates to `EconomyController.getSellPrice()` when available.

---

## Illegal Productions

---

### 1. Tobacco System

**The most complex production chain in the mod.**

- **Package:** `de.rolandsw.schedulemc.tobacco`
- **Strains:** 4 (Virginia, Burley, Oriental, Havana)
- **Quality:** Standard 4-tier (Schlecht / Gut / Sehr Gut / Legendaer)
- **Items:** 11 classes
- **Blocks:** 15 classes (including abstract bases)
- **Block Entities:** Drying racks, fermentation barrels, packaging tables (3 sizes each)
- **Menus, Screens, Networking:** Full GUI stack

#### Strain Properties

| Strain | Color | Seed Price | Growth Ticks | Water/Stage | Base Yield | Registry Growth |
|--------|-------|-----------|-------------|-------------|------------|-----------------|
| Virginia | `&e` | 10.0 | 100 | 0.8 | 6 | 3600 ticks |
| Burley | `&6` | 15.0 | 120 | 0.9 | 6 | 4200 ticks |
| Oriental | `&d` | 20.0 | 140 | 1.0 | 6 | 4800 ticks |
| Havana | `&c&l` | 30.0 | 160 | 1.2 | 6 | 6000 ticks |

#### Production Chain

```
+-------------------+
| Tobacco Seeds     |  (4 strains: Virginia, Burley, Oriental, Havana)
| (TobaccoSeedItem) |
+--------+----------+
         |
         v  Plant in pot (Terracotta/Ceramic/Iron/Golden)
+-------------------+
| TobaccoPlantBlock |  AGE 0-7, double-block at AGE 4
| 8 growth stages   |  Requires: soil (SoilBagItem), water (WateringCanItem)
| ~100-160 ticks    |  Boosted by: GrowLightSlabBlock (3 tiers), fertilizer
+--------+----------+
         |
         v  Harvest at AGE 7
+------------------------+
| Fresh Tobacco Leaves   |
| (FreshTobaccoLeafItem) |
+--------+---------------+
         |
         v  Place in drying rack (Small/Medium/Big)
+---------------------------------------------+
| Drying Rack                                 |
| SmallDryingRackBlock  (6 capacity, 1.0x)    |
| MediumDryingRackBlock (12 capacity, 1.5x)   |
| BigDryingRackBlock    (24 capacity, 2.0x)    |
| Processing: 1200 ticks                      |
+--------+------------------------------------+
         |
         v
+------------------------+
| Dried Tobacco Leaves   |
| (DriedTobaccoLeafItem) |
+--------+---------------+
         |
         v  Place in fermentation barrel (Small/Medium/Big)
+-----------------------------------------------+
| Fermentation Barrel                           |
| SmallFermentationBarrelBlock  (6 cap, 1.0x)   |
| MediumFermentationBarrelBlock (12 cap, 1.5x)  |
| BigFermentationBarrelBlock    (24 cap, 2.0x)   |
| Processing: 2400 ticks                        |
+--------+--------------------------------------+
         |
         v
+-----------------------------+
| Fermented Tobacco Leaves    |
| (FermentedTobaccoLeafItem)  |
+--------+--------------------+
         |
         v  Place in packaging table (Small/Medium/Large)
+---------------------------------------------------+
| Packaging Table                                   |
| SmallPackagingTableBlock   --> PackagingBagItem    |
| MediumPackagingTableBlock  --> PackagingBoxItem    |
| LargePackagingTableBlock   --> PackagingJarItem    |
| Processing: 600 ticks                             |
| Output: PackagedDrugItem (NBT: TOBACCO, 1-20g)    |
+--------+------------------------------------------+
         |
         v  Sell to NPCs or via market system
+-------------------+
| Sale / Profit     |
| Price = base * quality multiplier * weight        |
| Virginia: 0.50/g * quality * weight               |
| Havana:   1.50/g * quality * weight               |
+-------------------+
```

#### Equipment

| Equipment | Type | Purpose |
|-----------|------|---------|
| 4 Pot Types | PlantPotBlock | Terracotta (3 plants), Ceramic (4), Iron (5), Golden (5 + quality) |
| SoilBagItem | Item | 33 soil per plant, fills pot soil capacity |
| WateringCanItem | Item | Refills pot water, used at SinkBlock |
| GrowLightSlabBlock | Block | 3 tiers, accelerates growth |
| Drying Racks (3 sizes) | Block | Small/Medium/Big, process fresh -> dried leaves |
| Fermentation Barrels (3 sizes) | Block | Small/Medium/Big, process dried -> fermented leaves |
| Packaging Tables (3 sizes) | Block | Small/Medium/Large, creates packaged products |
| SinkBlock | Block | Water source for WateringCanItem |

#### Processing Stages (Registry Config)

| Stage | Input | Output | Time (ticks) | Quality Preserved |
|-------|-------|--------|-------------|-------------------|
| Trocknung (Drying) | fresh_tobacco_leaf | dried_tobacco_leaf | 1200 | Yes |
| Fermentation | dried_tobacco_leaf | fermented_tobacco_leaf | 2400 | Yes |
| Verpackung (Packaging) | fermented_tobacco_leaf | packaged_tobacco | 600 | Yes |

---

### 2. Cannabis System

**Multiple end products from a single plant -- buds, hash, and oil.**

- **Package:** `de.rolandsw.schedulemc.cannabis`
- **Strains:** 4 (Indica, Sativa, Hybrid, Autoflower)
- **Quality:** Special 5-tier (Schwag / Mids / Dank / Top Shelf / Exotic)
- **Items:** 9 classes
- **Blocks:** 7 classes
- **Block Entities:** 5 (Trocknungsnetz, TrimmStation, CuringGlas, HashPresse, OelExtraktor)

#### Strain Properties

| Strain | Color | Seed Price | Growth Ticks | THC% | CBD% | Yield | Flowering Days | Light Req |
|--------|-------|-----------|-------------|------|------|-------|----------------|-----------|
| Indica | `&5` | 25.0 | 120 (2400 reg) | 22.0 | 1.0 | 6 | 56 | Yes |
| Sativa | `&a` | 30.0 | 160 (3200 reg) | 18.0 | 0.5 | 6 | 70 | Yes |
| Hybrid | `&e` | 35.0 | 140 (2800 reg) | 20.0 | 2.0 | 6 | 63 | Yes |
| Autoflower | `&b` | 20.0 | 70 (1400 reg) | 15.0 | 3.0 | 6 | 42 | **No** |

Autoflower is unique: it does not require light (`requiresLight = false`).

#### Production Chain

```
+-------------------+
| Cannabis Seeds    |  (4 strains: Indica, Sativa, Hybrid, Autoflower)
| (CannabisSeedItem)|
+--------+----------+
         |
         v  Plant and grow (8 stages, 70-160 ticks)
+--------------------+
| CannabisPlantBlock |  AGE 0-7, double-block at AGE 4
| Flowering phase    |  42-70 Minecraft days depending on strain
+--------+-----------+
         |
         v  Harvest at AGE 7
+-------------------+
| Fresh Buds        |
| (FreshBudItem)    |
+--------+----------+
         |
         v  TrocknungsnetzBlock (drying net)
+---------------------+
| Trocknungsnetz      |  Processing: 72000 ticks (registry)
| (Drying Net)        |  Preserves quality
+--------+------------+
         |
         v
+-------------------+
| Dried Buds        |
| (DriedBudItem)    |
+--------+----------+
         |
         v  TrimmStationBlock (trimming station)
+---------------------+
| Trimm Station       |  Produces: TrimmedBudItem + TrimItem (byproduct)
| Quality: fromTrimScore(0.0-1.0)                                    |
| >= 0.90 -> Legendaer, >= 0.70 -> Sehr Gut                          |
| >= 0.40 -> Gut, else Schlecht                                       |
+--------+----------+-+
         |           |
         v           v
+-------------------+ +-------------------+
| Trimmed Buds      | | Trim (byproduct)  |
| (TrimmedBudItem)  | | (TrimItem)        |
+--------+----------+ +--------+----------+
         |                      |
         v                      v
+-------------------+  +-------------------+
| Curing Glas       |  | Hash Presse       |
| (CuringGlasBlock) |  | (HashPresseBlock) |
| 144000 ticks reg  |  | 4 trim -> 1 hash  |
| 14d = +1 quality  |  +--------+----------+
| 28d = +2 quality  |           |
+--------+----------+           v
         |            +-------------------+
         v            | Hash              |
+-------------------+ | (HashItem)        |
| Cured Buds        | +-------------------+
| (CuredBudItem)    |
+--------+----------+
         |
         v  (optional)
+---------------------+
| Oel Extraktor        |  3 buds -> 1 oil (3x concentrated)
| (OelExtraktortBlock) |
+--------+-------------+
         |
         v
+-------------------+
| Cannabis Oil      |
| (CannabisOilItem) |
+-------------------+
```

#### Cannabis Quality via Curing Time

```java
// From CannabisQuality.fromCuringTime()
if (days >= 28 && baseQuality < LEGENDAER) -> upgrade().upgrade()   // +2 tiers
if (days >= 14 && baseQuality < LEGENDAER) -> upgrade()             // +1 tier
```

---

### 3. Coca/Cocaine System

**Chemical extraction with diesel fuel requirement and crack cooking.**

- **Package:** `de.rolandsw.schedulemc.coca`
- **Strains:** 3 (Bolivianisch, Kolumbianisch, Peruanisch)
- **Quality:** Standard 4-tier + separate CrackQuality 4-tier
- **Items:** 7 classes
- **Blocks:** 9 classes (3 extraction vat sizes + 3 refinery sizes + CrackKocher + plant)
- **Block Entities:** 10 (abstract bases + Small/Medium/Big for vats and refineries + CrackKocher)

#### Strain Properties

| Strain | Color | Seed Price | Growth Ticks | Water/Stage | Base Yield |
|--------|-------|-----------|-------------|-------------|------------|
| Bolivianisch | `&a` | 20.0 | 100 | 0.8 | 6 |
| Kolumbianisch | `&2` | 35.0 | 140 | 1.0 | 6 |
| Peruanisch | `&6` | 27.5 | 120 | 0.9 | 6 |

#### Production Chain

```
+-------------------+
| Coca Seeds        |  (3 strains)
| (CocaSeedItem)    |
+--------+----------+
         |
         v  Plant and grow
+-------------------+
| CocaPlantBlock    |  AGE 0-7
+--------+----------+
         |
         v  Harvest
+------------------------+
| Fresh Coca Leaves      |
| (FreshCocaLeafItem)    |
+--------+---------------+
         |
         v  Extraction Vat (Small/Medium/Big) + Diesel fuel
+--------------------------------------------------+
| Extraction Vat                                   |
| SmallExtractionVatBlock   (6 cap, 1.0x speed)    |
| MediumExtractionVatBlock  (12 cap, 1.5x speed)   |
| BigExtractionVatBlock     (24 cap, 2.0x speed)    |
| Processing: 2400 ticks                           |
| Requires: "diesel" resource, 100 per batch       |
+--------+-----------------------------------------+
         |
         v
+-------------------+
| Coca Paste        |
| (CocaPasteItem)   |
+--------+----------+
         |
         v  Refinery (Small/Medium/Big) -- GLOWING when active!
+----------------------------------------------+
| Refinery                                     |
| SmallRefineryBlock   (6 cap, 1.0x speed)      |
| MediumRefineryBlock  (12 cap, 1.5x speed)     |
| BigRefineryBlock     (24 cap, 2.0x speed)      |
| Processing: 3600 ticks                       |
| Requires: "fuel" resource, 50 per batch      |
| Visual: Emits light when processing          |
+--------+-------------------------------------+
         |
         v
+-------------------+
| Cocaine           |
| (CocaineItem)     |
+--------+----------+
         |
         v  (optional) Crack Kocher + Backpulver (baking soda)
+---------------------------------------------------+
| CrackKocherBlock                                  |
| Input: Cocaine + BackpulverItem                   |
| Output: CrackRockItem                             |
| Quality: CrackQuality.fromTimingScore(0.0 - 1.0)  |
+--------+------------------------------------------+
         |
         v
+-------------------+
| Crack Rocks       |
| (CrackRockItem)   |
+-------------------+
```

#### CrackQuality Tiers

| Tier | Level | Multiplier |
|------|-------|-----------|
| SCHLECHT | 0 | 0.7x |
| GUT | 1 | 1.0x |
| SEHR_GUT | 2 | 1.5x |
| LEGENDAER | 3 | 2.5x |

---

### 4. Poppy/Opium System

**Multi-step refinement from raw opium to morphine to heroin.**

- **Package:** `de.rolandsw.schedulemc.poppy`
- **Strains:** 3 (Afghanisch, Tuerkisch, Indisch)
- **Quality:** Standard 4-tier
- **Items:** 7 classes (including ScoringKnifeItem tool)
- **Blocks:** 6 classes
- **Block Entities:** 5 (Ritzmaschine, OpiumPresse, Kochstation, HeroinRaffinerie)

#### Strain Properties

| Strain | Color | Seed Price | Growth Ticks | Water/Stage | Base Yield | Potency Mult |
|--------|-------|-----------|-------------|-------------|------------|-------------|
| Afghanisch | `&4` | 50.0 | 160 | 1.2 | 6 | 1.5x |
| Tuerkisch | `&6` | 35.0 | 120 | 1.0 | 6 | 1.0x |
| Indisch | `&5` | 20.0 | 80 | 0.8 | 6 | 0.8x |

Afghanisch has the highest potency multiplier (1.5x) but slowest growth and highest seed cost.
Price formula: `seedPrice * 3.5 * potencyMultiplier * quality.getPriceMultiplier() * amount`.

#### Production Chain

```
+-------------------+
| Poppy Seeds       |  (3 strains)
| (PoppySeedItem)   |
+--------+----------+
         |
         v  Plant and grow
+-------------------+
| PoppyPlantBlock   |  AGE 0-7
+--------+----------+
         |
         v  Harvest
+-------------------+
| Poppy Pods        |
| (PoppyPodItem)    |
+--------+----------+
         |
         v  Ritzmaschine (scoring machine) OR ScoringKnifeItem (manual)
+---------------------+
| RitzmaschineBlock   |  Automated scoring
| Processing: 1200 ticks (registry)
+--------+------------+
         |
         v
+-------------------+
| Raw Opium         |
| (RawOpiumItem)    |
+--------+----------+
         |
         v  Opium Presse (press)
+---------------------+
| OpiumPresseBlock    |  Processing: 1800 ticks (registry)
+--------+------------+
         |
         v
+-------------------+
| Pressed Opium     |
| (opium_block)     |
+--------+----------+
         |
         v  Kochstation (cooking station) -- requires water
+---------------------+
| KochstationBlock    |  Processing: 2400 ticks
|                     |  Requires: "water" resource, 1 per batch
|                     |  Temperature: 185 C
+--------+------------+
         |
         v
+-------------------+
| Morphine          |
| (MorphineItem)    |
+--------+----------+
         |
         v  Heroin Raffinerie (refinery)
+--------------------------+
| HeroinRaffinerieBlock    |  Processing: 3600 ticks
+---------+----------------+
          |
          v
+-------------------+
| Heroin            |
| (HeroinItem)      |
+-------------------+
```

---

### 5. Methamphetamine System

**Chemical synthesis with explosion risk -- highest danger production.**

- **Package:** `de.rolandsw.schedulemc.meth`
- **Strains:** None (chemical synthesis)
- **Quality:** Standard 4-tier (LEGENDAER = "Blue Sky" with `&b&l` aqua bold)
- **Items:** 9 classes (4 chemicals + 4 product stages + registry)
- **Blocks:** 4 classes
- **Block Entities:** 5 (Mixer, Reduktionskessel, Kristallisator, VakuumTrockner)

#### Chemical Inputs

| Chemical | Class | Purpose |
|----------|-------|---------|
| Ephedrin | `EphedrinItem` | Primary precursor |
| Pseudoephedrin | `PseudoephedrinItem` | Alternative precursor |
| Roter Phosphor | `RoterPhosphorItem` | Reduction agent |
| Jod (Iodine) | `JodItem` | Catalyst |

#### Production Chain

```
+-------------------------------------+
| Precursor Chemicals                 |
| Ephedrin OR Pseudoephedrin          |
| + Roter Phosphor + Jod             |
+--------+----------------------------+
         |
         v  Chemie Mixer (chemical mixer)
+---------------------+
| ChemieMixerBlock    |
+--------+------------+
         |
         v
+-------------------+
| Meth Paste        |
| (MethPasteItem)   |
+--------+----------+
         |
         v  Reduktionskessel (reduction vessel)
+---------------------------------------------------+
| ReduktionskesselBlock                             |
|                                                   |
|  *** WARNING: EXPLOSION RISK! ***                 |
|  Do not interrupt mid-process!                    |
|                                                   |
|  Quality: fromTemperaturePerformance(0.0 - 1.0)   |
|  >= 0.95 -> LEGENDAER (Blue Sky)                  |
|  >= 0.80 -> SEHR_GUT                              |
|  >= 0.60 -> GUT                                   |
|  < 0.60  -> SCHLECHT                              |
|                                                   |
|  Emits light (level 4) when active                |
+--------+------------------------------------------+
         |
         v
+-------------------+
| Roh Meth          |
| (RohMethItem)     |
+--------+----------+
         |
         v  Kristallisator (crystallizer)
+---------------------+
| KristallisatorBlock |
+--------+------------+
         |
         v
+---------------------+
| Kristall Meth       |
| (KristallMethItem)  |
+--------+------------+
         |
         v  Vakuum Trockner (vacuum dryer)
+-----------------------+
| VakuumTrocknerBlock   |
+---------+-------------+
          |
          v
+-------------------+
| Crystal Meth      |  Final product
| (MethItem)        |  95-99% purity
+-------------------+
```

#### Meth Quality (Temperature Performance)

The `MethQuality.fromTemperaturePerformance()` method maps the percentage of time spent in the
optimal temperature range to quality:

```
optimalTimePercent >= 0.95  -->  LEGENDAER (Blue Sky, 5.0x price)
optimalTimePercent >= 0.80  -->  SEHR_GUT  (2.0x price)
optimalTimePercent >= 0.60  -->  GUT       (1.0x price)
optimalTimePercent <  0.60  -->  SCHLECHT  (0.7x price)
```

Note: Meth LEGENDAER has a 5.0x multiplier (vs standard 4.0x), making it the highest-multiplier
tier of any standard-quality production.

---

### 6. LSD System

**The most scientific production -- precision laboratory synthesis with GUI controls.**

- **Package:** `de.rolandsw.schedulemc.lsd`
- **Strains:** None (laboratory synthesis)
- **Quality:** Dosage-based 4-tier (50-300 ug)
- **Items:** 7 classes
- **Blocks:** 4 classes
- **Block Entities:** 5 (FermentationsTank, DestillationsApparat, MikroDosierer, PerforationsPresse)
- **Special:** MikroDosierer has a slider-based dosing GUI, 8 BlotterDesign variants

#### Blotter Designs

| Design | Color | Symbol |
|--------|-------|--------|
| Totenkopf (Skull) | `&8` | Skull |
| Sonne (Sun) | `&e` | Sun |
| Auge (Eye) | `&5` | Bullseye |
| Pilz (Mushroom) | `&c` | Mushroom |
| Fahrrad (Bicycle) | `&b` | Gear (Bicycle Day reference) |
| Mandala | `&d` | Flower |
| Blitz (Lightning) | `&6` | Lightning bolt |
| Stern (Star) | `&f` | Star |

#### Production Chain

```
+-------------------+
| Mutterkorn        |  (Ergot fungus)
| (MutterkornItem)  |
+--------+----------+
         |
         v  Fermentations Tank
+------------------------------+
| FermentationsTankBlock       |  Processing: 6000 ticks (registry)
| Temperature: 25 C           |
+--------+---------------------+
         |
         v
+-----------------------+
| Ergot Kultur          |
| (ErgotKulturItem)     |
+--------+--------------+
         |
         v  Destillations Apparat -- GLOWING when active!
+------------------------------------+
| DestillationsApparatBlock          |  Processing: 4800 ticks (registry)
| Visual: Emits light when active    |
+--------+---------------------------+
         |
         v
+------------------------+
| Lysergsaeure           |  (Lysergic Acid)
| (LysergsaeureItem)     |
+--------+---------------+
         |
         v  Chemical synthesis (further processing)
+-----------------------+
| LSD Loesung           |  (LSD Solution, 100 ug/ml)
| (LSDLoesungItem)      |
+--------+--------------+
         |
         v  Mikro Dosierer -- HAS GUI with dosing slider!
+------------------------------------------------+
| MikroDosiererBlock                             |
|                                                |
| GUI Slider: 0 -------- 50 -------- 100        |
| Dosage:    50ug       175ug        300ug       |
|                                                |
| Formula: 50 + (slider/100) * 250 = micrograms  |
|                                                |
| Quality mapping:                               |
|   Slider >= 75 -> LEGENDAER (300ug)            |
|   Slider >= 50 -> SEHR_GUT  (200ug)            |
|   Slider >= 25 -> GUT       (100ug)            |
|   Slider <  25 -> SCHLECHT  (50ug)             |
+--------+---------------------------------------+
         |
         v  + Blotter Papier (BlotterPapierItem)
+-----------------------+
| Soaked Blotter Paper  |
+--------+--------------+
         |
         v  Perforations Presse
+----------------------------+
| PerforationsPresseBlock    |  Processing: 1200 ticks (registry)
+--------+-------------------+
         |
         v
+-----------------------+
| LSD Blotter Tabs      |  100 tabs per sheet
| (BlotterItem)         |  BlotterDesign: 8 variants
+-----------------------+
```

---

### 7. MDMA/Ecstasy System

**Timing-based pill pressing minigame with custom pill colors.**

- **Package:** `de.rolandsw.schedulemc.mdma`
- **Strains:** None (chemical synthesis)
- **Quality:** Standard 4-tier
- **Items:** 7 classes (including Bindemittel + Farbstoff materials)
- **Blocks:** 3 classes
- **Block Entities:** 4 (ReaktionsKessel, TrocknungsOfen, PillenPresse)
- **Special:** PillenPresse has a timing-based minigame, 8 PillColor options, PillDesign variants

#### Pill Colors

| Color | Code | Hex |
|-------|------|-----|
| Pink | `&d` | #FFAACC |
| Blau (Blue) | `&9` | #5555FF |
| Gruen (Green) | `&a` | #55FF55 |
| Orange | `&6` | #FFAA00 |
| Gelb (Yellow) | `&e` | #FFFF55 |
| Weiss (White) | `&f` | #FFFFFF |
| Rot (Red) | `&c` | #FF5555 |
| Lila (Purple) | `&5` | #AA55AA |

#### Production Chain

```
+-------------------+
| Safrol            |  (Precursor chemical, extracted from sassafras)
| (SafrolItem)      |
+--------+----------+
         |
         v  Reaktions Kessel (reaction vessel) -- GLOWING when active!
+------------------------------------+
| ReaktionsKesselBlock               |
| Processing: 4800 ticks (registry)   |
| Visual: Emits light when active    |
+--------+---------------------------+
         |
         v
+-------------------+
| MDMA Base         |
| (MDMABaseItem)    |
+--------+----------+
         |
         v  Trocknungs Ofen (drying oven)
+---------------------+
| TrocknungsOfenBlock |
+--------+------------+
         |
         v
+-----------------------+
| MDMA Kristall         |
| (MDMAKristallItem)    |
+--------+--------------+
         |
         v  Pillen Presse + Bindemittel (binder) + Farbstoff (dye)
+----------------------------------------------------------+
| PillenPresseBlock                                        |
|                                                          |
| *** TIMING MINIGAME ***                                  |
|                                                          |
| Quality from timingScore (0.0 - 1.0):                    |
|   >= 0.95 -> LEGENDAER  (perfect pills)                 |
|   >= 0.80 -> SEHR_GUT   (great pills)                   |
|   >= 0.50 -> GUT        (decent pills)                  |
|   <  0.50 -> SCHLECHT   (crumbly pills)                 |
|                                                          |
| Materials: BindemittelItem + FarbstoffItem (8 colors)    |
| Processing: 1200 ticks (registry)                        |
+--------+-------------------------------------------------+
         |
         v
+-----------------------+
| Ecstasy Pills         |  Custom color from PillColor enum
| (EcstasyPillItem)     |  PillDesign variant
+-----------------------+
```

---

### 8. Psilocybin Mushroom System

**Climate-controlled cultivation with spore syringes and mist bags.**

- **Package:** `de.rolandsw.schedulemc.mushroom`
- **Strains:** 3 (Cubensis, Azurescens, Mexicana)
- **Quality:** Standard 4-tier
- **Items:** 5 classes
- **Blocks:** 3 classes (Klimalampe, Wassertank, MushroomBlocks registry)
- **Block Entities:** 3 (KlimalampeBlockEntity, WassertankBlockEntity)
- **Special:** Flush system (multiple harvests), light level requirements for incubation vs fruiting

#### Strain Properties

| Strain | Full Name | Color | Spore Price | Growth Ticks | Water/Stage | Yield | Potency | Max Flushes | Max Light (Incub) | Max Light (Fruit) |
|--------|-----------|-------|-------------|-------------|-------------|-------|---------|-------------|-------------------|-------------------|
| Cubensis | Psilocybe Cubensis | `&6` | 30.0 | 100 (2400 reg) | 1.0 | 6 | 1.0x | 4 | 4 | 7 |
| Azurescens | Psilocybe Azurescens | `&9` | 60.0 | 180 | 1.5 | 6 | 2.0x | 3 | 3 | 5 |
| Mexicana | Psilocybe Mexicana | `&e` | 20.0 | 60 | 0.7 | 6 | 0.6x | 5 | 5 | 8 |

Key differences:
- **Azurescens:** Highest potency (2.0x), slowest growth, strictest light requirements, fewest flushes
- **Mexicana:** Fastest growth, most tolerant light requirements, most flushes, lowest potency
- **Cubensis:** Balanced middle ground

#### Klimalampe (Climate Lamp) Tiers

| Tier | Color | Automatic | Growth Bonus | Quality Bonus |
|------|-------|-----------|-------------|--------------|
| SMALL | `&7` | No | 0% | 0% |
| MEDIUM | `&e` | Yes | +10% | 0% |
| LARGE | `&6` | Yes | +25% | +10% |

#### Production Chain

```
+----------------------------+
| Spore Syringes             |  (3 strains)
| (SporeSyringeItem)         |
+--------+-------------------+
         |
         v  Inoculate mist bags
+-------------------+
| Mist Bags         |  (MistBagItem -- Small/Medium/Large sizes)
| Inoculated with   |
| strain spores     |
+--------+----------+
         |
         v  Place near Klimalampe + Wassertank for growing
+-----------------------------------------------------+
| Growing Environment                                 |
|                                                     |
| KlimalampeBlock (3 tiers: Small/Medium/Large)        |
|   - Controls light level for incubation/fruiting    |
|   - Growth and quality bonuses at higher tiers      |
|                                                     |
| WassertankBlock (water supply)                       |
|   - Required for humidity                           |
|                                                     |
| INCUBATION: Requires DARKNESS (light < maxLight)     |
| FRUITING: Requires moderate light (strain-dependent) |
+--------+--------------------------------------------+
         |
         v  Harvest (can flush multiple times!)
+-----------------------+
| Fresh Mushrooms       |
| (FreshMushroomItem)   |
| Up to 3-5 flushes     |
| depending on strain   |
+--------+--------------+
         |
         v  Natural drying
+-----------------------+
| Dried Mushrooms       |  Final product
| (DriedMushroomItem)   |  Processing: 1200 ticks (registry)
+-----------------------+
```

---

## Legal Productions

The 6 legal production systems follow similar architectural patterns to the illegal systems,
each with their own items, blocks, block entities, menus, screens, and networking.

---

### 9. Coffee System

**Altitude-sensitive cultivation with multiple roast levels and grind sizes.**

- **Package:** `de.rolandsw.schedulemc.coffee`
- **Types:** 4 (Arabica, Robusta, Liberica, Excelsa)
- **Items:** 9 classes
- **Blocks:** 11 classes (3 drying tray sizes + 3 roaster sizes + grinder + packaging + plant + wet processing)
- **Block Entities:** 12
- **Menus/Screens:** 10 each
- **Networking:** CoffeeNetworking, GrindSizePacket, RoasterLevelPacket, CoffeePackageRequestPacket
- **Special:** CoffeeRoastLevel, CoffeeGrindSize, CoffeeProcessingMethod enums; altitude bonus system

#### Type Properties

| Type | Color | Seedling Price | Growth Ticks | Water/Stage | Yield | Optimal Altitude (Y) |
|------|-------|---------------|-------------|-------------|-------|---------------------|
| Arabica | `&e` | 12.0 | 140 | 0.7 | 8 | 1800 |
| Robusta | `&6` | 18.0 | 120 | 0.9 | 10 | 1600 |
| Liberica | `&d` | 25.0 | 160 | 0.8 | 6 | 2000 |
| Excelsa | `&5&l` | 35.0 | 180 | 1.0 | 7 | 2200 |

Altitude bonus: within 10 blocks of optimal Y = +20% quality, 30 = normal, 50 = -20%, >50 = -40%.

#### Production Chain

```
CoffeeSeedlingItem --> CoffeePlantBlock (AGE 0-7)
    --> CoffeeCherryItem
    --> WetProcessingStationBlock
    --> GreenCoffeeBeanItem
    --> DryingTray (Small/Medium/Large)
    --> SmallCoffeeRoasterBlock / MediumCoffeeRoasterBlock / LargeCoffeeRoasterBlock
    --> RoastedCoffeeBeanItem (CoffeeRoastLevel)
    --> CoffeeGrinderBlock (CoffeeGrindSize)
    --> GroundCoffeeItem
    --> CoffeePackagingTableBlock
    --> PackagedCoffeeItem / BrewedCoffeeItem / EspressoItem
```

---

### 10. Wine System

**Grape cultivation with terroir system, pressing, fermentation, and barrel aging.**

- **Package:** `de.rolandsw.schedulemc.wine`
- **Types:** 4 -- 2 white (Riesling, Chardonnay), 2 red (Spaetburgunder, Merlot)
- **Items:** 5 classes
- **Blocks:** 14 classes (3 wine press sizes + 3 fermentation tank sizes + 3 aging barrel sizes + crushing + bottling + grapevine + pot)
- **Block Entities:** 15
- **Menus/Screens:** 12/11 each
- **Networking:** WineNetworking, ProcessingMethodPacket
- **Special:** WineAgeLevel, WineProcessingMethod, temperature bonus from biome

#### Type Properties

| Type | Color | Price/Liter | Growth Days | Quality Factor | Yield/Plant | Optimal Temp (C) | Category |
|------|-------|------------|------------|----------------|-------------|------------------|----------|
| Riesling | `&e` | 15.0 | 120 | 0.8 | 10 | 18 | White |
| Chardonnay | `&6` | 22.0 | 110 | 0.9 | 8 | 20 | White |
| Spaetburgunder | `&c` | 28.0 | 140 | 0.85 | 9 | 16 | Red |
| Merlot | `&4` | 35.0 | 130 | 0.95 | 7 | 22 | Red |

#### Production Chain

```
GrapeSeedlingItem --> GrapevineBlock / GrapevinePotBlock
    --> GrapeItem
    --> CrushingStationBlock
    --> WinePress (Small/Medium/Large)
    --> FermentationTank (Small/Medium/Large)
    --> AgingBarrel (Small/Medium/Large, WineAgeLevel)
    --> WineBottlingStationBlock
    --> WineBottleItem / WineGlassItem
```

---

### 11. Cheese System

**Milk pasteurization, curdling, pressing, and cave aging.**

- **Package:** `de.rolandsw.schedulemc.cheese`
- **Types:** 4 (Gouda, Emmental, Camembert, Parmesan)
- **Items:** 6 classes
- **Blocks:** 10 classes (3 cheese press sizes + 3 aging cave sizes + curdling vat + pasteurization + packaging)
- **Block Entities:** 12
- **Menus/Screens:** 10/9 each
- **Networking:** CheeseNetworking, ProcessingMethodPacket
- **Special:** CheeseAgeLevel, CheeseProcessingMethod

#### Type Properties

| Type | Color | Price/kg | Aging Days | Quality Factor |
|------|-------|---------|-----------|---------------|
| Gouda | `&e` | 15.0 | 30 | 1.0 |
| Emmental | `&6` | 22.0 | 35 | 1.1 |
| Camembert | `&f` | 28.0 | 25 | 1.2 |
| Parmesan | `&c` | 35.0 | 40 | 1.3 |

#### Production Chain

```
MilkBucketItem + RennetItem
    --> PasteurizationStationBlock
    --> CurdlingVatBlock
    --> CheeseCurdItem
    --> CheesePress (Small/Medium/Large)
    --> AgingCave (Small/Medium/Large, CheeseAgeLevel)
    --> CheeseWheelItem
    --> PackagingStationBlock
    --> CheeseWedgeItem
```

---

### 12. Honey System

**Beekeeping with multiple hive types, extraction, and aging.**

- **Package:** `de.rolandsw.schedulemc.honey`
- **Types:** 4 (Acacia, Wildflower, Forest, Manuka)
- **Items:** 2 classes (HoneyJarItem + registry)
- **Blocks:** 13 classes (3 aging chamber sizes + beehive + advanced beehive + apiary + extractor + centrifugal + filtering + creaming + bottling + processing)
- **Block Entities:** 14
- **Menus/Screens:** 13/12 each
- **Networking:** HoneyNetworking, ProcessingMethodPacket
- **Special:** HoneyAgeLevel, HoneyProcessingMethod, temperature bonus from biome (bees prefer 15-25 C)

#### Type Properties

| Type | Color | Price/kg | Aging Days | Quality Factor |
|------|-------|---------|-----------|---------------|
| Acacia | `&e` | 12.0 | 30 | 0.9 |
| Wildflower | `&6` | 15.0 | 60 | 1.0 |
| Forest | `&c` | 20.0 | 90 | 1.2 |
| Manuka | `&d` | 35.0 | 120 | 1.5 |

#### Production Chain

```
BeehiveBlock / AdvancedBeehiveBlock / ApiaryBlock
    --> HoneyExtractorBlock
    --> CentrifugalExtractorBlock
    --> FilteringStationBlock
    --> ProcessingStationBlock (HoneyProcessingMethod)
    --> CreamingStationBlock (optional, for creamed honey)
    --> AgingChamber (Small/Medium/Large, HoneyAgeLevel)
    --> BottlingStationBlock
    --> HoneyJarItem
```

---

### 13. Chocolate System

**Bean-to-bar chocolate making with roasting, conching, tempering, and molding.**

- **Package:** `de.rolandsw.schedulemc.chocolate`
- **Types:** 4 (Dark 70%, Milk 30%, White 0%, Ruby 47%)
- **Items:** 2 classes (ChocolateBarItem + registry)
- **Blocks:** 15 classes (3 conching machine sizes + 3 molding station sizes + roasting + winnowing + grinding + pressing + tempering + cooling + enrobing + wrapping)
- **Block Entities:** 17
- **Menus/Screens:** 15/14 each
- **Networking:** ChocolateNetworking, ProcessingMethodPacket
- **Special:** ChocolateAgeLevel, ChocolateProcessingMethod, temperature bonus (tropical 20-30 C = +30%)

#### Type Properties

| Type | Color | Price/kg | Aging Days | Quality Factor | Cocoa % |
|------|-------|---------|-----------|---------------|---------|
| Dark | `&6` | 20.0 | 60 | 1.3 | 70% |
| Milk | `&e` | 15.0 | 30 | 1.0 | 30% |
| White | `&f` | 12.0 | 20 | 0.9 | 0% |
| Ruby | `&d` | 30.0 | 40 | 1.5 | 47% |

#### Production Chain (Most steps of any legal system)

```
Cacao Beans
    --> RoastingStationBlock
    --> WinnowingMachineBlock
    --> GrindingMillBlock
    --> PressingStationBlock (cocoa butter/powder separation)
    --> ConchingMachine (Small/Medium/Large)
    --> TemperingStationBlock
    --> MoldingStation (Small/Medium/Large)
    --> CoolingTunnelBlock
    --> EnrobingMachineBlock (optional, for coated items)
    --> WrappingStationBlock
    --> ChocolateBarItem
```

---

### 14. Beer System

**Full brewing process: malting, mashing, boiling, fermenting, conditioning, and bottling.**

- **Package:** `de.rolandsw.schedulemc.beer`
- **Types:** 4 (Pilsner, Weizen, Ale, Stout)
- **Items:** 2 classes (BeerBottleItem + registry)
- **Blocks:** 13 classes (3 brew kettle sizes + 3 fermentation tank sizes + 3 conditioning tank sizes + malting + mash tun + bottling)
- **Block Entities:** 16
- **Menus/Screens:** 13/12 each
- **Networking:** BeerNetworking, ProcessingMethodPacket
- **Special:** BeerAgeLevel, BeerProcessingMethod, BeerQuality, alcohol %, IBU, original gravity

#### Type Properties

| Type | Color | Price/Liter | Aging Days | Quality Factor | Alcohol % | IBU | Gravity (Plato) |
|------|-------|------------|-----------|---------------|-----------|-----|----------------|
| Pilsner | `&e` | 8.0 | 30 | 1.0 | 4.8% | 35 | 11.5 |
| Weizen | `&6` | 10.0 | 35 | 1.1 | 5.4% | 15 | 12.5 |
| Ale | `&c` | 12.0 | 40 | 1.2 | 6.5% | 40 | 13.5 |
| Stout | `&8` | 15.0 | 50 | 1.3 | 7.2% | 45 | 16.0 |

Temperature bonus: Beer prefers cool storage (8-15 C = +30%, 5-20 C = +15%, 0-25 C = normal).

#### Production Chain

```
Grain / Hops
    --> MaltingStationBlock
    --> MashTunBlock (mashing)
    --> BrewKettle (Small/Medium/Large) -- boiling
    --> FermentationTank (Small/Medium/Large) -- primary fermentation
    --> ConditioningTank (Small/Medium/Large, BeerAgeLevel) -- secondary
    --> BottlingStationBlock
    --> BeerBottleItem (BeerType, BeerQuality, BeerAgeLevel)
```

---

## Production API

The `IProductionAPI` interface (package `de.rolandsw.schedulemc.api.production`) provides external
mod integration with the production system, implemented by `ProductionAPIImpl`.

### API Methods

```java
public interface IProductionAPI {

    // --- Lookup ---
    ProductionConfig getProduction(String productionId);
    boolean hasProduction(String productionId);
    Collection<ProductionConfig> getAllProductions();
    List<ProductionConfig> getProductionsByCategory(ProductionCategory category);
    int getProductionCount();

    // --- Registration ---
    void registerProduction(ProductionConfig config);
    boolean unregisterProduction(String productionId);

    // --- Runtime Control ---
    boolean startProduction(BlockPos position, String productionId);
    boolean stopProduction(BlockPos position);
    double getProductionProgress(BlockPos position);  // 0.0 - 100.0, -1 if inactive

    // --- Extended API (v3.2.0) ---
    Set<String> getAllProductionIds();
    ProductionCategory[] getCategories();
    boolean setProductionBasePrice(String productionId, double newBasePrice);
    String getProductionStatistics();
}
```

### Usage Example

```java
IProductionAPI api = ScheduleMCAPI.getProductionAPI();

// List all productions
Collection<ProductionConfig> all = api.getAllProductions();

// Filter by category
List<ProductionConfig> plants = api.getProductionsByCategory(ProductionCategory.PLANT);

// Lookup specific production
ProductionConfig indica = api.getProduction("cannabis_indica");

// Register a custom production
ProductionConfig custom = new ProductionConfig.Builder("my_herb", "My Custom Herb")
    .basePrice(15.0)
    .growthTicks(2400)
    .category(ProductionCategory.PLANT)
    .addProcessingStage("drying", new ProcessingStageConfig(
        "Drying", 1200, "fresh_herb", "dried_herb", true
    ))
    .build();
api.registerProduction(custom);

// Modify price at runtime
api.setProductionBasePrice("cannabis_indica", 30.0);

// Unregister
api.unregisterProduction("my_herb");
```

### Thread Safety

The `ProductionRegistry` uses `ConcurrentHashMap` internally. All `IProductionAPI` methods are
thread-safe. Production lookups are O(1).

### Default Registered Productions

The registry auto-registers these productions on initialization:

| ID | Display Name | Category | Base Price | Growth Ticks |
|----|-------------|----------|-----------|-------------|
| `tobacco_virginia` | Virginia Tabak | PLANT | 15.0 | 3600 |
| `tobacco_burley` | Burley Tabak | PLANT | 18.0 | 4200 |
| `tobacco_oriental` | Oriental Tabak | PLANT | 25.0 | 4800 |
| `tobacco_havana` | Havana Tabak | PLANT | 35.0 | 6000 |
| `cannabis_indica` | Indica | PLANT | 25.0 | 2400 |
| `cannabis_sativa` | Sativa | PLANT | 30.0 | 3200 |
| `cannabis_hybrid` | Hybrid | PLANT | 35.0 | 2800 |
| `cannabis_autoflower` | Autoflower | PLANT | 20.0 | 1400 |
| `coca_colombian` | Colombian Coca | PLANT | 30.0 | 3000 |
| `coca_bolivian` | Bolivian Coca | PLANT | 35.0 | 3600 |
| `poppy_afghan` | Afghan Poppy | PLANT | 40.0 | 4000 |
| `poppy_turkish` | Turkish Poppy | PLANT | 45.0 | 4800 |
| `poppy_indian` | Indian Poppy | PLANT | 38.0 | 3600 |
| `mushroom_cubensis` | Psilocybe Cubensis | MUSHROOM | 50.0 | 2400 |
| `mushroom_golden_teacher` | Golden Teacher | MUSHROOM | 60.0 | 2800 |
| `mdma_standard` | MDMA | CHEMICAL | 80.0 | 0 |
| `lsd_standard` | LSD | CHEMICAL | 100.0 | 0 |
| `meth_standard` | Methamphetamine | CHEMICAL | 120.0 | 0 |

---

## Production Systems Comparison

### Illegal Productions

| System | Strains | Steps | Item Classes | Block Classes | Category | Unique Mechanic |
|--------|---------|-------|-------------|--------------|----------|----------------|
| Tobacco | 4 | 6 | 11 | 15 | PLANT | 3 sizes for every processing step |
| Cannabis | 4 | 8 | 9 | 7 | PLANT | 5-tier quality, multiple end products |
| Coca/Cocaine | 3 | 5 | 7 | 9 | PLANT/EXTRACT | Diesel requirement, glowing refineries |
| Poppy/Opium | 3 | 6 | 7 | 6 | PLANT/EXTRACT | Potency multiplier per strain |
| Methamphetamine | 0 | 4 | 9 | 4 | CHEMICAL | Explosion risk, temperature monitoring |
| LSD | 0 | 6 | 7 | 4 | CHEMICAL | Dosing GUI slider, 8 blotter designs |
| MDMA/Ecstasy | 0 | 4 | 7 | 3 | CHEMICAL | Timing minigame, 8 pill colors |
| Mushrooms | 3 | 4 | 5 | 3 | MUSHROOM | Climate control, flush system |

### Legal Productions

| System | Types | Block Classes | Item Classes | Menu Classes | Screen Classes | Unique Mechanic |
|--------|-------|--------------|-------------|-------------|---------------|----------------|
| Coffee | 4 | 11 | 9 | 10 | 9 | Altitude bonus, roast levels, grind sizes |
| Wine | 4 | 14 | 5 | 12 | 11 | Terroir system, biome temperature |
| Cheese | 4 | 10 | 6 | 10 | 9 | Cave aging, pasteurization |
| Honey | 4 | 13 | 2 | 13 | 12 | 3 hive types, centrifugal extraction |
| Chocolate | 4 | 15 | 2 | 15 | 14 | 10+ processing steps, cocoa percentage |
| Beer | 4 | 13 | 2 | 13 | 12 | IBU, gravity, alcohol %, conditioning |

### Total Asset Count

| Category | Blocks | Items | Block Entities | Menus | Screens |
|----------|--------|-------|---------------|-------|---------|
| Illegal (8 systems) | ~51 | ~62 | ~47 | varies | varies |
| Legal (6 systems) | ~76 | ~26 | ~86 | ~73 | ~67 |
| Production Framework | 3 | 1 | 3 | -- | -- |
| **Total** | **~130** | **~89** | **~136** | **~73+** | **~67+** |

---

## Source Code Reference

| System | Source Path |
|--------|-----------|
| Production Framework | `src/main/java/de/rolandsw/schedulemc/production/` |
| Production API | `src/main/java/de/rolandsw/schedulemc/api/production/IProductionAPI.java` |
| Production API Impl | `src/main/java/de/rolandsw/schedulemc/api/impl/ProductionAPIImpl.java` |
| Tobacco | `src/main/java/de/rolandsw/schedulemc/tobacco/` |
| Cannabis | `src/main/java/de/rolandsw/schedulemc/cannabis/` |
| Coca/Cocaine | `src/main/java/de/rolandsw/schedulemc/coca/` |
| Poppy/Opium | `src/main/java/de/rolandsw/schedulemc/poppy/` |
| Methamphetamine | `src/main/java/de/rolandsw/schedulemc/meth/` |
| LSD | `src/main/java/de/rolandsw/schedulemc/lsd/` |
| MDMA/Ecstasy | `src/main/java/de/rolandsw/schedulemc/mdma/` |
| Mushrooms | `src/main/java/de/rolandsw/schedulemc/mushroom/` |
| Coffee | `src/main/java/de/rolandsw/schedulemc/coffee/` |
| Wine | `src/main/java/de/rolandsw/schedulemc/wine/` |
| Cheese | `src/main/java/de/rolandsw/schedulemc/cheese/` |
| Honey | `src/main/java/de/rolandsw/schedulemc/honey/` |
| Chocolate | `src/main/java/de/rolandsw/schedulemc/chocolate/` |
| Beer | `src/main/java/de/rolandsw/schedulemc/beer/` |

---

## Detailed System Guides

- [Tobacco System](production/Tobacco-System.md)
- [Cannabis System](production/Cannabis-System.md)
- [Coca System](production/Coca-System.md)
- [Poppy System](production/Poppy-System.md)
- [Meth System](production/Meth-System.md)
- [LSD System](production/LSD-System.md)
- [MDMA System](production/MDMA-System.md)
- [Mushroom System](production/Mushroom-System.md)

---

[Back to Wiki Home](Home.md)
