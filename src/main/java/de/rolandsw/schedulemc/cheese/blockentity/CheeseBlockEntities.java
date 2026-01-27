package de.rolandsw.schedulemc.cheese.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Cheese BlockEntities
 */
public class CheeseBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID
    );

    // Pasteurization Station
    public static final RegistryObject<BlockEntityType<PasteurizationStationBlockEntity>> PASTEURIZATION_STATION =
        BLOCK_ENTITIES.register("pasteurization_station", () ->
            BlockEntityType.Builder.of(PasteurizationStationBlockEntity::new,
                CheeseBlocks.PASTEURIZATION_STATION.get()
            ).build(null));

    // Curdling Vat
    public static final RegistryObject<BlockEntityType<CurdlingVatBlockEntity>> CURDLING_VAT =
        BLOCK_ENTITIES.register("curdling_vat", () ->
            BlockEntityType.Builder.of(CurdlingVatBlockEntity::new,
                CheeseBlocks.CURDLING_VAT.get()
            ).build(null));

    // Cheese Presses
    public static final RegistryObject<BlockEntityType<SmallCheesePressBlockEntity>> SMALL_CHEESE_PRESS =
        BLOCK_ENTITIES.register("small_cheese_press", () ->
            BlockEntityType.Builder.of(SmallCheesePressBlockEntity::new,
                CheeseBlocks.SMALL_CHEESE_PRESS.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumCheesePressBlockEntity>> MEDIUM_CHEESE_PRESS =
        BLOCK_ENTITIES.register("medium_cheese_press", () ->
            BlockEntityType.Builder.of(MediumCheesePressBlockEntity::new,
                CheeseBlocks.MEDIUM_CHEESE_PRESS.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeCheesePressBlockEntity>> LARGE_CHEESE_PRESS =
        BLOCK_ENTITIES.register("large_cheese_press", () ->
            BlockEntityType.Builder.of(LargeCheesePressBlockEntity::new,
                CheeseBlocks.LARGE_CHEESE_PRESS.get()
            ).build(null));

    // Aging Caves
    public static final RegistryObject<BlockEntityType<SmallAgingCaveBlockEntity>> SMALL_AGING_CAVE =
        BLOCK_ENTITIES.register("small_aging_cave", () ->
            BlockEntityType.Builder.of(SmallAgingCaveBlockEntity::new,
                CheeseBlocks.SMALL_AGING_CAVE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumAgingCaveBlockEntity>> MEDIUM_AGING_CAVE =
        BLOCK_ENTITIES.register("medium_aging_cave", () ->
            BlockEntityType.Builder.of(MediumAgingCaveBlockEntity::new,
                CheeseBlocks.MEDIUM_AGING_CAVE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeAgingCaveBlockEntity>> LARGE_AGING_CAVE =
        BLOCK_ENTITIES.register("large_aging_cave", () ->
            BlockEntityType.Builder.of(LargeAgingCaveBlockEntity::new,
                CheeseBlocks.LARGE_AGING_CAVE.get()
            ).build(null));

    // Packaging Station
    public static final RegistryObject<BlockEntityType<PackagingStationBlockEntity>> PACKAGING_STATION =
        BLOCK_ENTITIES.register("packaging_station", () ->
            BlockEntityType.Builder.of(PackagingStationBlockEntity::new,
                CheeseBlocks.PACKAGING_STATION.get()
            ).build(null));
}
