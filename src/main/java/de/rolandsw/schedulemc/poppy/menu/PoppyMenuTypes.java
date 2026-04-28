package de.rolandsw.schedulemc.poppy.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PoppyMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<MenuType<ScoringMachineMenu>> SCORING_MACHINE_MENU =
            MENUS.register("scoring_machine_menu", () -> IForgeMenuType.create(ScoringMachineMenu::new));

    public static final RegistryObject<MenuType<OpiumPressMenu>> OPIUM_PRESS_MENU =
            MENUS.register("opium_press_menu", () -> IForgeMenuType.create(OpiumPressMenu::new));

    public static final RegistryObject<MenuType<CookingStationMenu>> COOKING_STATION_MENU =
            MENUS.register("cooking_station_menu", () -> IForgeMenuType.create(CookingStationMenu::new));

    public static final RegistryObject<MenuType<HeroinRefineryMenu>> HEROIN_REFINERY_MENU =
            MENUS.register("heroin_refinery_menu", () -> IForgeMenuType.create(HeroinRefineryMenu::new));
}
