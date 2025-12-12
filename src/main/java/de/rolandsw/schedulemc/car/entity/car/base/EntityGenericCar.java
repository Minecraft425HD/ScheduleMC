package de.rolandsw.schedulemc.car.entity.car.base;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.config.Fuel;
import de.rolandsw.schedulemc.car.entity.car.components.*;
import de.rolandsw.schedulemc.car.entity.car.parts.*;
import de.rolandsw.schedulemc.car.items.ICarPart;
import de.rolandsw.schedulemc.car.items.ItemKey;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import de.maxhenkel.corelib.client.obj.OBJModelInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Main car entity using component-based architecture.
 * Inherits directly from EntityVehicleBase, eliminating 8 intermediate classes.
 */
public class EntityGenericCar extends EntityVehicleBase implements Container, IFluidHandler {

    private static final EntityDataAccessor<NonNullList<ItemStack>> PARTS = SynchedEntityData.defineId(EntityGenericCar.class, Main.ITEM_LIST.get());

    // Components - lazy initialization to avoid issues with Entity constructor
    private PhysicsComponent physicsComponent;
    private FuelComponent fuelComponent;
    private BatteryComponent batteryComponent;
    private DamageComponent damageComponent;
    private InventoryComponent inventoryComponent;
    private SecurityComponent securityComponent;

    private List<Part> parts;

    // Vehicle ownership and tracking
    private UUID ownerId;
    private UUID vehicleUUID;
    private BlockPos homeSpawnPoint;

    private boolean isInitialized;
    private boolean isSpawned = true;

    public EntityGenericCar(EntityType type, Level worldIn) {
        super(type, worldIn);
        initializeComponents();
    }

    public EntityGenericCar(Level worldIn) {
        this(Main.CAR_ENTITY_TYPE.get(), worldIn);
    }

    private void initializeComponents() {
        this.physicsComponent = new PhysicsComponent(this);
        this.fuelComponent = new FuelComponent(this);
        this.batteryComponent = new BatteryComponent(this);
        this.damageComponent = new DamageComponent(this);
        this.inventoryComponent = new InventoryComponent(this);
        this.securityComponent = new SecurityComponent(this);
    }

    // Component getters
    public PhysicsComponent getPhysicsComponent() {
        return physicsComponent;
    }

    public FuelComponent getFuelComponent() {
        return fuelComponent;
    }

    public BatteryComponent getBatteryComponent() {
        return batteryComponent;
    }

    public DamageComponent getDamageComponent() {
        return damageComponent;
    }

    public InventoryComponent getInventoryComponent() {
        return inventoryComponent;
    }

    public SecurityComponent getSecurityComponent() {
        return securityComponent;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PARTS, NonNullList.create());

        // Define component data directly (components not yet initialized at this point)
        PhysicsComponent.defineData(this.entityData);
        FuelComponent.defineData(this.entityData);
        BatteryComponent.defineData(this.entityData);
        DamageComponent.defineData(this.entityData);
        SecurityComponent.defineData(this.entityData);
    }

    @Override
    public void tick() {
        super.tick();

        // Tick all components
        physicsComponent.tick();
        fuelComponent.tick();
        damageComponent.tick();

        if (level().isClientSide) {
            batteryComponent.clientTick();
        } else {
            batteryComponent.serverTick();
        }

        tryInitPartsAndModel();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // Security check first
        if (securityComponent.onInteract(player, hand)) {
            return InteractionResult.CONSUME;
        }

        // Damage/repair check
        if (damageComponent.onInteract(player, hand)) {
            return InteractionResult.CONSUME;
        }

        // Inventory interaction
        if (inventoryComponent.onInteract(player, hand)) {
            return InteractionResult.SUCCESS;
        }

        // Default vehicle interaction (entering)
        return super.interact(player, hand);
    }

    public boolean canPlayerEnterCar(Player player) {
        return securityComponent.canPlayerEnterCar(player);
    }

    @Override
    public boolean canCollideWith(Entity entityIn) {
        if (!Main.SERVER_CONFIG.collideWithEntities.get()) {
            if (!(entityIn instanceof EntityVehicleBase)) {
                return false;
            }
        }
        physicsComponent.canCollideWith(entityIn);
        return (entityIn.canBeCollidedWith() || entityIn.isPushable()) && !isPassengerOfSameVehicle(entityIn);
    }

    public void destroyCar(Player player, boolean dropParts) {
        if (!securityComponent.canDestroyCar(player)) {
            return;
        }

        inventoryComponent.dropInventoryContents();
        if (dropParts) {
            inventoryComponent.dropPartInventory();
        }
        kill();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);

        if (!level().isClientSide() && vehicleUUID != null) {
            de.rolandsw.schedulemc.car.vehicle.VehiclePurchaseHandler.onVehicleDestroyed(vehicleUUID);
        }

        // Notify all components
        physicsComponent.onRemove();
        fuelComponent.onRemove();
        batteryComponent.onRemove();
        damageComponent.onRemove();
        inventoryComponent.onRemove();
        securityComponent.onRemove();
    }

    // Owner tracking methods
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setVehicleUUID(UUID vehicleUUID) {
        this.vehicleUUID = vehicleUUID;
    }

    public UUID getVehicleUUID() {
        return vehicleUUID;
    }

    public void setHomeSpawnPoint(BlockPos homeSpawnPoint) {
        this.homeSpawnPoint = homeSpawnPoint;
    }

    public BlockPos getHomeSpawnPoint() {
        return homeSpawnPoint;
    }

    // Parts system
    private List<Part> getCarParts() {
        if (parts == null) {
            parts = new ArrayList<>();
        }
        return parts;
    }

    public <T extends Part> T getPartByClass(Class<T> clazz) {
        for (Part part : getCarParts()) {
            if (clazz.isInstance(part)) {
                return (T) part;
            }
        }
        return null;
    }

    public void setPartSerializer() {
        Container partInv = inventoryComponent.getPartInventory();
        NonNullList<ItemStack> stacks = NonNullList.withSize(partInv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < partInv.getContainerSize(); i++) {
            stacks.set(i, partInv.getItem(i));
        }
        entityData.set(PARTS, stacks);
    }

    private boolean updateClientSideItems() {
        NonNullList<ItemStack> stacks = entityData.get(PARTS);
        if (stacks.isEmpty()) {
            return false;
        }
        Container partInv = inventoryComponent.getPartInventory();
        for (int i = 0; i < stacks.size(); i++) {
            partInv.setItem(i, stacks.get(i));
        }
        return true;
    }

    public void initParts() {
        getCarParts().clear();

        Container partInv = inventoryComponent.getPartInventory();
        for (int i = 0; i < partInv.getContainerSize(); i++) {
            ItemStack stack = partInv.getItem(i);

            if (!(stack.getItem() instanceof ICarPart)) {
                continue;
            }

            ICarPart itemCarPart = (ICarPart) stack.getItem();
            Part part = itemCarPart.getPart(stack);

            if (part == null) {
                continue;
            }

            getCarParts().add(part);
        }

        checkInitializing();
    }

    private void checkInitializing() {
        PartBody body = getPartByClass(PartBody.class);

        if (body instanceof PartBodyTransporter) {
            PartContainer container = getPartByClass(PartContainer.class);
            if (container != null) {
                inventoryComponent.setExternalInventorySize(54);
            } else {
                inventoryComponent.setExternalInventorySize(27);
            }
        }

        PartWheelBase partWheels = getPartByClass(PartWheelBase.class);
        if (partWheels != null) {
            setMaxUpStep(partWheels.getStepHeight());
        }
    }

    public void tryInitPartsAndModel() {
        if (!isInitialized) {
            if (level().isClientSide) {
                if (!isSpawned || updateClientSideItems()) {
                    initParts();
                    initModel();
                    isInitialized = true;
                }
            } else {
                initParts();
                isInitialized = true;
            }
        }
    }

    public void setIsSpawned(boolean isSpawned) {
        this.isSpawned = isSpawned;
    }

    public boolean isSpawned() {
        return isSpawned;
    }

    public List<Part> getModelParts() {
        return Collections.unmodifiableList(getCarParts());
    }

    // Model rendering (client-side)
    private List<OBJModelInstance<EntityGenericCar>> modelInstances = new ArrayList<>();

    protected void initModel() {
        modelInstances.clear();

        boolean addedWheels = false;
        for (Part part : getCarParts()) {
            if (part instanceof PartModel) {
                if (part instanceof PartWheelBase) {
                    if (!addedWheels) {
                        addedWheels = true;
                    } else {
                        continue;
                    }
                }
                modelInstances.addAll(((PartModel) part).getInstances(this));
            }
        }
    }

    public List<OBJModelInstance<EntityGenericCar>> getModels() {
        return modelInstances;
    }

    // Properties delegated to parts
    public float getWheelRotationAmount() {
        PartWheelBase wheel = getPartByClass(PartWheelBase.class);
        if (wheel == null) {
            return 120F * (physicsComponent != null ? physicsComponent.getSpeed() : 0);
        }
        return wheel.getRotationModifier() * (physicsComponent != null ? physicsComponent.getSpeed() : 0);
    }

    public int getFluidInventorySize() {
        PartTankContainer tank = getPartByClass(PartTankContainer.class);
        if (tank == null) {
            return 0;
        }
        return tank.getFluidAmount();
    }

    public float getMaxSpeed() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return 0F;
        }
        PartBody chassis = getPartByClass(PartBody.class);
        if (chassis == null) {
            return 0F;
        }
        return engine.getMaxSpeed() * chassis.getMaxSpeed();
    }

    public float getMaxReverseSpeed() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return 0F;
        }
        return engine.getMaxReverseSpeed();
    }

    public float getAcceleration() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return 0F;
        }
        PartBody chassis = getPartByClass(PartBody.class);
        if (chassis == null) {
            return 0F;
        }
        return engine.getAcceleration() * chassis.getAcceleration();
    }

    public float getMaxRotationSpeed() {
        PartBody chassis = getPartByClass(PartBody.class);
        if (chassis == null) {
            return 5.0F;
        }
        return chassis.getMaxRotationSpeed();
    }

    public float getMinRotationSpeed() {
        PartBody chassis = getPartByClass(PartBody.class);
        if (chassis == null) {
            return 2.0F;
        }
        return chassis.getMinRotationSpeed();
    }

    public float getRollResistance() {
        return 0.02F;
    }

    public float getOptimalTemperature() {
        return 90F;
    }

    public int getMaxFuel() {
        PartTank tank = getPartByClass(PartTank.class);
        if (tank == null) {
            return 1000; // Default tank size when no tank part is installed
        }
        return tank.getSize();
    }

    public float getCarFuelEfficiency() {
        PartEngine engine = getPartByClass(PartEngine.class);
        PartBody chassis = getPartByClass(PartBody.class);

        // If no parts are installed yet, use default efficiency
        if (engine == null || chassis == null) {
            return 1.0F; // Default efficiency when no parts installed
        }

        return chassis.getFuelEfficiency() * engine.getFuelEfficiency();
    }

    public float getRotationModifier() {
        return 0.5F;
    }

    public float getPitch() {
        if (physicsComponent == null) {
            return 0F;
        }
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine instanceof PartEngineTruck) {
            return 1F + 0.35F * Math.abs(physicsComponent.getSpeed()) / getMaxSpeed();
        }
        return Math.abs(physicsComponent.getSpeed()) / getMaxSpeed();
    }

    public double getPlayerYOffset() {
        return 0.2D;
    }

    @Override
    public Vector3d[] getPlayerOffsets() {
        PartBody chassis = getPartByClass(PartBody.class);
        if (chassis == null) {
            return new Vector3d[]{new Vector3d(0.55D, 0D, -0.38D), new Vector3d(0.55D, 0D, 0.38D)};
        }
        return chassis.getPlayerOffsets();
    }

    @Override
    public int getPassengerSize() {
        PartBody chassis = getPartByClass(PartBody.class);
        if (chassis == null) {
            return 0;
        }
        return chassis.getPlayerOffsets().length;
    }

    public Vector3d getLicensePlateOffset() {
        PartBody chassis = getPartByClass(PartBody.class);
        if (chassis == null) {
            return new Vector3d(0F, 0F, 0F);
        }

        PartLicensePlateHolder numberPlate = getPartByClass(PartLicensePlateHolder.class);
        if (numberPlate == null) {
            return new Vector3d(0F, 0F, 0F);
        }
        Vector3d offset = chassis.getNumberPlateOffset();
        Vector3d textOffset = numberPlate.getTextOffset();
        return new Vector3d(offset.x + textOffset.x, -offset.y + textOffset.y, offset.z - textOffset.z);
    }

    @Override
    public boolean doesEnterThirdPerson() {
        return true;
    }

    // Sound events
    public SoundEvent getStopSound() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return ModSounds.ENGINE_STOP.get();
        }
        return engine.getStopSound();
    }

    public SoundEvent getFailSound() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return ModSounds.ENGINE_FAIL.get();
        }
        return engine.getFailSound();
    }

    public SoundEvent getCrashSound() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return ModSounds.CAR_CRASH.get();
        }
        return engine.getCrashSound();
    }

    public SoundEvent getStartSound() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return ModSounds.ENGINE_START.get();
        }
        return engine.getStartSound();
    }

    public SoundEvent getStartingSound() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return ModSounds.ENGINE_STARTING.get();
        }
        return engine.getStartingSound();
    }

    public SoundEvent getIdleSound() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return ModSounds.ENGINE_IDLE.get();
        }
        return engine.getIdleSound();
    }

    public SoundEvent getHighSound() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return ModSounds.ENGINE_HIGH.get();
        }
        return engine.getHighSound();
    }

    public SoundEvent getHornSound() {
        PartEngine engine = getPartByClass(PartEngine.class);
        if (engine == null) {
            return ModSounds.CAR_HORN.get();
        }
        return engine.getHornSound();
    }

    @Override
    protected Component getTypeName() {
        PartBody body = getPartByClass(PartBody.class);
        if (body == null) {
            return super.getTypeName();
        }
        return Component.translatable("car_name." + body.getTranslationKey(), Component.translatable("car_variant." + body.getMaterialTranslationKey()));
    }

    public Component getShortName() {
        PartBody body = getPartByClass(PartBody.class);
        if (body == null) {
            return getTypeName();
        }
        return Component.translatable("car_short_name." + body.getTranslationKey());
    }

    @Override
    public double getCarWidth() {
        PartBody body = getPartByClass(PartBody.class);
        if (body != null) {
            return body.getWidth();
        }
        return super.getCarWidth();
    }

    @Override
    public double getCarHeight() {
        PartBody body = getPartByClass(PartBody.class);
        if (body != null) {
            return body.getHeight();
        }
        return super.getCarHeight();
    }

    // NBT serialization
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        // Load owner tracking data
        if (compound.contains("OwnerId")) {
            this.ownerId = compound.getUUID("OwnerId");
        }
        if (compound.contains("VehicleUUID")) {
            this.vehicleUUID = compound.getUUID("VehicleUUID");
        }
        if (compound.contains("HomeSpawnX")) {
            int x = compound.getInt("HomeSpawnX");
            int y = compound.getInt("HomeSpawnY");
            int z = compound.getInt("HomeSpawnZ");
            this.homeSpawnPoint = new BlockPos(x, y, z);
        }

        // Initialize default items if this is a new car
        if (compound.getAllKeys().stream().allMatch(s -> s.equals("id"))) {
            Container internal = inventoryComponent.getInternalInventory();
            internal.setItem(0, ItemKey.getKeyForCar(getUUID()));
            internal.setItem(1, ItemKey.getKeyForCar(getUUID()));
            fuelComponent.setFuelAmount(100);
            batteryComponent.setBatteryLevel(500);
            damageComponent.initTemperature();
        }

        // Load all component data
        physicsComponent.readAdditionalData(compound);
        fuelComponent.readAdditionalData(compound);
        batteryComponent.readAdditionalData(compound);
        damageComponent.readAdditionalData(compound);
        inventoryComponent.readAdditionalData(compound);
        securityComponent.readAdditionalData(compound);

        setPartSerializer();
        tryInitPartsAndModel();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        // Save owner tracking data
        if (this.ownerId != null) {
            compound.putUUID("OwnerId", this.ownerId);
        }
        if (this.vehicleUUID != null) {
            compound.putUUID("VehicleUUID", this.vehicleUUID);
        }
        if (this.homeSpawnPoint != null) {
            compound.putInt("HomeSpawnX", this.homeSpawnPoint.getX());
            compound.putInt("HomeSpawnY", this.homeSpawnPoint.getY());
            compound.putInt("HomeSpawnZ", this.homeSpawnPoint.getZ());
        }

        // Save all component data
        physicsComponent.saveAdditionalData(compound);
        fuelComponent.saveAdditionalData(compound);
        batteryComponent.saveAdditionalData(compound);
        damageComponent.saveAdditionalData(compound);
        inventoryComponent.saveAdditionalData(compound);
        securityComponent.saveAdditionalData(compound);
    }

    // Container implementation (delegates to InventoryComponent)
    @Override
    public int getContainerSize() {
        return inventoryComponent.getInternalInventory().getContainerSize();
    }

    @Override
    public ItemStack getItem(int index) {
        return inventoryComponent.getInternalInventory().getItem(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return inventoryComponent.getInternalInventory().removeItem(index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return inventoryComponent.getInternalInventory().removeItemNoUpdate(index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        inventoryComponent.getInternalInventory().setItem(index, stack);
    }

    @Override
    public int getMaxStackSize() {
        return inventoryComponent.getInternalInventory().getMaxStackSize();
    }

    @Override
    public void setChanged() {
        inventoryComponent.getInternalInventory().setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return inventoryComponent.getInternalInventory().stillValid(player);
    }

    @Override
    public boolean isEmpty() {
        return inventoryComponent.getInternalInventory().isEmpty();
    }

    @Override
    public void startOpen(Player player) {
        inventoryComponent.getInternalInventory().startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        inventoryComponent.getInternalInventory().stopOpen(player);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return inventoryComponent.getInternalInventory().canPlaceItem(index, stack);
    }

    @Override
    public void clearContent() {
        inventoryComponent.getInternalInventory().clearContent();
    }

    public Container getExternalInventory() {
        return inventoryComponent.getExternalInventory();
    }

    public Container getPartInventory() {
        return inventoryComponent.getPartInventory();
    }

    // IFluidHandler implementation (delegates to FuelComponent)
    @Override
    public int getTanks() {
        return fuelComponent.getTanks();
    }

    @Override
    @NotNull
    public net.minecraftforge.fluids.FluidStack getFluidInTank(int tank) {
        return fuelComponent.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return fuelComponent.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull net.minecraftforge.fluids.FluidStack stack) {
        return fuelComponent.isFluidValid(tank, stack);
    }

    @Override
    public int fill(net.minecraftforge.fluids.FluidStack resource, IFluidHandler.FluidAction action) {
        return fuelComponent.fill(resource, action);
    }

    @Override
    @NotNull
    public net.minecraftforge.fluids.FluidStack drain(net.minecraftforge.fluids.FluidStack resource, IFluidHandler.FluidAction action) {
        return fuelComponent.drain(resource, action);
    }

    @Override
    @NotNull
    public net.minecraftforge.fluids.FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        return fuelComponent.drain(maxDrain, action);
    }

    // Capability support
    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable net.minecraft.core.Direction side) {
        if (cap.equals(ForgeCapabilities.FLUID_HANDLER)) {
            return LazyOptional.of(() -> (T) fuelComponent);
        }
        return super.getCapability(cap, side);
    }

    public void centerCar() {
        net.minecraft.core.Direction facing = getDirection();
        switch (facing) {
            case SOUTH:
                setYRot(0F);
                break;
            case NORTH:
                setYRot(180F);
                break;
            case EAST:
                setYRot(-90F);
                break;
            case WEST:
                setYRot(90F);
                break;
        }
    }

    // Additional delegation methods for compatibility

    // Battery component delegates
    public float getBatterySoundPitchLevel() {
        return batteryComponent.getBatterySoundPitchLevel();
    }

    public boolean isStarting() {
        return batteryComponent.isStarting();
    }

    public void setBatteryLevel(int level) {
        batteryComponent.setBatteryLevel(level);
    }

    // Physics component delegates
    public boolean isStarted() {
        return physicsComponent.isStarted();
    }

    public float getSpeed() {
        return physicsComponent.getSpeed();
    }

    public float getKilometerPerHour() {
        return physicsComponent.getKilometerPerHour();
    }

    public float getWheelRotation(float partialTicks) {
        return physicsComponent.getWheelRotation(partialTicks);
    }

    public void updateControls(boolean forward, boolean backward, boolean left, boolean right, Player player) {
        physicsComponent.updateControls(forward, backward, left, right);
    }

    public void updateControls(boolean forward, boolean backward, boolean left, boolean right, net.minecraft.server.level.ServerPlayer player) {
        physicsComponent.updateControls(forward, backward, left, right);
    }

    public void openCarGUI(Player player) {
        inventoryComponent.openCarGUI(player);
    }

    public void openCarGUI(net.minecraft.server.level.ServerPlayer player) {
        inventoryComponent.openCarGUI(player);
    }

    // Damage component delegates
    public void onCollision(float speed) {
        damageComponent.onCollision(speed);
    }

    public void initTemperature() {
        damageComponent.initTemperature();
    }

    // Fuel component delegates
    public void setFuelAmount(int amount) {
        fuelComponent.setFuelAmount(amount);
    }

    public int getFuelAmount() {
        return fuelComponent.getFuelAmount();
    }

    // Security component delegates
    public String getLicensePlate() {
        return securityComponent.getLicensePlate();
    }

    public boolean isLocked() {
        return securityComponent.isLocked();
    }

    public void setLocked(boolean locked, boolean playSound) {
        securityComponent.setLocked(locked, playSound);
    }

    // Utility methods for component access to protected fields
    public net.minecraft.util.RandomSource getRandom() {
        return this.random;
    }
}
