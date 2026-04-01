package com.puff;
 
import com.puff.core.ModuleManager;
import com.puff.modules.impl.AimAssist;
import com.puff.modules.impl.Arrows;
import com.puff.modules.impl.Reach;
import com.puff.modules.impl.HitSelect;
import com.puff.modules.impl.WTap;
import com.puff.modules.impl.SilentAura;
import com.puff.modules.impl.Search;
import com.puff.modules.impl.StorageESP;
import com.puff.modules.impl.Sprint;
import com.puff.modules.impl.Fullbright;
import com.puff.modules.impl.FastPlace;
import com.puff.modules.impl.ItemESP;
import com.puff.modules.impl.AutoClicker;
import com.puff.modules.impl.Velocity;
import com.puff.modules.impl.ChestStealer;
import com.puff.modules.impl.ESP;
import com.puff.modules.impl.HudOverlay;
import com.puff.modules.impl.NameTags;
import com.puff.modules.impl.Scaffold;
import com.puff.modules.impl.FakeLag;
import com.puff.modules.impl.AntiDebuff;
import com.puff.modules.impl.PropHunt;
import com.puff.modules.impl.Projectiles;
import com.puff.modules.impl.Trajectories;
import com.puff.modules.impl.Chams;
import com.puff.modules.impl.SpawnerFinder;
import com.puff.ui.ClickGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
 
/**
 * Puff V0.1 - Framework Modulaire Expert (Forge 1.9.4)
 */
@Mod(modid = PuffMod.MODID, name = PuffMod.NAME, version = PuffMod.VERSION)
public class PuffMod {
 
    public static final String MODID = "puff";
    public static final String NAME = "Puff";
    public static final String VERSION = "0.1";
 
    private static final Logger LOGGER = LogManager.getLogger(NAME);
    private static ModuleManager moduleManager;
 
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide() != Side.CLIENT) return;
 
        log("Initialisation des Mixins Puff...");
        MixinBootstrap.init();
        Mixins.addConfiguration("puff.mixins.json");
 
        moduleManager = new ModuleManager();
    }
 
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() != Side.CLIENT) return;
 
        log("Démarrage du moteur Puff...");
 
        // Enregistrement des modules
        moduleManager.register(
                new AimAssist(),
                new Arrows(),
                new Reach(),
                new HitSelect(),
                new WTap(),
                new SilentAura(),
                new Search(),
                new StorageESP(),
                new Sprint(),
                new Fullbright(),
                new FastPlace(),
                new AutoClicker(),
                new Velocity(),
                new ChestStealer(),
                new ESP(),
                new ItemESP(),
                new NameTags(),
                new FakeLag(),
                new Scaffold(),
                new Trajectories(),
                new Projectiles(),
                new AntiDebuff(),
                new PropHunt(),
                new Chams(),
                new SpawnerFinder(),
                new HudOverlay()
        );

        moduleManager.enableModule(HudOverlay.class);
 
        // Enregistrement des événements Forge
        MinecraftForge.EVENT_BUS.register(this);
 
        log("Puff prêt. (RSHIFT pour le menu)");
    }
 
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && moduleManager != null) {
            moduleManager.onClientTick();
        }
    }
 
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (moduleManager != null) {
            moduleManager.onRender3D(event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_RSHIFT) {
            Minecraft.getMinecraft().displayGuiScreen(new ClickGui());
        }
    }
 
    public static ModuleManager getModuleManager() {
        return moduleManager;
    }
 
    public static void log(String message) {
        LOGGER.info("[" + NAME + "] " + message);
    }
}
