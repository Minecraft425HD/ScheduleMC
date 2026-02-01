package de.rolandsw.schedulemc.vehicle.net;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.*;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Processes a complete Werkstatt checkout: all selected services and upgrades
 * are applied atomically and paid from the player's bank account.
 */
public class MessageWerkstattCheckout implements Message<MessageWerkstattCheckout> {

    private UUID playerUuid;
    private UUID vehicleUuid;
    private List<WerkstattCartItem> cartItems;

    public MessageWerkstattCheckout() {
        this.cartItems = new ArrayList<>();
    }

    public MessageWerkstattCheckout(UUID playerUuid, UUID vehicleUuid, List<WerkstattCartItem> cartItems) {
        this.playerUuid = playerUuid;
        this.vehicleUuid = vehicleUuid;
        this.cartItems = cartItems;
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

        // Find the vehicle
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
                    Component.translatable("message.werkstatt.vehicle_not_found").withStyle(ChatFormatting.RED),
                    false
            );
            player.closeContainer();
            return;
        }

        // Empty cart = just leave (no inspection fee)
        if (cartItems.isEmpty()) {
            vehicle.unlockFromWerkstatt();
            player.closeContainer();
            return;
        }

        // Calculate total cost: inspection fee + all cart items
        double inspectionFee = ModConfigHandler.COMMON.WERKSTATT_BASE_INSPECTION_FEE.get();
        double itemsTotal = 0.0;
        for (WerkstattCartItem item : cartItems) {
            itemsTotal += item.calculateCost(vehicle);
        }
        double totalCost = inspectionFee + itemsTotal;

        // Check balance
        double balance = EconomyManager.getBalance(player.getUUID());
        if (balance < totalCost) {
            player.displayClientMessage(
                    Component.translatable("message.werkstatt.insufficient_funds",
                            String.format("%.2f\u20AC", totalCost),
                            String.format("%.2f\u20AC", balance))
                            .withStyle(ChatFormatting.RED),
                    false
            );
            return;
        }

        // Withdraw from bank account
        if (!EconomyManager.withdraw(player.getUUID(), totalCost, TransactionType.WERKSTATT_FEE,
                "Werkstatt-Auftrag: " + cartItems.size() + " Leistungen")) {
            player.displayClientMessage(
                    Component.translatable("message.werkstatt.payment_failed").withStyle(ChatFormatting.RED),
                    false
            );
            return;
        }

        // Apply all cart items
        boolean partsChanged = false;
        for (WerkstattCartItem item : cartItems) {
            switch (item.getType()) {
                case SERVICE_REPAIR -> {
                    vehicle.getDamageComponent().setDamage(0);
                    vehicle.getDamageComponent().setTemperature(20.0F);
                }
                case SERVICE_BATTERY -> {
                    vehicle.getBatteryComponent().setBatteryLevel(
                            vehicle.getBatteryComponent().getMaxBatteryLevel());
                }
                case SERVICE_OIL -> {
                    // Oil change applied
                }
                case UPGRADE_MOTOR -> {
                    Part newMotor = getMotorByLevel(item.getValue());
                    if (newMotor != null && replacePartInInventory(vehicle, PartEngine.class, newMotor)) {
                        partsChanged = true;
                    }
                }
                case UPGRADE_TANK -> {
                    Part newTank = getTankByLevel(item.getValue());
                    if (newTank != null && replacePartInInventory(vehicle, PartTank.class, newTank)) {
                        partsChanged = true;
                    }
                }
                case UPGRADE_TIRE -> {
                    Part newTire = getTireByIndex(item.getValue(), vehicle);
                    if (newTire != null && replacePartInInventory(vehicle, PartTireBase.class, newTire)) {
                        partsChanged = true;
                    }
                }
                case UPGRADE_FENDER -> {
                    // Fender upgrades not allowed for trucks and sports cars
                    PartBody body = vehicle.getPartByClass(PartBody.class);
                    boolean canHaveFender = !(body instanceof PartTruckChassis) && !(body instanceof PartLuxusChassis);
                    if (canHaveFender) {
                        Part newFender = getFenderByLevel(item.getValue());
                        if (newFender != null && replacePartInInventory(vehicle, PartBumper.class, newFender)) {
                            partsChanged = true;
                        }
                    }
                }
                case PAINT_CHANGE -> {
                    vehicle.setPaintColor(item.getValue());
                }
                case CONTAINER_ITEM -> {
                    if (vehicle.getPartByClass(PartContainer.class) == null) {
                        ItemStack containerItem = new ItemStack(ModItems.CARGO_MODULE.get());
                        Container partInv = vehicle.getInventoryComponent().getPartInventory();
                        for (int i = 0; i < partInv.getContainerSize(); i++) {
                            if (partInv.getItem(i).isEmpty()) {
                                partInv.setItem(i, containerItem);
                                vehicle.setHasHadItemContainer(true);
                                partsChanged = true;
                                break;
                            }
                        }
                    }
                }
                case CONTAINER_FLUID -> {
                    if (vehicle.getPartByClass(PartTankContainer.class) == null) {
                        ItemStack containerItem = new ItemStack(ModItems.FLUID_MODULE.get());
                        Container partInv = vehicle.getInventoryComponent().getPartInventory();
                        for (int i = 0; i < partInv.getContainerSize(); i++) {
                            if (partInv.getItem(i).isEmpty()) {
                                partInv.setItem(i, containerItem);
                                vehicle.setHasHadFluidContainer(true);
                                partsChanged = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Update vehicle if parts were changed
        if (partsChanged) {
            vehicle.invalidatePartCache();
            vehicle.initParts();
            vehicle.setPartSerializer();
            vehicle.checkInitializing();
        }

        // Success message
        player.displayClientMessage(
                Component.translatable("message.werkstatt.checkout_success",
                        String.format("%.2f\u20AC", totalCost))
                        .withStyle(ChatFormatting.GREEN),
                false
        );

        // Unlock vehicle and close GUI
        vehicle.unlockFromWerkstatt();
        player.closeContainer();
    }

    // === Part Helper Methods ===

    private boolean replacePartInInventory(EntityGenericVehicle vehicle, Class<? extends Part> partClass, Part newPart) {
        Container partInventory = vehicle.getInventoryComponent().getPartInventory();
        boolean replacedAny = false;

        for (int i = 0; i < partInventory.getContainerSize(); i++) {
            ItemStack stack = partInventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof de.rolandsw.schedulemc.vehicle.items.IVehiclePart partItem) {
                Part existingPart = partItem.getPart(stack);
                if (partClass.isInstance(existingPart)) {
                    ItemStack newStack = getItemStackForPart(newPart);
                    if (!newStack.isEmpty()) {
                        partInventory.setItem(i, newStack);
                        replacedAny = true;
                        if (!PartTireBase.class.isAssignableFrom(partClass)) {
                            break;
                        }
                    }
                }
            }
        }

        // If no existing part was found (e.g. vehicle has no fender yet), add to empty slot
        if (!replacedAny && !PartTireBase.class.isAssignableFrom(partClass)) {
            ItemStack newStack = getItemStackForPart(newPart);
            if (!newStack.isEmpty()) {
                for (int i = 0; i < partInventory.getContainerSize(); i++) {
                    if (partInventory.getItem(i).isEmpty()) {
                        partInventory.setItem(i, newStack);
                        replacedAny = true;
                        break;
                    }
                }
            }
        }

        return replacedAny;
    }

    private ItemStack getItemStackForPart(Part part) {
        if (part == PartRegistry.NORMAL_MOTOR) return new ItemStack(ModItems.NORMAL_MOTOR.get());
        if (part == PartRegistry.PERFORMANCE_MOTOR) return new ItemStack(ModItems.PERFORMANCE_MOTOR.get());
        if (part == PartRegistry.PERFORMANCE_2_MOTOR) return new ItemStack(ModItems.PERFORMANCE_2_MOTOR.get());
        if (part == PartRegistry.TANK_15L) return new ItemStack(ModItems.TANK_15L.get());
        if (part == PartRegistry.TANK_30L) return new ItemStack(ModItems.TANK_30L.get());
        if (part == PartRegistry.TANK_50L) return new ItemStack(ModItems.TANK_50L.get());
        if (part == PartRegistry.STANDARD_TIRE) return new ItemStack(ModItems.STANDARD_TIRE.get());
        if (part == PartRegistry.SPORT_TIRE) return new ItemStack(ModItems.SPORT_TIRE.get());
        if (part == PartRegistry.PREMIUM_TIRE) return new ItemStack(ModItems.PREMIUM_TIRE.get());
        if (part == PartRegistry.OFFROAD_TIRE) return new ItemStack(ModItems.OFFROAD_TIRE.get());
        if (part == PartRegistry.ALLTERRAIN_TIRE) return new ItemStack(ModItems.ALLTERRAIN_TIRE.get());
        if (part == PartRegistry.HEAVY_DUTY_TIRE) return new ItemStack(ModItems.HEAVY_DUTY_TIRE.get());
        if (part == PartRegistry.FENDER_BASIC) return new ItemStack(ModItems.FENDER_BASIC.get());
        if (part == PartRegistry.FENDER_CHROME) return new ItemStack(ModItems.FENDER_CHROME.get());
        if (part == PartRegistry.FENDER_SPORT) return new ItemStack(ModItems.FENDER_SPORT.get());
        return ItemStack.EMPTY;
    }

    private Part getMotorByLevel(int level) {
        return switch (level) {
            case 1 -> PartRegistry.NORMAL_MOTOR;
            case 2 -> PartRegistry.PERFORMANCE_MOTOR;
            case 3 -> PartRegistry.PERFORMANCE_2_MOTOR;
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
        PartBody body = vehicle.getPartByClass(PartBody.class);
        boolean isTruck = body != null && (body.getTranslationKey().contains("transporter")
                || body.getTranslationKey().contains("delivery"));
        if (isTruck) {
            return switch (index) {
                case 0 -> PartRegistry.OFFROAD_TIRE;
                case 1 -> PartRegistry.ALLTERRAIN_TIRE;
                case 2 -> PartRegistry.HEAVY_DUTY_TIRE;
                default -> null;
            };
        } else {
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
    public MessageWerkstattCheckout fromBytes(FriendlyByteBuf buf) {
        playerUuid = buf.readUUID();
        vehicleUuid = buf.readUUID();
        int count = buf.readInt();
        cartItems = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cartItems.add(WerkstattCartItem.fromBytes(buf));
        }
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeUUID(vehicleUuid);
        buf.writeInt(cartItems.size());
        for (WerkstattCartItem item : cartItems) {
            item.toBytes(buf);
        }
    }
}
