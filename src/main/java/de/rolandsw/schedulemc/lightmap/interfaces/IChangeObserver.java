package de.rolandsw.schedulemc.lightmap.interfaces;

import net.minecraft.world.level.chunk.LevelChunk;

public interface IChangeObserver {
    void handleChangeInWorld(int chunkX, int chunkZ);

    void processChunk(LevelChunk chunk);
}
