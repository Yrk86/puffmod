package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.network.PacketEvent;
import com.puff.core.settings.NumberSetting;
import com.puff.core.settings.BooleanSetting;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

import java.lang.reflect.Field;

public class Velocity extends Module {

    private final NumberSetting horizontal = new NumberSetting("Horizontal", 90.0, 0.0, 100.0, 1.0);
    private final NumberSetting vertical = new NumberSetting("Vertical", 100.0, 0.0, 100.0, 1.0);
    private final BooleanSetting randomizer = new BooleanSetting("Randomize", true);

    public Velocity() {
        super("Velocity", Category.COMBAT);
        registerSetting(horizontal);
        registerSetting(vertical);
        registerSetting(randomizer);
    }

    public double getHorizontal() {
        return horizontal.getValue();
    }

    public double getVertical() {
        return vertical.getValue();
    }

    @Override
    public void onEnable() {}
    @Override
    public void onDisable() {}

    @Override
    public void onTick() {
        // Backup method: hurtTime
        if (mc.thePlayer != null && mc.thePlayer.hurtTime == mc.thePlayer.maxHurtTime && mc.thePlayer.maxHurtTime > 0) {
            double h = horizontal.getValue() / 100.0;
            double v = vertical.getValue() / 100.0;
            
            if (randomizer.get()) {
                h += (new java.util.Random().nextDouble() - 0.5) * 0.1; // ±5%
            }
            
            mc.thePlayer.motionX *= h;
            mc.thePlayer.motionZ *= h;
            mc.thePlayer.motionY *= v;
        }
    }

    @Override
    public void onPacketReceive(PacketEvent event) {
        if (mc.thePlayer == null || event.isCancelled()) return;

        double h = horizontal.getValue();
        double v = vertical.getValue();

        if (event.getPacket() instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                if (h == 0.0 && v == 0.0) {
                    event.cancel();
                    return;
                }
                
                if (randomizer.get()) {
                    h += (new java.util.Random().nextDouble() - 0.5) * 10.0; // ±5% CPS? non, ici c'est des % de vitesse
                }
                
                modPacket(packet, h, v);
            }
        } else if (event.getPacket() instanceof SPacketExplosion) {
            modExplosion((SPacketExplosion) event.getPacket(), h, v);
        }
    }

    private void modPacket(SPacketEntityVelocity packet, double h, double v) {
        int counter = 0;
        for (Field field : SPacketEntityVelocity.class.getDeclaredFields()) {
            if (field.getType() == int.class) {
                try {
                    field.setAccessible(true);
                    if (counter == 1) field.setInt(packet, (int) (field.getInt(packet) * (h / 100.0)));
                    else if (counter == 2) field.setInt(packet, (int) (field.getInt(packet) * (v / 100.0)));
                    else if (counter == 3) field.setInt(packet, (int) (field.getInt(packet) * (h / 100.0)));
                    counter++;
                } catch (Exception ignored) {}
            }
        }
    }

    private void modExplosion(SPacketExplosion packet, double h, double v) {
        int counter = 0;
        for (Field field : SPacketExplosion.class.getDeclaredFields()) {
            if (field.getType() == float.class) {
                try {
                    field.setAccessible(true);
                    if (counter == 0) field.setFloat(packet, (float) (field.getFloat(packet) * (h / 100.0)));
                    else if (counter == 1) field.setFloat(packet, (float) (field.getFloat(packet) * (v / 100.0)));
                    else if (counter == 2) field.setFloat(packet, (float) (field.getFloat(packet) * (h / 100.0)));
                    counter++;
                } catch (Exception ignored) {}
            }
        }
    }
}
