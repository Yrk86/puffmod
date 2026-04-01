package mc.red.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mc.red.mods.setting.CycleSetting;
import mc.red.mods.setting.Setting;
import mc.red.mods.setting.SliderSetting;
import mc.red.mods.setting.ToggleSetting;
import mc.red.mods.SelfDestructMod;

public final class ModInstances {

    private ModInstances() {
    }

    private static List<Setting> settings(Setting... s) {
        return new ArrayList<>(Arrays.asList(s));
    }

    // Combat
    private static final Mod AUTO_CLICKER = new Mod("AutoClicker", "Left/Right CPS min/max").withSettings(settings(
            new SliderSetting("Min CPS", 1, 20, 8, 0.5),
            new SliderSetting("Max CPS", 1, 20, 12, 0.5),
            new ToggleSetting("Left Click", true),
            new ToggleSetting("Right Click", false),
            new ToggleSetting("Jitter", false)
    ));
    private static final Mod VELOCITY = new Mod("Velocity", "Configurable anti-knockback").withSettings(settings(
            new SliderSetting("Horizontal", 0, 1, 0.8, 0.05),
            new SliderSetting("Vertical", 0, 1, 0.8, 0.05),
            new ToggleSetting("Randomize", false)
    ));
    private static final Mod SPRINT = new Mod("Sprint", "Always sprint");
    private static final Mod AIM_ASSIST = new Mod("AimAssist", "Assist aim onto targets").withSettings(settings(
            new SliderSetting("Range", 1, 6, 3.2, 0.1),
            new SliderSetting("Speed", 0, 10, 4, 0.1),
            new SliderSetting("FOV", 1, 180, 90, 1),
            new ToggleSetting("Click Aim", true),
            new ToggleSetting("Players", true),
            new ToggleSetting("Mobs", false),
            new ToggleSetting("Animals", false),
            new ToggleSetting("TeamCheck", false)
    ));
    private static final Mod REACH = new Mod("Reach", "Reach limited to 4.0 blocks").withSettings(settings(
            new SliderSetting("Distance", 3, 4, 3.8, 0.01)
    ));
    private static final Mod HIT_SELECT = new Mod("HitSelect", "Hit selection tweak").withSettings(settings(
            new SliderSetting("Chance", 0, 100, 50, 1),
            new SliderSetting("HurtTime", 0, 10, 2, 0.1)
    ));
    private static final Mod W_TAP = new Mod("WTap", "Automated W-tap").withSettings(settings(
            new SliderSetting("Range", 1, 6, 3, 0.1),
            new SliderSetting("Ticks", 1, 10, 4, 1)
    ));
    private static final Mod SILENT_AURA = new Mod("SilentAura", "Silent aura attacks").withSettings(settings(
            new SliderSetting("Range", 1, 6, 3.5, 0.1),
            new SliderSetting("FOV", 1, 180, 120, 1),
            new SliderSetting("APS", 1, 20, 8, 0.5),
            new ToggleSetting("Players Only", true),
            new ToggleSetting("Show Target", true)
    ));

    // Render
    private static final Mod ESP = new Mod("ESP", "2D/3D boxes with distance").withSettings(settings(
            new CycleSetting("Mode", Arrays.asList("Box 3D", "Box 2D"), 0),
            new SliderSetting("Range", 1, 200, 64, 1),
            new ToggleSetting("Tracers", false),
            new ToggleSetting("Boxes", true),
            new ToggleSetting("Distance", true),
            new ToggleSetting("Players", true),
            new ToggleSetting("Mobs", false),
            new ToggleSetting("Animals", false),
            new ToggleSetting("TeamCheck", false)
    ));
    private static final Mod TRACERS = new Mod("Tracers", "Lines to entities");
    private static final Mod FULLBRIGHT = new Mod("Fullbright", "Night vision");
    private static final Mod SEARCH = new Mod("Search", "Highlight targets").withSettings(settings(
            new SliderSetting("Range", 1, 200, 64, 1),
            new ToggleSetting("Diamonds", true),
            new ToggleSetting("Gold", false),
            new ToggleSetting("Iron", false),
            new ToggleSetting("Emerald", false),
            new ToggleSetting("Lapis", false),
            new ToggleSetting("Redstone", false),
            new ToggleSetting("Coal", false)
    ));
    private static final Mod STORAGE_ESP = new Mod("StorageESP", "Chests/barrels highlight").withSettings(settings(
            new ToggleSetting("Chests", true),
            new ToggleSetting("Ender Chests", true),
            new ToggleSetting("Furnaces", false),
            new ToggleSetting("Hoppers", false),
            new ToggleSetting("Dispensers", false)
    ));
    private static final Mod ARROWS = new Mod("Arrows", "Arrow indicators").withSettings(settings(
            new SliderSetting("Radius", 1, 50, 10, 1),
            new SliderSetting("Size", 1, 10, 4, 1),
            new ToggleSetting("Players", true),
            new ToggleSetting("Mobs", false),
            new ToggleSetting("Animals", false),
            new ToggleSetting("TeamCheck", false)
    ));
    private static final Mod ITEM_ESP = new Mod("ItemESP", "Box size and centering fixed").withSettings(settings(
            new ToggleSetting("Box", true),
            new ToggleSetting("NameTags", false)
    ));
    private static final Mod SPAWNER_FINDER = new Mod("SpawnerFinder", "Find spawners").withSettings(settings(
            new SliderSetting("Range", 1, 200, 64, 1)
    ));
    private static final Mod NAME_TAGS = new Mod("NameTags", "Enhanced nametags").withSettings(settings(
            new ToggleSetting("Health", true),
            new ToggleSetting("Distance", true),
            new ToggleSetting("TeamCheck", false)
    ));
    private static final Mod CHAMS = new Mod("Chams", "Colored model rendering").withSettings(settings(
            new ToggleSetting("VisibleOnly", false),
            new ToggleSetting("ThroughWalls", true),
            new SliderSetting("Red", 0, 255, 120, 1),
            new SliderSetting("Green", 0, 255, 120, 1),
            new SliderSetting("Blue", 0, 255, 255, 1),
            new SliderSetting("Alpha", 0, 255, 160, 1)
    ));
    private static final Mod PROJECTILES = new Mod("Projectiles", "Show thrown projectiles").withSettings(settings(
            new ToggleSetting("Arrows", true),
            new ToggleSetting("Pearls", true),
            new ToggleSetting("Potions", true),
            new SliderSetting("Red", 0, 255, 255, 1),
            new SliderSetting("Green", 0, 255, 80, 1),
            new SliderSetting("Blue", 0, 255, 80, 1)
    ));
    private static final Mod TRAJECTORIES = new Mod("Trajectories", "Predict bow/pearl path").withSettings(settings(
            new ToggleSetting("ShowImpact", true),
            new SliderSetting("Red", 0, 255, 255, 1),
            new SliderSetting("Green", 0, 255, 255, 1),
            new SliderSetting("Blue", 0, 255, 255, 1)
    ));
    private static final Mod ANTI_DEBUFF = new Mod("AntiDebuff", "Anti debuff visuals").withSettings(settings(
            new ToggleSetting("NoNausea", true),
            new ToggleSetting("NoBlindness", true),
            new ToggleSetting("NoSlow", true)
    ));
    private static final Mod PROP_HUNT = new Mod("PropHunt", "Prop hunt helper").withSettings(settings(
        new SliderSetting("Red", 0, 255, 255, 1),
        new SliderSetting("Green", 0, 255, 255, 1),
        new SliderSetting("Blue", 0, 255, 255, 1)
    ));

    // Utility
    private static final Mod SCAFFOLD = new Mod("Scaffold", "Bridge assistance").withSettings(settings(
            new SliderSetting("Delay", 0, 500, 100, 10),
            new ToggleSetting("Rotations", false),
            new ToggleSetting("Tower", false)
    ));
    private static final Mod FAKE_LAG = new Mod("FakeLag", "Simulate lag").withSettings(settings(
            new SliderSetting("Delay", 0, 2000, 250, 10)
    ));
    private static final Mod AUTO_PEARL = new Mod("AutoPearl", "Automatic ender pearl");
    private static final Mod BLINK = new Mod("Blink", "Packet blink");
    private static final Mod PANIC = new Mod("Panic", "Quick disable");
    private static final Mod BACKTRACK = new Mod("BackTrack", "Rewind movement");
    private static final Mod SELF_DESTRUCT = new SelfDestructMod();

    // World
    private static final Mod CHEST_STEAL = new Mod("ChestSteal", "Delay + auto-close").withSettings(settings(
            new SliderSetting("Delay", 0, 500, 150, 10),
            new ToggleSetting("Auto Close", true)
    ));
    private static final Mod FAST_PLACE = new Mod("FastPlace", "Faster block place");
    private static final Mod AUTO_TOOL = new Mod("AutoTool", "Pick best tool");
    private static final Mod FREE_CAM = new Mod("FreeCam", "Detach camera");
    private static final Mod MLG = new Mod("MLG", "Auto MLG");

    // Inventory
    private static final Mod AUTO_ARMOR = new Mod("AutoArmor", "Auto equip armor");
    private static final Mod THROW_DEBUFF = new Mod("ThrowDebuff", "Throw debuff pots");
    private static final Mod ARMOR_SWITCH = new Mod("ArmorSwitch", "Switch armor sets");
    private static final Mod AUTO_HEAL = new Mod("AutoHeal", "Heal automatically");
    private static final Mod THROW_POT = new Mod("Throwpot", "Throw pots");
    private static final Mod REFILL = new Mod("Refill", "Hotbar refill");
    private static final Mod INV_CLEANER = new Mod("InvCleaner", "Clean inventory");
    private static final Mod INVENTORY_MANAGER = new Mod("InventoryManager", "Sort items");

    private static final List<Mod> ALL_MODS = Arrays.asList(
            AUTO_CLICKER, VELOCITY, SPRINT, AIM_ASSIST, REACH, HIT_SELECT, W_TAP, SILENT_AURA,
            ESP, TRACERS, FULLBRIGHT, SEARCH, STORAGE_ESP, ARROWS, ITEM_ESP, SPAWNER_FINDER,
            NAME_TAGS, CHAMS, PROJECTILES, TRAJECTORIES, ANTI_DEBUFF, PROP_HUNT,
            SCAFFOLD, FAKE_LAG, AUTO_PEARL, BLINK, PANIC, BACKTRACK, SELF_DESTRUCT,
            CHEST_STEAL, FAST_PLACE, AUTO_TOOL, FREE_CAM, MLG,
            AUTO_ARMOR, THROW_DEBUFF, ARMOR_SWITCH, AUTO_HEAL, THROW_POT, REFILL, INV_CLEANER, INVENTORY_MANAGER
    );

    public static List<Mod> getAllMods() {
        return ALL_MODS;
    }

    public static Mod getModByName(String name) {
        for (Mod mod : ALL_MODS) {
            if (mod.name.equalsIgnoreCase(name)) {
                return mod;
            }
        }
        return null;
    }

    public static Mod getAutoClicker() { return AUTO_CLICKER; }
    public static Mod getVelocity() { return VELOCITY; }
    public static Mod getSprint() { return SPRINT; }
    public static Mod getAimAssist() { return AIM_ASSIST; }
    public static Mod getReach() { return REACH; }
    public static Mod getHitSelect() { return HIT_SELECT; }
    public static Mod getWTap() { return W_TAP; }
    public static Mod getSilentAura() { return SILENT_AURA; }

    public static Mod getEsp() { return ESP; }
    public static Mod getTracers() { return TRACERS; }
    public static Mod getFullbright() { return FULLBRIGHT; }
    public static Mod getSearch() { return SEARCH; }
    public static Mod getStorageEsp() { return STORAGE_ESP; }
    public static Mod getArrows() { return ARROWS; }
    public static Mod getItemEsp() { return ITEM_ESP; }
    public static Mod getSpawnerFinder() { return SPAWNER_FINDER; }
    public static Mod getNameTags() { return NAME_TAGS; }
    public static Mod getChams() { return CHAMS; }
    public static Mod getProjectiles() { return PROJECTILES; }
    public static Mod getTrajectories() { return TRAJECTORIES; }
    public static Mod getAntiDebuff() { return ANTI_DEBUFF; }
    public static Mod getPropHunt() { return PROP_HUNT; }

    public static Mod getScaffold() { return SCAFFOLD; }
    public static Mod getFakeLag() { return FAKE_LAG; }
    public static Mod getAutoPearl() { return AUTO_PEARL; }
    public static Mod getBlink() { return BLINK; }
    public static Mod getPanic() { return PANIC; }
    public static Mod getBackTrack() { return BACKTRACK; }
    public static Mod getSelfDestruct() { return SELF_DESTRUCT; }

    public static Mod getChestSteal() { return CHEST_STEAL; }
    public static Mod getFastPlace() { return FAST_PLACE; }
    public static Mod getAutoTool() { return AUTO_TOOL; }
    public static Mod getFreeCam() { return FREE_CAM; }
    public static Mod getMlg() { return MLG; }

    public static Mod getAutoArmor() { return AUTO_ARMOR; }
    public static Mod getThrowDebuff() { return THROW_DEBUFF; }
    public static Mod getArmorSwitch() { return ARMOR_SWITCH; }
    public static Mod getAutoHeal() { return AUTO_HEAL; }
    public static Mod getThrowPot() { return THROW_POT; }
    public static Mod getRefill() { return REFILL; }
    public static Mod getInvCleaner() { return INV_CLEANER; }
    public static Mod getInventoryManager() { return INVENTORY_MANAGER; }
}
