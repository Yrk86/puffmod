package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.util.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;

public class ItemESP extends Module {

    private final BooleanSetting box = new BooleanSetting("Box", true);
    private final BooleanSetting nameTags = new BooleanSetting("NameTags", true);

    public ItemESP() {
        super("ItemESP", Category.VISUAL);
        registerSetting(box);
        registerSetting(nameTags);
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (!isEnabled() || mc.theWorld == null) return;

        for (net.minecraft.entity.Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityItem) {
                EntityItem item = (EntityItem) entity;
                BlockPos pos = item.getPosition();
                
                // Rendu de la boîte 3D à la taille réelle de l'entité
                if (box.get()) {
                    RenderUtils.drawEntityBox(item, partialTicks, new java.awt.Color(0, 255, 100, 150).getRGB());
                }

                // Rendu du NameTag (simplifié pour commencer)
                if (nameTags.get()) {
                    String name = item.getEntityItem().getDisplayName();
                    if (item.getEntityItem().stackSize > 1) {
                        name += " x" + item.getEntityItem().stackSize;
                    }
                    renderNameTag(item, name, partialTicks);
                }
            }
        }
    }

    private void renderNameTag(EntityItem item, String name, float partialTicks) {
        double x = item.lastTickPosX + (item.posX - item.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = item.lastTickPosY + (item.posY - item.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY + 0.5;
        double z = item.lastTickPosZ + (item.posZ - item.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        float scale = 0.02F;

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
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        int width = mc.fontRendererObj.getStringWidth(name) / 2;
        // Fond sombre derrière le texte
        net.minecraft.client.gui.Gui.drawRect(-width - 2, -1, width + 2, 8, 0x80000000);
        mc.fontRendererObj.drawStringWithShadow(name, -width, 0, -1);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}
