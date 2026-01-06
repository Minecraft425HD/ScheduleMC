package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.util.EventHelper;
import de.rolandsw.schedulemc.util.ConfigCache;
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
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import de.rolandsw.schedulemc.util.LRUCache;
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

    /**
     * UUID → Arrest Start Time (in Ticks) - LRU-Cache mit automatischem Cleanup.
     * <p>Max 1000 Einträge, älteste werden automatisch entfernt.</p>
     */
    private static final Map<UUID, Long> arrestTimers = new LRUCache<>(MAX_ARREST_TIMER_ENTRIES);

    /**
     * NPC UUID → Last Pursuit Target (letztes Verfolgungsziel) - LRU Cache.
     * <p>Max 500 Einträge für Performance.</p>
     */
    private static final Map<UUID, UUID> lastPursuitTarget = new LRUCache<>(MAX_CACHE_ENTRIES);

    /**
     * Wanted-Level Sync Cache (verhindert unnötige Netzwerk-Pakete) - LRU Cache.
     * <p>Max 500 Einträge.</p>
     */
    private static final Map<UUID, Integer> lastSyncedWantedLevel = new LRUCache<>(MAX_CACHE_ENTRIES);

    /**
     * Escape-Time Sync Cache (verhindert unnötige Netzwerk-Pakete) - LRU Cache.
     * <p>Max 500 Einträge.</p>
     */
    private static final Map<UUID, Long> lastSyncedEscapeTime = new LRUCache<>(MAX_CACHE_ENTRIES);

    // ═══════════════════════════════════════════════════════════════════════════
    // OPTIMIERUNG: Globaler Spieler-Cache (aktualisiert einmal pro Server-Tick)
    // Verhindert teure getEntitiesOfClass() Aufrufe für jeden Polizisten
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Map<UUID, CachedPlayerData> playerCache = new ConcurrentHashMap<>();
    private static long lastCacheUpdateTick = -1;

    // PERFORMANCE: Spatial Hash Grid für O(1) Spieler-Lookup bei 50+ Spielern
    // Grid-Size: 64 Blöcke (größer als RoadGraph wegen größeren Suchradien)
    private static final int SPATIAL_GRID_SIZE = 64;
    private static final Map<String, List<CachedPlayerData>> playerSpatialGrid = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════════════════════
    // OPTIMIERUNG: Globaler NPC-Cache (aktualisiert einmal pro Server-Tick)
    // Verhindert teure getEntitiesOfClass() Aufrufe in onPlayerTick()
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Map<UUID, CachedNPCData> npcCache = new ConcurrentHashMap<>();
    private static long lastNPCCacheUpdateTick = -1;

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
     * Gecachte NPC-Daten für schnellen Zugriff
     */
    private static class CachedNPCData {
        final CustomNPCEntity npc;
        final Vec3 position;
        final boolean isKnockedOut;

        CachedNPCData(CustomNPCEntity npc) {
            this.npc = npc;
            this.position = npc.position();
            this.isKnockedOut = npc.getPersistentData().getBoolean("IsKnockedOut");
        }
    }

    /**
     * Aktualisiert den Spieler-Cache einmal pro Server-Tick.
     * Sollte vom Server-Tick-Handler aufgerufen werden.
     *
     * PERFORMANCE: Baut auch Spatial Hash Grid für schnelle Radius-Suchen
     *
     * @param server Der Minecraft Server
     * @param currentTick Aktueller Game-Tick
     */
    public static void updatePlayerCache(net.minecraft.server.MinecraftServer server, long currentTick) {
        // Nur einmal pro Tick aktualisieren
        if (currentTick == lastCacheUpdateTick) return;
        lastCacheUpdateTick = currentTick;

        // Alte Einträge entfernen und neue hinzufügen
        playerCache.clear();
        playerSpatialGrid.clear(); // PERFORMANCE: Spatial Grid leeren

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CachedPlayerData data = new CachedPlayerData(player);
            playerCache.put(player.getUUID(), data);

            // PERFORMANCE: Füge zu Spatial Grid hinzu
            String cellKey = getSpatialCellKey(data.position);
            playerSpatialGrid.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(data);
        }
    }

    /**
     * PERFORMANCE: Berechnet Spatial Grid Zellen-Key für Position
     */
    private static String getSpatialCellKey(Vec3 pos) {
        int cellX = Math.floorDiv((int) pos.x, SPATIAL_GRID_SIZE);
        int cellZ = Math.floorDiv((int) pos.z, SPATIAL_GRID_SIZE);
        return cellX + "," + cellZ;
    }

    /**
     * Aktualisiert den NPC-Cache einmal pro Server-Tick.
     * Sollte vom Server-Tick-Handler aufgerufen werden.
     *
     * @param server Der Minecraft Server
     * @param currentTick Aktueller Game-Tick
     */
    public static void updateNPCCache(net.minecraft.server.MinecraftServer server, long currentTick) {
        // Nur einmal pro Tick aktualisieren
        if (currentTick == lastNPCCacheUpdateTick) return;
        lastNPCCacheUpdateTick = currentTick;

        // Alte Einträge entfernen und neue hinzufügen
        npcCache.clear();
        for (ServerLevel level : server.getAllLevels()) {
            // Sammle alle Polizei-NPCs in dieser Welt
            for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                if (entity instanceof CustomNPCEntity npc && npc.getNpcType() == NPCType.POLIZEI) {
                    npcCache.put(npc.getUUID(), new CachedNPCData(npc));
                }
            }
        }
    }

    /**
     * Findet Spieler im Radius einer Position (nutzt Spatial Hash Grid)
     *
     * PERFORMANCE: Spatial Hash Grid Optimierung
     * - Vorher: O(n) mit n = Alle Online-Spieler (10-100)
     * - Jetzt: O(1) Zellen-Lookup + O(k) mit k = Spieler in Nachbarzellen (~2-10)
     * - Bei 50+ Spielern: 5-10x schneller!
     *
     * @param center Zentrum der Suche
     * @param radius Suchradius
     * @return Liste von Spielern im Radius
     */
    private static List<ServerPlayer> getPlayersInRadius(Vec3 center, double radius) {
        List<ServerPlayer> result = new ArrayList<>();
        double radiusSq = radius * radius;

        // PERFORMANCE: Berechne welche Grid-Zellen wir durchsuchen müssen
        int centerCellX = Math.floorDiv((int) center.x, SPATIAL_GRID_SIZE);
        int centerCellZ = Math.floorDiv((int) center.z, SPATIAL_GRID_SIZE);

        // Berechne wie viele Zellen wir in jede Richtung prüfen müssen
        int cellRadius = (int) Math.ceil(radius / SPATIAL_GRID_SIZE);

        // Durchsuche nur relevante Grid-Zellen (3x3 bis 5x5 je nach Radius)
        for (int dx = -cellRadius; dx <= cellRadius; dx++) {
            for (int dz = -cellRadius; dz <= cellRadius; dz++) {
                String cellKey = (centerCellX + dx) + "," + (centerCellZ + dz);
                List<CachedPlayerData> cellPlayers = playerSpatialGrid.get(cellKey);

                if (cellPlayers != null) {
                    for (CachedPlayerData data : cellPlayers) {
                        if (data.position.distanceToSqr(center) <= radiusSq) {
                            result.add(data.player);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Findet Polizei-NPCs im Radius einer Position (nutzt Cache statt Entity-Lookup)
     * O(n) mit n = Anzahl Polizei-NPCs (typisch 10-50) statt World-Entity-Scan
     *
     * @param center Zentrum der Suche
     * @param radius Suchradius
     * @return Liste von Polizei-NPCs im Radius
     */
    private static List<CustomNPCEntity> getPoliceInRadius(Vec3 center, double radius) {
        List<CustomNPCEntity> result = new ArrayList<>();
        double radiusSq = radius * radius;

        for (CachedNPCData data : npcCache.values()) {
            if (!data.isKnockedOut && data.position.distanceToSqr(center) <= radiusSq) {
                result.add(data.npc);
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
    /**
     * Police AI event handler - manages police NPC behavior for pursuing and arresting criminals.
     * <p>
     * This method has been refactored to use extracted helper methods for better maintainability:
     * <ul>
     *   <li>{@link #handleSearchBehavior(CustomNPCEntity, long)} - Manages active search behavior</li>
     *   <li>{@link #findTargetCriminal(CustomNPCEntity, int)} - Finds target criminal (backup or nearby)</li>
     *   <li>{@link #handleArrestRange(CustomNPCEntity, ServerPlayer, long, long)} - Arrest timer logic</li>
     *   <li>{@link #handlePursuitRange(CustomNPCEntity, ServerPlayer, long)} - Pursuit logic (hidden vs visible)</li>
     *   <li>{@link #handleLostTarget(CustomNPCEntity, long)} - Lost target handling</li>
     * </ul>
     * </p>
     */
    public void onPoliceAI(LivingEvent.LivingTickEvent event) {
        EventHelper.handleLivingTick(event, () -> {
            if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
            if (npc.getNpcType() != NPCType.POLIZEI) return;

            // Check if police is knocked out
            if (npc.getPersistentData().getBoolean("IsKnockedOut")) {
                return; // No AI during knockout
            }

            // Only check every 20 ticks (1 second)
            if (npc.tickCount % AI_UPDATE_INTERVAL_TICKS != 0) return;

            long currentTick = npc.level().getGameTime();

            // Load config values (from cache for better performance)
            int detectionRadius = ConfigCache.getPoliceDetectionRadius();
            double arrestDistance = ConfigCache.getPoliceArrestDistance();
            long arrestCooldown = ConfigCache.getPoliceArrestCooldownTicks();

            // Handle active search behavior using helper method
            if (PoliceSearchBehavior.isSearching(npc)) {
                handleSearchBehavior(npc, currentTick);
            }

            // Find target criminal using helper method
            ServerPlayer targetCriminal = findTargetCriminal(npc, detectionRadius);

            if (targetCriminal != null) {
                // Criminal found! Stop search behavior if active
                if (PoliceSearchBehavior.isSearching(npc)) {
                    PoliceSearchBehavior.stopSearch(npc, targetCriminal.getUUID());
                }

                // Remember that we're pursuing this player
                lastPursuitTarget.put(npc.getUUID(), targetCriminal.getUUID());

                // Pursue criminal
                double distance = npc.distanceTo(targetCriminal);

                if (distance < arrestDistance) {
                    // In arrest range - use helper method
                    handleArrestRange(npc, targetCriminal, currentTick, arrestCooldown);
                } else {
                    // Outside arrest range (pursuit) - use helper method
                    handlePursuitRange(npc, targetCriminal, currentTick);
                }
            } else if (!PoliceSearchBehavior.isSearching(npc)) {
                // No criminal in sight - use helper method
                handleLostTarget(npc, currentTick);
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
            LOGGER.warn("[RAID] Haftzeit verdoppelt wegen unbezahlter Raid-Strafe");
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

            player.sendSystemMessage(Component.literal("§7Haftzeit: §e" + jailTimeSeconds + " Sekunden"));
            player.sendSystemMessage(Component.literal("§7Fahndungsstufe zurückgesetzt"));
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

                player.sendSystemMessage(Component.literal("§a✓ Du bist frei!"));
                player.sendSystemMessage(Component.literal("§7Halte dich von nun an an das Gesetz."));

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

            // Finde nächste Polizei (OPTIMIERT: Nutzt Cache statt getEntitiesOfClass)
            int backupSearchRadius = ModConfigHandler.COMMON.POLICE_BACKUP_SEARCH_RADIUS.get();
            List<CustomNPCEntity> nearbyPolice = getPoliceInRadius(player.position(), backupSearchRadius);

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
                    player.sendSystemMessage(Component.literal("§e✓ Du versteckst dich vor der Polizei..."));
                }

                // Prüfe ob Escape erfolgreich
                if (CrimeManager.checkEscapeSuccess(player.getUUID(), currentTick)) {
                    player.sendSystemMessage(Component.literal("§a✓ Du bist entkommen! -1★"));
                }
            } else {
                // Spieler kann sich nicht verstecken → Stop Escape-Timer
                if (CrimeManager.isHiding(player.getUUID())) {
                    CrimeManager.stopEscapeTimer(player.getUUID());

                    boolean isIndoors = PoliceSearchBehavior.isPlayerHidingIndoors(player);
                    if (isIndoors) {
                        // Im Gebäude aber am Fenster sichtbar
                        player.sendSystemMessage(Component.literal("§c✗ Polizei hat dich am Fenster entdeckt!"));
                    } else if (minDistance <= CrimeManager.ESCAPE_DISTANCE) {
                        // Draußen und Polizei zu nah
                        player.sendSystemMessage(Component.literal("§c✗ Polizei ist zu nah!"));
                    } else {
                        // Draußen und sichtbar
                        player.sendSystemMessage(Component.literal("§c✗ Polizei hat dich gesehen!"));
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

    // ==================== Police AI Helper Methods (Refactored from onPoliceAI) ====================

    /**
     * Handles active search behavior for a police NPC.
     *
     * @param npc the police NPC
     * @param currentTick the current game tick
     */
    private void handleSearchBehavior(CustomNPCEntity npc, long currentTick) {
        UUID searchTarget = PoliceSearchBehavior.getSearchTarget(npc);

        if (PoliceSearchBehavior.isSearchExpired(searchTarget, currentTick)) {
            // Search abandoned
            PoliceSearchBehavior.stopSearch(npc, searchTarget);
            PoliceBackupSystem.unregisterPolice(searchTarget, npc.getUUID());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[POLICE] {} hat die Suche aufgegeben", npc.getNpcName());
            }
        } else {
            // Continue searching
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[POLICE] {} führt Suche fort", npc.getNpcName());
            }
            PoliceSearchBehavior.searchArea(npc, searchTarget, currentTick);
        }
    }

    /**
     * Finds the target criminal for a police NPC.
     * Checks assigned backup target first, then searches nearby players.
     *
     * @param npc the police NPC
     * @param detectionRadius the detection radius
     * @return the target criminal, or null if none found
     */
    @javax.annotation.Nullable
    private ServerPlayer findTargetCriminal(CustomNPCEntity npc, int detectionRadius) {
        ServerPlayer targetCriminal = null;
        int highestWantedLevel = 0;

        // Check if this police has an assigned target (Backup System)
        UUID assignedTarget = PoliceBackupSystem.getAssignedTarget(npc.getUUID());
        if (assignedTarget != null) {
            ServerPlayer assignedPlayer = npc.level().getServer().getPlayerList().getPlayer(assignedTarget);
            if (assignedPlayer != null && !PoliceSearchBehavior.isPlayerHidden(assignedPlayer, npc)) {
                targetCriminal = assignedPlayer;
                highestWantedLevel = getCachedWantedLevel(assignedPlayer.getUUID());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[BACKUP] {} verfolgt zugewiesenes Ziel: {}",
                        npc.getNpcName(), assignedPlayer.getName().getString());
                }
            }
        }

        // If no assigned target, search for criminals nearby
        if (targetCriminal == null) {
            List<ServerPlayer> nearbyPlayers = getPlayersInRadius(npc.position(), detectionRadius);

            // Find player with highest wanted level
            for (ServerPlayer player : nearbyPlayers) {
                int wantedLevel = getCachedWantedLevel(player.getUUID());

                if (wantedLevel > 0 && wantedLevel > highestWantedLevel) {
                    // Check if player is hidden
                    if (!PoliceSearchBehavior.isPlayerHidden(player, npc)) {
                        targetCriminal = player;
                        highestWantedLevel = wantedLevel;
                    }
                }
            }
        }

        return targetCriminal;
    }

    /**
     * Handles arrest range logic when police is close enough to arrest.
     *
     * @param npc the police NPC
     * @param targetCriminal the target criminal
     * @param currentTick the current game tick
     * @param arrestCooldown the arrest cooldown in ticks
     */
    private void handleArrestRange(CustomNPCEntity npc, ServerPlayer targetCriminal, long currentTick, long arrestCooldown) {
        UUID playerUUID = targetCriminal.getUUID();

        // Atomic operation prevents race condition when multiple police try to arrest simultaneously
        Long previousStartTick = arrestTimers.putIfAbsent(playerUUID, currentTick);

        if (previousStartTick == null) {
            // New arrest timer started (we were first)
            int cooldownSeconds = ConfigCache.getPoliceArrestCooldownSeconds();
            targetCriminal.sendSystemMessage(Component.literal("§c⚠ FESTNAHME läuft... " + cooldownSeconds + "s"));
        } else {
            // Timer already running - check if expired
            long elapsed = currentTick - previousStartTick;

            if (elapsed >= arrestCooldown) {
                // Cooldown finished → ARREST!
                arrestPlayer(npc, targetCriminal);
                arrestTimers.remove(playerUUID);
            } else {
                // Show remaining time (every second)
                long remainingTicks = arrestCooldown - elapsed;
                int remainingSeconds = (int) Math.ceil(remainingTicks / 20.0);

                if (elapsed % 20 == 0) { // Every second
                    targetCriminal.sendSystemMessage(
                        Component.literal("§c⚠ FESTNAHME in " + remainingSeconds + "s...")
                    );
                }
            }
        }
    }

    /**
     * Handles pursuit range logic when police is chasing but not close enough to arrest.
     *
     * @param npc the police NPC
     * @param targetCriminal the target criminal
     * @param currentTick the current game tick
     */
    private void handlePursuitRange(CustomNPCEntity npc, ServerPlayer targetCriminal, long currentTick) {
        UUID playerUUID = targetCriminal.getUUID();

        // Check if player is hidden (in building)
        boolean isHidden = PoliceSearchBehavior.isPlayerHidden(targetCriminal, npc);

        if (isHidden) {
            // Player is hidden - DON'T follow into building!
            // Instead: search area / wait outside building
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[POLICE] {} - Spieler ist versteckt, wechsle zu Suchmodus", npc.getNpcName());
            }

            // Reset timer if exists (atomic operation)
            if (arrestTimers.remove(playerUUID) != null) {
                targetCriminal.sendSystemMessage(Component.literal("§e✓ Du bist entkommen!"));
            }

            // Start search behavior instead of direct pursuit
            if (!PoliceSearchBehavior.isSearching(npc)) {
                PoliceSearchBehavior.startSearch(npc, targetCriminal, currentTick);
                targetCriminal.sendSystemMessage(Component.literal("§e⚠ Die Polizei sucht dich im Gebiet..."));
            }

            // Remember that we're pursuing this player
            lastPursuitTarget.put(npc.getUUID(), targetCriminal.getUUID());

        } else {
            // Player is NOT hidden - normal pursuit

            // Reset timer if exists (atomic operation)
            if (arrestTimers.remove(playerUUID) != null) {
                targetCriminal.sendSystemMessage(Component.literal("§e✓ Du bist entkommen!"));
            }

            // Chase directly
            npc.getNavigation().moveTo(targetCriminal, POLICE_SPEED);

            // Warning every 5 seconds
            if (npc.tickCount % 100 == 0) {
                targetCriminal.sendSystemMessage(
                    Component.literal("§c⚠ POLIZEI! Bleib stehen!")
                );
            }
        }
    }

    /**
     * Handles lost target scenario when police loses sight of criminal.
     *
     * @param npc the police NPC
     * @param currentTick the current game tick
     */
    private void handleLostTarget(CustomNPCEntity npc, long currentTick) {
        UUID lastTarget = lastPursuitTarget.get(npc.getUUID());

        if (lastTarget != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[POLICE] {} hat Verfolgungsziel verloren, starte Suche...", npc.getNpcName());
            }

            // Start search behavior for lost player
            ServerPlayer lostPlayer = npc.level().getServer().getPlayerList().getPlayer(lastTarget);

            if (lostPlayer != null && CrimeManager.getWantedLevel(lastTarget) > 0) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} startet Suche nach {}", npc.getNpcName(), lostPlayer.getName().getString());
                }
                PoliceSearchBehavior.startSearch(npc, lostPlayer, currentTick);
                lostPlayer.sendSystemMessage(Component.literal("§e⚠ Die Polizei sucht dich im Gebiet..."));
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} kann Spieler nicht finden oder Wanted-Level ist 0", npc.getNpcName());
                }
            }

            // Remove pursuit target
            lastPursuitTarget.remove(npc.getUUID());

            // Cleanup arrestTimers if exists
            arrestTimers.remove(lastTarget);
        }
    }
}
