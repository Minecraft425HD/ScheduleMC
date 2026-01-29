package de.rolandsw.schedulemc.vehicle.blocks.tileentity;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.BlockFuelStation;
import de.rolandsw.schedulemc.vehicle.blocks.BlockFuelStationTop;
import de.rolandsw.schedulemc.vehicle.blocks.BlockOrientableHorizontal;
import de.rolandsw.schedulemc.vehicle.blocks.ModBlocks;
import de.rolandsw.schedulemc.vehicle.fluids.ModFluids;
import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
import de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry;
import de.rolandsw.schedulemc.vehicle.net.MessageStartFuel;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import de.rolandsw.schedulemc.vehicle.sounds.SoundLoopTileentity;
import de.rolandsw.schedulemc.vehicle.sounds.SoundLoopTileentity.ISoundLoopable;
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

public class TileEntityFuelStation extends TileEntityBase implements ITickableBlockEntity, IFluidHandler, ISoundLoopable {

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

    // Fuel Station ID for billing
    private UUID fuelStationId;

    // Shop Plot ID (wenn die Fuelstation in einem Shop-Plot ist)
    private String shopPlotId;

    // Tracking for billing
    private double totalCostThisSession;
    private int totalFueledThisSession;
    private UUID currentFuelingPlayer;

    // Client-synced price data (updated from config on server, synced via NBT)
    private int morningPrice;
    private int eveningPrice;

    @Nullable
    private IFluidHandler fluidHandlerInFront;

    // Entity-Referenz für GUI (Vehicle-Name, Odometer etc.)
    @Nullable
    private Entity entityInFront;

    // OPTIMIERUNG: Cache für Entity-Scan (reduziert von 20x/sek auf 5x/sek)
    private static final int ENTITY_SCAN_INTERVAL = 4; // Alle 4 Ticks (200ms)
    private int entityScanCounter = 0;
    private Entity cachedEntityInFront = null;
    private IFluidHandler cachedFluidHandler = null;

    public TileEntityFuelStation(BlockPos pos, BlockState state) {
        super(Main.FUEL_STATION_TILE_ENTITY_TYPE.get(), pos, state);
        this.transferRate = ModConfigHandler.VEHICLE_SERVER.fuelStationTransferRate.get();
        this.fuelCounter = 0;
        this.inventory = new SimpleContainer(27);
        this.trading = new SimpleContainer(2);
        this.owner = new UUID(0L, 0L);
        this.storage = FluidStack.EMPTY;
        this.tradeAmount = 1000;

        // Registriere Zapfsäule
        if (this.fuelStationId == null) {
            this.fuelStationId = FuelStationRegistry.registerFuelStation(pos);
        }
    }

    public UUID getFuelStationId() {
        return fuelStationId;
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
                case 3:
                    return morningPrice;
                case 4:
                    return eveningPrice;
                case 5:
                    return (int) totalCostThisSession;
                case 6:
                    return isFueling ? 1 : 0;
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
                case 3:
                    morningPrice = value;
                    break;
                case 4:
                    eveningPrice = value;
                    break;
                case 5:
                    totalCostThisSession = value;
                    break;
                case 6:
                    isFueling = value != 0;
                    break;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    @Override
    public Component getTranslatedName() {
        return Component.translatable("block.vehicle.fuel_station");
    }

    private void fixTop() {
        BlockState top = level.getBlockState(worldPosition.above());
        BlockState bottom = getBlockState();
        Direction facing = bottom.getValue(BlockOrientableHorizontal.FACING);
        if (top.getBlock().equals(ModBlocks.FUEL_STATION_TOP.get())) {
            if (!top.getValue(BlockFuelStationTop.FACING).equals(facing)) {
                level.setBlockAndUpdate(worldPosition.above(), ModBlocks.FUEL_STATION_TOP.get().defaultBlockState().setValue(BlockFuelStationTop.FACING, facing));
            }
        } else if (level.isEmptyBlock(worldPosition.above())) {
            level.setBlockAndUpdate(worldPosition.above(), ModBlocks.FUEL_STATION_TOP.get().defaultBlockState().setValue(BlockFuelStationTop.FACING, facing));
        }

    }

    @Override
    public void tick() {
        // Entity-Scan läuft auf BEIDEN Seiten (Client braucht es für die GUI-Anzeige)
        entityScanCounter++;
        if (entityScanCounter >= ENTITY_SCAN_INTERVAL || cachedEntityInFront == null) {
            entityScanCounter = 0;
            cachedEntityInFront = searchEntityWithFluidHandlerInFront();
            cachedFluidHandler = cachedEntityInFront != null
                    ? cachedEntityInFront.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null)
                    : null;
        }
        entityInFront = cachedEntityInFront;
        fluidHandlerInFront = cachedFluidHandler;

        // Alles ab hier nur serverseitig - keine Tank-Logik auf dem Client!
        if (level.isClientSide) {
            return;
        }

        if (level.getGameTime() % 100 == 0) {
            fixTop();
            // Sync price config to client via NBT
            int newMorning = ModConfigHandler.VEHICLE_SERVER.fuelStationMorningPricePer10mb.get();
            int newEvening = ModConfigHandler.VEHICLE_SERVER.fuelStationEveningPricePer10mb.get();
            if (newMorning != morningPrice || newEvening != eveningPrice) {
                morningPrice = newMorning;
                eveningPrice = newEvening;
                setChanged();
                synchronize();
            }
        }

        if (fluidHandlerInFront == null) {
            if (fuelCounter > 0 || isFueling) {
                finalizeFuelingSession();
            }
            return;
        }

        if (!isFueling) {
            return;
        }

        // Gas station now has unlimited Bio-Diesel, no storage check needed

        FluidStack s = FluidUtil.tryFluidTransfer(fluidHandlerInFront, this, transferRate, false);
        int amountVehicleCanTake = 0;
        if (!s.isEmpty()) {
            amountVehicleCanTake = s.getAmount();
        }

        if (amountVehicleCanTake <= 0) {
            return;
        }

        if (freeAmountLeft <= 0) {
            // Calculate price based on time of day
            int pricePerUnit = getCurrentPrice();

            // DEBUG: Log price
            Main.LOGGER.debug("[FuelStation] Current price: {}€ per 10mB (tradeAmount: {})", pricePerUnit, tradeAmount);

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
                            String timeOfDay = getCurrentPrice() == ModConfigHandler.VEHICLE_SERVER.fuelStationMorningPricePer10mb.get() ? "Tag" : "Nacht";
                            String stationName = FuelStationRegistry.getDisplayName(fuelStationId);
                            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
                            player.sendSystemMessage(Component.literal("⛽ ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.translatable("gui.fuel.station_title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
                            player.sendSystemMessage(Component.translatable("message.fuel.pump_label").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(stationName).withStyle(ChatFormatting.AQUA)));
                            player.sendSystemMessage(Component.literal("ID: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(fuelStationId.toString().substring(0, 8) + "...").withStyle(ChatFormatting.DARK_GRAY)));
                            player.sendSystemMessage(Component.translatable("message.common.current_price_prefix").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(timeOfDay).withStyle(ChatFormatting.AQUA))
                                .append(Component.literal("): ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(String.format("%.2f€", (double)pricePerUnit)).withStyle(ChatFormatting.GREEN))
                                .append(Component.translatable("vehicle.fuel_station.per_10mb").withStyle(ChatFormatting.GRAY)));
                            player.sendSystemMessage(Component.translatable("message.fuel.bill_activated").withStyle(ChatFormatting.AQUA));
                            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));

                            // DEBUG LOGGING
                            Main.LOGGER.debug("[FuelStation] Player {} started fueling at station {} ({})",
                                player.getName().getString(), stationName, fuelStationId);
                            Main.LOGGER.debug("[FuelStation] Price: {}€ per 10mB ({})", pricePerUnit, timeOfDay);
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
            synchronize(2);

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
        // DEBUG LOGGING
        String stationName = FuelStationRegistry.getDisplayName(fuelStationId);
        Main.LOGGER.debug("[FuelStation] Creating bill for player {} at station {} ({})",
            playerUUID, stationName, fuelStationId);
        Main.LOGGER.debug("[FuelStation] Bill details: {} mB fueled, total cost: {}€", totalFueled, totalCost);

        // Erstelle Fuel Bill für spätere Bezahlung am Tankstellen-NPC
        FuelBillManager.createBill(playerUUID, fuelStationId, totalFueled, totalCost);
        FuelBillManager.saveIfNeeded();

        Main.LOGGER.debug("[FuelStation] Bill created and saved successfully");

        Player player = level.getPlayerByUUID(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("📄 ").withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("gui.fuel.bill_title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.translatable("message.fuel.pump_label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(FuelStationRegistry.getDisplayName(fuelStationId)).withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.translatable("message.fuel.refueled_label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(totalFueled + " mB").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" Bio-Diesel").withStyle(ChatFormatting.GREEN)));
            player.sendSystemMessage(Component.translatable("vehicle.fuel_station.costs_label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", totalCost)).withStyle(ChatFormatting.GOLD)));
            player.sendSystemMessage(Component.translatable("message.common.outstanding_amount").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", FuelBillManager.getTotalUnpaidAmount(playerUUID))).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.translatable("message.fuel.pay_at_npc").withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
        }
    }

    /**
     * Finalisiert die Tanksitzung und erstellt die Rechnung
     * Wird aufgerufen wenn:
     * - Der STOP-Button gedrückt wird
     * - Das Fahrzeug wegfährt (fluidHandlerInFront == null)
     */
    public void finalizeFuelingSession() {
        // Send final bill if there was a fueling session
        if (currentFuelingPlayer != null && totalFueledThisSession > 0) {
            sendFuelBillToPlayer(currentFuelingPlayer, totalFueledThisSession, totalCostThisSession);
        }

        fuelCounter = 0;
        isFueling = false;
        totalCostThisSession = 0;
        totalFueledThisSession = 0;
        currentFuelingPlayer = null;
        cachedEntityInFront = null; // Cache invalidieren für nächstes Fahrzeug
        cachedFluidHandler = null;
        synchronize();
        setChanged();
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
            return ModConfigHandler.VEHICLE_SERVER.fuelStationMorningPricePer10mb.get();
        } else {
            // Evening/Night time
            return ModConfigHandler.VEHICLE_SERVER.fuelStationEveningPricePer10mb.get();
        }
    }

    /**
     * Tries to find the player for the entity being fueled
     */
    private Player findPlayerForFueling() {
        // Search for entities in the detection box
        AABB box = getDetectionBox();
        if (box == null) {
            return null;
        }
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, box);

        Entity vehicleWithFluidHandler = null;

        for (Entity entity : entities) {
            if (entity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
                vehicleWithFluidHandler = entity;
                Main.LOGGER.debug("[FuelStation] Found vehicle with fluid handler: {}", entity.getClass().getSimpleName());

                // First try to get the controlling passenger
                if (entity.getControllingPassenger() instanceof Player) {
                    Player player = (Player) entity.getControllingPassenger();
                    Main.LOGGER.debug("[FuelStation] Found controlling passenger: {}", player.getName().getString());
                    return player;
                }

                // Then try all passengers
                for (Entity passenger : entity.getPassengers()) {
                    if (passenger instanceof Player) {
                        Player player = (Player) passenger;
                        Main.LOGGER.debug("[FuelStation] Found passenger: {}", player.getName().getString());
                        return player;
                    }
                }
            }
        }

        // If no player in vehicle, search for nearby players who might have just exited
        if (vehicleWithFluidHandler != null) {
            List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class,
                box.inflate(3.0D), // 3 block radius around detection box
                p -> !p.isSpectator());

            if (!nearbyPlayers.isEmpty()) {
                Player nearestPlayer = nearbyPlayers.get(0);
                Main.LOGGER.debug("[FuelStation] Found nearby player: {}", nearestPlayer.getName().getString());
                return nearestPlayer;
            }
        }

        Main.LOGGER.debug("[FuelStation] No player found for fueling!");
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
        return ModConfigHandler.VEHICLE_SERVER.fuelStationValidFuelList.stream().anyMatch(fluidTag -> fluidTag.contains(f));
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

        if (fuelStationId != null) {
            compound.putUUID("fuel_station_id", fuelStationId);
        }

        if (shopPlotId != null && !shopPlotId.isEmpty()) {
            compound.putString("shop_plot_id", shopPlotId);
        }

        compound.putBoolean("is_fueling", isFueling);
        compound.putInt("morning_price", morningPrice);
        compound.putInt("evening_price", eveningPrice);
        compound.putDouble("total_cost_session", totalCostThisSession);
        compound.putInt("total_fueled_session", totalFueledThisSession);
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

        if (compound.contains("fuel_station_id")) {
            fuelStationId = compound.getUUID("fuel_station_id");
        } else {
            // Fallback: Registriere falls noch nicht vorhanden
            fuelStationId = FuelStationRegistry.registerFuelStation(worldPosition);
        }

        if (compound.contains("shop_plot_id")) {
            shopPlotId = compound.getString("shop_plot_id");
        }

        if (compound.contains("owner")) {
            owner = compound.getUUID("owner");
        } else {
            owner = new UUID(0L, 0L);
        }

        isFueling = compound.getBoolean("is_fueling");
        morningPrice = compound.getInt("morning_price");
        eveningPrice = compound.getInt("evening_price");
        totalCostThisSession = compound.getDouble("total_cost_session");
        totalFueledThisSession = compound.getInt("total_fueled_session");

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
            return Component.translatable("fuel_station.no_vehicle").getString();
        } else if (fuelCounter <= 0) {
            return Component.translatable("fuel_station.ready").getString();
        } else {
            return Component.translatable("fuel_station.fuel_amount", fuelCounter).getString();
        }
    }

    private CachedValue<Vec3> center = new CachedValue<>(() -> new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.5D, worldPosition.getZ() + 0.5D));

    @Nullable
    private Entity searchEntityWithFluidHandlerInFront() {
        if (level == null) {
            return null;
        }
        AABB box = getDetectionBox();
        if (box == null) {
            return null;
        }
        return level.getEntitiesOfClass(Entity.class, box)
                .stream()
                .sorted(Comparator.comparingDouble(o -> o.distanceToSqr(center.get())))
                .filter(entity -> entity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent())
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public IFluidHandler getFluidHandlerInFront() {
        return fluidHandlerInFront;
    }

    @Nullable
    public Entity getEntityInFront() {
        return entityInFront;
    }

    private CachedValue<AABB> detectionBox = new CachedValue<>(this::createDetectionBox);

    private AABB createDetectionBox() {
        BlockState ownState = level.getBlockState(worldPosition);

        if (!ownState.getBlock().equals(ModBlocks.FUEL_STATION.get())) {
            return null;
        }
        Direction facing = ownState.getValue(BlockFuelStation.FACING);
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
        return getBlockState().getValue(BlockFuelStation.FACING);
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
        ModSounds.playSoundLoop(new SoundLoopTileentity(ModSounds.FUEL_STATION.get(), SoundSource.BLOCKS, this), level);
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

    public int getMorningPrice() {
        return morningPrice;
    }

    public int getEveningPrice() {
        return eveningPrice;
    }

    public int getTotalCostThisSession() {
        return (int) totalCostThisSession;
    }

    public int getTotalFueledThisSession() {
        return totalFueledThisSession;
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
        // Only provide Bio-Diesel, capped at transfer rate to prevent abuse
        if (resource.getFluid().equals(ModFluids.BIO_DIESEL.get())) {
            int amount = Math.min(resource.getAmount(), transferRate);

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
        // Cap at transfer rate to prevent unlimited extraction by external systems
        int amount = Math.min(maxDrain, transferRate);

        if (action.execute()) {
            setChanged();
        }

        return new FluidStack(ModFluids.BIO_DIESEL.get(), amount);
    }
}
