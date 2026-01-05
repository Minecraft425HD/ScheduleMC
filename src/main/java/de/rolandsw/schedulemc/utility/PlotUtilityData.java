package de.rolandsw.schedulemc.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.EnumMap;

/**
 * Speichert Verbrauchsdaten für einen einzelnen Plot
 *
 * OPTIMIERT: Verwendet Circular Buffer statt Array-Shifting für O(1) Operationen
 */
public class PlotUtilityData {

    private static final int HISTORY_SIZE = 7;

    private final String plotId;

    // Alle Verbraucher-Blöcke in diesem Plot: Position -> Block-ID
    private final Map<BlockPos, String> consumers = new HashMap<>();

    // Aktiver Status jedes Blocks: Position -> isActive
    private final Map<BlockPos, Boolean> activeStatus = new HashMap<>();

    // OPTIMIERT: Circular Buffer statt Array-Shifting
    // Index 0..6 für Tage, historyIndex zeigt auf "heute"
    private final double[] dailyElectricity = new double[HISTORY_SIZE];
    private final double[] dailyWater = new double[HISTORY_SIZE];
    private int historyIndex = 0;  // Zeigt auf den aktuellen Tag

    // Aktueller Tag-Verbrauch (wird am Ende des Tages in Historie geschoben)
    private double currentDayElectricity = 0;
    private double currentDayWater = 0;

    // Letzter Minecraft-Tag an dem ein Update stattfand
    private long lastUpdateDay = -1;

    // Kategorie-Aufschlüsselung für aktuellen Tag
    private final Map<UtilityCategory, Double> categoryElectricity = new EnumMap<>(UtilityCategory.class);
    private final Map<UtilityCategory, Double> categoryWater = new EnumMap<>(UtilityCategory.class);

    public PlotUtilityData(String plotId) {
        this.plotId = plotId;
        Arrays.fill(dailyElectricity, 0);
        Arrays.fill(dailyWater, 0);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BLOCK MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Fügt einen Verbraucher-Block hinzu
     * SICHERHEIT: Null-Check für Block-Registry-Lookup
     */
    public void addConsumer(BlockPos pos, Block block) {
        ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        if (key == null) {
            // Block nicht in Registry - verwende Klassennamen als Fallback
            key = ResourceLocation.fromNamespaceAndPath("unknown", block.getClass().getSimpleName().toLowerCase());
        }
        consumers.put(pos, key.toString());
        activeStatus.put(pos, false); // Startet als idle
    }

    /**
     * Entfernt einen Verbraucher-Block
     */
    public void removeConsumer(BlockPos pos) {
        consumers.remove(pos);
        activeStatus.remove(pos);
    }

    /**
     * Setzt den Aktivitätsstatus eines Blocks
     */
    public void setActiveStatus(BlockPos pos, boolean isActive) {
        if (consumers.containsKey(pos)) {
            activeStatus.put(pos, isActive);
        }
    }

    /**
     * Gibt zurück ob ein Block aktiv ist
     */
    public boolean isActive(BlockPos pos) {
        return activeStatus.getOrDefault(pos, false);
    }

    /**
     * Gibt die Anzahl der Verbraucher zurück
     */
    public int getConsumerCount() {
        return consumers.size();
    }

    /**
     * Prüft ob eine Position ein registrierter Verbraucher ist
     */
    public boolean hasConsumer(BlockPos pos) {
        return consumers.containsKey(pos);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VERBRAUCHSBERECHNUNG
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Berechnet den aktuellen Gesamtverbrauch
     * Wird jede Minecraft-Stunde aufgerufen (1000 Ticks)
     */
    public void calculateCurrentConsumption() {
        currentDayElectricity = 0;
        currentDayWater = 0;
        categoryElectricity.clear();
        categoryWater.clear();

        for (Map.Entry<BlockPos, String> entry : consumers.entrySet()) {
            BlockPos pos = entry.getKey();
            String blockId = entry.getValue();
            boolean isActive = activeStatus.getOrDefault(pos, false);

            Optional<UtilityConsumptionData> dataOpt = UtilityRegistry.getConsumptionById(blockId);
            if (dataOpt.isPresent()) {
                UtilityConsumptionData data = dataOpt.get();
                double elec = data.getCurrentElectricity(isActive);
                double water = data.getCurrentWater(isActive);

                currentDayElectricity += elec;
                currentDayWater += water;

                // Kategorie-Tracking
                categoryElectricity.merge(data.category(), elec, Double::sum);
                categoryWater.merge(data.category(), water, Double::sum);
            }
        }
    }

    /**
     * Wird am Ende eines Minecraft-Tages aufgerufen
     * Schiebt den aktuellen Verbrauch in die Historie
     *
     * OPTIMIERT: O(1) Circular Buffer statt O(n) Array-Shifting
     */
    public void rolloverDay(long currentDay) {
        if (lastUpdateDay < 0) {
            lastUpdateDay = currentDay;
            return;
        }

        // Tage seit letztem Update
        long daysPassed = currentDay - lastUpdateDay;
        if (daysPassed <= 0) return;

        // Berechne finale Werte für den Tag
        calculateCurrentConsumption();

        // OPTIMIERT: Circular Buffer - kein Shifting nötig!
        // Bewege Index vorwärts und überschreibe ältesten Eintrag
        int daysToProcess = (int) Math.min(daysPassed, HISTORY_SIZE);

        for (int d = 0; d < daysToProcess; d++) {
            // Bewege Index zum nächsten Slot (rückwärts im Ring = vorwärts in Zeit)
            historyIndex = (historyIndex + HISTORY_SIZE - 1) % HISTORY_SIZE;

            // Schreibe aktuellen Verbrauch in neuen "heute" Slot
            dailyElectricity[historyIndex] = currentDayElectricity;
            dailyWater[historyIndex] = currentDayWater;
        }

        lastUpdateDay = currentDay;
    }

    /**
     * Hilfsmethode: Holt Historie-Wert relativ zu heute
     * OPTIMIERT: O(1) Zugriff auf beliebigen Tag
     */
    private double getHistoryElectricity(int daysAgo) {
        if (daysAgo < 0 || daysAgo >= HISTORY_SIZE) return 0;
        int idx = (historyIndex + daysAgo) % HISTORY_SIZE;
        return dailyElectricity[idx];
    }

    private double getHistoryWater(int daysAgo) {
        if (daysAgo < 0 || daysAgo >= HISTORY_SIZE) return 0;
        int idx = (historyIndex + daysAgo) % HISTORY_SIZE;
        return dailyWater[idx];
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GETTER
    // ═══════════════════════════════════════════════════════════════════════════

    public String getPlotId() {
        return plotId;
    }

    public double getCurrentElectricity() {
        return currentDayElectricity;
    }

    public double getCurrentWater() {
        return currentDayWater;
    }

    /**
     * Berechnet den 7-Tage-Durchschnitt für Strom
     * OPTIMIERT: Verwendet Circular Buffer Hilfsmethoden
     */
    public double get7DayAverageElectricity() {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < HISTORY_SIZE; i++) {
            double val = getHistoryElectricity(i);
            if (val > 0) {
                sum += val;
                count++;
            }
        }
        return count > 0 ? sum / count : currentDayElectricity;
    }

    /**
     * Berechnet den 7-Tage-Durchschnitt für Wasser
     * OPTIMIERT: Verwendet Circular Buffer Hilfsmethoden
     */
    public double get7DayAverageWater() {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < HISTORY_SIZE; i++) {
            double val = getHistoryWater(i);
            if (val > 0) {
                sum += val;
                count++;
            }
        }
        return count > 0 ? sum / count : currentDayWater;
    }

    /**
     * Gibt die Kategorie-Aufschlüsselung für Strom zurück
     */
    public Map<UtilityCategory, Double> getCategoryElectricity() {
        return new EnumMap<>(categoryElectricity);
    }

    /**
     * Gibt die Kategorie-Aufschlüsselung für Wasser zurück
     */
    public Map<UtilityCategory, Double> getCategoryWater() {
        return new EnumMap<>(categoryWater);
    }

    /**
     * Gibt die tägliche Historie zurück (Index 0 = heute)
     * OPTIMIERT: Konvertiert Circular Buffer in chronologische Reihenfolge
     */
    public double[] getDailyElectricity() {
        double[] result = new double[HISTORY_SIZE];
        for (int i = 0; i < HISTORY_SIZE; i++) {
            result[i] = getHistoryElectricity(i);
        }
        return result;
    }

    public double[] getDailyWater() {
        double[] result = new double[HISTORY_SIZE];
        for (int i = 0; i < HISTORY_SIZE; i++) {
            result[i] = getHistoryWater(i);
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SERIALISIERUNG
    // ═══════════════════════════════════════════════════════════════════════════

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("plotId", plotId);
        json.addProperty("lastUpdateDay", lastUpdateDay);
        json.addProperty("currentDayElectricity", currentDayElectricity);
        json.addProperty("currentDayWater", currentDayWater);
        json.addProperty("historyIndex", historyIndex);  // OPTIMIERT: Circular Buffer Index

        // Historie - speichere in chronologischer Reihenfolge für Kompatibilität
        JsonArray elecHistory = new JsonArray();
        JsonArray waterHistory = new JsonArray();
        for (int i = 0; i < HISTORY_SIZE; i++) {
            elecHistory.add(getHistoryElectricity(i));
            waterHistory.add(getHistoryWater(i));
        }
        json.add("electricityHistory", elecHistory);
        json.add("waterHistory", waterHistory);

        // Verbraucher
        JsonArray consumersArray = new JsonArray();
        for (Map.Entry<BlockPos, String> entry : consumers.entrySet()) {
            JsonObject consumer = new JsonObject();
            consumer.addProperty("x", entry.getKey().getX());
            consumer.addProperty("y", entry.getKey().getY());
            consumer.addProperty("z", entry.getKey().getZ());
            consumer.addProperty("blockId", entry.getValue());
            consumer.addProperty("active", activeStatus.getOrDefault(entry.getKey(), false));
            consumersArray.add(consumer);
        }
        json.add("consumers", consumersArray);

        return json;
    }

    public static PlotUtilityData fromJson(JsonObject json) {
        String plotId = json.get("plotId").getAsString();
        PlotUtilityData data = new PlotUtilityData(plotId);

        data.lastUpdateDay = json.get("lastUpdateDay").getAsLong();
        data.currentDayElectricity = json.get("currentDayElectricity").getAsDouble();
        data.currentDayWater = json.get("currentDayWater").getAsDouble();

        // OPTIMIERT: Lade Circular Buffer Index (mit Fallback für alte Daten)
        if (json.has("historyIndex")) {
            data.historyIndex = json.get("historyIndex").getAsInt();
        }

        // Historie - Daten sind in chronologischer Reihenfolge gespeichert
        JsonArray elecHistory = json.getAsJsonArray("electricityHistory");
        JsonArray waterHistory = json.getAsJsonArray("waterHistory");
        for (int i = 0; i < HISTORY_SIZE && i < elecHistory.size(); i++) {
            // Speichere direkt im Array - historyIndex zeigt auf "heute"
            int idx = (data.historyIndex + i) % HISTORY_SIZE;
            data.dailyElectricity[idx] = elecHistory.get(i).getAsDouble();
            data.dailyWater[idx] = waterHistory.get(i).getAsDouble();
        }

        // Verbraucher
        JsonArray consumersArray = json.getAsJsonArray("consumers");
        for (int i = 0; i < consumersArray.size(); i++) {
            JsonObject consumer = consumersArray.get(i).getAsJsonObject();
            BlockPos pos = new BlockPos(
                    consumer.get("x").getAsInt(),
                    consumer.get("y").getAsInt(),
                    consumer.get("z").getAsInt()
            );
            data.consumers.put(pos, consumer.get("blockId").getAsString());
            data.activeStatus.put(pos, consumer.get("active").getAsBoolean());
        }

        return data;
    }

    @Override
    public String toString() {
        return String.format("PlotUtilityData[%s: %d consumers, %.1f kWh, %.1f L (7d avg: %.1f kWh, %.1f L)]",
                plotId, consumers.size(), currentDayElectricity, currentDayWater,
                get7DayAverageElectricity(), get7DayAverageWater());
    }
}
