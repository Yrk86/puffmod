package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import com.puff.util.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Projectiles extends Module {

    private final BooleanSetting arrows = new BooleanSetting("Arrows", true);
    private final BooleanSetting pearls = new BooleanSetting("Pearls", true);
    private final BooleanSetting potions = new BooleanSetting("Potions", true);
    private final NumberSetting red = new NumberSetting("Red", 255, 0, 100, 5);
    private final NumberSetting green = new NumberSetting("Green", 255, 0, 255, 5);
    private final NumberSetting blue = new NumberSetting("Blue", 0, 0, 255, 5);

    public Projectiles() {
        super("Projectiles", Category.VISUAL);
        registerSetting(arrows);
        registerSetting(pearls);
        registerSetting(potions);
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
            if (entity instanceof EntityArrow && !arrows.get()) continue;
            if (entity instanceof EntityEnderPearl && !pearls.get()) continue;
            if ((entity instanceof EntityPotion || entity instanceof EntityThrowable) && !potions.get()) continue;

            if (entity instanceof EntityArrow || entity instanceof EntityEnderPearl || entity instanceof EntityPotion || entity instanceof EntityThrowable) {
                if (entity.onGround) continue;
                
                renderProjectileTrajectory(entity, partialTicks);
            }
        }
    }

    private void renderProjectileTrajectory(Entity entity, float partialTicks) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        double motionX = entity.motionX;
        double motionY = entity.motionY;
        double motionZ = entity.motionZ;

        float gravity = 0.03f;
        float drag = 0.99f;

        if (entity instanceof EntityArrow) gravity = 0.05f;
        else if (entity instanceof EntityPotion) gravity = 0.05f;

        List<Vec3d> trajectory = new ArrayList<>();
        RayTraceResult hit = null;

        for (int i = 0; i < 100; i++) {
            trajectory.add(new Vec3d(x, y, z));
            
            x += motionX;
            y += motionY;
            z += motionZ;
            
            motionX *= drag;
            motionY *= drag;
            motionZ *= drag;
            motionY -= gravity;

            Vec3d nextPos = new Vec3d(x, y, z);
            hit = mc.theWorld.rayTraceBlocks(trajectory.get(trajectory.size() - 1), nextPos, false, true, false);
            
            if (hit != null) {
                trajectory.add(hit.hitVec);
                break;
            }
        }

        renderPath(trajectory);
        
        if (hit != null) {
            RenderUtils.drawBlockBox(new BlockPos(hit.hitVec), partialTicks, new Color((int)red.get(), (int)green.get(), (int)blue.get(), 100).getRGB());
        }
    }

    private void renderPath(List<Vec3d> points) {
        if (points.isEmpty()) return;
        
        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.5f);

        GlStateManager.color((float) (red.get() / 255.0), (float) (green.get() / 255.0), (float) (blue.get() / 255.0), 0.8f);
        
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (Vec3d p : points) {
            GL11.glVertex3d(p.xCoord - renderPosX, p.yCoord - renderPosY, p.zCoord - renderPosZ);
        }
        GL11.glEnd();

        GlStateManager.depthMask(true);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
