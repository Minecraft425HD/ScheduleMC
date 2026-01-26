package de.rolandsw.schedulemc.coffee.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.coffee.blocks.CoffeeBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Coffee-BlockEntities
 */
public class CoffeeBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    // Wet Processing Station
    public static final RegistryObject<BlockEntityType<WetProcessingStationBlockEntity>> WET_PROCESSING_STATION =
        BLOCK_ENTITIES.register("wet_processing_station",
            () -> BlockEntityType.Builder.of(
                WetProcessingStationBlockEntity::new,
                CoffeeBlocks.WET_PROCESSING_STATION.get()
            ).build(null));

    // Drying Trays
    public static final RegistryObject<BlockEntityType<SmallDryingTrayBlockEntity>> SMALL_DRYING_TRAY =
        BLOCK_ENTITIES.register("small_coffee_drying_tray",
            () -> BlockEntityType.Builder.of(
                SmallDryingTrayBlockEntity::new,
                CoffeeBlocks.SMALL_DRYING_TRAY.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumDryingTrayBlockEntity>> MEDIUM_DRYING_TRAY =
        BLOCK_ENTITIES.register("medium_coffee_drying_tray",
            () -> BlockEntityType.Builder.of(
                MediumDryingTrayBlockEntity::new,
                CoffeeBlocks.MEDIUM_DRYING_TRAY.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeDryingTrayBlockEntity>> LARGE_DRYING_TRAY =
        BLOCK_ENTITIES.register("large_coffee_drying_tray",
            () -> BlockEntityType.Builder.of(
                LargeDryingTrayBlockEntity::new,
                CoffeeBlocks.LARGE_DRYING_TRAY.get()
            ).build(null));

    // Coffee Roasters
    public static final RegistryObject<BlockEntityType<SmallCoffeeRoasterBlockEntity>> SMALL_COFFEE_ROASTER =
        BLOCK_ENTITIES.register("small_coffee_roaster",
            () -> BlockEntityType.Builder.of(
                SmallCoffeeRoasterBlockEntity::new,
                CoffeeBlocks.SMALL_COFFEE_ROASTER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumCoffeeRoasterBlockEntity>> MEDIUM_COFFEE_ROASTER =
        BLOCK_ENTITIES.register("medium_coffee_roaster",
            () -> BlockEntityType.Builder.of(
                MediumCoffeeRoasterBlockEntity::new,
                CoffeeBlocks.MEDIUM_COFFEE_ROASTER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeCoffeeRoasterBlockEntity>> LARGE_COFFEE_ROASTER =
        BLOCK_ENTITIES.register("large_coffee_roaster",
            () -> BlockEntityType.Builder.of(
                LargeCoffeeRoasterBlockEntity::new,
                CoffeeBlocks.LARGE_COFFEE_ROASTER.get()
            ).build(null));

    // Coffee Grinder
    public static final RegistryObject<BlockEntityType<CoffeeGrinderBlockEntity>> COFFEE_GRINDER =
        BLOCK_ENTITIES.register("coffee_grinder",
            () -> BlockEntityType.Builder.of(
                CoffeeGrinderBlockEntity::new,
                CoffeeBlocks.COFFEE_GRINDER.get()
            ).build(null));

    // Packaging Table
    public static final RegistryObject<BlockEntityType<CoffeePackagingTableBlockEntity>> COFFEE_PACKAGING_TABLE =
        BLOCK_ENTITIES.register("coffee_packaging_table",
            () -> BlockEntityType.Builder.of(
                CoffeePackagingTableBlockEntity::new,
                CoffeeBlocks.COFFEE_PACKAGING_TABLE.get()
            ).build(null));
}
