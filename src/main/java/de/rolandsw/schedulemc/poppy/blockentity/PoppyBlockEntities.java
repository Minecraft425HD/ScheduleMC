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

    public static final RegistryObject<BlockEntityType<RitzmaschineBlockEntity>> RITZMASCHINE =
            BLOCK_ENTITIES.register("ritzmaschine",
                    () -> BlockEntityType.Builder.of(RitzmaschineBlockEntity::new,
                            PoppyBlocks.RITZMASCHINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<OpiumPresseBlockEntity>> OPIUM_PRESSE =
            BLOCK_ENTITIES.register("opium_presse",
                    () -> BlockEntityType.Builder.of(OpiumPresseBlockEntity::new,
                            PoppyBlocks.OPIUM_PRESSE.get()).build(null));

    public static final RegistryObject<BlockEntityType<KochstationBlockEntity>> KOCHSTATION =
            BLOCK_ENTITIES.register("kochstation",
                    () -> BlockEntityType.Builder.of(KochstationBlockEntity::new,
                            PoppyBlocks.KOCHSTATION.get()).build(null));

    public static final RegistryObject<BlockEntityType<HeroinRaffinerieBlockEntity>> HEROIN_RAFFINERIE =
            BLOCK_ENTITIES.register("heroin_raffinerie",
                    () -> BlockEntityType.Builder.of(HeroinRaffinerieBlockEntity::new,
                            PoppyBlocks.HEROIN_RAFFINERIE.get()).build(null));
}
