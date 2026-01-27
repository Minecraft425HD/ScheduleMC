package de.rolandsw.schedulemc.wine.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Wine Menu Types
 */
public class WineMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    // Crushing Station
    public static final RegistryObject<MenuType<CrushingStationMenu>> CRUSHING_STATION_MENU =
        MENUS.register("crushing_station_menu", () ->
            IForgeMenuType.create(CrushingStationMenu::new));

    // Wine Presses
    public static final RegistryObject<MenuType<SmallWinePressMenu>> SMALL_WINE_PRESS_MENU =
        MENUS.register("small_wine_press_menu", () ->
            IForgeMenuType.create(SmallWinePressMenu::new));

    public static final RegistryObject<MenuType<MediumWinePressMenu>> MEDIUM_WINE_PRESS_MENU =
        MENUS.register("medium_wine_press_menu", () ->
            IForgeMenuType.create(MediumWinePressMenu::new));

    public static final RegistryObject<MenuType<LargeWinePressMenu>> LARGE_WINE_PRESS_MENU =
        MENUS.register("large_wine_press_menu", () ->
            IForgeMenuType.create(LargeWinePressMenu::new));

    // Fermentation Tanks
    public static final RegistryObject<MenuType<SmallFermentationTankMenu>> SMALL_FERMENTATION_TANK_MENU =
        MENUS.register("small_fermentation_tank_menu", () ->
            IForgeMenuType.create(SmallFermentationTankMenu::new));

    public static final RegistryObject<MenuType<MediumFermentationTankMenu>> MEDIUM_FERMENTATION_TANK_MENU =
        MENUS.register("medium_fermentation_tank_menu", () ->
            IForgeMenuType.create(MediumFermentationTankMenu::new));

    public static final RegistryObject<MenuType<LargeFermentationTankMenu>> LARGE_FERMENTATION_TANK_MENU =
        MENUS.register("large_fermentation_tank_menu", () ->
            IForgeMenuType.create(LargeFermentationTankMenu::new));

    // Aging Barrels
    public static final RegistryObject<MenuType<SmallAgingBarrelMenu>> SMALL_AGING_BARREL_MENU =
        MENUS.register("small_aging_barrel_menu", () ->
            IForgeMenuType.create(SmallAgingBarrelMenu::new));

    public static final RegistryObject<MenuType<MediumAgingBarrelMenu>> MEDIUM_AGING_BARREL_MENU =
        MENUS.register("medium_aging_barrel_menu", () ->
            IForgeMenuType.create(MediumAgingBarrelMenu::new));

    public static final RegistryObject<MenuType<LargeAgingBarrelMenu>> LARGE_AGING_BARREL_MENU =
        MENUS.register("large_aging_barrel_menu", () ->
            IForgeMenuType.create(LargeAgingBarrelMenu::new));

    // Bottling Station
    public static final RegistryObject<MenuType<WineBottlingStationMenu>> WINE_BOTTLING_STATION_MENU =
        MENUS.register("wine_bottling_station_menu", () ->
            IForgeMenuType.create(WineBottlingStationMenu::new));
}
