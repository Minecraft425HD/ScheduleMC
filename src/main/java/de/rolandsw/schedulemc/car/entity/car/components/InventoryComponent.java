package de.rolandsw.schedulemc.car.entity.car.components;

import de.maxhenkel.corelib.item.ItemUtils;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.gui.ContainerCar;
import de.rolandsw.schedulemc.car.gui.ContainerCarInventory;
import de.rolandsw.schedulemc.car.items.ItemCanister;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages all inventories: internal, external (cargo), parts, and fluid storage
 */
public class InventoryComponent extends CarComponent {

    protected Container internalInventory;
    protected Container externalInventory;
    protected Container partInventory;
    protected FluidStack fluidInventory;

    public InventoryComponent(EntityGenericCar car) {
        super(car);
        internalInventory = new SimpleContainer(27);
        externalInventory = new SimpleContainer(0);
        partInventory = new SimpleContainer(15);
        fluidInventory = FluidStack.EMPTY;
    }

    public Container getInternalInventory() {
        return internalInventory;
    }

    public Container getExternalInventory() {
        return externalInventory;
    }

    public Container getPartInventory() {
        return partInventory;
    }

    public void setExternalInventorySize(int size) {
        if (externalInventory.getContainerSize() != size) {
            externalInventory = new SimpleContainer(size);
        }
    }

    @Override
    public boolean onInteract(Player player, InteractionHand hand) {
        SecurityComponent security = car.getSecurityComponent();
        if (security != null && !security.canPlayerAccessInventoryExternal(player)) {
            return false;
        }

        if (!player.isShiftKeyDown()) {
            return false;
        }

        // Canister
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof ItemCanister) {
                boolean success = ItemCanister.fillCanister(stack, car);

                if (success) {
                    ModSounds.playSound(SoundEvents.BREWING_STAND_BREW, car.level(), car.blockPosition(), null, SoundSource.BLOCKS);
                }
                return true;
            }
            if (car.getFluidInventorySize() > 0) {
                IFluidHandler handler = FluidUtil.getFluidHandler(stack).orElse(null);
                if (handler != null) {
                    FluidStack fluidStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);

                    if (!fluidStack.isEmpty()) {
                        if (handleEmpty(stack, getInventoryFluidHandler(), player, hand)) {
                            return true;
                        }
                    }

                    if (handleFill(stack, getInventoryFluidHandler(), player, hand)) {
                        return true;
                    }
                }
            }
        }

        // Inv
        if (!car.level().isClientSide) {
            if (externalInventory.getContainerSize() <= 0) {
                openCarGUI(player);
            } else {
                NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return car.getDisplayName();
                    }

                    @Nullable
                    @Override
                    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                        return new ContainerCarInventory(i, car, playerInventory);
                    }
                }, packetBuffer -> packetBuffer.writeUUID(car.getUUID()));
            }
        }

        return true;
    }

    public static boolean handleEmpty(ItemStack stack, IFluidHandler handler, Player playerIn, InteractionHand hand) {
        IItemHandler inv = new InvWrapper(playerIn.getInventory());

        FluidActionResult res = FluidUtil.tryEmptyContainerAndStow(stack, handler, inv, Integer.MAX_VALUE, playerIn, true);

        if (res.isSuccess()) {
            playerIn.setItemInHand(hand, res.result);
            return true;
        }

        return false;
    }

    public static boolean handleFill(ItemStack stack, IFluidHandler handler, Player playerIn, InteractionHand hand) {
        IItemHandler inv = new InvWrapper(playerIn.getInventory());

        FluidActionResult result = FluidUtil.tryFillContainerAndStow(stack, handler, inv, Integer.MAX_VALUE,
                playerIn, true);

        if (result.isSuccess()) {
            playerIn.setItemInHand(hand, result.result);
            return true;
        }

        return false;
    }

    public void openCarGUI(Player player) {
        if (!car.level().isClientSide && player instanceof ServerPlayer) {
            NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return car.getDisplayName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                    return new ContainerCar(i, car, playerInventory);
                }
            }, packetBuffer -> packetBuffer.writeUUID(car.getUUID()));
        }
    }

    public void dropInventoryContents() {
        Containers.dropContents(car.level(), car, internalInventory);
        Containers.dropContents(car.level(), car, externalInventory);
    }

    public void dropPartInventory() {
        Containers.dropContents(car.level(), car, partInventory);
    }

    public IFluidHandler getInventoryFluidHandler() {
        return inventoryFluidHandler;
    }

    private IFluidHandler inventoryFluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return fluidInventory;
        }

        @Override
        public int getTankCapacity(int tank) {
            return car.getFluidInventorySize();
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (fluidInventory.isEmpty()) {
                int amount = Math.min(resource.getAmount(), car.getFluidInventorySize());

                if (action.execute()) {
                    fluidInventory = new FluidStack(resource.getFluid(), amount);
                }

                return amount;
            } else if (resource.getFluid().equals(fluidInventory.getFluid())) {
                int amount = Math.min(resource.getAmount(), car.getFluidInventorySize() - fluidInventory.getAmount());

                if (action.execute()) {
                    fluidInventory.setAmount(fluidInventory.getAmount() + amount);
                }

                return amount;
            }
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (fluidInventory.isEmpty()) {
                return FluidStack.EMPTY;
            }

            if (fluidInventory.getFluid().equals(resource.getFluid())) {
                int amount = Math.min(resource.getAmount(), fluidInventory.getAmount());

                Fluid f = fluidInventory.getFluid();

                if (action.execute()) {
                    fluidInventory.setAmount(fluidInventory.getAmount() - amount);
                    if (fluidInventory.getAmount() <= 0) {
                        fluidInventory = FluidStack.EMPTY;
                    }
                }

                return new FluidStack(f, amount);
            }

            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (fluidInventory.isEmpty()) {
                return FluidStack.EMPTY;
            }

            int amount = Math.min(maxDrain, fluidInventory.getAmount());

            Fluid f = fluidInventory.getFluid();

            if (action.execute()) {
                fluidInventory.setAmount(fluidInventory.getAmount() - amount);
                if (fluidInventory.getAmount() <= 0) {
                    fluidInventory = FluidStack.EMPTY;
                }
            }

            return new FluidStack(f, amount);
        }
    };

    @Override
    public void saveAdditionalData(CompoundTag compound) {
        ItemUtils.saveInventory(compound, "int_inventory", internalInventory);
        compound.putInt("external_inventory_size", externalInventory.getContainerSize());
        ItemUtils.saveInventory(compound, "external_inventory", externalInventory);
        ItemUtils.saveInventory(compound, "parts", partInventory);

        if (!fluidInventory.isEmpty()) {
            compound.put("fluid_inventory", fluidInventory.writeToNBT(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalData(CompoundTag compound) {
        ItemUtils.readInventory(compound, "int_inventory", internalInventory);

        this.externalInventory = new SimpleContainer(compound.getInt("external_inventory_size"));
        ItemUtils.readInventory(compound, "external_inventory", externalInventory);

        ItemUtils.readInventory(compound, "parts", partInventory);

        if (compound.contains("fluid_inventory")) {
            fluidInventory = FluidStack.loadFluidStackFromNBT(compound.getCompound("fluid_inventory"));
        }
    }
}
