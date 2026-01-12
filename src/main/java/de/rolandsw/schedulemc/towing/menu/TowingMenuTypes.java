package de.rolandsw.schedulemc.towing.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.towing.TowingInvoiceData;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for towing menu types
 */
public class TowingMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<MenuType<TowingInvoiceMenu>> TOWING_INVOICE =
        MENUS.register("towing_invoice", () ->
            IForgeMenuType.create((windowId, inv, data) -> {
                TowingInvoiceData invoice = TowingInvoiceData.decode(data);
                return new TowingInvoiceMenu(windowId, inv, invoice);
            })
        );
}
