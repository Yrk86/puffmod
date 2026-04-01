package com.puff.modules.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.puff.PuffMod;
import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * HUD utilitaire légit : keystrokes, CPS, FPS, ping, reach réel, armor/held
 * item.
 * Enregistré sur l'event bus uniquement quand activé.
 */
public class HudOverlay extends Module {

    private static final String[] AUTO_GG_MESSAGES = {"GG", "GF", "Good Game", "Well played"};
    private static final Pattern[] AUTO_GG_PATTERNS = new Pattern[]{
            Pattern.compile(".*(game over|victory|you win|you survived|winners?|game ended).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b1st\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\bfirst place\\b.*", Pattern.CASE_INSENSITIVE)
    };
    private static final long AUTO_GG_COOLDOWN_MS = 10_000L;
    private static final long AUTO_GG_DELAY_MS = 350L;
    private static final float CHAT_OPACITY_NO_BG = 0.35F; // garde le texte lisible tout en minimisant le fond

    private final BooleanSetting showKeystrokes = new BooleanSetting("Keystrokes", true);
    private final BooleanSetting showCps = new BooleanSetting("CPS", true);
    private final BooleanSetting showFps = new BooleanSetting("FPS", true);
    private final BooleanSetting showPing = new BooleanSetting("Ping", true);
    private final BooleanSetting showReach = new BooleanSetting("Reach", true);
    private final BooleanSetting showArmor = new BooleanSetting("Armor", true);
    private final BooleanSetting toggleSprint = new BooleanSetting("ToggleSprint", true);
    private final BooleanSetting toggleSneak = new BooleanSetting("ToggleSneak", true);
    private final BooleanSetting showCoords = new BooleanSetting("Coords", true);
    private final BooleanSetting showPotions = new BooleanSetting("Potions", true);
    private final BooleanSetting showEntityHearts = new BooleanSetting("EntityHearts", true);
    private final BooleanSetting fullBright = new BooleanSetting("FullBright", false);
    private final BooleanSetting noHurtCam = new BooleanSetting("NoHurtCam", false);
    private final BooleanSetting removeChatBackground = new BooleanSetting("NoChatBackground", false);
    private final BooleanSetting autoGg = new BooleanSetting("AutoGG", false);
    private final NumberSetting autoGgMessageIndex = new NumberSetting("AutoGGMessage", 0, 0,
            AUTO_GG_MESSAGES.length - 1, 1);
    private final BooleanSetting fpsOptimizer = new BooleanSetting("FPS Optimizer", false);

    private final NumberSetting labelX = new NumberSetting("LabelX", 6.0, 0.0, 700.0, 1.0);
    private final NumberSetting labelY = new NumberSetting("LabelY", 6.0, 0.0, 700.0, 1.0);

    private final NumberSetting cpsX = new NumberSetting("CpsX", 6.0, 0.0, 700.0, 1.0);
    private final NumberSetting cpsY = new NumberSetting("CpsY", 30.0, 0.0, 700.0, 1.0);

    private final NumberSetting fpsPingX = new NumberSetting("FpsPingX", 6.0, 0.0, 700.0, 1.0);
    private final NumberSetting fpsPingY = new NumberSetting("FpsPingY", 50.0, 0.0, 700.0, 1.0);
    private final NumberSetting pingX = new NumberSetting("PingX", 6.0, 0.0, 700.0, 1.0);
    private final NumberSetting pingY = new NumberSetting("PingY", 70.0, 0.0, 700.0, 1.0);

    private final NumberSetting reachX = new NumberSetting("ReachX", 6.0, 0.0, 700.0, 1.0);
    private final NumberSetting reachY = new NumberSetting("ReachY", 90.0, 0.0, 700.0, 1.0);

    private final NumberSetting toggleSprintX = new NumberSetting("ToggleSprintX", 6.0, 0.0, 700.0, 1.0);
    private final NumberSetting toggleSprintY = new NumberSetting("ToggleSprintY", 110.0, 0.0, 700.0, 1.0);
    private final NumberSetting toggleSneakX = new NumberSetting("ToggleSneakX", 6.0, 0.0, 700.0, 1.0);
    private final NumberSetting toggleSneakY = new NumberSetting("ToggleSneakY", 126.0, 0.0, 700.0, 1.0);

    private final NumberSetting armorX = new NumberSetting("ArmorX", 6.0, 0.0, 700.0, 1.0);
    private final NumberSetting armorY = new NumberSetting("ArmorY", 145.0, 0.0, 700.0, 1.0);

    private final NumberSetting potionsX = new NumberSetting("PotionsX", 46.0, 0.0, 700.0, 1.0);
    private final NumberSetting potionsY = new NumberSetting("PotionsY", 146.0, 0.0, 700.0, 1.0);

    private final NumberSetting coordsX = new NumberSetting("CoordsX", 86.0, 0.0, 700.0, 1.0);
    private final NumberSetting coordsY = new NumberSetting("CoordsY", 6.0, 0.0, 700.0, 1.0);

    private final NumberSetting keystrokesX = new NumberSetting("KeystrokesX", 627.0, 0.0, 700.0, 1.0);
    private final NumberSetting keystrokesY = new NumberSetting("KeystrokesY", 8.0, 0.0, 700.0, 1.0);
    private float savedGamma = -1f;
    private float savedChatOpacity = -1f;

    // Fps Optimizer snapshot
    private int oldRenderDistance = -1;
    private boolean oldFancy;
    private int oldAmbientOcclusion;
    private boolean oldVsync;
    private boolean oldEntityShadows;
    private int oldLimitFramerate;

    // Styles (couleurs + fond par overlay)
    private int labelColor = 0xFFFF0000;
    private int cpsColor = 0xFFFF0000;
    private int fpsColor = 0xFFFF0000;
    private int pingColor = 0xFFFF0000;
    private int reachColor = 0xFFFF0000;
    private int toggleSprintColor = 0xFFFF0000;
    private int toggleSneakColor = 0xFFFF0000;
    private int armorColor = 0xFFFF0000;
    private int potionsColor = 0xFFFF0000;
    private int coordsColor = 0xFFFF0000;
    private int keystrokesColor = 0xFFFFFFFF;
    private int overlayBgColor = 0xF020202E;
    private boolean labelBg = true;
    private boolean cpsBg = true;
    private boolean fpsBg = true;
    private boolean pingBg = true;
    private boolean reachBg = true;
    private boolean toggleSprintBg = true;
    private boolean toggleSneakBg = true;
    private boolean armorBg = true;
    private boolean potionsBg = true;
    private boolean coordsBg = true;
    private boolean keystrokesBg = true;
    private boolean keystrokesRainbow = false;

    private final NumberSetting scale = new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05);

    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean lastLeft;
    private boolean lastRight;
    private boolean sneakToggled;
    private double lastHitReach;
    private final float[] keyFade = new float[6]; // forward, back, left, right, lmb, rmb
    private boolean physicalLeftDown;
    private boolean physicalRightDown;
    private long lastAutoGg = 0L;

    private final File configFile = new File(mc.mcDataDir, "config/puff-hud.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public HudOverlay() {
        super("HUD", Category.VISUAL);
        registerSetting(showKeystrokes);
        registerSetting(showCps);
        registerSetting(showFps);
        registerSetting(showPing);
        registerSetting(showReach);
        registerSetting(showArmor);
        registerSetting(toggleSprint);
        registerSetting(toggleSneak);
        registerSetting(showCoords);
        registerSetting(showPotions);
        registerSetting(showEntityHearts);
        registerSetting(fullBright);
        registerSetting(noHurtCam);
        registerSetting(removeChatBackground);
        registerSetting(autoGg);
        registerSetting(autoGgMessageIndex);
        registerSetting(fpsOptimizer);
        registerSetting(labelX);
        registerSetting(labelY);
        registerSetting(cpsX);
        registerSetting(cpsY);
        registerSetting(fpsPingX);
        registerSetting(fpsPingY);
        registerSetting(pingX);
        registerSetting(pingY);
        registerSetting(reachX);
        registerSetting(reachY);
        registerSetting(toggleSprintX);
        registerSetting(toggleSprintY);
        registerSetting(toggleSneakX);
        registerSetting(toggleSneakY);
        registerSetting(armorX);
        registerSetting(armorY);
        registerSetting(potionsX);
        registerSetting(potionsY);
        registerSetting(coordsX);
        registerSetting(coordsY);
        registerSetting(keystrokesX);
        registerSetting(keystrokesY);
        registerSetting(scale);
        loadConfig();
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        updateChatBackgroundState();
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        restoreChatOpacity();
        if (mc.thePlayer != null) {
            mc.thePlayer.setSprinting(false);
            mc.thePlayer.setSneaking(false);
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        sneakToggled = false;
        if (savedGamma >= 0f && mc.gameSettings != null) {
            mc.gameSettings.gammaSetting = savedGamma;
        }
        savedGamma = -1f;
        saveConfig();
    }

    @Override
    public void onTick() {
        updatePhysicalButtons();
        trackClicks();
    }

    private void updatePhysicalButtons() {
        physicalLeftDown = Mouse.isButtonDown(0);
        physicalRightDown = Mouse.isButtonDown(1);
    }

    private void handleToggleSprint() {
        if (!toggleSprint.get() || mc.thePlayer == null || mc.theWorld == null)
            return;
        boolean movingForward = mc.thePlayer.moveForward > 0;
        boolean canSprint = mc.thePlayer.getFoodStats().getFoodLevel() > 6
                && !mc.thePlayer.isSneaking()
                && !mc.thePlayer.isCollidedHorizontally;

        if (canSprint && movingForward) {
            mc.thePlayer.setSprinting(true);
        } else if (!movingForward) {
            mc.thePlayer.setSprinting(false);
        }
    }

    private void handleFullBright() {
        if (mc.gameSettings == null)
            return;
        if (fullBright.get()) {
            if (savedGamma < 0f) {
                savedGamma = mc.gameSettings.gammaSetting;
            }
            mc.gameSettings.gammaSetting = 1000f;
        } else if (savedGamma >= 0f) {
            mc.gameSettings.gammaSetting = savedGamma;
            savedGamma = -1f;
        }
    }

    private void handleFpsOptimizer() {
        if (mc.gameSettings == null) return;
        if (fpsOptimizer.get()) {
            if (oldRenderDistance == -1) {
                snapshotFps();
                applyFpsOptimized();
                mc.gameSettings.saveOptions();
            }
        } else if (oldRenderDistance != -1) {
            restoreFps();
            mc.gameSettings.saveOptions();
        }
    }

    private void snapshotFps() {
        oldRenderDistance = mc.gameSettings.renderDistanceChunks;
        oldFancy = mc.gameSettings.fancyGraphics;
        oldAmbientOcclusion = mc.gameSettings.ambientOcclusion;
        oldVsync = mc.gameSettings.enableVsync;
        oldEntityShadows = mc.gameSettings.entityShadows;
        oldLimitFramerate = mc.gameSettings.limitFramerate;
    }

    private void applyFpsOptimized() {
        mc.gameSettings.renderDistanceChunks = Math.min(mc.gameSettings.renderDistanceChunks, 6);
        mc.gameSettings.fancyGraphics = false;
        mc.gameSettings.ambientOcclusion = 0;
        mc.gameSettings.enableVsync = false;
        mc.gameSettings.entityShadows = false;
        mc.gameSettings.limitFramerate = Math.max(mc.gameSettings.limitFramerate, 260);
    }

    private void restoreFps() {
        mc.gameSettings.renderDistanceChunks = oldRenderDistance;
        mc.gameSettings.fancyGraphics = oldFancy;
        mc.gameSettings.ambientOcclusion = oldAmbientOcclusion;
        mc.gameSettings.enableVsync = oldVsync;
        mc.gameSettings.entityShadows = oldEntityShadows;
        mc.gameSettings.limitFramerate = oldLimitFramerate;
        oldRenderDistance = -1;
    }

    private void applySneakState() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            sneakToggled = false;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            return;
        }
        int keyCode = mc.gameSettings.keyBindSneak.getKeyCode();
        if (keyCode == 0)
            return;

        boolean shouldSneak = toggleSneak.get() ? sneakToggled : Keyboard.isKeyDown(keyCode);
        KeyBinding.setKeyBindState(keyCode, shouldSneak);
        mc.thePlayer.setSneaking(shouldSneak);
    }

    private void trackClicks() {
        boolean currentLeft = Mouse.isButtonDown(0);
        boolean currentRight = Mouse.isButtonDown(1);
        long now = System.currentTimeMillis();

        if (currentLeft && !lastLeft)
            leftClicks.addLast(now);
        if (currentRight && !lastRight)
            rightClicks.addLast(now);

        lastLeft = currentLeft;
        lastRight = currentRight;
    }

    private void pruneClicks() {
        long cutoff = System.currentTimeMillis() - 1000L;
        pruneDeque(leftClicks, cutoff);
        pruneDeque(rightClicks, cutoff);
    }

    private void updateReachOnHit() {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;
        RayTraceResult mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != RayTraceResult.Type.ENTITY || mop.entityHit == null)
            return;
        if (!(mop.entityHit instanceof EntityLivingBase))
            return;
        double dist = mop.hitVec.distanceTo(mc.thePlayer.getPositionEyes(1.0F));
        lastHitReach = dist;
    }

    private void pruneDeque(Deque<Long> deque, long cutoff) {
        Iterator<Long> it = deque.iterator();
        while (it.hasNext()) {
            if (it.next() < cutoff)
                it.remove();
            else
                break;
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null)
            return;

        float sc = (float) scale.get();

        // Label du mod
        renderLine((int) labelX.get(), (int) labelY.get(), "PuffMods V1", labelColor, labelBg, sc);

        // Ordre: CPS -> FPS -> Ping -> Reach -> ToggleSprint -> Armor -> Keystrokes
        if (showCps.get()) {
            int autoL = getAutoClickerCps(true);
            int autoR = getAutoClickerCps(false);
            int displayLeft = isAutoClicking(true) ? (autoL >= 0 ? autoL : 0) : leftClicks.size();
            int displayRight = isAutoClicking(false) ? (autoR >= 0 ? autoR : 0) : rightClicks.size();
            renderLine((int) cpsX.get(), (int) cpsY.get(),
                    String.format("CPS %d/%d", displayLeft, displayRight), cpsColor, cpsBg, sc);
        }

        if (showFps.get()) {
            int fps = net.minecraft.client.Minecraft.getDebugFPS();
            renderLine((int) fpsPingX.get(), (int) fpsPingY.get(),
                    String.format("%d fps", fps), fpsColor, fpsBg, sc);
        }
        if (showPing.get()) {
            int ping = resolvePing();
            renderLine((int) pingX.get(), (int) pingY.get(),
                    String.format("%d ms", ping), pingColor, pingBg, sc);
        }

        if (showReach.get()) {
            renderLine((int) reachX.get(), (int) reachY.get(), String.format("Reach: %.2f", lastHitReach), reachColor,
                    reachBg, sc);
        }

        renderLine((int) toggleSprintX.get(), (int) toggleSprintY.get(),
                "ToggleSprint: " + (toggleSprint.get() ? "ON" : "OFF"), toggleSprintColor, toggleSprintBg, sc);
        renderLine((int) toggleSneakX.get(), (int) toggleSneakY.get(),
                "ToggleSneak: " + (toggleSneak.get() ? "ON" : "OFF"), toggleSneakColor, toggleSneakBg, sc);

        if (showArmor.get()) {
            renderArmor((int) armorX.get(), (int) armorY.get(), sc, armorColor, armorBg);
        }

        if (showKeystrokes.get()) {
            int keystrokeColor = keystrokesRainbow ? getRainbowColor() : keystrokesColor;
            renderKeystrokes((int) keystrokesX.get(), (int) keystrokesY.get(), keystrokeColor, keystrokesBg, sc);
        }

        if (showPotions.get()) {
            renderPotions((int) potionsX.get(), (int) potionsY.get(), potionsColor, potionsBg, sc);
        }

        if (showCoords.get()) {
            renderCoords((int) coordsX.get(), (int) coordsY.get(), coordsColor, coordsBg, sc);
        }

    }

    /**
     * Écran de personnalisation des couleurs/fonds HUD.
     */
    private static final int[] COLOR_PALETTE = new int[] {
            0xFFFFFFFF, 0xFFFEDD00, 0xFFFF0000, 0xFFE53935, 0xFFFF9800,
            0xFF4CAF50, 0xFF2196F3, 0xFF9C27B0, 0xFF00BCD4, 0xFF795548,
            0xFF9E9E9E, 0xFF000000, 0xF020202E
    };
    private static final String[] COLOR_NAMES = new String[] {
            "White", "Yellow", "Red", "DeepRed", "Orange",
            "Green", "Blue", "Purple", "Teal", "Brown",
            "Gray", "Black", "Dark"
    };
    private static final int KEYSTROKE_TEXT_COLOR = 0xFFFFFFFF;
        private static final int COLOR_BUTTON_BASE = 400;

    private static int cycleColor(int current) {
        for (int i = 0; i < COLOR_PALETTE.length; i++) {
            if (COLOR_PALETTE[i] == current) {
                return COLOR_PALETTE[(i + 1) % COLOR_PALETTE.length];
            }
        }
        return COLOR_PALETTE[0];
    }

    private static String colorName(int current) {
        for (int i = 0; i < COLOR_PALETTE.length; i++) {
            if (COLOR_PALETTE[i] == current) {
                return COLOR_NAMES[i];
            }
        }
        return colorToHex(current);
    }

    private static int getRainbowColor() {
        float hue = (System.currentTimeMillis() % RAINBOW_PERIOD_MS) / (float) RAINBOW_PERIOD_MS;
        int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    private static final long RAINBOW_PERIOD_MS = 4500L;

    private static class HudStyleScreen extends GuiScreen {
        private final HudOverlay hud;
        private final List<ColorEntry> colorEntries = new ArrayList<>();
        private boolean labelBgToggle;
        private boolean cpsBgToggle;
        private boolean fpsBgToggle;
        private boolean pingBgToggle;
        private boolean reachBgToggle;
        private boolean toggleSprintBgToggle;
        private boolean toggleSneakBgToggle;
        private boolean armorBgToggle;
        private boolean potionsBgToggle;
        private boolean coordsBgToggle;
        private boolean keystrokesRainbowToggle;

        HudStyleScreen(HudOverlay hud) {
            this.hud = hud;
        }

        @Override
        public void initGui() {
            this.buttonList.clear();
            colorEntries.clear();
            int x = this.width / 2 - 150;
            int y = 40;

            addColorButton("Label", () -> hud.labelColor, value -> hud.labelColor = value, x, y, null, null,
                    paletteOptions());
            addColorButton("CPS", () -> hud.cpsColor, value -> hud.cpsColor = value, x, y += 22, null, null,
                    paletteOptions());
            addColorButton("FPS", () -> hud.fpsColor, value -> hud.fpsColor = value, x, y += 22, null, null,
                    paletteOptions());
            addColorButton("Ping", () -> hud.pingColor, value -> hud.pingColor = value, x, y += 22, null, null,
                    paletteOptions());
            addColorButton("Reach", () -> hud.reachColor, value -> hud.reachColor = value, x, y += 22, null, null,
                    paletteOptions());
            addColorButton("ToggleSprint", () -> hud.toggleSprintColor, value -> hud.toggleSprintColor = value, x,
                    y += 22, null, null, paletteOptions());
            addColorButton("ToggleSneak", () -> hud.toggleSneakColor, value -> hud.toggleSneakColor = value, x,
                    y += 22, null, null, paletteOptions());
            addColorButton("Armor", () -> hud.armorColor, value -> hud.armorColor = value, x, y += 22, null, null,
                    paletteOptions());
            addColorButton("Potions", () -> hud.potionsColor, value -> hud.potionsColor = value, x, y += 22, null, null,
                    paletteOptions());
            addColorButton("Coords", () -> hud.coordsColor, value -> hud.coordsColor = value, x, y += 22, null, null,
                    paletteOptions());
            addColorButton("Keystrokes", () -> hud.keystrokesColor, value -> hud.keystrokesColor = value, x,
                    y += 22, () -> hud.keystrokesRainbow, v -> hud.keystrokesRainbow = v, paletteOptions(true));
            addColorButton("OverlayBg", () -> hud.overlayBgColor, value -> hud.overlayBgColor = value, x, y += 22,
                    null, null, paletteOptions());

            int toggleX = this.width / 2 + 20;
            int ty = 40;
            this.buttonList.add(makeToggle(201, toggleX, ty, "Label BG", hud.labelBg));
            this.buttonList.add(makeToggle(202, toggleX, ty += 22, "CPS BG", hud.cpsBg));
            this.buttonList.add(makeToggle(203, toggleX, ty += 22, "FPS BG", hud.fpsBg));
            this.buttonList.add(makeToggle(204, toggleX, ty += 22, "Ping BG", hud.pingBg));
            this.buttonList.add(makeToggle(205, toggleX, ty += 22, "Reach BG", hud.reachBg));
            this.buttonList.add(makeToggle(206, toggleX, ty += 22, "ToggleSprint BG", hud.toggleSprintBg));
            this.buttonList.add(makeToggle(207, toggleX, ty += 22, "ToggleSneak BG", hud.toggleSneakBg));
            this.buttonList.add(makeToggle(208, toggleX, ty += 22, "Armor BG", hud.armorBg));
            this.buttonList.add(makeToggle(209, toggleX, ty += 22, "Potions BG", hud.potionsBg));
            this.buttonList.add(makeToggle(210, toggleX, ty += 22, "Coords BG", hud.coordsBg));
            labelBgToggle = hud.labelBg;
            cpsBgToggle = hud.cpsBg;
            fpsBgToggle = hud.fpsBg;
            pingBgToggle = hud.pingBg;
            reachBgToggle = hud.reachBg;
            toggleSprintBgToggle = hud.toggleSprintBg;
            toggleSneakBgToggle = hud.toggleSneakBg;
            armorBgToggle = hud.armorBg;
            potionsBgToggle = hud.potionsBg;
            coordsBgToggle = hud.coordsBg;
            keystrokesRainbowToggle = hud.keystrokesRainbow;

            this.buttonList.add(new net.minecraft.client.gui.GuiButton(300, this.width / 2 - 100, this.height - 40, 200,
                    20, "Sauvegarder"));
        }

        private void addColorButton(String name, Supplier<Integer> getter, Consumer<Integer> setter, int x, int y,
                                    Supplier<Boolean> rainbowGetter, Consumer<Boolean> rainbowSetter,
                                    ColorOption... options) {
            ColorEntry entry = new ColorEntry(COLOR_BUTTON_BASE + colorEntries.size(), name, getter, setter,
                    rainbowGetter, rainbowSetter, options);
            this.buttonList.add(entry.createButton(x, y));
            colorEntries.add(entry);
        }

        private ColorOption[] paletteOptions() {
            return paletteOptions(false);
        }

        private ColorOption[] paletteOptions(boolean allowRainbow) {
            List<ColorOption> opts = new ArrayList<>();
            for (int i = 0; i < COLOR_PALETTE.length; i++) {
                opts.add(new ColorOption(COLOR_NAMES[i], COLOR_PALETTE[i], false));
            }
            if (allowRainbow) {
                opts.add(new ColorOption("Rainbow", 0, true));
            }
            return opts.toArray(new ColorOption[0]);
        }

        private net.minecraft.client.gui.GuiButton makeToggle(int id, int x, int y, String label, boolean state) {
            return new net.minecraft.client.gui.GuiButton(id, x, y, 120, 20, label + ": " + (state ? "ON" : "OFF"));
        }

        @Override
        protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
            for (ColorEntry entry : colorEntries) {
                if (entry.id == button.id) {
                    entry.cycle();
                    return;
                }
            }
            switch (button.id) {
                case 201:
                    labelBgToggle = !labelBgToggle;
                    button.displayString = "Label BG: " + (labelBgToggle ? "ON" : "OFF");
                    break;
                case 212:
                    keystrokesRainbowToggle = !keystrokesRainbowToggle;
                    button.displayString = "Rainbow: " + (keystrokesRainbowToggle ? "ON" : "OFF");
                    break;
                case 202:
                    cpsBgToggle = !cpsBgToggle;
                    button.displayString = "CPS BG: " + (cpsBgToggle ? "ON" : "OFF");
                    break;
                case 203:
                    fpsBgToggle = !fpsBgToggle;
                    button.displayString = "FPS BG: " + (fpsBgToggle ? "ON" : "OFF");
                    break;
                case 204:
                    pingBgToggle = !pingBgToggle;
                    button.displayString = "Ping BG: " + (pingBgToggle ? "ON" : "OFF");
                    break;
                case 205:
                    reachBgToggle = !reachBgToggle;
                    button.displayString = "Reach BG: " + (reachBgToggle ? "ON" : "OFF");
                    break;
                case 206:
                    toggleSprintBgToggle = !toggleSprintBgToggle;
                    button.displayString = "ToggleSprint BG: " + (toggleSprintBgToggle ? "ON" : "OFF");
                    break;
                case 207:
                    toggleSneakBgToggle = !toggleSneakBgToggle;
                    button.displayString = "ToggleSneak BG: " + (toggleSneakBgToggle ? "ON" : "OFF");
                    break;
                case 208:
                    armorBgToggle = !armorBgToggle;
                    button.displayString = "Armor BG: " + (armorBgToggle ? "ON" : "OFF");
                    break;
                case 209:
                    potionsBgToggle = !potionsBgToggle;
                    button.displayString = "Potions BG: " + (potionsBgToggle ? "ON" : "OFF");
                    break;
                case 210:
                    coordsBgToggle = !coordsBgToggle;
                    button.displayString = "Coords BG: " + (coordsBgToggle ? "ON" : "OFF");
                    break;
                case 300:
                    applyAndClose();
                    break;
                default:
                    break;
            }
            super.actionPerformed(button);
        }

        private void applyAndClose() {
            hud.labelBg = labelBgToggle;
            hud.cpsBg = cpsBgToggle;
            hud.fpsBg = fpsBgToggle;
            hud.pingBg = pingBgToggle;
            hud.reachBg = reachBgToggle;
            hud.toggleSprintBg = toggleSprintBgToggle;
            hud.toggleSneakBg = toggleSneakBgToggle;
            hud.armorBg = armorBgToggle;
            hud.potionsBg = potionsBgToggle;
            hud.coordsBg = coordsBgToggle;
            hud.keystrokesRainbow = keystrokesRainbowToggle;

            hud.saveConfig();
            this.mc.displayGuiScreen(new HudSettingsScreen(hud));
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, "HUD Style", this.width / 2, 12, 0xFFFFFF);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }

        private static class ColorEntry {
            private final int id;
            private final String label;
            private final Supplier<Integer> getter;
            private final Consumer<Integer> setter;
            private final List<ColorOption> options;
            private final Consumer<Boolean> rainbowSetter;
            private net.minecraft.client.gui.GuiButton button;
            private int index;

            ColorEntry(int id, String label, Supplier<Integer> getter, Consumer<Integer> setter,
                       Supplier<Boolean> rainbowGetter, Consumer<Boolean> rainbowSetter, ColorOption... options) {
                this.id = id;
                this.label = label;
                this.getter = getter;
                this.setter = setter;
                this.options = Arrays.asList(options);
                this.rainbowSetter = rainbowSetter;
                this.index = 0;
                int current = getter.get();
                if (rainbowGetter != null && rainbowGetter.get()) {
                    for (int i = 0; i < this.options.size(); i++) {
                        if (this.options.get(i).isRainbow) {
                            this.index = i;
                            return;
                        }
                    }
                }
                for (int i = 0; i < this.options.size(); i++) {
                    if (this.options.get(i).matches(current)) {
                        this.index = i;
                        return;
                    }
                }
            }

            net.minecraft.client.gui.GuiButton createButton(int x, int y) {
                button = new net.minecraft.client.gui.GuiButton(id, x, y, 140, 20, formatLabel());
                return button;
            }

            private String formatLabel() {
                return label + ": " + options.get(index).name;
            }

            void cycle() {
                index = (index + 1) % options.size();
                ColorOption option = options.get(index);
                if (option.isRainbow) {
                    if (rainbowSetter != null) {
                        rainbowSetter.accept(true);
                    }
                    if (button != null) button.displayString = formatLabel();
                    return;
                }
                if (rainbowSetter != null) {
                    rainbowSetter.accept(false);
                }
                setter.accept(option.color);
                if (button != null) {
                    button.displayString = formatLabel();
                }
            }
        }

        private static class ColorOption {
            final String name;
            final int color;
            final boolean isRainbow;

            ColorOption(String name, int color, boolean isRainbow) {
                this.name = name;
                this.color = color;
                this.isRainbow = isRainbow;
            }

            boolean matches(int value) {
                return !isRainbow && value == color;
            }
        }
    }

    private int renderLine(int x, int y, String text, int color, boolean drawBg, float sc) {
        int drawX = (int) (x * sc);
        int drawY = (int) (y * sc);
        int w = mc.fontRendererObj.getStringWidth(text);
        if (drawBg) {
            GuiScreen.drawRect(drawX - 2, drawY - 1, drawX + w + 2, drawY + 9, overlayBgColor);
        }
        mc.fontRendererObj.drawStringWithShadow(text, drawX, drawY, color);
        return (int) (14 * sc);
    }

    private int renderKeystrokes(int x, int y, int textColor, boolean drawBg, float sc) {
        int block = (int) (26 * sc);
        int padding = (int) (3 * sc);
        int moveColor = textColor;

        // Key codes selon les contrôles configurés
        int keyForward = mc.gameSettings.keyBindForward.getKeyCode();
        int keyBack = mc.gameSettings.keyBindBack.getKeyCode();
        int keyLeft = mc.gameSettings.keyBindLeft.getKeyCode();
        int keyRight = mc.gameSettings.keyBindRight.getKeyCode();

        boolean pressedF = Keyboard.isKeyDown(keyForward);
        boolean pressedB = Keyboard.isKeyDown(keyBack);
        boolean pressedL = Keyboard.isKeyDown(keyLeft);
        boolean pressedR = Keyboard.isKeyDown(keyRight);

        // Top: forward
        drawKey(x + block + padding, y, getKeyName(keyForward), pressedF, block, moveColor, drawBg, 0);
        // Row: left, back, right
        drawKey(x, y + block + padding, getKeyName(keyLeft), pressedL, block, moveColor, drawBg, 1);
        drawKey(x + block + padding, y + block + padding, getKeyName(keyBack), pressedB, block, moveColor, drawBg, 2);
        drawKey(x + (block * 2) + (padding * 2), y + block + padding, getKeyName(keyRight), pressedR, block, moveColor,
                drawBg, 3);

        // Ligne souris avec LMB couvrant Q + S, RMB couvrant S + D (avec petit gap)
        int mouseRowY = y + (block * 2) + (padding * 2);
        int lmbWidth = (int) Math.round(block * 1.5f);
        int gap = padding; // petit espace entre LMB et RMB
        int endOfD = x + (block * 3) + (padding * 2); // bord droit de la touche D

        int autoL = getAutoClickerCps(true);
        int autoR = getAutoClickerCps(false);
        int displayLeft = isAutoClicking(true) ? (autoL >= 0 ? autoL : 0) : leftClicks.size();
        int displayRight = isAutoClicking(false) ? (autoR >= 0 ? autoR : 0) : rightClicks.size();

        drawMouseKey(x, mouseRowY, "LMB", physicalLeftDown, displayLeft, lmbWidth, block, true, drawBg, 4,
                moveColor);

        int rmbX = x + lmbWidth + gap;
        int rmbWidth = endOfD - rmbX;
        if (rmbWidth < block)
            rmbWidth = block; // garde une largeur minimale

        drawMouseKey(rmbX, mouseRowY, "RMB", physicalRightDown, displayRight, rmbWidth, block, false, drawBg, 5,
                moveColor);
        return (block * 3) + (padding * 2);
    }

    private void drawKey(int x, int y, String label, boolean pressed, int size, int textColor, boolean drawBg,
            int fadeIndex) {
        float f = updateFade(fadeIndex, pressed);
        if (drawBg) {
            int bg = blendColors(0xFF000000, 0xFFFFFFFF, f);
            GuiScreen.drawRect(x, y, x + size, y + size, bg);
        }
        String txt = label;
        int textWidth = mc.fontRendererObj.getStringWidth(txt);
        mc.fontRendererObj.drawStringWithShadow(txt, x + (size - textWidth) / 2.0F, y + size / 2.0F - 4, textColor);
    }

    private void drawMouseKey(int x, int y, String label, boolean pressed, int cps, int width, int height, boolean left,
            boolean drawBg, int fadeIndex, int textColor) {
        float f = updateFade(fadeIndex, pressed);
        int labelColor = blendColors(textColor, 0xFF000000, f);
        if (drawBg) {
            int bg = blendColors(0xFF000000, 0xFFFFFFFF, f);
            GuiScreen.drawRect(x, y, x + width, y + height, bg);
        }

        int textWidth = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawStringWithShadow(label, x + (width - textWidth) / 2.0F, y + 6, labelColor);

        String cpsText = cps + " CPS";
        float scale = 0.8F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + (width / 2.0F), y + height - 9, 0);
        GlStateManager.scale(scale, scale, 1);
        int cpsWidth = mc.fontRendererObj.getStringWidth(cpsText);
        mc.fontRendererObj.drawStringWithShadow(cpsText, -(cpsWidth / 2.0F), 0, 0xFFFFFFFF);
        GlStateManager.popMatrix();
    }

    private float updateFade(int index, boolean pressed) {
        float target = pressed ? 1.0f : 0.0f;
        float current = keyFade[index];
        current += (target - current) * 0.3f;
        keyFade[index] = current;
        return current;
    }

    private int blendColors(int c1, int c2, float t) {
        float u = Math.max(0f, Math.min(1f, t));
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * u);
        int r = (int) (r1 + (r2 - r1) * u);
        int g = (int) (g1 + (g2 - g1) * u);
        int b = (int) (b1 + (b2 - b1) * u);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private String getKeyName(int keyCode) {
        if (keyCode < 0) {
            // Mouse buttons are negative in LWJGL, but here we only map keyboard movement
            // keys
            return "MB";
        }
        String name = Keyboard.getKeyName(keyCode);
        return name == null ? "?" : name;
    }

    private int renderArmor(int x, int y, float sc, int textColor, boolean drawBg) {
        if (mc.thePlayer == null)
            return 0;

        java.util.List<ItemStack> armor = new ArrayList<>();
        mc.thePlayer.getArmorInventoryList().forEach(armor::add);
        Collections.reverse(armor); // casque en haut

        GlStateManager.pushMatrix();
        GlStateManager.scale(sc, sc, 1);
        RenderHelper.enableGUIStandardItemLighting();
        int dy = 0;
        boolean hasArmor = false;
        for (ItemStack piece : armor) {
            int drawY = y + dy;
            if (piece != null && piece.getItem() != null) {
                hasArmor = true;
                mc.getRenderItem().renderItemAndEffectIntoGUI(piece, x, drawY);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, piece, x, drawY, "");

                int max = piece.getMaxDamage();
                int remaining = max - piece.getItemDamage();
                int percent = max > 0 ? Math.max(0, Math.min(100, (int) Math.round((remaining * 100.0) / max))) : 0;
                String pct = percent + "%";
                float pctScale = 0.6F;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + 18, drawY + 2, 0);
                GlStateManager.scale(pctScale, pctScale, 1);
                mc.fontRendererObj.drawStringWithShadow(pct, 0, 0, colorFromPercent(percent));
                GlStateManager.popMatrix();
            }
            dy += 20;
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();

        if (!hasArmor) {
            return renderLine(x, y, "Armor: none", textColor, drawBg, sc);
        }
        return (int) (dy * sc);
    }

    private int colorFromPercent(int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        float ratio = clamped / 100.0f;
        int red = (int) (255 * (1.0f - ratio));
        int green = (int) (255 * ratio);
        return 0xFF000000 | (red << 16) | (green << 8);
    }

    private int resolvePing() {
        if (mc.thePlayer == null)
            return 0;
        NetHandlerPlayClient connection = mc.thePlayer.connection;
        if (connection == null)
            return 0;
        NetworkPlayerInfo info = connection.getPlayerInfo(mc.thePlayer.getUniqueID());
        return info != null ? info.getResponseTime() : 0;
    }

    private int getAutoClickerCps(boolean left) {
        if (PuffMod.getModuleManager() == null)
            return -1;
        AutoClicker ac = PuffMod.getModuleManager().getModule(AutoClicker.class);
        if (ac != null && ac.isEnabled()) {
            if (left && ac.isLeftEnabled())
                return ac.getCpsValue(true);
            if (!left && ac.isRightEnabled())
                return ac.getCpsValue(false);
        }
        return -1;
    }

    private boolean isAutoClicking(boolean left) {
        if (PuffMod.getModuleManager() == null)
            return false;
        AutoClicker ac = PuffMod.getModuleManager().getModule(AutoClicker.class);
        if (ac == null || !ac.isEnabled())
            return false;
        if (left && !ac.isLeftEnabled())
            return false;
        if (!left && !ac.isRightEnabled())
            return false;
        return left ? physicalLeftDown : physicalRightDown;
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!isEnabled())
            return;
        if (event.getGui() instanceof GuiIngameMenu) {
            ScaledResolution sr = new ScaledResolution(mc);
            int centerX = sr.getScaledWidth() / 2;

            int maxY = 0;
            for (Object o : event.getButtonList()) {
                if (o instanceof net.minecraft.client.gui.GuiButton) {
                    net.minecraft.client.gui.GuiButton b = (net.minecraft.client.gui.GuiButton) o;
                    maxY = Math.max(maxY, b.yPosition + b.height);
                }
            }

            int baseY = maxY + 6; // small spacing after vanilla buttons
            event.getButtonList()
                    .add(new net.minecraft.client.gui.GuiButton(9002, centerX - 100, baseY, 98, 20, "HUD Settings"));
            event.getButtonList()
                    .add(new net.minecraft.client.gui.GuiButton(9003, centerX + 2, baseY, 98, 20, "HUD Position"));
            int chatY = baseY + 28;
            event.getButtonList()
                    .add(new net.minecraft.client.gui.GuiButton(9004, centerX - 100, chatY, 200, 20, "Chat Settings"));
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (!isEnabled())
            return;
        if (event.getGui() instanceof GuiIngameMenu
                && event.getButton() != null
                && event.getButton().id == 9001) {
            mc.displayGuiScreen(new HudSettingsScreen(this));
        } else if (event.getGui() instanceof GuiIngameMenu
                && event.getButton() != null
                && event.getButton().id == 9002) {
            mc.displayGuiScreen(new HudSettingsScreen(this));
        } else if (event.getGui() instanceof GuiIngameMenu
                && event.getButton() != null
                && event.getButton().id == 9003) {
            mc.displayGuiScreen(new HudPositionScreen(this));
        } else if (event.getGui() instanceof GuiIngameMenu
                && event.getButton() != null
                && event.getButton().id == 9004) {
            mc.displayGuiScreen(new ChatSettingsScreen(this));
        }
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!isEnabled() || !autoGg.get() || mc.thePlayer == null)
            return;
        long now = System.currentTimeMillis();
        if (now - lastAutoGg < AUTO_GG_COOLDOWN_MS)
            return;
        String text = event.getMessage().getUnformattedText();
        if (text == null || text.isEmpty())
            return;
        for (Pattern trigger : AUTO_GG_PATTERNS) {
            if (trigger.matcher(text).find()) {
                lastAutoGg = now;
                scheduleAutoGgMessage();
                break;
            }
        }
    }

    private void scheduleAutoGgMessage() {
        final String reply = getAutoGgMessageText();
        if (reply.isEmpty())
            return;
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(AUTO_GG_DELAY_MS);
            } catch (InterruptedException ignored) {
            }
            if (mc == null)
                return;
            mc.addScheduledTask(() -> {
                if (mc.thePlayer != null) {
                    mc.thePlayer.sendChatMessage(reply);
                }
            });
        }, "PuffAutoGG");
        thread.setDaemon(true);
        thread.start();
    }

    private String getAutoGgMessageText() {
        if (AUTO_GG_MESSAGES.length == 0)
            return "";
        int index = (int) autoGgMessageIndex.get();
        if (index < 0 || index >= AUTO_GG_MESSAGES.length) {
            index = 0;
        }
        return AUTO_GG_MESSAGES[index];
    }

    private String getAutoGgMessageLabel() {
        return "AutoGG msg: " + getAutoGgMessageText();
    }

    private void cycleAutoGgMessage() {
        if (AUTO_GG_MESSAGES.length == 0)
            return;
        int next = ((int) autoGgMessageIndex.get() + 1) % AUTO_GG_MESSAGES.length;
        autoGgMessageIndex.set(next);
    }

    private void updateChatBackgroundState() {
        if (mc == null || mc.gameSettings == null)
            return;
        if (removeChatBackground.get()) {
            if (savedChatOpacity < 0f) {
                savedChatOpacity = mc.gameSettings.chatOpacity;
            }
            mc.gameSettings.chatOpacity = 0f;
        } else if (savedChatOpacity >= 0f) {
            mc.gameSettings.chatOpacity = savedChatOpacity;
            savedChatOpacity = -1f;
        }
    }

    private void restoreChatOpacity() {
        if (mc == null || mc.gameSettings == null)
            return;
        if (savedChatOpacity >= 0f) {
            mc.gameSettings.chatOpacity = savedChatOpacity;
        }
        savedChatOpacity = -1f;
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        // update click history when Forge sends mouse events
        trackClicks();
        pruneClicks();

        int btn = Mouse.getEventButton();
        if (btn == 0 && Mouse.getEventButtonState()) { // LMB pressed
            updateReachOnHit();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null)
            return;
        if (!toggleSneak.get())
            return;
        int keyCode = mc.gameSettings.keyBindSneak.getKeyCode();
        if (keyCode == 0)
            return;
        if (Keyboard.getEventKey() == keyCode && Keyboard.getEventKeyState()) {
            sneakToggled = !sneakToggled;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            handleToggleSprint();
            handleFullBright();
            handleFpsOptimizer();
            pruneClicks();
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre<EntityLivingBase> event) {
        if (!showEntityHearts.get())
            return;
        EntityLivingBase ent = event.getEntity();
        if (ent == mc.thePlayer || ent.isInvisible())
            return;
        if (!mc.thePlayer.canEntityBeSeen(ent))
            return;

        float health = ent.getHealth();
        float max = ent.getMaxHealth();
        if (max <= 0)
            return;
        float hearts = health / 2.0f;
        int pct = Math.max(0, Math.min(100, (int) ((health / max) * 100)));
        int color = 0xFFFF4444; // coeur rouge
        String text = "♥";
        String amount = String.format("%.1f", hearts);

        GlStateManager.pushMatrix();
        GlStateManager.translate(event.getX(), event.getY() + ent.height + 0.5, event.getZ());
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        float s = 0.025F;
        GlStateManager.scale(-s, -s, s);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        int wHeart = mc.fontRendererObj.getStringWidth(text) / 2;
        int wAmount = mc.fontRendererObj.getStringWidth(amount) / 2;
        mc.fontRendererObj.drawString(text, -wHeart, 0, color, false);
        mc.fontRendererObj.drawString(amount, -wAmount, 10, 0xFFFFFFFF, false);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (mc.theWorld == null || mc.thePlayer == null)
            return;
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityTNTPrimed))
                continue;
            EntityTNTPrimed tnt = (EntityTNTPrimed) o;
            renderWorldLabel(tnt, partialTicks);
        }
    }

    private void renderWorldLabel(EntityTNTPrimed tnt, float partialTicks) {
        if (mc.thePlayer != null && !mc.thePlayer.canEntityBeSeen(tnt))
            return;
        double x = tnt.lastTickPosX + (tnt.posX - tnt.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = tnt.lastTickPosY + (tnt.posY - tnt.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = tnt.lastTickPosZ + (tnt.posZ - tnt.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;
        y += tnt.height + 0.5;

        float seconds;
        try {
            seconds = tnt.getFuse() / 20.0f;
        } catch (NoSuchMethodError e) {
            seconds = 0f;
        }
        String text = String.format("%.1fs", seconds);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        float s = 0.025F;
        GlStateManager.scale(-s, -s, s);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        int w = mc.fontRendererObj.getStringWidth(text) / 2;
        mc.fontRendererObj.drawString(text, -w, 0, 0xFFFF4444, false);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player != mc.thePlayer)
            return;
        if (event.phase == TickEvent.Phase.START) {
            applySneakState();
        }
    }

    private int renderPotions(int x, int y, int textColor, boolean drawBg, float sc) {
        if (mc.thePlayer == null)
            return 0;

        GlStateManager.pushMatrix();
        GlStateManager.scale(sc, sc, 1);
        int dy = 0;
        if (mc.thePlayer.getActivePotionEffects().isEmpty()) {
            mc.fontRendererObj.drawStringWithShadow("Aucun effet", x, y, textColor);
            GlStateManager.popMatrix();
            return (int) (14 * sc);
        }

        int iconRawSize = 18;
        float iconScale = 0.8F; // réduit l'icône sans la rogner
        int iconDrawSize = (int) (iconRawSize * iconScale);
        int lineHeight = iconDrawSize + 6;
        int textOffsetX = iconDrawSize + 4;
        int durationOffsetY = 1;
        int nameOffsetY = iconDrawSize / 2 + 3;
        for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            Potion potion = effect.getPotion();
            if (potion == null)
                continue;
            int drawY = y + dy;

            int iconIndex = potion.getStatusIconIndex();
            if (iconIndex >= 0) {
                mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
                GlStateManager.color(1F, 1F, 1F, 1F);
                int u = (iconIndex % 8) * 18;
                int v = 198 + (iconIndex / 8) * 18;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, drawY, 0);
                GlStateManager.scale(iconScale, iconScale, 1);
                Gui.drawModalRectWithCustomSizedTexture(0, 0, u, v, iconRawSize, iconRawSize, 256, 256);
                GlStateManager.popMatrix();
            }

            String name = I18n.format(effect.getEffectName());
            int amp = effect.getAmplifier() + 1;
            if (amp > 1) {
                name = name + " " + amp;
            }
            String duration = Potion.getPotionDurationString(effect, 1.0F);
            // Alignement vertical centré sur l'icône réduite
            mc.fontRendererObj.drawStringWithShadow(duration, x + textOffsetX, drawY + durationOffsetY, textColor);
            mc.fontRendererObj.drawStringWithShadow(name, x + textOffsetX, drawY + nameOffsetY, textColor);
            dy += lineHeight;
        }
        GlStateManager.popMatrix();
        return (int) (dy * sc);
    }

    private int renderCoords(int x, int y, int textColor, boolean drawBg, float sc) {
        if (mc.thePlayer == null)
            return 0;
        double px = mc.thePlayer.posX;
        double py = mc.thePlayer.posY;
        double pz = mc.thePlayer.posZ;
        String line = String.format("XYZ: %.1f / %.1f / %.1f", px, py, pz);
        return renderLine(x, y, line, textColor, drawBg, sc);
    }

    private void loadConfig() {
        if (!configFile.exists())
            return;
        try (FileReader reader = new FileReader(configFile)) {
            HudConfig cfg = gson.fromJson(reader, HudConfig.class);
            if (cfg == null)
                return;
            labelX.set(cfg.labelX);
            labelY.set(cfg.labelY);
            showKeystrokes.set(cfg.keystrokes);
            showCps.set(cfg.cps);
            showFps.set(cfg.fps);
            showPing.set(cfg.ping);
            showReach.set(cfg.reach);
            showArmor.set(cfg.armor);
            toggleSprint.set(cfg.toggleSprint);
            toggleSneak.set(cfg.toggleSneak);
            showEntityHearts.set(cfg.entityHearts);
            fullBright.set(cfg.fullBright);
            noHurtCam.set(cfg.noHurtCam);
            showCoords.set(cfg.coords);
            showPotions.set(cfg.potions);
            keystrokesX.set(cfg.keystrokesX);
            keystrokesY.set(cfg.keystrokesY);
            cpsX.set(cfg.cpsX);
            cpsY.set(cfg.cpsY);
            fpsPingX.set(cfg.fpsPingX);
            fpsPingY.set(cfg.fpsPingY);
            pingX.set(cfg.pingX);
            pingY.set(cfg.pingY);
            reachX.set(cfg.reachX);
            reachY.set(cfg.reachY);
            toggleSprintX.set(cfg.toggleSprintX);
            toggleSprintY.set(cfg.toggleSprintY);
            toggleSneakX.set(cfg.toggleSneakX);
            toggleSneakY.set(cfg.toggleSneakY);
            armorX.set(cfg.armorX);
            armorY.set(cfg.armorY);
            potionsX.set(cfg.potionsX);
            potionsY.set(cfg.potionsY);
            coordsX.set(cfg.coordsX);
            coordsY.set(cfg.coordsY);
            scale.set(cfg.scale);
            fpsOptimizer.set(cfg.fpsOptimizer);
            // couleurs / fonds
            labelColor = cfg.labelColor;
            cpsColor = cfg.cpsColor;
            fpsColor = cfg.fpsColor;
            pingColor = cfg.pingColor;
            reachColor = cfg.reachColor;
            toggleSprintColor = cfg.toggleSprintColor;
            toggleSneakColor = cfg.toggleSneakColor;
            armorColor = cfg.armorColor;
            potionsColor = cfg.potionsColor;
            coordsColor = cfg.coordsColor;
            keystrokesColor = cfg.keystrokesColor;
            overlayBgColor = cfg.overlayBgColor;
            labelBg = cfg.labelBg;
            cpsBg = cfg.cpsBg;
            fpsBg = cfg.fpsBg;
            pingBg = cfg.pingBg;
            reachBg = cfg.reachBg;
            toggleSprintBg = cfg.toggleSprintBg;
            toggleSneakBg = cfg.toggleSneakBg;
            armorBg = cfg.armorBg;
            potionsBg = cfg.potionsBg;
            coordsBg = cfg.coordsBg;
            keystrokesBg = cfg.keystrokesBg;
            keystrokesRainbow = cfg.keystrokesRainbow;
            removeChatBackground.set(cfg.removeChatBackground);
            autoGg.set(cfg.autoGg);
            autoGgMessageIndex.set(cfg.autoGgMessageIndex);
            updateChatBackgroundState();
        } catch (IOException ignored) {
        }
    }

    public void saveConfig() {
        try {
            if (!Objects.requireNonNull(configFile.getParentFile()).exists()) {
                configFile.getParentFile().mkdirs();
            }
            HudConfig cfg = new HudConfig();
            cfg.keystrokes = showKeystrokes.get();
            cfg.cps = showCps.get();
            cfg.fps = showFps.get();
            cfg.ping = showPing.get();
            cfg.reach = showReach.get();
            cfg.armor = showArmor.get();
            cfg.toggleSprint = toggleSprint.get();
            cfg.toggleSneak = toggleSneak.get();
            cfg.entityHearts = showEntityHearts.get();
            cfg.fullBright = fullBright.get();
            cfg.noHurtCam = noHurtCam.get();
            cfg.coords = showCoords.get();
            cfg.potions = showPotions.get();
            cfg.labelX = labelX.get();
            cfg.labelY = labelY.get();
            cfg.keystrokesX = keystrokesX.get();
            cfg.keystrokesY = keystrokesY.get();
            cfg.cpsX = cpsX.get();
            cfg.cpsY = cpsY.get();
            cfg.fpsPingX = fpsPingX.get();
            cfg.fpsPingY = fpsPingY.get();
            cfg.pingX = pingX.get();
            cfg.pingY = pingY.get();
            cfg.reachX = reachX.get();
            cfg.reachY = reachY.get();
            cfg.toggleSprintX = toggleSprintX.get();
            cfg.toggleSprintY = toggleSprintY.get();
            cfg.toggleSneakX = toggleSneakX.get();
            cfg.toggleSneakY = toggleSneakY.get();
            cfg.armorX = armorX.get();
            cfg.armorY = armorY.get();
            cfg.potionsX = potionsX.get();
            cfg.potionsY = potionsY.get();
            cfg.coordsX = coordsX.get();
            cfg.coordsY = coordsY.get();
            cfg.scale = scale.get();
            cfg.keystrokesRainbow = keystrokesRainbow;
            cfg.labelColor = labelColor;
            cfg.cpsColor = cpsColor;
            cfg.fpsColor = fpsColor;
            cfg.pingColor = pingColor;
            cfg.reachColor = reachColor;
            cfg.toggleSprintColor = toggleSprintColor;
            cfg.toggleSneakColor = toggleSneakColor;
            cfg.armorColor = armorColor;
            cfg.potionsColor = potionsColor;
            cfg.coordsColor = coordsColor;
            cfg.keystrokesColor = keystrokesColor;
            cfg.overlayBgColor = overlayBgColor;
            cfg.labelBg = labelBg;
            cfg.cpsBg = cpsBg;
            cfg.fpsBg = fpsBg;
            cfg.pingBg = pingBg;
            cfg.reachBg = reachBg;
            cfg.toggleSprintBg = toggleSprintBg;
            cfg.toggleSneakBg = toggleSneakBg;
            cfg.armorBg = armorBg;
            cfg.potionsBg = potionsBg;
            cfg.coordsBg = coordsBg;
            cfg.keystrokesBg = keystrokesBg;
            cfg.removeChatBackground = removeChatBackground.get();
            cfg.autoGg = autoGg.get();
            cfg.autoGgMessageIndex = (int) autoGgMessageIndex.get();
            cfg.fpsOptimizer = fpsOptimizer.get();
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(cfg, writer);
            }
        } catch (IOException ignored) {
        }
    }

    // Accessors for settings screen
    public BooleanSetting getShowKeystrokes() {
        return showKeystrokes;
    }

    public BooleanSetting getShowCps() {
        return showCps;
    }

    public BooleanSetting getShowFps() {
        return showFps;
    }

    public BooleanSetting getShowPing() {
        return showPing;
    }

    public BooleanSetting getShowReach() {
        return showReach;
    }

    public BooleanSetting getShowArmor() {
        return showArmor;
    }

    public NumberSetting getKeystrokesX() {
        return keystrokesX;
    }

    public NumberSetting getKeystrokesY() {
        return keystrokesY;
    }

    public NumberSetting getCpsX() {
        return cpsX;
    }

    public NumberSetting getCpsY() {
        return cpsY;
    }

    public NumberSetting getFpsPingX() {
        return fpsPingX;
    }

    public NumberSetting getFpsPingY() {
        return fpsPingY;
    }

    public NumberSetting getPingX() {
        return pingX;
    }

    public NumberSetting getPingY() {
        return pingY;
    }

    public NumberSetting getToggleSprintX() {
        return toggleSprintX;
    }

    public NumberSetting getToggleSprintY() {
        return toggleSprintY;
    }

    public NumberSetting getReachX() {
        return reachX;
    }

    public NumberSetting getReachY() {
        return reachY;
    }

    public NumberSetting getArmorX() {
        return armorX;
    }

    public NumberSetting getArmorY() {
        return armorY;
    }

    public NumberSetting getScale() {
        return scale;
    }

    public BooleanSetting getNoHurtCam() {
        return noHurtCam;
    }

    private static int parseColorInput(String input, int fallback) {
        if (input == null)
            return fallback;
        String s = input.trim();
        try {
            if (s.startsWith("#"))
                s = s.substring(1);
            if (s.startsWith("0x") || s.startsWith("0X"))
                s = s.substring(2);
            int val = (int) Long.parseLong(s, 16);
            if (s.length() <= 6) {
                val |= 0xFF000000; // ajoute alpha si absent
            }
            return val;
        } catch (Exception e) {
            return fallback;
        }
    }

        private static String colorToHex(int c) {
        return String.format("#%08X", c);
    }

    private static class HudConfig {
        boolean keystrokes = true;
        boolean cps = true;
        boolean fps = true;
        boolean ping = true;
        boolean reach = true;
        boolean armor = true;
        boolean toggleSprint = true;
        boolean toggleSneak = true;
        boolean entityHearts = true;
        boolean fullBright = false;
        boolean noHurtCam = false;
        boolean coords = true;
        boolean potions = true;
        double labelX = 6.0;
        double labelY = 6.0;
        double keystrokesX = 627.0;
        double keystrokesY = 8.0;
        double cpsX = 6.0;
        double cpsY = 30.0;
        double fpsPingX = 6.0;
        double fpsPingY = 50.0;
        double pingX = 6.0;
        double pingY = 70.0;
        double reachX = 6.0;
        double reachY = 90.0;
        double toggleSprintX = 6.0;
        double toggleSprintY = 110.0;
        double toggleSneakX = 6.0;
        double toggleSneakY = 126.0;
        double armorX = 6.0;
        double armorY = 145.0;
        double potionsX = 46.0;
        double potionsY = 146.0;
        double coordsX = 86.0;
        double coordsY = 6.0;
        double scale = 1.0;
        int labelColor = 0xFFFF0000;
        int cpsColor = 0xFFFF0000;
        int fpsColor = 0xFFFF0000;
        int pingColor = 0xFFFF0000;
        int reachColor = 0xFFFF0000;
        int toggleSprintColor = 0xFFFF0000;
        int toggleSneakColor = 0xFFFF0000;
        int armorColor = 0xFFFF0000;
        int potionsColor = 0xFFFF0000;
        int coordsColor = 0xFFFF0000;
        int keystrokesColor = 0xFFFFFFFF;
        int overlayBgColor = 0xF020202E;
        boolean labelBg = true;
        boolean cpsBg = true;
        boolean fpsBg = true;
        boolean pingBg = true;
        boolean reachBg = true;
        boolean toggleSprintBg = true;
        boolean toggleSneakBg = true;
        boolean armorBg = true;
        boolean potionsBg = true;
        boolean coordsBg = true;
        boolean keystrokesBg = true;
        boolean keystrokesRainbow = false;
        boolean removeChatBackground = false;
        boolean autoGg = false;
        int autoGgMessageIndex = 0;
        boolean fpsOptimizer = false;
    }

    private static String toggleLabel(String name, boolean value) {
        return name + ": " + (value ? "ON" : "OFF");
    }

    /**
     * Écran simple de configuration HUD (toggles + scale).
     */
    private static class HudSettingsScreen extends GuiScreen {
        private final HudOverlay hud;

        public HudSettingsScreen(HudOverlay hud) {
            this.hud = hud;
        }

        @Override
        public void initGui() {
            int centerX = this.width / 2;
            int y = this.height / 4;
            this.buttonList.clear();
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(1, centerX - 100, y, 200, 20,
                    toggleLabel("Keystrokes", hud.showKeystrokes.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(2, centerX - 100, y, 200, 20,
                    toggleLabel("CPS", hud.showCps.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(3, centerX - 100, y, 200, 20,
                    toggleLabel("FPS", hud.showFps.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(4, centerX - 100, y, 200, 20,
                    toggleLabel("Ping", hud.showPing.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(5, centerX - 100, y, 200, 20,
                    toggleLabel("Reach", hud.showReach.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(6, centerX - 100, y, 200, 20,
                    toggleLabel("Armor", hud.showArmor.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(7, centerX - 100, y, 200, 20,
                    toggleLabel("ToggleSprint", hud.toggleSprint.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(8, centerX - 100, y, 200, 20,
                    toggleLabel("ToggleSneak", hud.toggleSneak.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(9, centerX - 100, y, 200, 20,
                    toggleLabel("EntityHearts", hud.showEntityHearts.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(10, centerX - 100, y, 200, 20,
                    toggleLabel("FullBright", hud.fullBright.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(11, centerX - 100, y, 200, 20,
                    toggleLabel("NoHurtCam", hud.noHurtCam.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(12, centerX - 100, y, 200, 20,
                    "HUD Style"));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(13, centerX - 100, y, 200, 20,
                    toggleLabel("FPS Optimizer", hud.fpsOptimizer.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(0, centerX - 100, y, 200, 20, "Retour"));
        }

        @Override
        protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
            switch (button.id) {
                case 1:
                    hud.showKeystrokes.set(!hud.showKeystrokes.get());
                    button.displayString = toggleLabel("Keystrokes", hud.showKeystrokes.get());
                    break;
                case 2:
                    hud.showCps.set(!hud.showCps.get());
                    button.displayString = toggleLabel("CPS", hud.showCps.get());
                    break;
                case 3:
                    hud.showFps.set(!hud.showFps.get());
                    button.displayString = toggleLabel("FPS", hud.showFps.get());
                    break;
                case 4:
                    hud.showPing.set(!hud.showPing.get());
                    button.displayString = toggleLabel("Ping", hud.showPing.get());
                    break;
                case 5:
                    hud.showReach.set(!hud.showReach.get());
                    button.displayString = toggleLabel("Reach", hud.showReach.get());
                    break;
                case 6:
                    hud.showArmor.set(!hud.showArmor.get());
                    button.displayString = toggleLabel("Armor", hud.showArmor.get());
                    break;
                case 7:
                    hud.toggleSprint.set(!hud.toggleSprint.get());
                    button.displayString = toggleLabel("ToggleSprint", hud.toggleSprint.get());
                    break;
                case 8:
                    hud.toggleSneak.set(!hud.toggleSneak.get());
                    button.displayString = toggleLabel("ToggleSneak", hud.toggleSneak.get());
                    break;
                case 9:
                    hud.showEntityHearts.set(!hud.showEntityHearts.get());
                    button.displayString = toggleLabel("EntityHearts", hud.showEntityHearts.get());
                    break;
                case 10:
                    hud.fullBright.set(!hud.fullBright.get());
                    button.displayString = toggleLabel("FullBright", hud.fullBright.get());
                    break;
                case 11:
                    hud.noHurtCam.set(!hud.noHurtCam.get());
                    button.displayString = toggleLabel("NoHurtCam", hud.noHurtCam.get());
                    break;
                case 12:
                    mc.displayGuiScreen(new HudStyleScreen(hud));
                    break;
                case 13:
                    hud.fpsOptimizer.set(!hud.fpsOptimizer.get());
                    button.displayString = toggleLabel("FPS Optimizer", hud.fpsOptimizer.get());
                    hud.handleFpsOptimizer();
                    break;
                case 0:
                    mc.displayGuiScreen(new GuiIngameMenu());
                    break;
                default:
                    break;
            }
            super.actionPerformed(button);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, "HUD Settings", this.width / 2, this.height / 4 - 12,
                    0xFFFFFF);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            super.keyTyped(typedChar, keyCode);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }

        @Override
        public void onGuiClosed() {
            hud.saveConfig();
            super.onGuiClosed();
        }

    }

    /**
     * Configuration minimaliste du chat (background + AutoGG).
     */
    private static class ChatSettingsScreen extends GuiScreen {
        private final HudOverlay hud;

        public ChatSettingsScreen(HudOverlay hud) {
            this.hud = hud;
        }

        @Override
        public void initGui() {
            int centerX = this.width / 2;
            int y = this.height / 4;
            this.buttonList.clear();
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(1, centerX - 100, y, 200, 20,
                    toggleLabel("Chat Background", hud.removeChatBackground.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(2, centerX - 100, y, 200, 20,
                    toggleLabel("AutoGG", hud.autoGg.get())));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(3, centerX - 100, y, 200, 20,
                    hud.getAutoGgMessageLabel()));
            y += 24;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(0, centerX - 100, y, 200, 20, "Retour"));
        }

        @Override
        protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
            switch (button.id) {
                case 1:
                    hud.removeChatBackground.set(!hud.removeChatBackground.get());
                    button.displayString = toggleLabel("Chat Background", hud.removeChatBackground.get());
                    hud.updateChatBackgroundState();
                    break;
                case 2:
                    hud.autoGg.set(!hud.autoGg.get());
                    button.displayString = toggleLabel("AutoGG", hud.autoGg.get());
                    break;
                case 3:
                    hud.cycleAutoGgMessage();
                    button.displayString = hud.getAutoGgMessageLabel();
                    break;
                case 0:
                    mc.displayGuiScreen(new GuiIngameMenu());
                    break;
                default:
                    break;
            }
            super.actionPerformed(button);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, "Chat Settings", this.width / 2, this.height / 4 - 12,
                    0xFFFFFF);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }

        @Override
        public void onGuiClosed() {
            hud.saveConfig();
            super.onGuiClosed();
        }
    }

    /**
     * Écran dédié au placement par glisser-déposer des éléments HUD.
     */
    private static class HudPositionScreen extends GuiScreen {
        private final HudOverlay hud;
        private final java.util.List<DraggableWidget> widgets = new java.util.ArrayList<>();
        private DraggableWidget dragging;

        HudPositionScreen(HudOverlay hud) {
            this.hud = hud;
        }

        @Override
        public void initGui() {
            widgets.clear();
            widgets.add(new DraggableWidget("Keystrokes", hud.keystrokesX, hud.keystrokesY, 110, 60, 0x802ECC71));
            widgets.add(new DraggableWidget("CPS", hud.cpsX, hud.cpsY, 80, 14, 0x8040C0FF));
            widgets.add(new DraggableWidget("FPS", hud.fpsPingX, hud.fpsPingY, 90, 14, 0x80FFAA00));
            widgets.add(new DraggableWidget("Ping", hud.pingX, hud.pingY, 90, 14, 0x80FFAA00));
            widgets.add(new DraggableWidget("Reach", hud.reachX, hud.reachY, 70, 14, 0x80FF66FF));
            widgets.add(new DraggableWidget("ToggleSprint", hud.toggleSprintX, hud.toggleSprintY, 110, 14, 0x80FF6666));
            widgets.add(new DraggableWidget("ToggleSneak", hud.toggleSneakX, hud.toggleSneakY, 110, 14, 0x8066FF66));
            widgets.add(new DraggableWidget("Armor", hud.armorX, hud.armorY, 100, 14, 0x80FFFFFF));
            widgets.add(new DraggableWidget("Potions", hud.potionsX, hud.potionsY, 120, 20, 0x8066CCFF));
            widgets.add(new DraggableWidget("Coords", hud.coordsX, hud.coordsY, 140, 14, 0x80CCCCCC));

            int centerX = this.width / 2;
            this.buttonList.clear();
            int baseY = this.height - 50;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(21, centerX - 50, baseY, 100, 20, "Retour"));

            int resetY = 40;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(30, centerX - 100, resetY, 200, 20,
                    "(Keystrokes) Réinitialiser la position"));
            resetY += 22;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(31, centerX - 100, resetY, 200, 20,
                    "(CPS) Réinitialiser la position"));
            resetY += 22;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(32, centerX - 100, resetY, 200, 20,
                    "(FPS) Réinitialiser la position"));
            resetY += 22;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(33, centerX - 100, resetY, 200, 20,
                    "(Ping) Réinitialiser la position"));
            resetY += 22;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(34, centerX - 100, resetY, 200, 20,
                    "(Reach) Réinitialiser la position"));
            resetY += 22;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(35, centerX - 100, resetY, 200, 20,
                    "(ToggleSprint) Réinitialiser la position"));
            resetY += 22;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(37, centerX - 100, resetY, 200, 20,
                    "(ToggleSneak) Réinitialiser la position"));
            resetY += 22;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(36, centerX - 100, resetY, 200, 20,
                    "(Armor) Réinitialiser la position"));
            resetY += 22;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(38, centerX - 100, resetY, 200, 20,
                    "(Potions) Réinitialiser la position"));
            resetY += 22;
            this.buttonList.add(new net.minecraft.client.gui.GuiButton(39, centerX - 100, resetY, 200, 20,
                    "(Coords) Réinitialiser la position"));
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, "HUD Position", this.width / 2, 14, 0xFFFFFF);
            this.drawString(this.fontRendererObj, "Glisse les modules pour repositionner le HUD.", this.width / 2 - 100,
                    28, 0xAAAAAA);
            for (DraggableWidget w : widgets) {
                w.draw(mouseX, mouseY, this.width, this.height, this.fontRendererObj);
            }
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            for (DraggableWidget w : widgets) {
                if (w.isInside(mouseX, mouseY)) {
                    dragging = w;
                    w.startDrag(mouseX, mouseY);
                    break;
                }
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        protected void mouseReleased(int mouseX, int mouseY, int state) {
            if (state == 0 && dragging != null) {
                dragging.stopDrag();
                dragging = null;
            }
            super.mouseReleased(mouseX, mouseY, state);
        }

        @Override
        protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
            if (dragging != null) {
                dragging.dragTo(mouseX, mouseY, this.width, this.height);
            }
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }

        @Override
        public void onGuiClosed() {
            hud.saveConfig();
            super.onGuiClosed();
        }

        @Override
        protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
            switch (button.id) {
                case 21:
                    mc.displayGuiScreen(new GuiIngameMenu());
                    break;
                case 30:
                    hud.keystrokesX.set(627.0);
                    hud.keystrokesY.set(8.0);
                    break;
                case 31:
                    hud.cpsX.set(6.0);
                    hud.cpsY.set(30.0);
                    break;
                case 32:
                    hud.fpsPingX.set(6.0);
                    hud.fpsPingY.set(50.0);
                    break;
                case 33:
                    hud.pingX.set(6.0);
                    hud.pingY.set(70.0);
                    break;
                case 34:
                    hud.reachX.set(6.0);
                    hud.reachY.set(90.0);
                    break;
                case 35:
                    hud.toggleSprintX.set(6.0);
                    hud.toggleSprintY.set(110.0);
                    break;
                case 37:
                    hud.toggleSneakX.set(6.0);
                    hud.toggleSneakY.set(126.0);
                    break;
                case 36:
                    hud.armorX.set(6.0);
                    hud.armorY.set(145.0);
                    break;
                case 38:
                    hud.potionsX.set(46.0);
                    hud.potionsY.set(146.0);
                    break;
                case 39:
                    hud.coordsX.set(86.0);
                    hud.coordsY.set(6.0);
                    break;
                default:
                    break;
            }
            super.actionPerformed(button);
        }
    }

    /**
     * Bloc graphique draggable utilisé par l'écran de position.
     */
    private static class DraggableWidget {
        private final String label;
        private final NumberSetting xSetting;
        private final NumberSetting ySetting;
        private final int width;
        private final int height;
        private final int color;
        private int offsetX;
        private int offsetY;
        private boolean dragging;

        DraggableWidget(String label, NumberSetting xSetting, NumberSetting ySetting, int width, int height,
                int color) {
            this.label = label;
            this.xSetting = xSetting;
            this.ySetting = ySetting;
            this.width = width;
            this.height = height;
            this.color = color;
        }

        void draw(int mouseX, int mouseY, int screenW, int screenH, net.minecraft.client.gui.FontRenderer fr) {
            int x = (int) xSetting.get();
            int y = (int) ySetting.get();
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (color >> 24) & 0xFF;
            int border = 0xFF000000 | (r << 16) | (g << 8) | b;
            int fill = (Math.max(30, a) << 24) | (r << 16) | (g << 8) | b;
            Gui.drawRect(x, y, x + width, y + height, fill);
            Gui.drawRect(x, y, x + width, y + 1, border);
            Gui.drawRect(x, y + height - 1, x + width, y + height, border);
            Gui.drawRect(x, y, x + 1, y + height, border);
            Gui.drawRect(x + width - 1, y, x + width, y + height, border);
            String lbl = label + " (" + (int) xSetting.get() + "," + (int) ySetting.get() + ")";
            fr.drawStringWithShadow(lbl, x + 4, y + 4, 0xFFFFFFFF);
        }

        boolean isInside(int mouseX, int mouseY) {
            int x = (int) xSetting.get();
            int y = (int) ySetting.get();
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        void startDrag(int mouseX, int mouseY) {
            int x = (int) xSetting.get();
            int y = (int) ySetting.get();
            offsetX = mouseX - x;
            offsetY = mouseY - y;
            dragging = true;
        }

        void dragTo(int mouseX, int mouseY, int screenW, int screenH) {
            if (!dragging)
                return;
            int newX = mouseX - offsetX;
            int newY = mouseY - offsetY;
            xSetting.set(newX);
            ySetting.set(newY);
        }

        void stopDrag() {
            dragging = false;
        }

        private int clamp(int val, int min, int max) {
            if (val < min)
                return min;
            if (val > max)
                return max;
            return val;
        }
    }
}
