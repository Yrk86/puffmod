package com.puff.mixins;

import com.puff.PuffMod;
import com.puff.modules.impl.HitSelect;
import com.puff.modules.impl.WTap;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    private final Random random = new Random();

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void puff$onAttackEntity(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        if (PuffMod.getModuleManager() == null) return;

        HitSelect hitSelect = PuffMod.getModuleManager().getModule(HitSelect.class);
        if (hitSelect != null && hitSelect.isEnabled()) {
            
            // Random chance check
            if (random.nextDouble() * 100.0 > hitSelect.getChance().get()) {
                return;
            }

            // HurtTime check
            if (targetEntity instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase) targetEntity;
                if (living.hurtTime > hitSelect.getHurtTime().get()) {
                    ci.cancel();
                    return;
                }
            }
        }

        WTap wtap = PuffMod.getModuleManager().getModule(WTap.class);
        if (wtap != null && wtap.isEnabled()) {
            wtap.onAttack();
        }
    }
}
