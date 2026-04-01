package com.puff.modules.impl;

import com.puff.core.Module;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

public class FastPlace extends Module {

    private Field rightClickDelayTimerField;

    public FastPlace() {
        super("FastPlace", Category.PLAYER);
        try {
            // Mapping habituel Forge / MCP
            rightClickDelayTimerField = Minecraft.class.getDeclaredField("rightClickDelayTimer");
            rightClickDelayTimerField.setAccessible(true);
        } catch (Exception e) {
            try {
                // Mapping MCP obfuscated (si nécessaire)
                rightClickDelayTimerField = Minecraft.class.getDeclaredField("field_71467_ac");
                rightClickDelayTimerField.setAccessible(true);
            } catch (Exception ex) {}
        }
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onTick() {
        if (mc.thePlayer == null || rightClickDelayTimerField == null) return;
        try {
            rightClickDelayTimerField.setInt(mc, 0); // Supprime le délai de 4 ticks pour construire vite
        } catch (Exception e) {}
    }
}
