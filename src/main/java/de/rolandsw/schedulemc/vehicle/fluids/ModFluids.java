package de.rolandsw.schedulemc.vehicle.fluids;

import de.rolandsw.schedulemc.vehicle.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {

    private static final DeferredRegister<Fluid> FLUID_REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, Main.MODID);

    public static final RegistryObject<FluidBioDiesel> BIO_DIESEL = FLUID_REGISTER.register("diesel", () -> new FluidBioDiesel());
    public static final RegistryObject<FluidBioDieselFlowing> BIO_DIESEL_FLOWING = FLUID_REGISTER.register("diesel_flowing", () -> new FluidBioDieselFlowing());

    private static final DeferredRegister<FluidType> FLUID_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Main.MODID);

    public static final RegistryObject<FluidType> BIO_DIESEL_TYPE = FLUID_TYPE_REGISTER.register("diesel", () ->
            new FluidTypeVehicle("block.vehicle.diesel", ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/diesel_still"), ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/diesel_flowing"))
    );

    public static void init(IEventBus modEventBus) {
        FLUID_REGISTER.register(modEventBus);
        FLUID_TYPE_REGISTER.register(modEventBus);
    }

}
