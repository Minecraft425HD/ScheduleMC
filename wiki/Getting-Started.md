# Getting Started with ScheduleMC

The definitive beginner's guide to **ScheduleMC v3.6.0-beta** -- from installation to running your first roleplay economy server.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [First Steps for Players](#first-steps-for-players)
- [Economy Basics](#economy-basics)
- [Plot Management Basics](#plot-management-basics)
- [NPC Interaction](#npc-interaction)
- [Production Basics](#production-basics)
- [Vehicle Basics](#vehicle-basics)
- [Smartphone](#smartphone)
- [Admin Setup Guide](#admin-setup-guide)
- [Useful Commands Quick Reference](#useful-commands-quick-reference)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before installing ScheduleMC, make sure you have the following software and hardware ready.

| Requirement | Version / Spec | Notes | Download |
|---|---|---|---|
| **Minecraft Java Edition** | 1.20.1 | Must be exactly 1.20.1 | [minecraft.net](https://www.minecraft.net/) |
| **Minecraft Forge** | 47.4.0 or newer | The mod loader that ScheduleMC runs on | [files.minecraftforge.net](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) |
| **Java** | 17 (LTS) | Eclipse Temurin (Adoptium) is recommended | [adoptium.net](https://adoptium.net/) |
| **RAM** | 4 GB minimum, 8 GB recommended | Allocate at least 4 GB to the Minecraft JVM; 8 GB is recommended for servers with many players and NPCs | -- |

### Verifying Java

Open a terminal or command prompt and run:

```
java -version
```

You should see output containing `openjdk version "17.x.x"` or similar. If not, install or update Java 17 from [Adoptium](https://adoptium.net/).

---

## Installation

### Step 1: Install Minecraft Forge

1. Download the **Forge 1.20.1 Installer** (version 47.4.0 or newer) from [files.minecraftforge.net](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html).
2. Run the installer:
   - **Windows / macOS**: Double-click the downloaded `.jar` file.
   - **Linux / Command Line**:
     ```bash
     java -jar forge-1.20.1-47.4.0-installer.jar
     ```
3. Select **"Install client"** (for playing) or **"Install server"** (for hosting).
4. Click **OK** and wait for the installation to complete.
5. Launch Minecraft once with the new **Forge** profile to generate the `mods/` folder.

### Step 2: Download ScheduleMC

Download the latest ScheduleMC JAR from the [GitHub Releases page](https://github.com/Minecraft425HD/ScheduleMC/releases).

The file will be named something like `ScheduleMC-3.6.0-beta.jar`.

### Step 3: Place in the mods Folder

Copy the ScheduleMC JAR into your `mods/` directory:

**For Players (Client):**
```
.minecraft/
  mods/
    ScheduleMC-3.6.0-beta.jar
```

**For Server Operators:**
```
minecraft_server/
  mods/
    ScheduleMC-3.6.0-beta.jar
```

### Step 4: Required Dependency -- CoreLib

ScheduleMC requires **CoreLib** for the vehicle system (networking, OBJ model rendering).

| Dependency | Version | Purpose |
|---|---|---|
| **CoreLib** | 1.20.1-1.1.1 | Vehicle networking, OBJ model support |

Download CoreLib and place it in the same `mods/` folder alongside ScheduleMC.

### Step 5: Optional Dependencies

These mods are not required but enhance the experience:

| Mod | Purpose |
|---|---|
| **JEI** (Just Enough Items) | View crafting recipes and item information |
| **Jade** | See block/entity tooltips when looking at them |
| **The One Probe** | Advanced block information overlay |

### Step 6: Launch

1. Open the Minecraft Launcher.
2. Select the **Forge 1.20.1** profile.
3. Click **Play**.
4. On the title screen, click **Mods** and verify that **ScheduleMC** appears in the mod list.

---

## First Steps for Players

When you first join a ScheduleMC server, the mod sets you up automatically. Here is what happens and what to do next.

### 1. Join the Server -- Automatic Account Creation

The moment you connect, ScheduleMC creates a bank account for you with a starting balance of **1,000 Euro**. You also receive a welcome message and your player profile is registered in the contact system.

There is nothing you need to do -- this happens instantly on your first login.

### 2. Collect Your Daily Reward (Automatic)

Daily rewards are **claimed automatically every time you log in**, once per real-world day. You do not need to type a command.

- **Base reward**: 50 Euro
- **Streak bonus**: +10 Euro for every consecutive day you log in
- **Maximum streak**: 30 days (up to 340 Euro per day at max streak)
- **Streak reset**: If you miss a day, your streak resets to zero

You will see a chat message showing your reward amount and current streak when you join.

### 3. Check Your Balance

```
/money
```

This is an admin command that shows detailed balance information. Players can also check their balance through the **Bank app** on the Smartphone (press **P**).

### 4. Get the Plot Selection Tool (Admin)

Plot creation is an admin-level action (requires OP level 2). If you are an admin or the server owner has given you permission:

```
/plot apartment wand
```

This gives you the **Plot Selection Tool**. Left-click to set Position 1, right-click to set Position 2. This creates a rectangular selection that defines the boundaries of your plot or apartment.

### 5. Create Your First Plot (Admin)

With two positions selected, create a residential plot:

```
/plot create residential "My Home" 50000
```

This creates a **Residential** plot named "My Home" with a sale price of 50,000 Euro. Once created, you can manage it through the **Smartphone Settings App**.

### 6. Send Money to Another Player

While there is no standalone `/pay` command registered in the current version, money transfers between players are handled through the **ATM blocks** and the **Smartphone Bank App**. Admins can transfer money with:

```
/money give <player> <amount>
```

---

## Economy Basics

ScheduleMC features one of the most comprehensive economy systems in Minecraft modding, powered by 11 specialized manager classes.

### Bank Account

- Every player receives a **bank account** on first join with a configurable starting balance (default: **1,000 Euro**).
- Your balance is tracked server-side and persists across sessions.
- Transaction history records every deposit, withdrawal, purchase, and transfer.

### ATM Blocks

ATM blocks are physical blocks placed in the world. Right-click an ATM to:

- **Deposit** cash items from your inventory into your bank account.
- **Withdraw** money from your account as physical cash items.
- View your current balance and recent transactions.

### Physical Cash

Cash exists as actual items in your inventory. You can:

- Drop cash items on the ground for other players to pick up.
- Trade cash items directly with other players.
- Store cash in chests (but be careful -- it is not protected like a bank balance!).

### Loans (via Credit Advisor NPC)

Loans are handled through **Credit Advisor NPCs** rather than commands. Talk to a Credit Advisor NPC to apply for a loan:

| Loan Tier | Amount | Interest Rate | Repayment Period |
|---|---|---|---|
| **Small** | 5,000 Euro | 10% | 14 days |
| **Medium** | 25,000 Euro | 15% | 28 days |
| **Large** | 100,000 Euro | 20% | 56 days |

Your **Credit Score** affects loan approval and interest rates. Pay loans on time to improve your score.

### Savings Accounts

Savings accounts earn **5% weekly interest** on deposited funds:

- Minimum deposit: 1,000 Euro
- Lock period: 4 weeks (early withdrawal forfeits interest)
- Managed through the Smartphone Bank App or NPC interactions

### Daily Rewards

Daily rewards are automatically claimed when you log in each day:

| Component | Value |
|---|---|
| **Base reward** | 50 Euro |
| **Streak bonus** | +10 Euro per consecutive day |
| **Maximum streak** | 30 days |
| **Max daily reward** | 340 Euro (at 30-day streak) |
| **Missed day** | Streak resets to 0 |

### Taxes

The server economy includes a tax system managed by admins:

- **Property Tax** -- Tax on plot ownership
- **Sales Tax** -- Tax on NPC transactions
- **Income Tax** -- Tax on earnings
- All tax revenue goes to the **State Account** (government treasury)

### State Account (Admin)

The state account is the government treasury. Admins manage it with:

```
/state balance         -- View state account balance
/state deposit <amt>   -- Deposit money into state account
/state withdraw <amt>  -- Withdraw from state account
```

---

## Plot Management Basics

Plots are protected land regions. Only the owner and trusted players can build inside a plot.

### Plot Types

ScheduleMC has **7 plot types**, each with different properties:

| Type | Purchasable | Rentable | Description |
|---|---|---|---|
| **Residential** | Yes | Yes | Player homes and apartments |
| **Commercial** | Yes | Yes | Businesses and offices |
| **Shop** | No | No | NPC merchant shops (state-owned) |
| **Public** | No | No | Parks, roads, common areas |
| **Government** | No | No | Town halls, official buildings |
| **Prison** | No | No | Jail facilities for the crime system |
| **Towing Yard** | Yes | Yes | Vehicle impound lots |

### Creating Plots (Admin)

Plot creation requires **OP level 2**. The workflow is:

1. Get the selection tool: `/plot apartment wand`
2. Left-click the first corner of the area.
3. Right-click the second corner.
4. Run the creation command:

```
/plot create residential "My Home" 50000
/plot create commercial "Downtown Office" 75000
/plot create shop "General Store"
/plot create public "Central Park"
/plot create government "City Hall"
/plot create prison "State Prison"
/plot create towing_yard "Impound Lot" 30000
```

Purchasable types (residential, commercial, towing_yard) require a price argument. Non-purchasable types (shop, public, government, prison) do not.

### Plot Protection

- **Block protection**: Only the owner and trusted players can place or break blocks.
- **Container protection**: Chests, barrels, and other containers are protected.
- **Trusted players**: The plot owner can grant build permission to friends through the Settings App.

### Rating System

Plots can be rated by other players on a **5-star scale**. Ratings and reviews are visible on the Plot Info Screen.

### Rental and Apartment System

Plot owners can create **apartments** (sub-areas) within their plots:

```
/plot apartment wand                              -- Get selection tool
/plot apartment create "Suite 101" 500            -- Create apartment (500 Euro/month)
/plot apartment list                              -- List all apartments in current plot
/plot apartment info <id>                         -- View apartment details
/plot apartment rent <id> [days]                  -- Rent an apartment (default: 30 days)
/plot apartment leave                             -- End your rental (deposit refunded)
/plot apartment setrent <id> <monthlyRent>        -- Change rent price (owner only)
/plot apartment evict <id>                        -- Evict tenant (owner only, no deposit refund)
/plot apartment delete <id>                       -- Remove apartment (owner only)
```

Apartment deposits are **3x the monthly rent** and are refunded when the tenant leaves voluntarily.

### Plot Management via Smartphone

Most day-to-day plot management (buying, selling, naming, descriptions, trusting players, viewing info, transferring ownership, abandoning) is handled through the **Smartphone Settings App** rather than commands. Open your Smartphone (press **P**) and navigate to the Plot app.

---

## NPC Interaction

ScheduleMC features advanced AI-driven NPCs with daily schedules, personalities, inventories, and wallets.

### NPC Types

| Type | Internal Name | Description |
|---|---|---|
| **Resident** | BEWOHNER / CITIZEN | Regular citizens who wander, socialize, and sleep |
| **Merchant** | VERKAEUFER / MERCHANT | Shop owners who buy and sell items from their warehouse |
| **Police** | POLIZEI / POLICE | Law enforcement -- chases criminals, makes arrests |
| **Banker** | BANK / BANKER | Handles banking and credit operations |
| **Tow Operator** | ABSCHLEPPER | Tows illegally parked or abandoned vehicles |
| **Drug Dealer** | DRUG_DEALER | Underground economy NPC |

### Talking to NPCs

Right-click on an NPC to interact. The interaction depends on the NPC type:

- **Merchants**: Opens a trading interface where you can buy items from their inventory or sell your items to them. Prices are influenced by the dynamic market system and your relationship with the NPC.
- **Residents**: Chat, build relationships, get quests.
- **Police**: Generally best avoided if you have wanted stars.
- **Bankers**: Access banking services, apply for loans.

### NPC Schedules

NPCs follow realistic daily schedules based on Minecraft time:

| Time | Activity | Description |
|---|---|---|
| **07:00** | Work Start | Merchants go to their shop; police begin patrol |
| **12:00** | Leisure | NPCs visit leisure locations (parks, restaurants) |
| **18:00** | Work End | Merchants close shop and head to leisure spots |
| **23:00** | Sleep | NPCs return home and sleep until morning |

Schedule times are configurable per NPC by admins.

### Relationship System

Your relationship with NPCs affects gameplay:

- **Better relationships** lead to lower prices and better trade deals.
- **Worse relationships** mean higher prices and possible refusal to trade.
- Build relationships through repeated positive interactions, completing quests, and regular patronage.

### Police NPCs and the Wanted System

The police system is inspired by GTA-style wanted levels:

| Stars | Severity | Police Response |
|---|---|---|
| 1 | Minor offense | Single officer pursuit |
| 2 | Moderate crime | 2 officers |
| 3 | Serious crime | 3 officers |
| 4 | Major crime | 4 officers + backup calls |
| 5 | Most wanted | Full force deployment |

- Wanted stars **decay by 1 per day** automatically.
- Getting caught results in **arrest and jail time**.
- Use `/bail` to pay bail and get released from prison.
- Use `/jailtime` to check remaining sentence.
- Attacking a player who has their Smartphone open gives the attacker **+1 wanted star**.

---

## Production Basics

ScheduleMC includes **8 complete production chains** with detailed multi-step processes. Here is the general workflow using tobacco as an example.

### General Production Workflow

```
1. PLANTING    -- Place seeds in a pot (Terracotta, Ceramic, Iron, or Golden)
      |
2. GROWING     -- Water with watering can, apply fertilizer, use grow lights
      |           8 growth stages, 10-20 minutes depending on setup
      |
3. HARVESTING  -- Right-click mature plant to collect raw product
      |
4. PROCESSING  -- Process through the production chain specific to each product
      |           (drying, fermenting, extracting, refining, etc.)
      |
5. PACKAGING   -- Package finished product at a packaging table
      |           4 sizes: Small, Medium, Large, XL
      |
6. SELLING     -- Sell to NPC merchants or other players
                  Price depends on quality and market conditions
```

### Quality System

Products have quality levels that affect their value:

| Quality | Influence |
|---|---|
| **Poor** | Low-tier pots, no fertilizer |
| **Good** | Mid-tier pots, basic care |
| **Very Good** | High-tier pots, fertilizer + grow lights |
| **Legendary** | Golden pots, quality boosters, optimal conditions |

### The 8 Production Chains

| # | Product | Steps | Key Equipment |
|---|---|---|---|
| 1 | **Tobacco** | 6 steps | Pots, Drying Racks, Barrels, Packaging Tables |
| 2 | **Cannabis** | 8 steps | Pots, Drying Net, Trim Station, Curing Glass, Hash Press |
| 3 | **Coca / Cocaine** | 5 steps | Pots, Extraction Vats, Refineries, Crack Cooker |
| 4 | **Poppy / Opium** | 6 steps | Pots, Scoring Machine, Press, Cooking Station, Refinery |
| 5 | **Methamphetamine** | 4 steps | Chemistry Mixer, Reduction Kettle (explosion risk!), Crystallizer, Vacuum Dryer |
| 6 | **LSD** | 6 steps | Fermentation Tank, Distillation Apparatus, Micro Doser, Perforation Press |
| 7 | **MDMA / Ecstasy** | 4 steps | Reaction Kettle, Drying Oven, Pill Press (timing minigame!) |
| 8 | **Psilocybin Mushrooms** | 4 steps | Spore Syringes, Mist Bags, Climate Lamps, Water Tank |

### Additional Production Systems (Legal)

ScheduleMC also includes legal production chains:

| Product | Key Equipment |
|---|---|
| **Coffee** | Coffee plants, roasting, brewing |
| **Wine** | Grape vines, fermentation, bottling |
| **Cheese** | Milk processing, aging |
| **Honey** | Beehives, extraction |
| **Chocolate** | Cocoa processing |
| **Beer** | Malting, mashing, brewing, fermenting, conditioning, bottling |

### Tips for New Producers

- Start with **Tobacco** -- it has the most straightforward chain and good profit margins.
- **Golden pots** produce the highest quality but are expensive.
- **Grow lights** (Basic, Advanced, Premium) significantly speed up growth.
- Larger processing equipment handles bigger batches faster.
- Check market prices before selling -- timing matters.

---

## Vehicle Basics

ScheduleMC includes a full vehicle system with drivable cars, fuel management, and customization.

### Spawning a Vehicle

Vehicles are spawned using the **Vehicle Spawn Tool** (admin item). Once spawned, a vehicle exists as an entity in the world.

### Driving

- **Mount**: Right-click the vehicle to get in.
- **W / S**: Accelerate / Brake (Reverse).
- **A / D**: Steer left / right.
- **Dismount**: Press Shift (Sneak) to exit the vehicle.

### Fuel System

Vehicles consume fuel as you drive:

- **Fuel Stations**: Multi-block structures placed in the world. Drive up and right-click to refuel.
- **Diesel Canisters**: Portable fuel containers you can carry in your inventory.
- Running out of fuel stops the vehicle.

### Vehicle Types

| Type | Chassis | Best For |
|---|---|---|
| **Limousine** | Limousine Chassis | Standard city driving |
| **Van** | Van Chassis | Cargo transport |
| **Truck** | Truck Chassis | Heavy hauling |
| **SUV** | Offroad Chassis | Off-road terrain |
| **Sports Car** | Luxus Chassis | Speed and performance |

### Garage and Repair

- **Garage blocks** (Werkstatt) allow vehicle storage and repair.
- Vehicles take damage from collisions and wear.
- Use **Maintenance Kits** to repair damage.
- Vehicles have modular parts (engines, tires, chassis, bumpers) that can be upgraded.

### Vehicle Parts

| Category | Options |
|---|---|
| **Engines** | Normal, Performance, Performance II |
| **Tires** | Standard, Sport, Premium, Offroad, All-Terrain, Heavy Duty |
| **Chassis** | Limousine, Van, Truck, Offroad, Luxus |
| **Bumpers** | Basic, Chrome, Sport |
| **Fuel Tanks** | 15L, 30L, 50L |

---

## Smartphone

The in-game Smartphone is your central hub for managing most ScheduleMC features.

### Opening the Smartphone

Press **P** (default keybind, configurable in Controls settings) to open the Smartphone GUI.

### Apps

The Smartphone includes **11 functional apps**:

| # | App | Icon Color | Description |
|---|---|---|---|
| 1 | **Map** | Blue | View plot locations, navigate the world |
| 2 | **Dealer** | Red | Find merchants, compare product prices |
| 3 | **Products** | Green | Browse the shop product catalog |
| 4 | **Order** | Yellow | Order management |
| 5 | **Contacts** | Purple | Player and NPC contact list |
| 6 | **Messages** | Cyan | Send and receive messages (inbox) |
| 7 | **Plot** | Gold | Manage your plots (buy, sell, trust, rename, etc.) |
| 8 | **Settings** | Gray | Smartphone and player settings |
| 9 | **Bank** | Dark Green | View balance, transactions, banking operations |
| 10 | **Crime Stats** | Dark Red | Check your wanted level and crime history |
| 11 | **Chat** | Cyan | Direct messaging with other players |

### PvP Protection

While your Smartphone is open, you receive **full PvP immunity**:

- You cannot take damage from other players or NPCs.
- Any player who attacks you while your Smartphone is open receives **+1 wanted star**.
- This prevents unfair kills while you are navigating menus.

Protection is automatically removed when you close the Smartphone.

### Notifications

Apps can send notifications to your Smartphone. External mods can also register custom apps and send notifications through the Smartphone API.

---

## Admin Setup Guide

This section walks server administrators through the complete setup process for a new ScheduleMC server.

### Step 1: Create Spawn and Public Plots

First, define the key areas of your server:

```
-- Get the selection tool
/plot apartment wand

-- Select the spawn area (left-click corner 1, right-click corner 2)

-- Create a public spawn area
/plot create public "Spawn"

-- Create a government plot for the town hall
/plot create government "Town Hall"

-- Create a commercial marketplace
/plot create commercial "Marketplace" 100000

-- Create residential areas for player homes
/plot create residential "Suburbs Lot 1" 50000
/plot create residential "Suburbs Lot 2" 50000
```

### Step 2: Spawn Merchant NPCs with Schedules

Use the admin NPC tools to spawn and configure merchants:

```
-- Spawn a merchant NPC (use admin tools -- NPC Spawner Tool)
-- Configure via commands:

/npc "Hans" info                        -- View NPC details
/npc "Hans" schedule workstart 0700     -- Start work at 7:00 AM
/npc "Hans" schedule workend 1800       -- End work at 6:00 PM
/npc "Hans" schedule home 2300          -- Go home at 11:00 PM
/npc "Hans" movement true               -- Enable pathfinding movement
/npc "Hans" speed 0.5                   -- Set walk speed

-- Set leisure locations (stand at the location and run):
/npc "Hans" leisure add                 -- Add current position as leisure spot
/npc "Hans" leisure add                 -- Add another (up to 10)
/npc "Hans" leisure list                -- View all leisure locations

-- Give inventory for selling:
/npc "Hans" inventory give 0 minecraft:diamond
/npc "Hans" inventory give 1 minecraft:iron_ingot
/npc "Hans" inventory give 2 minecraft:golden_apple

-- Set wallet (money the NPC has to buy from players):
/npc "Hans" wallet set 10000
```

### Step 3: Setup the Economy

```
-- Fund the state (government) account
/state deposit 1000000

-- Give a player starting money (if you want more than the default 1,000)
/money give <player> 5000

-- Set the money balance directly
/money set <player> 10000

-- View a player's transaction history
/money history <player> 20
```

### Step 4: Create a Prison

```
-- Select the prison area
/plot apartment wand
-- (Left-click and right-click to define area)

-- Create a prison plot
/plot create prison "State Prison"

-- Register it as a prison
/prison create <plotId>

-- Add cells
/prison addcell 1 <x1> <y1> <z1> <x2> <y2> <z2> 1
/prison addcell 2 <x1> <y1> <z1> <x2> <y2> <z2> 2

-- List prisons and cells
/prison list
/prison cells

-- View and manage inmates
/prison inmates
/prison status <player>
/prison release <player>
```

### Step 5: Setup the Hospital

The hospital system handles player respawning and death fees:

```
-- Stand at the hospital spawn point and run:
/hospital setspawn

-- Set the death fee (money deducted on death)
/hospital setfee 500

-- View current hospital configuration
/hospital info
```

### Step 6: Setup Warehouses

Warehouses are mass-storage blocks that supply NPC merchants:

```
-- Place a Warehouse block in the world

-- Link it to a shop
/warehouse setshop "generalstore"

-- Link an NPC to the warehouse (look at the warehouse block):
/npc "Hans" warehouse set

-- Stock the warehouse
/warehouse add minecraft:diamond 64
/warehouse add minecraft:iron_ingot 256
/warehouse add minecraft:golden_apple 32

-- View warehouse status
/warehouse info

-- Trigger a manual delivery cycle
/warehouse deliver
```

Warehouses auto-deliver every 3 days (configurable). The state account pays delivery costs.

### Step 7: Additional Setup

**Door Locks:**
```
/lock list                              -- View all locks
/lock info <lock-id>                    -- Lock details
/lock setcode <lock-id> <code>          -- Set combination code
/lock authorize <lock-id> <player>      -- Authorize a player
/lock remove <lock-id>                  -- Remove a lock
```

**Player Levels (Producer Level):**
```
/admin setlevel <player> <level>        -- Set player level (0-30)
/admin addxp <player> <xp>             -- Award XP to a player
/admin getlevel <player>               -- Check player level
```

**Utility Monitoring:**
```
/utility                                -- View plot electricity/water usage
/utility top                            -- Top 10 consumers
/utility scan                           -- Scan current plot (admin)
/utility stats                          -- Server-wide statistics
```

---

## Useful Commands Quick Reference

### Player Commands

| Command | Description | Permission |
|---|---|---|
| `/bail` | Pay bail to get released from prison | Everyone |
| `/jailtime` | Check remaining prison sentence | Everyone |
| `/utility` | View electricity and water usage for current plot | Everyone |
| `/utility stats` | Server-wide utility statistics | Everyone |
| `/strom` | Alias for `/utility` (electricity) | Everyone |
| `/wasser` | Alias for `/utility` (water) | Everyone |

### Economy Commands (Admin -- OP 2)

| Command | Description |
|---|---|
| `/money set <player> <amount>` | Set a player's balance |
| `/money give <player> <amount>` | Add money to a player's account |
| `/money take <player> <amount>` | Remove money from a player's account |
| `/money history <player> [limit]` | View transaction history (default: 10) |
| `/state balance` | View state account balance |
| `/state deposit <amount>` | Deposit into state account |
| `/state withdraw <amount>` | Withdraw from state account |

### Plot Commands (Admin -- OP 2)

| Command | Description |
|---|---|
| `/plot create <type> <name> [price]` | Create a new plot |
| `/plot remove` | Remove the plot you are standing in |
| `/plot setowner <player>` | Set plot owner |
| `/plot settype <type>` | Change plot type |
| `/plot reindex` | Rebuild spatial index (debug) |
| `/plot debug` | Show debug info for current position |
| `/plot warehouse set` | Link warehouse to current plot |
| `/plot warehouse clear` | Unlink warehouse from current plot |
| `/plot warehouse info` | View warehouse link info |

### Apartment Commands

| Command | Description | Permission |
|---|---|---|
| `/plot apartment wand` | Get the plot selection tool | Everyone |
| `/plot apartment create <name> <rent>` | Create apartment in current plot | Plot owner |
| `/plot apartment delete <id>` | Delete an apartment | Plot owner |
| `/plot apartment list` | List apartments in current plot | Everyone |
| `/plot apartment info <id>` | View apartment details | Everyone |
| `/plot apartment rent <id> [days]` | Rent an apartment (default: 30 days) | Everyone |
| `/plot apartment leave` | End your rental | Tenant |
| `/plot apartment setrent <id> <rent>` | Change monthly rent | Plot owner |
| `/plot apartment evict <id>` | Evict a tenant | Plot owner |

### NPC Commands (Admin -- OP 2)

| Command | Description |
|---|---|
| `/npc <name> info` | Show NPC information |
| `/npc <name> movement <true/false>` | Enable/disable NPC movement |
| `/npc <name> speed <0.1-1.0>` | Set movement speed |
| `/npc <name> schedule workstart <HHMM>` | Set work start time (e.g., 0700) |
| `/npc <name> schedule workend <HHMM>` | Set work end time (e.g., 1800) |
| `/npc <name> schedule home <HHMM>` | Set home time (e.g., 2300) |
| `/npc <name> leisure add` | Add current position as leisure spot |
| `/npc <name> leisure remove <index>` | Remove a leisure spot |
| `/npc <name> leisure list` | List all leisure spots |
| `/npc <name> leisure clear` | Remove all leisure spots |
| `/npc <name> inventory` | Show NPC inventory |
| `/npc <name> inventory give <slot> <item>` | Give item to NPC slot (0-8) |
| `/npc <name> inventory clear [slot]` | Clear inventory or single slot |
| `/npc <name> wallet` | Show NPC wallet balance |
| `/npc <name> wallet set <amount>` | Set wallet amount |
| `/npc <name> wallet add <amount>` | Add money to wallet |
| `/npc <name> wallet remove <amount>` | Remove money from wallet |
| `/npc <name> warehouse set` | Link NPC to warehouse (look at it) |
| `/npc <name> warehouse clear` | Unlink NPC from warehouse |
| `/npc <name> warehouse info` | View NPC warehouse link status |

### Prison Commands (Admin -- OP 2)

| Command | Description |
|---|---|
| `/prison create <plotId>` | Register a plot as a prison |
| `/prison addcell <num> <min> <max> [security]` | Add a cell (security 1-5) |
| `/prison removecell <num>` | Remove a cell |
| `/prison list` | List all prisons |
| `/prison cells` | List all cells |
| `/prison inmates` | List all current inmates |
| `/prison release <player>` | Release a player from prison |
| `/prison status <player>` | View a player's prison status |

### Hospital Commands (Admin -- OP 2)

| Command | Description |
|---|---|
| `/hospital setspawn` | Set hospital respawn at current position |
| `/hospital setfee <amount>` | Set the death/respawn fee |
| `/hospital info` | View hospital configuration |

### Warehouse Commands (Admin -- OP 2)

| Command | Description |
|---|---|
| `/warehouse info` | Show warehouse info (look at it) |
| `/warehouse add <item> <amount>` | Add items (up to 10,000) |
| `/warehouse remove <item> <amount>` | Remove items |
| `/warehouse clear` | Clear all warehouse contents |
| `/warehouse setshop <shopId>` | Link warehouse to a shop |
| `/warehouse deliver` | Trigger manual delivery cycle |
| `/warehouse reset` | Reset delivery timer |

### System Commands (Admin -- OP 2)

| Command | Description |
|---|---|
| `/health` | Overall system health report |
| `/health economy` | Economy system diagnostics |
| `/health plot` | Plot system diagnostics |
| `/health backups` | Backup file overview |
| `/health log` | Log health check to server console |
| `/admin setlevel <player> <level>` | Set producer level (0-30) |
| `/admin addxp <player> <xp>` | Award XP (1-100,000) |
| `/admin getlevel <player>` | View player level and XP |

---

## Troubleshooting

### Mod Does Not Load

**Symptoms**: ScheduleMC does not appear in the Mods list.

**Solutions**:
1. Verify you are running **Minecraft 1.20.1** with **Forge 47.4.0** or newer.
2. Confirm **Java 17** is installed (`java -version`).
3. Check that the ScheduleMC JAR is in the correct `mods/` folder.
4. Ensure **CoreLib 1.20.1-1.1.1** is also in the `mods/` folder.
5. Check the log file at `.minecraft/logs/latest.log` for error messages.

### Economy Data Not Saving

**Symptoms**: Player balances or transactions are lost after restart.

**Solutions**:
1. Check that `config/plotmod_economy.json` exists and is writable.
2. Verify the server has sufficient disk space.
3. Run `/health economy` to see detailed economy system diagnostics.
4. Check `/health backups` to see if automatic backups are being created.
5. Do not force-kill the server -- always use `/stop` to allow the IncrementalSaveManager to perform its final save.

### Plots Not Working

**Symptoms**: Plots are not being created, or protection is not active.

**Solutions**:
1. Ensure you have **OP level 2** to create plots.
2. Select two positions with the Plot Selection Tool before running `/plot create`.
3. Run `/health plot` to check plot system health.
4. Use `/plot debug` while standing in an area to check if a plot exists there.
5. Run `/plot reindex` to rebuild the spatial index if plots exist but are not being detected.

### NPCs Not Moving or Working

**Symptoms**: NPCs stand still, do not follow schedules, or do not sell items.

**Solutions**:
1. Check NPC info: `/npc <name> info`
2. Verify movement is enabled: `/npc <name> movement true`
3. Confirm schedule times are set: check work start, work end, and home times.
4. Ensure the NPC has a home location and work location set (use NPC tools).
5. Verify the NPC has inventory items and wallet funds for trading.
6. Check if the NPC is linked to a warehouse: `/npc <name> warehouse info`

### Plants Not Growing

**Symptoms**: Crops planted in pots remain at the same growth stage.

**Solutions**:
1. Water the plant using a **Watering Can**.
2. Apply **Fertilizer** to speed up growth.
3. Install **Grow Lights** (Basic, Advanced, or Premium) near the plant.
4. Use a better pot type (Golden pots produce the best results).
5. Growth takes 10-20 minutes in real time -- be patient.
6. Ensure the world is not frozen (at least one player must be online).

### Vehicles Not Spawning or Driving

**Symptoms**: Cannot spawn vehicles or they do not respond to controls.

**Solutions**:
1. Ensure **CoreLib 1.20.1-1.1.1** is installed in the `mods/` folder.
2. Use the **Vehicle Spawn Tool** to place vehicles.
3. Check that the vehicle has fuel (refuel at a Fuel Station or use a Diesel Canister).
4. Verify the vehicle is not damaged beyond use (repair at a Garage/Werkstatt).

### System Health Diagnostics

The `/health` command is your primary diagnostic tool:

```
/health              -- Full system overview (economy, plots, backups)
/health economy      -- Economy system details, account count, error status
/health plot         -- Plot system details, cache statistics, spatial index
/health backups      -- List all backup files with age and size
/health log          -- Write detailed health report to server console log
```

### Where to Get Help

- **GitHub Issues**: [github.com/Minecraft425HD/ScheduleMC/issues](https://github.com/Minecraft425HD/ScheduleMC/issues) -- Report bugs, request features
- **Discord**: [discord.gg/schedulemc](https://discord.gg/schedulemc) -- Community support and discussion
- **Server Logs**: Check `.minecraft/logs/latest.log` or `logs/latest.log` on the server for detailed error messages
- **Wiki**: Browse the full [Wiki Documentation](Home.md) for in-depth guides on every system

---

## Next Steps

Now that you have the basics, explore these resources to go deeper:

- [Complete Command Reference](Commands.md) -- All 161+ commands documented
- [Items Guide](Items.md) -- All 354 items with descriptions
- [Blocks Guide](Blocks.md) -- All 152 blocks and their uses
- [Production Systems](Production-Systems.md) -- Detailed guides for all 14 production chains
- [Plot System](features/Plot-System.md) -- Advanced plot management
- [Economy System](features/Economy-System.md) -- Banking, loans, taxes, investments
- [NPC System](features/NPC-System.md) -- AI behavior, schedules, shops, relationships
- [Police and Crime System](features/Police-Crime-System.md) -- Wanted levels, prison, bail
- [Vehicle System](features/Vehicle-System.md) -- Driving, fuel, parts, customization
- [Smartphone System](features/Smartphone-System.md) -- All 11 apps explained
- [Warehouse System](features/Warehouse-System.md) -- Mass storage and NPC supply chains
- [FAQ](FAQ.md) -- Frequently asked questions

---

*ScheduleMC v3.6.0-beta | Minecraft 1.20.1 | Forge 47.4.0+*

[Back to Wiki Home](Home.md)
