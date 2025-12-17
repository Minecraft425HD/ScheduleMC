package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Cannabis-Menu Types
 */
public class CannabisMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<MenuType<TrimmStationMenu>> TRIMM_STATION_MENU =
            MENUS.register("trimm_station_menu", () ->
                    IForgeMenuType.create(TrimmStationMenu::new));
}
