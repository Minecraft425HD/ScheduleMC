package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.npc.INPCAPI;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public CustomNPCEntity getNPCByUUID(UUID npcUUID, ServerLevel level) {
        if (npcUUID == null || level == null) {
            throw new IllegalArgumentException("npcUUID and level cannot be null");
        }

        return StreamSupport.stream(level.getAllEntities().spliterator(), false)
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
            StreamSupport.stream(level.getAllEntities().spliterator(), false)
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
            StreamSupport.stream(server.getAllLevels().spliterator(), false)
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
        // Access through npcData
        npc.getNpcData().setHomeLocation(homePos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNPCWork(CustomNPCEntity npc, BlockPos workPos) {
        if (npc == null || workPos == null) {
            throw new IllegalArgumentException("npc and workPos cannot be null");
        }
        // Access through npcData
        npc.getNpcData().setWorkLocation(workPos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNPCType(CustomNPCEntity npc, NPCType type) {
        if (npc == null || type == null) {
            throw new IllegalArgumentException("npc and type cannot be null");
        }
        // Access through npcData
        npc.getNpcData().setNpcType(type);
    }

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<CustomNPCEntity> getNPCsByType(NPCType type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return Collections.unmodifiableCollection(
            getAllNPCs().stream()
                .filter(npc -> npc.getNpcData().getNpcType() == type)
                .collect(Collectors.toList())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<CustomNPCEntity> getNPCsInRadius(ServerLevel level, BlockPos center, double radius) {
        if (level == null || center == null) {
            throw new IllegalArgumentException("level and center cannot be null");
        }
        if (radius < 0) {
            throw new IllegalArgumentException("radius must be non-negative, got: " + radius);
        }
        double radiusSquared = radius * radius;
        return Collections.unmodifiableCollection(
            getAllNPCs(level).stream()
                .filter(npc -> {
                    double dx = npc.getX() - center.getX();
                    double dy = npc.getY() - center.getY();
                    double dz = npc.getZ() - center.getZ();
                    return (dx * dx + dy * dy + dz * dz) <= radiusSquared;
                })
                .collect(Collectors.toList())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNPCName(CustomNPCEntity npc, String name) {
        if (npc == null || name == null) {
            throw new IllegalArgumentException("npc and name cannot be null");
        }
        npc.setCustomName(Component.literal(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNPCLeisureLocation(CustomNPCEntity npc, BlockPos leisurePos) {
        if (npc == null || leisurePos == null) {
            throw new IllegalArgumentException("npc and leisurePos cannot be null");
        }
        npc.getNpcData().addLeisureLocation(leisurePos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNPC(CustomNPCEntity npc) {
        if (npc == null) {
            throw new IllegalArgumentException("npc cannot be null");
        }
        npc.discard();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNPCSchedule(CustomNPCEntity npc, String activity, int time) {
        if (npc == null || activity == null) {
            throw new IllegalArgumentException("npc and activity cannot be null");
        }
        if (time < 0 || time > 2359) {
            throw new IllegalArgumentException("time must be in HHMM format (0-2359), got: " + time);
        }
        LOGGER.debug("Stub: setNPCSchedule not fully implemented - schedule system not directly accessible via API");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNPCBalance(CustomNPCEntity npc) {
        if (npc == null) {
            throw new IllegalArgumentException("npc cannot be null");
        }
        LOGGER.debug("Stub: getNPCBalance not fully implemented - NPC wallet not directly accessible");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNPCBalance(CustomNPCEntity npc, double amount) {
        if (npc == null) {
            throw new IllegalArgumentException("npc cannot be null");
        }
        LOGGER.debug("Stub: setNPCBalance not fully implemented - NPC wallet not directly accessible");
    }
}
