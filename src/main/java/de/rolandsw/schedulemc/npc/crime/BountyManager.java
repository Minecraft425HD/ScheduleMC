package de.rolandsw.schedulemc.npc.crime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Zentrales Kopfgeldsystem (Bounty System) für ScheduleMC.
 *
 * <p>Dieser Manager verwaltet automatische und manuelle Kopfgelder auf Spieler mit Verbrechen.
 * Das System integriert sich nahtlos mit dem Crime-System und ermöglicht es Spielern,
 * Belohnungen für das Fangen von Kriminellen einzulösen.</p>
 *
 * <h2>Hauptfunktionalität:</h2>
 * <ul>
 *   <li><b>Auto-Bounties:</b> Automatische Kopfgelder ab 3 Wanted-Stars (2.000€/Star)</li>
 *   <li><b>Manual Bounties:</b> Spieler können Kopfgelder auf andere Spieler platzieren</li>
 *   <li><b>Bounty Claiming:</b> Belohnungen werden nach Verhaftung/Kill ausgezahlt</li>
 *   <li><b>History Tracking:</b> Vollständige Historie aller eingelösten Kopfgelder</li>
 *   <li><b>Expiration Management:</b> Automatische Bereinigung abgelaufener Bounties</li>
 *   <li><b>Top Bounties:</b> Rangliste der höchsten aktiven Kopfgelder</li>
 * </ul>
 *
 * <h2>Auto-Bounty-System:</h2>
 * <p>Kopfgelder werden automatisch erstellt wenn:</p>
 * <ul>
 *   <li>Spieler Wanted-Level >= 3 erreicht</li>
 *   <li>Betrag: 2.000€ pro Wanted-Star</li>
 *   <li>Mehrere Verbrechen erhöhen bestehendes Bounty</li>
 * </ul>
 *
 * <h2>Manual-Bounty-System:</h2>
 * <p>Spieler können Kopfgelder platzieren durch:</p>
 * <ul>
 *   <li>Zahlung des Bounty-Betrags von eigenem Konto</li>
 *   <li>Angabe eines Grundes (z.B. "Rache für Überfall")</li>
 *   <li>Erhöhung bestehender Bounties durch zusätzliche Zahlungen</li>
 * </ul>
 *
 * <h2>Beispiel-Workflow:</h2>
 * <pre>{@code
 * // 1. Auto-Bounty: Spieler begeht Verbrechen (Wanted-Level = 4)
 * BountyManager manager = BountyManager.getInstance(server);
 * manager.createAutoBounty(criminalUUID, 4); // 8.000€ Bounty
 *
 * // 2. Manual-Bounty: Opfer erhöht Kopfgeld
 * manager.placeBounty(victimUUID, criminalUUID, 5000.0, "Rache für Überfall");
 * // Gesamtbounty: 13.000€
 *
 * // 3. Hunter verhaftet Kriminellen
 * manager.claimBounty(hunterUUID, criminalUUID); // Hunter erhält 13.000€
 * }</pre>
 *
 * <h2>Thread-Safety:</h2>
 * <p>Thread-sicher durch ConcurrentHashMap für Bounty-Maps und
 * Double-Checked Locking mit separatem Lock-Objekt für Singleton.</p>
 *
 * @author ScheduleMC Team
 * @version 1.0
 * @since 1.0.0
 * @see BountyData
 * @see CrimeManager
 * @see PrisonManager
 */
public class BountyManager extends AbstractPersistenceManager<Map<UUID, BountyData>> {
    /**
     * Singleton-Instanz des BountyManagers.
     * <p>Volatile für Double-Checked Locking Pattern.</p>
     */
    private static volatile BountyManager instance;

    /**
     * Separates Lock-Objekt für Thread-sichere Singleton-Initialisierung.
     * <p>Bessere Granularität als Class-Lock.</p>
     */
    private static final Object INSTANCE_LOCK = new Object();

    /**
     * Auto-Bounty-Betrag pro Wanted-Star.
     * <p>Standard: 2.000€ pro Star (z.B. 4 Stars = 8.000€ Bounty)</p>
     */
    private static final double AUTO_BOUNTY_PER_STAR = 2000.0; // 2000€ pro Wanted-Star

    /**
     * Minimaler Wanted-Level für automatisches Bounty.
     * <p>Standard: Ab 3 Stars werden automatisch Kopfgelder platziert.</p>
     */
    private static final int MIN_WANTED_LEVEL_FOR_BOUNTY = 3;  // Ab 3 Stars

    /**
     * Map aller aktiven Kopfgelder organisiert nach Target-UUID.
     * <p>Thread-sicher durch ConcurrentHashMap.</p>
     */
    private final Map<UUID, BountyData> activeBounties = new ConcurrentHashMap<>();

    /**
     * Historie aller eingelösten und abgelaufenen Kopfgelder pro Spieler.
     * <p>Ermöglicht Statistiken und Tracking von Wiederholungstätern.</p>
     */
    private final Map<UUID, List<BountyData>> bountyHistory = new ConcurrentHashMap<>();

    /**
     * MinecraftServer-Referenz für Spielerzugriffe und Benachrichtigungen.
     * <p>Volatile da Server-Referenz bei getInstance() aktualisiert werden kann.</p>
     */
    private volatile MinecraftServer server;

    private BountyManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_bounties.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    /**
     * Gibt die Singleton-Instanz des BountyManagers zurück.
     *
     * <p>Verwendet Double-Checked Locking mit separatem Lock-Objekt für optimale
     * Thread-Safety. Die Server-Referenz wird bei jedem Aufruf aktualisiert.</p>
     *
     * @param server MinecraftServer für Spielerzugriffe (non-null)
     * @return Singleton-Instanz des BountyManagers
     */
    public static BountyManager getInstance(@Nonnull MinecraftServer server) {
        BountyManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new BountyManager(server);
                }
            }
        }
        result.server = server;
        return result;
    }

    /**
     * Gibt die Singleton-Instanz zurück (ohne Server-Parameter).
     *
     * <p>Nur verwenden wenn Instanz bereits initialisiert wurde.
     * Gibt {@code null} zurück wenn noch nicht initialisiert.</p>
     *
     * @return Singleton-Instanz oder {@code null} wenn nicht initialisiert
     */
    @Nullable
    public static BountyManager getInstance() {
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // BOUNTY MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt automatisches Kopfgeld bei hohem Wanted-Level.
     *
     * <p>Wird automatisch vom Crime-System aufgerufen wenn Spieler >= 3 Wanted-Stars erreicht.
     * Berechnet Bounty-Betrag basierend auf Wanted-Level (2.000€/Star). Falls bereits ein
     * aktives Bounty existiert, wird dieses um 2.000€ erhöht.</p>
     *
     * <h3>Beispiele:</h3>
     * <ul>
     *   <li>3 Stars → 6.000€ Bounty</li>
     *   <li>5 Stars → 10.000€ Bounty</li>
     *   <li>Zweites Verbrechen bei 3 Stars → +2.000€ (total 8.000€)</li>
     * </ul>
     *
     * @param criminal UUID des Kriminellen (non-null)
     * @param wantedLevel Aktueller Wanted-Level (nur >= 3 erstellt Bounty)
     * @see #AUTO_BOUNTY_PER_STAR
     * @see #MIN_WANTED_LEVEL_FOR_BOUNTY
     */
    public void createAutoBounty(@Nonnull UUID criminal, int wantedLevel) {
        if (wantedLevel < MIN_WANTED_LEVEL_FOR_BOUNTY) {
            return; // Zu niedriger Wanted-Level
        }

        // Berechne Kopfgeld
        double bountyAmount = wantedLevel * AUTO_BOUNTY_PER_STAR;

        // Existiert bereits ein Bounty?
        BountyData existing = activeBounties.get(criminal);
        if (existing != null && existing.isActive()) {
            // Erhöhe bestehendes Bounty
            existing.increaseAmount(AUTO_BOUNTY_PER_STAR);
            LOGGER.info("Increased bounty for {}: +{}", criminal, AUTO_BOUNTY_PER_STAR);
        } else {
            // Erstelle neues Bounty
            BountyData bounty = new BountyData(criminal, bountyAmount, null,
                "Wanted Level: " + wantedLevel + " ⭐");
            activeBounties.put(criminal, bounty);
            LOGGER.info("Created auto-bounty for {}: {}", criminal, bountyAmount);

            // Benachrichtige Spieler
            ServerPlayer player = server.getPlayerList().getPlayer(criminal);
            if (player != null) {
                player.sendSystemMessage(Component.literal(
                    "§c§l⚠ KOPFGELD PLATZIERT!\n" +
                    "§7Auf deinen Kopf wurde ein Kopfgeld von §a" +
                    String.format("%.2f€", bountyAmount) + " §7ausgesetzt!"
                ));
            }
        }

        save();
    }

    /**
     * Platziert manuelles Kopfgeld von einem Spieler auf einen anderen.
     *
     * <p>Erlaubt Spielern, Kopfgelder auf andere Spieler zu platzieren. Der Bounty-Betrag
     * wird sofort vom Konto des Platzierenden abgezogen. Falls bereits ein Bounty existiert,
     * wird dieses um den neuen Betrag erhöht.</p>
     *
     * <h3>Validierungen:</h3>
     * <ul>
     *   <li>Betrag muss positiv sein</li>
     *   <li>Spieler kann nicht auf sich selbst Bounty platzieren</li>
     *   <li>Ausreichendes Guthaben muss vorhanden sein</li>
     * </ul>
     *
     * @param placerUUID UUID des Spielers, der das Bounty platziert (non-null)
     * @param targetUUID UUID des Zielspielers (non-null)
     * @param amount Bounty-Betrag in € (muss > 0 sein)
     * @param reason Grund für das Kopfgeld (z.B. "Rache für Überfall")
     * @return {@code true} bei erfolgreicher Platzierung, {@code false} bei Validierungsfehlern
     * @see EconomyManager#withdraw(UUID, double, TransactionType, String)
     */
    public boolean placeBounty(@Nonnull UUID placerUUID, @Nonnull UUID targetUUID, double amount, String reason) {
        // Validierung
        if (amount <= 0) {
            return false;
        }

        if (placerUUID.equals(targetUUID)) {
            return false; // Kann nicht auf sich selbst bounty platzieren
        }

        // Prüfe Kontostand
        if (!EconomyManager.withdraw(placerUUID, amount, TransactionType.OTHER,
                "Kopfgeld auf: " + targetUUID)) {
            return false; // Nicht genug Geld
        }

        // Existiert bereits ein Bounty?
        BountyData existing = activeBounties.get(targetUUID);
        if (existing != null && existing.isActive()) {
            // Erhöhe bestehendes Bounty
            existing.increaseAmount(amount);
        } else {
            // Erstelle neues Bounty
            BountyData bounty = new BountyData(targetUUID, amount, placerUUID, reason);
            activeBounties.put(targetUUID, bounty);
        }

        // Benachrichtige Target
        ServerPlayer target = server.getPlayerList().getPlayer(targetUUID);
        if (target != null) {
            target.sendSystemMessage(Component.literal(
                "§c§l⚠ KOPFGELD ERHÖHT!\n" +
                "§7Jemand hat §a" + String.format("%.2f€", amount) +
                " §7auf deinen Kopf ausgesetzt!\n" +
                "§7Grund: §e" + reason
            ));
        }

        LOGGER.info("Player {} placed bounty on {}: {}", placerUUID, targetUUID, amount);
        save();
        return true;
    }

    /**
     * Löst Kopfgeld ein nach Verhaftung oder Eliminierung des Ziels.
     *
     * <p>Zahlt die Bounty-Belohnung an den Hunter aus und verschiebt das Bounty in die
     * Historie. Hunter und Target werden über die Einlösung benachrichtigt.</p>
     *
     * <h3>Voraussetzungen:</h3>
     * <ul>
     *   <li>Aktives Bounty auf Target muss existieren</li>
     *   <li>Bounty darf nicht abgelaufen sein</li>
     * </ul>
     *
     * @param hunterUUID UUID des Bounty-Hunters (non-null)
     * @param targetUUID UUID des Zielspielers (non-null)
     * @return {@code true} bei erfolgreicher Einlösung, {@code false} wenn kein aktives Bounty
     * @see EconomyManager#deposit(UUID, double, TransactionType, String)
     */
    public boolean claimBounty(@Nonnull UUID hunterUUID, @Nonnull UUID targetUUID) {
        BountyData bounty = activeBounties.get(targetUUID);

        if (bounty == null || !bounty.isActive()) {
            return false; // Kein aktives Bounty
        }

        // Bounty einlösen
        if (!bounty.claim(hunterUUID)) {
            return false;
        }

        // Belohnung auszahlen
        double reward = bounty.getAmount();
        EconomyManager.deposit(hunterUUID, reward, TransactionType.OTHER,
            "Kopfgeld: " + targetUUID);

        // Zu Historie hinzufügen
        bountyHistory.computeIfAbsent(targetUUID, k -> new ArrayList<>()).add(bounty);

        // Aus aktiven Bounties entfernen
        activeBounties.remove(targetUUID);

        // Benachrichtige Hunter
        ServerPlayer hunter = server.getPlayerList().getPlayer(hunterUUID);
        if (hunter != null) {
            hunter.sendSystemMessage(Component.literal(
                "§a§l✓ KOPFGELD EINGELÖST!\n" +
                "§7Du hast §a" + String.format("%.2f€", reward) + " §7erhalten!"
            ));
        }

        // Benachrichtige Target
        ServerPlayer target = server.getPlayerList().getPlayer(targetUUID);
        if (target != null) {
            target.sendSystemMessage(Component.literal(
                "§c§lDein Kopfgeld wurde eingelöst!"
            ));
        }

        LOGGER.info("Bounty claimed: {} -> {}, amount: {}", targetUUID, hunterUUID, reward);
        save();
        return true;
    }

    /**
     * Gibt aktives Kopfgeld für einen Spieler zurück.
     *
     * <p>Prüft zusätzlich ob das Bounty noch aktiv ist (nicht abgelaufen/eingelöst).</p>
     *
     * @param playerUUID UUID des Spielers
     * @return Aktives Bounty-Objekt oder {@code null} wenn keines vorhanden
     */
    @Nullable
    public BountyData getActiveBounty(UUID playerUUID) {
        BountyData bounty = activeBounties.get(playerUUID);
        if (bounty != null && bounty.isActive()) {
            return bounty;
        }
        return null;
    }

    /**
     * Gibt alle aktiven Kopfgelder sortiert nach Höhe zurück.
     *
     * <p>Sortierung: Höchste Beträge zuerst (für Rangliste).</p>
     *
     * @return Liste aller aktiven Bounties, sortiert nach Betrag (absteigend)
     */
    public List<BountyData> getAllActiveBounties() {
        return activeBounties.values().stream()
            .filter(BountyData::isActive)
            .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount())) // Höchste zuerst
            .collect(Collectors.toList());
    }

    /**
     * Gibt die Top-N-Kopfgelder zurück (Rangliste).
     *
     * <p>Nützlich für Anzeigetafeln und Ranglisten der höchsten Bounties.</p>
     *
     * @param limit Maximale Anzahl Bounties (z.B. 10 für Top-10)
     * @return Liste der höchsten Bounties, limitiert auf angegebene Anzahl
     */
    public List<BountyData> getTopBounties(int limit) {
        return getAllActiveBounties().stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Gibt Kopfgeld-Historie für einen Spieler zurück.
     *
     * <p>Enthält alle eingelösten und abgelaufenen Bounties für Statistiken.</p>
     *
     * @param playerUUID UUID des Spielers
     * @return Liste der historischen Bounties, leer wenn keine vorhanden
     */
    public List<BountyData> getBountyHistory(UUID playerUUID) {
        return bountyHistory.getOrDefault(playerUUID, Collections.emptyList());
    }

    /**
     * Entfernt alle abgelaufenen Kopfgelder.
     *
     * <p>Verschiebt abgelaufene Bounties in die Historie und entfernt sie aus aktiven Bounties.
     * Sollte regelmäßig (z.B. täglich) aufgerufen werden.</p>
     */
    public void cleanupExpiredBounties() {
        int removed = 0;
        Iterator<Map.Entry<UUID, BountyData>> it = activeBounties.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, BountyData> entry = it.next();
            BountyData bounty = entry.getValue();

            if (bounty.isExpired()) {
                bountyHistory.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(bounty);
                it.remove();
                removed++;
            }
        }

        if (removed > 0) {
            LOGGER.info("Removed {} expired bounties", removed);
            save();
        }
    }

    /**
     * Gibt formatierte Statistiken über aktive Kopfgelder zurück.
     *
     * <p>Berechnet Anzahl aktiver Bounties und Gesamtwert in einem einzigen Durchlauf
     * (Single-Pass Optimierung statt doppelter Stream-Operation).</p>
     *
     * @return Formatierte Statistik-String (z.B. "Active Bounties: 5, Total: 25000.00€")
     */
    public String getStatistics() {
        int active = 0;
        double totalAmount = 0.0;

        for (BountyData bounty : activeBounties.values()) {
            if (bounty.isActive()) {
                active++;
                totalAmount += bounty.getAmount();
            }
        }

        return String.format("Active Bounties: %d, Total: %.2f€", active, totalAmount);
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, BountyData>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, BountyData> data) {
        activeBounties.clear();
        activeBounties.putAll(data);
        cleanupExpiredBounties();
    }

    @Override
    protected Map<UUID, BountyData> getCurrentData() {
        return new HashMap<>(activeBounties);
    }

    @Override
    protected String getComponentName() {
        return "BountyManager";
    }

    @Override
    protected String getHealthDetails() {
        return getStatistics();
    }

    @Override
    protected void onCriticalLoadFailure() {
        activeBounties.clear();
        bountyHistory.clear();
    }
}
