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
    public static final RegistryObject<BlockEntityType<ChemieMixerBlockEntity>> CHEMIE_MIXER =
            BLOCK_ENTITIES.register("chemie_mixer", () ->
                    BlockEntityType.Builder.of(ChemieMixerBlockEntity::new,
                            MethBlocks.CHEMIE_MIXER.get()
                    ).build(null));

    // Reduktionskessel
    public static final RegistryObject<BlockEntityType<ReduktionskesselBlockEntity>> REDUKTIONSKESSEL =
            BLOCK_ENTITIES.register("reduktionskessel", () ->
                    BlockEntityType.Builder.of(ReduktionskesselBlockEntity::new,
                            MethBlocks.REDUKTIONSKESSEL.get()
                    ).build(null));

    // Kristallisator
    public static final RegistryObject<BlockEntityType<KristallisatorBlockEntity>> KRISTALLISATOR =
            BLOCK_ENTITIES.register("kristallisator", () ->
                    BlockEntityType.Builder.of(KristallisatorBlockEntity::new,
                            MethBlocks.KRISTALLISATOR.get()
                    ).build(null));

    // Vakuum-Trockner
    public static final RegistryObject<BlockEntityType<VakuumTrocknerBlockEntity>> VAKUUM_TROCKNER =
            BLOCK_ENTITIES.register("vakuum_trockner", () ->
                    BlockEntityType.Builder.of(VakuumTrocknerBlockEntity::new,
                            MethBlocks.VAKUUM_TROCKNER.get()
                    ).build(null));
}
