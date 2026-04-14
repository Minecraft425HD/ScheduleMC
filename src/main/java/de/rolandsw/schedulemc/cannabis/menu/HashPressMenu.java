package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.blockentity.HashPressBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HashPressMenu extends AbstractContainerMenu {

    public final HashPressBlockEntity blockEntity;
    private final ContainerData data;

    private static final int DATA_TRIM_WEIGHT    = 0;
    private static final int DATA_STRAIN         = 1;
    private static final int DATA_PRESS_PROGRESS = 2;
    private static final int DATA_IS_PRESSING    = 3;
    private static final int DATA_HAS_OUTPUT     = 4;
    private static final int DATA_QUALITY        = 5;
    private static final int DATA_SIZE           = 6;

    // Server-side constructor
    public HashPressMenu(int containerId, Inventory playerInventory, HashPressBlockEntity blockEntity) {  // NOPMD
        super(CannabisMenuTypes.HASH_PRESS_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_TRIM_WEIGHT    -> blockEntity.getTrimWeight();
                    case DATA_STRAIN         -> blockEntity.getStrain().ordinal();
                    case DATA_PRESS_PROGRESS -> (int)(blockEntity.getPressProgress() * HashPressBlockEntity.PRESS_TICKS);
                    case DATA_IS_PRESSING    -> blockEntity.isPressing() ? 1 : 0;
                    case DATA_HAS_OUTPUT     -> blockEntity.hasOutput() ? 1 : 0;
                    case DATA_QUALITY        -> blockEntity.getTrimQuality().ordinal();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Read-only
            }

            @Override
            public int getCount() {
                return DATA_SIZE;
            }
        };

        addDataSlots(this.data);
    }

    // Client-side constructor
    public HashPressMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CannabisMenuTypes.HASH_PRESS_MENU.get(), containerId);

        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof HashPressBlockEntity presse ? presse : null;  // NOPMD

        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
    }

    public static final int BUTTON_START = 0;

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_START && blockEntity != null && !blockEntity.isRemoved()) {
            return blockEntity.startPressing();
        }
        return false;
    }

    // Getter
    public int getTrimWeight()       { return this.data.get(DATA_TRIM_WEIGHT); }
    public boolean isPressing()      { return this.data.get(DATA_IS_PRESSING) == 1; }
    public boolean hasOutput()       { return this.data.get(DATA_HAS_OUTPUT) == 1; }
    public int getPressProgress()    { return this.data.get(DATA_PRESS_PROGRESS); }
    public float getPressProgressF() { return (float)getPressProgress() / HashPressBlockEntity.PRESS_TICKS; }
    public boolean canStart()        { return getTrimWeight() >= HashPressBlockEntity.MIN_TRIM_WEIGHT && !isPressing() && !hasOutput(); }
    public int getExpectedHashWeight() { return (int)(getTrimWeight() * HashPressBlockEntity.CONVERSION_RATE); }

    public CannabisStrain getStrain() {
        int ordinal = this.data.get(DATA_STRAIN);
        CannabisStrain[] values = CannabisStrain.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : CannabisStrain.HYBRID;
    }

    public CannabisQuality getBaseQuality() {
        int ordinal = this.data.get(DATA_QUALITY);
        CannabisQuality[] values = CannabisQuality.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : CannabisQuality.GUT;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() &&
                player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                        blockEntity.getBlockPos().getY() + 0.5,
                        blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    public static class Provider implements MenuProvider {
        private final HashPressBlockEntity blockEntity;

        public Provider(HashPressBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.hash_presse.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new HashPressMenu(containerId, playerInventory, blockEntity);
        }
    }
}
