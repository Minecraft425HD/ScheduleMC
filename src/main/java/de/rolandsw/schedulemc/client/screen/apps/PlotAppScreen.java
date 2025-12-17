package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.utility.PlotUtilityData;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;
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

/**
 * Plot App - Immobilien-Verwaltung auf dem Smartphone
 *
 * Features:
 * - 4 Tabs mit Scroll-Support
 * - Tab 1: Aktueller Plot (Info + Verbrauch wenn Besitzer)
 * - Tab 2: Verfügbare Immobilien (Markt)
 * - Tab 3: Meine Plots (Übersicht eigener Grundstücke)
 * - Tab 4: Finanzen (Rechnungen, Warnungen, 7-Tage-Verlauf)
 */
@OnlyIn(Dist.CLIENT)
public class PlotAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;

    // Tab-System
    private int currentTab = 0;
    private static final String[] TAB_NAMES = {"Plot", "Markt", "Meine", "Geld"};
    private static final int TAB_HEIGHT = 22;
    private static final int TAB_WIDTH = 45;

    // Scrolling
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_SPEED = 15;
    private static final int CONTENT_HEIGHT = 160; // Sichtbarer Bereich

    private int leftPos;
    private int topPos;

    // Cached Data
    private PlotRegion currentPlot;
    private PlotUtilityData utilityData;
    private List<PlotRegion> availablePlots;
    private List<PlotRegion> myPlots;

    public PlotAppScreen(Screen parent) {
        super(Component.literal("Plot App"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

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
        if (currentPlot != null) {
            Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(currentPlot.getPlotId());
            utilityData = dataOpt.orElse(null);
        } else {
            utilityData = null;
        }

        // Verfügbare Plots (zum Verkauf oder ohne Besitzer)
        availablePlots = new ArrayList<>();
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (!plot.hasOwner() || plot.isForSale() || plot.isForRent()) {
                availablePlots.add(plot);
            }
        }

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
        guiGraphics.drawCenteredString(this.font, "§6§lImmobilien", leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        // Tab-Hintergrund (aktiver Tab hervorheben)
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tabX = leftPos + 10 + (i * TAB_WIDTH);
            int tabY = topPos + 30;
            if (i == currentTab) {
                guiGraphics.fill(tabX - 1, tabY - 1, tabX + TAB_WIDTH - 1, tabY + TAB_HEIGHT + 1, 0xFF4A90E2);
            }
        }

        // Content-Bereich (mit Clipping simuliert)
        int contentY = topPos + 55;
        int contentEndY = topPos + HEIGHT - 40;

        // Render Tab-Content
        switch (currentTab) {
            case 0 -> renderCurrentPlotTab(guiGraphics, contentY, contentEndY);
            case 1 -> renderMarketTab(guiGraphics, contentY, contentEndY);
            case 2 -> renderMyPlotsTab(guiGraphics, contentY, contentEndY);
            case 3 -> renderFinanceTab(guiGraphics, contentY, contentEndY);
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
    // TAB 1: AKTUELLER PLOT
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderCurrentPlotTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        if (currentPlot == null) {
            guiGraphics.drawCenteredString(this.font, "§7Kein Plot gefunden", leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            guiGraphics.drawCenteredString(this.font, "§8Stehe auf einem Plot", leftPos + WIDTH / 2, y + 35, 0x666666);
            maxScroll = 0;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean isOwner = mc.player != null && mc.player.getUUID().toString().equals(currentPlot.getOwnerUUID());

        // Plot-Name
        if (y >= startY - 15 && y < endY) {
            guiGraphics.drawString(this.font, "§6§l" + currentPlot.getPlotName(), leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // ID
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8ID: " + currentPlot.getPlotId(), leftPos + 15, y, 0x666666);
        }
        y += 12;
        contentHeight += 12;

        // Besitzer
        if (y >= startY - 10 && y < endY) {
            String owner = currentPlot.hasOwner() ? currentPlot.getOwnerName() : "§cKein Besitzer";
            guiGraphics.drawString(this.font, "§7Besitzer: §f" + owner, leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        // Größe
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§7Größe: §e" + String.format("%,d", currentPlot.getVolume()) + " Blöcke", leftPos + 15, y, 0xFFFFFF);
        }
        y += 15;
        contentHeight += 15;

        // Status (Verkauf/Miete)
        if (!currentPlot.hasOwner()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x44228B22);
                guiGraphics.drawString(this.font, "§a⚡ ZUM VERKAUF", leftPos + 15, y + 3, 0x00FF00);
                guiGraphics.drawString(this.font, "§7Preis: §e" + String.format("%.0f", currentPlot.getPrice()) + "€", leftPos + 15, y + 14, 0xFFFFFF);
            }
            y += 30;
            contentHeight += 30;
        } else if (currentPlot.isForSale()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x44228B22);
                guiGraphics.drawString(this.font, "§a⚡ ZUM VERKAUF", leftPos + 15, y + 3, 0x00FF00);
                guiGraphics.drawString(this.font, "§7Preis: §e" + String.format("%.0f", currentPlot.getSalePrice()) + "€", leftPos + 15, y + 14, 0xFFFFFF);
            }
            y += 30;
            contentHeight += 30;
        } else if (currentPlot.isForRent() && !currentPlot.isRented()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x44C71585);
                guiGraphics.drawString(this.font, "§d⚡ ZU VERMIETEN", leftPos + 15, y + 3, 0xFF00FF);
                guiGraphics.drawString(this.font, "§7Miete: §e" + String.format("%.0f", currentPlot.getRentPricePerDay()) + "€/Tag", leftPos + 15, y + 14, 0xFFFFFF);
            }
            y += 30;
            contentHeight += 30;
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // UTILITY-VERBRAUCH (nur für Besitzer)
        // ═══════════════════════════════════════════════════════════════════════════
        if (isOwner && utilityData != null) {
            // Trennlinie
            if (y >= startY && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x66FFFFFF);
            }
            y += 8;
            contentHeight += 8;

            // Überschrift
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§b§l⚡ VERBRAUCH", leftPos + 15, y, 0x55FFFF);
            }
            y += 14;
            contentHeight += 14;

            // Strom
            if (y >= startY - 10 && y < endY) {
                double elec = utilityData.getCurrentElectricity();
                double avgElec = utilityData.get7DayAverageElectricity();
                guiGraphics.drawString(this.font, "§e⚡ Strom:", leftPos + 15, y, 0xFFAA00);
                guiGraphics.drawString(this.font, PlotUtilityManager.formatElectricity(elec), leftPos + 80, y, 0xFFFFFF);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8  7-Tage-Ø: " + PlotUtilityManager.formatElectricity(utilityData.get7DayAverageElectricity()), leftPos + 15, y, 0x888888);
            }
            y += 13;
            contentHeight += 13;

            // Wasser
            if (y >= startY - 10 && y < endY) {
                double water = utilityData.getCurrentWater();
                guiGraphics.drawString(this.font, "§b💧 Wasser:", leftPos + 15, y, 0x55AAFF);
                guiGraphics.drawString(this.font, PlotUtilityManager.formatWater(water), leftPos + 80, y, 0xFFFFFF);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8  7-Tage-Ø: " + PlotUtilityManager.formatWater(utilityData.get7DayAverageWater()), leftPos + 15, y, 0x888888);
            }
            y += 13;
            contentHeight += 13;

            // Verbraucher-Anzahl
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7Geräte: §f" + utilityData.getConsumerCount(), leftPos + 15, y, 0xFFFFFF);
            }
            y += 15;
            contentHeight += 15;
        }

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 2: IMMOBILIEN-MARKT
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderMarketTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        if (availablePlots.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "§7Keine Angebote", leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            maxScroll = 0;
            return;
        }

        // Header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§e" + availablePlots.size() + " Angebote", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        for (PlotRegion plot : availablePlots) {
            if (y >= startY - 40 && y < endY) {
                // Plot-Box
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 35, 0x44333333);

                // Name
                guiGraphics.drawString(this.font, "§f" + plot.getPlotName(), leftPos + 15, y + 3, 0xFFFFFF);

                // Status & Preis
                String status;
                String price;
                if (!plot.hasOwner()) {
                    status = "§a[KAUF]";
                    price = String.format("%.0f€", plot.getPrice());
                } else if (plot.isForSale()) {
                    status = "§a[KAUF]";
                    price = String.format("%.0f€", plot.getSalePrice());
                } else {
                    status = "§d[MIETE]";
                    price = String.format("%.0f€/Tag", plot.getRentPricePerDay());
                }

                guiGraphics.drawString(this.font, status, leftPos + 15, y + 14, 0xFFFFFF);
                guiGraphics.drawString(this.font, "§e" + price, leftPos + 70, y + 14, 0xFFFFFF);

                // Größe
                guiGraphics.drawString(this.font, "§8" + String.format("%,d", plot.getVolume()) + " Blöcke", leftPos + 15, y + 25, 0x666666);
            }
            y += 40;
            contentHeight += 40;
        }

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 3: MEINE PLOTS
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderMyPlotsTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        if (myPlots.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "§7Du besitzt keine Plots", leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            guiGraphics.drawCenteredString(this.font, "§8Kaufe einen im Markt-Tab!", leftPos + WIDTH / 2, y + 35, 0x666666);
            maxScroll = 0;
            return;
        }

        // Header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6" + myPlots.size() + " Grundstücke", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        for (PlotRegion plot : myPlots) {
            if (y >= startY - 50 && y < endY) {
                // Plot-Box
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 45, 0x44333333);

                // Name
                guiGraphics.drawString(this.font, "§6§l" + plot.getPlotName(), leftPos + 15, y + 3, 0xFFAA00);

                // Utility-Verbrauch
                Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plot.getPlotId());
                if (dataOpt.isPresent()) {
                    PlotUtilityData data = dataOpt.get();
                    guiGraphics.drawString(this.font, "§e⚡ " + PlotUtilityManager.formatElectricity(data.getCurrentElectricity()), leftPos + 15, y + 15, 0xFFFFFF);
                    guiGraphics.drawString(this.font, "§b💧 " + PlotUtilityManager.formatWater(data.getCurrentWater()), leftPos + 100, y + 15, 0xFFFFFF);
                }

                // Status
                String status = "§7Privat";
                if (plot.isForSale()) status = "§a[Verkauf]";
                else if (plot.isForRent()) status = plot.isRented() ? "§e[Vermietet]" : "§d[Zu vermieten]";
                guiGraphics.drawString(this.font, status, leftPos + 15, y + 27, 0xFFFFFF);

                // Geräte-Anzahl
                if (dataOpt.isPresent()) {
                    guiGraphics.drawString(this.font, "§8" + dataOpt.get().getConsumerCount() + " Geräte", leftPos + 100, y + 27, 0x666666);
                }
            }
            y += 50;
            contentHeight += 50;
        }

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 4: FINANZEN
    // ═══════════════════════════════════════════════════════════════════════════

    // Preiskonstanten (pro Einheit)
    private static final double ELECTRICITY_PRICE_PER_KWH = 0.35; // €/kWh
    private static final double WATER_PRICE_PER_LITER = 0.005; // €/L

    private void renderFinanceTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String playerUUID = mc.player.getUUID().toString();

        // ═══════════════════════════════════════════════════════════════════════════
        // MAHNUNGEN / WARNUNGEN
        // ═══════════════════════════════════════════════════════════════════════════
        boolean hasWarnings = false;
        double totalElectricity = 0;
        double totalWater = 0;

        for (PlotRegion plot : myPlots) {
            Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plot.getPlotId());
            if (dataOpt.isPresent()) {
                PlotUtilityData data = dataOpt.get();
                totalElectricity += data.get7DayAverageElectricity();
                totalWater += data.get7DayAverageWater();

                // Warnung bei hohem Verbrauch (>100 kWh oder >500 L)
                if (data.get7DayAverageElectricity() > 100 || data.get7DayAverageWater() > 500) {
                    hasWarnings = true;
                }
            }
        }

        // Warnungs-Bereich
        if (hasWarnings) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x66AA0000);
                guiGraphics.drawString(this.font, "§c§l⚠ WARNUNG", leftPos + 15, y + 3, 0xFF5555);
                guiGraphics.drawString(this.font, "§7Hoher Verbrauch erkannt!", leftPos + 15, y + 14, 0xFFFFFF);
            }
            y += 30;
            contentHeight += 30;
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // AKTUELLE RECHNUNGEN
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§l💰 RECHNUNGEN", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        if (myPlots.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8Keine Grundstücke", leftPos + 15, y, 0x666666);
            }
            y += 15;
            contentHeight += 15;
        } else {
            // Berechne Gesamtkosten
            double totalElecCost = totalElectricity * ELECTRICITY_PRICE_PER_KWH;
            double totalWaterCost = totalWater * WATER_PRICE_PER_LITER;
            double totalCost = totalElecCost + totalWaterCost;

            // Gesamt-Box
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 50, 0x44333333);
                guiGraphics.drawString(this.font, "§fGesamt (7-Tage-Ø/Tag)", leftPos + 15, y + 3, 0xFFFFFF);

                // Strom
                guiGraphics.drawString(this.font, "§e⚡ " + PlotUtilityManager.formatElectricity(totalElectricity), leftPos + 15, y + 15, 0xFFFFFF);
                guiGraphics.drawString(this.font, String.format("§a%.2f€", totalElecCost), leftPos + 130, y + 15, 0x55FF55);

                // Wasser
                guiGraphics.drawString(this.font, "§b💧 " + PlotUtilityManager.formatWater(totalWater), leftPos + 15, y + 27, 0xFFFFFF);
                guiGraphics.drawString(this.font, String.format("§a%.2f€", totalWaterCost), leftPos + 130, y + 27, 0x55FF55);

                // Gesamtsumme
                guiGraphics.fill(leftPos + 15, y + 38, leftPos + WIDTH - 15, y + 39, 0x44FFFFFF);
                guiGraphics.drawString(this.font, "§f§lSUMME:", leftPos + 15, y + 41, 0xFFFFFF);
                guiGraphics.drawString(this.font, String.format("§e§l%.2f€/Tag", totalCost), leftPos + 100, y + 41, 0xFFAA00);
            }
            y += 55;
            contentHeight += 55;

            // Pro-Plot Aufschlüsselung
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8Pro Grundstück:", leftPos + 15, y, 0x888888);
            }
            y += 12;
            contentHeight += 12;

            for (PlotRegion plot : myPlots) {
                Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plot.getPlotId());
                if (dataOpt.isPresent()) {
                    PlotUtilityData data = dataOpt.get();
                    double elec = data.get7DayAverageElectricity();
                    double water = data.get7DayAverageWater();
                    double cost = (elec * ELECTRICITY_PRICE_PER_KWH) + (water * WATER_PRICE_PER_LITER);

                    if (y >= startY - 30 && y < endY) {
                        guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x33333333);
                        guiGraphics.drawString(this.font, "§7" + plot.getPlotName(), leftPos + 15, y + 3, 0xAAAAAA);
                        guiGraphics.drawString(this.font, String.format("§e%.2f€", cost), leftPos + 140, y + 3, 0xFFAA00);
                        guiGraphics.drawString(this.font, String.format("§8⚡%.0f kWh  💧%.0f L", elec, water), leftPos + 15, y + 14, 0x666666);
                    }
                    y += 28;
                    contentHeight += 28;
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // 7-TAGE VERLAUF
        // ═══════════════════════════════════════════════════════════════════════════
        y += 5;
        contentHeight += 5;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§l📊 7-TAGE VERLAUF", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Sammle Historie für alle Plots
        double[] totalDailyElec = new double[7];
        double[] totalDailyWater = new double[7];

        for (PlotRegion plot : myPlots) {
            Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plot.getPlotId());
            if (dataOpt.isPresent()) {
                PlotUtilityData data = dataOpt.get();
                double[] dailyElec = data.getDailyElectricity();
                double[] dailyWater = data.getDailyWater();
                for (int i = 0; i < 7; i++) {
                    totalDailyElec[i] += dailyElec[i];
                    totalDailyWater[i] += dailyWater[i];
                }
            }
        }

        // Tages-Anzeige (0 = Heute, 6 = Vor 6 Tagen)
        String[] dayLabels = {"Heute", "Gestern", "Vor 2d", "Vor 3d", "Vor 4d", "Vor 5d", "Vor 6d"};

        for (int i = 0; i < 7; i++) {
            double elec = totalDailyElec[i];
            double water = totalDailyWater[i];
            double cost = (elec * ELECTRICITY_PRICE_PER_KWH) + (water * WATER_PRICE_PER_LITER);

            if (y >= startY - 15 && y < endY) {
                // Hintergrund für jeden Tag
                int bgColor = (i == 0) ? 0x44228B22 : 0x22333333;
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 14, bgColor);

                guiGraphics.drawString(this.font, "§7" + dayLabels[i], leftPos + 15, y + 3, 0xAAAAAA);
                guiGraphics.drawString(this.font, String.format("§e%.2f€", cost), leftPos + 80, y + 3, 0xFFAA00);
                guiGraphics.drawString(this.font, String.format("§8%.0fkWh", elec), leftPos + 130, y + 3, 0x666666);
            }
            y += 16;
            contentHeight += 16;
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // HINWEIS
        // ═══════════════════════════════════════════════════════════════════════════
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawCenteredString(this.font, "§8Preise: 0.35€/kWh, 0.50€/100L", leftPos + WIDTH / 2, y, 0x666666);
        }
        y += 12;
        contentHeight += 12;

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
    public boolean isPauseScreen() {
        return false;
    }
}
