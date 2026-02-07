# ScheduleMC Configuration Guide

Complete reference for all configurable aspects of the ScheduleMC Minecraft Forge 1.20.1 mod.

---

## Table of Contents

1. [Forge Configuration (schedulemc-common.toml)](#1-forge-configuration)
2. [Economy Configuration](#2-economy-configuration)
3. [Plot Configuration](#3-plot-configuration)
4. [NPC Configuration](#4-npc-configuration)
5. [Production Configuration](#5-production-configuration)
6. [Vehicle Configuration](#6-vehicle-configuration)
7. [Police and Crime Configuration](#7-police-and-crime-configuration)
8. [Warehouse Configuration](#8-warehouse-configuration)
9. [Market Configuration](#9-market-configuration)
10. [Smartphone Configuration](#10-smartphone-configuration)
11. [Performance Configuration](#11-performance-configuration)
12. [Data Files](#12-data-files)
13. [Delivery Price Configuration](#13-delivery-price-configuration)

---

## 1. Forge Configuration

ScheduleMC uses the Forge configuration system managed by `ModConfigHandler`. All server/common configuration values are stored in a single spec (`SPEC`), while client-specific values use a separate spec (`CLIENT_SPEC`).

**Source:** `de.rolandsw.schedulemc.config.ModConfigHandler`

### File Locations

| File | Side | Description |
|------|------|-------------|
| `config/schedulemc-common.toml` | Server/Common | All gameplay settings (economy, plots, police, etc.) |
| `config/schedulemc-client.toml` | Client | Vehicle rendering, sounds, temperature display |
| `config/mapview.properties` | Client | Minimap and world map display settings |

### Hot-Reload Support

The configuration supports Forge's hot-reload mechanism. When TOML files are edited while the server is running, Forge detects the changes and triggers `ModConfigEvent.Reloading`. The `ServerConfig` class extends `ConfigBase` and overrides `onReload()` to re-parse dynamic lists such as fuel types and drivable blocks. Additionally, the `ConfigCache` utility (`de.rolandsw.schedulemc.util.ConfigCache`) maintains an in-memory cache of frequently accessed config values (police detection radius, arrest cooldown, warehouse delivery interval, start balance) and exposes an `invalidate()` method that is called on config reload to force a refresh.

### Architecture

```
ModConfigHandler
  |-- COMMON (Common inner class)     --> economy, plots, police, warehouse, etc.
  |-- TOBACCO (TobaccoConfig)         --> tobacco/production grow lights, drying, fermentation
  |-- VEHICLE_SERVER (ServerConfig)   --> fuel stations, vehicle parts, towing, memberships
  |-- VEHICLE_CLIENT (ClientConfig)   --> volume, camera, temperature display
```

---

## 2. Economy Configuration

All economy values live under the `[economy]`, `[savings]`, `[overdraft]`, `[recurring]`, `[tax]`, `[bank]`, `[stock_market]`, `[dynamic_pricing]`, `[economy_cycle]`, `[level_system]`, `[risk_premium]`, and `[anti_exploit]` sections of `schedulemc-common.toml`.

### 2.1 Core Economy

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `economy.start_balance` | `1000.0` | 0 - 1,000,000 | Starting money for new player accounts (in currency units) |
| `economy.save_interval_minutes` | `5` | 1 - 60 | Auto-save interval in minutes |

### 2.2 Daily Rewards

Located under `[daily]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `daily.daily_reward` | `50.0` | 1 - 10,000 | Base reward per daily login |
| `daily.streak_bonus` | `10.0` | 0 - 1,000 | Additional bonus per consecutive streak day |
| `daily.max_streak` | `30` | 1 - 365 | Maximum streak days for bonus calculation |

**Formula:** `reward = daily_reward + (streak_bonus * min(current_streak, max_streak))`

At maximum streak (30 days): `50 + (10 * 30) = 350` per day.

### 2.3 Loan System

Loan tiers are defined in `Loan.LoanType` (`de.rolandsw.schedulemc.economy.Loan`):

| Tier | Amount | Interest Rate | Duration | Daily Payment |
|------|--------|---------------|----------|---------------|
| **SMALL** | 5,000 | 10% | 14 days (2 weeks) | ~392.86 |
| **MEDIUM** | 25,000 | 15% | 28 days (4 weeks) | ~1,026.79 |
| **LARGE** | 100,000 | 20% | 56 days (8 weeks) | ~2,142.86 |

**Requirements:**
- Minimum account balance of 1,000 to apply (`MIN_BALANCE_FOR_LOAN`)
- Minimum 7 days playtime (`MIN_PLAYTIME_DAYS`)
- Only one active loan per player at a time

**Repayment formula:** `totalWithInterest = principal * (1 + interestRate)`, then `dailyPayment = totalWithInterest / durationDays`.

### 2.4 Savings Accounts

Located under `[savings]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `savings.max_per_player` | `50,000.0` | 1,000 - 10,000,000 | Maximum savings deposits per player |
| `savings.min_deposit` | `1,000.0` | 100 - 100,000 | Minimum deposit to open a savings account |
| `savings.interest_rate` | `0.05` | 0.0 - 0.5 | Weekly interest rate (0.05 = 5%) |
| `savings.lock_period_weeks` | `4` | 1 - 52 | Lock-in period in weeks |
| `savings.early_withdrawal_penalty` | `0.10` | 0.0 - 0.5 | Penalty for early withdrawal (0.10 = 10%) |

### 2.5 Overdraft (Dispo)

Located under `[overdraft]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `overdraft.interest_rate` | `0.25` | 0.0 - 1.0 | Weekly overdraft interest rate (0.25 = 25%) |

**Overdraft mechanics:**
- Players can go into unlimited negative balance.
- Day 7: Automatic settlement attempts (cash and savings are used to offset the debt).
- Day 28: Prison sentence (1,000 per minute of jail time).

### 2.6 Recurring Payments

Located under `[recurring]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `recurring.max_per_player` | `10` | 1 - 100 | Maximum recurring payments per player |

### 2.7 Tax System

Located under `[tax]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `tax.property_per_chunk` | `100.0` | 0 - 10,000 | Property tax per owned chunk per month |
| `tax.sales_rate` | `0.19` | 0.0 - 1.0 | Sales tax / VAT rate (0.19 = 19%) |

### 2.8 Bank System

Located under `[bank]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `bank.deposit_limit` | `9,999.0` | 100 - 1,000,000 | Maximum deposit amount per transaction |
| `bank.transfer_daily_limit` | `999.0` | 10 - 100,000 | Maximum transfer amount per day |

### 2.9 Stock Market

Located under `[stock_market]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `stock_market.gold_base_price` | `250.0` | 10 - 10,000 | Base price for gold ingots |
| `stock_market.diamond_base_price` | `450.0` | 10 - 10,000 | Base price for diamonds |
| `stock_market.emerald_base_price` | `180.0` | 10 - 10,000 | Base price for emeralds |
| `stock_market.max_price_change_percent` | `0.10` | 0.01 - 0.50 | Maximum daily price change (0.10 = 10%) |

### 2.10 Hospital Fees

Hospital fees are set at runtime via the `/hospital` command (requires OP level 2):

| Command | Description |
|---------|-------------|
| `/hospital setfee <amount>` | Set the hospital bill amount (default: 500) |
| `/hospital setspawn` | Set hospital respawn position to current location |
| `/hospital info` | Display current hospital spawn and fee |

**Source:** `de.rolandsw.schedulemc.economy.events.RespawnHandler` -- `HOSPITAL_FEE` defaults to `500.0`.

### 2.11 Dynamic Pricing System (UDPS)

Located under `[dynamic_pricing]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `dynamic_pricing.enabled` | `true` | -- | Enable/disable dynamic pricing |
| `dynamic_pricing.sd_factor` | `0.3` | 0.0 - 1.0 | Supply/demand influence factor |
| `dynamic_pricing.min_multiplier` | `0.3` | 0.1 - 1.0 | Global minimum price multiplier |
| `dynamic_pricing.max_multiplier` | `5.0` | 1.0 - 20.0 | Global maximum price multiplier |
| `dynamic_pricing.update_interval_minutes` | `5` | 1 - 30 | Price recalculation interval in minutes |
| `dynamic_pricing.sd_decay_rate` | `0.02` | 0.001 - 0.1 | Supply/demand decay rate per update (0.02 = 2%) |
| `dynamic_pricing.daily_food_cost` | `20.0` | 5 - 200 | Expected daily food cost on Hard difficulty |
| `dynamic_pricing.daily_reference_income` | `150.0` | 50 - 1,000 | Reference daily income for price calibration |

### 2.12 Economic Cycle

The economy cycles through six phases: **Normal -> Boom -> Ueberhitzung (Overheating) -> Rezession (Recession) -> Depression -> Erholung (Recovery) -> Normal**.

Located under `[economy_cycle]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `economy_cycle.enabled` | `true` | -- | Enable economic cycle |
| `economy_cycle.min_duration_days` | `2` | 1 - 30 | Minimum phase duration in MC days |
| `economy_cycle.max_duration_days` | `10` | 2 - 60 | Maximum phase duration in MC days |
| `economy_cycle.event_base_chance` | `0.10` | 0.0 - 1.0 | Base chance for economic events per day (10%) |

**Phase Multipliers** (defined in `EconomyCyclePhase`):

| Phase | Sell Prices | Buy Prices | Salaries | Event Chance | Duration |
|-------|------------|------------|----------|-------------|----------|
| Normal | 1.00x | 1.00x | 1.00x | 10% | 5-10 days |
| Boom | 1.20x | 1.10x | 1.10x | 15% | 3-7 days |
| Overheating | 1.40x | 1.25x | 1.15x | 25% | 2-4 days |
| Recession | 0.80x | 0.90x | 0.90x | 15% | 3-6 days |
| Depression | 0.60x | 0.75x | 0.80x | 20% | 2-5 days |
| Recovery | 0.90x | 0.95x | 0.95x | 10% | 3-6 days |

### 2.13 Producer Level System

Located under `[level_system]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `level_system.enabled` | `true` | -- | Enable producer level system |
| `level_system.max_level` | `30` | 10 - 100 | Maximum producer level |
| `level_system.base_xp` | `100` | 10 - 10,000 | Base XP required for level 1 |
| `level_system.xp_exponent` | `1.8` | 1.0 - 3.0 | Exponent for XP curve (higher = steeper) |
| `level_system.illegal_xp_multiplier` | `1.5` | 0.5 - 5.0 | XP multiplier for illegal sales |
| `level_system.legal_xp_multiplier` | `1.0` | 0.5 - 5.0 | XP multiplier for legal sales |

**XP Formula:** `xp_for_level = base_xp * (level ^ xp_exponent)`

### 2.14 Risk Premium

Located under `[risk_premium]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `risk_premium.base_cannabis` | `1.15` | 1.0 - 3.0 | Risk multiplier for cannabis (15% markup) |
| `risk_premium.base_cocaine` | `1.40` | 1.0 - 3.0 | Risk multiplier for cocaine (40% markup) |
| `risk_premium.base_heroin` | `1.50` | 1.0 - 3.0 | Risk multiplier for heroin (50% markup) |
| `risk_premium.base_meth` | `1.45` | 1.0 - 3.0 | Risk multiplier for methamphetamine (45% markup) |
| `risk_premium.confiscation_multiplier` | `1.25` | 1.0 - 3.0 | Risk surcharge for illegal machines (25% markup) |

### 2.15 Anti-Exploit

Located under `[anti_exploit]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `anti_exploit.daily_sell_limit` | `5,000.0` | 0 - 1,000,000 | Maximum daily revenue per player (0 = unlimited) |
| `anti_exploit.mass_sell_cooldown_seconds` | `30` | 5 - 300 | Cooldown after a mass sale |
| `anti_exploit.mass_sell_threshold` | `64` | 10 - 1,000 | Items per sale to trigger mass-sell detection |
| `anti_exploit.mass_sell_penalty` | `0.80` | 0.1 - 1.0 | Price reduction on mass sale (0.8 = 20% less) |

---

## 3. Plot Configuration

Located under `[plots]` and `[rent]` in the common config.

### 3.1 Plot Size and Pricing

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `plots.min_plot_size` | `64` | 1 - 1,000,000 | Minimum plot size in blocks |
| `plots.max_plot_size` | `1,000,000` | 1 - 100,000,000 | Maximum plot size in blocks |
| `plots.min_plot_price` | `1.0` | 0.01 - 1,000,000 | Minimum plot price |
| `plots.max_plot_price` | `1,000,000.0` | 1 - 100,000,000 | Maximum plot price |
| `plots.max_trusted_players` | `10` | 1 - 100 | Maximum trusted players per plot |
| `plots.allow_plot_transfer` | `true` | -- | Whether plots can be transferred between players |
| `plots.refund_on_abandon` | `0.5` | 0.0 - 1.0 | Refund percentage when abandoning (0.5 = 50%) |

### 3.2 Plot Types

Defined in `PlotType` (`de.rolandsw.schedulemc.region.PlotType`):

| Type | Purchasable | Rentable | Description |
|------|-------------|----------|-------------|
| `RESIDENTIAL` | Yes | Yes | Player housing |
| `COMMERCIAL` | Yes | Yes | Business plots |
| `SHOP` | No | No | State-owned shop plots |
| `PUBLIC` | No | No | Public areas |
| `GOVERNMENT` | No | No | Government buildings |
| `PRISON` | No | No | Prison facilities |
| `TOWING_YARD` | Yes | Yes | Vehicle towing yards |

### 3.3 Rental System

Located under `[rent]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `rent.enabled` | `true` | -- | Enable rental system |
| `rent.min_rent_price` | `10.0` | 0.1 - 10,000 | Minimum rent price per day |
| `rent.min_rent_days` | `1` | 1 - 365 | Minimum rental period in days |
| `rent.max_rent_days` | `30` | 1 - 365 | Maximum rental period in days |
| `rent.auto_evict` | `true` | -- | Automatically evict tenants when rent expires |

### 3.4 Rating System

Located under `[ratings]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `ratings.enabled` | `true` | -- | Enable plot rating system |
| `ratings.allow_multiple` | `false` | -- | Allow players to rate a plot more than once |
| `ratings.min_rating` | `1` | 1 - 5 | Minimum star rating |
| `ratings.max_rating` | `5` | 1 - 5 | Maximum star rating |

### 3.5 Spatial Index

The `PlotSpatialIndex` (`de.rolandsw.schedulemc.region.PlotSpatialIndex`) provides O(1) lookups for plot containment checks. This is used internally by `PlotManager` and does not have user-facing configuration. It partitions the world into grid cells and indexes plots by their bounding rectangles to accelerate lookups when checking whether a block position is inside a plot.

---

## 4. NPC Configuration

### 4.1 NPC Types

Defined in `NPCType` (`de.rolandsw.schedulemc.npc.data.NPCType`):

| Type | German Name | Description |
|------|-------------|-------------|
| `BEWOHNER` / `CITIZEN` | Bewohner | Standard citizen NPC |
| `VERKAEUFER` / `MERCHANT` | Verkaeufer | Shop merchant NPC |
| `POLIZEI` / `POLICE` | Polizei | Police officer NPC |
| `BANK` / `BANKER` | Bank | Bank teller NPC |
| `ABSCHLEPPER` | Abschlepper | Towing service NPC |
| `DRUG_DEALER` | Drug Dealer | Underground dealer NPC |

### 4.2 NPC Walkable Blocks

Located under `[npc]`:

| Key | Default | Description |
|-----|---------|-------------|
| `npc.walkable_blocks` | (see below) | Block types NPCs are allowed to walk on |

Default walkable blocks include: `stone`, `grass_block`, `dirt`, `cobblestone`, all plank and stair variants, `gravel`, `sand`, `stone_bricks`, `bricks`.

### 4.3 Schedule System

NPCs follow a daily schedule based on Minecraft tick time (0-24000 ticks per day). Schedules are configured per NPC entity via the NPC Spawner Tool.

**Default schedule values** (from `NPCData`):

| Parameter | Default Ticks | Real-World Time | Description |
|-----------|--------------|-----------------|-------------|
| `workStartTime` | `0` | 06:00 | When the NPC goes to work |
| `workEndTime` | `13000` | 19:00 | When work ends |
| `homeTime` | `23000` | 05:00 | When the NPC goes home to sleep |

**Minecraft tick-to-time mapping:**
- 0 ticks = 06:00
- 6000 ticks = 12:00 (noon)
- 12000 ticks = 18:00
- 18000 ticks = 00:00 (midnight)
- 24000 ticks = 06:00 (next day)

### 4.4 NPC Locations

Each NPC can have the following locations set:

| Location | Limit | Description |
|----------|-------|-------------|
| `homeLocation` | 1 | NPC's home / sleeping area |
| `workLocation` | 1 | Workplace (relevant for VERKAEUFER) |
| `assignedWarehouse` | 1 | Connected warehouse for shop NPCs |
| `leisureLocations` | Up to 10 | Recreation spots in the city |
| `policeStation` | 1 | Police station (POLIZEI only) |
| `patrolPoints` | Up to 16 | Patrol waypoints (POLIZEI only) |

### 4.5 Personality System

Defined in `NPCPersonality` (`de.rolandsw.schedulemc.npc.data.NPCPersonality`). Randomly assigned at NPC creation.

| Personality | Mood Weight | Demand Weight | Max Budget % | Purchase Threshold |
|-------------|-------------|---------------|-------------|-------------------|
| `SPARSAM` (Cautious) | 40% | 20% | 30% of wallet | Score >= 50 |
| `AUSGEWOGEN` (Balanced) | 30% | 30% | 50% of wallet | Score >= 40 |
| `IMPULSIV` (Impulsive) | 20% | 40% | 70% of wallet | Score >= 30 |

### 4.6 NPC Salary

NPC daily salary is randomized per day: `20 + random(0-130)` = **20-150 base income** per MC day. This value is then harmonized through the UDPS (Unified Dynamic Pricing System) via `EconomyController.getHarmonizedDailyReward()` which adjusts the amount based on the current economic cycle phase.

Only NPCs of type BEWOHNER and VERKAEUFER receive salaries; POLIZEI NPCs do not.

### 4.7 NPC Life System Constants

All NPC behavioral constants are centralized in `NPCLifeConstants` (`de.rolandsw.schedulemc.npc.life.NPCLifeConstants`). Key sections:

**Timing:**

| Constant | Value | Description |
|----------|-------|-------------|
| `NEEDS_UPDATE_INTERVAL` | 20 ticks (1s) | How often NPC needs are updated |
| `BEHAVIOR_DECISION_INTERVAL` | 10 ticks (0.5s) | NPC behavior decision frequency |
| `REPORT_CHECK_INTERVAL` | 200 ticks (10s) | Crime report check frequency |
| `MARKET_UPDATE_INTERVAL` | 24000 ticks (1 day) | Market condition update frequency |
| `INTERACTION_COOLDOWN` | 600 ticks (30s) | Player-NPC interaction cooldown |
| `NPC_INTERACTION_COOLDOWN` | 60 ticks (3s) | NPC-NPC interaction cooldown |
| `COMPANION_RESPAWN_COOLDOWN` | 6000 ticks (5 min) | Companion respawn delay |

**Emotions:**

| Constant | Value | Description |
|----------|-------|-------------|
| `DECAY_PER_TICK` | 0.02 | Emotion intensity decay per tick |
| `DURATION_HAPPY` | 6000 ticks (5 min) | Duration of happy state |
| `DURATION_SAD` | 12000 ticks (10 min) | Duration of sad state |
| `DURATION_ANGRY` | 9000 ticks (7.5 min) | Duration of angry state |
| `DURATION_FEARFUL` | 4800 ticks (4 min) | Duration of fearful state |
| `DURATION_SUSPICIOUS` | 12000 ticks (10 min) | Duration of suspicious state |

**Memory:**

| Constant | Value | Description |
|----------|-------|-------------|
| `MAX_MEMORIES_PER_PLAYER` | 10 | Memory entries per player |
| `MAX_DAILY_SUMMARIES` | 30 | Stored daily summaries |
| `MAX_PLAYER_PROFILES` | 50 | Tracked player profiles |

**Witness Detection:**

| Constant | Value | Description |
|----------|-------|-------------|
| `DETECTION_RANGE` | 20.0 blocks | Crime witness detection range |
| `MAX_REPORTS_PER_PLAYER` | 50 | Maximum stored reports per player |
| `SEVERITY_FOR_WANTED_LIST` | 7 | Crime severity to be added to wanted list |

---

## 5. Production Configuration

### 5.1 ProductionConfig (Builder Pattern)

Each production type is defined via `ProductionConfig.Builder` (`de.rolandsw.schedulemc.production.config.ProductionConfig`). Fields:

| Field | Default | Description |
|-------|---------|-------------|
| `id` | (required) | Unique identifier (e.g., `"tobacco_virginia"`) |
| `displayName` | (required) | Display name |
| `colorCode` | `"&f"` | Minecraft color code |
| `basePrice` | `10.0` | Base price per unit |
| `growthTicks` | `3600` | Ticks to grow from stage 0 to 7 (~3 minutes) |
| `baseYield` | `3` | Base harvest yield |
| `category` | `PLANT` | Category: PLANT, MUSHROOM, CHEMICAL, EXTRACT, PROCESSED |
| `requiresLight` | `true` | Whether the crop needs light |
| `minLightLevel` | `8` | Minimum light level for growth |
| `requiresWater` | `false` | Whether the crop needs water |
| `requiresTemperature` | `false` | Temperature sensitivity |

**Production Categories:**

| Category | Description | Color |
|----------|-------------|-------|
| `PLANT` | Tobacco, Cannabis, Coca, Poppy | Green |
| `MUSHROOM` | Mushrooms (special growth) | Pink |
| `CHEMICAL` | Meth, LSD, MDMA (synthesized) | Cyan |
| `EXTRACT` | Cocaine, Heroin (extracted) | Yellow |
| `PROCESSED` | Fermented, dried products | Gold |

### 5.2 Processing Stages

Each production type can define multiple processing stages via `ProcessingStageConfig`:

| Field | Description |
|-------|-------------|
| `stageName` | Name of the processing stage |
| `processingTime` | Duration in ticks |
| `inputItem` | Input item ID |
| `outputItem` | Output item ID |
| `preservesQuality` | Whether quality carries over |
| `requiredResource` | Optional resource (e.g., "diesel", "water") |
| `resourceAmount` | Amount of resource needed per process |

### 5.3 Tobacco System (TobaccoConfig)

Located under `[tobacco]`, `[drying_rack_capacities]`, `[fermentation_barrel_capacities]`, `[pot_capacities]`, `[bottle_effects]`, and `[grow_lights]` in the common config.

**Core Settings:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `tobacco.enabled` | `true` | -- | Enable/disable tobacco system |
| `tobacco.growth_speed_multiplier` | `1.0` | 0.1 - 10.0 | Growth speed (1.0 = normal, 2.0 = double) |
| `tobacco.drying_time` | `6000` | 100 - 72,000 | Drying time in ticks (6000 = 5 minutes) |
| `tobacco.fermenting_time` | `12000` | 100 - 72,000 | Fermentation time in ticks (12000 = 10 minutes) |
| `tobacco.fermentation_quality_chance` | `0.3` | 0.0 - 1.0 | Chance for quality improvement (30%) |

**Drying Rack Capacities:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `drying_rack_capacities.small_capacity` | `6` | 1 - 64 | Small drying rack slots |
| `drying_rack_capacities.medium_capacity` | `8` | 1 - 64 | Medium drying rack slots |
| `drying_rack_capacities.big_capacity` | `10` | 1 - 64 | Large drying rack slots |

**Fermentation Barrel Capacities:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `fermentation_barrel_capacities.small_capacity` | `6` | 1 - 64 | Small barrel slots |
| `fermentation_barrel_capacities.medium_capacity` | `8` | 1 - 64 | Medium barrel slots |
| `fermentation_barrel_capacities.big_capacity` | `10` | 1 - 64 | Large barrel slots |

**Pot Capacities (Water / Soil):**

| Pot Type | Water Capacity | Soil Capacity |
|----------|---------------|---------------|
| Terracotta | 100 | 50 |
| Ceramic | 200 | 100 |
| Iron | 400 | 200 |
| Golden | 800 | 400 |

All pot values range from 10 to 10,000.

**Bottle Effects:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `bottle_effects.fertilizer_yield_bonus` | `0.5` | 0.0 - 5.0 | Fertilizer yield bonus (+50%) |
| `bottle_effects.growth_booster_speed` | `2.0` | 1.0 - 10.0 | Growth booster multiplier (2x faster) |

**Grow Lights:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `grow_lights.require_light` | `true` | -- | Plants require light to grow |
| `grow_lights.min_light_level` | `9` | 0 - 15 | Minimum light level (9 = torches sufficient) |
| `grow_lights.basic_light_level` | `12` | 0 - 15 | Basic Grow Light emitted level |
| `grow_lights.basic_speed` | `1.0` | 0.1 - 10.0 | Growth speed under Basic light |
| `grow_lights.advanced_light_level` | `14` | 0 - 15 | Advanced Grow Light emitted level |
| `grow_lights.advanced_speed` | `1.25` | 0.1 - 10.0 | Growth speed under Advanced light (+25%) |
| `grow_lights.premium_light_level` | `15` | 0 - 15 | Premium UV Grow Light emitted level |
| `grow_lights.premium_speed` | `1.5` | 0.1 - 10.0 | Growth speed under Premium light (+50%) |
| `grow_lights.premium_quality_bonus` | `0.1` | 0.0 - 1.0 | Quality bonus under Premium UV (+10%) |

---

## 6. Vehicle Configuration

Vehicle settings are split between `ServerConfig` (gameplay) and `ClientConfig` (display).

### 6.1 Client Configuration (schedulemc-client.toml)

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `vehicle.third_person_when_enter_vehicle` | `true` | -- | Switch to third person when entering |
| `vehicle.temp_farenheit` | `false` | -- | Display temperature in Fahrenheit |
| `vehicle.vehicle_volume` | `0.25` | 0.0 - 1.0 | Vehicle sound volume |
| `vehicle.third_person_zoom` | `6.0` | 1.0 - 20.0 | Third person camera zoom distance |

### 6.2 Fuel System

**Fuel Station:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `machines.fuel_station.transfer_rate` | `5` | 1 - 32,767 | Fuel transfer rate (mB per tick) |
| `machines.fuel_station.valid_fuels` | `["#vehicle:fuel_station"]` | -- | Valid fuel tag(s) |
| `machines.fuel_station.morning_price_per_10mb` | `10` | 0 - MAX_INT | Price per 10 mB during morning (6:00-18:00) |
| `machines.fuel_station.evening_price_per_10mb` | `5` | 0 - MAX_INT | Price per 10 mB during evening (18:00-6:00) |

**Fuel Consumption (per 10 km, where 500 blocks = 1 km):**

| Key | Default | Description |
|-----|---------|-------------|
| `vehicle.fuel.normal_motor_consumption_per_10km` | `5.5` L | Normal motor consumption |
| `vehicle.fuel.performance_motor_consumption_per_10km` | `7.0` L | Performance motor consumption |
| `vehicle.fuel.performance_2_motor_consumption_per_10km` | `8.5` L | Performance 2 motor consumption |

**Canister:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `items.canister.max_fuel` | `1,000` | 1 - 10,000 | Maximum fuel in canister (mB) |

### 6.3 Engine Types and Speeds

**Motor Types:**

| Property | Normal | Performance | Performance 2 |
|----------|--------|-------------|---------------|
| Fuel Efficiency | 0.50 | 0.25 | 0.15 |
| Acceleration | 0.030 | 0.035 | 0.040 |
| Max Speed | 0.55 | 0.65 | 0.75 |
| Max Reverse Speed | 0.15 | 0.18 | 0.20 |

**Chassis Types:**

| Chassis | Fuel Efficiency | Acceleration | Max Speed | Inventory Slots |
|---------|----------------|-------------|-----------|-----------------|
| Limousine | 0.80 | 1.00 | 0.90 | 4 |
| Van | 0.70 | 0.95 | 0.85 | 6 |
| Luxus/Sport | 0.90 | 1.00 | 1.00 | 3 |
| Offroad/SUV | 0.60 | 0.80 | 0.70 | 6 |
| Truck | 0.60 | 0.80 | 0.765 | 0 (uses containers) |

### 6.4 Fuel Tank Capacities

| Key | Default | Description |
|-----|---------|-------------|
| `vehicle.parts.small_tank.max_fuel` | `11,000 mB` (11L) | Default tank |
| `vehicle.parts.medium_tank.max_fuel` | `15,000 mB` (15L) | 1st upgrade |
| `vehicle.parts.large_tank.max_fuel` | `20,000 mB` (20L) | 2nd upgrade |

### 6.5 Engine Simulation

| Key | Default | Description |
|-----|---------|-------------|
| `vehicle.engine_start_fuel_consumption` | `5 mB` | Fuel consumed on engine start |
| `vehicle.idle_fuel_consumption_interval` | `600 ticks` (30s) | Idle fuel consumption frequency |
| `vehicle.idle_battery_recharge_rate` | `1` per tick | Battery recharge while idling |
| `vehicle.driving_battery_recharge_multiplier` | `20.0` | Speed-based battery recharge multiplier |
| `vehicle.temperature_update_interval` | `20 ticks` (1s) | Temperature simulation frequency |

### 6.6 Vehicle Behavior

| Key | Default | Description |
|-----|---------|-------------|
| `vehicle.collide_with_entities` | `true` | Vehicles collide with non-vehicle entities |
| `vehicle.damage_entities` | `true` | Vehicles damage entities on collision |
| `vehicle.horn_flee` | `true` | Animals flee from horn |
| `vehicle.use_battery` | `true` | Starting requires battery |
| `vehicle.offroad_speed_modifier` | `1.0` | Speed on non-road blocks |
| `vehicle.onroad_speed_modifier` | `1.0` | Speed on road blocks |

### 6.7 Container Configuration

| Key | Default | Description |
|-----|---------|-------------|
| `vehicle.parts.item_container.slots` | `12` | Item Container inventory slots (truck-only) |
| `vehicle.parts.fluid_container.capacity` | `100,000 mB` | Fluid Container capacity (truck-only) |
| `vehicle.parts.container.reinstallation_cost` | `7,500.0` | Cost to reinstall a container (first install free) |

### 6.8 Vehicle Aging (Odometer)

| Key | Default | Description |
|-----|---------|-------------|
| `vehicle.aging.enabled` | `true` | Enable vehicle aging/degradation |
| `vehicle.aging.odometer_tier_1` | `250,000` (250 km) | Distance to tier 1 |
| `vehicle.aging.odometer_tier_2` | `500,000` (500 km) | Distance to tier 2 |
| `vehicle.aging.odometer_tier_3` | `750,000` (750 km) | Distance to tier 3 |
| `vehicle.aging.odometer_tier_4` | `1,000,000` (1000 km) | Distance to tier 4 |
| `vehicle.aging.max_health_tier_0` | `1.0` (100%) | Max health at tier 0 |
| `vehicle.aging.max_health_tier_1` | `0.75` (75%) | Max health at tier 1 |
| `vehicle.aging.max_health_tier_2` | `0.5` (50%) | Max health at tier 2 |
| `vehicle.aging.max_health_tier_3` | `0.25` (25%) | Max health at tier 3 |

### 6.9 Tire Season System (Serene Seasons Integration)

| Key | Default | Description |
|-----|---------|-------------|
| `vehicle.tire_season.enabled` | `true` | Enable tire season system |
| `vehicle.tire_season.correct_modifier` | `1.0` (100%) | Speed with correct tires |
| `vehicle.tire_season.wrong_modifier` | `0.5` (50%) | Speed with wrong tires |
| `vehicle.tire_season.all_season_modifier` | `0.85` (85%) | Speed with all-season tires |

### 6.10 Towing Service

| Key | Default | Description |
|-----|---------|-------------|
| `towing.base_fee` | `100.0` | Base towing fee |
| `towing.distance_fee_per_block` | `0.5` | Additional fee per block distance |
| `towing.damage_reduction_percent` | `10` | Damage reduction when collecting from tow yard |

### 6.11 Towing Membership

| Key | Default | Description |
|-----|---------|-------------|
| `towing.membership.payment_interval_days` | `7` MC days | Fee payment interval |

| Tier | Fee | Coverage |
|------|-----|----------|
| Bronze | 50.0 | 33% |
| Silver | 150.0 | 66% |
| Gold | 300.0 | 100% (free towing) |

### 6.12 Werkstatt (Workshop) System

Located under `[werkstatt]`:

**Service Costs:**

| Key | Default | Description |
|-----|---------|-------------|
| `werkstatt.base_inspection_fee` | `25.0` | Base inspection fee (always charged) |
| `werkstatt.repair_cost_per_percent` | `2.0` | Repair cost per percent damage |
| `werkstatt.battery_cost_per_percent` | `0.5` | Battery charge cost per percent |
| `werkstatt.oil_change_cost` | `15.0` | Oil change cost |

**Upgrade Costs:**

| Upgrade | Level 2 Cost | Level 3 Cost |
|---------|-------------|-------------|
| Motor (Normal -> Performance -> Performance 2) | 500.0 | 1,000.0 |
| Tank (11L -> 15L -> 20L) | 200.0 | 400.0 |
| Fender (Basic -> Chrome -> Sport) | 250.0 | 500.0 |
| Tires (per level) | 150.0 | -- |
| Paint change | 100.0 | -- |

### 6.13 Repair Kit

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `items.repair_kit.repair_amount` | `5.0` | 0.1 - 100.0 | Health restored per repair kit use |

---

## 7. Police and Crime Configuration

### 7.1 Wanted Level System

Defined in `CrimeManager` (`de.rolandsw.schedulemc.npc.crime.CrimeManager`):

| Constant | Value | Description |
|----------|-------|-------------|
| `MAX_WANTED_LEVEL` | `5` | Maximum wanted stars |
| `ESCAPE_DURATION` | `600 ticks` (30 seconds) | Time to hide before police gives up |
| `ESCAPE_DISTANCE` | `40.0 blocks` | Minimum distance from police to count as hiding |

**Wanted Level Tiers:**

| Stars | Category |
|-------|----------|
| 0 | Clean |
| 1-2 | Minor crime |
| 3-4 | Serious offenses |
| 5 | Maximum wanted level |

### 7.2 Police Behavior

Located under `[police]`:

**Core Settings:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `police.arrest_cooldown_seconds` | `5` | 1 - 60 | Cooldown before re-arrest |
| `police.detection_radius` | `32` | 8 - 128 | Crime detection radius in blocks |
| `police.arrest_distance` | `2.0` | 1.0 - 10.0 | Distance for arrest |

**Search Behavior:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `police.search_duration_seconds` | `60` | 10 - 300 | How long police searches for escaped player |
| `police.search_radius` | `50` | 10 - 100 | Search radius in blocks |
| `police.search_target_update_seconds` | `10` | 5 - 60 | How often search target changes |
| `police.backup_search_radius` | `50` | 20 - 100 | Backup police search radius |

**Indoor/Door Settings:**

| Key | Default | Description |
|-----|---------|-------------|
| `police.indoor_hiding_enabled` | `true` | Players can hide from police inside buildings |
| `police.block_doors_during_pursuit` | `true` | Doors are blocked during active pursuit |

**Raid System:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `police.raid_scan_radius` | `20` | 5 - 50 | Scan radius for illegal items on arrest |
| `police.illegal_cash_threshold` | `10,000.0` | 1,000 - 100,000 | Cash threshold for illegal cash detection |
| `police.raid_account_percentage` | `0.1` (10%) | 0.01 - 0.5 | Account balance percentage for fine |
| `police.raid_min_fine` | `1,000.0` | 100 - 50,000 | Minimum raid fine |

**Room-Based Scanning (Smart Search):**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `police.room_scan_enabled` | `true` | -- | Enable intelligent room-based scanning |
| `police.room_scan_max_size` | `500` | 50 - 2,000 | Maximum room size in blocks |
| `police.room_scan_max_depth` | `50` | 10 - 100 | Maximum Y-axis depth for room search |
| `police.room_scan_max_additional_rooms` | `3` | 0 - 10 | Additional rooms searched when contraband found |

**Patrol System:**

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `police.station_wait_minutes` | `5` | 1 - 60 | Time police stay at station |
| `police.station_radius` | `10` | 3 - 50 | Movement radius around station |
| `police.patrol_wait_minutes` | `1` | 1 - 30 | Time at each patrol point |
| `police.patrol_radius` | `3` | 1 - 20 | Movement radius around patrol points |

### 7.3 Prison System

Defined in `PrisonManager` (`de.rolandsw.schedulemc.npc.crime.prison.PrisonManager`):

| Constant | Value | Description |
|----------|-------|-------------|
| `JAIL_SECONDS_PER_WANTED_LEVEL` | `60` | Jail time per wanted star (in seconds) |
| `BAIL_MULTIPLIER` | `1,000.0` | Bail cost multiplier per wanted level |
| `BAIL_AVAILABLE_AFTER` | `0.33` (33%) | Bail available after 33% of sentence served |

**Bail Calculation:** `bail_amount = wanted_level * BAIL_MULTIPLIER`

| Wanted Level | Jail Time | Bail Amount | Bail Available After |
|-------------|-----------|-------------|---------------------|
| 1 star | 60 seconds | 1,000 | 20 seconds |
| 2 stars | 120 seconds | 2,000 | 40 seconds |
| 3 stars | 180 seconds | 3,000 | 60 seconds |
| 4 stars | 240 seconds | 4,000 | 80 seconds |
| 5 stars | 300 seconds | 5,000 | 100 seconds |

**Release Reasons:** `TIME_SERVED`, `BAIL_PAID`, `ADMIN_RELEASE`

Data stored at: `config/schedulemc/prisoners.json`

### 7.4 Stealing Minigame

Located under `[stealing]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `stealing.indicator_speed` | `0.04` | 0.001 - 0.2 | Red indicator speed (higher = harder) |
| `stealing.max_attempts` | `3` | 1 - 10 | Maximum steal attempts |
| `stealing.min_zone_size` | `0.05` (5%) | 0.01 - 0.5 | Smallest success zone (hardest) |
| `stealing.max_zone_size` | `0.15` (15%) | 0.01 - 0.5 | Largest success zone (easiest) |

---

## 8. Warehouse Configuration

Located under `[warehouse]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `warehouse.slot_count` | `32` | 8 - 128 | Number of different item slots per warehouse |
| `warehouse.max_capacity_per_slot` | `1,024` | 64 - 10,000 | Maximum items per slot (16 stacks) |
| `warehouse.delivery_interval_days` | `3` | 1 - 30 | Auto-delivery interval in Minecraft days |
| `warehouse.default_delivery_price` | `5` | 1 - 10,000 | Default delivery cost for items without a specific price |

Warehouses are linked to NPC merchants through the `assignedWarehouse` field on `NPCData`. Delivery prices per item type are configured through `DeliveryPriceConfig` (see [Section 13](#13-delivery-price-configuration)).

---

## 9. Market Configuration

### 9.1 Dynamic Pricing (UDPS)

The Unified Dynamic Pricing System controls price multipliers. See [Section 2.11](#211-dynamic-pricing-system-udps) for the config keys.

**Price multiplier range:**
- Minimum: `0.3` (configurable via `dynamic_pricing.min_multiplier`)
- Maximum: `5.0` (configurable via `dynamic_pricing.max_multiplier`)

### 9.2 Shop System

Located under `[shop]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `shop.enabled` | `true` | -- | Enable shop system |
| `shop.buy_multiplier` | `1.5` | 0.1 - 10.0 | Buy price multiplier (base price * multiplier) |
| `shop.sell_multiplier` | `0.5` | 0.1 - 10.0 | Sell price multiplier |

### 9.3 NPC Market Conditions

The `DynamicPriceManager` (`de.rolandsw.schedulemc.npc.life.economy.DynamicPriceManager`) maintains global and category-specific market conditions with JSON persistence. It applies:
- Time-based fluctuations
- Event-based price changes
- Category-specific supply/demand modifiers

**Price Modifiers** (from `NPCLifeConstants.Prices`):

| Modifier | Value | Description |
|----------|-------|-------------|
| `CRISIS_MULTIPLIER` | 1.5x | Price multiplier during crisis events |
| `SURPLUS_MULTIPLIER` | 0.6x | Price multiplier during surplus |
| `SHORTAGE_MULTIPLIER` | 1.8x | Price multiplier during shortage |

### 9.4 Supply/Demand Decay

The `sd_decay_rate` (default: `0.02` = 2% per update) controls how quickly supply/demand levels return to equilibrium. Combined with `update_interval_minutes` (default: 5), this means approximately 2% decay every 5 minutes.

---

## 10. Smartphone Configuration

### 10.1 Keybinding

Defined in `KeyBindings` (`de.rolandsw.schedulemc.client.KeyBindings`):

| Keybind | Default Key | Description |
|---------|-------------|-------------|
| `key.schedulemc.open_smartphone` | `P` | Open the smartphone UI |

The keybinding is registered under the `key.categories.schedulemc` category and can be remapped in Minecraft's Controls settings.

### 10.2 Map View Keybindings

Defined in `MapViewConfiguration` (`de.rolandsw.schedulemc.mapview.config.MapViewConfiguration`):

| Keybind | Default Key | Description |
|---------|-------------|-------------|
| `key.mapview.zoom` | `Z` | Map zoom |
| `key.mapview.toggleFullscreen` | `X` | Toggle fullscreen map |
| `key.mapview.menu` | `M` | Open map menu |

Stored in `config/mapview.properties`.

### 10.3 Map View Settings

Configured through `config/mapview.properties`:

| Setting | Default | Options | Description |
|---------|---------|---------|-------------|
| Zoom Level | `2` | 0-4 | Minimap zoom level |
| Old North | `false` | true/false | Legacy north orientation |
| MapViewRenderer Corner | `1` | 0=TL, 1=TR, 2=BR, 3=BL | Minimap screen corner |
| MapViewRenderer Size | `1` | -1 to 4 (small to XXXL) | Minimap size |
| Show Territories | `false` | true/false | Territory overlay on world map |
| Worldmap Zoom | `4.0` | 0.5 - 16.0 | World map zoom level |
| Worldmap Cache Size | `500` | (auto-minimum calculated) | LRU tile cache size |

---

## 11. Performance Configuration

### 11.1 ConfigCache

The `ConfigCache` (`de.rolandsw.schedulemc.util.ConfigCache`) reduces per-tick config lookups by caching frequently used values in static fields. It uses a `volatile` flag (`needsRefresh`) and double-checked locking for thread-safe lazy refresh.

**Cached values:**
- Police detection radius
- Police arrest cooldown (seconds and ticks)
- Police arrest distance
- Warehouse delivery interval days
- Economy start balance

Call `ConfigCache.invalidate()` to force a cache refresh (automatically done on config reload).

### 11.2 Rate Limiter

The `RateLimiter` (`de.rolandsw.schedulemc.economy.RateLimiter`) prevents command spam and economy exploits:

| Constant | Value | Description |
|----------|-------|-------------|
| `MAX_TRANSACTIONS_PER_MINUTE` | `10` | Maximum economy transactions per minute per player |
| `MINUTE_IN_MILLIS` | `60,000` | Rolling window duration |

Uses a `ConcurrentHashMap` of per-player `TransactionTracker` instances with `ArrayDeque`-based timestamp tracking.

### 11.3 Memory Cleanup

The `MemoryCleanupManager` (`de.rolandsw.schedulemc.economy.MemoryCleanupManager`) prevents memory leaks from offline player data:

| Constant | Value | Description |
|----------|-------|-------------|
| `CLEANUP_DELAY_MS` | `300,000` (5 minutes) | Grace period before cleaning up offline player data |
| `CLEANUP_INTERVAL_TICKS` | `1,200` (60 seconds) | How often the cleanup task runs |

Players who reconnect within the 5-minute window have their cleanup cancelled.

### 11.4 Incremental Save Manager

The `IncrementalSaveManager` (`de.rolandsw.schedulemc.util.IncrementalSaveManager`) distributes save operations over time instead of saving all data at once.

| Setting | Default | Description |
|---------|---------|-------------|
| `saveIntervalTicks` | `20` (1 second) | Ticks between incremental save passes |
| `batchSize` | `5` | Maximum components saved per tick (max: 20) |

**How it works:**
- Components implement `ISaveable` with a `isDirty()` flag and a priority (0 = highest, 10 = lowest).
- Each tick, up to `batchSize` dirty components are saved in priority order.
- On server shutdown, `saveAll()` is called to flush all remaining dirty data.
- `forceSaveAll()` saves everything regardless of dirty flags.

**Registered components include:** EconomyManager, PlotManager, EconomyCycle, GlobalEconomyTracker, ProducerLevel.

### 11.5 World Map Cache

The `WorldMapConfiguration` maintains an LRU tile cache:

| Setting | Default | Description |
|---------|---------|-------------|
| `cacheSize` | `500` | Maximum cached map tiles |
| `minZoom` | `0.5` | Minimum zoom level |
| `maxZoom` | `16.0` | Maximum zoom level |

The minimum cache size is automatically calculated based on screen resolution and minimum zoom level.

### 11.6 Navigation System

Located under `[navigation]`:

| Key | Default | Range | Description |
|-----|---------|-------|-------------|
| `navigation.scan_radius` | `500` | 100 - 2,000 | Road scanning radius in blocks |
| `navigation.path_update_interval` | `2,000` | 500 - 10,000 | Path recalculation interval in ms |
| `navigation.arrival_distance` | `5.0` | 1.0 - 50.0 | Distance to consider destination reached |

**Road blocks** (configurable list, defaults include): `cobblestone`, `stone_bricks`, `gravel`, `dirt_path`, `smooth_stone`, polished stone variants, `bricks`, `stone`, `granite`, `andesite`, `diorite`.

---

## 12. Data Files

All persistent data is stored as JSON files in the server's `config/` directory or the `config/schedulemc/` subdirectory.

### 12.1 File Locations

| File | Manager | Description |
|------|---------|-------------|
| `config/schedulemc/economy.json` | `EconomyManager` | Player balances and economy state |
| `config/schedulemc/plots.json` | `PlotManager` | Plot definitions, ownership, permissions |
| `config/schedulemc/npcs.json` | NPC System | NPC data, schedules, inventories |
| `config/schedulemc/warehouses.json` | `WarehouseManager` | Warehouse contents and delivery settings |
| `config/schedulemc/messages.json` | Message System | Localized message overrides |
| `config/schedulemc/achievements.json` | Achievement System | Player achievement progress |
| `config/schedulemc/prisoners.json` | `PrisonManager` | Active prisoner data |
| `config/schedulemc/prison.json` | `PrisonManager` | Prison cell definitions |
| `config/plotmod_loans.json` | `LoanManager` | Active loan data |
| `config/plotmod_crimes.json` | `CrimeManager` | Wanted levels and crime history |
| `config/schedulemc_economy_cycle.json` | `EconomyCycle` | Current economic cycle phase and state |
| `config/mapview.properties` | `MapViewConfiguration` | Client-side map display settings |

### 12.2 Backup System

The persistence layer (`AbstractPersistenceManager`) automatically creates backup files alongside the primary data files. Backup filenames follow the pattern `*.backup.json`. For example:
- `economy.json` -> `economy.backup.json`
- `plots.json` -> `plots.backup.json`

Backups are written before each save operation, preserving the previous state. If the primary file becomes corrupted, the backup can be renamed to restore the last known good state.

### 12.3 Auto-Save

The `save_interval_minutes` config value (default: 5 minutes) controls how often all data is auto-saved. With the `IncrementalSaveManager`, dirty data is continuously flushed in small batches every ~1 second, while a full save occurs at the configured interval and on server shutdown.

---

## 13. Delivery Price Configuration

The `DeliveryPriceConfig` (`de.rolandsw.schedulemc.config.DeliveryPriceConfig`) defines the base cost to deliver one item through the warehouse system. Prices can be set programmatically via `DeliveryPriceConfig.setPrice(Item, int)`.

### 13.1 Default Delivery Prices

The default delivery price for items without a specific entry is configurable via `warehouse.default_delivery_price` (default: `5`). This can also be set programmatically via `DeliveryPriceConfig.setDefaultPrice(int)`.

### 13.2 Per-Item Delivery Costs

**Basic Foodstuffs (cheap):**

| Item | Base Price |
|------|-----------|
| Wheat | 2 |
| Carrot | 2 |
| Potato | 2 |
| Beetroot | 2 |
| Apple | 3 |
| Bread | 5 |

**Meat:**

| Item | Base Price |
|------|-----------|
| Raw Beef | 8 |
| Raw Porkchop | 8 |
| Raw Chicken | 6 |
| Raw Mutton | 7 |
| Cooked Beef | 10 |
| Cooked Porkchop | 10 |

**Metals:**

| Item | Base Price |
|------|-----------|
| Iron Ingot | 15 |
| Gold Ingot | 25 |
| Copper Ingot | 8 |

**Gems:**

| Item | Base Price |
|------|-----------|
| Diamond | 100 |
| Emerald | 80 |
| Lapis Lazuli | 10 |

**Wood:**

| Item | Base Price |
|------|-----------|
| Oak/Birch/Spruce Log | 3 |
| Jungle/Acacia/Dark Oak Log | 4 |

**Building Materials:**

| Item | Base Price |
|------|-----------|
| Cobblestone | 1 |
| Stone | 2 |
| Stone Bricks | 3 |
| Glass | 2 |

**Tools and Weapons (expensive):**

| Item | Base Price |
|------|-----------|
| Iron Sword | 50 |
| Iron Pickaxe | 60 |
| Diamond Sword | 300 |
| Diamond Pickaxe | 400 |

### 13.3 Dynamic Pricing Integration

The `getDynamicPrice(Item)` method returns the UDPS-adjusted delivery price that factors in:
- The base static price from the table above
- Current economic cycle multipliers
- Inflation adjustments via `EconomyController.getDeliveryPrice()`

If the UDPS system is unavailable or encounters an error, the static base price is used as a fallback.

---

## Quick Reference: Config File Locations

```
<server_root>/
  config/
    schedulemc-common.toml          # Main server config (economy, plots, police, etc.)
    schedulemc-client.toml          # Client display settings (vehicle camera, sound)
    mapview.properties              # Map display settings
    schedulemc/
      economy.json                  # Player balances
      plots.json                    # Plot data
      npcs.json                     # NPC data
      warehouses.json               # Warehouse contents
      messages.json                 # Message overrides
      achievements.json             # Achievement progress
      prisoners.json                # Active prisoners
      prison.json                   # Prison cells
    plotmod_loans.json              # Active loans
    plotmod_crimes.json             # Crime records
    schedulemc_economy_cycle.json   # Economic cycle state
```
