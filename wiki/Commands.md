# Command Reference - Complete Guide

<div align="center">

**All 161+ Commands in ScheduleMC**

Comprehensive reference for players and administrators

[🏠 Back to Wiki Home](Home.md)

</div>

---

## 📋 Table of Contents

1. [Quick Reference](#quick-reference)
2. [Command Syntax](#command-syntax)
3. [Plot & Property Management](#1-plot--property-management) (47 commands)
4. [Economy & Banking](#2-economy--banking) (28 commands)
5. [NPC & Population](#3-npc--population) (23 commands)
6. [Crime & Justice](#4-crime--justice) (8 commands)
7. [Medical & Respawn](#5-medical--respawn) (3 commands)
8. [Utilities & Resources](#6-utilities--resources) (15 commands)
9. [Agriculture & Production](#7-agriculture--production) (3 commands)
10. [Market & Economy](#8-market--economy) (4 commands)
11. [Player Progress & Tutorial](#9-player-progress--tutorial) (7 commands)
12. [System Administration](#10-system-administration) (5 commands)
13. [Permission Levels](#permission-levels)
14. [Common Use Cases](#common-use-cases)

---

## 🚀 Quick Reference

### Most Used Player Commands
```bash
/daily                  # Daily reward (50€ + streak bonus)
/money                  # Check balance
/pay <player> <amount>  # Send money
/plot wand              # Get plot selection tool
/plot buy               # Buy plot you're standing on
/tutorial               # Tutorial system
/market prices          # Check market prices
```

### Most Used Admin Commands
```bash
/npc spawn merchant <name>        # Spawn merchant NPC
/plot create residential <name> <price>  # Create residential plot
/money give <player> <amount>     # Give money to player
/warehouse add <item> <amount>    # Add warehouse inventory
/health                           # System diagnostics
```

---

## 📖 Command Syntax

### Argument Types

| Syntax | Meaning | Example |
|--------|---------|---------|
| `<required>` | Must be provided | `/plot create <name>` |
| `[optional]` | Can be omitted | `/money history [limit]` |
| `<type1|type2>` | Choose one option | `/npc movement <true|false>` |
| `...` | Multiple values allowed | - |

### Permission Indicators

| Icon | Permission Level |
|------|------------------|
| 👤 | **Player** - Available to all players |
| 🔧 | **Admin Level 2** - Requires admin privileges |
| ⚡ | **Mixed** - Some subcommands require admin |

---

# 1. Plot & Property Management

👤⚡ **Base Command:** `/plot`
**Permission:** Mixed (Player & Admin Level 2)
**Description:** Complete land and property management system

## Plot Creation & Selection

### `/plot wand`
👤 **Player Command**
**Description:** Get the plot selection tool (golden axe)

**Usage:**
```bash
/plot wand
# Left-click first corner, right-click second corner
```

**Tips:**
- Selection must be cuboid (rectangular box)
- Positions are saved until you create the plot
- Use F3+G to see chunk borders

---

### `/plot create <type> <name> [price]`
🔧 **Admin Command (Level 2)**
**Description:** Create a new plot from your current selection

**Types:**
- `residential` - Homes, apartments (requires price)
- `commercial` - Businesses, offices (requires price)
- `shop` - NPC shops with inventory system (no price)
- `public` - Parks, roads, spawn (no price)
- `government` - Town halls, prisons (no price)

**Usage:**
```bash
# Residential plot
/plot create residential Downtown_House_1 50000

# Commercial plot
/plot create commercial Office_Building_A 100000

# Shop plot (no price needed)
/plot create shop Main_Street_Shop

# Public area
/plot create public Central_Park

# Government building
/plot create government City_Hall
```

**Important:**
- Name cannot contain spaces (use underscores)
- Price is in € (euros)
- Selection must not overlap existing plots
- Shop/public/government plots are not for sale

---

## Plot Ownership

### `/plot buy [plotId]`
👤 **Player Command**
**Description:** Purchase a plot

**Usage:**
```bash
# Buy plot you're standing on
/plot buy

# Buy specific plot by ID
/plot buy Downtown_House_1
```

**Requirements:**
- Plot must be for sale
- You must have enough money
- You cannot own multiple plots (unless configured)

**Returns:**
- Money is transferred to previous owner
- You become the new owner
- Plot is removed from sale listings

---

### `/plot abandon`
👤 **Player Command**
**Description:** Abandon your plot and get 50% refund

**Usage:**
```bash
/plot abandon
```

**Warning:**
- ⚠️ You lose 50% of the plot creation cost
- All apartments in the plot are deleted
- All tenants are evicted
- Cannot be undone!

---

### `/plot setowner <player>`
🔧 **Admin Command (Level 2)**
**Description:** Manually transfer plot ownership

**Usage:**
```bash
/plot setowner Steve
```

**Use Cases:**
- Fix ownership issues
- Transfer plots without payment
- Reset abandoned plots

---

## Plot Information

### `/plot list`
👤 **Player Command**
**Description:** List all plots on the server

**Usage:**
```bash
/plot list
```

**Shows:**
- Plot ID
- Plot name
- Plot type
- Owner
- Sale status
- Price (if for sale)

---

### `/plot info`
👤 **Player Command**
**Description:** Show detailed info about the plot you're standing on

**Usage:**
```bash
/plot info
```

**Information Displayed:**
```
Plot: Downtown_House_1
Type: RESIDENTIAL
Owner: Steve
Size: 20x15x10 (3,000 blocks)
Created: 2024-01-15
Rating: ⭐⭐⭐⭐ (4.2/5.0)
For Sale: Yes (75,000€)
Rent: Available (500€/day)
Apartments: 2 units
```

---

### `/plot topplots`
👤 **Player Command**
**Description:** Show top-rated plots

**Usage:**
```bash
/plot topplots
```

**Shows:**
- Top 10 plots by rating
- Plot name, owner, rating

---

## Plot Customization

### `/plot name <name>`
👤 **Player Command (Owner only)**
**Description:** Set display name for your plot

**Usage:**
```bash
/plot name Steve's Mansion
```

**Notes:**
- Display name CAN contain spaces
- Does not change plot ID
- Used in /plot list

---

### `/plot description <text>`
👤 **Player Command (Owner only)**
**Description:** Set plot description

**Usage:**
```bash
/plot description Beautiful downtown property with ocean view
```

**Limits:**
- Max 200 characters
- Shown in /plot info

---

## Plot Trust System

### `/plot trust <player>`
👤 **Player Command (Owner only)**
**Description:** Give a player build permissions on your plot

**Usage:**
```bash
/plot trust Alex
```

**Permissions Granted:**
- Place blocks
- Break blocks
- Open containers (chests, furnaces)
- Interact with blocks
- Use doors and buttons

**Does NOT grant:**
- Ownership transfer
- Ability to trust others
- Ability to sell plot

---

### `/plot untrust <player>`
👤 **Player Command (Owner only)**
**Description:** Remove build permissions

**Usage:**
```bash
/plot untrust Alex
```

---

### `/plot trustlist`
👤 **Player Command (Owner only)**
**Description:** List all trusted players

**Usage:**
```bash
/plot trustlist
```

---

## Plot Sales & Rental

### `/plot sell <price>`
👤 **Player Command (Owner only)**
**Description:** Put your plot up for sale

**Usage:**
```bash
/plot sell 75000
```

**Notes:**
- Price is in € (euros)
- You can still use the plot while it's for sale
- Buyer pays the full price, you receive 100%

---

### `/plot unsell`
👤 **Player Command (Owner only)**
**Description:** Cancel sale listing

**Usage:**
```bash
/plot unsell
```

---

### `/plot transfer <player>`
👤 **Player Command (Owner only)**
**Description:** Transfer plot to another player for free

**Usage:**
```bash
/plot transfer Alex
```

**Important:**
- No money is exchanged
- Immediate transfer
- Cannot be undone
- Useful for gifts or partnerships

---

### `/plot rent <pricePerDay>`
👤 **Player Command (Owner only)**
**Description:** Offer your plot for rent

**Usage:**
```bash
/plot rent 500
```

**Rent System:**
- Price is per day (in-game day = 20 minutes)
- Security deposit = 2x daily rent
- Tenant can extend anytime
- Owner cannot evict during rental period

---

### `/plot rentcancel`
👤 **Player Command (Owner only)**
**Description:** Cancel rent offer (only if not currently rented)

**Usage:**
```bash
/plot rentcancel
```

---

### `/plot rentplot <days> [plotId]`
👤 **Player Command**
**Description:** Rent a plot

**Usage:**
```bash
# Rent plot you're standing on for 7 days
/plot rentplot 7

# Rent specific plot for 30 days
/plot rentplot 30 Downtown_House_1
```

**Payment:**
- Daily rent × days
- Security deposit (2× daily rent)
- Total charged upfront

**Example:**
```
Daily rent: 500€
Days: 7
Rent cost: 3,500€
Security deposit: 1,000€
Total charged: 4,500€
```

---

### `/plot rentextend <days>`
👤 **Player Command (Tenant only)**
**Description:** Extend your current rental

**Usage:**
```bash
/plot rentextend 7
```

**Notes:**
- No new security deposit needed
- Only pays daily rent × additional days

---

## Plot Rating

### `/plot rate <rating>`
👤 **Player Command**
**Description:** Rate a plot from 1-5 stars

**Usage:**
```bash
/plot rate 5
```

**Rules:**
- Can only rate plots you don't own
- Rating: 1-5 (integers only)
- Can change your rating anytime
- Average rating shown in /plot info

---

## Apartment System

### `/plot apartment wand`
👤 **Player Command (Plot Owner only)**
**Description:** Get apartment selection tool

**Usage:**
```bash
/plot apartment wand
# Must be inside your plot
# Select apartment area with left/right clicks
```

---

### `/plot apartment create <name> <monthlyRent>`
👤 **Player Command (Plot Owner only)**
**Description:** Create an apartment unit in your plot

**Usage:**
```bash
/plot apartment create Penthouse_A 2000
```

**Requirements:**
- Must own the plot
- Must have selection with apartment wand
- Selection must be inside your plot
- Name cannot contain spaces

**Apartment Features:**
- Protected from other tenants
- Monthly rent auto-charged
- Security deposit system
- Owner can evict tenants

---

### `/plot apartment delete <apartmentId>`
👤 **Player Command (Plot Owner only)**
**Description:** Delete an apartment

**Usage:**
```bash
/plot apartment delete apt_1
```

**Warning:**
- Evicts current tenant (no deposit refund)
- Cannot be undone

---

### `/plot apartment list`
👤 **Player Command**
**Description:** List all apartments in current plot

**Usage:**
```bash
/plot apartment list
```

**Shows:**
```
Apartment: Penthouse_A (apt_1)
Monthly Rent: 2,000€
Status: Rented to Alex
Lease Ends: 2024-02-15
```

---

### `/plot apartment info <apartmentId>`
👤 **Player Command**
**Description:** Show detailed apartment info

**Usage:**
```bash
/plot apartment info apt_1
```

---

### `/plot apartment rent <apartmentId> [days]`
👤 **Player Command**
**Description:** Rent an apartment (default 30 days)

**Usage:**
```bash
# Rent for 30 days (default)
/plot apartment rent apt_1

# Rent for 60 days
/plot apartment rent apt_1 60
```

**Payment:**
- Monthly rent × (days/30)
- Security deposit = 1 month rent
- Auto-charged monthly

---

### `/plot apartment leave`
👤 **Player Command (Tenant only)**
**Description:** Leave your current apartment

**Usage:**
```bash
/plot apartment leave
```

**Refund:**
- Security deposit returned
- No refund on remaining rental days

---

### `/plot apartment setrent <apartmentId> <monthlyRent>`
👤 **Player Command (Plot Owner only)**
**Description:** Change apartment rent

**Usage:**
```bash
/plot apartment setrent apt_1 2500
```

**Notes:**
- Only affects new tenants
- Existing tenants keep old rate until renewal

---

### `/plot apartment evict <apartmentId>`
👤 **Player Command (Plot Owner only)**
**Description:** Evict tenant immediately

**Usage:**
```bash
/plot apartment evict apt_1
```

**Warning:**
- Tenant loses security deposit
- Use only for violations

---

## Plot Admin Commands

### `/plot remove`
🔧 **Admin Command (Level 2)**
**Description:** Delete plot at your current position

**Usage:**
```bash
/plot remove
```

**Warning:**
- Deletes plot permanently
- Evicts all tenants
- No refunds issued

---

### `/plot reindex`
🔧 **Admin Command (Level 2)**
**Description:** Rebuild spatial index for plot lookups

**Usage:**
```bash
/plot reindex
```

**Use Cases:**
- Fix plot detection issues
- After large-scale plot changes
- Performance optimization

---

### `/plot debug`
🔧 **Admin Command (Level 2)**
**Description:** Show debug information

**Usage:**
```bash
/plot debug
```

**Shows:**
- Total plots
- Spatial index stats
- Performance metrics

---

### `/plot settype <type>`
🔧 **Admin Command (Level 2)**
**Description:** Change plot type

**Usage:**
```bash
/plot settype COMMERCIAL
```

**Types:**
- RESIDENTIAL
- COMMERCIAL
- SHOP
- PUBLIC
- GOVERNMENT

---

## Warehouse Integration (Plot)

### `/plot warehouse set`
🔧 **Admin Command (Level 2)**
**Description:** Link current plot to warehouse block you're looking at

**Usage:**
```bash
# Look at warehouse block, then:
/plot warehouse set
```

---

### `/plot warehouse clear`
🔧 **Admin Command (Level 2)**
**Description:** Unlink warehouse from current plot

**Usage:**
```bash
/plot warehouse clear
```

---

### `/plot warehouse info`
🔧 **Admin Command (Level 2)**
**Description:** Show warehouse info for current plot

**Usage:**
```bash
/plot warehouse info
```

---

# 2. Economy & Banking

## Money Management

### `/money`
👤 **Player Command**
**Description:** Show your current balance

**Usage:**
```bash
/money
```

**Output:**
```
💰 Balance: 12,450€
```

---

### `/pay <player> <amount>`
👤 **Player Command**
**Description:** Send money to another player

**Usage:**
```bash
/pay Alex 1000
```

**Transaction Fee:**
- 1% fee applied
- Example: /pay Alex 1000 → Alex receives 990€, you pay 1,000€

**Notes:**
- Player must be online
- Cannot pay yourself
- Minimum 1€

---

### `/money set <player> <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Set a player's balance to exact amount

**Usage:**
```bash
/money set Steve 100000
```

**Use Cases:**
- Fix economy bugs
- Reset player balance
- Testing

---

### `/money give <player> <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Add money to a player

**Usage:**
```bash
/money give Alex 5000
```

**Notes:**
- Adds to current balance
- No transaction fee
- Logged in transaction history

---

### `/money take <player> <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Remove money from a player

**Usage:**
```bash
/money take Steve 1000
```

**Notes:**
- Removes from current balance
- Can result in negative balance (use carefully)

---

### `/money history [limit]`
👤 **Player Command**
**Description:** Show your transaction history

**Usage:**
```bash
# Show last 10 transactions (default)
/money history

# Show last 20 transactions
/money history 20
```

**Output:**
```
Transaction History (Last 10):
1. +50€ - Daily Reward - 2024-01-15 10:30
2. -5,000€ - Plot Purchase (Downtown_House_1) - 2024-01-15 11:00
3. +200€ - Payment from Alex - 2024-01-15 12:15
```

---

### `/money history <player> [limit]`
🔧 **Admin Command (Level 2)**
**Description:** Show another player's transaction history

**Usage:**
```bash
/money history Steve 10
```

---

## Loan System

### `/loan apply <type>`
👤 **Player Command**
**Description:** Apply for a loan

**Loan Types:**

| Type | Amount | Interest | Duration | Total Repayment |
|------|--------|----------|----------|-----------------|
| SMALL | 5,000€ | 10% | 14 days | 5,500€ |
| MEDIUM | 25,000€ | 15% | 28 days | 28,750€ |
| LARGE | 100,000€ | 20% | 56 days | 120,000€ |

**Usage:**
```bash
/loan apply SMALL
/loan apply MEDIUM
/loan apply LARGE
```

**Requirements:**
- Cannot have active loan
- Auto-payment on due date
- Interest calculated upfront

**Example:**
```bash
/loan apply MEDIUM
# Receive: 25,000€ immediately
# Owe: 28,750€ in 28 days
# Daily cost: ~1,027€
```

---

### `/loan info`
👤 **Player Command**
**Description:** Show active loan info or available loans

**Usage:**
```bash
/loan info
```

**With Active Loan:**
```
Active Loan:
Type: MEDIUM
Amount: 25,000€
Interest: 15% (3,750€)
Total Due: 28,750€
Due Date: 2024-02-12
Days Remaining: 14
```

**Without Active Loan:**
```
Available Loans:
SMALL: 5,000€ @ 10% (14 days)
MEDIUM: 25,000€ @ 15% (28 days)
LARGE: 100,000€ @ 20% (56 days)
```

---

### `/loan repay`
👤 **Player Command**
**Description:** Fully repay your loan early

**Usage:**
```bash
/loan repay
```

**Benefits:**
- No early repayment penalty
- Can take new loan immediately
- Saves interest if repaid early (no partial interest refund, but stops accrual)

**Requirements:**
- Must have full repayment amount
- Cannot partially repay

---

## Savings System

### `/savings create <amount>`
👤 **Player Command**
**Description:** Create a savings account

**Usage:**
```bash
/savings create 10000
```

**Features:**
- 5% interest per week
- Minimum deposit: 1,000€
- 4-week withdrawal lock
- Multiple accounts allowed

**Interest Calculation:**
```
Week 1: 10,000€ → 10,500€ (+500€)
Week 2: 10,500€ → 11,025€ (+525€)
Week 3: 11,025€ → 11,576€ (+551€)
Week 4: 11,576€ → 12,155€ (+579€)
```

---

### `/savings list`
👤 **Player Command**
**Description:** List all your savings accounts

**Usage:**
```bash
/savings list
```

**Output:**
```
Your Savings Accounts:
1. Account abc123
   Balance: 12,155€
   Created: 2024-01-15
   Locked Until: 2024-02-12

2. Account def456
   Balance: 5,200€
   Created: 2024-01-20
   Locked Until: 2024-02-17
```

---

### `/savings deposit <accountId> <amount>`
👤 **Player Command**
**Description:** Add money to savings account

**Usage:**
```bash
/savings deposit abc123 5000
```

**Notes:**
- No deposit limit
- Resets 4-week lock period
- Immediate deposit

---

### `/savings withdraw <accountId> <amount>`
👤 **Player Command**
**Description:** Withdraw money (after 4-week lock)

**Usage:**
```bash
/savings withdraw abc123 2000
```

**Requirements:**
- Account must be unlocked (4+ weeks old)
- Cannot withdraw more than balance

---

### `/savings forcewithdraw <accountId> <amount>`
👤 **Player Command**
**Description:** Emergency withdrawal with 10% penalty

**Usage:**
```bash
/savings forcewithdraw abc123 2000
```

**Penalty:**
- 10% fee on withdrawal amount
- Example: Withdraw 2,000€ → Receive 1,800€

**Use Cases:**
- Emergency cash needed
- Better than loan interest

---

### `/savings close <accountId>`
👤 **Player Command**
**Description:** Close savings account and withdraw all

**Usage:**
```bash
/savings close abc123
```

**Notes:**
- Same 4-week lock rules apply
- 10% penalty if closed early

---

## Recurring Payments

### `/autopay add <player> <amount> <intervalDays> <description>`
👤 **Player Command**
**Description:** Create recurring payment (standing order)

**Usage:**
```bash
/autopay add Alex 500 7 "Weekly rent"
```

**Parameters:**
- `<player>` - Recipient
- `<amount>` - Payment amount (€)
- `<intervalDays>` - How often (real days)
- `<description>` - What it's for

**Examples:**
```bash
# Weekly rent to landlord
/autopay add Steve 2000 7 "Apartment rent"

# Monthly subscription
/autopay add Alex 1000 30 "Shop investment"

# Daily payment
/autopay add Bob 100 1 "Daily fee"
```

---

### `/autopay list`
👤 **Player Command**
**Description:** List all your recurring payments

**Usage:**
```bash
/autopay list
```

**Output:**
```
Your Recurring Payments:
1. Payment abc123
   To: Alex
   Amount: 500€
   Interval: 7 days
   Description: Weekly rent
   Next Payment: 2024-01-22
   Status: Active

2. Payment def456
   To: Steve
   Amount: 1,000€
   Interval: 30 days
   Description: Monthly fee
   Next Payment: 2024-02-15
   Status: Paused
```

---

### `/autopay pause <paymentId>`
👤 **Player Command**
**Description:** Pause a recurring payment

**Usage:**
```bash
/autopay pause abc123
```

**Notes:**
- Stops future payments
- Can resume anytime
- No cancellation fee

---

### `/autopay resume <paymentId>`
👤 **Player Command**
**Description:** Resume a paused payment

**Usage:**
```bash
/autopay resume abc123
```

**Notes:**
- Next payment scheduled based on interval
- No catch-up payments

---

### `/autopay delete <paymentId>`
👤 **Player Command**
**Description:** Delete recurring payment permanently

**Usage:**
```bash
/autopay delete abc123
```

**Warning:**
- Cannot be undone
- Create new autopay if needed later

---

## Daily Rewards

### `/daily`
👤 **Player Command**
**Description:** Claim daily reward

**Usage:**
```bash
/daily
```

**Reward Structure:**
- Base reward: 50€
- Streak bonus: +50€ per day (max 30 days)
- Max daily reward: 300€ (day 30+)

**Streak Calculation:**
```
Day 1:  50€ + 0€ = 50€
Day 2:  50€ + 50€ = 100€
Day 3:  50€ + 100€ = 150€
...
Day 30: 50€ + 250€ = 300€
Day 31: 50€ + 250€ = 300€ (capped)
```

**Rules:**
- Must claim daily (resets after 48 hours)
- Missing a day resets streak to 0
- Time-based (real-world days, not in-game)

---

### `/daily streak`
👤 **Player Command**
**Description:** Show your streak statistics

**Usage:**
```bash
/daily streak
```

**Output:**
```
Daily Reward Streak:
Current Streak: 15 days
Longest Streak: 22 days
Total Claims: 87
Next Reward: 200€ (50€ base + 150€ bonus)
Claim Available: Yes
```

---

## State Treasury

### `/state balance`
🔧 **Admin Command (Level 2)**
**Description:** Show state account balance

**Usage:**
```bash
/state balance
```

**State Account Uses:**
- Warehouse delivery payments
- NPC salaries (if configured)
- Hospital fees
- Government services

---

### `/state deposit <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Deposit money into state account

**Usage:**
```bash
/state deposit 10000
```

**Use Cases:**
- Fund government services
- Prepare for NPC salaries
- Economic stimulus

---

### `/state withdraw <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Withdraw from state account

**Usage:**
```bash
/state withdraw 5000
```

**Notes:**
- Can result in negative balance
- Monitor with /state balance

---

## Shop Investment

### `/shopinvest list`
👤 **Player Command**
**Description:** List all shops available for investment

**Usage:**
```bash
/shopinvest list
```

**Output:**
```
Available Shops:
1. Downtown Shop (downtown_shop)
   Shares Available: 50/100
   Share Price: 1,000€
   Dividend Rate: 5% weekly

2. Main Street Market (main_market)
   Shares Available: 75/100
   Share Price: 1,000€
   Dividend Rate: 5% weekly
```

---

### `/shopinvest info <shopId>`
👤 **Player Command**
**Description:** Show detailed shop info

**Usage:**
```bash
/shopinvest info downtown_shop
```

**Output:**
```
Shop: Downtown Shop (downtown_shop)
Total Shares: 100
Available: 50
Share Price: 1,000€

Shareholders:
- Steve: 25 shares (25%)
- Alex: 15 shares (15%)
- Bob: 10 shares (10%)

Dividend: 5% weekly
Last Payment: 2024-01-15
Next Payment: 2024-01-22
```

---

### `/shopinvest buy <shopId> <shares>`
👤 **Player Command**
**Description:** Buy shop shares

**Usage:**
```bash
/shopinvest buy downtown_shop 10
```

**Cost:**
- 1,000€ per share
- Example: 10 shares = 10,000€

**Returns:**
- 5% weekly dividend on share value
- Example: 10 shares = 10,000€ value → 500€/week

**Limits:**
- Max 99 shares per player per shop
- Must have available shares

---

### `/shopinvest sell <shopId> <shares>`
👤 **Player Command**
**Description:** Sell your shares

**Usage:**
```bash
/shopinvest sell downtown_shop 5
```

**Refund:**
- 75% of purchase price
- Example: 5 shares → 3,750€ refund (75% of 5,000€)

**Notes:**
- 25% fee to prevent market manipulation
- Immediate sale

---

### `/shopinvest myshares`
👤 **Player Command**
**Description:** Show all your investments

**Usage:**
```bash
/shopinvest myshares
```

**Output:**
```
Your Shop Investments:
1. Downtown Shop: 10 shares (10%)
   Value: 10,000€
   Weekly Dividend: 500€

2. Main Market: 5 shares (5%)
   Value: 5,000€
   Weekly Dividend: 250€

Total Investment: 15,000€
Total Weekly Income: 750€
```

---

# 3. NPC & Population

### `/npc <name> info`
🔧 **Admin Command (Level 2)**
**Description:** Show comprehensive NPC information

**Usage:**
```bash
/npc Steve info
```

**Output:**
```
NPC: Steve
Type: MERCHANT
Status: Working
Position: 100, 64, 200 (downtown_shop)
Wallet: 5,450€

Schedule:
Work Start: 08:00
Work End: 17:00
Home Time: 22:00

Inventory:
Slot 0: Diamond (x16)
Slot 1: Gold Ingot (x32)

Warehouse: Linked (warehouse_1)
Shop: downtown_shop
Movement: Enabled
Speed: 0.3
```

---

## NPC Movement & Behavior

### `/npc <name> movement <true|false>`
🔧 **Admin Command (Level 2)**
**Description:** Enable or disable NPC movement

**Usage:**
```bash
# Enable movement
/npc Steve movement true

# Disable movement (NPC stays in place)
/npc Steve movement false
```

**Use Cases:**
- Disable for stationary shopkeepers
- Enable for roaming merchants
- Troubleshooting pathfinding issues

---

### `/npc <name> speed <value>`
🔧 **Admin Command (Level 2)**
**Description:** Set NPC movement speed

**Usage:**
```bash
/npc Steve speed 0.5
```

**Range:**
- Min: 0.1 (very slow)
- Max: 1.0 (very fast)
- Default: 0.3 (walking pace)

**Tips:**
- 0.3 = Realistic walking
- 0.5 = Brisk walk
- 0.8+ = Running

---

## NPC Schedule Management

### `/npc <name> schedule workstart <time>`
🔧 **Admin Command (Level 2)**
**Description:** Set when NPC starts work

**Usage:**
```bash
/npc Steve schedule workstart 0800
```

**Time Format:**
- HHMM (24-hour)
- Examples: 0800 (8 AM), 1400 (2 PM), 2300 (11 PM)

**Behavior:**
- NPC travels to work location at this time
- Must have work location set

---

### `/npc <name> schedule workend <time>`
🔧 **Admin Command (Level 2)**
**Description:** Set when NPC finishes work

**Usage:**
```bash
/npc Steve schedule workend 1700
```

**Behavior:**
- NPC leaves work and goes to leisure/home
- Shop closes if NPC is merchant

---

### `/npc <name> schedule home <time>`
🔧 **Admin Command (Level 2)**
**Description:** Set when NPC goes home to sleep

**Usage:**
```bash
/npc Steve schedule home 2200
```

**Behavior:**
- NPC travels home
- NPC sleeps (no movement) from this time until 07:00

---

## NPC Leisure Locations

### `/npc <name> leisure add`
🔧 **Admin Command (Level 2)**
**Description:** Add current position as leisure location

**Usage:**
```bash
# Stand where you want leisure spot, then:
/npc Steve leisure add
```

**Leisure Behavior:**
- After work ends, before home time
- NPC randomly visits leisure locations
- Examples: park, bar, restaurant

**Limits:**
- Max 10 leisure locations per NPC

---

### `/npc <name> leisure remove <index>`
🔧 **Admin Command (Level 2)**
**Description:** Remove a leisure location

**Usage:**
```bash
/npc Steve leisure remove 0
```

**Index:**
- Use /npc Steve leisure list to see indices
- 0-based (0 = first location)

---

### `/npc <name> leisure list`
🔧 **Admin Command (Level 2)**
**Description:** List all leisure locations

**Usage:**
```bash
/npc Steve leisure list
```

**Output:**
```
Steve's Leisure Locations:
0: Central Park (120, 64, 180)
1: Downtown Bar (105, 65, 195)
2: Coffee Shop (115, 64, 200)
```

---

### `/npc <name> leisure clear`
🔧 **Admin Command (Level 2)**
**Description:** Remove all leisure locations

**Usage:**
```bash
/npc Steve leisure clear
```

---

## NPC Inventory Management

### `/npc <name> inventory`
🔧 **Admin Command (Level 2)**
**Description:** Show NPC inventory

**Usage:**
```bash
/npc Steve inventory
```

**Output:**
```
Steve's Inventory:
Slot 0: Diamond (x16)
Slot 1: Gold Ingot (x32)
Slot 2: Empty
...
Slot 8: Empty
```

---

### `/npc <name> inventory give <slot> <item>`
🔧 **Admin Command (Level 2)**
**Description:** Give an item to NPC

**Usage:**
```bash
/npc Steve inventory give 0 minecraft:diamond
```

**Parameters:**
- `<slot>` - Inventory slot (0-8)
- `<item>` - Item ID (e.g., minecraft:diamond)

**Notes:**
- Merchant NPCs sell from inventory
- Stack size = 64 (default)

---

### `/npc <name> inventory clear [slot]`
🔧 **Admin Command (Level 2)**
**Description:** Clear inventory

**Usage:**
```bash
# Clear entire inventory
/npc Steve inventory clear

# Clear specific slot
/npc Steve inventory clear 0
```

---

## NPC Wallet Management

### `/npc <name> wallet`
🔧 **Admin Command (Level 2)**
**Description:** Show NPC cash balance

**Usage:**
```bash
/npc Steve wallet
```

**Output:**
```
Steve's Wallet: 5,450€
```

---

### `/npc <name> wallet set <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Set NPC wallet to exact amount

**Usage:**
```bash
/npc Steve wallet set 10000
```

**Use Cases:**
- Fund merchant NPC
- Reset NPC economy
- Testing

---

### `/npc <name> wallet add <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Add money to NPC

**Usage:**
```bash
/npc Steve wallet add 5000
```

**Notes:**
- Adds to current balance
- NPCs use this to buy from players

---

### `/npc <name> wallet remove <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Remove money from NPC

**Usage:**
```bash
/npc Steve wallet remove 2000
```

**Notes:**
- Can result in negative balance
- Monitor with /npc <name> wallet

---

## NPC Warehouse Integration

### `/npc <name> warehouse set`
🔧 **Admin Command (Level 2)**
**Description:** Link NPC to warehouse

**Usage:**
```bash
# Look at warehouse block, then:
/npc Steve warehouse set
```

**Behavior:**
- NPC sells from warehouse inventory
- Unlimited stock if warehouse has items
- Warehouse must be in same plot as NPC's shop

---

### `/npc <name> warehouse clear`
🔧 **Admin Command (Level 2)**
**Description:** Unlink warehouse from NPC

**Usage:**
```bash
/npc Steve warehouse clear
```

---

### `/npc <name> warehouse info`
🔧 **Admin Command (Level 2)**
**Description:** Show warehouse linkage info

**Usage:**
```bash
/npc Steve warehouse info
```

**Output:**
```
Steve's Warehouse:
Linked: Yes
Warehouse ID: warehouse_1
Location: 100, 64, 200
Items Available: 12 types
```

---

# 4. Crime & Justice

## Prison System

### `/prison create <plotId>`
🔧 **Admin Command (Level 2)**
**Description:** Convert a plot into a prison

**Usage:**
```bash
/prison create prison_main
```

**Requirements:**
- Plot must be GOVERNMENT type
- Plot must exist
- One prison per plot

---

### `/prison addcell <cellNumber> <min> <max> [securityLevel]`
🔧 **Admin Command (Level 2)**
**Description:** Add a prison cell

**Usage:**
```bash
/prison addcell 1 100 50 10 110 55 15 3
```

**Parameters:**
- `<cellNumber>` - Cell ID (1-99)
- `<min>` - First corner (x y z)
- `<max>` - Second corner (x y z)
- `[securityLevel]` - 1-5 (optional, default 1)

**Security Levels:**
- 1: Minimum security (comfortable)
- 2: Low security
- 3: Medium security
- 4: High security
- 5: Maximum security (solitary)

**Example Setup:**
```bash
# Stand at first corner, note coordinates: 100 50 10
# Stand at second corner, note coordinates: 110 55 15
/prison addcell 1 100 50 10 110 55 15 3
```

---

### `/prison removecell <cellNumber>`
🔧 **Admin Command (Level 2)**
**Description:** Remove a prison cell

**Usage:**
```bash
/prison removecell 1
```

**Warning:**
- Releases any prisoner in that cell

---

### `/prison list`
🔧 **Admin Command (Level 2)**
**Description:** List all prisons on server

**Usage:**
```bash
/prison list
```

**Output:**
```
Prisons:
1. Prison: prison_main
   Plot: Government_Prison
   Cells: 5
   Inmates: 2
```

---

### `/prison cells`
🔧 **Admin Command (Level 2)**
**Description:** List all cells in default prison

**Usage:**
```bash
/prison cells
```

**Output:**
```
Prison Cells:
Cell 1: Security Level 3, Occupied (Steve)
Cell 2: Security Level 1, Empty
Cell 3: Security Level 5, Occupied (Alex)
Cell 4: Security Level 2, Empty
Cell 5: Security Level 4, Empty
```

---

### `/prison inmates`
🔧 **Admin Command (Level 2)**
**Description:** List all current prisoners

**Usage:**
```bash
/prison inmates
```

**Output:**
```
Current Inmates:
Steve - Cell 1 - 3 days remaining
Alex - Cell 3 - 7 days remaining
```

---

### `/prison status <player>`
🔧 **Admin Command (Level 2)**
**Description:** Show detailed prisoner status

**Usage:**
```bash
/prison status Steve
```

**Output:**
```
Prisoner: Steve
Cell: 1 (Security Level 3)
Sentence: 7 days
Time Served: 4 days
Remaining: 3 days
Bail: 5,000€
Reason: Assault (⭐⭐⭐)
```

---

### `/prison release <player>`
🔧 **Admin Command (Level 2)**
**Description:** Release a prisoner early

**Usage:**
```bash
/prison release Steve
```

**Effect:**
- Immediate release
- Teleported to hospital spawn
- Wanted level cleared

---

## Player Prison Commands

### `/bail`
👤 **Player Command**
**Description:** Pay bail to get out of prison

**Usage:**
```bash
/bail
```

**Bail Calculation:**
- Based on wanted level
- ⭐ 1 star = 1,000€
- ⭐⭐ 2 stars = 2,500€
- ⭐⭐⭐ 3 stars = 5,000€
- ⭐⭐⭐⭐ 4 stars = 10,000€
- ⭐⭐⭐⭐⭐ 5 stars = 20,000€

**Requirements:**
- Must be in prison
- Must have enough money

---

### `/jailtime`
👤 **Player Command**
**Description:** Check remaining jail time

**Usage:**
```bash
/jailtime
```

**Output:**
```
Jail Time Remaining: 3 days, 4 hours
Bail Amount: 5,000€
Security Level: 3
```

---

# 5. Medical & Respawn

### `/hospital setspawn`
🔧 **Admin Command (Level 2)**
**Description:** Set hospital respawn point

**Usage:**
```bash
# Stand where players should respawn, then:
/hospital setspawn
```

**Behavior:**
- Players respawn here on death
- Police bring arrested players here

---

### `/hospital setfee <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Set death/respawn fee

**Usage:**
```bash
/hospital setfee 500
```

**Fee:**
- Charged on death
- Paid from player wallet
- Goes to state account

**Example:**
```
Player dies
→ Respawn at hospital
→ 500€ fee charged
→ "You paid 500€ hospital fee"
```

---

### `/hospital info`
🔧 **Admin Command (Level 2)**
**Description:** Show hospital configuration

**Usage:**
```bash
/hospital info
```

**Output:**
```
Hospital Configuration:
Spawn Point: 100, 64, 200 (world)
Death Fee: 500€
Total Deaths: 47
Total Fees Collected: 23,500€
```

---

# 6. Utilities & Resources

## Utility System

### `/utility`
👤⚡ **Mixed Command**
**Description:** Show electricity and water consumption
**Aliases:** `/strom`, `/wasser`

**Usage:**
```bash
# Show utility for plot you're standing on
/utility

# Show utility for specific plot
/utility downtown_shop
```

**Output:**
```
Plot: downtown_shop
Electricity Usage: 450 kW/h
Water Usage: 120 L/h

Breakdown:
- Grow Lights: 300 kW/h
- Machines: 100 kW/h
- Fuel Station: 50 kW/h

Monthly Cost: 2,250€ (estimated)
```

---

### `/utility top`
👤 **Player Command**
**Description:** Show top 10 utility consumers

**Usage:**
```bash
/utility top
```

**Output:**
```
Top 10 Utility Consumers:
1. industrial_complex: 1,200 kW/h
2. downtown_shop: 450 kW/h
3. main_market: 380 kW/h
...
```

---

### `/utility scan`
🔧 **Admin Command (Level 2)**
**Description:** Scan plot for utility consumers

**Usage:**
```bash
/utility scan
```

**Behavior:**
- Scans all blocks in current plot
- Identifies grow lights, machines, etc.
- Updates utility consumption database

**Use Cases:**
- Fix incorrect utility readings
- After adding new machines

---

### `/utility stats`
👤 **Player Command**
**Description:** Show server-wide utility statistics

**Usage:**
```bash
/utility stats
```

**Output:**
```
Server Utility Statistics:
Total Electricity: 5,400 kW/h
Total Water: 1,200 L/h
Active Plots: 47
Average per Plot: 114.9 kW/h
```

---

### `/utility breakdown <plotId>`
👤 **Player Command**
**Description:** Show detailed consumption breakdown

**Usage:**
```bash
/utility breakdown downtown_shop
```

**Output:**
```
Utility Breakdown: downtown_shop

Electricity:
- Grow Lights (Premium): 300 kW/h (66.7%)
- Processing Machines: 100 kW/h (22.2%)
- Fuel Station: 50 kW/h (11.1%)

Water:
- Irrigation: 80 L/h (66.7%)
- Processing: 40 L/h (33.3%)

Total: 450 kW/h, 120 L/h
```

---

## Warehouse System

### `/warehouse info`
🔧 **Admin Command (Level 2)**
**Description:** Show warehouse information

**Usage:**
```bash
# Look at warehouse block, then:
/warehouse info
```

**Output:**
```
Warehouse Information:
ID: warehouse_1
Location: 100, 64, 200
Plot: downtown_shop

Linked Shop: downtown_shop
Linked NPCs: Steve, Alex

Inventory: 12/32 slots used
Items:
- Diamond: 512
- Gold Ingot: 1024
- Emerald: 256
...

Last Delivery: 2024-01-15
Next Delivery: 2024-01-18 (3 days)
```

---

### `/warehouse add <item> <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Add items to warehouse

**Usage:**
```bash
/warehouse add minecraft:diamond 64
```

**Notes:**
- Max 32 different item types
- Max 1,024 per item type
- Exceeding limits shows error

---

### `/warehouse remove <item> <amount>`
🔧 **Admin Command (Level 2)**
**Description:** Remove items from warehouse

**Usage:**
```bash
/warehouse remove minecraft:diamond 32
```

**Notes:**
- Cannot remove more than available
- Items are deleted (not given to player)

---

### `/warehouse clear`
🔧 **Admin Command (Level 2)**
**Description:** Clear all items from warehouse

**Usage:**
```bash
/warehouse clear
```

**Warning:**
- Deletes ALL items
- Cannot be undone
- Confirm prompt shown

---

### `/warehouse setshop <shopId>`
🔧 **Admin Command (Level 2)**
**Description:** Link warehouse to shop plot

**Usage:**
```bash
/warehouse setshop downtown_shop
```

**Behavior:**
- NPCs in shop sell from warehouse
- Unlimited stock if warehouse has items

---

### `/warehouse deliver`
🔧 **Admin Command (Level 2)**
**Description:** Manually trigger delivery

**Usage:**
```bash
/warehouse deliver
```

**Delivery System:**
- Auto-delivery every 3 days
- Restocks warehouse to configured levels
- Costs paid from state account

---

### `/warehouse reset`
🔧 **Admin Command (Level 2)**
**Description:** Reset delivery timer

**Usage:**
```bash
/warehouse reset
```

**Use Cases:**
- Force immediate delivery
- Fix delivery schedule

---

# 7. Agriculture & Production

### `/tobacco info`
👤 **Player Command**
**Description:** Show info about tobacco pot

**Usage:**
```bash
# Look at tobacco pot, then:
/tobacco info
```

**Output:**
```
Tobacco Pot Information:
Pot Type: GOLDEN (+50% growth, +1 quality)
Strain: Virginia
Growth Stage: Flowering (Stage 3/4)
Growth Progress: 75%
Quality: Very Good

Modifiers:
- Watered: Yes (+25% growth)
- Fertilized: Yes (+15% growth)
- Growth Booster: Active (+30% growth)
- Quality Booster: Active
- Light Level: 15 (optimal)

Estimated Harvest: 5 minutes
Expected Yield: 3-4 leaves (Very Good quality)
```

---

### `/tobacco give <item>`
🔧 **Admin Command (Level 2)**
**Description:** Give tobacco-related items

**Usage:**
```bash
/tobacco give virginia_seeds
```

**Available Items:**
- Seeds: `virginia_seeds`, `burley_seeds`, `oriental_seeds`, `havana_seeds`
- Tools: `fertilizer`, `growth_booster`, `quality_booster`, `watering_can`

**Examples:**
```bash
/tobacco give virginia_seeds
/tobacco give fertilizer
/tobacco give quality_booster
```

---

### `/tobacco stats`
👤 **Player Command**
**Description:** Show your tobacco farming statistics

**Usage:**
```bash
/tobacco stats
```

**Output:**
```
Tobacco Farming Statistics:
Total Harvests: 47
Total Leaves: 235

By Strain:
- Virginia: 120 leaves (51%)
- Burley: 80 leaves (34%)
- Oriental: 25 leaves (11%)
- Havana: 10 leaves (4%)

By Quality:
- Legendary: 12 (5%)
- Very Good: 94 (40%)
- Good: 118 (50%)
- Poor: 11 (5%)

Best Quality Streak: 7 consecutive Very Good
```

---

# 8. Market & Economy

### `/market prices`
👤 **Player Command**
**Description:** Show all current market prices

**Usage:**
```bash
/market prices
```

**Output:**
```
Market Prices:

Tobacco Products:
- Virginia Cigar: 45€ (↑ +5€ from yesterday)
- Burley Cigar: 40€ (→ stable)
- Premium Cigarettes: 25€ (↓ -2€)

Cannabis Products:
- Indica Cured Bud: 150€ (↑ +10€)
- Sativa Hash: 200€ (↑ +15€)

Hard Drugs:
- Cocaine: 300€ (↓ -20€)
- Heroin: 800€ (↑ +50€)
- LSD Sheet: 1,500€ (→ stable)
- Meth Crystal: 650€ (↑ +30€)

(Showing 15/50 items - use /market prices <category> for more)
```

---

### `/market trends`
👤 **Player Command**
**Description:** Show top 5 rising and falling items

**Usage:**
```bash
/market trends
```

**Output:**
```
Market Trends (Last 24 Hours):

🔥 Rising:
1. Heroin: 800€ (+50€, +6.7%)
2. Meth Crystal: 650€ (+30€, +4.8%)
3. LSD Sheet: 1,500€ (+40€, +2.7%)
4. Indica Bud: 150€ (+10€, +7.1%)
5. MDMA Pills: 120€ (+8€, +7.1%)

📉 Falling:
1. Cocaine: 300€ (-20€, -6.3%)
2. Crack: 180€ (-12€, -6.3%)
3. Morphine: 400€ (-15€, -3.6%)
4. Cannabis Oil: 180€ (-8€, -4.3%)
5. Premium Cigarettes: 25€ (-2€, -7.4%)
```

---

### `/market stats`
👤 **Player Command**
**Description:** Show market statistics

**Usage:**
```bash
/market stats
```

**Output:**
```
Market Statistics:

Total Items Tracked: 50
Average Price: 285€
Market Volatility: Medium

24-Hour Trading Volume: 4,500 items
Total Value Traded: 1,282,500€

Most Traded:
1. Virginia Cigar: 450 units
2. Indica Bud: 380 units
3. Cocaine: 250 units

Supply & Demand:
- High Demand: Heroin, Meth, LSD
- Low Demand: Cigarettes, Crack
- Oversupply: Cocaine, Cannabis Oil
```

---

### `/market top`
👤 **Player Command**
**Description:** Show top 10 most expensive items

**Usage:**
```bash
/market top
```

**Output:**
```
Top 10 Most Expensive Items:

1. LSD Sheet: 1,500€
2. Heroin (Legendary): 1,200€
3. Meth Crystal (Legendary): 900€
4. Heroin (Very Good): 800€
5. Meth Crystal (Very Good): 650€
6. MDMA Pills (Legendary): 500€
7. Cocaine (Legendary): 450€
8. Opium (Legendary): 400€
9. Morphine: 400€
10. Cocaine (Very Good): 300€
```

---

# 9. Player Progress & Tutorial

### `/tutorial`
👤 **Player Command**
**Description:** Show current tutorial step

**Usage:**
```bash
/tutorial
```

**Output:**
```
Tutorial Progress: Step 3/7

Current Step: Create Your First Plot
Objective: Use /plot wand and create a plot

Instructions:
1. Use /plot wand to get selection tool
2. Left-click first corner
3. Right-click second corner
4. Use /plot create residential "My_Home" 50000

Reward: 1,000€ upon completion

Progress: 2/7 steps completed (28.6%)
```

---

### `/tutorial start`
👤 **Player Command**
**Description:** Start tutorial from beginning

**Usage:**
```bash
/tutorial start
```

**7-Step Tutorial:**
1. Welcome & Economy Basics
2. Claim Daily Reward
3. Create Your First Plot
4. Set Up Production
5. Interact with NPCs
6. Understanding Crime System
7. Advanced Features

**Total Rewards:**
- 5,000€ bonus for completing all 7 steps
- Unlocks achievement

---

### `/tutorial next`
👤 **Player Command**
**Description:** Complete current step and move to next

**Usage:**
```bash
/tutorial next
```

**Notes:**
- Auto-advances if objective met
- Can manually advance if stuck
- Rewards given immediately

---

### `/tutorial skip`
👤 **Player Command**
**Description:** Skip current step (no reward)

**Usage:**
```bash
/tutorial skip
```

**Warning:**
- Skipped steps give no rewards
- Progress still counted

---

### `/tutorial quit`
👤 **Player Command**
**Description:** Exit tutorial completely

**Usage:**
```bash
/tutorial quit
```

**Effect:**
- Can restart anytime
- Progress saved

---

### `/tutorial reset`
👤 **Player Command**
**Description:** Reset all tutorial progress

**Usage:**
```bash
/tutorial reset
```

**Warning:**
- Resets to step 1
- Previous rewards NOT refunded
- Cannot reclaim rewards

---

### `/tutorial status`
👤 **Player Command**
**Description:** Show detailed tutorial progress

**Usage:**
```bash
/tutorial status
```

**Output:**
```
Tutorial Status:

Progress: 5/7 steps (71.4%)
Rewards Earned: 3,500€
Time Spent: 45 minutes

Completed Steps:
✓ Step 1: Welcome & Economy
✓ Step 2: Daily Rewards
✓ Step 3: Plot Creation
✓ Step 4: Production Setup
✓ Step 5: NPC Interaction
✗ Step 6: Crime System (current)
✗ Step 7: Advanced Features

Estimated Time to Complete: 15 minutes
```

---

# 10. System Administration

### `/health`
🔧 **Admin Command (Level 2)**
**Description:** Show overall system health

**Usage:**
```bash
/health
```

**Output:**
```
ScheduleMC System Health Check:

Economy System: ✓ HEALTHY
- Total Money in Circulation: 4,250,000€
- Active Accounts: 47
- Transactions (24h): 1,247

Plot System: ✓ HEALTHY
- Total Plots: 152
- Spatial Index: Optimized
- Rental Active: 23 plots

NPC System: ✓ HEALTHY
- Active NPCs: 45
- Merchants: 12
- Police: 5
- Residents: 28

Persistence: ✓ HEALTHY
- Last Save: 2 minutes ago
- Last Backup: 1 hour ago
- Backup Count: 5

Performance:
- TPS: 20.0 (optimal)
- Memory: 4.2 GB / 8.0 GB (53%)
- CPU: 35%

Overall Status: ✓ ALL SYSTEMS OPERATIONAL
```

---

### `/health economy`
🔧 **Admin Command (Level 2)**
**Description:** Detailed economy system diagnostics

**Usage:**
```bash
/health economy
```

**Output:**
```
Economy System Diagnostics:

Money Supply:
- Player Wallets: 2,150,000€
- Savings Accounts: 1,500,000€
- NPC Wallets: 250,000€
- State Account: 350,000€
- Total: 4,250,000€

Transactions (Last 24 Hours):
- Payments: 450
- Plot Sales: 12
- Loans Issued: 8
- Shop Purchases: 777

Loans:
- Active Loans: 15
- Total Outstanding: 425,000€
- Average Interest: 16.3%

Savings:
- Active Accounts: 23
- Total Deposits: 1,500,000€
- Weekly Interest Payout: 75,000€

Economic Health: ✓ STABLE
- Inflation Rate: 2.1% (healthy)
- Velocity of Money: 0.29 (normal)
```

---

### `/health plot`
🔧 **Admin Command (Level 2)**
**Description:** Detailed plot system diagnostics

**Usage:**
```bash
/health plot
```

**Output:**
```
Plot System Diagnostics:

Plots:
- Total: 152
- Residential: 89
- Commercial: 35
- Shop: 18
- Public: 7
- Government: 3

Ownership:
- Owned: 124 (81.6%)
- For Sale: 18 (11.8%)
- Public: 10 (6.6%)

Rentals:
- Active Rentals: 23
- Monthly Revenue: 115,000€
- Average Rent: 5,000€

Apartments:
- Total Units: 67
- Occupied: 45 (67.2%)
- Vacant: 22 (32.8%)

Spatial Index:
- Lookup Performance: 0.3ms avg
- Index Size: 152 nodes
- Depth: 4 levels

Performance: ✓ OPTIMAL
- Average Lookup: O(log n)
- No Overlaps: ✓
- All Regions Valid: ✓
```

---

### `/health backups`
🔧 **Admin Command (Level 2)**
**Description:** Show backup system status

**Usage:**
```bash
/health backups
```

**Output:**
```
Backup System Status:

Last Backup: 1 hour ago (2024-01-15 14:00)
Next Backup: In 2 hours (2024-01-15 16:00)

Available Backups: 5
1. economy_2024-01-15_14-00.json (2.3 MB)
2. economy_2024-01-15_13-00.json (2.3 MB)
3. economy_2024-01-15_12-00.json (2.2 MB)
4. economy_2024-01-15_11-00.json (2.2 MB)
5. economy_2024-01-15_10-00.json (2.1 MB)

Backup Health: ✓ HEALTHY
- Backup Frequency: Hourly
- Retention: 5 backups
- Total Backup Size: 11.1 MB
- Disk Space Available: 45.2 GB
```

---

### `/health log`
🔧 **Admin Command (Level 2)**
**Description:** Log health check to console

**Usage:**
```bash
/health log
```

**Effect:**
- Writes full health report to server console
- Includes timestamp
- Useful for monitoring/debugging

**Console Output:**
```
[14:30:45] [Server thread/INFO] [ScheduleMC]: === SYSTEM HEALTH LOG ===
[14:30:45] [Server thread/INFO] [ScheduleMC]: Timestamp: 2024-01-15 14:30:45
[14:30:45] [Server thread/INFO] [ScheduleMC]: Economy: HEALTHY (4,250,000€ circulation)
[14:30:45] [Server thread/INFO] [ScheduleMC]: Plots: HEALTHY (152 plots, 0.3ms avg lookup)
[14:30:45] [Server thread/INFO] [ScheduleMC]: NPCs: HEALTHY (45 active)
[14:30:45] [Server thread/INFO] [ScheduleMC]: Persistence: HEALTHY (last save 2m ago)
[14:30:45] [Server thread/INFO] [ScheduleMC]: Overall: ALL SYSTEMS OPERATIONAL
```

---

# Permission Levels

## Permission Level Reference

### Level 0 (Player)
All players have access to these commands without any special permissions.

**Total: 13 Base Commands**

| Command | Purpose |
|---------|---------|
| `/money`, `/pay` | Economy management |
| `/loan`, `/savings`, `/autopay` | Banking |
| `/daily` | Daily rewards |
| `/plot` | Most plot commands |
| `/bail`, `/jailtime` | Prison interaction |
| `/shopinvest` | Investments |
| `/utility` | Utility info |
| `/tobacco` | Farming info |
| `/market` | Market prices |
| `/tutorial` | Tutorial system |

### Level 2 (Admin/Operator)
Requires operator status or admin permissions.

**Total: 8 Base Commands + Admin Subcommands**

| Command | Purpose |
|---------|---------|
| `/npc` | NPC management |
| `/prison` | Prison administration |
| `/hospital` | Medical system config |
| `/state` | State treasury |
| `/warehouse` | Warehouse management |
| `/health` | System diagnostics |
| `/money set/give/take` | Economy admin |
| `/plot create/remove/reindex` | Plot admin |

### Mixed Permission Commands

Some commands have both player and admin subcommands:

| Command | Player Access | Admin Access |
|---------|--------------|--------------|
| `/plot` | Buy, sell, trust, apartments | Create, remove, setowner, reindex |
| `/utility` | View consumption | Scan, admin stats |
| `/tobacco` | Info, stats | Give items |
| `/money` | Check, pay, history | Set, give, take |

---

# Common Use Cases

## For New Players

### Day 1: Getting Started
```bash
# Claim daily reward
/daily

# Check balance
/money

# Start tutorial
/tutorial start

# Check market prices
/market prices

# Find a plot to buy
/plot list
```

### Day 2: First Plot
```bash
# Daily reward again
/daily

# Get plot tool
/plot wand

# Select area (left-click, right-click)
# Create plot (if admin, or buy existing)
/plot buy

# Set up your plot
/plot name My Cool House
/plot description A beautiful home
```

### Week 1: Production & Economy
```bash
# Start tobacco farming
/tobacco give virginia_seeds

# Check utility costs
/utility

# Invest in shops
/shopinvest list
/shopinvest buy downtown_shop 5

# Save money
/savings create 10000
```

---

## For Admins

### Server Setup (30 minutes)
```bash
# 1. Create spawn area
/plot wand
/plot create public Spawn

# 2. Set hospital
/hospital setspawn
/hospital setfee 500

# 3. Create prison
/plot create government Prison
/prison create Prison
/prison addcell 1 100 50 10 110 55 15 3

# 4. Fund state account
/state deposit 1000000

# 5. Spawn merchant NPCs
/npc spawn merchant Shop_Owner_Hans
/npc Hans schedule workstart 0700
/npc Hans schedule workend 1800
/npc Hans wallet set 10000

# 6. Create warehouse
/warehouse setshop downtown_shop
/warehouse add minecraft:diamond 64
```

### Daily Maintenance
```bash
# Check system health
/health

# Check economy
/health economy

# Monitor top consumers
/utility top

# Check prison inmates
/prison inmates

# Review market trends
/market trends
```

### Troubleshooting
```bash
# Fix plot issues
/plot reindex
/plot debug

# Check NPC issues
/npc Steve info
/npc Steve movement true

# Economy fixes
/money history Steve 20
/state balance

# System diagnostics
/health log
/health backups
```

---

## For Property Owners

### Managing Your Plot
```bash
# Trust players
/plot trust Alex

# Set up for sale
/plot sell 75000

# Set up for rent
/plot rent 500

# Create apartments
/plot apartment wand
/plot apartment create Apt_1A 2000
```

### Managing Apartments
```bash
# List apartments
/plot apartment list

# Check apartment info
/plot apartment info apt_1

# Change rent
/plot apartment setrent apt_1 2500

# Evict tenant
/plot apartment evict apt_1
```

---

## For Merchants

### Setting Up Shop
```bash
# (Admin creates shop plot first)
/npc Hans setshop downtown_shop

# Give inventory
/npc Hans inventory give 0 minecraft:diamond
/npc Hans inventory give 1 minecraft:gold_ingot

# Set wallet
/npc Hans wallet set 10000

# Link warehouse (unlimited stock)
/npc Hans warehouse set
```

### Managing Shop Investment
```bash
# Check shop performance
/shopinvest info downtown_shop

# Check your investments
/shopinvest myshares

# Market analysis
/market prices
/market trends
```

---

<div align="center">

**That's all 161+ commands in ScheduleMC!**

For more information:
- [🏠 Back to Wiki Home](Home.md)
- [📚 Getting Started Guide](Getting-Started.md)
- [❓ FAQ](FAQ.md)
- [🔧 Admin Guide](Admin-Guide.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
