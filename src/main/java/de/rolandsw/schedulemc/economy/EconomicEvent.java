package de.rolandsw.schedulemc.economy;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Wirtschafts-Event das Preise beeinflusst
 *
 * Beispiele:
 * - "Große Dürre": Nahrung 2.5x teurer
 * - "Eisenerz-Entdeckung": Eisen 0.6x günstiger
 * - "Händlerfestival": Alle Items 0.8x günstiger
 */
public class EconomicEvent {
    private final String name;
    private final Map<Item, Float> itemMultipliers = new HashMap<>();
    private final long expiryTime;

    /**
     * @param name Event-Name
     * @param multipliers Item → Multiplikator Map
     * @param durationDays Dauer in Minecraft-Tagen
     */
    public EconomicEvent(String name, Map<Item, Float> multipliers, int durationDays) {
        this.name = name;
        this.itemMultipliers.putAll(multipliers);
        this.expiryTime = System.currentTimeMillis() + (durationDays * 20L * 60 * 1000); // ~20 Minuten pro MC-Tag
    }

    public String getName() {
        return name;
    }

    public boolean affectsItem(Item item) {
        return itemMultipliers.containsKey(item);
    }

    public float getMultiplier(Item item) {
        return itemMultipliers.getOrDefault(item, 1.0f);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public long getRemainingTime() {
        return Math.max(0, expiryTime - System.currentTimeMillis());
    }

    public int getRemainingDays() {
        return (int)(getRemainingTime() / (20L * 60 * 1000)); // ~20min pro Tag
    }

    @Override
    public String toString() {
        return String.format("%s (noch %d Tage)", name, getRemainingDays());
    }
}
