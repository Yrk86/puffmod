package mc.red.mods;

import mc.red.SelfDestructManager;

public class SelfDestructMod extends Mod {

    public SelfDestructMod() {
        super("SelfDestruct", "Emergency panic switch");
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            SelfDestructManager.activate(true);
        }
        super.setEnabled(false);
    }
}
