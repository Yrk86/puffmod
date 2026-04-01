package com.puff.modules.impl;

import com.puff.core.Module;

public class Fullbright extends Module {

    private float oldGamma;

    public Fullbright() {
        super("Fullbright", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        oldGamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 1000f; // Valeur extrême pour vision claire
    }

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = oldGamma;
    }
}
