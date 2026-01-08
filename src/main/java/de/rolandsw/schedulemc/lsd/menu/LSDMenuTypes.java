package de.rolandsw.schedulemc.lsd.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller LSD-Menu Types
 */
public class LSDMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<MenuType<MikroDosiererMenu>> MIKRO_DOSIERER_MENU =
            MENUS.register("mikro_dosierer_menu", () ->
                    IForgeMenuType.create(MikroDosiererMenu::new));
}
