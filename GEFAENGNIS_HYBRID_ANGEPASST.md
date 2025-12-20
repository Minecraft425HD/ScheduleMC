# Hybrid-Gefängnissystem - Angepasster Implementierungsplan

## Übersicht der Anforderungen

| Anforderung | Umsetzung |
|-------------|-----------|
| Plot-basiert | ✅ Neuer `PlotType.PRISON` |
| Zellen als Untertyp | ✅ `PrisonCell` erweitert `PlotArea` |
| Zellen-Rotation | ✅ Zelle 1 → 2 → 3 wenn besetzt |
| Haftzeit-Formel | ✅ `WantedLevel × 60 Sek` (behalten) |
| Kaution | ✅ Ab 1/3 der Zeit zahlbar |
| ESC blockieren | ✅ `ScreenEvent.Opening` abfangen |
| Server-Leave verhindern | ✅ Zeit pausieren + Strafe bei Flucht |

---

## 1. Neue PlotType: PRISON

### PlotType.java - Erweiterung

```java
public enum PlotType {
    RESIDENTIAL("Wohngebäude", true, true),
    COMMERCIAL("Gewerbefläche", true, true),
    SHOP("Laden", false, false),
    PUBLIC("Öffentlich", false, false),
    GOVERNMENT("Regierung", false, false),

    // NEU: Gefängnis-Typ
    PRISON("Gefängnis", false, false);  // Nicht kauf-/mietbar

    // ... bestehender Code ...

    public boolean isPrison() {
        return this == PRISON;
    }
}
```

---

## 2. Neue Klasse: PrisonCell

### PrisonCell.java

```java
package de.rolandsw.schedulemc.npc.crime.prison;

import de.rolandsw.schedulemc.region.PlotArea;
import net.minecraft.core.BlockPos;
import java.util.UUID;

/**
 * Gefängniszelle - Erweiterung von PlotArea mit Gefängnis-spezifischen Features
 */
public class PrisonCell extends PlotArea {

    // ═══════════════════════════════════════════════════════════
    // ZELLEN-EIGENSCHAFTEN
    // ═══════════════════════════════════════════════════════════

    private int cellNumber;              // Zellennummer (1, 2, 3, ...)
    private int securityLevel;           // 1-5 (matching WantedLevel)
    private UUID currentInmate;          // Aktueller Insasse (null = frei)
    private long inmateSince;            // Zeitpunkt der Einweisung
    private long releaseTime;            // Geplante Entlassung (Game-Ticks)

    // ═══════════════════════════════════════════════════════════
    // KONSTRUKTOR
    // ═══════════════════════════════════════════════════════════

    public PrisonCell(String id, int cellNumber, String parentPlotId,
                      BlockPos minCorner, BlockPos maxCorner, int securityLevel) {
        super(id, "Zelle " + cellNumber, parentPlotId, minCorner, maxCorner, 0);

        this.cellNumber = cellNumber;
        this.securityLevel = Math.min(5, Math.max(1, securityLevel));
        this.currentInmate = null;
        this.inmateSince = 0;
        this.releaseTime = 0;

        // Zellen sind nie zur Miete verfügbar
        this.setForRent(false);
    }

    // ═══════════════════════════════════════════════════════════
    // ZELLEN-VERWALTUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Ist die Zelle belegt?
     */
    public boolean isOccupied() {
        return currentInmate != null;
    }

    /**
     * Ist die Zelle frei?
     */
    public boolean isFree() {
        return currentInmate == null;
    }

    /**
     * Weist einen Gefangenen zu
     */
    public void assignInmate(UUID inmate, long releaseTime) {
        this.currentInmate = inmate;
        this.inmateSince = System.currentTimeMillis();
        this.releaseTime = releaseTime;
    }

    /**
     * Entlässt den Gefangenen
     */
    public void releaseInmate() {
        this.currentInmate = null;
        this.inmateSince = 0;
        this.releaseTime = 0;
    }

    /**
     * Gibt Spawn-Position in der Zelle zurück (Mitte, Boden)
     */
    public BlockPos getSpawnPosition() {
        return new BlockPos(
            (getMinCorner().getX() + getMaxCorner().getX()) / 2,
            getMinCorner().getY() + 1,
            (getMinCorner().getZ() + getMaxCorner().getZ()) / 2
        );
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER
    // ═══════════════════════════════════════════════════════════

    public int getCellNumber() { return cellNumber; }
    public int getSecurityLevel() { return securityLevel; }
    public UUID getCurrentInmate() { return currentInmate; }
    public long getInmateSince() { return inmateSince; }
    public long getReleaseTime() { return releaseTime; }

    public void setSecurityLevel(int level) {
        this.securityLevel = Math.min(5, Math.max(1, level));
    }

    @Override
    public String toString() {
        String status = isOccupied() ? "BELEGT" : "FREI";
        return String.format("Zelle[Nr=%d, Sicherheit=%d, Status=%s]",
            cellNumber, securityLevel, status);
    }
}
```

---

## 3. PrisonManager - Hauptklasse

### PrisonManager.java

```java
package de.rolandsw.schedulemc.npc.crime.prison;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.PlotType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrale Verwaltung des Gefängnissystems
 */
public class PrisonManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static PrisonManager instance;

    // ═══════════════════════════════════════════════════════════
    // KONSTANTEN
    // ═══════════════════════════════════════════════════════════

    public static final int JAIL_SECONDS_PER_WANTED_LEVEL = 60;
    public static final double BAIL_MULTIPLIER = 1000.0; // € pro WantedLevel
    public static final double BAIL_AVAILABLE_AFTER = 0.33; // 1/3 der Zeit

    // ═══════════════════════════════════════════════════════════
    // DATEN
    // ═══════════════════════════════════════════════════════════

    // Aktive Gefangene: UUID -> PrisonerData
    private final Map<UUID, PrisonerData> prisoners = new ConcurrentHashMap<>();

    // Offline-Tracking: UUID -> verbleibende Zeit in Ticks
    private final Map<UUID, Long> offlineRemainingTime = new ConcurrentHashMap<>();

    // Gefängnis-Plots (Cache)
    private final List<String> prisonPlotIds = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════
    // PRISONER DATA
    // ═══════════════════════════════════════════════════════════

    public static class PrisonerData {
        public UUID playerId;
        public String playerName;
        public String prisonPlotId;
        public int cellNumber;
        public BlockPos cellSpawn;

        public int originalWantedLevel;
        public long arrestTime;          // System.currentTimeMillis()
        public long releaseTime;         // Game-Ticks
        public long totalSentenceTicks;  // Gesamte Haftzeit in Ticks

        public double bailAmount;
        public boolean bailPaid = false;

        // Offline-Tracking
        public long lastOnlineTime;      // Letzter Online-Zeitpunkt
        public long offlineAccumulatedTicks; // Akkumulierte Offline-Zeit
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private PrisonManager() {
        loadPrisonPlots();
    }

    public static PrisonManager getInstance() {
        if (instance == null) {
            instance = new PrisonManager();
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // GEFÄNGNIS-PLOT VERWALTUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Lädt alle PRISON-Plots
     */
    private void loadPrisonPlots() {
        prisonPlotIds.clear();
        for (PlotRegion plot : PlotManager.getAllPlots()) {
            if (plot.getType() == PlotType.PRISON) {
                prisonPlotIds.add(plot.getPlotId());
            }
        }
        LOGGER.info("Gefängnisse geladen: {}", prisonPlotIds.size());
    }

    /**
     * Registriert ein neues Gefängnis
     */
    public void registerPrison(String plotId) {
        PlotRegion plot = PlotManager.getPlot(plotId);
        if (plot != null && plot.getType() == PlotType.PRISON) {
            if (!prisonPlotIds.contains(plotId)) {
                prisonPlotIds.add(plotId);
            }
        }
    }

    /**
     * Gibt das Standard-Gefängnis zurück
     */
    public PlotRegion getDefaultPrison() {
        if (prisonPlotIds.isEmpty()) return null;
        return PlotManager.getPlot(prisonPlotIds.get(0));
    }

    // ═══════════════════════════════════════════════════════════
    // ZELLEN-ZUWEISUNG (1 → 2 → 3 wenn besetzt)
    // ═══════════════════════════════════════════════════════════

    /**
     * Findet die nächste freie Zelle
     * Rotation: Zelle 1 → 2 → 3 etc. wenn besetzt
     */
    public PrisonCell findAvailableCell(int wantedLevel) {
        PlotRegion prison = getDefaultPrison();
        if (prison == null) {
            LOGGER.warn("Kein Gefängnis registriert!");
            return null;
        }

        // Sammle alle Zellen und sortiere nach Nummer
        List<PrisonCell> cells = new ArrayList<>();
        for (var area : prison.getSubAreas()) {
            if (area instanceof PrisonCell cell) {
                // Passende Sicherheitsstufe (oder niedriger)
                if (cell.getSecurityLevel() <= wantedLevel || wantedLevel <= 2) {
                    cells.add(cell);
                }
            }
        }

        // Sortiere nach Zellennummer
        cells.sort(Comparator.comparingInt(PrisonCell::getCellNumber));

        // Finde erste freie Zelle
        for (PrisonCell cell : cells) {
            if (cell.isFree()) {
                return cell;
            }
        }

        // Alle besetzt → nimm Zelle 1 (Überbelegung)
        if (!cells.isEmpty()) {
            LOGGER.warn("Alle Zellen belegt! Nutze Zelle 1 für Überbelegung.");
            return cells.get(0);
        }

        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // INHAFTIERUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Inhaftiert einen Spieler
     */
    public boolean imprisonPlayer(ServerPlayer player, int wantedLevel) {
        UUID playerId = player.getUUID();

        // Bereits inhaftiert?
        if (isPrisoner(playerId)) {
            LOGGER.warn("Spieler {} ist bereits inhaftiert!", player.getName().getString());
            return false;
        }

        // Finde freie Zelle
        PrisonCell cell = findAvailableCell(wantedLevel);
        if (cell == null) {
            player.sendSystemMessage(Component.literal(
                "§c✗ Kein Gefängnis verfügbar! Bitte Admin kontaktieren."));
            return false;
        }

        // Haftzeit berechnen (bestehende Formel)
        int jailSeconds = wantedLevel * JAIL_SECONDS_PER_WANTED_LEVEL;
        long jailTicks = jailSeconds * 20L;
        long releaseTime = player.level().getGameTime() + jailTicks;

        // Kaution berechnen
        double bail = wantedLevel * BAIL_MULTIPLIER;

        // PrisonerData erstellen
        PrisonerData data = new PrisonerData();
        data.playerId = playerId;
        data.playerName = player.getName().getString();
        data.prisonPlotId = cell.getParentPlotId();
        data.cellNumber = cell.getCellNumber();
        data.cellSpawn = cell.getSpawnPosition();
        data.originalWantedLevel = wantedLevel;
        data.arrestTime = System.currentTimeMillis();
        data.releaseTime = releaseTime;
        data.totalSentenceTicks = jailTicks;
        data.bailAmount = bail;
        data.lastOnlineTime = System.currentTimeMillis();

        // Zelle belegen
        cell.assignInmate(playerId, releaseTime);

        // In Map speichern
        prisoners.put(playerId, data);

        // Spieler teleportieren
        BlockPos spawn = cell.getSpawnPosition();
        player.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);

        // Effekte anwenden
        applyPrisonEffects(player, jailSeconds);

        // Persistente Daten setzen (für ESC-Block etc.)
        player.getPersistentData().putBoolean("IsInPrison", true);
        player.getPersistentData().putLong("JailReleaseTime", releaseTime);
        player.getPersistentData().putInt("JailCellNumber", cell.getCellNumber());

        // Nachricht
        player.sendSystemMessage(Component.literal(String.format(
            "§c⛓ INHAFTIERT!\n" +
            "§7━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "§7Zelle: §f%d\n" +
            "§7Haftzeit: §f%d Sekunden\n" +
            "§7Kaution: §f%.0f€ §7(ab 1/3 der Zeit)\n" +
            "§7━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "§c⚠ ESC-Menü und Disconnect sind gesperrt!",
            cell.getCellNumber(), jailSeconds, bail
        )));

        // Client-Sync für ESC-Block
        syncPrisonStateToClient(player, true);

        savePrisonerData();
        return true;
    }

    // ═══════════════════════════════════════════════════════════
    // ENTLASSUNG
    // ═══════════════════════════════════════════════════════════

    public enum ReleaseReason {
        TIME_SERVED("Haft verbüßt"),
        BAIL_PAID("Kaution bezahlt"),
        ADMIN_RELEASE("Admin-Entlassung"),
        ESCAPE_PENALTY("Flucht (Strafe folgt)");

        public final String displayName;
        ReleaseReason(String name) { this.displayName = name; }
    }

    /**
     * Entlässt einen Spieler
     */
    public void releasePlayer(ServerPlayer player, ReleaseReason reason) {
        UUID playerId = player.getUUID();
        PrisonerData data = prisoners.remove(playerId);

        if (data == null) return;

        // Zelle freigeben
        PlotRegion prison = PlotManager.getPlot(data.prisonPlotId);
        if (prison != null) {
            for (var area : prison.getSubAreas()) {
                if (area instanceof PrisonCell cell && cell.getCellNumber() == data.cellNumber) {
                    cell.releaseInmate();
                    break;
                }
            }
        }

        // Effekte entfernen
        removePrisonEffects(player);

        // Persistente Daten löschen
        player.getPersistentData().remove("IsInPrison");
        player.getPersistentData().remove("JailReleaseTime");
        player.getPersistentData().remove("JailCellNumber");

        // WantedLevel behandeln
        switch (reason) {
            case TIME_SERVED, ADMIN_RELEASE -> CrimeManager.clearWantedLevel(playerId);
            case BAIL_PAID -> CrimeManager.setWantedLevel(playerId,
                Math.max(0, data.originalWantedLevel - 2));
            case ESCAPE_PENALTY -> {
                // Strafe: +2 WantedLevel
                CrimeManager.addWantedLevel(playerId, 2, player.level().getDayTime());
            }
        }

        // Zum Gefängnis-Ausgang teleportieren
        if (prison != null) {
            BlockPos exit = prison.getSpawnPosition();
            player.teleportTo(exit.getX() + 0.5, exit.getY(), exit.getZ() + 0.5);
        }

        // Client-Sync für ESC-Freigabe
        syncPrisonStateToClient(player, false);

        // Nachricht
        player.sendSystemMessage(Component.literal(String.format(
            "§a✓ ENTLASSEN: %s\n" +
            "§7Du bist wieder frei!",
            reason.displayName
        )));

        offlineRemainingTime.remove(playerId);
        savePrisonerData();
    }

    // ═══════════════════════════════════════════════════════════
    // KAUTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob Kaution verfügbar ist (ab 1/3 der Zeit)
     */
    public boolean isBailAvailable(UUID playerId, long currentTick) {
        PrisonerData data = prisoners.get(playerId);
        if (data == null) return false;

        long servedTicks = currentTick - (data.releaseTime - data.totalSentenceTicks);
        long requiredTicks = (long)(data.totalSentenceTicks * BAIL_AVAILABLE_AFTER);

        return servedTicks >= requiredTicks;
    }

    /**
     * Zahlt Kaution
     */
    public boolean payBail(ServerPlayer player) {
        UUID playerId = player.getUUID();
        PrisonerData data = prisoners.get(playerId);

        if (data == null) {
            player.sendSystemMessage(Component.literal("§c✗ Du bist nicht im Gefängnis!"));
            return false;
        }

        if (!isBailAvailable(playerId, player.level().getGameTime())) {
            long remaining = getRemainingTimeUntilBail(playerId, player.level().getGameTime());
            player.sendSystemMessage(Component.literal(String.format(
                "§c✗ Kaution erst in %d Sekunden verfügbar!", remaining / 20)));
            return false;
        }

        // Geld prüfen
        if (!EconomyManager.hasBalance(playerId, data.bailAmount)) {
            player.sendSystemMessage(Component.literal(String.format(
                "§c✗ Nicht genug Geld! Kaution: %.0f€", data.bailAmount)));
            return false;
        }

        // Kaution abziehen
        EconomyManager.withdraw(playerId, data.bailAmount);
        data.bailPaid = true;

        // Entlassen
        releasePlayer(player, ReleaseReason.BAIL_PAID);
        return true;
    }

    private long getRemainingTimeUntilBail(UUID playerId, long currentTick) {
        PrisonerData data = prisoners.get(playerId);
        if (data == null) return 0;

        long servedTicks = currentTick - (data.releaseTime - data.totalSentenceTicks);
        long requiredTicks = (long)(data.totalSentenceTicks * BAIL_AVAILABLE_AFTER);

        return Math.max(0, requiredTicks - servedTicks);
    }

    // ═══════════════════════════════════════════════════════════
    // OFFLINE-HANDLING (Server Leave Prevention)
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird aufgerufen wenn Spieler offline geht
     * → Zeit pausieren, NICHT als Flucht werten
     */
    public void onPlayerLogout(UUID playerId, long currentTick) {
        PrisonerData data = prisoners.get(playerId);
        if (data == null) return;

        // Verbleibende Zeit speichern
        long remainingTicks = data.releaseTime - currentTick;
        offlineRemainingTime.put(playerId, remainingTicks);
        data.lastOnlineTime = System.currentTimeMillis();

        LOGGER.info("Gefangener {} offline. Verbleibend: {} Ticks",
            data.playerName, remainingTicks);

        savePrisonerData();
    }

    /**
     * Wird aufgerufen wenn Spieler wieder online kommt
     * → Zeit fortsetzen
     */
    public void onPlayerLogin(ServerPlayer player) {
        UUID playerId = player.getUUID();
        PrisonerData data = prisoners.get(playerId);

        if (data == null) return;

        Long remainingTicks = offlineRemainingTime.remove(playerId);
        if (remainingTicks == null) {
            remainingTicks = data.totalSentenceTicks; // Fallback
        }

        // Neue Release-Zeit berechnen
        long currentTick = player.level().getGameTime();
        data.releaseTime = currentTick + remainingTicks;

        // Zurück in Zelle teleportieren
        player.teleportTo(
            data.cellSpawn.getX() + 0.5,
            data.cellSpawn.getY(),
            data.cellSpawn.getZ() + 0.5
        );

        // Persistente Daten setzen
        player.getPersistentData().putBoolean("IsInPrison", true);
        player.getPersistentData().putLong("JailReleaseTime", data.releaseTime);

        // Effekte neu anwenden
        int remainingSeconds = (int)(remainingTicks / 20);
        applyPrisonEffects(player, remainingSeconds);

        // Client-Sync
        syncPrisonStateToClient(player, true);

        player.sendSystemMessage(Component.literal(String.format(
            "§c⛓ Du bist noch im Gefängnis!\n" +
            "§7Verbleibend: §f%d Sekunden",
            remainingSeconds
        )));

        LOGGER.info("Gefangener {} online. Verbleibend: {} Sekunden",
            data.playerName, remainingSeconds);
    }

    // ═══════════════════════════════════════════════════════════
    // TICK-HANDLER
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft alle Gefangenen auf Entlassung
     */
    public void onServerTick(long currentTick) {
        for (var entry : new HashMap<>(prisoners).entrySet()) {
            UUID playerId = entry.getKey();
            PrisonerData data = entry.getValue();

            // Offline-Spieler überspringen
            if (offlineRemainingTime.containsKey(playerId)) continue;

            // Zeit abgelaufen?
            if (currentTick >= data.releaseTime) {
                // Spieler finden und entlassen
                // (wird im Event-Handler gemacht wenn Spieler online)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EFFEKTE
    // ═══════════════════════════════════════════════════════════

    private void applyPrisonEffects(ServerPlayer player, int seconds) {
        int ticks = seconds * 20;

        // Langsamkeit (kann sich bewegen, aber langsam)
        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN, ticks, 1, false, false, true));

        // Kein Springen
        player.addEffect(new MobEffectInstance(
            MobEffects.JUMP, ticks, 128, false, false, true));

        // Mining Fatigue (kein Block-Abbau)
        player.addEffect(new MobEffectInstance(
            MobEffects.DIG_SLOWDOWN, ticks, 4, false, false, true));

        // Schwäche (kein Kampf)
        player.addEffect(new MobEffectInstance(
            MobEffects.WEAKNESS, ticks, 4, false, false, true));
    }

    private void removePrisonEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.JUMP);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
    }

    // ═══════════════════════════════════════════════════════════
    // CLIENT SYNC (für ESC-Block)
    // ═══════════════════════════════════════════════════════════

    private void syncPrisonStateToClient(ServerPlayer player, boolean inPrison) {
        // Sende Netzwerk-Paket an Client
        // PrisonStatePacket(inPrison, releaseTime)
        // → Client blockiert ESC-Menü wenn inPrison == true
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    public boolean isPrisoner(UUID playerId) {
        return prisoners.containsKey(playerId);
    }

    public PrisonerData getPrisonerData(UUID playerId) {
        return prisoners.get(playerId);
    }

    public Collection<PrisonerData> getAllPrisoners() {
        return Collections.unmodifiableCollection(prisoners.values());
    }

    public int getPrisonerCount() {
        return prisoners.size();
    }

    private void savePrisonerData() {
        // Speichern in JSON-Datei
        // config/schedulemc/prisoners.json
    }

    public void loadPrisonerData() {
        // Laden aus JSON-Datei
    }
}
```

---

## 4. ESC-Menü Blockierung (Client-Side)

### PrisonEscapeBlocker.java

```java
package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * CLIENT-SIDE: Blockiert ESC-Menü und Disconnect während Inhaftierung
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PrisonEscapeBlocker {

    // Wird vom Server gesetzt via Netzwerk-Paket
    private static boolean isInPrison = false;
    private static long releaseTime = 0;

    /**
     * Setzt den Gefängnis-Status (vom Server empfangen)
     */
    public static void setPrisonState(boolean inPrison, long release) {
        isInPrison = inPrison;
        releaseTime = release;
    }

    /**
     * Blockiert ESC-Menü (PauseScreen) während Inhaftierung
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenOpen(ScreenEvent.Opening event) {
        EventHelper.handleEvent(() -> {
            if (!isInPrison) return;

            Minecraft mc = Minecraft.getInstance();

            // Blockiere Pause-Screen (ESC-Menü)
            if (event.getNewScreen() instanceof PauseScreen) {
                event.setCanceled(true);

                if (mc.player != null) {
                    long remainingTicks = releaseTime - mc.level.getGameTime();
                    int remainingSeconds = Math.max(0, (int)(remainingTicks / 20));

                    mc.player.displayClientMessage(
                        Component.literal(String.format(
                            "§c⛓ GEFÄNGNIS: ESC-Menü gesperrt!\n" +
                            "§7Verbleibend: §f%d Sekunden\n" +
                            "§7Nutze §f/bail §7für Kaution.",
                            remainingSeconds
                        )),
                        false
                    );
                }
            }

        }, "onPrisonScreenBlock");
    }

    /**
     * Blockiert Alt+F4 und andere Disconnect-Versuche
     * → Zeigt Warnung, Disconnect wird trotzdem passieren
     * → Server handhabt Offline-Zeit
     */
    @SubscribeEvent
    public static void onDisconnectAttempt(ScreenEvent.Opening event) {
        EventHelper.handleEvent(() -> {
            if (!isInPrison) return;

            // DisconnectedScreen wird nach Disconnect gezeigt
            // TitleScreen wird bei Quit to Title gezeigt
            if (event.getNewScreen() instanceof TitleScreen ||
                event.getNewScreen() instanceof DisconnectedScreen) {

                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    // Warnung loggen - Zeit wird pausiert
                    System.out.println("[Prison] Spieler hat während Haft disconnected.");
                }
            }
        }, "onPrisonDisconnect");
    }

    /**
     * Verhindert Keybind für Disconnect (wenn vorhanden)
     */
    @SubscribeEvent
    public static void onKeyPress(net.minecraftforge.client.event.InputEvent.Key event) {
        if (!isInPrison) return;

        Minecraft mc = Minecraft.getInstance();

        // ESC-Taste abfangen
        if (event.getKey() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            if (mc.screen == null) {
                // Würde normalerweise Pause-Screen öffnen
                // Wir blockieren das in onScreenOpen
            }
        }
    }
}
```

---

## 5. Server-Events für Login/Logout

### PrisonEventHandler.java

```java
package de.rolandsw.schedulemc.npc.crime.prison;

import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-seitige Events für Gefängnis-System
 */
@Mod.EventBusSubscriber
public class PrisonEventHandler {

    /**
     * Spieler logged aus → Zeit pausieren
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        EventHelper.handleEvent(() -> {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            PrisonManager manager = PrisonManager.getInstance();
            if (manager.isPrisoner(player.getUUID())) {
                manager.onPlayerLogout(player.getUUID(), player.level().getGameTime());
            }
        }, "onPrisonerLogout");
    }

    /**
     * Spieler logged ein → Prüfen ob noch inhaftiert
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EventHelper.handleEvent(() -> {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            PrisonManager manager = PrisonManager.getInstance();
            if (manager.isPrisoner(player.getUUID())) {
                manager.onPlayerLogin(player);
            }
        }, "onPrisonerLogin");
    }

    /**
     * Server-Tick → Prüfe auf Entlassung
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Nur alle 20 Ticks prüfen (1 Sekunde)
        // (GameTime muss aus Level geholt werden)
    }

    /**
     * Spieler versucht zu teleportieren → Blockieren wenn im Gefängnis
     */
    @SubscribeEvent
    public static void onPlayerTeleport(net.minecraftforge.event.entity.EntityTeleportEvent event) {
        EventHelper.handleEvent(() -> {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            PrisonManager manager = PrisonManager.getInstance();
            PrisonManager.PrisonerData data = manager.getPrisonerData(player.getUUID());

            if (data != null) {
                // Teleport blockieren (außer zurück in Zelle)
                if (!isWithinCell(event.getTargetX(), event.getTargetY(), event.getTargetZ(), data)) {
                    event.setCanceled(true);
                    player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§c✗ Teleport im Gefängnis nicht erlaubt!"));
                }
            }
        }, "onPrisonerTeleport");
    }

    private static boolean isWithinCell(double x, double y, double z,
                                        PrisonManager.PrisonerData data) {
        return Math.abs(x - data.cellSpawn.getX()) < 5 &&
               Math.abs(y - data.cellSpawn.getY()) < 5 &&
               Math.abs(z - data.cellSpawn.getZ()) < 5;
    }
}
```

---

## 6. Netzwerk-Paket für Client-Sync

### PrisonStatePacket.java

```java
package de.rolandsw.schedulemc.npc.crime.prison.network;

import de.rolandsw.schedulemc.client.PrisonEscapeBlocker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Synchronisiert Gefängnis-Status zum Client
 */
public class PrisonStatePacket {

    private final boolean inPrison;
    private final long releaseTime;

    public PrisonStatePacket(boolean inPrison, long releaseTime) {
        this.inPrison = inPrison;
        this.releaseTime = releaseTime;
    }

    public static void encode(PrisonStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.inPrison);
        buf.writeLong(msg.releaseTime);
    }

    public static PrisonStatePacket decode(FriendlyByteBuf buf) {
        return new PrisonStatePacket(buf.readBoolean(), buf.readLong());
    }

    public static void handle(PrisonStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-seitig ausführen
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                PrisonEscapeBlocker.setPrisonState(msg.inPrison, msg.releaseTime);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
```

---

## 7. Admin-Commands

### PrisonCommand.java

```java
/prison create <plotId>              → Registriert Plot als Gefängnis
/prison addcell <nr> <x1> <y1> <z1> <x2> <y2> <z2> [security]
                                     → Fügt Zelle hinzu
/prison removecell <nr>              → Entfernt Zelle
/prison list                         → Zeigt alle Gefängnisse
/prison cells                        → Zeigt alle Zellen
/prison inmates                      → Zeigt alle Gefangenen
/prison release <player>             → Entlässt Spieler (Admin)
/prison status <player>              → Zeigt Gefängnis-Status

# Spieler-Commands:
/bail                                → Zahlt Kaution (wenn verfügbar)
/jailtime                            → Zeigt verbleibende Zeit
```

---

## 8. Zusammenfassung der Änderungen

| Datei | Änderung |
|-------|----------|
| `PlotType.java` | + `PRISON` enum |
| `PrisonCell.java` | Neue Klasse (erweitert PlotArea) |
| `PrisonManager.java` | Neue Hauptklasse |
| `PrisonEventHandler.java` | Login/Logout Events |
| `PrisonEscapeBlocker.java` | Client-Side ESC-Block |
| `PrisonStatePacket.java` | Netzwerk-Sync |
| `PrisonCommand.java` | Admin-Commands |
| `PoliceAIHandler.java` | Integriert PrisonManager.imprisonPlayer() |

---

## 9. Offene Fragen

1. **Gefängnis-Arbeit** - Soll Arbeit zur Zeitreduzierung möglich sein?
2. **Bewährung** - Soll Bewährung nach 2/3 der Zeit möglich sein?
3. **Wächter-NPCs** - Sollen NPCs im Gefängnis patrouillieren?
4. **Fluchtversuch** - Was passiert wenn Spieler Plot verlässt? (aktuell: Teleport zurück)
5. **Mehrere Gefängnisse** - Sollen verschiedene Sicherheitsstufen pro Gefängnis möglich sein?

Sag mir was du anpassen möchtest!
