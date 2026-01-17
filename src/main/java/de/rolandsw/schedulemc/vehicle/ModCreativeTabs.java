package de.rolandsw.schedulemc.vehicle;

import de.rolandsw.schedulemc.vehicle.blocks.ModBlocks;
import de.rolandsw.schedulemc.vehicle.items.ItemBioDieselCanister;
import de.rolandsw.schedulemc.vehicle.items.ItemVehiclePart;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MODID);

    public static final RegistryObject<CreativeModeTab> TAB_VEHICLE = TAB_REGISTER.register("vehicle", () -> {
        return CreativeModeTab.builder()
                .icon(() -> new ItemStack(ModBlocks.FUEL_STATION.get()))
                .displayItems((param, output) -> {
                    output.accept(new ItemStack(ModBlocks.FUEL_STATION.get()));
                    output.accept(new ItemStack(ModBlocks.GARAGE.get()));

                    // Pre-built vehicles
                    output.accept(new ItemStack(ModItems.SPAWN_VEHICLE_OAK.get()));
                    output.accept(new ItemStack(ModItems.SPAWN_VEHICLE_BIG_OAK.get()));
                    output.accept(new ItemStack(ModItems.SPAWN_VEHICLE_WHITE_TRANSPORTER.get()));
                    output.accept(new ItemStack(ModItems.SPAWN_VEHICLE_WHITE_SUV.get()));
                    output.accept(new ItemStack(ModItems.SPAWN_VEHICLE_WHITE_SPORT.get()));

                    output.accept(new ItemStack(ModItems.CANISTER.get()));
                    output.accept(ItemBioDieselCanister.createPreFilledStack());
                    output.accept(new ItemStack(ModItems.REPAIR_KIT.get()));
                    output.accept(new ItemStack(ModItems.KEY.get()));
                    output.accept(new ItemStack(ModItems.BATTERY.get()));
                    output.accept(new ItemStack(ModItems.LICENSE_PLATE.get()));
                    output.accept(new ItemStack(ModItems.VEHICLE_SPAWN_TOOL.get()));
                })
                .title(Component.translatable("itemGroup.schedulemc.vehicle"))
                .build();
    });

    public static void init(IEventBus modEventBus) {
        TAB_REGISTER.register(modEventBus);
    }

}
