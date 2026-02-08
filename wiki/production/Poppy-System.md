# Poppy Production System

A 6-step chemical refinement chain with 3 strains, multiple extraction methods, and a complete pipeline from raw opium through morphine to heroin.

**Stats at a Glance:**

| Attribute | Value |
|-----------|-------|
| Strains | 3 (Afghanisch, Tuerkisch, Indisch) |
| Production Steps | 6 |
| Total Items | 8 |
| Total Blocks | 7 |
| Quality Tiers | Poor, Good, Very Good, Legendary |

---

## Overview

Poppy production is the deepest refinement chain in ScheduleMC. Starting from planted seeds, players harvest poppy pods, score them using a manual knife or automated machine, press raw opium, cook it into morphine at the Kochstation, and finally refine it into heroin at the Heroin Raffinerie. Quality cascades through every step, and the Afghan strain's 1.5x potency multiplier makes it the most profitable strain in the game when combined with Legendary quality.

---

## Strains

| Strain | Potency | Growth Speed | Seed Price | Water Use | Quality Chance per Stage | Best For |
|--------|---------|-------------|------------|-----------|------------------------|----------|
| **Afghanisch** (Afghan) | 150% | Slow (160 ticks) | 50 | 1.2x (high) | 37.5% | Maximum profit |
| **Tuerkisch** (Turkish) | 100% | Medium (120 ticks) | 35 | 1.0x | 25% | Balanced production |
| **Indisch** (Indian) | 80% | Fast (80 ticks) | 20 | 0.8x (low) | 20% | High-volume runs |

Quality improves at growth stages 3, 5, and 7. Afghan has the highest upgrade chance per stage due to its potency multiplier.

---

## Production Chain

```
  [1. PLANTING]        [2. HARVESTING]
   Seeds + Pot    -->   Poppy Pods
   + Soil + Water       (Stage 7)
       |                     |
       v                     v
  Planted Pot         6-10 Poppy Pods
                             |
                             v
  [3. SCORING]         Ritzmaschine
   Manual: Scoring     (Automated)
   Knife (128 uses)    or Opium Presse
       |                (High-Yield)
       v
   Raw Opium
       |
       v
  [4. PRESSING]        Opium Presse
   16 slots, diesel    Yield: 2-5 per pod
   powered             (quality-dependent)
       |
       v
   Raw Opium
       |
       v
  [5. COOKING]         Kochstation
   Water + Fuel        1:1 ratio
   10 sec per unit     Quality preserved
       |
       v
   Morphine
       |
       v
  [6. REFINING]        Heroin Raffinerie
   Fuel powered        1:1 ratio
   15 sec per unit     20% quality upgrade
       |
       v
   Heroin
```

---

## Step 1-2: Planting and Harvesting

Plant poppy seeds in a pot with soil and water. Growth stages 0-7; plants become two blocks tall at stage 4.

### Quality-Based Yield

Quality directly affects harvest yield:

| Quality | Yield Multiplier | Base Pods | With Fertilizer |
|---------|-----------------|-----------|-----------------|
| Poor (Schlecht) | 0.7x | 4 pods | 7 pods |
| Good (Gut) | 1.0x | 6 pods | 10 pods (capped) |
| Very Good (Sehr Gut) | 1.3x | 8 pods | 10 pods (capped) |
| Legendary (Legendaer) | 1.6x | 10 pods (capped) | 10 pods (capped) |

---

## Step 3: Scoring (Ritzmaschine)

Score poppy pods to extract raw opium. Three methods are available:

### Extraction Methods

| Method | Equipment | Capacity | Time | Yield per Pod | Power |
|--------|-----------|----------|------|--------------|-------|
| **Manual** | Scoring Knife | 1 (manual) | Instant | 1-3 (quality-based) | None |
| **Automated** | Ritzmaschine | 8 pods | 5 sec/pod | 1-3 (quality-based) | Redstone |
| **High-Yield** | Opium Presse | 16 pods | 4 sec/pod | **2-5** (quality-based) | Diesel |

### Opium Presse Yield (Best Method)

| Quality | Opium per Pod |
|---------|--------------|
| Poor | 2 |
| Good | 3 |
| Very Good | 4 |
| Legendary | **5** |

The Opium Presse yields 67-100% more than manual scoring. Always use it for maximum output.

---

## Step 4: Cooking (Kochstation)

Cook raw opium into morphine at the Kochstation.

| Property | Value |
|----------|-------|
| Capacity | 8 slots (parallel) |
| Time | 200 ticks (10 seconds) per unit |
| Water Consumption | 10 units per opium |
| Fuel Consumption | 10 units per opium |
| Output | 1 Morphine per 1 Raw Opium (1:1) |
| Quality | Preserved from input |

Both water and fuel must be maintained. If either runs out, processing pauses but progress is preserved.

---

## Step 5: Refining (Heroin Raffinerie)

Refine morphine into heroin at the Heroin Raffinerie.

| Property | Value |
|----------|-------|
| Capacity | 8 slots (parallel) |
| Time | 300 ticks (15 seconds) per unit |
| Fuel Consumption | 15 units per morphine |
| Output | 1 Heroin per 1 Morphine (1:1) |
| Quality Upgrade | **20% chance** to upgrade +1 tier |

### Quality Upgrade

```
Good morphine       -->  20% Very Good heroin / 80% Good heroin
Very Good morphine  -->  20% Legendary heroin / 80% Very Good heroin
Legendary morphine  -->  100% Legendary heroin (cannot upgrade further)
```

---

## Items Table (8 Items)

| Item | Description | How Obtained |
|------|-------------|-------------|
| Afghanisch Poppy Seeds | Afghan strain seeds (highest potency) | Purchase / loot |
| Tuerkisch Poppy Seeds | Turkish strain seeds (balanced) | Purchase / loot |
| Indisch Poppy Seeds | Indian strain seeds (fastest) | Purchase / loot |
| Poppy Pods | Harvested pods (strain + quality preserved) | Harvesting mature plant |
| Raw Opium | Extracted opium resin | Scoring Knife / Ritzmaschine / Opium Presse |
| Morphine | Cooked opium product | Kochstation (water + fuel) |
| Heroin | Refined final product | Heroin Raffinerie (fuel) |
| Scoring Knife | Manual extraction tool (128 uses) | Crafting |

---

## Blocks Table (7 Blocks)

| Block | Description | Function |
|-------|-------------|----------|
| Terracotta/Ceramic/Iron/Golden Pot | Planting containers | Standard growth; Golden gives +1 quality |
| Ritzmaschine | Automated scoring machine | Extracts 1-3 opium per pod, requires redstone |
| Opium Presse | High-yield press | Extracts 2-5 opium per pod, requires diesel |
| Kochstation (Cooking Station) | Opium-to-morphine cooker | 8 parallel slots, requires water + fuel |
| Heroin Raffinerie | Morphine-to-heroin refinery | 8 parallel slots, 20% quality upgrade chance |

---

## Quality Modifiers

| Modifier | Stage | Effect |
|----------|-------|--------|
| Afghan Strain | Growing | 37.5% quality upgrade chance per stage (vs 25% Turkish, 20% Indian) |
| Golden Pot | Planting | +1 quality tier on harvest |
| Fertilizer | Growing | +15% quality upgrade chance per stage |
| Quality Booster | Growing | +20% quality upgrade chance per stage |
| Quality-Based Yield | Harvesting | Legendary yields 1.6x base pods |
| Opium Presse | Extraction | 2-5 opium per pod (quality-dependent) |
| Heroin Raffinerie | Refining | 20% chance to upgrade quality +1 tier |

With Afghan + Fertilizer + Quality Booster: 72.5% upgrade chance per growth stage. Three upgrade opportunities (stages 3, 5, 7) make Legendary achievable in most runs.

---

## Tips & Tricks

1. **Afghan is king for profit.** The 1.5x potency multiplier affects the final price of every heroin unit. No other strain comes close at Legendary quality.
2. **Always use the Opium Presse.** It yields 67-100% more opium than manual scoring. The diesel cost is negligible compared to the extra output.
3. **Stack all growth modifiers for Afghan.** Fertilizer (+15%) + Quality Booster (+20%) + Afghan base (37.5%) = 72.5% upgrade chance per stage. Legendary is almost guaranteed.
4. **Quality cascades through the entire chain.** A Legendary pod produces 5 opium at the Presse, each becoming Legendary morphine, then Legendary heroin. One good plant can produce 50 Legendary heroin units.
5. **Indian is the training-wheels strain.** Cheap seeds, fast growth, low water use. Use it to learn the pipeline before investing in Afghan.
6. **Keep water and fuel reserves stocked.** The Kochstation needs both; the Raffinerie needs fuel. Running out pauses but does not lose progress.
7. **Run large batches through the Raffinerie** to normalize the 20% upgrade chance. Small batches are too subject to RNG.
8. **Build dedicated resource storage** for water, fuel, and diesel near your processing equipment. The pipeline has heavy resource demands.
9. **Golden Pot + Quality Booster + Afghan** can often reach Legendary before harvest, making the 20% refining upgrade redundant (but still welcome for lower-quality overflow).
10. **Turkish is the middle ground.** If Afghan seeds are too expensive early on, Turkish provides standard potency with reasonable growth speed.

---

*See also: [Coca System](Coca-System.md) | [Meth System](Meth-System.md) | [Production Systems Overview](../Production-Systems.md)*
