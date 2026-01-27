package de.rolandsw.schedulemc.cheese.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Cheese Menu Types
 */
public class CheeseMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    // Pasteurization Station
    public static final RegistryObject<MenuType<PasteurizationStationMenu>> PASTEURIZATION_STATION_MENU =
        MENUS.register("pasteurization_station_menu", () ->
            IForgeMenuType.create(PasteurizationStationMenu::new));

    // Curdling Vat
    public static final RegistryObject<MenuType<CurdlingVatMenu>> CURDLING_VAT_MENU =
        MENUS.register("curdling_vat_menu", () ->
            IForgeMenuType.create(CurdlingVatMenu::new));

    // Cheese Presses
    public static final RegistryObject<MenuType<SmallCheesePressMenu>> SMALL_CHEESE_PRESS_MENU =
        MENUS.register("small_cheese_press_menu", () ->
            IForgeMenuType.create(SmallCheesePressMenu::new));

    public static final RegistryObject<MenuType<MediumCheesePressMenu>> MEDIUM_CHEESE_PRESS_MENU =
        MENUS.register("medium_cheese_press_menu", () ->
            IForgeMenuType.create(MediumCheesePressMenu::new));

    public static final RegistryObject<MenuType<LargeCheesePressMenu>> LARGE_CHEESE_PRESS_MENU =
        MENUS.register("large_cheese_press_menu", () ->
            IForgeMenuType.create(LargeCheesePressMenu::new));

    // Aging Caves
    public static final RegistryObject<MenuType<SmallAgingCaveMenu>> SMALL_AGING_CAVE_MENU =
        MENUS.register("small_aging_cave_menu", () ->
            IForgeMenuType.create(SmallAgingCaveMenu::new));

    public static final RegistryObject<MenuType<MediumAgingCaveMenu>> MEDIUM_AGING_CAVE_MENU =
        MENUS.register("medium_aging_cave_menu", () ->
            IForgeMenuType.create(MediumAgingCaveMenu::new));

    public static final RegistryObject<MenuType<LargeAgingCaveMenu>> LARGE_AGING_CAVE_MENU =
        MENUS.register("large_aging_cave_menu", () ->
            IForgeMenuType.create(LargeAgingCaveMenu::new));

    // Packaging Station
    public static final RegistryObject<MenuType<PackagingStationMenu>> PACKAGING_STATION_MENU =
        MENUS.register("packaging_station_menu", () ->
            IForgeMenuType.create(PackagingStationMenu::new));
}
