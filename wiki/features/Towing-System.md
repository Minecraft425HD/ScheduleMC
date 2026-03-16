# Towing-System (Abschlepp-System)

<div align="center">

**Vehicle Towing and Impound Service with Membership Tiers**

Distance-based pricing, membership benefits, and integrated invoicing via NPC

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Membership Tiers](#membership-tiers)
4. [Towing Process (Step by Step)](#towing-process-step-by-step)
5. [Invoice Payment via NPC](#invoice-payment-via-npc)
6. [Towing Yard Setup (Admin)](#towing-yard-setup-admin)
7. [Revenue Tracking](#revenue-tracking)
8. [Pricing and Cost Calculation](#pricing-and-cost-calculation)
9. [Configuration](#configuration)
10. [Data Persistence](#data-persistence)
11. [Admin Commands](#admin-commands)
12. [Best Practices](#best-practices)
13. [Troubleshooting](#troubleshooting)

---

## Overview

The Towing System allows players to have damaged or stranded vehicles transported to registered impound yards (Towing Yards). It features a tiered membership model, distance-based pricing, an NPC invoice payment flow, and revenue tracking per towing yard. The system integrates with the Smartphone app and the Plot system.

### Key Features

- **3 Membership Tiers** - Bronze (50% coverage), Silver (75% coverage), Gold (100% free)
- **Distance-Based Pricing** - Base fee plus per-block distance component
- **NPC Invoice System** - Dedicated Towing NPC handles invoice display and payment
- **Smartphone Integration** - Two built-in service contacts (ADAC and City Towing)
- **Revenue Tracking** - Per-yard revenue stats with 30-day transaction history
- **Automatic VAT** - 19% VAT automatically collected and transferred to the state treasury
- **Repair Benefit** - Vehicles receive a 10% damage reduction when towed

### Statistics

| Metric | Value |
|--------|-------|
| Default base towing fee | 200 EUR |
| Default distance fee per block | 0.50 EUR |
| Max transactions per yard (history) | 1,000 |
| Max parking spots (server total) | 50,000 |
| Max unpaid invoices | 10,000 |
| Transaction auto-cleanup | 30 days |
| VAT rate | 19% |

---

## Architecture

```
TowingManager
  ├── TowingMembership        -- Per-player membership tier and payment state
  ├── TowingInvoice           -- Invoice data (amount, vehicle, yard, timestamp)
  ├── TowingTransaction       -- Revenue record per yard
  ├── ParkingSpot             -- Available spot data (position, yard ID, occupied status)
  └── TowingNPCInvoiceScreen  -- GUI for invoice display and payment
```

### Integration Points

```
Player's Smartphone (Towing App)
         │
         ▼
TowingManager.requestTow(playerUUID, vehicleUUID, yardId)
         │
         ├── Validate vehicle ownership + no passengers
         ├── Find free parking spot in yard
         ├── Calculate cost: base + (distance × fee_per_block) × (1 - membership_coverage)
         ├── Reduce vehicle damage by 10%
         ├── Teleport vehicle to parking spot
         ├── Set vehicle.isOnTowingYard = true (disables fuel consumption)
         └── Create TowingInvoice
                  │
                  ▼
         Player visits Towing NPC → TowingNPCInvoiceScreen
                  │
                  ▼
         Player pays invoice → Revenue tracked → Vehicle released
```

---

## Membership Tiers

Membership determines what percentage of the towing bill the service covers.

| Tier | Coverage | Monthly Fee | Description |
|------|----------|-------------|-------------|
| **NONE** | 0% | Free | Full price, no subscription |
| **BRONZE** | 50% | 500 EUR/month | Half the bill covered |
| **SILVER** | 75% | 1,000 EUR/month | Three-quarters covered |
| **GOLD** | 100% | 2,000 EUR/month | Completely free towing |

### Payment Mechanics

- **Billing interval:** 30 Minecraft days (configurable via `membershipPaymentIntervalDays`)
- **Collection:** Automatic deduction from player's bank account
- **Insufficient funds:** Membership is automatically cancelled if the fee cannot be collected
- **Subscription management:** Players manage membership through the Towing App on the smartphone

### Cost Calculation Formula

```
Total Cost  = Base Fee + (Distance in Blocks × Distance Fee per Block)
Player Cost = Total Cost × (1 - Coverage Percentage)
```

**Example calculations:**

| Membership | Total Cost | Coverage | Player Pays |
|-----------|-----------|----------|-------------|
| NONE | 225 EUR | 0% | 225.00 EUR |
| BRONZE | 225 EUR | 50% | 112.50 EUR |
| SILVER | 225 EUR | 75% | 56.25 EUR |
| GOLD | 225 EUR | 100% | 0.00 EUR |

*Example assumes: Base 200 EUR + 50 blocks × 0.50 EUR = 225 EUR total*

---

## Towing Process (Step by Step)

### For Players

1. **Open smartphone** (press `P` to open)
2. **Navigate to Towing App** - You'll find either "ADAC Pannenhilfe" or "Stadtabschleppdienst" in your contacts
3. **Select your vehicle** - The app shows a list of your registered vehicles. The vehicle must:
   - Belong to you
   - Have no passengers inside
   - Not already be on a towing yard
4. **Choose a towing yard** - Select from available yards that have free parking spots
5. **Review the cost** - The app shows:
   - Base fee
   - Distance component
   - Your membership discount
   - Final amount you will pay
6. **Confirm** - Press Confirm to initiate the tow
7. **Vehicle teleports** to the chosen parking spot instantly

### After Towing

- The vehicle is parked at the towing yard
- Fuel consumption is disabled while on the yard (`isOnTowingYard = true`)
- An invoice is generated (even for Gold members, as a receipt)
- To retrieve the vehicle, you must visit the Towing NPC at the yard and pay any outstanding invoice

---

## Invoice Payment via NPC

### Finding the Towing NPC

Each towing yard should have a dedicated Towing NPC (spawned by the server administrator). The NPC handles invoice display and payment.

### Payment Flow

1. **Interact with the Towing NPC** - Right-click on the NPC at the towing yard
2. **Invoice screen opens** - Shows:
   - Vehicle name and type
   - Towing date and distance
   - Total amount due (after membership discount)
   - Transaction type: `WERKSTATT_FEE`
3. **Click "Bezahlen" (Pay)** - Deducts the amount from your bank account
4. **Vehicle is released** - `isOnTowingYard` flag is cleared; you can drive away

### Invoice Screen Components

| Field | Description |
|-------|-------------|
| Vehicle Name | Registered vehicle name/plate |
| Yard Name | Name of the towing yard |
| Date | When the tow was requested |
| Distance | Blocks traveled |
| Base Fee | Configurable base cost |
| Distance Fee | Distance × per-block rate |
| Discount | Membership coverage applied |
| **Total Due** | **Final amount to pay** |

For full technical documentation of the invoice screen GUI, see [TOWING_NPC_INVOICE_SCREEN.md](../../docs/TOWING_NPC_INVOICE_SCREEN.md).

---

## Towing Yard Setup (Admin)

### Prerequisites

1. **Create a TOWING_YARD plot:**
   ```
   /plot create <name> TOWING_YARD
   ```

2. **Add parking spots** to the yard - Each spot is a specific block position that vehicles teleport to:
   ```
   /towing addspot <yardId> <x> <y> <z>
   ```
   The spot must have enough clearance for the vehicle entity.

3. **Spawn a Towing NPC** for invoice payment:
   ```
   /npc spawn <name> MERCHANT
   /npc <name> type TOWING
   /npc <name> yard <yardId>
   ```

4. **Configure service contacts** - Two contacts are created automatically:
   - `towing_adac` - "ADAC Pannenhilfe"
   - `towing_city` - "Stadtabschleppdienst"
   These appear automatically in all players' Smartphone Contacts app.

### Multiple Yards

Multiple towing yards can co-exist on the same server. Players can choose which yard to tow to based on available spots and proximity.

---

## Revenue Tracking

### Per-Yard Statistics

Each towing yard tracks the following revenue metrics:

| Metric | Description |
|--------|-------------|
| Gross revenue (last N days) | Total fees collected before VAT deduction |
| Net revenue (last N days) | Revenue after 19% VAT removal |
| Tow count (last N days) | Number of completed tow requests |
| Average revenue per tow | Gross ÷ Tow count |
| Transaction list | Complete history (max 1,000 entries, oldest removed) |

### Revenue Distribution

When a player pays a towing invoice:

```
Player pays invoice (e.g., 225 EUR)
     │
     ├── 19% VAT = 42.75 EUR ──► State Treasury (StateAccount)
     │
     └── 81% Net = 182.25 EUR ──► Split equally among ALL towing yards
```

- If no towing yards exist: the full amount goes to the State Treasury
- Revenue is distributed evenly across all registered yards regardless of which yard processed the tow
- This encourages maintaining multiple yards as a network

### Viewing Revenue

```
/towing revenue [yardId]    View revenue stats (requires towing yard ownership or OP)
```

---

## Pricing and Cost Calculation

### Default Values

| Parameter | Default Value |
|-----------|---------------|
| Base towing fee | 200 EUR |
| Distance fee per block | 0.50 EUR/block |

### Cost Example

Vehicle is 100 blocks from the towing yard:
```
Base Fee:           200.00 EUR
Distance Fee:       100 blocks × 0.50 EUR = 50.00 EUR
Total Cost:         250.00 EUR

NONE membership:    250.00 EUR × 100% = 250.00 EUR
BRONZE membership:  250.00 EUR × 50%  = 125.00 EUR
SILVER membership:  250.00 EUR × 25%  = 62.50 EUR
GOLD membership:    250.00 EUR × 0%   = 0.00 EUR (free)
```

### Repair Benefit

As a bonus for using the towing service, the vehicle receives a **10% reduction in current damage** when towed. This is an incentive to use the service for damaged vehicles rather than attempting to drive back with a broken vehicle.

---

## Configuration

All towing configuration lives in the `[vehicle_towing]` section of `config/schedulemc-server.toml` (via `ServerConfig`):

```toml
[vehicle_towing]
# Membership coverage percentages (0 = no coverage, 100 = fully covered)
membershipBronzeCoveragePercent = 50
membershipSilverCoveragePercent = 75
membershipGoldCoveragePercent = 100

# Monthly membership fees (in EUR)
membershipBronzeFee = 500.0
membershipSilverFee = 1000.0
membershipGoldFee = 2000.0

# Billing cycle in Minecraft days
membershipPaymentIntervalDays = 30

# Towing fees
towingBaseFee = 200.0
towingDistanceFeePerBlock = 0.5
```

---

## Data Persistence

| Data Type | File | Description |
|-----------|------|-------------|
| Membership subscriptions | `config/plotmod_towing_memberships.json` | Per-player tier, next billing date |
| Parking spots | `config/plotmod_towing_parking_spots.json` | Position, yard ID, occupied status |
| Invoices | Stored in-memory + periodic save | Outstanding and recent invoices |
| Transaction history | Per-yard in towing data | Up to 1,000 per yard, 30-day retention |

### Limits

| Limit | Value |
|-------|-------|
| Max transactions per yard | 1,000 (oldest removed) |
| Max total parking spots | 50,000 |
| Max unpaid invoices | 10,000 |
| Transaction auto-cleanup age | 30 days |

---

## Admin Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/towing addspot <yardId> <x> <y> <z>` | OP Level 2 | Add a parking spot to a yard |
| `/towing removespot <spotId>` | OP Level 2 | Remove a parking spot |
| `/towing listspots [yardId]` | OP Level 2 | List parking spots |
| `/towing revenue [yardId]` | OP Level 2 | View revenue statistics |
| `/towing clearinvoices <player>` | OP Level 2 | Clear all invoices for a player |
| `/towing setmembership <player> <tier>` | OP Level 2 | Manually set a player's membership |

---

## Best Practices

### For Server Administrators

1. **Place multiple yards** - Having 2–3 towing yards at different locations gives players reasonable towing distances and reduces max fees.
2. **Parking spot clearance** - Ensure parking spots have enough space for the largest vehicles (Trucks, Vans). Use 2-block vertical clearance.
3. **NPC placement** - Place the Towing NPC near the entrance of the yard, clearly visible. Label the NPC with a readable name like "Abschleppservice".
4. **Membership pricing** - The default fees (500/1000/2000 EUR/month) are calibrated for a server economy with 1000 EUR starting balance. Adjust if your economy has different scale.
5. **Revenue transparency** - Share revenue stats with towing yard owners (players who own TOWING_YARD plots) to maintain engagement with the system.

### For Players

1. **Invest in membership** - If you use the towing service regularly (broken vehicles, garage storage), Gold membership at 2,000 EUR/month pays for itself with just 2-3 tows.
2. **Pay invoices promptly** - Unpaid invoices may prevent future towing requests. Visit the NPC at the yard to clear your balance.
3. **Check available spots** - The towing app shows which yards have free spots before you commit to a yard.

---

## Troubleshooting

### Vehicle cannot be towed

1. **Passengers inside** - Remove all passengers (players, NPCs) from the vehicle before requesting a tow.
2. **Not your vehicle** - The vehicle must be registered to your player UUID.
3. **Already at a yard** - If `isOnTowingYard = true`, the vehicle is already impounded. Pay the existing invoice first.
4. **No free spots** - The chosen yard has no available parking spots. Try a different yard or ask an admin to add more spots.

### Membership not charging

1. **Insufficient balance** - If the monthly fee cannot be collected, membership is cancelled. Check your balance and re-subscribe via the Towing App.
2. **Billing interval** - Billing happens every 30 Minecraft days (configurable). If billing seems late, check `membershipPaymentIntervalDays`.

### Invoice not appearing at NPC

1. **Wrong NPC** - Ensure you're interacting with the Towing NPC (type `TOWING`), not a general Merchant NPC.
2. **Invoice cleared** - If another player or admin cleared the invoice, it won't show. Check with `/towing listinvoices <player>`.
3. **NPC yard link** - The NPC must be linked to a specific yard with `/npc <name> yard <yardId>`.

### Revenue not distributing

1. **No towing yards registered** - Revenue goes entirely to the state treasury if no yards are registered as `TOWING_YARD` plot type.
2. **Check plot type** - Ensure the yard plot was created with `TOWING_YARD` type, not `COMMERCIAL`.
