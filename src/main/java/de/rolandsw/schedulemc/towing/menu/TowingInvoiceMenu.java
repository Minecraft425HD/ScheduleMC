package de.rolandsw.schedulemc.towing.menu;

import de.rolandsw.schedulemc.towing.TowingInvoiceData;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

/**
 * Container for towing invoice payment screen
 */
public class TowingInvoiceMenu extends AbstractContainerMenu {
    private final TowingInvoiceData invoice;
    private final Container invoiceInventory;

    public TowingInvoiceMenu(int id, Inventory playerInventory, TowingInvoiceData invoice) {
        super(TowingMenuTypes.TOWING_INVOICE.get(), id);
        this.invoice = invoice;
        this.invoiceInventory = createInvoiceInventory(invoice);

        // Invoice slot (read-only, always 1 paper item)
        addSlot(new InvoiceSlot(invoiceInventory, 0, 80, 35));

        // Player inventory
        addPlayerInventorySlots(playerInventory);
    }

    public TowingInvoiceData getInvoice() {
        return invoice;
    }

    private Container createInvoiceInventory(TowingInvoiceData invoice) {
        SimpleContainer container = new SimpleContainer(1);
        ItemStack invoiceItem = new ItemStack(Items.PAPER);
        invoiceItem.setCount(1); // ALWAYS 1

        invoiceItem.setHoverName(Component.translatable("item.towing_invoice"));

        // Add lore with invoice details
        CompoundTag tag = invoiceItem.getOrCreateTag();
        CompoundTag display = tag.getCompound("display");
        ListTag lore = new ListTag();

        lore.add(StringTag.valueOf(Component.Serializer.toJson(
            Component.translatable("item.towing_invoice.amount", String.format("%.0f€", invoice.getAmount()))
        )));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(
            Component.translatable("item.towing_invoice.yard", invoice.getTowingYardPlotId())
        )));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(
            Component.translatable("item.towing_invoice.vehicle", invoice.getVehicleId().toString().substring(0, 8))
        )));

        display.put("Lore", lore);
        tag.put("display", display);

        container.setItem(0, invoiceItem);
        return container;
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        // Player inventory (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar (1x9)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No quick move functionality needed
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /**
     * Read-only slot for invoice display
     */
    private static class InvoiceSlot extends Slot {
        public InvoiceSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false; // Cannot place items
        }

        @Override
        public boolean mayPickup(Player player) {
            return false; // Cannot take item
        }

        @Override
        public int getMaxStackSize() {
            return 1; // Always 1
        }
    }
}
