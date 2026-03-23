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

    // Für Client: Speichere Item-Informationen
    private final int[] itemPrices = new int[SHOP_SLOTS];
    private final boolean[] itemUnlimited = new boolean[SHOP_SLOTS];
    private final int[] itemStock = new int[SHOP_SLOTS];

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

        // Player Hotbar (Schnellauswahleiste)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 186));
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
            boolean unlimited = extraData.readBoolean();
            int stock = extraData.readInt();
            shopContainer.setItem(i, item);
            itemPrices[i] = price;
            itemUnlimited[i] = unlimited;
            itemStock[i] = stock;
        }

        // Shop-Item-Slots (4x4 Grid)
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 4; ++col) {
                this.addSlot(new Slot(shopContainer, col + row * 4, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player Hotbar (Schnellauswahleiste)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 186));
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
                itemUnlimited[i] = entry.isUnlimited();
                itemStock[i] = entry.getStock();
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            // Wenn aus Hotbar geklickt wird (Slots 16-24)
            if (index >= SHOP_SLOTS && index < SHOP_SLOTS + 9) {
                // Versuche in Shop-Slots zu verschieben
                if (!this.moveItemStackTo(slotStack, 0, SHOP_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Wenn aus Shop geklickt wird (Slots 0-15)
            else if (index < SHOP_SLOTS) {
                // Versuche in Hotbar zu verschieben
                if (!this.moveItemStackTo(slotStack, SHOP_SLOTS, SHOP_SLOTS + 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return resultStack;
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

    public boolean[] getItemUnlimited() {
        return itemUnlimited;
    }

    public void setItemUnlimited(int slot, boolean unlimited) {
        if (slot >= 0 && slot < SHOP_SLOTS) {
            itemUnlimited[slot] = unlimited;
        }
    }

    public int[] getItemStock() {
        return itemStock;
    }

    public void setItemStock(int slot, int stock) {
        if (slot >= 0 && slot < SHOP_SLOTS) {
            itemStock[slot] = stock;
        }
    }
}
