package de.rolandsw.schedulemc.meth.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Meth-Menu Types
 */
public class MethMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<MenuType<ReduktionskesselMenu>> REDUKTIONSKESSEL_MENU =
            MENUS.register("reduktionskessel_menu", () ->
                    IForgeMenuType.create(ReduktionskesselMenu::new));
}
