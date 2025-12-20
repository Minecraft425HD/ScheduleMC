# ScheduleMC - Complete Roleplay & Economy Server System

<div align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green?style=for-the-badge)
![Forge](https://img.shields.io/badge/Forge-47.4.0-orange?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-blue?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-2.7.0--beta-red?style=for-the-badge)
![Lines of Code](https://img.shields.io/badge/Lines_of_Code-93,349-yellow?style=for-the-badge)
![Items](https://img.shields.io/badge/Items-141-brightgreen?style=for-the-badge)
![Blocks](https://img.shields.io/badge/Blocks-77+-purple?style=for-the-badge)
![Commands](https://img.shields.io/badge/Commands-161+-cyan?style=for-the-badge)

**The Most Comprehensive Minecraft Economy & Roleplay Mod**

[📚 Documentation](#-documentation) • [🎮 Features](#-features) • [📦 Installation](#-installation) • [🌐 Wiki](wiki/Home.md) • [🐛 Issues](https://github.com/Minecraft425HD/ScheduleMC/issues)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Statistics](#-key-statistics)
- [Features](#-features)
  - [Plot Management](#️-plot-management-system)
  - [Economy System](#-economy-system)
  - [NPC System](#-npc-system)
  - [Police & Crime](#-police--crime-system)
  - [Production Systems (8 Types)](#-production-systems-8-types)
  - [Vehicle System](#-vehicle-system)
  - [Smartphone System](#-smartphone-system)
  - [Warehouse System](#-warehouse-system)
  - [Tutorial System](#-tutorial-system)
  - [Dynamic Market](#-dynamic-market-system)
- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Commands Reference](#-commands-reference)
- [Items & Blocks](#-items--blocks)
- [API for Developers](#-api-for-developers)
- [Documentation](#-documentation)
- [Development](#️-development)
- [Testing & Quality](#-testing--quality-assurance)
- [Support](#-support)
- [License](#-license)
- [Credits](#-credits)

---

## 🎯 Overview

**ScheduleMC** is a **professional-grade Minecraft Forge mod** for version **1.20.1** that implements a complete roleplay/economy server ecosystem. With **93,349 lines of code**, **141 items**, **77+ blocks**, and **161+ commands**, it represents one of the most ambitious community-driven modding projects.

### What Makes ScheduleMC Unique?

✨ **Complete Economy System** - Full banking, loans, savings, investments, taxes
✨ **8 Production Chains** - Tobacco, Cannabis, Coca, Poppy, Meth, LSD, MDMA, Mushrooms
✨ **Advanced NPC AI** - Schedule-based AI, personalities, shops, relationships
✨ **GTA-Style Police System** - 5-star wanted level, chases, prison, bail
✨ **Vehicle System** - 139 files, drivable cars with fuel, parts, customization
✨ **Plot Management** - Apartments, rentals, ratings, spatial indexing
✨ **Professional Architecture** - Public API, 200+ unit tests, comprehensive docs

---

## 📈 Key Statistics

| Metric | Value | Description |
|--------|-------|-------------|
| **Lines of Code** | 93,349 | Total Java code lines |
| **Java Files** | 641 | Source files |
| **Items** | 141 | Registered items |
| **Blocks** | 77+ | Registered blocks |
| **Commands** | 161+ | Available commands |
| **Production Systems** | 8 | Drug production chains |
| **API Modules** | 12 | Public API subsystems |
| **Unit Tests** | 200+ | Test coverage |
| **Documentation** | 163 KB | 7 comprehensive docs |
| **Manager Classes** | 29 | Core system managers |
| **GUI Screens** | 32 | User interfaces |
| **BlockEntity Types** | 43 | Block entities |
| **Version** | 2.7.0-beta | Current release |

---

## 🚀 Features

### 🏘️ Plot Management System

A comprehensive land management system with advanced features:

#### Core Features
- **Visual Selection Tool** - Plot Wand for easy boundary selection
- **5 Plot Types** - Residential, Commercial, Shop, Public, Government
- **Spatial Indexing** - O(log n) performance for thousands of plots
- **Apartment System** - Sub-lease plots with deposits and rent
- **Rating System** - 5-star ratings with leaderboards
- **Plot Transfer** - Transfer ownership between players
- **50% Refund** - Abandon plots with partial refund

#### Protection Features
- **Block Protection** - Only owners and trusted players can build
- **Inventory Protection** - Chest and container protection
- **Plot Boundaries** - Clear visual boundaries
- **Trusted Players** - Grant build permissions to friends

#### Rental System
- **Daily Rent** - Charge daily rent for plots
- **Auto-Eviction** - Automatic eviction after rent expiration
- **Rent Extension** - Extend rental periods
- **Apartment Deposits** - Security deposits for apartments

#### Statistics & Management
- 📊 **161+ Commands** - Comprehensive command system
- 🏠 **Plot Info Block** - Display plot information
- ⭐ **Top Plots** - Leaderboard of highest-rated plots
- 💰 **Investment Tracking** - Track plot values and ROI

**Related Commands:** `/plot` (47 commands)
**Documentation:** [Plot System Wiki](wiki/features/Plot-System.md)

---

### 💰 Economy System

The most comprehensive economy system in Minecraft modding:

#### Banking System
- **Player Accounts** - Every player gets a bank account (default: 1,000€)
- **ATM Blocks** - Deposit/withdraw cash at ATMs
- **Physical Cash** - Cash items you can drop/trade
- **Wallet System** - Carry cash in your inventory
- **Transaction History** - Complete audit trail of all transactions

#### Advanced Financial Products
| Product | Description | Details |
|---------|-------------|---------|
| **Loans** | 3 tiers | Small (5k, 10%), Medium (25k, 15%), Large (100k, 20%) |
| **Savings Accounts** | 5% weekly interest | Min 1,000€, 4-week lock period |
| **Recurring Payments** | Auto-pay system | Max 10 per player, configurable intervals |
| **Overdraft Protection** | Credit line | Configurable fees and limits |
| **Shop Investments** | Buy shares | 1,000€/share, profit sharing |

#### Tax System
- **Property Tax** - Tax on plot ownership
- **Sales Tax** - Tax on transactions
- **Income Tax** - Tax on earnings
- **State Account** - Government treasury

#### Daily Rewards
- **Base Reward**: 50€/day
- **Streak Bonus**: +10€ per consecutive day (max 30 days)
- **Automatic Reset**: After missed day

#### Money Management (11 Manager Classes)
- `EconomyManager` - Core account management
- `WalletManager` - Cash item tracking
- `TransactionHistory` - Complete audit trail
- `InterestManager` - Interest calculations
- `LoanManager` - Loan tracking & collection
- `TaxManager` - Tax collection & enforcement
- `SavingsAccountManager` - Savings accounts
- `OverdraftManager` - Overdraft handling
- `RecurringPaymentManager` - Auto-pay system
- `ShopAccountManager` - Shop finances
- `StateAccount` - Government treasury

**Related Commands:** `/money`, `/pay`, `/loan`, `/savings`, `/autopay`, `/daily`, `/shopinvest`, `/state` (28 commands)
**Items:** Cash Item, Cash Block, ATM
**Documentation:** [Economy System Wiki](wiki/features/Economy-System.md)

---

### 🤖 NPC System

Advanced AI-driven NPCs with schedules, personalities, and shops:

#### NPC Types
| Type | Description | Features |
|------|-------------|----------|
| **Resident** | Regular citizens | Schedules, homes, leisure |
| **Merchant** | Shop owners | Inventory, prices, warehouse |
| **Police** | Law enforcement | Chase AI, arrest, backup |

#### Personality System
- **Friendly** - Better prices, easier negotiations
- **Neutral** - Standard interactions
- **Hostile** - Higher prices, aggressive
- **Professional** - Business-focused

#### Schedule System
NPCs follow realistic daily schedules:
```
07:00 - Work Start (go to work location)
12:00 - Lunch Break (go to leisure location)
18:00 - Work End (return home)
23:00 - Sleep (stay at home)
```

#### Advanced Features
- **Custom Player Skins** - NPCs can use real player skins
- **Shop Integration** - Merchants sell items from warehouse
- **Relationship System** - Build relationships affecting prices
- **Wallet System** - NPCs have money and can buy/sell
- **Inventory Management** - 9-slot NPC inventory
- **Pathfinding AI** - Smart navigation
- **Dialogue System** - Interactive conversations
- **Salary System** - NPCs earn daily salaries

#### NPC Tools
- **NPC Spawner Tool** - Spawn NPCs
- **Location Tool** - Set home/work locations
- **Leisure Tool** - Set leisure locations (max 10)
- **Patrol Tool** - Set police patrol routes

**Related Commands:** `/npc` (23 commands)
**Items:** 4 NPC tools
**Entities:** CustomNPCEntity with 139 behavior goals
**Documentation:** [NPC System Wiki](wiki/features/NPC-System.md)

---

### 🚔 Police & Crime System

GTA-inspired wanted level system with consequences:

#### Wanted Level System
| Stars | Description | Police Response |
|-------|-------------|-----------------|
| ⭐ | Minor offense | Single officer pursuit |
| ⭐⭐ | Moderate crime | 2 officers |
| ⭐⭐⭐ | Serious crime | 3 officers |
| ⭐⭐⭐⭐ | Major crime | 4 officers + backup |
| ⭐⭐⭐⭐⭐ | Most wanted | Full force deployment |

#### Crime Mechanics
- **Auto-Decay** - 1 star per day automatically removed
- **Hiding** - Hide in buildings to avoid police
- **Door Blocking** - Police block doors during chase
- **Backup System** - Police call for reinforcements
- **Raid Penalties** - Lose illegal cash when arrested

#### Prison System
- **Multiple Prisons** - Create prison plots
- **Cell System** - Individual cells with security levels
- **Bail Payment** - Pay bail at hospital to get released
- **Jail Time** - Serve time or pay to escape
- **Inmate Management** - Admin tools for managing prisoners

#### Police AI
- **Chase Behavior** - Police chase criminals
- **Arrest Mechanics** - Automatic arrest when caught
- **Pursuit Tactics** - Smart pathfinding
- **Backup Calling** - Radio for help

**Related Commands:** `/prison`, `/bail`, `/jailtime` (10 commands)
**Systems:** CrimeManager, PrisonManager, PoliceAIHandler
**Documentation:** [Police & Crime Wiki](wiki/features/Police-Crime-System.md)

---

### 🌿 Production Systems (8 Types)

Ultra-detailed production chains for 8 different drugs:

#### 1️⃣ Tobacco System (Most Complex)

**4 Strains:** Virginia, Burley, Oriental, Havana

**Complete Production Chain (6 Steps):**

```
┌─────────────┐
│ 1. PLANTING │ - 4 Pot Types (Terracotta, Ceramic, Iron, Golden)
└──────┬──────┘ - 4 Tobacco Strains
       │        - Soil bags (Small/Medium/Large)
       │        - Watering can
       ▼
┌─────────────┐
│ 2. GROWING  │ - 8 Growth Stages
└──────┬──────┘ - Fertilizer, Growth Booster, Quality Booster
       │        - 3 Grow Light Tiers (Basic/Advanced/Premium)
       │        - Quality System (Poor → Legendary)
       ▼
┌─────────────┐
│ 3. DRYING   │ - 3 Drying Rack Sizes (Small/Medium/Big)
└──────┬──────┘ - Time-based processing
       │        - Quality preservation
       ▼
┌─────────────┐
│4.FERMENTATION│ - 3 Barrel Sizes (Small/Medium/Big)
└──────┬──────┘ - Enhanced flavor/quality
       │        - Duration affects quality
       ▼
┌─────────────┐
│ 5. PACKAGING│ - 3 Table Sizes (Multi-block 2x2)
└──────┬──────┘ - 4 Package Sizes (Small/Medium/Large/XL)
       │        - Packaging materials required
       ▼
┌─────────────┐
│ 6. SELLING  │ - NPC Negotiation System
└─────────────┘ - Dynamic prices based on quality
                - Market supply & demand
```

**Items:** 32 tobacco items
**Blocks:** 23 tobacco blocks
**Quality Levels:** Poor, Good, Very Good, Legendary
**Documentation:** [Tobacco System Wiki](wiki/production/Tobacco-System.md)

---

#### 2️⃣ Cannabis System

**4 Strains:** Indica, Sativa, Hybrid, Autoflower

**Production Chain (8 Steps):**

1. **Planting** - Seeds → Pots
2. **Growing** - 8 growth stages
3. **Harvesting** - Fresh buds
4. **Drying** - Trocknungsnetz (drying net)
5. **Trimming** - Trimm Station
6. **Curing** - Curing Glas (jars)
7. **Hash Production** - Hash Presse
8. **Oil Extraction** - Öl Extraktor

**Items:** 10 cannabis items
**Blocks:** 9 cannabis blocks
**By-Products:** Trim, Hash, Oil
**Documentation:** [Cannabis System Wiki](wiki/production/Cannabis-System.md)

---

#### 3️⃣ Coca/Cocaine System

**2 Strains:** Bolivianisch, Kolumbianisch

**Production Chain (5 Steps):**

1. **Planting** - Coca seeds → Pots
2. **Harvesting** - Fresh coca leaves
3. **Extraction** - 3 Vat Sizes → Coca paste (requires diesel)
4. **Refining** - 3 Refinery Sizes → Cocaine (glowing effect!)
5. **Crack Cooking** - Crack Kocher → Crack rocks (requires baking soda)

**Items:** 9 coca items
**Blocks:** 9 coca blocks (with light effects)
**Chemical:** Diesel canister, Backpulver
**Documentation:** [Coca System Wiki](wiki/production/Coca-System.md)

---

#### 4️⃣ Poppy/Opium System

**3 Strains:** Afghanisch, Türkisch, Indisch

**Production Chain (6 Steps):**

1. **Planting** - Poppy seeds → Pots
2. **Scoring** - Ritzmaschine (scoring machine)
3. **Collecting** - Raw opium
4. **Pressing** - Opium Presse
5. **Cooking** - Kochstation → Morphine
6. **Refining** - Heroin Raffinerie → Heroin

**Items:** 8 poppy items
**Blocks:** 7 poppy blocks
**Tools:** Scoring knife
**Documentation:** [Poppy System Wiki](wiki/production/Poppy-System.md)

---

#### 5️⃣ Methamphetamine System

**Production Chain (4 Steps - DANGEROUS!):**

1. **Mixing** - Chemie Mixer (Ephedrin/Pseudoephedrin + chemicals) → Meth paste
2. **Reduction** - Reduktionskessel (⚠️ EXPLOSION RISK!) → Raw meth
3. **Crystallization** - Kristallisator → Crystal meth
4. **Drying** - Vakuum Trockner → Final product

**Items:** 8 meth items
**Blocks:** 4 meth blocks
**Chemicals:** Ephedrin, Pseudoephedrin, Roter Phosphor, Jod
**Danger:** Reduktionskessel can explode!
**Documentation:** [Meth System Wiki](wiki/production/Meth-System.md)

---

#### 6️⃣ LSD System (Most Scientific)

**Precision Laboratory Production (6 Steps):**

1. **Fermentation** - Fermentations Tank (Mutterkorn culture) → Ergot culture
2. **Distillation** - Destillations Apparat (glowing!) → Lysergsäure
3. **Chemistry** - Chemical synthesis → LSD Lösung
4. **Micro-Dosing** - Mikro Dosierer (GUI!) → Apply to blotter paper
5. **Perforation** - Perforations Presse → Cut into tabs
6. **Final Product** - LSD Blotter sheets

**Items:** 6 LSD items
**Blocks:** 4 LSD blocks (with GUI)
**Process:** Very detailed chemical simulation
**Documentation:** [LSD System Wiki](wiki/production/LSD-System.md)

---

#### 7️⃣ MDMA/Ecstasy System

**Arcade-Style Production (4 Steps):**

1. **Safrol Extraction** - Safrol base
2. **Reaction** - Reaktions Kessel (glowing!) → MDMA Base
3. **Drying** - Trocknungs Ofen (hot!) → MDMA Kristall
4. **Pill Pressing** - Pillen Presse (⭐ TIMING MINIGAME!) → Ecstasy pills

**Items:** 6 MDMA items
**Blocks:** 3 MDMA blocks
**Minigame:** Timing-based pill press!
**Materials:** Bindemittel (binder), Farbstoff (dye)
**Documentation:** [MDMA System Wiki](wiki/production/MDMA-System.md)

---

#### 8️⃣ Psilocybin Mushroom System

**3 Strains:** Cubensis, Azurescens, Mexicana

**Production Chain (4 Steps):**

1. **Inoculation** - Spore syringes → Mist bags (3 sizes)
2. **Growing** - Climate lamps (3 tiers)
3. **Harvesting** - Fresh mushrooms
4. **Drying** - Final product

**Items:** 15 mushroom items
**Blocks:** 4 mushroom blocks
**Environment:** Requires water tank + climate lamps
**Documentation:** [Mushroom System Wiki](wiki/production/Mushroom-System.md)

---

**Universal Packaging System:** All drugs use the `packaged_drug` item with NBT data for type and quality.

**Total Production Items:** 102 items
**Total Production Blocks:** 60+ blocks
**Documentation:** [Production Overview](wiki/Production-Systems.md)

---

### 🚗 Vehicle System

Complete vehicle simulation with 139 Java files:

#### Vehicle Features
- **Drivable Vehicles** - Full vehicle control
- **Fuel System** - Diesel and gasoline
- **Fuel Stations** - Refuel at stations (multi-block)
- **Garage Blocks** - Vehicle storage and repair
- **License Plates** - Customizable plates
- **Vehicle Damage** - Durability system
- **OBJ Model Support** - Custom 3D models via CoreLib

#### Vehicle Types (5)
| Type | Chassis | Description |
|------|---------|-------------|
| **Limousine** | LIMOUSINE_CHASSIS | Standard car |
| **Van** | VAN_CHASSIS | Cargo transport |
| **Truck** | TRUCK_CHASSIS | Heavy cargo |
| **SUV** | OFFROAD_CHASSIS | Off-road vehicle |
| **Sports Car** | LUXUS_CHASSIS | High-performance |

#### Vehicle Parts (Modular System)

**Engines (3 Types):**
- Normal Motor
- Performance Motor
- Industrial Motor

**Tires (6 Types):**
- Standard, Sport, Premium (Cars)
- Offroad, Allterrain, Heavy Duty (Trucks)

**Chassis (5 Types):**
- Limousine, Van, Truck, Offroad, Luxus

**Other Parts:**
- Fenders (Basic, Chrome, Sport)
- Modules (Cargo, Fluid, License Plate Holder)
- Fuel Tanks (15L, 30L, 50L)

**Tools & Consumables:**
- Diesel Can (Empty/Full)
- Maintenance Kit
- Key, Battery
- Spawn Tool

**Total Vehicle Items:** 40 items
**Vehicle Blocks:** 4 blocks
**Java Files:** 139 files
**Documentation:** [Vehicle System Wiki](wiki/features/Vehicle-System.md)

---

### 📱 Smartphone System

In-game smartphone with 11 functional apps:

#### Apps Overview
| # | App Name | Icon | Description |
|---|----------|------|-------------|
| 1 | **MAP** | §9Blue | Plot locations, markers, navigation |
| 2 | **DEALER** | §cRed | Find tobacco dealers, compare prices |
| 3 | **PRODUCTS** | §aGreen | Shop catalog browser |
| 4 | **ORDER** | §eYellow | Order management (planned) |
| 5 | **CONTACTS** | §5Purple | Player & NPC contacts |
| 6 | **MESSAGES** | §3Cyan | Inbox and chat |
| 7 | **PLOT** | §6Gold | Plot management |
| 8 | **SETTINGS** | §7Gray | Smartphone settings |
| 9 | **BANK** | §2Dark Green | Banking operations |
| 10 | **CRIME STATS** | §4Dark Red | Wanted level, crime history |
| 11 | **CHAT** | §bCyan | Direct messaging |

#### PvP Protection
When using smartphone:
- **Immunity** - No damage while smartphone is open
- **Attacker Penalty** - Attackers get +1 wanted star ⭐
- **Fair Play** - Prevents unfair PvP situations

#### Features
- **Default Keybind:** P (configurable)
- **Custom GUI** - Professional smartphone interface
- **Network Sync** - Client-server synchronization
- **App Framework** - Extensible app system

**Related Commands:** Integrated with all systems
**GUI Screens:** 11 app screens
**Documentation:** [Smartphone System Wiki](wiki/features/Smartphone-System.md)

---

### 📦 Warehouse System

Mass storage system for NPC merchants:

#### Features
- **32 Inventory Slots** - Each holds 1,024 items
- **Automatic Deliveries** - Every 3 days (configurable)
- **Shop Linking** - Connect to shop plots
- **NPC Integration** - NPCs sell from warehouse
- **State Payment** - Government pays delivery costs
- **Revenue Tracking** - Income/expense tracking

#### Usage Flow
```
1. Admin creates Warehouse
2. Link to Shop Plot: /warehouse setshop <shopId>
3. Link to NPC Merchant: /npc <name> warehouse set
4. Add items: /warehouse add <item> <amount>
5. NPCs automatically sell warehouse items
6. Auto-delivery refills every 3 days
```

**Storage Capacity:** 32,768 items total (32 slots × 1,024)
**Related Commands:** `/warehouse` (7 commands)
**Block:** Warehouse Block
**Documentation:** [Warehouse System Wiki](wiki/features/Warehouse-System.md)

---

### 🎓 Tutorial System

7-step interactive onboarding system:

#### Tutorial Steps
1. **Welcome** - Introduction to ScheduleMC
2. **Economy Basics** - Bank accounts, money
3. **Plot System** - Create and manage plots
4. **NPCs** - Interact with NPCs
5. **Production** - Start production
6. **Trading** - Sell products
7. **Completion** - Rewards and next steps

#### Features
- **Progress Tracking** - Save progress
- **Skip Options** - Skip individual steps
- **Rewards** - Completion rewards
- **Tutorial API** - Extensible framework

**Related Commands:** `/tutorial` (7 commands - not registered)
**Documentation:** [Tutorial System Wiki](wiki/features/Tutorial-System.md)

---

### 📊 Dynamic Market System

Supply & demand based economy:

#### Features
- **Dynamic Prices** - Prices change based on trading
- **Supply & Demand** - Realistic economics
- **Price History** - Track price trends
- **Market Data** - Comprehensive statistics
- **Trend Analysis** - Top risers/fallers

#### Price Factors
- Player trading volume
- Item rarity
- Time-based decay
- Server-wide supply

**Related Commands:** `/market` (4 commands - not registered)
**Documentation:** [Market System Wiki](wiki/features/Market-System.md)

---

## 📦 Installation

### Prerequisites

| Requirement | Version | Download |
|-------------|---------|----------|
| **Minecraft** | 1.20.1 | [minecraft.net](https://minecraft.net) |
| **Minecraft Forge** | 47.4.0+ | [files.minecraftforge.net](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) |
| **Java** | 17 | [adoptium.net](https://adoptium.net/) |
| **RAM** | 4 GB min (8 GB recommended) | - |

### Installation Steps

#### 1️⃣ Install Forge

```bash
# Download Forge installer
wget https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.0/forge-1.20.1-47.4.0-installer.jar

# Run installer
java -jar forge-1.20.1-47.4.0-installer.jar
```

#### 2️⃣ Download ScheduleMC

Download latest release from [GitHub Releases](https://github.com/Minecraft425HD/ScheduleMC/releases)

Or build from source:
```bash
git clone https://github.com/Minecraft425HD/ScheduleMC.git
cd ScheduleMC
./gradlew build
```

#### 3️⃣ Install Mod

```
.minecraft/
└── mods/
    └── ScheduleMC-2.7.0-beta.jar
```

#### 4️⃣ Optional Dependencies

For enhanced features, install:
- **JEI** (Just Enough Items) - Recipe viewing
- **Jade** - Block info tooltips
- **The One Probe** - Advanced block info

#### 5️⃣ Launch Minecraft

Select **Forge 1.20.1** profile and start!

---

## 🎯 Quick Start

### For Players

#### First Join
```
You receive automatically:
✓ Bank account with 1,000€
✓ Welcome message
✓ Tutorial prompt
```

#### Day 1 Tutorial

**1. Get your daily reward**
```
/daily
→ Receive 50€ (+ streak bonus)
```

**2. Create your first plot**
```
/plot wand
→ Right-click two corners to select area

/plot create residential "My Home" 50000
→ Creates residential plot for 50,000€ sale price
```

**3. Explore the economy**
```
/money
→ Check your balance

/pay Steve 100
→ Send money to friends

/loan info
→ See available loans
```

**4. Start production**
```
/tobacco give virginia_seeds
→ Get tobacco seeds (admin command for testing)

# Plant in pot, water, fertilize, grow!
```

### For Admins

#### Server Setup

**1. Create spawn areas**
```
/plot wand
/plot create public "Spawn"
/plot create government "Town Hall"
```

**2. Spawn NPCs**
```
/npc spawn merchant Shop_Owner_Hans
/npc Hans schedule workstart 0700
/npc Hans schedule workend 1800
/npc Hans setshop baumarkt
```

**3. Setup economy**
```
/money give @a 5000
→ Give all players starting money

/state deposit 1000000
→ Fund government account
```

**4. Create prison**
```
/plot wand
/plot create government "Prison"
/prison create prison_main
/prison addcell 1 100,10,100 105,15,105 1
```

**5. Setup hospital**
```
/hospital setspawn
/hospital setfee 500
```

---

## 📋 Commands Reference

### Quick Reference Table

| Category | Command Prefix | Count | Example |
|----------|---------------|-------|---------|
| Plot Management | `/plot` | 47 | `/plot create residential "Home" 50000` |
| Economy | `/money`, `/pay` | 28 | `/money`, `/pay Steve 1000` |
| Loans | `/loan` | 3 | `/loan apply MEDIUM` |
| Savings | `/savings` | 6 | `/savings create 5000` |
| Autopay | `/autopay` | 5 | `/autopay add Steve 100 7 "Rent"` |
| Daily Rewards | `/daily` | 2 | `/daily`, `/daily streak` |
| Shop Investment | `/shopinvest` | 5 | `/shopinvest buy shop_1 25` |
| State | `/state` | 3 | `/state balance` |
| Hospital | `/hospital` | 3 | `/hospital setspawn` |
| Warehouse | `/warehouse` | 7 | `/warehouse add diamond 64` |
| Tobacco | `/tobacco` | 3 | `/tobacco info` |
| NPCs | `/npc` | 23 | `/npc Hans info` |
| Utilities | `/utility` | 7 | `/utility`, `/strom`, `/wasser` |
| Prison | `/prison`, `/bail` | 10 | `/bail`, `/jailtime` |
| Tutorial | `/tutorial` | 7 | `/tutorial start` (not active) |
| Market | `/market` | 4 | `/market prices` (not active) |
| Health | `/health` | 5 | `/health` |

**Total:** 161+ commands

For complete command documentation, see:
- [Command Reference Wiki](wiki/Commands.md)
- [Benutzer-Anleitung (German)](docs/BENUTZER_ANLEITUNG.md)

---

## 🎮 Items & Blocks

### Items Summary

| Category | Count | Examples |
|----------|-------|----------|
| **Economy** | 3 | Cash, Cash Block, Plot Tool |
| **NPCs** | 4 | Spawner, Location Tool, Leisure Tool, Patrol Tool |
| **Tobacco** | 32 | Seeds (4), Leaves (12), Tools (7), Packaging (9) |
| **Cannabis** | 10 | Seeds, Buds (4 stages), Hash, Oil |
| **Coca** | 9 | Seeds (2), Leaves (2), Paste, Cocaine, Crack |
| **Poppy** | 8 | Seeds (3), Pods, Opium, Morphine, Heroin |
| **Meth** | 8 | Chemicals (4), Paste, Raw, Crystal, Final |
| **LSD** | 6 | Mutterkorn, Ergot, Acid, Solution, Blotter |
| **MDMA** | 6 | Safrol, Base, Crystal, Pills, Materials |
| **Mushroom** | 15 | Syringes (3), Mist Bags (3), Mushrooms (9) |
| **Vehicle** | 40 | Parts (20), Tools (10), Vehicles (5), Fuel (5) |
| **TOTAL** | **141** | |

### Blocks Summary

| Category | Count | Key Blocks |
|----------|-------|------------|
| **Tobacco** | 23 | Pots (4), Plants (4), Racks (3), Barrels (3), Tables (3), Lights (3), Sink |
| **Cannabis** | 9 | Plants (4), Trocknungsnetz, Trimm Station, Curing Glas, Hash Presse, Öl Extraktor |
| **Coca** | 9 | Plants (2), Vats (3), Refineries (3), Crack Kocher |
| **Poppy** | 7 | Plants (3), Ritzmaschine, Presse, Kochstation, Raffinerie |
| **Meth** | 4 | Mixer, Reduktionskessel, Kristallisator, Vakuum Trockner |
| **LSD** | 4 | Fermentation, Destillation, Mikro Dosierer, Perforations Presse |
| **MDMA** | 3 | Reaktions Kessel, Trocknungs Ofen, Pillen Presse |
| **Mushroom** | 4 | Klimalampe (3 sizes), Wassertank |
| **Economy** | 2 | ATM, Cash Block |
| **Plot** | 1 | Plot Info Block |
| **Warehouse** | 1 | Warehouse |
| **Vehicle** | 4 | Fuel Station, Garage, Diesel Fluid |
| **TOTAL** | **77+** | |

For complete item/block documentation:
- [Items Wiki](wiki/Items.md)
- [Blocks Wiki](wiki/Blocks.md)

---

## 👨‍💻 API for Developers

ScheduleMC provides a comprehensive public API for mod developers:

### API Modules (12)

```java
// Get API instance
ScheduleMCAPI api = ScheduleMCAPI.getInstance();

// 1. Economy API
IEconomyAPI economy = api.getEconomyAPI();
economy.deposit(playerUUID, 1000.0);
double balance = economy.getBalance(playerUUID);

// 2. Plot API
IPlotAPI plots = api.getPlotAPI();
Optional<PlotRegion> plot = plots.getPlotAt(blockPos);

// 3. Production API
IProductionAPI production = api.getProductionAPI();
production.registerCustomPlant(...);

// 4. NPC API
INPCAPI npcs = api.getNPCAPI();
npcs.spawnNPC(location, type, name);

// 5. Police API
IPoliceAPI police = api.getPoliceAPI();
police.setWantedLevel(playerUUID, 3);

// 6. Warehouse API
IWarehouseAPI warehouse = api.getWarehouseAPI();
warehouse.addItem(warehouseId, itemStack, amount);

// 7. Messaging API
IMessagingAPI messaging = api.getMessagingAPI();
messaging.sendMessage(from, to, message);

// 8. Smartphone API
ISmartphoneAPI smartphone = api.getSmartphoneAPI();
smartphone.registerApp(customApp);

// 9. Vehicle API
IVehicleAPI vehicles = api.getVehicleAPI();
vehicles.spawnVehicle(location, vehicleType);

// 10. Achievement API
IAchievementAPI achievements = api.getAchievementAPI();
achievements.grantAchievement(playerUUID, achievementId);

// 11. Tutorial API
ITutorialAPI tutorial = api.getTutorialAPI();
tutorial.startTutorial(playerUUID);

// 12. Market API
IMarketAPI market = api.getMarketAPI();
double price = market.getPrice(itemId);
```

### API Version
**Current:** 3.0.0
**Status:** ✅ Stable & Production-Ready

For complete API documentation:
- [API Documentation](docs/API_DOKUMENTATION.md)
- [Developer Documentation](docs/ENTWICKLER_DOKUMENTATION.md)

---

## 📚 Documentation

### Available Documentation (163 KB)

| Document | Size | Description |
|----------|------|-------------|
| **[User Guide (DE)](docs/BENUTZER_ANLEITUNG.md)** | 45 KB | Complete user manual (German) |
| **[Developer Docs (DE)](docs/ENTWICKLER_DOKUMENTATION.md)** | 40 KB | Architecture & development |
| **[API Docs (DE)](docs/API_DOKUMENTATION.md)** | 21 KB | API reference for developers |
| **[Configuration (DE)](docs/KONFIGURATION.md)** | 19 KB | All config options |
| **[Production System (DE)](docs/GENERIC_PRODUCTION_SYSTEM.md)** | 20 KB | Production framework |
| **[Unit Tests (DE)](docs/UNIT_TESTS.md)** | 8 KB | Testing guide |
| **[Improvements Summary (DE)](docs/IMPROVEMENTS_SUMMARY.md)** | 10 KB | Changelog |

### Wiki Documentation

- **[Wiki Home](wiki/Home.md)** - Wiki homepage
- **[Getting Started](wiki/Getting-Started.md)** - Beginner's guide
- **[Features](wiki/Features.md)** - All features overview
- **[Items](wiki/Items.md)** - Complete item list
- **[Blocks](wiki/Blocks.md)** - Complete block list
- **[Commands](wiki/Commands.md)** - All commands
- **[Production](wiki/Production-Systems.md)** - Production guide
- **[API](wiki/API.md)** - API documentation
- **[FAQ](wiki/FAQ.md)** - Frequently asked questions

---

## 🛠️ Development

### Build from Source

```bash
# Clone repository
git clone https://github.com/Minecraft425HD/ScheduleMC.git
cd ScheduleMC

# Build mod
./gradlew build

# Run client (for testing)
./gradlew runClient

# Run server
./gradlew runServer

# Run tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport
```

### Project Structure

```
ScheduleMC/
├── src/main/java/de/rolandsw/schedulemc/
│   ├── ScheduleMC.java              # Main mod class
│   ├── api/                         # Public API (12 modules)
│   ├── commands/                    # Command implementations
│   ├── economy/                     # Economy system (11 managers)
│   ├── region/                      # Plot system
│   ├── npc/                         # NPC system (139 behavior files)
│   ├── tobacco/                     # Tobacco production
│   ├── cannabis/                    # Cannabis production
│   ├── coca/                        # Coca production
│   ├── poppy/                       # Poppy production
│   ├── meth/                        # Meth production
│   ├── lsd/                         # LSD production
│   ├── mdma/                        # MDMA production
│   ├── mushroom/                    # Mushroom production
│   ├── vehicle/                     # Vehicle system (139 files)
│   ├── warehouse/                   # Warehouse system
│   ├── messaging/                   # Messaging system
│   ├── client/                      # Client-side (Smartphone)
│   ├── util/                        # Utilities
│   ├── managers/                    # Core managers
│   ├── config/                      # Configuration
│   └── ...
├── src/main/resources/
│   ├── META-INF/mods.toml          # Mod metadata
│   ├── assets/schedulemc/
│   │   ├── lang/                   # Translations (DE, EN)
│   │   ├── models/                 # Item/block models
│   │   ├── textures/               # Textures
│   │   └── blockstates/            # Block states
│   └── data/schedulemc/
│       ├── loot_tables/            # Loot tables
│       ├── recipes/                # Crafting recipes
│       └── tags/                   # Item/block tags
├── src/test/java/                  # Unit tests (12 test classes)
├── docs/                           # Documentation (163 KB)
├── wiki/                           # Wiki pages
├── build.gradle                    # Build configuration
└── gradle.properties               # Project properties
```

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Minecraft** | 1.20.1 | Game platform |
| **Forge** | 47.4.0 | Mod loader |
| **Java** | 17 | Programming language |
| **Gson** | 2.10.1 | JSON serialization |
| **CoreLib** | 1.20.1-1.1.1 | Vehicle system (networking, OBJ models) |
| **JUnit Jupiter** | 5.10.1 | Unit testing |
| **Mockito** | 5.8.0 | Mocking framework |
| **AssertJ** | 3.24.2 | Fluent assertions |
| **JaCoCo** | 0.8.11 | Code coverage |

### Design Patterns Used

| Pattern | Usage | Example |
|---------|-------|---------|
| **Singleton** | API, Managers | `ScheduleMCAPI.getInstance()` |
| **Template Method** | Persistence | `AbstractPersistenceManager` |
| **Strategy** | Production | Production systems |
| **Observer** | Events | Forge event bus |
| **Factory** | NPCs, Items | Entity/item creation |
| **Registry** | Items/Blocks | Deferred Register |
| **Command** | Commands | Command pattern |
| **Facade** | API | Simplified interfaces |

---

## ✅ Testing & Quality Assurance

### Test Coverage

```
Total Tests: 200+
Coverage: 60% minimum (80% for utilities)

Test Files:
✓ EconomyManagerTest.java
✓ PlotSpatialIndexTest.java
✓ PlantSerializerTest.java
✓ EventHelperTest.java
✓ AbstractPersistenceManagerTest.java
✓ PacketHandlerTest.java
✓ CommandExecutorTest.java
✓ GenericProductionSystemTest.java
✓ EconomyIntegrationTest.java
✓ NPCIntegrationTest.java
✓ ProductionChainIntegrationTest.java
✓ MinecraftTestBootstrap.java
```

### Quality Metrics

| Metric | Value | Status |
|--------|-------|--------|
| **Lines of Code** | 93,349 | ✅ Well-structured |
| **TODO/FIXME** | 34 | ✅ Very low |
| **Code Coverage** | 60%+ | ✅ Good |
| **Documentation** | 163 KB | ✅ Comprehensive |
| **Unit Tests** | 200+ | ✅ Extensive |

### Quality Features

✅ **Backup System** - Automatic backups before saves
✅ **Corruption Recovery** - Auto-restore from backups
✅ **Graceful Degradation** - System runs even with errors
✅ **Health Checks** - `/health` command for diagnostics
✅ **Error Isolation** - EventHelper prevents crashes
✅ **Atomic Writes** - No corrupted save files
✅ **Input Validation** - All commands validate input

---

## 🐛 Support

### Bug Reports

Found a bug? [Create an issue](https://github.com/Minecraft425HD/ScheduleMC/issues)

**Please include:**
- Minecraft version
- Forge version
- ScheduleMC version
- Steps to reproduce
- Log files (`.minecraft/logs/latest.log`)

### Common Issues

<details>
<summary><b>Mod doesn't load</b></summary>

**Solution:**
- Check Forge version (47.4.0+)
- Check Java version (17)
- Verify mod is in `mods/` folder
- Check logs for errors
</details>

<details>
<summary><b>Plots not saving</b></summary>

**Solution:**
- Check `config/schedulemc/plots.json` exists
- Verify write permissions
- Check disk space
- Use `/health plot` for diagnostics
</details>

<details>
<summary><b>NPCs not spawning</b></summary>

**Solution:**
- Use `/npc spawn <type> <name>`
- Check server logs
- Verify plot permissions
- Use `/health economy` to check NPC system
</details>

<details>
<summary><b>Economy not working</b></summary>

**Solution:**
- Check `config/schedulemc/economy.json`
- Verify player has account (`/money`)
- Check transaction history (`/money history`)
- Use `/health economy` for diagnostics
</details>

### Discord Community

Join our Discord for:
- Technical support
- Feature requests
- Community showcase
- Development updates

[Join Discord](https://discord.gg/schedulemc) (placeholder)

---

## 📄 License

This project is licensed under the **All Rights Reserved** license.

See [LICENSE](LICENSE) file for details.

---

## 🙏 Credits

### Development Team

**Lead Developer:** Luckas R. Schneider (Minecraft425HD)

### Special Thanks

- **Minecraft Forge Team** - For the incredible modding platform
- **CoreLib** by MaxHenkel - Vehicle system foundation
- **Community Contributors** - Bug reports and feedback
- **Beta Testers** - Testing and quality assurance

### Libraries & Dependencies

- **Gson** - JSON serialization
- **JUnit Jupiter** - Unit testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **JaCoCo** - Code coverage analysis

---

## 🔗 Links

- 🌐 [GitHub Repository](https://github.com/Minecraft425HD/ScheduleMC)
- 📖 [Wiki Documentation](wiki/Home.md)
- 🐛 [Issue Tracker](https://github.com/Minecraft425HD/ScheduleMC/issues)
- 💬 [Discord Community](https://discord.gg/schedulemc)
- 📦 [CurseForge](https://www.curseforge.com/minecraft/mc-mods/schedulemc) (coming soon)
- 📦 [Modrinth](https://modrinth.com/mod/schedulemc) (coming soon)

---

<div align="center">

**Made with ❤️ for the Minecraft Community**

ScheduleMC v2.7.0-beta | Minecraft 1.20.1 | Forge 47.4.0

[⬆ Back to Top](#schedulemc---complete-roleplay--economy-server-system)

</div>
