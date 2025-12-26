package de.rolandsw.schedulemc.mapview.presentation.component;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class OptionButton extends Button {
    private final MapOption enumOptions;

    public OptionButton(int x, int y, MapOption par4EnumOptions, Component message, OnPress onPress) {
        super (x, y, 150, 20, message, onPress, DEFAULT_NARRATION);
        this.enumOptions = par4EnumOptions;
    }

    public MapOption returnEnumOptions() { return this.enumOptions; }
}