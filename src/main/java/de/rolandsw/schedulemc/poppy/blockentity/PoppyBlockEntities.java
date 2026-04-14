package de.rolandsw.schedulemc.poppy.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry für alle Mohn-bezogenen BlockEntities
 */
public class PoppyBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<BlockEntityType<ScoringMachineBlockEntity>> SCORING_MACHINE =
            BLOCK_ENTITIES.register("ritzmaschine",
                    () -> BlockEntityType.Builder.of(ScoringMachineBlockEntity::new,
                            PoppyBlocks.SCORING_MACHINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<OpiumPressBlockEntity>> OPIUM_PRESS =
            BLOCK_ENTITIES.register("opium_presse",
                    () -> BlockEntityType.Builder.of(OpiumPressBlockEntity::new,
                            PoppyBlocks.OPIUM_PRESS.get()).build(null));

    public static final RegistryObject<BlockEntityType<CookingStationBlockEntity>> COOKING_STATION =
            BLOCK_ENTITIES.register("kochstation",
                    () -> BlockEntityType.Builder.of(CookingStationBlockEntity::new,
                            PoppyBlocks.COOKING_STATION.get()).build(null));

    public static final RegistryObject<BlockEntityType<HeroinRefineryBlockEntity>> HEROIN_REFINERY =
            BLOCK_ENTITIES.register("heroin_raffinerie",
                    () -> BlockEntityType.Builder.of(HeroinRefineryBlockEntity::new,
                            PoppyBlocks.HEROIN_REFINERY.get()).build(null));
}
