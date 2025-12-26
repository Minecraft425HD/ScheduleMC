package de.rolandsw.schedulemc.lightmap.mixins;

import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Mixin(ChatComponent.class)
public class APIMixinChatListenerHud {
    // @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    public void postSay(Component message, CallbackInfo ci) {
        if (!LightMapConstants.onChat(message, null)) {
            ci.cancel();
        }
    }
}
