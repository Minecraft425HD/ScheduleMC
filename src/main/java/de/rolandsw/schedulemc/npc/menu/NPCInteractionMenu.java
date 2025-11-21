package de.rolandsw.schedulemc.npc.menu;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu für NPC Interaktion
 * - Dialog, Kaufen, Verkaufen
 */
public class NPCInteractionMenu extends AbstractContainerMenu {

    private final CustomNPCEntity npc;
    private final int entityId;

    // Server-Side Constructor
    public NPCInteractionMenu(int id, Inventory playerInventory, CustomNPCEntity npc) {
        super(NPCMenuTypes.NPC_INTERACTION_MENU.get(), id);
        this.npc = npc;
        this.entityId = npc.getId();

        // Kein Player-Inventar in dieser GUI - nur NPC-Interaktionsoptionen
    }

    // Client-Side Constructor
    public NPCInteractionMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, (CustomNPCEntity) playerInventory.player.level().getEntity(extraData.readInt()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return npc != null && npc.isAlive();
    }

    public CustomNPCEntity getNpc() {
        return npc;
    }

    public int getEntityId() {
        return entityId;
    }
}
