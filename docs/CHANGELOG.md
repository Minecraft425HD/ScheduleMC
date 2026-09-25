# ScheduleMC Changelog

All notable changes to ScheduleMC are documented in this file.

Format: `[version] - date — Summary of changes`

---

## [3.9.5-beta] - 2026-09-25

### Changed — Merged NPCInteractionManager and NPCSocialInteractionManager
Verified before merging (per explicit request) whether these were a simple duplicate or whether
one side had more current features — found neither: `NPCInteractionManager` was ticked regularly
but none of its rich action methods (`converse()`, `greet()`, `initiateNPCTrade()`) were ever
triggered by anything; `NPCSocialInteractionManager` was a complete, self-contained NPC-relation
simulator with real trigger logic, but was never ticked at all. Merged the trigger/scan logic
(`autoTriggerNearbyInteractions()`) into `NPCInteractionManager`, wired it into
`NPCLifeSystemIntegration.tick()` at a 200-tick (10s) cadence, and made the NPC-relation map
(`npcRelations`) persisted (previously transient, lost on every restart). Deleted
`NPCSocialInteractionManager.java` (208 lines, verified zero remaining references).

### Added — Registered `CrimeRecordCommand`
`/crimerecord <player> [evidence|clear]` was fully implemented but never registered with any
command dispatcher. Now registered in `ScheduleMC.onRegisterCommands()`.

### Investigated, not changed — `WantedListSyncPacket`
Verified this packet (meant to back a "Wanted Posters" smartphone app) is unregistered, its
client-side cache has zero readers, no server code ever constructs/sends it, and — checked
against all 15 registered smartphone apps in `SmartphoneScreen.java` — no such app exists at all.
Registering the packet alone would be a hollow fix (nothing would ever send or display it); a real
fix needs a new UI screen plus a new send-trigger, which is a new feature, not a wiring quick win.
Left unregistered and documented as a deferred gap in CLAUDE.md.

---

## [3.9.4-beta] - 2026-09-25

### Added — Wired in four fully-built but completely orphaned systems
All four had zero external callers anywhere in the codebase (verified by grep), despite being
complete, working implementations:
- **`WarehouseMarketBridge`** (real warehouse-stock-based supply/demand signal) now feeds into
  `DynamicPriceManager`'s item price multiplier, but only once per Minecraft day
  (`onDayChange()` → `updateWarehouseData()`), not in real time — it rides the same 7-day
  rolling average as the rest of the S&D system, so a single day's stock swing only shifts the
  used price by ~1/7.
- **`PriceModifier`** (the complete NPC price-modifier calculation: trait/greed + emotion +
  faction reputation + player relationship + market condition) now backs
  `CustomNPCEntity.getPersonalPriceModifier(player, level, isBuying)`, replacing the previous
  greed+emotion-only inline calculation that was missing faction/reputation/market factors
  entirely. All 3 real call sites (`PurchaseItemPacket`, `NegotiationPacket`,
  `OpenMerchantShopPacket`) updated.
- **`BatchTransactionManager`** now batches `InterestManager`'s weekly interest payout across
  all accounts in one pass instead of depositing one at a time (its stated "66-90% performance
  win" doesn't actually apply to this codebase's `EconomyManager.markDirty()`, which is already
  a cheap idempotent flag set — the real value here is the cleaner bulk API and batch
  statistics, not a performance fix).
- **`CrimeEventHandler.registerVandalism()`/`registerTrespassing()`** now fire from
  `BlockProtectionHandler`'s existing plot-permission checks: breaking a block in a plot you
  don't have access to now reports Vandalism to the witness system, placing one reports
  Trespassing — previously the action was silently cancelled with no crime record at all.

### Removed — Two more duplicate/dead systems found while wiring the above
- **`npc/entity/component/TradingComponent`** — turned out to be a *third* implementation of
  the NPC price modifier (alongside `PriceModifier` and the inline one in `CustomNPCEntity`).
  It ticked every 20 ticks for every NPC computing a near-identical greed+emotion value that
  **nothing ever read** — pure wasted computation, not just dead code. Deleted along with its
  registration.
- **`npc/life/economy/NegotiationSystem`** — a generic round-based price-negotiation system,
  superseded by the tobacco/drug-specific `NegotiationEngine` (the one actually used by
  `NegotiationPacket`). Zero callers; deleted rather than wired in since the newer engine
  already covers the same use case completely.

Also found, documented but intentionally left alone: `npc/life/witness/BriberySystem`
("bribe a witness to suppress their report") is itself completely unwired (0 callers) —
looks like an unfinished feature needing a real UI/packet flow, not a wiring fix. Several
`CrimeType` values (`DRUG_USE`, `FRAUD`, `BRIBERY`) still have no real trigger anywhere in the
game; no safe existing hook was found for them, so nothing was guessed in. See `CLAUDE.md` for
the full verification trail.

## [3.9.3-beta] - 2026-09-25

### Added — Meth now has 3 separately-priced purity variants
Meth previously used a single flat fallback price regardless of purity, because it has no
strain/variety NBT the way cannabis/tobacco/coca/poppy do — `PackagedDrugItem.parseVariant()`
always returned `null` for it, so it never got a UDPS product id (dead code path noted in the
previous release). New `meth/MethVariant.java` (`STANDARD`/`GOOD`/`BLUE_SKY`, base prices
30/50/80€, its own S&D-tracked UDPS product) is derived from `MethQuality` instead of a
missing variant tag (`MethVariant.fromQuality()`: POOR+GOOD→STANDARD, VERY_GOOD→GOOD,
LEGENDARY→BLUE_SKY). `PackagedDrugItem.resolveVariant(DrugType, ItemStack)` picks the right
resolution strategy per drug type and is now used by both the tooltip price preview and the
real `NegotiationPacket` sale-completion path. Renamed the pre-existing German product key
`METH_GUT` to `METH_GOOD` everywhere (`EconomyController`, `ModConfigHandler` config defaults,
`EconomyPricesConfigScreen`).

### Changed — Prices now recalculate once per Minecraft day, smoothed over a 7-day average
Previously, `DynamicPriceManager.getItemPriceMultiplier()`/`getProductPriceMultiplier()`
recomputed the live supply/demand multiplier on every single price query, so one large
transaction could swing the price all the way to its configured bound instantly. The
underlying supply/demand accumulation is unchanged (still updates immediately on every
purchase/sale, still decays on the existing `update_interval_minutes` cadence) — but the
multiplier actually used for pricing is now a rolling 7-Minecraft-day average, recalculated
once per day at day-rollover. A single day's spike now only shifts the used price by roughly
1/7; sustained pressure over several days still clearly moves it. New per-item
(`DynamicPriceManager.PriceSmoothingState`) and per-product (`ProductMarketState`'s new
`multiplierHistory`/`effectiveMultiplier` fields) history, persisted so it survives restarts.
`/market prices|trends|stats|top` intentionally still shows the raw, live-updating S&D data
for admin/diagnostic visibility — only the price actually charged is smoothed.

## [3.9.2-beta] - 2026-09-25

### Fixed — Drug-deal sales never triggered XP, economy tracking, or S&D updates
Verified, via grepping every caller of `EconomyController.getSellPrice()`/`ProductionType
.calculateDynamicPrice()` in the codebase (Honey/Beer/Chocolate/Wine/Coffee/Tobacco/Cannabis/
Coca items), that **every single one** passes `playerUUID = null` — these calls only ever run
from item tooltip previews, never from an actual sale. The real money transfer for drug/tobacco
deals happens entirely inside `NegotiationPacket` (NPC negotiation), which paid the player
directly via `WalletManager`/NPC wallet without ever calling back into `EconomyController` with
a real player UUID. Result: `ProducerLevel.awardSaleXP()` (the `SELL_LEGAL`/`SELL_ILLEGAL` XP
sources) was **never triggered anywhere in the game**, `GlobalEconomyTracker.onSale()` (inflation/
money-supply tracking) never saw these sales, and supply/demand tracking for production goods
was permanently empty regardless of how well the S&D system itself was wired.
Extracted the duplicated tracking block from both `getSellPrice()` overloads into a new
`EconomyController.recordCompletedSale(productId, amount, quality, revenue, playerUUID)`
method (also reduces code duplication) and call it from `NegotiationPacket` right after a
negotiated deal's money changes hands — the price itself is untouched (already negotiated/paid),
only XP, economy tracking, and S&D are now reported. Covers cannabis, coca, poppy/heroin,
mushroom, and tobacco deals (identified via the existing `PackagedDrugItem.parseVariant()` /
`ProductionType.getProductId()`/`getItemCategory()` infrastructure). `DrugType.METH` has no
variant type in `parseVariant()` and is knowingly left unresolved rather than guessing a
product id — see `CLAUDE.md`.

### Changed — Removed the third, also-dead S&D map in EconomyController
`EconomyController.marketDataMap` (a separate, String-keyed `MarketData` map, confirmed 100%
dead — `registerMarketData()` had zero callers anywhere) is deleted. `DynamicPriceManager`
(already the sole item-level S&D owner as of the previous release) gained a second, String-keyed
`ProductMarketState` map with the same ratio^factor math, and `EconomyController
.getSupplyDemandMultiplier()`/`updateSupplyOnSale()` now delegate to it. `DynamicPriceManager`
is now the single system that owns all supply/demand state in the mod, for both NPC-shop items
and production-good sell/buy prices.

## [3.9.1-beta] - 2026-09-25

### Fixed — Config-screen slider ranges & style inconsistency
- Corrected all UI slider `min`/`max` bounds across 7 config screens (Police, Advanced
  Economy, Economy, NPC/Navigation, Plot, Stealing, Workshop) to exactly match their
  `ForgeConfigSpec.defineInRange()` bounds — 66 mismatches found and fixed. A mismatched
  slider could silently write a value outside the field's actual spec-enforced range, or
  make part of the visible slider track produce a clamped, unreachable value. Also fixed 3
  label/precision bugs the corrected (tighter) ranges exposed (Police speed limit, Stealing
  indicator/zone sliders), and a pre-existing mislabeling on the NPC navigation path-update
  slider (said "ticks", the value is actually milliseconds).
- Fixed a style inconsistency in `ScheduleMC`'s weekly gang-fee task: `GangManager
  .getInstance()` was called without the defensive null-check every other manager singleton
  in the same method uses.

### Changed — DynamicPriceManager/DynamicMarketManager merge
Merged the two overlapping, only-partially-functional pricing systems into one. See
`CLAUDE.md` ("DynamicPriceManager/DynamicMarketManager Merge") for the full rationale.
`DynamicPriceManager` now also owns a real per-item supply/demand market (absorbed from
the deleted `DynamicMarketManager`, which was 100% inert — nothing ever called its
`registerItem`/`setEnabled`/`.tick()`/`load()`). `PurchaseItemPacket` now registers
NPC-shop items on first purchase, applies the item's live S&D price multiplier, and feeds
the purchase back into the demand tracker — so the config-driven `sd_factor`/
`min_multiplier`/`max_multiplier`/`sd_decay_rate` values (previously only affecting the
unrelated `MarketCondition` state machine, or entirely unused in `sd_decay_rate`'s case)
now have a real, player-visible effect on NPC shop prices. The per-item price also folds in
`SeasonalPriceModifier` (via a new `ItemCategory` → seasonal-category mapping applied at
registration), so NPC shop prices for plants/food/chemicals/weapons/luxury/building-material
items fluctuate with the season the same way UDPS category prices already did — previously
the per-item market had no seasonal component at all. `/market prices|trends|stats|top`
and `HealthCheckManager`'s market check now read from `DynamicPriceManager` instead of the
deleted class. Persistence merged into the existing `npc_life_prices.json`; the separate
`plotmod_market.json` file is no longer written.

## [3.9.0-beta] - 2026-09-24

### Fixed — Config Editor: ~40 previously-cosmetic settings now actually apply
A full audit of every `ModConfigHandler` field found 59 settings, editable in the in-game
Config Editor and saved to `schedulemc-common.toml`, that no game logic ever read — changing
them in the UI silently did nothing. Wired the ones with a safe, behavior-preserving default
into real code:

- **Plot System**: `min_plot_size`/`max_plot_size` (now enforced as plot *area* in
  `InputValidation.validatePlotRegion`, replacing a hardcoded 10,000-block-per-side-only
  cap), `min_plot_price`/`max_plot_price` (enforced in `PlotSalePacket`),
  `max_trusted_players` (enforced in `PlotCommand`/`PlotTrustPacket`, previously unlimited),
  `refund_on_abandon` (now drives `PlotTaxService.calculateAbandonSplit`, replacing a
  hardcoded 50%), `min_rent_price` (enforced in `PlotSalePacket`/`/plot rent`). Removed
  `allow_plot_transfer` and `min_rent_days`/`max_rent_days` — dead config for features that
  no longer exist (plot transfer was replaced by the Settings App sale/purchase flow years
  ago; multi-day renting only ever existed in the unreferenced `RentManager` class, the live
  rent-purchase flow always rents for a hardcoded 1 day).
- **Police**: `vehicle_pursuit_enabled`, `vehicle_speed_multiplier` (now actually threaded
  through `NPCDrivingScheduler`/`NPCDrivingTask` into pursuit speed), `siren_enabled`,
  `warning_enabled`, `warning_timeout_seconds`, `traffic_violations_enabled`,
  `container_scan_depth`, `evidence_multiplier_enabled` (now scales jail sentences by
  evidence strength via `EvidenceManager`, previously collected but never applied),
  `max_roadblocks`/`roadblock_duration_seconds` (wired into `PoliceRoadblock`, though nothing
  currently calls `createRoadblock()` — that AI trigger was never built). `siren_sound_radius`,
  `speed_limit_default`, `flanking_enabled`, and `wanted_posters_min_level` remain
  unimplemented stubs — no corresponding feature exists in the codebase at all.
- **Economy**: `shop_enabled` (gates `PurchaseItemPacket`), `ratings_enabled`,
  `allow_multiple_ratings`, `min_rating`/`max_rating` (enforced in `PlotRatingPacket`/
  `PlotRegion`, also fixed a UI bug letting the max-rating slider go to 10 despite the config
  clamping to 5), `economy_cycle_enabled` (gates `EconomyCycle` in `EconomyController`),
  `economy_cycle_min/max_duration_days` and `event_base_chance` (now clamp/scale the
  6 per-phase hardcoded values in `EconomyCyclePhase` rather than being ignored),
  `level_system_enabled` (gates `ProducerLevel.awardXP`), `illegal_xp_multiplier`/
  `legal_xp_multiplier` (now applied in `XPSource.calculateXP`), all 5 `risk_premium.*`
  fields (now read in `RiskPremium.java`, replacing 5 hardcoded constants with identical
  default values), `save_interval_minutes` (now passed to `IncrementalSaveManager` at
  startup instead of a hardcoded 1-minute interval). Left `shop.buy_multiplier`/
  `sell_multiplier` and `dynamic_pricing.daily_food_cost`/`daily_reference_income` unwired:
  the first has a non-neutral default (`1.5`) that would silently raise every shop price 50%
  rather than fix a bug, the rest have no corresponding mechanism anywhere in the codebase to
  attach to. (`dynamic_pricing.sd_decay_rate` was wired in the following release, 3.9.1-beta,
  as part of the `DynamicPriceManager`/`DynamicMarketManager` merge.) Also left
  `level_system.max_level`/`base_xp`/
  `xp_exponent` unwired — `LevelRequirements`'s XP table is a `static final` array computed
  at class-load time, before Forge guarantees config is loaded; wiring it safely needs a
  config-reload hook, not a one-line substitution.
- **Dynamic Pricing (UDPS)**: `enabled`, `min_multiplier`/`max_multiplier` (now clamp
  `DynamicPriceManager.calculatePrice()`'s combined modifier, previously unbounded),
  `update_interval_minutes` (now a real elapsed-time gate on `updateMarketConditions()`,
  decoupled from the day-change/season logic that used to run it once per Minecraft day
  regardless of this setting), `sd_factor` (repurposed as the daily market-condition
  transition chance, replacing the hardcoded `MARKET_CHANGE_CHANCE = 0.3f`).
- **NPC/Map Navigation**: `navigation.scan_radius`, `path_update_interval`,
  `arrival_distance` — these actually belong to the smartphone/world-map road-navigation
  system (`RoadNavigationService`), not NPC AI movement goals as their screen placement
  implied; wired in, replacing 3 hardcoded constants with identical default values.

See `docs/CONFIGURATION.md` for the full per-key breakdown, including which settings remain
intentionally unwired and why.

### Removed
- **Public API deleted** — the entire `de.rolandsw.schedulemc.api` package
  (`ScheduleMCAPI`, 11 `I*API` interfaces, all 11 `*APIImpl` classes, and the
  legacy `PlotModAPI` facade — 5,689 lines). Verified zero internal consumers
  before removal; all internal code already used the manager classes
  directly. There is currently no supported integration API for third-party
  mods.
- **Mixin framework deleted** — all 10 Mixin/connector classes and
  `schedulemc.mixins.json`. The 8 MapView mixins had their `@Mixin`
  annotation commented out (dead since a failed 1.20.1 port) and the 2
  vehicle mixins had no loading mechanism anywhere (no `[[mixins]]` in
  `mods.toml`, no `MixinConfigs` manifest attribute, no `IMixinConnector`
  service registration) and were very likely never applied either. The
  vehicle fuel/speed HUD (previously injected over the XP bar via mixin)
  was reimplemented mixin-free via `RenderGuiOverlayEvent`; the vehicle
  volume slider mixin was dropped with no functional loss (already
  available via `ClientConfigScreen`).
- **JEI / Jade / The One Probe `compileOnly` dependencies removed** — zero
  integration code for any of the three ever existed in the source.
- **~7,000+ lines of dead code removed** across a full automated scan
  (unused private/protected members, whole orphaned classes, unused
  imports), including `commands/PlotCommand.java`'s 16 unregistered
  handler methods for plot sub-commands the wiki already documented as
  moved to the Settings App UI, and 4 fully orphaned classes
  (`gui/PlotMenuGUI`, `messaging/NPCMessageTemplates`,
  `npc/pathfinding/NPCNodeEvaluator`, `mapview/util/LayoutVariables`).
  See `docs/CODE_VS_DOCS_ABGLEICH_2026-09-24.md` for the full breakdown.
- **Legacy radius-based police raid scan removed** — `IllegalActivityScanner.scanArea()`
  (deprecated in favor of `scanRoomBased()`) and the `police.room_scan_enabled`
  toggle that switched between them. `PoliceAIHandler` now always uses the
  room-based scan; the now-unused `police.raid_scan_radius` config key and
  its config-screen slider were also removed.

### Docs / accuracy
- All API/Mixin/JEI-Jade-TOP references removed or corrected across
  README, ARCHITECTURE, DEVELOPER_GUIDE, TESTING, CONFIGURATION, FAQ,
  Home, and the affected feature wiki pages, following the removals above.
  `docs/API_REFERENCE.md` deleted (documented a package that no longer
  exists).
- File/LOC counts updated to current values (1,568 Java files, ~251k LOC).
- 11 public I*API modules (no ITutorialAPI in source) — superseded, see Removed above
- 8 plot types including INDUSTRIAL
- Persistence: JSON + IncrementalSaveManager
- License metadata: GNU GPLv3
- WeaponItems: 28 registered items; no Laser/Heavy magazine items

---

## [3.7.2-beta] - 2026-04-17

### Added
- **Seasonal market pricing now driven by Serene Seasons (if installed)** — `SeasonalPriceModifier` previously always used its own internal 120-game-day calendar; it now uses the real Serene Seasons season when that optional mod is present, matching the Vehicle System's tire-traction mechanic (`SereneSeasonsCompat`). Falls back to the internal calendar otherwise, or when the new `dynamic_pricing.season_use_serene_seasons` config option (default: on) is disabled. Toggle is editable in-game via the Dynamic Pricing (UDPS) config screen.
- **`/season` command** — shows the current season with an icon and a 10-segment progress bar, active per-category price changes, and (in fallback mode) days until the next season; shows a sub-season phase indicator instead when driven by Serene Seasons.
- **Cannabis Processing Blocks — Slot-based GUI** — All 4 processing blocks (Trimm Station, Curing Jar, Hash Presse, Öl Extraktor) now display item slots directly in their GUI:
  - **Trimm Station:** Input slot (DriedBud), TrimmedBud output slot, Trim output slot — items are stored in the machine, not taken from player inventory
  - **Curing Jar:** Input slot (TrimmedBud), Output slot (live quality preview of resulting CuredBud)
  - **Hash Presse:** Input slot (Trim display), Output slot (Hash when pressing complete)
  - **Öl Extraktor:** Input slot (material), Solvent slot, Output slot (oil)
- **Cannabis Block Interactions:**
  - **Trimm Station:** Right-click with DriedBud = load input slot; Shift+Right-click = collect output (or return input); Right-click empty hand = open GUI
  - **Hash Presse:** Shift+Right-click with empty hand = collect hash (was: plain right-click); Right-click with Trim = fill (unchanged)
  - **Öl Extraktor:** Shift+Right-click with empty hand = collect oil (was: plain right-click); Right-click with material/solvent = fill (unchanged)
  - **Curing Jar:** Interaction unchanged — Right-click with TrimmedBud fills; Shift+Right-click extracts CuredBud

### Changed
- **Fermentation Barrel GUIs (Tobacco — Small/Medium/Big):** Complete visual redesign — dark theme, progress bar with gloss highlight, capacity bar, consistent 176×166 layout; color-coded by size (Small=green, Medium=orange, Big=red)
- **Packaging Table GUIs (Tobacco — Medium/Large):** Redesigned to match SmallPackagingTable layout — dark theme, 2×5 output grid on the left, info panel on the right, buttons at y=145, hotbar at y=168
- **Large Packaging Table:** Output grid changed from 3×3 (9 slots) to 2×5 (10 slots), matching the Medium Packaging Table layout; BlockEntity expanded from 10 to 11 slots
- **Slot labels removed:** Removed "Input"/"Output" text labels above machine slots in all Packaging Table screens (visual cleanup)
- **Plot App refresh rate:** `DATA_REFRESH_INTERVAL_MS` reduced from 1000 ms to 250 ms; `UtilityEventHandler.UPDATE_INTERVAL` reduced from 1000 ticks (~50 s) to 100 ticks (~5 s) — utility consumption data now updates within ~5 seconds instead of ~50 seconds
- **Currency symbol locale-aware:** `format.currency` translation key added (`€` in `de_de.json`, `$` in `en_us.json`); all 12 hardcoded `€` in `PlotAppScreen` replaced; `outstanding_fmt` and `bill_detail_fmt` lang keys updated per locale

### Fixed
- **Arrest fines and illegal-cash detection were silently no-ops:** `CashItem.getValue()/setValue()/addValue()/removeValue()` had been reduced to stub methods (always returning `0.0`/`false`) when cash storage moved to the UUID-based `WalletManager`, but `PoliceAIHandler` (arrest fine deduction) and `IllegalActivityScanner` (raid cash-threshold detection) were never migrated off them. Effect in practice: every arrest treated the player as having €0, always doubled jail time and showed a "cash confiscated" message without actually removing any money; raids could never flag a player for carrying suspiciously large cash. Both now read/write `WalletManager` directly; the dead stub methods on `CashItem` were removed. Also fixed the related dead-item-pickup path in `CashSlotRestrictionHandler` that showed a misleading "+0.00€ added to wallet" message.
- **Property tax calculation:** Replaced chunk-area-based formula with flat rate per plot (`TAX_PROPERTY_PER_CHUNK` config value × number of owned plots); tax amount is now predictable and config-driven
- **Regular bank interest rounding:** `InterestManager.payoutInterest()` now rounds to 2 decimal places (`Math.round(interest * 100.0) / 100.0`) and skips payouts below 0.005€ — prevents log spam with amounts like `23.475724`
- **Savings account interest:** `SavingsAccount.calculateAndPayInterest()` skips payout when balance < 0.005€ (was 0.01€); result rounded to 2dp; `SavingsAccountManager` also uses `>= 0.005` threshold
- **HashPressScreen / OilExtractorScreen:** Removed erroneous `this.onClose()` call that automatically closed the GUI when output was ready; output is now shown in the output slot instead

## [3.8.0-beta] - 2026-03-17

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
- `ITutorialAPI` — **not implemented** (no interface in source)
- 12 new test files covering Gang, Vehicle, Utility, Integration, and Command systems

### Changed
- Public API is 11 I*API modules (Achievement + Market added; no Tutorial API)
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
- Vehicle workshop blocks for storage, repairs, and upgrades
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
