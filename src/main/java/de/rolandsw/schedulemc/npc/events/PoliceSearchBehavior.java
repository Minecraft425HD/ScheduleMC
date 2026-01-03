package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Polizei-Such- und Versteck-System
 * SICHERHEIT: Thread-safe Collections für parallele Event-Handler Zugriffe
 *
 * Features:
 * - Indoor-Versteck-Erkennung (Spieler kann sich in Gebäuden verstecken)
 * - Sichtlinien-Checks für Fenster und durchsichtige Türen
 * - Polizei sucht in der Gegend, wo Spieler zuletzt gesehen wurde
 */
public class PoliceSearchBehavior {

    private static final Logger LOGGER = LogUtils.getLogger();

    // SICHERHEIT: ConcurrentHashMap für Thread-Safety (EventBus + ServerTick)
    // UUID -> Last Known Position
    private static final Map<UUID, BlockPos> lastKnownPositions = new ConcurrentHashMap<>();

    // UUID -> Movement Direction (Vec3 als String gespeichert: "x,y,z")
    private static final Map<UUID, String> movementDirections = new ConcurrentHashMap<>();

    // UUID -> Search Start Time (in Ticks)
    private static final Map<UUID, Long> searchTimers = new ConcurrentHashMap<>();

    // NPC UUID -> Target Player UUID
    private static final Map<UUID, UUID> activeSearches = new ConcurrentHashMap<>();

    // NPC UUID -> Last Search Target Update Time
    private static final Map<UUID, Long> lastTargetUpdate = new ConcurrentHashMap<>();

    /**
     * Prüft, ob ein Spieler sich erfolgreich vor der Polizei versteckt
     *
     * @param player Der Spieler
     * @param police Die Polizei-NPC
     * @return true, wenn der Spieler versteckt ist und nicht gesehen werden kann
     */
    public static boolean isPlayerHidden(ServerPlayer player, CustomNPCEntity police) {
        if (!ModConfigHandler.COMMON.POLICE_INDOOR_HIDING_ENABLED.get()) {
            return false; // Feature deaktiviert
        }

        // Prüfe ob Spieler in einem Gebäude ist
        if (!isPlayerIndoors(player)) {
            return false; // Spieler ist draußen
        }

        // Prüfe Sichtlinie (Line of Sight)
        return !hasLineOfSight(police, player);
    }

    /**
     * Prüft ob Spieler sich in einem Gebäude versteckt (für Escape-Timer)
     * Gibt true zurück wenn Spieler indoor ist UND nicht am Fenster steht
     */
    public static boolean isPlayerHidingIndoors(ServerPlayer player) {
        if (!ModConfigHandler.COMMON.POLICE_INDOOR_HIDING_ENABLED.get()) {
            return false;
        }

        // Prüfe ob Spieler in einem Gebäude ist
        if (!isPlayerIndoors(player)) {
            return false;
        }

        // Prüfe ob Spieler am Fenster steht
        return !isPlayerNearWindow(player);
    }

    /**
     * Prüft ob Spieler nah an einem Fenster/transparenten Block steht
     */
    private static boolean isPlayerNearWindow(ServerPlayer player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // Prüfe Blöcke im 2-Block-Radius um den Spieler
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    Block block = state.getBlock();

                    if (isTransparentBlock(block)) {
                        return true; // Fenster in der Nähe
                    }
                }
            }
        }

        return false; // Kein Fenster in der Nähe
    }

    /**
     * Prüft, ob ein Spieler sich in einem Gebäude befindet
     */
    private static boolean isPlayerIndoors(ServerPlayer player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // Prüfe ob über dem Spieler ein Dach ist (innerhalb von 10 Blöcken)
        boolean hasRoof = false;
        for (int y = 1; y <= 10; y++) {
            BlockPos checkPos = playerPos.above(y);
            BlockState state = level.getBlockState(checkPos);

            if (!state.isAir() && state.isRedstoneConductor(level, checkPos)) {
                hasRoof = true;
                break;
            }
        }

        if (!hasRoof) {
            return false; // Kein Dach gefunden
        }

        // Prüfe ob Spieler von Wänden umgeben ist (mindestens 2 Seiten)
        int wallCount = 0;
        BlockPos[] checkPositions = {
            playerPos.north(),
            playerPos.south(),
            playerPos.east(),
            playerPos.west()
        };

        for (BlockPos wallPos : checkPositions) {
            BlockState state = level.getBlockState(wallPos);
            if (!state.isAir() && state.isRedstoneConductor(level, wallPos)) {
                wallCount++;
            }
        }

        // Mindestens 2 Wände = in einem Gebäude
        return wallCount >= 2;
    }

    /**
     * Prüft, ob die Polizei direkte Sichtlinie zum Spieler hat
     * Berücksichtigt Fenster (Glas) und durchsichtige Türen
     */
    private static boolean hasLineOfSight(CustomNPCEntity police, ServerPlayer player) {
        Vec3 policeEye = police.getEyePosition(1.0f);
        Vec3 playerEye = player.getEyePosition(1.0f);

        // Raycast von Polizei zu Spieler
        ClipContext context = new ClipContext(
            policeEye,
            playerEye,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            police
        );

        BlockHitResult hitResult = police.level().clip(context);

        // Wenn kein Block getroffen wurde, freie Sichtlinie
        if (hitResult.getType() == HitResult.Type.MISS) {
            return true;
        }

        // Prüfe ob der getroffene Block transparent ist (Glas, Türen, etc.)
        BlockPos hitPos = hitResult.getBlockPos();
        BlockState hitState = police.level().getBlockState(hitPos);
        Block hitBlock = hitState.getBlock();

        // Fenster und durchsichtige Blöcke erlauben Sicht
        if (isTransparentBlock(hitBlock)) {
            // Prüfe ob Spieler direkt am Fenster/Tür steht (innerhalb 2 Blöcke)
            double distanceToWindow = player.position().distanceTo(
                new Vec3(hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5)
            );

            return distanceToWindow <= 2.0; // Nur sichtbar wenn nah am Fenster
        }

        // Fester Block blockiert Sicht
        return false;
    }

    /**
     * Prüft, ob ein Block transparent ist (Fenster, Glas-Türen, etc.)
     */
    private static boolean isTransparentBlock(Block block) {
        // Prüfe bekannte transparente Block-Typen
        if (block instanceof GlassBlock ||
            block instanceof StainedGlassBlock ||
            block instanceof TintedGlassBlock ||
            block instanceof IronBarsBlock) {
            return true;
        }

        // Prüfe ob Block-Name "glass" oder "pane" enthält (für Glasscheiben)
        String blockName = block.getDescriptionId().toLowerCase();
        if (blockName.contains("glass") || blockName.contains("pane")) {
            return true;
        }

        // Prüfe Türen
        if (block instanceof DoorBlock && isTransparentDoor(block)) {
            return true;
        }

        return false;
    }

    /**
     * Prüft, ob eine Tür durchsichtig ist (Glas-Türen)
     */
    private static boolean isTransparentDoor(Block block) {
        // In Vanilla Minecraft gibt es keine Glas-Türen, aber Mods könnten welche haben
        // Prüfe ob der Block-Name "glass" enthält
        String blockName = block.getDescriptionId().toLowerCase();
        return blockName.contains("glass") || blockName.contains("window");
    }

    /**
     * Startet Suchverhalten für eine Polizei-NPC
     * Polizei sucht in der Gegend, wo der Spieler zuletzt gesehen wurde
     */
    public static void startSearch(CustomNPCEntity police, ServerPlayer player, long currentTick) {
        UUID playerUUID = player.getUUID();
        UUID policeUUID = police.getUUID();

        // Speichere letzte bekannte Position
        lastKnownPositions.put(playerUUID, player.blockPosition());
        searchTimers.put(playerUUID, currentTick);
        activeSearches.put(policeUUID, playerUUID);

        // Speichere Bewegungsrichtung des Spielers
        Vec3 movement = player.getDeltaMovement();
        if (movement.lengthSqr() > 0.01) { // Nur wenn sich Spieler bewegt
            String direction = movement.x + "," + movement.y + "," + movement.z;
            movementDirections.put(playerUUID, direction);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[POLICE] {} startet Suche nach Spieler {} bei {} in Richtung {}",
                    police.getNpcName(), player.getName().getString(), player.blockPosition(), movement);
            }
        } else {
            // Spieler steht still - keine bevorzugte Richtung
            movementDirections.remove(playerUUID);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[POLICE] {} startet Suche nach Spieler {} bei {}",
                    police.getNpcName(), player.getName().getString(), player.blockPosition());
            }
        }
    }

    /**
     * Stoppt Suchverhalten
     */
    public static void stopSearch(CustomNPCEntity police, UUID playerUUID) {
        searchTimers.remove(playerUUID);
        activeSearches.remove(police.getUUID());
        lastTargetUpdate.remove(police.getUUID());
        // lastKnownPositions bleibt für spätere Referenz
    }

    /**
     * Prüft, ob eine Polizei gerade sucht
     */
    public static boolean isSearching(CustomNPCEntity police) {
        return activeSearches.containsKey(police.getUUID());
    }

    /**
     * Gibt das Such-Ziel einer Polizei zurück
     */
    public static UUID getSearchTarget(CustomNPCEntity police) {
        return activeSearches.get(police.getUUID());
    }

    /**
     * Prüft, ob die Suchzeit abgelaufen ist
     * SICHERHEIT: Single get() statt containsKey/get für TOCTOU-Vermeidung
     */
    public static boolean isSearchExpired(UUID playerUUID, long currentTick) {
        Long startTick = searchTimers.get(playerUUID);
        if (startTick == null) {
            return true;
        }

        long elapsed = currentTick - startTick;
        long maxDuration = ModConfigHandler.COMMON.POLICE_SEARCH_DURATION_SECONDS.get() * 20L;

        return elapsed >= maxDuration;
    }

    /**
     * Gibt letzte bekannte Position eines Spielers zurück
     */
    public static BlockPos getLastKnownPosition(UUID playerUUID) {
        return lastKnownPositions.get(playerUUID);
    }

    /**
     * Bewegt Polizei zur letzten bekannten Position des Spielers
     * Setzt nur alle 10 Sekunden ein neues Ziel, damit die Polizei aktiv patrouilliert
     * SICHERHEIT: Single get() statt containsKey/get für TOCTOU-Vermeidung
     */
    public static void searchArea(CustomNPCEntity police, UUID playerUUID, long currentTick) {
        BlockPos lastPos = getLastKnownPosition(playerUUID);
        if (lastPos == null) {
            LOGGER.error("[POLICE] FEHLER: Keine letzte Position für Spieler {}", playerUUID);
            return;
        }

        UUID policeUUID = police.getUUID();

        // SICHERHEIT: Single get() für atomaren Zugriff
        Long lastUpdate = lastTargetUpdate.get(policeUUID);

        // Prüfe ob wir ein neues Ziel setzen müssen
        boolean needsNewTarget = false;

        if (lastUpdate == null) {
            needsNewTarget = true; // Erstes Mal
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[POLICE] {} setzt erstes Suchziel", police.getNpcName());
            }
        } else {
            long timeSinceUpdate = currentTick - lastUpdate;
            long updateInterval = ModConfigHandler.COMMON.POLICE_SEARCH_TARGET_UPDATE_SECONDS.get() * 20L;

            // Neues Ziel nach konfigurierbarem Intervall ODER wenn Polizei angekommen ist
            if (timeSinceUpdate >= updateInterval) {
                needsNewTarget = true;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} Update-Intervall erreicht ({}s)", police.getNpcName(), timeSinceUpdate / 20);
                }
            } else if (police.getNavigation().isDone()) {
                needsNewTarget = true;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} hat Ziel erreicht, neues Ziel wird gesetzt", police.getNpcName());
                }
            }
        }

        if (needsNewTarget) {
            int searchRadius = ModConfigHandler.COMMON.POLICE_SEARCH_RADIUS.get();

            BlockPos searchTarget;

            // SICHERHEIT: Single get() für atomaren Zugriff
            String dirStr = movementDirections.get(playerUUID);
            if (dirStr != null) {
                // Suche in Bewegungsrichtung des Spielers
                String[] parts = dirStr.split(",");
                double dirX = Double.parseDouble(parts[0]);
                double dirZ = Double.parseDouble(parts[2]);

                // Normalisiere die Richtung
                double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
                if (length > 0) {
                    dirX /= length;
                    dirZ /= length;
                }

                // Suche hauptsächlich in Bewegungsrichtung (70%) + etwas Zufall (30%)
                double distance = searchRadius * (0.5 + police.getRandom().nextDouble() * 0.5); // 50-100% des Radius
                double spread = searchRadius * 0.3; // 30% Streuung

                int targetX = lastPos.getX() + (int)(dirX * distance) + (police.getRandom().nextInt((int)(spread * 2)) - (int)spread);
                int targetZ = lastPos.getZ() + (int)(dirZ * distance) + (police.getRandom().nextInt((int)(spread * 2)) - (int)spread);

                searchTarget = new BlockPos(targetX, lastPos.getY(), targetZ);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} sucht in Bewegungsrichtung bei {} (Radius: {})",
                        police.getNpcName(), searchTarget, searchRadius);
                }
            } else {
                // Keine Bewegungsrichtung - zufällige Position
                int randomX = lastPos.getX() + (police.getRandom().nextInt(searchRadius * 2) - searchRadius);
                int randomZ = lastPos.getZ() + (police.getRandom().nextInt(searchRadius * 2) - searchRadius);

                searchTarget = new BlockPos(randomX, lastPos.getY(), randomZ);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} sucht zufällig bei {} (Radius: {})",
                        police.getNpcName(), searchTarget, searchRadius);
                }
            }

            // Stoppe aktuelle Navigation
            police.getNavigation().stop();

            // Navigiere zur Suchposition mit höherer Geschwindigkeit
            boolean navigationStarted = police.getNavigation().moveTo(searchTarget.getX(), searchTarget.getY(), searchTarget.getZ(), 1.2);

            if (navigationStarted) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[POLICE] {} Navigation gestartet zu {}", police.getNpcName(), searchTarget);
                }
            } else {
                LOGGER.error("[POLICE] FEHLER: {} konnte nicht zu {} navigieren!", police.getNpcName(), searchTarget);
            }

            // Speichere Update-Zeit
            lastTargetUpdate.put(policeUUID, currentTick);
        } else {
            // Kein neues Ziel - prüfe ob Navigation noch läuft
            if (police.getNavigation().isDone()) {
                LOGGER.warn("[POLICE] WARNUNG: {} Navigation ist fertig aber kein neues Ziel wurde gesetzt!", police.getNpcName());
            }
        }
    }

    /**
     * Cleanup-Methode für entfernte Spieler
     */
    public static void cleanup(UUID playerUUID) {
        lastKnownPositions.remove(playerUUID);
        searchTimers.remove(playerUUID);
        movementDirections.remove(playerUUID);
    }
}
