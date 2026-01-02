package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.data.BankCategory;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.BankerMenu;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * Packet zum Öffnen des Banker-Menüs
 */
public class OpenBankerMenuPacket {
    private final int npcEntityId;

    public OpenBankerMenuPacket(int npcEntityId) {
        this.npcEntityId = npcEntityId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(npcEntityId);
    }

    public static OpenBankerMenuPacket decode(FriendlyByteBuf buf) {
        return new OpenBankerMenuPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(npcEntityId);
            if (entity instanceof CustomNPCEntity npc) {
                // Prüfe ob es ein Bank-NPC mit Banker-Kategorie ist
                if (npc.getNpcType() == NPCType.BANK && npc.getBankCategory() == BankCategory.BANKER) {
                    NetworkHooks.openScreen(player, new SimpleMenuProvider(
                        (id, playerInventory, p) -> new BankerMenu(id, playerInventory, npc),
                        Component.literal("Banker")
                    ), buf -> {
                        buf.writeInt(npc.getId());
                    });
                } else {
                    player.sendSystemMessage(Component.literal("§cDieser NPC ist kein Banker!"));
                }
            }
        });
    }
}
