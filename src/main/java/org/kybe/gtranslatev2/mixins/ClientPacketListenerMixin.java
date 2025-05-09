package org.kybe.gtranslatev2.mixins;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.kybe.gtranslatev2.GTranslateV2Module;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
  @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
  public void sendChat(String message, CallbackInfo ci) {
    if (GTranslateV2Module.INSTANCE == null) return;
    if (!GTranslateV2Module.INSTANCE.isToggled()) return;
    boolean ret = GTranslateV2Module.INSTANCE.onChat(message);
    if (ret) {
      ci.cancel();
    }
  }
}
