package mc.red;

import java.io.File;

import org.lwjgl.input.Keyboard;

import java.io.File;

import org.lwjgl.input.Keyboard;

import mc.red.Gui.ClickGui.ClickGui;
import mc.red.Gui.GuiChatInterceptor;
import mc.red.Gui.LoginGui;
import mc.red.SelfDestructManager;
import mc.red.config.ConfigManager;
import mc.red.mods.ModInstances;
import mc.red.notification.NotificationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = PuffModV1Mod.MODID, name = "PuffMod V1", version = PuffModV1Mod.VERSION, acceptedMinecraftVersions = "[1.9.4]")
public class PuffModV1Mod {
    public static final String MODID = "puffmodv1";
    public static final String VERSION = "0.1.0";

    private static final String DEFAULT_KEY = "puffmodv1";
    private KeyBinding openGui;
    private LoginGui loginGui;
    private File keyFile;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        openGui = new KeyBinding("Open PuffMod V1", Keyboard.KEY_RSHIFT, "PuffMod V1");
        ClientRegistry.registerKeyBinding(openGui);
        MinecraftForge.EVENT_BUS.register(this);
        File configDir = new File(event.getModConfigurationDirectory(), ".cache");
        ConfigManager.init(configDir);
        NotificationManager.init();
        this.keyFile = new File(configDir, ".auth/key.dat");
        this.loginGui = new LoginGui(this.keyFile, DEFAULT_KEY);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // No-op; kept for compatibility with older Forge lifecycle expectations.
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (openGui.isPressed() && !SelfDestructManager.isActive()) {
            if (loginGui.hasStoredKey()) {
                Minecraft.getMinecraft().displayGuiScreen(new ClickGui());
            } else {
                Minecraft.getMinecraft().displayGuiScreen(loginGui);
            }
        }
        if (Keyboard.getEventKeyState() && Minecraft.getMinecraft().currentScreen == null) {
            int key = Keyboard.getEventKey();
            if (key != Keyboard.KEY_NONE) {
                handleModKeyPress(key);
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiChat && !(event.getGui() instanceof GuiChatInterceptor)) {
            event.setGui(new GuiChatInterceptor());
        }
    }

    private void handleModKeyPress(int keyCode) {
        if (SelfDestructManager.isActive()) {
            return;
        }
        for (mc.red.mods.Mod mod : ModInstances.getAllMods()) {
            if (mod.getKeyCode() == keyCode && keyCode >= 0) {
                mod.setEnabled(!mod.isEnabled());
                NotificationManager.notifyToggle(mod);
            }
        }
    }

}
