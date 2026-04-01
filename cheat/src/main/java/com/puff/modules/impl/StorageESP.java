package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.util.RenderUtils;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;

public class StorageESP extends Module {

    private final BooleanSetting chests = new BooleanSetting("Chests", true);
    private final BooleanSetting enderChests = new BooleanSetting("Ender Chests", true);
    private final BooleanSetting furnaces = new BooleanSetting("Furnaces", true);
    private final BooleanSetting hoppers = new BooleanSetting("Hoppers", true);
    private final BooleanSetting dispensers = new BooleanSetting("Dispensers", true);

    public StorageESP() {
        super("StorageESP", Category.VISUAL);
        registerSetting(chests);
        registerSetting(enderChests);
        registerSetting(furnaces);
        registerSetting(hoppers);
        registerSetting(dispensers);
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (!isEnabled() || mc.theWorld == null) return;

        for (TileEntity tile : mc.theWorld.loadedTileEntityList) {
            BlockPos pos = tile.getPos();
            int color = -1;

            if (tile instanceof TileEntityChest && chests.get()) {
                color = new Color(255, 128, 0, 100).getRGB(); // Orange
            } else if (tile instanceof TileEntityEnderChest && enderChests.get()) {
                color = new Color(180, 0, 255, 100).getRGB(); // Purple
            } else if (tile instanceof TileEntityFurnace && furnaces.get()) {
                color = new Color(150, 150, 150, 100).getRGB(); // Gray
            } else if (tile instanceof TileEntityHopper && hoppers.get()) {
                color = new Color(80, 80, 80, 100).getRGB(); // Dark Gray
            } else if ((tile instanceof TileEntityDispenser || tile instanceof TileEntityDropper) && dispensers.get()) {
                color = new Color(100, 100, 100, 100).getRGB(); // Light Gray
            }

            if (color != -1) {
                RenderUtils.drawBlockBox(pos, partialTicks, color);
            }
        }
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}
