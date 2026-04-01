package com.puff.core;

import com.puff.core.network.PacketEvent;
import com.puff.core.settings.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe de base de tous les modules utility de Puff.
 * Fournit l'accès au moteur Minecraft et aux hooks de cycle de vie.
 */
public abstract class Module {

    public enum Category {
        COMBAT("Combat \u2694\ufe0f"),
        RENDER("Render \ud83d\udc41\ufe0f"),
        UTILITY("Utility \ud83d\udee0\ufe0f"),
        WORLD("World \ud83c\udf0d"),
        INVENTORY("Inventory \ud83d\udcbc");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    protected final Minecraft mc = Minecraft.getMinecraft();
    private final String name;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    /**
     * Appelé dès l'activation du module.
     */
    public abstract void onEnable();

    /**
     * Appelé à la désactivation du module.
     */
    public abstract void onDisable();

    /**
     * Cycle de mise à jour client (uniquement si actif).
     */
    public void onTick() {}

    /**
     * Hook exécuté avant le traitement des mouvements joueur (Mixins).
     */
    public void onPrePacketSend() {}

    /**
     * Hook interceptant tout paquet sortant via NetworkManager (Mixins).
     */
    public void onPacketSend(PacketEvent event) {}

    /**
     * Hook interceptant tout paquet entrant via NetworkManager (Mixins).
     */
    public void onPacketReceive(PacketEvent event) {}

    /**
     * Rendu 3D (RenderWorldLastEvent).
     */
    public void onRender3D(float partialTicks) {}

    /**
     * Enregistre un paramètre (Setting) pour ce module.
     */
    protected <T extends Setting<?>> T registerSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    /**
     * Accès fluide aux paramètres par nom.
     */
    public Optional<Setting<?>> getSetting(String name) {
        return settings.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }
}
