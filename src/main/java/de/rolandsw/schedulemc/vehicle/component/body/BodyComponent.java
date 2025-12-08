package de.rolandsw.schedulemc.vehicle.component.body;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;

/**
 * Component representing the vehicle's body/chassis.
 * Defines appearance, size, and structural properties.
 */
public class BodyComponent extends BaseComponent {

    private BodySpecification specification;
    private int color = 0xFFFFFF; // RGB color
    private float structuralIntegrity = 1.0f; // 0.0 to 1.0

    public BodyComponent(BodySpecification specification) {
        super(ComponentType.BODY);
        this.specification = specification;
    }

    public BodyComponent() {
        this(BodySpecification.SEDAN);
    }

    // Getters and Setters
    public BodySpecification getSpecification() {
        return specification;
    }

    public void setSpecification(BodySpecification specification) {
        this.specification = specification;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getStructuralIntegrity() {
        return structuralIntegrity;
    }

    public void setStructuralIntegrity(float integrity) {
        this.structuralIntegrity = Math.max(0, Math.min(1, integrity));
    }

    /**
     * Gets the number of passenger seats.
     */
    public int getPassengerCapacity() {
        return specification.getPassengerSeats();
    }

    /**
     * Gets the weight multiplier for physics calculations.
     */
    public float getWeightMultiplier() {
        return specification.getWeight();
    }

    /**
     * Gets aerodynamic efficiency (affects top speed).
     */
    public float getAerodynamicEfficiency() {
        return specification.getAerodynamics() * structuralIntegrity;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putString("BodyType", specification.getIdentifier());
        tag.putInt("Color", color);
        tag.putFloat("StructuralIntegrity", structuralIntegrity);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        String bodyType = tag.getString("BodyType");
        this.specification = BodySpecification.getByIdentifier(bodyType);
        this.color = tag.getInt("Color");
        this.structuralIntegrity = tag.getFloat("StructuralIntegrity");
    }

    @Override
    public IVehicleComponent duplicate() {
        BodyComponent copy = new BodyComponent(specification);
        copy.color = this.color;
        copy.structuralIntegrity = this.structuralIntegrity;
        return copy;
    }

    @Override
    public boolean isValid() {
        return specification != null && structuralIntegrity > 0;
    }
}
