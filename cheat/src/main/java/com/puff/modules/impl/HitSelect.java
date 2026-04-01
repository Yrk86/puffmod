package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.NumberSetting;

public class HitSelect extends Module {

    private final NumberSetting chance = new NumberSetting("Chance", 100.0, 0.0, 100.0, 1.0);
    private final NumberSetting hurtTime = new NumberSetting("HurtTime", 10.0, 0.0, 20.0, 1.0);

    public HitSelect() {
        super("HitSelect", Category.COMBAT);
        registerSetting(chance);
        registerSetting(hurtTime);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    public NumberSetting getChance() {
        return chance;
    }

    public NumberSetting getHurtTime() {
        return hurtTime;
    }
}
