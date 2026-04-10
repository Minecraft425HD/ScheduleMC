package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Update der Warehouse-Einstellungen
 */
public class UpdateSettingsPacket {

    private final BlockPos pos;
    private final String shopId;
    private final boolean autoDeliveryEnabled;
    private final int deliveryIntervalDays;
    private final int defaultSlotCapacity;
    private final boolean adminOnly;
    private final boolean sellersCanView;

    public UpdateSettingsPacket(BlockPos pos, String shopId,
                                boolean autoDeliveryEnabled, int deliveryIntervalDays,
                                int defaultSlotCapacity, boolean adminOnly, boolean sellersCanView) {
        this.pos = pos;
        this.shopId = shopId;
        this.autoDeliveryEnabled = autoDeliveryEnabled;
        this.deliveryIntervalDays = deliveryIntervalDays;
        this.defaultSlotCapacity = defaultSlotCapacity;
        this.adminOnly = adminOnly;
        this.sellersCanView = sellersCanView;
    }

    public static void encode(UpdateSettingsPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeBoolean(msg.shopId != null);
        if (msg.shopId != null) {
            buffer.writeUtf(msg.shopId);
        }
        buffer.writeBoolean(msg.autoDeliveryEnabled);
        buffer.writeInt(msg.deliveryIntervalDays);
        buffer.writeInt(msg.defaultSlotCapacity);
        buffer.writeBoolean(msg.adminOnly);
        buffer.writeBoolean(msg.sellersCanView);
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static UpdateSettingsPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        String shopId = buffer.readBoolean() ? buffer.readUtf(64) : null; // Shop ID max 64 chars
        boolean autoDelivery = buffer.readBoolean();
        int intervalDays = Math.max(1, Math.min(365, buffer.readInt()));
        int slotCapacity = Math.max(1, Math.min(100000, buffer.readInt()));
        boolean adminOnly = buffer.readBoolean();
        boolean sellersCanView = buffer.readBoolean();
        return new UpdateSettingsPacket(pos, shopId, autoDelivery, intervalDays, slotCapacity, adminOnly, sellersCanView);
    }

    public static void handle(UpdateSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleAdminPacket(ctx, 2, player -> {
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) return;

            warehouse.setShopId(msg.shopId);
            warehouse.setAutoDeliveryEnabled(msg.autoDeliveryEnabled);
            warehouse.setDeliveryIntervalDays(msg.deliveryIntervalDays);
            warehouse.setDefaultSlotCapacity(msg.defaultSlotCapacity);
            warehouse.setAdminOnly(msg.adminOnly);
            warehouse.setSellersCanView(msg.sellersCanView);
            warehouse.setChanged();

            player.sendSystemMessage(Component.translatable("message.warehouse.settings_updated"));
        });
    }
}
