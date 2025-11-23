package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Polizei-Such- und Versteck-System
 *
 * Features:
 * - Indoor-Versteck-Erkennung (Spieler kann sich in Gebäuden verstecken)
 * - Sichtlinien-Checks für Fenster und durchsichtige Türen
 * - Polizei sucht in der Gegend, wo Spieler zuletzt gesehen wurde
 */
public class PoliceSearchBehavior {

    // UUID -> Last Known Position
    private static final Map<UUID, BlockPos> lastKnownPositions = new HashMap<>();

    // UUID -> Search Start Time (in Ticks)
    private static final Map<UUID, Long> searchTimers = new HashMap<>();

    // NPC UUID -> Target Player UUID
    private static final Map<UUID, UUID> activeSearches = new HashMap<>();

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

        System.out.println("[POLICE] " + police.getNpcName() + " startet Suche nach Spieler " +
            player.getName().getString() + " bei " + player.blockPosition());
    }

    /**
     * Stoppt Suchverhalten
     */
    public static void stopSearch(CustomNPCEntity police, UUID playerUUID) {
        searchTimers.remove(playerUUID);
        activeSearches.remove(police.getUUID());
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
     */
    public static boolean isSearchExpired(UUID playerUUID, long currentTick) {
        if (!searchTimers.containsKey(playerUUID)) {
            return true;
        }

        long startTick = searchTimers.get(playerUUID);
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
     */
    public static void searchArea(CustomNPCEntity police, UUID playerUUID) {
        BlockPos lastPos = getLastKnownPosition(playerUUID);
        if (lastPos == null) return;

        int searchRadius = ModConfigHandler.COMMON.POLICE_SEARCH_RADIUS.get();

        // Zufällige Position in der Nähe der letzten bekannten Position
        int randomX = lastPos.getX() + (police.getRandom().nextInt(searchRadius * 2) - searchRadius);
        int randomZ = lastPos.getZ() + (police.getRandom().nextInt(searchRadius * 2) - searchRadius);

        BlockPos searchTarget = new BlockPos(randomX, lastPos.getY(), randomZ);

        // Navigiere zur Suchposition
        police.getNavigation().moveTo(searchTarget.getX(), searchTarget.getY(), searchTarget.getZ(), 1.0);
    }

    /**
     * Cleanup-Methode für entfernte Spieler
     */
    public static void cleanup(UUID playerUUID) {
        lastKnownPositions.remove(playerUUID);
        searchTimers.remove(playerUUID);
    }
}
