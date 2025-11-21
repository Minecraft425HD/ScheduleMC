package de.rolandsw.schedulemc.npc.menu;

import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu für Verkäufer Shop GUI
 */
public class MerchantShopMenu extends AbstractContainerMenu {

    private final CustomNPCEntity merchant;
    private final int entityId;
    private final MerchantCategory category;
    private final List<NPCData.ShopEntry> shopItems; // Shop-Items (Client-Side verfügbar)

    // Server-Side Constructor
    public MerchantShopMenu(int id, Inventory playerInventory, CustomNPCEntity merchant) {
        super(NPCMenuTypes.MERCHANT_SHOP_MENU.get(), id);
        this.merchant = merchant;
        this.entityId = merchant.getId();
        this.category = merchant.getMerchantCategory();
        this.shopItems = merchant.getNpcData().getBuyShop().getEntries();

        // Kein Player-Inventar in dieser GUI - nur Shop-Anzeige
    }

    // Client-Side Constructor
    public MerchantShopMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(NPCMenuTypes.MERCHANT_SHOP_MENU.get(), id);
        int entityId = extraData.readInt();
        this.merchant = (CustomNPCEntity) playerInventory.player.level().getEntity(entityId);
        this.entityId = entityId;
        this.category = merchant != null ? merchant.getMerchantCategory() : MerchantCategory.BAUMARKT;

        // Lese Shop-Items vom Buffer (vom Server gesendet)
        this.shopItems = new ArrayList<>();
        int itemCount = extraData.readInt();
        for (int i = 0; i < itemCount; i++) {
            ItemStack item = extraData.readItem();
            int price = extraData.readInt();
            shopItems.add(new NPCData.ShopEntry(item, price));
        }
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
     * Diese Liste ist sowohl auf Server als auch Client verfügbar
     */
    public List<NPCData.ShopEntry> getShopItems() {
        return shopItems;
    }
}
