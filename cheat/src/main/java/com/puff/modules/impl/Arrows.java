package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class Arrows extends Module {

    private final NumberSetting radius = new NumberSetting("Radius", 45.0, 10.0, 200.0, 5.0);
    private final NumberSetting size = new NumberSetting("Size", 10.0, 5.0, 25.0, 1.0);
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);
    private final BooleanSetting teamCheck = new BooleanSetting("TeamCheck", true);

    public Arrows() {
        super("Arrows", Category.VISUAL);
        registerSetting(radius);
        registerSetting(size);
        registerSetting(players);
        registerSetting(mobs);
        registerSetting(animals);
        registerSetting(teamCheck);
    }

    @Override
    public void onEnable() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this);
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void onRender2D(net.minecraftforge.client.event.RenderGameOverlayEvent.Text event) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        float cx = sr.getScaledWidth() / 2.0F;
        float cy = sr.getScaledHeight() / 2.0F;

        for (net.minecraft.entity.Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer || entity.isInvisible() || !entity.isEntityAlive()) continue;

            boolean shouldShow = false;
            if (entity instanceof EntityPlayer && players.get()) {
                if (teamCheck.get() && mc.thePlayer.isOnSameTeam(entity)) continue;
                shouldShow = true;
            } else if (entity instanceof net.minecraft.entity.monster.IMob && mobs.get()) {
                shouldShow = true;
            } else if ((entity instanceof net.minecraft.entity.passive.EntityAnimal || entity instanceof net.minecraft.entity.passive.IAnimals) && animals.get()) {
                shouldShow = true;
            }

            if (!shouldShow) continue;

            // Calcul de l'angle relatif
            double dx = entity.posX - mc.thePlayer.posX;
            double dz = entity.posZ - mc.thePlayer.posZ;
            
            // Angle absolu vers le joueur
            double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
            // Angle relatif à la vue du joueur local
            double relativeAngle = angle - mc.thePlayer.rotationYaw;

            // Si le joueur est dans le FOV (approximativement), on ne dessine pas de flèche ?
            // Ou on dessine tout le temps s'il est hors écran.
            // Pour l'instant, on dessine tout le temps pour tester.

            drawArrow(cx, cy, (float) relativeAngle, (float) radius.get(), (float) size.get());
        }
    }

    private void drawArrow(float cx, float cy, float angle, float radius, float size) {
        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, 0);
        GL11.glRotatef(angle, 0, 0, 1);
        GL11.glTranslatef(0, -radius, 0);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Couleur rose Puff (Alpha 150)
        GL11.glColor4f(1.0F, 0.15F, 0.7F, 0.6F);

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(0, -size);
        GL11.glVertex2f(-size / 2.0F, 0);
        GL11.glVertex2f(size / 2.0F, 0);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
