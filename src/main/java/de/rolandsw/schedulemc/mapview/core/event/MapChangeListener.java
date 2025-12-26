package de.rolandsw.schedulemc.mapview.core.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public interface MapChangeListener {
    void handleChangeInWorld(int chunkX, int chunkZ);

    void processChunk(LevelChunk chunk);

    void newWorld(ClientLevel world);
}
