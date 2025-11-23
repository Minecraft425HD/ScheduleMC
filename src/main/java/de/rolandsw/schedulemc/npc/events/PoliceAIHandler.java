package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.WantedLevelSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Polizei-KI System:
 * - Patrouilliert und sucht Verbrecher
 * - Verfolgt Spieler mit Wanted-Level
 * - Festnahme bei Kontakt (5 Sekunden Cooldown)
 * - Gefängnis-System mit Timer
 */
public class PoliceAIHandler {

    private static final int DETECTION_RADIUS = 32; // 32 Blöcke
    private static final double ARREST_DISTANCE = 2.0; // 2 Blöcke
    private static final double POLICE_SPEED = 1.2; // 20% schneller
    private static final long ARREST_COOLDOWN = 5 * 20; // 5 Sekunden in Ticks

    // UUID -> Arrest Start Time (in Ticks)
    private static final Map<UUID, Long> arrestTimers = new HashMap<>();

    /**
     * Polizei-KI: Sucht Verbrecher und verfolgt sie
     */
    @SubscribeEvent
    public void onPoliceAI(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
        if (npc.level().isClientSide) return;
        if (npc.getNpcType() != NPCType.POLIZEI) return;

        // Prüfe ob Polizei knockout ist
        if (npc.getPersistentData().getBoolean("IsKnockedOut")) {
            return; // Keine AI während knockout
        }

        // Nur alle 20 Ticks (1 Sekunde) prüfen
        if (npc.tickCount % 20 != 0) return;

        // Suche Verbrecher in der Nähe
        List<ServerPlayer> nearbyPlayers = npc.level().getEntitiesOfClass(
            ServerPlayer.class,
            AABB.ofSize(npc.position(), DETECTION_RADIUS, DETECTION_RADIUS, DETECTION_RADIUS)
        );

        ServerPlayer targetCriminal = null;
        int highestWantedLevel = 0;

        // Finde Spieler mit höchstem Wanted-Level
        for (ServerPlayer player : nearbyPlayers) {
            int wantedLevel = CrimeManager.getWantedLevel(player.getUUID());

            if (wantedLevel > 0 && wantedLevel > highestWantedLevel) {
                targetCriminal = player;
                highestWantedLevel = wantedLevel;
            }
        }

        if (targetCriminal != null) {
            // Verfolge Verbrecher
            double distance = npc.distanceTo(targetCriminal);

            if (distance < ARREST_DISTANCE) {
                // ═══════════════════════════════════════════
                // IM ARREST-BEREICH (< 2 Blöcke)
                // ═══════════════════════════════════════════
                long currentTick = npc.level().getGameTime();
                UUID playerUUID = targetCriminal.getUUID();

                if (!arrestTimers.containsKey(playerUUID)) {
                    // Start Arrest-Timer
                    arrestTimers.put(playerUUID, currentTick);
                    targetCriminal.sendSystemMessage(Component.literal("§c⚠ FESTNAHME läuft... 5s"));
                } else {
                    // Timer läuft bereits - prüfe ob abgelaufen
                    long startTick = arrestTimers.get(playerUUID);
                    long elapsed = currentTick - startTick;

                    if (elapsed >= ARREST_COOLDOWN) {
                        // 5 Sekunden vorbei → FESTNAHME!
                        arrestPlayer(npc, targetCriminal);
                        arrestTimers.remove(playerUUID);
                    } else {
                        // Zeige verbleibende Zeit (alle Sekunde)
                        long remainingTicks = ARREST_COOLDOWN - elapsed;
                        int remainingSeconds = (int) Math.ceil(remainingTicks / 20.0);

                        if (elapsed % 20 == 0) { // Jede Sekunde
                            targetCriminal.sendSystemMessage(
                                Component.literal("§c⚠ FESTNAHME in " + remainingSeconds + "s...")
                            );
                        }
                    }
                }
            } else {
                // ═══════════════════════════════════════════
                // AUSSERHALB ARREST-BEREICH (verfolgen)
                // ═══════════════════════════════════════════
                // Reset Timer falls vorhanden
                UUID playerUUID = targetCriminal.getUUID();
                if (arrestTimers.containsKey(playerUUID)) {
                    arrestTimers.remove(playerUUID);
                    targetCriminal.sendSystemMessage(Component.literal("§e✓ Du bist entkommen!"));
                }

                // Verfolge
                npc.getNavigation().moveTo(targetCriminal, POLICE_SPEED);

                // Warnung alle 5 Sekunden
                if (npc.tickCount % 100 == 0) {
                    targetCriminal.sendSystemMessage(
                        Component.literal("§c⚠ POLIZEI! Bleib stehen!")
                    );
                }
            }
        } else {
            // Keine Verbrecher - patrouilliere normal
            // (TODO: Implementiere Patrol-Route wenn gewünscht)
        }
    }

    /**
     * Festnahme eines Spielers
     */
    private void arrestPlayer(CustomNPCEntity police, ServerPlayer player) {
        int wantedLevel = CrimeManager.getWantedLevel(player.getUUID());
        if (wantedLevel <= 0) return; // Kein Wanted-Level mehr

        // Verhindere mehrfache Festnahmen
        if (player.getPersistentData().getLong("JailReleaseTime") > 0) {
            return; // Schon im Gefängnis
        }

        // Strafe berechnen
        int fine = wantedLevel * 500; // 500€ pro Stern
        int jailTimeSeconds = wantedLevel * 60; // 60 Sekunden pro Stern

        // Geld aus Wallet-Item abziehen
        ItemStack wallet = player.getInventory().getItem(8);
        if (wallet.getItem() instanceof CashItem) {
            double currentMoney = CashItem.getValue(wallet);

            if (currentMoney >= fine) {
                // Strafe bezahlen
                CashItem.removeValue(wallet, fine);
                player.sendSystemMessage(Component.literal("§c✗ FESTGENOMMEN!"));
                player.sendSystemMessage(Component.literal("§7Strafe: §c" + fine + "€"));
            } else {
                // Nicht genug Geld → Gefängnis länger + alles konfisziert
                jailTimeSeconds *= 2;
                CashItem.setValue(wallet, 0);
                player.sendSystemMessage(Component.literal("§c✗ FESTGENOMMEN!"));
                player.sendSystemMessage(Component.literal("§7Alles Bargeld konfisziert!"));
                player.sendSystemMessage(Component.literal("§c§lDOPPELTE HAFTZEIT!"));
            }
        }

        // Teleportiere zu Gefängnis (Spawn-Punkt als Platzhalter)
        BlockPos jailPos = player.getRespawnPosition() != null ?
            player.getRespawnPosition() : BlockPos.ZERO;

        player.teleportTo(jailPos.getX(), jailPos.getY(), jailPos.getZ());

        // Setze Gefängnis-Timer
        long releaseTime = player.level().getGameTime() + (jailTimeSeconds * 20L); // Ticks
        player.getPersistentData().putLong("JailReleaseTime", releaseTime);
        player.getPersistentData().putInt("JailX", jailPos.getX());
        player.getPersistentData().putInt("JailY", jailPos.getY());
        player.getPersistentData().putInt("JailZ", jailPos.getZ());

        // Gefängnis-Effekte
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, jailTimeSeconds * 20, 2));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, jailTimeSeconds * 20, 250)); // Kein Springen

        // Reset Wanted-Level
        CrimeManager.clearWantedLevel(player.getUUID());

        // Reset Arrest-Timer
        arrestTimers.remove(player.getUUID());

        // Meldungen
        player.sendSystemMessage(Component.literal("§7Haftzeit: §e" + jailTimeSeconds + " Sekunden"));
        player.sendSystemMessage(Component.literal("§7Fahndungsstufe zurückgesetzt"));

        System.out.println("[POLICE] " + police.getNpcName() + " arrested " +
            player.getName().getString() + " - Jail time: " + jailTimeSeconds + "s");
    }

    /**
     * Gefängnis-System: Hält Spieler im Gefängnis
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        long releaseTime = player.getPersistentData().getLong("JailReleaseTime");

        if (releaseTime > 0) {
            long currentTime = player.level().getGameTime();

            if (currentTime >= releaseTime) {
                // ═══════════════════════════════════════════
                // FREILASSUNG
                // ═══════════════════════════════════════════
                player.getPersistentData().remove("JailReleaseTime");
                player.getPersistentData().remove("JailX");
                player.getPersistentData().remove("JailY");
                player.getPersistentData().remove("JailZ");

                player.sendSystemMessage(Component.literal("§a✓ Du bist frei!"));
                player.sendSystemMessage(Component.literal("§7Halte dich von nun an an das Gesetz."));

                // Entferne Effekte
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                player.removeEffect(MobEffects.JUMP);

                System.out.println("[JAIL] Player " + player.getName().getString() + " released");
            } else {
                // ═══════════════════════════════════════════
                // NOCH IM GEFÄNGNIS
                // ═══════════════════════════════════════════
                // Verhindere Flucht - teleportiere zurück
                int jailX = player.getPersistentData().getInt("JailX");
                int jailY = player.getPersistentData().getInt("JailY");
                int jailZ = player.getPersistentData().getInt("JailZ");

                if (player.distanceToSqr(jailX, jailY, jailZ) > 100) {
                    player.teleportTo(jailX, jailY, jailZ);
                    player.sendSystemMessage(Component.literal("§c✗ FLUCHT VERHINDERT!"));
                }

                // Zeit anzeigen alle 10 Sekunden
                if (currentTime % 200 == 0) {
                    long remainingTicks = releaseTime - currentTime;
                    long remainingSeconds = remainingTicks / 20;

                    player.sendSystemMessage(Component.literal(
                        "§7Noch §e" + remainingSeconds + " Sekunden §7im Gefängnis..."
                    ));
                }
            }
        }

        // ═══════════════════════════════════════════
        // ESCAPE SYSTEM (Verstecken vor Polizei)
        // ═══════════════════════════════════════════
        int wantedLevel = CrimeManager.getWantedLevel(player.getUUID());
        if (wantedLevel > 0) {
            long currentTick = player.level().getGameTime();

            // Finde nächste Polizei
            List<CustomNPCEntity> nearbyPolice = player.level().getEntitiesOfClass(
                CustomNPCEntity.class,
                AABB.ofSize(player.position(), 100, 100, 100),
                npc -> npc.getNpcType() == NPCType.POLIZEI && !npc.getPersistentData().getBoolean("IsKnockedOut")
            );

            double minDistance = Double.MAX_VALUE;
            for (CustomNPCEntity police : nearbyPolice) {
                double distance = player.distanceTo(police);
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }

            // Escape-Logic
            if (minDistance > CrimeManager.ESCAPE_DISTANCE) {
                // Weit genug von Polizei entfernt → Start Escape-Timer
                if (!CrimeManager.isHiding(player.getUUID())) {
                    CrimeManager.startEscapeTimer(player.getUUID(), currentTick);
                    player.sendSystemMessage(Component.literal("§e✓ Du versteckst dich vor der Polizei..."));
                }

                // Prüfe ob Escape erfolgreich
                if (CrimeManager.checkEscapeSuccess(player.getUUID(), currentTick)) {
                    player.sendSystemMessage(Component.literal("§a✓ Du bist entkommen! -1★"));
                }
            } else {
                // Polizei zu nah → Stop Escape-Timer
                if (CrimeManager.isHiding(player.getUUID())) {
                    CrimeManager.stopEscapeTimer(player.getUUID());
                    player.sendSystemMessage(Component.literal("§c✗ Polizei hat dich entdeckt!"));
                }
            }

            // Sync zu Client (für HUD Overlay)
            long escapeTime = CrimeManager.getEscapeTimeRemaining(player.getUUID(), currentTick);
            NPCNetworkHandler.sendToPlayer(new WantedLevelSyncPacket(wantedLevel, escapeTime), player);
        } else {
            // Kein Wanted-Level → cleanup und sync 0 zum Client
            arrestTimers.remove(player.getUUID());
            NPCNetworkHandler.sendToPlayer(new WantedLevelSyncPacket(0, 0), player);
        }
    }
}
