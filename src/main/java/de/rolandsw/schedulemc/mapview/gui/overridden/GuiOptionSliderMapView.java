package de.rolandsw.schedulemc.mapview.gui.overridden;

import de.rolandsw.schedulemc.mapview.interfaces.ISettingsManager;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class GuiOptionSliderMapView extends AbstractSliderButton {
    private final ISettingsManager options;
    private final EnumOptionsMapView option;

    public GuiOptionSliderMapView(int x, int y, EnumOptionsMapView optionIn, float value, ISettingsManager options) {
        super (x, y, 150, 20, Component.literal(options.getKeyText(optionIn)), value);
        this.options = options;
        this.option = optionIn;
    }

    @Override
    protected void updateMessage() { setMessage(Component.literal(this.options.getKeyText(this.option))); }

    @Override
    protected void applyValue() { this.options.setOptionFloatValue(option, (float) this.value); }

    public EnumOptionsMapView returnEnumOptions() { return option; }

    public void setValue(float value) {
        if (isHovered()) {
            return;
        }

        this.value = value;
        this.updateMessage();
    }
}