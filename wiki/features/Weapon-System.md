# Weapon System

The Weapon System adds a complete combat overhaul to ScheduleMC, including firearms, melee weapons, grenades, attachments, and fire-mode upgrades. All weapons are server-side authoritative and tightly integrated with the mod's NPC and economy systems.

---

## Overview

| Category | Items | Count |
|---|---|---|
| Guns | AK-47, Pistol, Revolver, Shotgun, Sniper Rifle, MP5 | 6 |
| Magazines | Pistol, Rifle, Shotgun, Sniper, SMG, Heavy | 6 |
| Melee weapons | Baseball Bat, Machete, Combat Knife, (base class) | 3 playable |
| Grenades | Frag, Smoke, Flash | 3 |
| Attachments | Scope, Silencer, Laser | 3 |
| Fire-mode upgrades | Single Precision, Burst Fire, Auto Fire | 3 |
| Legacy ammo | ammo_pistol, ammo_rifle, ammo_shotgun, ammo_sniper, ammo_smg, ammo_heavy | 6 |
| **Total registered items** | | **29** |

---

## Guns

All guns extend `GunItem` and store their entire state in NBT on the `ItemStack`:

| NBT Key | Type | Description |
|---|---|---|
| `Ammo` | int | Current magazine fill |
| `AmmoType` | int | Index into the gun's supported magazine list |
| `FireMode` | int | 0 = single, 1 = burst, 2 = auto |
| `Attachment1` / `Attachment2` | String | Attachment type name or empty |
| `Cooldown` | int | Ticks remaining before next shot |

### Gun Stats

| Gun | Damage | Accuracy | Cooldown (ticks) | Magazine | Fire Modes |
|---|---|---|---|---|---|
| AK-47 | 8 | 85 % | 3 | 30 | Single, Auto |
| Pistol | 6 | 95 % | 10 | 15 | Single, Burst |
| Revolver | 10 | 90 % | 15 | 6 | Single |
| Shotgun | 4 × 5 pellets | 60 % | 25 | 8 | Single |
| Sniper Rifle | 20 | 98 % | 40 | 5 | Single |
| MP5 | 5 | 80 % | 2 | 30 | Single, Burst, Auto |

### Accuracy and Spray

When a shot is fired, the bullet direction is randomized within a cone based on accuracy (0.0–1.0). The Scope attachment narrows the cone significantly. The Shotgun fires 5 simultaneous projectiles per shot.

### Reloading

- Press **R** (default) to reload the current gun.
- A reload packet (`WeaponReloadPacket`) is sent to the server, which calls `gun.reload()`.
- The gun's magazine is fully refilled if the player has the appropriate magazine item in their inventory; the magazine item is consumed.

### Fire Modes

- Press **R while sneaking** to cycle through available fire modes.
- Fire mode is stored as NBT on the item and synced via `WeaponSetAmmoTypePacket`.
- Auto mode triggers a server-side loop in `PlayerTickEvent` while `WeaponAutoFireActive` is set in player persistent data.

---

## Melee Weapons

All melee weapons extend `MeleeWeaponItem` (which extends `SwordItem`) with customized enchantment whitelists.

| Weapon | Special Ability |
|---|---|
| Baseball Bat | Increased knockback on every hit |
| Machete | Fast destruction of leaves and cobwebs; 20 % chance to apply a bleed effect on hit |
| Combat Knife | Faster base attack speed than a sword |

---

## Grenades

Grenades are thrown items backed by `ThrownWeaponGrenade` (`ThrowableItemProjectile`). On impact, each type triggers a different effect:

| Grenade | Effect |
|---|---|
| Frag | Creates an explosion with power 3.0 (damages blocks and entities) |
| Smoke | Spawns campfire smoke particles in a 4-block radius |
| Flash | Applies Blindness (10 s) and Slowness (5 s) to all players and NPCs within 8 blocks |

---

## Attachments

Up to **2 attachments** can be applied to any gun. Attachments are stored by type name in the gun's NBT (`Attachment1`, `Attachment2`).

| Attachment | Effect |
|---|---|
| Scope | Zooms FOV while holding Shift; narrows bullet spray cone |
| Silencer | Suppresses the `weapon_gun_shot` sound (plays `weapon_click` instead) |
| Laser | Renders a visible laser beam from the gun barrel (client-side renderer) |

Attachments are applied and removed by right-clicking the gun with the attachment item in hand.

---

## Fire-Mode Upgrades

Fire-mode upgrades permanently unlock additional fire modes for a gun. They are consumed on use.

| Upgrade | Unlocks |
|---|---|
| Single Precision Upgrade | Locks gun to single-shot mode (removes burst/auto) |
| Burst Fire Upgrade | Unlocks burst mode (3 shots per trigger pull) |
| Auto Fire Upgrade | Unlocks full-auto mode |

---

## Network Packets

All gun actions are client → server authoritative. The client sends a packet; the server validates and executes.

| Packet | Direction | Action |
|---|---|---|
| `WeaponFirePacket` | Client → Server | Trigger `gun.performShots(slot, count)` |
| `WeaponReloadPacket` | Client → Server | Trigger `gun.reload()` |
| `WeaponStartAutoFirePacket` | Client → Server | Set `WeaponAutoFireActive` in player persistent data |
| `WeaponStopAutoFirePacket` | Client → Server | Remove `WeaponAutoFireActive` |
| `WeaponSetAmmoTypePacket` | Client → Server | Change loaded magazine type index |

Channel: `schedulemc:weapon` (protocol version 1).

---

## Client HUD

While holding a gun, the following information is rendered on-screen (`RenderGuiOverlayEvent`):

- **Ammo counter** — current rounds / max magazine capacity
- **Fire mode** — text label (Single / Burst / Auto)
- **Cooldown bar** — horizontal bar showing remaining cooldown ticks

The HUD is drawn in the bottom-right area of the screen and is hidden when the player is not holding a gun.

---

## Sounds

All sounds are registered under the `schedulemc` namespace and declared in `assets/schedulemc/sounds.json`.

| Sound Key | Trigger |
|---|---|
| `schedulemc:weapon_gun_shot` | Every shot (suppressed by Silencer) |
| `schedulemc:weapon_empty_click` | Trigger pull with empty magazine |
| `schedulemc:weapon_click` | Shot with Silencer attached |
| `schedulemc:weapon_reload` | Successful reload |
| `schedulemc:weapon_grenade_explode` | Frag grenade detonation |

---

## Particles

| Particle | Trigger |
|---|---|
| `schedulemc:weapon_muzzle_flash` | Each shot, at the gun's barrel position |
| `schedulemc:weapon_blood` | Entity hit by a bullet |

---

## Configuration

Weapon ranges are configurable in `schedulemc-weapons.toml` (placed in the server's `config/` directory).

```toml
[weapon_ranges]
  ak47_range = 64
  pistol_range = 32
  revolver_range = 48
  shotgun_range = 16
  sniper_range = 128
  mp5_range = 40
```

Bullets are discarded (`entity.discard()`) when they exceed the configured range.

---

## Architecture

```
weapon/
├── attachment/          Attachment data class, WeaponAttachments registry, 3 attachment items
├── client/              WeaponClientSetup — entity renderers + particle providers
├── config/              WeaponConfig (ForgeConfigSpec)
├── entity/              WeaponBulletEntity, ThrownWeaponGrenade, WeaponEntities registry
├── grenade/             GrenadeType enum, GrenadeItem, Frag/Smoke/FlashGrenadeItem
├── gun/                 GunProperties builder, GunItem base, 6 concrete gun classes
├── handler/             WeaponServerEventHandler (auto-fire, left-click cancel)
│                        WeaponClientEventHandler (HUD, scope, input handling)
├── item/                WeaponItems — DeferredRegister with all 29 items
├── melee/               MeleeWeaponItem base, Baseball Bat, Machete, Combat Knife
├── network/             WeaponPackets channel, 5 packet classes
├── particle/            WeaponParticles — muzzle flash + blood particle types
├── render/              WeaponBulletRenderer — cross-quad lightning beam
├── sound/               WeaponSounds — DeferredRegister for 5 sound events
└── upgrade/             FireModeUpgradeType enum, base upgrade item, 3 concrete upgrades
```

---

## Creative Tab

All 29 weapon items appear in the **Weapons** creative tab (`schedulemc.weapon_tab`), ordered as:
magazines → guns → melee weapons → grenades → attachments → upgrades → legacy ammo items.

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-13**.
- Diese Datei wurde im Rahmen der Vollständigkeits-Aktualisierung überarbeitet.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

