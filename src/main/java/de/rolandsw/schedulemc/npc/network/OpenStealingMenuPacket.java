package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.StealingMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * Packet zum Öffnen des Bestehlen-Minigames
 */
public class OpenStealingMenuPacket {
    private final int npcEntityId;

    public OpenStealingMenuPacket(int npcEntityId) {
        this.npcEntityId = npcEntityId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(npcEntityId);
    }

    public static OpenStealingMenuPacket decode(FriendlyByteBuf buf) {
        return new OpenStealingMenuPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(npcEntityId);
                if (entity instanceof CustomNPCEntity npc) {
                    // Prüfe ob es kein Polizist ist
                    if (npc.getNpcType() != NPCType.POLIZEI) {
                        // Öffne Bestehlen-GUI
                        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                            (id, playerInventory, p) -> new StealingMenu(id, playerInventory, npc),
                            Component.literal("§cBestehle " + npc.getNpcName())
                        ), buf -> {
                            buf.writeInt(npc.getId());
                        });
                    } else {
                        player.displayClientMessage(Component.literal("§c✗ Du kannst keine Polizisten bestehlen!"), true);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
