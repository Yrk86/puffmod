package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import com.puff.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Search extends Module {

    private final NumberSetting range = new NumberSetting("Range", 32.0, 16.0, 128.0, 8.0);
    private final BooleanSetting diamonds = new BooleanSetting("Diamonds", true);
    private final BooleanSetting gold = new BooleanSetting("Gold", true);
    private final BooleanSetting iron = new BooleanSetting("Iron", true);
    private final BooleanSetting emerald = new BooleanSetting("Emerald", true);
    private final BooleanSetting lapis = new BooleanSetting("Lapis", true);
    private final BooleanSetting redstone = new BooleanSetting("Redstone", true);
    private final BooleanSetting coal = new BooleanSetting("Coal", true);

    private final List<BlockPos> foundBlocks = new CopyOnWriteArrayList<>();
    private long lastScanTime = 0;

    public Search() {
        super("Search", Category.VISUAL);
        registerSetting(range);
        registerSetting(diamonds);
        registerSetting(gold);
        registerSetting(iron);
        registerSetting(emerald);
        registerSetting(lapis);
        registerSetting(redstone);
        registerSetting(coal);
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        // Scanner toutes les 2 secondes pour les performances
        if (now - lastScanTime >= 2000) {
            new Thread(this::scanBlocks).start();
            lastScanTime = now;
        }
    }

    private void scanBlocks() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        List<BlockPos> tempBlocks = new ArrayList<>();
        int r = (int) range.get();
        BlockPos playerPos = mc.thePlayer.getPosition();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();

                    if (isTargetBlock(block)) {
                        tempBlocks.add(pos);
                    }
                }
            }
        }

        foundBlocks.clear();
        foundBlocks.addAll(tempBlocks);
    }

    private boolean isTargetBlock(Block block) {
        if (diamonds.get() && block == Blocks.DIAMOND_ORE) return true;
        if (gold.get() && block == Blocks.GOLD_ORE) return true;
        if (iron.get() && block == Blocks.IRON_ORE) return true;
        if (emerald.get() && block == Blocks.EMERALD_ORE) return true;
        if (lapis.get() && block == Blocks.LAPIS_ORE) return true;
        if (redstone.get() && (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE)) return true;
        if (coal.get() && block == Blocks.COAL_ORE) return true;
        return false;
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (!isEnabled()) return;

        for (BlockPos pos : foundBlocks) {
            Block block = mc.theWorld.getBlockState(pos).getBlock();
            int color = getBlockColor(block);
            RenderUtils.drawBlockBox(pos, partialTicks, color);
        }
    }

    private int getBlockColor(Block block) {
        if (block == Blocks.DIAMOND_ORE) return new Color(0, 255, 255, 100).getRGB();
        if (block == Blocks.GOLD_ORE) return new Color(255, 255, 0, 100).getRGB();
        if (block == Blocks.IRON_ORE) return new Color(200, 200, 200, 100).getRGB();
        if (block == Blocks.EMERALD_ORE) return new Color(0, 255, 0, 100).getRGB();
        if (block == Blocks.LAPIS_ORE) return new Color(0, 0, 255, 100).getRGB();
        if (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE) return new Color(255, 0, 0, 100).getRGB();
        if (block == Blocks.COAL_ORE) return new Color(50, 50, 50, 100).getRGB();
        return -1;
    }

    @Override
    public void onEnable() {
        foundBlocks.clear();
    }

    @Override
    public void onDisable() {
        foundBlocks.clear();
    }
}
