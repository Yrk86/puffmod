package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class Chams extends Module {

    private final BooleanSetting visibleOnly = new BooleanSetting("VisibleOnly", false);
    private final BooleanSetting throughWalls = new BooleanSetting("ThroughWalls", true);
    private final NumberSetting red = new NumberSetting("Red", 255, 0, 255, 5);
    private final NumberSetting green = new NumberSetting("Green", 100, 0, 255, 5);
    private final NumberSetting blue = new NumberSetting("Blue", 100, 0, 255, 5);
    private final NumberSetting alpha = new NumberSetting("Alpha", 150, 0, 255, 5);

    public Chams() {
        super("Chams", Category.VISUAL);
        registerSetting(visibleOnly);
        registerSetting(throughWalls);
        registerSetting(red);
        registerSetting(green);
        registerSetting(blue);
        registerSetting(alpha);
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!isEnabled() || event.getEntityPlayer() == mc.thePlayer) return;

        if (throughWalls.get()) {
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0F, -1000000.0F);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            
            // Couleur à travers les murs
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            int r = (int) red.get();
            int g = (int) green.get();
            int b = (int) blue.get();
            int a = (int) alpha.get();
            
            GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f);
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (!isEnabled() || event.getEntityPlayer() == mc.thePlayer) return;

        if (throughWalls.get()) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GlStateManager.disablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0F, 1000000.0F);
            
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.color(1f, 1f, 1f, 1f);
        }
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}
