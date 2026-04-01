package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.ModeSetting;
import com.puff.core.settings.NumberSetting;
import com.puff.util.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.Color;

public class ESP extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Box 3D", "Box 3D", "Box 2D");
    private final NumberSetting range = new NumberSetting("Range", 100.0, 10.0, 500.0, 10.0);
    private final BooleanSetting tracers = new BooleanSetting("Tracers", true);
    private final BooleanSetting boxes = new BooleanSetting("Boxes", true);
    private final BooleanSetting distance = new BooleanSetting("Distance", true);
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", true);
    private final BooleanSetting teamCheck = new BooleanSetting("TeamCheck", true);

    public ESP() {
        super("ESP", Category.VISUAL);
        registerSetting(mode);
        registerSetting(range);
        registerSetting(tracers);
        registerSetting(boxes);
        registerSetting(distance);
        registerSetting(players);
        registerSetting(mobs);
        registerSetting(animals);
        registerSetting(teamCheck);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onRender3D(float partialTicks) {
        if (mc.theWorld == null) return;

        int pink = new Color(255, 40, 180, 150).getRGB();
        int green = new Color(50, 255, 50, 150).getRGB();
        int red = new Color(255, 50, 50, 150).getRGB();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;
            
            int color = pink;
            boolean shouldRender = false;

            if (entity instanceof EntityPlayer && players.getValue()) {
                if (teamCheck.getValue() && mc.thePlayer.isOnSameTeam(entity)) {
                    // Ignore les alliés
                } else {
                    shouldRender = true;
                    color = pink;
                }
            } else if (entity instanceof net.minecraft.entity.monster.IMob && mobs.getValue()) {
                shouldRender = true;
                color = red;
            } else if ((entity instanceof net.minecraft.entity.passive.EntityAnimal || entity instanceof net.minecraft.entity.passive.IAnimals) && animals.getValue()) {
                shouldRender = true;
                color = green;
            }

            if (!shouldRender) continue;

            double dist = mc.thePlayer.getDistanceToEntity(entity);
            if (dist > range.getValue()) continue;

            if (boxes.getValue()) {
                if (mode.getValue().equals("Box 3D")) {
                    RenderUtils.drawESPBox(entity, partialTicks, color);
                } else {
                    RenderUtils.draw2DBox(entity, partialTicks, color);
                }
            }

            if (tracers.getValue()) {
                RenderUtils.drawTracerLine(entity, partialTicks, color);
            }

            if (distance.getValue()) {
                String distStr = String.format("%.1fm", dist);
                RenderUtils.drawEntityInfo(entity, partialTicks, distStr, -1);
            }
        }
    }
}
