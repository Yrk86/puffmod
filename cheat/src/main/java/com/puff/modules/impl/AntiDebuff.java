package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class AntiDebuff extends Module {

    private final BooleanSetting nausea = new BooleanSetting("NoNausea", true);
    private final BooleanSetting blindness = new BooleanSetting("NoBlindness", true);
    private final BooleanSetting slowness = new BooleanSetting("NoSlow", true);

    public AntiDebuff() {
        super("AntiDebuff", Category.VISUAL);
        registerSetting(nausea);
        registerSetting(blindness);
        registerSetting(slowness);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onTick() {
        if (!isEnabled() || mc.thePlayer == null) return;

        if (nausea.get() && mc.thePlayer.isPotionActive(MobEffects.NAUSEA)) {
            mc.thePlayer.removeActivePotionEffect(MobEffects.NAUSEA);
        }

        if (blindness.get() && mc.thePlayer.isPotionActive(MobEffects.BLINDNESS)) {
            mc.thePlayer.removeActivePotionEffect(MobEffects.BLINDNESS);
        }

        if (slowness.get() && mc.thePlayer.isPotionActive(MobEffects.SLOWNESS)) {
            mc.thePlayer.removeActivePotionEffect(MobEffects.SLOWNESS);
            mc.thePlayer.removePotionEffect(MobEffects.SLOWNESS);
            // On force la remise à jour des attributs si possible
            mc.thePlayer.setSprinting(mc.thePlayer.isSprinting());
        }
    }
}
