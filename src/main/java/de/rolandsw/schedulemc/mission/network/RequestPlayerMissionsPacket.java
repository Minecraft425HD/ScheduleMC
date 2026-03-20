package de.rolandsw.schedulemc.mission.network;

import de.rolandsw.schedulemc.gang.network.OpenScenarioEditorPacket;
import de.rolandsw.schedulemc.gang.scenario.MissionScenario;
import de.rolandsw.schedulemc.gang.scenario.ScenarioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client -> Server: Spieler-Missionsszenarien (STORY_*) anfragen.
 * Server antwortet mit SyncPlayerMissionsPacket.
 */
public class RequestPlayerMissionsPacket {

    public static void encode(RequestPlayerMissionsPacket msg, FriendlyByteBuf buf) {}

    public static RequestPlayerMissionsPacket decode(FriendlyByteBuf buf) {
        return new RequestPlayerMissionsPacket();
    }

    public static void handle(RequestPlayerMissionsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Nur OPs duerfen den Missionseditor oeffnen
            if (!player.hasPermissions(2)) return;

            ScenarioManager manager = ScenarioManager.getInstance();
            if (manager == null) {
                MissionNetworkHandler.sendToPlayer(new SyncPlayerMissionsPacket("[]",
                        new ArrayList<>(), new ArrayList<>(), new ArrayList<>()), player);
                return;
            }

            // Filtere nur STORY_* Szenarien
            List<MissionScenario> storyScenarios = new ArrayList<>();
            for (MissionScenario s : manager.getAllScenarios()) {
                String type = s.getMissionType();
                if (type != null && type.startsWith("STORY_")) {
                    storyScenarios.add(s);
                }
            }

            // Collect server data for dropdowns (same as OpenScenarioEditorPacket)
            List<String> npcNames = collectNpcNames(player.serverLevel());
            List<OpenScenarioEditorPacket.PlotInfo> plots = new ArrayList<>();
            List<OpenScenarioEditorPacket.LockInfo> locks = new ArrayList<>();

            // Serialize scenarios
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().create();
            String json = serializeScenarios(storyScenarios);

            MissionNetworkHandler.sendToPlayer(
                    new SyncPlayerMissionsPacket(json, npcNames, plots, locks), player);
        });
        ctx.get().setPacketHandled(true);
    }

    private static List<String> collectNpcNames(ServerLevel level) {
        try {
            return de.rolandsw.schedulemc.managers.NPCNameRegistry.getAllNamesSorted();
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private static String serializeScenarios(List<MissionScenario> scenarios) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (MissionScenario s : scenarios) {
            if (!first) sb.append(",");
            sb.append(de.rolandsw.schedulemc.gang.scenario.ScenarioManager.scenarioToJson(s));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
