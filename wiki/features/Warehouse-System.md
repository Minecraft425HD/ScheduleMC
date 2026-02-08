# Warehouse System

<div align="center">

**Mass Storage & Automated Delivery for NPC Shops**

32 slots x 1,024 items each = 32,768 total capacity

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Warehouse Structure](#warehouse-structure)
3. [Creating Warehouses](#creating-warehouses)
4. [Inventory Management](#inventory-management)
5. [Shop Linking](#shop-linking)
6. [NPC Merchant Integration](#npc-merchant-integration)
7. [Delivery System](#delivery-system)
8. [Revenue Tracking](#revenue-tracking)
9. [Commands](#commands)
10. [Developer API](#developer-api)
11. [Best Practices](#best-practices)
12. [Troubleshooting](#troubleshooting)

---

## Overview

The Warehouse System provides centralized inventory management for NPC shops with automatic delivery, NPC merchant integration, and revenue tracking. Warehouses give shops effectively unlimited stock capacity.

### Key Features

- **32,768 Total Capacity** - 32 slots x 1,024 items per slot
- **Automatic Deliveries** - Restocking every 3 days
- **Shop Linking** - Connect warehouse to shop plots
- **NPC Merchant Integration** - NPCs sell directly from warehouse inventory
- **State Payment** - Delivery costs paid by state treasury
- **Revenue Tracking** - Monitor sales and income per warehouse
- **7 Commands** - Full warehouse management via `/warehouse`

---

## Warehouse Structure

### Capacity Specifications

```
Single Warehouse:
  Slots:           32
  Items per Slot:  1,024 (max stack x 16)
  Total Capacity:  32,768 items
  Item Types:      32 different types

Example Inventory:
  Slot 0:  Diamond x 1,024
  Slot 1:  Gold Ingot x 1,024
  Slot 2:  Emerald x 512
  Slot 3:  Iron Ingot x 1,024
  ...
  Slot 31: Coal x 256
```

### Warehouse Block

**Block:** `schedulemc:warehouse`
**Placement:** Must be placed inside a plot
**GUI:** Right-click to open inventory

**Inventory Screen:**
```
+-------------------------------+
|     WAREHOUSE INVENTORY       |
+-------------------------------+
| [Diamond x1024] [Gold x1024] |
| [Emerald x512]  [Iron x1024] |
| [Empty]         [Empty]      |
| ...                           |
|                               |
| Linked Shop: Electronics      |
| Next Delivery: 2 days         |
+-------------------------------+
```

---

## Creating Warehouses

### Placement Requirements

- Must be placed inside a plot
- Plot must be SHOP type
- One warehouse per shop plot

### Initial Setup Example

```bash
# Step 1: Create shop plot (admin)
/plot wand
# Select area
/plot create shop "Electronics_Store"

# Step 2: Place warehouse block in the plot (in-game)

# Step 3: Link warehouse to shop
/warehouse setshop Electronics_Store

# Step 4: Stock initial inventory
/warehouse add minecraft:diamond 512
/warehouse add minecraft:gold_ingot 1024
/warehouse add minecraft:emerald 256
```

---

## Inventory Management

### Adding Items

```bash
/warehouse add <item> <amount>
```

**Examples:**
```bash
/warehouse add minecraft:diamond 64
/warehouse add minecraft:gold_ingot 128
/warehouse add schedulemc:virginia_cigar 256
```

**Limits:**
- Max per slot: 1,024 items
- Max slots: 32 different item types
- Total capacity: 32,768 items

**Result:**
```
Items Added to Warehouse!

Item: Diamond
Amount: +64
New Total: 576
Slot: 0/32

Warehouse: Electronics_Store
Total Items: 2,840 / 32,768
```

### Removing Items

```bash
/warehouse remove <item> <amount>
```

**Notes:**
- Items are deleted (not returned to the player)
- Cannot remove more than what is available
- Use for inventory adjustments and cleanup

### Viewing Inventory

```bash
/warehouse info
```

**Requirements:** Must be looking at the warehouse block.

**Output:**
```
WAREHOUSE INFO

Location: 100, 64, 200 (world)
Plot: Electronics_Store
Linked Shop: Electronics_Store
Linked NPCs: 2 (Shop_Owner, Assistant)

Inventory (12/32 slots used):
  Slot 0:  Diamond x 544
  Slot 1:  Gold Ingot x 1,024 (FULL)
  Slot 2:  Emerald x 256
  Slot 3:  Iron Ingot x 892
  ...

Total Items: 5,124 / 32,768 (15.6%)

Last Delivery: 2024-01-15 10:00
Next Delivery: 2024-01-18 10:00 (2 days)
```

### Clearing Inventory

```bash
/warehouse clear
```

**Warning:** Deletes ALL items in the warehouse. Cannot be undone. Requires confirmation:

```bash
/warehouse clear confirm    # Confirms the clear operation
```

---

## Shop Linking

### Linking Warehouse to Shop

```bash
/warehouse setshop <shopId>
```

**Requirements:**
- Warehouse must be in a shop plot
- Shop plot must exist
- Must be looking at the warehouse block

**Result:**
```
Warehouse Linked to Shop!

Warehouse Location: 100, 64, 200
Shop Plot: Electronics_Store
Shop Type: SHOP

NPCs can now sell from this warehouse.
Use /npc <name> warehouse set to link NPCs.
```

### Unlinking Warehouse

```bash
/warehouse unlink
```

**Effect:**
- Removes the shop connection
- NPCs can no longer sell from this warehouse
- Inventory remains intact

---

## NPC Merchant Integration

### Linking NPC to Warehouse

```bash
# 1. Assign NPC to shop
/npc Shop_Owner setshop Electronics_Store

# 2. Look at warehouse block
# 3. Link NPC to warehouse
/npc Shop_Owner warehouse set
```

**Result:**
```
NPC Linked to Warehouse!

NPC: Shop_Owner
Warehouse: Electronics_Store (100, 64, 200)
Shop: Electronics_Store

Shop_Owner can now sell unlimited items from warehouse.
```

### NPC Selling Behavior

**With Warehouse:**
```
Player buys Diamond from NPC:
  NPC Inventory: Diamond x 16
  Warehouse: Diamond x 544

  NPC sells 1 Diamond from inventory
  NPC auto-restocks +1 Diamond from warehouse
  Warehouse: 543 remaining

  Result: Unlimited stock as long as warehouse has items
```

**Without Warehouse:**
```
Player buys Diamond from NPC:
  NPC Inventory: Diamond x 16
  No Warehouse linked

  NPC sells 1 Diamond
  NPC Inventory: 15 remaining

  Result: Limited to NPC inventory (max 9 slots x 64)
```

### Checking NPC Warehouse Link

```bash
/npc <name> warehouse info
```

**Output:**
```
NPC WAREHOUSE INFO

NPC: Shop_Owner
Warehouse: Electronics_Store
Location: 100, 64, 200

Linked: Yes
Status: Active
Items Available: 12 types

Shop: Electronics_Store
Plot: shop_electronics_1
```

### Unlinking NPC from Warehouse

```bash
/npc <name> warehouse clear
```

**Effect:**
- NPC can only sell from personal inventory (9 slots)
- Warehouse remains linked to shop
- Other NPCs are unaffected

---

## Delivery System

### Automatic Delivery

Warehouses receive automatic deliveries to replenish stock.

**Configuration:**
- **Frequency:** Every 3 days (real-time)
- **Payment:** State treasury pays delivery cost
- **Restocking:** Configured items automatically refilled to capacity

### Manual Delivery

```bash
/warehouse deliver
```

**Requirements:**
- Admin permission (Level 2)
- State account must have sufficient funds

**Cost Calculation:**
```
Base Cost:              1,000
Item Count Multiplier:  +10 per item type
Distance Multiplier:    +1 per 100 blocks from spawn

Example:
  Items: 12 types   -> 12 x 10 = 120
  Distance: 500 blocks -> 500 / 100 = 5
  Total Cost: 1,000 + 120 + 5 = 1,125
```

**Result:**
```
Delivery Completed!

Items Delivered:
  Diamond: +256 (800 total)
  Gold Ingot: +0 (1,024 FULL)
  Emerald: +512 (768 total)
  ... (all configured items)

Delivery Cost: 1,125
Paid by: State Treasury
State Remaining: 244,475

Next Auto-Delivery: 2024-01-21
```

### State Payment for Delivery

All delivery costs are paid from the **State Treasury**, not from individual players or NPC wallets. This ensures warehouses stay stocked as long as the state has funds.

```
Delivery every 3 days = ~120 deliveries per year
Cost per delivery: ~1,000
Annual cost: ~120,000

Ensure state treasury has:
  Minimum: 50,000 (buffer for 50 deliveries)
  Recommended: 200,000 (seasonal buffer)
```

### Delivery Timer Reset

```bash
/warehouse reset
```

Resets the delivery timer, useful for fixing schedule issues or forcing the next delivery cycle.

---

## Revenue Tracking

The warehouse system tracks all revenue generated from sales through linked NPCs.

**Tracked metrics:**
- Total items sold from warehouse
- Total revenue generated
- Revenue per item type
- Revenue per linked NPC
- Daily, weekly, and monthly summaries

Revenue data is available through the `/warehouse info` command and the `IWarehouseAPI` for programmatic access.

---

## Commands

The `/warehouse` command provides **7 subcommands** for warehouse management:

```bash
/warehouse info                    # View warehouse details and inventory
/warehouse add <item> <amount>     # Add items to warehouse
/warehouse remove <item> <amount>  # Remove items from warehouse
/warehouse clear                   # Clear all items (requires confirmation)
/warehouse setshop <shopId>        # Link warehouse to shop plot
/warehouse unlink                  # Remove shop link
/warehouse deliver                 # Trigger manual delivery (admin)
/warehouse reset                   # Reset delivery timer (admin)
```

**Note:** Most commands require you to be looking at the warehouse block.

---

## Developer API

### IWarehouseAPI Interface

External mods can access the warehouse system through the `IWarehouseAPI` interface.

**Access:**
```java
IWarehouseAPI warehouseAPI = ScheduleMCAPI.getWarehouseAPI();
```

### Core Methods (v3.0.0+)

| Method | Description |
|--------|-------------|
| `hasWarehouse(BlockPos)` | Check if a warehouse exists at position |
| `addItemToWarehouse(BlockPos, Item, int)` | Add items to a warehouse |
| `removeItemFromWarehouse(BlockPos, Item, int)` | Remove items from a warehouse |
| `getItemStock(BlockPos, Item)` | Get current stock of an item |
| `getItemCapacity(BlockPos, Item)` | Get max capacity for an item |
| `getAllSlots(BlockPos)` | Get list of all WarehouseSlot objects |
| `addSeller(BlockPos, UUID)` | Add a seller (NPC) to the warehouse |
| `removeSeller(BlockPos, UUID)` | Remove a seller from the warehouse |
| `isSeller(BlockPos, UUID)` | Check if a UUID is a registered seller |

### Extended Methods (v3.2.0+)

| Method | Description |
|--------|-------------|
| `getAllWarehousePositions()` | Get set of all warehouse BlockPos values |
| `getTotalItemCount(BlockPos)` | Total items stored in a warehouse |
| `getUsagePercentage(BlockPos)` | Capacity usage as percentage (0.0 - 100.0) |
| `getAllSellers(BlockPos)` | Get set of all seller UUIDs for a warehouse |
| `linkToShop(BlockPos, String)` | Link warehouse to a shop plot by ID |
| `triggerDelivery(BlockPos)` | Trigger an immediate delivery |
| `clearWarehouse(BlockPos)` | Clear all items from a warehouse |

### Example Usage

```java
IWarehouseAPI warehouseAPI = ScheduleMCAPI.getWarehouseAPI();

// Check if warehouse exists
BlockPos pos = new BlockPos(100, 64, 200);
if (warehouseAPI.hasWarehouse(pos)) {

    // Add items
    warehouseAPI.addItemToWarehouse(pos, Items.DIAMOND, 256);

    // Check stock
    int diamonds = warehouseAPI.getItemStock(pos, Items.DIAMOND);

    // Get usage percentage
    double usage = warehouseAPI.getUsagePercentage(pos);

    // Get total items
    int total = warehouseAPI.getTotalItemCount(pos);

    // Link to shop
    warehouseAPI.linkToShop(pos, "Electronics_Store");

    // Add NPC as seller
    warehouseAPI.addSeller(pos, npcUUID);

    // Trigger delivery
    warehouseAPI.triggerDelivery(pos);
}

// Get all warehouses on the server
Set<BlockPos> warehouses = warehouseAPI.getAllWarehousePositions();
```

**Thread Safety:** All methods are thread-safe through synchronized operations.

---

## Best Practices

### For Admins

#### 1. Stock Essential Items

```bash
# Basic resources
/warehouse add minecraft:diamond 512
/warehouse add minecraft:gold_ingot 1024
/warehouse add minecraft:emerald 256
/warehouse add minecraft:iron_ingot 1024

# Production items
/warehouse add schedulemc:virginia_cigar 500
/warehouse add schedulemc:cannabis_bud 300

# Tools
/warehouse add minecraft:diamond_pickaxe 64
/warehouse add minecraft:diamond_sword 64
```

#### 2. Monitor Inventory Levels

```bash
# Regular checks
/warehouse info

# Low stock alert thresholds:
  < 100 items:  Restock soon
  < 50 items:   Critical
  0 items:      Out of stock (NPCs cannot sell)
```

#### 3. Optimize Delivery Timing

```
Delivery every 3 days = 120 deliveries/year
Cost per delivery: ~1,000
Annual cost: ~120,000

Ensure state treasury has:
  Minimum: 50,000 (buffer for 50 deliveries)
  Recommended: 200,000 (seasonal buffer)
```

#### 4. Balance Item Distribution

```
High-Demand Items (stock 800-1,024):
- Diamonds, Gold, Emeralds
- Popular production items

Medium-Demand Items (stock 400-600):
- Iron, Redstone
- Common items

Low-Demand Items (stock 100-200):
- Specialty items
- Rare products
```

### For Server Performance

#### 1. Warehouse Limits

```
Recommended:
- Max warehouses per server: 20
- Max linked NPCs per warehouse: 5
- Keep inventory < 80% full for performance
```

#### 2. Delivery Optimization

```
Avoid simultaneous deliveries:
- Stagger warehouse creation dates
- Different delivery times reduce server load spikes
```

---

## Troubleshooting

### NPC Out of Stock

**Problem:** NPC says "I don't have that item"

**Causes:**
1. Item not in NPC inventory
2. Item not in warehouse
3. NPC not linked to warehouse
4. Warehouse not linked to shop

**Solutions:**
```bash
/npc Shop_Owner inventory       # Check NPC inventory
/npc Shop_Owner warehouse info  # Check warehouse link
/warehouse info                 # Check warehouse inventory
/warehouse add minecraft:diamond 512  # Add items if missing
/npc Shop_Owner warehouse set   # Link NPC if needed
```

### Delivery Failed

**Problem:** Automatic delivery not working

**Causes:**
1. State account has insufficient funds
2. Warehouse not linked to shop
3. Delivery timer not set properly

**Solutions:**
```bash
/state balance          # Check state funds
/state deposit 50000    # Fund state if needed
/warehouse info         # Check warehouse status
/warehouse deliver      # Manually trigger delivery
/warehouse reset        # Reset timer if stuck
```

### Cannot Add Items

**Problem:** `/warehouse add` command fails

**Causes:**
1. Warehouse full (32 item types)
2. Slot full (1,024 items)
3. Not looking at warehouse block
4. Not in correct plot

**Solutions:**
```bash
/warehouse info                        # Check capacity
/warehouse remove <unused_item> <amt>  # Remove old items if full
# Stand close and look directly at warehouse block
```

### Warehouse Not Saving

**Problem:** Inventory resets after server restart

**Causes:**
1. Disk space full
2. Permission errors on data files
3. Corrupted data file

**Solutions:**
```bash
/health                 # Check system health
/health backups         # Check backup status
# Verify file: config/plotmod_warehouses.json
# Restart server - data should auto-recover from backup
```

---

<div align="center">

**Warehouse System - Complete Guide**

For related systems:
- [NPC System](NPC-System.md)
- [Economy System](Economy-System.md)
- [Plot System](Plot-System.md)
- [Market System](Market-System.md)

[Back to Wiki Home](../Home.md) | [All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
