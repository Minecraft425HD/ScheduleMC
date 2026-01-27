package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.wine.blocks.WineBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Wine BlockEntities
 */
public class WineBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID
    );

    // Crushing Station
    public static final RegistryObject<BlockEntityType<CrushingStationBlockEntity>> CRUSHING_STATION =
        BLOCK_ENTITIES.register("crushing_station", () ->
            BlockEntityType.Builder.of(CrushingStationBlockEntity::new,
                WineBlocks.CRUSHING_STATION.get()
            ).build(null));

    // Wine Presses
    public static final RegistryObject<BlockEntityType<SmallWinePressBlockEntity>> SMALL_WINE_PRESS =
        BLOCK_ENTITIES.register("small_wine_press", () ->
            BlockEntityType.Builder.of(SmallWinePressBlockEntity::new,
                WineBlocks.SMALL_WINE_PRESS.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumWinePressBlockEntity>> MEDIUM_WINE_PRESS =
        BLOCK_ENTITIES.register("medium_wine_press", () ->
            BlockEntityType.Builder.of(MediumWinePressBlockEntity::new,
                WineBlocks.MEDIUM_WINE_PRESS.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeWinePressBlockEntity>> LARGE_WINE_PRESS =
        BLOCK_ENTITIES.register("large_wine_press", () ->
            BlockEntityType.Builder.of(LargeWinePressBlockEntity::new,
                WineBlocks.LARGE_WINE_PRESS.get()
            ).build(null));

    // Fermentation Tanks
    public static final RegistryObject<BlockEntityType<SmallFermentationTankBlockEntity>> SMALL_FERMENTATION_TANK =
        BLOCK_ENTITIES.register("small_fermentation_tank", () ->
            BlockEntityType.Builder.of(SmallFermentationTankBlockEntity::new,
                WineBlocks.SMALL_FERMENTATION_TANK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumFermentationTankBlockEntity>> MEDIUM_FERMENTATION_TANK =
        BLOCK_ENTITIES.register("medium_fermentation_tank", () ->
            BlockEntityType.Builder.of(MediumFermentationTankBlockEntity::new,
                WineBlocks.MEDIUM_FERMENTATION_TANK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeFermentationTankBlockEntity>> LARGE_FERMENTATION_TANK =
        BLOCK_ENTITIES.register("large_fermentation_tank", () ->
            BlockEntityType.Builder.of(LargeFermentationTankBlockEntity::new,
                WineBlocks.LARGE_FERMENTATION_TANK.get()
            ).build(null));

    // Aging Barrels
    public static final RegistryObject<BlockEntityType<SmallAgingBarrelBlockEntity>> SMALL_AGING_BARREL =
        BLOCK_ENTITIES.register("small_aging_barrel", () ->
            BlockEntityType.Builder.of(SmallAgingBarrelBlockEntity::new,
                WineBlocks.SMALL_AGING_BARREL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumAgingBarrelBlockEntity>> MEDIUM_AGING_BARREL =
        BLOCK_ENTITIES.register("medium_aging_barrel", () ->
            BlockEntityType.Builder.of(MediumAgingBarrelBlockEntity::new,
                WineBlocks.MEDIUM_AGING_BARREL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeAgingBarrelBlockEntity>> LARGE_AGING_BARREL =
        BLOCK_ENTITIES.register("large_aging_barrel", () ->
            BlockEntityType.Builder.of(LargeAgingBarrelBlockEntity::new,
                WineBlocks.LARGE_AGING_BARREL.get()
            ).build(null));

    // Bottling Station
    public static final RegistryObject<BlockEntityType<WineBottlingStationBlockEntity>> WINE_BOTTLING_STATION =
        BLOCK_ENTITIES.register("wine_bottling_station", () ->
            BlockEntityType.Builder.of(WineBottlingStationBlockEntity::new,
                WineBlocks.WINE_BOTTLING_STATION.get()
            ).build(null));
}
