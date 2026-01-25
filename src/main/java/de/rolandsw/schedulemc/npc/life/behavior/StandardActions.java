package de.rolandsw.schedulemc.npc.life.behavior;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * StandardActions - Sammlung von Standard-Verhaltensaktionen
 *
 * Enthält häufig verwendete Aktionen die von NPCs ausgeführt werden können.
 */
public class StandardActions {

    // ═══════════════════════════════════════════════════════════
    // FLEE ACTION
    // ═══════════════════════════════════════════════════════════

    /**
     * FleeAction - NPC flieht vor einer Bedrohung
     */
    public static class FleeAction extends BehaviorAction {

        private static final double FLEE_DISTANCE = 30.0;
        private static final int FLEE_SPEED_BOOST = 40; // 40% schneller

        private Vec3 fleeDirection;
        private BlockPos fleeTarget;
        private int stuckCounter = 0;

        public FleeAction() {
            super("flee", "Fliehen", BehaviorPriority.CRITICAL, BehaviorState.FLEEING);
            setMaxDurationSeconds(60); // Max 1 Minute fliehen
        }

        @Override
        public boolean canExecute(CustomNPCEntity npc) {
            NPCLifeData lifeData = npc.getLifeData();
            if (lifeData == null) return false;

            // Kann fliehen wenn: Angst oder niedrige Sicherheit oder Ziel vorhanden
            return lifeData.getEmotions().wouldFlee() ||
                   lifeData.getNeeds().getSafety() < 20 ||
                   (targetEntity != null && targetEntity.isAlive());
        }

        @Override
        public void start(CustomNPCEntity npc) {
            // Fluchtrichtung berechnen - weg vom Ziel
            if (targetEntity != null) {
                Vec3 npcPos = npc.position();
                Vec3 threatPos = targetEntity.position();
                fleeDirection = npcPos.subtract(threatPos).normalize();
            } else {
                // Zufällige Richtung
                Random rand = new Random();
                fleeDirection = new Vec3(
                    rand.nextDouble() * 2 - 1,
                    0,
                    rand.nextDouble() * 2 - 1
                ).normalize();
            }

            // Flucht-Ziel berechnen
            Vec3 fleePos = npc.position().add(fleeDirection.scale(FLEE_DISTANCE));
            fleeTarget = new BlockPos((int) fleePos.x, (int) fleePos.y, (int) fleePos.z);

            // Navigation starten
            npc.getNavigation().moveTo(fleeTarget.getX(), fleeTarget.getY(), fleeTarget.getZ(), 1.3);
        }

        @Override
        public boolean tick(CustomNPCEntity npc) {
            // Prüfen ob Ziel erreicht oder sicher
            NPCLifeData lifeData = npc.getLifeData();
            if (lifeData != null && lifeData.getNeeds().getSafety() > 70) {
                return false; // Sicher genug, aufhören zu fliehen
            }

            // Prüfen ob Bedrohung noch aktiv
            if (targetEntity != null) {
                double distance = npc.distanceTo(targetEntity);
                if (distance > FLEE_DISTANCE * 1.5) {
                    return false; // Weit genug entfernt
                }

                // Richtung neu berechnen wenn Bedrohung näher kommt
                if (distance < 10) {
                    Vec3 npcPos = npc.position();
                    Vec3 threatPos = targetEntity.position();
                    fleeDirection = npcPos.subtract(threatPos).normalize();

                    Vec3 newFleePos = npc.position().add(fleeDirection.scale(FLEE_DISTANCE));
                    fleeTarget = new BlockPos((int) newFleePos.x, (int) newFleePos.y, (int) newFleePos.z);
                    npc.getNavigation().moveTo(fleeTarget.getX(), fleeTarget.getY(), fleeTarget.getZ(), 1.3);
                }
            }

            // Stuck-Detection
            if (npc.getNavigation().isDone() || !npc.getNavigation().isInProgress()) {
                stuckCounter++;
                if (stuckCounter > 20) {
                    // Neue Richtung versuchen
                    Random rand = new Random();
                    fleeDirection = new Vec3(
                        rand.nextDouble() * 2 - 1,
                        0,
                        rand.nextDouble() * 2 - 1
                    ).normalize();

                    Vec3 newFleePos = npc.position().add(fleeDirection.scale(FLEE_DISTANCE / 2));
                    fleeTarget = new BlockPos((int) newFleePos.x, (int) newFleePos.y, (int) newFleePos.z);
                    npc.getNavigation().moveTo(fleeTarget.getX(), fleeTarget.getY(), fleeTarget.getZ(), 1.3);
                    stuckCounter = 0;
                }
            } else {
                stuckCounter = 0;
            }

            return true;
        }

        @Override
        public void stop(CustomNPCEntity npc, boolean interrupted) {
            npc.getNavigation().stop();
            stuckCounter = 0;

            // Emotion abklingen lassen
            NPCLifeData lifeData = npc.getLifeData();
            if (lifeData != null && !interrupted) {
                lifeData.getEmotions().trigger(EmotionState.NEUTRAL, 0);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ALERT POLICE ACTION
    // ═══════════════════════════════════════════════════════════

    /**
     * AlertPoliceAction - NPC alarmiert die Polizei
     */
    public static class AlertPoliceAction extends BehaviorAction {

        private int alertTimer = 0;
        private boolean hasAlerted = false;

        public AlertPoliceAction() {
            super("alert_police", "Polizei alarmieren", BehaviorPriority.CRITICAL, BehaviorState.ALERTING);
            setMaxDurationSeconds(10);
        }

        @Override
        public boolean canExecute(CustomNPCEntity npc) {
            NPCLifeData lifeData = npc.getLifeData();
            if (lifeData == null) return false;

            // Kann alarmieren wenn: Trait erlaubt und Emotion passt
            return lifeData.getEmotions().wouldCallPolice() ||
                   (lifeData.getTraits().wouldReport(5) && lifeData.getEmotions().hasActiveEmotion());
        }

        @Override
        public void start(CustomNPCEntity npc) {
            alertTimer = 0;
            hasAlerted = false;

            // NPC bleibt stehen
            npc.getNavigation().stop();
        }

        @Override
        public boolean tick(CustomNPCEntity npc) {
            alertTimer++;

            // Nach 2 Sekunden: Alarmierung auslösen
            if (alertTimer >= 40 && !hasAlerted) {
                hasAlerted = true;
                triggerPoliceAlert(npc);
            }

            // Nach der Alarmierung noch kurz warten
            return alertTimer < 200; // 10 Sekunden insgesamt
        }

        private void triggerPoliceAlert(CustomNPCEntity npc) {
            if (!(npc.level() instanceof ServerLevel serverLevel)) return;

            // Police-System Integration:
            // 1. Finde Polizei-NPCs in der Nähe (100 Block Radius)
            var policeNPCs = serverLevel.getEntitiesOfClass(
                CustomNPCEntity.class,
                npc.getBoundingBox().inflate(100),
                police -> police.getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI
            );

            // 2. Wenn Polizei in der Nähe und Ziel ist ein Spieler
            if (targetEntity instanceof ServerPlayer serverPlayer) {
                // Wanted-Level erhöhen
                de.rolandsw.schedulemc.npc.crime.CrimeManager.addWantedLevel(
                    serverPlayer.getUUID(), 1, serverLevel.getDayTime() / 24000);

                // Polizei-NPCs zum Spieler schicken
                for (CustomNPCEntity police : policeNPCs) {
                    // Setze Ziel auf den Spieler
                    police.setTarget(serverPlayer);
                }

                // WitnessManager: Registriere das Verbrechen
                var witnessManager = de.rolandsw.schedulemc.npc.life.witness.WitnessManager.getManager(serverLevel);
                witnessManager.registerCrime(
                    serverPlayer,
                    de.rolandsw.schedulemc.npc.life.witness.CrimeType.ASSAULT,
                    npc.blockPosition(),
                    serverLevel,
                    null  // kein explizites Opfer
                );

                // Erinnerung speichern dass alarmiert wurde
                if (npc.getLifeData() != null) {
                    npc.getLifeData().getMemory().addPlayerTag(serverPlayer.getUUID(), "PolizeiGerufen");
                }
            }
        }

        @Override
        public void stop(CustomNPCEntity npc, boolean interrupted) {
            alertTimer = 0;

            if (!interrupted && hasAlerted) {
                // Nach erfolgreicher Alarmierung: Etwas ruhiger
                NPCLifeData lifeData = npc.getLifeData();
                if (lifeData != null) {
                    lifeData.getNeeds().modifySafety(20);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INVESTIGATE ACTION
    // ═══════════════════════════════════════════════════════════

    /**
     * InvestigateAction - NPC untersucht verdächtige Aktivitäten
     */
    public static class InvestigateAction extends BehaviorAction {

        private BlockPos investigateTarget;
        private int investigateTime = 0;
        private boolean reachedTarget = false;

        public InvestigateAction() {
            super("investigate", "Untersuchen", BehaviorPriority.HIGH, BehaviorState.INVESTIGATING);
            setMaxDurationSeconds(30);
        }

        @Override
        public boolean canExecute(CustomNPCEntity npc) {
            NPCLifeData lifeData = npc.getLifeData();
            if (lifeData == null) return false;

            // Nur mutige und ehrliche NPCs untersuchen
            return lifeData.getTraits().wouldInvestigate() &&
                   !lifeData.getEmotions().wouldFlee();
        }

        @Override
        public void start(CustomNPCEntity npc) {
            investigateTime = 0;
            reachedTarget = false;

            // Ziel bestimmen
            if (targetEntity != null) {
                investigateTarget = targetEntity.blockPosition();
            } else {
                // Zufälliger Punkt in der Nähe
                Random rand = new Random();
                investigateTarget = npc.blockPosition().offset(
                    rand.nextInt(10) - 5,
                    0,
                    rand.nextInt(10) - 5
                );
            }

            // Zum Ziel navigieren
            npc.getNavigation().moveTo(
                investigateTarget.getX(),
                investigateTarget.getY(),
                investigateTarget.getZ(),
                0.8
            );
        }

        @Override
        public boolean tick(CustomNPCEntity npc) {
            // Prüfen ob Ziel erreicht
            if (!reachedTarget) {
                double distance = npc.blockPosition().distSqr(investigateTarget);
                if (distance < 9) { // 3 Blöcke
                    reachedTarget = true;
                    npc.getNavigation().stop();
                }
            }

            if (reachedTarget) {
                investigateTime++;

                // "Untersuchen" - NPC schaut sich um
                if (investigateTime % 20 == 0) {
                    npc.getLookControl().setLookAt(
                        npc.getX() + Math.random() * 4 - 2,
                        npc.getY() + 1,
                        npc.getZ() + Math.random() * 4 - 2
                    );
                }

                // Nach 5 Sekunden fertig
                if (investigateTime >= 100) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void stop(CustomNPCEntity npc, boolean interrupted) {
            investigateTime = 0;
            reachedTarget = false;
            investigateTarget = null;

            // Emotion: Leicht misstrauisch bleiben
            NPCLifeData lifeData = npc.getLifeData();
            if (lifeData != null && !interrupted) {
                lifeData.getEmotions().trigger(EmotionState.SUSPICIOUS, 20);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HIDE ACTION
    // ═══════════════════════════════════════════════════════════

    /**
     * HideAction - NPC versteckt sich vor Gefahr
     */
    public static class HideAction extends BehaviorAction {

        private BlockPos hideSpot;
        private boolean isHiding = false;

        public HideAction() {
            super("hide", "Verstecken", BehaviorPriority.HIGH, BehaviorState.HIDING);
            setMaxDurationSeconds(120); // Max 2 Minuten verstecken
        }

        @Override
        public boolean canExecute(CustomNPCEntity npc) {
            NPCLifeData lifeData = npc.getLifeData();
            if (lifeData == null) return false;

            // Ängstliche NPCs verstecken sich eher
            return lifeData.getNeeds().getSafety() < 40 &&
                   lifeData.getTraits().getCourage() < 0;
        }

        @Override
        public void start(CustomNPCEntity npc) {
            isHiding = false;

            // Versteck-Punkt suchen (Zuhause oder geschlossener Raum)
            var homePos = npc.getNpcData().getHomeLocation();
            if (homePos != null) {
                hideSpot = homePos;
            } else {
                // Irgendwo in der Nähe mit Deckung
                hideSpot = findNearbyHideSpot(npc);
            }

            if (hideSpot != null) {
                npc.getNavigation().moveTo(hideSpot.getX(), hideSpot.getY(), hideSpot.getZ(), 1.2);
            }
        }

        private BlockPos findNearbyHideSpot(CustomNPCEntity npc) {
            // Vereinfacht: Geh 10-20 Blöcke in eine Richtung
            Random rand = new Random();
            int dx = rand.nextInt(20) - 10;
            int dz = rand.nextInt(20) - 10;
            return npc.blockPosition().offset(dx, 0, dz);
        }

        @Override
        public boolean tick(CustomNPCEntity npc) {
            if (!isHiding) {
                // Noch auf dem Weg zum Versteck
                if (hideSpot != null && npc.blockPosition().distSqr(hideSpot) < 4) {
                    isHiding = true;
                    npc.getNavigation().stop();
                }
            }

            // Wenn versteckt, warten bis sicher
            if (isHiding) {
                NPCLifeData lifeData = npc.getLifeData();
                if (lifeData != null && lifeData.getNeeds().getSafety() > 60) {
                    return false; // Sicher genug, aufhören
                }
            }

            return true;
        }

        @Override
        public void stop(CustomNPCEntity npc, boolean interrupted) {
            isHiding = false;
            hideSpot = null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // IDLE ACTION
    // ═══════════════════════════════════════════════════════════

    /**
     * IdleAction - NPC tut nichts besonderes
     */
    public static class IdleAction extends BehaviorAction {

        public IdleAction() {
            super("idle", "Ruhend", BehaviorPriority.LOWEST, BehaviorState.IDLE);
        }

        @Override
        public boolean canExecute(CustomNPCEntity npc) {
            return true; // Kann immer ausgeführt werden
        }

        @Override
        public void start(CustomNPCEntity npc) {
            // Nichts besonderes
        }

        @Override
        public boolean tick(CustomNPCEntity npc) {
            // Idle bleibt aktiv bis etwas anderes passiert
            return true;
        }

        @Override
        public void stop(CustomNPCEntity npc, boolean interrupted) {
            // Nichts besonderes
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FACTORY METHOD
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt und registriert alle Standard-Aktionen für einen NPC
     */
    public static void registerAllStandardActions(NPCBehaviorEngine engine) {
        engine.registerAction(new FleeAction());
        engine.registerAction(new AlertPoliceAction());
        engine.registerAction(new InvestigateAction());
        engine.registerAction(new HideAction());
        engine.registerAction(new IdleAction());
    }
}
