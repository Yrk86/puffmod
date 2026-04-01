package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import com.puff.util.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Trajectories extends Module {

    private final BooleanSetting showImpact = new BooleanSetting("ShowImpact", true);
    private final NumberSetting red = new NumberSetting("Red", 255, 0, 255, 5);
    private final NumberSetting green = new NumberSetting("Green", 0, 0, 255, 5);
    private final NumberSetting blue = new NumberSetting("Blue", 0, 0, 255, 5);

    public Trajectories() {
        super("Trajectories", Category.VISUAL);
        registerSetting(showImpact);
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
        if (!isEnabled() || mc.thePlayer == null || mc.thePlayer.getHeldItemMainhand() == null) return;

        ItemStack stack = mc.thePlayer.getHeldItemMainhand();
        Item item = stack.getItem();

        double motionX, motionY, motionZ;
        double x = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks - (MathHelper.cos(mc.thePlayer.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
        double y = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks + mc.thePlayer.getEyeHeight() - 0.10000000149011612D;
        double z = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks - (MathHelper.sin(mc.thePlayer.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);

        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;

        float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float f1 = -MathHelper.sin(pitch * 0.017453292F);
        float f2 = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float f3 = (float) Math.sqrt(f * f + f1 * f1 + f2 * f2);

        float gravity = 0.03f;
        float drag = 0.99f;

        if (item instanceof ItemBow) {
            int useCount = mc.thePlayer.getItemInUseMaxCount();
            if (useCount <= 0) return;
            float force = (float)useCount / 20.0F;
            force = (force * force + force * 2.0F) / 3.0F;
            if (force < 0.1F) return;
            if (force > 1.0F) force = 1.0F;
            
            float velocity = force * 2.0f * 1.5f;
            motionX = f / f3 * velocity;
            motionY = f1 / f3 * velocity;
            motionZ = f2 / f3 * velocity;
            gravity = 0.05f;
        } else if (item == Items.ENDER_PEARL || item == Items.SNOWBALL || item == Items.EGG) {
            float velocity = 1.5f;
            motionX = f / f3 * velocity;
            motionY = f1 / f3 * velocity;
            motionZ = f2 / f3 * velocity;
            gravity = 0.03f;
        } else if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
            float velocity = 0.5f;
            motionX = f / f3 * velocity;
            motionY = f1 / f3 * velocity;
            motionZ = f2 / f3 * velocity;
            gravity = 0.05f;
        } else {
            return;
        }

        // Simulation
        List<Vec3d> trajectory = new ArrayList<>();
        RayTraceResult hit = null;

        for (int i = 0; i < 300; i++) {
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

        // Rendu
        renderTrajectory(trajectory, partialTicks);
        
        if (hit != null && showImpact.get()) {
            RenderUtils.drawBlockBox(new BlockPos(hit.hitVec), partialTicks, new Color((int)red.get(), (int)green.get(), (int)blue.get(), 150).getRGB());
        }
    }

    private void renderTrajectory(List<Vec3d> points, float partialTicks) {
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
        GL11.glLineWidth(2.0f);

        float r = (float) (red.get() / 255.0);
        float g = (float) (green.get() / 255.0);
        float b = (float) (blue.get() / 255.0);

        GlStateManager.color(r, g, b, 1.0f);
        
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
