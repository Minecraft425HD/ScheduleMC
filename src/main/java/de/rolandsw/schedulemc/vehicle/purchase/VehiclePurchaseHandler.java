package de.rolandsw.schedulemc.vehicle.purchase;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.vehicle.VehicleMod;
import de.rolandsw.schedulemc.vehicle.builder.VehiclePresets;
import de.rolandsw.schedulemc.vehicle.component.body.BodySpecification;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineSpecification;
import de.rolandsw.schedulemc.vehicle.component.fuel.FuelTankSpecification;
import de.rolandsw.schedulemc.vehicle.component.mobility.WheelSpecification;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.items.VehicleSpawnMarker;
import de.rolandsw.schedulemc.vehicle.items.VehicleVoucher;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Handles vehicle purchases from dealers for the new ECS-based vehicle system.
 */
public class VehiclePurchaseHandler {

    /**
     * Purchase a vehicle from a dealer
     *
     * @param player      The player purchasing the vehicle
     * @param dealerUUID  The UUID of the dealer NPC
     * @param vehicleItem The vehicle voucher being purchased
     * @param price       The price of the vehicle
     * @return true if purchase was successful
     */
    public static boolean purchaseVehicle(ServerPlayer player, UUID dealerUUID, ItemStack vehicleItem, int price) {
        // Check if player has spawn marker set
        if (!VehicleSpawnMarker.hasSpawnMarker(player.getUUID())) {
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal("✗ ").withStyle(ChatFormatting.RED)
                .append(Component.literal("KEIN SPAWNPUNKT GESETZT").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.literal("Bitte setze zuerst einen Fahrzeug-Spawnpunkt!").withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal("Verwende den Fahrzeug-Spawnpunkt-Marker.").withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.RED));
            return false;
        }

        // Check if player has enough money
        double balance = EconomyManager.getBalance(player.getUUID());
        if (balance < price) {
            player.sendSystemMessage(Component.literal("§cNicht genug Geld! Benötigt: " + price + "€, Verfügbar: " + String.format("%.2f€", balance)));
            return false;
        }

        // Get spawn marker location
        VehicleSpawnMarker.SpawnMarkerData marker = VehicleSpawnMarker.getSpawnMarker(player.getUUID());
        BlockPos spawnPos = marker.getPosition();

        // Check if spawn location is clear (no vehicle already there)
        ServerLevel level = player.serverLevel();
        if (!isSpawnLocationClear(level, spawnPos)) {
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal("✗ ").withStyle(ChatFormatting.RED)
                .append(Component.literal("SPAWNPUNKT BLOCKIERT").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.literal("Ein Fahrzeug befindet sich bereits am Spawnpunkt!").withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal("Entferne das alte Fahrzeug zuerst.").withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.RED));
            return false;
        }

        // Withdraw money
        if (!EconomyManager.withdraw(player.getUUID(), price)) {
            player.sendSystemMessage(Component.literal("§cFehler bei der Abbuchung!"));
            return false;
        }

        // Get vehicle type from voucher
        VehicleVoucher.VehicleType vehicleType = VehicleVoucher.getVehicleType(vehicleItem);
        if (vehicleType == null) {
            // Refund and error
            EconomyManager.deposit(player.getUUID(), price);
            player.sendSystemMessage(Component.literal("§cUngültiger Fahrzeugtyp!"));
            return false;
        }

        try {
            // Create vehicle based on type
            VehicleEntity vehicle = createVehicleByType(level, vehicleType);

            if (vehicle == null) {
                throw new IllegalStateException("Failed to create vehicle of type: " + vehicleType);
            }

            // Set vehicle position at spawn marker
            vehicle.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

            // Set owner
            vehicle.setOwner(player.getUUID(), player.getName().getString());

            // Spawn vehicle in world
            level.addFreshEntity(vehicle);

            // Send success message
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("FAHRZEUG GEKAUFT").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.literal("Typ: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(vehicleType.getDisplayName()).withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("Preis: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(price + "€").withStyle(ChatFormatting.GOLD)));
            player.sendSystemMessage(Component.literal("Spawnpunkt: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("X: %d, Y: %d, Z: %d", spawnPos.getX(), spawnPos.getY(), spawnPos.getZ())).withStyle(ChatFormatting.YELLOW)));
            player.sendSystemMessage(Component.literal("Restguthaben: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))).withStyle(ChatFormatting.YELLOW)));
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));

            ScheduleMC.LOGGER.info("Player {} purchased vehicle {} for {}€ at spawn marker {}",
                player.getName().getString(), vehicleType.getDisplayName(), price, spawnPos);
            return true;

        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Failed to spawn vehicle for player {}", player.getName().getString(), e);
            // Refund the player
            EconomyManager.deposit(player.getUUID(), price);
            player.sendSystemMessage(Component.literal("§cFehler beim Spawnen des Fahrzeugs! Geld wurde zurückerstattet."));
            return false;
        }
    }

    /**
     * Check if spawn location is clear of vehicles
     */
    private static boolean isSpawnLocationClear(ServerLevel level, BlockPos pos) {
        // Check for vehicles in a 3x3x3 area around spawn point
        return level.getEntitiesOfClass(VehicleEntity.class,
            new net.minecraft.world.phys.AABB(pos).inflate(2.0)).isEmpty();
    }

    /**
     * Create vehicle by type
     */
    private static VehicleEntity createVehicleByType(ServerLevel level, VehicleVoucher.VehicleType type) {
        return switch (type) {
            case SEDAN -> VehiclePresets.createSedan(level);
            case SPORT -> VehiclePresets.createSportCar(level);
            case SUV -> VehiclePresets.createSUV(level);
            case TRUCK -> VehiclePresets.createTruck(level);
            case TRANSPORTER -> VehiclePresets.createTransporter(level);
        };
    }

}
