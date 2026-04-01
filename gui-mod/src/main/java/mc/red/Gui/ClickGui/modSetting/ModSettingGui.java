package mc.red.Gui.ClickGui.modSetting;

import java.awt.Color;
import java.util.List;

import mc.red.mods.Mod;
import mc.red.mods.setting.CycleSetting;
import mc.red.mods.setting.Setting;
import mc.red.mods.setting.SliderSetting;
import mc.red.mods.setting.ToggleSetting;
import mc.red.util.render.RoundedUtil;
import net.minecraft.client.Minecraft;

public class ModSettingGui {
	public Mod mod;
	public int x,y,w,h;
	private SliderSetting draggingSlider = null;
	
	public ModSettingGui(int x, int y, int w, int h,Mod mod) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.mod = mod;
	}
	
	
	public void render() {
		RoundedUtil.drawRoundedRect(x , y , x+w , y+h, 8, new Color(16, 18, 28,235).getRGB());
		RoundedUtil.drawRoundedRect(x , y , x+w , y+10, 8, new Color(255, 47, 146,255).getRGB());
		Minecraft.getMinecraft().fontRendererObj.drawString(mod.name + " : " + mod.isEnabled(), x + 3, y + 13, new Color(235,225,238,255).getRGB());
		Minecraft.getMinecraft().fontRendererObj.drawString(mod.description, x + 3, y + 23, new Color(190,185,195,255).getRGB());

		int offsetY = y + 40;
		int lineHeight = 18;
		List<Setting> settings = mod.getSettings();
		for (Setting setting : settings) {
			if (setting instanceof ToggleSetting) {
				drawToggle((ToggleSetting) setting, offsetY);
			} else if (setting instanceof SliderSetting) {
				drawSlider((SliderSetting) setting, offsetY);
			} else if (setting instanceof CycleSetting) {
				drawCycle((CycleSetting) setting, offsetY);
			}
			offsetY += lineHeight;
		}
	}

	public boolean onClick(int mouseX, int mouseY, int button) {
		int offsetY = y + 40;
		int lineHeight = 18;
		for (Setting setting : mod.getSettings()) {
			if (mouseY >= offsetY && mouseY <= offsetY + lineHeight) {
				if (setting instanceof ToggleSetting) {
					((ToggleSetting) setting).toggle();
					return true;
				}
				if (setting instanceof CycleSetting) {
					((CycleSetting) setting).next();
					return true;
				}
				if (setting instanceof SliderSetting) {
					SliderSetting slider = (SliderSetting) setting;
					if (mouseX >= x + w - 120 && mouseX <= x + w - 20) {
						double ratio = (mouseX - (x + w - 120)) / 100.0;
						double newVal = slider.getMin() + (slider.getMax() - slider.getMin()) * ratio;
						slider.setValue(newVal);
						draggingSlider = slider;
						return true;
					}
				}
			}
			offsetY += lineHeight;
		}
		return false;
	}

	public void onMouseRelease() {
		draggingSlider = null;
	}

	public void onMouseDrag(int mouseX) {
		if (draggingSlider != null) {
			int barX = x + w - 120;
			int barW = 100;
			double ratio = (mouseX - barX) / (double) barW;
			double newVal = draggingSlider.getMin() + (draggingSlider.getMax() - draggingSlider.getMin()) * ratio;
			draggingSlider.setValue(newVal);
		}
	}

	private void drawToggle(ToggleSetting toggleSetting, int yPos) {
		int textColor = new Color(235, 225, 238, 255).getRGB();
		Minecraft.getMinecraft().fontRendererObj.drawString(toggleSetting.name, x + 4, yPos + 4, textColor);
		int boxX = x + w - 30;
		int boxY = yPos + 2;
		int color = toggleSetting.isEnabled() ? new Color(214, 107, 165, 255).getRGB() : new Color(60, 50, 65, 255).getRGB();
		RoundedUtil.drawRoundedRect(boxX, boxY, boxX + 20, boxY + 12, 4, color);
	}

	private void drawSlider(SliderSetting sliderSetting, int yPos) {
		int textColor = new Color(235, 225, 238, 255).getRGB();
		String label = sliderSetting.name + ": " + String.format("%.2f", sliderSetting.getValue());
		Minecraft.getMinecraft().fontRendererObj.drawString(label, x + 4, yPos + 4, textColor);
		int barX = x + w - 120;
		int barY = yPos + 6;
		int barW = 100;
		RoundedUtil.drawRoundedRect(barX, barY, barX + barW, barY + 8, 3, new Color(45, 38, 55, 255).getRGB());
		double ratio = (sliderSetting.getValue() - sliderSetting.getMin()) / (sliderSetting.getMax() - sliderSetting.getMin());
		int fillW = (int) (barW * ratio);
		RoundedUtil.drawRoundedRect(barX, barY, barX + fillW, barY + 8, 3, new Color(214, 107, 165, 255).getRGB());
	}

	private void drawCycle(CycleSetting cycleSetting, int yPos) {
		int textColor = new Color(235, 225, 238, 255).getRGB();
		String label = cycleSetting.name + ": " + cycleSetting.getValue();
		Minecraft.getMinecraft().fontRendererObj.drawString(label, x + 4, yPos + 4, textColor);
		int boxX = x + w - 80;
		int boxY = yPos + 2;
		RoundedUtil.drawRoundedRect(boxX, boxY, boxX + 70, boxY + 12, 4, new Color(45, 38, 55, 255).getRGB());
		Minecraft.getMinecraft().fontRendererObj.drawString(cycleSetting.getValue(), boxX + 4, boxY + 4, textColor);
	}
}
