# Complete Blocks Reference - ScheduleMC

**Total Blocks:** 210+ blocks across 18 categories

Quick Navigation:

**Illegal Production:**
- [Tobacco Blocks](#tobacco-blocks) (23)
- [Cannabis Blocks](#cannabis-blocks) (9)
- [Coca Blocks](#coca-blocks) (9)
- [Poppy Blocks](#poppy-blocks) (7)
- [Meth Blocks](#meth-blocks) (4)
- [LSD Blocks](#lsd-blocks) (4)
- [MDMA Blocks](#mdma-blocks) (3)
- [Mushroom Blocks](#mushroom-blocks) (4)

**Legal Production:**
- [Coffee Blocks](#coffee-blocks) (12)
- [Wine Blocks](#wine-blocks) (13)
- [Beer Blocks](#beer-blocks) (10)
- [Cheese Blocks](#cheese-blocks) (10)
- [Honey Blocks](#honey-blocks) (12)
- [Chocolate Blocks](#chocolate-blocks) (13)

**Infrastructure:**
- [Economy Blocks](#economy-blocks) (2)
- [Plot Blocks](#plot-blocks) (1)
- [Warehouse Blocks](#warehouse-blocks) (1)
- [Vehicle Blocks](#vehicle-blocks) (4)

---

## Tobacco Blocks

### Planting Pots (4 Variants)

#### Terracotta Pot
- **ID:** `terracotta_pot`
- **Type:** PotType.TERRACOTTA
- **Capacity:** 1 plant
- **Growth Bonus:** +0%
- **Durability:** Standard
- **Crafting:** 7 terracotta
- **BlockEntity:** PlantPotBlockEntity

#### Ceramic Pot
- **ID:** `ceramic_pot`
- **Type:** PotType.CERAMIC
- **Capacity:** 1 plant
- **Growth Bonus:** +10%
- **Durability:** Good
- **Crafting:** 7 bricks + glaze
- **BlockEntity:** PlantPotBlockEntity

#### Iron Pot
- **ID:** `iron_pot`
- **Type:** PotType.IRON
- **Capacity:** 1 plant
- **Growth Bonus:** +25%
- **Durability:** Excellent
- **Crafting:** 7 iron ingots
- **BlockEntity:** PlantPotBlockEntity

#### Golden Pot
- **ID:** `golden_pot`
- **Type:** PotType.GOLDEN
- **Capacity:** 1 plant
- **Growth Bonus:** +50%
- **Quality Bonus:** +1 tier
- **Durability:** Premium
- **Crafting:** 7 gold ingots
- **BlockEntity:** PlantPotBlockEntity

### Tobacco Plants (4 Types) - NO BLOCK ITEMS

| Block ID | Strain | Growth Stages | Harvest |
|----------|--------|---------------|---------|
| `virginia_plant` | Virginia | 8 (0-7) | Stage 7 |
| `burley_plant` | Burley | 8 (0-7) | Stage 7 |
| `oriental_plant` | Oriental | 8 (0-7) | Stage 7 |
| `havana_plant` | Havana | 8 (0-7) | Stage 7 |

**Note:** Plants are NOT items - they grow from seeds placed in pots

### Drying Racks (3 Sizes)

#### Small Drying Rack
- **ID:** `small_drying_rack`
- **Capacity:** 3 slots
- **Processing Time:** 5 minutes
- **Batch Size:** 3 leaves → 3 dried
- **Size:** 1x1x1 blocks
- **BlockEntity:** SmallDryingRackBlockEntity
- **Crafting:** 4 sticks + 2 string

#### Medium Drying Rack
- **ID:** `medium_drying_rack`
- **Capacity:** 6 slots
- **Processing Time:** 4 minutes (faster!)
- **Batch Size:** 6 leaves → 6 dried
- **Size:** 2x1x1 blocks
- **BlockEntity:** MediumDryingRackBlockEntity
- **Crafting:** 8 sticks + 4 string

#### Big Drying Rack
- **ID:** `big_drying_rack`
- **Capacity:** 9 slots
- **Processing Time:** 3 minutes (fastest!)
- **Batch Size:** 9 leaves → 9 dried
- **Size:** 3x2x1 blocks
- **BlockEntity:** BigDryingRackBlockEntity
- **Crafting:** 12 sticks + 6 string

### Fermentation Barrels (3 Sizes)

#### Small Fermentation Barrel
- **ID:** `small_fermentation_barrel`
- **Capacity:** 4 slots
- **Processing Time:** 10 minutes
- **Quality Bonus:** +5%
- **Size:** 1x1x1 blocks
- **BlockEntity:** SmallFermentationBarrelBlockEntity
- **Crafting:** 8 planks + 2 iron ingots

#### Medium Fermentation Barrel
- **ID:** `medium_fermentation_barrel`
- **Capacity:** 8 slots
- **Processing Time:** 12 minutes
- **Quality Bonus:** +15%
- **Size:** 1x2x1 blocks
- **BlockEntity:** MediumFermentationBarrelBlockEntity
- **Crafting:** 16 planks + 4 iron ingots

#### Big Fermentation Barrel
- **ID:** `big_fermentation_barrel`
- **Capacity:** 16 slots
- **Processing Time:** 15 minutes
- **Quality Bonus:** +30%
- **Size:** 2x2x2 blocks
- **BlockEntity:** BigFermentationBarrelBlockEntity
- **Crafting:** 32 planks + 8 iron ingots

### Packaging Tables (3 Sizes) - MULTI-BLOCK 2x2

#### Small Packaging Table
- **ID:** `small_packaging_table`
- **Size:** 2x2 multi-block
- **Capacity:** 4 input slots
- **Package Sizes:** 1g, 5g, 10g
- **Speed:** 1 package/minute
- **BlockEntity:** SmallPackagingTableBlockEntity
- **Crafting:** 16 planks + 4 iron

#### Medium Packaging Table
- **ID:** `medium_packaging_table`
- **Size:** 2x2 multi-block
- **Capacity:** 8 input slots
- **Package Sizes:** 1g-50g
- **Speed:** 2 packages/minute
- **BlockEntity:** MediumPackagingTableBlockEntity
- **Crafting:** 32 planks + 8 iron

#### Large Packaging Table
- **ID:** `large_packaging_table`
- **Size:** 2x2 multi-block
- **Capacity:** 16 input slots
- **Package Sizes:** All (1g-100g+)
- **Speed:** 4 packages/minute
- **BlockEntity:** LargePackagingTableBlockEntity
- **Crafting:** 64 planks + 16 iron

### Grow Lights (3 Tiers)

| Block ID | Tier | Light Level | Growth Bonus | Power Usage |
|----------|------|-------------|--------------|-------------|
| `basic_grow_light_slab` | Basic | 12 | +10% | 5 W |
| `advanced_grow_light_slab` | Advanced | 14 | +25% | 10 W |
| `premium_grow_light_slab` | Premium | 15 | +50% | 20 W |

**BlockEntity:** GrowLightSlabBlockEntity
**Requires:** Utility System (power)
**Placement:** Ceiling/wall mounted

### Utility

#### Sink
- **ID:** `sink`
- **Function:** Refill watering cans
- **Water Capacity:** Unlimited (water source)
- **BlockEntity:** SinkBlockEntity
- **Crafting:** Iron ingots + bucket
- **Requires:** Water source nearby

---

## Cannabis Blocks

### Cannabis Plants (4 Strains) - NO BLOCK ITEMS

| Block ID | Strain | Growth Stages | Characteristics |
|----------|--------|---------------|-----------------|
| `cannabis_indica_plant` | Indica | 8 | Short, bushy, faster |
| `cannabis_sativa_plant` | Sativa | 8 | Tall, thin, slower |
| `cannabis_hybrid_plant` | Hybrid | 8 | Balanced |
| `cannabis_autoflower_plant` | Autoflower | 6 | Fastest, auto-bloom |

### Processing Blocks

#### Trocknungsnetz (Drying Net)
- **ID:** `cannabis_trocknungsnetz`
- **Function:** Dry fresh buds
- **Capacity:** 9 slots
- **Time:** 5 minutes
- **Output:** Dried cannabis buds
- **BlockEntity:** TrocknungsnetzBlockEntity
- **Size:** 2x1x2 blocks

#### Trimm Station
- **ID:** `cannabis_trimm_station`
- **Function:** Trim dried buds
- **Capacity:** 6 slots
- **Time:** 3 minutes
- **Output:** Trimmed buds + Trim
- **Trim Ratio:** 25% of input
- **BlockEntity:** TrimmStationBlockEntity

#### Curing Glas (Curing Jar)
- **ID:** `cannabis_curing_glas`
- **Function:** Cure trimmed buds
- **Capacity:** 4 slots
- **Time:** 10 minutes
- **Quality Bonus:** +20%
- **BlockEntity:** CuringGlasBlockEntity
- **Crafting:** 8 glass + lid

#### Hash Presse
- **ID:** `cannabis_hash_presse`
- **Function:** Press hash from trim/buds
- **Input:** 4 trim OR 2 cured buds
- **Output:** 1 hash
- **Time:** 5 minutes
- **Pressure:** Hydraulic press
- **BlockEntity:** HashPresseBlockEntity

#### Öl Extraktor
- **ID:** `cannabis_oel_extraktor`
- **Function:** Extract cannabis oil
- **Input:** 3 cured buds + 1 solvent
- **Output:** 1 oil (3x potency)
- **Time:** 8 minutes
- **Method:** Solvent extraction
- **BlockEntity:** OelExtraktortBlockEntity

---

## Coca Blocks

### Coca Plants (2 Types) - NO BLOCK ITEMS

| Block ID | Type | Growth Time | Yield |
|----------|------|-------------|-------|
| `bolivianisch_coca_plant` | Bolivianisch | 15 min | 3-5 leaves |
| `kolumbianisch_coca_plant` | Kolumbianisch | 18 min | 2-4 leaves |

### Extraction Vats (3 Sizes)

| Block ID | Size | Capacity | Time | Output |
|----------|------|----------|------|--------|
| `small_extraction_vat` | Small | 10 leaves | 5 min | 2 paste |
| `medium_extraction_vat` | Medium | 20 leaves | 8 min | 5 paste |
| `big_extraction_vat` | Big | 40 leaves | 12 min | 12 paste |

**BlockEntity:** SmallExtractionVatBlockEntity (+ Medium, Big variants)
**Required:** 1 diesel canister per batch
**Process:** Chemical extraction

### Refineries (3 Sizes) - WITH GLOWING EFFECT

| Block ID | Size | Capacity | Time | Light | Purity |
|----------|------|----------|------|-------|--------|
| `small_refinery` | Small | 3 paste | 10 min | 8 | 70-85% |
| `medium_refinery` | Medium | 6 paste | 15 min | 10 | 80-92% |
| `big_refinery` | Big | 12 paste | 20 min | 12 | 90-95% |

**BlockEntity:** SmallRefineryBlockEntity (+ Medium, Big variants)
**Effect:** Glowing when active
**Output:** Cocaine

### Crack Production

#### Crack Kocher
- **ID:** `crack_kocher`
- **Function:** Cook crack from cocaine
- **Input:** 1 cocaine + 1 backpulver (baking soda)
- **Output:** 2 crack rocks
- **Time:** 5 minutes
- **Light Level:** 6 (when active)
- **BlockEntity:** CrackKocherBlockEntity

---

## Poppy Blocks

### Poppy Plants (3 Types) - NO BLOCK ITEMS

| Block ID | Type | Growth Time | Opium Quality |
|----------|------|-------------|---------------|
| `afghanisch_poppy_plant` | Afghanisch | 12 min | High yield |
| `tuerkisch_poppy_plant` | Türkisch | 15 min | Medium |
| `indisch_poppy_plant` | Indisch | 18 min | Premium |

### Processing Blocks

#### Ritzmaschine (Scoring Machine)
- **ID:** `ritzmaschine`
- **Function:** Score poppy pods for opium
- **Capacity:** 9 pods
- **Output:** Raw opium
- **Time:** 3 minutes
- **Automation:** Automated scoring
- **BlockEntity:** RitzmaschineBlockEntity

#### Opium Presse
- **ID:** `opium_presse`
- **Function:** Press raw opium
- **Input:** 5 raw opium
- **Output:** 1 pressed opium
- **Time:** 5 minutes
- **Pressure:** High-pressure press
- **BlockEntity:** OpiumPresseBlockEntity

#### Kochstation (Cooking Station)
- **ID:** `kochstation`
- **Function:** Cook pressed opium → morphine
- **Input:** 2 pressed opium
- **Output:** 1 morphine
- **Time:** 10 minutes
- **Temperature:** 185°C
- **BlockEntity:** KochstationBlockEntity

#### Heroin Raffinerie
- **ID:** `heroin_raffinerie`
- **Function:** Refine morphine → heroin
- **Input:** 1 morphine + chemicals
- **Output:** 1 heroin
- **Time:** 20 minutes
- **Purity:** 80-99%
- **BlockEntity:** HeroinRaffinerieBlockEntity

---

## Meth Blocks

**Complete 4-Step Production Chain:**

### 1. Chemie Mixer
- **ID:** `chemie_mixer`
- **Function:** Mix base chemicals
- **Input:** Ephedrin/Pseudoephedrin + reagents
- **Output:** Meth paste
- **Time:** 5 minutes
- **Safety:** Moderate
- **BlockEntity:** ChemieMixerBlockEntity

### 2. Reduktionskessel (Reduction Kettle)
- **ID:** `reduktionskessel`
- **Function:** Reduce paste → raw meth
- **Input:** Meth paste + roter phosphor + jod
- **Output:** Raw meth
- **Time:** 10 minutes
- **Light Level:** 4 (glowing)
- **⚠️ DANGER:** EXPLOSION RISK if mishandled!
- **BlockEntity:** ReduktionskesselBlockEntity

### 3. Kristallisator (Crystallizer)
- **ID:** `kristallisator`
- **Function:** Crystallize raw meth
- **Input:** Raw meth
- **Output:** Crystal meth
- **Time:** 15 minutes
- **Process:** Slow crystallization
- **BlockEntity:** KristallisatorBlockEntity

### 4. Vakuum Trockner (Vacuum Dryer)
- **ID:** `vakuum_trockner`
- **Function:** Final drying
- **Input:** Crystal meth
- **Output:** Meth (95-99% pure)
- **Time:** 8 minutes
- **Vacuum:** -0.9 bar
- **BlockEntity:** VakuumTrocknerBlockEntity

---

## LSD Blocks

**Precision Laboratory Production Chain:**

### 1. Fermentations Tank
- **ID:** `fermentations_tank`
- **Function:** Ferment mutterkorn → ergot culture
- **Input:** Mutterkorn
- **Output:** Ergot culture
- **Time:** 12 minutes
- **Temperature:** 25°C
- **BlockEntity:** FermentationsTankBlockEntity

### 2. Destillations Apparat (Distillation Apparatus)
- **ID:** `destillations_apparat`
- **Function:** Distill culture → lysergic acid
- **Input:** Ergot culture
- **Output:** Lysergsäure
- **Time:** 20 minutes
- **Light Level:** 6 (glowing!)
- **Precision:** High
- **BlockEntity:** DestillationsApparatBlockEntity

### 3. Mikro Dosierer (Micro Doser)
- **ID:** `mikro_dosierer`
- **Function:** Apply solution to blotter paper
- **Input:** LSD solution + blotter paper
- **Output:** Soaked blotter
- **Time:** 5 minutes
- **GUI:** Yes (dosing control)
- **Precision:** ±5µg
- **BlockEntity:** MikroDosiererBlockEntity

### 4. Perforations Presse (Perforation Press)
- **ID:** `perforations_presse`
- **Function:** Cut blotter into tabs
- **Input:** Soaked blotter
- **Output:** 100 LSD tabs
- **Time:** 3 minutes
- **Grid:** 10x10
- **BlockEntity:** PerforationsPresseBlockEntity

---

## MDMA Blocks

**Arcade-Style Production:**

### 1. Reaktions Kessel (Reaction Kettle)
- **ID:** `reaktions_kessel`
- **Function:** Synthesize MDMA base from safrol
- **Input:** Safrol + reagents
- **Output:** MDMA base
- **Time:** 10 minutes
- **Light Level:** 4 (glowing!)
- **Heat:** 120°C
- **BlockEntity:** ReaktionsKesselBlockEntity

### 2. Trocknungs Ofen (Drying Oven)
- **ID:** `trocknungs_ofen`
- **Function:** Dry MDMA base → crystals
- **Input:** MDMA base
- **Output:** MDMA crystals
- **Time:** 15 minutes
- **Light Level:** 8 (hot!)
- **Temperature:** 80°C
- **BlockEntity:** TrocknungsOfenBlockEntity

### 3. Pillen Presse (Pill Press)
- **ID:** `pillen_presse`
- **Function:** Press ecstasy pills
- **Input:** MDMA crystal + binder + dye
- **Output:** Ecstasy pills
- **⭐ TIMING MINIGAME!**
- **Success Rate:** Skill-based
- **Pill Shapes:** Customizable
- **BlockEntity:** PillenPresseBlockEntity

---

## Mushroom Blocks

### Climate Lamps (3 Tiers)

| Block ID | Tier | Light Level | Heat | Growth Bonus | Power |
|----------|------|-------------|------|--------------|-------|
| `klimalampe_small` | Small | 10 | Low | +15% | 8 W |
| `klimalampe_medium` | Medium | 12 | Medium | +30% | 15 W |
| `klimalampe_large` | Large | 14 | High | +50% | 25 W |

**BlockEntity:** KlimalampeBlockEntity
**Required:** Utility System (power + water)
**Effect:** Humidity + warmth control

### Wassertank (Water Tank)
- **ID:** `wassertank`
- **Function:** Provide water/humidity
- **Capacity:** 10,000 water units
- **Coverage:** 5x5 area
- **Refill:** Rain or manual
- **BlockEntity:** WassertankBlockEntity
- **Requirement:** For mushroom growth

---

## Coffee Blocks

### Coffee Pots (4 Variants)

| Block ID | Pot Type | Growth Bonus |
|----------|----------|-------------|
| `coffee_terracotta_pot` | Terracotta | Standard |
| `coffee_ceramic_pot` | Ceramic | +10% |
| `coffee_iron_pot` | Iron | +25% |
| `coffee_golden_pot` | Golden | +50% + Quality |

### Coffee Plants (4 Types) - NO BLOCK ITEMS

| Block ID | Variety | Growth Ticks | Yield |
|----------|---------|-------------|-------|
| `arabica_plant` | Arabica | 140 | 8 cherries |
| `robusta_plant` | Robusta | 120 | 10 cherries |
| `liberica_plant` | Liberica | 160 | 6 cherries |
| `excelsa_plant` | Excelsa | 180 | 7 cherries |

### Processing Blocks

#### Wet Processing Station
- **ID:** `wet_processing_station`
- **Function:** Multi-stage wet processing (Pulping, Fermentation, Washing)
- **Capacity:** 32 cherries
- **Total Time:** 1,800 ticks (90 seconds)
- **Output:** Green coffee beans (2 per cherry)

#### Coffee Drying Trays (3 Sizes)

| Block ID | Capacity | Ticks/Cherry |
|----------|----------|-------------|
| `small_coffee_drying_tray` | 15 | 300 |
| `medium_coffee_drying_tray` | 22 | 250 |
| `large_coffee_drying_tray` | 30 | 400 |

#### Coffee Roasters (3 Sizes)

| Block ID | Capacity | Ticks/Bean |
|----------|----------|-----------|
| `small_coffee_roaster` | 16 | 300 |
| `medium_coffee_roaster` | 24 | 250 |
| `large_coffee_roaster` | 32 | 200 |

**Selectable Roast Levels:** Light (200C), Medium (220C), Dark (240C), Espresso (260C)

#### Coffee Grinder
- **ID:** `coffee_grinder`
- **Capacity:** 64 roasted beans
- **Time:** 100 ticks (5 seconds)
- **Selectable Grind Sizes:** Coarse, Medium, Fine, Extra Fine

#### Coffee Packaging Table
- **ID:** `coffee_packaging_table`
- **Function:** Package ground coffee into bags
- **Time:** 200 ticks (10 seconds)
- **Package Sizes:** 250g, 500g, 1kg

---

## Wine Blocks

### Grapevine Pots (4 Varieties)

| Block ID | Variety |
|----------|---------|
| `riesling_grapevine_pot` | Riesling (White) |
| `spaetburgunder_grapevine_pot` | Spaetburgunder (Red) |
| `chardonnay_grapevine_pot` | Chardonnay (White) |
| `merlot_grapevine_pot` | Merlot (Red) |

### Processing Blocks

#### Crushing Station
- **ID:** `crushing_station`
- **Function:** Crush grapes into mash
- **Quality:** Determined at crushing

#### Wine Presses (3 Sizes)

| Block ID | Capacity | Ticks/Mash |
|----------|----------|-----------|
| `small_wine_press` | 16 | 200 |
| `medium_wine_press` | 24 | 150 |
| `large_wine_press` | 32 | 100 |

#### Fermentation Tanks (3 Sizes)

| Block ID | Capacity | Ticks/Juice |
|----------|----------|------------|
| `small_fermentation_tank` | 16 | 2,400 |
| `medium_fermentation_tank` | 24 | 1,800 |
| `large_fermentation_tank` | 32 | 1,200 |

#### Aging Barrels (3 Sizes)

| Block ID | Capacity | Feature |
|----------|----------|---------|
| `small_aging_barrel` | 16 | Auto-aging, quality upgrade every 2,400 ticks |
| `medium_aging_barrel` | 24 | Auto-aging, quality upgrade every 2,400 ticks |
| `large_aging_barrel` | 32 | Auto-aging, quality upgrade every 2,400 ticks |

#### Wine Bottling Station
- **ID:** `wine_bottling_station`
- **Function:** Seal wine in bottles (stops aging)

---

## Beer Blocks

### Processing Blocks

#### Malting Station
- **ID:** `malting_station`
- **Function:** Malt grain into wort

#### Mash Tun
- **ID:** `mash_tun`
- **Function:** Mash grain with water

#### Brew Kettles (3 Sizes)

| Block ID | Speed | Ticks |
|----------|-------|-------|
| `small_brew_kettle` | 1.0x | 1,200 |
| `medium_brew_kettle` | 1.5x | 800 |
| `large_brew_kettle` | 2.0x | 600 |

#### Beer Fermentation Tanks (3 Sizes)

| Block ID | Speed | Ticks |
|----------|-------|-------|
| `small_beer_fermentation_tank` | 1.0x | 2,400 |
| `medium_beer_fermentation_tank` | 1.5x | 1,600 |
| `large_beer_fermentation_tank` | 2.0x | 1,200 |

#### Conditioning Tanks (3 Sizes)

| Block ID | Speed | Duration |
|----------|-------|---------|
| `small_conditioning_tank` | 1.0x | 30-50 days (by type) |
| `medium_conditioning_tank` | 1.5x | 30-50 days (by type) |
| `large_conditioning_tank` | 2.0x | 30-50 days (by type) |

#### Beer Bottling Station
- **ID:** `beer_bottling_station`
- **Time:** 200 ticks per bottle

---

## Cheese Blocks

### Processing Blocks

#### Pasteurization Station
- **ID:** `pasteurization_station`
- **Function:** Pasteurize raw milk (65C)
- **Time:** 400 ticks (20 seconds)

#### Curdling Vat
- **ID:** `curdling_vat`
- **Capacity:** 16 milk buckets
- **Time:** 600 ticks (30 seconds)
- **Output:** 4 curds per milk bucket
- **Requires:** 1 rennet per batch

#### Cheese Presses (3 Sizes)

| Block ID | Capacity | Ticks/Curd |
|----------|----------|-----------|
| `small_cheese_press` | 16 | 400 |
| `medium_cheese_press` | 22 | 300 |
| `large_cheese_press` | 28 | 200 |

#### Aging Caves (3 Sizes)

| Block ID | Capacity | Feature |
|----------|----------|---------|
| `small_aging_cave` | 16 | Temperature-controlled aging |
| `medium_aging_cave` | 22 | Temperature-controlled aging |
| `large_aging_cave` | 28 | Temperature-controlled aging |

#### Packaging Station
- **ID:** `packaging_station`
- **Function:** Package aged cheese into portions

---

## Honey Blocks

### Beekeeping Blocks

| Block ID | Type | Yield/Cycle |
|----------|------|-------------|
| `beehive` | Basic | 1-2 honeycombs |
| `advanced_beehive` | Advanced | 2-3 honeycombs |
| `apiary` | Professional | 3-4 honeycombs |

### Processing Blocks

#### Honey Extractor
- **ID:** `honey_extractor`
- **Capacity:** 64 honeycombs
- **Time:** 400 ticks (20 seconds)
- **Conversion:** 4 honeycombs = 1 honey bucket + 2 beeswax

#### Centrifugal Extractor
- **ID:** `centrifugal_extractor`
- **Capacity:** 64 honeycombs
- **Time:** 200 ticks (10 seconds) - 2x faster
- **Conversion:** 4 honeycombs = 1 honey bucket + 2 beeswax

#### Filtering Station
- **ID:** `filtering_station`
- **Function:** Remove impurities, improve clarity

#### Processing Station
- **ID:** `processing_station`
- **Function:** Apply processing method (Liquid, Creamed, Chunk)

#### Creaming Station
- **ID:** `creaming_station`
- **Function:** Controlled crystallization for creamed honey

#### Aging Chambers (3 Sizes)

| Block ID | Capacity |
|----------|----------|
| `small_aging_chamber` | 12 |
| `medium_aging_chamber` | 18 |
| `large_aging_chamber` | 24 |

#### Bottling Station
- **ID:** `bottling_station`
- **Function:** Seal honey in jars

### Storage

| Block ID | Function |
|----------|---------|
| `honey_storage_barrel` | Bulk honey storage |
| `honey_display_case` | Display honey products |

---

## Chocolate Blocks

### Processing Blocks (10-Step Chain)

#### Roasting Station
- **ID:** `roasting_station`
- **Capacity:** 64 raw cocoa beans
- **Time:** 400 ticks (20 seconds)

#### Winnowing Machine
- **ID:** `winnowing_machine`
- **Function:** Remove cocoa shells, produce nibs
- **Quality:** +1 tier upgrade

#### Grinding Mill
- **ID:** `grinding_mill`
- **Capacity:** 64 nibs
- **Time:** 600 ticks (30 seconds)
- **Quality:** +1 tier upgrade (max GOOD)

#### Pressing Station
- **ID:** `pressing_station`
- **Function:** Extract cocoa butter from mass

#### Conching Machines (3 Sizes)

| Block ID | Capacity | Speed |
|----------|----------|-------|
| `small_conching_machine` | 16 | 1.0x |
| `medium_conching_machine` | 24 | 1.5x |
| `large_conching_machine` | 32 | 2.0x |

#### Tempering Station
- **ID:** `tempering_station`
- **Time:** 800 ticks (40 seconds)
- **Function:** Create crystalline structure for snap

#### Molding Stations (3 Sizes)

| Block ID | Capacity | Ticks/Mold |
|----------|----------|-----------|
| `small_molding_station` | 16 | 200 |
| `medium_molding_station` | 24 | 150 |
| `large_molding_station` | 32 | 100 |

#### Cooling Tunnel
- **ID:** `cooling_tunnel`
- **Time:** 400 ticks (20 seconds)
- **Function:** Prevent fat bloom

#### Enrobing Machine
- **ID:** `enrobing_machine`
- **Function:** Coat/fill chocolate pieces

#### Wrapping Station
- **ID:** `wrapping_station`
- **Function:** Final packaging

### Storage

| Block ID | Function |
|----------|---------|
| `chocolate_storage_cabinet` | Chocolate storage |

---

## Economy Blocks

### ATM (Geldautomat)
- **ID:** `atm`
- **Function:** Banking terminal
- **Features:**
  - Deposit cash
  - Withdraw cash (creates items)
  - Check balance
  - View transaction history
- **Interaction:** Right-click
- **BlockEntity:** ATMBlockEntity
- **Placement:** Any plot

### Cash Block
- **ID:** `cash_block`
- **Function:** Decorative money storage
- **Storage:** 64 cash items
- **Appearance:** Stack of bills
- **BlockEntity:** CashBlockEntity
- **Note:** For decoration, not secure storage

---

## Plot Blocks

### Plot Info Block
- **ID:** `plot_info_block`
- **Function:** Display plot information
- **Shows:**
  - Plot name
  - Owner
  - Price (if for sale)
  - Rent (if for rent)
  - Rating (stars)
- **Interaction:** Right-click to view details
- **No BlockEntity**
- **Placement:** Plot boundaries

---

## Warehouse Blocks

### Warehouse
- **ID:** `warehouse`
- **Function:** Mass storage system
- **Capacity:** 32 slots × 1,024 items = 32,768 items
- **Features:**
  - Auto-delivery every 3 days
  - NPC merchant linking
  - Shop plot linking
  - Revenue tracking
- **GUI:** 32-slot inventory interface
- **BlockEntity:** WarehouseBlockEntity
- **Admin Only:** Yes

---

## Vehicle Blocks

### Fuel Station (Multi-Block)
- **ID:** `fuel_station` (bottom) + `fuel_station_top` (top)
- **Function:** Refuel vehicles
- **Structure:** 2-block tall
- **Fuel Types:** Diesel, gasoline
- **Registry:** FuelStationRegistry
- **Interaction:** Right-click with vehicle nearby
- **No BlockEntity** (registry-based)

### Garage
- **ID:** `garage`
- **Function:** Vehicle storage & repair
- **Capacity:** 4 vehicles
- **Features:**
  - Repair damaged vehicles
  - Change parts
  - Custom paint
- **BlockEntity:** TileEntityGarage
- **Size:** 3x3x3 multi-block

### Bio Diesel (Fluid)
- **ID:** `diesel`
- **Type:** Fluid block
- **Viscosity:** Medium
- **Density:** 850 kg/m³
- **Flammable:** Yes
- **Source:** Fuel stations
- **Use:** Vehicle fuel, coca extraction

---

## Block Categories Summary

### Illegal Production Blocks

| Category | Block Count | Key Features |
|----------|-------------|--------------|
| **Tobacco** | 23 | Pots, plants, drying, fermentation, packaging, lights |
| **Cannabis** | 9 | Plants, drying, trimming, curing, hash, oil |
| **Coca** | 9 | Plants, extraction vats, refineries, crack cooking |
| **Poppy** | 7 | Plants, scoring, pressing, cooking, refining |
| **Meth** | 4 | 4-step production, explosion risk |
| **LSD** | 4 | Precision lab equipment, GUI dosing |
| **MDMA** | 3 | Chemical synthesis, timing minigame |
| **Mushroom** | 4 | Climate control, water system |

### Legal Production Blocks

| Category | Block Count | Key Features |
|----------|-------------|--------------|
| **Coffee** | 12 | 4 pots, 4 plants, wet processing, roasters, grinder, packaging |
| **Wine** | 13 | 4 grapevine pots, crushing, pressing, fermentation, aging, bottling |
| **Beer** | 10 | Malting, mashing, brewing, fermentation, conditioning, bottling |
| **Cheese** | 10 | Pasteurization, curdling, pressing, aging caves, packaging |
| **Honey** | 12 | 3 hive types, 2 extractors, filtering, creaming, aging, bottling |
| **Chocolate** | 13 | Roasting, winnowing, grinding, conching, tempering, molding, cooling, enrobing, wrapping |

### Infrastructure Blocks

| Category | Block Count | Key Features |
|----------|-------------|--------------|
| **Economy** | 2 | Banking, decorative storage |
| **Plot** | 1 | Information display |
| **Warehouse** | 1 | Mass storage, auto-delivery |
| **Vehicle** | 4 | Fuel station, garage, diesel fluid |

**Total: 210+ Blocks** (plant blocks have no items)

---

## Special Features

### Multi-Block Structures
- Packaging Tables: 2x2
- Fuel Station: 2 blocks tall
- Garage: 3x3x3

### Blocks with Light Effects
- Coca Refineries: Light 8/10/12
- Crack Kocher: Light 6
- Reduktionskessel (Meth): Light 4
- Destillations Apparat (LSD): Light 6
- Reaktions Kessel (MDMA): Light 4
- Trocknungs Ofen (MDMA): Light 8
- Grow Lights: Light 12/14/15
- Climate Lamps: Light 10/12/14

### Blocks with GUIs
- ATM: Banking interface
- Warehouse: 32-slot inventory
- Mikro Dosierer (LSD): Dosing control
- Pillen Presse (MDMA): Minigame interface
- All processing blocks: Progress bars

### Blocks with Minigames
- Pillen Presse (MDMA): Timing-based pill pressing

### Dangerous Blocks
- Reduktionskessel (Meth): **EXPLOSION RISK**
- All chemical processing: Handle with care

---

[⬆ Back to Wiki Home](Home.md) | [Items Reference](Items.md) | [Commands Reference](Commands.md)
