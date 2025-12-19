package de.rolandsw.schedulemc.items;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller ScheduleMC Items (ERWEITERT)
 */
public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);
    
    // Plot-Auswahl-Werkzeug (wie WorldEdit Axe)
    public static final RegistryObject<Item> PLOT_SELECTION_TOOL = 
        ITEMS.register("plot_selection_tool", PlotSelectionTool::new);
    
    // ═══════════════════════════════════════════════════════════
    // NEUE FEATURES: BARGELD-ITEM
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> CASH =
        ITEMS.register("cash", () -> new de.rolandsw.schedulemc.economy.items.CashItem());

    // ═══════════════════════════════════════════════════════════
    // UNIVERSELLES PACKAGING-SYSTEM
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> PACKAGED_DRUG =
        ITEMS.register("packaged_drug", PackagedDrugItem::new);
}
