# Mushroom Production System

<div align="center">

**Multi-Flush Cultivation - 3 Psilocybe Strains**

Unique flush harvesting with automated climate control

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production Systems](../Production-Systems.md)

</div>

---

## 📋 Quick Reference

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐⭐ (2/5 - Beginner Friendly) |
| **Steps** | 4 (Substrate → Inoculate → Incubate → Fruit) |
| **Strains** | 3 (Cubensis, Azurescens, Mexicana) |
| **Duration** | 3-9 seconds (growth cycle) |
| **Profitability** | ⭐⭐⭐ (3/5 - Good) |
| **Quality Tiers** | Poor, Good, Very Good, Legendary |
| **Unique Mechanic** | Multi-flush harvesting (2-5 harvests per substrate) |
| **Equipment** | Mist bags, climate lamps, water tank, drying racks |

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Strains & Spores](#strains--spores)
3. [Substrate Preparation](#substrate-preparation)
4. [Growing Process](#growing-process)
5. [Multi-Flush Harvesting](#multi-flush-harvesting)
6. [Drying Process](#drying-process)
7. [Climate Control](#climate-control)
8. [Quality System](#quality-system)
9. [Equipment Guide](#equipment-guide)
10. [Profitability Analysis](#profitability-analysis)
11. [Best Practices](#best-practices)

---

## Overview

Mushroom production is the most beginner-friendly system in ScheduleMC, featuring fast growth cycles, automated climate control, and a unique multi-flush harvesting system that allows 2-5 harvests from a single substrate.

### Production Flow

```
1. SUBSTRATE → Add mist bag to pot
2. INOCULATE → Add spore syringe
3. INCUBATE → Darkness, stages 0-3
4. FRUIT → Low light + water, stages 4-7
5. HARVEST → Fresh mushrooms (repeat 2-5 times!)
6. DRY → Drying rack for storage
```

### Why Choose Mushrooms?

**Advantages:**
✅ Fastest growth (3-9 seconds)
✅ Multi-flush harvesting (2-5 harvests)
✅ Beginner-friendly
✅ Automated climate control
✅ Requires darkness (easy to manage)

**Challenges:**
💡 Darkness required
💧 Water automation recommended
🌡️ Temperature control for optimal yield
📦 Drying required for storage

---

## Strains & Spores

### 1. Psilocybe Cubensis (Balanced) ⚖️

**Characteristics:**
- **Color:** Gold (§6)
- **Type:** Balanced, beginner-friendly
- **Potency:** 100% (standard)
- **Best For:** General production

**Seeds:** `spore_syringe_cubensis`
- **Price:** 30€ per syringe
- **Growth Time:** 100 ticks (5 seconds)
- **Water Use:** 1.0× (standard)
- **Max Flushes:** 4 harvests
- **Temperature:** Neutral (no special requirements)

**Light Requirements:**
- Incubation: < 4 light level
- Fruiting: < 7 light level

---

### 2. Psilocybe Azurescens (Premium) 💎

**Characteristics:**
- **Color:** Blue (§9)
- **Type:** Strongest, most difficult
- **Potency:** 200% (HIGHEST!)
- **Best For:** Maximum profit

**Seeds:** `spore_syringe_azurescens`
- **Price:** 60€ per syringe
- **Growth Time:** 180 ticks (9 seconds)
- **Water Use:** 1.5× (high)
- **Max Flushes:** 3 harvests
- **Temperature:** Requires COLD (Klimalampe)

**Light Requirements:**
- Incubation: < 3 light level (strictest!)
- Fruiting: < 5 light level

---

### 3. Psilocybe Mexicana (Fast) ⚡

**Characteristics:**
- **Color:** Yellow (§e)
- **Type:** Fastest, most tolerant
- **Potency:** 60% (lowest)
- **Best For:** High-volume production

**Seeds:** `spore_syringe_mexicana`
- **Price:** 20€ per syringe
- **Growth Time:** 60 ticks (3 seconds - FASTEST!)
- **Water Use:** 0.7× (low)
- **Max Flushes:** 5 harvests (MOST!)
- **Temperature:** Requires WARM (Klimalampe)

**Light Requirements:**
- Incubation: < 5 light level (most tolerant)
- Fruiting: < 8 light level

---

### Strain Comparison

| Feature | Mexicana | Cubensis | Azurescens |
|---------|----------|----------|------------|
| **Spore Price** | 20€ | 30€ | 60€ |
| **Growth Time** | 3 sec | 5 sec | 9 sec |
| **Potency** | 60% | 100% | 200% |
| **Max Flushes** | 5 | 4 | 3 |
| **Water Use** | 0.7× | 1.0× | 1.5× |
| **Temperature** | Warm | Neutral | Cold |
| **Difficulty** | Easy | Medium | Hard |

---

## Substrate Preparation

### Mist Bags (3 Sizes)

**Small Mist Bag**
- **Price:** 15€
- **Capacity:** 1 culture
- **Best For:** Single-pot setup

**Medium Mist Bag**
- **Price:** 35€
- **Capacity:** 2 cultures
- **Best For:** Small-scale production

**Large Mist Bag**
- **Price:** 60€
- **Capacity:** 3 cultures
- **Best For:** Large-scale operation

---

### Adding Substrate

**Process:**
1. Place Plant Pot
2. Right-click pot with Mist Bag
3. Substrate added (1 unit)
4. Ready for inoculation

**Substrate Consumption:**
- Total: 15 units consumed during full growth
- Per stage: ~2.14 units (15/7 stages)
- Must replenish after all flushes exhausted

---

## Growing Process

### Phase 1: Inoculation

**Requirements:**
- Pot with substrate (mist bag added)
- Spore Syringe (strain-specific)

**Process:**
1. Right-click prepared pot with Spore Syringe
2. Mycelium culture established
3. Growth begins automatically

**Initial State:**
```
Growth Stage: 0/7 (Incubation)
Strain: [Cubensis/Azurescens/Mexicana]
Quality: GUT (default)
Flush Count: 0
```

---

### Phase 2: Incubation (Stages 0-3)

**Purpose:** Mycelium colonization

**Requirements:**
- **DARKNESS** (low light levels)
- Substrate only (no water yet)
- Temperature control (if using premium strains)

**Light Requirements:**
```
Mexicana: < 5 light level (most tolerant)
Cubensis: < 4 light level
Azurescens: < 3 light level (strictest)
```

**Duration:**
```
Mexicana: 60 ticks / 8 stages = 7.5 ticks/stage × 4 = 30 ticks (~1.5 sec)
Cubensis: 100 / 8 × 4 = 50 ticks (~2.5 sec)
Azurescens: 180 / 8 × 4 = 90 ticks (~4.5 sec)
```

**Visual:** Small mycelium growth in substrate

---

### Phase 3: Fruiting (Stages 4-7)

**Purpose:** Mushroom formation

**Requirements:**
- **Low to moderate light** (strain-specific)
- **WATER** (strain-specific consumption)
- Substrate (continued)
- Temperature control

**Light Requirements:**
```
Mexicana: < 8 light level (most tolerant)
Cubensis: < 7 light level
Azurescens: < 5 light level (strictest)
```

**Water Consumption:**
```java
waterPerStage = baseWater × strainWaterMultiplier

Mexicana: baseWater × 0.7 (drought tolerant)
Cubensis: baseWater × 1.0 (standard)
Azurescens: baseWater × 1.5 (water hungry)
```

**Duration:** Same as incubation (4 stages)

**Visual:** Mushrooms forming and growing

---

### Stage 7: Fully Grown

**Indicators:**
- Growth stage: 7/7
- Visual: Full-sized mushrooms
- Tooltip: "Ready to harvest"

**Ready to harvest!**

---

## Multi-Flush Harvesting

### Unique Feature: Multiple Harvests

**Most Unique Mechanic in ScheduleMC:**
- Harvest mushrooms WITHOUT destroying culture
- Substrate remains viable
- Regrows for additional harvests
- Each flush yields progressively less

---

### Harvesting

**How to Harvest:**
1. Shift + Right-click fully grown mushrooms with empty hand
2. Receive Fresh Mushrooms
3. Culture automatically resets to stage 3
4. Regrowth begins immediately
5. Repeat until max flushes reached

**Why Stage 3?**
- Mycelium already established
- Skips incubation phase (stages 0-3)
- Faster subsequent harvests
- Only needs fruiting phase (stages 4-7)

---

### Flush System

**Flush Count:**
```
Mexicana: Up to 5 flushes
Cubensis: Up to 4 flushes
Azurescens: Up to 3 flushes
```

**Yield Reduction per Flush:**
```java
flushPenalty = 1.0 - (currentFlush × 0.15)

Flush 1: 100% yield
Flush 2: 85% yield (-15%)
Flush 3: 70% yield (-30%)
Flush 4: 55% yield (-45%)
Flush 5: 40% yield (-60%)
```

**Example (Cubensis, 6g base yield):**
```
Flush 1: 6g × 1.00 = 6.0g
Flush 2: 6g × 0.85 = 5.1g
Flush 3: 6g × 0.70 = 4.2g
Flush 4: 6g × 0.55 = 3.3g
────────────────────────
Total: 18.6g from one substrate!
```

---

### Substrate Exhaustion

**After Max Flushes:**
```
Mexicana: After 5th flush
Cubensis: After 4th flush
Azurescens: After 3rd flush
```

**What Happens:**
- Plant becomes "exhausted"
- No further regrowth
- Must add new mist bag to continue

**Fresh Start:**
1. Remove exhausted culture
2. Add new mist bag
3. Inoculate with spore syringe
4. Begin new multi-flush cycle

---

## Drying Process

### Why Dry?

**Fresh Mushrooms:**
- Perishable (spoil over time)
- Cannot be stored long-term
- Must be dried for stability

**Dried Mushrooms:**
- Stable (no spoilage)
- Can be stored indefinitely
- Ready for sale or packaging

---

### Drying Racks

**Using Tobacco Drying Racks:**

**Small Drying Rack**
- Capacity: 3 slots
- Time: 1,200 ticks (60 seconds) per batch
- Cost: ~300€

**Medium Drying Rack**
- Capacity: 6 slots
- Time: 1,200 ticks per batch
- Cost: ~600€

**Big Drying Rack**
- Capacity: 9 slots
- Time: 1,200 ticks per batch
- Cost: ~900€

---

### Drying Process

**Steps:**
1. Place Drying Rack
2. Right-click with Fresh Mushrooms
3. Wait 60 seconds
4. Shift+right-click to extract Dried Mushrooms

**Quality:** Preserved (LEGENDAER → LEGENDAER)

**Example:**
```
Input: 6g Fresh Cubensis (SEHR_GUT quality)
Time: 1,200 ticks (60 seconds)
Output: 6g Dried Cubensis (SEHR_GUT quality)
```

---

## Climate Control

### Klimalampe (Climate Lamp)

**3 Tiers:**

#### 1. Klimalampe Small (Manual)
- **Price:** ~200€
- **Color:** Gray (§7)
- **Automatic:** No
- **Growth Bonus:** 0%
- **Quality Bonus:** 0%

**Manual Control:**
- Modes: OFF → COLD → WARM → OFF
- Light Emission: Cold=4, Warm=8, Off=0
- Must manually match strain needs

---

#### 2. Auto-Klimalampe Medium
- **Price:** ~500€
- **Color:** Yellow (§e)
- **Automatic:** Yes
- **Growth Bonus:** +10%
- **Quality Bonus:** 0%

**Auto Features:**
- Detects neighboring mushroom strains
- Auto-adjusts temperature mode:
  - Azurescens → COLD
  - Mexicana → WARM
  - Cubensis → OFF

---

#### 3. Premium-Klimalampe Large
- **Price:** ~1,200€
- **Color:** Gold (§6)
- **Automatic:** Yes
- **Growth Bonus:** +25%
- **Quality Bonus:** +10%

**Premium Features:**
- All auto features
- Highest growth speed boost
- Quality improvement chance
- Best for professional operations

---

### Temperature Modes

**OFF (⚫)**
- Neutral temperature
- For Cubensis strain
- Light level: 0

**COLD (§b❄)**
- Cold temperature
- For Azurescens strain
- Light level: 4

**WARM (§c🔥)**
- Warm temperature
- For Mexicana strain
- Light level: 8

---

### Wassertank (Water Tank)

**Automated Watering:**
- **Capacity:** 10,000 units (10 buckets)
- **Watering Rate:** 1 unit per 10 ticks to adjacent pots
- **Range:** Horizontally adjacent blocks only

**Usage:**
1. Place Water Tank next to pots
2. Fill with water bucket (adds 1,000 units)
3. Automatically waters adjacent pots during fruiting
4. Refill as needed

**Benefits:**
- No manual watering
- Consistent water supply
- Supports multiple pots
- Essential for large operations

---

## Quality System

### Quality Tiers

**4 Levels:**

| Quality | Price Multiplier | Yield Multiplier |
|---------|------------------|------------------|
| **SCHLECHT** (Poor) | 1.0× | 1.0× |
| **GUT** (Good) | 1.5× | 1.5× |
| **SEHR_GUT** (Very Good) | 2.5× | 2.5× |
| **LEGENDAER** (Legendary) | 5.0× | 5.0× |

**Default:** GUT (Good) quality

---

### Quality Improvement

**Quality Booster:**
- 15% chance per growth stage to upgrade
- Apply to pot before growth
- Cumulative chance across 8 stages
- Can reach LEGENDAER with luck

**Expected Outcomes:**
```
No Booster: 100% GUT quality
With Booster: ~50% SEHR_GUT, ~20% LEGENDAER
```

---

### Yield Formula

```java
baseYield = 6 (all strains)
qualityMultiplier = quality.yieldMultiplier (1.0-5.0)
fertilizerBonus = 1.67 (if applied)
flushPenalty = 1.0 - (currentFlush × 0.15)
randomVariation = ±20%

finalYield = min(10, baseYield × qualityMultiplier
             × fertilizerBonus × flushPenalty
             × randomVariation)
```

**Example (LEGENDAER, Fertilizer, 1st Flush):**
```
6 × 5.0 × 1.67 × 1.0 = 50.1 → Capped at 10g
```

---

## Equipment Guide

### Essential (Minimum)

**Starting Setup:**
1. ✅ 1× Plant Pot
2. ✅ 1× Small Mist Bag
3. ✅ 1× Spore Syringe
4. ✅ 1× Drying Rack (Small)
5. ✅ Darkness (low light area)

**Cost:** ~550€
**Capacity:** 1 pot, 4 flushes
**Production:** Slow but functional

---

### Intermediate

**Recommended:**
1. ✅ 4× Plant Pots
2. ✅ 2× Medium Mist Bags
3. ✅ 1× Auto-Klimalampe Medium
4. ✅ 1× Wassertank
5. ✅ 1× Medium Drying Rack

**Cost:** ~2,500€
**Capacity:** 4 pots, automated
**Production:** Moderate, 10% faster

---

### Advanced (Professional)

**Professional:**
1. ✅ 8× Plant Pots
2. ✅ 3× Large Mist Bags
3. ✅ 2× Premium-Klimalampe Large
4. ✅ 2× Wassertank (for full coverage)
5. ✅ 1× Big Drying Rack
6. ✅ Quality Booster supply

**Cost:** ~6,000€
**Capacity:** 8 pots, full automation
**Production:** Professional, 25% faster + quality

---

## Profitability Analysis

### Mexicana (Volume Strategy)

**Input Costs:**
```
Spore Syringe: 20€
Mist Bag (Small): 15€
Time: 3 seconds × 5 flushes = 15 seconds total

Total: 35€
```

**Output (5 Flushes, GUT Quality):**
```
Flush 1: 6g × 1.00 = 6.0g
Flush 2: 6g × 0.85 = 5.1g
Flush 3: 6g × 0.70 = 4.2g
Flush 4: 6g × 0.55 = 3.3g
Flush 5: 6g × 0.40 = 2.4g
Total: 21.0g

Price: 20 × 2.5 × 0.6 × 1.5 = 45€/g
Revenue: 21.0g × 45€ = 945€
Profit: 910€
Hourly Rate: 218,400€/hour
```

---

### Cubensis (Balanced Strategy)

**Input Costs:**
```
Spore Syringe: 30€
Mist Bag (Medium): 35€
Premium Klimalampe: 1,200€ (reusable)
Quality Booster: 50€
Time: 5 seconds × 4 flushes = 20 seconds

Variable Cost: 115€
```

**Output (4 Flushes, SEHR_GUT Quality):**
```
Flush 1: 6g × 1.00 × 2.5 = 15.0g → 10g (capped)
Flush 2: 6g × 0.85 × 2.5 = 12.8g → 10g (capped)
Flush 3: 6g × 0.70 × 2.5 = 10.5g → 10g (capped)
Flush 4: 6g × 0.55 × 2.5 = 8.3g
Total: 38.3g

Price: 30 × 2.5 × 1.0 × 2.5 = 187.5€/g
Revenue: 38.3g × 187.5€ = 7,181€
Profit: 7,066€
Hourly Rate: 1,271,880€/hour
```

---

### Azurescens (Premium Strategy)

**Input Costs:**
```
Spore Syringe: 60€
Mist Bag (Large): 60€
Premium Klimalampe: 1,200€ (reusable)
Quality Booster: 50€
Fertilizer: 20€
Time: 9 seconds × 3 flushes = 27 seconds

Variable Cost: 190€
```

**Output (3 Flushes, LEGENDAER Quality with Potency):**
```
Flush 1: 6g × 1.00 × 5.0 = 30g → 10g (capped)
Flush 2: 6g × 0.85 × 5.0 = 25.5g → 10g (capped)
Flush 3: 6g × 0.70 × 5.0 = 21g → 10g (capped)
Total: 30g (max from caps)

Price: 60 × 2.5 × 2.0 × 5.0 = 1,500€/g
Revenue: 30g × 1,500€ = 45,000€
Profit: 44,810€
Hourly Rate: 5,975,111€/hour
```

**Analysis:** Azurescens = HIGHEST profit in the game with flushes!

---

## Best Practices

### For Beginners

**Start Simple:**
1. Choose **Mexicana** (easiest, most flushes)
2. Use **Small Mist Bags**
3. Build in **dark room** (no lamps needed)
4. **Manual watering** initially
5. Target **GUT quality**
6. Get all 5 flushes

**First Goal:**
- Complete 5 flushes successfully
- Achieve 900€+ profit
- Learn flush mechanics

---

### For Intermediate

**Optimize:**
1. Switch to **Cubensis** (better profit/time)
2. Invest in **Auto-Klimalampe**
3. Add **Wassertank** (automation)
4. Use **Quality Booster**
5. Run **4-6 pots** simultaneously
6. Target **SEHR_GUT** quality

**Target:**
- Consistent SEHR_GUT quality
- 25,000€+/hour profit
- Automated watering

---

### For Advanced

**Maximum Profit:**
1. **Azurescens only** (2.0× potency)
2. **8× pots** with Premium Klimalampe
3. **Quality Booster** on every culture
4. **Fertilizer** for max yield caps
5. **Full automation** (water, climate)
6. Target **LEGENDAER + Fertilizer**

**Target:**
- 100% LEGENDAER quality
- 5,000,000€+/hour profit
- Industrial-scale operation

---

### Multi-Flush Optimization

**Maximize Total Yield:**

**Mexicana (5 flushes):**
```
21g total over 15 seconds
Best for: Speed farming
Strategy: Get all 5 flushes, replace immediately
```

**Cubensis (4 flushes):**
```
38.3g total (with SEHR_GUT) over 20 seconds
Best for: Balance
Strategy: Quality boosters, get all 4 flushes
```

**Azurescens (3 flushes):**
```
30g total (capped, LEGENDAER) over 27 seconds
Best for: Maximum profit
Strategy: Legendary + Fertilizer + 3 flushes = $45,000
```

---

### Resource Management

**Dark Room Design:**
```
Build Requirements:
- Fully enclosed room
- NO windows
- NO torches inside
- Door for access
- Outside lighting only

Verify: Place mushroom pot, check light level
Must be < 3-8 depending on strain
```

**Water Automation:**
```
1. Place Wassertank adjacent to each pot
2. Fill tanks before starting batch
3. Monitor water levels
4. Refill every ~5-10 flushes
```

**Climate Zones:**
```
Separate rooms for each strain:
- Cold Room: Azurescens (COLD lamps)
- Warm Room: Mexicana (WARM lamps)
- Neutral Room: Cubensis (OFF or no lamp)
```

---

<div align="center">

**Mushroom Production System - Master Guide**

For related systems:
- [🌿 Cannabis System](Cannabis-System.md)
- [🚬 Tobacco System](Tobacco-System.md)
- [💰 Economy & Sales](../features/Economy-System.md)

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production](../Production-Systems.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
