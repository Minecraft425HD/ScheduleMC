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
 * plus Server-Daten fuer Dropdowns (NPC-Namen, Grundstuecke, Schloesser).
 */
public class OpenScenarioEditorPacket {

    private final String scenariosJson;
    private final List<String> npcNames;
    private final List<PlotInfo> plots;
    private final List<LockInfo> locks;

    public OpenScenarioEditorPacket(String scenariosJson, List<String> npcNames, List<PlotInfo> plots, List<LockInfo> locks) {
        this.scenariosJson = scenariosJson;
        this.npcNames = npcNames;
        this.plots = plots;
        this.locks = locks;
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

        // Schloesser
        buf.writeInt(msg.locks.size());
        for (LockInfo lock : msg.locks) {
            buf.writeUtf(lock.lockId, 20);
            buf.writeUtf(lock.lockType, 30);
            buf.writeUtf(lock.ownerName, 100);
            buf.writeInt(lock.x);
            buf.writeInt(lock.y);
            buf.writeInt(lock.z);
            buf.writeBoolean(lock.hasCode);
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

        int lockCount = buf.readInt();
        List<LockInfo> locks = new ArrayList<>(lockCount);
        for (int i = 0; i < lockCount; i++) {
            locks.add(new LockInfo(
                    buf.readUtf(20),
                    buf.readUtf(30),
                    buf.readUtf(100),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean()
            ));
        }

        return new OpenScenarioEditorPacket(json, npcNames, plots, locks);
    }

    public static void handle(OpenScenarioEditorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            List<MissionScenario> scenarios = ScenarioManager.listFromJson(msg.scenariosJson);
            net.minecraft.client.Minecraft.getInstance().setScreen(
                    new ScenarioEditorScreen(scenarios, msg.npcNames, msg.plots, msg.locks)
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

    /**
     * Schloss-Info fuer Client-Dropdowns.
     */
    public record LockInfo(String lockId, String lockType, String ownerName, int x, int y, int z, boolean hasCode) {
        public String getDisplayLabel() {
            return lockId + " - " + lockType + " @ " + x + "," + y + "," + z;
        }
    }
}
