package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Tabak-BlockEntities (ERWEITERT)
 */
public class TobaccoBlockEntities {
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);
    
    // Topf BlockEntity
    public static final RegistryObject<BlockEntityType<TobaccoPotBlockEntity>> TOBACCO_POT = 
        BLOCK_ENTITIES.register("tobacco_pot", () -> 
            BlockEntityType.Builder.of(TobaccoPotBlockEntity::new,
                TobaccoBlocks.TERRACOTTA_POT.get(),
                TobaccoBlocks.CERAMIC_POT.get(),
                TobaccoBlocks.IRON_POT.get(),
                TobaccoBlocks.GOLDEN_POT.get()
            ).build(null));
    
    // Trocknungsgestelle BlockEntities (3 Größen)
    public static final RegistryObject<BlockEntityType<SmallDryingRackBlockEntity>> SMALL_DRYING_RACK =
        BLOCK_ENTITIES.register("small_drying_rack", () ->
            BlockEntityType.Builder.of(SmallDryingRackBlockEntity::new,
                TobaccoBlocks.SMALL_DRYING_RACK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumDryingRackBlockEntity>> MEDIUM_DRYING_RACK =
        BLOCK_ENTITIES.register("medium_drying_rack", () ->
            BlockEntityType.Builder.of(MediumDryingRackBlockEntity::new,
                TobaccoBlocks.MEDIUM_DRYING_RACK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<BigDryingRackBlockEntity>> BIG_DRYING_RACK =
        BLOCK_ENTITIES.register("big_drying_rack", () ->
            BlockEntityType.Builder.of(BigDryingRackBlockEntity::new,
                TobaccoBlocks.BIG_DRYING_RACK.get()
            ).build(null));

    // Fermentierungsfass BlockEntities (3 Größen)
    public static final RegistryObject<BlockEntityType<SmallFermentationBarrelBlockEntity>> SMALL_FERMENTATION_BARREL =
        BLOCK_ENTITIES.register("small_fermentation_barrel", () ->
            BlockEntityType.Builder.of(SmallFermentationBarrelBlockEntity::new,
                TobaccoBlocks.SMALL_FERMENTATION_BARREL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumFermentationBarrelBlockEntity>> MEDIUM_FERMENTATION_BARREL =
        BLOCK_ENTITIES.register("medium_fermentation_barrel", () ->
            BlockEntityType.Builder.of(MediumFermentationBarrelBlockEntity::new,
                TobaccoBlocks.MEDIUM_FERMENTATION_BARREL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<BigFermentationBarrelBlockEntity>> BIG_FERMENTATION_BARREL =
        BLOCK_ENTITIES.register("big_fermentation_barrel", () ->
            BlockEntityType.Builder.of(BigFermentationBarrelBlockEntity::new,
                TobaccoBlocks.BIG_FERMENTATION_BARREL.get()
            ).build(null));

    // Packtische BlockEntities (3 Größen)
    public static final RegistryObject<BlockEntityType<SmallPackagingTableBlockEntity>> SMALL_PACKAGING_TABLE =
        BLOCK_ENTITIES.register("small_packaging_table", () ->
            BlockEntityType.Builder.of(SmallPackagingTableBlockEntity::new,
                TobaccoBlocks.SMALL_PACKAGING_TABLE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumPackagingTableBlockEntity>> MEDIUM_PACKAGING_TABLE =
        BLOCK_ENTITIES.register("medium_packaging_table", () ->
            BlockEntityType.Builder.of(MediumPackagingTableBlockEntity::new,
                TobaccoBlocks.MEDIUM_PACKAGING_TABLE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargePackagingTableBlockEntity>> LARGE_PACKAGING_TABLE =
        BLOCK_ENTITIES.register("large_packaging_table", () ->
            BlockEntityType.Builder.of(LargePackagingTableBlockEntity::new,
                TobaccoBlocks.LARGE_PACKAGING_TABLE.get()
            ).build(null));

    // Grow Light Slabs BlockEntity (alle 3 Tiers teilen sich einen Typ)
    public static final RegistryObject<BlockEntityType<GrowLightSlabBlockEntity>> GROW_LIGHT_SLAB =
        BLOCK_ENTITIES.register("grow_light_slab", () ->
            BlockEntityType.Builder.of(GrowLightSlabBlockEntity::new,
                TobaccoBlocks.BASIC_GROW_LIGHT_SLAB.get(),
                TobaccoBlocks.ADVANCED_GROW_LIGHT_SLAB.get(),
                TobaccoBlocks.PREMIUM_GROW_LIGHT_SLAB.get()
            ).build(null));
}
