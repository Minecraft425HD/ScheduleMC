package de.rolandsw.schedulemc.car.blocks.tileentity;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.blocks.BlockGasStation;
import de.rolandsw.schedulemc.car.blocks.BlockGasStationTop;
import de.rolandsw.schedulemc.car.blocks.BlockOrientableHorizontal;
import de.rolandsw.schedulemc.car.blocks.ModBlocks;
import de.rolandsw.schedulemc.car.fluids.ModFluids;
import de.rolandsw.schedulemc.car.fuel.FuelBillManager;
import de.rolandsw.schedulemc.car.net.MessageStartFuel;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import de.rolandsw.schedulemc.car.sounds.SoundLoopTileentity;
import de.rolandsw.schedulemc.car.sounds.SoundLoopTileentity.ISoundLoopable;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.maxhenkel.corelib.CachedValue;
import de.maxhenkel.corelib.blockentity.ITickableBlockEntity;
import de.maxhenkel.corelib.item.ItemUtils;
import net.minecraft.ChatFormatting;
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

    // Gas Station ID for billing
    private UUID gasStationId;

    // Shop Plot ID (wenn die Gasstation in einem Shop-Plot ist)
    private String shopPlotId;

    // Tracking for billing
    private double totalCostThisSession;
    private int totalFueledThisSession;
    private UUID currentFuelingPlayer;

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

        // Registriere Zapfsäule
        if (this.gasStationId == null) {
            this.gasStationId = GasStationRegistry.registerGasStation(pos);
        }
    }

    public UUID getGasStationId() {
        return gasStationId;
    }

    public String getShopPlotId() {
        return shopPlotId;
    }

    public void setShopPlotId(String shopPlotId) {
        this.shopPlotId = shopPlotId;
        setChanged();
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
                // Send final bill if there was a fueling session
                if (currentFuelingPlayer != null && totalFueledThisSession > 0) {
                    sendFuelBillToPlayer(currentFuelingPlayer, totalFueledThisSession, totalCostThisSession);
                }

                fuelCounter = 0;
                isFueling = false;
                totalCostThisSession = 0;
                totalFueledThisSession = 0;
                currentFuelingPlayer = null;
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
                // If no trade amount set, fuel on credit (bill payment system)
                if (pricePerUnit > 0) {
                    // Find the player who owns the fueling entity
                    Player player = findPlayerForFueling();
                    if (player != null) {
                        UUID playerUUID = player.getUUID();

                        // Initialize session if new player
                        if (currentFuelingPlayer == null) {
                            currentFuelingPlayer = playerUUID;
                            totalCostThisSession = 0;
                            totalFueledThisSession = 0;

                            // Send welcome message with price
                            String timeOfDay = getCurrentPrice() == Main.SERVER_CONFIG.gasStationMorningPricePer10mb.get() ? "Tag" : "Nacht";
                            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
                            player.sendSystemMessage(Component.literal("⛽ ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal("TANKSTELLE").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
                            player.sendSystemMessage(Component.literal("Aktueller Preis (").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(timeOfDay).withStyle(ChatFormatting.AQUA))
                                .append(Component.literal("): ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(String.format("%.2f€", (double)pricePerUnit)).withStyle(ChatFormatting.GREEN))
                                .append(Component.literal(" pro 10 mB").withStyle(ChatFormatting.GRAY)));
                            player.sendSystemMessage(Component.literal("💳 Tanken auf Rechnung aktiviert").withStyle(ChatFormatting.AQUA));
                            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
                        }

                        // Calculate cost for 10 mB and add to bill (no immediate payment)
                        double cost = pricePerUnit;
                        freeAmountLeft = 10; // Allow 10 mB per pricing cycle
                        totalCostThisSession += cost;
                        setChanged();
                    } else {
                        // No player found, stop fueling
                        isFueling = false;
                        synchronize();
                        return;
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
            totalFueledThisSession += result.getAmount();
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
     * Sends a fuel bill to the player and creates a fuel bill record
     */
    private void sendFuelBillToPlayer(UUID playerUUID, int totalFueled, double totalCost) {
        // Erstelle Fuel Bill für spätere Bezahlung am Tankstellen-NPC
        FuelBillManager.createBill(playerUUID, gasStationId, totalFueled, totalCost);
        FuelBillManager.saveIfNeeded();

        Player player = level.getPlayerByUUID(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("📄 ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("TANKRECHNUNG").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.literal("Zapfsäule: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(GasStationRegistry.getDisplayName(gasStationId)).withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("Getankt: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(totalFueled + " mB").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" Bio-Diesel").withStyle(ChatFormatting.GREEN)));
            player.sendSystemMessage(Component.literal("Kosten: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", totalCost)).withStyle(ChatFormatting.GOLD)));
            player.sendSystemMessage(Component.literal("Offener Betrag: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", FuelBillManager.getTotalUnpaidAmount(playerUUID))).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.literal("Bitte bezahlen Sie am Tankstellen-NPC!").withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
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
     * Tries to find the player for the entity being fueled
     */
    private Player findPlayerForFueling() {
        // Search for entities in the detection box
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, getDetectionBox());

        for (Entity entity : entities) {
            if (entity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
                // First try to get the controlling passenger
                if (entity.getControllingPassenger() instanceof Player) {
                    return (Player) entity.getControllingPassenger();
                }

                // Then try all passengers
                for (Entity passenger : entity.getPassengers()) {
                    if (passenger instanceof Player) {
                        return (Player) passenger;
                    }
                }
            }
        }

        return null;
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

        if (gasStationId != null) {
            compound.putUUID("gas_station_id", gasStationId);
        }

        if (shopPlotId != null && !shopPlotId.isEmpty()) {
            compound.putString("shop_plot_id", shopPlotId);
        }
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

        if (compound.contains("gas_station_id")) {
            gasStationId = compound.getUUID("gas_station_id");
        } else {
            // Fallback: Registriere falls noch nicht vorhanden
            gasStationId = GasStationRegistry.registerGasStation(worldPosition);
        }

        if (compound.contains("shop_plot_id")) {
            shopPlotId = compound.getString("shop_plot_id");
        }

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
