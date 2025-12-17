package de.rolandsw.schedulemc.mdma.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.mdma.blocks.MDMABlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registriert alle MDMA-BlockEntities
 */
public class MDMABlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<BlockEntityType<ReaktionsKesselBlockEntity>> REAKTIONS_KESSEL =
            BLOCK_ENTITIES.register("reaktions_kessel", () ->
                    BlockEntityType.Builder.of(ReaktionsKesselBlockEntity::new,
                            MDMABlocks.REAKTIONS_KESSEL.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<TrocknungsOfenBlockEntity>> TROCKNUNGS_OFEN =
            BLOCK_ENTITIES.register("trocknungs_ofen", () ->
                    BlockEntityType.Builder.of(TrocknungsOfenBlockEntity::new,
                            MDMABlocks.TROCKNUNGS_OFEN.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<PillenPresseBlockEntity>> PILLEN_PRESSE =
            BLOCK_ENTITIES.register("pillen_presse", () ->
                    BlockEntityType.Builder.of(PillenPresseBlockEntity::new,
                            MDMABlocks.PILLEN_PRESSE.get()
                    ).build(null));
}
