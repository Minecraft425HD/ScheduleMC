package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.blockentity.OelExtraktortBlockEntity;
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

public class OelExtraktortMenu extends AbstractContainerMenu {

    public final OelExtraktortBlockEntity blockEntity;
    private final ContainerData data;

    private static final int DATA_MATERIAL_WEIGHT     = 0;
    private static final int DATA_IS_FROM_BUDS        = 1;
    private static final int DATA_STRAIN              = 2;
    private static final int DATA_QUALITY             = 3;
    private static final int DATA_SOLVENT_COUNT       = 4;
    private static final int DATA_EXTRACTION_PROGRESS = 5;
    private static final int DATA_IS_EXTRACTING       = 6;
    private static final int DATA_HAS_OUTPUT          = 7;
    private static final int DATA_SIZE                = 8;

    // Server-side constructor
    public OelExtraktortMenu(int containerId, Inventory playerInventory, OelExtraktortBlockEntity blockEntity) {
        super(CannabisMenuTypes.OEL_EXTRAKTOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_MATERIAL_WEIGHT     -> blockEntity.getMaterialWeight();
                    case DATA_IS_FROM_BUDS        -> blockEntity.isFromBuds() ? 1 : 0;
                    case DATA_STRAIN              -> blockEntity.getStrain().ordinal();
                    case DATA_QUALITY             -> blockEntity.getBaseQuality().ordinal();
                    case DATA_SOLVENT_COUNT       -> blockEntity.getSolventCount();
                    case DATA_EXTRACTION_PROGRESS -> (int)(blockEntity.getExtractionProgress() * OelExtraktortBlockEntity.EXTRACTION_TICKS);
                    case DATA_IS_EXTRACTING       -> blockEntity.isExtracting() ? 1 : 0;
                    case DATA_HAS_OUTPUT          -> blockEntity.hasOutput() ? 1 : 0;
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
    public OelExtraktortMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CannabisMenuTypes.OEL_EXTRAKTOR_MENU.get(), containerId);

        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof OelExtraktortBlockEntity extraktor ? extraktor : null;

        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
    }

    public static final int BUTTON_START = 0;

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_START && blockEntity != null && !blockEntity.isRemoved()) {
            return blockEntity.startExtraction();
        }
        return false;
    }

    // Getter
    public int getMaterialWeight()        { return this.data.get(DATA_MATERIAL_WEIGHT); }
    public boolean isFromBuds()           { return this.data.get(DATA_IS_FROM_BUDS) == 1; }
    public int getSolventCount()          { return this.data.get(DATA_SOLVENT_COUNT); }
    public boolean isExtracting()         { return this.data.get(DATA_IS_EXTRACTING) == 1; }
    public boolean hasOutput()            { return this.data.get(DATA_HAS_OUTPUT) == 1; }
    public int getExtractionProgressRaw() { return this.data.get(DATA_EXTRACTION_PROGRESS); }
    public float getExtractionProgressF() { return (float)getExtractionProgressRaw() / OelExtraktortBlockEntity.EXTRACTION_TICKS; }
    public boolean canStart() {
        return getMaterialWeight() >= OelExtraktortBlockEntity.MIN_MATERIAL_WEIGHT
                && getSolventCount() >= 1
                && !isExtracting()
                && !hasOutput();
    }
    public int getExpectedOilAmount() {
        float rate = isFromBuds()
                ? OelExtraktortBlockEntity.BUD_CONVERSION_RATE
                : OelExtraktortBlockEntity.TRIM_CONVERSION_RATE;
        return Math.max(1, (int)(getMaterialWeight() * rate));
    }

    public CannabisStrain getStrain() {
        int ordinal = this.data.get(DATA_STRAIN);
        CannabisStrain[] values = CannabisStrain.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : CannabisStrain.HYBRID;
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
        private final OelExtraktortBlockEntity blockEntity;

        public Provider(OelExtraktortBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.oel_extraktor.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new OelExtraktortMenu(containerId, playerInventory, blockEntity);
        }
    }
}
