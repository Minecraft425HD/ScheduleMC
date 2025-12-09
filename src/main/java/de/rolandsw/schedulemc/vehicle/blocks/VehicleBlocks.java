package de.rolandsw.schedulemc.vehicle.blocks;

import de.rolandsw.schedulemc.vehicle.VehicleMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Registry for all vehicle-related blocks.
 */
public class VehicleBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, VehicleMod.MOD_ID);

    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VehicleMod.MOD_ID);

    // Gas Station Blocks
    public static final RegistryObject<Block> GAS_STATION = registerBlockWithItem(
            "gas_station",
            () -> new GasStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    public static final RegistryObject<Block> GAS_STATION_TOP = registerBlock(
            "gas_station_top",
            () -> new GasStationTopBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    /**
     * Helper method to register a block with an item.
     */
    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        BLOCK_ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    /**
     * Helper method to register a block without an item (for multi-part blocks like the top).
     */
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }
}
