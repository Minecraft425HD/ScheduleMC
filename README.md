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

> Complete directory tree listing all **1,407 Java source files**, **19 test files**, and **1,206 resource files**.

<details>
<summary><strong>Click to expand full project tree (3,000+ lines)</strong></summary>

```
ScheduleMC/
├── .github/
│   └── workflows
│       └── ci.yml
├── docs/
│   ├── API_REFERENCE.md
│   ├── ARCHITECTURE.md
│   ├── CONFIGURATION.md
│   ├── DEVELOPER_GUIDE.md
│   ├── TOWING_NPC_INVOICE_SCREEN.md
│   └── TOWING_SYSTEM_SETUP.md
├── gradle/
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/
│   ├── main
│   │   ├── java
│   │   │   └── de
│   │   │       └── rolandsw
│   │   │           └── schedulemc
│   │   │               ├── achievement
│   │   │               │   ├── client
│   │   │               │   │   └── ClientAchievementCache.java
│   │   │               │   ├── network
│   │   │               │   │   ├── AchievementData.java
│   │   │               │   │   ├── AchievementNetworkHandler.java
│   │   │               │   │   ├── RequestAchievementDataPacket.java
│   │   │               │   │   └── SyncAchievementDataPacket.java
│   │   │               │   ├── Achievement.java
│   │   │               │   ├── AchievementCategory.java
│   │   │               │   ├── AchievementManager.java
│   │   │               │   ├── AchievementTier.java
│   │   │               │   ├── AchievementTracker.java
│   │   │               │   └── PlayerAchievements.java
│   │   │               ├── api
│   │   │               │   ├── achievement
│   │   │               │   │   └── IAchievementAPI.java
│   │   │               │   ├── economy
│   │   │               │   │   └── IEconomyAPI.java
│   │   │               │   ├── impl
│   │   │               │   │   ├── AchievementAPIImpl.java
│   │   │               │   │   ├── EconomyAPIImpl.java
│   │   │               │   │   ├── MarketAPIImpl.java
│   │   │               │   │   ├── MessagingAPIImpl.java
│   │   │               │   │   ├── NPCAPIImpl.java
│   │   │               │   │   ├── PlotAPIImpl.java
│   │   │               │   │   ├── PoliceAPIImpl.java
│   │   │               │   │   ├── ProductionAPIImpl.java
│   │   │               │   │   ├── SmartphoneAPIImpl.java
│   │   │               │   │   ├── VehicleAPIImpl.java
│   │   │               │   │   └── WarehouseAPIImpl.java
│   │   │               │   ├── market
│   │   │               │   │   └── IMarketAPI.java
│   │   │               │   ├── messaging
│   │   │               │   │   └── IMessagingAPI.java
│   │   │               │   ├── npc
│   │   │               │   │   └── INPCAPI.java
│   │   │               │   ├── plot
│   │   │               │   │   └── IPlotAPI.java
│   │   │               │   ├── police
│   │   │               │   │   └── IPoliceAPI.java
│   │   │               │   ├── production
│   │   │               │   │   └── IProductionAPI.java
│   │   │               │   ├── smartphone
│   │   │               │   │   └── ISmartphoneAPI.java
│   │   │               │   ├── vehicle
│   │   │               │   │   └── IVehicleAPI.java
│   │   │               │   ├── warehouse
│   │   │               │   │   └── IWarehouseAPI.java
│   │   │               │   ├── PlotModAPI.java
│   │   │               │   └── ScheduleMCAPI.java
│   │   │               ├── beer
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── AbstractBeerFermentationTankBlockEntity.java
│   │   │               │   │   ├── AbstractBrewKettleBlockEntity.java
│   │   │               │   │   ├── AbstractConditioningTankBlockEntity.java
│   │   │               │   │   ├── BeerBlockEntities.java
│   │   │               │   │   ├── BottlingStationBlockEntity.java
│   │   │               │   │   ├── LargeBeerFermentationTankBlockEntity.java
│   │   │               │   │   ├── LargeBrewKettleBlockEntity.java
│   │   │               │   │   ├── LargeConditioningTankBlockEntity.java
│   │   │               │   │   ├── MaltingStationBlockEntity.java
│   │   │               │   │   ├── MashTunBlockEntity.java
│   │   │               │   │   ├── MediumBeerFermentationTankBlockEntity.java
│   │   │               │   │   ├── MediumBrewKettleBlockEntity.java
│   │   │               │   │   ├── MediumConditioningTankBlockEntity.java
│   │   │               │   │   ├── SmallBeerFermentationTankBlockEntity.java
│   │   │               │   │   ├── SmallBrewKettleBlockEntity.java
│   │   │               │   │   └── SmallConditioningTankBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── BeerBlocks.java
│   │   │               │   │   ├── BottlingStationBlock.java
│   │   │               │   │   ├── LargeBeerFermentationTankBlock.java
│   │   │               │   │   ├── LargeBrewKettleBlock.java
│   │   │               │   │   ├── LargeConditioningTankBlock.java
│   │   │               │   │   ├── MaltingStationBlock.java
│   │   │               │   │   ├── MashTunBlock.java
│   │   │               │   │   ├── MediumBeerFermentationTankBlock.java
│   │   │               │   │   ├── MediumBrewKettleBlock.java
│   │   │               │   │   ├── MediumConditioningTankBlock.java
│   │   │               │   │   ├── SmallBeerFermentationTankBlock.java
│   │   │               │   │   ├── SmallBrewKettleBlock.java
│   │   │               │   │   └── SmallConditioningTankBlock.java
│   │   │               │   ├── items
│   │   │               │   │   ├── BeerBottleItem.java
│   │   │               │   │   └── BeerItems.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── BeerMenuTypes.java
│   │   │               │   │   ├── BottlingStationMenu.java
│   │   │               │   │   ├── LargeBeerFermentationTankMenu.java
│   │   │               │   │   ├── LargeBrewKettleMenu.java
│   │   │               │   │   ├── LargeConditioningTankMenu.java
│   │   │               │   │   ├── MaltingStationMenu.java
│   │   │               │   │   ├── MashTunMenu.java
│   │   │               │   │   ├── MediumBeerFermentationTankMenu.java
│   │   │               │   │   ├── MediumBrewKettleMenu.java
│   │   │               │   │   ├── MediumConditioningTankMenu.java
│   │   │               │   │   ├── SmallBeerFermentationTankMenu.java
│   │   │               │   │   ├── SmallBrewKettleMenu.java
│   │   │               │   │   └── SmallConditioningTankMenu.java
│   │   │               │   ├── network
│   │   │               │   │   ├── BeerNetworking.java
│   │   │               │   │   └── ProcessingMethodPacket.java
│   │   │               │   ├── screen
│   │   │               │   │   ├── BottlingStationScreen.java
│   │   │               │   │   ├── LargeBeerFermentationTankScreen.java
│   │   │               │   │   ├── LargeBrewKettleScreen.java
│   │   │               │   │   ├── LargeConditioningTankScreen.java
│   │   │               │   │   ├── MaltingStationScreen.java
│   │   │               │   │   ├── MashTunScreen.java
│   │   │               │   │   ├── MediumBeerFermentationTankScreen.java
│   │   │               │   │   ├── MediumBrewKettleScreen.java
│   │   │               │   │   ├── MediumConditioningTankScreen.java
│   │   │               │   │   ├── SmallBeerFermentationTankScreen.java
│   │   │               │   │   ├── SmallBrewKettleScreen.java
│   │   │               │   │   └── SmallConditioningTankScreen.java
│   │   │               │   ├── BeerAgeLevel.java
│   │   │               │   ├── BeerProcessingMethod.java
│   │   │               │   ├── BeerQuality.java
│   │   │               │   └── BeerType.java
│   │   │               ├── cannabis
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── CannabisBlockEntities.java
│   │   │               │   │   ├── CuringGlasBlockEntity.java
│   │   │               │   │   ├── HashPresseBlockEntity.java
│   │   │               │   │   ├── OelExtraktortBlockEntity.java
│   │   │               │   │   ├── TrimmStationBlockEntity.java
│   │   │               │   │   └── TrocknungsnetzBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── CannabisBlocks.java
│   │   │               │   │   ├── CannabisPlantBlock.java
│   │   │               │   │   ├── CuringGlasBlock.java
│   │   │               │   │   ├── HashPresseBlock.java
│   │   │               │   │   ├── OelExtraktortBlock.java
│   │   │               │   │   ├── TrimmStationBlock.java
│   │   │               │   │   └── TrocknungsnetzBlock.java
│   │   │               │   ├── data
│   │   │               │   │   └── CannabisPlantData.java
│   │   │               │   ├── items
│   │   │               │   │   ├── CannabisItems.java
│   │   │               │   │   ├── CannabisOilItem.java
│   │   │               │   │   ├── CannabisSeedItem.java
│   │   │               │   │   ├── CuredBudItem.java
│   │   │               │   │   ├── DriedBudItem.java
│   │   │               │   │   ├── FreshBudItem.java
│   │   │               │   │   ├── HashItem.java
│   │   │               │   │   ├── TrimItem.java
│   │   │               │   │   └── TrimmedBudItem.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── CannabisMenuTypes.java
│   │   │               │   │   └── TrimmStationMenu.java
│   │   │               │   ├── screen
│   │   │               │   │   └── TrimmStationScreen.java
│   │   │               │   ├── CannabisQuality.java
│   │   │               │   └── CannabisStrain.java
│   │   │               ├── cheese
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── AbstractAgingCaveBlockEntity.java
│   │   │               │   │   ├── AbstractCheesePressBlockEntity.java
│   │   │               │   │   ├── CheeseBlockEntities.java
│   │   │               │   │   ├── CurdlingVatBlockEntity.java
│   │   │               │   │   ├── LargeAgingCaveBlockEntity.java
│   │   │               │   │   ├── LargeCheesePressBlockEntity.java
│   │   │               │   │   ├── MediumAgingCaveBlockEntity.java
│   │   │               │   │   ├── MediumCheesePressBlockEntity.java
│   │   │               │   │   ├── PackagingStationBlockEntity.java
│   │   │               │   │   ├── PasteurizationStationBlockEntity.java
│   │   │               │   │   ├── SmallAgingCaveBlockEntity.java
│   │   │               │   │   └── SmallCheesePressBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── CheeseBlocks.java
│   │   │               │   │   ├── CurdlingVatBlock.java
│   │   │               │   │   ├── LargeAgingCaveBlock.java
│   │   │               │   │   ├── LargeCheesePressBlock.java
│   │   │               │   │   ├── MediumAgingCaveBlock.java
│   │   │               │   │   ├── MediumCheesePressBlock.java
│   │   │               │   │   ├── PackagingStationBlock.java
│   │   │               │   │   ├── PasteurizationStationBlock.java
│   │   │               │   │   ├── SmallAgingCaveBlock.java
│   │   │               │   │   └── SmallCheesePressBlock.java
│   │   │               │   ├── items
│   │   │               │   │   ├── CheeseCurdItem.java
│   │   │               │   │   ├── CheeseItems.java
│   │   │               │   │   ├── CheeseWedgeItem.java
│   │   │               │   │   ├── CheeseWheelItem.java
│   │   │               │   │   ├── MilkBucketItem.java
│   │   │               │   │   └── RennetItem.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── CheeseMenuTypes.java
│   │   │               │   │   ├── CurdlingVatMenu.java
│   │   │               │   │   ├── LargeAgingCaveMenu.java
│   │   │               │   │   ├── LargeCheesePressMenu.java
│   │   │               │   │   ├── MediumAgingCaveMenu.java
│   │   │               │   │   ├── MediumCheesePressMenu.java
│   │   │               │   │   ├── PackagingStationMenu.java
│   │   │               │   │   ├── PasteurizationStationMenu.java
│   │   │               │   │   ├── SmallAgingCaveMenu.java
│   │   │               │   │   └── SmallCheesePressMenu.java
│   │   │               │   ├── network
│   │   │               │   │   ├── CheeseNetworking.java
│   │   │               │   │   └── ProcessingMethodPacket.java
│   │   │               │   ├── screen
│   │   │               │   │   ├── CurdlingVatScreen.java
│   │   │               │   │   ├── LargeAgingCaveScreen.java
│   │   │               │   │   ├── LargeCheesePressScreen.java
│   │   │               │   │   ├── MediumAgingCaveScreen.java
│   │   │               │   │   ├── MediumCheesePressScreen.java
│   │   │               │   │   ├── PackagingStationScreen.java
│   │   │               │   │   ├── PasteurizationStationScreen.java
│   │   │               │   │   ├── SmallAgingCaveScreen.java
│   │   │               │   │   └── SmallCheesePressScreen.java
│   │   │               │   ├── CheeseAgeLevel.java
│   │   │               │   ├── CheeseProcessingMethod.java
│   │   │               │   ├── CheeseQuality.java
│   │   │               │   └── CheeseType.java
│   │   │               ├── chocolate
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── AbstractConchingMachineBlockEntity.java
│   │   │               │   │   ├── AbstractMoldingStationBlockEntity.java
│   │   │               │   │   ├── ChocolateBlockEntities.java
│   │   │               │   │   ├── CoolingTunnelBlockEntity.java
│   │   │               │   │   ├── EnrobingMachineBlockEntity.java
│   │   │               │   │   ├── GrindingMillBlockEntity.java
│   │   │               │   │   ├── LargeConchingMachineBlockEntity.java
│   │   │               │   │   ├── LargeMoldingStationBlockEntity.java
│   │   │               │   │   ├── MediumConchingMachineBlockEntity.java
│   │   │               │   │   ├── MediumMoldingStationBlockEntity.java
│   │   │               │   │   ├── PressingStationBlockEntity.java
│   │   │               │   │   ├── RoastingStationBlockEntity.java
│   │   │               │   │   ├── SmallConchingMachineBlockEntity.java
│   │   │               │   │   ├── SmallMoldingStationBlockEntity.java
│   │   │               │   │   ├── TemperingStationBlockEntity.java
│   │   │               │   │   ├── WinnowingMachineBlockEntity.java
│   │   │               │   │   └── WrappingStationBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── ChocolateBlocks.java
│   │   │               │   │   ├── CoolingTunnelBlock.java
│   │   │               │   │   ├── EnrobingMachineBlock.java
│   │   │               │   │   ├── GrindingMillBlock.java
│   │   │               │   │   ├── LargeConchingMachineBlock.java
│   │   │               │   │   ├── LargeMoldingStationBlock.java
│   │   │               │   │   ├── MediumConchingMachineBlock.java
│   │   │               │   │   ├── MediumMoldingStationBlock.java
│   │   │               │   │   ├── PressingStationBlock.java
│   │   │               │   │   ├── RoastingStationBlock.java
│   │   │               │   │   ├── SmallConchingMachineBlock.java
│   │   │               │   │   ├── SmallMoldingStationBlock.java
│   │   │               │   │   ├── TemperingStationBlock.java
│   │   │               │   │   ├── WinnowingMachineBlock.java
│   │   │               │   │   └── WrappingStationBlock.java
│   │   │               │   ├── items
│   │   │               │   │   ├── ChocolateBarItem.java
│   │   │               │   │   └── ChocolateItems.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── ChocolateMenuTypes.java
│   │   │               │   │   ├── CoolingTunnelMenu.java
│   │   │               │   │   ├── EnrobingMachineMenu.java
│   │   │               │   │   ├── GrindingMillMenu.java
│   │   │               │   │   ├── LargeConchingMachineMenu.java
│   │   │               │   │   ├── LargeMoldingStationMenu.java
│   │   │               │   │   ├── MediumConchingMachineMenu.java
│   │   │               │   │   ├── MediumMoldingStationMenu.java
│   │   │               │   │   ├── PressingStationMenu.java
│   │   │               │   │   ├── RoastingStationMenu.java
│   │   │               │   │   ├── SmallConchingMachineMenu.java
│   │   │               │   │   ├── SmallMoldingStationMenu.java
│   │   │               │   │   ├── TemperingStationMenu.java
│   │   │               │   │   ├── WinnowingMachineMenu.java
│   │   │               │   │   └── WrappingStationMenu.java
│   │   │               │   ├── network
│   │   │               │   │   ├── ChocolateNetworking.java
│   │   │               │   │   └── ProcessingMethodPacket.java
│   │   │               │   ├── screen
│   │   │               │   │   ├── CoolingTunnelScreen.java
│   │   │               │   │   ├── EnrobingMachineScreen.java
│   │   │               │   │   ├── GrindingMillScreen.java
│   │   │               │   │   ├── LargeConchingMachineScreen.java
│   │   │               │   │   ├── LargeMoldingStationScreen.java
│   │   │               │   │   ├── MediumConchingMachineScreen.java
│   │   │               │   │   ├── MediumMoldingStationScreen.java
│   │   │               │   │   ├── PressingStationScreen.java
│   │   │               │   │   ├── RoastingStationScreen.java
│   │   │               │   │   ├── SmallConchingMachineScreen.java
│   │   │               │   │   ├── SmallMoldingStationScreen.java
│   │   │               │   │   ├── TemperingStationScreen.java
│   │   │               │   │   ├── WinnowingMachineScreen.java
│   │   │               │   │   └── WrappingStationScreen.java
│   │   │               │   ├── ChocolateAgeLevel.java
│   │   │               │   ├── ChocolateProcessingMethod.java
│   │   │               │   ├── ChocolateQuality.java
│   │   │               │   └── ChocolateType.java
│   │   │               ├── client
│   │   │               │   ├── network
│   │   │               │   │   ├── SmartphoneNetworkHandler.java
│   │   │               │   │   └── SmartphoneStatePacket.java
│   │   │               │   ├── screen
│   │   │               │   │   ├── apps
│   │   │               │   │   │   ├── AchievementAppScreen.java
│   │   │               │   │   │   ├── BankAppScreen.java
│   │   │               │   │   │   ├── ChatScreen.java
│   │   │               │   │   │   ├── ContactDetailScreen.java
│   │   │               │   │   │   ├── ContactsAppScreen.java
│   │   │               │   │   │   ├── CrimeStatsAppScreen.java
│   │   │               │   │   │   ├── DealerAppScreen.java
│   │   │               │   │   │   ├── GangAppScreen.java
│   │   │               │   │   │   ├── MembershipSelectionScreen.java
│   │   │               │   │   │   ├── MessagesAppScreen.java
│   │   │               │   │   │   ├── OrderAppScreen.java
│   │   │               │   │   │   ├── PlotAppScreen.java
│   │   │               │   │   │   ├── ProducerLevelAppScreen.java
│   │   │               │   │   │   ├── ProductsAppScreen.java
│   │   │               │   │   │   ├── ScenarioEditorScreen.java
│   │   │               │   │   │   ├── SettingsAppScreen.java
│   │   │               │   │   │   ├── TowingServiceAppScreen.java
│   │   │               │   │   │   └── TowingYardSelectionScreen.java
│   │   │               │   │   ├── CombinationLockScreen.java
│   │   │               │   │   ├── ConfirmDialogScreen.java
│   │   │               │   │   ├── InputDialogScreen.java
│   │   │               │   │   └── SmartphoneScreen.java
│   │   │               │   ├── ClientModEvents.java
│   │   │               │   ├── InventoryBlockHandler.java
│   │   │               │   ├── KeyBindings.java
│   │   │               │   ├── PlotInfoClientHandler.java
│   │   │               │   ├── PlotInfoHudOverlay.java
│   │   │               │   ├── PlotInfoScreen.java
│   │   │               │   ├── QualityItemColors.java
│   │   │               │   ├── SmartphoneKeyHandler.java
│   │   │               │   ├── SmartphonePlayerHandler.java
│   │   │               │   ├── SmartphoneProtectionHandler.java
│   │   │               │   ├── SmartphoneTracker.java
│   │   │               │   ├── TobaccoPotHudOverlay.java
│   │   │               │   ├── UpdateNotificationHandler.java
│   │   │               │   └── WantedLevelOverlay.java
│   │   │               ├── coca
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── AbstractExtractionVatBlockEntity.java
│   │   │               │   │   ├── AbstractRefineryBlockEntity.java
│   │   │               │   │   ├── BigExtractionVatBlockEntity.java
│   │   │               │   │   ├── BigRefineryBlockEntity.java
│   │   │               │   │   ├── CocaBlockEntities.java
│   │   │               │   │   ├── CrackKocherBlockEntity.java
│   │   │               │   │   ├── MediumExtractionVatBlockEntity.java
│   │   │               │   │   ├── MediumRefineryBlockEntity.java
│   │   │               │   │   ├── SmallExtractionVatBlockEntity.java
│   │   │               │   │   └── SmallRefineryBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── BigExtractionVatBlock.java
│   │   │               │   │   ├── BigRefineryBlock.java
│   │   │               │   │   ├── CocaBlocks.java
│   │   │               │   │   ├── CocaPlantBlock.java
│   │   │               │   │   ├── CrackKocherBlock.java
│   │   │               │   │   ├── MediumExtractionVatBlock.java
│   │   │               │   │   ├── MediumRefineryBlock.java
│   │   │               │   │   ├── SmallExtractionVatBlock.java
│   │   │               │   │   └── SmallRefineryBlock.java
│   │   │               │   ├── data
│   │   │               │   │   └── CocaPlantData.java
│   │   │               │   ├── items
│   │   │               │   │   ├── BackpulverItem.java
│   │   │               │   │   ├── CocaItems.java
│   │   │               │   │   ├── CocaPasteItem.java
│   │   │               │   │   ├── CocaSeedItem.java
│   │   │               │   │   ├── CocaineItem.java
│   │   │               │   │   ├── CrackRockItem.java
│   │   │               │   │   └── FreshCocaLeafItem.java
│   │   │               │   ├── CocaType.java
│   │   │               │   └── CrackQuality.java
│   │   │               ├── coffee
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── AbstractCoffeeDryingTrayBlockEntity.java
│   │   │               │   │   ├── AbstractCoffeeRoasterBlockEntity.java
│   │   │               │   │   ├── CoffeeBlockEntities.java
│   │   │               │   │   ├── CoffeeGrinderBlockEntity.java
│   │   │               │   │   ├── CoffeePackagingTableBlockEntity.java
│   │   │               │   │   ├── LargeCoffeeRoasterBlockEntity.java
│   │   │               │   │   ├── LargeDryingTrayBlockEntity.java
│   │   │               │   │   ├── MediumCoffeeRoasterBlockEntity.java
│   │   │               │   │   ├── MediumDryingTrayBlockEntity.java
│   │   │               │   │   ├── SmallCoffeeRoasterBlockEntity.java
│   │   │               │   │   ├── SmallDryingTrayBlockEntity.java
│   │   │               │   │   └── WetProcessingStationBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── CoffeeBlocks.java
│   │   │               │   │   ├── CoffeeGrinderBlock.java
│   │   │               │   │   ├── CoffeePackagingTableBlock.java
│   │   │               │   │   ├── CoffeePlantBlock.java
│   │   │               │   │   ├── LargeCoffeeRoasterBlock.java
│   │   │               │   │   ├── LargeDryingTrayBlock.java
│   │   │               │   │   ├── MediumCoffeeRoasterBlock.java
│   │   │               │   │   ├── MediumDryingTrayBlock.java
│   │   │               │   │   ├── SmallCoffeeRoasterBlock.java
│   │   │               │   │   ├── SmallDryingTrayBlock.java
│   │   │               │   │   └── WetProcessingStationBlock.java
│   │   │               │   ├── items
│   │   │               │   │   ├── BrewedCoffeeItem.java
│   │   │               │   │   ├── CoffeeCherryItem.java
│   │   │               │   │   ├── CoffeeItems.java
│   │   │               │   │   ├── CoffeeSeedlingItem.java
│   │   │               │   │   ├── EspressoItem.java
│   │   │               │   │   ├── GreenCoffeeBeanItem.java
│   │   │               │   │   ├── GroundCoffeeItem.java
│   │   │               │   │   ├── PackagedCoffeeItem.java
│   │   │               │   │   └── RoastedCoffeeBeanItem.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── CoffeeGrinderMenu.java
│   │   │               │   │   ├── CoffeeMenuTypes.java
│   │   │               │   │   ├── CoffeePackagingTableMenu.java
│   │   │               │   │   ├── LargeCoffeeRoasterMenu.java
│   │   │               │   │   ├── LargeDryingTrayMenu.java
│   │   │               │   │   ├── MediumCoffeeRoasterMenu.java
│   │   │               │   │   ├── MediumDryingTrayMenu.java
│   │   │               │   │   ├── SmallCoffeeRoasterMenu.java
│   │   │               │   │   ├── SmallDryingTrayMenu.java
│   │   │               │   │   └── WetProcessingStationMenu.java
│   │   │               │   ├── network
│   │   │               │   │   ├── CoffeeNetworking.java
│   │   │               │   │   ├── CoffeePackageRequestPacket.java
│   │   │               │   │   ├── GrindSizePacket.java
│   │   │               │   │   └── RoasterLevelPacket.java
│   │   │               │   ├── screen
│   │   │               │   │   ├── CoffeeGrinderScreen.java
│   │   │               │   │   ├── CoffeePackagingTableScreen.java
│   │   │               │   │   ├── LargeCoffeeRoasterScreen.java
│   │   │               │   │   ├── LargeDryingTrayScreen.java
│   │   │               │   │   ├── MediumCoffeeRoasterScreen.java
│   │   │               │   │   ├── MediumDryingTrayScreen.java
│   │   │               │   │   ├── SmallCoffeeRoasterScreen.java
│   │   │               │   │   ├── SmallDryingTrayScreen.java
│   │   │               │   │   └── WetProcessingStationScreen.java
│   │   │               │   ├── CoffeeGrindSize.java
│   │   │               │   ├── CoffeeProcessingMethod.java
│   │   │               │   ├── CoffeeQuality.java
│   │   │               │   ├── CoffeeRoastLevel.java
│   │   │               │   └── CoffeeType.java
│   │   │               ├── commands
│   │   │               │   ├── AdminCommand.java
│   │   │               │   ├── CommandExecutor.java
│   │   │               │   ├── HealthCommand.java
│   │   │               │   ├── MoneyCommand.java
│   │   │               │   └── PlotCommand.java
│   │   │               ├── config
│   │   │               │   ├── ClientConfig.java
│   │   │               │   ├── DeliveryPriceConfig.java
│   │   │               │   ├── Fuel.java
│   │   │               │   ├── FuelConfig.java
│   │   │               │   ├── ModConfigHandler.java
│   │   │               │   ├── ServerConfig.java
│   │   │               │   └── TobaccoConfig.java
│   │   │               ├── data
│   │   │               │   └── DailyReward.java
│   │   │               ├── economy
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── ATMBlockEntity.java
│   │   │               │   │   └── CashBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── ATMBlock.java
│   │   │               │   │   ├── CashBlock.java
│   │   │               │   │   └── EconomyBlocks.java
│   │   │               │   ├── commands
│   │   │               │   │   ├── HospitalCommand.java
│   │   │               │   │   └── StateCommand.java
│   │   │               │   ├── events
│   │   │               │   │   ├── CashSlotRestrictionHandler.java
│   │   │               │   │   ├── CreditScoreEventHandler.java
│   │   │               │   │   └── RespawnHandler.java
│   │   │               │   ├── items
│   │   │               │   │   └── CashItem.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── ATMMenu.java
│   │   │               │   │   └── EconomyMenuTypes.java
│   │   │               │   ├── network
│   │   │               │   │   ├── ATMTransactionPacket.java
│   │   │               │   │   ├── ClientBankDataCache.java
│   │   │               │   │   ├── EconomyNetworkHandler.java
│   │   │               │   │   ├── RequestATMDataPacket.java
│   │   │               │   │   ├── RequestBankDataPacket.java
│   │   │               │   │   ├── SyncATMDataPacket.java
│   │   │               │   │   ├── SyncBankDataPacket.java
│   │   │               │   │   └── SyncFullBankDataPacket.java
│   │   │               │   ├── screen
│   │   │               │   │   └── ATMScreen.java
│   │   │               │   ├── AntiExploitManager.java
│   │   │               │   ├── BatchTransactionManager.java
│   │   │               │   ├── CreditLoan.java
│   │   │               │   ├── CreditLoanManager.java
│   │   │               │   ├── CreditScore.java
│   │   │               │   ├── CreditScoreManager.java
│   │   │               │   ├── DailyRevenueRecord.java
│   │   │               │   ├── EconomicEvent.java
│   │   │               │   ├── EconomyController.java
│   │   │               │   ├── EconomyCycle.java
│   │   │               │   ├── EconomyCyclePhase.java
│   │   │               │   ├── EconomyManager.java
│   │   │               │   ├── FeeManager.java
│   │   │               │   ├── GlobalEconomyTracker.java
│   │   │               │   ├── InterestManager.java
│   │   │               │   ├── ItemCategory.java
│   │   │               │   ├── Loan.java
│   │   │               │   ├── LoanManager.java
│   │   │               │   ├── MemoryCleanupManager.java
│   │   │               │   ├── OverdraftManager.java
│   │   │               │   ├── PlayerJoinHandler.java
│   │   │               │   ├── PriceBounds.java
│   │   │               │   ├── PriceManager.java
│   │   │               │   ├── RateLimiter.java
│   │   │               │   ├── RecurringPayment.java
│   │   │               │   ├── RecurringPaymentEventHandler.java
│   │   │               │   ├── RecurringPaymentInterval.java
│   │   │               │   ├── RecurringPaymentManager.java
│   │   │               │   ├── RiskPremium.java
│   │   │               │   ├── SavingsAccount.java
│   │   │               │   ├── SavingsAccountManager.java
│   │   │               │   ├── ShopAccount.java
│   │   │               │   ├── ShopAccountManager.java
│   │   │               │   ├── StateAccount.java
│   │   │               │   ├── TaxManager.java
│   │   │               │   ├── Transaction.java
│   │   │               │   ├── TransactionHistory.java
│   │   │               │   ├── TransactionType.java
│   │   │               │   ├── UnifiedPriceCalculator.java
│   │   │               │   ├── WalletManager.java
│   │   │               │   ├── WarehouseMarketBridge.java
│   │   │               │   ├── WarehouseStockLevel.java
│   │   │               │   └── package-info.java
│   │   │               ├── events
│   │   │               │   ├── BlockProtectionHandler.java
│   │   │               │   ├── InventoryRestrictionHandler.java
│   │   │               │   ├── ModEvents.java
│   │   │               │   └── PlayerDisconnectHandler.java
│   │   │               ├── gang
│   │   │               │   ├── client
│   │   │               │   │   ├── ClientGangCache.java
│   │   │               │   │   ├── GangNametagRenderer.java
│   │   │               │   │   └── GangTabListHandler.java
│   │   │               │   ├── mission
│   │   │               │   │   ├── GangMission.java
│   │   │               │   │   ├── GangMissionManager.java
│   │   │               │   │   ├── MissionTemplate.java
│   │   │               │   │   └── MissionType.java
│   │   │               │   ├── network
│   │   │               │   │   ├── GangActionPacket.java
│   │   │               │   │   ├── GangNetworkHandler.java
│   │   │               │   │   ├── GangSyncHelper.java
│   │   │               │   │   ├── OpenScenarioEditorPacket.java
│   │   │               │   │   ├── PlayerGangInfo.java
│   │   │               │   │   ├── RequestGangDataPacket.java
│   │   │               │   │   ├── RequestGangListPacket.java
│   │   │               │   │   ├── SaveScenarioPacket.java
│   │   │               │   │   ├── SyncAllPlayerGangInfoPacket.java
│   │   │               │   │   ├── SyncGangDataPacket.java
│   │   │               │   │   └── SyncGangListPacket.java
│   │   │               │   ├── scenario
│   │   │               │   │   ├── MissionScenario.java
│   │   │               │   │   ├── ObjectiveType.java
│   │   │               │   │   ├── ScenarioManager.java
│   │   │               │   │   ├── ScenarioObjective.java
│   │   │               │   │   └── ScenarioTemplates.java
│   │   │               │   ├── Gang.java
│   │   │               │   ├── GangCommand.java
│   │   │               │   ├── GangLevelRequirements.java
│   │   │               │   ├── GangManager.java
│   │   │               │   ├── GangMemberData.java
│   │   │               │   ├── GangPerk.java
│   │   │               │   ├── GangRank.java
│   │   │               │   ├── GangReputation.java
│   │   │               │   └── GangXPSource.java
│   │   │               ├── gui
│   │   │               │   └── PlotMenuGUI.java
│   │   │               ├── honey
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── AbstractAgingChamberBlockEntity.java
│   │   │               │   │   ├── AdvancedBeehiveBlockEntity.java
│   │   │               │   │   ├── ApiaryBlockEntity.java
│   │   │               │   │   ├── BeehiveBlockEntity.java
│   │   │               │   │   ├── BottlingStationBlockEntity.java
│   │   │               │   │   ├── CentrifugalExtractorBlockEntity.java
│   │   │               │   │   ├── CreamingStationBlockEntity.java
│   │   │               │   │   ├── FilteringStationBlockEntity.java
│   │   │               │   │   ├── HoneyBlockEntities.java
│   │   │               │   │   ├── HoneyExtractorBlockEntity.java
│   │   │               │   │   ├── LargeAgingChamberBlockEntity.java
│   │   │               │   │   ├── MediumAgingChamberBlockEntity.java
│   │   │               │   │   ├── ProcessingStationBlockEntity.java
│   │   │               │   │   └── SmallAgingChamberBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── AdvancedBeehiveBlock.java
│   │   │               │   │   ├── ApiaryBlock.java
│   │   │               │   │   ├── BeehiveBlock.java
│   │   │               │   │   ├── BottlingStationBlock.java
│   │   │               │   │   ├── CentrifugalExtractorBlock.java
│   │   │               │   │   ├── CreamingStationBlock.java
│   │   │               │   │   ├── FilteringStationBlock.java
│   │   │               │   │   ├── HoneyBlocks.java
│   │   │               │   │   ├── HoneyExtractorBlock.java
│   │   │               │   │   ├── LargeAgingChamberBlock.java
│   │   │               │   │   ├── MediumAgingChamberBlock.java
│   │   │               │   │   ├── ProcessingStationBlock.java
│   │   │               │   │   └── SmallAgingChamberBlock.java
│   │   │               │   ├── items
│   │   │               │   │   ├── HoneyItems.java
│   │   │               │   │   └── HoneyJarItem.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── AdvancedBeehiveMenu.java
│   │   │               │   │   ├── ApiaryMenu.java
│   │   │               │   │   ├── BeehiveMenu.java
│   │   │               │   │   ├── BottlingStationMenu.java
│   │   │               │   │   ├── CentrifugalExtractorMenu.java
│   │   │               │   │   ├── CreamingStationMenu.java
│   │   │               │   │   ├── FilteringStationMenu.java
│   │   │               │   │   ├── HoneyExtractorMenu.java
│   │   │               │   │   ├── HoneyMenuTypes.java
│   │   │               │   │   ├── LargeAgingChamberMenu.java
│   │   │               │   │   ├── MediumAgingChamberMenu.java
│   │   │               │   │   ├── ProcessingStationMenu.java
│   │   │               │   │   └── SmallAgingChamberMenu.java
│   │   │               │   ├── network
│   │   │               │   │   ├── HoneyNetworking.java
│   │   │               │   │   └── ProcessingMethodPacket.java
│   │   │               │   ├── screen
│   │   │               │   │   ├── AdvancedBeehiveScreen.java
│   │   │               │   │   ├── ApiaryScreen.java
│   │   │               │   │   ├── BeehiveScreen.java
│   │   │               │   │   ├── BottlingStationScreen.java
│   │   │               │   │   ├── CentrifugalExtractorScreen.java
│   │   │               │   │   ├── CreamingStationScreen.java
│   │   │               │   │   ├── FilteringStationScreen.java
│   │   │               │   │   ├── HoneyExtractorScreen.java
│   │   │               │   │   ├── LargeAgingChamberScreen.java
│   │   │               │   │   ├── MediumAgingChamberScreen.java
│   │   │               │   │   ├── ProcessingStationScreen.java
│   │   │               │   │   └── SmallAgingChamberScreen.java
│   │   │               │   ├── HoneyAgeLevel.java
│   │   │               │   ├── HoneyProcessingMethod.java
│   │   │               │   ├── HoneyQuality.java
│   │   │               │   └── HoneyType.java
│   │   │               ├── items
│   │   │               │   ├── ModItems.java
│   │   │               │   └── PlotSelectionTool.java
│   │   │               ├── level
│   │   │               │   ├── client
│   │   │               │   │   └── ClientProducerLevelCache.java
│   │   │               │   ├── network
│   │   │               │   │   ├── LevelUpNotificationPacket.java
│   │   │               │   │   ├── ProducerLevelNetworkHandler.java
│   │   │               │   │   ├── RequestProducerLevelDataPacket.java
│   │   │               │   │   ├── SyncProducerLevelDataPacket.java
│   │   │               │   │   └── UnlockableData.java
│   │   │               │   ├── LevelRequirements.java
│   │   │               │   ├── ProducerLevel.java
│   │   │               │   ├── ProducerLevelData.java
│   │   │               │   ├── UnlockCategory.java
│   │   │               │   ├── Unlockable.java
│   │   │               │   └── XPSource.java
│   │   │               ├── lock
│   │   │               │   ├── items
│   │   │               │   │   ├── BypassModuleItem.java
│   │   │               │   │   ├── CodeCrackerItem.java
│   │   │               │   │   ├── DoorLockItem.java
│   │   │               │   │   ├── HackingToolItem.java
│   │   │               │   │   ├── KeyItem.java
│   │   │               │   │   ├── KeyRingItem.java
│   │   │               │   │   ├── LockItems.java
│   │   │               │   │   ├── LockPickItem.java
│   │   │               │   │   └── OmniHackItem.java
│   │   │               │   ├── network
│   │   │               │   │   ├── CodeEntryPacket.java
│   │   │               │   │   ├── LockNetworkHandler.java
│   │   │               │   │   └── OpenCodeEntryPacket.java
│   │   │               │   ├── DoorLockHandler.java
│   │   │               │   ├── LockCommand.java
│   │   │               │   ├── LockData.java
│   │   │               │   ├── LockManager.java
│   │   │               │   └── LockType.java
│   │   │               ├── lsd
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── DestillationsApparatBlockEntity.java
│   │   │               │   │   ├── FermentationsTankBlockEntity.java
│   │   │               │   │   ├── LSDBlockEntities.java
│   │   │               │   │   ├── MikroDosiererBlockEntity.java
│   │   │               │   │   └── PerforationsPresseBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── DestillationsApparatBlock.java
│   │   │               │   │   ├── FermentationsTankBlock.java
│   │   │               │   │   ├── LSDBlocks.java
│   │   │               │   │   ├── MikroDosiererBlock.java
│   │   │               │   │   └── PerforationsPresseBlock.java
│   │   │               │   ├── items
│   │   │               │   │   ├── BlotterItem.java
│   │   │               │   │   ├── BlotterPapierItem.java
│   │   │               │   │   ├── ErgotKulturItem.java
│   │   │               │   │   ├── LSDItems.java
│   │   │               │   │   ├── LSDLoesungItem.java
│   │   │               │   │   ├── LysergsaeureItem.java
│   │   │               │   │   └── MutterkornItem.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── LSDMenuTypes.java
│   │   │               │   │   └── MikroDosiererMenu.java
│   │   │               │   ├── screen
│   │   │               │   │   └── MikroDosiererScreen.java
│   │   │               │   ├── BlotterDesign.java
│   │   │               │   └── LSDDosage.java
│   │   │               ├── managers
│   │   │               │   ├── DailyRewardManager.java
│   │   │               │   ├── NPCEntityRegistry.java
│   │   │               │   ├── NPCNameRegistry.java
│   │   │               │   └── RentManager.java
│   │   │               ├── mapview
│   │   │               │   ├── config
│   │   │               │   │   ├── MapOption.java
│   │   │               │   │   ├── MapViewConfiguration.java
│   │   │               │   │   └── WorldMapConfiguration.java
│   │   │               │   ├── core
│   │   │               │   │   ├── event
│   │   │               │   │   │   ├── ChunkProcessEvent.java
│   │   │               │   │   │   ├── EventBridgeAdapter.java
│   │   │               │   │   │   ├── MapChangeListener.java
│   │   │               │   │   │   ├── MapEvent.java
│   │   │               │   │   │   ├── MapEventBus.java
│   │   │               │   │   │   ├── SettingsManager.java
│   │   │               │   │   │   ├── SubSettingsManager.java
│   │   │               │   │   │   └── WorldChangedEvent.java
│   │   │               │   │   └── model
│   │   │               │   │       ├── AbstractMapData.java
│   │   │               │   │       ├── BiomeData.java
│   │   │               │   │       ├── BlockModel.java
│   │   │               │   │       └── MapChunk.java
│   │   │               │   ├── data
│   │   │               │   │   ├── cache
│   │   │               │   │   │   ├── BlockPositionCache.java
│   │   │               │   │   │   ├── ComparisonRegionCache.java
│   │   │               │   │   │   ├── EmptyRegionCache.java
│   │   │               │   │   │   └── RegionCache.java
│   │   │               │   │   ├── persistence
│   │   │               │   │   │   ├── AsyncPersistenceManager.java
│   │   │               │   │   │   ├── CompressedImageData.java
│   │   │               │   │   │   ├── CompressedMapData.java
│   │   │               │   │   │   └── CompressionUtils.java
│   │   │               │   │   └── repository
│   │   │               │   │       └── MapDataRepository.java
│   │   │               │   ├── entityrender
│   │   │               │   │   ├── variants
│   │   │               │   │   │   ├── DefaultEntityVariantData.java
│   │   │               │   │   │   ├── DefaultEntityVariantDataFactory.java
│   │   │               │   │   │   ├── HorseVariantDataFactory.java
│   │   │               │   │   │   └── TropicalFishVariantDataFactory.java
│   │   │               │   │   ├── EntityVariantData.java
│   │   │               │   │   └── EntityVariantDataFactory.java
│   │   │               │   ├── integration
│   │   │               │   │   ├── forge
│   │   │               │   │   │   └── forge
│   │   │               │   │   │       ├── mixins
│   │   │               │   │   │       │   └── MixinRenderPipelines.java
│   │   │               │   │   │       ├── ForgeEvents.java
│   │   │               │   │   │       ├── ForgeModApiBridge.java
│   │   │               │   │   │       ├── ForgePacketBridge.java
│   │   │               │   │   │       ├── MapViewSettingsChannelHandlerForge.java
│   │   │               │   │   │       └── MapViewWorldIdChannelHandlerForge.java
│   │   │               │   │   ├── minecraft
│   │   │               │   │   │   ├── mixins
│   │   │               │   │   │   │   ├── APIMixinChatListenerHud.java
│   │   │               │   │   │   │   ├── APIMixinMinecraftClient.java
│   │   │               │   │   │   │   ├── APIMixinNetHandlerPlayClient.java
│   │   │               │   │   │   │   ├── AccessorEnderDragonRenderer.java
│   │   │               │   │   │   │   ├── MixinChatHud.java
│   │   │               │   │   │   │   ├── MixinInGameHud.java
│   │   │               │   │   │   │   └── MixinWorldRenderer.java
│   │   │               │   │   │   └── MinecraftAccessor.java
│   │   │               │   │   ├── network
│   │   │               │   │   │   ├── MapViewSettingsS2C.java
│   │   │               │   │   │   ├── WorldIdC2S.java
│   │   │               │   │   │   └── WorldIdS2C.java
│   │   │               │   │   ├── DebugRenderState.java
│   │   │               │   │   ├── Events.java
│   │   │               │   │   ├── ModApiBridge.java
│   │   │               │   │   └── PacketBridge.java
│   │   │               │   ├── navigation
│   │   │               │   │   └── graph
│   │   │               │   │       ├── NavigationOverlay.java
│   │   │               │   │       ├── NavigationPathOverlay.java
│   │   │               │   │       ├── NavigationTarget.java
│   │   │               │   │       ├── RoadBlockDetector.java
│   │   │               │   │       ├── RoadGraph.java
│   │   │               │   │       ├── RoadGraphBuilder.java
│   │   │               │   │       ├── RoadNavigationService.java
│   │   │               │   │       ├── RoadNode.java
│   │   │               │   │       ├── RoadPathRenderer.java
│   │   │               │   │       └── RoadSegment.java
│   │   │               │   ├── npc
│   │   │               │   │   ├── NPCActivityStatus.java
│   │   │               │   │   └── NPCMapRenderer.java
│   │   │               │   ├── presentation
│   │   │               │   │   ├── component
│   │   │               │   │   │   ├── OptionButton.java
│   │   │               │   │   │   ├── OptionSlider.java
│   │   │               │   │   │   ├── PopupButton.java
│   │   │               │   │   │   ├── PopupComponent.java
│   │   │               │   │   │   └── TextButton.java
│   │   │               │   │   ├── renderer
│   │   │               │   │   │   └── MapViewRenderer.java
│   │   │               │   │   └── screen
│   │   │               │   │       ├── BaseMapScreen.java
│   │   │               │   │       ├── IPopupScreen.java
│   │   │               │   │       ├── MapOptionsScreen.java
│   │   │               │   │       ├── PopupScreen.java
│   │   │               │   │       └── WorldMapScreen.java
│   │   │               │   ├── service
│   │   │               │   │   ├── coordination
│   │   │               │   │   │   ├── LifecycleService.java
│   │   │               │   │   │   ├── RenderCoordinationService.java
│   │   │               │   │   │   └── WorldStateService.java
│   │   │               │   │   ├── data
│   │   │               │   │   │   ├── ConfigNotificationService.java
│   │   │               │   │   │   ├── DimensionService.java
│   │   │               │   │   │   ├── MapDataManager.java
│   │   │               │   │   │   └── WorldMapData.java
│   │   │               │   │   ├── render
│   │   │               │   │   │   ├── strategy
│   │   │               │   │   │   │   ├── ChunkScanStrategy.java
│   │   │               │   │   │   │   ├── ChunkScanStrategyFactory.java
│   │   │               │   │   │   │   ├── GridScanStrategy.java
│   │   │               │   │   │   │   └── SpiralScanStrategy.java
│   │   │               │   │   │   ├── ColorCalculationService.java
│   │   │               │   │   │   ├── ColorUtils.java
│   │   │               │   │   │   └── LightingCalculator.java
│   │   │               │   │   └── scan
│   │   │               │   │       ├── BiomeScanner.java
│   │   │               │   │       ├── BlockStateAnalyzer.java
│   │   │               │   │       └── HeightCalculator.java
│   │   │               │   ├── textures
│   │   │               │   │   ├── IIconCreator.java
│   │   │               │   │   ├── Sprite.java
│   │   │               │   │   ├── Stitcher.java
│   │   │               │   │   ├── StitcherException.java
│   │   │               │   │   └── TextureAtlas.java
│   │   │               │   ├── util
│   │   │               │   │   ├── ARGBCompat.java
│   │   │               │   │   ├── AllocatedTexture.java
│   │   │               │   │   ├── BackgroundImageInfo.java
│   │   │               │   │   ├── BiomeColors.java
│   │   │               │   │   ├── BlockDatabase.java
│   │   │               │   │   ├── ChunkCache.java
│   │   │               │   │   ├── DimensionContainer.java
│   │   │               │   │   ├── DynamicMoveableTexture.java
│   │   │               │   │   ├── EasingUtils.java
│   │   │               │   │   ├── FloatBlitRenderState.java
│   │   │               │   │   ├── FourColoredRectangleRenderState.java
│   │   │               │   │   ├── GLUtils.java
│   │   │               │   │   ├── ImageHelper.java
│   │   │               │   │   ├── LayoutVariables.java
│   │   │               │   │   ├── MapViewCachedOrthoProjectionMatrixBuffer.java
│   │   │               │   │   ├── MapViewGuiGraphics.java
│   │   │               │   │   ├── MapViewHelper.java
│   │   │               │   │   ├── MapViewPipelines.java
│   │   │               │   │   ├── MapViewRenderTypes.java
│   │   │               │   │   ├── MessageUtils.java
│   │   │               │   │   ├── MutableBlockPos.java
│   │   │               │   │   ├── ReflectionUtils.java
│   │   │               │   │   ├── ScaledDynamicMutableTexture.java
│   │   │               │   │   ├── TextUtils.java
│   │   │               │   │   └── WorldUpdateListener.java
│   │   │               │   └── MapViewConstants.java
│   │   │               ├── market
│   │   │               │   ├── DynamicMarketManager.java
│   │   │               │   ├── MarketCommand.java
│   │   │               │   └── MarketData.java
│   │   │               ├── mdma
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── MDMABlockEntities.java
│   │   │               │   │   ├── PillenPresseBlockEntity.java
│   │   │               │   │   ├── ReaktionsKesselBlockEntity.java
│   │   │               │   │   └── TrocknungsOfenBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── MDMABlocks.java
│   │   │               │   │   ├── PillenPresseBlock.java
│   │   │               │   │   ├── ReaktionsKesselBlock.java
│   │   │               │   │   └── TrocknungsOfenBlock.java
│   │   │               │   ├── items
│   │   │               │   │   ├── BindemittelItem.java
│   │   │               │   │   ├── EcstasyPillItem.java
│   │   │               │   │   ├── FarbstoffItem.java
│   │   │               │   │   ├── MDMABaseItem.java
│   │   │               │   │   ├── MDMAItems.java
│   │   │               │   │   ├── MDMAKristallItem.java
│   │   │               │   │   └── SafrolItem.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── MDMAMenuTypes.java
│   │   │               │   │   └── PillenPresseMenu.java
│   │   │               │   ├── screen
│   │   │               │   │   └── PillenPresseScreen.java
│   │   │               │   ├── MDMAQuality.java
│   │   │               │   ├── PillColor.java
│   │   │               │   └── PillDesign.java
│   │   │               ├── messaging
│   │   │               │   ├── network
│   │   │               │   │   ├── MessageNetworkHandler.java
│   │   │               │   │   ├── ReceiveMessagePacket.java
│   │   │               │   │   └── SendMessagePacket.java
│   │   │               │   ├── Conversation.java
│   │   │               │   ├── HeadRenderer.java
│   │   │               │   ├── Message.java
│   │   │               │   ├── MessageManager.java
│   │   │               │   ├── MessageNotificationOverlay.java
│   │   │               │   └── NPCMessageTemplates.java
│   │   │               ├── meth
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── ChemieMixerBlockEntity.java
│   │   │               │   │   ├── KristallisatorBlockEntity.java
│   │   │               │   │   ├── MethBlockEntities.java
│   │   │               │   │   ├── ReduktionskesselBlockEntity.java
│   │   │               │   │   └── VakuumTrocknerBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── ChemieMixerBlock.java
│   │   │               │   │   ├── KristallisatorBlock.java
│   │   │               │   │   ├── MethBlocks.java
│   │   │               │   │   ├── ReduktionskesselBlock.java
│   │   │               │   │   └── VakuumTrocknerBlock.java
│   │   │               │   ├── items
│   │   │               │   │   ├── EphedrinItem.java
│   │   │               │   │   ├── JodItem.java
│   │   │               │   │   ├── KristallMethItem.java
│   │   │               │   │   ├── MethItem.java
│   │   │               │   │   ├── MethItems.java
│   │   │               │   │   ├── MethPasteItem.java
│   │   │               │   │   ├── PseudoephedrinItem.java
│   │   │               │   │   ├── RohMethItem.java
│   │   │               │   │   └── RoterPhosphorItem.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── MethMenuTypes.java
│   │   │               │   │   └── ReduktionskesselMenu.java
│   │   │               │   ├── screen
│   │   │               │   │   └── ReduktionskesselScreen.java
│   │   │               │   └── MethQuality.java
│   │   │               ├── mushroom
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── KlimalampeBlockEntity.java
│   │   │               │   │   ├── MushroomBlockEntities.java
│   │   │               │   │   └── WassertankBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── KlimalampeBlock.java
│   │   │               │   │   ├── KlimalampeTier.java
│   │   │               │   │   ├── MushroomBlocks.java
│   │   │               │   │   ├── TemperatureMode.java
│   │   │               │   │   └── WassertankBlock.java
│   │   │               │   ├── data
│   │   │               │   │   └── MushroomPlantData.java
│   │   │               │   ├── items
│   │   │               │   │   ├── DriedMushroomItem.java
│   │   │               │   │   ├── FreshMushroomItem.java
│   │   │               │   │   ├── MistBagItem.java
│   │   │               │   │   ├── MushroomItems.java
│   │   │               │   │   └── SporeSyringeItem.java
│   │   │               │   └── MushroomType.java
│   │   │               ├── network
│   │   │               │   └── AbstractPacket.java
│   │   │               ├── npc
│   │   │               │   ├── bank
│   │   │               │   │   ├── StockMarketData.java
│   │   │               │   │   ├── StockTradingTracker.java
│   │   │               │   │   └── TransferLimitTracker.java
│   │   │               │   ├── client
│   │   │               │   │   ├── model
│   │   │               │   │   │   └── CustomNPCModel.java
│   │   │               │   │   ├── renderer
│   │   │               │   │   │   ├── CustomNPCRenderer.java
│   │   │               │   │   │   └── CustomSkinManager.java
│   │   │               │   │   ├── screen
│   │   │               │   │   │   ├── BankerScreen.java
│   │   │               │   │   │   ├── BoerseScreen.java
│   │   │               │   │   │   ├── CreditAdvisorScreen.java
│   │   │               │   │   │   ├── MerchantShopScreen.java
│   │   │               │   │   │   ├── NPCInteractionScreen.java
│   │   │               │   │   │   ├── NPCSpawnerScreen.java
│   │   │               │   │   │   ├── ShopEditorScreen.java
│   │   │               │   │   │   └── StealingScreen.java
│   │   │               │   │   ├── ClientNPCNameCache.java
│   │   │               │   │   └── NPCClientEvents.java
│   │   │               │   ├── commands
│   │   │               │   │   ├── AdminToolsCommand.java
│   │   │               │   │   └── NPCCommand.java
│   │   │               │   ├── crime
│   │   │               │   │   ├── prison
│   │   │               │   │   │   ├── client
│   │   │               │   │   │   │   └── PrisonScreen.java
│   │   │               │   │   │   ├── network
│   │   │               │   │   │   │   ├── ClientPrisonScreenHandler.java
│   │   │               │   │   │   │   ├── ClosePrisonScreenPacket.java
│   │   │               │   │   │   │   ├── OpenPrisonScreenPacket.java
│   │   │               │   │   │   │   ├── PayBailPacket.java
│   │   │               │   │   │   │   ├── PrisonNetworkHandler.java
│   │   │               │   │   │   │   └── UpdatePrisonBalancePacket.java
│   │   │               │   │   │   ├── PrisonCell.java
│   │   │               │   │   │   ├── PrisonCommand.java
│   │   │               │   │   │   ├── PrisonEventHandler.java
│   │   │               │   │   │   └── PrisonManager.java
│   │   │               │   │   ├── BountyCommand.java
│   │   │               │   │   ├── BountyData.java
│   │   │               │   │   ├── BountyManager.java
│   │   │               │   │   ├── CrimeManager.java
│   │   │               │   │   ├── CrimeRecord.java
│   │   │               │   │   └── CrimeType.java
│   │   │               │   ├── data
│   │   │               │   │   ├── BankCategory.java
│   │   │               │   │   ├── MerchantCategory.java
│   │   │               │   │   ├── MerchantShopDefaults.java
│   │   │               │   │   ├── NPCData.java
│   │   │               │   │   ├── NPCPersonality.java
│   │   │               │   │   ├── NPCType.java
│   │   │               │   │   └── ServiceCategory.java
│   │   │               │   ├── entity
│   │   │               │   │   ├── CustomNPCEntity.java
│   │   │               │   │   └── NPCEntities.java
│   │   │               │   ├── events
│   │   │               │   │   ├── EntityRemoverHandler.java
│   │   │               │   │   ├── IllegalActivityScanner.java
│   │   │               │   │   ├── NPCDailySalaryHandler.java
│   │   │               │   │   ├── NPCKnockoutHandler.java
│   │   │               │   │   ├── NPCNameSyncHandler.java
│   │   │               │   │   ├── NPCStealingHandler.java
│   │   │               │   │   ├── PoliceAIHandler.java
│   │   │               │   │   ├── PoliceBackupSystem.java
│   │   │               │   │   ├── PoliceDoorBlockHandler.java
│   │   │               │   │   ├── PoliceRaidPenalty.java
│   │   │               │   │   ├── PoliceSearchBehavior.java
│   │   │               │   │   └── RoomScanner.java
│   │   │               │   ├── goals
│   │   │               │   │   ├── MoveToHomeGoal.java
│   │   │               │   │   ├── MoveToLeisureGoal.java
│   │   │               │   │   ├── MoveToWorkGoal.java
│   │   │               │   │   ├── PolicePatrolGoal.java
│   │   │               │   │   └── PoliceStationGoal.java
│   │   │               │   ├── items
│   │   │               │   │   ├── EntityRemoverItem.java
│   │   │               │   │   ├── NPCItems.java
│   │   │               │   │   ├── NPCLeisureTool.java
│   │   │               │   │   ├── NPCLocationTool.java
│   │   │               │   │   ├── NPCPatrolTool.java
│   │   │               │   │   └── NPCSpawnerTool.java
│   │   │               │   ├── life
│   │   │               │   │   ├── behavior
│   │   │               │   │   │   ├── BehaviorAction.java
│   │   │               │   │   │   ├── BehaviorPriority.java
│   │   │               │   │   │   ├── BehaviorState.java
│   │   │               │   │   │   ├── NPCBehaviorEngine.java
│   │   │               │   │   │   └── StandardActions.java
│   │   │               │   │   ├── companion
│   │   │               │   │   │   ├── CompanionBehavior.java
│   │   │               │   │   │   ├── CompanionData.java
│   │   │               │   │   │   ├── CompanionEventHandler.java
│   │   │               │   │   │   ├── CompanionManager.java
│   │   │               │   │   │   └── CompanionType.java
│   │   │               │   │   ├── core
│   │   │               │   │   │   ├── EmotionState.java
│   │   │               │   │   │   ├── MemoryType.java
│   │   │               │   │   │   ├── NPCEmotions.java
│   │   │               │   │   │   ├── NPCLifeData.java
│   │   │               │   │   │   ├── NPCMemory.java
│   │   │               │   │   │   ├── NPCNeeds.java
│   │   │               │   │   │   ├── NPCTraits.java
│   │   │               │   │   │   └── NeedType.java
│   │   │               │   │   ├── dialogue
│   │   │               │   │   │   ├── DialogueAction.java
│   │   │               │   │   │   ├── DialogueCondition.java
│   │   │               │   │   │   ├── DialogueContext.java
│   │   │               │   │   │   ├── DialogueHelper.java
│   │   │               │   │   │   ├── DialogueManager.java
│   │   │               │   │   │   ├── DialogueNode.java
│   │   │               │   │   │   ├── DialogueOption.java
│   │   │               │   │   │   ├── DialogueTree.java
│   │   │               │   │   │   └── NPCDialogueProvider.java
│   │   │               │   │   ├── economy
│   │   │               │   │   │   ├── DynamicPriceManager.java
│   │   │               │   │   │   ├── MarketCondition.java
│   │   │               │   │   │   ├── NegotiationSystem.java
│   │   │               │   │   │   ├── PriceModifier.java
│   │   │               │   │   │   └── TradeEventHelper.java
│   │   │               │   │   ├── quest
│   │   │               │   │   │   ├── Quest.java
│   │   │               │   │   │   ├── QuestEventHandler.java
│   │   │               │   │   │   ├── QuestManager.java
│   │   │               │   │   │   ├── QuestObjective.java
│   │   │               │   │   │   ├── QuestProgress.java
│   │   │               │   │   │   ├── QuestReward.java
│   │   │               │   │   │   └── QuestType.java
│   │   │               │   │   ├── social
│   │   │               │   │   │   ├── Faction.java
│   │   │               │   │   │   ├── FactionManager.java
│   │   │               │   │   │   ├── FactionRelation.java
│   │   │               │   │   │   ├── NPCInteractionManager.java
│   │   │               │   │   │   ├── Rumor.java
│   │   │               │   │   │   ├── RumorNetwork.java
│   │   │               │   │   │   └── RumorType.java
│   │   │               │   │   ├── witness
│   │   │               │   │   │   ├── BriberySystem.java
│   │   │               │   │   │   ├── CrimeEventHandler.java
│   │   │               │   │   │   ├── CrimeType.java
│   │   │               │   │   │   ├── WitnessManager.java
│   │   │               │   │   │   └── WitnessReport.java
│   │   │               │   │   ├── world
│   │   │               │   │   │   ├── WorldEvent.java
│   │   │               │   │   │   ├── WorldEventManager.java
│   │   │               │   │   │   └── WorldEventType.java
│   │   │               │   │   ├── NPCLifeConstants.java
│   │   │               │   │   ├── NPCLifeSystemEvents.java
│   │   │               │   │   ├── NPCLifeSystemIntegration.java
│   │   │               │   │   └── NPCLifeSystemSavedData.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── BankerMenu.java
│   │   │               │   │   ├── BoerseMenu.java
│   │   │               │   │   ├── CreditAdvisorMenu.java
│   │   │               │   │   ├── MerchantShopMenu.java
│   │   │               │   │   ├── NPCInteractionMenu.java
│   │   │               │   │   ├── NPCMenuTypes.java
│   │   │               │   │   ├── NPCSpawnerMenu.java
│   │   │               │   │   ├── ShopEditorMenu.java
│   │   │               │   │   └── StealingMenu.java
│   │   │               │   ├── network
│   │   │               │   │   ├── ApplyCreditLoanPacket.java
│   │   │               │   │   ├── BankDepositPacket.java
│   │   │               │   │   ├── BankTransferPacket.java
│   │   │               │   │   ├── BankWithdrawPacket.java
│   │   │               │   │   ├── ClientCreditScreenHandler.java
│   │   │               │   │   ├── CreateRecurringPaymentPacket.java
│   │   │               │   │   ├── DeleteRecurringPaymentPacket.java
│   │   │               │   │   ├── DeltaSyncNPCNamesPacket.java
│   │   │               │   │   ├── NPCActionPacket.java
│   │   │               │   │   ├── NPCNetworkHandler.java
│   │   │               │   │   ├── OpenBankerMenuPacket.java
│   │   │               │   │   ├── OpenBoerseMenuPacket.java
│   │   │               │   │   ├── OpenCreditAdvisorMenuPacket.java
│   │   │               │   │   ├── OpenMerchantShopPacket.java
│   │   │               │   │   ├── OpenStealingMenuPacket.java
│   │   │               │   │   ├── PauseRecurringPaymentPacket.java
│   │   │               │   │   ├── PayFuelBillPacket.java
│   │   │               │   │   ├── PurchaseItemPacket.java
│   │   │               │   │   ├── RepayCreditLoanPacket.java
│   │   │               │   │   ├── RequestCreditDataPacket.java
│   │   │               │   │   ├── RequestStockDataPacket.java
│   │   │               │   │   ├── ResumeRecurringPaymentPacket.java
│   │   │               │   │   ├── SavingsDepositPacket.java
│   │   │               │   │   ├── SavingsWithdrawPacket.java
│   │   │               │   │   ├── SpawnNPCPacket.java
│   │   │               │   │   ├── StealingAttemptPacket.java
│   │   │               │   │   ├── StockTradePacket.java
│   │   │               │   │   ├── SyncCreditDataPacket.java
│   │   │               │   │   ├── SyncNPCBalancePacket.java
│   │   │               │   │   ├── SyncNPCDataPacket.java
│   │   │               │   │   ├── SyncNPCNamesPacket.java
│   │   │               │   │   ├── SyncStockDataPacket.java
│   │   │               │   │   ├── UpdateShopItemsPacket.java
│   │   │               │   │   └── WantedLevelSyncPacket.java
│   │   │               │   ├── pathfinding
│   │   │               │   │   ├── NPCNodeEvaluator.java
│   │   │               │   │   └── NPCPathNavigation.java
│   │   │               │   └── personality
│   │   │               │       ├── NPCPersonalityTrait.java
│   │   │               │       ├── NPCRelationship.java
│   │   │               │       └── NPCRelationshipManager.java
│   │   │               ├── player
│   │   │               │   ├── network
│   │   │               │   │   ├── ClientPlayerSettings.java
│   │   │               │   │   ├── PlayerSettingsNetworkHandler.java
│   │   │               │   │   ├── PlayerSettingsPacket.java
│   │   │               │   │   └── SyncPlayerSettingsPacket.java
│   │   │               │   ├── PlayerSettings.java
│   │   │               │   ├── PlayerSettingsManager.java
│   │   │               │   ├── PlayerTracker.java
│   │   │               │   └── ServiceContact.java
│   │   │               ├── poppy
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── HeroinRaffinerieBlockEntity.java
│   │   │               │   │   ├── KochstationBlockEntity.java
│   │   │               │   │   ├── OpiumPresseBlockEntity.java
│   │   │               │   │   ├── PoppyBlockEntities.java
│   │   │               │   │   └── RitzmaschineBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── HeroinRaffinerieBlock.java
│   │   │               │   │   ├── KochstationBlock.java
│   │   │               │   │   ├── OpiumPresseBlock.java
│   │   │               │   │   ├── PoppyBlocks.java
│   │   │               │   │   ├── PoppyPlantBlock.java
│   │   │               │   │   └── RitzmaschineBlock.java
│   │   │               │   ├── data
│   │   │               │   │   └── PoppyPlantData.java
│   │   │               │   ├── items
│   │   │               │   │   ├── HeroinItem.java
│   │   │               │   │   ├── MorphineItem.java
│   │   │               │   │   ├── PoppyItems.java
│   │   │               │   │   ├── PoppyPodItem.java
│   │   │               │   │   ├── PoppySeedItem.java
│   │   │               │   │   ├── RawOpiumItem.java
│   │   │               │   │   └── ScoringKnifeItem.java
│   │   │               │   └── PoppyType.java
│   │   │               ├── production
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── AbstractProcessingBlockEntity.java
│   │   │               │   │   ├── PlantPotBlockEntity.java
│   │   │               │   │   └── UnifiedProcessingBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── AbstractPlantBlock.java
│   │   │               │   │   ├── AbstractProcessingBlock.java
│   │   │               │   │   └── PlantPotBlock.java
│   │   │               │   ├── config
│   │   │               │   │   ├── ProductionConfig.java
│   │   │               │   │   └── ProductionRegistry.java
│   │   │               │   ├── core
│   │   │               │   │   ├── DrugType.java
│   │   │               │   │   ├── GenericPlantData.java
│   │   │               │   │   ├── GenericQuality.java
│   │   │               │   │   ├── PotType.java
│   │   │               │   │   ├── ProductionQuality.java
│   │   │               │   │   ├── ProductionStage.java
│   │   │               │   │   └── ProductionType.java
│   │   │               │   ├── data
│   │   │               │   │   └── PlantPotData.java
│   │   │               │   ├── growth
│   │   │               │   │   ├── AbstractPlantGrowthHandler.java
│   │   │               │   │   ├── CannabisGrowthHandler.java
│   │   │               │   │   ├── CocaGrowthHandler.java
│   │   │               │   │   ├── MushroomGrowthHandler.java
│   │   │               │   │   ├── PlantGrowthHandler.java
│   │   │               │   │   ├── PlantGrowthHandlerFactory.java
│   │   │               │   │   ├── PoppyGrowthHandler.java
│   │   │               │   │   └── TobaccoGrowthHandler.java
│   │   │               │   ├── items
│   │   │               │   │   └── PackagedDrugItem.java
│   │   │               │   ├── nbt
│   │   │               │   │   ├── CannabisPlantSerializer.java.disabled
│   │   │               │   │   ├── CocaPlantSerializer.java.disabled
│   │   │               │   │   ├── MushroomPlantSerializer.java.disabled
│   │   │               │   │   ├── PlantSerializer.java
│   │   │               │   │   ├── PlantSerializerFactory.java
│   │   │               │   │   ├── PoppyPlantSerializer.java.disabled
│   │   │               │   │   └── TobaccoPlantSerializer.java
│   │   │               │   └── ProductionSize.java
│   │   │               ├── region
│   │   │               │   ├── blocks
│   │   │               │   │   ├── PlotBlocks.java
│   │   │               │   │   └── PlotInfoBlock.java
│   │   │               │   ├── network
│   │   │               │   │   ├── PlotAbandonPacket.java
│   │   │               │   │   ├── PlotDescriptionPacket.java
│   │   │               │   │   ├── PlotNetworkHandler.java
│   │   │               │   │   ├── PlotPurchasePacket.java
│   │   │               │   │   ├── PlotRatingPacket.java
│   │   │               │   │   ├── PlotRenamePacket.java
│   │   │               │   │   ├── PlotSalePacket.java
│   │   │               │   │   └── PlotTrustPacket.java
│   │   │               │   ├── PlotArea.java
│   │   │               │   ├── PlotCache.java
│   │   │               │   ├── PlotChunkCache.java
│   │   │               │   ├── PlotManager.java
│   │   │               │   ├── PlotProtectionHandler.java
│   │   │               │   ├── PlotRegion.java
│   │   │               │   ├── PlotSpatialIndex.java
│   │   │               │   ├── PlotType.java
│   │   │               │   └── package-info.java
│   │   │               ├── territory
│   │   │               │   ├── network
│   │   │               │   │   ├── ClientMapScreenOpener.java
│   │   │               │   │   ├── OpenMapEditorPacket.java
│   │   │               │   │   ├── SetTerritoryPacket.java
│   │   │               │   │   ├── SyncTerritoriesPacket.java
│   │   │               │   │   ├── SyncTerritoryDeltaPacket.java
│   │   │               │   │   └── TerritoryNetworkHandler.java
│   │   │               │   ├── MapCommand.java
│   │   │               │   ├── Territory.java
│   │   │               │   ├── TerritoryManager.java
│   │   │               │   ├── TerritoryTracker.java
│   │   │               │   └── TerritoryType.java
│   │   │               ├── tobacco
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── AbstractDryingRackBlockEntity.java
│   │   │               │   │   ├── AbstractFermentationBarrelBlockEntity.java
│   │   │               │   │   ├── AbstractPackagingTableBlockEntity.java
│   │   │               │   │   ├── BigDryingRackBlockEntity.java
│   │   │               │   │   ├── BigFermentationBarrelBlockEntity.java
│   │   │               │   │   ├── GrowLightSlabBlockEntity.java
│   │   │               │   │   ├── LargePackagingTableBlockEntity.java
│   │   │               │   │   ├── MediumDryingRackBlockEntity.java
│   │   │               │   │   ├── MediumFermentationBarrelBlockEntity.java
│   │   │               │   │   ├── MediumPackagingTableBlockEntity.java
│   │   │               │   │   ├── SinkBlockEntity.java
│   │   │               │   │   ├── SmallDryingRackBlockEntity.java
│   │   │               │   │   ├── SmallFermentationBarrelBlockEntity.java
│   │   │               │   │   ├── SmallPackagingTableBlockEntity.java
│   │   │               │   │   └── TobaccoBlockEntities.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── AbstractDryingRackBlock.java
│   │   │               │   │   ├── AbstractPackagingTableBlock.java
│   │   │               │   │   ├── BigDryingRackBlock.java
│   │   │               │   │   ├── BigFermentationBarrelBlock.java
│   │   │               │   │   ├── GrowLightSlabBlock.java
│   │   │               │   │   ├── LargePackagingTableBlock.java
│   │   │               │   │   ├── MediumDryingRackBlock.java
│   │   │               │   │   ├── MediumFermentationBarrelBlock.java
│   │   │               │   │   ├── MediumPackagingTableBlock.java
│   │   │               │   │   ├── SinkBlock.java
│   │   │               │   │   ├── SmallDryingRackBlock.java
│   │   │               │   │   ├── SmallFermentationBarrelBlock.java
│   │   │               │   │   ├── SmallPackagingTableBlock.java
│   │   │               │   │   ├── TobaccoBlocks.java
│   │   │               │   │   └── TobaccoPlantBlock.java
│   │   │               │   ├── business
│   │   │               │   │   ├── BusinessMetricsUpdateHandler.java
│   │   │               │   │   ├── DemandLevel.java
│   │   │               │   │   ├── NPCAddictionProfile.java
│   │   │               │   │   ├── NPCBusinessMetrics.java
│   │   │               │   │   ├── NPCPurchaseDecision.java
│   │   │               │   │   ├── NPCResponse.java
│   │   │               │   │   ├── NegotiationEngine.java
│   │   │               │   │   ├── NegotiationScoreCalculator.java
│   │   │               │   │   ├── NegotiationState.java
│   │   │               │   │   ├── PriceCalculator.java
│   │   │               │   │   ├── Purchase.java
│   │   │               │   │   └── TobaccoBusinessConstants.java
│   │   │               │   ├── data
│   │   │               │   │   └── TobaccoPlantData.java
│   │   │               │   ├── entity
│   │   │               │   │   └── ModEntities.java
│   │   │               │   ├── events
│   │   │               │   │   └── TobaccoBottleHandler.java
│   │   │               │   ├── items
│   │   │               │   │   ├── DriedTobaccoLeafItem.java
│   │   │               │   │   ├── FermentedTobaccoLeafItem.java
│   │   │               │   │   ├── FreshTobaccoLeafItem.java
│   │   │               │   │   ├── PackagingBagItem.java
│   │   │               │   │   ├── PackagingBoxItem.java
│   │   │               │   │   ├── PackagingJarItem.java
│   │   │               │   │   ├── SoilBagItem.java
│   │   │               │   │   ├── TobaccoBottleItem.java
│   │   │               │   │   ├── TobaccoItems.java
│   │   │               │   │   ├── TobaccoSeedItem.java
│   │   │               │   │   └── WateringCanItem.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── BigDryingRackMenu.java
│   │   │               │   │   ├── LargePackagingTableMenu.java
│   │   │               │   │   ├── MediumDryingRackMenu.java
│   │   │               │   │   ├── MediumPackagingTableMenu.java
│   │   │               │   │   ├── ModMenuTypes.java
│   │   │               │   │   ├── SmallDryingRackMenu.java
│   │   │               │   │   ├── SmallPackagingTableMenu.java
│   │   │               │   │   └── TobaccoNegotiationMenu.java
│   │   │               │   ├── network
│   │   │               │   │   ├── ClientTobaccoScreenHandler.java
│   │   │               │   │   ├── LargePackageRequestPacket.java
│   │   │               │   │   ├── MediumPackageRequestPacket.java
│   │   │               │   │   ├── ModNetworking.java
│   │   │               │   │   ├── NegotiationPacket.java
│   │   │               │   │   ├── NegotiationResponsePacket.java
│   │   │               │   │   ├── OpenTobaccoNegotiationPacket.java
│   │   │               │   │   ├── PurchaseDecisionSyncPacket.java
│   │   │               │   │   └── SmallPackageRequestPacket.java
│   │   │               │   ├── screen
│   │   │               │   │   ├── BigDryingRackScreen.java
│   │   │               │   │   ├── LargePackagingTableScreen.java
│   │   │               │   │   ├── MediumDryingRackScreen.java
│   │   │               │   │   ├── MediumPackagingTableScreen.java
│   │   │               │   │   ├── SmallDryingRackScreen.java
│   │   │               │   │   ├── SmallPackagingTableScreen.java
│   │   │               │   │   └── TobaccoNegotiationScreen.java
│   │   │               │   ├── TobaccoQuality.java
│   │   │               │   └── TobaccoType.java
│   │   │               ├── towing
│   │   │               │   ├── menu
│   │   │               │   │   ├── TowingInvoiceMenu.java
│   │   │               │   │   └── TowingMenuTypes.java
│   │   │               │   ├── network
│   │   │               │   │   ├── ChangeMembershipPacket.java
│   │   │               │   │   ├── PayTowingInvoicePacket.java
│   │   │               │   │   ├── RequestTowingPacket.java
│   │   │               │   │   └── TowingNetworkHandler.java
│   │   │               │   ├── screen
│   │   │               │   │   └── TowingInvoiceScreen.java
│   │   │               │   ├── MembershipData.java
│   │   │               │   ├── MembershipManager.java
│   │   │               │   ├── MembershipTier.java
│   │   │               │   ├── TowingInvoiceData.java
│   │   │               │   ├── TowingServiceRegistry.java
│   │   │               │   ├── TowingTransaction.java
│   │   │               │   ├── TowingYardManager.java
│   │   │               │   └── TowingYardParkingSpot.java
│   │   │               ├── util
│   │   │               │   ├── AbstractPersistenceManager.java
│   │   │               │   ├── BackupManager.java
│   │   │               │   ├── ConfigCache.java
│   │   │               │   ├── EventHelper.java
│   │   │               │   ├── GsonHelper.java
│   │   │               │   ├── HealthCheckManager.java
│   │   │               │   ├── IncrementalSaveManager.java
│   │   │               │   ├── InputValidation.java
│   │   │               │   ├── LocaleHelper.java
│   │   │               │   ├── PacketHandler.java
│   │   │               │   ├── PerformanceMonitor.java
│   │   │               │   ├── PersistenceHelper.java
│   │   │               │   ├── RateLimiter.java
│   │   │               │   ├── SaveableWrapper.java
│   │   │               │   ├── ThreadPoolManager.java
│   │   │               │   ├── TickThrottler.java
│   │   │               │   ├── VersionChecker.java
│   │   │               │   ├── VersionedData.java
│   │   │               │   └── package-info.java
│   │   │               ├── utility
│   │   │               │   ├── commands
│   │   │               │   │   └── UtilityCommand.java
│   │   │               │   ├── IUtilityConsumer.java
│   │   │               │   ├── PlotUtilityData.java
│   │   │               │   ├── PlotUtilityManager.java
│   │   │               │   ├── UtilityCategory.java
│   │   │               │   ├── UtilityConsumptionData.java
│   │   │               │   ├── UtilityEventHandler.java
│   │   │               │   └── UtilityRegistry.java
│   │   │               ├── vehicle
│   │   │               │   ├── blocks
│   │   │               │   │   ├── fluid
│   │   │               │   │   │   └── VehicleFluidBlock.java
│   │   │               │   │   ├── tileentity
│   │   │               │   │   │   ├── render
│   │   │               │   │   │   │   └── TileentitySpecialRendererFuelStation.java
│   │   │               │   │   │   ├── FuelStationRegistry.java
│   │   │               │   │   │   ├── TileEntityBase.java
│   │   │               │   │   │   ├── TileEntityFuelStation.java
│   │   │               │   │   │   └── TileEntityWerkstatt.java
│   │   │               │   │   ├── BlockBase.java
│   │   │               │   │   ├── BlockFuelStation.java
│   │   │               │   │   ├── BlockFuelStationTop.java
│   │   │               │   │   ├── BlockGui.java
│   │   │               │   │   ├── BlockOrientableHorizontal.java
│   │   │               │   │   ├── BlockWerkstatt.java
│   │   │               │   │   └── ModBlocks.java
│   │   │               │   ├── entity
│   │   │               │   │   ├── model
│   │   │               │   │   │   └── GenericVehicleModel.java
│   │   │               │   │   └── vehicle
│   │   │               │   │       ├── base
│   │   │               │   │       │   ├── EntityGenericVehicle.java
│   │   │               │   │       │   └── EntityVehicleBase.java
│   │   │               │   │       ├── components
│   │   │               │   │       │   ├── BatteryComponent.java
│   │   │               │   │       │   ├── DamageComponent.java
│   │   │               │   │       │   ├── FuelComponent.java
│   │   │               │   │       │   ├── InventoryComponent.java
│   │   │               │   │       │   ├── PhysicsComponent.java
│   │   │               │   │       │   ├── SecurityComponent.java
│   │   │               │   │       │   └── VehicleComponent.java
│   │   │               │   │       ├── parts
│   │   │               │   │       │   ├── Part.java
│   │   │               │   │       │   ├── PartAllterrainTire.java
│   │   │               │   │       │   ├── PartBody.java
│   │   │               │   │       │   ├── PartBumper.java
│   │   │               │   │       │   ├── PartChassisBase.java
│   │   │               │   │       │   ├── PartChromeBumper.java
│   │   │               │   │       │   ├── PartContainer.java
│   │   │               │   │       │   ├── PartEngine.java
│   │   │               │   │       │   ├── PartHeavyDutyTire.java
│   │   │               │   │       │   ├── PartLicensePlateHolder.java
│   │   │               │   │       │   ├── PartLimousineChassis.java
│   │   │               │   │       │   ├── PartLuxusChassis.java
│   │   │               │   │       │   ├── PartModel.java
│   │   │               │   │       │   ├── PartNormalMotor.java
│   │   │               │   │       │   ├── PartOffroadChassis.java
│   │   │               │   │       │   ├── PartOffroadTire.java
│   │   │               │   │       │   ├── PartPerformance2Motor.java
│   │   │               │   │       │   ├── PartPerformanceMotor.java
│   │   │               │   │       │   ├── PartPremiumTire.java
│   │   │               │   │       │   ├── PartRegistry.java
│   │   │               │   │       │   ├── PartSportBumper.java
│   │   │               │   │       │   ├── PartSportTire.java
│   │   │               │   │       │   ├── PartStandardTire.java
│   │   │               │   │       │   ├── PartTank.java
│   │   │               │   │       │   ├── PartTankContainer.java
│   │   │               │   │       │   ├── PartTireBase.java
│   │   │               │   │       │   ├── PartTransporterBack.java
│   │   │               │   │       │   ├── PartTruckChassis.java
│   │   │               │   │       │   ├── PartVanChassis.java
│   │   │               │   │       │   └── TireSeasonType.java
│   │   │               │   │       └── VehicleFactory.java
│   │   │               │   ├── events
│   │   │               │   │   ├── BlockEvents.java
│   │   │               │   │   ├── KeyEvents.java
│   │   │               │   │   ├── PlayerEvents.java
│   │   │               │   │   ├── RenderEvents.java
│   │   │               │   │   ├── SoundEvents.java
│   │   │               │   │   └── VehicleSessionHandler.java
│   │   │               │   ├── fluids
│   │   │               │   │   ├── FluidBioDiesel.java
│   │   │               │   │   ├── FluidBioDieselFlowing.java
│   │   │               │   │   ├── FluidTypeVehicle.java
│   │   │               │   │   ├── IEffectApplyable.java
│   │   │               │   │   ├── ModFluidTags.java
│   │   │               │   │   ├── ModFluids.java
│   │   │               │   │   ├── VehicleFluidFlowing.java
│   │   │               │   │   └── VehicleFluidSource.java
│   │   │               │   ├── fuel
│   │   │               │   │   ├── FuelBillManager.java
│   │   │               │   │   └── FuelStationRegistry.java
│   │   │               │   ├── gui
│   │   │               │   │   ├── ContainerBase.java
│   │   │               │   │   ├── ContainerFactoryTileEntity.java
│   │   │               │   │   ├── ContainerFuelStation.java
│   │   │               │   │   ├── ContainerVehicle.java
│   │   │               │   │   ├── ContainerVehicleInventory.java
│   │   │               │   │   ├── ContainerWerkstatt.java
│   │   │               │   │   ├── GuiFuelStation.java
│   │   │               │   │   ├── GuiVehicle.java
│   │   │               │   │   ├── GuiVehicleInventory.java
│   │   │               │   │   ├── GuiWerkstatt.java
│   │   │               │   │   ├── SlotBattery.java
│   │   │               │   │   ├── SlotFuel.java
│   │   │               │   │   ├── SlotMaintenance.java
│   │   │               │   │   ├── SlotOneItem.java
│   │   │               │   │   ├── SlotPresent.java
│   │   │               │   │   ├── SlotRepairKit.java
│   │   │               │   │   ├── SlotResult.java
│   │   │               │   │   └── TileEntityContainerProvider.java
│   │   │               │   ├── items
│   │   │               │   │   ├── AbstractItemVehiclePart.java
│   │   │               │   │   ├── IVehiclePart.java
│   │   │               │   │   ├── ItemBattery.java
│   │   │               │   │   ├── ItemBioDieselCanister.java
│   │   │               │   │   ├── ItemCanister.java
│   │   │               │   │   ├── ItemCraftingComponent.java
│   │   │               │   │   ├── ItemKey.java
│   │   │               │   │   ├── ItemLicensePlate.java
│   │   │               │   │   ├── ItemRepairKit.java
│   │   │               │   │   ├── ItemSpawnVehicle.java
│   │   │               │   │   ├── ItemVehiclePart.java
│   │   │               │   │   ├── ModItems.java
│   │   │               │   │   └── VehicleSpawnTool.java
│   │   │               │   ├── mixins
│   │   │               │   │   ├── GuiMixin.java
│   │   │               │   │   └── SoundOptionsScreenMixin.java
│   │   │               │   ├── net
│   │   │               │   │   ├── MessageCenterVehicle.java
│   │   │               │   │   ├── MessageCenterVehicleClient.java
│   │   │               │   │   ├── MessageContainerOperation.java
│   │   │               │   │   ├── MessageControlVehicle.java
│   │   │               │   │   ├── MessageCrash.java
│   │   │               │   │   ├── MessageStartFuel.java
│   │   │               │   │   ├── MessageStarting.java
│   │   │               │   │   ├── MessageSyncTileEntity.java
│   │   │               │   │   ├── MessageVehicleGui.java
│   │   │               │   │   ├── MessageVehicleHorn.java
│   │   │               │   │   ├── MessageWerkstattCheckout.java
│   │   │               │   │   ├── MessageWerkstattPayment.java
│   │   │               │   │   ├── MessageWerkstattUpgrade.java
│   │   │               │   │   ├── UpgradeType.java
│   │   │               │   │   └── WerkstattCartItem.java
│   │   │               │   ├── sounds
│   │   │               │   │   ├── ModSounds.java
│   │   │               │   │   ├── SoundLoopHigh.java
│   │   │               │   │   ├── SoundLoopIdle.java
│   │   │               │   │   ├── SoundLoopStart.java
│   │   │               │   │   ├── SoundLoopStarting.java
│   │   │               │   │   ├── SoundLoopTileentity.java
│   │   │               │   │   └── SoundLoopVehicle.java
│   │   │               │   ├── util
│   │   │               │   │   ├── SereneSeasonsCompat.java
│   │   │               │   │   ├── UniqueBlockPosList.java
│   │   │               │   │   └── VehicleUtils.java
│   │   │               │   ├── vehicle
│   │   │               │   │   ├── VehicleOwnershipTracker.java
│   │   │               │   │   ├── VehiclePurchaseHandler.java
│   │   │               │   │   └── VehicleSpawnRegistry.java
│   │   │               │   ├── DamageSourceVehicle.java
│   │   │               │   ├── Main.java
│   │   │               │   ├── MixinConnector.java
│   │   │               │   ├── ModCreativeTabs.java
│   │   │               │   ├── PredicateUUID.java
│   │   │               │   └── VehicleConstants.java
│   │   │               ├── warehouse
│   │   │               │   ├── client
│   │   │               │   │   └── ClientWarehouseNPCCache.java
│   │   │               │   ├── commands
│   │   │               │   │   └── WarehouseCommand.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── WarehouseMenu.java
│   │   │               │   │   └── WarehouseMenuTypes.java
│   │   │               │   ├── network
│   │   │               │   │   ├── packet
│   │   │               │   │   │   ├── AddItemToSlotPacket.java
│   │   │               │   │   │   ├── AddSellerPacket.java
│   │   │               │   │   │   ├── AutoFillPacket.java
│   │   │               │   │   │   ├── ClearSlotPacket.java
│   │   │               │   │   │   ├── ModifySlotPacket.java
│   │   │               │   │   │   ├── RemoveSellerPacket.java
│   │   │               │   │   │   ├── UpdateSettingsPacket.java
│   │   │               │   │   │   └── UpdateSlotCapacityPacket.java
│   │   │               │   │   └── WarehouseNetworkHandler.java
│   │   │               │   ├── screen
│   │   │               │   │   └── WarehouseScreen.java
│   │   │               │   ├── ExpenseEntry.java
│   │   │               │   ├── WarehouseBlock.java
│   │   │               │   ├── WarehouseBlockEntity.java
│   │   │               │   ├── WarehouseBlocks.java
│   │   │               │   ├── WarehouseManager.java
│   │   │               │   └── WarehouseSlot.java
│   │   │               ├── wine
│   │   │               │   ├── blockentity
│   │   │               │   │   ├── AbstractAgingBarrelBlockEntity.java
│   │   │               │   │   ├── AbstractFermentationTankBlockEntity.java
│   │   │               │   │   ├── AbstractWinePressBlockEntity.java
│   │   │               │   │   ├── CrushingStationBlockEntity.java
│   │   │               │   │   ├── LargeAgingBarrelBlockEntity.java
│   │   │               │   │   ├── LargeFermentationTankBlockEntity.java
│   │   │               │   │   ├── LargeWinePressBlockEntity.java
│   │   │               │   │   ├── MediumAgingBarrelBlockEntity.java
│   │   │               │   │   ├── MediumFermentationTankBlockEntity.java
│   │   │               │   │   ├── MediumWinePressBlockEntity.java
│   │   │               │   │   ├── SmallAgingBarrelBlockEntity.java
│   │   │               │   │   ├── SmallFermentationTankBlockEntity.java
│   │   │               │   │   ├── SmallWinePressBlockEntity.java
│   │   │               │   │   ├── WineBlockEntities.java
│   │   │               │   │   └── WineBottlingStationBlockEntity.java
│   │   │               │   ├── blocks
│   │   │               │   │   ├── CrushingStationBlock.java
│   │   │               │   │   ├── GrapevineBlock.java
│   │   │               │   │   ├── GrapevinePotBlock.java
│   │   │               │   │   ├── LargeAgingBarrelBlock.java
│   │   │               │   │   ├── LargeFermentationTankBlock.java
│   │   │               │   │   ├── LargeWinePressBlock.java
│   │   │               │   │   ├── MediumAgingBarrelBlock.java
│   │   │               │   │   ├── MediumFermentationTankBlock.java
│   │   │               │   │   ├── MediumWinePressBlock.java
│   │   │               │   │   ├── SmallAgingBarrelBlock.java
│   │   │               │   │   ├── SmallFermentationTankBlock.java
│   │   │               │   │   ├── SmallWinePressBlock.java
│   │   │               │   │   ├── WineBlocks.java
│   │   │               │   │   └── WineBottlingStationBlock.java
│   │   │               │   ├── items
│   │   │               │   │   ├── GrapeItem.java
│   │   │               │   │   ├── GrapeSeedlingItem.java
│   │   │               │   │   ├── WineBottleItem.java
│   │   │               │   │   ├── WineGlassItem.java
│   │   │               │   │   └── WineItems.java
│   │   │               │   ├── menu
│   │   │               │   │   ├── CrushingStationMenu.java
│   │   │               │   │   ├── LargeAgingBarrelMenu.java
│   │   │               │   │   ├── LargeFermentationTankMenu.java
│   │   │               │   │   ├── LargeWinePressMenu.java
│   │   │               │   │   ├── MediumAgingBarrelMenu.java
│   │   │               │   │   ├── MediumFermentationTankMenu.java
│   │   │               │   │   ├── MediumWinePressMenu.java
│   │   │               │   │   ├── SmallAgingBarrelMenu.java
│   │   │               │   │   ├── SmallFermentationTankMenu.java
│   │   │               │   │   ├── SmallWinePressMenu.java
│   │   │               │   │   ├── WineBottlingStationMenu.java
│   │   │               │   │   └── WineMenuTypes.java
│   │   │               │   ├── network
│   │   │               │   │   ├── ProcessingMethodPacket.java
│   │   │               │   │   └── WineNetworking.java
│   │   │               │   ├── screen
│   │   │               │   │   ├── CrushingStationScreen.java
│   │   │               │   │   ├── LargeAgingBarrelScreen.java
│   │   │               │   │   ├── LargeFermentationTankScreen.java
│   │   │               │   │   ├── LargeWinePressScreen.java
│   │   │               │   │   ├── MediumAgingBarrelScreen.java
│   │   │               │   │   ├── MediumFermentationTankScreen.java
│   │   │               │   │   ├── MediumWinePressScreen.java
│   │   │               │   │   ├── SmallAgingBarrelScreen.java
│   │   │               │   │   ├── SmallFermentationTankScreen.java
│   │   │               │   │   ├── SmallWinePressScreen.java
│   │   │               │   │   └── WineBottlingStationScreen.java
│   │   │               │   ├── WineAgeLevel.java
│   │   │               │   ├── WineProcessingMethod.java
│   │   │               │   ├── WineQuality.java
│   │   │               │   └── WineType.java
│   │   │               ├── ModCreativeTabs.java
│   │   │               ├── ScheduleMC.java
│   │   │               └── package-info.java
│   │   └── resources
│   │       ├── META-INF
│   │       │   └── mods.toml
│   │       ├── assets
│   │       │   └── schedulemc
│   │       │       ├── blockstates
│   │       │       │   ├── advanced_grow_light_slab.json
│   │       │       │   ├── afghanisch_poppy_plant.json
│   │       │       │   ├── atm.json
│   │       │       │   ├── basic_grow_light_slab.json
│   │       │       │   ├── big_drying_rack.json
│   │       │       │   ├── big_extraction_vat.json
│   │       │       │   ├── big_fermentation_barrel.json
│   │       │       │   ├── big_refinery.json
│   │       │       │   ├── bolivianisch_coca_plant.json
│   │       │       │   ├── burley_plant.json
│   │       │       │   ├── cannabis_autoflower_plant.json
│   │       │       │   ├── cannabis_curing_glas.json
│   │       │       │   ├── cannabis_hash_presse.json
│   │       │       │   ├── cannabis_hybrid_plant.json
│   │       │       │   ├── cannabis_indica_plant.json
│   │       │       │   ├── cannabis_oel_extraktor.json
│   │       │       │   ├── cannabis_sativa_plant.json
│   │       │       │   ├── cannabis_trimm_station.json
│   │       │       │   ├── cannabis_trocknungsnetz.json
│   │       │       │   ├── cash_block.json
│   │       │       │   ├── ceramic_pot.json
│   │       │       │   ├── chemie_mixer.json
│   │       │       │   ├── crack_kocher.json
│   │       │       │   ├── destillations_apparat.json
│   │       │       │   ├── diesel.json
│   │       │       │   ├── fermentation_barrel.json
│   │       │       │   ├── fermentations_tank.json
│   │       │       │   ├── fuel_station.json
│   │       │       │   ├── fuel_station_top.json
│   │       │       │   ├── golden_pot.json
│   │       │       │   ├── havana_plant.json
│   │       │       │   ├── heroin_raffinerie.json
│   │       │       │   ├── indisch_poppy_plant.json
│   │       │       │   ├── iron_pot.json
│   │       │       │   ├── klimalampe_large.json
│   │       │       │   ├── klimalampe_medium.json
│   │       │       │   ├── klimalampe_small.json
│   │       │       │   ├── kochstation.json
│   │       │       │   ├── kolumbianisch_coca_plant.json
│   │       │       │   ├── kristallisator.json
│   │       │       │   ├── large_packaging_table.json
│   │       │       │   ├── medium_drying_rack.json
│   │       │       │   ├── medium_extraction_vat.json
│   │       │       │   ├── medium_fermentation_barrel.json
│   │       │       │   ├── medium_packaging_table.json
│   │       │       │   ├── medium_refinery.json
│   │       │       │   ├── mikro_dosierer.json
│   │       │       │   ├── opium_presse.json
│   │       │       │   ├── oriental_plant.json
│   │       │       │   ├── packaging_table.json
│   │       │       │   ├── perforations_presse.json
│   │       │       │   ├── pillen_presse.json
│   │       │       │   ├── plot_info_block.json
│   │       │       │   ├── premium_grow_light_slab.json
│   │       │       │   ├── reaktions_kessel.json
│   │       │       │   ├── reduktionskessel.json
│   │       │       │   ├── ritzmaschine.json
│   │       │       │   ├── sink.json
│   │       │       │   ├── small_drying_rack.json
│   │       │       │   ├── small_extraction_vat.json
│   │       │       │   ├── small_fermentation_barrel.json
│   │       │       │   ├── small_packaging_table.json
│   │       │       │   ├── small_refinery.json
│   │       │       │   ├── terracotta_pot.json
│   │       │       │   ├── trocknungs_ofen.json
│   │       │       │   ├── tuerkisch_poppy_plant.json
│   │       │       │   ├── vakuum_trockner.json
│   │       │       │   ├── virginia_plant.json
│   │       │       │   ├── warehouse.json
│   │       │       │   ├── wassertank.json
│   │       │       │   └── werkstatt.json
│   │       │       ├── lang
│   │       │       │   ├── de_de.json
│   │       │       │   ├── de_de.json.FULL_BACKUP
│   │       │       │   ├── de_de.json.tmp
│   │       │       │   ├── en_us.json
│   │       │       │   ├── en_us.json.FULL_BACKUP
│   │       │       │   └── en_us.json.tmp
│   │       │       ├── mapview
│   │       │       │   ├── conf
│   │       │       │   │   └── biomecolors.txt
│   │       │       │   └── images
│   │       │       │       ├── circle.png
│   │       │       │       ├── colorpicker.png
│   │       │       │       ├── mmarrow.png
│   │       │       │       ├── roundmap.png
│   │       │       │       ├── square.png
│   │       │       │       └── squaremap.png
│   │       │       ├── models
│   │       │       │   ├── block
│   │       │       │   │   ├── advanced_grow_light_slab.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage0.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage0_top.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage1.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage1_top.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage2.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage2_top.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage3.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage3_top.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage4.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage4_top.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage5.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage5_top.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage6.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage6_top.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage7.json
│   │       │       │   │   ├── afghanisch_poppy_plant_stage7_top.json
│   │       │       │   │   ├── atm_lower.json
│   │       │       │   │   ├── atm_upper.json
│   │       │       │   │   ├── basic_grow_light_slab.json
│   │       │       │   │   ├── big_drying_rack.json
│   │       │       │   │   ├── big_extraction_vat.json
│   │       │       │   │   ├── big_fermentation_barrel.json
│   │       │       │   │   ├── big_refinery.json
│   │       │       │   │   ├── block_big.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage0.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage0_top.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage1.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage1_top.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage2.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage2_top.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage3.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage3_top.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage4.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage4_top.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage5.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage5_top.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage6.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage6_top.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage7.json
│   │       │       │   │   ├── bolivianisch_coca_plant_stage7_top.json
│   │       │       │   │   ├── burley_plant_stage0.json
│   │       │       │   │   ├── burley_plant_stage0_top.json
│   │       │       │   │   ├── burley_plant_stage1.json
│   │       │       │   │   ├── burley_plant_stage1_top.json
│   │       │       │   │   ├── burley_plant_stage2.json
│   │       │       │   │   ├── burley_plant_stage2_top.json
│   │       │       │   │   ├── burley_plant_stage3.json
│   │       │       │   │   ├── burley_plant_stage3_top.json
│   │       │       │   │   ├── burley_plant_stage4.json
│   │       │       │   │   ├── burley_plant_stage4_top.json
│   │       │       │   │   ├── burley_plant_stage5.json
│   │       │       │   │   ├── burley_plant_stage5_top.json
│   │       │       │   │   ├── burley_plant_stage6.json
│   │       │       │   │   ├── burley_plant_stage6_top.json
│   │       │       │   │   ├── burley_plant_stage7.json
│   │       │       │   │   ├── burley_plant_stage7_top.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage0.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage0_top.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage1.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage1_top.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage2.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage2_top.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage3.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage3_top.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage4.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage4_top.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage5.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage5_top.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage6.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage6_top.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage7.json
│   │       │       │   │   ├── cannabis_autoflower_plant_stage7_top.json
│   │       │       │   │   ├── cannabis_curing_glas.json
│   │       │       │   │   ├── cannabis_hash_presse.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage0.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage0_top.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage1.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage1_top.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage2.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage2_top.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage3.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage3_top.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage4.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage4_top.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage5.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage5_top.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage6.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage6_top.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage7.json
│   │       │       │   │   ├── cannabis_hybrid_plant_stage7_top.json
│   │       │       │   │   ├── cannabis_indica_plant_stage0.json
│   │       │       │   │   ├── cannabis_indica_plant_stage0_top.json
│   │       │       │   │   ├── cannabis_indica_plant_stage1.json
│   │       │       │   │   ├── cannabis_indica_plant_stage1_top.json
│   │       │       │   │   ├── cannabis_indica_plant_stage2.json
│   │       │       │   │   ├── cannabis_indica_plant_stage2_top.json
│   │       │       │   │   ├── cannabis_indica_plant_stage3.json
│   │       │       │   │   ├── cannabis_indica_plant_stage3_top.json
│   │       │       │   │   ├── cannabis_indica_plant_stage4.json
│   │       │       │   │   ├── cannabis_indica_plant_stage4_top.json
│   │       │       │   │   ├── cannabis_indica_plant_stage5.json
│   │       │       │   │   ├── cannabis_indica_plant_stage5_top.json
│   │       │       │   │   ├── cannabis_indica_plant_stage6.json
│   │       │       │   │   ├── cannabis_indica_plant_stage6_top.json
│   │       │       │   │   ├── cannabis_indica_plant_stage7.json
│   │       │       │   │   ├── cannabis_indica_plant_stage7_top.json
│   │       │       │   │   ├── cannabis_oel_extraktor.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage0.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage0_top.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage1.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage1_top.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage2.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage2_top.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage3.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage3_top.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage4.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage4_top.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage5.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage5_top.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage6.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage6_top.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage7.json
│   │       │       │   │   ├── cannabis_sativa_plant_stage7_top.json
│   │       │       │   │   ├── cannabis_trimm_station.json
│   │       │       │   │   ├── cannabis_trocknungsnetz.json
│   │       │       │   │   ├── cash_block.json
│   │       │       │   │   ├── ceramic_pot.json
│   │       │       │   │   ├── chemie_mixer.json
│   │       │       │   │   ├── crack_kocher.json
│   │       │       │   │   ├── destillations_apparat.json
│   │       │       │   │   ├── diesel.json
│   │       │       │   │   ├── drying_rack_empty.json
│   │       │       │   │   ├── fermentation_barrel.json
│   │       │       │   │   ├── fermentations_tank.json
│   │       │       │   │   ├── fuel_station.json
│   │       │       │   │   ├── fuel_station_top.json
│   │       │       │   │   ├── golden_pot.json
│   │       │       │   │   ├── havana_plant_stage0.json
│   │       │       │   │   ├── havana_plant_stage0_top.json
│   │       │       │   │   ├── havana_plant_stage1.json
│   │       │       │   │   ├── havana_plant_stage1_top.json
│   │       │       │   │   ├── havana_plant_stage2.json
│   │       │       │   │   ├── havana_plant_stage2_top.json
│   │       │       │   │   ├── havana_plant_stage3.json
│   │       │       │   │   ├── havana_plant_stage3_top.json
│   │       │       │   │   ├── havana_plant_stage4.json
│   │       │       │   │   ├── havana_plant_stage4_top.json
│   │       │       │   │   ├── havana_plant_stage5.json
│   │       │       │   │   ├── havana_plant_stage5_top.json
│   │       │       │   │   ├── havana_plant_stage6.json
│   │       │       │   │   ├── havana_plant_stage6_top.json
│   │       │       │   │   ├── havana_plant_stage7.json
│   │       │       │   │   ├── havana_plant_stage7_top.json
│   │       │       │   │   ├── heroin_raffinerie.json
│   │       │       │   │   ├── indisch_poppy_plant_stage0.json
│   │       │       │   │   ├── indisch_poppy_plant_stage0_top.json
│   │       │       │   │   ├── indisch_poppy_plant_stage1.json
│   │       │       │   │   ├── indisch_poppy_plant_stage1_top.json
│   │       │       │   │   ├── indisch_poppy_plant_stage2.json
│   │       │       │   │   ├── indisch_poppy_plant_stage2_top.json
│   │       │       │   │   ├── indisch_poppy_plant_stage3.json
│   │       │       │   │   ├── indisch_poppy_plant_stage3_top.json
│   │       │       │   │   ├── indisch_poppy_plant_stage4.json
│   │       │       │   │   ├── indisch_poppy_plant_stage4_top.json
│   │       │       │   │   ├── indisch_poppy_plant_stage5.json
│   │       │       │   │   ├── indisch_poppy_plant_stage5_top.json
│   │       │       │   │   ├── indisch_poppy_plant_stage6.json
│   │       │       │   │   ├── indisch_poppy_plant_stage6_top.json
│   │       │       │   │   ├── indisch_poppy_plant_stage7.json
│   │       │       │   │   ├── indisch_poppy_plant_stage7_top.json
│   │       │       │   │   ├── iron_pot.json
│   │       │       │   │   ├── klimalampe_large_cold.json
│   │       │       │   │   ├── klimalampe_large_off.json
│   │       │       │   │   ├── klimalampe_large_warm.json
│   │       │       │   │   ├── klimalampe_medium_cold.json
│   │       │       │   │   ├── klimalampe_medium_off.json
│   │       │       │   │   ├── klimalampe_medium_warm.json
│   │       │       │   │   ├── klimalampe_small_cold.json
│   │       │       │   │   ├── klimalampe_small_off.json
│   │       │       │   │   ├── klimalampe_small_warm.json
│   │       │       │   │   ├── kochstation.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage0.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage0_top.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage1.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage1_top.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage2.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage2_top.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage3.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage3_top.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage4.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage4_top.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage5.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage5_top.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage6.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage6_top.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage7.json
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage7_top.json
│   │       │       │   │   ├── kristallisator.json
│   │       │       │   │   ├── large_packaging_table.json
│   │       │       │   │   ├── medium_drying_rack.json
│   │       │       │   │   ├── medium_extraction_vat.json
│   │       │       │   │   ├── medium_fermentation_barrel.json
│   │       │       │   │   ├── medium_packaging_table.json
│   │       │       │   │   ├── medium_refinery.json
│   │       │       │   │   ├── mikro_dosierer.json
│   │       │       │   │   ├── opium_presse.json
│   │       │       │   │   ├── oriental_plant_stage0.json
│   │       │       │   │   ├── oriental_plant_stage0_top.json
│   │       │       │   │   ├── oriental_plant_stage1.json
│   │       │       │   │   ├── oriental_plant_stage1_top.json
│   │       │       │   │   ├── oriental_plant_stage2.json
│   │       │       │   │   ├── oriental_plant_stage2_top.json
│   │       │       │   │   ├── oriental_plant_stage3.json
│   │       │       │   │   ├── oriental_plant_stage3_top.json
│   │       │       │   │   ├── oriental_plant_stage4.json
│   │       │       │   │   ├── oriental_plant_stage4_top.json
│   │       │       │   │   ├── oriental_plant_stage5.json
│   │       │       │   │   ├── oriental_plant_stage5_top.json
│   │       │       │   │   ├── oriental_plant_stage6.json
│   │       │       │   │   ├── oriental_plant_stage6_top.json
│   │       │       │   │   ├── oriental_plant_stage7.json
│   │       │       │   │   ├── oriental_plant_stage7_top.json
│   │       │       │   │   ├── packaging_table_empty.json
│   │       │       │   │   ├── perforations_presse.json
│   │       │       │   │   ├── pillen_presse.json
│   │       │       │   │   ├── plot_info_block.json
│   │       │       │   │   ├── premium_grow_light_slab.json
│   │       │       │   │   ├── reaktions_kessel.json
│   │       │       │   │   ├── reduktionskessel.json
│   │       │       │   │   ├── ritzmaschine.json
│   │       │       │   │   ├── sink.json
│   │       │       │   │   ├── slope.json
│   │       │       │   │   ├── small_drying_rack.json
│   │       │       │   │   ├── small_extraction_vat.json
│   │       │       │   │   ├── small_fermentation_barrel.json
│   │       │       │   │   ├── small_packaging_table.json
│   │       │       │   │   ├── small_refinery.json
│   │       │       │   │   ├── terracotta_pot.json
│   │       │       │   │   ├── trocknungs_ofen.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage0.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage0_top.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage1.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage1_top.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage2.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage2_top.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage3.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage3_top.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage4.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage4_top.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage5.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage5_top.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage6.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage6_top.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage7.json
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage7_top.json
│   │       │       │   │   ├── vakuum_trockner.json
│   │       │       │   │   ├── virginia_plant_stage0.json
│   │       │       │   │   ├── virginia_plant_stage0_top.json
│   │       │       │   │   ├── virginia_plant_stage1.json
│   │       │       │   │   ├── virginia_plant_stage1_top.json
│   │       │       │   │   ├── virginia_plant_stage2.json
│   │       │       │   │   ├── virginia_plant_stage2_top.json
│   │       │       │   │   ├── virginia_plant_stage3.json
│   │       │       │   │   ├── virginia_plant_stage3_top.json
│   │       │       │   │   ├── virginia_plant_stage4.json
│   │       │       │   │   ├── virginia_plant_stage4_top.json
│   │       │       │   │   ├── virginia_plant_stage5.json
│   │       │       │   │   ├── virginia_plant_stage5_top.json
│   │       │       │   │   ├── virginia_plant_stage6.json
│   │       │       │   │   ├── virginia_plant_stage6_top.json
│   │       │       │   │   ├── virginia_plant_stage7.json
│   │       │       │   │   ├── virginia_plant_stage7_top.json
│   │       │       │   │   ├── warehouse.json
│   │       │       │   │   ├── wassertank.json
│   │       │       │   │   └── werkstatt.json
│   │       │       │   ├── entity
│   │       │       │   │   ├── big_wheel.mtl
│   │       │       │   │   ├── big_wheel.obj
│   │       │       │   │   ├── container.mtl
│   │       │       │   │   ├── container.obj
│   │       │       │   │   ├── license_plate.mtl
│   │       │       │   │   ├── license_plate.obj
│   │       │       │   │   ├── sport_body.mtl
│   │       │       │   │   ├── sport_body.obj
│   │       │       │   │   ├── suv_body.mtl
│   │       │       │   │   ├── suv_body.obj
│   │       │       │   │   ├── tank_container.mtl
│   │       │       │   │   ├── tank_container.obj
│   │       │       │   │   ├── transporter_body.mtl
│   │       │       │   │   ├── transporter_body.obj
│   │       │       │   │   ├── wheel.mtl
│   │       │       │   │   ├── wheel.obj
│   │       │       │   │   ├── wood_body.mtl
│   │       │       │   │   ├── wood_body.obj
│   │       │       │   │   ├── wood_body_big.mtl
│   │       │       │   │   ├── wood_body_big.obj
│   │       │       │   │   ├── wood_bumper.mtl
│   │       │       │   │   └── wood_bumper.obj
│   │       │       │   └── item
│   │       │       │       ├── advanced_grow_light_slab.json
│   │       │       │       ├── afghanisch_poppy_seeds.json
│   │       │       │       ├── allterrain_tire.json
│   │       │       │       ├── atm.json
│   │       │       │       ├── backpulver.json
│   │       │       │       ├── basic_grow_light_slab.json
│   │       │       │       ├── beer_bottle.json
│   │       │       │       ├── big_drying_rack.json
│   │       │       │       ├── big_extraction_vat.json
│   │       │       │       ├── big_fermentation_barrel.json
│   │       │       │       ├── big_refinery.json
│   │       │       │       ├── bindemittel.json
│   │       │       │       ├── bio_diesel.json
│   │       │       │       ├── blotter_papier.json
│   │       │       │       ├── bolivianisch_coca_seeds.json
│   │       │       │       ├── brewed_coffee.json
│   │       │       │       ├── burley_seeds.json
│   │       │       │       ├── camembert_wedge.json
│   │       │       │       ├── camembert_wheel.json
│   │       │       │       ├── cannabis_curing_glas.json
│   │       │       │       ├── cannabis_hash.json
│   │       │       │       ├── cannabis_hash_presse.json
│   │       │       │       ├── cannabis_oel_extraktor.json
│   │       │       │       ├── cannabis_oil.json
│   │       │       │       ├── cannabis_seed.json
│   │       │       │       ├── cannabis_trim.json
│   │       │       │       ├── cannabis_trimm_station.json
│   │       │       │       ├── cannabis_trocknungsnetz.json
│   │       │       │       ├── cargo_module.json
│   │       │       │       ├── cash.json
│   │       │       │       ├── cash_block.json
│   │       │       │       ├── ceramic_pot.json
│   │       │       │       ├── chardonnay_grapes.json
│   │       │       │       ├── cheese_curd.json
│   │       │       │       ├── cheese_wedge.json
│   │       │       │       ├── cheese_wheel.json
│   │       │       │       ├── chemie_mixer.json
│   │       │       │       ├── chocolate_bar_100g.json
│   │       │       │       ├── chocolate_bar_200g.json
│   │       │       │       ├── chocolate_bar_500g.json
│   │       │       │       ├── coca_paste.json
│   │       │       │       ├── cocaine.json
│   │       │       │       ├── coffee_package_1kg.json
│   │       │       │       ├── coffee_package_250g.json
│   │       │       │       ├── coffee_package_500g.json
│   │       │       │       ├── crack_kocher.json
│   │       │       │       ├── crack_rock.json
│   │       │       │       ├── cured_cannabis_bud.json
│   │       │       │       ├── destillations_apparat.json
│   │       │       │       ├── diesel_bucket.json
│   │       │       │       ├── diesel_canister.json
│   │       │       │       ├── dried_azurescens.json
│   │       │       │       ├── dried_burley_leaf.json
│   │       │       │       ├── dried_cannabis_bud.json
│   │       │       │       ├── dried_cubensis.json
│   │       │       │       ├── dried_havana_leaf.json
│   │       │       │       ├── dried_mexicana.json
│   │       │       │       ├── dried_oriental_leaf.json
│   │       │       │       ├── dried_virginia_leaf.json
│   │       │       │       ├── drying_rack.json
│   │       │       │       ├── ecstasy_pill.json
│   │       │       │       ├── emmental_wedge.json
│   │       │       │       ├── emmental_wheel.json
│   │       │       │       ├── empty_diesel_can.json
│   │       │       │       ├── ephedrin.json
│   │       │       │       ├── ergot_kultur.json
│   │       │       │       ├── espresso.json
│   │       │       │       ├── extraction_solvent.json
│   │       │       │       ├── fender_basic.json
│   │       │       │       ├── fender_chrome.json
│   │       │       │       ├── fender_sport.json
│   │       │       │       ├── fermentation_barrel.json
│   │       │       │       ├── fermentations_tank.json
│   │       │       │       ├── fermented_burley_leaf.json
│   │       │       │       ├── fermented_havana_leaf.json
│   │       │       │       ├── fermented_oriental_leaf.json
│   │       │       │       ├── fermented_virginia_leaf.json
│   │       │       │       ├── fertilizer_bottle.json
│   │       │       │       ├── fluid_module.json
│   │       │       │       ├── fresh_azurescens.json
│   │       │       │       ├── fresh_bolivianisch_coca_leaf.json
│   │       │       │       ├── fresh_burley_leaf.json
│   │       │       │       ├── fresh_cannabis_bud.json
│   │       │       │       ├── fresh_cubensis.json
│   │       │       │       ├── fresh_havana_leaf.json
│   │       │       │       ├── fresh_kolumbianisch_coca_leaf.json
│   │       │       │       ├── fresh_mexicana.json
│   │       │       │       ├── fresh_oriental_leaf.json
│   │       │       │       ├── fresh_peruanisch_coca_leaf.json
│   │       │       │       ├── fresh_virginia_leaf.json
│   │       │       │       ├── fuel_station.json
│   │       │       │       ├── full_diesel_can.json
│   │       │       │       ├── glass_of_wine.json
│   │       │       │       ├── golden_pot.json
│   │       │       │       ├── gouda_wedge.json
│   │       │       │       ├── gouda_wheel.json
│   │       │       │       ├── ground_coffee.json
│   │       │       │       ├── growth_booster_bottle.json
│   │       │       │       ├── havana_seeds.json
│   │       │       │       ├── heavyduty_tire.json
│   │       │       │       ├── herb_cheese.json
│   │       │       │       ├── heroin.json
│   │       │       │       ├── heroin_raffinerie.json
│   │       │       │       ├── honey_jar_1kg.json
│   │       │       │       ├── honey_jar_250g.json
│   │       │       │       ├── honey_jar_500g.json
│   │       │       │       ├── indisch_poppy_seeds.json
│   │       │       │       ├── iron_pot.json
│   │       │       │       ├── jod.json
│   │       │       │       ├── key.json
│   │       │       │       ├── klimalampe_large.json
│   │       │       │       ├── klimalampe_medium.json
│   │       │       │       ├── klimalampe_small.json
│   │       │       │       ├── kochstation.json
│   │       │       │       ├── kolumbianisch_coca_seeds.json
│   │       │       │       ├── kristall_meth.json
│   │       │       │       ├── kristallisator.json
│   │       │       │       ├── large_packaging_table.json
│   │       │       │       ├── license_sign.json
│   │       │       │       ├── license_sign_mount.json
│   │       │       │       ├── limousine.json
│   │       │       │       ├── limousine_chassis.json
│   │       │       │       ├── lsd_blotter.json
│   │       │       │       ├── lsd_loesung.json
│   │       │       │       ├── luxus_chassis.json
│   │       │       │       ├── lysergsaeure.json
│   │       │       │       ├── maintenance_kit.json
│   │       │       │       ├── mdma_base.json
│   │       │       │       ├── mdma_kristall.json
│   │       │       │       ├── medium_drying_rack.json
│   │       │       │       ├── medium_extraction_vat.json
│   │       │       │       ├── medium_fermentation_barrel.json
│   │       │       │       ├── medium_packaging_table.json
│   │       │       │       ├── medium_refinery.json
│   │       │       │       ├── merlot_grapes.json
│   │       │       │       ├── meth.json
│   │       │       │       ├── meth_paste.json
│   │       │       │       ├── mikro_dosierer.json
│   │       │       │       ├── mist_bag_large.json
│   │       │       │       ├── mist_bag_medium.json
│   │       │       │       ├── mist_bag_small.json
│   │       │       │       ├── morphine.json
│   │       │       │       ├── mutterkorn.json
│   │       │       │       ├── normal_motor.json
│   │       │       │       ├── npc_leisure_tool.json
│   │       │       │       ├── npc_location_tool.json
│   │       │       │       ├── npc_patrol_tool.json
│   │       │       │       ├── npc_spawner_tool.json
│   │       │       │       ├── offroad_chassis.json
│   │       │       │       ├── offroad_tire.json
│   │       │       │       ├── opium_presse.json
│   │       │       │       ├── oriental_seeds.json
│   │       │       │       ├── packaged_drug.json
│   │       │       │       ├── packaged_tobacco.json
│   │       │       │       ├── packaging_bag.json
│   │       │       │       ├── packaging_box.json
│   │       │       │       ├── packaging_jar.json
│   │       │       │       ├── packaging_table.json
│   │       │       │       ├── parmesan_wedge.json
│   │       │       │       ├── parmesan_wheel.json
│   │       │       │       ├── path_staff.json
│   │       │       │       ├── path_staff_model.json
│   │       │       │       ├── perforations_presse.json
│   │       │       │       ├── performance_2_motor.json
│   │       │       │       ├── performance_motor.json
│   │       │       │       ├── peruanisch_coca_seeds.json
│   │       │       │       ├── pillen_farbstoff.json
│   │       │       │       ├── pillen_presse.json
│   │       │       │       ├── plot_info_block.json
│   │       │       │       ├── plot_selection_tool.json
│   │       │       │       ├── pollen_press_mold.json
│   │       │       │       ├── poppy_pod.json
│   │       │       │       ├── premium_grow_light_slab.json
│   │       │       │       ├── premium_tire.json
│   │       │       │       ├── pseudoephedrin.json
│   │       │       │       ├── quality_booster_bottle.json
│   │       │       │       ├── raw_opium.json
│   │       │       │       ├── reaktions_kessel.json
│   │       │       │       ├── reduktionskessel.json
│   │       │       │       ├── riesling_grapes.json
│   │       │       │       ├── ritzmaschine.json
│   │       │       │       ├── roasted_coffee_beans.json
│   │       │       │       ├── roh_meth.json
│   │       │       │       ├── roter_phosphor.json
│   │       │       │       ├── safrol.json
│   │       │       │       ├── scoring_knife.json
│   │       │       │       ├── sink.json
│   │       │       │       ├── small_drying_rack.json
│   │       │       │       ├── small_extraction_vat.json
│   │       │       │       ├── small_fermentation_barrel.json
│   │       │       │       ├── small_packaging_table.json
│   │       │       │       ├── small_refinery.json
│   │       │       │       ├── smoked_cheese.json
│   │       │       │       ├── soil_bag_large.json
│   │       │       │       ├── soil_bag_medium.json
│   │       │       │       ├── soil_bag_small.json
│   │       │       │       ├── spaetburgunder_grapes.json
│   │       │       │       ├── spawn_tool.json
│   │       │       │       ├── spore_syringe_azurescens.json
│   │       │       │       ├── spore_syringe_cubensis.json
│   │       │       │       ├── spore_syringe_mexicana.json
│   │       │       │       ├── sport_tire.json
│   │       │       │       ├── sports_car.json
│   │       │       │       ├── standard_front_fender.json
│   │       │       │       ├── standard_tire.json
│   │       │       │       ├── starter_battery.json
│   │       │       │       ├── suv.json
│   │       │       │       ├── tank_15l.json
│   │       │       │       ├── tank_30l.json
│   │       │       │       ├── tank_50l.json
│   │       │       │       ├── terracotta_pot.json
│   │       │       │       ├── trimmed_cannabis_bud.json
│   │       │       │       ├── trocknungs_ofen.json
│   │       │       │       ├── truck.json
│   │       │       │       ├── truck_chassis.json
│   │       │       │       ├── tuerkisch_poppy_seeds.json
│   │       │       │       ├── vakuum_trockner.json
│   │       │       │       ├── van.json
│   │       │       │       ├── van_chassis.json
│   │       │       │       ├── virginia_seeds.json
│   │       │       │       ├── warehouse.json
│   │       │       │       ├── wassertank.json
│   │       │       │       ├── watering_can.json
│   │       │       │       ├── werkstatt.json
│   │       │       │       ├── wine_bottle_1500ml.json
│   │       │       │       ├── wine_bottle_375ml.json
│   │       │       │       ├── wine_bottle_750ml.json
│   │       │       │       └── worker_spawn_egg_model.json
│   │       │       ├── skins
│   │       │       │   ├── .gitkeep
│   │       │       │   └── 1.png
│   │       │       ├── sounds
│   │       │       │   ├── fuel_station.ogg
│   │       │       │   ├── fuel_station_attendant_1.ogg
│   │       │       │   ├── fuel_station_attendant_2.ogg
│   │       │       │   ├── fuel_station_attendant_3.ogg
│   │       │       │   ├── generator.ogg
│   │       │       │   ├── motor_fail.ogg
│   │       │       │   ├── motor_high.ogg
│   │       │       │   ├── motor_idle.ogg
│   │       │       │   ├── motor_start.ogg
│   │       │       │   ├── motor_starting.ogg
│   │       │       │   ├── motor_stop.ogg
│   │       │       │   ├── performance_2_motor_fail.ogg
│   │       │       │   ├── performance_2_motor_high.ogg
│   │       │       │   ├── performance_2_motor_idle.ogg
│   │       │       │   ├── performance_2_motor_start.ogg
│   │       │       │   ├── performance_2_motor_starting.ogg
│   │       │       │   ├── performance_2_motor_stop.ogg
│   │       │       │   ├── performance_motor_fail.ogg
│   │       │       │   ├── performance_motor_high.ogg
│   │       │       │   ├── performance_motor_idle.ogg
│   │       │       │   ├── performance_motor_start.ogg
│   │       │       │   ├── performance_motor_starting.ogg
│   │       │       │   ├── performance_motor_stop.ogg
│   │       │       │   ├── ratchet_1.ogg
│   │       │       │   ├── ratchet_2.ogg
│   │       │       │   ├── ratchet_3.ogg
│   │       │       │   ├── vehicle_crash.ogg
│   │       │       │   ├── vehicle_horn.ogg
│   │       │       │   ├── vehicle_lock.ogg
│   │       │       │   └── vehicle_unlock.ogg
│   │       │       ├── textures
│   │       │       │   ├── block
│   │       │       │   │   ├── advanced_grow_light_slab.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage0.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage0_top.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage1.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage1_top.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage2.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage2_top.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage3.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage3_top.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage4.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage4_top.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage5.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage5_top.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage6.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage6_top.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage7.png
│   │       │       │   │   ├── afghanisch_poppy_plant_stage7_top.png
│   │       │       │   │   ├── atm.png
│   │       │       │   │   ├── basic_grow_light_slab.png
│   │       │       │   │   ├── big_drying_rack.png
│   │       │       │   │   ├── big_extraction_vat.png
│   │       │       │   │   ├── big_fermentation_barrel.png
│   │       │       │   │   ├── big_refinery.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage0.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage0_top.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage1.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage1_top.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage2.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage2_top.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage3.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage3_top.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage4.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage4_top.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage5.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage5_top.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage6.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage6_top.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage7.png
│   │       │       │   │   ├── bolivianisch_coca_plant_stage7_top.png
│   │       │       │   │   ├── burley_plant_stage0.png
│   │       │       │   │   ├── burley_plant_stage0_top.png
│   │       │       │   │   ├── burley_plant_stage1.png
│   │       │       │   │   ├── burley_plant_stage1_top.png
│   │       │       │   │   ├── burley_plant_stage2.png
│   │       │       │   │   ├── burley_plant_stage2_top.png
│   │       │       │   │   ├── burley_plant_stage3.png
│   │       │       │   │   ├── burley_plant_stage3_top.png
│   │       │       │   │   ├── burley_plant_stage4.png
│   │       │       │   │   ├── burley_plant_stage4_top.png
│   │       │       │   │   ├── burley_plant_stage5.png
│   │       │       │   │   ├── burley_plant_stage5_top.png
│   │       │       │   │   ├── burley_plant_stage6.png
│   │       │       │   │   ├── burley_plant_stage6_top.png
│   │       │       │   │   ├── burley_plant_stage7.png
│   │       │       │   │   ├── burley_plant_stage7_top.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage0.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage0_top.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage1.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage1_top.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage2.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage2_top.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage3.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage3_top.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage4.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage4_top.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage5.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage5_top.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage6.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage6_top.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage7.png
│   │       │       │   │   ├── cannabis_autoflower_plant_stage7_top.png
│   │       │       │   │   ├── cannabis_curing_glas.png
│   │       │       │   │   ├── cannabis_hash_presse.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage0.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage0_top.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage1.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage1_top.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage2.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage2_top.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage3.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage3_top.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage4.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage4_top.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage5.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage5_top.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage6.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage6_top.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage7.png
│   │       │       │   │   ├── cannabis_hybrid_plant_stage7_top.png
│   │       │       │   │   ├── cannabis_indica_plant_stage0.png
│   │       │       │   │   ├── cannabis_indica_plant_stage0_top.png
│   │       │       │   │   ├── cannabis_indica_plant_stage1.png
│   │       │       │   │   ├── cannabis_indica_plant_stage1_top.png
│   │       │       │   │   ├── cannabis_indica_plant_stage2.png
│   │       │       │   │   ├── cannabis_indica_plant_stage2_top.png
│   │       │       │   │   ├── cannabis_indica_plant_stage3.png
│   │       │       │   │   ├── cannabis_indica_plant_stage3_top.png
│   │       │       │   │   ├── cannabis_indica_plant_stage4.png
│   │       │       │   │   ├── cannabis_indica_plant_stage4_top.png
│   │       │       │   │   ├── cannabis_indica_plant_stage5.png
│   │       │       │   │   ├── cannabis_indica_plant_stage5_top.png
│   │       │       │   │   ├── cannabis_indica_plant_stage6.png
│   │       │       │   │   ├── cannabis_indica_plant_stage6_top.png
│   │       │       │   │   ├── cannabis_indica_plant_stage7.png
│   │       │       │   │   ├── cannabis_indica_plant_stage7_top.png
│   │       │       │   │   ├── cannabis_oel_extraktor.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage0.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage0_top.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage1.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage1_top.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage2.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage2_top.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage3.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage3_top.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage4.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage4_top.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage5.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage5_top.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage6.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage6_top.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage7.png
│   │       │       │   │   ├── cannabis_sativa_plant_stage7_top.png
│   │       │       │   │   ├── cannabis_trimm_station.png
│   │       │       │   │   ├── cannabis_trocknungsnetz.png
│   │       │       │   │   ├── cash_block.png
│   │       │       │   │   ├── ceramic_pot.png
│   │       │       │   │   ├── chemie_mixer.png
│   │       │       │   │   ├── crack_kocher.png
│   │       │       │   │   ├── destillations_apparat.png
│   │       │       │   │   ├── diesel.png
│   │       │       │   │   ├── diesel_flowing.png
│   │       │       │   │   ├── diesel_flowing.png.mcmeta
│   │       │       │   │   ├── diesel_still.png
│   │       │       │   │   ├── diesel_still.png.mcmeta
│   │       │       │   │   ├── fermentation_barrel.png
│   │       │       │   │   ├── fermentations_tank.png
│   │       │       │   │   ├── fuel_station.png
│   │       │       │   │   ├── fuel_station_arms.png
│   │       │       │   │   ├── fuel_station_base_north_south.png
│   │       │       │   │   ├── fuel_station_base_up_down.png
│   │       │       │   │   ├── fuel_station_base_west_east.png
│   │       │       │   │   ├── fuel_station_head.png
│   │       │       │   │   ├── fuel_station_head_down.png
│   │       │       │   │   ├── fuel_station_top.png
│   │       │       │   │   ├── golden_pot.png
│   │       │       │   │   ├── havana_plant_stage0.png
│   │       │       │   │   ├── havana_plant_stage0_top.png
│   │       │       │   │   ├── havana_plant_stage1.png
│   │       │       │   │   ├── havana_plant_stage1_top.png
│   │       │       │   │   ├── havana_plant_stage2.png
│   │       │       │   │   ├── havana_plant_stage2_top.png
│   │       │       │   │   ├── havana_plant_stage3.png
│   │       │       │   │   ├── havana_plant_stage3_top.png
│   │       │       │   │   ├── havana_plant_stage4.png
│   │       │       │   │   ├── havana_plant_stage4_top.png
│   │       │       │   │   ├── havana_plant_stage5.png
│   │       │       │   │   ├── havana_plant_stage5_top.png
│   │       │       │   │   ├── havana_plant_stage6.png
│   │       │       │   │   ├── havana_plant_stage6_top.png
│   │       │       │   │   ├── havana_plant_stage7.png
│   │       │       │   │   ├── havana_plant_stage7_top.png
│   │       │       │   │   ├── heroin_raffinerie.png
│   │       │       │   │   ├── indisch_poppy_plant_stage0.png
│   │       │       │   │   ├── indisch_poppy_plant_stage0_top.png
│   │       │       │   │   ├── indisch_poppy_plant_stage1.png
│   │       │       │   │   ├── indisch_poppy_plant_stage1_top.png
│   │       │       │   │   ├── indisch_poppy_plant_stage2.png
│   │       │       │   │   ├── indisch_poppy_plant_stage2_top.png
│   │       │       │   │   ├── indisch_poppy_plant_stage3.png
│   │       │       │   │   ├── indisch_poppy_plant_stage3_top.png
│   │       │       │   │   ├── indisch_poppy_plant_stage4.png
│   │       │       │   │   ├── indisch_poppy_plant_stage4_top.png
│   │       │       │   │   ├── indisch_poppy_plant_stage5.png
│   │       │       │   │   ├── indisch_poppy_plant_stage5_top.png
│   │       │       │   │   ├── indisch_poppy_plant_stage6.png
│   │       │       │   │   ├── indisch_poppy_plant_stage6_top.png
│   │       │       │   │   ├── indisch_poppy_plant_stage7.png
│   │       │       │   │   ├── indisch_poppy_plant_stage7_top.png
│   │       │       │   │   ├── iron_pot.png
│   │       │       │   │   ├── klimalampe_large_cold.png
│   │       │       │   │   ├── klimalampe_large_off.png
│   │       │       │   │   ├── klimalampe_large_warm.png
│   │       │       │   │   ├── klimalampe_medium_cold.png
│   │       │       │   │   ├── klimalampe_medium_off.png
│   │       │       │   │   ├── klimalampe_medium_warm.png
│   │       │       │   │   ├── klimalampe_small_cold.png
│   │       │       │   │   ├── klimalampe_small_off.png
│   │       │       │   │   ├── klimalampe_small_warm.png
│   │       │       │   │   ├── kochstation.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage0.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage0_top.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage1.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage1_top.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage2.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage2_top.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage3.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage3_top.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage4.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage4_top.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage5.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage5_top.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage6.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage6_top.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage7.png
│   │       │       │   │   ├── kolumbianisch_coca_plant_stage7_top.png
│   │       │       │   │   ├── kristallisator.png
│   │       │       │   │   ├── large_packaging_table.png
│   │       │       │   │   ├── medium_drying_rack.png
│   │       │       │   │   ├── medium_extraction_vat.png
│   │       │       │   │   ├── medium_fermentation_barrel.png
│   │       │       │   │   ├── medium_packaging_table.png
│   │       │       │   │   ├── medium_refinery.png
│   │       │       │   │   ├── mikro_dosierer.png
│   │       │       │   │   ├── opium_presse.png
│   │       │       │   │   ├── oriental_plant_stage0.png
│   │       │       │   │   ├── oriental_plant_stage0_top.png
│   │       │       │   │   ├── oriental_plant_stage1.png
│   │       │       │   │   ├── oriental_plant_stage1_top.png
│   │       │       │   │   ├── oriental_plant_stage2.png
│   │       │       │   │   ├── oriental_plant_stage2_top.png
│   │       │       │   │   ├── oriental_plant_stage3.png
│   │       │       │   │   ├── oriental_plant_stage3_top.png
│   │       │       │   │   ├── oriental_plant_stage4.png
│   │       │       │   │   ├── oriental_plant_stage4_top.png
│   │       │       │   │   ├── oriental_plant_stage5.png
│   │       │       │   │   ├── oriental_plant_stage5_top.png
│   │       │       │   │   ├── oriental_plant_stage6.png
│   │       │       │   │   ├── oriental_plant_stage6_top.png
│   │       │       │   │   ├── oriental_plant_stage7.png
│   │       │       │   │   ├── oriental_plant_stage7_top.png
│   │       │       │   │   ├── perforations_presse.png
│   │       │       │   │   ├── pillen_presse.png
│   │       │       │   │   ├── plot_info_block.png
│   │       │       │   │   ├── premium_grow_light_slab.png
│   │       │       │   │   ├── reaktions_kessel.png
│   │       │       │   │   ├── reduktionskessel.png
│   │       │       │   │   ├── ritzmaschine.png
│   │       │       │   │   ├── sink.png
│   │       │       │   │   ├── small_drying_rack.png
│   │       │       │   │   ├── small_extraction_vat.png
│   │       │       │   │   ├── small_fermentation_barrel.png
│   │       │       │   │   ├── small_packaging_table.png
│   │       │       │   │   ├── small_refinery.png
│   │       │       │   │   ├── terracotta_pot.png
│   │       │       │   │   ├── trocknungs_ofen.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage0.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage0_top.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage1.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage1_top.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage2.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage2_top.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage3.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage3_top.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage4.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage4_top.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage5.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage5_top.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage6.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage6_top.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage7.png
│   │       │       │   │   ├── tuerkisch_poppy_plant_stage7_top.png
│   │       │       │   │   ├── vakuum_trockner.png
│   │       │       │   │   ├── virginia_plant_stage0.png
│   │       │       │   │   ├── virginia_plant_stage0_top.png
│   │       │       │   │   ├── virginia_plant_stage1.png
│   │       │       │   │   ├── virginia_plant_stage1_top.png
│   │       │       │   │   ├── virginia_plant_stage2.png
│   │       │       │   │   ├── virginia_plant_stage2_top.png
│   │       │       │   │   ├── virginia_plant_stage3.png
│   │       │       │   │   ├── virginia_plant_stage3_top.png
│   │       │       │   │   ├── virginia_plant_stage4.png
│   │       │       │   │   ├── virginia_plant_stage4_top.png
│   │       │       │   │   ├── virginia_plant_stage5.png
│   │       │       │   │   ├── virginia_plant_stage5_top.png
│   │       │       │   │   ├── virginia_plant_stage6.png
│   │       │       │   │   ├── virginia_plant_stage6_top.png
│   │       │       │   │   ├── virginia_plant_stage7.png
│   │       │       │   │   ├── virginia_plant_stage7_top.png
│   │       │       │   │   ├── warehouse.png
│   │       │       │   │   ├── wassertank.png
│   │       │       │   │   └── werkstatt.png
│   │       │       │   ├── entity
│   │       │       │   │   ├── npc
│   │       │       │   │   │   └── default.png
│   │       │       │   │   ├── allterrain_wheel.png
│   │       │       │   │   ├── big_wheel.png
│   │       │       │   │   ├── container_white.png
│   │       │       │   │   ├── heavyduty_wheel.png
│   │       │       │   │   ├── premium_wheel.png
│   │       │       │   │   ├── sport_wheel.png
│   │       │       │   │   ├── tank_container_white.png
│   │       │       │   │   ├── vehicle_big_wood_black.png
│   │       │       │   │   ├── vehicle_big_wood_blue.png
│   │       │       │   │   ├── vehicle_big_wood_oak.png
│   │       │       │   │   ├── vehicle_big_wood_red.png
│   │       │       │   │   ├── vehicle_big_wood_yellow.png
│   │       │       │   │   ├── vehicle_sport_black.png
│   │       │       │   │   ├── vehicle_sport_blue.png
│   │       │       │   │   ├── vehicle_sport_red.png
│   │       │       │   │   ├── vehicle_sport_white.png
│   │       │       │   │   ├── vehicle_sport_yellow.png
│   │       │       │   │   ├── vehicle_suv_black.png
│   │       │       │   │   ├── vehicle_suv_blue.png
│   │       │       │   │   ├── vehicle_suv_red.png
│   │       │       │   │   ├── vehicle_suv_white.png
│   │       │       │   │   ├── vehicle_suv_yellow.png
│   │       │       │   │   ├── vehicle_transporter_black.png
│   │       │       │   │   ├── vehicle_transporter_blue.png
│   │       │       │   │   ├── vehicle_transporter_red.png
│   │       │       │   │   ├── vehicle_transporter_white.png
│   │       │       │   │   ├── vehicle_transporter_yellow.png
│   │       │       │   │   ├── vehicle_wood_black.png
│   │       │       │   │   ├── vehicle_wood_blue.png
│   │       │       │   │   ├── vehicle_wood_oak.png
│   │       │       │   │   ├── vehicle_wood_red.png
│   │       │       │   │   ├── vehicle_wood_yellow.png
│   │       │       │   │   └── wheel.png
│   │       │       │   ├── gui
│   │       │       │   │   ├── apps
│   │       │       │   │   │   ├── README.md
│   │       │       │   │   │   ├── app_achievement.png
│   │       │       │   │   │   ├── app_bank.png
│   │       │       │   │   │   ├── app_contacts.png
│   │       │       │   │   │   ├── app_crime.png
│   │       │       │   │   │   ├── app_dealer.png
│   │       │       │   │   │   ├── app_map.png
│   │       │       │   │   │   ├── app_messages.png
│   │       │       │   │   │   ├── app_order.png
│   │       │       │   │   │   ├── app_plot.png
│   │       │       │   │   │   ├── app_products.png
│   │       │       │   │   │   ├── app_settings.png
│   │       │       │   │   │   ├── app_towing.png
│   │       │       │   │   │   └── close.png
│   │       │       │   │   ├── boerse_gui.png
│   │       │       │   │   ├── gui_fuel_station.png
│   │       │       │   │   ├── gui_generator.png
│   │       │       │   │   ├── gui_vehicle.png
│   │       │       │   │   ├── gui_werkstatt.png
│   │       │       │   │   ├── merchant_shop.png
│   │       │       │   │   ├── npc_interaction.png
│   │       │       │   │   ├── npc_spawner.png
│   │       │       │   │   ├── stealing.png
│   │       │       │   │   └── towing_invoice.png
│   │       │       │   ├── item
│   │       │       │   │   ├── advanced_grow_light_slab.png
│   │       │       │   │   ├── afghanisch_poppy_seeds.png
│   │       │       │   │   ├── allterrain_tire.png
│   │       │       │   │   ├── atm.png
│   │       │       │   │   ├── backpulver.png
│   │       │       │   │   ├── basic_grow_light_slab.png
│   │       │       │   │   ├── beer_bottle.png
│   │       │       │   │   ├── big_drying_rack.png
│   │       │       │   │   ├── big_fermentation_barrel.png
│   │       │       │   │   ├── bindemittel.png
│   │       │       │   │   ├── blotter_papier.png
│   │       │       │   │   ├── bolivianisch_coca_seeds.png
│   │       │       │   │   ├── brewed_coffee.png
│   │       │       │   │   ├── burley_seeds.png
│   │       │       │   │   ├── camembert_wedge.png
│   │       │       │   │   ├── camembert_wheel.png
│   │       │       │   │   ├── cannabis_curing_glas.png
│   │       │       │   │   ├── cannabis_hash.png
│   │       │       │   │   ├── cannabis_hash_presse.png
│   │       │       │   │   ├── cannabis_oel_extraktor.png
│   │       │       │   │   ├── cannabis_oil.png
│   │       │       │   │   ├── cannabis_seed_autoflower.png
│   │       │       │   │   ├── cannabis_seed_hybrid.png
│   │       │       │   │   ├── cannabis_seed_indica.png
│   │       │       │   │   ├── cannabis_seed_sativa.png
│   │       │       │   │   ├── cannabis_trim.png
│   │       │       │   │   ├── cannabis_trimm_station.png
│   │       │       │   │   ├── cannabis_trocknungsnetz.png
│   │       │       │   │   ├── cargo_module.png
│   │       │       │   │   ├── cash.png
│   │       │       │   │   ├── cash_block.png
│   │       │       │   │   ├── ceramic_pot.png
│   │       │       │   │   ├── chardonnay_grapes.png
│   │       │       │   │   ├── cheese_curd.png
│   │       │       │   │   ├── cheese_wedge.png
│   │       │       │   │   ├── cheese_wheel.png
│   │       │       │   │   ├── chemie_mixer.png
│   │       │       │   │   ├── chocolate_bar_100g.png
│   │       │       │   │   ├── chocolate_bar_200g.png
│   │       │       │   │   ├── chocolate_bar_500g.png
│   │       │       │   │   ├── coca_paste.png
│   │       │       │   │   ├── cocaine.png
│   │       │       │   │   ├── coffee_package_1kg.png
│   │       │       │   │   ├── coffee_package_250g.png
│   │       │       │   │   ├── coffee_package_500g.png
│   │       │       │   │   ├── crack_rock.png
│   │       │       │   │   ├── cured_cannabis_bud.png
│   │       │       │   │   ├── destillations_apparat.png
│   │       │       │   │   ├── diesel_bucket.png
│   │       │       │   │   ├── diesel_canister.png
│   │       │       │   │   ├── dried_azurescens.png
│   │       │       │   │   ├── dried_burley_leaf.png
│   │       │       │   │   ├── dried_cannabis_bud.png
│   │       │       │   │   ├── dried_cubensis.png
│   │       │       │   │   ├── dried_havana_leaf.png
│   │       │       │   │   ├── dried_mexicana.png
│   │       │       │   │   ├── dried_oriental_leaf.png
│   │       │       │   │   ├── dried_virginia_leaf.png
│   │       │       │   │   ├── ecstasy_pill.png
│   │       │       │   │   ├── emmental_wedge.png
│   │       │       │   │   ├── emmental_wheel.png
│   │       │       │   │   ├── empty_diesel_can.png
│   │       │       │   │   ├── ephedrin.png
│   │       │       │   │   ├── ergot_kultur.png
│   │       │       │   │   ├── espresso.png
│   │       │       │   │   ├── extraction_solvent.png
│   │       │       │   │   ├── fender_basic.png
│   │       │       │   │   ├── fender_chrome.png
│   │       │       │   │   ├── fender_sport.png
│   │       │       │   │   ├── fermentation_barrel.png
│   │       │       │   │   ├── fermentations_tank.png
│   │       │       │   │   ├── fermented_burley_leaf.png
│   │       │       │   │   ├── fermented_havana_leaf.png
│   │       │       │   │   ├── fermented_oriental_leaf.png
│   │       │       │   │   ├── fermented_virginia_leaf.png
│   │       │       │   │   ├── fertilizer_bottle.png
│   │       │       │   │   ├── fluid_module.png
│   │       │       │   │   ├── fresh_azurescens.png
│   │       │       │   │   ├── fresh_bolivianisch_coca_leaf.png
│   │       │       │   │   ├── fresh_burley_leaf.png
│   │       │       │   │   ├── fresh_cannabis_bud.png
│   │       │       │   │   ├── fresh_cubensis.png
│   │       │       │   │   ├── fresh_havana_leaf.png
│   │       │       │   │   ├── fresh_kolumbianisch_coca_leaf.png
│   │       │       │   │   ├── fresh_mexicana.png
│   │       │       │   │   ├── fresh_oriental_leaf.png
│   │       │       │   │   ├── fresh_peruanisch_coca_leaf.png
│   │       │       │   │   ├── fresh_virginia_leaf.png
│   │       │       │   │   ├── glass_of_wine.png
│   │       │       │   │   ├── golden_pot.png
│   │       │       │   │   ├── gouda_wedge.png
│   │       │       │   │   ├── gouda_wheel.png
│   │       │       │   │   ├── ground_coffee.png
│   │       │       │   │   ├── growth_booster_bottle.png
│   │       │       │   │   ├── havana_seeds.png
│   │       │       │   │   ├── heavyduty_tire.png
│   │       │       │   │   ├── herb_cheese.png
│   │       │       │   │   ├── heroin.png
│   │       │       │   │   ├── heroin_raffinerie.png
│   │       │       │   │   ├── honey_jar_1kg.png
│   │       │       │   │   ├── honey_jar_250g.png
│   │       │       │   │   ├── honey_jar_500g.png
│   │       │       │   │   ├── indisch_poppy_seeds.png
│   │       │       │   │   ├── iron_pot.png
│   │       │       │   │   ├── jod.png
│   │       │       │   │   ├── key.png
│   │       │       │   │   ├── klimalampe_large.png
│   │       │       │   │   ├── klimalampe_medium.png
│   │       │       │   │   ├── klimalampe_small.png
│   │       │       │   │   ├── kochstation.png
│   │       │       │   │   ├── kolumbianisch_coca_seeds.png
│   │       │       │   │   ├── kristallisator.png
│   │       │       │   │   ├── large_packaging_table.png
│   │       │       │   │   ├── license_sign.png
│   │       │       │   │   ├── license_sign_mount.png
│   │       │       │   │   ├── limousine.png
│   │       │       │   │   ├── limousine_chassis.png
│   │       │       │   │   ├── lsd_blotter.png
│   │       │       │   │   ├── lsd_loesung.png
│   │       │       │   │   ├── luxus_chassis.png
│   │       │       │   │   ├── lysergsaeure.png
│   │       │       │   │   ├── maintenance_kit.png
│   │       │       │   │   ├── mdma_base.png
│   │       │       │   │   ├── mdma_kristall.png
│   │       │       │   │   ├── medium_drying_rack.png
│   │       │       │   │   ├── medium_fermentation_barrel.png
│   │       │       │   │   ├── medium_packaging_table.png
│   │       │       │   │   ├── merlot_grapes.png
│   │       │       │   │   ├── meth.png
│   │       │       │   │   ├── meth_paste.png
│   │       │       │   │   ├── mikro_dosierer.png
│   │       │       │   │   ├── mist_bag_large.png
│   │       │       │   │   ├── mist_bag_medium.png
│   │       │       │   │   ├── mist_bag_small.png
│   │       │       │   │   ├── morphine.png
│   │       │       │   │   ├── mutterkorn.png
│   │       │       │   │   ├── normal_motor.png
│   │       │       │   │   ├── npc_leisure_tool.png
│   │       │       │   │   ├── npc_location_tool.png
│   │       │       │   │   ├── npc_patrol_tool.png
│   │       │       │   │   ├── npc_spawner_tool.png
│   │       │       │   │   ├── offroad_chassis.png
│   │       │       │   │   ├── offroad_tire.png
│   │       │       │   │   ├── opium_presse.png
│   │       │       │   │   ├── oriental_seeds.png
│   │       │       │   │   ├── packaging_bag.png
│   │       │       │   │   ├── packaging_box.png
│   │       │       │   │   ├── packaging_jar.png
│   │       │       │   │   ├── parmesan_wedge.png
│   │       │       │   │   ├── parmesan_wheel.png
│   │       │       │   │   ├── path_staff.png
│   │       │       │   │   ├── perforations_presse.png
│   │       │       │   │   ├── performance_2_motor.png
│   │       │       │   │   ├── performance_motor.png
│   │       │       │   │   ├── peruanisch_coca_seeds.png
│   │       │       │   │   ├── pillen_farbstoff.png
│   │       │       │   │   ├── pillen_presse.png
│   │       │       │   │   ├── plot_info_block.png
│   │       │       │   │   ├── plot_selection_tool.png
│   │       │       │   │   ├── pollen_press_mold.png
│   │       │       │   │   ├── poppy_pod.png
│   │       │       │   │   ├── premium_grow_light_slab.png
│   │       │       │   │   ├── premium_tire.png
│   │       │       │   │   ├── pseudoephedrin.png
│   │       │       │   │   ├── quality_booster_bottle.png
│   │       │       │   │   ├── quality_frame.png
│   │       │       │   │   ├── raw_opium.png
│   │       │       │   │   ├── reaktions_kessel.png
│   │       │       │   │   ├── reduktionskessel.png
│   │       │       │   │   ├── riesling_grapes.png
│   │       │       │   │   ├── ritzmaschine.png
│   │       │       │   │   ├── roasted_coffee_beans.png
│   │       │       │   │   ├── roh_meth.png
│   │       │       │   │   ├── roter_phosphor.png
│   │       │       │   │   ├── safrol.png
│   │       │       │   │   ├── scoring_knife.png
│   │       │       │   │   ├── sink.png
│   │       │       │   │   ├── small_drying_rack.png
│   │       │       │   │   ├── small_fermentation_barrel.png
│   │       │       │   │   ├── small_packaging_table.png
│   │       │       │   │   ├── smoked_cheese.png
│   │       │       │   │   ├── soil_bag_large.png
│   │       │       │   │   ├── soil_bag_medium.png
│   │       │       │   │   ├── soil_bag_small.png
│   │       │       │   │   ├── spaetburgunder_grapes.png
│   │       │       │   │   ├── spawn_tool.png
│   │       │       │   │   ├── spore_syringe_azurescens.png
│   │       │       │   │   ├── spore_syringe_cubensis.png
│   │       │       │   │   ├── spore_syringe_mexicana.png
│   │       │       │   │   ├── sport_tire.png
│   │       │       │   │   ├── sports_car.png
│   │       │       │   │   ├── standard_front_fender.png
│   │       │       │   │   ├── standard_tire.png
│   │       │       │   │   ├── starter_battery.png
│   │       │       │   │   ├── suv.png
│   │       │       │   │   ├── tank_15l.png
│   │       │       │   │   ├── tank_30l.png
│   │       │       │   │   ├── tank_50l.png
│   │       │       │   │   ├── terracotta_pot.png
│   │       │       │   │   ├── trimmed_cannabis_bud.png
│   │       │       │   │   ├── trocknungs_ofen.png
│   │       │       │   │   ├── truck.png
│   │       │       │   │   ├── truck_chassis.png
│   │       │       │   │   ├── tuerkisch_poppy_seeds.png
│   │       │       │   │   ├── vakuum_trockner.png
│   │       │       │   │   ├── van.png
│   │       │       │   │   ├── van_chassis.png
│   │       │       │   │   ├── virginia_seeds.png
│   │       │       │   │   ├── warehouse.png
│   │       │       │   │   ├── wassertank.png
│   │       │       │   │   ├── watering_can.png
│   │       │       │   │   ├── wine_bottle_1500ml.png
│   │       │       │   │   ├── wine_bottle_375ml.png
│   │       │       │   │   └── wine_bottle_750ml.png
│   │       │       │   └── parts
│   │       │       │       ├── fender_chrome.png
│   │       │       │       └── fender_sport.png
│   │       │       └── sounds.json
│   │       ├── data
│   │       │   └── schedulemc
│   │       │       ├── damage_type
│   │       │       │   └── hit_vehicle.json
│   │       │       ├── loot_tables
│   │       │       │   └── blocks
│   │       │       │       └── fuel_station.json
│   │       │       └── tags
│   │       │           └── fluids
│   │       │               └── fuel_station.json
│   │       ├── log4j2.xml
│   │       ├── pack.mcmeta
│   │       ├── schedulemc-server.toml
│   │       └── schedulemc.mixins.json
│   └── test
│       ├── java
│       │   └── de
│       │       └── rolandsw
│       │           └── schedulemc
│       │               ├── commands
│       │               │   └── CommandExecutorTest.java
│       │               ├── economy
│       │               │   ├── EconomyManagerTest.java
│       │               │   ├── LoanManagerTest.java
│       │               │   ├── MemoryCleanupManagerTest.java
│       │               │   ├── TransactionHistoryTest.java
│       │               │   └── WalletManagerTest.java
│       │               ├── integration
│       │               │   ├── EconomyIntegrationTest.java
│       │               │   ├── NPCIntegrationTest.java
│       │               │   └── ProductionChainIntegrationTest.java
│       │               ├── production
│       │               │   ├── nbt
│       │               │   │   └── PlantSerializerTest.java
│       │               │   ├── GenericProductionSystemTest.java
│       │               │   └── ProductionSizeTest.java
│       │               ├── region
│       │               │   ├── PlotManagerTest.java
│       │               │   └── PlotSpatialIndexTest.java
│       │               ├── test
│       │               │   └── MinecraftTestBootstrap.java
│       │               └── util
│       │                   ├── AbstractPersistenceManagerTest.java
│       │                   ├── EventHelperTest.java
│       │                   ├── InputValidationTest.java
│       │                   └── PacketHandlerTest.java
│       └── resources
│           └── mockito-extensions
│               └── org.mockito.plugins.MockMaker
├── wiki/
│   ├── features
│   │   ├── Achievement-System.md
│   │   ├── Economy-System.md
│   │   ├── Gang-System.md
│   │   ├── Level-System.md
│   │   ├── Lock-System.md
│   │   ├── MapView-System.md
│   │   ├── Market-System.md
│   │   ├── Messaging-System.md
│   │   ├── NPC-System.md
│   │   ├── Plot-System.md
│   │   ├── Police-Crime-System.md
│   │   ├── Smartphone-System.md
│   │   ├── Territory-System.md
│   │   ├── Towing-System.md
│   │   ├── Tutorial-System.md
│   │   ├── Vehicle-System.md
│   │   └── Warehouse-System.md
│   ├── production
│   │   ├── Beer-System.md
│   │   ├── Cannabis-System.md
│   │   ├── Cheese-System.md
│   │   ├── Chocolate-System.md
│   │   ├── Coca-System.md
│   │   ├── Coffee-System.md
│   │   ├── Honey-System.md
│   │   ├── LSD-System.md
│   │   ├── MDMA-System.md
│   │   ├── Meth-System.md
│   │   ├── Mushroom-System.md
│   │   ├── Poppy-System.md
│   │   ├── Tobacco-System.md
│   │   └── Wine-System.md
│   ├── Blocks.md
│   ├── Commands.md
│   ├── FAQ.md
│   ├── Getting-Started.md
│   ├── Home.md
│   ├── Items.md
│   └── Production-Systems.md
├── .gitignore
├── LICENSE
├── README.md
├── TEXTURES_LIST.md
├── build.gradle
├── gitignore
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
└── update.json
```

</details>

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
