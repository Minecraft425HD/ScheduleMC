# Market System

<div align="center">

**Dynamic Pricing with Supply & Demand Economics**

Real-time price tracking for 50+ items

[🏠 Back to Wiki Home](../Home.md) • [📋 Commands Reference](../Commands.md)

</div>

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Price Tracking](#price-tracking)
3. [Supply & Demand](#supply--demand)
4. [Market Commands](#market-commands)
5. [Price Trends](#price-trends)
6. [Market Statistics](#market-statistics)
7. [Trading Strategies](#trading-strategies)
8. [Best Practices](#best-practices)

---

## Overview

The Market System provides dynamic pricing for all tradeable items in ScheduleMC, with prices fluctuating based on supply and demand.

### Key Features

✅ **Dynamic Pricing** - Prices change based on trading volume
✅ **Supply & Demand** - Economic simulation
✅ **50+ Tracked Items** - All production and trade goods
✅ **Price History** - 7-day rolling average
✅ **Trend Analysis** - Rising/falling price indicators
✅ **Market Stats** - Trading volume, volatility
✅ **Real-time Updates** - Prices update after each transaction

---

## Price Tracking

### Tracked Item Categories

**Production Items:**
- Tobacco products (cigars, cigarettes)
- Cannabis products (buds, hash, oil)
- Hard drugs (cocaine, heroin, meth, LSD, MDMA)
- Mushroom products
- Coca derivatives

**Resources:**
- Diamonds, Gold, Emeralds
- Iron, Redstone, Lapis
- Building materials

**Vehicles & Parts:**
- Complete vehicles
- Engines, chassis, tires
- Vehicle modules

---

### Price Display Format

```
Item: Virginia Cigar
Current Price: 45€
24h Change: +5€ (↑ +12.5%)
7-Day Average: 42€
Status: Rising 📈
```

---

## Supply & Demand

### Price Mechanics

**Supply & Demand Formula:**
```
Base Price: 40€
Demand Multiplier: 1.2 (high demand)
Supply Multiplier: 0.9 (low supply)

Market Price = Base Price × Demand × Supply
Market Price = 40 × 1.2 × 0.9 = 43.20€
```

---

### Demand Factors

**High Demand (1.2-1.5x):**
- Many players buying
- Low inventory in shops
- Seasonal demand
- New content release

**Low Demand (0.7-0.9x):**
- Few players buying
- High shop inventory
- Market saturation

---

### Supply Factors

**Low Supply (1.1-1.4x):**
- Few players producing
- Hard to manufacture
- Rare ingredients
- Time-consuming process

**High Supply (0.8-0.95x):**
- Many players producing
- Easy to manufacture
- Abundant resources

---

## Market Commands

### View Prices

```bash
/market prices
```

**Output:**
```
═══ MARKET PRICES ═══

Tobacco Products:
- Virginia Cigar: 45€ (↑ +5€)
- Burley Cigar: 40€ (→ stable)
- Oriental Cigar: 50€ (↑ +3€)
- Havana Cigar: 55€ (↑ +8€)
- Premium Cigarettes: 25€ (↓ -2€)

Cannabis Products:
- Indica Cured Bud: 150€ (↑ +10€)
- Sativa Cured Bud: 145€ (↑ +8€)
- Hybrid Cured Bud: 140€ (→ stable)
- Ruderalis Bud: 120€ (↓ -5€)
- Cannabis Hash: 200€ (↑ +15€)
- Cannabis Oil: 180€ (↓ -8€)

Hard Drugs:
- Cocaine (Very Good): 300€ (↓ -20€)
- Crack: 180€ (↓ -12€)
- Heroin (Very Good): 800€ (↑ +50€)
- Morphine: 400€ (→ stable)
- Meth Crystal (Very Good): 650€ (↑ +30€)
- LSD Sheet: 1,500€ (→ stable)
- MDMA Pills (Good): 120€ (↑ +8€)

Resources:
- Diamond: 100€ (→ stable)
- Gold Ingot: 80€ (↑ +2€)
- Emerald: 120€ (↑ +5€)

(Showing 25/50 items)
Use /market prices <category> for more
```

---

### View Trends

```bash
/market trends
```

**Output:**
```
═══ MARKET TRENDS (24h) ═══

🔥 TOP 5 RISING:
1. Heroin (Very Good): 800€ (↑ +50€, +6.7%)
2. Meth Crystal (VG): 650€ (↑ +30€, +4.8%)
3. Cannabis Hash: 200€ (↑ +15€, +8.1%)
4. Indica Bud: 150€ (↑ +10€, +7.1%)
5. MDMA Pills: 120€ (↑ +8€, +7.1%)

📉 TOP 5 FALLING:
1. Cocaine (VG): 300€ (↓ -20€, -6.3%)
2. Crack: 180€ (↓ -12€, -6.3%)
3. Morphine: 400€ (↓ -15€, -3.6%)
4. Cannabis Oil: 180€ (↓ -8€, -4.3%)
5. Premium Cigarettes: 25€ (↓ -2€, -7.4%)
```

---

### Market Statistics

```bash
/market stats
```

**Output:**
```
═══ MARKET STATISTICS ═══

Items Tracked: 50
Average Price: 285€
Market Cap: ~14,250€ (all items)

24-Hour Activity:
- Trading Volume: 4,500 items
- Total Value: 1,282,500€
- Transactions: 3,247

Most Traded (24h):
1. Virginia Cigar: 450 units (12,375€)
2. Indica Bud: 380 units (57,000€)
3. Cocaine: 250 units (75,000€)
4. Diamond: 200 units (20,000€)
5. Meth Crystal: 180 units (117,000€)

Supply & Demand:
- High Demand: Heroin, Meth, LSD, Hash
- Medium Demand: Cannabis Bud, Cigars
- Low Demand: Cigarettes, Crack, Morphine

- Oversupply: Cocaine, Cannabis Oil
- Balanced: Diamonds, Gold
- Undersupply: Heroin, LSD, Quality items

Market Volatility: MEDIUM
Price Stability Index: 72/100
```

---

### Top Items

```bash
/market top
```

**Output:**
```
═══ TOP 10 MOST EXPENSIVE ═══

1. LSD Sheet: 1,500€
2. Heroin (Legendary): 1,200€
3. Meth Crystal (Legendary): 900€
4. Heroin (Very Good): 800€
5. Meth Crystal (Very Good): 650€
6. MDMA Pills (Legendary): 500€
7. Cocaine (Legendary): 450€
8. Morphine: 400€
9. Opium (Legendary): 400€
10. Cocaine (Very Good): 300€
```

---

## Price Trends

### Trend Indicators

**Symbols:**
- `↑` - Rising (price increasing)
- `↓` - Falling (price decreasing)
- `→` - Stable (price unchanged)
- `📈` - Strong uptrend
- `📉` - Strong downtrend

**Trend Strength:**
```
Change < 2%:   → Stable
Change 2-5%:   ↑/↓ Moderate
Change 5-10%:  ↑↑/↓↓ Strong
Change > 10%:  📈/📉 Very Strong
```

---

### Historical Trends

**7-Day Price History Example:**
```
Virginia Cigar (Last 7 Days):

Day 1: 38€
Day 2: 39€ (↑ +1€)
Day 3: 41€ (↑ +2€)
Day 4: 42€ (↑ +1€)
Day 5: 43€ (↑ +1€)
Day 6: 44€ (↑ +1€)
Day 7: 45€ (↑ +1€)

7-Day Change: +7€ (+18.4%)
Average: 41.71€
Trend: 📈 Strong Uptrend
```

---

## Market Statistics

### Trading Volume

**Volume Metrics:**
- **Daily Volume:** Total items traded per day
- **Value Traded:** Total € value of transactions
- **Transaction Count:** Number of trades

**Example:**
```
Today's Trading Volume:
Items: 4,500
Value: 1,282,500€
Transactions: 3,247
Average per Transaction: 395€
```

---

### Market Volatility

**Volatility Levels:**

| Level | Price Change | Description |
|-------|--------------|-------------|
| **LOW** | < 3% daily | Stable market |
| **MEDIUM** | 3-7% daily | Normal fluctuation |
| **HIGH** | 7-15% daily | Volatile market |
| **EXTREME** | > 15% daily | Crisis/boom |

**Current Market:**
```
Volatility: MEDIUM
Price Stability: 72/100
Risk Level: Moderate
```

---

### Supply Analysis

**Supply Indicators:**

| Status | Supply Level | Price Impact |
|--------|--------------|--------------|
| **Oversupply** | > 150% demand | Prices falling |
| **Balanced** | 90-110% demand | Stable prices |
| **Undersupply** | < 70% demand | Prices rising |
| **Critical** | < 30% demand | Extreme prices |

**Current Supply Status:**
```
Oversupply:
- Cocaine (Supply: 180% of demand)
- Cannabis Oil (Supply: 165%)
- Crack (Supply: 155%)

Undersupply:
- Heroin (Supply: 45% of demand)
- LSD (Supply: 50%)
- Quality Legendary items (Supply: 35%)
```

---

## Trading Strategies

### Buy Low, Sell High

**Strategy:**
1. Monitor `/market trends` daily
2. Buy items showing `↓` falling prices
3. Hold until price rises
4. Sell when showing `↑` rising prices

**Example:**
```
Day 1: Cocaine at 300€ (↓ -20€)
→ BUY 50 units for 15,000€

Day 5: Cocaine at 340€ (↑ +40€)
→ SELL 50 units for 17,000€

Profit: 2,000€ (13.3% return)
```

---

### Trend Following

**Strategy:**
1. Find strong uptrends (📈)
2. Buy early in trend
3. Ride the wave
4. Sell before reversal

**Example:**
```
Heroin Price Trend:
Day 1: 750€ (↑ +10€) - BUY
Day 2: 770€ (↑ +20€) - HOLD
Day 3: 800€ (↑ +30€) - SELL

Buy: 750€
Sell: 800€
Profit: 50€ per unit (6.7%)
```

---

### Production Arbitrage

**Strategy:**
1. Check production costs
2. Compare to market price
3. Produce if profitable
4. Sell at peak prices

**Example:**
```
Meth Crystal Production:
- Precursor Cost: 200€
- Time: 40 minutes
- Market Price: 650€
- Profit: 450€ (225% margin)

ROI: 225% per batch
Hourly Rate: 337.50€/hour (1.5 batches)
```

---

### Seasonal Trading

**Strategy:**
1. Identify seasonal patterns
2. Stock up before high demand
3. Sell during peak season
4. Repeat annually

**Example Seasons:**
```
High Cigar Demand:
- Winter months (Nov-Feb)
- Price: +15-20% above average

Low Demand:
- Summer months (Jun-Aug)
- Price: -10-15% below average

Strategy:
Buy in Summer at 35€
Sell in Winter at 55€
Profit: 20€ per unit (57%)
```

---

## Best Practices

### For Traders

#### 1. Daily Market Check
```bash
# Morning routine
/market prices      # Check current prices
/market trends     # Identify opportunities
/market stats      # Understand market health
```

---

#### 2. Track Your Inventory
```
Maintain Spreadsheet:
- Item purchased
- Purchase price
- Purchase date
- Target sell price
- Profit margin
```

---

#### 3. Diversify Portfolio
```
Good Portfolio Distribution:
- 40% High-volume items (cigars, cannabis)
- 30% High-value items (heroin, LSD)
- 20% Stable items (diamonds, gold)
- 10% Speculative (new items, trends)
```

---

#### 4. Set Price Alerts
```
Mental alerts for:
- Heroin < 750€ → BUY
- Meth > 700€ → SELL
- LSD < 1,400€ → BUY
- Cocaine > 350€ → SELL
```

---

### For Producers

#### 1. Monitor Production Costs
```
Calculate:
Cost of Production = Materials + Time Value
Target Price = Cost × 2.5 (150% profit margin)

Example:
Tobacco Cigar Cost: 15€
Target Sell Price: 37.50€
Current Market: 45€
Decision: PRODUCE & SELL ✓
```

---

#### 2. Time Market Entry
```
Best Times to Sell:
- Weekend peak hours
- After major updates
- During high demand seasons
- When trending ↑
```

---

#### 3. Quality Matters
```
Price by Quality:
- Poor: 50% of market price
- Good: 80% of market price
- Very Good: 100% of market price
- Legendary: 150% of market price

Focus on quality for max profit
```

---

### For Shop Owners

#### 1. Competitive Pricing
```bash
# Check market before setting prices
/market prices

# Price competitively:
Market Price: 45€
Your Price: 43€ (slightly below market)
Result: More sales volume
```

---

#### 2. Stock Popular Items
```
High-Demand Items (Stock 500+):
- Virginia Cigars
- Indica Buds
- Diamonds
- Meth Crystals

Low-Demand Items (Stock 100):
- Cigarettes
- Crack
- Poor quality items
```

---

#### 3. Dynamic Pricing
```
Adjust prices based on:
- Market trends
- Inventory levels
- Competition
- Demand signals

Update daily with /market prices info
```

---

<div align="center">

**Market System - Complete Guide**

For related systems:
- [💰 Economy System](Economy-System.md)
- [🤖 NPC System](NPC-System.md)
- [🏪 Warehouse System](Warehouse-System.md)

[🏠 Back to Wiki Home](../Home.md) • [📋 All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
