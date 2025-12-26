package de.rolandsw.schedulemc.mapview.gui.overridden;

public enum EnumOptionsMapView {
    OLD_NORTH("options.minimap.oldNorth", false, true, false),
    ZOOM("option.minimapZoom", false, true, false),
    LOCATION("options.minimap.location", false, false, true),
    SIZE("options.minimap.size", false, false, true);

    private final boolean isFloat;
    private final boolean isBoolean;
    private final boolean isList;
    private final String name;

    EnumOptionsMapView(String name, boolean isFloat, boolean isBoolean, boolean isList) {
        this.name = name;
        this.isFloat = isFloat;
        this.isBoolean = isBoolean;
        this.isList = isList;
    }

    public boolean isFloat() {
        return this.isFloat;
    }

    public boolean isBoolean() {
        return this.isBoolean;
    }

    public boolean isList() {
        return this.isList;
    }

    public String getName() {
        return this.name;
    }
}
