package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import com.puff.util.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;

public class PropHunt extends Module {

    private final NumberSetting red = new NumberSetting("Red", 255, 0, 255, 5);
    private final NumberSetting green = new NumberSetting("Green", 200, 0, 255, 5);
    private final NumberSetting blue = new NumberSetting("Blue", 0, 0, 255, 5);

    public PropHunt() {
        super("PropHunt", Category.VISUAL);
        registerSetting(red);
        registerSetting(green);
        registerSetting(blue);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onRender3D(float partialTicks) {
        if (!isEnabled()) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityFallingBlock) {
                RenderUtils.drawBlockBox(new BlockPos(entity), partialTicks, new Color((int)red.get(), (int)green.get(), (int)blue.get(), 150).getRGB());
            }
        }
    }
}
