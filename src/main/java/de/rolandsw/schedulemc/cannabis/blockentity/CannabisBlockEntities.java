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

    public static final RegistryObject<BlockEntityType<TrimmStationBlockEntity>> TRIMM_STATION =
            BLOCK_ENTITIES.register("cannabis_trimm_station", () ->
                    BlockEntityType.Builder.of(TrimmStationBlockEntity::new,
                            CannabisBlocks.TRIMM_STATION.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<CuringGlasBlockEntity>> CURING_GLAS =
            BLOCK_ENTITIES.register("cannabis_curing_glas", () ->
                    BlockEntityType.Builder.of(CuringGlasBlockEntity::new,
                            CannabisBlocks.CURING_GLAS.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<HashPresseBlockEntity>> HASH_PRESSE =
            BLOCK_ENTITIES.register("cannabis_hash_presse", () ->
                    BlockEntityType.Builder.of(HashPresseBlockEntity::new,
                            CannabisBlocks.HASH_PRESSE.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<OelExtraktortBlockEntity>> OEL_EXTRAKTOR =
            BLOCK_ENTITIES.register("cannabis_oel_extraktor", () ->
                    BlockEntityType.Builder.of(OelExtraktortBlockEntity::new,
                            CannabisBlocks.OEL_EXTRAKTOR.get()
                    ).build(null));
}
