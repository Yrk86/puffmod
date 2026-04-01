package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.NumberSetting;
import com.puff.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpawnerFinder extends Module {

    private final NumberSetting range = new NumberSetting("Range", 64.0, 16.0, 128.0, 8.0);
    private final List<BlockPos> spawners = new CopyOnWriteArrayList<>();
    private long lastScanTime = 0;

    public SpawnerFinder() {
        super("SpawnerFinder", Category.VISUAL);
        registerSetting(range);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        
        long now = System.currentTimeMillis();
        // Scan toutes les 3 secondes
        if (now - lastScanTime >= 3000) {
            new Thread(this::scanSpawners).start();
            lastScanTime = now;
        }
    }

    private void scanSpawners() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        List<BlockPos> tempSpawners = new ArrayList<>();
        int r = (int) range.get();
        BlockPos playerPos = mc.thePlayer.getPosition();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();

                    if (block == Blocks.MOB_SPAWNER) {
                        tempSpawners.add(pos);
                    }
                }
            }
        }

        spawners.clear();
        spawners.addAll(tempSpawners);
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (!isEnabled()) return;

        int color = new Color(255, 0, 255, 120).getRGB(); // Magenta pour les spawners
        for (BlockPos pos : spawners) {
            RenderUtils.drawBlockBox(pos, partialTicks, color);
        }
    }

    @Override
    public void onEnable() {
        spawners.clear();
    }

    @Override
    public void onDisable() {
        spawners.clear();
    }
}
