package de.rolandsw.schedulemc.beer.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Beer Menu Types
 */
public class BeerMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    // Malting Station
    public static final RegistryObject<MenuType<MaltingStationMenu>> MALTING_STATION_MENU =
        MENUS.register("malting_station_menu", () ->
            IForgeMenuType.create(MaltingStationMenu::new));

    // Mash Tun
    public static final RegistryObject<MenuType<MashTunMenu>> MASH_TUN_MENU =
        MENUS.register("mash_tun_menu", () ->
            IForgeMenuType.create(MashTunMenu::new));

    // Brew Kettles
    public static final RegistryObject<MenuType<SmallBrewKettleMenu>> SMALL_BREW_KETTLE_MENU =
        MENUS.register("small_brew_kettle_menu", () ->
            IForgeMenuType.create(SmallBrewKettleMenu::new));

    public static final RegistryObject<MenuType<MediumBrewKettleMenu>> MEDIUM_BREW_KETTLE_MENU =
        MENUS.register("medium_brew_kettle_menu", () ->
            IForgeMenuType.create(MediumBrewKettleMenu::new));

    public static final RegistryObject<MenuType<LargeBrewKettleMenu>> LARGE_BREW_KETTLE_MENU =
        MENUS.register("large_brew_kettle_menu", () ->
            IForgeMenuType.create(LargeBrewKettleMenu::new));

    // Beer Fermentation Tanks
    public static final RegistryObject<MenuType<SmallBeerFermentationTankMenu>> SMALL_BEER_FERMENTATION_TANK_MENU =
        MENUS.register("small_beer_fermentation_tank_menu", () ->
            IForgeMenuType.create(SmallBeerFermentationTankMenu::new));

    public static final RegistryObject<MenuType<MediumBeerFermentationTankMenu>> MEDIUM_BEER_FERMENTATION_TANK_MENU =
        MENUS.register("medium_beer_fermentation_tank_menu", () ->
            IForgeMenuType.create(MediumBeerFermentationTankMenu::new));

    public static final RegistryObject<MenuType<LargeBeerFermentationTankMenu>> LARGE_BEER_FERMENTATION_TANK_MENU =
        MENUS.register("large_beer_fermentation_tank_menu", () ->
            IForgeMenuType.create(LargeBeerFermentationTankMenu::new));

    // Conditioning Tanks
    public static final RegistryObject<MenuType<SmallConditioningTankMenu>> SMALL_CONDITIONING_TANK_MENU =
        MENUS.register("small_conditioning_tank_menu", () ->
            IForgeMenuType.create(SmallConditioningTankMenu::new));

    public static final RegistryObject<MenuType<MediumConditioningTankMenu>> MEDIUM_CONDITIONING_TANK_MENU =
        MENUS.register("medium_conditioning_tank_menu", () ->
            IForgeMenuType.create(MediumConditioningTankMenu::new));

    public static final RegistryObject<MenuType<LargeConditioningTankMenu>> LARGE_CONDITIONING_TANK_MENU =
        MENUS.register("large_conditioning_tank_menu", () ->
            IForgeMenuType.create(LargeConditioningTankMenu::new));

    // Bottling Station
    public static final RegistryObject<MenuType<BottlingStationMenu>> BOTTLING_STATION_MENU =
        MENUS.register("bottling_station_menu", () ->
            IForgeMenuType.create(BottlingStationMenu::new));
}
