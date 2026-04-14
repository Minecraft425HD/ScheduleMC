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

    public static final RegistryObject<BlockEntityType<ReactionKettleBlockEntity>> REACTION_KETTLE =
            BLOCK_ENTITIES.register("reaktions_kessel", () ->
                    BlockEntityType.Builder.of(ReactionKettleBlockEntity::new,
                            MDMABlocks.REACTION_KETTLE.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<DryingOvenBlockEntity>> DRYING_OVEN =
            BLOCK_ENTITIES.register("trocknungs_ofen", () ->
                    BlockEntityType.Builder.of(DryingOvenBlockEntity::new,
                            MDMABlocks.DRYING_OVEN.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<PillPressBlockEntity>> PILL_PRESS =
            BLOCK_ENTITIES.register("pillen_presse", () ->
                    BlockEntityType.Builder.of(PillPressBlockEntity::new,
                            MDMABlocks.PILL_PRESS.get()
                    ).build(null));
}
