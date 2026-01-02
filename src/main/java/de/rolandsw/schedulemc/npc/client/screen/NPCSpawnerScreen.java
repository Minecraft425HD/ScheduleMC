package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.client.ClientNPCNameCache;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.BankCategory;
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
    private Button refreshSkinsButton;
    private Button spawnButton;

    // NPC Typ Auswahl
    private int selectedNPCTypeIndex = 0;
    private Button prevNPCTypeButton;
    private Button nextNPCTypeButton;

    // Verkäufer Kategorie Auswahl
    private int selectedMerchantCategoryIndex = 0;
    private Button prevMerchantCategoryButton;
    private Button nextMerchantCategoryButton;

    // Bank Kategorie Auswahl
    private int selectedBankCategoryIndex = 0;
    private Button prevBankCategoryButton;
    private Button nextBankCategoryButton;

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
        npcNameInput.setResponder(this::onNameChanged);
        addRenderableWidget(npcNameInput);

        // Skin Selection Buttons
        prevSkinButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            selectedSkinIndex = (selectedSkinIndex - 1 + availableSkins.size()) % Math.max(1, availableSkins.size());
        }).bounds(x + 20, y + 50, 20, 20).build());

        nextSkinButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            selectedSkinIndex = (selectedSkinIndex + 1) % Math.max(1, availableSkins.size());
        }).bounds(x + 136, y + 50, 20, 20).build());

        // Refresh Skins Button (Reload button to refresh the skin list)
        refreshSkinsButton = addRenderableWidget(Button.builder(Component.literal("⟳"), button -> {
            refreshSkinList();
        }).bounds(x + 42, y + 50, 20, 20).build());

        // NPC Typ Selection Buttons
        prevNPCTypeButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            selectedNPCTypeIndex = (selectedNPCTypeIndex - 1 + NPCType.values().length) % NPCType.values().length;
            updateCategoryVisibility();
        }).bounds(x + 20, y + 80, 20, 20).build());

        nextNPCTypeButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            selectedNPCTypeIndex = (selectedNPCTypeIndex + 1) % NPCType.values().length;
            updateCategoryVisibility();
        }).bounds(x + 136, y + 80, 20, 20).build());

        // Verkäufer Kategorie Selection Buttons
        prevMerchantCategoryButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            selectedMerchantCategoryIndex = (selectedMerchantCategoryIndex - 1 + MerchantCategory.values().length) % MerchantCategory.values().length;
        }).bounds(x + 20, y + 110, 20, 20).build());

        nextMerchantCategoryButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            selectedMerchantCategoryIndex = (selectedMerchantCategoryIndex + 1) % MerchantCategory.values().length;
        }).bounds(x + 136, y + 110, 20, 20).build());

        // Bank Kategorie Selection Buttons
        prevBankCategoryButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            selectedBankCategoryIndex = (selectedBankCategoryIndex - 1 + BankCategory.values().length) % BankCategory.values().length;
        }).bounds(x + 20, y + 110, 20, 20).build());

        nextBankCategoryButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            selectedBankCategoryIndex = (selectedBankCategoryIndex + 1) % BankCategory.values().length;
        }).bounds(x + 136, y + 110, 20, 20).build());

        // Spawn Button
        spawnButton = addRenderableWidget(Button.builder(Component.literal("NPC Spawnen"), button -> {
            spawnNPC();
        }).bounds(x + 38, y + 140, 100, 20).build());

        // Initial visibility update
        updateCategoryVisibility();
    }

    /**
     * Callback wenn der NPC-Name geändert wird
     */
    private void onNameChanged(String newName) {
        // Update Spawn-Button Aktivierung
        boolean nameTaken = ClientNPCNameCache.isNameTaken(newName);
        spawnButton.active = !nameTaken && !newName.trim().isEmpty();
    }

    /**
     * Aktualisiert die Sichtbarkeit der Kategorie-Buttons (Verkäufer/Bank)
     */
    private void updateCategoryVisibility() {
        NPCType currentType = NPCType.values()[selectedNPCTypeIndex];
        boolean isVerkaufer = currentType == NPCType.VERKAEUFER;
        boolean isBank = currentType == NPCType.BANK;

        prevMerchantCategoryButton.visible = isVerkaufer;
        nextMerchantCategoryButton.visible = isVerkaufer;

        prevBankCategoryButton.visible = isBank;
        nextBankCategoryButton.visible = isBank;
    }

    /**
     * Lädt die Skin-Liste neu (z.B. nachdem neue Skins hinzugefügt wurden)
     */
    private void refreshSkinList() {
        ScheduleMC.LOGGER.info("Refreshing NPC skin list...");
        availableSkins = loadAvailableSkins();

        // Passe den ausgewählten Index an, falls er außerhalb des Bereichs liegt
        if (selectedSkinIndex >= availableSkins.size()) {
            selectedSkinIndex = Math.max(0, availableSkins.size() - 1);
        }

        ScheduleMC.LOGGER.info("Skin list refreshed. Currently selected: {} ({}/{})",
            availableSkins.get(selectedSkinIndex), selectedSkinIndex + 1, availableSkins.size());
    }

    /**
     * Lädt alle verfügbaren Skins aus dem assets/schedulemc/skins/ Ordner
     * und fügt Standard-Minecraft-Skins hinzu
     */
    private List<String> loadAvailableSkins() {
        List<String> skins = new ArrayList<>();

        // Füge Standard-Minecraft-Player-Skins hinzu
        skins.add("steve");
        skins.add("alex");
        skins.add("default.png");

        try {
            // Lade Skins aus dem Ressourcen-Ordner
            // In der Entwicklung: src/main/resources/assets/schedulemc/skins/
            // Im Build: assets/schedulemc/skins/ im JAR
            var resourceManager = Minecraft.getInstance().getResourceManager();
            var namespace = ScheduleMC.MOD_ID;

            ScheduleMC.LOGGER.info("Loading custom NPC skins from assets/{}/skins/", namespace);

            // Liste alle Ressourcen im skins Ordner auf
            resourceManager.listResources("skins", location ->
                location.getNamespace().equals(namespace) && location.getPath().endsWith(".png")
            ).forEach((location, resource) -> {
                try {
                    // Extrahiere den Dateinamen aus dem Pfad
                    String path = location.getPath();
                    String fileName = path.substring(path.lastIndexOf('/') + 1);

                    ScheduleMC.LOGGER.info("Found skin file: {}", fileName);

                    // Füge nur hinzu, wenn nicht schon in der Liste
                    if (!skins.contains(fileName)) {
                        skins.add(fileName);
                        ScheduleMC.LOGGER.info("Added custom skin: {}", fileName);
                    }
                } catch (Exception e) {
                    ScheduleMC.LOGGER.error("Error processing skin: {}", location, e);
                }
            });

            ScheduleMC.LOGGER.info("Total skins available: {} ({})", skins.size(), String.join(", ", skins));
        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Failed to load NPC skins", e);
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
        BankCategory bankCategory = BankCategory.values()[selectedBankCategoryIndex];

        NPCNetworkHandler.sendToServer(new SpawnNPCPacket(
            menu.getSpawnPosition(),
            npcName,
            skinFile,
            npcType,
            merchantCategory,
            bankCategory
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

        // Zeige Warnung wenn Name bereits existiert
        String currentName = npcNameInput.getValue();
        if (ClientNPCNameCache.isNameTaken(currentName)) {
            npcNameInput.setTextColor(0xFF5555); // Rot
            // Zeige Fehlermeldung unter dem Name-Input
            String errorMsg = "Name bereits vergeben!";
            int errorX = x + 38;
            int errorY = y + 46;
            guiGraphics.drawString(this.font, errorMsg, errorX, errorY, 0xFF5555, false);
        } else {
            npcNameInput.setTextColor(0xE0E0E0); // Normal (hell-grau)
        }

        // Zeige ausgewählten Skin-Namen mit Zähler
        if (!availableSkins.isEmpty()) {
            String skinName = availableSkins.get(selectedSkinIndex);
            // Zeige schöneren Namen für Standard-Skins
            String displayName = skinName;
            if (skinName.equals("steve")) {
                displayName = "Steve (Standard)";
            } else if (skinName.equals("alex")) {
                displayName = "Alex (Standard)";
            } else if (skinName.equals("default.png")) {
                displayName = "Default";
            } else {
                // Entferne .png Endung für bessere Anzeige
                displayName = skinName.replace(".png", "");
            }

            String skinCounter = "(" + (selectedSkinIndex + 1) + "/" + availableSkins.size() + ")";
            int textX = x + (imageWidth - font.width(displayName)) / 2;
            int textY = y + 55;
            int counterX = x + (imageWidth - font.width(skinCounter)) / 2;
            int counterY = y + 65;

            guiGraphics.drawString(this.font, displayName, textX, textY, 0x404040, false);
            guiGraphics.drawString(this.font, skinCounter, counterX, counterY, 0x808080, false);
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

        // Zeige ausgewählte Bank-Kategorie (nur wenn Bank)
        if (currentType == NPCType.BANK) {
            BankCategory currentCategory = BankCategory.values()[selectedBankCategoryIndex];
            String categoryName = currentCategory.getDisplayName();
            int categoryTextX = x + (imageWidth - font.width(categoryName)) / 2;
            int categoryTextY = y + 115;
            guiGraphics.drawString(this.font, categoryName, categoryTextX, categoryTextY, 0x404040, false);
        }
    }    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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

        // Bank-Kategorie Label nur anzeigen wenn Bank ausgewählt
        if (currentType == NPCType.BANK) {
            guiGraphics.drawString(this.font, "Kategorie:", 8, 103, 0x404040, false);
        }
    }
}
