package de.rolandsw.schedulemc.mapview.core.event;

import net.minecraft.world.level.chunk.LevelChunk;

public interface MapChangeListener {
    void handleChangeInWorld(int chunkX, int chunkZ);

    void processChunk(LevelChunk chunk);
}
