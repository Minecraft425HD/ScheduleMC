package de.rolandsw.schedulemc.npc.menu;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
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

        // Player Inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
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
