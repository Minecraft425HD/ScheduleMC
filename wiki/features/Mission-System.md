# Mission System

**Navigation:** [Home](../Home.md) | [Features Overview](../Home.md#feature-systems) | [Achievement System](Achievement-System.md) | [Level System](Level-System.md)

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Core Components](#core-components)
   - [MissionDefinition](#missiondefinition)
   - [MissionRegistry](#missionregistry)
   - [MissionCategory](#missioncategory)
   - [MissionStatus](#missionstatus)
   - [PlayerMission](#playermission)
   - [PlayerMissionManager](#playermissionmanager)
4. [Mission Lifecycle](#mission-lifecycle)
5. [Network Layer](#network-layer)
6. [Client Cache](#client-cache)
7. [Available Missions](#available-missions)
8. [Tracking Key System](#tracking-key-system)
9. [Prerequisite System](#prerequisite-system)
10. [Persistence](#persistence)
11. [Integrating Custom Missions](#integrating-custom-missions)
12. [Troubleshooting](#troubleshooting)

---

## Overview

The **Mission System** provides a narrative mission structure with main story missions and repeatable side missions. Each player has individual mission state, tracked server-side and synced to the client on demand. Missions reward XP and in-game currency on completion.

**Package:** `de.rolandsw.schedulemc.mission`

**Key features:**
- Two categories: **Hauptmissionen** (main story) and **Nebenmissionen** (side missions)
- Four-state lifecycle: AVAILABLE → ACTIVE → COMPLETED → CLAIMED
- Prerequisite chain support for story progression
- NPC giver integration (optional UUID + name)
- Synchronized to client via dedicated network channel
- Thread-safe via volatile fields + synchronized blocks
- Persisted to `schedulemc_missions.json` with atomic writes and backup

---

## Architecture

```
mission/
├── MissionDefinition.java         ← Static template: id, title, rewards, prerequisites
├── MissionRegistry.java           ← Static registry of all available missions
├── MissionCategory.java           ← Enum: HAUPT, NEBEN
├── MissionStatus.java             ← Enum: AVAILABLE, ACTIVE, COMPLETED, CLAIMED
├── PlayerMission.java             ← Per-player instance with progress + state
├── PlayerMissionManager.java      ← Singleton manager: accept, abandon, claim, track
│
├── client/
│   ├── ClientMissionCache.java    ← Client-side mission state cache
│   └── PlayerMissionDto.java      ← Data transfer object for client rendering
│
└── network/
    ├── MissionNetworkHandler.java  ← SimpleChannel registration
    ├── RequestMissionsPacket.java  ← Client → Server: request sync
    ├── SyncMissionsPacket.java     ← Server → Client: mission list
    └── MissionActionPacket.java    ← Client → Server: accept/abandon/claim
```

**Data flow:**

```
Player joins server
  → PlayerMissionManager loads missions from JSON
  → MissionNetworkHandler.sendToPlayer(SyncMissionsPacket)
    → ClientMissionCache.update(dtos)

Player accepts mission (via GUI):
  → Client sends MissionActionPacket(action=ACCEPT, missionId)
    → PlayerMissionManager.acceptMission(player, definitionId)
      → validates prerequisites, creates PlayerMission
      → MissionNetworkHandler.sendToPlayer(SyncMissionsPacket) [re-sync]

In-game event fires (e.g., player delivers package):
  → PlayerMissionManager.trackProgress(player, "package_delivered", 1)
    → for each ACTIVE mission with matching trackingKey: addProgress(1)
    → if completed: player receives chat notification
    → syncToPlayer() sends updated SyncMissionsPacket
```

---

## Core Components

### MissionDefinition

**File:** `MissionDefinition.java`

Static, immutable mission template. Created once at startup and stored in `MissionRegistry`.

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique mission identifier (e.g., `"haupt_lieferung_01"`) |
| `title` | `String` | Display title |
| `description` | `String` | Mission description text |
| `category` | `MissionCategory` | HAUPT or NEBEN |
| `xpReward` | `int` | XP granted on claim |
| `moneyReward` | `int` | Money (currency) granted on claim |
| `targetAmount` | `int` | Progress target (e.g., 5 packages) |
| `trackingKey` | `String` | Key used to match `trackProgress()` calls |
| `prerequisiteIds` | `List<String>` | Mission IDs that must be CLAIMED first |
| `npcGiverUUID` | `UUID?` | Optional NPC that gives this mission |
| `npcGiverName` | `String` | Display name of the NPC giver |

**Constructors:**

```java
// Full constructor:
new MissionDefinition(
    "haupt_grossauftrag",
    "Der große Auftrag",
    "Schließe 10 erfolgreiche Transaktionen ab.",
    MissionCategory.HAUPT,
    2000,   // XP reward
    15000,  // Money reward
    10,     // Target amount
    "transaction_completed",  // Tracking key
    List.of("haupt_lieferung_01"),  // Prerequisites
    null,   // NPC UUID
    ""      // NPC name
);

// Simple constructor (no NPC, no prerequisites):
new MissionDefinition(
    "neben_fahrzeuge_01", "Stadtfahrer", "Fahre 10 km...",
    MissionCategory.NEBEN, 200, 500, 10, "km_driven"
);
```

---

### MissionRegistry

**File:** `MissionRegistry.java`

Static, `LinkedHashMap`-backed registry of all available mission definitions. Populated via static initializer at class load time.

**Methods:**

| Method | Return | Description |
|--------|--------|-------------|
| `getById(id)` | `MissionDefinition` | Get definition by ID |
| `getAll()` | `Collection<MissionDefinition>` | All definitions (unmodifiable) |
| `getByCategory(cat)` | `List<MissionDefinition>` | Filter by HAUPT or NEBEN |
| `exists(id)` | `boolean` | Check if ID is registered |

---

### MissionCategory

**File:** `MissionCategory.java`

```java
public enum MissionCategory {
    HAUPT("Hauptmissionen", "gui.missions.tab_haupt"),
    NEBEN("Nebenmissionen", "gui.missions.tab_neben");
}
```

| Value | Display Name | GUI Tab Key |
|-------|-------------|-------------|
| `HAUPT` | Hauptmissionen | `gui.missions.tab_haupt` |
| `NEBEN` | Nebenmissionen | `gui.missions.tab_neben` |

---

### MissionStatus

**File:** `MissionStatus.java`

```java
public enum MissionStatus {
    AVAILABLE,   // Visible but not yet accepted
    ACTIVE,      // Accepted, progress tracking active
    COMPLETED,   // Target reached, reward not yet collected
    CLAIMED      // Reward collected — mission done
}
```

| Status | Description | Transitions to |
|--------|-------------|----------------|
| `AVAILABLE` | Mission can be accepted | ACTIVE (via `acceptMission`) |
| `ACTIVE` | In progress, `trackProgress` applies | COMPLETED (auto, when target reached) |
| `COMPLETED` | Done, reward pending | CLAIMED (via `claimMission`) |
| `CLAIMED` | Fully finished | — (terminal state) |

Note: AVAILABLE is not stored server-side — all definitions not yet in a player's list are implicitly available.

---

### PlayerMission

**File:** `PlayerMission.java`

Per-player instance of a mission. Thread-safe via `volatile` fields and `synchronized` blocks on compound operations.

**Key fields:**

| Field | Type | Notes |
|-------|------|-------|
| `missionId` | `String` | Unique instance ID: `"pm_{uuid8}_{definitionId}"` |
| `definitionId` | `String` | References `MissionDefinition` in registry |
| `playerUUID` | `UUID` | Owner |
| `currentProgress` | `volatile int` | Current progress count |
| `status` | `volatile MissionStatus` | Current status |
| `acceptedAt` | `long` | System.currentTimeMillis() at acceptance |
| `completedAt` | `volatile long` | Set when status → COMPLETED |
| `claimedAt` | `volatile long` | Set when status → CLAIMED |

**Key methods:**

```java
// Increment progress (for counted events):
boolean completed = mission.addProgress(1);
// Returns true if this call completed the mission

// Set absolute progress (for threshold events):
boolean completed = mission.setProgress(newValue);

// Claim reward:
boolean success = mission.claim();
// Returns true if was COMPLETED, transitions to CLAIMED

// Utility:
double pct = mission.getProgressPercent();  // 0.0 to 1.0
boolean canClaim = mission.isClaimable();    // status == COMPLETED
```

---

### PlayerMissionManager

**File:** `PlayerMissionManager.java`

Singleton (double-checked locking) managing all player missions. Handles acceptance, abandonment, reward claiming, progress tracking, and persistence.

**Initialization:**
```java
// On server start:
PlayerMissionManager manager = PlayerMissionManager.initialize(configDir);

// On server stop:
PlayerMissionManager.resetInstance();  // saves and nulls the instance
```

**Core methods:**

| Method | Return | Description |
|--------|--------|-------------|
| `getPlayerMissions(uuid)` | `List<PlayerMission>` | All missions for a player |
| `acceptMission(player, definitionId)` | `boolean` | Accept if not already active and prerequisites met |
| `abandonMission(player, missionId)` | `boolean` | Remove if ACTIVE |
| `claimMission(player, missionId)` | `boolean` | Award XP + money if COMPLETED |
| `trackProgress(player, key, amount)` | `void` | Increment all ACTIVE missions matching the key |
| `syncToPlayer(player)` | `void` | Send `SyncMissionsPacket` with current state |
| `save()` | `void` | Save all data to JSON |

---

## Mission Lifecycle

```
     Available
     (in MissionRegistry, not yet in player list)
          │
          │ acceptMission(player, definitionId)
          │ - checks: not already ACTIVE/COMPLETED
          │ - checks: prerequisites are all CLAIMED
          ▼
       ACTIVE
     (PlayerMission created, progress = 0)
          │
          │ trackProgress(player, trackingKey, amount)
          │ - addProgress(amount) on all matching missions
          ▼
     COMPLETED
     (progress >= targetAmount, completedAt set)
     (player receives chat notification)
          │
          │ claimMission(player, missionId)
          │ - player.giveExperiencePoints(xpReward)
          │ - EconomyManager.deposit(uuid, moneyReward)
          │ - claimedAt set
          ▼
      CLAIMED
     (terminal, stays in player list for history)
```

**Abandon path:** ACTIVE → removed from list (no history kept)

---

## Network Layer

**Channel:** `schedulemc:mission_network` (protocol version `"1"`)

| Packet | Direction | Purpose |
|--------|-----------|---------|
| `RequestMissionsPacket` | Client → Server | Request a fresh mission sync (e.g., on GUI open) |
| `SyncMissionsPacket` | Server → Client | Send full mission list as `List<PlayerMissionDto>` |
| `MissionActionPacket` | Client → Server | Perform action: ACCEPT, ABANDON, or CLAIM |

**Trigger points for `SyncMissionsPacket`:**
- Player joins server (after login)
- After `acceptMission()` succeeds
- After `abandonMission()` succeeds
- After `claimMission()` succeeds
- After `trackProgress()` changes any mission (progress update or completion)
- In response to `RequestMissionsPacket`

---

## Client Cache

**File:** `client/ClientMissionCache.java`

Client-side storage of the last received `List<PlayerMissionDto>`. Used by the GUI renderer to display missions without server round-trips.

**`PlayerMissionDto` fields:**

| Field | Type | Description |
|-------|------|-------------|
| `missionId` | `String` | Instance ID |
| `definitionId` | `String` | Definition ID |
| `title` | `String` | Display title |
| `description` | `String` | Description text |
| `category` | `MissionCategory` | HAUPT / NEBEN |
| `status` | `MissionStatus` | Current status |
| `currentProgress` | `int` | Current count |
| `targetAmount` | `int` | Target count |
| `xpReward` | `int` | XP on claim |
| `moneyReward` | `int` | Money on claim |
| `npcGiverName` | `String` | NPC name (if any) |

---

## Available Missions

### Hauptmissionen (Story Missions)

| ID | Title | Target | Tracking Key | Reward (XP / $) | Prerequisites |
|----|-------|--------|-------------|-----------------|---------------|
| `haupt_erster_kontakt` | Erster Kontakt | 1 | `npc_interaction_dealer` | 500 XP / $2,000 | — |
| `haupt_lieferung_01` | Die erste Lieferung | 5 | `package_delivered` | 800 XP / $5,000 | — |
| `haupt_territorium` | Territorium sichern | 3 | `territory_captured` | 1,200 XP / $8,000 | — |
| `haupt_grossauftrag` | Der große Auftrag | 10 | `transaction_completed` | 2,000 XP / $15,000 | `haupt_lieferung_01` |

### Nebenmissionen (Side Missions)

| ID | Title | Target | Tracking Key | Reward (XP / $) |
|----|-------|--------|-------------|-----------------|
| `neben_fahrzeuge_01` | Stadtfahrer | 10 | `km_driven` | 200 XP / $500 |
| `neben_handel_01` | Kleinhändler | 20 | `item_sold_to_npc` | 300 XP / $800 |
| `neben_erkunder` | Stadterkunder | 5 | `district_visited` | 150 XP / $400 |
| `neben_banker` | Sparsamer Bürger | 3 | `bank_deposit` | 250 XP / $600 |

---

## Tracking Key System

The tracking key is a string that connects game events to mission progress. Any system can call `trackProgress()` with a matching key to advance missions.

**Known tracking keys:**

| Key | Triggered by |
|-----|-------------|
| `npc_interaction_dealer` | Interacting with a dealer NPC |
| `package_delivered` | Delivering a package to a location |
| `territory_captured` | Claiming a territory in the Territory System |
| `transaction_completed` | Completing an NPC or market transaction |
| `km_driven` | Driving distance (per km in a vehicle) |
| `item_sold_to_npc` | Selling an item to any NPC |
| `district_visited` | Entering a new city district |
| `bank_deposit` | Depositing money at the bank |

**How to fire a tracking event:**

```java
// In any game event handler (server-side):
ServerPlayer player = ...;
PlayerMissionManager manager = PlayerMissionManager.getInstance();
if (manager != null) {
    manager.trackProgress(player, "package_delivered", 1);
}
```

The manager will automatically:
1. Find all of the player's ACTIVE missions with matching `trackingKey`
2. Call `mission.addProgress(amount)` on each
3. Send chat message if any mission completes
4. Sync updated state to client

---

## Prerequisite System

Missions can require other missions to be CLAIMED before they become acceptable:

```java
// haupt_grossauftrag requires haupt_lieferung_01 to be CLAIMED:
new MissionDefinition(
    "haupt_grossauftrag", ...,
    List.of("haupt_lieferung_01"),  // prerequisiteIds
    null, ""
);
```

**Prerequisite check in `acceptMission()`:**
```java
for (String prereqId : def.getPrerequisiteIds()) {
    boolean fulfilled = missions.stream().anyMatch(
        m -> m.getDefinitionId().equals(prereqId)
          && m.getStatus() == MissionStatus.CLAIMED
    );
    if (!fulfilled) return false;  // block acceptance
}
```

Prerequisites are only checked at acceptance time — if a player has all requirements CLAIMED, they can accept the mission.

---

## Persistence

**Save file:** `schedulemc_missions.json` (in the configured save directory)
**Backup file:** `schedulemc_missions.json.bak`

**Save strategy:**
1. Serialize all player missions to a temp file (`.tmp`)
2. Copy current file to `.bak`
3. Atomically move `.tmp` → main file (`ATOMIC_MOVE` flag)

**Load strategy:**
1. Try to load the main file
2. On `IOException`: try to restore from `.bak`
3. On second failure: log error, start empty

**JSON structure:**
```json
{
  "550e8400-e29b-41d4-a716-446655440000": [
    {
      "missionId": "pm_550e8400_haupt_lieferung_01",
      "definitionId": "haupt_lieferung_01",
      "currentProgress": 3,
      "status": "ACTIVE",
      "acceptedAt": 1710000000000,
      "completedAt": 0,
      "claimedAt": 0
    }
  ]
}
```

**Note:** If a `definitionId` no longer exists in `MissionRegistry` when loading, the entry is skipped with a warning log. This prevents crashes when missions are removed.

---

## Integrating Custom Missions

### Step 1: Add a Mission Definition

Add to the static initializer in `MissionRegistry.java`:

```java
register(new MissionDefinition(
    "neben_meine_mission",           // unique ID
    "Meine Mission",                 // title
    "Erfülle die Aufgabe...",        // description
    MissionCategory.NEBEN,
    500,                             // XP reward
    1000,                            // money reward
    5,                               // target amount (5 completions)
    "my_custom_tracking_key"         // tracking key
));
```

### Step 2: Fire Progress Events

In the relevant game event handler:

```java
// When the player-relevant action occurs (server-side):
PlayerMissionManager manager = PlayerMissionManager.getInstance();
if (manager != null) {
    manager.trackProgress(serverPlayer, "my_custom_tracking_key", 1);
}
```

### Step 3: (Optional) Link to an NPC

```java
register(new MissionDefinition(
    "neben_npc_mission", "NPC Aufgabe", "...",
    MissionCategory.NEBEN, 400, 800, 3, "npc_task_done",
    Collections.emptyList(),
    UUID.fromString("your-npc-uuid"),
    "Händler Max"
));
```

---

## Troubleshooting

| Problem | Cause | Solution |
|---------|-------|----------|
| Mission not appearing in GUI | Not synced to client yet | Open GUI → `RequestMissionsPacket` fires automatically |
| Can't accept mission | Prerequisites not CLAIMED | Check prerequisite IDs; verify they are CLAIMED for the player |
| Progress not tracking | Wrong tracking key | Verify the key matches exactly (case-sensitive) |
| Mission vanished after update | `definitionId` no longer in registry | Re-add the definition or clean old save entries |
| Reward not received | Economy system unavailable | Check `EconomyManager.deposit()` logs for errors |
| Duplicate mission entries | Mission accepted multiple times | Check that `acceptMission` returns `false` for non-CLAIMED duplicate |

---

*Part of the ScheduleMC v3.6.9-beta documentation. For API integration, see the [API Reference](../../docs/API_REFERENCE.md).*
