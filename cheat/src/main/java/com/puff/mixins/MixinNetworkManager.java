package com.puff.mixins;

import com.puff.PuffMod;
import com.puff.core.network.PacketEvent;
import io.netty.channel.Channel;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Hook réseau:
 * intercept `NetworkManager#sendPacket` pour créer un PacketEvent OUTGOING.
 *
 * Les modules peuvent:
 * - annuler l'envoi
 * - remplacer le packet par un autre
 */
@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Shadow
    private Channel channel;

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void vproject$sendPacketHook(Packet<?> packetIn, CallbackInfo ci) {
        if (PuffMod.getModuleManager() == null) return;
        if (packetIn == null) return;

        PacketEvent event = new PacketEvent(PacketEvent.PacketDirection.OUTGOING, packetIn);
        PuffMod.getModuleManager().onOutgoingPacket(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        // Si un module a remplacé le packet, on annule l'appel original
        // puis on écrit le packet modifié directement via la channel.
        if (event.getPacket() != packetIn && event.getPacket() != null) {
            ci.cancel();
            if (channel != null) {
                channel.writeAndFlush(event.getPacket());
            }
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void puff$channelRead0Hook(ChannelHandlerContext context, Packet<?> packetIn, CallbackInfo ci) {
        if (PuffMod.getModuleManager() == null || packetIn == null) return;

        // Debug discret dans la console
        if (packetIn.getClass().getSimpleName().startsWith("SPacket")) {
             // On ne log rien ici pour éviter de flood, mais on sait que ça passe
        }

        PacketEvent event = new PacketEvent(PacketEvent.PacketDirection.INCOMING, packetIn);
        PuffMod.getModuleManager().onIncomingPacket(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}

