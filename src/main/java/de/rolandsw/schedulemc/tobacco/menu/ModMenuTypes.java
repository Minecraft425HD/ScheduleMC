package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Menu Types
 */
public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<MenuType<PackagingTableMenu>> PACKAGING_TABLE_MENU =
        MENUS.register("packaging_table_menu", () ->
            IForgeMenuType.create(PackagingTableMenu::new));

    public static final RegistryObject<MenuType<TobaccoNegotiationMenu>> TOBACCO_NEGOTIATION_MENU =
        MENUS.register("tobacco_negotiation_menu", () ->
            IForgeMenuType.create(TobaccoNegotiationMenu::new));

    public static final RegistryObject<MenuType<SmallPackagingTableMenu>> SMALL_PACKAGING_TABLE_MENU =
        MENUS.register("small_packaging_table_menu", () ->
            IForgeMenuType.create(SmallPackagingTableMenu::new));

    public static final RegistryObject<MenuType<MediumPackagingTableMenu>> MEDIUM_PACKAGING_TABLE_MENU =
        MENUS.register("medium_packaging_table_menu", () ->
            IForgeMenuType.create(MediumPackagingTableMenu::new));

    public static final RegistryObject<MenuType<LargePackagingTableMenu>> LARGE_PACKAGING_TABLE_MENU =
        MENUS.register("large_packaging_table_menu", () ->
            IForgeMenuType.create(LargePackagingTableMenu::new));
}
