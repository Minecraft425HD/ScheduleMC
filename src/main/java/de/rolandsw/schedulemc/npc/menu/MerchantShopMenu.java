package de.rolandsw.schedulemc.npc.menu;

import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Menu für Verkäufer Shop GUI
 */
public class MerchantShopMenu extends AbstractContainerMenu {

    private final CustomNPCEntity merchant;
    private final int entityId;
    private final MerchantCategory category;

    // Server-Side Constructor
    public MerchantShopMenu(int id, Inventory playerInventory, CustomNPCEntity merchant) {
        super(NPCMenuTypes.MERCHANT_SHOP_MENU.get(), id);
        this.merchant = merchant;
        this.entityId = merchant.getId();
        this.category = merchant.getMerchantCategory();

        // Kein Player-Inventar in dieser GUI - nur Shop-Anzeige
    }

    // Client-Side Constructor
    public MerchantShopMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, (CustomNPCEntity) playerInventory.player.level().getEntity(extraData.readInt()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return merchant != null && merchant.isAlive() && merchant.distanceTo(player) < 8.0;
    }

    public CustomNPCEntity getMerchant() {
        return merchant;
    }

    public int getEntityId() {
        return entityId;
    }

    public MerchantCategory getCategory() {
        return category;
    }

    /**
     * Gibt die verfügbaren Shop-Items für diese Kategorie zurück
     */
    public List<NPCData.ShopEntry> getShopItems() {
        if (merchant != null) {
            return merchant.getNpcData().getBuyShop().getEntries();
        }
        return List.of();
    }
}
