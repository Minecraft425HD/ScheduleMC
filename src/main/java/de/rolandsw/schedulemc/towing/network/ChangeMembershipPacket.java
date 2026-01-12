package de.rolandsw.schedulemc.towing.network;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.towing.MembershipManager;
import de.rolandsw.schedulemc.towing.MembershipTier;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from client to server to change membership tier
 */
public class ChangeMembershipPacket {
    private final int tierOrdinal;

    public ChangeMembershipPacket(MembershipTier tier) {
        this.tierOrdinal = tier.ordinal();
    }

    private ChangeMembershipPacket(int tierOrdinal) {
        this.tierOrdinal = tierOrdinal;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(tierOrdinal);
    }

    public static ChangeMembershipPacket decode(FriendlyByteBuf buf) {
        return new ChangeMembershipPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, sender -> {
            MembershipTier newTier = MembershipTier.fromOrdinal(tierOrdinal);

            if (newTier == MembershipTier.NONE) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.cannot_select_none"),
                    false
                );
                return;
            }

            boolean success = MembershipManager.changeMembership(sender.getUUID(), newTier, sender.getServer());

            if (success) {
                sender.displayClientMessage(
                    Component.translatable("towing.membership.changed",
                        Component.translatable(newTier.getTranslationKey()),
                        String.format("%.0f", newTier.getMonthlyFee())),
                    false
                );
                ScheduleMC.LOGGER.info("Player {} changed membership to {}", sender.getName().getString(), newTier.name());
            } else {
                sender.displayClientMessage(
                    Component.translatable("towing.error.membership_change_failed"),
                    false
                );
                ScheduleMC.LOGGER.warn("Failed to change membership for player {} to {}",
                    sender.getName().getString(), newTier.name());
            }
        });
    }
}
