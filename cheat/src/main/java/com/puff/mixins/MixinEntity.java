package com.puff.mixins;

import com.puff.PuffMod;
import com.puff.modules.impl.Velocity;
import net.minecraft.entity.Entity;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {

    @Shadow public double motionX;
    @Shadow public double motionY;
    @Shadow public double motionZ;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;

    @Inject(method = "setVelocity", at = @At("HEAD"), cancellable = true)
    private void puff$setVelocity(double x, double y, double z, CallbackInfo ci) {
        // Cible uniquement le joueur local pour éviter de casser le recul des mobs
        if ((Object)this instanceof EntityPlayerSP) {
            Velocity velocity = PuffMod.getModuleManager() != null ? PuffMod.getModuleManager().getModule(Velocity.class) : null;
            if (velocity != null && velocity.isEnabled()) {
                double h = velocity.getHorizontal() / 100.0;
                double v = velocity.getVertical() / 100.0;
                
                this.motionX = x * h;
                this.motionY = y * v;
                this.motionZ = z * h;
                
                ci.cancel(); // On annule l'original pour utiliser nos valeurs modifiées
            }
        }
    }
}
