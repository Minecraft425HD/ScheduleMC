package de.rolandsw.schedulemc.npc.menu;

import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu für Shop-Editor (Admin-GUI)
 * - Erlaubt Admins, Shop-Items zu bearbeiten
 */
public class ShopEditorMenu extends AbstractContainerMenu {

    private final CustomNPCEntity merchant;
    private final int entityId;
    private final MerchantCategory category;
    private final Container shopContainer;
    public static final int SHOP_SLOTS = 16; // Max 16 Items im Shop (4x4)

    // Für Client: Speichere Preis-Informationen
    private final int[] itemPrices = new int[SHOP_SLOTS];

    // Server-Side Constructor
    public ShopEditorMenu(int id, Inventory playerInventory, CustomNPCEntity merchant) {
        super(NPCMenuTypes.SHOP_EDITOR_MENU.get(), id);
        this.merchant = merchant;
        this.entityId = merchant.getId();
        this.category = merchant.getMerchantCategory();
        this.shopContainer = new SimpleContainer(SHOP_SLOTS);

        // Lade aktuelle Shop-Items in den Container
        loadShopItems();

        // Shop-Item-Slots (4x4 Grid)
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 4; ++col) {
                this.addSlot(new Slot(shopContainer, col + row * 4, 8 + col * 18, 18 + row * 18));
            }
        }

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
    public ShopEditorMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(NPCMenuTypes.SHOP_EDITOR_MENU.get(), id);
        int entityId = extraData.readInt();
        this.merchant = (CustomNPCEntity) playerInventory.player.level().getEntity(entityId);
        this.entityId = entityId;
        this.category = merchant != null ? merchant.getMerchantCategory() : MerchantCategory.BAUMARKT;
        this.shopContainer = new SimpleContainer(SHOP_SLOTS);

        // Lese Shop-Items vom Buffer
        int itemCount = extraData.readInt();
        for (int i = 0; i < itemCount && i < SHOP_SLOTS; i++) {
            ItemStack item = extraData.readItem();
            int price = extraData.readInt();
            shopContainer.setItem(i, item);
            itemPrices[i] = price;
        }

        // Shop-Item-Slots (4x4 Grid)
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 4; ++col) {
                this.addSlot(new Slot(shopContainer, col + row * 4, 8 + col * 18, 18 + row * 18));
            }
        }

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

    /**
     * Lädt aktuelle Shop-Items in den Container
     */
    private void loadShopItems() {
        if (merchant != null) {
            List<NPCData.ShopEntry> entries = merchant.getNpcData().getBuyShop().getEntries();
            for (int i = 0; i < Math.min(entries.size(), SHOP_SLOTS); i++) {
                NPCData.ShopEntry entry = entries.get(i);
                shopContainer.setItem(i, entry.getItem().copy());
                itemPrices[i] = entry.getPrice();
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();

            // Shift-Click aus Player-Inventar in Shop-Slots
            if (index >= SHOP_SLOTS) {
                if (!this.moveItemStackTo(slotItem, 0, SHOP_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Shift-Click aus Shop-Slots zurück ins Inventar
            else {
                if (!this.moveItemStackTo(slotItem, SHOP_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotItem.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return merchant != null && merchant.isAlive() && merchant.distanceTo(player) < 8.0
                && player.hasPermissions(2); // Admin-Check
    }

    public Container getShopContainer() {
        return shopContainer;
    }

    public int getEntityId() {
        return entityId;
    }

    public MerchantCategory getCategory() {
        return category;
    }

    public int[] getItemPrices() {
        return itemPrices;
    }

    public void setItemPrice(int slot, int price) {
        if (slot >= 0 && slot < SHOP_SLOTS) {
            itemPrices[slot] = price;
        }
    }
}
