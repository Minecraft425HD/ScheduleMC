package de.rolandsw.schedulemc.vehicle.entity.vehicle.base;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.config.Fuel;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.components.*;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.*;
import de.rolandsw.schedulemc.vehicle.items.IVehiclePart;
import de.rolandsw.schedulemc.vehicle.items.ItemKey;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry;
import de.maxhenkel.corelib.client.obj.OBJModelInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Main vehicle entity using component-based architecture.
 * Inherits directly from EntityVehicleBase, eliminating 8 intermediate classes.
 */
public class EntityGenericVehicle extends EntityVehicleBase implements Container, IFluidHandler {

    private static final EntityDataAccessor<NonNullList<ItemStack>> PARTS = SynchedEntityData.defineId(EntityGenericVehicle.class, Main.ITEM_LIST.get());
    private static final EntityDataAccessor<Integer> PAINT_COLOR = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_ON_TOWING_YARD = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_INITIALIZED = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> INTERNAL_INV_SIZE = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXTERNAL_INV_SIZE = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.INT);

    // Components - lazy initialization to avoid issues with Entity constructor
    private PhysicsComponent physicsComponent;
    private FuelComponent fuelComponent;
    private BatteryComponent batteryComponent;
    private DamageComponent damageComponent;
    private InventoryComponent inventoryComponent;
    private SecurityComponent securityComponent;

    private List<Part> parts;
    // Optimierung: Cache für Part-Lookups (vermeidet 30+ Iterationen pro Tick)
    private Map<Class<? extends Part>, Part> partCache;
    private boolean partCacheValid = false;  // OPTIMIERT: Cache-Invalidierungs-Flag

    // Vehicle ownership and tracking
    private UUID ownerId;
    private UUID vehicleUUID;
    private BlockPos homeSpawnPoint;
    private boolean spawnPointReleased = false; // Track if spawn point was released when driving away

    // Garage locking system
    private boolean isLockedInGarage;
    @Nullable
    private BlockPos garagePosition;

    // Container installation tracking (for cost system)
    // First installation is free, reinstallation after removal costs money
    private boolean hasHadItemContainer = false;
    private boolean hasHadFluidContainer = false;

    private boolean isSpawned = true;

    public EntityGenericVehicle(EntityType type, Level worldIn) {
        super(type, worldIn);
        initializeComponents();
    }

    public EntityGenericVehicle(Level worldIn) {
        this(Main.VEHICLE_ENTITY_TYPE.get(), worldIn);
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
        this.entityData.define(PAINT_COLOR, 0); // Default: 0 = white
        this.entityData.define(IS_ON_TOWING_YARD, false); // Default: not on towing yard
        this.entityData.define(IS_INITIALIZED, false); // Default: not initialized
        this.entityData.define(INTERNAL_INV_SIZE, 0); // Default: 0 internal slots
        this.entityData.define(EXTERNAL_INV_SIZE, 0); // Default: 0 external slots

        // Define component data directly (components not yet initialized at this point)
        PhysicsComponent.defineData(this.entityData);
        FuelComponent.defineData(this.entityData);
        BatteryComponent.defineData(this.entityData);
        DamageComponent.defineData(this.entityData);
        SecurityComponent.defineData(this.entityData);
    }

    @Override
    public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        // When PARTS data changes from server, re-initialize on client
        if (level().isClientSide && key.equals(PARTS)) {
            // Force re-initialization on next tick
            setIsInitialized(false);
        }

        // When inventory sizes change, update the inventory component
        if (key.equals(INTERNAL_INV_SIZE) || key.equals(EXTERNAL_INV_SIZE)) {
            if (level().isClientSide) {
                // Apply synced inventory sizes immediately on client
                inventoryComponent.setInternalInventorySize(entityData.get(INTERNAL_INV_SIZE));
                inventoryComponent.setExternalInventorySize(entityData.get(EXTERNAL_INV_SIZE));
            }
        }
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

            // Prüfe ob Spawnpunkt freigegeben werden soll (nur alle 20 Ticks = 1 Sekunde)
            if (tickCount % 20 == 0) {
                checkAndReleaseSpawnPoint();
            }
        }

        tryInitPartsAndModel();
    }

    /**
     * Prüft ob das Fahrzeug sich vom Spawnpunkt entfernt hat und gibt ihn frei.
     * Wird aufgerufen wenn das Fahrzeug gekauft wurde und sich dann bewegt.
     */
    private void checkAndReleaseSpawnPoint() {
        // Nur prüfen wenn wir einen Spawnpunkt haben und er noch nicht freigegeben wurde
        if (homeSpawnPoint == null || spawnPointReleased || vehicleUUID == null) {
            return;
        }

        // Berechne Distanz zum Spawnpunkt (horizontal, ignoriere Y für einfachere Berechnung)
        double distanceSquared = blockPosition().distSqr(homeSpawnPoint);

        // Wenn mehr als 5 Blöcke entfernt (25 = 5²), gib Spawnpunkt frei
        if (distanceSquared > 25) {
            VehicleSpawnRegistry.releaseSpawnPoint(vehicleUUID);
            VehicleSpawnRegistry.saveIfNeeded();
            spawnPointReleased = true;
        }
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

    public boolean canPlayerEnterVehicle(Player player) {
        return securityComponent.canPlayerEnterVehicle(player);
    }

    @Override
    public boolean canCollideWith(Entity entityIn) {
        if (!ModConfigHandler.VEHICLE_SERVER.collideWithEntities.get()) {
            if (!(entityIn instanceof EntityVehicleBase)) {
                return false;
            }
        }
        physicsComponent.canCollideWith(entityIn);
        if (!entityIn.canBeCollidedWith() && !entityIn.isPushable()) {
            return false;
        }
        return !isPassengerOfSameVehicle(entityIn);
    }

    public void destroyVehicle(Player player, boolean dropParts) {
        if (!securityComponent.canDestroyVehicle(player)) {
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
            de.rolandsw.schedulemc.vehicle.vehicle.VehiclePurchaseHandler.onVehicleDestroyed(vehicleUUID);
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
    private List<Part> getVehicleParts() {
        if (parts == null) {
            // Optimierung: Initial capacity = 15 (max Part-Inventory Größe)
            parts = new ArrayList<>(15);
        }
        return parts;
    }

    public <T extends Part> T getPartByClass(Class<T> clazz) {
        // OPTIMIERT: Re-validiere Cache bei Invalidierung
        if (!partCacheValid && partCache != null) {
            initParts();  // Re-initialisiert Cache
        }

        // Optimierung: Nutze Cache für schnelle Lookups
        if (partCache != null) {
            @SuppressWarnings("unchecked")
            T cached = (T) partCache.get(clazz);
            if (cached != null) {
                return cached;
            }
        }

        // Fallback: Suche in Parts-Liste (sollte nur bei Cache-Miss passieren)
        for (Part part : getVehicleParts()) {
            if (clazz.isInstance(part)) {
                return (T) part;
            }
        }
        return null;
    }

    /**
     * Invalidiert den Part-Cache
     * OPTIMIERT: Muss aufgerufen werden wenn Parts geändert werden
     */
    public void invalidatePartCache() {
        partCacheValid = false;
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
        getVehicleParts().clear();

        // Optimierung: Initialisiere Cache mit geschätzter Größe
        partCache = new HashMap<>(8); // Typisch ~8 verschiedene Part-Typen

        Container partInv = inventoryComponent.getPartInventory();

        for (int i = 0; i < partInv.getContainerSize(); i++) {
            ItemStack stack = partInv.getItem(i);
            if (stack.getItem() instanceof IVehiclePart) {
                Part part = ((IVehiclePart) stack.getItem()).getPart(stack);
                if (part != null) {
                    getVehicleParts().add(part);
                    partCache.put(part.getClass(), part);
                }
            }
        }

        // OPTIMIERT: Markiere Cache als valide
        partCacheValid = true;

        checkInitializing();
    }

    /**
     * Recalculates vehicle properties based on installed parts and current config values.
     * This includes:
     * - External inventory size (from chassis config or container part)
     * - Step height (from tire parts)
     *
     * IMPORTANT: This method should be called:
     * - When parts are added/removed (via initParts)
     * - When loading from NBT (to apply updated config values)
     * - When config values change (to update existing vehicles)
     */
    public void checkInitializing() {
        PartBody body = getPartByClass(PartBody.class);

        // Calculate internal and external inventory sizes
        int internalSlots = 0;
        int externalSlots = 0;

        if (body != null) {
            // Internal inventory is always chassis-specific (4/6/0/3/6)
            internalSlots = body.getInternalInventorySize();

            // External inventory is ONLY for containers (mounted externally)
            if (body instanceof PartTruckChassis) {
                PartContainer container = getPartByClass(PartContainer.class);
                if (container != null) {
                    externalSlots = container.getSlotCount(); // 12 Slots (external container)
                }
                // Otherwise: externalSlots = 0 (no container mounted)
            }
            // For other vehicles: externalSlots = 0 (containers not allowed)

            if (!level().isClientSide) {
                de.rolandsw.schedulemc.ScheduleMC.LOGGER.debug(
                    "[VEHICLE INVENTORY] Chassis: {}, Internal: {}, External: {}",
                    body.getClass().getSimpleName(),
                    internalSlots,
                    externalSlots
                );
            }
        }

        // CRITICAL: Sync inventory sizes via entityData so client always has correct sizes!
        this.entityData.set(INTERNAL_INV_SIZE, internalSlots);
        this.entityData.set(EXTERNAL_INV_SIZE, externalSlots);

        inventoryComponent.setInternalInventorySize(internalSlots);
        inventoryComponent.setExternalInventorySize(externalSlots);

        PartTireBase partWheels = getPartByClass(PartTireBase.class);
        if (partWheels != null) {
            setMaxUpStep(partWheels.getStepHeight());
        }
    }

    public void tryInitPartsAndModel() {
        if (!isInitialized()) {
            if (level().isClientSide) {
                if (!isSpawned || updateClientSideItems()) {
                    initParts();
                    checkInitializing(); // CRITICAL: Recalculate inventory sizes based on loaded parts!
                    initModel();
                    setIsInitialized(true);
                }
            } else {
                initParts();
                checkInitializing(); // CRITICAL: Recalculate inventory sizes based on loaded parts!
                setIsInitialized(true);
            }
        }
    }

    public void setIsSpawned(boolean isSpawned) {
        this.isSpawned = isSpawned;
    }

    public boolean isSpawned() {
        return isSpawned;
    }

    public void setIsInitialized(boolean isInitialized) {
        this.entityData.set(IS_INITIALIZED, isInitialized);
    }

    public boolean isInitialized() {
        return this.entityData.get(IS_INITIALIZED);
    }

    public int getSyncedInternalInventorySize() {
        return this.entityData.get(INTERNAL_INV_SIZE);
    }

    public int getSyncedExternalInventorySize() {
        return this.entityData.get(EXTERNAL_INV_SIZE);
    }

    public List<Part> getModelParts() {
        return Collections.unmodifiableList(getVehicleParts());
    }

    // Model rendering (client-side)
    // Optimierung: Initial capacity = 8 (typisch ~8 Model-Parts pro Vehicle)
    private List<OBJModelInstance<EntityGenericVehicle>> modelInstances = new ArrayList<>(8);

    protected void initModel() {
        modelInstances.clear();

        // Optimierung: Stream-API mit State für Wheel-Deduplication
        final boolean[] addedWheels = {false};
        getVehicleParts().stream()
            .filter(part -> part instanceof PartModel)
            .filter(part -> {
                // Nur erste PartTireBase hinzufügen (Duplikate überspringen)
                if (part instanceof PartTireBase) {
                    if (addedWheels[0]) {
                        return false;
                    }
                    addedWheels[0] = true;
                }
                return true;
            })
            .forEach(part -> modelInstances.addAll(((PartModel) part).getInstances(this)));
    }

    public List<OBJModelInstance<EntityGenericVehicle>> getModels() {
        return modelInstances;
    }

    // Properties delegated to parts
    public float getWheelRotationAmount() {
        float speed = physicsComponent != null ? physicsComponent.getSpeed() : 0F;
        PartTireBase tire = getPartByClass(PartTireBase.class);
        float rotMod = tire != null ? tire.getRotationModifier() : 120F;
        return rotMod * speed;
    }

    public int getFluidInventorySize() {
        PartTankContainer tank = getPartByClass(PartTankContainer.class);
        return tank != null ? tank.getFluidAmount() : 0;
    }

    public float getMaxSpeed() {
        PartEngine engine = getPartByClass(PartEngine.class);
        PartBody body = getPartByClass(PartBody.class);
        if (engine == null || body == null) return 0F;
        return engine.getMaxSpeed() * body.getMaxSpeed();
    }

    public float getMaxReverseSpeed() {
        PartEngine engine = getPartByClass(PartEngine.class);
        return engine != null ? engine.getMaxReverseSpeed() : 0F;
    }

    public float getAcceleration() {
        PartEngine engine = getPartByClass(PartEngine.class);
        PartBody body = getPartByClass(PartBody.class);
        if (engine == null || body == null) return 0F;
        return engine.getAcceleration() * body.getAcceleration();
    }

    public float getMaxRotationSpeed() {
        PartBody body = getPartByClass(PartBody.class);
        return body != null ? body.getMaxRotationSpeed() : 5.0F;
    }

    public float getMinRotationSpeed() {
        PartBody body = getPartByClass(PartBody.class);
        return body != null ? body.getMinRotationSpeed() : 2.0F;
    }

    public float getRollResistance() {
        return 0.02F;
    }

    public float getOptimalTemperature() {
        return 90F;
    }

    // ═══════════════════════════════════════════════════════════
    // ODOMETER (Delegate to PhysicsComponent)
    // ═══════════════════════════════════════════════════════════

    public long getOdometer() {
        return physicsComponent != null ? physicsComponent.getOdometer() : 0L;
    }

    public void setOdometer(long value) {
        if (physicsComponent != null) {
            physicsComponent.setOdometer(value);
        }
    }

    /**
     * Maximale Gesundheit in Prozent (0.0 - 1.0) basierend auf Kilometerstand.
     * 100% bei 0-250k Blöcken, 75% bei 250k-500k, 50% bei 500k-750k, 25% bei 750k+
     */
    public float getMaxHealthPercent() {
        return physicsComponent != null ? physicsComponent.getMaxHealthPercent() : 1.0F;
    }

    public int getMaxFuel() {
        PartTank tank = getPartByClass(PartTank.class);
        return tank != null ? tank.getSize() : 10000;
    }

    public float getVehicleFuelEfficiency() {
        PartEngine engine = getPartByClass(PartEngine.class);
        PartBody body = getPartByClass(PartBody.class);
        float efficiency = (engine != null && body != null)
            ? body.getFuelEfficiency() * engine.getFuelEfficiency()
            : 1.0F;
        return efficiency <= 0.0F ? 1.0F : efficiency;
    }

    public float getRotationModifier() {
        return 0.5F;
    }

    public float getPitch() {
        if (physicsComponent == null) return 0F;
        float maxSpd = getMaxSpeed();
        if (maxSpd <= 0F) return 0F;
        float speed = Math.abs(physicsComponent.getSpeed()) / maxSpd;
        PartEngine engine = getPartByClass(PartEngine.class);
        return engine instanceof PartIndustrialMotor ? 1F + 0.35F * speed : speed;
    }

    public double getPlayerYOffset() {
        return 0.2D;
    }

    @Override
    public Vector3d[] getPlayerOffsets() {
        PartBody body = getPartByClass(PartBody.class);
        return body != null ? body.getPlayerOffsets()
            : new Vector3d[]{new Vector3d(0.55D, 0D, -0.38D), new Vector3d(0.55D, 0D, 0.38D)};
    }

    @Override
    public int getPassengerSize() {
        PartBody body = getPartByClass(PartBody.class);
        return body != null ? body.getPlayerOffsets().length : 0;
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
    private SoundEvent getEngineSoundOrDefault(java.util.function.Function<PartEngine, SoundEvent> getter, java.util.function.Supplier<SoundEvent> fallback) {
        PartEngine engine = getPartByClass(PartEngine.class);
        return engine != null ? getter.apply(engine) : fallback.get();
    }

    public SoundEvent getStopSound() {
        return getEngineSoundOrDefault(PartEngine::getStopSound, () -> ModSounds.ENGINE_STOP.get());
    }

    public SoundEvent getFailSound() {
        return getEngineSoundOrDefault(PartEngine::getFailSound, () -> ModSounds.ENGINE_FAIL.get());
    }

    public SoundEvent getCrashSound() {
        return getEngineSoundOrDefault(PartEngine::getCrashSound, () -> ModSounds.VEHICLE_CRASH.get());
    }

    public SoundEvent getStartSound() {
        return getEngineSoundOrDefault(PartEngine::getStartSound, () -> ModSounds.ENGINE_START.get());
    }

    public SoundEvent getStartingSound() {
        return getEngineSoundOrDefault(PartEngine::getStartingSound, () -> ModSounds.ENGINE_STARTING.get());
    }

    public SoundEvent getIdleSound() {
        return getEngineSoundOrDefault(PartEngine::getIdleSound, () -> ModSounds.ENGINE_IDLE.get());
    }

    public SoundEvent getHighSound() {
        return getEngineSoundOrDefault(PartEngine::getHighSound, () -> ModSounds.ENGINE_HIGH.get());
    }

    public SoundEvent getHornSound() {
        return getEngineSoundOrDefault(PartEngine::getHornSound, () -> ModSounds.VEHICLE_HORN.get());
    }

    // Garage locking system methods
    public void lockInGarage(BlockPos garagePos) {
        this.isLockedInGarage = true;
        this.garagePosition = garagePos;
        // Stop all movement
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void unlockFromGarage() {
        this.isLockedInGarage = false;
        this.garagePosition = null;
    }

    public boolean isLockedInGarage() {
        return isLockedInGarage;
    }

    @Nullable
    public BlockPos getGaragePosition() {
        return garagePosition;
    }

    public boolean canMove() {
        return !isLockedInGarage;
    }

    // Container installation tracking
    public boolean hasHadItemContainer() {
        return hasHadItemContainer;
    }

    public void setHasHadItemContainer(boolean hasHad) {
        this.hasHadItemContainer = hasHad;
    }

    public boolean hasHadFluidContainer() {
        return hasHadFluidContainer;
    }

    public void setHasHadFluidContainer(boolean hasHad) {
        this.hasHadFluidContainer = hasHad;
    }

    @Override
    protected Component getTypeName() {
        PartBody body = getPartByClass(PartBody.class);
        if (body == null) {
            return super.getTypeName();
        }
        return Component.translatable("vehicle_name." + body.getTranslationKey(),
            Component.translatable("vehicle_variant." + body.getMaterialTranslationKey()));
    }

    public Component getShortName() {
        PartBody body = getPartByClass(PartBody.class);
        if (body == null) {
            return this.getTypeName();
        }
        return Component.translatable("vehicle_short_name." + body.getTranslationKey());
    }

    @Override
    public double getVehicleWidth() {
        PartBody body = getPartByClass(PartBody.class);
        if (body == null) {
            return super.getVehicleWidth();
        }
        return body.getWidth();
    }

    @Override
    public double getVehicleHeight() {
        PartBody body = getPartByClass(PartBody.class);
        if (body == null) {
            return super.getVehicleHeight();
        }
        return body.getHeight();
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
        if (compound.contains("SpawnPointReleased")) {
            this.spawnPointReleased = compound.getBoolean("SpawnPointReleased");
        }

        // Load garage locking data
        if (compound.contains("IsLockedInGarage")) {
            this.isLockedInGarage = compound.getBoolean("IsLockedInGarage");
        }
        if (compound.contains("GarageX")) {
            int x = compound.getInt("GarageX");
            int y = compound.getInt("GarageY");
            int z = compound.getInt("GarageZ");
            this.garagePosition = new BlockPos(x, y, z);
        }

        // Load paint color
        if (compound.contains("PaintColor")) {
            setPaintColor(compound.getInt("PaintColor"));
        }

        // Load towing yard flag
        if (compound.contains("IsOnTowingYard")) {
            setIsOnTowingYard(compound.getBoolean("IsOnTowingYard"));
        }

        // Load container installation tracking
        if (compound.contains("HasHadItemContainer")) {
            this.hasHadItemContainer = compound.getBoolean("HasHadItemContainer");
        }
        if (compound.contains("HasHadFluidContainer")) {
            this.hasHadFluidContainer = compound.getBoolean("HasHadFluidContainer");
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

        // CRITICAL: Recalculate inventory size to apply current config values
        // This ensures that config changes are applied to existing vehicles when loaded from NBT
        checkInitializing();

        // Initialize default items if this is a new vehicle (MUST be AFTER checkInitializing!)
        if (compound.getAllKeys().stream().allMatch(s -> s.equals("id"))) {
            Container internal = inventoryComponent.getInternalInventory();
            internal.setItem(0, ItemKey.getKeyForVehicle(getUUID()));
            internal.setItem(1, ItemKey.getKeyForVehicle(getUUID()));
            fuelComponent.setFuelAmount(100);
            batteryComponent.setBatteryLevel(500);
            damageComponent.initTemperature();
        }
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
        compound.putBoolean("SpawnPointReleased", this.spawnPointReleased);

        // Save garage locking data
        compound.putBoolean("IsLockedInGarage", this.isLockedInGarage);
        if (this.garagePosition != null) {
            compound.putInt("GarageX", this.garagePosition.getX());
            compound.putInt("GarageY", this.garagePosition.getY());
            compound.putInt("GarageZ", this.garagePosition.getZ());
        }

        // Save paint color
        compound.putInt("PaintColor", getPaintColor());

        // Save towing yard flag
        compound.putBoolean("IsOnTowingYard", isOnTowingYard());

        // Save container installation tracking
        compound.putBoolean("HasHadItemContainer", this.hasHadItemContainer);
        compound.putBoolean("HasHadFluidContainer", this.hasHadFluidContainer);

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

    public Container getInternalInventory() {
        return inventoryComponent.getInternalInventory();
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

    public void centerVehicle() {
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

    public void openVehicleGUI(Player player) {
        inventoryComponent.openVehicleGUI(player);
    }

    public void openVehicleGUI(net.minecraft.server.level.ServerPlayer player) {
        inventoryComponent.openVehicleGUI(player);
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

    public void setLicensePlate(String plate) {
        securityComponent.setLicensePlate(plate);
    }

    public boolean isLocked() {
        return securityComponent.isLocked();
    }

    public void setLocked(boolean locked, boolean playSound) {
        securityComponent.setLocked(locked, playSound);
    }

    // Paint color system (0=white, 1=black, 2=red, 3=blue, 4=yellow)
    public int getPaintColor() {
        return this.entityData.get(PAINT_COLOR);
    }

    public void setPaintColor(int color) {
        if (color >= 0 && color <= 4) {
            this.entityData.set(PAINT_COLOR, color);
        }
    }

    // Towing yard system - flag to disable fuel consumption when parked at towing yard
    public boolean isOnTowingYard() {
        return this.entityData.get(IS_ON_TOWING_YARD);
    }

    public void setIsOnTowingYard(boolean isOnTowingYard) {
        this.entityData.set(IS_ON_TOWING_YARD, isOnTowingYard);
    }

    public String getPaintColorName() {
        return switch (getPaintColor()) {
            case 1 -> "black";
            case 2 -> "red";
            case 3 -> "blue";
            case 4 -> "yellow";
            default -> "white";
        };
    }

    // Utility methods for component access to protected fields
    public net.minecraft.util.RandomSource getRandom() {
        return this.random;
    }
}
