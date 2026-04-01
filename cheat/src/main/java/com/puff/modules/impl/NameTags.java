package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.Gui;

import java.awt.Color;

public class NameTags extends Module {

    private final BooleanSetting showHealth = new BooleanSetting("Health", true);
    private final BooleanSetting showDistance = new BooleanSetting("Distance", true);
    private final BooleanSetting teamCheck = new BooleanSetting("TeamCheck", true);

    public NameTags() {
        super("NameTags", Category.VISUAL);
        registerSetting(showHealth);
        registerSetting(showDistance);
        registerSetting(teamCheck);
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (!isEnabled() || mc.theWorld == null || mc.thePlayer == null) return;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer) continue;
            if (teamCheck.get() && mc.thePlayer.isOnSameTeam(player)) continue;

            renderNameTag(player, partialTicks);
        }
    }

    private void renderNameTag(EntityPlayer player, float partialTicks) {
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY + player.height + 0.3;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        double dist = mc.thePlayer.getDistance(player.posX, player.posY, player.posZ);
        float scale = (float) Math.max(0.02, 0.02 * dist / 5.0);

        // Nom du joueur
        String name = player.getName();
        // HP
        int hp = (int) Math.ceil(player.getHealth());
        int maxHp = (int) Math.ceil(player.getMaxHealth());
        // Couleur HP (vert -> jaune -> rouge)
        int hpColor = getHpColor(hp, maxHp);

        // Construction du tag
        String nameStr = player.getName();
        String hpStr = showHealth.get() ? " [" + hp + "/" + maxHp + "❤]" : "";
        String distStr = showDistance.get() ? " " + (int) dist + "m" : "";
        String text = nameStr + hpStr + distStr;
        int width = mc.fontRendererObj.getStringWidth(text) / 2;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // Fond semi-transparent
        Gui.drawRect(-width - 2, -1, width + 2, 10, 0xA0000000);

        // Barre de vie colorée sous le fond
        if (showHealth.get()) {
            int barWidth = (width + 2) * 2;
            int filledWidth = (int) (barWidth * (player.getHealth() / player.getMaxHealth()));
            Gui.drawRect(-width - 2, 10, width + 2, 12, 0xFF333333);
            Gui.drawRect(-width - 2, 10, -width - 2 + filledWidth, 12, hpColor | 0xFF000000);
        }

        mc.fontRendererObj.drawStringWithShadow(text, -(float) width, 0, 0xFFFFFF);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private int getHpColor(int hp, int maxHp) {
        float ratio = (float) hp / maxHp;
        if (ratio > 0.6f) return 0x00FF00;
        if (ratio > 0.3f) return 0xFFAA00;
        return 0xFF0000;
    }

    private String colorCode(int color) {
        return ""; // Pas de format §, couleur déjà appliquée via drawRect
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}
