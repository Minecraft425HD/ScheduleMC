package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verwaltet Business-Metriken für einen NPC
 * Speichert in NPCData.customData
 */
public class NPCBusinessMetrics {

    private final CustomNPCEntity npc;
    private final NPCData npcData;

    // Metriken
    private int satisfaction;                          // 0-100
    private DemandLevel demand;                        // LOW, MEDIUM, HIGH
    private long lastPurchaseDay;                      // Letzter Kauf-Tag
    private int totalPurchases;                        // Gesamt-Käufe
    private Map<String, Integer> playerReputation;     // UUID -> Reputation
    private List<Purchase> purchaseHistory;            // Letzte 10 Käufe

    public NPCBusinessMetrics(CustomNPCEntity npc) {
        this.npc = npc;
        this.npcData = npc.getNpcData();
        load();
    }

    /**
     * Lädt Metriken aus customData
     */
    private void load() {
        CompoundTag data = npcData.getCustomData();

        this.satisfaction = data.contains("satisfaction") ? data.getInt("satisfaction") : 50;
        this.demand = data.contains("tobaccoDemand") ?
            DemandLevel.valueOf(data.getString("tobaccoDemand")) : DemandLevel.MEDIUM;
        this.lastPurchaseDay = data.contains("lastPurchaseDay") ? data.getLong("lastPurchaseDay") : -1;
        this.totalPurchases = data.contains("totalPurchases") ? data.getInt("totalPurchases") : 0;

        // Player Reputation
        this.playerReputation = new HashMap<>();
        if (data.contains("playerReputation", Tag.TAG_COMPOUND)) {
            CompoundTag repData = data.getCompound("playerReputation");
            for (String uuid : repData.getAllKeys()) {
                playerReputation.put(uuid, repData.getInt(uuid));
            }
        }

        // Purchase History
        this.purchaseHistory = new ArrayList<>();
        if (data.contains("purchaseHistory", Tag.TAG_LIST)) {
            ListTag historyList = data.getList("purchaseHistory", Tag.TAG_COMPOUND);
            for (int i = 0; i < historyList.size(); i++) {
                CompoundTag purchaseTag = historyList.getCompound(i);
                purchaseHistory.add(new Purchase(
                    purchaseTag.getString("player"),
                    TobaccoType.valueOf(purchaseTag.getString("type")),
                    TobaccoQuality.valueOf(purchaseTag.getString("quality")),
                    purchaseTag.getInt("weight"),
                    purchaseTag.getDouble("price"),
                    purchaseTag.getLong("day")
                ));
            }
        }
    }

    /**
     * Speichert Metriken in customData
     */
    public void save() {
        CompoundTag data = npcData.getCustomData();

        data.putInt("satisfaction", satisfaction);
        data.putString("tobaccoDemand", demand.name());
        data.putLong("lastPurchaseDay", lastPurchaseDay);
        data.putInt("totalPurchases", totalPurchases);

        // Player Reputation
        CompoundTag repData = new CompoundTag();
        for (Map.Entry<String, Integer> entry : playerReputation.entrySet()) {
            repData.putInt(entry.getKey(), entry.getValue());
        }
        data.put("playerReputation", repData);

        // Purchase History
        ListTag historyList = new ListTag();
        for (Purchase purchase : purchaseHistory) {
            CompoundTag purchaseTag = new CompoundTag();
            purchaseTag.putString("player", purchase.getPlayerUUID());
            purchaseTag.putString("type", purchase.getType().name());
            purchaseTag.putString("quality", purchase.getQuality().name());
            purchaseTag.putInt("weight", purchase.getWeight());
            purchaseTag.putDouble("price", purchase.getPrice());
            purchaseTag.putLong("day", purchase.getDay());
            historyList.add(purchaseTag);
        }
        data.put("purchaseHistory", historyList);

        // Note: No need to call npcData.save() here as we're already modifying the customData reference directly
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS/SETTERS
    // ═══════════════════════════════════════════════════════════

    public int getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(int value) {
        this.satisfaction = Math.max(0, Math.min(100, value));
    }

    public void modifySatisfaction(int delta) {
        setSatisfaction(satisfaction + delta);
    }

    public DemandLevel getDemand() {
        return demand;
    }

    public void setDemand(DemandLevel demand) {
        this.demand = demand;
    }

    public long getLastPurchaseDay() {
        return lastPurchaseDay;
    }

    public void setLastPurchaseDay(long day) {
        this.lastPurchaseDay = day;
    }

    public int getTotalPurchases() {
        return totalPurchases;
    }

    public int getReputation(String uuid) {
        return playerReputation.getOrDefault(uuid, 50);
    }

    public void setReputation(String uuid, int value) {
        this.playerReputation.put(uuid, Math.max(0, Math.min(100, value)));
    }

    public void modifyReputation(String uuid, int delta) {
        int current = getReputation(uuid);
        setReputation(uuid, current + delta);
    }

    public List<Purchase> getPurchaseHistory() {
        return purchaseHistory;
    }

    // ═══════════════════════════════════════════════════════════
    // BUSINESS-LOGIK
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen Kauf
     */
    public void recordPurchase(String playerUUID, TobaccoType type, TobaccoQuality quality,
                              int weight, double price, long day) {
        Purchase purchase = new Purchase(playerUUID, type, quality, weight, price, day);
        purchaseHistory.add(0, purchase);

        // Nur letzte 10 behalten
        if (purchaseHistory.size() > 10) {
            purchaseHistory = purchaseHistory.subList(0, 10);
        }

        totalPurchases++;
        lastPurchaseDay = day;

        // Zufriedenheit steigt
        modifySatisfaction(5);

        // Nachfrage sinkt nach Kauf
        if (demand == DemandLevel.HIGH) {
            demand = DemandLevel.MEDIUM;
        } else if (demand == DemandLevel.MEDIUM && totalPurchases % 3 == 0) {
            demand = DemandLevel.LOW;
        }

        save();
    }

    /**
     * Nachfrage regenerieren (täglich aufgerufen)
     */
    public void regenerateDemand() {
        if (demand == DemandLevel.LOW) {
            demand = DemandLevel.MEDIUM;
        } else if (demand == DemandLevel.MEDIUM) {
            demand = DemandLevel.HIGH;
        }
        save();
    }
}
