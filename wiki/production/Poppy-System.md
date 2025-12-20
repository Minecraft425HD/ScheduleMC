# Poppy/Opium Production System

<div align="center">

**5-Step Chemical Refinement Chain - Scoring & Processing**

Multi-method extraction with automated refining to heroin

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production Systems](../Production-Systems.md)

</div>

---

## 📋 Quick Reference

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐⭐⭐⭐⭐ (5/5 - Expert) |
| **Steps** | 5 (Plant → Harvest → Score → Cook → Refine) |
| **Strains** | 3 (Afghan, Turkish, Indian) |
| **Duration** | 22-23 minutes (full cycle to heroin) |
| **Profitability** | ⭐⭐⭐⭐⭐ (5/5 - Highest) |
| **Quality Tiers** | Poor, Good, Very Good, Legendary |
| **Final Products** | Raw Opium, Morphine, Heroin |
| **Unique Mechanic** | Multiple extraction methods, quality inheritance |
| **Equipment** | Scoring tools, Ritzmaschine, Presse, Kochstation, Raffinerie |

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Strains & Seeds](#strains--seeds)
3. [Growing Process](#growing-process)
4. [Extraction Methods](#extraction-methods)
5. [Morphine Production](#morphine-production)
6. [Heroin Refining](#heroin-refining)
7. [Quality System](#quality-system)
8. [Equipment Guide](#equipment-guide)
9. [Profitability Analysis](#profitability-analysis)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

---

## Overview

Poppy/Opium production is the most complex and profitable system in ScheduleMC, featuring three extraction methods, quality-based yield multipliers, and a complete chemical refinement chain from raw opium to pharmaceutical-grade heroin.

### Production Flow

```
1. PLANT → Poppy seeds in pot
2. GROW → 8 growth stages (4-8 seconds)
3. HARVEST → Poppy pods
4. EXTRACT → Raw opium (3 methods)
   ├─ Manual: Scoring Knife
   ├─ Automated: Ritzmaschine
   └─ High-Yield: Opium Presse
5. COOK → Morphine (Kochstation)
6. REFINE → Heroin (Heroin Raffinerie)
```

### Why Choose Poppy?

**Advantages:**
✅ Highest profit potential
✅ Quality affects yield (up to 1.6× multiplier)
✅ Multiple extraction methods
✅ 20% quality upgrade during refining
✅ Potency multipliers (Afghan = 1.5×)

**Challenges:**
⏰ Longest total production time (22-23 min)
📚 Most complex processing chain
💰 Highest equipment investment
🔬 Multiple processing stages
⚖️ Resource management (water, fuel, diesel)

---

## Strains & Seeds

### 3 Poppy Strains

#### 1. Afghanisch (Afghan) 💎

**Characteristics:**
- **Color:** Dark Red (§4)
- **Type:** Premium, highest potency
- **Region:** Afghan highlands
- **Best For:** Maximum profit

**Seeds:** `afghanisch_poppy_seeds`
- **Price:** 50€ per seed (most expensive)
- **Growth Time:** 160 ticks (8 seconds)
- **Growth Stages:** 0-7 (8 stages)
- **Water Consumption:** 1.2× (high)
- **Base Yield:** 6 poppy pods
- **Potency Multiplier:** 1.5× (150%)

**Market:**
- Highest seed price
- Slowest growth
- **Best potency** (50% bonus)
- **Best quality chances**
- Highest final price

**Quality Improvement:**
- Base chance: 25% per stage (3, 5, 7)
- With Potency: 25% × 1.5 = **37.5% per stage**
- More likely to reach LEGENDAER

**Ticks per Stage:** 160 / 8 = **20 ticks** (~1 second)

---

#### 2. Tuerkisch (Turkish) ⚖️

**Characteristics:**
- **Color:** Gold (§6)
- **Type:** Balanced, standard
- **Region:** Turkish fields
- **Best For:** General production

**Seeds:** `tuerkisch_poppy_seeds`
- **Price:** 35€ per seed
- **Growth Time:** 120 ticks (6 seconds)
- **Growth Stages:** 0-7 (8 stages)
- **Water Consumption:** 1.0× (standard)
- **Base Yield:** 6 poppy pods
- **Potency Multiplier:** 1.0× (100%)

**Market:**
- Medium seed price
- Medium growth time
- Standard potency
- Balanced choice

**Quality Improvement:**
- Base chance: 25% per stage
- With Potency: 25% × 1.0 = **25% per stage**
- Standard quality progression

**Ticks per Stage:** 120 / 8 = **15 ticks** (~0.75 seconds)

---

#### 3. Indisch (Indian) ⚡

**Characteristics:**
- **Color:** Purple (§5)
- **Type:** Fast-growing, volume
- **Region:** Indian fields
- **Best For:** High-volume production

**Seeds:** `indisch_poppy_seeds`
- **Price:** 20€ per seed (cheapest)
- **Growth Time:** 80 ticks (4 seconds)
- **Growth Stages:** 0-7 (8 stages)
- **Water Consumption:** 0.8× (low)
- **Base Yield:** 6 poppy pods
- **Potency Multiplier:** 0.8× (80%)

**Market:**
- Lowest seed price
- **Fastest growth** (2× faster than Afghan)
- Lowest potency (-20%)
- Volume over quality

**Quality Improvement:**
- Base chance: 25% per stage
- With Potency: 25% × 0.8 = **20% per stage**
- Lower quality chances

**Ticks per Stage:** 80 / 8 = **10 ticks** (~0.5 seconds)

---

### Strain Comparison

| Feature | Afghan | Turkish | Indian |
|---------|--------|---------|--------|
| **Seed Price** | 50€ | 35€ | 20€ |
| **Growth Time** | 8 sec | 6 sec | 4 sec |
| **Speed** | Slow | Medium | Fast |
| **Water Use** | 1.2× | 1.0× | 0.8× |
| **Potency** | 150% | 100% | 80% |
| **Quality Chance** | 37.5% | 25% | 20% |
| **Base Price** | Highest | Medium | Lowest |
| **Best For** | Premium | Balanced | Volume |

---

## Growing Process

### Step 1: Planting

**Requirements:**
- Poppy seeds (Afghan, Turkish, or Indian)
- Plant Pot (Terracotta, Ceramic, Iron, or Golden)
- Soil (15 units total)
- Water (100 units capacity)

**Process:**
1. Place pot in desired location
2. Add soil to pot (minimum 15 units)
3. Add water to pot (100 units)
4. Right-click pot with poppy seeds
5. Seed planted, growth begins

**Initial State:**
```
Growth Stage: 0/7 (Seedling)
Strain: Afghanisch/Tuerkisch/Indisch
Quality: SCHLECHT (base)
Progress: 0%
```

---

### Growth Stages

**8 Growth Stages (0-7):**

| Stage | Name | Height | Visual | Soil Use | Water Use |
|-------|------|--------|--------|----------|-----------|
| 0 | Seedling | 1 block | Tiny | 1/7 total | 1/7 total |
| 1 | Vegetative 1 | 1 block | Small | 1/7 total | 1/7 total |
| 2 | Vegetative 2 | 1 block | Growing | 1/7 total | 1/7 total |
| 3 | Pre-Flower | 1 block | Larger | 1/7 total | 1/7 total |
| 4 | Flowering 1 | **2 blocks** | Tall | 1/7 total | 1/7 total |
| 5 | Flowering 2 | **2 blocks** | Buds | 1/7 total | 1/7 total |
| 6 | Mature | **2 blocks** | Pods forming | 1/7 total | 1/7 total |
| 7 | **HARVEST** | **2 blocks** | Ready | 1/7 total | 1/7 total |

**Plant Height:**
- Stages 0-3: Single block
- Stages 4-7: **Two blocks tall** (DoubleBlockHalf system)

---

### Quality Improvement During Growth

**Critical Feature:** Quality improves at stages 3, 5, and 7

**Base Chance:** 25% per improvement stage

**Potency Modifier:**
```java
effectiveChance = baseChance × potencyMultiplier

Afghan: 25% × 1.5 = 37.5%
Turkish: 25% × 1.0 = 25%
Indian: 25% × 0.8 = 20%
```

**With Fertilizer:**
```java
fertilizerBonus = +15%

Afghan with Fert: 37.5% + 15% = 52.5%
Turkish with Fert: 25% + 15% = 40%
Indian with Fert: 20% + 15% = 35%
```

**With Quality Booster:**
```java
qualityBoosterBonus = +20%

Afghan with Fert + QB: 37.5% + 15% + 20% = 72.5%
```

**Quality Progression Example (Afghan):**
```
Stage 0: SCHLECHT (start)
Stage 3: 37.5% chance → GUT
Stage 5: 37.5% chance → SEHR_GUT
Stage 7: 37.5% chance → LEGENDAER

With all bonuses (72.5% per stage):
High probability of LEGENDAER quality!
```

---

### Growth Times

**Base Growth Times:**

| Strain | Total Time | Per Stage | Real-Time |
|--------|------------|-----------|-----------|
| **Afghan** | 160 ticks | 20 ticks | ~8 seconds |
| **Turkish** | 120 ticks | 15 ticks | ~6 seconds |
| **Indian** | 80 ticks | 10 ticks | ~4 seconds |

**With Growth Booster (30% faster):**
```java
boostedTime = growthTicks × 0.7

Afghan: 160 × 0.7 = 112 ticks (~5.6 seconds)
Turkish: 120 × 0.7 = 84 ticks (~4.2 seconds)
Indian: 80 × 0.7 = 56 ticks (~2.8 seconds)
```

---

### Step 2: Harvesting

**When to Harvest:**
- Stage 7/7 (fully mature)
- Visual: 2-block tall, fully developed pods
- Tooltip: "Ready to harvest"

**Harvest Yields:**

**Quality Yield Multipliers:**
```java
SCHLECHT: 0.7×
GUT: 1.0×
SEHR_GUT: 1.3×
LEGENDAER: 1.6×
```

**With Fertilizer:**
```java
fertilizer Bonus = +0.67×
```

**Yield Formula:**
```java
baseYield = 6 pods
qualityMultiplier = quality.getYieldMultiplier()
fertilizerMultiplier = fertilized ? 1.67 : 1.0

finalYield = min(10, baseYield × qualityMultiplier × fertilizerMultiplier)
```

**Examples:**

**SCHLECHT Quality:**
```
6 × 0.7 = 4.2 → 4 pods
With Fertilizer: 6 × 0.7 × 1.67 = 7.0 → 7 pods
```

**GUT Quality:**
```
6 × 1.0 = 6 pods
With Fertilizer: 6 × 1.0 × 1.67 = 10 pods (capped)
```

**LEGENDAER Quality:**
```
6 × 1.6 = 9.6 → 10 pods (capped at 10)
With Fertilizer: 6 × 1.6 × 1.67 = 16 → 10 pods (capped)
```

**Partial Harvest (Stage 4-6):**
- Yields: 50% of formula
- Quality: SCHLECHT (downgraded)
- Not recommended

**How to Harvest:**
1. Right-click fully mature plant (Stage 7)
2. Receive Poppy Pods (strain + quality preserved)
3. Plant drops, pot becomes empty
4. Replant immediately

---

## Extraction Methods

### Method 1: Manual Extraction (Scoring Knife)

**Tool:** Scoring Knife (`scoring_knife`)
- **Durability:** 128 uses
- **Crafting:** Handled via crafting grid
- **Speed:** Instant (manual)
- **Yield:** 1-3 Raw Opium per pod (quality-based)

**How to Use:**
1. Craft Scoring Knife
2. Place Poppy Pod in crafting grid
3. Place Scoring Knife in grid
4. Receive Raw Opium
5. Knife returned with -1 durability

**Yield by Quality:**
```
SCHLECHT: 1 Raw Opium per pod
GUT: 2 Raw Opium per pod
SEHR_GUT: 2 Raw Opium per pod
LEGENDAER: 3 Raw Opium per pod
```

**Pros:**
- ✅ No power/fuel required
- ✅ Instant processing
- ✅ Portable (anywhere)
- ✅ Cheap (only knife durability)

**Cons:**
- ❌ Manual labor (click-intensive)
- ❌ Knife breaks after 128 uses
- ❌ Lowest yield

**Cost Analysis:**
```
Scoring Knife: ~100€
Uses: 128
Cost per use: ~0.78€

Processing 10 LEGENDAER pods:
Yield: 10 × 3 = 30 Raw Opium
Cost: 10 × 0.78 = 7.8€
Time: ~10 seconds (manual clicking)
```

---

### Method 2: Automated Extraction (Ritzmaschine)

**Equipment:** Ritzmaschine (`ritzmaschine`)
- **Capacity:** 8 slots
- **Process Time:** 100 ticks (5 seconds) per pod
- **Power:** Requires Redstone signal
- **Yield:** 1-3 Raw Opium (same as Scoring Knife)

**Block Properties:**
- Hardness: 3.5F
- Blast Resistance: 6.0F
- Material: Iron block
- Light Level: 0

**How to Use:**
1. Place Ritzmaschine
2. Apply Redstone signal (lever, button, etc.)
3. Right-click with Poppy Pods to add
4. Wait 5 seconds per pod
5. Shift+right-click to extract Raw Opium

**Yield by Quality:**
```
SCHLECHT: 1 Raw Opium per pod
GUT: 2 Raw Opium per pod
SEHR_GUT: 2 Raw Opium per pod
LEGENDAER: 3 Raw Opium per pod
```

**Pros:**
- ✅ Automated (set and forget)
- ✅ No durability loss
- ✅ Processes 8 pods simultaneously
- ✅ Reliable

**Cons:**
- ❌ Requires Redstone power
- ❌ Slower than manual (5 sec/pod)
- ❌ Same yield as manual

**Processing Example:**
```
Input: 8 LEGENDAER Afghan Pods
Time: 8 × 100 ticks = 800 ticks (40 seconds)
Output: 8 × 3 = 24 Raw Opium
Cost: Redstone power only
```

---

### Method 3: High-Yield Extraction (Opium Presse)

**Equipment:** Opium Presse (`opium_presse`)
- **Capacity:** 16 slots (2× Ritzmaschine!)
- **Process Time:** 80 ticks (4 seconds) per pod
- **Fuel:** Diesel required (1,000 mB max tank)
- **Yield:** **2-5 Raw Opium** (HIGHER than other methods!)

**Block Properties:**
- Hardness: 4.0F
- Blast Resistance: 6.0F
- Material: Metal
- Light Level: 0
- Diesel Consumption: 1 per 20 ticks

**How to Use:**
1. Place Opium Presse
2. Fill with Diesel (Diesel Canister)
3. Right-click with Poppy Pods to add
4. Wait 4 seconds per pod
5. Shift+right-click to extract Raw Opium

**Yield by Quality (SUPERIOR):**
```
SCHLECHT: 2 Raw Opium per pod (+100% vs manual!)
GUT: 3 Raw Opium per pod (+50% vs manual)
SEHR_GUT: 4 Raw Opium per pod (+100% vs manual!)
LEGENDAER: 5 Raw Opium per pod (+67% vs manual!)
```

**Diesel Consumption:**
```java
dieselPerPod = 80 ticks / 20 = 4 diesel units

Examples:
1 pod = 4 diesel
16 pods = 64 diesel
100 pods = 400 diesel
```

**Pros:**
- ✅ **HIGHEST YIELD** (2-5 per pod)
- ✅ **FASTEST** (4 sec vs 5 sec)
- ✅ **LARGEST CAPACITY** (16 slots)
- ✅ Fully automated

**Cons:**
- ❌ Requires diesel fuel
- ❌ Higher equipment cost
- ❌ Must manage diesel supply

**Processing Example:**
```
Input: 16 LEGENDAER Afghan Pods
Diesel: 64 units (~6.4€)
Time: 16 × 80 ticks = 1,280 ticks (64 seconds)
Output: 16 × 5 = 80 Raw Opium

vs Ritzmaschine (same input):
Output: 16 × 3 = 48 Raw Opium
Gain: +32 Raw Opium (+67% more!)
```

---

### Extraction Method Comparison

| Method | Capacity | Time/Pod | Yield Range | Power | Best For |
|--------|----------|----------|-------------|-------|----------|
| **Scoring Knife** | Manual | Instant | 1-3 | None | Early game, portable |
| **Ritzmaschine** | 8 pods | 5 sec | 1-3 | Redstone | Mid-game automation |
| **Opium Presse** | 16 pods | 4 sec | **2-5** | Diesel | **End-game (best!)** |

**Recommendation:** Always use Opium Presse for maximum yield!

---

## Morphine Production

### Step 4: Cooking to Morphine

**Purpose:** Cook raw opium into morphine base

**Input:** Raw Opium
**Output:** Morphine (1:1 ratio)
**Duration:** 200 ticks (10 seconds) per unit
**Equipment:** Kochstation

---

### Kochstation (Cooking Station)

**Block:** `kochstation`
- **Capacity:** 8 slots (parallel processing)
- **Cook Time:** 200 ticks (10 seconds) per slot
- **Requirements:**
  - Water: Max 1,000 units, consumes 1 per 20 ticks
  - Fuel: Max 500 units, consumes 1 per 20 ticks

**Block Properties:**
- Hardness: 3.0F
- Blast Resistance: 6.0F
- Material: Metal
- Light Level: 0

---

### Cooking Mechanics

**Resource Consumption:**
```java
waterPerTick = 1 per 20 ticks
fuelPerTick = 1 per 20 ticks
ticksPerOpium = 200

waterPerOpium = 200 / 20 = 10 water units
fuelPerOpium = 200 / 20 = 10 fuel units

Examples:
1 opium = 10 water + 10 fuel
8 opium = 80 water + 80 fuel
50 opium = 500 water + 500 fuel
```

**Processing:**
- Each slot processes independently
- If water or fuel runs out, processing pauses
- Progress preserved during pause
- Quality inherited from Raw Opium
- Strain inherited (Afghan/Turkish/Indian)

---

### Using Kochstation

**Steps:**
1. Place Kochstation
2. Fill water tank (1,000 max)
3. Add fuel (coal, etc.)
4. Right-click with Raw Opium to add
5. Wait 10 seconds per unit
6. Shift+right-click to extract Morphine

**Example:**
```
Input: 8 SEHR_GUT Afghan Raw Opium
Water: 80 units consumed
Fuel: 80 units (8 coal)
Time: 8 × 200 ticks = 1,600 ticks (80 seconds)

Output: 8 SEHR_GUT Afghan Morphine
Quality: Preserved (SEHR_GUT → SEHR_GUT)
Strain: Preserved (Afghan → Afghan)
```

---

## Heroin Refining

### Step 5: Refining to Heroin

**Purpose:** Chemically refine morphine into pharmaceutical heroin

**Input:** Morphine
**Output:** Heroin (1:1 ratio)
**Duration:** 300 ticks (15 seconds) per unit
**Equipment:** Heroin Raffinerie
**Quality Upgrade:** 20% chance to upgrade +1 tier

---

### Heroin Raffinerie (Heroin Refinery)

**Block:** `heroin_raffinerie`
- **Capacity:** 8 slots (parallel processing)
- **Refine Time:** 300 ticks (15 seconds) per slot
- **Requirements:**
  - Fuel: Max 800 units, consumes 1 per 20 ticks

**Block Properties:**
- Hardness: 4.0F
- Blast Resistance: 6.0F
- Material: Metal
- Light Level: 0

---

### Refining Mechanics

**Fuel Consumption:**
```java
fuelPerTick = 1 per 20 ticks
ticksPerMorphine = 300

fuelPerMorphine = 300 / 20 = 15 fuel units

Examples:
1 morphine = 15 fuel
8 morphine = 120 fuel
50 morphine = 750 fuel
```

**Quality Upgrade System:**
```java
if (quality != LEGENDAER && random.nextFloat() < 0.2) {
    outputQuality = inputQuality.upgrade();
} else {
    outputQuality = inputQuality;
}

Examples:
GUT morphine → 20% SEHR_GUT, 80% GUT
SEHR_GUT morphine → 20% LEGENDAER, 80% SEHR_GUT
LEGENDAER morphine → 100% LEGENDAER (can't upgrade)
```

---

### Using Heroin Raffinerie

**Steps:**
1. Place Heroin Raffinerie
2. Add fuel (coal, charcoal, etc.)
3. Right-click with Morphine to add
4. Wait 15 seconds per unit
5. Shift+right-click to extract Heroin

**Example:**
```
Input: 8 SEHR_GUT Turkish Morphine
Fuel: 120 units (12 coal)
Time: 8 × 300 ticks = 2,400 ticks (120 seconds / 2 min)

Output (expected):
→ ~1-2 LEGENDAER Turkish Heroin (20% chance)
→ ~6-7 SEHR_GUT Turkish Heroin (80% no upgrade)

Total: 8 Heroin (mixed qualities)
```

---

### Quality Inheritance Chain

**Complete Chain:**
```
Plant Quality → Pod Quality → Opium Quality → Morphine Quality → Heroin Quality
                                                                      ↓
                                                                20% upgrade
```

**Example (Afghan with Golden Pot):**
```
Stage 0: SCHLECHT (start)
Stage 3: 52.5% → GUT (Afghan bonus + Fertilizer)
Stage 5: 52.5% → SEHR_GUT
Stage 7: 52.5% → LEGENDAER

Harvest: 10 LEGENDAER Afghan Pods
Extract (Presse): 10 × 5 = 50 LEGENDAER Raw Opium
Cook: 50 LEGENDAER Morphine
Refine: 50 LEGENDAER Heroin (can't upgrade further)

Result: 50 LEGENDAER Heroin from 1 plant!
```

---

## Quality System

### Quality Tiers

**4 Quality Levels:**

| Quality | Price Multiplier | Color | Description |
|---------|------------------|-------|-------------|
| **SCHLECHT** (Poor) | 1.0x | §c (red) | Niedrige Qualität |
| **GUT** (Good) | 1.5x | §e (yellow) | Gute Qualität |
| **SEHR_GUT** (Very Good) | 2.5x | §a (green) | Sehr gute Qualität |
| **LEGENDAER** (Legendary) | 5.0x | §6§l (gold bold) | Legendäre Qualität |

---

### Quality Affects

**1. Harvest Yield:**
```
SCHLECHT: 0.7× (4 pods)
GUT: 1.0× (6 pods)
SEHR_GUT: 1.3× (8 pods)
LEGENDAER: 1.6× (10 pods capped)
```

**2. Extraction Yield (Opium Presse):**
```
SCHLECHT: 2 opium/pod
GUT: 3 opium/pod
SEHR_GUT: 4 opium/pod
LEGENDAER: 5 opium/pod
```

**3. Final Price:**
```
Price = basePrice × potencyMultiplier × qualityMultiplier

Afghan LEGENDAER:
(50 × 3.5) × 1.5 × 5.0 = 1,312.5€ per heroin!
```

---

### Achieving Legendary Quality

**Method 1: Afghan + Fertilizer + Quality Booster**
```
Afghan base: 37.5% per stage
Fertilizer: +15%
Quality Booster: +20%
Total: 72.5% per stage

Stage 3: 72.5% → GUT
Stage 5: 72.5% → SEHR_GUT
Stage 7: 72.5% → LEGENDAER

Expected: Very high LEGENDAER rate
```

**Method 2: Golden Pot**
```
Golden Pot: +1 tier on harvest
Start: SCHLECHT
Growth upgrades: → GUT → SEHR_GUT
Harvest bonus: +1 tier
Result: LEGENDAER
```

**Method 3: Refining Luck**
```
SEHR_GUT morphine → Raffinerie → 20% LEGENDAER
Run large batches to get ~20% LEGENDAER output
```

---

## Equipment Guide

### Essential Equipment (Minimum)

**Starting Setup:**
1. ✅ 1× Terracotta Pot
2. ✅ 1× Scoring Knife (128 uses)
3. ✅ 1× Kochstation
4. ✅ 1× Heroin Raffinerie
5. ✅ Water source (1,000 units)
6. ✅ Fuel supply (500 units)

**Cost:** ~2,500€
**Capacity:** 1 plant, manual scoring
**Production:** Slow but functional

---

### Intermediate Setup

**Recommended:**
1. ✅ 4× Iron Pots
2. ✅ 1× Ritzmaschine (8 pods automated)
3. ✅ 1× Kochstation
4. ✅ 1× Heroin Raffinerie
5. ✅ Redstone power system
6. ✅ Water/fuel storage (2,000 units)

**Cost:** ~8,000€
**Capacity:** 4 plants, 8 pods automated
**Production:** Moderate

---

### Advanced Setup (Professional)

**Professional:**
1. ✅ 8× Golden Pots (LEGENDAER quality)
2. ✅ 2× Opium Presse (32 pods, high yield)
3. ✅ 2× Kochstation (16 opium parallel)
4. ✅ 2× Heroin Raffinerie (16 morphine parallel)
5. ✅ Diesel storage (4,000+ units)
6. ✅ Water/fuel storage (5,000+ units)
7. ✅ Fertilizer + Quality Booster supply

**Cost:** ~40,000€
**Capacity:** 8 plants, 32 pods high-yield
**Production:** Industrial scale
**ROI:** 30-40 production cycles

---

### Equipment Comparison

| Equipment | Capacity | Time | Consumption | Cost |
|-----------|----------|------|-------------|------|
| **Scoring Knife** | Manual | Instant | Durability | 100€ |
| **Ritzmaschine** | 8 pods | 5 sec/pod | Redstone | 1,200€ |
| **Opium Presse** | 16 pods | 4 sec/pod | Diesel | 2,500€ |
| **Kochstation** | 8 opium | 10 sec/unit | Water+Fuel | 1,000€ |
| **Heroin Raffinerie** | 8 morphine | 15 sec/unit | Fuel | 1,500€ |

---

## Profitability Analysis

### Indian Production (Volume)

**Input Costs:**
```
Seeds: 20€
Pot: 50€ (Iron, reusable)
Diesel: 4€ (8 pods × 4 diesel × 0.1€)
Water: 10€
Fuel: 20€
Time: 22 minutes

Total Variable: 54€
```

**Output (GUT Quality with Presse):**
```
Harvest: 6 GUT Indian Pods (fertilized = 10 capped)
Extract (Presse): 10 × 3 = 30 Raw Opium
Cook: 30 Morphine
Refine: 30 Heroin (24 GUT + 6 SEHR_GUT)

Revenue:
basePrice = 20 × 3.5 = 70€
potency = 70 × 0.8 = 56€
GUT: 56 × 1.5 = 84€/unit
SEHR_GUT: 56 × 2.5 = 140€/unit

Revenue: (24 × 84) + (6 × 140) = 2,856€
Profit: 2,802€
Hourly Rate: 7,642€/hour
```

---

### Turkish Production (Balanced)

**Input Costs:**
```
Seeds: 35€
Golden Pot: 200€ (reusable)
Diesel: 6.4€
Water: 10€
Fuel: 20€
Time: 22 minutes

Total Variable: 71.4€
```

**Output (SEHR_GUT + LEGENDAER with Presse):**
```
Harvest: 8 SEHR_GUT Turkish Pods (quality upgrades)
Extract (Presse): 8 × 4 = 32 Raw Opium
Cook: 32 Morphine
Refine: 32 Heroin (26 SEHR_GUT + 6 LEGENDAER)

Revenue:
basePrice = 35 × 3.5 = 122.5€
potency = 122.5 × 1.0 = 122.5€
SEHR_GUT: 122.5 × 2.5 = 306.25€
LEGENDAER: 122.5 × 5.0 = 612.5€

Revenue: (26 × 306.25) + (6 × 612.5) = 11,637.5€
Profit: 11,566€
Hourly Rate: 31,543€/hour
```

---

### Afghan Production (Premium)

**Input Costs:**
```
Seeds: 50€
Golden Pot: 200€ (reusable)
Fertilizer: 20€
Quality Booster: 30€
Diesel: 8€ (10 × 5 × 4 × 0.1€)
Water: 15€
Fuel: 30€
Time: 23 minutes

Total Variable: 153€
```

**Output (LEGENDAER with Presse):**
```
Harvest: 10 LEGENDAER Afghan Pods (max quality)
Extract (Presse): 10 × 5 = 50 Raw Opium
Cook: 50 Morphine
Refine: 50 LEGENDAER Heroin (can't upgrade)

Revenue:
basePrice = 50 × 3.5 = 175€
potency = 175 × 1.5 = 262.5€
LEGENDAER: 262.5 × 5.0 = 1,312.5€

Revenue: 50 × 1,312.5 = 65,625€
Profit: 65,472€
Hourly Rate: 170,836€/hour (!!!)
```

**Analysis:** Afghan + LEGENDAER = HIGHEST profit in game!

---

### Scale Production (8 Afghan Plants)

**Per Cycle:**
```
Seeds: 400€ (8 × 50€)
Fertilizer: 160€ (8 × 20€)
Quality Booster: 240€ (8 × 30€)
Diesel: 64€
Water: 120€
Fuel: 240€
Time: 23 minutes

Cost: 1,224€
Output: 400 LEGENDAER Heroin (8 × 50)

Revenue: 400 × 1,312.5 = 525,000€
Profit: 523,776€

Hourly Rate: 1,366,839€/hour
Daily (8 hours): 10,934,712€
Monthly: 328,041,360€
```

**ROI:** 40,000€ setup → Break-even in 2 minutes!

---

## Best Practices

### For Beginners

**Start Simple:**
1. Choose **Indian** strain (cheap, fast)
2. Use **Terracotta Pots** (affordable)
3. **Scoring Knife** for extraction
4. Grow **2-3 plants** initially
5. Skip advanced boosters
6. Target **GUT quality**

**First Cycle Goal:**
- Complete full chain to heroin
- Achieve GUT quality
- 1,000€+ profit

---

### For Intermediate

**Optimize:**
1. Switch to **Turkish** strain
2. Invest in **Ritzmaschine**
3. Use **Fertilizer** for higher yields
4. Run **4-6 plants** simultaneously
5. Target **SEHR_GUT** quality
6. Manage water/fuel supplies

**Target:**
- Consistent SEHR_GUT quality
- 10,000€+/hour profit
- Automated extraction

---

### For Advanced

**Scale & Maximize:**
1. **Afghan strain only** (maximum profit)
2. **8× Golden Pots** (LEGENDAER quality)
3. **Opium Presse** for 2.5× yield
4. **Fertilizer + Quality Booster** on all plants
5. Target **100% LEGENDAER** quality
6. **Automated workflows** (hoppers, pipes)

**Target:**
- 100% LEGENDAER quality
- 100,000€+/hour profit
- Industrial-scale operation

---

### Resource Management

**Water Strategy:**
1. Build **water tank** (5,000+ units)
2. Automate refilling (pipes/buckets)
3. Calculate per-cycle needs (10 per opium)
4. Never run dry mid-process

**Fuel Strategy:**
1. Use **Coal Blocks** (16× efficient)
2. Store 2,000+ units
3. Calculate needs (10 per opium + 15 per morphine)
4. Automated hoppers for refilling

**Diesel Strategy:**
1. Buy in bulk (discount)
2. Store 5,000+ mB
3. Calculate per-cycle (4 per pod)
4. Dedicated diesel storage

---

## Troubleshooting

### "Plant Not Improving Quality"

**Causes:**
1. Low potency strain (Indian = 20%)
2. No fertilizer/quality booster
3. Bad RNG

**Solutions:**
```
✓ Use Afghan strain (37.5% base)
✓ Add Fertilizer (+15%)
✓ Add Quality Booster (+20%)
✓ Run more plants (averages out)
```

---

### "Low Extraction Yield"

**Causes:**
1. Using Scoring Knife or Ritzmaschine (lower yield)
2. Low quality pods (SCHLECHT = 1-2 opium)

**Solutions:**
```
✓ ALWAYS use Opium Presse (2-5 yield)
✓ Improve pod quality to LEGENDAER
✓ Check diesel supply in Presse
```

---

### "Kochstation/Raffinerie Stopped"

**Causes:**
1. Ran out of water/fuel
2. Server restart
3. Chunk unloaded

**Solutions:**
```
✓ Check water tank: Refill if needed
✓ Check fuel: Add coal/charcoal
✓ Progress preserved, resumes when refilled
✓ Stay in chunk or use chunk loader
```

---

### "Not Getting LEGENDAER Quality"

**Causes:**
1. Wrong strain (Indian has 20% vs Afghan's 37.5%)
2. No boosters used
3. Only 3 upgrade chances (stages 3, 5, 7)

**Solutions:**
```
✓ Use Afghan strain ONLY
✓ Add Fertilizer (+15% = 52.5% total)
✓ Add Quality Booster (+20% = 72.5% total)
✓ Use Golden Pot (+1 tier on harvest)
✓ Run multiple plants (probability)
```

---

<div align="center">

**Poppy/Opium Production System - Master Guide**

For related systems:
- [💊 Coca System](Coca-System.md)
- [💰 Economy & Sales](../features/Economy-System.md)
- [🏪 NPC Shops](../features/NPC-System.md)

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production](../Production-Systems.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
