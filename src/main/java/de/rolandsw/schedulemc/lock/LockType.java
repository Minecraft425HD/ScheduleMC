package de.rolandsw.schedulemc.lock;

/**
 * Schloss-Typen mit Sicherheitsstufen.
 *
 * Schluessel-Verfall: besseres Schloss = kuerzere Lebensdauer + weniger Nutzungen.
 * Kopien: 50%, Gestohlene: 25% der Original-Werte.
 */
public enum LockType {

    SIMPLE("Einfaches Schloss",
            7 * 24 * 3600_000L,   // 7 Tage Schluessel-Dauer
            100,                   // 100 Nutzungen
            0.80f,                 // 80% Dietrich-Erfolg
            false,                 // kein Code
            -1L,                   // keine Code-Rotation
            false,                 // kein Alarm
            0xFF27AE60),

    SECURITY("Sicherheitsschloss",
            3 * 24 * 3600_000L,   // 3 Tage
            30,                    // 30 Nutzungen
            0.40f,                 // 40%
            false,
            -1L,
            false,                 // Warnung, aber kein Fahndungslevel
            0xFF2980B9),

    HIGH_SECURITY("Hochsicherheitsschloss",
            12 * 3600_000L,       // 12 Stunden
            10,                    // 10 Nutzungen
            0.10f,                 // 10%
            false,
            -1L,
            true,                  // Alarm + Fahndung bei Dietrich-Fehlschlag
            0xFFE74C3C),

    COMBINATION("Zahlenschloss",
            -1L,                   // kein Schluessel
            -1,
            0.0f,                  // kein Dietrich (Brute-Force moeglich)
            true,                  // hat Code
            -1L,                   // Code bleibt permanent
            false,
            0xFFF39C12),

    DUAL("Dual-Lock",
            12 * 3600_000L,       // 12 Stunden Schluessel
            10,                    // 10 Nutzungen
            0.05f,                 // 5% Dietrich
            true,                  // hat AUCH Code
            24 * 3600_000L,       // Code rotiert taeglich
            true,                  // Alarm + Fahndung
            0xFF8E44AD);

    private final String displayName;
    private final long keyDurationMs;
    private final int keyMaxUses;
    private final float pickChance;
    private final boolean hasCode;
    private final long codeRotationMs;
    private final boolean triggersAlarm;
    private final int color;

    LockType(String displayName, long keyDurationMs, int keyMaxUses, float pickChance,
             boolean hasCode, long codeRotationMs, boolean triggersAlarm, int color) {
        this.displayName = displayName;
        this.keyDurationMs = keyDurationMs;
        this.keyMaxUses = keyMaxUses;
        this.pickChance = pickChance;
        this.hasCode = hasCode;
        this.codeRotationMs = codeRotationMs;
        this.triggersAlarm = triggersAlarm;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public long getKeyDurationMs() { return keyDurationMs; }
    public int getKeyMaxUses() { return keyMaxUses; }
    public float getPickChance() { return pickChance; }
    public boolean hasCode() { return hasCode; }
    public long getCodeRotationMs() { return codeRotationMs; }
    public boolean triggersAlarm() { return triggersAlarm; }
    public int getColor() { return color; }

    /** Ob dieses Schloss physische Schluessel unterstuetzt. */
    public boolean supportsKeys() { return keyDurationMs > 0; }

    /** Schluessel-Dauer fuer eine bestimmte Herkunft. */
    public long getKeyDuration(KeyOrigin origin) {
        if (keyDurationMs <= 0) return -1;
        return (long)(keyDurationMs * origin.getDurationMultiplier());
    }

    /** Schluessel-Nutzungen fuer eine bestimmte Herkunft. */
    public int getKeyUses(KeyOrigin origin) {
        if (keyMaxUses <= 0) return -1;
        return Math.max(1, (int)(keyMaxUses * origin.getUsesMultiplier()));
    }

    /** Benoetigter Rohling-Tier (0=Kupfer, 1=Eisen, 2=Netherite). */
    public int getRequiredBlankTier() {
        return switch (this) {
            case SIMPLE -> 0;
            case SECURITY -> 1;
            case HIGH_SECURITY, DUAL -> 2;
            default -> -1;  // COMBINATION braucht keinen Schluessel
        };
    }

    // ═══════════════════════════════════════════════════════════
    // SCHLUESSEL-HERKUNFT
    // ═══════════════════════════════════════════════════════════

    public enum KeyOrigin {
        ORIGINAL("Original", 1.0f, 1.0f),
        COPY("Kopie", 0.5f, 0.5f),
        STOLEN("Gestohlen", 0.25f, 0.25f);

        private final String displayName;
        private final float durationMultiplier;
        private final float usesMultiplier;

        KeyOrigin(String displayName, float durationMultiplier, float usesMultiplier) {
            this.displayName = displayName;
            this.durationMultiplier = durationMultiplier;
            this.usesMultiplier = usesMultiplier;
        }

        public String getDisplayName() { return displayName; }
        public float getDurationMultiplier() { return durationMultiplier; }
        public float getUsesMultiplier() { return usesMultiplier; }
    }
}
