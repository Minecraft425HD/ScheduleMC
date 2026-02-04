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
     * Gibt alle verfuegbaren Vorlagen zurueck.
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
        return templates;
    }

    /**
     * Gibt die Vorlagen-Namen fuer das Dropdown zurueck.
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
                "Schutzgeld"
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
        rob.setParam("difficulty", "3"); rob.setParam("time_limit", "180");
        y += STEP;
        ScenarioObjective crack = new ScenarioObjective("o3", ObjectiveType.CRACK_SAFE, CX, y);
        crack.setParam("difficulty", "3"); crack.setParam("time_limit", "60");
        y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o4", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("center_x", "150"); escape.setParam("center_y", "64");
        escape.setParam("center_z", "200"); escape.setParam("radius", "100");
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
        rob.setParam("difficulty", "1"); rob.setParam("time_limit", "90");
        y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o3", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("center_x", "80"); escape.setParam("center_y", "64");
        escape.setParam("center_z", "50"); escape.setParam("radius", "50");
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
        rob.setParam("difficulty", "3"); rob.setParam("time_limit", "120");
        y += STEP;
        ScenarioObjective collect = new ScenarioObjective("o4", ObjectiveType.COLLECT_ITEMS, CX, y);
        collect.setParam("item_id", "minecraft:diamond"); collect.setParam("amount", "5");
        y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o5", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("center_x", "200"); escape.setParam("center_y", "64");
        escape.setParam("center_z", "300"); escape.setParam("radius", "80");
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
        goNPC.setParam("npc_type", "WAREHOUSE_WORKER");
        y += STEP;
        ScenarioObjective collect = new ScenarioObjective("o2", ObjectiveType.COLLECT_ITEMS, CX, y);
        collect.setParam("item_id", "minecraft:paper"); collect.setParam("amount", "3");
        y += STEP;
        ScenarioObjective deliver = new ScenarioObjective("o3", ObjectiveType.DELIVER_ITEM, CX, y);
        deliver.setParam("item_id", "minecraft:paper");
        deliver.setParam("target_x", "300"); deliver.setParam("target_y", "64"); deliver.setParam("target_z", "100");
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
        deliver.setParam("item_id", "minecraft:sugar");
        deliver.setParam("target_x", "400"); deliver.setParam("target_y", "64"); deliver.setParam("target_z", "-100");
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
        rob.setParam("difficulty", "1"); rob.setParam("time_limit", "60");
        y += STEP;
        ScenarioObjective escape = new ScenarioObjective("o3", ObjectiveType.ESCAPE_ZONE, CX, y);
        escape.setParam("center_x", "50"); escape.setParam("center_y", "64");
        escape.setParam("center_z", "50"); escape.setParam("radius", "40");
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
        defend.setParam("center_x", "0"); defend.setParam("center_y", "64");
        defend.setParam("center_z", "0"); defend.setParam("duration", "300");
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
        goNPC.setParam("npc_type", "SHOPKEEPER");
        y += STEP;
        ScenarioObjective interact = new ScenarioObjective("o2", ObjectiveType.INTERACT_NPC, CX, y);
        interact.setParam("npc_type", "SHOPKEEPER"); interact.setParam("dialog_id", "extortion");
        y += STEP;
        ScenarioObjective earn = new ScenarioObjective("o3", ObjectiveType.EARN_MONEY, CX, y);
        earn.setParam("amount", "2000");
        y += STEP;
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, CX, y);
        reward.setParam("xp", "120"); reward.setParam("money", "4000");

        start.setNextObjectiveId("o1");
        goNPC.setNextObjectiveId("o2");
        interact.setNextObjectiveId("o3");
        earn.setNextObjectiveId("r0");

        objs.add(start); objs.add(goNPC); objs.add(interact); objs.add(earn); objs.add(reward);

        return new MissionScenario("tpl_extort", "Schutzgeld",
                "Schutzgeld von Ladenbesitzern eintreiben",
                2, 3, false, "DAILY", objs);
    }
}
