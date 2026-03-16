# Player Settings & Tracker System

**Navigation:** [Home](../Home.md) | [Features Overview](../Home.md#feature-systems) | [Utility System](Utility-System.md) | [Smartphone System](Smartphone-System.md)

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [PlayerSettings](#playersettings)
4. [PlayerSettingsManager](#playersettingsmanager)
5. [PlayerTracker](#playertracker)
6. [ServiceContact](#servicecontact)
7. [Network Layer](#network-layer)
8. [Persistence](#persistence)
9. [Integration with Other Systems](#integration-with-other-systems)
10. [Troubleshooting](#troubleshooting)

---

## Overview

The **Player Settings & Tracker System** manages two related responsibilities:

1. **Player Settings** — Per-player configurable preferences (currently utility warning thresholds) stored server-side and synced to the client
2. **Player Tracker** — Persistent registry of all players who have ever joined the server, used by the Contacts App to show available contacts

**Package:** `de.rolandsw.schedulemc.player`

**Key features:**
- Thread-safe `ConcurrentHashMap` for all player data
- Lazy save (dirty flag) — only writes to disk when data changes
- Incremental saves via scheduled server task (`saveIfNeeded`)
- Separate persistence for settings vs. contacts
- `ServiceContact` system for built-in game services (Towing, Taxi, etc.)
- Network sync on login and on change

---

## Architecture

```
player/
├── PlayerSettings.java              ← Per-player settings POJO
├── PlayerSettingsManager.java       ← Static manager: load/save, get/set settings
├── PlayerTracker.java               ← Static manager: tracks all joined players
├── ServiceContact.java              ← Service contact POJO + ServiceType enum
│
└── network/
    ├── PlayerSettingsNetworkHandler.java  ← Channel registration
    ├── PlayerSettingsPacket.java          ← Client → Server: update settings
    ├── SyncPlayerSettingsPacket.java      ← Server → Client: sync settings
    └── ClientPlayerSettings.java          ← Client-side settings cache
```

**Data flow:**

```
Server startup:
  → PlayerSettingsManager.load() from world/data/player_settings.json
  → PlayerTracker.load() from config/plotmod_player_contacts.json

Player joins:
  → PlayerTracker.registerPlayer(uuid, name)
  → SyncPlayerSettingsPacket sent to player
    → ClientPlayerSettings.update(...)

Player changes setting (future GUI):
  → PlayerSettingsPacket sent to server
    → PlayerSettingsManager.updateSettings(uuid, settings)
    → SyncPlayerSettingsPacket sent back to confirm
    → needsSave = true

Periodic tick (IncrementalSaveManager):
  → PlayerSettingsManager.saveIfNeeded()
  → PlayerTracker.saveIfNeeded()
```

---

## PlayerSettings

**File:** `PlayerSettings.java`

Simple POJO holding a player's configurable preferences. Serialized to JSON via Gson.

**Fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `playerUUID` | `String` | (set on creation) | UUID as string for JSON serialization |
| `utilityWarningsEnabled` | `boolean` | `true` | Whether to show utility consumption warnings |
| `electricityWarningThreshold` | `double` | `100.0` | kWh/day before warning is shown |
| `waterWarningThreshold` | `double` | `500.0` | L/day before warning is shown |

**Getters & setters:**

```java
PlayerSettings settings = PlayerSettingsManager.getSettings(playerUUID);

// Check/set utility warnings:
boolean enabled = settings.isUtilityWarningsEnabled();
settings.setUtilityWarningsEnabled(false);

// Check/set thresholds:
double elecThreshold = settings.getElectricityWarningThreshold();
settings.setElectricityWarningThreshold(150.0);

double waterThreshold = settings.getWaterWarningThreshold();
settings.setWaterWarningThreshold(750.0);
```

---

## PlayerSettingsManager

**File:** `PlayerSettingsManager.java`

Static manager class with thread-safe `ConcurrentHashMap` storage. Does not follow the singleton pattern — all methods are static.

**Storage location:** `world/data/player_settings.json`

**Methods:**

| Method | Description |
|--------|-------------|
| `load()` | Load all settings from JSON file |
| `save()` | Force save all settings to JSON file |
| `saveIfNeeded()` | Save only if `needsSave` flag is true |
| `getSettings(uuid)` | Get settings for player (creates default if missing) |
| `updateSettings(uuid, settings)` | Replace full settings object, mark dirty |
| `setUtilityWarningsEnabled(uuid, enabled)` | Update single field, mark dirty |
| `setElectricityThreshold(uuid, threshold)` | Update single field, mark dirty |
| `setWaterThreshold(uuid, threshold)` | Update single field, mark dirty |

**Usage example (server-side):**

```java
// Get a player's settings:
UUID playerUUID = player.getUUID();
PlayerSettings settings = PlayerSettingsManager.getSettings(playerUUID);

// Check if warnings are enabled:
if (settings.isUtilityWarningsEnabled()) {
    double usage = plotData.getCurrentElectricity();
    if (usage > settings.getElectricityWarningThreshold()) {
        player.sendSystemMessage(Component.literal("§e⚡ Warnung: Hoher Stromverbrauch!"));
    }
}

// Update a setting:
PlayerSettingsManager.setUtilityWarningsEnabled(playerUUID, false);
// needsSave is now true; will be saved on next saveIfNeeded() call
```

**JSON format:**

```json
{
  "550e8400-e29b-41d4-a716-446655440000": {
    "playerUUID": "550e8400-e29b-41d4-a716-446655440000",
    "utilityWarningsEnabled": true,
    "electricityWarningThreshold": 100.0,
    "waterWarningThreshold": 500.0
  },
  "6ba7b810-9dad-11d1-80b4-00c04fd430c8": {
    "playerUUID": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "utilityWarningsEnabled": false,
    "electricityWarningThreshold": 200.0,
    "waterWarningThreshold": 1000.0
  }
}
```

---

## PlayerTracker

**File:** `PlayerTracker.java`

Tracks all players who have joined the server. Primary use: powering the **Contacts App** in the Smartphone System, which needs a list of all players to display as potential contacts.

**Storage location:** `config/plotmod_player_contacts.json`

**Inner class:** `PlayerContact`

| Field | Type | Description |
|-------|------|-------------|
| `uuid` | `UUID` | Player's UUID (immutable) |
| `name` | `String` | Latest known in-game name |
| `lastSeen` | `long` | `System.currentTimeMillis()` at last join |

**Name updates:** If a player's name changes (e.g., username change), the tracker automatically updates it on the next login.

**`lastSeen` optimization:** Only marks dirty if more than **5 minutes** have passed since the last update, reducing disk I/O for frequent logins.

**Key methods:**

| Method | Return | Description |
|--------|--------|-------------|
| `registerPlayer(uuid, name)` | `void` | Called on PlayerJoinEvent |
| `getAllContacts()` | `List<PlayerContact>` | Alphabetically sorted |
| `getContact(uuid)` | `PlayerContact` | Get specific player |
| `getServiceContacts()` | `List<ServiceContact>` | Built-in services |
| `load()` | `void` | Load from JSON |
| `save()` | `void` | Force save to JSON |
| `saveIfNeeded()` | `void` | Save if dirty |

**Usage example:**

```java
// When building the contacts list for the Contacts App:
List<PlayerTracker.PlayerContact> players = PlayerTracker.getAllContacts();
List<ServiceContact> services = PlayerTracker.getServiceContacts();

// Display in GUI:
for (PlayerTracker.PlayerContact contact : players) {
    String name = contact.getName();
    UUID uuid = contact.getUuid();
    long lastSeen = contact.getLastSeen();
    // ... render contact entry
}
```

**Data validation on load:**
- Null/empty UUID strings are skipped with a warning
- Null/empty names default to `"Unknown Player"`
- Names longer than 100 characters are truncated
- Negative `lastSeen` values are reset to 0
- Map size > 10,000 triggers a warning log

**JSON format:**
```json
{
  "550e8400-e29b-41d4-a716-446655440000": {
    "name": "PlayerOne",
    "lastSeen": 1710000000000
  },
  "6ba7b810-9dad-11d1-80b4-00c04fd430c8": {
    "name": "PlayerTwo",
    "lastSeen": 1710050000000
  }
}
```

---

## ServiceContact

**File:** `ServiceContact.java`

Represents a built-in game service that appears in the Contacts App alongside player contacts. Unlike player contacts, services are hardcoded (not loaded from file).

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `serviceId` | `String` | Unique identifier (e.g., `"towing_service"`) |
| `name` | `String` | Display name (e.g., `"Pannenhilfe ADAC"`) |
| `type` | `ServiceType` | Category (TOWING, TAXI, EMERGENCY, DELIVERY) |

**ServiceType enum:**

| Value | Icon | Description |
|-------|------|-------------|
| `TOWING` | 🔧 | Vehicle towing service (Pannenhilfe) |
| `TAXI` | 🚕 | Taxi service |
| `EMERGENCY` | 🚑 | Emergency services |
| `DELIVERY` | 📦 | Delivery service |

**Translation key:** `service.contact.{serviceId}.name` (for i18n)

**Currently registered services:**

| Service ID | Name | Type |
|-----------|------|------|
| `towing_service` | Pannenhilfe ADAC | TOWING |

*Additional services (TAXI, EMERGENCY, DELIVERY) are planned for future versions.*

---

## Network Layer

**Channel:** Uses the main ScheduleMC network channel (not a dedicated channel like missions)

### `SyncPlayerSettingsPacket` (Server → Client)

Sends current settings to the client. Triggered:
- On player join
- After any `PlayerSettingsManager.updateSettings()` call

**Packet fields:**

| Field | Type | Size |
|-------|------|------|
| `utilityWarningsEnabled` | `boolean` | 1 byte |
| `electricityThreshold` | `double` | 8 bytes |
| `waterThreshold` | `double` | 8 bytes |

**Client handler:** `ClientPlayerSettings.update(enabled, elecThreshold, waterThreshold)`
- Uses atomic update to avoid race conditions with rapid successive packets
- Updates static fields in `ClientPlayerSettings` for GUI access

### `PlayerSettingsPacket` (Client → Server)

Sent when the player changes a setting via GUI.

---

## Persistence

### PlayerSettingsManager

- **File:** `world/data/player_settings.json`
- **Format:** JSON object: `{uuid_string: PlayerSettings}`
- **Save strategy:** Lazy (dirty flag), typically saved by `IncrementalSaveManager`
- **Load:** On server start, before any player joins

### PlayerTracker

- **File:** `config/plotmod_player_contacts.json`
- **Format:** JSON object: `{uuid_string: {name, lastSeen}}`
- **Save strategy:** Lazy (dirty flag) via `AbstractPersistenceManager`
- **Load:** On server start

**File location note:** Settings use `world/data/` (server-world-relative) while contacts use `config/` (server-install-relative). This means contacts persist across world resets but settings do not.

---

## Integration with Other Systems

### Utility System

The most direct consumer of `PlayerSettings`. The `UtilityEventHandler` reads each player's settings to determine when to show utility consumption warnings:

```java
// In UtilityEventHandler (PlayerTickEvent):
PlayerSettings settings = PlayerSettingsManager.getSettings(player.getUUID());
if (settings.isUtilityWarningsEnabled()) {
    PlotUtilityData data = getCurrentPlotData(player);
    if (data.getCurrentElectricity() > settings.getElectricityWarningThreshold()) {
        // Show warning to player
    }
}
```

### Smartphone / Contacts App

The Contacts App uses `PlayerTracker` to populate the contacts list:

```java
// When opening contacts:
List<PlayerTracker.PlayerContact> contacts = PlayerTracker.getAllContacts();
List<ServiceContact> services = PlayerTracker.getServiceContacts();
// Combine and render
```

### Towing System

`ServiceContact.ServiceType.TOWING` is used by the Towing System to register itself as a callable service in the Contacts App.

### Police System

Future integration: Police officers may appear as `ServiceContact.ServiceType.EMERGENCY` entries.

---

## Troubleshooting

| Problem | Cause | Solution |
|---------|-------|----------|
| Settings not persisting between restarts | `saveIfNeeded()` not called on shutdown | Ensure server shutdown hook calls `PlayerSettingsManager.save()` |
| Player not in contacts list | Player hasn't joined since `PlayerTracker` was initialized | Have the player join the server once |
| Settings reset to defaults | `world/data/player_settings.json` was deleted (e.g., world reset) | Settings are per-world; expected behavior |
| Contacts list shows "Unknown Player" | Player's name was null/empty in saved data | Data was auto-corrected; will update on next login |
| Warning thresholds not working | Client cache stale | Relog to trigger `SyncPlayerSettingsPacket` on join |
| `getServiceContacts()` returning empty | `PlayerTracker.getServiceContacts()` is hardcoded | Services are not loaded from file; check the method directly |

---

*Part of the ScheduleMC v3.6.9-beta documentation. For API integration, see the [API Reference](../../docs/API_REFERENCE.md).*
