package de.rolandsw.schedulemc.vehicle.net;
nimport de.rolandsw.schedulemc.util.StringUtils;

import de.rolandsw.schedulemc.config.ModConfigHandler;
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
    private boolean repairDamage;
    private boolean chargeBattery;
    private boolean changeOil;

    public MessageGaragePayment() {
    }

    public MessageGaragePayment(UUID playerUuid, UUID vehicleUuid, boolean repairDamage, boolean chargeBattery, boolean changeOil) {
        this.playerUuid = playerUuid;
        this.vehicleUuid = vehicleUuid;
        this.repairDamage = repairDamage;
        this.chargeBattery = chargeBattery;
        this.changeOil = changeOil;
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

        // Calculate total service cost based on selected services
        double totalCost = calculateServiceCost(vehicle, repairDamage, chargeBattery, changeOil);

        // Check if player has enough money in bank account
        double playerBalance = EconomyManager.getBalance(player.getUUID());

        if (playerBalance < totalCost) {
            player.displayClientMessage(
                Component.translatable("message.garage.insufficient_funds",
                    StringUtils.formatMoney(totalCost),
                    StringUtils.formatMoney(playerBalance))
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
            // Apply selected repairs/services
            applyServices(vehicle, repairDamage, chargeBattery, changeOil);

            player.displayClientMessage(
                Component.translatable("message.garage.payment_success", StringUtils.formatMoney(totalCost))
                    .withStyle(ChatFormatting.GREEN),
                false
            );

            // Unlock vehicle
            vehicle.unlockFromGarage();

            // Close GUI
            player.closeContainer();
        }
    }

    private double calculateServiceCost(EntityGenericVehicle vehicle, boolean repairDamage, boolean chargeBattery, boolean changeOil) {
        // Base inspection fee (always charged)
        double cost = ModConfigHandler.COMMON.GARAGE_BASE_INSPECTION_FEE.get();

        // Repair cost based on damage
        if (repairDamage) {
            float damage = vehicle.getDamageComponent().getDamage();
            if (damage > 0) {
                cost += damage * ModConfigHandler.COMMON.GARAGE_REPAIR_COST_PER_PERCENT.get();
            }
        }

        // Battery charging cost
        if (chargeBattery) {
            float batteryPercent = vehicle.getBatteryComponent().getBatteryPercentage() * 100F;
            if (batteryPercent < 50) {
                cost += (50 - batteryPercent) * ModConfigHandler.COMMON.GARAGE_BATTERY_COST_PER_PERCENT.get();
            }
        }

        // Oil change cost
        if (changeOil) {
            cost += ModConfigHandler.COMMON.GARAGE_OIL_CHANGE_COST.get();
        }

        return cost;
    }

    private void applyServices(EntityGenericVehicle vehicle, boolean repairDamage, boolean chargeBattery, boolean changeOil) {
        // Repair damage if selected
        if (repairDamage) {
            vehicle.getDamageComponent().setDamage(0);
            vehicle.getDamageComponent().setTemperature(20.0F); // Reset temperature
        }

        // Charge battery if selected
        if (chargeBattery) {
            vehicle.getBatteryComponent().setBatteryLevel(vehicle.getBatteryComponent().getMaxBatteryLevel());
        }

        // Oil change if selected
        if (changeOil) {
            // NOTE: Oil system not yet implemented - placeholder for future enhancement
        }
    }

    @Override
    public MessageGaragePayment fromBytes(FriendlyByteBuf buf) {
        playerUuid = buf.readUUID();
        vehicleUuid = buf.readUUID();
        repairDamage = buf.readBoolean();
        chargeBattery = buf.readBoolean();
        changeOil = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeUUID(vehicleUuid);
        buf.writeBoolean(repairDamage);
        buf.writeBoolean(chargeBattery);
        buf.writeBoolean(changeOil);
    }
}
