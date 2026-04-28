package de.rolandsw.schedulemc.coca.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CocaMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<MenuType<ExtractionVatMenu>> EXTRACTION_VAT_MENU =
            MENUS.register("extraction_vat_menu", () -> IForgeMenuType.create(ExtractionVatMenu::new));

    public static final RegistryObject<MenuType<RefineryMenu>> REFINERY_MENU =
            MENUS.register("refinery_menu", () -> IForgeMenuType.create(RefineryMenu::new));

    public static final RegistryObject<MenuType<CrackCookerMenu>> CRACK_COOKER_MENU =
            MENUS.register("crack_cooker_menu", () -> IForgeMenuType.create(CrackCookerMenu::new));
}
