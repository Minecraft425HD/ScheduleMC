package de.rolandsw.schedulemc.lightmap.util;

import de.rolandsw.schedulemc.lightmap.DebugRenderState;
import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import de.rolandsw.schedulemc.lightmap.interfaces.IChangeObserver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChunkCache {
    private final int width;
    private final int height;
    private LevelChunk lastCenterChunk;
    private final ChunkData[] mapChunks;
    private int left;
    private int right;
    private int top;
    private int bottom;
    private boolean loaded;
    private final IChangeObserver changeObserver;
    // Performance-Optimierung: Dirty-Flag System - nur modified Chunks tracken
    private final Set<Integer> dirtyChunks = new HashSet<>();
    private boolean fullCheckNeeded = false;

    public ChunkCache(int width, int height, IChangeObserver changeObserver) {
        this.width = width;
        this.height = height;
        this.mapChunks = new ChunkData[width * height];
        this.changeObserver = changeObserver;
    }

    public void centerChunks(BlockPos blockPos) {
        LevelChunk currentChunk = LightMapConstants.getPlayer().level().getChunkAt(blockPos);
        if (currentChunk != this.lastCenterChunk) {
            if (this.lastCenterChunk == null) {
                this.fillAllChunks(blockPos);
                this.lastCenterChunk = currentChunk;
                return;
            }

            int middleX = this.width / 2;
            int middleZ = this.height / 2;
            int movedX = currentChunk.getPos().x - this.lastCenterChunk.getPos().x;
            int movedZ = currentChunk.getPos().z - this.lastCenterChunk.getPos().z;
            if (Math.abs(movedX) < this.width && Math.abs(movedZ) < this.height && currentChunk.getLevel().equals(this.lastCenterChunk.getLevel())) {
                this.moveX(movedX);
                this.moveZ(movedZ);

                // Performance-Optimierung: Markiere nur neue Chunks als dirty (nicht alle!)
                for (int z = movedZ > 0 ? this.height - movedZ : 0; z < (movedZ > 0 ? this.height : -movedZ); ++z) {
                    for (int x = 0; x < this.width; ++x) {
                        int index = x + z * this.width;
                        this.mapChunks[index] = new ChunkData(currentChunk.getPos().x - (middleX - x), currentChunk.getPos().z - (middleZ - z));
                        dirtyChunks.add(index); // Neue Chunks sind dirty
                    }
                }

                for (int z = 0; z < this.height; ++z) {
                    for (int x = movedX > 0 ? this.width - movedX : 0; x < (movedX > 0 ? this.width : -movedX); ++x) {
                        int index = x + z * this.width;
                        this.mapChunks[index] = new ChunkData(currentChunk.getPos().x - (middleX - x), currentChunk.getPos().z - (middleZ - z));
                        dirtyChunks.add(index); // Neue Chunks sind dirty
                    }
                }
            } else {
                this.fillAllChunks(blockPos);
            }

            this.left = this.mapChunks[0].getX();
            this.top = this.mapChunks[0].getZ();
            this.right = this.mapChunks[this.mapChunks.length - 1].getX();
            this.bottom = this.mapChunks[this.mapChunks.length - 1].getZ();
            this.lastCenterChunk = currentChunk;
        }

    }

    private void fillAllChunks(BlockPos blockPos) {
        ChunkAccess currentChunk = LightMapConstants.getPlayer().level().getChunk(blockPos);
        int middleX = this.width / 2;
        int middleZ = this.height / 2;

        // Performance-Optimierung: Markiere alle Chunks als dirty nach Full-Fill
        dirtyChunks.clear();
        for (int z = 0; z < this.height; ++z) {
            for (int x = 0; x < this.width; ++x) {
                int index = x + z * this.width;
                this.mapChunks[index] = new ChunkData(currentChunk.getPos().x - (middleX - x), currentChunk.getPos().z - (middleZ - z));
                dirtyChunks.add(index); // Alle neuen Chunks sind dirty
            }
        }

        this.left = this.mapChunks[0].getX();
        this.top = this.mapChunks[0].getZ();
        this.right = this.mapChunks[this.mapChunks.length - 1].getX();
        this.bottom = this.mapChunks[this.mapChunks.length - 1].getZ();
        this.loaded = true;
    }

    private void moveX(int offset) {
        if (offset > 0) {
            System.arraycopy(this.mapChunks, offset, this.mapChunks, 0, this.mapChunks.length - offset);
        } else if (offset < 0) {
            System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset, this.mapChunks.length + offset);
        }

    }

    private void moveZ(int offset) {
        if (offset > 0) {
            System.arraycopy(this.mapChunks, offset * this.width, this.mapChunks, 0, this.mapChunks.length - offset * this.width);
        } else if (offset < 0) {
            System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset * this.width, this.mapChunks.length + offset * this.width);
        }

    }

    public void checkIfChunksChanged() {
        if (this.loaded) {
            DebugRenderState.chunksChanged = 0;
            DebugRenderState.chunksTotal = 0;

            // Performance-Optimierung: Nur dirty Chunks prüfen statt alle!
            // Vorher: Iterierte über alle width*height Chunks (bis zu 33*33=1089 Chunks!)
            // Jetzt: Nur über geänderte Chunks (typisch 1-10 Chunks)
            if (fullCheckNeeded || dirtyChunks.size() > this.mapChunks.length / 2) {
                // Full check wenn explizit gefordert oder >50% dirty
                for (int z = this.height - 1; z >= 0; --z) {
                    for (int x = 0; x < this.width; ++x) {
                        DebugRenderState.chunksTotal++;
                        this.mapChunks[x + z * this.width].checkIfChunkChanged(this.changeObserver);
                    }
                }
                dirtyChunks.clear();
                fullCheckNeeded = false;
            } else {
                // Optimierter Pfad: Nur dirty Chunks prüfen
                Iterator<Integer> iterator = dirtyChunks.iterator();
                while (iterator.hasNext()) {
                    int index = iterator.next();
                    if (index >= 0 && index < this.mapChunks.length) {
                        DebugRenderState.chunksTotal++;
                        this.mapChunks[index].checkIfChunkChanged(this.changeObserver);
                    }
                    iterator.remove(); // Chunk aus dirty list entfernen
                }
            }
        }
    }

    public void checkIfChunksBecameSurroundedByLoaded() {
        if (this.loaded) {
            // Performance-Optimierung: Parallel processing für große Chunk-Arrays
            // Bei zoom=4 (33x33=1089 Chunks) kann Parallelverarbeitung 2-4x schneller sein
            int totalChunks = this.width * this.height;
            if (totalChunks > 100) { // Nur bei >100 Chunks parallel (ab zoom=2)
                // Parallele Verarbeitung mit ThreadManager
                java.util.concurrent.CompletableFuture<?>[] futures = new java.util.concurrent.CompletableFuture<?>[totalChunks];
                for (int z = this.height - 1, idx = 0; z >= 0; --z) {
                    for (int x = 0; x < this.width; ++x, ++idx) {
                        final int index = x + z * this.width;
                        futures[idx] = java.util.concurrent.CompletableFuture.runAsync(() -> {
                            this.mapChunks[index].checkIfChunkBecameSurroundedByLoaded(this.changeObserver);
                        }, de.rolandsw.schedulemc.lightmap.persistent.ThreadManager.executorService);
                    }
                }
                // Warte auf alle Tasks
                java.util.concurrent.CompletableFuture.allOf(futures).join();
            } else {
                // Sequential processing für kleine Arrays (overhead würde nicht lohnen)
                for (int z = this.height - 1; z >= 0; --z) {
                    for (int x = 0; x < this.width; ++x) {
                        this.mapChunks[x + z * this.width].checkIfChunkBecameSurroundedByLoaded(this.changeObserver);
                    }
                }
            }
        }
    }

    public void registerChangeAt(int chunkX, int chunkZ) {
        try {
            if (this.lastCenterChunk != null && chunkX >= this.left && chunkX <= this.right && chunkZ >= this.top && chunkZ <= this.bottom) {
                int arrayX = chunkX - this.left;
                int arrayZ = chunkZ - this.top;
                int index = arrayX + arrayZ * this.width;
                ChunkData mapChunk = this.mapChunks[index];
                mapChunk.setModified(true);
                // Performance-Optimierung: Chunk zur Dirty-List hinzufügen
                dirtyChunks.add(index);
            }
        } catch (RuntimeException e) {
            LightMapConstants.getLogger().error(e);
        }
    }

    public boolean isChunkSurroundedByLoaded(int chunkX, int chunkZ) {
        if (this.lastCenterChunk != null && chunkX >= this.left && chunkX <= this.right && chunkZ >= this.top && chunkZ <= this.bottom) {
            int arrayX = chunkX - this.left;
            int arrayZ = chunkZ - this.top;
            ChunkData mapChunk = this.mapChunks[arrayX + arrayZ * this.width];
            return mapChunk.isSurroundedByLoaded();
        } else {
            return false;
        }
    }
}
