package de.rolandsw.schedulemc.vehicle.component.control;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;

/**
 * Component representing a vehicle license plate.
 * Stores the plate number/text.
 */
public class LicensePlateComponent extends BaseComponent {

    private String plateText = "";
    private int plateColor = 0xFFFFFF;
    private int textColor = 0x000000;

    public LicensePlateComponent() {
        super(ComponentType.LICENSE_PLATE);
    }

    public LicensePlateComponent(String plateText) {
        super(ComponentType.LICENSE_PLATE);
        this.plateText = plateText;
    }

    // Getters and Setters
    public String getPlateText() {
        return plateText;
    }

    public void setPlateText(String text) {
        // Limit length to prevent issues
        if (text != null && text.length() <= 10) {
            this.plateText = text;
        }
    }

    public int getPlateColor() {
        return plateColor;
    }

    public void setPlateColor(int color) {
        this.plateColor = color;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    public boolean hasPlate() {
        return plateText != null && !plateText.isEmpty();
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putString("PlateText", plateText);
        tag.putInt("PlateColor", plateColor);
        tag.putInt("TextColor", textColor);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.plateText = tag.getString("PlateText");
        this.plateColor = tag.getInt("PlateColor");
        this.textColor = tag.getInt("TextColor");
    }

    @Override
    public IVehicleComponent duplicate() {
        LicensePlateComponent copy = new LicensePlateComponent(plateText);
        copy.plateColor = this.plateColor;
        copy.textColor = this.textColor;
        return copy;
    }

    @Override
    public boolean isValid() {
        return plateText != null && plateText.length() <= 10;
    }
}
