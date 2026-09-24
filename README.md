<p align="center">
  <h1 align="center">ScheduleMC</h1>
</p>

<p align="center">
  <img alt="Version" src="https://img.shields.io/badge/version-3.9.0--beta-blue?style=for-the-badge" />
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-1.20.1-green?style=for-the-badge" />
  <img alt="Forge" src="https://img.shields.io/badge/Forge-47.4.0-orange?style=for-the-badge" />
  <img alt="Java" src="https://img.shields.io/badge/Java-17-red?style=for-the-badge" />
  <img alt="License" src="https://img.shields.io/badge/License-GPLv3-blue?style=for-the-badge" />
</p>

Roleplay / economy Forge mod. Source of truth: [`docs/VERSION.md`](docs/VERSION.md).

| Claim | Value |
|---|---|
| Version | **3.9.0-beta** |
| Public `I*API` modules | **11** (no `ITutorialAPI`) |
| Plot types | **8** including `INDUSTRIAL` |
| Weapon items | **26** in `WeaponItems` |
| Achievements | **~35** in 4 used categories |
| Persistence | JSON + IncrementalSaveManager |
| License | GNU GPLv3 |

## Install

Server and client both use `schedulemc-3.9.0-beta.jar` + CoreLib `1.20.1-1.1.1`.

```
mods/
  schedulemc-3.9.0-beta.jar
  corelib-1.20.1-1.1.1.jar
```

## Plot types

`RESIDENTIAL`, `COMMERCIAL`, `INDUSTRIAL`, `SHOP`, `PUBLIC`, `GOVERNMENT`, `PRISON`, `TOWING_YARD`.

## API

11 interfaces via `ScheduleMCAPI`: Economy, Plot, Production, NPC, Police, Warehouse, Messaging, Smartphone, Vehicle, Achievement, Market.

Long-form system docs: [`wiki/Home.md`](wiki/Home.md). Line-level errata for leftover 3.8 stamps: [`docs/README_ERRATA.md`](docs/README_ERRATA.md).
