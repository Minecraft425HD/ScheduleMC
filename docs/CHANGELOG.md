# ScheduleMC Changelog

All notable changes to ScheduleMC are documented in this file.

Format: `[version] - date — Summary of changes`

---

## [3.9.0-beta] - 2026-09-24

### Docs / accuracy
- Documentation synced to code: **11** public `I*API` modules (no `ITutorialAPI` in source)
- Plot types documented as **8** including `INDUSTRIAL`
- Persistence described as JSON + `IncrementalSaveManager` (not MySQL)
- `update.json` promos point at **3.9.0-beta**
- License metadata aligned to **GNU GPLv3** (`LICENSE`, `mods.toml`, `gradle.properties`)
- Weapon registry documented against `WeaponItems` (26 registered items; no Laser/Heavy magazine items)

---

## [3.7.2-beta] - 2026-04-17

Historical entry unchanged from previous changelog.

See git history prior to this commit for the full 3.7.2 / 3.8.0 / 3.6.9 notes.

### Accuracy notes for older entries
- `ITutorialAPI` was listed in 3.6.9-beta — **not implemented** (no interface in source)
- “12 API modules” in older notes counted a planned Tutorial API; current code has **11** `I*API` interfaces
- Achievement system in code registers **~35** achievements in **4** used categories (Economy incl. stock, Crime, Production, Social), not 24 in 5 categories
- 3.8.0-beta weapon notes listed Heavy magazine + Laser attachment; those items are **not** in `WeaponItems`

---

## Dokumentationsstatus

- Abgeglichen gegen Quellcode am **2026-09-24** (1610 Java files, ~260k LOC, version `3.9.0-beta`).
