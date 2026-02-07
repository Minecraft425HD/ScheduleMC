# LSD Production System

The most scientific production system in ScheduleMC. A 6-step precision lab process featuring ergot fermentation, glowing distillation, a GUI-based micro-dosing system, and perforated blotter sheets.

**Stats at a Glance:**

| Attribute | Value |
|-----------|-------|
| Strains | N/A (chemical synthesis from Mutterkorn) |
| Production Steps | 6 |
| Total Items | 6 |
| Total Blocks | 4 |
| Dosage Levels | Schwach (50ug), Standard (100ug), Stark (200ug), Bicycle Day (300ug) |

---

## Overview

LSD production is a precision chemistry pipeline. It begins with fermenting Mutterkorn (ergot) into ergot culture, distilling it into Lysergsaeure (lysergic acid), synthesizing LSD Loesung (solution), applying it to blotter paper via a GUI-controlled Mikro Dosierer, and perforating the sheets into individual tabs. The system rewards careful dosage selection -- higher doses mean fewer tabs per batch but dramatically higher per-tab value.

---

## Production Chain

```
  [1. FERMENTATION]         Fermentations Tank
   Mutterkorn culture  -->  8 capacity
   Time: 60 sec             1:1 output
       |
       v
   Ergot-Kultur
       |
       v
  [2. DISTILLATION]         Destillations Apparat
   GLOWING BLOCK!      -->  4 capacity
   Time: 40 sec             2:1 yield (double!)
       |
       v
   Lysergsaeure
       |
       v
  [3. CHEMISTRY]            Synthesis step
   LSD synthesis       -->  Lysergsaeure to
                             LSD Loesung
       |
       v
   LSD Loesung (Solution)
       |
       v
  [4. MICRO-DOSING]         Mikro Dosierer
   HAS GUI!            -->  Slider: 0-100
   Time: 10 sec             Maps to 50-300ug
   Select dosage             Charges per bottle vary
       |
       v
   Dosed LSD Loesung
       |
       v
  [5. PERFORATION]          Perforations Presse
   LSD Loesung         -->  9 tabs per sheet
   + Blotter Paper           Time: 5 sec
       |
       v
   LSD Blotter Tabs
       |
       v
  [6. FINAL PRODUCT]        LSD Blotter Sheets
   Ready for sale            Dosage + design marked
```

---

## Step 1: Fermentation (Fermentations Tank)

Culture Mutterkorn (ergot) into usable ergot culture.

| Property | Value |
|----------|-------|
| Input | Mutterkorn |
| Capacity | 8 units |
| Processing Time | 1,200 ticks (60 seconds) |
| Output | 8 Ergot-Kultur (1:1 ratio) |

---

## Step 2: Distillation (Destillations Apparat)

Distill ergot culture into lysergic acid. The distillation apparatus **glows** during operation (light level 6).

| Property | Value |
|----------|-------|
| Input | Ergot-Kultur |
| Capacity | 4 units |
| Processing Time | 800 ticks (40 seconds) |
| Output | 8 Lysergsaeure (**2:1 yield -- double output!**) |
| Light Level | 6 (glowing block) |

The 2:1 yield is a key efficiency point. Every 4 ergot cultures produce 8 lysergic acid units.

---

## Step 3: Chemistry (LSD Synthesis)

Synthesize lysergic acid into LSD solution.

| Property | Value |
|----------|-------|
| Input | Lysergsaeure |
| Output | LSD Loesung (solution) |
| Process | Chemical synthesis |

---

## Step 4: Micro-Dosing (Mikro Dosierer)

Apply LSD solution to blotter paper using a GUI-based dosing system. The Mikro Dosierer has an interactive slider interface.

| Property | Value |
|----------|-------|
| Input | Lysergsaeure (up to 16) |
| Processing Time | 200 ticks (10 seconds) |
| GUI | Slider control (0-100) maps to 50-300ug dosage |
| Output | 1 LSD Loesung bottle with variable charges |

### Dosage Levels

The slider position determines dosage, which affects both the number of charges per bottle and the price per tab.

| Dosage | Micrograms | Price Multiplier | Base Price | Charges per Lysergsaeure |
|--------|-----------|-----------------|------------|-------------------------|
| **Schwach** (Light) | 50ug | 1.0x | 25 | 9 charges |
| **Standard** (Normal) | 100ug | 2.0x | 50 | 8 charges |
| **Stark** (Strong) | 200ug | 3.5x | 87.50 | 6 charges |
| **Bicycle Day** | 300ug | **6.0x** | **150** | 4 charges |

**Bicycle Day** refers to April 19, 1943 -- Albert Hofmann's famous first intentional LSD experience. It is the highest dosage tier and commands the greatest per-tab price, but produces the fewest tabs per batch.

### Trade-off: Volume vs Value

```
16 Lysergsaeure at Schwach (50ug):   144 charges  -->  144 tabs at 25 each  = 3,600
16 Lysergsaeure at Bicycle Day:       64 charges  -->   64 tabs at 150 each = 9,600

Bicycle Day produces 56% fewer tabs but 167% more revenue.
```

---

## Step 5: Perforation (Perforations Presse)

Cut dosed blotter paper into individual tabs.

| Property | Value |
|----------|-------|
| Input | LSD Loesung + Blotter Paper |
| Processing Time | 100 ticks (5 seconds) |
| Output | 9 LSD tabs per paper sheet |
| Design Selection | Shift+click to cycle through designs |

### Blotter Designs (8 Designs)

| Design | Symbol | Theme |
|--------|--------|-------|
| Totenkopf | Skull | Dark gray skull |
| Sonne | Sun | Yellow sun |
| Auge | Eye | Purple eye |
| Pilz | Mushroom | Red mushroom |
| Fahrrad | Bicycle | Aqua bicycle |
| Mandala | Flower | Pink mandala |
| Blitz | Lightning | Gold lightning bolt |
| Stern | Star | White star |

---

## Step 6: Final Product (LSD Blotter Sheets)

The finished LSD Blotter tabs are ready for sale. Each tab carries its dosage level and blotter design.

---

## Items Table (6 Items)

| Item | Description | How Obtained |
|------|-------------|-------------|
| Mutterkorn | Ergot fungus starting material | Purchase / loot |
| Ergot-Kultur | Fermented ergot culture | Fermentations Tank |
| Lysergsaeure | Lysergic acid (distilled) | Destillations Apparat (2:1 yield) |
| LSD Loesung | LSD solution with charges | Mikro Dosierer (GUI dosing) |
| Blotter Paper | Blank paper for tab production | Purchase / crafting |
| LSD Blotter Tabs | Final product (9 per sheet) | Perforations Presse |

---

## Blocks Table (4 Blocks)

| Block | Description | Function |
|-------|-------------|----------|
| Fermentations Tank | Ergot fermentation vessel | Converts 8 Mutterkorn to 8 Ergot-Kultur (60 sec) |
| Destillations Apparat | Distillation apparatus (**glowing**, light level 6) | Converts 4 Ergot-Kultur to 8 Lysergsaeure (40 sec, 2:1 yield) |
| Mikro Dosierer | Micro-dosing station (**has GUI with slider**) | Applies dosage to LSD solution (10 sec); slider selects 50-300ug |
| Perforations Presse | Perforation press | Cuts blotter sheets into 9 tabs each (5 sec) |

---

## Quality Modifiers

LSD quality is primarily determined by dosage level rather than a traditional quality tier system.

| Modifier | Stage | Effect |
|----------|-------|--------|
| Dosage Selection | Micro-Dosing | Higher dose = higher price per tab but fewer tabs |
| 2:1 Distillation Yield | Distillation | Doubles lysergic acid output for efficiency |
| Blotter Design | Perforation | Cosmetic choice; does not affect price |
| Batch Size | All Stages | Larger batches improve throughput efficiency |

The key optimization decision is dosage: Schwach maximizes tab count, Bicycle Day maximizes revenue per batch.

---

## Tips & Tricks

1. **Bicycle Day is the most profitable dosage** on a per-batch basis. If you can sell all 64 tabs, it generates 167% more revenue than Schwach dosage from the same input.
2. **The distillation apparatus glows.** At light level 6, it can serve as ambient lighting for your lab, but it also makes your operation visible. Build underground if secrecy matters.
3. **The 2:1 distillation yield is the system's biggest efficiency lever.** 4 ergot cultures become 8 lysergic acid units. Plan your pipeline around this doubling.
4. **The Mikro Dosierer GUI lets you fine-tune dosage.** The slider maps 0-100 to 50-300ug. Experiment with the slider to find the exact dosage level you want.
5. **Blotter design is purely cosmetic** but can influence player preference in a market context. Fahrrad (Bicycle) is thematic for Bicycle Day tabs.
6. **Start with Schwach dosage** to maximize tab output while learning the pipeline. Switch to higher dosages once you have reliable demand.
7. **Perforating is fast (5 seconds per sheet).** The bottleneck is fermentation (60 seconds). Run multiple fermentation tanks to keep the pipeline flowing.
8. **Stock Blotter Paper in advance.** Running out during a production run wastes your dosed solution's shelf time.
9. **The full pipeline from 8 Mutterkorn to finished tabs takes roughly 115 seconds.** This is one of the faster production systems once equipment is set up.
10. **Combine Bicycle Day dosage with the Fahrrad blotter design** for the most thematically authentic product in the game.

---

*See also: [MDMA System](MDMA-System.md) | [Mushroom System](Mushroom-System.md) | [Production Systems Overview](../Production-Systems.md)*
