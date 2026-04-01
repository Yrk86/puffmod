package com.puff.core.network;

import net.minecraft.network.Packet;

/**
 * Event envoyé pour chaque packet réseau.
 * Ici on se concentre sur les packets sortants (vers le serveur).
 */
public class PacketEvent {

    public enum PacketDirection {
        OUTGOING,
        INCOMING
    }

    private final PacketDirection direction;
    private Packet<?> packet;
    private boolean cancelled;

    public PacketEvent(PacketDirection direction, Packet<?> packet) {
        this.direction = direction;
        this.packet = packet;
        this.cancelled = false;
    }

    public PacketDirection getDirection() {
        return direction;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    /**
     * Permet de remplacer le packet envoyé (si le mixin applique la substitution).
     */
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}

