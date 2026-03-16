# Lock-System (Schloss-System)

<div align="center">

**Advanced Door Security with Locks, Keys, and Hacking Tools**

5 lock types, 3 key origins, lockpicking, code entry, and alarm integration

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Lock Types](#lock-types)
4. [Key System](#key-system)
5. [Key Ring](#key-ring)
6. [Hacking Tools](#hacking-tools)
7. [Code Entry System](#code-entry-system)
8. [Lock Placement](#lock-placement)
9. [Lock Information Display](#lock-information-display)
10. [Commands Reference](#commands-reference)
11. [Integration with Police and Crime System](#integration-with-police-and-crime-system)
12. [Data Persistence](#data-persistence)
13. [Admin Guide](#admin-guide)
14. [Best Practices](#best-practices)
15. [Troubleshooting](#troubleshooting)

---

## Overview

The Lock System provides a comprehensive door security solution for ScheduleMC. Players can secure any door with one of five lock types, each offering different security levels, key management options, and resistance to hacking. The system integrates with the Police and Crime system — failed break-in attempts on high-security locks trigger alarms and raise the perpetrator's wanted level.

### Key Features

- **5 Lock Types** — From Simple (easy to pick) to Dual (combined key + code)
- **3 Key Tiers** — Copper (basic), Iron (security), Netherite (high security)
- **3 Key Origins** — Original (full strength), Copy (half strength), Stolen (quarter strength)
- **Key Ring** — Carry up to 8 keys on a single item for convenience
- **4 Hacking Tools** — Lockpick, Code Cracker, Bypass Module, Omni-Hack
- **Code Entry GUI** — 4-digit code entry for combination and dual locks
- **Daily Code Rotation** — DUAL locks automatically rotate codes daily
- **Alarm Integration** — High-security breaks trigger alarms + wanted level
- **Admin-Owned Locks** — Locks without an owner for scenario/event use

### Statistics

| Metric | Value |
|--------|-------|
| Lock types | 5 |
| Key tiers | 3 |
| Key ring capacity | 8 keys |
| Code length | 4 digits (0000–9999) |
| DUAL code rotation interval | Daily |
| Lockpick max attempts | 15 |
| Code Cracker max uses | 10 |
| Bypass Module max uses | 5 |
| Omni-Hack max uses | 3 |

---

## Architecture

```
LockManager
  ├── Lock                  -- Lock data (id, type, owner, position, dimension, code, authorized)
  ├── LockType              -- Enum: SIMPLE, SECURITY, HIGH_SECURITY, COMBINATION, DUAL
  ├── DoorLockHandler       -- Forge event handler for door interaction and protection
  └── items/
        ├── LockItems        -- Item registrations for all locks and hacking tools
        ├── KeyItem          -- Physical key item with NBT data (lock ID, tier, origin, uses)
        └── KeyRingItem      -- Key ring carrying up to 8 keys
```

### Lock Interaction Flow

```
Player right-clicks locked door
          │
          ├── Player has matching key? ──► Use key (decrement uses), open door
          │
          ├── COMBINATION or DUAL lock? ──► Open code entry GUI
          │
          ├── No valid access ──► Deny entry
          │
          └── (Optional) Player uses hacking tool:
                    │
                    ├── Check success rate for this lock type
                    ├── Success ──► Open door
                    └── Failure on HIGH_SECURITY/DUAL ──► ALARM + Wanted Level
```

---

## Lock Types

| Type | Key Required | Key Duration | Key Uses | Pickable | Pick Chance | Code | Alarm |
|------|-------------|-------------|---------|----------|-------------|------|-------|
| **SIMPLE** | Copper Key | 7 days | 100 | Yes | 80% | No | No |
| **SECURITY** | Iron Key | 3 days | 30 | Yes | 40% | No | Warning only |
| **HIGH_SECURITY** | Netherite Key | 12 hours | 10 | Yes | 10% | No | Yes (alarm + wanted level) |
| **COMBINATION** | None | — | — | No (immune) | 0% | Yes (4-digit) | No |
| **DUAL** | Netherite Key | 12 hours | 10 | Yes | 5% | Yes (rotating) | Yes (alarm + wanted level) |

### Lock Descriptions

**SIMPLE** — Basic wooden door lock. Common for residential use. Very easy to pick (80% success rate). Best for low-value properties.

**SECURITY** — Standard security lock. Picks have a 40% success rate. A failed pick triggers a warning to nearby players but no alarm. Suitable for commercial properties.

**HIGH_SECURITY** — Military-grade lock with a 10% pick chance. Failed picks immediately trigger an alarm and add to the perpetrator's wanted level. Suitable for vaults and high-value storage.

**COMBINATION** — A keyless lock with a 4-digit code. Completely immune to lockpicking (no keyhole). Only vulnerble to Code Cracker or Omni-Hack tools. No alarm on failed hacking attempts.

**DUAL** — The most secure lock type, combining both a physical Netherite key AND a 4-digit code. The code rotates automatically every day. Failed bypass attempts trigger an alarm and wanted level. Effectively immune to non-specialized hacking.

---

## Key System

### Key Blanks (Crafting)

| Blank | Tier | Compatible Locks |
|-------|------|-----------------|
| Copper Key Blank | Tier 0 | SIMPLE |
| Iron Key Blank | Tier 1 | SECURITY |
| Netherite Key Blank | Tier 2 | HIGH_SECURITY, DUAL |

### Creating a Key

1. Hold the **matching blank** in your hand
2. **Right-click on the locked door**
3. A key is automatically created with full duration and uses
4. Only the **lock owner** or an **authorized player** can create keys

### Key Properties

| Property | Description |
|----------|-------------|
| Lock ID | Identifies which lock this key opens |
| Tier | Must match the lock tier |
| Origin | Original, Copy, or Stolen (affects duration and uses) |
| Remaining Uses | Decrements on each use |
| Expiry | Timestamp after which key becomes invalid |

### Key Origins

Key origin affects how long the key lasts and how many times it can be used:

| Origin | Duration Multiplier | Uses Multiplier | Chat Tag |
|--------|---------------------|-----------------|----------|
| `ORIGINAL` | 100% | 100% | `[O]` (green) |
| `COPY` | 50% | 50% | `[K]` (yellow) |
| `STOLEN` | 25% | 25% | `[G]` (red) |

**Original keys** are created by the lock owner directly.
**Copies** are made from an Original key using an in-game duplicating mechanism.
**Stolen** keys are keys taken from NPCs or other players.

---

## Key Ring

The **Key Ring** item allows players to carry up to **8 keys** on a single item, reducing inventory clutter.

### Key Ring Usage

| Action | Result |
|--------|--------|
| Right-click on locked door | Automatically finds and uses the correct key |
| Shift + right-click in air | Shows all stored keys with origin tags |
| Shift + right-click (key in offhand) | Adds the offhand key to the ring |
| Automatic | Expired/used-up keys are automatically removed |

### Key Ring Display

When viewed (Shift + right-click), the Key Ring shows:
```
Key Ring (4/8 keys):
  [O] Iron Key - Lock #42 (30 uses left)
  [K] Copper Key - Lock #7 (12 uses left, 2d remaining)
  [G] Netherite Key - Lock #99 (8 uses left)
  [O] Iron Key - Lock #15 (30 uses left)
```

The origin tag `[O]`/`[K]`/`[G]` is color-coded: green (Original), yellow (Copy), red (Stolen).

---

## Hacking Tools

Four hacking tools provide ways to bypass locks without a key:

### 1. Lockpick (Common)

- **Durability:** 15 attempts before it breaks
- **Success rates:** 80% (Simple) / 40% (Security) / 10% (High-Security) / 0% (Combination) / 5% (Dual)
- **Alarm:** Triggered on failure against HIGH_SECURITY or DUAL locks
- **Note:** Useless against COMBINATION locks (they have no keyhole)

### 2. Code Cracker (Uncommon)

- **Durability:** 10 uses
- **Success rate:** 50% per attempt
- **Target:** COMBINATION locks only
- **Alarm:** No alarm on failure
- **Note:** Cannot open DUAL locks (requires dedicated Bypass Module or Omni-Hack)

### 3. Bypass Module (Uncommon)

- **Durability:** 5 uses
- **Success rate:** 50% per attempt
- **Target:** DUAL locks only
- **Alarm:** Alarm + wanted level on failure
- **Note:** Only works on DUAL locks, not COMBINATION

### 4. Omni-Hack (Rare)

- **Durability:** 3 uses (very limited)
- **Success rate:** 50% per attempt
- **Target:** COMBINATION and DUAL locks
- **Appearance:** Has enchantment glint effect
- **Alarm:** Alarm only on failure against DUAL locks (not COMBINATION)
- **Note:** The only tool that can target both code-based lock types

### Hacking Tool Comparison

| Tool | Rarity | Max Uses | Targets | Success Rate | Alarm on Fail |
|------|--------|---------|---------|-------------|---------------|
| Lockpick | Common | 15 | SIMPLE, SECURITY, HIGH_SEC, DUAL | 80%/40%/10%/5% | HIGH_SEC, DUAL |
| Code Cracker | Uncommon | 10 | COMBINATION | 50% | None |
| Bypass Module | Uncommon | 5 | DUAL | 50% | DUAL |
| Omni-Hack | Rare | 3 | COMBINATION, DUAL | 50% | DUAL only |

---

## Code Entry System

Locks of type COMBINATION and DUAL use a 4-digit code instead of (or in addition to) a physical key.

### Code Entry Flow

1. Right-click on a COMBINATION or DUAL door **without** a valid key
2. A **Code Entry GUI** opens showing a numeric keypad
3. Enter the 4-digit code (0000–9999)
4. Press Enter/Confirm
5. **Success:** Door opens for **3 seconds**, then automatically closes
6. **Failure:** Nothing happens (no alarm, no notification to owner)

### Setting and Changing Codes

```bash
/lock setcode <lock-id> <code>    # Change your lock's code (owner only)
```

Codes must be exactly 4 digits (e.g., `1234`, `0000`, `9999`).

### Automatic Code Rotation (DUAL Locks)

DUAL lock codes automatically rotate **every 24 real-time hours**. The new code is:
- Visible only to the lock owner via `/lock info <lock-id>`
- Distributed through the in-game `/lock info` command or the lock's info display
- The old code becomes invalid immediately on rotation

---

## Lock Placement

### Placing a Lock

1. Hold the **lock item** (SIMPLE_LOCK, SECURITY_LOCK, etc.) in your hand
2. **Right-click on a door** (upper or lower half)
3. The lock attaches to the **lower half** of the door
4. The lock is immediately active — the door is now secured

### Lock Placement Notes

- Only one lock per door is allowed
- Admin-placed locks (OP Level 2+) have **no owner** — useful for scenario/event setup
- Code locks auto-generate a random 4-digit code on placement
- The placer automatically becomes the lock owner

---

## Lock Information Display

### Quick Info (In-World)

Shift + right-click on a locked door shows an overlay:

```
Lock #42 - HIGH_SECURITY
  Owner: Max Mustermann
  Key Tier: Netherite (Tier 2)
  Pickable: Yes (10% success rate)
  Alarm: Active
  Authorized: 2 players
```

Only the owner, authorized players, and OPs see the current code (if COMBINATION/DUAL).

### `/lock info` Command

```bash
/lock info <lock-id>
```

Displays full details including the current code (if you are the owner or OP), authorized player list, and placement timestamp.

---

## Commands Reference

| Command | Permission | Description |
|---------|-----------|-------------|
| `/lock code <lock-id> <code>` | All players | Enter a code to unlock (COMBINATION/DUAL) |
| `/lock setcode <lock-id> <code>` | Owner only | Change the lock's 4-digit code |
| `/lock authorize <lock-id> <player>` | Owner only | Grant a player access to create keys |
| `/lock info <lock-id>` | All players | View lock details (code visible to owner/OP) |
| `/lock remove <lock-id>` | Owner only | Remove your own lock |
| `/lock list` | All players | List all locks you own |
| `/lock admin remove <lock-id>` | OP Level 2+ | Remove any lock (admin command) |

---

## Integration with Police and Crime System

The Lock System integrates directly with the [Police and Crime System](Police-Crime-System.md):

### Alarm Triggers

| Situation | Consequence |
|-----------|-------------|
| Failed lockpick on HIGH_SECURITY | Alarm + wanted level increase |
| Failed lockpick on DUAL | Alarm + wanted level increase |
| Failed Bypass Module on DUAL | Alarm + wanted level increase |
| Failed Omni-Hack on DUAL | Alarm + wanted level increase |
| Failed lockpick on SECURITY | Warning message to owner (no wanted level) |

### What the Alarm Does

1. Triggers a sound effect at the door location
2. Sends a notification to the lock owner (if online)
3. Increases the perpetrator's **wanted level** by 1 star
4. If a Police NPC is within detection range, they may start a pursuit

---

## Data Persistence

**File:** `schedulemc_locks.json`

### Stored Data per Lock

| Field | Type | Description |
|-------|------|-------------|
| `lockId` | String | Unique lock identifier |
| `lockType` | LockType | SIMPLE, SECURITY, etc. |
| `ownerUUID` | UUID | Lock owner's UUID (null = admin lock) |
| `ownerName` | String | Cached owner name for display |
| `position` | BlockPos | Block coordinate of the door |
| `dimension` | String | Dimension key |
| `code` | String | 4-digit code (null for non-code locks) |
| `codeRotatedAt` | long | Last rotation timestamp (DUAL locks) |
| `authorizedPlayers` | List\<UUID\> | Players allowed to create keys |
| `createdAt` | long | Epoch timestamp of placement |

---

## Admin Guide

### Admin Lock Use Cases

Admin-placed locks (OP Level 2+) have no owner and cannot be removed by regular players. Use cases:
- Locking doors in scenario buildings for quests or events
- Securing NPC homes and shops
- Protecting government buildings

Place an admin lock by being OP Level 2+ when using the lock item.

### Clearing Locks

```bash
/lock admin remove <lock-id>    # Remove any lock
/lock list                       # Find lock IDs in a specific area
```

### Finding Orphaned Locks

If a player leaves the server permanently, their locks remain active. To clean up:
1. Use `/lock list` (as OP) to find their locks
2. Use `/lock admin remove <id>` to remove them

---

## Best Practices

### For Players

1. **Use COMBINATION locks for valuable storage** — No key to lose or steal; only vulnerable to Code Cracker/Omni-Hack which are rare.
2. **Rotate your codes regularly** — Use `/lock setcode` periodically. DUAL locks rotate automatically, but COMBINATION locks don't.
3. **Use Key Ring for multiple doors** — If you own multiple properties with SECURITY locks, a Key Ring avoids needing separate keys in your inventory.
4. **Don't copy Original keys** — Copies have 50% the uses and duration. Only copy when sharing access with trusted players.
5. **Authorize trusted players** — Use `/lock authorize` instead of giving out physical keys when possible.

### For Server Administrators

1. **Pre-lock NPC shops** — Place SECURITY locks on NPC merchant shops to prevent players from entering after hours.
2. **Use DUAL for high-value areas** — Bank vaults, government buildings, and prison cells should use DUAL locks.
3. **Admin locks for scenario rooms** — Admin locks can be used in quests where players need to "hack in" to a location with specific hacking tools.
4. **Monitor alarm logs** — Frequent alarm triggers at certain locations may indicate griefing attempts.

---

## Troubleshooting

### Door not locking

1. **Already has a lock** — Each door can only have one lock. Check with Shift + right-click.
2. **Wrong lock item** — Ensure you are holding a lock item (not a key or hacking tool).
3. **Plot protection** — You may not have build permission on this plot. Check plot ownership.

### Key not working

1. **Wrong key** — The key must match the lock's tier. An Iron Key won't open a SIMPLE lock.
2. **Key expired** — Keys have limited duration (7 days for Original SIMPLE). Check remaining time in item tooltip.
3. **Key uses exhausted** — Keys have limited uses. A depleted key becomes invalid.
4. **Not authorized** — You need to be authorized or the owner to use a Netherite key on HIGH_SECURITY/DUAL locks.

### Code not working

1. **Wrong code** — The code must be exactly the 4-digit code set on the lock. Use `/lock info <id>` if you are the owner.
2. **DUAL code rotated** — DUAL lock codes rotate daily. Get the current code from the owner or `/lock info`.
3. **COMBINATION lock (not DUAL)** — COMBINATION codes don't rotate. The original code is still valid unless changed with `/lock setcode`.

### Alarm not triggering

1. **Lock type** — Alarms only trigger for HIGH_SECURITY and DUAL locks. SIMPLE and SECURITY locks don't raise alarms.
2. **Hacking tool type** — Code Cracker on COMBINATION locks never triggers an alarm. Only Bypass Module and Omni-Hack on DUAL locks do.

### Lock data lost after restart

1. **File check** — Open `schedulemc_locks.json` to verify it is valid JSON.
2. **Backup recovery** — Restore from `config/backups/schedulemc_locks_<timestamp>.json`.
3. **Permission issue** — The server process must have write access to the root server directory where `schedulemc_locks.json` is stored.
