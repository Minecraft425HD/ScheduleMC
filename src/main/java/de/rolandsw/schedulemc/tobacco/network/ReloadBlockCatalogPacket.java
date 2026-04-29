package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.economy.BlockShopCatalog;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C→S Packet: Operator signalisiert dem Server, den BlockShopCatalog
 * aus der gespeicherten Config neu zu laden.
 * Wird vom ProductionBlockCatalogScreen nach dem Speichern gesendet.
 */
public class ReloadBlockCatalogPacket {

    public ReloadBlockCatalogPacket() {}

    public void encode(FriendlyByteBuf buf) {
        // kein Payload nötig
    }

    public static ReloadBlockCatalogPacket decode(FriendlyByteBuf buf) {
        return new ReloadBlockCatalogPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            if (player.hasPermissions(2) || player.getServer() != null && !player.getServer().isDedicatedServer()) {
                BlockShopCatalog.getInstance().applyConfig();
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                        "message.block_catalog.reloaded"
                    )
                );
            }
        });
    }
}
