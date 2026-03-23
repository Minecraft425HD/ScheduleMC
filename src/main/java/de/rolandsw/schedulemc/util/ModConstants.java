package de.rolandsw.schedulemc.util;

/**
 * Zentrale Konstanten für ScheduleMC.
 *
 * Enthält alle geteilten Magic Numbers, die an mehreren Stellen im Mod vorkommen.
 * Lokale Konstanten, die nur in einer einzigen Klasse benötigt werden,
 * bleiben dort definiert.
 */
public final class ModConstants {

    private ModConstants() {
        // Utility-Klasse - keine Instanzen
    }

    // ═══════════════════════════════════════════════════════════
    // TICK-INTERVALLE
    // ═══════════════════════════════════════════════════════════

    /** 1 Minecraft-Minute = 1200 Ticks (60 Sekunden bei 20 TPS) */
    public static final int TICKS_PER_MINUTE = 1200;

    /**
     * Standard-Speicherintervall = 6000 Ticks (5 Minuten bei 20 TPS).
     * Wird von ScheduleMC, UtilityEventHandler und NPCLifeSystemEvents gemeinsam genutzt.
     */
    public static final int TICKS_SAVE_INTERVAL = 6000;

    // ═══════════════════════════════════════════════════════════
    // ECONOMY
    // ═══════════════════════════════════════════════════════════

    /** Maximales Konto-Guthaben: 1 Billion € */
    public static final double MAX_ECONOMY_BALANCE = 1_000_000_000_000.0;

    // ═══════════════════════════════════════════════════════════
    // PLOT-SYSTEM
    // ═══════════════════════════════════════════════════════════

    /** Maximale Anzahl an Plots im gesamten System */
    public static final int MAX_PLOTS = 50_000;

    /** Maximale Einträge im Plot-LRU-Cache */
    public static final int PLOT_CACHE_MAX_SIZE = 1_000;

    // ═══════════════════════════════════════════════════════════
    // PRODUKTIONS-BLOCK-ENTITIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Tick-Throttling-Intervall für Verarbeitungs-BlockEntities.
     * Verarbeitung läuft nur alle 5 Ticks statt jeden Tick (Faktor 5 weniger CPU-Last).
     * Wird von AbstractFermentationBarrelBlockEntity, AbstractProcessingBlockEntity
     * und UnifiedProcessingBlockEntity genutzt.
     */
    public static final int PROCESSING_TICK_INTERVAL = 5;

    /**
     * Netzwerk-Sync-Intervall für Verarbeitungs-BlockEntities (in Verarbeitungszyklen).
     * Bei PROCESSING_TICK_INTERVAL=5 entspricht 8 Zyklen ~40 Ticks (2 Sekunden).
     * Wird von AbstractFermentationBarrelBlockEntity genutzt.
     */
    public static final int PROCESSING_SYNC_CYCLE = 8;
}
