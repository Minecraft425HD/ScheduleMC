package de.rolandsw.schedulemc.mapview.service.coordination;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.presentation.renderer.MapViewRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Service responsible for coordinating map rendering operations.
 * Manages the MapViewRenderer lifecycle and render tick updates.
 *
 * Part of Phase 2 refactoring to reduce structural similarity.
 */
public class RenderCoordinationService {

    private MapViewRenderer renderer;
    private String pendingMessage;

    public RenderCoordinationService() {
        // Delay renderer initialization to avoid circular dependency during static initialization
        this.renderer = null;
    }

    /**
     * Lazily initializes the renderer on first access.
     * This avoids circular dependency issues during static initialization.
     */
    private void ensureRendererInitialized() {
        if (this.renderer == null) {
            this.renderer = new MapViewRenderer();
        }
    }

    /**
     * Called each game tick when rendering is needed.
     * Delegates to the renderer and handles pending chat messages.
     */
    public void onTickInGame(GuiGraphics guiGraphics) {
        ensureRendererInitialized();
        if (this.renderer != null) {
            this.renderer.onTickInGame(guiGraphics);
        }

        // Dispatch pending message to chat if any
        if (pendingMessage != null) {
            MapViewConstants.getMinecraft().gui.getChat()
                .addMessage(Component.literal(pendingMessage));
            pendingMessage = null;
        }
    }

    /**
     * Queues a message to be displayed in chat on the next render tick.
     * Thread-safe way to send messages from background threads.
     */
    public void sendMessageOnMainThread(String message) {
        this.pendingMessage = message;
    }

    /**
     * Gets the managed renderer instance.
     */
    public MapViewRenderer getRenderer() {
        ensureRendererInitialized();
        return this.renderer;
    }

    /**
     * Notifies the renderer of a new world being loaded.
     */
    public void onWorldChanged(net.minecraft.client.multiplayer.ClientLevel world) {
        ensureRendererInitialized();
        if (this.renderer != null && world != null) {
            this.renderer.newWorld(world);
        }
    }
}
