# Coca/Cocaine Production System

<div align="center">

**4-Step Industrial Production Chain - Extraction & Refining**

Chemical processing with timing-based crack minigame

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production Systems](../Production-Systems.md)

</div>

---

## 📋 Quick Reference

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐⭐⭐⭐ (4/5 - Advanced) |
| **Steps** | 4-5 (Plant → Harvest → Extract → Refine → Optional: Crack) |
| **Strains** | 2 (Bolivian, Colombian) |
| **Duration** | 5-12 minutes (full cycle to cocaine) |
| **Profitability** | ⭐⭐⭐⭐ (4/5 - Very High) |
| **Quality Tiers** | Poor, Good, Very Good, Legendary |
| **Final Products** | Cocaine, Crack Rock |
| **Unique Mechanic** | Crack cooking timing minigame |
| **Equipment** | Extraction Vats, Refineries, Crack Cooker |

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Strains & Seeds](#strains--seeds)
3. [Growing Process](#growing-process)
4. [Extraction Process](#extraction-process)
5. [Refining Process](#refining-process)
6. [Crack Production](#crack-production)
7. [Quality System](#quality-system)
8. [Equipment Guide](#equipment-guide)
9. [Profitability Analysis](#profitability-analysis)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

---

## Overview

Coca/Cocaine production is an industrial-scale chemical processing system featuring diesel-powered extraction, fuel-based refining, and an optional timing-based crack cooking minigame.

### Production Flow

```
1. PLANT → Coca seeds in pot
2. GROW → 8 growth stages (5-7 seconds)
3. HARVEST → Fresh coca leaves
4. EXTRACT → Diesel extraction to coca paste (5 min)
5. REFINE → Fuel refining to cocaine (6.67 min)
6. OPTIONAL: CRACK → Timing minigame for crack rocks
```

### Why Choose Coca?

**Advantages:**
✅ Fast growth (5-7 seconds)
✅ Industrial scale processing
✅ Multiple product tiers (paste, cocaine, crack)
✅ Skill-based crack quality
✅ High profit margins

**Challenges:**
⏰ Requires diesel and fuel supplies
📚 Multi-equipment setup
💰 Higher equipment costs
🎮 Crack minigame requires timing skill
⚖️ Resource management (diesel/fuel)

---

## Strains & Seeds

### 2 Coca Strains

#### 1. Bolivianisch (Bolivian) 💚

**Characteristics:**
- **Color:** Green (§a)
- **Type:** Fast-growing, budget-friendly
- **Region:** Bolivian highlands
- **Best For:** Volume production

**Seeds:** `bolivianisch_coca_seeds`
- **Price:** 20€ per seed
- **Growth Time:** 100 ticks (5 seconds)
- **Growth Stages:** 0-7 (8 stages)
- **Water Consumption:** 0.8 per stage (low)
- **Base Yield:** 6 leaves

**Market:**
- Cheapest seeds
- Fastest growth
- Lower base price
- High-volume production

**Ticks per Stage:** 100 / 8 = **12.5 ticks** (~0.625 seconds)

---

#### 2. Kolumbianisch (Colombian) 💎

**Characteristics:**
- **Color:** Dark Green (§2)
- **Type:** Premium, slower-growing
- **Region:** Colombian mountains
- **Best For:** Premium production

**Seeds:** `kolumbianisch_coca_seeds`
- **Price:** 35€ per seed
- **Growth Time:** 140 ticks (7 seconds)
- **Growth Stages:** 0-7 (8 stages)
- **Water Consumption:** 1.0 per stage (standard)
- **Base Yield:** 6 leaves

**Market:**
- Premium seeds
- Slower growth (+40% time)
- Higher base price (+75%)
- Quality over quantity

**Ticks per Stage:** 140 / 8 = **17.5 ticks** (~0.875 seconds)

---

### Strain Comparison

| Feature | Bolivian | Colombian |
|---------|----------|-----------|
| **Seed Price** | 20€ | 35€ |
| **Growth Time** | 5 seconds | 7 seconds |
| **Speed** | Fast | Slow |
| **Water Use** | 0.8x | 1.0x |
| **Yield** | 6 leaves | 6 leaves |
| **Base Price** | Lower | Higher (+75%) |
| **Best For** | Volume | Premium |

---

## Growing Process

### Step 1: Planting

**Requirements:**
- Coca seeds (Bolivian or Colombian)
- Plant Pot (Terracotta, Ceramic, Iron, or Golden)
- Soil (15 units total)
- Water (varies by pot type)

**Process:**
1. Place pot in desired location
2. Add soil to pot (minimum 15 units)
3. Add water to pot
4. Right-click pot with coca seeds
5. Seed planted, growth begins

**Initial State:**
```
Growth Stage: 0/7 (Seedling)
Strain: Bolivianisch/Kolumbianisch
Quality: Pending (default: GUT)
Progress: 0%
```

---

### Growth Stages

**8 Growth Stages (0-7):**

| Stage | Name | Height | Visual | Soil Use |
|-------|------|--------|--------|----------|
| 0 | Seedling | 1 block | Tiny sprout | 2.14 units |
| 1 | Vegetative 1 | 1 block | Small plant | 2.14 units |
| 2 | Vegetative 2 | 1 block | Growing | 2.14 units |
| 3 | Pre-Mature | 1 block | Larger | 2.14 units |
| 4 | Mature 1 | **2 blocks** | Tall plant | 2.14 units |
| 5 | Mature 2 | **2 blocks** | Leaves forming | 2.14 units |
| 6 | Mature 3 | **2 blocks** | Full foliage | 2.14 units |
| 7 | **HARVEST READY** | **2 blocks** | Fully grown | 2.14 units |

**Total Soil Consumption:** 15 units (2.14 per stage × 7 stages)

**Plant Height:**
- Stages 0-3: Single block (small)
- Stages 4-7: **Two blocks tall** (DoubleBlockHalf.LOWER + UPPER)

---

### Growth Times

**Base Growth Times:**

| Strain | Total Time | Per Stage | Real-Time |
|--------|------------|-----------|-----------|
| **Bolivian** | 100 ticks | 12.5 ticks | ~5 seconds |
| **Colombian** | 140 ticks | 17.5 ticks | ~7 seconds |

**With Growth Booster:**
```java
boostedTime = growthTicks / 2

Bolivian: 100 / 2 = 50 ticks (~2.5 seconds)
Colombian: 140 / 2 = 70 ticks (~3.5 seconds)
```

---

### Growth Modifiers

#### Pot Types

| Pot | Water Capacity | Price | Special Effect |
|-----|----------------|-------|----------------|
| **Terracotta** | 100 units | 20€ | Basic |
| **Ceramic** | 150 units | 40€ | Better retention |
| **Iron** | 200 units | 80€ | Durable |
| **Golden** | 250 units | 150€ | +1 quality tier |

**Golden Pot Benefit:**
- Harvest quality: GUT → SEHR_GUT
- Worth it for premium production

---

#### Fertilizer (Dünger)

**Effect:** Increases yield

```java
baseYield = 6 leaves
withFertilizer = min(10, baseYield * 1.67)
maxYield = 10 leaves
```

**Trade-off:**
- ✅ +67% yield (6 → 10 leaves)
- ❌ -1 quality tier

**Worth It?**
```
6 GUT leaves vs 10 SCHLECHT leaves
6 × 1.5x = 9.0x value
10 × 1.0x = 10.0x value

Result: Fertilizer gives +11% value
Use for volume production
```

---

#### Growth Booster (Wachstumsbeschleuniger)

**Effect:** Doubles growth speed

```java
normalTime = 100-140 ticks
boostedTime = 50-70 ticks (50% faster)
```

**Trade-off:**
- ✅ 50% faster growth
- ❌ -1 quality tier

**Usage:**
- Apply to pot before/during growth
- Best for rapid production cycles
- Sacrifice quality for speed

---

#### Quality Booster (Qualitätsverbesserer)

**Effect:** +1 quality tier on harvest

```java
normalQuality = GUT (default)
boostedQuality = SEHR_GUT (+1 tier)
```

**Trade-off:**
- ✅ Guaranteed +1 quality
- ❌ Cannot combine with fertilizer or growth booster

**Usage:**
- Apply to pot before harvest
- Best for premium production
- Combine with Golden Pot for LEGENDAER quality

---

### Step 2: Harvesting

**When to Harvest:**
- Stage 7/7 (fully mature)
- Visual: 2-block tall plant, full foliage
- Tooltip: "Ready to harvest"

**Harvest Yields:**

**Full Harvest (Stage 7):**
- Yields: 6 Fresh Coca Leaves (base)
- Quality: GUT (default)
- With Fertilizer: 10 leaves (SCHLECHT quality)

**Partial Harvest (Stage 4-6):**
- Yields: 3 leaves (50% penalty)
- Quality: SCHLECHT (downgraded)
- Not recommended

**Early Harvest (Stage 0-3):**
- Yields: 0 leaves (no drops)

**How to Harvest:**
1. Right-click fully mature plant (Stage 7)
2. Receive Fresh Coca Leaves (strain-specific)
3. Plant drops, pot becomes empty
4. Replant immediately

**Yield Formula:**
```java
if (stage == 7) {
    baseYield = 6;
    if (fertilized) yield = min(10, 6 * 1.67);
    if (goldenPot) quality = quality.upgrade();
} else if (stage >= 4 && stage <= 6) {
    yield = 3; // 50% penalty
    quality = SCHLECHT;
} else {
    yield = 0; // too early
}
```

---

## Extraction Process

### Step 3: Coca Paste Extraction

**Purpose:** Extract alkaloids from fresh leaves using diesel

**Input:** Fresh Coca Leaves
**Output:** Coca Paste (brown paste)
**Duration:** 6,000 ticks (5 minutes) per batch
**Requirement:** Diesel (100 mB per leaf)

---

### Extraction Vats (3 Sizes)

**Equipment Variants:**

| Size | Capacity | Extraction Time | Max Diesel | Throughput |
|------|----------|-----------------|------------|------------|
| **Small Extraction Vat** | 6 leaves | 6,000 ticks (5 min) | 1,000 mB | 6 leaves/5min |
| **Medium Extraction Vat** | 12 leaves | 6,000 ticks (5 min) | 2,000 mB | 12 leaves/5min |
| **Big Extraction Vat** | 24 leaves | 6,000 ticks (5 min) | 4,000 mB | 24 leaves/5min |

**Block Names:**
- `small_extraction_vat`
- `medium_extraction_vat`
- `big_extraction_vat`

---

### Extraction Mechanics

**Process Details:**
- Each leaf processes independently in parallel slots
- Requires 100 mB diesel per leaf
- If diesel runs out, processing pauses (progress preserved)
- Output: 1 Coca Paste per 1 Fresh Leaf (1:1 ratio)
- Quality preserved from input

**Diesel Consumption:**
```java
dieselPerLeaf = 100 mB

Examples:
6 leaves = 600 mB diesel
12 leaves = 1,200 mB diesel
24 leaves = 2,400 mB diesel
```

---

### Using Extraction Vats

**Steps:**
1. Place Extraction Vat
2. Fill with Diesel (use Diesel Canister)
3. Add Fresh Coca Leaves (right-click)
4. Wait 5 minutes
5. Collect Coca Paste (shift+right-click)

**Example (Big Extraction Vat):**
```
Input: 24 Fresh Bolivian Leaves (GUT quality)
Diesel: 2,400 mB
Time: 6,000 ticks (5 minutes)

Output: 24 Coca Paste (GUT quality, brown)
```

**Parallel Processing:**
```
Slot 1: 1 leaf → 100 mB → 1 paste
Slot 2: 1 leaf → 100 mB → 1 paste
...
Slot 24: 1 leaf → 100 mB → 1 paste

All slots process simultaneously!
Total: 24 paste in 5 minutes
```

---

### Diesel Supply

**Diesel Canister:**
- Item: `diesel_canister`
- Capacity: 1,000 mB
- Refillable

**Obtaining Diesel:**
- Purchase from NPC shops
- Craft from petroleum (if enabled)
- Trade with players

**Diesel Costs:**
```
Typical price: ~50€ per 1,000 mB

Per leaf: 100 mB = ~5€
Per 6-leaf batch: 600 mB = ~30€
Per 24-leaf batch: 2,400 mB = ~120€
```

---

## Refining Process

### Step 4: Cocaine Refining

**Purpose:** Purify coca paste into cocaine powder

**Input:** Coca Paste
**Output:** Cocaine (white powder)
**Duration:** 8,000 ticks (6.67 minutes) per batch
**Requirement:** Fuel (400 units per paste)
**Quality Upgrade:** 20% chance to upgrade quality +1 tier

---

### Refineries (3 Sizes)

**Equipment Variants:**

| Size | Capacity | Refinery Time | Max Fuel | Throughput |
|------|----------|---------------|----------|------------|
| **Small Refinery** | 6 paste | 8,000 ticks (6.67 min) | 500 units | 6 paste/6.67min |
| **Medium Refinery** | 12 paste | 8,000 ticks (6.67 min) | 1,000 units | 12 paste/6.67min |
| **Big Refinery** | 24 paste | 8,000 ticks (6.67 min) | 2,000 units | 24 paste/6.67min |

**Block Names:**
- `small_refinery` (Light Level: 8)
- `medium_refinery` (Light Level: 10)
- `big_refinery` (Light Level: 12)

**Visual:** Refineries emit light when operating

---

### Refining Mechanics

**Process Details:**
- Each paste processes independently
- Fuel consumed: 1 unit per 20 ticks = 400 fuel per paste
- Output: 1 Cocaine per 1 Paste (1:1 ratio)
- **Quality Upgrade:** 20% chance to upgrade from input quality
- If fuel runs out, processing pauses (progress preserved)

**Fuel Consumption:**
```java
fuelPerTick = 1 unit per 20 ticks
fuelPerPaste = 8,000 / 20 = 400 units

Examples:
6 paste = 2,400 fuel units
12 paste = 4,800 fuel units
24 paste = 9,600 fuel units
```

**Quality Upgrade Chance:**
```java
if (random.nextFloat() < 0.2 && quality != LEGENDAER) {
    outputQuality = inputQuality.upgrade();
} else {
    outputQuality = inputQuality;
}

Example:
GUT paste → 20% chance → SEHR_GUT cocaine
           → 80% chance → GUT cocaine
```

---

### Using Refineries

**Steps:**
1. Place Refinery
2. Add Fuel (coal, charcoal, etc.)
3. Add Coca Paste (right-click)
4. Wait 6.67 minutes
5. Collect Cocaine (shift+right-click)

**Example (Medium Refinery):**
```
Input: 12 GUT Coca Paste
Fuel: 4,800 units (coal)
Time: 8,000 ticks (6.67 minutes)

Output (expected):
→ ~2-3 SEHR_GUT Cocaine (20% upgrade chance)
→ ~9-10 GUT Cocaine (80% no upgrade)

Total: 12 Cocaine (mixed qualities)
```

---

### Fuel Types

**Acceptable Fuels:**
- Coal: 1,600 burn time = 80 ticks worth
- Charcoal: 1,600 burn time = 80 ticks worth
- Coal Block: 16,000 burn time = 800 ticks worth
- Lava Bucket: 20,000 burn time = 1,000 ticks worth

**Fuel Efficiency:**
```
1 Coal = 80 ticks worth of fuel
1 Paste needs 400 units of fuel
→ 5 coal per paste

6 paste = 30 coal
12 paste = 60 coal
24 paste = 120 coal
```

---

## Crack Production

### Step 5: Crack Cooking (Optional)

**Purpose:** Convert cocaine into crack rocks via timing minigame

**Input:** Cocaine + Backpulver (Baking Soda)
**Output:** Crack Rock (~80% of input weight)
**Duration:** 80 ticks (4 seconds) - TIMING CRITICAL
**Quality:** Skill-based (timing minigame)

---

### Crack Kocher (Crack Cooker)

**Block:** `crack_kocher`
**Light Level:** 6 (glows during cooking)
**Capacity:** 1-10g cocaine per cook
**Mechanic:** Timing-based minigame

**Technical Specs:**
```java
Cook Cycle: 80 ticks (4 seconds)
Input: 1-10g Cocaine + 1+ Backpulver
Output: Crack Rock (~80% weight)
Weight Conversion: crackWeight = cocaineWeight * 0.8
```

---

### Crack Timing Minigame

**How It Works:**

1. Place cocaine and baking soda in Crack Cooker
2. Cook cycle starts (80 ticks / 4 seconds)
3. Visual/audio indicator shows progress
4. **Click at the RIGHT MOMENT** to determine quality
5. Too early or too late = SCHLECHT (burnt/undercooked)
6. Perfect timing = FISHSCALE (premium)

**Timing Zones:**

```
[0───27]─[28──────52]─[53───80]
  TOO      GOOD/        TOO
  EARLY    PERFECT      LATE

Tick 0-27:    TOO EARLY    → SCHLECHT
Tick 28-52:   GOOD WINDOW  → GUT/STANDARD
Tick 35-45:   PERFECT      → FISHSCALE
Tick 53-80:   TOO LATE     → SCHLECHT (burnt)
```

---

### Timing Zones Detailed

#### Zone 1: Too Early (0-27 ticks)
**Quality:** SCHLECHT
**Score:** 0.2 + (cookTick / 28) * 0.3
**Result:** Undercooked, poor quality
**Visual:** Red indicator, failure sound

#### Zone 2: Good Window (28-52 ticks)
**Quality:** GUT or STANDARD
**Score:** 0.6 + (0.3 * (1.0 - |cookTick - 40| / 20.0))
**Result:** Acceptable cook
**Visual:** Yellow indicator

#### Zone 3: Perfect Window (35-45 ticks)
**Quality:** FISHSCALE
**Score:** 1.0 - (distanceFromPerfect / 10.0) * 0.05
**Result:** Perfect cook, premium quality
**Visual:** Green indicator, success sound
**Center:** Tick 40 (exact center of cycle)

#### Zone 4: Too Late (53-80 ticks)
**Quality:** SCHLECHT
**Score:** max(0.1, 0.5 - ((cookTick - 52) / 30.0) * 0.4)
**Result:** Burnt, ruined
**Visual:** Dark red, burn sound

---

### Crack Quality Levels

**4 Quality Tiers:**

| Quality | Score Range | Price Multiplier | Description |
|---------|-------------|------------------|-------------|
| **SCHLECHT** | < 0.5 | 0.6x | Überkokt oder unterkokt (burnt or raw) |
| **STANDARD** | 0.5-0.79 | 1.0x | Normaler Cook (normal) |
| **GUT** | 0.8-0.94 | 1.5x | Guter Cook (good) |
| **FISHSCALE** | ≥ 0.95 | 2.5x | Perfekter Cook, glänzend (perfect, shiny) |

**Visual Colors:**
- SCHLECHT: §c (red)
- STANDARD: §f (white)
- GUT: §e (yellow)
- FISHSCALE: §b§l (bold cyan - premium!)

---

### Crack Production Examples

#### Example 1: Perfect Timing
```
Input: 10g GUT Cocaine + 1 Backpulver
Click Time: Tick 40 (exact center)
Distance from Perfect: 0

Result:
Score: 1.0 (perfect)
Quality: FISHSCALE
Output: 8g FISHSCALE Crack (10 * 0.8)
Price: cocaineBasePrice * 0.8 * 2.5
```

#### Example 2: Good Timing
```
Input: 10g GUT Cocaine + 1 Backpulver
Click Time: Tick 32
Distance from Perfect: 8 ticks

Result:
Score: 0.78
Quality: GUT
Output: 8g GUT Crack
Price: cocaineBasePrice * 0.8 * 1.5
```

#### Example 3: Too Late (Burnt)
```
Input: 10g SEHR_GUT Cocaine + 1 Backpulver
Click Time: Tick 65
Zone: Too Late

Result:
Score: 0.32
Quality: SCHLECHT
Output: 8g SCHLECHT Crack (burnt)
Price: cocaineBasePrice * 0.8 * 0.6
WASTE of SEHR_GUT cocaine!
```

---

### Special Crack Mechanics

**Legendary Input Bonus:**
```java
if (inputQuality == LEGENDAER && crackQuality < FISHSCALE) {
    crackQuality = crackQuality.upgrade();
}

Example:
LEGENDAER cocaine + GUT cook (score 0.85)
→ Normally GUT crack
→ With bonus: FISHSCALE crack
```

**Auto-Completion:**
```java
if (cookTick >= 80 && !removed) {
    // Auto-complete at end (usually burnt)
    outputQuality = SCHLECHT;
}
```

**Weight Conversion:**
```java
crackWeight = (int)(cocaineWeight * 0.8)

Examples:
10g cocaine → 8g crack
5g cocaine → 4g crack
1g cocaine → 0g crack (minimum 1g required)
```

---

## Quality System

### Quality Tiers (Cocaine)

**4 Quality Levels:**

| Quality | Price Multiplier | Color | Description |
|---------|------------------|-------|-------------|
| **SCHLECHT** (Poor) | 1.0x | §c (red) | Niedrige Qualität |
| **GUT** (Good) | 1.5x | §e (yellow) | Gute Qualität |
| **SEHR_GUT** (Very Good) | 2.5x | §a (green) | Sehr gute Qualität |
| **LEGENDAER** (Legendary) | 5.0x | §6§l (gold bold) | Legendäre Qualität |

**Default:** GUT (harvested from plants)

---

### Quality Progression

**Path 1: Standard Production**
```
Plant (GUT) → Leaves (GUT) → Paste (GUT)
→ Refining (20% upgrade) → Cocaine (GUT or SEHR_GUT)
```

**Path 2: Golden Pot**
```
Plant in Golden Pot (+1) → Leaves (SEHR_GUT)
→ Paste (SEHR_GUT) → Refining (20% upgrade)
→ Cocaine (SEHR_GUT or LEGENDAER)
```

**Path 3: Quality Booster**
```
Plant with Quality Booster → Leaves (SEHR_GUT)
→ Same as Path 2
```

**Path 4: Fertilizer (Volume)**
```
Plant with Fertilizer (-1) → Leaves (SCHLECHT)
→ Paste (SCHLECHT) → Refining (20% upgrade)
→ Cocaine (SCHLECHT or GUT)
Higher yield, lower quality
```

---

### Achieving Legendary Quality

**Method 1: Golden Pot + Luck**
```
1. Plant in Golden Pot (+1 tier)
2. Harvest: SEHR_GUT leaves
3. Extract: SEHR_GUT paste
4. Refine: 20% chance → LEGENDAER cocaine
Result: 20% chance LEGENDAER
```

**Method 2: Quality Booster + Luck**
```
1. Use Quality Booster on pot
2. Harvest: SEHR_GUT leaves
3. Extract: SEHR_GUT paste
4. Refine: 20% chance → LEGENDAER
Result: 20% chance LEGENDAER
```

**Expected Yield:**
```
100 plants with Golden Pot:
→ 100 SEHR_GUT leaves
→ 100 SEHR_GUT paste
→ ~20 LEGENDAER cocaine
→ ~80 SEHR_GUT cocaine
```

---

## Equipment Guide

### Essential Equipment (Minimum)

**Starting Setup:**
1. ✅ 1× Terracotta Pot
2. ✅ 1× Small Extraction Vat
3. ✅ 1× Small Refinery
4. ✅ Diesel supply (1,000 mB)
5. ✅ Fuel supply (500 units)

**Cost:** ~3,000€
**Capacity:** 6 leaves per cycle
**Production:** Basic

---

### Intermediate Setup

**Recommended:**
1. ✅ 4× Iron Pots
2. ✅ 1× Medium Extraction Vat
3. ✅ 1× Medium Refinery
4. ✅ Diesel storage (2,000 mB)
5. ✅ Fuel storage (1,000 units)
6. ✅ Optional: 1× Crack Cooker

**Cost:** ~8,000€
**Capacity:** 12 leaves per cycle
**Production:** Moderate

---

### Advanced Setup (Professional)

**Professional:**
1. ✅ 8× Golden Pots (for LEGENDAER quality)
2. ✅ 2× Big Extraction Vats (48 leaves total)
3. ✅ 2× Big Refineries (48 paste total)
4. ✅ 2× Crack Cookers (parallel crack production)
5. ✅ Diesel storage (8,000 mB+)
6. ✅ Fuel storage (4,000+ units)
7. ✅ Automated diesel/fuel delivery

**Cost:** ~35,000€
**Capacity:** 48 leaves per cycle
**Production:** Industrial scale
**ROI:** 25-35 production cycles

---

### Equipment Comparison

| Equipment | Size | Capacity | Time | Cost |
|-----------|------|----------|------|------|
| **Extraction Vat** | Small | 6 | 5 min | 800€ |
| **Extraction Vat** | Medium | 12 | 5 min | 1,500€ |
| **Extraction Vat** | Big | 24 | 5 min | 2,500€ |
| **Refinery** | Small | 6 | 6.67 min | 1,000€ |
| **Refinery** | Medium | 12 | 6.67 min | 2,000€ |
| **Refinery** | Big | 24 | 6.67 min | 3,500€ |
| **Crack Cooker** | - | 1-10g | 4 sec | 600€ |

---

## Profitability Analysis

### Bolivian Production (Standard)

**Input Costs:**
```
Seeds: 20€
Pot: 50€ (Iron, reusable)
Diesel: 30€ (6 leaves × 5€)
Fuel: 15€ (6 paste × ~2.5€)
Time: 11.75 minutes

Total Variable: 65€/cycle
```

**Output (GUT Quality):**
```
Harvest: 6 leaves
Extract: 6 paste
Refine: 6 cocaine (4.8 GUT + 1.2 SEHR_GUT avg)

Revenue Calculation:
basePrice = 20 * 3.0 = 60€ per unit
GUT: 60 * 1.5 = 90€/g
SEHR_GUT: 60 * 2.5 = 150€/g

Revenue: (4.8 * 90) + (1.2 * 150) = 612€
Profit: 547€
Hourly Rate: 2,794€/hour
```

---

### Colombian Production (Premium)

**Input Costs:**
```
Seeds: 35€
Golden Pot: 200€ (reusable)
Diesel: 30€
Fuel: 15€
Time: 11.78 minutes

Total Variable: 80€
```

**Output (SEHR_GUT + LEGENDAER):**
```
Harvest: 6 SEHR_GUT leaves (Golden Pot)
Extract: 6 SEHR_GUT paste
Refine: 6 cocaine (4.8 SEHR_GUT + 1.2 LEGENDAER avg)

Revenue:
basePrice = 35 * 3.0 = 105€ per unit
SEHR_GUT: 105 * 2.5 = 262.5€/g
LEGENDAER: 105 * 5.0 = 525€/g

Revenue: (4.8 * 262.5) + (1.2 * 525) = 1,890€
Profit: 1,810€
Hourly Rate: 9,214€/hour
```

**Analysis:** Colombian + Golden Pot = 3.3× higher profit!

---

### Crack Production (Skill-Based)

**Input:**
```
10g GUT Bolivian Cocaine: 900€
1 Backpulver: 10€
Time: 4 seconds

Total: 910€
```

**Output (Quality-Dependent):**

**SCHLECHT (Bad Timing):**
```
8g SCHLECHT Crack
Price: (60 * 0.8 * 0.6) = 28.8€/g
Revenue: 8 * 28.8 = 230.4€
Profit: -679.6€ (LOSS!)
```

**GUT (Good Timing):**
```
8g GUT Crack
Price: (60 * 0.8 * 1.5) = 72€/g
Revenue: 8 * 72 = 576€
Profit: -334€ (LOSS)
```

**FISHSCALE (Perfect Timing):**
```
8g FISHSCALE Crack
Price: (60 * 0.8 * 2.5) = 120€/g
Revenue: 8 * 120 = 960€
Profit: +50€ (Small profit)
```

**Conclusion:**
```
Crack is ONLY profitable with FISHSCALE quality
Requires perfect timing (tick 35-45)
High risk, low reward
Better to sell cocaine directly!
```

---

### Scale Production (48 Leaves)

**Setup:** 2× Big Extraction Vats, 2× Big Refineries

**Per Cycle:**
```
Seeds: 280€ (8 Colombian plants × 35€)
Diesel: 240€ (48 leaves × 5€)
Fuel: 120€ (48 paste × 2.5€)
Time: 11.78 minutes

Cost: 640€
Output: 48 cocaine (mixed quality)

Quality Distribution (with Golden Pots):
38.4 SEHR_GUT @ 262.5€ = 10,080€
9.6 LEGENDAER @ 525€ = 5,040€

Total Revenue: 15,120€
Profit: 14,480€

Hourly Rate: 73,708€/hour
Daily (8 hours): 589,664€
Monthly: 17,689,920€
```

**ROI:** 35,000€ setup → Break-even in 3 minutes!

---

## Best Practices

### For Beginners

**Start Simple:**
1. Choose **Bolivian** strain (cheaper, faster)
2. Use **Iron Pots** (good value)
3. Grow **2-3 plants** initially
4. **Small equipment** (6-slot vats/refineries)
5. **Skip crack** (high risk for beginners)
6. Focus on **consistent cocaine production**

**First Cycle Goal:**
- Understand full process
- Achieve GUT quality
- 500€+ profit

---

### For Intermediate

**Optimize:**
1. Upgrade to **Colombian** strain
2. Invest in **Golden Pots** (LEGENDAER quality)
3. Run **6-8 plants** simultaneously
4. **Medium equipment** (12-slot)
5. Target **SEHR_GUT** quality
6. **Stockpile diesel/fuel**

**Target:**
- 80%+ SEHR_GUT quality
- 5,000€+/hour profit
- Efficient resource management

---

### For Advanced

**Scale & Specialize:**
1. **8-12 plant operation** (all Golden Pots)
2. **Big equipment** (24-slot vats/refineries)
3. **LEGENDAER quality** focus (20% from refining)
4. **Crack mastery** (FISHSCALE timing)
5. **Bulk diesel/fuel** storage
6. **Automated workflows**

**Target:**
- 20%+ LEGENDAER quality
- 10,000€+/hour profit
- Crack FISHSCALE 80%+ success rate

---

### Crack Timing Mastery

**Practice Routine:**

**Level 1: Learning (0-10 attempts)**
- Aim for GUT quality (tick 28-52)
- Wide window, easier to hit
- Accept 50% success rate

**Level 2: Competent (10-30 attempts)**
- Narrow to tick 35-45 (FISHSCALE window)
- Practice counting rhythm
- Target 60-70% FISHSCALE rate

**Level 3: Expert (30+ attempts)**
- Perfect timing at tick 40 (exact center)
- Muscle memory established
- Achieve 80%+ FISHSCALE rate

**Timing Tips:**
```
Count Method:
"One... two... three... CLICK!" (at tick 40)

Visual Method:
Watch indicator bar, click when centered

Audio Method:
Listen for timing cues (if enabled)

Practice: Use cheap SCHLECHT cocaine first!
```

---

### Resource Management

**Diesel Strategy:**
1. **Buy in bulk** (discount from NPCs)
2. **Store 5,000+ mB** (multiple cycles)
3. **Calculate per-cycle needs** (100 mB × leaves)
4. **Refill before running out**

**Fuel Strategy:**
1. **Use Coal Blocks** (16× more efficient)
2. **Store 2,000+ units** (multiple cycles)
3. **Calculate per-cycle needs** (400 × paste)
4. **Automated fuel delivery** (hoppers)

---

## Troubleshooting

### "Extraction Vat Not Processing"

**Causes:**
1. Out of diesel
2. No leaves added
3. Server lag

**Solutions:**
```
✓ Check diesel level: Right-click with empty hand
✓ Add diesel: Right-click with Diesel Canister
✓ Confirm leaves added: Check GUI
✓ Check server TPS: /tps
```

---

### "Refinery Stopped Mid-Process"

**Causes:**
1. Ran out of fuel
2. Server restart
3. Chunk unloaded

**Solutions:**
```
✓ Add more fuel: Right-click with coal/charcoal
✓ Check progress: Progress preserved, resumes when fuel added
✓ Keep chunk loaded: Stay nearby or use chunk loader
```

---

### "Low Quality Cocaine"

**Causes:**
1. Used fertilizer (downgrades quality)
2. No Golden Pot
3. Bad RNG on refining (80% no upgrade)

**Solutions:**
```
✓ Don't use fertilizer for premium production
✓ Invest in Golden Pots (+1 tier)
✓ Run more batches (20% upgrade averages out)
```

---

### "Crack Always SCHLECHT"

**Causes:**
1. Clicking too early (tick 0-27)
2. Clicking too late (tick 53-80)
3. Poor timing skill

**Solutions:**
```
✓ Practice with cheap cocaine first
✓ Aim for tick 35-45 window (FISHSCALE)
✓ Count rhythm: "1... 2... 3... CLICK"
✓ Watch visual indicator carefully
✓ Avoid distractions during 4-second window
```

---

### "Running Out of Diesel/Fuel"

**Causes:**
1. Insufficient stockpile
2. Underestimated consumption
3. No automated refill

**Solutions:**
```
✓ Calculate needs: 100 mB diesel + 400 fuel per leaf
✓ Buy in bulk: 5,000+ mB diesel, 2,000+ fuel
✓ Automate: Hoppers for fuel, storage for diesel
✓ Monitor levels before starting batch
```

---

<div align="center">

**Coca/Cocaine Production System - Master Guide**

For related systems:
- [🚬 Tobacco System](Tobacco-System.md)
- [🌿 Cannabis System](Cannabis-System.md)
- [💰 Economy & Sales](../features/Economy-System.md)

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production](../Production-Systems.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
