package de.rolandsw.schedulemc.vehicle;

import de.rolandsw.schedulemc.vehicle.blocks.ModBlocks;
import de.rolandsw.schedulemc.vehicle.items.ItemBioDieselCanister;
import de.rolandsw.schedulemc.vehicle.items.ItemVehiclePart;
import de.rolandsw.schedulemc.vehicle.items.VehicleItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class VehicleCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MODID);

    public static final RegistryObject<CreativeModeTab> TAB_VEHICLE = TAB_REGISTER.register("vehicle", () -> {
        return CreativeModeTab.builder()
                .icon(() -> new ItemStack(ModBlocks.FUEL_STATION.get()))
                .displayItems((param, output) -> {
                    output.accept(new ItemStack(ModBlocks.FUEL_STATION.get()));
                    output.accept(new ItemStack(ModBlocks.GARAGE.get()));
                    output.accept(new ItemStack(VehicleItems.BIO_DIESEL_BUCKET.get()));

                    // Pre-built vehicles
                    output.accept(new ItemStack(VehicleItems.SPAWN_VEHICLE_OAK.get()));
                    output.accept(new ItemStack(VehicleItems.SPAWN_VEHICLE_BIG_OAK.get()));
                    output.accept(new ItemStack(VehicleItems.SPAWN_VEHICLE_WHITE_TRANSPORTER.get()));
                    output.accept(new ItemStack(VehicleItems.SPAWN_VEHICLE_WHITE_SUV.get()));
                    output.accept(new ItemStack(VehicleItems.SPAWN_VEHICLE_WHITE_SPORT.get()));

                    output.accept(new ItemStack(VehicleItems.CANISTER.get()));
                    output.accept(ItemBioDieselCanister.createPreFilledStack());
                    output.accept(new ItemStack(VehicleItems.REPAIR_KIT.get()));
                    output.accept(new ItemStack(VehicleItems.KEY.get()));
                    output.accept(new ItemStack(VehicleItems.BATTERY.get()));
                    output.accept(new ItemStack(VehicleItems.LICENSE_PLATE.get()));
                    output.accept(new ItemStack(VehicleItems.VEHICLE_SPAWN_TOOL.get()));
                })
                .title(Component.translatable("itemGroup.vehicle"))
                .build();
    });

    public static void init() {
        TAB_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
