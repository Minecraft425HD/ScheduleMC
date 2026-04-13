# ScheduleMC Changelog

All notable changes to ScheduleMC are documented in this file.

Format: `[version] - date — Summary of changes`

---

## [3.7.0-beta] - 2026-03-17

### Added
- **Weapon System** — Full weapon mod integration (46 new Java files, 26 item textures, 26 item models)
  - **Guns (6):** AK-47, Pistol, Revolver, Shotgun, Sniper Rifle, MP5 — each with individual damage, accuracy, cooldown, and magazine size stats
  - **Fire modes:** Single-shot (0), Burst (1), Auto (2) — configurable per gun via `NBT`; auto-fire loop handled server-side via `PlayerTickEvent`
  - **Ammo magazines (6):** Pistol, Rifle, Shotgun, Sniper, SMG, and Heavy magazines as separate inventory items
  - **Melee weapons (4):** Baseball Bat (knockback), Machete (bleed chance + fast foliage destruction), Combat Knife (fast attack speed), and a base `MeleeWeaponItem` class
  - **Grenades (3):** Frag (explosion radius 3.0), Smoke (campfire particles), Flash (blindness + slowness in 8-block radius) — thrown via `ThrowableItemProjectile`
  - **Attachments (3):** Scope (FOV zoom on Shift), Silencer, Laser (beam renderer); up to 2 attachments per gun stored as NBT
  - **Fire mode upgrades (3):** Single Precision, Burst Fire, Auto Fire upgrade items
  - **Custom entities:** `WeaponBulletEntity` (AbstractArrow-based, discards on hit/range) and `ThrownWeaponGrenade` (ThrowableItemProjectile-based)
  - **Particle effects:** `weapon_muzzle_flash` and `weapon_blood` particle types (using vanilla texture references)
  - **Sound events:** `weapon_gun_shot`, `weapon_empty_click`, `weapon_click`, `weapon_grenade_explode`, `weapon_reload` (registered under `schedulemc` namespace)
  - **Network packets (5):** `WeaponFirePacket`, `WeaponReloadPacket`, `WeaponStartAutoFirePacket`, `WeaponStopAutoFirePacket`, `WeaponSetAmmoTypePacket` — channel `schedulemc:weapon`
  - **Client HUD:** Ammo counter, fire mode indicator, cooldown bar — rendered via `RenderGuiOverlayEvent`
  - **Laser beam renderer:** Cross-quad beam using `RenderType.lightning()`
  - **Weapon config:** Per-gun range configuration via `schedulemc-weapons.toml` (`ForgeConfigSpec`)
  - **Creative tab:** Dedicated `WEAPON_TAB` with all 29 weapon items
  - Translations for all weapon items, subtitles, and creative tab label in `de_de.json` and `en_us.json`

### Fixed
- **NPE in `NPCDialogueProvider.setupForLevel`** — `LevelEvent.Load` fires before `ServerStartedEvent`; `DialogueManager.getManager()` returned `null`. Added early-return null guard; dialogue trees are re-registered in `onServerStarted` for all loaded levels after manager init.
- **NPE in `NPCLifeSystemIntegration.tick()`** — All 9 manager fields (`interactionManager`, `factionManager`, etc.) were `final` and set in the constructor, which ran during `LevelEvent.Load` before managers were initialized — so every field was `null`. Fix:
  - Removed `final` from all 9 manager fields
  - Added `reinitializeManagers()` method to re-fetch all manager references
  - Added null guard (`if (interactionManager == null) return`) in `tick()` to skip ticking during server startup
  - `onServerStarted` calls `NPCLifeSystemIntegration.get(lvl).reinitializeManagers()` for each loaded level after all NPC Life System managers are initialized

---

## [3.6.9-beta] - 2026-03-16

### Added
- **Lock System** — 5 lock types (Simple, Security, High-Security, Combination, Dual) with key management, lockpicking, and Code Cracker/Bypass/Omni-Hack tools. Key Ring item holds up to 8 keys.
- **Gang System** — Hierarchical criminal organizations with 4 ranks (Boss, Underboss, Member, Recruit), Level 1–30 XP progression, 20 perks in 4 branches (Territory, Economy, Crime, Production), and automated missions.
- **Territory System** — Chunk-based gang territory control with 10 color types, map editor, economic/crime bonuses, and real-time delta sync.
- **Towing System** — Vehicle towing service with Bronze/Silver/Gold membership tiers, NPC invoice screen, distance-based pricing, and per-yard revenue tracking.
- **Level System** — Producer level progression (0–30) with 53 unlockable features, XP from production and sales, smartphone app integration.
- **MapView System** — Custom minimap and world map renderer with A* road navigation, NPC icons, territory overlay, and dimension support (122 files).
- **Legal Production Chains (6)**:
  - Beer — Pilsner, Weizen, Ale, Stout with malting, mashing, fermenting, conditioning, bottling
  - Wine — Riesling, Chardonnay, Spätburgunder, Merlot with barrel aging and temperature control
  - Coffee — Arabica, Robusta, Liberica, Excelsa with 4 roast levels and altitude quality bonus
  - Chocolate — 10-step chain (roast, winnow, grind, conch, temper, mold, cool, enrobe, wrap) for 4 varieties
  - Cheese — Gouda, Emmentaler, Camembert, Parmesan with pasteurization, curdling, pressing, cave aging
  - Honey — Acacia, Wildflower, Forest, Manuka with 3 hive tiers and 4 aging stages
- **Achievement System** — 24 achievements in 5 categories (Economy, Crime, Production, Social, Exploration) with 5 tiers (Bronze to Platinum) and monetary rewards up to 50,000 EUR
- **Towing NPC Invoice Screen** — Dedicated GUI for viewing and paying towing invoices at impound yards
- **Economy Cycle** — 6-phase economic cycle (Normal → Boom → Overheating → Recession → Depression → Recovery) affecting prices and salaries
- **Risk Premium System** — Configurable risk multipliers for illegal substances affecting market prices
- **Savings Account Early Withdrawal** — Configurable penalty for early savings withdrawal
- `IAchievementAPI` — Full public API for external achievement integration
- `ITutorialAPI` — Tutorial progress control API for external mods
- 12 new test files covering Gang, Vehicle, Utility, Integration, and Command systems

### Changed
- Increased total API modules to 12 (added Achievement and Market)
- `EconomyManager` now supports batch transaction processing via `BatchTransactionManager`
- `PlotManager` spatial index upgraded to `ConcurrentHashMap`-based chunk grid (O(1) lookups)
- `WalletManager` physical cash system expanded with Euro bills and coins as tradable items
- NPCs now support Driving behavior (operate vehicles on road networks)
- NPC Witness System now integrates with Gang `CRIME_INTIMIDATION` perk
- Vehicle fuel consumption disabled while on towing yard (`isOnTowingYard` flag)
- SpotBugs version updated to 4.8.3
- Mockito updated to 5.8.0

### Fixed
- Loan repayment formula now correctly applies `totalWithInterest / durationDays`
- Plot chunk cache LRU eviction no longer causes `ConcurrentModificationException` under high load
- NPC schedule transition now handles midnight rollover correctly (24:00 → 00:00)
- Vehicle entity sync packet now includes chassis type to prevent client-side model mismatch
- Overdraft interest accrual now correctly handles Day 7 settlement before Day 28 prison penalty

---

## [3.6.0-beta] - 2025-12-20

### Added
- **Smartphone System** — 11 functional apps: Map, Dealer, Products, Order, Contacts, Messages, Plot, Settings, Bank, Crime Stats, Chat. PvP immunity while phone is open.
- **Market System** — Supply and demand-based dynamic pricing with trade volume effects, price history, trend analysis, and UDPS (Universal Dynamic Pricing System)
- **Warehouse System** — 32 inventory slots (1,024 items each = 32,768 total capacity), auto-delivery every 3 days, shop plot linking
- **Gang System (initial)** — Basic gang creation and management (expanded in 3.6.9-beta)
- **Territory System (initial)** — Basic chunk claiming (expanded in 3.6.9-beta)
- **API v3.0.0** — Central `ScheduleMCAPI` singleton with 10 initial modules
- Economy cycle system (initial version without full phases)
- Dynamic pricing anti-exploit: `AntiExploitManager` with `RateLimiter` and `BatchTransactionManager`
- Memory cleanup: `MemoryCleanupManager` for resource management

### Changed
- NPC pathfinding upgraded to A* algorithm
- Economy manager now uses `ConcurrentHashMap` for all balance operations
- Plot system adds multi-level LRU caching (`PlotCache` + `PlotChunkCache`)

### Fixed
- NPC merchants no longer sell items below base price due to floating-point rounding
- Recurring payment manager no longer processes payments during server shutdown sequence
- Plot ownership transfer now correctly updates spatial index

---

## [3.4.0-beta] - 2025-10-15

### Added
- **Vehicle System** — 5 vehicle types (Limousine, Van, Truck, SUV, Sports Car) using OBJ models via CoreLib
- Vehicle upgrade system: modular chassis, engines (3 tiers), tires (6 types), fenders, fuel tanks
- Garage blocks for vehicle storage
- Fuel station blocks with configurable fuel types
- License plate system with custom text
- Vehicle damage and crash system
- `IVehicleAPI` module

### Changed
- NPC AI expanded with `NPCDrivingGoal` for vehicle operation
- `IMessagingAPI` expanded with `broadcastMessage`, `sendSystemMessage`, `getConversation`, `blockPlayer`, `unblockPlayer` *(v3.2.0)*
- `IAchievementAPI` expanded with `getCompletionPercentage`, `getTotalRewardsEarned`, `getUnlockedAchievements`, `resetPlayerAchievements`, `getTopAchievers` *(v3.2.0)*
- `IMarketAPI` expanded with `getTopPricedItems`, `getTopDemandItems`, `hasMarketData`, `getTrackedItemCount`, `resetAllMarketData` *(v3.2.0)*
- Production framework extracted to generic `production/` package (shared by all chains)
- Test coverage improved to 60%+ overall; utility classes at 80%+

### Fixed
- OBJ model loading via CoreLib no longer causes `OutOfMemoryError` on large servers
- Vehicle fuel station interaction sometimes not detecting player correctly — fixed with improved raycasting
- NPC schedule parser now correctly handles time values like "0000" and "2359"

---

## [2.7.0-beta] - 2025-07-20

### Added
- **Messaging System** — Player-to-player and player-to-NPC messaging with persistent history, NPC reputation responses, and real-time notifications
- **Tutorial System** — 7-step onboarding flow with progress tracking, skip option, and completion rewards
- **Daily Rewards System** — 50 EUR base + 10 EUR per streak day (up to 30-day streak = 350 EUR/day)
- **Shop Investments** — Players can buy shares in NPC shops (1,000 EUR/share)
- Smartphone initial version (basic map and bank apps)
- `IMessagingAPI` initial version
- `ISmartphoneAPI` module

### Changed
- NPC relationship system now has range of -100 to +100 (was 0–100)
- Economy transaction history expanded to 1,000 entries per player (was 100)
- PlotManager now supports `TOWING_YARD` plot type
- Documentation overhaul (initial `docs/` folder created)

### Fixed
- NPC dialogue trees no longer lose context after player relog
- Economy manager `getBalance` returned wrong value for players with exactly 0.0 balance

---

## [2.6.0] - 2025-05-10

### Added
- **Prison System** — Cells, bail system, evidence tracking, sentencing
- **Hospital System** — Configurable respawn fees and spawn point
- **Bounty System** — Player bounties with `/bounty` commands
- **Utility System** — Power and water tracking for buildings
- `IPoliceAPI` expanded with arrest triggers and evidence management
- `HealthCheckManager` with `/health` command covering 38 subsystems

### Changed
- Police AI now uses `PoliceAIHandler` with backup calling mechanics
- Crime detection radius now configurable via `config/schedulemc-common.toml`
- NPC witness system expanded with evidence types and evidence decay

### Fixed
- Police NPC sometimes spawned outside prison during arrest sequence
- Bail payment via ATM block now correctly deducts from bank account (not wallet)

---

## [2.5.0] - 2025-03-18

### Added
- **Vehicle System** (initial version) — Basic vehicle entities with CoreLib integration
- **Lock System** (initial version) — Simple and Security locks
- **Gang System** (initial version) — Basic gang creation
- `IVehicleAPI` initial version

### Changed
- NPC entity now uses CustomNPCEntity with component architecture
- Production block entities migrated to generic `production/blockentity/` framework

---

## [2.0.0] - 2025-01-15

### Added
- **NPC System** — Full AI system with schedules, personalities (4 types), shop integration, wallet/salary, pathfinding, dialogue trees, quests, and social relationships
- **Police and Crime System** — 5-star wanted level with auto-decay, police AI with chase/arrest, door blocking during pursuit
- **Production Systems (8 illegal chains)**:
  - Tobacco — Virginia, Burley, Oriental, Havana (6 steps, quality system)
  - Cannabis — Indica, Sativa, Hybrid, Autoflower (8 steps with hash/oil)
  - Coca — Bolivianisch, Kolumbianisch (5 steps, chemical extraction)
  - Poppy — Afghanisch, Türkisch, Indisch (6 steps, heroin refinery)
  - Meth — (4 steps, explosion risk)
  - LSD — (6 steps, laboratory process)
  - MDMA — (4 steps, timing minigame)
  - Mushrooms — Cubensis, Azurescens, Mexicana (4 steps, climate control)
- `INPCAPI`, `IPoliceAPI`, `IProductionAPI` modules
- Credit score system (`CreditScoreManager`)
- Overdraft protection (`OverdraftManager`)
- 3-tier loan system (SMALL 5K, MEDIUM 25K, LARGE 100K)
- Savings accounts with weekly interest

### Changed
- Economy manager rebuilt for thread safety with `ConcurrentHashMap`
- Plot system adds `PUBLIC`, `GOVERNMENT`, `PRISON` plot types

---

## [1.7.0-alpha] - 2024-11-05

### Added
- **Warehouse System** — Mass storage with auto-delivery, shop plot linking, and NPC merchant integration
- Minimap initial version (basic rendering, no navigation)
- Plot optimizations: spatial indexing with chunk-based lookup

### Fixed
- Plot save/load performance improved (5x faster with spatial index)
- NPC shop inventory sync no longer triggers on every tick (debounced to 5s)

---

## [1.6.0] - 2024-09-20

### Added
- Economy event system: `RespawnHandler` (hospital fees), `BusinessMetricsUpdateHandler`
- NPC `NPCStealingHandler` — NPCs can be pickpocketed
- Tax system: property tax per chunk per month, sales tax (configurable VAT)
- Recurring payments system (`RecurringPaymentManager`)
- `IEconomyAPI` expanded with savings and overdraft methods
- Anti-exploit rate limiting (`RateLimiter`)

### Fixed
- Plot rent collection no longer double-charges on server restart
- NPC movement sometimes teleported to incorrect position on chunk boundary

---

## [1.0.0] - 2024-07-01

### Initial Release

- **Plot Management System** — 5 plot types (Residential, Commercial, Shop, Public, Government), chunk-based ownership, trusted players, block protection, apartment sub-leasing, 5-star ratings
- **Economy System** — Bank accounts (1,000 EUR starting balance), ATM blocks, physical cash items (Euro bills and coins), transaction history
- `IEconomyAPI`, `IPlotAPI` — First public API modules
- Forge 1.20.1 / 47.4.0 support
- CoreLib 1.20.1-1.1.1 dependency for OBJ models, GUI, networking
- JUnit 5 + Mockito test infrastructure
- Initial Gradle build configuration with JaCoCo coverage

---

## Version Numbering

ScheduleMC uses a modified semantic versioning scheme:

```
MAJOR.MINOR.PATCH[-STAGE]
```

| Component | Description |
|-----------|-------------|
| `MAJOR` | Breaking API changes or complete system rewrites |
| `MINOR` | New systems or major feature additions |
| `PATCH` | Bug fixes, balance changes, minor improvements |
| `-STAGE` | `alpha` (unstable), `beta` (feature-complete, testing), no suffix (stable) |

Current stage: **beta** — All systems are feature-complete and tested, but balance tuning and community feedback are ongoing.

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-13**.
- Diese Datei wurde im Rahmen der Vollständigkeits-Aktualisierung überarbeitet.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

