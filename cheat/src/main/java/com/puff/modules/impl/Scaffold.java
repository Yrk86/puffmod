package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

    private final NumberSetting delay = new NumberSetting("Delay", 50, 0, 500, 10);
    private final BooleanSetting rotations = new BooleanSetting("Rotations", true);
    private final BooleanSetting tower = new BooleanSetting("Tower", true);

    private long lastPlaceTime = 0;

    public Scaffold() {
        super("Scaffold", Category.MOVEMENT);
        registerSetting(delay);
        registerSetting(rotations);
        registerSetting(tower);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onTick() {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;

        BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        
        if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.AIR) {
            placeBlock(pos);
        } else if (tower.get() && mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.motionY < 0.3) {
            // Mode Tower : si on saute, on continue de poser sous nous
            if (mc.theWorld.getBlockState(pos.down()).getBlock() != Blocks.AIR) {
                 placeBlock(pos);
            }
        }
    }

    private void placeBlock(BlockPos pos) {
        if (System.currentTimeMillis() - lastPlaceTime < delay.get()) return;

        int slot = findBlockInHotbar();
        if (slot == -1) return;

        int oldSlot = mc.thePlayer.inventory.currentItem;
        mc.thePlayer.inventory.currentItem = slot;

        // Trouver une face sur laquelle poser le bloc
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            if (mc.theWorld.getBlockState(neighbor).getBlock() != Blocks.AIR) {
                // On peut poser sur ce voisin
                EnumFacing side = facing.getOpposite();
                Vec3d hitVec = new Vec3d(neighbor).addVector(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));

                if (rotations.get()) {
                    float[] angles = getRotationsToBlock(neighbor, side);
                    // On n'applique que si la différence est notable pour éviter le jitter permanent
                    mc.thePlayer.rotationYaw = angles[0];
                    mc.thePlayer.rotationPitch = angles[1];
                }

                if (mc.playerController.processRightClickBlock(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(EnumHand.MAIN_HAND), neighbor, side, hitVec, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS) {
                    mc.thePlayer.swingArm(EnumHand.MAIN_HAND);
                    lastPlaceTime = System.currentTimeMillis();
                }
                break;
            }
        }

        mc.thePlayer.inventory.currentItem = oldSlot;
    }

    private int findBlockInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block.getDefaultState().isFullBlock()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private float[] getRotationsToBlock(BlockPos pos, EnumFacing facing) {
        // Approche simplifiée : on regarde simplement vers le bas (entre 75 et 85 degrés)
        // et on garde le Yaw actuel du joueur pour éviter de le faire tourner sur lui-même.
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = 82.0f; // Angle idéal pour le Scaffold standard
        
        return new float[]{yaw, pitch};
    }
}
