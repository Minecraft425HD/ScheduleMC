package de.rolandsw.schedulemc.mapview.presentation.screen;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;

public abstract class PopupScreen extends BaseMapScreen implements IPopupScreen {
    private final ArrayList<PopupComponent> popups = new ArrayList<>();

    @Override
    public void removed() {
    }

    public void createPopup(int x, int y, int directX, int directY, int minWidth, ArrayList<PopupComponent.PopupEntry> entries) {
        popups.add(new PopupComponent(x, y, directX, directY, minWidth, entries, this));
    }

    public void clearPopups() {
        popups.clear();
    }

    public boolean clickedPopup(double x, double y) {
        boolean clicked = false;
        ArrayList<PopupComponent> deadPopups = new ArrayList<>();

        for (PopupComponent popup : this.popups) {
            boolean clickedPopup = popup.clickedMe(x, y);
            if (!clickedPopup) {
                deadPopups.add(popup);
            } else if (popup.shouldClose()) {
                deadPopups.add(popup);
            }

            clicked = clicked || clickedPopup;
        }

        this.popups.removeAll(deadPopups);
        return clicked;
    }

    @Override
    public boolean overPopup(int mouseX, int mouseY) {
        boolean over = false;

        for (PopupComponent popup : this.popups) {
            boolean overPopup = popup.overMe(mouseX, mouseY);
            over = over || overPopup;
        }

        return !over;
    }

    @Override
    public boolean popupOpen() {
        return popups.isEmpty();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        for (PopupComponent popup : this.popups) {
            popup.drawPopup(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return !this.clickedPopup(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
    }
}
