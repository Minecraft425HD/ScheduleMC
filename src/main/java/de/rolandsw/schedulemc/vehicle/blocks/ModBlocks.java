package de.rolandsw.schedulemc.vehicle.blocks;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.fluid.VehicleFluidBlock;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityGarage;
import de.rolandsw.schedulemc.vehicle.fluids.ModFluids;
import de.maxhenkel.corelib.block.IItemBlock;
import de.maxhenkel.corelib.reflection.ReflectionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
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
    public static final RegistryObject<BlockGarage> GARAGE = BLOCK_REGISTER.register("garage", () -> new BlockGarage());
    public static final RegistryObject<LiquidBlock> BIO_DIESEL = BLOCK_REGISTER.register("diesel", () -> new VehicleFluidBlock(() -> ModFluids.BIO_DIESEL.get()));

    public static final RegistryObject<BlockEntityType<TileEntityGarage>> GARAGE_TILE_ENTITY_TYPE = BLOCK_ENTITY_REGISTER.register("garage", () ->
            BlockEntityType.Builder.of(TileEntityGarage::new, GARAGE.get()).build(null)
    );

    public static void init(IEventBus modEventBus) {
        BLOCK_REGISTER.register(modEventBus);
        BLOCK_ENTITY_REGISTER.register(modEventBus);
    }

}
