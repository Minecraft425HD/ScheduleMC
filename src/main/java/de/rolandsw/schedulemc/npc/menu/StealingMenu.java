package de.rolandsw.schedulemc.npc.menu;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu für Bestehlen von NPCs
 * - Spacebar-Klick Minigame
 * - Items/Geld stehlen
 */
public class StealingMenu extends AbstractContainerMenu {

    private final CustomNPCEntity npc;
    private final int entityId;
    private final int npcWalletAmount;

    // Server-Side Constructor
    public StealingMenu(int id, Inventory playerInventory, CustomNPCEntity npc) {
        super(NPCMenuTypes.STEALING_MENU.get(), id);
        this.npc = npc;
        this.entityId = npc.getId();
        this.npcWalletAmount = npc.getNpcData() != null ? npc.getNpcData().getWallet() : 0;

        // Kein Player-Inventar in dieser GUI - nur Minigame
    }

    // Client-Side Constructor
    public StealingMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(NPCMenuTypes.STEALING_MENU.get(), id);
        int npcId = extraData.readInt();
        this.npcWalletAmount = extraData.readInt(); // Lese Wallet vom Server
        this.npc = (CustomNPCEntity) playerInventory.player.level().getEntity(npcId);
        this.entityId = npcId;
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

    public int getNpcWalletAmount() {
        return npcWalletAmount;
    }
}
