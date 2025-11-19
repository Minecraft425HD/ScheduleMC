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
    
    // Trocknungsgestell BlockEntity
    public static final RegistryObject<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK = 
        BLOCK_ENTITIES.register("drying_rack", () -> 
            BlockEntityType.Builder.of(DryingRackBlockEntity::new,
                TobaccoBlocks.DRYING_RACK.get()
            ).build(null));
    
    // Fermentierungsfass BlockEntity
    public static final RegistryObject<BlockEntityType<FermentationBarrelBlockEntity>> FERMENTATION_BARREL = 
        BLOCK_ENTITIES.register("fermentation_barrel", () -> 
            BlockEntityType.Builder.of(FermentationBarrelBlockEntity::new,
                TobaccoBlocks.FERMENTATION_BARREL.get()
            ).build(null));
}
