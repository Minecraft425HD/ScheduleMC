package de.rolandsw.schedulemc.vehicle.component.control;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Component representing vehicle ownership and security.
 * Handles ownership, locking, and access control.
 */
public class OwnershipComponent extends BaseComponent {

    @Nullable
    private UUID ownerId;
    @Nullable
    private String ownerName;

    private boolean locked = false;
    private boolean requiresKey = false;

    @Nullable
    private UUID keyItemId; // UUID of the key item

    public OwnershipComponent() {
        super(ComponentType.SECURITY);
    }

    // Owner management
    @Nullable
    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwner(UUID ownerId, String ownerName) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }

    public void clearOwner() {
        this.ownerId = null;
        this.ownerName = null;
    }

    @Nullable
    public String getOwnerName() {
        return ownerName;
    }

    public boolean hasOwner() {
        return ownerId != null;
    }

    public boolean isOwner(Player player) {
        return ownerId != null && ownerId.equals(player.getUUID());
    }

    // Lock management
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void toggleLock() {
        this.locked = !this.locked;
    }

    public boolean requiresKey() {
        return requiresKey;
    }

    public void setRequiresKey(boolean requiresKey) {
        this.requiresKey = requiresKey;
    }

    // Key management
    @Nullable
    public UUID getKeyItemId() {
        return keyItemId;
    }

    public void setKeyItemId(@Nullable UUID keyId) {
        this.keyItemId = keyId;
    }

    /**
     * Checks if a player can access this vehicle.
     */
    public boolean canAccess(Player player) {
        // No owner means anyone can access
        if (ownerId == null) {
            return true;
        }

        // Owner can always access
        if (isOwner(player)) {
            return true;
        }

        // If not locked, allow access
        if (!locked) {
            return true;
        }

        // If locked and requires key, check if player has key
        // (Key check would be implemented in a system)
        return false;
    }

    /**
     * Checks if a player can modify this vehicle.
     */
    public boolean canModify(Player player) {
        // Only owner can modify
        return ownerId == null || isOwner(player);
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);

        if (ownerId != null) {
            tag.putUUID("OwnerId", ownerId);
        }
        if (ownerName != null) {
            tag.putString("OwnerName", ownerName);
        }

        tag.putBoolean("Locked", locked);
        tag.putBoolean("RequiresKey", requiresKey);

        if (keyItemId != null) {
            tag.putUUID("KeyItemId", keyItemId);
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.hasUUID("OwnerId")) {
            this.ownerId = tag.getUUID("OwnerId");
        }
        if (tag.contains("OwnerName")) {
            this.ownerName = tag.getString("OwnerName");
        }

        this.locked = tag.getBoolean("Locked");
        this.requiresKey = tag.getBoolean("RequiresKey");

        if (tag.hasUUID("KeyItemId")) {
            this.keyItemId = tag.getUUID("KeyItemId");
        }
    }

    @Override
    public IVehicleComponent duplicate() {
        OwnershipComponent copy = new OwnershipComponent();
        copy.ownerId = this.ownerId;
        copy.ownerName = this.ownerName;
        copy.locked = this.locked;
        copy.requiresKey = this.requiresKey;
        copy.keyItemId = this.keyItemId;
        return copy;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
