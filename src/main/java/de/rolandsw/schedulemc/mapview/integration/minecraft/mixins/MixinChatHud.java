package de.rolandsw.schedulemc.mapview.integration.minecraft;

import de.rolandsw.schedulemc.mapview.service.data.MapDataManager;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Mixin(ChatComponent.class)
public class MixinChatHud {

    // @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void addMessage(Component message, CallbackInfo ci) {
        MapDataManager.checkPermissionMessages(message);
    }
}
