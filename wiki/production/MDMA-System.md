# MDMA/Ecstasy Production System

An arcade-style 4-step production chain featuring safrol extraction, a glowing reaction kettle, a hot drying oven, and an interactive timing minigame for pill pressing with 64 customization combinations.

**Stats at a Glance:**

| Attribute | Value |
|-----------|-------|
| Strains | N/A (chemical synthesis from Safrol) |
| Production Steps | 4 |
| Total Items | 6 |
| Total Blocks | 3 |
| Quality Tiers | Schlecht, Standard, Gut, Premium |
| Unique Feature | Timing minigame for pill pressing |

---

## Overview

MDMA production combines chemical synthesis with an arcade-style pill pressing minigame. The first three steps are passive (extraction, reaction, drying), but the final step -- the Pillen Presse -- requires the player to nail a timing window to determine pill quality. Players can customize their pills with 8 colors and 8 stamp designs for 64 unique combinations. The Reaktions Kessel glows during operation and the Trocknungs Ofen radiates heat.

---

## Production Chain

```
  [1. SAFROL EXTRACTION]    Starting material
   Safrol            -->    Base ingredient
       |
       v
  [2. REACTION]             Reaktions Kessel
   Safrol --> MDMA Base     GLOWING BLOCK! (light 4)
   Time: 50 sec             8 capacity
   40% Gut if 6+ Safrol
       |
       v
   MDMA Base
       |
       v
  [3. DRYING]               Trocknungs Ofen
   MDMA Base --> Kristall    HOT BLOCK! (light 8)
   Time: 30 sec              120 C operating temp
   Quality preserved          8 capacity
       |
       v
   MDMA Kristall
       |
       v
  [4. PILL PRESSING]        Pillen Presse
   TIMING MINIGAME!     --> 16 Kristall + 16 Bindemittel
   + Bindemittel (binder)    Quality = timing skill
   + Farbstoff (dye)         Color + Design selection
       |
       v
   Ecstasy Pills
   (64 color/design combos)
```

---

## Step 1: Safrol Extraction

Safrol is the base precursor material for MDMA synthesis. Obtain it from NPC shops or crafting.

---

## Step 2: Reaction (Reaktions Kessel)

Synthesize safrol into MDMA base using the Reaktions Kessel (reaction kettle). The block **glows** at light level 4 during operation.

| Property | Value |
|----------|-------|
| Input | Safrol (up to 8) |
| Capacity | 8 units |
| Processing Time | 1,000 ticks (50 seconds) |
| Output | 8 MDMA-Base (1:1 ratio) |
| Light Level | 4 (glowing during operation) |

### Quality from Batch Size

The amount of safrol loaded affects base quality chance:

| Safrol Count | Gut Quality Chance |
|-------------|-------------------|
| 6-8 Safrol | **40%** |
| 4-5 Safrol | 25% |
| 1-3 Safrol | 10% |

Always load 6+ safrol per batch for the best quality odds.

---

## Step 3: Drying (Trocknungs Ofen)

Dry MDMA base into crystalline form using the Trocknungs Ofen (drying oven). The oven operates at 120 C and **emits light at level 8** -- it runs hot.

| Property | Value |
|----------|-------|
| Input | MDMA-Base (up to 8) |
| Capacity | 8 units |
| Processing Time | 600 ticks (30 seconds) |
| Output | 8 MDMA-Kristall (1:1 ratio) |
| Light Level | 8 (hot, glowing) |
| Quality | Preserved from input |

---

## Step 4: Pill Pressing (Pillen Presse) -- TIMING MINIGAME

The Pillen Presse combines MDMA Kristall with Bindemittel (binder) and optional Farbstoff (dye) into finished ecstasy pills. Quality is determined entirely by your timing skill.

### Materials

| Material | Description | Required |
|----------|-------------|----------|
| **MDMA Kristall** | Crystallized MDMA | Yes (up to 16) |
| **Bindemittel** (Binder) | Pill binding agent | Yes (1 per pill) |
| **Farbstoff** (Dye) | Pill coloring | Optional (for custom colors) |

### Timing Minigame

A visual indicator sweeps across a bar in a 60-tick (3-second) cycle. Press the action button when the indicator is in the green zone.

```
  [0------20]--[25----35]--[40------60]
    EARLY        PERFECT      LATE
    (Red)        (Green)      (Red)

  Tick 0-20:   Too Early   -->  Schlecht / Standard
  Tick 20-24:  Good        -->  Gut
  Tick 25-35:  PERFECT     -->  Premium (tick 30 = exact center)
  Tick 36-40:  Good        -->  Gut
  Tick 40-60:  Too Late    -->  Schlecht
```

### Scoring

```
Perfect Zone (ticks 25-35):
  score = 1.0 - (distance from tick 30 / 10.0) * 0.1
  Tick 30 = score 1.0 (perfect)
  Tick 25 = score 0.95 (Premium threshold)

Good Zone (ticks 20-24 and 36-40):
  score = 0.6 to 0.9

Early/Late Zones:
  score < 0.5
```

### Quality from Score

| Quality | Score Threshold | Price Multiplier |
|---------|----------------|-----------------|
| **Schlecht** (Poor) | Below 0.5 | 0.5x |
| **Standard** (Normal) | 0.5 - 0.79 | 1.0x |
| **Gut** (Good) | 0.8 - 0.94 | 2.0x |
| **Premium** (Laboratory) | 0.95+ | **4.0x** |

Final quality is the higher of the input crystal quality and the timing quality. Perfect timing can upgrade even Standard input to Premium output.

---

## Customization

### 8 Pill Colors

| Color | Display Code |
|-------|-------------|
| Pink | Magenta |
| Blue | Dark Blue |
| Green | Green |
| Orange | Gold |
| Yellow | Yellow |
| White | White |
| Red | Red |
| Purple | Dark Purple |

### 8 Pill Designs (Stamps)

| Design | Symbol | Theme |
|--------|--------|-------|
| **Tesla** | T | Tesla logo |
| **Superman** | S | Superhero |
| **Totenkopf** | Skull | Skull/hardcore |
| **Herz** | Heart | Heart/love |
| **Schmetterling** | Butterfly | Butterfly/euphoria |
| **Stern** | Star | Star/classic |
| **Peace** | Peace sign | Peace symbol/rave |
| **Diamant** | Diamond | Diamond/premium |

**Total Combinations:** 8 colors x 8 designs = **64 unique pill variants**

---

## Items Table (6 Items)

| Item | Description | How Obtained |
|------|-------------|-------------|
| Safrol | Base precursor for MDMA synthesis | Purchase / crafting |
| MDMA-Base | Intermediate liquid product | Reaktions Kessel |
| MDMA-Kristall | Dried crystal form | Trocknungs Ofen |
| Bindemittel (Binder) | Required for pill pressing | Purchase / crafting |
| Farbstoff (Dye) | Optional pill coloring | Purchase / crafting |
| Ecstasy Pills | Finished customized pills | Pillen Presse (timing minigame) |

---

## Blocks Table (3 Blocks)

| Block | Description | Function |
|-------|-------------|----------|
| Reaktions Kessel | Reaction kettle (**glowing**, light level 4) | Synthesizes safrol into MDMA-Base (8 slots, 50 sec); 40% Gut with 6+ safrol |
| Trocknungs Ofen | Drying oven (**hot**, light level 8, 120 C) | Dries MDMA-Base into crystals (8 slots, 30 sec); quality preserved |
| Pillen Presse | Pill press (**interactive GUI**) | Timing minigame; combines crystals + binder into pills; color/design selection |

---

## Quality Modifiers

| Modifier | Stage | Effect |
|----------|-------|--------|
| Safrol Batch Size | Reaction | 6+ safrol = 40% Gut; 4-5 = 25%; 1-3 = 10% |
| Timing Score | Pill Pressing | Primary quality determinant (0.0 to 1.0) |
| Input Quality | Pill Pressing | Final quality = max(input quality, timing quality) |
| Perfect Timing | Pill Pressing | Score 0.95+ = Premium (4.0x price) |
| Gut Input + Perfect Timing | Combined | Guaranteed Premium output |

---

## Tips & Tricks

1. **Always load 6+ safrol per reaction batch.** This gives 40% Gut quality chance versus only 10% with smaller batches. The difference is enormous.
2. **Master the 3-second timing window.** The pill press minigame is a 60-tick cycle. Learn to hit tick 28-32 consistently for Premium quality.
3. **Count the rhythm.** The indicator moves at constant speed. Practice counting "one... two... PRESS!" to build muscle memory for the center.
4. **Gut input + perfect timing = guaranteed Premium.** Since final quality is the max of input and timing, starting with Gut crystals means you only need a 0.95+ score for Premium.
5. **The Reaktions Kessel glows at light level 4** and the Trocknungs Ofen at light level 8. Plan your lab layout knowing these blocks emit light.
6. **The oven runs at 120 C.** While this does not cause fire, it is a visual/thematic indicator of the drying process.
7. **Tesla and Superman stamps are the most popular.** If you are selling to other players, these iconic designs may command slight premiums.
8. **Practice timing on Standard Kristall first.** Do not waste Gut-quality crystals until your Premium hit rate exceeds 80%.
9. **Bindemittel is cheap but essential.** Stock up before production runs -- you need one per pill with no substitutes.
10. **Create limited-edition color/design combinations** to build brand recognition. A consistent "signature pill" can become your market identity.

---

*See also: [LSD System](LSD-System.md) | [Cannabis System](Cannabis-System.md) | [Production Systems Overview](../Production-Systems.md)*
