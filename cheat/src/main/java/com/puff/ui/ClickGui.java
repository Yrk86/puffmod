package com.puff.ui;

import com.puff.PuffMod;
import com.puff.core.Module;
import com.puff.core.settings.Setting;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import com.puff.core.settings.ModeSetting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.Minecraft;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGui extends GuiScreen {
    private final List<Frame> frames = new ArrayList<>();

    public ClickGui() {
        int xOffset = 10;
        for (Module.Category category : Module.Category.values()) {
            frames.add(new Frame(category, xOffset, 10, 100, 15));
            xOffset += 110;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Optionnel : fond légèrement assombri
        drawRect(0, 0, width, height, new Color(0, 0, 0, 100).getRGB());

        for (Frame frame : frames) {
            frame.updatePosition(mouseX, mouseY);
            frame.render(mouseX, mouseY);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Frame frame : frames) {
            frame.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == org.lwjgl.input.Keyboard.KEY_RSHIFT || keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    // --- INNER CLASSES ---

    private class Frame {
        public int x, y, width, height;
        public Module.Category category;
        public boolean dragging;
        public int dragX, dragY;
        public boolean expanded;
        public List<ModuleButton> buttons = new ArrayList<>();

        public Frame(Module.Category category, int x, int y, int width, int height) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.expanded = true;

            int btnY = this.height;
            for (Module m : PuffMod.getModuleManager().getModulesByCategory(category)) {
                if (!(m instanceof com.puff.modules.impl.HudOverlay)) { // HUD Overlay resté hors du ClickGUI cheat
                    buttons.add(new ModuleButton(m, this, btnY));
                    btnY += this.height;
                }
            }
        }

        public void render(int mouseX, int mouseY) {
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            // Header
            drawRect(x, y, x + width, y + height, new Color(30, 30, 30).getRGB());
            drawRect(x, y, x + width, y + 2, new Color(255, 40, 180).getRGB()); // Accent Rose
            fr.drawStringWithShadow(category.getDisplayName(), x + 4, y + 4, -1);
            fr.drawStringWithShadow(expanded ? "-" : "+", x + width - 12, y + 4, -1);

            if (expanded) {
                int startY = y + height;
                for (ModuleButton btn : buttons) {
                    btn.yOffset = startY - y;
                    btn.render(mouseX, mouseY);
                    startY += btn.getHeight();
                }
            }
        }

        public void updatePosition(int mouseX, int mouseY) {
            if (dragging) {
                x = mouseX - dragX;
                y = mouseY - dragY;
            }
        }

        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                if (mouseButton == 0) {
                    dragging = true;
                    dragX = mouseX - x;
                    dragY = mouseY - y;
                } else if (mouseButton == 1) {
                    expanded = !expanded;
                }
            }

            if (expanded) {
                for (ModuleButton btn : buttons) {
                    btn.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            if (state == 0) dragging = false;
            
            if (expanded) {
                for (ModuleButton btn : buttons) {
                    btn.mouseReleased(mouseX, mouseY, state);
                }
            }
        }
    }

    private class ModuleButton {
        public Module module;
        public Frame parent;
        public int yOffset;
        public boolean expanded;
        public List<SettingComponent> settings = new ArrayList<>();

        public ModuleButton(Module module, Frame parent, int yOffset) {
            this.module = module;
            this.parent = parent;
            this.yOffset = yOffset;
            this.expanded = false;

            int setY = this.parent.height;
            for (Setting<?> s : module.getSettings()) {
                if (s instanceof BooleanSetting) settings.add(new BooleanComponent((BooleanSetting) s, this, setY));
                else if (s instanceof NumberSetting) settings.add(new NumberComponent((NumberSetting) s, this, setY));
                else if (s instanceof ModeSetting) settings.add(new ModeComponent((ModeSetting) s, this, setY));
                setY += 15;
            }
        }

        public void render(int mouseX, int mouseY) {
            int x = parent.x;
            int y = parent.y + yOffset;
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

            boolean hovered = mouseX >= x && mouseX <= x + parent.width && mouseY >= y && mouseY <= y + parent.height;
            int bgColor = module.isEnabled() ? new Color(50, 60, 50).getRGB() : new Color(40, 40, 40).getRGB();
            if (hovered) bgColor = new Color(70, 70, 70).getRGB();

            drawRect(x, y, x + parent.width, y + parent.height, bgColor);
            
            String displayName = module.getName();
            if (!settings.isEmpty()) {
                displayName += " [+]";
            }
            fr.drawStringWithShadow(displayName, x + 4, y + 4, module.isEnabled() ? new Color(255, 40, 180).getRGB() : -1);

            if (expanded) {
                int startY = y + parent.height;
                for (SettingComponent set : settings) {
                    set.render(mouseX, mouseY, x, startY);
                    startY += set.getHeight();
                }
            }
        }

        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            int x = parent.x;
            int y = parent.y + yOffset;

            if (mouseX >= x && mouseX <= x + parent.width && mouseY >= y && mouseY <= y + parent.height) {
                if (mouseButton == 0) {
                    module.toggle();
                } else if (mouseButton == 1) {
                    expanded = !expanded;
                }
            }

            if (expanded) {
                int startY = y + parent.height;
                for (SettingComponent set : settings) {
                    set.mouseClicked(mouseX, mouseY, mouseButton, x, startY);
                    startY += set.getHeight();
                }
            }
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            if (expanded) {
                int startY = parent.y + yOffset + parent.height;
                for (SettingComponent set : settings) {
                    set.mouseReleased(mouseX, mouseY, state, parent.x, startY);
                    startY += set.getHeight();
                }
            }
        }

        public int getHeight() {
            int h = parent.height;
            if (expanded) {
                for (SettingComponent s : settings) h += s.getHeight();
            }
            return h;
        }
    }

    private abstract class SettingComponent {
        public abstract void render(int mouseX, int mouseY, int x, int y);
        public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton, int x, int y);
        public abstract void mouseReleased(int mouseX, int mouseY, int state, int x, int y);
        public int getHeight() { return 15; }
    }

    private class BooleanComponent extends SettingComponent {
        BooleanSetting setting;
        public BooleanComponent(BooleanSetting setting, ModuleButton parent, int yOffset) { this.setting = setting; }
        public void render(int mouseX, int mouseY, int x, int y) {
            drawRect(x, y, x + 100, y + 15, new Color(30, 30, 30).getRGB());
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(setting.getName(), x + 4, y + 4, -1);
            if (setting.getValue()) drawRect(x + 85, y + 4, x + 93, y + 12, new Color(255, 40, 180).getRGB());
            else drawRect(x + 85, y + 4, x + 93, y + 12, new Color(80, 80, 80).getRGB());
        }
        public void mouseClicked(int mouseX, int mouseY, int btn, int x, int y) {
            if (mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + 15) {
                setting.setValue(!setting.getValue());
            }
        }
        public void mouseReleased(int mouseX, int mouseY, int state, int x, int y) {}
    }

    private class NumberComponent extends SettingComponent {
        NumberSetting setting;
        boolean dragging = false;
        public NumberComponent(NumberSetting setting, ModuleButton parent, int yOffset) { this.setting = setting; }
        public void render(int mouseX, int mouseY, int x, int y) {
            drawRect(x, y, x + 100, y + 15, new Color(30, 30, 30).getRGB());
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(setting.getName() + ": " + String.format("%.1f", setting.getValue()), x + 4, y + 2, -1);
            
            double min = setting.getMin();
            double max = setting.getMax();
            double val = setting.getValue();
            float ratio = (float) ((val - min) / (max - min));
            
            drawRect(x + 4, y + 12, x + 96, y + 14, new Color(80, 80, 80).getRGB()); // fond slider
            drawRect(x + 4, y + 12, x + 4 + (int)(92 * ratio), y + 14, new Color(255, 40, 180).getRGB()); // slider fill

            if (dragging) {
                double diff = Math.min(92, Math.max(0, mouseX - (x + 4)));
                double newValue = min + (diff / 92) * (max - min);
                setting.setValue(newValue);
            }
        }
        public void mouseClicked(int mouseX, int mouseY, int btn, int x, int y) {
            if (mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + 15 && btn == 0) {
                dragging = true;
            }
        }
        public void mouseReleased(int mouseX, int mouseY, int state, int x, int y) {
            dragging = false;
        }
    }

    private class ModeComponent extends SettingComponent {
        ModeSetting setting;
        public ModeComponent(ModeSetting setting, ModuleButton parent, int yOffset) { this.setting = setting; }
        public void render(int mouseX, int mouseY, int x, int y) {
            drawRect(x, y, x + 100, y + 15, new Color(30, 30, 30).getRGB());
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(setting.getName() + ": " + setting.getValue(), x + 4, y + 4, -1);
        }
        public void mouseClicked(int mouseX, int mouseY, int btn, int x, int y) {
            if (mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + 15) {
                setting.cycle();
            }
        }
        public void mouseReleased(int mouseX, int mouseY, int state, int x, int y) {}
    }
}
