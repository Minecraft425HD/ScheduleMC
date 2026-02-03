# Getting Started with ScheduleMC

Complete beginner's guide to ScheduleMC - from installation to your first empire.

---

## 📦 Installation

### Prerequisites

| Requirement | Version | Download Link |
|-------------|---------|---------------|
| Minecraft | 1.20.1 | [minecraft.net](https://minecraft.net) |
| Forge | 47.4.0+ | [Forge Downloads](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) |
| Java | 17 | [Adoptium](https://adoptium.net/) |
| RAM | 4-8 GB | - |

### Step 1: Install Forge

```bash
# Download Forge installer
wget https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.0/forge-1.20.1-47.4.0-installer.jar

# Run installer (Windows/Mac: double-click)
java -jar forge-1.20.1-47.4.0-installer.jar

# Select "Install client" or "Install server"
```

### Step 2: Install ScheduleMC

**For Players:**
```
.minecraft/
└── mods/
    └── ScheduleMC-2.7.0-beta.jar
```

**For Servers:**
```
minecraft_server/
└── mods/
    └── ScheduleMC-2.7.0-beta.jar
```

### Step 3: First Launch

1. Launch Minecraft with Forge profile
2. Check mod list - ScheduleMC should appear
3. Join your server or create singleplayer world

---

## 🎮 For Players

### First Join

When you first join a ScheduleMC server:

✅ **Auto-Setup:**
- Bank account created (1,000€ balance)
- Tutorial notification
- Welcome message

✅ **Recommended First Steps:**
1. Check balance: `/money`
2. Get daily reward: `/daily`
3. Get plot tool: `/plot wand`
4. Read tutorial: `/tutorial start`

### Day 1 Guide

#### 1. Daily Reward (5 minutes)

```bash
/daily
```

**Result:**
- Base reward: 50€
- Streak bonus: +10€ per consecutive day (max 300€/day at 25-day streak)
- Don't miss a day or streak resets!

#### 2. Explore the Economy (10 minutes)

```bash
# Check your balance
/money
→ Shows: Balance, daily change, account age

# View transaction history
/money history
→ Shows: Last 10 transactions

# Send money to a friend
/pay PlayerName 100
→ Transfers 100€ (small fee may apply)
```

**Pro Tip:** Save some money for your first plot!

#### 3. Create Your First Plot (15 minutes)

```bash
# Get the plot selection tool
/plot wand

# Select area
→ Left-click first corner
→ Right-click second corner
→ Creates rectangular selection

# Create residential plot
/plot create residential "My Home" 50000
→ Name: "My Home"
→ Sale price: 50,000€ (if you want to sell later)

# Check your plot
/plot info
```

**Plot Types:**
- **Residential** - Homes, apartments
- **Commercial** - Businesses, offices
- **Shop** - NPC shops with inventory
- **Public** - Parks, roads (admin only)
- **Government** - Town halls, prisons (admin only)

#### 4. Manage Your Plot (Ongoing)

```bash
# Trust a friend
/plot trust FriendName

# Rent out your plot
/plot rent 100
→ 100€ per day rental

# Create apartment
/plot apartment create "Apartment 1A" 500
→ 500€/month rent
```

#### 5. Start Production (30 minutes)

**Tobacco Example:**

```bash
# Get seeds (from NPC or admin)
# Plant in pot (Terracotta/Ceramic/Iron/Golden)
# Water with watering can
# Apply fertilizer for faster growth

# Wait for growth (8 stages, ~10-20 minutes)
# Harvest at stage 7
→ Receive: Fresh tobacco leaves

# Dry on drying rack
→ 5 minutes → Dried leaves

# Ferment in barrel
→ 10 minutes → Fermented leaves

# Package at packaging table
→ Ready to sell!

# Sell to NPC merchants
→ Price depends on quality + market demand
```

### Week 1 Goals

**Day 1-3:**
- [x] Get daily rewards
- [x] Create first plot
- [x] Start tobacco production
- [x] Save 10,000€

**Day 4-7:**
- [x] Expand plot or buy second plot
- [x] Try different production (cannabis, etc.)
- [x] Build up 50,000€ savings

---

## 🛠️ For Server Admins

### Initial Server Setup (30 minutes)

#### 1. Create Spawn Areas

```bash
# Select spawn area
/plot wand
→ Left-click, right-click corners

# Create public spawn
/plot create public "Spawn"
→ No building restrictions for admins

# Create town hall
/plot create government "Town Hall"

# Create marketplace
/plot create commercial "Marketplace"
```

#### 2. Setup Economy

```bash
# Fund state account
/state deposit 1000000
→ Government treasury for payments

# Give starting money (optional)
/money give @a 5000
→ All players start with 5,000€

# Configure hospital
/hospital setspawn
→ Set at current location

/hospital setfee 500
→ 500€ death penalty
```

#### 3. Create NPCs

```bash
# Spawn shop owner
/npc spawn merchant Shop_Owner_Hans

# Set work schedule
/npc Hans schedule workstart 0700
/npc Hans schedule workend 1800
/npc Hans schedule home 2300

# Set locations
# 1. Face NPC, then face home location
/npc Hans location tool
→ Sets home

# 2. Set work location
→ Shop plot

# 3. Add leisure locations (max 10)
/npc Hans leisure add
→ At park, restaurant, etc.
```

#### 4. Setup NPC Shop

```bash
# Create shop plot
/plot wand
/plot create shop "General Store"

# Assign to NPC
/npc Hans setshop generalstore

# Give inventory
/npc Hans inventory give 0 minecraft:diamond
/npc Hans inventory give 1 minecraft:iron_ingot

# Set wallet
/npc Hans wallet set 10000
→ 10,000€ to buy from players
```

#### 5. Create Warehouse System

```bash
# Place warehouse block
→ Right-click to open GUI

# Link to shop
/warehouse setshop generalstore

# Link to NPC
/npc Hans warehouse set
→ Look at warehouse, run command

# Add stock
/warehouse add minecraft:diamond 64
/warehouse add minecraft:iron_ingot 128

# NPCs will now sell from warehouse
→ Auto-refill every 3 days
```

#### 6. Create Prison System

```bash
# Select prison plot
/plot wand
/plot create government "State Prison"

# Initialize prison
/prison create prison_main

# Add cells
/prison addcell 1 100,10,100 105,15,105 1
→ Cell 1, coords, security level 1

/prison addcell 2 110,10,100 115,15,105 2
→ Cell 2, higher security

# Configure bail
/hospital setfee 1000
→ Bail = 1,000€
```

### Production System Setup

#### Enable All Production Systems

```bash
# Tobacco
/tobacco give @a virginia_seeds
→ Give everyone starter seeds

# Cannabis (if enabled)
# Coca (if enabled)
# Etc. - All use similar commands
```

### Server Configuration

Edit `config/schedulemc-common.toml`:

```toml
[economy]
    startingBalance = 1000
    dailyReward = 50
    streakBonus = 10
    maxStreak = 30

[plots]
    maxPlotSize = 100
    minPlotPrice = 1000
    transferFee = 0.05

[npc]
    maxNPCs = 100
    salaryInterval = 86400
    defaultSalary = 100

[production]
    tobaccoEnabled = true
    cannabisEnabled = true
    # etc.
```

---

## 📚 Essential Commands

### For Everyone

| Command | Description | Example |
|---------|-------------|---------|
| `/daily` | Daily reward | `/daily` |
| `/money` | Check balance | `/money` |
| `/pay <player> <amount>` | Send money | `/pay Steve 1000` |
| `/plot wand` | Get plot tool | `/plot wand` |
| `/plot info` | Plot information | `/plot info` |
| `/plot create <type> <name> <price>` | Create plot | `/plot create residential "Home" 50000` |

### For Admins

| Command | Description | Example |
|---------|-------------|---------|
| `/money give <player> <amount>` | Give money | `/money give Steve 5000` |
| `/npc spawn <type> <name>` | Spawn NPC | `/npc spawn merchant Hans` |
| `/warehouse add <item> <amount>` | Add to warehouse | `/warehouse add diamond 64` |
| `/prison create <id>` | Create prison | `/prison create main` |

[Complete Command Reference →](Commands.md)

---

## 🎯 Common Tasks

### How to...

#### ...Buy a Plot?

```bash
# Find plot for sale
/plot list
→ Shows all plots

# Stand in the plot
/plot buy
→ Buys current plot

# Or specify ID
/plot buy plotId
```

#### ...Create an Apartment?

```bash
# In your plot
/plot apartment wand
→ Select apartment area

/plot apartment create "Apt 1A" 500
→ Name: "Apt 1A"
→ Monthly rent: 500€
```

#### ...Apply for a Loan?

```bash
# Check available loans
/loan info

# Apply for medium loan
/loan apply MEDIUM
→ Receive: 25,000€
→ Interest: 15%
→ Repay: 28,750€ within 28 days

# Repay early
/loan repay
→ Pays back full amount
```

#### ...Start Cannabis Production?

```bash
# 1. Plant seeds in pot
→ Right-click pot with seeds

# 2. Wait for growth (8 stages)
→ ~12-20 minutes

# 3. Harvest
→ Right-click mature plant

# 4. Process:
→ Trocknungsnetz (5 min) → Dried
→ Trimm Station (3 min) → Trimmed
→ Curing Glas (10 min) → Cured
→ Hash Presse OR Öl Extraktor

# 5. Package and sell
→ Packaging table
→ Sell to NPCs or players
```

---

## 💡 Tips & Tricks

### Money Making

💰 **Daily Login**: Never miss `/daily` - 30-day streak = 300€/day
💰 **Plot Rental**: Create apartments for recurring revenue
💰 **Quality Matters**: Golden pots + quality boosters = legendary products
💰 **Market Timing**: Check `/market prices` for best selling times

### Efficiency

⚡ **Grow Lights**: Premium lights = +50% growth speed
⚡ **Bigger is Better**: Large processing blocks = faster batch processing
⚡ **Automate**: Link warehouses to NPC shops for automatic selling
⚡ **Plan Routes**: Set NPC leisure spots strategically for RP

### Safety

🔒 **Plots**: Always `/plot trust` carefully
🔒 **Backups**: Admins - use `/health` to check system status
🔒 **Police**: Avoid wanted stars or use `/bail` quickly
🔒 **Meth**: Reduktionskessel can EXPLODE - be careful!

---

## 🆘 Troubleshooting

### "I can't build in this plot"

**Solution:**
1. Check owner: `/plot info`
2. Ask owner for trust: `Owner runs /plot trust YourName`
3. Or buy the plot: `/plot buy`

### "My plants aren't growing"

**Solutions:**
- Water them (watering can)
- Add fertilizer
- Use grow lights (requires power)
- Check pot type (Golden = best)
- Wait - growth takes 10-20 minutes

### "Transaction failed - insufficient funds"

**Solutions:**
- Check balance: `/money`
- Get daily reward: `/daily`
- Apply for loan: `/loan apply SMALL`
- Sell products to NPCs
- Ask friend to `/pay` you

### "NPC won't talk to me"

**Solutions:**
- Check NPC schedule (might be sleeping)
- Approach closer
- Right-click NPC
- Check if NPC has inventory/wallet

---

## 📖 Next Steps

**Learn More:**
- [Complete Command Reference](Commands.md) - All 161+ commands
- [Items Guide](Items.md) - All 141 items
- [Blocks Guide](Blocks.md) - All 77+ blocks
- [Production Systems](Production-Systems.md) - 8 production chains
- [API Documentation](API.md) - For developers

**Systems Deep-Dive:**
- [Plot System](features/Plot-System.md) - Advanced plot management
- [Economy System](features/Economy-System.md) - Banking, loans, taxes
- [NPC System](features/NPC-System.md) - AI, schedules, shops
- [Police & Crime](features/Police-Crime-System.md) - Wanted levels, prison

**Production Guides:**
- [Tobacco Production](production/Tobacco-System.md) - Most profitable
- [Cannabis Production](production/Cannabis-System.md) - Hash & oil
- [Vehicle System](features/Vehicle-System.md) - Cars & customization

---

## 🎮 Join the Community

- 💬 [Discord](https://discord.gg/schedulemc) - Get help, share builds
- 🐛 [Report Bugs](https://github.com/Minecraft425HD/ScheduleMC/issues) - Help improve the mod
- 🌐 [GitHub](https://github.com/Minecraft425HD/ScheduleMC) - Source code

---

[⬆ Back to Wiki Home](Home.md)
