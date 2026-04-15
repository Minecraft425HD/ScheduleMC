package de.rolandsw.schedulemc.region.network;

import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Utility-Rechnungen in der Plot-App.
 */
public class PlotUtilityBillingPacket {

    public enum Action {
        PAY_ALL,
        SET_AUTOPAY
    }

    private final Action action;
    private final boolean enabled;

    public PlotUtilityBillingPacket(Action action, boolean enabled) {
        this.action = action;
        this.enabled = enabled;
    }

    public static PlotUtilityBillingPacket payAll() {
        return new PlotUtilityBillingPacket(Action.PAY_ALL, false);
    }

    public static PlotUtilityBillingPacket setAutoPay(boolean enabled) {
        return new PlotUtilityBillingPacket(Action.SET_AUTOPAY, enabled);
    }

    public static void encode(PlotUtilityBillingPacket msg, FriendlyByteBuf buffer) {
        buffer.writeEnum(msg.action);
        buffer.writeBoolean(msg.enabled);
    }

    public static PlotUtilityBillingPacket decode(FriendlyByteBuf buffer) {
        Action action = buffer.readEnum(Action.class);
        boolean enabled = buffer.readBoolean();
        return new PlotUtilityBillingPacket(action, enabled);
    }

    public static void handle(PlotUtilityBillingPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            switch (msg.action) {
                case PAY_ALL -> {
                    double outstanding = PlotUtilityManager.getOutstandingBillsForOwner(player.getUUID());
                    if (outstanding <= 0.0) {
                        player.sendSystemMessage(Component.literal("Keine offenen Utility-Rechnungen."));
                        return;
                    }

                    double paid = PlotUtilityManager.payOutstandingBillsForOwner(player.getUUID());
                    if (paid > 0.0) {
                        player.sendSystemMessage(Component.literal(String.format("Utility-Rechnungen bezahlt: %.2f€", paid))
                            .withStyle(ChatFormatting.GREEN));
                    } else {
                        player.sendSystemMessage(Component.literal(String.format("Nicht genug Guthaben (fällig: %.2f€)", outstanding))
                            .withStyle(ChatFormatting.RED));
                    }
                }
                case SET_AUTOPAY -> {
                    PlotUtilityManager.setAutoPayForOwner(player.getUUID(), msg.enabled);
                    player.sendSystemMessage(Component.literal(msg.enabled
                            ? "AutoPay für Utility-Rechnungen aktiviert"
                            : "AutoPay für Utility-Rechnungen deaktiviert")
                        .withStyle(ChatFormatting.YELLOW));
                }
            }
        });
    }
}
