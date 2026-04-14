package de.rolandsw.schedulemc.lsd.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.lsd.blocks.LSDBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registriert alle LSD-BlockEntities
 */
public class LSDBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<BlockEntityType<FermentationTankBlockEntity>> FERMENTATION_TANK =
            BLOCK_ENTITIES.register("fermentations_tank", () ->
                    BlockEntityType.Builder.of(FermentationTankBlockEntity::new,
                            LSDBlocks.FERMENTATION_TANK.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<DistillationApparatusBlockEntity>> DISTILLATION_APPARATUS =
            BLOCK_ENTITIES.register("destillations_apparat", () ->
                    BlockEntityType.Builder.of(DistillationApparatusBlockEntity::new,
                            LSDBlocks.DISTILLATION_APPARATUS.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<MicroDoserBlockEntity>> MICRO_DOSER =
            BLOCK_ENTITIES.register("mikro_dosierer", () ->
                    BlockEntityType.Builder.of(MicroDoserBlockEntity::new,
                            LSDBlocks.MICRO_DOSER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<PerforationPressBlockEntity>> PERFORATION_PRESS =
            BLOCK_ENTITIES.register("perforations_presse", () ->
                    BlockEntityType.Builder.of(PerforationPressBlockEntity::new,
                            LSDBlocks.PERFORATION_PRESS.get()
                    ).build(null));
}
