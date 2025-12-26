package de.rolandsw.schedulemc.mapview.mixins;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Mixin(ChatComponent.class)
public class APIMixinChatListenerHud {
    // @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    public void postSay(Component message, CallbackInfo ci) {
        if (!MapViewConstants.onChat(message, null)) {
            ci.cancel();
        }
    }
}
