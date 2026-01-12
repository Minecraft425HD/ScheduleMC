package de.rolandsw.schedulemc.towing.network;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.region.Plot;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.towing.MembershipData;
import de.rolandsw.schedulemc.towing.MembershipManager;
import de.rolandsw.schedulemc.towing.TowingYardManager;
import de.rolandsw.schedulemc.towing.TowingYardParkingSpot;
import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from client to server to request vehicle towing
 */
public class RequestTowingPacket {
    private final UUID vehicleEntityId;
    private final UUID towingYardPlotId;

    public RequestTowingPacket(UUID vehicleEntityId, UUID towingYardPlotId) {
        this.vehicleEntityId = vehicleEntityId;
        this.towingYardPlotId = towingYardPlotId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(vehicleEntityId);
        buf.writeUUID(towingYardPlotId);
    }

    public static RequestTowingPacket decode(FriendlyByteBuf buf) {
        return new RequestTowingPacket(buf.readUUID(), buf.readUUID());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, sender -> {
            ServerLevel level = sender.serverLevel();

            // Find vehicle entity
            Entity entity = level.getEntity(vehicleEntityId);
            if (!(entity instanceof EntityGenericVehicle vehicle)) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.vehicle_not_found"),
                    false
                );
                return;
            }

            // Check ownership
            if (!sender.getUUID().equals(vehicle.getOwnerId())) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.not_your_vehicle"),
                    false
                );
                return;
            }

            // Check if vehicle has passengers
            if (!vehicle.getPassengers().isEmpty()) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.vehicle_has_passengers"),
                    false
                );
                return;
            }

            // Get towing yard plot
            Plot towingYard = PlotManager.getInstance().getPlotById(towingYardPlotId);
            if (towingYard == null || !towingYard.getPlotType().isTowingYard()) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.invalid_yard"),
                    false
                );
                return;
            }

            // Find free parking spot
            TowingYardParkingSpot freeSpot = TowingYardManager.findFreeSpot(towingYardPlotId);
            if (freeSpot == null) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.no_free_spots"),
                    false
                );
                return;
            }

            // Calculate cost
            BlockPos vehiclePos = vehicle.blockPosition();
            BlockPos yardPos = freeSpot.getLocation();
            double distance = Math.sqrt(vehiclePos.distSqr(yardPos));
            double totalCost = TowingYardManager.calculateTowingCost(distance);

            // Apply membership discount
            MembershipData membership = MembershipManager.getMembership(sender.getUUID());
            if (membership != null && membership.isActive()) {
                totalCost = membership.getTier().calculatePlayerCost(totalCost);
            }

            // Save current vehicle state
            float currentDamage = vehicle.getDamageComponent().getDamage();
            boolean engineWasRunning = vehicle.getPhysicsComponent().isStarted();

            // Apply -10% damage reduction
            float newDamage = Math.max(0, currentDamage - 10f);
            vehicle.getDamageComponent().setDamage(newDamage);

            // Park vehicle at towing yard
            freeSpot.parkVehicle(
                vehicle.getUUID(),
                sender.getUUID(),
                totalCost,
                (int) currentDamage,
                engineWasRunning
            );

            // Teleport vehicle to parking spot
            vehicle.teleportTo(
                yardPos.getX() + 0.5,
                yardPos.getY(),
                yardPos.getZ() + 0.5
            );

            // Mark vehicle as on towing yard to disable fuel consumption
            vehicle.setIsOnTowingYard(true);

            TowingYardManager.save();

            // Increment tow count for membership
            if (membership != null) {
                membership.incrementTowCount();
                MembershipManager.save();
            }

            // Send success message
            sender.displayClientMessage(
                Component.translatable("towing.success.vehicle_towed",
                    String.format("%.0f", distance),
                    String.format("%.0f", totalCost)),
                false
            );

            ScheduleMC.LOGGER.info("Player {} towed vehicle {} to towing yard {} (distance: {}, cost: {})",
                sender.getName().getString(),
                vehicle.getDisplayName().getString(),
                towingYardPlotId,
                String.format("%.1f", distance),
                String.format("%.2f", totalCost));
        });
    }
}
