package de.rolandsw.schedulemc.vehicle.blocks.entity;

import de.rolandsw.schedulemc.vehicle.fuel.GasStationRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Block entity for gas station.
 * Stores fuel amount, owner, pricing, and station ID.
 */
public class GasStationBlockEntity extends BlockEntity implements MenuProvider {

    private UUID stationId;
    private UUID ownerUUID;
    private int fuelAmount; // in mB (millibuckets)
    private int maxFuelCapacity = 100000; // 100 buckets (100,000 mB)
    private double pricePerMb = 0.01; // Price per millibucket
    private String displayName = "Gas Station";

    public GasStationBlockEntity(BlockPos pos, BlockState state) {
        super(VehicleBlockEntities.GAS_STATION.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.hasUUID("StationId")) {
            this.stationId = tag.getUUID("StationId");
        }
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        this.fuelAmount = tag.getInt("FuelAmount");
        this.maxFuelCapacity = tag.getInt("MaxFuelCapacity");
        this.pricePerMb = tag.getDouble("PricePerMb");
        if (tag.contains("DisplayName")) {
            this.displayName = tag.getString("DisplayName");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (this.stationId != null) {
            tag.putUUID("StationId", this.stationId);
        }
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        tag.putInt("FuelAmount", this.fuelAmount);
        tag.putInt("MaxFuelCapacity", this.maxFuelCapacity);
        tag.putDouble("PricePerMb", this.pricePerMb);
        tag.putString("DisplayName", this.displayName);
    }

    // Getters and Setters

    public UUID getStationId() {
        return stationId;
    }

    public void setStationId(UUID stationId) {
        this.stationId = stationId;
        setChanged();
    }

    public UUID getOwner() {
        return ownerUUID;
    }

    public void setOwner(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        setChanged();
    }

    public boolean isOwner(UUID playerUUID) {
        return this.ownerUUID != null && this.ownerUUID.equals(playerUUID);
    }

    public int getFuelAmount() {
        return fuelAmount;
    }

    public void setFuelAmount(int fuelAmount) {
        this.fuelAmount = Math.max(0, Math.min(fuelAmount, maxFuelCapacity));
        setChanged();
    }

    public void addFuel(int amount) {
        setFuelAmount(this.fuelAmount + amount);
    }

    public void removeFuel(int amount) {
        setFuelAmount(this.fuelAmount - amount);
    }

    public boolean hasEnoughFuel(int amount) {
        return this.fuelAmount >= amount;
    }

    public int getMaxFuelCapacity() {
        return maxFuelCapacity;
    }

    public void setMaxFuelCapacity(int maxFuelCapacity) {
        this.maxFuelCapacity = maxFuelCapacity;
        setChanged();
    }

    public double getPricePerMb() {
        return pricePerMb;
    }

    public void setPricePerMb(double pricePerMb) {
        this.pricePerMb = pricePerMb;
        setChanged();

        // Update in registry
        if (stationId != null) {
            GasStationRegistry.setPricePerMb(stationId, pricePerMb);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        setChanged();

        // Update in registry
        if (stationId != null) {
            GasStationRegistry.setDisplayName(stationId, displayName);
        }
    }

    public double calculateCost(int fuelAmount) {
        return fuelAmount * pricePerMb;
    }

    // MenuProvider implementation (for future GUI)

    @Override
    public Component getDisplayName() {
        return Component.literal(this.displayName);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        // TODO: Implement gas station menu/GUI for managing the station
        // For now, return null (no GUI)
        return null;
    }
}
