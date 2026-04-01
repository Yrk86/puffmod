package mc.red.notification;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import mc.red.mods.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public final class NotificationManager {

    private static final NotificationManager INSTANCE = new NotificationManager();
    private static final List<Notification> QUEUE = new LinkedList<>();
    private static boolean registered = false;

    private NotificationManager() {
    }

    public static void init() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            registered = true;
        }
    }

    public static void notifyToggle(Mod mod) {
        String text = mod.name + (mod.isEnabled() ? " enabled" : " disabled");
        QUEUE.add(new Notification(text, mod.isEnabled(), 2000));
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;
        int margin = 10;
        int y = margin;
        Iterator<Notification> iterator = QUEUE.iterator();
        while (iterator.hasNext()) {
            Notification notification = iterator.next();
            if (notification.isExpired()) {
                iterator.remove();
                continue;
            }
            int width = fr.getStringWidth(notification.text) + 20;
            int x = sr.getScaledWidth() - width - margin;
            int bgColor = notification.enabled ? 0xCCFF2F92 : 0xCC2B1C2C;
            int borderColor = 0x80FF2F92;
            Gui.drawRect(x - 2, y - 2, x + width + 2, y + 18, borderColor);
            Gui.drawRect(x, y, x + width, y + 16, 0xAA0B0B12);
            fr.drawString(notification.text, x + 8, y + 4, 0xFFFFFFFF);
            y += 24;
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.END && QUEUE.size() > 20) {
            QUEUE.remove(0);
        }
    }
}
