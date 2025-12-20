package de.rolandsw.schedulemc.territory.network;

import de.rolandsw.schedulemc.client.screen.TerritoryMapEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Öffnet den Territory Map Editor auf dem Client
 */
public class OpenMapEditorPacket {

    public OpenMapEditorPacket() {
    }

    public static void encode(OpenMapEditorPacket msg, FriendlyByteBuf buf) {
        // Keine Daten zu senden
    }

    public static OpenMapEditorPacket decode(FriendlyByteBuf buf) {
        return new OpenMapEditorPacket();
    }

    public static void handle(OpenMapEditorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();
                mc.setScreen(new TerritoryMapEditorScreen());
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
