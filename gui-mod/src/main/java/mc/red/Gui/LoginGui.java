package mc.red.Gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.lwjgl.input.Keyboard;

import mc.red.Gui.ClickGui.ClickGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class LoginGui extends GuiScreen {

    private final File keyFile;
    private GuiTextField keyField;
    private String status = "";
    private final String expectedKey;

    public LoginGui(File keyFile, String expectedKey) {
        this.keyFile = keyFile;
        this.expectedKey = expectedKey;
    }

    private static final int PANEL_BG = 0xCC0A0A12;
    private static final int PANEL_OUTLINE = 0x55FF2F92;
    private static final int ACCENT = 0xFFFF2F92;
    private static final int TEXT_COLOR = 0xFFF6F1F6;

    @Override
    public void initGui() {
        int fieldWidth = 180;
        int fieldX = this.width / 2 - fieldWidth / 2;
        int fieldY = this.height / 2;
        this.keyField = new GuiTextField(0, this.fontRendererObj, fieldX + 6, fieldY + 4, fieldWidth - 12, 16);
        this.keyField.setMaxStringLength(64);
        this.keyField.setEnableBackgroundDrawing(false);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, this.width / 2 - 40, fieldY + 26, 80, 18, "Access"));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.keyField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN) {
            validateKey();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.keyField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            validateKey();
        }
    }

    private void validateKey() {
        String entered = keyField.getText().trim();
        if (entered.isEmpty()) {
            status = "Key required";
            return;
        }
        if (!entered.equals(expectedKey)) {
            status = "Invalid key";
            return;
        }
        saveKey(entered);
        Minecraft.getMinecraft().displayGuiScreen(new ClickGui());
    }

    public boolean hasStoredKey() {
        if (!keyFile.exists()) {
            return false;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(keyFile))) {
            String stored = reader.readLine();
            return stored != null && stored.trim().equals(expectedKey);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int panelWidth = 260;
        int panelHeight = 150;
        int panelX = this.width / 2 - panelWidth / 2;
        int panelY = this.height / 2 - panelHeight / 2;
        drawRect(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, PANEL_OUTLINE);
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_BG);
        this.drawCenteredString(this.fontRendererObj, "PuffMod Access", this.width / 2, panelY + 15, ACCENT);
        this.drawCenteredString(this.fontRendererObj, "Enter your key", this.width / 2, panelY + 35, TEXT_COLOR);

        drawRect(panelX + 40, keyField.yPosition - 2, panelX + panelWidth - 40, keyField.yPosition + 20, 0xFF131320);
        drawRect(panelX + 40, keyField.yPosition - 2, panelX + panelWidth - 40, keyField.yPosition + 20, PANEL_OUTLINE & 0x22FFFFFF);
        this.keyField.drawTextBox();

        String message = status;
        if (!message.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, message, this.width / 2, panelY + panelHeight - 30, 0xFFFF5555);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void saveKey(String key) {
        try {
            if (!keyFile.getParentFile().exists()) {
                keyFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(keyFile)) {
                writer.write(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
