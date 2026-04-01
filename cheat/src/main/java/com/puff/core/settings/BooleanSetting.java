package com.puff.core.settings;

/**
 * Setting pour activer/désactiver un comportement.
 */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public boolean get() {
        Boolean v = getValue();
        return v != null && v;
    }

    public void set(boolean value) {
        setValue(value);
    }
}

