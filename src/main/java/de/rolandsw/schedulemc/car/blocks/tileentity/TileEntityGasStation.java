package de.rolandsw.schedulemc.car.blocks.tileentity;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.blocks.BlockGasStation;
import de.rolandsw.schedulemc.car.blocks.BlockGasStationTop;
import de.rolandsw.schedulemc.car.blocks.BlockOrientableHorizontal;
import de.rolandsw.schedulemc.car.blocks.ModBlocks;
import de.rolandsw.schedulemc.car.fluids.ModFluids;
import de.rolandsw.schedulemc.car.net.MessageStartFuel;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import de.rolandsw.schedulemc.car.sounds.SoundLoopTileentity;
import de.rolandsw.schedulemc.car.sounds.SoundLoopTileentity.ISoundLoopable;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.maxhenkel.corelib.CachedValue;
import de.maxhenkel.corelib.blockentity.ITickableBlockEntity;
import de.maxhenkel.corelib.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityGasStation extends TileEntityBase implements ITickableBlockEntity, IFluidHandler, ISoundLoopable {

    private FluidStack storage;

    public int maxStorageAmount = 16000;

    private final int transferRate;

    private int fuelCounter;
    private boolean isFueling;
    private boolean wasFueling;

    private SimpleContainer inventory;
    private SimpleContainer trading;
    private int tradeAmount;

    private int freeAmountLeft;

    private UUID owner;

    @Nullable
    private IFluidHandler fluidHandlerInFront;

    public TileEntityGasStation(BlockPos pos, BlockState state) {
        super(Main.GAS_STATION_TILE_ENTITY_TYPE.get(), pos, state);
        this.transferRate = Main.SERVER_CONFIG.gasStationTransferRate.get();
        this.fuelCounter = 0;
        this.inventory = new SimpleContainer(27);
        this.trading = new SimpleContainer(2);
        this.owner = new UUID(0L, 0L);
        this.storage = FluidStack.EMPTY;
        this.tradeAmount = 1000;
    }

    public final ContainerData FIELDS = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return fuelCounter;
                case 1:
                    if (!storage.isEmpty()) {
                        return storage.getAmount();
                    }
                    return 0;
                case 2:
                    return tradeAmount;
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    fuelCounter = value;
                    break;
                case 1:
                    if (!storage.isEmpty()) {
                        storage.setAmount(value);
                    }
                    break;
                case 2:
                    tradeAmount = value;
                    setChanged();
                    break;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    @Override
    public Component getTranslatedName() {
        return Component.translatable("block.car.fuel_station");
    }

    private void fixTop() {
        BlockState top = level.getBlockState(worldPosition.above());
        BlockState bottom = getBlockState();
        Direction facing = bottom.getValue(BlockOrientableHorizontal.FACING);
        if (top.getBlock().equals(ModBlocks.GAS_STATION_TOP.get())) {
            if (!top.getValue(BlockGasStationTop.FACING).equals(facing)) {
                level.setBlockAndUpdate(worldPosition.above(), ModBlocks.GAS_STATION_TOP.get().defaultBlockState().setValue(BlockGasStationTop.FACING, facing));
            }
        } else if (level.isEmptyBlock(worldPosition.above())) {
            level.setBlockAndUpdate(worldPosition.above(), ModBlocks.GAS_STATION_TOP.get().defaultBlockState().setValue(BlockGasStationTop.FACING, facing));
        }

    }

    @Override
    public void tick() {
        if (level.getGameTime() % 100 == 0) {
            fixTop();
        }

        fluidHandlerInFront = searchFluidHandlerInFront();

        if (fluidHandlerInFront == null) {
            if (fuelCounter > 0 || isFueling) {
                fuelCounter = 0;
                isFueling = false;
                synchronize();
                setChanged();
            }
            return;
        }

        if (!isFueling) {
            return;
        }

        // Gas station now has unlimited Bio-Diesel, no storage check needed

        FluidStack s = FluidUtil.tryFluidTransfer(fluidHandlerInFront, this, transferRate, false);
        int amountCarCanTake = 0;
        if (!s.isEmpty()) {
            amountCarCanTake = s.getAmount();
        }

        if (amountCarCanTake <= 0) {
            return;
        }

        if (freeAmountLeft <= 0) {
            // Calculate price based on time of day
            int pricePerUnit = getCurrentPrice();

            if (tradeAmount <= 0) {
                // If no trade amount set, check wallet payment
                if (pricePerUnit > 0) {
                    // Find the player who owns the fueling entity
                    UUID playerUUID = findPlayerForFueling();
                    if (playerUUID != null) {
                        // Calculate cost for 10 mB
                        double cost = pricePerUnit;
                        if (WalletManager.removeMoney(playerUUID, cost)) {
                            freeAmountLeft = 10; // Allow 10 mB per payment
                            setChanged();
                        } else {
                            // Not enough money, stop fueling
                            isFueling = false;
                            synchronize();
                            return;
                        }
                    } else {
                        // No player found, allow free fueling
                        freeAmountLeft = transferRate;
                        setChanged();
                    }
                } else {
                    // Free fueling
                    freeAmountLeft = transferRate;
                    setChanged();
                }
            } else if (removeTradeItem()) {
                freeAmountLeft = tradeAmount;
                setChanged();
            } else {
                isFueling = false;
                synchronize();
                return;
            }
        }

        FluidStack result = FluidUtil.tryFluidTransfer(fluidHandlerInFront, this, Math.min(transferRate, freeAmountLeft), true);

        if (!result.isEmpty()) {
            fuelCounter += result.getAmount();
            freeAmountLeft -= result.getAmount();
            synchronize(100);

            setChanged();
            if (!wasFueling) {
                synchronize();
            }
            wasFueling = true;
        } else {
            if (wasFueling) {
                synchronize();
            }
            wasFueling = false;
        }
    }

    /**
     * Gets the current price per 10 mB based on time of day
     * Morning (0-12000 ticks / 6:00-18:00): higher price
     * Evening (12000-24000 ticks / 18:00-6:00): lower price
     */
    private int getCurrentPrice() {
        long dayTime = level.getDayTime() % 24000;

        if (dayTime >= 0 && dayTime < 12000) {
            // Morning/Day time
            return Main.SERVER_CONFIG.gasStationMorningPricePer10mb.get();
        } else {
            // Evening/Night time
            return Main.SERVER_CONFIG.gasStationEveningPricePer10mb.get();
        }
    }

    /**
     * Tries to find the player UUID for the entity being fueled
     */
    private UUID findPlayerForFueling() {
        // Search for entities in the detection box
        return level.getEntitiesOfClass(Entity.class, getDetectionBox())
                .stream()
                .filter(entity -> entity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent())
                .findFirst()
                .map(entity -> {
                    // Try to get the controlling player
                    if (entity.getControllingPassenger() instanceof Player) {
                        return ((Player) entity.getControllingPassenger()).getUUID();
                    }
                    return null;
                })
                .orElse(null);
    }

    /**
     * @return true if the item was successfully removed
     */
    public boolean removeTradeItem() {
        ItemStack tradeTemplate = trading.getItem(0);
        ItemStack tradingStack = trading.getItem(1);

        if (tradeTemplate.isEmpty()) {
            return true;
        }

        if (tradingStack.isEmpty()) {
            return false;
        }

        if (!tradeTemplate.getItem().equals(tradingStack.getItem())) {
            return false;
        }

        if (tradeTemplate.getDamageValue() != tradingStack.getDamageValue()) {
            return false;
        }

        if (tradingStack.getCount() < tradeTemplate.getCount()) {
            return false;
        }

        ItemStack addStack = tradingStack.copy();
        addStack.setCount(tradeTemplate.getCount());
        ItemStack add = inventory.addItem(addStack);
        if (add.getCount() > 0) {
            return false;
        }
        tradingStack.setCount(tradingStack.getCount() - tradeTemplate.getCount());
        trading.setItem(1, tradingStack);

        return true;
    }

    public Container getInventory() {
        return inventory;
    }

    public Container getTradingInventory() {
        return trading;
    }

    public boolean isValidFluid(Fluid f) {
        return Main.SERVER_CONFIG.gasStationValidFuelList.stream().anyMatch(fluidTag -> fluidTag.contains(f));
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public void setOwner(Player player) {
        this.owner = new UUID(player.getUUID().getMostSignificantBits(), player.getUUID().getLeastSignificantBits());
        setChanged();
    }

    /**
     * OPs are also owners
     */
    public boolean isOwner(Player player) {
        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer) player;

            boolean isOp = p.hasPermissions(p.server.getOperatorUserPermissionLevel());
            if (isOp) {
                return true;
            }
        }
        return player.getUUID().equals(owner);
    }

    public boolean hasTrade() {
        return !trading.getItem(0).isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        compound.putInt("counter", fuelCounter);

        if (!storage.isEmpty()) {
            CompoundTag comp = new CompoundTag();
            storage.writeToNBT(comp);
            compound.put("fluid", comp);
        }

        ItemUtils.saveInventory(compound, "inventory", inventory);

        ItemUtils.saveInventory(compound, "trading", trading);

        compound.putInt("trade_amount", tradeAmount);
        compound.putInt("free_amount", freeAmountLeft);

        compound.putUUID("owner", owner);
    }

    @Override
    public void load(CompoundTag compound) {
        fuelCounter = compound.getInt("counter");

        if (compound.contains("fluid")) {
            CompoundTag comp = compound.getCompound("fluid");
            storage = FluidStack.loadFluidStackFromNBT(comp);
        }

        ItemUtils.readInventory(compound, "inventory", inventory);
        ItemUtils.readInventory(compound, "trading", trading);

        tradeAmount = compound.getInt("trade_amount");
        freeAmountLeft = compound.getInt("free_amount");

        if (compound.contains("owner")) {
            owner = compound.getUUID("owner");
        } else {
            owner = new UUID(0L, 0L);
        }
        super.load(compound);
    }

    public boolean isFueling() {
        return isFueling;
    }

    public int getFuelCounter() {
        return this.fuelCounter;
    }

    public void setStorage(FluidStack storage) {
        this.storage = storage;
        setChanged();
        synchronize();
    }

    public FluidStack getStorage() {
        return storage;
    }

    public void setFuelCounter(int fuelCounter) {
        this.fuelCounter = fuelCounter;
        setChanged();
        synchronize();
    }

    public void setFueling(boolean isFueling) {
        if (fluidHandlerInFront == null) {
            return;
        }

        if (isFueling && !this.isFueling) {
            if (level.isClientSide) {
                playSound();
            }
        }
        this.isFueling = isFueling;
        synchronize();
    }

    public String getRenderText() {
        if (fluidHandlerInFront == null) {
            return Component.translatable("gas_station.no_vehicle").getString();
        } else if (fuelCounter <= 0) {
            return Component.translatable("gas_station.ready").getString();
        } else {
            return Component.translatable("gas_station.fuel_amount", fuelCounter).getString();
        }
    }

    private CachedValue<Vec3> center = new CachedValue<>(() -> new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.5D, worldPosition.getZ() + 0.5D));

    @Nullable
    private IFluidHandler searchFluidHandlerInFront() {
        if (level == null) {
            return null;
        }
        return level.getEntitiesOfClass(Entity.class, getDetectionBox())
                .stream()
                .sorted(Comparator.comparingDouble(o -> o.distanceToSqr(center.get())))
                .map(entity -> entity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public IFluidHandler getFluidHandlerInFront() {
        return fluidHandlerInFront;
    }

    private CachedValue<AABB> detectionBox = new CachedValue<>(this::createDetectionBox);

    private AABB createDetectionBox() {
        BlockState ownState = level.getBlockState(worldPosition);

        if (!ownState.getBlock().equals(ModBlocks.GAS_STATION.get())) {
            return null;
        }
        Direction facing = ownState.getValue(BlockGasStation.FACING);
        BlockPos start = worldPosition.relative(facing);
        return new AABB(start.getX(), start.getY(), start.getZ(), start.getX() + 1D, start.getY() + 2.5D, start.getZ() + 1D)
                .expandTowards(facing.getStepX(), 0D, facing.getStepZ())
                .inflate(facing.getStepX() == 0 ? 0.5D : 0D, 0D, facing.getStepZ() == 0 ? 0.5D : 0D);
    }

    public AABB getDetectionBox() {
        return detectionBox.get();
    }

    public boolean canEntityBeFueled() {
        if (fluidHandlerInFront == null) {
            return false;
        }
        FluidStack result = FluidUtil.tryFluidTransfer(fluidHandlerInFront, this, transferRate, false);
        return !result.isEmpty();
    }

    public Direction getDirection() {
        return getBlockState().getValue(BlockGasStation.FACING);
    }

    public void sendStartFuelPacket(boolean start) {
        if (level.isClientSide) {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageStartFuel(worldPosition, start));
        }
    }

    @Override
    public boolean shouldSoundBePlayed() {
        if (!isFueling) {
            return false;
        }

        return canEntityBeFueled();
    }

    @OnlyIn(Dist.CLIENT)
    public void playSound() {
        ModSounds.playSoundLoop(new SoundLoopTileentity(ModSounds.GAS_STATION.get(), SoundSource.BLOCKS, this), level);
    }

    @Override
    public void play() {

    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 2, 1));
    }

    public int getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(int tradeAmount) {
        this.tradeAmount = tradeAmount;
        synchronize();
    }

    public int getFuelAmount() {
        if (!storage.isEmpty()) {
            return storage.getAmount();
        }
        return 0;
    }

    @Override
    public ContainerData getFields() {
        return FIELDS;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return storage;
    }

    @Override
    public int getTankCapacity(int tank) {
        return maxStorageAmount;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return isValidFluid(stack.getFluid());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!isValidFluid(resource.getFluid())) {
            return 0;
        }

        if (storage.isEmpty()) {
            int amount = Math.min(resource.getAmount(), maxStorageAmount);

            if (action.execute()) {
                storage = new FluidStack(resource.getFluid(), amount);
                synchronize();
                setChanged();
            }

            return amount;
        } else if (resource.getFluid().equals(storage.getFluid())) {
            int amount = Math.min(resource.getAmount(), maxStorageAmount - storage.getAmount());

            if (action.execute()) {
                storage.setAmount(storage.getAmount() + amount);
                synchronize();
                setChanged();
            }

            return amount;
        }
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        // Always return unlimited Bio-Diesel
        if (resource.getFluid().equals(ModFluids.BIO_DIESEL.get())) {
            int amount = resource.getAmount();

            if (action.execute()) {
                setChanged();
            }

            return new FluidStack(ModFluids.BIO_DIESEL.get(), amount);
        }

        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        // Always return unlimited Bio-Diesel
        int amount = maxDrain;

        if (action.execute()) {
            setChanged();
        }

        return new FluidStack(ModFluids.BIO_DIESEL.get(), amount);
    }
}
