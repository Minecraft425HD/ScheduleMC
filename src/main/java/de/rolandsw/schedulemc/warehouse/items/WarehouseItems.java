package de.rolandsw.schedulemc.warehouse.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry für Warehouse Items
 */
public class WarehouseItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    public static final RegistryObject<Item> WAREHOUSE_TOOL = ITEMS.register("warehouse_tool",
        WarehouseTool::new);
}
