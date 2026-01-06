package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.client.screen.ConfirmDialogScreen;
import de.rolandsw.schedulemc.client.screen.InputDialogScreen;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.network.PlotAbandonPacket;
import de.rolandsw.schedulemc.region.network.PlotDescriptionPacket;
import de.rolandsw.schedulemc.region.network.PlotNetworkHandler;
import de.rolandsw.schedulemc.region.network.PlotRenamePacket;
import de.rolandsw.schedulemc.region.network.PlotSalePacket;
import de.rolandsw.schedulemc.region.network.PlotTrustPacket;
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

    // Color Constants
    private static final int COLOR_APP_FRAME = 0xFF1C1C1C;
    private static final int COLOR_APP_BACKGROUND = 0xFF2A2A2A;
    private static final int COLOR_APP_HEADER = 0xFF1A1A1A;
    private static final int COLOR_TAB_ACTIVE_BLUE = 0xFF4A90E2;
    private static final int COLOR_BUTTON_DEFAULT = 0xFF333333;
    private static final int COLOR_BUTTON_BORDER_TOP = 0xFF555555;
    private static final int COLOR_BUTTON_BORDER_BOTTOM = 0xFF111111;

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;

    // Tab-System
    private int currentTab = 0;
    private static final String[] TAB_NAMES = {"Plot", "Meldung", "Konto"};
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

        int centeredTop = (this.height - HEIGHT) / 2;
        int minTop = MARGIN_TOP + BORDER_SIZE;
        int maxTop = this.height - HEIGHT - BORDER_SIZE - MARGIN_BOTTOM;
        this.topPos = Math.max(minTop, Math.min(centeredTop, maxTop));

        // Cache data
        refreshData();

        // Tab-Buttons
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(
                Component.literal(TAB_NAMES[i]),
                button -> {
                    currentTab = tabIndex;
                    scrollOffset = 0;
                    refreshData();
                }
            ).bounds(leftPos + 10 + (i * TAB_WIDTH), topPos + 30, TAB_WIDTH - 2, TAB_HEIGHT).build());
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.literal("<< Zurück"), button -> {
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
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, COLOR_APP_FRAME);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, COLOR_APP_BACKGROUND);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, COLOR_APP_HEADER);
        guiGraphics.drawCenteredString(this.font, "§f§lEinstellungen", leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        // Tab-Hintergrund (aktiver Tab hervorheben)
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tabX = leftPos + 10 + (i * TAB_WIDTH);
            int tabY = topPos + 30;
            if (i == currentTab) {
                guiGraphics.fill(tabX - 1, tabY - 1, tabX + TAB_WIDTH - 1, tabY + TAB_HEIGHT + 1, COLOR_TAB_ACTIVE_BLUE);
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

    /**
     * Renders the plot settings tab with sale/rent options, trusted players, rename, description, and abandon.
     * <p>
     * This method delegates rendering to specialized helper methods:
     * <ul>
     *   <li>{@link #renderSaleRentSection} - Sale/rent status and buttons</li>
     *   <li>{@link #renderTrustedPlayersSection} - Trusted players list and add button</li>
     *   <li>{@link #renderPlotRenameSection} - Plot rename functionality</li>
     *   <li>{@link #renderPlotDescriptionSection} - Plot description display and edit</li>
     *   <li>{@link #renderPlotAbandonSection} - Plot abandonment with confirmation</li>
     * </ul>
     *
     * @param guiGraphics   the graphics context
     * @param startY        the start Y position for rendering
     * @param endY          the end Y position for rendering
     * @param mouseX        the mouse X coordinate
     * @param mouseY        the mouse Y coordinate
     */
    private void renderPlotSettingsTab(GuiGraphics guiGraphics, int startY, int endY, int mouseX, int mouseY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String playerUUID = mc.player.getUUID().toString();

        // Aktueller Plot Check
        if (currentPlot == null) {
            guiGraphics.drawCenteredString(this.font, "§7Kein Plot gefunden", leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            guiGraphics.drawCenteredString(this.font, "§8Stehe auf einem Plot", leftPos + WIDTH / 2, y + 35, 0x666666);
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
                guiGraphics.drawString(this.font, "§c✗ Nicht dein Grundstück", leftPos + 15, y + 8, 0xFF5555);
            }
            y += 30;
            contentHeight += 30;
            maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
            return;
        }

        // Render all settings sections using helper methods
        y = renderSaleRentSection(guiGraphics, currentPlot, plotId, y, startY, endY, mouseX, mouseY);
        contentHeight = y - (startY - scrollOffset);

        y = renderTrustedPlayersSection(guiGraphics, currentPlot, plotId, y, startY, endY, mouseX, mouseY);
        contentHeight = y - (startY - scrollOffset);

        y = renderPlotRenameSection(guiGraphics, currentPlot, plotId, y, startY, endY, mouseX, mouseY);
        contentHeight = y - (startY - scrollOffset);

        y = renderPlotDescriptionSection(guiGraphics, currentPlot, plotId, y, startY, endY, mouseX, mouseY);
        contentHeight = y - (startY - scrollOffset);

        y = renderPlotAbandonSection(guiGraphics, plotId, y, startY, endY, mouseX, mouseY);
        contentHeight = y - (startY - scrollOffset);

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ==================== Plot Settings Helper Methods ====================

    /**
     * Renders sale/rent section with buttons.
     * @return new y position
     */
    private int renderSaleRentSection(GuiGraphics guiGraphics, PlotRegion currentPlot, String plotId, int y, int startY, int endY, int mouseX, int mouseY) {
        // Separator
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;

        // Section header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§e§l🏷 VERKAUF / MIETE", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;

        // Current status
        String saleStatus;
        if (currentPlot.isForSale()) {
            saleStatus = "§a● Zum Verkauf (" + String.format("%.0f€", currentPlot.getSalePrice()) + ")";
        } else if (currentPlot.isForRent()) {
            if (currentPlot.isRented()) {
                saleStatus = "§e● Vermietet";
            } else {
                saleStatus = "§d● Zu vermieten (" + String.format("%.0f€/Tag", currentPlot.getRentPricePerDay()) + ")";
            }
        } else {
            saleStatus = "§7● Privat (nicht angeboten)";
        }

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§7Status: " + saleStatus, leftPos + 15, y, 0xAAAAAA);
        }
        y += 12;

        // Interactive buttons
        if (y >= startY - 30 && y < endY + 30) {
            int btnY = y;
            int btnWidth = WIDTH - 20;

            if (!currentPlot.isForSale()) {
                drawButton(guiGraphics, leftPos + 10, btnY, btnWidth, 18, "§a🏷 Zum Verkauf stellen", 0x55FF55, mouseX, mouseY);
                clickableRegions.add(new ClickableRegion(leftPos + 10, btnY, leftPos + 10 + btnWidth, btnY + 18, () -> {
                    minecraft.setScreen(new InputDialogScreen(this, "Verkaufspreis", "Preis in Euro eingeben:",
                        InputDialogScreen.InputType.NUMBER, price -> {
                            PlotNetworkHandler.sendToServer(new PlotSalePacket(plotId, Double.parseDouble(price), PlotSalePacket.SaleType.SELL));
                        }));
                }));
                btnY += 20;
            }

            if (!currentPlot.isForRent()) {
                drawButton(guiGraphics, leftPos + 10, btnY, btnWidth, 18, "§d🏠 Zur Miete stellen", 0xFF55FF, mouseX, mouseY);
                clickableRegions.add(new ClickableRegion(leftPos + 10, btnY, leftPos + 10 + btnWidth, btnY + 18, () -> {
                    minecraft.setScreen(new InputDialogScreen(this, "Mietpreis", "Preis pro Tag in Euro:",
                        InputDialogScreen.InputType.NUMBER, price -> {
                            PlotNetworkHandler.sendToServer(new PlotSalePacket(plotId, Double.parseDouble(price), PlotSalePacket.SaleType.RENT));
                        }));
                }));
                btnY += 20;
            }

            if (currentPlot.isForSale() || currentPlot.isForRent()) {
                drawButton(guiGraphics, leftPos + 10, btnY, btnWidth, 18, "§c✗ Angebot beenden", 0xFF5555, mouseX, mouseY);
                clickableRegions.add(new ClickableRegion(leftPos + 10, btnY, leftPos + 10 + btnWidth, btnY + 18, () -> {
                    PlotNetworkHandler.sendToServer(new PlotSalePacket(plotId, 0, PlotSalePacket.SaleType.CANCEL));
                }));
                btnY += 20;
            }

            y = btnY;
        } else {
            y += 60;
        }

        return y;
    }

    /**
     * Renders trusted players section with list and add/remove buttons.
     * @return new y position
     */
    private int renderTrustedPlayersSection(GuiGraphics guiGraphics, PlotRegion currentPlot, String plotId, int y, int startY, int endY, int mouseX, int mouseY) {
        // Separator
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;

        // Section header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§b§l👥 TRUSTED PLAYERS", leftPos + 15, y, 0x55FFFF);
        }
        y += 15;

        // Show trusted players with remove buttons
        Set<String> trustedPlayers = currentPlot.getTrustedPlayers();
        if (trustedPlayers.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8Keine vertrauenswürdigen Spieler", leftPos + 15, y, 0x666666);
            }
            y += 12;
        } else {
            for (String trusted : trustedPlayers) {
                if (y >= startY - 30 && y < endY + 30) {
                    guiGraphics.drawString(this.font, "§a● §f" + trusted, leftPos + 20, y, 0xFFFFFF);

                    int btnX = leftPos + WIDTH - 50;
                    drawButton(guiGraphics, btnX, y - 2, 40, 12, "§c×", 0xFF5555, mouseX, mouseY);
                    String trustedName = trusted;
                    clickableRegions.add(new ClickableRegion(btnX, y - 2, btnX + 40, y + 10, () -> {
                        PlotNetworkHandler.sendToServer(new PlotTrustPacket(plotId, trustedName, PlotTrustPacket.TrustAction.REMOVE));
                    }));
                }
                y += 13;
            }
        }

        // "Add player" button
        if (y >= startY - 30 && y < endY + 30) {
            drawButton(guiGraphics, leftPos + 10, y + 5, WIDTH - 20, 18, "§b+ Spieler hinzufügen", 0x55FFFF, mouseX, mouseY);
            clickableRegions.add(new ClickableRegion(leftPos + 10, y + 5, leftPos + WIDTH - 10, y + 23, () -> {
                minecraft.setScreen(new InputDialogScreen(this, "Spieler vertrauen", "Spielername eingeben:",
                    InputDialogScreen.InputType.TEXT, playerName -> {
                        PlotNetworkHandler.sendToServer(new PlotTrustPacket(plotId, playerName, PlotTrustPacket.TrustAction.ADD));
                    }));
            }));
        }
        y += 30;

        return y;
    }

    /**
     * Renders plot rename section with button.
     * @return new y position
     */
    private int renderPlotRenameSection(GuiGraphics guiGraphics, PlotRegion currentPlot, String plotId, int y, int startY, int endY, int mouseX, int mouseY) {
        // Separator
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;

        // Section header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§d§l✏ PLOT-NAME", leftPos + 15, y, 0xFF55FF);
        }
        y += 15;

        // Current name
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§7Aktuell: §f" + currentPlot.getPlotName(), leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;

        // Rename button
        if (y >= startY - 30 && y < endY + 30) {
            drawButton(guiGraphics, leftPos + 10, y + 3, WIDTH - 20, 18, "§e✏ Umbenennen", 0xFFAA00, mouseX, mouseY);
            clickableRegions.add(new ClickableRegion(leftPos + 10, y + 3, leftPos + WIDTH - 10, y + 21, () -> {
                minecraft.setScreen(new InputDialogScreen(this, "Plot umbenennen", "Neuen Namen eingeben:",
                    InputDialogScreen.InputType.TEXT, newName -> {
                        PlotNetworkHandler.sendToServer(new PlotNamePacket(plotId, newName));
                    }));
            }));
        }
        y += 25;

        return y;
    }

    /**
     * Renders plot description section with button.
     * @return new y position
     */
    private int renderPlotDescriptionSection(GuiGraphics guiGraphics, PlotRegion currentPlot, String plotId, int y, int startY, int endY, int mouseX, int mouseY) {
        // Separator
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;

        // Section header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§l📝 BESCHREIBUNG", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;

        // Current description
        String desc = currentPlot.getDescription();
        if (desc != null && !desc.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7" + desc, leftPos + 15, y, 0xAAAAAA);
            }
            y += 12;
        } else {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8Keine Beschreibung", leftPos + 15, y, 0x666666);
            }
            y += 12;
        }

        // "Change description" button
        if (y >= startY - 30 && y < endY + 30) {
            drawButton(guiGraphics, leftPos + 10, y + 3, WIDTH - 20, 18, "§a📝 Beschreibung ändern", 0x55FF55, mouseX, mouseY);
            clickableRegions.add(new ClickableRegion(leftPos + 10, y + 3, leftPos + WIDTH - 10, y + 21, () -> {
                minecraft.setScreen(new InputDialogScreen(this, "Beschreibung", "Beschreibung eingeben:",
                    InputDialogScreen.InputType.TEXT, description -> {
                        PlotNetworkHandler.sendToServer(new PlotDescriptionPacket(plotId, description));
                    }));
            }));
        }
        y += 30;

        return y;
    }

    /**
     * Renders plot abandon section with warning and button.
     * @return new y position
     */
    private int renderPlotAbandonSection(GuiGraphics guiGraphics, String plotId, int y, int startY, int endY, int mouseX, int mouseY) {
        // Separator
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;

        // Section header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§c§l🗑 PLOT AUFGEBEN", leftPos + 15, y, 0xFF5555);
        }
        y += 15;

        // Warning box
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x44330000);
            guiGraphics.drawString(this.font, "§8⚠ WARNUNG: Nicht rückgängig!", leftPos + 15, y + 5, 0x666666);
            guiGraphics.drawString(this.font, "§8Plot geht an Server zurück.", leftPos + 15, y + 15, 0x666666);
        }
        y += 35;

        // Abandon button
        if (y >= startY - 30 && y < endY + 30) {
            drawButton(guiGraphics, leftPos + 10, y, WIDTH - 20, 18, "§c🗑 Plot aufgeben", 0xFF5555, mouseX, mouseY);
            clickableRegions.add(new ClickableRegion(leftPos + 10, y, leftPos + WIDTH - 10, y + 18, () -> {
                minecraft.setScreen(new ConfirmDialogScreen(this, "⚠ WARNUNG",
                    "Plot wirklich aufgeben?\nDiese Aktion kann NICHT\nrückgängig gemacht werden!",
                    "Plot wird an Server zurückgegeben",
                    () -> PlotNetworkHandler.sendToServer(new PlotAbandonPacket(plotId)),
                    null));
            }));
        }
        y += 25;

        return y;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 2: BENACHRICHTIGUNGEN
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Renders the notifications tab with utility warnings, threshold sliders, and police heat warning.
     * <p>
     * This method delegates rendering to specialized helper methods:
     * <ul>
     *   <li>{@link #renderUtilityWarningsSection} - Utility warnings toggle and info</li>
     *   <li>{@link #renderThresholdsSection} - Electricity and water threshold sliders</li>
     *   <li>{@link #renderPoliceHeatSection} - Police heat warning about high consumption</li>
     * </ul>
     *
     * @param guiGraphics   the graphics context
     * @param startY        the start Y position for rendering
     * @param endY          the end Y position for rendering
     * @param mouseX        the mouse X coordinate
     * @param mouseY        the mouse Y coordinate
     */
    private void renderNotificationsTab(GuiGraphics guiGraphics, int startY, int endY, int mouseX, int mouseY) {
        sliderRegions.clear(); // Clear slider regions before re-rendering
        int y = startY - scrollOffset;
        int contentHeight = 0;

        // Render all notification sections using helper methods
        y = renderUtilityWarningsSection(guiGraphics, y, startY, endY, mouseX, mouseY);
        contentHeight = y - (startY - scrollOffset);

        y = renderThresholdsSection(guiGraphics, y, startY, endY);
        contentHeight = y - (startY - scrollOffset);

        y = renderPoliceHeatSection(guiGraphics, y, startY, endY);
        contentHeight = y - (startY - scrollOffset);

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ==================== Notification Settings Helper Methods ====================

    /**
     * Renders the utility warnings section with toggle checkbox and info text.
     * Shows settings for enabling/disabling utility consumption warnings.
     *
     * @param guiGraphics   the graphics context
     * @param y             the current Y position
     * @param startY        the start Y position for rendering
     * @param endY          the end Y position for rendering
     * @param mouseX        the mouse X coordinate
     * @param mouseY        the mouse Y coordinate
     * @return              the updated Y position after rendering
     */
    private int renderUtilityWarningsSection(GuiGraphics guiGraphics, int y, int startY, int endY, int mouseX, int mouseY) {
        // Header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§e§l⚠ UTILITY-WARNUNGEN", leftPos + 15, y, 0xFFAA00);
        }
        y += 18;

        // Toggle Checkbox
        if (y >= startY - 30 && y < endY + 30) {
            String checkBox = utilityWarningsEnabled ? "§a[✓]" : "§7[ ]";
            guiGraphics.drawString(this.font, checkBox + " §fUtility-Warnungen", leftPos + 15, y, 0xFFFFFF);

            clickableRegions.add(new ClickableRegion(leftPos + 15, y - 2, leftPos + WIDTH - 10, y + 10, () -> {
                utilityWarningsEnabled = !utilityWarningsEnabled;
                saveSettings();
            }));
        }
        y += 15;

        // Info text
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8Du erhältst Warnungen bei", leftPos + 15, y, 0x666666);
        }
        y += 11;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8hohem Strom-/Wasserverbrauch.", leftPos + 15, y, 0x666666);
        }
        y += 18;

        return y;
    }

    /**
     * Renders the threshold settings section with interactive sliders for electricity and water warnings.
     * Includes two sliders with visual feedback and info text about threshold functionality.
     *
     * @param guiGraphics   the graphics context
     * @param y             the current Y position
     * @param startY        the start Y position for rendering
     * @param endY          the end Y position for rendering
     * @return              the updated Y position after rendering
     */
    private int renderThresholdsSection(GuiGraphics guiGraphics, int y, int startY, int endY) {
        // Section separator + header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§b§l📊 SCHWELLENWERTE", leftPos + 15, y, 0x55FFFF);
        }
        y += 18;

        // Electricity threshold slider
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x33333333);
            guiGraphics.drawString(this.font, "§e⚡ Strom-Warnung ab:", leftPos + 15, y + 4, 0xFFAA00);
            guiGraphics.drawString(this.font, "§f" + String.format("%.0f kWh", electricityWarningThreshold), leftPos + 130, y + 4, 0xFFFFFF);

            int barWidth = WIDTH - 40;
            int filledWidth = (int) ((electricityWarningThreshold / 500.0) * barWidth);
            int barX = leftPos + 15;
            int barY = y + 18;

            guiGraphics.fill(barX, barY, barX + barWidth, barY + 6, 0x44666666);
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + 6, 0xAAFFAA00);

            sliderRegions.add(new SliderRegion(barX, barY, barWidth, 0, 500,
                value -> {
                    electricityWarningThreshold = value;
                    saveSettings();
                }));
        }
        y += 35;

        // Water threshold slider
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x33333333);
            guiGraphics.drawString(this.font, "§b💧 Wasser-Warnung ab:", leftPos + 15, y + 4, 0x55AAFF);
            guiGraphics.drawString(this.font, "§f" + String.format("%.0f L", waterWarningThreshold), leftPos + 135, y + 4, 0xFFFFFF);

            int barWidth = WIDTH - 40;
            int filledWidth = (int) ((waterWarningThreshold / 2000.0) * barWidth);
            int barX = leftPos + 15;
            int barY = y + 18;

            guiGraphics.fill(barX, barY, barX + barWidth, barY + 6, 0x44666666);
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + 6, 0xAA55AAFF);

            sliderRegions.add(new SliderRegion(barX, barY, barWidth, 0, 2000,
                value -> {
                    waterWarningThreshold = value;
                    saveSettings();
                }));
        }
        y += 38;

        // Info text
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8Bei Überschreitung siehst du", leftPos + 15, y, 0x666666);
        }
        y += 11;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8eine Warnung in der Finanz-App.", leftPos + 15, y, 0x666666);
        }
        y += 20;

        return y;
    }

    /**
     * Renders the police heat warning section showing the relationship between high consumption and police attention.
     * Displays critical thresholds that may attract police investigation.
     *
     * @param guiGraphics   the graphics context
     * @param y             the current Y position
     * @param startY        the start Y position for rendering
     * @param endY          the end Y position for rendering
     * @return              the updated Y position after rendering
     */
    private int renderPoliceHeatSection(GuiGraphics guiGraphics, int y, int startY, int endY) {
        // Section separator + header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§c§l🚨 POLIZEI-HEAT", leftPos + 15, y, 0xFF5555);
        }
        y += 15;

        // Warning box
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 45, 0x44330000);
            guiGraphics.drawString(this.font, "§8Hoher Verbrauch kann", leftPos + 15, y + 5, 0x666666);
            guiGraphics.drawString(this.font, "§8Aufmerksamkeit erregen!", leftPos + 15, y + 15, 0x666666);
            guiGraphics.drawString(this.font, "§c>200 kWh §8oder §c>1000 L", leftPos + 15, y + 30, 0xAA5555);
        }
        y += 50;

        return y;
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
            guiGraphics.drawString(this.font, "§6§l💰 KONTOSTAND", leftPos + 15, y, 0xFFAA00);
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
            guiGraphics.drawCenteredString(this.font, "§fVerfügbar:", leftPos + WIDTH / 2, y + 8, 0xFFFFFF);

            // Großer Betrag
            String balanceStr = String.format("§a§l%.2f €", accountBalance);
            guiGraphics.drawCenteredString(this.font, balanceStr, leftPos + WIDTH / 2, y + 25, 0x55FF55);

            guiGraphics.drawCenteredString(this.font, "§8Bank of Schedule", leftPos + WIDTH / 2, y + 40, 0x666666);
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
            guiGraphics.drawString(this.font, "§e§l📊 LAUFENDE KOSTEN", leftPos + 15, y, 0xFFAA00);
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

            guiGraphics.drawString(this.font, "§7Täglich:", leftPos + 15, y + 5, 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("§e%.2f €", dailyCost), leftPos + 100, y + 5, 0xFFAA00);

            guiGraphics.drawString(this.font, "§7Wöchentlich:", leftPos + 15, y + 18, 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("§e%.2f €", weeklyCost), leftPos + 100, y + 18, 0xFFAA00);

            guiGraphics.drawString(this.font, "§7Monatlich (30d):", leftPos + 15, y + 31, 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("§e%.2f €", monthlyCost), leftPos + 100, y + 31, 0xFFAA00);

            // Reichweite - lade echten Kontostand
            double currentBalance = minecraft.player != null ? EconomyManager.getBalance(minecraft.player.getUUID()) : 0.0;
            int daysUntilEmpty = dailyCost > 0 ? (int) (currentBalance / dailyCost) : 999;
            String reichweiteColor = daysUntilEmpty < 7 ? "§c" : (daysUntilEmpty < 30 ? "§e" : "§a");
            guiGraphics.drawString(this.font, "§8Reichweite: " + reichweiteColor + daysUntilEmpty + " Tage", leftPos + 15, y + 44, 0x888888);
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
            guiGraphics.drawString(this.font, "§b§l🏠 EIGENTUM", leftPos + 15, y, 0x55FFFF);
        }
        y += 15;
        contentHeight += 15;

        if (myPlots.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8Keine Grundstücke", leftPos + 15, y, 0x666666);
            }
            y += 12;
            contentHeight += 12;
        } else {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§a" + myPlots.size() + " Grundstück(e)", leftPos + 15, y, 0x55FF55);
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
            guiGraphics.drawCenteredString(this.font, "§8Geld verdienen:", leftPos + WIDTH / 2, y + 5, 0x666666);
            guiGraphics.drawCenteredString(this.font, "§7Produkte verkaufen!", leftPos + WIDTH / 2, y + 17, 0xAAAAAA);
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
        guiGraphics.fill(x, y, x + width, y + height, hovered ? COLOR_TAB_ACTIVE_BLUE : COLOR_BUTTON_DEFAULT);
        guiGraphics.fill(x, y, x + width, y + 1, COLOR_BUTTON_BORDER_TOP);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, COLOR_BUTTON_BORDER_BOTTOM);
        guiGraphics.drawCenteredString(this.font, text, x + width / 2, y + (height - 8) / 2, hovered ? 0xFFFFFF : color);
    }
}
