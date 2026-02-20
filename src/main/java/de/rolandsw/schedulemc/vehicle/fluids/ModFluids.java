package de.rolandsw.schedulemc.vehicle.fluids;

import de.rolandsw.schedulemc.vehicle.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class ModFluids {

    private static final DeferredRegister<Fluid> FLUID_REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, Main.MODID);
    private static final DeferredRegister<FluidType> FLUID_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Main.MODID);

    public static final RegistryObject<FluidType> DIESEL_TYPE = FLUID_TYPE_REGISTER.register("diesel", () ->
            new FluidType(FluidType.Properties.create()
                    .canConvertToSource(false)
                    .canDrown(true)
                    .canExtinguish(false)
                    .canHydrate(false)
                    .canPushEntity(true)
                    .canSwim(true)
                    .lightLevel(0)
                    .supportsBoating(false)
                    .fallDistanceModifier(0F)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                    .descriptionId("fluid.schedulemc.diesel")) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private static final ResourceLocation UNDERWATER_LOCATION = ResourceLocation.parse("textures/misc/underwater.png");
                        private static final ResourceLocation WATER_OVERLAY = ResourceLocation.parse("block/water_overlay");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/diesel_still");
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/diesel_flowing");
                        }

                        @Override
                        public ResourceLocation getOverlayTexture() {
                            return WATER_OVERLAY;
                        }

                        @Override
                        public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                            return UNDERWATER_LOCATION;
                        }
                    });
                }
            }
    );

    public static final RegistryObject<ForgeFlowingFluid.Source> DIESEL = FLUID_REGISTER.register("diesel",
            () -> new ForgeFlowingFluid.Source(new ForgeFlowingFluid.Properties(
                    () -> DIESEL_TYPE.get(),
                    () -> DIESEL.get(),
                    () -> DIESEL_FLOWING.get()))
    );

    public static final RegistryObject<ForgeFlowingFluid.Flowing> DIESEL_FLOWING = FLUID_REGISTER.register("diesel_flowing",
            () -> new ForgeFlowingFluid.Flowing(new ForgeFlowingFluid.Properties(
                    () -> DIESEL_TYPE.get(),
                    () -> DIESEL.get(),
                    () -> DIESEL_FLOWING.get()))
    );

    public static void init(IEventBus modEventBus) {
        FLUID_REGISTER.register(modEventBus);
        FLUID_TYPE_REGISTER.register(modEventBus);
    }

}
