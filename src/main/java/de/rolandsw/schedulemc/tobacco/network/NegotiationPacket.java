package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.tobacco.business.NPCBusinessMetrics;
import de.rolandsw.schedulemc.tobacco.business.NPCResponse;
import de.rolandsw.schedulemc.tobacco.business.NegotiationEngine;
import de.rolandsw.schedulemc.tobacco.items.PackagedTobaccoItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Tabak-Verhandlung
 */
public class NegotiationPacket {
    private final int npcEntityId;
    private final int playerSlot;
    private final double offeredPrice;

    public NegotiationPacket(int npcEntityId, int playerSlot, double offeredPrice) {
        this.npcEntityId = npcEntityId;
        this.playerSlot = playerSlot;
        this.offeredPrice = offeredPrice;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(npcEntityId);
        buf.writeInt(playerSlot);
        buf.writeDouble(offeredPrice);
    }

    public static NegotiationPacket decode(FriendlyByteBuf buf) {
        return new NegotiationPacket(
            buf.readInt(),
            buf.readInt(),
            buf.readDouble()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Entity entity = player.level().getEntity(npcEntityId);
            if (!(entity instanceof CustomNPCEntity npc)) return;

            ItemStack playerItem = player.getInventory().getItem(playerSlot);
            if (!(playerItem.getItem() instanceof PackagedTobaccoItem)) return;

            NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);
            NPCResponse response = NegotiationEngine.handleNegotiation(
                npc, player, playerItem, offeredPrice
            );

            if (response.isAccepted()) {
                // Verkauf durchführen
                double price = offeredPrice;

                // Item entfernen
                playerItem.shrink(1);

                // Geld hinzufügen
                // TODO: Integration mit Economy-System

                // Metriken aktualisieren
                metrics.recordPurchase(
                    player.getStringUUID(),
                    PackagedTobaccoItem.getType(playerItem),
                    PackagedTobaccoItem.getQuality(playerItem),
                    PackagedTobaccoItem.getWeight(playerItem),
                    price,
                    player.level().getDayTime() / 24000
                );

                // Reputation ändern
                metrics.modifyReputation(player.getStringUUID(), response.getReputationChange());
                metrics.save();

                player.sendSystemMessage(Component.literal("§a✓ Verkauf erfolgreich! +" + String.format("%.2f", price) + "€"));
            } else {
                player.sendSystemMessage(Component.literal("§e" + response.getMessage()));
                if (response.getCounterOffer() > 0) {
                    player.sendSystemMessage(Component.literal("§7Gegenangebot: " + String.format("%.2f", response.getCounterOffer()) + "€"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
