package de.rolandsw.schedulemc.mapview.util;

import de.rolandsw.schedulemc.mapview.integration.DebugRenderState;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.core.event.MapChangeListener;
import de.rolandsw.schedulemc.mapview.core.model.MapChunk;
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
    private final MapChunk[] mapChunks;
    private int left;
    private int right;
    private int top;
    private int bottom;
    private boolean loaded;
    private final MapChangeListener changeObserver;
    // Performance-Optimierung: Dirty-Flag System - nur modified Chunks tracken
    private final Set<Integer> dirtyChunks = new HashSet<>();
    private boolean fullCheckNeeded = false;
    // Performance-Optimierung: nur Chunks prüfen, die noch NICHT als "von geladenen Chunks umschlossen"
    // markiert sind - vorher wurde JEDEN Tick das komplette Grid (bis zu 33x33=1089 Chunks) neu
    // gescannt, unabhängig davon ob sich am Umschlossen-Status irgendetwas geändert haben konnte.
    private final Set<Integer> pendingSurroundCheck = new HashSet<>();

    public ChunkCache(int width, int height, MapChangeListener changeObserver) {
        this.width = width;
        this.height = height;
        this.mapChunks = new MapChunk[width * height];
        this.changeObserver = changeObserver;
    }

    public void centerChunks(BlockPos blockPos) {
        LevelChunk currentChunk = MapViewConstants.getPlayer().level().getChunkAt(blockPos);
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
                        this.mapChunks[index] = new MapChunk(currentChunk.getPos().x - (middleX - x), currentChunk.getPos().z - (middleZ - z));
                        dirtyChunks.add(index); // Neue Chunks sind dirty
                        pendingSurroundCheck.add(index); // Neue Chunks sind noch nicht als umschlossen bekannt
                    }
                }

                for (int z = 0; z < this.height; ++z) {
                    for (int x = movedX > 0 ? this.width - movedX : 0; x < (movedX > 0 ? this.width : -movedX); ++x) {
                        int index = x + z * this.width;
                        this.mapChunks[index] = new MapChunk(currentChunk.getPos().x - (middleX - x), currentChunk.getPos().z - (middleZ - z));
                        dirtyChunks.add(index); // Neue Chunks sind dirty
                        pendingSurroundCheck.add(index); // Neue Chunks sind noch nicht als umschlossen bekannt
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
        ChunkAccess currentChunk = MapViewConstants.getPlayer().level().getChunk(blockPos);
        int middleX = this.width / 2;
        int middleZ = this.height / 2;

        // Performance-Optimierung: Markiere alle Chunks als dirty nach Full-Fill
        dirtyChunks.clear();
        pendingSurroundCheck.clear();
        for (int z = 0; z < this.height; ++z) {
            for (int x = 0; x < this.width; ++x) {
                int index = x + z * this.width;
                this.mapChunks[index] = new MapChunk(currentChunk.getPos().x - (middleX - x), currentChunk.getPos().z - (middleZ - z));
                dirtyChunks.add(index); // Alle neuen Chunks sind dirty
                pendingSurroundCheck.add(index); // Alle neuen Chunks sind noch nicht als umschlossen bekannt
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
        if (!this.loaded || this.pendingSurroundCheck.isEmpty()) {
            return;
        }

        // Performance-Optimierung (2026-09-26): Vorher wurde hier JEDEN Tick das komplette
        // Grid (bis zu 33x33=1089 Chunks) neu gescannt - über 1089 CompletableFutures an einen
        // Thread-Pool submitted und der Client-Thread hat per join() synchron auf alle gewartet,
        // 20x/Sekunde, unabhängig davon ob überhaupt eine Karte sichtbar war. Jetzt wird nur noch
        // die (typischerweise kleine) Menge an Chunks geprüft, die noch nicht als "von geladenen
        // Chunks umschlossen" markiert sind - analog zum bereits vorhandenen dirtyChunks-Muster
        // in checkIfChunksChanged(). Ein Chunk verlässt pendingSurroundCheck erst, sobald er
        // tatsächlich umschlossen ist; bis dahin wird er bei jedem Tick erneut geprüft (z. B.
        // während benachbarte Chunks noch asynchron vom Server nachladen).
        Iterator<Integer> iterator = this.pendingSurroundCheck.iterator();
        while (iterator.hasNext()) {
            int index = iterator.next();
            if (index < 0 || index >= this.mapChunks.length) {
                iterator.remove();
                continue;
            }

            MapChunk mapChunk = this.mapChunks[index];
            mapChunk.checkIfChunkBecameSurroundedByLoaded(this.changeObserver);
            if (mapChunk.isMarkedSurroundedByLoaded()) {
                iterator.remove();
            }
        }
    }

    public void registerChangeAt(int chunkX, int chunkZ) {
        try {
            if (this.lastCenterChunk != null && chunkX >= this.left && chunkX <= this.right && chunkZ >= this.top && chunkZ <= this.bottom) {
                int arrayX = chunkX - this.left;
                int arrayZ = chunkZ - this.top;
                int index = arrayX + arrayZ * this.width;
                MapChunk mapChunk = this.mapChunks[index];
                mapChunk.setModified(true);
                // Performance-Optimierung: Chunk zur Dirty-List hinzufügen
                dirtyChunks.add(index);
            }
        } catch (RuntimeException e) {
            MapViewConstants.getLogger().error(e);
        }
    }

    public boolean isChunkSurroundedByLoaded(int chunkX, int chunkZ) {
        if (this.lastCenterChunk != null && chunkX >= this.left && chunkX <= this.right && chunkZ >= this.top && chunkZ <= this.bottom) {
            int arrayX = chunkX - this.left;
            int arrayZ = chunkZ - this.top;
            MapChunk mapChunk = this.mapChunks[arrayX + arrayZ * this.width];
            return mapChunk.isSurroundedByLoaded();
        } else {
            return false;
        }
    }
}
