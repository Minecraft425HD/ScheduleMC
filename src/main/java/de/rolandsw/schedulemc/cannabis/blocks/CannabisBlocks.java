package de.rolandsw.schedulemc.cannabis.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registriert alle Cannabis-Blöcke
 */
public class CannabisBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // CANNABIS-PFLANZEN (4 Strains) - KEIN BlockItem!
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> INDICA_PLANT = BLOCKS.register("cannabis_indica_plant",
            () -> new CannabisPlantBlock(CannabisStrain.INDICA));

    public static final RegistryObject<Block> SATIVA_PLANT = BLOCKS.register("cannabis_sativa_plant",
            () -> new CannabisPlantBlock(CannabisStrain.SATIVA));

    public static final RegistryObject<Block> HYBRID_PLANT = BLOCKS.register("cannabis_hybrid_plant",
            () -> new CannabisPlantBlock(CannabisStrain.HYBRID));

    public static final RegistryObject<Block> AUTOFLOWER_PLANT = BLOCKS.register("cannabis_autoflower_plant",
            () -> new CannabisPlantBlock(CannabisStrain.AUTOFLOWER));

    // ═══════════════════════════════════════════════════════════
    // VERARBEITUNGS-BLÖCKE
    // ═══════════════════════════════════════════════════════════

    // Trimm-Station
    public static final RegistryObject<Block> TRIM_STATION = BLOCKS.register("cannabis_trimm_station",
            () -> new TrimStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final RegistryObject<Item> TRIM_STATION_ITEM = ITEMS.register("cannabis_trimm_station",
            () -> new BlockItem(TRIM_STATION.get(), new Item.Properties()));

    // Curing-Glas
    public static final RegistryObject<Block> CURING_JAR = BLOCKS.register("cannabis_curing_glas",
            () -> new CuringJarBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistryObject<Item> CURING_JAR_ITEM = ITEMS.register("cannabis_curing_glas",
            () -> new BlockItem(CURING_JAR.get(), new Item.Properties()));

    // Hash-Presse
    public static final RegistryObject<Block> HASH_PRESS = BLOCKS.register("cannabis_hash_presse",
            () -> new HashPressBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .sound(SoundType.ANVIL)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> HASH_PRESS_ITEM = ITEMS.register("cannabis_hash_presse",
            () -> new BlockItem(HASH_PRESS.get(), new Item.Properties()));

    // Öl-Extractor
    public static final RegistryObject<Block> OIL_EXTRACTOR = BLOCKS.register("cannabis_oel_extraktor",
            () -> new OilExtractorBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> OIL_EXTRACTOR_ITEM = ITEMS.register("cannabis_oel_extraktor",
            () -> new BlockItem(OIL_EXTRACTOR.get(), new Item.Properties()));
}
