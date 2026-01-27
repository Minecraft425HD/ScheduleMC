package de.rolandsw.schedulemc.chocolate.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Chocolate Menu Types
 */
public class ChocolateMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    // Basic Processing
    public static final RegistryObject<MenuType<RoastingStationMenu>> ROASTING_STATION_MENU =
        MENUS.register("roasting_station_menu", () ->
            IForgeMenuType.create(RoastingStationMenu::new));

    public static final RegistryObject<MenuType<WinnowingMachineMenu>> WINNOWING_MACHINE_MENU =
        MENUS.register("winnowing_machine_menu", () ->
            IForgeMenuType.create(WinnowingMachineMenu::new));

    public static final RegistryObject<MenuType<GrindingMillMenu>> GRINDING_MILL_MENU =
        MENUS.register("grinding_mill_menu", () ->
            IForgeMenuType.create(GrindingMillMenu::new));

    public static final RegistryObject<MenuType<PressingStationMenu>> PRESSING_STATION_MENU =
        MENUS.register("pressing_station_menu", () ->
            IForgeMenuType.create(PressingStationMenu::new));

    // Conching Machines
    public static final RegistryObject<MenuType<SmallConchingMachineMenu>> SMALL_CONCHING_MACHINE_MENU =
        MENUS.register("small_conching_machine_menu", () ->
            IForgeMenuType.create(SmallConchingMachineMenu::new));

    public static final RegistryObject<MenuType<MediumConchingMachineMenu>> MEDIUM_CONCHING_MACHINE_MENU =
        MENUS.register("medium_conching_machine_menu", () ->
            IForgeMenuType.create(MediumConchingMachineMenu::new));

    public static final RegistryObject<MenuType<LargeConchingMachineMenu>> LARGE_CONCHING_MACHINE_MENU =
        MENUS.register("large_conching_machine_menu", () ->
            IForgeMenuType.create(LargeConchingMachineMenu::new));

    // Tempering
    public static final RegistryObject<MenuType<TemperingStationMenu>> TEMPERING_STATION_MENU =
        MENUS.register("tempering_station_menu", () ->
            IForgeMenuType.create(TemperingStationMenu::new));

    // Molding Stations
    public static final RegistryObject<MenuType<SmallMoldingStationMenu>> SMALL_MOLDING_STATION_MENU =
        MENUS.register("small_molding_station_menu", () ->
            IForgeMenuType.create(SmallMoldingStationMenu::new));

    public static final RegistryObject<MenuType<MediumMoldingStationMenu>> MEDIUM_MOLDING_STATION_MENU =
        MENUS.register("medium_molding_station_menu", () ->
            IForgeMenuType.create(MediumMoldingStationMenu::new));

    public static final RegistryObject<MenuType<LargeMoldingStationMenu>> LARGE_MOLDING_STATION_MENU =
        MENUS.register("large_molding_station_menu", () ->
            IForgeMenuType.create(LargeMoldingStationMenu::new));

    // Enrobing & Finishing
    public static final RegistryObject<MenuType<EnrobingMachineMenu>> ENROBING_MACHINE_MENU =
        MENUS.register("enrobing_machine_menu", () ->
            IForgeMenuType.create(EnrobingMachineMenu::new));

    public static final RegistryObject<MenuType<CoolingTunnelMenu>> COOLING_TUNNEL_MENU =
        MENUS.register("cooling_tunnel_menu", () ->
            IForgeMenuType.create(CoolingTunnelMenu::new));

    public static final RegistryObject<MenuType<WrappingStationMenu>> WRAPPING_STATION_MENU =
        MENUS.register("wrapping_station_menu", () ->
            IForgeMenuType.create(WrappingStationMenu::new));
}
