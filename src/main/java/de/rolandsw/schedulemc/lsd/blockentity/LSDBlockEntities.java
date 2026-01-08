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

    public static final RegistryObject<BlockEntityType<FermentationsTankBlockEntity>> FERMENTATIONS_TANK =
            BLOCK_ENTITIES.register("fermentations_tank", () ->
                    BlockEntityType.Builder.of(FermentationsTankBlockEntity::new,
                            LSDBlocks.FERMENTATIONS_TANK.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<DestillationsApparatBlockEntity>> DESTILLATIONS_APPARAT =
            BLOCK_ENTITIES.register("destillations_apparat", () ->
                    BlockEntityType.Builder.of(DestillationsApparatBlockEntity::new,
                            LSDBlocks.DESTILLATIONS_APPARAT.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<MikroDosiererBlockEntity>> MIKRO_DOSIERER =
            BLOCK_ENTITIES.register("mikro_dosierer", () ->
                    BlockEntityType.Builder.of(MikroDosiererBlockEntity::new,
                            LSDBlocks.MIKRO_DOSIERER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<PerforationsPresseBlockEntity>> PERFORATIONS_PRESSE =
            BLOCK_ENTITIES.register("perforations_presse", () ->
                    BlockEntityType.Builder.of(PerforationsPresseBlockEntity::new,
                            LSDBlocks.PERFORATIONS_PRESSE.get()
                    ).build(null));
}
