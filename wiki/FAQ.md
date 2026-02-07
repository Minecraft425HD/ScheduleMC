# Frequently Asked Questions (FAQ)

Comprehensive answers to common questions about ScheduleMC, the all-in-one roleplay and economy mod for Minecraft Forge 1.20.1.

**Version:** 3.6.0-beta | **Last Updated:** 2026-02-07

---

## Table of Contents

- [General](#general)
- [Installation](#installation)
- [Economy](#economy)
- [Plots](#plots)
- [NPCs](#npcs)
- [Production](#production)
- [Vehicles](#vehicles)
- [Police and Crime](#police-and-crime)
- [Smartphone](#smartphone)
- [Technical](#technical)
- [For Developers](#for-developers)

---

## General

### Q: What is ScheduleMC?

**A:** ScheduleMC is a professional-grade Minecraft Forge mod that transforms your server into a complete roleplay and economy ecosystem. It provides a full banking system with loans and savings, 14 production chains (8 illegal, 6 legal), schedule-based NPC AI, a GTA-inspired police and crime system with a 5-star wanted level, drivable vehicles with fuel and parts, plot management with apartments and rentals, an in-game smartphone with 11 apps, and much more. With over 93,000 lines of Java code, 354 items, 152 blocks, and 161+ commands, it is one of the most comprehensive Minecraft mods available.

---

### Q: What Minecraft, Forge, and Java versions does ScheduleMC require?

**A:** ScheduleMC requires the following:

| Requirement | Version |
|-------------|---------|
| **Minecraft** | 1.20.1 |
| **Minecraft Forge** | 47.4.0 or higher |
| **Java** | 17 |

ScheduleMC targets only Minecraft 1.20.1. It will not work on earlier versions, later versions, or on alternative mod loaders such as Fabric or NeoForge.

---

### Q: Where can I download ScheduleMC?

**A:** You can download the latest release from the [GitHub Releases page](https://github.com/Minecraft425HD/ScheduleMC/releases). You can also build the mod from source if you prefer (see the [Installation section](#q-how-do-i-build-schedulemc-from-source) below). Distribution on CurseForge and Modrinth is planned for future releases.

---

### Q: Is ScheduleMC free?

**A:** ScheduleMC is distributed under the **All Rights Reserved** license. The `gradle.properties` file specifies the license as `All Rights Reserved`, meaning all rights are retained by the author, Luckas R. Schneider (Minecraft425HD). While the source code is available on GitHub, redistribution, modification, and commercial use may be restricted. Refer to the project's license terms for specific permissions.

**Note:** The repository's LICENSE file contains the text of the GNU GPL v3, which may indicate a transition or dual-licensing arrangement. When in doubt, contact the developer for clarification.

---

### Q: Does ScheduleMC work with other mods?

**A:** Yes. ScheduleMC is designed to coexist with most Minecraft Forge mods. It uses its own namespaced items, blocks, and commands under the `schedulemc` mod ID, so it generally does not conflict with other mods. The mod uses Forge's standard event bus, deferred registration, and networking systems, making it compatible with the broader Forge ecosystem.

That said, mods that heavily modify core game mechanics (such as world generation, entity rendering, or GUI systems) could potentially cause compatibility issues. Always test with your specific mod configuration.

---

### Q: Are there optional dependencies?

**A:** Yes. ScheduleMC supports optional integration with the following mods for enhanced features:

| Mod | Version | Benefit |
|-----|---------|---------|
| **JEI** (Just Enough Items) | 15.2.0.27 for 1.20.1 | View crafting recipes and item information |
| **Jade** | 11.8.0 for 1.20.1 | Display block information tooltips when looking at blocks |
| **The One Probe** | 1.20.1-10.0.2-forge | Advanced block and entity information overlay |

These mods are entirely optional. ScheduleMC functions fully without them. They are declared as `compileOnly` dependencies, meaning they are not bundled with or required by the mod.

---

## Installation

### Q: How do I install Minecraft Forge 47.4.0+?

**A:** Follow these steps to install Forge:

1. **Ensure Java 17 is installed.** Download it from [Adoptium](https://adoptium.net/) if needed.

2. **Download the Forge installer** for Minecraft 1.20.1 from [files.minecraftforge.net](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html). Select version 47.4.0 or newer.

3. **Run the installer:**
   - **Windows/Mac:** Double-click the downloaded `.jar` file.
   - **Command line:**
     ```bash
     java -jar forge-1.20.1-47.4.0-installer.jar
     ```

4. **Select installation type:**
   - Choose **"Install client"** for playing on your own machine.
   - Choose **"Install server"** for a dedicated server.

5. **Launch Minecraft** and select the **Forge 1.20.1** profile in the launcher. Run it once to generate the `mods/` folder.

---

### Q: How do I install ScheduleMC?

**A:** Once Forge is installed:

1. Download the latest ScheduleMC `.jar` file from the [GitHub Releases page](https://github.com/Minecraft425HD/ScheduleMC/releases).

2. Place the `.jar` file in your `mods/` folder:

   **For players (client):**
   ```
   .minecraft/
   └── mods/
       └── ScheduleMC-3.6.0-beta.jar
   ```

   **For servers:**
   ```
   minecraft_server/
   └── mods/
       └── ScheduleMC-3.6.0-beta.jar
   ```

3. Launch Minecraft with the Forge profile (client) or start the server. ScheduleMC should appear in the mod list.

4. **(Optional)** Install JEI, Jade, or The One Probe for enhanced features.

---

### Q: What are the minimum RAM requirements?

**A:** The recommended memory allocation is:

| Configuration | RAM | Notes |
|---------------|-----|-------|
| **Minimum** | 4 GB | Basic functionality with a small number of players |
| **Recommended** | 8 GB | Smooth performance with multiple players and all systems active |

ScheduleMC loads many systems in parallel at server startup (plots, economy, NPCs, vehicles, warehouses, and more), so adequate memory is important. If you experience lag or crashes, try increasing the allocated memory in your Minecraft launcher or server startup script (e.g., `-Xmx8G`).

---

### Q: How do I build ScheduleMC from source?

**A:** To build from source:

```bash
# 1. Clone the repository
git clone https://github.com/Minecraft425HD/ScheduleMC.git
cd ScheduleMC

# 2. Build the mod (produces the .jar file)
./gradlew build

# 3. The built .jar is located at:
#    build/libs/schedulemc-3.6.0-beta.jar
```

**Additional development commands:**

```bash
# Run the Minecraft client in development mode
./gradlew runClient

# Run a dedicated server in development mode
./gradlew runServer

# Run the unit test suite
./gradlew test

# Generate a JaCoCo code coverage report
./gradlew jacocoTestReport
```

**Requirements for building:** Java 17 JDK, internet connection (for Gradle dependencies), and at least 3 GB of RAM for the Gradle daemon (`org.gradle.jvmargs=-Xmx3G` is set in `gradle.properties`).

---

## Economy

### Q: How much starting money does a new player receive?

**A:** Every player who joins a ScheduleMC server for the first time automatically receives a bank account with a **1,000 euro** starting balance. This is handled by the `PlayerJoinHandler` during the login event. The starting balance is configurable by server administrators in `config/schedulemc-common.toml` under the `[economy]` section.

Additionally, players receive an automatic daily reward on their first login.

---

### Q: How do daily rewards work?

**A:** Daily rewards are claimed automatically when a player logs in. The system works as follows:

- **Base reward:** 50 euro per day
- **Streak bonus:** +10 euro for each consecutive day you log in
- **Maximum streak:** 30 days (meaning a maximum bonus of 300 euro per day at a 30-day streak: 50 base + 250 bonus)
- **Streak reset:** If you miss a day without logging in, your streak resets to zero

Daily rewards are managed by the `DailyRewardManager` and are processed via the `DailyRewardManager.claimOnLogin()` method that fires automatically in the `onPlayerLoggedIn` event handler. There is no manual `/daily` command needed -- the reward is granted on login.

---

### Q: How do loans work?

**A:** ScheduleMC has a three-tier loan system. Each player can only have one active loan at a time and must have a minimum account balance of 1,000 euro to apply.

| Tier | Amount | Interest Rate | Duration | Total Repayment |
|------|--------|---------------|----------|-----------------|
| **SMALL** | 5,000 euro | 10% | 14 days (2 weeks) | 5,500 euro |
| **MEDIUM** | 25,000 euro | 15% | 28 days (4 weeks) | 28,750 euro |
| **LARGE** | 100,000 euro | 20% | 56 days (8 weeks) | 120,000 euro |

- The loan amount is deposited into your bank account immediately upon approval.
- Repayment is divided into daily installments automatically deducted from your account.
- You can repay early with `/loan repay`, which pays off the remaining balance in full.
- If you fail to make a daily payment (insufficient funds), the debt accumulates.

Additionally, ScheduleMC features an advanced **Credit Score System** (`CreditScoreManager`) and a **Credit Loan Manager** (`CreditLoanManager`) that provide NPC-based lending with credit score tracking, allowing for more dynamic loan terms based on player history.

---

### Q: How do savings accounts work?

**A:** Savings accounts let players earn interest on deposited funds:

- **Interest rate:** 5% weekly (paid out every 7 in-game days)
- **Lock period:** 4 weeks (funds cannot be withdrawn during this period)
- **Minimum deposit:** 1,000 euro (configurable by administrators)
- **Maximum per player:** Configurable via `SAVINGS_MAX_PER_PLAYER` in the server config

**How to use savings:**

```
/savings create <amount>    - Open a savings account with an initial deposit
/savings info               - View your savings account details
/savings withdraw           - Withdraw after the lock period expires
```

When you create a savings account, the deposit amount is withdrawn from your main bank account. After the 4-week lock period, you can withdraw your principal plus accumulated interest.

---

### Q: How do taxes work?

**A:** ScheduleMC implements a progressive tax system managed by the `TaxManager`:

**Income Tax (Progressive):**

| Bracket | Rate |
|---------|------|
| Up to 10,000 euro | 0% (tax-free) |
| 10,001 - 50,000 euro | 10% |
| 50,001 - 100,000 euro | 15% |
| Above 100,000 euro | 20% |

**Property Tax:**
- 100 euro per chunk per month for plot ownership

**Tax Period:**
- Taxes are assessed every 7 in-game days (one Minecraft week)

**Sales Tax:**
- Applied to transactions (configurable rate)

All collected taxes are deposited into the State Account (government treasury). If a player cannot pay their taxes, a tax debt accumulates and is tracked by the system.

---

### Q: What is the State Account?

**A:** The State Account is the government treasury that serves as the central financial account for server-wide operations. It is managed by the `StateAccount` class and persists across server restarts.

**The State Account receives funds from:**
- Tax collection (income, property, and sales taxes)
- Hospital fees (death penalties)
- Transaction fees

**The State Account pays for:**
- Warehouse delivery costs
- NPC salaries (if configured)
- Public services and infrastructure

**Admin commands:**
```
/state balance     - View the State Account balance
/state deposit     - Add funds to the State Account
/state withdraw    - Remove funds from the State Account
```

---

## Plots

### Q: How do I create a plot?

**A:** Creating a plot involves three steps:

1. **Get the selection tool:**
   ```
   /plot wand
   ```

2. **Select the area:**
   - Left-click the ground at one corner of your desired plot to set Position 1.
   - Right-click the ground at the opposite corner to set Position 2.
   - This defines a rectangular region.

3. **Create the plot:**
   ```
   /plot create <type> "<name>" <price>
   ```
   Example:
   ```
   /plot create residential "My Home" 50000
   ```

The plot is now protected. Only you and trusted players can build, break blocks, or access containers within its boundaries.

---

### Q: What are the plot types?

**A:** ScheduleMC supports 5 plot types:

| Type | Description | Who Can Create |
|------|-------------|----------------|
| **Residential** | Homes, apartments, and living spaces | All players |
| **Commercial** | Businesses, offices, and shops | All players |
| **Shop** | NPC shops with inventory and warehouse integration | All players |
| **Public** | Parks, roads, spawn areas, and community spaces | Admins only |
| **Government** | Town halls, prisons, hospitals, and official buildings | Admins only |

Each type has specific permissions and behaviors. For example, shop plots can be linked to NPC merchants and warehouses, while government plots are used for system features like prisons.

---

### Q: How does the rating system work?

**A:** Players can rate plots on a 5-star scale. The rating system includes:

- **5-star ratings** that other players can leave on your plot
- **Leaderboards** that show the top-rated plots on the server
- **Plot Info Block** that can be placed to display plot information and ratings

Ratings help build reputation and serve as a way for players to showcase their builds. Use `/plot info` while standing in a plot to see its current rating and details.

---

### Q: How do I rent or buy apartments?

**A:** Plot owners can subdivide their plots into apartments for other players to rent:

**Creating an apartment (plot owner):**
```
/plot apartment create "<name>" <monthly_rent>
```
Example:
```
/plot apartment create "Apartment 1A" 500
```

**Renting an apartment (tenant):**
```
/plot apartment rent <id> <days>
```
Example:
```
/plot apartment rent apt_1a 30
```
A security deposit is taken automatically upon renting.

**Managing apartments:**
```
/plot apartment evict <id>     - Evict a tenant (plot owner)
```

Tenants gain build permissions within the apartment boundaries. If rent expires, the system handles auto-eviction.

---

### Q: What happens when I abandon a plot?

**A:** When you abandon a plot using `/plot abandon`, the following occurs:

- The plot is removed from the system.
- All protections on the area are lifted.
- You receive a **50% refund** of the original creation cost.
- Any apartments within the plot are also removed.
- Tenants lose access immediately.

This is permanent and cannot be undone. Consider transferring the plot to another player with `/plot transfer <player>` instead if you want to preserve the plot.

---

## NPCs

### Q: How do I spawn NPCs?

**A:** NPCs are spawned using admin commands. The base command is:

```
/npc spawn <type> <name>
```

Examples:
```
/npc spawn resident Town_Citizen
/npc spawn merchant Shop_Owner_Hans
/npc spawn police Officer_Mueller
```

After spawning, you typically configure the NPC with a schedule and locations:

```
/npc Hans schedule workstart 0700
/npc Hans schedule workend 1800
/npc Hans schedule home 2300
```

You can also set the NPC's home, work, and leisure locations using the NPC location tools (4 tool items in the NPC creative tab).

---

### Q: What NPC types exist?

**A:** There are 3 core NPC types:

| Type | Description | Key Behaviors |
|------|-------------|---------------|
| **Resident** | Regular citizens who follow daily schedules | Go to work, take lunch breaks, visit leisure spots, sleep at home |
| **Merchant** | Shop owners who buy and sell items | Manage inventory, trade with players, sell from linked warehouses |
| **Police** | Law enforcement officers | Chase criminals, arrest wanted players, call for backup, block doors during pursuit |

All NPC types share common features including custom player skins, a wallet system, personality traits (Friendly, Neutral, Hostile, Professional), a relationship system with players, and pathfinding AI with 139 behavior goals.

---

### Q: How do NPC schedules work?

**A:** NPC schedules use the **HHMM format** (24-hour time, four digits). Each NPC has configurable time points that control their daily routine.

**Schedule configuration:**
```
/npc <name> schedule workstart 0700    - NPC goes to work at 07:00
/npc <name> schedule workend 1800      - NPC leaves work at 18:00
/npc <name> schedule home 2300         - NPC goes home to sleep at 23:00
```

**Default daily cycle:**
```
07:00 - Work Start     (NPC travels to work location)
12:00 - Lunch Break    (NPC visits a leisure location)
18:00 - Work End       (NPC returns home or visits leisure spots)
23:00 - Sleep          (NPC stays at home until morning)
```

NPCs navigate between their assigned home, work, and leisure locations (up to 10 leisure locations per NPC) using smart pathfinding AI. While sleeping (23:00-07:00), NPCs remain stationary at their home.

---

### Q: How do NPC shops work?

**A:** Setting up an NPC shop involves linking a merchant NPC to a shop plot and optionally a warehouse:

1. **Create a shop plot:**
   ```
   /plot wand
   /plot create shop "General Store"
   ```

2. **Assign the NPC to the shop:**
   ```
   /npc Hans setshop generalstore
   ```

3. **Give the NPC inventory:**
   ```
   /npc Hans inventory give 0 minecraft:diamond
   /npc Hans inventory give 1 minecraft:iron_ingot
   ```

4. **Set the NPC's wallet (buying power):**
   ```
   /npc Hans wallet set 10000
   ```

5. **(Optional) Link a warehouse for auto-restocking:**
   ```
   /warehouse setshop generalstore
   /npc Hans warehouse set
   ```

Players interact with merchant NPCs by right-clicking to open a trade GUI. Prices are influenced by the NPC's personality, the player's relationship with the NPC, and dynamic market conditions. When linked to a warehouse, the NPC's stock is automatically replenished every 3 days.

---

## Production

### Q: How many production types are there?

**A:** ScheduleMC features **14 total production systems** divided into two categories:

**Illegal Productions (8):**

| # | Type | Strains | Steps | Key Feature |
|---|------|---------|-------|-------------|
| 1 | Tobacco | 4 (Virginia, Burley, Oriental, Havana) | 6 | Most complex, full quality chain |
| 2 | Cannabis | 4 (Indica, Sativa, Hybrid, Autoflower) | 8 | Multiple end products (buds, hash, oil) |
| 3 | Coca/Cocaine | 2 (Bolivianisch, Kolumbianisch) | 5 | Crack cooking, glowing refineries |
| 4 | Poppy/Opium | 3 (Afghanisch, Tuerkisch, Indisch) | 6 | Opium to morphine to heroin chain |
| 5 | Methamphetamine | - | 4 | Explosion risk at Reduktionskessel |
| 6 | LSD | - | 6 | Precision laboratory synthesis |
| 7 | MDMA/Ecstasy | - | 4 | Timing minigame at pill press |
| 8 | Psilocybin Mushrooms | 3 (Cubensis, Azurescens, Mexicana) | 4 | Easiest for beginners |

**Legal Productions (6):**

| # | Type | Description |
|---|------|-------------|
| 9 | Coffee | Roasting and brewing system |
| 10 | Wine | Grape growing and fermentation |
| 11 | Cheese | Milk processing and aging |
| 12 | Honey | Beekeeping and extraction |
| 13 | Chocolate | Cacao processing and molding |
| 14 | Beer | Brewing with malting, mashing, fermentation, and conditioning |

All production systems share a unified quality framework (Poor, Good, Very Good, Legendary) and use the universal packaging system via the `packaged_drug` item with NBT data.

---

### Q: How do I start production?

**A:** The basic process is the same across all plant-based production systems:

1. **Obtain seeds** from NPC merchants or admin commands.

2. **Plant seeds in a pot** by right-clicking a pot block with seeds in hand. Pots come in 4 tiers:
   - **Terracotta Pot** - Basic, slowest growth
   - **Ceramic Pot** - Slightly better
   - **Iron Pot** - Good growth speed and quality
   - **Golden Pot** - Best: +50% growth speed, +1 quality tier

3. **Water the plant** using a watering can.

4. **Accelerate growth** with fertilizer, growth boosters, quality boosters, and grow lights (3 tiers: Basic, Advanced, Premium with +50% growth speed).

5. **Harvest** when the plant reaches maturity (typically 8 growth stages, 10-20 minutes).

6. **Process** through the production chain specific to the product type (drying, fermenting, refining, etc.).

7. **Package** at a packaging table (Small, Medium, Large, or XL sizes).

8. **Sell** to NPC merchants or other players. Prices depend on quality and dynamic market conditions.

---

### Q: What affects product quality?

**A:** Several factors influence the quality of your products:

- **Pot type:** Golden pots provide the highest quality bonus (+1 tier)
- **Quality Booster items:** Apply directly to plants for quality improvement
- **Grow lights:** Premium grow lights improve growth conditions
- **Processing equipment size:** Larger machines (Big vs. Small) produce better quality output
- **Fermentation/aging duration:** Longer processing times yield higher quality
- **Strain/variety selection:** Some strains naturally produce higher-quality output

**Quality levels (from lowest to highest):**
1. Poor
2. Good
3. Very Good
4. Legendary

Higher quality products sell for significantly more at NPC shops and on the dynamic market.

---

### Q: Can meth really explode?

**A:** Yes. The **Reduktionskessel** (reduction kettle) used in methamphetamine production has an actual explosion mechanic. If the process is interrupted or mishandled (such as breaking the block mid-process), the Reduktionskessel can detonate, causing a block-destroying explosion in the surrounding area.

**Safety tips:**
- Do not break the Reduktionskessel while it is processing
- Ensure a stable power supply to avoid interruptions
- Build your meth lab in a reinforced area away from valuable builds
- Keep fire extinguishers and backup supplies nearby

This explosion risk makes meth production the most dangerous (but also one of the most profitable) production chains.

---

### Q: What is the universal packaging system?

**A:** All production chains use a unified packaging system. Once a product is processed and ready for sale, it is packaged at a Packaging Table into the universal `packaged_drug` item.

This single item type uses **NBT data** to store:
- The product type (tobacco, cannabis, cocaine, etc.)
- The product quality (Poor, Good, Very Good, Legendary)
- The quantity
- The strain/variety

This system allows all drugs and products to be handled uniformly for trading, NPC shop inventory, warehouse storage, and market pricing via the Unified Dynamic Pricing System (UDPS).

Packaging tables come in 4 sizes: Small, Medium, Large, and XL (the larger tables are multi-block 2x2 structures).

---

## Vehicles

### Q: How do I get a vehicle?

**A:** There are two ways to obtain a vehicle:

1. **Use a pre-built vehicle spawn item:** ScheduleMC includes spawn items for each of the 5 vehicle chassis types. Use the Vehicle Spawn Tool (left-click a surface) to place a vehicle.

2. **Assemble from parts:** Collect individual vehicle components (chassis, engine, tires, fenders, fuel tank) and combine them at a Garage block.

Vehicles can also be obtained through admin commands or NPC shops if the server is configured accordingly.

---

### Q: How do I refuel a vehicle?

**A:** Vehicles consume fuel as you drive and need to be refueled:

1. Drive to a **Fuel Station** block (multi-block structure).
2. Right-click the Fuel Station with your vehicle nearby.
3. Select the fuel type (diesel or gasoline) from the interface.
4. Pay from your wallet/bank account for the fuel.

You can also carry fuel using **Diesel Cans** (empty or full variants). Fuel tanks come in 3 sizes: 15L, 30L, and 50L, which determine how far you can drive between refueling stops.

---

### Q: What vehicle types are available?

**A:** ScheduleMC features 5 vehicle chassis types:

| Vehicle Type | Chassis | Description | Best For |
|--------------|---------|-------------|----------|
| **Limousine** | LIMOUSINE_CHASSIS | Standard sedan | General transport |
| **Van** | VAN_CHASSIS | Cargo van | Hauling goods |
| **Truck** | TRUCK_CHASSIS | Heavy-duty truck | Large cargo transport |
| **SUV** | OFFROAD_CHASSIS | Off-road capable vehicle | Rough terrain |
| **Sports Car** | LUXUS_CHASSIS | High-performance vehicle | Speed |

---

### Q: How do vehicle parts work?

**A:** Vehicles use a modular parts system. You can customize and upgrade your vehicle with different components:

**Engines (3 types):**
- Normal Motor - Standard performance
- Performance Motor - Higher speed and acceleration
- Industrial Motor - Built for heavy loads

**Tires (6 types):**
- Standard, Sport, Premium (for cars)
- Offroad, Allterrain, Heavy Duty (for trucks/SUVs)

**Chassis (5 types):** Limousine, Van, Truck, Offroad, Luxus

**Fenders:** Basic, Chrome, Sport

**Fuel Tanks:** 15L, 30L, 50L

**Modules:** Cargo module, Fluid module, License Plate Holder

**Other parts:** Battery, Key, Maintenance Kit (10 uses for repairs)

Parts are swapped and installed at the **Garage block**. Vehicle health depletes over time and with damage; use a Maintenance Kit or Garage block to repair.

---

## Police and Crime

### Q: How does the wanted system work?

**A:** ScheduleMC uses a GTA-inspired 5-star wanted level system:

| Stars | Severity | Police Response |
|-------|----------|-----------------|
| 1 | Minor offense | Single police NPC pursues you |
| 2 | Moderate crime | 2 police NPCs respond |
| 3 | Serious crime | 3 police NPCs pursue |
| 4 | Major crime | 4 police NPCs + backup called |
| 5 | Most wanted | Full force deployment |

**Actions that increase wanted level:**
- Attacking NPCs or other players
- Stealing from NPC shops
- Producing or selling illegal products (if caught by police NPCs)
- Trespassing on government plots
- Attacking a player who has their smartphone open (+1 star penalty for the attacker)

**Wanted level decay:** Stars automatically decay at a rate of 1 star per in-game day.

---

### Q: How do I escape the police?

**A:** To escape a police pursuit, you need to break line of sight and stay hidden:

- **Distance requirement:** Get at least **40 blocks** away from pursuing police NPCs.
- **Time requirement:** Stay hidden for at least **30 seconds** without being detected.
- **Use buildings:** Enter buildings and close doors. Police NPCs will attempt to block doors during chases, but indoor spaces offer concealment.
- **Use your vehicle:** Driving away quickly can help create distance.

If police NPCs lose sight of you and the timer expires, the pursuit ends. Your wanted stars will remain and decay naturally (1 per day) unless you pay bail.

---

### Q: How does prison work?

**A:** When a police NPC catches and arrests a wanted player:

1. The player is teleported to a **prison cell** within a prison plot.
2. The player's illegal cash is confiscated (raid penalty).
3. The player's wanted level is cleared.
4. The player must serve their jail time or pay bail.

**Prison features:**
- **Multiple prisons** can exist on a server, each with multiple cells.
- **Cells have security levels** (1-5) that determine conditions.
- **Jail time** scales with the severity of crimes committed.
- Check remaining time with `/jailtime`.

**Admin commands for prison management:**
```
/prison create <id>                              - Create a new prison
/prison addcell <number> <x1,y1,z1> <x2,y2,z2> <security_level>  - Add a cell
/prison release <player>                         - Release a prisoner
```

---

### Q: How does bail work?

**A:** Bail allows arrested players to pay for early release from prison:

- Use the `/bail` command while in prison.
- The bail amount is deducted from your bank account.
- The bail fee is configured by administrators (via `/hospital setfee <amount>`).
- Upon paying bail, you are teleported to the hospital spawn point.
- Your wanted level is cleared.

If you cannot afford bail, you must serve your full jail time. The remaining time can be checked with `/jailtime`.

---

## Smartphone

### Q: How do I open the smartphone?

**A:** Press the **P key** to open the smartphone interface. This is the default keybind and can be changed in Minecraft's Controls settings under the ScheduleMC category.

The smartphone provides a custom GUI with app icons that you can click to access different features.

---

### Q: What apps are available on the smartphone?

**A:** The smartphone includes 11 functional apps:

| # | App | Icon Color | Description |
|---|-----|------------|-------------|
| 1 | **MAP** | Blue | View plot locations, markers, and navigate the world |
| 2 | **DEALER** | Red | Find drug dealers, compare prices across merchants |
| 3 | **PRODUCTS** | Green | Browse the shop product catalog |
| 4 | **ORDER** | Yellow | Order management (planned feature) |
| 5 | **CONTACTS** | Purple | View and manage player and NPC contacts |
| 6 | **MESSAGES** | Cyan | Read and send messages (inbox and chat) |
| 7 | **PLOT** | Gold | Manage your plots from anywhere |
| 8 | **SETTINGS** | Gray | Configure smartphone settings |
| 9 | **BANK** | Dark Green | View balance, transactions, and perform banking operations |
| 10 | **CRIME STATS** | Dark Red | Check your wanted level and crime history |
| 11 | **CHAT** | Cyan | Direct messaging with other players |

The smartphone app framework is extensible. Developers can register custom apps via the `ISmartphoneAPI.registerApp()` method.

---

### Q: Am I protected while using the smartphone?

**A:** Yes. ScheduleMC implements a **PvP immunity system** while the smartphone is open:

- **No damage:** You cannot take damage from any source while your smartphone GUI is open.
- **NPC protection:** NPCs will not attack you while you are using your smartphone.
- **Attacker penalty:** Any player who attacks someone with their smartphone open receives **+1 wanted star** as a penalty.
- **Fair play:** This prevents unfair kills while players are navigating menus.

The protection is managed by the `ISmartphoneAPI` and tracks which players have the smartphone open using a thread-safe concurrent set. Protection is automatically removed when you close the smartphone.

---

## Technical

### Q: How do I report bugs?

**A:** Report bugs on the GitHub issue tracker:

**URL:** [https://github.com/Minecraft425HD/ScheduleMC/issues](https://github.com/Minecraft425HD/ScheduleMC/issues)

**Please include the following information in your bug report:**
- Minecraft version (should be 1.20.1)
- Forge version (e.g., 47.4.0)
- ScheduleMC version (e.g., 3.6.0-beta)
- Steps to reproduce the issue
- Expected behavior vs. actual behavior
- Log files from `.minecraft/logs/latest.log`
- Screenshots if applicable
- List of other installed mods

---

### Q: What are common issues and their solutions?

**A:**

**Mod not loading:**
1. Verify you are using Forge 47.4.0 or higher (not Fabric, not NeoForge).
2. Confirm Java 17 is installed and selected (`java -version` to check).
3. Ensure the `.jar` file is placed directly in the `mods/` folder (not in a subfolder).
4. Check `.minecraft/logs/latest.log` for error messages.
5. Try removing other mods to rule out conflicts.
6. Delete the config files and let ScheduleMC regenerate them on the next launch.

**Plots not saving:**
1. Check that the server has sufficient disk space.
2. Verify that the server process has write permissions to the `config/` directory.
3. Look for error messages in the server log related to file I/O.
4. Run `/health plot` to perform a diagnostic check on the plot system.
5. ScheduleMC maintains automatic backups. Check the backup directory for recovery options.

**NPCs not spawning:**
1. Use the correct command syntax: `/npc spawn <type> <name>` (e.g., `/npc spawn merchant Hans`).
2. Ensure you are an operator or have the required permissions.
3. Check the server console for error messages.
4. Verify that the spawn location is in a loaded chunk.
5. Run `/health` to check overall system health.

**Economy not working:**
1. Check your balance with `/money`.
2. View the transaction history with `/money history`.
3. Run `/health economy` for a diagnostic check.
4. Verify that `config/schedulemc/economy.json` exists and is not corrupted.
5. If data is corrupted, the backup system should automatically restore from the most recent clean save.

---

### Q: How do I check system health?

**A:** Use the `/health` command (requires admin permissions) to perform diagnostic checks on all ScheduleMC subsystems:

```
/health          - Overview of all system statuses
/health economy  - Detailed economy system diagnostics
/health plot     - Plot system health check
```

The health check reports on:
- Economy system status (accounts, balances, managers)
- Plot system status (plot count, spatial index integrity)
- Backup availability and last backup time
- Manager health for all registered subsystems
- Memory usage and thread pool status

The `HealthCheckManager` automatically runs an initial health check on server startup and logs the results.

---

### Q: What are some performance tips?

**A:**

1. **Allocate sufficient RAM:** Use at least 4 GB, preferably 8 GB (`-Xmx8G` in JVM arguments).

2. **Use an SSD:** ScheduleMC performs frequent file I/O for saving data. SSD storage significantly improves save/load times.

3. **Reduce unnecessary NPC count:** Each NPC runs AI behavior goals every tick. Keep the NPC count reasonable for your server's CPU capacity.

4. **Use the IncrementalSaveManager:** ScheduleMC uses an optimized incremental save system that only writes changed data. This is enabled by default.

5. **Monitor with `/health`:** Regularly check system health to catch issues early.

6. **Keep production setups reasonable:** Excessively large numbers of production blocks (processing machines, pots, etc.) can impact server tick rate.

7. **Parallel data loading:** ScheduleMC loads persistent data in parallel at startup using thread pools. Ensure your CPU has multiple cores for optimal startup times.

---

## For Developers

### Q: How do I use the ScheduleMC API?

**A:** ScheduleMC provides a comprehensive public API with 11 subsystems. To use it in your mod:

1. **Add ScheduleMC as a dependency** in your `build.gradle`:
   ```gradle
   compileOnly files('libs/ScheduleMC-3.6.0-beta.jar')
   ```

2. **Access the API** through the singleton entry point:
   ```java
   import de.rolandsw.schedulemc.api.ScheduleMCAPI;

   // Get the API instance (available after server start)
   ScheduleMCAPI api = ScheduleMCAPI.getInstance();

   // Check if the API is ready
   if (api.isInitialized()) {
       // Use any of the 11 subsystems
       IEconomyAPI economy = api.getEconomyAPI();
       double balance = economy.getBalance(playerUUID);
   }
   ```

3. **Important:** The API is initialized during the `ServerStartedEvent`. Do not attempt to use it during mod construction or common setup. Always check `api.isInitialized()` before making calls.

---

### Q: Where is the API documentation?

**A:** API documentation is available in several locations:

- **Javadoc comments:** All API interfaces (`IEconomyAPI`, `IPlotAPI`, `INPCAPI`, etc.) are thoroughly documented with Javadoc, including usage examples, parameter descriptions, and thread-safety notes.

- **API Documentation (German):** [docs/API_DOKUMENTATION.md](../docs/API_DOKUMENTATION.md) - Complete API reference (21 KB)

- **Developer Documentation (German):** [docs/ENTWICKLER_DOKUMENTATION.md](../docs/ENTWICKLER_DOKUMENTATION.md) - Architecture and development guide (40 KB)

- **Wiki API page:** [API.md](API.md) - Wiki-formatted API overview

**API modules (11 total):**

| Module | Interface | Description |
|--------|-----------|-------------|
| Economy | `IEconomyAPI` | Accounts, deposits, withdrawals, balance queries |
| Plot | `IPlotAPI` | Plot creation, lookup, ownership, protection |
| Production | `IProductionAPI` | Plant registration, growth handling, quality system |
| NPC | `INPCAPI` | NPC spawning, schedule management, AI configuration |
| Police | `IPoliceAPI` | Wanted levels, crime tracking, arrest mechanics |
| Warehouse | `IWarehouseAPI` | Warehouse management, item storage, delivery |
| Messaging | `IMessagingAPI` | Player-to-player and system messaging |
| Smartphone | `ISmartphoneAPI` | App registration, notifications, open/close tracking |
| Vehicle | `IVehicleAPI` | Vehicle spawning, fuel management, parts |
| Achievement | `IAchievementAPI` | Achievement granting, progress tracking |
| Market | `IMarketAPI` | Dynamic pricing, supply and demand queries |

---

### Q: How do I add custom production types?

**A:** You can register custom production types through the Production API:

```java
IProductionAPI production = ScheduleMCAPI.getInstance().getProductionAPI();
production.registerCustomPlant(/* your plant configuration */);
```

Custom production types should implement the `ProductionType` interface, which defines:

```java
public interface ProductionType {
    String getDisplayName();        // Display name (e.g., "Virginia")
    String getColorCode();          // Minecraft color code (e.g., "§e")
    double getBasePrice();          // Seed/spore purchase price
    int getGrowthTicks();           // Growth time in ticks
    int getBaseYield();             // Base harvest yield
    String getProductId();          // Unique ID for the economy system
    ItemCategory getItemCategory(); // Category for pricing bounds
    double calculatePrice(ProductionQuality quality, int amount);
}
```

All custom production types automatically integrate with:
- The Unified Dynamic Pricing System (UDPS) via `calculateDynamicPrice()`
- The quality system (Poor, Good, Very Good, Legendary)
- The universal packaging system (`packaged_drug` item with NBT)

Refer to existing implementations like `TobaccoType`, `CannabisStrain`, or `MushroomType` for reference.

---

### Q: How do I create custom smartphone apps?

**A:** The Smartphone API (v3.2.0+) supports external app registration:

```java
ISmartphoneAPI smartphone = ScheduleMCAPI.getInstance().getSmartphoneAPI();

// Register a custom app
boolean success = smartphone.registerApp(
    "my_mod_app",           // Unique app identifier
    "My Custom App",        // Display name
    "§dPurple"              // Icon color code
);

// Send a notification to a player's smartphone
smartphone.sendNotification(playerUUID, "my_mod_app", "You have a new alert!");

// Check if a player has the smartphone
boolean hasPhone = smartphone.hasSmartphone(playerUUID);

// Query all registered apps
Set<String> allApps = smartphone.getRegisteredApps();

// Unregister when your mod unloads
smartphone.unregisterApp("my_mod_app");
```

Custom apps appear alongside the built-in 11 apps in the smartphone GUI. You handle the app's GUI rendering and logic on the client side, using the app ID to coordinate between your mod and the smartphone framework.

---

## Additional Resources

- [Wiki Home](Home.md) - Main wiki page
- [Getting Started Guide](Getting-Started.md) - Step-by-step beginner's guide
- [Command Reference](Commands.md) - All 161+ commands documented
- [Items Guide](Items.md) - Complete item list (354 items)
- [Blocks Guide](Blocks.md) - Complete block list (152 blocks)
- [Production Overview](Production-Systems.md) - All 14 production chains
- [GitHub Repository](https://github.com/Minecraft425HD/ScheduleMC) - Source code and issue tracker

---

**Still have questions?** Open an issue on [GitHub](https://github.com/Minecraft425HD/ScheduleMC/issues) or check the detailed documentation linked above.

[Back to Wiki Home](Home.md)
