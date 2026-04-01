package mc.red.Gui;

import mc.red.SelfDestructManager;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.Minecraft;

public class GuiChatInterceptor extends GuiChat {

    public GuiChatInterceptor() {
        super();
    }

    public GuiChatInterceptor(String defaultText) {
        super(defaultText);
    }

    @Override
    public void sendChatMessage(String msg, boolean addToChat) {
        if (msg == null) {
            return;
        }
        String trimmed = msg.trim();
        if (trimmed.equalsIgnoreCase(".restore")) {
            SelfDestructManager.restore(true);
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }
        if (trimmed.equalsIgnoreCase(".selfdestruct")) {
            SelfDestructManager.activate(true);
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }
        super.sendChatMessage(msg, addToChat);
    }
}
