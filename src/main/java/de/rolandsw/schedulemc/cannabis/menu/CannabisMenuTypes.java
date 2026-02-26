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

    public static final RegistryObject<MenuType<CuringGlasMenu>> CURING_GLAS_MENU =
            MENUS.register("curing_glas_menu", () ->
                    IForgeMenuType.create(CuringGlasMenu::new));

    public static final RegistryObject<MenuType<HashPresseMenu>> HASH_PRESSE_MENU =
            MENUS.register("hash_presse_menu", () ->
                    IForgeMenuType.create(HashPresseMenu::new));

    public static final RegistryObject<MenuType<OelExtraktortMenu>> OEL_EXTRAKTOR_MENU =
            MENUS.register("oel_extraktor_menu", () ->
                    IForgeMenuType.create(OelExtraktortMenu::new));
}
