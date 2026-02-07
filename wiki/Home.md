# ScheduleMC Wiki

<div align="center">

![Version](https://img.shields.io/badge/Version-3.6.0--beta-blue?style=for-the-badge)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green?style=for-the-badge)
![Forge](https://img.shields.io/badge/Forge-47.4.0-orange?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-red?style=for-the-badge)
![License](https://img.shields.io/badge/License-GPLv3-purple?style=for-the-badge)

**The Most Comprehensive Minecraft Economy and Roleplay Mod**

*Complete server-side ecosystem for roleplay, economy, production, vehicles, NPCs, and more.*

</div>

---

## Project Overview

**ScheduleMC** is a professional-grade Minecraft Forge mod for version 1.20.1 that implements a full-featured roleplay and economy server system. Designed for dedicated Minecraft servers, it provides interconnected game systems ranging from banking and real estate to NPC AI, law enforcement, vehicle simulation, and multi-step production chains. The mod is built with a modular architecture, a public API for third-party developers, and comprehensive test coverage.

### At a Glance

| Metric | Value |
|---|---|
| **Version** | 3.6.0-beta |
| **Minecraft** | 1.20.1 |
| **Forge** | 47.4.0 |
| **Java** | 17 |
| **Lines of Code** | 93,349 |
| **Total Files** | 1,407 |
| **Registered Items** | 354 |
| **Registered Blocks** | 152 |
| **Available Commands** | 139 |
| **Production Chains** | 14 (8 illegal + 6 legal) |
| **API Modules** | 12 |
| **Unit Tests** | 292 |
| **GUI Screens** | 126 |
| **Manager Classes** | 55 |
| **BlockEntity Types** | 131 |
| **License** | GNU General Public License v3 |
| **Author** | Luckas R. Schneider (Minecraft425HD) |

---

## Wiki Navigation

### Getting Started

| Page | Description |
|---|---|
| [Getting Started](Getting-Started.md) | Installation guide, prerequisites, first-time setup, and first steps for both players and server administrators |
| [Commands](Commands.md) | Complete reference for all 139 commands organized by category with syntax, permissions, and examples |
| [Items](Items.md) | Full catalog of all 354 registered items with descriptions, categories, and usage information |
| [Blocks](Blocks.md) | Complete listing of all 152 registered blocks including multi-block structures and special behaviors |
| [Production Systems](Production-Systems.md) | Overview of all 14 production chains (8 illegal + 6 legal) with flowcharts, item lists, and quality mechanics |
| [FAQ](FAQ.md) | Frequently asked questions, common issues, and troubleshooting guide |

---

### Feature Systems

Detailed documentation for each major game system.

| Page | System | Summary |
|---|---|---|
| [Economy System](features/Economy-System.md) | Banking, Loans, and Taxes | Full financial ecosystem with bank accounts, ATMs, physical cash, 3-tier loans, savings accounts, recurring payments, overdraft protection, shop investments, property/sales/income taxes, daily rewards with streak bonuses, and a government treasury |
| [Plot System](features/Plot-System.md) | Land Management | 5 plot types (Residential, Commercial, Shop, Public, Government), spatial indexing for O(log n) lookups, apartment sub-leasing, 5-star rating system, trusted player permissions, block and inventory protection, daily rent with auto-eviction, and 50% refund on abandonment |
| [NPC System](features/NPC-System.md) | AI Characters | Schedule-driven NPCs with 4 personality types, 3 NPC roles (Resident, Merchant, Police), custom player skins, shop and warehouse integration, relationship system, wallet and salary mechanics, pathfinding AI, and interactive dialogue |
| [Police and Crime System](features/Police-Crime-System.md) | Law Enforcement | GTA-inspired 5-star wanted level system with auto-decay, police AI with chase and arrest mechanics, backup calling, prison system with cells and bail, raid penalties, door blocking during pursuits, and PvP crime detection |
| [Vehicle System](features/Vehicle-System.md) | Drivable Vehicles | 5 vehicle types (Limousine, Van, Truck, SUV, Sports Car), modular parts system (engines, tires, chassis, fenders, fuel tanks), fuel stations, garage blocks, license plates, vehicle damage, OBJ model support via CoreLib, and 137 dedicated Java files |
| [Smartphone System](features/Smartphone-System.md) | In-Game Phone | 11 functional apps (Map, Dealer, Products, Order, Contacts, Messages, Plot, Settings, Bank, Crime Stats, Chat), PvP immunity while using, configurable keybind (default: P), and extensible app framework |
| [Warehouse System](features/Warehouse-System.md) | Mass Storage | 32 inventory slots each holding 1,024 items (32,768 total capacity), automatic deliveries every 3 days, shop plot linking, NPC merchant integration, state-funded delivery costs, and revenue tracking |
| [Tutorial System](features/Tutorial-System.md) | Player Onboarding | 7-step interactive tutorial covering welcome, economy basics, plot system, NPCs, production, trading, and completion rewards with progress tracking and skip options |
| [Market System](features/Market-System.md) | Dynamic Pricing | Supply and demand-based economy with dynamic prices, trading volume effects, price history tracking, trend analysis, and time-based decay |
| [Gang System](features/Gang-System.md) | Criminal Organizations | Hierarchical gangs with ranks (Boss, Underboss, Member, Recruit), XP/level progression (1-30), 20 perks across 4 branches, automated missions, weekly fees, and reputation tiers |
| [Territory System](features/Territory-System.md) | Gang Territories | Chunk-based territory control with 10 color-coded zone types, map editor, economic and crime bonuses, and real-time synchronization |
| [Lock System](features/Lock-System.md) | Door Security | 5 lock types (Simple, Security, High-Security, Combination, Dual), key management with 3 origins, lockpicking, hacking tools, and combination code entry |
| [Towing System](features/Towing-System.md) | Vehicle Towing | Membership-based towing service (Bronze/Silver/Gold tiers), invoice payment via NPC, distance-based pricing, revenue tracking, and smartphone integration |
| [Achievement System](features/Achievement-System.md) | Player Achievements | 24 achievements across 5 categories with 5 difficulty tiers (Bronze to Platinum), automatic tracking, monetary rewards up to 50,000 EUR |
| [Level System](features/Level-System.md) | Producer Progression | Level 0-30 progression with 53 unlockable features, XP from production and sales, unlock system for strains, machines, vehicles, and economy features |
| [MapView System](features/MapView-System.md) | In-Game Map | Advanced map renderer with minimap, world map, A* road navigation, NPC display, territory overlay, and configurable display settings |
| [Messaging System](features/Messaging-System.md) | Communication | Player-to-player and player-to-NPC messaging with persistent history, reputation-based NPC responses, real-time notifications, and smartphone integration |

---

### Production Systems

Each production chain has its own dedicated documentation page with step-by-step guides, item lists, block descriptions, and quality mechanics.

| Page | Product | Strains/Variants | Steps | Key Mechanic |
|---|---|---|---|---|
| [Tobacco System](production/Tobacco-System.md) | Tobacco | Virginia, Burley, Oriental, Havana | 6 | Most complex chain: planting, growing, drying, fermentation, packaging, selling. 4 pot types, 3 grow light tiers, quality system (Poor to Legendary) |
| [Cannabis System](production/Cannabis-System.md) | Cannabis | Indica, Sativa, Hybrid, Autoflower | 8 | Full chain including drying net, trim station, curing jars, hash press, and oil extractor. Produces buds, hash, and oil |
| [Coca System](production/Coca-System.md) | Cocaine and Crack | Bolivianisch, Kolumbianisch | 5 | Chemical extraction using diesel, 3 vat sizes, 3 refinery sizes with glowing effect, and crack cooking with baking soda |
| [Poppy System](production/Poppy-System.md) | Opium and Heroin | Afghanisch, Turkisch, Indisch | 6 | Scoring machine, opium press, cooking station for morphine, heroin refinery |
| [Meth System](production/Meth-System.md) | Crystal Meth | -- | 4 | Dangerous production with explosion risk at the reduction stage. Uses ephedrine, pseudoephedrine, red phosphorus, and iodine |
| [LSD System](production/LSD-System.md) | LSD Blotter | -- | 6 | Scientific laboratory process: fermentation, distillation (glowing apparatus), chemical synthesis, micro-dosing with GUI, perforation press |
| [MDMA System](production/MDMA-System.md) | Ecstasy Pills | -- | 4 | Arcade-style production with a timing minigame at the pill press stage. Glowing reaction vessel, heated drying oven |
| [Mushroom System](production/Mushroom-System.md) | Psilocybin Mushrooms | Cubensis, Azurescens, Mexicana | 4 | Spore syringes, mist bags (3 sizes), climate lamps (3 tiers), water tank environment requirement |

#### Legal Production Chains

| Page | Product | Varieties | Steps | Key Mechanic |
|---|---|---|---|---|
| [Coffee System](production/Coffee-System.md) | Coffee | Arabica, Robusta, Liberica, Excelsa | 5 | Altitude-based quality bonus, wet/dry processing methods, 4 roast levels, 4 grind sizes, 3 packaging sizes |
| [Wine System](production/Wine-System.md) | Wine | Riesling, Chardonnay, Spaetburgunder, Merlot | 6 | Temperature-based quality, 4 sweetness levels, 3 aging stages, auto-aging in barrels |
| [Beer System](production/Beer-System.md) | Beer | Pilsner, Weizen, Ale, Stout | 5 | Complete brewing process with malting, mashing, fermenting, conditioning, and bottling. Temperature-sensitive |
| [Cheese System](production/Cheese-System.md) | Cheese | Gouda, Emmentaler, Camembert, Parmesan | 5 | Pasteurization, curdling with rennet, pressing, cave aging (4 stages), 3 processing methods |
| [Honey System](production/Honey-System.md) | Honey | Acacia, Wildflower, Forest, Manuka | 5 | Beekeeping with 3 hive tiers, extraction, filtering, creaming, 4 aging stages (up to Vintage) |
| [Chocolate System](production/Chocolate-System.md) | Chocolate | Milk, White, Dark, Ruby | 10 | Most complex legal chain: roasting, winnowing, grinding, conching, tempering, molding, cooling, enrobing, wrapping |

---

### API and Developer Documentation

Technical documentation for mod developers and server plugin authors.

| Document | Location | Description |
|---|---|---|
| Towing System Setup | [docs/TOWING_SYSTEM_SETUP.md](../docs/TOWING_SYSTEM_SETUP.md) | Setup guide for the vehicle towing and impound system |
| Towing NPC Invoice Screen | [docs/TOWING_NPC_INVOICE_SCREEN.md](../docs/TOWING_NPC_INVOICE_SCREEN.md) | Technical documentation for the towing NPC invoice GUI |

#### Public API Modules

ScheduleMC exposes 12 public API modules through the `ScheduleMCAPI` singleton. All API interfaces are located in the `de.rolandsw.schedulemc.api` package.

| Module | Interface | Purpose |
|---|---|---|
| Economy | `IEconomyAPI` | Account management, deposits, withdrawals, balance queries, transactions |
| Plot | `IPlotAPI` | Plot creation, lookup, ownership, region queries |
| Production | `IProductionAPI` | Custom plant registration, production chain hooks |
| NPC | `INPCAPI` | NPC spawning, configuration, schedule management |
| Police | `IPoliceAPI` | Wanted level management, crime records, arrest triggers |
| Vehicle | `IVehicleAPI` | Vehicle spawning, part management, fuel operations |
| Warehouse | `IWarehouseAPI` | Item storage, delivery scheduling, shop linking |
| Messaging | `IMessagingAPI` | Player-to-player and system messaging |
| Smartphone | `ISmartphoneAPI` | Custom app registration, notification system |
| Achievement | `IAchievementAPI` | Achievement granting, progress tracking |
| Tutorial | `ITutorialAPI` | Tutorial step management, progress control |
| Market | `IMarketAPI` | Price queries, supply/demand data, market manipulation |

**API usage example:**

```java
ScheduleMCAPI api = ScheduleMCAPI.getInstance();
IEconomyAPI economy = api.getEconomyAPI();
economy.deposit(playerUUID, 1000.0);
double balance = economy.getBalance(playerUUID);
```

---

## Quick Reference for Players

### First Join Checklist

When you join a ScheduleMC server for the first time, the following happens automatically:

- A bank account is created with a starting balance of 1,000 EUR
- A welcome message is displayed
### Essential Player Commands

```
/state balance              Check your bank balance
/state deposit <amount>     Deposit money into state treasury
/state withdraw <amount>    Withdraw from state treasury
/bail                       Pay bail to leave prison
/jailtime                   Check remaining prison sentence
/gang create <name>         Create a new gang
/gang info                  View your gang info
/bounty list                View active bounties
/market prices              View current market prices
/lock code                  Lock a block with a code
```

### Getting Started Workflow

1. Explore the server and find available plots
2. Use `/plot apartment list` to find available apartments
3. Rent an apartment with `/plot apartment rent <id>`
4. Check production systems and start crafting
5. Trade with NPC merchants or other players
6. Use the smartphone (press P) to access the map, banking, and messaging apps

---

## Quick Reference for Admins

### Server Setup Commands

```
/plot create public "Spawn"                    Create a public spawn area
/plot create government "Town Hall"            Create a government building
/npc <name> info                               View NPC details
/npc <name> schedule workstart 0700            Set NPC work schedule
/npc <name> schedule workend 1800              Set NPC end-of-day
/npc <name> setshop <shopId>                   Link NPC to a shop
/money give <player> <amount>                  Grant money to a player
/state deposit <amount>                        Fund the government treasury
/hospital setspawn                             Set the hospital respawn point
/hospital setfee <amount>                      Set the hospital fee
/prison create <name>                          Create a prison
/prison addcell <id> <pos1> <pos2> <security>  Add a cell to a prison
/warehouse setshop <shopId>                    Link a warehouse to a shop
/warehouse add <item> <amount>                 Stock a warehouse
/health                                        Run system diagnostics
```

### System Monitoring

- Use `/health` to run diagnostics across all subsystems
- Use `/health economy` to check economy-specific status
- Use `/health plot` to verify plot data integrity
- Automatic backup system protects against data corruption
- Atomic file writes prevent save file corruption

---

## Technology Stack

| Component | Technology | Version | Purpose |
|---|---|---|---|
| Game Platform | Minecraft | 1.20.1 | Base game |
| Mod Loader | Minecraft Forge | 47.4.0 | Modding framework and event system |
| Language | Java | 17 | Primary development language |
| JSON Library | Gson | 2.10.1 | Data serialization and configuration |
| Vehicle Framework | CoreLib | 1.20.1-1.1.1 | Networking, OBJ model loading, GUI utilities |
| Unit Testing | JUnit Jupiter | 5.10.1 | Test framework |
| Mocking | Mockito | 5.8.0 | Test doubles and behavior verification |
| Assertions | AssertJ | 3.24.2 | Fluent test assertions |
| Code Coverage | JaCoCo | 0.8.11 | Coverage measurement and reporting |
| Build System | Gradle | -- | Build automation, dependency management |
| Mappings | Official (Mojang) | 1.20.1 | Obfuscation mappings |

### Optional Integrations

These mods are not required but provide enhanced functionality when installed:

| Mod | Purpose |
|---|---|
| JEI (Just Enough Items) | Recipe viewing and item lookup |
| Jade | Block information tooltips on hover |
| The One Probe | Advanced block and entity information overlay |

### Architecture Highlights

| Pattern | Application |
|---|---|
| Singleton | `ScheduleMCAPI.getInstance()`, manager classes |
| Template Method | `AbstractPersistenceManager` for data storage |
| Strategy | Interchangeable production system behaviors |
| Observer | Forge event bus integration |
| Factory | Entity and item creation |
| Registry | Deferred Register for items and blocks |
| Command | Command pattern for all 139 commands |
| Facade | Simplified public API interfaces |

---

## Version History

| Version | Status | Highlights |
|---|---|---|
| **3.6.0-beta** | **Current** | Towing system, gang and territory systems, additional crafting (beer, wine, coffee, chocolate, cheese, honey), map view, lock system, level system, continued API expansion |
| 3.4.0-beta | -- | Vehicle system enhancements, API v3 stabilization, expanded unit test coverage |
| 2.7.0-beta | -- | Market system, smartphone apps, documentation overhaul |
| 2.6.0 | -- | Prison system, utility system (power and water tracking) |
| 2.5.0 | -- | Vehicle system introduction with CoreLib integration |
| 2.0.0 | -- | NPC system, all 8 production chains, police and crime system |
| 1.7.0-alpha | -- | Warehouse system with auto-delivery, minimap improvements, plot optimizations |
| 1.6.0 | -- | Initial feature set and bug fixes |
| 1.0.0 | -- | Plot management system, core economy |

---

## Additional Source Modules

Beyond the primary systems documented above, version 3.6.0-beta includes the following modules visible in the source tree. These represent newer or supplementary systems:

| Module | Package | Description |
|---|---|---|
| Beer | `beer` | Beer brewing and serving |
| Wine | `wine` | Wine production |
| Coffee | `coffee` | Coffee roasting and preparation |
| Chocolate | `chocolate` | Chocolate crafting |
| Cheese | `cheese` | Cheese making |
| Honey | `honey` | Beekeeping and honey production |
| Gang | `gang` | Player gang/faction system |
| Territory | `territory` | Territory control mechanics |
| Towing | `towing` | Vehicle towing and impound service |
| Lock | `lock` | Door and container locking |
| Map View | `mapview` | In-game map rendering |
| Level | `level` | Player leveling and progression |

---

## System Requirements

### Minimum

| Requirement | Specification |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.0 or higher |
| Java | 17 |
| RAM | 4 GB allocated to Minecraft |

### Recommended

| Requirement | Specification |
|---|---|
| RAM | 8 GB or more |
| Storage | SSD |
| CPU | Quad-core or better |

---

## Building from Source

```bash
git clone https://github.com/Minecraft425HD/ScheduleMC.git
cd ScheduleMC
./gradlew build
```

Additional Gradle tasks:

| Task | Description |
|---|---|
| `./gradlew runClient` | Launch the Minecraft client with the mod loaded for testing |
| `./gradlew runServer` | Launch a dedicated server with the mod loaded |
| `./gradlew test` | Run all unit tests |
| `./gradlew jacocoTestReport` | Generate HTML and XML code coverage reports |

---

## Project Structure

```
ScheduleMC/
|-- src/main/java/de/rolandsw/schedulemc/
|   |-- ScheduleMC.java            Main mod entry point
|   |-- ModCreativeTabs.java        Creative mode tab registration
|   |-- api/                        Public API (12 modules)
|   |-- commands/                   Command implementations (139)
|   |-- economy/                    Economy system (11 manager classes)
|   |-- region/                     Plot management and spatial indexing
|   |-- npc/                        NPC AI, schedules, and behaviors
|   |-- tobacco/                    Tobacco production chain
|   |-- cannabis/                   Cannabis production chain
|   |-- coca/                       Coca/cocaine production chain
|   |-- poppy/                      Poppy/opium production chain
|   |-- meth/                       Methamphetamine production chain
|   |-- lsd/                        LSD production chain
|   |-- mdma/                       MDMA/ecstasy production chain
|   |-- mushroom/                   Psilocybin mushroom production chain
|   |-- production/                 Generic production framework
|   |-- vehicle/                    Vehicle system (137 files)
|   |-- warehouse/                  Warehouse and auto-delivery
|   |-- messaging/                  Messaging and contacts
|   |-- market/                     Dynamic market pricing
|   |-- client/                     Client-side rendering and smartphone
|   |-- gui/                        GUI screens (32 screens)
|   |-- network/                    Client-server packet handling
|   |-- config/                     Configuration management
|   |-- managers/                   Core system managers
|   |-- events/                     Forge event handlers
|   |-- items/                      Item registration
|   |-- data/                       Data generation
|   |-- util/                       Utility classes
|   |-- player/                     Player data and state
|   |-- gang/                       Gang/faction system
|   |-- territory/                  Territory control
|   |-- towing/                     Towing and impound
|   |-- lock/                       Locking system
|   |-- mapview/                    Map rendering
|   |-- level/                      Level and progression
|   |-- beer/                       Beer crafting
|   |-- wine/                       Wine crafting
|   |-- coffee/                     Coffee crafting
|   |-- chocolate/                  Chocolate crafting
|   |-- cheese/                     Cheese crafting
|   |-- honey/                      Honey crafting
|   |-- achievement/                Achievement system
|   `-- utility/                    Utility tracking (power/water)
|-- src/main/resources/
|   |-- META-INF/mods.toml          Mod metadata
|   `-- assets/schedulemc/          Models, textures, blockstates, lang
|-- src/test/java/                  Unit tests (200+)
|-- docs/                           Technical documentation
|-- wiki/                           This wiki
|-- build.gradle                    Build configuration
`-- gradle.properties               Project properties
```

---

## Support and Links

| Resource | Link |
|---|---|
| GitHub Repository | [github.com/Minecraft425HD/ScheduleMC](https://github.com/Minecraft425HD/ScheduleMC) |
| Issue Tracker | [GitHub Issues](https://github.com/Minecraft425HD/ScheduleMC/issues) |
| Discord | [discord.gg/schedulemc](https://discord.gg/schedulemc) |

### Reporting Bugs

When reporting an issue, include the following information:

- Minecraft version (1.20.1)
- Forge version
- ScheduleMC version (3.6.0-beta)
- Steps to reproduce the problem
- Relevant log output from `.minecraft/logs/latest.log`
- Output of the `/health` command if applicable

---

<div align="center">

ScheduleMC v3.6.0-beta -- Minecraft 1.20.1 -- Forge 47.4.0

Developed by Luckas R. Schneider (Minecraft425HD)

Licensed under the GNU General Public License v3

</div>
