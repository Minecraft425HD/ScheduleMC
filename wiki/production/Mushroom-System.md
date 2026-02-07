# Mushroom Production System

A beginner-friendly 4-step cultivation system featuring 3 psilocybin strains, multi-flush harvesting, tiered climate lamps, and automated water tanks.

**Stats at a Glance:**

| Attribute | Value |
|-----------|-------|
| Strains | 3 (Cubensis, Azurescens, Mexicana) |
| Production Steps | 4 |
| Total Items | 15 |
| Total Blocks | 4 |
| Quality Tiers | Poor, Good, Very Good, Legendary |
| Unique Feature | Multi-flush harvesting (2-5 harvests per substrate) |

---

## Overview

Mushroom production is the most beginner-friendly system in ScheduleMC. It features the fastest growth cycles of any plant-based system, a unique multi-flush mechanic that allows multiple harvests from one substrate, and automated climate control via tiered lamps and water tanks. The system requires darkness for incubation and controlled light for fruiting, making build design an important part of the strategy.

---

## Strains

| Strain | Type | Potency | Growth Time | Water Use | Max Flushes | Temperature | Difficulty |
|--------|------|---------|-------------|-----------|-------------|-------------|------------|
| **Cubensis** | Balanced | 100% | 5 sec (100 ticks) | 1.0x | 4 | Neutral | Medium |
| **Azurescens** | Premium | **200%** | 9 sec (180 ticks) | 1.5x | 3 | Cold | Hard |
| **Mexicana** | Fast | 60% | **3 sec** (60 ticks) | 0.7x | **5** | Warm | Easy |

### Strain Details

**Cubensis** -- The all-rounder. Standard potency, reasonable growth time, neutral temperature (no climate lamp needed). Good for learning the system.

**Azurescens** -- The premium strain. Highest potency at 200%, but slowest growth, highest water demand, fewest flushes, and requires cold temperature. Produces the most valuable mushrooms in the game.

**Mexicana** -- The speed strain. Fastest growth at only 3 seconds per cycle, lowest water use, and the most flushes at 5. Lowest potency but highest volume output.

---

## Production Chain

```
  [1. INOCULATION]         Spore Syringe
   Spore Syringe    -->    + Mist Bag (in pot)
   + Mist Bag               Mycelium established
   (3 sizes: S/M/L)
       |
       v
  [2. GROWING]             Growth Stages 0-7
   DARKNESS required  -->  Incubation: stages 0-3
   for incubation           (dark, no water)
   LOW LIGHT for      -->  Fruiting: stages 4-7
   fruiting                 (low light + water)
   Climate Lamps            Climate Lamps (3 tiers)
   (3 tiers)                + Water Tank
       |
       v
  [3. HARVESTING]          Multi-Flush System
   Shift + Right-click -->  Receive fresh mushrooms
   Stage 7 (mature)         Culture resets to stage 3
   Repeat 2-5 times!        Yield decreases per flush
       |
       v
   Fresh Mushrooms
       |
       v
  [4. DRYING]              Drying Rack
   Time-based         -->  Shared with other systems
   Quality preserved        3 sizes available
       |
       v
   Dried Mushrooms
   (Final Product)
```

---

## Step 1: Inoculation

Prepare the substrate and introduce spores.

### Process

1. Place a Plant Pot
2. Right-click the pot with a Mist Bag to add substrate
3. Right-click the prepared pot with a Spore Syringe to inoculate

### Mist Bags (3 Sizes)

| Size | Price | Capacity | Best For |
|------|-------|----------|----------|
| **Small Mist Bag** | 15 | 1 culture | Single pot |
| **Medium Mist Bag** | 35 | 2 cultures | Small operation |
| **Large Mist Bag** | 60 | 3 cultures | Large operation |

### Spore Syringes (1 per Strain)

| Syringe | Price | Strain |
|---------|-------|--------|
| Cubensis Spore Syringe | 30 | Cubensis |
| Azurescens Spore Syringe | 60 | Azurescens |
| Mexicana Spore Syringe | 20 | Mexicana |

---

## Step 2: Growing

Growth occurs in two phases: incubation (stages 0-3) and fruiting (stages 4-7).

### Phase A: Incubation (Stages 0-3)

Mycelium colonizes the substrate. Requires **darkness** -- the pot must be below the strain's light threshold.

| Strain | Max Light Level |
|--------|----------------|
| Mexicana | < 5 (most tolerant) |
| Cubensis | < 4 |
| Azurescens | < 3 (strictest) |

No water is needed during incubation. Build in a dark, enclosed room.

### Phase B: Fruiting (Stages 4-7)

Mushrooms form and grow. Requires low-to-moderate light and **water** (from Wassertank or manual watering).

| Strain | Max Light Level | Water Multiplier |
|--------|----------------|-----------------|
| Mexicana | < 8 (most tolerant) | 0.7x (low) |
| Cubensis | < 7 | 1.0x (standard) |
| Azurescens | < 5 (strictest) | 1.5x (high) |

### Climate Lamps (3 Tiers)

Climate lamps provide temperature control for strains that require it. Place adjacent to pots.

| Tier | Name | Auto Mode | Growth Bonus | Quality Bonus | Price |
|------|------|-----------|-------------|--------------|-------|
| **Tier 1** | Klimalampe Small | No (manual toggle: OFF/COLD/WARM) | None | None | ~200 |
| **Tier 2** | Auto-Klimalampe Medium | Yes (auto-detects strain) | +10% | None | ~500 |
| **Tier 3** | Premium-Klimalampe Large | Yes (auto-detects strain) | **+25%** | **+10%** | ~1,200 |

Temperature modes:
- **OFF** -- Neutral (for Cubensis)
- **COLD** -- Cold environment (for Azurescens)
- **WARM** -- Warm environment (for Mexicana)

Tier 2 and Tier 3 lamps auto-detect the strain of adjacent mushroom pots and switch modes automatically.

### Water Tank (Wassertank)

| Property | Value |
|----------|-------|
| Capacity | 10,000 units (10 water buckets) |
| Rate | 1 unit per 10 ticks to adjacent pots |
| Range | Horizontally adjacent blocks only |
| Refill | Right-click with water bucket (+1,000 units) |

Place water tanks next to your pots for automated watering during the fruiting phase.

---

## Step 3: Harvesting (Multi-Flush System)

The mushroom system's signature mechanic: harvest multiple times from the same substrate.

### How to Harvest

1. Wait until growth reaches stage 7 (fully mature)
2. Shift + Right-click with an empty hand
3. Receive Fresh Mushrooms
4. Culture resets to **stage 3** (skips incubation)
5. Regrowth begins immediately (fruiting phase only)
6. Repeat until max flushes reached

### Flush Yield

Each successive flush produces less:

| Flush | Yield Multiplier |
|-------|-----------------|
| 1st | 100% |
| 2nd | 85% |
| 3rd | 70% |
| 4th | 55% |
| 5th | 40% |

### Total Yield Example (Cubensis, Good quality, base 6g)

```
Flush 1: 6g x 1.00 = 6.0g
Flush 2: 6g x 0.85 = 5.1g
Flush 3: 6g x 0.70 = 4.2g
Flush 4: 6g x 0.55 = 3.3g
--------------------------
Total:              18.6g from one substrate
```

### Substrate Exhaustion

After reaching the maximum flush count (3/4/5 depending on strain), the culture is exhausted. Remove it, add a new mist bag, and re-inoculate to start fresh.

---

## Step 4: Drying

Dry fresh mushrooms for stable storage and sale. Uses the shared drying rack system.

### Drying Racks

| Size | Capacity | Time per Batch |
|------|----------|---------------|
| **Small Drying Rack** | 3 slots | 60 seconds |
| **Medium Drying Rack** | 6 slots | 60 seconds |
| **Big Drying Rack** | 9 slots | 60 seconds |

Quality is fully preserved through drying. Legendary fresh mushrooms become Legendary dried mushrooms.

---

## Quality Tiers

| Quality | Price Multiplier | Yield Multiplier |
|---------|-----------------|-----------------|
| **Poor (Schlecht)** | 1.0x | 1.0x |
| **Good (Gut)** | 1.5x | 1.5x |
| **Very Good (Sehr Gut)** | 2.5x | 2.5x |
| **Legendary (Legendaer)** | **5.0x** | **5.0x** |

Default starting quality is Good. Quality Boosters give a 15% chance per growth stage to upgrade.

### Yield Formula

```
finalYield = min(10, baseYield * qualityMultiplier * fertilizerBonus * flushPenalty * randomVariation)

baseYield = 6 (all strains)
fertilizerBonus = 1.67 (if applied)
randomVariation = +/- 20%
Cap = 10g maximum per harvest
```

---

## Items Table (15 Items)

| Item | Description | How Obtained |
|------|-------------|-------------|
| Cubensis Spore Syringe | Balanced strain spores | Purchase (30) |
| Azurescens Spore Syringe | Premium strain spores | Purchase (60) |
| Mexicana Spore Syringe | Fast strain spores | Purchase (20) |
| Small Mist Bag | Substrate for 1 culture | Purchase (15) |
| Medium Mist Bag | Substrate for 2 cultures | Purchase (35) |
| Large Mist Bag | Substrate for 3 cultures | Purchase (60) |
| Fresh Cubensis Mushrooms | Freshly harvested (perishable) | Harvesting mature Cubensis |
| Fresh Azurescens Mushrooms | Freshly harvested (perishable) | Harvesting mature Azurescens |
| Fresh Mexicana Mushrooms | Freshly harvested (perishable) | Harvesting mature Mexicana |
| Dried Cubensis Mushrooms | Shelf-stable final product | Drying Rack |
| Dried Azurescens Mushrooms | Shelf-stable final product | Drying Rack |
| Dried Mexicana Mushrooms | Shelf-stable final product | Drying Rack |
| Fertilizer | Increases yield (+67%) | Crafting / purchase |
| Quality Booster | 15% quality upgrade per stage | Crafting / purchase |
| Water Bucket | Fills Wassertank (+1,000 units) | Crafting / water source |

---

## Blocks Table (4 Blocks)

| Block | Description | Function |
|-------|-------------|----------|
| Klimalampe Small | Manual climate lamp (Tier 1) | Manual toggle OFF/COLD/WARM; no growth bonus |
| Auto-Klimalampe Medium | Automatic climate lamp (Tier 2) | Auto-detects strain; +10% growth speed |
| Premium-Klimalampe Large | Premium climate lamp (Tier 3) | Auto-detects strain; +25% growth speed, +10% quality |
| Wassertank (Water Tank) | Automated watering system | 10,000 unit capacity; waters adjacent pots automatically |

---

## Quality Modifiers

| Modifier | Stage | Effect |
|----------|-------|--------|
| Quality Booster | Growing | 15% chance per stage to upgrade quality tier |
| Premium-Klimalampe | Growing | +10% quality chance |
| Fertilizer | Growing | +67% yield but does NOT affect quality |
| Strain Potency | Pricing | Azurescens = 2.0x, Cubensis = 1.0x, Mexicana = 0.6x price modifier |
| Flush Number | Harvesting | Each successive flush yields 15% less |
| Quality Tier | Harvesting | Legendary = 5.0x yield and price multiplier |

### Reaching Legendary

Use Quality Boosters on every culture. With a 15% chance per growth stage across 8 stages, roughly 20% of cultures will reach Legendary. Combine with the Premium-Klimalampe's +10% quality bonus for better odds.

---

## Tips & Tricks

1. **Start with Mexicana.** It is the easiest strain: fastest growth (3 seconds), lowest water use, most flushes (5), and most tolerant light requirements.
2. **Build a dedicated dark room.** Fully enclosed, no windows, no torches inside. Verify light levels are below your strain's threshold before planting.
3. **Set up separate climate zones.** If growing multiple strains, create separate rooms: Cold room for Azurescens, Warm room for Mexicana, Neutral room for Cubensis.
4. **Invest in the Premium-Klimalampe early.** The +25% growth speed and +10% quality bonus pay for themselves quickly. It is the single best equipment investment.
5. **Place Wassertanks adjacent to every pot.** One tank can water multiple adjacent pots. Fill before starting a batch and monitor levels.
6. **Harvest ALL flushes before replacing substrate.** Even the 5th flush at 40% yield is essentially free product from substrate you already paid for.
7. **Azurescens is the end-game money maker.** At 200% potency, Legendary Azurescens mushrooms are worth 10x what Legendary Mexicana produce per gram.
8. **Dry immediately after harvest.** Fresh mushrooms are perishable. A Big Drying Rack (9 slots) prevents spoilage in large operations.
9. **Use Fertilizer for quantity, Quality Boosters for quality.** They serve different purposes. On Mexicana volume runs, use fertilizer. On Azurescens premium runs, use quality boosters.
10. **Multi-flush is your compounding advantage.** A single Cubensis inoculation yields ~18.6g total across 4 flushes. That is 3x more efficient than any single-harvest crop.

---

*See also: [Cannabis System](Cannabis-System.md) | [Tobacco System](Tobacco-System.md) | [Production Systems Overview](../Production-Systems.md)*
