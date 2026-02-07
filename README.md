<p align="center">
  <h1 align="center">ScheduleMC</h1>
  <p align="center">
    A comprehensive Minecraft Forge mod implementing a complete roleplay and economy server system.
    <br />
    Designed for immersive city roleplay, with deep production chains, NPC life simulation, vehicle mechanics, a full banking system, and much more.
  </p>
</p>

<p align="center">
  <img alt="Version" src="https://img.shields.io/badge/version-3.6.0--beta-blue?style=for-the-badge" />
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-1.20.1-green?style=for-the-badge&logo=mojangstudios" />
  <img alt="Forge" src="https://img.shields.io/badge/Forge-47.4.0-orange?style=for-the-badge" />
  <img alt="Java" src="https://img.shields.io/badge/Java-17-red?style=for-the-badge&logo=openjdk" />
  <img alt="Lines of Code" src="https://img.shields.io/badge/Lines_of_Code-219%2C500%2B-brightgreen?style=for-the-badge" />
  <img alt="Items" src="https://img.shields.io/badge/Items-354-purple?style=for-the-badge" />
  <img alt="Blocks" src="https://img.shields.io/badge/Blocks-152-yellow?style=for-the-badge" />
  <img alt="Commands" src="https://img.shields.io/badge/Commands-139-blueviolet?style=for-the-badge" />
  <img alt="License" src="https://img.shields.io/badge/License-GPLv3-blue?style=for-the-badge" />
</p>

---

## Table of Contents

- [Overview](#overview)
- [Project Statistics](#project-statistics)
- [Installation](#installation)
  - [Server Installation](#server-installation)
  - [Client Installation](#client-installation)
  - [Optional Dependencies](#optional-dependencies)
- [Quick Start](#quick-start)
  - [For Players](#for-players)
  - [For Server Administrators](#for-server-administrators)
- [Major Systems](#major-systems)
  - [1. Plot Management System](#1-plot-management-system)
  - [2. Economy System](#2-economy-system)
  - [3. NPC System](#3-npc-system)
  - [4. Police and Crime System](#4-police-and-crime-system)
  - [5. Production Systems](#5-production-systems)
    - [Illegal Production Chains (8)](#illegal-production-chains-8)
    - [Legal Production Chains (6)](#legal-production-chains-6)
  - [6. Vehicle System](#6-vehicle-system)
  - [7. Smartphone System](#7-smartphone-system)
  - [8. Warehouse System](#8-warehouse-system)
  - [9. Dynamic Market System](#9-dynamic-market-system)
- [Additional Systems](#additional-systems)
  - [MapView System](#mapview-system)
  - [Gang System](#gang-system)
  - [Territory System](#territory-system)
  - [Lock System](#lock-system)
  - [Towing System](#towing-system)
  - [Level and XP System](#level-and-xp-system)
  - [Achievement System](#achievement-system)
  - [Messaging System](#messaging-system)
  - [Utility System](#utility-system)
- [Commands Reference](#commands-reference)
  - [Plot Commands](#Service-commands)
  - [Economy Commands](#economy-commands)
  - [NPC Commands](#npc-commands)
  - [Police and Crime Commands](#police-and-crime-commands)
  - [Production Commands](#production-commands)
  - [Vehicle Commands](#vehicle-commands)
  - [Gang Commands](#gang-commands)
  - [Lock Commands](#lock-commands)
  - [Warehouse Commands](#warehouse-commands)
  - [Utility Commands](#utility-commands)
  - [Admin Commands](#admin-commands)
  - [General Commands](#general-commands)
- [Items Reference](#items-reference)
- [Blocks Reference](#blocks-reference)
- [API Overview](#api-overview)
  - [API Modules](#api-modules)
  - [Code Examples](#code-examples)
- [Development Guide](#development-guide)
  - [Prerequisites](#prerequisites)
  - [Building from Source](#building-from-source)
  - [Running in Development](#running-in-development)
  - [Project Structure](#project-structure)
  - [Technology Stack](#technology-stack)
  - [Design Patterns](#design-patterns)
- [Testing and Quality Assurance](#testing-and-quality-assurance)
- [Troubleshooting / FAQ](#troubleshooting--faq)
- [License](#license)
- [Credits](#credits)

---

## Overview

**ScheduleMC** is a large-scale Minecraft Forge mod that transforms a vanilla Minecraft server into a fully-featured roleplay and economy experience. It covers every aspect of city life simulation: owning and managing property, running businesses through complex production chains, interacting with AI-driven NPCs that follow daily schedules, driving vehicles, managing finances through a realistic banking system, and navigating a GTA-inspired police and crime system.

The mod is built for Minecraft 1.20.1 with Forge 47.4.0 and leverages CoreLib for OBJ model rendering, GUI systems, and networking utilities. It exposes a comprehensive public API with 12 modules, enabling third-party mods to integrate with any subsystem.

### Key Highlights

- **219,500+ lines of Java code** across **1,407 source files**
- **1,206 resource files** (textures, models, sounds, configurations)
- **354 items**, **152 blocks**, **139 commands**
- **12 API modules** with full external integration support
- **292 unit tests** across **19 test files**
- **55 manager classes**, **126 GUI screens**, **131 block entity types**, **100 network packets**
- **16 major interconnected systems** with dozens of supporting subsystems
- **14 production chains** (8 illegal, 6 legal) each with multi-step crafting processes
- **137 vehicle system files** with 5 chassis types, 3 engines, 6 tire types
- **NPC behavior engine** with 9 AI goals, 5 behavior actions, and 14 behavior states driving realistic daily life simulation

---

## Project Statistics

| Metric | Count |
|---|---|
| Lines of Java Code | 219,500+ |
| Source Files | 1,407 |
| Resource Files | 1,206 |
| Items | 354 |
| Blocks | 152 |
| Commands | 139 |
| API Modules | 12 |
| Unit Tests | 292 |
| Test Files | 19 |
| Manager Classes | 55 |
| GUI Screens | 126 |
| Block Entity Types | 131 |
| Network Packets | 100 |
| Event Handlers | 53 files (116 @SubscribeEvent) |
| Config Options | 228 |
| NPC AI Goals | 9 goals + 5 behavior actions |
| Vehicle Files | 137 |
| MapView Files | 122 |
| Production Chains | 14 (8 illegal + 6 legal) |

---

## Installation

### Server Installation

1. Download and install [Minecraft Forge 47.4.0](https://files.minecraftforge.net/) for Minecraft 1.20.1.
2. Download the latest `schedulemc-3.6.0-beta.jar` release from [GitHub Releases](https://github.com/Minecraft425HD/ScheduleMC/releases).
3. Download [CoreLib 1.20.1-1.1.1](https://maven.maxhenkel.de/repository/public/de/maxhenkel/corelib/) and place it in your `mods/` folder.
4. Place `schedulemc-3.6.0-beta.jar` into the server's `mods/` folder.
5. Start the server once to generate default configuration files.
6. Edit configuration files in `config/schedulemc/` to match your server's needs.
7. Restart the server.

**Minimum requirements:** Java 17, 4 GB RAM allocated to the server (8 GB recommended).

### Client Installation

1. Install Minecraft Forge 47.4.0 for your Minecraft 1.20.1 client.
2. Place `schedulemc-3.6.0-beta.jar` and `corelib-1.20.1-1.1.1.jar` into your `.minecraft/mods/` folder.
3. Launch Minecraft with the Forge profile.

```
.minecraft/
  mods/
    schedulemc-3.6.0-beta.jar
    corelib-1.20.1-1.1.1.jar
```

### Optional Dependencies

These mods are not required but provide additional integration features when present:

| Mod | Version | Purpose |
|---|---|---|
| [JEI (Just Enough Items)](https://modrinth.com/mod/jei) | 15.2.0.27 | Recipe viewing integration for production chains |
| [Jade](https://modrinth.com/mod/jade) | 11.8.0 | Block tooltip overlay showing production status, lock info, etc. |
| [The One Probe](https://modrinth.com/mod/the-one-probe) | 1.20.1-10.0.2 | Alternative block information overlay |

---

## Quick Start

### For Players

1. **Join the server.** Every new player receives a bank account with **1,000 Euro** starting balance.
2. **Open your smartphone** (configurable keybind, default: `P`) to access the Map, Bank, Contacts, and other apps.
3. **Find a plot.** Explore the city to find available plots. Plots come in types: Residential, Commercial, Shop, Public, and Government.
4. **Rent an apartment.** Use `/plot apartment rent <name>` to rent an apartment unit.
5. **Start earning money.** Sell items at shops, complete NPC quests, or start your own production business.
6. **Explore production.** Grow coffee, produce wine, brew beer, make chocolate, harvest honey, or age cheese -- all legally. Or take risks with illegal operations for higher profits.
7. **Buy a vehicle.** Visit a vehicle dealer, choose your chassis, engine, and tires, fuel up, and explore the city.
8. **Interact with NPCs.** Talk to residents, buy from merchants, and watch out for the police if your wanted level rises.
9. **Join a gang.** Create or join a gang with `/gang create <name>` or accept invitations, and claim territory.

### For Server Administrators

1. **Create the city layout.** Use the Plot Selection Tool (`/give @s schedulemc:plot_selection_tool`) to define plot boundaries by right-clicking two corners.
2. **Define plots.** Use `/plot create <name> <type>` to establish Residential, Commercial, Shop, Public, Government, Prison, and Towing Yard plots.
3. **Set up the economy.** Configure starting balance, tax rates, interest rates, and loan parameters in the config files under `config/schedulemc/`.
4. **Configure NPCs.** NPCs are placed as entities in-world. Use `/npc <name> schedule workstart 0700` to set their daily schedules, `/npc <name> speed <value>` to adjust movement, and `/npc <name> inventory` to manage their items.
5. **Configure production.** Set up which production chains are available and configure risk/reward balances.
6. **Set up warehouses.** Place warehouse blocks and link them to shop plots with `/warehouse setshop <shopId>`. Configure auto-delivery schedules.
7. **Configure the dynamic market.** Adjust supply/demand multiplier ranges and base prices through the market configuration.
8. **Establish police infrastructure.** Create Prison-type plots, set bail amounts, and configure wanted level decay and escape mechanics.
9. **Set up the hospital.** Use `/hospital setspawn` and `/hospital setfee <amount>` to configure respawn points and death costs.
10. **Test everything.** Use `/health` to run a comprehensive system health check across all subsystems (economy, plots, backups, etc.).

---

## Major Systems

### 1. Plot Management System

The plot system provides full land management with chunk-based spatial indexing for high-performance O(1) lookups, multi-level LRU caching for frequently accessed plots, and a comprehensive apartment and rating system.

**Plot Types:**

| Type | Purchasable | Rentable | Description |
|---|---|---|---|
| `RESIDENTIAL` | Yes | Yes | Player housing and apartments |
| `COMMERCIAL` | Yes | Yes | Player-owned businesses |
| `SHOP` | No | No | State-owned shops operated by Merchant NPCs |
| `PUBLIC` | No | No | Parks, roads, communal areas |
| `GOVERNMENT` | No | No | Government buildings, administrative areas |
| `PRISON` | No | No | Jail facilities for the police system |
| `TOWING_YARD` | Yes | Yes | Vehicle impound and towing lots |

**Core Features:**
- **Chunk-based spatial indexing** with O(1) lookup performance via `PlotSpatialIndex` (ConcurrentHashMap with 16x16x16 grid)
- **Multi-level LRU caching** (`PlotCache` for plot-level, `PlotChunkCache` for chunk-level) for high-frequency spatial queries
- **Plot protection** preventing unauthorized building, breaking, and interaction through `PlotProtectionHandler`
- **Apartment system** with individual units within a single plot, each with separate ownership and rent
- **Plot rating and review** system with 1-5 star ratings and leaderboards
- **Rent collection** with automatic recurring payments and auto-eviction on non-payment
- **Plot transfer** between players with configurable refund percentages (default 50% via abandon)
- **Trusted players** system for granting build permissions to friends
- **Visual selection tool** (Plot Wand) for easy boundary definition
- **Plot info blocks** displaying plot details in the world
- **Inventory protection** for chests and containers within plots

**Key Classes:**

| Class | Responsibility |
|---|---|
| `PlotManager` | Central plot management, CRUD operations, persistence |
| `PlotSpatialIndex` | Chunk-based spatial indexing for O(1) lookups |
| `PlotCache` | LRU cache for frequently accessed plot objects |
| `PlotChunkCache` | Chunk-level caching for rapid spatial queries |
| `PlotProtectionHandler` | Block and interaction protection enforcement |
| `PlotRegion` | Spatial region definition with world coordinates |
| `PlotArea` | Abstract area representation for plot boundaries |
| `PlotType` | Enum defining all 7 plot types and their capabilities |

---

### 2. Economy System

A deep and realistic economy simulation with 16 manager classes covering banking, loans, credit scoring, taxes, savings, recurring payments, anti-exploit measures, price management, and memory cleanup.

**Banking System:**
- **Bank accounts** with 1,000 Euro starting balance for every new player, created automatically on first join via `PlayerJoinHandler`
- **ATM blocks** for deposits, withdrawals, and transfers in the game world
- **Physical cash** system with Euro bills and coins as droppable, tradable items
- **Wallet system** (`WalletManager`) managing physical cash on hand vs. bank balance
- **Transaction history** (`TransactionHistory`) with complete audit trail of all financial activity

**Loan System (3 Tiers):**

| Tier | Maximum Amount | Interest Rate | Description |
|---|---|---|---|
| Small | 5,000 Euro | 10% | Short-term personal loans |
| Medium | 25,000 Euro | 15% | Business development loans |
| Large | 100,000 Euro | 20% | Major investment loans |

**Advanced Financial Features:**
- **Credit score system** (`CreditScoreManager`) affecting loan eligibility and interest rates
- **Credit-based loans** (`CreditLoanManager`) with dynamic terms based on credit history
- **Savings accounts** (`SavingsAccountManager`) with 5% weekly interest
- **Recurring payments** (`RecurringPaymentManager`) for rent, utilities, and subscriptions with configurable intervals (`RecurringPaymentInterval`)
- **Overdraft protection** (`OverdraftManager`) with configurable limits and fees
- **Tax system** (`TaxManager`) with property tax, sales tax, income tax, and automatic collection
- **State treasury** (`StateAccount`) managing government finances
- **Shop accounts** (`ShopAccountManager`) tracking shop revenue and expenses
- **Daily rewards**: 50 Euro base + 10 Euro per consecutive login streak day, managed by `DailyRewardManager`
- **Hospital system** with configurable respawn costs via `RespawnHandler`
- **Anti-exploit measures** (`AntiExploitManager`) with rate limiting (`RateLimiter`) and batch transaction management (`BatchTransactionManager`)
- **Memory cleanup** (`MemoryCleanupManager`) for automatic resource management
- **Risk premium** calculation (`RiskPremium`) for dynamic interest rate adjustments

**Economy Manager Classes:**

| Manager | Responsibility |
|---|---|
| `EconomyManager` | Core balance operations, deposits, withdrawals, transfers |
| `WalletManager` | Physical cash item management |
| `LoanManager` | Loan issuance, repayment tracking, defaults |
| `CreditLoanManager` | Credit-based loan system with dynamic terms |
| `CreditScoreManager` | Credit score tracking and calculation |
| `SavingsAccountManager` | Savings accounts with weekly interest accrual |
| `TaxManager` | Tax collection, brackets, and enforcement |
| `InterestManager` | Interest calculation and periodic accrual |
| `OverdraftManager` | Overdraft limits, fees, and enforcement |
| `RecurringPaymentManager` | Automatic scheduled payment processing |
| `ShopAccountManager` | Shop revenue, expenses, and profit tracking |
| `AntiExploitManager` | Detects and blocks common economy exploitation patterns |
| `BatchTransactionManager` | Groups related transactions for efficiency |
| `FeeManager` | Transaction fee calculation |
| `MemoryCleanupManager` | Automatic resource management and cleanup |
| `PriceManager` | Price range enforcement and management |

**Supporting Infrastructure:**

| Class | Responsibility |
|---|---|
| `EconomyController` | High-level economy orchestration |
| `EconomyCycle` / `EconomyCyclePhase` | Economic boom/bust cycle simulation |
| `GlobalEconomyTracker` | Server-wide economic health monitoring |
| `UnifiedPriceCalculator` | Consistent pricing across all systems |
| `PriceBounds` / `PriceManager` | Price range enforcement and management |
| `FeeManager` | Transaction fee calculation |
| `WarehouseMarketBridge` | Links warehouse stock levels to market pricing |
| `Transaction` / `TransactionType` | Transaction data model and categorization |
| `DailyRevenueRecord` | Daily revenue tracking per entity |
| `EconomicEvent` | Special economic event triggers |

---

### 3. NPC System

A sophisticated NPC system with three distinct types, a behavior engine with 9 AI goals and 14 behavior states, personality simulation, daily schedules, dialogue trees, quest distribution, relationships, and a witness mechanic that integrates with the police system.

**NPC Types:**

| Type | Role | Key Behaviors |
|---|---|---|
| Resident | City inhabitants | Follow daily schedules, have homes and workplaces, build relationships, witness crimes |
| Merchant | Shop operators | Run shops, buy/sell goods, manage inventory from linked warehouses, set prices |
| Police | Law enforcement | Patrol routes, detect crimes, pursue wanted players, make arrests, call for backup |

**Behavior System (9 AI Goals + 5 Behavior Actions):**

The NPC AI is driven by interchangeable behavior goals that determine NPC actions throughout the day:

**Custom ScheduleMC Goals:**

| Goal | Description |
|---|---|
| `MoveToHomeGoal` | Navigate to home location at end of day |
| `MoveToWorkGoal` | Navigate to workplace during work hours |
| `MoveToLeisureGoal` | Navigate to leisure locations during breaks (up to 10 configured) |
| `PolicePatrolGoal` | Follow configured patrol routes |
| `PoliceStationGoal` | Return to police station when off duty |
| `CompanionFollowGoal` | Companion NPCs follow assigned players (inner class in `CompanionBehavior`) |

**Vanilla Minecraft Goals (reused):**

| Goal | Description |
|---|---|
| `FloatGoal` | Swimming ability for NPCs |
| `OpenDoorGoal` | NPCs can open and close doors |
| `LookAtPlayerGoal` | NPCs face nearby players when idle |
| `RandomLookAroundGoal` | Idle looking behavior for realism |

**Behavior Actions (inner classes in `StandardActions`):**

| Action | Description |
|---|---|
| `FleeAction` | Flee from threats and danger |
| `AlertPoliceAction` | Call police when witnessing crimes |
| `InvestigateAction` | Investigate suspicious activity nearby |
| `HideAction` | Hide from danger in safe locations |
| `IdleAction` | Default idle behavior when no other action applies |

**Schedule System:**

NPCs follow daily schedules defined in HHMM format (e.g., `0800` = 8:00 AM, `1430` = 2:30 PM):

```
0700  -  Work Start (NPC travels to workplace)
1200  -  Lunch Break (NPC travels to leisure location)
1300  -  Resume Work (NPC returns to workplace)
1800  -  Work End (NPC returns home)
2300  -  Sleep (NPC stays at home)
```

**Feature Details:**
- **Personality system** (`personality/` package) influencing NPC behavior, dialogue tone, pricing, and decision-making
- **Dialogue system** with branching conversation trees and NPC-specific responses
- **Quest system** with NPC-issued tasks, objectives, and currency rewards
- **Relationship system** tracking player standing with individual NPCs, affecting prices and dialogue
- **NPC wallets** (`bank/` package) enabling NPCs to participate in the economy with salaries and purchases
- **Witness system** where NPCs observe crimes and report them, increasing the offender's wanted level
- **Companion system** allowing NPCs to follow and assist players
- **Custom skins** for unique NPC appearances via URL-based skin loading
- **Custom pathfinding** (`pathfinding/` package) optimized for city environments with roads and buildings
- **Entity registry** (`NPCEntityRegistry`) and **name registry** (`NPCNameRegistry`) for centralized NPC management
- **Stealing handler** (`NPCStealingHandler`) detecting theft from NPC shops

**NPC Admin Tools (Items):**
- NPC Spawner Tool -- spawn NPCs by type
- Location Tool -- set home and work locations
- Leisure Tool -- set leisure destinations (up to 10)
- Patrol Tool -- define police patrol waypoints

---

### 4. Police and Crime System

A GTA-inspired wanted level system with 5 stars, automatic decay, escape mechanics, a full prison system, bail payments, and player bounties.

**Wanted Level Stars:**

| Stars | Severity | Police Response |
|---|---|---|
| 1 | Minor infraction | Police NPCs observe the player |
| 2 | Moderate crime | Police NPCs approach and warn |
| 3 | Serious crime | Police NPCs actively pursue |
| 4 | Major crime | Aggressive pursuit, multiple officers respond |
| 5 | Most Wanted | Maximum force, all nearby police engage |

**Crime Mechanics:**
- **Auto-decay**: Wanted level decreases by 1 star per Minecraft day if no new crimes are committed
- **Escape mechanic**: Maintaining a distance of 40+ blocks from any police NPC for 30 continuous seconds reduces wanted level by 1 star
- **NPC witnesses**: NPCs that observe crimes will report them, increasing the offender's wanted level
- **Crime types** (`CrimeType` enum): Theft, assault, trespassing, drug possession, drug production, murder, vehicle theft, lockpicking, and more
- **Crime records** (`CrimeRecord`): Full history of criminal activity per player with timestamps and details

**Prison System:**
- Arrested players are teleported to a Prison-type plot
- Configurable sentence durations based on crime severity
- Bail payment system with amounts scaling by wanted level and crime history
- Admin tools for manual imprisonment and release

**Bounty System:**
- Players can place bounties on other players via `/bounty place <player> <amount>`
- Bounty hunters can claim rewards by bringing wanted players to justice
- Bounty data persistence (`BountyData`) and management (`BountyManager`)
- Public bounty listing via `/bounty list`

**Client-Side Features:**
- **Wanted level HUD overlay** (`WantedLevelOverlay`) displaying star count on screen
- **Crime stats smartphone app** (`CrimeStatsAppScreen`) showing personal crime record

**Key Classes:**

| Class | Responsibility |
|---|---|
| `CrimeManager` | Central crime tracking, wanted level management, crime detection |
| `BountyManager` | Bounty creation, tracking, and reward distribution |
| `BountyCommand` | Bounty command implementations |
| `CrimeRecord` | Individual crime event data with timestamps |
| `CrimeType` | Enumeration of all recognized crime categories |
| `WantedLevelOverlay` | Client-side HUD star rendering |

---

### 5. Production Systems

ScheduleMC features 14 production chains -- 8 illegal and 6 legal -- each with unique multi-step crafting processes, dedicated items, blocks, block entities, menus, and network packets. All production systems share a core framework (`production/` package) providing growth stages, quality levels, size variants, and serialization.

**Quality Levels:** Poor, Good, Very Good, Legendary -- quality affects sale price and is influenced by strain, growing conditions, equipment tier, and processing care.

#### Illegal Production Chains (8)

##### Tobacco Production

4 strains (Virginia, Burley, Oriental, Havana), 6-step chain, 32 items, 23 blocks.

```
  [Tobacco Seeds]  (4 strains)
        |
        v
  [Tobacco Pot]  (4 tiers: Terracotta, Ceramic, Iron, Golden)
        |            + Soil Bags (Small/Medium/Large)
        |            + Watering Can
        v
  [Growing Phase]  (8 growth stages)
        |             + Fertilizer / Growth Booster / Quality Booster
        |             + Grow Lights (3 tiers: Basic/Advanced/Premium)
        v
  [Drying Rack]  (3 sizes: Small/Medium/Big)
        |           Time-based processing, quality preservation
        v
  [Fermentation Barrel]  (3 sizes: Small/Medium/Big)
        |                   Duration affects flavor and quality
        v
  [Packaging Table]  (3 sizes, multi-block 2x2)
        |               4 package sizes (Small/Medium/Large/XL)
        |               Requires packaging materials
        v
  [Finished Product]  -->  Sell to NPC merchants or players
                            Dynamic pricing based on quality + market
```

**HUD Feature:** Tobacco Pot HUD Overlay (`TobaccoPotHudOverlay`) shows growing status when looking at pots.

**Business Integration:** `BusinessMetricsUpdateHandler` tracks production metrics for shop analytics.

---

##### Cannabis Production

4 strains (Indica, Sativa, Hybrid, Autoflower), 8-step chain, 10 items, 9 blocks.

```
  [Cannabis Seeds]  (4 strains)
        |
        v
  [Cannabis Plant]  (light and water requirements, 8 growth stages)
        |
        v
  [Harvest]  -->  [Fresh Cannabis Buds]
        |
        v
  [Drying Net]  -->  [Dried Cannabis]
        |
        v
  [Trimming Station]  -->  [Trimmed Buds]  +  [Trim By-Product]
        |
        v
  [Curing Jar]  -->  [Cured Cannabis]
        |
        v
  [Hash Press]  -->  [Hash]  (optional path)
        |
        v
  [Oil Extractor]  -->  [Cannabis Oil]  (optional path)
```

**Menu System:** Cannabis-specific menu types (`CannabisMenuTypes`) for processing station GUIs.

---

##### Coca Production

2 strains (Bolivian, Colombian), 5-step chain, 9 items, 9 blocks.

```
  [Coca Seeds]  (2 strains)
        |
        v
  [Coca Plant]  (growth stages)
        |
        v
  [Harvest]  -->  [Fresh Coca Leaves]
        |
        v
  [Extraction Vat]  (3 sizes)  -->  [Coca Paste]
        |                              Requires diesel fuel
        v
  [Refinery]  (3 sizes, glowing effect)  -->  [Refined Powder]
        |
        v
  [Crack Cooker]  -->  [Crack Rocks]  (optional path, requires baking soda)
```

---

##### Poppy Production

3 strains (Afghan, Turkish, Indian), 6-step chain, 8 items, 7 blocks.

```
  [Poppy Seeds]  (3 strains)
        |
        v
  [Poppy Plant]  (growth stages)
        |
        v
  [Scoring Machine]  -->  [Raw Latex]
        |                    Uses scoring knife tool
        v
  [Opium Press]  -->  [Pressed Opium]
        |
        v
  [Cooking Station]  -->  [Morphine]
        |
        v
  [Refinery]  -->  [Refined Product]
```

---

##### Methamphetamine Production

4-step chain with explosion risk, 8 items, 4 blocks.

```
  [Chemical Precursors]  (Ephedrine/Pseudoephedrine + Red Phosphorus + Iodine)
        |
        v
  [Chemistry Mixer]  -->  [Meth Paste]
        |
        v
  [Reduction Vessel]  -->  [Raw Meth]
        |                    *** EXPLOSION RISK! ***
        |                    Failed reactions destroy the block
        |                    and damage surrounding area
        v
  [Crystallizer]  -->  [Crystal Meth]
        |
        v
  [Vacuum Dryer]  -->  [Finished Product]
```

**Danger:** The Reduction Vessel (`Reduktionskessel`) has a configurable chance of explosion during operation. Explosions destroy the block, damage surrounding blocks, and can injure or kill the player. Build meth labs away from valuable structures.

---

##### LSD Production

6-step scientific chain with dedicated GUI interface, 6 items, 4 blocks.

```
  [Ergot Cultures]  (Mutterkorn)
        |
        v
  [Fermentation Tank]  -->  [Lysergic Acid]
        |                      GUI-based fermentation monitoring
        v
  [Distillation Apparatus]  -->  [Distilled Compound]
        |                          Block has glowing effect
        v
  [Chemical Synthesis]  -->  [LSD Solution]
        |
        v
  [Micro Dosing Station]  -->  [Applied Blotter Paper]
        |                        GUI-based precise application
        v
  [Perforation Press]  -->  [LSD Blotter Sheets]
```

**Menu System:** LSD-specific menu types (`LSDMenuTypes`) for fermentation tank and micro dosing GUIs.

---

##### MDMA Production

4-step chain with timing minigame, 6 items, 3 blocks.

```
  [Safrole Precursor]
        |
        v
  [Reaction Vessel]  -->  [MDMA Base]
        |                    Block has glowing effect
        |                    Requires precise chemical ratios
        v
  [Drying Oven]  -->  [MDMA Crystals]
        |                Block emits heat (hot to touch)
        v
  [Pill Press]  -->  [Ecstasy Pills]
                       *** TIMING MINIGAME! ***
                       Player must press at the right moment
                       Timing affects pill quality
                       Requires binder + dye materials
```

**Menu System:** MDMA-specific menu types (`MDMAMenuTypes`) for the pill press timing interface.

---

##### Mushroom Production

3 strains (Cubensis, Azurescens, Mexicana), 4-step chain, 15 items, 4 blocks.

```
  [Spore Syringes]  (3 strains)
        |
        v
  [Substrate/Mist Bags]  (3 sizes: Small/Medium/Large)
        |                    Inoculate substrate with spores
        v
  [Climate Lamp]  (3 tiers)  -->  [Colonized Substrate]
        |                           Requires water tank nearby
        |                           Light and humidity requirements
        v
  [Fruiting]  -->  [Fresh Mushrooms]
        |
        v
  [Dehydrator]  -->  [Dried Mushrooms]
```

**Environment Requirements:** Mushroom cultivation requires both climate lamps (3 tiers) and a water tank block for proper humidity control.

---

#### Legal Production Chains (6)

All legal production chains follow similar multi-step patterns with dedicated items, blocks, block entities, menus, and network handlers.

##### Coffee Production

```
  [Coffee Seeds]  -->  [Coffee Plant]  -->  [Coffee Cherries]
        |
        v
  [Roasting Machine]  -->  [Roasted Beans]
        |
        v
  [Coffee Grinder]  -->  [Ground Coffee]
        |
        v
  [Brewing Station]  -->  [Coffee Cup]
```

**Network:** Coffee-specific networking (`CoffeeNetworking`) for machine synchronization.

---

##### Wine Production

```
  [Grape Seeds]  -->  [Grape Vines]  -->  [Grapes]
        |
        v
  [Crushing Basin]  -->  [Grape Must]
        |
        v
  [Fermentation Barrel]  -->  [Wine]
        |                       Aging duration improves quality
        v
  [Bottling Station]  -->  [Wine Bottles]
```

---

##### Cheese Production

```
  [Milk Bucket]
        |
        v
  [Curdling Vat]  -->  [Cheese Curds]
        |
        v
  [Cheese Press]  -->  [Cheese Wheel]
        |
        v
  [Aging Shelf]  -->  [Aged Cheese]
                        Quality improves with aging time
```

---

##### Honey Production

```
  [Bee Hive]  -->  [Honeycomb]
        |
        v
  [Honey Extractor]  -->  [Raw Honey]
        |
        v
  [Filtering Station]  -->  [Filtered Honey]
        |
        v
  [Bottling Station]  -->  [Honey Jars]
```

---

##### Chocolate Production

```
  [Cocoa Beans]
        |
        v
  [Roasting Drum]  -->  [Roasted Cocoa]
        |
        v
  [Grinding Mill]  -->  [Cocoa Mass]
        |
        v
  [Conching Machine]  -->  [Chocolate Mixture]
        |
        v
  [Mold Station]  -->  [Chocolate Bars]
```

---

##### Beer Production

```
  [Barley / Wheat]
        |
        v
  [Malting Floor]  -->  [Malt]
        |
        v
  [Mash Tun]  -->  [Wort]
        |
        v
  [Brew Kettle]  +  [Hops]  -->  [Boiled Wort]
        |
        v
  [Fermenter]  -->  [Beer]
        |
        v
  [Keg / Bottle]  -->  [Finished Beer]
```

---

**Production Framework (`production/` package):**

| Component | Description |
|---|---|
| `production/core/` | Shared production logic, base classes |
| `production/growth/` | Growth stage management, timers |
| `production/config/` | Production chain configuration |
| `production/data/` | Data serialization and persistence |
| `production/items/` | Shared production item base classes |
| `production/blocks/` | Shared production block base classes |
| `production/blockentity/` | Shared block entity base classes |
| `production/nbt/` | NBT serialization for production data |
| `ProductionSize` | Enum for equipment size variants |

---

### 6. Vehicle System

A complete vehicle system spanning 137 files, covering chassis types, engines, tires, fuel mechanics, garages, license plates, and OBJ model rendering through CoreLib.

**Vehicle Components:**

| Component | Options | Details |
|---|---|---|
| Chassis Types | 5 | Limousine, Van, Truck, Offroad (SUV), Luxus (Sports) |
| Engine Types | 3 | Normal Motor, Performance Motor, Performance 2 Motor (upgrade tiers 0/1/2) |
| Tire Types | 6 | Standard, Sport, Premium, Offroad, All-Terrain, Heavy Duty |
| Fenders | 3 | Basic, Chrome, Sport |
| Fuel Tanks | 3 | 15L, 30L, 50L |
| Modules | 3 | Cargo, Fluid, License Plate Holder |

**Features:**
- **Fuel system** with diesel and gasoline fuel types, fuel cans (empty/full), and fuel station blocks for refueling
- **Garage system** with garage blocks for vehicle storage, retrieval, and management
- **License plate system** with customizable plate text
- **OBJ model support** via CoreLib for detailed 3D vehicle models with proper rendering
- **Vehicle damage** system with durability, collision damage, and repair mechanics using maintenance kits
- **Towing integration** for abandoned or illegally parked vehicles (see Towing System)
- **Vehicle sounds** for engine start, idle, driving, horn, and collisions
- **Physics-based movement** with speed, acceleration, braking, and handling varying by component configuration
- **Vehicle creative tab** (`ModCreativeTabs`) for easy access in creative mode
- **Custom damage source** (`DamageSourceVehicle`) for vehicle-related damage
- **Vehicle constants** (`VehicleConstants`) for centralized configuration values
- **UUID-based identification** (`PredicateUUID`) for vehicle entity tracking
- **Mixin integration** (`MixinConnector`) for rendering pipeline modifications

**Package Structure:**

| Package | Contents |
|---|---|
| `vehicle/entity/` | Vehicle entity definitions and behavior |
| `vehicle/vehicle/` | Vehicle data models, configurations |
| `vehicle/blocks/` | Fuel stations, garages |
| `vehicle/items/` | Parts, fuel, tools, keys |
| `vehicle/gui/` | Vehicle-related GUI screens |
| `vehicle/fuel/` | Fuel types, consumption calculations |
| `vehicle/sounds/` | Vehicle sound events |
| `vehicle/net/` | Vehicle network packets |
| `vehicle/events/` | Vehicle event handlers |
| `vehicle/util/` | Vehicle utility classes |
| `vehicle/fluids/` | Fluid types for fuel |
| `vehicle/mixins/` | Rendering mixins |

---

### 7. Smartphone System

An in-game smartphone with 11+ applications providing access to most mod features through a unified mobile interface. Opening the smartphone activates PvP protection to prevent combat exploitation.

**Apps:**

| App | Screen Class | Description |
|---|---|---|
| Map | (integrated) | City minimap and navigation |
| Dealer | `DealerAppScreen` | Browse and purchase from dealers |
| Products | `ProductsAppScreen` | View available production items and prices |
| Order | `OrderAppScreen` | Place and track delivery orders |
| Contacts | `ContactsAppScreen` | Manage player and NPC contacts with detail view (`ContactDetailScreen`) |
| Messages | `MessagesAppScreen` | Send and receive text messages |
| Plot | `PlotAppScreen` | View plot information, ownership, and listings |
| Settings | `SettingsAppScreen` | Configure smartphone preferences |
| Bank | `BankAppScreen` | Mobile banking: balance, transfers, history |
| Crime Stats | `CrimeStatsAppScreen` | View personal crime record and wanted status |
| Chat | `ChatScreen` | In-game communication channels |
| Gang | `GangAppScreen` | Gang management and mission tracking |
| Achievements | `AchievementAppScreen` | View unlocked achievements and progress |
| Producer Level | `ProducerLevelAppScreen` | View production XP and unlocks |
| Towing Service | `TowingServiceAppScreen` | Request towing or view impounded vehicles |
| Membership | `MembershipSelectionScreen` | Manage membership subscriptions |
| Scenario Editor | `ScenarioEditorScreen` | Create and edit gang scenarios (admin) |
| Towing Yard Selection | `TowingYardSelectionScreen` | Choose towing destination |

**PvP Protection:**
- Players are immune to damage while the smartphone GUI is open (`SmartphoneProtectionHandler`)
- Attackers who hit a smartphone-using player receive +1 wanted star
- Prevents unfair combat situations where players cannot defend themselves

**Client-Side Infrastructure:**

| Class | Responsibility |
|---|---|
| `SmartphoneScreen` | Main smartphone GUI container and app launcher |
| `SmartphoneKeyHandler` | Keybind registration and toggle handling |
| `SmartphonePlayerHandler` | Player-specific smartphone state management |
| `SmartphoneProtectionHandler` | PvP immunity enforcement |
| `SmartphoneTracker` | Tracks which players have phones open server-wide |
| `SmartphoneNetworkHandler` | Client-server data synchronization |
| Additional screens | `ConfirmDialogScreen`, `InputDialogScreen`, `CombinationLockScreen` |

---

### 8. Warehouse System

A large-scale storage system with 32 slots holding up to 1,024 items each, automatic deliveries, NPC merchant integration, and market system bridging.

**Features:**
- **32 storage slots**, each holding up to **1,024 items** (total capacity: 32,768 items)
- **Auto-delivery system**: New stock delivered every 3 in-game days (configurable)
- **NPC integration**: Merchant NPCs restock their shops from connected warehouses
- **Market bridge** (`WarehouseMarketBridge`): Direct integration with the dynamic market for automatic pricing based on warehouse stock levels (`WarehouseStockLevel`)
- **Shop linking**: Connect warehouses to shop plots via `/warehouse setshop <shopId>`
- **State payment**: Government treasury funds delivery costs automatically
- **Revenue tracking**: Income and expense monitoring per warehouse
- **Warehouse commands** for full inventory management

**Usage Flow:**
```
1. Admin places a Warehouse block
2. Link to Shop Plot:     /warehouse setshop <shopId>
3. Link to NPC Merchant:  /npc <name> warehouse set
4. Add items:             /warehouse add <item> <amount>
5. NPCs automatically sell items from the warehouse
6. Auto-delivery refills stock every 3 days
7. State account is charged for delivery costs
```

---

### 9. Dynamic Market System

A supply-and-demand pricing system that adjusts item prices based on market activity, creating a living economy that responds to player behavior.

**Features:**
- **Supply/demand pricing**: Prices increase when items are scarce and decrease when oversupplied
- **Price multiplier range**: 0.5x to 2.0x of base price, enforced by `PriceBounds`
- **Unified price calculator** (`UnifiedPriceCalculator`) ensuring consistent pricing across all shop, NPC, and warehouse transactions
- **Economic cycles** (`EconomyCycle`, `EconomyCyclePhase`) simulating boom and bust periods affecting all prices
- **Global economy tracker** (`GlobalEconomyTracker`) monitoring overall server economic health
- **Economic events** (`EconomicEvent`) triggering special market conditions (shortages, surpluses, etc.)
- **Warehouse-market bridge** (`WarehouseMarketBridge`) for automatic supply tracking based on warehouse stock
- **Item categorization** (`ItemCategory`) for grouping items into market sectors
- **Price history** tracking for trend analysis

---

## Additional Systems

### MapView System

A comprehensive minimap and world map system spanning **122 files**, providing players with real-time navigation, plot boundary visualization, NPC location markers, and territory overlays. Uses Mixin (`MixinConnector`, Mixin 0.8.5 + MixinExtras 0.5.0) for deep integration with Minecraft's rendering pipeline.

### Gang System

An organized crime system with full gang management, progression, and missions.

**Features:**
- **Gang creation and management** with hierarchical rank structures via `GangRank` (Boss, Underboss, Member, Recruit)
- **Gang reputation** (`GangReputation`) affecting NPC behavior and police response
- **Gang perks** (`GangPerk`) unlocked through leveling (e.g., reduced police attention, better prices, shared resources)
- **Level requirements** (`GangLevelRequirements`) with multiple XP sources (`GangXPSource`) from missions, territory control, production, and crime
- **Gang missions** (`mission/` package) with objectives, rewards, and cooperative gameplay
- **Scenario editor** (`ScenarioEditorScreen`) for admins to create custom gang events and storylines
- **Member management** (`GangMemberData`) with join dates, contribution tracking, and activity stats
- **Network synchronization** (`network/` package) for multiplayer gang state
- **Dedicated smartphone app** (`GangAppScreen`) for mobile gang management

**Key Classes:** `Gang`, `GangManager`, `GangCommand`, `GangRank`, `GangReputation`, `GangPerk`, `GangXPSource`, `GangLevelRequirements`, `GangMemberData`

### Territory System

A territory control system allowing gangs and factions to claim and contest regions of the map.

**Features:**
- **Territory definitions** with typed regions (`TerritoryType`) for different strategic values
- **Territory tracking** (`TerritoryTracker`) with real-time state monitoring
- **Territory management** (`TerritoryManager`) for claiming, defending, contesting, and losing territory
- **Map command** (`MapCommand`) for visualizing territory boundaries and ownership
- **Network packets** (`network/` package) for multiplayer territory synchronization

**Key Classes:** `Territory`, `TerritoryManager`, `TerritoryTracker`, `TerritoryType`, `MapCommand`

### Lock System

A 5-tier door and container locking system with physical keys, key blanks, combination codes, lock picking, key duplication, and alarm integration with the police system.

**Lock Types:**

| Lock Type | Pick Chance | Key Duration | Key Uses | Has Code | Code Rotation | Triggers Alarm | Required Blank |
|---|---|---|---|---|---|---|---|
| Simple | 80% | 7 days | 100 | No | N/A | No | Copper |
| Security | 40% | 3 days | 30 | No | N/A | No | Iron |
| High Security | 10% | 12 hours | 10 | No | N/A | Yes | Netherite |
| Combination | 0% (brute-force) | N/A | N/A | Yes | Permanent | No | N/A |
| Dual Lock | 5% | 12 hours | 10 | Yes | Rotates daily | Yes | Netherite |

**Key Origins and Degradation:**

| Origin | Duration Modifier | Uses Modifier |
|---|---|---|
| Original | 100% | 100% |
| Copy | 50% | 50% |
| Stolen | 25% | 25% |

**Features:**
- **Lock picking** with configurable success rates; failed attempts on High Security and Dual Locks trigger an alarm that increases the picker's wanted level
- **Key duplication** with degraded performance (copies are weaker than originals; stolen keys are weakest)
- **Key blank tiers**: Copper (Simple locks), Iron (Security locks), Netherite (High Security and Dual locks)
- **Combination lock GUI** (`CombinationLockScreen`) for entering numeric codes
- **Daily code rotation** on Dual Locks for enhanced security
- **Door lock handler** (`DoorLockHandler`) for automatic lock checking on all door and container interactions
- **Lock data persistence** (`LockData`) via `LockManager`

**Key Classes:** `LockManager`, `LockType`, `LockData`, `LockCommand`, `DoorLockHandler`, `LockType.KeyOrigin`

### Towing System

A vehicle impound system for illegally parked, abandoned, or confiscated vehicles.

**Features:**
- **Towing yard plots** (`TOWING_YARD` plot type) with individual parking spots (`TowingYardParkingSpot`)
- **Towing service registry** (`TowingServiceRegistry`) for managing tow operator companies
- **Towing invoices** (`TowingInvoiceData`) with itemized charges
- **Towing transactions** (`TowingTransaction`) with full financial tracking
- **Membership tiers** (`MembershipTier`) with `MembershipData` for reduced towing fees and priority service
- **Smartphone integration**: `TowingServiceAppScreen` for requesting towing, `TowingYardSelectionScreen` for choosing impound destinations
- **Menu system** (`menu/` package) for in-world towing interactions
- **Network packets** (`network/` package) for multiplayer synchronization

### Level and XP System

A progression system tracking player experience across production and other activities, gating access to advanced features and recipes.

**Features:**
- **Producer levels** (`ProducerLevel`) with XP-based progression and named tiers
- **XP sources** (`XPSource`) earned from production, sales, quests, and other activities
- **Unlock categories** (`UnlockCategory`) gating access to advanced recipes, equipment tiers, and production chains
- **Unlockable content** (`Unlockable`) tied to specific level thresholds
- **Level requirements** (`LevelRequirements`) defining XP thresholds per level
- **Producer level data** (`ProducerLevelData`) for per-player persistence
- **Client-side rendering** (`client/` package) for level display and progress bars
- **Network synchronization** (`network/` package) for multiplayer state
- **Smartphone app** (`ProducerLevelAppScreen`) showing current level, XP, and available unlocks

### Achievement System

A comprehensive achievement tracking system with 24 achievements across 5 categories, rewarding players for milestones in production, economy, social, crime, and exploration activities.

**Features:**
- **24 achievements** across 5 categories: Production, Economy, Social, Crime, and Exploration
- **5 difficulty tiers**: Bronze, Silver, Gold, Diamond, and Platinum with increasing rewards
- **Monetary rewards** scaling from 100 EUR (Bronze) to 50,000 EUR (Platinum)
- **Automatic tracking** of player actions and progress via event listeners
- **Network synchronization** for multiplayer achievement state
- **Smartphone app** (`AchievementAppScreen`) for viewing progress and unlocked achievements
- **Persistent data** (`AchievementData`, `PlayerAchievements`) stored per player

**Key Classes:** `AchievementManager`, `AchievementTracker`, `Achievement`, `AchievementCategory`, `AchievementTier`, `AchievementData`, `PlayerAchievements`

### Messaging System

A player-to-player and player-to-NPC messaging system with persistent message history and reputation-based NPC interactions.

**Features:**
- **Player-to-player messaging** with real-time delivery and notification
- **Player-to-NPC messaging** with reputation-based response behavior
- **Persistent message history** (`Message`) stored per player
- **Reputation system** affecting NPC willingness to respond and message tone
- **Network synchronization** (`network/` package) for real-time message delivery
- **Smartphone integration** via `MessagesAppScreen` and `ContactsAppScreen`
- **Contact management** with player and NPC contact lists

**Key Classes:** `MessageManager`, `Message`, `MessageNotificationOverlay`, `MessageNetworkHandler`

### Utility System

A water and electricity infrastructure system for plots, adding an ongoing maintenance cost to property ownership.

**Features:**
- **Utility categories** (`UtilityCategory`) for water and electricity services
- **Plot utility data** (`PlotUtilityData`) tracking consumption per plot
- **Consumption monitoring** (`UtilityConsumptionData`) with metered billing
- **Utility registry** (`UtilityRegistry`) for registering blocks that consume utilities
- **Consumer interface** (`IUtilityConsumer`) implemented by production blocks that require water or electricity
- **Event handler** (`UtilityEventHandler`) for automatic utility processing and billing
- **Plot utility management** (`PlotUtilityManager`) for toggling services on/off per plot
- **Commands** (`commands/` package) for viewing and managing utility usage (`/utility`, `/strom`, `/wasser`)

---

## Commands Reference

ScheduleMC provides commands organized by system. Below is a reference based on the actual registered command handlers.

### Plot Commands (`PlotCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/plot create <name> <type>` | Admin | Create a new plot (types: residential, commercial, shop, public, government, prison, towing_yard) |
| `/plot setowner <player>` | Admin | Set the owner of a plot |
| `/plot remove` | Admin | Remove a plot |
| `/plot reindex` | Admin | Rebuild the spatial index |
| `/plot debug` | Admin | Toggle debug mode for plots |
| `/plot settype <type>` | Admin | Change a plot's type |
| `/plot warehouse set` | Admin | Link a warehouse to the current plot |
| `/plot warehouse clear` | Admin | Remove warehouse link |
| `/plot warehouse info` | Admin | View warehouse link info |
| `/plot apartment wand` | Admin | Get the apartment selection tool |
| `/plot apartment create <name>` | Admin | Create an apartment unit |
| `/plot apartment delete <name>` | Admin | Delete an apartment unit |
| `/plot apartment list` | Player | List apartments in the current plot |
| `/plot apartment info <name>` | Player | View apartment details |
| `/plot apartment rent <name>` | Player | Rent an apartment unit |
| `/plot apartment leave` | Player | Leave a rented apartment |
| `/plot apartment setrent <amount>` | Admin | Set apartment rental price |
| `/plot apartment evict <player>` | Admin | Evict a tenant from an apartment |

### Economy Commands (`MoneyCommand.java`, `StateCommand.java`, `HospitalCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/money set <player> <amount>` | Admin | Set a player's balance |
| `/money give <player> <amount>` | Admin | Add money to a player's account |
| `/money take <player> <amount>` | Admin | Remove money from a player's account |
| `/money history <player>` | Admin | View a player's transaction history |
| `/state balance` | Admin | View the state treasury balance |
| `/state deposit <amount>` | Admin | Add funds to the state treasury |
| `/state withdraw <amount>` | Admin | Remove funds from the state treasury |
| `/hospital setspawn` | Admin | Set hospital respawn point |
| `/hospital setfee <amount>` | Admin | Set hospital respawn fee |
| `/hospital info` | Admin | View hospital configuration |

### NPC Commands (`NPCCommand.java`, `AdminToolsCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/npc <name> info` | Admin | View detailed NPC information |
| `/npc <name> movement <true/false>` | Admin | Enable or disable NPC movement |
| `/npc <name> speed <value>` | Admin | Set NPC movement speed |
| `/npc <name> schedule workstart <HHMM>` | Admin | Set NPC work start time |
| `/npc <name> schedule workend <HHMM>` | Admin | Set NPC work end time |
| `/npc <name> schedule home` | Admin | Set NPC home to current position |
| `/npc <name> leisure add` | Admin | Add current position as leisure location |
| `/npc <name> leisure remove <index>` | Admin | Remove a leisure location |
| `/npc <name> leisure list` | Admin | List all leisure locations |
| `/npc <name> leisure clear` | Admin | Clear all leisure locations |
| `/npc <name> inventory give <item> <amount>` | Admin | Give items to NPC inventory |
| `/npc <name> inventory clear` | Admin | Clear NPC inventory |
| `/npc <name> wallet set <amount>` | Admin | Set NPC wallet balance |
| `/npc <name> wallet add <amount>` | Admin | Add money to NPC wallet |
| `/npc <name> wallet remove <amount>` | Admin | Remove money from NPC wallet |
| `/npc <name> warehouse set` | Admin | Link NPC to a warehouse |
| `/npc <name> warehouse clear` | Admin | Remove NPC warehouse link |
| `/npc <name> warehouse info` | Admin | View NPC warehouse link |
| `/admintools remover` | Admin | Get the entity remover tool |
| `/admintools help` | Admin | Show admin tools help |

### Police and Crime Commands (`PrisonCommand.java`, `BountyCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/prison create <name>` | Admin | Create a prison facility |
| `/prison addcell <id> <pos1> <pos2> <level>` | Admin | Add a cell to a prison |
| `/prison removecell <id>` | Admin | Remove a prison cell |
| `/prison list` | Admin | List all prisons |
| `/prison cells <prison>` | Admin | List cells in a prison |
| `/prison inmates` | Admin | List all imprisoned players |
| `/prison release <player>` | Admin | Release a player from prison |
| `/prison status` | Admin | View prison system status |
| `/bail` | Player | Pay bail to leave prison early |
| `/jailtime` | Player | Check remaining jail time |
| `/bounty list` | Player | View all active bounties |
| `/bounty place <player> <amount>` | Player | Place a bounty on a player |
| `/bounty info <player>` | Player | View bounty details |
| `/bounty history` | Player | View bounty history |

### Gang Commands (`GangCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/gang create <name>` | Player | Create a new gang |
| `/gang invite <player>` | Boss/Underboss | Invite a player to your gang |
| `/gang accept` | Player | Accept a gang invitation |
| `/gang leave` | Member | Leave your current gang |
| `/gang kick <player>` | Boss | Kick a member from the gang |
| `/gang promote <player>` | Boss | Promote a gang member |
| `/gang info` | Player | View gang information |
| `/gang list` | Player | List all gangs on the server |
| `/gang disband` | Boss | Disband the gang permanently |
| `/gang perk <perk>` | Boss | Activate a gang perk |
| `/gang admin setlevel <gang> <level>` | Admin | Set a gang's level |
| `/gang admin addxp <gang> <amount>` | Admin | Add XP to a gang |
| `/gang admin info <gang>` | Admin | View admin gang details |
| `/gang task editor` | Admin | Open the scenario/task editor |

### Lock Commands (`LockCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/lock code <code>` | Player | Enter a lock combination code |
| `/lock setcode <code>` | Owner | Set combination code for a lock |
| `/lock authorize <player>` | Owner | Authorize a player to use a lock |
| `/lock info` | Player | View lock information on the targeted block |
| `/lock remove` | Owner | Remove a lock from a door or container |
| `/lock list` | Player | List your locks |
| `/lock admin remove` | Admin | Force-remove any lock |

### Warehouse Commands (`WarehouseCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/warehouse info` | Admin | View warehouse statistics |
| `/warehouse add <item> <amount>` | Admin | Add items to a warehouse |
| `/warehouse remove <item> <amount>` | Admin | Remove items from a warehouse |
| `/warehouse clear` | Admin | Clear all warehouse inventory |
| `/warehouse setshop <shopId>` | Admin | Link warehouse to a shop plot |
| `/warehouse deliver` | Admin | Trigger an immediate warehouse delivery |
| `/warehouse reset` | Admin | Reset warehouse state |

### Market Commands (`MarketCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/market prices` | Player | View current dynamic market prices |
| `/market trends` | Player | View market price trends |
| `/market stats` | Player | View market statistics |
| `/market top` | Player | View top traded items |

### Utility Commands (`UtilityCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/utility` | Player | View utility consumption for your plot |
| `/utility top` | Player | View top utility consumers |
| `/utility scan` | Admin | Scan for utility consumers in area |
| `/utility stats` | Player | View detailed utility statistics |
| `/utility breakdown` | Player | View utility cost breakdown |
| `/strom` | Player | View electricity consumption and costs |
| `/wasser` | Player | View water consumption and costs |

### Map and Territory Commands (`MapCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/map edit` | Admin | Open the territory map editor |
| `/map info` | Player | View territory information |

### Admin and Health Commands (`HealthCommand.java`, `AdminCommand.java`)

| Command | Permission | Description |
|---|---|---|
| `/health` | Admin | Run system health check overview (all 38 systems) |
| `/health economy` | Admin | Economy subsystem health + backup info |
| `/health plot` | Admin | Plot subsystem health + cache stats + backups |
| `/health wallet` | Admin | Wallet subsystem health |
| `/health loan` | Admin | Loan system health |
| `/health creditloan` | Admin | Credit loan system health |
| `/health creditscore` | Admin | Credit score system health |
| `/health savings` | Admin | Savings account system health |
| `/health tax` | Admin | Tax system health |
| `/health overdraft` | Admin | Overdraft system health |
| `/health recurring` | Admin | Recurring payment system health |
| `/health shopaccount` | Admin | Shop account system health |
| `/health interest` | Admin | Interest system health |
| `/health crime` | Admin | Crime system health |
| `/health bounty` | Admin | Bounty system health |
| `/health npc` | Admin | NPC registry health |
| `/health prison` | Admin | Prison system health + prisoner count |
| `/health witness` | Admin | Witness system health |
| `/health dialogue` | Admin | NPC dialogue system health |
| `/health quest` | Admin | Quest system health |
| `/health companion` | Admin | Companion NPC system health |
| `/health faction` | Admin | Faction system health |
| `/health npcinteraction` | Admin | NPC interaction system health |
| `/health relationship` | Admin | NPC relationship system health |
| `/health worldevent` | Admin | World event system health |
| `/health dynamicprice` | Admin | Dynamic NPC price system health |
| `/health gang` | Admin | Gang system health |
| `/health territory` | Admin | Territory system health |
| `/health achievement` | Admin | Achievement system health |
| `/health daily` | Admin | Daily reward system health |
| `/health message` | Admin | Messaging system health |
| `/health gangmission` | Admin | Gang mission system health |
| `/health scenario` | Admin | Scenario system health |
| `/health lock` | Admin | Lock system health |
| `/health market` | Admin | Dynamic market health |
| `/health warehouse` | Admin | Warehouse system health |
| `/health towing` | Admin | Towing system health |
| `/health antiexploit` | Admin | Anti-exploit system health |
| `/health threadpool` | Admin | Thread pool infrastructure health |
| `/health backups` | Admin | Backup overview for all persistent files |
| `/health log` | Admin | Log health check to server console |
| `/admin setlevel <player> <level>` | Admin | Set a player's producer level |
| `/admin addxp <player> <amount>` | Admin | Add XP to a player |
| `/admin getlevel <player>` | Admin | Check a player's producer level |

---

## Items Reference

ScheduleMC adds **354 items** across all systems. Below is a categorized reference.

| Category | Count | Item Examples |
|---|---|---|
| **Economy** | 7+ | 1 Euro, 5 Euro, 10 Euro, 50 Euro, 100 Euro, 500 Euro bills; Bank Card |
| **Tools** | 6+ | Plot Selection Tool, Lock Pick, Copper Key Blank, Iron Key Blank, Netherite Key Blank, Scoring Knife |
| **NPC Tools** | 4 | NPC Spawner Tool, Location Tool, Leisure Tool, Patrol Tool |
| **Smartphone** | 1 | Smartphone |
| **Tobacco** | 32 | Seeds (4 strains), Tobacco Leaves (4 strains x 3 quality), Dried Tobacco, Cut Tobacco, Cigarettes, Cigars, Cigarette Packs (4 sizes), Rolling Paper, Lighter, Watering Can, Soil Bags (3 sizes), Fertilizer, Growth Booster, Quality Booster, Packaging Materials |
| **Cannabis** | 10 | Seeds (4 strains), Fresh Buds, Dried Cannabis, Trimmed Buds, Trim, Cured Cannabis, Ground Cannabis, Hash, Cannabis Oil, Cannabis Bags |
| **Coca** | 9 | Seeds (2 strains), Fresh Coca Leaves, Dried Leaves, Coca Paste, Refined Powder, Crack Rocks, Diesel Canister, Baking Soda |
| **Poppy** | 8 | Seeds (3 strains), Poppy Pods, Raw Latex, Pressed Opium, Morphine, Refined Product, Scoring Knife |
| **Meth** | 8 | Ephedrine, Pseudoephedrine, Red Phosphorus, Iodine, Meth Paste, Raw Meth, Crystal Meth, Packaged Product |
| **LSD** | 6 | Ergot Culture (Mutterkorn), Lysergic Acid, LSD Solution, Blotter Paper, Perforated Sheet, LSD Blotter |
| **MDMA** | 6 | Safrole, MDMA Base, MDMA Crystals, Ecstasy Pills, Binder, Dye |
| **Mushroom** | 15 | Spore Syringes (3 strains), Substrate, Mist Bags (3 sizes x 3 strains), Fresh Mushrooms (3 strains), Dried Mushrooms (3 strains) |
| **Coffee** | 20+ | Coffee Seeds (4 strains), Coffee Cherries (4 strains), Green Beans, Roasted Beans (4 roast levels), Ground Coffee (4 grind sizes), Coffee Cups, Packaging (3 sizes) |
| **Wine** | 18+ | Grape Seeds (4 varieties), Grapes (4 varieties), Grape Must, Wine (4 varieties x sweetness levels), Wine Bottles, Corks |
| **Cheese** | 16+ | Milk Buckets, Pasteurized Milk, Rennet, Cheese Curds (quality variants), Cheese Wheels (4 types), Cheese Wedges (4 types), Packaging |
| **Honey** | 19+ | Honeycombs (4 types), Raw Honey Buckets, Beeswax, Liquid Honey, Creamy Honey, Chunk Honey, Honey Jars (4 types), Equipment |
| **Chocolate** | 18+ | Cocoa Pods, Raw Cocoa Beans, Roasted Beans, Cocoa Nibs, Cocoa Mass, Cocoa Butter, Conched Chocolate, Tempered Chocolate, Chocolate Bars (4 types), Wrapped Bars |
| **Beer** | 20+ | Grain (various types), Wort Buckets, Hops (4 types: fresh, dried, extract, pellets), Yeast (4 types), Fermenting Beer, Green Beer, Conditioned Beer, Beer Bottles |
| **Vehicle** | 40+ | Chassis (5), Engines (3), Tires (6), Fenders (3), Fuel Tanks (3), Modules (3), Diesel Can (Empty/Full), Maintenance Kit, Vehicle Key, Battery, Spawn Tool, License Plate |
| **TOTAL** | **354** | |

---

## Blocks Reference

ScheduleMC adds **152 blocks** across all systems.

| Category | Count | Block Examples |
|---|---|---|
| **Tobacco** | 21 | Tobacco Pots (4 tiers: Terracotta/Ceramic/Iron/Golden), Tobacco Plants (4 strains), Drying Racks (3 sizes), Fermentation Barrels (3 sizes), Packaging Tables (3 sizes), Grow Lights (3 tiers), Sink |
| **Cannabis** | 9 | Cannabis Plants (4 strains), Drying Net, Trimming Station, Curing Jar, Hash Press, Oil Extractor |
| **Coca** | 9 | Coca Plants (2 strains), Extraction Vats (3 sizes), Refineries (3 sizes), Crack Cooker |
| **Poppy** | 7 | Poppy Plants (3 strains), Scoring Machine, Opium Press, Cooking Station, Heroin Refinery |
| **Meth** | 4 | Chemistry Mixer, Reduction Vessel, Crystallizer, Vacuum Dryer |
| **LSD** | 4 | Fermentation Tank, Distillation Apparatus, Micro Dosing Station, Perforation Press |
| **MDMA** | 3 | Reaction Vessel, Drying Oven, Pill Press |
| **Mushroom** | 4 | Climate Lamps (3 tiers), Water Tank |
| **Coffee** | 17 | Coffee Pots (4 tiers), Coffee Plants (4 strains), Wet Processing Station, Drying Trays (3 sizes), Coffee Roasters (3 sizes), Coffee Grinder, Packaging Table |
| **Wine** | 16 | Grapevine, Grapevine Pots (4 varieties), Crushing Station, Wine Presses (3 sizes), Fermentation Tanks (3 sizes), Aging Barrels (3 sizes), Bottling Station |
| **Cheese** | 9 | Pasteurization Station, Curdling Vat, Cheese Presses (3 sizes), Aging Caves (3 sizes), Packaging Station |
| **Honey** | 14 | Beehives (3 tiers: Basic/Advanced/Apiary), Extractors (2: Standard/Centrifugal), Filtering Station, Processing Station, Creaming Station, Aging Chambers (3 sizes), Bottling Station, Storage Barrel, Display Case |
| **Chocolate** | 15 | Roasting Station, Winnowing Machine, Grinding Mill, Pressing Station, Conching Machines (3 sizes), Tempering Station, Molding Stations (3 sizes), Cooling Tunnel, Enrobing Machine, Wrapping Station, Storage Cabinet |
| **Beer** | 12 | Malting Station, Mash Tun, Brew Kettles (3 sizes), Fermentation Tanks (3 sizes), Conditioning Tanks (3 sizes), Bottling Station |
| **Economy** | 2 | ATM Block, Cash Block |
| **Plot** | 1 | Plot Info Block |
| **Warehouse** | 1 | Warehouse Block |
| **Vehicle** | 4 | Fuel Station, Fuel Station Top, Garage (Werkstatt), Bio Diesel Fluid |
| **TOTAL** | **152** | |

---

## API Overview

ScheduleMC exposes a comprehensive public API through the `ScheduleMCAPI` singleton class, providing 12 modules for external mod integration. The API uses a double-checked locking singleton pattern for thread-safe initialization and interface-based abstractions for clean separation between API contracts and internal implementations.

**API Entry Point:** `de.rolandsw.schedulemc.api.ScheduleMCAPI`

### API Modules

| # | Module | Interface | Package | Description |
|---|---|---|---|---|
| 1 | Economy | `IEconomyAPI` | `api.economy` | Account management, deposits, withdrawals, transfers, balance queries |
| 2 | Plot | `IPlotAPI` | `api.plot` | Plot queries, ownership checks, protection status |
| 3 | Production | `IProductionAPI` | `api.production` | Production chain access, custom plant registration |
| 4 | NPC | `INPCAPI` | `api.npc` | NPC spawning, schedule management, dialogue, quests |
| 5 | Police | `IPoliceAPI` | `api.police` | Wanted level management, crime detection, prison |
| 6 | Warehouse | `IWarehouseAPI` | `api.warehouse` | Warehouse inventory, deliveries, stock queries |
| 7 | Messaging | `IMessagingAPI` | `api.messaging` | Player-to-player and system message sending |
| 8 | Smartphone | `ISmartphoneAPI` | `api.smartphone` | Custom app registration, push notifications |
| 9 | Vehicle | `IVehicleAPI` | `api.vehicle` | Vehicle spawning, fuel management, damage, customization |
| 10 | Achievement | `IAchievementAPI` | `api.achievement` | Achievement registration, progress tracking, rewards |
| 11 | Market | `IMarketAPI` | `api.market` | Price queries, supply/demand data, market events |
| 12 | PlotModAPI (legacy) | `PlotModAPI` | `api` | Legacy plot API for backward compatibility |

All API implementations reside in the `api.impl` package and are registered during mod initialization.

### Code Examples

**Getting the API Instance and Checking Status:**

```java
import de.rolandsw.schedulemc.api.ScheduleMCAPI;

ScheduleMCAPI api = ScheduleMCAPI.getInstance();

// Check if all subsystems are initialized
if (api.isInitialized()) {
    System.out.println("ScheduleMC API v" + api.getVersion() + " is ready!");
    System.out.println(api.getStatus());  // Prints status of each subsystem
}
```

**Economy API -- Managing Player Funds:**

```java
import de.rolandsw.schedulemc.api.economy.IEconomyAPI;
import java.util.UUID;

IEconomyAPI economy = ScheduleMCAPI.getInstance().getEconomyAPI();
UUID playerUUID = player.getUUID();

// Deposit 1000 Euro into a player's bank account
economy.deposit(playerUUID, 1000.0);

// Check balance
double balance = economy.getBalance(playerUUID);

// Transfer between players
economy.transfer(senderUUID, receiverUUID, 500.0);
```

**Plot API -- Querying Plot Information:**

```java
import de.rolandsw.schedulemc.api.plot.IPlotAPI;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.core.BlockPos;
import java.util.Optional;

IPlotAPI plots = ScheduleMCAPI.getInstance().getPlotAPI();

// Get the plot at a specific world position
Optional<PlotRegion> plot = plots.getPlotAt(new BlockPos(100, 64, 200));

plot.ifPresent(p -> {
    System.out.println("Plot: " + p.getName());
    System.out.println("Type: " + p.getType());
    System.out.println("Owner: " + p.getOwner());
});
```

**Production API -- Registering a Custom Plant:**

```java
import de.rolandsw.schedulemc.api.production.IProductionAPI;

IProductionAPI production = ScheduleMCAPI.getInstance().getProductionAPI();

// Register a custom plant type for your addon mod
production.registerCustomPlant(/* your plant definition */);
```

**Police API -- Managing Wanted Levels:**

```java
import de.rolandsw.schedulemc.api.police.IPoliceAPI;

IPoliceAPI police = ScheduleMCAPI.getInstance().getPoliceAPI();

// Get a player's current wanted level (0-5 stars)
int stars = police.getWantedLevel(playerUUID);

// Add wanted stars for a crime
police.addWantedLevel(playerUUID, 2);

// Clear wanted level (admin action)
police.clearWantedLevel(playerUUID);
```

**Market API -- Querying Dynamic Prices:**

```java
import de.rolandsw.schedulemc.api.market.IMarketAPI;

IMarketAPI market = ScheduleMCAPI.getInstance().getMarketAPI();

// Get the current market price for an item
// Price includes supply/demand multiplier (0.5x to 2.0x base)
double price = market.getCurrentPrice(item);
```

**Smartphone API -- Registering a Custom App:**

```java
import de.rolandsw.schedulemc.api.smartphone.ISmartphoneAPI;

ISmartphoneAPI smartphone = ScheduleMCAPI.getInstance().getSmartphoneAPI();

// Register a custom smartphone app from your addon mod
smartphone.registerApp(myCustomApp);
```

**NPC API -- Spawning and Configuring NPCs:**

```java
import de.rolandsw.schedulemc.api.npc.INPCAPI;

INPCAPI npcs = ScheduleMCAPI.getInstance().getNPCAPI();

// Spawn a merchant NPC at a location
npcs.spawnNPC(worldPosition, NPCType.MERCHANT, "Shop Owner Hans");
```

---

## Development Guide

### Prerequisites

- **Java Development Kit (JDK) 17** or later ([Adoptium](https://adoptium.net/) recommended)
- **Gradle** (wrapper included via `gradlew` -- no separate installation needed)
- **Git** for version control
- **IDE**: IntelliJ IDEA or Eclipse (both supported via Gradle IDE plugins)
- **RAM**: At least 3 GB for Gradle builds (`org.gradle.jvmargs=-Xmx3G`), 2 GB for tests

### Building from Source

```bash
# Clone the repository
git clone https://github.com/Minecraft425HD/ScheduleMC.git
cd ScheduleMC

# Build the mod (generates the JAR in build/libs/)
./gradlew build

# The compiled JAR will be located at:
# build/libs/schedulemc-3.6.0-beta.jar
```

### Running in Development

```bash
# Set up IDE workspace
./gradlew eclipse          # For Eclipse
# For IntelliJ IDEA: import as Gradle project directly

# Run the Minecraft client in development mode
./gradlew runClient

# Run the Minecraft server in development mode (--nogui)
./gradlew runServer

# Run data generators (outputs to src/generated/resources/)
./gradlew runData

# Run Forge game test server (automated in-game tests)
./gradlew runGameTestServer

# Run unit tests
./gradlew test

# Run tests with JaCoCo coverage report
./gradlew test jacocoTestReport
# HTML report: build/reports/jacoco/test/html/index.html

# Run full check (tests + coverage verification)
./gradlew check
```

### Project Structure

```
ScheduleMC/
|-- build.gradle                         # Build config: Forge, deps, JaCoCo, test settings
|-- gradle.properties                    # Mod metadata: version, ID, author, description
|-- settings.gradle                      # Gradle plugin management
|-- gradlew / gradlew.bat               # Gradle wrapper scripts
|-- LICENSE                              # GNU General Public License v3.0
|-- update.json                          # Forge update checker metadata
|-- docs/                                # Documentation (German)
|-- wiki/                                # Wiki pages
|-- libs/                                # Local library dependencies (flatDir)
|-- src/
|   |-- main/
|   |   |-- java/de/rolandsw/schedulemc/
|   |   |   |-- ScheduleMC.java              # Main mod entry point (@Mod annotation)
|   |   |   |-- ModCreativeTabs.java         # Creative mode tab registration
|   |   |   |-- package-info.java            # Package documentation
|   |   |   |
|   |   |   |-- api/                         # === PUBLIC API (12 modules) ===
|   |   |   |   |-- ScheduleMCAPI.java       #   Central API singleton (thread-safe)
|   |   |   |   |-- PlotModAPI.java          #   Legacy plot API
|   |   |   |   |-- economy/                 #   IEconomyAPI interface
|   |   |   |   |-- plot/                    #   IPlotAPI interface
|   |   |   |   |-- production/              #   IProductionAPI interface
|   |   |   |   |-- npc/                     #   INPCAPI interface
|   |   |   |   |-- police/                  #   IPoliceAPI interface
|   |   |   |   |-- warehouse/               #   IWarehouseAPI interface
|   |   |   |   |-- messaging/               #   IMessagingAPI interface
|   |   |   |   |-- smartphone/              #   ISmartphoneAPI interface
|   |   |   |   |-- vehicle/                 #   IVehicleAPI interface
|   |   |   |   |-- achievement/             #   IAchievementAPI interface
|   |   |   |   |-- market/                  #   IMarketAPI interface
|   |   |   |   |-- impl/                    #   All API implementation classes
|   |   |   |
|   |   |   |-- economy/                     # === ECONOMY SYSTEM (16 managers) ===
|   |   |   |   |-- EconomyManager.java      #   Core balance operations
|   |   |   |   |-- WalletManager.java       #   Physical cash management
|   |   |   |   |-- LoanManager.java         #   Loan issuance and repayment
|   |   |   |   |-- CreditLoanManager.java   #   Credit-based loans
|   |   |   |   |-- CreditScoreManager.java  #   Credit score tracking
|   |   |   |   |-- SavingsAccountManager.java  # Savings with interest
|   |   |   |   |-- TaxManager.java          #   Tax collection
|   |   |   |   |-- InterestManager.java     #   Interest calculations
|   |   |   |   |-- OverdraftManager.java    #   Overdraft handling
|   |   |   |   |-- RecurringPaymentManager.java  # Auto-pay
|   |   |   |   |-- ShopAccountManager.java  #   Shop finances
|   |   |   |   |-- (30+ more classes)       #   Controllers, events, network, etc.
|   |   |   |   |-- blockentity/             #   ATM, cash register block entities
|   |   |   |   |-- blocks/                  #   ATM, shop counter blocks
|   |   |   |   |-- commands/                #   Economy command handlers
|   |   |   |   |-- events/                  #   Economy event handlers
|   |   |   |   |-- items/                   #   Cash items, bank cards
|   |   |   |   |-- menu/                    #   ATM and shop menus
|   |   |   |   |-- network/                 #   Economy network packets
|   |   |   |   |-- screen/                  #   Economy GUI screens
|   |   |   |
|   |   |   |-- region/                      # === PLOT MANAGEMENT ===
|   |   |   |   |-- PlotManager.java         #   Central CRUD + persistence
|   |   |   |   |-- PlotSpatialIndex.java    #   O(1) chunk-based spatial lookups
|   |   |   |   |-- PlotCache.java           #   LRU cache (plot-level)
|   |   |   |   |-- PlotChunkCache.java      #   LRU cache (chunk-level)
|   |   |   |   |-- PlotProtectionHandler.java  # Block/interaction protection
|   |   |   |   |-- PlotRegion.java          #   Region coordinates
|   |   |   |   |-- PlotArea.java            #   Area abstraction
|   |   |   |   |-- PlotType.java            #   7 plot types enum
|   |   |   |   |-- blocks/                  #   Plot info blocks
|   |   |   |   |-- network/                 #   Plot network packets
|   |   |   |
|   |   |   |-- npc/                         # === NPC SYSTEM ===
|   |   |   |   |-- entity/                  #   CustomNPCEntity, NPCEntities
|   |   |   |   |-- goals/                   #   6 custom behavior goals
|   |   |   |   |-- personality/             #   Personality traits
|   |   |   |   |-- crime/                   #   Crime detection, bounty, prison
|   |   |   |   |-- life/                    #   Daily life simulation
|   |   |   |   |-- bank/                    #   NPC wallet and banking
|   |   |   |   |-- data/                    #   NPC data persistence
|   |   |   |   |-- client/                  #   NPC client rendering
|   |   |   |   |-- commands/                #   NPC admin commands
|   |   |   |   |-- events/                  #   NPC event handlers
|   |   |   |   |-- items/                   #   NPC admin tools
|   |   |   |   |-- menu/                    #   NPC shop menus
|   |   |   |   |-- network/                 #   NPC network packets
|   |   |   |   |-- pathfinding/             #   Custom NPC pathfinding
|   |   |   |
|   |   |   |-- tobacco/                     # === TOBACCO (32 items, 23 blocks) ===
|   |   |   |-- cannabis/                    # === CANNABIS (10 items, 9 blocks) ===
|   |   |   |-- coca/                        # === COCA (9 items, 9 blocks) ===
|   |   |   |-- poppy/                       # === POPPY (8 items, 7 blocks) ===
|   |   |   |-- meth/                        # === METH (8 items, 4 blocks) ===
|   |   |   |-- lsd/                         # === LSD (6 items, 4 blocks) ===
|   |   |   |-- mdma/                        # === MDMA (6 items, 3 blocks) ===
|   |   |   |-- mushroom/                    # === MUSHROOM (15 items, 4 blocks) ===
|   |   |   |-- coffee/                      # === COFFEE (legal) ===
|   |   |   |-- wine/                        # === WINE (legal) ===
|   |   |   |-- cheese/                      # === CHEESE (legal) ===
|   |   |   |-- honey/                       # === HONEY (legal) ===
|   |   |   |-- chocolate/                   # === CHOCOLATE (legal) ===
|   |   |   |-- beer/                        # === BEER (legal) ===
|   |   |   |   (each with items/, blocks/, blockentity/, menu/, network/ sub-packages)
|   |   |   |
|   |   |   |-- production/                  # === SHARED PRODUCTION FRAMEWORK ===
|   |   |   |   |-- core/                    #   Base production logic
|   |   |   |   |-- growth/                  #   Growth stage management
|   |   |   |   |-- config/                  #   Production configuration
|   |   |   |   |-- data/                    #   Serialization
|   |   |   |   |-- items/                   #   Base item classes
|   |   |   |   |-- blocks/                  #   Base block classes
|   |   |   |   |-- blockentity/             #   Base block entity classes
|   |   |   |   |-- nbt/                     #   NBT serialization
|   |   |   |   |-- ProductionSize.java      #   Size variants enum
|   |   |   |
|   |   |   |-- vehicle/                     # === VEHICLE SYSTEM (137 files) ===
|   |   |   |   |-- entity/                  #   Vehicle entities
|   |   |   |   |-- vehicle/                 #   Vehicle data models
|   |   |   |   |-- blocks/                  #   Fuel stations, garages
|   |   |   |   |-- items/                   #   Parts, fuel, tools
|   |   |   |   |-- gui/                     #   Vehicle GUIs
|   |   |   |   |-- fuel/                    #   Fuel system
|   |   |   |   |-- sounds/                  #   Sound events
|   |   |   |   |-- net/                     #   Network packets
|   |   |   |   |-- events/                  #   Event handlers
|   |   |   |   |-- util/                    #   Utilities
|   |   |   |   |-- fluids/                  #   Fuel fluids
|   |   |   |   |-- mixins/                  #   Render mixins
|   |   |   |
|   |   |   |-- mapview/                     # === MAPVIEW SYSTEM (122 files) ===
|   |   |   |-- gang/                        # === GANG SYSTEM ===
|   |   |   |-- territory/                   # === TERRITORY SYSTEM ===
|   |   |   |-- lock/                        # === LOCK SYSTEM (5 types) ===
|   |   |   |-- towing/                      # === TOWING SYSTEM ===
|   |   |   |-- level/                       # === LEVEL/XP SYSTEM ===
|   |   |   |-- utility/                     # === UTILITY SYSTEM (water/electricity) ===
|   |   |   |-- warehouse/                   # === WAREHOUSE SYSTEM ===
|   |   |   |-- messaging/                   # === MESSAGING SYSTEM ===
|   |   |   |-- achievement/                 # === ACHIEVEMENT SYSTEM ===
|   |   |   |-- market/                      # === DYNAMIC MARKET ===
|   |   |   |-- player/                      # === PLAYER TRACKING ===
|   |   |   |
|   |   |   |-- client/                      # === CLIENT-SIDE ===
|   |   |   |   |-- ClientModEvents.java     #   Client event registration
|   |   |   |   |-- KeyBindings.java         #   Key binding registration
|   |   |   |   |-- PlotInfoHudOverlay.java  #   Plot info HUD
|   |   |   |   |-- WantedLevelOverlay.java  #   Wanted stars HUD
|   |   |   |   |-- TobaccoPotHudOverlay.java  # Tobacco pot HUD
|   |   |   |   |-- SmartphoneKeyHandler.java  # Phone keybind
|   |   |   |   |-- SmartphonePlayerHandler.java
|   |   |   |   |-- SmartphoneProtectionHandler.java
|   |   |   |   |-- SmartphoneTracker.java
|   |   |   |   |-- QualityItemColors.java   #   Quality-based item tinting
|   |   |   |   |-- UpdateNotificationHandler.java
|   |   |   |   |-- screen/                  #   GUI screens
|   |   |   |   |   |-- SmartphoneScreen.java  # Main phone GUI
|   |   |   |   |   |-- CombinationLockScreen.java
|   |   |   |   |   |-- ConfirmDialogScreen.java
|   |   |   |   |   |-- InputDialogScreen.java
|   |   |   |   |   |-- apps/               #   Smartphone app screens (18+)
|   |   |   |   |-- network/                 #   Client network handlers
|   |   |   |
|   |   |   |-- gui/                         # === ADDITIONAL GUIs ===
|   |   |   |   |-- PlotMenuGUI.java         #   Plot management GUI
|   |   |   |
|   |   |   |-- network/                     # === CORE NETWORKING (100 packets) ===
|   |   |   |   |-- AbstractPacket.java      #   Base packet class
|   |   |   |
|   |   |   |-- commands/                    # === COMMAND REGISTRATION ===
|   |   |   |   |-- AdminCommand.java
|   |   |   |   |-- CommandExecutor.java
|   |   |   |   |-- HealthCommand.java
|   |   |   |   |-- MoneyCommand.java
|   |   |   |   |-- PlotCommand.java
|   |   |   |
|   |   |   |-- config/                      # === CONFIGURATION ===
|   |   |   |   |-- ModConfigHandler.java    #   Config file management
|   |   |   |
|   |   |   |-- events/                      # === CORE EVENT HANDLERS ===
|   |   |   |   |-- BlockProtectionHandler.java
|   |   |   |   |-- InventoryRestrictionHandler.java
|   |   |   |
|   |   |   |-- managers/                    # === SYSTEM MANAGERS ===
|   |   |   |   |-- DailyRewardManager.java
|   |   |   |   |-- NPCEntityRegistry.java
|   |   |   |   |-- NPCNameRegistry.java
|   |   |   |   |-- RentManager.java
|   |   |   |
|   |   |   |-- items/                       # === CORE ITEMS ===
|   |   |   |   |-- ModItems.java            #   Item registration
|   |   |   |   |-- PlotSelectionTool.java   #   Plot wand tool
|   |   |   |
|   |   |   |-- data/                        # === DATA GENERATION ===
|   |   |   |-- util/                        # === UTILITY CLASSES ===
|   |   |       |-- EventHelper.java         #   Safe event handling
|   |   |       |-- ThreadPoolManager.java   #   Async thread pool
|   |   |       |-- HealthCheckManager.java  #   System health monitoring
|   |   |       |-- IncrementalSaveManager.java  # Efficient persistence
|   |   |
|   |   |-- resources/                       # === 1,206+ RESOURCE FILES ===
|   |       |-- META-INF/mods.toml           #   Forge mod descriptor
|   |       |-- pack.mcmeta                  #   Resource pack metadata
|   |       |-- assets/schedulemc/           #   Client assets
|   |       |   |-- lang/                    #   Language files (DE, EN)
|   |       |   |-- models/                  #   Item and block models
|   |       |   |-- textures/                #   Textures (items, blocks, GUI)
|   |       |   |-- blockstates/             #   Block state definitions
|   |       |   |-- sounds/                  #   Sound files
|   |       |-- data/schedulemc/             #   Server data
|   |           |-- loot_tables/             #   Block drop tables
|   |           |-- recipes/                 #   Crafting recipes
|   |           |-- tags/                    #   Item and block tags
|   |
|   |-- test/
|   |   |-- java/de/rolandsw/schedulemc/    # === 19 TEST FILES, 292 UNIT TESTS ===
|   |       |-- commands/                    #   Command tests
|   |       |-- economy/                     #   Economy manager tests
|   |       |-- production/                  #   Production chain tests
|   |       |-- region/                      #   Plot/spatial index tests
|   |       |-- util/                        #   Utility class tests
|   |       |-- integration/                 #   Cross-system integration tests
|   |       |-- test/                        #   Test infrastructure/bootstrap
|   |
|   |-- generated/resources/                 # === DATA GENERATOR OUTPUT ===
```

### Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Primary programming language (Mojang ships Java 17 with MC 1.18+) |
| Minecraft Forge | 47.4.0 | Mod loader, event bus, registry system, networking |
| Minecraft | 1.20.1 | Target game version |
| CoreLib | 1.20.1-1.1.1 | OBJ model rendering, GUI utilities, networking helpers |
| Gson | 2.10.1 | JSON serialization/deserialization for data persistence |
| Mixin | 0.8.5 | Bytecode modification for MapView rendering integration |
| MixinExtras | 0.5.0 | Enhanced Mixin features (expression targets, wrappers) |
| JUnit 5 (Jupiter) | 5.10.1 | Unit testing framework with parameterized tests |
| Mockito | 5.8.0 | Mocking framework with JUnit 5 extension |
| AssertJ | 3.24.2 | Fluent assertion library for readable test assertions |
| JaCoCo | 0.8.11 | Code coverage analysis with HTML/XML reporting |
| Gradle | 6.x+ | Build system with ForgeGradle plugin |
| Official Mappings | 1.20.1 | Mojang official field/method name mappings |

### Design Patterns

The codebase employs the following design patterns consistently across all systems:

| Pattern | Usage | Examples |
|---|---|---|
| **Singleton** | Thread-safe single instances for central managers | `ScheduleMCAPI` (double-checked locking), `EconomyManager`, `PlotManager`, `CrimeManager` |
| **Observer / Event Bus** | Decoupled cross-system communication via Forge events | `PlayerJoinHandler`, `BlockProtectionHandler`, `UtilityEventHandler`, `RespawnHandler`, `NPCStealingHandler`, `BusinessMetricsUpdateHandler` |
| **Strategy** | Interchangeable algorithms for NPC behavior and economy | 9 NPC AI goal strategies (`MoveToHomeGoal`, `PolicePatrolGoal`, etc.) + 5 behavior actions, `EconomyCyclePhase` strategies |
| **Factory** | Object creation for entities, items, and vehicles | NPC entity creation, vehicle component assembly, lock creation by type |
| **Manager / Service Layer** | Encapsulated business logic in 55 dedicated managers | `EconomyManager`, `PlotManager`, `CrimeManager`, `BountyManager`, `LoanManager`, `GangManager`, `TerritoryManager`, `TowingYardManager`, etc. |
| **Repository** | Data persistence abstraction | Plot data storage, NPC data, economy records, crime records |
| **Command Pattern** | Forge command framework | `CommandExecutor` base, `PlotCommand`, `MoneyCommand`, `AdminCommand`, `HealthCommand` |
| **State** | State machine transitions | NPC schedule states, `EconomyCycle` phases, wanted level states, production growth stages |
| **Spatial Index** | Efficient geometric queries | `PlotSpatialIndex` for O(1) chunk-based plot containment lookups |
| **Cache (LRU)** | High-frequency data access optimization | `PlotCache` (plot-level), `PlotChunkCache` (chunk-level) |
| **Facade** | Unified interface over complex subsystems | `ScheduleMCAPI` exposing 12 subsystem APIs through a single entry point |
| **Bridge** | Connecting independent system hierarchies | `WarehouseMarketBridge` linking warehouse stock to market pricing |
| **Rate Limiter** | Throttling to prevent abuse | `RateLimiter` in economy preventing transaction spam |
| **Template Method** | Shared base logic with customizable steps | Production system base classes with chain-specific overrides |
| **Registry** | Centralized object registration | Forge `DeferredRegister` for items, blocks, entities; `NPCEntityRegistry`, `NPCNameRegistry`, `UtilityRegistry`, `TowingServiceRegistry` |
| **Incremental Save** | Efficient periodic persistence | `IncrementalSaveManager` saving only changed data |
| **Thread Pool** | Asynchronous background operations | `ThreadPoolManager` for non-blocking tasks |
| **Abstract Packet** | Network communication base | `AbstractPacket` providing shared serialization for 100 packet types |

---

## Testing and Quality Assurance

ScheduleMC maintains a comprehensive test suite to ensure reliability across all major systems.

**Test Infrastructure:**

| Component | Details |
|---|---|
| Framework | JUnit 5 (Jupiter) 5.10.1 with parameterized test support |
| Mocking | Mockito 5.8.0 with `mockito-junit-jupiter` extension |
| Assertions | AssertJ 3.24.2 (fluent assertion chains) |
| Coverage | JaCoCo 0.8.11 (HTML + XML reports) |
| Test Files | 19 |
| Total Tests | 292 |
| Test Memory | 2 GB max heap (`maxHeapSize = '2G'`) |
| Test Logging | Passed, skipped, and failed events with full exception format |

**Coverage Requirements:**

| Scope | Minimum Coverage | Rationale |
|---|---|---|
| Global (all classes) | 60% line coverage | Baseline quality threshold |
| Utility classes (`de.rolandsw.schedulemc.util.*`) | 80% line coverage | Critical shared code requires higher coverage |

**Coverage Exclusions** (classes excluded from coverage requirements):
- Generated code (`**/generated/**`)
- Minecraft event handlers (`**/*Event*.class`) -- require running game server
- GUI classes (`**/client/gui/**`) -- require rendering context
- Block entities (`**/blockentity/**`) -- require Minecraft world instance

**Running Tests:**

```bash
# Run all unit tests
./gradlew test

# Run tests and generate JaCoCo coverage report
./gradlew test jacocoTestReport

# View the HTML coverage report
# Location: build/reports/jacoco/test/html/index.html

# Run coverage verification (enforces 60%/80% minimums)
./gradlew check

# Note: 'check' depends on 'jacocoTestCoverageVerification'
# which depends on 'test', so this runs everything
```

**Test Categories:**

| Category | Directory | What Is Tested |
|---|---|---|
| Command Tests | `src/test/.../commands/` | Command parsing, argument validation, execution logic |
| Economy Tests | `src/test/.../economy/` | Transaction processing, balance operations, loans, savings, tax, interest, anti-exploit |
| Production Tests | `src/test/.../production/` | Growth cycles, quality calculations, chain validation, serialization |
| Region Tests | `src/test/.../region/` | Plot CRUD, spatial index correctness, cache behavior, protection |
| Utility Tests | `src/test/.../util/` | Helper functions, data structures, event handling |
| Integration Tests | `src/test/.../integration/` | Cross-system interaction (economy + NPC, production + market) |
| Test Infrastructure | `src/test/.../test/` | Bootstrap classes for test environment setup |

**Built-In Health Check System:**

The mod includes a runtime health monitoring system (`HealthCheckManager`) accessible via the `/health` command. The health check monitors **38 subsystems** organized in 7 categories:

- **Kern-Systeme (3):** Economy, Plot, Wallet
- **Finanz-Systeme (9):** Loan, Credit Loan, Credit Score, Savings, Tax, Overdraft, Recurring Payments, Shop Accounts, Interest
- **NPC & Crime (5):** Crime, Bounty, NPC Registry, Prison, Witness
- **NPC Life (8):** Dialogue, Quest, Companion, Faction, NPC Interaction, NPC Relationship, World Event, Dynamic Price
- **Spieler-Systeme (7):** Gang, Territory, Achievement, Daily Reward, Messaging, Gang Mission, Scenario
- **Welt-Systeme (4):** Lock, Dynamic Market, Warehouse, Towing
- **Infrastruktur (2):** AntiExploit, ThreadPool

Each system reports HEALTHY, DEGRADED, or UNHEALTHY status. Use `/health <system>` for details on a specific subsystem, or `/health` for the full overview.
- Economy state is consistent (no negative balances, valid transactions)

**Quality Assurance Features:**

| Feature | Implementation | Description |
|---|---|---|
| Automatic Backups | `IncrementalSaveManager` | Creates backups before every save operation |
| Corruption Recovery | Backup restoration | Auto-restores from last known good backup on load failure |
| Graceful Degradation | `EventHelper` | Individual system failures do not crash the entire mod |
| Error Isolation | Try-catch wrappers | Event handlers are isolated to prevent cascade failures |
| Atomic Writes | Temp file + rename | Save files are written atomically to prevent corruption |
| Input Validation | All command handlers | Commands validate all arguments before execution |
| Rate Limiting | `RateLimiter` | Prevents exploit attempts through rapid API calls |
| Anti-Exploit | `AntiExploitManager` | Detects and blocks common economy exploitation patterns |

---

## Troubleshooting / FAQ

**Q: The mod fails to load with a missing dependency error for CoreLib.**
A: CoreLib is a required dependency. Download [CoreLib 1.20.1-1.1.1](https://maven.maxhenkel.de/repository/public/de/maxhenkel/corelib/) and place the JAR file in your `mods/` folder alongside the ScheduleMC JAR. CoreLib provides essential functionality for OBJ model rendering, GUI systems, and networking.

**Q: Players cannot interact with blocks on their own plot.**
A: Verify the plot is correctly defined and the player is listed as the owner or member. Use `/plot debug` while standing on the plot to check ownership and boundaries. If the issue persists, check the server logs for `PlotProtectionHandler` messages. You can also run `/health plot` to verify the plot subsystem is healthy.

**Q: NPCs are standing still and not following their schedules.**
A: NPC schedules use HHMM format (e.g., `0800` for 8:00 AM, `1430` for 2:30 PM). Verify that schedule entries are set correctly with `/npc <name> info`. Additionally, ensure NPCs have valid home, work, and leisure locations set, and that pathfinding routes between those locations are not blocked by obstacles.

**Q: The economy system shows incorrect or inconsistent balances.**
A: Run `/health economy` to verify the economy subsystem is healthy. Check server logs for `AntiExploitManager` messages that might indicate blocked transactions. If a player's balance appears corrupted, admins can reset it with `/money set <player> <amount>`. The `TransactionHistory` can be reviewed to trace discrepancies.

**Q: A meth production block exploded and destroyed my base.**
A: This is intended behavior. The Reduction Vessel (Reduktionskessel) has a configurable chance of explosion during the reduction step. The explosion destroys the block and damages surrounding blocks in a radius. Always build meth labs in isolated locations away from valuable structures. The explosion risk is part of the risk/reward balance for methamphetamine production.

**Q: Vehicles are invisible or rendering incorrectly.**
A: Ensure CoreLib 1.20.1-1.1.1 is installed and matches the expected version. Vehicle models use OBJ format rendered through CoreLib's model system. If models appear invisible or distorted, try: (1) verifying CoreLib is the correct version, (2) reinstalling both mods, and (3) clearing your Minecraft shader cache. Check client logs for rendering errors.

**Q: The dynamic market shows all prices at 1.0x multiplier.**
A: The market system needs actual buy/sell transaction data to adjust prices. On a new server, all prices start at their base values (1.0x multiplier). As players buy and sell items, supply and demand data accumulates and prices will diverge from base values. The multiplier range is 0.5x to 2.0x.

**Q: How do I reset all mod data for a completely fresh start?**
A: Delete the `data/schedulemc/` directory inside your world save folder, then restart the server. This removes all economy, plot, NPC, crime, gang, territory, warehouse, and production data. Always create a backup of the world before doing this.

**Q: The smartphone screen is black or unresponsive.**
A: This is typically a client-side rendering issue. Verify you have the exact same mod version on both client and server. Try pressing the smartphone keybind again to close and reopen the interface. If the problem persists, check client logs for errors related to `SmartphoneScreen` or `SmartphoneNetworkHandler`.

**Q: Lock picking always fails on High Security and Dual locks.**
A: High Security locks have only a 10% pick success chance, and Dual Locks are even harder at 5%. This is by design to make these lock tiers meaningful security upgrades. Additionally, failed pick attempts on High Security and Dual Locks trigger an alarm that increases the picker's wanted level via the police system.

**Q: The Gradle build fails with OutOfMemoryError.**
A: Increase Gradle's JVM memory allocation in `gradle.properties`. The default is `-Xmx3G`. For machines with limited RAM, ensure at least 3 GB is available for the build process. Tests allocate up to 2 GB (`maxHeapSize = '2G'`). You may need `-Xmx4G` or higher if building alongside other memory-intensive processes.

**Q: How do I integrate my own mod with the ScheduleMC API?**
A: Add ScheduleMC as a `compileOnly` dependency in your `build.gradle`, then access the API at runtime via `ScheduleMCAPI.getInstance()`. Always check `isInitialized()` before calling any subsystem API, as ScheduleMC may initialize after your mod depending on load order. See the [API Overview](#api-overview) section for detailed code examples covering all 12 modules.

**Q: JEI/Jade/The One Probe integration features are not appearing.**
A: These mods are optional dependencies (`compileOnly` in the build). Ensure you have the correct compatible versions installed: JEI 15.2.0.27, Jade 11.8.0, or The One Probe 1.20.1-10.0.2. Integration is automatic when these mods are detected at runtime. Check that both ScheduleMC and the integration mod are loaded (visible in the Forge mod list).

**Q: The MDMA pill press timing minigame is too difficult.**
A: The pill press requires precise timing to produce high-quality pills. Missing the timing window degrades product quality but does not waste materials. Practice the timing rhythm -- the press has a visual and audio indicator for the optimal press moment. Administrators can adjust timing difficulty through the production configuration.

**Q: NPC witnesses keep reporting my crimes even when no one is around.**
A: NPCs have a detection radius for witnessing crimes. Even if you cannot see an NPC, one may be within detection range around a corner or inside a building. Use the Crime Stats smartphone app to see recent reports, and plan illegal activities in truly isolated areas away from NPC patrol routes and daily schedule locations.

---

## License

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**.

You are free to redistribute and modify this software under the terms of the GPLv3. See the [LICENSE](LICENSE) file for the full license text.

```
ScheduleMC - A comprehensive Minecraft Forge roleplay and economy mod
Copyright (C) 2025 Luckas R. Schneider (Minecraft425HD)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

---

## Credits

**Author:** Luckas R. Schneider (Minecraft425HD)

**Special Thanks:**
- The [Minecraft Forge](https://minecraftforge.net/) team for the modding framework and toolchain
- [Max Henkel](https://github.com/henkelmax) for [CoreLib](https://github.com/henkelmax/corelib), providing OBJ model rendering, GUI systems, and networking utilities
- The [Mojang](https://www.mojang.com/) team for Minecraft and the official mapping files
- The [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) project for bytecode modification technology
- The [LlamaLad7 MixinExtras](https://github.com/LlamaLad7/MixinExtras) project for enhanced Mixin features
- The [JUnit 5](https://junit.org/junit5/) team for the unit testing framework
- The [Mockito](https://site.mockito.org/) team for the mocking framework
- The [AssertJ](https://assertj.github.io/doc/) team for the fluent assertion library
- The [JaCoCo](https://www.jacoco.org/) project for code coverage analysis
- The [Google Gson](https://github.com/google/gson) team for JSON processing
- The Minecraft modding community for continued inspiration and feedback

---

<p align="center">
  Built for the Minecraft roleplay community.
  <br />
  <strong>ScheduleMC v3.6.0-beta</strong> -- Minecraft 1.20.1 -- Forge 47.4.0
  <br />
  <br />
  <a href="https://github.com/Minecraft425HD/ScheduleMC">GitHub</a>
  &middot;
  <a href="https://github.com/Minecraft425HD/ScheduleMC/issues">Issues</a>
  &middot;
  <a href="https://github.com/Minecraft425HD/ScheduleMC/wiki">Wiki</a>
</p>
