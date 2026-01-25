package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
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
        NEXT_DIALOG
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
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(entityId);
            if (entity instanceof CustomNPCEntity npc && npc.getNpcData() != null) {
                if (action == Action.NEXT_DIALOG) {
                    npc.getNpcData().nextDialog();
                }
            }
        });
    }
}
