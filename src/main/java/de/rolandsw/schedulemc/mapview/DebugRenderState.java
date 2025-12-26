package de.rolandsw.schedulemc.lightmap;

public class DebugRenderState {

    public static int checkChunkX;
    public static int checkChunkZ;
    public static int blockX;
    public static int blockY;
    public static int blockZ;
    public static int chunksChanged;
    public static int chunksTotal;

    public static void print() {
        LightMapConstants.getLogger().error("LightMap:DebugRenderState -> Chunk: " + checkChunkX + " " + checkChunkZ + " Block: " + blockX + " " + blockY + " " + blockZ);
        LightMapConstants.getLogger().error("LightMap:DebugRenderState -> Changed: " + chunksChanged + "/" + chunksTotal);
    }
}
