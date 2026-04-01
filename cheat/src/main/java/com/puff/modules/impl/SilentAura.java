package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.NumberSetting;
import com.puff.core.settings.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SilentAura extends Module {

    private final NumberSetting range = new NumberSetting("Range", 3.0, 3.0, 6.0, 0.1);
    private final NumberSetting fov = new NumberSetting("FOV", 90.0, 10.0, 360.0, 1.0);
    private final NumberSetting aps = new NumberSetting("APS", 10.0, 1.0, 20.0, 1.0);
    private final BooleanSetting playersOnly = new BooleanSetting("Players Only", true);
    private final BooleanSetting showTarget = new BooleanSetting("Show Target", true);

    private EntityLivingBase target;
    private float lastYaw, lastPitch;
    private boolean hasTarget;
    private long lastAttackTime;

    public SilentAura() {
        super("SilentAura", Category.COMBAT);
        registerSetting(range);
        registerSetting(fov);
        registerSetting(aps);
        registerSetting(playersOnly);
        registerSetting(showTarget);
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (isEnabled() && showTarget.get() && target != null) {
            com.puff.util.RenderUtils.drawTracerLine(target, partialTicks, new java.awt.Color(255, 0, 0, 150).getRGB());
        }
    }

    @Override
    public void onTick() {
        findTarget();

        if (target != null) {
            float[] rotations = getRotations(target);
            lastYaw = rotations[0];
            lastPitch = rotations[1];
            hasTarget = true;

            // Système d'attaque automatique (Aura)
            long now = System.currentTimeMillis();
            long delay = (long) (1000.0 / aps.get());
            if (now - lastAttackTime >= delay) {
                if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit == target) {
                    mc.playerController.attackEntity(mc.thePlayer, target);
                    mc.thePlayer.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
                    lastAttackTime = now;
                } else if (mc.thePlayer.getDistanceToEntity(target) <= range.get()) {
                    // Si on n'est pas exactement dessus mais à portée, on force l'attaque 
                    // (Les rotations silencieuses feront le reste côté serveur)
                    mc.playerController.attackEntity(mc.thePlayer, target);
                    mc.thePlayer.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
                    lastAttackTime = now;
                }
            }
        } else {
            hasTarget = false;
        }
    }

    private void findTarget() {
        List<EntityLivingBase> targets = new ArrayList<>();
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase && entity != mc.thePlayer) {
                EntityLivingBase living = (EntityLivingBase) entity;
                if (living.isEntityAlive() && mc.thePlayer.getDistanceToEntity(living) <= range.get()) {
                    if (playersOnly.get() && !(living instanceof EntityPlayer)) continue;
                    if (isInFov(living, (float) fov.get())) {
                        targets.add(living);
                    }
                }
            }
        }

        targets.sort(Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e)));
        target = targets.isEmpty() ? null : targets.get(0);
    }

    private boolean isInFov(Entity entity, float fov) {
        float[] rotations = getRotations(entity);
        float yawDiff = getAngleDifference(mc.thePlayer.rotationYaw, rotations[0]);
        float pitchDiff = getAngleDifference(mc.thePlayer.rotationPitch, rotations[1]);
        return Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff) <= fov / 2.0;
    }

    private float getAngleDifference(float a, float b) {
        return ((((a - b) % 360.0F) + 540.0F) % 360.0F) - 180.0F;
    }

    private float[] getRotations(Entity entity) {
        double d0 = entity.posX - mc.thePlayer.posX;
        double d1 = entity.posZ - mc.thePlayer.posZ;
        double d2 = entity.posY + (double)entity.getEyeHeight() - (mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight());
        double d3 = (double) MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float f = (float)(MathHelper.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
        float f1 = (float)(-(MathHelper.atan2(d2, d3) * (180D / Math.PI)));
        return new float[]{f, f1};
    }

    @Override
    public void onEnable() {
        target = null;
        hasTarget = false;
    }

    @Override
    public void onDisable() {
        target = null;
        hasTarget = false;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public float getYaw() {
        return lastYaw;
    }

    public float getPitch() {
        return lastPitch;
    }
}
