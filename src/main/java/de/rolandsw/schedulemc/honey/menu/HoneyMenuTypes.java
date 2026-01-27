package de.rolandsw.schedulemc.honey.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Honey Menu Types
 */
public class HoneyMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    // Beehives
    public static final RegistryObject<MenuType<BeehiveMenu>> BEEHIVE_MENU =
        MENUS.register("beehive_menu", () ->
            IForgeMenuType.create(BeehiveMenu::new));

    public static final RegistryObject<MenuType<AdvancedBeehiveMenu>> ADVANCED_BEEHIVE_MENU =
        MENUS.register("advanced_beehive_menu", () ->
            IForgeMenuType.create(AdvancedBeehiveMenu::new));

    public static final RegistryObject<MenuType<ApiaryMenu>> APIARY_MENU =
        MENUS.register("apiary_menu", () ->
            IForgeMenuType.create(ApiaryMenu::new));

    // Extractors
    public static final RegistryObject<MenuType<HoneyExtractorMenu>> HONEY_EXTRACTOR_MENU =
        MENUS.register("honey_extractor_menu", () ->
            IForgeMenuType.create(HoneyExtractorMenu::new));

    public static final RegistryObject<MenuType<CentrifugalExtractorMenu>> CENTRIFUGAL_EXTRACTOR_MENU =
        MENUS.register("centrifugal_extractor_menu", () ->
            IForgeMenuType.create(CentrifugalExtractorMenu::new));

    // Filtering
    public static final RegistryObject<MenuType<FilteringStationMenu>> FILTERING_STATION_MENU =
        MENUS.register("filtering_station_menu", () ->
            IForgeMenuType.create(FilteringStationMenu::new));

    // Aging Chambers
    public static final RegistryObject<MenuType<SmallAgingChamberMenu>> SMALL_AGING_CHAMBER_MENU =
        MENUS.register("small_aging_chamber_menu", () ->
            IForgeMenuType.create(SmallAgingChamberMenu::new));

    public static final RegistryObject<MenuType<MediumAgingChamberMenu>> MEDIUM_AGING_CHAMBER_MENU =
        MENUS.register("medium_aging_chamber_menu", () ->
            IForgeMenuType.create(MediumAgingChamberMenu::new));

    public static final RegistryObject<MenuType<LargeAgingChamberMenu>> LARGE_AGING_CHAMBER_MENU =
        MENUS.register("large_aging_chamber_menu", () ->
            IForgeMenuType.create(LargeAgingChamberMenu::new));

    // Processing
    public static final RegistryObject<MenuType<ProcessingStationMenu>> PROCESSING_STATION_MENU =
        MENUS.register("processing_station_menu", () ->
            IForgeMenuType.create(ProcessingStationMenu::new));

    public static final RegistryObject<MenuType<CreamingStationMenu>> CREAMING_STATION_MENU =
        MENUS.register("creaming_station_menu", () ->
            IForgeMenuType.create(CreamingStationMenu::new));

    // Bottling
    public static final RegistryObject<MenuType<BottlingStationMenu>> BOTTLING_STATION_MENU =
        MENUS.register("bottling_station_menu", () ->
            IForgeMenuType.create(BottlingStationMenu::new));
}
