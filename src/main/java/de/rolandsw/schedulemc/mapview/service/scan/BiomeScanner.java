package de.rolandsw.schedulemc.mapview.service.scan;

import com.google.common.collect.BiMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class BiomeScanner {
    private BiomeScanner() {
    }

    public static void parseLine(Level world, String line, BiMap<Biome, Integer> map) {
        String[] lineParts = line.split(" ");

        int id = Integer.parseInt(lineParts[0]);
        Biome biome = world.registryAccess().registryOrThrow(Registries.BIOME).get(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(lineParts[1])));
        if (biome != null) {
            map.forcePut(biome, id);
        }
    }

    public static void populateLegacyBiomeMap(ClientLevel world, BiMap<Biome, Integer> map) {
        Registry<Biome> registry = world.registryAccess().registryOrThrow(Registries.BIOME);
        registry.forEach(biome -> {
            map.forcePut(biome, registry.getId(biome));
        });
    }
}
