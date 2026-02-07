# Meth Production System

The most dangerous production system in ScheduleMC. A 4-step lab process with temperature-controlled reduction that can explode, destroying your equipment and killing nearby players.

**Stats at a Glance:**

| Attribute | Value |
|-----------|-------|
| Strains | N/A (chemical synthesis) |
| Production Steps | 4 |
| Total Items | 8 |
| Total Blocks | 4 |
| Quality Tiers | Standard (White), Gut (Yellow), Blue Sky (96-99%) |
| Danger | EXTREME -- Explosion risk at Step 2 |

---

## Overview

Meth production is a pure chemistry system with no farming stage. Players combine precursor chemicals in a mixer, carefully manage temperature during a dangerous reduction process, crystallize the output, and vacuum-dry the final product. The Reduktionskessel (reduction kettle) is the heart of the system -- and its greatest risk. If temperature exceeds 150 degrees, the kettle explodes with TNT-equivalent force, destroying itself and damaging everything nearby.

**WARNING:** The reduction step requires constant player monitoring. Do not AFK. Do not multitask. One moment of inattention above 150 degrees means an explosion.

---

## Chemicals

### Precursors (Choose One)

| Precursor | Price | Quality Bonus | Notes |
|-----------|-------|--------------|-------|
| **Ephedrin** | ~100 per unit | 25% chance for Gut | Standard option |
| **Pseudoephedrin** | ~150 per unit | **35% chance for Gut** | Premium option (+40% better odds) |

### Catalysts (Both Required)

| Chemical | Price | Description |
|----------|-------|-------------|
| **Roter Phosphor** (Red Phosphorus) | ~50 per unit | Highly flammable catalyst |
| **Jod** (Iodine) | ~50 per unit | Essential catalyst; triggers mixing when added |

### Recipe per Batch

```
1 Ephedrin/Pseudoephedrin + 1 Roter Phosphor + 1 Jod = 1 Meth-Paste

Standard batch cost:  200 (Ephedrin path)
Premium batch cost:   250 (Pseudoephedrin path)
```

---

## Production Chain

```
  [1. MIXING]               Chemie Mixer
   Ephedrin/Pseudo    -->    4 parallel slots
   + Roter Phosphor          30 sec per batch
   + Jod (triggers)
       |
       v
   Meth-Paste
       |
       v
  [2. REDUCTION]             Reduktionskessel
   WARNING: CAN EXPLODE!     Temperature control
   Keep temp 80-120 C        20 sec process
   NEVER exceed 150 C!       GUI with real-time display
       |
       v
   Roh-Meth (Raw Meth)
       |
       v
  [3. CRYSTALLIZATION]       Kristallisator
   Passive process            4 parallel slots
   15% quality upgrade        40 sec per batch
       |
       v
   Kristall-Meth (Wet Crystals)
       |
       v
  [4. DRYING]                Vakuum Trockner
   Passive vacuum dry         6 parallel slots
   Quality preserved          30 sec per batch
       |
       v
   Final Meth Product
```

---

## Step 1: Mixing (Chemie Mixer)

Combine precursors into meth paste. Add ingredients in order: precursor first, then Roter Phosphor, then Jod (which triggers the mixing process).

| Property | Value |
|----------|-------|
| Capacity | 4 batches simultaneously |
| Processing Time | 600 ticks (30 seconds) per batch |
| Quality | 25% Gut with Ephedrin; 35% Gut with Pseudoephedrin |

---

## Step 2: Reduction (Reduktionskessel) -- DANGER

The most critical and dangerous step. Heat meth paste under controlled temperature to produce raw meth.

### Temperature Zones

| Range | Temperature | Status | Effect |
|-------|-------------|--------|--------|
| **Cold** | Below 80 C | ZU KALT (Too Cold) | No progress; process paused |
| **Optimal** | 80-120 C | OPTIMAL | Best quality; safe |
| **Danger** | 121-150 C | GEFAHR (Danger) | Quality degrades if >30% of time spent here |
| **Critical** | 151+ C | KRITISCH | **EXPLOSION -- Immediate** |

### Temperature Mechanics

```
Heating rate (heater ON):     +1.5 C per tick
Process heat (during reaction): +0.3 C per tick
Combined heating:              +1.8 C per tick

Cooling rate (heater OFF):    -0.8 C per tick
Starting temperature:          20 C (room temp)
```

### GUI Interface

The Reduktionskessel has a real-time GUI showing:
- Current temperature with color coding (blue/green/orange/red)
- Progress percentage
- Optimal time percentage
- Danger time percentage
- Heater ON/OFF toggle
- Predicted quality

### Quality Calculation

```
If danger time > 30%:        Quality DOWNGRADED
If optimal time >= 90%:      30% chance for BLUE SKY
If optimal time 70-89%:      50% chance to upgrade
If optimal time < 70%:       No improvement
```

### Explosion Effects

When temperature hits 151 C:
- Explosion power: 4 (TNT-equivalent)
- Reduktionskessel is **destroyed**
- Player damage: 10.0 hearts within 8 blocks
- All meth paste in the kettle is lost
- Nearby blocks may be destroyed

---

## Step 3: Crystallization (Kristallisator)

Passive crystallization of raw meth into wet crystal form.

| Property | Value |
|----------|-------|
| Capacity | 4 batches simultaneously |
| Processing Time | 800 ticks (40 seconds) per batch |
| Quality Upgrade | 15% chance to upgrade +1 tier |

The 15% upgrade chance means Standard can become Gut, and Gut can become Blue Sky during this step.

---

## Step 4: Drying (Vakuum Trockner)

Final vacuum drying to remove moisture from wet crystals.

| Property | Value |
|----------|-------|
| Capacity | 6 batches simultaneously |
| Processing Time | 600 ticks (30 seconds) per batch |
| Quality | Preserved (no change) |

---

## Quality Tiers

| Quality | Name | Color | Purity | Price Multiplier |
|---------|------|-------|--------|-----------------|
| **Standard** | Crystal Meth (White) | White | 70-79% | 1.0x |
| **Gut** | Premium Crystal (Yellow) | Yellow | 80-89% | 2.0x |
| **Blue Sky** | Blue Sky | Bold Cyan | **96-99%** | **5.0x** |

Blue Sky is the legendary tier. Achieving it requires Pseudoephedrin, near-perfect temperature control (90%+ optimal time), and some luck.

---

## Items Table (8 Items)

| Item | Description | How Obtained |
|------|-------------|-------------|
| Ephedrin | Standard precursor (white powder) | NPC shops |
| Pseudoephedrin | Premium precursor (+10% quality) | NPC shops |
| Roter Phosphor | Red phosphorus catalyst | NPC shops |
| Jod | Iodine catalyst | NPC shops |
| Meth-Paste | Mixed precursor paste | Chemie Mixer |
| Roh-Meth | Raw methamphetamine | Reduktionskessel |
| Kristall-Meth | Wet crystal meth | Kristallisator |
| Meth (Final) | Dried final product | Vakuum Trockner |

---

## Blocks Table (4 Blocks)

| Block | Description | Function |
|-------|-------------|----------|
| Chemie Mixer | Chemical mixing station | Combines precursors into meth paste (4 slots, 30 sec) |
| Reduktionskessel | Reduction kettle (light level 4, glows) | Temperature-controlled reduction; CAN EXPLODE at 151+ C |
| Kristallisator | Crystallization chamber (glass) | Passive crystallization (4 slots, 40 sec); 15% quality upgrade |
| Vakuum Trockner | Vacuum dryer | Final drying step (6 slots, 30 sec); quality preserved |

---

## Quality Modifiers

| Modifier | Stage | Effect |
|----------|-------|--------|
| Pseudoephedrin | Mixing | 35% Gut chance (vs 25% with Ephedrin) |
| Temperature Control | Reduction | 90%+ optimal time = 30% Blue Sky chance |
| Danger Time | Reduction | >30% danger time = quality downgrade |
| Crystallization RNG | Crystallization | 15% chance for +1 quality tier |
| Combined Path | All | Pseudo + perfect temp + crystal luck = ~50-60% Blue Sky rate |

---

## Tips & Tricks

1. **This is NOT a beginner system.** Practice temperature control in creative mode or with cheap Ephedrin batches before risking anything valuable.
2. **Turn the heater OFF at 115 C, not 120 C.** Process heat (+0.3/tick) will carry the temperature up. Overshooting 120 puts you in the danger zone.
3. **Never leave the heater ON continuously.** Use short pulses. Heat to 115, turn off, let it cool to 100, pulse again.
4. **Build an explosion-proof lab.** Obsidian walls, 5x5x3 minimum. Keep other equipment and storage outside the blast radius.
5. **Always use Pseudoephedrin for premium production.** The +10% Gut chance compounds through the pipeline, significantly increasing Blue Sky odds.
6. **The Kristallisator is your second chance.** Even if reduction only produces Gut, the 15% crystallization upgrade can push it to Blue Sky.
7. **Watch for server lag before starting reduction.** Delayed heater response on a laggy server can cause unexpected temperature spikes.
8. **Keep backup armor and healing nearby.** If an explosion happens, you need to survive and recover quickly.
9. **The total cycle is only about 2 minutes.** Meth has the fastest production cycle of any system -- high risk but extremely high output per minute.
10. **"Say my name."** Blue Sky at 96-99% purity is the rarest and most valuable product in the game. Master the temperature and earn the title of Heisenberg.

---

*See also: [Coca System](Coca-System.md) | [LSD System](LSD-System.md) | [Production Systems Overview](../Production-Systems.md)*
