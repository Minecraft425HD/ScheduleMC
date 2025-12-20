# Complete Items Reference - ScheduleMC

**Total Items:** 141 items across 11 categories

Quick Navigation:
- [Economy & System Items](#economy--system-items) (3)
- [NPC Items](#npc-items) (4)
- [Tobacco Items](#tobacco-items) (32)
- [Cannabis Items](#cannabis-items) (10)
- [Coca Items](#coca-items) (9)
- [Poppy Items](#poppy-items) (8)
- [Meth Items](#meth-items) (8)
- [LSD Items](#lsd-items) (6)
- [MDMA Items](#mdma-items) (6)
- [Mushroom Items](#mushroom-items) (15)
- [Vehicle Items](#vehicle-items) (40)

---

## Economy & System Items

### Plot Selection Tool
- **ID:** `plot_selection_tool`
- **Type:** Tool
- **Stack Size:** 1
- **Durability:** Unlimited
- **Usage:**
  - Left-click block: Set position 1
  - Right-click block: Set position 2
  - Creates rectangular selection
- **Obtain:** `/plot wand`
- **Related Commands:** `/plot create`, `/plot apartment wand`

### Cash Item
- **ID:** `cash`
- **Type:** Currency
- **Stack Size:** 64
- **Value:** Variable (stored in NBT)
- **Usage:**
  - Physical money representation
  - Can be dropped, traded, stolen
  - Deposit at ATM
- **Obtain:** `/money withdraw <amount>` at ATM
- **Related:** ATM Block, Wallet System

### Packaged Drug (Universal)
- **ID:** `packaged_drug`
- **Type:** Consumable
- **Stack Size:** 16
- **NBT Data:**
  - Drug type (tobacco/cannabis/coca/etc.)
  - Quality level (Poor/Good/Very Good/Legendary)
  - Weight (1g/5g/10g/25g/50g/100g)
- **Usage:** Universal packaging for all drugs
- **Obtain:** Packaging tables/machines

---

## NPC Items

### NPC Spawner Tool
- **ID:** `npc_spawner_tool`
- **Type:** Admin Tool
- **Stack Size:** 1
- **Permission:** OP Level 2
- **Usage:** Right-click to open NPC spawn GUI
- **Options:**
  - NPC Type: Resident/Merchant/Police
  - Name customization
  - Skin selection
- **Related Commands:** `/npc spawn`

### NPC Location Tool
- **ID:** `npc_location_tool`
- **Type:** Admin Tool
- **Stack Size:** 1
- **Permission:** OP Level 2
- **Usage:**
  - Set home location
  - Set work location
  - Right-click NPC, then location
- **Related Commands:** `/npc <name> schedule`

### NPC Leisure Tool
- **ID:** `npc_leisure_tool`
- **Type:** Admin Tool
- **Stack Size:** 1
- **Permission:** OP Level 2
- **Usage:**
  - Add leisure location (max 10)
  - Right-click NPC, then location
  - NPCs visit randomly
- **Related Commands:** `/npc <name> leisure`

### NPC Patrol Tool
- **ID:** `npc_patrol_tool`
- **Type:** Admin Tool
- **Stack Size:** 1
- **Permission:** OP Level 2
- **Usage:**
  - Set patrol routes for Police NPCs
  - Right-click to add waypoints
- **For:** Police NPCs only

---

## Tobacco Items

### Seeds (4 Strains)

#### Virginia Seeds
- **ID:** `virginia_seeds`
- **Type:** TobaccoType.VIRGINIA
- **Stack Size:** 64
- **Growth Time:** ~10 minutes
- **Quality Range:** Poor - Legendary
- **Climate:** Warm, moderate humidity
- **Obtain:** `/tobacco give virginia_seeds`

#### Burley Seeds
- **ID:** `burley_seeds`
- **Type:** TobaccoType.BURLEY
- **Stack Size:** 64
- **Growth Time:** ~12 minutes
- **Quality Range:** Poor - Legendary
- **Climate:** Cool, dry
- **Obtain:** `/tobacco give burley_seeds`

#### Oriental Seeds
- **ID:** `oriental_seeds`
- **Type:** TobaccoType.ORIENTAL
- **Stack Size:** 64
- **Growth Time:** ~15 minutes
- **Quality Range:** Poor - Legendary
- **Climate:** Hot, dry
- **Obtain:** `/tobacco give oriental_seeds`

#### Havana Seeds
- **ID:** `havana_seeds`
- **Type:** TobaccoType.HAVANA
- **Stack Size:** 64
- **Growth Time:** ~20 minutes (slowest, highest quality)
- **Quality Range:** Good - Legendary
- **Climate:** Tropical, humid
- **Obtain:** `/tobacco give havana_seeds`

### Fresh Tobacco Leaves (4 Types)

| Item ID | Type | Stack Size | Next Step |
|---------|------|------------|-----------|
| `fresh_virginia_leaf` | Virginia | 16 | Drying rack |
| `fresh_burley_leaf` | Burley | 16 | Drying rack |
| `fresh_oriental_leaf` | Oriental | 16 | Drying rack |
| `fresh_havana_leaf` | Havana | 16 | Drying rack |

**Obtain:** Harvest from fully grown tobacco plants (stage 7/7)

### Dried Tobacco Leaves (4 Types)

| Item ID | Type | Stack Size | Processing Time | Next Step |
|---------|------|------------|-----------------|-----------|
| `dried_virginia_leaf` | Virginia | 16 | 5 min | Fermentation |
| `dried_burley_leaf` | Burley | 16 | 6 min | Fermentation |
| `dried_oriental_leaf` | Oriental | 16 | 7 min | Fermentation |
| `dried_havana_leaf` | Havana | 16 | 10 min | Fermentation |

**Obtain:** Drying racks (Small/Medium/Big)

### Fermented Tobacco Leaves (4 Types)

| Item ID | Type | Stack Size | Processing Time | Final Product |
|---------|------|------------|-----------------|---------------|
| `fermented_virginia_leaf` | Virginia | 16 | 10 min | Ready for packaging |
| `fermented_burley_leaf` | Burley | 16 | 12 min | Ready for packaging |
| `fermented_oriental_leaf` | Oriental | 16 | 15 min | Ready for packaging |
| `fermented_havana_leaf` | Havana | 16 | 20 min | Ready for packaging |

**Obtain:** Fermentation barrels (Small/Medium/Big)

### Tobacco Tools

#### Fertilizer Bottle
- **ID:** `fertilizer_bottle`
- **Stack Size:** 16
- **Effect:** +10% growth speed
- **Uses:** 10 applications
- **Application:** Right-click on pot
- **Obtain:** `/tobacco give fertilizer`

#### Growth Booster Bottle
- **ID:** `growth_booster_bottle`
- **Stack Size:** 16
- **Effect:** +25% growth speed (stacks with fertilizer)
- **Uses:** 5 applications
- **Application:** Right-click on pot
- **Obtain:** `/tobacco give growth_booster`

#### Quality Booster Bottle
- **ID:** `quality_booster_bottle`
- **Stack Size:** 16
- **Effect:** +1 quality tier chance
- **Uses:** 5 applications
- **Application:** Right-click on pot
- **Obtain:** `/tobacco give quality_booster`

#### Watering Can
- **ID:** `watering_can`
- **Stack Size:** 1
- **Capacity:** 1000 water units
- **Effect:** Prevents wilting, +5% growth
- **Refill:** Right-click water source or sink
- **Obtain:** `/tobacco give watering_can`

### Soil Bags

| Item ID | Type | Stack Size | Soil Units | Pot Capacity |
|---------|------|------------|------------|--------------|
| `soil_bag_small` | Small | 16 | 100 units | 1 terracotta pot |
| `soil_bag_medium` | Medium | 8 | 500 units | 5 terracotta pots |
| `soil_bag_large` | Large | 4 | 1000 units | 10 terracotta pots |

**Usage:** Right-click empty pot to fill

### Packaging Materials

#### Packaging Bag
- **ID:** `packaging_bag`
- **Stack Size:** 64
- **For:** Small packages (1g-10g)
- **Material Cost:** Paper + String
- **Obtain:** Crafting

#### Packaging Jar
- **ID:** `packaging_jar`
- **Stack Size:** 16
- **For:** Medium packages (25g-50g)
- **Material Cost:** Glass
- **Obtain:** Crafting

#### Packaging Box
- **ID:** `packaging_box`
- **Stack Size:** 8
- **For:** Large packages (100g+)
- **Material Cost:** Wood + Nails
- **Obtain:** Crafting

---

## Cannabis Items

### Cannabis Seed
- **ID:** `cannabis_seed`
- **Stack Size:** 64
- **Strains:** 4 (Indica/Sativa/Hybrid/Autoflower)
- **Growth Stages:** 8
- **Growth Time:** 12-20 minutes
- **Obtain:** Admin command or NPC trading

### Processing Stages

| Item | Stage | Stack | Time | Next |
|------|-------|-------|------|------|
| `fresh_cannabis_bud` | Harvest | 16 | - | Drying net |
| `dried_cannabis_bud` | Drying | 16 | 5 min | Trimming |
| `trimmed_cannabis_bud` | Trimming | 16 | 3 min | Curing |
| `cured_cannabis_bud` | Curing | 16 | 10 min | Final/Hash/Oil |

### By-Products

#### Cannabis Trim
- **ID:** `cannabis_trim`
- **Stack Size:** 64
- **Source:** Trimm Station by-product
- **Use:** Hash production
- **Ratio:** 4 trim = 1 hash

### Concentrates

#### Cannabis Hash
- **ID:** `cannabis_hash`
- **Stack Size:** 16
- **Process:** Hash Presse
- **Input:** 4 trim or 2 cured buds
- **Time:** 5 minutes
- **Quality:** Inherits from source

#### Cannabis Oil
- **ID:** `cannabis_oil`
- **Stack Size:** 16
- **Process:** Öl Extraktor
- **Input:** 3 cured buds + solvent
- **Time:** 8 minutes
- **Potency:** 3x concentrated

### Processing Items

#### Pollen Press Mold
- **ID:** `pollen_press_mold`
- **Stack Size:** 1
- **Durability:** 100 uses
- **For:** Hash Presse
- **Obtain:** Crafting

#### Extraction Solvent
- **ID:** `extraction_solvent`
- **Stack Size:** 16
- **For:** Öl Extraktor
- **Consumption:** 1 per extraction
- **Obtain:** Crafting or NPC

---

## Coca Items

### Seeds

#### Bolivianisch Coca Seeds
- **ID:** `bolivianisch_coca_seeds`
- **Type:** CocaType.BOLIVIANISCH
- **Stack Size:** 64
- **Growth Time:** 15 minutes
- **Yield:** Higher quantity
- **Quality:** Standard

#### Kolumbianisch Coca Seeds
- **ID:** `kolumbianisch_coca_seeds`
- **Type:** CocaType.KOLUMBIANISCH
- **Stack Size:** 64
- **Growth Time:** 18 minutes
- **Yield:** Lower quantity
- **Quality:** Higher purity

### Fresh Leaves

| Item | Type | Stack | Yield/Plant |
|------|------|-------|-------------|
| `fresh_bolivianisch_coca_leaf` | Bolivianisch | 16 | 3-5 leaves |
| `fresh_kolumbianisch_coca_leaf` | Kolumbianisch | 16 | 2-4 leaves |

### Processing Chain

#### Coca Paste
- **ID:** `coca_paste`
- **Stack Size:** 16
- **Process:** Extraction vat
- **Input:** 10 leaves + 1 diesel canister
- **Time:** 8 minutes
- **Color:** Brown paste

#### Diesel Canister
- **ID:** `diesel_canister`
- **Stack Size:** 16
- **Required For:** Coca extraction
- **Source:** Fuel stations or crafting

#### Cocaine
- **ID:** `cocaine`
- **Stack Size:** 16
- **Process:** Refinery
- **Input:** 3 coca paste
- **Time:** 15 minutes (glowing effect!)
- **Purity:** 70-95%

#### Crack Rock
- **ID:** `crack_rock`
- **Stack Size:** 16
- **Process:** Crack Kocher
- **Input:** 1 cocaine + 1 backpulver
- **Time:** 5 minutes
- **Effect:** Crystallized form

#### Backpulver (Baking Soda)
- **ID:** `backpulver`
- **Stack Size:** 64
- **Required For:** Crack production
- **Ratio:** 1:1 with cocaine

---

## Poppy Items

### Seeds (3 Strains)

| Item ID | Type | Stack | Growth Time | Opium Yield |
|---------|------|-------|-------------|-------------|
| `afghanisch_poppy_seeds` | Afghanisch | 64 | 12 min | High |
| `tuerkisch_poppy_seeds` | Türkisch | 64 | 15 min | Medium |
| `indisch_poppy_seeds` | Indisch | 64 | 18 min | Premium |

### Processing Chain

#### Poppy Pod
- **ID:** `poppy_pod`
- **Stack Size:** 16
- **Obtain:** Harvest mature poppy
- **Next:** Ritzmaschine (scoring)

#### Raw Opium
- **ID:** `raw_opium`
- **Stack Size:** 16
- **Process:** Ritzmaschine
- **Color:** Dark brown
- **Next:** Opium Presse

#### Morphine
- **ID:** `morphine`
- **Stack Size:** 16
- **Process:** Kochstation
- **Input:** Pressed opium
- **Time:** 10 minutes
- **Purity:** 60-85%

#### Heroin
- **ID:** `heroin`
- **Stack Size:** 16
- **Process:** Heroin Raffinerie
- **Input:** Morphine
- **Time:** 20 minutes
- **Purity:** 80-99%

### Tool

#### Scoring Knife
- **ID:** `scoring_knife`
- **Stack Size:** 1
- **Durability:** 200 uses
- **For:** Manual opium extraction (alternative to Ritzmaschine)

---

## Meth Items

### Basic Chemicals

| Item | Stack | Source | Danger |
|------|-------|--------|--------|
| `ephedrin` | 16 | NPC/Crafting | Regulated |
| `pseudoephedrin` | 16 | NPC/Crafting | Regulated |
| `roter_phosphor` | 16 | Crafting | Flammable |
| `jod` | 16 | Crafting | Corrosive |

### Processing Chain

#### Meth Paste
- **ID:** `meth_paste`
- **Stack Size:** 16
- **Process:** Chemie Mixer
- **Input:** Ephedrin/Pseudoephedrin + chemicals
- **Time:** 5 minutes

#### Roh Meth (Raw Meth)
- **ID:** `roh_meth`
- **Stack Size:** 16
- **Process:** Reduktionskessel ⚠️
- **Input:** Meth paste + roter phosphor + jod
- **Time:** 10 minutes
- **WARNING:** EXPLOSION RISK!

#### Kristall Meth (Crystal Meth)
- **ID:** `kristall_meth`
- **Stack Size:** 16
- **Process:** Kristallisator
- **Input:** Raw meth
- **Time:** 15 minutes
- **Appearance:** Crystalline

#### Meth (Final)
- **ID:** `meth`
- **Stack Size:** 16
- **Process:** Vakuum Trockner
- **Input:** Crystal meth
- **Time:** 8 minutes
- **Purity:** 95-99%

---

## LSD Items

### Base Materials

#### Mutterkorn (Ergot)
- **ID:** `mutterkorn`
- **Stack Size:** 16
- **Source:** Specialized farming
- **Toxicity:** Dangerous if consumed raw

#### Blotter Papier
- **ID:** `blotter_papier`
- **Stack Size:** 64
- **For:** Final LSD tabs
- **Size:** 10x10 grid

### Processing Chain

#### Ergot Kultur (Culture)
- **ID:** `ergot_kultur`
- **Stack Size:** 16
- **Process:** Fermentations Tank
- **Input:** Mutterkorn
- **Time:** 12 minutes

#### Lysergsäure (Lysergic Acid)
- **ID:** `lysergsaeure`
- **Stack Size:** 16
- **Process:** Destillations Apparat (glowing!)
- **Input:** Ergot culture
- **Time:** 20 minutes
- **Danger:** Requires precision

#### LSD Lösung (Solution)
- **ID:** `lsd_loesung`
- **Stack Size:** 16
- **Process:** Chemical synthesis
- **Input:** Lysergic acid + reagents
- **Time:** 25 minutes
- **Concentration:** 100µg/ml

#### LSD Blotter
- **ID:** `lsd_blotter`
- **Stack Size:** 16
- **Process:** Mikro Dosierer (GUI!) → Perforations Presse
- **Input:** Solution + blotter paper
- **Time:** 5 min + 3 min
- **Tabs:** 100 per sheet

---

## MDMA Items

### Raw Materials

| Item | Stack | Source | Use |
|------|-------|--------|-----|
| `safrol` | 16 | Extraction | Base chemical |
| `bindemittel` | 64 | Crafting | Pill binder |
| `pillen_farbstoff` | 64 | Crafting | Pill coloring |

### Processing Chain

#### MDMA Base
- **ID:** `mdma_base`
- **Stack Size:** 16
- **Process:** Reaktions Kessel (glowing!)
- **Input:** Safrol + reagents
- **Time:** 10 minutes

#### MDMA Kristall
- **ID:** `mdma_kristall`
- **Stack Size:** 16
- **Process:** Trocknungs Ofen (hot!)
- **Input:** MDMA base
- **Time:** 15 minutes
- **Purity:** 80-95%

#### Ecstasy Pill
- **ID:** `ecstasy_pill`
- **Stack Size:** 64
- **Process:** Pillen Presse ⭐ MINIGAME!
- **Input:** Crystal + binder + dye
- **Time:** Timing-based
- **Colors:** Customizable

---

## Mushroom Items

### Mist Bags (3 Sizes)

| Item ID | Size | Stack | Capacity | Growth Space |
|---------|------|-------|----------|--------------|
| `mist_bag_small` | Small | 16 | 1 spore syringe | 3 mushrooms |
| `mist_bag_medium` | Medium | 8 | 2 spore syringes | 6 mushrooms |
| `mist_bag_large` | Large | 4 | 3 spore syringes | 9 mushrooms |

### Spore Syringes (3 Strains)

| Item ID | Strain | Stack | Potency | Growth Time |
|---------|--------|-------|---------|-------------|
| `spore_syringe_cubensis` | Cubensis | 8 | Standard | 8 min |
| `spore_syringe_azurescens` | Azurescens | 8 | High | 12 min |
| `spore_syringe_mexicana` | Mexicana | 8 | Premium | 15 min |

### Fresh Mushrooms (3 Strains)

| Item ID | Strain | Stack | Harvest | Drying |
|---------|--------|-------|---------|--------|
| `fresh_cubensis` | Cubensis | 16 | Mist bag | Required |
| `fresh_azurescens` | Azurescens | 16 | Mist bag | Required |
| `fresh_mexicana` | Mexicana | 16 | Mist bag | Required |

### Dried Mushrooms (3 Strains)

| Item ID | Strain | Stack | Drying Time | Final Product |
|---------|--------|-------|-------------|---------------|
| `dried_cubensis` | Cubensis | 16 | 5 min | Ready |
| `dried_azurescens` | Azurescens | 16 | 6 min | Ready |
| `dried_mexicana` | Mexicana | 16 | 7 min | Ready |

---

## Vehicle Items

### Engines (3 Types)

| Item | Registry | HP | Fuel Efficiency | Speed |
|------|----------|----|--------------------|-------|
| `normal_motor` | NORMAL_MOTOR | 100 | Standard | 1.0x |
| `performance_motor` | PERFORMANCE_MOTOR | 150 | Poor | 1.5x |
| `industrial_motor` | INDUSTRIAL_MOTOR | 200 | Good | 0.8x |

### Tires - Standard (3 Types)

| Item | Type | Durability | Speed | Terrain |
|------|------|------------|-------|---------|
| `standard_tire` | Standard | 1000 | 1.0x | Road |
| `sport_tire` | Sport | 800 | 1.2x | Road |
| `premium_tire` | Premium | 1500 | 1.1x | All |

### Tires - Truck (3 Types)

| Item | Type | Durability | Load | Terrain |
|------|------|------------|------|---------|
| `offroad_tire` | Offroad | 2000 | Heavy | Dirt |
| `allterrain_tire` | Allterrain | 1800 | Heavy | All |
| `heavyduty_tire` | Heavy Duty | 2500 | Max | All |

### Chassis (5 Types)

| Item | Type | Capacity | Modules | Weight |
|------|------|----------|---------|--------|
| `limousine_chassis` | Limousine | 4 seats | 2 | Light |
| `van_chassis` | Van | 8 seats | 4 | Medium |
| `truck_chassis` | Truck | 2 seats | 6 | Heavy |
| `offroad_chassis` | Offroad | 5 seats | 3 | Medium |
| `luxus_chassis` | Luxus | 2 seats | 2 | Light |

### Other Parts

#### Fenders
- `fender_basic` - Standard protection
- `fender_chrome` - +10% durability, shiny
- `fender_sport` - +5% speed, aerodynamic

#### Modules
- `cargo_module` - +16 inventory slots
- `fluid_module` - +1000L tank capacity
- `license_sign_mount` - License plate holder

#### Fuel Tanks
- `tank_15l` - 15L capacity, light
- `tank_30l` - 30L capacity, standard
- `tank_50l` - 50L capacity, heavy

### Tools & Consumables

| Item | Function | Durability/Uses |
|------|----------|-----------------|
| `empty_diesel_can` | Portable fuel storage | Refillable |
| `full_diesel_can` | 5L diesel | Single use |
| `maintenance_kit` | Repair vehicle | 10 uses |
| `key` | Start/lock vehicle | Per vehicle |
| `starter_battery` | Jump-start dead vehicle | 5 uses |
| `license_sign` | Vehicle registration | Customizable text |
| `spawn_tool` | Admin: Spawn vehicles | Unlimited |
| `diesel_bucket` | Transfer fuel | 1000 uses |

### Complete Vehicles (5 Types)

| Item | Type | Chassis | Speed | Capacity |
|------|------|---------|-------|----------|
| `limousine` | Car | LIMOUSINE | Fast | 4 seats |
| `van` | Cargo | VAN | Medium | 8 seats |
| `truck` | Heavy | TRUCK | Slow | 2 seats |
| `suv` | Offroad | OFFROAD | Medium | 5 seats |
| `sports_car` | Racing | LUXUS | Very Fast | 2 seats |

---

## Item Categories Summary

| Category | Item Count | Key Features |
|----------|-----------|--------------|
| Economy | 3 | Money, plots, packaging |
| NPC | 4 | Admin tools for NPC management |
| Tobacco | 32 | 4 strains, 6-step process, quality system |
| Cannabis | 10 | 4 strains, hash & oil production |
| Coca | 9 | 2 strains, cocaine & crack |
| Poppy | 8 | 3 strains, opium → heroin chain |
| Meth | 8 | Chemical synthesis, explosion risk |
| LSD | 6 | Laboratory precision, micro-dosing |
| MDMA | 6 | Pill press minigame |
| Mushroom | 15 | 3 strains, climate controlled |
| Vehicle | 40 | Modular parts, 5 complete vehicles |

**Total: 141 Items**

---

[⬆ Back to Wiki Home](Home.md) | [Blocks Reference](Blocks.md) | [Commands Reference](Commands.md)
