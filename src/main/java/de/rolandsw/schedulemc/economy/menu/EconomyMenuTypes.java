package de.rolandsw.schedulemc.economy.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Economy Menu Types (FIXED)
 */
public class EconomyMenuTypes {
    
    public static final DeferredRegister<MenuType<?>> MENUS = 
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);
    
    // ATM Menu - Verwendet Client-Side Konstruktor!
    public static final RegistryObject<MenuType<ATMMenu>> ATM_MENU = 
        MENUS.register("atm_menu", () -> IForgeMenuType.create(ATMMenu::new));
}
