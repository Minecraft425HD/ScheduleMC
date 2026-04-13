# Territory-System (Territorium-System)

<div align="center">

**Chunk-Based Territory Control for Gangs**

Strategic map control with economic bonuses, crime protection, and real-time synchronization

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Territory Basics](#territory-basics)
4. [Territory Colors and Types](#territory-colors-and-types)
5. [Territory Limits by Gang Level](#territory-limits-by-gang-level)
6. [Territory Advantages](#territory-advantages)
7. [Map Editor (Admin)](#map-editor-admin)
8. [Territory Capture and Defense](#territory-capture-and-defense)
9. [Network Synchronization](#network-synchronization)
10. [Integration with Gang System](#integration-with-gang-system)
11. [Data Persistence](#data-persistence)
12. [Admin Guide](#admin-guide)
13. [Best Practices](#best-practices)
14. [Troubleshooting](#troubleshooting)

---

## Overview

The Territory System allows gangs to claim, control, and defend chunks of the game world. Controlled territories provide economic advantages, reduced police attention, production bonuses, and gang XP. Territory limits scale with gang level and unlocked perks, creating a meaningful progression system for criminal organizations.

### Key Features

- **Chunk-Based Control** - One territory = one 16×16 block chunk
- **10 Color Types** - Visually distinguish territories on the map
- **Gang Level Scaling** - More territories unlock as your gang levels up
- **Perk Tree** - 5 dedicated territory perks expand your control further
- **Economic Bonuses** - Shop income boosts and sales bonuses in owned territory
- **Crime Protection** - Faster wanted level decay and NPCs ignoring crimes
- **MapView Integration** - Territories displayed as colored overlays on the in-game map
- **Real-Time Sync** - Delta updates keep all clients synchronized with minimal bandwidth
- **Admin Map Editor** - Visual chunk-based territory management tool

### Statistics

| Metric | Value |
|--------|-------|
| Territory size | 1 chunk (16×16 blocks) |
| Territory colors/types | 10 |
| Minimum territories (Level 1) | 1 |
| Maximum territories without perks (Level 30) | 25 |
| Maximum territories (Level 28+ with TERRITORY_EMPIRE) | Unlimited |
| Gang XP per territory held per day | 2 XP |
| Gang XP for capturing new territory | 15 XP |

---

## Architecture

```
TerritoryManager
  ├── Territory              -- Individual territory data (position, gang, name, color, timestamp)
  ├── TerritoryType          -- Enum of 10 color types
  └── network/
        ├── OpenMapEditorPacket      -- Server → Client: open the map editor
        ├── SyncTerritoriesPacket    -- Server → Client: full territory sync
        └── SyncTerritoryDeltaPacket -- Server → Client: incremental updates
```

### Integration with Gang System

```
GangManager ◄──────── TerritoryManager
     │                       │
     │ Gang perk unlocked     │ Territory claimed
     ▼                       ▼
Check territory limit    Validate against limit
     │                       │
     └────────────────────────┘
          MapView overlay
```

---

## Territory Basics

### What is a Territory?

A territory is a single Minecraft chunk (16×16 blocks in the X and Z axes, full height Y) owned by a gang. Territories are displayed as colored overlays on the in-game map.

### Territory Properties

| Property | Description |
|----------|-------------|
| **Position** | Chunk X/Z coordinates (not block coordinates) |
| **Gang Owner** | UUID of the owning gang |
| **Name** | Custom user-defined name (e.g., "HQ", "Harbor", "Downtown") |
| **Type/Color** | One of 10 color-coded zone types |
| **Created At** | Timestamp of when territory was claimed |

### Territory Identity

Territories are identified by their chunk position. A gang can only own one territory per chunk. Attempting to claim an already-owned chunk fails.

---

## Territory Colors and Types

| Color | Chat Code | Hex | Suggested Usage |
|-------|-----------|-----|-----------------|
| **Red** | `&c` | `#FF4444` | Gang headquarters, important zones |
| **Green** | `&a` | `#44FF44` | Safe zones, farms |
| **Orange** | `&6` | `#FFAA00` | Markets, trade routes |
| **Blue** | `&9` | `#4444FF` | Residential areas |
| **Yellow** | `&e` | `#FFFF44` | Neutral zones |
| **Purple** | `&d` | `#FF44FF` | Industrial areas |
| **Cyan** | `&b` | `#44FFFF` | Ports, outskirts |
| **Gray** | `&7` | `#AAAAAA` | Uncategorized |
| **Dark Red** | `&4` | `#AA0000` | High-security zones |
| **Lime** | `&2` | `#88FF44` | Agricultural zones |

Colors are purely cosmetic and serve as organizational tools for gang leadership to categorize their territory map.

---

## Territory Limits by Gang Level

The maximum number of chunks a gang can control increases with gang level. Perks from the [Gang System](Gang-System.md) can further expand this limit.

| Gang Level | Base Chunks | With TERRITORY_EXPAND | With TERRITORY_DOMINANCE | With TERRITORY_STRONGHOLD | With TERRITORY_EMPIRE |
|-----------|-------------|----------------------|--------------------------|--------------------------|----------------------|
| 1–7 | 1 | — | — | — | — |
| 8–14 | 4 | 9 | — | — | — |
| 15–21 | 9 | — | 16 | — | — |
| 22–27 | 16 | — | — | 25 | — |
| 28–30 | 25 | — | — | — | Unlimited |

**Notes:**
- Perk prerequisites require minimum gang levels (e.g., TERRITORY_EXPAND requires Level 3)
- Each perk replaces the previous tier's limit (they do not stack additively)
- TERRITORY_EMPIRE (Level 28) removes all limits

---

## Territory Advantages

### Economic Advantages

Controlled territory provides economic benefits to gang members operating within it:

| Perk Required | Benefit | Condition |
|--------------|---------|-----------|
| None | 2 XP/day per held chunk | Passive, automatic |
| `ECONOMY_TAX` (Level 10) | +5% income from shops | Only shops within owned territory |
| `ECONOMY_MONOPOLY` (Level 27) | +15% sales bonus | All sales in owned territory |

### Crime Reduction Advantages

| Perk Required | Benefit |
|--------------|---------|
| `CRIME_PROTECTION` (Level 5) | Wanted level decays 20% faster while in territory |
| `CRIME_INTIMIDATION` (Level 23) | NPCs in territory will not report crimes to police |
| `CRIME_UNTOUCHABLE` (Level 29) | Maximum wanted level capped at 3 (instead of 5) while in territory |

### Intelligence / Defense Advantages

| Perk Required | Benefit |
|--------------|---------|
| `TERRITORY_FORTIFY` (Level 8) | Gang officers receive a notification when a member of another gang enters the territory |

---

## Map Editor (Admin)

The Map Editor is a visual tool available to server operators (OP Level 2+) for managing territory assignments.

### Opening the Editor

```
/map edit        Open the visual territory map editor
/map info        Display territory statistics (total territories, gang breakdown)
```

### Editor Features

- **Visual grid** - Each cell represents one 16×16 chunk
- **Click to assign** - Click a chunk to assign/modify territory
- **Color picker** - Select territory type from the 10 available colors
- **Name field** - Set a custom name for the selected territory
- **Gang selector** - Assign territory to any gang or clear it
- **Existing data overlay** - Shows existing plots, NPCs, and locks for reference
- **Real-time sync** - Changes apply immediately to all connected clients

### Editor Workflow

1. Open with `/map edit`
2. The editor opens as a fullscreen GUI overlay on the map
3. Use the mouse to select chunks
4. Choose a color/type and enter a name in the sidebar
5. Confirm to apply the change
6. Changes sync to all clients via `SyncTerritoryDeltaPacket`

---

## Territory Capture and Defense

### Claiming Territory

Gang bosses and underbosses can claim unclaimed chunks within their territory limit:

```
/gang territory claim [name]      Claim the chunk you are standing in
/gang territory claim <x> <z> [name]   Claim a specific chunk by coordinates
/gang territory unclaim           Release your gang's territory at current location
```

### Defending Territory

- The `TERRITORY_FORTIFY` perk alerts officers when enemies enter
- `CRIME_INTIMIDATION` makes NPCs ignore crimes, reducing police interference
- `CRIME_UNTOUCHABLE` caps wanted levels, making criminal operations safer

### Territory Conquest

If a gang exceeds their territory limit (e.g., due to level loss or perk removal), they cannot claim new territories but keep existing ones. To reclaim space, they must manually unclaim territories.

---

## Network Synchronization

Territory data is synchronized to all clients to enable the MapView overlay.

| Packet | Direction | When Sent | Description |
|--------|-----------|-----------|-------------|
| `OpenMapEditorPacket` | Server → Client | Admin uses `/map edit` | Opens the visual editor GUI on the admin's client |
| `SyncTerritoriesPacket` | Server → Client | Player joins / explicit sync | Complete territory dataset (all gangs) |
| `SyncTerritoryDeltaPacket` | Server → Client | Territory claimed/unclaimed/modified | Incremental update for changed chunks only |

### Bandwidth Efficiency

- Full sync (`SyncTerritoriesPacket`) only happens on join or explicit request
- Delta packets (`SyncTerritoryDeltaPacket`) carry only the changed chunk(s)
- This keeps ongoing sync overhead minimal even with large territory maps

---

## Integration with Gang System

Territories are deeply integrated with the Gang System:

### XP from Territory

| Activity | XP Earned |
|----------|-----------|
| Holding a territory chunk (per day) | 2 XP |
| Capturing a new territory chunk | 15 XP |

### Level Requirements

The territory system enforces gang level requirements:
- Minimum Level 1 to claim any territory (1 chunk)
- Higher levels automatically increase the limit

### Perk Integration

Five dedicated territory perks in the Gang System directly affect territory:

| Perk | Level Req | Effect |
|------|-----------|--------|
| `TERRITORY_EXPAND` | 3 | Increase territory limit from 4 to 9 |
| `TERRITORY_FORTIFY` | 8 | Enemy gang entry notifications |
| `TERRITORY_DOMINANCE` | 15 | Increase territory limit from 9 to 16 |
| `TERRITORY_STRONGHOLD` | 22 | Increase limit to 25 + sales bonus |
| `TERRITORY_EMPIRE` | 28 | Unlimited territory |

See [Gang System](Gang-System.md) for full perk details.

---

## Data Persistence

**File:** `config/plotmod_territories.json`

### Stored Data per Territory

| Field | Type | Description |
|-------|------|-------------|
| `chunkX` | int | Chunk X coordinate |
| `chunkZ` | int | Chunk Z coordinate |
| `dimension` | String | Dimension key (e.g., `minecraft:overworld`) |
| `gangUUID` | UUID | Owning gang's UUID |
| `name` | String | Territory name |
| `type` | TerritoryType | Color/type enum value |
| `createdAt` | long | Epoch milliseconds timestamp |

### Auto-save

Territory changes are saved immediately on claim/release/modify and also during the periodic `IncrementalSaveManager` cycle.

---

## Admin Guide

### Initial Server Setup

1. **Create gang plots** - Before territories can be meaningful, create city plots for gang activities:
   ```
   /plot create "Harbor" COMMERCIAL
   /plot create "Downtown" COMMERCIAL
   ```

2. **Set up the map** - Ensure the MapView system is enabled in `config/mapview.properties`:
   ```properties
   worldmapAllowed=true
   minimapAllowed=true
   showTerritories=true
   ```

3. **Pre-assign starter territories** - Use `/map edit` to pre-define important zones before gangs form.

4. **Monitor territory expansion** - Use `/map info` to see which gangs hold which territories and how many.

### Admin Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/map edit` | OP Level 2 | Open the visual territory map editor |
| `/map info` | OP Level 2 | Show territory statistics |
| `/gang admin setlevel <gang> <level>` | OP Level 2 | Adjust territory limit via level change |

### Balancing

- **Territory limit** - The base limits (1, 4, 9, 16, 25) are designed to require significant gang investment to expand. If you want faster expansion, lower the gang XP requirements via `/gang admin addxp`.
- **Economic bonuses** - The 5% and 15% income bonuses are deliberately modest. In a healthy economy, gang territory should be strategically valuable but not mandatory.
- **Crime protection** - `CRIME_UNTOUCHABLE` (cap at wanted level 3) is very powerful. It requires Level 29, ensuring only end-game gangs benefit.

---

## Best Practices

### For Gang Leaders

1. **Prioritize contiguous territory** - Connected chunks are more defensible and create cleaner territory maps.
2. **Name your territories** - Use descriptive names (e.g., "HQ", "Drug Lab Zone", "Market") so your members know what's protected.
3. **Use colors strategically** - Assign colors by zone type (red for HQ, green for production zones, etc.) to organize your operations.
4. **Invest in TERRITORY_FORTIFY early** - At Level 8 it's affordable and the early-warning notification is operationally valuable.

### For Server Administrators

1. **Don't pre-assign gang territories** - Let gangs earn territory through gameplay. Pre-assignment undermines the progression system.
2. **Use the map editor for world features only** - Pre-define territory types for real-world areas (harbor, industrial zone) but leave gang ownership to the game.
3. **Watch for territory stagnation** - If one gang holds all available territory early, other gangs can't progress. Consider adjusting level limits or XP requirements.
4. **Regular `/map info` checks** - Monitor the territory distribution weekly to spot imbalances.

---

## Troubleshooting

### Territory not visible on map

1. **Enable territories in mapview config** - Set `showTerritories=true` in `config/mapview.properties`.
2. **Client sync** - Territory data syncs on join. If a territory was just claimed, reconnect or wait for the next delta sync.
3. **MapView system enabled** - Verify `minimapAllowed=true` and `worldmapAllowed=true`.

### Cannot claim territory

1. **Gang level** - Check gang level with `/gang info`. Level 1 allows only 1 territory.
2. **Already at limit** - Count current territories with `/gang info`. At the limit until a perk is unlocked.
3. **Chunk already owned** - Another gang (or your gang) already owns this chunk. Use `/map info` to see owner.
4. **Permission** - Only BOSS and UNDERBOSS ranks can claim territory.

### Territory bonus not applying

1. **Perk check** - Economic bonuses require `ECONOMY_TAX` or `ECONOMY_MONOPOLY` perks. Use `/gang info` to verify perks.
2. **Location check** - You must be standing in your gang's territory for the bonus to apply.
3. **NPC shop link** - For the `ECONOMY_TAX` bonus, the shop must be within the territory chunk, not just near it.

### Data lost after restart

1. **Check file** - Open `config/plotmod_territories.json` to verify it's valid JSON.
2. **Backup recovery** - Restore from `config/backups/plotmod_territories_<timestamp>.json`.
3. **Re-sync** - After restoring, use `/map info` to verify data loaded correctly.

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-13**.
- Diese Datei wurde im Rahmen der Vollständigkeits-Aktualisierung überarbeitet.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

