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

## Synced on this branch

- `gradle.properties`, `mods.toml`, `update.json`
- `wiki/Home.md`
- `wiki/features/Weapon-System.md`
- `wiki/features/Achievement-System.md` (header)
- `docs/CHANGELOG.md` (3.9.0 notes; older history shortened)
- `docs/README_ERRATA.md` (README + ARCHITECTURE line fixes)

## Still stamped 3.8.0-beta in-page

Feature wiki footers and some `docs/*.md` headers. Read them as 3.9.0-beta. Full in-place rewrite of 200k+ files is tracked as follow-up; errata above is authoritative until then.
