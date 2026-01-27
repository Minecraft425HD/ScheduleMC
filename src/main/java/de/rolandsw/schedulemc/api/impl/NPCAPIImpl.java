package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.npc.INPCAPI;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of INPCAPI
 *
 * Wrapper für NPC-System mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class NPCAPIImpl implements INPCAPI {

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public CustomNPCEntity getNPCByUUID(UUID npcUUID, ServerLevel level) {
        if (npcUUID == null || level == null) {
            throw new IllegalArgumentException("npcUUID and level cannot be null");
        }

        return level.getAllEntities().stream()
            .filter(entity -> entity instanceof CustomNPCEntity)
            .map(entity -> (CustomNPCEntity) entity)
            .filter(npc -> npc.getUUID().equals(npcUUID))
            .findFirst()
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public CustomNPCEntity getNPCByUUID(UUID npcUUID) {
        if (npcUUID == null) {
            throw new IllegalArgumentException("npcUUID cannot be null");
        }

        // Search in all loaded levels
        MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }

        for (ServerLevel level : server.getAllLevels()) {
            CustomNPCEntity npc = getNPCByUUID(npcUUID, level);
            if (npc != null) {
                return npc;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<CustomNPCEntity> getAllNPCs(ServerLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("level cannot be null");
        }

        return Collections.unmodifiableCollection(
            level.getAllEntities().stream()
                .filter(entity -> entity instanceof CustomNPCEntity)
                .map(entity -> (CustomNPCEntity) entity)
                .collect(Collectors.toList())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<CustomNPCEntity> getAllNPCs() {
        MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(
            server.getAllLevels().stream()
                .flatMap(level -> getAllNPCs(level).stream())
                .collect(Collectors.toList())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNPCCount(ServerLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("level cannot be null");
        }
        return getAllNPCs(level).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalNPCCount() {
        return getAllNPCs().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NPCData getNPCData(CustomNPCEntity npc) {
        if (npc == null) {
            throw new IllegalArgumentException("npc cannot be null");
        }
        return npc.getNpcData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNPCHome(CustomNPCEntity npc, BlockPos homePos) {
        if (npc == null || homePos == null) {
            throw new IllegalArgumentException("npc and homePos cannot be null");
        }
        npc.setHomePosition(homePos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNPCWork(CustomNPCEntity npc, BlockPos workPos) {
        if (npc == null || workPos == null) {
            throw new IllegalArgumentException("npc and workPos cannot be null");
        }
        npc.setWorkPosition(workPos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNPCType(CustomNPCEntity npc, NPCType type) {
        if (npc == null || type == null) {
            throw new IllegalArgumentException("npc and type cannot be null");
        }
        npc.setNPCType(type);
    }
}
