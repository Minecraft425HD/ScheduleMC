# MapView-System

<div align="center">

**Advanced In-Game Map Renderer with Navigation and Territory Overlay**

122 files powering a minimap, world map, A* road navigation, and real-time entity display

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Minimap](#minimap)
4. [World Map](#world-map)
5. [Road Navigation System](#road-navigation-system)
6. [Rendering Layers](#rendering-layers)
7. [Entity and NPC Display](#entity-and-npc-display)
8. [Territory Overlay](#territory-overlay)
9. [Chunk Scan Strategies](#chunk-scan-strategies)
10. [Configuration](#configuration)
11. [Keyboard Controls](#keyboard-controls)
12. [Service Architecture](#service-architecture)
13. [Data Layer](#data-layer)
14. [Minecraft Integration (Mixins)](#minecraft-integration-mixins)
15. [Dimension Support](#dimension-support)
16. [Performance](#performance)
17. [Admin Guide](#admin-guide)
18. [Troubleshooting](#troubleshooting)

---

## Overview

The MapView System is a fully custom in-game map renderer spanning over 120 Java files. It provides players with a real-time minimap in the corner of the screen and an interactive world map accessible via the Smartphone app. The system features A* road navigation, NPC position tracking, territory overlays from the Gang System, and dimension-aware rendering with a service-oriented architecture for clean separation of concerns.

### Key Features

- **Real-Time Minimap** — Configurable size, position, and zoom level in a corner of the HUD
- **Full World Map** — Interactive map with zoom levels (0–4) accessible from the Smartphone
- **A* Road Navigation** — Automatic route calculation along detected road blocks
- **NPC Rendering** — NPCs displayed as icons on both minimap and world map
- **Territory Overlay** — Gang territory visualization from the Territory System
- **Dimension Support** — Separate map data per dimension (Overworld, Nether, End)
- **Async Rendering** — Multi-core chunk processing with Grid and Spiral scan strategies
- **Persistent Cache** — Explored regions saved to disk and loaded on reconnect
- **Configurable** — Extensive `mapview.properties` settings for display preferences
- **Mixin Integration** — Hooks into Minecraft's WorldRenderer and HUD for seamless rendering

### Statistics

| Metric | Value |
|--------|-------|
| Java source files | 122 |
| Zoom levels | 5 (0–4) |
| Navigation scan radius | 150 blocks |
| Path update interval | 1,000 ms |
| Arrival detection distance | 5 blocks |
| Path deviation threshold | 15 blocks |
| Rendering layers | 8 |
| Chunk scan strategies | 2 (Grid, Spiral) |

---

## Architecture

The MapView System uses a layered service architecture for clean separation between rendering, data management, navigation, and Minecraft integration.

```
MapDataManager (Central Coordinator)
  │
  ├── Services (Service Layer)
  │     ├── RenderCoordinationService    -- Manages rendering pipeline and frame updates
  │     ├── WorldStateService            -- Tracks world state, player position, biomes
  │     ├── LifecycleService             -- Handles join, disconnect, world change events
  │     ├── ColorCalculationService      -- Computes block/biome colors for rendering
  │     ├── DimensionService             -- Per-dimension map management
  │     └── ConfigNotificationService    -- Propagates config changes to services
  │
  ├── Navigation (Navigation Layer)
  │     ├── RoadNavigationService        -- Main navigation coordinator
  │     ├── RoadGraph                    -- Weighted graph with A* pathfinding
  │     ├── RoadGraphBuilder             -- Scans the world for road blocks
  │     ├── RoadBlockDetector            -- Identifies which blocks count as roads
  │     ├── NavigationOverlay            -- Renders the route on the map
  │     └── RoadPathRenderer             -- Detailed path rendering
  │
  ├── Data (Data Layer)
  │     ├── WorldMapData                 -- Persistent chunk color data
  │     ├── AsyncPersistenceManager      -- Background save/load operations
  │     └── MapDataRepository            -- Data access layer
  │
  └── Rendering (Client-Side)
        ├── MinimapRenderer              -- HUD minimap rendering
        ├── WorldMapRenderer             -- Full-screen world map rendering
        ├── NPCMapRenderer               -- NPC icon overlay
        └── TerritoryMapRenderer         -- Territory color overlay
```

---

## Minimap

The minimap is a real-time top-down view of the player's surroundings, rendered as a HUD element.

### Minimap Features

- **Terrain visualization** — Block colors reflect actual block types, biomes, height, and lighting
- **Height shading** — Elevation is visualized through brightness variation
- **NPC icons** — NPCs shown as small icons with type differentiation
- **Player marker** — Your position shown as a directional arrow
- **Territory overlay** — Colored zone highlights from gang territories (optional)
- **Configurable position** — Choose which screen corner (0 = top-left, 1 = top-right, 2 = bottom-left, 3 = bottom-right)
- **Configurable size** — `sizeModifier` setting from -1 (smallest) to 4 (largest)

### Minimap Default Layout

```
┌─────────────────────────────────────────┐
│ [Minimap]                               │
│  ┌───────────────────────┐              │
│  │    ░░░▓▓▓███████▓▓░   │              │
│  │    ░▓▓████████████▓░  │              │
│  │    ░███████▲████████  │  (▲ = you)   │
│  │    ░░░▓▓▓███████▓▓░   │              │
│  └───────────────────────┘              │
│                                         │
└─────────────────────────────────────────┘
```

---

## World Map

The World Map is a full-screen interactive map accessible through the Smartphone app (`Map` button).

### World Map Features

- **5 Zoom Levels** (0–4) — From city overview (0) to street-level detail (4)
- **Panning** — Click and drag to move around the world
- **Exploration tracking** — Only chunks you have visited are rendered (unexplored areas shown as gray)
- **NPC overlay** — All active NPCs shown on the map
- **Territory overlay** — Full territory map with gang color coding
- **Navigation route** — Active A* route displayed as a colored path
- **Coordinate display** — Current map center coordinates shown

### Zoom Level Guide

| Level | Scale | Best Use |
|-------|-------|----------|
| 0 | Zoomed out (city overview) | Server layout overview |
| 1 | City district level | Planning routes |
| 2 | Default (street level) | Normal navigation |
| 3 | Neighborhood detail | Finding specific buildings |
| 4 | Maximum zoom | Precise block-level navigation |

---

## Road Navigation System

The navigation system uses A* pathfinding to calculate optimal routes along road blocks in the world.

### How It Works

1. **Road detection** — `RoadBlockDetector` scans the world within a 150-block radius and identifies road blocks (configurable block types)
2. **Graph construction** — `RoadGraphBuilder` builds a weighted graph of connected road nodes
3. **Route calculation** — `RoadGraph` runs A* on the graph to find the shortest path
4. **Overlay rendering** — `NavigationOverlay` draws the path on the minimap and world map
5. **Real-time updates** — The path is recalculated every 1,000 ms as you move

### Navigation Parameters

| Parameter | Value | Description |
|-----------|-------|-------------|
| Scan radius | 150 blocks | Area scanned for road blocks |
| Path update interval | 1,000 ms | How often the route recalculates |
| Movement threshold | 10 blocks | Move this far to trigger recalculation |
| Arrival distance | 5 blocks | Distance to consider destination reached |
| Path deviation threshold | 15 blocks | Off-path distance to force recalculation |

### Navigation Events

The navigation system emits events consumed by the overlay renderer:

| Event | Trigger |
|-------|---------|
| `GRAPH_UPDATED` | Road network scanned/refreshed |
| `NAVIGATION_STARTED` | Player sets a navigation destination |
| `NAVIGATION_STOPPED` | Player cancels navigation |
| `PATH_UPDATED` | Route recalculated after movement |
| `PATH_NOT_FOUND` | No navigable route exists to destination |
| `DESTINATION_REACHED` | Player arrived within 5 blocks of destination |

### Starting Navigation

Navigation is integrated into the World Map screen:
1. Open the World Map via Smartphone
2. Right-click on a destination point
3. Select "Navigate Here"
4. The route appears as a colored line on the map and minimap

---

## Rendering Layers

The renderer composes the final map image from multiple stacked layers:

| Layer | Description | Performance Impact |
|-------|-------------|-------------------|
| **Heightmap** | Elevation visualization (brightness by Y-level) | Low |
| **Lightmap** | Ambient and block lighting applied to block colors | Medium |
| **Slopemap** | Slope/incline shading for terrain contour | Low |
| **Biomes** | Biome-specific color tinting (grass, water, foliage) | Medium |
| **Water Transparency** | Semi-transparent water revealing underwater terrain | High |
| **Block Transparency** | Glass and other transparent blocks | Medium |
| **Territories** | Gang territory color overlay (optional, disabled by default) | Low |
| **World Border** | World border visualization | Negligible |

Layers are composited in order from bottom to top. Each layer can be independently toggled (see Configuration).

---

## Entity and NPC Display

### NPC Rendering

NPCs are displayed on both the minimap and world map via `NPCMapRenderer`:

- **Position** — Updated in real-time as NPCs move
- **Icon differentiation** — Different icons for Resident, Merchant, and Police NPCs
- **Visibility range** — NPCs within the rendered map area are shown
- **NPC names** — Names displayed when zoomed in (World Map only)

### Player Display

On the minimap:
- **Your position** — Directional arrow showing facing direction
- **Other players** — (if server-enabled) Shown as dots

---

## Territory Overlay

When `showTerritories=true`, the map renders a color overlay corresponding to gang territory data from the [Territory System](Territory-System.md).

- **Color coding** — Each territory uses the assigned gang color (Red, Green, Blue, etc.)
- **Opacity** — Semi-transparent to allow terrain to show through
- **Real-time updates** — Territory changes sync via `SyncTerritoryDeltaPacket` and update the map overlay
- **Hover info** — Hovering over a territory chunk on the World Map shows gang name and territory name

---

## Chunk Scan Strategies

When processing chunks for map rendering, the system uses one of two scan patterns:

| Strategy | Pattern | Best For |
|----------|---------|----------|
| **Grid Scan** | Systematic row-by-row raster | Initial full-world scans |
| **Spiral Scan** | Outward spiral from player position | Prioritizing nearby chunks |

A factory automatically selects the optimal strategy based on the scanning context (initial load vs. update). Both strategies support multi-core processing with configurable thread count.

---

## Configuration

**File:** `config/mapview.properties`

### Display Settings

| Key | Default | Options | Description |
|-----|---------|---------|-------------|
| `showUnderMenus` | `false` | `true`/`false` | Show minimap under inventory/menus |
| `zoom` | `2` | `0`–`4` | Default zoom level |
| `sizeModifier` | `1` | `-1`–`4` | Minimap size modifier |
| `mapCorner` | `1` | `0`–`3` | Screen corner (0=TL, 1=TR, 2=BL, 3=BR) |
| `oldNorth` | `false` | `true`/`false` | Use classic north orientation |
| `showTerritories` | `false` | `true`/`false` | Enable territory color overlay |

### Feature Flags

| Key | Default | Description |
|-----|---------|-------------|
| `worldmapAllowed` | `true` | Enable the full world map screen |
| `minimapAllowed` | `true` | Enable the minimap HUD element |
| `multicore` | System default | Enable multi-core chunk processing |

### Hot-Reload

Configuration changes in `mapview.properties` are applied on the next map render cycle. No restart required. Changes propagate via `ConfigNotificationService` to all active rendering services.

---

## Keyboard Controls

| Key | Action |
|-----|--------|
| `Z` | Cycle through zoom levels |
| `X` | Toggle fullscreen map / minimap mode |
| `M` | Open map menu (settings, navigation) |

Keybindings can be remapped in Minecraft's standard keybinding settings (Options → Controls).

---

## Service Architecture

### Service Lifecycle

All services are managed by `MapDataManager`:

```
Server Login
     │
     ▼
LifecycleService.onPlayerJoin()
     ├── Load persistent map data (WorldMapData)
     ├── Initialize dimension-specific rendering
     └── Trigger initial chunk scan (Spiral strategy)

During Play
     ├── WorldStateService monitors player position
     ├── RenderCoordinationService schedules frame updates
     ├── ColorCalculationService computes block colors
     └── RoadNavigationService updates route if navigating

Server Disconnect / Shutdown
     └── LifecycleService.onDisconnect()
           ├── Save map data to disk (AsyncPersistenceManager)
           └── Clear dimension-specific caches
```

---

## Data Layer

### WorldMapData

`WorldMapData` stores all rendered chunk data persistently:

- **Format:** Binary/NBT per dimension
- **Content:** Per-chunk color arrays (ARGB pixels)
- **Location:** `config/mapview/<dimension_key>/`
- **Size:** Varies by explored area; typical city server = 10–50 MB

### AsyncPersistenceManager

Map data is saved asynchronously to avoid server tick stalls:

- Background thread handles all disk I/O
- Writes use atomic file operations (temp file + rename) to prevent corruption
- Periodic auto-save every 5 minutes
- Full save on player disconnect

### MapDataRepository

Provides a clean data access layer above the raw file storage:
- Caches recently accessed chunks in memory (LRU)
- Lazy-loads chunk data on first access
- Handles format versioning for backwards compatibility

---

## Minecraft Integration (Mixins)

The MapView system uses Minecraft Forge Mixins to hook into the rendering pipeline:

| Mixin | Target | Purpose |
|-------|--------|---------|
| `MixinWorldRenderer` | WorldRenderer | Hook into world rendering to capture chunk updates |
| `MixinChatHud` | ChatHud | Ensure minimap renders correctly when chat is open |
| `MixinInGameHud` | InGameHud | Inject minimap rendering into the main HUD layer |

Mixins are declared in `schedulemc.mixins.json` and loaded by Forge at startup.

---

## Dimension Support

The system maintains **separate map data per dimension**:

| Dimension | Support |
|-----------|---------|
| `minecraft:overworld` | Full support |
| `minecraft:the_nether` | Full support (separate map) |
| `minecraft:the_end` | Full support (separate map) |
| Custom dimensions | Full support (auto-detected) |

`DimensionService` manages:
- Switching active map data when player changes dimension
- Cache management per dimension
- Navigation graph reset on dimension change (roads are world-specific)

---

## Performance

### Optimization Strategies

| Strategy | Benefit |
|----------|---------|
| Multi-core chunk scanning | Faster initial map population |
| LRU cache for chunk data | Reduces repeated disk reads |
| Delta-only updates | Only re-renders changed chunks |
| Async persistence | No impact on server tick |
| Spiral scan priority | Nearest chunks rendered first |
| Configurable zoom | Lower zoom = fewer pixels to render |

### Performance Tips

1. **Disable water transparency** on large water maps — it's the most expensive rendering layer
2. **Use Grid scan** for initial full-world scans (more efficient for large areas)
3. **Limit minimap size** on lower-end client hardware (`sizeModifier = 0` or lower)
4. **Disable territories overlay** if not needed — it adds a compositing pass

---

## Admin Guide

### Enabling the Map for Players

Ensure the following settings in `config/mapview.properties`:
```properties
worldmapAllowed=true
minimapAllowed=true
```

### Pre-Rendering the Map

For a new server where you want all players to see the map immediately:
1. Explore the entire world area yourself (or use a map pre-generator mod)
2. The map data will be populated as you explore
3. Map data is shared server-wide (all players see the same explored areas)

### Territory Overlay

To enable the territory overlay for all players:
```properties
showTerritories=true
```

This is disabled by default as it requires the Gang System to be active.

### Troubleshooting Rendering

Use `/health mapview` to check the health status of the MapView system components.

---

## Troubleshooting

### Minimap not showing

1. **Config check** — Ensure `minimapAllowed=true` in `config/mapview.properties`.
2. **Keybind conflict** — The default `Z`/`X`/`M` keys may conflict with other mods. Check keybindings.
3. **Client-side issue** — The minimap renders client-side. Ensure the mod JAR is in the client's `mods/` folder.

### Map showing gray/unexplored areas

1. **Not explored yet** — The map only shows chunks you or other players have visited. This is expected behavior.
2. **Cache not loading** — Check `config/mapview/` folder exists and has map data files. If missing, the area simply hasn't been explored yet.

### Navigation not finding a path

1. **No roads scanned** — The road graph requires road-type blocks within 150 blocks of the player and destination.
2. **`PATH_NOT_FOUND` event** — The system logs this when no route exists. Check if there is a connected road network between source and destination.
3. **Different dimension** — Navigation only works within a single dimension.

### Performance issues (low FPS)

1. **Reduce minimap size** — Lower `sizeModifier` to reduce rendering resolution.
2. **Disable water transparency** — Set `showUnderMenus=false` and check if a transparent-water layer can be toggled.
3. **Disable multicore** — On some systems, the overhead of thread management for chunk scanning can be worse than single-threaded. Set `multicore=false`.
4. **Reduce zoom** — Higher zoom levels render more detail. Switch to zoom level 0 or 1 for the minimap.

### Map data corruption

1. **Backup recovery** — Map data is backed up periodically. Find backups in `config/backups/mapview/`.
2. **Delete and re-explore** — Delete the `config/mapview/` folder to reset all map data. Players will need to re-explore.
3. **Atomic writes** — The system uses atomic file writes to prevent corruption during saves. If corruption occurs despite this, investigate disk health.
