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
}
