package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Container-Menu für Tabak-Verhandlung mit NPC
 */
public class TobaccoNegotiationMenu extends AbstractContainerMenu {

    private final int npcEntityId;
    private final Inventory playerInventory;

    // Server-side constructor
    public TobaccoNegotiationMenu(int containerId, Inventory playerInventory, int npcEntityId) {
        super(ModMenuTypes.TOBACCO_NEGOTIATION_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.npcEntityId = npcEntityId;
        addPlayerSlots();
    }

    // Client-side constructor
    public TobaccoNegotiationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readInt());
    }

    private void addPlayerSlots() {
        // Player Inventory (3 Reihen)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY; // Kein Shift-Click in diesem GUI
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        Entity entity = player.level().getEntity(npcEntityId);
        return entity instanceof CustomNPCEntity && entity.isAlive() && player.distanceToSqr(entity) <= 64.0;
    }

    public int getNpcEntityId() {
        return npcEntityId;
    }

    public CustomNPCEntity getNpc() {
        if (playerInventory.player.level().getEntity(npcEntityId) instanceof CustomNPCEntity npc) {
            return npc;
        }
        return null;
    }
}
