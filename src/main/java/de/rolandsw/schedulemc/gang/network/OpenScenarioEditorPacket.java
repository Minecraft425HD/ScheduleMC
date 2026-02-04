package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.client.screen.apps.ScenarioEditorScreen;
import de.rolandsw.schedulemc.gang.scenario.MissionScenario;
import de.rolandsw.schedulemc.gang.scenario.ScenarioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Server -> Client: Oeffnet den Szenario-Editor mit allen gespeicherten Szenarien.
 */
public class OpenScenarioEditorPacket {

    private final String scenariosJson;

    public OpenScenarioEditorPacket(String scenariosJson) {
        this.scenariosJson = scenariosJson;
    }

    public static void encode(OpenScenarioEditorPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.scenariosJson, 262144); // Max 256KB
    }

    public static OpenScenarioEditorPacket decode(FriendlyByteBuf buf) {
        return new OpenScenarioEditorPacket(buf.readUtf(262144));
    }

    public static void handle(OpenScenarioEditorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            List<MissionScenario> scenarios = ScenarioManager.listFromJson(msg.scenariosJson);
            net.minecraft.client.Minecraft.getInstance().setScreen(
                    new ScenarioEditorScreen(scenarios)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
