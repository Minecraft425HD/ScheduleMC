package de.rolandsw.schedulemc.vehicle.purchase;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.vehicle.VehicleMod;
import de.rolandsw.schedulemc.vehicle.api.VehiclePresets;
import de.rolandsw.schedulemc.vehicle.component.body.BodySpecification;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineSpecification;
import de.rolandsw.schedulemc.vehicle.component.fuel.FuelTankSpecification;
import de.rolandsw.schedulemc.vehicle.component.mobility.WheelSpecification;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
     * @param vehicleItem The vehicle spawn item being purchased
     * @param price       The price of the vehicle
     * @return true if purchase was successful
     */
    public static boolean purchaseVehicle(ServerPlayer player, UUID dealerUUID, ItemStack vehicleItem, int price) {
        // Check if player has enough money
        double balance = EconomyManager.getBalance(player.getUUID());
        if (balance < price) {
            player.sendSystemMessage(Component.literal("§cNicht genug Geld! Benötigt: " + price + "€, Verfügbar: " + String.format("%.2f€", balance)));
            return false;
        }

        // Withdraw money
        if (!EconomyManager.withdraw(player.getUUID(), price)) {
            player.sendSystemMessage(Component.literal("§cFehler beim Abbuchung!"));
            return false;
        }

        // Determine vehicle type from item name
        String itemName = vehicleItem.getHoverName().getString().toLowerCase();
        VehicleEntity vehicle = null;

        try {
            if (itemName.contains("sedan") || itemName.contains("limousine")) {
                vehicle = VehiclePresets.createSedan(player.serverLevel());
            } else if (itemName.contains("sport")) {
                vehicle = VehiclePresets.createSportCar(player.serverLevel());
            } else if (itemName.contains("suv")) {
                vehicle = VehiclePresets.createSUV(player.serverLevel());
            } else if (itemName.contains("truck") || itemName.contains("lkw")) {
                vehicle = VehiclePresets.createTruck(player.serverLevel());
            } else if (itemName.contains("transporter")) {
                vehicle = VehiclePresets.createTransporter(player.serverLevel());
            } else {
                // Default to sedan if type cannot be determined
                vehicle = VehiclePresets.createSedan(player.serverLevel());
            }

            // Set vehicle position near player
            Vec3 playerPos = player.position();
            vehicle.setPos(playerPos.x, playerPos.y + 1, playerPos.z);

            // Set owner
            vehicle.setOwner(player.getUUID(), player.getName().getString());

            // Spawn vehicle in world
            player.serverLevel().addFreshEntity(vehicle);

            // Send success message
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("FAHRZEUG GEKAUFT").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.literal("Typ: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(vehicleItem.getHoverName().getString()).withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("Preis: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(price + "€").withStyle(ChatFormatting.GOLD)));
            player.sendSystemMessage(Component.literal("Restguthaben: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))).withStyle(ChatFormatting.YELLOW)));
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));

            ScheduleMC.LOGGER.info("Player {} purchased vehicle {} for {}€", player.getName().getString(), itemName, price);
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
     * Get vehicle type name from spawn item
     */
    private static String getVehicleTypeName(ItemStack item) {
        String name = item.getHoverName().getString().toLowerCase();

        if (name.contains("sedan") || name.contains("limousine")) {
            return "Sedan";
        } else if (name.contains("sport")) {
            return "Sportwagen";
        } else if (name.contains("suv")) {
            return "SUV";
        } else if (name.contains("truck") || name.contains("lkw")) {
            return "LKW";
        } else if (name.contains("transporter")) {
            return "Transporter";
        }

        return "Fahrzeug";
    }
}
