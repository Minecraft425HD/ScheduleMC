package de.rolandsw.schedulemc.cannabis.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registriert alle Cannabis-BlockEntities
 */
public class CannabisBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<BlockEntityType<TrimStationBlockEntity>> TRIM_STATION =
            BLOCK_ENTITIES.register("cannabis_trimm_station", () ->
                    BlockEntityType.Builder.of(TrimStationBlockEntity::new,
                            CannabisBlocks.TRIM_STATION.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<CuringJarBlockEntity>> CURING_JAR =
            BLOCK_ENTITIES.register("cannabis_curing_glas", () ->
                    BlockEntityType.Builder.of(CuringJarBlockEntity::new,
                            CannabisBlocks.CURING_JAR.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<HashPressBlockEntity>> HASH_PRESS =
            BLOCK_ENTITIES.register("cannabis_hash_presse", () ->
                    BlockEntityType.Builder.of(HashPressBlockEntity::new,
                            CannabisBlocks.HASH_PRESS.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<OilExtractorBlockEntity>> OIL_EXTRACTOR =
            BLOCK_ENTITIES.register("cannabis_oel_extraktor", () ->
                    BlockEntityType.Builder.of(OilExtractorBlockEntity::new,
                            CannabisBlocks.OIL_EXTRACTOR.get()
                    ).build(null));
}
