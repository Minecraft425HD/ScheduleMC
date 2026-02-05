package de.rolandsw.schedulemc.gang.mission;

import java.util.concurrent.ThreadLocalRandom;

/**
 * ~30 vordefinierte Auftrags-Vorlagen fuer das Gang-Missions-System.
 *
 * Jede Vorlage definiert:
 * - MissionType (HOURLY/DAILY/WEEKLY)
 * - Beschreibung (mit %d Platzhalter fuer Zielwert)
 * - TrackingKey (fuer Fortschritts-Erkennung)
 * - Min/Max Zielwert, XP-Belohnung, Geld-Belohnung
 * - TrackingMode (INCREMENTAL = Events addieren, THRESHOLD = Zustandspruefung)
 */
public enum MissionTemplate {

    // ═══════════════════════════════════════════════════════════
    // STUENDLICH (Pool: 10, davon 2 zufaellig gewaehlt)
    // ═══════════════════════════════════════════════════════════
    H_SELL_PRODUCTS(MissionType.HOURLY, "Verkaufe %d Produkte", "SELL",
            TrackingMode.INCREMENTAL, 3, 8, 2, 5, 100, 300),
    H_PRODUCE_ITEMS(MissionType.HOURLY, "Produziere %d Items", "PRODUCE",
            TrackingMode.INCREMENTAL, 5, 15, 2, 4, 50, 200),
    H_EARN_MONEY(MissionType.HOURLY, "Verdiene %d\u20AC", "EARN",
            TrackingMode.INCREMENTAL, 500, 2000, 3, 5, 100, 250),
    H_HARVEST_PLANTS(MissionType.HOURLY, "Ernte %d Pflanzen", "HARVEST",
            TrackingMode.INCREMENTAL, 10, 30, 1, 3, 50, 150),
    H_DELIVER_GOODS(MissionType.HOURLY, "Liefere %d Waren", "DELIVER",
            TrackingMode.INCREMENTAL, 2, 5, 3, 5, 150, 300),
    H_SELL_VARIETY(MissionType.HOURLY, "Verkaufe %d versch. Produkte", "SELL_VARIETY",
            TrackingMode.INCREMENTAL, 2, 4, 3, 5, 100, 200),
    H_PRODUCE_QUALITY(MissionType.HOURLY, "Produziere %d Qualitaetsitems", "PRODUCE_QUALITY",
            TrackingMode.INCREMENTAL, 3, 8, 3, 5, 100, 250),
    H_EARN_TIPS(MissionType.HOURLY, "Verdiene %d\u20AC Trinkgeld", "TIPS",
            TrackingMode.INCREMENTAL, 100, 500, 2, 4, 50, 150),
    H_COMPLETE_TRADE(MissionType.HOURLY, "Schliesse %d Handel ab", "TRADE",
            TrackingMode.INCREMENTAL, 1, 3, 3, 5, 100, 300),
    H_COLLECT_RESOURCES(MissionType.HOURLY, "Sammle %d Ressourcen", "COLLECT",
            TrackingMode.INCREMENTAL, 10, 25, 2, 4, 50, 200),

    // ═══════════════════════════════════════════════════════════
    // TAEGLICH (Pool: 12, davon 3 zufaellig gewaehlt)
    // ═══════════════════════════════════════════════════════════
    D_DAILY_REVENUE(MissionType.DAILY, "Tagesumsatz %d\u20AC", "EARN",
            TrackingMode.INCREMENTAL, 3000, 10000, 10, 25, 500, 2000),
    D_MEMBERS_ONLINE(MissionType.DAILY, "%d Mitglieder gleichzeitig", "ONLINE_MEMBERS",
            TrackingMode.THRESHOLD, 2, 5, 10, 20, 0, 0),
    D_HOLD_TERRITORIES(MissionType.DAILY, "Halte %d Territorien", "TERRITORY_COUNT",
            TrackingMode.THRESHOLD, 2, 5, 15, 25, 300, 800),
    D_SELL_VARIETY(MissionType.DAILY, "%d versch. Produkte verkaufen", "SELL_VARIETY",
            TrackingMode.INCREMENTAL, 3, 8, 10, 15, 400, 1000),
    D_GANG_BALANCE(MissionType.DAILY, "Gang-Kasse auf %d\u20AC", "GANG_BALANCE",
            TrackingMode.THRESHOLD, 10000, 50000, 15, 20, 0, 0),
    D_EARN_GANG_XP(MissionType.DAILY, "Sammle %d Gang-XP", "GANG_XP",
            TrackingMode.INCREMENTAL, 20, 50, 10, 15, 500, 500),
    D_PRODUCE_BATCH(MissionType.DAILY, "Produziere %d Items gesamt", "PRODUCE",
            TrackingMode.INCREMENTAL, 20, 50, 10, 15, 400, 800),
    D_EARN_DAILY(MissionType.DAILY, "Verdiene %d\u20AC heute", "EARN",
            TrackingMode.INCREMENTAL, 5000, 15000, 15, 25, 500, 1500),
    D_SELL_BATCH(MissionType.DAILY, "Verkaufe %d Produkte heute", "SELL",
            TrackingMode.INCREMENTAL, 10, 30, 10, 20, 500, 1000),
    D_DEPOSIT_GANG(MissionType.DAILY, "Zahle %d\u20AC in Gang-Kasse", "DEPOSIT",
            TrackingMode.INCREMENTAL, 1000, 5000, 10, 15, 0, 0),
    D_HARVEST_BATCH(MissionType.DAILY, "Ernte %d Pflanzen heute", "HARVEST",
            TrackingMode.INCREMENTAL, 20, 60, 8, 15, 300, 700),
    D_TRADE_VOLUME(MissionType.DAILY, "Handelsvolumen %d\u20AC", "TRADE_VOLUME",
            TrackingMode.INCREMENTAL, 5000, 20000, 15, 20, 500, 1000),

    // ═══════════════════════════════════════════════════════════
    // WOECHENTLICH (Pool: 8, davon 2 zufaellig gewaehlt)
    // ═══════════════════════════════════════════════════════════
    W_WEEKLY_REVENUE(MissionType.WEEKLY, "Gesamtumsatz %d\u20AC", "EARN",
            TrackingMode.INCREMENTAL, 30000, 100000, 80, 150, 3000, 10000),
    W_PROMOTE_MEMBER(MissionType.WEEKLY, "Befoerdere ein Mitglied", "PROMOTE",
            TrackingMode.INCREMENTAL, 1, 1, 50, 50, 0, 0),
    W_UNLOCK_PERK(MissionType.WEEKLY, "Schalte einen Perk frei", "UNLOCK_PERK",
            TrackingMode.INCREMENTAL, 1, 1, 60, 100, 2000, 2000),
    W_HOLD_TERRITORIES(MissionType.WEEKLY, "%d Territorien gleichzeitig", "TERRITORY_COUNT",
            TrackingMode.THRESHOLD, 3, 8, 80, 120, 5000, 5000),
    W_COMPLETE_MISSIONS(MissionType.WEEKLY, "%d Auftraege erledigen", "COMPLETE_MISSION",
            TrackingMode.INCREMENTAL, 10, 20, 100, 100, 5000, 5000),
    W_RECRUIT_MEMBER(MissionType.WEEKLY, "Werbe ein neues Mitglied", "RECRUIT",
            TrackingMode.INCREMENTAL, 1, 1, 40, 60, 1000, 1000),
    W_WEEKLY_XP(MissionType.WEEKLY, "Sammle %d Gang-XP diese Woche", "GANG_XP",
            TrackingMode.INCREMENTAL, 100, 300, 80, 120, 3000, 3000),
    W_WEEKLY_BALANCE(MissionType.WEEKLY, "Gang-Kasse auf %d\u20AC bringen", "GANG_BALANCE",
            TrackingMode.THRESHOLD, 50000, 200000, 100, 150, 5000, 5000);

    public enum TrackingMode {
        /** Fortschritt wird bei Events addiert (z.B. Verkauf +1) */
        INCREMENTAL,
        /** Fortschritt = aktueller Zustand (z.B. Territorien = 4) */
        THRESHOLD
    }

    private final MissionType type;
    private final String descriptionTemplate;
    private final String trackingKey;
    private final TrackingMode trackingMode;
    private final int minTarget, maxTarget;
    private final int minXP, maxXP;
    private final int minMoney, maxMoney;

    MissionTemplate(MissionType type, String descriptionTemplate, String trackingKey,
                    TrackingMode trackingMode,
                    int minTarget, int maxTarget,
                    int minXP, int maxXP,
                    int minMoney, int maxMoney) {
        this.type = type;
        this.descriptionTemplate = descriptionTemplate;
        this.trackingKey = trackingKey;
        this.trackingMode = trackingMode;
        this.minTarget = minTarget;
        this.maxTarget = maxTarget;
        this.minXP = minXP;
        this.maxXP = maxXP;
        this.minMoney = minMoney;
        this.maxMoney = maxMoney;
    }

    /**
     * Generiert eine konkrete Mission mit zufaelligen Werten.
     */
    public GangMission generate(String missionId) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int target = minTarget == maxTarget ? minTarget : rng.nextInt(minTarget, maxTarget + 1);
        int xp = minXP == maxXP ? minXP : rng.nextInt(minXP, maxXP + 1);
        int money = minMoney == maxMoney ? minMoney : rng.nextInt(minMoney, maxMoney + 1);

        // Zielwerte auf schoene Zahlen runden
        if (target >= 1000) target = (target / 500) * 500;
        else if (target >= 100) target = (target / 50) * 50;
        if (money >= 1000) money = (money / 500) * 500;
        else if (money >= 100) money = (money / 50) * 50;

        String desc = String.format(descriptionTemplate, target);
        return new GangMission(missionId, this, desc, target, xp, money);
    }

    /**
     * Gibt alle Templates fuer einen bestimmten MissionType zurueck.
     */
    public static MissionTemplate[] getByType(MissionType type) {
        return java.util.Arrays.stream(values())
                .filter(t -> t.type == type)
                .toArray(MissionTemplate[]::new);
    }

    public MissionType getType() { return type; }
    public String getTrackingKey() { return trackingKey; }
    public TrackingMode getTrackingMode() { return trackingMode; }
}
