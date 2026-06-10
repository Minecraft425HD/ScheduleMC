package de.rolandsw.schedulemc.util;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.List;
import java.util.Map;

/**
 * Remappt die ehemals deutschen Registry-Schlüssel auf ihre englischen
 * Nachfolger, damit bestehende Spielstände beim Update nicht ihre Blöcke,
 * Items und BlockEntities verlieren (Umbenennung 2026-06).
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public final class LegacyIdRemapHandler {

    private static final Map<String, String> RENAMES = Map.ofEntries(
        Map.entry("cannabis_trimm_station", "cannabis_trim_station"),
        Map.entry("cannabis_curing_glas", "cannabis_curing_jar"),
        Map.entry("cannabis_hash_presse", "cannabis_hash_press"),
        Map.entry("cannabis_oel_extraktor", "cannabis_oil_extractor"),
        Map.entry("afghanisch_poppy_plant", "afghan_poppy_plant"),
        Map.entry("tuerkisch_poppy_plant", "turkish_poppy_plant"),
        Map.entry("indisch_poppy_plant", "indian_poppy_plant"),
        Map.entry("ritzmaschine", "scoring_machine"),
        Map.entry("opium_presse", "opium_press"),
        Map.entry("kochstation", "cooking_station"),
        Map.entry("heroin_raffinerie", "heroin_refinery"),
        Map.entry("fermentations_tank", "fermentation_tank"),
        Map.entry("destillations_apparat", "distillation_apparatus"),
        Map.entry("mikro_dosierer", "micro_doser"),
        Map.entry("perforations_presse", "perforation_press"),
        Map.entry("reaktions_kessel", "reaction_kettle"),
        Map.entry("trocknungs_ofen", "drying_oven"),
        Map.entry("pillen_presse", "pill_press"),
        Map.entry("chemie_mixer", "chemical_mixer"),
        Map.entry("reduktionskessel", "reduction_kettle"),
        Map.entry("kristallisator", "crystallizer"),
        Map.entry("vakuum_trockner", "vacuum_dryer"),
        Map.entry("bolivianisch_coca_plant", "bolivian_coca_plant"),
        Map.entry("kolumbianisch_coca_plant", "colombian_coca_plant"),
        Map.entry("crack_kocher", "crack_cooker"),
        Map.entry("klimalampe", "climate_lamp"),
        Map.entry("klimalampe_small", "climate_lamp_small"),
        Map.entry("klimalampe_medium", "climate_lamp_medium"),
        Map.entry("klimalampe_large", "climate_lamp_large"),
        Map.entry("wassertank", "water_tank"),
        Map.entry("werkstatt", "workshop")
    );

    private LegacyIdRemapHandler() {
    }

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        remap(event.getMappings(ForgeRegistries.Keys.BLOCKS, ScheduleMC.MOD_ID), ForgeRegistries.BLOCKS);
        remap(event.getMappings(ForgeRegistries.Keys.ITEMS, ScheduleMC.MOD_ID), ForgeRegistries.ITEMS);
        remap(event.getMappings(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID), ForgeRegistries.BLOCK_ENTITY_TYPES);
    }

    private static <T> void remap(List<MissingMappingsEvent.Mapping<T>> mappings, IForgeRegistry<T> registry) {
        for (MissingMappingsEvent.Mapping<T> mapping : mappings) {
            String newPath = RENAMES.get(mapping.getKey().getPath());
            if (newPath == null) {
                continue;
            }
            T target = registry.getValue(new ResourceLocation(ScheduleMC.MOD_ID, newPath));
            if (target != null) {
                mapping.remap(target);
            }
        }
    }
}
