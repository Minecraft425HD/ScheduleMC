package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.data.BankCategory;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.CreditAdvisorMenu;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * Packet zum Öffnen des Kreditberater-Menüs
 * Client → Server
 */
public class OpenCreditAdvisorMenuPacket {
    private final int entityId;

    public OpenCreditAdvisorMenuPacket(int entityId) {
        this.entityId = entityId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public static OpenCreditAdvisorMenuPacket decode(FriendlyByteBuf buf) {
        return new OpenCreditAdvisorMenuPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Finde NPC Entity
            if (player.level().getEntity(entityId) instanceof CustomNPCEntity npc) {
                // Prüfe ob NPC ein Kreditberater ist
                if (npc.getNpcType() != NPCType.BANK ||
                    npc.getBankCategory() != BankCategory.KREDITBERATER) {
                    player.sendSystemMessage(Component.literal(
                        "§c§lFehler: §7Dieser NPC ist kein Kreditberater!"
                    ));
                    return;
                }

                // Prüfe Distanz
                if (player.distanceToSqr(npc) > 64.0D) {
                    player.sendSystemMessage(Component.literal(
                        "§c§lFehler: §7Du bist zu weit vom Kreditberater entfernt!"
                    ));
                    return;
                }

                // Öffne Kreditberater-Menu
                NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (id, playerInventory, p) -> new CreditAdvisorMenu(id, playerInventory, npc),
                    Component.literal("Kreditberater")
                ), buf -> {
                    buf.writeInt(npc.getId());
                });
            }
        });
    }
}
