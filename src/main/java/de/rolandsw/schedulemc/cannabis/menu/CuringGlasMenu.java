package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.blockentity.CuringGlasBlockEntity;
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

public class CuringGlasMenu extends AbstractContainerMenu {

    public final CuringGlasBlockEntity blockEntity;
    private final ContainerData data;

    private static final int DATA_CURING_DAYS = 0;
    private static final int DATA_STRAIN      = 1;
    private static final int DATA_QUALITY     = 2;
    private static final int DATA_WEIGHT      = 3;
    private static final int DATA_HAS_CONTENT = 4;
    private static final int DATA_SIZE        = 5;

    // Server-side constructor
    public CuringGlasMenu(int containerId, Inventory playerInventory, CuringGlasBlockEntity blockEntity) {
        super(CannabisMenuTypes.CURING_GLAS_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_CURING_DAYS -> blockEntity.getCuringDays();
                    case DATA_STRAIN      -> blockEntity.getStrain().ordinal();
                    case DATA_QUALITY     -> blockEntity.getBaseQuality().ordinal();
                    case DATA_WEIGHT      -> blockEntity.getWeight();
                    case DATA_HAS_CONTENT -> blockEntity.hasContent() ? 1 : 0;
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
    public CuringGlasMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CannabisMenuTypes.CURING_GLAS_MENU.get(), containerId);

        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof CuringGlasBlockEntity glas ? glas : null;

        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
    }

    // Getter
    public int getCuringDays()       { return this.data.get(DATA_CURING_DAYS); }
    public boolean hasContent()      { return this.data.get(DATA_HAS_CONTENT) == 1; }
    public int getWeight()           { return this.data.get(DATA_WEIGHT); }
    public float getCuringProgress() { return Math.min(1.0f, getCuringDays() / 28.0f); }

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

    public CannabisQuality getExpectedQuality() {
        return CannabisQuality.fromCuringTime(getCuringDays(), getBaseQuality());
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
        private final CuringGlasBlockEntity blockEntity;

        public Provider(CuringGlasBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.curing_glas.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new CuringGlasMenu(containerId, playerInventory, blockEntity);
        }
    }
}
