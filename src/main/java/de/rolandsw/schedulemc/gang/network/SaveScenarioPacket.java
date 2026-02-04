package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.gang.scenario.MissionScenario;
import de.rolandsw.schedulemc.gang.scenario.ScenarioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> Server: Speichert ein Szenario aus dem Editor.
 *
 * Nur Spieler mit OP Level 2+ koennen Szenarien speichern.
 */
public class SaveScenarioPacket {

    private final String scenarioJson;

    public SaveScenarioPacket(String scenarioJson) {
        this.scenarioJson = scenarioJson;
    }

    public static void encode(SaveScenarioPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.scenarioJson, 262144);
    }

    public static SaveScenarioPacket decode(FriendlyByteBuf buf) {
        return new SaveScenarioPacket(buf.readUtf(262144));
    }

    public static void handle(SaveScenarioPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Nur OPs duerfen Szenarien speichern
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal(
                        "\u00A7c[Szenario-Editor] Keine Berechtigung!"));
                return;
            }

            ScenarioManager manager = ScenarioManager.getInstance();
            if (manager == null) {
                player.sendSystemMessage(Component.literal(
                        "\u00A7c[Szenario-Editor] Manager nicht initialisiert!"));
                return;
            }

            try {
                MissionScenario scenario = ScenarioManager.fromJson(msg.scenarioJson);
                manager.saveScenario(scenario);
                player.sendSystemMessage(Component.literal(
                        "\u00A7a[Szenario-Editor] '" + scenario.getName() + "' gespeichert! ("
                        + scenario.getStepCount() + " Phasen, "
                        + scenario.getDifficultyStars() + ")"));
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal(
                        "\u00A7c[Szenario-Editor] Fehler beim Speichern: " + e.getMessage()));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
