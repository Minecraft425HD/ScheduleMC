package de.rolandsw.schedulemc.npc.life.economy;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;

import de.rolandsw.schedulemc.economy.ItemCategory;
import de.rolandsw.schedulemc.market.MarketData;
import de.rolandsw.schedulemc.market.SeasonalPriceModifier;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * DynamicPriceManager - Verwaltet dynamische Preise und Marktbedingungen mit JSON-Persistenz
 *
 * Features:
 * - Globale Marktbedingungen
 * - Kategorie-spezifische Bedingungen
 * - Zeitbasierte Schwankungen
 * - Event-basierte Änderungen
 * - Per-Item Supply&Demand-Tracking für NPC-Shop-Items (ehemals DynamicMarketManager,
 *   siehe CLAUDE.md "DynamicPriceManager/DynamicMarketManager Merge" 2026-09-25)
 */
public class DynamicPriceManager extends AbstractPersistenceManager<DynamicPriceManager.DynamicPriceManagerData> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile DynamicPriceManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static DynamicPriceManager getInstance() {
        return instance;
    }

    public static DynamicPriceManager initialize(MinecraftServer server) {
        DynamicPriceManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new DynamicPriceManager(server);
                }
            }
        }
        return result;
    }

    /**
     * Gets manager instance for a specific level (convenience method).
     * Note: Manager is server-wide, not per-level.
     */
    public static DynamicPriceManager getManager(ServerLevel level) {
        return getInstance();
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** 1 Minute = 1200 Ticks (20 Ticks/Sekunde) */
    private static final long TICKS_PER_MINUTE = 1200L;

    /** Untere Grenze der zufälligen Preisschwankung (ergibt ±5%-Bereich: 0.95–1.05) */
    static final float PRICE_VARIANCE_MIN = 0.95f;

    /** Bandbreite der zufälligen Preisschwankung (10%) */
    static final float PRICE_VARIANCE_RANGE = 0.1f;

    /**
     * Anzahl Minecraft-Tage, über die der tatsächlich für Item-/Produkt-Preise genutzte
     * S&D-Multiplikator geglättet wird (gleitender Durchschnitt). Der rohe S&D-Wert wird
     * weiterhin laufend aktualisiert (Supply/Demand ändern sich sofort bei jedem Kauf/Verkauf),
     * aber der EFFEKTIVE, für Preise genutzte Multiplikator wird nur 1x pro Minecraft-Tag
     * (bei Tageswechsel) neu in dieses Fenster eingerollt — spürbare, aber gedämpfte Bewegung
     * statt sofortiger Preissprünge bei jeder einzelnen Transaktion.
     */
    private static final int PRICE_SMOOTHING_WINDOW_DAYS = 7;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Globale Marktbedingung */
    private MarketCondition globalCondition = MarketCondition.NORMAL;

    /** Kategorie-spezifische Bedingungen */
    private final Map<String, MarketCondition> categoryConditions = new ConcurrentHashMap<>();

    /** Zusätzliche temporäre Modifikatoren */
    private final Map<String, TemporaryModifier> temporaryModifiers = new ConcurrentHashMap<>();

    /** Preis-History für Analysen */
    private final Deque<PriceSnapshot> priceHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY = 30; // 30 Tage

    /** Letzter bekannter Tag */
    private long lastKnownDay = -1;

    /** Tick, zu dem zuletzt updateMarketConditions() lief (DYNAMIC_PRICING_UPDATE_INTERVAL_MINUTES) */
    private long lastMarketUpdateTick = -1;

    /** Per-Item Supply&Demand-Daten für NPC-Shop-Items (ehemals DynamicMarketManager) */
    private final Map<Item, MarketData> itemMarketData = new ConcurrentHashMap<>();

    /** Item -> Saisonal-Kategorie (für SeasonalPriceModifier), sofern zuordenbar */
    private final Map<Item, String> itemSeasonalCategory = new ConcurrentHashMap<>();

    /** Anzahl der bisher durchgeführten Item-Markt-Updates (Decay-Zyklen) */
    private long totalItemMarketUpdates = 0;

    /**
     * Supply&Demand-Zustand für abstrakte UDPS-Produkte (String-Key, z.B. "CANNABIS_INDICA").
     * Ersetzt EconomyController.marketDataMap, das nie befüllt wurde ({@code registerMarketData()}
     * hatte keinen Aufrufer) — jetzt der einzige S&D-Speicher im gesamten Mod.
     */
    private final Map<String, ProductMarketState> productMarketData = new ConcurrentHashMap<>();

    /**
     * Item -> geglätteter (7-Minecraft-Tage-Durchschnitt) Preismultiplikator, nur 1x pro
     * Minecraft-Tag aktualisiert. Das Gegenstück für {@link #productMarketData} trägt seine
     * Glättungsdaten direkt in {@link ProductMarketState}, da es dort ohnehin schon ein
     * eigenes, pro Produkt persistiertes Objekt gibt.
     */
    private final Map<Item, PriceSmoothingState> itemPriceSmoothing = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private DynamicPriceManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_life_prices.json").toFile(),
            GsonHelper.get()
        );
        load();
    }

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Tick aufgerufen
     */
    public void tick(ServerLevel level) {
        // Temporäre Modifikatoren aktualisieren
        temporaryModifiers.entrySet().removeIf(e -> {
            e.getValue().ticksRemaining--;
            if (e.getValue().ticksRemaining <= 0) {
                markDirty();
                return true;
            }
            return false;
        });

        long currentTick = level.getDayTime();

        // Markt-Update-Intervall (konfigurierbar in Minuten, unabhaengig vom Tageswechsel)
        if (ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.get()) {
            long ticksPerUpdate = ModConfigHandler.COMMON.DYNAMIC_PRICING_UPDATE_INTERVAL_MINUTES.get() * TICKS_PER_MINUTE;
            if (lastMarketUpdateTick == -1 || currentTick - lastMarketUpdateTick >= ticksPerUpdate) {
                updateMarketConditions();
                updateItemMarketData();
                updateProductMarketData();
                lastMarketUpdateTick = currentTick;
            }
        }

        // Tageswechsel prüfen (Saison + Snapshot bleiben tagesbasiert)
        long currentDay = currentTick / 24000;
        if (lastKnownDay != -1 && currentDay > lastKnownDay) {
            onDayChange(currentDay, level);
        }
        lastKnownDay = currentDay;
    }

    /**
     * Wird bei Tageswechsel aufgerufen
     */
    private void onDayChange(long currentDay, ServerLevel level) {
        // Saison aktualisieren (Serene Seasons falls installiert, sonst Spieltage-Zyklus)
        SeasonalPriceModifier.getInstance().updateSeason(currentDay, level);

        // Lagerbestände scannen (1x/Tag, NICHT in Echtzeit) - fließt über
        // computeRawItemMultiplier() in den unten folgenden Gleitdurchschnitt mit ein.
        de.rolandsw.schedulemc.economy.WarehouseMarketBridge.getInstance().updateWarehouseData();

        // Item-/Produkt-Preise: 1x/Tag neu in den 7-Tage-Gleitdurchschnitt einrollen
        updatePriceSmoothingSnapshots();

        // Snapshot speichern
        savePriceSnapshot(currentDay);
        markDirty();
    }

    /**
     * Aktualisiert die Marktbedingungen
     */
    private void updateMarketConditions() {
        double changeChance = ModConfigHandler.COMMON.DYNAMIC_PRICING_SD_FACTOR.get();

        // Globale Bedingung möglicherweise ändern
        if (ThreadLocalRandom.current().nextDouble() < changeChance) {
            MarketCondition[] possible = globalCondition.getPossibleTransitions();
            if (possible.length > 0) {
                // Gewichtete Auswahl
                float roll = (float) ThreadLocalRandom.current().nextDouble();
                float cumulative = 0;
                for (MarketCondition condition : possible) {
                    cumulative += globalCondition.getTransitionChance(condition);
                    if (roll < cumulative) {
                        globalCondition = condition;
                        markDirty();
                        break;
                    }
                }
            }
        }

        // Kategorie-Bedingungen ähnlich aktualisieren
        List<String> categoriesToUpdate = new ArrayList<>(categoryConditions.keySet());
        for (String category : categoriesToUpdate) {
            if (ThreadLocalRandom.current().nextDouble() < changeChance * 0.5) {
                MarketCondition current = categoryConditions.get(category);
                MarketCondition[] possible = current.getPossibleTransitions();
                if (possible.length > 0 && ThreadLocalRandom.current().nextDouble() < 0.5) {
                    categoryConditions.put(category, possible[ThreadLocalRandom.current().nextInt(possible.length)]);
                    markDirty();
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MARKET CONDITIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt die globale Marktbedingung zurück
     */
    public MarketCondition getGlobalMarketCondition() {
        return globalCondition;
    }

    /**
     * Setzt die globale Marktbedingung
     */
    public void setGlobalMarketCondition(MarketCondition condition) {
        this.globalCondition = condition;
        markDirty();
    }

    /**
     * Alias für setGlobalMarketCondition - setzt die Marktbedingung
     */
    public void setMarketCondition(MarketCondition condition) {
        setGlobalMarketCondition(condition);
    }

    /**
     * Gibt die Marktbedingung für eine Kategorie zurück
     */
    public MarketCondition getCategoryCondition(String category) {
        return categoryConditions.getOrDefault(category, globalCondition);
    }

    /**
     * Setzt die Marktbedingung für eine Kategorie
     */
    public void setCategoryCondition(String category, MarketCondition condition) {
        categoryConditions.put(category, condition);
        markDirty();
    }

    /**
     * Entfernt die Kategorie-spezifische Bedingung (fällt auf global zurück)
     */
    public void resetCategoryCondition(String category) {
        categoryConditions.remove(category);
        markDirty();
    }

    // ═══════════════════════════════════════════════════════════
    // TEMPORARY MODIFIERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt einen temporären Modifikator hinzu
     */
    public void addTemporaryModifier(String id, float modifier, int durationTicks, String reason) {
        temporaryModifiers.put(id, new TemporaryModifier(modifier, durationTicks, reason));
        markDirty();
    }

    /**
     * Entfernt einen temporären Modifikator
     */
    public void removeTemporaryModifier(String id) {
        temporaryModifiers.remove(id);
        markDirty();
    }

    /**
     * Berechnet den kombinierten temporären Modifikator
     */
    public float getCombinedTemporaryModifier() {
        if (temporaryModifiers.isEmpty()) return 1.0f;

        float combined = 1.0f;
        for (TemporaryModifier mod : temporaryModifiers.values()) {
            combined *= mod.modifier;
        }
        return combined;
    }

    // ═══════════════════════════════════════════════════════════
    // PRICE CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den finalen Preis für ein Item
     *
     * @param basePrice Basispreis des Items
     * @param category Kategorie des Items (z.B. "FOOD", "WEAPONS")
     * @return Angepasster Preis
     */
    public int calculatePrice(int basePrice, String category) {
        float modifier = 1.0f;

        // Kategorie-Bedingung oder global
        MarketCondition condition = getCategoryCondition(category);
        modifier *= condition.getPriceMultiplier();

        // Saisonaler Modifikator
        modifier *= SeasonalPriceModifier.getInstance().getModifier(category);

        // Temporäre Modifikatoren
        modifier *= getCombinedTemporaryModifier();

        // Zufällige kleine Schwankung (±5%)
        modifier *= PRICE_VARIANCE_MIN + (float) ThreadLocalRandom.current().nextDouble() * PRICE_VARIANCE_RANGE;

        // Globale Multiplikator-Grenzen (verhindert unbegrenzte Preisexplosion/-verfall
        // bei gestapelten Bedingungen/Saison/temporaeren Modifikatoren)
        float minMultiplier = ModConfigHandler.COMMON.DYNAMIC_PRICING_MIN_MULTIPLIER.get().floatValue();
        float maxMultiplier = ModConfigHandler.COMMON.DYNAMIC_PRICING_MAX_MULTIPLIER.get().floatValue();
        modifier = Math.max(minMultiplier, Math.min(maxMultiplier, modifier));

        return Math.max(1, Math.round(basePrice * modifier));
    }

    /**
     * Berechnet den Preis mit NPC-Modifikatoren
     */
    public int calculatePriceWithNPC(int basePrice, String category, float npcModifier) {
        int marketPrice = calculatePrice(basePrice, category);
        return Math.max(1, Math.round(marketPrice * npcModifier));
    }

    // ═══════════════════════════════════════════════════════════
    // ITEM MARKET (Supply & Demand für NPC-Shop-Items)
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert ein Item im Supply&Demand-Markt (falls noch nicht registriert).
     */
    public void registerItem(Item item, double basePrice) {
        registerItem(item, basePrice, (String) null);
    }

    /**
     * Registriert ein Item im Supply&Demand-Markt und ordnet es einer {@link ItemCategory}
     * für den saisonalen Preismodifikator zu (siehe {@link SeasonalPriceModifier}).
     */
    public void registerItem(Item item, double basePrice, ItemCategory economyCategory) {
        registerItem(item, basePrice, mapToSeasonalCategory(economyCategory));
    }

    private void registerItem(Item item, double basePrice, @Nullable String seasonalCategory) {
        if (!itemMarketData.containsKey(item)) {
            double sdFactor = ModConfigHandler.COMMON.DYNAMIC_PRICING_SD_FACTOR.get();
            double minMult = ModConfigHandler.COMMON.DYNAMIC_PRICING_MIN_MULTIPLIER.get();
            double maxMult = ModConfigHandler.COMMON.DYNAMIC_PRICING_MAX_MULTIPLIER.get();
            itemMarketData.put(item, new MarketData(item, basePrice, sdFactor, minMult, maxMult));
            markDirty();
        }
        if (seasonalCategory != null) {
            itemSeasonalCategory.put(item, seasonalCategory);
        }
    }

    /**
     * Ordnet eine Produkt-Kategorie (aus dem UDPS-System) der passenden
     * {@link SeasonalPriceModifier}-Kategorie zu, sofern ein saisonaler Bezug sinnvoll ist.
     */
    @Nullable
    private static String mapToSeasonalCategory(ItemCategory category) {
        return switch (category) {
            case CANNABIS, TOBACCO_PRODUCT, SEED_ILLEGAL, SEED_LEGAL, RAW_MATERIAL -> "PLANT";
            case MUSHROOM -> "MUSHROOM";
            case CHEMICAL, COCAINE, HEROIN, METH, MDMA, LSD -> "CHEMICAL";
            case FOOD, WINE, BEER, COFFEE, CHEESE, CHOCOLATE, HONEY -> "FOOD";
            case WEAPON -> "WEAPONS";
            case RARE_ITEM, VEHICLE, VEHICLE_UPGRADE -> "LUXURY";
            case BUILDING_MATERIAL -> "BUILDING";
            default -> null; // Maschinen, Töpfe, Werkzeuge, Dienstleistungen etc. — kein saisonaler Bezug
        };
    }

    /**
     * Item wurde an einen NPC verkauft (Spieler verkauft) - erhöht Supply.
     */
    public void onItemSoldToNPC(Item item, int amount) {
        if (!ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.get()) return;
        MarketData data = itemMarketData.get(item);
        if (data != null) {
            data.onItemSold(amount);
            markDirty();
        }
    }

    /**
     * Item wurde von einem NPC gekauft (Spieler kauft) - erhöht Demand.
     */
    public void onItemBoughtFromNPC(Item item, int amount) {
        if (!ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.get()) return;
        MarketData data = itemMarketData.get(item);
        if (data != null) {
            data.onItemBought(amount);
            markDirty();
        }
    }

    /**
     * Gibt den tatsächlich für Preise genutzten Multiplikator für ein Item zurück (S&D ×
     * saisonaler Modifikator, geglättet über {@link #PRICE_SMOOTHING_WINDOW_DAYS}
     * Minecraft-Tage). 1.0 = neutral, z.B. wenn Dynamic Pricing deaktiviert ist oder das Item
     * nicht registriert wurde. Wird nur 1x pro Minecraft-Tag neu berechnet (siehe
     * {@link #onDayChange}) — bei der allerersten Abfrage eines frisch registrierten Items wird
     * einmalig sofort mit dem aktuellen Rohwert geseedet, damit es nicht bis zum nächsten
     * Tageswechsel künstlich neutral bepreist bleibt.
     */
    public double getItemPriceMultiplier(Item item) {
        if (!ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.get()) return 1.0;
        MarketData data = itemMarketData.get(item);
        if (data == null) return 1.0;

        PriceSmoothingState smoothing = itemPriceSmoothing.computeIfAbsent(item, k -> new PriceSmoothingState());
        if (smoothing.history.isEmpty()) {
            smoothing.effectiveMultiplier = recordAndAverage(smoothing.history, computeRawItemMultiplier(item, data));
            markDirty();
        }
        return smoothing.effectiveMultiplier;
    }

    /**
     * Berechnet den rohen, unverzögerten Preismultiplikator für ein Item (S&D × Saison,
     * geclampt). Wird sowohl beim Seeden eines neuen Items als auch beim täglichen
     * Glättungs-Update ({@link #updatePriceSmoothingSnapshots()}) verwendet.
     */
    private double computeRawItemMultiplier(Item item, MarketData data) {
        double multiplier = data.getPriceMultiplier();

        String seasonalCategory = itemSeasonalCategory.get(item);
        if (seasonalCategory != null) {
            multiplier *= SeasonalPriceModifier.getInstance().getModifier(seasonalCategory);
        }

        // Lager-Füllstand (nur 1x/Tag per updateWarehouseData() in onDayChange() aktualisiert,
        // NICHT in Echtzeit) - hoher Füllstand = günstiger, niedriger Füllstand = teurer.
        multiplier *= de.rolandsw.schedulemc.economy.WarehouseMarketBridge.getInstance()
                .getWarehousePriceMultiplier(item.getDescriptionId());

        double minMult = ModConfigHandler.COMMON.DYNAMIC_PRICING_MIN_MULTIPLIER.get();
        double maxMult = ModConfigHandler.COMMON.DYNAMIC_PRICING_MAX_MULTIPLIER.get();
        return Math.max(minMult, Math.min(maxMult, multiplier));
    }

    /**
     * Holt aktuellen Marktpreis eines Items (0 wenn nicht registriert).
     */
    public double getCurrentItemPrice(Item item) {
        MarketData data = itemMarketData.get(item);
        return data != null ? data.getCurrentPrice() : 0.0;
    }

    @Nullable
    public MarketData getItemMarketData(Item item) {
        return itemMarketData.get(item);
    }

    public Collection<MarketData> getAllItemMarketData() {
        return new ArrayList<>(itemMarketData.values());
    }

    public List<MarketData> getTopPricedItems(int limit) {
        List<MarketData> all = new ArrayList<>(itemMarketData.values());
        all.sort((a, b) -> Double.compare(b.getCurrentPrice(), a.getCurrentPrice()));
        return all.subList(0, Math.min(limit, all.size()));
    }

    public List<MarketData> getTrendingUpItems(int limit) {
        List<MarketData> rising = new ArrayList<>();
        for (MarketData data : itemMarketData.values()) {
            if (data.getPriceTrend() == MarketData.PriceTrend.RISING) {
                rising.add(data);
            }
        }
        rising.sort((a, b) -> Double.compare(b.getPriceChangePercent(), a.getPriceChangePercent()));
        return rising.subList(0, Math.min(limit, rising.size()));
    }

    public List<MarketData> getTrendingDownItems(int limit) {
        List<MarketData> falling = new ArrayList<>();
        for (MarketData data : itemMarketData.values()) {
            if (data.getPriceTrend() == MarketData.PriceTrend.FALLING) {
                falling.add(data);
            }
        }
        falling.sort((a, b) -> Double.compare(a.getPriceChangePercent(), b.getPriceChangePercent()));
        return falling.subList(0, Math.min(limit, falling.size()));
    }

    /**
     * Führt Decay (Supply&Demand-Abkühlung) für alle Item-Markt-Einträge aus.
     * Läuft im gleichen Intervall wie updateMarketConditions() (DYNAMIC_PRICING_UPDATE_INTERVAL_MINUTES).
     */
    private void updateItemMarketData() {
        if (itemMarketData.isEmpty()) return;

        double decayRate = ModConfigHandler.COMMON.DYNAMIC_PRICING_SD_DECAY_RATE.get();
        for (MarketData data : itemMarketData.values()) {
            data.snapshotForTrend();
            data.decaySupply(decayRate);
            data.decayDemand(decayRate);
        }
        totalItemMarketUpdates++;
        markDirty();
    }

    // ═══════════════════════════════════════════════════════════
    // PRODUCT MARKET (Supply & Demand für abstrakte UDPS-Produkte, z.B. Anbau-/Drogen-Sorten)
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt den tatsächlich für Preise genutzten Multiplikator für ein Produkt zurück, geglättet
     * über {@link #PRICE_SMOOTHING_WINDOW_DAYS} Minecraft-Tage (1.0 = neutral, z.B. wenn Dynamic
     * Pricing deaktiviert ist oder das Produkt noch keine Verkäufe/Käufe hatte). Siehe
     * {@link #getItemPriceMultiplier(Item)} für dasselbe Prinzip auf Item-Ebene.
     */
    public double getProductPriceMultiplier(String productId) {
        if (!ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.get()) return 1.0;
        ProductMarketState state = productMarketData.get(productId);
        if (state == null) return 1.0;

        if (state.multiplierHistory.isEmpty()) {
            state.effectiveMultiplier = recordAndAverage(state.multiplierHistory, computeRawProductMultiplier(state));
            markDirty();
        }
        return state.effectiveMultiplier;
    }

    /**
     * Berechnet den rohen, unverzögerten S&D-Preismultiplikator für ein Produkt (geclampt).
     * Nutzt dieselbe Ratio^Faktor-Formel wie {@link MarketData#getPriceMultiplier()}.
     */
    private double computeRawProductMultiplier(ProductMarketState state) {
        double sdFactor = ModConfigHandler.COMMON.DYNAMIC_PRICING_SD_FACTOR.get();
        double minMult = ModConfigHandler.COMMON.DYNAMIC_PRICING_MIN_MULTIPLIER.get();
        double maxMult = ModConfigHandler.COMMON.DYNAMIC_PRICING_MAX_MULTIPLIER.get();

        double ratio = (double) state.demand / Math.max(1, state.supply);
        double multiplier = Math.pow(ratio, sdFactor);
        return Math.max(minMult, Math.min(maxMult, multiplier));
    }

    /**
     * Ein Produkt wurde verkauft (Spieler verkauft an NPC/Markt) - erhöht Supply.
     */
    public void onProductSold(String productId, int amount) {
        if (!ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.get() || amount <= 0) return;
        productMarketData.computeIfAbsent(productId, k -> new ProductMarketState()).supply += amount;
        markDirty();
    }

    /**
     * Ein Produkt wurde gekauft (Spieler kauft, z.B. Saatgut/Rohstoffe) - erhöht Demand.
     */
    public void onProductBought(String productId, int amount) {
        if (!ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.get() || amount <= 0) return;
        productMarketData.computeIfAbsent(productId, k -> new ProductMarketState()).demand += amount;
        markDirty();
    }

    /**
     * Führt Decay für alle Produkt-Markt-Einträge aus, im selben Intervall wie
     * {@link #updateItemMarketData()}.
     */
    private void updateProductMarketData() {
        if (productMarketData.isEmpty()) return;

        double decayRate = ModConfigHandler.COMMON.DYNAMIC_PRICING_SD_DECAY_RATE.get();
        for (ProductMarketState state : productMarketData.values()) {
            state.supply = Math.max(1, (int) (state.supply * (1.0 - decayRate)));
            state.demand = Math.max(1, (int) (state.demand * (1.0 - decayRate)));
        }
        markDirty();
    }

    /** Supply&Demand-Zustand eines einzelnen UDPS-Produkts. */
    public static class ProductMarketState {
        public int supply = 100;
        public int demand = 100;
        /** Letzte bis zu {@link #PRICE_SMOOTHING_WINDOW_DAYS} täglichen Roh-Multiplikatoren. */
        public List<Double> multiplierHistory = new ArrayList<>();
        /** Zuletzt berechneter, geglätteter (7-Tage-Ø) Multiplikator — das ist der genutzte Preis. */
        public double effectiveMultiplier = 1.0;
    }

    /** Geglätteter Preiszustand eines einzelnen Items (Gegenstück zu {@link ProductMarketState}). */
    public static class PriceSmoothingState {
        public final List<Double> history = new ArrayList<>();
        public double effectiveMultiplier = 1.0;
    }

    /**
     * Rollt {@code rawValue} in {@code history} ein (FIFO, max. {@link #PRICE_SMOOTHING_WINDOW_DAYS}
     * Einträge) und gibt den neuen arithmetischen Durchschnitt zurück. Das ist der "gleitende
     * 7-Minecraft-Tage-Durchschnitt", der Preisbewegung spürbar, aber gedämpft macht.
     */
    private static double recordAndAverage(List<Double> history, double rawValue) {
        history.add(rawValue);
        while (history.size() > PRICE_SMOOTHING_WINDOW_DAYS) {
            history.remove(0);
        }
        double sum = 0;
        for (double v : history) {
            sum += v;
        }
        return sum / history.size();
    }

    /**
     * Läuft 1x pro Minecraft-Tag (aus {@link #onDayChange}): berechnet für jedes registrierte
     * Item/Produkt den aktuellen Rohmultiplikator und rollt ihn in den 7-Tage-Gleitdurchschnitt
     * ein. Der dadurch aktualisierte {@code effectiveMultiplier} ist das, was
     * {@link #getItemPriceMultiplier(Item)}/{@link #getProductPriceMultiplier(String)}
     * tatsächlich zurückgeben — Käufe/Verkäufe ändern weiterhin sofort Supply/Demand, wirken
     * sich auf den genutzten Preis aber erst beim nächsten Tageswechsel aus, und dann nur
     * anteilig (1 von {@link #PRICE_SMOOTHING_WINDOW_DAYS} Tageswerten).
     */
    private void updatePriceSmoothingSnapshots() {
        for (Map.Entry<Item, MarketData> entry : itemMarketData.entrySet()) {
            PriceSmoothingState smoothing = itemPriceSmoothing.computeIfAbsent(entry.getKey(), k -> new PriceSmoothingState());
            smoothing.effectiveMultiplier = recordAndAverage(smoothing.history,
                    computeRawItemMultiplier(entry.getKey(), entry.getValue()));
        }
        for (ProductMarketState state : productMarketData.values()) {
            state.effectiveMultiplier = recordAndAverage(state.multiplierHistory, computeRawProductMultiplier(state));
        }
    }

    public MarketStatistics getItemMarketStatistics() {
        int totalItems = itemMarketData.size();
        int risingCount = 0;
        int fallingCount = 0;
        int stableCount = 0;
        double avgPrice = 0;
        double avgMultiplier = 0;

        for (MarketData data : itemMarketData.values()) {
            switch (data.getPriceTrend()) {
                case RISING -> risingCount++;
                case FALLING -> fallingCount++;
                case STABLE -> stableCount++;
            }
            avgPrice += data.getCurrentPrice();
            avgMultiplier += data.getPriceMultiplier();
        }

        if (totalItems > 0) {
            avgPrice /= totalItems;
            avgMultiplier /= totalItems;
        }

        return new MarketStatistics(totalItems, risingCount, fallingCount, stableCount,
            avgPrice, avgMultiplier, totalItemMarketUpdates);
    }

    /**
     * Erstellt einen spieler-sichtbaren Marktbericht als Chat-Nachricht (Top 5 steigend/fallend).
     */
    public String getPlayerItemMarketReport() {
        if (itemMarketData.isEmpty()) {
            return "§7No market data available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§6§l=== Market Overview ===§r\n");

        List<MarketData> rising = getTrendingUpItems(5);
        if (!rising.isEmpty()) {
            sb.append("\n§a↑ Rising prices:\n");
            for (MarketData data : rising) {
                sb.append(String.format("  §f%s: §a+%.1f%% §7(%.2f€)\n",
                    data.getItemName(), data.getPriceChangePercent(), data.getCurrentPrice()));
            }
        }

        List<MarketData> falling = getTrendingDownItems(5);
        if (!falling.isEmpty()) {
            sb.append("\n§c↓ Falling prices:\n");
            for (MarketData data : falling) {
                sb.append(String.format("  §f%s: §c%.1f%% §7(%.2f€)\n",
                    data.getItemName(), data.getPriceChangePercent(), data.getCurrentPrice()));
            }
        }

        MarketStatistics stats = getItemMarketStatistics();
        sb.append(String.format("\n§7Items: %d | §a↑%d §c↓%d §e↔%d",
            stats.totalItems(), stats.risingCount(), stats.fallingCount(), stats.stableCount()));

        return sb.toString();
    }

    /**
     * Item-Markt-Statistiken (ehemals DynamicMarketManager.MarketStatistics).
     */
    public record MarketStatistics(
        int totalItems,
        int risingCount,
        int fallingCount,
        int stableCount,
        double averagePrice,
        double averageMultiplier,
        long totalUpdates
    ) {
        @Override
        public String toString() {
            return String.format(
                "MarketStats{items=%d, rising=%d, falling=%d, stable=%d, avgPrice=%.2f, avgMult=%.2fx, updates=%d}",
                totalItems, risingCount, fallingCount, stableCount, averagePrice, averageMultiplier, totalUpdates
            );
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird bei einem Marktkrise-Event aufgerufen
     */
    public void triggerMarketCrisis(String category, int durationDays) {
        if (category == null || category.isEmpty()) {
            globalCondition = MarketCondition.CRISIS;
        } else {
            categoryConditions.put(category, MarketCondition.CRISIS);
        }

        addTemporaryModifier(
            "crisis_" + (category != null ? category : "global"),
            1.5f,
            durationDays * 24000,
            "Marktkrise"
        );
    }

    /**
     * Wird bei einem Überfluss-Event aufgerufen
     */
    public void triggerSurplus(String category, int durationDays) {
        categoryConditions.put(category, MarketCondition.SURPLUS);

        addTemporaryModifier(
            "surplus_" + category,
            0.6f,
            durationDays * 24000,
            "Goods surplus: " + category
        );
    }

    /**
     * Wird bei einer Knappheit aufgerufen
     */
    public void triggerShortage(String category, int durationDays) {
        categoryConditions.put(category, MarketCondition.SHORTAGE);

        addTemporaryModifier(
            "shortage_" + category,
            1.8f,
            durationDays * 24000,
            "Warenknappheit: " + category
        );
    }

    // ═══════════════════════════════════════════════════════════
    // HISTORY
    // ═══════════════════════════════════════════════════════════

    private void savePriceSnapshot(long day) {
        if (priceHistory.size() >= MAX_HISTORY) {
            priceHistory.removeLast();
        }
        priceHistory.addFirst(new PriceSnapshot(day, globalCondition, getCombinedTemporaryModifier()));
    }

    public List<PriceSnapshot> getPriceHistory() {
        return new ArrayList<>(priceHistory);
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<DynamicPriceManagerData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(DynamicPriceManagerData data) {
        int invalidCount = 0;
        int correctedCount = 0;

        // VALIDATE GLOBAL CONDITION
        if (data.globalCondition != null) {
            try {
                globalCondition = MarketCondition.valueOf(data.globalCondition);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid global market condition {}, resetting to NORMAL", data.globalCondition);
                globalCondition = MarketCondition.NORMAL;
                correctedCount++;
            }
        } else {
            globalCondition = MarketCondition.NORMAL;
        }

        // VALIDATE LAST KNOWN DAY
        if (data.lastKnownDay < 0) {
            LOGGER.warn("Invalid lastKnownDay {}, resetting to 0", data.lastKnownDay);
            lastKnownDay = 0;
            correctedCount++;
        } else {
            lastKnownDay = data.lastKnownDay;
        }

        // Validate and load categoryConditions
        categoryConditions.clear();
        if (data.categoryConditions != null) {
            // Check collection size
            if (data.categoryConditions.size() > 1000) {
                LOGGER.warn("Category conditions map size ({}) exceeds limit, potential corruption",
                    data.categoryConditions.size());
                correctedCount++;
            }

            for (Map.Entry<String, MarketCondition> entry : data.categoryConditions.entrySet()) {
                try {
                    String category = entry.getKey();
                    MarketCondition condition = entry.getValue();

                    // NULL CHECK
                    if (category == null || category.isEmpty()) {
                        LOGGER.warn("Null/empty category key, skipping");
                        invalidCount++;
                        continue;
                    }
                    if (condition == null) {
                        LOGGER.warn("Null market condition for category {}, skipping", category);
                        invalidCount++;
                        continue;
                    }

                    // VALIDATE CATEGORY LENGTH
                    if (category.length() > 100) {
                        LOGGER.warn("Category name too long ({} chars), skipping", category.length());
                        invalidCount++;
                        continue;
                    }

                    categoryConditions.put(category, condition);
                } catch (Exception e) {
                    LOGGER.error("Error loading category condition for {}", entry.getKey(), e);
                    invalidCount++;
                }
            }
        }

        // Validate and load temporaryModifiers
        temporaryModifiers.clear();
        if (data.temporaryModifiers != null) {
            // Check collection size
            if (data.temporaryModifiers.size() > 1000) {
                LOGGER.warn("Temporary modifiers map size ({}) exceeds limit, potential corruption",
                    data.temporaryModifiers.size());
                correctedCount++;
            }

            for (Map.Entry<String, TemporaryModifier> entry : data.temporaryModifiers.entrySet()) {
                try {
                    String key = entry.getKey();
                    TemporaryModifier modifier = entry.getValue();

                    // NULL CHECK
                    if (key == null || key.isEmpty()) {
                        LOGGER.warn("Null/empty modifier key, skipping");
                        invalidCount++;
                        continue;
                    }
                    if (modifier == null) {
                        LOGGER.warn("Null price modifier for key {}, skipping", key);
                        invalidCount++;
                        continue;
                    }

                    // VALIDATE KEY LENGTH
                    if (key.length() > 100) {
                        LOGGER.warn("Modifier key too long ({} chars), skipping", key.length());
                        invalidCount++;
                        continue;
                    }

                    temporaryModifiers.put(key, modifier);
                } catch (Exception e) {
                    LOGGER.error("Error loading temporary modifier for {}", entry.getKey(), e);
                    invalidCount++;
                }
            }
        }

        // Validate and load priceHistory
        priceHistory.clear();
        if (data.priceHistory != null) {
            // Check collection size
            if (data.priceHistory.size() > MAX_HISTORY * 2) {
                LOGGER.warn("Price history size ({}) exceeds limit, potential corruption",
                    data.priceHistory.size());
                correctedCount++;
            }

            for (PriceSnapshot snapshot : data.priceHistory) {
                try {
                    // NULL CHECK
                    if (snapshot == null) {
                        LOGGER.warn("Null price snapshot in history, skipping");
                        invalidCount++;
                        continue;
                    }

                    priceHistory.add(snapshot);
                } catch (Exception e) {
                    LOGGER.error("Error loading price snapshot", e);
                    invalidCount++;
                }
            }
        }

        // Validate and load item market data (Supply&Demand, ehemals DynamicMarketManager)
        itemMarketData.clear();
        if (data.itemMarketData != null) {
            if (data.itemMarketData.size() > 10000) {
                LOGGER.warn("Item market data size ({}) exceeds limit, potential corruption",
                    data.itemMarketData.size());
                correctedCount++;
            }

            double sdFactor = ModConfigHandler.COMMON.DYNAMIC_PRICING_SD_FACTOR.get();
            double minMult = ModConfigHandler.COMMON.DYNAMIC_PRICING_MIN_MULTIPLIER.get();
            double maxMult = ModConfigHandler.COMMON.DYNAMIC_PRICING_MAX_MULTIPLIER.get();

            for (SerializedItemMarketData serialized : data.itemMarketData) {
                try {
                    if (serialized == null || serialized.itemId == null) {
                        invalidCount++;
                        continue;
                    }
                    ResourceLocation itemId = ResourceLocation.parse(serialized.itemId);
                    Item item = BuiltInRegistries.ITEM.get(itemId);
                    if (item == null || item == net.minecraft.world.item.Items.AIR) {
                        LOGGER.warn("Could not deserialize market item {} - not found in registry", serialized.itemId);
                        invalidCount++;
                        continue;
                    }

                    MarketData marketData = new MarketData(
                        item, serialized.basePrice, sdFactor, minMult, maxMult,
                        serialized.supply, serialized.demand, serialized.currentPrice,
                        serialized.previousPrice, serialized.previousSupply, serialized.previousDemand
                    );
                    itemMarketData.put(item, marketData);
                } catch (Exception e) {
                    LOGGER.error("Error loading item market data", e);
                    invalidCount++;
                }
            }
        }

        // Validate and load item price smoothing (7-Minecraft-Tage-Gleitdurchschnitt)
        itemPriceSmoothing.clear();
        if (data.itemPriceSmoothing != null) {
            if (data.itemPriceSmoothing.size() > 10000) {
                LOGGER.warn("Item price smoothing size ({}) exceeds limit, potential corruption",
                    data.itemPriceSmoothing.size());
                correctedCount++;
            }

            for (SerializedItemSmoothing serialized : data.itemPriceSmoothing) {
                try {
                    if (serialized == null || serialized.itemId == null) {
                        invalidCount++;
                        continue;
                    }
                    ResourceLocation itemId = ResourceLocation.parse(serialized.itemId);
                    Item item = BuiltInRegistries.ITEM.get(itemId);
                    if (item == null || item == net.minecraft.world.item.Items.AIR) {
                        invalidCount++;
                        continue;
                    }

                    PriceSmoothingState smoothing = new PriceSmoothingState();
                    if (serialized.history != null) {
                        for (Double v : serialized.history) {
                            if (v != null) smoothing.history.add(v);
                        }
                        while (smoothing.history.size() > PRICE_SMOOTHING_WINDOW_DAYS) {
                            smoothing.history.remove(0);
                        }
                    }
                    smoothing.effectiveMultiplier = smoothing.history.isEmpty() ? 1.0 : serialized.effectiveMultiplier;
                    itemPriceSmoothing.put(item, smoothing);
                } catch (Exception e) {
                    LOGGER.error("Error loading item price smoothing", e);
                    invalidCount++;
                }
            }
        }

        // Validate and load product market data (Supply&Demand, ehemals EconomyController.marketDataMap)
        productMarketData.clear();
        if (data.productMarketData != null) {
            if (data.productMarketData.size() > 10000) {
                LOGGER.warn("Product market data size ({}) exceeds limit, potential corruption",
                    data.productMarketData.size());
                correctedCount++;
            }

            for (Map.Entry<String, ProductMarketState> entry : data.productMarketData.entrySet()) {
                try {
                    String productId = entry.getKey();
                    ProductMarketState state = entry.getValue();
                    if (productId == null || productId.isEmpty() || state == null) {
                        invalidCount++;
                        continue;
                    }
                    ProductMarketState clean = new ProductMarketState();
                    clean.supply = Math.max(1, state.supply);
                    clean.demand = Math.max(1, state.demand);
                    if (state.multiplierHistory != null) {
                        for (Double v : state.multiplierHistory) {
                            if (v != null) clean.multiplierHistory.add(v);
                        }
                        while (clean.multiplierHistory.size() > PRICE_SMOOTHING_WINDOW_DAYS) {
                            clean.multiplierHistory.remove(0);
                        }
                    }
                    clean.effectiveMultiplier = clean.multiplierHistory.isEmpty() ? 1.0 : state.effectiveMultiplier;
                    productMarketData.put(productId, clean);
                } catch (Exception e) {
                    LOGGER.error("Error loading product market data for {}", entry.getKey(), e);
                    invalidCount++;
                }
            }
        }

        // SUMMARY
        if (invalidCount > 0 || correctedCount > 0) {
            LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                invalidCount, correctedCount);
            if (correctedCount > 0) {
                markDirty(); // Re-save corrected data
            }
        }
    }

    @Override
    protected DynamicPriceManagerData getCurrentData() {
        DynamicPriceManagerData data = new DynamicPriceManagerData();
        data.globalCondition = globalCondition.name();
        data.lastKnownDay = lastKnownDay;
        data.categoryConditions = new HashMap<>(categoryConditions);
        data.temporaryModifiers = new HashMap<>(temporaryModifiers);
        data.priceHistory = new ArrayList<>(priceHistory);

        data.itemMarketData = new ArrayList<>();
        for (Map.Entry<Item, MarketData> entry : itemMarketData.entrySet()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(entry.getKey());
            if (itemId == null) continue;
            MarketData md = entry.getValue();
            data.itemMarketData.add(new SerializedItemMarketData(
                itemId.toString(), md.getBasePrice(), md.getSupply(), md.getDemand(),
                md.getCurrentPrice(), md.getPreviousPrice(), md.getPreviousSupply(), md.getPreviousDemand()
            ));
        }

        data.productMarketData = new HashMap<>(productMarketData);

        data.itemPriceSmoothing = new ArrayList<>();
        for (Map.Entry<Item, PriceSmoothingState> entry : itemPriceSmoothing.entrySet()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(entry.getKey());
            if (itemId == null) continue;
            PriceSmoothingState smoothing = entry.getValue();
            data.itemPriceSmoothing.add(new SerializedItemSmoothing(
                itemId.toString(), new ArrayList<>(smoothing.history), smoothing.effectiveMultiplier
            ));
        }
        return data;
    }

    @Override
    protected String getComponentName() {
        return "DynamicPriceManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%s market, %d categories, %d modifiers",
            globalCondition.name(), categoryConditions.size(), temporaryModifiers.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        globalCondition = MarketCondition.NORMAL;
        categoryConditions.clear();
        temporaryModifiers.clear();
        priceHistory.clear();
        itemMarketData.clear();
        productMarketData.clear();
        itemPriceSmoothing.clear();
        lastKnownDay = -1;
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASSES FOR JSON SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public static class DynamicPriceManagerData {
        public String globalCondition;
        public long lastKnownDay;
        public Map<String, MarketCondition> categoryConditions;
        public Map<String, TemporaryModifier> temporaryModifiers;
        public List<PriceSnapshot> priceHistory;
        public List<SerializedItemMarketData> itemMarketData;
        public Map<String, ProductMarketState> productMarketData;
        public List<SerializedItemSmoothing> itemPriceSmoothing;
    }

    /**
     * Serialisierbarer Preisglättungs-Zustand eines Items (Item als ResourceLocation-String).
     */
    public static class SerializedItemSmoothing {
        public String itemId;
        public List<Double> history;
        public double effectiveMultiplier;

        public SerializedItemSmoothing() {}

        public SerializedItemSmoothing(String itemId, List<Double> history, double effectiveMultiplier) {
            this.itemId = itemId;
            this.history = history;
            this.effectiveMultiplier = effectiveMultiplier;
        }
    }

    /**
     * Serialisierbare Item-Markt-Daten (Item wird als ResourceLocation-String gespeichert).
     */
    public static class SerializedItemMarketData {
        public String itemId;
        public double basePrice;
        public int supply;
        public int demand;
        public double currentPrice;
        public double previousPrice;
        public int previousSupply;
        public int previousDemand;

        public SerializedItemMarketData() {}

        public SerializedItemMarketData(String itemId, double basePrice, int supply, int demand,
                                         double currentPrice, double previousPrice,
                                         int previousSupply, int previousDemand) {
            this.itemId = itemId;
            this.basePrice = basePrice;
            this.supply = supply;
            this.demand = demand;
            this.currentPrice = currentPrice;
            this.previousPrice = previousPrice;
            this.previousSupply = previousSupply;
            this.previousDemand = previousDemand;
        }
    }

    public static class TemporaryModifier {
        public final float modifier;
        public int ticksRemaining;
        public final String reason;

        public TemporaryModifier(float modifier, int ticksRemaining, String reason) {
            this.modifier = modifier;
            this.ticksRemaining = ticksRemaining;
            this.reason = reason;
        }
    }

    public static class PriceSnapshot {
        public final long day;
        public final MarketCondition condition;
        public final float modifier;

        public PriceSnapshot(long day, MarketCondition condition, float modifier) {
            this.day = day;
            this.condition = condition;
            this.modifier = modifier;
        }
    }

    @Override
    public String toString() {
        return String.format("DynamicPriceManager{global=%s, categories=%d, tempMods=%d}",
            globalCondition.name(), categoryConditions.size(), temporaryModifiers.size());
    }

    public String getMarketReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Marktbericht ===\n");
        sb.append(String.format("Global market: %s (×%.2f)\n",
            globalCondition.getDisplayName(), globalCondition.getPriceMultiplier()));

        if (!categoryConditions.isEmpty()) {
            sb.append("\nKategorien:\n");
            for (Map.Entry<String, MarketCondition> entry : categoryConditions.entrySet()) {
                sb.append(String.format("  %s: %s (×%.2f)\n",
                    entry.getKey(), entry.getValue().getDisplayName(),
                    entry.getValue().getPriceMultiplier()));
            }
        }

        if (!temporaryModifiers.isEmpty()) {
            sb.append("\nAktive Events:\n");
            for (Map.Entry<String, TemporaryModifier> entry : temporaryModifiers.entrySet()) {
                sb.append(String.format("  %s: ×%.2f (%s)\n",
                    entry.getValue().reason, entry.getValue().modifier,
                    formatTicksRemaining(entry.getValue().ticksRemaining)));
            }
        }

        return sb.toString();
    }

    private String formatTicksRemaining(int ticks) {
        int days = ticks / 24000;
        if (days > 0) return days + " Tage";
        int hours = ticks / 1000;
        return hours + " Stunden";
    }
}
