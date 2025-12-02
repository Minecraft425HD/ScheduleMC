package de.rolandsw.schedulemc.warehouse.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry für alle Warehouse Menu Types
 */
public class WarehouseMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    // Warehouse Menu - Tab-basiertes Management Panel
    public static final RegistryObject<MenuType<WarehouseMenu>> WAREHOUSE_MENU =
        MENUS.register("warehouse_menu", () -> IForgeMenuType.create(WarehouseMenu::new));
}
