# Economy System

<div align="center">

**Complete Banking, Loans, Savings, Taxes & Investment System**

Thread-safe transaction processing with 16 manager classes and automatic backup

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Money Management](#money-management)
4. [Banking System](#banking-system)
5. [ATM System](#atm-system)
6. [Loan System](#loan-system)
7. [Credit Score System](#credit-score-system)
8. [Savings Accounts](#savings-accounts)
9. [Daily Rewards](#daily-rewards)
10. [Recurring Payments](#recurring-payments)
11. [Tax System](#tax-system)
12. [Shop Investments](#shop-investments)
13. [State Treasury](#state-treasury)
14. [Overdraft System](#overdraft-system-dispo)
15. [Transaction System](#transaction-system)
16. [Anti-Exploit Mechanisms](#anti-exploit-mechanisms)
17. [Developer API](#developer-api)
18. [Commands Reference](#commands-reference)
19. [Best Practices](#best-practices)
20. [Troubleshooting](#troubleshooting)

---

## Overview

The Economy System is the financial backbone of ScheduleMC, providing a complete banking infrastructure with loans, savings, taxes, credit scoring, investments, and automated payment processing. It is built with 11 dedicated manager classes coordinating through thread-safe operations.

### Key Features

- **Persistent Balances** -- Thread-safe with ConcurrentHashMap
- **Dual Currency** -- Virtual bank accounts and physical wallet cash
- **3-Tier Loan System** -- SMALL (5K), MEDIUM (25K), LARGE (100K)
- **Savings Accounts** -- 5% weekly compound interest, 4-week lock
- **Credit Scoring** -- Dynamic credit ratings affecting loan eligibility
- **Tax System** -- Progressive income tax, property tax, sales tax
- **Daily Rewards** -- Up to 340 EUR/day with streak bonuses (max 30 days)
- **Recurring Payments** -- Up to 10 automated standing orders per player
- **Shop Investments** -- 1,000 EUR/share in NPC shops
- **State Treasury** -- Centralized government fund with income tracking
- **Overdraft Protection** -- Up to -5,000 EUR credit line at 25% weekly interest
- **Anti-Exploit** -- Rate limiting, mass-sell detection, daily volume caps
- **Transaction History** -- Full audit trail (1,000 transactions per player)

---

## Architecture

The Economy System is composed of 16 manager classes, each responsible for a distinct financial subsystem.

### Manager Classes

| # | Class | Responsibility |
|---|-------|---------------|
| 1 | **EconomyManager** | Core balance registry, deposits, withdrawals, transfers |
| 2 | **WalletManager** | Physical cash items in player inventory |
| 3 | **TransactionHistory** | Per-player audit trail (1,000 entries max) |
| 4 | **InterestManager** | Savings and overdraft interest calculations |
| 5 | **LoanManager** | 3-tier loan system with daily repayment |
| 6 | **CreditScoreManager** | Player credit ratings and eligibility |
| 7 | **TaxManager** | Income, property, and sales tax processing |
| 8 | **SavingsAccountManager** | Savings accounts with compound interest |
| 9 | **OverdraftManager** | Negative balance limits, seizure (Pfandung) |
| 10 | **RecurringPaymentManager** | Automated standing orders (autopay) |
| 11 | **ShopAccountManager** | NPC shop financial accounts and profit tracking |

### System Diagram

```
EconomyManager (Singleton)
|-- Balance Registry (ConcurrentHashMap<UUID, Double>)
|-- WalletManager (Physical cash items)
|-- TransactionHistory (1,000 per player)
|-- LoanManager (3 loan tiers)
|-- CreditScoreManager (Dynamic credit ratings)
|-- SavingsAccountManager (5% weekly interest)
|-- RecurringPaymentManager (Autopay system, max 10)
|-- TaxManager (Income + Property + Sales)
|-- OverdraftManager (Up to -5,000 EUR)
|-- ShopAccountManager (NPC shop accounts)
|-- StateAccount (Government treasury)
|-- AntiExploitManager (Rate limiting + mass-sell detection)
+-- RateLimiter (10 transactions/minute cap)
```

### Supporting Classes

| Class | Purpose |
|-------|---------|
| **AntiExploitManager** | Prevents mass-selling exploits and tracks daily sell volume |
| **RateLimiter** | Caps transactions at 10 per minute per player |
| **BatchTransactionManager** | Processes multi-recipient transfers atomically |
| **FeeManager** | Calculates transfer, ATM, and service fees |
| **StateAccount** | Government treasury singleton |
| **GlobalEconomyTracker** | Aggregate economy statistics |

### Performance

| Metric | Value |
|--------|-------|
| Transaction processing | < 1ms |
| Auto-save interval | 5 minutes |
| Backup system | Automatic with recovery |
| Starting balance | 1,000 EUR |
| Thread safety | ConcurrentHashMap throughout |

### Persistence Files

```
config/plotmod_economy.json       -- Player balances
config/plotmod_transactions.json  -- Transaction history
config/plotmod_loans.json         -- Active loans
config/plotmod_savings.json       -- Savings accounts
config/plotmod_taxes.json         -- Tax records and debts
config/state_account.json         -- Government treasury
```

---

## Money Management

### Checking Your Balance

```
/money
```

**Output:**
```
Balance: 12,450 EUR
```

### Transferring Money

```
/pay <player> <amount>
```

**Example:**
```
/pay Alex 1000
```

**Fee Structure:**
- Transfer Fee: 1% of amount (minimum 10 EUR)
- Recipient receives full amount
- Sender pays amount + fee
- Fee goes to State Treasury

**Calculation:**
```
Transfer Amount:  1,000 EUR
Fee:              max(1,000 x 0.01, 10) = max(10, 10) = 10 EUR
Total Deducted:   1,010 EUR
Alex Receives:    1,000 EUR
State Gets:       10 EUR (fee)
```

**Fee Examples:**

| Transfer | Fee Calculation | Total Cost |
|----------|----------------|------------|
| 500 EUR | max(500 x 0.01, 10) = 10 EUR | 510 EUR |
| 5,000 EUR | max(5,000 x 0.01, 10) = 50 EUR | 5,050 EUR |
| 50,000 EUR | max(50,000 x 0.01, 10) = 500 EUR | 50,500 EUR |

### Transaction History

```
/money history [limit]
```

**Examples:**
```
/money history       -- Last 10 transactions (default)
/money history 20    -- Last 20 transactions
```

**Output:**
```
Transaction History (Last 10):

1. +50 EUR - Daily Reward - 2024-01-15 10:30
   Balance after: 12,450 EUR

2. -5,000 EUR - Plot Purchase (Downtown_House_1) - 2024-01-15 11:00
   Balance after: 7,450 EUR

3. +990 EUR - Payment from Alex - 2024-01-15 12:15
   Balance after: 8,440 EUR

4. -10 EUR - Transfer Fee - 2024-01-15 12:15
   Balance after: 8,430 EUR
```

### Admin Commands

#### Set Balance
```
/money set <player> <amount>
```

Sets a player's balance to an exact amount. Logged in transaction history.

#### Give Money
```
/money give <player> <amount>
```

Adds to the player's current balance. No transaction fee applied. Logged in history.

#### Take Money
```
/money take <player> <amount>
```

Removes from the player's balance. Can result in negative balance (respect overdraft limits).

---

## Banking System

### Dual Money System

ScheduleMC uses two types of currency storage that work together.

#### 1. Bank Account (Virtual)

| Property | Value |
|----------|-------|
| Manager | EconomyManager |
| Storage | `config/plotmod_economy.json` |
| Starting Balance | 1,000 EUR |
| Used For | Smartphone Bank App, NPC transactions, admin commands |
| Can Go Negative | Yes (overdraft up to -5,000 EUR) |
| Thread Safety | ConcurrentHashMap |

#### 2. Wallet / Cash (Physical)

| Property | Value |
|----------|-------|
| Manager | WalletManager |
| Storage | Player inventory (physical items) |
| Starting Balance | 0 EUR |
| Used For | NPC/shop purchases, face-to-face trading |
| Can Go Negative | No |
| Dropped on Death | Configurable |

Players convert between the two systems using ATM blocks.

---

## ATM System

ATM Blocks allow conversion between bank accounts and physical wallet cash.

### Operations

| Operation | Description | Fee |
|-----------|-------------|-----|
| **Deposit** | Cash items from wallet into bank account | 5 EUR |
| **Withdraw** | Bank balance into physical cash items | 5 EUR |

### Withdrawal Example

```
Action:             Withdraw 1,000 EUR from bank
Fee:                5 EUR
Deducted from bank: 1,005 EUR
Cash received:      1,000 EUR (physical items in inventory)
State gets:         5 EUR (ATM fee)
```

### Deposit Example

```
Action:             Deposit 2,000 EUR cash into bank
Fee:                5 EUR
Cash removed:       2,000 EUR (from inventory)
Added to bank:      1,995 EUR
State gets:         5 EUR (ATM fee)
```

---

## Loan System

> **Note:** Loan, savings, daily reward, recurring payment, and shop investment features are managed internally by their respective manager classes (`LoanManager`, `SavingsAccountManager`, `DailyRewardManager`, etc.) and accessed through the **Smartphone Bank App** and **NPC banker interactions**. The command syntax shown below describes the intended user-facing operations available through the UI, not chat commands.

### Loan Tiers

ScheduleMC offers three loan tiers with varying amounts, interest rates, and durations.

| Tier | Amount | Interest | Duration | Daily Payment | Total Repayment |
|------|--------|----------|----------|---------------|-----------------|
| **SMALL** | 5,000 EUR | 10% | 14 days | ~392.86 EUR | 5,500 EUR |
| **MEDIUM** | 25,000 EUR | 15% | 28 days | ~1,035.71 EUR | 28,750 EUR |
| **LARGE** | 100,000 EUR | 20% | 56 days | ~2,142.86 EUR | 120,000 EUR |

### Loan Calculation

```
Total with Interest = Principal x (1 + Interest Rate)
Daily Payment       = Total with Interest / Duration Days

Examples:
SMALL:   5,000 x 1.10  =   5,500 EUR -->   5,500 / 14 =   392.86 EUR/day
MEDIUM: 25,000 x 1.15  =  28,750 EUR -->  28,750 / 28 = 1,035.71 EUR/day
LARGE: 100,000 x 1.20  = 120,000 EUR --> 120,000 / 56 = 2,142.86 EUR/day
```

### Applying for a Loan

```
/loan apply <SMALL|MEDIUM|LARGE>
```

**Requirements:**
- Minimum Balance: 1,000 EUR
- No Active Loan: Cannot have an existing loan
- Credit score may affect eligibility (see Credit Score section)

**Example:**
```
/loan apply MEDIUM
```

**Result:**
```
Loan Approved!

Type:            MEDIUM
Amount Received: 25,000 EUR
Interest Rate:   15%
Total to Repay:  28,750 EUR
Duration:        28 days
Daily Payment:   1,035.71 EUR

Your new balance: 26,000 EUR
```

### Loan Repayment

#### Automatic Daily Payments

The system auto-deducts daily payments every in-game day (24,000 ticks).

**Timeline Example (SMALL Loan):**
```
Day 0:   Receive 5,000 EUR (Balance: 6,000 EUR)
Day 1:   -392.86 EUR payment (Balance: 5,607.14 EUR)
Day 2:   -392.86 EUR payment (Balance: 5,214.28 EUR)
...
Day 14:  -392.86 EUR final payment --> LOAN PAID OFF
```

#### Early Repayment

```
/loan repay
```

**Benefits:**
- No early repayment penalty
- Can take new loan immediately
- Saves remaining time on the loan

#### Payment Failures

When daily payment fails due to insufficient funds:

| Failure | Action |
|---------|--------|
| 1st failure | Warning message sent to player |
| 2nd failure | Another warning |
| 3rd+ failure | Continued warnings, interest accrues |

No additional penalties are charged for missed payments beyond the normal interest.

### Loan Information

```
/loan info
```

Displays active loan details or available loan tiers if no loan is active.

---

## Credit Score System

The CreditScoreManager tracks player financial behavior and assigns a credit rating that can influence loan eligibility.

### How Credit Score Works

| Factor | Effect |
|--------|--------|
| On-time loan payments | Increases score |
| Missed loan payments | Decreases score |
| Account age | Gradual increase over time |
| Transaction volume | Positive indicator |
| Overdraft usage | Negative indicator |
| Tax debt | Negative indicator |

### Score Ranges

| Range | Rating | Loan Impact |
|-------|--------|-------------|
| 800-1000 | Excellent | All tiers available |
| 600-799 | Good | All tiers available |
| 400-599 | Fair | SMALL and MEDIUM only |
| 200-399 | Poor | SMALL only |
| 0-199 | Very Poor | No loans available |

Players build credit through responsible financial behavior -- paying loans on time, avoiding overdrafts, and maintaining a positive balance.

---

## Savings Accounts

### Savings Configuration

| Parameter | Value |
|-----------|-------|
| Interest Rate | 5% per week (compound) |
| Lock Period | 4 weeks (28 days) |
| Min Deposit | 1,000 EUR per account |
| Max Total | 50,000 EUR across all accounts |
| Multiple Accounts | Allowed (until total limit) |

### Interest Calculation

**Weekly Compound Interest:**
```
After Week N: Balance x (1.05)^N

Example: 10,000 EUR initial deposit
Week 1: 10,000.00 x 1.05 = 10,500.00 EUR (+500.00 EUR)
Week 2: 10,500.00 x 1.05 = 11,025.00 EUR (+525.00 EUR)
Week 3: 11,025.00 x 1.05 = 11,576.25 EUR (+551.25 EUR)
Week 4: 11,576.25 x 1.05 = 12,155.06 EUR (+578.81 EUR)

Total Profit after 4 weeks: 2,155.06 EUR (21.55% return)
```

**Long-Term Growth Table:**

| Weeks | Balance | Total Interest |
|-------|---------|----------------|
| 1 | 10,500 EUR | 500 EUR |
| 4 | 12,155 EUR | 2,155 EUR |
| 8 | 14,775 EUR | 4,775 EUR |
| 12 | 17,959 EUR | 7,959 EUR |
| 26 | 34,813 EUR | 24,813 EUR |
| 52 | 121,242 EUR | 111,242 EUR |

### Creating Savings Accounts

```
/savings create <amount>
```

**Example:**
```
/savings create 10000
```

**Requirements:**
- Minimum 1,000 EUR
- Must have balance available
- Total across all accounts must not exceed 50,000 EUR

**Result:**
```
Savings Account Created!

Account ID:      a3b4c5d6
Initial Deposit: 10,000 EUR
Interest Rate:   5% per week
Lock Period:     4 weeks (28 days)

Status: Locked until 2024-02-12

Expected Balance (4 weeks): 12,155.06 EUR
Expected Profit:            2,155.06 EUR
```

### Managing Savings

#### List Accounts
```
/savings list
```

#### Deposit to Account
```
/savings deposit <accountId> <amount>
```

**Important:** Depositing resets the lock period to 4 weeks from the deposit date.

#### Withdraw from Account (Unlocked)
```
/savings withdraw <accountId> <amount>
```

Account must be unlocked (28+ days old). No penalty.

#### Force Withdrawal (Locked)
```
/savings forcewithdraw <accountId> <amount>
```

**Penalty:** 10% of withdrawal amount goes to State Treasury.

**Calculation:**
```
Requested:         5,000 EUR
Penalty (10%):       500 EUR
You Receive:       4,500 EUR
State Gets:          500 EUR
```

#### Close Account
```
/savings close <accountId>
```

Withdraws entire balance. 10% penalty if still locked.

---

## Daily Rewards

### Reward Configuration

| Parameter | Value |
|-----------|-------|
| Base Reward | 50 EUR |
| Streak Bonus | 10 EUR per day of streak |
| Max Streak | 30 days |
| Max Daily Reward | 340 EUR |
| Cooldown | 24 hours |
| Grace Period | 48 hours to maintain streak |

### Claiming Rewards

```
/daily
```

**Result:**
```
Daily Reward Claimed!

Base Reward:   50 EUR
Streak Bonus: 150 EUR (15-day streak)
Total Reward: 200 EUR

New Balance: 12,650 EUR

Current Streak: 15
Next Claim: In 24 hours
```

### Streak Progression Table

| Day | Bonus | Total Reward | Cumulative |
|-----|-------|--------------|------------|
| 1 | 0 EUR | 50 EUR | 50 EUR |
| 2 | 10 EUR | 60 EUR | 110 EUR |
| 3 | 20 EUR | 70 EUR | 180 EUR |
| 5 | 40 EUR | 90 EUR | 400 EUR |
| 10 | 90 EUR | 140 EUR | 1,220 EUR |
| 15 | 140 EUR | 190 EUR | 2,470 EUR |
| 20 | 190 EUR | 240 EUR | 4,020 EUR |
| 25 | 240 EUR | 290 EUR | 5,870 EUR |
| 30 | 290 EUR | 340 EUR | 8,820 EUR |
| 30+ | 290 EUR | 340 EUR (capped) | -- |

**Formula:**
```
Streak Bonus = min(10 x (streak - 1), 290)
Total Reward = 50 + Streak Bonus

Examples:
Day 1:  50 + (10 x 0)  = 50 EUR
Day 15: 50 + (10 x 14) = 190 EUR
Day 30: 50 + (10 x 29) = 340 EUR
Day 31: 50 + 290        = 340 EUR (capped at 30)
```

### Streak Rules

- Claim within 48 hours of last claim to maintain streak
- Grace period allows one missed day
- More than 48 hours gap resets streak to 1

### Streak Statistics

```
/daily streak
```

Displays current streak, longest streak, total claims, and time until next claim.

---

## Recurring Payments

### Autopay Configuration

| Parameter | Value |
|-----------|-------|
| Max per Player | 10 recurring payments |
| Min Interval | 1 day |
| Auto-disable | After 3 failed payments |

### Creating Autopay

```
/autopay add <player> <amount> <intervalDays> <description>
```

**Example:**
```
/autopay add Alex 500 7 "Weekly rent"
```

**Use Cases:**
- Rent payments to landlords
- Salary payments to employees
- Subscription fees
- Regular transfers to business partners

### Managing Autopay

#### List Payments
```
/autopay list
```

Displays all recurring payments with status, next payment date, and amount.

#### Pause Payment
```
/autopay pause <paymentId>
```

Stops future payments. Can be resumed anytime with no fee.

#### Resume Payment
```
/autopay resume <paymentId>
```

Reactivates a paused payment. Next payment scheduled based on interval.

#### Delete Payment
```
/autopay delete <paymentId>
```

Permanently removes the recurring payment. Cannot be undone.

### Failure Handling

```
Payment Due: 500 EUR to Alex
Balance: 300 EUR (insufficient)

1st Failure: Warning sent, retry in 1 day
2nd Failure: Another warning, retry in 1 day
3rd Failure: Critical warning, payment AUTO-DISABLED
```

Player receives a notification and must re-enable with `/autopay resume <id>` after adding funds.

---

## Tax System

The TaxManager processes three types of taxes, collected every 7 in-game days (1 MC week).

### Tax Types

#### 1. Income Tax (Progressive)

| Balance Bracket | Tax Rate |
|----------------|----------|
| 0 -- 10,000 EUR | 0% (tax-free allowance) |
| 10,001 -- 50,000 EUR | 10% |
| 50,001 -- 100,000 EUR | 15% |
| 100,001+ EUR | 20% |

**Example Calculation:**
```
Player Balance: 75,000 EUR

Bracket 1: 10,000 EUR at  0% =        0 EUR
Bracket 2: 40,000 EUR at 10% =    4,000 EUR  (10,001 to 50,000)
Bracket 3: 25,000 EUR at 15% =    3,750 EUR  (50,001 to 75,000)
                                  ---------
Total Income Tax:                  7,750 EUR
```

#### 2. Property Tax

| Parameter | Value |
|-----------|-------|
| Rate | Configurable per chunk (default: 100 EUR/chunk/period) |
| Calculation | Plot area / 256 blocks (16x16 chunk), rounded up |
| Period | Every 7 MC days |

**Example:**
```
Plot size: 48 x 32 blocks = 1,536 block area
Chunks: ceil(1,536 / 256) = 6 chunks
Tax: 6 x 100 EUR = 600 EUR per period
```

#### 3. Sales Tax (MwSt)

| Parameter | Value |
|-----------|-------|
| Rate | 19% |
| Applied To | NPC shop purchases |
| Recipient | State Treasury |

**Example:**
```
Item price: 100 EUR
Sales tax:   19 EUR
Player pays: 119 EUR
Shop gets:   100 EUR
State gets:   19 EUR
```

### Tax Debt

If a player cannot afford their taxes, debt accumulates.

```
/money taxdebt     -- View outstanding tax debt
```

Tax debt must be paid before certain financial operations (e.g., taking new loans).

---

## Shop Investments

> **STATUS: NOT IMPLEMENTED** - The Shop Investment system described below was planned but has not been implemented in the current codebase. No ShopInvestmentManager or related code exists.

### Investment System (Planned)

Players can invest in NPC shops through share purchases, receiving a portion of shop profits.

| Parameter | Value |
|-----------|-------|
| Share Price | 1,000 EUR per share |
| Profit Distribution | Based on share ownership percentage |
| Tracking Period | 7-day revenue cycle |
| Manager | ShopAccountManager |

### Investing

```
/shopinvest buy <shopId> <shares>
```

**Example:**
```
/shopinvest buy Electronics_Store 5
```

**Cost:** 5 x 1,000 EUR = 5,000 EUR

### Checking Investments

```
/shopinvest info <shopId>
```

Displays share count, revenue data, and projected dividends.

### Selling Shares

```
/shopinvest sell <shopId> <shares>
```

Returns 1,000 EUR per share (base price).

### How Profits Work

The ShopAccountManager tracks all revenue flowing through each NPC shop. At the end of each 7-day cycle, profits are distributed to shareholders proportionally.

```
Shop Revenue (7 days):  50,000 EUR
Operating Costs:        10,000 EUR
Net Profit:             40,000 EUR

Your Shares: 5 of 20 total (25%)
Your Dividend: 40,000 x 0.25 = 10,000 EUR
```

---

## State Treasury

### State Account

The State Account is the centralized government fund that collects fees, taxes, and other revenue. It is managed as a singleton (`StateAccount`).

| Parameter | Value |
|-----------|-------|
| Starting Balance | 100,000 EUR |
| Manager | StateAccount (singleton) |
| Storage | `config/state_account.json` |

### Income Sources

| Source | Amount | Frequency |
|--------|--------|-----------|
| ATM Fees | 5 EUR | Per transaction |
| Transfer Fees | 1% (min 10 EUR) | Per transfer |
| Sales Tax (MwSt) | 19% | Per shop sale |
| Income Tax | Progressive (0-20%) | Every 7 MC days |
| Property Tax | Per chunk | Every 7 MC days |
| Savings Penalties | 10% | Early withdrawals |
| Overdraft Interest | 25% weekly | Per overdraft account |
| Admin Deposits | Variable | Manual |

**Example Daily Income (Active Server):**
```
100 ATM transactions:       100 x 5 EUR          =     500 EUR
50 transfers (avg 2,000):   50 x 20 EUR          =   1,000 EUR
Shop sales (100,000):       100,000 x 0.19       =  19,000 EUR
Savings penalties:                                =   2,000 EUR
                                                    ---------
Total Daily Income:                               ~ 22,500 EUR
```

### State Commands

#### View Balance
```
/state balance
```

#### Deposit (Admin)
```
/state deposit <amount>
```

#### Withdraw (Admin)
```
/state withdraw <amount>
```

---

## Overdraft System (Dispo)

### Configuration

| Parameter | Value |
|-----------|-------|
| Max Limit | -5,000 EUR |
| Warning Threshold | -2,500 EUR |
| Interest Rate | 25% per week on negative balance |
| Seizure | Triggered at limit |

### Overdraft Mechanics

```
Current Balance:  500 EUR
Purchase:       3,000 EUR
New Balance:   -2,500 EUR   (Allowed, within -5,000 EUR limit)

Current Balance: -4,000 EUR
Purchase:        2,000 EUR
New Balance:    -6,000 EUR   (DECLINED, exceeds -5,000 EUR limit)
```

### Weekly Interest on Negative Balance

```
Formula: |Negative Balance| x 0.25

Example:
Current Balance:  -3,000 EUR
Weekly Interest:   3,000 x 0.25 = 750 EUR
New Balance:      -3,750 EUR

Next Week:
Interest:          3,750 x 0.25 = 937.50 EUR
New Balance:      -4,687.50 EUR
```

### Warning System

**At -2,500 EUR (threshold):**
```
OVERDRAFT WARNING

Your account is at -2,500 EUR
Interest Rate: 25% per week
Max Limit: -5,000 EUR

Please deposit funds to avoid penalties!
```

### Seizure (Pfandung)

**Triggered at -5,000 EUR:**

1. All physical cash removed from wallet
2. Account balance remains at -5,000 EUR
3. Confiscated cash sent to State Treasury
4. Critical warning notification sent to player

---

## Transaction System

### Transaction Types

All financial activity is categorized and tracked.

| Category | Types |
|----------|-------|
| Transfers | TRANSFER, TRANSFER_FEE |
| ATM | ATM_DEPOSIT, ATM_WITHDRAW, ATM_FEE |
| Purchases | NPC_PURCHASE, VEHICLE_PURCHASE |
| Admin | ADMIN_SET, ADMIN_GIVE, ADMIN_TAKE |
| Taxes | TAX_INCOME, TAX_SALES, TAX_PROPERTY |
| Interest | INTEREST, INTEREST_SAVINGS |
| Loans | LOAN_DISBURSEMENT, LOAN_REPAYMENT, LOAN_INTEREST |
| Overdraft | OVERDRAFT_FEE |
| Bonds | BOND_PURCHASE, BOND_MATURITY |
| Insurance | INSURANCE_PAYMENT, INSURANCE_PAYOUT |
| State | STATE_SUBSIDY, STATE_SPENDING |
| Savings | SAVINGS_DEPOSIT, SAVINGS_WITHDRAW |
| Rewards | DAILY_REWARD |
| Fees | DEATH_FEE, GARAGE_FEE |
| Other | OTHER |

### Transaction Storage

| Parameter | Value |
|-----------|-------|
| Max per Player | 1,000 transactions |
| Auto-pruning | Oldest deleted when limit reached |
| Persistence | `config/plotmod_transactions.json` |
| Thread Safety | ConcurrentHashMap |

**Transaction Data Structure:**
```json
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

## Anti-Exploit Mechanisms

ScheduleMC includes two anti-exploit systems to prevent economic abuse.

### Rate Limiter

The `RateLimiter` class prevents command spam.

| Parameter | Value |
|-----------|-------|
| Max Transactions | 10 per minute |
| Window | 60 seconds (sliding) |
| Scope | Per player |
| Tracking | ArrayDeque of timestamps |

When the limit is reached, the player receives an error message with the number of seconds until the next allowed transaction.

### Anti-Exploit Manager

The `AntiExploitManager` class detects and penalizes suspicious selling behavior.

| Mechanism | Description |
|-----------|-------------|
| **Daily Sell Volume** | Tracks total EUR sold per player per day |
| **Daily Limit** | Configurable maximum daily sell volume |
| **Mass-Sell Detection** | Detects rapid bulk selling within cooldown window |
| **Progressive Penalties** | Price multiplier decreases the more a player exceeds limits |
| **Warning Levels** | 0-3 levels; at level 3, a 50% penalty applies on top |

**How Penalties Work:**
```
Normal sale:          1.0x price (full value)
Approaching limit:    0.7x - 1.0x price (progressive reduction)
Mass-sell detected:   Additional penalty multiplier applied
Warning level 3+:    Additional 0.5x multiplier (stacks)
```

Daily counters reset at the start of each new in-game day. Warning levels persist and can only be reset by admins.

---

## Developer API

### IEconomyAPI Interface

External mods can access the economy system through `IEconomyAPI`. All methods are thread-safe.

**Obtaining the API:**
```java
IEconomyAPI economy = ScheduleMCAPI.getEconomyAPI();
```

### Core Methods (v3.0.0+)

| Method | Return | Description |
|--------|--------|-------------|
| `getBalance(UUID)` | `double` | Get player balance (0.0 if no account) |
| `hasAccount(UUID)` | `boolean` | Check if account exists |
| `createAccount(UUID)` | `void` | Create new account with start balance |
| `deposit(UUID, double)` | `void` | Deposit funds |
| `deposit(UUID, double, String)` | `void` | Deposit with description |
| `withdraw(UUID, double)` | `boolean` | Withdraw funds (false if insufficient) |
| `withdraw(UUID, double, String)` | `boolean` | Withdraw with description |
| `transfer(UUID, UUID, double)` | `boolean` | Transfer between players |
| `transfer(UUID, UUID, double, String)` | `boolean` | Transfer with description |
| `setBalance(UUID, double)` | `void` | Admin: set exact balance |
| `deleteAccount(UUID)` | `void` | Admin: delete account permanently |
| `getStartBalance()` | `double` | Get configured start balance |

### Extended Methods (v3.2.0+)

| Method | Return | Description |
|--------|--------|-------------|
| `getAllBalances()` | `Map<UUID, Double>` | Unmodifiable map of all balances |
| `getTotalMoneyInCirculation()` | `double` | Total EUR across all accounts |
| `getAccountCount()` | `int` | Number of registered accounts |
| `getTopBalances(int)` | `List<Entry<UUID, Double>>` | Top N richest players |
| `canAfford(UUID, double)` | `boolean` | Check if player can afford amount |
| `batchTransfer(UUID, Map, String)` | `boolean` | Batch transfer to multiple recipients |
| `getTransactionHistory(UUID, int)` | `List<String>` | Recent transactions as strings |

### Usage Example

```java
IEconomyAPI economy = ScheduleMCAPI.getEconomyAPI();

// Check balance
double balance = economy.getBalance(playerUUID);

// Deposit funds
economy.deposit(playerUUID, 100.0, "Quest reward");

// Transfer between players
boolean success = economy.transfer(fromUUID, toUUID, 50.0, "Item purchase");

// Check affordability
if (economy.canAfford(playerUUID, 5000.0)) {
    economy.withdraw(playerUUID, 5000.0, "Vehicle purchase");
}

// Batch transfer (e.g., salary payment)
Map<UUID, Double> recipients = new HashMap<>();
recipients.put(employee1, 1000.0);
recipients.put(employee2, 1500.0);
economy.batchTransfer(bossUUID, recipients, "Weekly salaries");
```

---

## Commands Reference

### Chat Commands (7)

> **Note:** Most player-facing economy features (balance checking, payments, loans, savings, daily rewards, recurring payments, shop investments) are accessed through the **Smartphone Bank App** and **NPC banker interactions**, not through chat commands. Only the following chat commands exist:

| Command | Description | Permission |
|---------|-------------|------------|
| `/money set <player> <amount>` | Set player balance | Admin |
| `/money give <player> <amount>` | Add money to player | Admin |
| `/money take <player> <amount>` | Remove money from player | Admin |
| `/money history <player>` | View transaction history | Admin |
| `/state balance` | View State Treasury balance | Default |
| `/state deposit <amount>` | Deposit into State Treasury | Default |
| `/state withdraw <amount>` | Withdraw from State Treasury | Default |

### UI-Based Features (via Smartphone Bank App)

The following features are managed by internal manager classes and accessed via the Smartphone Bank App or NPC interactions:

| Feature | Manager Class | Access Method |
|---------|--------------|---------------|
| Balance Check | `EconomyManager` | Smartphone Bank App |
| Money Transfer | `EconomyManager` | Smartphone Bank App |
| Loan Application | `LoanManager` / `CreditLoanManager` | Smartphone Bank App / NPC |
| Loan Repayment | `LoanManager` | Smartphone Bank App |
| Savings Accounts | `SavingsAccountManager` | Smartphone Bank App / NPC (`SavingsDepositPacket`, `SavingsWithdrawPacket`) |
| Daily Rewards | `DailyRewardManager` | Automatic on login |
| Credit Score | `CreditScoreManager` | Internal tracking |

**Total: 7 chat commands + Smartphone/NPC UI features**

---

## Best Practices

### For Players

#### 1. Build an Emergency Fund
```
Recommended:  10,000 EUR in savings
Reason:       Cover unexpected expenses (taxes, overdraft)
Strategy:     Save 20% of all income
```

#### 2. Use Loans Wisely

**Good uses:**
- Plot purchases (appreciating asset)
- Business investment (shop shares)
- Production equipment

**Bad uses:**
- Consumption spending
- Gambling
- Impulse purchases

#### 3. Maximize Daily Rewards
```
Claim /daily every 24 hours
30-day streak = 340 EUR/day
Annual value: ~124,100 EUR (if claimed every day)
```

#### 4. Diversify Income
```
Income Strategy Example:
- Savings accounts (safe, 5% weekly)
- Production and selling products
- Plot rentals and apartments
- Shop investments (dividends)
- Keep liquid funds for emergencies
```

#### 5. Manage Tax Liability
```
Stay below 10,000 EUR balance on tax day for 0% income tax.
Use savings accounts to shelter funds (not counted as balance).
Own only plots you actively use to minimize property tax.
```

### For Admins

#### 1. Monitor State Treasury
```
/state balance          -- Check treasury health
/health economy         -- Full economy diagnostics
```

#### 2. Backup Economy Data
```
Files to Backup:
- config/plotmod_economy.json
- config/plotmod_transactions.json
- config/plotmod_loans.json
- config/plotmod_savings.json
- config/plotmod_taxes.json
- config/state_account.json

Frequency: Daily
Retention: 7 days minimum
```

#### 3. Watch for Inflation
```
Monitor total money in circulation.
If prices are rising across all NPC shops, consider:
- Increasing tax rates
- Reducing daily reward amounts
- Adding money sinks (fees, costs)
```

---

## Troubleshooting

### "Insufficient Funds"

**Cause:** Balance too low for transaction.

**Solutions:**
```
1. /money             -- Check current balance
2. /daily             -- Claim daily reward
3. /loan apply SMALL  -- Get a 5,000 EUR loan
4. Sell items to NPCs
5. Check overdraft limit (/money)
```

### "Transaction Failed"

**Possible Causes:**
1. Insufficient funds
2. Recipient does not exist
3. Amount too low (< 1 EUR)
4. Rate limit hit (10 transactions/minute)

**Solutions:**
- Verify balance with /money
- Check player name spelling
- Wait for rate limit cooldown
- Check /money history for recent errors

### "Rate Limited"

**Cause:** More than 10 transactions in the last 60 seconds.

**Solution:** Wait for the displayed cooldown timer. The RateLimiter uses a sliding window, so the oldest transaction will expire first.

### Loan Repayment Issues

**Problem:** Cannot afford daily payment.

**Solutions:**
1. Early Repayment: `/loan repay` to pay remaining balance
2. Increase income: sell products, claim daily, work for players
3. Emergency: force-withdraw savings (10% penalty)
4. Last resort: abandon unused plots (50% refund)

### Savings Account Locked

**Problem:** Cannot withdraw before 28 days.

**Options:**
1. Wait (most economical, no penalty)
2. Force withdraw: `/savings forcewithdraw <id> <amount>` (10% penalty)
3. Close account: `/savings close <id>` (10% penalty on full balance)

### Overdraft Warnings

**Problem:** Account approaching -5,000 EUR limit.

**Priority Actions:**
```
1. /daily                     -- Claim daily reward
2. /loan apply SMALL          -- Get 5,000 EUR loan
3. Sell items to NPC shops
4. /autopay list              -- Pause unnecessary autopays
5. /savings forcewithdraw     -- Emergency savings withdrawal
```

---

<div align="center">

**Economy System -- Complete Guide**

For related systems:
- [Plot System](Plot-System.md)
- [NPC System](NPC-System.md)
- [Market System](Market-System.md)
- [Warehouse System](Warehouse-System.md)

[Back to Wiki Home](../Home.md) | [All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
