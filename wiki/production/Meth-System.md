# Methamphetamine Production System

<div align="center">

**4-Stage Lab Process - Temperature Control & Explosion Risk**

Breaking Bad inspired with Blue Sky quality system

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production Systems](../Production-Systems.md)

</div>

---

## 📋 Quick Reference

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐⭐⭐⭐⭐ (5/5 - Expert) |
| **Steps** | 4 (Mix → Reduce → Crystallize → Dry) |
| **Precursors** | Ephedrin or Pseudoephedrin (premium) |
| **Duration** | 2 minutes (full cycle) |
| **Profitability** | ⭐⭐⭐⭐⭐ (5/5 - Highest per cycle) |
| **Quality Tiers** | Standard (White), Gut (Yellow), Blue Sky (96-99%) |
| **Danger Level** | ⚠️ **EXTREME - EXPLOSION RISK** ⚠️ |
| **Unique Mechanic** | Temperature-based quality + explosion mechanics |

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Precursors & Ingredients](#precursors--ingredients)
3. [Lab Equipment](#lab-equipment)
4. [Mixing Process](#mixing-process)
5. [Reduction Process (DANGER)](#reduction-process-danger)
6. [Crystallization](#crystallization)
7. [Drying Process](#drying-process)
8. [Quality System](#quality-system)
9. [Explosion Mechanics](#explosion-mechanics)
10. [Profitability Analysis](#profitability-analysis)
11. [Best Practices](#best-practices)
12. [Troubleshooting](#troubleshooting)

---

## Overview

Methamphetamine production is the most dangerous and skill-intensive system in ScheduleMC. Inspired by Breaking Bad, it features a realistic temperature-controlled reduction process with genuine explosion risks, rewarding skilled chemists with legendary "Blue Sky" quality meth at 96-99% purity.

### Production Flow

```
1. MIX → Ephedrin + Phosphor + Jod → Meth-Paste
2. REDUCE → Temperature control (DANGER!) → Roh-Meth
3. CRYSTALLIZE → Passive crystallization → Kristall-Meth
4. DRY → Vacuum drying → Final Meth

⚠️ CRITICAL: Stage 2 requires constant monitoring or EXPLOSION!
```

### Why Choose Meth?

**Advantages:**
✅ Fastest production cycle (2 minutes)
✅ Highest quality multiplier (5.0× for Blue Sky)
✅ Extreme profit margins
✅ Skill-based quality determination
✅ Breaking Bad easter eggs

**Challenges:**
⚠️ **EXPLOSION RISK** (lose equipment + take damage)
📚 Complex temperature management
💰 Expensive precursors
🎮 Requires constant monitoring
🔬 Precision timing critical

**WARNING:** This is NOT a beginner system!

---

## Precursors & Ingredients

### Base Precursors (Choose ONE)

#### 1. Ephedrin (Standard)

**Item:** `ephedrin`
- **Type:** White powder
- **Stack Size:** 64
- **Price:** ~100€ per unit (NPC shops)
- **Quality Bonus:** None (standard 25% for GUT)

**Properties:**
- Basic precursor
- Standard quality chances
- Widely available
- Lower cost

**Usage:**
```
1 Ephedrin + 1 Roter Phosphor + 1 Jod = 1 Meth-Paste
Base quality: 25% chance for GUT
```

---

#### 2. Pseudoephedrin (Premium) ⭐

**Item:** `pseudoephedrin`
- **Type:** White crystalline powder
- **Stack Size:** 64
- **Price:** ~150€ per unit (NPC shops)
- **Quality Bonus:** +10% (35% total for GUT!)

**Properties:**
- **PREMIUM PRECURSOR**
- Higher quality chances (+10%)
- Harder to obtain
- Higher cost (+50%)

**Usage:**
```
1 Pseudoephedrin + 1 Roter Phosphor + 1 Jod = 1 Meth-Paste
Enhanced quality: 35% chance for GUT

Comparison:
Ephedrin: 25% GUT chance
Pseudoephedrin: 35% GUT chance (+40% better!)
```

**Recommendation:** Always use Pseudoephedrin for premium production

---

### Catalysts (REQUIRED)

#### Roter Phosphor (Red Phosphorus)

**Item:** `roter_phosphor`
- **Type:** Red powder
- **Stack Size:** 64
- **Price:** ~50€ per unit
- **Warning:** ⚠️ "Leicht entzündlich" (highly flammable)

**Properties:**
- Catalyzer for reaction
- Required for every batch
- Handle with care (lore only, no actual fire risk)

---

#### Jod (Iodine)

**Item:** `jod`
- **Type:** Dark purple crystals
- **Stack Size:** 64
- **Price:** ~50€ per unit

**Properties:**
- Essential catalyst
- Required for every batch
- Triggers mixing start when added

---

### Cost Summary per Batch

**Using Ephedrin (Standard):**
```
1 Ephedrin: 100€
1 Roter Phosphor: 50€
1 Jod: 50€
────────────────────
Total: 200€ per gram of final meth
```

**Using Pseudoephedrin (Premium):**
```
1 Pseudoephedrin: 150€
1 Roter Phosphor: 50€
1 Jod: 50€
────────────────────
Total: 250€ per gram of final meth
```

---

## Lab Equipment

### Complete 4-Stage Lab Setup

#### Stage 1: Chemie-Mixer (Chemistry Mixer)

**Block:** `chemie_mixer`
- **Function:** Mixes precursors into meth paste
- **Capacity:** 4 batches simultaneously
- **Processing Time:** 600 ticks (30 seconds) per batch
- **Properties:** Metal block, 3.0F strength
- **Cost:** ~800€

**Usage:**
1. Right-click with Ephedrin/Pseudoephedrin
2. Right-click with Roter Phosphor
3. Right-click with Jod (triggers mixing)
4. Wait 30 seconds
5. Extract Meth-Paste with empty hand

---

#### Stage 2: Reduktionskessel (Reduction Kettle) ⚠️

**Block:** `reduktionskessel`
- **Function:** Reduces meth paste to raw meth via temperature control
- **Capacity:** 1 batch at a time
- **Processing Time:** 400 ticks (20 seconds) **with monitoring**
- **Properties:** Metal block, 4.0F strength, **light level 4** (glows)
- **Cost:** ~1,500€
- **WARNING:** ⚠️ **EXPLOSION RISK IF TEMPERATURE > 150°C!**

**Critical Equipment:**
- **GUI Interface:** Real-time temperature display
- **Manual Heater:** Player-controlled ON/OFF button
- **Temperature Zones:** Color-coded safety indicators
- **Explosion Threshold:** 151°C (instant kaboom!)

---

#### Stage 3: Kristallisator (Crystallizer)

**Block:** `kristallisator`
- **Function:** Crystallizes raw meth into wet crystals
- **Capacity:** 4 batches simultaneously
- **Processing Time:** 800 ticks (40 seconds) per batch
- **Properties:** Glass block, 3.5F strength
- **Cost:** ~600€

**Usage:**
1. Add Roh-Meth
2. Wait 40 seconds (passive)
3. Extract Kristall-Meth (wet crystals)

**Quality Chance:**
- 15% chance to upgrade quality +1 tier
- Example: STANDARD → GUT (15% chance)

---

#### Stage 4: Vakuum-Trockner (Vacuum Dryer)

**Block:** `vakuum_trockner`
- **Function:** Dries wet crystals to final product
- **Capacity:** 6 batches simultaneously
- **Processing Time:** 600 ticks (30 seconds) per batch
- **Properties:** Metal block, 3.5F strength
- **Cost:** ~500€

**Usage:**
1. Add Kristall-Meth (wet)
2. Wait 30 seconds (passive)
3. Extract final Meth product

**Quality:** Preserved (no change)

---

### Lab Setup Cost

**Complete Lab:**
```
1× Chemie-Mixer: 800€
1× Reduktionskessel: 1,500€
1× Kristallisator: 600€
1× Vakuum-Trockner: 500€
────────────────────────
Total Setup: 3,400€

ROI: ~7-10 batches (depending on quality)
```

---

## Mixing Process

### Step 1: Chemie-Mixer

**Purpose:** Combine precursors into meth paste

**Sequence (MUST FOLLOW ORDER):**
1. Add Ephedrin or Pseudoephedrin first
2. Add Roter Phosphor second
3. Add Jod third (triggers mixing start)

**Processing:**
- Time: 600 ticks (30 seconds)
- Capacity: 4 slots (parallel processing)
- Output: 1 Meth-Paste per set of ingredients

**Quality Determination:**
```java
if (usedPseudoephedrin) {
    chance = 35% for GUT quality
} else {
    chance = 25% for GUT quality
}

if (random < chance) {
    outputQuality = GUT
} else {
    outputQuality = STANDARD
}
```

**Example:**
```
Batch 1: Pseudoephedrin + Phosphor + Jod
Batch 2: Ephedrin + Phosphor + Jod
Batch 3: Pseudoephedrin + Phosphor + Jod
Batch 4: Pseudoephedrin + Phosphor + Jod

After 30 seconds:
Batch 1: 35% → GUT, 65% → STANDARD
Batch 2: 25% → GUT, 75% → STANDARD
Batch 3: 35% → GUT, 65% → STANDARD
Batch 4: 35% → GUT, 65% → STANDARD

Expected: ~1-2 GUT, 2-3 STANDARD (with Pseudo)
```

---

## Reduction Process (DANGER)

### Step 2: Reduktionskessel ⚠️

**PURPOSE:** Reduce meth paste to raw meth via controlled heating

**⚠️ THIS IS THE MOST DANGEROUS STAGE! ⚠️**

---

### Temperature System

**Temperature Ranges:**

| Range | Temperature | Status | Effect |
|-------|-------------|--------|--------|
| **COLD** | < 80°C | ZU KALT (Too Cold) | Process paused, no progress |
| **OPTIMAL** | 80-120°C | OPTIMAL | Best quality, safe |
| **DANGER** | 121-150°C | ⚠ GEFAHR (Danger) | Quality degrades, unsafe |
| **CRITICAL** | ≥ 151°C | ☠ KRITISCH (Critical) | **EXPLOSION!** |

**Starting Temperature:** 20°C (room temperature)

---

### Temperature Change Rates

**Heating (when heater ON):**
```java
heatingRate = +1.5°C per tick
processHeat = +0.3°C per tick (during reaction)
totalHeating = +1.8°C per tick when both active

Example: 20°C → 80°C in ~33 ticks (~1.7 seconds)
```

**Cooling (when heater OFF):**
```java
coolingRate = -0.8°C per tick

Example: 150°C → 120°C in ~38 ticks (~1.9 seconds)
```

---

### GUI Interface

**Real-Time Display:**
```
╔═══════════════════════════════════╗
║    REDUKTIONSKESSEL               ║
╠═══════════════════════════════════╣
║                                   ║
║  Temperature: 115°C               ║
║  Status: OPTIMAL ✓                ║
║                                   ║
║  Progress: 75%                    ║
║  Optimal Time: 90%                ║
║  Danger Time: 0%                  ║
║                                   ║
║  [HEATER: ON] [Turn OFF]          ║
║                                   ║
║  Predicted Quality: GUT           ║
╚═══════════════════════════════════╝
```

**Color Coding:**
- Blue (< 80°C): "ZU KALT"
- Green (80-120°C): "OPTIMAL"
- Orange (120-150°C): "⚠ GEFAHR"
- Red (> 150°C): "☠ KRITISCH!"

---

### Quality Calculation (Temperature-Based)

**Timing Metrics:**
```java
optimalTimePercent = time at 80-120°C / total time
dangerTimePercent = time at 121-150°C / total time
```

**Quality Formula:**
```java
if (dangerTimePercent > 0.3) {
    // Too much danger time = DEGRADATION
    quality = inputQuality.downgrade()
}
else if (optimalTimePercent >= 0.9) {
    // 90%+ optimal = 30% chance for BLUE SKY!
    if (random < 0.3) {
        quality = BLUE_SKY
    } else {
        quality = inputQuality.upgrade()
    }
}
else if (optimalTimePercent >= 0.7) {
    // 70-89% optimal = 50% chance upgrade
    if (random < 0.5) {
        quality = inputQuality.upgrade()
    } else {
        quality = inputQuality
    }
}
else {
    // < 70% optimal = no improvement
    quality = inputQuality
}
```

---

### Temperature Control Strategy

**Optimal Strategy (For Blue Sky):**

**Phase 1: Heat-Up (0-5 seconds)**
```
1. Turn heater ON
2. Monitor temperature rise
3. When temp reaches ~115°C, turn heater OFF
4. Let process heat carry it to 120°C
```

**Phase 2: Maintain (5-18 seconds)**
```
5. Monitor temperature carefully
6. If temp < 100°C: Turn heater ON briefly
7. If temp > 130°C: Turn heater OFF immediately
8. Goal: Keep 90%+ time in 80-120°C range
```

**Phase 3: Finish (18-20 seconds)**
```
9. Let temperature stabilize
10. Ensure < 150°C at all times
11. Extract when process completes
```

**Perfect Run Example:**
```
Time 0s: 20°C → Heater ON
Time 5s: 115°C → Heater OFF
Time 6-19s: 110-120°C (fluctuating, optimal)
Time 20s: Complete at 105°C → Extract

Result:
Optimal Time: 95% (19/20 seconds in 80-120°C)
Danger Time: 0%
Quality: 30% BLUE SKY, 70% upgrade to GUT/BLUE SKY
```

---

### Explosion Trigger

**Conditions:**
```java
if (currentTemperature >= 151°C) {
    EXPLODE();
}
```

**Results:**
- Explosion Power: 4 (TNT-equivalent)
- Block Destruction: Reduktionskessel **DESTROYED**
- Player Damage: 10.0 hearts (instant death if unarmored)
- Radius: 8 blocks for player damage
- Loss: All meth paste in kettle LOST

**WARNING SIGNS:**
1. Temperature > 140°C: Urgent action needed
2. Temperature > 145°C: Turn heater OFF NOW
3. Temperature > 148°C: CRITICAL - likely too late
4. Temperature 151°C+: **BOOM**

---

## Crystallization

### Step 3: Kristallisator

**Purpose:** Convert raw meth into crystal form

**Input:** Roh-Meth (raw methamphetamine)
**Output:** Kristall-Meth (wet crystals)
**Duration:** 800 ticks (40 seconds) per batch
**Capacity:** 4 batches simultaneously

**Process:**
1. Add Roh-Meth to Kristallisator
2. Wait 40 seconds (passive)
3. Extract Kristall-Meth

**Quality Mechanic:**
```java
if (random < 0.15 && quality != BLUE_SKY) {
    outputQuality = inputQuality.upgrade()
} else {
    outputQuality = inputQuality
}

15% chance to upgrade:
STANDARD → GUT (15% chance)
GUT → BLUE_SKY (15% chance)
BLUE_SKY → BLUE_SKY (already max)
```

**Example:**
```
Input: 4 GUT Roh-Meth batches
Processing: 800 ticks each (parallel)

Expected Output:
~3.4 GUT Kristall-Meth (85%)
~0.6 BLUE SKY Kristall-Meth (15%)
```

---

## Drying Process

### Step 4: Vakuum-Trockner

**Purpose:** Remove moisture, finalize product

**Input:** Kristall-Meth (wet crystals)
**Output:** Meth (final product)
**Duration:** 600 ticks (30 seconds) per batch
**Capacity:** 6 batches simultaneously

**Process:**
1. Add Kristall-Meth to Vakuum-Trockner
2. Wait 30 seconds (passive vacuum drying)
3. Extract final Meth product

**Quality:** Preserved (no change)

**Example:**
```
Input: 6 batches mixed quality
- 4 GUT Kristall-Meth
- 2 BLUE SKY Kristall-Meth

After 30 seconds:
- 4 GUT Meth
- 2 BLUE SKY Meth

Quality unchanged, just dried
```

---

## Quality System

### Quality Tiers

**3 Quality Levels:**

| Quality | Name | Color | Purity | Price Multiplier | Description |
|---------|------|-------|--------|------------------|-------------|
| **STANDARD** | Weiß (White) | §f | 70-79% | 1.0× | White crystals, standard |
| **GUT** | Gelblich (Yellowish) | §e | 80-89% | 2.0× | Yellowish crystals, good |
| **BLUE_SKY** | Blau (Blue) | §b§l | **96-99%** | **5.0×** | Blue crystals, Heisenberg quality |

---

### Quality Names

**Item Names:**
```
STANDARD: "Crystal Meth"
GUT: "Premium Crystal"
BLUE_SKY: "Blue Sky" ★
```

**Blue Sky Easter Eggs:**
- Tooltip: "Say my name." (Breaking Bad reference)
- Extraction message: "I am the one who knocks."
- Bold cyan color (§b§l)
- Legendary 96-99% purity

---

### Quality Progression

**Best Case Scenario (Pseudoephedrin + Perfect Reduction):**

```
Stage 1 - Mixing:
Pseudoephedrin: 35% → GUT paste
Otherwise: STANDARD paste

Stage 2 - Reduction:
90%+ optimal time: 30% → BLUE SKY
70-89% optimal: 50% → upgrade
< 70%: No improvement
Danger time > 30%: DOWNGRADE

Stage 3 - Crystallization:
15% → upgrade

Stage 4 - Drying:
Quality preserved
```

**Probability to Blue Sky (from GUT paste):**

```
Path 1: Reduction upgrade (30% if 90%+ optimal)
Path 2: Reduction → GUT, Crystallization → BLUE SKY (15%)
Path 3: Both stages maintain GUT (no Blue Sky)

With perfect temperature control (90%+ optimal):
~40-45% final Blue Sky rate
```

---

### Achieving Blue Sky

**Method 1: Perfect Temperature Control**
```
1. Use Pseudoephedrin (35% GUT paste)
2. Perfect reduction (90%+ optimal time)
   → 30% BLUE SKY + 70% upgrade
3. Result: ~40% BLUE SKY final

Requirements:
✓ Constant monitoring
✓ Quick reactions
✓ Keep temp 80-120°C for 18+ seconds
✓ Never exceed 150°C
```

**Method 2: Lucky Crystallization**
```
1. Get GUT Roh-Meth
2. Crystallization: 15% → BLUE SKY
3. Result: Lower rate but easier

Less skill, more RNG
```

**Method 3: Combined (Best Chance)**
```
1. Pseudoephedrin → 35% GUT paste
2. Good reduction (70-89% optimal) → 50% upgrade to GUT Roh-Meth
3. Crystallization: 15% → BLUE SKY
4. Result: Multiple chances at Blue Sky

Overall: ~50-60% final Blue Sky rate with skill
```

---

## Explosion Mechanics

### Explosion Triggers

**Primary Trigger:**
```java
if (currentTemperature >= 151°C) {
    world.explode(pos, 4.0F, Explosion.Mode.BLOCK);
}
```

**No Other Triggers:**
- Temperature is ONLY explosion risk
- No random explosions
- No time-based explosions
- Fully player-controlled

---

### Explosion Effects

**Block Destruction:**
```
Explosion Power: 4 (equivalent to TNT)
Explosion Type: Mode.BLOCK (destroys blocks)
Center: Reduktionskessel position
Radius: ~4 blocks

Destroyed:
- Reduktionskessel (always)
- Nearby weak blocks (glass, etc.)
- Possible damage to adjacent lab equipment
```

**Player Damage:**
```
Damage: 10.0 hearts
Radius: 8 blocks from center
Affected: All players within radius

Survival:
✓ Full diamond armor: Survives
✓ Iron armor: Survives (barely)
✗ No armor: Instant death
```

**Material Loss:**
```
Lost:
- Reduktionskessel block (1,500€)
- All meth paste in kettle
- Nearby items/blocks
- Current batch progress

Not Lost:
- Other lab equipment (if far enough)
- Precursor stockpiles (if protected)
```

---

### Preventing Explosions

**Safety Checklist:**

**1. Never AFK**
```
✗ Do NOT leave reduction running
✗ Do NOT multitask during reduction
✓ Stay at GUI for full 20 seconds
```

**2. Monitor Temperature**
```
✓ Watch temperature display constantly
✓ Know the zones (80-120 = safe, 121-150 = danger)
✓ React immediately if > 140°C
```

**3. Heater Control**
```
✓ Turn heater OFF at ~115°C
✓ Use process heat (+0.3/tick) to reach 120°C
✓ Only pulse heater ON if temp < 100°C
✓ NEVER leave heater ON continuously
```

**4. Emergency Protocol**
```
If temp > 145°C:
1. Turn heater OFF immediately
2. Wait for cooling (-0.8°C/tick)
3. Pray it drops below 151°C in time
4. Be ready to take explosion if > 148°C
```

---

### Explosion Recovery

**After Explosion:**
```
1. Assess damage:
   - Reduktionskessel destroyed (rebuild: 1,500€)
   - Check other equipment (may survive)
   - Heal player (potions, food)

2. Rebuild:
   - Craft new Reduktionskessel
   - Repair any damaged equipment
   - Restock precursors

3. Learn:
   - Review what went wrong
   - Practice temperature control
   - Consider smaller batches until skilled
```

---

## Profitability Analysis

### Standard Production (Ephedrin, STANDARD Quality)

**Input Costs:**
```
Ephedrin: 100€
Roter Phosphor: 50€
Jod: 50€
Time: 2 minutes

Total Cost: 200€
```

**Output (STANDARD):**
```
Price Multiplier: 1.0×
Base Price: ~500€ per gram
Final Price: 500€

Profit: 300€
Hourly Rate: 9,000€/hour
```

---

### Good Production (Pseudoephedrin, GUT Quality)

**Input Costs:**
```
Pseudoephedrin: 150€
Roter Phosphor: 50€
Jod: 50€
Time: 2 minutes

Total Cost: 250€
```

**Output (GUT, 35% from Pseudo + good reduction):**
```
Price Multiplier: 2.0×
Base Price: ~500€
Final Price: 1,000€

Profit: 750€
Hourly Rate: 22,500€/hour
```

---

### Blue Sky Production (Pseudoephedrin, Perfect Control)

**Input Costs:**
```
Pseudoephedrin: 150€
Roter Phosphor: 50€
Jod: 50€
Time: 2 minutes

Total Cost: 250€
```

**Output (BLUE SKY, ~40% with perfect temp control):**
```
Price Multiplier: 5.0×
Base Price: ~500€
Final Price: 2,500€

Profit: 2,250€
Hourly Rate: 67,500€/hour
```

---

### Scale Production (4-batch Mixer, Perfect Blue Sky)

**Per Cycle (4 batches):**
```
Input:
- 4 Pseudoephedrin: 600€
- 4 Roter Phosphor: 200€
- 4 Jod: 200€
Time: 2 minutes (parallel mixer, sequential reduction)

Total Cost: 1,000€

Output (40% Blue Sky rate):
- 1-2 BLUE SKY @ 2,500€ = 3,750€ avg
- 2-3 GUT @ 1,000€ = 2,500€ avg

Total Revenue: 6,250€
Profit: 5,250€

Hourly Rate: 157,500€/hour
Daily (8 hours): 1,260,000€
Monthly: 37,800,000€
```

**ROI:** 3,400€ setup → Break-even in 1 cycle!

---

### Comparison to Other Systems

**Per Hour Profit:**
```
Mushroom: ~3,000€/hour
Tobacco: ~2,800€/hour
Cannabis (trimmed): ~2,400€/hour
Coca (cocaine): ~9,200€/hour
Poppy (heroin, basic): ~31,500€/hour
Poppy (LEGENDAER Afghan): ~170,000€/hour

Meth (BLUE SKY): ~67,500€/hour (single batch)
Meth (scaled 4-batch): ~157,500€/hour

Winner: Poppy > Meth > Coca > Others
```

**Per Minute Profit:**
```
Meth: 1,125€/minute (fastest cycle)
Poppy: 1,078€/minute
Coca: 820€/minute

Meth has HIGHEST per-minute profit!
```

---

## Best Practices

### For Beginners

**IMPORTANT: This is NOT a beginner system!**

**If You Insist:**
1. **Practice on creative mode first**
2. Use **Ephedrin** (cheaper mistakes)
3. **Build far from base** (explosion protection)
4. Start with **1-batch runs**
5. Focus on **not exploding** (forget Blue Sky)
6. Target **STANDARD quality** (low temp, safe)

**First Goal:**
- Complete 5 runs without explosion
- Achieve STANDARD quality consistently
- Break even (300€ profit)

---

### For Intermediate

**Requirements:**
- Completed 20+ runs
- Explosion rate < 10%
- Understand temperature zones

**Strategy:**
1. Upgrade to **Pseudoephedrin**
2. Aim for **70-89% optimal time**
3. Target **GUT quality** (2.0× price)
4. Accept occasional explosions
5. Build **explosion-proof lab** (obsidian walls)

**Target:**
- 70% GUT quality rate
- < 5% explosion rate
- 20,000€+/hour profit

---

### For Advanced

**Requirements:**
- Completed 50+ runs
- Explosion rate < 2%
- Consistent 80%+ optimal time

**Strategy:**
1. **Only Pseudoephedrin**
2. **Perfect temperature control** (90%+ optimal)
3. Target **40%+ Blue Sky rate**
4. **4-batch production** (parallel mixing)
5. **Automated precursor supply**

**Target:**
- 40-50% BLUE SKY quality
- < 1% explosion rate
- 150,000€+/hour profit
- "Heisenberg" reputation

---

### Temperature Mastery

**Practice Routine:**

**Level 1: Safety (Runs 1-10)**
```
Goal: No explosions
Strategy: Keep temp < 130°C at all times
Method: Brief heater pulses, long cooling
Result: Low optimal time (< 50%), STANDARD quality
```

**Level 2: Optimal (Runs 11-30)**
```
Goal: 70%+ optimal time
Strategy: Maintain 100-120°C range
Method: Heater ON to 115°C, OFF to cool, repeat
Result: 50% upgrade chance, occasional GUT
```

**Level 3: Perfect (Runs 31-50)**
```
Goal: 90%+ optimal time
Strategy: Precise 110-120°C maintenance
Method: Minimal heater pulses, process heat utilization
Result: 30% Blue Sky + 70% upgrade = ~40% Blue Sky
```

**Level 4: Heisenberg (Runs 51+)**
```
Goal: 95%+ optimal time consistently
Strategy: Perfect temperature reading and anticipation
Method: Muscle memory, instant reactions
Result: 50%+ Blue Sky rate, master chemist
```

---

### Lab Safety

**Lab Design:**
```
1. Explosion-Proof Room:
   - Obsidian walls (blast resistant)
   - 5×5×3 minimum size
   - Single door (iron door)

2. Equipment Layout:
   - Reduktionskessel in center (splash zone)
   - Other equipment on walls (safe distance)
   - Chest storage OUTSIDE room

3. Emergency Exit:
   - Always have escape route
   - Keep door accessible
   - Don't block with equipment

4. Fire Suppression:
   - Water buckets nearby
   - Fire resistance potions
   - Backup armor in chest
```

---

## Troubleshooting

### "Temperature Rising Too Fast"

**Cause:**
- Heater left ON too long
- Process heat (+0.3/tick) adding up

**Solutions:**
```
✓ Turn heater OFF at 115°C (not 120°C)
✓ Let process heat carry to 120°C
✓ Monitor every second during heat-up
✓ React immediately if > 125°C
```

---

### "Can't Maintain Optimal Range"

**Cause:**
- Poor heater timing
- Overcompensating (too much ON/OFF)

**Solutions:**
```
✓ Use short heater pulses (1-2 seconds)
✓ Let temperature stabilize before adjusting
✓ Aim for 110-115°C (buffer zone)
✓ Practice on cheap batches first
```

---

### "Explosion Despite Monitoring"

**Cause:**
- Temperature spike (heater ON when near 140°C)
- Server lag (delayed heater OFF)
- Distraction (missed critical moment)

**Solutions:**
```
✓ Always turn heater OFF before 140°C
✓ Check server TPS (/tps) before starting
✓ Eliminate distractions (close other GUIs)
✓ Have emergency exit strategy
```

---

### "Never Getting Blue Sky"

**Cause:**
- Low optimal time (< 70%)
- Using Ephedrin (lower base quality)
- Bad RNG

**Solutions:**
```
✓ Use Pseudoephedrin (35% vs 25% GUT base)
✓ Achieve 90%+ optimal time (temp control)
✓ Run more batches (probability averages out)
✓ Check "Optimal Time" stat in GUI
```

---

### "Quality Downgrading in Reduction"

**Cause:**
- Danger time > 30% (too long at 121-150°C)
- Excessive heat

**Solutions:**
```
✓ Keep temperature < 120°C (optimal range)
✓ Never exceed 130°C for extended periods
✓ Turn heater OFF if approaching danger zone
✓ Monitor "Danger Time" stat in GUI
```

---

<div align="center">

**Methamphetamine Production System - Master Guide**

**⚠️ "Say my name." ⚠️**

For related systems:
- [🌿 Cannabis System](Cannabis-System.md)
- [💊 Coca System](Coca-System.md)
- [💉 Poppy System](Poppy-System.md)

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production](../Production-Systems.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

**"I am the one who knocks."**

</div>
