package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.util.EventHelper;
import de.rolandsw.schedulemc.util.ConfigCache;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import de.rolandsw.schedulemc.npc.life.witness.WitnessManager;
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
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.phys.Vec3;

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

    private static final Logger LOGGER = LogUtils.getLogger();

    // Konstanten
    private static final double POLICE_SPEED = 1.2; // 20% schneller
    private static final long TICKS_PER_SECOND = 20L;
    private static final int AI_UPDATE_INTERVAL_TICKS = 20; // Alle 1 Sekunde
    private static final int FINE_PER_WANTED_LEVEL = 500;
    private static final int JAIL_SECONDS_PER_WANTED_LEVEL = 60;
    private static final int MAX_ARREST_TIMER_ENTRIES = 1000;
    private static final long ARREST_TIMER_TIMEOUT_MS = 600000; // 10 Minuten
    private static final int MAX_CACHE_ENTRIES = 500; // Max Einträge für LRU-Caches

    // UUID -> Arrest Start Time (in Ticks) - mit automatischem Cleanup
    private static final Map<UUID, Long> arrestTimers = new LinkedHashMap<UUID, Long>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Long> eldest) {
            return size() > MAX_ARREST_TIMER_ENTRIES;
        }
    };

    // NPC UUID -> Last Pursuit Target (um zu wissen wen wir verfolgt haben) - LRU Cache
    private static final Map<UUID, UUID> lastPursuitTarget = new LinkedHashMap<UUID, UUID>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, UUID> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };

    // Wanted-Level Sync Cache (verhindert unnötige Netzwerk-Pakete) - LRU Cache
    private static final Map<UUID, Integer> lastSyncedWantedLevel = new LinkedHashMap<UUID, Integer>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Integer> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };

    // Escape-Time Sync Cache (verhindert unnötige Netzwerk-Pakete) - LRU Cache
    private static final Map<UUID, Long> lastSyncedEscapeTime = new LinkedHashMap<UUID, Long>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Long> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };

    // ═══════════════════════════════════════════════════════════════════════════
    // OPTIMIERUNG: Globaler Spieler-Cache (aktualisiert alle 5 Ticks = 250ms)
    // Verhindert teure getEntitiesOfClass() Aufrufe für jeden Polizisten
    // Atomic swap statt clear()+put() verhindert Race Conditions
    // ═══════════════════════════════════════════════════════════════════════════
    private static volatile Map<UUID, CachedPlayerData> playerCache = Map.of();
    private static volatile long lastCacheUpdateTick = -1;
    private static final int CACHE_UPDATE_INTERVAL = 5; // Alle 5 Ticks (250ms) statt jeden Tick

    /**
     * Gecachte Spielerdaten für schnellen Zugriff
     */
    private static class CachedPlayerData {
        final ServerPlayer player;
        final Vec3 position;
        final int wantedLevel;

        CachedPlayerData(ServerPlayer player) {
            this.player = player;
            this.position = player.position();
            this.wantedLevel = CrimeManager.getWantedLevel(player.getUUID());
        }
    }

    /**
     * Aktualisiert den Spieler-Cache alle 5 Ticks (250ms).
     * Baut neuen Cache auf und tauscht ihn atomar aus (kein clear()+put() mehr).
     * Sollte vom Server-Tick-Handler aufgerufen werden.
     *
     * @param server Der Minecraft Server
     * @param currentTick Aktueller Game-Tick
     */
    public static void updatePlayerCache(net.minecraft.server.MinecraftServer server, long currentTick) {
        // Nur alle 5 Ticks aktualisieren (250ms statt 50ms)
        if (currentTick - lastCacheUpdateTick < CACHE_UPDATE_INTERVAL) return;
        lastCacheUpdateTick = currentTick;

        // Neuen Cache aufbauen und atomar tauschen (keine Race Condition möglich)
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        Map<UUID, CachedPlayerData> newCache = new HashMap<>(players.size() * 2);
        for (ServerPlayer player : players) {
            newCache.put(player.getUUID(), new CachedPlayerData(player));
        }
        playerCache = newCache;
    }

    /**
     * Findet Spieler im Radius einer Position (nutzt Cache statt Entity-Lookup)
     * O(n) mit n = Anzahl Online-Spieler (typisch 10-100) statt World-Entity-Scan
     *
     * @param center Zentrum der Suche
     * @param radius Suchradius
     * @return Liste von Spielern im Radius
     */
    private static List<ServerPlayer> getPlayersInRadius(Vec3 center, double radius) {
        List<ServerPlayer> result = new ArrayList<>();
        double radiusSq = radius * radius;

        for (CachedPlayerData data : playerCache.values()) {
            if (data.position.distanceToSqr(center) <= radiusSq) {
                result.add(data.player);
            }
        }

        return result;
    }

    /**
     * Holt gecachten Wanted-Level (vermeidet mehrfache CrimeManager-Aufrufe)
     */
    private static int getCachedWantedLevel(UUID playerUUID) {
        CachedPlayerData data = playerCache.get(playerUUID);
        return data != null ? data.wantedLevel : CrimeManager.getWantedLevel(playerUUID);
    }

    /**
     * Polizei-KI: Sucht Verbrecher und verfolgt sie
     */
    @SubscribeEvent
    public void onPoliceAI(LivingEvent.LivingTickEvent event) {
        EventHelper.handleLivingTick(event, () -> {
            if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
            if (npc.getNpcType() != NPCType.POLIZEI) return;

        // Prüfe ob Polizei knockout ist
        if (npc.getPersistentData().getBoolean("IsKnockedOut")) {
            return; // Keine AI während knockout
        }

        // Nur alle 20 Ticks (1 Sekunde) prüfen
        if (npc.tickCount % AI_UPDATE_INTERVAL_TICKS != 0) return;

        long currentTick = npc.level().getGameTime();

        // Lade Config-Werte (aus Cache für bessere Performance)
        int detectionRadius = ConfigCache.getPoliceDetectionRadius();
        double arrestDistance = ConfigCache.getPoliceArrestDistance();
        long arrestCooldown = ConfigCache.getPoliceArrestCooldownTicks();

        // Prüfe ob Polizei gerade sucht
        if (PoliceSearchBehavior.isSearching(npc)) {
            UUID searchTarget = PoliceSearchBehavior.getSearchTarget(npc);

            if (PoliceSearchBehavior.isSearchExpired(searchTarget, currentTick)) {
                // Suche abgebrochen
                PoliceSearchBehavior.stopSearch(npc, searchTarget);
                PoliceBackupSystem.unregisterPolice(searchTarget, npc.getUUID());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} has given up the search", npc.getNpcName());
                }
            } else {
                // Weiter suchen
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} continues search", npc.getNpcName());
                }
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
                highestWantedLevel = getCachedWantedLevel(assignedPlayer.getUUID()); // OPTIMIERT: Cache nutzen
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[BACKUP] {} pursues assigned target: {}",
                        npc.getNpcName(), assignedPlayer.getName().getString());
                }
            }
        }

        // Wenn kein zugewiesenes Ziel, suche Verbrecher in der Nähe
        if (targetCriminal == null) {
            // OPTIMIERT: Nutze gecachte Spieler-Positionen statt teuren Entity-Scan
            List<ServerPlayer> nearbyPlayers = getPlayersInRadius(npc.position(), detectionRadius);

            // Finde Spieler mit höchstem Wanted-Level
            for (ServerPlayer player : nearbyPlayers) {
                int wantedLevel = getCachedWantedLevel(player.getUUID()); // OPTIMIERT: Cache nutzen

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

                // SICHERHEIT: Atomare Operation verhindert Race Condition
                // wenn mehrere Polizisten gleichzeitig versuchen zu arrestieren
                Long previousStartTick = arrestTimers.putIfAbsent(playerUUID, currentTick);

                if (previousStartTick == null) {
                    // Neuer Arrest-Timer gestartet (wir waren erste)
                    int cooldownSeconds = ConfigCache.getPoliceArrestCooldownSeconds();
                    targetCriminal.sendSystemMessage(Component.translatable("event.police.arrest_in_progress", cooldownSeconds));
                } else {
                    // Timer läuft bereits - prüfe ob abgelaufen
                    long elapsed = currentTick - previousStartTick;

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
                                Component.translatable("event.police.arrest_in", remainingSeconds)
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
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[POLICE] {} - Player is hidden, switching to search mode", npc.getNpcName());
                    }

                    // Reset Timer falls vorhanden (atomare Operation)
                    if (arrestTimers.remove(playerUUID) != null) {
                        targetCriminal.sendSystemMessage(Component.translatable("event.police.escaped"));
                    }

                    // Starte Suchverhalten statt direkter Verfolgung
                    if (!PoliceSearchBehavior.isSearching(npc)) {
                        PoliceSearchBehavior.startSearch(npc, targetCriminal, currentTick);
                        targetCriminal.sendSystemMessage(Component.translatable("event.police.searching_area"));
                    }

                    // Speichere dass wir diesen Spieler verfolgen
                    lastPursuitTarget.put(npc.getUUID(), targetCriminal.getUUID());

                } else {
                    // Spieler ist NICHT versteckt - normale Verfolgung

                    // Reset Timer falls vorhanden (atomare Operation)
                    if (arrestTimers.remove(playerUUID) != null) {
                        targetCriminal.sendSystemMessage(Component.translatable("event.police.escaped"));
                    }

                    // Verfolge direkt
                    npc.getNavigation().moveTo(targetCriminal, POLICE_SPEED);

                    // Warnung alle 5 Sekunden
                    if (npc.tickCount % 100 == 0) {
                        targetCriminal.sendSystemMessage(
                            Component.translatable("event.police.stop")
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
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} has lost pursuit target, starting search...", npc.getNpcName());
                }

                // Starte Suchverhalten für verlorenen Spieler
                ServerPlayer lostPlayer = npc.level().getServer().getPlayerList().getPlayer(lastTarget);

                if (lostPlayer != null && CrimeManager.getWantedLevel(lastTarget) > 0) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[POLICE] {} starts search for {}", npc.getNpcName(), lostPlayer.getName().getString());
                    }
                    PoliceSearchBehavior.startSearch(npc, lostPlayer, currentTick);
                    lostPlayer.sendSystemMessage(Component.translatable("event.police.searching_area"));
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[POLICE] {} cannot find player or wanted level is 0", npc.getNpcName());
                    }
                }

                // Entferne Verfolgungsziel
                lastPursuitTarget.remove(npc.getUUID());

                // Cleanup arrestTimers falls vorhanden
                arrestTimers.remove(lastTarget);
            }
        }
        });
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
        // NPC LIFE SYSTEM INTEGRATION: WitnessManager informieren
        // ═══════════════════════════════════════════════════════════
        if (player.level() instanceof ServerLevel serverLevel) {
            WitnessManager witnessManager = WitnessManager.getManager(serverLevel);
            // Registriere die Verhaftung als offiziellen Bericht
            // CrimeType basierend auf Wanted-Level auswählen
            CrimeType arrestCrimeType = switch (wantedLevel) {
                case 1 -> CrimeType.PETTY_THEFT;
                case 2 -> CrimeType.SHOPLIFTING;
                case 3 -> CrimeType.ASSAULT;
                case 4 -> CrimeType.ROBBERY;
                default -> CrimeType.ARMED_VIOLENCE;
            };
            witnessManager.registerCrime(
                player,
                arrestCrimeType,
                player.blockPosition(),
                serverLevel,
                null  // kein spezifisches Opfer bei Verhaftung
            );

            // NPCLifeSystemIntegration: onCrimeWitnessed
            var integration = de.rolandsw.schedulemc.npc.life.NPCLifeSystemIntegration.get(serverLevel);
            integration.onCrimeWitnessed(player, arrestCrimeType, police);
        }

        // ═══════════════════════════════════════════════════════════
        // POLIZEI RAID - Scanne nach illegalen Items
        // Modus wird über Config gesteuert (POLICE_ROOM_SCAN_ENABLED):
        //   TRUE  = Intelligentes Raum-Scannen (nur gesehene Räume)
        //   FALSE = Klassisches Radius-Scannen (komplette Umgebung)
        // ═══════════════════════════════════════════════════════════
        boolean useRoomScan = ModConfigHandler.COMMON.POLICE_ROOM_SCAN_ENABLED.get();

        IllegalActivityScanner.ScanResult scanResult;
        if (useRoomScan) {
            // NEUE LOGIK: Nur Raum scannen, in dem Festnahme stattfand
            //             Wenn Konterband gefunden → weitere Räume durchsuchen
            //             Wenn nichts gefunden → KEINE Durchsuchung des ganzen Gebäudes
            scanResult = IllegalActivityScanner.scanRoomBased(
                player.level(),
                player.blockPosition(),
                player
            );
        } else {
            // ALTE LOGIK: Kompletter Radius wird gescannt (deprecated)
            scanResult = IllegalActivityScanner.scanArea(
                player.level(),
                player.blockPosition(),
                player
            );
        }

        if (scanResult.hasIllegalActivity()) {
            // Illegale Aktivitäten gefunden!
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[RAID] Illegale Aktivitäten bei {} festgestellt!", player.getName().getString());
                LOGGER.info("[RAID] Pflanzen: {}, Bargeld: {}, Items: {}",
                    scanResult.illegalPlantCount, scanResult.totalCashFound, scanResult.illegalItemCount);
            }

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
            LOGGER.warn("[RAID] Jail time doubled due to unpaid raid fine");
        }

        // Geld aus Wallet-Item abziehen
        ItemStack wallet = player.getInventory().getItem(8);
        if (wallet.getItem() instanceof CashItem) {
            double currentMoney = CashItem.getValue(wallet);

            if (currentMoney >= fine) {
                // Strafe bezahlen
                CashItem.removeValue(wallet, fine);
                player.sendSystemMessage(Component.translatable("event.police.arrested"));
                player.sendSystemMessage(Component.translatable("event.police.fine", fine));
            } else {
                // Nicht genug Geld → Gefängnis länger + alles konfisziert
                jailTimeSeconds *= 2;
                CashItem.setValue(wallet, 0);
                player.sendSystemMessage(Component.translatable("event.police.arrested"));
                player.sendSystemMessage(Component.translatable("event.police.cash_confiscated"));
                player.sendSystemMessage(Component.translatable("event.police.double_jail_time"));
            }
        }

        // Nutze PrisonManager für Inhaftierung
        de.rolandsw.schedulemc.npc.crime.prison.PrisonManager prisonManager =
            de.rolandsw.schedulemc.npc.crime.prison.PrisonManager.getInstance();

        // Versuche Spieler ins Gefängnis zu bringen
        boolean imprisoned = prisonManager.imprisonPlayer(player, wantedLevel);

        if (!imprisoned) {
            // Fallback: Alte Logik wenn kein Gefängnis verfügbar
            BlockPos jailPos = player.getRespawnPosition() != null ?
                player.getRespawnPosition() : BlockPos.ZERO;

            player.teleportTo(jailPos.getX(), jailPos.getY(), jailPos.getZ());

            long releaseTime = player.level().getGameTime() + (jailTimeSeconds * 20L);
            player.getPersistentData().putLong("JailReleaseTime", releaseTime);
            player.getPersistentData().putInt("JailX", jailPos.getX());
            player.getPersistentData().putInt("JailY", jailPos.getY());
            player.getPersistentData().putInt("JailZ", jailPos.getZ());

            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, jailTimeSeconds * 20, 2));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, jailTimeSeconds * 20, 250));

            CrimeManager.clearWantedLevel(player.getUUID());

            player.sendSystemMessage(Component.translatable("event.police.jail_time", jailTimeSeconds));
            player.sendSystemMessage(Component.translatable("event.police.wanted_level_reset"));
        }

        // Reset Arrest-Timer
        arrestTimers.remove(player.getUUID());

        // Cleanup Backup-System
        PoliceBackupSystem.cleanup(player.getUUID());

        LOGGER.info("[POLICE] {} arrested {} - Jail time: {}s",
            police.getNpcName(), player.getName().getString(), jailTimeSeconds);
    }

    /**
     * Gefängnis-System: Hält Spieler im Gefängnis
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EventHelper.handlePlayerTickEnd(event, p -> {
            if (!(p instanceof ServerPlayer)) return;
            ServerPlayer player = (ServerPlayer) p;

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

                player.sendSystemMessage(Component.translatable("event.police.freed"));
                player.sendSystemMessage(Component.translatable("event.police.obey_law"));

                // Entferne Effekte
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                player.removeEffect(MobEffects.JUMP);

                LOGGER.info("[JAIL] Player {} released", player.getName().getString());
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
                    player.sendSystemMessage(Component.translatable("event.police.escape_prevented"));
                }

                // Zeit anzeigen alle 10 Sekunden
                if (currentTime % 200 == 0) {
                    long remainingTicks = releaseTime - currentTime;
                    long remainingSeconds = remainingTicks / 20;

                    player.sendSystemMessage(Component.translatable("event.police.time_remaining", remainingSeconds));
                }
            }
        }

        // ═══════════════════════════════════════════
        // ESCAPE SYSTEM (Verstecken vor Polizei)
        // ═══════════════════════════════════════════
        int wantedLevel = CrimeManager.getWantedLevel(player.getUUID());
        if (wantedLevel > 0) {
            long currentTick = player.level().getGameTime();

            // Finde nächste Polizei (OPTIMIERT: konfigurierbar statt hardcoded 100)
            int backupSearchRadius = ModConfigHandler.COMMON.POLICE_BACKUP_SEARCH_RADIUS.get();
            List<CustomNPCEntity> nearbyPolice = player.level().getEntitiesOfClass(
                CustomNPCEntity.class,
                AABB.ofSize(player.position(), backupSearchRadius, backupSearchRadius, backupSearchRadius),
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
            if (!nearbyPolice.isEmpty()) {
                // Es gibt Polizei in der Nähe - prüfe ob Spieler vor ALLEN versteckt ist
                boolean hiddenFromAll = true;

                for (CustomNPCEntity police : nearbyPolice) {
                    // isPlayerHidden gibt FALSE zurück wenn Polizei den Spieler SEHEN kann
                    if (!PoliceSearchBehavior.isPlayerHidden(player, police)) {
                        // Diese Polizei kann den Spieler sehen (z.B. draußen oder durch Fenster)
                        hiddenFromAll = false;
                        break;
                    }
                }

                if (hiddenFromAll) {
                    // Spieler ist vor ALLER Polizei versteckt (im Gebäude, nicht am Fenster)
                    canHide = true;
                }
            } else if (minDistance > CrimeManager.ESCAPE_DISTANCE) {
                // Keine Polizei in der Nähe UND weit genug entfernt
                canHide = true;
            }

            if (canHide) {
                // Spieler kann sich verstecken → Start Escape-Timer
                if (!CrimeManager.isHiding(player.getUUID())) {
                    CrimeManager.startEscapeTimer(player.getUUID(), currentTick);
                    player.sendSystemMessage(Component.translatable("event.police.hiding"));
                }

                // Prüfe ob Escape erfolgreich
                if (CrimeManager.checkEscapeSuccess(player.getUUID(), currentTick)) {
                    player.sendSystemMessage(Component.translatable("event.police.escaped_star"));
                }
            } else {
                // Spieler kann sich nicht verstecken → Stop Escape-Timer
                if (CrimeManager.isHiding(player.getUUID())) {
                    CrimeManager.stopEscapeTimer(player.getUUID());

                    boolean isIndoors = PoliceSearchBehavior.isPlayerHidingIndoors(player);
                    if (isIndoors) {
                        // Im Gebäude aber am Fenster sichtbar
                        player.sendSystemMessage(Component.translatable("event.police.discovered_window"));
                    } else if (minDistance <= CrimeManager.ESCAPE_DISTANCE) {
                        // Draußen und Polizei zu nah
                        player.sendSystemMessage(Component.translatable("event.police.too_close"));
                    } else {
                        // Draußen und sichtbar
                        player.sendSystemMessage(Component.translatable("event.police.spotted"));
                    }
                }
            }

            // Sync zu Client (für HUD Overlay) - NUR wenn sich Wert geändert hat!
            long escapeTime = CrimeManager.getEscapeTimeRemaining(player.getUUID(), currentTick);
            int lastSyncedLevel = lastSyncedWantedLevel.getOrDefault(player.getUUID(), -1);
            long lastSyncedTime = lastSyncedEscapeTime.getOrDefault(player.getUUID(), -1L);

            // Sync wenn sich Wanted-Level ODER Escape-Time geändert hat
            if (wantedLevel != lastSyncedLevel || escapeTime != lastSyncedTime) {
                NPCNetworkHandler.sendToPlayer(new WantedLevelSyncPacket(wantedLevel, escapeTime), player);
                lastSyncedWantedLevel.put(player.getUUID(), wantedLevel);
                lastSyncedEscapeTime.put(player.getUUID(), escapeTime);
            }
        } else {
            // Kein Wanted-Level → cleanup und sync 0 zum Client (nur einmal)
            if (lastSyncedWantedLevel.getOrDefault(player.getUUID(), -1) != 0) {
                arrestTimers.remove(player.getUUID());
                NPCNetworkHandler.sendToPlayer(new WantedLevelSyncPacket(0, 0), player);
                lastSyncedWantedLevel.put(player.getUUID(), 0);
                lastSyncedEscapeTime.put(player.getUUID(), 0L);
            }
        }
        });
    }

    /**
     * Bereinigt alle Caches für einen Spieler (aufrufen bei Logout).
     * Verhindert Memory Leaks bei langen Server-Laufzeiten.
     *
     * @param playerUUID UUID des Spielers der den Server verlässt
     */
    public static void cleanupPlayer(UUID playerUUID) {
        // playerCache wird nicht manuell bereinigt - der atomare Swap alle 5 Ticks
        // entfernt abgemeldete Spieler automatisch beim nächsten Rebuild
        arrestTimers.remove(playerUUID);
        lastSyncedWantedLevel.remove(playerUUID);
        lastSyncedEscapeTime.remove(playerUUID);
        // lastPursuitTarget verwendet NPC-UUIDs als Key, nicht Spieler-UUIDs
        // Daher hier kein Cleanup nötig
        LOGGER.debug("[POLICE] Cleaned up caches for player {}", playerUUID);
    }

    /**
     * Bereinigt alle Caches für einen NPC (aufrufen bei NPC-Entfernung).
     *
     * @param npcUUID UUID des NPCs der entfernt wird
     */
    public static void cleanupNPC(UUID npcUUID) {
        lastPursuitTarget.remove(npcUUID);
        LOGGER.debug("[POLICE] Cleaned up pursuit target for NPC {}", npcUUID);
    }
}
