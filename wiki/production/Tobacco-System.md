# Tobacco Production System

The most complex production system in ScheduleMC, featuring 4 strains, a full 6-step production chain, and deep quality mechanics. Tobacco rewards patient, methodical players who invest in every stage of the pipeline.

**Stats at a Glance:**

| Attribute | Value |
|-----------|-------|
| Strains | 4 (Virginia, Burley, Oriental, Havana) |
| Production Steps | 6 |
| Total Items | 32 |
| Total Blocks | 23 |
| Quality Tiers | Poor, Good, Very Good, Legendary |

---

## Overview

Tobacco production spans six distinct stages: planting, growing, drying, fermentation, packaging, and selling. Each step affects the final product's quality and market value. The system features four unique strains, tiered equipment at every stage, and a dynamic NPC selling system driven by negotiation, quality, and market supply/demand.

---

## Strains

| Strain | Profile | Growth Speed | Base Value | Best For |
|--------|---------|-------------|------------|----------|
| **Virginia** | Balanced, mild | Medium | Medium | Beginners -- forgiving at all stages |
| **Burley** | Strong, full-bodied | Slow | High | Extended fermentation for depth |
| **Oriental** | Aromatic, spicy | Fast | Medium-High | Quality booster synergy |
| **Havana** | Premium, smooth | Very Slow | Very High | Highest profit ceiling; hardest to master |

---

## Production Chain

```
  [1. PLANTING]       [2. GROWING]        [3. DRYING]
   Seeds + Pot    -->   Fertilizer    -->   Drying Rack
   + Soil Bag          + Boosters          (Time-based)
   + Watering Can      + Grow Lights
       |                    |                   |
       v                    v                   v
  Planted Pot  -->    Mature Plant   -->   Dried Tobacco
                                               |
                                               v
  [6. SELLING]       [5. PACKAGING]      [4. FERMENTATION]
   NPC Dealer   <--   Packaging     <--   Barrel
   Negotiation        Table (2x2)         (Time-based)
   Dynamic Price      + Materials         Enhances Flavor
       ^                   ^                   |
       |                   |                   v
  Final Sale  <--  Packaged Product  <--  Fermented Tobacco
```

---

## Step 1: Planting

Planting establishes the foundation. Pot type, soil quality, and watering all influence starting growth conditions.

### Pot Types

| Pot | Durability | Growth Bonus | Notes |
|-----|-----------|-------------|-------|
| **Terracotta Pot** | Low | None | Cheapest option |
| **Ceramic Pot** | Medium | +5% growth speed | Solid mid-tier |
| **Iron Pot** | High | +10% growth speed | Best cost-to-benefit ratio |
| **Golden Pot** | Very High | +20% growth speed, +5% quality | Premium investment |

### Soil Bags

| Size | Capacity | Nutrient Duration |
|------|----------|-------------------|
| **Small (S)** | 1 pot fill | Short |
| **Medium (M)** | 3 pot fills | Medium |
| **Large (L)** | 6 pot fills | Long |

### Watering

Use the **Watering Can** on a planted pot to hydrate the soil. Plants require regular watering throughout the growing phase. Neglecting water causes growth to stall and quality to degrade over time.

---

## Step 2: Growing

Plants advance through 8 distinct growth stages. Environmental factors and applied items determine how fast and how well a plant matures.

### Growth Stages

| Stage | Name | Description |
|-------|------|-------------|
| 1 | Sprout | Seed has germinated |
| 2 | Seedling | First leaves appear |
| 3 | Young Plant | Stem strengthening |
| 4 | Vegetative | Rapid leaf growth |
| 5 | Pre-Mature | Leaves filling out |
| 6 | Maturing | Leaves reaching full size |
| 7 | Near Harvest | Leaves beginning to yellow |
| 8 | Harvest Ready | Full maturity -- ready to pick |

### Growth Modifiers

| Item | Effect | Usage |
|------|--------|-------|
| **Fertilizer** | Increases growth speed | Apply to pot during stages 1-5 |
| **Growth Booster** | Significantly accelerates current stage | Single-use consumable |
| **Quality Booster** | Increases final quality tier chance | Apply during stages 4-7 for best effect |

### Grow Lights (3 Tiers)

| Grow Light | Range | Growth Bonus | Notes |
|-----------|-------|-------------|-------|
| **Tier 1** | 1 block | +10% speed | Entry-level |
| **Tier 2** | 2 blocks | +25% speed | Covers small setups |
| **Tier 3** | 3 blocks | +40% speed, +5% quality | Best for premium production |

---

## Step 3: Drying

Harvested tobacco leaves must be dried before further processing. Place leaves on a drying rack and wait.

### Drying Racks

| Rack Size | Capacity | Drying Speed |
|-----------|----------|-------------|
| **Small Drying Rack** | 4 leaves | Normal |
| **Medium Drying Rack** | 8 leaves | +15% faster |
| **Big Drying Rack** | 16 leaves | +30% faster |

Drying is entirely time-based. No items accelerate this step. Build multiple racks to avoid bottlenecks.

---

## Step 4: Fermentation

Fermentation develops flavor complexity. Place dried tobacco into a barrel and allow it to ferment. Duration directly affects quality, but each strain has a different sweet spot. Over-fermentation yields diminishing returns.

### Barrels

| Barrel Size | Capacity | Bonus |
|-------------|----------|-------|
| **Small Barrel** | 8 units | None |
| **Medium Barrel** | 16 units | +10% flavor |
| **Large Barrel** | 32 units | +20% flavor, more consistent quality |

### Fermentation Duration

| Duration | Effect |
|----------|--------|
| Under-fermented | Harsh taste, lower quality |
| Optimal | Full flavor, quality boost |
| Over-fermented | Diminishing returns, slight quality loss |

---

## Step 5: Packaging

Packaging requires a **Packaging Table**, a multi-block structure that occupies a 2x2 area. Place all four table blocks in a square to form the workstation.

### Packaging Tables

| Table Size | Speed | Slots |
|------------|-------|-------|
| **Small Packaging Table** | Normal | 2 input, 1 output |
| **Medium Packaging Table** | +20% faster | 4 input, 2 output |
| **Big Packaging Table** | +40% faster | 6 input, 3 output |

### Package Sizes

| Package | Tobacco Required | Materials Needed | Value Multiplier |
|---------|-----------------|-----------------|-----------------|
| **Small (S)** | 4 units | 1 Packaging Material | 1.0x |
| **Medium (M)** | 8 units | 2 Packaging Material | 1.1x |
| **Large (L)** | 16 units | 3 Packaging Material | 1.25x |
| **Extra Large (XL)** | 32 units | 5 Packaging Material | 1.5x |

**Packaging Materials** are a required consumable. Stock up before starting a packaging session.

---

## Step 6: Selling

Sell packaged tobacco to NPC dealers through a negotiation interface with dynamic pricing.

### NPC Negotiation

When interacting with a buyer NPC, a negotiation window opens. You can:
- **Accept** the offered price
- **Counter-offer** a higher price (risk of rejection)
- **Walk away** and try another dealer

### Dynamic Pricing Factors

| Factor | Effect |
|--------|--------|
| **Quality** | Higher quality raises the base price |
| **Market Supply** | Flooded market lowers prices |
| **Market Demand** | Scarcity drives prices up |
| **Reputation** | Successful deals improve future offers |

---

## Quality Tiers

Quality accumulates across all production steps.

| Quality | Color | Value Multiplier | How to Achieve |
|---------|-------|-----------------|----------------|
| **Poor** | Gray | 0.5x | Rushed or neglected production |
| **Good** | Green | 1.0x | Standard care at each step |
| **Very Good** | Blue | 1.75x | Quality boosters + optimal fermentation |
| **Legendary** | Gold | 3.0x | Perfect conditions at every step: Golden Pot, Tier 3 lights, optimal fermentation, Large Barrel |

---

## Items Table (32 Items)

| Item | Description | How Obtained |
|------|-------------|-------------|
| Virginia Seeds | Balanced strain seeds | Purchase / loot |
| Burley Seeds | Strong strain seeds | Purchase / loot |
| Oriental Seeds | Aromatic strain seeds | Purchase / loot |
| Havana Seeds | Premium strain seeds | Rare NPC stock / loot |
| Small Soil Bag (S) | Fills 1 pot | Crafting / purchase |
| Medium Soil Bag (M) | Fills 3 pots | Crafting / purchase |
| Large Soil Bag (L) | Fills 6 pots | Crafting / purchase |
| Watering Can | Waters planted pots | Crafting |
| Fertilizer | Speeds up growth | Crafting / purchase |
| Growth Booster | Instant stage advancement | Crafting (rare ingredients) |
| Quality Booster | Increases quality tier probability | Crafting (rare ingredients) |
| Raw Virginia Leaves | Fresh harvested Virginia | Harvest mature Virginia plant |
| Raw Burley Leaves | Fresh harvested Burley | Harvest mature Burley plant |
| Raw Oriental Leaves | Fresh harvested Oriental | Harvest mature Oriental plant |
| Raw Havana Leaves | Fresh harvested Havana | Harvest mature Havana plant |
| Dried Virginia Tobacco | Virginia after drying | Drying Rack |
| Dried Burley Tobacco | Burley after drying | Drying Rack |
| Dried Oriental Tobacco | Oriental after drying | Drying Rack |
| Dried Havana Tobacco | Havana after drying | Drying Rack |
| Fermented Virginia Tobacco | Virginia after fermentation | Barrel |
| Fermented Burley Tobacco | Burley after fermentation | Barrel |
| Fermented Oriental Tobacco | Oriental after fermentation | Barrel |
| Fermented Havana Tobacco | Havana after fermentation | Barrel |
| Packaging Material | Required for packaging step | Crafting / purchase |
| Small Package (S) | 4-unit tobacco package | Packaging Table |
| Medium Package (M) | 8-unit tobacco package | Packaging Table |
| Large Package (L) | 16-unit tobacco package | Packaging Table |
| Extra Large Package (XL) | 32-unit tobacco package | Packaging Table |
| Virginia Tobacco Product | Finished Virginia product | Packaging Table |
| Burley Tobacco Product | Finished Burley product | Packaging Table |
| Oriental Tobacco Product | Finished Oriental product | Packaging Table |
| Havana Tobacco Product | Finished Havana product | Packaging Table |

---

## Blocks Table (23 Blocks)

| Block | Description | Function |
|-------|-------------|----------|
| Terracotta Pot | Basic planting pot | Holds soil and seeds; no bonus |
| Ceramic Pot | Improved planting pot | +5% growth speed |
| Iron Pot | Industrial planting pot | +10% growth speed |
| Golden Pot | Premium planting pot | +20% growth speed, +5% quality |
| Tier 1 Grow Light | Basic grow lamp | +10% growth speed, 1-block range |
| Tier 2 Grow Light | Advanced grow lamp | +25% growth speed, 2-block range |
| Tier 3 Grow Light | Elite grow lamp | +40% growth speed, +5% quality, 3-block range |
| Small Drying Rack | Compact drying station | Dries 4 leaves at normal speed |
| Medium Drying Rack | Standard drying station | Dries 8 leaves at +15% speed |
| Big Drying Rack | Large drying station | Dries 16 leaves at +30% speed |
| Small Barrel | Compact fermentation barrel | Ferments 8 units |
| Medium Barrel | Standard fermentation barrel | Ferments 16 units, +10% flavor |
| Large Barrel | Industrial fermentation barrel | Ferments 32 units, +20% flavor |
| Small Packaging Table (TL) | Top-left section | Part of 2x2 Small Packaging Table |
| Small Packaging Table (TR) | Top-right section | Part of 2x2 Small Packaging Table |
| Small Packaging Table (BL) | Bottom-left section | Part of 2x2 Small Packaging Table |
| Small Packaging Table (BR) | Bottom-right section | Part of 2x2 Small Packaging Table |
| Medium Packaging Table (TL) | Top-left section | Part of 2x2 Medium Packaging Table |
| Medium Packaging Table (TR) | Top-right section | Part of 2x2 Medium Packaging Table |
| Medium Packaging Table (BL) | Bottom-left section | Part of 2x2 Medium Packaging Table |
| Medium Packaging Table (BR) | Bottom-right section | Part of 2x2 Medium Packaging Table |
| Big Packaging Table | Large 2x2 multi-block table | Fastest packaging speed |
| NPC Dealer Stand | Tobacco buyer NPC location | Sell packaged tobacco products |

---

## Quality Modifiers

Quality is influenced at every stage. The following modifiers stack:

| Modifier | Stage | Effect |
|----------|-------|--------|
| Golden Pot | Planting | +5% quality baseline |
| Tier 3 Grow Light | Growing | +5% quality |
| Quality Booster | Growing (stages 4-7) | Significant quality tier increase |
| Optimal Fermentation | Fermentation | Full flavor development |
| Large Barrel | Fermentation | +20% flavor consistency |
| XL Package | Packaging | 1.5x value multiplier |

To reach **Legendary** quality reliably, combine: Golden Pot + Tier 3 Grow Light + Quality Booster (stages 4-7) + optimal fermentation in a Large Barrel.

---

## Tips & Tricks

1. **Start with Virginia.** It is the most forgiving strain and teaches the full pipeline without punishing mistakes.
2. **Build multiple drying racks early.** Drying is the biggest bottleneck since nothing speeds it up. Run several racks in parallel.
3. **Match barrel size to batch size.** A half-empty Large Barrel does not ferment better than a full Small Barrel. Fill your barrels completely.
4. **Watch fermentation timers.** Each strain has a different sweet spot. Over-fermentation wastes product.
5. **Golden Pot + Tier 3 Light** is the only reliable path to Legendary quality. Do not expect Legendary results from lesser equipment.
6. **Package in bulk.** XL packages give a 1.5x value multiplier. Save up for large batches whenever possible.
7. **Diversify strains for market advantage.** Supply/demand is tracked per strain. If everyone sells Virginia, its price drops. Havana stays expensive because fewer players produce it.
8. **Negotiate wisely.** Counter-offers that are too aggressive will be rejected. Start modest to build reputation, then push harder once your standing improves.
9. **Upgrade pots gradually.** Iron Pots offer the best cost-to-benefit ratio for mid-game. Save Golden Pots for Havana.
10. **Quality Boosters are most effective during stages 4-7.** Using them earlier wastes the effect.

---

*See also: [Cannabis System](Cannabis-System.md) | [Coca System](Coca-System.md) | [Production Systems Overview](../Production-Systems.md)*
