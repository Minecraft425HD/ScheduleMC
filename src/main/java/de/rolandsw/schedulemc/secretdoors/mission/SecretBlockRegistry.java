package de.rolandsw.schedulemc.secretdoors.mission;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Laufzeit-Registry für Secret-Blocks (Door/Hatch/HiddenSwitch).
 * Wird beim Laden/Entladen der BlockEntities gepflegt.
 */
public final class SecretBlockRegistry {

    private static final Map<String, SecretBlockEntry> ENTRIES = new ConcurrentHashMap<>();

    private SecretBlockRegistry() {
    }

    public static void register(ServerLevel level, BlockPos pos, String type) {
        if (level == null || pos == null || type == null) return;
        String dim = level.dimension().location().toString();
        ENTRIES.put(key(dim, pos), new SecretBlockEntry(dim, pos.getX(), pos.getY(), pos.getZ(), type));
    }

    public static void unregister(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) return;
        ENTRIES.remove(key(level.dimension().location().toString(), pos));
    }

    public static List<SecretBlockEntry> getAllLoadedEntries(MinecraftServer server) {
        if (server == null) return List.of();
        List<SecretBlockEntry> out = new ArrayList<>();
        for (SecretBlockEntry e : ENTRIES.values()) {
            ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                new net.minecraft.resources.ResourceLocation(e.dimension())));
            if (level == null) continue;
            BlockPos pos = new BlockPos(e.x(), e.y(), e.z());
            if (!level.isLoaded(pos)) continue;
            out.add(e);
        }
        return out;
    }

    private static String key(String dim, BlockPos pos) {
        return dim + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ();
    }

    public record SecretBlockEntry(String dimension, int x, int y, int z, String type) { }
}
