package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.honey.blocks.HoneyBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Honey BlockEntities
 */
public class HoneyBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID
    );

    // Beehives
    public static final RegistryObject<BlockEntityType<BeehiveBlockEntity>> BEEHIVE =
        BLOCK_ENTITIES.register("beehive", () ->
            BlockEntityType.Builder.of(BeehiveBlockEntity::new,
                HoneyBlocks.BEEHIVE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<AdvancedBeehiveBlockEntity>> ADVANCED_BEEHIVE =
        BLOCK_ENTITIES.register("advanced_beehive", () ->
            BlockEntityType.Builder.of(AdvancedBeehiveBlockEntity::new,
                HoneyBlocks.ADVANCED_BEEHIVE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ApiaryBlockEntity>> APIARY =
        BLOCK_ENTITIES.register("apiary", () ->
            BlockEntityType.Builder.of(ApiaryBlockEntity::new,
                HoneyBlocks.APIARY.get()
            ).build(null));

    // Extractors
    public static final RegistryObject<BlockEntityType<HoneyExtractorBlockEntity>> HONEY_EXTRACTOR =
        BLOCK_ENTITIES.register("honey_extractor", () ->
            BlockEntityType.Builder.of(HoneyExtractorBlockEntity::new,
                HoneyBlocks.HONEY_EXTRACTOR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CentrifugalExtractorBlockEntity>> CENTRIFUGAL_EXTRACTOR =
        BLOCK_ENTITIES.register("centrifugal_extractor", () ->
            BlockEntityType.Builder.of(CentrifugalExtractorBlockEntity::new,
                HoneyBlocks.CENTRIFUGAL_EXTRACTOR.get()
            ).build(null));

    // Filtering Station
    public static final RegistryObject<BlockEntityType<FilteringStationBlockEntity>> FILTERING_STATION =
        BLOCK_ENTITIES.register("filtering_station", () ->
            BlockEntityType.Builder.of(FilteringStationBlockEntity::new,
                HoneyBlocks.FILTERING_STATION.get()
            ).build(null));

    // Aging Chambers
    public static final RegistryObject<BlockEntityType<SmallAgingChamberBlockEntity>> SMALL_AGING_CHAMBER =
        BLOCK_ENTITIES.register("small_aging_chamber", () ->
            BlockEntityType.Builder.of(SmallAgingChamberBlockEntity::new,
                HoneyBlocks.SMALL_AGING_CHAMBER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumAgingChamberBlockEntity>> MEDIUM_AGING_CHAMBER =
        BLOCK_ENTITIES.register("medium_aging_chamber", () ->
            BlockEntityType.Builder.of(MediumAgingChamberBlockEntity::new,
                HoneyBlocks.MEDIUM_AGING_CHAMBER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeAgingChamberBlockEntity>> LARGE_AGING_CHAMBER =
        BLOCK_ENTITIES.register("large_aging_chamber", () ->
            BlockEntityType.Builder.of(LargeAgingChamberBlockEntity::new,
                HoneyBlocks.LARGE_AGING_CHAMBER.get()
            ).build(null));

    // Processing Stations
    public static final RegistryObject<BlockEntityType<ProcessingStationBlockEntity>> PROCESSING_STATION =
        BLOCK_ENTITIES.register("processing_station", () ->
            BlockEntityType.Builder.of(ProcessingStationBlockEntity::new,
                HoneyBlocks.PROCESSING_STATION.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CreamingStationBlockEntity>> CREAMING_STATION =
        BLOCK_ENTITIES.register("creaming_station", () ->
            BlockEntityType.Builder.of(CreamingStationBlockEntity::new,
                HoneyBlocks.CREAMING_STATION.get()
            ).build(null));

    // Bottling Station
    public static final RegistryObject<BlockEntityType<BottlingStationBlockEntity>> BOTTLING_STATION =
        BLOCK_ENTITIES.register("bottling_station", () ->
            BlockEntityType.Builder.of(BottlingStationBlockEntity::new,
                HoneyBlocks.BOTTLING_STATION.get()
            ).build(null));
}
