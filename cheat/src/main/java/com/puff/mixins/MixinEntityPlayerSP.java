package com.puff.mixins;

import com.puff.PuffMod;
import com.puff.modules.impl.SilentAura;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinEntity {

    private float preYaw, prePitch;
    private boolean rotating;

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void puff$onUpdateWalkingPlayerHead(CallbackInfo ci) {
        if (PuffMod.getModuleManager() != null) {
            PuffMod.getModuleManager().onPrePacketSend();

            SilentAura aura = PuffMod.getModuleManager().getModule(SilentAura.class);
            if (aura != null && aura.isEnabled() && aura.hasTarget()) {
                this.preYaw = this.rotationYaw;
                this.prePitch = this.rotationPitch;
                this.rotationYaw = aura.getYaw();
                this.rotationPitch = aura.getPitch();
                this.rotating = true;
            }
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    private void puff$onUpdateWalkingPlayerReturn(CallbackInfo ci) {
        if (this.rotating) {
            this.rotationYaw = this.preYaw;
            this.rotationPitch = this.prePitch;
            this.rotating = false;
        }
    }
}

