package de.rolandsw.schedulemc.cannabis.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
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

    // Trocknungsnetz
    public static final RegistryObject<Block> TROCKNUNGSNETZ = BLOCKS.register("cannabis_trocknungsnetz",
            () -> new TrocknungsnetzBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f)
                    .sound(SoundType.SCAFFOLDING)
                    .noOcclusion()));

    public static final RegistryObject<Item> TROCKNUNGSNETZ_ITEM = ITEMS.register("cannabis_trocknungsnetz",
            () -> new BlockItem(TROCKNUNGSNETZ.get(), new Item.Properties()));

    // Trimm-Station
    public static final RegistryObject<Block> TRIMM_STATION = BLOCKS.register("cannabis_trimm_station",
            () -> new TrimmStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final RegistryObject<Item> TRIMM_STATION_ITEM = ITEMS.register("cannabis_trimm_station",
            () -> new BlockItem(TRIMM_STATION.get(), new Item.Properties()));

    // Curing-Glas
    public static final RegistryObject<Block> CURING_GLAS = BLOCKS.register("cannabis_curing_glas",
            () -> new CuringGlasBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistryObject<Item> CURING_GLAS_ITEM = ITEMS.register("cannabis_curing_glas",
            () -> new BlockItem(CURING_GLAS.get(), new Item.Properties()));

    // Hash-Presse
    public static final RegistryObject<Block> HASH_PRESSE = BLOCKS.register("cannabis_hash_presse",
            () -> new HashPresseBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .sound(SoundType.ANVIL)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> HASH_PRESSE_ITEM = ITEMS.register("cannabis_hash_presse",
            () -> new BlockItem(HASH_PRESSE.get(), new Item.Properties()));

    // Öl-Extraktor
    public static final RegistryObject<Block> OEL_EXTRAKTOR = BLOCKS.register("cannabis_oel_extraktor",
            () -> new OelExtraktortBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> OEL_EXTRAKTOR_ITEM = ITEMS.register("cannabis_oel_extraktor",
            () -> new BlockItem(OEL_EXTRAKTOR.get(), new Item.Properties()));
}
