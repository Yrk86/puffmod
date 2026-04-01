package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.NumberSetting;
import com.puff.core.settings.BooleanSetting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Mouse;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class AutoClicker extends Module {

    private final NumberSetting minCps = new NumberSetting("Min CPS", 8.0, 1.0, 20.0, 1.0);
    private final NumberSetting maxCps = new NumberSetting("Max CPS", 12.0, 1.0, 20.0, 1.0);
    private final BooleanSetting leftClick = new BooleanSetting("Left Click", true);
    private final BooleanSetting rightClick = new BooleanSetting("Right Click", false);
    private final BooleanSetting jitter = new BooleanSetting("Jitter", false);
    
    private long lastLeftClickTime;
    private long lastRightClickTime;
    private long nextLeftDelay = 100;
    private long nextRightDelay = 100;
    
    // Variables pour le drift naturel
    private double currentLeftCps = 10.0;
    private double currentRightCps = 10.0;
    
    private double leftCpsTarget = 10.0;
    private double rightCpsTarget = 10.0;
    
    private final Random random = new Random();
    
    private final Deque<Long> lClicks = new ArrayDeque<>();
    private final Deque<Long> rClicks = new ArrayDeque<>();

    public AutoClicker() {
        super("AutoClicker", Category.COMBAT);
        registerSetting(minCps);
        registerSetting(maxCps);
        registerSetting(leftClick);
        registerSetting(rightClick);
        registerSetting(jitter);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    private long generateDelay(boolean left) {
        double min = Math.min(minCps.getValue(), maxCps.getValue());
        double max = Math.max(minCps.getValue(), maxCps.getValue());
        if (min == max) return (long) (1000.0 / Math.max(min, 1.0));
        
        if (left) {
            // Un coup de temps en temps (1 chance sur 4), on définit un nouvel objectif de vitesse CPS (Cible)
            if (random.nextInt(4) == 0) {
                leftCpsTarget = min + random.nextDouble() * (max - min);
            }
            // Le CPS actuel glisse (s'approche doucement) vers la cible
            currentLeftCps += (leftCpsTarget - currentLeftCps) * 0.4;
            
            // Borne de sécurité
            currentLeftCps = Math.max(min, Math.min(max, currentLeftCps));
            return (long) (1000.0 / Math.max(currentLeftCps, 1.0));
        } else {
            if (random.nextInt(4) == 0) {
                rightCpsTarget = min + random.nextDouble() * (max - min);
            }
            currentRightCps += (rightCpsTarget - currentRightCps) * 0.4;
            currentRightCps = Math.max(min, Math.min(max, currentRightCps));
            return (long) (1000.0 / Math.max(currentRightCps, 1.0));
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null || mc.currentScreen != null) return;
        if (event.phase != TickEvent.Phase.END) return;

        boolean isClicking = false;

        // Left Click (Bouton d'attaque)
        if (leftClick.getValue() && Mouse.isButtonDown(0)) {
            if (System.currentTimeMillis() - lastLeftClickTime >= nextLeftDelay) {
                lastLeftClickTime = System.currentTimeMillis();
                nextLeftDelay = generateDelay(true);
                try {
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                    lClicks.addLast(System.currentTimeMillis());
                    isClicking = true;
                } catch (Exception e) {}
            }
        }

        // Right Click (Bouton d'utilisation / FastPlace)
        if (rightClick.getValue() && Mouse.isButtonDown(1)) {
            if (System.currentTimeMillis() - lastRightClickTime >= nextRightDelay) {
                lastRightClickTime = System.currentTimeMillis();
                nextRightDelay = generateDelay(false);
                try {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    rClicks.addLast(System.currentTimeMillis());
                    isClicking = true;
                } catch (Exception e) {}
            }
        }

        // Jitter effect (Mouvements de caméra) si on a cliqué et qu'une souris est maintenue
        if (jitter.getValue() && (Mouse.isButtonDown(0) || Mouse.isButtonDown(1))) {
            float yawJitter = (random.nextFloat() - 0.5f) * 1.5f;
            float pitchJitter = (random.nextFloat() - 0.5f) * 1.5f;
            mc.thePlayer.rotationYaw += yawJitter;
            mc.thePlayer.rotationPitch += pitchJitter;
        }

        long cutoff = System.currentTimeMillis() - 1000L;
        while (!lClicks.isEmpty() && lClicks.getFirst() < cutoff) lClicks.removeFirst();
        while (!rClicks.isEmpty() && rClicks.getFirst() < cutoff) rClicks.removeFirst();
    }

    // Getters utilisés par le HUD
    public int getCpsValue(boolean left) {
        return left ? lClicks.size() : rClicks.size();
    }

    public boolean isLeftEnabled() {
        return leftClick.getValue();
    }

    public boolean isRightEnabled() {
        return rightClick.getValue();
    }
}
