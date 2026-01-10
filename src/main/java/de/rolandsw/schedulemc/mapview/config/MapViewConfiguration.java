package de.rolandsw.schedulemc.mapview.config;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.config.MapOption;
import de.rolandsw.schedulemc.mapview.core.event.SettingsManager;
import de.rolandsw.schedulemc.mapview.core.event.SubSettingsManager;
import de.rolandsw.schedulemc.mapview.util.MessageUtils;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MapViewConfiguration implements SettingsManager {
    private File settingsFile;
    public boolean showUnderMenus;
    private final int availableProcessors = Runtime.getRuntime().availableProcessors();
    public final boolean multicore = this.availableProcessors > 1;
    public final boolean lightmap = true;
    public final boolean heightmap = true;
    public final boolean slopemap = true;
    public final boolean filtering = true;
    public final boolean waterTransparency = true;
    public final boolean blockTransparency = true;
    public final boolean biomes = true;
    public final int biomeOverlay = 0;
    public final boolean chunkGrid = false;
    public final boolean slimeChunks = false;
    public final boolean worldborder = true;
    public final boolean squareMap = true;
    public final boolean rotates = false;
    public boolean oldNorth;
    public int zoom = 2;
    public int sizeModifier = 1;
    public int mapCorner = 1;

    public boolean worldmapAllowed = true;
    public boolean minimapAllowed = true;
    public boolean showTerritories = false; // Territory Overlay on worldmap for normal players

    public final boolean moveMapDownWhileStatusEffect = true;
    public final boolean moveScoreBoardDown = true;
    protected boolean realTimeTorches;
    public KeyMapping keyBindZoom;
    public KeyMapping keyBindFullscreen;
    public KeyMapping keyBindMenu;
    public final KeyMapping[] keyBindings;
    private boolean somethingChanged;
    public static MapViewConfiguration instance;
    private final List<SubSettingsManager> subSettingsManagers = new ArrayList<>();

    public String teleportCommand = "tp %p %x %y %z";
    public String serverTeleportCommand;

    public MapViewConfiguration() {
        instance = this;
        String category = "key.categories.mapview";

        keyBindZoom = new KeyMapping("key.mapview.zoom", InputConstants.getKey("key.keyboard.z").getValue(), category);
        keyBindFullscreen = new KeyMapping("key.mapview.toggleFullscreen", InputConstants.getKey("key.keyboard.x").getValue(), category);
        keyBindMenu = new KeyMapping("key.mapview.menu", InputConstants.getKey("key.keyboard.m").getValue(), category);

        this.keyBindings = new KeyMapping[]{this.keyBindMenu, this.keyBindZoom, this.keyBindFullscreen};
    }

    public void addSecondaryOptionsManager(SubSettingsManager secondarySettingsManager) {
        this.subSettingsManagers.add(secondarySettingsManager);
    }

    public void loadAll() {
        this.settingsFile = new File(MapViewConstants.getMinecraft().gameDirectory, "config/mapview.properties");

        try {
            if (this.settingsFile.exists()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.settingsFile), StandardCharsets.UTF_8.newDecoder()));
                String sCurrentLine;
                while ((sCurrentLine = in.readLine()) != null) {
                    String[] curLine = sCurrentLine.split(":");
                    switch (curLine[0]) {
                        case "Zoom Level" -> this.zoom = Math.max(0, Math.min(4, Integer.parseInt(curLine[1])));
                        case "Old North" -> this.oldNorth = Boolean.parseBoolean(curLine[1]);
                        case "MapViewRenderer Corner" -> this.mapCorner = Math.max(0, Math.min(3, Integer.parseInt(curLine[1])));
                        case "MapViewRenderer Size" -> this.sizeModifier = Math.max(-1, Math.min(4, Integer.parseInt(curLine[1])));
                        case "Show Territories" -> this.showTerritories = Boolean.parseBoolean(curLine[1]);
                        case "Zoom Key" -> this.bindKey(this.keyBindZoom, curLine[1]);
                        case "Fullscreen Key" -> this.bindKey(this.keyBindFullscreen, curLine[1]);
                        case "Menu Key" -> this.bindKey(this.keyBindMenu, curLine[1]);
                    }
                }
                KeyMapping.resetMapping();
                for (SubSettingsManager subSettingsManager : this.subSettingsManagers) {
                    subSettingsManager.loadSettings(this.settingsFile);
                }

                in.close();
            }

            this.saveAll();
        } catch (IOException exception) {
            MapViewConstants.getLogger().error(exception);
        }

    }

    private void bindKey(KeyMapping keyBinding, String id) {
        try {
            keyBinding.setKey(InputConstants.getKey(id));
        } catch (RuntimeException var4) {
            MapViewConstants.getLogger().warn(id + " is not a valid keybinding");
        }

    }

    public void saveAll() {
        File settingsFileDir = new File(MapViewConstants.getMinecraft().gameDirectory, "/config/");
        if (!settingsFileDir.exists()) {
            settingsFileDir.mkdirs();
        }

        this.settingsFile = new File(settingsFileDir, "mapview.properties");

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.settingsFile), StandardCharsets.UTF_8.newEncoder())));
            out.println("Zoom Level:" + this.zoom);
            out.println("Old North:" + this.oldNorth);
            out.println("MapViewRenderer Corner:" + this.mapCorner);
            out.println("MapViewRenderer Size:" + this.sizeModifier);
            out.println("Show Territories:" + this.showTerritories);
            out.println("Zoom Key:" + this.keyBindZoom.saveString());
            out.println("Fullscreen Key:" + this.keyBindFullscreen.saveString());
            out.println("Menu Key:" + this.keyBindMenu.saveString());

            for (SubSettingsManager subSettingsManager : this.subSettingsManagers) {
                subSettingsManager.saveAll(out);
            }

            out.close();
        } catch (FileNotFoundException var5) {
            MessageUtils.chatInfo("§EError Saving Settings " + var5.getLocalizedMessage());
        }
    }

    @Override
    public String getKeyText(MapOption options) {
        String s = I18n.get(options.getName()) + ": ";
        if (options.isFloat()) {
            float f = this.getOptionFloatValue(options);
            if (options == MapOption.ZOOM) {
                return s + (int) f;
            } else {
                return f == 0.0F ? s + I18n.get("options.off") : s + (int) f + "%";
            }
        } else if (options.isBoolean()) {
            boolean flag = this.getOptionBooleanValue(options);
            return flag ? s + I18n.get("options.on") : s + I18n.get("options.off");
        } else if (options.isList()) {
            String state = this.getOptionListValue(options);
            return s + state;
        } else {
            return s;
        }
    }

    @Override
    public float getOptionFloatValue(MapOption options) {
        if (options == MapOption.ZOOM) {
            return this.zoom;
        } else {
            return 0.0F;
        }
    }

    public boolean getOptionBooleanValue(MapOption par1EnumOptions) {
        return switch (par1EnumOptions) {
            case OLD_NORTH -> this.oldNorth;
            case SHOW_TERRITORIES -> this.showTerritories;
            default -> throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a boolean applicable to minimap)");
        };
    }

    public String getOptionListValue(MapOption par1EnumOptions) {
        switch (par1EnumOptions) {
            case LOCATION -> {
                if (this.mapCorner == 0) {
                    return I18n.get("options.minimap.location.topLeft");
                } else if (this.mapCorner == 1) {
                    return I18n.get("options.minimap.location.topRight");
                } else if (this.mapCorner == 2) {
                    return I18n.get("options.minimap.location.bottomRight");
                } else {
                    if (this.mapCorner == 3) {
                        return I18n.get("options.minimap.location.bottomLeft");
                    }

                    return "Error";
                }
            }
            case SIZE -> {
                if (this.sizeModifier == -1) {
                    return I18n.get("options.minimap.size.small");
                } else if (this.sizeModifier == 0) {
                    return I18n.get("options.minimap.size.medium");
                } else if (this.sizeModifier == 1) {
                    return I18n.get("options.minimap.size.large");
                } else if (this.sizeModifier == 2) {
                    return I18n.get("options.minimap.size.xl");
                } else if (this.sizeModifier == 3) {
                    return I18n.get("options.minimap.size.xxl");
                } else {
                    if (this.sizeModifier == 4) {
                        return I18n.get("options.minimap.size.xxxl");
                    }

                    return "error";
                }
            }
            default ->
                    throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a list value applicable to minimap)");
        }
    }

    @Override
    public void setOptionFloatValue(MapOption options, float value) {
        this.somethingChanged = true;
    }

    public void setOptionValue(MapOption par1EnumOptions) {
        switch (par1EnumOptions) {
            case OLD_NORTH -> this.oldNorth = !this.oldNorth;
            case SHOW_TERRITORIES -> this.showTerritories = !this.showTerritories;
            case LOCATION -> this.mapCorner = this.mapCorner >= 3 ? 0 : this.mapCorner + 1;
            case SIZE -> this.sizeModifier = this.sizeModifier >= 4 ? -1 : this.sizeModifier + 1;
            default ->
                    throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName());
        }

        this.somethingChanged = true;
    }

    public String getKeyBindingDescription(int keybindIndex) {
        return this.keyBindings[keybindIndex].getName().equals("key.mapview.menu") ? I18n.get("key.mapview.menu") : I18n.get(this.keyBindings[keybindIndex].getName());
    }

    public Component getKeybindDisplayString(int keybindIndex) {
        KeyMapping keyBinding = this.keyBindings[keybindIndex];
        return this.getKeybindDisplayString(keyBinding);
    }

    public Component getKeybindDisplayString(KeyMapping keyBinding) {
        return keyBinding.getTranslatedKeyMessage();
    }

    public void setKeyBinding(KeyMapping keyBinding, InputConstants.Key input) {
        keyBinding.setKey(input);
        this.saveAll();
    }

    public boolean isChanged() {
        if (this.somethingChanged) {
            this.somethingChanged = false;
            return true;
        } else {
            return false;
        }
    }

    // Public getter for protected field
    public boolean isRealTimeTorches() {
        return realTimeTorches;
    }
}
