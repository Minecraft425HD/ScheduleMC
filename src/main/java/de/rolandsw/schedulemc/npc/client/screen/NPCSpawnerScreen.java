package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.menu.NPCSpawnerMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.SpawnNPCPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI für NPC Spawner Tool
 * - Auswahl des NPC-Skins
 * - Eingabe des NPC-Namens
 * - Spawn-Button
 */
@OnlyIn(Dist.CLIENT)
public class NPCSpawnerScreen extends AbstractContainerScreen<NPCSpawnerMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/npc_spawner.png");

    private EditBox npcNameInput;
    private List<String> availableSkins;
    private int selectedSkinIndex = 0;
    private Button prevSkinButton;
    private Button nextSkinButton;
    private Button spawnButton;

    // NPC Typ Auswahl
    private int selectedNPCTypeIndex = 0;
    private Button prevNPCTypeButton;
    private Button nextNPCTypeButton;

    // Verkäufer Kategorie Auswahl
    private int selectedMerchantCategoryIndex = 0;
    private Button prevMerchantCategoryButton;
    private Button nextMerchantCategoryButton;

    public NPCSpawnerScreen(NPCSpawnerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 140; // Ohne Player-Inventar
        this.availableSkins = loadAvailableSkins();
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // NPC Name Input
        npcNameInput = new EditBox(this.font, x + 38, y + 25, 100, 20, Component.literal("NPC Name"));
        npcNameInput.setMaxLength(32);
        npcNameInput.setValue("NPC");
        addRenderableWidget(npcNameInput);

        // Skin Selection Buttons
        prevSkinButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            selectedSkinIndex = (selectedSkinIndex - 1 + availableSkins.size()) % Math.max(1, availableSkins.size());
        }).bounds(x + 20, y + 50, 20, 20).build());

        nextSkinButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            selectedSkinIndex = (selectedSkinIndex + 1) % Math.max(1, availableSkins.size());
        }).bounds(x + 136, y + 50, 20, 20).build());

        // NPC Typ Selection Buttons
        prevNPCTypeButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            selectedNPCTypeIndex = (selectedNPCTypeIndex - 1 + NPCType.values().length) % NPCType.values().length;
            updateMerchantCategoryVisibility();
        }).bounds(x + 20, y + 80, 20, 20).build());

        nextNPCTypeButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            selectedNPCTypeIndex = (selectedNPCTypeIndex + 1) % NPCType.values().length;
            updateMerchantCategoryVisibility();
        }).bounds(x + 136, y + 80, 20, 20).build());

        // Verkäufer Kategorie Selection Buttons
        prevMerchantCategoryButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            selectedMerchantCategoryIndex = (selectedMerchantCategoryIndex - 1 + MerchantCategory.values().length) % MerchantCategory.values().length;
        }).bounds(x + 20, y + 110, 20, 20).build());

        nextMerchantCategoryButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            selectedMerchantCategoryIndex = (selectedMerchantCategoryIndex + 1) % MerchantCategory.values().length;
        }).bounds(x + 136, y + 110, 20, 20).build());

        // Spawn Button
        spawnButton = addRenderableWidget(Button.builder(Component.literal("NPC Spawnen"), button -> {
            spawnNPC();
        }).bounds(x + 38, y + 140, 100, 20).build());

        // Initial visibility update
        updateMerchantCategoryVisibility();
    }

    /**
     * Aktualisiert die Sichtbarkeit der Verkäufer-Kategorie Buttons
     */
    private void updateMerchantCategoryVisibility() {
        NPCType currentType = NPCType.values()[selectedNPCTypeIndex];
        boolean isVerkaufer = currentType == NPCType.VERKAEUFER;

        prevMerchantCategoryButton.visible = isVerkaufer;
        nextMerchantCategoryButton.visible = isVerkaufer;
    }

    /**
     * Lädt alle verfügbaren Skins aus dem config/schedulemc/npc_skins/ Ordner
     */
    private List<String> loadAvailableSkins() {
        List<String> skins = new ArrayList<>();

        try {
            Path skinsDir = Paths.get("config", ScheduleMC.MOD_ID, "npc_skins");
            File dir = skinsDir.toFile();

            if (!dir.exists()) {
                dir.mkdirs();
                ScheduleMC.LOGGER.info("Created NPC skins directory: " + skinsDir);
            }

            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    skins.add(file.getName());
                }
            }
        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Failed to load NPC skins", e);
        }

        if (skins.isEmpty()) {
            skins.add("default.png");
        }

        return skins;
    }

    /**
     * Sendet Spawn-Packet an Server
     */
    private void spawnNPC() {
        String npcName = npcNameInput.getValue();
        String skinFile = availableSkins.get(selectedSkinIndex);
        NPCType npcType = NPCType.values()[selectedNPCTypeIndex];
        MerchantCategory merchantCategory = MerchantCategory.values()[selectedMerchantCategoryIndex];

        NPCNetworkHandler.sendToServer(new SpawnNPCPacket(
            menu.getSpawnPosition(),
            npcName,
            skinFile,
            npcType,
            merchantCategory
        ));

        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Zeige ausgewählten Skin-Namen
        if (!availableSkins.isEmpty()) {
            String skinName = availableSkins.get(selectedSkinIndex);
            int textX = x + (imageWidth - font.width(skinName)) / 2;
            int textY = y + 55;
            guiGraphics.drawString(this.font, skinName, textX, textY, 0x404040, false);
        }

        // Zeige ausgewählten NPC Typ
        NPCType currentType = NPCType.values()[selectedNPCTypeIndex];
        String typeName = currentType.getDisplayName();
        int typeTextX = x + (imageWidth - font.width(typeName)) / 2;
        int typeTextY = y + 85;
        guiGraphics.drawString(this.font, typeName, typeTextX, typeTextY, 0x404040, false);

        // Zeige ausgewählte Verkäufer-Kategorie (nur wenn Verkäufer)
        if (currentType == NPCType.VERKAEUFER) {
            MerchantCategory currentCategory = MerchantCategory.values()[selectedMerchantCategoryIndex];
            String categoryName = currentCategory.getDisplayName();
            int categoryTextX = x + (imageWidth - font.width(categoryName)) / 2;
            int categoryTextY = y + 115;
            guiGraphics.drawString(this.font, categoryName, categoryTextX, categoryTextY, 0x404040, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, "Name:", 8, 18, 0x404040, false);
        guiGraphics.drawString(this.font, "Skin:", 8, 43, 0x404040, false);
        guiGraphics.drawString(this.font, "Typ:", 8, 73, 0x404040, false);

        // Verkäufer-Kategorie Label nur anzeigen wenn Verkäufer ausgewählt
        NPCType currentType = NPCType.values()[selectedNPCTypeIndex];
        if (currentType == NPCType.VERKAEUFER) {
            guiGraphics.drawString(this.font, "Kategorie:", 8, 103, 0x404040, false);
        }
    }
}
