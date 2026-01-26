package de.rolandsw.schedulemc.npc.life.companion;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * CompanionBehavior - Steuert das KI-Verhalten eines Begleiters
 *
 * Enthält verschiedene Verhaltenslogiken basierend auf:
 * - Begleitertyp
 * - Aktueller Befehl
 * - Umgebung
 */
public class CompanionBehavior {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final CustomNPCEntity companion;
    private final CompanionData data;

    /** Aktuelles Ziel (z.B. zu verfolgendes Entity) */
    @Nullable
    private LivingEntity currentTarget;

    /** Aktuelle Erkundungsposition */
    @Nullable
    private BlockPos scoutTarget;

    /** Cooldowns */
    private int attackCooldown = 0;
    private int scoutCooldown = 0;
    private int healCooldown = 0;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public CompanionBehavior(CustomNPCEntity companion, CompanionData data) {
        this.companion = companion;
        this.data = data;
    }

    // ═══════════════════════════════════════════════════════════
    // MAIN TICK
    // ═══════════════════════════════════════════════════════════

    /**
     * Haupttick - wird jeden Gametick aufgerufen
     */
    public void tick(ServerLevel level) {
        // Data ticken
        data.tick();

        // Cooldowns
        if (attackCooldown > 0) attackCooldown--;
        if (scoutCooldown > 0) scoutCooldown--;
        if (healCooldown > 0) healCooldown--;

        // Nicht agieren wenn kampfunfähig
        if (data.getState() == CompanionData.CompanionState.INCAPACITATED) {
            return;
        }

        // Besitzer finden
        ServerPlayer owner = findOwner(level);
        if (owner == null) {
            handleNoOwner();
            return;
        }

        // Verhalten basierend auf aktuellem Befehl
        switch (data.getCurrentCommand()) {
            case FOLLOW -> behaviorFollow(owner);
            case STAY -> behaviorStay();
            case SCOUT -> behaviorScout(level, owner);
            case ATTACK -> behaviorAttack(level);
            case DEFEND -> behaviorDefend(level, owner);
            case HEAL -> behaviorHeal(level, owner);
            case RETURN -> behaviorReturn(owner);
            case FREE -> behaviorFree(level, owner);
        }

        // Bei Interaktion Zufriedenheit erhöhen
        if (companion.distanceTo(owner) < 5) {
            data.onOwnerInteraction();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BEHAVIOR IMPLEMENTATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Folge-Verhalten: Bleibt beim Besitzer
     */
    private void behaviorFollow(ServerPlayer owner) {
        double distance = companion.distanceTo(owner);
        double idealDistance = data.getType().getIdealDistance();
        double maxDistance = data.getType().getMaxFollowDistance();

        if (distance > maxDistance) {
            // Zu weit weg - teleportieren
            teleportToOwner(owner);
        } else if (distance > idealDistance) {
            // Zum Besitzer laufen
            companion.getNavigation().moveTo(owner, data.getMoveSpeed());
        } else {
            // Nahe genug - stoppen und Besitzer anschauen
            companion.getNavigation().stop();
            companion.getLookControl().setLookAt(owner);
        }
    }

    /**
     * Warte-Verhalten: Bleibt an Position
     */
    private void behaviorStay() {
        BlockPos waitPos = data.getWaitPosition();
        if (waitPos == null) return;

        double distance = companion.distanceToSqr(Vec3.atCenterOf(waitPos));
        if (distance > 4) { // Mehr als 2 Blöcke entfernt
            companion.getNavigation().moveTo(waitPos.getX(), waitPos.getY(), waitPos.getZ(), data.getMoveSpeed());
        } else {
            companion.getNavigation().stop();
        }
    }

    /**
     * Erkunden-Verhalten: Scout erkundet die Umgebung
     */
    private void behaviorScout(ServerLevel level, ServerPlayer owner) {
        if (!data.getType().canScout()) return;

        // Neues Erkungsziel finden
        if (scoutTarget == null || companion.distanceToSqr(Vec3.atCenterOf(scoutTarget)) < 4 || scoutCooldown <= 0) {
            scoutTarget = findScoutTarget(level, owner);
            scoutCooldown = 200; // 10 Sekunden
        }

        if (scoutTarget != null) {
            companion.getNavigation().moveTo(scoutTarget.getX(), scoutTarget.getY(), scoutTarget.getZ(),
                data.getMoveSpeed() * 1.2f);

            // Nach Feinden suchen während Erkundung
            LivingEntity threat = findNearbyThreat(level, 15);
            if (threat != null) {
                // Besitzer warnen
                warnOwnerAboutThreat(owner, threat);
            }
        }

        // Nicht zu weit vom Besitzer entfernen
        if (companion.distanceTo(owner) > data.getType().getMaxFollowDistance()) {
            behaviorReturn(owner);
        }
    }

    /**
     * Angriffs-Verhalten: Greift Feinde an
     */
    private void behaviorAttack(ServerLevel level) {
        if (!data.getType().canFight()) return;

        // Ziel validieren
        if (currentTarget != null && (currentTarget.isDeadOrDying() || companion.distanceTo(currentTarget) > 20)) {
            currentTarget = null;
        }

        // Neues Ziel suchen
        if (currentTarget == null) {
            currentTarget = findNearbyThreat(level, 15);
        }

        if (currentTarget != null) {
            double distance = companion.distanceTo(currentTarget);

            if (distance > 2) {
                // Zum Ziel laufen
                companion.getNavigation().moveTo(currentTarget, data.getMoveSpeed() * 1.3f);
            } else {
                // Angreifen
                if (attackCooldown <= 0) {
                    companion.doHurtTarget(currentTarget);
                    attackCooldown = 20; // 1 Sekunde
                }
            }
        }
    }

    /**
     * Verteidigungs-Verhalten: Verteidigt den Besitzer
     */
    private void behaviorDefend(ServerLevel level, ServerPlayer owner) {
        if (!data.getType().canFight()) return;

        // Bleibe nahe beim Besitzer
        double distanceToOwner = companion.distanceTo(owner);
        if (distanceToOwner > 5) {
            companion.getNavigation().moveTo(owner, data.getMoveSpeed());
            return;
        }

        // Suche Bedrohungen in der Nähe des Besitzers
        LivingEntity threat = findThreatNearOwner(level, owner, 10);
        if (threat != null) {
            currentTarget = threat;
            behaviorAttack(level);
        }
    }

    /**
     * Heil-Verhalten: Heilt den Besitzer
     */
    private void behaviorHeal(ServerLevel level, ServerPlayer owner) {
        if (!data.getType().canHeal()) return;

        // Zum Besitzer gehen
        double distance = companion.distanceTo(owner);
        if (distance > 3) {
            companion.getNavigation().moveTo(owner, data.getMoveSpeed());
            return;
        }

        // Heilen wenn Cooldown abgelaufen und Besitzer verletzt
        if (healCooldown <= 0 && owner.getHealth() < owner.getMaxHealth()) {
            float healAmount = 2.0f + data.getLevel() * 0.5f;
            owner.heal(healAmount);
            healCooldown = 100; // 5 Sekunden

            // Erfahrung gewinnen
            data.addExperience(5);
        }
    }

    /**
     * Rückkehr-Verhalten: Kehrt sofort zum Besitzer zurück
     */
    private void behaviorReturn(ServerPlayer owner) {
        double distance = companion.distanceTo(owner);

        if (distance > 20) {
            teleportToOwner(owner);
        } else {
            companion.getNavigation().moveTo(owner, data.getMoveSpeed() * 1.5f);
        }

        // Automatisch zu FOLLOW wechseln wenn nah genug
        if (distance < 3) {
            data.setFollowing();
        }
    }

    /**
     * Freies Verhalten: Begleiter entscheidet selbst
     */
    private void behaviorFree(ServerLevel level, ServerPlayer owner) {
        // Typ-basierte Entscheidung
        switch (data.getType()) {
            case FIGHTER -> {
                LivingEntity threat = findNearbyThreat(level, 10);
                if (threat != null) {
                    currentTarget = threat;
                    behaviorAttack(level);
                } else {
                    behaviorFollow(owner);
                }
            }
            case SCOUT -> behaviorScout(level, owner);
            case HEALER -> {
                if (owner.getHealth() < owner.getMaxHealth() * 0.8f) {
                    behaviorHeal(level, owner);
                } else {
                    behaviorFollow(owner);
                }
            }
            case TRADER -> behaviorFollow(owner);
        }
    }

    /**
     * Verhalten wenn kein Besitzer gefunden
     */
    private void handleNoOwner() {
        // Warte am aktuellen Ort
        companion.getNavigation().stop();
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Findet den Besitzer
     */
    @Nullable
    private ServerPlayer findOwner(ServerLevel level) {
        UUID ownerUUID = data.getOwnerUUID();
        if (ownerUUID == null) return null;
        return level.getServer().getPlayerList().getPlayer(ownerUUID);
    }

    /**
     * Teleportiert zum Besitzer
     */
    private void teleportToOwner(ServerPlayer owner) {
        Vec3 pos = owner.position();
        companion.teleportTo(pos.x + (Math.random() * 2 - 1), pos.y, pos.z + (Math.random() * 2 - 1));
    }

    /**
     * Findet eine Bedrohung in der Nähe
     */
    @Nullable
    private LivingEntity findNearbyThreat(ServerLevel level, double range) {
        AABB searchBox = companion.getBoundingBox().inflate(range);
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, searchBox,
            m -> !m.isDeadOrDying() && companion.hasLineOfSight(m));

        return monsters.isEmpty() ? null : monsters.get(0);
    }

    /**
     * Findet Bedrohungen in der Nähe des Besitzers
     */
    @Nullable
    private LivingEntity findThreatNearOwner(ServerLevel level, ServerPlayer owner, double range) {
        AABB searchBox = owner.getBoundingBox().inflate(range);
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, searchBox,
            m -> !m.isDeadOrDying());

        return monsters.isEmpty() ? null : monsters.get(0);
    }

    /**
     * Findet ein Erkundungsziel
     */
    @Nullable
    private BlockPos findScoutTarget(ServerLevel level, ServerPlayer owner) {
        BlockPos ownerPos = owner.blockPosition();
        double range = data.getType().getMaxFollowDistance() - 5;

        // Zufällige Position in Reichweite
        int dx = (int) ((Math.random() * 2 - 1) * range);
        int dz = (int) ((Math.random() * 2 - 1) * range);

        BlockPos target = ownerPos.offset(dx, 0, dz);

        // Höhe anpassen
        for (int y = 5; y > -5; y--) {
            BlockPos checkPos = target.offset(0, y, 0);
            if (level.getBlockState(checkPos).isAir() &&
                level.getBlockState(checkPos.below()).isSolid()) {
                return checkPos;
            }
        }

        return target;
    }

    /**
     * Warnt den Besitzer vor einer Bedrohung
     */
    private void warnOwnerAboutThreat(ServerPlayer owner, LivingEntity threat) {
        // Hier könnte eine Nachricht oder ein Sound gesendet werden
        // Für jetzt: Companion schaut zum Besitzer
        companion.getLookControl().setLookAt(owner);

        // Erfahrung für erfolgreiche Erkundung
        data.addExperience(2);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    @Nullable
    public LivingEntity getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(@Nullable LivingEntity target) {
        this.currentTarget = target;
    }

    // ═══════════════════════════════════════════════════════════
    // GOAL CLASSES (für AI-System Integration)
    // ═══════════════════════════════════════════════════════════

    /**
     * AI-Goal für Begleiter-Verhalten
     */
    public static class CompanionFollowGoal extends Goal {
        private final CustomNPCEntity companion;
        private final CompanionBehavior behavior;

        public CompanionFollowGoal(CustomNPCEntity companion, CompanionBehavior behavior) {
            this.companion = companion;
            this.behavior = behavior;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            CompanionData data = behavior.data;
            return data.hasOwner() &&
                   data.getState() != CompanionData.CompanionState.INCAPACITATED;
        }

        @Override
        public void tick() {
            if (companion.level() instanceof ServerLevel level) {
                behavior.tick(level);
            }
        }
    }
}
