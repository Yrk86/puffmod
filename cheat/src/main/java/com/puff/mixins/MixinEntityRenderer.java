package com.puff.mixins;

import com.puff.PuffMod;
import com.puff.modules.impl.HudOverlay;
import com.puff.modules.impl.Reach;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.multiplayer.PlayerControllerMP;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void puff$cancelHurtCam(float partialTicks, CallbackInfo ci) {
        if (PuffMod.getModuleManager() != null) {
            HudOverlay hud = PuffMod.getModuleManager().getModule(HudOverlay.class);
            if (hud != null && hud.isEnabled() && hud.getNoHurtCam().get()) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;extendedReach()Z"))
    private boolean puff$extendedReach(PlayerControllerMP playerControllerMP) {
        if (PuffMod.getModuleManager() != null) {
            Reach reach = PuffMod.getModuleManager().getModule(Reach.class);
            if (reach != null && reach.isEnabled()) {
                return true;
            }
        }
        return playerControllerMP.extendedReach();
    }

    @ModifyConstant(method = "getMouseOver", constant = @Constant(doubleValue = 6.0D))
    private double puff$modifyReach6(double original) {
        if (PuffMod.getModuleManager() != null) {
            Reach reach = PuffMod.getModuleManager().getModule(Reach.class);
            if (reach != null && reach.isEnabled()) {
                return reach.getReachDistance();
            }
        }
        return original;
    }
}
