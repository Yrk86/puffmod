package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

public class AimAssist extends Module {

    private final NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 8.0, 0.1);
    private final NumberSetting speed = new NumberSetting("Speed", 5.0, 1.0, 10.0, 0.5);
    private final NumberSetting fov = new NumberSetting("FOV", 45.0, 10.0, 180.0, 5.0);
    private final BooleanSetting clickAim = new BooleanSetting("Click Aim", true);
    
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);
    private final BooleanSetting teamCheck = new BooleanSetting("TeamCheck", true);

    public AimAssist() {
        super("AimAssist", Category.COMBAT);
        
        registerSetting(range);
        registerSetting(speed);
        registerSetting(fov);
        registerSetting(clickAim);
        registerSetting(players);
        registerSetting(mobs);
        registerSetting(animals);
        registerSetting(teamCheck);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        
        if (clickAim.getValue() && !Mouse.isButtonDown(0)) {
            return;
        }

        Entity target = getOptimalTarget();
        if (target != null) {
            faceTarget(target);
        }
    }

    private void faceTarget(Entity target) {
        float[] rots = getRotations(target);
        
        float currentYaw = mc.thePlayer.rotationYaw;
        float currentPitch = mc.thePlayer.rotationPitch;
        
        float yawDiff = MathHelper.wrapDegrees(rots[0] - currentYaw);
        float pitchDiff = MathHelper.wrapDegrees(rots[1] - currentPitch);
        
        // Speed de 1 (lent) à 10 (rapide). 
        // 11 - speed donne le diviseur : de 1 (instant) à 10 (très lissé).
        float smooth = (float) (11.0 - speed.getValue());
        
        float yawStep = yawDiff / smooth;
        float pitchStep = pitchDiff / smooth;
        
        mc.thePlayer.rotationYaw += yawStep;
        mc.thePlayer.rotationPitch += pitchStep;
    }

    private Entity getOptimalTarget() {
        Entity optimal = null;
        double minAngleDiff = Double.MAX_VALUE;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;
            if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHealth() <= 0) continue;

            boolean isValid = false;
            
            if (entity instanceof EntityPlayer && players.getValue()) {
                if (teamCheck.getValue() && mc.thePlayer.isOnSameTeam(entity)) {
                    continue; // Ignore alliés
                } else {
                    isValid = true;
                }
            } else if (entity instanceof net.minecraft.entity.monster.IMob && mobs.getValue()) {
                isValid = true;
            } else if ((entity instanceof net.minecraft.entity.passive.EntityAnimal || entity instanceof net.minecraft.entity.passive.IAnimals) && animals.getValue()) {
                isValid = true;
            }

            if (!isValid) continue;

            double dist = mc.thePlayer.getDistanceToEntity(entity);
            if (dist > range.getValue()) continue;

            float[] rots = getRotations(entity);
            float yawDiff = Math.abs(MathHelper.wrapDegrees(rots[0] - mc.thePlayer.rotationYaw));
            float pitchDiff = Math.abs(MathHelper.wrapDegrees(rots[1] - mc.thePlayer.rotationPitch));

            if (yawDiff <= fov.getValue() / 2.0 && pitchDiff <= fov.getValue() / 2.0) {
                double totalDiff = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
                if (totalDiff < minAngleDiff) {
                    optimal = entity;
                    minAngleDiff = totalDiff;
                }
            }
        }
        return optimal;
    }

    private float[] getRotations(Entity entity) {
        double diffX = entity.posX - mc.thePlayer.posX;
        double diffZ = entity.posZ - mc.thePlayer.posZ;
        double diffY;
        
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase entityLB = (EntityLivingBase) entity;
            diffY = entityLB.posY + (entityLB.getEyeHeight() * 0.9) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        } else {
            diffY = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }

        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);
        
        return new float[]{
                mc.thePlayer.rotationYaw + MathHelper.wrapDegrees(yaw - mc.thePlayer.rotationYaw),
                mc.thePlayer.rotationPitch + MathHelper.wrapDegrees(pitch - mc.thePlayer.rotationPitch)
        };
    }
}
