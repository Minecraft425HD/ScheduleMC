package de.rolandsw.schedulemc.mission.network;

import de.rolandsw.schedulemc.gang.scenario.MissionScenario;
import de.rolandsw.schedulemc.gang.scenario.ObjectiveType;
import de.rolandsw.schedulemc.gang.scenario.ScenarioManager;
import de.rolandsw.schedulemc.gang.scenario.ScenarioObjective;
import de.rolandsw.schedulemc.mission.MissionCategory;
import de.rolandsw.schedulemc.mission.MissionDefinition;
import de.rolandsw.schedulemc.mission.MissionRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client -> Server: Speichert ein Spieler-Missionsszenario (STORY_MAIN / STORY_SIDE).
 *
 * Nur OPs (Level 2+) koennen Spieler-Missionen speichern.
 * Nach dem Speichern wird die MissionRegistry aus den gespeicherten Szenarien
 * neu aufgebaut, sodass die neuen Missionen sofort verfuegbar sind.
 */
public class SavePlayerMissionPacket {

    private final String scenarioJson;

    public SavePlayerMissionPacket(String scenarioJson) {
        this.scenarioJson = scenarioJson;
    }

    public static void encode(SavePlayerMissionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.scenarioJson, 524288);
    }

    public static SavePlayerMissionPacket decode(FriendlyByteBuf buf) {
        return new SavePlayerMissionPacket(buf.readUtf(524288));
    }

    public static void handle(SavePlayerMissionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal(
                        "\u00A7c[Mission-Editor] Keine Berechtigung!"));
                return;
            }

            ScenarioManager manager = ScenarioManager.getInstance();
            if (manager == null) {
                player.sendSystemMessage(Component.literal(
                        "\u00A7c[Mission-Editor] Manager nicht initialisiert!"));
                return;
            }

            try {
                MissionScenario scenario = ScenarioManager.fromJson(msg.scenarioJson);

                // Szenario in ScenarioManager speichern
                manager.saveScenario(scenario);

                // MissionRegistry aus allen gespeicherten STORY_* Szenarien neu aufbauen
                rebuildMissionRegistry(manager);

                // Feedback
                MissionDefinition def = MissionRegistry.getById(scenario.getId());
                String catLabel = def != null
                        ? (def.getCategory() == MissionCategory.HAUPT ? "Hauptmission" : "Nebenmission")
                        : "?";
                player.sendSystemMessage(Component.literal(
                        "\u00A7a[Mission-Editor] '" + scenario.getName() + "' gespeichert! "
                        + "(" + catLabel + ", " + scenario.getStepCount() + " Schritte, "
                        + scenario.getDifficultyStars() + ")"));

            } catch (Exception e) {
                player.sendSystemMessage(Component.literal(
                        "\u00A7c[Mission-Editor] Fehler: " + e.getMessage()));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // ═══════════════════════════════════════════════════════════
    // KONVERTIERUNG: MissionScenario → MissionDefinition
    // ═══════════════════════════════════════════════════════════

    /**
     * Baut die MissionRegistry aus allen gespeicherten STORY_*-Szenarien neu auf.
     */
    public static void rebuildMissionRegistry(ScenarioManager manager) {
        MissionRegistry.clearDynamic();
        for (MissionScenario scenario : manager.getAllScenarios()) {
            String type = scenario.getMissionType();
            if (type != null && type.startsWith("STORY_")) {
                MissionDefinition def = scenarioToDefinition(scenario);
                if (def != null) {
                    MissionRegistry.registerDynamic(def);
                }
            }
        }
    }

    /**
     * Konvertiert ein MissionScenario (STORY_*) in eine MissionDefinition.
     * Liest Parameter aus speziellen Story-Bloecken (MISSION_INFO, MISSION_TRACKING, MISSION_PREREQ).
     */
    public static MissionDefinition scenarioToDefinition(MissionScenario scenario) {
        String description = scenario.getDescription();
        String npcGiverName = "";
        String trackingKey = "mission_completed";
        int targetAmount = 1;
        List<String> prereqs = new ArrayList<>();

        // Bloecke auswerten
        for (ScenarioObjective obj : scenario.getObjectives()) {
            switch (obj.getType()) {
                case MISSION_INFO -> {
                    String desc = obj.getParam("description");
                    if (desc != null && !desc.isEmpty()) description = desc;
                    String npc = obj.getParam("npc_giver");
                    if (npc != null) npcGiverName = npc;
                }
                case MISSION_TRACKING -> {
                    String key = obj.getParam("tracking_key");
                    if (key != null && !key.isEmpty()) trackingKey = key;
                    String amt = obj.getParam("target_amount");
                    if (amt != null) {
                        try { targetAmount = Math.max(1, Integer.parseInt(amt)); }
                        catch (NumberFormatException ex) { targetAmount = 1; }
                    }
                }
                case MISSION_PREREQ -> {
                    String prereq = obj.getParam("prereq_id");
                    if (prereq != null && !prereq.isEmpty()) prereqs.add(prereq);
                }
                default -> { /* Andere Bloecke werden ignoriert */ }
            }
        }

        MissionCategory category = "STORY_SIDE".equals(scenario.getMissionType())
                ? MissionCategory.NEBEN : MissionCategory.HAUPT;

        return new MissionDefinition(
                scenario.getId(),
                scenario.getName(),
                description,
                category,
                scenario.getTotalXP(),
                scenario.getTotalMoney(),
                targetAmount,
                trackingKey,
                prereqs,
                null,
                npcGiverName
        );
    }
}
