package mc.red.mods.setting;

public class ToggleSetting extends Setting {
    private boolean value;

    public ToggleSetting(String name, boolean value) {
        super(name);
        this.value = value;
    }

    public boolean isEnabled() {
        return value;
    }

    public void toggle() {
        this.value = !this.value;
    }

    public void set(boolean value) {
        this.value = value;
    }
}
