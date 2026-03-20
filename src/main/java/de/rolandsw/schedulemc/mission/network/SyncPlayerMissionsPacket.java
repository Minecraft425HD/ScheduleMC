package de.rolandsw.schedulemc.mission.network;

import de.rolandsw.schedulemc.client.screen.apps.ScenarioEditorScreen;
import de.rolandsw.schedulemc.gang.network.OpenScenarioEditorPacket;
import de.rolandsw.schedulemc.gang.scenario.MissionScenario;
import de.rolandsw.schedulemc.gang.scenario.ScenarioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server -> Client: Sendet alle Spieler-Missionsszenarien (STORY_*) zum Client.
 * Wird als Antwort auf RequestPlayerMissionsPacket gesendet.
 * Aktualisiert den ScenarioEditorScreen in den Spieler-Modus.
 */
public class SyncPlayerMissionsPacket {

    private final String scenariosJson;
    private final List<String> npcNames;
    private final List<OpenScenarioEditorPacket.PlotInfo> plots;
    private final List<OpenScenarioEditorPacket.LockInfo> locks;

    public SyncPlayerMissionsPacket(String scenariosJson, List<String> npcNames,
                                     List<OpenScenarioEditorPacket.PlotInfo> plots,
                                     List<OpenScenarioEditorPacket.LockInfo> locks) {
        this.scenariosJson = scenariosJson;
        this.npcNames = npcNames;
        this.plots = plots;
        this.locks = locks;
    }

    public static void encode(SyncPlayerMissionsPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.scenariosJson, 524288);

        buf.writeInt(msg.npcNames.size());
        for (String name : msg.npcNames) buf.writeUtf(name, 100);

        buf.writeInt(msg.plots.size());
        for (OpenScenarioEditorPacket.PlotInfo p : msg.plots) {
            buf.writeUtf(p.id(), 100);
            buf.writeUtf(p.name(), 100);
            buf.writeUtf(p.type(), 50);
            buf.writeInt(p.x()); buf.writeInt(p.y()); buf.writeInt(p.z());
        }

        buf.writeInt(msg.locks.size());
        for (OpenScenarioEditorPacket.LockInfo l : msg.locks) {
            buf.writeUtf(l.lockId(), 20);
            buf.writeUtf(l.lockType(), 30);
            buf.writeUtf(l.ownerName(), 100);
            buf.writeInt(l.x()); buf.writeInt(l.y()); buf.writeInt(l.z());
            buf.writeBoolean(l.hasCode());
        }
    }

    public static SyncPlayerMissionsPacket decode(FriendlyByteBuf buf) {
        String json = buf.readUtf(524288);

        int npcCount = buf.readInt();
        List<String> npcNames = new ArrayList<>(npcCount);
        for (int i = 0; i < npcCount; i++) npcNames.add(buf.readUtf(100));

        int plotCount = buf.readInt();
        List<OpenScenarioEditorPacket.PlotInfo> plots = new ArrayList<>(plotCount);
        for (int i = 0; i < plotCount; i++) {
            plots.add(new OpenScenarioEditorPacket.PlotInfo(
                    buf.readUtf(100), buf.readUtf(100), buf.readUtf(50),
                    buf.readInt(), buf.readInt(), buf.readInt()));
        }

        int lockCount = buf.readInt();
        List<OpenScenarioEditorPacket.LockInfo> locks = new ArrayList<>(lockCount);
        for (int i = 0; i < lockCount; i++) {
            locks.add(new OpenScenarioEditorPacket.LockInfo(
                    buf.readUtf(20), buf.readUtf(30), buf.readUtf(100),
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean()));
        }

        return new SyncPlayerMissionsPacket(json, npcNames, plots, locks);
    }

    public static void handle(SyncPlayerMissionsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg))
        );
        ctx.get().setPacketHandled(true);
    }

    private static void handleClient(SyncPlayerMissionsPacket msg) {
        List<MissionScenario> scenarios = ScenarioManager.listFromJson(msg.scenariosJson);
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.screen instanceof ScenarioEditorScreen editor) {
            // Aktualisiere den bereits offenen Editor mit Spieler-Szenarien
            editor.loadPlayerScenarios(scenarios, msg.npcNames, msg.plots, msg.locks);
        } else {
            // Oeffne Editor direkt im Spieler-Modus
            mc.setScreen(new ScenarioEditorScreen(
                    new ArrayList<>(), scenarios, msg.npcNames, msg.plots, msg.locks));
        }
    }
}
