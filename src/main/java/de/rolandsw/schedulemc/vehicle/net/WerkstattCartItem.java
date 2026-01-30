package de.rolandsw.schedulemc.vehicle.net;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.*;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Represents a single item in the Werkstatt shopping cart.
 * Each item has a type and an optional value (e.g. target upgrade level or paint color).
 */
public class WerkstattCartItem {

    public enum Type {
        SERVICE_REPAIR,
        SERVICE_BATTERY,
        SERVICE_OIL,
        UPGRADE_MOTOR,
        UPGRADE_TANK,
        UPGRADE_TIRE,
        UPGRADE_FENDER,
        PAINT_CHANGE
    }

    private final Type type;
    private final int value; // target level for upgrades, color index for paint, 0 for services

    public WerkstattCartItem(Type type, int value) {
        this.type = type;
        this.value = value;
    }

    public WerkstattCartItem(Type type) {
        this(type, 0);
    }

    public Type getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    /**
     * Calculate the cost of this cart item based on current config and vehicle state.
     */
    public double calculateCost(EntityGenericVehicle vehicle) {
        return switch (type) {
            case SERVICE_REPAIR -> {
                float damage = Math.min(vehicle.getDamageComponent().getDamage(), 100);
                yield damage > 0 ? damage * ModConfigHandler.COMMON.WERKSTATT_REPAIR_COST_PER_PERCENT.get() : 0.0;
            }
            case SERVICE_BATTERY -> {
                float battery = vehicle.getBatteryComponent().getBatteryPercentage() * 100F;
                yield battery < 100 ? (100 - battery) * ModConfigHandler.COMMON.WERKSTATT_BATTERY_COST_PER_PERCENT.get() : 0.0;
            }
            case SERVICE_OIL -> ModConfigHandler.COMMON.WERKSTATT_OIL_CHANGE_COST.get();
            case UPGRADE_MOTOR -> {
                if (value == 2) yield ModConfigHandler.COMMON.WERKSTATT_MOTOR_UPGRADE_COST_LVL2.get();
                if (value == 3) yield ModConfigHandler.COMMON.WERKSTATT_MOTOR_UPGRADE_COST_LVL3.get();
                yield 0.0;
            }
            case UPGRADE_TANK -> {
                if (value == 2) yield ModConfigHandler.COMMON.WERKSTATT_TANK_UPGRADE_COST_LVL2.get();
                if (value == 3) yield ModConfigHandler.COMMON.WERKSTATT_TANK_UPGRADE_COST_LVL3.get();
                yield 0.0;
            }
            case UPGRADE_TIRE -> ModConfigHandler.COMMON.WERKSTATT_TIRE_UPGRADE_COST.get();
            case UPGRADE_FENDER -> {
                if (value == 2) yield ModConfigHandler.COMMON.WERKSTATT_FENDER_UPGRADE_COST_LVL2.get();
                if (value == 3) yield ModConfigHandler.COMMON.WERKSTATT_FENDER_UPGRADE_COST_LVL3.get();
                yield 0.0;
            }
            case PAINT_CHANGE -> {
                if (vehicle.getPaintColor() == value) yield 0.0;
                yield ModConfigHandler.COMMON.WERKSTATT_PAINT_CHANGE_COST.get();
            }
        };
    }

    /**
     * Get a display name key for this cart item.
     */
    public String getDisplayKey() {
        return switch (type) {
            case SERVICE_REPAIR -> "werkstatt.cart.repair";
            case SERVICE_BATTERY -> "werkstatt.cart.battery";
            case SERVICE_OIL -> "werkstatt.cart.oil_change";
            case UPGRADE_MOTOR -> "werkstatt.cart.motor_upgrade";
            case UPGRADE_TANK -> "werkstatt.cart.tank_upgrade";
            case UPGRADE_TIRE -> "werkstatt.cart.tire_upgrade";
            case UPGRADE_FENDER -> "werkstatt.cart.fender_upgrade";
            case PAINT_CHANGE -> "werkstatt.cart.paint_change";
        };
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeInt(value);
    }

    public static WerkstattCartItem fromBytes(FriendlyByteBuf buf) {
        Type type = buf.readEnum(Type.class);
        int value = buf.readInt();
        return new WerkstattCartItem(type, value);
    }
}
