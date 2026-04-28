package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.network.PlotNetworkHandler;
import de.rolandsw.schedulemc.region.network.PlotPurchasePacket;
import de.rolandsw.schedulemc.region.network.PlotUtilityBillingPacket;
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
    private static final int MARGIN_TOP = 5;
    private static final int MARGIN_BOTTOM = 60;

    // Tab-System
    private int currentTab = 0;
    private static final int TAB_HEIGHT = 22;
    private static final int TAB_WIDTH = 45;

    /**
     * Get localized tab names (returns cached array populated in init())
     */
    private String[] getTabNames() {
        return cachedTabNames;
    }

    // Scrolling
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_SPEED = 15;
    private static final int CONTENT_HEIGHT = 125; // Sichtbarer Bereich (HEIGHT - 55 Tabs - 60 Buttons)
    private static final long DATA_REFRESH_INTERVAL_MS = 250L;

    private int leftPos;
    private int topPos;
    private long lastRefreshMs = 0L;

    // Cached Data
    private PlotRegion currentPlot;
    private PlotUtilityData utilityData;
    private List<PlotRegion> availablePlots;
    private List<PlotRegion> myPlots;

    // OPTIMIERT: Utility-Daten für myPlots gecacht, um per-Frame PlotUtilityManager.getPlotData()-Lookups zu vermeiden
    private java.util.Map<String, PlotUtilityData> cachedUtilityByPlot = new java.util.HashMap<>();

    // OPTIMIERT: Aggregierte Finanz-Werte in refreshData() berechnet statt per Frame
    private double cachedTotalElectricity;
    private double cachedTotalWater;
    private double cachedTotalOutstandingBills;
    private boolean cachedHasWarnings;
    private boolean cachedHasUtilityCutoff;

    // Config-Preise (aus ModConfigHandler, in refreshData() aktualisiert)
    private double electricityPricePerKwh;
    private double waterPricePerLiter;

    // OPTIMIERT: Wiederverwendbare Arrays statt Allokation pro Frame in renderFinanceTab()
    private final double[] totalDailyElec = new double[7];
    private final double[] totalDailyWater = new double[7];

    // Cached localized strings (populated in init() to avoid per-frame allocations)
    private String[] cachedTabNames;
    private String cachedTitle;
    private String cachedNoPlot;
    private String cachedStandOnPlot;
    private String cachedUnnamed;
    private String cachedNoOwner;
    private String cachedOwnerLabel;
    private String cachedSizeLabel;
    private String cachedBlocksLabel;
    private String cachedForSale;
    private String cachedPriceLabel;
    private String cachedForRent;
    private String cachedRentPriceLabel;
    private String cachedPerDay;
    private String cachedConsumption;
    private String cachedElectricity;
    private String cachedAvg7day;
    private String cachedWater;
    private String cachedDevicesLabel;
    private String cachedNoOffers;
    private String cachedOffersLabel;
    private String cachedBuyLabel;
    private String cachedRentLabel;
    private String cachedNoPlots;
    private String cachedBuyInMarket;
    private String cachedProperties;
    private String cachedPrivateStatus;
    private String cachedForSaleShort;
    private String cachedRented;
    private String cachedToRent;
    private String cachedDevicesCount;
    private String cachedWarning;
    private String cachedHighConsumption;
    private String cachedBills;
    private String cachedNoProperties;
    private String cachedTotalAvg;
    private String cachedSum;
    private String cachedPerProperty;
    private String cachedHistory7day;
    private String[] cachedDayLabels;
    private String cachedPrices;
    // Währungssymbol (locale-abhängig: "€" für de_de, "$" für en_us)
    private String cachedCurrencySymbol;
    // Neue Übersetzungsschlüssel für vormals hardcodierte Strings
    private String cachedPayBills;
    private String cachedAutoPayOn;
    private String cachedAutoPayOff;
    private String cachedUtilityCutoff;
    private String cachedDueToday;
    private String cachedOutstandingFmt;
    private String cachedBillDetailFmt;

    // Tab-1 action buttons (buy/rent on current plot)
    private Button buyButton;
    private Button rentButton;
    private Button payBillsButton;
    private Button autoPayButton;

    public PlotAppScreen(Screen parent) {
        super(Component.translatable("gui.app.plot.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;

        // Positioniere oben mit Margin
        this.topPos = MARGIN_TOP;

        // Währungssymbol zuerst initialisieren, da refreshData() es bereits nutzt
        cachedCurrencySymbol = Component.translatable("format.currency").getString();

        // Cache data
        refreshData();

        // Cache all localized strings to avoid per-frame allocations
        cachedTabNames = new String[]{
            Component.translatable("gui.app.plot.tab.plot").getString(),
            Component.translatable("gui.app.plot.tab.market").getString(),
            Component.translatable("gui.app.plot.tab.mine").getString(),
            Component.translatable("gui.app.plot.tab.finances").getString()
        };
        cachedTitle = Component.translatable("gui.app.plot.title").getString();
        cachedNoPlot = Component.translatable("gui.app.plot.no_plot").getString();
        cachedStandOnPlot = Component.translatable("gui.app.plot.stand_on_plot").getString();
        cachedUnnamed = Component.translatable("plot.unnamed").getString();
        cachedNoOwner = Component.translatable("gui.app.plot.no_owner").getString();
        cachedOwnerLabel = Component.translatable("gui.app.plot.owner").getString();
        cachedSizeLabel = Component.translatable("gui.app.plot.size").getString();
        cachedBlocksLabel = Component.translatable("gui.app.plot.blocks").getString();
        cachedForSale = Component.translatable("gui.app.plot.for_sale").getString();
        cachedPriceLabel = Component.translatable("gui.app.plot.price").getString();
        cachedForRent = Component.translatable("gui.app.plot.for_rent").getString();
        cachedRentPriceLabel = Component.translatable("gui.app.plot.rent").getString();
        cachedPerDay = Component.translatable("format.per_day").getString();
        cachedConsumption = Component.translatable("gui.app.plot.consumption").getString();
        cachedElectricity = Component.translatable("gui.app.plot.electricity").getString();
        cachedAvg7day = Component.translatable("gui.app.plot.avg_7day").getString();
        cachedWater = Component.translatable("gui.app.plot.water").getString();
        cachedDevicesLabel = Component.translatable("gui.app.plot.devices").getString();
        cachedNoOffers = Component.translatable("gui.app.plot.no_offers").getString();
        cachedOffersLabel = Component.translatable("gui.app.plot.offers").getString();
        cachedBuyLabel = Component.translatable("gui.app.plot.buy_label").getString();
        cachedRentLabel = Component.translatable("gui.app.plot.rent_label").getString();
        cachedNoPlots = Component.translatable("gui.app.plot.no_plots").getString();
        cachedBuyInMarket = Component.translatable("gui.app.plot.buy_in_market").getString();
        cachedProperties = Component.translatable("gui.app.plot.properties").getString();
        cachedPrivateStatus = Component.translatable("gui.app.plot.private").getString();
        cachedForSaleShort = Component.translatable("gui.app.plot.for_sale_short").getString();
        cachedRented = Component.translatable("gui.app.plot.rented").getString();
        cachedToRent = Component.translatable("gui.app.plot.to_rent").getString();
        cachedDevicesCount = Component.translatable("gui.app.plot.devices_count").getString();
        cachedWarning = Component.translatable("gui.app.plot.warning").getString();
        cachedHighConsumption = Component.translatable("gui.app.plot.high_consumption").getString();
        cachedBills = Component.translatable("gui.app.plot.bills").getString();
        cachedNoProperties = Component.translatable("gui.app.plot.no_properties").getString();
        cachedTotalAvg = Component.translatable("gui.app.plot.total_avg").getString();
        cachedSum = Component.translatable("gui.app.plot.sum").getString();
        cachedPerProperty = Component.translatable("gui.app.plot.per_property").getString();
        cachedHistory7day = Component.translatable("gui.app.plot.history_7day").getString();
        cachedDayLabels = new String[]{
            Component.translatable("gui.app.plot.today").getString(),
            Component.translatable("gui.app.plot.yesterday").getString(),
            Component.translatable("gui.app.plot.days_ago_2").getString(),
            Component.translatable("gui.app.plot.days_ago_3").getString(),
            Component.translatable("gui.app.plot.days_ago_4").getString(),
            Component.translatable("gui.app.plot.days_ago_5").getString(),
            Component.translatable("gui.app.plot.days_ago_6").getString()
        };
        cachedPayBills = Component.translatable("gui.app.plot.button.pay_bills").getString();
        cachedAutoPayOn = Component.translatable("gui.app.plot.button.autopay_on").getString();
        cachedAutoPayOff = Component.translatable("gui.app.plot.button.autopay_off").getString();
        cachedUtilityCutoff = Component.translatable("gui.app.plot.utility_cutoff").getString();
        cachedDueToday = Component.translatable("gui.app.plot.due_today").getString();
        cachedOutstandingFmt = Component.translatable("gui.app.plot.outstanding_fmt").getString();
        cachedBillDetailFmt = Component.translatable("gui.app.plot.bill_detail_fmt").getString();
        // cachedPrices wird dynamisch in refreshData() aus Config-Werten gebaut

        // Tab-Buttons
        for (int i = 0; i < getTabNames().length; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(
                Component.literal(getTabNames()[i]),
                button -> {
                    currentTab = tabIndex;
                    scrollOffset = 0;
                    refreshData();
                    updateButtonVisibility();
                }
            ).bounds(leftPos + 10 + (i * TAB_WIDTH), topPos + 30, TAB_WIDTH - 2, TAB_HEIGHT).build());
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.common.back_arrows"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());

        // Tab-1: Kaufen / Mieten Buttons (Sichtbarkeit wird in updateButtonVisibility() gesetzt)
        buyButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.app.plot.button.buy"),
            button -> {
                if (currentPlot != null) {
                    PlotNetworkHandler.sendToServer(
                        new PlotPurchasePacket(currentPlot.getPlotId(), PlotPurchasePacket.PurchaseType.BUY)
                    );
                    refreshData();
                    updateButtonVisibility();
                }
            }
        ).bounds(leftPos + 10, topPos + HEIGHT - 55, 85, 18).build());

        rentButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.app.plot.button.rent"),
            button -> {
                if (currentPlot != null) {
                    PlotNetworkHandler.sendToServer(
                        new PlotPurchasePacket(currentPlot.getPlotId(), PlotPurchasePacket.PurchaseType.RENT)
                    );
                    refreshData();
                    updateButtonVisibility();
                }
            }
        ).bounds(leftPos + 105, topPos + HEIGHT - 55, 85, 18).build());

        // AutoPay-Toggle links (schmaler), Rechnungen zahlen rechts (breiter als primäre Aktion)
        autoPayButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.app.plot.button.autopay_off"),
            button -> {
                if (minecraft == null || minecraft.player == null) {
                    return;
                }
                boolean currentlyEnabled = PlotUtilityManager.isAutoPayEnabledForOwner(minecraft.player.getUUID());
                PlotNetworkHandler.sendToServer(PlotUtilityBillingPacket.setAutoPay(!currentlyEnabled));
                refreshData();
                updateButtonVisibility();
            }
        ).bounds(leftPos + 10, topPos + HEIGHT - 55, 75, 18).build());

        payBillsButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.app.plot.button.pay_bills"),
            button -> {
                PlotNetworkHandler.sendToServer(PlotUtilityBillingPacket.payAll());
                refreshData();
                updateButtonVisibility();
            }
        ).bounds(leftPos + 90, topPos + HEIGHT - 55, 100, 18).build());

        updateButtonVisibility();
    }

    /**
     * Aktualisiert gecachte Daten (wird max. 1x/Sekunde aufgerufen).
     * OPTIMIERT: Alle teuren Lookups hier gebündelt statt per Frame.
     */
    private void refreshData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        String playerUUID = mc.player.getUUID().toString();

        // Config-Preise (aus gemeinsamer COMMON-Config, auf Client verfügbar)
        electricityPricePerKwh = ModConfigHandler.COMMON.UTILITY_ELECTRICITY_PRICE_PER_KWH.get();
        waterPricePerLiter = ModConfigHandler.COMMON.UTILITY_WATER_PRICE_PER_LITER.get();
        cachedPrices = String.format("§8%.2f%s/kWh, %.2f%s/100L",
            electricityPricePerKwh, cachedCurrencySymbol, waterPricePerLiter * 100, cachedCurrencySymbol);

        // Aktueller Plot
        currentPlot = PlotManager.getPlotAt(playerPos);
        if (currentPlot != null) {
            Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(currentPlot.getPlotId());
            utilityData = dataOpt.orElse(null);
        } else {
            utilityData = null;
        }

        // Verfügbare Plots (zum Verkauf oder ohne Besitzer - nur kaufbare/mietbare Typen)
        availablePlots = new ArrayList<>();
        for (PlotRegion plot : PlotManager.getPlots()) {
            if ((!plot.hasOwner() && plot.isPurchasable()) || plot.isForSale() || plot.isForRent()) {
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

        // OPTIMIERT: Utility-Daten und Finanz-Aggregate einmalig cachen
        cachedUtilityByPlot.clear();
        cachedTotalElectricity = 0;
        cachedTotalWater = 0;
        cachedTotalOutstandingBills = 0;
        cachedHasWarnings = false;
        cachedHasUtilityCutoff = false;

        for (PlotRegion plot : myPlots) {
            Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plot.getPlotId());
            if (dataOpt.isPresent()) {
                PlotUtilityData data = dataOpt.get();
                cachedUtilityByPlot.put(plot.getPlotId(), data);
                cachedTotalElectricity += data.getCurrentElectricity();
                cachedTotalWater += data.getCurrentWater();
                cachedTotalOutstandingBills += data.getOutstandingBill();
                if (!data.isUtilitiesEnabled()) {
                    cachedHasUtilityCutoff = true;
                }
                if (data.getCurrentElectricity() > 100 || data.getCurrentWater() > 500 || data.getOutstandingBill() > 0) {
                    cachedHasWarnings = true;
                }
            }
        }

        lastRefreshMs = System.currentTimeMillis();
    }

    /**
     * Zeigt/versteckt Kauf-/Miet-Buttons je nach aktuellem Tab und Plot-Status.
     */
    private void updateButtonVisibility() {
        if (buyButton == null || rentButton == null || payBillsButton == null || autoPayButton == null) return;
        boolean onTab0 = currentTab == 0;
        boolean onTab3 = currentTab == 3;
        boolean canBuy = onTab0 && currentPlot != null
            && ((!currentPlot.hasOwner() && currentPlot.isPurchasable()) || currentPlot.isForSale());
        boolean canRent = onTab0 && currentPlot != null
            && currentPlot.isForRent() && !currentPlot.isRented();
        buyButton.visible = canBuy;
        rentButton.visible = canRent;
        payBillsButton.visible = onTab3 && !myPlots.isEmpty();
        autoPayButton.visible = onTab3 && !myPlots.isEmpty();
        if (minecraft != null && minecraft.player != null) {
            boolean autoPayEnabled = PlotUtilityManager.isAutoPayEnabledForOwner(minecraft.player.getUUID());
            autoPayButton.setMessage(Component.literal(autoPayEnabled ? cachedAutoPayOn : cachedAutoPayOff));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= DATA_REFRESH_INTERVAL_MS) {
            refreshData();
            updateButtonVisibility();
        }

        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, cachedTitle, leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        // Tab-Hintergrund (aktiver Tab hervorheben)
        for (int i = 0; i < getTabNames().length; i++) {
            int tabX = leftPos + 10 + (i * TAB_WIDTH);
            int tabY = topPos + 30;
            if (i == currentTab) {
                guiGraphics.fill(tabX - 1, tabY - 1, tabX + TAB_WIDTH - 1, tabY + TAB_HEIGHT + 1, 0xFF4A90E2);
            }
        }

        // Content-Bereich: endet 60px vom unteren Rand, damit Text die Buttons (bei -55) nicht überlappt
        int contentY = topPos + 55;
        int contentEndY = topPos + HEIGHT - 60;

        // Render Tab-Content
        switch (currentTab) {
            case 0 -> renderCurrentPlotTab(guiGraphics, contentY, contentEndY);
            case 1 -> renderMarketTab(guiGraphics, contentY, contentEndY);
            case 2 -> renderMyPlotsTab(guiGraphics, contentY, contentEndY);
            case 3 -> renderFinanceTab(guiGraphics, contentY, contentEndY);
            default -> {}
        }

        // Gradient-Fade am unteren Content-Rand: blendet scrollenden Text sanft aus
        // (verhindert, dass Text über die Buttons läuft)
        final int FADE_HEIGHT = 15;
        for (int fi = 0; fi < FADE_HEIGHT; fi++) {
            int alpha = (255 * fi / FADE_HEIGHT) << 24;
            guiGraphics.fill(leftPos, contentEndY - FADE_HEIGHT + fi,
                leftPos + WIDTH, contentEndY - FADE_HEIGHT + fi + 1,
                alpha | 0x2A2A2A);
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
            Minecraft mcW = Minecraft.getInstance();
            // Dimensionsname ermitteln
            String dimName;
            if (mcW.level != null) {
                String path = mcW.level.dimension().location().getPath();
                dimName = switch (path) {
                    case "overworld" -> Component.translatable("gui.app.plot.world.overworld").getString();
                    case "the_nether" -> Component.translatable("gui.app.plot.world.nether").getString();
                    case "the_end" -> Component.translatable("gui.app.plot.world.end").getString();
                    default -> path;
                };
            } else {
                dimName = Component.translatable("gui.app.plot.world.overworld").getString();
            }

            guiGraphics.drawString(this.font, "§6§l" + dimName, leftPos + 15, y, 0xFFAA00);
            y += 15;

            if (mcW.player != null) {
                BlockPos pos = mcW.player.blockPosition();
                guiGraphics.drawString(this.font, "§8" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ(), leftPos + 15, y, 0x666666);
                y += 12;
            }

            guiGraphics.drawString(this.font, cachedOwnerLabel + Component.translatable("gui.app.plot.world.owner").getString(), leftPos + 15, y, 0xFFFFFF);
            y += 12;

            guiGraphics.drawString(this.font, "§7" + Component.translatable("gui.app.plot.world.no_plot").getString(), leftPos + 15, y, 0x888888);

            maxScroll = 0;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean isOwner = mc.player != null && mc.player.getUUID().toString().equals(currentPlot.getOwnerUUID());

        // Plot-Name
        if (y >= startY - 15 && y < endY) {
            String plotName = currentPlot.getPlotName();
            if (plotName == null || plotName.isEmpty()) {
                plotName = cachedUnnamed;
            }
            guiGraphics.drawString(this.font, "§6§l" + plotName, leftPos + 15, y, 0xFFAA00);
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
            String owner = currentPlot.hasOwner() ? currentPlot.getOwnerName() : cachedNoOwner;
            guiGraphics.drawString(this.font, cachedOwnerLabel + owner, leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        // Größe
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, cachedSizeLabel + String.format("%,d", currentPlot.getVolume()) + cachedBlocksLabel, leftPos + 15, y, 0xFFFFFF);
        }
        y += 15;
        contentHeight += 15;

        // Status (Verkauf/Miete)
        if (!currentPlot.hasOwner() && currentPlot.isPurchasable()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x44228B22);
                guiGraphics.drawString(this.font, cachedForSale, leftPos + 15, y + 3, 0x00FF00);
                guiGraphics.drawString(this.font, cachedPriceLabel + String.format("%.0f", currentPlot.getPrice()) + cachedCurrencySymbol, leftPos + 15, y + 14, 0xFFFFFF);
            }
            y += 30;
            contentHeight += 30;
        } else if (currentPlot.isForSale()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x44228B22);
                guiGraphics.drawString(this.font, cachedForSale, leftPos + 15, y + 3, 0x00FF00);
                guiGraphics.drawString(this.font, cachedPriceLabel + String.format("%.0f", currentPlot.getSalePrice()) + cachedCurrencySymbol, leftPos + 15, y + 14, 0xFFFFFF);
            }
            y += 30;
            contentHeight += 30;
        } else if (currentPlot.isForRent() && !currentPlot.isRented()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x44C71585);
                guiGraphics.drawString(this.font, cachedForRent, leftPos + 15, y + 3, 0xFF00FF);
                guiGraphics.drawString(this.font, cachedRentPriceLabel + String.format("%.0f", currentPlot.getRentPricePerDay()) + cachedCurrencySymbol + cachedPerDay, leftPos + 15, y + 14, 0xFFFFFF);
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
                guiGraphics.drawString(this.font, cachedConsumption, leftPos + 15, y, 0x55FFFF);
            }
            y += 14;
            contentHeight += 14;

            // Strom
            if (y >= startY - 10 && y < endY) {
                double elec = utilityData.getCurrentElectricity();
                double avgElec = utilityData.get7DayAverageElectricity();  // NOPMD
                guiGraphics.drawString(this.font, cachedElectricity, leftPos + 15, y, 0xFFAA00);
                guiGraphics.drawString(this.font, PlotUtilityManager.formatElectricity(elec), leftPos + 80, y, 0xFFFFFF);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedAvg7day + PlotUtilityManager.formatElectricity(utilityData.get7DayAverageElectricity()), leftPos + 15, y, 0x888888);
            }
            y += 13;
            contentHeight += 13;

            // Wasser
            if (y >= startY - 10 && y < endY) {
                double water = utilityData.getCurrentWater();
                guiGraphics.drawString(this.font, cachedWater, leftPos + 15, y, 0x55AAFF);
                guiGraphics.drawString(this.font, PlotUtilityManager.formatWater(water), leftPos + 80, y, 0xFFFFFF);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedAvg7day + PlotUtilityManager.formatWater(utilityData.get7DayAverageWater()), leftPos + 15, y, 0x888888);
            }
            y += 13;
            contentHeight += 13;

            // Verbraucher-Anzahl
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedDevicesLabel + utilityData.getConsumerCount(), leftPos + 15, y, 0xFFFFFF);
            }
            y += 15;  // NOPMD
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
            guiGraphics.drawCenteredString(this.font, cachedNoOffers, leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            maxScroll = 0;
            return;
        }

        // Header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§e" + availablePlots.size() + cachedOffersLabel, leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        for (PlotRegion plot : availablePlots) {
            if (y >= startY - 40 && y < endY) {
                // Plot-Box
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 35, 0x44333333);

                // Name
                String plotName = plot.getPlotName();
                if (plotName == null || plotName.isEmpty()) {
                    plotName = cachedUnnamed;
                }
                guiGraphics.drawString(this.font, "§f" + plotName, leftPos + 15, y + 3, 0xFFFFFF);

                // Status & Preis
                String status;
                String price;
                if (!plot.hasOwner()) {
                    status = cachedBuyLabel;
                    price = String.format("%.0f", plot.getPrice()) + cachedCurrencySymbol;
                } else if (plot.isForSale()) {
                    status = cachedBuyLabel;
                    price = String.format("%.0f", plot.getSalePrice()) + cachedCurrencySymbol;
                } else {
                    status = cachedRentLabel;
                    price = String.format("%.0f", plot.getRentPricePerDay()) + cachedCurrencySymbol + cachedPerDay;
                }

                guiGraphics.drawString(this.font, status, leftPos + 15, y + 14, 0xFFFFFF);
                guiGraphics.drawString(this.font, "§e" + price, leftPos + 70, y + 14, 0xFFFFFF);

                // Größe
                guiGraphics.drawString(this.font, "§8" + String.format("%,d", plot.getVolume()) + cachedBlocksLabel, leftPos + 15, y + 25, 0x666666);
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
            guiGraphics.drawCenteredString(this.font, cachedNoPlots, leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            guiGraphics.drawCenteredString(this.font, cachedBuyInMarket, leftPos + WIDTH / 2, y + 35, 0x666666);
            maxScroll = 0;
            return;
        }

        // Header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6" + myPlots.size() + cachedProperties, leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        for (PlotRegion plot : myPlots) {
            if (y >= startY - 50 && y < endY) {
                // Plot-Box
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 45, 0x44333333);

                // Name
                String plotName = plot.getPlotName();
                if (plotName == null || plotName.isEmpty()) {
                    plotName = cachedUnnamed;
                }
                guiGraphics.drawString(this.font, "§6§l" + plotName, leftPos + 15, y + 3, 0xFFAA00);

                // Utility-Verbrauch — OPTIMIERT: cachedUtilityByPlot statt getPlotData() per Frame
                PlotUtilityData data = cachedUtilityByPlot.get(plot.getPlotId());
                if (data != null) {
                    guiGraphics.drawString(this.font, "§e⚡ " + PlotUtilityManager.formatElectricity(data.getCurrentElectricity()), leftPos + 15, y + 15, 0xFFFFFF);
                    guiGraphics.drawString(this.font, "§b💧 " + PlotUtilityManager.formatWater(data.getCurrentWater()), leftPos + 100, y + 15, 0xFFFFFF);
                }

                // Status
                String status = cachedPrivateStatus;
                if (plot.isForSale()) status = cachedForSaleShort;
                else if (plot.isForRent()) status = plot.isRented() ? cachedRented : cachedToRent;
                guiGraphics.drawString(this.font, status, leftPos + 15, y + 27, 0xFFFFFF);

                // Geräte-Anzahl
                if (data != null) {
                    guiGraphics.drawString(this.font, "§8" + data.getConsumerCount() + cachedDevicesCount, leftPos + 100, y + 27, 0x666666);
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

    private void renderFinanceTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // OPTIMIERT: Aggregate aus refreshData() — kein Per-Frame-Looping mehr
        double totalElectricity = cachedTotalElectricity;
        double totalWater = cachedTotalWater;
        double totalOutstandingBills = cachedTotalOutstandingBills;

        // ═══════════════════════════════════════════════════════════════════════════
        // MAHNUNGEN / WARNUNGEN
        // ═══════════════════════════════════════════════════════════════════════════
        if (cachedHasWarnings) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x66AA0000);
                guiGraphics.drawString(this.font, cachedWarning, leftPos + 15, y + 3, 0xFF5555);
                String warningText = cachedHasUtilityCutoff ? cachedUtilityCutoff : cachedHighConsumption;
                guiGraphics.drawString(this.font, warningText, leftPos + 15, y + 14, 0xFFFFFF);
            }
            y += 30;
            contentHeight += 30;
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // AKTUELLE RECHNUNGEN
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, cachedBills, leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        if (myPlots.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedNoProperties, leftPos + 15, y, 0x666666);
            }
            y += 15;
            contentHeight += 15;
        } else {
            // Gesamtkosten mit Config-Preisen
            double totalElecCost = totalElectricity * electricityPricePerKwh;
            double totalWaterCost = totalWater * waterPricePerLiter;
            double totalCost = totalElecCost + totalWaterCost;

            // Gesamt-Box
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 50, 0x44333333);
                guiGraphics.drawString(this.font, cachedDueToday, leftPos + 15, y + 3, 0xFFFFFF);

                // Strom
                guiGraphics.drawString(this.font, "§e⚡ " + PlotUtilityManager.formatElectricity(totalElectricity), leftPos + 15, y + 15, 0xFFFFFF);
                guiGraphics.drawString(this.font, String.format("§a%.2f", totalElecCost) + cachedCurrencySymbol, leftPos + 130, y + 15, 0x55FF55);

                // Wasser
                guiGraphics.drawString(this.font, "§b💧 " + PlotUtilityManager.formatWater(totalWater), leftPos + 15, y + 27, 0xFFFFFF);
                guiGraphics.drawString(this.font, String.format("§a%.2f", totalWaterCost) + cachedCurrencySymbol, leftPos + 130, y + 27, 0x55FF55);

                // Gesamtsumme
                guiGraphics.fill(leftPos + 15, y + 38, leftPos + WIDTH - 15, y + 39, 0x44FFFFFF);
                guiGraphics.drawString(this.font, cachedSum, leftPos + 15, y + 41, 0xFFFFFF);
                guiGraphics.drawString(this.font, String.format("§e§l%.2f", totalCost) + cachedCurrencySymbol, leftPos + 100, y + 41, 0xFFAA00);
            }
            y += 55;
            contentHeight += 55;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, String.format(cachedOutstandingFmt, totalOutstandingBills),
                    leftPos + 15, y, 0xFF5555);
            }
            y += 12;
            contentHeight += 12;

            // Pro-Plot Aufschlüsselung — OPTIMIERT: nutzt cachedUtilityByPlot statt getPlotData() per Frame
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedPerProperty, leftPos + 15, y, 0x888888);
            }
            y += 12;
            contentHeight += 12;

            for (PlotRegion plot : myPlots) {
                PlotUtilityData data = cachedUtilityByPlot.get(plot.getPlotId());
                double elec = data != null ? data.getCurrentElectricity() : 0.0;
                double water = data != null ? data.getCurrentWater() : 0.0;
                double cost = (elec * electricityPricePerKwh) + (water * waterPricePerLiter);
                double outstanding = data != null ? data.getOutstandingBill() : 0.0;
                int unpaidDays = data != null ? data.getUnpaidDays() : 0;

                if (y >= startY - 40 && y < endY) {
                    guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 34, 0x33333333);
                    String plotName = plot.getPlotName();
                    if (plotName == null || plotName.isEmpty()) {
                        plotName = cachedUnnamed;
                    }
                    guiGraphics.drawString(this.font, "§7" + plotName, leftPos + 15, y + 3, 0xAAAAAA);
                    guiGraphics.drawString(this.font, String.format("§e%.2f", cost) + cachedCurrencySymbol, leftPos + 140, y + 3, 0xFFAA00);
                    guiGraphics.drawString(this.font, Component.translatable("ui.plot.utility_display",
                        String.format("%.0f", elec),
                        String.format("%.0f", water)).getString(), leftPos + 15, y + 14, 0x666666);
                    guiGraphics.drawString(this.font,
                        String.format(cachedBillDetailFmt, outstanding, unpaidDays),
                        leftPos + 15, y + 24, 0xAA6666);
                }
                y += 37;
                contentHeight += 37;
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // 7-TAGE VERLAUF — OPTIMIERT: cachedUtilityByPlot statt getPlotData() per Frame
        // ═══════════════════════════════════════════════════════════════════════════
        y += 5;
        contentHeight += 5;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, cachedHistory7day, leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Wiederverwendbare Instance-Arrays zurücksetzen
        java.util.Arrays.fill(totalDailyElec, 0.0);
        java.util.Arrays.fill(totalDailyWater, 0.0);

        for (PlotUtilityData data : cachedUtilityByPlot.values()) {
            double[] dailyElec = data.getDailyElectricity();
            double[] dailyWater = data.getDailyWater();
            for (int i = 0; i < 7; i++) {
                totalDailyElec[i] += dailyElec[i];
                totalDailyWater[i] += dailyWater[i];
            }
        }

        // Tages-Anzeige (0 = Heute, 6 = Vor 6 Tagen)
        for (int i = 0; i < 7; i++) {
            double elec = totalDailyElec[i];
            double water = totalDailyWater[i];
            double cost = (elec * electricityPricePerKwh) + (water * waterPricePerLiter);

            if (y >= startY - 15 && y < endY) {
                int bgColor = (i == 0) ? 0x44228B22 : 0x22333333;
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 14, bgColor);

                guiGraphics.drawString(this.font, "§7" + cachedDayLabels[i], leftPos + 15, y + 3, 0xAAAAAA);
                guiGraphics.drawString(this.font, String.format("§e%.2f", cost) + cachedCurrencySymbol, leftPos + 80, y + 3, 0xFFAA00);
                guiGraphics.drawString(this.font, String.format("§8%.0fkWh", elec), leftPos + 130, y + 3, 0x666666);
            }
            y += 16;
            contentHeight += 16;
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // PREISHINWEIS (dynamisch aus Config)
        // ═══════════════════════════════════════════════════════════════════════════
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawCenteredString(this.font, cachedPrices, leftPos + WIDTH / 2, y, 0x666666);
        }
        y += 12;  // NOPMD
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
    }    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 264 || keyCode == 265) { // NOPMD
            return true;
        }

        // Block E key (inventory key - 69) from closing the screen
        return keyCode == 69 || super.keyPressed(keyCode, scanCode, modifiers); // Block E key (GLFW_KEY_E)
    }



    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
