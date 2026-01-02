package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.data.BankCategory;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.BoerseMenu;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * Packet zum Öffnen des Börsen-Menüs
 */
public class OpenBoerseMenuPacket {
    private final int npcEntityId;

    public OpenBoerseMenuPacket(int npcEntityId) {
        this.npcEntityId = npcEntityId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(npcEntityId);
    }

    public static OpenBoerseMenuPacket decode(FriendlyByteBuf buf) {
        return new OpenBoerseMenuPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(npcEntityId);
            if (entity instanceof CustomNPCEntity npc) {
                // Prüfe ob es ein Bank-NPC mit Börse-Kategorie ist
                if (npc.getNpcType() == NPCType.BANK && npc.getBankCategory() == BankCategory.BOERSE) {
                    NetworkHooks.openScreen(player, new SimpleMenuProvider(
                        (id, playerInventory, p) -> new BoerseMenu(id, playerInventory, npc),
                        Component.literal("Börsenmakler")
                    ), buf -> {
                        buf.writeInt(npc.getId());
                    });
                } else {
                    player.sendSystemMessage(Component.literal("§cDieser NPC ist kein Börsenmakler!"));
                }
            }
        });
    }
}
