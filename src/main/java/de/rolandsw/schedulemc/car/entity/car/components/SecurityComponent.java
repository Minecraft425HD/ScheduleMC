package de.rolandsw.schedulemc.car.entity.car.components;

import de.maxhenkel.corelib.item.ItemUtils;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.entity.car.parts.PartLicensePlateHolder;
import de.rolandsw.schedulemc.car.items.ItemKey;
import de.rolandsw.schedulemc.car.items.ItemLicensePlate;
import de.rolandsw.schedulemc.car.items.ModItems;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
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
 * Manages lock system and license plate for the car
 */
public class SecurityComponent extends CarComponent {

    private static final EntityDataAccessor<Boolean> LOCKED = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> LICENSE_PLATE = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.STRING);

    public SecurityComponent(EntityGenericCar car) {
        super(car);
    }

    public static void defineData(SynchedEntityData entityData) {
        entityData.define(LOCKED, Boolean.FALSE);
        entityData.define(LICENSE_PLATE, "");
    }

    @Override
    public void defineSynchedData() {
        defineData(car.getEntityData());
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
                ItemKey.setCar(stack, car.getUUID());
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
        return car.getPartByClass(PartLicensePlateHolder.class) != null;
    }

    public String getLicensePlate() {
        return car.getEntityData().get(LICENSE_PLATE);
    }

    public void setLicensePlate(String plate) {
        car.getEntityData().set(LICENSE_PLATE, plate);
    }

    public void setLocked(boolean locked, boolean playsound) {
        if (locked && playsound) {
            playLockSound();
        } else if (!locked && playsound) {
            playUnLockSound();
        }

        car.getEntityData().set(LOCKED, locked);
    }

    public boolean isLocked() {
        return car.getEntityData().get(LOCKED);
    }

    public void playLockSound() {
        ModSounds.playSound(getLockSound(), car.level(), car.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public void playUnLockSound() {
        ModSounds.playSound(getUnLockSound(), car.level(), car.blockPosition(), null, SoundSource.MASTER, 1F);
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
