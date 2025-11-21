package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.data.NPCData;
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
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/merchant_shop.png");

    private List<ShopItemRow> shopItemRows;
    private int scrollOffset = 0;
    private static final int VISIBLE_ROWS = 6; // Wie viele Items gleichzeitig sichtbar sind

    public MerchantShopScreen(MerchantShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 320; // Breitere GUI für alle Spalten
        this.imageHeight = 180; // Höhe ohne Inventar
        this.shopItemRows = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Lade Shop-Items
        loadShopItems();

        // Erstelle Input-Felder und Buttons für sichtbare Rows
        // Layout: Icon(18) | Name(80) | Preis(50) | Verfügbar(45) | Input(40) | Gesamt(40) | Button(45)
        for (int i = 0; i < Math.min(VISIBLE_ROWS, shopItemRows.size()); i++) {
            int rowIndex = i;
            ShopItemRow row = shopItemRows.get(i);

            // Menge Eingabe-Feld
            EditBox quantityInput = new EditBox(this.font, x + 200, y + 30 + i * 22, 35, 16, Component.literal("Menge"));
            quantityInput.setMaxLength(4);
            quantityInput.setValue("1");
            quantityInput.setFilter(s -> s.matches("\\d*")); // Nur Zahlen
            row.quantityInput = quantityInput;
            addRenderableWidget(quantityInput);

            // Kaufen Button
            Button buyButton = addRenderableWidget(Button.builder(Component.literal("Kaufen"), button -> {
                purchaseItem(rowIndex + scrollOffset);
            }).bounds(x + 270, y + 29, 45, 18).build());
            row.buyButton = buyButton;
        }

        // Scroll Buttons (falls mehr als VISIBLE_ROWS vorhanden)
        if (shopItemRows.size() > VISIBLE_ROWS) {
            addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                scrollUp();
            }).bounds(x + imageWidth - 15, y + 25, 12, 12).build());

            addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                scrollDown();
            }).bounds(x + imageWidth - 15, y + 150, 12, 12).build());
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
            row.availableQuantity = entry.getItem().getCount(); // Stack-Größe = verfügbare Menge
            shopItemRows.add(row);
        }

        ScheduleMC.LOGGER.info("Shop geladen: {} Items für Kategorie {}", shopItemRows.size(), menu.getCategory());
    }

    /**
     * Kauft ein Item
     */
    private void purchaseItem(int shopIndex) {
        if (shopIndex >= 0 && shopIndex < shopItemRows.size()) {
            ShopItemRow row = shopItemRows.get(shopIndex - scrollOffset);
            if (row.quantityInput != null) {
                String quantityStr = row.quantityInput.getValue();
                if (!quantityStr.isEmpty()) {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity > 0 && quantity <= row.availableQuantity) {
                        // Sende Kauf-Packet an Server
                        NPCNetworkHandler.sendToServer(new PurchaseItemPacket(
                            menu.getEntityId(),
                            shopIndex,
                            quantity
                        ));
                    }
                }
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

    private void updateVisibleRows() {
        // Update row data nach Scroll
        for (int i = 0; i < VISIBLE_ROWS && (i + scrollOffset) < shopItemRows.size(); i++) {
            ShopItemRow row = shopItemRows.get(i + scrollOffset);
            // Row wird beim Rendern aktualisiert
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render Spaltenüberschriften
        guiGraphics.drawString(this.font, "Item", x + 25, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "Preis", x + 110, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "Lager", x + 155, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "Menge", x + 200, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "Gesamt", x + 240, y + 18, 0x404040, false);

        // Render Shop Items
        for (int i = 0; i < Math.min(VISIBLE_ROWS, shopItemRows.size() - scrollOffset); i++) {
            ShopItemRow row = shopItemRows.get(i + scrollOffset);
            renderShopItem(guiGraphics, row, x + 8, y + 30 + i * 22, i);
        }
    }

    /**
     * Rendert eine Shop-Item Row
     * Schema: [Icon] Name | Preis | Verfügbar | [Input] | Gesamt | [Button]
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

        // Verfügbare Menge im Lager
        guiGraphics.drawString(this.font, "" + row.availableQuantity, x + 155, y + 4, 0xAAAAAA, false);

        // Gesamtpreis berechnen und anzeigen
        int quantity = 1;
        if (row.quantityInput != null && !row.quantityInput.getValue().isEmpty()) {
            try {
                quantity = Integer.parseInt(row.quantityInput.getValue());
            } catch (NumberFormatException e) {
                quantity = 1;
            }
        }
        int totalPrice = row.pricePerItem * quantity;
        guiGraphics.drawString(this.font, totalPrice + "$", x + 232, y + 4, 0xFFFF55, false);
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
        String categoryName = "Shop: " + menu.getCategory().getDisplayName();
        guiGraphics.drawString(this.font, categoryName, 8, 6, 0x404040, false);
    }

    /**
     * Interne Klasse für Shop-Item Row
     */
    private static class ShopItemRow {
        ItemStack item;
        int pricePerItem;
        int availableQuantity;
        EditBox quantityInput;
        Button buyButton;
    }
}
