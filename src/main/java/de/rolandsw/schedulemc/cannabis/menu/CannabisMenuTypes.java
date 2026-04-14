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

    public static final RegistryObject<MenuType<TrimStationMenu>> TRIM_STATION_MENU =
            MENUS.register("trimm_station_menu", () ->
                    IForgeMenuType.create(TrimStationMenu::new));

    public static final RegistryObject<MenuType<CuringJarMenu>> CURING_JAR_MENU =
            MENUS.register("curing_glas_menu", () ->
                    IForgeMenuType.create(CuringJarMenu::new));

    public static final RegistryObject<MenuType<HashPressMenu>> HASH_PRESS_MENU =
            MENUS.register("hash_presse_menu", () ->
                    IForgeMenuType.create(HashPressMenu::new));

    public static final RegistryObject<MenuType<OilExtractorMenu>> OIL_EXTRACTOR_MENU =
            MENUS.register("oel_extraktor_menu", () ->
                    IForgeMenuType.create(OilExtractorMenu::new));
}
