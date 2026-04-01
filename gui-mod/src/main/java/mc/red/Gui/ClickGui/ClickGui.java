package mc.red.Gui.ClickGui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import mc.red.Gui.ClickGui.comp.CategoryManager;
import mc.red.Gui.ClickGui.comp.ClickGuiCategoryButton;
import mc.red.Gui.ClickGui.comp.ModButton;
import mc.red.Gui.ClickGui.modSetting.ModSettingManager;
import mc.red.SelfDestructManager;
import mc.red.config.ConfigManager;
import mc.red.mods.ModInstances;
import mc.red.util.render.RoundedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ClickGui extends GuiScreen{

	public static ArrayList<ClickGuiCategoryButton> clickGuiCategoryButton = new ArrayList<>();
	
	public static ArrayList<ModButton> modButtonToRender = new ArrayList<>();

	private static final int[] scrollOffsets = new int[5];
	
	ScaledResolution sr;
	private ModSettingManager msManager;
	
	int backgroundW = 250;
	int centerW;
	int centerH;
	private float guiCenterX;
	private float guiCenterY;
	private int lastScreenWidth;
	private int lastScreenHeight;
	private final String title = "PuffMod V1";
	private final Color bgMain = new Color(7, 7, 14, 250);
	private final Color bgGradientTop = new Color(26, 18, 40, 255);
	private final Color panelBg = new Color(18, 18, 26, 230);
	private final Color panelBorder = new Color(132, 80, 211, 180);
	private final Color cardBg = new Color(18, 18, 26, 230);
	private final Color cardBorder = new Color(92, 52, 165, 170);
	private final Color cardShadow = new Color(0, 0, 0, 110);
	private final Color accent = new Color(255, 113, 210, 255);
	private final Color accentSoft = new Color(255, 122, 230, 200);
	private final Color textPrimary = new Color(246, 246, 248, 255);
	private final Color textSecondary = new Color(189, 187, 205, 210);
	private final Color sidebarBg = new Color(8, 8, 16, 220);
	private final Color sidebarBorder = new Color(255, 255, 255, 30);
	private final Color sidebarActiveBar = new Color(255, 119, 210, 210);
	private final Color sidebarHover = new Color(255, 255, 255, 17);
	private final Color configFieldBg = new Color(21, 21, 28, 230);
	private final Color configFieldBorder = new Color(255, 255, 255, 35);
	private final Color configFieldGlow = new Color(255, 120, 216, 200);
	private float accentPulse = 0.0f;
	private final int gridGap = 18;
	private final int sidebarW = 150;
	private final int maxColumns = 3;
	private int dragOffsetX, dragOffsetY;
	private boolean dragging = false;
	private GuiTextField configField;
	private int configFieldX, configFieldY, configFieldW;
	private int saveBtnX, saveBtnY, saveBtnW, saveBtnH;
	private int profileBtnX, profileBtnY, profileBtnW, profileBtnH;
	private boolean hoveringSaveButton = false;
	private boolean hoveringLoadButton = false;
	private boolean hoveringConfigField = false;
	
	private int getContentStartX() {
		return centerW - backgroundW + sidebarW + 26;
	}
	
	
	@Override
	public void initGui() {
		//Enable Minecrafts blur shader
		try {
			mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/menu_blur.json"));
		} catch (Exception ignored) {
			// Shader missing in some MC installs; continue without blur.
		}
		
		sr = new ScaledResolution(mc);
		lastScreenWidth = sr.getScaledWidth();
		lastScreenHeight = sr.getScaledHeight();
		guiCenterX = lastScreenWidth / 2.0f;
		guiCenterY = lastScreenHeight / 2.0f;
		centerW = Math.round(guiCenterX);
		centerH = Math.round(guiCenterY);
		dragging = false;
		reset();
		int sidebarX0 = centerW - backgroundW + 20;
		int catX = sidebarX0 + 12;
		int catW = sidebarW - 24;
		int catSpacing = 28;
		int catBaseY = centerH - 90;
		String[] categoryNames = {"Combat", "Render", "Utility", "World", "Inventory"};
		for(int i = 0; i < categoryNames.length; i++) {
			this.clickGuiCategoryButton.add(new ClickGuiCategoryButton(catX, catBaseY + i * catSpacing, catW, 26, categoryNames[i], i));
		}
			
		int modButtonW = 260;
		int modButtonH = 25;
		int spaceBetween = 26;

		int baseY = centerH - 80;

		// Combat
		this.modButtonToRender.add(new ModButton(centerW,baseY + 0*spaceBetween, modButtonW, modButtonH, ModInstances.getAutoClicker(),0));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 1*spaceBetween, modButtonW, modButtonH, ModInstances.getVelocity(),0));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 2*spaceBetween, modButtonW, modButtonH, ModInstances.getSprint(),0));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 3*spaceBetween, modButtonW, modButtonH, ModInstances.getAimAssist(),0));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 4*spaceBetween, modButtonW, modButtonH, ModInstances.getReach(),0));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 5*spaceBetween, modButtonW, modButtonH, ModInstances.getHitSelect(),0));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 6*spaceBetween, modButtonW, modButtonH, ModInstances.getWTap(),0));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 7*spaceBetween, modButtonW, modButtonH, ModInstances.getSilentAura(),0));

		// Render
		this.modButtonToRender.add(new ModButton(centerW,baseY + 0*spaceBetween, modButtonW, modButtonH, ModInstances.getEsp(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 1*spaceBetween, modButtonW, modButtonH, ModInstances.getTracers(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 2*spaceBetween, modButtonW, modButtonH, ModInstances.getFullbright(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 3*spaceBetween, modButtonW, modButtonH, ModInstances.getSearch(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 4*spaceBetween, modButtonW, modButtonH, ModInstances.getStorageEsp(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 5*spaceBetween, modButtonW, modButtonH, ModInstances.getArrows(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 6*spaceBetween, modButtonW, modButtonH, ModInstances.getItemEsp(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 7*spaceBetween, modButtonW, modButtonH, ModInstances.getSpawnerFinder(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 8*spaceBetween, modButtonW, modButtonH, ModInstances.getNameTags(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 9*spaceBetween, modButtonW, modButtonH, ModInstances.getChams(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 10*spaceBetween, modButtonW, modButtonH, ModInstances.getProjectiles(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 11*spaceBetween, modButtonW, modButtonH, ModInstances.getTrajectories(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 12*spaceBetween, modButtonW, modButtonH, ModInstances.getAntiDebuff(),1));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 13*spaceBetween, modButtonW, modButtonH, ModInstances.getPropHunt(),1));

		// Utility
		this.modButtonToRender.add(new ModButton(centerW,baseY + 0*spaceBetween, modButtonW, modButtonH, ModInstances.getScaffold(),2));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 1*spaceBetween, modButtonW, modButtonH, ModInstances.getFakeLag(),2));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 2*spaceBetween, modButtonW, modButtonH, ModInstances.getAutoPearl(),2));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 3*spaceBetween, modButtonW, modButtonH, ModInstances.getBlink(),2));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 4*spaceBetween, modButtonW, modButtonH, ModInstances.getPanic(),2));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 5*spaceBetween, modButtonW, modButtonH, ModInstances.getBackTrack(),2));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 6*spaceBetween, modButtonW, modButtonH, ModInstances.getSelfDestruct(),2));

		// World
		this.modButtonToRender.add(new ModButton(centerW,baseY + 0*spaceBetween, modButtonW, modButtonH, ModInstances.getChestSteal(),3));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 1*spaceBetween, modButtonW, modButtonH, ModInstances.getFastPlace(),3));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 2*spaceBetween, modButtonW, modButtonH, ModInstances.getAutoTool(),3));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 3*spaceBetween, modButtonW, modButtonH, ModInstances.getFreeCam(),3));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 4*spaceBetween, modButtonW, modButtonH, ModInstances.getMlg(),3));

		// Inventory
		this.modButtonToRender.add(new ModButton(centerW,baseY + 0*spaceBetween, modButtonW, modButtonH, ModInstances.getAutoArmor(),4));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 1*spaceBetween, modButtonW, modButtonH, ModInstances.getThrowDebuff(),4));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 2*spaceBetween, modButtonW, modButtonH, ModInstances.getArmorSwitch(),4));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 3*spaceBetween, modButtonW, modButtonH, ModInstances.getAutoHeal(),4));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 4*spaceBetween, modButtonW, modButtonH, ModInstances.getThrowPot(),4));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 5*spaceBetween, modButtonW, modButtonH, ModInstances.getRefill(),4));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 6*spaceBetween, modButtonW, modButtonH, ModInstances.getInvCleaner(),4));
		this.modButtonToRender.add(new ModButton(centerW,baseY + 7*spaceBetween, modButtonW, modButtonH, ModInstances.getInventoryManager(),4));
		
		msManager = new ModSettingManager(centerW,centerH);
		configFieldW = sidebarW - 40;
		configFieldX = sidebarX0 + 10;
		configFieldY = centerH + 54;
		configField = new GuiTextField(0, mc.fontRendererObj, configFieldX + 8, configFieldY + 6, configFieldW - 16, 12);
		configField.setMaxStringLength(32);
		configField.setEnableBackgroundDrawing(false);
		configField.setTextColor(textPrimary.getRGB());
		configField.setDisabledTextColour(textPrimary.getRGB());
		configField.setText(ConfigManager.getSelectedProfile());
	}
	@Override
    public void onGuiClosed() {
        //Disable Minecrafts blur shader
		mc.entityRenderer.loadEntityShader(null);
		dragging = false;
        super.onGuiClosed();
        
    }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		updateScreenMetrics();
		updateInteractiveBounds();
		accentPulse = (accentPulse + partialTicks * 0.9f) % (float) (Math.PI * 2);
		super.drawScreen(mouseX, mouseY, partialTicks);
		if(configField != null) {
			configField.updateCursorCounter();
		}

		float glow = 0.6f + 0.4f * (float) Math.sin(accentPulse * 1.2f);
		int glowAlpha = Math.round(120 * glow);
		Color dynamicAccent = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), Math.min(glowAlpha + 100, 255));

		GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();
		RoundedUtil.drawRoundedRect(centerW - backgroundW - 16, centerH - 152, centerW + backgroundW + 16, centerH + 152, 34, new Color(0, 0, 0, 140).getRGB());
		RoundedUtil.drawRoundedRect(centerW - backgroundW - 10, centerH - 148, centerW + backgroundW + 10, centerH + 148, 30, new Color(14, 12, 24, 220).getRGB());
		RoundedUtil.drawRoundedRect(centerW - backgroundW - 6, centerH - 146, centerW + backgroundW + 6, centerH + 146, 26, bgGradientTop.getRGB());
		RoundedUtil.drawRoundedRect(centerW - backgroundW - 2, centerH - 144, centerW + backgroundW + 2, centerH + 144, 24, bgMain.getRGB());
		RoundedUtil.drawRoundedOutline(centerW - backgroundW - 8, centerH - 148, centerW + backgroundW + 8, centerH + 148, 26, 2, new Color(dynamicAccent.getRed(), dynamicAccent.getGreen(), dynamicAccent.getBlue(), 180).getRGB());
		RoundedUtil.drawRoundedRect(centerW - 96, centerH - backgroundW / 4 - 16, centerW + 96, centerH - backgroundW / 4 + 8, 12, new Color(20, 18, 35, 200).getRGB());
		RoundedUtil.drawRoundedOutline(centerW - 96, centerH - backgroundW / 4 - 16, centerW + 96, centerH - backgroundW / 4 + 8, 12, 1.6f, new Color(dynamicAccent.getRed(), dynamicAccent.getGreen(), dynamicAccent.getBlue(), 120).getRGB());
		String header = title;
		mc.fontRendererObj.drawString(header, centerW - mc.fontRendererObj.getStringWidth(header)/2, centerH - backgroundW / 4 - 10, dynamicAccent.getRGB());
		GlStateManager.popMatrix();

		int sidebarX0 = centerW - backgroundW + 20;
		int sidebarX1 = sidebarX0 + sidebarW;
		RoundedUtil.drawRoundedRect(sidebarX0, centerH - 118, sidebarX1, centerH + 118, 24, sidebarBg.getRGB());
		RoundedUtil.drawRoundedOutline(sidebarX0, centerH - 118, sidebarX1, centerH + 118, 24, 1.4f, sidebarBorder.getRGB());

		for(ClickGuiCategoryButton clickGuiCategoryButton :clickGuiCategoryButton) {
			clickGuiCategoryButton.renderButton(mouseX, mouseY);
		}
		if(configField != null) {
			drawConfigControls(mouseX, mouseY);
		}

		int contentStartX = getContentStartX();
		int contentEndX = centerW + backgroundW - 12;
		RoundedUtil.drawRoundedRect(contentStartX - 8, centerH - 136, contentEndX + 6, centerH + 136, 22, cardBg.getRGB());
		RoundedUtil.drawRoundedOutline(contentStartX - 8, centerH - 136, contentEndX + 6, centerH + 136, 22, 1.2f, cardBorder.getRGB());
		RoundedUtil.drawRoundedOutline(contentStartX - 6, centerH - 138, contentEndX + 4, centerH + 138, 24, 1, new Color(dynamicAccent.getRed(), dynamicAccent.getGreen(), dynamicAccent.getBlue(), 70).getRGB());

		if(SelfDestructManager.isActive()) {
			drawSelfDestructOverlay();
			GlStateManager.popAttrib();
			return;
		}
		
		int wheel = Mouse.getDWheel();
		int page = CategoryManager.currentPage;

		ArrayList<ModButton> pageButtons = new ArrayList<>();
		for (ModButton modButton : modButtonToRender) {
			if (modButton.id == page) {
				pageButtons.add(modButton);
			}
		}

		int viewHeight = 275;
		int cardMinWidth = 220;
		int areaW = backgroundW * 2 - sidebarW - 80;
		int columns = Math.max(1, Math.min(maxColumns, areaW / (cardMinWidth + gridGap)));
		int cardW = (areaW - (columns -1)*gridGap)/columns;

		int[] virtualHeights = new int[columns];
		for(int c=0;c<columns;c++) virtualHeights[c] = 0;
		for (ModButton modButton : pageButtons) {
			int cardH = modButton.getRenderHeight();
			int col = 0;
			for(int c=1;c<columns;c++) {
				if(virtualHeights[c] < virtualHeights[col]) col = c;
			}
			virtualHeights[col] += cardH + gridGap;
		}
		int maxColHeight = 0;
		for(int hVal : virtualHeights) maxColHeight = Math.max(maxColHeight, hVal);
		int totalHeight = maxColHeight;
		int maxScroll = Math.max(0, totalHeight - viewHeight);
		int extraScroll = 10;

		if (wheel < 0) {
			scrollOffsets[page] = Math.max(-maxScroll - extraScroll, scrollOffsets[page] - 10);
		} else if (wheel > 0) {
			scrollOffsets[page] = Math.min(0, scrollOffsets[page] + 10);
		}

		int baseY = centerH - 120 + scrollOffsets[page];
		int areaX = getContentStartX();
		float borderGlow = 0.35f + 0.15f * (float) Math.sin(accentPulse * 1.1f);
		int borderAlpha = Math.round(60 + 80 * borderGlow);
		Color pulseColor = new Color(255, 130, 230, borderAlpha);
		int[] colHeights = new int[columns];
		for(int c=0;c<columns;c++) {
			colHeights[c] = baseY;
		}

        for (ModButton modButton : pageButtons) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            this.glScissor(centerW - backgroundW, centerH - 138, backgroundW * 2, viewHeight);

            int cardH = modButton.getRenderHeight();
            int col = 0;
            for(int c=1;c<columns;c++) {
            	if(colHeights[c] < colHeights[col]) col = c;
            }
            int cardX = areaX + col*(cardW + gridGap);
            int cardY = colHeights[col];
            colHeights[col] += cardH + gridGap;

            modButton.x = cardX;
            modButton.y = cardY;
            modButton.w = cardW;
            modButton.h = cardH;
            RoundedUtil.drawRoundedOutline(cardX - 1, cardY - 1, cardX + cardW + 1, cardY + cardH + 1, 16, 1, pulseColor.getRGB());
            modButton.render(mouseX, mouseY);

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
		
        GlStateManager.popAttrib();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if(configField != null) {
			configField.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if(mouseButton == 0) {
			if(mouseX >= centerW - backgroundW && mouseX <= centerW + backgroundW && mouseY >= centerH - 138 && mouseY <= centerH - 116) {
				dragging = true;
				dragOffsetX = mouseX - centerW;
				dragOffsetY = mouseY - centerH;
				return;
			}
			if(isInside(mouseX, mouseY, saveBtnX, saveBtnY, saveBtnW, saveBtnH)) {
				handleSaveConfig();
				return;
			}
			if(isInside(mouseX, mouseY, profileBtnX, profileBtnY, profileBtnW, profileBtnH)) {
				handleCycleProfile();
				return;
			}
		}
		for(ClickGuiCategoryButton clickGuiCategoryButton :clickGuiCategoryButton) {
			clickGuiCategoryButton.onClick(mouseX, mouseY, mouseButton);
		}
		int contentX = getContentStartX();
		if(mouseX >= contentX && mouseX <= (centerW + backgroundW) && mouseY >= (centerH - 138) && mouseY <= (centerH + 138)) {
			for(ModButton modButton : modButtonToRender) {
				if(modButton.id == CategoryManager.currentPage) {
					if(modButton.onClick(mouseX, mouseY, mouseButton)) {
						return;
					}
				}
			}
		}
		
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		if(dragging && clickedMouseButton == 0) {
			setGuiCenter(mouseX - dragOffsetX, mouseY - dragOffsetY);
			return;
		}
		for(ModButton modButton : modButtonToRender) {
			if(modButton.id == CategoryManager.currentPage) {
				modButton.onDrag(mouseX, mouseY);
			}
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if(state == 0 && dragging) {
			dragging = false;
		}
		for(ModButton modButton : modButtonToRender) {
			modButton.onRelease();
		}
	}
	public static ArrayList<ClickGuiCategoryButton> getClickGuiCategoryButton() {
		return clickGuiCategoryButton;
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(configField != null && configField.textboxKeyTyped(typedChar, keyCode)) {
			return;
		}
		if(configField != null && configField.isFocused() && keyCode == Keyboard.KEY_RETURN) {
			handleSaveConfig();
			return;
		}
		if(keyCode == Keyboard.KEY_RSHIFT) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
			return;
		}
		for(ModButton modButton : modButtonToRender) {
			if(modButton.id == CategoryManager.currentPage) {
				if(modButton.handleKeyPress(keyCode)) {
					return;
				}
			}
		}
		super.keyTyped(typedChar, keyCode);
	}

	private void updateScreenMetrics() {
		ScaledResolution current = new ScaledResolution(mc);
		sr = current;
		int screenW = current.getScaledWidth();
		int screenH = current.getScaledHeight();
		if(lastScreenWidth == 0 || lastScreenHeight == 0) {
			lastScreenWidth = screenW;
			lastScreenHeight = screenH;
			if(guiCenterX == 0 && guiCenterY == 0) {
				guiCenterX = screenW / 2.0f;
				guiCenterY = screenH / 2.0f;
			}
		}
		if(screenW != lastScreenWidth || screenH != lastScreenHeight) {
			if(lastScreenWidth != 0 && lastScreenHeight != 0) {
				float widthRatio = screenW / (float) lastScreenWidth;
				float heightRatio = screenH / (float) lastScreenHeight;
				guiCenterX *= widthRatio;
				guiCenterY *= heightRatio;
			}
			lastScreenWidth = screenW;
			lastScreenHeight = screenH;
		}
		clampGuiPosition(screenW, screenH);
		centerW = Math.round(guiCenterX);
		centerH = Math.round(guiCenterY);
	}

	private void updateInteractiveBounds() {
		updateCategoryButtonLayout();
		updateConfigFieldBounds();
	}

	private void updateCategoryButtonLayout() {
		if(clickGuiCategoryButton.isEmpty()) {
			return;
		}
		int catX = centerW - backgroundW + 30;
		int catW = sidebarW - 50;
		int spacing = 23;
		int baseY = centerH - 75;
		for(int i = 0; i < clickGuiCategoryButton.size(); i++) {
			ClickGuiCategoryButton button = clickGuiCategoryButton.get(i);
			button.updateBounds(catX, baseY + i * spacing, catW, 22);
		}
	}

	private void updateConfigFieldBounds() {
		configFieldW = sidebarW - 40;
		int sidebarX0 = centerW - backgroundW + 20;
		configFieldX = sidebarX0 + 10;
		configFieldY = centerH + 54;
		if(configField != null) {
			configField.xPosition = configFieldX + 8;
			configField.yPosition = configFieldY + 6;
		}
	}

	private void setGuiCenter(float newCenterX, float newCenterY) {
		guiCenterX = newCenterX;
		guiCenterY = newCenterY;
		int screenW = sr != null ? sr.getScaledWidth() : lastScreenWidth;
		int screenH = sr != null ? sr.getScaledHeight() : lastScreenHeight;
		clampGuiPosition(screenW, screenH);
		centerW = Math.round(guiCenterX);
		centerH = Math.round(guiCenterY);
		updateInteractiveBounds();
	}

	private void clampGuiPosition(int screenW, int screenH) {
		if(screenW <= 0 || screenH <= 0) {
			return;
		}
		int halfWidth = backgroundW;
		int halfHeight = 130;
		if(screenW < halfWidth * 2) {
			guiCenterX = screenW / 2.0f;
		} else {
			float minX = halfWidth;
			float maxX = screenW - halfWidth;
			guiCenterX = Math.max(minX, Math.min(guiCenterX, maxX));
		}
		if(screenH < halfHeight * 2) {
			guiCenterY = screenH / 2.0f;
		} else {
			float minY = halfHeight;
			float maxY = screenH - halfHeight;
			guiCenterY = Math.max(minY, Math.min(guiCenterY, maxY));
		}
	}
	
	private void drawConfigControls(int mouseX, int mouseY) {
		int fieldH = 22;
		int fieldX = configFieldX;
		int fieldY = configFieldY;
		int fieldW = configFieldW;
		RoundedUtil.drawRoundedRect(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, 8, configFieldBg.getRGB());
		RoundedUtil.drawRoundedOutline(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, 8, 1.5f, configFieldBorder.getRGB());
		hoveringConfigField = false;
		if(configField != null) {
			hoveringConfigField = isInside(mouseX, mouseY, fieldX, fieldY, fieldW, fieldH);
		}
		boolean fieldActive = configField != null && (configField.isFocused() || hoveringConfigField);
		if(configField != null) {
			configField.drawTextBox();
			if(fieldActive) {
				int glowAlpha = configField.isFocused() ? 220 : 140;
				RoundedUtil.drawRoundedOutline(fieldX - 2, fieldY - 2, fieldX + fieldW + 2, fieldY + fieldH + 2, 10, 1.2f,
						new Color(configFieldGlow.getRed(), configFieldGlow.getGreen(), configFieldGlow.getBlue(), glowAlpha).getRGB());
			}
		}

		saveBtnX = fieldX;
		saveBtnY = fieldY + fieldH + 10;
		saveBtnW = (fieldW - 8) / 2;
		saveBtnH = 20;
		profileBtnX = saveBtnX + saveBtnW + 8;
		profileBtnY = saveBtnY;
		profileBtnW = fieldW - saveBtnW - 8;
		profileBtnH = saveBtnH;

		hoveringSaveButton = isInside(mouseX, mouseY, saveBtnX, saveBtnY, saveBtnW, saveBtnH);
		hoveringLoadButton = isInside(mouseX, mouseY, profileBtnX, profileBtnY, profileBtnW, profileBtnH);

		drawConfigButton(saveBtnX, saveBtnY, saveBtnW, saveBtnH, accent.getRGB(), hoveringSaveButton, true, "Save");
		Color secondaryState = hoveringLoadButton ? new Color(255, 255, 255, 25) : panelBg;
		drawConfigButton(profileBtnX, profileBtnY, profileBtnW, profileBtnH, secondaryState.getRGB(), hoveringLoadButton, false, "Load: " + ConfigManager.getSelectedProfile());
	}

	private void drawConfigButton(int x, int y, int w, int h, int baseColor, boolean hovered, boolean centerText, String label) {
		float scale = hovered ? 1.03f : 1.0f;
		float centerX = x + w / 2f;
		float centerY = y + h / 2f;
		GlStateManager.pushMatrix();
		GlStateManager.translate(centerX, centerY, 0);
		GlStateManager.scale(scale, scale, 1f);
		GlStateManager.translate(-centerX, -centerY, 0);
		RoundedUtil.drawRoundedRect(x, y, x + w, y + h, 8, baseColor);
		RoundedUtil.drawRoundedOutline(x, y, x + w, y + h, 8, 1.4f, new Color(255, 255, 255, 40).getRGB());
		GlStateManager.popMatrix();
		int textY = y + (h - this.fontRendererObj.FONT_HEIGHT) / 2;
		if(centerText) {
			this.drawCenteredString(this.fontRendererObj, label, (int) centerX, textY, textPrimary.getRGB());
		} else {
			this.fontRendererObj.drawString(label, x + 10, textY, textSecondary.getRGB());
		}
	}

	private void handleSaveConfig() {
		if(configField == null) return;
		String name = configField.getText().trim();
		if(name.isEmpty()) {
			name = ConfigManager.getSelectedProfile();
		}
		ConfigManager.save(name);
		ConfigManager.reloadProfiles();
		ConfigManager.select(name);
		configField.setText(ConfigManager.getSelectedProfile());
	}

	private void handleCycleProfile() {
		ConfigManager.cycleProfile();
		if(configField != null) {
			configField.setText(ConfigManager.getSelectedProfile());
		}
	}

	private boolean isInside(int mx, int my, int x, int y, int w, int h) {
		return mx >= x && mx <= x + w && my >= y && my <= y + h;
	}

	private void drawSelfDestructOverlay() {
		int overlayColor = 0xAA000000;
		drawRect(centerW - backgroundW, centerH - 130, centerW + backgroundW, centerH + 130, overlayColor);
		this.drawCenteredString(this.fontRendererObj, "Self-destruct active", centerW, centerH - 10, 0xFFFF2F92);
		this.drawCenteredString(this.fontRendererObj, "Type .restore in chat to re-enable", centerW, centerH + 10, 0xFFFFFFFF);
	}
	
	private static void reset() {
		modButtonToRender.removeAll(modButtonToRender);
		clickGuiCategoryButton.removeAll(clickGuiCategoryButton);
		
	}
	
	private void glScissor(double x, double y, double width, double height) {

        y += height;

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        Minecraft mc = Minecraft.getMinecraft();

        GL11.glScissor((int) ((x * mc.displayWidth) / scaledResolution.getScaledWidth()),
                (int) (((scaledResolution.getScaledHeight() - y) * mc.displayHeight) / scaledResolution.getScaledHeight()),
                (int) (width * mc.displayWidth / scaledResolution.getScaledWidth()),
                (int) (height * mc.displayHeight / scaledResolution.getScaledHeight()));
    }

	
	
}
	
	
