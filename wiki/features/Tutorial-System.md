# Tutorial System

> **STATUS: NOT IMPLEMENTED** - The Tutorial System described on this page was planned but has not been implemented in the current codebase. No tutorial-related code, commands, or manager classes exist in ScheduleMC v3.7.0-beta. This page is retained for reference as a design document for potential future implementation.

<div align="center">

**7-Step Interactive Onboarding for New Players (PLANNED - NOT IMPLEMENTED)**

Complete guide from basics to advanced features

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Tutorial Steps](#tutorial-steps)
3. [Tutorial Commands](#tutorial-commands)
4. [Rewards System](#rewards-system)
5. [Progress Tracking](#progress-tracking)
6. [Skipping & Quitting](#skipping--quitting)
7. [Best Practices](#best-practices)

---

## Overview

The Tutorial System provides a comprehensive 7-step onboarding experience for new players, teaching all core game mechanics with rewards.

### Key Features

✅ **7 Progressive Steps** - From basics to advanced
✅ **Interactive Learning** - Hands-on tasks
✅ **Generous Rewards** - 5,000€ total completion bonus
✅ **Skip/Quit Options** - Flexible progression
✅ **Progress Tracking** - Save/resume anytime
✅ **Achievement Integration** - Unlocks on completion

---

## Tutorial Steps

### Step 1: Welcome & Economy Basics

**Duration:** 5 minutes
**Reward:** 500€

**Objectives:**
- Learn about the economy system
- Understand money management
- Check your starting balance
- Learn /money command

**Tasks:**
```bash
1. Run /money to check balance
   Expected: 1,000€ starting balance

2. Learn about daily rewards
   Info: /daily gives 50€+ daily

3. Understand transactions
   Info: Use /pay to send money
```

**Completion Message:**
```
✓ Step 1 Complete: Economy Basics!

You learned:
- How to check your balance (/money)
- Daily reward system (/daily)
- Money transfers (/pay)

Reward: +500€
New Balance: 1,500€

Progress: 1/7 steps (14.3%)
Next: Step 2 - Daily Rewards
```

---

### Step 2: Daily Rewards

**Duration:** 2 minutes
**Reward:** 100€

**Objectives:**
- Claim your first daily reward
- Understand streak bonuses
- Learn reward progression

**Tasks:**
```bash
1. Claim daily reward
   Command: /daily

2. Check streak info
   Command: /daily streak

3. Understand bonuses
   Info: +10€ per day of streak
   Max: 340€/day at 30-day streak
```

**Completion Message:**
```
✓ Step 2 Complete: Daily Rewards!

You claimed your first daily reward!
Base: 50€
Streak: Day 1

Remember: Claim every 24h to maintain streak!
Max reward: 340€/day (30-day streak)

Reward: +100€
New Balance: 1,650€

Progress: 2/7 steps (28.6%)
Next: Step 3 - Plot Creation
```

---

### Step 3: Create Your First Plot

**Duration:** 10 minutes
**Reward:** 1,000€

**Objectives:**
- Get plot selection tool
- Select a plot area
- Create residential plot
- Understand plot types

**Tasks:**
```bash
1. Get plot tool
   Command: /plot wand
   Item: Golden Axe received

2. Select first corner
   Action: Left-click a block
   Feedback: "First position set"

3. Select second corner
   Action: Right-click a block
   Feedback: "Second position set"

4. Create plot
   Command: /plot create residential "My_Home" 50000

5. Check plot info
   Command: /plot info
```

**Helpful Tips:**
```
Plot Size Recommendations:
- Small home: 15×15×10
- Medium home: 25×25×15
- Large estate: 50×50×20

Plot Types:
- RESIDENTIAL: Homes (✓ recommended for tutorial)
- COMMERCIAL: Businesses
- SHOP: NPC stores (admin only)
```

**Completion Message:**
```
✓ Step 3 Complete: First Plot Created!

You created your first plot!
Plot: My_Home
Type: Residential
Size: 25×25×15 (9,375 blocks)

You now own land in ScheduleMC!
Use /plot info to view details.

Reward: +1,000€
New Balance: 2,650€

Progress: 3/7 steps (42.9%)
Next: Step 4 - Production Setup
```

---

### Step 4: Production Setup

**Duration:** 15 minutes
**Reward:** 1,200€

**Objectives:**
- Learn about production systems
- Start basic production (mushroom recommended)
- Harvest first product
- Understand quality system

**Tasks:**
```bash
1. Choose production system
   Recommended: Mushroom (easiest)
   Alternative: Tobacco

2. Obtain seeds/spores
   Admin gives: Mushroom spores
   Or: Find in world

3. Plant and grow
   Place: Terracotta pot
   Plant: Mushroom spores
   Wait: 25-35 minutes (or use boosters)

4. Harvest first product
   Action: Right-click mature plant
   Receive: Mushroom caps

5. Check quality
   Hover: View quality tier
   Learn: Poor/Good/Very Good/Legendary
```

**Production Comparison:**
```
Easiest (Recommended for Tutorial):
★ Mushroom: 4 steps, 25-35 min, simple

Beginner-Friendly:
★★ Tobacco: 6 steps, 45-60 min, quality system
★★ Cannabis: 8 steps, 30-45 min, multiple products

Advanced (Skip for tutorial):
★★★★★ Meth: Explosion risk
★★★★★ LSD: Complex lab process
```

**Completion Message:**
```
✓ Step 4 Complete: Production Mastered!

You completed your first production!
Item: Mushroom Caps
Quality: Good
Quantity: 4

You're now a producer in ScheduleMC!
Check /market prices to see item values.

Reward: +1,200€
New Balance: 3,850€

Progress: 4/7 steps (57.1%)
Next: Step 5 - NPC Interaction
```

---

### Step 5: NPC Interaction

**Duration:** 10 minutes
**Reward:** 800€

**Objectives:**
- Find NPC merchant
- Sell products to NPC
- Buy items from NPC
- Understand NPC schedules

**Tasks:**
```bash
1. Locate NPC merchant
   Look for: NPC with shop assignment
   Check: /npc <name> info (if admin)

2. Interact with NPC
   Action: Right-click NPC
   Opens: Shop GUI

3. Sell your products
   Click: Your mushroom caps
   Receive: Money based on quality/market price

4. Buy from NPC
   Browse: Available items
   Purchase: Select and buy

5. Learn NPC schedules
   Info: NPCs work 07:00-18:00
   Info: NPCs sleep 23:00-07:00
   Info: Leisure time between work and sleep
```

**NPC Types:**
```
Merchant:
- Buys player products
- Sells items from inventory/warehouse
- Has wallet for purchases

Resident:
- Roleplay NPCs
- Don't trade
- Walk around town

Police:
- Law enforcement
- Chase criminals
- Issue wanted levels
```

**Completion Message:**
```
✓ Step 5 Complete: NPC Expert!

You successfully interacted with NPCs!
Items Sold: Mushroom Caps × 4
Money Earned: ~600€
Items Bought: (your purchases)

NPCs are essential for economy!
Sell high-quality items for best prices.

Reward: +800€
New Balance: 5,250€

Progress: 5/7 steps (71.4%)
Next: Step 6 - Crime & Police
```

---

### Step 6: Crime & Police System

**Duration:** 10 minutes
**Reward:** 600€

**Objectives:**
- Understand wanted levels
- Learn about police NPCs
- Know how to pay bail
- Understand prison system

**Tasks:**
```bash
1. Learn wanted system
   Info: ⭐-⭐⭐⭐⭐⭐ (1-5 stars)
   Causes: Attacking NPCs, trespassing, crime

2. Understand police behavior
   Info: Police chase criminals
   Info: Arrest at prison
   Options: Serve time or pay bail

3. Learn about bail
   Command: /bail (when arrested)
   Cost: Based on wanted level

4. Check jail time
   Command: /jailtime (when imprisoned)

5. Crime prevention
   Tip: Stay on private plots
   Tip: Avoid government plots
   Tip: Pay bail quickly
```

**Wanted Level Guide:**
```
⭐       (1 star): Minor offense, 1,000€ bail
⭐⭐      (2 stars): Moderate, 2,500€ bail
⭐⭐⭐     (3 stars): Serious, 5,000€ bail
⭐⭐⭐⭐    (4 stars): Major, 10,000€ bail
⭐⭐⭐⭐⭐   (5 stars): Extreme, 20,000€ bail

Auto-decay: -1 star per day
```

**Completion Message:**
```
✓ Step 6 Complete: Crime System Understood!

You learned about:
- Wanted level system (⭐-⭐⭐⭐⭐⭐)
- Police NPCs and arrests
- Bail system (/bail)
- Prison mechanics

Stay lawful or be prepared to pay!

Reward: +600€
New Balance: 5,850€

Progress: 6/7 steps (85.7%)
Next: Step 7 - Advanced Features
```

---

### Step 7: Advanced Features

**Duration:** 20 minutes
**Reward:** 1,300€ + Achievement

**Objectives:**
- Explore smartphone system
- Learn about vehicles
- Master savings accounts
- Complete tutorial achievement

**Tasks:**
```bash
1. Open smartphone
   Keybind: P (default)
   Explore: 11 apps

   Available Apps:
   - MAP: Plot locations
   - DEALER: Tobacco dealers
   - PRODUCTS: Shop catalog
   - ORDER: Order management
   - CONTACTS: Player & NPC contacts
   - MESSAGES: Inbox & chat
   - PLOT: Plot management
   - SETTINGS: Config
   - BANK: Banking
   - CRIME STATS: Wanted level
   - CHAT: Direct messaging

2. Learn about vehicles
   Info: Cars, trucks, SUVs available
   Fuel: Requires gasoline/diesel
   Repair: Maintenance kits

3. Explore savings
   Command: /savings create 1000
   Learn: 5% weekly interest, 4-week lock

4. Review all systems
   Summary of learned mechanics
```

**Smartphone Features:**
```
📱 PvP Protection:
- Immune to damage while using
- Attackers get +1 wanted star
- Safe interface usage

📱 Key Apps:
- BANK: Manage accounts remotely
- CRIME STATS: Check wanted level
- PLOT: Manage properties
- MAP: Navigate world
```

**Final Completion Message:**
```
🎉 TUTORIAL COMPLETE! 🎉

Congratulations! You've mastered ScheduleMC!

Steps Completed: 7/7 (100%)
Time Spent: ~60 minutes
Total Rewards: 5,000€
Final Balance: 6,850€

Achievements Unlocked:
🏆 Tutorial Master
🏆 First Steps
🏆 Economy Basics
🏆 Property Owner
🏆 Producer
🏆 Merchant

You're ready for the full ScheduleMC experience!

Recommended Next Steps:
1. Build on your plot
2. Expand production
3. Join server economy
4. Help other new players!

Use /tutorial to review anytime.
```

---

## Tutorial Commands

### Start Tutorial

```bash
/tutorial start
```

**Effect:**
- Starts from Step 1
- Resets progress (if previously started)
- Shows first objectives

---

### View Current Step

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
Estimated Time Remaining: 40 minutes
```

---

### Complete Current Step

```bash
/tutorial next
```

**Effect:**
- Marks current step as complete
- Awards reward
- Advances to next step

**Use When:**
- Task completed manually
- Auto-advancement didn't trigger
- Stuck on objective

---

### Skip Current Step

```bash
/tutorial skip
```

**Warning:**
- ⚠️ No reward for skipped step
- Progress still counts
- Can't go back

**Output:**
```
⚠️ Step Skipped

Step 3 skipped (no reward)
Progress: 3/7 (42.9%)
Next: Step 4 - Production Setup
```

---

### Quit Tutorial

```bash
/tutorial quit
```

**Effect:**
- Exits tutorial temporarily
- Progress saved
- Can resume anytime with /tutorial

---

### Reset Tutorial

```bash
/tutorial reset
```

**Warning:**
- ⚠️ Resets ALL progress
- Previous rewards NOT refunded
- Cannot reclaim rewards

**Confirmation:**
```
⚠️ Reset Tutorial?

This will:
- Reset progress to 0/7
- Restart from Step 1
- You will NOT receive rewards again

Type /tutorial reset confirm to proceed
```

---

### View Status

```bash
/tutorial status
```

**Output:**
```
═══ TUTORIAL STATUS ═══

Progress: 5/7 steps (71.4%)
Rewards Earned: 3,600€
Time Spent: 45 minutes

Completed Steps:
✓ Step 1: Welcome & Economy (500€)
✓ Step 2: Daily Rewards (100€)
✓ Step 3: Plot Creation (1,000€)
✓ Step 4: Production Setup (1,200€)
✓ Step 5: NPC Interaction (800€)
✗ Step 6: Crime System (current)
✗ Step 7: Advanced Features

Estimated Time to Complete: 20 minutes
Total Possible Rewards: 5,000€
Remaining Rewards: 1,400€
```

---

## Rewards System

### Reward Breakdown

| Step | Topic | Duration | Reward | Cumulative |
|------|-------|----------|--------|------------|
| 1 | Economy Basics | 5 min | 500€ | 500€ |
| 2 | Daily Rewards | 2 min | 100€ | 600€ |
| 3 | Plot Creation | 10 min | 1,000€ | 1,600€ |
| 4 | Production | 15 min | 1,200€ | 2,800€ |
| 5 | NPC Interaction | 10 min | 800€ | 3,600€ |
| 6 | Crime System | 10 min | 600€ | 4,200€ |
| 7 | Advanced Features | 20 min | 1,300€ | **5,500€** |

**Total:** 5,500€ + Achievement

---

### Bonus Rewards

**Achievement:** Tutorial Master
- Unlocked: On completing all 7 steps
- Badge: 🏆 Tutorial Master
- Benefits: Access to exclusive content (if configured)

**Starting Balance Boost:**
```
Default Starting: 1,000€
Tutorial Completion: +5,500€
Final Balance: 6,500€

Additional:
+ Daily reward claims during tutorial
+ Income from selling products
= ~7,000-8,000€ total by end
```

---

## Progress Tracking

### Automatic Saving

**Progress Auto-Saved:**
- After each step completion
- Every 5 minutes during active play
- On logout
- On server restart

**Saved Data:**
- Current step (1-7)
- Completion status per step
- Rewards claimed
- Time spent
- Longest step completed

---

### Resume Tutorial

**Auto-Resume:**
```
Player logs in:
→ Tutorial incomplete? (5/7 steps)
→ Shows current step objectives
→ Continues from last checkpoint
```

**Manual Resume:**
```bash
/tutorial
# Shows current step and objectives
```

---

## Skipping & Quitting

### When to Skip

**Good Reasons:**
- Already familiar with mechanic
- Stuck due to bug
- Want to explore freely
- Time constraints

**Consequences:**
- Lose step reward
- Progress still counts
- Can't redo for reward

---

### When to Quit

**Temporary Exit:**
```bash
/tutorial quit
# Progress saved, resume later
```

**Use Cases:**
- Need a break
- Want to explore
- Friend needs help
- Server event

**Resuming:**
```bash
/tutorial
# Automatically shows where you left off
```

---

## Best Practices

### For New Players

#### 1. Complete in One Session
```
Recommended: 60-90 minute session
Benefit: Momentum, context retention
Result: Better understanding
```

---

#### 2. Don't Skip Steps
```
Each step teaches crucial mechanics
Rewards are generous (5,500€ total)
Foundation for advanced gameplay
```

---

#### 3. Ask Questions
```
Use chat: Ask experienced players
Discord: Community support
Wiki: Reference documentation
```

---

#### 4. Take Your Time
```
Don't rush through
Read instructions carefully
Experiment with features
Understanding > Speed
```

---

### For Server Admins

#### 1. Encourage Tutorial
```
Welcome message: Mention /tutorial start
Incentivize: Additional server rewards
Support: Help stuck players
```

---

#### 2. Monitor Completion
```
Track: Completion rate
Identify: Common stuck points
Improve: Tutorial clarity
```

---

#### 3. Provide Support
```
Helpers: Assign tutorial assistants
FAQ: Common tutorial questions
Resources: Video guides
```

---

<div align="center">

**Tutorial System - Complete Guide**

For related systems:
- [💰 Economy System](Economy-System.md)
- [🏘️ Plot System](Plot-System.md)
- [🌿 Production Systems](../Production-Systems.md)

[🏠 Back to Wiki Home](../Home.md) • [📋 All Commands](../Commands.md)

**Last Updated:** 2026-04-13 | **ScheduleMC v3.7.0-beta**

</div>

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-13**.
- Diese Datei wurde im Rahmen der Vollständigkeits-Aktualisierung überarbeitet.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

