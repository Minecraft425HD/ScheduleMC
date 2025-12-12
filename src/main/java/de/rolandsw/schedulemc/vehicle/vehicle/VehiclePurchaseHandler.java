package de.rolandsw.schedulemc.vehicle.vehicle;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.VehicleFactory;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartRegistry;
import de.rolandsw.schedulemc.vehicle.items.ItemSpawnVehicle;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import de.rolandsw.schedulemc.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handler für den Kauf von Fahrzeugen beim Autohändler
 * Spawnt Fahrzeuge direkt am definierten Spawn-Punkt
 */
public class VehiclePurchaseHandler {

    /**
     * Verarbeitet den Kauf eines Fahrzeugs
     *
     * @param player Der Käufer
     * @param dealerId UUID des Autohändler-NPCs
     * @param vehicleItem Das Fahrzeug-Item (SPAWN_VEHICLE_*)
     * @param price Preis des Fahrzeugs
     * @return true wenn erfolgreich, false sonst
     */
    public static boolean purchaseVehicle(Player player, UUID dealerId, ItemStack vehicleItem, int price) {
        Level level = player.level();

        if (level.isClientSide()) {
            return false;
        }

        // Prüfe ob Spieler genug Geld hat
        double playerBalance = EconomyManager.getBalance(player.getUUID());
        if (playerBalance < price) {
            player.sendSystemMessage(Component.literal("⚠ Nicht genug Geld!").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal("Preis: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(price + "€").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" | Dein Guthaben: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.format("%.2f€", playerBalance)).withStyle(ChatFormatting.YELLOW)));
            return false;
        }

        // Ziehe Geld ab
        if (!EconomyManager.withdraw(player.getUUID(), price)) {
            player.sendSystemMessage(Component.literal("⚠ Fehler beim Abbuchung!").withStyle(ChatFormatting.RED));
            return false;
        }

        // Finde freien Spawn-Punkt
        VehicleSpawnRegistry.VehicleSpawnPoint spawnPoint = VehicleSpawnRegistry.findFreeSpawnPoint(dealerId);

        if (spawnPoint == null) {
            // Geld zurückgeben
            EconomyManager.deposit(player.getUUID(), price);
            player.sendSystemMessage(Component.literal("⚠ Kein freier Parkplatz verfügbar!").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal("Bitte warten Sie, bis ein Parkplatz frei wird.").withStyle(ChatFormatting.GRAY));
            return false;
        }

        // Spawn das Fahrzeug
        EntityGenericVehicle vehicle = spawnVehicle((ServerLevel) level, vehicleItem, spawnPoint.getPosition(), spawnPoint.getYaw());

        if (vehicle == null) {
            // Geld zurückgeben
            EconomyManager.deposit(player.getUUID(), price);
            player.sendSystemMessage(Component.literal("⚠ Fehler beim Spawnen des Fahrzeugs!").withStyle(ChatFormatting.RED));
            return false;
        }

        // Setze Owner und verknüpfe Fahrzeug
        UUID vehicleUUID = UUID.randomUUID();
        vehicle.setOwnerId(player.getUUID());
        vehicle.setVehicleUUID(vehicleUUID);
        vehicle.setHomeSpawnPoint(spawnPoint.getPosition());

        // Markiere Spawn-Punkt als belegt
        VehicleSpawnRegistry.occupySpawnPoint(spawnPoint.getPosition(), vehicleUUID);
        VehicleSpawnRegistry.saveIfNeeded();

        // Erfolgs-Nachricht
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("🚗 ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal("FAHRZEUG GEKAUFT").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.literal("Modell: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(getVehicleName(vehicleItem)).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Preis: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(price + "€").withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.literal("Parkplatz: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(spawnPoint.getPosition().toShortString()).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Fahrzeug-ID: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(vehicleUUID.toString().substring(0, 8) + "...").withStyle(ChatFormatting.DARK_GRAY)));
        player.sendSystemMessage(Component.literal("Restguthaben: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))).withStyle(ChatFormatting.YELLOW)));
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));

        return true;
    }

    /**
     * Spawnt ein Fahrzeug am angegebenen Spawn-Punkt
     */
    private static EntityGenericVehicle spawnVehicle(ServerLevel level, ItemStack vehicleItem, BlockPos pos, float yaw) {
        if (!(vehicleItem.getItem() instanceof ItemSpawnVehicle)) {
            return null;
        }

        // Bestimme den Body-Type basierend auf dem Item
        ItemSpawnVehicle spawnVehicleItem = (ItemSpawnVehicle) vehicleItem.getItem();

        // Erstelle Teile-Liste (wie in ItemSpawnVehicle)
        List<ItemStack> parts = new ArrayList<>();

        // Bestimme Body basierend auf dem SPAWN_CAR Item
        if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_OAK.get()) {
            parts.add(new ItemStack(ModItems.OAK_BODY.get()));
            parts.add(new ItemStack(ModItems.ENGINE_3_CYLINDER.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
        } else if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_BIG_OAK.get()) {
            parts.add(new ItemStack(ModItems.BIG_OAK_BODY.get()));
            parts.add(new ItemStack(ModItems.ENGINE_3_CYLINDER.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
        } else if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_WHITE_TRANSPORTER.get()) {
            parts.add(new ItemStack(ModItems.WHITE_TRANSPORTER_BODY.get()));
            parts.add(new ItemStack(ModItems.ENGINE_3_CYLINDER.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
        } else if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_WHITE_SUV.get()) {
            parts.add(new ItemStack(ModItems.WHITE_SUV_BODY.get()));
            parts.add(new ItemStack(ModItems.ENGINE_3_CYLINDER.get()));
            parts.add(new ItemStack(ModItems.BIG_WHEEL.get()));
            parts.add(new ItemStack(ModItems.BIG_WHEEL.get()));
            parts.add(new ItemStack(ModItems.BIG_WHEEL.get()));
            parts.add(new ItemStack(ModItems.BIG_WHEEL.get()));
        } else if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_WHITE_SPORT.get()) {
            parts.add(new ItemStack(ModItems.WHITE_SPORT_BODY.get()));
            parts.add(new ItemStack(ModItems.ENGINE_3_CYLINDER.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
        } else {
            return null; // Unbekanntes Fahrzeug
        }

        // Füge Tank und Lizenzplatte hinzu
        parts.add(new ItemStack(ModItems.SMALL_TANK.get()));
        parts.add(new ItemStack(ModItems.LICENSE_PLATE.get()));
        parts.add(new ItemStack(ModItems.IRON_LICENSE_PLATE_HOLDER.get()));

        // Erstelle Auto mit VehicleFactory (wie in ItemSpawnVehicle)
        EntityGenericVehicle vehicle = VehicleFactory.createVehicle(level, parts);

        if (vehicle == null) {
            return null;
        }

        // Position und Rotation setzen
        vehicle.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        vehicle.setYRot(yaw);
        vehicle.yRotO = yaw;
        vehicle.setFuelAmount(100);
        vehicle.setBatteryLevel(500);

        // Spawn das Fahrzeug in der Welt
        level.addFreshEntity(vehicle);
        vehicle.setIsSpawned(true);
        vehicle.initTemperature();

        return vehicle;
    }

    /**
     * Gibt den Fahrzeug-Namen zurück
     */
    private static String getVehicleName(ItemStack vehicleItem) {
        return vehicleItem.getHoverName().getString();
    }

    /**
     * Wird aufgerufen wenn ein Fahrzeug zerstört wird
     * Gibt den Spawn-Punkt frei
     */
    public static void onVehicleDestroyed(UUID vehicleUUID) {
        VehicleSpawnRegistry.releaseSpawnPoint(vehicleUUID);
        VehicleSpawnRegistry.saveIfNeeded();
    }
}
