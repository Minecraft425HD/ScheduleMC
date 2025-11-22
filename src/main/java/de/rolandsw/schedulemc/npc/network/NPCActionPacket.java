package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für NPC Aktionen (Dialog, Shop, etc.)
 */
public class NPCActionPacket {
    private final int entityId;
    private final Action action;

    public enum Action {
        NEXT_DIALOG,
        OPEN_SHOP_BUY,
        OPEN_SHOP_SELL
    }

    public NPCActionPacket(int entityId, Action action) {
        this.entityId = entityId;
        this.action = action;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeEnum(action);
    }

    public static NPCActionPacket decode(FriendlyByteBuf buf) {
        return new NPCActionPacket(
            buf.readInt(),
            buf.readEnum(Action.class)
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(entityId);
                if (entity instanceof CustomNPCEntity npc) {
                    switch (action) {
                        case NEXT_DIALOG:
                            npc.getNpcData().nextDialog();
                            break;
                        case OPEN_SHOP_BUY:
                            // TODO: Implementiere Shop Buy Logic
                            break;
                        case OPEN_SHOP_SELL:
                            // TODO: Implementiere Shop Sell Logic
                            break;
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
