package com.puff.modules.impl;

import com.puff.core.Module;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        // Rien de spécial à l'activation
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        
        // Conditions minimales pour un sprint "legit" (Vape style)
        if (mc.thePlayer.moveForward > 0 && !mc.thePlayer.isSneaking() && !mc.thePlayer.isCollidedHorizontally && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
            mc.thePlayer.setSprinting(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.thePlayer.setSprinting(false);
        }
    }
}
