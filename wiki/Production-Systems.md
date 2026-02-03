# Production Systems Overview

**ScheduleMC** features **8 complete production chains** for different drugs, each with unique mechanics, processing steps, and profitability.

---

## 📊 Production Systems Comparison

| System | Strains | Steps | Items | Blocks | Complexity | Profitability | Time |
|--------|---------|-------|-------|--------|------------|---------------|------|
| **Tobacco** | 4 | 6 | 32 | 23 | ⭐⭐⭐⭐⭐ | 💰💰💰 | 45-60 min |
| **Cannabis** | 4 | 8 | 10 | 9 | ⭐⭐⭐⭐ | 💰💰💰💰 | 30-45 min |
| **Coca** | 2 | 5 | 9 | 9 | ⭐⭐⭐ | 💰💰💰💰 | 40-55 min |
| **Poppy** | 3 | 6 | 8 | 7 | ⭐⭐⭐ | 💰💰💰💰💰 | 50-70 min |
| **Meth** | - | 4 | 8 | 4 | ⭐⭐⭐⭐⭐ | 💰💰💰💰💰 | 40 min |
| **LSD** | - | 6 | 6 | 4 | ⭐⭐⭐⭐⭐ | 💰💰💰💰💰 | 60-80 min |
| **MDMA** | - | 4 | 6 | 3 | ⭐⭐⭐ | 💰💰💰💰 | 30-40 min |
| **Mushroom** | 3 | 4 | 15 | 4 | ⭐⭐ | 💰💰💰 | 25-35 min |

---

## 1️⃣ Tobacco System

**Most Complex & Flexible**

### Overview
- **Strains:** 4 (Virginia, Burley, Oriental, Havana)
- **Steps:** 6 (Plant → Grow → Dry → Ferment → Package → Sell)
- **Quality System:** Poor → Good → Very Good → Legendary
- **Market Integration:** NPC negotiation system

### Production Chain

```
Seeds (4 types)
  ↓ Plant in pots (Terracotta/Ceramic/Iron/Golden)
Fresh Leaves
  ↓ 8 growth stages (~10-20 min)
Harvest
  ↓ Drying racks (5-10 min)
Dried Leaves
  ↓ Fermentation barrels (10-20 min)
Fermented Leaves
  ↓ Packaging tables (Small/Medium/Large)
Packaged Tobacco
  ↓ Sell to NPCs or players
Profit 💰
```

### Key Features
- **Quality Boosters:** Fertilizer, growth booster, quality booster
- **Pot Types:** Better pots = faster growth + higher quality
- **Grow Lights:** 3 tiers for accelerated growth
- **3 Processing Sizes:** Small/Medium/Big for batch processing

### Profitability
- **Poor Quality:** 10-20€/package
- **Good Quality:** 30-50€/package
- **Very Good:** 75-100€/package
- **Legendary:** 150-300€/package

[📖 Full Tobacco Guide →](production/Tobacco-System.md)

---

## 2️⃣ Cannabis System

**Popular & Profitable**

### Overview
- **Strains:** 4 (Indica, Sativa, Hybrid, Autoflower)
- **Steps:** 8 (Plant → Grow → Harvest → Dry → Trim → Cure → Hash/Oil)
- **By-Products:** Trim (for hash), Hash, Oil
- **Special:** Multiple end products from one plant

### Production Chain

```
Seeds
  ↓ Plant & grow (8 stages, 12-20 min)
Fresh Buds
  ↓ Trocknungsnetz (5 min)
Dried Buds
  ↓ Trimm Station (3 min) → Produces TRIM
Trimmed Buds
  ↓ Curing Glas (10 min)
Cured Buds
  ↓ Hash Presse OR Öl Extraktor
Hash (4 trim = 1 hash) OR Oil (3 buds = 1 oil, 3x potency)
```

### Key Features
- **Multiple Products:** Buds, hash, oil from same plant
- **Trim Usage:** Trimming by-product makes hash
- **Oil Extraction:** 3x concentrated product
- **Strain Differences:**
  - Indica: Faster growth, bushier
  - Sativa: Slower, taller, premium quality
  - Hybrid: Balanced
  - Autoflower: Fastest (6 stages)

### Profitability
- **Cured Buds:** 50-100€/package
- **Hash:** 100-150€/unit
- **Oil:** 200-400€/unit (concentrated)

[📖 Full Cannabis Guide →](production/Cannabis-System.md)

---

## 3️⃣ Coca/Cocaine System

**Chemical Processing**

### Overview
- **Strains:** 2 (Bolivianisch, Kolumbianisch)
- **Steps:** 5 (Plant → Harvest → Extract → Refine → Cook Crack)
- **Chemicals:** Requires diesel for extraction
- **Refineries:** Glowing light effect when active

### Production Chain

```
Coca Seeds
  ↓ Plant & grow (15-18 min)
Fresh Coca Leaves
  ↓ Extraction vats + diesel (8 min)
Coca Paste (brown)
  ↓ Refineries (15 min, glowing!)
Cocaine (white, 70-95% purity)
  ↓ Crack Kocher + backpulver (5 min)
Crack Rocks (crystallized)
```

### Vat & Refinery Sizes

| Size | Leaves | Time | Output | Purity |
|------|--------|------|--------|--------|
| Small | 10 | 5 min | 2 paste | 70-85% |
| Medium | 20 | 8 min | 5 paste | 80-92% |
| Big | 40 | 12 min | 12 paste | 90-95% |

### Key Features
- **Diesel Required:** 1 canister per extraction batch
- **Purity System:** Higher purity = higher value
- **Crack Conversion:** 1 cocaine + 1 baking soda = 2 crack rocks
- **Light Effects:** Refineries glow when processing

### Profitability
- **Coca Paste:** 20-30€/unit
- **Cocaine (90%+):** 200-350€/unit
- **Crack:** 150-250€/unit

[📖 Full Coca Guide →](production/Coca-System.md)

---

## 4️⃣ Poppy/Opium System

**Highest Profitability**

### Overview
- **Strains:** 3 (Afghanisch, Türkisch, Indisch)
- **Steps:** 6 (Plant → Score → Collect → Press → Cook → Refine)
- **Tools:** Scoring knife or Ritzmaschine
- **Final Product:** Heroin (80-99% purity)

### Production Chain

```
Poppy Seeds
  ↓ Plant & grow (12-18 min)
Poppy Pods
  ↓ Ritzmaschine (3 min)
Raw Opium (dark brown)
  ↓ Opium Presse (5 min)
Pressed Opium
  ↓ Kochstation (10 min, 185°C)
Morphine (60-85% purity)
  ↓ Heroin Raffinerie (20 min)
Heroin (80-99% purity) 💰💰💰💰💰
```

### Key Features
- **Automated Scoring:** Ritzmaschine vs manual knife
- **Long Process:** Total ~50-70 minutes
- **High Value:** Best profit per time invested
- **Premium Strains:** Indisch poppy = highest quality

### Profitability
- **Raw Opium:** 30-50€/unit
- **Morphine:** 100-200€/unit
- **Heroin (95%+):** 500-1000€/unit 💰

[📖 Full Poppy Guide →](production/Poppy-System.md)

---

## 5️⃣ Methamphetamine System

**DANGEROUS - High Risk, High Reward**

### Overview
- **Strains:** None (chemical synthesis)
- **Steps:** 4 (Mix → Reduce → Crystallize → Dry)
- **⚠️ WARNING:** Reduktionskessel can EXPLODE!
- **Chemicals:** Ephedrin, Pseudoephedrin, Red Phosphorus, Iodine

### Production Chain

```
Ephedrin/Pseudoephedrin + Chemicals
  ↓ Chemie Mixer (5 min)
Meth Paste
  ↓ Reduktionskessel (10 min) ⚠️ EXPLOSION RISK!
Raw Meth
  ↓ Kristallisator (15 min)
Crystal Meth
  ↓ Vakuum Trockner (8 min)
Meth (95-99% purity)
```

### Key Features
- **Explosion Risk:** Reduktionskessel requires careful handling
- **High Purity:** 95-99% final product
- **Chemical Procurement:** Regulated chemicals
- **Glowing Effect:** Reduktionskessel glows (light level 4)

### Safety Tips
⚠️ Don't interrupt Reduktionskessel mid-process
⚠️ Ensure stable power supply
⚠️ Keep fire extinguishers nearby
⚠️ Police raids increase with production volume

### Profitability
- **Meth Paste:** 50-75€/unit
- **Crystal Meth:** 200-300€/unit
- **Pure Meth (99%):** 600-900€/unit

[📖 Full Meth Guide →](production/Meth-System.md)

---

## 6️⃣ LSD System

**Most Scientific - Precision Required**

### Overview
- **Strains:** None (laboratory synthesis)
- **Steps:** 6 (Ferment → Distill → Synthesize → Dose → Perforate → Package)
- **GUI System:** Mikro Dosierer has dosing control
- **Precision:** ±5µg tolerance

### Production Chain

```
Mutterkorn (Ergot)
  ↓ Fermentations Tank (12 min, 25°C)
Ergot Culture
  ↓ Destillations Apparat (20 min, glowing!)
Lysergsäure (Lysergic Acid)
  ↓ Chemical synthesis (25 min)
LSD Lösung (100µg/ml)
  ↓ Mikro Dosierer (5 min, GUI)
Soaked Blotter Paper
  ↓ Perforations Presse (3 min)
LSD Blotter (100 tabs per sheet)
```

### Key Features
- **Precision Lab Equipment:** Requires exact measurements
- **Micro-Dosing GUI:** Control dosage per tab
- **Batch Production:** 100 tabs per sheet
- **Long Process:** 60-80 minutes total
- **Glowing Distillation:** Light effect during distillation

### Profitability
- **Lysergsäure:** 100-200€/unit
- **LSD Solution:** 300-500€/unit
- **LSD Blotter:** 10-20€/tab → 1,000-2,000€/sheet

[📖 Full LSD Guide →](production/LSD-System.md)

---

## 7️⃣ MDMA/Ecstasy System

**Fun Minigame - Arcade Style**

### Overview
- **Strains:** None (chemical synthesis)
- **Steps:** 4 (Extract → React → Dry → Press)
- **⭐ MINIGAME:** Timing-based pill pressing!
- **Customization:** Custom pill colors

### Production Chain

```
Safrol
  ↓ Reaktions Kessel (10 min, glowing!)
MDMA Base
  ↓ Trocknungs Ofen (15 min, hot!)
MDMA Kristall (80-95% purity)
  ↓ Pillen Presse (TIMING MINIGAME) + Binder + Dye
Ecstasy Pills (customizable colors)
```

### Pill Press Minigame
- **Timing-Based:** Hit the sweet spot for perfect pills
- **Success Rates:**
  - Perfect: 100% yield, max quality
  - Good: 80% yield
  - Fair: 50% yield
  - Miss: 0% yield, materials lost
- **Skill Curve:** Practice makes perfect!

### Key Features
- **Interactive:** Only production with minigame
- **Customization:** Different pill colors (dye)
- **Fast:** 30-40 minutes total
- **Fun:** Arcade-style gameplay

### Profitability
- **MDMA Kristall:** 100-200€/unit
- **Ecstasy Pills:** 10-30€/pill
- **Perfect Pills:** +50% value bonus

[📖 Full MDMA Guide →](production/MDMA-System.md)

---

## 8️⃣ Mushroom System

**Easiest - Beginner Friendly**

### Overview
- **Strains:** 3 (Cubensis, Azurescens, Mexicana)
- **Steps:** 4 (Inoculate → Grow → Harvest → Dry)
- **Environment:** Climate-controlled (lamps + water)
- **Fastest:** 25-35 minutes total

### Production Chain

```
Spore Syringe (3 strains)
  ↓ Inoculate Mist Bag (Small/Medium/Large)
Growing Mushrooms
  ↓ Climate Lamp + Wassertank (8-15 min)
Fresh Mushrooms
  ↓ Natural drying (5-7 min)
Dried Mushrooms (final product)
```

### Mist Bag Sizes

| Size | Capacity | Yield | Growth Time |
|------|----------|-------|-------------|
| Small | 1 syringe | 3 mushrooms | 8 min |
| Medium | 2 syringes | 6 mushrooms | 10 min |
| Large | 3 syringes | 9 mushrooms | 15 min |

### Key Features
- **Simple Process:** Only 4 steps
- **Climate Control:** Requires Klimalampe + Wassertank
- **Multiple Strains:**
  - Cubensis: Standard potency
  - Azurescens: High potency
  - Mexicana: Premium
- **Fast Turnaround:** Good for beginners

### Profitability
- **Fresh Mushrooms:** 20-30€/unit
- **Dried Cubensis:** 50-80€/unit
- **Dried Mexicana:** 120-200€/unit

[📖 Full Mushroom Guide →](production/Mushroom-System.md)

---

## 💰 Profitability Ranking

1. **Poppy/Heroin** - 💰💰💰💰💰 (500-1,000€/unit, 60min)
2. **LSD** - 💰💰💰💰💰 (1,000-2,000€/sheet, 70min)
3. **Meth** - 💰💰💰💰 (600-900€/unit, 40min)
4. **Cocaine** - 💰💰💰💰 (200-350€/unit, 50min)
5. **Cannabis Oil** - 💰💰💰💰 (200-400€/unit, 40min)
6. **MDMA** - 💰💰💰 (200-400€/batch, 35min)
7. **Tobacco** - 💰💰💰 (150-300€/legendary, 60min)
8. **Mushroom** - 💰💰💰 (120-200€/unit, 30min)

---

## 🎯 Recommended Path

### Beginner (Week 1)
1. **Start:** Mushroom (easiest, fast)
2. **Then:** Tobacco (learn quality system)
3. **Goal:** 50,000€ capital

### Intermediate (Week 2-3)
1. **Expand:** Cannabis (multiple products)
2. **Try:** MDMA (fun minigame)
3. **Goal:** 200,000€ capital + NPC shop

### Advanced (Week 4+)
1. **High-Value:** Poppy or LSD (best profit)
2. **Risky:** Meth (high reward, explosion risk)
3. **Goal:** Drug empire with warehouses

---

## 🔧 Universal Tips

### Quality Optimization
- **Golden Pots:** Always use for best quality
- **Quality Boosters:** Apply early in growth
- **Climate Control:** Maintain optimal conditions
- **Processing Time:** Longer = better quality

### Efficiency
- **Batch Processing:** Use bigger machines
- **Parallel Production:** Run multiple chains
- **Warehouses:** Auto-sell via NPC shops

### Safety
- **Police:** High production = higher wanted risk
- **Hide Cash:** Don't carry large amounts
- **Plots:** Secure your production facilities
- **Backups:** Multiple production locations

---

## 📊 Resource Requirements

### Starting Capital
- **Mushroom:** 5,000€ (mist bags, lamps, spores)
- **Tobacco:** 10,000€ (pots, racks, barrels)
- **Cannabis:** 15,000€ (processing equipment)
- **Others:** 20,000-50,000€ (specialized equipment)

### Time Investment
- **Part-Time:** 1-2 hours/day → Mushroom or Tobacco
- **Casual:** 3-4 hours/day → Cannabis or MDMA
- **Dedicated:** 5+ hours/day → Poppy, LSD, or Meth

---

## 🏆 Achievements

### Production Milestones
- First Harvest
- 100 Units Produced
- Legendary Quality Item
- 1 Million in Sales
- Master Producer (all 8 systems)

---

[⬆ Back to Wiki Home](Home.md)

## Detailed System Guides

- [Tobacco System →](production/Tobacco-System.md)
- [Cannabis System →](production/Cannabis-System.md)
- [Coca System →](production/Coca-System.md)
- [Poppy System →](production/Poppy-System.md)
- [Meth System →](production/Meth-System.md)
- [LSD System →](production/LSD-System.md)
- [MDMA System →](production/MDMA-System.md)
- [Mushroom System →](production/Mushroom-System.md)
