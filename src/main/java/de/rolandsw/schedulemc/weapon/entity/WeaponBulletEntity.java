package de.rolandsw.schedulemc.weapon.entity;

import de.rolandsw.schedulemc.weapon.attachment.Attachment;
import de.rolandsw.schedulemc.weapon.item.WeaponItems;
import de.rolandsw.schedulemc.weapon.particle.WeaponParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

public class WeaponBulletEntity extends AbstractArrow {
    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(WeaponBulletEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> AMMO_TYPE =
            SynchedEntityData.defineId(WeaponBulletEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ATTACHMENT =
            SynchedEntityData.defineId(WeaponBulletEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> RANGE =
            SynchedEntityData.defineId(WeaponBulletEntity.class, EntityDataSerializers.INT);

    private double startX, startY, startZ;
    private boolean startPosSet = false;

    public WeaponBulletEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public WeaponBulletEntity(Level level, LivingEntity shooter, float damage, Item ammoType, Attachment attachment, int range) {
        super(WeaponEntities.WEAPON_BULLET.get(), shooter, level);
        this.setNoGravity(true);
        this.entityData.set(DAMAGE, damage);
        this.entityData.set(AMMO_TYPE, ammoType.getDescriptionId());
        this.entityData.set(ATTACHMENT, attachment != null ? attachment.getType().name() : "");
        this.entityData.set(RANGE, range);
        this.startX = this.getX();
        this.startY = this.getY();
        this.startZ = this.getZ();
        this.startPosSet = true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DAMAGE, 5.0f);
        this.entityData.define(AMMO_TYPE, WeaponItems.AMMO_STANDARD.get().getDescriptionId());
        this.entityData.define(ATTACHMENT, "");
        this.entityData.define(RANGE, 100);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (target instanceof LivingEntity living) {
            float damage = this.entityData.get(DAMAGE);
            String ammoTypeId = this.entityData.get(AMMO_TYPE);
            Item ammoType = WeaponItems.ITEMS.getEntries().stream()
                    .map(reg -> reg.get())
                    .filter(item -> item.getDescriptionId().equals(ammoTypeId))
                    .findFirst().orElse(WeaponItems.AMMO_STANDARD.get());

            if (ammoType == WeaponItems.AMMO_AP.get()) {
                damage *= 1.5;
            } else if (ammoType == WeaponItems.AMMO_RUBBER.get()) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                damage *= 0.3;
            }

            Entity owner = this.getOwner();
            living.hurt(living.damageSources().arrow(this, owner instanceof LivingEntity le ? le : null), damage);

            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(WeaponParticles.BLOOD.get(),
                        living.getX(), living.getY() + 1, living.getZ(), 10, 0.2, 0.2, 0.2, 0);
            }
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                    result.getLocation().x, result.getLocation().y, result.getLocation().z,
                    10, 0.1, 0.1, 0.1, 0.1);
        }
        this.discard();
    }

    @Override
    public void tick() {
        if (!startPosSet) {
            startX = this.getX();
            startY = this.getY();
            startZ = this.getZ();
            startPosSet = true;
        }
        super.tick();
        double distSq = this.distanceToSqr(startX, startY, startZ);
        int range = this.entityData.get(RANGE);
        if (distSq > (double) (range * range)) {
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("BulletDamage", this.entityData.get(DAMAGE));
        tag.putString("BulletAmmo", this.entityData.get(AMMO_TYPE));
        tag.putString("BulletAttachment", this.entityData.get(ATTACHMENT));
        tag.putInt("BulletRange", this.entityData.get(RANGE));
        tag.putDouble("StartX", startX);
        tag.putDouble("StartY", startY);
        tag.putDouble("StartZ", startZ);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DAMAGE, tag.getFloat("BulletDamage"));
        this.entityData.set(AMMO_TYPE, tag.getString("BulletAmmo"));
        this.entityData.set(ATTACHMENT, tag.getString("BulletAttachment"));
        this.entityData.set(RANGE, tag.getInt("BulletRange"));
        this.startX = tag.getDouble("StartX");
        this.startY = tag.getDouble("StartY");
        this.startZ = tag.getDouble("StartZ");
        this.startPosSet = true;
    }
}
