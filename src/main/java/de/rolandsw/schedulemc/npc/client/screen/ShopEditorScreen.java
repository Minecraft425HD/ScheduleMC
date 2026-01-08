package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.menu.ShopEditorMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.UpdateShopItemsPacket;
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
 * GUI für Shop-Editor (Admin-Only)
 * Professionelles Design mit 4x4 Item-Grid und Preis/Stock-Tabelle
 */
@OnlyIn(Dist.CLIENT)
public class ShopEditorScreen extends AbstractContainerScreen<ShopEditorMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    private static class ItemRow {
        EditBox priceInput;
        Button unlimitedToggle;
        EditBox stockInput;
    }

    private List<ItemRow> itemRows;
    private Button saveButton;
    private int scrollOffset = 0;
    private static final int VISIBLE_ROWS = 4; // Zeige 4 Zeilen gleichzeitig (keine Überlappung)

    public ShopEditorScreen(ShopEditorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 320; // Breiter für alle Felder
        this.imageHeight = 188; // Kompakte Höhe für 4 Item-Zeilen + Hotbar
        this.itemRows = new ArrayList<>();
        this.inventoryLabelY = 10000; // Verstecke Inventar-Label
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Erstelle Eingabefelder (scrollbar für alle 16 Items)
        createInputFields();

        // Scroll-Buttons (falls mehr als VISIBLE_ROWS Items)
        if (ShopEditorMenu.SHOP_SLOTS > VISIBLE_ROWS) {
            addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                scrollUp();
            }).bounds(x + 290, y + 28, 12, 12).build());

            addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                scrollDown();
            }).bounds(x + 290, y + 120, 12, 12).build());
        }

        // Speichern-Button (groß und deutlich)
        saveButton = addRenderableWidget(Button.builder(
            Component.literal("Shop Speichern"),
            button -> saveShopItems()
        ).bounds(x + 8, y + imageHeight - 26, 304, 20).build());
    }

    /**
     * Erstellt die Eingabefelder für Preis, Unlimited und Stock
     */
    private void createInputFields() {
        // Entferne alte Felder
        for (ItemRow row : itemRows) {
            if (row.priceInput != null) this.removeWidget(row.priceInput);
            if (row.unlimitedToggle != null) this.removeWidget(row.unlimitedToggle);
            if (row.stockInput != null) this.removeWidget(row.stockInput);
        }
        itemRows.clear();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Erstelle sichtbare Eingabefelder
        for (int i = 0; i < Math.min(VISIBLE_ROWS, ShopEditorMenu.SHOP_SLOTS - scrollOffset); i++) {
            int slotIndex = i + scrollOffset;
            ItemRow row = new ItemRow();

            int rowY = y + 30 + i * 28; // 28 Pixel Abstand = kompakte Darstellung (nur 4 Zeilen sichtbar)

            // Preis-Eingabefeld
            row.priceInput = new EditBox(this.font,
                x + 180, rowY, 40, 16,
                Component.translatable("gui.common.price"));
            row.priceInput.setMaxLength(6);
            row.priceInput.setValue(String.valueOf(menu.getItemPrices()[slotIndex]));
            row.priceInput.setFilter(s -> s.matches("\\d*")); // Nur Zahlen

            final int finalSlotIndex = slotIndex;
            row.priceInput.setResponder(value -> {
                try {
                    int price = value.isEmpty() ? 0 : Integer.parseInt(value);
                    menu.setItemPrice(finalSlotIndex, price);
                } catch (NumberFormatException e) {
                    // Ignoriere ungültige Eingaben
                }
            });

            // Unlimited-Toggle Button (zeigt ∞ oder ✓)
            boolean initialUnlimited = menu.getItemUnlimited()[slotIndex];
            row.unlimitedToggle = Button.builder(
                Component.literal(initialUnlimited ? "∞" : "✓"),
                button -> {
                    // Toggle unlimited status
                    boolean currentUnlimited = menu.getItemUnlimited()[finalSlotIndex];
                    boolean newUnlimited = !currentUnlimited;
                    menu.setItemUnlimited(finalSlotIndex, newUnlimited);

                    // Update Button-Text
                    button.setMessage(Component.literal(newUnlimited ? "∞" : "✓"));

                    // Aktiviere/Deaktiviere Stock-Feld basierend auf Unlimited
                    row.stockInput.setEditable(!newUnlimited);
                    if (newUnlimited) {
                        row.stockInput.setTextColor(0x808080); // Grau wenn deaktiviert
                    } else {
                        row.stockInput.setTextColor(0xE0E0E0); // Weiß wenn aktiv
                    }
                }
            ).bounds(x + 226, rowY, 16, 16).build();

            // Stock-Eingabefeld
            row.stockInput = new EditBox(this.font,
                x + 248, rowY, 40, 16,
                Component.translatable("gui.warehouse.storage"));
            row.stockInput.setMaxLength(6);
            row.stockInput.setValue(String.valueOf(menu.getItemStock()[slotIndex]));
            row.stockInput.setFilter(s -> s.matches("\\d*")); // Nur Zahlen

            // Deaktiviere Stock-Feld wenn Unlimited
            boolean unlimited = menu.getItemUnlimited()[slotIndex];
            row.stockInput.setEditable(!unlimited);
            row.stockInput.setTextColor(unlimited ? 0x808080 : 0xE0E0E0);

            row.stockInput.setResponder(value -> {
                try {
                    int stock = value.isEmpty() ? 0 : Integer.parseInt(value);
                    menu.setItemStock(finalSlotIndex, stock);
                } catch (NumberFormatException e) {
                    // Ignoriere ungültige Eingaben
                }
            });

            itemRows.add(row);
            addRenderableWidget(row.priceInput);
            addRenderableWidget(row.unlimitedToggle);
            addRenderableWidget(row.stockInput);
        }
    }

    private void scrollUp() {
        if (scrollOffset > 0) {
            scrollOffset--;
            createInputFields();
        }
    }

    private void scrollDown() {
        if (scrollOffset < ShopEditorMenu.SHOP_SLOTS - VISIBLE_ROWS) {
            scrollOffset++;
            createInputFields();
        }
    }

    /**
     * Speichert die Shop-Items zum Server
     */
    private void saveShopItems() {
        List<ItemStack> items = new ArrayList<>();
        List<Integer> prices = new ArrayList<>();
        List<Boolean> unlimited = new ArrayList<>();
        List<Integer> stock = new ArrayList<>();

        // Sammle Items, Preise, Unlimited und Stock
        for (int i = 0; i < ShopEditorMenu.SHOP_SLOTS; i++) {
            ItemStack item = menu.getShopContainer().getItem(i);
            if (!item.isEmpty()) {
                items.add(item.copy());
                prices.add(menu.getItemPrices()[i]);
                unlimited.add(menu.getItemUnlimited()[i]);
                stock.add(menu.getItemStock()[i]);
            }
        }

        // Sende Packet an Server
        NPCNetworkHandler.sendToServer(new UpdateShopItemsPacket(
            menu.getEntityId(),
            items,
            prices,
            unlimited,
            stock
        ));

        // Schließe GUI
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render Tabellen-Header
        guiGraphics.drawString(this.font, "#", x + 92, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "Item", x + 110, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "Preis", x + 180, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "∞", x + 228, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "Lager", x + 250, y + 18, 0x404040, false);

        // Render Tabellen-Einträge
        for (int i = 0; i < Math.min(VISIBLE_ROWS, ShopEditorMenu.SHOP_SLOTS - scrollOffset); i++) {
            int slotIndex = i + scrollOffset;
            ItemStack item = menu.getShopContainer().getItem(slotIndex);

            int rowY = y + 30 + i * 28; // 28 Pixel Abstand = kompakte Darstellung

            // Slot-Nummer
            guiGraphics.drawString(this.font, "#" + (slotIndex + 1), x + 92, rowY + 4, 0x888888, false);

            // Item-Name (wenn vorhanden)
            if (!item.isEmpty()) {
                String itemName = item.getHoverName().getString();
                if (itemName.length() > 11) {
                    itemName = itemName.substring(0, 11) + "..";
                }
                guiGraphics.drawString(this.font, itemName, x + 110, rowY + 4, 0xFFFFFF, false);
            } else {
                guiGraphics.drawString(this.font, "-leer-", x + 110, rowY + 4, 0x666666, false);
            }
        }

        // Hotbar-Label
        guiGraphics.drawString(this.font, "Schnellauswahl",
            x + 92, y + 144, 0x404040, false);

        // Hinweistext unten (dunkle Farbe für gute Lesbarkeit auf hellgrauem Hintergrund)
        guiGraphics.drawString(this.font, "Items aus Hotbar ins 4x4 Grid ziehen, dann konfigurieren",
            x + 10, y + imageHeight - 38, 0x404040, false);
        guiGraphics.drawString(this.font, "Preis: $-Betrag | ∞: Unbegrenzt | Lager: Stückzahl",
            x + 10, y + imageHeight - 28, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Haupthintergrund
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF8B8B8B);

        // Item-Grid Hintergrund (4x4)
        guiGraphics.fill(x + 6, y + 16, x + 80, y + 90, 0xFF373737);

        // Tabelle Hintergrund (4 Zeilen mit 28px Abstand - kompakt)
        guiGraphics.fill(x + 88, y + 14, x + 304, y + 142, 0xFF373737);

        // Hotbar Hintergrund (9 Slots) - rechts neben Item-Grid
        guiGraphics.fill(x + 88, y + 150, x + 250, y + 176, 0xFF373737);

        // Rahmen
        guiGraphics.renderOutline(x, y, imageWidth, imageHeight, 0xFF000000);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String title = "§6§lShop Editor";
        guiGraphics.drawString(this.font, title, 8, 6, 0xFFD700, false);

        String category = "§7" + menu.getCategory().getDisplayName();
        guiGraphics.drawString(this.font, category, imageWidth - font.width(category) - 8, 6, 0xAAAAAA, false);

        // Überschrift für Shop Items
        guiGraphics.drawString(this.font, "Shop Items (4x4)", 8, 94, 0xFFFFFF, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Verhindere, dass E-Taste das GUI schließt
        if (keyCode == 69) { // 69 = E-Taste
            return true; // Event konsumieren ohne etwas zu tun
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
