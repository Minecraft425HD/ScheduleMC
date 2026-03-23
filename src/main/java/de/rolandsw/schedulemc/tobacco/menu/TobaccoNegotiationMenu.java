package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Container-Menu für Tabak-Verhandlung mit NPC
 *
 * Features:
 * - 9 Hotbar-Slots des Spielers werden angezeigt (nur Drugs sind selektierbar)
 * - Keine Item-Bewegung möglich (nur Auswahl)
 * - Cooldown-Status wird synchronisiert
 */
public class TobaccoNegotiationMenu extends AbstractContainerMenu {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    public static final int HOTBAR_SLOT_COUNT = 9;
    public static final int HOTBAR_SLOT_START = 0;
    public static final int HOTBAR_SLOT_END = HOTBAR_SLOT_START + HOTBAR_SLOT_COUNT;

    // Slot-Positionen im GUI (3x3 Grid)
    public static final int SLOT_START_X = 8;
    public static final int SLOT_START_Y = 32;
    public static final int SLOT_SIZE = 18;
    public static final int SLOTS_PER_ROW = 3;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final int npcEntityId;
    private final Inventory playerInventory;
    private boolean hasCooldown;
    private int selectedSlot = -1;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    // Server-side constructor
    public TobaccoNegotiationMenu(int containerId, Inventory playerInventory, int npcEntityId) {
        super(ModMenuTypes.TOBACCO_NEGOTIATION_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.npcEntityId = npcEntityId;
        this.hasCooldown = false;

        // Füge Hotbar-Slots hinzu (3x3 Grid Layout)
        addHotbarSlots(playerInventory);
    }

    // Client-side constructor
    public TobaccoNegotiationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readInt());
        this.hasCooldown = extraData.readBoolean();
    }

    // ═══════════════════════════════════════════════════════════
    // SLOT MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt die 9 Hotbar-Slots in einem 3x3 Grid hinzu
     */
    private void addHotbarSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotIndex = row * SLOTS_PER_ROW + col;
                int x = SLOT_START_X + col * SLOT_SIZE;
                int y = SLOT_START_Y + row * SLOT_SIZE;

                this.addSlot(new DrugOnlySlot(inventory, slotIndex, x, y));
            }
        }
    }

    /**
     * Custom Slot der nur Drogen-Items anzeigt/selektiert
     */
    private static class DrugOnlySlot extends Slot {
        public DrugOnlySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            // Kein Item kann platziert werden (nur Anzeige)
            return false;
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            // Kein Item kann entnommen werden (nur Anzeige)
            return false;
        }

        /**
         * Prüft ob der Slot ein gültiges Drogen-Item enthält
         */
        public boolean containsDrug() {
            ItemStack stack = getItem();
            return !stack.isEmpty() &&
                   stack.getItem() instanceof PackagedDrugItem;
        }

        /**
         * Prüft ob der Slot ein spezifisches Drogen-Item enthält
         */
        public boolean containsDrugType(DrugType type) {
            ItemStack stack = getItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof PackagedDrugItem)) {
                return false;
            }
            return PackagedDrugItem.getDrugType(stack) == type;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MENU METHODS
    // ═══════════════════════════════════════════════════════════

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        // Kein Shift-Click in diesem GUI
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        Entity entity = player.level().getEntity(npcEntityId);
        return entity instanceof CustomNPCEntity && entity.isAlive() && player.distanceToSqr(entity) <= 64.0;
    }

    // ═══════════════════════════════════════════════════════════
    // SELECTION METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wählt einen Slot aus
     */
    public boolean selectSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= HOTBAR_SLOT_COUNT) {
            return false;
        }

        Slot slot = this.slots.get(slotIndex);
        if (slot instanceof DrugOnlySlot drugSlot && drugSlot.containsDrug()) {
            this.selectedSlot = slotIndex;
            return true;
        }

        return false;
    }

    /**
     * Holt den ausgewählten Slot-Index
     */
    public int getSelectedSlot() {
        return selectedSlot;
    }

    /**
     * Holt das Item im ausgewählten Slot
     */
    public ItemStack getSelectedItem() {
        if (selectedSlot < 0 || selectedSlot >= HOTBAR_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return this.slots.get(selectedSlot).getItem();
    }

    /**
     * Prüft ob ein Slot ein gültiges Drogen-Item enthält
     */
    public boolean isSlotValid(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= HOTBAR_SLOT_COUNT) {
            return false;
        }
        Slot slot = this.slots.get(slotIndex);
        return slot instanceof DrugOnlySlot drugSlot && drugSlot.containsDrug();
    }

    /**
     * Holt den DrugType des Items in einem Slot
     */
    public DrugType getDrugTypeInSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= HOTBAR_SLOT_COUNT) {
            return null;
        }
        ItemStack stack = this.slots.get(slotIndex).getItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof PackagedDrugItem)) {
            return null;
        }
        return PackagedDrugItem.getDrugType(stack);
    }

    // ═══════════════════════════════════════════════════════════
    // COOLDOWN
    // ═══════════════════════════════════════════════════════════

    public void setCooldown(boolean hasCooldown) {
        this.hasCooldown = hasCooldown;
    }

    public boolean hasCooldown() {
        return hasCooldown;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public int getNpcEntityId() {
        return npcEntityId;
    }

    public CustomNPCEntity getNpc() {
        if (playerInventory.player.level().getEntity(npcEntityId) instanceof CustomNPCEntity npc) {
            return npc;
        }
        return null;
    }

    public Inventory getPlayerInventory() {
        return playerInventory;
    }
}
