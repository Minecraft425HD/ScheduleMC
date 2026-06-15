package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.weapon.config.WeaponConfig;
import de.rolandsw.schedulemc.weapon.entity.WeaponBulletEntity;
import de.rolandsw.schedulemc.weapon.item.WeaponItems;
import de.rolandsw.schedulemc.weapon.sound.WeaponSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Polizei-Kampfeskalation: greift NUR, wenn eine Festnahme nicht funktioniert
 * und das Fahndungslevel hoch ist. Festnahme bleibt immer bevorzugt.
 *
 * - Nahkampf (Baseball-Schläger) ab POLICE_MELEE_WANTED_LEVEL
 * - Fernkampf (Pistole) ab POLICE_RANGED_WANTED_LEVEL jenseits der Mindestdistanz
 * - Standardmäßig nicht-tödlich (Gummigeschosse, Stopp bei ½ Herz → Festnahme);
 *   scharfe Munition nur im Notfall (5★ UND Spieler hat Polizei angegriffen).
 */
@Mod.EventBusSubscriber(modid = "schedulemc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PoliceCombatHandler {

    public enum EngagementMode { NONE, MELEE, RANGED }

    /** playerUUID -> Anzahl Festnahme-Fluchten (für Eskalation). */
    private static final Map<UUID, Integer> escapeCounts = new ConcurrentHashMap<>();
    /** playerUUID -> aktive Schützen (Cap). */
    private static final Map<UUID, java.util.Set<UUID>> activeShooters = new ConcurrentHashMap<>();
    /** playerUUID -> letzter Tick, an dem der Spieler einen Polizisten angriff (Notfall-Tötung). */
    private static final Map<UUID, Long> lastAttackedPolice = new ConcurrentHashMap<>();
    /** Bullet-Geschwindigkeit (Blöcke/Tick), wie GunItem. */
    private static final float BULLET_SPEED = 3.0f;
    /** Bewegungstempo beim Vorrücken im Fernkampf (identisch zur Verfolgung in PoliceAIHandler). */
    private static final double POLICE_SPEED = 1.2;
    /** Repath-Schwelle (Blöcke², XZ) beim feuernden Vorrücken — spart A*-Last. */
    private static final double REPATH_THRESHOLD_SQR = 16.0;
    /** Mindestabstand (Ticks) zwischen zwei Ausweich-/Deckungs-Manövern eines Polizisten. */
    private static final long EVADE_COOLDOWN_TICKS = 30L;
    /** Dauer (Ticks) einer Deckungs-/Strafe-Phase, in der nicht vorgerückt wird. */
    private static final long COVER_DURATION_TICKS = 30L;
    /** Inventar-Snapshot bei Polizei-Tod (Items bleiben beim Spieler). */
    private static final Map<UUID, java.util.List<ItemStack>> deathInventorySnapshot = new ConcurrentHashMap<>();

    private PoliceCombatHandler() {
    }

    // ═══════════════════════════════════════════════════════════
    // ESKALATIONS-STATE (aus PoliceAIHandler gepflegt)
    // ═══════════════════════════════════════════════════════════

    public static void recordEscape(UUID playerUUID) {
        escapeCounts.merge(playerUUID, 1, Integer::sum);
    }

    public static void resetEscalation(UUID playerUUID) {
        escapeCounts.remove(playerUUID);
        activeShooters.remove(playerUUID);
    }

    private static int escapeCount(UUID playerUUID) {
        return escapeCounts.getOrDefault(playerUUID, 0);
    }

    // ═══════════════════════════════════════════════════════════
    // ENTSCHEIDUNG (pure, unit-testbar)
    // ═══════════════════════════════════════════════════════════

    public static EngagementMode decideEngagement(boolean enabled, int wantedLevel,
            double distance, double arrestDistance, double rangedMinDistance,
            boolean inArrestCountdown, boolean hasLineOfSight, boolean targetInVehicle,
            int escapeCount, long pursuitTicks, int escapeThreshold, long pursuitTickThreshold,
            int meleeWantedLevel, int rangedWantedLevel) {
        if (!enabled) return EngagementMode.NONE;
        // Festnahme gewinnt immer
        if (inArrestCountdown || distance < arrestDistance) return EngagementMode.NONE;
        boolean escalated = escapeCount >= escapeThreshold || pursuitTicks >= pursuitTickThreshold;
        if (!escalated) return EngagementMode.NONE;
        if (wantedLevel >= rangedWantedLevel && distance > rangedMinDistance
                && hasLineOfSight && !targetInVehicle) {
            return EngagementMode.RANGED;
        }
        if (wantedLevel >= meleeWantedLevel && distance <= rangedMinDistance) {
            return EngagementMode.MELEE;
        }
        return EngagementMode.NONE;
    }

    // ═══════════════════════════════════════════════════════════
    // KAMPF-TICK (aus PoliceAIHandler.onPoliceAI im Verfolgungs-Branch)
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird im Verfolgungs-Branch aufgerufen (Spieler außerhalb Arrest-Bereich,
     * nicht versteckt, nicht im Fahrzeug). Bewegung läuft parallel weiter.
     */
    public static EngagementMode tickCombat(CustomNPCEntity npc, ServerPlayer target,
                                  int wantedLevel, double distance, double arrestDistance, long currentTick) {
        var cfg = ModConfigHandler.COMMON;
        if (!cfg.POLICE_COMBAT_ENABLED.get()) {
            standDown(npc);
            return EngagementMode.NONE;
        }

        long pursuitStart = npc.getPersistentData().getLong("PursuitStartTick");
        if (pursuitStart == 0) {
            npc.getPersistentData().putLong("PursuitStartTick", currentTick);
            pursuitStart = currentTick;
        }
        long pursuitTicks = currentTick - pursuitStart;

        boolean los = npc.hasLineOfSight(target);
        boolean inVehicle = PoliceVehiclePursuit.isPlayerInVehicle(target);

        EngagementMode mode = decideEngagement(
            true, wantedLevel, distance, arrestDistance, cfg.POLICE_RANGED_MIN_DISTANCE.get(),
            false, los, inVehicle,
            escapeCount(target.getUUID()), pursuitTicks,
            cfg.POLICE_ESCALATION_ESCAPE_COUNT.get(), cfg.POLICE_ESCALATION_PURSUIT_SECONDS.get() * 20L,
            cfg.POLICE_MELEE_WANTED_LEVEL.get(), cfg.POLICE_RANGED_WANTED_LEVEL.get());

        if (mode == EngagementMode.NONE) {
            standDown(npc);
            return EngagementMode.NONE;
        }

        if (mode == EngagementMode.MELEE) {
            // MeleeAttackGoal übernimmt die Bewegung — PoliceAIHandler darf
            // NICHT zusätzlich moveTo aufrufen (sonst Navigations-Tauziehen)
            equip(npc, WeaponItems.BASEBALL_BAT.get());
            npc.setTarget(target);
            npc.getPersistentData().putLong("RetaliationUntil", npc.level().getGameTime() + 100L);
        } else { // RANGED
            // Feuernd vorrücken: so nah wie möglich an den Spieler heran (bis MELEE greift).
            // Kein Nahkampf-Goal (setTarget(null)) → der Handler darf die Navigation selbst steuern.
            equip(npc, WeaponItems.PISTOL.get());
            npc.setTarget(null);
            npc.getLookControl().setLookAt(target);

            long now = npc.level().getGameTime();
            if (now >= npc.getPersistentData().getLong("CoverUntilTick")) {
                // Keine aktive Deckungs-/Strafe-Phase → Richtung Spieler vorrücken.
                advanceTowards(npc, target);
            }
            // Während der Deckungsphase läuft der in reactToHit gesetzte moveTo-Pfad weiter.
            tryShoot(npc, target, currentTick, cfg);
        }
        return mode;
    }

    private static void tryShoot(CustomNPCEntity npc, ServerPlayer target, long currentTick,
                                 ModConfigHandler.Common cfg) {
        // In der Deckungsphase ist die Sichtlinie bewusst gebrochen → nicht durch Wände feuern.
        if (!npc.hasLineOfSight(target)) return;
        // Schützen-Cap pro Ziel
        var shooters = activeShooters.computeIfAbsent(target.getUUID(), k -> ConcurrentHashMap.newKeySet());
        if (!shooters.contains(npc.getUUID()) && shooters.size() >= cfg.POLICE_MAX_SIMULTANEOUS_SHOOTERS.get()) {
            return;
        }
        long last = npc.getPersistentData().getLong("LastPoliceShotTick");
        if (currentTick - last < cfg.POLICE_SHOT_COOLDOWN_TICKS.get()) return;
        if (!(npc.level() instanceof ServerLevel level)) return;
        npc.getPersistentData().putLong("LastPoliceShotTick", currentTick);
        shooters.add(npc.getUUID());

        boolean emergency = isEmergency(target);
        var ammo = emergency ? WeaponItems.AMMO_STANDARD.get() : WeaponItems.AMMO_RUBBER.get();

        WeaponBulletEntity bullet = new WeaponBulletEntity(level, npc,
            (float) (double) cfg.POLICE_PISTOL_DAMAGE.get(), ammo, null, WeaponConfig.PISTOL_RANGE.get());

        // Vorhalte-Berechnung auf die Zielbewegung
        double flightTicks = npc.distanceTo(target) / BULLET_SPEED;
        Vec3 aim = target.position()
            .add(target.getDeltaMovement().scale(flightTicks))
            .add(0, target.getBbHeight() * 0.6, 0)
            .subtract(npc.getEyePosition()).normalize();
        double spread = 0.04;
        bullet.shoot(
            aim.x + (level.random.nextDouble() - 0.5) * spread,
            aim.y + (level.random.nextDouble() - 0.5) * spread,
            aim.z + (level.random.nextDouble() - 0.5) * spread,
            BULLET_SPEED, 0f);
        level.addFreshEntity(bullet);
        level.playSound(null, npc.getX(), npc.getY(), npc.getZ(),
            WeaponSounds.GUN_SHOT.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);

        if (npc.tickCount % 60 == 0) {
            target.sendSystemMessage(Component.translatable("event.police.shots_warning"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FERNKAMPF-BEWEGUNG: Vorrücken + Deckung/Ausweichen bei Treffern
    // ═══════════════════════════════════════════════════════════

    /**
     * Rückt feuernd auf den Spieler vor. Repath-Throttle (wie PoliceAIHandler),
     * da tickCombat nur ~alle 20 Ticks läuft.
     */
    private static void advanceTowards(CustomNPCEntity npc, ServerPlayer target) {
        var data = npc.getPersistentData();
        Vec3 tp = target.position();
        boolean hasPath = data.getBoolean("AdvanceHasPath");
        double movedSqr = Double.MAX_VALUE;
        if (hasPath) {
            double dx = tp.x - data.getDouble("AdvancePathX");
            double dz = tp.z - data.getDouble("AdvancePathZ");
            movedSqr = dx * dx + dz * dz;
        }
        if (npc.getNavigation().isDone() || !hasPath || movedSqr > REPATH_THRESHOLD_SQR) {
            npc.getNavigation().moveTo(target, POLICE_SPEED);
            data.putDouble("AdvancePathX", tp.x);
            data.putDouble("AdvancePathZ", tp.z);
            data.putBoolean("AdvanceHasPath", true);
        }
    }

    /**
     * Treffer-Reaktion im Fernkampf: nahe Deckung (LOS brechen) suchen, sonst seitlich ausweichen.
     * Ereignisbasiert aus {@link #onCombatDamage} aufgerufen; per Cooldown gedrosselt.
     */
    private static void reactToHit(CustomNPCEntity npc, ServerPlayer attacker) {
        if (!(npc.level() instanceof ServerLevel)) return;
        // Nur reagieren, wenn aktiv im Fernkampf (Verfolgung läuft + Pistole gezogen).
        if (npc.getPersistentData().getLong("PursuitStartTick") == 0L) return;
        if (npc.getMainHandItem().getItem() != WeaponItems.PISTOL.get()) return;

        long now = npc.level().getGameTime();
        if (now - npc.getPersistentData().getLong("LastEvadeTick") < EVADE_COOLDOWN_TICKS) return;

        Vec3 cover = findCoverPos(npc, attacker);
        boolean moved = (cover != null)
            ? npc.getNavigation().moveTo(cover.x, cover.y, cover.z, POLICE_SPEED)
            : strafeAway(npc, attacker);

        if (moved) {
            npc.getPersistentData().putLong("LastEvadeTick", now);
            npc.getPersistentData().putLong("CoverUntilTick", now + COVER_DURATION_TICKS);
            // Advance-Pfad invalidieren, damit nach der Phase frisch zum Spieler gepatht wird.
            npc.getPersistentData().putBoolean("AdvanceHasPath", false);
        }
    }

    /**
     * Sucht billig (max. 6 Raycasts) eine nahe Position, die die Sichtlinie zum Spieler bricht.
     * @return Deckungsposition oder {@code null}, wenn keine gefunden wurde.
     */
    private static Vec3 findCoverPos(CustomNPCEntity npc, ServerPlayer player) {
        if (!(npc.level() instanceof ServerLevel level)) return null;
        Vec3 npcPos = npc.position();
        Vec3 playerEye = player.getEyePosition(1.0f);

        Vec3 away = npcPos.subtract(player.position());
        away = new Vec3(away.x, 0, away.z);
        if (away.lengthSqr() < 1.0e-3) return null;
        away = away.normalize();
        Vec3 right = away.cross(new Vec3(0, 1, 0)).normalize();

        double d = 4.0;
        Vec3[] candidates = new Vec3[] {
            npcPos.add(away.scale(d)),
            npcPos.add(away.scale(d)).add(right.scale(d)),
            npcPos.add(away.scale(d)).add(right.scale(-d)),
            npcPos.add(right.scale(d)),
            npcPos.add(right.scale(-d)),
            npcPos.add(away.scale(d * 1.5)),
        };

        double eyeHeight = npc.getEyeHeight();
        for (Vec3 c : candidates) {
            Vec3 coverEye = new Vec3(c.x, npcPos.y + eyeHeight, c.z);
            BlockHitResult hit = level.clip(new ClipContext(
                coverEye, playerEye, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, npc));
            if (hit.getType() == HitResult.Type.BLOCK) {
                return c; // fester Block bricht die Sichtlinie → gute Deckung
            }
        }
        return null;
    }

    /**
     * Seitlicher Ausweichschritt quer zur Schussrichtung. Probiert beide Seiten.
     * @return true, wenn eine Bewegung gestartet wurde.
     */
    private static boolean strafeAway(CustomNPCEntity npc, ServerPlayer player) {
        Vec3 npcPos = npc.position();
        Vec3 toPlayer = player.position().subtract(npcPos);
        toPlayer = new Vec3(toPlayer.x, 0, toPlayer.z);
        if (toPlayer.lengthSqr() < 1.0e-3) return false;
        Vec3 right = toPlayer.normalize().cross(new Vec3(0, 1, 0)).normalize();

        double sign = npc.getRandom().nextBoolean() ? 1.0 : -1.0;
        double step = 3.0;
        Vec3 dest = npcPos.add(right.scale(sign * step));
        if (npc.getNavigation().moveTo(dest.x, dest.y, dest.z, POLICE_SPEED)) {
            return true;
        }
        dest = npcPos.add(right.scale(-sign * step));
        return npc.getNavigation().moveTo(dest.x, dest.y, dest.z, POLICE_SPEED);
    }

    /** Stand-down: Waffe weg, Ziel weg, Schützen-Slot frei. */
    public static void standDown(CustomNPCEntity npc) {
        if (!npc.getMainHandItem().isEmpty()) {
            npc.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        npc.getPersistentData().putLong("PursuitStartTick", 0L);
        npc.getPersistentData().putLong("CoverUntilTick", 0L);
        npc.getPersistentData().putLong("LastEvadeTick", 0L);
        npc.getPersistentData().putBoolean("AdvanceHasPath", false);
        if (npc.getTarget() instanceof ServerPlayer p) {
            var set = activeShooters.get(p.getUUID());
            if (set != null) set.remove(npc.getUUID());
        }
    }

    private static void equip(CustomNPCEntity npc, net.minecraft.world.item.Item weapon) {
        ItemStack current = npc.getMainHandItem();
        if (current.isEmpty() || current.getItem() != weapon) {
            npc.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
            npc.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
            npc.level().players().stream()
                .filter(pl -> pl.distanceToSqr(npc) < 1024)
                .forEach(pl -> pl.sendSystemMessage(Component.translatable("event.police.weapon_drawn")));
        }
    }

    /** Notfall: 5★ UND der Spieler hat in den letzten 10s einen Polizisten angegriffen. */
    private static boolean isEmergency(ServerPlayer player) {
        if (CrimeManager.getWantedLevel(player.getUUID()) < 5) return false;
        Long t = lastAttackedPolice.get(player.getUUID());
        return t != null && player.level().getGameTime() - t <= 200L;
    }

    // ═══════════════════════════════════════════════════════════
    // SCHADENS-FILTER: nicht-tödlich, Friendly-Fire, Notfall-Tracking
    // ═══════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onCombatDamage(LivingDamageEvent event) {
        // Spieler greift Polizist an -> Notfall-Fenster setzen
        if (event.getEntity() instanceof CustomNPCEntity npcVictim
                && npcVictim.getNpcType() == NPCType.POLICE
                && event.getSource().getEntity() instanceof ServerPlayer attacker) {
            lastAttackedPolice.put(attacker.getUUID(), npcVictim.level().getGameTime());
            // Im Fernkampf getroffen → Deckung suchen oder seitlich ausweichen.
            reactToHit(npcVictim, attacker);
        }

        // Polizei-Kugeln: Friendly-Fire + nicht-tödliche Klammer
        if (event.getSource().getDirectEntity() instanceof WeaponBulletEntity bullet
                && bullet.getOwner() instanceof CustomNPCEntity owner
                && owner.getNpcType() == NPCType.POLICE) {

            LivingEntity victim = event.getEntity();
            // Kein Friendly-Fire auf andere NPCs
            if (victim instanceof CustomNPCEntity) {
                event.setCanceled(true);
                return;
            }
            if (victim instanceof ServerPlayer player) {
                // Unbeteiligte (nicht gesucht) werden nicht getroffen
                if (CrimeManager.getWantedLevel(player.getUUID()) == 0) {
                    event.setCanceled(true);
                    return;
                }
                boolean lethalAllowed = ModConfigHandler.COMMON.POLICE_LETHAL_FORCE.get() && isEmergency(player);
                if (!lethalAllowed && event.getAmount() >= player.getHealth() - 1.0f) {
                    // Stopp bei ½ Herz -> Festnahme übernimmt
                    event.setAmount(Math.max(0f, player.getHealth() - 1.0f));
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
                    player.sendSystemMessage(Component.translatable("event.police.downed_arrest"));
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // POLIZEI-TOD: Inventar behalten + Illegale-Items-Strafe
    // ═══════════════════════════════════════════════════════════

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPoliceKill(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!killedByPolice(event)) return;

        // Illegale-Items-Strafe VOR dem Leeren prüfen
        if (hasIllegalItems(player)) {
            applyIllegalItemsPenalty(player);
        }

        // Inventar sichern und leeren, damit Vanilla nichts droppt
        java.util.List<ItemStack> snapshot = new java.util.ArrayList<>();
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (!st.isEmpty()) snapshot.add(st.copy());
        }
        deathInventorySnapshot.put(player.getUUID(), snapshot);
        inv.clearContent();
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        var snapshot = deathInventorySnapshot.remove(event.getOriginal().getUUID());
        if (snapshot == null) return;
        if (event.getEntity() instanceof ServerPlayer newPlayer) {
            for (ItemStack st : snapshot) {
                newPlayer.getInventory().add(st);
            }
        }
    }

    private static boolean killedByPolice(LivingDeathEvent event) {
        var direct = event.getSource().getDirectEntity();
        if (direct instanceof WeaponBulletEntity b
                && b.getOwner() instanceof CustomNPCEntity owner
                && owner.getNpcType() == NPCType.POLICE) {
            return true;
        }
        return event.getSource().getEntity() instanceof CustomNPCEntity npc
            && npc.getNpcType() == NPCType.POLICE;
    }

    private static boolean hasIllegalItems(ServerPlayer player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (de.rolandsw.schedulemc.npc.crime.ItemLegalityChecker.isIllegal(inv.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    private static void applyIllegalItemsPenalty(ServerPlayer player) {
        var cfg = ModConfigHandler.COMMON;
        double cashRate = cfg.POLICE_ILLEGAL_ITEMS_CASH_PENALTY.get();
        double accRate = cfg.POLICE_ILLEGAL_ITEMS_ACCOUNT_PENALTY.get();
        UUID id = player.getUUID();

        double cash = de.rolandsw.schedulemc.economy.WalletManager.getBalance(id);
        double account = de.rolandsw.schedulemc.economy.EconomyManager.getBalance(id);
        double cashPenalty = Math.round(cash * cashRate * 100.0) / 100.0;
        double accPenalty = Math.round(account * accRate * 100.0) / 100.0;

        if (cashPenalty > 0) {
            de.rolandsw.schedulemc.economy.WalletManager.removeMoney(id, cashPenalty);
            de.rolandsw.schedulemc.economy.WalletManager.save();
        }
        if (accPenalty > 0) {
            de.rolandsw.schedulemc.economy.EconomyManager.withdraw(id, accPenalty,
                de.rolandsw.schedulemc.economy.TransactionType.TAX_SALES, "Illegal items penalty");
        }
        double total = cashPenalty + accPenalty;
        if (total > 0 && player.getServer() != null) {
            de.rolandsw.schedulemc.economy.StateAccount.getInstance(player.getServer())
                .deposit(total, "Illegal items penalty");
            player.sendSystemMessage(Component.translatable("event.police.illegal_items_penalty",
                String.format("%.2f", cashPenalty), String.format("%.2f", accPenalty)));
        }
    }
}
