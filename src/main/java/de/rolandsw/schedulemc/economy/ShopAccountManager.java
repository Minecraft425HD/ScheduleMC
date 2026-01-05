package de.rolandsw.schedulemc.economy;
import de.rolandsw.schedulemc.util.EventHelper;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
     * Gibt alle Shop-Konten zurück (Read-only View)
     * Optimierung: Keine Kopie mehr, nutzt unmodifiable view
     */
    public static Collection<ShopAccount> getAllAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
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
        EventHelper.handleServerTickEnd(event, server -> {
            if (server.getTickCount() % 20 == 0) {
                // Nur jede Sekunde ticken (20 ticks)
                Level overworld = server.overworld();
                tickAll(overworld);
            }
        });
    }

    /**
     * Gibt Liste aller Shop-IDs zurück (Read-only View)
     * Optimierung: Keine Kopie mehr, nutzt unmodifiable view
     */
    public static List<String> getAllShopIds() {
        return Collections.unmodifiableList(new ArrayList<>(accounts.keySet()));
    }

    /**
     * Gibt Anzahl der Shops zurück
     */
    public static int getShopCount() {
        return accounts.size();
    }
}
