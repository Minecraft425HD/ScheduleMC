package de.rolandsw.schedulemc.economy;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet alle Shop-Konten
 * Koordiniert Ticks für 7-Tage-Tracking und Gewinnausschüttungen
 */
@Mod.EventBusSubscriber
public class ShopAccountManager {

    private static final Map<String, ShopAccount> accounts = new ConcurrentHashMap<>();

    /**
     * Holt oder erstellt Shop-Konto
     */
    public static ShopAccount getOrCreateAccount(String shopId) {
        return accounts.computeIfAbsent(shopId, ShopAccount::new);
    }

    /**
     * Gibt Shop-Konto zurück (oder null wenn nicht existiert)
     */
    @Nullable
    public static ShopAccount getAccount(String shopId) {
        return accounts.get(shopId);
    }

    /**
     * Gibt alle Shop-Konten zurück
     */
    public static Collection<ShopAccount> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    /**
     * Prüft ob Shop-Konto existiert
     */
    public static boolean exists(String shopId) {
        return accounts.containsKey(shopId);
    }

    /**
     * Entfernt Shop-Konto
     */
    public static void removeAccount(String shopId) {
        accounts.remove(shopId);
    }

    /**
     * Tickt alle Shop-Konten
     */
    public static void tickAll(Level level) {
        for (ShopAccount account : accounts.values()) {
            account.tick(level);
        }
    }

    /**
     * Server Tick Event - Koordiniert alle Shop-Konten
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.getServer().getTickCount() % 20 == 0) {
            // Nur jede Sekunde ticken (20 ticks)
            Level overworld = event.getServer().overworld();
            tickAll(overworld);
        }
    }

    /**
     * Gibt Liste aller Shop-IDs zurück
     */
    public static List<String> getAllShopIds() {
        return new ArrayList<>(accounts.keySet());
    }

    /**
     * Gibt Anzahl der Shops zurück
     */
    public static int getShopCount() {
        return accounts.size();
    }
}
