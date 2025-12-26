package de.rolandsw.schedulemc.mapview.gui.overridden;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiOptionButtonMapView extends Button {
    private final MapOption enumOptions;

    public GuiOptionButtonMapView(int x, int y, MapOption par4EnumOptions, Component message, OnPress onPress) {
        super (x, y, 150, 20, message, onPress, DEFAULT_NARRATION);
        this.enumOptions = par4EnumOptions;
    }

    public MapOption returnEnumOptions() { return this.enumOptions; }
}