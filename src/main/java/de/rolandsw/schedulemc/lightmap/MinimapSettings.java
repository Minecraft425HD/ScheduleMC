package de.rolandsw.schedulemc.lightmap;

import de.rolandsw.schedulemc.lightmap.gui.overridden.EnumOptionsMinimap;
import de.rolandsw.schedulemc.lightmap.interfaces.ISettingsManager;
import de.rolandsw.schedulemc.lightmap.interfaces.ISubSettingsManager;
import de.rolandsw.schedulemc.lightmap.util.MessageUtils;
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

public class MinimapSettings implements ISettingsManager {
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

    public final boolean moveMapDownWhileStatusEffect = true;
    public final boolean moveScoreBoardDown = true;
    protected boolean realTimeTorches;
    public KeyMapping keyBindZoom;
    public KeyMapping keyBindFullscreen;
    public KeyMapping keyBindMenu;
    public final KeyMapping[] keyBindings;
    private boolean somethingChanged;
    public static MinimapSettings instance;
    private final List<ISubSettingsManager> subSettingsManagers = new ArrayList<>();

    public String teleportCommand = "tp %p %x %y %z";
    public String serverTeleportCommand;

    public MinimapSettings() {
        instance = this;
        String category = "key.categories.lightmap";

        keyBindZoom = new KeyMapping("key.minimap.zoom", InputConstants.getKey("key.keyboard.z").getValue(), category);
        keyBindFullscreen = new KeyMapping("key.minimap.toggleFullscreen", InputConstants.getKey("key.keyboard.x").getValue(), category);
        keyBindMenu = new KeyMapping("key.minimap.lightmapMenu", InputConstants.getKey("key.keyboard.m").getValue(), category);

        this.keyBindings = new KeyMapping[]{this.keyBindMenu, this.keyBindZoom, this.keyBindFullscreen};
    }

    public void addSecondaryOptionsManager(ISubSettingsManager secondarySettingsManager) {
        this.subSettingsManagers.add(secondarySettingsManager);
    }

    public void loadAll() {
        this.settingsFile = new File(LightMapConstants.getMinecraft().gameDirectory, "config/lightmap.properties");

        try {
            if (this.settingsFile.exists()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.settingsFile), StandardCharsets.UTF_8.newDecoder()));
                String sCurrentLine;
                while ((sCurrentLine = in.readLine()) != null) {
                    String[] curLine = sCurrentLine.split(":");
                    switch (curLine[0]) {
                        case "Zoom Level" -> this.zoom = Math.max(0, Math.min(4, Integer.parseInt(curLine[1])));
                        case "Old North" -> this.oldNorth = Boolean.parseBoolean(curLine[1]);
                        case "MinimapRenderer Corner" -> this.mapCorner = Math.max(0, Math.min(3, Integer.parseInt(curLine[1])));
                        case "MinimapRenderer Size" -> this.sizeModifier = Math.max(-1, Math.min(4, Integer.parseInt(curLine[1])));
                        case "Zoom Key" -> this.bindKey(this.keyBindZoom, curLine[1]);
                        case "Fullscreen Key" -> this.bindKey(this.keyBindFullscreen, curLine[1]);
                        case "Menu Key" -> this.bindKey(this.keyBindMenu, curLine[1]);
                    }
                }
                KeyMapping.resetMapping();
                for (ISubSettingsManager subSettingsManager : this.subSettingsManagers) {
                    subSettingsManager.loadSettings(this.settingsFile);
                }

                in.close();
            }

            this.saveAll();
        } catch (IOException exception) {
            LightMapConstants.getLogger().error(exception);
        }

    }

    private void bindKey(KeyMapping keyBinding, String id) {
        try {
            keyBinding.setKey(InputConstants.getKey(id));
        } catch (RuntimeException var4) {
            LightMapConstants.getLogger().warn(id + " is not a valid keybinding");
        }

    }

    public void saveAll() {
        File settingsFileDir = new File(LightMapConstants.getMinecraft().gameDirectory, "/config/");
        if (!settingsFileDir.exists()) {
            settingsFileDir.mkdirs();
        }

        this.settingsFile = new File(settingsFileDir, "lightmap.properties");

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.settingsFile), StandardCharsets.UTF_8.newEncoder())));
            out.println("Zoom Level:" + this.zoom);
            out.println("Old North:" + this.oldNorth);
            out.println("MinimapRenderer Corner:" + this.mapCorner);
            out.println("MinimapRenderer Size:" + this.sizeModifier);
            out.println("Zoom Key:" + this.keyBindZoom.saveString());
            out.println("Fullscreen Key:" + this.keyBindFullscreen.saveString());
            out.println("Menu Key:" + this.keyBindMenu.saveString());

            for (ISubSettingsManager subSettingsManager : this.subSettingsManagers) {
                subSettingsManager.saveAll(out);
            }

            out.close();
        } catch (FileNotFoundException var5) {
            MessageUtils.chatInfo("§EError Saving Settings " + var5.getLocalizedMessage());
        }
    }

    @Override
    public String getKeyText(EnumOptionsMinimap options) {
        String s = I18n.get(options.getName()) + ": ";
        if (options.isFloat()) {
            float f = this.getOptionFloatValue(options);
            if (options == EnumOptionsMinimap.ZOOM) {
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
    public float getOptionFloatValue(EnumOptionsMinimap options) {
        if (options == EnumOptionsMinimap.ZOOM) {
            return this.zoom;
        } else {
            return 0.0F;
        }
    }

    public boolean getOptionBooleanValue(EnumOptionsMinimap par1EnumOptions) {
        return switch (par1EnumOptions) {
            case OLD_NORTH -> this.oldNorth;
            default -> throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a boolean applicable to minimap)");
        };
    }

    public String getOptionListValue(EnumOptionsMinimap par1EnumOptions) {
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
    public void setOptionFloatValue(EnumOptionsMinimap options, float value) {
        this.somethingChanged = true;
    }

    public void setOptionValue(EnumOptionsMinimap par1EnumOptions) {
        switch (par1EnumOptions) {
            case OLD_NORTH -> this.oldNorth = !this.oldNorth;
            case LOCATION -> this.mapCorner = this.mapCorner >= 3 ? 0 : this.mapCorner + 1;
            case SIZE -> this.sizeModifier = this.sizeModifier >= 4 ? -1 : this.sizeModifier + 1;
            default ->
                    throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName());
        }

        this.somethingChanged = true;
    }

    public String getKeyBindingDescription(int keybindIndex) {
        return this.keyBindings[keybindIndex].getName().equals("key.minimap.lightmapMenu") ? I18n.get("key.minimap.menu") : I18n.get(this.keyBindings[keybindIndex].getName());
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
}
