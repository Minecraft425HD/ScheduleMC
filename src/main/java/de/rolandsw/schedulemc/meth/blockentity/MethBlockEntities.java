package de.rolandsw.schedulemc.meth.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.meth.blocks.MethBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registriert alle Meth-BlockEntities
 */
public class MethBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    // Chemie-Mixer
    public static final RegistryObject<BlockEntityType<ChemicalMixerBlockEntity>> CHEMICAL_MIXER =
            BLOCK_ENTITIES.register("chemie_mixer", () ->
                    BlockEntityType.Builder.of(ChemicalMixerBlockEntity::new,
                            MethBlocks.CHEMICAL_MIXER.get()
                    ).build(null));

    // ReductionKettle
    public static final RegistryObject<BlockEntityType<ReductionKettleBlockEntity>> REDUCTION_KETTLE =
            BLOCK_ENTITIES.register("reduktionskessel", () ->
                    BlockEntityType.Builder.of(ReductionKettleBlockEntity::new,
                            MethBlocks.REDUCTION_KETTLE.get()
                    ).build(null));

    // Crystallizer
    public static final RegistryObject<BlockEntityType<CrystallizerBlockEntity>> CRYSTALLIZER =
            BLOCK_ENTITIES.register("kristallisator", () ->
                    BlockEntityType.Builder.of(CrystallizerBlockEntity::new,
                            MethBlocks.CRYSTALLIZER.get()
                    ).build(null));

    // Vakuum-Trockner
    public static final RegistryObject<BlockEntityType<VacuumDryerBlockEntity>> VACUUM_DRYER =
            BLOCK_ENTITIES.register("vakuum_trockner", () ->
                    BlockEntityType.Builder.of(VacuumDryerBlockEntity::new,
                            MethBlocks.VACUUM_DRYER.get()
                    ).build(null));
}
