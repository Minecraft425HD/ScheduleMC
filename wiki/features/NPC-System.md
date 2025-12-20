# NPC System

<div align="center">

**Advanced AI with Schedules, Personalities & Relationships**

Intelligent NPCs that bring your server to life

[🏠 Back to Wiki Home](../Home.md) • [📋 Commands Reference](../Commands.md)

</div>

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [NPC Types](#npc-types)
3. [AI & Pathfinding](#ai--pathfinding)
4. [Schedule System](#schedule-system)
5. [Inventory & Wallet](#inventory--wallet)
6. [Shop System](#shop-system)
7. [Personality System](#personality-system)
8. [Relationship System](#relationship-system)
9. [Movement Behaviors](#movement-behaviors)
10. [NPC Management](#npc-management)
11. [Best Practices](#best-practices)
12. [Troubleshooting](#troubleshooting)

---

## Overview

The NPC System provides intelligent, schedule-driven NPCs with personalities, relationships, and dynamic behavior that creates a living, breathing economy.

### Key Features

✅ **3 NPC Types** - Resident, Merchant, Police
✅ **AI Pathfinding** - Smart navigation with door opening
✅ **Dynamic Schedules** - Work, home, leisure times
✅ **Personality Traits** - 5 traits affecting prices/behavior
✅ **Relationship System** - -100 to +100 levels with price modifiers
✅ **Shop Integration** - Buy/sell with warehouse support
✅ **9-Slot Inventory** - Item management
✅ **Wallet System** - Cash transactions
✅ **Leisure Activities** - Up to 10 locations per NPC
✅ **Police Patrols** - Up to 16 patrol points

---

## NPC Types

### 1. BEWOHNER (Resident)

**Purpose:** Roleplay NPCs that populate the city

**Features:**
- ✅ 9-slot inventory system
- ✅ Personal wallet for cash
- ❌ No work schedule (they don't work!)
- ✅ Only leisure and sleep times
- ✅ Can buy/sell at shops
- ✅ Daily income system
- ✅ Personality-based buying

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
/npc spawn bewohner Hans_Mueller
/npc Hans_Mueller schedule home 2300  # Sleep at 11 PM
/npc Hans_Mueller leisure add         # Add leisure spots (up to 10)
/npc Hans_Mueller wallet set 5000     # Give starting money
```

---

### 2. VERKAEUFER (Merchant)

**Purpose:** Shop owners and vendors

**Features:**
- ✅ 9-slot inventory system
- ✅ Personal wallet
- ✅ Full work schedule (start, end, home)
- ✅ Shop inventory (buy/sell)
- ✅ 7 merchant categories
- ✅ Warehouse integration
- ✅ Personality price modifiers
- ✅ Relationship system

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
/npc spawn verkaeufer Shop_Owner_Klaus
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
- ❌ No inventory or wallet
- ✅ Patrol system (up to 16 points)
- ✅ Police station location
- ✅ Wait times at patrol points (default: 3 min)
- ✅ Wander radius (default: 3 blocks)
- ✅ Crime detection
- ✅ Backup system
- ✅ Door-breaking abilities

**Example Setup:**
```bash
/npc spawn polizei Officer_Mueller
# Police automatically patrol and respond to crime
# Configure patrol points at police station
```

---

## AI & Pathfinding

### Navigation System

**Custom Pathfinding:**
- ✅ Opens and closes doors automatically
- ✅ Navigates stairs and slopes
- ✅ Configurable walkable blocks
- ✅ Water navigation support
- ✅ Max step height: 1.5 blocks

**Supported Blocks:**
```
Walkable:
- All stairs (automatically detected)
- Doors (opens/closes automatically)
- Trapdoors
- Fences/Gates (can navigate over)
- Custom blocks (configurable)
```

**Navigation Attributes:**
```
Max Health: 20.0 HP
Movement Speed: 0.3 (default, 0.1-1.0 configurable)
Follow Range: 32.0 blocks
Step Height: 1.5 blocks
```

---

### Movement Speed

**Configuration:**
```bash
/npc <name> speed <0.1-1.0>
```

**Speed Guide:**

| Speed | Description | Use Case |
|-------|-------------|----------|
| 0.1-0.2 | Very slow | Stationary shop owners |
| 0.3 | Default walk | Normal citizens |
| 0.4-0.5 | Brisk walk | Busy merchants |
| 0.6-0.7 | Jogging | Police on patrol |
| 0.8-1.0 | Running | Police chasing criminals |

**Examples:**
```bash
/npc Shop_Owner speed 0.2    # Mostly stationary
/npc Town_Citizen speed 0.3  # Normal walking
/npc Officer_1 speed 0.7     # Patrol speed
```

---

## Schedule System

### Time Format

**Format:** HHMM (24-hour, 4 digits)

**Examples:**
- `0600` = 6:00 AM
- `0730` = 7:30 AM
- `1200` = 12:00 PM (noon)
- `1830` = 6:30 PM
- `2300` = 11:00 PM

**Minecraft Time Conversion:**
```
Real World → Minecraft Ticks
6:00 AM = 0 ticks
12:00 PM = 6000 ticks
6:00 PM = 12000 ticks
12:00 AM = 18000 ticks
6:00 AM = 24000 ticks (next day)

Formula:
ticks = (totalMinutes - 360) × (1000 / 60)
```

---

### Schedule Configuration

#### For BEWOHNER (Residents)

**Has Only 2 Times:**
- ❌ No work time (they don't work!)
- ✅ Home time (sleep)
- ✅ Leisure time (all other time)

**Setup:**
```bash
/npc Hans schedule home 2300  # Sleep at 11 PM to 6 AM
# Leisure: 6 AM to 11 PM (all other time)
```

**Behavior:**
```
6:00 AM - 11:00 PM: Leisure time
  → Wanders between leisure locations
  → Visits shops
  → Socializes

11:00 PM - 6:00 AM: Home time
  → Goes to home location
  → Stays there (sleeping)
```

---

#### For VERKAEUFER (Merchants)

**Has 3 Times:**
- ✅ Work start
- ✅ Work end
- ✅ Home time (sleep)

**Setup:**
```bash
/npc Shop_Owner schedule workstart 0700  # 7 AM
/npc Shop_Owner schedule workend 1800    # 6 PM
/npc Shop_Owner schedule home 2300       # 11 PM
```

**Behavior:**
```
7:00 AM - 6:00 PM: Work time
  → Goes to work location (shop)
  → Stays at shop
  → Sells/buys from players

6:00 PM - 11:00 PM: Leisure time
  → Leaves shop
  → Goes to leisure locations
  → Wanders around

11:00 PM - 7:00 AM: Home time
  → Goes home
  → Stays home (sleeping)
```

---

#### For POLIZEI (Police)

**No Schedule:**
- Uses patrol system instead
- Always active (no sleep)
- Patrols designated routes

---

### Default Times

**If Not Configured:**
```
Work Start: 6:00 AM (0600)
Work End: 7:00 PM (1900)
Home Time: 5:00 AM (0500)
```

---

## Inventory & Wallet

### Inventory System

**Structure:**
- **Slots:** 9 (indexed 0-8, like player hotbar)
- **Available For:** BEWOHNER, VERKAEUFER only
- **Not For:** POLIZEI (no inventory)

**Slot Layout:**
```
[0] [1] [2] [3] [4] [5] [6] [7] [8]
```

**Stacking:** Follows Minecraft rules (max 64 for most items)

---

### Inventory Commands

```bash
# View inventory
/npc <name> inventory

# Give item to slot
/npc <name> inventory give <0-8> <item>

# Clear all slots
/npc <name> inventory clear

# Clear specific slot
/npc <name> inventory clear <0-8>
```

**Examples:**
```bash
# Give diamonds to slot 0
/npc Shop_Owner inventory give 0 minecraft:diamond

# Give cigars to slot 1
/npc Merchant inventory give 1 schedulemc:virginia_cigar

# Clear slot 2
/npc Shop_Owner inventory clear 2

# Clear all inventory
/npc Shop_Owner inventory clear
```

---

### Wallet System

**Currency:** Cash (Bargeld)
**Type:** Integer (0 to 2,147,483,647)
**Purpose:** NPC money for transactions

**Daily Income:**
- NPCs receive automatic daily income
- Tracked per NPC
- Configurable amount

---

### Wallet Commands

```bash
# View wallet
/npc <name> wallet

# Set exact amount
/npc <name> wallet set <amount>

# Add money
/npc <name> wallet add <amount>

# Remove money (if sufficient)
/npc <name> wallet remove <amount>
```

**Examples:**
```bash
# Check wallet
/npc Shop_Owner wallet
→ "Shop_Owner has 25,450€"

# Set wallet to 50,000€
/npc Shop_Owner wallet set 50000

# Add 10,000€
/npc Shop_Owner wallet add 10000
→ "Added 10,000€. New balance: 60,000€"

# Remove 5,000€
/npc Shop_Owner wallet remove 5000
→ "Removed 5,000€. New balance: 55,000€"
```

---

## Shop System

### Shop Inventory

**Two Separate Inventories:**

1. **Buy Shop** - Items NPC sells to players
2. **Sell Shop** - Items NPC buys from players

**Shop Entry Structure:**
```
Item: Diamond
Price: 100€
Stock: 64 (or UNLIMITED)
```

---

### Shop Configuration

**Method:** SHIFT + Left-click NPC (admin only)

**Opens GUI with:**
- Items to sell list
- Items to buy list
- Price per item
- Stock limits
- Unlimited toggle

**Example Configuration:**
```
Buy Shop (NPC sells):
- Diamond × 64 @ 100€ each (unlimited)
- Gold Ingot × 128 @ 80€ each (limited)
- Emerald × 32 @ 120€ each (unlimited)

Sell Shop (NPC buys):
- Virginia Cigar @ 45€ each (unlimited)
- Cannabis Bud @ 150€ each (unlimited)
```

---

### Warehouse Integration

**NEW FEATURE:** NPCs can sell from warehouses for unlimited stock.

**Setup:**
```bash
# 1. Link NPC to warehouse (look at warehouse block)
/npc Shop_Owner warehouse set

# 2. Check linkage
/npc Shop_Owner warehouse info

# 3. Clear if needed
/npc Shop_Owner warehouse clear
```

**How It Works:**
```
Player buys Diamond from NPC:
1. Check NPC inventory first (9 slots)
2. If not enough, check warehouse
3. If warehouse has stock, sell from warehouse
4. Revenue goes to shop account
5. Unlimited stock as long as warehouse has items
```

**Example:**
```
NPC Inventory: Diamond × 16
Warehouse: Diamond × 544

Player buys 32 diamonds:
→ NPC sells 16 from inventory
→ NPC auto-restocks from warehouse (+16)
→ NPC sells 16 more
→ Warehouse: 528 remaining
→ NPC inventory: 16 (restocked)

Result: Unlimited stock from warehouse!
```

---

## Personality System

### Personality Traits

**5 Traits for Merchant Behavior:**

| Trait | Price | Free Item | Police Call | Distribution |
|-------|-------|-----------|-------------|--------------|
| **FRIENDLY** | 0.80× (20% off) | 5% chance | 30% chance | 20% |
| **NEUTRAL** | 1.00× (normal) | 2% chance | 70% chance | 40% |
| **GENEROUS** | 0.90× (10% off) | 15% chance | 50% chance | 15% |
| **GREEDY** | 1.30× (30% markup) | 0% | 90% chance | 15% |
| **SUSPICIOUS** | 1.10× (10% markup) | 0% | 95% chance | 10% |

---

### Personality Effects

#### 1. Price Modifiers

**Base Price Calculation:**
```
Item: Diamond
Base Price: 100€

FRIENDLY NPC: 100 × 0.80 = 80€ (20% discount)
NEUTRAL NPC: 100 × 1.00 = 100€ (normal)
GENEROUS NPC: 100 × 0.90 = 90€ (10% discount)
GREEDY NPC: 100 × 1.30 = 130€ (30% markup)
SUSPICIOUS NPC: 100 × 1.10 = 110€ (10% markup)
```

---

#### 2. Free Item Chance

**Random Gift System:**
```
GENEROUS NPC: 15% chance
  → "Shop_Owner: Here, take this as a gift!"
  → Player receives item for free

FRIENDLY NPC: 5% chance
  → Occasionally gives bonus items

NEUTRAL NPC: 2% chance
  → Rare gifts

GREEDY/SUSPICIOUS: 0%
  → Never gives anything free
```

---

#### 3. Police Calling

**When Player Steals/Causes Trouble:**
```
SUSPICIOUS NPC: 95% chance to call police
  → Very quick to report

GREEDY NPC: 90% chance
  → Protects merchandise

NEUTRAL NPC: 70% chance
  → Standard response

GENEROUS NPC: 50% chance
  → More forgiving

FRIENDLY NPC: 30% chance
  → Rarely calls police
```

---

### Buying Personality

**3 Buying Behaviors (for BEWOHNER NPCs):**

| Type | Mood Weight | Demand Weight | Max Budget | Threshold |
|------|-------------|---------------|------------|-----------|
| **SPARSAM** (Cautious) | 40% | 20% | 30% of wallet | 50+ score |
| **AUSGEWOGEN** (Balanced) | 30% | 30% | 50% of wallet | 40+ score |
| **IMPULSIV** (Impulsive) | 20% | 40% | 70% of wallet | 30+ score |

**Purpose:** Controls how NPCs buy items in dynamic economy

---

## Relationship System

### Relationship Levels

**Range:** -100 to +100

**Tiers:**

| Level | Tier | Price Modifier | Color |
|-------|------|----------------|-------|
| -100 to -50 | **HOSTILE** | +50% markup | Red |
| -49 to -10 | **UNFRIENDLY** | +25% markup | Orange |
| -9 to +9 | **NEUTRAL** | Normal | Gray |
| +10 to +49 | **FRIENDLY** | -10% discount | Green |
| +50 to +100 | **VERY FRIENDLY** | -20% discount | Dark Green |

---

### Gaining Relationship

**Actions that Improve:**

| Action | Points | Bonus |
|--------|--------|-------|
| Purchase (any) | +2 | Base |
| Purchase ≥1,000€ | +4 | Large |
| Purchase ≥5,000€ | +7 | Very large |
| Sell (any) | +1 | Base |
| Sell ≥1,000€ | +2 | Large |
| Help NPC | +10 | Assistance |

**Example:**
```
Start: 0 (Neutral)

Buy 10 diamonds (1,000€): +4
→ Level: 4 (Neutral)

Buy 50 diamonds (5,000€): +7
→ Level: 11 (Friendly, -10% prices)

Buy 100 diamonds (10,000€): +7
→ Level: 18 (Friendly)

...continue trading...

→ Level: 50+ (Very Friendly, -20% prices)
```

---

### Losing Relationship

**Actions that Decrease:**

| Action | Points | Severity |
|--------|--------|----------|
| Theft attempt | -20 | Serious |
| Caught stealing | -30 | Very serious |
| Attack NPC | -50 | Extremely serious |

**Example:**
```
Current: 50 (Very Friendly, -20% prices)

Attack NPC: -50
→ Level: 0 (Neutral, normal prices)

Attack again: -50
→ Level: -50 (Hostile, +50% prices!)
```

---

### Price Calculation

**Combined Formula:**
```
Final Price = Base Price × Personality × Relationship

Example 1 (Best Case):
Item: Diamond (100€ base)
NPC: FRIENDLY (0.80×)
Relationship: +100 (Very Friendly, 0.80×)
Price: 100 × 0.80 × 0.80 = 64€ (36% total discount!)

Example 2 (Worst Case):
Item: Diamond (100€ base)
NPC: GREEDY (1.30×)
Relationship: -100 (Hostile, 1.50×)
Price: 100 × 1.30 × 1.50 = 195€ (95% markup!)

Example 3 (Neutral):
Item: Diamond (100€ base)
NPC: NEUTRAL (1.00×)
Relationship: 0 (Neutral, 1.00×)
Price: 100 × 1.00 × 1.00 = 100€ (normal)
```

---

## Movement Behaviors

### Home Movement

**Goal:** MoveToHomeGoal
**Active:** During home/sleep time
**NPCs:** BEWOHNER, VERKAEUFER

**Behavior:**
```
11:00 PM (home time reached):
→ NPC pathfinds to home location
→ Arrives within 2 blocks
→ Stays at home until work/leisure time
→ No wandering while at home
```

**Configuration:**
```bash
/npc <name> schedule home 2300
# NPC will go home at 11 PM
```

---

### Work Movement

**Goal:** MoveToWorkGoal
**Active:** During work hours
**NPCs:** VERKAEUFER only (not BEWOHNER!)

**Behavior:**
```
7:00 AM (work start time):
→ NPC pathfinds to work location (shop)
→ Arrives within 2 blocks
→ Stays at work until work end time
→ Sells/buys from players

6:00 PM (work end time):
→ Leaves work
→ Goes to leisure or home
```

**Configuration:**
```bash
/npc Shop_Owner schedule workstart 0700
/npc Shop_Owner schedule workend 1800
```

---

### Leisure Movement

**Goal:** MoveToLeisureGoal
**Active:** During leisure time (not work, not home)
**NPCs:** BEWOHNER, VERKAEUFER

**Features:**
- **Max Locations:** 10 per NPC
- **Wander Radius:** 15 blocks from location
- **Location Change:** Every 5 minutes
- **Wander Interval:** Every 10 seconds

**Behavior:**
```
Leisure Time Active:
1. Pick random leisure location from list (1-10)
2. Navigate to location
3. Upon arrival, wander within 15-block radius
4. Every 10 seconds, pick new point in radius
5. After 5 minutes, switch to different location
6. Repeat
```

**Commands:**
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

**Example Setup:**
```bash
# Add park
/npc Hans leisure add

# Add bar
/npc Hans leisure add

# Add coffee shop
/npc Hans leisure add

# List locations
/npc Hans leisure list
→ 0: Park (120, 64, 180)
→ 1: Bar (105, 65, 195)
→ 2: Coffee (115, 64, 200)
```

---

### Police Patrol

**Goal:** PolicePatrolGoal
**Active:** Always (for POLIZEI NPCs)
**NPCs:** POLIZEI only

**Features:**
- **Max Points:** 16 patrol points
- **Wait Time:** 3 minutes per point (configurable)
- **Wander Radius:** 3 blocks (configurable)
- **Loop:** Continuous patrol from point 0 to last, repeat

**Behavior:**
```
Patrol Cycle:
1. Navigate to patrol point 0
2. Arrive within 2 blocks
3. Wander within 3-block radius
4. Wait for 3 minutes
5. Move to patrol point 1
6. Repeat steps 2-4
...
16. After last point, return to point 0
17. Continuous loop
```

**Configuration:**
- Patrol points set at police station
- Wait time: Config setting
- Wander radius: Config setting

---

## NPC Management

### Creating NPCs

**Spawn Command:**
```bash
/npc spawn <type> <name>
```

**Types:**
- `bewohner` - Resident
- `verkaeufer` - Merchant
- `polizei` - Police

**Examples:**
```bash
/npc spawn bewohner Hans_Mueller
/npc spawn verkaeufer Shop_Owner_Klaus
/npc spawn polizei Officer_Schmidt
```

---

### Complete Setup Example

**Full Merchant Setup:**
```bash
# 1. Spawn merchant
/npc spawn verkaeufer Electronics_Owner

# 2. Set schedule
/npc Electronics_Owner schedule workstart 0800
/npc Electronics_Owner schedule workend 1900
/npc Electronics_Owner schedule home 2300

# 3. Give money
/npc Electronics_Owner wallet set 100000

# 4. Set movement
/npc Electronics_Owner movement true
/npc Electronics_Owner speed 0.3

# 5. Add leisure spots
# (Stand at bar)
/npc Electronics_Owner leisure add
# (Stand at park)
/npc Electronics_Owner leisure add
# (Stand at restaurant)
/npc Electronics_Owner leisure add

# 6. Assign to shop
/npc Electronics_Owner setshop Electronics_Store

# 7. Link warehouse (look at warehouse block)
/npc Electronics_Owner warehouse set

# 8. Configure shop (SHIFT + Left-click NPC)
# Add items to buy/sell in GUI

# Done! NPC is fully functional
```

---

### NPC Information

```bash
/npc <name> info
```

**Output:**
```
═══ NPC INFO: Shop_Owner ═══

Type: VERKAEUFER (Merchant)
Status: Working
Position: 100, 64, 200 (world)
Shop: Electronics_Store

Schedule:
- Work Start: 08:00
- Work End: 19:00
- Home Time: 23:00

Wallet: 85,450€

Inventory (5/9 slots):
- Slot 0: Diamond × 16
- Slot 1: Gold Ingot × 32
- Slot 2: Emerald × 8
- Slot 3: Virginia Cigar × 24
- Slot 4: Cannabis Bud × 12

Warehouse: Linked (warehouse_electronics)
Movement: Enabled
Speed: 0.3

Personality: FRIENDLY (20% discount)
Merchant Category: BAUMARKT

Leisure Locations: 3
- 0: Park (150, 64, 210)
- 1: Bar (140, 65, 195)
- 2: Restaurant (160, 64, 180)
```

---

## Best Practices

### For Admins

#### 1. Balanced NPC Distribution

**Recommended Ratio (per 100 blocks):**
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

---

#### 2. Shop Placement

**Strategic Locations:**
```
✓ Central locations (spawn, town square)
✓ Near roads/paths
✓ Visible from distance
✓ Multiple shops clustered
✓ Near player plots

✗ Hidden areas
✗ Far from spawn
✗ Underground
✗ Isolated locations
```

---

#### 3. Realistic Schedules

**Example Schedules:**
```
Coffee Shop:
- Work: 0600-1200 (6 AM - noon)
- Early morning business

Restaurant:
- Work: 1100-2200 (11 AM - 10 PM)
- Lunch and dinner hours

Bar:
- Work: 1800-0200 (6 PM - 2 AM)
- Evening/night business

Hardware Store:
- Work: 0800-1800 (8 AM - 6 PM)
- Standard business hours

24/7 Store:
- No schedule, always open
- Or rotate multiple NPCs
```

---

#### 4. Wallet Funding

**Initial Funding Guidelines:**
```
Small Shop: 10,000-25,000€
Medium Shop: 25,000-75,000€
Large Shop: 75,000-200,000€
Warehouse Shop: 200,000+€

Monitor daily:
- Check wallet levels
- Refund if running low
- Adjust prices if needed
```

---

### For Players

#### 1. Build Relationships

**Strategy:**
```
1. Find shops with FRIENDLY personality
2. Make small purchases regularly (+2 per purchase)
3. Build to +50 for -20% discount
4. Save thousands of € long-term
```

---

#### 2. Shop Around

**Price Comparison:**
```
Diamond prices:
Shop A (GREEDY, -50 relationship): 195€
Shop B (NEUTRAL, 0 relationship): 100€
Shop C (FRIENDLY, +50 relationship): 64€

Savings per diamond: 131€
Savings on 100 diamonds: 13,100€!
```

---

#### 3. Avoid Negative Actions

**Consequences:**
```
Steal from shop: -30 relationship
→ Prices increase 15-25%
→ Hard to recover

Attack NPC: -50 relationship
→ Prices increase 25-50%
→ Very hard to recover
→ Police called

Better: Just buy items legitimately!
```

---

## Troubleshooting

### "NPC Not Moving"

**Causes:**
1. Movement disabled
2. No schedule set
3. No home/work/leisure locations
4. NPC sleeping (home time)

**Solutions:**
```bash
# Enable movement
/npc <name> movement true

# Set schedule
/npc <name> schedule workstart 0700
/npc <name> schedule workend 1800
/npc <name> schedule home 2300

# Add leisure spots
/npc <name> leisure add

# Check current time
# If NPC home time, they stay home (expected)
```

---

### "NPC Won't Sell Items"

**Causes:**
1. Shop not configured
2. Item not in shop inventory
3. NPC not at work (not work hours)
4. Warehouse not linked (if using warehouse)

**Solutions:**
```bash
# Check shop assignment
/npc <name> info

# Configure shop (SHIFT + Left-click)
# Add items to "Buy Shop"

# Check time - must be work hours
# Merchants only sell during work time

# Link warehouse if needed
/npc <name> warehouse set
```

---

### "NPC Can't Buy From Players"

**Causes:**
1. No wallet money
2. Sell shop not configured
3. Not work hours
4. Price too high

**Solutions:**
```bash
# Check wallet
/npc <name> wallet
→ If low, add money:
/npc <name> wallet add 50000

# Configure sell shop (SHIFT + Left-click)
# Add items NPC buys in "Sell Shop"

# Check work hours
/npc <name> info
```

---

### "Prices Too High/Low"

**Causes:**
1. Personality trait (GREEDY = +30%)
2. Bad relationship (-50 = +25%)
3. Combined effects

**Solutions:**
```
For admins:
- NPC personality is set on spawn (random)
- Cannot change personality after creation
- Delete and respawn NPC for different personality

For players:
- Improve relationship by trading
- Find different NPC with better personality
- Build to +50 relationship for -20% discount
```

---

<div align="center">

**NPC System - Complete Guide**

For related systems:
- [🏘️ Plot System](Plot-System.md)
- [💰 Economy System](Economy-System.md)
- [🏪 Warehouse System](Warehouse-System.md)

[🏠 Back to Wiki Home](../Home.md) • [📋 All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
