package com.puff.modules.impl;

import net.minecraft.network.play.server.SPacketEntityVelocity;
public class TestSpeed {
    public static void main(String[] args) {
        SPacketEntityVelocity p = new SPacketEntityVelocity();
        int mx = p.getMotionX();
    }
}
