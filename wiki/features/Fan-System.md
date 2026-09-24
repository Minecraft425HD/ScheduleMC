# Fan / Multiblock Booster System

<div align="center">

**Directional Speed Boosters for Drying Racks**

A small, fully-wired proximity-scan system that speeds up Tobacco and Cannabis drying

[Back to Wiki Home](../Home.md) | [Tobacco System](../production/Tobacco-System.md) | [Cannabis System](../production/Cannabis-System.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Fan Tiers & Blocks](#fan-tiers--blocks)
3. [How Boosting Works](#how-boosting-works)
4. [Multiblock Detection](#multiblock-detection)
5. [Interaction With Production](#interaction-with-production)
6. [Notes](#notes)

---

## Overview

A **Fan** is a placeable, directional block that acts as a passive speed
booster for nearby drying racks. Despite its "Ventilator"/air-flow flavor
text, there is **no smell, ventilation, or air-quality mechanic** anywhere
in the code — ScheduleMC has no grow-room-enclosure requirement. The fan's
only real effect is a tick-rate multiplier applied to a compatible receiver
block that is facing it within range and in line of sight.

The only receiver currently implemented is `AbstractDryingRackBlockEntity`
(`de.rolandsw.schedulemc.tobacco.blockentity`), which is shared by **both**
the Tobacco and Cannabis production chains — so fans speed up drying for
both Dried Tobacco Leaf and Dried Cannabis Bud production. No other
production block (fermentation barrels, curing jars, presses, etc.)
consumes a boost.

---

## Fan Tiers & Blocks

| Registry ID | Class | Height | Boost | Notes |
|---|---|---|---|---|
| `schedulemc:fan_tier1` | `FanBlock` | 1 block | +50% (1.5x) | Iron, strength 2.0 |
| `schedulemc:fan_tier2` | `TallFanBlock` | 2 blocks | +150% (2.5x) | Door-like paired lower/upper halves |
| `schedulemc:fan_tier3` | `TallFanBlock` | 2 blocks | +300% (4.0x) | "Industrial", strength 3.5 |

Each has a matching `BlockItem` of the same registry name, with blockstates,
models, loot tables, and lang entries. All three are registered via
`fan/blocks/FanBlocks.java` (a `DeferredRegister`) and added to the
creative tab in `ModCreativeTabs.java`.

Fans have no GUI, no inventory, and no `BlockEntity` — the only in-game
feedback is an item tooltip showing tier, boost percentage, and range.

---

## How Boosting Works

Boost multipliers **stack additively** across all detected fans, then are
capped:

```
total = min(1.0 + Σ(tier.multiplier - 1.0), MAX_MULTIPLIER)
MAX_MULTIPLIER = 8.0  (MultiblockHelper.MAX_MULTIPLIER)
```

Two Tier 3 fans facing the same drying rack, for example, would sum to
`1.0 + 3.0 + 3.0 = 7.0x` (below the 8.0x cap).

Fans affect **drying speed only** — there is no quality bonus from fans.

---

## Multiblock Detection

Despite the "Multiblock" name, this is not a structural/room-shaped
multiblock system — it is a proximity + line-of-sight scan performed by the
static utility `multiblock/MultiblockHelper.java`:

1. From the drying rack's position (or footprint, for multi-tile racks), scan outward in the 4 horizontal cardinal directions up to `range` blocks (the drying rack calls with `range = 4`).
2. The path between the rack and a candidate fan must be **all air** — a solid non-booster block in the way blocks that direction entirely.
3. The block found must implement `multiblock/IMultiblockBooster` (`float getBoostMultiplier()`) and have its `FACING` property pointing back at the rack.
4. Each valid booster position counts once toward the additive stack.
5. Additionally gated by `PlotUtilityManager.areUtilitiesEnabled(...)` — a fan does not count if the plot's utilities are disabled (e.g. unpaid utility days).

The scan is cached and only re-run every `BOOST_SCAN_INTERVAL` ticks inside
`AbstractDryingRackBlockEntity`, not every tick.

---

## Interaction With Production

- **Tobacco:** speeds up drying of tobacco leaves in Small/Medium/Large Drying Racks.
- **Cannabis:** speeds up drying of Fresh Cannabis Buds → Dried Cannabis Buds in the same shared Drying Rack blocks (see [Cannabis System](../production/Cannabis-System.md#step-3-drying-tobacco-drying-rack)).
- No other production chain (Coca, Poppy, Meth, LSD, MDMA, Mushroom, or any legal chain) reads a fan boost.

---

## Notes

- Fully implemented and live — registered in `ScheduleMC.java`, resolved into the plot/utility system via `UtilityRegistry`, and actively consumed by `AbstractDryingRackBlockEntity.tick()`. Not a stub.
- The `IMultiblockBooster` interface is generic (any block could implement it), so this system is architecturally ready for future booster types, but Fans are currently the only implementation.

---

<div align="center">

**Fan / Multiblock Booster System - Complete Guide**

For related systems:
- [Tobacco System](../production/Tobacco-System.md)
- [Cannabis System](../production/Cannabis-System.md)
- [Utility System](Utility-System.md)

[Back to Wiki Home](../Home.md) | [All Commands](../Commands.md)

**Last Updated:** 2026-09-24 | **ScheduleMC v3.9.0-beta**

</div>

---

## Dokumentationsstatus

- Neu erstellt am **2026-09-24** im Rahmen der Dokumentationsvervollständigung (bisher undokumentiertes System).
- Referenz für Live-Metriken: `docs/REPO_METRICS.md`.
