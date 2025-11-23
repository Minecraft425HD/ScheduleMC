package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
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
 * - Festnahme bei Kontakt (konfigurierbarer Cooldown)
 * - Gefängnis-System mit Timer
 * - Indoor-Versteck-System
 * - Polizei sucht in Gebieten wo Spieler zuletzt gesehen wurde
 */
public class PoliceAIHandler {

    private static final double POLICE_SPEED = 1.2; // 20% schneller

    // UUID -> Arrest Start Time (in Ticks)
    private static final Map<UUID, Long> arrestTimers = new HashMap<>();

    // NPC UUID -> Last Pursuit Target (um zu wissen wen wir verfolgt haben)
    private static final Map<UUID, UUID> lastPursuitTarget = new HashMap<>();

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

        long currentTick = npc.level().getGameTime();

        // Lade Config-Werte
        int detectionRadius = ModConfigHandler.COMMON.POLICE_DETECTION_RADIUS.get();
        double arrestDistance = ModConfigHandler.COMMON.POLICE_ARREST_DISTANCE.get();
        long arrestCooldown = ModConfigHandler.COMMON.POLICE_ARREST_COOLDOWN_SECONDS.get() * 20L;

        // Prüfe ob Polizei gerade sucht
        if (PoliceSearchBehavior.isSearching(npc)) {
            UUID searchTarget = PoliceSearchBehavior.getSearchTarget(npc);

            if (PoliceSearchBehavior.isSearchExpired(searchTarget, currentTick)) {
                // Suche abgebrochen
                PoliceSearchBehavior.stopSearch(npc, searchTarget);
                PoliceBackupSystem.unregisterPolice(searchTarget, npc.getUUID());
                System.out.println("[POLICE] " + npc.getNpcName() + " hat die Suche aufgegeben");
            } else {
                // Weiter suchen
                System.out.println("[POLICE] " + npc.getNpcName() + " führt Suche fort (searchArea wird aufgerufen)");
                PoliceSearchBehavior.searchArea(npc, searchTarget, currentTick);
            }
        }

        // Prüfe ob diese Polizei einer Verfolgung zugewiesen ist (Backup-System)
        UUID assignedTarget = PoliceBackupSystem.getAssignedTarget(npc.getUUID());
        ServerPlayer targetCriminal = null;
        int highestWantedLevel = 0;

        if (assignedTarget != null) {
            // Diese Polizei hat ein zugewiesenes Ziel (Verstärkung)
            ServerPlayer assignedPlayer = npc.level().getServer().getPlayerList().getPlayer(assignedTarget);
            if (assignedPlayer != null && !PoliceSearchBehavior.isPlayerHidden(assignedPlayer, npc)) {
                targetCriminal = assignedPlayer;
                highestWantedLevel = CrimeManager.getWantedLevel(assignedPlayer.getUUID());
                System.out.println("[BACKUP] " + npc.getNpcName() + " verfolgt zugewiesenes Ziel: " +
                    assignedPlayer.getName().getString());
            }
        }

        // Wenn kein zugewiesenes Ziel, suche Verbrecher in der Nähe
        if (targetCriminal == null) {
            List<ServerPlayer> nearbyPlayers = npc.level().getEntitiesOfClass(
                ServerPlayer.class,
                AABB.ofSize(npc.position(), detectionRadius, detectionRadius, detectionRadius)
            );

            // Finde Spieler mit höchstem Wanted-Level
            for (ServerPlayer player : nearbyPlayers) {
                int wantedLevel = CrimeManager.getWantedLevel(player.getUUID());

                if (wantedLevel > 0 && wantedLevel > highestWantedLevel) {
                    // Prüfe ob Spieler versteckt ist
                    if (!PoliceSearchBehavior.isPlayerHidden(player, npc)) {
                        targetCriminal = player;
                        highestWantedLevel = wantedLevel;
                    }
                }
            }
        }

        if (targetCriminal != null) {
            // Spieler gefunden! Stoppe Suchverhalten falls aktiv
            if (PoliceSearchBehavior.isSearching(npc)) {
                PoliceSearchBehavior.stopSearch(npc, targetCriminal.getUUID());
            }

            // Speichere dass wir diesen Spieler verfolgen
            lastPursuitTarget.put(npc.getUUID(), targetCriminal.getUUID());

            // Verfolge Verbrecher
            double distance = npc.distanceTo(targetCriminal);

            if (distance < arrestDistance) {
                // ═══════════════════════════════════════════
                // IM ARREST-BEREICH
                // ═══════════════════════════════════════════
                UUID playerUUID = targetCriminal.getUUID();

                if (!arrestTimers.containsKey(playerUUID)) {
                    // Start Arrest-Timer
                    arrestTimers.put(playerUUID, currentTick);
                    int cooldownSeconds = ModConfigHandler.COMMON.POLICE_ARREST_COOLDOWN_SECONDS.get();
                    targetCriminal.sendSystemMessage(Component.literal("§c⚠ FESTNAHME läuft... " + cooldownSeconds + "s"));
                } else {
                    // Timer läuft bereits - prüfe ob abgelaufen
                    long startTick = arrestTimers.get(playerUUID);
                    long elapsed = currentTick - startTick;

                    if (elapsed >= arrestCooldown) {
                        // Cooldown vorbei → FESTNAHME!
                        arrestPlayer(npc, targetCriminal);
                        arrestTimers.remove(playerUUID);
                    } else {
                        // Zeige verbleibende Zeit (alle Sekunde)
                        long remainingTicks = arrestCooldown - elapsed;
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
                UUID playerUUID = targetCriminal.getUUID();

                // Prüfe ob Spieler versteckt ist (im Gebäude)
                boolean isHidden = PoliceSearchBehavior.isPlayerHidden(targetCriminal, npc);

                if (isHidden) {
                    // Spieler ist versteckt - NICHT ins Gebäude folgen!
                    // Stattdessen: Gebiet durchsuchen / vor Gebäude warten
                    System.out.println("[POLICE] " + npc.getNpcName() + " - Spieler ist versteckt, wechsle zu Suchmodus");

                    // Reset Timer falls vorhanden
                    if (arrestTimers.containsKey(playerUUID)) {
                        arrestTimers.remove(playerUUID);
                        targetCriminal.sendSystemMessage(Component.literal("§e✓ Du bist entkommen!"));
                    }

                    // Starte Suchverhalten statt direkter Verfolgung
                    if (!PoliceSearchBehavior.isSearching(npc)) {
                        PoliceSearchBehavior.startSearch(npc, targetCriminal, currentTick);
                        targetCriminal.sendSystemMessage(Component.literal("§e⚠ Die Polizei sucht dich im Gebiet..."));
                    }

                    // Speichere dass wir diesen Spieler verfolgen
                    lastPursuitTarget.put(npc.getUUID(), targetCriminal.getUUID());

                } else {
                    // Spieler ist NICHT versteckt - normale Verfolgung

                    // Reset Timer falls vorhanden
                    if (arrestTimers.containsKey(playerUUID)) {
                        arrestTimers.remove(playerUUID);
                        targetCriminal.sendSystemMessage(Component.literal("§e✓ Du bist entkommen!"));
                    }

                    // Verfolge direkt
                    npc.getNavigation().moveTo(targetCriminal, POLICE_SPEED);

                    // Warnung alle 5 Sekunden
                    if (npc.tickCount % 100 == 0) {
                        targetCriminal.sendSystemMessage(
                            Component.literal("§c⚠ POLIZEI! Bleib stehen!")
                        );
                    }
                }
            }
        } else if (!PoliceSearchBehavior.isSearching(npc)) {
            // ═══════════════════════════════════════════
            // KEIN VERBRECHER IN SICHT
            // ═══════════════════════════════════════════
            // Prüfe ob wir vorher jemanden verfolgt haben
            UUID lastTarget = lastPursuitTarget.get(npc.getUUID());

            if (lastTarget != null) {
                System.out.println("[POLICE] " + npc.getNpcName() + " hat Verfolgungsziel verloren, starte Suche...");

                // Starte Suchverhalten für verlorenen Spieler
                ServerPlayer lostPlayer = npc.level().getServer().getPlayerList().getPlayer(lastTarget);

                if (lostPlayer != null && CrimeManager.getWantedLevel(lastTarget) > 0) {
                    System.out.println("[POLICE] " + npc.getNpcName() + " startet Suche nach " + lostPlayer.getName().getString());
                    PoliceSearchBehavior.startSearch(npc, lostPlayer, currentTick);
                    lostPlayer.sendSystemMessage(Component.literal("§e⚠ Die Polizei sucht dich im Gebiet..."));
                } else {
                    System.out.println("[POLICE] " + npc.getNpcName() + " kann Spieler nicht finden oder Wanted-Level ist 0");
                }

                // Entferne Verfolgungsziel
                lastPursuitTarget.remove(npc.getUUID());

                // Cleanup arrestTimers falls vorhanden
                arrestTimers.remove(lastTarget);
            }
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

        // ═══════════════════════════════════════════════════════════
        // POLIZEI RAID - Scanne Umgebung nach illegalen Items
        // ═══════════════════════════════════════════════════════════
        IllegalActivityScanner.ScanResult scanResult = IllegalActivityScanner.scanArea(
            player.level(),
            player.blockPosition(),
            player
        );

        if (scanResult.hasIllegalActivity()) {
            // Illegale Aktivitäten gefunden!
            System.out.println("[RAID] Illegale Aktivitäten bei " + player.getName().getString() + " festgestellt!");
            System.out.println("[RAID] Pflanzen: " + scanResult.illegalPlantCount +
                ", Bargeld: " + scanResult.totalCashFound +
                ", Items: " + scanResult.illegalItemCount);

            // Wende Strafen an (Geldstrafe, Fahndungslevel-Erhöhung)
            PoliceRaidPenalty.applyPenalties(player, scanResult);

            // Upgrade zu Raid (max 4 Polizisten statt 2)
            PoliceBackupSystem.upgradeToRaid(player.getUUID());
            PoliceBackupSystem.registerPolice(player.getUUID(), police.getUUID(), true);

            // Rufe Verstärkung (bis zu 4 Polizisten total)
            PoliceBackupSystem.callBackup(player, police);

        } else {
            // Keine illegalen Items - normale Verfolgung (max 2 Polizisten)
            PoliceBackupSystem.registerPolice(player.getUUID(), police.getUUID(), false);
        }

        // Strafe berechnen
        int fine = wantedLevel * 500; // 500€ pro Stern
        int jailTimeSeconds = wantedLevel * 60; // 60 Sekunden pro Stern

        // Prüfe ob Raid-Strafe nicht bezahlt werden konnte (aus PoliceRaidPenalty)
        if (player.getPersistentData().getBoolean("DoublePenalty")) {
            jailTimeSeconds *= 2;
            player.getPersistentData().remove("DoublePenalty");
            System.out.println("[RAID] Haftzeit verdoppelt wegen unbezahlter Raid-Strafe");
        }

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

        // Cleanup Backup-System
        PoliceBackupSystem.cleanup(player.getUUID());

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
            boolean canHide = false;

            // Prüfe ob Spieler sich verstecken kann
            if (PoliceSearchBehavior.isPlayerHidingIndoors(player)) {
                // Spieler ist in Gebäude UND nicht am Fenster → kann sich verstecken
                canHide = true;
            } else if (minDistance > CrimeManager.ESCAPE_DISTANCE) {
                // Alternativ: Weit genug von Polizei entfernt
                canHide = true;
            }

            if (canHide) {
                // Spieler kann sich verstecken → Start Escape-Timer
                if (!CrimeManager.isHiding(player.getUUID())) {
                    CrimeManager.startEscapeTimer(player.getUUID(), currentTick);
                    if (PoliceSearchBehavior.isPlayerHidingIndoors(player)) {
                        player.sendSystemMessage(Component.literal("§e✓ Du versteckst dich im Gebäude..."));
                    } else {
                        player.sendSystemMessage(Component.literal("§e✓ Du versteckst dich vor der Polizei..."));
                    }
                }

                // Prüfe ob Escape erfolgreich
                if (CrimeManager.checkEscapeSuccess(player.getUUID(), currentTick)) {
                    player.sendSystemMessage(Component.literal("§a✓ Du bist entkommen! -1★"));
                }
            } else {
                // Spieler kann sich nicht verstecken → Stop Escape-Timer
                if (CrimeManager.isHiding(player.getUUID())) {
                    CrimeManager.stopEscapeTimer(player.getUUID());
                    if (PoliceSearchBehavior.isPlayerHidingIndoors(player) == false && minDistance <= CrimeManager.ESCAPE_DISTANCE) {
                        player.sendSystemMessage(Component.literal("§c✗ Polizei ist zu nah!"));
                    } else {
                        player.sendSystemMessage(Component.literal("§c✗ Polizei hat dich am Fenster entdeckt!"));
                    }
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
