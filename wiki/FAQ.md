# Frequently Asked Questions (FAQ)

Common questions and answers about ScheduleMC.

---

## 📦 Installation & Setup

### Q: What Minecraft version does ScheduleMC support?
**A:** ScheduleMC requires **Minecraft 1.20.1** with **Forge 47.4.0 or higher** and **Java 17**.

### Q: Can I use ScheduleMC in singleplayer?
**A:** Yes! ScheduleMC works in both singleplayer and multiplayer. However, some features (like trading with other players) are more fun on a server.

### Q: Does ScheduleMC work with Fabric?
**A:** No, ScheduleMC is a **Forge-only** mod. It uses Forge-specific APIs and cannot run on Fabric.

### Q: What mods are compatible with ScheduleMC?
**A:** ScheduleMC is compatible with most mods. Optional integration with:
- JEI (Just Enough Items) - Recipe viewing
- Jade - Block tooltips
- The One Probe - Advanced info

### Q: How much RAM does the server need?
**A:** Minimum 4 GB, recommended 8 GB for smooth performance with multiple players.

---

## 💰 Economy & Money

### Q: How do I get money?
**A: Multiple ways:**
1. **Daily reward:** `/daily` (50€ + streak bonus)
2. **Sell products:** To NPCs or players
3. **Rent plots:** Create apartments
4. **Loans:** `/loan apply SMALL/MEDIUM/LARGE`

### Q: What's the maximum daily reward streak?
**A:** 30 days maximum = 300€/day (50€ base + 250€ bonus)

### Q: How do loans work?
**A: Three loan tiers:**
- **SMALL:** 5,000€ at 10% interest (14 days)
- **MEDIUM:** 25,000€ at 15% interest (28 days)
- **LARGE:** 100,000€ at 20% interest (56 days)

Repay early with `/loan repay` or get auto-charged on due date.

### Q: Can I lose money?
**A: Yes, money can be lost through:**
- Plot purchases
- Loan interest
- Taxes (if configured)
- Death (hospital fee)
- Being arrested with illegal cash (raid penalty)
- Failed transactions

### Q: What's the state account?
**A:** Government treasury used for:
- Warehouse delivery payments
- NPC salaries (if configured)
- Hospital fees
- Public services

Admins manage with `/state`.

---

## 🏘️ Plots & Land

### Q: How do I create a plot?
**A:**
```bash
/plot wand          # Get selection tool
# Left-click corner 1, right-click corner 2
/plot create residential "My Home" 50000
```

### Q: What's the difference between plot types?
**A:**
- **Residential:** Homes, apartments
- **Commercial:** Businesses, offices
- **Shop:** NPC shops with inventory systems
- **Public:** Parks, roads (admin only)
- **Government:** Town halls, prisons (admin only)

### Q: How do apartments work?
**A:**
1. Create apartment in YOUR plot: `/plot apartment create "Apt 1A" 500`
2. Other players rent with: `/plot apartment rent <id> 30` (30 days)
3. Security deposit taken automatically
4. Evict with: `/plot apartment evict <id>`

### Q: Can I transfer a plot to another player?
**A:** Yes! `/plot transfer <player>` transfers ownership for free.

### Q: What happens if I abandon a plot?
**A:** `/plot abandon` removes the plot and refunds **50% of creation cost**.

### Q: How do I protect my plot?
**A:** Plots automatically protect:
- Block placement/breaking
- Container access (chests, furnaces, etc.)
- Entity interaction

Only owner and trusted players can build.

---

## 🤖 NPCs

### Q: How do I spawn an NPC?
**A (Admin):**
```bash
/npc spawn merchant Shop_Owner_Hans
/npc Hans schedule workstart 0700
/npc Hans schedule workend 1800
```

### Q: What NPC types exist?
**A:**
- **Resident:** Regular citizens (roleplay)
- **Merchant:** Shop owners (buy/sell items)
- **Police:** Law enforcement (chase criminals)

### Q: Why isn't my NPC moving?
**A: Common causes:**
- No schedule set (work/home times)
- No home/work location set
- NPC is sleeping (23:00-07:00)
- Movement disabled (`/npc <name> movement false`)

### Q: How do NPC shops work?
**A:**
1. Create shop plot
2. Assign NPC: `/npc <name> setshop <shopId>`
3. Give inventory: `/npc <name> inventory give 0 diamond`
4. Link warehouse (optional): `/warehouse setshop <shopId>`
5. NPCs sell from inventory + warehouse

### Q: Can NPCs run out of money?
**A:** Yes! NPCs have wallets. If empty, they can't buy from players.
Set wallet: `/npc <name> wallet set 10000`

### Q: What are NPC personalities?
**A:**
- **Friendly:** Better prices, easier to negotiate
- **Neutral:** Standard interactions
- **Hostile:** Higher prices, aggressive
- **Professional:** Business-focused

---

## 🚔 Police & Crime

### Q: How do I get a wanted level?
**A: Criminal activities:**
- Attacking NPCs or players
- Stealing from shops
- Producing/selling drugs (if caught)
- Trespassing on government plots

### Q: How do I lose wanted stars?
**A: Three ways:**
1. **Auto-decay:** -1 star per day
2. **Pay bail:** `/bail` at hospital (instant)
3. **Hide:** Stay in buildings until police give up

### Q: What happens when I'm arrested?
**A:**
- Teleported to prison cell
- Option to pay bail or serve time
- Illegal cash confiscated (raid penalty)
- Wanted level cleared

### Q: How do I create a prison?
**A (Admin):**
```bash
/plot create government "Prison"
/prison create prison_main
/prison addcell 1 100,10,100 105,15,105 1
```

Security levels 1-5 affect cell conditions.

### Q: Can I escape prison?
**A:** No direct escape mechanic. Options:
- Pay bail: `/bail`
- Wait for time to expire: Check with `/jailtime`
- Admin release: `/prison release <player>`

---

## 🌿 Production & Crafting

### Q: Which production system should I start with?
**A: Recommended for beginners:**
1. **Mushroom:** Easiest (4 steps, 25-35 min)
2. **Tobacco:** Learn quality system
3. **Cannabis:** Multiple products

### Q: How do I improve product quality?
**A:**
- Use **Golden Pots** (+50% growth, +1 quality tier)
- Apply **Quality Boosters**
- Use **Premium Grow Lights** (+50% growth)
- Longer fermentation/processing times
- Better equipment (bigger machines = better quality)

### Q: Why aren't my plants growing?
**A: Troubleshooting:**
- ✓ Water them (watering can)
- ✓ Apply fertilizer
- ✓ Use grow lights (requires power from utility system)
- ✓ Check pot type (Golden is best)
- ✓ Wait - growth takes 10-20 minutes
- ✓ Ensure plant is in valid growth stage

### Q: What's the most profitable production?
**A: Profitability ranking:**
1. **Poppy/Heroin:** 500-1,000€/unit (60 min)
2. **LSD:** 1,000-2,000€/sheet (70 min)
3. **Meth:** 600-900€/unit (40 min) ⚠️ Explosion risk
4. **Cocaine:** 200-350€/unit (50 min)

### Q: Can the Meth Reduktionskessel really explode?
**A:** Yes! If interrupted or mishandled. Safety tips:
- Don't break mid-process
- Ensure stable power
- Keep fire extinguishers nearby

### Q: How do I use the MDMA Pill Press minigame?
**A:** Timing-based! Click when indicator hits sweet spot:
- **Perfect:** 100% yield, max quality
- **Good:** 80% yield
- **Fair:** 50% yield
- **Miss:** Materials lost

Practice makes perfect!

---

## 🚗 Vehicles

### Q: How do I get a vehicle?
**A: Two ways:**
1. **Spawn Item:** Use pre-built vehicle items (limousine, van, truck, SUV, sports car)
2. **Craft Parts:** Build custom vehicle from parts (engines, chassis, tires, etc.)

### Q: How do I refuel?
**A:**
- Drive to fuel station
- Right-click fuel station
- Select fuel type (diesel/gasoline)
- Pay from wallet

### Q: Can vehicles be damaged?
**A:** Yes! Vehicles have durability. Repair with:
- Maintenance Kit (10 uses)
- Garage block

### Q: How do I customize my vehicle?
**A: Modular parts system:**
- Engines (Normal, Performance, Industrial)
- Tires (6 types)
- Fenders (Basic, Chrome, Sport)
- Fuel tanks (15L, 30L, 50L)
- Modules (Cargo, Fluid, License Plate)

Change parts at Garage block.

---

## 📱 Smartphone & Apps

### Q: How do I open the smartphone?
**A:** Press **P** (default key, configurable).

### Q: What are the smartphone apps?
**A: 11 apps:**
1. MAP - Plot locations
2. DEALER - Find tobacco dealers
3. PRODUCTS - Shop catalog
4. ORDER - Order management
5. CONTACTS - Player & NPC contacts
6. MESSAGES - Inbox & chat
7. PLOT - Plot management
8. SETTINGS - Config
9. BANK - Banking
10. CRIME STATS - Wanted level
11. CHAT - Direct messaging

### Q: Can I be attacked while using the smartphone?
**A:** No! **PvP Protection:**
- You're immune to damage
- Attackers get +1 wanted star ⭐
- Fair play enforcement

---

## 🏪 Warehouse & Trading

### Q: How does the warehouse work?
**A:**
- **32 slots** × 1,024 items each
- **Auto-delivery** every 3 days
- Link to shop plots
- NPCs sell from warehouse inventory

Admin commands: `/warehouse add/remove/setshop`

### Q: How do I link warehouse to NPC shop?
**A:**
```bash
/warehouse setshop <shopId>      # Link to shop
/npc <name> warehouse set         # Link to NPC (look at warehouse)
```

### Q: Can players access warehouses?
**A:** No, warehouses are admin-only. Players interact via NPC shops.

---

## ⚙️ Configuration & Admin

### Q: Where are the config files?
**A:** `config/schedulemc-common.toml` and `config/schedulemc-client.toml`

### Q: How do I change starting balance?
**A:** Edit `config/schedulemc-common.toml`:
```toml
[economy]
    startingBalance = 1000
```

### Q: How do I check system health?
**A (Admin):** `/health` shows:
- Economy system status
- Plot system status
- Backup availability
- Manager health checks

### Q: Can I reset the economy?
**A:** Yes, delete save files:
- `world/schedulemc/economy.json`
- `world/schedulemc/plots.json`
- etc.

**WARNING:** This deletes all player data!

---

## 🐛 Troubleshooting

### Q: "Transaction failed - insufficient funds"
**A: Solutions:**
1. Check balance: `/money`
2. Get daily reward: `/daily`
3. Apply for loan: `/loan apply SMALL`
4. Sell products to NPCs

### Q: "You cannot build here"
**A: Common causes:**
- Not plot owner
- Not trusted on plot
- Public/government plot (admin only)

**Solution:** `/plot info` to check owner, ask for `/plot trust`

### Q: NPCs won't buy my products
**A: Troubleshooting:**
- Check NPC wallet: `/npc <name> wallet`
- Check if NPC is merchant type
- Check if shop is configured
- Verify NPC schedule (must be at work)

### Q: My plots aren't saving
**A: Admin troubleshooting:**
1. Check disk space
2. Verify write permissions
3. Check logs for errors
4. Use `/health plot` for diagnostics
5. Check backup system: `/health backups`

### Q: Mod crashes on startup
**A: Common fixes:**
1. Verify Forge 47.4.0+
2. Check Java 17
3. Remove conflicting mods
4. Check logs: `.minecraft/logs/latest.log`
5. Delete config and let it regenerate

---

## 📊 Statistics & Progress

### Q: How do I check my statistics?
**A: Various commands:**
- `/money history` - Transaction history
- `/daily streak` - Daily reward streak
- `/plot list` - Your plots

### Q: Is there an achievement system?
**A:** Currently in development. Tutorial system tracks:
- Tutorial completion
- First plot
- First production
- etc.

---

## 🔄 Updates & Support

### Q: How do I update ScheduleMC?
**A:**
1. Download latest version from GitHub
2. Replace old JAR in `mods/` folder
3. Restart server
4. Mod checks for updates automatically

### Q: Where can I report bugs?
**A:** GitHub Issues: [https://github.com/Minecraft425HD/ScheduleMC/issues](https://github.com/Minecraft425HD/ScheduleMC/issues)

### Q: Is there a Discord?
**A:** Coming soon! Check README for latest links.

### Q: Can I contribute to development?
**A:** Yes! Fork on GitHub, submit pull requests.
See [API Documentation](API.md) for developers.

---

## 💡 Tips & Best Practices

### Q: What's the best money-making strategy?
**A: Recommended path:**
1. **Week 1:** Daily rewards + mushroom production (low risk)
2. **Week 2:** Cannabis production + savings accounts
3. **Week 3:** Expand to poppy/LSD (high profit)
4. **Week 4+:** Multiple production chains + rental income

### Q: How do I avoid getting arrested?
**A: Stay safe:**
- Produce in private plots
- Don't carry illegal cash
- Use warehouses for storage
- Pay bail quickly if caught
- Hide in buildings during chases

### Q: What's the most efficient production setup?
**A: Pro setup:**
- **Golden pots** for all plants
- **Premium grow lights**
- **Big processing machines** (faster batches)
- **Warehouse auto-sell** to NPC shops
- **Multiple parallel chains**

---

[⬆ Back to Wiki Home](Home.md)

**Still have questions?**
- Check [Getting Started Guide](Getting-Started.md)
- Read detailed [Command Reference](Commands.md)
- Join [Discord](https://discord.gg/schedulemc) (coming soon)
- Ask in [GitHub Discussions](https://github.com/Minecraft425HD/ScheduleMC/discussions)
