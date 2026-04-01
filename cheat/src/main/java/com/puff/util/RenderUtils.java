package com.puff.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtils {

    public static void drawRoundedRect(float x, float y, float x1, float y1, float radius, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);

        glBegin(GL_POLYGON);
        for (int i = 0; i <= 90; i += 3) glVertex2d(x + radius + Math.sin(Math.toRadians(i - 90)) * radius, y + radius + Math.cos(Math.toRadians(i - 90)) * radius);
        for (int i = 90; i <= 180; i += 3) glVertex2d(x + radius + Math.sin(Math.toRadians(i - 90)) * radius, y1 - radius + Math.cos(Math.toRadians(i - 90)) * radius);
        for (int i = 180; i <= 270; i += 3) glVertex2d(x1 - radius + Math.sin(Math.toRadians(i - 90)) * radius, y1 - radius + Math.cos(Math.toRadians(i - 90)) * radius);
        for (int i = 270; i <= 360; i += 3) glVertex2d(x1 - radius + Math.sin(Math.toRadians(i - 90)) * radius, y + radius + Math.cos(Math.toRadians(i - 90)) * radius);
        glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRoundedOutline(float x, float y, float x1, float y1, float radius, float lineWidth, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        GL11.glLineWidth(lineWidth);

        glBegin(GL_LINE_LOOP);
        for (int i = 0; i <= 90; i += 3) glVertex2d(x + radius + Math.sin(Math.toRadians(i - 90)) * radius, y + radius + Math.cos(Math.toRadians(i - 90)) * radius);
        for (int i = 90; i <= 180; i += 3) glVertex2d(x + radius + Math.sin(Math.toRadians(i - 90)) * radius, y1 - radius + Math.cos(Math.toRadians(i - 90)) * radius);
        for (int i = 180; i <= 270; i += 3) glVertex2d(x1 - radius + Math.sin(Math.toRadians(i - 90)) * radius, y1 - radius + Math.cos(Math.toRadians(i - 90)) * radius);
        for (int i = 270; i <= 360; i += 3) glVertex2d(x1 - radius + Math.sin(Math.toRadians(i - 90)) * radius, y + radius + Math.cos(Math.toRadians(i - 90)) * radius);
        glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (startColor >> 24 & 255) / 255.0F;
        float f1 = (startColor >> 16 & 255) / 255.0F;
        float f2 = (startColor >> 8 & 255) / 255.0F;
        float f3 = (startColor & 255) / 255.0F;
        float f4 = (endColor >> 24 & 255) / 255.0F;
        float f5 = (endColor >> 16 & 255) / 255.0F;
        float f6 = (endColor >> 8 & 255) / 255.0F;
        float f7 = (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(right, top, 0.0D).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(left, top, 0.0D).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(left, bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.pos(right, bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawSmallString(String text, float x, float y, int color, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        net.minecraft.client.Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, 0, 0, color);
        GlStateManager.popMatrix();
    }

    public static void drawCenteredString(String text, float x, float y, int color) {
        net.minecraft.client.gui.FontRenderer fr = net.minecraft.client.Minecraft.getMinecraft().fontRendererObj;
        fr.drawStringWithShadow(text, x - (float)fr.getStringWidth(text) / 2, y, color);
    }

    public static void drawTracerLine(net.minecraft.entity.Entity entity, float partialTicks, int color) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        GL11.glLineWidth(1.5F);
        GL11.glColor4f(f1, f2, f3, f);
        
        glBegin(GL_LINES);
        glVertex3d(0, 0, 0);
        glVertex3d(x, y + entity.getEyeHeight(), z);
        glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawESPBox(net.minecraft.entity.Entity entity, float partialTicks, int color) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        float f = (color >> 24 & 255) / 255.0F;

        net.minecraft.util.math.AxisAlignedBB bb = entity.getEntityBoundingBox().offset(-entity.posX, -entity.posY, -entity.posZ).offset(x, y, z);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(f1, f2, f3, f);
        
        // Dessin de la boîte (fil de fer)
        net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox(bb);

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void draw2DBox(net.minecraft.entity.Entity entity, float partialTicks, int color) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        float f = (color >> 24 & 255) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2.0, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        
        float scale = 0.025F;
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(f1, f2, f3, f);
        
        float w = entity.width * 20.0F;
        float h = entity.height * 20.0F;
        
        glBegin(GL_LINE_LOOP);
        glVertex2f(-w, -h);
        glVertex2f(w, -h);
        glVertex2f(w, h);
        glVertex2f(-w, h);
        glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawEntityInfo(net.minecraft.entity.Entity entity, float partialTicks, String text, int color) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height + 0.5, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        
        float scale = 0.025F;
        GlStateManager.scale(-scale, -scale, scale);
        
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int width = mc.fontRendererObj.getStringWidth(text) / 2;
        mc.fontRendererObj.drawStringWithShadow(text, -width, 0, color);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawBlockBox(net.minecraft.util.math.BlockPos pos, float partialTicks, int color) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        double x = pos.getX() - mc.getRenderManager().viewerPosX;
        double y = pos.getY() - mc.getRenderManager().viewerPosY;
        double z = pos.getZ() - mc.getRenderManager().viewerPosZ;

        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        float f = (color >> 24 & 255) / 255.0F;

        net.minecraft.util.math.AxisAlignedBB bb = new net.minecraft.util.math.AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(f1, f2, f3, f);
        
        net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox(bb);

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawEntityBox(net.minecraft.entity.Entity entity, float partialTicks, int color) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();

        double ix = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double iy = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double iz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        // Boîte fixe de 0.3x0.3x0.3 centrée sur le centre visuel de l'item
        // L'item flotte à environ 0.15 bloc au-dessus de sa posY
        double half = 0.15;
        double centerY = iy + 0.15; // centre visuel approximatif

        net.minecraft.util.math.AxisAlignedBB renderBB = new net.minecraft.util.math.AxisAlignedBB(
                ix - half, centerY - half, iz - half,
                ix + half, centerY + half, iz + half
        );

        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8  & 255) / 255.0F;
        float f3 = (color       & 255) / 255.0F;
        float f  = (color >> 24 & 255) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glLineWidth(1.5F);
        GL11.glColor4f(f1, f2, f3, f);
        net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox(renderBB);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
