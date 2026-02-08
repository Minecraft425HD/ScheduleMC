package de.rolandsw.schedulemc.npc.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature 3: Strassensperren
 *
 * Bei Wanted Level >= 4 kann die Polizei Strassensperren errichten.
 * - Temporaere Barrieren (3 Bloecke breit) auf Strassen
 * - Automatisches Entfernen nach 5 Minuten
 * - Maximal 2 Sperren pro gesuchtem Spieler
 */
public class PoliceRoadblock {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Maximale Anzahl Sperren pro Spieler */
    public static final int MAX_ROADBLOCKS_PER_PLAYER = 2;

    /** Dauer einer Sperre in Ticks (5 Minuten) */
    public static final long ROADBLOCK_DURATION_TICKS = 6000L;

    /** Breite der Sperre in Bloecken */
    private static final int ROADBLOCK_WIDTH = 3;

    /** Hoehe der Sperre in Bloecken */
    private static final int ROADBLOCK_HEIGHT = 2;

    /** Aktive Sperren: Spieler-UUID -> Liste von Roadblock-Daten */
    private static final Map<UUID, List<RoadblockData>> activeRoadblocks = new ConcurrentHashMap<>();

    /**
     * Daten einer einzelnen Strassensperre
     */
    public static class RoadblockData {
        public final BlockPos center;
        public final long creationTick;
        public final long expirationTick;
        public final List<BlockPos> barrierPositions;
        public final Map<BlockPos, BlockState> originalBlocks;
        public final UUID targetPlayer;

        public RoadblockData(BlockPos center, long creationTick, UUID targetPlayer) {
            this.center = center;
            this.creationTick = creationTick;
            this.expirationTick = creationTick + ROADBLOCK_DURATION_TICKS;
            this.barrierPositions = new ArrayList<>();
            this.originalBlocks = new HashMap<>();
            this.targetPlayer = targetPlayer;
        }

        public boolean isExpired(long currentTick) {
            return currentTick >= expirationTick;
        }
    }

    /**
     * Erstellt eine Strassensperre an der angegebenen Position
     *
     * @param level Die ServerLevel
     * @param location Position der Sperre
     * @param targetPlayer UUID des gesuchten Spielers
     * @return true wenn Sperre erstellt wurde
     */
    public static boolean createRoadblock(ServerLevel level, BlockPos location, UUID targetPlayer) {
        // Pruefe Limit
        List<RoadblockData> playerBlocks = activeRoadblocks.computeIfAbsent(targetPlayer, k -> new ArrayList<>());
        if (playerBlocks.size() >= MAX_ROADBLOCKS_PER_PLAYER) {
            return false;
        }

        long currentTick = level.getGameTime();
        RoadblockData roadblock = new RoadblockData(location, currentTick, targetPlayer);

        // Platziere Barrieren (3 breit, 2 hoch)
        for (int x = -ROADBLOCK_WIDTH / 2; x <= ROADBLOCK_WIDTH / 2; x++) {
            for (int y = 0; y < ROADBLOCK_HEIGHT; y++) {
                BlockPos barrierPos = location.offset(x, y, 0);

                // Speichere originalen Block
                BlockState original = level.getBlockState(barrierPos);
                if (original.isAir() || original.canBeReplaced()) {
                    roadblock.originalBlocks.put(barrierPos, original);
                    roadblock.barrierPositions.add(barrierPos);

                    // Setze Barrier-Block
                    level.setBlock(barrierPos, Blocks.BARRIER.defaultBlockState(), 3);
                }
            }
        }

        // Auch in Z-Richtung (Kreuzung)
        for (int z = -ROADBLOCK_WIDTH / 2; z <= ROADBLOCK_WIDTH / 2; z++) {
            if (z == 0) continue; // Schon gesetzt
            for (int y = 0; y < ROADBLOCK_HEIGHT; y++) {
                BlockPos barrierPos = location.offset(0, y, z);
                BlockState original = level.getBlockState(barrierPos);
                if (original.isAir() || original.canBeReplaced()) {
                    roadblock.originalBlocks.put(barrierPos, original);
                    roadblock.barrierPositions.add(barrierPos);
                    level.setBlock(barrierPos, Blocks.BARRIER.defaultBlockState(), 3);
                }
            }
        }

        playerBlocks.add(roadblock);
        LOGGER.info("[ROADBLOCK] Strassensperre erstellt bei {} fuer Spieler {}",
            location.toShortString(), targetPlayer);
        return true;
    }

    /**
     * Entfernt eine Strassensperre und stellt originale Bloecke wieder her
     */
    private static void removeRoadblock(ServerLevel level, RoadblockData roadblock) {
        for (Map.Entry<BlockPos, BlockState> entry : roadblock.originalBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState original = entry.getValue();

            // Nur entfernen wenn es noch eine Barrier ist
            if (level.getBlockState(pos).is(Blocks.BARRIER)) {
                level.setBlock(pos, original, 3);
            }
        }

        LOGGER.info("[ROADBLOCK] Strassensperre entfernt bei {}", roadblock.center.toShortString());
    }

    /**
     * Wird jeden Server-Tick aufgerufen - prueft abgelaufene Sperren
     */
    public static void tick(ServerLevel level) {
        long currentTick = level.getGameTime();

        // Nur alle 20 Ticks pruefen
        if (currentTick % 20 != 0) return;

        for (Map.Entry<UUID, List<RoadblockData>> entry : activeRoadblocks.entrySet()) {
            Iterator<RoadblockData> it = entry.getValue().iterator();
            while (it.hasNext()) {
                RoadblockData roadblock = it.next();
                if (roadblock.isExpired(currentTick)) {
                    removeRoadblock(level, roadblock);
                    it.remove();
                }
            }
        }

        // Leere Listen entfernen
        activeRoadblocks.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    /**
     * Entfernt alle Sperren fuer einen Spieler (z.B. nach Festnahme)
     */
    public static void removeAllForPlayer(ServerLevel level, UUID playerUUID) {
        List<RoadblockData> roadblocks = activeRoadblocks.remove(playerUUID);
        if (roadblocks != null) {
            for (RoadblockData roadblock : roadblocks) {
                removeRoadblock(level, roadblock);
            }
        }
    }

    /**
     * Gibt die Anzahl aktiver Sperren fuer einen Spieler zurueck
     */
    public static int getActiveCount(UUID playerUUID) {
        List<RoadblockData> blocks = activeRoadblocks.get(playerUUID);
        return blocks != null ? blocks.size() : 0;
    }
}
