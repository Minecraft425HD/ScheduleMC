package de.rolandsw.schedulemc.vehicle.blocks;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityWerkstatt;
import de.maxhenkel.corelib.block.IItemBlock;
import de.maxhenkel.corelib.reflection.ReflectionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MODID);

    public static final RegistryObject<BlockFuelStation> FUEL_STATION = BLOCK_REGISTER.register("fuel_station", () -> new BlockFuelStation());
    public static final RegistryObject<BlockFuelStationTop> FUEL_STATION_TOP = BLOCK_REGISTER.register("fuel_station_top", () -> new BlockFuelStationTop());
    public static final RegistryObject<BlockWerkstatt> WERKSTATT = BLOCK_REGISTER.register("werkstatt", () -> new BlockWerkstatt());
    public static final RegistryObject<BlockEntityType<TileEntityWerkstatt>> WERKSTATT_TILE_ENTITY_TYPE = BLOCK_ENTITY_REGISTER.register("werkstatt", () ->
            BlockEntityType.Builder.of(TileEntityWerkstatt::new, WERKSTATT.get()).build(null)
    );

    public static void init(IEventBus modEventBus) {
        BLOCK_REGISTER.register(modEventBus);
        BLOCK_ENTITY_REGISTER.register(modEventBus);
    }

}
