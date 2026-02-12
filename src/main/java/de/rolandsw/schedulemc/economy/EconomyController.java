package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.level.ProducerLevel;
import de.rolandsw.schedulemc.level.XPSource;
import de.rolandsw.schedulemc.market.MarketData;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.core.ProductionType;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SINGLE SOURCE OF TRUTH für alle Preise im gesamten Mod.
 *
 * Der EconomyController ist der zentrale Anlaufpunkt für JEDE Preisabfrage.
 * Kein anderes System sollte Preise selbst berechnen — alle müssen hier anfragen.
 *
 * Integriert:
 * - UnifiedPriceCalculator (Preisformel)
 * - PriceBounds (Preisgrenzen)
 * - RiskPremium (Risiko-Aufschläge)
 * - GlobalEconomyTracker (Inflation, Geldmenge)
 * - MarketData (Angebot/Nachfrage per Item)
 * - EconomyCycle (Wirtschaftszyklus) — wird in Phase 2 hinzugefügt
 * - PriceManager Events (Wirtschafts-Events)
 *
 * Thread-Safety: Alle Methoden sind thread-safe.
 */
public class EconomyController {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ItemCategory[] ITEM_CATEGORIES = ItemCategory.values();

    // Singleton
    private static volatile EconomyController instance;

    // ═══════════════════════════════════════════════════════════
    // REFERENZ-PREISE (kalibrierte Basis-Preise pro Produkt)
    // ═══════════════════════════════════════════════════════════

    /**
     * Kalibrierte Referenz-Preise für alle Endprodukte.
     * Key: Produkt-Identifier (z.B. "CANNABIS_INDICA", "WINE_RIESLING")
     * Value: Referenz-Preis pro Einheit
     */
    private final ConcurrentHashMap<String, Double> referencePrices = new ConcurrentHashMap<>();

    /**
     * Item-zu-Kategorie Mapping für schnellen Lookup.
     */
    private final ConcurrentHashMap<String, ItemCategory> itemCategories = new ConcurrentHashMap<>();

    /**
     * MarketData pro Item-Identifier für S&D Tracking.
     */
    private final ConcurrentHashMap<String, MarketData> marketDataMap = new ConcurrentHashMap<>();

    /**
     * Aktueller Wirtschaftszyklus-Multiplikator (wird von EconomyCycle gesetzt).
     */
    private volatile double cycleMultiplier = 1.0;

    /**
     * Aktuelles globales Wanted-Level (Durchschnitt, für Risiko-Berechnung).
     */
    private volatile int globalWantedLevel = 0;

    /**
     * Event-Multiplikatoren pro Kategorie (aus PriceManager-Events aggregiert).
     */
    private final ConcurrentHashMap<ItemCategory, Double> categoryEventMultipliers = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private EconomyController() {
        initializeReferencePrices();
    }

    public static EconomyController getInstance() {
        EconomyController localRef = instance;
        if (localRef == null) {
            synchronized (EconomyController.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new EconomyController();
                }
            }
        }
        return localRef;
    }

    // ═══════════════════════════════════════════════════════════
    // HAUPT-API: VERKAUFS-PREIS (was der Spieler bekommt)
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den Verkaufspreis für ein Produkt.
     * DIES IST DIE EINZIGE METHODE DIE FÜR VERKAUFSPREISE GENUTZT WERDEN SOLLTE.
     *
     * @param productId   Produkt-Identifier (z.B. "CANNABIS_INDICA")
     * @param quality     Qualitätsstufe
     * @param amount      Menge
     * @param playerUUID  Spieler-UUID (für Anti-Exploit Tracking)
     * @return Finaler Verkaufspreis
     */
    public double getSellPrice(String productId, ProductionQuality quality, int amount,
                                @Nullable UUID playerUUID) {
        double refPrice = getRefPrice(productId);
        ItemCategory category = getCategory(productId);

        double qualityMult = quality.getPriceMultiplier();
        double sdMult = getSupplyDemandMultiplier(productId);
        double riskMult = RiskPremium.calculateRiskMultiplier(category, globalWantedLevel);
        double eventMult = getEventMultiplier(productId);
        double inflationAdj = GlobalEconomyTracker.getInstance().getInflationAdjustment();

        double price = UnifiedPriceCalculator.calculatePrice(
                refPrice, qualityMult, sdMult, riskMult,
                cycleMultiplier * inflationAdj, eventMult,
                category, amount
        );

        // Steuern abziehen
        double taxRate = UnifiedPriceCalculator.getTaxRate(category);
        double afterTax = price * (1.0 - taxRate);

        // Anti-Exploit Prüfung
        if (playerUUID != null) {
            double exploitMult = AntiExploitManager.getInstance().checkAndGetMultiplier(playerUUID, amount, afterTax);
            afterTax *= exploitMult;
        }

        // Tracking & XP
        if (playerUUID != null) {
            GlobalEconomyTracker.getInstance().onSale(playerUUID, category, amount, afterTax);
            updateSupplyOnSale(productId, amount);

            // ProducerLevel XP vergeben
            try {
                XPSource xpSource = category.isIllegal() ? XPSource.SELL_ILLEGAL : XPSource.SELL_LEGAL;
                ProducerLevel.getInstance().awardSaleXP(playerUUID, xpSource, amount,
                        qualityMult, afterTax);
            } catch (Exception e) {
                LOGGER.debug("Could not award sale XP: {}", e.getMessage());
            }
        }

        LOGGER.debug("Sell price for {}(q={}, amt={}): {:.2f}€ (tax: {:.0f}%)",
                productId, quality.getDisplayName(), amount, afterTax, taxRate * 100);

        return afterTax;
    }

    /**
     * Vereinfachte Version für Verkauf ohne Quality-Objekt.
     *
     * @param productId        Produkt-Identifier
     * @param qualityMultiplier Qualitäts-Multiplikator direkt
     * @param amount           Menge
     * @param playerUUID       Spieler-UUID
     * @return Finaler Verkaufspreis
     */
    public double getSellPrice(String productId, double qualityMultiplier, int amount,
                                @Nullable UUID playerUUID) {
        double refPrice = getRefPrice(productId);
        ItemCategory category = getCategory(productId);

        double sdMult = getSupplyDemandMultiplier(productId);
        double riskMult = RiskPremium.calculateRiskMultiplier(category, globalWantedLevel);
        double eventMult = getEventMultiplier(productId);
        double inflationAdj = GlobalEconomyTracker.getInstance().getInflationAdjustment();

        double price = UnifiedPriceCalculator.calculatePrice(
                refPrice, qualityMultiplier, sdMult, riskMult,
                cycleMultiplier * inflationAdj, eventMult,
                category, amount
        );

        double taxRate = UnifiedPriceCalculator.getTaxRate(category);
        double afterTax = price * (1.0 - taxRate);

        if (playerUUID != null) {
            GlobalEconomyTracker.getInstance().onSale(playerUUID, category, amount, afterTax);
            updateSupplyOnSale(productId, amount);

            // ProducerLevel XP vergeben
            try {
                XPSource xpSource = category.isIllegal() ? XPSource.SELL_ILLEGAL : XPSource.SELL_LEGAL;
                ProducerLevel.getInstance().awardSaleXP(playerUUID, xpSource, amount,
                        qualityMultiplier, afterTax);
            } catch (Exception e) {
                LOGGER.debug("Could not award sale XP: {}", e.getMessage());
            }
        }

        return afterTax;
    }

    // ═══════════════════════════════════════════════════════════
    // HAUPT-API: EINKAUFS-PREIS (was der Spieler zahlt)
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den Einkaufspreis für ein Item (Samen, Maschinen, Shop-Waren).
     *
     * @param productId Produkt-Identifier
     * @param amount    Menge
     * @return Finaler Einkaufspreis
     */
    public double getBuyPrice(String productId, int amount) {
        double refPrice = getRefPrice(productId);
        ItemCategory category = getCategory(productId);

        double sdMult = getSupplyDemandMultiplier(productId);
        double inflationAdj = GlobalEconomyTracker.getInstance().getInflationAdjustment();

        double unitPrice = UnifiedPriceCalculator.calculateBuyPrice(
                refPrice, sdMult, cycleMultiplier * inflationAdj, category
        );

        return unitPrice * amount;
    }

    /**
     * Berechnet den Einkaufspreis für ein Shop-Item mit fester Basis.
     *
     * @param baseShopPrice Basis-Shop-Preis (aus ShopEntry)
     * @param category      Item-Kategorie
     * @param amount        Menge
     * @return Dynamischer Einkaufspreis
     */
    public double getDynamicShopPrice(double baseShopPrice, ItemCategory category, int amount) {
        double sdMult = 1.0; // Shop-Items haben kein eigenes S&D
        double inflationAdj = GlobalEconomyTracker.getInstance().getInflationAdjustment();

        double unitPrice = UnifiedPriceCalculator.calculateBuyPrice(
                baseShopPrice, sdMult, cycleMultiplier * inflationAdj, category
        );

        return unitPrice * amount;
    }

    // ═══════════════════════════════════════════════════════════
    // HAUPT-API: GEHÄLTER & REWARDS
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet das harmonisierte Gehalt.
     *
     * @param baseSalary Basis-Gehalt des Jobs
     * @return Angepasstes Gehalt
     */
    public double getHarmonizedSalary(double baseSalary) {
        return UnifiedPriceCalculator.calculateSalary(baseSalary, cycleMultiplier);
    }

    /**
     * Berechnet den harmonisierten Daily Reward.
     *
     * @param baseReward Basis-Belohnung
     * @return Angepasste Belohnung
     */
    public double getHarmonizedDailyReward(double baseReward) {
        return UnifiedPriceCalculator.calculateDailyReward(baseReward, cycleMultiplier);
    }

    // ═══════════════════════════════════════════════════════════
    // HAUPT-API: LIEFERKOSTEN (dynamisch via UDPS)
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den dynamischen Lieferpreis für Warehouse-Lieferungen.
     *
     * Lieferkosten schwanken mit Wirtschaftszyklus und Inflation,
     * sind aber weniger volatil als Verkaufspreise (60% Zyklus-Dämpfung).
     *
     * Formel: baseDeliveryPrice × dampedCycle × inflationAdjustment × amount
     *
     * @param baseDeliveryPrice Basis-Lieferpreis pro Stück (aus DeliveryPriceConfig)
     * @param amount            Menge
     * @return Dynamischer Gesamt-Lieferpreis
     */
    public double getDeliveryPrice(double baseDeliveryPrice, int amount) {
        double inflationAdj = GlobalEconomyTracker.getInstance().getInflationAdjustment();
        // Lieferkosten schwanken mit 60% der Zyklusstärke (moderat volatil)
        double dampedCycle = 1.0 + (cycleMultiplier - 1.0) * 0.6;
        double unitPrice = baseDeliveryPrice * dampedCycle * inflationAdj;
        double total = Math.max(0.01, unitPrice) * amount;

        LOGGER.debug("Delivery price: base={:.2f} × cycle={:.2f} × infl={:.2f} × {} = {:.2f}€",
                baseDeliveryPrice, dampedCycle, inflationAdj, amount, total);

        return total;
    }

    /**
     * Gibt den aktuellen Lieferkosten-Multiplikator zurück (für UI-Anzeige).
     * Kombiniert Wirtschaftszyklus (60% gedämpft) und Inflation.
     *
     * @return Multiplikator relativ zum Basispreis (1.0 = neutral)
     */
    public double getDeliveryCostMultiplier() {
        double inflationAdj = GlobalEconomyTracker.getInstance().getInflationAdjustment();
        double dampedCycle = 1.0 + (cycleMultiplier - 1.0) * 0.6;
        return dampedCycle * inflationAdj;
    }

    // ═══════════════════════════════════════════════════════════
    // PREIS-INFO (für UI/Tooltip-Anzeige)
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt Preis-Informationen für die UI zurück.
     *
     * @param productId Produkt-Identifier
     * @return Formatierter Preis-Info-String
     */
    public String getPriceInfo(String productId) {
        double refPrice = getRefPrice(productId);
        ItemCategory category = getCategory(productId);
        double currentSellPrice = getSellPrice(productId, 1.0, 1, null);
        double[] range = PriceBounds.getRange(refPrice, category);

        StringBuilder sb = new StringBuilder();
        sb.append("§6Preis: §f").append(String.format("%.2f€", currentSellPrice));
        sb.append("\n§7Bereich: ").append(String.format("%.2f€ - %.2f€", range[0], range[1]));

        // Trend
        MarketData md = marketDataMap.get(productId);
        if (md != null) {
            MarketData.PriceTrend trend = md.getPriceTrend();
            sb.append("\n§7Trend: ").append(trend.getFormatted());
        }

        // Risiko
        if (category.isIllegal()) {
            sb.append("\n").append(RiskPremium.getFormattedRiskInfo(category, globalWantedLevel));
        }

        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════
    // CYCLE & EVENT INTEGRATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird vom EconomyCycle aufgerufen um den Zyklus-Multiplikator zu setzen.
     */
    public void setCycleMultiplier(double multiplier) {
        this.cycleMultiplier = multiplier;
        LOGGER.info("Economy cycle multiplier set to: {:.2f}", multiplier);
    }

    public double getCycleMultiplier() {
        return cycleMultiplier;
    }

    /**
     * Setzt das globale Wanted-Level (Durchschnitt aller Spieler).
     */
    public void setGlobalWantedLevel(int level) {
        this.globalWantedLevel = Math.max(0, Math.min(5, level));
    }

    public int getGlobalWantedLevel() {
        return globalWantedLevel;
    }

    // ═══════════════════════════════════════════════════════════
    // REFERENZ-PREIS MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen Referenz-Preis und Kategorie für ein Produkt.
     */
    public void registerProduct(String productId, double referencePrice, ItemCategory category) {
        referencePrices.put(productId, referencePrice);
        itemCategories.put(productId, category);
        LOGGER.debug("Product registered: {} = {:.2f}€ ({})", productId, referencePrice, category.name());
    }

    /**
     * Gibt den Referenz-Preis eines Produkts zurück.
     */
    public double getRefPrice(String productId) {
        return referencePrices.getOrDefault(productId, 10.0); // Fallback 10€
    }

    /**
     * Gibt die Kategorie eines Produkts zurück.
     */
    public ItemCategory getCategory(String productId) {
        return itemCategories.getOrDefault(productId, ItemCategory.OTHER);
    }

    // ═══════════════════════════════════════════════════════════
    // S&D INTEGRATION
    // ═══════════════════════════════════════════════════════════

    private double getSupplyDemandMultiplier(String productId) {
        MarketData md = marketDataMap.get(productId);
        if (md != null) {
            return md.getPriceMultiplier();
        }
        return 1.0; // Kein S&D Daten = neutraler Markt
    }

    private double getEventMultiplier(String productId) {
        ItemCategory category = getCategory(productId);
        return categoryEventMultipliers.getOrDefault(category, 1.0);
    }

    /**
     * Aktualisiert Event-Multiplikatoren aus PriceManager.
     * Wird periodisch aufgerufen um PriceManager-Events in Kategorie-Multiplikatoren umzurechnen.
     */
    public void refreshEventMultipliers() {
        categoryEventMultipliers.clear();

        java.util.List<EconomicEvent> events = PriceManager.getActiveEvents();
        if (events.isEmpty()) return;

        // PriceManager Events haben einheitliche Multiplikatoren pro Event
        // Wir aggregieren diese zu Kategorie-Multiplikatoren
        for (EconomicEvent event : events) {
            float avgMult = getAverageEventMultiplier(event);
            if (avgMult == 1.0f) continue;

            for (ItemCategory cat : mapEventToCategories(event)) {
                categoryEventMultipliers.merge(cat, (double) avgMult, (a, b) -> a * b);
            }
        }
    }

    /**
     * Extrahiert den durchschnittlichen Multiplikator eines Events.
     */
    private float getAverageEventMultiplier(EconomicEvent event) {
        // Da PriceManager.createEvent() alle Items mit dem gleichen Multiplikator erstellt,
        // ist der Durchschnitt = der einheitliche Multiplikator
        // Wir testen ein paar bekannte Items um den Multiplikator zu finden
        float testMult = event.getMultiplier(null); // null gibt 1.0f zurück
        if (testMult != 1.0f) return testMult;

        // Versuche bekannte Items
        try {
            var testItems = new net.minecraft.world.item.Item[] {
                de.rolandsw.schedulemc.cannabis.items.CannabisItems.CURED_BUD.get(),
                de.rolandsw.schedulemc.coca.items.CocaItems.COCAINE.get(),
                de.rolandsw.schedulemc.meth.items.MethItems.METH.get(),
                de.rolandsw.schedulemc.tobacco.items.TobaccoItems.FERMENTED_VIRGINIA_LEAF.get()
            };
            for (var item : testItems) {
                if (item != null && event.affectsItem(item)) {
                    return event.getMultiplier(item);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error calculating event multiplier: {}", e.getMessage());
        }

        return 1.0f;
    }

    /**
     * Ordnet einen Event den betroffenen ItemCategories zu.
     */
    private java.util.List<ItemCategory> mapEventToCategories(EconomicEvent event) {
        java.util.List<ItemCategory> categories = new java.util.ArrayList<>();
        String name = event.getName().toLowerCase();

        if (name.contains("cannabis")) categories.add(ItemCategory.CANNABIS);
        if (name.contains("kokain") || name.contains("coca")) categories.add(ItemCategory.COCAINE);
        if (name.contains("meth")) categories.add(ItemCategory.METH);
        if (name.contains("heroin") || name.contains("mohn")) categories.add(ItemCategory.HEROIN);
        if (name.contains("mdma") || name.contains("ecstasy") || name.contains("techno")) {
            categories.add(ItemCategory.MDMA);
            categories.add(ItemCategory.LSD);
        }
        if (name.contains("pilz") || name.contains("mushroom")) categories.add(ItemCategory.MUSHROOM);
        if (name.contains("festival") || name.contains("party")) {
            categories.addAll(java.util.List.of(ItemCategory.CANNABIS, ItemCategory.MDMA, ItemCategory.LSD, ItemCategory.COCAINE));
        }
        if (name.contains("konkurrenz") || name.contains("grenz")) {
            for (ItemCategory cat : ITEM_CATEGORIES) {
                if (cat.isIllegal()) categories.add(cat);
            }
        }
        if (name.contains("dürre") || name.contains("pflanz")) {
            categories.addAll(java.util.List.of(ItemCategory.CANNABIS, ItemCategory.COCAINE, ItemCategory.TOBACCO_PRODUCT));
        }
        if (name.contains("chemikalien") || name.contains("synthet")) {
            categories.addAll(java.util.List.of(ItemCategory.METH, ItemCategory.MDMA, ItemCategory.LSD));
        }
        if (name.contains("uni") || name.contains("stimul")) {
            categories.addAll(java.util.List.of(ItemCategory.METH, ItemCategory.COCAINE));
        }
        if (name.contains("vip")) categories.add(ItemCategory.COCAINE);

        return categories;
    }

    private void updateSupplyOnSale(String productId, int amount) {
        MarketData md = marketDataMap.get(productId);
        if (md != null) {
            md.onItemSold(amount);
        }
    }

    /**
     * Registriert MarketData für ein Produkt.
     */
    public void registerMarketData(String productId, MarketData data) {
        marketDataMap.put(productId, data);
    }

    /**
     * Gibt MarketData für ein Produkt zurück.
     */
    @Nullable
    public MarketData getMarketData(String productId) {
        return marketDataMap.get(productId);
    }

    // ═══════════════════════════════════════════════════════════
    // KALIBRIERTE REFERENZ-PREISE INITIALISIEREN
    // ═══════════════════════════════════════════════════════════

    /**
     * Initialisiert alle kalibrierten Referenz-Preise.
     *
     * Kalibrierung basiert auf:
     * - Tages-Referenz-Einkommen: 150€
     * - Tägliche Essenskosten: 20€
     * - Level 1 Spieler soll 50-80€/Tag netto verdienen
     * - Level 30 Spieler soll 400-800€/Tag netto verdienen
     * - Illegale Produkte haben höhere Margen aber Risiko
     *
     * HINWEIS: Diese Referenz-Preise werden in Phase 6 von den
     * jeweiligen Type-Enums via registerProduct() überschrieben.
     * Hier stehen nur die Default-Fallbacks.
     */
    private void initializeReferencePrices() {
        // ═══ CANNABIS ═══
        // Rohstoff-Invest: ~25€ Samen + ~7€ Erde/Wasser = ~32€
        // Ertrag: 6 Buds pro Ernte, ~2 Ernten/Tag = 12 Buds/Tag
        // Ziel: ~60-100€/Tag bei Standard-Qualität
        registerProduct("CANNABIS_INDICA", 12.0, ItemCategory.CANNABIS);
        registerProduct("CANNABIS_SATIVA", 14.0, ItemCategory.CANNABIS);
        registerProduct("CANNABIS_HYBRID", 16.0, ItemCategory.CANNABIS);
        registerProduct("CANNABIS_AUTOFLOWER", 9.0, ItemCategory.CANNABIS);

        // ═══ TABAK ═══
        // Rohstoff-Invest: ~10-30€ Samen
        // Ertrag: ~6 Blätter, verarbeitet zu Produkten
        registerProduct("TOBACCO_VIRGINIA", 6.0, ItemCategory.TOBACCO_PRODUCT);
        registerProduct("TOBACCO_BURLEY", 8.0, ItemCategory.TOBACCO_PRODUCT);
        registerProduct("TOBACCO_ORIENTAL", 10.0, ItemCategory.TOBACCO_PRODUCT);
        registerProduct("TOBACCO_HAVANA", 14.0, ItemCategory.TOBACCO_PRODUCT);

        // ═══ KOKAIN ═══
        // Höhere Investition, höherer Gewinn, höheres Risiko
        registerProduct("COCA_BOLIVIANISCH", 25.0, ItemCategory.COCAINE);
        registerProduct("COCA_PERUANISCH", 35.0, ItemCategory.COCAINE);
        registerProduct("COCA_KOLUMBIANISCH", 50.0, ItemCategory.COCAINE);
        registerProduct("CRACK_ROCK", 40.0, ItemCategory.COCAINE); // Verarbeitungsprodukt

        // ═══ HEROIN (Mohn) ═══
        // Höchste Investition, höchster Gewinn, höchstes Risiko
        registerProduct("POPPY_INDISCH", 20.0, ItemCategory.HEROIN);
        registerProduct("POPPY_TUERKISCH", 35.0, ItemCategory.HEROIN);
        registerProduct("POPPY_AFGHANISCH", 55.0, ItemCategory.HEROIN);

        // ═══ METH ═══
        registerProduct("METH_STANDARD", 30.0, ItemCategory.METH);
        registerProduct("METH_GUT", 50.0, ItemCategory.METH);
        registerProduct("METH_BLUE_SKY", 80.0, ItemCategory.METH);

        // ═══ MDMA ═══
        registerProduct("MDMA_SCHLECHT", 8.0, ItemCategory.MDMA);
        registerProduct("MDMA_STANDARD", 18.0, ItemCategory.MDMA);
        registerProduct("MDMA_GUT", 30.0, ItemCategory.MDMA);
        registerProduct("MDMA_PREMIUM", 50.0, ItemCategory.MDMA);

        // ═══ LSD ═══
        registerProduct("LSD_SCHWACH", 15.0, ItemCategory.LSD);
        registerProduct("LSD_STANDARD", 25.0, ItemCategory.LSD);
        registerProduct("LSD_STARK", 40.0, ItemCategory.LSD);
        registerProduct("LSD_BICYCLE_DAY", 70.0, ItemCategory.LSD);

        // ═══ PILZE ═══
        registerProduct("MUSHROOM_MEXICANA", 10.0, ItemCategory.MUSHROOM);
        registerProduct("MUSHROOM_CUBENSIS", 18.0, ItemCategory.MUSHROOM);
        registerProduct("MUSHROOM_AZURESCENS", 35.0, ItemCategory.MUSHROOM);

        // ═══ WEIN ═══
        // Legal, kein Risiko, niedrigere Marge
        registerProduct("WINE_RIESLING", 8.0, ItemCategory.WINE);
        registerProduct("WINE_CHARDONNAY", 12.0, ItemCategory.WINE);
        registerProduct("WINE_SPAETBURGUNDER", 15.0, ItemCategory.WINE);
        registerProduct("WINE_MERLOT", 20.0, ItemCategory.WINE);

        // ═══ BIER ═══
        registerProduct("BEER_PILSNER", 5.0, ItemCategory.BEER);
        registerProduct("BEER_WEIZEN", 6.0, ItemCategory.BEER);
        registerProduct("BEER_ALE", 7.0, ItemCategory.BEER);
        registerProduct("BEER_STOUT", 9.0, ItemCategory.BEER);

        // ═══ KAFFEE ═══
        registerProduct("COFFEE_ARABICA", 6.0, ItemCategory.COFFEE);
        registerProduct("COFFEE_ROBUSTA", 8.0, ItemCategory.COFFEE);
        registerProduct("COFFEE_LIBERICA", 12.0, ItemCategory.COFFEE);
        registerProduct("COFFEE_EXCELSA", 18.0, ItemCategory.COFFEE);

        // ═══ KÄSE ═══
        registerProduct("CHEESE_GOUDA", 7.0, ItemCategory.CHEESE);
        registerProduct("CHEESE_EMMENTAL", 10.0, ItemCategory.CHEESE);
        registerProduct("CHEESE_CAMEMBERT", 13.0, ItemCategory.CHEESE);
        registerProduct("CHEESE_PARMESAN", 17.0, ItemCategory.CHEESE);

        // ═══ SCHOKOLADE ═══
        registerProduct("CHOCOLATE_WHITE", 5.0, ItemCategory.CHOCOLATE);
        registerProduct("CHOCOLATE_MILK", 6.0, ItemCategory.CHOCOLATE);
        registerProduct("CHOCOLATE_DARK", 9.0, ItemCategory.CHOCOLATE);
        registerProduct("CHOCOLATE_RUBY", 14.0, ItemCategory.CHOCOLATE);

        // ═══ HONIG ═══
        registerProduct("HONEY_ACACIA", 6.0, ItemCategory.HONEY);
        registerProduct("HONEY_WILDFLOWER", 8.0, ItemCategory.HONEY);
        registerProduct("HONEY_FOREST", 11.0, ItemCategory.HONEY);
        registerProduct("HONEY_MANUKA", 18.0, ItemCategory.HONEY);

        // ═══ FAHRZEUGE ═══
        registerProduct("VEHICLE_OAK", 5000.0, ItemCategory.VEHICLE);
        registerProduct("VEHICLE_BIG_OAK", 7500.0, ItemCategory.VEHICLE);
        registerProduct("VEHICLE_SUV", 10000.0, ItemCategory.VEHICLE);
        registerProduct("VEHICLE_TRANSPORTER", 12000.0, ItemCategory.VEHICLE);
        registerProduct("VEHICLE_SPORT", 15000.0, ItemCategory.VEHICLE);

        // ═══ TÖPFE ═══
        registerProduct("POT_TERRACOTTA", 20.0, ItemCategory.POT);
        registerProduct("POT_CERAMIC", 40.0, ItemCategory.POT);
        registerProduct("POT_IRON", 80.0, ItemCategory.POT);
        registerProduct("POT_GOLDEN", 150.0, ItemCategory.POT);

        // ═══ NAHRUNG ═══
        registerProduct("FOOD_BREAD", 3.75, ItemCategory.FOOD);
        registerProduct("FOOD_COOKED_BEEF", 7.5, ItemCategory.FOOD);
        registerProduct("FOOD_COOKED_PORKCHOP", 6.875, ItemCategory.FOOD);
        registerProduct("FOOD_APPLE", 1.25, ItemCategory.FOOD);
        registerProduct("FOOD_GOLDEN_APPLE", 200.0, ItemCategory.FOOD);
        registerProduct("FOOD_CARROT", 0.9375, ItemCategory.FOOD);
        registerProduct("FOOD_POTATO", 0.9375, ItemCategory.FOOD);
        registerProduct("FOOD_CAKE", 80.0, ItemCategory.FOOD);
        registerProduct("FOOD_COOKIE", 0.78125, ItemCategory.FOOD);

        LOGGER.info("EconomyController initialized with {} reference prices", referencePrices.size());
    }

    // ═══════════════════════════════════════════════════════════
    // PERIODISCHE UPDATES
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird alle 5 Minuten Echtzeit aufgerufen.
     * Aktualisiert Tracking-Daten und decayed S&D.
     */
    public void periodicUpdate() {
        GlobalEconomyTracker.getInstance().updateMoneySupplyStats();

        // S&D Decay für alle registrierten MarketDatas
        for (MarketData md : marketDataMap.values()) {
            md.decaySupply(0.02);  // 2% Supply Decay pro Intervall
            md.decayDemand(0.02); // 2% Demand Decay pro Intervall
        }

        // Razzia-Daten Decay
        RiskPremium.decayRaidData();

        // Event-Multiplikatoren aus PriceManager aktualisieren
        PriceManager.removeExpiredEvents();
        refreshEventMultipliers();

        // EconomyCycle-Multiplikator aktualisieren
        try {
            EconomyCycle cycle = EconomyCycle.getInstance();
            this.cycleMultiplier = cycle.getCurrentMultiplier();
        } catch (Exception e) {
            LOGGER.debug("EconomyCycle not available, using default multiplier");
        }

        LOGGER.debug("Periodic economy update completed");
    }

    /**
     * Wird täglich (Minecraft-Tag) aufgerufen.
     */
    public void onNewDay() {
        GlobalEconomyTracker.getInstance().onNewDay();

        // EconomyCycle tägliches Update
        try {
            EconomyCycle cycle = EconomyCycle.getInstance();
            cycle.onNewDay();
            this.cycleMultiplier = cycle.getCurrentMultiplier();
        } catch (Exception e) {
            LOGGER.debug("EconomyCycle not available for daily update");
        }

        // PriceManager tägliche Event-Prüfung
        PriceManager.checkDailyEvents();
        refreshEventMultipliers();

        // Anti-Exploit täglicher Reset
        AntiExploitManager.getInstance().onNewDay();

        LOGGER.info("New economy day started. Cycle multiplier: {:.2f}", cycleMultiplier);
    }

    /**
     * Wird bei Server-Shutdown aufgerufen.
     */
    public void onServerShutdown() {
        GlobalEconomyTracker.getInstance().saveData();
        EconomyCycle.getInstance().save();
        PriceManager.clearAllEvents();
        LOGGER.info("EconomyController shutdown - data saved");
    }

    /**
     * Wird bei Server-Start aufgerufen.
     */
    public void onServerStart() {
        GlobalEconomyTracker.getInstance().loadData();
        GlobalEconomyTracker.getInstance().updateMoneySupplyStats();

        // EconomyCycle laden und Multiplikator setzen
        try {
            EconomyCycle cycle = EconomyCycle.getInstance();
            cycle.loadData();
            this.cycleMultiplier = cycle.getCurrentMultiplier();
        } catch (Exception e) {
            LOGGER.warn("Could not load EconomyCycle, using default multiplier");
        }

        LOGGER.info("EconomyController started - {} products registered, cycle={:.2f}",
                referencePrices.size(), cycleMultiplier);
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt eine Zusammenfassung des Wirtschaftszustands zurück.
     */
    public String getEconomySummary() {
        GlobalEconomyTracker tracker = GlobalEconomyTracker.getInstance();
        return String.format(
                "§6═══ Wirtschafts-Übersicht ═══\n" +
                "§7Geldmenge: §f%.0f€\n" +
                "§7Spieler: §f%d\n" +
                "§7Ø Vermögen: §f%.0f€\n" +
                "§7Inflation: §f%.2f%%\n" +
                "§7Zyklus-Mult.: §f%.2fx\n" +
                "§7Tages-Volumen: §f%.0f€\n" +
                "§7Produkte: §f%d registriert",
                tracker.getTotalMoneySupply(),
                tracker.getActivePlayerCount(),
                tracker.getAveragePlayerWealth(),
                tracker.getInflationRate() * 100,
                cycleMultiplier,
                tracker.getDailyTransactionVolume(),
                referencePrices.size()
        );
    }
}
