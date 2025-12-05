package de.rolandsw.schedulemc.car.blocks;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.blocks.BlockPaint.EnumPaintType;
import de.rolandsw.schedulemc.car.blocks.fluid.CarFluidBlock;
import de.rolandsw.schedulemc.car.fluids.ModFluids;
import de.maxhenkel.corelib.block.IItemBlock;
import de.maxhenkel.corelib.reflection.ReflectionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {

    private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);

    public static final RegistryObject<BlockGasStation> GAS_STATION = BLOCK_REGISTER.register("gas_station", () -> new BlockGasStation());
    public static final RegistryObject<BlockGasStationTop> GAS_STATION_TOP = BLOCK_REGISTER.register("gas_station_top", () -> new BlockGasStationTop());
    public static final RegistryObject<BlockCarWorkshop> CAR_WORKSHOP = BLOCK_REGISTER.register("car_workshop", () -> new BlockCarWorkshop());
    public static final RegistryObject<BlockCarWorkshopOutter> CAR_WORKSHOP_OUTTER = BLOCK_REGISTER.register("car_workshop_outter", () -> new BlockCarWorkshopOutter());
    public static final RegistryObject<BlockCarPressurePlate> CAR_PRESSURE_PLATE = BLOCK_REGISTER.register("car_pressure_plate", () -> new BlockCarPressurePlate());
    public static final RegistryObject<LiquidBlock> BIO_DIESEL = BLOCK_REGISTER.register("bio_diesel", () -> new CarFluidBlock(() -> ModFluids.BIO_DIESEL.get()));

    public static final RegistryObject<BlockPaint>[] PAINTS;
    public static final RegistryObject<BlockPaint>[] YELLOW_PAINTS;

    static {
        PAINTS = new RegistryObject[EnumPaintType.values().length];
        for (int i = 0; i < PAINTS.length; i++) {
            int paintIndex = i;
            PAINTS[i] = BLOCK_REGISTER.register(EnumPaintType.values()[i].getPaintName(), () -> new BlockPaint(EnumPaintType.values()[paintIndex], false));
        }

        YELLOW_PAINTS = new RegistryObject[EnumPaintType.values().length];
        for (int i = 0; i < YELLOW_PAINTS.length; i++) {
            int paintIndex = i;
            YELLOW_PAINTS[i] = BLOCK_REGISTER.register(EnumPaintType.values()[i].getPaintName() + "_yellow", () -> new BlockPaint(EnumPaintType.values()[paintIndex], true));
        }
    }

    public static void init() {
        BLOCK_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
