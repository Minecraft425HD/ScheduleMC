package de.rolandsw.schedulemc.wine.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.wine.WineType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WineBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
        ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID
    );
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // Grapevine Plant
    public static final RegistryObject<Block> GRAPEVINE = BLOCKS.register("grapevine",
        () -> new GrapevineBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)));

    // Grapevine Pots (4 varieties)
    public static final RegistryObject<Block> RIESLING_GRAPEVINE_POT = BLOCKS.register("riesling_grapevine_pot",
        () -> new GrapevinePotBlock(WineType.RIESLING, BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));
    public static final RegistryObject<Item> RIESLING_GRAPEVINE_POT_ITEM = ITEMS.register("riesling_grapevine_pot",
        () -> new BlockItem(RIESLING_GRAPEVINE_POT.get(), new Item.Properties()));

    public static final RegistryObject<Block> SPAETBURGUNDER_GRAPEVINE_POT = BLOCKS.register("spaetburgunder_grapevine_pot",
        () -> new GrapevinePotBlock(WineType.SPAETBURGUNDER, BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));
    public static final RegistryObject<Item> SPAETBURGUNDER_GRAPEVINE_POT_ITEM = ITEMS.register("spaetburgunder_grapevine_pot",
        () -> new BlockItem(SPAETBURGUNDER_GRAPEVINE_POT.get(), new Item.Properties()));

    public static final RegistryObject<Block> CHARDONNAY_GRAPEVINE_POT = BLOCKS.register("chardonnay_grapevine_pot",
        () -> new GrapevinePotBlock(WineType.CHARDONNAY, BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));
    public static final RegistryObject<Item> CHARDONNAY_GRAPEVINE_POT_ITEM = ITEMS.register("chardonnay_grapevine_pot",
        () -> new BlockItem(CHARDONNAY_GRAPEVINE_POT.get(), new Item.Properties()));

    public static final RegistryObject<Block> MERLOT_GRAPEVINE_POT = BLOCKS.register("merlot_grapevine_pot",
        () -> new GrapevinePotBlock(WineType.MERLOT, BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));
    public static final RegistryObject<Item> MERLOT_GRAPEVINE_POT_ITEM = ITEMS.register("merlot_grapevine_pot",
        () -> new BlockItem(MERLOT_GRAPEVINE_POT.get(), new Item.Properties()));

    // Crushing Station
    public static final RegistryObject<Block> CRUSHING_STATION = BLOCKS.register("crushing_station",
        () -> new CrushingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> CRUSHING_STATION_ITEM = ITEMS.register("crushing_station",
        () -> new BlockItem(CRUSHING_STATION.get(), new Item.Properties()));

    // Pressing Stations (3 sizes)
    public static final RegistryObject<Block> SMALL_WINE_PRESS = BLOCKS.register("small_wine_press",
        () -> new SmallWinePressBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> SMALL_WINE_PRESS_ITEM = ITEMS.register("small_wine_press",
        () -> new BlockItem(SMALL_WINE_PRESS.get(), new Item.Properties()));

    public static final RegistryObject<Block> MEDIUM_WINE_PRESS = BLOCKS.register("medium_wine_press",
        () -> new MediumWinePressBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> MEDIUM_WINE_PRESS_ITEM = ITEMS.register("medium_wine_press",
        () -> new BlockItem(MEDIUM_WINE_PRESS.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARGE_WINE_PRESS = BLOCKS.register("large_wine_press",
        () -> new LargeWinePressBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> LARGE_WINE_PRESS_ITEM = ITEMS.register("large_wine_press",
        () -> new BlockItem(LARGE_WINE_PRESS.get(), new Item.Properties()));

    // Fermentation Tanks (3 sizes)
    public static final RegistryObject<Block> SMALL_FERMENTATION_TANK = BLOCKS.register("small_fermentation_tank",
        () -> new SmallFermentationTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> SMALL_FERMENTATION_TANK_ITEM = ITEMS.register("small_fermentation_tank",
        () -> new BlockItem(SMALL_FERMENTATION_TANK.get(), new Item.Properties()));

    public static final RegistryObject<Block> MEDIUM_FERMENTATION_TANK = BLOCKS.register("medium_fermentation_tank",
        () -> new MediumFermentationTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> MEDIUM_FERMENTATION_TANK_ITEM = ITEMS.register("medium_fermentation_tank",
        () -> new BlockItem(MEDIUM_FERMENTATION_TANK.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARGE_FERMENTATION_TANK = BLOCKS.register("large_fermentation_tank",
        () -> new LargeFermentationTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> LARGE_FERMENTATION_TANK_ITEM = ITEMS.register("large_fermentation_tank",
        () -> new BlockItem(LARGE_FERMENTATION_TANK.get(), new Item.Properties()));

    // Aging Barrels (3 sizes)
    public static final RegistryObject<Block> SMALL_AGING_BARREL = BLOCKS.register("small_aging_barrel",
        () -> new SmallAgingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> SMALL_AGING_BARREL_ITEM = ITEMS.register("small_aging_barrel",
        () -> new BlockItem(SMALL_AGING_BARREL.get(), new Item.Properties()));

    public static final RegistryObject<Block> MEDIUM_AGING_BARREL = BLOCKS.register("medium_aging_barrel",
        () -> new MediumAgingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> MEDIUM_AGING_BARREL_ITEM = ITEMS.register("medium_aging_barrel",
        () -> new BlockItem(MEDIUM_AGING_BARREL.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARGE_AGING_BARREL = BLOCKS.register("large_aging_barrel",
        () -> new LargeAgingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> LARGE_AGING_BARREL_ITEM = ITEMS.register("large_aging_barrel",
        () -> new BlockItem(LARGE_AGING_BARREL.get(), new Item.Properties()));

    // Bottling Station
    public static final RegistryObject<Block> WINE_BOTTLING_STATION = BLOCKS.register("wine_bottling_station",
        () -> new WineBottlingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Item> WINE_BOTTLING_STATION_ITEM = ITEMS.register("wine_bottling_station",
        () -> new BlockItem(WINE_BOTTLING_STATION.get(), new Item.Properties()));
}
