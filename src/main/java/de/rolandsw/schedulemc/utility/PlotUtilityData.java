package de.rolandsw.schedulemc.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.*;

/**
 * Speichert Verbrauchsdaten für einen einzelnen Plot
 */
public class PlotUtilityData {

    private final String plotId;

    // Alle Verbraucher-Blöcke in diesem Plot: Position -> Block-ID
    private final Map<BlockPos, String> consumers = new HashMap<>();

    // Aktiver Status jedes Blocks: Position -> isActive
    private final Map<BlockPos, Boolean> activeStatus = new HashMap<>();

    // 7-Tage-Historie (Index 0 = heute, 6 = vor 6 Tagen)
    private final double[] dailyElectricity = new double[7];
    private final double[] dailyWater = new double[7];

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
     */
    public void addConsumer(BlockPos pos, Block block) {
        String blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block).toString();
        consumers.put(pos, blockId);
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

        // Schiebe Historie nach hinten
        for (int i = 6; i >= 1; i--) {
            dailyElectricity[i] = dailyElectricity[i - 1];
            dailyWater[i] = dailyWater[i - 1];
        }

        // Aktuellen Tag an Position 0
        dailyElectricity[0] = currentDayElectricity;
        dailyWater[0] = currentDayWater;

        // Falls mehrere Tage vergangen sind, fülle mit Schätzwerten
        for (int d = 1; d < daysPassed && d < 7; d++) {
            for (int i = 6; i >= 1; i--) {
                dailyElectricity[i] = dailyElectricity[i - 1];
                dailyWater[i] = dailyWater[i - 1];
            }
            dailyElectricity[0] = currentDayElectricity; // Schätzung basierend auf aktuellem Setup
            dailyWater[0] = currentDayWater;
        }

        lastUpdateDay = currentDay;
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
     */
    public double get7DayAverageElectricity() {
        double sum = 0;
        int count = 0;
        for (double val : dailyElectricity) {
            if (val > 0) {
                sum += val;
                count++;
            }
        }
        return count > 0 ? sum / count : currentDayElectricity;
    }

    /**
     * Berechnet den 7-Tage-Durchschnitt für Wasser
     */
    public double get7DayAverageWater() {
        double sum = 0;
        int count = 0;
        for (double val : dailyWater) {
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
     */
    public double[] getDailyElectricity() {
        return Arrays.copyOf(dailyElectricity, 7);
    }

    public double[] getDailyWater() {
        return Arrays.copyOf(dailyWater, 7);
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

        // Historie
        JsonArray elecHistory = new JsonArray();
        JsonArray waterHistory = new JsonArray();
        for (int i = 0; i < 7; i++) {
            elecHistory.add(dailyElectricity[i]);
            waterHistory.add(dailyWater[i]);
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

        // Historie
        JsonArray elecHistory = json.getAsJsonArray("electricityHistory");
        JsonArray waterHistory = json.getAsJsonArray("waterHistory");
        for (int i = 0; i < 7 && i < elecHistory.size(); i++) {
            data.dailyElectricity[i] = elecHistory.get(i).getAsDouble();
            data.dailyWater[i] = waterHistory.get(i).getAsDouble();
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
