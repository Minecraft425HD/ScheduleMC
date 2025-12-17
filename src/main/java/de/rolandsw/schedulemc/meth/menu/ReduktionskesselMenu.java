package de.rolandsw.schedulemc.meth.menu;

import de.rolandsw.schedulemc.meth.MethQuality;
import de.rolandsw.schedulemc.meth.blockentity.ReduktionskesselBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Container-Menu für den Reduktionskessel
 * Arcade-Style Temperaturkontrolle
 */
public class ReduktionskesselMenu extends AbstractContainerMenu {

    public final ReduktionskesselBlockEntity blockEntity;
    private final ContainerData data;

    // Data Indices
    private static final int DATA_TEMPERATURE = 0;
    private static final int DATA_PROGRESS = 1;
    private static final int DATA_PROCESS_TIME = 2;
    private static final int DATA_IS_HEATING = 3;
    private static final int DATA_IS_PROCESSING = 4;
    private static final int DATA_EXPECTED_QUALITY = 5;
    private static final int DATA_INPUT_QUALITY = 6;
    private static final int DATA_SIZE = 7;

    // Server-side constructor
    public ReduktionskesselMenu(int containerId, Inventory playerInventory, ReduktionskesselBlockEntity blockEntity) {
        super(MethMenuTypes.REDUKTIONSKESSEL_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        // ContainerData für Server-Client Sync
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_TEMPERATURE -> (int) blockEntity.getTemperature();
                    case DATA_PROGRESS -> blockEntity.getProcessProgress();
                    case DATA_PROCESS_TIME -> blockEntity.getProcessTime();
                    case DATA_IS_HEATING -> blockEntity.isHeating() ? 1 : 0;
                    case DATA_IS_PROCESSING -> blockEntity.isProcessing() ? 1 : 0;
                    case DATA_EXPECTED_QUALITY -> blockEntity.getExpectedQuality().getLevel();
                    case DATA_INPUT_QUALITY -> blockEntity.getInputQuality().getLevel();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Nur Heating kann vom Client gesetzt werden
                if (index == DATA_IS_HEATING) {
                    blockEntity.setHeating(value == 1);
                }
            }

            @Override
            public int getCount() {
                return DATA_SIZE;
            }
        };

        addDataSlots(this.data);
    }

    // Client-side constructor
    public ReduktionskesselMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(MethMenuTypes.REDUKTIONSKESSEL_MENU.get(), containerId);

        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof ReduktionskesselBlockEntity kessel) {
            this.blockEntity = kessel;
        } else {
            this.blockEntity = null;
        }

        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
    }

    /**
     * Setzt Heizung an/aus (vom GUI Button)
     */
    public void setHeating(boolean heating) {
        this.data.set(DATA_IS_HEATING, heating ? 1 : 0);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER für GUI
    // ═══════════════════════════════════════════════════════════

    public int getTemperature() {
        return this.data.get(DATA_TEMPERATURE);
    }

    public int getProgress() {
        return this.data.get(DATA_PROGRESS);
    }

    public int getProcessTime() {
        return this.data.get(DATA_PROCESS_TIME);
    }

    public float getProgressPercent() {
        int processTime = getProcessTime();
        if (processTime <= 0) return 0;
        return (float) getProgress() / processTime;
    }

    public boolean isHeating() {
        return this.data.get(DATA_IS_HEATING) == 1;
    }

    public boolean isProcessing() {
        return this.data.get(DATA_IS_PROCESSING) == 1;
    }

    public MethQuality getExpectedQuality() {
        return MethQuality.fromLevel(this.data.get(DATA_EXPECTED_QUALITY));
    }

    public MethQuality getInputQuality() {
        return MethQuality.fromLevel(this.data.get(DATA_INPUT_QUALITY));
    }

    /**
     * Gibt Temperatur-Zone als String zurück
     */
    public String getTemperatureZone() {
        int temp = getTemperature();
        if (temp < ReduktionskesselBlockEntity.TEMP_OPTIMAL_MIN) {
            return "§9ZU KALT";
        } else if (temp <= ReduktionskesselBlockEntity.TEMP_OPTIMAL_MAX) {
            return "§aOPTIMAL";
        } else if (temp <= ReduktionskesselBlockEntity.TEMP_DANGER_MAX) {
            return "§c⚠ GEFAHR";
        } else {
            return "§4§l☠ KRITISCH!";
        }
    }

    /**
     * Gibt Temperaturfarbe für GUI zurück
     */
    public int getTemperatureColor() {
        int temp = getTemperature();
        if (temp < ReduktionskesselBlockEntity.TEMP_OPTIMAL_MIN) {
            return 0x5555FF; // Blau - zu kalt
        } else if (temp <= ReduktionskesselBlockEntity.TEMP_OPTIMAL_MAX) {
            return 0x55FF55; // Grün - optimal
        } else if (temp <= ReduktionskesselBlockEntity.TEMP_DANGER_MAX) {
            return 0xFFAA00; // Orange - gefährlich
        } else {
            return 0xFF5555; // Rot - kritisch
        }
    }

    /**
     * Berechnet Thermometer-Position (0.0 = unten, 1.0 = oben)
     */
    public float getThermometerPosition() {
        int temp = getTemperature();
        // Skaliere von 20°C (0.0) bis 160°C (1.0)
        float normalized = (temp - 20f) / 140f;
        return Math.max(0, Math.min(1, normalized));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY; // Keine Slots zum Verschieben
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() &&
                player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                        blockEntity.getBlockPos().getY() + 0.5,
                        blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Heizung aus und aktiven Spieler entfernen wenn GUI geschlossen wird
        if (blockEntity != null) {
            blockEntity.setHeating(false);
            blockEntity.clearActivePlayer();
        }
    }
}
