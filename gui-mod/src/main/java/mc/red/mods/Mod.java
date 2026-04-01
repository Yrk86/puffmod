package mc.red.mods;

public class Mod {
    public final String name;
    public final String description;
    private boolean enabled;
    private java.util.List<mc.red.mods.setting.Setting> settings = new java.util.ArrayList<>();
    private int keyCode = -1;

    public Mod(String name, String description) {
        this(name, description, false);
    }

    public Mod(String name, String description, boolean enabled) {
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public void toggle() {
        this.enabled = !this.enabled;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getKeyName() {
        if (keyCode < 0) return "None";
        try {
            return org.lwjgl.input.Keyboard.getKeyName(keyCode);
        } catch (Exception e) {
            return "Key " + keyCode;
        }
    }

    public Mod withSettings(java.util.List<mc.red.mods.setting.Setting> settings) {
        this.settings = settings;
        return this;
    }

    public java.util.List<mc.red.mods.setting.Setting> getSettings() {
        return settings;
    }
}
