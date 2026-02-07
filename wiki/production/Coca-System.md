# Coca Production System

An industrial-scale chemical processing chain with 2 strains, diesel-powered extraction, glowing refineries, and optional crack cooking with baking soda.

**Stats at a Glance:**

| Attribute | Value |
|-----------|-------|
| Strains | 2 (Bolivianisch, Kolumbianisch) |
| Production Steps | 5 |
| Total Items | 9 |
| Total Blocks | 9 (including light-emitting refineries) |
| Quality Tiers | Poor, Good, Very Good, Legendary |

---

## Overview

The Coca system is a fuel-driven industrial pipeline. Plants grow fast, but the real complexity lies in the extraction and refining stages. Extraction vats require diesel, refineries consume fuel and emit light when operating, and the optional crack cooking step introduces a skill-based element. The system features 3 sizes of both vats and refineries, with refineries that glow at different light levels.

---

## Strains

| Strain | Growth Speed | Seed Price | Water Use | Base Price | Best For |
|--------|-------------|------------|-----------|------------|----------|
| **Bolivianisch** (Bolivian) | Fast (100 ticks) | 20 | 0.8x (low) | Lower | Volume production |
| **Kolumbianisch** (Colombian) | Slower (140 ticks) | 35 | 1.0x | Higher (+75%) | Premium production |

Both strains yield 6 leaves at harvest and progress through 8 growth stages (0-7). Colombian commands significantly higher base prices but takes 40% longer to grow.

---

## Production Chain

```
  [1. PLANTING]        [2. HARVESTING]
   Seeds + Pot    -->   Fresh Coca Leaves
   + Soil + Water       (Stage 7)
       |                     |
       v                     v
  Planted Pot  -->     6 Coca Leaves
                             |
                             v
  [3. EXTRACTION]      Extraction Vat
   Requires DIESEL     (3 sizes: S/M/B)
   100 mB per leaf     Time: 5 min
       |
       v
   Coca Paste
       |
       v
  [4. REFINING]        Refinery
   Requires FUEL       (3 sizes: S/M/B)
   GLOWING BLOCKS!     Time: 6.67 min
   20% quality upgrade chance
       |
       v
   Cocaine
       |  (optional)
       v
  [5. CRACK COOKING]   Crack Kocher
   + Baking Soda       Time: 4 sec
   (Backpulver)        Timing minigame
       |
       v
   Crack Rock
```

---

## Step 1-2: Planting and Harvesting

Plant coca seeds in a pot with soil and water. Growth is very fast (5-7 seconds real time). Harvest at stage 7 for 6 leaves. Early harvest (stages 4-6) gives only 3 leaves at Poor quality.

Standard growth modifiers apply: Golden Pot (+1 quality), Fertilizer (+67% yield / -1 quality), Growth Booster (halves time / -1 quality), Quality Booster (+1 quality).

---

## Step 3: Extraction (Extraction Vats)

Diesel-powered extraction converts fresh leaves into coca paste.

### Extraction Vats (3 Sizes)

| Size | Capacity | Time | Max Diesel | Diesel per Leaf |
|------|----------|------|------------|-----------------|
| **Small Extraction Vat** | 6 leaves | 5 min | 1,000 mB | 100 mB |
| **Medium Extraction Vat** | 12 leaves | 5 min | 2,000 mB | 100 mB |
| **Big Extraction Vat** | 24 leaves | 5 min | 4,000 mB | 100 mB |

All slots process in parallel. If diesel runs out, processing pauses but progress is preserved. Quality is carried over from the input leaves. Output is 1:1 (one paste per leaf).

### Diesel Supply

Diesel is loaded via the **Diesel Canister** (1,000 mB capacity, refillable). Purchase diesel from NPC shops or craft from petroleum.

---

## Step 4: Refining (Refineries)

Fuel-based refining purifies coca paste into cocaine. Refineries emit light when operating.

### Refineries (3 Sizes)

| Size | Capacity | Time | Max Fuel | Light Level |
|------|----------|------|----------|-------------|
| **Small Refinery** | 6 paste | 6.67 min | 500 units | 8 |
| **Medium Refinery** | 12 paste | 6.67 min | 1,000 units | 10 |
| **Big Refinery** | 24 paste | 6.67 min | 2,000 units | **12** |

Fuel consumption: 1 unit per 20 ticks = 400 fuel per paste. Acceptable fuels include coal, charcoal, coal blocks, and lava buckets.

### Quality Upgrade

Each piece of paste refined has a **20% chance** to upgrade one quality tier:

```
Good paste  -->  20% chance  -->  Very Good cocaine
             -->  80% chance  -->  Good cocaine
```

---

## Step 5: Crack Cooking (Optional)

Convert cocaine into crack rocks using the Crack Kocher and Backpulver (baking soda).

### Crack Cooking Mechanics

| Property | Value |
|----------|-------|
| Input | 1-10g Cocaine + 1 Backpulver |
| Cook Cycle | 80 ticks (4 seconds) |
| Weight Conversion | 80% (10g cocaine = 8g crack) |
| Quality | Determined by timing minigame |

### Timing Zones

```
  [0----27]---[28------52]---[53----80]
    TOO          GOOD /        TOO
    EARLY        PERFECT       LATE

  Ticks 0-27:   Too Early    -->  Schlecht (Poor)
  Ticks 28-52:  Good Window  -->  Gut / Standard
  Ticks 35-45:  PERFECT      -->  Fishscale (Premium)
  Ticks 53-80:  Too Late     -->  Schlecht (Burnt)
```

### Crack Quality Tiers

| Quality | Score | Price Multiplier |
|---------|-------|-----------------|
| **Schlecht** | < 0.5 | 0.6x |
| **Standard** | 0.5-0.79 | 1.0x |
| **Gut** | 0.8-0.94 | 1.5x |
| **Fishscale** | >= 0.95 | 2.5x |

Legendary-quality cocaine input provides a bonus: if the cook result is below Fishscale, it gets upgraded one tier automatically.

---

## Items Table (9 Items)

| Item | Description | How Obtained |
|------|-------------|-------------|
| Bolivianisch Coca Seeds | Bolivian strain seeds | Purchase / loot |
| Kolumbianisch Coca Seeds | Colombian strain seeds | Purchase / loot |
| Fresh Coca Leaves | Raw harvested leaves (strain-specific) | Harvesting mature plant |
| Coca Paste | Extracted alkaloid paste | Extraction Vat (requires diesel) |
| Cocaine | Refined white powder | Refinery (requires fuel) |
| Crack Rock | Cooked cocaine product | Crack Kocher (requires Backpulver) |
| Backpulver (Baking Soda) | Required for crack cooking | Purchase / crafting |
| Diesel Canister | Fuel container for extraction (1,000 mB) | Purchase / crafting |
| Fuel (Coal/Charcoal) | Powers refineries | Mining / purchase |

---

## Blocks Table (9 Blocks)

| Block | Description | Function |
|-------|-------------|----------|
| Terracotta Pot | Basic planting pot | Standard growth container |
| Ceramic Pot | Improved pot | Better water retention |
| Iron Pot | Industrial pot | Durable |
| Golden Pot | Premium pot | +1 quality tier on harvest |
| Small Extraction Vat | Compact diesel-powered extractor | Processes 6 leaves in 5 min |
| Medium Extraction Vat | Standard diesel-powered extractor | Processes 12 leaves in 5 min |
| Big Extraction Vat | Industrial diesel-powered extractor | Processes 24 leaves in 5 min |
| Small Refinery | Compact fuel-based refinery (light level 8) | Refines 6 paste, 20% quality upgrade |
| Medium Refinery | Standard fuel-based refinery (light level 10) | Refines 12 paste, 20% quality upgrade |
| Big Refinery | Industrial fuel-based refinery (light level **12**) | Refines 24 paste, 20% quality upgrade, brightest glow |
| Crack Kocher | Crack cooking station (light level 6 when active) | Timing minigame for crack production |

---

## Quality Modifiers

| Modifier | Stage | Effect |
|----------|-------|--------|
| Golden Pot | Planting | +1 quality tier on harvest |
| Quality Booster | Growing | +1 quality tier |
| Fertilizer | Growing | +67% yield but -1 quality |
| Refining RNG | Refining | 20% chance for +1 quality tier |
| Crack Timing | Crack Cooking | Skill-based quality (Schlecht to Fishscale) |
| Legendary Input | Crack Cooking | Auto-upgrades crack quality if below Fishscale |

---

## Tips & Tricks

1. **Colombian + Golden Pot** is the premium path. The 75% higher base price combined with Very Good/Legendary quality yields massive profits.
2. **Bolivian is the speed-runner's choice.** Cheapest seeds, fastest growth, and still viable at scale.
3. **Stock diesel in bulk.** Running out mid-extraction pauses progress. Keep 5,000+ mB on hand.
4. **Big Refineries glow at light level 12.** Use this to your advantage for base lighting, or hide them underground if you want secrecy.
5. **Crack is only profitable at Fishscale quality.** If you cannot consistently hit the perfect timing window (ticks 35-45), sell cocaine directly instead.
6. **Refining luck averages out over large batches.** Run 24+ paste at a time to get a reliable 20% upgrade distribution.
7. **Use coal blocks for fuel efficiency.** One coal block provides 16x the burn time of a single coal piece.
8. **Do not mix strains in the same extraction vat.** Each leaf processes independently, but tracking quality is easier when batches are uniform.
9. **Build refineries away from flammable materials.** While they do not cause fire, the light emission can affect nearby light-sensitive crops.
10. **Practice crack timing with cheap Schlecht-quality cocaine** before risking your premium stock.

---

*See also: [Poppy System](Poppy-System.md) | [Meth System](Meth-System.md) | [Production Systems Overview](../Production-Systems.md)*
