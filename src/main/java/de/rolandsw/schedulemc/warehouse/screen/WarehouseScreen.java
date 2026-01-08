package de.rolandsw.schedulemc.warehouse.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.ShopAccountManager;
import de.rolandsw.schedulemc.economy.ShopAccount;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import de.rolandsw.schedulemc.warehouse.client.ClientWarehouseNPCCache;
import de.rolandsw.schedulemc.warehouse.menu.WarehouseMenu;
import de.rolandsw.schedulemc.warehouse.network.WarehouseNetworkHandler;
import de.rolandsw.schedulemc.warehouse.network.packet.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Warehouse GUI Screen - Tab-basiertes Management Panel
 *
 * Tabs:
 * - Items: Item-Verwaltung mit scrollbarer Liste
 * - Seller: Verkäufer-Verwaltung
 * - Stats: Statistiken und Übersicht
 * - Settings: Konfiguration
 */
@OnlyIn(Dist.CLIENT)
public class WarehouseScreen extends AbstractContainerScreen<WarehouseMenu> {

    // GUI Dimensionen
    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 240;

    // Farben (Dark Theme)
    private static final int COLOR_BG = 0xFF2B2B2B;
    private static final int COLOR_BG_LIGHT = 0xFF3C3C3C;
    private static final int COLOR_BORDER = 0xFF1A1A1A;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_GRAY = 0xFFAAAAAA;
    private static final int COLOR_ACCENT = 0xFF4A90E2;
    private static final int COLOR_SUCCESS = 0xFF5CB85C;
    private static final int COLOR_WARNING = 0xFFF0AD4E;
    private static final int COLOR_DANGER = 0xFFD9534F;

    // Tabs
    private enum Tab {
        ITEMS("Items", "📦"),
        SELLERS("Seller", "👥"),
        STATS("Stats", "📊"),
        SETTINGS("Einstellungen", "⚙");

        final String name;
        final String icon;

        Tab(String name, String icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    private Tab currentTab = Tab.ITEMS;

    // Items Tab - Scrolling
    private int itemScrollOffset = 0;
    private static final int ITEMS_VISIBLE_ROWS = 6;
    private int selectedSlotIndex = -1;

    // Seller Tab - Scrolling
    private int sellerScrollOffset = 0;
    private static final int SELLER_VISIBLE_ROWS = 8;
    private int availableNpcScrollOffset = 0;
    private static final int AVAILABLE_NPC_VISIBLE_ROWS = 6;

    // Stats Tab - Scrolling
    private int statsScrollOffset = 0;
    private static final int STATS_CONTENT_HEIGHT = 400; // Geschätzte Höhe des gesamten Inhalts
    private static final int STATS_VISIBLE_HEIGHT = 180; // Sichtbare Höhe im Tab

    // Input fields
    private EditBox slotCapacityInput;
    private EditBox deliveryIntervalInput;
    private EditBox shopIdInput;

    // Item selection overlay
    private boolean showItemSelection = false;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    private int itemSelectionScrollOffset = 0;
    private static final int ITEM_SELECTION_VISIBLE_ROWS = 10;
    private EditBox itemSearchField;

    // Clickable areas for NPCs
    private static class ClickableNPC {
        UUID npcId;
        int x, y, width, height;
        boolean isRemove; // true = remove button, false = add button

        ClickableNPC(UUID npcId, int x, int y, int width, int height, boolean isRemove) {
            this.npcId = npcId;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isRemove = isRemove;
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private List<ClickableNPC> clickableNPCs = new ArrayList<>();

    public WarehouseScreen(WarehouseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

        // Lade alle Items
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item != net.minecraft.world.item.Items.AIR) {
                allItems.add(item);
            }
        });
        // Sortiere alphabetisch
        allItems.sort((a, b) -> a.getDescription().getString().compareToIgnoreCase(b.getDescription().getString()));
        filteredItems = new ArrayList<>(allItems);
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Tab Buttons
        int tabX = x + 10;
        int tabY = y + 5;
        int tabWidth = 90;
        int tabHeight = 20;

        for (Tab tab : Tab.values()) {
            final Tab finalTab = tab;
            addRenderableWidget(Button.builder(
                Component.literal(tab.icon + " " + tab.name),
                button -> switchTab(finalTab)
            ).bounds(tabX, tabY, tabWidth, tabHeight).build());

            tabX += tabWidth + 5;
        }

        // Initialisiere Tab-spezifische Komponenten
        initTabComponents();
    }

    /**
     * Initialisiert die Komponenten für den aktuellen Tab
     */
    private void initTabComponents() {
        // Entferne alte Widgets (außer Tab-Buttons)
        clearTabWidgets();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        switch (currentTab) {
            case ITEMS -> initItemsTab(x, y);
            case SELLERS -> initSellersTab(x, y);
            case STATS -> initStatsTab(x, y);
            case SETTINGS -> initSettingsTab(x, y);
        }
    }

    /**
     * Entfernt Tab-spezifische Widgets (behält nur die ersten 4 Tab-Buttons)
     */
    private void clearTabWidgets() {
        // Behalte nur die 4 Tab-Buttons
        while (renderables.size() > 4) {
            renderables.remove(renderables.size() - 1);
        }
        children().removeIf(widget -> !renderables.contains(widget));
    }

    /**
     * Wechselt den Tab
     */
    private void switchTab(Tab newTab) {
        if (currentTab != newTab) {
            currentTab = newTab;
            itemScrollOffset = 0;
            sellerScrollOffset = 0;
            selectedSlotIndex = -1;
            init(); // Neu-initialisieren für neue Widgets
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ITEMS TAB
    // ═══════════════════════════════════════════════════════════

    private void initItemsTab(int x, int y) {
        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null) return;

        WarehouseSlot[] slots = warehouse.getSlots();
        int nonEmptySlots = 0;
        for (WarehouseSlot slot : slots) {
            if (!slot.isEmpty()) nonEmptySlots++;
        }

        // Scroll Buttons (wenn mehr als ITEMS_VISIBLE_ROWS)
        final int finalNonEmptySlots = nonEmptySlots;
        if (nonEmptySlots > ITEMS_VISIBLE_ROWS) {
            addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                if (itemScrollOffset > 0) {
                    itemScrollOffset--;
                    initTabComponents();
                }
            }).bounds(x + 250, y + 35, 15, 15).build());

            addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                if (itemScrollOffset < finalNonEmptySlots - ITEMS_VISIBLE_ROWS) {
                    itemScrollOffset++;
                    initTabComponents();
                }
            }).bounds(x + 250, y + 165, 15, 15).build());
        }

        // Slot Detail Buttons (wenn ein Slot ausgewählt ist)
        if (selectedSlotIndex >= 0 && selectedSlotIndex < slots.length) {
            WarehouseSlot selectedSlot = slots[selectedSlotIndex];
            int detailX = x + 270;
            int detailY = y + 40;

            // +100 Button
            addRenderableWidget(Button.builder(Component.literal("+100"), button -> {
                sendModifySlotPacket(selectedSlotIndex, 100);
            }).bounds(detailX, detailY, 50, 20).build());

            // +1000 Button
            addRenderableWidget(Button.builder(Component.literal("+1000"), button -> {
                sendModifySlotPacket(selectedSlotIndex, 1000);
            }).bounds(detailX + 55, detailY, 55, 20).build());

            // -100 Button
            addRenderableWidget(Button.builder(Component.literal("-100"), button -> {
                sendModifySlotPacket(selectedSlotIndex, -100);
            }).bounds(detailX, detailY + 25, 50, 20).build());

            // -1000 Button
            addRenderableWidget(Button.builder(Component.literal("-1000"), button -> {
                sendModifySlotPacket(selectedSlotIndex, -1000);
            }).bounds(detailX + 55, detailY + 25, 55, 20).build());

            // Leeren Button
            addRenderableWidget(Button.builder(Component.translatable("gui.common.clear"), button -> {
                sendClearSlotPacket(selectedSlotIndex);
                selectedSlotIndex = -1;
                initTabComponents();
            }).bounds(detailX, detailY + 55, 105, 20).build());

            // Auffüllen Button
            addRenderableWidget(Button.builder(Component.translatable("gui.warehouse.auto_fill"), button -> {
                int restockAmount = selectedSlot.getRestockAmount();
                if (restockAmount > 0) {
                    sendModifySlotPacket(selectedSlotIndex, restockAmount);
                }
            }).bounds(detailX, detailY + 80, 105, 20).build());
        }

        // Neuer Slot Button
        addRenderableWidget(Button.builder(Component.translatable("gui.warehouse.new_slot"), button -> {
            openItemSelection();
        }).bounds(x + 10, y + 210, 100, 20).build());

        // Auto-Fill Button
        addRenderableWidget(Button.builder(Component.translatable("gui.warehouse.auto_fill"), button -> {
            sendAutoFillPacket();
        }).bounds(x + 115, y + 210, 80, 20).build());
    }

    private void sendModifySlotPacket(int slotIndex, int amount) {
        WarehouseNetworkHandler.INSTANCE.sendToServer(
            new ModifySlotPacket(menu.getBlockPos(), slotIndex, amount)
        );
    }

    private void sendClearSlotPacket(int slotIndex) {
        WarehouseNetworkHandler.INSTANCE.sendToServer(
            new ClearSlotPacket(menu.getBlockPos(), slotIndex)
        );
    }

    private void sendAutoFillPacket() {
        WarehouseNetworkHandler.INSTANCE.sendToServer(
            new AutoFillPacket(menu.getBlockPos())
        );
    }

    // ═══════════════════════════════════════════════════════════
    // SELLERS TAB
    // ═══════════════════════════════════════════════════════════

    private void initSellersTab(int x, int y) {
        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null) return;

        List<UUID> sellers = warehouse.getLinkedSellers();

        // Scroll Buttons für verknüpfte Seller
        if (sellers.size() > SELLER_VISIBLE_ROWS) {
            addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                if (sellerScrollOffset > 0) {
                    sellerScrollOffset--;
                    initTabComponents();
                }
            }).bounds(x + 185, y + 35, 15, 15).build());

            addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                if (sellerScrollOffset < sellers.size() - SELLER_VISIBLE_ROWS) {
                    sellerScrollOffset++;
                    initTabComponents();
                }
            }).bounds(x + 185, y + 165, 15, 15).build());
        }

        // Get available NPCs
        List<CustomNPCEntity> availableNpcs = getAvailableNPCs();

        // Scroll Buttons für verfügbare NPCs
        if (availableNpcs.size() > AVAILABLE_NPC_VISIBLE_ROWS) {
            addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                if (availableNpcScrollOffset > 0) {
                    availableNpcScrollOffset--;
                    initTabComponents();
                }
            }).bounds(x + imageWidth - 25, y + 35, 15, 15).build());

            addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                if (availableNpcScrollOffset < availableNpcs.size() - AVAILABLE_NPC_VISIBLE_ROWS) {
                    availableNpcScrollOffset++;
                    initTabComponents();
                }
            }).bounds(x + imageWidth - 25, y + 165, 15, 15).build());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATS TAB
    // ═══════════════════════════════════════════════════════════

    private void initStatsTab(int x, int y) {
        // Scroll Buttons nur anzeigen wenn Content größer als sichtbare Höhe
        int maxScrollOffset = Math.max(0, STATS_CONTENT_HEIGHT - STATS_VISIBLE_HEIGHT);

        if (maxScrollOffset > 0) {
            // Scroll Up Button
            addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                statsScrollOffset = Math.max(0, statsScrollOffset - 20);
            }).bounds(x + imageWidth - 25, y + 35, 20, 20).build());

            // Scroll Down Button
            addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                statsScrollOffset = Math.min(maxScrollOffset, statsScrollOffset + 20);
            }).bounds(x + imageWidth - 25, y + imageHeight - 30, 20, 20).build());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SETTINGS TAB
    // ═══════════════════════════════════════════════════════════

    private void initSettingsTab(int x, int y) {
        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null) return;

        // Shop ID Input
        shopIdInput = new EditBox(this.font, x + 120, y + 50, 150, 20, Component.translatable("gui.warehouse.shop_id"));
        shopIdInput.setValue(warehouse.getShopId() != null ? warehouse.getShopId() : "");
        shopIdInput.setMaxLength(50);
        addRenderableWidget(shopIdInput);

        // Save Button
        addRenderableWidget(Button.builder(Component.translatable("gui.common.save"), button -> {
            sendUpdateSettingsPacket();
        }).bounds(x + 280, y + 210, 100, 20).build());

        // Reset Button
        addRenderableWidget(Button.builder(Component.translatable("gui.warehouse.reset"), button -> {
            initTabComponents();
        }).bounds(x + 170, y + 210, 100, 20).build());
    }

    private void sendUpdateSettingsPacket() {
        String shopId = shopIdInput.getValue().trim();
        if (shopId.isEmpty()) shopId = null;

        WarehouseNetworkHandler.INSTANCE.sendToServer(
            new UpdateSettingsPacket(menu.getBlockPos(), shopId)
        );

        minecraft.player.sendSystemMessage(Component.translatable("message.warehouse.settings_saved"));
    }

    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Background
        graphics.fill(x, y, x + imageWidth, y + imageHeight, COLOR_BG);

        // Border
        graphics.fill(x, y, x + imageWidth, y + 1, COLOR_BORDER); // Top
        graphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, COLOR_BORDER); // Bottom
        graphics.fill(x, y, x + 1, y + imageHeight, COLOR_BORDER); // Left
        graphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, COLOR_BORDER); // Right

        // Tab Content Area
        graphics.fill(x + 5, y + 30, x + imageWidth - 5, y + imageHeight - 5, COLOR_BG_LIGHT);

        // Render Tab Content
        switch (currentTab) {
            case ITEMS -> renderItemsTab(graphics, x, y, mouseX, mouseY);
            case SELLERS -> renderSellersTab(graphics, x, y, mouseX, mouseY);
            case STATS -> renderStatsTab(graphics, x, y, mouseX, mouseY);
            case SETTINGS -> renderSettingsTab(graphics, x, y, mouseX, mouseY);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        // Render item selection overlay on top
        if (showItemSelection) {
            renderItemSelectionOverlay(graphics, mouseX, mouseY);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ITEMS TAB RENDERING
    // ═══════════════════════════════════════════════════════════

    private void renderItemsTab(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null) return;

        // Left Panel: Item List
        graphics.drawString(this.font, "§lITEM LISTE", x + 10, y + 35, COLOR_TEXT, false);

        WarehouseSlot[] slots = warehouse.getSlots();
        int renderY = y + 50;
        int visibleCount = 0;
        int currentIndex = 0;

        for (int i = 0; i < slots.length; i++) {
            WarehouseSlot slot = slots[i];
            if (slot.isEmpty()) continue;

            if (currentIndex < itemScrollOffset) {
                currentIndex++;
                continue;
            }

            if (visibleCount >= ITEMS_VISIBLE_ROWS) break;

            // Highlight if selected
            if (i == selectedSlotIndex) {
                graphics.fill(x + 10, renderY - 2, x + 240, renderY + 18, COLOR_ACCENT);
            }

            // Render Item Icon
            if (slot.getAllowedItem() != null) {
                ItemStack stack = new ItemStack(slot.getAllowedItem());
                graphics.renderItem(stack, x + 12, renderY);
            }

            // Item Name
            String itemName = slot.getAllowedItem() != null ?
                slot.getAllowedItem().getDescription().getString() : "Leer";
            graphics.drawString(this.font, itemName, x + 32, renderY + 4, COLOR_TEXT, false);

            // Stock Info - Unlimited als ∞ anzeigen
            String stockInfo;
            if (slot.isUnlimited()) {
                stockInfo = slot.getStock() + " / §a∞";
            } else {
                stockInfo = slot.getStock() + " / " + slot.getMaxCapacity();
            }
            int stockColor = slot.isFull() ? COLOR_SUCCESS :
                            slot.getStock() < slot.getMaxCapacity() / 4 ? COLOR_DANGER : COLOR_TEXT;
            graphics.drawString(this.font, stockInfo, x + 180, renderY + 4, stockColor, false);

            // Click handler for selection
            if (mouseX >= x + 10 && mouseX <= x + 240 &&
                mouseY >= renderY - 2 && mouseY <= renderY + 18) {
                if (minecraft.mouseHandler.isLeftPressed()) {
                    selectedSlotIndex = i;
                    initTabComponents();
                }
            }

            renderY += 22;
            visibleCount++;
            currentIndex++;
        }

        // Right Panel: Slot Details
        graphics.drawString(this.font, "§lSLOT DETAILS", x + 270, y + 35, COLOR_TEXT, false);

        if (selectedSlotIndex >= 0 && selectedSlotIndex < slots.length) {
            WarehouseSlot selectedSlot = slots[selectedSlotIndex];
            int detailY = y + 120;

            graphics.drawString(this.font, "Item: " +
                (selectedSlot.getAllowedItem() != null ?
                    selectedSlot.getAllowedItem().getDescription().getString() : "N/A"),
                x + 270, detailY, COLOR_TEXT_GRAY, false);

            graphics.drawString(this.font, "Bestand: " + selectedSlot.getStock(),
                x + 270, detailY + 12, COLOR_TEXT_GRAY, false);

            graphics.drawString(this.font, "Max: " + selectedSlot.getMaxCapacity(),
                x + 270, detailY + 24, COLOR_TEXT_GRAY, false);

            graphics.drawString(this.font, "Frei: " + selectedSlot.getAvailableSpace(),
                x + 270, detailY + 36, COLOR_TEXT_GRAY, false);
        } else {
            graphics.drawString(this.font, "Kein Slot ausgewählt",
                x + 270, y + 120, COLOR_TEXT_GRAY, false);

            // Zeige Hilfetext wenn keine Items vorhanden
            if (warehouse.getUsedSlots() == 0) {
                int helpY = y + 140;
                graphics.drawString(this.font, "§7So fügen Sie Items hinzu:",
                    x + 270, helpY, COLOR_TEXT_GRAY, false);
                helpY += 12;

                graphics.drawString(this.font, "§7Verwenden Sie den Command:",
                    x + 270, helpY, COLOR_TEXT_GRAY, false);
                helpY += 12;

                graphics.drawString(this.font, "§e/warehouse add <item> <amount>",
                    x + 270, helpY, COLOR_WARNING, false);
            }
        }

        // NPC Shop Items Section (unten rechts)
        int npcShopY = y + 160;
        graphics.drawString(this.font, "§l§eNPC SHOP", x + 270, npcShopY, COLOR_WARNING, false);
        npcShopY += 12;

        List<NPCData.ShopEntry> shopItems = getLinkedNPCShopItems();
        if (shopItems != null && !shopItems.isEmpty()) {
            // Zeige erste 3 Shop-Items als Beispiel
            int shown = 0;
            for (NPCData.ShopEntry entry : shopItems) {
                if (shown >= 3) break;

                ItemStack item = entry.getItem();
                if (!item.isEmpty()) {
                    // Render icon
                    graphics.renderItem(item, x + 270, npcShopY - 2);

                    // Item name (gekürzt)
                    String itemName = item.getHoverName().getString();
                    if (itemName.length() > 10) {
                        itemName = itemName.substring(0, 10) + "...";
                    }
                    graphics.drawString(this.font, itemName, x + 290, npcShopY + 2, COLOR_TEXT_GRAY, false);

                    // Status: Unlimited oder Stock
                    String status = entry.isUnlimited() ?
                        "§a∞" : "§e" + entry.getStock();
                    graphics.drawString(this.font, status, x + 365, npcShopY + 2, COLOR_TEXT, false);

                    npcShopY += 14;
                    shown++;
                }
            }

            if (shopItems.size() > 3) {
                graphics.drawString(this.font, "§7+" + (shopItems.size() - 3) + " mehr...",
                    x + 270, npcShopY, COLOR_TEXT_GRAY, false);
            }
        } else {
            graphics.drawString(this.font, "§7Kein NPC Shop", x + 270, npcShopY, COLOR_TEXT_GRAY, false);
        }

        // Bottom Info
        int usedSlots = warehouse.getUsedSlots();
        int totalSlots = slots.length;
        double fillPercentage = (double) usedSlots / totalSlots * 100;

        graphics.drawString(this.font,
            String.format("Slots: %d/%d (%.0f%%)", usedSlots, totalSlots, fillPercentage),
            x + 210, y + 215, COLOR_TEXT_GRAY, false);
    }

    // ═══════════════════════════════════════════════════════════
    // SELLERS TAB RENDERING
    // ═══════════════════════════════════════════════════════════

    private void renderSellersTab(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null) return;

        // Clear clickable areas at start of render
        clickableNPCs.clear();

        // Left Panel: Verknüpfte Verkäufer
        graphics.drawString(this.font, "§lVERKNÜPFTE VERKÄUFER", x + 10, y + 35, COLOR_TEXT, false);

        List<UUID> sellers = warehouse.getLinkedSellers();
        int renderY = y + 50;
        int visibleCount = 0;

        for (int i = sellerScrollOffset; i < sellers.size() && visibleCount < SELLER_VISIBLE_ROWS; i++) {
            UUID sellerId = sellers.get(i);

            // Get NPC name
            String npcName = getNPCName(sellerId);

            graphics.drawString(this.font, "✓ " + npcName, x + 15, renderY, COLOR_SUCCESS, false);

            // Remove button area
            int removeX = x + 160;
            int removeY = renderY - 2;
            boolean isHovered = mouseX >= removeX && mouseX <= removeX + 30 &&
                               mouseY >= removeY && mouseY <= removeY + 12;

            if (isHovered) {
                graphics.fill(removeX, removeY, removeX + 30, removeY + 12, COLOR_DANGER);
                graphics.drawString(this.font, "[X]", removeX + 8, renderY, COLOR_TEXT, false);
            } else {
                graphics.drawString(this.font, "[X]", removeX + 8, renderY, COLOR_DANGER, false);
            }

            // Register clickable area
            clickableNPCs.add(new ClickableNPC(sellerId, removeX, removeY, 30, 12, true));

            renderY += 18;
            visibleCount++;
        }

        if (sellers.isEmpty()) {
            graphics.drawString(this.font, "Keine Verkäufer verknüpft",
                x + 15, y + 50, COLOR_TEXT_GRAY, false);
        }

        // Right Panel: Verfügbare NPCs
        graphics.drawString(this.font, "§lVERFÜGBARE NPCS", x + 210, y + 35, COLOR_TEXT, false);

        List<CustomNPCEntity> availableNpcs = getAvailableNPCs();
        renderY = y + 50;
        visibleCount = 0;

        for (int i = availableNpcScrollOffset; i < availableNpcs.size() && visibleCount < AVAILABLE_NPC_VISIBLE_ROWS; i++) {
            CustomNPCEntity npc = availableNpcs.get(i);
            String npcName = npc.getNpcName();

            // Add button area
            int addX = x + 215;
            int addY = renderY - 2;
            boolean isHovered = mouseX >= addX && mouseX <= addX + 160 &&
                               mouseY >= addY && mouseY <= addY + 12;

            if (isHovered) {
                graphics.fill(addX, addY, addX + 160, addY + 12, COLOR_ACCENT);
                graphics.drawString(this.font, "+ " + npcName, addX + 5, renderY, COLOR_TEXT, false);
            } else {
                graphics.drawString(this.font, "+ " + npcName, addX + 5, renderY, COLOR_TEXT, false);
            }

            // Register clickable area
            clickableNPCs.add(new ClickableNPC(npc.getUUID(), addX, addY, 160, 12, false));

            renderY += 18;
            visibleCount++;
        }

        if (availableNpcs.isEmpty()) {
            graphics.drawString(this.font, "Alle NPCs sind verknüpft",
                x + 215, y + 50, COLOR_TEXT_GRAY, false);
        }

        // Bottom info
        graphics.drawString(this.font, "Verknüpft: " + sellers.size(),
            x + 10, y + 215, COLOR_TEXT_GRAY, false);
        graphics.drawString(this.font, "Verfügbar: " + availableNpcs.size(),
            x + 210, y + 215, COLOR_TEXT_GRAY, false);
    }

    private void sendRemoveSellerPacket(UUID sellerId) {
        WarehouseNetworkHandler.INSTANCE.sendToServer(
            new RemoveSellerPacket(menu.getBlockPos(), sellerId)
        );
    }

    private void sendAddSellerPacket(UUID sellerId) {
        WarehouseNetworkHandler.INSTANCE.sendToServer(
            new AddSellerPacket(menu.getBlockPos(), sellerId)
        );
    }

    /**
     * Gibt alle verfügbaren NPCs zurück, die nicht mit einem Warehouse verknüpft sind
     */
    private List<CustomNPCEntity> getAvailableNPCs() {
        List<CustomNPCEntity> availableNpcs = new ArrayList<>();
        if (minecraft.level == null) return availableNpcs;

        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null) return availableNpcs;

        // Hole die Liste der bereits verknüpften Seller
        List<UUID> linkedSellers = warehouse.getLinkedSellers();

        // Sammle alle Custom NPCs in der Welt
        for (var entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof CustomNPCEntity npc) {
                // Cache den NPC-Namen für spätere Verwendung
                ClientWarehouseNPCCache.putNPC(npc.getUUID(), npc.getNpcName());

                // Prüfe ob NPC bereits mit DIESEM Warehouse verknüpft ist
                // Verwende die Warehouse-Liste als Source of Truth, nicht die NPC-Daten
                if (!linkedSellers.contains(npc.getUUID())) {
                    availableNpcs.add(npc);
                }
            }
        }

        // Sortiere nach Namen für bessere Übersicht
        availableNpcs.sort((a, b) -> a.getNpcName().compareToIgnoreCase(b.getNpcName()));

        return availableNpcs;
    }

    // ═══════════════════════════════════════════════════════════
    // STATS TAB RENDERING
    // ═══════════════════════════════════════════════════════════

    private void renderStatsTab(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null) return;

        // Apply clipping to content area
        int clipX = x + 5;
        int clipY = y + 35;
        int clipWidth = imageWidth - 35; // Leave space for scroll buttons
        int clipHeight = STATS_VISIBLE_HEIGHT;

        graphics.enableScissor(clipX, clipY, clipX + clipWidth, clipY + clipHeight);

        int contentY = y + 40 - statsScrollOffset; // Apply scroll offset

        // === LAGERBESTAND ÜBERSICHT ===
        graphics.drawString(this.font, "§l§e📊 LAGERBESTAND ÜBERSICHT", x + 10, contentY, COLOR_TEXT, false);
        contentY += 15;

        WarehouseSlot[] slots = warehouse.getSlots();
        int usedSlots = warehouse.getUsedSlots();
        int totalSlots = slots.length;
        double fillPercentage = (double) usedSlots / totalSlots * 100;

        // Progress Bar
        int barWidth = 300;
        int barHeight = 20;
        int barX = x + 10;
        int filledWidth = (int) (barWidth * fillPercentage / 100);

        graphics.fill(barX, contentY, barX + barWidth, contentY + barHeight, COLOR_BG_LIGHT);
        graphics.fill(barX, contentY, barX + filledWidth, contentY + barHeight, COLOR_SUCCESS);
        graphics.drawString(this.font, String.format("%.0f%% ausgelastet (%d/%d Slots)",
            fillPercentage, usedSlots, totalSlots),
            barX + 80, contentY + 6, COLOR_TEXT, false);

        contentY += 35;

        // Top 5 Items
        graphics.drawString(this.font, "Top 5 Items nach Bestand:", x + 10, contentY, COLOR_TEXT_GRAY, false);
        contentY += 12;

        // Simple top 5 (sorted by stock)
        List<WarehouseSlot> sortedSlots = new ArrayList<>();
        for (WarehouseSlot slot : slots) {
            if (!slot.isEmpty()) sortedSlots.add(slot);
        }
        sortedSlots.sort((a, b) -> Integer.compare(b.getStock(), a.getStock()));

        for (int i = 0; i < Math.min(5, sortedSlots.size()); i++) {
            WarehouseSlot slot = sortedSlots.get(i);
            String itemName = slot.getAllowedItem() != null ?
                slot.getAllowedItem().getDescription().getString() : "Unknown";

            int percentage = (int) ((double) slot.getStock() / slot.getMaxCapacity() * 100);
            String status = slot.isFull() ? " [VOLL]" : "";

            graphics.drawString(this.font,
                String.format("%d. %s: %d/%d (%d%%)%s",
                    i + 1, itemName, slot.getStock(), slot.getMaxCapacity(), percentage, status),
                x + 15, contentY, COLOR_TEXT, false);
            contentY += 12;
        }

        contentY += 10;

        // === FINANZEN ===
        graphics.drawString(this.font, "§l§e💰 FINANZEN", x + 10, contentY, COLOR_TEXT, false);
        contentY += 15;

        String shopId = warehouse.getShopId();
        if (shopId != null) {
            ShopAccount account = ShopAccountManager.getAccount(shopId);
            if (account != null) {
                int netRevenue7Days = account.get7DayNetRevenue();
                graphics.drawString(this.font, "Nettoumsatz (7 Tage): " + String.format("%d€", netRevenue7Days),
                    x + 15, contentY, netRevenue7Days >= 0 ? COLOR_SUCCESS : COLOR_DANGER, false);
                contentY += 12;

                // Expense tracking über 30 Tage
                long currentTime = minecraft.level != null ? minecraft.level.getGameTime() : 0;
                int totalExpenses30Days = warehouse.getTotalExpenses(currentTime, 30);
                int deliveryCount30Days = warehouse.getDeliveryCount(currentTime, 30);
                double avgExpensePerDelivery = warehouse.getAverageExpensePerDelivery(currentTime, 30);

                graphics.drawString(this.font,
                    "Ausgaben (30 Tage): " + String.format("%d€", totalExpenses30Days),
                    x + 15, contentY, COLOR_WARNING, false);
                contentY += 12;

                if (deliveryCount30Days > 0) {
                    graphics.drawString(this.font,
                        "  Lieferungen: " + deliveryCount30Days + "x | Ø " + String.format("%.0f€", avgExpensePerDelivery),
                        x + 15, contentY, COLOR_TEXT_GRAY, false);
                    contentY += 12;

                    // Zeige letzte 3 Lieferungen
                    List<de.rolandsw.schedulemc.warehouse.ExpenseEntry> recentExpenses = warehouse.getExpenses();
                    if (!recentExpenses.isEmpty()) {
                        graphics.drawString(this.font, "  Letzte Lieferungen:",
                            x + 15, contentY, COLOR_TEXT_GRAY, false);
                        contentY += 12;

                        // Zeige bis zu 3 der letzten Lieferungen
                        int shown = 0;
                        for (int i = recentExpenses.size() - 1; i >= 0 && shown < 3; i--) {
                            de.rolandsw.schedulemc.warehouse.ExpenseEntry expense = recentExpenses.get(i);
                            int ageDays = expense.getAgeDays(currentTime);
                            String ageStr = ageDays == 0 ? "heute" : "vor " + ageDays + "d";

                            graphics.drawString(this.font,
                                String.format("    • %d€ (%s)", expense.getAmount(), ageStr),
                                x + 15, contentY, COLOR_TEXT_GRAY, false);
                            contentY += 10;
                            shown++;
                        }
                        contentY += 2;
                    }
                }
            } else {
                graphics.drawString(this.font, "Shop-Konto nicht gefunden: " + shopId,
                    x + 15, contentY, COLOR_DANGER, false);
            }
        } else {
            graphics.drawString(this.font, "Kein Shop-Konto verknüpft",
                x + 15, contentY, COLOR_TEXT_GRAY, false);
        }

        contentY += 20;

        // === AUTO-DELIVERY ===
        graphics.drawString(this.font, "§l§e📦 AUTO-DELIVERY", x + 10, contentY, COLOR_TEXT, false);
        contentY += 15;

        graphics.drawString(this.font, "Status: Aktiv ✓", x + 15, contentY, COLOR_SUCCESS, false);
        contentY += 12;

        long lastDeliveryDay = warehouse.getLastDeliveryDay();
        long currentDay = minecraft.level != null ? minecraft.level.getDayTime() / 24000L : 0;
        long intervalDays = ModConfigHandler.COMMON.WAREHOUSE_DELIVERY_INTERVAL_DAYS.get();
        long daysUntilNext = (lastDeliveryDay + intervalDays) - currentDay;

        graphics.drawString(this.font,
            "Nächste Lieferung: in " + Math.max(0, daysUntilNext) + " Tagen",
            x + 15, contentY, COLOR_TEXT, false);
        contentY += 12;

        graphics.drawString(this.font,
            "Interval: alle " + ModConfigHandler.COMMON.WAREHOUSE_DELIVERY_INTERVAL_DAYS.get() + " Tage",
            x + 15, contentY, COLOR_TEXT_GRAY, false);

        // Disable scissor after rendering
        graphics.disableScissor();
    }

    // ═══════════════════════════════════════════════════════════
    // SETTINGS TAB RENDERING
    // ═══════════════════════════════════════════════════════════

    private void renderSettingsTab(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null) return;

        int contentY = y + 40;

        graphics.drawString(this.font, "§l§e⚙ WAREHOUSE KONFIGURATION", x + 10, contentY, COLOR_TEXT, false);
        contentY += 20;

        // Shop ID
        graphics.drawString(this.font, "Shop ID:", x + 15, contentY + 5, COLOR_TEXT, false);
        // Input field is rendered by widget system
        contentY += 30;

        // Auto-Delivery Info
        graphics.drawString(this.font, "Auto-Delivery:", x + 15, contentY, COLOR_TEXT, false);
        contentY += 15;

        graphics.drawString(this.font,
            "  Aktiviert: Ja",
            x + 20, contentY, COLOR_SUCCESS, false);
        contentY += 12;

        graphics.drawString(this.font,
            "  Interval: " + ModConfigHandler.COMMON.WAREHOUSE_DELIVERY_INTERVAL_DAYS.get() + " Tage",
            x + 20, contentY, COLOR_TEXT_GRAY, false);
        contentY += 20;

        // Slot Config
        graphics.drawString(this.font, "Slot-Konfiguration:", x + 15, contentY, COLOR_TEXT, false);
        contentY += 15;

        WarehouseSlot[] slots = warehouse.getSlots();
        graphics.drawString(this.font,
            "  Anzahl Slots: " + slots.length,
            x + 20, contentY, COLOR_TEXT_GRAY, false);
        contentY += 12;

        graphics.drawString(this.font,
            "  Max Kapazität/Slot: " + ModConfigHandler.COMMON.WAREHOUSE_MAX_CAPACITY_PER_SLOT.get(),
            x + 20, contentY, COLOR_TEXT_GRAY, false);
        contentY += 20;

        // Berechtigungen
        graphics.drawString(this.font, "Berechtigungen:", x + 15, contentY, COLOR_TEXT, false);
        contentY += 15;

        graphics.drawString(this.font, "  ✓ Nur Admin kann bearbeiten",
            x + 20, contentY, COLOR_SUCCESS, false);
        contentY += 12;

        graphics.drawString(this.font, "  ✓ Seller können Bestand sehen",
            x + 20, contentY, COLOR_SUCCESS, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Override to disable default label rendering
        // We handle all labels in renderBg
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Plant ein GUI-Refresh nach kurzer Zeit ein, damit Server-Daten synchronisiert werden können
     */
    private void scheduleRefresh() {
        // Schedule refresh after 5 ticks to allow server sync
        new Thread(() -> {
            try {
                Thread.sleep(250); // 250ms delay for server sync
            } catch (InterruptedException e) {
                // Ignore
            }
            // Execute on main thread
            minecraft.execute(() -> {
                if (this.menu != null && this.menu.getWarehouse() != null) {
                    initTabComponents();
                }
            });
        }).start();
    }

    /**
     * Spezielle Refresh-Methode für Item-Hinzufügung mit längerer Verzögerung
     */
    private void scheduleRefreshForItemAddition() {
        new Thread(() -> {
            try {
                Thread.sleep(500); // Längere Verzögerung für Item-Addition
            } catch (InterruptedException e) {
                // Ignore
            }
            // Execute on main thread - kompletter Refresh
            minecraft.execute(() -> {
                if (this.menu != null && this.menu.getWarehouse() != null) {
                    // Deselektiere aktuellen Slot
                    selectedSlotIndex = -1;
                    // Setze Scroll-Offset zurück
                    itemScrollOffset = 0;
                    // Reinitialize components
                    init();
                }
            });
        }).start();
    }

    // ═══════════════════════════════════════════════════════════
    // ITEM SELECTION OVERLAY
    // ═══════════════════════════════════════════════════════════

    private void openItemSelection() {
        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null) return;

        // Prüfe ob mindestens ein Verkäufer verknüpft ist
        if (warehouse.getLinkedSellers().isEmpty()) {
            minecraft.player.sendSystemMessage(Component.literal(
                "§cFehler: Es muss mindestens ein Verkäufer-NPC verknüpft sein, bevor Items hinzugefügt werden können!"));
            return;
        }

        showItemSelection = true;
        itemSelectionScrollOffset = 0;
        filteredItems = new ArrayList<>(allItems);

        // Create search field
        int x = (width - 300) / 2;
        int y = (height - 400) / 2;
        itemSearchField = new EditBox(this.font, x + 10, y + 38, 280, 20, Component.translatable("gui.common.search"));
        itemSearchField.setMaxLength(50);
        itemSearchField.setValue("");
        itemSearchField.setResponder(this::filterItems);
        itemSearchField.setBordered(true);
        itemSearchField.setTextColor(0xFFFFFFFF);
        itemSearchField.setTextColorUneditable(0xFFAAAAAA);
        // Set focus on search field
        this.setFocused(itemSearchField);
    }

    private void closeItemSelection() {
        showItemSelection = false;
        itemSearchField = null;
    }

    private void filterItems(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            String search = searchText.toLowerCase();
            filteredItems = allItems.stream()
                .filter(item -> item.getDescription().getString().toLowerCase().contains(search))
                .toList();
        }
        itemSelectionScrollOffset = 0;
    }

    private void renderItemSelectionOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        // Dark overlay
        graphics.fill(0, 0, width, height, 0xAA000000);

        // Dialog box
        int dialogWidth = 300;
        int dialogHeight = 400;
        int x = (width - dialogWidth) / 2;
        int y = (height - dialogHeight) / 2;

        // Background
        graphics.fill(x, y, x + dialogWidth, y + dialogHeight, COLOR_BG);
        graphics.fill(x, y, x + dialogWidth, y + 1, COLOR_BORDER);
        graphics.fill(x, y + dialogHeight - 1, x + dialogWidth, y + dialogHeight, COLOR_BORDER);
        graphics.fill(x, y, x + 1, y + dialogHeight, COLOR_BORDER);
        graphics.fill(x + dialogWidth - 1, y, x + dialogWidth, y + dialogHeight, COLOR_BORDER);

        // Title
        graphics.drawString(this.font, "§lItem auswählen", x + 10, y + 10, COLOR_TEXT, false);

        // "Suche:" Label
        graphics.drawString(this.font, "Suche:", x + 10, y + 25, COLOR_TEXT_GRAY, false);

        // Search field
        if (itemSearchField != null) {
            itemSearchField.render(graphics, mouseX, mouseY, 0);
        }

        // Item list
        int listY = y + 55;
        int listHeight = dialogHeight - 115;
        int itemHeight = 20;
        int visibleRows = listHeight / itemHeight;

        graphics.enableScissor(x + 5, listY, x + dialogWidth - 5, listY + listHeight);

        for (int i = itemSelectionScrollOffset; i < Math.min(itemSelectionScrollOffset + visibleRows, filteredItems.size()); i++) {
            Item item = filteredItems.get(i);
            int itemY = listY + ((i - itemSelectionScrollOffset) * itemHeight);

            boolean isHovered = mouseX >= x + 10 && mouseX <= x + dialogWidth - 10 &&
                               mouseY >= itemY && mouseY <= itemY + itemHeight;

            if (isHovered) {
                graphics.fill(x + 10, itemY, x + dialogWidth - 10, itemY + itemHeight, COLOR_ACCENT);
            }

            // Render item icon
            graphics.renderItem(new ItemStack(item), x + 12, itemY + 2);

            // Render item name
            graphics.drawString(this.font, item.getDescription().getString(), x + 32, itemY + 6, COLOR_TEXT, false);
        }

        graphics.disableScissor();

        // Scroll indicators
        if (itemSelectionScrollOffset > 0) {
            graphics.drawString(this.font, "▲", x + dialogWidth - 20, listY + 5, COLOR_TEXT, false);
        }
        if (itemSelectionScrollOffset + visibleRows < filteredItems.size()) {
            graphics.drawString(this.font, "▼", x + dialogWidth - 20, listY + listHeight - 15, COLOR_TEXT, false);
        }

        // Close button
        int closeX = x + dialogWidth - 60;
        int closeY = y + dialogHeight - 35;
        boolean closeHovered = mouseX >= closeX && mouseX <= closeX + 50 && mouseY >= closeY && mouseY <= closeY + 20;
        if (closeHovered) {
            graphics.fill(closeX, closeY, closeX + 50, closeY + 20, COLOR_DANGER);
        }
        graphics.drawString(this.font, "Abbrechen", closeX + 5, closeY + 6, COLOR_TEXT, false);

        // Item count
        graphics.drawString(this.font, filteredItems.size() + " Items", x + 10, y + dialogHeight - 25, COLOR_TEXT_GRAY, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle item selection overlay clicks
        if (showItemSelection) {
            if (itemSearchField != null && itemSearchField.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }

            int dialogWidth = 300;
            int dialogHeight = 400;
            int x = (width - dialogWidth) / 2;
            int y = (height - dialogHeight) / 2;

            // Close button
            int closeX = x + dialogWidth - 60;
            int closeY = y + dialogHeight - 35;
            if (mouseX >= closeX && mouseX <= closeX + 50 && mouseY >= closeY && mouseY <= closeY + 20) {
                closeItemSelection();
                return true;
            }

            // Item list clicks
            int listY = y + 55;
            int listHeight = dialogHeight - 115;
            int itemHeight = 20;
            int visibleRows = listHeight / itemHeight;

            for (int i = itemSelectionScrollOffset; i < Math.min(itemSelectionScrollOffset + visibleRows, filteredItems.size()); i++) {
                Item item = filteredItems.get(i);
                int itemY = listY + ((i - itemSelectionScrollOffset) * itemHeight);

                if (mouseX >= x + 10 && mouseX <= x + dialogWidth - 10 &&
                    mouseY >= itemY && mouseY <= itemY + itemHeight) {
                    // Item selected!
                    sendAddItemPacket(item);
                    closeItemSelection();
                    // Force GUI refresh with longer delay for item addition
                    scheduleRefreshForItemAddition();
                    return true;
                }
            }

            return true; // Consume all clicks when overlay is open
        }

        // Only handle left clicks for normal GUI
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        // Check if we're in the SELLERS tab
        if (currentTab == Tab.SELLERS) {
            // Check all clickable NPC areas
            for (ClickableNPC clickable : clickableNPCs) {
                if (clickable.contains((int) mouseX, (int) mouseY)) {
                    if (clickable.isRemove) {
                        // Remove seller
                        sendRemoveSellerPacket(clickable.npcId);
                        minecraft.getSoundManager().play(
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F
                            )
                        );
                        // Refresh GUI after short delay to allow server sync
                        scheduleRefresh();
                    } else {
                        // Add seller
                        sendAddSellerPacket(clickable.npcId);
                        availableNpcScrollOffset = 0;
                        minecraft.getSoundManager().play(
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F
                            )
                        );
                        // Refresh GUI after short delay to allow server sync
                        scheduleRefresh();
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (showItemSelection) {
            int dialogWidth = 300;
            int dialogHeight = 400;
            int x = (width - dialogWidth) / 2;
            int y = (height - dialogHeight) / 2;
            int listY = y + 55;
            int listHeight = dialogHeight - 115;
            int itemHeight = 20;
            int visibleRows = listHeight / itemHeight;

            if (mouseX >= x && mouseX <= x + dialogWidth && mouseY >= listY && mouseY <= listY + listHeight) {
                if (delta > 0 && itemSelectionScrollOffset > 0) {
                    itemSelectionScrollOffset--;
                } else if (delta < 0 && itemSelectionScrollOffset < filteredItems.size() - visibleRows) {
                    itemSelectionScrollOffset++;
                }
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showItemSelection && itemSearchField != null) {
            if (keyCode == 256) { // ESC
                closeItemSelection();
                return true;
            }
            if (itemSearchField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (showItemSelection && itemSearchField != null) {
            if (itemSearchField.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void sendAddItemPacket(Item item) {
        String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
        WarehouseNetworkHandler.INSTANCE.sendToServer(
            new AddItemToSlotPacket(menu.getBlockPos(), itemId)
        );
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt die Shop-Entries des ersten verknüpften Verkäufer-NPCs zurück
     */
    @Nullable
    private List<NPCData.ShopEntry> getLinkedNPCShopItems() {
        WarehouseBlockEntity warehouse = menu.getWarehouse();
        if (warehouse == null || minecraft.level == null) return null;

        List<UUID> sellers = warehouse.getLinkedSellers();
        if (sellers.isEmpty()) return null;

        // Hole ersten Verkäufer-NPC
        UUID firstSeller = sellers.get(0);
        for (var entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof CustomNPCEntity npc) {
                if (npc.getUUID().equals(firstSeller)) {
                    return npc.getNpcData().getBuyShop().getEntries();
                }
            }
        }

        return null;
    }

    /**
     * Versucht den NPC-Namen aus einer UUID abzurufen
     */
    private String getNPCName(UUID npcUUID) {
        if (minecraft.level == null) return "Unknown";

        // Versuche zuerst aus dem Cache
        String cachedName = ClientWarehouseNPCCache.getNPCName(npcUUID);
        if (cachedName != null) {
            return cachedName;
        }

        // Suche alle Custom NPCs in der Welt und cache sie
        for (var entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof CustomNPCEntity npc) {
                // Cache alle NPCs die wir finden
                ClientWarehouseNPCCache.putNPC(npc.getUUID(), npc.getNpcName());

                if (npc.getNpcData().getNpcUUID().equals(npcUUID)) {
                    return npc.getNpcName();
                }
            }
        }

        // Fallback: Zeige gekürzte UUID
        return npcUUID.toString().substring(0, 8) + "...";
    }
}
