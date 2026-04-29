package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.level.client.ClientProducerLevelCache;
import de.rolandsw.schedulemc.npc.data.ShopEntry;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.menu.MerchantShopMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.PurchaseItemPacket;
import de.rolandsw.schedulemc.util.MoneyFormat;
import de.rolandsw.schedulemc.vehicle.items.ItemSpawnVehicle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Vollständig neu gestaltetes Merchant Shop GUI.
 *
 * Moderne Dark-Theme UI mit:
 * - Mausrad-Scrolling
 * - Hover-Highlight pro Zeile
 * - Scroll-Indikator
 * - Out-of-Stock-Markierung in Rot
 * - Gesamtkosten-Anzeige
 */
@OnlyIn(Dist.CLIENT)
public class MerchantShopScreen extends AbstractContainerScreen<MerchantShopMenu> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d*");

    // ─── GUI Dimensionen ─────────────────────────────────────────────
    private static final int GUI_W = 360;
    private static final int GUI_H = 230;
    private static final int VISIBLE_ROWS = 7;
    private static final int ROW_H = 22;

    // Spalten-X-Positionen (relativ zum GUI-Ursprung)
    private static final int COL_ICON   = 8;
    private static final int COL_NAME   = 28;
    private static final int COL_PRICE  = 155;
    private static final int COL_STOCK  = 210;
    private static final int COL_INPUT  = 260;
    private static final int COL_TOTAL  = 310;

    // Header Y
    private static final int HEADER_Y   = 22;
    private static final int LIST_Y     = 35;
    private static final int FOOTER_Y_OFFSET = 25; // von unten

    // ─── Dark Theme Farben ────────────────────────────────────────────
    private static final int C_BG           = 0xFF1E1E1E;
    private static final int C_BG_HEADER    = 0xFF2A2A2A;
    private static final int C_BG_ROW_ODD   = 0xFF252525;
    private static final int C_BG_ROW_EVEN  = 0xFF2B2B2B;
    private static final int C_BG_HOVER     = 0xFF333D4A;
    private static final int C_BG_OOS       = 0xFF3A1A1A; // out of stock
    private static final int C_BG_FOOTER    = 0xFF212121;
    private static final int C_BORDER       = 0xFF444444;
    private static final int C_TEXT         = 0xFFFFFFFF;
    private static final int C_TEXT_GRAY    = 0xFFAAAAAA;
    private static final int C_TEXT_DARK    = 0xFF777777;
    private static final int C_PRICE        = 0xFF55FF55;
    private static final int C_STOCK        = 0xFFFFAA00;
    private static final int C_STOCK_OOS    = 0xFFFF5555;
    private static final int C_TOTAL_POS    = 0xFFFFFF55;
    private static final int C_ACCENT       = 0xFF4A90E2;
    private static final int C_SCROLL_BAR   = 0xFF4A90E2;
    private static final int C_SCROLL_BG    = 0xFF333333;
    private static final int C_BTN_BUY      = 0xFF2E7D32;
    private static final int C_BTN_BUY_HOV  = 0xFF388E3C;
    private static final int C_BTN_BUY_DIS  = 0xFF333333;

    // ─── State ────────────────────────────────────────────────────────
    private final List<ShopItemRow> shopItemRows = new ArrayList<>();
    private int scrollOffset = 0;

    // Gecachte Strings
    private String cachedColItem;
    private String cachedColPrice;
    private String cachedColStock;
    private String cachedColQty;
    private String cachedColTotal;
    private String cachedTotalLabel;

    private Button buyButton;

    // ─── Konstruktor ──────────────────────────────────────────────────

    public MerchantShopScreen(MerchantShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
    }

    // ─── Init ─────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        cachedColItem   = Component.translatable("screen.merchant_shop.column_item").getString();
        cachedColPrice  = Component.translatable("screen.merchant_shop.column_price").getString();
        cachedColStock  = Component.translatable("screen.merchant_shop.column_stock").getString();
        cachedColQty    = Component.translatable("screen.merchant_shop.column_quantity").getString();
        cachedColTotal  = Component.translatable("screen.merchant_shop.column_total").getString();
        cachedTotalLabel = Component.translatable("screen.merchant_shop.total_cost").getString();

        loadShopItems();
        rebuildInputFields();
        addBuyButton();
    }

    private void addBuyButton() {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        buyButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.common.buy"),
            btn -> purchaseAllItems()
        ).bounds(x + GUI_W - 72, y + GUI_H - FOOTER_Y_OFFSET + 2, 64, 18).build());
    }

    // ─── Item-Laden ───────────────────────────────────────────────────

    private void loadShopItems() {
        shopItemRows.clear();
        int playerLevel = ClientProducerLevelCache.getCurrentLevel();
        for (ShopEntry entry : menu.getShopItems()) {
            ShopItemRow row = new ShopItemRow();
            row.item           = entry.getItem().copy();
            row.price          = entry.getPrice();
            row.unlimited      = entry.isUnlimited();
            row.stock          = entry.isUnlimited() ? Integer.MAX_VALUE : entry.getStock();
            row.requiredLevel  = entry.getRequiredLevel();
            row.locked         = row.requiredLevel > 0 && playerLevel < row.requiredLevel;
            if (row.item.hasTag() && row.item.getTag().contains("BillType")) {
                row.savedQty = "1";
                row.isBill   = true;
            }
            shopItemRows.add(row);
        }
        LOGGER.info("[MerchantShop] Loaded {} items", shopItemRows.size());
    }

    // ─── Input-Felder ────────────────────────────────────────────────

    private void rebuildInputFields() {
        // Alte Felder entfernen
        for (ShopItemRow row : shopItemRows) {
            if (row.input != null) { removeWidget(row.input); row.input = null; }
        }

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int visible = Math.min(VISIBLE_ROWS, shopItemRows.size() - scrollOffset);

        for (int i = 0; i < visible; i++) {
            int idx = i + scrollOffset;
            ShopItemRow row = shopItemRows.get(idx);
            boolean oos = !row.unlimited && row.stock <= 0;

            EditBox box = new EditBox(this.font,
                x + COL_INPUT, y + LIST_Y + i * ROW_H + 3, 40, 15,
                Component.translatable("screen.merchant_shop.quantity"));
            box.setMaxLength(4);

            if (row.isBill) {
                box.setValue("1");
                box.setEditable(false);
                box.setTextColor(0xAAAAAA);
            } else if (row.locked) {
                box.setValue("0");
                box.setEditable(false);
                box.setTextColor(C_STOCK_OOS);
            } else if (oos) {
                box.setValue("0");
                box.setEditable(false);
                box.setTextColor(C_STOCK_OOS);
            } else {
                box.setValue(row.savedQty);
                boolean isVehicle = row.item.getItem() instanceof ItemSpawnVehicle;
                if (isVehicle) {
                    box.setMaxLength(1);
                    box.setFilter(s -> s.isEmpty() || "1".equals(s));
                } else {
                    box.setFilter(s -> DIGITS_ONLY.matcher(s).matches());
                }
                final int fi = idx;
                box.setResponder(v -> shopItemRows.get(fi).savedQty = v);
            }
            row.input = box;
            addRenderableWidget(box);
        }
    }

    // ─── Scrollen ─────────────────────────────────────────────────────

    private void scrollUp() {
        if (scrollOffset > 0) { scrollOffset--; rebuildInputFields(); }
    }

    private void scrollDown() {
        if (scrollOffset < shopItemRows.size() - VISIBLE_ROWS) { scrollOffset++; rebuildInputFields(); }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) scrollUp();
        else if (delta < 0) scrollDown();
        return true;
    }

    // ─── Kaufen ───────────────────────────────────────────────────────

    private void purchaseAllItems() {
        saveInputValues();
        int sent = 0;

        for (int i = 0; i < shopItemRows.size(); i++) {
            ShopItemRow row = shopItemRows.get(i);
            if (row.locked) continue;
            if (row.savedQty.isEmpty() || "0".equals(row.savedQty)) continue;

            try {
                int qty = Integer.parseInt(row.savedQty);
                if (qty <= 0) continue;

                // Prüfe ob out of stock (clientseitig bekannte Menge)
                if (!row.unlimited && qty > row.stock) {
                    Minecraft.getInstance().player.sendSystemMessage(
                        Component.translatable("message.shop.out_of_stock", row.item.getHoverName())
                            .withStyle(ChatFormatting.RED));
                    continue;
                }

                NPCNetworkHandler.sendToServer(new PurchaseItemPacket(menu.getEntityId(), i, qty));
                sent++;
            } catch (NumberFormatException e) {
                LOGGER.error("[MerchantShop] Invalid quantity '{}'", row.savedQty, e);
            }
        }

        LOGGER.info("[MerchantShop] {} purchase packet(s) sent", sent);
        this.onClose();
    }

    private void saveInputValues() {
        for (ShopItemRow row : shopItemRows) {
            if (row.input != null) row.savedQty = row.input.getValue();
        }
    }

    // ─── Rendering ───────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Äußerer Rahmen + Hintergrund
        g.fill(x - 1, y - 1, x + GUI_W + 1, y + GUI_H + 1, C_BORDER);
        g.fill(x, y, x + GUI_W, y + GUI_H, C_BG);

        // Header-Bereich
        g.fill(x, y, x + GUI_W, y + HEADER_Y + 10, C_BG_HEADER);
        g.fill(x, y + HEADER_Y + 10, x + GUI_W, y + HEADER_Y + 11, C_BORDER);

        // Footer-Bereich
        int footerY = y + GUI_H - FOOTER_Y_OFFSET;
        g.fill(x, footerY - 1, x + GUI_W, footerY, C_BORDER);
        g.fill(x, footerY, x + GUI_W, y + GUI_H, C_BG_FOOTER);

        // Zebrastreifen für sichtbare Zeilen
        int visible = Math.min(VISIBLE_ROWS, shopItemRows.size() - scrollOffset);
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int rowY = y + LIST_Y + i * ROW_H;
            int rowColor = (i % 2 == 0) ? C_BG_ROW_EVEN : C_BG_ROW_ODD;

            if (i < visible) {
                ShopItemRow row = shopItemRows.get(i + scrollOffset);
                boolean oos = !row.unlimited && row.stock <= 0;
                if (oos || row.locked) rowColor = C_BG_OOS;
            }

            // Hover
            boolean hovered = my >= rowY && my < rowY + ROW_H && mx >= x && mx < x + GUI_W - (hasScrollbar() ? 8 : 0);
            g.fill(x, rowY, x + GUI_W - (hasScrollbar() ? 8 : 0), rowY + ROW_H,
                (hovered && i < visible) ? C_BG_HOVER : rowColor);
        }

        // Spalten-Trennlinien (leicht)
        int lineColor = 0x33FFFFFF;
        g.fill(x + COL_PRICE - 3,  y + LIST_Y, x + COL_PRICE - 2,  y + LIST_Y + VISIBLE_ROWS * ROW_H, lineColor);
        g.fill(x + COL_STOCK - 3,  y + LIST_Y, x + COL_STOCK - 2,  y + LIST_Y + VISIBLE_ROWS * ROW_H, lineColor);
        g.fill(x + COL_INPUT - 3,  y + LIST_Y, x + COL_INPUT - 2,  y + LIST_Y + VISIBLE_ROWS * ROW_H, lineColor);
        g.fill(x + COL_TOTAL - 3,  y + LIST_Y, x + COL_TOTAL - 2,  y + LIST_Y + VISIBLE_ROWS * ROW_H, lineColor);

        // Scrollbar
        if (hasScrollbar()) {
            renderScrollbar(g, x, y);
        }
    }

    private boolean hasScrollbar() {
        return shopItemRows.size() > VISIBLE_ROWS;
    }

    private void renderScrollbar(GuiGraphics g, int x, int y) {
        int sbX = x + GUI_W - 7;
        int sbY = y + LIST_Y;
        int sbH = VISIBLE_ROWS * ROW_H;

        // Track
        g.fill(sbX, sbY, sbX + 5, sbY + sbH, C_SCROLL_BG);

        // Thumb
        float ratio  = (float) VISIBLE_ROWS / shopItemRows.size();
        int thumbH   = Math.max(10, (int)(sbH * ratio));
        float posRatio = (float) scrollOffset / (shopItemRows.size() - VISIBLE_ROWS);
        int thumbY   = sbY + (int)((sbH - thumbH) * posRatio);
        g.fill(sbX + 1, thumbY, sbX + 4, thumbY + thumbH, C_SCROLL_BAR);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        this.renderBackground(g);
        super.render(g, mx, my, partial);
        this.renderTooltip(g, mx, my);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Spalten-Header
        g.drawString(font, cachedColItem,   x + COL_NAME,  y + HEADER_Y, C_TEXT_DARK, false);
        g.drawString(font, cachedColPrice,  x + COL_PRICE, y + HEADER_Y, C_TEXT_DARK, false);
        g.drawString(font, cachedColStock,  x + COL_STOCK, y + HEADER_Y, C_TEXT_DARK, false);
        g.drawString(font, cachedColQty,    x + COL_INPUT, y + HEADER_Y, C_TEXT_DARK, false);
        g.drawString(font, cachedColTotal,  x + COL_TOTAL, y + HEADER_Y, C_TEXT_DARK, false);

        // Shop-Zeilen
        int visible = Math.min(VISIBLE_ROWS, shopItemRows.size() - scrollOffset);
        for (int i = 0; i < visible; i++) {
            ShopItemRow row = shopItemRows.get(i + scrollOffset);
            renderRow(g, row, x, y + LIST_Y + i * ROW_H, mx, my);
        }

        // Leere Zeilen-Placeholder (für konsistentes Aussehen)
        if (shopItemRows.isEmpty()) {
            String empty = Component.translatable("screen.merchant_shop.empty").getString();
            int ew = font.width(empty);
            g.drawString(font, empty, x + (GUI_W - ew) / 2, y + LIST_Y + 3 * ROW_H, C_TEXT_GRAY, false);
        }

        // Footer: Gesamtkosten + Scroll-Indikator
        renderFooter(g, x, y);
    }

    private void renderRow(GuiGraphics g, ShopItemRow row, int x, int rowY, int mx, int my) {
        boolean oos = !row.unlimited && row.stock <= 0;
        int nameColor = (oos || row.locked) ? C_STOCK_OOS : C_TEXT;

        // Icon
        g.renderItem(row.item, x + COL_ICON, rowY + 3);

        // Name
        String name = row.item.getHoverName().getString();
        // Maximalbreite berücksichtigt ggf. Level-Badge
        int nameMaxWidth = COL_PRICE - COL_NAME - (row.requiredLevel > 0 ? 32 : 6);
        if (font.width(name) > nameMaxWidth) {
            while (font.width(name + "..") > nameMaxWidth && !name.isEmpty())
                name = name.substring(0, name.length() - 1);
            name += "..";
        }
        g.drawString(font, name, x + COL_NAME, rowY + 7, nameColor, false);

        // Level-Badge (rechts neben dem Namen, vor der Preis-Spalte)
        if (row.requiredLevel > 0) {
            String badge = "Lvl " + row.requiredLevel;
            int badgeColor = row.locked ? 0xFFFF5555 : 0xFF55FF55;
            g.drawString(font, badge, x + COL_PRICE - font.width(badge) - 2, rowY + 7, badgeColor, false);
        }

        // Preis
        g.drawString(font, MoneyFormat.format(row.price), x + COL_PRICE, rowY + 7, C_PRICE, false);

        // Vorrat
        String stockStr = row.unlimited ? "∞" : (oos ? "✗" : String.valueOf(row.stock));
        int stockColor  = row.unlimited ? C_ACCENT : (oos ? C_STOCK_OOS : C_STOCK);
        g.drawString(font, stockStr, x + COL_STOCK, rowY + 7, stockColor, false);

        // Gesamtpreis dieser Zeile (live berechnet aus Input)
        if (row.input != null) {
            int qty = parsePositiveIntOrZero(row.input.getValue());
            if (qty > 0) {
                String rowTotal = MoneyFormat.format((long) row.price * qty);
                g.drawString(font, rowTotal, x + COL_TOTAL, rowY + 7, C_TOTAL_POS, false);
            }
        }
    }

    private int parsePositiveIntOrZero(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        try {
            int parsed = Integer.parseInt(raw);
            return Math.max(0, parsed);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void renderFooter(GuiGraphics g, int x, int y) {
        saveInputValues(); // aktuellen Stand für Berechnung sichern
        int footerY = y + GUI_H - FOOTER_Y_OFFSET;

        // Gesamtkosten
        long total = 0;
        for (ShopItemRow row : shopItemRows) {
            if (!row.savedQty.isEmpty()) {
                try {
                    long q = Long.parseLong(row.savedQty);
                    if (q > 0) total += (long) row.price * q;
                } catch (NumberFormatException ex) {
                    total += 0;
                }
            }
        }
        g.drawString(font, cachedTotalLabel, x + 8, footerY + 5, C_TEXT_GRAY, false);
        int tw = font.width(cachedTotalLabel);
        g.drawString(font, MoneyFormat.format(total), x + 12 + tw, footerY + 5, total > 0 ? C_TOTAL_POS : C_TEXT_DARK, false);

        // Scroll-Indikator (Mitte)
        if (shopItemRows.size() > VISIBLE_ROWS) {
            int end  = Math.min(scrollOffset + VISIBLE_ROWS, shopItemRows.size());
            String indicator = (scrollOffset + 1) + "-" + end + " / " + shopItemRows.size();
            int iw = font.width(indicator);
            g.drawString(font, indicator, x + (GUI_W - iw) / 2, footerY + 5, C_TEXT_DARK, false);
        }
    }

    // ─── Labels ───────────────────────────────────────────────────────

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        String cat = Component.translatable("screen.merchant_shop.title_prefix").getString()
            + menu.getCategory().getDisplayName();
        g.drawString(font, cat, 8, 7, C_ACCENT, false);
    }

    // ─── Keys ─────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(int keyCode, int scan, int mods) {
        if (keyCode == 264 || keyCode == 265) { // NOPMD
            return true;
        }

        return keyCode == 69 || super.keyPressed(keyCode, scan, mods);
    }

    // ─── Interne Datenklasse ──────────────────────────────────────────

    private static class ShopItemRow {
        ItemStack item;
        int       price;
        boolean   unlimited;
        int       stock;
        boolean   isBill;
        EditBox   input;
        String    savedQty      = "";
        int       requiredLevel = 0;
        boolean   locked        = false;
    }
}
