package mc.red.Gui.ClickGui.comp;

import java.awt.Color;
import java.util.Locale;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import mc.red.SelfDestructManager;
import mc.red.mods.Mod;
import mc.red.mods.setting.CycleSetting;
import mc.red.mods.setting.Setting;
import mc.red.mods.setting.SliderSetting;
import mc.red.mods.setting.ToggleSetting;
import mc.red.notification.NotificationManager;
import mc.red.util.render.RoundedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;

public class ModButton {

	public int x, y, w, h;
	public Mod mod;
	public int id;
	private SliderSetting draggingSlider = null;
	private boolean listeningForBind = false;
	private int bindBtnX, bindBtnY, bindBtnW, bindBtnH;

	private static final int CARD_RADIUS = 16;
	private static final int HEADER_HEIGHT = 36;
	private static final int SETTING_LINE_HEIGHT = 28;
	private static final int BIND_HEIGHT = 24;
	private static final int CARD_PADDING = 12;
	private static final int CARD_BG_RGB = new Color(18, 18, 26, 235).getRGB();
	private static final int HEADER_BG_RGB = new Color(22, 20, 34, 230).getRGB();
	private static final int CARD_BORDER_RGB = new Color(88, 60, 160, 180).getRGB();
	private static final int TEXT_PRIMARY_RGB = new Color(244, 236, 255, 255).getRGB();
	private static final int TEXT_SECONDARY_RGB = new Color(193, 190, 206, 220).getRGB();
	private static final int SWITCH_TRACK_ACTIVE_RGB = new Color(62, 24, 118, 235).getRGB();
	private static final int SWITCH_TRACK_INACTIVE_RGB = new Color(32, 28, 45, 220).getRGB();
	private static final int SWITCH_KNOB_RGB = new Color(255, 255, 255, 245).getRGB();
	private static final int SWITCH_KNOB_OUTLINE_RGB = new Color(255, 120, 220, 160).getRGB();
	private static final Color SLIDER_GRADIENT_START = new Color(255, 122, 216, 220);
	private static final Color SLIDER_GRADIENT_END = new Color(158, 90, 255, 220);
	private static final int SLIDER_BG_RGB = new Color(26, 23, 36, 220).getRGB();
	private static final int BIND_BG_RGB = new Color(24, 22, 36, 220).getRGB();

	public ModButton(int x, int y, int w, int h, Mod mod, int id) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.mod = mod;
		this.id = id;
	}

	public void render(int mouseX, int mouseY) {
		RoundedUtil.drawRoundedRect(x - 2, y - 2, x + w + 2, y + h + 2, CARD_RADIUS + 2,
				new Color(0, 0, 0, 110).getRGB());
		RoundedUtil.drawRoundedRect(x, y, x + w, y + h, CARD_RADIUS, CARD_BG_RGB);
		RoundedUtil.drawRoundedRect(x + 2, y + 2, x + w - 2, y + HEADER_HEIGHT, CARD_RADIUS - 4, HEADER_BG_RGB);
		RoundedUtil.drawRoundedOutline(x, y, x + w, y + h, CARD_RADIUS, 1.4f, CARD_BORDER_RGB);

		int toggleX = getHeaderToggleX();
		int toggleY = getHeaderToggleY();
		drawHeaderText();
		drawToggleSwitch(toggleX, toggleY, getToggleWidth(), getToggleHeight(), mod.isEnabled(), mouseX, mouseY);

		int yOffset = y + HEADER_HEIGHT;
		for (Setting setting : mod.getSettings()) {
			if (setting instanceof ToggleSetting) {
				drawSettingToggle((ToggleSetting) setting, yOffset, mouseX, mouseY);
			} else if (setting instanceof SliderSetting) {
				drawSettingSlider((SliderSetting) setting, yOffset, mouseX, mouseY);
			} else if (setting instanceof CycleSetting) {
				drawSettingCycle((CycleSetting) setting, yOffset);
			}
			yOffset += SETTING_LINE_HEIGHT;
		}

		int bindY = y + h - BIND_HEIGHT - 10;
		drawBindRow(bindY, mouseX, mouseY);
	}

	private void drawHeaderText() {
		int textColor = mod.isEnabled() ? SWITCH_KNOB_OUTLINE_RGB : TEXT_PRIMARY_RGB;
		Minecraft.getMinecraft().fontRendererObj.drawString(mod.name, x + CARD_PADDING, y + 10, textColor);
	}

	private int getHeaderToggleX() {
		return x + w - CARD_PADDING - getToggleWidth();
	}

	private int getHeaderToggleY() {
		return y + 8;
	}

	private int getToggleWidth() {
		return 44;
	}

	private int getToggleHeight() {
		return 22;
	}

	private void drawToggleSwitch(int tx, int ty, int trackW, int trackH, boolean active, int mouseX, int mouseY) {
		boolean hover = isPointInsideRect(tx, ty, trackW, trackH, mouseX, mouseY);
		int trackColor = active ? SWITCH_TRACK_ACTIVE_RGB : SWITCH_TRACK_INACTIVE_RGB;
		if (hover) {
			Color base = new Color(trackColor);
			int upAlpha = Math.min(255, base.getAlpha() + 25);
			trackColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), upAlpha).getRGB();
		}
		RoundedUtil.drawRoundedRect(tx, ty, tx + trackW, ty + trackH, trackH / 2f, trackColor);
		int knobSize = trackH - 6;
		int knobY = ty + (trackH - knobSize) / 2;
		int knobX = active ? tx + trackW - knobSize - 3 : tx + 3;
		RoundedUtil.drawRoundedRect(knobX, knobY, knobX + knobSize, knobY + knobSize, knobSize / 2f, SWITCH_KNOB_RGB);
		if (active) {
			RoundedUtil.drawRoundedOutline(knobX - 2, knobY - 2, knobX + knobSize + 2, knobY + knobSize + 2, knobSize,
					1f,
					SWITCH_KNOB_OUTLINE_RGB);
		}
	}

	private void drawSettingToggle(ToggleSetting setting, int yPos, int mouseX, int mouseY) {
		Minecraft.getMinecraft().fontRendererObj.drawString(setting.name, x + CARD_PADDING, yPos + 10,
				TEXT_SECONDARY_RGB);
		int toggleX = x + w - CARD_PADDING - getToggleWidth();
		int toggleY = yPos + 2;
		drawToggleSwitch(toggleX, toggleY, getToggleWidth(), getToggleHeight(), setting.isEnabled(), mouseX, mouseY);
	}

	private void drawSettingSlider(SliderSetting setting, int yPos, int mouseX, int mouseY) {
		Minecraft.getMinecraft().fontRendererObj.drawString(setting.name, x + CARD_PADDING, yPos + 8,
				TEXT_SECONDARY_RGB);
		String value = String.format(Locale.US, "%.2f", setting.getValue());
		int valueWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(value);
		int valueX = x + w - CARD_PADDING - valueWidth;
		Minecraft.getMinecraft().fontRendererObj.drawString(value, valueX, yPos + 8, TEXT_PRIMARY_RGB);

		int barX = getSliderBarX();
		int barW = getSliderBarWidth();
		int barY = yPos + 16;
		int barH = 4;
		RoundedUtil.drawRoundedRect(barX, barY, barX + barW, barY + barH, barH / 2f, SLIDER_BG_RGB);
		float ratio = MathHelper.clamp_float(
				(float) ((setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin())), 0.0F, 1.0F);
		int fillW = Math.max(2, (int) (barW * ratio));
		if (fillW > 2) {
			drawHorizontalGradient(barX + 1, barY + 1, fillW - 2, barH - 2, SLIDER_GRADIENT_START, SLIDER_GRADIENT_END);
		}
		int knobSize = 12;
		int knobCenter = (int) (barX + 2 + (barW - 4) * ratio);
		int knobX = MathHelper.clamp_int(knobCenter - knobSize / 2, barX + 1, barX + barW - knobSize - 1);
		int knobY = barY - (knobSize - barH) / 2;
		RoundedUtil.drawRoundedRect(knobX, knobY, knobX + knobSize, knobY + knobSize, knobSize / 2f, SWITCH_KNOB_RGB);
		RoundedUtil.drawRoundedOutline(knobX - 2, knobY - 2, knobX + knobSize + 2, knobY + knobSize + 2, knobSize, 1f,
				SWITCH_KNOB_OUTLINE_RGB);
	}

	private void drawSettingCycle(CycleSetting setting, int yPos) {
		Minecraft.getMinecraft().fontRendererObj.drawString(setting.name, x + CARD_PADDING, yPos + 8,
				TEXT_SECONDARY_RGB);
		int boxW = 90;
		int boxH = 22;
		int boxX = x + w - CARD_PADDING - boxW;
		int boxY = yPos + 2;
		RoundedUtil.drawRoundedRect(boxX, boxY, boxX + boxW, boxY + boxH, 10, new Color(22, 20, 34, 235).getRGB());

		int textX = boxX + boxW / 2 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(setting.getValue()) / 2;
		int textY = boxY + (boxH - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT) / 2;
		Minecraft.getMinecraft().fontRendererObj.drawString(setting.getValue(), textX, textY, TEXT_PRIMARY_RGB);
	}

	private void drawBindRow(int yPos, int mouseX, int mouseY) {
		bindBtnX = x + CARD_PADDING;
		bindBtnY = yPos;
		bindBtnW = w - CARD_PADDING * 2;
		bindBtnH = BIND_HEIGHT;
		boolean hover = isPointInsideRect(bindBtnX, bindBtnY, bindBtnW, bindBtnH, mouseX, mouseY);
		Color base = new Color(hover ? new Color(255, 255, 255, 18).getRGB() : BIND_BG_RGB);
		RoundedUtil.drawRoundedRect(bindBtnX, bindBtnY, bindBtnX + bindBtnW, bindBtnY + bindBtnH, 10, base.getRGB());
		RoundedUtil.drawRoundedOutline(bindBtnX, bindBtnY, bindBtnX + bindBtnW, bindBtnY + bindBtnH, 10, 1.4f,
				new Color(255, 255, 255, 35).getRGB());
		if (listeningForBind) {
			RoundedUtil.drawRoundedOutline(bindBtnX + 2, bindBtnY + 2, bindBtnX + bindBtnW - 2, bindBtnY + bindBtnH - 2,
					8, 1f, new Color(255, 118, 220, 150).getRGB());
		}
		String label = listeningForBind ? "Press a key..." : "Bind: " + mod.getKeyName();
		Minecraft.getMinecraft().fontRendererObj.drawString(label, bindBtnX + 10,
				bindBtnY + (bindBtnH - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT) / 2, TEXT_SECONDARY_RGB);
	}

	private int getSliderBarWidth() {
		int width = w - 110;
		if (width < 90)
			width = 90;
		if (width > 150)
			width = 150;
		return width;
	}

	private int getSliderBarX() {
		return x + w - getSliderBarWidth() - CARD_PADDING;
	}

	private boolean isPointInsideRect(int px, int py, int pw, int ph, int mx, int my) {
		return mx >= px && mx <= px + pw && my >= py && my <= py + ph;
	}

	private void drawHorizontalGradient(int startX, int startY, int width, int height, Color start, Color end) {
		if (width <= 0 || height <= 0)
			return;
		for (int i = 0; i < width; i++) {
			float ratio = width <= 1 ? 1f : i / (float) (width - 1);
			int blended = blendColors(start.getRGB(), end.getRGB(), ratio);
			Gui.drawRect(startX + i, startY, startX + i + 1, startY + height, blended);
		}
	}

	private int blendColors(int colorA, int colorB, float ratio) {
		float inverse = 1f - ratio;
		int a = (int) ((((colorA >> 24) & 0xFF) * inverse) + (((colorB >> 24) & 0xFF) * ratio));
		int r = (int) ((((colorA >> 16) & 0xFF) * inverse) + (((colorB >> 16) & 0xFF) * ratio));
		int g = (int) ((((colorA >> 8) & 0xFF) * inverse) + (((colorB >> 8) & 0xFF) * ratio));
		int b = (int) ((((colorA) & 0xFF) * inverse) + (((colorB) & 0xFF) * ratio));
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public int getRenderHeight() {
		int count = mod.getSettings().size();
		return HEADER_HEIGHT + count * SETTING_LINE_HEIGHT + BIND_HEIGHT + 16;
	}

	public boolean onClick(int mouseX, int mouseY, int button) {
		if (SelfDestructManager.isActive()) {
			return false;
		}
		if (button == 0) {
			if (isPointInsideRect(getHeaderToggleX(), getHeaderToggleY(), getToggleWidth(), getToggleHeight(), mouseX,
					mouseY)) {
				mod.setEnabled(!mod.isEnabled());
				NotificationManager.notifyToggle(mod);
				return true;
			}
			int yOffset = y + HEADER_HEIGHT;
			for (Setting setting : mod.getSettings()) {
				if (mouseY >= yOffset && mouseY <= yOffset + SETTING_LINE_HEIGHT) {
					if (setting instanceof ToggleSetting) {
						((ToggleSetting) setting).toggle();
						return true;
					}
					if (setting instanceof CycleSetting) {
						((CycleSetting) setting).next();
						return true;
					}
					if (setting instanceof SliderSetting) {
						int barX = getSliderBarX();
						int barW = getSliderBarWidth();
						int sliderHitY = yOffset + 10;
						int sliderHitH = 18;
						if (mouseX >= barX && mouseX <= barX + barW && mouseY >= sliderHitY
								&& mouseY <= sliderHitY + sliderHitH) {
							SliderSetting slider = (SliderSetting) setting;
							double ratio = (mouseX - barX) / (double) barW;
							double newVal = slider.getMin()
									+ (slider.getMax() - slider.getMin())
											* MathHelper.clamp_float((float) ratio, 0.0F, 1.0F);
							slider.setValue(newVal);
							draggingSlider = slider;
							return true;
						}
					}
				}
				yOffset += SETTING_LINE_HEIGHT;
			}
		}
		if (button == 0 && isPointInsideRect(bindBtnX, bindBtnY, bindBtnW, bindBtnH, mouseX, mouseY)) {
			listeningForBind = true;
			return true;
		}
		return false;
	}

	public void onRelease(int mouseX, int mouseY, int state) {
		draggingSlider = null;
	}

	public void onRelease() {
		draggingSlider = null;
	}

	public void onKeyTyped(char typedChar, int keyCode) {
		if (listeningForBind) {
			if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
				mod.setKeyCode(0);
			} else {
				mod.setKeyCode(keyCode);
			}
			listeningForBind = false;
		}
	}

	public boolean handleKeyPress(int keyCode) {
		if (listeningForBind) {
			onKeyTyped(' ', keyCode);
			return true;
		}
		return false;
	}

	public void update() {
		if (draggingSlider != null) {
			int barX = getSliderBarX();
			int barW = getSliderBarWidth();
			int mouseX = Mouse.getX() * new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth()
					/ Minecraft.getMinecraft().displayWidth;
			double ratio = (mouseX - barX) / (double) barW;
			double newVal = draggingSlider.getMin()
					+ (draggingSlider.getMax() - draggingSlider.getMin())
							* MathHelper.clamp_float((float) ratio, 0.0F, 1.0F);
			draggingSlider.setValue(newVal);
		}
	}

	public void onDrag(int mouseX, int mouseY) {
		update();
	}

}
