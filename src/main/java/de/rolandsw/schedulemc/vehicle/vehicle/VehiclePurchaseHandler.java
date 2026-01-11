package de.rolandsw.schedulemc.vehicle.vehicle;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.VehicleFactory;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartRegistry;
import de.rolandsw.schedulemc.vehicle.items.ItemLicensePlate;
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
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handler für den Kauf von Fahrzeugen beim Autohändler
 * Spawnt Fahrzeuge direkt am definierten Spawn-Punkt
 */
public class VehiclePurchaseHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

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

        LOGGER.info("Fahrzeugkauf gestartet: Spieler={}, Händler={}, Fahrzeug={}, Preis={}",
            player.getName().getString(), dealerId, vehicleItem.getHoverName().getString(), price);

        // Prüfe ob Spieler genug Geld hat
        double playerBalance = EconomyManager.getBalance(player.getUUID());
        if (playerBalance < price) {
            LOGGER.warn("Fahrzeugkauf fehlgeschlagen: Nicht genug Geld. Balance={}, Preis={}", playerBalance, price);
            player.sendSystemMessage(Component.translatable("message.common.not_enough_money").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.translatable("message.common.price_label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(price + "€").withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("vehicle.purchase.your_balance").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.format("%.2f€", playerBalance)).withStyle(ChatFormatting.YELLOW)));
            return false;
        }

        // Ziehe Geld ab
        if (!EconomyManager.withdraw(player.getUUID(), price)) {
            LOGGER.error("Fahrzeugkauf fehlgeschlagen: Geld konnte nicht abgebucht werden");
            player.sendSystemMessage(Component.translatable("vehicle.purchase.deduction_error").withStyle(ChatFormatting.RED));
            return false;
        }

        LOGGER.info("Geld abgebucht: {}€. Suche Spawn-Punkt für Händler {}", price, dealerId);

        // Finde freien Spawn-Punkt
        List<VehicleSpawnRegistry.VehicleSpawnPoint> allPoints = VehicleSpawnRegistry.getSpawnPoints(dealerId);
        LOGGER.info("Verfügbare Spawn-Punkte für Händler {}: {}", dealerId, allPoints.size());

        VehicleSpawnRegistry.VehicleSpawnPoint spawnPoint = VehicleSpawnRegistry.findFreeSpawnPoint(dealerId);

        if (spawnPoint == null) {
            // Geld zurückgeben
            EconomyManager.deposit(player.getUUID(), price);
            LOGGER.warn("Fahrzeugkauf fehlgeschlagen: Kein freier Spawn-Punkt verfügbar! Händler {} hat {} Punkte, alle belegt",
                dealerId, allPoints.size());
            player.sendSystemMessage(Component.translatable("vehicle.purchase.no_parking").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.translatable("vehicle.purchase.wait_for_parking").withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal("DEBUG: Händler hat " + allPoints.size() + " Spawn-Punkte, alle belegt")
                .withStyle(ChatFormatting.DARK_GRAY));
            return false;
        }

        LOGGER.info("Freien Spawn-Punkt gefunden: {}", spawnPoint.getPosition());

        // Spawn das Fahrzeug
        EntityGenericVehicle vehicle = spawnVehicle(player, (ServerLevel) level, vehicleItem, spawnPoint.getPosition(), spawnPoint.getYaw());

        if (vehicle == null) {
            // Geld zurückgeben
            EconomyManager.deposit(player.getUUID(), price);
            LOGGER.error("Fahrzeugkauf fehlgeschlagen: Fahrzeug konnte nicht gespawnt werden");
            player.sendSystemMessage(Component.translatable("message.vehicle.spawn_error").withStyle(ChatFormatting.RED));
            return false;
        }

        LOGGER.info("Fahrzeug erfolgreich gespawnt");

        // Setze Owner und verknüpfe Fahrzeug
        UUID vehicleUUID = UUID.randomUUID();
        vehicle.setOwnerId(player.getUUID());
        vehicle.setVehicleUUID(vehicleUUID);
        vehicle.setHomeSpawnPoint(spawnPoint.getPosition());

        // Markiere Spawn-Punkt als belegt
        VehicleSpawnRegistry.occupySpawnPoint(spawnPoint.getPosition(), vehicleUUID);
        VehicleSpawnRegistry.saveIfNeeded();

        LOGGER.info("Fahrzeugkauf erfolgreich abgeschlossen: Vehicle-UUID={}, Kennzeichen={}", vehicleUUID, vehicle.getLicensePlate());

        // Erfolgs-Nachricht
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("🚗 ").withStyle(ChatFormatting.YELLOW)
            .append(Component.translatable("message.vehicle.purchased").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.translatable("vehicle.purchase.model_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(getVehicleName(vehicleItem)).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.translatable("message.common.price_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(price + "€").withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.translatable("vehicle.purchase.parking_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(spawnPoint.getPosition().toShortString()).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.translatable("message.vehicle.id_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(vehicleUUID.toString().substring(0, 8) + "...").withStyle(ChatFormatting.DARK_GRAY)));
        player.sendSystemMessage(Component.translatable("message.bank.remaining_credit_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))).withStyle(ChatFormatting.YELLOW)));
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));

        return true;
    }

    /**
     * Spawnt ein Fahrzeug am angegebenen Spawn-Punkt
     */
    private static EntityGenericVehicle spawnVehicle(Player player, ServerLevel level, ItemStack vehicleItem, BlockPos pos, float yaw) {
        if (!(vehicleItem.getItem() instanceof ItemSpawnVehicle)) {
            return null;
        }

        // Bestimme den Body-Type basierend auf dem Item
        ItemSpawnVehicle spawnVehicleItem = (ItemSpawnVehicle) vehicleItem.getItem();

        // Erstelle Teile-Liste (wie in ItemSpawnVehicle)
        List<ItemStack> parts = new ArrayList<>();

        // Bestimme Body basierend auf dem SPAWN_VEHICLE Item
        if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_OAK.get()) {
            parts.add(new ItemStack(ModItems.LIMOUSINE_CHASSIS.get()));
            parts.add(new ItemStack(ModItems.NORMAL_MOTOR.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        } else if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_BIG_OAK.get()) {
            parts.add(new ItemStack(ModItems.VAN_CHASSIS.get()));
            parts.add(new ItemStack(ModItems.NORMAL_MOTOR.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        } else if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_WHITE_TRANSPORTER.get()) {
            parts.add(new ItemStack(ModItems.TRUCK_CHASSIS.get()));
            parts.add(new ItemStack(ModItems.NORMAL_MOTOR.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        } else if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_WHITE_SUV.get()) {
            parts.add(new ItemStack(ModItems.OFFROAD_CHASSIS.get()));
            parts.add(new ItemStack(ModItems.NORMAL_MOTOR.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
        } else if (spawnVehicleItem == ModItems.SPAWN_VEHICLE_WHITE_SPORT.get()) {
            parts.add(new ItemStack(ModItems.LUXUS_CHASSIS.get()));
            parts.add(new ItemStack(ModItems.NORMAL_MOTOR.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        } else {
            return null; // Unbekanntes Fahrzeug
        }

        // Füge Tank und Lizenzplatte hinzu
        parts.add(new ItemStack(ModItems.TANK_15L.get()));

        // Generiere Auto-Kennzeichen
        ItemStack licensePlate = new ItemStack(ModItems.LICENSE_PLATE.get());
        String plateText = generateLicensePlateText(player, (ServerLevel) level);
        ItemLicensePlate.setText(licensePlate, plateText);
        parts.add(licensePlate);

        parts.add(new ItemStack(ModItems.LICENSE_PLATE_HOLDER.get()));

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

        // WICHTIG: Setze Kennzeichen auch auf der Entity (nicht nur auf dem Item)
        vehicle.setLicensePlate(plateText);

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

    /**
     * Generiert Kennzeichen-Text basierend auf Spielername
     * Format: XXX-YY
     * XXX = 3 Anfangsbuchstaben des Spielernamens
     * YY = Fahrzeug-Nummer (01-99), mit Offset bei gleichem Präfix
     *
     * Beispiele:
     * - Minecraft425HD (1. Auto): MIN-01
     * - Minecraft425HD (2. Auto): MIN-02
     * - MinecraftSteve (1. Auto, gleicher Präfix): MIN-10
     */
    private static String generateLicensePlateText(Player player, ServerLevel level) {
        // Extrahiere Präfix aus Spielername (3 Buchstaben)
        String prefix = extractPrefix(player.getName().getString());

        // Hole Tracker
        VehicleOwnershipTracker tracker = VehicleOwnershipTracker.get(level);

        // Registriere Kauf und hole Nummer
        int plateNumber = tracker.registerVehiclePurchase(player, prefix);

        // Formatiere: XXX-YY (z.B. "MIN-01")
        return String.format("%s-%02d", prefix, plateNumber);
    }

    /**
     * Extrahiert 3-Buchstaben-Präfix aus Spielername
     * - Mindestens 3 Zeichen, sonst mit 'X' auffüllen
     * - Nur Großbuchstaben
     * - Umlaute konvertieren (ä→A, ö→O, ü→U, ß→S)
     * - Sonderzeichen entfernen
     */
    private static String extractPrefix(String playerName) {
        // Entferne Sonderzeichen und konvertiere
        StringBuilder cleaned = new StringBuilder();
        for (char c : playerName.toCharArray()) {
            char upper = Character.toUpperCase(c);

            // Umlaute konvertieren
            if (upper == 'Ä') upper = 'A';
            else if (upper == 'Ö') upper = 'O';
            else if (upper == 'Ü') upper = 'U';
            else if (upper == 'ß') upper = 'S';

            // Nur A-Z behalten
            if (upper >= 'A' && upper <= 'Z') {
                cleaned.append(upper);
            }
        }

        String result = cleaned.toString();

        // Mindestens 3 Zeichen
        if (result.length() < 3) {
            // Mit 'X' auffüllen
            while (result.length() < 3) {
                result += "X";
            }
        } else if (result.length() > 3) {
            // Nur erste 3 Zeichen
            result = result.substring(0, 3);
        }

        return result;
    }
}
