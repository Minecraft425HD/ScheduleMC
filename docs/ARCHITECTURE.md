# ScheduleMC Architecture Documentation

> Minecraft 1.20.1 Forge mod implementing a complete roleplay/economy server ecosystem.
> **Version 3.9.0-beta** | **~260k LOC** across **1,610 Java files** organized into **44+ modules**.

Canonical counts: [`docs/VERSION.md`](VERSION.md). Public API: **11** `I*API` modules (no `ITutorialAPI`). Persistence: JSON + `IncrementalSaveManager`.

The detailed module walkthrough that used to live in this file is still on `main` (same structure: entry point, event buses, 11 API impls, economy/plot/npc/vehicle/production). This header replaces the stale `3.8.0-beta` / 1,561-file stamp.

## API modules (code)

`IEconomyAPI`, `IPlotAPI`, `IProductionAPI`, `INPCAPI`, `IPoliceAPI`, `IWarehouseAPI`, `IMessagingAPI`, `ISmartphoneAPI`, `IVehicleAPI`, `IAchievementAPI`, `IMarketAPI`.
