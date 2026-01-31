package de.rolandsw.schedulemc.npc.life.witness;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.behavior.BehaviorState;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.npc.life.social.RumorNetwork;
import de.rolandsw.schedulemc.npc.life.social.RumorType;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * WitnessManager - Verwaltet das Zeugensystem mit JSON-Persistenz
 *
 * Funktionen:
 * - Verbrechen erkennen und Zeugen identifizieren
 * - Zeugenberichte erstellen und verwalten
 * - Meldungen an die Polizei koordinieren
 * - Fahndungslisten verwalten
 */
public class WitnessManager extends AbstractPersistenceManager<WitnessManager.WitnessData> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile WitnessManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static WitnessManager getInstance() {
        return instance;
    }

    public static WitnessManager getInstance(MinecraftServer server) {
        WitnessManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new WitnessManager(server);
                }
            }
        }
        return result;
    }

    /**
     * Gets manager instance for a specific level (convenience method).
     * Note: Manager is server-wide, not per-level.
     */
    public static WitnessManager getManager(ServerLevel level) {
        return getInstance(level.getServer());
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Sichtweite für Zeugen (in Blöcken) */
    public static final double WITNESS_RANGE = 20.0;

    /** Maximale Anzahl an Berichten pro Spieler */
    public static final int MAX_REPORTS_PER_PLAYER = 50;

    /** Wie oft wird geprüft ob Berichte gemeldet werden sollen (Ticks) */
    public static final int REPORT_CHECK_INTERVAL = 200; // 10 Sekunden

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Alle Zeugenberichte, nach Täter-UUID gruppiert */
    private final Map<UUID, List<WitnessReport>> reportsByCriminal = new ConcurrentHashMap<>();

    /** Gesuchte Spieler mit aktivem Haftbefehl */
    private final Set<UUID> wantedPlayers = ConcurrentHashMap.newKeySet();

    /** Kopfgelder: Spieler UUID -> Betrag */
    private final Map<UUID, Integer> bounties = new ConcurrentHashMap<>();

    /** Tick-Counter für periodische Prüfungen */
    private int tickCounter = 0;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private WitnessManager(MinecraftServer server) {
        super(
            new File(server.getServerDirectory(), "config/npc_life_witness.json"),
            GsonHelper.get()
        );
        load();
    }

    // ═══════════════════════════════════════════════════════════
    // CRIME DETECTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert ein Verbrechen und findet Zeugen
     *
     * @param criminal Der Spieler der das Verbrechen begangen hat
     * @param crimeType Art des Verbrechens
     * @param location Ort des Verbrechens
     * @param level Die ServerLevel
     * @param victim Optional: Opfer des Verbrechens
     * @return Liste der erstellten Zeugenberichte
     */
    public List<WitnessReport> registerCrime(ServerPlayer criminal, CrimeType crimeType,
                                             BlockPos location, ServerLevel level,
                                             @Nullable UUID victim) {

        long currentTime = level.getGameTime();
        long currentDay = currentTime / 24000;

        List<WitnessReport> newReports = new ArrayList<>();

        // Alle NPCs in Sichtweite finden
        AABB searchBox = new AABB(location).inflate(WITNESS_RANGE);
        List<CustomNPCEntity> potentialWitnesses = level.getEntitiesOfClass(
            CustomNPCEntity.class,
            searchBox,
            npc -> canWitnessCrime(npc, location)
        );

        for (CustomNPCEntity witness : potentialWitnesses) {
            // Zeugenbericht erstellen
            WitnessReport report = new WitnessReport(
                criminal.getUUID(),
                witness.getNpcData().getNpcUUID(),
                crimeType,
                location,
                currentTime,
                currentDay
            );

            if (victim != null) {
                report.setVictimUUID(victim);
            }

            // Glaubwürdigkeit basierend auf NPC-Traits
            NPCLifeData lifeData = witness.getLifeData();
            if (lifeData != null) {
                float credibility = 70.0f + lifeData.getTraits().getHonesty() * 0.3f;
                report.setWitnessCredibility(credibility);
            }

            // Report speichern
            addReport(report);
            newReports.add(report);

            // NPC reagiert auf das Verbrechen
            reactToCrime(witness, criminal, crimeType);
        }

        // Gerücht erstellen
        RumorNetwork network = RumorNetwork.getNetwork(level);
        network.createRumor(
            criminal.getUUID(),
            crimeType.getAssociatedRumor(),
            crimeType.getDisplayName() + " bei " + location.toShortString(),
            currentDay,
            potentialWitnesses.isEmpty() ? null : potentialWitnesses.get(0).getNpcData().getNpcUUID()
        );

        // Fraktions-Reputation anpassen
        FactionManager.getManager(level).onCrimeCommitted(
            criminal.getUUID(),
            crimeType.name(),
            crimeType.getSeverity()
        );

        // Bei schweren Verbrechen: Sofort auf Fahndungsliste
        if (crimeType.getSeverity() >= 7 && !newReports.isEmpty()) {
            addToWantedList(criminal.getUUID(), crimeType);
        }

        return newReports;
    }

    /**
     * Prüft ob ein NPC ein Verbrechen beobachten kann
     */
    private boolean canWitnessCrime(CustomNPCEntity npc, BlockPos crimeLocation) {
        // NPC muss wach sein
        if (npc.getBehaviorState() == BehaviorState.SLEEPING) {
            return false;
        }

        // Sichtlinie prüfen (vereinfacht - nur Distanz)
        double distance = npc.blockPosition().distSqr(crimeLocation);
        if (distance > WITNESS_RANGE * WITNESS_RANGE) {
            return false;
        }

        // Life-System muss aktiv sein
        return npc.getLifeData() != null && npc.isLifeSystemEnabled();
    }

    /**
     * NPC-Reaktion auf beobachtetes Verbrechen
     */
    private void reactToCrime(CustomNPCEntity witness, ServerPlayer criminal, CrimeType crimeType) {
        NPCLifeData lifeData = witness.getLifeData();
        if (lifeData == null) return;

        // Erinnerung speichern
        lifeData.getMemory().addMemory(
            criminal.getUUID(),
            MemoryType.CRIME_WITNESSED,
            "Zeuge: " + crimeType.getDisplayName(),
            crimeType.getSeverity() + 2
        );

        // Tags hinzufügen
        lifeData.getMemory().addPlayerTag(criminal.getUUID(), "Kriminell");
        if (crimeType.isViolent()) {
            lifeData.getMemory().addPlayerTag(criminal.getUUID(), "Gefährlich");
        }

        // Emotion auslösen
        float emotionIntensity = crimeType.getSeverity() * 10.0f;
        if (crimeType.isViolent()) {
            lifeData.getEmotions().trigger(EmotionState.FEARFUL, emotionIntensity);
        } else {
            lifeData.getEmotions().trigger(EmotionState.SUSPICIOUS, emotionIntensity * 0.7f);
        }

        // Sicherheit reduzieren
        lifeData.getNeeds().modifySafety(-crimeType.getSafetyImpact());

        // Behavior Engine informieren
        if (witness.getBehaviorEngine() != null) {
            witness.getBehaviorEngine().onWitnessCrime(
                criminal,
                crimeType.name(),
                crimeType.getSeverity()
            );
        }
    }

    // ═══════════════════════════════════════════════════════════
    // REPORT MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt einen Zeugenbericht hinzu
     */
    public void addReport(WitnessReport report) {
        List<WitnessReport> reports = reportsByCriminal.computeIfAbsent(
            report.getCriminalUUID(), k -> new ArrayList<>()
        );

        // Limit einhalten
        while (reports.size() >= MAX_REPORTS_PER_PLAYER) {
            reports.stream()
                .min(Comparator.comparingLong(WitnessReport::getTimestamp))
                .ifPresent(reports::remove);
        }

        reports.add(report);
        markDirty();
    }

    /**
     * Holt alle Berichte über einen Spieler
     */
    public List<WitnessReport> getReportsAbout(UUID criminalUUID) {
        return reportsByCriminal.getOrDefault(criminalUUID, Collections.emptyList());
    }

    /**
     * Holt alle nicht gemeldeten Berichte über einen Spieler
     */
    public List<WitnessReport> getUnreportedReports(UUID criminalUUID) {
        return getReportsAbout(criminalUUID).stream()
            .filter(r -> !r.isReported() && !r.isBribed())
            .collect(Collectors.toList());
    }

    /**
     * Holt den Bericht eines bestimmten Zeugen über einen Spieler
     */
    @Nullable
    public WitnessReport getReportByWitness(UUID criminalUUID, UUID witnessNPCUUID) {
        return getReportsAbout(criminalUUID).stream()
            .filter(r -> r.getWitnessNPCUUID().equals(witnessNPCUUID))
            .filter(r -> !r.isReported() && !r.isBribed())
            .findFirst()
            .orElse(null);
    }

    // ═══════════════════════════════════════════════════════════
    // WANTED SYSTEM
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt einen Spieler zur Fahndungsliste hinzu
     */
    public void addToWantedList(UUID playerUUID, CrimeType crimeType) {
        wantedPlayers.add(playerUUID);

        // Kopfgeld erhöhen
        int currentBounty = bounties.getOrDefault(playerUUID, 0);
        bounties.put(playerUUID, currentBounty + crimeType.getBaseBounty());
        markDirty();
    }

    /**
     * Entfernt einen Spieler von der Fahndungsliste
     */
    public void removeFromWantedList(UUID playerUUID) {
        wantedPlayers.remove(playerUUID);
        bounties.remove(playerUUID);
        markDirty();
    }

    /**
     * Prüft ob ein Spieler gesucht wird
     */
    public boolean isWanted(UUID playerUUID) {
        return wantedPlayers.contains(playerUUID);
    }

    /**
     * Gibt das Kopfgeld auf einen Spieler zurück
     */
    public int getBounty(UUID playerUUID) {
        return bounties.getOrDefault(playerUUID, 0);
    }

    /**
     * Reduziert das Kopfgeld (z.B. durch Zahlung)
     */
    public void reduceBounty(UUID playerUUID, int amount) {
        int current = bounties.getOrDefault(playerUUID, 0);
        int newBounty = current - amount;

        if (newBounty <= 0) {
            bounties.remove(playerUUID);
            // Wenn kein Kopfgeld mehr, von Fahndungsliste entfernen
            wantedPlayers.remove(playerUUID);
        } else {
            bounties.put(playerUUID, newBounty);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird regelmäßig aufgerufen
     */
    public void tick(ServerLevel level) {
        tickCounter++;

        if (tickCounter >= REPORT_CHECK_INTERVAL) {
            tickCounter = 0;
            processReports(level);
        }
    }

    /**
     * Verarbeitet ausstehende Berichte
     */
    private void processReports(ServerLevel level) {
        long currentDay = level.getDayTime() / 24000;

        // Abgelaufene Berichte entfernen
        reportsByCriminal.values().forEach(reports ->
            reports.removeIf(r -> !r.isValid(currentDay))
        );

        // Leere Listen entfernen
        reportsByCriminal.entrySet().removeIf(e -> e.getValue().isEmpty());

        // Berichte die automatisch gemeldet werden sollten
        for (Map.Entry<UUID, List<WitnessReport>> entry : reportsByCriminal.entrySet()) {
            for (WitnessReport report : entry.getValue()) {
                if (!report.isReported() && !report.isBribed()) {
                    // Prüfe ob Polizei-NPC in der Nähe ist (50 Block Radius vom Zeugen)
                    BlockPos witnessPos = report.getCrimeLocation();
                    boolean policeNearby = level.getEntitiesOfClass(
                        de.rolandsw.schedulemc.npc.entity.CustomNPCEntity.class,
                        AABB.ofSize(witnessPos.getCenter(), 100, 50, 100),
                        npc -> npc.getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI
                    ).size() > 0;

                    // Meldungs-Chance: 90% wenn Polizei in der Nähe, sonst 10%
                    double reportChance = policeNearby ? 0.9 : 0.1;

                    if (ThreadLocalRandom.current().nextDouble() < reportChance) {
                        report.markAsReported();
                        addToWantedList(report.getCriminalUUID(), report.getCrimeType());

                        // Wenn Polizei in der Nähe, sofort Wanted-Level erhöhen
                        if (policeNearby) {
                            de.rolandsw.schedulemc.npc.crime.CrimeManager.addWantedLevel(
                                report.getCriminalUUID(), 1, level.getDayTime() / 24000);
                        }
                    }
                }
            }
        }
    }

    /**
     * Wird bei Tageswechsel aufgerufen
     */
    public void onDayChange(long currentDay) {
        // Berichte altern lassen
        reportsByCriminal.values().forEach(reports ->
            reports.removeIf(r -> !r.isValid(currentDay))
        );
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet die Gesamtzahl der Verbrechen eines Spielers
     */
    public int getTotalCrimeCount(UUID playerUUID) {
        return getReportsAbout(playerUUID).size();
    }

    /**
     * Prüft ob ein Spieler ein Wiederholungstäter ist
     */
    public boolean isRepeatOffender(UUID playerUUID) {
        return getTotalCrimeCount(playerUUID) >= 3;
    }

    /**
     * Gibt das schwerwiegendste ungemeldete Verbrechen zurück
     */
    @Nullable
    public WitnessReport getMostSevereCrime(UUID playerUUID) {
        return getUnreportedReports(playerUUID).stream()
            .max(Comparator.comparingInt(r -> r.getCrimeType().getSeverity()))
            .orElse(null);
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<WitnessData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(WitnessData data) {
        reportsByCriminal.clear();
        wantedPlayers.clear();
        bounties.clear();

        int invalidCount = 0;
        int correctedCount = 0;

        // Validate and load reports
        if (data.reports != null) {
            // Check collection size
            if (data.reports.size() > 10000) {
                LOGGER.warn("Reports map size ({}) exceeds limit, potential corruption",
                    data.reports.size());
                correctedCount++;
            }

            for (Map.Entry<UUID, List<WitnessReport>> entry : data.reports.entrySet()) {
                try {
                    UUID criminalUUID = entry.getKey();
                    List<WitnessReport> reports = entry.getValue();

                    // NULL CHECK
                    if (reports == null) {
                        LOGGER.warn("Null reports list for criminal {}, skipping", criminalUUID);
                        invalidCount++;
                        continue;
                    }

                    // VALIDATE LIST SIZE
                    if (reports.size() > 1000) {
                        LOGGER.warn("Criminal {} has too many reports ({}), truncating to 1000",
                            criminalUUID, reports.size());
                        reports = new ArrayList<>(reports.subList(0, 1000));
                        correctedCount++;
                    }

                    // VALIDATE REPORTS - check for null entries
                    List<WitnessReport> validReports = new ArrayList<>();
                    for (WitnessReport report : reports) {
                        if (report == null) {
                            LOGGER.warn("Criminal {} has null report, skipping", criminalUUID);
                            invalidCount++;
                            continue;
                        }
                        validReports.add(report);
                    }

                    if (validReports.size() != reports.size()) {
                        correctedCount++;
                    }

                    reportsByCriminal.put(criminalUUID, validReports);
                } catch (Exception e) {
                    LOGGER.error("Error loading reports for criminal {}", entry.getKey(), e);
                    invalidCount++;
                }
            }
        }

        // Validate and load wantedPlayers
        if (data.wantedPlayers != null) {
            // Check collection size
            if (data.wantedPlayers.size() > 10000) {
                LOGGER.warn("Wanted players set size ({}) exceeds limit, potential corruption",
                    data.wantedPlayers.size());
                correctedCount++;
            }

            for (UUID playerUUID : data.wantedPlayers) {
                try {
                    // NULL CHECK
                    if (playerUUID == null) {
                        LOGGER.warn("Null UUID in wanted players set, skipping");
                        invalidCount++;
                        continue;
                    }

                    wantedPlayers.add(playerUUID);
                } catch (Exception e) {
                    LOGGER.error("Error loading wanted player UUID", e);
                    invalidCount++;
                }
            }
        }

        // Validate and load bounties
        if (data.bounties != null) {
            // Check collection size
            if (data.bounties.size() > 10000) {
                LOGGER.warn("Bounties map size ({}) exceeds limit, potential corruption",
                    data.bounties.size());
                correctedCount++;
            }

            for (Map.Entry<UUID, Integer> entry : data.bounties.entrySet()) {
                try {
                    UUID playerUUID = entry.getKey();
                    Integer bountyAmount = entry.getValue();

                    // NULL CHECK
                    if (bountyAmount == null) {
                        LOGGER.warn("Null bounty amount for player {}, skipping", playerUUID);
                        invalidCount++;
                        continue;
                    }

                    // VALIDATE BOUNTY AMOUNT (>= 0)
                    if (bountyAmount < 0) {
                        LOGGER.warn("Player {} has negative bounty {}, resetting to 0",
                            playerUUID, bountyAmount);
                        bountyAmount = 0;
                        correctedCount++;
                    }

                    bounties.put(playerUUID, bountyAmount);
                } catch (Exception e) {
                    LOGGER.error("Error loading bounty for player {}", entry.getKey(), e);
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
    protected WitnessData getCurrentData() {
        WitnessData data = new WitnessData();
        data.reports = new HashMap<>(reportsByCriminal);
        data.wantedPlayers = new HashSet<>(wantedPlayers);
        data.bounties = new HashMap<>(bounties);
        return data;
    }

    @Override
    protected String getComponentName() {
        return "WitnessManager";
    }

    @Override
    protected String getHealthDetails() {
        int totalReports = reportsByCriminal.values().stream().mapToInt(List::size).sum();
        return String.format("%d Berichte, %d Gesuchte", totalReports, wantedPlayers.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        reportsByCriminal.clear();
        wantedPlayers.clear();
        bounties.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASSES FOR JSON SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public static class WitnessData {
        public Map<UUID, List<WitnessReport>> reports;
        public Set<UUID> wantedPlayers;
        public Map<UUID, Integer> bounties;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        int totalReports = reportsByCriminal.values().stream().mapToInt(List::size).sum();
        return String.format("WitnessManager{reports=%d, wanted=%d}",
            totalReports, wantedPlayers.size());
    }
}
