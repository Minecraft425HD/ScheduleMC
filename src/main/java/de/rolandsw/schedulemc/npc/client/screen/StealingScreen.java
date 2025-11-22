package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.StealingMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.StealingAttemptPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

/**
 * GUI für Bestehlen von NPCs
 * - Spacebar-Klick Minigame
 * - Schwierigkeit basierend auf NPC Inventarwert
 */
@OnlyIn(Dist.CLIENT)
public class StealingScreen extends AbstractContainerScreen<StealingMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/stealing.png");

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

    public StealingScreen(StealingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 120;
    }

    @Override
    protected void init() {
        super.init();
        startGame();
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
            targetPosition = 0.3f + (float)(Math.random() * 0.4f);
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

        // Minigame Bar
        if (gameActive) {
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
            guiGraphics.drawString(this.font, "§cBestehle: " + npc.getNpcName(), 8, 6, 0x404040, false);
        }

        guiGraphics.drawString(this.font, "§7Geschätzter Wert: §a" + String.format("%.0f€", inventoryValue), 8, 20, 0x404040, false);
        guiGraphics.drawString(this.font, "§7Versuche: §e" + attempts + "/" + maxAttempts, 8, 32, 0x404040, false);

        if (gameActive) {
            guiGraphics.drawString(this.font, "§eDrücke LEERTASTE im grünen Bereich!", 8, 48, 0x404040, false);
        } else {
            guiGraphics.drawString(this.font, "§cFehlgeschlagen! Zu viele Versuche.", 8, 48, 0x404040, false);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (gameActive) {
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
            // Erfolg! Sende Packet an Server
            NPCNetworkHandler.sendToServer(new StealingAttemptPacket(
                menu.getEntityId(),
                true
            ));

            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.literal("§a✓ Erfolgreich gestohlen!"), true);
            }

            this.onClose();
        } else {
            // Fehlgeschlagen
            if (attempts >= maxAttempts) {
                // Zu viele Versuche - sende Fehlschlag an Server
                NPCNetworkHandler.sendToServer(new StealingAttemptPacket(
                    menu.getEntityId(),
                    false
                ));

                gameActive = false;

                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.literal("§c✗ Diebstahl fehlgeschlagen!"), true);
                }

                // Schließe GUI nach 2 Sekunden
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        if (minecraft != null) {
                            minecraft.execute(this::onClose);
                        }
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }).start();
            } else {
                // Noch ein Versuch
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.literal("§e⚠ Versuche es nochmal!"), true);
                }
            }
        }
    }
}
