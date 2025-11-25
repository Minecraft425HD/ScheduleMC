package de.rolandsw.schedulemc.npc.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu für NPC Spawner Tool
 * - Erlaubt Auswahl von NPC-Typ und Konfiguration
 */
public class NPCSpawnerMenu extends AbstractContainerMenu {

    private final BlockPos spawnPosition;

    // Server-Side Constructor
    public NPCSpawnerMenu(int id, Inventory playerInventory, BlockPos spawnPos) {
        super(NPCMenuTypes.NPC_SPAWNER_MENU.get(), id);
        this.spawnPosition = spawnPos;

        // Kein Player-Inventar in dieser GUI - nur NPC-Konfiguration
    }

    // Client-Side Constructor
    public NPCSpawnerMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, extraData.readBlockPos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public BlockPos getSpawnPosition() {
        return spawnPosition;
    }
}
