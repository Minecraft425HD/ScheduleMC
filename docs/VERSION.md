# ScheduleMC version source of truth

Canonical runtime version comes from `gradle.properties`:

- **mod_version:** `3.9.0-beta`
- **minecraft:** `1.20.1`
- **forge:** `47.4.0`
- **license:** GNU GPLv3 (`LICENSE`, `mods.toml`, `gradle.properties`)
- **persistence:** JSON + `IncrementalSaveManager` (not MySQL)

## Counts verified against source (2026-09-24)

| Claim | Code |
|---|---|
| Public `I*API` modules | **11** (no `ITutorialAPI`) |
| Plot types | **8** including `INDUSTRIAL` |
| Weapon items in `WeaponItems` | **26** (no Laser attachment item, no Heavy magazine item) |
| Achievement registrations | **~35** in 4 used categories |
| Java files / LOC | **~1610 / ~260k** |

## Docs still drifting on this branch

- `README.md` still says 12 API modules, 7 plot types, client JAR `3.8.0-beta` in one install snippet
- `docs/ARCHITECTURE.md` header still `3.8.0-beta` / 1561 files
- many wiki feature pages still stamp `3.8.0-beta`
- `docs/CHANGELOG.md` on this branch was shortened; restore full history from `main` then prepend the 3.9.0 section
