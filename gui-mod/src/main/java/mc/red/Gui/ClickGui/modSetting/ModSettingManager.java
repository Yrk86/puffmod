package mc.red.Gui.ClickGui.modSetting;

import java.util.ArrayList;

import mc.red.Gui.ClickGui.ClickGui;
import mc.red.Gui.ClickGui.comp.ModButton;
import mc.red.mods.Mod;


public class ModSettingManager {
	public ArrayList<ModSettingGui> modSettingRender = new ArrayList<>();
	
	public static Mod mod = null;
	
	
	public ModSettingManager(int centerW, int centerH) {
		//reset
		modSettingRender.removeAll(modSettingRender);
		
		//add
		System.out.println(centerW);
		for(ModButton modButton : ClickGui.modButtonToRender) {
			this.modSettingRender.add(new ModSettingGui(centerW+205, centerH-100, 200, 200, modButton.mod));
		}
	}
	
	public void render() {
		// settings are rendered inside cards now
		
	}

	public void onClick(int mouseX, int mouseY, int button) {
		// handled in ModButton
	}

	public void onDrag(int mouseX) {
		// handled in ModButton
	}

	public void onRelease() {
		// handled in ModButton
	}
	


}
