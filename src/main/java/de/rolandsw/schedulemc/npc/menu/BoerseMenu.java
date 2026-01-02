package de.rolandsw.schedulemc.npc.menu;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu für Börsen-NPC Interaktion
 * Ermöglicht Aktienhandel (Gold, Diamanten, Smaragde)
 */
public class BoerseMenu extends AbstractContainerMenu {

    private final CustomNPCEntity npc;
    private final int entityId;

    // Server-side constructor
    public BoerseMenu(int containerId, Inventory playerInventory, CustomNPCEntity npc) {
        super(NPCMenuTypes.BOERSE_MENU.get(), containerId);
        this.npc = npc;
        this.entityId = npc.getId();
    }

    // Client-side constructor
    public BoerseMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(NPCMenuTypes.BOERSE_MENU.get(), containerId);
        this.entityId = extraData.readInt();

        // Client-side: Entity muss aus der Welt geholt werden
        if (playerInventory.player.level().getEntity(entityId) instanceof CustomNPCEntity entity) {
            this.npc = entity;
        } else {
            this.npc = null;
        }
    }

    public CustomNPCEntity getNpc() {
        return npc;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // Keine Standard-Inventar-Interaktionen
    }

    @Override
    public boolean stillValid(Player player) {
        if (npc == null || !npc.isAlive()) {
            return false;
        }

        // Prüfe Distanz zum NPC (max 8 Blöcke)
        return player.distanceToSqr(npc) <= 64.0D;
    }
}
