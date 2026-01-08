package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.menu.MerchantShopMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.PurchaseItemPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI für Verkäufer Shop
 * Schema: [Item Icon] | Name | Preis/Stück | Verfügbar | [Eingabe] | Gesamtpreis | [Kaufen]
 */
@OnlyIn(Dist.CLIENT)
public class MerchantShopScreen extends AbstractContainerScreen<MerchantShopMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/gui/merchant_shop.png");

    private List<ShopItemRow> shopItemRows;
    private int scrollOffset = 0;
    private static final int VISIBLE_ROWS = 6; // Wie viele Items gleichzeitig sichtbar sind
    private Button buyButton; // Einziger Kaufen-Button unten rechts

    public MerchantShopScreen(MerchantShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 320; // Breitere GUI für alle Spalten
        this.imageHeight = 200; // Höhe ohne Inventar + Platz für Kostenaufstellung
        this.shopItemRows = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Lade Shop-Items
        loadShopItems();

        // Erstelle Input-Felder für sichtbare Rows
        createInputFields();

        // Scroll Buttons (falls mehr als VISIBLE_ROWS vorhanden)
        if (shopItemRows.size() > VISIBLE_ROWS) {
            addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                scrollUp();
            }).bounds(x + imageWidth - 15, y + 25, 12, 12).build());

            addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                scrollDown();
            }).bounds(x + imageWidth - 15, y + 150, 12, 12).build());
        }

        // Großer Kaufen-Button unten rechts
        buyButton = addRenderableWidget(Button.builder(Component.translatable("gui.common.buy"), button -> {
            purchaseAllItems();
        }).bounds(x + imageWidth - 70, y + imageHeight - 25, 60, 20).build());
    }

    /**
     * Erstellt die Input-Felder für die aktuell sichtbaren Rows
     */
    private void createInputFields() {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Erstelle Input-Felder für sichtbare Rows
        // Layout: Icon(18) | Name(80) | Preis(50) | Verfügbar(45) | Input(40)
        for (int i = 0; i < Math.min(VISIBLE_ROWS, shopItemRows.size() - scrollOffset); i++) {
            int rowIndex = i + scrollOffset;
            ShopItemRow row = shopItemRows.get(rowIndex);

            // Menge Eingabe-Feld
            EditBox quantityInput = new EditBox(this.font, x + 230, y + 30 + i * 22, 35, 16, Component.translatable("screen.merchant_shop.quantity"));
            quantityInput.setMaxLength(4);

            // SPEZIALBEHANDLUNG: Rechnungs-Items immer auf "1" fixieren
            boolean isBillItem = row.item.hasTag() && row.item.getTag().contains("BillType");
            if (isBillItem) {
                quantityInput.setValue("1");
                quantityInput.setEditable(false); // Nicht editierbar
                quantityInput.setTextColor(0xAAAAAA); // Grau = disabled
                row.savedQuantity = "1"; // Fixiere auf 1
            } else {
                quantityInput.setValue(row.savedQuantity);
                quantityInput.setFilter(s -> s.matches("\\d*")); // Nur Zahlen

                // Speichere Werte beim Tippen (nur für normale Items)
                final int finalRowIndex = rowIndex;
                quantityInput.setResponder(value -> {
                    shopItemRows.get(finalRowIndex).savedQuantity = value;
                });
            }

            row.quantityInput = quantityInput;
            addRenderableWidget(quantityInput);
        }
    }

    /**
     * Lädt die Shop-Items aus dem NPC
     */
    private void loadShopItems() {
        shopItemRows.clear();
        List<NPCData.ShopEntry> items = menu.getShopItems();

        for (NPCData.ShopEntry entry : items) {
            ShopItemRow row = new ShopItemRow();
            row.item = entry.getItem().copy(); // Kopie erstellen
            row.pricePerItem = entry.getPrice();
            row.unlimited = entry.isUnlimited();
            row.availableQuantity = entry.isUnlimited() ? Integer.MAX_VALUE : entry.getStock();

            // SPEZIALBEHANDLUNG: Rechnungs-Items automatisch auf "1" setzen
            boolean isBillItem = row.item.hasTag() && row.item.getTag().contains("BillType");
            if (isBillItem) {
                row.savedQuantity = "1"; // Rechnungen immer Menge 1
            }

            shopItemRows.add(row);
        }

        ScheduleMC.LOGGER.info("Shop geladen: {} Items für Kategorie {}", shopItemRows.size(), menu.getCategory());
    }

    /**
     * Kauft alle Items mit Menge > 0
     */
    private void purchaseAllItems() {
        // Speichere aktuelle Eingaben vor dem Kauf
        saveCurrentInputValues();

        // Sammle alle Items mit Menge > 0
        for (int i = 0; i < shopItemRows.size(); i++) {
            ShopItemRow row = shopItemRows.get(i);
            String quantityStr = row.savedQuantity;
            if (!quantityStr.isEmpty() && !quantityStr.equals("0")) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity > 0 && quantity <= row.availableQuantity) {
                        // Sende Kauf-Packet an Server für dieses Item
                        NPCNetworkHandler.sendToServer(new PurchaseItemPacket(
                            menu.getEntityId(),
                            i,
                            quantity
                        ));
                    }
                } catch (NumberFormatException e) {
                    // Ignoriere ungültige Eingaben
                }
            }
        }
        // Schließe GUI nach Kauf
        this.onClose();
    }

    /**
     * Speichert die aktuellen Input-Werte in savedQuantity
     */
    private void saveCurrentInputValues() {
        for (ShopItemRow row : shopItemRows) {
            if (row.quantityInput != null) {
                row.savedQuantity = row.quantityInput.getValue();
            }
        }
    }

    private void scrollUp() {
        if (scrollOffset > 0) {
            scrollOffset--;
            updateVisibleRows();
        }
    }

    private void scrollDown() {
        if (scrollOffset < shopItemRows.size() - VISIBLE_ROWS) {
            scrollOffset++;
            updateVisibleRows();
        }
    }

    /**
     * Aktualisiert die sichtbaren Rows nach dem Scrollen
     * Entfernt alte Input-Felder und erstellt neue für die aktuellen Items
     */
    private void updateVisibleRows() {
        // Speichere die aktuellen Input-Werte (passiert automatisch via setResponder)

        // Entferne alle alten Input-Felder
        for (ShopItemRow row : shopItemRows) {
            if (row.quantityInput != null) {
                this.removeWidget(row.quantityInput);
                row.quantityInput = null;
            }
        }

        // Erstelle neue Input-Felder für die aktuell sichtbaren Items
        createInputFields();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render Spaltenüberschriften
        guiGraphics.drawString(this.font, Component.translatable("screen.merchant_shop.column_item").getString(), x + 25, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.merchant_shop.column_price").getString(), x + 110, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.merchant_shop.column_stock").getString(), x + 155, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.merchant_shop.column_quantity").getString(), x + 230, y + 18, 0x404040, false);

        // Render Shop Items
        for (int i = 0; i < Math.min(VISIBLE_ROWS, shopItemRows.size() - scrollOffset); i++) {
            ShopItemRow row = shopItemRows.get(i + scrollOffset);
            renderShopItem(guiGraphics, row, x + 8, y + 30 + i * 22, i);
        }

        // Render Kostenaufstellung unten
        int totalCost = calculateTotalCost();
        guiGraphics.drawString(this.font, Component.translatable("screen.merchant_shop.total_cost").getString(), x + 10, y + imageHeight - 22, 0x404040, false);
        guiGraphics.drawString(this.font, totalCost + "$", x + 90, y + imageHeight - 22, totalCost > 0 ? 0xFFFF55 : 0x888888, false);
    }

    /**
     * Rendert eine Shop-Item Row
     * Schema: [Icon] Name | Preis | Verfügbar | [Input]
     */
    private void renderShopItem(GuiGraphics guiGraphics, ShopItemRow row, int x, int y, int rowIndex) {
        // Item Icon (16x16)
        guiGraphics.renderItem(row.item, x, y);

        // Item Name (gekürzt wenn zu lang)
        String itemName = row.item.getHoverName().getString();
        if (itemName.length() > 12) {
            itemName = itemName.substring(0, 12) + "..";
        }
        guiGraphics.drawString(this.font, itemName, x + 18, y + 4, 0xFFFFFF, false);

        // Preis pro Item
        guiGraphics.drawString(this.font, row.pricePerItem + "$", x + 102, y + 4, 0x55FF55, false);

        // Verfügbare Menge im Lager (∞ für unlimited, Zahl für limited)
        String stockDisplay = row.unlimited ? "∞" : String.valueOf(row.availableQuantity);
        guiGraphics.drawString(this.font, stockDisplay, x + 155, y + 4, row.unlimited ? 0x55FFFF : 0xAAAAAA, false);
    }

    /**
     * Berechnet die Gesamtkosten aller eingegebenen Mengen
     */
    private int calculateTotalCost() {
        // Speichere zuerst die aktuellen Eingaben
        saveCurrentInputValues();

        int totalCost = 0;
        for (ShopItemRow row : shopItemRows) {
            if (!row.savedQuantity.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(row.savedQuantity);
                    if (quantity > 0) {
                        totalCost += row.pricePerItem * quantity;
                    }
                } catch (NumberFormatException e) {
                    // Ignoriere ungültige Eingaben
                }
            }
        }
        return totalCost;
    }    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }



    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String categoryName = Component.translatable("screen.merchant_shop.title_prefix").getString() + menu.getCategory().getDisplayName();
        guiGraphics.drawString(this.font, categoryName, 8, 6, 0x404040, false);
    }

    /**
     * Interne Klasse für Shop-Item Row
     */
    private static class ShopItemRow {
        ItemStack item;
        int pricePerItem;
        boolean unlimited; // True = unbegrenzte Menge, False = begrenzter Lagerbestand
        int availableQuantity;
        EditBox quantityInput;
        String savedQuantity = "0"; // Gespeicherte Eingabe (persistent beim Scrollen)
    }
}
