package de.rolandsw.schedulemc.mdma.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller MDMA-Menu Types
 */
public class MDMAMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<MenuType<PillenPresseMenu>> PILLEN_PRESSE_MENU =
            MENUS.register("pillen_presse_menu", () ->
                    IForgeMenuType.create(PillenPresseMenu::new));
}
