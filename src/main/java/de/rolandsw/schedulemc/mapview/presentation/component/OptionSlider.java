package de.rolandsw.schedulemc.mapview.presentation.component;

import de.rolandsw.schedulemc.mapview.config.MapOption;
import de.rolandsw.schedulemc.mapview.core.event.SettingsManager;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class OptionSlider extends AbstractSliderButton {
    private final SettingsManager options;
    private final MapOption option;

    public OptionSlider(int x, int y, MapOption optionIn, float value, SettingsManager options) {
        super (x, y, 150, 20, Component.literal(options.getKeyText(optionIn)), value);
        this.options = options;
        this.option = optionIn;
    }

    @Override
    protected void updateMessage() { setMessage(Component.literal(this.options.getKeyText(this.option))); }

    @Override
    protected void applyValue() { this.options.setOptionFloatValue(option, (float) this.value); }

    public MapOption returnEnumOptions() { return option; }

    public void setValue(float value) {
        if (isHovered()) {
            return;
        }

        this.value = value;
        this.updateMessage();
    }
}