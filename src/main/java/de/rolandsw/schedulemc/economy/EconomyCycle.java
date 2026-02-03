package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.GsonHelper;
import de.rolandsw.schedulemc.util.IncrementalSaveManager;
import de.rolandsw.schedulemc.util.PersistenceHelper;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Type;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Verwaltet den Wirtschaftszyklus.
 *
 * Der Zyklus durchläuft automatisch 6 Phasen:
 * NORMAL → BOOM → ÜBERHITZUNG → REZESSION → DEPRESSION → ERHOLUNG → NORMAL
 *
 * Jede Phase dauert eine bestimmte Anzahl MC-Tage und beeinflusst
 * alle Preise, Gehälter und Event-Wahrscheinlichkeiten.
 *
 * Der Zyklus kann durch externe Faktoren beeinflusst werden:
 * - Hohe Inflation beschleunigt den Übergang zu Überhitzung
 * - Hohe Arbeitslosigkeit beschleunigt Depression
 * - Razzien können Rezession auslösen
 */
public class EconomyCycle implements IncrementalSaveManager.ISaveable {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Singleton
    private static volatile EconomyCycle instance;

    // Persistenz
    private static volatile File file = new File("config/schedulemc_economy_cycle.json");
    private static final Gson gson = GsonHelper.get();
    private static volatile boolean needsSave = false;

    // ═══════════════════════════════════════════════════════════
    // ZYKLUS-DATEN
    // ═══════════════════════════════════════════════════════════

    /** Aktuelle Phase */
    private volatile EconomyCyclePhase currentPhase = EconomyCyclePhase.NORMAL;

    /** Verbleibende Tage in der aktuellen Phase */
    private volatile int remainingDays;

    /** Gesamte vergangene Tage seit Server-Start */
    private volatile int totalDaysElapsed = 0;

    /** Anzahl abgeschlossener Zyklen */
    private volatile int completedCycles = 0;

    /** Aktueller interpolierter Multiplikator (smooth transition) */
    private volatile double currentMultiplier = 1.0;

    /** Multiplikator der vorherigen Phase (für Interpolation) */
    private volatile double previousMultiplier = 1.0;

    /** Tage die diese Phase insgesamt dauert (für Interpolation) */
    private volatile int currentPhaseTotalDays;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private EconomyCycle() {
        this.remainingDays = rollDuration(EconomyCyclePhase.NORMAL);
        this.currentPhaseTotalDays = remainingDays;
    }

    public static EconomyCycle getInstance() {
        EconomyCycle localRef = instance;
        if (localRef == null) {
            synchronized (EconomyCycle.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new EconomyCycle();
                }
            }
        }
        return localRef;
    }

    // ═══════════════════════════════════════════════════════════
    // TÄGLICHES UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Minecraft-Tag aufgerufen.
     * Prüft ob die aktuelle Phase endet und wechselt ggf. zur nächsten.
     */
    public void onNewDay() {
        totalDaysElapsed++;
        remainingDays--;

        // Multiplikator interpolieren für sanften Übergang
        updateInterpolatedMultiplier();

        // Phase-Wechsel prüfen
        if (remainingDays <= 0) {
            transitionToNextPhase();
        }

        // Multiplikator an EconomyController weitergeben
        EconomyController.getInstance().setCycleMultiplier(currentMultiplier);

        // Event-Check basierend auf aktueller Phase
        checkPhaseEvents();

        needsSave = true;

        LOGGER.info("Economy Day {}: Phase={}, Remaining={} days, Multiplier={:.2f}",
                totalDaysElapsed, currentPhase.getDisplayName(), remainingDays, currentMultiplier);
    }

    /**
     * Wechselt zur nächsten Phase im Zyklus.
     */
    private void transitionToNextPhase() {
        EconomyCyclePhase oldPhase = currentPhase;
        previousMultiplier = oldPhase.getSellPriceMultiplier();
        currentPhase = oldPhase.getNextPhase();
        remainingDays = rollDuration(currentPhase);
        currentPhaseTotalDays = remainingDays;

        // Prüfe ob ein voller Zyklus abgeschlossen wurde
        if (currentPhase == EconomyCyclePhase.NORMAL && oldPhase == EconomyCyclePhase.ERHOLUNG) {
            completedCycles++;
            LOGGER.info("Full economic cycle #{} completed!", completedCycles);
        }

        LOGGER.info("Economy phase transition: {} → {} (duration: {} days)",
                oldPhase.getDisplayName(), currentPhase.getDisplayName(), remainingDays);
    }

    /**
     * Interpoliert den Multiplikator für sanften Phasenübergang.
     * Verhindert abrupte Preissprünge.
     */
    private void updateInterpolatedMultiplier() {
        double targetMultiplier = currentPhase.getSellPriceMultiplier();

        if (currentPhaseTotalDays <= 0) {
            currentMultiplier = targetMultiplier;
            return;
        }

        // Progress: 0.0 am Anfang der Phase, 1.0 am Ende
        double progress = 1.0 - ((double) remainingDays / currentPhaseTotalDays);

        // Smooth interpolation (easeInOut)
        double smoothProgress = smoothStep(progress);

        // Interpoliere zwischen vorherigem und aktuellem Multiplikator
        currentMultiplier = previousMultiplier + (targetMultiplier - previousMultiplier) * smoothProgress;
    }

    /**
     * Smooth-Step Funktion für sanfte Interpolation.
     */
    private static double smoothStep(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3.0 - 2.0 * t);
    }

    /**
     * Prüft und triggert Phase-spezifische Events.
     */
    private void checkPhaseEvents() {
        double chance = currentPhase.getEventChance();
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            PriceManager.checkDailyEvents();
        }
    }

    /**
     * Würfelt die Dauer einer Phase.
     */
    private static int rollDuration(EconomyCyclePhase phase) {
        return ThreadLocalRandom.current().nextInt(
                phase.getMinDurationDays(),
                phase.getMaxDurationDays() + 1
        );
    }

    // ═══════════════════════════════════════════════════════════
    // EXTERNE EINFLÜSSE
    // ═══════════════════════════════════════════════════════════

    /**
     * Beschleunigt den Phasenwechsel (z.B. durch hohe Inflation).
     * Reduziert die verbleibenden Tage.
     *
     * @param days Anzahl Tage die abgezogen werden
     */
    public void accelerateTransition(int days) {
        remainingDays = Math.max(1, remainingDays - days);
        needsSave = true;
        LOGGER.info("Phase transition accelerated by {} days. Remaining: {}", days, remainingDays);
    }

    /**
     * Erzwingt eine bestimmte Phase (Admin-Befehl).
     *
     * @param phase    Gewünschte Phase
     * @param duration Dauer in MC-Tagen (0 = zufällig)
     */
    public void forcePhase(EconomyCyclePhase phase, int duration) {
        previousMultiplier = currentMultiplier;
        currentPhase = phase;
        remainingDays = duration > 0 ? duration : rollDuration(phase);
        currentPhaseTotalDays = remainingDays;
        needsSave = true;

        // Sofort an Controller melden
        EconomyController.getInstance().setCycleMultiplier(phase.getSellPriceMultiplier());

        LOGGER.info("Phase forced to: {} (duration: {} days)", phase.getDisplayName(), remainingDays);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public EconomyCyclePhase getCurrentPhase() {
        return currentPhase;
    }

    public int getRemainingDays() {
        return remainingDays;
    }

    public int getTotalDaysElapsed() {
        return totalDaysElapsed;
    }

    public int getCompletedCycles() {
        return completedCycles;
    }

    public double getCurrentMultiplier() {
        return currentMultiplier;
    }

    /**
     * Gibt formatierte Zyklus-Info für UI zurück.
     */
    public String getCycleInfo() {
        return String.format(
                "§6═══ Wirtschaftszyklus ═══\n" +
                "§7Phase: %s\n" +
                "§7Verbleibend: §f%d Tage\n" +
                "§7Preis-Multiplikator: §f%.2fx\n" +
                "§7Gehalt-Multiplikator: §f%.2fx\n" +
                "§7Nächste Phase: §f%s\n" +
                "§7Tag: §f%d §7(Zyklus: §f#%d§7)",
                currentPhase.getFormattedName(),
                remainingDays,
                currentMultiplier,
                currentPhase.getSalaryMultiplier(),
                currentPhase.getNextPhase().getFormattedName(),
                totalDaysElapsed,
                completedCycles + 1
        );
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENZ
    // ═══════════════════════════════════════════════════════════

    private static final Type CYCLE_DATA_TYPE = new TypeToken<CycleData>(){}.getType();

    public void loadData() {
        PersistenceHelper.LoadResult<CycleData> result =
                PersistenceHelper.load(file, gson, CYCLE_DATA_TYPE, "EconomyCycle");

        if (result.isSuccess() && result.hasData()) {
            CycleData data = result.getData();
            try {
                this.currentPhase = EconomyCyclePhase.valueOf(data.currentPhase);
            } catch (IllegalArgumentException e) {
                this.currentPhase = EconomyCyclePhase.NORMAL;
            }
            this.remainingDays = data.remainingDays;
            this.totalDaysElapsed = data.totalDaysElapsed;
            this.completedCycles = data.completedCycles;
            this.currentMultiplier = data.currentMultiplier;
            this.previousMultiplier = data.previousMultiplier;
            this.currentPhaseTotalDays = data.currentPhaseTotalDays;

            LOGGER.info("EconomyCycle loaded: phase={}, remaining={}, day={}",
                    currentPhase.getDisplayName(), remainingDays, totalDaysElapsed);
        }
    }

    public void saveData() {
        CycleData data = new CycleData();
        data.currentPhase = currentPhase.name();
        data.remainingDays = remainingDays;
        data.totalDaysElapsed = totalDaysElapsed;
        data.completedCycles = completedCycles;
        data.currentMultiplier = currentMultiplier;
        data.previousMultiplier = previousMultiplier;
        data.currentPhaseTotalDays = currentPhaseTotalDays;

        PersistenceHelper.SaveResult saveResult = PersistenceHelper.save(file, gson, data, "EconomyCycle");
        if (saveResult.isSuccess()) {
            needsSave = false;
        }
    }

    private static class CycleData {
        String currentPhase;
        int remainingDays;
        int totalDaysElapsed;
        int completedCycles;
        double currentMultiplier;
        double previousMultiplier;
        int currentPhaseTotalDays;
    }

    // ═══════════════════════════════════════════════════════════
    // INCREMENTAL SAVE MANAGER
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean isDirty() {
        return needsSave;
    }

    @Override
    public void save() {
        saveData();
    }

    @Override
    public String getName() {
        return "EconomyCycle";
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
