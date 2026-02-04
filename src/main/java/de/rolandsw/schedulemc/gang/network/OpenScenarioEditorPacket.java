package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.client.screen.apps.ScenarioEditorScreen;
import de.rolandsw.schedulemc.gang.scenario.MissionScenario;
import de.rolandsw.schedulemc.gang.scenario.ScenarioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server -> Client: Oeffnet den Szenario-Editor mit allen gespeicherten Szenarien
 * plus Server-Daten fuer Dropdowns (NPC-Namen, Grundstuecke).
 */
public class OpenScenarioEditorPacket {

    private final String scenariosJson;
    private final List<String> npcNames;
    private final List<PlotInfo> plots;

    public OpenScenarioEditorPacket(String scenariosJson, List<String> npcNames, List<PlotInfo> plots) {
        this.scenariosJson = scenariosJson;
        this.npcNames = npcNames;
        this.plots = plots;
    }

    public static void encode(OpenScenarioEditorPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.scenariosJson, 262144);

        // NPC-Namen
        buf.writeInt(msg.npcNames.size());
        for (String name : msg.npcNames) {
            buf.writeUtf(name, 100);
        }

        // Grundstuecke
        buf.writeInt(msg.plots.size());
        for (PlotInfo plot : msg.plots) {
            buf.writeUtf(plot.id, 100);
            buf.writeUtf(plot.name, 100);
            buf.writeUtf(plot.type, 50);
            buf.writeInt(plot.x);
            buf.writeInt(plot.y);
            buf.writeInt(plot.z);
        }
    }

    public static OpenScenarioEditorPacket decode(FriendlyByteBuf buf) {
        String json = buf.readUtf(262144);

        int npcCount = buf.readInt();
        List<String> npcNames = new ArrayList<>(npcCount);
        for (int i = 0; i < npcCount; i++) {
            npcNames.add(buf.readUtf(100));
        }

        int plotCount = buf.readInt();
        List<PlotInfo> plots = new ArrayList<>(plotCount);
        for (int i = 0; i < plotCount; i++) {
            plots.add(new PlotInfo(
                    buf.readUtf(100),
                    buf.readUtf(100),
                    buf.readUtf(50),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt()
            ));
        }

        return new OpenScenarioEditorPacket(json, npcNames, plots);
    }

    public static void handle(OpenScenarioEditorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            List<MissionScenario> scenarios = ScenarioManager.listFromJson(msg.scenariosJson);
            net.minecraft.client.Minecraft.getInstance().setScreen(
                    new ScenarioEditorScreen(scenarios, msg.npcNames, msg.plots)
            );
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Grundstueck-Info fuer Client-Dropdowns.
     */
    public record PlotInfo(String id, String name, String type, int x, int y, int z) {
        public String getDisplayLabel() {
            return name + " (" + type + ")";
        }
    }
}
