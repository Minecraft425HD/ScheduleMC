# NPC System

<div align="center">

**Advanced AI with Schedules, Personalities & Relationships**

173 files powering intelligent NPCs that bring your server to life

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [NPC Types](#npc-types)
3. [Behavior Goals](#behavior-goals)
4. [Personality System](#personality-system)
5. [Schedule System](#schedule-system)
6. [Custom Player Skins](#custom-player-skins)
7. [Shop & Warehouse Integration](#shop--warehouse-integration)
8. [Relationship System](#relationship-system)
9. [Wallet System](#wallet-system)
10. [Dialogue System](#dialogue-system)
11. [Quest System](#quest-system)
12. [Social Relationships](#social-relationships)
13. [Witness System](#witness-system)
14. [Companion System](#companion-system)
15. [Dynamic Pricing System](#dynamic-pricing-system)
16. [Banker-NPC System](#banker-npc-system)
15. [AI & Pathfinding](#ai--pathfinding)
16. [NPC Tools](#npc-tools)
17. [Commands](#commands)
18. [Developer API](#developer-api)
19. [Best Practices](#best-practices)
20. [Troubleshooting](#troubleshooting)

---

## Overview

The NPC System is the largest subsystem in ScheduleMC, spanning **173 source files** with **9 AI behavior goals**, **5 behavior actions**, and **14 behavior states** driving intelligent, schedule-driven NPCs. NPCs have personalities, form relationships, run shops, patrol streets, and create a living, breathing economy on your server.

### Key Features

- **3 NPC Types** - Resident, Merchant, Police
- **9 AI Goals + 5 Behavior Actions** - Complex AI decision-making via NPCBehaviorEngine
- **4 Personality Types** - Friendly, Neutral, Hostile, Professional
- **Schedule System** - HHMM format time-based routines
- **Custom Player Skins** - NPCs use real Minecraft player skins
- **Shop + Warehouse Integration** - Unlimited stock from warehouse inventory
- **Relationship System** - Affects prices and NPC interactions (-100 to +100)
- **Wallet System** - NPCs carry and spend money
- **Dialogue System** - Branching conversations with NPCs
- **Quest System** - NPCs assign and track player quests
- **Social Relationships** - NPCs form bonds with each other
- **Witness System** - NPCs observe and report crimes
- **Companion System** - NPCs follow and assist players
- **Pathfinding AI** - Obstacle avoidance, door opening, stair navigation
- **23 Commands** - Full admin control via `/npc`

---

## NPC Types

### 1. BEWOHNER (Resident)

**Purpose:** Roleplay NPCs that populate the city

**Features:**
- 9-slot inventory system
- Personal wallet for cash
- No work schedule (leisure and sleep only)
- Can buy/sell at shops
- Daily income system
- Personality-based buying behavior

**Use Cases:**
```
- Town citizens
- Wandering NPCs
- Background population
- Customers at shops
- Social interactions
```

**Example Setup:**
```bash
# Place NPC entity in-world, then configure:
/npc Hans_Mueller schedule home 2300  # Sleep at 11 PM
/npc Hans_Mueller leisure add         # Add leisure spots (up to 10)
/npc Hans_Mueller wallet set 5000     # Give starting money
```

---

### 2. VERKAEUFER (Merchant)

**Purpose:** Shop owners and vendors

**Features:**
- 9-slot inventory system
- Personal wallet
- Full work schedule (start, end, home)
- Shop inventory (buy/sell)
- 7 merchant categories
- Warehouse integration for unlimited stock
- Personality price modifiers
- Relationship-based discounts and markups

**Merchant Categories:**
1. **BAUMARKT** (Hardware Store) - Building materials
2. **WAFFENHAENDLER** (Gun Shop) - Weapons, tools
3. **TANKSTELLE** (Gas Station) - Fuel, vehicle items
4. **LEBENSMITTEL** (Grocery) - Food, consumables
5. **PERSONALMANAGEMENT** (HR) - Services
6. **ILLEGALER_HAENDLER** (Black Market) - Illegal goods
7. **AUTOHAENDLER** (Vehicle Dealer) - Cars, parts

**Example Setup:**
```bash
# Place merchant NPC entity in-world, then configure:
/npc Shop_Owner_Klaus schedule workstart 0700
/npc Shop_Owner_Klaus schedule workend 1800
/npc Shop_Owner_Klaus schedule home 2300
/npc Shop_Owner_Klaus wallet set 50000
/npc Shop_Owner_Klaus setshop Electronics_Store

# Configure shop inventory (SHIFT + Left-click NPC)
```

---

### 3. POLIZEI (Police)

**Purpose:** Law enforcement NPCs

**Features:**
- No inventory or wallet
- Patrol system (up to 16 points)
- Police station location
- Wait times at patrol points (default: 3 min)
- Wander radius (default: 3 blocks)
- Crime detection and response
- Backup calling system
- Door-blocking abilities during pursuit

**Example Setup:**
```bash
# Place police NPC entity in-world
# Police automatically patrol and respond to crime
/npc Officer_Mueller info        # Check configuration
/npc Officer_Mueller movement true  # Ensure movement is enabled
```

---

## Behavior Goals

The NPC system contains **9 AI behavior goals** and **5 behavior actions** that drive all NPC decision-making. These goals are prioritized and selected by the behavior engine based on NPC type, schedule, personality, and current context.

### Goal Categories

**Movement Goals:**
- `MoveToHomeGoal` - Navigate to home during sleep time
- `MoveToWorkGoal` - Navigate to workplace during work hours
- `MoveToLeisureGoal` - Visit leisure locations during free time
- `PolicePatrolGoal` - Follow patrol routes (police only)
- `PoliceStationGoal` - Return to police station

**Social Goals:**
- Dialogue initiation and response
- Relationship building with players
- Companion following and assistance
- Witness observation and crime reporting

**Economy Goals:**
- Shop management and item selling
- Purchase decision-making (based on buying personality)
- Warehouse restocking

**Combat/Police Goals:**
- Criminal pursuit and arrest
- Backup calling to nearby officers
- Door blocking during chases
- Search behavior after losing line of sight

### Behavior Engine

The `NPCBehaviorEngine` evaluates all registered goals each tick and selects the highest-priority applicable goal:

```
Priority System:
1. Emergency (combat, arrest, flee)
2. Schedule (work, home, sleep)
3. Social (dialogue, companion)
4. Economy (shopping, selling)
5. Idle (wander, leisure)
```

---

## Personality System

### Personality Types

NPCs are assigned one of **4 personality types** that affect their behavior, pricing, and interactions:

| Personality | Price Modifier | Behavior | Police Call Chance |
|-------------|---------------|----------|-------------------|
| **Friendly** | 0.80x (20% discount) | Welcoming, generous | 30% |
| **Neutral** | 1.00x (standard) | Standard behavior | 70% |
| **Hostile** | 1.30x (30% markup) | Aggressive, uncooperative | 95% |
| **Professional** | 1.10x (10% markup) | Business-focused, efficient | 80% |

### Personality Effects

#### 1. Price Modifiers

```
Item: Diamond (Base Price: 100)

Friendly NPC:      100 x 0.80 = 80     (20% discount)
Neutral NPC:       100 x 1.00 = 100    (standard)
Professional NPC:  100 x 1.10 = 110    (10% markup)
Hostile NPC:       100 x 1.30 = 130    (30% markup)
```

#### 2. Free Item Chance

```
Friendly:      5% chance to give item for free
Neutral:       2% chance
Professional:  0% (never gives free items)
Hostile:       0% (never gives free items)
```

#### 3. Police Reporting

When a player commits a crime near an NPC, the NPC's personality determines how likely they are to call the police:

```
Hostile:       95% chance to call police
Professional:  80% chance
Neutral:       70% chance
Friendly:      30% chance
```

### Buying Personality (Residents)

Resident NPCs also have a buying personality that controls their purchasing behavior:

| Type | Mood Weight | Demand Weight | Max Budget | Purchase Threshold |
|------|-------------|---------------|------------|-------------------|
| **SPARSAM** (Cautious) | 40% | 20% | 30% of wallet | 50+ score |
| **AUSGEWOGEN** (Balanced) | 30% | 30% | 50% of wallet | 40+ score |
| **IMPULSIV** (Impulsive) | 20% | 40% | 70% of wallet | 30+ score |

---

## Schedule System

### Time Format

All schedule times use **HHMM format** (24-hour, 4 digits):

| Time | HHMM | Description |
|------|------|-------------|
| 6:00 AM | `0600` | Default wake time |
| 7:00 AM | `0700` | Typical work start |
| 12:00 PM | `1200` | Lunch time |
| 6:00 PM | `1800` | Typical work end / home |
| 11:00 PM | `2300` | Typical sleep time |

**Minecraft Time Conversion:**
```
Real World    -> Minecraft Ticks
6:00 AM       = 0 ticks
12:00 PM      = 6000 ticks
6:00 PM       = 12000 ticks
12:00 AM      = 18000 ticks
6:00 AM       = 24000 ticks (next day)

Formula: ticks = (totalMinutes - 360) x (1000 / 60)
```

### Schedule by NPC Type

#### Residents (BEWOHNER)

Residents have only 2 schedule phases: **leisure** and **home/sleep**.

```bash
/npc Hans schedule home 2300  # Sleep at 11 PM to 6 AM
# Leisure: 6 AM to 11 PM (all remaining time)
```

**Behavior:**
```
6:00 AM  - 11:00 PM: Leisure time
  -> Wanders between leisure locations (max 10)
  -> Visits shops, socializes

11:00 PM - 6:00 AM: Home time
  -> Goes to home location
  -> Stays there (sleeping)
```

#### Merchants (VERKAEUFER)

Merchants have 3 schedule phases: **work**, **leisure**, and **home/sleep**.

```bash
/npc Shop_Owner schedule workstart 0700  # Work at 7 AM
/npc Shop_Owner schedule workend 1800    # Leave work at 6 PM
/npc Shop_Owner schedule home 2300       # Sleep at 11 PM
```

**Behavior:**
```
7:00 AM  - 6:00 PM: Work time
  -> Goes to work location (shop)
  -> Sells/buys from players

6:00 PM  - 11:00 PM: Leisure time
  -> Leaves shop, visits leisure locations

11:00 PM - 7:00 AM: Home time
  -> Goes home, stays there (sleeping)
```

#### Police (POLIZEI)

Police NPCs use a **patrol system** instead of a schedule. They are always active and never sleep.

### Default Times (If Not Configured)

```
Work Start: 6:00 AM  (0600)
Work End:   7:00 PM  (1900)
Home Time:  5:00 AM  (0500)
```

---

## Custom Player Skins

NPCs in ScheduleMC use **real Minecraft player skins**. The `CustomSkinManager` fetches and caches player skin textures so that each NPC can appear as a unique character.

**How it works:**
1. Admin assigns a Minecraft player name to the NPC
2. The system fetches that player's skin from Mojang servers
3. The skin is cached locally for performance
4. The NPC renders with the player model and skin in-game

This allows server admins to create visually distinct NPCs by referencing any valid Minecraft account skin.

---

## Shop & Warehouse Integration

### Shop System

Merchant NPCs can be assigned to shops with two separate inventories:

1. **Buy Shop** - Items the NPC sells to players
2. **Sell Shop** - Items the NPC buys from players

**Configuration:** SHIFT + Left-click on the NPC (admin only) to open the shop editor GUI.

```
Buy Shop (NPC sells to players):
- Diamond x 64 @ 100 each (unlimited)
- Gold Ingot x 128 @ 80 each (limited)

Sell Shop (NPC buys from players):
- Virginia Cigar @ 45 each (unlimited)
- Cannabis Bud @ 150 each (unlimited)
```

### Warehouse Integration

NPCs can sell items directly from a linked warehouse, providing effectively unlimited stock:

```bash
# 1. Link NPC to warehouse (look at warehouse block)
/npc Shop_Owner warehouse set

# 2. Check linkage
/npc Shop_Owner warehouse info

# 3. Clear linkage if needed
/npc Shop_Owner warehouse clear
```

**How it works:**
```
Player buys Diamond from NPC:
1. Check NPC inventory first (9 slots)
2. If not enough, check linked warehouse
3. If warehouse has stock, sell from warehouse
4. Revenue goes to shop account
5. Stock is unlimited as long as warehouse has items
```

---

## Relationship System

### Relationship Levels

**Range:** -100 to +100

| Level | Tier | Price Modifier | Color |
|-------|------|----------------|-------|
| -100 to -50 | **HOSTILE** | +50% markup | Red |
| -49 to -10 | **UNFRIENDLY** | +25% markup | Orange |
| -9 to +9 | **NEUTRAL** | Normal price | Gray |
| +10 to +49 | **FRIENDLY** | -10% discount | Green |
| +50 to +100 | **VERY FRIENDLY** | -20% discount | Dark Green |

### Gaining Relationship

| Action | Points | Notes |
|--------|--------|-------|
| Purchase (any) | +2 | Base gain |
| Purchase >= 1,000 | +4 | Large purchase |
| Purchase >= 5,000 | +7 | Very large purchase |
| Sell (any) | +1 | Base gain |
| Sell >= 1,000 | +2 | Large sale |
| Help NPC | +10 | Quest/assistance |

### Losing Relationship

| Action | Points | Notes |
|--------|--------|-------|
| Theft attempt | -20 | Serious |
| Caught stealing | -30 | Very serious |
| Attack NPC | -50 | Extremely serious |

### Combined Price Calculation

```
Final Price = Base Price x Personality Modifier x Relationship Modifier

Best Case (Friendly NPC, +100 relationship):
  100 x 0.80 x 0.80 = 64   (36% total discount)

Worst Case (Hostile NPC, -100 relationship):
  100 x 1.30 x 1.50 = 195  (95% total markup)

Standard (Neutral NPC, 0 relationship):
  100 x 1.00 x 1.00 = 100  (no change)
```

---

## Wallet System

NPCs carry their own money, used for transactions and daily expenses.

**Currency:** Cash (Bargeld)
**Type:** Integer (0 to 2,147,483,647)

### Wallet Commands

```bash
# View wallet balance
/npc <name> wallet

# Set exact amount
/npc <name> wallet set <amount>

# Add money
/npc <name> wallet add <amount>

# Remove money (if sufficient)
/npc <name> wallet remove <amount>
```

**Daily Income:** NPCs receive automatic daily income, tracked per NPC. This ensures they always have funds to participate in the economy.

---

## Dialogue System

The dialogue system provides **branching conversations** between players and NPCs, managed by `DialogueManager` and a tree of `DialogueNode` objects.

### Components

| Class | Purpose |
|-------|---------|
| `DialogueTree` | Root structure holding all conversation branches |
| `DialogueNode` | A single dialogue step with text and options |
| `DialogueOption` | A player choice that leads to another node |
| `DialogueCondition` | Conditions that must be met (relationship, quest state, etc.) |
| `DialogueAction` | Side effects triggered by dialogue (give item, start quest, etc.) |
| `DialogueContext` | Current state of the conversation |
| `DialogueManager` | Manages active conversations across all players |
| `NPCDialogueProvider` | Generates dialogue trees based on NPC type/personality |

### How It Works

```
Player right-clicks NPC:
1. DialogueManager creates a DialogueContext
2. NPCDialogueProvider selects a DialogueTree based on NPC data
3. Player sees DialogueNode text with DialogueOption choices
4. DialogueConditions filter which options are available
5. Player selects an option
6. DialogueActions execute (give item, change relationship, etc.)
7. Conversation advances to the next DialogueNode
```

---

## Quest System

NPCs can assign quests to players through the dialogue system. Quests have objectives, rewards, and state tracking managed by the NPC life system.

**Quest Flow:**
```
1. Player talks to NPC
2. NPC offers quest through dialogue
3. Player accepts
4. Objectives tracked (deliver items, visit locations, etc.)
5. Player returns to NPC
6. Rewards given (money, items, relationship points)
```

---

## Social Relationships

NPCs form relationships with **each other**, not just with players. The social relationship system tracks bonds between NPCs and influences their behavior:

- NPCs visit friends during leisure time
- NPCs share information about crimes they witnessed
- Social bonds affect mood and emotional state
- NPCs remember positive and negative interactions via `NPCMemory`

---

## Witness System

NPCs observe their surroundings and can **witness crimes** committed by players. When an NPC witnesses a crime:

1. The `IllegalActivityScanner` detects the crime
2. Nearby NPCs are checked for line of sight
3. Based on personality, the NPC may call the police
4. The crime is recorded in the NPC's memory
5. Relationship with the criminal decreases

The witness system connects directly to the Police & Crime System through the `PoliceAIHandler` and `PoliceBackupSystem`.

---

## Companion System

Players can recruit NPCs as **companions** who follow and assist them.

### Components

| Class | Purpose |
|-------|---------|
| `CompanionManager` | Manages all active companion relationships |
| `CompanionData` | Stores companion state and preferences |
| `CompanionBehavior` | AI behavior goal for following the player |
| `CompanionType` | Enum of companion roles |
| `CompanionEventHandler` | Handles companion-related events |

### Companion Behavior

```
Companion follows player:
1. Pathfinds to player position
2. Maintains comfortable following distance
3. Assists in combat or tasks based on CompanionType
4. Returns to home location when dismissed
```

---

## AI & Pathfinding

### Navigation System

The NPC pathfinding system provides intelligent navigation with obstacle avoidance:

- Opens and closes doors automatically
- Navigates stairs and slopes
- Configurable walkable blocks
- Water navigation support
- Max step height: 1.5 blocks

**Navigation Attributes:**
```
Max Health:     20.0 HP
Movement Speed: 0.3 (default, 0.1-1.0 configurable)
Follow Range:   32.0 blocks
Step Height:    1.5 blocks
```

### Movement Speed

```bash
/npc <name> speed <0.1-1.0>
```

| Speed | Description | Use Case |
|-------|-------------|----------|
| 0.1-0.2 | Very slow | Stationary shop owners |
| 0.3 | Default walk | Normal citizens |
| 0.4-0.5 | Brisk walk | Busy merchants |
| 0.6-0.7 | Jogging | Police on patrol |
| 0.8-1.0 | Running | Police chasing criminals |

### Leisure Movement

- **Max Locations:** 10 per NPC
- **Wander Radius:** 15 blocks from location
- **Location Change:** Every 5 minutes
- **Wander Interval:** Every 10 seconds

### Police Patrol

- **Max Points:** 16 patrol points
- **Wait Time:** 3 minutes per point (configurable)
- **Wander Radius:** 3 blocks (configurable)
- **Loop:** Continuous patrol from point 0 to last, then repeat

---

## NPC Tools

ScheduleMC provides **4 specialized items** for NPC management:

### 1. NPC Spawner Tool (`NPCSpawnerTool`)

Opens the NPC Spawner GUI screen (`NPCSpawnerScreen`) to create new NPCs with type, name, and initial configuration.

### 2. NPC Location Tool (`NPCLocationTool`)

Sets home and work locations for NPCs by right-clicking on blocks in the world.

### 3. NPC Leisure Tool (`NPCLeisureTool`)

Adds leisure locations to an NPC's schedule. Each NPC supports **up to 10** leisure locations.

```bash
# Add current position as leisure spot
/npc <name> leisure add

# Remove leisure location by index (0-9)
/npc <name> leisure remove <index>

# List all leisure locations
/npc <name> leisure list

# Clear all leisure locations
/npc <name> leisure clear
```

### 4. NPC Patrol Tool (`NPCPatrolTool`)

Sets patrol points for police NPCs. Supports up to 16 patrol points per officer.

---

## Commands

The `/npc` command provides **23 subcommands** for full NPC management:

### Placement

NPCs are placed as entities in-world via spawn eggs or the creative menu. There is no `/npc spawn` command. NPCs are removed by killing the entity or using standard Minecraft entity removal.

### Information

```bash
/npc <name> info                  # View NPC details (type, schedule, wallet, etc.)
```

### Schedule

```bash
/npc <name> schedule workstart <HHMM>   # Set work start time
/npc <name> schedule workend <HHMM>     # Set work end time
/npc <name> schedule home <HHMM>        # Set home/sleep time
```

### Movement

```bash
/npc <name> movement <true/false>       # Enable/disable movement
/npc <name> speed <0.1-1.0>            # Set movement speed
```

### Wallet

```bash
/npc <name> wallet                      # Check balance
/npc <name> wallet set <amount>         # Set balance
/npc <name> wallet add <amount>         # Add money
/npc <name> wallet remove <amount>      # Remove money
```

### Inventory

```bash
/npc <name> inventory                   # View inventory
/npc <name> inventory give <0-8> <item> # Give item to slot
/npc <name> inventory clear             # Clear all slots
/npc <name> inventory clear <0-8>       # Clear specific slot
```

### Leisure

```bash
/npc <name> leisure add                 # Add leisure location
/npc <name> leisure remove <index>      # Remove leisure location
/npc <name> leisure list                # List leisure locations
/npc <name> leisure clear               # Clear all leisure locations
```

### Shop and Warehouse

```bash
/npc <name> setshop <shopId>            # Assign NPC to shop
/npc <name> warehouse set               # Link NPC to warehouse (look at block)
/npc <name> warehouse info              # Check warehouse link
/npc <name> warehouse clear             # Unlink NPC from warehouse
```

---

## Developer API

### INPCAPI Interface

External mods can access the NPC system through the `INPCAPI` interface.

**Access:**
```java
INPCAPI npcAPI = ScheduleMCAPI.getNPCAPI();
```

### Core Methods (v3.0.0+)

| Method | Description |
|--------|-------------|
| `getNPCByUUID(UUID, ServerLevel)` | Find NPC by UUID in a specific level (O(1) lookup) |
| `getNPCByUUID(UUID)` | Find NPC by UUID across all loaded worlds |
| `getAllNPCs(ServerLevel)` | Get all NPCs in a level |
| `getAllNPCs()` | Get all NPCs across all worlds |
| `getNPCCount(ServerLevel)` | Count NPCs in a level |
| `getTotalNPCCount()` | Count all NPCs globally |
| `getNPCData(CustomNPCEntity)` | Get NPCData object with all configuration |
| `setNPCHome(CustomNPCEntity, BlockPos)` | Set home position |
| `setNPCWork(CustomNPCEntity, BlockPos)` | Set work position |
| `setNPCType(CustomNPCEntity, NPCType)` | Change NPC type |

### Extended Methods (v3.2.0+)

| Method | Description |
|--------|-------------|
| `getNPCsByType(NPCType)` | Get all NPCs of a specific type |
| `getNPCsInRadius(ServerLevel, BlockPos, double)` | Get NPCs within radius |
| `setNPCName(CustomNPCEntity, String)` | Set display name |
| `addNPCLeisureLocation(CustomNPCEntity, BlockPos)` | Add leisure location |
| `removeNPC(CustomNPCEntity)` | Remove NPC from world |
| `setNPCSchedule(CustomNPCEntity, String, int)` | Set schedule (activity + HHMM time) |
| `getNPCBalance(CustomNPCEntity)` | Get wallet balance |
| `setNPCBalance(CustomNPCEntity, double)` | Set wallet balance |

### Example Usage

```java
INPCAPI npcAPI = ScheduleMCAPI.getNPCAPI();

// Find an NPC
CustomNPCEntity npc = npcAPI.getNPCByUUID(uuid, level);

// Get all merchants
Collection<CustomNPCEntity> merchants = npcAPI.getNPCsByType(NPCType.VERKAEUFER);

// Find NPCs near a position
Collection<CustomNPCEntity> nearby = npcAPI.getNPCsInRadius(level, pos, 50.0);

// Configure an NPC
npcAPI.setNPCName(npc, "Klaus_The_Merchant");
npcAPI.setNPCSchedule(npc, "workstart", 700);
npcAPI.setNPCSchedule(npc, "workend", 1800);
npcAPI.setNPCSchedule(npc, "sleep", 2300);
npcAPI.setNPCBalance(npc, 50000.0);

// Add leisure locations
npcAPI.addNPCLeisureLocation(npc, new BlockPos(100, 64, 200));
npcAPI.addNPCLeisureLocation(npc, new BlockPos(120, 64, 180));
```

**Thread Safety:** All methods are thread-safe through ConcurrentHashMap-based registry.
**Performance:** NPC lookups use O(1) UUID indexing for fast search.

---

## Best Practices

### For Admins

#### 1. Balanced NPC Distribution

```
Small Town:
- 5-10 BEWOHNER (residents)
- 2-3 VERKAEUFER (shops)
- 1-2 POLIZEI (police)

Large City:
- 20-30 BEWOHNER
- 8-12 VERKAEUFER
- 3-5 POLIZEI
```

#### 2. Realistic Schedules

```
Coffee Shop:  workstart 0600, workend 1200 (early morning)
Restaurant:   workstart 1100, workend 2200 (lunch and dinner)
Bar:          workstart 1800, workend 0200 (evening/night)
Hardware:     workstart 0800, workend 1800 (standard hours)
```

#### 3. Wallet Funding

```
Small Shop:     10,000 - 25,000
Medium Shop:    25,000 - 75,000
Large Shop:     75,000 - 200,000
Warehouse Shop: 200,000+
```

### For Players

#### 1. Build Relationships

```
1. Find shops with Friendly personality
2. Make small purchases regularly (+2 per purchase)
3. Build to +50 for -20% discount
4. Save thousands long-term
```

#### 2. Shop Around

```
Diamond prices comparison:
  Shop A (Hostile, -50 relationship):   195
  Shop B (Neutral, 0 relationship):     100
  Shop C (Friendly, +50 relationship):   64

Savings per diamond: 131
Savings on 100 diamonds: 13,100
```

---

## Troubleshooting

### NPC Not Moving

**Causes:**
1. Movement disabled
2. No schedule set
3. No home/work/leisure locations
4. NPC is in sleep time

**Solutions:**
```bash
/npc <name> movement true
/npc <name> schedule workstart 0700
/npc <name> schedule workend 1800
/npc <name> schedule home 2300
/npc <name> leisure add
```

### NPC Will Not Sell Items

**Causes:**
1. Shop not configured
2. Item not in shop inventory
3. NPC not at work (outside work hours)
4. Warehouse not linked

**Solutions:**
```bash
/npc <name> info                 # Check shop assignment
/npc <name> warehouse set        # Link warehouse if needed
# SHIFT + Left-click NPC to configure shop inventory
```

### Prices Too High/Low

**Causes:**
1. Personality trait (Hostile = +30%)
2. Bad relationship (-50 = +25%)
3. Combined effects

**Solutions:**
```
For admins:
- Personality is set on spawn (random)
- Delete and respawn NPC for different personality

For players:
- Improve relationship by trading
- Find different NPC with better personality
- Build to +50 relationship for -20% discount
```

---

## Dynamic Pricing System

NPC-Preise werden durch ein globales Marktsystem beeinflusst, das sich dynamisch veraendert.

### Marktbedingungen

| Bedingung | Multiplikator | Beschreibung |
|-----------|-------------|-------------|
| NORMAL | 1,0x | Standardpreise |
| BOOM | 1,3x | Hohe Nachfrage |
| REZESSION | 0,8x | Niedrige Nachfrage |
| KNAPPHEIT | 1,8x | Extreme Knappheit |
| UEBERFLUSS | 0,6x | Ueberangebot |
| KRISE | 2,0x | Marktzusammenbruch |
| INFLATION | 1,4x | Allgemeiner Preisanstieg |
| DEFLATION | 0,7x | Allgemeiner Preisverfall |

- **Update-Zyklus**: Einmal pro Ingame-Tag (24.000 Ticks) mit 30% Aenderungswahrscheinlichkeit
- **Preisformel**: `Basispreis x Marktbedingung x temporaere Modifikatoren x Zufallsvariation (+-5%)`
- **Preisverlauf**: Die letzten 30 Ingame-Tage werden gespeichert

### Verhandlungssystem

Spieler koennen mit NPCs ueber Preise verhandeln:

- **Max. Runden**: 5 pro Verhandlung
- **Max. Rabatt**: 30% insgesamt erreichbar
- **Rabatt pro Runde**: 2-8% bei Erfolg

#### Verhandlungstaktiken

| Taktik | Erfolgschance | Rabatt-Multiplikator |
|--------|-------------|---------------------|
| NORMAL | +0% | 1,0x |
| SCHMEICHELEI | +10% | 0,8x (weniger Rabatt, hoehere Chance) |
| AGGRESSIV | -15% | 1,3x (riskant, groesserer Rabatt) |
| LOGISCH | +5% | 1,0x (ausgewogen) |
| MITLEID | 0% | 0,9x (wirkt bei netten NPCs) |

**Persoenlichkeitsfaktoren**: Gier reduziert den Verhandlungserfolg, Ehrlichkeit erhoeht die Schwierigkeit. Aggressives Verhandeln kostet 2 Geduld statt 1.

### Handels-Events

| Transaktionstyp | Wirkung |
|----------------|---------|
| Fairer Kauf (>=80% Basispreis) | Neutral |
| Fairer Verkauf (<=120% Basispreis) | Neutral |
| Grosszuegiges Ueberzahlen (+10%) | "Grosszuegig"-Tag, HAPPY Emotion, Geruechte |
| Hochwertiger Handel (>=1.000 EUR) | HAPPY Emotion |
| Illegaler Kauf | Im Gedaechtnis gespeichert, Geruechte verbreitet |
| Diebstahlversuch | Negative Tags, Sicherheitsbeduerfnis sinkt |

---

## Banker-NPC System

Der Banker-NPC bietet ein vollstaendiges Banking-Interface mit 6 Tabs:

### Banking-Tabs

| Tab | Funktion |
|-----|---------|
| **Uebersicht** | Bargeld, Girokonto, Sparkonto, Gesamtguthaben |
| **Girokonto** | Ein-/Auszahlung, Einzahlungslimit, Ueberziehungssystem |
| **Sparkonto** | Ein-/Auszahlung, 10% Strafzins bei vorzeitiger Abhebung |
| **Ueberweisung** | Geld an andere Spieler senden (Tageslimit) |
| **Historie** | Scrollbare Liste der letzten Transaktionen (8 sichtbar) |
| **Dauerauftraege** | Wiederkehrende Zahlungen (taeglich/woechentlich/monatlich/jaehrlich) |

### Ueberziehungs-Konsequenzen

| Tag | Konsequenz |
|-----|-----------|
| 1-6 | Automatischer Rueckzahlungs-Countdown |
| 7-27 | Gefaengnis-Warnung |
| 28+ | Spieler wird ins Gefaengnis geschickt |

---

<div align="center">

**NPC System - Complete Guide**

For related systems:
- [Plot System](Plot-System.md)
- [Economy System](Economy-System.md)
- [Warehouse System](Warehouse-System.md)
- [Police & Crime System](Police-Crime-System.md)
- [Gang System](Gang-System.md)

[Back to Wiki Home](../Home.md) | [All Commands](../Commands.md)

**Last Updated:** 2026-02-07 | **ScheduleMC v3.6.0-beta**

</div>
