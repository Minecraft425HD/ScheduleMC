package de.rolandsw.schedulemc.npc.life.witness;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.behavior.BehaviorState;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.npc.life.social.RumorNetwork;
import de.rolandsw.schedulemc.npc.life.social.RumorType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WitnessManager - Verwaltet das Zeugensystem
 *
 * Funktionen:
 * - Verbrechen erkennen und Zeugen identifizieren
 * - Zeugenberichte erstellen und verwalten
 * - Meldungen an die Polizei koordinieren
 * - Fahndungslisten verwalten
 */
public class WitnessManager {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON-LIKE PER LEVEL
    // ═══════════════════════════════════════════════════════════

    private static final Map<ServerLevel, WitnessManager> MANAGERS = new HashMap<>();

    public static WitnessManager getManager(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, l -> new WitnessManager());
    }

    public static void removeManager(ServerLevel level) {
        MANAGERS.remove(level);
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
    private final Map<UUID, List<WitnessReport>> reportsByCriminal = new HashMap<>();

    /** Gesuchte Spieler mit aktivem Haftbefehl */
    private final Set<UUID> wantedPlayers = new HashSet<>();

    /** Kopfgelder: Spieler UUID -> Betrag */
    private final Map<UUID, Integer> bounties = new HashMap<>();

    /** Tick-Counter für periodische Prüfungen */
    private int tickCounter = 0;

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
    }

    /**
     * Entfernt einen Spieler von der Fahndungsliste
     */
    public void removeFromWantedList(UUID playerUUID) {
        wantedPlayers.remove(playerUUID);
        bounties.remove(playerUUID);
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

                    if (Math.random() < reportChance) {
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
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // Reports speichern
        ListTag reportsList = new ListTag();
        for (List<WitnessReport> reports : reportsByCriminal.values()) {
            for (WitnessReport report : reports) {
                reportsList.add(report.save());
            }
        }
        tag.put("Reports", reportsList);

        // Wanted speichern
        ListTag wantedList = new ListTag();
        for (UUID uuid : wantedPlayers) {
            CompoundTag wantedTag = new CompoundTag();
            wantedTag.putUUID("UUID", uuid);
            wantedTag.putInt("Bounty", bounties.getOrDefault(uuid, 0));
            wantedList.add(wantedTag);
        }
        tag.put("Wanted", wantedList);

        return tag;
    }

    public void load(CompoundTag tag) {
        reportsByCriminal.clear();
        wantedPlayers.clear();
        bounties.clear();

        // Reports laden
        ListTag reportsList = tag.getList("Reports", Tag.TAG_COMPOUND);
        for (int i = 0; i < reportsList.size(); i++) {
            WitnessReport report = WitnessReport.load(reportsList.getCompound(i));
            reportsByCriminal.computeIfAbsent(report.getCriminalUUID(), k -> new ArrayList<>())
                .add(report);
        }

        // Wanted laden
        ListTag wantedList = tag.getList("Wanted", Tag.TAG_COMPOUND);
        for (int i = 0; i < wantedList.size(); i++) {
            CompoundTag wantedTag = wantedList.getCompound(i);
            UUID uuid = wantedTag.getUUID("UUID");
            wantedPlayers.add(uuid);
            bounties.put(uuid, wantedTag.getInt("Bounty"));
        }
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
