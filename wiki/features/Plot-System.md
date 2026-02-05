# Plot Management System

<div align="center">

**Complete Land Ownership, Apartments & Property Management**

Advanced spatial indexing with O(log n) lookup performance

[🏠 Back to Wiki Home](../Home.md) • [📋 Commands Reference](../Commands.md)

</div>

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Plot Types](#plot-types)
3. [Creating Plots](#creating-plots)
4. [Plot Ownership](#plot-ownership)
5. [Trust System](#trust-system)
6. [Apartment System](#apartment-system)
7. [Rental System](#rental-system)
8. [Plot Rating System](#plot-rating-system)
9. [Protection & Permissions](#protection--permissions)
10. [Spatial Indexing](#spatial-indexing-technical)
11. [Best Practices](#best-practices)
12. [Troubleshooting](#troubleshooting)

---

## Overview

The Plot Management System is the foundation of ScheduleMC's property and land ownership mechanics. It provides a comprehensive framework for creating, managing, and trading virtual real estate.

### Key Features

✅ **5 Plot Types** - Residential, Commercial, Shop, Public, Government
✅ **Apartment System** - Multi-tenant rental units within plots
✅ **Trust Management** - Granular permission control
✅ **Rating System** - Community-driven plot quality assessment
✅ **Rental Mechanics** - Daily rental with security deposits
✅ **Spatial Indexing** - O(log n) performance for 10,000+ plots
✅ **Multi-block Protection** - Comprehensive block & entity protection
✅ **Transaction System** - Integrated with economy for sales/rentals

### Architecture

```
PlotManager (Singleton)
├── PlotRegistry (ConcurrentHashMap)
├── SpatialIndex (QuadTree for fast lookup)
├── ApartmentManager (Nested apartment system)
└── PlotPersistence (JSON with backup system)
```

**Performance Specs:**
- Plot lookup: **0.3ms average** (O(log n))
- Max plots tested: **10,000+**
- Protection check: **< 1ms**
- Persistence: **Auto-save every 5 minutes**

---

## Plot Types

ScheduleMC supports 5 distinct plot types, each with specific purposes and permissions.

### 1. Residential Plots 🏠

**Purpose:** Homes, private properties, housing
**Color Code:** Green (on map)
**Ownership:** Player-owned
**Tradeable:** Yes

**Features:**
- Apartment creation allowed
- Can be rented to other players
- Protected from non-trusted players
- Default type for player housing

**Use Cases:**
```
- Personal homes
- Private mansions
- Apartment buildings
- Rental properties
```

**Creation:**
```bash
/plot create residential "My_Home" 50000
```

---

### 2. Commercial Plots 🏢

**Purpose:** Businesses, offices, workplaces
**Color Code:** Blue (on map)
**Ownership:** Player-owned
**Tradeable:** Yes

**Features:**
- Higher utility costs (if configured)
- Can be rented
- Suitable for businesses
- Office space apartments

**Use Cases:**
```
- Office buildings
- Corporate headquarters
- Business centers
- Professional spaces
```

**Creation:**
```bash
/plot create commercial "Office_Tower" 100000
```

---

### 3. Shop Plots 🛒

**Purpose:** NPC merchant shops with inventory systems
**Color Code:** Yellow (on map)
**Ownership:** Admin-created, can be transferred
**Tradeable:** Yes

**Features:**
- Warehouse linkage support
- NPC merchant assignment

**Special Properties:**
- No creation price (admin-only)
- Can link to warehouse blocks
- NPCs sell from shop inventory

**Use Cases:**
```
- NPC shops
- Market stalls
- Trading posts
- Item vendors
```

**Creation:**
```bash
/plot create shop "Main_Street_Market"
```

---

### 4. Public Plots 🌳

**Purpose:** Public spaces, parks, roads, spawn areas
**Color Code:** Gray (on map)
**Ownership:** Server (admin-managed)
**Tradeable:** No

**Features:**
- No protection (all players can access)
- Cannot be bought/sold
- No apartments
- Free to use

**Use Cases:**
```
- Spawn areas
- Public parks
- Roads and paths
- Community centers
```

**Creation:**
```bash
/plot create public "Central_Park"
```

---

### 5. Government Plots 🏛️

**Purpose:** Prisons, town halls, government buildings
**Color Code:** Red (on map)
**Ownership:** Server (admin-managed)
**Tradeable:** No

**Features:**
- Prison system integration
- Cannot be bought/sold
- Admin-only access
- Special mechanics (prison cells)

**Use Cases:**
```
- Prison buildings
- Town halls
- Police stations
- Government offices
```

**Creation:**
```bash
/plot create government "City_Prison"
```

---

## Creating Plots

### Selection Process

Plots are created using a **two-point selection system** similar to WorldEdit.

#### Step 1: Get the Selection Tool
```bash
/plot wand
```

**Received:** Golden Axe (Plot Selection Tool)

#### Step 2: Select First Corner
**Action:** Left-click a block

**Feedback:**
```
✓ First position set: (100, 64, 200)
```

#### Step 3: Select Second Corner
**Action:** Right-click a block

**Feedback:**
```
✓ Second position set: (150, 80, 250)
Volume: 63,750 blocks (50×16×50)
```

#### Step 4: Create the Plot
```bash
/plot create <type> <name> [price]
```

**Examples:**
```bash
# Residential with 50,000€ price
/plot create residential "Downtown_House_1" 50000

# Commercial with 100,000€ price
/plot create commercial "Office_Building_A" 100000

# Shop (no price)
/plot create shop "Market_Stall_3"

# Public (no price)
/plot create public "Town_Square"
```

---

### Plot Naming Rules

| Rule | Valid | Invalid |
|------|-------|---------|
| **No Spaces** | ✓ My_Home | ✗ My Home |
| **Alphanumeric + Underscore** | ✓ House_2A | ✗ House-2A |
| **Length** | ✓ 3-32 chars | ✗ AB (too short) |
| **Uniqueness** | ✓ Unique ID | ✗ Duplicate name |

**Display Name:**
- The ID is permanent (e.g., `Downtown_House_1`)
- Display name can be changed: `/plot name Steve's Amazing Mansion`

---

### Plot Validation

Before creation, the system validates:

1. **No Overlap** - Plot cannot overlap existing plots
2. **Minimum Size** - At least 3×3×3 blocks (27 blocks)
3. **Maximum Size** - Configurable (default: 100×100×100)
4. **Selection Valid** - Both corners set in same world
5. **Owner Limit** - Player doesn't exceed max plots (configurable)

**Error Examples:**
```
✗ "Plot overlaps with existing plot: Downtown_House_2"
✗ "Plot too small (minimum 27 blocks)"
✗ "You already own the maximum number of plots (5)"
```

---

## Plot Ownership

### Buying Plots

#### Method 1: Buy While Standing On Plot
```bash
/plot buy
```

**Requirements:**
- Plot must be for sale
- You have enough money
- Plot is not already owned by you

**Transaction:**
```
Plot: Downtown_House_1
Price: 50,000€
Seller: Alex

✓ Transaction complete!
- You paid: 50,000€
- Alex received: 50,000€
- You are now the owner
```

#### Method 2: Buy Specific Plot by ID
```bash
/plot buy Downtown_House_1
```

**Use Case:**
- Buy from remote location
- Browse /plot list first

---

### Selling Plots

#### Put Plot Up For Sale
```bash
/plot sell <price>
```

**Example:**
```bash
/plot sell 75000
```

**Effects:**
- Plot appears in `/plot list` as for sale
- You retain ownership until sold
- You can still use the plot
- Price can be changed anytime

#### Cancel Sale
```bash
/plot unsell
```

---

### Transferring Plots

#### Free Transfer
```bash
/plot transfer <player>
```

**Example:**
```bash
/plot transfer Alex
```

**Use Cases:**
- Gift to friend
- Partnership transfer
- Clan/guild management

**Important:**
- ⚠️ Immediate and irreversible
- No money exchanged
- All apartments transfer with plot

---

### Abandoning Plots

```bash
/plot abandon
```

**Refund:** 50% of creation cost

**Example:**
```
Plot created for: 50,000€
Refund amount: 25,000€

⚠️ This will:
- Delete the plot
- Evict all apartment tenants (no refund)
- Remove all protections
- This cannot be undone!

Type /plot abandon confirm to proceed.
```

---

## Trust System

The trust system allows plot owners to grant build permissions to other players without transferring ownership.

### Trusting Players

```bash
/plot trust <player>
```

**Example:**
```bash
/plot trust Alex
```

**Permissions Granted:**
✅ Place blocks
✅ Break blocks
✅ Open containers (chests, furnaces, etc.)
✅ Use doors, buttons, levers
✅ Interact with blocks
✅ Kill entities (animals, NPCs)

**Permissions NOT Granted:**
❌ Transfer ownership
❌ Trust other players
❌ Sell the plot
❌ Create apartments
❌ Delete the plot

---

### Untrusting Players

```bash
/plot untrust <player>
```

**Effect:**
- Immediately revokes all permissions
- Player can no longer build
- No refund or compensation

---

### Trust List

```bash
/plot trustlist
```

**Output:**
```
Trusted Players on Downtown_House_1:
1. Alex (since 2024-01-10)
2. Bob (since 2024-01-15)
3. Charlie (since 2024-01-18)

Total: 3 trusted players
```

---

### Trust Inheritance

**Apartments:**
- Apartment tenants do NOT inherit plot trust
- Tenants only have access to their apartment area
- Plot owner must explicitly trust for full access

**Rentals:**
- Plot renters get full plot access during rental period
- Trust is automatic for rental duration
- Revoked automatically when rental expires

---

## Apartment System

Apartments allow plot owners to subdivide their plots into rentable units with individual protections.

### Apartment Architecture

```
Plot: Downtown_House_1 (Owner: Steve)
├── Apartment: Apt_1A (Tenant: Alex)
│   ├── Protected area: 100,50,200 → 110,55,210
│   ├── Monthly rent: 2,000€
│   └── Lease until: 2024-02-15
├── Apartment: Apt_1B (Tenant: Bob)
│   ├── Protected area: 110,50,200 → 120,55,210
│   ├── Monthly rent: 1,500€
│   └── Lease until: 2024-02-20
└── Apartment: Apt_2A (Vacant)
    ├── Protected area: 100,56,200 → 110,60,210
    ├── Monthly rent: 2,500€
    └── Status: Available
```

---

### Creating Apartments

#### Step 1: Get Apartment Wand
```bash
/plot apartment wand
```

**Requirements:**
- Must be owner of current plot
- Must be inside your plot

#### Step 2: Select Apartment Area
**Actions:**
- Left-click first corner
- Right-click second corner

**Validation:**
- Selection must be inside plot boundaries
- Cannot overlap other apartments

#### Step 3: Create Apartment
```bash
/plot apartment create <name> <monthlyRent>
```

**Example:**
```bash
/plot apartment create Penthouse_A 2500
```

**Result:**
```
✓ Apartment created!
ID: apt_12345
Name: Penthouse_A
Monthly Rent: 2,500€
Security Deposit: 2,500€ (1 month)
Size: 10×5×10 (500 blocks)
Status: Available for rent
```

---

### Apartment Rental (Tenant)

#### Rent an Apartment
```bash
/plot apartment rent <apartmentId> [days]
```

**Example:**
```bash
/plot apartment rent apt_12345 60
```

**Payment Calculation:**
```
Monthly Rent: 2,500€
Rental Period: 60 days = 2 months

Rent Cost: 2,500€ × 2 = 5,000€
Security Deposit: 2,500€ (1 month)
Total Charged: 7,500€

Lease Period: 60 days
Auto-renewal: Optional (configurable)
```

**Effects:**
- Money deducted immediately
- Full access to apartment area
- Protected from other players
- Monthly auto-charge begins

---

### Monthly Auto-Charge

**System:**
- Every 30 in-game days, rent is auto-charged
- If balance insufficient, **eviction warning**
- After 3 days grace period, **automatic eviction**

**Example Timeline:**
```
Day 1:   Rent apartment (7,500€ charged)
Day 30:  Auto-charge 2,500€ (rent)
Day 60:  Auto-charge 2,500€ (rent) → Lease expires

Option 1: Extend lease with /plot apartment rent
Option 2: Leave apartment with /plot apartment leave
```

---

### Leaving Apartments

```bash
/plot apartment leave
```

**Refund:**
✅ Security deposit returned in full
❌ No refund on unused rental days

**Example:**
```
Rental Period: 60 days
Days Used: 45 days
Days Remaining: 15 days

Security Deposit Refund: 2,500€
Unused Rent: 0€ (no refund)

Total Refund: 2,500€
```

---

### Managing Apartments (Owner)

#### List Apartments
```bash
/plot apartment list
```

**Output:**
```
Apartments in Downtown_House_1:

1. Apt_1A (apt_12345)
   Tenant: Alex
   Monthly Rent: 2,500€
   Lease Expires: 2024-02-15 (22 days)
   Status: Occupied

2. Apt_1B (apt_67890)
   Tenant: None
   Monthly Rent: 1,500€
   Status: Available

3. Apt_2A (apt_11111)
   Tenant: Bob
   Monthly Rent: 2,000€
   Lease Expires: 2024-02-20 (27 days)
   Status: Occupied

Total: 3 apartments (2 occupied, 1 vacant)
Monthly Revenue: 4,500€
```

---

#### Change Rent
```bash
/plot apartment setrent <apartmentId> <newRent>
```

**Example:**
```bash
/plot apartment setrent apt_12345 3000
```

**Important:**
- Only affects NEW tenants
- Existing tenants keep old rate until lease renewal
- No retroactive changes

---

#### Evict Tenant
```bash
/plot apartment evict <apartmentId>
```

**Warning:**
⚠️ **Eviction Policy**
- Tenant loses security deposit
- Immediate removal
- Use only for violations

**Example:**
```
Evict tenant from Apt_1A?
Tenant: Alex
Security Deposit Forfeited: 2,500€

Type /plot apartment evict apt_12345 confirm
```

---

#### Delete Apartment
```bash
/plot apartment delete <apartmentId>
```

**Effects:**
- Evicts current tenant (if any)
- Removes apartment permanently
- Cannot be undone

---

## Rental System

Whole-plot rental is different from apartments - the entire plot is rented to one player.

### Offering Plot for Rent

```bash
/plot rent <pricePerDay>
```

**Example:**
```bash
/plot rent 500
```

**Rental Terms:**
- Price is per in-game day (20 minutes real-time)
- Security deposit = 2× daily rent
- Tenant gets full plot access
- Cannot be evicted during rental period

---

### Renting a Plot

```bash
/plot rentplot <days> [plotId]
```

**Example:**
```bash
/plot rentplot 7 Downtown_House_1
```

**Payment:**
```
Daily Rent: 500€
Rental Days: 7
Security Deposit: 1,000€ (2× daily)

Total Cost:
- Rent: 500€ × 7 = 3,500€
- Deposit: 1,000€
- Total: 4,500€

Lease Expires: 2024-01-22 (7 days)
```

---

### Extending Rentals

```bash
/plot rentextend <days>
```

**Example:**
```bash
/plot rentextend 7
```

**Payment:**
- Only pays daily rent × additional days
- No new security deposit

**Example:**
```
Current Lease: Expires in 2 days
Extension: 7 days
New Expiry: 9 days from now

Cost: 500€ × 7 = 3,500€
```

---

### Rental Expiration

**Auto-Expiry:**
1. Lease expires after rental period
2. Security deposit auto-refunded
3. Plot access revoked
4. Tenant notified

**Grace Period:**
- 24-hour grace period before eviction
- Can extend during grace period
- After grace period, automatic removal

---

## Plot Rating System

Community-driven quality assessment for plots.

### Rating a Plot

```bash
/plot rate <rating>
```

**Example:**
```bash
/plot rate 5
```

**Rating Scale:**
- ⭐ 1 Star - Poor quality
- ⭐⭐ 2 Stars - Below average
- ⭐⭐⭐ 3 Stars - Average
- ⭐⭐⭐⭐ 4 Stars - Good quality
- ⭐⭐⭐⭐⭐ 5 Stars - Excellent

---

### Rating Rules

✅ **Can rate:**
- Plots you don't own
- Any plot type
- Change your rating anytime

❌ **Cannot rate:**
- Your own plots
- Same plot multiple times (overwrites previous rating)

---

### Rating Calculation

**Average Rating:**
```
Total Ratings: 15
5 stars: 8 players
4 stars: 5 players
3 stars: 2 players

Average: (8×5 + 5×4 + 2×3) ÷ 15 = 4.4 stars
Display: ⭐⭐⭐⭐ (4.4/5.0)
```

---

### Top Rated Plots

```bash
/plot topplots
```

**Output:**
```
🏆 Top Rated Plots:

1. ⭐⭐⭐⭐⭐ Steve's Mansion (5.0/5.0)
   Owner: Steve
   Ratings: 23
   Type: Residential

2. ⭐⭐⭐⭐⭐ Downtown Tower (4.9/5.0)
   Owner: Alex
   Ratings: 18
   Type: Commercial

3. ⭐⭐⭐⭐ Central Park (4.7/5.0)
   Owner: Server
   Ratings: 45
   Type: Public

...

10. ⭐⭐⭐⭐ Market Plaza (4.2/5.0)
    Owner: Bob
    Ratings: 12
    Type: Shop
```

---

## Protection & Permissions

### Block Protection

**Protected Actions:**
- ✗ Place blocks
- ✗ Break blocks
- ✗ Use buckets (lava/water)
- ✗ Ignite fire/TNT
- ✗ Trample farmland

**Exceptions:**
- ✓ Plot owner
- ✓ Trusted players
- ✓ Apartment tenants (within apartment)

---

### Container Protection

**Protected Containers:**
- Chests
- Barrels
- Furnaces
- Hoppers
- Dispensers
- Droppers
- Shulker boxes

**Access Control:**
- Only owner, trusted, or apartment tenant can open

---

### Entity Protection

**Protected Entities:**
- Animals (cows, pigs, sheep)
- Villagers
- NPCs
- Item frames
- Armor stands
- Vehicles

**Protected Actions:**
- ✗ Attack entities
- ✗ Leash animals
- ✗ Shear sheep
- ✗ Breed animals
- ✗ Remove items from item frames

---

### Interaction Protection

**Protected Interactions:**
- Doors
- Trapdoors
- Fence gates
- Buttons
- Levers
- Pressure plates

**Access:**
- Doors: Owner + trusted only
- Buttons/Levers: Owner + trusted only
- Pressure plates: Anyone (for pathways)

---

## Spatial Indexing (Technical)

ScheduleMC uses a **QuadTree spatial index** for high-performance plot lookups.

### Architecture

```java
public class SpatialIndex {
    private QuadTree<Plot> root;

    // O(log n) lookup
    public Plot getPlotAt(BlockPos pos) {
        return root.query(pos.getX(), pos.getZ());
    }
}
```

### Performance Benchmarks

| Operation | Time Complexity | Average Time |
|-----------|----------------|--------------|
| **Plot Lookup** | O(log n) | 0.3ms |
| **Add Plot** | O(log n) | 0.5ms |
| **Remove Plot** | O(log n) | 0.4ms |
| **Overlap Check** | O(log n) | 0.6ms |

**Stress Test Results:**
- 10,000 plots: 0.3ms average lookup
- 50,000 plots: 0.8ms average lookup
- 100,000 plots: 1.2ms average lookup

---

### QuadTree Structure

```
Root Node (World bounds)
├── NW Quadrant
│   ├── Plot A (100,200 → 150,250)
│   └── Plot B (160,200 → 200,240)
├── NE Quadrant
│   └── Plot C (300,100 → 350,150)
├── SW Quadrant
│   ├── Plot D (50,400 → 100,450)
│   └── Plot E (120,420 → 170,480)
└── SE Quadrant
    └── Plot F (400,500 → 450,550)
```

**Depth:** Auto-balancing, typically 4-6 levels for 10,000 plots

---

### Index Rebuilding

```bash
/plot reindex
```

**When to use:**
- After large-scale plot changes
- Performance degradation
- Corrupt index data

**Process:**
1. Clears existing index
2. Iterates all plots
3. Rebuilds QuadTree
4. Validates structure

**Time:** ~50ms per 1,000 plots

---

## Best Practices

### For Plot Owners

1. **Set Clear Descriptions**
   ```bash
   /plot name Steve's Downtown Mansion
   /plot description Beautiful 3-story home with ocean view
   ```

2. **Trust Carefully**
   - Only trust players you know
   - Review trust list regularly
   - Untrust inactive players

3. **Apartment Management**
   - Set fair rent prices
   - Respond to tenant issues
   - Keep apartments maintained

4. **Security**
   - Don't share plot ownership
   - Use apartments for guests
   - Monitor trusted player activity

---

### For Admins

1. **Initial Setup**
   ```bash
   # Create spawn area
   /plot create public Spawn

   # Create prison
   /plot create government Prison

   # Create marketplace
   /plot create shop Main_Market
   ```

2. **Maintenance**
   ```bash
   # Monthly health check
   /health plot

   # Reindex if needed
   /plot reindex

   # Check top plots
   /plot topplots
   ```

3. **Performance Monitoring**
   - Monitor `/health plot` for index performance
   - Reindex if lookup time > 2ms
   - Check for plot overlap issues

---

### For Server Performance

1. **Plot Limits**
   - Recommended: 5 plots per player
   - Max tested: 10,000 total plots
   - Balance quality over quantity

2. **Size Limits**
   - Recommended max: 100×100×100
   - Prevents mega-plots
   - Better for index performance

3. **Persistence**
   - Auto-save: Every 5 minutes
   - Backups: Hourly
   - Test restore procedure

---

## Troubleshooting

### "You cannot build here"

**Causes:**
1. Not plot owner
2. Not trusted on plot
3. Public/Government plot (admin only)

**Solutions:**
```bash
# Check plot info
/plot info

# If you should have access:
- Ask owner for /plot trust YourName
- Check if rental expired
- Verify you're in correct plot
```

---

### "Plot overlaps existing plot"

**Cause:**
- New plot boundaries overlap another plot

**Solution:**
```bash
# Check existing plots
/plot list

# Adjust selection to avoid overlap
/plot wand
# Select smaller or different area
```

---

### Plot Not Saving

**Causes:**
1. Disk space full
2. Permission errors
3. Corrupted data

**Solutions:**
```bash
# Check system health
/health plot

# Check backups
/health backups

# Manual save (admin)
# Triggered automatically every 5 minutes
```

---

### Apartment Access Issues

**Problem:** Tenant can't access apartment

**Solutions:**
1. Check lease expiry: `/plot apartment info <id>`
2. Verify tenant is correct player
3. Check apartment boundaries (might be outside)
4. Relog to refresh permissions

---

### Performance Degradation

**Symptoms:**
- Slow plot lookups
- Lag when entering plots
- `/plot info` takes > 1 second

**Solutions:**
```bash
# Rebuild spatial index
/plot reindex

# Check index health
/plot debug

# Review total plot count
/health plot
```

---

<div align="center">

**Plot Management System - Complete Guide**

For related systems:
- [💰 Economy System](Economy-System.md)
- [🤖 NPC System](NPC-System.md)
- [🏪 Warehouse System](Warehouse-System.md)

[🏠 Back to Wiki Home](../Home.md) • [📋 All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
