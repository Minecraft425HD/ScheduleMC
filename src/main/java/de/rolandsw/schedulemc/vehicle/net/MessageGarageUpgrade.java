package de.rolandsw.schedulemc.vehicle.net;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.*;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageGarageUpgrade implements Message<MessageGarageUpgrade> {

    private UUID playerUuid;
    private UUID vehicleUuid;
    private UpgradeType upgradeType;
    private int value; // Used for: motor/tank/fender level, tire type index, or paint color

    public MessageGarageUpgrade() {
    }

    public MessageGarageUpgrade(UUID playerUuid, UUID vehicleUuid, UpgradeType upgradeType, int value) {
        this.playerUuid = playerUuid;
        this.vehicleUuid = vehicleUuid;
        this.upgradeType = upgradeType;
        this.value = value;
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
            return;
        }

        // Calculate upgrade cost
        double upgradeCost = calculateUpgradeCost(upgradeType, value, vehicle);

        if (upgradeCost < 0) {
            player.displayClientMessage(
                Component.translatable("message.garage.invalid_upgrade").withStyle(ChatFormatting.RED),
                false
            );
            return;
        }

        // Check if player has enough money
        double playerBalance = EconomyManager.getBalance(player.getUUID());

        if (playerBalance < upgradeCost) {
            player.displayClientMessage(
                Component.translatable("message.garage.insufficient_funds",
                    String.format("%.2f€", upgradeCost),
                    String.format("%.2f€", playerBalance))
                    .withStyle(ChatFormatting.RED),
                false
            );
            return;
        }

        // Deduct money and apply upgrade
        if (EconomyManager.withdraw(player.getUUID(), upgradeCost)) {
            boolean success = applyUpgrade(vehicle, upgradeType, value);

            if (success) {
                player.displayClientMessage(
                    Component.translatable("message.garage.upgrade_success", String.format("%.2f€", upgradeCost))
                        .withStyle(ChatFormatting.GREEN),
                    false
                );
            } else {
                // Refund if upgrade failed
                EconomyManager.deposit(player.getUUID(), upgradeCost);
                player.displayClientMessage(
                    Component.translatable("message.garage.upgrade_failed").withStyle(ChatFormatting.RED),
                    false
                );
            }
        }
    }

    private double calculateUpgradeCost(UpgradeType type, int value, EntityGenericVehicle vehicle) {
        return switch (type) {
            case MOTOR -> {
                if (value == 2) yield ModConfigHandler.COMMON.GARAGE_MOTOR_UPGRADE_COST_LVL2.get();
                if (value == 3) yield ModConfigHandler.COMMON.GARAGE_MOTOR_UPGRADE_COST_LVL3.get();
                yield -1.0;
            }
            case TANK -> {
                if (value == 2) yield ModConfigHandler.COMMON.GARAGE_TANK_UPGRADE_COST_LVL2.get();
                if (value == 3) yield ModConfigHandler.COMMON.GARAGE_TANK_UPGRADE_COST_LVL3.get();
                yield -1.0;
            }
            case TIRE -> ModConfigHandler.COMMON.GARAGE_TIRE_UPGRADE_COST.get();
            case PAINT -> {
                // Only charge if color is different from current
                if (vehicle.getPaintColor() == value) {
                    yield 0.0;
                }
                yield ModConfigHandler.COMMON.GARAGE_PAINT_CHANGE_COST.get();
            }
            case FENDER -> {
                if (value == 2) yield ModConfigHandler.COMMON.GARAGE_FENDER_UPGRADE_COST_LVL2.get();
                if (value == 3) yield ModConfigHandler.COMMON.GARAGE_FENDER_UPGRADE_COST_LVL3.get();
                yield -1.0;
            }
        };
    }

    private boolean applyUpgrade(EntityGenericVehicle vehicle, UpgradeType type, int value) {
        try {
            switch (type) {
                case MOTOR -> {
                    // Replace motor part in part inventory
                    return replacePartInInventory(vehicle, PartEngine.class, getMotorByLevel(value));
                }
                case TANK -> {
                    // Replace tank part in part inventory
                    return replacePartInInventory(vehicle, PartTank.class, getTankByLevel(value));
                }
                case TIRE -> {
                    // Replace all tire parts in part inventory
                    return replacePartInInventory(vehicle, PartTireBase.class, getTireByIndex(value, vehicle));
                }
                case PAINT -> {
                    // Change vehicle paint color (direct property change)
                    if (value >= 0 && value <= 4) {
                        vehicle.setPaintColor(value);
                        return true;
                    }
                    return false;
                }
                case FENDER -> {
                    // Replace fender part in part inventory
                    return replacePartInInventory(vehicle, PartBumper.class, getFenderByLevel(value));
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper method to replace a part in the vehicle's part inventory
     */
    private boolean replacePartInInventory(EntityGenericVehicle vehicle, Class<? extends Part> partClass, Part newPart) {
        if (newPart == null) {
            return false;
        }

        net.minecraft.world.Container partInventory = vehicle.getInventoryComponent().getPartInventory();

        // Find and replace the part(s) in the inventory
        boolean replacedAny = false;
        for (int i = 0; i < partInventory.getContainerSize(); i++) {
            ItemStack stack = partInventory.getItem(i);

            if (!stack.isEmpty() && stack.getItem() instanceof de.rolandsw.schedulemc.vehicle.items.IVehiclePart partItem) {
                Part existingPart = partItem.getPart(stack);

                // Check if this part matches the class we want to replace
                if (partClass.isInstance(existingPart)) {
                    // Create new ItemStack for the new part
                    ItemStack newStack = getItemStackForPart(newPart);
                    if (!newStack.isEmpty()) {
                        partInventory.setItem(i, newStack);
                        replacedAny = true;

                        // For tires, we need to replace all of them, so continue
                        // For motors/tanks/fenders, just replace the first one and break
                        if (!PartTireBase.class.isAssignableFrom(partClass)) {
                            break;
                        }
                    }
                }
            }
        }

        // Update the vehicle's serialized parts data
        if (replacedAny) {
            vehicle.setPartSerializer();
        }

        return replacedAny;
    }

    /**
     * Get the ItemStack for a given Part
     */
    private ItemStack getItemStackForPart(Part part) {
        // Map parts to their items
        if (part == PartRegistry.NORMAL_MOTOR) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.NORMAL_MOTOR.get());
        if (part == PartRegistry.PERFORMANCE_MOTOR) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.PERFORMANCE_MOTOR.get());
        if (part == PartRegistry.INDUSTRIAL_MOTOR) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.INDUSTRIAL_MOTOR.get());

        if (part == PartRegistry.TANK_15L) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.TANK_15L.get());
        if (part == PartRegistry.TANK_30L) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.TANK_30L.get());
        if (part == PartRegistry.TANK_50L) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.TANK_50L.get());

        if (part == PartRegistry.STANDARD_TIRE) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.STANDARD_TIRE.get());
        if (part == PartRegistry.SPORT_TIRE) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.SPORT_TIRE.get());
        if (part == PartRegistry.PREMIUM_TIRE) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.PREMIUM_TIRE.get());
        if (part == PartRegistry.OFFROAD_TIRE) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.OFFROAD_TIRE.get());
        if (part == PartRegistry.ALLTERRAIN_TIRE) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.ALLTERRAIN_TIRE.get());
        if (part == PartRegistry.HEAVY_DUTY_TIRE) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.HEAVY_DUTY_TIRE.get());

        if (part == PartRegistry.FENDER_BASIC) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.FENDER_BASIC.get());
        if (part == PartRegistry.FENDER_CHROME) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.FENDER_CHROME.get());
        if (part == PartRegistry.FENDER_SPORT) return new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.FENDER_SPORT.get());

        return ItemStack.EMPTY;
    }

    private Part getMotorByLevel(int level) {
        return switch (level) {
            case 1 -> PartRegistry.NORMAL_MOTOR;
            case 2 -> PartRegistry.PERFORMANCE_MOTOR;
            case 3 -> PartRegistry.INDUSTRIAL_MOTOR;
            default -> null;
        };
    }

    private Part getTankByLevel(int level) {
        return switch (level) {
            case 1 -> PartRegistry.TANK_15L;
            case 2 -> PartRegistry.TANK_30L;
            case 3 -> PartRegistry.TANK_50L;
            default -> null;
        };
    }

    private Part getTireByIndex(int index, EntityGenericVehicle vehicle) {
        // Determine if vehicle is a truck based on chassis type
        PartBody body = vehicle.getPartByClass(PartBody.class);
        boolean isTruck = body != null && (body.getTranslationKey().contains("transporter")
                                         || body.getTranslationKey().contains("delivery"));

        if (isTruck) {
            // Truck tires: 0=OFFROAD, 1=ALLTERRAIN, 2=HEAVY_DUTY
            return switch (index) {
                case 0 -> PartRegistry.OFFROAD_TIRE;
                case 1 -> PartRegistry.ALLTERRAIN_TIRE;
                case 2 -> PartRegistry.HEAVY_DUTY_TIRE;
                default -> null;
            };
        } else {
            // Normal vehicle tires: 0=STANDARD, 1=SPORT, 2=PREMIUM
            return switch (index) {
                case 0 -> PartRegistry.STANDARD_TIRE;
                case 1 -> PartRegistry.SPORT_TIRE;
                case 2 -> PartRegistry.PREMIUM_TIRE;
                default -> null;
            };
        }
    }

    private Part getFenderByLevel(int level) {
        return switch (level) {
            case 1 -> PartRegistry.FENDER_BASIC;
            case 2 -> PartRegistry.FENDER_CHROME;
            case 3 -> PartRegistry.FENDER_SPORT;
            default -> null;
        };
    }

    @Override
    public MessageGarageUpgrade fromBytes(FriendlyByteBuf buf) {
        playerUuid = buf.readUUID();
        vehicleUuid = buf.readUUID();
        upgradeType = buf.readEnum(UpgradeType.class);
        value = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeUUID(vehicleUuid);
        buf.writeEnum(upgradeType);
        buf.writeInt(value);
    }
}
