package de.rolandsw.schedulemc.vehicle.blocks;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.fluid.VehicleFluidBlock;
import de.rolandsw.schedulemc.vehicle.fluids.ModFluids;
import de.maxhenkel.corelib.block.IItemBlock;
import de.maxhenkel.corelib.reflection.ReflectionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);

    public static final RegistryObject<BlockGasStation> GAS_STATION = BLOCK_REGISTER.register("fuel_station", () -> new BlockGasStation());
    public static final RegistryObject<BlockGasStationTop> GAS_STATION_TOP = BLOCK_REGISTER.register("fuel_station_top", () -> new BlockGasStationTop());
    public static final RegistryObject<LiquidBlock> BIO_DIESEL = BLOCK_REGISTER.register("diesel", () -> new VehicleFluidBlock(() -> ModFluids.BIO_DIESEL.get()));

    public static void init() {
        BLOCK_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
