package de.rolandsw.schedulemc.car.fluids;

import de.rolandsw.schedulemc.car.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {

    private static final DeferredRegister<Fluid> FLUID_REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, Main.MODID);

    public static final RegistryObject<FluidBioDiesel> BIO_DIESEL = FLUID_REGISTER.register("bio_diesel", () -> new FluidBioDiesel());
    public static final RegistryObject<FluidBioDieselFlowing> BIO_DIESEL_FLOWING = FLUID_REGISTER.register("bio_diesel_flowing", () -> new FluidBioDieselFlowing());

    private static final DeferredRegister<FluidType> FLUID_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Main.MODID);

    public static final RegistryObject<FluidType> BIO_DIESEL_TYPE = FLUID_TYPE_REGISTER.register("bio_diesel", () ->
            new FluidTypeCar("block.car.bio_diesel", new ResourceLocation(Main.MODID, "block/bio_diesel_still"), new ResourceLocation(Main.MODID, "block/bio_diesel_flowing"))
    );

    public static void init() {
        FLUID_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        FLUID_TYPE_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
