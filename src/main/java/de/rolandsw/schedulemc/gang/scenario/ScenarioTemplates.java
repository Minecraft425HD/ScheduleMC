package de.rolandsw.schedulemc.gang.scenario;

import java.util.ArrayList;
import java.util.List;

/**
 * Vorgefertigte Szenario-Vorlagen fuer den Editor.
 *
 * Jede Vorlage ist ein vollstaendiges Szenario mit platzierten Bloecken,
 * Verbindungen und sinnvollen Standard-Parametern.
 */
public class ScenarioTemplates {

    private static final int CX = 130; // Standard Canvas-X fuer Bloecke
    private static final int STEP = 55; // Vertikaler Abstand zwischen Bloecken

    /**
     * Gibt alle verfuegbaren Gang-Vorlagen zurueck.
     */
    public static List<MissionScenario> getAll() {
        List<MissionScenario> templates = new ArrayList<>();
        templates.add(bankUeberfall());
        templates.add(ladenRaub());
        templates.add(juwelenHeist());
        templates.add(lieferjobStandard());
        templates.add(drogenLieferung());
        templates.add(tankstellenRaub());
        templates.add(gangKrieg());
        templates.add(schutzgeldEintreiben());
        templates.add(waffenHandel());
        templates.add(autoKlau());
        templates.add(geldWaesche());
        templates.add(fabrikSabotage());
        templates.add(entfuehrung());
        templates.add(undercoverOp());
        templates.add(autobahnFlucht());
        templates.add(kasino());
        return templates;
    }

    /**
     * Gibt die Gang-Vorlagen-Namen fuer das Dropdown zurueck.
     */
    public static String[] getTemplateNames() {
        return new String[]{
                "Bank-Ueberfall",
                "Laden-Raub",
                "Juwelen-Heist",
                "Lieferjob",
                "Drogen-Lieferung",
                "Tankstellen-Raub",
                "Gangkrieg",
                "Schutzgeld",
                "Waffenhandel",
                "Auto-Klau",
                "Geldwaesche",
                "Fabrik-Sabotage",
                "Entfuehrung",
                "Undercover-Op",
                "Autobahn-Flucht",
                "Kasino-Raub"
        };
    }

    /**
     * Gibt alle verfuegbaren Spieler-Missionsvorlagen zurueck.
     */
    public static List<MissionScenario> getPlayerTemplates() {
        List<MissionScenario> templates = new ArrayList<>();
        templates.add(playerErsterAuftrag());
        templates.add(playerGeheimeLieferung());
        templates.add(playerInformant());
        templates.add(playerTerritoriumAuftrag());
        templates.add(playerNotfallAuftrag());
        templates.add(playerKampfAuftrag());
        templates.add(playerSchmuggler());
        templates.add(playerDetektiv());
        return templates;
    }

    /**
     * Gibt die Spieler-Vorlagen-Namen fuer das Dropdown zurueck.
     */
    public static String[] getPlayerTemplateNames() {
        return new String[]{
                "Erster Auftrag",
                "Geheime Lieferung",
                "Informant",
                "Territorium-Auftrag",
                "Notfall-Auftrag",
                "Kampf-Auftrag",
                "Schmuggler",
                "Detektiv"
        };
    }

    // ═══════════════════════════════════════════════════════════
    // VORLAGEN
    // ═══════════════════════════════════════════════════════════

    public static MissionScenario bankUeberfall() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y);
        y += STEP;
        ScenarioObjective gotoBank = new ScenarioObjective("o1", ObjectiveType.GOTO_LOCATION, CX, y);
        gotoBank.setParam("x", "150"); gotoBank.setParam("y", "64"); gotoBank.setParam("z", "200");
        gotoBank.setParam("radius", "10");
        y += STEP;
        ScenarioObjective rob = new ScenarioObjective("o2", ObjectiveType.ROB_BANK, CX, y);
        rob.setParam("plot_id", ""); rob.setParam("difficulty", "3"); rob.setParam("time_limit", "180");
        y += STEP;
        ScenarioObjective crack = new ScenarioObjective("o3", ObjectiveType.CRACK_SAFE, CX, y);
        crack.setParam("difficulty", "3"); crack.setParam("time_limit", "60");
        y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o4", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("plot_id", ""); escape.setParam("radius", "100");
        y += STEP;
        ScenarioObjective evade = new ScenarioObjective("o5", ObjectiveType.EVADE_POLICE, CX, y);
        evade.setParam("wanted_level", "3"); evade.setParam("duration", "120");
        y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "500"); reward.setParam("money", "15000");

        start.setNextObjectiveId("o1");
        gotoBank.setNextObjectiveId("o2");
        rob.setNextObjectiveId("o3");
        crack.setNextObjectiveId("o4");
        escape.setNextObjectiveId("o5");
        evade.setNextObjectiveId("r0");

        objs.add(start); objs.add(gotoBank); objs.add(rob);
        objs.add(crack); objs.add(escape); objs.add(evade); objs.add(reward);

        return new MissionScenario("tpl_bank", "Bank-Ueberfall",
                "Ueberfall auf die Zentralbank mit Tresor-Knacken und Flucht",
                4, 8, false, "WEEKLY", objs);
    }

    public static MissionScenario ladenRaub() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y);
        y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o1", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "80"); goTo.setParam("y", "64"); goTo.setParam("z", "50");
        goTo.setParam("radius", "5");
        y += STEP;
        ScenarioObjective rob = new ScenarioObjective("o2", ObjectiveType.ROB_STORE, CX, y);
        rob.setParam("plot_id", ""); rob.setParam("difficulty", "1"); rob.setParam("time_limit", "90");
        y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o3", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("plot_id", ""); escape.setParam("radius", "50");
        y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "100"); reward.setParam("money", "3000");

        start.setNextObjectiveId("o1");
        goTo.setNextObjectiveId("o2");
        rob.setNextObjectiveId("o3");
        escape.setNextObjectiveId("r0");

        objs.add(start); objs.add(goTo); objs.add(rob); objs.add(escape); objs.add(reward);

        return new MissionScenario("tpl_store", "Laden-Raub",
                "Einfacher Ueberfall auf einen Laden",
                1, 1, false, "DAILY", objs);
    }

    public static MissionScenario juwelenHeist() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y);
        y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o1", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "200"); goTo.setParam("y", "64"); goTo.setParam("z", "300");
        goTo.setParam("radius", "5");
        y += STEP;
        ScenarioObjective hack = new ScenarioObjective("o2", ObjectiveType.HACK_SYSTEM, CX, y);
        hack.setParam("difficulty", "3");
        y += STEP;
        ScenarioObjective rob = new ScenarioObjective("o3", ObjectiveType.ROB_JEWELRY, CX, y);
        rob.setParam("plot_id", ""); rob.setParam("difficulty", "3"); rob.setParam("time_limit", "120");
        y += STEP;
        ScenarioObjective collect = new ScenarioObjective("o4", ObjectiveType.COLLECT_ITEMS, CX, y);
        collect.setParam("item_id", "minecraft:diamond"); collect.setParam("amount", "5");
        y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o5", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("plot_id", ""); escape.setParam("radius", "80");
        y += STEP;
        ScenarioObjective evade = new ScenarioObjective("o6", ObjectiveType.EVADE_POLICE, CX, y);
        evade.setParam("wanted_level", "4"); evade.setParam("duration", "180");
        y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "750"); reward.setParam("money", "25000");

        start.setNextObjectiveId("o1");
        goTo.setNextObjectiveId("o2");
        hack.setNextObjectiveId("o3");
        rob.setNextObjectiveId("o4");
        collect.setNextObjectiveId("o5");
        escape.setNextObjectiveId("o6");
        evade.setNextObjectiveId("r0");

        objs.add(start); objs.add(goTo); objs.add(hack); objs.add(rob);
        objs.add(collect); objs.add(escape); objs.add(evade); objs.add(reward);

        return new MissionScenario("tpl_jewel", "Juwelen-Heist",
                "Aufwaendiger Juwelier-Ueberfall mit Alarm-Hack",
                5, 12, false, "WEEKLY", objs);
    }

    public static MissionScenario lieferjobStandard() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y);
        y += STEP;
        ScenarioObjective goNPC = new ScenarioObjective("o1", ObjectiveType.GOTO_NPC, CX, y);
        goNPC.setParam("npc_name", ""); goNPC.setParam("npc_type", "VERKAEUFER");
        y += STEP;
        ScenarioObjective collect = new ScenarioObjective("o2", ObjectiveType.COLLECT_ITEMS, CX, y);
        collect.setParam("item_id", "minecraft:paper"); collect.setParam("amount", "3");
        y += STEP;
        ScenarioObjective deliver = new ScenarioObjective("o3", ObjectiveType.DELIVER_ITEM, CX, y);
        deliver.setParam("item_id", "minecraft:paper"); deliver.setParam("plot_id", "");
        y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "80"); reward.setParam("money", "2000");

        start.setNextObjectiveId("o1");
        goNPC.setNextObjectiveId("o2");
        collect.setNextObjectiveId("o3");
        deliver.setNextObjectiveId("r0");

        objs.add(start); objs.add(goNPC); objs.add(collect); objs.add(deliver); objs.add(reward);

        return new MissionScenario("tpl_delivery", "Lieferjob",
                "Pakete abholen und an Zieladresse liefern",
                1, 0, false, "HOURLY", objs);
    }

    public static MissionScenario drogenLieferung() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y);
        y += STEP;
        ScenarioObjective collect = new ScenarioObjective("o1", ObjectiveType.COLLECT_ITEMS, CX, y);
        collect.setParam("item_id", "minecraft:sugar"); collect.setParam("amount", "10");
        y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o2", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "400"); goTo.setParam("y", "64"); goTo.setParam("z", "-100");
        goTo.setParam("radius", "10");
        y += STEP;
        ScenarioObjective deliver = new ScenarioObjective("o3", ObjectiveType.DELIVER_ITEM, CX, y);
        deliver.setParam("item_id", "minecraft:sugar"); deliver.setParam("plot_id", "");
        y += STEP;
        ScenarioObjective evade = new ScenarioObjective("o4", ObjectiveType.EVADE_POLICE, CX, y);
        evade.setParam("wanted_level", "2"); evade.setParam("duration", "90");
        y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "200"); reward.setParam("money", "8000");

        start.setNextObjectiveId("o1");
        collect.setNextObjectiveId("o2");
        goTo.setNextObjectiveId("o3");
        deliver.setNextObjectiveId("o4");
        evade.setNextObjectiveId("r0");

        objs.add(start); objs.add(collect); objs.add(goTo);
        objs.add(deliver); objs.add(evade); objs.add(reward);

        return new MissionScenario("tpl_drugs", "Drogen-Lieferung",
                "Illegale Ware abholen, liefern und Polizei abhaengen",
                3, 5, false, "DAILY", objs);
    }

    public static MissionScenario tankstellenRaub() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y);
        y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o1", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "50"); goTo.setParam("y", "64"); goTo.setParam("z", "50");
        goTo.setParam("radius", "5");
        y += STEP;
        ScenarioObjective rob = new ScenarioObjective("o2", ObjectiveType.ROB_GAS_STATION, CX, y);
        rob.setParam("plot_id", ""); rob.setParam("difficulty", "1"); rob.setParam("time_limit", "60");
        y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o3", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("plot_id", ""); escape.setParam("radius", "40");
        y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "60"); reward.setParam("money", "1500");

        start.setNextObjectiveId("o1");
        goTo.setNextObjectiveId("o2");
        rob.setNextObjectiveId("o3");
        escape.setNextObjectiveId("r0");

        objs.add(start); objs.add(goTo); objs.add(rob); objs.add(escape); objs.add(reward);

        return new MissionScenario("tpl_gas", "Tankstellen-Raub",
                "Schneller Ueberfall auf eine Tankstelle",
                1, 0, false, "HOURLY", objs);
    }

    public static MissionScenario gangKrieg() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y);
        y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o1", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "0"); goTo.setParam("y", "64"); goTo.setParam("z", "0");
        goTo.setParam("radius", "20");
        y += STEP;
        ScenarioObjective kill = new ScenarioObjective("o2", ObjectiveType.KILL_MOBS, CX, y);
        kill.setParam("entity_type", "zombie"); kill.setParam("count", "10");
        y += STEP;
        ScenarioObjective defend = new ScenarioObjective("o3", ObjectiveType.DEFEND_AREA, CX, y);
        defend.setParam("plot_id", ""); defend.setParam("duration", "300");
        y += STEP;
        ScenarioObjective survive = new ScenarioObjective("o4", ObjectiveType.SURVIVE_TIME, CX, y);
        survive.setParam("duration", "120");
        y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "400"); reward.setParam("money", "12000");

        start.setNextObjectiveId("o1");
        goTo.setNextObjectiveId("o2");
        kill.setNextObjectiveId("o3");
        defend.setNextObjectiveId("o4");
        survive.setNextObjectiveId("r0");

        objs.add(start); objs.add(goTo); objs.add(kill);
        objs.add(defend); objs.add(survive); objs.add(reward);

        return new MissionScenario("tpl_war", "Gangkrieg",
                "Feindliches Gebiet erobern und verteidigen",
                4, 10, false, "WEEKLY", objs);
    }

    public static MissionScenario schutzgeldEintreiben() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y);
        y += STEP;
        ScenarioObjective goNPC = new ScenarioObjective("o1", ObjectiveType.GOTO_NPC, CX, y);
        goNPC.setParam("npc_name", ""); goNPC.setParam("npc_type", "VERKAEUFER");
        y += STEP;
        ScenarioObjective talk = new ScenarioObjective("o2", ObjectiveType.TALK_TO_NPC, CX, y);
        talk.setParam("npc_name", ""); talk.setParam("npc_type", "VERKAEUFER");
        talk.setParam("dialog_id", "extortion");
        y += STEP;
        ScenarioObjective earn = new ScenarioObjective("o3", ObjectiveType.EARN_MONEY, CX, y);
        earn.setParam("amount", "2000");
        y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "120"); reward.setParam("money", "4000");

        start.setNextObjectiveId("o1");
        goNPC.setNextObjectiveId("o2");
        talk.setNextObjectiveId("o3");
        earn.setNextObjectiveId("r0");

        objs.add(start); objs.add(goNPC); objs.add(talk); objs.add(earn); objs.add(reward);

        return new MissionScenario("tpl_extort", "Schutzgeld",
                "Schutzgeld von Ladenbesitzern eintreiben",
                2, 3, false, "DAILY", objs);
    }

    // ═══════════════════════════════════════════════════════════
    // NEUE GANG-VORLAGEN
    // ═══════════════════════════════════════════════════════════

    public static MissionScenario waffenHandel() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective goNPC = new ScenarioObjective("o1", ObjectiveType.GOTO_NPC, CX, y);
        goNPC.setParam("npc_name", ""); goNPC.setParam("npc_type", "WAFFENHAENDLER"); y += STEP;
        ScenarioObjective buy = new ScenarioObjective("o2", ObjectiveType.BUY_ITEM, CX, y);
        buy.setParam("item_id", "schedulemc:pistol"); buy.setParam("amount", "5"); buy.setParam("price", "500"); y += STEP;
        ScenarioObjective goto2 = new ScenarioObjective("o3", ObjectiveType.GOTO_LOCATION, CX, y);
        goto2.setParam("x", "300"); goto2.setParam("y", "64"); goto2.setParam("z", "-200"); goto2.setParam("radius", "10"); y += STEP;
        ScenarioObjective deliver = new ScenarioObjective("o4", ObjectiveType.DELIVER_ITEM, CX, y);
        deliver.setParam("item_id", "schedulemc:pistol"); deliver.setParam("plot_id", ""); y += STEP;
        ScenarioObjective earn = new ScenarioObjective("o5", ObjectiveType.EARN_MONEY, CX, y);
        earn.setParam("amount", "5000"); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "250"); reward.setParam("money", "6000");

        start.setNextObjectiveId("o1"); goNPC.setNextObjectiveId("o2"); buy.setNextObjectiveId("o3");
        goto2.setNextObjectiveId("o4"); deliver.setNextObjectiveId("o5"); earn.setNextObjectiveId("r0");

        objs.add(start); objs.add(goNPC); objs.add(buy); objs.add(goto2); objs.add(deliver); objs.add(earn); objs.add(reward);
        return new MissionScenario("tpl_weapons", "Waffenhandel",
                "Waffen beschaffen und an Kunden liefern", 3, 6, false, "DAILY", objs);
    }

    public static MissionScenario autoKlau() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o1", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "-150"); goTo.setParam("y", "64"); goTo.setParam("z", "250"); goTo.setParam("radius", "8"); y += STEP;
        ScenarioObjective steal = new ScenarioObjective("o2", ObjectiveType.STEAL_ITEM, CX, y);
        steal.setParam("item_id", "schedulemc:car_key"); steal.setParam("npc_name", ""); y += STEP;
        ScenarioObjective drive = new ScenarioObjective("o3", ObjectiveType.DRIVE_TO, CX, y);
        drive.setParam("x", "500"); drive.setParam("y", "64"); drive.setParam("z", "100"); drive.setParam("radius", "15"); y += STEP;
        ScenarioObjective evade = new ScenarioObjective("o4", ObjectiveType.EVADE_POLICE, CX, y);
        evade.setParam("wanted_level", "2"); evade.setParam("duration", "90"); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "180"); reward.setParam("money", "7000");

        start.setNextObjectiveId("o1"); goTo.setNextObjectiveId("o2"); steal.setNextObjectiveId("o3");
        drive.setNextObjectiveId("o4"); evade.setNextObjectiveId("r0");

        objs.add(start); objs.add(goTo); objs.add(steal); objs.add(drive); objs.add(evade); objs.add(reward);
        return new MissionScenario("tpl_carjack", "Auto-Klau",
                "Fahrzeug stehlen und zu einem geheimen Treff bringen", 2, 4, false, "DAILY", objs);
    }

    public static MissionScenario geldWaesche() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective earn = new ScenarioObjective("o1", ObjectiveType.EARN_MONEY, CX, y);
        earn.setParam("amount", "10000"); y += STEP;
        ScenarioObjective goNPC = new ScenarioObjective("o2", ObjectiveType.GOTO_NPC, CX, y);
        goNPC.setParam("npc_name", ""); goNPC.setParam("npc_type", "BANKER"); y += STEP;
        ScenarioObjective launder = new ScenarioObjective("o3", ObjectiveType.LAUNDER_MONEY, CX, y);
        launder.setParam("amount", "10000"); launder.setParam("fee_percent", "20"); y += STEP;
        ScenarioObjective deliver = new ScenarioObjective("o4", ObjectiveType.GOTO_LOCATION, CX, y);
        deliver.setParam("x", "0"); deliver.setParam("y", "64"); deliver.setParam("z", "0"); deliver.setParam("radius", "5"); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "300"); reward.setParam("money", "8000");

        start.setNextObjectiveId("o1"); earn.setNextObjectiveId("o2"); goNPC.setNextObjectiveId("o3");
        launder.setNextObjectiveId("o4"); deliver.setNextObjectiveId("r0");

        objs.add(start); objs.add(earn); objs.add(goNPC); objs.add(launder); objs.add(deliver); objs.add(reward);
        return new MissionScenario("tpl_launder", "Geldwaesche",
                "Schwarzgeld durch einen Hintermann waschen", 3, 7, false, "WEEKLY", objs);
    }

    public static MissionScenario fabrikSabotage() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o1", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "600"); goTo.setParam("y", "64"); goTo.setParam("z", "400"); goTo.setParam("radius", "10"); y += STEP;
        ScenarioObjective sneak = new ScenarioObjective("o2", ObjectiveType.SNEAK_PAST, CX, y);
        sneak.setParam("plot_id", ""); sneak.setParam("guard_count", "3"); y += STEP;
        ScenarioObjective sabotage = new ScenarioObjective("o3", ObjectiveType.SABOTAGE, CX, y);
        sabotage.setParam("plot_id", ""); sabotage.setParam("difficulty", "3"); y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o4", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("plot_id", ""); escape.setParam("radius", "150"); y += STEP;
        ScenarioObjective survive = new ScenarioObjective("o5", ObjectiveType.SURVIVE_TIME, CX, y);
        survive.setParam("duration", "60"); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "450"); reward.setParam("money", "14000");

        start.setNextObjectiveId("o1"); goTo.setNextObjectiveId("o2"); sneak.setNextObjectiveId("o3");
        sabotage.setNextObjectiveId("o4"); escape.setNextObjectiveId("o5"); survive.setNextObjectiveId("r0");

        objs.add(start); objs.add(goTo); objs.add(sneak); objs.add(sabotage); objs.add(escape); objs.add(survive); objs.add(reward);
        return new MissionScenario("tpl_sabotage", "Fabrik-Sabotage",
                "Rivalen-Fabrik unbemerkt sabotieren und fliehen", 4, 9, false, "WEEKLY", objs);
    }

    public static MissionScenario entfuehrung() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective goNPC = new ScenarioObjective("o1", ObjectiveType.GOTO_NPC, CX, y);
        goNPC.setParam("npc_name", ""); goNPC.setParam("npc_type", "ZIEL"); y += STEP;
        ScenarioObjective kidnap = new ScenarioObjective("o2", ObjectiveType.KIDNAP_NPC, CX, y);
        kidnap.setParam("npc_name", ""); kidnap.setParam("plot_id", ""); y += STEP;
        ScenarioObjective goto2 = new ScenarioObjective("o3", ObjectiveType.GOTO_LOCATION, CX, y);
        goto2.setParam("x", "-300"); goto2.setParam("y", "64"); goto2.setParam("z", "500"); goto2.setParam("radius", "10"); y += STEP;
        ScenarioObjective evade = new ScenarioObjective("o4", ObjectiveType.EVADE_POLICE, CX, y);
        evade.setParam("wanted_level", "4"); evade.setParam("duration", "150"); y += STEP;
        ScenarioObjective earn = new ScenarioObjective("o5", ObjectiveType.EARN_MONEY, CX, y);
        earn.setParam("amount", "20000"); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "600"); reward.setParam("money", "20000");

        start.setNextObjectiveId("o1"); goNPC.setNextObjectiveId("o2"); kidnap.setNextObjectiveId("o3");
        goto2.setNextObjectiveId("o4"); evade.setNextObjectiveId("o5"); earn.setNextObjectiveId("r0");

        objs.add(start); objs.add(goNPC); objs.add(kidnap); objs.add(goto2); objs.add(evade); objs.add(earn); objs.add(reward);
        return new MissionScenario("tpl_kidnap", "Entfuehrung",
                "Hochwertiges Ziel entfuehren und Loesegeld kassieren", 5, 15, false, "WEEKLY", objs);
    }

    public static MissionScenario undercoverOp() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective disguise = new ScenarioObjective("o1", ObjectiveType.DISGUISE, CX, y);
        disguise.setParam("outfit", "civilian"); y += STEP;
        ScenarioObjective goNPC = new ScenarioObjective("o2", ObjectiveType.GOTO_NPC, CX, y);
        goNPC.setParam("npc_name", ""); goNPC.setParam("npc_type", "INFORMANT"); y += STEP;
        ScenarioObjective talk = new ScenarioObjective("o3", ObjectiveType.TALK_TO_NPC, CX, y);
        talk.setParam("npc_name", ""); talk.setParam("dialog_id", "undercover"); y += STEP;
        ScenarioObjective code = new ScenarioObjective("o4", ObjectiveType.RECEIVE_CODE, CX, y);
        code.setParam("code_id", "safehouse_code"); y += STEP;
        ScenarioObjective enter = new ScenarioObjective("o5", ObjectiveType.ENTER_BUILDING, CX, y);
        enter.setParam("plot_id", ""); y += STEP;
        ScenarioObjective hack = new ScenarioObjective("o6", ObjectiveType.HACK_SYSTEM, CX, y);
        hack.setParam("difficulty", "4"); y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o7", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("plot_id", ""); escape.setParam("radius", "200"); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "700"); reward.setParam("money", "22000");

        start.setNextObjectiveId("o1"); disguise.setNextObjectiveId("o2"); goNPC.setNextObjectiveId("o3");
        talk.setNextObjectiveId("o4"); code.setNextObjectiveId("o5"); enter.setNextObjectiveId("o6");
        hack.setNextObjectiveId("o7"); escape.setNextObjectiveId("r0");

        objs.add(start); objs.add(disguise); objs.add(goNPC); objs.add(talk);
        objs.add(code); objs.add(enter); objs.add(hack); objs.add(escape); objs.add(reward);
        return new MissionScenario("tpl_undercover", "Undercover-Op",
                "Verkleidet in feindliche Organisation eindringen und Daten stehlen", 5, 14, false, "WEEKLY", objs);
    }

    public static MissionScenario autobahnFlucht() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o1", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "0"); goTo.setParam("y", "64"); goTo.setParam("z", "0"); goTo.setParam("radius", "5"); y += STEP;
        ScenarioObjective collect = new ScenarioObjective("o2", ObjectiveType.COLLECT_ITEMS, CX, y);
        collect.setParam("item_id", "minecraft:chest"); collect.setParam("amount", "1"); y += STEP;
        ScenarioObjective drive = new ScenarioObjective("o3", ObjectiveType.DRIVE_TO, CX, y);
        drive.setParam("x", "1000"); drive.setParam("y", "64"); drive.setParam("z", "1000"); drive.setParam("radius", "20"); y += STEP;
        ScenarioObjective evade = new ScenarioObjective("o4", ObjectiveType.EVADE_POLICE, CX, y);
        evade.setParam("wanted_level", "3"); evade.setParam("duration", "120"); y += STEP;
        ScenarioObjective lose = new ScenarioObjective("o5", ObjectiveType.LOSE_WANTED, CX, y);
        lose.setParam("time_to_lose", "30"); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "220"); reward.setParam("money", "9000");

        start.setNextObjectiveId("o1"); goTo.setNextObjectiveId("o2"); collect.setNextObjectiveId("o3");
        drive.setNextObjectiveId("o4"); evade.setNextObjectiveId("o5"); lose.setNextObjectiveId("r0");

        objs.add(start); objs.add(goTo); objs.add(collect); objs.add(drive); objs.add(evade); objs.add(lose); objs.add(reward);
        return new MissionScenario("tpl_highway", "Autobahn-Flucht",
                "Paket abholen, auf der Flucht liefern und Polizei abhaengen", 3, 5, false, "DAILY", objs);
    }

    public static MissionScenario kasino() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o1", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "200"); goTo.setParam("y", "64"); goTo.setParam("z", "-300"); goTo.setParam("radius", "8"); y += STEP;
        ScenarioObjective disguise = new ScenarioObjective("o2", ObjectiveType.DISGUISE, CX, y);
        disguise.setParam("outfit", "formal"); y += STEP;
        ScenarioObjective enter = new ScenarioObjective("o3", ObjectiveType.ENTER_BUILDING, CX, y);
        enter.setParam("plot_id", ""); y += STEP;
        ScenarioObjective hack = new ScenarioObjective("o4", ObjectiveType.HACK_SYSTEM, CX, y);
        hack.setParam("difficulty", "2"); y += STEP;
        ScenarioObjective rob = new ScenarioObjective("o5", ObjectiveType.ROB_PLOT, CX, y);
        rob.setParam("plot_id", ""); rob.setParam("difficulty", "3"); rob.setParam("time_limit", "120"); y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o6", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("plot_id", ""); escape.setParam("radius", "100"); y += STEP;
        ScenarioObjective evade = new ScenarioObjective("o7", ObjectiveType.EVADE_POLICE, CX, y);
        evade.setParam("wanted_level", "3"); evade.setParam("duration", "150"); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "550"); reward.setParam("money", "18000");

        start.setNextObjectiveId("o1"); goTo.setNextObjectiveId("o2"); disguise.setNextObjectiveId("o3");
        enter.setNextObjectiveId("o4"); hack.setNextObjectiveId("o5"); rob.setNextObjectiveId("o6");
        escape.setNextObjectiveId("o7"); evade.setNextObjectiveId("r0");

        objs.add(start); objs.add(goTo); objs.add(disguise); objs.add(enter);
        objs.add(hack); objs.add(rob); objs.add(escape); objs.add(evade); objs.add(reward);
        return new MissionScenario("tpl_casino", "Kasino-Raub",
                "Verkleidet ins Kasino einschleichen, System hacken und Kasse rauben", 5, 11, false, "WEEKLY", objs);
    }

    // ═══════════════════════════════════════════════════════════
    // SPIELER-MISSIONSVORLAGEN (STORY)
    // ═══════════════════════════════════════════════════════════

    public static MissionScenario playerErsterAuftrag() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective info = new ScenarioObjective("o1", ObjectiveType.MISSION_INFO, CX, y);
        info.setParam("title", "Erster Auftrag"); info.setParam("description", "Hol dir deinen ersten Auftrag bei einem Haendler."); info.setParam("npc_giver", ""); y += STEP;
        ScenarioObjective give = new ScenarioObjective("o2", ObjectiveType.NPC_GIVE_MISSION, CX, y);
        give.setParam("npc_name", ""); give.setParam("dialog", "Ich habe einen Job fuer dich. Interessiert?"); y += STEP;
        ScenarioObjective tracking = new ScenarioObjective("o3", ObjectiveType.MISSION_TRACKING, CX, y);
        tracking.setParam("tracking_key", "npc_interaction_dealer"); tracking.setParam("target_amount", "1"); y += STEP;
        ScenarioObjective complete = new ScenarioObjective("o4", ObjectiveType.NPC_COMPLETE_MISSION, CX, y);
        complete.setParam("npc_name", ""); complete.setParam("dialog", "Gut gemacht. Hier ist deine Belohnung."); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "500"); reward.setParam("money", "2000");

        start.setNextObjectiveId("o1"); info.setNextObjectiveId("o2"); give.setNextObjectiveId("o3");
        tracking.setNextObjectiveId("o4"); complete.setNextObjectiveId("r0");

        objs.add(start); objs.add(info); objs.add(give); objs.add(tracking); objs.add(complete); objs.add(reward);
        return new MissionScenario("ptpl_first", "Erster Auftrag",
                "Einfache Hauptmission: NPC gibt Auftrag, Tracking, Abgabe", 1, 0, false, "STORY_MAIN", objs);
    }

    public static MissionScenario playerGeheimeLieferung() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective info = new ScenarioObjective("o1", ObjectiveType.MISSION_INFO, CX, y);
        info.setParam("title", "Geheime Lieferung"); info.setParam("description", "Bring das Paket sicher an."); info.setParam("npc_giver", ""); y += STEP;
        ScenarioObjective notify = new ScenarioObjective("o2", ObjectiveType.PLAYER_NOTIFY, CX, y);
        notify.setParam("message", "Geh zum Abholort."); notify.setParam("color", "yellow"); y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o3", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "100"); goTo.setParam("y", "64"); goTo.setParam("z", "100"); goTo.setParam("radius", "5"); y += STEP;
        ScenarioObjective collect = new ScenarioObjective("o4", ObjectiveType.COLLECT_ITEMS, CX, y);
        collect.setParam("item_id", "minecraft:paper"); collect.setParam("amount", "1"); y += STEP;
        ScenarioObjective deliver = new ScenarioObjective("o5", ObjectiveType.DELIVER_ITEM, CX, y);
        deliver.setParam("item_id", "minecraft:paper"); deliver.setParam("plot_id", ""); y += STEP;
        ScenarioObjective complete = new ScenarioObjective("o6", ObjectiveType.NPC_COMPLETE_MISSION, CX, y);
        complete.setParam("npc_name", ""); complete.setParam("dialog", "Paket erhalten. Danke."); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "400"); reward.setParam("money", "3000");

        start.setNextObjectiveId("o1"); info.setNextObjectiveId("o2"); notify.setNextObjectiveId("o3");
        goTo.setNextObjectiveId("o4"); collect.setNextObjectiveId("o5"); deliver.setNextObjectiveId("o6"); complete.setNextObjectiveId("r0");

        objs.add(start); objs.add(info); objs.add(notify); objs.add(goTo); objs.add(collect); objs.add(deliver); objs.add(complete); objs.add(reward);
        return new MissionScenario("ptpl_delivery", "Geheime Lieferung",
                "Nebenmission: Paket abholen und diskret zustellen", 1, 0, false, "STORY_SIDE", objs);
    }

    public static MissionScenario playerInformant() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective info = new ScenarioObjective("o1", ObjectiveType.MISSION_INFO, CX, y);
        info.setParam("title", "Der Informant"); info.setParam("description", "Befrage drei Kontakte und beschaffe Infos."); info.setParam("npc_giver", ""); y += STEP;
        ScenarioObjective give = new ScenarioObjective("o2", ObjectiveType.NPC_GIVE_MISSION, CX, y);
        give.setParam("npc_name", ""); give.setParam("dialog", "Finde heraus was in der Stadt vorgeht."); y += STEP;
        ScenarioObjective tracking = new ScenarioObjective("o3", ObjectiveType.MISSION_TRACKING, CX, y);
        tracking.setParam("tracking_key", "npc_talked"); tracking.setParam("target_amount", "3"); y += STEP;
        ScenarioObjective complete = new ScenarioObjective("o4", ObjectiveType.NPC_COMPLETE_MISSION, CX, y);
        complete.setParam("npc_name", ""); complete.setParam("dialog", "Gute Arbeit. Du hast mir sehr geholfen."); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "350"); reward.setParam("money", "2500");

        start.setNextObjectiveId("o1"); info.setNextObjectiveId("o2"); give.setNextObjectiveId("o3");
        tracking.setNextObjectiveId("o4"); complete.setNextObjectiveId("r0");

        objs.add(start); objs.add(info); objs.add(give); objs.add(tracking); objs.add(complete); objs.add(reward);
        return new MissionScenario("ptpl_informant", "Informant",
                "Nebenmission: Informationen durch NPC-Gespraeche sammeln", 1, 0, false, "STORY_SIDE", objs);
    }

    public static MissionScenario playerTerritoriumAuftrag() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective info = new ScenarioObjective("o1", ObjectiveType.MISSION_INFO, CX, y);
        info.setParam("title", "Territorium sichern"); info.setParam("description", "Erweitere den Einflussbereich der Gang."); info.setParam("npc_giver", ""); y += STEP;
        ScenarioObjective prereq = new ScenarioObjective("o2", ObjectiveType.MISSION_PREREQ, CX, y);
        prereq.setParam("prereq_id", "haupt_erster_kontakt"); prereq.setParam("fail_message", "Schliess zuerst den ersten Kontakt ab."); y += STEP;
        ScenarioObjective give = new ScenarioObjective("o3", ObjectiveType.NPC_GIVE_MISSION, CX, y);
        give.setParam("npc_name", ""); give.setParam("dialog", "Wir brauchen mehr Kontrolle. Sichere drei Gebiete."); y += STEP;
        ScenarioObjective tracking = new ScenarioObjective("o4", ObjectiveType.MISSION_TRACKING, CX, y);
        tracking.setParam("tracking_key", "territory_captured"); tracking.setParam("target_amount", "3"); y += STEP;
        ScenarioObjective complete = new ScenarioObjective("o5", ObjectiveType.NPC_COMPLETE_MISSION, CX, y);
        complete.setParam("npc_name", ""); complete.setParam("dialog", "Ausgezeichnet. Die Stadt gehoert uns."); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "1200"); reward.setParam("money", "8000");

        start.setNextObjectiveId("o1"); info.setNextObjectiveId("o2"); prereq.setNextObjectiveId("o3");
        give.setNextObjectiveId("o4"); tracking.setNextObjectiveId("o5"); complete.setNextObjectiveId("r0");

        objs.add(start); objs.add(info); objs.add(prereq); objs.add(give); objs.add(tracking); objs.add(complete); objs.add(reward);
        return new MissionScenario("ptpl_territory", "Territorium-Auftrag",
                "Hauptmission mit Voraussetzung: Gebiete sichern", 2, 0, false, "STORY_MAIN", objs);
    }

    public static MissionScenario playerNotfallAuftrag() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective notify1 = new ScenarioObjective("o1", ObjectiveType.PLAYER_NOTIFY, CX, y);
        notify1.setParam("message", "ALARM! Treffe sofort am Treffpunkt ein."); notify1.setParam("color", "red"); y += STEP;
        ScenarioObjective goTo = new ScenarioObjective("o2", ObjectiveType.GOTO_LOCATION, CX, y);
        goTo.setParam("x", "50"); goTo.setParam("y", "64"); goTo.setParam("z", "50"); goTo.setParam("radius", "5"); y += STEP;
        ScenarioObjective failCond = new ScenarioObjective("o3", ObjectiveType.MISSION_FAIL_COND, CX, y);
        failCond.setParam("condition", "player_died"); failCond.setParam("fail_message", "Du bist gefallen!"); y += STEP;
        ScenarioObjective collect = new ScenarioObjective("o4", ObjectiveType.COLLECT_ITEMS, CX, y);
        collect.setParam("item_id", "minecraft:diamond"); collect.setParam("amount", "3"); y += STEP;
        ScenarioObjective notify2 = new ScenarioObjective("o5", ObjectiveType.PLAYER_NOTIFY, CX, y);
        notify2.setParam("message", "Gut gemacht! Verschwinde schnell."); notify2.setParam("color", "green"); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "600"); reward.setParam("money", "5000");

        start.setNextObjectiveId("o1"); notify1.setNextObjectiveId("o2"); goTo.setNextObjectiveId("o3");
        failCond.setNextObjectiveId("o4"); collect.setNextObjectiveId("o5"); notify2.setNextObjectiveId("r0");

        objs.add(start); objs.add(notify1); objs.add(goTo); objs.add(failCond); objs.add(collect); objs.add(notify2); objs.add(reward);
        return new MissionScenario("ptpl_emergency", "Notfall-Auftrag",
                "Zeitkritische Nebenmission mit Abbruchbedingung und Hinweisen", 2, 0, false, "STORY_SIDE", objs);
    }

    public static MissionScenario playerKampfAuftrag() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective info = new ScenarioObjective("o1", ObjectiveType.MISSION_INFO, CX, y);
        info.setParam("title", "Bedrohung ausschalten"); info.setParam("description", "Raeume das feindliche Lager aus."); info.setParam("npc_giver", ""); y += STEP;
        ScenarioObjective give = new ScenarioObjective("o2", ObjectiveType.NPC_GIVE_MISSION, CX, y);
        give.setParam("npc_name", ""); give.setParam("dialog", "Diese Kerle machen uns das Leben schwer. Kuemmer dich drum."); y += STEP;
        ScenarioObjective tracking = new ScenarioObjective("o3", ObjectiveType.MISSION_TRACKING, CX, y);
        tracking.setParam("tracking_key", "enemy_killed"); tracking.setParam("target_amount", "10"); y += STEP;
        ScenarioObjective failCond = new ScenarioObjective("o4", ObjectiveType.MISSION_FAIL_COND, CX, y);
        failCond.setParam("condition", "player_died"); failCond.setParam("fail_message", "Mission fehlgeschlagen - du wurdest ausgeschaltet."); y += STEP;
        ScenarioObjective complete = new ScenarioObjective("o5", ObjectiveType.NPC_COMPLETE_MISSION, CX, y);
        complete.setParam("npc_name", ""); complete.setParam("dialog", "Sauber erledigt. Hier ist deine Auszahlung."); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "900"); reward.setParam("money", "7000");

        start.setNextObjectiveId("o1"); info.setNextObjectiveId("o2"); give.setNextObjectiveId("o3");
        tracking.setNextObjectiveId("o4"); failCond.setNextObjectiveId("o5"); complete.setNextObjectiveId("r0");

        objs.add(start); objs.add(info); objs.add(give); objs.add(tracking); objs.add(failCond); objs.add(complete); objs.add(reward);
        return new MissionScenario("ptpl_combat", "Kampf-Auftrag",
                "Hauptmission mit Gegnern besiegen und Abbruchbedingung", 3, 0, false, "STORY_MAIN", objs);
    }

    public static MissionScenario playerSchmuggler() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective info = new ScenarioObjective("o1", ObjectiveType.MISSION_INFO, CX, y);
        info.setParam("title", "Schmuggelauftrag"); info.setParam("description", "Bringe Waren unentdeckt durch die Stadt."); info.setParam("npc_giver", ""); y += STEP;
        ScenarioObjective give = new ScenarioObjective("o2", ObjectiveType.NPC_GIVE_MISSION, CX, y);
        give.setParam("npc_name", ""); give.setParam("dialog", "Fuer jeden gelieferten Auftrag gibt es Kohle."); y += STEP;
        ScenarioObjective tracking = new ScenarioObjective("o3", ObjectiveType.MISSION_TRACKING, CX, y);
        tracking.setParam("tracking_key", "package_delivered"); tracking.setParam("target_amount", "5"); y += STEP;
        ScenarioObjective complete = new ScenarioObjective("o4", ObjectiveType.NPC_COMPLETE_MISSION, CX, y);
        complete.setParam("npc_name", ""); complete.setParam("dialog", "Alle Pakete angekommen. Sauber."); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "700"); reward.setParam("money", "6000");

        start.setNextObjectiveId("o1"); info.setNextObjectiveId("o2"); give.setNextObjectiveId("o3");
        tracking.setNextObjectiveId("o4"); complete.setNextObjectiveId("r0");

        objs.add(start); objs.add(info); objs.add(give); objs.add(tracking); objs.add(complete); objs.add(reward);
        return new MissionScenario("ptpl_smuggler", "Schmuggler",
                "Nebenmission: Pakete liefern und Belohnung kassieren", 2, 0, false, "STORY_SIDE", objs);
    }

    public static MissionScenario playerDetektiv() {
        List<ScenarioObjective> objs = new ArrayList<>();
        int y = 20;

        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, CX, y); y += STEP;
        ScenarioObjective info = new ScenarioObjective("o1", ObjectiveType.MISSION_INFO, CX, y);
        info.setParam("title", "Spurensuche"); info.setParam("description", "Finde den Verraeter in der Gang."); info.setParam("npc_giver", ""); y += STEP;
        ScenarioObjective prereq = new ScenarioObjective("o2", ObjectiveType.MISSION_PREREQ, CX, y);
        prereq.setParam("prereq_id", "haupt_lieferung_01"); prereq.setParam("fail_message", "Du musst zuerst beweisen, dass du vertrauenswuerdig bist."); y += STEP;
        ScenarioObjective give = new ScenarioObjective("o3", ObjectiveType.NPC_GIVE_MISSION, CX, y);
        give.setParam("npc_name", ""); give.setParam("dialog", "Jemand hat uns verraten. Finde raus wer."); y += STEP;
        ScenarioObjective talk = new ScenarioObjective("o4", ObjectiveType.MISSION_TRACKING, CX, y);
        talk.setParam("tracking_key", "npc_talked"); talk.setParam("target_amount", "5"); y += STEP;
        ScenarioObjective notify = new ScenarioObjective("o5", ObjectiveType.PLAYER_NOTIFY, CX, y);
        notify.setParam("message", "Du hast genug Informationen gesammelt."); notify.setParam("color", "aqua"); y += STEP;
        ScenarioObjective complete = new ScenarioObjective("o6", ObjectiveType.NPC_COMPLETE_MISSION, CX, y);
        complete.setParam("npc_name", ""); complete.setParam("dialog", "Das wusste ich nicht. Du bist unser Mann."); y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "1500"); reward.setParam("money", "12000");

        start.setNextObjectiveId("o1"); info.setNextObjectiveId("o2"); prereq.setNextObjectiveId("o3");
        give.setNextObjectiveId("o4"); talk.setNextObjectiveId("o5"); notify.setNextObjectiveId("o6"); complete.setNextObjectiveId("r0");

        objs.add(start); objs.add(info); objs.add(prereq); objs.add(give); objs.add(talk); objs.add(notify); objs.add(complete); objs.add(reward);
        return new MissionScenario("ptpl_detective", "Detektiv",
                "Komplexe Hauptmission: Verraeter aufdecken mit Voraussetzung und Hinweisen", 3, 0, false, "STORY_MAIN", objs);
    }
}
