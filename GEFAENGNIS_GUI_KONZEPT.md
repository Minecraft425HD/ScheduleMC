# Gefängnis-GUI System - Nicht schließbar

## Konzept

Das Gefängnis-GUI:
- Öffnet sich **automatisch** bei Inhaftierung
- Kann **NICHT geschlossen werden** (ESC, X, Keybinds blockiert)
- Schließt sich **NUR** wenn:
  - Haftzeit abgelaufen
  - Kaution erfolgreich gezahlt

---

## GUI Design

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                     ⛓ GEFÄNGNIS ⛓                          │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────────────────────────────────────────────┐   │
│   │                                                     │   │
│   │                    ZELLE 3                          │   │
│   │                                                     │   │
│   │             ████████████░░░░░░░░                    │   │
│   │                                                     │   │
│   │              2:45 VERBLEIBEND                       │   │
│   │                                                     │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
│   ─────────────────────────────────────────────────────────  │
│                                                             │
│   KAUTION                                                   │
│                                                             │
│   Betrag:        3.000€                                     │
│   Dein Konto:    5.420€                                     │
│   Verfügbar ab:  1:00 (1/3 der Zeit)                        │
│                                                             │
│   Status:        ⏳ Noch 0:15 warten...                     │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐   │
│   │                                                     │   │
│   │              [ KAUTION ZAHLEN ]                     │   │
│   │                    (grau wenn nicht verfügbar)      │   │
│   │                                                     │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
│   ─────────────────────────────────────────────────────────  │
│                                                             │
│   §7§o"Du kannst dieses Fenster nicht schließen."           │
│   §7§o"Warte deine Strafe ab oder zahle Kaution."           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## PrisonScreen.java

```java
package de.rolandsw.schedulemc.npc.crime.prison.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.npc.crime.prison.network.PayBailPacket;
import de.rolandsw.schedulemc.npc.crime.prison.network.PrisonNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Gefängnis-GUI - KANN NICHT GESCHLOSSEN WERDEN
 *
 * Schließt sich nur wenn:
 * - Haftzeit abgelaufen (Server sendet ReleasePacket)
 * - Kaution bezahlt (Server bestätigt)
 */
@OnlyIn(Dist.CLIENT)
public class PrisonScreen extends Screen {

    // ═══════════════════════════════════════════════════════════
    // KONSTANTEN
    // ═══════════════════════════════════════════════════════════

    private static final int BACKGROUND_WIDTH = 300;
    private static final int BACKGROUND_HEIGHT = 280;

    private static final int COLOR_BACKGROUND = 0xDD1a1a1a;
    private static final int COLOR_HEADER = 0xFF8B0000;      // Dunkelrot
    private static final int COLOR_PROGRESS_BG = 0xFF333333;
    private static final int COLOR_PROGRESS_FILL = 0xFF4CAF50; // Grün
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_GRAY = 0xFFAAAAAA;
    private static final int COLOR_TEXT_RED = 0xFFFF5555;
    private static final int COLOR_TEXT_GREEN = 0xFF55FF55;
    private static final int COLOR_TEXT_GOLD = 0xFFFFAA00;

    // ═══════════════════════════════════════════════════════════
    // DATEN (vom Server synchronisiert)
    // ═══════════════════════════════════════════════════════════

    private int cellNumber;
    private long totalSentenceTicks;
    private long releaseTime;
    private double bailAmount;
    private double playerBalance;
    private boolean bailAvailable;
    private long bailAvailableAtTick;

    // ═══════════════════════════════════════════════════════════
    // UI ELEMENTE
    // ═══════════════════════════════════════════════════════════

    private int leftPos;
    private int topPos;
    private Button bailButton;

    // Flag: Wurde vom Server freigegeben?
    private boolean canClose = false;

    // ═══════════════════════════════════════════════════════════
    // KONSTRUKTOR
    // ═══════════════════════════════════════════════════════════

    public PrisonScreen(int cellNumber, long totalSentenceTicks, long releaseTime,
                        double bailAmount, double playerBalance, long bailAvailableAtTick) {
        super(Component.literal("Gefängnis"));

        this.cellNumber = cellNumber;
        this.totalSentenceTicks = totalSentenceTicks;
        this.releaseTime = releaseTime;
        this.bailAmount = bailAmount;
        this.playerBalance = playerBalance;
        this.bailAvailableAtTick = bailAvailableAtTick;
        this.bailAvailable = false;
    }

    // ═══════════════════════════════════════════════════════════
    // SCHLIESSEN BLOCKIEREN
    // ═══════════════════════════════════════════════════════════

    /**
     * ESC-Taste soll Screen NICHT schließen
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return false; // NIEMALS durch ESC schließen
    }

    /**
     * Blockiere onClose() - nur wenn canClose == true
     */
    @Override
    public void onClose() {
        if (canClose) {
            super.onClose();
        } else {
            // Zeige Hinweis
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(
                    Component.literal("§c⛓ Du kannst nicht entkommen! Warte ab oder zahle Kaution."),
                    true
                );
            }
        }
    }

    /**
     * Blockiere alle Tasten die schließen könnten
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC (256) blockieren
        if (keyCode == 256) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(
                    Component.literal("§c⛓ ESC ist gesperrt!"),
                    true
                );
            }
            return true; // Event konsumieren, nicht weiterleiten
        }

        // E (Inventar) blockieren
        if (keyCode == 69) {
            return true;
        }

        // Alle anderen Tasten auch blockieren (außer Chat)
        if (keyCode == 84) { // T für Chat
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return true; // Alle anderen blockieren
    }

    /**
     * Verhindere dass Mausklicks außerhalb schließen
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Nur innerhalb des GUIs erlauben
        if (mouseX >= leftPos && mouseX <= leftPos + BACKGROUND_WIDTH &&
            mouseY >= topPos && mouseY <= topPos + BACKGROUND_HEIGHT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return true; // Außerhalb blockieren
    }

    /**
     * Verhindere isPauseScreen um Spiel nicht zu pausieren
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ═══════════════════════════════════════════════════════════
    // INITIALISIERUNG
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
        this.topPos = (this.height - BACKGROUND_HEIGHT) / 2;

        // Kaution-Button (initial deaktiviert)
        this.bailButton = addRenderableWidget(Button.builder(
            Component.literal("KAUTION ZAHLEN"),
            this::onBailButtonClick
        ).bounds(
            leftPos + 50,
            topPos + BACKGROUND_HEIGHT - 80,
            BACKGROUND_WIDTH - 100,
            30
        ).build());

        updateBailButton();
    }

    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Hintergrund abdunkeln
        renderBackground(graphics);

        // Hauptfenster
        renderWindow(graphics);

        // Header
        renderHeader(graphics);

        // Zellen-Info & Timer
        renderCellInfo(graphics);

        // Kautions-Bereich
        renderBailSection(graphics);

        // Hinweis-Text
        renderHintText(graphics);

        // Buttons rendern
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderWindow(GuiGraphics graphics) {
        // Hintergrund
        graphics.fill(leftPos, topPos, leftPos + BACKGROUND_WIDTH, topPos + BACKGROUND_HEIGHT, COLOR_BACKGROUND);

        // Rahmen
        graphics.fill(leftPos, topPos, leftPos + BACKGROUND_WIDTH, topPos + 2, COLOR_HEADER);
        graphics.fill(leftPos, topPos + BACKGROUND_HEIGHT - 2, leftPos + BACKGROUND_WIDTH, topPos + BACKGROUND_HEIGHT, COLOR_HEADER);
        graphics.fill(leftPos, topPos, leftPos + 2, topPos + BACKGROUND_HEIGHT, COLOR_HEADER);
        graphics.fill(leftPos + BACKGROUND_WIDTH - 2, topPos, leftPos + BACKGROUND_WIDTH, topPos + BACKGROUND_HEIGHT, COLOR_HEADER);
    }

    private void renderHeader(GuiGraphics graphics) {
        // Header-Hintergrund
        graphics.fill(leftPos + 2, topPos + 2, leftPos + BACKGROUND_WIDTH - 2, topPos + 35, COLOR_HEADER);

        // Titel
        String title = "⛓ GEFÄNGNIS ⛓";
        int titleWidth = font.width(title);
        graphics.drawString(font, title, leftPos + (BACKGROUND_WIDTH - titleWidth) / 2, topPos + 12, COLOR_TEXT, true);
    }

    private void renderCellInfo(GuiGraphics graphics) {
        int y = topPos + 50;

        // Zellen-Box
        graphics.fill(leftPos + 20, y, leftPos + BACKGROUND_WIDTH - 20, y + 70, 0xFF2a2a2a);

        // Zellen-Nummer
        String cellText = "ZELLE " + cellNumber;
        int cellWidth = font.width(cellText);
        graphics.drawString(font, cellText, leftPos + (BACKGROUND_WIDTH - cellWidth) / 2, y + 8, COLOR_TEXT_GOLD, true);

        // Progress-Bar
        int barX = leftPos + 40;
        int barY = y + 28;
        int barWidth = BACKGROUND_WIDTH - 80;
        int barHeight = 12;

        // Hintergrund
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, COLOR_PROGRESS_BG);

        // Fortschritt berechnen
        long currentTick = getCurrentGameTime();
        long remainingTicks = Math.max(0, releaseTime - currentTick);
        float progress = 1.0f - ((float) remainingTicks / (float) totalSentenceTicks);
        progress = Math.max(0, Math.min(1, progress));

        int progressWidth = (int) (barWidth * progress);
        graphics.fill(barX, barY, barX + progressWidth, barY + barHeight, COLOR_PROGRESS_FILL);

        // Zeit-Anzeige
        int remainingSeconds = (int) (remainingTicks / 20);
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeText = String.format("%d:%02d VERBLEIBEND", minutes, seconds);
        int timeWidth = font.width(timeText);
        graphics.drawString(font, timeText, leftPos + (BACKGROUND_WIDTH - timeWidth) / 2, y + 48, COLOR_TEXT, true);
    }

    private void renderBailSection(GuiGraphics graphics) {
        int y = topPos + 130;

        // Trennlinie
        graphics.fill(leftPos + 20, y, leftPos + BACKGROUND_WIDTH - 20, y + 1, 0xFF555555);

        y += 10;

        // Titel
        graphics.drawString(font, "KAUTION", leftPos + 25, y, COLOR_TEXT_GOLD, true);

        y += 18;

        // Betrag
        graphics.drawString(font, "Betrag:", leftPos + 25, y, COLOR_TEXT_GRAY, false);
        graphics.drawString(font, String.format("%.0f€", bailAmount), leftPos + 120, y, COLOR_TEXT, false);

        y += 14;

        // Kontostand
        graphics.drawString(font, "Dein Konto:", leftPos + 25, y, COLOR_TEXT_GRAY, false);
        int balanceColor = playerBalance >= bailAmount ? COLOR_TEXT_GREEN : COLOR_TEXT_RED;
        graphics.drawString(font, String.format("%.0f€", playerBalance), leftPos + 120, y, balanceColor, false);

        y += 14;

        // Verfügbar ab
        long currentTick = getCurrentGameTime();

        if (bailAvailable || currentTick >= bailAvailableAtTick) {
            bailAvailable = true;
            graphics.drawString(font, "Status:", leftPos + 25, y, COLOR_TEXT_GRAY, false);
            graphics.drawString(font, "✓ Verfügbar!", leftPos + 120, y, COLOR_TEXT_GREEN, false);
        } else {
            long waitTicks = bailAvailableAtTick - currentTick;
            int waitSeconds = (int) (waitTicks / 20);
            int waitMin = waitSeconds / 60;
            int waitSec = waitSeconds % 60;

            graphics.drawString(font, "Status:", leftPos + 25, y, COLOR_TEXT_GRAY, false);
            graphics.drawString(font, String.format("⏳ Noch %d:%02d warten...", waitMin, waitSec),
                leftPos + 120, y, COLOR_TEXT_GRAY, false);
        }
    }

    private void renderHintText(GuiGraphics graphics) {
        int y = topPos + BACKGROUND_HEIGHT - 35;

        String hint1 = "Du kannst dieses Fenster nicht schließen.";
        String hint2 = "Warte deine Strafe ab oder zahle Kaution.";

        int hint1Width = font.width(hint1);
        int hint2Width = font.width(hint2);

        graphics.drawString(font, hint1, leftPos + (BACKGROUND_WIDTH - hint1Width) / 2, y, COLOR_TEXT_GRAY, false);
        graphics.drawString(font, hint2, leftPos + (BACKGROUND_WIDTH - hint2Width) / 2, y + 12, COLOR_TEXT_GRAY, false);
    }

    // ═══════════════════════════════════════════════════════════
    // TICK & UPDATE
    // ═══════════════════════════════════════════════════════════

    @Override
    public void tick() {
        super.tick();

        // Prüfe ob Zeit abgelaufen
        long currentTick = getCurrentGameTime();
        if (currentTick >= releaseTime) {
            // Server sollte ReleasePacket senden, aber Fallback:
            allowClose();
        }

        // Update Bail-Button Status
        if (!bailAvailable && currentTick >= bailAvailableAtTick) {
            bailAvailable = true;
        }
        updateBailButton();
    }

    private void updateBailButton() {
        if (bailButton != null) {
            boolean canAfford = playerBalance >= bailAmount;
            bailButton.active = bailAvailable && canAfford;

            if (!bailAvailable) {
                bailButton.setMessage(Component.literal("§7⏳ Noch nicht verfügbar"));
            } else if (!canAfford) {
                bailButton.setMessage(Component.literal("§c✗ Nicht genug Geld"));
            } else {
                bailButton.setMessage(Component.literal("§a✓ KAUTION ZAHLEN"));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // KAUTION ZAHLEN
    // ═══════════════════════════════════════════════════════════

    private void onBailButtonClick(Button button) {
        if (!bailAvailable || playerBalance < bailAmount) {
            return;
        }

        // Sende Paket an Server
        PrisonNetworkHandler.sendToServer(new PayBailPacket());

        // Button deaktivieren während Verarbeitung
        button.active = false;
        button.setMessage(Component.literal("§7Verarbeite..."));
    }

    // ═══════════════════════════════════════════════════════════
    // SERVER CALLBACKS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird vom Server aufgerufen wenn Spieler entlassen wird
     */
    public void allowClose() {
        this.canClose = true;
        if (minecraft != null) {
            minecraft.setScreen(null); // Schließe GUI
        }
    }

    /**
     * Update Kontostand (vom Server)
     */
    public void updateBalance(double newBalance) {
        this.playerBalance = newBalance;
        updateBailButton();
    }

    /**
     * Kautions-Zahlung fehlgeschlagen
     */
    public void onBailFailed(String reason) {
        if (bailButton != null) {
            bailButton.setMessage(Component.literal("§c" + reason));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    private long getCurrentGameTime() {
        if (minecraft != null && minecraft.level != null) {
            return minecraft.level.getGameTime();
        }
        return 0;
    }

    /**
     * Statische Methode um GUI zu öffnen (vom Netzwerk-Handler aufgerufen)
     */
    public static void open(int cellNumber, long totalSentenceTicks, long releaseTime,
                           double bailAmount, double playerBalance, long bailAvailableAtTick) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new PrisonScreen(
            cellNumber, totalSentenceTicks, releaseTime,
            bailAmount, playerBalance, bailAvailableAtTick
        ));
    }
}
```

---

## Netzwerk-Pakete

### OpenPrisonScreenPacket.java (Server → Client)

```java
package de.rolandsw.schedulemc.npc.crime.prison.network;

import de.rolandsw.schedulemc.npc.crime.prison.client.PrisonScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Öffnet das Gefängnis-GUI auf dem Client
 */
public class OpenPrisonScreenPacket {

    private final int cellNumber;
    private final long totalSentenceTicks;
    private final long releaseTime;
    private final double bailAmount;
    private final double playerBalance;
    private final long bailAvailableAtTick;

    public OpenPrisonScreenPacket(int cellNumber, long totalSentenceTicks, long releaseTime,
                                   double bailAmount, double playerBalance, long bailAvailableAtTick) {
        this.cellNumber = cellNumber;
        this.totalSentenceTicks = totalSentenceTicks;
        this.releaseTime = releaseTime;
        this.bailAmount = bailAmount;
        this.playerBalance = playerBalance;
        this.bailAvailableAtTick = bailAvailableAtTick;
    }

    public static void encode(OpenPrisonScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.cellNumber);
        buf.writeLong(msg.totalSentenceTicks);
        buf.writeLong(msg.releaseTime);
        buf.writeDouble(msg.bailAmount);
        buf.writeDouble(msg.playerBalance);
        buf.writeLong(msg.bailAvailableAtTick);
    }

    public static OpenPrisonScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenPrisonScreenPacket(
            buf.readInt(),
            buf.readLong(),
            buf.readLong(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readLong()
        );
    }

    public static void handle(OpenPrisonScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                PrisonScreen.open(
                    msg.cellNumber,
                    msg.totalSentenceTicks,
                    msg.releaseTime,
                    msg.bailAmount,
                    msg.playerBalance,
                    msg.bailAvailableAtTick
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
```

### PayBailPacket.java (Client → Server)

```java
package de.rolandsw.schedulemc.npc.crime.prison.network;

import de.rolandsw.schedulemc.npc.crime.prison.PrisonManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Spieler möchte Kaution zahlen
 */
public class PayBailPacket {

    public PayBailPacket() {}

    public static void encode(PayBailPacket msg, FriendlyByteBuf buf) {
        // Keine Daten nötig - Server kennt den Spieler
    }

    public static PayBailPacket decode(FriendlyByteBuf buf) {
        return new PayBailPacket();
    }

    public static void handle(PayBailPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                boolean success = PrisonManager.getInstance().payBail(player);

                if (!success) {
                    // Sende Fehlermeldung zurück
                    PrisonNetworkHandler.sendToPlayer(player,
                        new BailResultPacket(false, "Zahlung fehlgeschlagen!"));
                }
                // Bei Erfolg wird ClosePrisonScreenPacket gesendet
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
```

### ClosePrisonScreenPacket.java (Server → Client)

```java
package de.rolandsw.schedulemc.npc.crime.prison.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Schließt das Gefängnis-GUI (Entlassung)
 */
public class ClosePrisonScreenPacket {

    private final String reason;

    public ClosePrisonScreenPacket(String reason) {
        this.reason = reason;
    }

    public static void encode(ClosePrisonScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.reason);
    }

    public static ClosePrisonScreenPacket decode(FriendlyByteBuf buf) {
        return new ClosePrisonScreenPacket(buf.readUtf());
    }

    public static void handle(ClosePrisonScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();

                // Prüfe ob PrisonScreen offen ist
                if (mc.screen instanceof de.rolandsw.schedulemc.npc.crime.prison.client.PrisonScreen prisonScreen) {
                    prisonScreen.allowClose();
                }

                // Zeige Entlassungs-Nachricht
                if (mc.player != null) {
                    mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§a✓ ENTLASSEN: " + msg.reason),
                        false
                    );
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
```

---

## Ablauf

```
┌─────────────────────────────────────────────────────────────┐
│ 1. VERHAFTUNG                                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Polizei verhaftet Spieler                                 │
│              ↓                                              │
│   PrisonManager.imprisonPlayer()                            │
│              ↓                                              │
│   Spieler in Zelle teleportiert                             │
│              ↓                                              │
│   Server sendet OpenPrisonScreenPacket                      │
│              ↓                                              │
│   Client öffnet PrisonScreen (nicht schließbar!)            │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ 2. IM GEFÄNGNIS                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   GUI zeigt:                                                │
│   - Zellennummer                                            │
│   - Fortschritts-Balken                                     │
│   - Verbleibende Zeit (Live-Update)                         │
│   - Kautions-Info                                           │
│   - Kautions-Button (grau bis 1/3 Zeit)                     │
│                                                             │
│   Spieler kann NICHT:                                       │
│   - ESC drücken                                             │
│   - GUI schließen                                           │
│   - Inventar öffnen                                         │
│   - Außerhalb klicken                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ 3a. ZEIT ABGELAUFEN                                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Server-Tick erkennt: releaseTime erreicht                 │
│              ↓                                              │
│   PrisonManager.releasePlayer(TIME_SERVED)                  │
│              ↓                                              │
│   Server sendet ClosePrisonScreenPacket                     │
│              ↓                                              │
│   Client: prisonScreen.allowClose()                         │
│              ↓                                              │
│   GUI schließt sich                                         │
│              ↓                                              │
│   Nachricht: "§a✓ ENTLASSEN: Haft verbüßt"                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ 3b. KAUTION GEZAHLT                                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Spieler klickt "KAUTION ZAHLEN"                           │
│              ↓                                              │
│   Client sendet PayBailPacket                               │
│              ↓                                              │
│   Server prüft:                                             │
│   - 1/3 Zeit verbüßt? ✓                                     │
│   - Genug Geld? ✓                                           │
│              ↓                                              │
│   EconomyManager.withdraw(bail)                             │
│              ↓                                              │
│   PrisonManager.releasePlayer(BAIL_PAID)                    │
│              ↓                                              │
│   Server sendet ClosePrisonScreenPacket                     │
│              ↓                                              │
│   GUI schließt sich                                         │
│              ↓                                              │
│   Nachricht: "§a✓ ENTLASSEN: Kaution bezahlt"               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Blockierte Aktionen im GUI

| Aktion | Blockiert | Wie |
|--------|-----------|-----|
| ESC drücken | ✅ | `shouldCloseOnEsc()` → false |
| E (Inventar) | ✅ | `keyPressed()` blockiert |
| Außerhalb klicken | ✅ | `mouseClicked()` blockiert |
| X-Button | ✅ | Kein X-Button im GUI |
| Alt+F4 | ⚠️ | Disconnect, aber Zeit pausiert |
| /kill | ⚠️ | Respawn in Zelle |

---

## Zusätzliche Sicherheit

### PrisonTickHandler.java

```java
/**
 * Stellt sicher dass GUI immer offen ist während Haft
 */
@SubscribeEvent
public static void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.END) return;

    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) return;

    // Prüfe ob Spieler im Gefängnis ist
    boolean inPrison = mc.player.getPersistentData().getBoolean("IsInPrison");

    if (inPrison) {
        // GUI muss offen sein
        if (!(mc.screen instanceof PrisonScreen)) {
            // GUI wurde irgendwie geschlossen - wieder öffnen!
            // (Sollte nicht passieren, aber Sicherheit)
            requestPrisonScreenFromServer();
        }
    }
}
```

---

## Zusammenfassung

| Feature | Status |
|---------|--------|
| GUI öffnet automatisch | ✅ Bei Inhaftierung |
| ESC blockiert | ✅ `shouldCloseOnEsc()` = false |
| Alle Keybinds blockiert | ✅ `keyPressed()` überschrieben |
| Klick außerhalb blockiert | ✅ `mouseClicked()` überschrieben |
| Live-Timer | ✅ `tick()` + `render()` |
| Kaution-Button | ✅ Aktiviert nach 1/3 Zeit |
| Schließt bei Entlassung | ✅ Server sendet Packet |
| Schließt bei Kaution | ✅ Nach erfolgreicher Zahlung |

Soll ich noch etwas anpassen oder weitere Details hinzufügen?
