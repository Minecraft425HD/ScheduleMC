# Market System

<div align="center">

**Dynamic Pricing with Supply & Demand Economics**

Real-time price adjustments driven by player activity

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Price Mechanics](#price-mechanics)
3. [Supply & Demand](#supply--demand)
4. [Price Multiplier](#price-multiplier)
5. [Time-Based Decay](#time-based-decay)
6. [Commands](#commands)
7. [Developer API](#developer-api)
8. [Trading Strategies](#trading-strategies)
9. [Best Practices](#best-practices)

---

## Overview

The Market System provides **dynamic pricing** for all tradeable items in ScheduleMC. Prices fluctuate in real time based on supply and demand, driven by actual player transactions. Every purchase drives prices up, every sale drives prices down, and time-based decay gradually normalizes prices back to baseline.

### Key Features

- **Dynamic Pricing** - Prices change with every transaction
- **Supply & Demand Simulation** - Levels tracked from 0 to 100
- **Price Multiplier Range** - 0.5x to 2.0x of base price
- **Purchase Recording** - `recordPurchase` increases demand, raises prices
- **Sale Recording** - `recordSale` increases supply, lowers prices
- **Time-Based Decay** - Prices gradually normalize when no transactions occur
- **4 Commands** - Check prices, trends, statistics, and top items
- **50+ Tracked Items** - All production goods, resources, and vehicles

---

## Price Mechanics

### How Prices Change

The market system tracks every transaction and adjusts prices accordingly:

```
recordPurchase(item, amount):
  -> Demand increases
  -> Price rises
  -> More purchases = higher prices

recordSale(item, amount):
  -> Supply increases
  -> Price drops
  -> More sales = lower prices
```

### Base Price vs Market Price

Every item has a **base price** (set by admins) and a **market price** (calculated dynamically):

```
Market Price = Base Price x Price Multiplier

Where Price Multiplier is determined by supply and demand levels.

Example:
  Base Price: 100
  Price Multiplier: 1.35 (high demand)
  Market Price: 100 x 1.35 = 135
```

### Price Display Format

```
Item: Virginia Cigar
Current Price: 45
24h Change: +5 (+12.5%)
7-Day Average: 42
Status: Rising
```

---

## Supply & Demand

### Supply and Demand Levels

Both supply and demand are tracked on a **0 to 100** scale for each item:

| Level | Range | Description |
|-------|-------|-------------|
| Very Low | 0-20 | Almost no activity |
| Low | 21-40 | Below average activity |
| Medium | 41-60 | Balanced |
| High | 61-80 | Above average activity |
| Very High | 81-100 | Extreme activity |

### Demand Factors

**High Demand (pushes prices up):**
- Many players buying the item
- Low inventory in shops
- Seasonal demand spikes
- New content releases creating interest

**Low Demand (pushes prices down):**
- Few players buying
- High shop inventory
- Market saturation
- Players switching to alternatives

### Supply Factors

**High Supply (pushes prices down):**
- Many players producing and selling
- Easy to manufacture
- Abundant resources available

**Low Supply (pushes prices up):**
- Few players producing
- Hard to manufacture
- Rare ingredients required
- Time-consuming production process

### Price Formula

```
Market Price = Base Price x Demand Multiplier x Supply Multiplier

Example:
  Base Price: 40
  Demand Multiplier: 1.2 (high demand)
  Supply Multiplier: 0.9 (moderate supply)
  Market Price: 40 x 1.2 x 0.9 = 43.20
```

---

## Price Multiplier

### Range

The price multiplier is bounded between **0.5** and **2.0**:

| Multiplier | Effect | Condition |
|-----------|--------|-----------|
| 0.5x | 50% of base price (floor) | Extreme oversupply, no demand |
| 0.75x | 25% discount | High supply, low demand |
| 1.0x | Base price | Balanced market |
| 1.25x | 25% markup | Low supply, high demand |
| 1.5x | 50% markup | Very low supply, very high demand |
| 2.0x | Double base price (ceiling) | Extreme undersupply, extreme demand |

### How Multiplier Changes

```
Player buys 10 Diamonds:
  -> recordPurchase(diamond, 10)
  -> Demand level increases
  -> Price multiplier rises
  -> Diamond price goes up

Player sells 20 Diamonds:
  -> recordSale(diamond, 20)
  -> Supply level increases
  -> Price multiplier drops
  -> Diamond price goes down
```

### Example Price Trajectory

```
Diamond (Base Price: 100):

  Day 1: Multiplier 1.0 -> Price 100 (balanced)
  Day 2: 50 purchased   -> Multiplier 1.15 -> Price 115
  Day 3: 80 purchased   -> Multiplier 1.35 -> Price 135
  Day 4: 20 sold        -> Multiplier 1.25 -> Price 125
  Day 5: No activity    -> Multiplier 1.20 -> Price 120 (decay)
  Day 6: No activity    -> Multiplier 1.15 -> Price 115 (decay)
  Day 7: 100 sold       -> Multiplier 0.85 -> Price 85
```

---

## Time-Based Decay

### How Decay Works

When no transactions occur for an item, the price multiplier **gradually decays** back toward 1.0 (the base price).

```
Decay Behavior:
  - Applies when no purchases or sales are recorded
  - Multiplier moves toward 1.0 over time
  - Prevents prices from staying artificially high or low indefinitely
  - Rate is configurable

Example:
  Multiplier: 1.50 (50% above base)
  No transactions for 1 day -> 1.40
  No transactions for 2 days -> 1.30
  No transactions for 3 days -> 1.20
  ...continues until reaching 1.0
```

### Decay vs Active Trading

```
Active Market (frequent trades):
  -> Supply and demand constantly updated
  -> Decay has minimal effect
  -> Prices driven by real player activity

Inactive Market (no trades):
  -> Decay gradually normalizes prices
  -> Prices return to base over time
  -> Prevents stale extreme prices
```

---

## Commands

The `/market` command provides **4 subcommands** for market information:

### View Prices

```bash
/market prices
```

**Output:**
```
MARKET PRICES

Tobacco Products:
  Virginia Cigar: 45 (+5)
  Burley Cigar: 40 (stable)
  Oriental Cigar: 50 (+3)
  Havana Cigar: 55 (+8)

Cannabis Products:
  Indica Cured Bud: 150 (+10)
  Sativa Cured Bud: 145 (+8)
  Cannabis Hash: 200 (+15)
  Cannabis Oil: 180 (-8)

Resources:
  Diamond: 100 (stable)
  Gold Ingot: 80 (+2)
  Emerald: 120 (+5)

(Showing 25/50 items)
Use /market prices <category> for more
```

### View Trends

```bash
/market trends
```

**Output:**
```
MARKET TRENDS (24h)

TOP 5 RISING:
  1. Heroin (Very Good): 800 (+50, +6.7%)
  2. Meth Crystal (VG): 650 (+30, +4.8%)
  3. Cannabis Hash: 200 (+15, +8.1%)
  4. Indica Bud: 150 (+10, +7.1%)
  5. MDMA Pills: 120 (+8, +7.1%)

TOP 5 FALLING:
  1. Cocaine (VG): 300 (-20, -6.3%)
  2. Crack: 180 (-12, -6.3%)
  3. Morphine: 400 (-15, -3.6%)
  4. Cannabis Oil: 180 (-8, -4.3%)
  5. Cigarettes: 25 (-2, -7.4%)
```

### View Statistics

```bash
/market stats
```

**Output:**
```
MARKET STATISTICS

Items Tracked: 50
Average Price: 285
Market Cap: ~14,250 (all items)

24-Hour Activity:
  Trading Volume: 4,500 items
  Total Value: 1,282,500
  Transactions: 3,247

Most Traded (24h):
  1. Virginia Cigar: 450 units
  2. Indica Bud: 380 units
  3. Cocaine: 250 units
  4. Diamond: 200 units
  5. Meth Crystal: 180 units

Market Volatility: MEDIUM
Price Stability Index: 72/100
```

### View Top Items

```bash
/market top
```

**Output:**
```
TOP 10 MOST EXPENSIVE

  1. LSD Sheet: 1,500
  2. Heroin (Legendary): 1,200
  3. Meth Crystal (Legendary): 900
  4. Heroin (Very Good): 800
  5. Meth Crystal (Very Good): 650
  6. MDMA Pills (Legendary): 500
  7. Cocaine (Legendary): 450
  8. Morphine: 400
  9. Opium (Legendary): 400
  10. Cocaine (Very Good): 300
```

---

## Developer API

### IMarketAPI Interface

External mods can access the market system through the `IMarketAPI` interface.

**Access:**
```java
IMarketAPI marketAPI = ScheduleMCAPI.getMarketAPI();
```

### Core Methods (v3.0.0+)

| Method | Description |
|--------|-------------|
| `getCurrentPrice(Item)` | Get current market price |
| `getBasePrice(Item)` | Get base price without dynamic adjustment |
| `recordPurchase(Item, int)` | Record a purchase (increases demand, raises price) |
| `recordSale(Item, int)` | Record a sale (increases supply, lowers price) |
| `getPriceMultiplier(Item)` | Get current multiplier (typically 0.5 - 2.0) |
| `getDemandLevel(Item)` | Get demand level (0-100) |
| `getSupplyLevel(Item)` | Get supply level (0-100) |
| `getAllPrices()` | Get map of all Item to Price entries |
| `setBasePrice(Item, double)` | Set base price (admin function) |
| `resetMarketData(Item)` | Reset market data for item (null for all items) |

### Extended Methods (v3.2.0+)

| Method | Description |
|--------|-------------|
| `getTopPricedItems(int)` | Get items sorted by price (highest first), limited by count |
| `getTopDemandItems(int)` | Get items sorted by demand (highest first), limited by count |
| `hasMarketData(Item)` | Check if an item has market tracking data |
| `getTrackedItemCount()` | Count of items with market data |
| `resetAllMarketData()` | Reset all market data for all items |

### Example Usage

```java
IMarketAPI marketAPI = ScheduleMCAPI.getMarketAPI();

// Get current price
double price = marketAPI.getCurrentPrice(Items.DIAMOND);
double basePrice = marketAPI.getBasePrice(Items.DIAMOND);
double multiplier = marketAPI.getPriceMultiplier(Items.DIAMOND);

// Record transactions
marketAPI.recordPurchase(Items.DIAMOND, 10);  // Price rises
marketAPI.recordSale(Items.DIAMOND, 5);       // Price drops

// Check supply and demand
int demand = marketAPI.getDemandLevel(Items.DIAMOND);  // 0-100
int supply = marketAPI.getSupplyLevel(Items.DIAMOND);  // 0-100

// Get all prices
Map<Item, Double> allPrices = marketAPI.getAllPrices();

// Get top items
List<Map.Entry<Item, Double>> topPriced = marketAPI.getTopPricedItems(10);
List<Map.Entry<Item, Integer>> topDemand = marketAPI.getTopDemandItems(10);

// Admin operations
marketAPI.setBasePrice(Items.DIAMOND, 120.0);   // Change base price
marketAPI.resetMarketData(Items.DIAMOND);        // Reset single item
marketAPI.resetAllMarketData();                  // Reset everything

// Check tracking
int tracked = marketAPI.getTrackedItemCount();
boolean hasData = marketAPI.hasMarketData(Items.DIAMOND);
```

**Thread Safety:** All methods are thread-safe through ConcurrentHashMap.

---

## Trading Strategies

### Buy Low, Sell High

```
Strategy:
  1. Monitor /market trends daily
  2. Buy items showing falling prices
  3. Hold until price rises
  4. Sell when showing rising prices

Example:
  Day 1: Cocaine at 300 (falling -20)
    -> BUY 50 units for 15,000

  Day 5: Cocaine at 340 (rising +40)
    -> SELL 50 units for 17,000

  Profit: 2,000 (13.3% return)
```

### Trend Following

```
Strategy:
  1. Find strong uptrends in /market trends
  2. Buy early in the trend
  3. Hold while trend continues
  4. Sell before reversal

Example:
  Heroin Price Trend:
    Day 1: 750 (+10) -> BUY
    Day 2: 770 (+20) -> HOLD
    Day 3: 800 (+30) -> SELL

  Profit: 50 per unit (6.7%)
```

### Production Arbitrage

```
Strategy:
  1. Check production costs for an item
  2. Compare to current market price
  3. Produce if market price exceeds cost
  4. Sell at peak prices

Example:
  Meth Crystal Production:
    Precursor Cost: 200
    Production Time: 40 minutes
    Market Price: 650
    Profit: 450 per unit (225% margin)
```

### Supply Analysis

```
Use /market stats to identify:

Oversupply (price falling):
  -> Avoid selling these items
  -> Consider buying at low prices

Undersupply (price rising):
  -> Produce and sell these items
  -> Premium prices available

Balanced:
  -> Stable prices, reliable income
  -> Good for consistent trading
```

---

## Best Practices

### For Traders

#### 1. Daily Market Check

```bash
# Morning routine
/market prices      # Check current prices
/market trends      # Identify opportunities
/market stats       # Understand market health
```

#### 2. Diversify Portfolio

```
Good Portfolio Distribution:
  40% High-volume items (cigars, cannabis)
  30% High-value items (heroin, LSD)
  20% Stable items (diamonds, gold)
  10% Speculative (trending items)
```

#### 3. Track Your Inventory

```
Maintain records of:
  Item purchased
  Purchase price
  Purchase date
  Target sell price
  Actual profit margin
```

### For Producers

#### 1. Monitor Production Costs

```
Calculate:
  Cost of Production = Materials + Time Value
  Target Price = Cost x 2.5 (150% profit margin)

Example:
  Tobacco Cigar Cost: 15
  Target Sell Price: 37.50
  Current Market: 45
  Decision: PRODUCE AND SELL (profitable)
```

#### 2. Quality Matters

```
Price by Quality:
  Poor: 50% of market price
  Good: 80% of market price
  Very Good: 100% of market price
  Legendary: 150% of market price

Focus on high quality for maximum profit.
```

### For Shop Owners

#### 1. Competitive Pricing

```bash
/market prices      # Check market before setting prices

Market Price: 45
Your Price: 43 (slightly below market)
Result: More sales volume, more customers
```

#### 2. Stock Popular Items

```
High-Demand Items (stock 500+):
  Virginia Cigars, Indica Buds, Diamonds

Low-Demand Items (stock 100):
  Cigarettes, Crack, Poor quality items
```

#### 3. Adjust Prices Dynamically

```
Update shop prices based on:
  Market trends (rising = raise prices)
  Inventory levels (overstocked = lower prices)
  Competition (undercut competitors slightly)
  Demand signals from /market stats
```

---

<div align="center">

**Market System - Complete Guide**

For related systems:
- [Economy System](Economy-System.md)
- [NPC System](NPC-System.md)
- [Warehouse System](Warehouse-System.md)
- [Smartphone System](Smartphone-System.md)

[Back to Wiki Home](../Home.md) | [All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
