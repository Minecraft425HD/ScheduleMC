package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.StealingMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.StealingAttemptPacket;
import de.rolandsw.schedulemc.util.ThreadPoolManager;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.concurrent.ThreadLocalRandom;
import org.lwjgl.glfw.GLFW;

/**
 * GUI für Bestehlen von NPCs
 * - Spacebar-Klick Minigame
 * - Schwierigkeit basierend auf NPC Inventarwert
 */
@OnlyIn(Dist.CLIENT)
public class StealingScreen extends AbstractContainerScreen<StealingMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/gui/stealing.png");

    // Auswahl-Modus
    private boolean choosingMode = true; // true = Auswahl, false = Minigame
    private int stealType = 0; // 0 = Geld, 1 = Items

    // Minigame-Variablen
    private boolean gameActive = false;
    private long gameStartTime = 0;
    private float targetPosition = 0.5f; // 0.0 - 1.0
    private float targetZoneSize = 0.2f; // Größe der "Erfolgs-Zone"
    private float currentPosition = 0.0f;
    private float speed = 0.005f; // Geschwindigkeit des Indikators
    private boolean movingRight = true;

    private double inventoryValue = 0.0;
    private int attempts = 0;
    private int maxAttempts = 3;

    // Buttons
    private Button moneyButton;
    private Button itemsButton;

    public StealingScreen(StealingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 120;
    }

    @Override
    protected void init() {
        super.init();

        // Erstelle Auswahl-Buttons
        int buttonWidth = 70;
        int buttonHeight = 20;
        int centerX = (width - imageWidth) / 2 + imageWidth / 2;
        int centerY = (height - imageHeight) / 2 + imageHeight / 2;

        moneyButton = Button.builder(Component.translatable("screen.stealing.button_money"), button -> {
            stealType = 0;
            choosingMode = false;
            startGame();
            updateButtons();
        }).bounds(centerX - buttonWidth - 5, centerY - buttonHeight / 2, buttonWidth, buttonHeight).build();

        itemsButton = Button.builder(Component.translatable("screen.stealing.button_items"), button -> {
            stealType = 1;
            choosingMode = false;
            startGame();
            updateButtons();
        }).bounds(centerX + 5, centerY - buttonHeight / 2, buttonWidth, buttonHeight).build();

        addRenderableWidget(moneyButton);
        addRenderableWidget(itemsButton);

        updateButtons();
    }

    /**
     * Aktualisiert Button-Sichtbarkeit
     */
    private void updateButtons() {
        moneyButton.visible = choosingMode;
        itemsButton.visible = choosingMode;
    }

    /**
     * Startet das Minigame
     */
    private void startGame() {
        gameActive = true;
        gameStartTime = System.currentTimeMillis();
        currentPosition = 0.0f;
        movingRight = true;
        attempts = 0;

        // Lade Config-Werte
        maxAttempts = ModConfigHandler.COMMON.STEALING_MAX_ATTEMPTS.get();
        speed = ModConfigHandler.COMMON.STEALING_INDICATOR_SPEED.get().floatValue();

        float minZone = ModConfigHandler.COMMON.STEALING_MIN_ZONE_SIZE.get().floatValue();
        float maxZone = ModConfigHandler.COMMON.STEALING_MAX_ZONE_SIZE.get().floatValue();

        // Berechne Inventarwert des NPCs
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null) {
            inventoryValue = calculateInventoryValue(npc);

            // Schwierigkeit basierend auf Inventarwert (logarithmische Skalierung)
            // Je wertvoller, desto kleiner die Zone und schneller der Indikator
            if (inventoryValue <= 0) {
                targetZoneSize = maxZone; // Einfach (wenn NPC nichts hat)
            } else if (inventoryValue < 50) {
                targetZoneSize = maxZone; // Sehr einfach
            } else if (inventoryValue < 200) {
                targetZoneSize = maxZone * 0.8f; // Einfach
            } else if (inventoryValue < 500) {
                targetZoneSize = (minZone + maxZone) / 2; // Mittel
            } else if (inventoryValue < 1000) {
                targetZoneSize = minZone * 1.5f; // Schwer
            } else {
                targetZoneSize = minZone; // Sehr schwer
            }

            // Zufällige Target-Position
            targetPosition = 0.3f + (float)(ThreadLocalRandom.current().nextDouble() * 0.4f);
        }
    }

    /**
     * Berechnet den Wert des NPC Inventars
     */
    private double calculateInventoryValue(CustomNPCEntity npc) {
        double value = 0.0;

        // Geld des NPCs (aus Menu - vom Server gesendet)
        value += menu.getNpcWalletAmount();

        // Items im NPC Inventar (Hotbar)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = npc.getNpcData().getInventory().get(i);
            if (!stack.isEmpty()) {
                // Schätze Item-Wert (vereinfacht: 10€ pro Item)
                value += stack.getCount() * 10;
            }
        }

        return value;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Hintergrund (fallback wenn Texture fehlt)
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF8B8B8B);

        // Minigame Bar (nur wenn nicht in Auswahl-Modus)
        if (!choosingMode && gameActive) {
            int barX = x + 18;
            int barY = y + 60;
            int barWidth = 140;
            int barHeight = 20;

            // Bar Hintergrund
            guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF404040);

            // Target Zone (grün)
            int targetStart = (int)(targetPosition * barWidth - (targetZoneSize * barWidth / 2));
            int targetEnd = (int)(targetPosition * barWidth + (targetZoneSize * barWidth / 2));
            guiGraphics.fill(barX + targetStart, barY, barX + targetEnd, barY + barHeight, 0xFF00FF00);

            // Aktueller Indikator (rot)
            int indicatorPos = (int)(currentPosition * barWidth);
            guiGraphics.fill(barX + indicatorPos - 2, barY - 5, barX + indicatorPos + 2, barY + barHeight + 5, 0xFFFF0000);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null) {
            String title = Component.translatable("screen.stealing.title_prefix").getString() + npc.getNpcName();
            guiGraphics.drawString(this.font, title, 8, 6, 0x404040, false);
        }

        if (choosingMode) {
            // Auswahl-Modus
            guiGraphics.drawString(this.font, Component.translatable("screen.stealing.prompt").getString(), 8, 20, 0x404040, false);
        } else {
            // Minigame-Modus
            String valueLabel = Component.translatable("screen.stealing.estimated_value").getString() + String.format("%.0f€", inventoryValue);
            guiGraphics.drawString(this.font, valueLabel, 8, 20, 0x404040, false);

            String attemptsLabel = Component.translatable("screen.stealing.attempts").getString() + attempts + "/" + maxAttempts;
            guiGraphics.drawString(this.font, attemptsLabel, 8, 32, 0x404040, false);

            if (gameActive) {
                guiGraphics.drawString(this.font, Component.translatable("screen.stealing.instruction").getString(), 8, 48, 0x404040, false);
            } else {
                guiGraphics.drawString(this.font, Component.translatable("screen.stealing.failed").getString(), 8, 48, 0x404040, false);
            }
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (!choosingMode && gameActive) {
            // Bewege Indikator
            if (movingRight) {
                currentPosition += speed;
                if (currentPosition >= 1.0f) {
                    currentPosition = 1.0f;
                    movingRight = false;
                }
            } else {
                currentPosition -= speed;
                if (currentPosition <= 0.0f) {
                    currentPosition = 0.0f;
                    movingRight = true;
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Spacebar gedrückt
        if (keyCode == GLFW.GLFW_KEY_SPACE && gameActive) {
            checkStealingAttempt();
            return true;
        }

        // ESC zum Schließen
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Prüft ob der Spieler erfolgreich gestohlen hat
     */
    private void checkStealingAttempt() {
        attempts++;

        // Prüfe ob Indikator in der Target-Zone ist
        float targetMin = targetPosition - (targetZoneSize / 2);
        float targetMax = targetPosition + (targetZoneSize / 2);

        boolean success = currentPosition >= targetMin && currentPosition <= targetMax;

        if (success) {
            // Erfolg! Sende Packet an Server mit stealType
            NPCNetworkHandler.sendToServer(new StealingAttemptPacket(
                menu.getEntityId(),
                true,
                stealType
            ));

            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.translatable("message.stealing.success"), true);
            }

            this.onClose();
        } else {
            // Fehlgeschlagen
            if (attempts >= maxAttempts) {
                // Zu viele Versuche - sende Fehlschlag an Server
                NPCNetworkHandler.sendToServer(new StealingAttemptPacket(
                    menu.getEntityId(),
                    false,
                    stealType
                ));

                gameActive = false;

                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.translatable("message.stealing.failed"), true);
                }

                // THREAD-SAFETY: Use ThreadPoolManager for delayed task
                // Schließe GUI nach 2 Sekunden
                ThreadPoolManager.schedule(() -> {
                    if (minecraft != null) {
                        minecraft.execute(this::onClose);
                    }
                }, 2000L, TimeUnit.MILLISECONDS);
            } else {
                // Noch ein Versuch
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.translatable("screen.stealing.try_again"), true);
                }
            }
        }
    }
}
