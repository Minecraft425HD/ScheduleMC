# Police & Crime System

<div align="center">

**GTA-Style 5-Star Wanted Level & Prison System**

Dynamic law enforcement with NPC police officers

[🏠 Back to Wiki Home](../Home.md) • [📋 Commands Reference](../Commands.md)

</div>

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Wanted Level System](#wanted-level-system)
3. [Criminal Activities](#criminal-activities)
4. [Police NPCs](#police-npcs)
5. [Prison System](#prison-system)
6. [Bail System](#bail-system)
7. [Penalties & Consequences](#penalties--consequences)
8. [Avoiding Arrest](#avoiding-arrest)
9. [Best Practices](#best-practices)
10. [Troubleshooting](#troubleshooting)

---

## Overview

The Police & Crime System brings GTA-style law enforcement to ScheduleMC with a 5-star wanted level system, NPC police officers, and a functional prison system.

### Key Features

✅ **5-Star Wanted System** - ⭐ to ⭐⭐⭐⭐⭐
✅ **Police NPCs** - Chase and arrest criminals
✅ **Prison System** - Cells with security levels 1-5
✅ **Bail Payments** - Pay to get out early
✅ **Auto-Decay** - -1 star per day
✅ **Smartphone Integration** - Check wanted level
✅ **Penalties** - Cash confiscation, time served
✅ **Hiding Mechanics** - Evade police in buildings

---

## Wanted Level System

### Star Levels

| Stars | Severity | Police Response | Bail Cost | Description |
|-------|----------|-----------------|-----------|-------------|
| ⭐ | Minor | 1 officer, walking | 1,000€ | First offense |
| ⭐⭐ | Moderate | 1-2 officers, jogging | 2,500€ | Repeat offender |
| ⭐⭐⭐ | Serious | 2-3 officers, running | 5,000€ | Dangerous criminal |
| ⭐⭐⭐⭐ | Major | 3-4 officers, aggressive | 10,000€ | High threat |
| ⭐⭐⭐⭐⭐ | Extreme | All police, relentless | 20,000€ | Most wanted |

---

### Checking Wanted Level

**Method 1: Smartphone**
```
1. Press P (open smartphone)
2. Open CRIME STATS app
3. View current wanted level
```

**Method 2: Notification**
```
On gaining wanted star:
🚨 WANTED LEVEL: ⭐⭐⭐
Reason: Assault on NPC
Police alerted!
```

**Method 3: Direct Check**
```
Look in chat for wanted level updates
Check HUD (if configured)
```

---

### Gaining Wanted Stars

**Automatic System:**
- System detects illegal actions
- Instantly adds appropriate stars
- Notifies player
- Alerts nearby police NPCs

**Examples:**
```
Action: Punch NPC
Result: +1 star ⭐
Message: "Assault on NPC detected!"

Action: Trespass government plot
Result: +2 stars ⭐⭐
Message: "Trespassing on government property!"

Action: Kill NPC
Result: +3 stars ⭐⭐⭐
Message: "Murder! Police notified!"

Action: Attack police officer
Result: +4 stars ⭐⭐⭐⭐
Message: "Assaulting an officer!"

Action: Multiple crimes
Result: +5 stars ⭐⭐⭐⭐⭐ (cap)
Message: "You are now extremely wanted!"
```

---

### Losing Wanted Stars

**Method 1: Auto-Decay**
```
System: -1 star per real-world day
No player action required

Example:
Day 1: ⭐⭐⭐⭐⭐ (5 stars)
Day 2: ⭐⭐⭐⭐ (4 stars)
Day 3: ⭐⭐⭐ (3 stars)
...
Day 6: Clear
```

**Method 2: Pay Bail**
```bash
/bail
# Instantly clears all wanted stars
# Costs based on wanted level
```

**Method 3: Serve Time**
```
Get arrested → Sent to prison
Serve full sentence → Released with no stars
```

**Method 4: Hide Successfully**
```
Stay in buildings/plots
Police give up after time
Wanted level may reduce by 1-2 stars
```

---

## Criminal Activities

### Tier 1 Offenses (+1 Star ⭐)

**Minor Crimes:**
- Attacking NPCs (non-lethal)
- Minor trespassing
- Petty theft
- Disturbing peace
- Attacking smartphone users

**Example:**
```
You punch Shop_Owner NPC
→ +1 star ⭐
→ 1 police officer dispatched
→ Walking speed chase
→ Bail: 1,000€
```

---

### Tier 2 Offenses (+2 Stars ⭐⭐)

**Moderate Crimes:**
- Government plot trespassing
- Repeated assault
- Significant theft
- Property damage

**Example:**
```
You enter prison plot without permission
→ +2 stars ⭐⭐
→ 1-2 police officers dispatched
→ Jogging speed chase
→ Bail: 2,500€
```

---

### Tier 3 Offenses (+3 Stars ⭐⭐⭐)

**Serious Crimes:**
- Killing NPCs
- Major theft
- Vandalism
- Drug production (if caught)

**Example:**
```
You kill merchant NPC
→ +3 stars ⭐⭐⭐
→ 2-3 police officers dispatched
→ Running speed chase
→ Bail: 5,000€
```

---

### Tier 4 Offenses (+4 Stars ⭐⭐⭐⭐)

**Major Crimes:**
- Attacking police officers
- Mass destruction
- Organized crime
- Multiple murders

**Example:**
```
You attack police officer chasing you
→ +4 stars ⭐⭐⭐⭐
→ 3-4 police officers dispatched
→ Aggressive chase
→ Bail: 10,000€
```

---

### Tier 5 (Maximum ⭐⭐⭐⭐⭐)

**Extreme Crimes:**
- Killing police officers
- Terrorism
- Multiple serious crimes
- Resisting arrest violently

**Example:**
```
You kill police officer
→ +5 stars ⭐⭐⭐⭐⭐ (maximum)
→ ALL police officers dispatched
→ Relentless chase
→ Bail: 20,000€
```

---

## Police NPCs

### Police Behavior

**Patrol Mode (No Wanted Players):**
- Walk around designated areas
- Follow patrol routes
- Stand at stations
- Normal NPC behavior

**Chase Mode (Wanted Player Detected):**
```
1. Detection:
   - Police scan for wanted players within range
   - Range increases with wanted level
   - ⭐: 20 blocks
   - ⭐⭐⭐⭐⭐: 100 blocks

2. Pursuit:
   - Run toward criminal
   - Speed scales with wanted level
   - Pathfinding around obstacles
   - Never give up (until hidden or arrested)

3. Arrest:
   - Get within 2 blocks
   - Automatic arrest trigger
   - Teleport to prison
```

---

### Police Stats

**Configuration:**
- **Speed:** 0.3-0.8 (scales with wanted level)
- **Detection Range:** 20-100 blocks
- **Persistence:** High (don't give up easily)
- **Pathfinding:** Smart (navigate buildings)

**Example Chase:**
```
Wanted Level: ⭐⭐⭐ (3 stars)
Police Assigned: 2 officers
Police Speed: 0.5 (running)
Detection Range: 50 blocks

Officer_1: 45 blocks away → CHASING
Officer_2: 30 blocks away → CHASING

*You run into building*

Officer_1: Path blocked, finding route
Officer_2: Entering building

*You hide in corner*

Time hidden: 30 seconds
Officers: Searching...
Time hidden: 60 seconds
Officers: Giving up...
Wanted reduced: ⭐⭐⭐ → ⭐⭐
```

---

### Police Commands (Admin)

**Spawn Police:**
```bash
/npc spawn police Officer_Smith
```

**Set Patrol Route:**
```bash
/npc Officer_Smith leisure add
# At multiple locations for patrol
```

**Configure Behavior:**
```bash
/npc Officer_Smith movement true
/npc Officer_Smith speed 0.5
```

---

## Prison System

### Prison Structure

**Components:**
1. **Prison Plot** (Government type)
2. **Prison Cells** (1-99 cells)
3. **Security Levels** (1-5)
4. **Bail Office** (optional)

---

### Security Levels

| Level | Name | Conditions | For |
|-------|------|------------|-----|
| 1 | Minimum | Comfortable, windows | ⭐ |
| 2 | Low | Basic, small window | ⭐⭐ |
| 3 | Medium | Cramped, no window | ⭐⭐⭐ |
| 4 | High | Very small, isolated | ⭐⭐⭐⭐ |
| 5 | Maximum | Solitary, minimal space | ⭐⭐⭐⭐⭐ |

**Cell Assignment:**
```
Arrested with ⭐⭐⭐ → Assigned to Level 3 cell
Cell conditions match crime severity
Higher security = smaller, worse conditions
```

---

### Creating Prison (Admin)

**Step 1: Create Prison Plot**
```bash
/plot create government "City_Prison"
/prison create City_Prison
```

**Step 2: Add Cells**
```bash
# Syntax: /prison addcell <number> <x1> <y1> <z1> <x2> <y2> <z2> [security]

# Cell 1 (Minimum Security)
/prison addcell 1 100 50 10 105 55 15 1

# Cell 2 (Medium Security)
/prison addcell 2 110 50 10 113 53 13 3

# Cell 3 (Maximum Security)
/prison addcell 3 120 50 10 122 52 12 5
```

**Step 3: Verify**
```bash
/prison cells
# Lists all cells with security levels
```

---

### Arrest Process

**Automatic Arrest:**
```
1. Police officer reaches criminal (within 2 blocks)
2. Arrest trigger activates
3. Player teleported to prison
4. Assigned to appropriate cell based on wanted level
5. Sentence calculated
6. Options presented: Serve time or Pay bail
```

**Arrest Message:**
```
🚨 YOU HAVE BEEN ARRESTED! 🚨

Wanted Level: ⭐⭐⭐
Charges: Assault on NPC, Trespassing

Sentence: 3 days
Security Level: 3 (Medium)
Cell: #2

Cash Confiscated: 1,250€ (raid penalty)

Options:
[Pay Bail: 5,000€] [Serve Time]
```

---

### Serving Time

**In Prison:**
```
Sentence: 3 days (real-world time)
Current Cell: #2 (Security Level 3)

Time Remaining: 2 days, 14 hours
Release Date: 2024-01-18 16:30

Options:
/bail - Pay 5,000€ to leave now
/jailtime - Check remaining time
```

**Auto-Release:**
```
Sentence expires:
→ Teleported to hospital spawn
→ Wanted level cleared
→ Free to go
```

---

## Bail System

### Bail Costs

**By Wanted Level:**

| Wanted Level | Bail Cost | Quick Cash |
|--------------|-----------|------------|
| ⭐ | 1,000€ | Daily × 20 |
| ⭐⭐ | 2,500€ | Daily × 50 |
| ⭐⭐⭐ | 5,000€ | Daily × 100 |
| ⭐⭐⭐⭐ | 10,000€ | Daily × 200 |
| ⭐⭐⭐⭐⭐ | 20,000€ | Daily × 400 |

---

### Paying Bail

**Command:**
```bash
/bail
```

**Process:**
```
1. Check if in prison
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
Wanted Level: ⭐⭐⭐
Bail Amount: 5,000€
Your Balance: 7,500€

/bail

✓ Bail Paid: 5,000€
New Balance: 2,500€

You have been released!
Teleporting to hospital...
Wanted level cleared.
```

---

### Insufficient Funds

**Problem:**
```
/bail

✗ Insufficient Funds!
Bail Required: 5,000€
Your Balance: 2,300€
Missing: 2,700€
```

**Solutions:**
1. **Ask Friend for Money:**
   ```
   Friend uses: /pay YourName 3000
   You use: /bail
   ```

2. **Serve Time:**
   ```
   Wait for sentence to expire
   Free release at end
   ```

3. **Admin Release (if bug):**
   ```bash
   /prison release <player>
   ```

---

## Penalties & Consequences

### Cash Confiscation

**Raid Penalty:**
```
Formula: Confiscated = Random(10-30%) of wallet

Example:
Wallet at arrest: 10,000€
Raid penalty (25%): 2,500€
Remaining: 7,500€

Cash goes to: State Treasury
```

---

### Death Fee

**If you die while wanted:**
```
Normal death fee: 500€
+Wanted penalty: +100€ per star

Example (3 stars):
Base death fee: 500€
Wanted penalty: 300€ (3 × 100€)
Total: 800€
```

---

### Time Loss

**Opportunity Cost:**
```
3-day sentence = 3 real days locked up
Cannot:
- Produce items
- Trade with players
- Manage plots
- Earn money

Lost Income Estimate:
Daily rewards: 150€ × 3 = 450€
Production: ~1,000€ × 3 = 3,000€
Total: ~3,450€ lost opportunity

Bail (5,000€) often cheaper than serving time!
```

---

### Reputation Impact

**Social Consequences:**
- Appears in prison inmate list
- Other players see your arrest
- May affect trust for future dealings
- Server reputation damaged

---

## Avoiding Arrest

### Strategy 1: Stay Legal

**Best Approach:**
```
✓ Don't attack NPCs
✓ Don't trespass
✓ Produce legally on private plots
✓ Avoid conflict
✓ Pay bail immediately if stars gained
```

---

### Strategy 2: Hide in Buildings

**Mechanics:**
```
Enter building/plot:
→ Police pathfinding slowed
→ If hidden for 60 seconds:
   → Police may give up
   → Wanted level may reduce by 1-2 stars

Best Hiding Spots:
- Your own plot (with walls)
- Friend's plot
- Complex buildings
- Underground areas
```

**Example:**
```
Wanted: ⭐⭐⭐
Police: 20 blocks away

*Run into your house*
*Close doors*
*Hide in corner*

Time: 30 seconds → Police searching
Time: 60 seconds → Police confused
Time: 90 seconds → Police giving up

Wanted reduced: ⭐⭐⭐ → ⭐⭐
```

---

### Strategy 3: Pay Bail Immediately

**Quick Response:**
```
Gain wanted star:
1. Open smartphone (P)
2. BANK app
3. Keep enough for bail
4. Get arrested (or hide until safe)
5. Pay bail immediately

Cost: Bail amount
Benefit: No time lost, clean record
```

---

### Strategy 4: Use Smartphone Protection

**While using smartphone:**
```
You are immune to:
- Police damage
- Arrest (can't be caught)
- All attacks

Use to:
- Check wanted level
- Navigate to safety
- Pay bail remotely
- Call for help

⚠️ Cannot move between locations easily
⚠️ 3-second cooldown after closing
```

---

## Best Practices

### For Players

#### 1. Keep Bail Money Ready
```
Recommended Balance Reserve:
⭐ risk: 1,000€
⭐⭐ risk: 2,500€
⭐⭐⭐ risk: 5,000€

Keep in bank, not wallet (wallet can be raided)
```

---

#### 2. Know Your Escape Routes
```
Plan ahead:
- Locate nearest safe plot
- Know complex buildings
- Have friends' plots trusted
- Multiple escape paths
```

---

#### 3. Avoid Escalation
```
Have 1 star? Don't:
- Attack police (adds stars)
- Resist arrest (adds time)
- Cause more crimes

Do:
- Hide immediately
- Pay bail
- Wait for decay
```

---

#### 4. Produce Safely
```
Legal Production:
- Use private plots
- Don't produce in public
- Hide from view
- Sell discreetly
```

---

### For Admins

#### 1. Build Proper Prisons
```
Requirements:
- Government plot
- Multiple cells (5-10 minimum)
- Varied security levels
- Bail office area
- Good spawn point
```

---

#### 2. Configure Police
```
Spawn enough police:
- Small server: 2-3 police
- Medium server: 5-8 police
- Large server: 10+ police

Set patrols:
- Downtown areas
- Government buildings
- High-traffic zones
```

---

#### 3. Monitor System
```bash
# Check inmates
/prison inmates

# Release if needed
/prison release <player>

# Check prison health
/prison list
/prison cells
```

---

## Troubleshooting

### "Police Not Chasing Me"

**Causes:**
1. No police NPCs spawned
2. Police too far away
3. Police movement disabled
4. Wanted level too low

**Solutions:**
```bash
# Check police exist
/npc list

# Spawn more police (admin)
/npc spawn police Officer_1

# Enable movement (admin)
/npc Officer_1 movement true
```

---

### "Can't Pay Bail"

**Causes:**
1. Insufficient funds
2. Not in prison
3. Command disabled

**Solutions:**
```
1. Check balance: /money
2. Get money from friend: Have them /pay you
3. Serve time instead
4. Contact admin
```

---

### "Stuck in Prison"

**Problem:** Sentence expired but not released

**Solutions:**
```bash
# Check time remaining
/jailtime

# If should be free, contact admin
# Admin can manually release:
/prison release <yourname>
```

---

### "Wanted Level Not Decreasing"

**Expected:** -1 star per day

**Check:**
```
1. Wait full 24 hours
2. Check with smartphone CRIME STATS
3. May require server restart
4. Contact admin if bugged
```

---

<div align="center">

**Police & Crime System - Complete Guide**

For related systems:
- [🏘️ Plot System](Plot-System.md)
- [💰 Economy System](Economy-System.md)
- [📱 Smartphone System](Smartphone-System.md)

[🏠 Back to Wiki Home](../Home.md) • [📋 All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
