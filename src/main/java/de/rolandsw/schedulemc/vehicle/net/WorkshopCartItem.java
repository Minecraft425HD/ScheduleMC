package de.rolandsw.schedulemc.vehicle.net;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.*;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Represents a single item in the Workshop shopping cart.
 * Each item has a type and an optional value (e.g. target upgrade level or paint color).
 */
public class WorkshopCartItem {

    public enum Type {
        SERVICE_REPAIR,
        SERVICE_BATTERY,
        SERVICE_OIL,
        UPGRADE_MOTOR,
        UPGRADE_TANK,
        UPGRADE_TIRE,
        UPGRADE_FENDER,
        PAINT_CHANGE,
        CONTAINER_ITEM,
        CONTAINER_FLUID,
        TIRE_SEASON_SWITCH
    }

    private final Type type;
    private final int value; // target level for upgrades, color index for paint, 0 for services

    public WorkshopCartItem(Type type, int value) {
        this.type = type;
        this.value = value;
    }

    public WorkshopCartItem(Type type) {
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
                yield damage > 0 ? damage * ModConfigHandler.COMMON.WORKSHOP_REPAIR_COST_PER_PERCENT.get() : 0.0;
            }
            case SERVICE_BATTERY -> {
                float battery = vehicle.getBatteryComponent().getBatteryPercentage() * 100F;
                yield battery < 100 ? (100 - battery) * ModConfigHandler.COMMON.WORKSHOP_BATTERY_COST_PER_PERCENT.get() : 0.0;
            }
            case SERVICE_OIL -> ModConfigHandler.COMMON.WORKSHOP_OIL_CHANGE_COST.get();
            case UPGRADE_MOTOR -> {
                if (value == 2) yield ModConfigHandler.COMMON.WORKSHOP_MOTOR_UPGRADE_COST_LVL2.get();
                if (value == 3) yield ModConfigHandler.COMMON.WORKSHOP_MOTOR_UPGRADE_COST_LVL3.get();
                yield 0.0;
            }
            case UPGRADE_TANK -> {
                if (value == 2) yield ModConfigHandler.COMMON.WORKSHOP_TANK_UPGRADE_COST_LVL2.get();
                if (value == 3) yield ModConfigHandler.COMMON.WORKSHOP_TANK_UPGRADE_COST_LVL3.get();
                yield 0.0;
            }
            case UPGRADE_TIRE -> ModConfigHandler.COMMON.WORKSHOP_TIRE_UPGRADE_COST.get();
            case UPGRADE_FENDER -> {
                if (value == 2) yield ModConfigHandler.COMMON.WORKSHOP_FENDER_UPGRADE_COST_LVL2.get();
                if (value == 3) yield ModConfigHandler.COMMON.WORKSHOP_FENDER_UPGRADE_COST_LVL3.get();
                yield 0.0;
            }
            case PAINT_CHANGE -> {
                if (vehicle.getPaintColor() == value) yield 0.0;
                yield ModConfigHandler.COMMON.WORKSHOP_PAINT_CHANGE_COST.get();
            }
            case CONTAINER_ITEM -> vehicle.hasHadItemContainer()
                    ? ModConfigHandler.VEHICLE_SERVER.containerReinstallationCost.get() : 0.0;
            case CONTAINER_FLUID -> vehicle.hasHadFluidContainer()
                    ? ModConfigHandler.VEHICLE_SERVER.containerReinstallationCost.get() : 0.0;
            case TIRE_SEASON_SWITCH -> ModConfigHandler.COMMON.WORKSHOP_TIRE_UPGRADE_COST.get();
        };
    }

    /**
     * Get a display name key for this cart item.
     */
    public String getDisplayKey() {
        return switch (type) {
            case SERVICE_REPAIR -> "workshop.cart.repair";
            case SERVICE_BATTERY -> "workshop.cart.battery";
            case SERVICE_OIL -> "workshop.cart.oil_change";
            case UPGRADE_MOTOR -> "workshop.cart.motor_upgrade";
            case UPGRADE_TANK -> "workshop.cart.tank_upgrade";
            case UPGRADE_TIRE -> "workshop.cart.tire_upgrade";
            case UPGRADE_FENDER -> "workshop.cart.fender_upgrade";
            case PAINT_CHANGE -> "workshop.cart.paint_change";
            case CONTAINER_ITEM -> "workshop.cart.container_item";
            case CONTAINER_FLUID -> "workshop.cart.container_fluid";
            case TIRE_SEASON_SWITCH -> "workshop.cart.tire_season_switch";
        };
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeInt(value);
    }

    public static WorkshopCartItem fromBytes(FriendlyByteBuf buf) {
        Type type = buf.readEnum(Type.class);
        int value = buf.readInt();
        return new WorkshopCartItem(type, value);
    }
}
