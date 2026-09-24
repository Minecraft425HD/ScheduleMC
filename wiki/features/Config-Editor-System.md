# Config Editor System

<div align="center">

**In-Game GUI Config Editor**

17 client-side screens for editing server/client settings without touching TOML files by hand

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Opening the Config Editor](#opening-the-config-editor)
3. [Underlying Config System](#underlying-config-system)
4. [Client vs. Server / Permissions](#client-vs-server--permissions)
5. [Screen Reference](#screen-reference)
6. [UI Mechanics](#ui-mechanics)
7. [Known Limitations](#known-limitations)

---

## Overview

ScheduleMC ships an in-game **Config Editor**: 17 GUI screens under
`de.rolandsw.schedulemc.client.gui.config` (plus one entry-point stub one
package up) that let a player edit almost every tunable value in the mod —
economy balances, police behavior, plot rules, production speeds, and more —
without opening a text editor.

This is **not** a slash command or a keybind-triggered overlay. It is wired
into Forge's standard **mod config screen extension point**, so it appears
exactly where any Forge mod's config button would.

---

## Opening the Config Editor

There is no `/config` command and no dedicated keybind. Access is entirely
through the vanilla **Mods list**:

```
Esc → Mods → ScheduleMC → Config
```

This registers in `ScheduleMC.java` via:

```java
context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
    () -> new ConfigScreenFactory((mc, screen) -> new ConfigScreen(screen)));
```

`ConfigScreen` itself is a thin pass-through stub with no logic of its own —
it exists only to satisfy Forge's factory signature — and immediately opens
`ConfigCategoryScreen`, the real hub screen with a scrollable, two-column
list of 15 category buttons.

---

## Underlying Config System

Every screen reads and writes through `de.rolandsw.schedulemc.config.ModConfigHandler`,
which owns two `ForgeConfigSpec` instances:

| Spec | Registered as | Backs |
|---|---|---|
| `SPEC` | `ModConfig.Type.COMMON` | `ModConfigHandler.COMMON`, `TOBACCO`, `VEHICLE_SERVER` — the shared/server-authoritative config |
| `CLIENT_SPEC` | `ModConfig.Type.CLIENT` | `VEHICLE_CLIENT` only — used solely by `ClientConfigScreen` |

**Persistence:** almost every widget calls `configValue.set(...)` followed
immediately by `SPEC.save()` (or `CLIENT_SPEC.save()`) — changes write to
the TOML file on disk instantly, with **no explicit "Apply" step and no
server restart required** for most values, since Forge config values are
live-reloadable.

Two screens are the exception and use an explicit **Save / Reset / Back**
button pattern instead of auto-save: `EconomyPricesConfigScreen` and
`ProductionBlockCatalogScreen`. Both additionally send a network packet
(`ReloadEconomyPricesPacket` / `ReloadBlockCatalogPacket` via `ModNetworking`)
to the server after saving so server-side price/catalog caches pick up the
new values immediately, without a restart.

---

## Client vs. Server / Permissions

All 17 screens are `@OnlyIn(Dist.CLIENT)` pure GUI code, but most of them
write to the **COMMON** spec — the same config the server uses.

- **Singleplayer:** editing here directly changes the live world config.
- **Multiplayer:** a client can open the GUI and locally overwrite their own
  copy of the COMMON config file, but Forge re-syncs the server's
  authoritative COMMON values to the client on join, so an unprivileged
  player's local edits to gameplay-affecting COMMON values are effectively
  overwritten again on reconnect.

**There is no in-code permission/OP check anywhere in this package** — no
`hasPermissions` call, no permission-level gate. Only `ClientConfigScreen`'s
vehicle/debug options (backed by `CLIENT_SPEC`) are genuinely per-player.
Server admins should not assume the Mods-menu Config button is safe to leave
reachable for untrusted players on a server install; access control here
relies entirely on controlling who can install/see the mod's config screen,
not on any in-mod permission system.

---

## Screen Reference

| Screen | Class | What it configures |
|---|---|---|
| Entry point | `client/gui/ConfigScreen.java` | Pass-through stub, opens the category hub |
| Category hub | `ConfigCategoryScreen` | Scrollable list of 15 category buttons |
| Client Settings | `ClientConfigScreen` | Vehicle third-person toggle, °F/°C, vehicle volume/zoom sliders, debug logging (writes `CLIENT_SPEC`) |
| Economy Settings | `EconomyConfigScreen` | Start balance, save interval, daily rewards/streak bonus, savings accounts, overdraft rate, recurring payment cap, property/sales tax, shop multipliers (16 options) |
| Advanced Economy | `AdvancedEconomyConfigScreen` | Rent, shop enable, ratings, bank limits, stock market base prices/volatility, economy cycle, level/XP, per-drug risk premium, anti-exploit sell limits/cooldowns (34 options) |
| Dynamic Pricing (UDPS) | `DynamicPricingConfigScreen` | Unified Dynamic Pricing System toggle, SD factor/decay, min/max multiplier, update interval, daily food cost, reference income, and a Serene-Seasons-vs-internal-calendar toggle for [seasonal market pricing](Market-System.md#seasonal-pricing) (9 options) |
| Produkt-Referenzpreise | `EconomyPricesConfigScreen` | Per-unit €-price for ~60 individual products across every production chain; explicit Save/Reset/Back |
| Produktionsblock-Katalog | `ProductionBlockCatalogScreen` | Per-block price + required player level for ~100 production machine blocks; explicit Save/Reset/Back |
| Plot Settings | `PlotConfigScreen` | Min/max plot size & price, max trusted players, refund-on-abandon %, transfer allow/deny; links to Block Restrictions |
| Plot Block Restrictions | `PlotBlockRestrictionConfigScreen` | Allowed-block-ID lists per plot type (8 plot types), TAB autocomplete against the block registry |
| Police Settings | `PoliceConfigScreen` | Arrest, search/pursuit, raid/room-scanning, station/patrol, vehicle pursuit, roadblocks, traffic violations, warnings (36 options) |
| NPC & Navigation | `NPCConfigScreen` | Pathfinding scan radius, path update interval, arrival distance; links to Block Lists |
| NPC Block Lists | `NPCBlockListConfigScreen` | Walkable-block and road-block ID lists, TAB autocomplete |
| Utility Consumer Blocks | `UtilityBlockListConfigScreen` | Block IDs that bill electricity/water, autocomplete from `UtilityRegistry` |
| Warehouse Settings | `WarehouseConfigScreen` | Slot count, max capacity/slot, delivery interval, default delivery price |
| Workshop Settings | `WorkshopConfigScreen` | Vehicle repair/upgrade prices: inspection fee, repair/battery cost, oil change, upgrade tiers, tire, paint (12 options) |
| Tobacco Settings | `TobaccoConfigScreen` | Growth speed, drying/fermenting time, quality chance, rack/barrel capacities, pot capacity, fertilizer effects, grow-light tiers (31 options) |
| Stealing/Crime Settings | `StealingConfigScreen` | Vehicle-stealing minigame: indicator speed, max attempts, min/max zone size (4 options) |

---

## UI Mechanics

- **Scrollable lists** (`ContainerObjectSelectionList`) with section headers for the larger screens (hub, Advanced Economy, Economy, Police, Tobacco).
- **Sliders** — custom `IntSlider`/`DoubleSlider`/`LongSlider` widgets with live-updating printf-style labels (e.g. `%.0f€`, `%d ticks`).
- **Toggle buttons** (`BoolButton`) with colored ON/OFF state.
- **Free-text EditBoxes with TAB-autocomplete** against `ForgeRegistries.BLOCKS` or `UtilityRegistry` for comma-separated block-ID list fields.
- **Save / Reset-to-default / Back** buttons only on the two catalog screens; every other screen just has a "« Back" button since changes save instantly.
- No tabs, no dropdown widgets, and no tooltips are implemented anywhere in this package.

---

## Known Limitations

- No permission gating — see [Client vs. Server / Permissions](#client-vs-server--permissions).
- Heavy code duplication: the slider/toggle/row widget inner classes are copy-pasted near-identically across `AdvancedEconomyConfigScreen`, `EconomyConfigScreen`, `PoliceConfigScreen`, and `TobaccoConfigScreen` rather than shared.
- No search/filter across the ~250 total settings; navigation is purely by category button.

---

<div align="center">

**Config Editor System - Complete Guide**

For related systems:
- [Economy System](Economy-System.md)
- [Plot System](Plot-System.md)
- [Police and Crime System](Police-Crime-System.md)

[Back to Wiki Home](../Home.md) | [All Commands](../Commands.md)

**Last Updated:** 2026-09-24 | **ScheduleMC v3.9.0-beta**

</div>

---

## Dokumentationsstatus

- Neu erstellt am **2026-09-24** im Rahmen der Dokumentationsvervollständigung (bisher undokumentiertes System).
- Referenz für Live-Metriken: `docs/REPO_METRICS.md`.
