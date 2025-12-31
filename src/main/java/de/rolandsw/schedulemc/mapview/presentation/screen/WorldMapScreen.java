package de.rolandsw.schedulemc.mapview.presentation.screen;

import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.config.WorldMapConfiguration;
import de.rolandsw.schedulemc.mapview.service.data.WorldMapData;
import de.rolandsw.schedulemc.mapview.npc.NPCMapRenderer;
import de.rolandsw.schedulemc.mapview.navigation.graph.RoadNavigationService;
import de.rolandsw.schedulemc.mapview.navigation.graph.NavigationTarget;
import de.rolandsw.schedulemc.mapview.navigation.graph.NavigationOverlay;
import net.minecraft.core.BlockPos;
import de.rolandsw.schedulemc.mapview.data.cache.RegionCache;
import de.rolandsw.schedulemc.mapview.data.persistence.AsyncPersistenceManager;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.service.data.MapDataManager;
import de.rolandsw.schedulemc.mapview.presentation.screen.MapOptionsScreen;
import de.rolandsw.schedulemc.mapview.presentation.component.PopupComponent;
import de.rolandsw.schedulemc.mapview.presentation.component.PopupButton;
import de.rolandsw.schedulemc.mapview.presentation.screen.PopupScreen;
import de.rolandsw.schedulemc.mapview.core.model.AbstractMapData;
import de.rolandsw.schedulemc.mapview.textures.Sprite;
import de.rolandsw.schedulemc.mapview.textures.TextureAtlas;
import de.rolandsw.schedulemc.mapview.util.BackgroundImageInfo;
import de.rolandsw.schedulemc.mapview.core.model.BiomeData;
import de.rolandsw.schedulemc.mapview.util.DimensionContainer;
import de.rolandsw.schedulemc.mapview.util.EasingUtils;
import de.rolandsw.schedulemc.mapview.integration.minecraft.MinecraftAccessor;
import de.rolandsw.schedulemc.mapview.util.ImageHelper;
import de.rolandsw.schedulemc.mapview.util.MapViewGuiGraphics;
import de.rolandsw.schedulemc.mapview.util.MapViewPipelines;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.border.WorldBorder;
import org.lwjgl.glfw.GLFW;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public class WorldMapScreen extends PopupScreen {
    private final Random generator = new Random();
    private final WorldMapData persistentMap;
    private final Screen parent;
    private final MapViewConfiguration mapOptions;
    private final WorldMapConfiguration options;
    protected String screenTitle = "World MapViewRenderer";
    protected String worldNameDisplay = "";
    protected int worldNameDisplayLength;
    protected int maxWorldNameDisplayLength;
    private String subworldName = "";
    private int top;
    private int bottom;
    private boolean oldNorth;
    private boolean lastStill;
    private boolean editingCoordinates;
    private boolean lastEditingCoordinates;
    private EditBox coordinates;
    int centerX;
    int centerY;
    float mapCenterX;
    float mapCenterZ;
    float deltaX;
    float deltaY;
    float deltaXonRelease;
    float deltaYonRelease;
    long timeOfRelease;
    boolean mouseCursorShown = true;
    long timeAtLastTick;
    long timeOfLastKBInput;
    long timeOfLastMouseInput;
    float lastMouseX;
    float lastMouseY;
    protected int mouseX;
    protected int mouseY;
    boolean leftMouseButtonDown;
    float zoom;
    float zoomStart;
    float zoomGoal;
    long timeOfZoom;
    float zoomDirectX;
    float zoomDirectY;
    private float scScale = 1.0F;
    private float guiToMap = 2.0F;
    private float mapToGui = 0.5F;
    private float mouseDirectToMap = 1.0F;
    private float guiToDirectMouse = 2.0F;
    private static boolean gotSkin;
    private boolean closed;
    private RegionCache[] regions = new RegionCache[0];
    private final BiomeData biomeMapData = new BiomeData(760, 360);
    private final NPCMapRenderer npcMapRenderer = new NPCMapRenderer();
    private float mapPixelsX;
    private float mapPixelsY;
    private final Object closedLock = new Object();
    int sideMargin = 10;
    int buttonCount = 3;
    int buttonSeparation = 4;
    int buttonWidth = 66;
    public boolean passEvents;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final ResourceLocation lightmapSkinLocation = new ResourceLocation("schedulemc", "mapview/persistentmap/playerskin");
    private final ResourceLocation crosshairResource = new ResourceLocation("textures/gui/sprites/hud/crosshair.png");
    private boolean currentDragging;
    private boolean keySprintPressed;
    private boolean keyUpPressed;
    private boolean keyDownPressed;
    private boolean keyLeftPressed;
    private boolean keyRightPressed;
    private int iconsWidth = 16;
    private int iconsHeight = 16;

    public WorldMapScreen(Screen parent) {
        this.parent = parent;
        this.setParentScreen(this.parent);

        mapOptions = MapViewConstants.getLightMapInstance().getMapOptions();
        this.persistentMap = MapViewConstants.getLightMapInstance().getWorldMapData();
        this.options = MapViewConstants.getLightMapInstance().getWorldMapDataOptions();
        this.zoom = this.options.getZoom();
        this.zoomStart = this.options.getZoom();
        this.zoomGoal = this.options.getZoom();
        this.persistentMap.setLightMapArray(MapViewConstants.getLightMapInstance().getMap().getLightmapArray());
        if (!gotSkin) {
            this.getSkin();
        }

    }

    private void getSkin() {
        java.util.Map<com.mojang.authlib.minecraft.MinecraftProfileTexture.Type, com.mojang.authlib.minecraft.MinecraftProfileTexture> skinMap =
            MapViewConstants.getMinecraft().getSkinManager().getInsecureSkinInformation(MapViewConstants.getPlayer().getGameProfile());
        com.mojang.authlib.minecraft.MinecraftProfileTexture skinTexture = skinMap.get(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN);
        ResourceLocation skinLocation = skinTexture != null ?
            MapViewConstants.getMinecraft().getSkinManager().registerTexture(skinTexture, com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN) : null;

        BufferedImage skinImage = ImageHelper.createBufferedImageFromIdentifier(skinLocation);

        if (skinImage == null) {
            if (MapViewConstants.DEBUG) {
                MapViewConstants.getLogger().warn("Got no player skin!");
            }
            return;
        }

        gotSkin = true;

        boolean showHat = MapViewConstants.getPlayer().isModelPartShown(PlayerModelPart.HAT);
        if (showHat) {
            skinImage = ImageHelper.addImages(ImageHelper.loadImage(skinImage, 8, 8, 8, 8), ImageHelper.loadImage(skinImage, 40, 8, 8, 8), 0.0F, 0.0F, 8, 8);
        } else {
            skinImage = ImageHelper.loadImage(skinImage, 8, 8, 8, 8);
        }

        float scale = skinImage.getWidth() / 8.0F;
        skinImage = ImageHelper.fillOutline(ImageHelper.pad(ImageHelper.scaleImage(skinImage, 2.0F / scale)), true, 1);

        DynamicTexture texture = new DynamicTexture(ImageHelper.nativeImageFromBufferedImage(skinImage));
        minecraft.getTextureManager().register(lightmapSkinLocation, texture);
    }

    @Override
    public void init() {
        this.passEvents = true;
        this.oldNorth = mapOptions.oldNorth;
        this.centerAt(this.options.getMapX(), this.options.getMapZ());
        if (minecraft.screen == this) {
            this.closed = false;
        }

        this.screenTitle = I18n.get("worldmap.title");
        this.buildWorldName();
        this.leftMouseButtonDown = false;
        this.sideMargin = 10;
        this.buttonCount = 3;
        this.buttonSeparation = 4;
        this.buttonWidth = (this.width - this.sideMargin * 2 - this.buttonSeparation * (this.buttonCount - 1)) / this.buttonCount;
        this.addRenderableWidget(new PopupButton(this.sideMargin + 1 * (this.buttonWidth + this.buttonSeparation), this.getHeight() - 28, this.buttonWidth, 20, Component.translatable("menu.options"), button -> minecraft.setScreen(new MapOptionsScreen(this)), this));
        this.addRenderableWidget(new PopupButton(this.sideMargin + 2 * (this.buttonWidth + this.buttonSeparation), this.getHeight() - 28, this.buttonWidth, 20, Component.translatable("gui.done"), button -> minecraft.setScreen(parent), this));
        this.coordinates = new EditBox(this.font, this.sideMargin, 10, 140, 20, null);
        this.top = 32;
        this.bottom = this.getHeight() - 32;
        this.centerX = this.getWidth() / 2;
        this.centerY = (this.bottom - this.top) / 2;
        this.scScale = (float) minecraft.getWindow().getGuiScale();
        this.mapPixelsX = minecraft.getWindow().getWidth();
        this.mapPixelsY = (minecraft.getWindow().getHeight() - (int) (64.0F * this.scScale));
        this.lastStill = false;
        this.timeAtLastTick = System.currentTimeMillis();
    }

    @Override
    public void added() {
        currentDragging = false;
        super.added();
    }

    private void centerAt(int x, int z) {
        if (this.oldNorth) {
            this.mapCenterX = (-z);
            this.mapCenterZ = x;
        } else {
            this.mapCenterX = x;
            this.mapCenterZ = z;
        }

    }

    private void buildWorldName() {
        final AtomicReference<String> worldName = new AtomicReference<>();

        MapViewConstants.getIntegratedServer().ifPresentOrElse(integratedServer -> {
            worldName.set(integratedServer.getWorldData().getLevelName());

            if (worldName.get() == null || worldName.get().isBlank()) {
                worldName.set("Singleplayer World");
            }
        }, () -> {
            ServerData info = minecraft.getCurrentServer();

            if (info != null) {
                worldName.set(info.name);
            }
            if (worldName.get() == null || worldName.get().isBlank()) {
                worldName.set("Multiplayer Server");
            }
            if (MapViewConstants.isRealmServer()) {
                worldName.set("Realms");
            }
        });

        StringBuilder worldNameBuilder = (new StringBuilder("§r")).append(worldName.get());
        String subworldName = "";
        this.subworldName = subworldName;

        if (subworldName != null && !subworldName.isEmpty()) {
            worldNameBuilder.append(" - ").append(subworldName);
        }

        this.worldNameDisplay = worldNameBuilder.toString();
        this.worldNameDisplayLength = this.font.width(this.worldNameDisplay);

        for (this.maxWorldNameDisplayLength = this.getWidth() / 2 - this.font.width(this.screenTitle) / 2 - this.sideMargin * 2; this.worldNameDisplayLength > this.maxWorldNameDisplayLength
                && worldName.get().length() > 5; this.worldNameDisplayLength = this.font.width(this.worldNameDisplay)) {
            worldName.set(worldName.get().substring(0, worldName.get().length() - 1));
            worldNameBuilder = new StringBuilder(worldName.get());
            worldNameBuilder.append("...");
            if (subworldName != null && !subworldName.isEmpty()) {
                worldNameBuilder.append(" - ").append(subworldName);
            }

            this.worldNameDisplay = worldNameBuilder.toString();
        }

        if (subworldName != null && !subworldName.isEmpty()) {
            while (this.worldNameDisplayLength > this.maxWorldNameDisplayLength && subworldName.length() > 5) {
                worldNameBuilder = new StringBuilder(worldName.get());
                worldNameBuilder.append("...");
                subworldName = subworldName.substring(0, subworldName.length() - 1);
                worldNameBuilder.append(" - ").append(subworldName);
                this.worldNameDisplay = worldNameBuilder.toString();
                this.worldNameDisplayLength = this.font.width(this.worldNameDisplay);
            }
        }

    }

    private float bindZoom(float zoom) {
        zoom = Math.max(this.options.getMinZoom(), zoom);
        return Math.min(this.options.getMaxZoom(), zoom);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.timeOfLastMouseInput = System.currentTimeMillis();
        this.switchToMouseInput();
        float mouseDirectX = (float) minecraft.mouseHandler.xpos();
        float mouseDirectY = (float) minecraft.mouseHandler.ypos();
        if (amount != 0.0) {
            if (amount > 0.0) {
                this.zoomGoal *= 1.26F;
            } else if (amount < 0.0) {
                this.zoomGoal /= 1.26F;
            }

            this.zoomStart = this.zoom;
            this.zoomGoal = this.bindZoom(this.zoomGoal);
            this.timeOfZoom = System.currentTimeMillis();
            this.zoomDirectX = mouseDirectX;
            this.zoomDirectY = mouseDirectY;
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        currentDragging = false;
        if (mouseY > this.top && mouseY < this.bottom && button == 1) {
            this.timeOfLastKBInput = 0L;
            int mouseDirectX = (int) minecraft.mouseHandler.xpos();
            int mouseDirectY = (int) minecraft.mouseHandler.ypos();
            if (MapDataManager.mapOptions.worldmapAllowed) {
                this.createPopup((int) mouseX, (int) mouseY, mouseDirectX, mouseDirectY);
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.popupOpen()) {
            this.coordinates.mouseClicked(mouseX, mouseY, button);
            this.editingCoordinates = this.coordinates.isFocused();
            if (this.editingCoordinates && !this.lastEditingCoordinates) {
                int x;
                int z;
                if (this.oldNorth) {
                    x = (int) Math.floor(this.mapCenterZ);
                    z = -((int) Math.floor(this.mapCenterX));
                } else {
                    x = (int) Math.floor(this.mapCenterX);
                    z = (int) Math.floor(this.mapCenterZ);
                }

                this.coordinates.setValue(x + ", " + z);
                this.coordinates.setTextColor(0xFFFFFF);
            }

            this.lastEditingCoordinates = this.editingCoordinates;
        }
        if (button == 0) {
            currentDragging = true;
        }
        return super.mouseClicked(mouseX, mouseY, button) || button == 1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.editingCoordinates && (minecraft.options.keyJump.matches(keyCode, scanCode) || minecraft.options.keyShift.matches(keyCode, scanCode))) {
            if (minecraft.options.keyJump.matches(keyCode, scanCode)) {
                this.zoomGoal /= 1.26F;
            }

            if (minecraft.options.keyShift.matches(keyCode, scanCode)) {
                this.zoomGoal *= 1.26F;
            }

            this.zoomStart = this.zoom;
            this.zoomGoal = this.bindZoom(this.zoomGoal);
            this.timeOfZoom = System.currentTimeMillis();
            this.zoomDirectX = (minecraft.getWindow().getWidth() / 2f);
            this.zoomDirectY = (minecraft.getWindow().getHeight() - minecraft.getWindow().getHeight() / 2f);
            this.switchToKeyboardInput();
        }

        this.clearPopups();
        if (this.editingCoordinates) {
            this.coordinates.keyPressed(keyCode, scanCode, modifiers);
            boolean isGood = this.isAcceptable(this.coordinates.getValue());
            this.coordinates.setTextColor(isGood ? 0xFFFFFF : 0xFF0000);
            if ((keyCode == 257 || keyCode == 335) && this.coordinates.isFocused() && isGood) {
                String[] xz = this.coordinates.getValue().split(",");
                this.centerAt(Integer.parseInt(xz[0].trim()), Integer.parseInt(xz[1].trim()));
                this.editingCoordinates = false;
                this.lastEditingCoordinates = false;
                this.switchToKeyboardInput();
            }

            if (keyCode == 258 && this.coordinates.isFocused()) {
                this.editingCoordinates = false;
                this.lastEditingCoordinates = false;
                this.switchToKeyboardInput();
            }
        }

        if (MapViewConstants.getLightMapInstance().getMapOptions().keyBindMenu.matches(keyCode, scanCode)) {
            keyCode = GLFW.GLFW_KEY_ESCAPE;
            scanCode = -1;
        }

        keySprintPressed = minecraft.options.keySprint.matches(keyCode, scanCode) || keySprintPressed;
        keyUpPressed = minecraft.options.keyUp.matches(keyCode, scanCode) || keyUpPressed;
        keyDownPressed = minecraft.options.keyDown.matches(keyCode, scanCode) || keyDownPressed;
        keyLeftPressed = minecraft.options.keyLeft.matches(keyCode, scanCode) || keyLeftPressed;
        keyRightPressed = minecraft.options.keyRight.matches(keyCode, scanCode) || keyRightPressed;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        keySprintPressed = !minecraft.options.keySprint.matches(keyCode, scanCode) && keySprintPressed;
        keyUpPressed = !minecraft.options.keyUp.matches(keyCode, scanCode) && keyUpPressed;
        keyDownPressed = !minecraft.options.keyDown.matches(keyCode, scanCode) && keyDownPressed;
        keyLeftPressed = !minecraft.options.keyLeft.matches(keyCode, scanCode) && keyLeftPressed;
        keyRightPressed = !minecraft.options.keyRight.matches(keyCode, scanCode) && keyRightPressed;

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        this.clearPopups();
        if (this.editingCoordinates) {
            this.coordinates.charTyped(codePoint, modifiers);
            boolean isGood = this.isAcceptable(this.coordinates.getValue());
            this.coordinates.setTextColor(isGood ? 0xFFFFFF : 0xFF0000);
            if (codePoint == '\r' && this.coordinates.isFocused() && isGood) {
                String[] xz = this.coordinates.getValue().split(",");
                this.centerAt(Integer.parseInt(xz[0].trim()), Integer.parseInt(xz[1].trim()));
                this.editingCoordinates = false;
                this.lastEditingCoordinates = false;
                this.switchToKeyboardInput();
            }
        }

        return super.charTyped(codePoint, modifiers);
    }

    private boolean isAcceptable(String input) {
        try {
            String[] xz = this.coordinates.getValue().split(",");
            Integer.valueOf(xz[0].trim());
            Integer.valueOf(xz[1].trim());
            return true;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException var3) {
            return false;
        }
    }

    private void switchToMouseInput() {
        this.timeOfLastKBInput = 0L;
        if (!this.mouseCursorShown) {
            GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), 208897, 212993);
        }

        this.mouseCursorShown = true;
    }

    private void switchToKeyboardInput() {
        this.timeOfLastKBInput = System.currentTimeMillis();
        this.mouseCursorShown = false;
        GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), 208897, 212995);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.pose().pushPose();
        this.zoomGoal = this.bindZoom(this.zoomGoal);
        if (this.mouseX != mouseX || this.mouseY != mouseY) {
            this.timeOfLastMouseInput = System.currentTimeMillis();
            this.switchToMouseInput();
        }

        this.mouseX = mouseX;
        this.mouseY = mouseY;
        float mouseDirectX = (float) minecraft.mouseHandler.xpos();
        float mouseDirectY = (float) minecraft.mouseHandler.ypos();
        if (this.zoom != this.zoomGoal) {
            float previousZoom = this.zoom;
            long timeSinceZoom = System.currentTimeMillis() - this.timeOfZoom;
            if (timeSinceZoom < 700.0F) {
                this.zoom = EasingUtils.easeOutExpo(this.zoomStart, this.zoomGoal, timeSinceZoom, 700.0F);
            } else {
                this.zoom = this.zoomGoal;
            }

            float scaledZoom = this.zoom;
            if (minecraft.getWindow().getWidth() > 1600) {
                scaledZoom = this.zoom * minecraft.getWindow().getWidth() / 1600.0F;
            }

            float zoomDelta = this.zoom / previousZoom;
            float zoomOffsetX = this.centerX * this.guiToDirectMouse - this.zoomDirectX;
            float zoomOffsetY = (this.top + this.centerY) * this.guiToDirectMouse - this.zoomDirectY;
            float zoomDeltaX = zoomOffsetX - zoomOffsetX * zoomDelta;
            float zoomDeltaY = zoomOffsetY - zoomOffsetY * zoomDelta;
            this.mapCenterX += zoomDeltaX / scaledZoom;
            this.mapCenterZ += zoomDeltaY / scaledZoom;
        }

        this.options.setZoom(this.zoomGoal);
        float scaledZoom = this.zoom;
        if (minecraft.getWindow().getScreenWidth() > 1600) {
            scaledZoom = this.zoom * minecraft.getWindow().getScreenWidth() / 1600.0F;
        }

        this.guiToMap = this.scScale / scaledZoom;
        this.mapToGui = 1.0F / this.scScale * scaledZoom;
        this.mouseDirectToMap = 1.0F / scaledZoom;
        this.guiToDirectMouse = this.scScale;
        this.renderBackground(guiGraphics);
        if (currentDragging) {
            if (!this.leftMouseButtonDown && this.overPopup(mouseX, mouseY)) {
                this.deltaX = 0.0F;
                this.deltaY = 0.0F;
                this.lastMouseX = mouseDirectX;
                this.lastMouseY = mouseDirectY;
                this.leftMouseButtonDown = true;
            } else if (this.leftMouseButtonDown) {
                this.deltaX = (this.lastMouseX - mouseDirectX) * this.mouseDirectToMap;
                this.deltaY = (this.lastMouseY - mouseDirectY) * this.mouseDirectToMap;
                this.lastMouseX = mouseDirectX;
                this.lastMouseY = mouseDirectY;
                this.deltaXonRelease = this.deltaX;
                this.deltaYonRelease = this.deltaY;
                this.timeOfRelease = System.currentTimeMillis();
            }
        } else {
            long timeSinceRelease = System.currentTimeMillis() - this.timeOfRelease;
            if (timeSinceRelease < 700.0F) {
                this.deltaX = EasingUtils.easeOutExpo(this.deltaXonRelease, 0.0F, timeSinceRelease, 700.0F);
                this.deltaY = EasingUtils.easeOutExpo(this.deltaYonRelease, 0.0F, timeSinceRelease, 700.0F);
            } else {
                this.deltaX = 0.0F;
                this.deltaY = 0.0F;
                this.deltaXonRelease = 0.0F;
                this.deltaYonRelease = 0.0F;
            }

            this.leftMouseButtonDown = false;
        }

        long timeSinceLastTick = System.currentTimeMillis() - this.timeAtLastTick;
        this.timeAtLastTick = System.currentTimeMillis();
        if (!this.editingCoordinates) {
            int kbDelta = 5;
            if (keySprintPressed) {
                kbDelta = 10;
            }

            if (keyUpPressed) {
                this.deltaY -= kbDelta / scaledZoom * timeSinceLastTick / 12.0F;
                this.switchToKeyboardInput();
            }

            if (keyDownPressed) {
                this.deltaY += kbDelta / scaledZoom * timeSinceLastTick / 12.0F;
                this.switchToKeyboardInput();
            }

            if (keyLeftPressed) {
                this.deltaX -= kbDelta / scaledZoom * timeSinceLastTick / 12.0F;
                this.switchToKeyboardInput();
            }

            if (keyRightPressed) {
                this.deltaX += kbDelta / scaledZoom * timeSinceLastTick / 12.0F;
                this.switchToKeyboardInput();
            }
        }

        this.mapCenterX += this.deltaX;
        this.mapCenterZ += this.deltaY;
        if (this.oldNorth) {
            this.options.setMapX((int) this.mapCenterZ);
            this.options.setMapZ(-((int) this.mapCenterX));
        } else {
            this.options.setMapX((int) this.mapCenterX);
            this.options.setMapZ((int) this.mapCenterZ);
        }

        this.centerX = this.getWidth() / 2;
        this.centerY = (this.bottom - this.top) / 2;
        int left;
        int right;
        int top;
        int bottom;
        if (this.oldNorth) {
            left = (int) Math.floor((this.mapCenterZ - this.centerY * this.guiToMap) / 256.0F);
            right = (int) Math.floor((this.mapCenterZ + this.centerY * this.guiToMap) / 256.0F);
            top = (int) Math.floor((-this.mapCenterX - this.centerX * this.guiToMap) / 256.0F);
            bottom = (int) Math.floor((-this.mapCenterX + this.centerX * this.guiToMap) / 256.0F);
        } else {
            left = (int) Math.floor((this.mapCenterX - this.centerX * this.guiToMap) / 256.0F);
            right = (int) Math.floor((this.mapCenterX + this.centerX * this.guiToMap) / 256.0F);
            top = (int) Math.floor((this.mapCenterZ - this.centerY * this.guiToMap) / 256.0F);
            bottom = (int) Math.floor((this.mapCenterZ + this.centerY * this.guiToMap) / 256.0F);
        }

        synchronized (this.closedLock) {
            if (this.closed) {
                return;
            }

            this.regions = this.persistentMap.getRegions(left - 1, right + 1, top - 1, bottom + 1);
        }

        guiGraphics.pose().translate(this.centerX - this.mapCenterX * this.mapToGui, (this.top + this.centerY) - this.mapCenterZ * this.mapToGui, 0.0f);
        if (this.oldNorth) {
            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0F));
        }

        float cursorCoordZ = 0.0f;
        float cursorCoordX = 0.0f;
        guiGraphics.pose().scale(this.mapToGui, this.mapToGui, 1.0f);
        if (MapDataManager.mapOptions.worldmapAllowed) {
            for (RegionCache region : this.regions) {
                ResourceLocation resource = region.getTextureLocation();
                if (resource != null) {
                    guiGraphics.blit(resource, region.getX() * 256, region.getZ() * 256, 0, 0, region.getWidth(), region.getWidth(), region.getWidth(), region.getWidth());
                }
            }

            if (MapDataManager.mapOptions.worldborder) {
                WorldBorder worldBorder = minecraft.level.getWorldBorder();
                float scale = 1.0f / (float) minecraft.getWindow().getGuiScale() / mapToGui;

                float x1 = (float) (worldBorder.getMinX());
                float z1 = (float) (worldBorder.getMinZ());
                float x2 = (float) (worldBorder.getMaxX());
                float z2 = (float) (worldBorder.getMaxZ());

                MapViewGuiGraphics.fillGradient(guiGraphics, x1 - scale, z1 - scale, x2 + scale, z1 + scale, 0xffff0000, 0xffff0000, 0xffff0000, 0xffff0000);
                MapViewGuiGraphics.fillGradient(guiGraphics, x1 - scale, z2 - scale, x2 + scale, z2 + scale, 0xffff0000, 0xffff0000, 0xffff0000, 0xffff0000);

                MapViewGuiGraphics.fillGradient(guiGraphics, x1 - scale, z1 - scale, x1 + scale, z2 + scale, 0xffff0000, 0xffff0000, 0xffff0000, 0xffff0000);
                MapViewGuiGraphics.fillGradient(guiGraphics, x2 - scale, z1 - scale, x2 + scale, z2 + scale, 0xffff0000, 0xffff0000, 0xffff0000, 0xffff0000);
            }

            float cursorX;
            float cursorY;
            if (this.mouseCursorShown) {
                cursorX = mouseDirectX;
                cursorY = mouseDirectY - this.top * this.guiToDirectMouse;
            } else {
                cursorX = (minecraft.getWindow().getWidth() / 2f);
                cursorY = (minecraft.getWindow().getHeight() - minecraft.getWindow().getHeight() / 2f) - this.top * this.guiToDirectMouse;
            }

            if (this.oldNorth) {
                cursorCoordX = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
                cursorCoordZ = -(cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
            } else {
                cursorCoordX = cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
                cursorCoordZ = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
            }

            if (this.oldNorth) {
                guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90.0F));
            }

            guiGraphics.pose().scale(this.guiToMap, this.guiToMap, 1.0f);
            guiGraphics.pose().translate(-(this.centerX - this.mapCenterX * this.mapToGui), -((this.top + this.centerY) - this.mapCenterZ * this.mapToGui), 0.0f);
            if (mapOptions.biomeOverlay != 0) {
                float biomeScaleX = this.mapPixelsX / 760.0F;
                float biomeScaleY = this.mapPixelsY / 360.0F;
                boolean still = !this.leftMouseButtonDown;
                still = still && this.zoom == this.zoomGoal;
                still = still && this.deltaX == 0.0F && this.deltaY == 0.0F;
                still = still && AsyncPersistenceManager.executorService.getActiveCount() == 0;
                if (still && !this.lastStill) {
                    int column;
                    if (this.oldNorth) {
                        column = (int) Math.floor(Math.floor(this.mapCenterZ - this.centerY * this.guiToMap) / 256.0) - (left - 1);
                    } else {
                        column = (int) Math.floor(Math.floor(this.mapCenterX - this.centerX * this.guiToMap) / 256.0) - (left - 1);
                    }

                    for (int x = 0; x < this.biomeMapData.getWidth(); ++x) {
                        for (int z = 0; z < this.biomeMapData.getHeight(); ++z) {
                            float floatMapX;
                            float floatMapZ;
                            if (this.oldNorth) {
                                floatMapX = z * biomeScaleY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
                                floatMapZ = -(x * biomeScaleX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
                            } else {
                                floatMapX = x * biomeScaleX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
                                floatMapZ = z * biomeScaleY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
                            }

                            int mapX = (int) Math.floor(floatMapX);
                            int mapZ = (int) Math.floor(floatMapZ);
                            int regionX = (int) Math.floor(mapX / 256.0F) - (left - 1);
                            int regionZ = (int) Math.floor(mapZ / 256.0F) - (top - 1);
                            if (!this.oldNorth && regionX != column || this.oldNorth && regionZ != column) {
                                this.persistentMap.compress();
                            }

                            column = !this.oldNorth ? regionX : regionZ;
                            RegionCache region = this.regions[regionZ * (right + 1 - (left - 1) + 1) + regionX];
                            Biome biome = null;
                            if (region.getMapData() != null && region.isLoaded() && !region.isEmpty()) {
                                int inRegionX = mapX - region.getX() * region.getWidth();
                                int inRegionZ = mapZ - region.getZ() * region.getWidth();
                                int height = region.getMapData().getHeight(inRegionX, inRegionZ);
                                int light = region.getMapData().getLight(inRegionX, inRegionZ);
                                if (height != Short.MIN_VALUE || light != 0) {
                                    biome = region.getMapData().getBiome(inRegionX, inRegionZ);
                                }
                            }

                            this.biomeMapData.setBiome(x, z, biome);
                        }
                    }

                    this.persistentMap.compress();
                    this.biomeMapData.segmentBiomes();
                    this.biomeMapData.findCenterOfSegments(true);
                }

                this.lastStill = still;
                boolean displayStill = !this.leftMouseButtonDown;
                displayStill = displayStill && this.zoom == this.zoomGoal;
                displayStill = displayStill && this.deltaX == 0.0F && this.deltaY == 0.0F;
                if (displayStill) {
                    int minimumSize = (int) (20.0F * this.scScale / biomeScaleX);
                    minimumSize *= minimumSize;
                    ArrayList<AbstractMapData.BiomeLabel> labels = this.biomeMapData.getBiomeLabels();
                    for (AbstractMapData.BiomeLabel biomeLabel : labels) {
                        if (biomeLabel.segmentSize > minimumSize) {
                            String label = biomeLabel.name; // + " (" + biomeLabel.x + "," + biomeLabel.z + ")";
                            int nameWidth = this.chkLen(label);
                            float x = biomeLabel.x * biomeScaleX / this.scScale;
                            float z = biomeLabel.z * biomeScaleY / this.scScale;

                            this.write(guiGraphics, label, x - (nameWidth / 2f), this.top + z - 3.0F, 0xFFFFFFFF);
                        }
                    }
                }
            }
        }
        guiGraphics.pose().popPose();

        // Render NPCs auf der Worldmap (gefiltert nach Typ und Status)
        renderNPCsOnWorldmap(guiGraphics, (int) this.mapCenterX, (int) this.mapCenterZ, this.zoom);

        // Render Navigations-Overlay auf der Worldmap
        renderNavigationOverlay(guiGraphics, (int) this.mapCenterX, (int) this.mapCenterZ, this.zoom);

        if (gotSkin) {
            float playerX = (float) MinecraftAccessor.xCoordDouble();
            float playerZ = (float) MinecraftAccessor.zCoordDouble();

            float width = iconsWidth * 0.75F;
            float height = iconsHeight * 0.75F;

            boolean hover = cursorCoordX >= playerX - width / 2 * guiToMap && cursorCoordX <= playerX + width / 2 * guiToMap && cursorCoordZ >= playerZ - height / 2 * guiToMap && cursorCoordZ <= playerZ + height / 2 * guiToMap;
            if (hover) {
                renderTooltip(guiGraphics, Component.literal("X: " + MinecraftAccessor.xCoord() + ", Y: " + MinecraftAccessor.yCoord() + ", Z: " + MinecraftAccessor.zCoord()), this.mouseX, this.mouseY);
            }

            int x = this.width / 2;
            int y = this.height / 2;

            double wayX = this.mapCenterX - (this.oldNorth ? -playerZ : playerX);
            double wayY = this.mapCenterZ - (this.oldNorth ? playerX : playerZ);
            float locate = (float) Math.atan2(wayX, wayY);
            float hypot = (float) Math.sqrt(wayX * wayX + wayY * wayY) * mapToGui;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, y, 0.0f);
            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-locate * Mth.RAD_TO_DEG));
            guiGraphics.pose().translate(0.0F, -hypot, 0.0f);
            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(locate * Mth.RAD_TO_DEG));
            guiGraphics.pose().translate(-x, -y, 0.0f);

            MapViewGuiGraphics.blitFloat(guiGraphics, MapViewPipelines.GUI_TEXTURED_LESS_OR_EQUAL_DEPTH_PIPELINE, lightmapSkinLocation, x - width / 2, y - height / 2, width, height, 0, 1, 0, 1, 0xFFFFFFFF);

            guiGraphics.pose().popPose();
        }

        if (System.currentTimeMillis() - this.timeOfLastKBInput < 2000L) {
            int scWidth = minecraft.getWindow().getGuiScaledWidth();
            int scHeight = minecraft.getWindow().getGuiScaledHeight();

            guiGraphics.blit(crosshairResource, scWidth / 2 - 8, scHeight / 2 - 8, 0, 0, 15, 15, 15, 15);
        } else {
            this.switchToMouseInput();
        }

        this.overlayBackground(guiGraphics, 0, this.top, 255, 255);
        this.overlayBackground(guiGraphics, this.bottom, this.getHeight(), 255, 255);
        if (MapDataManager.mapOptions.worldmapAllowed) {
            guiGraphics.drawCenteredString(this.font, this.screenTitle, this.getWidth() / 2, 16, 0xFFFFFFFF);
            int x = (int) Math.floor(cursorCoordX);
            int z = (int) Math.floor(cursorCoordZ);
            if (!this.editingCoordinates) {
                guiGraphics.drawString(this.font, "X: " + x, this.sideMargin, 16, 0xFFFFFFFF);
                guiGraphics.drawString(this.font, "Z: " + z, this.sideMargin + 64, 16, 0xFFFFFFFF);
            } else {
                this.coordinates.render(guiGraphics, mouseX, mouseY, delta);
            }
            guiGraphics.drawString(this.font, this.worldNameDisplay, this.getWidth() - this.sideMargin - this.worldNameDisplayLength, 16, 0xFFFFFF);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("worldmap.disabled"), this.sideMargin, 16, 0xFFFFFFFF);
        }
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // nothing
    }

    //private boolean isOnScreen(int x, int z) {
    //    int left;
    //    int right;
    //    int top;
    //    int bottom;
    //    if (this.oldNorth) {
    //        left = (int) Math.floor(this.mapCenterZ - (this.centerY * this.guiToMap) * 1.1);
    //        right = (int) Math.floor(this.mapCenterZ + (this.centerY * this.guiToMap) * 1.1);
    //        top = (int) Math.floor((-this.mapCenterX) - (this.centerX * this.guiToMap) * 1.1);
    //        bottom = (int) Math.floor((-this.mapCenterX) + (this.centerX * this.guiToMap) * 1.1);
    //    } else {
    //        left = (int) Math.floor(this.mapCenterX - (this.centerX * this.guiToMap) * 1.1);
    //        right = (int) Math.floor(this.mapCenterX + (this.centerX * this.guiToMap) * 1.1);
    //        top = (int) Math.floor(this.mapCenterZ - (this.centerY * this.guiToMap) * 1.1);
    //        bottom = (int) Math.floor(this.mapCenterZ + (this.centerY * this.guiToMap) * 1.1);
    //    }
    //
    //    return x > left && x < right && z > top && z < bottom;
    //}

    public void renderBackground(GuiGraphics drawContext) {
        drawContext.fill(0, 0, this.getWidth(), this.getHeight(), 0xff000000);
    }

    protected void overlayBackground(GuiGraphics guiGraphics, int startY, int endY, int startAlpha, int endAlpha) {
        int colorBase = 0x404040;
        int colorStart = (startAlpha << 24) | colorBase;
        int colorEnd = (endAlpha << 24) | colorBase;
        float renderedTextureSize = 32.0F;

        MapViewGuiGraphics.blitFloatGradient(guiGraphics, null, MapViewConstants.getOptionsBackgroundTexture(), 0, startY, this.getWidth(), endY, 0, this.width / renderedTextureSize, 0, endY / renderedTextureSize, colorStart, colorEnd);
    }

    @Override
    public void tick() {
    }

    @Override
    public void removed() {
        synchronized (this.closedLock) {
            this.closed = true;
            this.persistentMap.getRegions(0, -1, 0, -1);
            this.regions = new RegionCache[0];
        }
    }

    private void createPopup(int x, int y, int directX, int directY) {
        ArrayList<PopupComponent.PopupEntry> entries = new ArrayList<>();
        float cursorX = directX;
        float cursorY = directY - this.top * this.guiToDirectMouse;
        float cursorCoordX;
        float cursorCoordZ;
        if (this.oldNorth) {
            cursorCoordX = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
            cursorCoordZ = -(cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
        } else {
            cursorCoordX = cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
            cursorCoordZ = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
        }

        PopupComponent.PopupEntry entry;
        entry = new PopupComponent.PopupEntry(I18n.get("mapview.waypoints.teleportTo"), 3, true, true);
        entries.add(entry);

        // Navigation Option
        PopupComponent.PopupEntry navEntry = new PopupComponent.PopupEntry("Hierhin navigieren", 4, true, true);
        entries.add(navEntry);

        // Navigation stoppen (wenn aktiv)
        if (RoadNavigationService.getInstance() != null && RoadNavigationService.getInstance().isNavigationActive()) {
            PopupComponent.PopupEntry stopEntry = new PopupComponent.PopupEntry("Navigation beenden", 5, true, true);
            entries.add(stopEntry);
        }

        // World-Koordinaten bei Popup-Erstellung speichern (wichtig für korrekte Navigation!)
        int worldX = (int) Math.floor(cursorCoordX);
        int worldZ = (int) Math.floor(cursorCoordZ);

        // Debug-Logging für Koordinaten
        MapViewConstants.getLogger().info("[WorldMap] Popup created at:");
        MapViewConstants.getLogger().info("  directX={}, directY={}", directX, directY);
        MapViewConstants.getLogger().info("  cursorX={}, cursorY={}", cursorX, cursorY);
        MapViewConstants.getLogger().info("  mouseDirectToMap={}, guiToMap={}", this.mouseDirectToMap, this.guiToMap);
        MapViewConstants.getLogger().info("  mapCenterX={}, mapCenterZ={}", this.mapCenterX, this.mapCenterZ);
        MapViewConstants.getLogger().info("  centerX={}, centerY={}, top={}", this.centerX, this.centerY, this.top);
        MapViewConstants.getLogger().info("  cursorCoordX={}, cursorCoordZ={}", cursorCoordX, cursorCoordZ);
        MapViewConstants.getLogger().info("  worldX={}, worldZ={}", worldX, worldZ);
        MapViewConstants.getLogger().info("  Player pos: x={}, z={}", MinecraftAccessor.xCoord(), MinecraftAccessor.zCoord());

        this.createPopup(x, y, directX, directY, worldX, worldZ, 60, entries);
        if (MapViewConstants.DEBUG) {
            persistentMap.debugLog(worldX, worldZ);
        }
    }

    @Override
    public void popupAction(PopupComponent popup, int action) {
        // Gespeicherte World-Koordinaten verwenden (wurden bei Popup-Erstellung berechnet)
        int x = popup.getClickedWorldX();
        int z = popup.getClickedWorldZ();
        int y = this.persistentMap.getHeightAt(x, z);
        switch (action) {
            case 3 -> {
                if (y < MapViewConstants.getPlayer().level().getMinBuildHeight()) {
                    y = (!(MapViewConstants.getPlayer().level().dimensionType().hasCeiling()) ? MapViewConstants.getPlayer().level().getMaxBuildHeight() : 64);
                }
                MapViewConstants.playerRunTeleportCommand(x, y, z);
            }
            case 4 -> {
                // Navigation starten
                MapViewConstants.getLogger().info("[Navigation] Target coordinates from popup: x={}, z={}", x, z);
                BlockPos targetPos = new BlockPos(x, y, z);
                NavigationTarget target = NavigationTarget.atPosition(targetPos, "Kartenziel (" + x + ", " + z + ")");
                // Initialisiere Service falls nötig
                RoadNavigationService navService = RoadNavigationService.getInstance();
                if (navService == null) {
                    navService = RoadNavigationService.getInstance(this.persistentMap);
                }
                navService.startNavigation(target);
            }
            case 5 -> {
                // Navigation beenden
                if (RoadNavigationService.getInstance() != null) {
                    RoadNavigationService.getInstance().stopNavigation();
                }
            }
            default -> MapViewConstants.getLogger().warn("unimplemented command");
        }

    }

    public boolean isEditing() {
        return false;
    }

    public void accept(boolean b) {
        minecraft.setScreen(this);
    }

    private int chkLen(String string) {
        return this.font.width(string);
    }

    private void write(GuiGraphics drawContext, String string, float x, float y, int color) {
        drawContext.drawString(this.font, string, (int) x, (int) y, color);
    }

    /**
     * Rendert NPCs auf der Worldmap
     * Filtert automatisch Polizei-NPCs und NPCs auf Arbeit/Zuhause
     */
    private void renderNPCsOnWorldmap(GuiGraphics graphics, int centerX, int centerZ, float zoom) {
        // Berechne Offset für Kartenanzeige
        int offsetX = (int) ((this.width / 2.0f) - centerX / zoom);
        int offsetY = (int) ((this.height / 2.0f) - centerZ / zoom);

        npcMapRenderer.renderOnWorldmap(graphics, centerX, centerZ,
                this.width, this.height, zoom, offsetX, offsetY);

        // Render Tooltip bei Hover
        npcMapRenderer.renderNPCTooltip(graphics, this.mouseX, this.mouseY,
                centerX, centerZ, zoom, this.width, this.height);
    }

    /**
     * Rendert das Navigations-Overlay auf der Worldmap
     * Zeigt Pfadlinie, Zielmarker und Distanzanzeige
     */
    private void renderNavigationOverlay(GuiGraphics graphics, int centerX, int centerZ, float zoom) {
        NavigationOverlay overlay = NavigationOverlay.getInstance();

        // Initialisiere falls nötig
        if (!overlay.isInitialized()) {
            overlay.initialize(this.persistentMap);
        }

        if (!overlay.isInitialized() || !overlay.isNavigating()) {
            return;
        }

        // Tick für Updates (Position, Pfad-Neuberechnung)
        overlay.tick();

        // Berechne Bildschirm-Zentrum
        int screenCenterX = this.width / 2;
        int screenCenterY = this.top + (this.bottom - this.top) / 2;

        // Render auf Fullscreen-Worldmap mit korrekter Skalierung
        // mapToGui konvertiert Weltkoordinaten zu Bildschirmkoordinaten
        overlay.renderFullscreenAccurate(graphics, centerX, centerZ,
                screenCenterX, screenCenterY, this.mapToGui);
    }
}
