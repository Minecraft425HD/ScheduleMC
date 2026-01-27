package de.rolandsw.schedulemc.honey.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Honig-Blöcke
 *
 * Struktur:
 * - Bienenstöcke (verschiedene Typen)
 * - Honig-Extraktoren (zum Ernten)
 * - Filterstation (zum Reinigen)
 * - Reifekammern (3 Größen)
 * - Verarbeitungsstation (für Cremehonig, etc.)
 * - Abfüllstation (zum Abfüllen in Gläser)
 */
public class HoneyBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
        ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // BIENENSTÖCKE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> BEEHIVE = BLOCKS.register("beehive",
        () -> new BeehiveBlock(BlockBehaviour.Properties.copy(Blocks.BEEHIVE)));
    public static final RegistryObject<Item> BEEHIVE_ITEM = ITEMS.register("beehive",
        () -> new BlockItem(BEEHIVE.get(), new Item.Properties()));

    public static final RegistryObject<Block> ADVANCED_BEEHIVE = BLOCKS.register("advanced_beehive",
        () -> new AdvancedBeehiveBlock(BlockBehaviour.Properties.copy(Blocks.BEEHIVE)));
    public static final RegistryObject<Item> ADVANCED_BEEHIVE_ITEM = ITEMS.register("advanced_beehive",
        () -> new BlockItem(ADVANCED_BEEHIVE.get(), new Item.Properties()));

    public static final RegistryObject<Block> APIARY = BLOCKS.register("apiary",
        () -> new ApiaryBlock(BlockBehaviour.Properties.copy(Blocks.BEEHIVE)));
    public static final RegistryObject<Item> APIARY_ITEM = ITEMS.register("apiary",
        () -> new BlockItem(APIARY.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // HONIG-EXTRAKTOR (zum Ernten der Waben)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> HONEY_EXTRACTOR = BLOCKS.register("honey_extractor",
        () -> new HoneyExtractorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> HONEY_EXTRACTOR_ITEM = ITEMS.register("honey_extractor",
        () -> new BlockItem(HONEY_EXTRACTOR.get(), new Item.Properties()));

    public static final RegistryObject<Block> CENTRIFUGAL_EXTRACTOR = BLOCKS.register("centrifugal_extractor",
        () -> new CentrifugalExtractorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> CENTRIFUGAL_EXTRACTOR_ITEM = ITEMS.register("centrifugal_extractor",
        () -> new BlockItem(CENTRIFUGAL_EXTRACTOR.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // FILTERSTATION (zum Reinigen & Filtern)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> FILTERING_STATION = BLOCKS.register("filtering_station",
        () -> new FilteringStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> FILTERING_STATION_ITEM = ITEMS.register("filtering_station",
        () -> new BlockItem(FILTERING_STATION.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // REIFEKAMMERN (3 Größen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> SMALL_AGING_CHAMBER = BLOCKS.register("small_aging_chamber",
        () -> new SmallAgingChamberBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> SMALL_AGING_CHAMBER_ITEM = ITEMS.register("small_aging_chamber",
        () -> new BlockItem(SMALL_AGING_CHAMBER.get(), new Item.Properties()));

    public static final RegistryObject<Block> MEDIUM_AGING_CHAMBER = BLOCKS.register("medium_aging_chamber",
        () -> new MediumAgingChamberBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> MEDIUM_AGING_CHAMBER_ITEM = ITEMS.register("medium_aging_chamber",
        () -> new BlockItem(MEDIUM_AGING_CHAMBER.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARGE_AGING_CHAMBER = BLOCKS.register("large_aging_chamber",
        () -> new LargeAgingChamberBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> LARGE_AGING_CHAMBER_ITEM = ITEMS.register("large_aging_chamber",
        () -> new BlockItem(LARGE_AGING_CHAMBER.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // VERARBEITUNGSSTATION (für spezielle Honig-Varianten)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> PROCESSING_STATION = BLOCKS.register("processing_station",
        () -> new ProcessingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> PROCESSING_STATION_ITEM = ITEMS.register("processing_station",
        () -> new BlockItem(PROCESSING_STATION.get(), new Item.Properties()));

    public static final RegistryObject<Block> CREAMING_STATION = BLOCKS.register("creaming_station",
        () -> new CreamingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> CREAMING_STATION_ITEM = ITEMS.register("creaming_station",
        () -> new BlockItem(CREAMING_STATION.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // ABFÜLLSTATION (zum Abfüllen in Gläser)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> BOTTLING_STATION = BLOCKS.register("bottling_station",
        () -> new BottlingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> BOTTLING_STATION_ITEM = ITEMS.register("bottling_station",
        () -> new BlockItem(BOTTLING_STATION.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // LAGERUNG & DISPLAY
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> HONEY_STORAGE_BARREL = BLOCKS.register("honey_storage_barrel",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> HONEY_STORAGE_BARREL_ITEM = ITEMS.register("honey_storage_barrel",
        () -> new BlockItem(HONEY_STORAGE_BARREL.get(), new Item.Properties()));

    public static final RegistryObject<Block> HONEY_DISPLAY_CASE = BLOCKS.register("honey_display_case",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Item> HONEY_DISPLAY_CASE_ITEM = ITEMS.register("honey_display_case",
        () -> new BlockItem(HONEY_DISPLAY_CASE.get(), new Item.Properties()));
}
