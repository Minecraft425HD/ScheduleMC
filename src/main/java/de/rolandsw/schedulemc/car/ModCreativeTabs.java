package de.rolandsw.schedulemc.car;

import de.rolandsw.schedulemc.car.blocks.ModBlocks;
import de.rolandsw.schedulemc.car.items.ItemCarPart;
import de.rolandsw.schedulemc.car.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MODID);

    public static final RegistryObject<CreativeModeTab> TAB_CAR = TAB_REGISTER.register("car", () -> {
        return CreativeModeTab.builder()
                .icon(() -> new ItemStack(ModBlocks.GAS_STATION.get()))
                .displayItems((param, output) -> {
                    output.accept(new ItemStack(ModBlocks.GAS_STATION.get()));
                    output.accept(new ItemStack(ModItems.BIO_DIESEL_BUCKET.get()));

                    // Pre-built cars
                    output.accept(new ItemStack(ModItems.SPAWN_CAR_OAK.get()));
                    output.accept(new ItemStack(ModItems.SPAWN_CAR_BIG_OAK.get()));
                    output.accept(new ItemStack(ModItems.SPAWN_CAR_WHITE_TRANSPORTER.get()));
                    output.accept(new ItemStack(ModItems.SPAWN_CAR_WHITE_SUV.get()));
                    output.accept(new ItemStack(ModItems.SPAWN_CAR_WHITE_SPORT.get()));

                    output.accept(new ItemStack(ModItems.CANISTER.get()));
                    output.accept(new ItemStack(ModItems.REPAIR_KIT.get()));
                    output.accept(new ItemStack(ModItems.WRENCH.get()));
                    output.accept(new ItemStack(ModItems.KEY.get()));
                    output.accept(new ItemStack(ModItems.BATTERY.get()));
                    output.accept(new ItemStack(ModItems.LICENSE_PLATE.get()));
                })
                .title(Component.translatable("itemGroup.car"))
                .build();
    });

    public static final RegistryObject<CreativeModeTab> TAB_CAR_PARTS = TAB_REGISTER.register("car_parts", () -> {
        return CreativeModeTab.builder()
                .icon(() -> new ItemStack(ModItems.OAK_BODY.get()))
                .displayItems((param, output) -> {
                    output.accept(new ItemStack(ModItems.ENGINE_3_CYLINDER.get()));
                    output.accept(new ItemStack(ModItems.ENGINE_6_CYLINDER.get()));
                    output.accept(new ItemStack(ModItems.ENGINE_TRUCK.get()));
                    output.accept(new ItemStack(ModItems.WHEEL.get()));
                    output.accept(new ItemStack(ModItems.BIG_WHEEL.get()));
                    output.accept(new ItemStack(ModItems.SMALL_TANK.get()));
                    output.accept(new ItemStack(ModItems.MEDIUM_TANK.get()));
                    output.accept(new ItemStack(ModItems.LARGE_TANK.get()));

                    for (RegistryObject<ItemCarPart> part : ModItems.WOOD_BODIES) {
                        output.accept(new ItemStack(part.get()));
                    }
                    for (RegistryObject<ItemCarPart> part : ModItems.BIG_WOOD_BODIES) {
                        output.accept(new ItemStack(part.get()));
                    }
                    for (RegistryObject<ItemCarPart> part : ModItems.TRANSPORTER_BODIES) {
                        output.accept(new ItemStack(part.get()));
                    }
                    for (RegistryObject<ItemCarPart> part : ModItems.SUV_BODIES) {
                        output.accept(new ItemStack(part.get()));
                    }
                    for (RegistryObject<ItemCarPart> part : ModItems.SPORT_BODIES) {
                        output.accept(new ItemStack(part.get()));
                    }
                    for (RegistryObject<ItemCarPart> part : ModItems.CONTAINERS) {
                        output.accept(new ItemStack(part.get()));
                    }
                    for (RegistryObject<ItemCarPart> part : ModItems.TANK_CONTAINERS) {
                        output.accept(new ItemStack(part.get()));
                    }
                    for (RegistryObject<ItemCarPart> part : ModItems.BUMPERS) {
                        output.accept(new ItemStack(part.get()));
                    }
                    output.accept(new ItemStack(ModItems.IRON_LICENSE_PLATE_HOLDER.get()));
                })
                .title(Component.translatable("itemGroup.car_parts"))
                .build();
    });

    public static void init() {
        TAB_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
