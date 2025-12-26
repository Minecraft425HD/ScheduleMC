package de.rolandsw.schedulemc.mapview.presentation.screen;

import de.rolandsw.schedulemc.mapview.presentation.component.PopupComponent;

public interface IPopupScreen {
    boolean overPopup(int mouseX, int mouseY);

    boolean popupOpen();

    void popupAction(PopupComponent popup, int action);

    boolean mouseClicked(double mouseX, double mouseY, int button);
}