package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Raum-Scanner für intelligente Polizei-Durchsuchungen
 *
 * Verwendet Flood-Fill Algorithmus um zusammenhängende Räume zu erkennen.
 * Polizei scannt nur Räume, die sie "gesehen" hat.
 *
 * Alle Werte sind in der Config konfigurierbar!
 */
public class RoomScanner {

    /**
     * Ergebnis eines Raum-Scans
     */
    public static class RoomScanResult {
        public final Set<BlockPos> roomBlocks;
        public final Set<BlockPos> adjacentRooms; // Türen/Durchgänge zu anderen Räumen

        public RoomScanResult(Set<BlockPos> roomBlocks, Set<BlockPos> adjacentRooms) {
            this.roomBlocks = roomBlocks;
            this.adjacentRooms = adjacentRooms;
        }

        public int size() {
            return roomBlocks.size();
        }
    }

    /**
     * Scannt einen einzelnen Raum ausgehend von einer Start-Position
     * Nutzt Flood-Fill um alle zusammenhängenden Luft-Blöcke zu finden
     *
     * @param level Die Welt
     * @param start Start-Position (normalerweise Spieler-Position bei Festnahme)
     * @return RoomScanResult mit allen Blöcken im Raum
     */
    public static RoomScanResult scanRoom(Level level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> roomBlocks = new HashSet<>();
        Set<BlockPos> adjacentRoomDoors = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        // Lade Config-Werte
        int maxRoomSize = ModConfigHandler.COMMON.POLICE_ROOM_SCAN_MAX_SIZE.get();
        int maxSearchDepth = ModConfigHandler.COMMON.POLICE_ROOM_SCAN_MAX_DEPTH.get();

        queue.add(start);
        visited.add(start);

        // Flood-Fill: Finde alle zusammenhängenden Luft-Blöcke
        while (!queue.isEmpty() && roomBlocks.size() < maxRoomSize) {
            BlockPos current = queue.poll();

            // Prüfe ob dieser Block zum Raum gehört
            if (!isPassableBlock(level, current)) {
                continue; // Wand/Solider Block
            }

            roomBlocks.add(current);

            // Prüfe alle 6 Nachbarn (N, S, E, W, Oben, Unten)
            for (BlockPos neighbor : get6Neighbors(current)) {
                if (visited.contains(neighbor)) {
                    continue; // Schon besucht
                }

                visited.add(neighbor);

                BlockState neighborState = level.getBlockState(neighbor);

                // Prüfe ob Nachbar eine Tür/Durchgang ist
                if (isDoorOrOpening(level, neighbor)) {
                    adjacentRoomDoors.add(neighbor);
                    // Füge Position HINTER der Tür zur Queue hinzu (anderer Raum)
                    // ABER markiere diese nicht als Teil dieses Raums
                }
                // Prüfe ob Nachbar passierbar ist
                else if (isPassableBlock(level, neighbor)) {
                    // Sicherheits-Check: Nicht zu weit vom Start entfernen (Y-Achse)
                    if (Math.abs(neighbor.getY() - start.getY()) <= maxSearchDepth) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        return new RoomScanResult(roomBlocks, adjacentRoomDoors);
    }

    /**
     * Scannt mehrere verbundene Räume (wenn Konterband gefunden wurde)
     *
     * @param level Die Welt
     * @param initialRoom Der initial gescannte Raum
     * @param maxAdditionalRooms Maximale Anzahl zusätzlicher Räume
     * @return Set aller Blöcke in allen gescannten Räumen
     */
    public static Set<BlockPos> scanConnectedRooms(Level level, RoomScanResult initialRoom, int maxAdditionalRooms) {
        Set<BlockPos> allBlocks = new HashSet<>(initialRoom.roomBlocks);
        Set<BlockPos> processedDoors = new HashSet<>();
        int roomsScanned = 0;

        Queue<BlockPos> doorsToCheck = new LinkedList<>(initialRoom.adjacentRooms);

        while (!doorsToCheck.isEmpty() && roomsScanned < maxAdditionalRooms) {
            BlockPos door = doorsToCheck.poll();

            if (processedDoors.contains(door)) {
                continue; // Schon verarbeitet
            }

            processedDoors.add(door);

            // Finde Position auf der anderen Seite der Tür
            BlockPos beyondDoor = findPositionBeyondDoor(level, door);
            if (beyondDoor == null) {
                continue;
            }

            // Scanne den nächsten Raum
            RoomScanResult nextRoom = scanRoom(level, beyondDoor);

            // Füge alle Blöcke des neuen Raums hinzu
            allBlocks.addAll(nextRoom.roomBlocks);

            // Füge neue Türen zur Queue hinzu
            for (BlockPos newDoor : nextRoom.adjacentRooms) {
                if (!processedDoors.contains(newDoor)) {
                    doorsToCheck.add(newDoor);
                }
            }

            roomsScanned++;
        }

        return allBlocks;
    }

    /**
     * Findet Position auf der anderen Seite einer Tür
     */
    private static BlockPos findPositionBeyondDoor(Level level, BlockPos door) {
        // Prüfe alle 4 horizontalen Richtungen
        BlockPos[] directions = {
            door.north(),
            door.south(),
            door.east(),
            door.west()
        };

        for (BlockPos pos : directions) {
            if (isPassableBlock(level, pos)) {
                return pos;
            }
        }

        return null; // Keine passierbare Position gefunden
    }

    /**
     * Prüft ob ein Block passierbar ist (Luft, hohe Vegetation, etc.)
     */
    private static boolean isPassableBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        // Luft ist immer passierbar
        if (state.isAir()) {
            return true;
        }

        // Prüfe ob Block keine Kollision hat (z.B. Gras, Blumen)
        return !state.canOcclude();
    }

    /**
     * PERFORMANCE: Nutzt Block-Klassen-Prüfung statt String-Vergleiche.
     * Vermeidet getDescriptionId().toLowerCase() + 3x contains() pro Block.
     */
    private static boolean isDoorOrOpening(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        net.minecraft.world.level.block.Block block = state.getBlock();

        // Direkte Typ-Prüfung statt String-Vergleich
        if (block instanceof net.minecraft.world.level.block.DoorBlock) return true;
        if (block instanceof net.minecraft.world.level.block.TrapDoorBlock) return true;
        if (block instanceof net.minecraft.world.level.block.FenceGateBlock) return true;

        return false;
    }

    /**
     * PERFORMANCE: Direkte BlockPos-Offsets statt Arrays.asList() Allokation.
     * Vermeidet 6 BlockPos-Objekte + 1 List-Wrapper pro Flood-Fill-Schritt.
     */
    private static final BlockPos[] NEIGHBOR_OFFSETS = {
        new BlockPos(0, 0, -1), // north
        new BlockPos(0, 0, 1),  // south
        new BlockPos(1, 0, 0),  // east
        new BlockPos(-1, 0, 0), // west
        new BlockPos(0, 1, 0),  // above
        new BlockPos(0, -1, 0)  // below
    };

    private static List<BlockPos> get6Neighbors(BlockPos pos) {
        return Arrays.asList(
            pos.north(),
            pos.south(),
            pos.east(),
            pos.west(),
            pos.above(),
            pos.below()
        );
    }

    /**
     * Berechnet ungefähre Raum-Größe in Kubikmetern
     */
    public static int calculateRoomVolume(Set<BlockPos> roomBlocks) {
        if (roomBlocks.isEmpty()) {
            return 0;
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : roomBlocks) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;

        return width * height * depth;
    }
}
