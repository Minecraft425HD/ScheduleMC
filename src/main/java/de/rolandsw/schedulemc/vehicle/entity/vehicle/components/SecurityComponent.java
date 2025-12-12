package de.rolandsw.schedulemc.vehicle.entity.vehicle.components;

import de.maxhenkel.corelib.item.ItemUtils;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartLicensePlateHolder;
import de.rolandsw.schedulemc.vehicle.items.ItemKey;
import de.rolandsw.schedulemc.vehicle.items.ItemLicensePlate;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Manages lock system and license plate for the vehicle
 */
public class SecurityComponent extends VehicleComponent {

    private static final EntityDataAccessor<Boolean> LOCKED = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> LICENSE_PLATE = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.STRING);

    public SecurityComponent(EntityGenericVehicle vehicle) {
        super(vehicle);
    }

    public static void defineData(SynchedEntityData entityData) {
        entityData.define(LOCKED, Boolean.FALSE);
        entityData.define(LICENSE_PLATE, "");
    }

    @Override
    public void defineSynchedData() {
        defineData(vehicle.getEntityData());
    }

    @Override
    public boolean onInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // License plate installation
        if (player.isShiftKeyDown() && !isLocked()) {
            if (hasLicensePlateHolder()) {
                if (stack.getItem() instanceof ItemLicensePlate) {
                    String text = ItemLicensePlate.getText(stack);
                    if (!text.isEmpty()) {
                        ItemUtils.decrItemStack(stack, player);
                        player.setItemInHand(hand, stack);
                        setLicensePlate(text);
                        return true;
                    }
                }
            }
        }

        // Key binding
        if (!isLocked() && player.isShiftKeyDown() && player.getAbilities().instabuild && !stack.isEmpty() && stack.getItem().equals(ModItems.KEY.get())) {
            UUID uuid = ItemKey.getCar(stack);
            if (uuid == null) {
                ItemKey.setCar(stack, vehicle.getUUID());
                return true;
            }
        }

        return false;
    }

    public boolean canPlayerEnterCar(Player player) {
        if (isLocked()) {
            player.displayClientMessage(Component.translatable("message.car_locked"), true);
            return false;
        }
        return true;
    }

    public boolean canDestroyCar(Player player) {
        if (isLocked() && !player.hasPermissions(2)) {
            player.displayClientMessage(Component.translatable("message.car_locked"), true);
            return false;
        }
        return true;
    }

    public boolean canPlayerAccessInventoryExternal(Player player) {
        return !isLocked();
    }

    public boolean hasLicensePlateHolder() {
        return vehicle.getPartByClass(PartLicensePlateHolder.class) != null;
    }

    public String getLicensePlate() {
        return vehicle.getEntityData().get(LICENSE_PLATE);
    }

    public void setLicensePlate(String plate) {
        vehicle.getEntityData().set(LICENSE_PLATE, plate);
    }

    public void setLocked(boolean locked, boolean playsound) {
        if (locked && playsound) {
            playLockSound();
        } else if (!locked && playsound) {
            playUnLockSound();
        }

        vehicle.getEntityData().set(LOCKED, locked);
    }

    public boolean isLocked() {
        return vehicle.getEntityData().get(LOCKED);
    }

    public void playLockSound() {
        ModSounds.playSound(getLockSound(), vehicle.level(), vehicle.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public void playUnLockSound() {
        ModSounds.playSound(getUnLockSound(), vehicle.level(), vehicle.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public SoundEvent getLockSound() {
        return ModSounds.CAR_LOCK.get();
    }

    public SoundEvent getUnLockSound() {
        return ModSounds.CAR_UNLOCK.get();
    }

    @Override
    public void saveAdditionalData(CompoundTag compound) {
        compound.putBoolean("locked", isLocked());
        compound.putString("license_plate", getLicensePlate());
    }

    @Override
    public void readAdditionalData(CompoundTag compound) {
        setLocked(compound.getBoolean("locked"), false);
        setLicensePlate(compound.getString("license_plate"));
    }
}
