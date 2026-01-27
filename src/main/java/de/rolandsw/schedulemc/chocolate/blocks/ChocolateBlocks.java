package de.rolandsw.schedulemc.chocolate.blocks;

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
 * Zentrale Registrierung aller Schokoladen-Blöcke
 *
 * Produktions-Pipeline:
 * 1. Roasting Station - Rösten der Kakaobohnen
 * 2. Winnowing Machine - Schälen und Trennen
 * 3. Grinding Mill - Mahlen zu Kakaomasse
 * 4. Pressing Station - Pressen zu Butter und Pulver
 * 5. Conching Machine (3 Größen) - Conchieren für Geschmack
 * 6. Tempering Station - Temperieren für Glanz
 * 7. Molding Station (3 Größen) - Formen der Schokolade
 */
public class ChocolateBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
        ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // VERARBEITUNGSSTATIONEN
    // ═══════════════════════════════════════════════════════════

    // Roasting Station - Rösten der Kakaobohnen
    public static final RegistryObject<Block> ROASTING_STATION = BLOCKS.register("roasting_station",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> ROASTING_STATION_ITEM = ITEMS.register("roasting_station",
        () -> new BlockItem(ROASTING_STATION.get(), new Item.Properties()));

    // Winnowing Machine - Schälen und Trennen der Schale von den Nibs
    public static final RegistryObject<Block> WINNOWING_MACHINE = BLOCKS.register("winnowing_machine",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> WINNOWING_MACHINE_ITEM = ITEMS.register("winnowing_machine",
        () -> new BlockItem(WINNOWING_MACHINE.get(), new Item.Properties()));

    // Grinding Mill - Mahlen der Nibs zu Kakaomasse
    public static final RegistryObject<Block> GRINDING_MILL = BLOCKS.register("grinding_mill",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> GRINDING_MILL_ITEM = ITEMS.register("grinding_mill",
        () -> new BlockItem(GRINDING_MILL.get(), new Item.Properties()));

    // Pressing Station - Pressen zu Kakaobutter und Kakaopulver
    public static final RegistryObject<Block> PRESSING_STATION = BLOCKS.register("pressing_station",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(4.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> PRESSING_STATION_ITEM = ITEMS.register("pressing_station",
        () -> new BlockItem(PRESSING_STATION.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // CONCHING MACHINES (3 Größen) - Conchieren für glatten Geschmack
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> SMALL_CONCHING_MACHINE = BLOCKS.register("small_conching_machine",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> SMALL_CONCHING_MACHINE_ITEM = ITEMS.register("small_conching_machine",
        () -> new BlockItem(SMALL_CONCHING_MACHINE.get(), new Item.Properties()));

    public static final RegistryObject<Block> MEDIUM_CONCHING_MACHINE = BLOCKS.register("medium_conching_machine",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(4.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> MEDIUM_CONCHING_MACHINE_ITEM = ITEMS.register("medium_conching_machine",
        () -> new BlockItem(MEDIUM_CONCHING_MACHINE.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARGE_CONCHING_MACHINE = BLOCKS.register("large_conching_machine",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(4.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> LARGE_CONCHING_MACHINE_ITEM = ITEMS.register("large_conching_machine",
        () -> new BlockItem(LARGE_CONCHING_MACHINE.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // TEMPERING STATION - Temperieren für Glanz und Snap
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> TEMPERING_STATION = BLOCKS.register("tempering_station",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> TEMPERING_STATION_ITEM = ITEMS.register("tempering_station",
        () -> new BlockItem(TEMPERING_STATION.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // MOLDING STATIONS (3 Größen) - Formen der Schokolade
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> SMALL_MOLDING_STATION = BLOCKS.register("small_molding_station",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> SMALL_MOLDING_STATION_ITEM = ITEMS.register("small_molding_station",
        () -> new BlockItem(SMALL_MOLDING_STATION.get(), new Item.Properties()));

    public static final RegistryObject<Block> MEDIUM_MOLDING_STATION = BLOCKS.register("medium_molding_station",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> MEDIUM_MOLDING_STATION_ITEM = ITEMS.register("medium_molding_station",
        () -> new BlockItem(MEDIUM_MOLDING_STATION.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARGE_MOLDING_STATION = BLOCKS.register("large_molding_station",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(4.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> LARGE_MOLDING_STATION_ITEM = ITEMS.register("large_molding_station",
        () -> new BlockItem(LARGE_MOLDING_STATION.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // ZUSÄTZLICHE STATIONEN
    // ═══════════════════════════════════════════════════════════

    // Enrobing Machine - Überziehen mit Schokolade
    public static final RegistryObject<Block> ENROBING_MACHINE = BLOCKS.register("enrobing_machine",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> ENROBING_MACHINE_ITEM = ITEMS.register("enrobing_machine",
        () -> new BlockItem(ENROBING_MACHINE.get(), new Item.Properties()));

    // Cooling Tunnel - Kühlen der Schokolade
    public static final RegistryObject<Block> COOLING_TUNNEL = BLOCKS.register("cooling_tunnel",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> COOLING_TUNNEL_ITEM = ITEMS.register("cooling_tunnel",
        () -> new BlockItem(COOLING_TUNNEL.get(), new Item.Properties()));

    // Wrapping Station - Verpacken der Schokolade
    public static final RegistryObject<Block> WRAPPING_STATION = BLOCKS.register("wrapping_station",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(2.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> WRAPPING_STATION_ITEM = ITEMS.register("wrapping_station",
        () -> new BlockItem(WRAPPING_STATION.get(), new Item.Properties()));

    // Storage Cabinet - Lagerung der fertigen Schokolade
    public static final RegistryObject<Block> CHOCOLATE_STORAGE_CABINET = BLOCKS.register("chocolate_storage_cabinet",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.BARREL)
            .strength(2.5f, 3.0f)));
    public static final RegistryObject<Item> CHOCOLATE_STORAGE_CABINET_ITEM = ITEMS.register("chocolate_storage_cabinet",
        () -> new BlockItem(CHOCOLATE_STORAGE_CABINET.get(), new Item.Properties()));
}
