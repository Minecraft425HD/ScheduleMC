package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.bank.StockMarketData;
import de.rolandsw.schedulemc.npc.menu.BoerseMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.StockTradePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Vollständige Börsen-GUI für Handel mit Gold/Diamant/Smaragd
 * Zeigt Live-Preise, Trends und ermöglicht schnellen Handel
 */
@OnlyIn(Dist.CLIENT)
public class BoerseScreen extends AbstractContainerScreen<BoerseMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/npc_interaction.png");

    // Trade Buttons für Gold
    private Button goldBuy1Button;
    private Button goldBuy10Button;
    private Button goldBuy64Button;
    private Button goldSellAllButton;

    // Trade Buttons für Diamant
    private Button diamondBuy1Button;
    private Button diamondBuy10Button;
    private Button diamondBuy64Button;
    private Button diamondSellAllButton;

    // Trade Buttons für Smaragd
    private Button emeraldBuy1Button;
    private Button emeraldBuy10Button;
    private Button emeraldBuy64Button;
    private Button emeraldSellAllButton;

    // Close Button
    private Button closeButton;

    public BoerseScreen(BoerseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Gold Buttons (Zeile 1)
        int goldY = y + 45;
        goldBuy1Button = addRenderableWidget(Button.builder(Component.literal("+1"), button -> {
            trade(StockTradePacket.TradeType.BUY, StockTradePacket.StockType.GOLD, 1);
        }).bounds(x + 75, goldY, 20, 12).build());

        goldBuy10Button = addRenderableWidget(Button.builder(Component.literal("+10"), button -> {
            trade(StockTradePacket.TradeType.BUY, StockTradePacket.StockType.GOLD, 10);
        }).bounds(x + 97, goldY, 23, 12).build());

        goldBuy64Button = addRenderableWidget(Button.builder(Component.literal("+64"), button -> {
            trade(StockTradePacket.TradeType.BUY, StockTradePacket.StockType.GOLD, 64);
        }).bounds(x + 122, goldY, 23, 12).build());

        goldSellAllButton = addRenderableWidget(Button.builder(Component.literal("Alle"), button -> {
            sellAll(StockTradePacket.StockType.GOLD);
        }).bounds(x + 147, goldY, 23, 12).build());

        // Diamant Buttons (Zeile 2)
        int diamondY = y + 68;
        diamondBuy1Button = addRenderableWidget(Button.builder(Component.literal("+1"), button -> {
            trade(StockTradePacket.TradeType.BUY, StockTradePacket.StockType.DIAMOND, 1);
        }).bounds(x + 75, diamondY, 20, 12).build());

        diamondBuy10Button = addRenderableWidget(Button.builder(Component.literal("+10"), button -> {
            trade(StockTradePacket.TradeType.BUY, StockTradePacket.StockType.DIAMOND, 10);
        }).bounds(x + 97, diamondY, 23, 12).build());

        diamondBuy64Button = addRenderableWidget(Button.builder(Component.literal("+64"), button -> {
            trade(StockTradePacket.TradeType.BUY, StockTradePacket.StockType.DIAMOND, 64);
        }).bounds(x + 122, diamondY, 23, 12).build());

        diamondSellAllButton = addRenderableWidget(Button.builder(Component.literal("Alle"), button -> {
            sellAll(StockTradePacket.StockType.DIAMOND);
        }).bounds(x + 147, diamondY, 23, 12).build());

        // Smaragd Buttons (Zeile 3)
        int emeraldY = y + 91;
        emeraldBuy1Button = addRenderableWidget(Button.builder(Component.literal("+1"), button -> {
            trade(StockTradePacket.TradeType.BUY, StockTradePacket.StockType.EMERALD, 1);
        }).bounds(x + 75, emeraldY, 20, 12).build());

        emeraldBuy10Button = addRenderableWidget(Button.builder(Component.literal("+10"), button -> {
            trade(StockTradePacket.TradeType.BUY, StockTradePacket.StockType.EMERALD, 10);
        }).bounds(x + 97, emeraldY, 23, 12).build());

        emeraldBuy64Button = addRenderableWidget(Button.builder(Component.literal("+64"), button -> {
            trade(StockTradePacket.TradeType.BUY, StockTradePacket.StockType.EMERALD, 64);
        }).bounds(x + 122, emeraldY, 23, 12).build());

        emeraldSellAllButton = addRenderableWidget(Button.builder(Component.literal("Alle"), button -> {
            sellAll(StockTradePacket.StockType.EMERALD);
        }).bounds(x + 147, emeraldY, 23, 12).build());

        // Close Button
        closeButton = addRenderableWidget(Button.builder(Component.literal("Schließen"), button -> {
            this.onClose();
        }).bounds(x + 38, y + 140, 100, 20).build());
    }

    /**
     * Führt einen Handel aus
     */
    private void trade(StockTradePacket.TradeType tradeType, StockTradePacket.StockType stockType, int quantity) {
        NPCNetworkHandler.sendToServer(new StockTradePacket(tradeType, stockType, quantity));
        this.onClose();
    }

    /**
     * Verkauft alle Items eines Typs
     */
    private void sellAll(StockTradePacket.StockType stockType) {
        if (minecraft == null || minecraft.player == null) return;

        int count = countItems(stockType);
        if (count > 0) {
            NPCNetworkHandler.sendToServer(new StockTradePacket(StockTradePacket.TradeType.SELL, stockType, count));
            this.onClose();
        }
    }

    /**
     * Zählt Items im Inventar
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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Get stock market data
        StockMarketData stockMarket = StockMarketData.getInstance(minecraft.level.getServer());

        // Header
        guiGraphics.drawString(this.font, "MARKT", x + 10, y + 25, 0x404040, false);
        guiGraphics.drawString(this.font, "PREIS", x + 55, y + 25, 0x404040, false);
        guiGraphics.drawString(this.font, "HANDEL", x + 100, y + 25, 0x404040, false);
        guiGraphics.drawString(this.font, "BESTAND", x + 10, y + 33, 0x808080, false);

        // Gold
        renderStockRow(guiGraphics, x, y + 40, "Gold", Items.GOLD_INGOT,
            stockMarket.getCurrentPrice(Items.GOLD_INGOT),
            stockMarket.getTrend(Items.GOLD_INGOT),
            countItems(StockTradePacket.StockType.GOLD));

        // Diamant
        renderStockRow(guiGraphics, x, y + 63, "Diamant", Items.DIAMOND,
            stockMarket.getCurrentPrice(Items.DIAMOND),
            stockMarket.getTrend(Items.DIAMOND),
            countItems(StockTradePacket.StockType.DIAMOND));

        // Smaragd
        renderStockRow(guiGraphics, x, y + 86, "Smaragd", Items.EMERALD,
            stockMarket.getCurrentPrice(Items.EMERALD),
            stockMarket.getTrend(Items.EMERALD),
            countItems(StockTradePacket.StockType.EMERALD));

        // Kontostand
        if (minecraft != null && minecraft.player != null) {
            double balance = EconomyManager.getBalance(minecraft.player.getUUID());
            guiGraphics.drawString(this.font, "Kontostand:", x + 10, y + 110, 0x808080, false);
            guiGraphics.drawString(this.font, String.format("%.2f€", balance),
                x + 75, y + 110, 0x00AA00, false);
        }

        // Info
        guiGraphics.drawString(this.font, "Tipp: Kaufe günstig, verkaufe teuer!", x + 10, y + 122, 0x606060, false);
    }

    /**
     * Rendert eine Zeile für ein Wertpapier
     */
    private void renderStockRow(GuiGraphics guiGraphics, int x, int y, String name,
                                net.minecraft.world.item.Item item, double price, int trend, int playerStock) {
        // Name
        guiGraphics.drawString(this.font, name, x + 10, y, 0x404040, false);

        // Preis mit Trend-Arrow
        String priceStr = String.format("%.0f€", price);
        String trendArrow = trend > 0 ? "↗" : (trend < 0 ? "↘" : "→");
        int trendColor = trend > 0 ? 0x00AA00 : (trend < 0 ? 0xFF5555 : 0x808080);

        guiGraphics.drawString(this.font, priceStr, x + 43, y, 0xFFAA00, false);
        guiGraphics.drawString(this.font, trendArrow, x + 65, y, trendColor, false);

        // Player Stock
        guiGraphics.drawString(this.font, playerStock + "x", x + 10, y + 8, 0x808080, false);
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
        guiGraphics.drawString(this.font, "BÖRSENMAKLER", 8, 6, 0x404040, false);
    }
}
