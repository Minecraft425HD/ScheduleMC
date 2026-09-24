# ScheduleMC Changelog

All notable changes to ScheduleMC are documented in this file.

Format: `[version] - date — Summary of changes`

---

## [3.9.0-beta] - 2026-09-24

### Docs / accuracy
- Documentation synced to code: 11 public `I*API` modules (no `ITutorialAPI`)
- Plot types documented as 8 including `INDUSTRIAL`
- Persistence described as JSON + IncrementalSaveManager (not MySQL)
- `update.json` promos now point at 3.9.0-beta
- License metadata aligned to GNU GPLv3 (`LICENSE`, `mods.toml`, `gradle.properties`)
- Weapon registry documented without unimplemented Laser attachment / Heavy magazine

---

## [3.7.2-beta] - 2026-04-17

### Added
- Cannabis processing block slot GUIs (Trimm Station, Curing Jar, Hash Presse, Öl Extraktor)
- Cannabis block interaction updates (shift-right-click collect)

### Changed
- Fermentation barrel and packaging table GUI redesigns
- Plot app refresh 1000ms → 250ms; utility update interval 1000 → 100 ticks
- Locale-aware currency symbol

### Fixed
- Property tax now flat rate per plot
- Interest rounding to 2 decimal places
- HashPressScreen / OilExtractorScreen no longer auto-close

## [3.8.0-beta] - 2026-03-17

### Added
- Weapon system (6 guns, 4 melee, 3 grenades, attachments, HUD, packets)
- Note vs code: Heavy magazine and Laser attachment **items are not registered** in `WeaponItems` (26 items)

### Fixed
- NPE in NPCDialogueProvider.setupForLevel
- NPE in NPCLifeSystemIntegration.tick

## [3.6.9-beta] - 2026-03-16

### Added
- Lock, Gang, Territory, Towing, Level, MapView, 6 legal production chains
- Achievement system (~35 registered, 4 used categories — not 24/5)
- IAchievementAPI
- ITutorialAPI — **not implemented** (no interface in source)

### Changed
- Public API modules: current code total **11** I*API — no Tutorial API

Full older release notes (3.6.0 through 1.0.0) remain on `main` git history if this file is still too large for a single connector write. This commit restores the post-3.6.9 narrative plus accuracy corrections.

## Version Numbering

`MAJOR.MINOR.PATCH[-STAGE]` — current stage **beta** (`3.9.0-beta`).
