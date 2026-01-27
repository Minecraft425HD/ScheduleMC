package de.rolandsw.schedulemc.cheese.blocks;

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
 * Zentrale Registrierung aller Kase-Blocke
 *
 * Struktur:
 * - Pasteurisierungsstation
 * - Dicklegungsbottich (Curdling Vat)
 * - 3x Kasepressen (Small/Medium/Large)
 * - 3x Reifekeller (Small/Medium/Large)
 * - Verpackungsstation
 */
public class CheeseBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
        ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // VORBEREITUNGS-STATIONEN
    // ═══════════════════════════════════════════════════════════

    // Pasteurisierungsstation
    public static final RegistryObject<Block> PASTEURIZATION_STATION = BLOCKS.register("pasteurization_station",
        () -> new PasteurizationStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> PASTEURIZATION_STATION_ITEM = ITEMS.register("pasteurization_station",
        () -> new BlockItem(PASTEURIZATION_STATION.get(), new Item.Properties()));

    // Dicklegungsbottich
    public static final RegistryObject<Block> CURDLING_VAT = BLOCKS.register("curdling_vat",
        () -> new CurdlingVatBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON)));
    public static final RegistryObject<Item> CURDLING_VAT_ITEM = ITEMS.register("curdling_vat",
        () -> new BlockItem(CURDLING_VAT.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // KASEPRESSEN (3 Großen)
    // ═══════════════════════════════════════════════════════════

    // Kleine Kasepresse
    public static final RegistryObject<Block> SMALL_CHEESE_PRESS = BLOCKS.register("small_cheese_press",
        () -> new SmallCheesePressBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> SMALL_CHEESE_PRESS_ITEM = ITEMS.register("small_cheese_press",
        () -> new BlockItem(SMALL_CHEESE_PRESS.get(), new Item.Properties()));

    // Mittlere Kasepresse
    public static final RegistryObject<Block> MEDIUM_CHEESE_PRESS = BLOCKS.register("medium_cheese_press",
        () -> new MediumCheesePressBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> MEDIUM_CHEESE_PRESS_ITEM = ITEMS.register("medium_cheese_press",
        () -> new BlockItem(MEDIUM_CHEESE_PRESS.get(), new Item.Properties()));

    // Große Kasepresse
    public static final RegistryObject<Block> LARGE_CHEESE_PRESS = BLOCKS.register("large_cheese_press",
        () -> new LargeCheesePressBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> LARGE_CHEESE_PRESS_ITEM = ITEMS.register("large_cheese_press",
        () -> new BlockItem(LARGE_CHEESE_PRESS.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // REIFEKELLER (3 Großen)
    // ═══════════════════════════════════════════════════════════

    // Kleiner Reifekeller
    public static final RegistryObject<Block> SMALL_AGING_CAVE = BLOCKS.register("small_aging_cave",
        () -> new SmallAgingCaveBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Item> SMALL_AGING_CAVE_ITEM = ITEMS.register("small_aging_cave",
        () -> new BlockItem(SMALL_AGING_CAVE.get(), new Item.Properties()));

    // Mittlerer Reifekeller
    public static final RegistryObject<Block> MEDIUM_AGING_CAVE = BLOCKS.register("medium_aging_cave",
        () -> new MediumAgingCaveBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Item> MEDIUM_AGING_CAVE_ITEM = ITEMS.register("medium_aging_cave",
        () -> new BlockItem(MEDIUM_AGING_CAVE.get(), new Item.Properties()));

    // Großer Reifekeller
    public static final RegistryObject<Block> LARGE_AGING_CAVE = BLOCKS.register("large_aging_cave",
        () -> new LargeAgingCaveBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Item> LARGE_AGING_CAVE_ITEM = ITEMS.register("large_aging_cave",
        () -> new BlockItem(LARGE_AGING_CAVE.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // VERPACKUNGS-STATION
    // ═══════════════════════════════════════════════════════════

    // Verpackungsstation
    public static final RegistryObject<Block> PACKAGING_STATION = BLOCKS.register("packaging_station",
        () -> new PackagingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> PACKAGING_STATION_ITEM = ITEMS.register("packaging_station",
        () -> new BlockItem(PACKAGING_STATION.get(), new Item.Properties()));
}
