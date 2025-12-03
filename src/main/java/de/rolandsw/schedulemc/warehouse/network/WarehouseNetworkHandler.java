package de.rolandsw.schedulemc.warehouse.network;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.warehouse.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für Warehouse-Packets
 */
public class WarehouseNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ScheduleMC.MOD_ID, "warehouse_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    /**
     * Registriert alle Warehouse-Packets
     */
    public static void register() {
        // Modify Slot (add/remove items)
        INSTANCE.messageBuilder(ModifySlotPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(ModifySlotPacket::decode)
            .encoder(ModifySlotPacket::encode)
            .consumerMainThread(ModifySlotPacket::handle)
            .add();

        // Clear Slot
        INSTANCE.messageBuilder(ClearSlotPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(ClearSlotPacket::decode)
            .encoder(ClearSlotPacket::encode)
            .consumerMainThread(ClearSlotPacket::handle)
            .add();

        // Auto-Fill All Slots
        INSTANCE.messageBuilder(AutoFillPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(AutoFillPacket::decode)
            .encoder(AutoFillPacket::encode)
            .consumerMainThread(AutoFillPacket::handle)
            .add();

        // Update Settings
        INSTANCE.messageBuilder(UpdateSettingsPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(UpdateSettingsPacket::decode)
            .encoder(UpdateSettingsPacket::encode)
            .consumerMainThread(UpdateSettingsPacket::handle)
            .add();

        // Remove Seller
        INSTANCE.messageBuilder(RemoveSellerPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(RemoveSellerPacket::decode)
            .encoder(RemoveSellerPacket::encode)
            .consumerMainThread(RemoveSellerPacket::handle)
            .add();

        // Add Seller
        INSTANCE.messageBuilder(AddSellerPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(AddSellerPacket::decode)
            .encoder(AddSellerPacket::encode)
            .consumerMainThread(AddSellerPacket::handle)
            .add();
    }
}
