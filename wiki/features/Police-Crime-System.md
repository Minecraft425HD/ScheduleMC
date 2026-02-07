# Police & Crime System

<div align="center">

**GTA-Style 5-Star Wanted Level & Prison System**

Dynamic law enforcement with NPC police officers

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Wanted Level System](#wanted-level-system)
3. [Crime Levels](#crime-levels)
4. [Auto-Decay](#auto-decay)
5. [Escape Mechanic](#escape-mechanic)
6. [Police AI](#police-ai)
7. [Prison System](#prison-system)
8. [Bail System](#bail-system)
9. [Raid Penalties](#raid-penalties)
10. [Bounty System](#bounty-system)
11. [Commands](#commands)
12. [Developer API](#developer-api)
13. [Best Practices](#best-practices)
14. [Troubleshooting](#troubleshooting)

---

## Overview

The Police & Crime System brings **GTA-style law enforcement** to ScheduleMC with a 5-star wanted level system, intelligent NPC police officers, a functional prison system, and a player bounty system.

### Key Features

- **5-Star Wanted System** - Progressive law enforcement response
- **Auto-Decay** - Wanted level decreases by 1 star per Minecraft day
- **Escape Mechanic** - Hide for 30 seconds, 40+ blocks from police
- **5 Crime Levels** - Clean through Maximum severity
- **Prison System** - Cells with security levels 1-5
- **Bail System** - Pay at hospital to clear charges
- **Police AI** - Chase, arrest, backup calling, door blocking
- **Raid Penalties** - Lose illegal cash on arrest
- **Bounty System** - Place bounties on other players
- **10 Commands** - Full crime and prison management

---

## Wanted Level System

### Star Levels

| Stars | Severity | Police Response | Bail Cost | Description |
|-------|----------|-----------------|-----------|-------------|
| 1 | Minor | 1 officer, walking | 1,000 | First offense |
| 2 | Moderate | 1-2 officers, jogging | 2,500 | Repeat offender |
| 3 | Serious | 2-3 officers, running | 5,000 | Dangerous criminal |
| 4 | Major | 3-4 officers, aggressive | 10,000 | High threat |
| 5 | Extreme | All police, relentless | 20,000 | Most wanted |

### Checking Wanted Level

**Method 1: Smartphone**
```
1. Press P (open smartphone)
2. Open CRIME STATS app
3. View current wanted level
```

**Method 2: Chat Notification**
```
On gaining a wanted star:
  WANTED LEVEL: 3 Stars
  Reason: Assault on NPC
  Police alerted!
```

### Gaining Wanted Stars

The system automatically detects illegal actions, instantly adds the appropriate number of stars, notifies the player, and alerts nearby police NPCs.

```
Action: Punch NPC            -> +1 star
Action: Trespass gov. plot   -> +2 stars
Action: Kill NPC             -> +3 stars
Action: Attack police        -> +4 stars
Action: Kill police          -> +5 stars (cap)
```

### Losing Wanted Stars

There are four ways to reduce your wanted level:

1. **Auto-Decay** - -1 star per Minecraft day (no action required)
2. **Pay Bail** - `/bail` instantly clears all stars
3. **Serve Time** - Complete prison sentence, released with clean record
4. **Hide Successfully** - Escape mechanic reduces by 1 star

---

## Crime Levels

The wanted system defines **5 crime levels** based on star count:

| Stars | Level | Classification | Examples |
|-------|-------|----------------|----------|
| 0 | **Clean** | No criminal record | Law-abiding citizen |
| 1-2 | **Misdemeanor** | Minor offenses | Assault, petty theft, trespassing |
| 3-4 | **Felony** | Serious offenses | Murder, armed robbery, drug production |
| 5 | **Maximum** | Highest severity | Killing police, terrorism, mass crime |

### Tier 1 Offenses (+1 Star)

**Minor Crimes:**
- Attacking NPCs (non-lethal)
- Minor trespassing
- Petty theft
- Disturbing peace
- Attacking smartphone users

### Tier 2 Offenses (+2 Stars)

**Moderate Crimes:**
- Government plot trespassing
- Repeated assault
- Significant theft
- Property damage

### Tier 3 Offenses (+3 Stars)

**Serious Crimes:**
- Killing NPCs
- Major theft
- Vandalism
- Drug production (if caught)

### Tier 4 Offenses (+4 Stars)

**Major Crimes:**
- Attacking police officers
- Mass destruction
- Organized crime
- Multiple murders

### Tier 5 / Maximum (+5 Stars)

**Extreme Crimes:**
- Killing police officers
- Terrorism
- Multiple serious crimes
- Resisting arrest violently

---

## Auto-Decay

Wanted levels automatically decrease over time without any player action required.

**Rate:** -1 star per Minecraft day

```
Day 1: 5 stars (Maximum)
Day 2: 4 stars (Major)
Day 3: 3 stars (Serious)
Day 4: 2 stars (Moderate)
Day 5: 1 star  (Minor)
Day 6: 0 stars (Clean)
```

The decay only applies when no new crimes are committed. Committing a new crime resets the decay timer and adds stars.

---

## Escape Mechanic

Players can reduce their wanted level by successfully hiding from police.

### Requirements

- **Duration:** Stay hidden for **30 seconds** continuously
- **Distance:** Maintain **40+ blocks** distance from all police NPCs
- **Result:** Wanted level reduced by 1 star on success

### How It Works

```
1. Player breaks line of sight with police
2. Escape timer starts (30 seconds)
3. Player must stay 40+ blocks from all police
4. If police re-detect player, timer resets
5. After 30 seconds: -1 star, police give up chase

Example:
  Wanted: 3 stars
  Run into building, close doors
  Time: 10s -> Police searching area
  Time: 20s -> Police confused
  Time: 30s -> Escape successful!
  Result: 3 stars -> 2 stars
```

### Best Hiding Spots

```
- Your own plot (with walls and doors)
- Friend's plot (trusted access)
- Complex multi-story buildings
- Underground areas
- Locations far from police patrol routes
```

---

## Police AI

### Patrol Mode (No Wanted Players)

When no wanted players are nearby, police NPCs follow standard patrol behavior:
- Walk between designated patrol points (up to 16)
- Wait at each point (default: 3 minutes)
- Wander within radius (default: 3 blocks)
- Continuous loop through all points

### Chase Mode (Wanted Player Detected)

When a wanted player is detected, police behavior changes dramatically:

**Detection Range (scales with wanted level):**

| Stars | Detection Range |
|-------|----------------|
| 1 | 20 blocks |
| 2 | 35 blocks |
| 3 | 50 blocks |
| 4 | 75 blocks |
| 5 | 100 blocks |

**Chase Behavior:**
```
1. Detection: Police scans for wanted players within range
2. Pursuit: Run toward criminal (speed scales with wanted level)
3. Pathfinding: Navigate around obstacles, open doors
4. Arrest: Get within 2 blocks -> automatic arrest trigger
5. Teleport: Player sent to prison
```

### Backup Calling

The `PoliceBackupSystem` allows police NPCs to call for reinforcements:

```
Officer detects 3-star criminal:
  -> Calls backup via PoliceBackupSystem
  -> Nearby officers switch to chase mode
  -> Multiple officers converge on criminal
  -> Higher wanted level = more officers respond
```

### Door Blocking

The `PoliceDoorBlockHandler` enables police to block exit routes:

```
Criminal runs into building:
  -> Police officer blocks doorway
  -> Other officers search interior
  -> Prevents easy escape through doors
```

### Arrest Process

```
1. Police officer reaches criminal (within 2 blocks)
2. Arrest trigger activates
3. Player teleported to prison
4. Assigned to cell based on wanted level (security 1-5)
5. Sentence calculated based on crime severity
6. Options: Serve time or Pay bail
```

---

## Prison System

### Prison Structure

**Components:**
1. **Prison Plot** - Government type plot
2. **Prison Cells** - Up to 99 cells
3. **Security Levels** - 1 through 5
4. **Bail Office** - Located at hospital

### Security Levels

| Level | Name | Conditions | Assigned For |
|-------|------|------------|-------------|
| 1 | Minimum | Comfortable, windows | 1 star |
| 2 | Low | Basic, small window | 2 stars |
| 3 | Medium | Cramped, no window | 3 stars |
| 4 | High | Very small, isolated | 4 stars |
| 5 | Maximum | Solitary, minimal space | 5 stars |

Cell assignment matches crime severity: a player arrested at 3 stars is placed in a Security Level 3 cell.

### Creating a Prison (Admin)

```bash
# Step 1: Create prison plot
/plot create government "City_Prison"
/prison create City_Prison

# Step 2: Add cells with coordinates and security level
/prison addcell 1 100 50 10 105 55 15 1    # Cell 1, Security 1
/prison addcell 2 110 50 10 113 53 13 3    # Cell 2, Security 3
/prison addcell 3 120 50 10 122 52 12 5    # Cell 3, Security 5

# Step 3: Verify setup
/prison cells
```

### Serving Time

```
In Prison:
  Sentence: Based on wanted level (real-world time)
  Cell: Assigned by security level

  Options while imprisoned:
    /bail      - Pay to leave immediately
    /jailtime  - Check remaining sentence

  Auto-Release:
    Sentence expires -> Teleported to hospital spawn
    Wanted level cleared -> Free to go
```

---

## Bail System

### Bail Costs

Bail is paid at the hospital to instantly clear all charges.

| Wanted Level | Bail Cost |
|-------------|-----------|
| 1 star | 1,000 |
| 2 stars | 2,500 |
| 3 stars | 5,000 |
| 4 stars | 10,000 |
| 5 stars | 20,000 |

### Paying Bail

```bash
/bail
```

**Process:**
```
1. Check if player is in prison
2. Calculate bail based on wanted level
3. Check player balance
4. Deduct bail amount
5. Release player
6. Teleport to hospital
7. Clear wanted level
```

**Example:**
```
You are in prison.
Wanted Level: 3 stars
Bail Amount: 5,000
Your Balance: 7,500

/bail

Bail Paid: 5,000
New Balance: 2,500
You have been released!
Teleporting to hospital...
Wanted level cleared.
```

### Insufficient Funds

If you cannot afford bail:

1. **Ask a friend for money** - They use `/pay YourName <amount>`
2. **Serve the full sentence** - Free release at end of sentence
3. **Admin release** - `/prison release <player>` (emergency/bugs only)

---

## Raid Penalties

When a player is arrested, a **raid penalty** confiscates a portion of their illegal cash.

**Formula:**
```
Confiscated = Random(10-30%) of wallet at time of arrest

Example:
  Wallet at arrest: 10,000
  Raid penalty (25%): 2,500
  Remaining: 7,500

  Confiscated cash goes to: State Treasury
```

This creates a strong financial incentive to avoid arrest. Players should keep money in the bank rather than carrying large amounts of cash.

---

## Bounty System

The bounty system allows players to place monetary bounties on other players, managed by the `BountyManager`.

### How It Works

```
1. Player A places bounty on Player B
2. Bounty amount deducted from Player A
3. Bounty is publicly visible
4. Any player who defeats Player B collects the bounty
5. Bounties persist across sessions (BountyData saved to disk)
```

### Bounty Commands

```bash
/bounty set <player> <amount>    # Place a bounty
/bounty check <player>           # Check bounty on a player
/bounty list                     # List all active bounties
/bounty remove <player>          # Remove your bounty (admin)
```

---

## Commands

The Police & Crime System provides **10 commands** across four command groups:

### Prison Commands

```bash
/prison create <name>                                    # Create prison
/prison addcell <num> <x1> <y1> <z1> <x2> <y2> <z2> [security]  # Add cell
/prison cells                                            # List all cells
/prison inmates                                          # List prisoners
/prison release <player>                                 # Release player (admin)
/prison list                                             # List all prisons
```

### Bail Commands

```bash
/bail                    # Pay bail to leave prison
```

### Jail Time Commands

```bash
/jailtime                # Check remaining sentence
```

### Bounty Commands

```bash
/bounty set <player> <amount>    # Place bounty
/bounty check <player>           # Check bounty
/bounty list                     # List active bounties
/bounty remove <player>          # Remove bounty (admin)
```

---

## Developer API

### IPoliceAPI Interface

External mods can access the police and crime system through the `IPoliceAPI` interface.

**Access:**
```java
IPoliceAPI policeAPI = ScheduleMCAPI.getPoliceAPI();
```

### Core Methods (v3.0.0+)

| Method | Description |
|--------|-------------|
| `getWantedLevel(UUID)` | Get current wanted level (0-5) |
| `addWantedLevel(UUID, int)` | Add wanted stars (capped at 5) |
| `setWantedLevel(UUID, int)` | Set wanted level directly |
| `clearWantedLevel(UUID)` | Reset wanted level to 0 |
| `decayWantedLevel(UUID)` | Reduce by 1 star (called per MC day) |
| `startEscape(UUID)` | Start 30-second escape timer |
| `stopEscape(UUID)` | Cancel escape timer (police re-detected player) |
| `isHiding(UUID)` | Check if escape timer is active |
| `getEscapeTimeRemaining(UUID)` | Remaining escape time in milliseconds |
| `checkEscapeSuccess(UUID)` | Check if escape succeeded, reduce wanted level |

### Extended Methods (v3.2.0+)

| Method | Description |
|--------|-------------|
| `getAllWantedPlayers()` | Map of all wanted player UUIDs to their level |
| `getPlayersAtWantedLevel(int)` | Set of player UUIDs at a specific level |
| `getWantedPlayerCount()` | Total count of wanted players |
| `isImprisoned(UUID)` | Check if a player is in prison |
| `getRemainingJailTime(UUID)` | Remaining jail time in seconds |
| `releaseFromPrison(UUID)` | Programmatically release a player |
| `getBailAmount(UUID)` | Get bail cost based on wanted level |

### Example Usage

```java
IPoliceAPI policeAPI = ScheduleMCAPI.getPoliceAPI();

// Check a player's wanted level
int wantedLevel = policeAPI.getWantedLevel(playerUUID);

// Add wanted stars for a crime
policeAPI.addWantedLevel(playerUUID, 2); // +2 stars

// Clear wanted level after arrest/bail
policeAPI.clearWantedLevel(playerUUID);

// Start escape timer
policeAPI.startEscape(playerUUID);

// Check if player is hiding
if (policeAPI.isHiding(playerUUID)) {
    long remaining = policeAPI.getEscapeTimeRemaining(playerUUID);
    // remaining is in milliseconds
}

// Check escape result
if (policeAPI.checkEscapeSuccess(playerUUID)) {
    // Wanted level was reduced by 1
}

// Prison integration
if (policeAPI.isImprisoned(playerUUID)) {
    long jailSeconds = policeAPI.getRemainingJailTime(playerUUID);
    double bail = policeAPI.getBailAmount(playerUUID);
}

// Get all wanted players
Map<UUID, Integer> wanted = policeAPI.getAllWantedPlayers();
```

**Thread Safety:** All methods are thread-safe through ConcurrentHashMap and atomic operations.

---

## Best Practices

### For Players

#### 1. Keep Bail Money Ready

```
Recommended Balance Reserve:
  1-star risk: 1,000
  2-star risk: 2,500
  3-star risk: 5,000

Keep money in bank, not wallet (wallet can be raided on arrest).
```

#### 2. Know Your Escape Routes

```
Plan ahead:
- Locate nearest safe plot
- Know complex buildings nearby
- Have friends' plots trusted for access
- Identify multiple escape paths
```

#### 3. Avoid Escalation

```
Have 1 star? Do NOT:
- Attack police (adds more stars)
- Resist arrest (adds time)
- Commit more crimes

Instead:
- Hide immediately
- Pay bail
- Wait for auto-decay
```

#### 4. Use Smartphone Protection

```
While smartphone is open:
- You are immune to police damage
- You cannot be arrested
- Use to check wanted level, pay bail, navigate to safety
```

### For Admins

#### 1. Build Proper Prisons

```
Requirements:
- Government plot type
- Multiple cells (5-10 minimum)
- Varied security levels (1-5)
- Bail office area at hospital
- Good spawn point for released players
```

#### 2. Configure Adequate Police

```
Small server:  2-3 police NPCs
Medium server: 5-8 police NPCs
Large server:  10+ police NPCs

Set patrols in:
- Downtown areas
- Government buildings
- High-traffic zones
```

#### 3. Monitor the System

```bash
/prison inmates          # Check current prisoners
/prison release <player> # Release if needed (bugs)
/prison cells            # Verify cell configuration
```

---

## Troubleshooting

### Police Not Chasing

**Causes:**
1. No police NPCs spawned
2. Police too far from wanted player
3. Police movement disabled
4. Wanted level is 0

**Solutions:**
```bash
/npc list                        # Verify police exist
/npc spawn polizei Officer_1     # Spawn more police
/npc Officer_1 movement true     # Enable movement
```

### Cannot Pay Bail

**Causes:**
1. Insufficient funds
2. Not currently in prison
3. Command disabled by config

**Solutions:**
```
1. Check balance: /money
2. Get money from friend: Have them /pay you
3. Serve time instead (wait for release)
4. Contact admin if bugged
```

### Stuck in Prison

**Problem:** Sentence expired but player not released

**Solutions:**
```bash
/jailtime                        # Check remaining time
/prison release <player>         # Admin manual release
```

### Wanted Level Not Decreasing

**Expected:** -1 star per Minecraft day

**Checks:**
```
1. Wait a full Minecraft day cycle
2. Check with smartphone CRIME STATS app
3. Ensure no new crimes were committed (resets decay timer)
4. May require server restart if bugged
```

---

<div align="center">

**Police & Crime System - Complete Guide**

For related systems:
- [NPC System](NPC-System.md)
- [Smartphone System](Smartphone-System.md)
- [Economy System](Economy-System.md)
- [Plot System](Plot-System.md)

[Back to Wiki Home](../Home.md) | [All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
