# Commands Reference

<div align="center">

**Complete Command Reference for ScheduleMC v3.6.0-beta**

All registered commands organized by system, with syntax, permissions, and examples.

[Back to Wiki Home](Home.md)

</div>

---

## Table of Contents

1. [Command Syntax Legend](#command-syntax-legend)
2. [Plot and Property Commands](#1-plot-and-property-commands) (24 commands)
3. [Plot Features via Settings App UI](#2-plot-features-via-settings-app-ui) (17 former commands)
4. [Economy and Money Commands](#3-economy-and-money-commands) (4 commands)
5. [NPC Commands](#4-npc-commands) (28 commands)
6. [Warehouse Commands](#5-warehouse-commands) (7 commands)
7. [Prison and Crime Commands](#6-prison-and-crime-commands) (10 commands)
8. [Bounty Commands](#7-bounty-commands) (4 commands)
9. [Hospital Commands](#8-hospital-commands) (3 commands)
10. [State Treasury Commands](#9-state-treasury-commands) (3 commands)
11. [Utility Commands](#10-utility-commands) (8 commands)
12. [Market Commands](#11-market-commands) (4 commands)
13. [Gang Commands](#12-gang-commands) (14 commands)
14. [Lock Commands](#13-lock-commands) (8 commands)
15. [Territory and Map Commands](#14-territory-and-map-commands) (2 commands)
16. [Health and Diagnostics Commands](#15-health-and-diagnostics-commands) (5 commands)
17. [Admin Commands](#16-admin-commands) (5 commands)
18. [Permission Level Reference](#permission-level-reference)
19. [Common Workflows](#common-workflows)

---

## Command Syntax Legend

| Notation | Meaning | Example |
|----------|---------|---------|
| `<required>` | Parameter must be provided | `/money set <player> <amount>` |
| `[optional]` | Parameter can be omitted | `/plot apartment rent <id> [days]` |
| `<a\|b>` | Choose one of the listed options | `/npc <name> movement <true\|false>` |
| `HHMM` | 24-hour time format, four digits | `0800` for 8:00 AM, `1700` for 5:00 PM |

### Permission Indicators

| Label | Meaning |
|-------|---------|
| **Player** | Available to all players, no special permission required |
| **Owner** | Requires plot or apartment ownership |
| **Tenant** | Requires active rental on the relevant apartment or plot |
| **Admin (OP 2)** | Requires operator level 2 or higher |

---

# 1. Plot and Property Commands

**Base command:** `/plot`
**Description:** Land management, plot creation, apartment system, and warehouse linking.

> **Note:** As of ScheduleMC 3.0, many former player-facing plot commands (buy, sell, trust, info, etc.) have been moved to the in-game **Settings App UI** (accessed via the Plot Info Block). These are listed separately in [Section 2](#2-plot-features-via-settings-app-ui). The commands below are the ones currently registered in the command dispatcher.

---

## Plot Creation

### `/plot create residential <name> <price>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Create a new residential plot from the current selection. Residential plots can be purchased and rented by players. |
| **Arguments** | `<name>` -- Plot name (no spaces, use underscores). `<price>` -- Purchase price in euros (minimum 0.01). |
| **Prerequisites** | Use the Plot Selection Tool (left-click first corner, right-click second corner) before running this command. |
| **Related System** | Plot System |

**Example:**
```
/plot create residential Downtown_House_1 50000
```

---

### `/plot create commercial <name> <price>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Create a new commercial plot. Commercial plots are purchasable and rentable, intended for businesses and offices. |
| **Arguments** | `<name>` -- Plot name. `<price>` -- Purchase price in euros. |
| **Related System** | Plot System |

**Example:**
```
/plot create commercial Office_Tower_A 120000
```

---

### `/plot create shop <name>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Create a shop plot. Shop plots are state-owned and cannot be purchased by players. They are used for NPC merchant shops with inventory systems. |
| **Arguments** | `<name>` -- Plot name. |
| **Notes** | No price argument is required. Shop plots belong to the state treasury. |
| **Related System** | Plot System, NPC System, Warehouse System |

**Example:**
```
/plot create shop Main_Street_Bakery
```

---

### `/plot create public <name>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Create a public plot. Public plots are not purchasable. Use for parks, roads, spawn areas, and other shared spaces. |
| **Arguments** | `<name>` -- Plot name. |
| **Related System** | Plot System |

**Example:**
```
/plot create public Central_Park
```

---

### `/plot create government <name>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Create a government plot. Government plots are not purchasable. Use for town halls, police stations, hospitals, and other civic buildings. |
| **Arguments** | `<name>` -- Plot name. |
| **Related System** | Plot System |

**Example:**
```
/plot create government City_Hall
```

---

### `/plot create prison <name>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Create a prison plot. Prison plots are not purchasable. After creation, use `/prison create` to register it as an active prison and `/prison addcell` to define cells within it. |
| **Arguments** | `<name>` -- Plot name. |
| **Related System** | Plot System, Prison System |

**Example:**
```
/plot create prison State_Penitentiary
```

---

### `/plot create towing_yard <name> <price>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Create a towing yard plot. Towing yard plots can be purchased and rented. They are used for vehicle impound and towing operations. |
| **Arguments** | `<name>` -- Plot name. `<price>` -- Purchase price in euros. |
| **Related System** | Plot System, Towing System |

**Example:**
```
/plot create towing_yard West_Side_Impound 75000
```

---

## Plot Administration

### `/plot setowner <player>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set the owner of the plot you are currently standing in. Overwrites any existing ownership without payment. |
| **Arguments** | `<player>` -- Target player (must be online). |
| **Notes** | Stand inside the target plot before running this command. |
| **Related System** | Plot System |

**Example:**
```
/plot setowner Steve
```

---

### `/plot remove`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Permanently delete the plot at your current position. All apartments, tenants, and ownership data are removed. This action cannot be undone. |
| **Rate Limit** | Maximum 3 deletions per second per player (DoS protection). |
| **Related System** | Plot System |

**Example:**
```
/plot remove
```

---

### `/plot settype <type>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Change the type of the plot at your current position. |
| **Arguments** | `<type>` -- One of: `RESIDENTIAL`, `COMMERCIAL`, `SHOP`, `PUBLIC`, `GOVERNMENT`, `PRISON`, `TOWING_YARD` (case-insensitive). |
| **Related System** | Plot System |

**Example:**
```
/plot settype COMMERCIAL
```

---

### `/plot reindex`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Rebuild the spatial index used for plot lookups. Use this after large-scale plot changes or if plot detection is not working correctly. |
| **Related System** | Plot System |

**Example:**
```
/plot reindex
```

---

### `/plot debug`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display debug information about the plot at your current position, including your coordinates, the plot ID and name (if any), and the total number of plots on the server. |
| **Related System** | Plot System |

**Example:**
```
/plot debug
```

---

## Plot Warehouse Linking

### `/plot warehouse set`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Link the plot at the current position to the warehouse block you are looking at (5-block range). The warehouse block must be within a plot boundary. |
| **Notes** | Look directly at a placed Warehouse Block before running this command. If no block is in your line of sight, the command checks the block below and at your feet as a fallback. |
| **Related System** | Plot System, Warehouse System |

**Example:**
```
/plot warehouse set
```

---

### `/plot warehouse clear`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Remove the warehouse link from the plot at your current position. |
| **Related System** | Plot System, Warehouse System |

**Example:**
```
/plot warehouse clear
```

---

### `/plot warehouse info`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display the warehouse linkage status and position for the plot at your current position. |
| **Related System** | Plot System, Warehouse System |

**Example:**
```
/plot warehouse info
```

---

## Apartment System

### `/plot apartment wand`

| | |
|---|---|
| **Permission** | Player (Plot Owner) |
| **Description** | Receive the Plot Selection Tool for defining apartment boundaries within your plot. Use left-click for the first corner and right-click for the second corner. |
| **Related System** | Plot System, Apartment System |

**Example:**
```
/plot apartment wand
```

---

### `/plot apartment create <name> <monthlyRent>`

| | |
|---|---|
| **Permission** | Player (Plot Owner) |
| **Description** | Create a new apartment unit within the plot you are standing in. The selection (from the apartment wand) must be entirely inside the plot and must not overlap existing apartments. |
| **Arguments** | `<name>` -- Apartment name (no spaces). `<monthlyRent>` -- Monthly rent in euros (minimum 0). |
| **Prerequisites** | Must own the plot. Must have an active selection from the apartment wand. |
| **Related System** | Apartment System |

**Example:**
```
/plot apartment create Penthouse_A 2000
```

---

### `/plot apartment delete <apartmentId>`

| | |
|---|---|
| **Permission** | Player (Plot Owner) |
| **Description** | Delete an apartment unit. The apartment must not currently be rented. You can search by apartment ID (e.g., `apt_1`) or by name. |
| **Arguments** | `<apartmentId>` -- Apartment ID or name. |
| **Related System** | Apartment System |

**Example:**
```
/plot apartment delete apt_1
```

---

### `/plot apartment list`

| | |
|---|---|
| **Permission** | Player |
| **Description** | List all apartments in the plot you are currently standing in, including name, ID, rent amount, rental status, and size. |
| **Related System** | Apartment System |

**Example:**
```
/plot apartment list
```

---

### `/plot apartment info <apartmentId>`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Show detailed information about a specific apartment, including rent, size, rental status, and remaining lease time. |
| **Arguments** | `<apartmentId>` -- Apartment ID or name. |
| **Related System** | Apartment System |

**Example:**
```
/plot apartment info apt_1
```

---

### `/plot apartment rent <apartmentId> [days]`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Rent an apartment. Default lease duration is 30 days. Payment includes the prorated rent plus a security deposit equal to 3 months' rent. The rent payment goes to the plot owner; the deposit is held and returned when the tenant leaves. |
| **Arguments** | `<apartmentId>` -- Apartment ID or name. `[days]` -- Lease duration in days (default: 30, minimum: 1). |
| **Restrictions** | Plot owners cannot rent their own apartments. The apartment must be available and not already rented. |
| **Related System** | Apartment System, Economy System |

**Example:**
```
/plot apartment rent apt_1
/plot apartment rent apt_1 60
```

---

### `/plot apartment leave`

| | |
|---|---|
| **Permission** | Tenant |
| **Description** | Terminate your current apartment lease. Your security deposit (3x monthly rent) is refunded. No refund is given for remaining rental days. You must be standing inside the plot containing your apartment. |
| **Related System** | Apartment System, Economy System |

**Example:**
```
/plot apartment leave
```

---

### `/plot apartment setrent <apartmentId> <monthlyRent>`

| | |
|---|---|
| **Permission** | Player (Plot Owner) |
| **Description** | Change the monthly rent for an apartment. Only affects future tenants; existing leases retain their original rate. |
| **Arguments** | `<apartmentId>` -- Apartment ID or name. `<monthlyRent>` -- New monthly rent in euros. |
| **Related System** | Apartment System |

**Example:**
```
/plot apartment setrent apt_1 2500
```

---

### `/plot apartment evict <apartmentId>`

| | |
|---|---|
| **Permission** | Player (Plot Owner) |
| **Description** | Immediately evict the tenant from an apartment. The tenant does NOT receive their security deposit back. Use only for serious violations. |
| **Arguments** | `<apartmentId>` -- Apartment ID or name. |
| **Related System** | Apartment System |

**Example:**
```
/plot apartment evict apt_1
```

---

# 2. Plot Features via Settings App UI

As of ScheduleMC 3.0, the following plot management features have been moved from chat commands to the **Settings App UI**, accessed by interacting with the Plot Info Block placed on your plot. These features are fully functional through the graphical interface.

| Former Command | Function | UI Location |
|----------------|----------|-------------|
| `/plot wand` | Get plot selection tool | Replaced by apartment wand for sub-areas |
| `/plot buy` | Purchase a plot | Plot Info Screen -- Buy button |
| `/plot list` | List all plots | Plot Info Screen -- Browse |
| `/plot info` | View plot details | Plot Info Screen -- Info panel |
| `/plot name <name>` | Set plot display name | Plot Info Screen -- Settings |
| `/plot description <text>` | Set plot description | Plot Info Screen -- Settings |
| `/plot trust <player>` | Grant build permissions | Plot Info Screen -- Trust management |
| `/plot untrust <player>` | Revoke build permissions | Plot Info Screen -- Trust management |
| `/plot trustlist` | List trusted players | Plot Info Screen -- Trust management |
| `/plot sell <price>` | List plot for sale | Plot Info Screen -- Sale settings |
| `/plot unsell` | Cancel sale listing | Plot Info Screen -- Sale settings |
| `/plot transfer <player>` | Transfer ownership | Plot Info Screen -- Settings |
| `/plot abandon` | Abandon plot (50% refund) | Plot Info Screen -- Settings |
| `/plot rent <pricePerDay>` | Offer plot for rent | Plot Info Screen -- Rental settings |
| `/plot rentcancel` | Cancel rent offer | Plot Info Screen -- Rental settings |
| `/plot rentplot <days>` | Rent a plot | Plot Info Screen -- Rent button |
| `/plot rentextend <days>` | Extend rental period | Plot Info Screen -- Rental settings |
| `/plot rate <1-5>` | Rate a plot | Plot Info Screen -- Rating buttons |
| `/plot topplots` | View top-rated plots | Plot Info Screen -- Rating display |

---

# 3. Economy and Money Commands

**Base command:** `/money`
**Description:** Administrative commands for managing player balances and viewing transaction history.

> **Note:** Player-facing economy features (checking balance, making payments, daily rewards, loans, savings, recurring payments) are accessed through the Smartphone Bank App and in-game ATM blocks. The commands below are admin-only operations.

---

### `/money set <player> <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set a player's balance to an exact amount. The target player is notified of the change. Logged as `ADMIN_SET` in transaction history. |
| **Arguments** | `<player>` -- Target player (must be online). `<amount>` -- New balance in euros (minimum 0, maximum 10,000,000). |
| **Related System** | Economy System |

**Example:**
```
/money set Steve 100000
```

---

### `/money give <player> <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Add money to a player's current balance. No transaction fee is applied. The target player is notified. Logged as `ADMIN_GIVE` in transaction history. |
| **Arguments** | `<player>` -- Target player (must be online). `<amount>` -- Amount to add in euros (minimum 0.01). |
| **Related System** | Economy System |

**Example:**
```
/money give Alex 5000
```

---

### `/money take <player> <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Remove money from a player's balance. Fails if the player does not have sufficient funds. The target player is notified. Logged as `ADMIN_TAKE` in transaction history. |
| **Arguments** | `<player>` -- Target player (must be online). `<amount>` -- Amount to remove in euros (minimum 0.01). |
| **Related System** | Economy System |

**Example:**
```
/money take Steve 1000
```

---

### `/money history <player> [limit]`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | View the transaction history for a specific player. Shows recent transactions with details, plus summary statistics (total income, total expenses, transaction count). |
| **Arguments** | `<player>` -- Target player (must be online). `[limit]` -- Number of transactions to show (1-100, default: 10). |
| **Related System** | Economy System |

**Example:**
```
/money history Steve
/money history Steve 25
```

---

# 4. NPC Commands

**Base command:** `/npc`
**Description:** Complete NPC lifecycle management including behavior, schedules, inventory, wallet, and warehouse integration. All NPC commands are admin-only.

> **Note:** All `/npc` commands require the NPC name as the first argument. Tab-completion is available and suggests registered NPC names from the NPCNameRegistry. NPC types include: `BEWOHNER` (Resident), `VERKAEUFER` (Merchant), `POLIZEI` (Police), `BANK` (Banker), `ABSCHLEPPER` (Tow Truck), and English aliases `CITIZEN`, `MERCHANT`, `POLICE`, `BANKER`, `DRUG_DEALER`.

---

## NPC Information

### `/npc <name> info`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display comprehensive information about an NPC, including type, movement status, speed, home location, work location, schedule times, leisure locations, inventory summary, and wallet balance. Output varies depending on NPC type (Merchant shows work location; Resident shows sleep time and leisure count; Police shows full schedule). |
| **Arguments** | `<name>` -- NPC name (tab-completable). |
| **Related System** | NPC System |

**Example:**
```
/npc Hans info
```

---

## NPC Movement

### `/npc <name> movement <true|false>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Enable or disable NPC pathfinding and movement. When disabled, the NPC stays at its current position. |
| **Arguments** | `<name>` -- NPC name. `<true\|false>` -- Enable or disable movement. |
| **Related System** | NPC System |

**Example:**
```
/npc Hans movement true
/npc Hans movement false
```

---

### `/npc <name> speed <value>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set the NPC's movement speed. |
| **Arguments** | `<name>` -- NPC name. `<value>` -- Speed value from 0.1 (very slow) to 1.0 (very fast). Default is approximately 0.3 (walking pace). |
| **Related System** | NPC System |

**Example:**
```
/npc Hans speed 0.5
```

---

## NPC Schedule

### `/npc <name> schedule workstart <time>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set when the NPC begins work. The NPC travels to its assigned work location at this time. For Residents (BEWOHNER), this is the wake-up time. Time is converted to Minecraft ticks internally (0 ticks = 6:00 AM). |
| **Arguments** | `<name>` -- NPC name. `<time>` -- Time in HHMM format (e.g., `0800` for 8:00 AM). |
| **Related System** | NPC System |

**Example:**
```
/npc Hans schedule workstart 0700
```

---

### `/npc <name> schedule workend <time>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set when the NPC finishes work. The NPC leaves its work location and transitions to leisure or home behavior. For Merchants, the associated shop closes at this time. |
| **Arguments** | `<name>` -- NPC name. `<time>` -- Time in HHMM format. |
| **Related System** | NPC System |

**Example:**
```
/npc Hans schedule workend 1800
```

---

### `/npc <name> schedule home <time>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set when the NPC goes home to sleep. The NPC travels to its home location and remains stationary until the next work start time. For Residents, this defines the start of the sleep period. |
| **Arguments** | `<name>` -- NPC name. `<time>` -- Time in HHMM format. |
| **Related System** | NPC System |

**Example:**
```
/npc Hans schedule home 2200
```

---

## NPC Leisure Locations

### `/npc <name> leisure add`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Add your current position as a leisure location for the NPC. During free time (after work, before home time), the NPC randomly visits its assigned leisure locations. Maximum 10 locations per NPC. |
| **Arguments** | `<name>` -- NPC name. |
| **Notes** | Stand at the desired location before running this command. |
| **Related System** | NPC System |

**Example:**
```
/npc Hans leisure add
```

---

### `/npc <name> leisure remove <index>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Remove a leisure location by its index. Use `/npc <name> leisure list` to see indices. |
| **Arguments** | `<name>` -- NPC name. `<index>` -- Location index (0-9, 0-based). |
| **Related System** | NPC System |

**Example:**
```
/npc Hans leisure remove 0
```

---

### `/npc <name> leisure list`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | List all leisure locations assigned to the NPC, with index numbers and coordinates. |
| **Arguments** | `<name>` -- NPC name. |
| **Related System** | NPC System |

**Example:**
```
/npc Hans leisure list
```

---

### `/npc <name> leisure clear`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Remove all leisure locations from the NPC. |
| **Arguments** | `<name>` -- NPC name. |
| **Related System** | NPC System |

**Example:**
```
/npc Hans leisure clear
```

---

## NPC Inventory

### `/npc <name> inventory`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display the NPC's inventory contents. Shows all 9 slots with item names, quantities, and empty slots. Only available for NPC types that have inventory (Residents and Merchants). |
| **Arguments** | `<name>` -- NPC name. |
| **Related System** | NPC System |

**Example:**
```
/npc Hans inventory
```

---

### `/npc <name> inventory give <slot> <item>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Place an item in a specific inventory slot. Merchant NPCs sell items from their inventory when interacted with by players. |
| **Arguments** | `<name>` -- NPC name. `<slot>` -- Inventory slot (0-8). `<item>` -- Minecraft item ID (e.g., `minecraft:diamond`). |
| **Related System** | NPC System |

**Example:**
```
/npc Hans inventory give 0 minecraft:diamond
/npc Hans inventory give 1 minecraft:gold_ingot
```

---

### `/npc <name> inventory clear`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Clear all items from the NPC's inventory. |
| **Arguments** | `<name>` -- NPC name. |
| **Related System** | NPC System |

**Example:**
```
/npc Hans inventory clear
```

---

### `/npc <name> inventory clear <slot>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Clear a specific inventory slot. |
| **Arguments** | `<name>` -- NPC name. `<slot>` -- Inventory slot to clear (0-8). |
| **Related System** | NPC System |

**Example:**
```
/npc Hans inventory clear 0
```

---

## NPC Wallet

### `/npc <name> wallet`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display the NPC's current cash balance. Only available for NPC types that have a wallet (Residents and Merchants). |
| **Arguments** | `<name>` -- NPC name. |
| **Related System** | NPC System, Economy System |

**Example:**
```
/npc Hans wallet
```

---

### `/npc <name> wallet set <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set the NPC's wallet to an exact amount. Syncs the new value to connected clients. |
| **Arguments** | `<name>` -- NPC name. `<amount>` -- Amount in whole euros (minimum 0). |
| **Related System** | NPC System, Economy System |

**Example:**
```
/npc Hans wallet set 10000
```

---

### `/npc <name> wallet add <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Add money to the NPC's wallet. Useful for funding merchant NPCs so they can buy items from players. |
| **Arguments** | `<name>` -- NPC name. `<amount>` -- Amount to add (minimum 1). |
| **Related System** | NPC System, Economy System |

**Example:**
```
/npc Hans wallet add 5000
```

---

### `/npc <name> wallet remove <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Remove money from the NPC's wallet. Fails if the NPC does not have sufficient funds. |
| **Arguments** | `<name>` -- NPC name. `<amount>` -- Amount to remove (minimum 1). |
| **Related System** | NPC System, Economy System |

**Example:**
```
/npc Hans wallet remove 2000
```

---

## NPC Warehouse Integration

### `/npc <name> warehouse set`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Link an NPC to the warehouse block you are looking at (5-block range). The NPC will sell items from the linked warehouse inventory, providing effectively unlimited stock as long as the warehouse is supplied. |
| **Arguments** | `<name>` -- NPC name. |
| **Notes** | Look directly at a placed Warehouse Block before running this command. |
| **Related System** | NPC System, Warehouse System |

**Example:**
```
/npc Hans warehouse set
```

---

### `/npc <name> warehouse clear`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Remove the warehouse link from the NPC. The NPC will revert to selling from its personal inventory only. |
| **Arguments** | `<name>` -- NPC name. |
| **Related System** | NPC System, Warehouse System |

**Example:**
```
/npc Hans warehouse clear
```

---

### `/npc <name> warehouse info`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display the warehouse linkage status for the NPC, including warehouse position, slot usage, and total item count. |
| **Arguments** | `<name>` -- NPC name. |
| **Related System** | NPC System, Warehouse System |

**Example:**
```
/npc Hans warehouse info
```

---

# 5. Warehouse Commands

**Base command:** `/warehouse`
**Description:** Direct warehouse block management for adding, removing, and inspecting inventory. All commands operate on the warehouse block the player is looking at or standing on.

---

### `/warehouse info`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display detailed warehouse information including position, slot usage, total items, linked shop ID, linked seller count, delivery schedule, and next delivery estimate. |
| **Related System** | Warehouse System |

**Example:**
```
/warehouse info
```

---

### `/warehouse add <item> <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Add items to the warehouse. If the warehouse does not have enough space for the full amount, as many items as possible are added and a notification is shown. |
| **Arguments** | `<item>` -- Minecraft item ID (e.g., `minecraft:diamond`). `<amount>` -- Number of items (1-10,000). |
| **Related System** | Warehouse System |

**Example:**
```
/warehouse add minecraft:diamond 64
/warehouse add minecraft:emerald 512
```

---

### `/warehouse remove <item> <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Remove items from the warehouse. Items are deleted, not given to the player. Fails if the item is not present in the warehouse. |
| **Arguments** | `<item>` -- Minecraft item ID. `<amount>` -- Number of items to remove (1-10,000). |
| **Related System** | Warehouse System |

**Example:**
```
/warehouse remove minecraft:diamond 32
```

---

### `/warehouse clear`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Remove all items from the warehouse. This action cannot be undone. |
| **Related System** | Warehouse System |

**Example:**
```
/warehouse clear
```

---

### `/warehouse setshop <shopId>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Link the warehouse to a shop plot by its ID. NPC merchants in the linked shop will sell items from this warehouse. |
| **Arguments** | `<shopId>` -- The plot ID of the target shop. |
| **Related System** | Warehouse System, Plot System, NPC System |

**Example:**
```
/warehouse setshop Main_Street_Bakery
```

---

### `/warehouse deliver`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Manually trigger a delivery cycle for the warehouse. Shows current day, last delivery day, and days since last delivery before executing. Delivery costs are paid from the state account. |
| **Related System** | Warehouse System, Economy System |

**Example:**
```
/warehouse deliver
```

---

### `/warehouse reset`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Reset the delivery timer to the current day. The next automatic delivery will occur after the configured interval (default: 3 days) from now. |
| **Related System** | Warehouse System |

**Example:**
```
/warehouse reset
```

---

# 6. Prison and Crime Commands

**Base command:** `/prison` (Admin), `/bail` and `/jailtime` (Player)
**Description:** Prison administration including cell management, inmate tracking, and player-facing bail and sentence queries.

---

## Prison Administration

### `/prison create <plotId>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Register an existing plot as a prison. The plot type is automatically set to `PRISON`. Cells must be added separately with `/prison addcell`. |
| **Arguments** | `<plotId>` -- ID of the plot to convert into a prison. |
| **Related System** | Prison System, Plot System |

**Example:**
```
/prison create State_Penitentiary
```

---

### `/prison addcell <cellNumber> <min> <max> [securityLevel]`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Add a prison cell to the default prison. Define the cell boundaries using block coordinates. |
| **Arguments** | `<cellNumber>` -- Cell ID number (starting from 1). `<min>` -- First corner coordinates (x y z). `<max>` -- Second corner coordinates (x y z). `[securityLevel]` -- Security level 1-5 (default: 1). |
| **Security Levels** | 1 = Minimum, 2 = Low, 3 = Medium, 4 = High, 5 = Maximum (solitary). |
| **Related System** | Prison System |

**Example:**
```
/prison addcell 1 100 50 10 110 55 15
/prison addcell 2 112 50 10 122 55 15 3
```

---

### `/prison removecell <cellNumber>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Remove a prison cell. If a prisoner is currently in the cell, they are released. |
| **Arguments** | `<cellNumber>` -- Cell number to remove. |
| **Related System** | Prison System |

**Example:**
```
/prison removecell 1
```

---

### `/prison list`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | List all prison plots on the server, showing plot ID and cell count. |
| **Related System** | Prison System |

**Example:**
```
/prison list
```

---

### `/prison cells`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | List all cells in the default prison, showing cell number, security level, and occupancy status. |
| **Related System** | Prison System |

**Example:**
```
/prison cells
```

---

### `/prison inmates`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | List all current prisoners with their names, cell numbers, and remaining sentence time in seconds. |
| **Related System** | Prison System |

**Example:**
```
/prison inmates
```

---

### `/prison status <player>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show detailed status for a specific prisoner, including cell number, remaining time (minutes and seconds), bail amount, and original wanted level. |
| **Arguments** | `<player>` -- Target player (must be online). |
| **Related System** | Prison System |

**Example:**
```
/prison status Steve
```

---

### `/prison release <player>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Immediately release a prisoner. The player is freed and the release is logged as `ADMIN_RELEASE`. |
| **Arguments** | `<player>` -- Target player (must be online and currently imprisoned). |
| **Related System** | Prison System |

**Example:**
```
/prison release Steve
```

---

## Player Prison Commands

### `/bail`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Pay bail to be released from prison early. The bail amount depends on your wanted level at the time of arrest. Requires sufficient funds. |
| **Related System** | Prison System, Economy System |

**Example:**
```
/bail
```

---

### `/jailtime`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Check your remaining jail time (minutes and seconds) and current bail amount. Shows a message if you are not currently imprisoned. |
| **Related System** | Prison System |

**Example:**
```
/jailtime
```

---

# 7. Bounty Commands

**Base command:** `/bounty`
**Description:** Player-driven bounty system for placing and tracking bounties on other players.

---

### `/bounty list`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Display the top 10 active bounties on the server, including target name, amount, and reason. |
| **Related System** | Bounty System, Crime System |

**Example:**
```
/bounty list
```

---

### `/bounty place <player> <amount> <reason>`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Place a bounty on another player. The amount is withdrawn from your balance. Both the amount and reason are validated for safety. |
| **Arguments** | `<player>` -- Target player (must be online). `<amount>` -- Bounty amount in euros (minimum 100). `<reason>` -- Reason for the bounty (free text). |
| **Related System** | Bounty System, Economy System |

**Example:**
```
/bounty place Steve 5000 Robbed my shop
```

---

### `/bounty info [player]`

| | |
|---|---|
| **Permission** | Player |
| **Description** | View bounty information. Without arguments, shows any active bounty on yourself. With a player argument, shows the bounty on the specified player. |
| **Arguments** | `[player]` -- Target player (optional, must be online). |
| **Related System** | Bounty System |

**Example:**
```
/bounty info
/bounty info Steve
```

---

### `/bounty history`

| | |
|---|---|
| **Permission** | Player |
| **Description** | View your bounty history (last 5 entries), including date, amount, reason, and whether each bounty was claimed or expired. |
| **Related System** | Bounty System |

**Example:**
```
/bounty history
```

---

# 8. Hospital Commands

**Base command:** `/hospital`
**Description:** Configure the hospital respawn system. All hospital commands are admin-only.

---

### `/hospital setspawn`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set the hospital respawn point to your current position. Players will respawn at this location on death and be charged the configured hospital fee. |
| **Related System** | Hospital System |

**Example:**
```
/hospital setspawn
```

---

### `/hospital setfee <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set the fee charged to players when they die and respawn at the hospital. The fee is deducted from the player's balance and deposited into the state account. |
| **Arguments** | `<amount>` -- Fee in euros (minimum 0 for free respawns). |
| **Related System** | Hospital System, Economy System |

**Example:**
```
/hospital setfee 500
```

---

### `/hospital info`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display the current hospital configuration, including spawn point coordinates and the death fee amount. |
| **Related System** | Hospital System |

**Example:**
```
/hospital info
```

---

# 9. State Treasury Commands

**Base command:** `/state`
**Description:** Manage the government treasury account. The state account funds warehouse deliveries, NPC salaries, and collects taxes and hospital fees.

---

### `/state balance`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display the current state account balance. |
| **Related System** | Economy System, State Treasury |

**Example:**
```
/state balance
```

---

### `/state deposit <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Deposit money into the state account. Logged as "Admin-Einzahlung" (Admin deposit). |
| **Arguments** | `<amount>` -- Amount to deposit in whole euros (minimum 1). |
| **Related System** | Economy System, State Treasury |

**Example:**
```
/state deposit 100000
```

---

### `/state withdraw <amount>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Withdraw money from the state account. Fails if the state account has insufficient funds. Logged as "Admin-Abhebung" (Admin withdrawal). |
| **Arguments** | `<amount>` -- Amount to withdraw in whole euros (minimum 1). |
| **Related System** | Economy System, State Treasury |

**Example:**
```
/state withdraw 5000
```

---

# 10. Utility Commands

**Base command:** `/utility`
**Aliases:** `/strom`, `/wasser`
**Description:** Track electricity (Strom) and water (Wasser) consumption for plots. Includes per-plot breakdowns, server-wide statistics, and top consumer rankings.

---

### `/utility`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Show electricity and water consumption for the plot you are currently standing on. Displays current usage, 7-day averages, and consumer block count. |
| **Aliases** | `/strom`, `/wasser` (identical behavior). |
| **Related System** | Utility System, Plot System |

**Example:**
```
/utility
/strom
/wasser
```

---

### `/utility <plotId>`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Show electricity and water consumption for a specific plot by ID. |
| **Arguments** | `<plotId>` -- Plot ID to query. |
| **Related System** | Utility System, Plot System |

**Example:**
```
/utility Downtown_Shop
```

---

### `/utility top`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Show the top 10 plots by utility consumption, displaying 7-day average electricity and water usage for each. |
| **Related System** | Utility System |

**Example:**
```
/utility top
```

---

### `/utility stats`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Show server-wide utility statistics summary. |
| **Related System** | Utility System |

**Example:**
```
/utility stats
```

---

### `/utility breakdown <plotId>`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Show a detailed category-by-category breakdown of electricity and water consumption for a specific plot (e.g., grow lights, machines, irrigation). |
| **Arguments** | `<plotId>` -- Plot ID to query. |
| **Related System** | Utility System |

**Example:**
```
/utility breakdown Downtown_Shop
```

---

### `/utility scan`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Scan the plot at your current position for utility-consuming blocks (grow lights, machines, etc.) and update the consumption database. Use after adding or removing utility-consuming blocks. |
| **Related System** | Utility System, Plot System |

**Example:**
```
/utility scan
```

---

# 11. Market Commands

**Base command:** `/market`
**Description:** Dynamic market system with supply-and-demand-based pricing, trends, and statistics.

---

### `/market prices`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Display current market prices for all tracked items, including item name, current price, price trend indicator (rising/falling/stable), and base price. |
| **Related System** | Market System |

**Example:**
```
/market prices
```

---

### `/market trends`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Show the top 5 items with rising prices and the top 5 items with falling prices, including percentage change and current price. |
| **Related System** | Market System |

**Example:**
```
/market trends
```

---

### `/market stats`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Display market statistics including total registered items, counts of rising/falling/stable prices, average price, average price multiplier, and total price updates. |
| **Related System** | Market System |

**Example:**
```
/market stats
```

---

### `/market top`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Show the top 10 most expensive items on the market, with rank, item name, current price, and price multiplier. |
| **Related System** | Market System |

**Example:**
```
/market top
```

---

# 12. Gang Commands

**Base command:** `/gang`
**Description:** Player faction system with creation, membership management, leveling, perks, and admin tools.

---

## Gang Management

### `/gang create <name> <tag> [color]`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Create a new gang. You become the Boss. The tag is a short identifier displayed in chat. Color is optional and defaults to WHITE. |
| **Arguments** | `<name>` -- Gang name (unique, quoted if containing spaces). `<tag>` -- Short tag (unique, e.g., `[MSK]`). `[color]` -- Chat formatting color name (e.g., `RED`, `GOLD`, `AQUA`). |
| **Restrictions** | You cannot create a gang if you are already in one. Name and tag must be unique. |
| **Related System** | Gang System |

**Example:**
```
/gang create "Street Kings" SKG GOLD
```

---

### `/gang invite <player>`

| | |
|---|---|
| **Permission** | Player (Gang member with invite permission) |
| **Description** | Invite a player to your gang. The invitation expires after 5 minutes. The target player must use `/gang accept` to join. |
| **Arguments** | `<player>` -- Target player (must be online). |
| **Related System** | Gang System |

**Example:**
```
/gang invite Alex
```

---

### `/gang accept`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Accept a pending gang invitation. You must have received an invitation within the last 5 minutes. |
| **Related System** | Gang System |

**Example:**
```
/gang accept
```

---

### `/gang leave`

| | |
|---|---|
| **Permission** | Player (Gang member) |
| **Description** | Leave your current gang. The Boss cannot leave -- they must either transfer leadership via `/gang promote` or disband with `/gang disband`. |
| **Related System** | Gang System |

**Example:**
```
/gang leave
```

---

### `/gang kick <player>`

| | |
|---|---|
| **Permission** | Player (Gang member with kick permission) |
| **Description** | Remove a player from your gang. The kicked player is notified. |
| **Arguments** | `<player>` -- Target player (must be online). |
| **Related System** | Gang System |

**Example:**
```
/gang kick Alex
```

---

### `/gang promote <player> <rank>`

| | |
|---|---|
| **Permission** | Player (Gang Boss or Underboss) |
| **Description** | Change a member's rank. Valid ranks: `RECRUIT`, `MEMBER`, `UNDERBOSS`, `BOSS`. Promoting someone to Boss transfers leadership. |
| **Arguments** | `<player>` -- Target player (must be online). `<rank>` -- New rank name (case-insensitive). |
| **Related System** | Gang System |

**Example:**
```
/gang promote Alex UNDERBOSS
```

---

### `/gang info`

| | |
|---|---|
| **Permission** | Player (Gang member) |
| **Description** | Display detailed information about your gang, including name, level, XP, member count, territory count, perk usage, reputation, and a full member roster with ranks and XP contributions. |
| **Related System** | Gang System |

**Example:**
```
/gang info
```

---

### `/gang list`

| | |
|---|---|
| **Permission** | Player |
| **Description** | List all gangs on the server with their tags, names, levels, and member counts. |
| **Related System** | Gang System |

**Example:**
```
/gang list
```

---

### `/gang disband`

| | |
|---|---|
| **Permission** | Player (Gang Boss only) |
| **Description** | Permanently dissolve your gang. All members are removed and all gang data is deleted. This action cannot be undone. |
| **Related System** | Gang System |

**Example:**
```
/gang disband
```

---

### `/gang perk <perkname>`

| | |
|---|---|
| **Permission** | Player (Gang Boss only) |
| **Description** | Unlock a gang perk using available perk points. Perk points are earned through gang leveling. |
| **Arguments** | `<perkname>` -- Perk name (case-insensitive). |
| **Related System** | Gang System |

**Example:**
```
/gang perk EXTRA_TERRITORY
```

---

## Gang Admin Commands

### `/gang admin setlevel <gangname> <level>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set a gang's level directly. |
| **Arguments** | `<gangname>` -- Gang name (case-insensitive match). `<level>` -- New level (1-30). |
| **Related System** | Gang System |

**Example:**
```
/gang admin setlevel "Street Kings" 10
```

---

### `/gang admin addxp <gangname> <xp>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Add XP to a gang. May trigger level-ups. |
| **Arguments** | `<gangname>` -- Gang name (case-insensitive match). `<xp>` -- XP to add (1-100,000). |
| **Related System** | Gang System |

**Example:**
```
/gang admin addxp "Street Kings" 5000
```

---

### `/gang admin info <gangname>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show detailed admin view of a gang, including internal ID, max level, XP totals, balance, weekly fee, perk points, and reputation. |
| **Arguments** | `<gangname>` -- Gang name (case-insensitive match). |
| **Related System** | Gang System |

**Example:**
```
/gang admin info "Street Kings"
```

---

### `/gang task editor`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Open the Gang Scenario Editor GUI. Sends scenario data, NPC names, plot information, and lock data to the client for editing gang scenarios. |
| **Related System** | Gang System, Scenario System |

**Example:**
```
/gang task editor
```

---

# 13. Lock Commands

**Base command:** `/lock`
**Description:** Door and container locking system with combination locks, authorized player management, and lock removal.

---

### `/lock code <lockId> <code>`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Enter a 4-digit code to unlock a combination lock. If the code is correct, the door is opened. Only works on lock types that support codes. |
| **Arguments** | `<lockId>` -- Lock ID. `<code>` -- 4-digit code. |
| **Related System** | Lock System |

**Example:**
```
/lock code abc123 1234
```

---

### `/lock setcode <lockId> <code>`

| | |
|---|---|
| **Permission** | Player (Lock Owner only) |
| **Description** | Set or change the code on a combination lock. Only the lock owner can change the code. |
| **Arguments** | `<lockId>` -- Lock ID. `<code>` -- New 4-digit numeric code. |
| **Related System** | Lock System |

**Example:**
```
/lock setcode abc123 5678
```

---

### `/lock authorize <lockId> <player>`

| | |
|---|---|
| **Permission** | Player (Lock Owner only) |
| **Description** | Authorize another player to create keys for the lock. Only the lock owner can authorize players. |
| **Arguments** | `<lockId>` -- Lock ID. `<player>` -- Player name (must be online). |
| **Related System** | Lock System |

**Example:**
```
/lock authorize abc123 Alex
```

---

### `/lock info <lockId>`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Display information about a lock, including type, owner, position, code (visible only to the owner), and number of authorized players. |
| **Arguments** | `<lockId>` -- Lock ID. |
| **Related System** | Lock System |

**Example:**
```
/lock info abc123
```

---

### `/lock remove <lockId>`

| | |
|---|---|
| **Permission** | Player (Lock Owner only) |
| **Description** | Remove a lock. Only the lock owner can remove it. Use `/lock admin remove` for admin override. |
| **Arguments** | `<lockId>` -- Lock ID. |
| **Related System** | Lock System |

**Example:**
```
/lock remove abc123
```

---

### `/lock list`

| | |
|---|---|
| **Permission** | Player |
| **Description** | List all locks you own, showing lock ID, type, and position for each. |
| **Related System** | Lock System |

**Example:**
```
/lock list
```

---

### `/lock admin remove <lockId>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Force-remove any lock regardless of ownership. Use for maintenance and dispute resolution. |
| **Arguments** | `<lockId>` -- Lock ID. |
| **Related System** | Lock System |

**Example:**
```
/lock admin remove abc123
```

---

# 14. Territory and Map Commands

**Base command:** `/map`
**Description:** Territory visualization and management through the map editor.

---

### `/map edit`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Open the Territory Map Editor GUI. Synchronizes all territory data to the client before opening the editor screen. Used for defining and managing gang territory boundaries on the server map. |
| **Related System** | Territory System, Gang System |

**Example:**
```
/map edit
```

---

### `/map info`

| | |
|---|---|
| **Permission** | Player |
| **Description** | Display territory statistics from the Territory Manager, including territory counts and ownership summaries. |
| **Related System** | Territory System |

**Example:**
```
/map info
```

---

# 15. Health and Diagnostics Commands

**Base command:** `/health`
**Description:** System health monitoring and diagnostics for server administrators. Monitors **38 subsystems** across 7 categories.

---

### `/health`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display the overall health report for all 38 ScheduleMC subsystems, grouped by 7 categories (Kern, Finanz, NPC/Crime, NPC Life, Spieler, Welt, Infrastruktur). Shows HEALTHY/DEGRADED/UNHEALTHY status for each system. |
| **Related System** | Health Check System |

**Example:**
```
/health
```

---

### `/health economy`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show detailed economy system diagnostics, including health status, error information (if unhealthy), and backup availability with age and filename of the latest backup. |
| **Related System** | Health Check System, Economy System |

**Example:**
```
/health economy
```

---

### `/health plot`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show detailed plot system diagnostics, including health status, cache statistics, error information (if unhealthy), and backup availability. |
| **Related System** | Health Check System, Plot System |

**Example:**
```
/health plot
```

---

### `/health wallet`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show wallet subsystem health status and details. |
| **Related System** | Health Check System, Economy System |

---

### `/health loan`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show loan system health, including initialization status and persistence state. |
| **Related System** | Health Check System, Economy System |

---

### `/health creditloan`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show credit loan system health and initialization status. |
| **Related System** | Health Check System, Economy System |

---

### `/health creditscore`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show credit score system health and initialization status. |
| **Related System** | Health Check System, Economy System |

---

### `/health savings`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show savings account system health and initialization status. |
| **Related System** | Health Check System, Economy System |

---

### `/health tax`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show tax system health and initialization status. |
| **Related System** | Health Check System, Economy System |

---

### `/health overdraft`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show overdraft system health and initialization status. |
| **Related System** | Health Check System, Economy System |

---

### `/health recurring`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show recurring payment system health and initialization status. |
| **Related System** | Health Check System, Economy System |

---

### `/health shopaccount`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show shop account system health, including registered shop count. |
| **Related System** | Health Check System, Economy System |

---

### `/health interest`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show interest system health and initialization status. |
| **Related System** | Health Check System, Economy System |

---

### `/health crime`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show crime system health status and details. |
| **Related System** | Health Check System, Crime System |

---

### `/health bounty`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show bounty system health with statistics. |
| **Related System** | Health Check System, Crime System |

---

### `/health npc`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show NPC registry health and initialization status. |
| **Related System** | Health Check System, NPC System |

---

### `/health prison`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show prison system health and current prisoner count. |
| **Related System** | Health Check System, Crime System |

---

### `/health witness`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show witness/wanted system health and persistence status. |
| **Related System** | Health Check System, Crime System |

---

### `/health dialogue`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show NPC dialogue tree system health. |
| **Related System** | Health Check System, NPC Life System |

---

### `/health quest`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show quest system health and persistence status. |
| **Related System** | Health Check System, NPC Life System |

---

### `/health companion`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show companion NPC system health. |
| **Related System** | Health Check System, NPC Life System |

---

### `/health faction`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show faction reputation system health. |
| **Related System** | Health Check System, NPC Life System |

---

### `/health npcinteraction`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show NPC-to-NPC interaction system health. |
| **Related System** | Health Check System, NPC Life System |

---

### `/health relationship`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show NPC-player relationship system health, including total relationship and player counts. |
| **Related System** | Health Check System, NPC Life System |

---

### `/health worldevent`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show world event system health, including active event count. |
| **Related System** | Health Check System, NPC Life System |

---

### `/health dynamicprice`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show dynamic NPC price system health, including current market condition. |
| **Related System** | Health Check System, NPC Life System |

---

### `/health gang`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show gang system health, including active gang count. |
| **Related System** | Health Check System, Gang System |

---

### `/health territory`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show territory system health, including territory count. |
| **Related System** | Health Check System, Territory System |

---

### `/health achievement`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show achievement system health and initialization status. |
| **Related System** | Health Check System, Achievement System |

---

### `/health daily`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show daily reward system health status. |
| **Related System** | Health Check System, Daily Reward System |

---

### `/health message`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show messaging system health status. |
| **Related System** | Health Check System, Messaging System |

---

### `/health gangmission`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show gang mission system health and initialization status. |
| **Related System** | Health Check System, Gang System |

---

### `/health scenario`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show scenario system health, including active/total scenario counts. |
| **Related System** | Health Check System, Gang System |

---

### `/health lock`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show lock system health, including registered lock count. |
| **Related System** | Health Check System, Lock System |

---

### `/health market`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show dynamic market system health and enabled/disabled status. |
| **Related System** | Health Check System, Market System |

---

### `/health warehouse`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show warehouse system health, including registered warehouse count. |
| **Related System** | Health Check System, Warehouse System |

---

### `/health towing`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show towing yard system health status. |
| **Related System** | Health Check System, Towing System |

---

### `/health antiexploit`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show anti-exploit detection system health and initialization status. |
| **Related System** | Health Check System, Economy System |

---

### `/health threadpool`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show thread pool infrastructure health, including IO queue size and computation pool activity. Reports DEGRADED if IO queue exceeds 100. |
| **Related System** | Health Check System, Infrastructure |

---

### `/health backups`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Show a comprehensive backup overview for all data files (economy and plots). Lists the 3 most recent backups for each file with age and size, plus total backup counts and auto-backup configuration. |
| **Related System** | Health Check System, Backup System |

**Example:**
```
/health backups
```

---

### `/health log`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Write a full health check report to the server console log and display a quick status summary in chat. Useful for automated monitoring and post-incident analysis. |
| **Related System** | Health Check System |

**Example:**
```
/health log
```

---

# 16. Admin Commands

**Base command:** `/admin`, `/admintools`
**Description:** Player level management and special admin tools.

---

## Player Level Commands

### `/admin setlevel <player> <level>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Set a player's Producer Level directly. The player is notified of the change. |
| **Arguments** | `<player>` -- Target player (must be online). `<level>` -- New level (0-30). |
| **Related System** | Level System |

**Example:**
```
/admin setlevel Steve 15
```

---

### `/admin addxp <player> <xp>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Award XP to a player's Producer Level. May trigger level-ups. XP source is logged as `ADMIN_GRANT`. |
| **Arguments** | `<player>` -- Target player (must be online). `<xp>` -- XP amount to award (1-100,000). |
| **Related System** | Level System |

**Example:**
```
/admin addxp Steve 5000
```

---

### `/admin getlevel <player>`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display a player's current Producer Level, total XP, and XP required for the next level. |
| **Arguments** | `<player>` -- Target player (must be online). |
| **Related System** | Level System |

**Example:**
```
/admin getlevel Steve
```

---

## Admin Tools

### `/admintools remover`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Receive the Entity Remover tool. This special item allows admins to remove entities (NPCs, vehicles, etc.) by right-clicking on them. If inventory is full, the item is dropped at your feet. |
| **Related System** | Admin Tools |

**Example:**
```
/admintools remover
```

---

### `/admintools help`

| | |
|---|---|
| **Permission** | Admin (OP 2) |
| **Description** | Display help information for all available admin tools. |
| **Related System** | Admin Tools |

**Example:**
```
/admintools help
```

---

# Permission Level Reference

## Player Commands (No special permission required)

| Category | Commands |
|----------|----------|
| Plot Apartments | `/plot apartment wand`, `create`, `delete`, `list`, `info`, `rent`, `leave`, `setrent`, `evict` |
| Bounty | `/bounty list`, `place`, `info`, `history` |
| Prison (Player) | `/bail`, `/jailtime` |
| Utility | `/utility`, `/strom`, `/wasser`, `/utility top`, `stats`, `breakdown`, `<plotId>` |
| Market | `/market prices`, `trends`, `stats`, `top` |
| Gang | `/gang create`, `invite`, `accept`, `leave`, `kick`, `promote`, `info`, `list`, `disband`, `perk` |
| Lock | `/lock code`, `setcode`, `authorize`, `info`, `remove`, `list` |
| Territory | `/map info` |

## Admin Commands (OP Level 2 required)

| Category | Commands |
|----------|----------|
| Plot Admin | `/plot create`, `setowner`, `remove`, `settype`, `reindex`, `debug`, `warehouse set/clear/info` |
| Economy Admin | `/money set`, `give`, `take`, `history` |
| NPC (all) | `/npc <name> info`, `movement`, `speed`, `schedule`, `leisure`, `inventory`, `wallet`, `warehouse` |
| Warehouse (all) | `/warehouse info`, `add`, `remove`, `clear`, `setshop`, `deliver`, `reset` |
| Prison Admin | `/prison create`, `addcell`, `removecell`, `list`, `cells`, `inmates`, `status`, `release` |
| Hospital (all) | `/hospital setspawn`, `setfee`, `info` |
| State (all) | `/state balance`, `deposit`, `withdraw` |
| Utility Admin | `/utility scan` |
| Gang Admin | `/gang admin setlevel`, `addxp`, `info`, `/gang task editor` |
| Lock Admin | `/lock admin remove` |
| Territory Admin | `/map edit` |
| Health (all 40) | `/health`, `economy`, `plot`, `wallet`, `loan`, `creditloan`, `creditscore`, `savings`, `tax`, `overdraft`, `recurring`, `shopaccount`, `interest`, `crime`, `bounty`, `npc`, `prison`, `witness`, `dialogue`, `quest`, `companion`, `faction`, `npcinteraction`, `relationship`, `worldevent`, `dynamicprice`, `gang`, `territory`, `achievement`, `daily`, `message`, `gangmission`, `scenario`, `lock`, `market`, `warehouse`, `towing`, `antiexploit`, `threadpool`, `backups`, `log` |
| Player Levels | `/admin setlevel`, `addxp`, `getlevel` |
| Admin Tools | `/admintools remover`, `help` |

---

# Common Workflows

## Server Setup (Admin)

```
# 1. Create the spawn area
/plot create public Spawn

# 2. Configure the hospital
/hospital setspawn
/hospital setfee 500

# 3. Fund the state treasury
/state deposit 1000000

# 4. Create a prison
/plot create prison State_Prison
/prison create State_Prison
/prison addcell 1 100 50 10 110 55 15 3
/prison addcell 2 112 50 10 122 55 15 1

# 5. Create shop plots and set up merchants
/plot create shop Downtown_Bakery
/npc Hans schedule workstart 0700
/npc Hans schedule workend 1800
/npc Hans schedule home 2200
/npc Hans wallet set 10000
/npc Hans inventory give 0 minecraft:bread
/npc Hans warehouse set

# 6. Stock the warehouse
/warehouse add minecraft:bread 1000
/warehouse setshop Downtown_Bakery
```

## Setting Up an NPC Merchant (Admin)

```
# 1. Configure the schedule
/npc Hans schedule workstart 0800
/npc Hans schedule workend 1700
/npc Hans schedule home 2200

# 2. Add leisure locations (stand at each spot)
/npc Hans leisure add
/npc Hans leisure add

# 3. Set movement and speed
/npc Hans movement true
/npc Hans speed 0.3

# 4. Fund the wallet and add inventory
/npc Hans wallet set 10000
/npc Hans inventory give 0 minecraft:diamond
/npc Hans inventory give 1 minecraft:emerald

# 5. Link to warehouse for unlimited stock
/npc Hans warehouse set

# 6. Verify configuration
/npc Hans info
```

## Creating an Apartment Building (Plot Owner)

```
# 1. Get the apartment selection tool
/plot apartment wand

# 2. Select each apartment area and create
# (Left-click corner 1, right-click corner 2, then:)
/plot apartment create Unit_1A 1500
/plot apartment create Unit_1B 1500
/plot apartment create Penthouse 3000

# 3. Verify
/plot apartment list
/plot apartment info Unit_1A
```

## Daily Maintenance (Admin)

```
# Check all 23 systems at once
/health

# Check specific subsystems
/health economy
/health gang
/health market
/health crime

# Review backups
/health backups

# Check prison inmates
/prison inmates

# Review market trends
/market trends

# Check top utility consumers
/utility top

# Log health report to console
/health log
```

## Managing a Gang (Player)

```
# Create a gang
/gang create "The Enforcers" ENF RED

# Invite members
/gang invite Alex
/gang invite Bob

# Promote a trusted member
/gang promote Alex UNDERBOSS

# Unlock perks as you level up
/gang perk EXTRA_TERRITORY

# Check gang status
/gang info

# View all gangs on server
/gang list
```

---

<div align="center">

**ScheduleMC v3.6.0-beta** -- Minecraft 1.20.1 -- Forge 47.4.0

[Back to Wiki Home](Home.md) -- [Getting Started](Getting-Started.md) -- [Items](Items.md) -- [Blocks](Blocks.md) -- [FAQ](FAQ.md)

</div>
