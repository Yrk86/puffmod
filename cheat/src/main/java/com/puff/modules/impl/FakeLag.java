package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.network.PacketEvent;
import com.puff.core.settings.NumberSetting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.ArrayList;
import java.util.List;

public class FakeLag extends Module {

    private final NumberSetting delay = new NumberSetting("Delay", 200, 50, 1000, 10);
    private final List<Packet<?>> packets = new ArrayList<>();
    private long lastSendTime = System.currentTimeMillis();
    private boolean sending = false;

    public FakeLag() {
        super("FakeLag", Category.MISC);
        registerSetting(delay);
    }

    @Override
    public void onEnable() {
        packets.clear();
        lastSendTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        sendPackets();
    }

    @Override
    public void onTick() {
        if (System.currentTimeMillis() - lastSendTime >= delay.get()) {
            sendPackets();
            lastSendTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onPacketSend(PacketEvent event) {
        if (sending || !isEnabled()) return;

        Packet<?> p = event.getPacket();
        
        // On intercepte principalement les paquets de mouvement
        if (p instanceof CPacketPlayer) {
            packets.add(p);
            event.cancel();
        }
    }

    private void sendPackets() {
        if (packets.isEmpty() || mc.thePlayer == null || mc.thePlayer.connection == null) {
            packets.clear();
            return;
        }

        sending = true;
        for (Packet<?> p : packets) {
            mc.thePlayer.connection.sendPacket(p);
        }
        sending = false;
        packets.clear();
    }
}
