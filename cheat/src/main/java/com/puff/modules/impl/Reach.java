package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.NumberSetting;

public class Reach extends Module {

    private final NumberSetting distance = new NumberSetting("Distance", 3.0, 3.0, 4.0, 0.1);

    public Reach() {
        super("Reach", Category.COMBAT);
        registerSetting(distance);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    public double getReachDistance() {
        return distance.getValue();
    }
}
