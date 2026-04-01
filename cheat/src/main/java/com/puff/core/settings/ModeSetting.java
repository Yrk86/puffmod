package com.puff.core.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Setting de type "mode".
 * Exemple: ModeSetting("FlightMode", "Vanilla", "Vanilla", "Packet")
 */
public class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = new ArrayList<>(Arrays.asList(modes));
        // Clamp le mode au cas où le default n'est pas dans la liste.
        if (!this.modes.contains(defaultMode) && !this.modes.isEmpty()) {
            setValue(this.modes.get(0));
        }
    }

    public List<String> getModes() {
        return modes;
    }

    public String get() {
        String v = getValue();
        return v == null ? "" : v;
    }

    public void set(String mode) {
        if (modes.contains(mode)) setValue(mode);
    }

    public void cycle() {
        if (modes.isEmpty()) return;
        int index = modes.indexOf(getValue());
        index++;
        if (index >= modes.size()) index = 0;
        setValue(modes.get(index));
    }
}

