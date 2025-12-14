package de.rolandsw.schedulemc.vehicle.net;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageGaragePayment implements Message<MessageGaragePayment> {

    private UUID playerUuid;
    private UUID vehicleUuid;

    public MessageGaragePayment() {
    }

    public MessageGaragePayment(UUID playerUuid, UUID vehicleUuid) {
        this.playerUuid = playerUuid;
        this.vehicleUuid = vehicleUuid;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || !player.getUUID().equals(playerUuid)) {
            return;
        }

        // Find the vehicle by UUID in a large radius around the player
        double searchRadius = 50.0;
        net.minecraft.world.phys.Vec3 playerPos = player.position();
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(
            playerPos.subtract(searchRadius, searchRadius, searchRadius),
            playerPos.add(searchRadius, searchRadius, searchRadius)
        );

        EntityGenericVehicle vehicle = player.level().getEntitiesOfClass(
            EntityGenericVehicle.class,
            searchBox,
            v -> v.getUUID().equals(vehicleUuid)
        ).stream().findFirst().orElse(null);

        if (vehicle == null) {
            player.displayClientMessage(
                Component.translatable("message.garage.vehicle_not_found").withStyle(ChatFormatting.RED),
                false
            );
            player.closeContainer();
            return;
        }

        // Calculate total service cost
        double totalCost = calculateServiceCost(vehicle);

        // Check if player has enough money in bank account
        double playerBalance = EconomyManager.getBalance(player.getUUID());

        if (playerBalance < totalCost) {
            player.displayClientMessage(
                Component.translatable("message.garage.insufficient_funds",
                    String.format("%.2f€", totalCost),
                    String.format("%.2f€", playerBalance))
                    .withStyle(ChatFormatting.RED),
                false
            );

            // Revert vehicle changes (currently just unlock it)
            vehicle.unlockFromGarage();
            player.closeContainer();
            return;
        }

        // Deduct money from bank account
        if (EconomyManager.withdraw(player.getUUID(), totalCost)) {
            // Apply repairs/services
            applyServices(vehicle);

            player.displayClientMessage(
                Component.translatable("message.garage.payment_success", String.format("%.2f€", totalCost))
                    .withStyle(ChatFormatting.GREEN),
                false
            );

            // Unlock vehicle
            vehicle.unlockFromGarage();

            // Close GUI
            player.closeContainer();
        }
    }

    private double calculateServiceCost(EntityGenericVehicle vehicle) {
        double cost = 0.0;

        // Base inspection fee
        cost += 10.0;

        // Repair cost based on damage
        float damage = vehicle.getDamageComponent().getDamage();
        if (damage > 0) {
            cost += damage * 2.0; // 2€ per damage point
        }

        // Battery service if low
        float batteryPercent = vehicle.getBatteryComponent().getBatteryPercentage() * 100F;
        if (batteryPercent < 50) {
            cost += (50 - batteryPercent) * 0.5; // 0.5€ per percent to charge
        }

        return cost;
    }

    private void applyServices(EntityGenericVehicle vehicle) {
        // Repair damage
        vehicle.getDamageComponent().setDamage(0);

        // Charge battery to full
        vehicle.getBatteryComponent().setBatteryLevel(vehicle.getBatteryComponent().getMaxBatteryLevel());

        // Reset temperature
        vehicle.getDamageComponent().setTemperature(20.0F);
    }

    @Override
    public MessageGaragePayment fromBytes(FriendlyByteBuf buf) {
        playerUuid = buf.readUUID();
        vehicleUuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeUUID(vehicleUuid);
    }
}
