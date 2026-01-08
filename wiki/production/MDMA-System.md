# MDMA/Ecstasy Production System

<div align="center">

**3-Stage Synthesis with Timing Minigame - 64 Pill Designs**

Arcade-style pill pressing with premium quality rewards

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production Systems](../Production-Systems.md)

</div>

---

## 📋 Quick Reference

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐⭐⭐ (3/5 - Moderate) |
| **Steps** | 3 (Synthesize → Dry → Press) |
| **Precursor** | Safrol |
| **Duration** | ~83 seconds (full cycle) |
| **Profitability** | ⭐⭐⭐⭐ (4/5 - Very High) |
| **Quality Tiers** | Schlecht, Standard, Gut, Premium |
| **Unique Mechanic** | Timing minigame determines quality |
| **Customization** | 8 colors × 8 designs = 64 combinations |
| **Final Product** | Ecstasy Pills (customizable) |

---

## Overview

MDMA production is an arcade-style system featuring a 3-stage chemical synthesis culminating in an interactive timing-based pill pressing minigame. Players can customize pills with 8 colors and 8 iconic designs while quality is determined entirely by pressing skill.

### Production Flow

```
1. SYNTHESIZE → Safrol → MDMA-Base (50s)
   Quality Chance: 40% GUT if 6+ Safrol
2. DRY → MDMA-Base → MDMA-Kristalle (30s)
   Quality Preserved
3. PRESS → Kristalle + Bindemittel → Pills (3s minigame)
   Quality Based on Timing Skill
```

---

## Quality System

### 4 Quality Tiers

**Schlecht (Poor) - Contaminated**
- Color: §7 (Gray)
- Price Multiplier: 0.5×
- Base Price: 15€
- Timing Score: < 0.5

**Standard (Normal)**
- Color: §f (White)
- Price Multiplier: 1.0×
- Base Price: 30€
- Timing Score: 0.5-0.79

**Gut (Good) - High Purity**
- Color: §e (Yellow)
- Price Multiplier: 2.0×
- Base Price: 60€
- Timing Score: 0.8-0.94

**Premium (Laboratory Quality)**
- Color: §d§l (Bold Magenta)
- Price Multiplier: 4.0×
- Base Price: 120€
- Timing Score: ≥ 0.95

---

## Timing Minigame

### Press Mechanics

**Cycle Duration:** 60 ticks (3 seconds)

**Timing Zones:**

```
[0───20]──[25───35]──[40───60]
  EARLY    PERFECT    LATE

Tick 0-20:   TOO EARLY (Red zone)
Tick 20-40:  GOOD WINDOW (Yellow zone)
Tick 25-35:  PERFECT ZONE (Green zone)
Tick 30:     EXACT CENTER (Best score)
Tick 40-60:  TOO LATE (Red zone)
```

---

### Scoring System

**Perfect Zone (Ticks 25-35):**
```java
perfectCenter = 30
distanceFromPerfect = abs(tick - 30)
score = 1.0 - (distanceFromPerfect / 10.0) × 0.1

Examples:
Tick 30: score = 1.0 (PERFECT)
Tick 28: score = 0.98 (Excellent)
Tick 25: score = 0.95 (PREMIUM threshold)
```

**Good Zone (Ticks 20-40, outside perfect):**
```java
score = 0.6 + (0.3 × (1.0 - abs(tick - 30) / 15.0))

Examples:
Tick 22: score = 0.76 (GUT)
Tick 38: score = 0.74 (GUT)
```

**Early Press (< 20):**
```java
score = 0.2 + (tick / 20.0) × 0.3

Example:
Tick 15: score = 0.425 (SCHLECHT)
```

**Late Press (> 40):**
```java
score = max(0.1, 0.5 - ((tick - 40) / 20.0) × 0.4)

Example:
Tick 50: score = 0.3 (SCHLECHT)
```

---

### Quality from Score

```java
if (score >= 0.95) → PREMIUM
if (score >= 0.8)  → GUT
if (score >= 0.5)  → STANDARD
Otherwise          → SCHLECHT
```

**Final Quality:**
```java
baseQuality = inputKristall.quality
timingQuality = qualityFromScore(timingScore)
finalQuality = max(baseQuality, timingQuality)
```

This means perfect timing can upgrade quality!

---

## Customization

### 8 Pill Colors

| Color | Display | Color Code | Hex Value |
|-------|---------|------------|-----------|
| **Pink** | §d | Magenta | 0xFFAACC |
| **Blue** | §9 | Dark Blue | 0x5555FF |
| **Green** | §a | Green | 0x55FF55 |
| **Orange** | §6 | Gold | 0xFFAA00 |
| **Yellow** | §e | Yellow | 0xFFFF55 |
| **White** | §f | White | 0xFFFFFF |
| **Red** | §c | Red | 0xFF5555 |
| **Purple** | §5 | Dark Purple | 0xAA55AA |

---

### 8 Pill Designs/Stamps

| Design | Symbol | Color | Description |
|--------|--------|-------|-------------|
| **TESLA** | T | §c (Red) | Tesla logo - premium brand |
| **SUPERMAN** | S | §9 (Blue) | Superhero symbol |
| **TOTENKOPF** | ☠ | §8 (Dark Gray) | Skull - hardcore |
| **HERZ** | ♥ | §d (Pink) | Heart - love pills |
| **SCHMETTERLING** | 🦋 | §e (Yellow) | Butterfly - euphoria |
| **STERN** | ★ | §6 (Gold) | Star - classic |
| **PEACE** | ☮ | §a (Green) | Peace symbol - rave culture |
| **DIAMANT** | ◆ | §b (Aqua) | Diamond - premium |

**Total Combinations:** 8 colors × 8 designs = **64 unique pills!**

---

## Equipment & Processing

### Stage 1: Reaktions-Kessel (Reaction Kettle)

**Block Properties:**
- Light Level: 4 (glowing)
- Strength: 3.5F
- Sound: Metal

**Processing:**
- Capacity: 8 Safrol
- Time: 1,000 ticks (50 seconds)
- Output: 8 MDMA-Base (1:1 ratio)

**Quality Determination:**
```java
if (safrolCount >= 6) {
    chance = 40% for GUT quality
} else if (safrolCount >= 4) {
    chance = 25% for GUT quality
} else {
    chance = 10% for GUT quality
}
```

**Strategy:** Always use 6+ Safrol for 40% GUT chance!

---

### Stage 2: Trocknungs-Ofen (Drying Oven)

**Block Properties:**
- Light Level: 8 (hot, glowing)
- Temperature: 120°C
- Strength: 3.5F

**Processing:**
- Capacity: 8 MDMA-Base
- Time: 600 ticks (30 seconds)
- Output: 8 MDMA-Kristalle (1:1 ratio)
- Quality: Preserved from input

---

### Stage 3: Pillen-Presse (Pill Press) 🎮

**Block Properties:**
- Has GUI Interface
- Interactive Minigame
- Strength: 4.0F

**Processing:**
- Capacity: 16 Kristall + 16 Bindemittel
- Time: ~3 seconds (player-dependent)
- Output: Equal to min(kristall, bindemittel)
- Quality: Timing-based

**GUI Features:**
- Visual timing bar with colored zones
- Moving indicator with pulse animation
- Press button or spacebar
- Real-time score feedback
- Color/design selection

---

## Production Examples

### Example 1: Standard Production

**Input:**
```
8 Safrol (400€)
8 Bindemittel (80€)
Total: 480€
```

**Processing:**
```
Reaktions-Kessel (50s):
8 Safrol → 8 MDMA-Base
Quality: 40% GUT, 60% STANDARD

Trocknungs-Ofen (30s):
8 MDMA-Base → 8 MDMA-Kristalle
Quality: Preserved

Pillen-Presse (3s each = 24s):
8 Kristalle + 8 Bindemittel → 8 Pills
Quality: Depends on timing!
```

**Output (GUT Timing):**
```
8 GUT Pills × 60€ = 480€
Profit: 0€ (break-even)
Time: 104 seconds
```

**Output (PREMIUM Timing):**
```
8 PREMIUM Pills × 120€ = 960€
Profit: 480€
Hourly Rate: 16,615€/hour
```

---

### Example 2: Skilled Production

**Input:**
```
16 Safrol (800€)
16 Bindemittel (160€)
Total: 960€
```

**Processing (2 batches):**
```
Batch 1: 8 Safrol → 8 Base (50s)
Batch 2: 8 Safrol → 8 Base (50s)
Drying: 16 Base → 16 Kristalle (30s × 2)
Pressing: 16 pills (3s each)

Total Time: ~168 seconds
```

**Output (Consistent PREMIUM):**
```
Expected from 16 Safrol:
~6 GUT Base + ~10 STANDARD Base

With perfect timing:
All 16 → PREMIUM pills

16 PREMIUM Pills × 120€ = 1,920€
Profit: 960€
Hourly Rate: 20,571€/hour
```

---

## Profitability Analysis

### Quality Impact

**Per Pill Pricing:**
```
SCHLECHT: 30 × 0.5 = 15€
STANDARD: 30 × 1.0 = 30€
GUT: 30 × 2.0 = 60€
PREMIUM: 30 × 4.0 = 120€
```

**8-Pill Batch Comparison:**
```
All SCHLECHT: 8 × 15€ = 120€ (LOSS)
All STANDARD: 8 × 30€ = 240€ (LOSS)
All GUT: 8 × 60€ = 480€ (break-even)
All PREMIUM: 8 × 120€ = 960€ (+480€ profit)
```

**Skill = Profit!**

---

### Scaling Production

**16-Pill Operation (Skilled Player):**
```
Input: 960€
Output (90% PREMIUM rate):
- 14 PREMIUM @ 120€ = 1,680€
- 2 GUT @ 60€ = 120€
Total: 1,800€
Profit: 840€

Hourly Rate: 18,000€/hour
```

**32-Pill Operation (Expert):**
```
Input: 1,920€
Output (95% PREMIUM rate):
- 30 PREMIUM @ 120€ = 3,600€
- 2 GUT @ 60€ = 120€
Total: 3,720€
Profit: 1,800€

Hourly Rate: 19,355€/hour
```

---

## Best Practices

### Timing Mastery

**Beginner (Attempts 0-20):**
- Goal: Hit GOOD zone (ticks 20-40)
- Accept GUT quality (2.0× multiplier)
- Success Rate: 50-70%
- Focus: Understanding rhythm

**Intermediate (Attempts 20-50):**
- Goal: Target PERFECT zone (ticks 25-35)
- Achieve PREMIUM 60-80% of time
- Success Rate: 70-85%
- Focus: Precision timing

**Expert (Attempts 50+):**
- Goal: Consistent tick 28-32 presses
- Achieve PREMIUM 90%+ of time
- Success Rate: 90-95%
- Focus: Muscle memory

---

### Training Routine

**Phase 1: Learning (First 10 pills)**
```
- Watch full indicator cycle without pressing
- Identify visual center (tick 30)
- Practice on STANDARD Kristall (cheap mistakes)
- Target any hit in green zone
```

**Phase 2: Consistency (Pills 10-30)**
```
- Aim for tick 27-33 (close to center)
- Count rhythm: "One... two... three... PRESS!"
- Eliminate distractions
- Track success rate
```

**Phase 3: Mastery (Pills 30+)**
```
- Perfect tick 29-31 (exact center zone)
- Develop muscle memory
- 90%+ PREMIUM rate
- Switch to GUT input Kristall
```

---

### Production Strategy

**Quality Input = Quality Output:**
```
Strategy 1: Volume (STANDARD Kristall)
- Rely 100% on timing skill
- Need 95%+ PREMIUM timing
- High risk, high reward

Strategy 2: Quality (GUT Kristall)
- Start with GUT base
- Perfect timing → guaranteed PREMIUM
- Safer, more consistent
```

**Recommended:**
- Use 6+ Safrol per batch (40% GUT)
- Practice timing until 80%+ success
- Combine GUT input + perfect timing
- Result: ~95% PREMIUM pills

---

### Customization for Profit

**Popular Designs (Premium Pricing):**
```
TESLA (Red): Iconic rave brand (+10-20% value)
SUPERMAN (Blue): Recognizable symbol (+10% value)
DIAMANT (Aqua): Luxury appearance (+5% value)
```

**Market Strategy:**
```
- Produce limited runs of specific designs
- Create "signature" color/design combo
- Match colors to quality:
  - PREMIUM → Purple or Magenta
  - GUT → Yellow or Green
  - STANDARD → White
```

---

### Equipment Setup

**Essential (Minimum):**
```
1× Reaktions-Kessel: 1,000€
1× Trocknungs-Ofen: 800€
1× Pillen-Presse: 1,200€
Total: 3,000€

ROI: ~3-4 batches (PREMIUM quality)
```

**Professional:**
```
2× Reaktions-Kessel (parallel synthesis)
2× Trocknungs-Ofen (parallel drying)
2× Pillen-Presse (dual pressing)
Safrol stockpile: 100+
Bindemittel stockpile: 100+

Total Investment: ~10,000€
ROI: 5-6 batches
Production: 2× throughput
```

---

## Troubleshooting

### "Always Getting SCHLECHT Quality"

**Causes:**
- Pressing too early (< tick 20)
- Pressing too late (> tick 40)
- Not watching indicator

**Solutions:**
```
✓ Focus on indicator movement
✓ Count rhythm (practice offline)
✓ Aim for green zone (ticks 25-35)
✓ Use audio cues if available
✓ Eliminate distractions
```

---

### "Can't Hit PREMIUM Consistently"

**Causes:**
- Timing slightly off-center
- Inconsistent reaction time
- Not enough practice

**Solutions:**
```
✓ Aim for exact tick 30
✓ Practice 20-30 pills minimum
✓ Use tick 29-31 margin of error
✓ Develop muscle memory
✓ Track improvement over time
```

---

### "Low Profit Despite Good Timing"

**Causes:**
- Using STANDARD input Kristall
- Not enough Safrol (< 6)
- Market saturation

**Solutions:**
```
✓ Use 6+ Safrol per batch (40% GUT)
✓ Upgrade to GUT Kristall input
✓ Sell to NPCs for guaranteed prices
✓ Create limited edition designs
```

---

<div align="center">

**MDMA/Ecstasy Production System - Master Guide**

For related systems:
- [🌿 Cannabis System](Cannabis-System.md)
- [💊 LSD System](LSD-System.md)
- [💰 Economy & Sales](../features/Economy-System.md)

[🏠 Back to Wiki Home](../Home.md) • [🌿 All Production](../Production-Systems.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
