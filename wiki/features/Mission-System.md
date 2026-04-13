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
7. [Mission Editor (Unified Editor)](#mission-editor-unified-editor)
   - [Gang-Modus](#gang-modus)
   - [Spieler-Modus](#spieler-modus)
   - [Story-Blöcke](#story-blöcke)
   - [Tracking-Keys Dropdown](#tracking-keys-dropdown)
   - [Workflow](#workflow)
8. [Available Missions](#available-missions)
9. [Tracking Key System](#tracking-key-system)
10. [Prerequisite System](#prerequisite-system)
11. [Persistence](#persistence)
12. [Integrating Custom Missions](#integrating-custom-missions)
13. [Troubleshooting](#troubleshooting)

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
- **Missions are fully editable in-game** via the unified Mission Editor (same Blockly-style editor used for Gang missions)

---

## Architecture

```
mission/
├── MissionDefinition.java         ← Static/dynamic template: id, title, rewards, prerequisites
├── MissionRegistry.java           ← Registry (static built-in + dynamic from editor)
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
    ├── MissionNetworkHandler.java        ← SimpleChannel registration (6 packets)
    ├── RequestMissionsPacket.java         ← Client → Server: request player mission sync
    ├── SyncMissionsPacket.java            ← Server → Client: player mission list
    ├── MissionActionPacket.java           ← Client → Server: accept/abandon/claim
    ├── RequestPlayerMissionsPacket.java   ← Client → Server: request editor scenarios
    ├── SyncPlayerMissionsPacket.java      ← Server → Client: story scenarios for editor
    └── SavePlayerMissionPacket.java       ← Client → Server: save edited story mission
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

Admin edits a mission in the editor:
  → SavePlayerMissionPacket sent to server
    → ScenarioManager.saveScenario(scenario) [persists to JSON]
    → SavePlayerMissionPacket.rebuildMissionRegistry() [reloads MissionRegistry]
      → All STORY_* scenarios converted to MissionDefinition
      → MissionRegistry.clearDynamic() + registerDynamic(def) per scenario
```

---

## Core Components

### MissionDefinition

**File:** `MissionDefinition.java`

Static or dynamically loaded mission template. Created at startup from `MissionRegistry` static block, or dynamically from the Mission Editor.

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

---

### MissionRegistry

**File:** `MissionRegistry.java`

Registry of all available mission definitions. Contains **static** (built-in, hardcoded) and **dynamic** (loaded from editor/scenarios) missions.

**Methods:**

| Method | Return | Description |
|--------|--------|-------------|
| `getById(id)` | `MissionDefinition` | Get definition by ID |
| `getAll()` | `Collection<MissionDefinition>` | All definitions (static + dynamic) |
| `getByCategory(cat)` | `List<MissionDefinition>` | Filter by HAUPT or NEBEN |
| `exists(id)` | `boolean` | Check if ID is registered |
| `registerDynamic(def)` | `void` | Register a dynamically created mission |
| `clearDynamic()` | `void` | Remove all dynamically registered missions (preserves static ones) |

**Dynamic mission flow:**
When the admin saves a story mission via the editor, `SavePlayerMissionPacket` calls `rebuildMissionRegistry()`:
1. `MissionRegistry.clearDynamic()` — removes old dynamic missions
2. For each `STORY_*` scenario in `ScenarioManager`: extract metadata from blocks → `MissionRegistry.registerDynamic(def)`

---

### MissionCategory

**File:** `MissionCategory.java`

```java
public enum MissionCategory {
    HAUPT("Hauptmissionen", "gui.missions.tab_haupt"),
    NEBEN("Nebenmissionen", "gui.missions.tab_neben");
}
```

| Value | Display Name | Maps to missionType |
|-------|-------------|---------------------|
| `HAUPT` | Hauptmissionen | `STORY_MAIN` |
| `NEBEN` | Nebenmissionen | `STORY_SIDE` |

---

### MissionStatus

**File:** `MissionStatus.java`

| Status | Description | Transitions to |
|--------|-------------|----------------|
| `AVAILABLE` | Mission can be accepted | ACTIVE (via `acceptMission`) |
| `ACTIVE` | In progress, `trackProgress` applies | COMPLETED (auto, when target reached) |
| `COMPLETED` | Done, reward pending | CLAIMED (via `claimMission`) |
| `CLAIMED` | Fully finished | — (terminal state) |

---

### PlayerMission

**File:** `PlayerMission.java`

Per-player instance of a mission. Thread-safe.

**Key methods:**

```java
boolean completed = mission.addProgress(1);   // increment counter
boolean completed = mission.setProgress(val); // set absolute progress
boolean success   = mission.claim();          // claim reward (COMPLETED → CLAIMED)
double  pct       = mission.getProgressPercent(); // 0.0 to 1.0
```

---

### PlayerMissionManager

**File:** `PlayerMissionManager.java`

Singleton managing all player missions.

| Method | Description |
|--------|-------------|
| `acceptMission(player, definitionId)` | Accept if prerequisites met |
| `abandonMission(player, missionId)` | Remove if ACTIVE |
| `claimMission(player, missionId)` | Award XP + money if COMPLETED |
| `trackProgress(player, key, amount)` | Increment matching ACTIVE missions |
| `syncToPlayer(player)` | Send `SyncMissionsPacket` |

---

## Mission Lifecycle

```
     Available (in MissionRegistry)
          │ acceptMission(player, definitionId)
          ▼
       ACTIVE (progress tracking starts)
          │ trackProgress(player, trackingKey, amount)
          ▼
     COMPLETED (target reached)
          │ claimMission(player, missionId)
          ▼
      CLAIMED (terminal, kept for history)
```

**Abandon path:** ACTIVE → removed from list (no history)

---

## Network Layer

**Channel:** `schedulemc:mission_network` (protocol version `"1"`)

| # | Packet | Direction | Purpose |
|---|--------|-----------|---------|
| 0 | `RequestMissionsPacket` | C → S | Request player mission sync (on GUI open) |
| 1 | `SyncMissionsPacket` | S → C | Send full mission list |
| 2 | `MissionActionPacket` | C → S | ACCEPT / ABANDON / CLAIM |
| 3 | `RequestPlayerMissionsPacket` | C → S | Request story scenarios for editor |
| 4 | `SyncPlayerMissionsPacket` | S → C | Send story scenarios to editor |
| 5 | `SavePlayerMissionPacket` | C → S | Save an edited story mission (OP 2+) |

---

## Client Cache

**File:** `client/ClientMissionCache.java`

Client-side storage of the last received `List<PlayerMissionDto>`. Used by the GUI renderer.

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

## Mission Editor (Unified Editor)

The **Mission Editor** (`ScenarioEditorScreen`) is a unified Blockly-style visual editor for both **Gang missions** and **Spieler-Missionen (story missions)**. It is opened server-side via `/scenario edit` (or similar OP command).

### Gang-Modus vs. Spieler-Modus

The editor has a **Tab-Toggle** in the toolbar:

```
┌─[Gang][Spieler]──[Oeffnen][Speichern][Aktive]──────────────────[Neu][Loeschen][X]┐
│ PALETTE  │            CANVAS (Drag & Drop)           │  PROPERTIES               │
│ Bausteine│   Blöcke verbinden, platzieren             │  Parameter des Blocks     │
│ (Kategorien│                                           │  (Dropdowns, Zahlen)     │
│  aufklappb.)│                                          │                          │
├──────────┴───────────────────────────────────────────┴──────────────────────────┤
│ Name: [..........] │ 4 Phasen │ ★★☆☆☆ │ [Gang:Taeglich] │ Min-Lvl: 2          │
└──────────────────────────────────────────────────────────────────────────────────┘
```

**[Gang]** — Blauer Tab: Bearbeitet Gang-Missionen (HOURLY/DAILY/WEEKLY)
- Speichern → `SaveScenarioPacket` → `ScenarioManager`
- Vorlagen-Button verfügbar

**[Spieler]** — Goldener Tab: Bearbeitet Spieler-Missionen (STORY_MAIN/STORY_SIDE)
- Beim ersten Klick: sendet `RequestPlayerMissionsPacket` → Server antwortet mit `SyncPlayerMissionsPacket`
- Speichern → `SavePlayerMissionPacket` → `ScenarioManager` + `MissionRegistry.rebuildFromScenarios()`
- Status-Bar zeigt: `[Story:Hauptmission]` / `[Story:Nebenmission]` (togglebar)

### Story-Blöcke

Im Spieler-Modus stehen zusätzlich zur normalen Palette alle Blöcke der Kategorie **"Story / Spieler-Missionen"** zur Verfügung:

| Block | Icon | Farbe | Parameter | Verwendung |
|-------|------|-------|-----------|------------|
| `MISSION_INFO` | ⓘ | Cyan | description (TEXT), npc_giver (NPC-Dropdown) | Beschreibung + NPC-Geber der Mission |
| `MISSION_PREREQ` | ⤴ | Teal | prereq_id (TEXT) | Voraussetzung: andere Mission muss abgeschlossen sein |
| `MISSION_TRACKING` | ↗ | Grün | tracking_key (Dropdown), target_amount (Zahl) | Tracking-Event definieren |
| `NPC_GIVE_MISSION` | 📋 | Hellblau | npc_name (NPC), dialog_id (TEXT) | NPC gibt dem Spieler den Auftrag |
| `NPC_COMPLETE_MISSION` | ✔ | Cyan | npc_name (NPC), dialog_id (TEXT) | Spieler gibt den Auftrag beim NPC ab |
| `MISSION_FAIL_COND` | ✕ | Rot | condition_key (Dropdown), threshold (Zahl) | Abbruchbedingung (z.B. gestorben) |
| `PLAYER_NOTIFY` | 💬 | Hellblau | text (TEXT), color (Farb-Dropdown), persistent (0/1) | Hinweis-Nachricht an den Spieler |

**Metadaten-Extraktion beim Speichern:**

Der Server liest beim Speichern folgende Block-Parameter aus dem Szenario und befüllt damit die `MissionDefinition`:

| Quelle | MissionDefinition-Feld |
|--------|----------------------|
| `MISSION_INFO.description` | `description` |
| `MISSION_INFO.npc_giver` | `npcGiverName` |
| `MISSION_TRACKING.tracking_key` | `trackingKey` |
| `MISSION_TRACKING.target_amount` | `targetAmount` |
| Alle `MISSION_PREREQ.prereq_id` | `prerequisiteIds` |
| `REWARD.xp` | `xpReward` |
| `REWARD.money` | `moneyReward` |
| `MissionScenario.name` | `title` |
| `MissionScenario.missionType` == `STORY_MAIN` | `category = HAUPT` |
| `MissionScenario.missionType` == `STORY_SIDE` | `category = NEBEN` |

### Tracking-Keys Dropdown

Der `MISSION_TRACKING`-Block bietet ein Dropdown mit allen bekannten Tracking-Keys:

```
item_collected, item_sold_to_npc, npc_interaction_dealer,
package_delivered, territory_captured, transaction_completed,
km_driven, district_visited, bank_deposit, player_died,
enemy_killed, vehicle_driven, mission_completed, money_earned,
robbery_completed, plot_visited, gang_mission_completed,
item_crafted, npc_talked, item_delivered
```

### Workflow

**Neue Spieler-Mission erstellen:**
1. Editor öffnen (OP-Befehl)
2. **[Spieler]**-Tab klicken
3. **[Neu]** — erzeugt leere Mission mit `STORY_MAIN`
4. `MISSION_INFO`-Block aus Palette ziehen → Beschreibung + NPC eintragen
5. `MISSION_TRACKING`-Block ziehen → Tracking-Key + Ziel-Menge
6. Aufgaben-Blöcke (GOTO_NPC, COLLECT_ITEMS, TALK_TO_NPC etc.) in den Canvas ziehen
7. Blöcke durch Klick auf Konnektoren verbinden: START → INFO → TRACKING → ... → REWARD
8. Typ in Status-Bar auf `Hauptmission` / `Nebenmission` setzen
9. **[Speichern]** — Mission ist sofort im Spiel verfügbar

**Beispiel-Szenario (Hauptmission "Erste Lieferung"):**
```
START ─→ MISSION_INFO (desc: "Liefere 3 Pakete", npc: "Händler Karl")
       ─→ NPC_GIVE_MISSION (npc: "Händler Karl")
       ─→ GOTO_PLOT (Ziel-Grundstück)
       ─→ MISSION_TRACKING (key: "package_delivered", amount: 3)
       ─→ NPC_COMPLETE_MISSION (npc: "Händler Karl")
       ─→ REWARD (xp: 800, money: 5000)
```

---

## Available Missions

### Hauptmissionen (eingebaut, statisch)

| ID | Title | Target | Tracking Key | Reward (XP / $) | Prerequisites |
|----|-------|--------|-------------|-----------------|---------------|
| `haupt_erster_kontakt` | Erster Kontakt | 1 | `npc_interaction_dealer` | 500 / $2,000 | — |
| `haupt_lieferung_01` | Die erste Lieferung | 5 | `package_delivered` | 800 / $5,000 | — |
| `haupt_territorium` | Territorium sichern | 3 | `territory_captured` | 1,200 / $8,000 | — |
| `haupt_grossauftrag` | Der große Auftrag | 10 | `transaction_completed` | 2,000 / $15,000 | `haupt_lieferung_01` |

### Nebenmissionen (eingebaut, statisch)

| ID | Title | Target | Tracking Key | Reward (XP / $) |
|----|-------|--------|-------------|-----------------|
| `neben_fahrzeuge_01` | Stadtfahrer | 10 | `km_driven` | 200 / $500 |
| `neben_handel_01` | Kleinhändler | 20 | `item_sold_to_npc` | 300 / $800 |
| `neben_erkunder` | Stadterkunder | 5 | `district_visited` | 150 / $400 |
| `neben_banker` | Sparsamer Bürger | 3 | `bank_deposit` | 250 / $600 |

**Dynamische Missionen** (aus dem Editor erstellt) werden zusätzlich in `MissionRegistry` registriert und gespeichert in `schedulemc_scenarios.json` (gemeinsam mit Gang-Szenarien, gefiltert nach `missionType = STORY_*`).

---

## Tracking Key System

**Bekannte Tracking-Keys:**

| Key | Ausgelöst durch |
|-----|----------------|
| `npc_interaction_dealer` | Interaktion mit Dealer-NPC |
| `package_delivered` | Paket-Lieferung |
| `territory_captured` | Gebiet übernommen |
| `transaction_completed` | NPC/Markt-Transaktion |
| `km_driven` | Gefahrene Kilometer (Vehicle System) |
| `item_sold_to_npc` | Item an NPC verkauft |
| `district_visited` | Neuen Stadtteil betreten |
| `bank_deposit` | Einzahlung bei der Bank |
| `player_died` | Spieler gestorben (für Fail-Conditions) |
| `enemy_killed` | Gegner besiegt |
| `vehicle_driven` | Fahrzeug benutzt |
| `money_earned` | Geld verdient |
| `robbery_completed` | Überfall abgeschlossen |
| `plot_visited` | Grundstück betreten |
| `gang_mission_completed` | Gang-Auftrag abgeschlossen |
| `item_crafted` | Item gecraftet |
| `npc_talked` | Mit NPC gesprochen |
| `item_delivered` | Item geliefert |

**Tracking-Event auslösen (server-seitig):**
```java
PlayerMissionManager manager = PlayerMissionManager.getInstance();
if (manager != null) {
    manager.trackProgress(serverPlayer, "package_delivered", 1);
}
```

---

## Prerequisite System

Missionen können andere Missionen als Voraussetzung haben (muss CLAIMED sein):

```java
// Statisch (MissionRegistry):
new MissionDefinition(
    "haupt_grossauftrag", ...,
    List.of("haupt_lieferung_01"),  // prerequisiteIds
    null, ""
);

// Dynamisch (Editor): MISSION_PREREQ-Block mit prereq_id = "haupt_lieferung_01"
```

---

## Persistence

**Spieler-Fortschritt:** `schedulemc_missions.json` (mit Backup `.bak`)

**Mission-Szenarien (Editor):** `schedulemc_scenarios.json` (geteilt mit Gang-Szenarien)
- Gang-Szenarien: `missionType ∈ {HOURLY, DAILY, WEEKLY}`
- Spieler-Missionen: `missionType ∈ {STORY_MAIN, STORY_SIDE}`

Beim Server-Start werden alle `STORY_*`-Szenarien automatisch in die `MissionRegistry` als dynamische Missionen geladen (via `SavePlayerMissionPacket.rebuildMissionRegistry()`).

---

## Integrating Custom Missions

### Option A: Statisch (Code)

```java
// In MissionRegistry static block:
register(new MissionDefinition(
    "neben_meine_mission", "Meine Mission", "Beschreibung...",
    MissionCategory.NEBEN, 500, 1000, 5, "my_tracking_key"
));
```

### Option B: Dynamisch (Editor, empfohlen)

1. OP-Befehl → Editor öffnen → **[Spieler]**-Tab
2. Neue Mission erstellen mit `MISSION_INFO`, `MISSION_TRACKING`, Aufgaben-Blöcken, `REWARD`
3. **[Speichern]** — wird in `ScenarioManager` persistiert und sofort in `MissionRegistry` registriert

### Option C: Progress-Event integrieren

```java
// In einem beliebigen Server-Event-Handler:
PlayerMissionManager manager = PlayerMissionManager.getInstance();
if (manager != null) {
    manager.trackProgress(serverPlayer, "my_tracking_key", 1);
}
```

---

## Troubleshooting

| Problem | Ursache | Lösung |
|---------|---------|--------|
| Mission nicht in GUI | Noch nicht sync'd | GUI öffnen → RequestMissionsPacket feuert automatisch |
| Mission nicht im Editor | Noch nicht geladen | Im Editor **[Spieler]**-Tab klicken → lädt vom Server |
| Speichern schlägt fehl | Kein OP Level 2 | Player braucht OP-Berechtigung |
| Progress wird nicht getrackt | Falscher Tracking-Key | Key exakt prüfen (case-sensitive), passt zum MISSION_TRACKING-Block |
| Mission nach Update verschwunden | `definitionId` nicht mehr in Registry | Szenario erneut speichern oder statische Definition erhalten |
| Voraussetzung nicht erfüllt | Mission nicht CLAIMED | Voraussetzungs-Mission erst abschließen |

---

*Part of the ScheduleMC documentation. For API integration, see the [API Reference](../../docs/API_REFERENCE.md).*

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-13**.
- Diese Datei wurde im Rahmen der Vollständigkeits-Aktualisierung überarbeitet.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

