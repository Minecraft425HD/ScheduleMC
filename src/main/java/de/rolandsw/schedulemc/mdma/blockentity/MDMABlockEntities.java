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

    public static final RegistryObject<BlockEntityType<ReactionCauldronBlockEntity>> REACTION_CAULDRON =
            BLOCK_ENTITIES.register("reaktions_kessel", () ->
                    BlockEntityType.Builder.of(ReactionCauldronBlockEntity::new,
                            MDMABlocks.REACTION_CAULDRON.get()
                    ).build(null));

    /**
     * @deprecated Use {@link #REACTION_CAULDRON} instead
     */
    @Deprecated
    public static final RegistryObject<BlockEntityType<ReactionCauldronBlockEntity>> REAKTIONS_KESSEL = REACTION_CAULDRON;

    public static final RegistryObject<BlockEntityType<DryingOvenBlockEntity>> DRYING_OVEN =
            BLOCK_ENTITIES.register("trocknungs_ofen", () ->
                    BlockEntityType.Builder.of(DryingOvenBlockEntity::new,
                            MDMABlocks.DRYING_OVEN.get()
                    ).build(null));

    /**
     * @deprecated Use {@link #DRYING_OVEN} instead
     */
    @Deprecated
    public static final RegistryObject<BlockEntityType<DryingOvenBlockEntity>> TROCKNUNGS_OFEN = DRYING_OVEN;

    public static final RegistryObject<BlockEntityType<PillenPresseBlockEntity>> PILLEN_PRESSE =
            BLOCK_ENTITIES.register("pillen_presse", () ->
                    BlockEntityType.Builder.of(PillenPresseBlockEntity::new,
                            MDMABlocks.PILLEN_PRESSE.get()
                    ).build(null));
}
