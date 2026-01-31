package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.network.ClientBankDataCache;
import de.rolandsw.schedulemc.npc.menu.BoerseMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.RequestStockDataPacket;
import de.rolandsw.schedulemc.npc.network.StockTradePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Modern Trading Dashboard für Börsen-NPC
 * Multi-Panel Layout mit Charts, Slider und Portfolio
 */
@OnlyIn(Dist.CLIENT)
public class BoerseScreen extends AbstractContainerScreen<BoerseMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/gui/boerse_gui.png");

    // Selected stock for trading
    private StockTradePacket.StockType selectedStock = StockTradePacket.StockType.GOLD;

    // Quantity slider
    private QuantitySlider quantitySlider;

    // Trade buttons
    private Button buyButton;
    private Button sellButton;
    private Button maxBuyButton;
    private Button sellAllButton;

    // Stock selection buttons
    private Button goldSelectButton;
    private Button diamondSelectButton;
    private Button emeraldSelectButton;

    // Close button
    private Button closeButton;

    public BoerseScreen(BoerseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // === LEFT PANEL: Stock Selection (3 buttons) ===
        goldSelectButton = addRenderableWidget(Button.builder(
            Component.literal("▶ ").append(Component.translatable("gui.boerse.stock_gold")),
            btn -> selectStock(StockTradePacket.StockType.GOLD)
        ).bounds(x + 7, y + 40, 120, 20).build());

        diamondSelectButton = addRenderableWidget(Button.builder(
            Component.literal("  ").append(Component.translatable("gui.boerse.stock_diamond")),
            btn -> selectStock(StockTradePacket.StockType.DIAMOND)
        ).bounds(x + 7, y + 68, 120, 20).build());

        emeraldSelectButton = addRenderableWidget(Button.builder(
            Component.literal("  ").append(Component.translatable("gui.boerse.stock_emerald")),
            btn -> selectStock(StockTradePacket.StockType.EMERALD)
        ).bounds(x + 7, y + 96, 120, 20).build());

        // === RIGHT PANEL: Trade Controls ===

        // Quantity Slider
        quantitySlider = addRenderableWidget(new QuantitySlider(x + 135, y + 65, 115, 20));

        // Buy Button
        buyButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.common.buy"),
            btn -> executeTrade(StockTradePacket.TradeType.BUY)
        ).bounds(x + 135, y + 90, 56, 20).build());

        // Sell Button
        sellButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.boerse.sell"),
            btn -> executeTrade(StockTradePacket.TradeType.SELL)
        ).bounds(x + 194, y + 90, 56, 20).build());

        // Max Buy Button (calculates max affordable)
        maxBuyButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.boerse.max_buy"),
            btn -> {
                int max = calculateMaxAffordable();
                quantitySlider.setValue(Math.min(max, 64));
            }
        ).bounds(x + 135, y + 113, 56, 18).build());

        // Sell All Button
        sellAllButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.boerse.sell_all"),
            btn -> executeSellAll()
        ).bounds(x + 194, y + 113, 56, 18).build());

        // Close Button
        closeButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.common.close"),
            btn -> this.onClose()
        ).bounds(x + 78, y + 198, 100, 18).build());

        // Request stock market data from server
        NPCNetworkHandler.sendToServer(new RequestStockDataPacket());

        // Initial stock selection
        selectStock(StockTradePacket.StockType.GOLD);
    }

    /**
     * Selects a stock for trading
     */
    private void selectStock(StockTradePacket.StockType stock) {
        selectedStock = stock;

        // Update button labels to show selection
        goldSelectButton.setMessage(Component.literal(
            stock == StockTradePacket.StockType.GOLD ? "▶ " : "  "
        ).append(Component.translatable("gui.boerse.stock_gold")));
        diamondSelectButton.setMessage(Component.literal(
            stock == StockTradePacket.StockType.DIAMOND ? "▶ " : "  "
        ).append(Component.translatable("gui.boerse.stock_diamond")));
        emeraldSelectButton.setMessage(Component.literal(
            stock == StockTradePacket.StockType.EMERALD ? "▶ " : "  "
        ).append(Component.translatable("gui.boerse.stock_emerald")));

        // Reset slider
        quantitySlider.setValue(1);
    }

    /**
     * Executes a trade
     */
    private void executeTrade(StockTradePacket.TradeType tradeType) {
        int quantity = quantitySlider.getQuantity();
        if (quantity > 0) {
            NPCNetworkHandler.sendToServer(new StockTradePacket(tradeType, selectedStock, quantity));
            // DON'T CLOSE - let player continue trading!
            // Re-request data to update display
            NPCNetworkHandler.sendToServer(new RequestStockDataPacket());
        }
    }

    /**
     * Sells all items of selected stock
     */
    private void executeSellAll() {
        int count = countItems(selectedStock);
        if (count > 0) {
            NPCNetworkHandler.sendToServer(new StockTradePacket(
                StockTradePacket.TradeType.SELL, selectedStock, count
            ));
            NPCNetworkHandler.sendToServer(new RequestStockDataPacket());
        }
    }

    /**
     * Calculates maximum affordable quantity
     */
    private int calculateMaxAffordable() {
        double balance = ClientBankDataCache.getBalance();
        double price = getCurrentPrice(selectedStock);
        if (price <= 0) return 0;
        return Math.min(64, (int)(balance / price));
    }

    /**
     * Counts items in inventory
     */
    private int countItems(StockTradePacket.StockType stockType) {
        if (minecraft == null || minecraft.player == null) return 0;

        int count = 0;
        for (int i = 0; i < minecraft.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = minecraft.player.getInventory().getItem(i);
            if (stack.getItem() == stockType.getItem()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Gets current price for a stock
     */
    private double getCurrentPrice(StockTradePacket.StockType stock) {
        return switch (stock) {
            case GOLD -> ClientBankDataCache.getGoldPrice();
            case DIAMOND -> ClientBankDataCache.getDiamondPrice();
            case EMERALD -> ClientBankDataCache.getEmeraldPrice();
        };
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // === LEFT PANEL: Stock Market Overview ===
        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.stock_market").getString(), x + 8, y + 22, 0x404040, false);

        // Render each stock row
        renderStockRow(guiGraphics, x + 7, y + 42, StockTradePacket.StockType.GOLD);
        renderStockRow(guiGraphics, x + 7, y + 70, StockTradePacket.StockType.DIAMOND);
        renderStockRow(guiGraphics, x + 7, y + 98, StockTradePacket.StockType.EMERALD);

        // === MINI CHARTS ===
        renderMiniChart(guiGraphics, x + 8, y + 125, 120, 30, ClientBankDataCache.getGoldHistory(), 0xFFAA00);
        renderMiniChart(guiGraphics, x + 8, y + 125, 120, 30, ClientBankDataCache.getDiamondHistory(), 0x55FFFF);
        renderMiniChart(guiGraphics, x + 8, y + 125, 120, 30, ClientBankDataCache.getEmeraldHistory(), 0x55FF55);

        // === RIGHT PANEL: Trade Info ===
        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.trade_panel").getString(), x + 137, y + 22, 0x404040, false);

        // Selected stock info
        String stockName = selectedStock.getDisplayName();
        double currentPrice = getCurrentPrice(selectedStock);
        int playerStock = countItems(selectedStock);

        guiGraphics.drawString(this.font, stockName, x + 137, y + 38, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("%.0f€", currentPrice), x + 210, y + 38, 0xFFAA00, false);

        // Statistics
        double high = getHighPrice(selectedStock);
        double low = getLowPrice(selectedStock);
        double avg = getAvgPrice(selectedStock);

        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.high_24h").getString(), x + 137, y + 50, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.0f\u20ac", high), x + 210, y + 50, 0x00AA00, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.low_24h").getString(), x + 137, y + 58, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.0f\u20ac", low), x + 210, y + 58, 0xFF5555, false);

        // Quantity & Cost
        int quantity = quantitySlider.getQuantity();
        double totalCost = currentPrice * quantity;

        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.quantity", quantity).getString(), x + 137, y + 135, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.cost", String.format("%.0f", totalCost)).getString(), x + 137, y + 145, 0xFFAA00, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.own", playerStock).getString(), x + 137, y + 155, 0x606060, false);

        // Steuer-Hinweis (falls Spieler Items besitzt)
        if (playerStock > 0) {
            guiGraphics.drawString(this.font, Component.translatable("gui.boerse.tax_hint").getString(), x + 137, y + 164, 0xFFAA00, false);
        }

        // === BOTTOM PANEL: Portfolio ===
        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.portfolio").getString(), x + 8, y + 165, 0x404040, false);

        double balance = ClientBankDataCache.getBalance();
        double portfolioValue = calculatePortfolioValue();

        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.balance_label").getString(), x + 8, y + 177, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f\u20ac", balance), x + 70, y + 177, 0x00AA00, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.holdings").getString(), x + 8, y + 186, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.0f\u20ac", portfolioValue), x + 70, y + 186, 0xFFAA00, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.total").getString(), x + 135, y + 177, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.0f\u20ac", balance + portfolioValue),
            x + 185, y + 177, 0x55FFFF, false);
    }

    /**
     * Renders a stock row with price and trend
     */
    private void renderStockRow(GuiGraphics guiGraphics, int x, int y, StockTradePacket.StockType stock) {
        double price = getCurrentPrice(stock);
        int trend = getTrend(stock);
        double changePercent = getChangePercent(stock);

        // Trend arrow and color
        String trendArrow = trend > 0 ? "↗" : (trend < 0 ? "↘" : "→");
        int trendColor = trend > 0 ? 0x00AA00 : (trend < 0 ? 0xFF5555 : 0x808080);

        // Draw on button (offset slightly)
        int yOffset = 5;
    }

    /**
     * Renders a mini price chart
     */
    private void renderMiniChart(GuiGraphics guiGraphics, int x, int y, int width, int height, List<Double> history, int color) {
        if (history == null || history.size() < 2) return;

        // Find min/max for scaling
        double min = history.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = history.stream().mapToDouble(Double::doubleValue).max().orElse(100);
        double range = max - min;
        if (range < 0.01) range = 1.0; // Avoid division by zero

        // Draw chart lines
        int segments = history.size() - 1;
        float segmentWidth = (float) width / segments;

        for (int i = 0; i < segments; i++) {
            double v1 = history.get(i);
            double v2 = history.get(i + 1);

            int y1 = y + height - (int)((v1 - min) / range * height);
            int y2 = y + height - (int)((v2 - min) / range * height);
            int x1 = x + (int)(i * segmentWidth);
            int x2 = x + (int)((i + 1) * segmentWidth);

            // Draw line using fill (simple line drawing)
            guiGraphics.fill(x1, y1, x2, y2, color | 0xFF000000);
        }
    }

    /**
     * Calculates portfolio value (all holdings)
     */
    private double calculatePortfolioValue() {
        double goldValue = countItems(StockTradePacket.StockType.GOLD) * ClientBankDataCache.getGoldPrice();
        double diamondValue = countItems(StockTradePacket.StockType.DIAMOND) * ClientBankDataCache.getDiamondPrice();
        double emeraldValue = countItems(StockTradePacket.StockType.EMERALD) * ClientBankDataCache.getEmeraldPrice();
        return goldValue + diamondValue + emeraldValue;
    }

    private int getTrend(StockTradePacket.StockType stock) {
        return switch (stock) {
            case GOLD -> ClientBankDataCache.getGoldTrend();
            case DIAMOND -> ClientBankDataCache.getDiamondTrend();
            case EMERALD -> ClientBankDataCache.getEmeraldTrend();
        };
    }

    private double getChangePercent(StockTradePacket.StockType stock) {
        // Calculate from current vs average (simplified)
        double current = getCurrentPrice(stock);
        double avg = getAvgPrice(stock);
        if (avg > 0) {
            return ((current - avg) / avg) * 100.0;
        }
        return 0.0;
    }

    private double getHighPrice(StockTradePacket.StockType stock) {
        return switch (stock) {
            case GOLD -> ClientBankDataCache.getGoldHigh();
            case DIAMOND -> ClientBankDataCache.getDiamondHigh();
            case EMERALD -> ClientBankDataCache.getEmeraldHigh();
        };
    }

    private double getLowPrice(StockTradePacket.StockType stock) {
        return switch (stock) {
            case GOLD -> ClientBankDataCache.getGoldLow();
            case DIAMOND -> ClientBankDataCache.getDiamondLow();
            case EMERALD -> ClientBankDataCache.getEmeraldLow();
        };
    }

    private double getAvgPrice(StockTradePacket.StockType stock) {
        return switch (stock) {
            case GOLD -> ClientBankDataCache.getGoldAvg();
            case DIAMOND -> ClientBankDataCache.getDiamondAvg();
            case EMERALD -> ClientBankDataCache.getEmeraldAvg();
        };
    }

    @Override
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
        guiGraphics.drawString(this.font, Component.translatable("gui.boerse.title").getString(), 8, 6, 0x404040, false);
    }

    /**
     * Custom slider for quantity selection
     */
    private class QuantitySlider extends AbstractSliderButton {
        private static final int MIN = 1;
        private static final int MAX = 64;

        public QuantitySlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.translatable("gui.boerse.qty_label", 1), 0.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable("gui.boerse.qty_label", getQuantity()));
        }

        @Override
        protected void applyValue() {
            // Called when slider is moved
        }

        public int getQuantity() {
            return MIN + (int)(this.value * (MAX - MIN));
        }

        public void setValue(int quantity) {
            this.value = (double)(quantity - MIN) / (MAX - MIN);
            this.value = Math.max(0.0, Math.min(1.0, this.value));
            updateMessage();
        }
    }
}
