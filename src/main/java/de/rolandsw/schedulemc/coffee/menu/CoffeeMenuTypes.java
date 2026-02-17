package de.rolandsw.schedulemc.coffee.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Coffee Menu Types
 */
public class CoffeeMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    // Drying Trays - ENTFERNT (verwende TobaccoMenuTypes stattdessen)

    // Wet Processing Station
    public static final RegistryObject<MenuType<WetProcessingStationMenu>> WET_PROCESSING_STATION_MENU =
        MENUS.register("wet_processing_station_menu", () ->
            IForgeMenuType.create(WetProcessingStationMenu::new));

    // Coffee Roasters
    public static final RegistryObject<MenuType<SmallCoffeeRoasterMenu>> SMALL_COFFEE_ROASTER_MENU =
        MENUS.register("small_coffee_roaster_menu", () ->
            IForgeMenuType.create(SmallCoffeeRoasterMenu::new));

    public static final RegistryObject<MenuType<MediumCoffeeRoasterMenu>> MEDIUM_COFFEE_ROASTER_MENU =
        MENUS.register("medium_coffee_roaster_menu", () ->
            IForgeMenuType.create(MediumCoffeeRoasterMenu::new));

    public static final RegistryObject<MenuType<LargeCoffeeRoasterMenu>> LARGE_COFFEE_ROASTER_MENU =
        MENUS.register("large_coffee_roaster_menu", () ->
            IForgeMenuType.create(LargeCoffeeRoasterMenu::new));

    // Coffee Grinder
    public static final RegistryObject<MenuType<CoffeeGrinderMenu>> COFFEE_GRINDER_MENU =
        MENUS.register("coffee_grinder_menu", () ->
            IForgeMenuType.create(CoffeeGrinderMenu::new));

    // Coffee Packaging Table
    public static final RegistryObject<MenuType<CoffeePackagingTableMenu>> COFFEE_PACKAGING_TABLE_MENU =
        MENUS.register("coffee_packaging_table_menu", () ->
            IForgeMenuType.create(CoffeePackagingTableMenu::new));
}
