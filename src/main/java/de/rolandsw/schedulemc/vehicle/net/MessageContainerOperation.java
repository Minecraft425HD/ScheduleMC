package de.rolandsw.schedulemc.vehicle.net;

import de.maxhenkel.corelib.net.Message;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.*;
import de.rolandsw.schedulemc.vehicle.items.IVehiclePart;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class MessageContainerOperation implements Message<MessageContainerOperation> {

    public enum Operation {
        INSTALL_ITEM,
        REMOVE_ITEM,
        INSTALL_FLUID,
        REMOVE_FLUID
    }

    private int vehicleId;
    private Operation operation;

    public MessageContainerOperation() {
    }

    public MessageContainerOperation(int vehicleId, Operation operation) {
        this.vehicleId = vehicleId;
        this.operation = operation;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;

        net.minecraft.world.entity.Entity entity = player.level().getEntity(vehicleId);
        if (!(entity instanceof EntityGenericVehicle vehicle)) {
            player.sendSystemMessage(Component.translatable("werkstatt.container.error").withStyle(ChatFormatting.RED));
            return;
        }

        // Permission check
        if (vehicle.getOwnerId() == null || !vehicle.getOwnerId().equals(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("werkstatt.container.error").withStyle(ChatFormatting.RED));
            return;
        }

        // Only trucks allowed
        PartBody chassis = vehicle.getPartByClass(PartBody.class);
        if (!(chassis instanceof PartTruckChassis)) {
            player.sendSystemMessage(Component.translatable("werkstatt.container.truck_only"));
            return;
        }

        switch (operation) {
            case INSTALL_ITEM -> installItemContainer(vehicle, player);
            case REMOVE_ITEM -> removeItemContainer(vehicle, player);
            case INSTALL_FLUID -> installFluidContainer(vehicle, player);
            case REMOVE_FLUID -> removeFluidContainer(vehicle, player);
        }
    }

    private void installItemContainer(EntityGenericVehicle vehicle, ServerPlayer player) {
        // Check if already installed
        if (vehicle.getPartByClass(PartContainer.class) != null) {
            player.sendSystemMessage(Component.literal("Already installed!").withStyle(ChatFormatting.YELLOW));
            return;
        }

        // Calculate cost
        double cost = 0;
        if (vehicle.hasHadItemContainer()) {
            cost = ModConfigHandler.VEHICLE_SERVER.containerReinstallationCost.get();

            // Check balance
            double balance = EconomyManager.getBalance(player.getUUID());
            if (balance < cost) {
                player.sendSystemMessage(
                    Component.translatable("werkstatt.container.not_enough_money", String.format("%.0f", cost))
                        .withStyle(ChatFormatting.RED)
                );
                return;
            }

            // Deduct money
            if (!EconomyManager.withdraw(player.getUUID(), cost)) {
                player.sendSystemMessage(Component.translatable("werkstatt.container.error").withStyle(ChatFormatting.RED));
                return;
            }
        }

        // Add container to vehicle
        ItemStack containerItem = new ItemStack(ModItems.CARGO_MODULE.get());
        Container partInventory = vehicle.getInventoryComponent().getPartInventory();

        // Find empty slot
        boolean added = false;
        for (int i = 0; i < partInventory.getContainerSize(); i++) {
            if (partInventory.getItem(i).isEmpty()) {
                partInventory.setItem(i, containerItem);
                added = true;
                break;
            }
        }

        if (!added) {
            // Refund if no slot available
            if (cost > 0) {
                EconomyManager.deposit(player.getUUID(), cost);
            }
            player.sendSystemMessage(Component.translatable("message.vehicle.no_parts_space"));
            return;
        }

        // Mark as "had container"
        vehicle.setHasHadItemContainer(true);

        // Reinitialize vehicle - CRITICAL: Must reload parts and recalculate inventory!
        vehicle.invalidatePartCache();
        vehicle.initParts(); // Reload parts from part inventory
        vehicle.setPartSerializer(); // Sync parts to client (shows 3D model!)
        vehicle.checkInitializing(); // Recalculate inventory sizes (sets external to 12!)
        vehicle.setIsInitialized(false); // Force re-initialization on client
        vehicle.tryInitPartsAndModel(); // Update 3D models (now actually runs!)

        // Success message
        String costMsg = cost > 0 ? Component.translatable("message.vehicle.install_cost", cost).getString() : Component.translatable("message.vehicle.install_free").getString();
        player.sendSystemMessage(
            Component.translatable("werkstatt.container.installed_successfully")
                .append(Component.literal(costMsg).withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.GREEN)
        );
    }

    private void removeItemContainer(EntityGenericVehicle vehicle, ServerPlayer player) {
        PartContainer container = vehicle.getPartByClass(PartContainer.class);
        if (container == null) {
            player.sendSystemMessage(Component.literal("No container installed!").withStyle(ChatFormatting.YELLOW));
            return;
        }

        // Remove from part inventory
        Container partInventory = vehicle.getInventoryComponent().getPartInventory();
        for (int i = 0; i < partInventory.getContainerSize(); i++) {
            ItemStack stack = partInventory.getItem(i);
            if (stack.getItem() instanceof IVehiclePart vehiclePart) {
                if (vehiclePart.getPart(stack) instanceof PartContainer) {
                    partInventory.removeItem(i, 1);
                    break;
                }
            }
        }

        // Reinitialize - CRITICAL: Must reload parts and recalculate inventory!
        vehicle.invalidatePartCache();
        vehicle.initParts(); // Reload parts from part inventory
        vehicle.setPartSerializer(); // Sync parts to client (updates 3D model!)
        vehicle.checkInitializing(); // Recalculate inventory sizes (sets external back to 0!)
        vehicle.setIsInitialized(false); // Force re-initialization on client
        vehicle.tryInitPartsAndModel(); // Update 3D models

        player.sendSystemMessage(
            Component.translatable("werkstatt.container.removed_successfully").withStyle(ChatFormatting.GREEN)
        );
    }

    private void installFluidContainer(EntityGenericVehicle vehicle, ServerPlayer player) {
        // Check if already installed
        if (vehicle.getPartByClass(PartTankContainer.class) != null) {
            player.sendSystemMessage(Component.literal("Already installed!").withStyle(ChatFormatting.YELLOW));
            return;
        }

        // Calculate cost
        double cost = 0;
        if (vehicle.hasHadFluidContainer()) {
            cost = ModConfigHandler.VEHICLE_SERVER.containerReinstallationCost.get();

            // Check balance
            double balance = EconomyManager.getBalance(player.getUUID());
            if (balance < cost) {
                player.sendSystemMessage(
                    Component.translatable("werkstatt.container.not_enough_money", String.format("%.0f", cost))
                        .withStyle(ChatFormatting.RED)
                );
                return;
            }

            // Deduct money
            if (!EconomyManager.withdraw(player.getUUID(), cost)) {
                player.sendSystemMessage(Component.translatable("werkstatt.container.error").withStyle(ChatFormatting.RED));
                return;
            }
        }

        // Add container to vehicle
        ItemStack containerItem = new ItemStack(ModItems.FLUID_MODULE.get());
        Container partInventory = vehicle.getInventoryComponent().getPartInventory();

        // Find empty slot
        boolean added = false;
        for (int i = 0; i < partInventory.getContainerSize(); i++) {
            if (partInventory.getItem(i).isEmpty()) {
                partInventory.setItem(i, containerItem);
                added = true;
                break;
            }
        }

        if (!added) {
            // Refund if no slot available
            if (cost > 0) {
                EconomyManager.deposit(player.getUUID(), cost);
            }
            player.sendSystemMessage(Component.translatable("message.vehicle.no_parts_space"));
            return;
        }

        // Mark as "had container"
        vehicle.setHasHadFluidContainer(true);

        // Reinitialize vehicle - CRITICAL: Must reload parts and recalculate inventory!
        vehicle.invalidatePartCache();
        vehicle.initParts(); // Reload parts from part inventory
        vehicle.setPartSerializer(); // Sync parts to client (shows 3D model!)
        vehicle.checkInitializing(); // Recalculate inventory sizes
        vehicle.setIsInitialized(false); // Force re-initialization on client
        vehicle.tryInitPartsAndModel(); // Update 3D models

        // Success message
        String costMsg = cost > 0 ? Component.translatable("message.vehicle.install_cost", cost).getString() : Component.translatable("message.vehicle.install_free").getString();
        player.sendSystemMessage(
            Component.translatable("werkstatt.container.installed_successfully")
                .append(Component.literal(costMsg).withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.GREEN)
        );
    }

    private void removeFluidContainer(EntityGenericVehicle vehicle, ServerPlayer player) {
        PartTankContainer container = vehicle.getPartByClass(PartTankContainer.class);
        if (container == null) {
            player.sendSystemMessage(Component.literal("No container installed!").withStyle(ChatFormatting.YELLOW));
            return;
        }

        // Remove from part inventory
        Container partInventory = vehicle.getInventoryComponent().getPartInventory();
        for (int i = 0; i < partInventory.getContainerSize(); i++) {
            ItemStack stack = partInventory.getItem(i);
            if (stack.getItem() instanceof IVehiclePart vehiclePart) {
                if (vehiclePart.getPart(stack) instanceof PartTankContainer) {
                    partInventory.removeItem(i, 1);
                    break;
                }
            }
        }

        // Reinitialize - CRITICAL: Must reload parts and recalculate inventory!
        vehicle.invalidatePartCache();
        vehicle.initParts(); // Reload parts from part inventory
        vehicle.setPartSerializer(); // Sync parts to client (updates 3D model!)
        vehicle.checkInitializing(); // Recalculate inventory sizes (sets external back to 0!)
        vehicle.setIsInitialized(false); // Force re-initialization on client
        vehicle.tryInitPartsAndModel(); // Update 3D models

        player.sendSystemMessage(
            Component.translatable("werkstatt.container.removed_successfully").withStyle(ChatFormatting.GREEN)
        );
    }

    @Override
    public MessageContainerOperation fromBytes(FriendlyByteBuf buf) {
        vehicleId = buf.readInt();
        operation = buf.readEnum(Operation.class);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(vehicleId);
        buf.writeEnum(operation);
    }
}
