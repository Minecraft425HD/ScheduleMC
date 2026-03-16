# Secret Doors System

**Navigation:** [Home](../Home.md) | [Features Overview](../Home.md#feature-systems) | [Lock System](Lock-System.md) | [MapView System](MapView-System.md)

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Components](#components)
   - [Secret Door (Pivot)](#secret-door-pivot)
   - [Hatch (Floor/Ceiling)](#hatch-floorceiling)
   - [Hidden Switch](#hidden-switch)
   - [Elevator](#elevator)
   - [Door Filler Block](#door-filler-block)
   - [Remote Control Item](#remote-control-item)
4. [Block States & Properties](#block-states--properties)
5. [Dynamic Size System](#dynamic-size-system)
6. [Camouflage System](#camouflage-system)
7. [Linking System](#linking-system)
8. [Redstone Integration](#redstone-integration)
9. [Admin Commands](#admin-commands)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

---

## Overview

The **Secret Doors System** provides fully concealable, player-owned secret passages for ScheduleMC. All components can be disguised as regular blocks (camouflage), controlled via redstone, linked to hidden switches, and operated remotely via a Remote Control item.

**Package:** `de.rolandsw.schedulemc.secretdoors`

**Key features:**
- Dynamic door sizes from **1×1 to 20×20** blocks
- Full **block camouflage** — doors appear as any other block type
- **Redstone integration** — open/close with redstone signal
- **Hidden switches** that look like normal stone or wood blocks
- **Remote Control** item with up to 20 linked doors, 64-block range
- **Elevator system** with BFS floodfill platform and station linking

---

## Architecture

```
secretdoors/
├── SecretDoors.java              ← Block/Item/BlockEntity registration
├── SecretDoorCommand.java        ← /secretdoor command (OP)
├── SecretDoorEventHandler.java   ← Forge event handling
│
├── blocks/
│   ├── AbstractSecretDoorBlock.java  ← Base class: size detection, filler management,
│   │                                    camouflage, redstone, placement/removal
│   ├── SecretDoorBlock.java          ← PIVOT type (wall door, swings sideways)
│   ├── HatchBlock.java               ← HATCH type (floor/ceiling trapdoor)
│   ├── HiddenSwitchBlock.java        ← Disguised switch (stone/oak variants)
│   ├── ElevatorBlock.java            ← BFS floodfill elevator platform
│   └── DoorFillerBlock.java          ← Internal filler (no item, no crafting)
│
├── blockentity/
│   ├── SecretDoorBlockEntity.java    ← Shared by SECRET_DOOR + HATCH
│   ├── HiddenSwitchBlockEntity.java  ← Switch state + linked doors list
│   ├── ElevatorBlockEntity.java      ← Station list, max range/stations
│   └── DoorFillerBlockEntity.java    ← Controller back-reference only
│
├── items/
│   └── RemoteControlItem.java        ← NBT-based linked door list (max 20)
│
└── client/
    ├── SecretDoorBlockEntityRenderer.java  ← Camouflage rendering
    ├── ElevatorBlockEntityRenderer.java    ← Elevator rendering
    └── DoorFillerBlockEntityRenderer.java  ← Filler rendering
```

**Block-Entity relationship:**

| Block | BlockEntity | Shared? |
|-------|-------------|---------|
| `secret_door` | `SecretDoorBlockEntity` | Yes (with hatch) |
| `hatch` | `SecretDoorBlockEntity` | Yes (with secret_door) |
| `hidden_switch_stone` / `_oak` | `HiddenSwitchBlockEntity` | No |
| `elevator` | `ElevatorBlockEntity` | No |
| `door_filler` | `DoorFillerBlockEntity` | No |

---

## Components

### Secret Door (Pivot)

**Registry ID:** `schedulemc:secret_door`
**Item:** `schedulemc:secret_door_item`
**Type:** `DoorType.PIVOT`
**Class:** `SecretDoorBlock extends AbstractSecretDoorBlock`

A wall-mounted secret door that **rotates/swings sideways** when opened (like a bookshelf door in popular culture). Placed at the bottom-left corner of the desired opening.

**Block States:**

| Property | Values | Default |
|----------|--------|---------|
| `facing` | NORTH, SOUTH, EAST, WEST | NORTH |
| `open` | true / false | false |
| `powered` | true / false | false |

**Size detection:** On placement, automatically detects adjacent air blocks to determine width (right of facing direction) and height (upward). Max 20×20.

**Interaction:**
- **Right-click** (empty hand or non-block item): Toggle open/close
- **Right-click** (block item): Set camouflage to that block type
- **Shift + Right-click** (block item): Remove camouflage
- **Shift + Right-click** (empty hand): Show size configuration info in chat

**Rendering:** Uses `RenderShape.ENTITYBLOCK_ANIMATED` when closed (BlockEntityRenderer handles camouflage), `RenderShape.INVISIBLE` when open (no collision).

---

### Hatch (Floor/Ceiling)

**Registry ID:** `schedulemc:hatch`
**Item:** `schedulemc:hatch_item`
**Type:** `DoorType.HATCH`
**Class:** `HatchBlock extends AbstractSecretDoorBlock`

A floor or ceiling hatch that expands horizontally (X-Z plane). Placed at one corner of the desired opening. Spreads in the facing direction (horizontally) instead of upward.

**Block States:**

| Property | Values | Default |
|----------|--------|---------|
| `facing` | NORTH, SOUTH, EAST, WEST | NORTH (opposite of player look direction) |
| `open` | true / false | false |
| `powered` | true / false | false |
| `waterlogged` | true / false | false |

**Size detection:**
- **Width:** Air blocks to the right (clockwise of facing)
- **Height:** Air blocks in the facing direction (horizontal spread)

All interactions and camouflage behavior are identical to the Secret Door.

---

### Hidden Switch

**Registry IDs:** `schedulemc:hidden_switch_stone`, `schedulemc:hidden_switch_oak`
**Items:** `schedulemc:hidden_switch_stone_item`, `schedulemc:hidden_switch_oak_item`
**Class:** `HiddenSwitchBlock extends BaseEntityBlock`

Appears visually identical to a full stone or oak plank block. Functions as a secret toggle switch for linked doors.

**Block States:**

| Property | Values | Default |
|----------|--------|---------|
| `powered` | true / false | false |

**Interaction:**

| Action | Result |
|--------|--------|
| Right-click | Toggle all linked doors |
| Shift + Right-click (owner only) | Enter/exit linking mode |
| Redstone rising edge | Open all linked doors |
| Redstone falling edge | Close all linked doors |

**Linking mode workflow:**
1. Shift + right-click the switch → enter linking mode (chat message shown)
2. Right-click any Secret Door or Hatch → link/unlink that door
3. The switch also registers itself in the door's `SecretDoorBlockEntity`
4. Right-click the switch again in linking mode → exit linking mode

**Ownership:** The player who places the switch becomes the owner. Only the owner can enter linking mode.

---

### Elevator

**Registry ID:** `schedulemc:elevator`
**Item:** `schedulemc:elevator_item`
**Class:** `ElevatorBlock extends BaseEntityBlock`

A redstone-controlled elevator that teleports all players standing on its platform to the next station above.

**Block States:**

| Property | Values | Default |
|----------|--------|---------|
| `facing` | NORTH, SOUTH, EAST, WEST | NORTH |
| `powered` | true / false | false |

**Platform Detection (BFS Floodfill):**

On placement, the elevator uses **Breadth-First Search** to fill all connected air blocks on the same Y-level:
- Maximum spread: **20 blocks** in X/Z (constant `MAX_SPREAD = 20`)
- Fills only air blocks at the exact same Y coordinate
- The platform shape can be any connected area (L-shaped, T-shaped, etc.)

**Station Linking:**
- **Shift + Right-click:** Enter linking mode to link stations
- Stations must be at the **exact same X/Z coordinates** (directly above/below)
- Maximum vertical distance: **128 blocks** (`MAX_DISTANCE` in `ElevatorBlockEntity`)
- Maximum stations per elevator chain: **32**

**Operation:**
- A **redstone signal** (rising edge) triggers the elevator
- All players standing on the floodfill platform are teleported to the next station upward
- If no station above, nothing happens

**Camouflage:**
- Right-click with any block → set platform camouflage
- Shift + Right-click (empty hand) → remove camouflage

---

### Door Filler Block

**Registry ID:** `schedulemc:door_filler`
**Item:** *None* (internal only, not obtainable in survival)
**Class:** `DoorFillerBlock`

Internal blocks that fill the area of a secret door or hatch beyond the 1×1 controller block. Players cannot craft or obtain these blocks. They are automatically placed and removed by the `AbstractSecretDoorBlock` when a door is placed, resized, or broken.

Each filler stores a back-reference to the controller's `BlockPos` in its `DoorFillerBlockEntity`, so breaking any filler triggers removal of the entire door.

---

### Remote Control Item

**Registry ID:** `schedulemc:remote_control`
**Item:** `schedulemc:remote_control`
**Class:** `RemoteControlItem extends Item`

A handheld device for remotely controlling secret doors. Linked doors are stored in the item's **NBT data**.

**Limits:**
- Maximum linked doors: **20** (`MAX_LINKED_DOORS = 20`)
- Activation range: **64 blocks** (`MAX_RANGE = 64`)

**NBT Structure:**
```nbt
{
  "linked_doors": [
    { "pos": <long> },   // BlockPos.asLong() encoded
    ...
  ]
}
```

**Interaction:**

| Action | Result |
|--------|--------|
| Right-click on air | Toggle all linked doors within 64 blocks |
| Right-click on door (no shift) | Toggle only that specific door |
| Shift + Right-click on door | Link/unlink that door (toggle) |
| Shift + Right-click on switch | Toggle switch's linking mode |

**Tooltip display:**
- Number of linked doors (e.g., `Verknüpfte Türen: 3 / 20`)
- First 5 linked positions (coordinates)
- `... und X mehr` if more than 5
- Range reminder: `Reichweite: 64 Blöcke`

---

## Block States & Properties

All non-filler secret door blocks share these Forge block state properties from `AbstractSecretDoorBlock`:

| Property | Class | Description |
|----------|-------|-------------|
| `OPEN` | `BooleanProperty` (`BlockStateProperties.OPEN`) | Whether the door is open |
| `FACING` | `DirectionProperty` (`BlockStateProperties.HORIZONTAL_FACING`) | Which direction the door faces |
| `POWERED` | `BooleanProperty` (`BlockStateProperties.POWERED`) | Whether currently powered by redstone |

Additional properties per block type:
- `HatchBlock`: adds `WATERLOGGED` (`BlockStateProperties.WATERLOGGED`)
- `HiddenSwitchBlock`: only `POWERED` (no OPEN or FACING)
- `ElevatorBlock`: only `FACING` and `POWERED`

---

## Dynamic Size System

The size system is implemented in `AbstractSecretDoorBlock` and works as follows:

### Auto-Detection on Placement

```
Place controller block →
  autoDetectSize():
    width = 1 + consecutive air blocks to the RIGHT (clockwise of FACING), max 19
    height:
      PIVOT:  1 + consecutive air blocks UPWARD, max 19
      HATCH:  1 + consecutive air blocks in FACING direction, max 19
  spawnFillers(width, height):
    for each (w, h) position except (0,0):
      place DOOR_FILLER block
      store DoorFillerBlockEntity.controllerPos = controller
      store offset in SecretDoorBlockEntity.fillerOffsets
```

### Manual Resize via Command

Admins can manually set size with:
```
/secretdoor size <x> <y> <z> <width> <height>
```

This calls `spawnFillers()` again, first removing all existing fillers.

### Size Limits

| Dimension | Minimum | Maximum |
|-----------|---------|---------|
| Width | 1 | 20 |
| Height | 1 | 20 |
| Total blocks | 1 | 400 |

---

## Camouflage System

The camouflage system allows any secret door or elevator to appear as any other block type.

### Setting Camouflage

```
Right-click on door/elevator with any BlockItem
  → be.setCamoBlock(block)
  → sendBlockUpdated() to force re-render
  → Chat: "Tarnung gesetzt auf: <block name>"
```

### Removing Camouflage

```
Shift + Right-click on door with any BlockItem
  → be.clearCamoBlock()
  → sendBlockUpdated()
  → Chat: "Tarnung entfernt."
```

### Rendering

The `SecretDoorBlockEntityRenderer` (client-side only) reads the camouflage block from the `SecretDoorBlockEntity` and renders the stored block's model in place of the door block. When the door is open (`OPEN = true`), the block renders as `RenderShape.INVISIBLE` (no rendering).

Collision shapes follow the open state:
- **Closed:** Full block collision (`Shapes.block()`)
- **Open:** No collision (`Shapes.empty()`)

---

## Linking System

### Door ↔ Switch Linking

Both sides of the link are stored:
- `HiddenSwitchBlockEntity`: stores `List<BlockPos> linkedDoors`
- `SecretDoorBlockEntity`: stores `List<BlockPos> linkedSwitches`

This bidirectional storage allows:
- The switch to toggle all its doors
- The door to notify its switches when broken (cleanup)

### Workflow

```
1. Player shift+right-clicks switch → be.setLinkingMode(true)
2. Player right-clicks a secret door:
   HiddenSwitchBlock.tryLinkDoor():
     if be.isLinkingMode():
       if door already in linkedDoors:
         be.removeDoor(doorPos)
         doorBe.removeLinkedSwitch(switchPos)
       else:
         be.addDoor(doorPos)
         doorBe.addLinkedSwitch(switchPos)
3. Switch sends chat message confirming link/unlink
```

### Remote Control Linking

The Remote Control uses a simpler per-item NBT list (no bidirectional tracking):
- Shift + right-click on door → `RemoteControlItem.toggleLink(stack, pos)`
- Adds/removes `{pos: <long>}` entries from the item's `linked_doors` NBT list
- Range check occurs at activation time only

---

## Redstone Integration

All secret door types respond to redstone neighbor signals:

| Block Type | Rising Edge | Falling Edge |
|------------|-------------|--------------|
| Secret Door / Hatch | Opens door | Closes door |
| Hidden Switch | Opens all linked doors | Closes all linked doors |
| Elevator | Teleports players upward | No action |

**Signal source:** `level.hasNeighborSignal(pos)` — any adjacent redstone signal (wire, button, lever, comparator, etc.)

**State tracking:** The `POWERED` block state property is updated in sync with the redstone signal, preventing toggle loops.

Secret door blocks are **not** signal sources (`isSignalSource()` returns `false`).

---

## Admin Commands

All `/secretdoor` sub-commands require **operator level 2** (`source.hasPermission(2)`).

### `/secretdoor size <pos> <width> <height>`

Sets the size of a secret door at the given position.

```
/secretdoor size 100 64 200 3 4
```

- `pos`: Block position of the controller block
- `width`: 1–20
- `height`: 1–20

Removes all existing fillers and spawns new ones.

---

### `/secretdoor toggle <pos>`

Remotely opens or closes a secret door.

```
/secretdoor toggle 100 64 200
```

---

### `/secretdoor info <pos>`

Displays detailed information about a secret door.

```
/secretdoor info 100 64 200
```

Output includes:
- Type (PIVOT / HATCH)
- Size (width × height)
- Status (OPEN / CLOSED)
- Owner name
- Filler block count
- Number of linked switches

---

## Best Practices

### Placement Tips

1. **Clear the area first** — Place the controller with air blocks already in the desired shape. The auto-detection scans for contiguous air in the correct directions immediately on placement.
2. **Orientation matters** — The door faces **opposite** to the player's horizontal look direction at placement time. Face toward where you want the door to open from.
3. **Camouflage after sizing** — Apply camouflage after confirming the correct size, as resizing clears and re-places filler blocks (which may disturb already-set camouflage on fillers).

### Switch Placement Tips

1. **Place in walls** — Hidden switches appear as full blocks; place them inside walls, floors, or ceilings for best concealment.
2. **Link before camouflaging** — Link the switch to all desired doors before applying camouflage to surrounding blocks, as camouflaged blocks may be harder to identify later.
3. **Multiple switches** — One door can be linked to multiple switches. Place redundant switches for emergency access.

### Elevator Setup

1. **Clear the platform area** — Ensure the floodfill area is fully air before placing the elevator block. The BFS only fills air blocks.
2. **Align X/Z exactly** — All stations in a chain must share the exact same X and Z coordinates.
3. **Vertical spacing** — Keep stations within 128 blocks vertically. The maximum chain is 32 stations.

---

## Troubleshooting

| Problem | Cause | Solution |
|---------|-------|----------|
| Door won't open/close | Filler blocks were placed manually over the door area | Break and re-place the controller block |
| Camouflage not showing | Client-side renderer issue | Relog or use `/secretdoor toggle` to force update |
| Switch not toggling door | Link not established bidirectionally | Break and re-place switch, then re-link |
| Elevator not teleporting | Players not standing on the floodfill area | Ensure players are on the exact platform Y-level |
| Door stuck open after break | Filler cleanup failed | Use `/secretdoor toggle` then `/fill` to clean air blocks manually |
| Remote control out of range | Range is 64 blocks (straight-line distance) | Move closer or place additional switches |
| Size won't change | Already at max 20×20 or insufficient air space | Clear surrounding blocks, then use `/secretdoor size` |
| "Keine Berechtigung" on switch | Not the switch owner | Ask the owner or use `/secretdoor toggle` (OP) |

---

*Part of the ScheduleMC v3.6.9-beta documentation. For API integration, see the [API Reference](../../docs/API_REFERENCE.md).*
