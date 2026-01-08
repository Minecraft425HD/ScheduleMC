# Cannabis Production System

<div align="center">

**8-Step Production Chain - 4 Strains, Skill-Based Quality**

Advanced production with trimming minigame and curing mechanics

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production Systems](../Production-Systems.md)

</div>

---

## 📋 Quick Reference

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐⭐⭐⭐ (4/5 - Advanced) |
| **Steps** | 8 (Plant → Grow → Harvest → Dry → Trim → Cure → Hash/Oil) |
| **Strains** | 4 (Indica, Sativa, Hybrid, Autoflower) |
| **Duration** | 30-70 minutes (growth) + 14-28 days (curing) |
| **Profitability** | ⭐⭐⭐⭐⭐ (5/5 - Excellent) |
| **Quality Tiers** | Schwag, Mids, Dank, Top Shelf, Exotic |
| **Final Products** | Buds, Hash, Oil |
| **Unique Mechanic** | Trimming minigame affects quality |

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Strains & Seeds](#strains--seeds)
3. [Growing Process](#growing-process)
4. [Drying Process](#drying-process)
5. [Trimming Minigame](#trimming-minigame)
6. [Curing Process](#curing-process)
7. [Hash Production](#hash-production)
8. [Oil Extraction](#oil-extraction)
9. [Quality System](#quality-system)
10. [Equipment Guide](#equipment-guide)
11. [Profitability Analysis](#profitability-analysis)
12. [Best Practices](#best-practices)
13. [Troubleshooting](#troubleshooting)

---

## Overview

Cannabis production is one of the most sophisticated systems in ScheduleMC, featuring skill-based quality determination through a trimming minigame, long-term curing mechanics, and multiple product pathways.

### Production Flow

```
1. PLANT → Cannabis seeds in pot
2. GROW → 8 growth stages (30-70 min)
3. HARVEST → Fresh cannabis buds
4. DRY → Dried buds (3 days)
5. TRIM → Trimming minigame (skill-based quality)
6. CURE → Curing jars (14-28 days, optional)
7. HASH → Hash press (from trim)
8. OIL → Oil extractor (from buds/trim)
```

### Why Choose Cannabis?

**Advantages:**
✅ Highest profit potential
✅ Skill-based quality control
✅ Multiple product types
✅ THC concentration system
✅ Long-term value growth (curing)

**Challenges:**
⏰ Longest total production time
🎮 Requires skill (trimming minigame)
💰 Higher initial investment
📚 Complex processing chain
⚖️ Weight-based inventory system

---

## Strains & Seeds

### 4 Cannabis Strains

#### 1. Indica (Purple) 💜

**Characteristics:**
- **Color:** Purple (§5)
- **Effect:** Relaxing, sedating, physical
- **THC Content:** 22.0%
- **CBD Content:** 1.0%
- **Best For:** Night-time products

**Seeds:** `cannabis_seed` (Indica strain)
- **Price:** 25€ per seed
- **Growth Time:** 120 ticks (fastest growth)
- **Flowering Days:** 56 days
- **Yield:** 6g base (up to 10g with fertilizer)

**Market:**
- High THC content (22%)
- Classic strain, consistent demand
- Good for beginners

---

#### 2. Sativa (Green) 💚

**Characteristics:**
- **Color:** Green (§a)
- **Effect:** Energetic, creative, euphoric
- **THC Content:** 18.0%
- **CBD Content:** 0.5%
- **Best For:** Day-time products

**Seeds:** `cannabis_seed` (Sativa strain)
- **Price:** 30€ per seed
- **Growth Time:** 160 ticks (slowest growth)
- **Flowering Days:** 70 days
- **Yield:** 6g base (up to 10g)

**Market:**
- Moderate THC (18%)
- Premium pricing due to slower growth
- Popular for creative users

---

#### 3. Hybrid (Yellow) 💛

**Characteristics:**
- **Color:** Yellow (§e)
- **Effect:** Balanced, versatile
- **THC Content:** 20.0%
- **CBD Content:** 2.0%
- **Best For:** All-purpose products

**Seeds:** `cannabis_seed` (Hybrid strain)
- **Price:** 35€ per seed (most expensive)
- **Growth Time:** 140 ticks (medium)
- **Flowering Days:** 63 days
- **Yield:** 6g base (up to 10g)

**Market:**
- Balanced THC/CBD profile
- Highest seed price
- Best overall strain
- Highest CBD content

---

#### 4. Autoflower (Aqua) 🩵

**Characteristics:**
- **Color:** Aqua (§b)
- **Effect:** Mild, beginner-friendly
- **THC Content:** 15.0%
- **CBD Content:** 3.0%
- **Best For:** Fast production

**Seeds:** `cannabis_seed` (Autoflower strain)
- **Price:** 20€ per seed (cheapest)
- **Growth Time:** 70 ticks (FASTEST!)
- **Flowering Days:** 42 days
- **Yield:** 6g base (up to 10g)

**Market:**
- Lowest THC (15%)
- Fastest growth (half time of Sativa)
- Beginner-friendly
- Highest CBD content
- Best for high-volume production

---

## Growing Process

### Step 1: Planting

**Requirements:**
- Cannabis seeds (any strain)
- Plant Pot (Terracotta, Ceramic, Iron, or Golden)
- Soil (15 units total consumed during growth)
- Water (varies by pot type)

**Process:**
1. Place pot in desired location
2. Add soil to pot (minimum 15 units)
3. Add water to pot
4. Right-click pot with cannabis seeds
5. Seed planted, growth begins

**Initial State:**
```
Growth Stage: 0/7 (Seedling)
Strain: [Indica/Sativa/Hybrid/Autoflower]
Quality: Pending
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
| 3 | Pre-Flower | 1 block | Larger | 2.14 units |
| 4 | Flowering 1 | **2 blocks** | Tall plant | 2.14 units |
| 5 | Flowering 2 | **2 blocks** | Buds forming | 2.14 units |
| 6 | Late Flower | **2 blocks** | Buds swelling | 2.14 units |
| 7 | **MATURE** | **2 blocks** | Ready to harvest | 2.14 units |

**Total Growth Time:**

```java
ticksPerStage = strainGrowthTicks / 8
```

| Strain | Growth Ticks | Ticks/Stage | Total Time |
|--------|--------------|-------------|------------|
| Autoflower | 70 | 8.75 | ~4 minutes |
| Indica | 120 | 15 | ~7 minutes |
| Hybrid | 140 | 17.5 | ~8 minutes |
| Sativa | 160 | 20 | ~9 minutes |

**Note:** Times are base values - boosters can halve these!

---

### Growth Modifiers

#### Pot Types

| Pot | Water Capacity | Durability | Special |
|-----|----------------|------------|---------|
| **Terracotta** | 100 units | Basic | Standard |
| **Ceramic** | 150 units | Good | Better water retention |
| **Iron** | 200 units | Excellent | Durable |
| **Golden** | 250 units | Best | +1 quality tier |

**Golden Pot Benefits:**
- Highest water capacity (250 units)
- Automatic +1 quality tier on harvest
- Worth the investment for premium production

---

#### Soil & Water

**Soil Consumption:**
- Total: 15 units per full growth cycle
- Per stage: ~2.14 units
- Must have enough soil or plant won't grow

**Water:**
- Consumed during growth
- Amount varies by pot type
- Empty water = growth stops
- Can refill anytime

**Tip:** Always maintain 20+ soil units and full water

---

#### Fertilizer

**Effect:** Increases yield

```java
baseYield = 6g (all strains)
withFertilizer = min(10g, baseYield * 1.67)
maxYield = 10g
```

**Trade-off:**
- ✅ +67% yield (6g → 10g)
- ❌ -1 quality tier

**Worth It?**
```
6g DANK quality buds (2.0x price)
vs
10g MIDS quality buds (1.0x price)

Result: 10g MIDS often better for volume sales
Use fertilizer for quantity over quality
```

---

#### Growth Booster

**Effect:** Halves growth time

```java
normalTime = strainGrowthTicks
boostedTime = strainGrowthTicks / 2

// Sativa example:
Normal: 160 ticks (~9 min)
Boosted: 80 ticks (~4.5 min)
```

**Trade-off:**
- ✅ 50% faster growth
- ❌ -1 quality tier

**Usage:**
- Apply to pot before/during growth
- Cannot stack with quality booster
- Best for high-volume production

---

#### Quality Booster

**Effect:** +1 quality tier on harvest

```java
normalQuality = DANK (default)
boostedQuality = TOP_SHELF (+1 tier)
```

**Trade-off:**
- ✅ Guaranteed +1 quality tier
- ❌ No yield increase
- ❌ Cannot stack with growth booster or fertilizer

**Usage:**
- Apply to pot before harvest
- Best for premium production
- Combine with Golden Pot for +2 tiers total

---

#### Grow Lights

**Effect:** Provides light for growth

**Types:**
- **Standard Grow Light:** Basic light source
- **Premium Grow Light:** Faster growth (20-50% boost)

**Placement:**
- Above or near plant pot
- Required if no natural light
- Premium lights = faster growth

---

### Step 2: Harvesting

**When to Harvest:**
- Stage 7/7 (fully mature)
- Visual: 2-block tall plant, fully developed buds
- Tooltip: "Ready to harvest"

**Early Harvest (Stage 4-6):**
- Yields: 50% of normal (3g instead of 6g)
- Quality: MIDS (downgraded)
- Not recommended

**Optimal Harvest (Stage 7):**
- Yields: 6g Fresh Buds (base)
- Quality: DANK (default)
- Full potential

**How to Harvest:**
1. Right-click fully mature plant (Stage 7)
2. Receive Fresh Cannabis Buds
3. Plant drops, pot becomes empty
4. Replant immediately for continuous production

**Yield Formula:**
```java
if (stage == 7) {
    baseYield = 6g
    if (fertilized) yield = min(10g, 6 * 1.67)
    if (goldenPot) quality = DANK → TOP_SHELF
} else if (stage >= 4 && stage <= 6) {
    yield = 3g (50% penalty)
    quality = MIDS (downgraded)
} else {
    yield = 0 (too early)
}
```

---

## Drying Process

### Step 3: Drying Cannabis

**Purpose:** Remove moisture from fresh buds for storage and processing

**Input:** Fresh Cannabis Buds 🌿
**Output:** Dried Cannabis Buds 🍂
**Duration:** 72,000 ticks (3 Minecraft days / ~60 minutes real-time)

---

### Trocknungsnetz (Drying Net)

**Block:** Trocknungsnetz (Drying Net)
**Capacity:** 4 slots
**Process:** Passive drying

**Technical Specs:**
```java
Capacity: 4 slots
Processing Time: 72,000 ticks per slot
Input: Fresh Cannabis Buds
Output: Dried Cannabis Buds
Preservation: Strain, Quality, Weight
```

---

### Drying Process

**Steps:**
1. Place Drying Net in desired location
2. Right-click with Fresh Cannabis Buds
3. Buds placed in available slot (max 4)
4. Wait 72,000 ticks (3 days)
5. Collect Dried Cannabis Buds

**Example:**
```
Input: 6g Fresh Hybrid Buds (DANK quality)
Time: 72,000 ticks (3 Minecraft days)
Output: 6g Dried Hybrid Buds (DANK quality)

Weight: Preserved (6g → 6g)
Quality: Preserved (DANK → DANK)
Strain: Preserved (Hybrid → Hybrid)
```

**Multiple Slots:**
```
Slot 1: 6g Indica (Day 0)
Slot 2: 10g Sativa (Day 1)
Slot 3: 8g Hybrid (Day 2)
Slot 4: 6g Autoflower (Day 3)

Each slot processes independently
Can fill slots at different times
```

---

### Drying Tips

**Optimal Workflow:**
1. Harvest every 3 days
2. Immediately place in Drying Net
3. By the time next harvest is ready, dried buds are done
4. Continuous production cycle

**Do NOT:**
- Mix strains in same slot (each slot = one strain)
- Remove early (no partial drying)
- Overfill slots (max stack size: 16g per slot)

---

## Trimming Minigame

### Step 4: Trimming Station

**Purpose:** Remove excess leaves, determine final quality through skill

**Input:** Dried Cannabis Buds 🍂
**Output:** Trimmed Cannabis Buds ✂ + Cannabis Trim 🍃
**Duration:** Interactive (player skill-based)

**This is the MOST IMPORTANT step for quality!**

---

### Trimm Station (Trimming Station)

**Block:** Trimm Station
**Mechanic:** Timing-based minigame
**Difficulty:** Medium-High

**Technical Specs:**
```java
Cycle Duration: 100 ticks (5 seconds)
Leaves to Trim: 10
Perfect Window: ±5 ticks from center (tick 45-55)
Good Window: ±15 ticks from center (tick 35-65)
Bad Window: Everything else
```

---

### Trimming Minigame Mechanics

**How It Works:**

1. Place Dried Buds in Trimming Station
2. Minigame starts automatically
3. A visual indicator sweeps across a bar
4. Press [SPACE] or [Click] when indicator is in the green zone
5. Repeat 10 times (10 leaves to trim)
6. Final quality determined by your performance

**Visual:**
```
[====|=====GREEN=====|====]
     ←  INDICATOR  →

Perfect Zone (Center): ███ (ticks 45-55)
Good Zone: ███████████ (ticks 35-65)
Bad Zone: Everything else
```

---

### Timing Zones

**Perfect Trim:**
- Window: Ticks 45-55 (±5 from center)
- Points: 2 points
- Visual: Bright green, satisfying sound
- Effect: Highest quality contribution

**Good Trim:**
- Window: Ticks 35-65 (±15 from center)
- Points: 1 point
- Visual: Yellow, acceptable sound
- Effect: Medium quality contribution

**Bad Trim:**
- Window: All other ticks
- Points: 0 points
- Visual: Red, failure sound
- Effect: No quality contribution

---

### Quality Calculation

**Formula:**
```java
perfectTrims = count of perfect hits (2 points each)
goodTrims = count of good hits (1 point each)
badTrims = count of bad hits (0 points each)

score = (perfectTrims * 1.0 + goodTrims * 0.6 + badTrims * 0.2) / 10

finalQuality = max(baseQuality, fromTrimScore(score))
```

**Score → Quality Mapping:**
```java
score >= 0.95 → EXOTIC (5.0x price)
score >= 0.80 → TOP_SHELF (3.5x price)
score >= 0.60 → DANK (2.0x price)
score >= 0.40 → MIDS (1.0x price)
score < 0.40 → SCHWAG (0.5x price)
```

---

### Trimming Examples

#### Example 1: Expert Trimmer
```
Perfect Trims: 10/10
Good Trims: 0/10
Bad Trims: 0/10

Score: (10 * 1.0 + 0 * 0.6 + 0 * 0.2) / 10 = 1.0
Quality: EXOTIC (5.0x price multiplier!)
```

#### Example 2: Skilled Trimmer
```
Perfect Trims: 8/10
Good Trims: 2/10
Bad Trims: 0/10

Score: (8 * 1.0 + 2 * 0.6 + 0 * 0.2) / 10 = 0.92
Quality: TOP_SHELF (3.5x price)
```

#### Example 3: Average Trimmer
```
Perfect Trims: 4/10
Good Trims: 4/10
Bad Trims: 2/10

Score: (4 * 1.0 + 4 * 0.6 + 2 * 0.2) / 10 = 0.68
Quality: DANK (2.0x price)
```

#### Example 4: Poor Trimmer
```
Perfect Trims: 1/10
Good Trims: 3/10
Bad Trims: 6/10

Score: (1 * 1.0 + 3 * 0.6 + 6 * 0.2) / 10 = 0.40
Quality: MIDS (1.0x price)
```

---

### Trimming Output

**Trimmed Buds:**
- Amount: Same as input (1:1 ratio)
- Quality: Determined by minigame performance
- Strain: Preserved

**Cannabis Trim (By-product):**
- Amount: 50% of input weight
- Example: 10g input → 5g trim
- Use: Hash production or oil extraction
- Value: Lower than buds but still sellable

**Example:**
```
Input: 10g Dried Hybrid Buds (DANK)
Trimming Performance: 0.85 score

Output:
→ 10g Trimmed Hybrid Buds (TOP_SHELF)
→ 5g Hybrid Trim
```

---

### Trimming Tips

**Improving Your Score:**
1. **Practice:** The more you trim, the better you get
2. **Focus:** Don't get distracted during the 10 trims
3. **Rhythm:** The indicator moves at a constant speed
4. **Anticipate:** Learn to predict when it hits the center
5. **Audio Cues:** Listen for sound cues (if enabled)

**Optimal Strategy:**
- Aim for 8+ perfect trims
- Acceptable to get 1-2 good trims
- Avoid bad trims at all costs
- Target score: 0.80+ (TOP_SHELF minimum)

---

## Curing Process

### Step 5: Curing Jars (Optional)

**Purpose:** Long-term aging to improve quality and price

**Input:** Trimmed Cannabis Buds ✂
**Output:** Cured Cannabis Buds 🫙
**Duration:** 14-28+ days (336,000-672,000+ ticks)

**Is Curing Worth It?**
- ✅ **YES** for long-term investment
- ✅ **YES** for premium product lines
- ✅ **YES** if you have storage space
- ❌ **NO** if you need quick cash
- ❌ **NO** for high-volume low-quality production

---

### Curing Glas (Curing Jar)

**Block:** Curing Glas (Curing Jar)
**Capacity:** 1 stack (max 16g)
**Process:** Passive aging
**Minimum Time:** 14 days
**Optimal Time:** 28+ days

**Technical Specs:**
```java
Input: Trimmed Cannabis Buds
Output: Cured Cannabis Buds
Minimum Time: 336,000 ticks (14 days)
Optimal Time: 672,000 ticks (28 days)
Max Bonus Time: 1,440,000 ticks (60 days)
```

---

### Curing Mechanics

**Quality Upgrades:**
```java
if (curingDays >= 28 && quality < EXOTIC) {
    quality.upgrade().upgrade() // +2 tiers
} else if (curingDays >= 14 && quality < EXOTIC) {
    quality.upgrade() // +1 tier
}
```

**Quality Progression Examples:**
```
MIDS → 14 days → DANK (+1 tier)
MIDS → 28 days → TOP_SHELF (+2 tiers)

DANK → 14 days → TOP_SHELF (+1 tier)
DANK → 28 days → EXOTIC (+2 tiers)

TOP_SHELF → 14 days → EXOTIC (+1 tier)
TOP_SHELF → 28 days → EXOTIC (already max)

EXOTIC → Already maximum quality
```

---

### Price Bonus

**Formula:**
```java
curingBonus = min(curingDays / 60.0, 0.5) // Max +50% at 60 days
finalPrice = basePrice * (1.0 + curingBonus)
```

**Curing Time → Price Bonus:**
```
14 days: +23.3% price
28 days: +46.7% price
30 days: +50% price (near max)
60 days: +50% price (maximum)
```

**Combined Effect:**
```
Start: 6g DANK Hybrid → 840€ base value (140€/g)

After 14 days:
Quality: TOP_SHELF (3.5x instead of 2.0x)
Price Bonus: +23.3%
Value: 140 * 3.5 * 1.233 = ~603€/g → 3,618€ total

After 28 days:
Quality: EXOTIC (5.0x instead of 2.0x)
Price Bonus: +46.7%
Value: 140 * 5.0 * 1.467 = ~1,027€/g → 6,162€ total

ROI: 840€ → 6,162€ (+633% value increase!)
```

---

### Curing Strategy

**Short Cure (14 days):**
- +1 quality tier
- +23% price
- Faster turnover
- Good for volume production

**Long Cure (28 days):**
- +2 quality tiers
- +47% price
- Maximum quality potential
- Best for premium products

**Extended Cure (30-60 days):**
- No additional quality (already max at 28 days)
- Price bonus: 47% → 50% (minor gain)
- Only worth if you have excess storage

**Recommendation:**
```
MIDS/DANK quality: Cure 28 days (maximize quality)
TOP_SHELF quality: Cure 14 days (already near-max)
EXOTIC quality: Sell immediately (already perfect)
```

---

## Hash Production

### Step 6: Hash Presse (Hash Press)

**Purpose:** Convert low-value trim into concentrated hash

**Input:** Cannabis Trim 🍃 (20g minimum, single strain)
**Output:** Hash 🟤
**Duration:** 6,000 ticks (5 minutes)

---

### Hash Press Mechanics

**Technical Specs:**
```java
Minimum Input: 20g trim (same strain)
Processing Time: 6,000 ticks (5 minutes)
Conversion Rate: 25% (4:1 ratio)
THC Concentration: strain.thcContent * 1.5
Price Multiplier: 1.5x base bud price
```

**Conversion Formula:**
```java
hashWeight = (int)(trimWeight * 0.25)

Examples:
20g trim → 5g hash
40g trim → 10g hash
80g trim → 20g hash
```

---

### Hash Quality by Input Weight

**Quality Tiers:**
```java
if (trimWeight >= 80g) → TOP_SHELF
else if (trimWeight >= 50g) → DANK
else if (trimWeight >= 30g) → MIDS
else → SCHWAG
```

**Examples:**
```
Input: 20g Indica Trim
Output: 5g Indica Hash (SCHWAG quality)
THC: 22% * 1.5 = 33%
Price: (25 * 2 * 2.2) * 0.5 * 1.5 = 82.5€/g

Input: 80g Indica Trim
Output: 20g Indica Hash (TOP_SHELF quality)
THC: 22% * 1.5 = 33%
Price: (25 * 2 * 2.2) * 3.5 * 1.5 = 577.5€/g
```

---

### Hash Production Strategy

**Trim Collection:**
- Every 10g trimmed buds → 5g trim
- Need to trim 40g buds → 20g trim → 5g hash (minimum)
- Need to trim 160g buds → 80g trim → 20g TOP_SHELF hash

**Profitability:**
```
Trim: Low value (~10-20€/g)
Hash (SCHWAG): 80-100€/g
Hash (TOP_SHELF): 500-600€/g

Conversion:
20g trim (400€) → 5g SCHWAG hash (412.5€)
Profit: +12.5€ (minimal)

80g trim (1,600€) → 20g TOP_SHELF hash (11,550€)
Profit: +9,950€ (MASSIVE!)
```

**Recommendation:**
- **Don't** make hash from small batches (<50g trim)
- **Do** collect trim until you have 80g+
- **Always** use same strain for maximum quality
- **Target:** TOP_SHELF hash (80g+ trim input)

---

## Oil Extraction

### Step 7: Öl Extraktor (Oil Extractor)

**Purpose:** Create highest-concentration cannabis product

**Input:** Trimmed Buds OR Trim (10g minimum, single strain) + Extraction Solvent
**Output:** Cannabis Oil 🧪
**Duration:** 12,000 ticks (10 minutes)

---

### Oil Extractor Mechanics

**Technical Specs:**
```java
Minimum Input: 10g material (buds or trim, single strain)
Processing Time: 12,000 ticks (10 minutes)
Requires: 1x Extraction Solvent per batch
THC Concentration: strain.thcContent * 3
Price Multiplier: 3.0x base bud price
```

**Conversion Rates:**
```java
BUD_CONVERSION_RATE = 0.15 (15%)
TRIM_CONVERSION_RATE = 0.08 (8%)

Examples (Buds):
10g Trimmed Buds → 1.5ml Oil
20g Trimmed Buds → 3ml Oil
50g Trimmed Buds → 7.5ml Oil

Examples (Trim):
10g Trim → 0.8ml Oil
50g Trim → 4ml Oil
100g Trim → 8ml Oil
```

---

### Oil Quality

**From Trimmed Buds:**
```java
finalQuality = baseQuality.upgrade() // +1 tier

Examples:
MIDS Buds → DANK Oil
DANK Buds → TOP_SHELF Oil
TOP_SHELF Buds → EXOTIC Oil
```

**From Trim:**
```java
finalQuality = MIDS (always)

All trim → MIDS quality oil (regardless of input)
```

---

### Oil Production Examples

#### Example 1: From Premium Buds
```
Input: 10g TOP_SHELF Hybrid Buds
Solvent: 1x Extraction Solvent

Output: 1.5ml EXOTIC Hybrid Oil
THC: 20% * 3 = 60%
Quality: EXOTIC (5.0x price)

Price Calculation:
basePrice = 35 * 2 = 70€
gramPrice = 70 * 2.0 * 5.0 = 700€
oilPrice = 700 * 3.0 = 2,100€/ml

Total Value: 1.5ml * 2,100€ = 3,150€
```

#### Example 2: From Trim
```
Input: 50g Indica Trim
Solvent: 1x Extraction Solvent

Output: 4ml MIDS Indica Oil
THC: 22% * 3 = 66%
Quality: MIDS (1.0x price, fixed)

Price Calculation:
basePrice = 25 * 2 = 50€
gramPrice = 50 * 2.2 * 1.0 = 110€
oilPrice = 110 * 3.0 = 330€/ml

Total Value: 4ml * 330€ = 1,320€
```

---

### Oil Strategy

**When to Use Buds:**
- ✅ Want EXOTIC quality oil
- ✅ Have excess TOP_SHELF buds
- ✅ Premium product line
- ✅ Highest price per ml

**When to Use Trim:**
- ✅ Want to use up trim by-product
- ✅ Volume production
- ✅ Lower profit margin acceptable
- ✅ Efficient use of waste material

**ROI Comparison:**
```
10g TOP_SHELF Buds:
Sell as-is: 490€/g * 10 = 4,900€
Convert to oil: 1.5ml EXOTIC @ 2,100€/ml = 3,150€
Loss: -1,750€

10g Trim:
Sell as-is: 15€/g * 10 = 150€
Convert to oil: 0.8ml MIDS @ 330€/ml = 264€
Gain: +114€

Conclusion: Use TRIM for oil, sell BUDS as-is!
```

---

## Quality System

### Quality Tiers

**5 Quality Levels:**

| Quality | Color | Level | Price Multiplier | Description |
|---------|-------|-------|------------------|-------------|
| **SCHWAG** | Gray (§8) | 0 | 0.5x (50%) | Poor quality with seeds |
| **MIDS** | Gray (§7) | 1 | 1.0x (100%) | Average quality |
| **DANK** | Green (§a) | 2 | 2.0x (200%) | Good quality |
| **TOP_SHELF** | Gold (§6) | 3 | 3.5x (350%) | Premium quality |
| **EXOTIC** | Magenta Bold (§d§l) | 4 | 5.0x (500%) | Best quality |

---

### Quality Progression Paths

**Path 1: Golden Pot + Perfect Trimming**
```
Start: Plant with Golden Pot
Harvest: TOP_SHELF (Golden Pot bonus)
Trimming: Perfect score (1.0) → EXOTIC
Result: EXOTIC quality without curing!
```

**Path 2: Standard + Trimming + Curing**
```
Start: Plant in Terracotta Pot
Harvest: DANK (default)
Trimming: Good score (0.80) → TOP_SHELF
Curing: 28 days → EXOTIC
Result: EXOTIC quality (slower but cheaper)
```

**Path 3: Volume Production**
```
Start: Plant with Fertilizer
Harvest: MIDS (fertilizer penalty)
Trimming: Average score (0.65) → DANK
Curing: Skip (sell immediately)
Result: DANK quality, high volume
```

---

### Achieving EXOTIC Quality

**Method 1: Speed (Expensive)**
```
1. Golden Pot (+1 tier: DANK → TOP_SHELF)
2. Perfect Trimming (score 0.95+: → EXOTIC)
Total Time: ~60 minutes
Cost: High (Golden Pot + no fertilizer)
```

**Method 2: Time (Cheaper)**
```
1. Standard Pot (DANK default)
2. Good Trimming (score 0.80: → TOP_SHELF)
3. Cure 28 days (+2 tiers: → EXOTIC)
Total Time: 28 days + growth
Cost: Low (Terracotta Pot)
```

**Method 3: Balanced**
```
1. Iron Pot (DANK default)
2. Perfect Trimming (score 0.95+: → EXOTIC)
Total Time: ~60 minutes
Cost: Medium
```

---

## Equipment Guide

### Essential Equipment (Minimum)

**Starting Setup:**
1. ✅ 1× Terracotta Pot
2. ✅ Soil & Water supply
3. ✅ 1× Trocknungsnetz (Drying Net)
4. ✅ 1× Trimm Station

**Cost:** ~1,500€
**Capacity:** 1 plant at a time
**Production:** Slow but functional

---

### Intermediate Setup

**Recommended:**
1. ✅ 4× Iron Pots
2. ✅ 2× Drying Nets (8 slots total)
3. ✅ 1× Trimm Station
4. ✅ 1× Curing Glas
5. ✅ Standard Grow Lights
6. ✅ Soil/Water auto-refill system

**Cost:** ~6,000€
**Capacity:** 4 plants simultaneously
**Production:** Moderate, good quality

---

### Advanced Setup (Premium)

**Professional:**
1. ✅ 8× Golden Pots
2. ✅ 4× Drying Nets (16 slots total)
3. ✅ 2× Trimm Stations (parallel trimming)
4. ✅ 4× Curing Jars (64g curing capacity)
5. ✅ 1× Hash Press
6. ✅ 1× Oil Extractor
7. ✅ Premium Grow Lights (all pots)
8. ✅ Automated soil/water system

**Cost:** ~30,000€
**Capacity:** 8 plants, full processing
**Production:** Professional operation
**ROI:** 20-30 production cycles

---

### Processing Equipment Comparison

| Equipment | Cost | Capacity | Time | Output |
|-----------|------|----------|------|--------|
| **Drying Net** | 500€ | 4 slots | 3 days | Dried Buds |
| **Trimm Station** | 800€ | 1 player | Interactive | Trimmed Buds + Trim |
| **Curing Jar** | 300€ | 16g | 14-28 days | Cured Buds |
| **Hash Press** | 1,200€ | 20g+ trim | 5 min | Hash |
| **Oil Extractor** | 1,500€ | 10g+ material | 10 min | Oil |

---

## Profitability Analysis

### Autoflower Production (Speed)

**Input Costs:**
```
Seeds: 20€
Pot: 50€ (Terracotta, reusable)
Soil: 10€
Water: 5€
Time: 30 minutes (fastest strain + growth booster)

Total Variable Cost: 35€/cycle
```

**Output:**
```
Yield: 6g Fresh Buds
After Drying: 6g Dried Buds
After Trimming (0.75 score): 6g DANK Buds
Quality: DANK (2.0x price)

Price: (20 * 2) * (15% THC / 10) * 2.0 = 120€/g
Revenue: 6g * 120€ = 720€
Profit: 685€
Hourly Rate: 1,370€/hour
```

---

### Hybrid Production (Premium)

**Input Costs:**
```
Seeds: 35€
Golden Pot: 200€ (reusable)
Growth Booster: 30€
Extraction Solvent: 50€
Time: 45 minutes

Total Variable: 115€
```

**Output (Oil Path):**
```
Harvest: 6g TOP_SHELF Buds (Golden Pot bonus)
After Trimming (0.95 score): 6g EXOTIC Buds
Oil Extraction: 6g → 0.9ml EXOTIC Oil

THC: 20% * 3 = 60%
Quality: EXOTIC (5.0x)
Price: (35 * 2) * 2.0 * 5.0 * 3.0 = 2,100€/ml

Revenue: 0.9ml * 2,100€ = 1,890€
Profit: 1,775€
Hourly Rate: 2,367€/hour
```

**Analysis:** Oil from premium buds = highest $/hour

---

### Scale Production (8 Golden Pots)

**Per Cycle:**
```
Seeds: 280€ (8 Hybrid plants)
Growth Boosters: 240€
Soil/Water: 80€
Time: 45 minutes

Cost: 600€
Output: 48g EXOTIC Buds (6g * 8 plants)
Revenue: 48g * 700€/g = 33,600€
Profit: 33,000€

Hourly Rate: 44,000€/hour
Daily (8 hours): 352,000€
Monthly: 10,560,000€
```

**ROI:** 30,000€ setup → Break-even in 1 hour!

---

### Curing Investment

**Example: 48g DANK Hybrid**
```
Immediate Sale:
48g DANK @ 280€/g = 13,440€

After 28 Days Curing:
48g EXOTIC @ 1,027€/g = 49,296€

Profit Increase: +35,856€ (+267%)
Daily Interest: 1,281€/day
Weekly: 8,967€/week

Worth It? ABSOLUTELY for long-term
```

---

## Best Practices

### For Beginners

**Start Simple:**
1. Choose **Autoflower** strain (fastest, easiest)
2. Use **Terracotta or Iron Pots** (affordable)
3. Grow **2-3 plants** initially
4. **Practice trimming minigame** (crucial skill)
5. Skip curing (sell dried buds immediately)
6. Focus on **volume over quality**

**First Cycle Goal:**
- Understand full growth cycle
- Achieve DANK quality (2.0x price)
- Earn 500€+ profit

---

### For Intermediate

**Optimize:**
1. Upgrade to **Golden Pots** (ROI is worth it)
2. Master **trimming minigame** (aim for 0.80+ score)
3. Run **4-6 plants** simultaneously
4. Target **TOP_SHELF quality** consistently
5. Start **short curing** (14 days)
6. Use **trim for hash** (80g+ batches)

**Target:**
- 90% TOP_SHELF quality on trimming
- 2,000€+/hour profit
- Efficient workflow

---

### For Advanced

**Scale & Specialize:**
1. **8-plant operation** (Golden Pots)
2. **Perfect trimming** (EXOTIC quality)
3. **28-day curing** for premium line
4. **Oil extraction** from select TOP_SHELF buds
5. **Hash production** from all trim (80g+ batches)
6. **Investment** in full equipment suite

**Target:**
- 95%+ EXOTIC quality
- 5,000€+/hour profit
- Multiple product lines (Buds, Hash, Oil)

---

### Trimming Mastery

**Practice Routine:**
1. **Session 1-5:** Aim for 50% perfect trims
2. **Session 6-10:** Aim for 70% perfect trims
3. **Session 11+:** Aim for 80%+ perfect trims

**Improvement Tips:**
- **Count:** "1, 2, 3" rhythm as indicator moves
- **Sound:** Enable audio cues if available
- **Muscle Memory:** Practice daily
- **No Distractions:** Focus during 10 trims
- **Review:** Check score after each session

**Score Targets:**
```
Beginner: 0.50-0.65 (DANK)
Intermediate: 0.70-0.85 (TOP_SHELF)
Advanced: 0.85-0.95 (TOP_SHELF to EXOTIC)
Expert: 0.95-1.00 (EXOTIC guaranteed)
```

---

### Time Management

**Optimal Workflow (8 Plants):**
```
Minute 0: Plant 8 seeds (Autoflower)
Minute 5: Water all plants
Minute 30: Harvest all (staggered)
Minute 32: Place in Drying Nets (2 nets)

Day 3: Collect dried buds
Day 3 + 10 min: Trim all buds (2 sessions)
Day 3 + 20 min:
  - Sell trimmed buds immediately, OR
  - Place in Curing Jars (for 28 days)

Replant immediately, continuous cycle
```

**Continuous Production:**
- Always have plants growing
- Always have buds drying
- Always have buds curing (long-term storage)
- Never idle time

---

## Troubleshooting

### "Plant Not Growing"

**Causes:**
1. Insufficient soil (need 15 units total)
2. No water in pot
3. No light source (if indoors)
4. Server lag

**Solutions:**
```
✓ Check soil level: /checkpot or hover tooltip
✓ Refill water to maximum
✓ Add Grow Light above pot
✓ Check server TPS: /tps
```

---

### "Low Yield on Harvest"

**Causes:**
1. Early harvest (Stage 4-6 instead of 7)
2. No fertilizer used
3. Random variation

**Solutions:**
```
✓ Wait until Stage 7/7 (2-block tall, fully mature)
✓ Use fertilizer for 10g yield (vs 6g base)
✓ Check plant tooltip for "Ready to harvest"
```

---

### "Poor Quality After Trimming"

**Causes:**
1. Low trimming score (<0.60)
2. Many bad trims
3. Low base quality from harvest

**Solutions:**
```
✓ Practice trimming minigame
✓ Focus during trimming (no distractions)
✓ Use Golden Pot for better base quality
✓ Use Quality Booster before harvest
```

---

### "Hash Quality is SCHWAG"

**Causes:**
1. Only used 20-30g trim (minimum amount)
2. Mixed strains in hash press

**Solutions:**
```
✓ Collect 80g+ trim of SAME strain
✓ Only use single strain per hash batch
✓ TOP_SHELF hash requires 80g+ input
```

---

### "Curing Not Improving Quality"

**Causes:**
1. Already at EXOTIC quality (can't upgrade further)
2. Only cured 7-13 days (need 14+ for upgrade)
3. Removed too early

**Solutions:**
```
✓ Cure minimum 14 days for +1 tier
✓ Cure minimum 28 days for +2 tiers
✓ Check tooltip for curing days progress
✓ EXOTIC quality can't be upgraded (already max)
```

---

### "Oil Quality Lower Than Expected"

**Causes:**
1. Used trim instead of buds (always MIDS)
2. Base bud quality was low

**Solutions:**
```
✓ Use Trimmed Buds for quality oil (not trim)
✓ Start with TOP_SHELF buds → EXOTIC oil
✓ Trim is always MIDS quality oil
```

---

<div align="center">

**Cannabis Production System - Master Guide**

For related systems:
- [🚬 Tobacco System](Tobacco-System.md)
- [💰 Economy & Sales](../features/Economy-System.md)
- [🏪 NPC Shops](../features/NPC-System.md)

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production](../Production-Systems.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
