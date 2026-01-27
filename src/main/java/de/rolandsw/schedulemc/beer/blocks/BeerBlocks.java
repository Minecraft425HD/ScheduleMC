package de.rolandsw.schedulemc.beer.blocks;

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
 * Zentrale Registrierung aller Bier-Blöcke
 *
 * Struktur:
 * - Malting Station (Mälzen von Getreide)
 * - Mash Tun (Maischen)
 * - Brew Kettles (Kochen, 3 Größen)
 * - Fermentation Tanks (Gärung, 3 Größen)
 * - Conditioning Tanks (Nachgärung/Reifung, 3 Größen)
 * - Bottling Station (Abfüllung)
 */
public class BeerBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
        ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // MALTING STATION (Mälzen von Getreide)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> MALTING_STATION = BLOCKS.register("malting_station",
        () -> new MaltingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> MALTING_STATION_ITEM = ITEMS.register("malting_station",
        () -> new BlockItem(MALTING_STATION.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // MASH TUN (Maischbottich für Maischvorgang)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> MASH_TUN = BLOCKS.register("mash_tun",
        () -> new MashTunBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> MASH_TUN_ITEM = ITEMS.register("mash_tun",
        () -> new BlockItem(MASH_TUN.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // BREW KETTLES (Sudkessel, 3 Größen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> SMALL_BREW_KETTLE = BLOCKS.register("small_brew_kettle",
        () -> new SmallBrewKettleBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> SMALL_BREW_KETTLE_ITEM = ITEMS.register("small_brew_kettle",
        () -> new BlockItem(SMALL_BREW_KETTLE.get(), new Item.Properties()));

    public static final RegistryObject<Block> MEDIUM_BREW_KETTLE = BLOCKS.register("medium_brew_kettle",
        () -> new MediumBrewKettleBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
            .strength(4.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> MEDIUM_BREW_KETTLE_ITEM = ITEMS.register("medium_brew_kettle",
        () -> new BlockItem(MEDIUM_BREW_KETTLE.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARGE_BREW_KETTLE = BLOCKS.register("large_brew_kettle",
        () -> new LargeBrewKettleBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
            .strength(4.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> LARGE_BREW_KETTLE_ITEM = ITEMS.register("large_brew_kettle",
        () -> new BlockItem(LARGE_BREW_KETTLE.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // FERMENTATION TANKS (Gärtanks, 3 Größen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> SMALL_FERMENTATION_TANK = BLOCKS.register("small_beer_fermentation_tank",
        () -> new SmallBeerFermentationTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> SMALL_FERMENTATION_TANK_ITEM = ITEMS.register("small_beer_fermentation_tank",
        () -> new BlockItem(SMALL_FERMENTATION_TANK.get(), new Item.Properties()));

    public static final RegistryObject<Block> MEDIUM_FERMENTATION_TANK = BLOCKS.register("medium_beer_fermentation_tank",
        () -> new MediumBeerFermentationTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(4.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> MEDIUM_FERMENTATION_TANK_ITEM = ITEMS.register("medium_beer_fermentation_tank",
        () -> new BlockItem(MEDIUM_FERMENTATION_TANK.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARGE_FERMENTATION_TANK = BLOCKS.register("large_beer_fermentation_tank",
        () -> new LargeBeerFermentationTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(4.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> LARGE_FERMENTATION_TANK_ITEM = ITEMS.register("large_beer_fermentation_tank",
        () -> new BlockItem(LARGE_FERMENTATION_TANK.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // CONDITIONING TANKS (Lagertanks für Nachgärung, 3 Größen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> SMALL_CONDITIONING_TANK = BLOCKS.register("small_conditioning_tank",
        () -> new SmallConditioningTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> SMALL_CONDITIONING_TANK_ITEM = ITEMS.register("small_conditioning_tank",
        () -> new BlockItem(SMALL_CONDITIONING_TANK.get(), new Item.Properties()));

    public static final RegistryObject<Block> MEDIUM_CONDITIONING_TANK = BLOCKS.register("medium_conditioning_tank",
        () -> new MediumConditioningTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(4.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> MEDIUM_CONDITIONING_TANK_ITEM = ITEMS.register("medium_conditioning_tank",
        () -> new BlockItem(MEDIUM_CONDITIONING_TANK.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARGE_CONDITIONING_TANK = BLOCKS.register("large_conditioning_tank",
        () -> new LargeConditioningTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(4.5f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> LARGE_CONDITIONING_TANK_ITEM = ITEMS.register("large_conditioning_tank",
        () -> new BlockItem(LARGE_CONDITIONING_TANK.get(), new Item.Properties()));

    // ═══════════════════════════════════════════════════════════
    // BOTTLING STATION (Abfüllstation)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> BOTTLING_STATION = BLOCKS.register("beer_bottling_station",
        () -> new BottlingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> BOTTLING_STATION_ITEM = ITEMS.register("beer_bottling_station",
        () -> new BlockItem(BOTTLING_STATION.get(), new Item.Properties()));
}
