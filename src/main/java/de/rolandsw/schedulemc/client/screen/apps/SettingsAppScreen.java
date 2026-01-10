package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.client.screen.ConfirmDialogScreen;
import de.rolandsw.schedulemc.client.screen.InputDialogScreen;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.network.*;
import de.rolandsw.schedulemc.utility.PlotUtilityData;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;
import de.rolandsw.schedulemc.player.network.ClientPlayerSettings;
import de.rolandsw.schedulemc.player.network.PlayerSettingsNetworkHandler;
import de.rolandsw.schedulemc.player.network.PlayerSettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Settings App - Einstellungen auf dem Smartphone
 *
 * Features:
 * - 3 Tabs mit Scroll-Support
 * - Tab 1: Plot-Einstellungen (Verkauf/Miete, Trusted Players, Umbenennen)
 * - Tab 2: Benachrichtigungen (Utility-Warnungen, Schwellenwerte)
 * - Tab 3: Konto (Kontostand)
 */
@OnlyIn(Dist.CLIENT)
public class SettingsAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 5;
    private static final int MARGIN_BOTTOM = 60;

    // Tab-System
    private int currentTab = 0;
    private static final int TAB_HEIGHT = 22;
    private static final int TAB_WIDTH = 58;

    // Scrolling
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_SPEED = 15;
    private static final int CONTENT_HEIGHT = 160;

    private int leftPos;
    private int topPos;

    // Cached Data
    private PlotRegion currentPlot;
    private List<PlotRegion> myPlots;

    // Settings State (simuliert - würde normalerweise persistent gespeichert)
    private boolean utilityWarningsEnabled = true;
    private double electricityWarningThreshold = 100.0; // kWh
    private double waterWarningThreshold = 500.0; // L

    // Clickable regions for interactive elements
    private final List<ClickableRegion> clickableRegions = new ArrayList<>();
    private final List<SliderRegion> sliderRegions = new ArrayList<>();

    private static class ClickableRegion {
        int x1, y1, x2, y2;
        Runnable onClick;

        ClickableRegion(int x1, int y1, int x2, int y2, Runnable onClick) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.onClick = onClick;
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
        }
    }

    private static class SliderRegion {
        int x, y, width;
        double minValue, maxValue;
        java.util.function.Consumer<Double> onValueChange;

        SliderRegion(int x, int y, int width, double minValue, double maxValue,
                    java.util.function.Consumer<Double> onValueChange) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.onValueChange = onValueChange;
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 6;
        }

        double getValue(int mouseX) {
            double percent = Math.max(0, Math.min(1, (mouseX - x) / (double) width));
            return minValue + (maxValue - minValue) * percent;
        }
    }

    public SettingsAppScreen(Screen parent) {
        super(Component.literal("Settings"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Lade gespeicherte Einstellungen vom Client-Cache
        this.utilityWarningsEnabled = ClientPlayerSettings.utilityWarningsEnabled;
        this.electricityWarningThreshold = ClientPlayerSettings.electricityThreshold;
        this.waterWarningThreshold = ClientPlayerSettings.waterThreshold;

        this.leftPos = (this.width - WIDTH) / 2;

        // Positioniere oben mit Margin
        this.topPos = MARGIN_TOP;

        // Cache data
        refreshData();

        // Tab-Buttons
        String[] tabKeys = {"gui.app.settings.tab.plot", "gui.app.settings.tab.notification", "gui.app.settings.tab.account"};
        for (int i = 0; i < tabKeys.length; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(
                Component.translatable(tabKeys[i]),
                button -> {
                    currentTab = tabIndex;
                    scrollOffset = 0;
                    refreshData();
                }
            ).bounds(leftPos + 10 + (i * TAB_WIDTH), topPos + 30, TAB_WIDTH - 2, TAB_HEIGHT).build());
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.common.back_arrows"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());
    }

    /**
     * Aktualisiert gecachte Daten
     */
    private void refreshData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        String playerUUID = mc.player.getUUID().toString();

        // Aktueller Plot
        currentPlot = PlotManager.getPlotAt(playerPos);

        // Meine Plots
        myPlots = new ArrayList<>();
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (playerUUID.equals(plot.getOwnerUUID())) {
                myPlots.add(plot);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.settings.title").getString(), leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        // Tab-Hintergrund (aktiver Tab hervorheben)
        for (int i = 0; i < 3; i++) {
            int tabX = leftPos + 10 + (i * TAB_WIDTH);
            int tabY = topPos + 30;
            if (i == currentTab) {
                guiGraphics.fill(tabX - 1, tabY - 1, tabX + TAB_WIDTH - 1, tabY + TAB_HEIGHT + 1, 0xFF4A90E2);
            }
        }

        // Content-Bereich
        int contentY = topPos + 55;
        int contentEndY = topPos + HEIGHT - 40;

        // Clear clickable regions
        clickableRegions.clear();

        // Render Tab-Content
        switch (currentTab) {
            case 0 -> renderPlotSettingsTab(guiGraphics, contentY, contentEndY, mouseX, mouseY);
            case 1 -> renderNotificationsTab(guiGraphics, contentY, contentEndY, mouseX, mouseY);
            case 2 -> renderAccountTab(guiGraphics, contentY, contentEndY);
        }

        // Scroll-Indikator
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, CONTENT_HEIGHT * CONTENT_HEIGHT / (CONTENT_HEIGHT + maxScroll));
            int scrollBarY = contentY + (scrollOffset * (CONTENT_HEIGHT - scrollBarHeight) / maxScroll);
            guiGraphics.fill(leftPos + WIDTH - 8, contentY, leftPos + WIDTH - 5, contentEndY, 0x44FFFFFF);
            guiGraphics.fill(leftPos + WIDTH - 8, scrollBarY, leftPos + WIDTH - 5, scrollBarY + scrollBarHeight, 0xAAFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 1: PLOT-EINSTELLUNGEN
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderPlotSettingsTab(GuiGraphics guiGraphics, int startY, int endY, int mouseX, int mouseY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String playerUUID = mc.player.getUUID().toString();

        // Aktueller Plot Check
        if (currentPlot == null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.settings.no_plot").getString(), leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.settings.stand_on_plot").getString(), leftPos + WIDTH / 2, y + 35, 0x666666);
            maxScroll = 0;
            return;
        }

        boolean isOwner = playerUUID.equals(currentPlot.getOwnerUUID());
        String plotId = currentPlot.getPlotId();

        // Plot-Header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§l" + currentPlot.getPlotName(), leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        if (!isOwner) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x44AA0000);
                guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.not_your_plot").getString(), leftPos + 15, y + 8, 0xFF5555);
            }
            y += 30;
            contentHeight += 30;
            maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
            return;
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // VERKAUF / MIETE
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.sale_rent").getString(), leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Aktueller Status
        String saleStatus;
        if (currentPlot.isForSale()) {
            saleStatus = Component.translatable("gui.app.settings.for_sale").getString() + " (" + String.format("%.0f€", currentPlot.getSalePrice()) + ")";
        } else if (currentPlot.isForRent()) {
            if (currentPlot.isRented()) {
                saleStatus = Component.translatable("gui.app.settings.rented").getString();
            } else {
                saleStatus = Component.translatable("gui.app.settings.to_rent").getString() + " (" + String.format("%.0f€/Tag", currentPlot.getRentPricePerDay()) + ")";
            }
        } else {
            saleStatus = Component.translatable("gui.app.settings.private").getString();
        }

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.status").getString() + saleStatus, leftPos + 15, y, 0xAAAAAA);
        }
        y += 12;
        contentHeight += 12;

        // ✅ INTERAKTIVE BUTTONS
        if (y >= startY - 30 && y < endY + 30) {
            int btnY = y;
            int btnWidth = WIDTH - 20;

            // "Zum Verkauf stellen" Button
            if (!currentPlot.isForSale()) {
                drawButton(guiGraphics, leftPos + 10, btnY, btnWidth, 18, Component.translatable("gui.app.settings.list_for_sale").getString(), 0x55FF55, mouseX, mouseY);
                clickableRegions.add(new ClickableRegion(leftPos + 10, btnY, leftPos + 10 + btnWidth, btnY + 18, () -> {
                    minecraft.setScreen(new InputDialogScreen(this, Component.translatable("gui.app.settings.sale_price").getString(), Component.translatable("gui.app.settings.enter_price").getString(),
                        InputDialogScreen.InputType.NUMBER, price -> {
                            PlotNetworkHandler.sendToServer(new PlotSalePacket(plotId, Double.parseDouble(price), PlotSalePacket.SaleType.SELL));
                        }));
                }));
                btnY += 20;
            }

            // "Zur Miete stellen" Button
            if (!currentPlot.isForRent()) {
                drawButton(guiGraphics, leftPos + 10, btnY, btnWidth, 18, Component.translatable("gui.app.settings.list_for_rent").getString(), 0xFF55FF, mouseX, mouseY);
                clickableRegions.add(new ClickableRegion(leftPos + 10, btnY, leftPos + 10 + btnWidth, btnY + 18, () -> {
                    minecraft.setScreen(new InputDialogScreen(this, Component.translatable("gui.app.settings.rent_price").getString(), Component.translatable("gui.app.settings.price_per_day").getString(),
                        InputDialogScreen.InputType.NUMBER, price -> {
                            PlotNetworkHandler.sendToServer(new PlotSalePacket(plotId, Double.parseDouble(price), PlotSalePacket.SaleType.RENT));
                        }));
                }));
                btnY += 20;
            }

            // "Angebot beenden" Button
            if (currentPlot.isForSale() || currentPlot.isForRent()) {
                drawButton(guiGraphics, leftPos + 10, btnY, btnWidth, 18, Component.translatable("gui.app.settings.end_offer").getString(), 0xFF5555, mouseX, mouseY);
                clickableRegions.add(new ClickableRegion(leftPos + 10, btnY, leftPos + 10 + btnWidth, btnY + 18, () -> {
                    PlotNetworkHandler.sendToServer(new PlotSalePacket(plotId, 0, PlotSalePacket.SaleType.CANCEL));
                }));
                btnY += 20;
            }

            y = btnY;
        } else {
            y += 60; // Reserve space even when scrolled out of view
        }
        contentHeight += 60;

        // ═══════════════════════════════════════════════════════════════════════════
        // TRUSTED PLAYERS
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.trusted_players").getString(), leftPos + 15, y, 0x55FFFF);
        }
        y += 15;
        contentHeight += 15;

        // Zeige Trusted Players mit Remove-Buttons
        Set<String> trustedPlayers = currentPlot.getTrustedPlayers();
        if (trustedPlayers.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.no_trusted").getString(), leftPos + 15, y, 0x666666);
            }
            y += 12;
            contentHeight += 12;
        } else {
            for (String trusted : trustedPlayers) {
                if (y >= startY - 30 && y < endY + 30) {
                    guiGraphics.drawString(this.font, "§a● §f" + trusted, leftPos + 20, y, 0xFFFFFF);

                    // ✅ Remove button
                    int btnX = leftPos + WIDTH - 50;
                    drawButton(guiGraphics, btnX, y - 2, 40, 12, "§c×", 0xFF5555, mouseX, mouseY);
                    String trustedName = trusted;
                    clickableRegions.add(new ClickableRegion(btnX, y - 2, btnX + 40, y + 10, () -> {
                        PlotNetworkHandler.sendToServer(new PlotTrustPacket(plotId, trustedName, PlotTrustPacket.TrustAction.REMOVE));
                    }));
                }
                y += 13;
                contentHeight += 13;
            }
        }

        // ✅ "Spieler hinzufügen" Button
        if (y >= startY - 30 && y < endY + 30) {
            drawButton(guiGraphics, leftPos + 10, y + 5, WIDTH - 20, 18, Component.translatable("gui.app.settings.add_player").getString(), 0x55FFFF, mouseX, mouseY);
            clickableRegions.add(new ClickableRegion(leftPos + 10, y + 5, leftPos + WIDTH - 10, y + 23, () -> {
                minecraft.setScreen(new InputDialogScreen(this, Component.translatable("gui.app.settings.trust_player").getString(), Component.translatable("gui.app.settings.enter_username").getString(),
                    InputDialogScreen.InputType.TEXT, playerName -> {
                        PlotNetworkHandler.sendToServer(new PlotTrustPacket(plotId, playerName, PlotTrustPacket.TrustAction.ADD));
                    }));
            }));
        }
        y += 30;
        contentHeight += 30;

        // ═══════════════════════════════════════════════════════════════════════════
        // PLOT UMBENENNEN
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.plot_name").getString(), leftPos + 15, y, 0xFF55FF);
        }
        y += 15;
        contentHeight += 15;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.current").getString() + currentPlot.getPlotName(), leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        // ✅ "Umbenennen" Button
        if (y >= startY - 30 && y < endY + 30) {
            drawButton(guiGraphics, leftPos + 10, y + 3, WIDTH - 20, 18, Component.translatable("gui.app.settings.rename").getString(), 0xFFAA00, mouseX, mouseY);
            clickableRegions.add(new ClickableRegion(leftPos + 10, y + 3, leftPos + WIDTH - 10, y + 21, () -> {
                minecraft.setScreen(new InputDialogScreen(this, Component.translatable("gui.app.settings.rename_plot").getString(), Component.translatable("gui.app.settings.enter_new_name").getString(),
                    InputDialogScreen.InputType.TEXT, newName -> {
                        PlotNetworkHandler.sendToServer(new PlotRenamePacket(plotId, newName));
                    }));
            }));
        }
        y += 25;
        contentHeight += 25;

        // ═══════════════════════════════════════════════════════════════════════════
        // PLOT BESCHREIBUNG
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.description").getString(), leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        String desc = currentPlot.getDescription();
        if (desc != null && !desc.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7" + desc, leftPos + 15, y, 0xAAAAAA);
            }
            y += 12;
            contentHeight += 12;
        } else {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.no_description").getString(), leftPos + 15, y, 0x666666);
            }
            y += 12;
            contentHeight += 12;
        }

        // ✅ "Beschreibung ändern" Button
        if (y >= startY - 30 && y < endY + 30) {
            drawButton(guiGraphics, leftPos + 10, y + 3, WIDTH - 20, 18, Component.translatable("gui.app.settings.change_description").getString(), 0x55FF55, mouseX, mouseY);
            clickableRegions.add(new ClickableRegion(leftPos + 10, y + 3, leftPos + WIDTH - 10, y + 21, () -> {
                minecraft.setScreen(new InputDialogScreen(this, Component.translatable("gui.app.settings.description_label").getString(), Component.translatable("gui.app.settings.enter_description").getString(),
                    InputDialogScreen.InputType.TEXT, description -> {
                        PlotNetworkHandler.sendToServer(new PlotDescriptionPacket(plotId, description));
                    }));
            }));
        }
        y += 30;
        contentHeight += 30;

        // ═══════════════════════════════════════════════════════════════════════════
        // PLOT AUFGEBEN
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.abandon_plot").getString(), leftPos + 15, y, 0xFF5555);
        }
        y += 15;
        contentHeight += 15;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x44330000);
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.warning_irreversible").getString(), leftPos + 15, y + 5, 0x666666);
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.returned_to_server").getString(), leftPos + 15, y + 15, 0x666666);
        }
        y += 35;
        contentHeight += 35;

        // ✅ "Plot aufgeben" Button (ROT)
        if (y >= startY - 30 && y < endY + 30) {
            drawButton(guiGraphics, leftPos + 10, y, WIDTH - 20, 18, Component.translatable("gui.app.settings.abandon_button").getString(), 0xFF5555, mouseX, mouseY);
            clickableRegions.add(new ClickableRegion(leftPos + 10, y, leftPos + WIDTH - 10, y + 18, () -> {
                minecraft.setScreen(new ConfirmDialogScreen(this, Component.translatable("gui.app.settings.confirm_abandon").getString(),
                    Component.translatable("gui.app.settings.confirm_message").getString(),
                    Component.translatable("gui.app.settings.returned_warning").getString(),
                    () -> PlotNetworkHandler.sendToServer(new PlotAbandonPacket(plotId)),
                    null));
            }));
        }
        y += 25;
        contentHeight += 25;

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 2: BENACHRICHTIGUNGEN
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderNotificationsTab(GuiGraphics guiGraphics, int startY, int endY, int mouseX, int mouseY) {
        sliderRegions.clear(); // Clear slider regions before re-rendering
        int y = startY - scrollOffset;
        int contentHeight = 0;

        // ═══════════════════════════════════════════════════════════════════════════
        // UTILITY-WARNUNGEN
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.utility_warnings").getString(), leftPos + 15, y, 0xFFAA00);
        }
        y += 18;
        contentHeight += 18;

        // ✅ An/Aus Toggle (Checkbox)
        if (y >= startY - 30 && y < endY + 30) {
            String checkBox = utilityWarningsEnabled ? "§a[✓]" : "§7[ ]";
            guiGraphics.drawString(this.font, checkBox + Component.translatable("gui.app.settings.utility_warnings_enabled").getString(), leftPos + 15, y, 0xFFFFFF);

            clickableRegions.add(new ClickableRegion(leftPos + 15, y - 2, leftPos + WIDTH - 10, y + 10, () -> {
                utilityWarningsEnabled = !utilityWarningsEnabled;
                saveSettings(); // Sende Settings zum Server
            }));
        }
        y += 15;
        contentHeight += 15;

        // Info über Warnungen
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.warnings_info1").getString(), leftPos + 15, y, 0x666666);
        }
        y += 11;
        contentHeight += 11;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.warnings_info2").getString(), leftPos + 15, y, 0x666666);
        }
        y += 18;
        contentHeight += 18;

        // ═══════════════════════════════════════════════════════════════════════════
        // SCHWELLENWERTE
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.thresholds").getString(), leftPos + 15, y, 0x55FFFF);
        }
        y += 18;
        contentHeight += 18;

        // Strom-Schwellenwert
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x33333333);
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.power_warning").getString(), leftPos + 15, y + 4, 0xFFAA00);
            guiGraphics.drawString(this.font, "§f" + String.format("%.0f", electricityWarningThreshold) + Component.translatable("gui.app.settings.kwh").getString(), leftPos + 130, y + 4, 0xFFFFFF);

            // Mini-Balken (Interaktiv!)
            int barWidth = WIDTH - 40;
            int filledWidth = (int) ((electricityWarningThreshold / 500.0) * barWidth);
            int barX = leftPos + 15;
            int barY = y + 18;

            guiGraphics.fill(barX, barY, barX + barWidth, barY + 6, 0x44666666);
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + 6, 0xAAFFAA00);

            // Registriere Slider (0-500 kWh)
            sliderRegions.add(new SliderRegion(barX, barY, barWidth, 0, 500,
                value -> {
                    electricityWarningThreshold = value;
                    saveSettings(); // Sende Settings zum Server
                }));
        }
        y += 35;
        contentHeight += 35;

        // Wasser-Schwellenwert
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x33333333);
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.water_warning").getString(), leftPos + 15, y + 4, 0x55AAFF);
            guiGraphics.drawString(this.font, "§f" + String.format("%.0f", waterWarningThreshold) + Component.translatable("gui.app.settings.liters").getString(), leftPos + 135, y + 4, 0xFFFFFF);

            // Mini-Balken (Interaktiv!)
            int barWidth = WIDTH - 40;
            int filledWidth = (int) ((waterWarningThreshold / 2000.0) * barWidth);
            int barX = leftPos + 15;
            int barY = y + 18;

            guiGraphics.fill(barX, barY, barX + barWidth, barY + 6, 0x44666666);
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + 6, 0xAA55AAFF);

            // Registriere Slider (0-2000 L)
            sliderRegions.add(new SliderRegion(barX, barY, barWidth, 0, 2000,
                value -> {
                    waterWarningThreshold = value;
                    saveSettings(); // Sende Settings zum Server
                }));
        }
        y += 38;
        contentHeight += 38;

        // Info-Text
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.threshold_info1").getString(), leftPos + 15, y, 0x666666);
        }
        y += 11;
        contentHeight += 11;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.threshold_info2").getString(), leftPos + 15, y, 0x666666);
        }
        y += 20;
        contentHeight += 20;

        // ═══════════════════════════════════════════════════════════════════════════
        // VERDÄCHTIGKEITS-HINWEIS
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.police_heat").getString(), leftPos + 15, y, 0xFF5555);
        }
        y += 15;
        contentHeight += 15;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 45, 0x44330000);
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.heat_info1").getString(), leftPos + 15, y + 5, 0x666666);
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.heat_info2").getString(), leftPos + 15, y + 15, 0x666666);
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.heat_threshold").getString(), leftPos + 15, y + 30, 0xAA5555);
        }
        y += 50;
        contentHeight += 50;

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 3: KONTO
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderAccountTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        // ═══════════════════════════════════════════════════════════════════════════
        // KONTOSTAND
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.balance").getString(), leftPos + 15, y, 0xFFAA00);
        }
        y += 18;
        contentHeight += 18;

        // Großer Kontostand-Display
        if (y >= startY - 10 && y < endY) {
            // ✅ Lade echten Kontostand von EconomyManager
            double accountBalance = 0.0;
            if (minecraft.player != null) {
                accountBalance = EconomyManager.getBalance(minecraft.player.getUUID());
            }

            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 50, 0x44228B22);
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.settings.available").getString(), leftPos + WIDTH / 2, y + 8, 0xFFFFFF);

            // Großer Betrag
            String balanceStr = String.format("§a§l%.2f €", accountBalance);
            guiGraphics.drawCenteredString(this.font, balanceStr, leftPos + WIDTH / 2, y + 25, 0x55FF55);

            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.settings.bank_name").getString(), leftPos + WIDTH / 2, y + 40, 0x666666);
        }
        y += 58;
        contentHeight += 58;

        // ═══════════════════════════════════════════════════════════════════════════
        // MONATLICHE KOSTEN ÜBERSICHT
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.running_costs").getString(), leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Berechne geschätzte monatliche Kosten
        double totalDailyElec = 0;
        double totalDailyWater = 0;

        for (PlotRegion plot : myPlots) {
            Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plot.getPlotId());
            if (dataOpt.isPresent()) {
                PlotUtilityData data = dataOpt.get();
                totalDailyElec += data.get7DayAverageElectricity();
                totalDailyWater += data.get7DayAverageWater();
            }
        }

        double dailyCost = (totalDailyElec * 0.35) + (totalDailyWater * 0.005);
        double weeklyCost = dailyCost * 7;
        double monthlyCost = dailyCost * 30;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 55, 0x33333333);

            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.daily").getString(), leftPos + 15, y + 5, 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("§e%.2f €", dailyCost), leftPos + 100, y + 5, 0xFFAA00);

            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.weekly").getString(), leftPos + 15, y + 18, 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("§e%.2f €", weeklyCost), leftPos + 100, y + 18, 0xFFAA00);

            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.monthly").getString(), leftPos + 15, y + 31, 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("§e%.2f €", monthlyCost), leftPos + 100, y + 31, 0xFFAA00);

            // Reichweite - lade echten Kontostand
            double currentBalance = minecraft.player != null ? EconomyManager.getBalance(minecraft.player.getUUID()) : 0.0;
            int daysUntilEmpty = dailyCost > 0 ? (int) (currentBalance / dailyCost) : 999;
            String reichweiteColor = daysUntilEmpty < 7 ? "§c" : (daysUntilEmpty < 30 ? "§e" : "§a");
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.range").getString() + reichweiteColor + daysUntilEmpty + Component.translatable("gui.app.settings.days").getString(), leftPos + 15, y + 44, 0x888888);
        }
        y += 60;
        contentHeight += 60;

        // ═══════════════════════════════════════════════════════════════════════════
        // GRUNDSTÜCKE
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.property").getString(), leftPos + 15, y, 0x55FFFF);
        }
        y += 15;
        contentHeight += 15;

        if (myPlots.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, Component.translatable("gui.app.settings.no_properties").getString(), leftPos + 15, y, 0x666666);
            }
            y += 12;
            contentHeight += 12;
        } else {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§a" + myPlots.size() + Component.translatable("gui.app.settings.properties_count").getString(), leftPos + 15, y, 0x55FF55);
            }
            y += 12;
            contentHeight += 12;

            // Liste der Grundstücke
            for (PlotRegion plot : myPlots) {
                if (y >= startY - 10 && y < endY) {
                    guiGraphics.drawString(this.font, "§7● " + plot.getPlotName(), leftPos + 20, y, 0xAAAAAA);
                }
                y += 11;
                contentHeight += 11;
            }
        }
        y += 10;
        contentHeight += 10;

        // ═══════════════════════════════════════════════════════════════════════════
        // HINWEIS
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x33222222);
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.settings.earn_money").getString(), leftPos + WIDTH / 2, y + 5, 0x666666);
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.settings.sell_products").getString(), leftPos + WIDTH / 2, y + 17, 0xAAAAAA);
        }
        y += 35;
        contentHeight += 35;

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCROLL HANDLING
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta * SCROLL_SPEED));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            // Check sliders first
            for (SliderRegion slider : sliderRegions) {
                if (slider.contains((int) mouseX, (int) mouseY)) {
                    double newValue = slider.getValue((int) mouseX);
                    slider.onValueChange.accept(newValue);
                    return true;
                }
            }

            // Then check regular clickable regions
            for (ClickableRegion region : clickableRegions) {
                if (region.contains((int) mouseX, (int) mouseY)) {
                    region.onClick.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Sendet aktuelle Einstellungen zum Server zum persistenten Speichern
     */
    private void saveSettings() {
        // Sende zum Server
        PlayerSettingsNetworkHandler.sendToServer(
            new PlayerSettingsPacket(
                utilityWarningsEnabled,
                electricityWarningThreshold,
                waterWarningThreshold
            )
        );

        // Aktualisiere auch lokal den Client-Cache
        ClientPlayerSettings.utilityWarningsEnabled = utilityWarningsEnabled;
        ClientPlayerSettings.electricityThreshold = electricityWarningThreshold;
        ClientPlayerSettings.waterThreshold = waterWarningThreshold;
    }

    // Helper method to draw a button-like region
    private void drawButton(GuiGraphics guiGraphics, int x, int y, int width, int height,
                           String text, int color, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        guiGraphics.fill(x, y, x + width, y + height, hovered ? 0xFF4A90E2 : 0xFF333333);
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF555555);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF111111);
        guiGraphics.drawCenteredString(this.font, text, x + width / 2, y + (height - 8) / 2, hovered ? 0xFFFFFF : color);
    }
}
