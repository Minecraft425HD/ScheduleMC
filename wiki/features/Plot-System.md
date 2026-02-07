# Plot Management System

<div align="center">

**Complete Land Ownership, Apartments, Ratings & Property Management**

Chunk-based spatial indexing with O(1) lookup and multi-level LRU cache performance

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Plot Types](#plot-types)
4. [Creating Plots](#creating-plots)
5. [Plot Ownership](#plot-ownership)
6. [Trust System](#trust-system)
7. [Apartment System](#apartment-system)
8. [Rental System](#rental-system)
9. [Plot Rating System](#plot-rating-system)
10. [Plot Info Block](#plot-info-block)
11. [Protection and Permissions](#protection-and-permissions)
12. [Spatial Indexing](#spatial-indexing-technical)
13. [Developer API](#developer-api)
14. [Commands Reference](#commands-reference)
15. [Best Practices](#best-practices)
16. [Troubleshooting](#troubleshooting)

---

## Overview

The Plot Management System is the foundation of ScheduleMC's property and land ownership mechanics. It provides a comprehensive framework for creating, managing, trading, and rating virtual real estate with apartment sub-leasing and integrated economy transactions.

### Key Features

- **5 Plot Types** -- Residential, Commercial, Shop, Public, Government
- **Visual Selection Tool** -- Plot Wand (Golden Axe) for intuitive boundary selection
- **Apartment System** -- Multi-tenant rental units with deposits and rent
- **Trust Management** -- Granular permission control for block and inventory access
- **Rating System** -- Community-driven 5-star quality assessment with leaderboards
- **Rental Mechanics** -- Daily rental with security deposits and grace periods
- **50% Refund** -- On plot abandonment (based on creation cost)
- **Plot Info Block** -- Physical block that displays plot information
- **Spatial Indexing** -- Chunk-based spatial index with LRU cache for O(1) performance
- **Multi-block Protection** -- Comprehensive block, container, entity, and interaction protection
- **Economy Integration** -- Integrated with economy system for purchases, sales, and rentals

---

## Architecture

### System Diagram

```
PlotManager (Singleton)
|-- PlotRegistry (ConcurrentHashMap<String, PlotRegion>)
|-- SpatialIndex (Chunk-based ConcurrentHashMap for fast lookup)
|   |-- O(1) for chunk-based spatial queries
|   +-- LRU Cache layer for O(1) repeated lookups
|-- ApartmentManager (Nested apartment system)
|-- PlotPersistence (JSON with automatic backup)
+-- PlotRatingManager (5-star rating and leaderboards)
```

### Performance Specifications

| Operation | Time Complexity | Average Time |
|-----------|----------------|--------------|
| Plot Lookup (cached) | O(1) | < 0.1ms |
| Plot Lookup (cache miss) | O(1) chunk lookup + O(k) iteration | 0.3ms |
| Add Plot | O(1) | 0.5ms |
| Remove Plot | O(1) | 0.4ms |
| Overlap Check | O(k) within chunk | 0.6ms |
| Protection Check | O(1) | < 1ms |
| Auto-save | -- | Every 5 minutes |

### Stress Test Results

| Plot Count | Average Lookup Time |
|------------|-------------------|
| 10,000 | 0.3ms |
| 50,000 | 0.8ms |
| 100,000 | 1.2ms |

---

## Plot Types

ScheduleMC supports 5 distinct plot types, each with specific purposes and permissions.

### Comparison Table

| Type | Color | Ownership | Tradeable | Apartments | Protection |
|------|-------|-----------|-----------|------------|------------|
| **RESIDENTIAL** | Green | Player | Yes | Yes | Full |
| **COMMERCIAL** | Blue | Player | Yes | Yes | Full |
| **SHOP** | Yellow | Admin/Player | Yes | No | Full |
| **PUBLIC** | Gray | Server | No | No | None (open) |
| **GOVERNMENT** | Red | Server | No | No | Admin-only |

### 1. Residential Plots

**Purpose:** Homes, private properties, housing

**Features:**
- Apartment creation allowed
- Can be rented to other players
- Protected from non-trusted players
- Default type for player housing

**Use Cases:**
- Personal homes and mansions
- Apartment buildings
- Rental properties

**Creation:**
```
/plot create residential "My_Home" 50000
```

### 2. Commercial Plots

**Purpose:** Businesses, offices, workplaces

**Features:**
- Higher utility costs (if configured)
- Can be rented
- Suitable for business operations
- Office space apartments

**Use Cases:**
- Office buildings and corporate headquarters
- Business centers
- Professional workspaces

**Creation:**
```
/plot create commercial "Office_Tower" 100000
```

### 3. Shop Plots

**Purpose:** NPC merchant shops with inventory systems

**Features:**
- Warehouse linkage support
- NPC merchant assignment
- No creation price (admin-only creation)
- Can link to warehouse blocks

**Use Cases:**
- NPC shops and market stalls
- Trading posts and item vendors

**Creation:**
```
/plot create shop "Main_Street_Market"
```

### 4. Public Plots

**Purpose:** Public spaces, parks, roads, spawn areas

**Features:**
- No protection (all players can access)
- Cannot be bought or sold
- No apartments
- Free to use

**Use Cases:**
- Spawn areas, public parks
- Roads and paths
- Community centers

**Creation:**
```
/plot create public "Central_Park"
```

### 5. Government Plots

**Purpose:** Prisons, town halls, government buildings

**Features:**
- Prison system integration
- Cannot be bought or sold
- Admin-only access
- Special mechanics (prison cells)

**Use Cases:**
- Prison buildings and police stations
- Town halls and government offices

**Creation:**
```
/plot create government "City_Prison"
```

---

## Creating Plots

### Selection Process

Plots are created using a **two-point visual selection system** with the Plot Wand.

#### Step 1: Get the Plot Wand
```
/plot wand
```

**Received:** Golden Axe (Plot Selection Tool)

The Plot Wand provides visual feedback as you select corners, highlighting the selection area.

#### Step 2: Select First Corner

**Action:** Left-click a block with the Plot Wand

**Feedback:**
```
First position set: (100, 64, 200)
```

#### Step 3: Select Second Corner

**Action:** Right-click a block with the Plot Wand

**Feedback:**
```
Second position set: (150, 80, 250)
Volume: 63,750 blocks (50 x 16 x 50)
```

#### Step 4: Create the Plot
```
/plot create <type> <name> [price]
```

**Examples:**
```
/plot create residential "Downtown_House_1" 50000
/plot create commercial "Office_Building_A" 100000
/plot create shop "Market_Stall_3"
/plot create public "Town_Square"
/plot create government "City_Hall"
```

### Plot Naming Rules

| Rule | Valid | Invalid |
|------|-------|---------|
| No Spaces | My_Home | My Home |
| Alphanumeric + Underscore | House_2A | House-2A |
| Length 3-32 chars | Downtown_1 | AB (too short) |
| Unique ID | Unique name | Duplicate name |

**Display Name:** The ID is permanent (e.g., `Downtown_House_1`), but the display name can be changed:
```
/plot name Steve's Amazing Mansion
```

### Plot Validation

Before creation, the system validates:

1. **No Overlap** -- Plot cannot overlap existing plots
2. **Minimum Size** -- At least 3 x 3 x 3 blocks (27 blocks)
3. **Maximum Size** -- Configurable (default: 100 x 100 x 100)
4. **Selection Valid** -- Both corners set in the same world
5. **Owner Limit** -- Player does not exceed max plots (configurable)

**Error Examples:**
```
"Plot overlaps with existing plot: Downtown_House_2"
"Plot too small (minimum 27 blocks)"
"You already own the maximum number of plots (5)"
```

---

## Plot Ownership

> **Note:** As of ScheduleMC 3.0, plot buying, selling, renting, listing, and info features have been moved from chat commands to the **Plot Info Block** GUI (Settings App UI). The descriptions below show the functionality available through the UI. There are no `/plot buy`, `/plot sell`, `/plot list`, `/plot info`, or `/plot rent` chat commands.

### Buying Plots

#### Method: Plot Info Block UI
Interact with the Plot Info Block placed on a plot to access the Buy button.

**Requirements:**
- Plot must be for sale
- You have enough money
- Plot is not already owned by you

**Transaction:**
```
Plot:   Downtown_House_1
Price:  50,000 EUR
Seller: Alex

Transaction complete!
- You paid: 50,000 EUR
- Alex received: 50,000 EUR
- You are now the owner
```

#### Method 2: Buy Specific Plot by ID
```
/plot buy Downtown_House_1
```

Allows purchasing from a remote location. Browse `/plot list` first.

### Selling Plots

#### Put Plot Up For Sale
```
/plot sell <price>
```

**Example:**
```
/plot sell 75000
```

**Effects:**
- Plot appears in `/plot list` as for sale
- You retain ownership until sold
- You can still use the plot
- Price can be changed anytime

#### Cancel Sale
```
/plot unsell
```

### Transferring Plots

```
/plot transfer <player>
```

**Example:**
```
/plot transfer Alex
```

**Important:**
- Immediate and irreversible
- No money exchanged
- All apartments transfer with plot

### Abandoning Plots

```
/plot abandon
```

**Refund:** 50% of creation cost

**Example:**
```
Plot created for:  50,000 EUR
Refund amount:     25,000 EUR

Warning! This will:
- Delete the plot
- Evict all apartment tenants (no deposit refund to them)
- Remove all protections
- This cannot be undone!

Type /plot abandon confirm to proceed.
```

---

## Trust System

The trust system allows plot owners to grant build permissions to other players without transferring ownership.

### Trusting Players

```
/plot trust <player>
```

**Permissions Granted:**
- Place and break blocks
- Open containers (chests, furnaces, etc.)
- Use doors, buttons, levers
- Interact with blocks
- Kill entities (animals, NPCs)

**Permissions NOT Granted:**
- Transfer ownership
- Trust other players
- Sell the plot
- Create apartments
- Delete the plot

### Untrusting Players

```
/plot untrust <player>
```

Immediately revokes all permissions.

### Trust List

```
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

### Trust Inheritance Rules

| Context | Inherits Plot Trust? |
|---------|---------------------|
| Apartment tenants | No -- only have access to their apartment area |
| Plot renters | Yes -- automatic full access during rental period |
| Plot transfer | Trust list cleared on transfer |

---

## Apartment System

Apartments allow plot owners to subdivide their plots into individually protected, rentable units with security deposits.

### Apartment Architecture

```
Plot: Downtown_House_1 (Owner: Steve)
|-- Apartment: Apt_1A (Tenant: Alex)
|   |-- Protected area: 100,50,200 -> 110,55,210
|   |-- Monthly rent: 2,000 EUR
|   +-- Lease until: 2024-02-15
|-- Apartment: Apt_1B (Tenant: Bob)
|   |-- Protected area: 110,50,200 -> 120,55,210
|   |-- Monthly rent: 1,500 EUR
|   +-- Lease until: 2024-02-20
+-- Apartment: Apt_2A (Vacant)
    |-- Protected area: 100,56,200 -> 110,60,210
    |-- Monthly rent: 2,500 EUR
    +-- Status: Available
```

### Creating Apartments

#### Step 1: Get Apartment Wand
```
/plot apartment wand
```

**Requirements:**
- Must be the owner of the current plot
- Must be standing inside your plot

#### Step 2: Select Apartment Area

- Left-click first corner
- Right-click second corner
- Selection must be inside plot boundaries
- Cannot overlap other apartments

#### Step 3: Create Apartment
```
/plot apartment create <name> <monthlyRent>
```

**Example:**
```
/plot apartment create Penthouse_A 2500
```

**Result:**
```
Apartment created!
ID:               apt_12345
Name:             Penthouse_A
Monthly Rent:     2,500 EUR
Security Deposit: 2,500 EUR (1 month)
Size:             10 x 5 x 10 (500 blocks)
Status:           Available for rent
```

### Renting an Apartment (Tenant)

```
/plot apartment rent <apartmentId> [days]
```

**Example:**
```
/plot apartment rent apt_12345 60
```

**Payment Calculation:**
```
Monthly Rent:      2,500 EUR
Rental Period:     60 days = 2 months

Rent Cost:         2,500 x 2 = 5,000 EUR
Security Deposit:  2,500 EUR (1 month)
Total Charged:     7,500 EUR

Lease Period:      60 days
Auto-renewal:      Optional (configurable)
```

**Effects:**
- Money deducted immediately
- Full access to apartment area
- Protected from other players (including plot owner for that area)
- Monthly auto-charge begins

### Monthly Auto-Charge

Every 30 in-game days, rent is auto-charged from the tenant's balance.

```
Day 1:   Rent apartment (7,500 EUR charged)
Day 30:  Auto-charge 2,500 EUR (rent)
Day 60:  Auto-charge 2,500 EUR (rent) --> Lease expires

Option 1: Extend lease with /plot apartment rent
Option 2: Leave apartment with /plot apartment leave
```

If balance is insufficient: eviction warning, then 3-day grace period, then automatic eviction.

### Leaving Apartments

```
/plot apartment leave
```

**Refund:**
- Security deposit returned in full
- No refund on unused rental days

### Managing Apartments (Owner)

#### List Apartments
```
/plot apartment list
```

**Output:**
```
Apartments in Downtown_House_1:

1. Apt_1A (apt_12345)
   Tenant: Alex
   Monthly Rent: 2,500 EUR
   Lease Expires: 2024-02-15 (22 days)
   Status: Occupied

2. Apt_1B (apt_67890)
   Tenant: None
   Monthly Rent: 1,500 EUR
   Status: Available

3. Apt_2A (apt_11111)
   Tenant: Bob
   Monthly Rent: 2,000 EUR
   Lease Expires: 2024-02-20 (27 days)
   Status: Occupied

Total: 3 apartments (2 occupied, 1 vacant)
Monthly Revenue: 4,500 EUR
```

#### Change Rent
```
/plot apartment setrent <apartmentId> <newRent>
```

Only affects new tenants. Existing tenants keep their rate until lease renewal.

#### Evict Tenant
```
/plot apartment evict <apartmentId>
```

**Warning:** Tenant loses security deposit. Use only for violations.

Requires confirmation: `/plot apartment evict <id> confirm`

#### Delete Apartment
```
/plot apartment delete <apartmentId>
```

Evicts current tenant (if any) and removes the apartment permanently.

---

## Rental System

Whole-plot rental is different from apartments -- the entire plot is rented to one player.

### Offering Plot for Rent

```
/plot rent <pricePerDay>
```

**Example:**
```
/plot rent 500
```

**Terms:**
- Price is per in-game day (20 minutes real time)
- Security deposit = 2x daily rent
- Tenant gets full plot access
- Cannot be evicted during rental period

### Renting a Plot

```
/plot rentplot <days> [plotId]
```

**Example:**
```
/plot rentplot 7 Downtown_House_1
```

**Payment:**
```
Daily Rent:        500 EUR
Rental Days:       7
Security Deposit:  1,000 EUR (2x daily)

Total Cost:
- Rent:    500 x 7 = 3,500 EUR
- Deposit:          1,000 EUR
- Total:            4,500 EUR

Lease Expires: 2024-01-22 (7 days)
```

### Extending Rentals

```
/plot rentextend <days>
```

Only pays daily rent times additional days. No new security deposit required.

### Rental Expiration

1. Lease expires after rental period
2. Security deposit auto-refunded
3. Plot access revoked
4. Tenant notified
5. 24-hour grace period before full removal (can extend during grace)

---

## Plot Rating System

Community-driven quality assessment for plots, with a 5-star scale and leaderboard support.

### Rating a Plot

```
/plot rate <rating>
```

**Rating Scale:**

| Stars | Description |
|-------|-------------|
| 1 | Poor quality |
| 2 | Below average |
| 3 | Average |
| 4 | Good quality |
| 5 | Excellent |

### Rating Rules

**Can rate:**
- Plots you do not own
- Any plot type
- Change your rating anytime (overwrites previous)

**Cannot rate:**
- Your own plots
- Same plot with multiple ratings (one per player)

### Rating Calculation

```
Total Ratings: 15
5 stars: 8 players
4 stars: 5 players
3 stars: 2 players

Average: (8 x 5 + 5 x 4 + 2 x 3) / 15 = 4.4 stars
Display: 4.4/5.0
```

### Leaderboard

```
/plot topplots
```

**Output:**
```
Top Rated Plots:

1. Steve's Mansion (5.0/5.0)
   Owner: Steve | Ratings: 23 | Type: Residential

2. Downtown Tower (4.9/5.0)
   Owner: Alex | Ratings: 18 | Type: Commercial

3. Central Park (4.7/5.0)
   Owner: Server | Ratings: 45 | Type: Public

4. Market Plaza (4.2/5.0)
   Owner: Bob | Ratings: 12 | Type: Shop

...

10. Riverside Cottage (3.8/5.0)
    Owner: Charlie | Ratings: 8 | Type: Residential
```

The leaderboard ranks plots by average rating, with ties broken by number of ratings.

---

## Plot Info Block

### Description

The Plot Info Block is a physical block that can be placed inside a plot to display its information to any player who interacts with it.

### Features

| Feature | Description |
|---------|-------------|
| Display | Shows plot name, owner, type, price, and rating |
| Placement | Must be placed inside a plot boundary |
| Interaction | Right-click to view plot details |
| Updates | Information updates dynamically |

### Usage

1. Obtain a Plot Info Block (crafting or admin give)
2. Place it inside any plot you own
3. Players right-click the block to see:
   - Plot name and ID
   - Owner name
   - Plot type
   - Sale price (if for sale)
   - Average rating and number of ratings
   - Apartment availability (if any)

This is especially useful for plots listed for sale or apartment buildings advertising vacancies.

---

## Protection and Permissions

### Block Protection

**Protected Actions (non-trusted players cannot):**
- Place blocks
- Break blocks
- Use buckets (lava/water)
- Ignite fire or TNT
- Trample farmland

**Exceptions:**
- Plot owner has full access
- Trusted players have full access
- Apartment tenants have access within their apartment area only

### Container Protection

**Protected Containers:**

| Container | Protected |
|-----------|-----------|
| Chests | Yes |
| Barrels | Yes |
| Furnaces | Yes |
| Hoppers | Yes |
| Dispensers | Yes |
| Droppers | Yes |
| Shulker boxes | Yes |

Only owner, trusted players, or apartment tenants (within their area) can open.

### Entity Protection

**Protected Entities:**
- Animals (cows, pigs, sheep)
- Villagers and NPCs
- Item frames and armor stands
- Vehicles

**Protected Actions:**
- Attack entities
- Leash animals
- Shear sheep
- Breed animals
- Remove items from item frames

### Interaction Protection

| Block | Access |
|-------|--------|
| Doors | Owner + trusted only |
| Trapdoors | Owner + trusted only |
| Fence gates | Owner + trusted only |
| Buttons | Owner + trusted only |
| Levers | Owner + trusted only |
| Pressure plates | Anyone (for pathways) |

---

## Spatial Indexing (Technical)

ScheduleMC uses a **QuadTree spatial index** combined with an **LRU cache** for high-performance plot lookups.

### Architecture

```java
public class SpatialIndex {
    private QuadTree<Plot> root;
    private LRUCache<BlockPos, Plot> cache;

    // O(1) with cache hit, O(1) chunk-based with cache miss
    public Plot getPlotAt(BlockPos pos) {
        Plot cached = cache.get(pos);
        if (cached != null) return cached;

        Plot found = root.query(pos.getX(), pos.getZ());
        if (found != null) cache.put(pos, found);
        return found;
    }
}
```

### Performance Characteristics

| Operation | Cache Hit | Cache Miss |
|-----------|-----------|------------|
| Plot Lookup | O(1) | O(1) chunk-based |
| Typical latency | < 0.1ms | 0.3ms |
| Cache hit rate | 85-95% (typical) | -- |

### QuadTree Structure

```
Root Node (World bounds)
|-- NW Quadrant
|   |-- Plot A (100,200 -> 150,250)
|   +-- Plot B (160,200 -> 200,240)
|-- NE Quadrant
|   +-- Plot C (300,100 -> 350,150)
|-- SW Quadrant
|   |-- Plot D (50,400 -> 100,450)
|   +-- Plot E (120,420 -> 170,480)
+-- SE Quadrant
    +-- Plot F (400,500 -> 450,550)
```

**Depth:** Auto-balancing, typically 4-6 levels for 10,000 plots.

### Index Rebuilding

```
/plot reindex
```

**When to use:**
- After large-scale plot changes
- Performance degradation detected
- Corrupt index data

**Process:**
1. Clears existing index and cache
2. Iterates all plots
3. Rebuilds QuadTree
4. Validates structure

**Time:** Approximately 50ms per 1,000 plots.

---

## Developer API

### IPlotAPI Interface

External mods can access the plot system through `IPlotAPI`. All methods are thread-safe (ConcurrentHashMap-backed).

**Obtaining the API:**
```java
IPlotAPI plotAPI = ScheduleMCAPI.getPlotAPI();
```

### Core Methods (v3.0.0+)

| Method | Return | Description |
|--------|--------|-------------|
| `getPlotAt(BlockPos)` | `PlotRegion` | Get plot at position (O(1) cached, O(1) chunk-based uncached) |
| `getPlot(String)` | `PlotRegion` | Get plot by ID |
| `hasPlot(String)` | `boolean` | Check if plot exists |
| `getPlotsByOwner(UUID)` | `List<PlotRegion>` | Get all plots owned by player |
| `getAvailablePlots()` | `List<PlotRegion>` | Get all purchasable plots |
| `getPlotsForSale()` | `List<PlotRegion>` | Get all plots listed for sale |
| `createPlot(BlockPos, BlockPos, String, PlotType, double)` | `PlotRegion` | Create a new plot |
| `removePlot(String)` | `boolean` | Remove a plot by ID |
| `getPlotCount()` | `int` | Get total number of plots |

### Extended Methods (v3.2.0+)

| Method | Return | Description |
|--------|--------|-------------|
| `getPlotsByType(PlotType)` | `List<PlotRegion>` | All plots of a specific type |
| `setPlotOwner(String, UUID)` | `boolean` | Change plot owner (null to remove) |
| `setPlotPrice(String, double)` | `boolean` | Set plot price |
| `addTrustedPlayer(String, UUID)` | `boolean` | Add a trusted player |
| `removeTrustedPlayer(String, UUID)` | `boolean` | Remove a trusted player |
| `getTrustedPlayers(String)` | `Set<UUID>` | Get all trusted players |
| `setPlotForSale(String, boolean)` | `boolean` | List or delist plot for sale |
| `setPlotType(String, PlotType)` | `boolean` | Change plot type |
| `getPlotsInRadius(BlockPos, double)` | `List<PlotRegion>` | Find plots within radius |
| `getPlotCountByType()` | `Map<PlotType, Integer>` | Count of plots per type |

### Usage Example

```java
IPlotAPI plotAPI = ScheduleMCAPI.getPlotAPI();

// Find plot at a position
PlotRegion plot = plotAPI.getPlotAt(new BlockPos(100, 64, 200));

// Get all plots owned by a player
List<PlotRegion> myPlots = plotAPI.getPlotsByOwner(playerUUID);

// Create a new plot
PlotRegion newPlot = plotAPI.createPlot(
    pos1, pos2, "MyPlot", PlotType.RESIDENTIAL, 50000.0
);

// Trust management
plotAPI.addTrustedPlayer("MyPlot", friendUUID);
Set<UUID> trusted = plotAPI.getTrustedPlayers("MyPlot");

// Find nearby plots
List<PlotRegion> nearby = plotAPI.getPlotsInRadius(playerPos, 100.0);

// Statistics
Map<PlotType, Integer> stats = plotAPI.getPlotCountByType();
```

---

## Commands Reference

### Player Commands (32)

| Command | Description | Permission |
|---------|-------------|------------|
| `/plot create <type> <name>` | Create a new plot (residential/commercial/shop/public/government/prison/towing_yard) | Admin |
| `/plot setowner <plotId> <player>` | Change plot owner | Admin |
| `/plot remove <plotId>` | Remove a plot | Admin |
| `/plot reindex` | Rebuild spatial index | Admin |
| `/plot debug` | Show debug/performance info for current position | Admin |
| `/plot settype <plotId> <type>` | Change plot type | Admin |
| `/plot apartment wand` | Get apartment selection tool | Admin |
| `/plot apartment create <name> <rent>` | Create an apartment | Admin |
| `/plot apartment delete <id>` | Delete an apartment | Admin |
| `/plot apartment list` | List all apartments in plot | Default |
| `/plot apartment info <id>` | View apartment details | Default |
| `/plot apartment rent <id> [days]` | Rent an apartment | Default |
| `/plot apartment leave` | Leave current apartment | Default |
| `/plot apartment setrent <id> <rent>` | Change apartment rent | Admin |
| `/plot apartment evict <id>` | Evict tenant from apartment | Admin |
| `/plot warehouse set` | Set warehouse location | Admin |
| `/plot warehouse clear` | Clear warehouse location | Admin |
| `/plot warehouse info` | View warehouse info | Default |

> **Note:** Plot buying, selling, renting, listing, info, trust management, ratings, and name/description are handled through the **Plot Info Block** GUI (Settings App UI), not chat commands. See [Plot Features via Settings App UI](../Commands.md#2-plot-features-via-settings-app-ui) for details.

**Total: 18 chat subcommands + GUI features**

---

## Best Practices

### For Plot Owners

#### 1. Set Clear Information
```
/plot name Steve's Downtown Mansion
/plot description Beautiful 3-story home with ocean view
```

Good descriptions help attract buyers and renters.

#### 2. Trust Carefully
- Only trust players you know
- Review trust list regularly with `/plot trustlist`
- Untrust inactive players

#### 3. Apartment Management
- Set fair rent prices based on apartment size
- Respond to tenant issues promptly
- Keep apartments maintained and attractive
- Place a Plot Info Block near the entrance

#### 4. Security
- Do not share plot ownership (use trust instead)
- Use apartments for temporary guests
- Monitor trusted player activity

### For Admins

#### 1. Initial Server Setup
```
/plot create public Spawn
/plot create government Prison
/plot create shop Main_Market
```

#### 2. Maintenance
```
/health plot              -- Monthly health check
/plot reindex             -- Rebuild index if lookups are slow
/plot topplots            -- Monitor community engagement
/plot stats               -- Review total plot counts
```

#### 3. Performance Monitoring
- Monitor `/health plot` for index performance
- Reindex if lookup time exceeds 2ms
- Check for plot overlap issues with `/plot debug`

### For Server Performance

| Recommendation | Value |
|----------------|-------|
| Max plots per player | 5 (recommended) |
| Max total plots tested | 10,000+ |
| Recommended max plot size | 100 x 100 x 100 |
| Auto-save interval | 5 minutes |
| Backup frequency | Hourly |

---

## Troubleshooting

### "You cannot build here"

**Causes:**
1. Not plot owner
2. Not trusted on plot
3. Public/Government plot (admin only)

**Solutions:**
```
/plot debug              -- Check plot details at current position
-- Ask owner to trust you via Plot Info Block UI
-- Check if rental has expired
-- Verify you are in the correct plot
```

### "Plot overlaps existing plot"

**Cause:** New plot boundaries overlap another plot.

**Solution:**
```
/plot debug              -- Check plots at current position
-- Use plot selection tool to select a different area
-- Adjust selection to avoid overlap
```

### Plot Not Saving

**Causes:**
1. Disk space full
2. Permission errors
3. Corrupted data

**Solutions:**
```
/health plot             -- Check system health
/health backups          -- Check backup integrity
-- Auto-save triggers every 5 minutes
-- Check server logs for write errors
```

### Apartment Access Issues

**Problem:** Tenant cannot access apartment.

**Solutions:**
1. Check lease expiry: `/plot apartment info <id>`
2. Verify the correct player is the tenant
3. Check apartment boundaries (tenant may be outside)
4. Relog to refresh permissions

### Performance Degradation

**Symptoms:**
- Slow plot lookups
- Lag when entering plots
- `/plot debug` takes more than 1 second

**Solutions:**
```
/plot reindex             -- Rebuild spatial index
/plot debug               -- Check index health metrics
/health plot              -- Review total plot count
```

---

<div align="center">

**Plot Management System -- Complete Guide**

For related systems:
- [Economy System](Economy-System.md)
- [NPC System](NPC-System.md)
- [Warehouse System](Warehouse-System.md)

[Back to Wiki Home](../Home.md) | [All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
