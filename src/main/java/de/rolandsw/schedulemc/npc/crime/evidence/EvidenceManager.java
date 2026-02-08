package de.rolandsw.schedulemc.npc.crime.evidence;

import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature 10: Beweisketten-System Manager
 *
 * Verwaltet Beweise fuer alle Verbrechen.
 * - Sammelt Beweise automatisch bei Crime-Events
 * - Berechnet Beweisstaerke fuer Strafbemessung
 * - Persistiert Beweise via JSON
 *
 * Beweisstaerke beeinflusst:
 * - Strafmultiplikator (0.5x - 2.0x)
 * - Kautions-Hoehe
 * - Wahrscheinlichkeit einer Verurteilung
 */
public class EvidenceManager extends AbstractPersistenceManager<EvidenceManager.EvidenceData> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile EvidenceManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    /** Maximale Beweise pro Spieler */
    private static final int MAX_EVIDENCE_PER_PLAYER = 200;

    /** Beweise nach Straftaeter gruppiert */
    private final Map<UUID, List<Evidence>> evidenceByCriminal = new ConcurrentHashMap<>();

    private EvidenceManager(MinecraftServer server) {
        super(
            new File(server.getServerDirectory(), "config/schedulemc/evidence.json"),
            GsonHelper.get()
        );
        load();
    }

    public static EvidenceManager getInstance(MinecraftServer server) {
        EvidenceManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new EvidenceManager(server);
                }
            }
        }
        return result;
    }

    @Nullable
    public static EvidenceManager getInstance() {
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // EVIDENCE COLLECTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Fuegt einen Beweis hinzu
     *
     * @param criminalUUID UUID des Straftaeters
     * @param type Art des Beweises
     * @param location Ort des Beweises
     * @param reliability Zuverlaessigkeit (0.0 - 1.0)
     * @param description Beschreibung
     * @param witnessUUID Optional: UUID des Zeugen-NPC
     */
    public void addEvidence(UUID criminalUUID, Evidence.EvidenceType type,
                            @Nullable BlockPos location, float reliability,
                            String description, @Nullable UUID witnessUUID) {
        Evidence evidence = new Evidence(type, location, reliability, description, witnessUUID);

        List<Evidence> evidenceList = evidenceByCriminal.computeIfAbsent(
            criminalUUID, k -> new ArrayList<>()
        );

        // Limit einhalten
        while (evidenceList.size() >= MAX_EVIDENCE_PER_PLAYER) {
            evidenceList.remove(0);
        }

        evidenceList.add(evidence);
        markDirty();

        LOGGER.debug("Evidence added for {}: {} (reliability: {})",
            criminalUUID, type, reliability);
    }

    /**
     * Erstellt automatisch Zeugen-Beweis
     */
    public void addWitnessTestimony(UUID criminalUUID, UUID witnessNPCUUID,
                                     BlockPos crimeLocation, float witnessCredibility,
                                     String crimeDescription) {
        addEvidence(criminalUUID, Evidence.EvidenceType.WITNESS_TESTIMONY,
            crimeLocation, witnessCredibility / 100.0f,
            "Zeugenaussage: " + crimeDescription, witnessNPCUUID);
    }

    /**
     * Erstellt "Auf frischer Tat ertappt" Beweis (z.B. bei Polizei-Verfolgung)
     */
    public void addCaughtInAct(UUID criminalUUID, BlockPos location, String description) {
        addEvidence(criminalUUID, Evidence.EvidenceType.CAUGHT_IN_ACT,
            location, 1.0f, description, null);
    }

    /**
     * Erstellt Polizei-Bericht Beweis (z.B. bei Raid)
     */
    public void addPoliceReport(UUID criminalUUID, BlockPos location, String description) {
        addEvidence(criminalUUID, Evidence.EvidenceType.POLICE_REPORT,
            location, 0.85f, description, null);
    }

    // ═══════════════════════════════════════════════════════════
    // EVIDENCE ANALYSIS
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet die Gesamtbeweisstaerke fuer einen Spieler (0.0 - 1.0)
     *
     * Formel: Max-Value aus allen Beweisen, mit Bonus fuer Anzahl
     * - 1 Beweis: max_evidence_value
     * - 2+ Beweise: max_evidence_value + 0.1 pro zusaetzlichem Beweis (cap 1.0)
     */
    public float calculateEvidenceStrength(UUID criminalUUID) {
        List<Evidence> evidenceList = evidenceByCriminal.get(criminalUUID);
        if (evidenceList == null || evidenceList.isEmpty()) {
            return 0.0f;
        }

        float maxValue = 0.0f;
        for (Evidence e : evidenceList) {
            float value = e.getEvidenceValue();
            if (value > maxValue) maxValue = value;
        }

        // Bonus fuer mehrere Beweise
        int bonusCount = Math.min(evidenceList.size() - 1, 4); // Max +0.4 Bonus
        float bonus = bonusCount * 0.1f;

        return Math.min(1.0f, maxValue + bonus);
    }

    /**
     * Berechnet den Strafmultiplikator basierend auf Beweisstaerke
     *
     * @return Multiplikator (0.5 - 2.0)
     */
    public float calculateSentenceMultiplier(UUID criminalUUID) {
        float strength = calculateEvidenceStrength(criminalUUID);

        // 0.0 Beweisstaerke -> 0.5x Strafe (Mangel an Beweisen)
        // 0.5 Beweisstaerke -> 1.0x Strafe (normal)
        // 1.0 Beweisstaerke -> 2.0x Strafe (erdrückende Beweislage)
        return 0.5f + (strength * 1.5f);
    }

    /**
     * Gibt alle Beweise fuer einen Spieler zurueck
     */
    public List<Evidence> getEvidence(UUID criminalUUID) {
        return evidenceByCriminal.getOrDefault(criminalUUID, Collections.emptyList());
    }

    /**
     * Gibt die Anzahl Beweise fuer einen Spieler zurueck
     */
    public int getEvidenceCount(UUID criminalUUID) {
        List<Evidence> list = evidenceByCriminal.get(criminalUUID);
        return list != null ? list.size() : 0;
    }

    /**
     * Loescht alle Beweise fuer einen Spieler (z.B. nach Freispruch)
     */
    public void clearEvidence(UUID criminalUUID) {
        if (evidenceByCriminal.remove(criminalUUID) != null) {
            markDirty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<EvidenceData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(EvidenceData data) {
        evidenceByCriminal.clear();
        if (data != null && data.evidence != null) {
            for (Map.Entry<UUID, List<Evidence>> entry : data.evidence.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    evidenceByCriminal.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
            LOGGER.info("Loaded evidence for {} criminals", evidenceByCriminal.size());
        }
    }

    @Override
    protected EvidenceData getCurrentData() {
        EvidenceData data = new EvidenceData();
        data.evidence = new HashMap<>(evidenceByCriminal);
        return data;
    }

    @Override
    protected String getComponentName() {
        return "EvidenceManager";
    }

    @Override
    protected String getHealthDetails() {
        int total = 0;
        for (List<Evidence> list : evidenceByCriminal.values()) total += list.size();
        return String.format("%d Taeter, %d Beweise", evidenceByCriminal.size(), total);
    }

    @Override
    protected void onCriticalLoadFailure() {
        evidenceByCriminal.clear();
    }

    public static class EvidenceData {
        public Map<UUID, List<Evidence>> evidence;
    }
}
