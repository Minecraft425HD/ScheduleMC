# Cannabis Production System

An advanced 8-step production chain with 4 strains, a skill-based trimming minigame, and multiple end products including buds, hash, and oil.

**Stats at a Glance:**

| Attribute | Value |
|-----------|-------|
| Strains | 4 (Indica, Sativa, Hybrid, Autoflower) |
| Production Steps | 8 |
| Total Items | 10 |
| Total Blocks | 9 |
| Quality Tiers | Schwag, Mids, Dank, Top Shelf, Exotic |

---

## Overview

Cannabis production is one of the most sophisticated systems in ScheduleMC. It features 8 growth stages, a hands-on trimming minigame that directly determines quality, long-term curing mechanics, and branching product paths into hash and oil. The 5-tier quality system rewards skilled players who master the trimming station.

---

## Strains

| Strain | Effect | Growth Speed | THC | Notes |
|--------|--------|-------------|-----|-------|
| **Indica** | Relaxing, sedating | Fast (120 ticks) | 22% | Classic strain, good for beginners |
| **Sativa** | Energizing, creative | Slow (160 ticks) | 18% | Premium pricing due to slow growth |
| **Hybrid** | Balanced, versatile | Medium (140 ticks) | 20% | Best overall; highest CBD (2.0%) |
| **Autoflower** | Mild, beginner-friendly | Very Fast (70 ticks) | 15% | Half the growth time of Sativa; highest volume |

---

## Production Chain

```
  [1. PLANTING]      [2. GROWING]       [3. HARVESTING]
   Seeds + Pot   -->  8 Growth     -->   Fresh Buds
   + Soil + Water     Stages
       |                  |                   |
       v                  v                   v
  Planted Pot  -->   Mature Plant  -->  Fresh Cannabis Buds
                                              |
                                              v
                     [4. DRYING]         Trocknungsnetz
                      (Drying Net)       Time-based
                                              |
                                              v
                     [5. TRIMMING]       Trimm Station
                      MINIGAME!          Skill = Quality
                          |
            +-------------+-------------+
            |                           |
            v                           v
     Trimmed Buds                 Cannabis Trim
            |                           |
            v                     +-----+-----+
     [6. CURING]                  |           |
      Curing Glas                 v           v
      (14-28 days)          [7. HASH]    [8. OIL]
            |                Hash Presse  Oel Extraktor
            v                    |           |
      Cured Buds                 v           v
                              Hash      Cannabis Oil
```

---

## Step 1-2: Planting and Growing

Plant cannabis seeds in a pot with soil and water. The plant progresses through 8 growth stages (0-7). At stage 4 the plant becomes two blocks tall.

### Growth Modifiers

| Modifier | Effect | Trade-off |
|----------|--------|-----------|
| **Pot Type** (Terracotta to Golden) | Golden Pot gives +1 quality tier | Higher cost |
| **Soil** | 15 units consumed per full cycle | Must maintain supply |
| **Fertilizer** | +67% yield (6g to 10g) | -1 quality tier |
| **Growth Booster** | Halves growth time | -1 quality tier |
| **Quality Booster** | +1 quality tier | Cannot stack with fertilizer or growth booster |
| **Grow Lights** (Standard/Premium) | Faster growth | Required indoors |

### Harvesting

Harvest at stage 7 for full yield. Early harvest (stages 4-6) gives only 50% yield at Mids quality.

---

## Step 3: Drying (Trocknungsnetz)

Place fresh buds on the **Trocknungsnetz** (drying net) and wait. Quality, strain, and weight are all preserved.

| Property | Value |
|----------|-------|
| Capacity | 4 slots |
| Processing Time | 72,000 ticks (3 Minecraft days) per slot |
| Quality | Preserved from input |

Each slot processes independently. You can fill slots at different times.

---

## Step 4: Trimming (Trimm Station) -- MINIGAME

The trimming station is the most important step for quality. It uses a timing-based minigame where you trim 10 leaves from each batch.

### Minigame Mechanics

A visual indicator sweeps across a bar. Press the action key when the indicator is in the target zone. Repeat 10 times per batch.

```
  [====|=====GREEN=====|====]
       <-- INDICATOR -->

  Perfect Zone (center): ticks 45-55   = 2 points
  Good Zone:             ticks 35-65   = 1 point
  Bad Zone:              everything else = 0 points
```

### Scoring

```
score = (perfectTrims * 1.0 + goodTrims * 0.6 + badTrims * 0.2) / 10

Score >= 0.95  -->  EXOTIC     (5.0x price)
Score >= 0.80  -->  TOP SHELF  (3.5x price)
Score >= 0.60  -->  DANK       (2.0x price)
Score >= 0.40  -->  MIDS       (1.0x price)
Score <  0.40  -->  SCHWAG     (0.5x price)
```

### Trimming Output

- **Trimmed Buds:** Same weight as input; quality set by minigame score
- **Cannabis Trim (by-product):** 50% of input weight; used for hash or oil

---

## Step 5: Curing (Curing Glas)

Optional long-term aging in curing jars. Dramatically improves quality over time.

| Curing Duration | Quality Upgrade | Price Bonus |
|----------------|----------------|-------------|
| 14 days | +1 tier | +23% |
| 28 days | +2 tiers | +47% |
| 60 days | +2 tiers (max) | +50% (max) |

Curing can push Mids all the way to Top Shelf, or Dank all the way to Exotic.

---

## Step 6: Hash Production (Hash Presse)

Convert trim into concentrated hash using the Hash Presse.

| Property | Value |
|----------|-------|
| Minimum Input | 20g trim (single strain) |
| Processing Time | 6,000 ticks (5 minutes) |
| Conversion Rate | 25% (4:1 ratio) |
| THC Concentration | 1.5x the strain's base THC |

Hash quality depends on input weight: 80g+ trim yields Top Shelf, 50g+ yields Dank, 30g+ yields Mids, below that yields Schwag.

---

## Step 7: Oil Extraction (Oel Extraktor)

Create high-concentration cannabis oil from buds or trim.

| Property | Value |
|----------|-------|
| Minimum Input | 10g material (single strain) + 1 Extraction Solvent |
| Processing Time | 12,000 ticks (10 minutes) |
| Bud Conversion | 15% (10g buds = 1.5ml oil) |
| Trim Conversion | 8% (10g trim = 0.8ml oil) |
| THC Concentration | 3x the strain's base THC |

Oil from trimmed buds gets +1 quality tier. Oil from trim is always Mids quality.

---

## Quality Tiers

| Quality | Color | Price Multiplier | Description |
|---------|-------|-----------------|-------------|
| **Schwag** | Gray | 0.5x | Poor quality with seeds |
| **Mids** | Light Gray | 1.0x | Average quality |
| **Dank** | Green | 2.0x | Good quality |
| **Top Shelf** | Gold | 3.5x | Premium quality |
| **Exotic** | Bold Magenta | 5.0x | Best possible quality |

---

## Items Table (10 Items)

| Item | Description | How Obtained |
|------|-------------|-------------|
| Cannabis Seeds | Strain-specific planting seeds (Indica/Sativa/Hybrid/Autoflower) | Purchase / loot |
| Fresh Cannabis Buds | Unprocessed harvested buds | Harvesting mature plant |
| Dried Cannabis Buds | Moisture-removed buds | Trocknungsnetz (Drying Net) |
| Trimmed Cannabis Buds | Quality-graded buds | Trimm Station minigame |
| Cannabis Trim | Leaf by-product from trimming | Trimm Station (50% of input weight) |
| Cured Cannabis Buds | Long-term aged buds | Curing Glas (14-28 days) |
| Hash | Concentrated trim product | Hash Presse |
| Cannabis Oil | High-potency extract | Oel Extraktor |
| Extraction Solvent | Required for oil extraction | Crafting / purchase |
| Spore Syringe | Used for inoculation (shared item) | Purchase |

---

## Blocks Table (9 Blocks)

| Block | Description | Function |
|-------|-------------|----------|
| Terracotta Pot | Basic planting pot | Standard growth container |
| Ceramic Pot | Improved pot | Better water retention |
| Iron Pot | Industrial pot | Durable, good value |
| Golden Pot | Premium pot | +1 quality tier on harvest |
| Trocknungsnetz (Drying Net) | Passive drying station | Dries 4 buds over 3 Minecraft days |
| Trimm Station | Interactive trimming station | Timing minigame; determines quality |
| Curing Glas (Curing Jar) | Long-term aging container | +1 or +2 quality tiers over 14-28 days |
| Hash Presse | Trim-to-hash press | Converts 20g+ trim into concentrated hash |
| Oel Extraktor (Oil Extractor) | Oil extraction machine | Creates high-THC oil from buds or trim |

---

## Quality Modifiers

| Modifier | Stage | Effect |
|----------|-------|--------|
| Golden Pot | Planting | +1 quality tier on harvest |
| Quality Booster | Growing | +1 quality tier |
| Fertilizer | Growing | +67% yield but -1 quality |
| Trimming Score | Trimming | Primary quality determinant (0.0 to 1.0) |
| Curing Duration | Curing | +1 tier at 14 days, +2 tiers at 28 days |
| Hash Input Weight | Hash | 80g+ = Top Shelf, 50g+ = Dank |
| Bud vs Trim Input | Oil | Buds give +1 tier; trim is always Mids |

---

## Tips & Tricks

1. **Trimming skill is everything.** The minigame is the single biggest quality lever. Practice until you can hit 8+ perfect trims consistently.
2. **Autoflower for beginners.** It grows in half the time and teaches the pipeline fast. Switch to Hybrid or Indica once you are comfortable.
3. **Do not waste Quality Boosters with Fertilizer.** They cancel each other out. Choose yield (fertilizer) or quality (booster), not both.
4. **Golden Pot + perfect trimming** can reach Exotic without any curing. This is the fastest path to top-tier product.
5. **Cure Dank-quality buds for 28 days** to reach Exotic. This is the cheapest path -- no Golden Pot required, just patience.
6. **Save trim for 80g+ batches** before making hash. Small batches produce Schwag hash, which is barely worth the effort.
7. **Use trim for oil, sell buds directly.** Oil from buds actually loses value compared to selling the buds. Trim-to-oil is pure profit.
8. **Stagger your drying net.** Fill one slot per day to create a continuous harvest pipeline instead of waiting 3 days for everything at once.
9. **Hybrid is the best all-around strain.** Balanced THC/CBD, reasonable growth time, and the highest CBD for medical-style products.
10. **Count the rhythm during trimming.** The indicator moves at a constant speed. Learn to anticipate the center rather than react to it.

---

*See also: [Tobacco System](Tobacco-System.md) | [Mushroom System](Mushroom-System.md) | [Production Systems Overview](../Production-Systems.md)*
