# Warehouse System

<div align="center">

**Mass Storage & Automated Delivery for NPC Shops**

32 slots × 1,024 items each = 32,768 total capacity

[🏠 Back to Wiki Home](../Home.md) • [📋 Commands Reference](../Commands.md)

</div>

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Warehouse Structure](#warehouse-structure)
3. [Creating Warehouses](#creating-warehouses)
4. [Inventory Management](#inventory-management)
5. [Shop Integration](#shop-integration)
6. [Delivery System](#delivery-system)
7. [NPC Integration](#npc-integration)
8. [Best Practices](#best-practices)
9. [Troubleshooting](#troubleshooting)

---

## Overview

The Warehouse System provides centralized inventory management for NPC shops with automatic delivery and unlimited stock capabilities.

### Key Features

✅ **Massive Storage** - 32 slots × 1,024 items = 32,768 capacity
✅ **Auto-Delivery** - Restocking every 3 days
✅ **Shop Linking** - Connect to shop plots
✅ **NPC Integration** - NPCs sell from warehouse inventory
✅ **Unlimited Stock** - As long as warehouse has items
✅ **State Funding** - Delivery costs paid by state treasury
✅ **Multi-Item Support** - 32 different item types per warehouse

---

## Warehouse Structure

### Capacity Specifications

```
Single Warehouse:
├── Slots: 32
├── Items per Slot: 1,024 (max stack × 16)
├── Total Capacity: 32,768 items
└── Item Types: 32 different types

Example Inventory:
Slot 0:  Diamond × 1,024
Slot 1:  Gold Ingot × 1,024
Slot 2:  Emerald × 512
Slot 3:  Iron Ingot × 1,024
...
Slot 31: Coal × 256
```

---

### Warehouse Block

**Block:** `schedulemc:warehouse`
**Placement:** Must be in a plot
**GUI:** Right-click to open inventory

**Inventory Screen:**
```
╔═══════════════════════════════╗
║     WAREHOUSE INVENTORY       ║
╠═══════════════════════════════╣
║ [Diamond x1024] [Gold x1024]  ║
║ [Emerald x512]  [Iron x1024]  ║
║ [Empty]         [Empty]       ║
║ ...                           ║
║                               ║
║ Linked Shop: Electronics      ║
║ Next Delivery: 2 days         ║
╚═══════════════════════════════╝
```

---

## Creating Warehouses

### Placement

**Requirements:**
- Must be placed in a plot
- Plot must be SHOP type
- One warehouse per shop plot

**Steps:**
1. Create shop plot: `/plot create shop "MyShop"`
2. Place warehouse block in shop plot
3. Link to shop: `/warehouse setshop <shopId>`

---

### Initial Setup Example

```bash
# Step 1: Create shop plot (admin)
/plot wand
# Select area
/plot create shop "Electronics_Store"

# Step 2: Place warehouse block
# (Place block in-game)

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
- **Max per slot:** 1,024 items
- **Max slots:** 32 different items
- **Total capacity:** 32,768 items

**Result:**
```
✓ Items Added to Warehouse!

Item: Diamond
Amount: +64
New Total: 576
Slot: 0/32

Warehouse: Electronics_Store
Total Items: 2,840 / 32,768
```

---

### Removing Items

```bash
/warehouse remove <item> <amount>
```

**Examples:**
```bash
/warehouse remove minecraft:diamond 32
/warehouse remove minecraft:gold_ingot 64
```

**Notes:**
- Items are deleted (not given to player)
- Cannot remove more than available
- Use for inventory adjustments

**Result:**
```
✓ Items Removed from Warehouse!

Item: Diamond
Amount: -32
Remaining: 544
Slot: 0/32

Total Items: 2,808 / 32,768
```

---

### Viewing Inventory

```bash
/warehouse info
```

**Requirements:**
- Look at warehouse block
- Must be in plot with warehouse

**Output:**
```
═══ WAREHOUSE INFO ═══

Location: 100, 64, 200 (world)
Plot: Electronics_Store
Linked Shop: Electronics_Store
Linked NPCs: 2 (Shop_Owner, Assistant)

Inventory (12/32 slots used):
Slot 0:  Diamond × 544
Slot 1:  Gold Ingot × 1,024 (FULL)
Slot 2:  Emerald × 256
Slot 3:  Iron Ingot × 892
Slot 4:  Redstone × 1,024 (FULL)
Slot 5:  Lapis Lazuli × 320
Slot 6:  Virginia Cigar × 450
Slot 7:  Burley Cigar × 380
Slot 8:  Cannabis Bud × 128
Slot 9:  Cocaine × 64
Slot 10: LSD Sheet × 24
Slot 11: Heroin × 18

Total Items: 5,124 / 32,768 (15.6%)

Last Delivery: 2024-01-15 10:00
Next Delivery: 2024-01-18 10:00 (2 days)
```

---

### Clearing Inventory

```bash
/warehouse clear
```

**Warning:**
- ⚠️ Deletes ALL items in warehouse
- Cannot be undone
- Requires confirmation

**Confirmation:**
```
⚠️ WARNING: Clear Warehouse?

This will DELETE all items:
- Diamond × 544
- Gold Ingot × 1,024
- Emerald × 256
... (all 12 item types)

Total: 5,124 items will be lost!

Type /warehouse clear confirm to proceed
```

---

## Shop Integration

### Linking Warehouse to Shop

```bash
/warehouse setshop <shopId>
```

**Requirements:**
- Warehouse must be in a shop plot
- Shop plot must exist
- Must be looking at warehouse block

**Example:**
```bash
/warehouse setshop Electronics_Store
```

**Result:**
```
✓ Warehouse Linked to Shop!

Warehouse Location: 100, 64, 200
Shop Plot: Electronics_Store
Shop Type: SHOP

NPCs can now sell from this warehouse.
Use /npc <name> warehouse set to link NPCs.
```

---

### Unlinking Warehouse

```bash
/warehouse unlink
```

**Effect:**
- Removes shop connection
- NPCs can no longer sell from warehouse
- Inventory remains intact

---

## Delivery System

### Automatic Delivery

**Configuration:**
- **Frequency:** Every 3 days (real-time)
- **Payment:** State treasury pays delivery cost
- **Restocking:** Configured items automatically refilled

---

### Manual Delivery

```bash
/warehouse deliver
```

**Requirements:**
- Must be admin (Level 2)
- State account must have funds

**Cost Calculation:**
```
Base Cost: 1,000€
Item Count Multiplier: +10€ per item type
Distance Multiplier: +1€ per 100 blocks from spawn

Example:
Items: 12 types → 12 × 10€ = 120€
Distance: 500 blocks → 500 ÷ 100 = 5€
Total Cost: 1,000 + 120 + 5 = 1,125€
```

**Result:**
```
✓ Delivery Completed!

Items Delivered:
- Diamond: +256 (800 total)
- Gold Ingot: +0 (1,024 FULL)
- Emerald: +512 (768 total)
... (all configured items)

Delivery Cost: 1,125€
Paid by: State Treasury
State Remaining: 244,475€

Next Auto-Delivery: 2024-01-21
```

---

### Delivery Timer Reset

```bash
/warehouse reset
```

**Use Cases:**
- Force immediate next delivery
- Fix delivery schedule issues
- Testing delivery system

**Result:**
```
✓ Delivery Timer Reset!

Previous Next Delivery: 2024-01-18 (in 2 days)
New Next Delivery: 2024-01-16 (in 3 days from now)
```

---

## NPC Integration

### Linking NPC to Warehouse

```bash
/npc <name> warehouse set
```

**Requirements:**
- Look at warehouse block
- NPC must be merchant type
- NPC must have shop assigned

**Example:**
```bash
# 1. Assign NPC to shop
/npc Shop_Owner setshop Electronics_Store

# 2. Look at warehouse block
# 3. Link NPC to warehouse
/npc Shop_Owner warehouse set
```

**Result:**
```
✓ NPC Linked to Warehouse!

NPC: Shop_Owner
Warehouse: Electronics_Store (100, 64, 200)
Shop: Electronics_Store

Shop_Owner can now sell unlimited items from warehouse.
```

---

### NPC Selling Behavior

**With Warehouse:**
```
Player: "I want to buy Diamond"
NPC Inventory: Diamond × 16
Warehouse Inventory: Diamond × 544

NPC sells: 1 Diamond from inventory
NPC auto-restocks: +1 Diamond from warehouse
Warehouse: 543 remaining

Result: Unlimited stock as long as warehouse has items
```

**Without Warehouse:**
```
Player: "I want to buy Diamond"
NPC Inventory: Diamond × 16
No Warehouse

NPC sells: 1 Diamond
NPC Inventory: 15 remaining

Result: Limited to NPC inventory (max 9 slots × 64)
```

---

### Checking NPC Warehouse

```bash
/npc <name> warehouse info
```

**Output:**
```
═══ NPC WAREHOUSE INFO ═══

NPC: Shop_Owner
Warehouse: Electronics_Store
Location: 100, 64, 200

Linked: ✓ Yes
Status: Active
Items Available: 12 types

Shop: Electronics_Store
Plot: shop_electronics_1
```

---

### Unlinking NPC from Warehouse

```bash
/npc <name> warehouse clear
```

**Effect:**
- NPC can only sell from personal inventory
- Warehouse remains linked to shop
- Other NPCs unaffected

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

---

#### 2. Monitor Inventory Levels
```bash
# Daily check
/warehouse info

# Low stock alert thresholds:
- < 100 items: Restock soon
- < 50 items: Critical
- 0 items: Out of stock (NPCs can't sell)
```

---

#### 3. Optimize Delivery Timing
```
Delivery every 3 days = 120 deliveries/year
Cost per delivery: ~1,000€
Annual cost: ~120,000€

Ensure state treasury has:
Minimum: 50,000€ (buffer for 50 deliveries)
Recommended: 200,000€ (seasonal buffer)
```

---

#### 4. Balance Item Distribution
```
High-Demand Items (stock 800-1,024):
- Diamonds
- Gold
- Emeralds
- Popular production items

Medium-Demand (stock 400-600):
- Iron
- Redstone
- Common items

Low-Demand (stock 100-200):
- Specialty items
- Rare products
```

---

### For Server Performance

#### 1. Warehouse Limits
```
Recommended:
- Max warehouses per server: 20
- Max linked NPCs per warehouse: 5
- Keep inventory < 80% full for performance
```

---

#### 2. Delivery Optimization
```
# Avoid simultaneous deliveries
- Stagger warehouse placements
- Different delivery times
- Reduces server load spikes
```

---

## Troubleshooting

### "NPC Out of Stock"

**Problem:** NPC says "I don't have that item"

**Causes:**
1. Item not in NPC inventory
2. Item not in warehouse
3. NPC not linked to warehouse
4. Warehouse not linked to shop

**Solutions:**
```bash
# Check NPC inventory
/npc Shop_Owner inventory

# Check warehouse link
/npc Shop_Owner warehouse info

# Check warehouse inventory
/warehouse info

# Add items if missing
/warehouse add minecraft:diamond 512

# Link NPC if needed
/npc Shop_Owner warehouse set
```

---

### "Delivery Failed"

**Problem:** Automatic delivery not working

**Causes:**
1. State account insufficient funds
2. Warehouse not linked to shop
3. Delivery timer not set

**Solutions:**
```bash
# Check state balance
/state balance

# Fund state if needed
/state deposit 50000

# Check warehouse info
/warehouse info

# Manually trigger delivery
/warehouse deliver

# Reset timer if stuck
/warehouse reset
```

---

### "Cannot Add Items"

**Problem:** `/warehouse add` command fails

**Causes:**
1. Warehouse full (32 item types)
2. Slot full (1,024 items)
3. Not looking at warehouse
4. Not in correct plot

**Solutions:**
```bash
# Check capacity
/warehouse info
# Look for slots at 1,024/1,024

# Remove old items if full
/warehouse remove <unused_item> <amount>

# Ensure looking at warehouse block
# Stand close and look directly at it
```

---

### Warehouse Not Saving

**Problem:** Inventory resets after restart

**Causes:**
1. Disk space full
2. Permission errors
3. Corrupted data file

**Solutions:**
```bash
# Check system health
/health

# Check backups
/health backups

# Verify file permissions
# Check: config/plotmod_warehouses.json

# Restart server if needed
# Data should auto-recover from backup
```

---

<div align="center">

**Warehouse System - Complete Guide**

For related systems:
- [🏘️ Plot System](Plot-System.md)
- [💰 Economy System](Economy-System.md)
- [🤖 NPC System](NPC-System.md)

[🏠 Back to Wiki Home](../Home.md) • [📋 All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
