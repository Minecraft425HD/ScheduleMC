package de.rolandsw.schedulemc.mapview.integration;

public class DebugRenderState {

    public static int checkChunkX;
    public static int checkChunkZ;
    public static int blockX;
    public static int blockY;
    public static int blockZ;
    public static int chunksChanged;
    public static int chunksTotal;

    public static void print() {
        MapViewConstants.getLogger().error("MapDataManager:DebugRenderState -> Chunk: " + checkChunkX + " " + checkChunkZ + " Block: " + blockX + " " + blockY + " " + blockZ);
        MapViewConstants.getLogger().error("MapDataManager:DebugRenderState -> Changed: " + chunksChanged + "/" + chunksTotal);
    }
}
