package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class WTap extends Module {

    private final NumberSetting range = new NumberSetting("Range", 3.0, 1.0, 6.0, 0.1);
    private final NumberSetting ticks = new NumberSetting("Ticks", 1.0, 1.0, 5.0, 1.0);

    private int comboTicks = 0;
    private boolean active = false;

    public WTap() {
        super("WTap", Category.COMBAT);
        registerSetting(range);
        registerSetting(ticks);
    }

    @Override
    public void onEnable() {
        active = false;
        comboTicks = 0;
    }

    @Override
    public void onTick() {
        if (active) {
            if (comboTicks > 0) {
                // Relâcher la touche d'avancée
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                comboTicks--;
            } else {
                active = false;
            }
        }
    }

    public void onAttack() {
        if (isEnabled()) {
            active = true;
            comboTicks = (int) ticks.get();
        }
    }

    @Override
    public void onDisable() {
        active = false;
        comboTicks = 0;
    }
}
