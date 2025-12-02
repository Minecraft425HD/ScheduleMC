package de.rolandsw.schedulemc.warehouse.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.economy.ShopAccountManager;
import de.rolandsw.schedulemc.economy.ShopAccount;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import de.rolandsw.schedulemc.warehouse.menu.WarehouseMenu;
import de.rolandsw.schedulemc.warehouse.network.WarehouseNetworkHandler;
import de.rolandsw.schedulemc.warehouse.network.packet.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
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

    // Stats Tab - Scrolling
    private int statsScrollOffset = 0;
    private static final int STATS_CONTENT_HEIGHT = 400; // Geschätzte Höhe des gesamten Inhalts
    private static final int STATS_VISIBLE_HEIGHT = 180; // Sichtbare Höhe im Tab

    // Input fields
    private EditBox slotCapacityInput;
    private EditBox deliveryIntervalInput;
    private EditBox shopIdInput;

    public WarehouseScreen(WarehouseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
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
            addRenderableWidget(Button.builder(Component.literal("Leeren"), button -> {
                sendClearSlotPacket(selectedSlotIndex);
                selectedSlotIndex = -1;
                initTabComponents();
            }).bounds(detailX, detailY + 55, 105, 20).build());

            // Auffüllen Button
            addRenderableWidget(Button.builder(Component.literal("Auffüllen"), button -> {
                int restockAmount = selectedSlot.getRestockAmount();
                if (restockAmount > 0) {
                    sendModifySlotPacket(selectedSlotIndex, restockAmount);
                }
            }).bounds(detailX, detailY + 80, 105, 20).build());
        }

        // Neuer Slot Button
        addRenderableWidget(Button.builder(Component.literal("+ Neuer Slot"), button -> {
            // TODO: Öffne Item-Auswahl Dialog
            minecraft.player.sendSystemMessage(Component.literal("§eItem-Auswahl noch nicht implementiert"));
        }).bounds(x + 10, y + 210, 100, 20).build());

        // Auto-Fill Button
        addRenderableWidget(Button.builder(Component.literal("Auto-Fill"), button -> {
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

        // Scroll Buttons
        if (sellers.size() > SELLER_VISIBLE_ROWS) {
            addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                if (sellerScrollOffset > 0) {
                    sellerScrollOffset--;
                    initTabComponents();
                }
            }).bounds(x + imageWidth - 25, y + 35, 15, 15).build());

            addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                if (sellerScrollOffset < sellers.size() - SELLER_VISIBLE_ROWS) {
                    sellerScrollOffset++;
                    initTabComponents();
                }
            }).bounds(x + imageWidth - 25, y + 165, 15, 15).build());
        }

        // Add Seller Button
        addRenderableWidget(Button.builder(Component.literal("+ Seller hinzufügen"), button -> {
            minecraft.player.sendSystemMessage(Component.literal("§eUse /warehouse addseller <player>"));
        }).bounds(x + 10, y + 210, 150, 20).build());
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
        shopIdInput = new EditBox(this.font, x + 120, y + 50, 150, 20, Component.literal("Shop ID"));
        shopIdInput.setValue(warehouse.getShopId() != null ? warehouse.getShopId() : "");
        shopIdInput.setMaxLength(50);
        addRenderableWidget(shopIdInput);

        // Save Button
        addRenderableWidget(Button.builder(Component.literal("Speichern"), button -> {
            sendUpdateSettingsPacket();
        }).bounds(x + 280, y + 210, 100, 20).build());

        // Reset Button
        addRenderableWidget(Button.builder(Component.literal("Zurücksetzen"), button -> {
            initTabComponents();
        }).bounds(x + 170, y + 210, 100, 20).build());
    }

    private void sendUpdateSettingsPacket() {
        String shopId = shopIdInput.getValue().trim();
        if (shopId.isEmpty()) shopId = null;

        WarehouseNetworkHandler.INSTANCE.sendToServer(
            new UpdateSettingsPacket(menu.getBlockPos(), shopId)
        );

        minecraft.player.sendSystemMessage(Component.literal("§aEinstellungen gespeichert!"));
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

            // Stock Info
            String stockInfo = slot.getStock() + " / " + slot.getMaxCapacity();
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

        graphics.drawString(this.font, "§lVERKNÜPFTE VERKÄUFER", x + 10, y + 35, COLOR_TEXT, false);

        List<UUID> sellers = warehouse.getLinkedSellers();
        int renderY = y + 50;
        int visibleCount = 0;

        for (int i = sellerScrollOffset; i < sellers.size() && visibleCount < SELLER_VISIBLE_ROWS; i++) {
            UUID sellerId = sellers.get(i);

            // Get NPC name
            String npcName = getNPCName(sellerId);

            graphics.drawString(this.font, "✓ " + npcName, x + 15, renderY, COLOR_SUCCESS, false);

            // Zeige UUID nur wenn Name nicht gefunden
            if (npcName.contains("...")) {
                graphics.drawString(this.font, "(UUID: " + npcName + ")",
                    x + 150, renderY, COLOR_TEXT_GRAY, false);
            }

            // Remove button area
            int removeX = x + 350;
            int removeY = renderY - 2;
            if (mouseX >= removeX && mouseX <= removeX + 40 &&
                mouseY >= removeY && mouseY <= removeY + 12) {
                graphics.fill(removeX, removeY, removeX + 40, removeY + 12, COLOR_DANGER);
                graphics.drawString(this.font, "[X]", removeX + 12, renderY, COLOR_TEXT, false);

                if (minecraft.mouseHandler.isLeftPressed()) {
                    sendRemoveSellerPacket(sellerId);
                }
            } else {
                graphics.drawString(this.font, "[X]", removeX + 12, renderY, COLOR_DANGER, false);
            }

            renderY += 18;
            visibleCount++;
        }

        if (sellers.isEmpty()) {
            graphics.drawString(this.font, "Keine Verkäufer verknüpft",
                x + 15, y + 50, COLOR_TEXT_GRAY, false);
            int helpY = y + 70;

            // Anleitung zum Verknüpfen
            graphics.drawString(this.font, "§7So verknüpfen Sie einen NPC-Verkäufer:",
                x + 15, helpY, COLOR_TEXT_GRAY, false);
            helpY += 12;

            graphics.drawString(this.font, "§71. Nimm das §eWarehouse Tool §7aus dem Creative-Inventar",
                x + 15, helpY, COLOR_TEXT_GRAY, false);
            helpY += 12;

            graphics.drawString(this.font, "§72. §eRechtsklick §7auf diesen Warehouse-Block",
                x + 15, helpY, COLOR_TEXT_GRAY, false);
            helpY += 12;

            graphics.drawString(this.font, "§73. §eLinksklick §7auf den NPC den Sie verknüpfen möchten",
                x + 15, helpY, COLOR_TEXT_GRAY, false);
            helpY += 12;

            graphics.drawString(this.font, "§74. Der NPC verkauft dann Items aus diesem Warehouse",
                x + 15, helpY, COLOR_TEXT_GRAY, false);
        }

        // Bottom info
        graphics.drawString(this.font, "Verkäufer: " + sellers.size(),
            x + 300, y + 215, COLOR_TEXT_GRAY, false);
    }

    private void sendRemoveSellerPacket(UUID sellerId) {
        WarehouseNetworkHandler.INSTANCE.sendToServer(
            new RemoveSellerPacket(menu.getBlockPos(), sellerId)
        );
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
        long intervalDays = de.rolandsw.schedulemc.warehouse.WarehouseConfig.DELIVERY_INTERVAL_DAYS.get();
        long daysUntilNext = (lastDeliveryDay + intervalDays) - currentDay;

        graphics.drawString(this.font,
            "Nächste Lieferung: in " + Math.max(0, daysUntilNext) + " Tagen",
            x + 15, contentY, COLOR_TEXT, false);
        contentY += 12;

        graphics.drawString(this.font,
            "Interval: alle " + de.rolandsw.schedulemc.warehouse.WarehouseConfig.DELIVERY_INTERVAL_DAYS.get() + " Tage",
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
            "  Interval: " + de.rolandsw.schedulemc.warehouse.WarehouseConfig.DELIVERY_INTERVAL_DAYS.get() + " Tage",
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
            "  Max Kapazität/Slot: " + de.rolandsw.schedulemc.warehouse.WarehouseConfig.MAX_CAPACITY_PER_SLOT.get(),
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

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Versucht den NPC-Namen aus einer UUID abzurufen
     */
    private String getNPCName(UUID npcUUID) {
        if (minecraft.level == null) return "Unknown";

        // Suche alle Custom NPCs in der Welt
        for (var entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof CustomNPCEntity npc) {
                if (npc.getNpcData().getNpcUUID().equals(npcUUID)) {
                    return npc.getNpcName();
                }
            }
        }

        // Fallback: Zeige gekürzte UUID
        return npcUUID.toString().substring(0, 8) + "...";
    }
}
