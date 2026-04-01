package com.puff.core;

import com.puff.core.network.PacketEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager central responsable du cycle de vie des modules.
 * Gère l'enregistrement, l'accès fluide et la délégation des événements.
 */
public class ModuleManager {

    // Spécification: utiliser une ArrayList pour stocker les instances
    private final ArrayList<Module> modules = new ArrayList<>();

    /**
     * Enregistre un ou plusieurs modules dans le framework.
     */
    public void register(Module... modulesToRegister) {
        for (Module module : modulesToRegister) {
            modules.add(module);
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    /**
     * Retourne la liste des modules actuellement activés.
     */
    public List<Module> getEnabledModules() {
        return modules.stream()
                .filter(Module::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * Accès type-safe et fluide à un module via sa classe.
     */
    public <T extends Module> T getModule(Class<T> clazz) {
        return modules.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }
 
    public <T extends Module> void enableModule(Class<T> clazz) {
        T module = getModule(clazz);
        if (module != null) module.setEnabled(true);
    }
 
    public <T extends Module> void disableModule(Class<T> clazz) {
        T module = getModule(clazz);
        if (module != null) module.setEnabled(false);
    }
 
    public <T extends Module> void toggleModule(Class<T> clazz) {
        T module = getModule(clazz);
        if (module != null) module.toggle();
    }
 
    public List<Module> getModulesByCategory(Module.Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(java.util.stream.Collectors.toList());
    }
 
    public void onClientTick() {
        for (Module module : modules) {
            if (module.isEnabled()) module.onTick();
        }
    }

    public void onPrePacketSend() {
        for (Module module : modules) {
            if (module.isEnabled()) module.onPrePacketSend();
        }
    }

    public void onOutgoingPacket(PacketEvent event) {
        for (Module module : modules) {
            if (module.isEnabled()) module.onPacketSend(event);
        }
    }

    public void onIncomingPacket(PacketEvent event) {
        for (Module module : modules) {
            if (module.isEnabled()) module.onPacketReceive(event);
        }
    }

    public void onRender3D(float partialTicks) {
        for (Module module : modules) {
            if (module.isEnabled()) module.onRender3D(partialTicks);
        }
    }
}

