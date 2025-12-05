package de.rolandsw.schedulemc.npc.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry für NPC Items
 */
public class NPCItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    public static final RegistryObject<Item> NPC_SPAWNER_TOOL = ITEMS.register("npc_spawner_tool",
        NPCSpawnerTool::new);

    public static final RegistryObject<Item> NPC_LOCATION_TOOL = ITEMS.register("npc_location_tool",
        NPCLocationTool::new);

    public static final RegistryObject<Item> NPC_LEISURE_TOOL = ITEMS.register("npc_leisure_tool",
        NPCLeisureTool::new);

    public static final RegistryObject<Item> NPC_PATROL_TOOL = ITEMS.register("npc_patrol_tool",
        NPCPatrolTool::new);
}
