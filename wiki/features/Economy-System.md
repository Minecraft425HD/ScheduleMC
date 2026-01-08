# Economy System

<div align="center">

**Complete Banking, Loans, Savings & Investment System**

Thread-safe transaction processing with automatic backup

[🏠 Back to Wiki Home](../Home.md) • [📋 Commands Reference](../Commands.md)

</div>

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Money Management](#money-management)
3. [Banking System](#banking-system)
4. [Loan System](#loan-system)
5. [Savings Accounts](#savings-accounts)
6. [Daily Rewards](#daily-rewards)
7. [Shop Investment](#shop-investment)
8. [Recurring Payments](#recurring-payments)
9. [State Treasury](#state-treasury)
10. [Transaction System](#transaction-system)
11. [Overdraft System](#overdraft-system-dispo)
12. [Best Practices](#best-practices)
13. [Troubleshooting](#troubleshooting)

---

## Overview

The Economy System is the financial backbone of ScheduleMC, providing a complete banking infrastructure with loans, savings, investments, and automated payment processing.

### Key Features

✅ **Persistent Balances** - Thread-safe with ConcurrentHashMap
✅ **3-Tier Loan System** - SMALL (5K), MEDIUM (25K), LARGE (100K)
✅ **Savings Accounts** - 5% weekly interest, 4-week lock
✅ **Daily Rewards** - Up to 340€/day with streak bonuses
✅ **Shop Investment** - Buy shares, earn weekly dividends
✅ **Recurring Payments** - Automated standing orders
✅ **Transaction History** - Full audit trail (1000 transactions/player)
✅ **Overdraft Protection** - Up to -5,000€ credit line
✅ **State Treasury** - Centralized government fund

### Architecture

```
EconomyManager (Singleton)
├── Balance Registry (ConcurrentHashMap<UUID, Double>)
├── Transaction History (1000 per player)
├── LoanManager (3 loan tiers)
├── SavingsAccountManager (5% weekly interest)
├── RecurringPaymentManager (Autopay system)
├── DailyRewardManager (Streak bonuses)
├── ShopInvestmentManager (Dividend system)
└── StateAccount (Government treasury)
```

**Performance:**
- Transaction processing: **< 1ms**
- Auto-save interval: **5 minutes**
- Backup system: **Automatic with recovery**
- Starting balance: **1,000€**

---

## Money Management

### Checking Your Balance

```bash
/money
```

**Output:**
```
💰 Balance: 12,450€
```

---

### Transferring Money

```bash
/pay <player> <amount>
```

**Example:**
```bash
/pay Alex 1000
```

**Fee Structure:**
- **Transfer Fee:** 1% of amount (minimum 10€)
- **Recipient receives:** Full amount
- **Sender pays:** Amount + fee

**Calculation:**
```
Transfer Amount: 1,000€
Fee: max(1,000 × 0.01, 10) = max(10, 10) = 10€
Total Deducted: 1,010€
Alex Receives: 1,000€
State Gets: 10€ (fee)
```

**More Examples:**

| Transfer | Fee Calculation | Total Cost |
|----------|----------------|------------|
| 500€ | max(500×0.01, 10) = 10€ | 510€ |
| 5,000€ | max(5,000×0.01, 10) = 50€ | 5,050€ |
| 50,000€ | max(50,000×0.01, 10) = 500€ | 50,500€ |

---

### Transaction History

```bash
/money history [limit]
```

**Example:**
```bash
# Last 10 transactions (default)
/money history

# Last 20 transactions
/money history 20
```

**Output:**
```
Transaction History (Last 10):

1. +50€ - Daily Reward - 2024-01-15 10:30
   Balance after: 12,450€

2. -5,000€ - Plot Purchase (Downtown_House_1) - 2024-01-15 11:00
   Balance after: 7,450€

3. +990€ - Payment from Alex - 2024-01-15 12:15
   Balance after: 8,440€

4. -10€ - Transfer Fee - 2024-01-15 12:15
   Balance after: 8,430€

5. +500€ - Shop Dividend (Electronics) - 2024-01-14 08:00
   Balance after: 8,930€
```

---

### Admin Commands

#### Set Balance
```bash
/money set <player> <amount>
```

**Example:**
```bash
/money set Steve 100000
```

**Use Cases:**
- Fix economy bugs
- Reset player balance
- Testing features

---

#### Give Money
```bash
/money give <player> <amount>
```

**Example:**
```bash
/money give Alex 5000
```

**Notes:**
- Adds to current balance
- No transaction fee
- Logged in history

---

#### Take Money
```bash
/money take <player> <amount>
```

**Example:**
```bash
/money take Steve 1000
```

**Warning:**
- Can result in negative balance
- Use carefully
- Consider overdraft limits

---

## Banking System

### Dual Money System

ScheduleMC uses two types of currency storage:

#### 1. Bank Account (Virtual)
- **Manager:** EconomyManager
- **Storage:** `config/plotmod_economy.json`
- **Features:**
  - Virtual balance
  - Used for commands (/pay, /plot buy, etc.)
  - Can go negative (overdraft)
  - Thread-safe operations

#### 2. Wallet/Cash (Physical)
- **Manager:** WalletManager
- **Storage:** Player inventory
- **Features:**
  - Physical cash items
  - Used for NPC/shop purchases
  - Cannot go negative
  - Dropped on death (configurable)

---

### ATM System

**ATM Blocks** allow conversion between bank and wallet.

**Operations:**
- **Deposit:** Cash → Bank account
- **Withdraw:** Bank account → Cash
- **Fee:** 5€ per transaction

**Example:**
```
Action: Withdraw 1,000€ from bank
Fee: 5€
Deducted from bank: 1,005€
Cash received: 1,000€ (physical items)
State gets: 5€
```

---

## Loan System

### Loan Tiers

ScheduleMC offers three loan tiers with varying amounts, interest rates, and durations.

| Tier | Amount | Interest | Duration | Daily Payment | Total Repayment |
|------|--------|----------|----------|---------------|-----------------|
| **SMALL** | 5,000€ | 10% | 14 days | ~392.86€ | 5,500€ |
| **MEDIUM** | 25,000€ | 15% | 28 days | ~1,035.71€ | 28,750€ |
| **LARGE** | 100,000€ | 20% | 56 days | ~2,142.86€ | 120,000€ |

---

### Loan Calculation

**Formula:**
```
Total with Interest = Principal × (1 + Interest Rate)
Daily Payment = Total with Interest ÷ Duration Days

Examples:
SMALL:  5,000 × 1.10 = 5,500€  → 5,500 ÷ 14 = 392.86€/day
MEDIUM: 25,000 × 1.15 = 28,750€ → 28,750 ÷ 28 = 1,035.71€/day
LARGE:  100,000 × 1.20 = 120,000€ → 120,000 ÷ 56 = 2,142.86€/day
```

---

### Applying for a Loan

```bash
/loan apply <SMALL|MEDIUM|LARGE>
```

**Requirements:**
- **Minimum Balance:** 1,000€
- **No Active Loan:** Cannot have existing loan
- **No Minimum Playtime** (previously 7 days, removed)

**Example:**
```bash
/loan apply MEDIUM
```

**Result:**
```
✓ Loan Approved!

Type: MEDIUM
Amount Received: 25,000€
Interest Rate: 15%
Total to Repay: 28,750€
Duration: 28 days
Daily Payment: 1,035.71€

Your new balance: 26,000€
```

---

### Loan Repayment

#### Automatic Daily Payments

**System:**
- Every in-game day (24,000 ticks)
- Daily payment auto-deducted
- Tracks progress automatically

**Timeline Example (SMALL Loan):**
```
Day 0:  Receive 5,000€ (Balance: 6,000€)
Day 1:  -392.86€ payment (Balance: 5,607.14€)
Day 2:  -392.86€ payment (Balance: 5,214.28€)
...
Day 14: -392.86€ payment (Balance: 4,857.14€) → LOAN PAID OFF
```

---

#### Early Repayment

```bash
/loan repay
```

**Benefits:**
- No early repayment penalty
- Can take new loan immediately
- Saves remaining interest accrual

**Example:**
```
Active Loan: MEDIUM
Days Elapsed: 14/28
Remaining: 14,375€ (50% of total)

Early Repayment Cost: 14,375€
Immediate payoff
New loan available now
```

---

#### Payment Failures

**Insufficient Funds:**
1. **1st Failure:** Warning message sent
2. **2nd Failure:** Another warning
3. **3rd+ Failure:** Continued warnings

**No Penalties:**
- No additional fees
- Interest continues as normal
- Just warnings to player

---

### Loan Information

```bash
/loan info
```

**With Active Loan:**
```
Active Loan:
Type: MEDIUM
Amount Borrowed: 25,000€
Interest Rate: 15% (3,750€)
Total Due: 28,750€
Daily Payment: 1,035.71€

Progress:
Days Elapsed: 14/28 (50%)
Amount Paid: 14,500€
Remaining: 14,250€

Due Date: 2024-02-12
Days Remaining: 14
```

**Without Active Loan:**
```
Available Loans:

SMALL Loan
Amount: 5,000€
Interest: 10% (500€)
Total Repayment: 5,500€
Duration: 14 days
Daily Payment: 392.86€

MEDIUM Loan
Amount: 25,000€
Interest: 15% (3,750€)
Total Repayment: 28,750€
Duration: 28 days
Daily Payment: 1,035.71€

LARGE Loan
Amount: 100,000€
Interest: 20% (20,000€)
Total Repayment: 120,000€
Duration: 56 days
Daily Payment: 2,142.86€
```

---

## Savings Accounts

### Savings Configuration

**Interest System:**
- **Interest Rate:** 5% per week
- **Lock Period:** 4 weeks (28 days)
- **Compound Interest:** Yes
- **Multiple Accounts:** Allowed (up to 50,000€ total)

**Limits:**
- **Min Deposit:** 1,000€ per account
- **Max Total:** 50,000€ across all accounts
- **Unlimited Accounts:** Until total limit reached

---

### Interest Calculation

**Weekly Compound Interest:**
```
After Week N: Balance × (1.05)^N

Example: 10,000€ initial deposit
Week 1: 10,000 × 1.05 = 10,500€ (+500€)
Week 2: 10,500 × 1.05 = 11,025€ (+525€)
Week 3: 11,025 × 1.05 = 11,576.25€ (+551.25€)
Week 4: 11,576.25 × 1.05 = 12,155.06€ (+578.81€)

Total Profit after 4 weeks: 2,155.06€ (21.55% return)
```

**Long-Term Growth:**

| Weeks | Balance | Total Interest |
|-------|---------|----------------|
| 1 | 10,500€ | 500€ |
| 4 | 12,155€ | 2,155€ |
| 8 | 14,775€ | 4,775€|
| 12 | 17,959€ | 7,959€|
| 26 | 34,813€ | 24,813€ |
| 52 | 121,242€ | 111,242€ |

---

### Creating Savings Accounts

```bash
/savings create <amount>
```

**Example:**
```bash
/savings create 10000
```

**Requirements:**
- Minimum 1,000€
- Must have balance available
- Total across accounts ≤ 50,000€

**Result:**
```
✓ Savings Account Created!

Account ID: a3b4c5d6
Initial Deposit: 10,000€
Interest Rate: 5% per week
Lock Period: 4 weeks (28 days)

Status: 🔒 Locked until 2024-02-12

Expected Balance (4 weeks): 12,155.06€
Expected Profit: 2,155.06€
```

---

### Managing Savings

#### List Accounts
```bash
/savings list
```

**Output:**
```
━━━━━━━━━ SAVINGS ACCOUNTS ━━━━━━━━━

ID: a3b4c5d6
Balance: 12,155.06€
Status: 🔓 Unlocked
Created: 2024-01-15 (28 days ago)
Total Interest Earned: 2,155.06€

ID: e7f8g9h0
Balance: 5,250.00€
Status: 🔒 Locked (15 days remaining)
Created: 2024-01-30 (13 days ago)
Total Interest Earned: 250.00€

━━━━━━━━━ TOTAL ━━━━━━━━━
Total Savings: 17,405.06€
Total Interest: 2,405.06€
Interest Rate: 5.0% per week
```

---

#### Deposit to Account
```bash
/savings deposit <accountId> <amount>
```

**Example:**
```bash
/savings deposit a3b4c5d6 5000
```

**Important:**
- **Resets lock period** to 4 weeks from deposit
- No deposit limit
- Immediate deposit

**Result:**
```
✓ Deposit Successful!

Account: a3b4c5d6
Deposited: 5,000€
New Balance: 17,155.06€

⚠️ Lock period reset!
New unlock date: 2024-02-20 (28 days from now)
```

---

#### Withdraw from Account

##### Normal Withdrawal (Unlocked)
```bash
/savings withdraw <accountId> <amount>
```

**Requirements:**
- Account must be unlocked (28+ days old)
- Sufficient balance

**Example:**
```bash
/savings withdraw a3b4c5d6 2000
```

**Result:**
```
✓ Withdrawal Successful!

Account: a3b4c5d6
Withdrawn: 2,000€
New Balance: 15,155.06€
Remaining: 15,155.06€

No penalty (account unlocked)
```

---

##### Force Withdrawal (Locked)
```bash
/savings forcewithdraw <accountId> <amount>
```

**Penalty:** 10% of withdrawal amount

**Example:**
```bash
/savings forcewithdraw e7f8g9h0 5000
```

**Calculation:**
```
Requested: 5,000€
Penalty (10%): 500€
You Receive: 4,500€
State Gets: 500€

Account remaining: 250€
Lock status: Still locked
```

**Result:**
```
⚠️ Early Withdrawal Penalty Applied!

Account: e7f8g9h0
Withdrawal Amount: 5,000€
Penalty (10%): -500€
You Receive: 4,500€

New Balance: 250€
Status: Still locked (15 days remaining)
```

---

#### Close Account
```bash
/savings close <accountId>
```

**Rules:**
- Same lock period applies
- 10% penalty if still locked
- Entire balance withdrawn

**Example (Unlocked):**
```
Account: a3b4c5d6
Balance: 15,155.06€

✓ Account Closed!
Full Balance Returned: 15,155.06€
No Penalty
```

**Example (Locked):**
```
Account: e7f8g9h0
Balance: 5,250€

⚠️ Account is locked (15 days remaining)
Penalty (10%): 525€
You Receive: 4,725€
State Gets: 525€

Account Closed
```

---

## Daily Rewards

### Reward System

**Base Reward:** 50€
**Streak Bonus:** 10€ per day of streak
**Max Streak:** 30 days (340€/day maximum)

---

### Claiming Rewards

```bash
/daily
```

**Cooldown:** 24 hours
**Grace Period:** 48 hours to maintain streak

**Result:**
```
✓ Daily Reward Claimed!

Base Reward: 50€
Streak Bonus: 150€ (15-day streak)
Total Reward: 200€

New Balance: 12,650€

Current Streak: 15 🔥
Next Claim: In 24 hours
```

---

### Streak Mechanics

**Progression Table:**

| Day | Bonus | Total Reward | Cumulative (30 days) |
|-----|-------|--------------|----------------------|
| 1 | 0€ | 50€ | 50€ |
| 2 | 10€ | 60€ | 110€ |
| 3 | 20€ | 70€ | 180€ |
| 5 | 40€ | 90€ | 400€ |
| 10 | 90€ | 140€ | 1,220€ |
| 15 | 140€ | 190€ | 2,470€ |
| 20 | 190€ | 240€ | 4,020€ |
| 25 | 240€ | 290€ | 5,870€ |
| 30 | 290€ | 340€ | 5,850€ (complete) |
| 30+ | 290€ | 340€ (capped) | - |

**Formula:**
```
Streak Bonus = min(10 × (streak - 1), 290)
Total Reward = 50 + Streak Bonus

Examples:
Day 1:  50 + (10 × 0) = 50€
Day 15: 50 + (10 × 14) = 190€
Day 30: 50 + (10 × 29) = 340€
Day 31: 50 + 290 = 340€ (capped at 30)
```

---

### Streak Rules

**Maintaining Streak:**
- ✅ Claim within 48 hours of last claim
- ✅ Grace period allows one missed day
- ❌ > 48 hours = streak resets to 1

**Example Timeline:**
```
Monday 10:00:    Claim (Streak 14, 190€)
Tuesday 10:00:   Can claim (Streak 15, 200€)
Wednesday 09:00: Still valid (within 48h, Streak 16, 210€)
Wednesday 11:00: ⚠️ Warning - Grace period ends in 23h
Thursday 09:30:  ✗ Streak reset (Streak 1, 50€)
```

---

### Streak Statistics

```bash
/daily streak
```

**Output:**
```
═══ Daily Reward Statistics ═══

Current Streak: 15 🔥
Longest Streak: 28
Total Claims: 156

Next Reward:
- Base: 50€
- Bonus: 150€ (15-day streak)
- Total: 200€

Next Claim Available: 05:32:18
```

---

## Shop Investment

### Share System

**Configuration:**
- **Total Shares per Shop:** 100
- **Share Price:** 1,000€ each
- **Max Shareholders:** 2 players
- **Max Purchase:** 99 shares (must leave room for 2nd shareholder)
- **Sell Penalty:** 25% (receive 75% back)

---

### Buying Shares

```bash
/shopinvest buy <shopId> <shares>
```

**Example:**
```bash
/shopinvest buy Electronics 50
```

**Cost:**
```
Shares: 50
Price per Share: 1,000€
Total Cost: 50,000€

Your Ownership: 50% (50/100 shares)
```

**Result:**
```
✓ Share Purchase Successful!

Shop: Electronics
Shares Purchased: 50 (50%)
Total Investment: 50,000€

Expected Weekly Dividend: Based on shop revenue
(Check with /shopinvest info Electronics)
```

---

### Dividend System

**7-Day Revenue Tracking:**
- Shops track revenue for rolling 7-day period
- Automatic tax deduction (19% MwSt)
- Weekly dividend payout

**Calculation:**
```
7-Day Gross Revenue: 100,000€
Sales Tax (19%): -19,000€
Net Revenue: 81,000€

Your Shares: 50 (50%)
Your Dividend: 81,000 × 0.50 = 40,500€
```

---

### Dividend Examples

#### Example 1: High-Revenue Shop
```
Shop: Electronics Store
7-Day Gross: 100,000€
Sales Tax (19%): -19,000€
Net Revenue: 81,000€

Shareholder A (60 shares): 81,000 × 0.60 = 48,600€
Shareholder B (40 shares): 81,000 × 0.40 = 32,400€
```

#### Example 2: Moderate Shop
```
Shop: Corner Market
7-Day Gross: 20,000€
Sales Tax (19%): -3,800€
Net Revenue: 16,200€

Shareholder A (70 shares): 16,200 × 0.70 = 11,340€
Shareholder B (30 shares): 16,200 × 0.30 = 4,860€
```

---

### ROI Calculation

**Investment Analysis:**
```
Initial Investment: 50,000€ (50 shares)
Weekly Net Revenue: 81,000€
Weekly Dividend: 40,500€

ROI Metrics:
- Payback Period: 1.23 weeks
- Weekly Return: 81% (40,500 / 50,000)
- Annual Return: 4,212% (if consistent)
```

**Long-Term Projection:**

| Week | Dividend | Cumulative | ROI |
|------|----------|------------|-----|
| 1 | 40,500€ | 40,500€ | 81% |
| 2 | 40,500€ | 81,000€ | 162% |
| 4 | 40,500€ | 162,000€ | 324% |
| 8 | 40,500€ | 324,000€ | 648% |
| 12 | 40,500€ | 486,000€ | 972% |

---

### Selling Shares

```bash
/shopinvest sell <shopId> <shares>
```

**Penalty:** 75% refund only

**Example:**
```bash
/shopinvest sell Electronics 20
```

**Calculation:**
```
Original Purchase: 20 shares × 1,000€ = 20,000€
Refund Rate: 75%
You Receive: 20,000 × 0.75 = 15,000€
Loss: 5,000€ (25%)

Remaining Shares: 30 (30%)
```

**Why the Penalty?**
- Prevents market manipulation
- Encourages long-term investment
- 25% stays with shop (becomes available shares)

---

### Shop Investment Commands

```bash
/shopinvest list                    # List all shops
/shopinvest info <shopId>          # Shop details
/shopinvest buy <shopId> <shares>  # Buy shares
/shopinvest sell <shopId> <shares> # Sell shares (75% refund)
/shopinvest myshares               # Your portfolio
```

**Example Output:**
```
═══ Shop Info: Electronics ═══

Available Shares: 40 / 100
Share Price: 1,000€

7-Day Net Revenue: 81,000€
Tax Paid (19%): 19,000€

Shareholders:
- PlayerA: 60 shares (60%)
  Investment: 60,000€
  Weekly Dividend: 48,600€

Available for Purchase: 40 shares (40,000€)
```

---

## Recurring Payments

### Autopay System

**Configuration:**
- **Max per Player:** 10 recurring payments
- **Min Interval:** 1 day
- **Auto-disable:** After 3 failed payments

**Use Cases:**
- Rent payments to landlords
- Salary payments to employees
- Subscription fees
- Regular transfers

---

### Creating Autopay

```bash
/autopay add <player> <amount> <intervalDays> <description>
```

**Example:**
```bash
/autopay add Alex 500 7 "Weekly rent"
```

**Result:**
```
✓ Recurring Payment Created!

Payment ID: a1b2c3d4
Recipient: Alex
Amount: 500€
Interval: Every 7 days
Description: Weekly rent

First Payment: In 7 days (2024-01-22)
```

---

### Managing Autopay

#### List Payments
```bash
/autopay list
```

**Output:**
```
━━━━ RECURRING PAYMENTS ━━━━

ID: a1b2c3d4
To: Alex
Amount: -500€
Interval: 7 days
Description: Weekly rent
Status: Active
Next Payment: In 3 days

ID: e5f6g7h8
To: Bob
Amount: -100€
Interval: 1 day
Description: Daily fee
Status: Paused
Next Payment: -

Total Active: 1
Total Paused: 1
```

---

#### Pause Payment
```bash
/autopay pause <paymentId>
```

**Example:**
```bash
/autopay pause a1b2c3d4
```

**Effect:**
- Stops future payments
- Can resume anytime
- No cancellation fee

---

#### Resume Payment
```bash
/autopay resume <paymentId>
```

**Example:**
```bash
/autopay resume a1b2c3d4
```

**Effect:**
- Reactivates payment
- Next payment scheduled based on interval

---

#### Delete Payment
```bash
/autopay delete <paymentId>
```

**Example:**
```bash
/autopay delete a1b2c3d4
```

**Warning:**
- Permanent deletion
- Cannot be undone
- Create new autopay if needed later

---

### Failure Handling

**Automatic Retry System:**
```
Payment Due: 500€ to Alex
Balance: 300€ (insufficient)

1st Failure:
- Warning sent to player
- Retry in 1 day
- Failure count: 1

2nd Failure (next day):
- Another warning
- Retry in 1 day
- Failure count: 2

3rd Failure (next day):
- Critical warning
- Payment AUTO-DISABLED
- Failure count: 3
```

**Notification:**
```
§c§l[AUTOPAY] DISABLED!
§7Reason: 3 failed payment attempts
§7To: Alex
§7Amount: 500€

§cPlease add funds and re-enable:
§e/autopay resume a1b2c3d4
```

---

## State Treasury

### State Account

**Purpose:** Central government fund for public expenses

**Starting Balance:** 100,000€

---

### Income Sources

| Source | Amount | Frequency |
|--------|--------|-----------|
| **ATM Fees** | 5€ | Per transaction |
| **Transfer Fees** | 1% (min 10€) | Per transfer |
| **Sales Tax (MwSt)** | 19% | Per shop sale |
| **Savings Penalties** | 10% | Early withdrawals |
| **Admin Deposits** | Variable | Manual |

**Example Daily Income:**
```
100 ATM transactions:     100 × 5€ = 500€
50 transfers (avg 2,000€): 50 × 20€ = 1,000€
Shop sales (100,000€):    100,000 × 0.19 = 19,000€
Savings penalties:        2,000€

Total Daily Income: ~22,500€
```

---

### Expenditure Uses

| Use | Purpose | Frequency |
|-----|---------|-----------|
| **Warehouse Deliveries** | NPC shop restocking | Every 3 days |
| **NPC Salaries** | (Future feature) | Weekly |
| **Public Infrastructure** | (Future feature) | Variable |
| **Admin Withdrawals** | Manual expenses | As needed |

---

### State Commands

#### View Balance
```bash
/state balance
```

**Output:**
```
═══ STATE TREASURY ═══

Current Balance: 245,600€

Last 24h Activity:
Income: +32,400€
Expenses: -15,000€
Net: +17,400€
```

---

#### Deposit
```bash
/state deposit <amount>
```

**Example:**
```bash
/state deposit 50000
```

**Result:**
```
✓ State Deposit Successful!

Amount: +50,000€
New Balance: 295,600€
Reason: Admin funding
```

---

#### Withdraw
```bash
/state withdraw <amount>
```

**Example:**
```bash
/state withdraw 20000
```

**Result:**
```
✓ State Withdrawal Successful!

Amount: -20,000€
New Balance: 275,600€
Remaining: 275,600€
```

---

## Transaction System

### Transaction Types

**40 Transaction Types Tracked:**

| Category | Types |
|----------|-------|
| **Transfers** | TRANSFER, TRANSFER_FEE |
| **ATM** | ATM_DEPOSIT, ATM_WITHDRAW, ATM_FEE |
| **Purchases** | NPC_PURCHASE, VEHICLE_PURCHASE, SHOP_PAYOUT |
| **Admin** | ADMIN_SET, ADMIN_GIVE, ADMIN_TAKE |
| **Investments** | SHOP_INVESTMENT, SHOP_DIVESTMENT |
| **Taxes** | TAX_INCOME, TAX_SALES, TAX_PROPERTY |
| **Interest** | INTEREST, INTEREST_SAVINGS |
| **Loans** | LOAN_DISBURSEMENT, LOAN_REPAYMENT, LOAN_INTEREST |
| **Overdraft** | OVERDRAFT_FEE |
| **Bonds** | BOND_PURCHASE, BOND_MATURITY |
| **Insurance** | INSURANCE_PAYMENT, INSURANCE_PAYOUT |
| **State** | STATE_SUBSIDY, STATE_SPENDING |
| **Savings** | SAVINGS_DEPOSIT, SAVINGS_WITHDRAW |
| **Rewards** | DAILY_REWARD |
| **Fees** | DEATH_FEE, GARAGE_FEE |
| **Other** | OTHER |

---

### Transaction Storage

**Limits:**
- **Max per Player:** 1,000 transactions
- **Auto-pruning:** Oldest deleted when limit reached
- **Persistence:** `config/plotmod_transactions.json`
- **Thread-safe:** ConcurrentHashMap

**Transaction Data:**
```java
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1705320600000,
  "type": "TRANSFER",
  "fromPlayer": "uuid-player-a",
  "toPlayer": "uuid-player-b",
  "amount": 1000.0,
  "description": "Payment for services",
  "balanceAfter": 11450.0
}
```

---

## Overdraft System (Dispo)

### Configuration

**Overdraft Limits:**
- **Max Limit:** -5,000€
- **Warning Threshold:** -2,500€
- **Interest Rate:** 25% per week
- **Seizure at Limit:** Account reset + warning

---

### Overdraft Mechanics

**Allowed Negative Balance:**
```
Current Balance: 500€
Purchase: 3,000€
New Balance: -2,500€ ✓ Allowed (within -5,000€ limit)

Current Balance: -4,000€
Purchase: 2,000€
New Balance: -6,000€ ✗ DECLINED (exceeds -5,000€ limit)
```

---

### Weekly Interest

**Calculation:**
```
Formula: |Negative Balance| × 0.25

Example:
Current Balance: -3,000€
Weekly Interest: 3,000 × 0.25 = 750€
New Balance: -3,750€

Next Week:
Interest: 3,750 × 0.25 = 937.50€
New Balance: -4,687.50€
```

---

### Warning System

**Threshold Warning (-2,500€):**
```
⚠️ OVERDRAFT WARNING

Your account is at -2,500€
Interest Rate: 25% per week
Max Limit: -5,000€

Please deposit funds to avoid penalties!
```

**Limit Reached (-5,000€):**
```
🚨 OVERDRAFT LIMIT REACHED

Your account has been seized!
- Wallet emptied
- Account reset to -5,000€
- All cash confiscated

Deposit funds immediately to restore account!
```

---

### Seizure (Pfändung)

**Triggered at -5,000€:**
1. **Empty Wallet:** All physical cash removed
2. **Reset Balance:** Set to -5,000€
3. **Critical Warning:** Notification sent
4. **Cash to State:** Confiscated cash → State Treasury

**Example:**
```
Balance: -5,000€ (limit reached)
Wallet Cash: 2,000€

Seizure Process:
1. Wallet cash removed: -2,000€ → State
2. Balance reset: -5,000€ (no change)
3. Warning sent to player

Player must deposit to restore positive balance
```

---

## Best Practices

### For Players

#### 1. Build Emergency Fund
```
Recommended: 10,000€ in savings
Reason: Cover unexpected expenses
Strategy: Save 20% of income
```

#### 2. Use Loans Wisely
```
✓ Good Uses:
- Plot purchases (asset)
- Business investment
- Production equipment

✗ Bad Uses:
- Consumption
- Gambling
- Impulse buys
```

#### 3. Maximize Daily Rewards
```
Set Reminder: Claim /daily every 24h
Maintain Streak: 30-day streak = 340€/day
Annual Value: 124,100€ (if daily for year)
```

#### 4. Diversify Investments
```
Portfolio Example:
- 30% Savings (safe, 5% weekly)
- 50% Shop Investment (high return)
- 20% Liquid (emergencies)
```

---

### For Admins

#### 1. Monitor State Treasury
```bash
# Daily health check
/state balance

# Weekly review
/health economy
```

#### 2. Manage Economy
```bash
# Inflation control
/money take @a 100  # Tax collection

# Stimulus
/money give @a 500  # Economic boost
```

#### 3. Backup Economy Data
```
Files to Backup:
- config/plotmod_economy.json
- config/plotmod_transactions.json
- config/plotmod_loans.json
- config/plotmod_savings.json
- config/state_account.json

Frequency: Daily
Retention: 7 days
```

---

## Troubleshooting

### "Insufficient Funds"

**Cause:** Balance too low for transaction

**Solutions:**
```bash
# Check balance
/money

# Get daily reward
/daily

# Apply for loan
/loan apply SMALL

# Sell items to NPCs
```

---

### "Transaction Failed"

**Possible Causes:**
1. Insufficient funds
2. Recipient doesn't exist
3. Amount too low (< 1€)
4. Rate limit hit

**Solutions:**
- Verify balance
- Check player name spelling
- Wait 5 seconds between transfers
- Check /money history for errors

---

### Loan Repayment Issues

**Problem:** Can't afford daily payment

**Solutions:**
1. **Early Repayment:**
   ```bash
   /loan repay
   ```
   Pay off remaining balance

2. **Increase Income:**
   - Sell products
   - Get daily reward
   - Work for other players

3. **Emergency Measures:**
   - Sell shop shares (/shopinvest sell)
   - Withdraw savings (with penalty)
   - Abandon unused plots

---

### Savings Account Locked

**Problem:** Can't withdraw before 28 days

**Options:**

1. **Wait:** Most economical
   ```
   Days Remaining: 15
   Recommended: Wait for unlock
   ```

2. **Force Withdrawal:** 10% penalty
   ```bash
   /savings forcewithdraw <accountId> <amount>
   Penalty: 10% of withdrawal
   ```

3. **Close Account:** 10% penalty on entire balance
   ```bash
   /savings close <accountId>
   ```

---

### Overdraft Warnings

**Problem:** Account at -2,500€

**Actions:**
```bash
# Priority 1: Deposit funds
/daily           # Claim reward
/loan apply SMALL # Get 5,000€ loan

# Priority 2: Increase income
Sell items
Work for money
Rent out plots

# Priority 3: Reduce expenses
Cancel autopay
Stop unnecessary spending
```

---

<div align="center">

**Economy System - Complete Guide**

For related systems:
- [🏘️ Plot System](Plot-System.md)
- [🤖 NPC System](NPC-System.md)
- [🏪 Warehouse System](Warehouse-System.md)

[🏠 Back to Wiki Home](../Home.md) • [📋 All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
