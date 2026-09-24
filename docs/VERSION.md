# ScheduleMC version source of truth

From `gradle.properties`:

- **mod_version:** `3.9.0-beta`
- **minecraft:** `1.20.1`
- **forge:** `47.4.0`
- **license:** GNU GPLv3
- **persistence:** JSON + IncrementalSaveManager

## Verified against source (2026-09-24, updated same day after the dead-code/API cleanup)

| Claim | Code |
|---|---|
| Public API | **none** — the `de.rolandsw.schedulemc.api` package (`ScheduleMCAPI`, 11 `I*API` interfaces, `PlotModAPI`) has been deleted; zero internal consumers existed |
| Mixin framework | **none** — all 10 Mixin classes and the `schedulemc.mixins.json` config were dead (unregistered or commented-out) and have been removed |
| Optional integrations | **none** — JEI/Jade/The One Probe `compileOnly` deps removed from `build.gradle`; no integration code ever existed |
| Plot types | **8** including INDUSTRIAL |
| WeaponItems | **28** |
| Achievements | **~35** / 4 categories |
| Java / LOC | **1,568 / ~251k** |

## Updated on this branch

README.md, docs/ARCHITECTURE.md, docs/DEVELOPER_GUIDE.md, docs/TESTING.md, wiki/FAQ.md, wiki/Home.md, wiki/features/Plot-System.md, wiki/features/Weapon-System.md, wiki/features/Achievement-System.md, gradle.properties, mods.toml, update.json, docs/CHANGELOG.md (3.9 notes; older history shortened), docs/README_ERRATA.md, docs/API_REFERENCE.md (deleted).

Remaining `v3.9.0-beta` footers on other wiki feature pages mean 3.9.0-beta.
