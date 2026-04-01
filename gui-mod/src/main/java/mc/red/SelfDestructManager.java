package mc.red;

import java.util.List;

import mc.red.mods.Mod;
import mc.red.mods.ModInstances;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public final class SelfDestructManager {

    private static boolean active = false;

    private SelfDestructManager() {
    }

    public static void activate() {
        activate(false);
    }

    public static void activate(boolean silent) {
        if (active) {
            return;
        }
        active = true;
        List<Mod> mods = ModInstances.getAllMods();
        for (Mod mod : mods) {
            mod.setEnabled(false);
        }
        if (!silent) {
            sendChat("Self-destruct enabled. Type .restore to reactivate.");
        }
    }

    public static void restore() {
        restore(false);
    }

    public static void restore(boolean silent) {
        if (!active) {
            return;
        }
        active = false;
        if (!silent) {
            sendChat("PuffMod restored.");
        }
    }

    public static boolean isActive() {
        return active;
    }

    private static void sendChat(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new TextComponentString(message));
        }
    }
}
