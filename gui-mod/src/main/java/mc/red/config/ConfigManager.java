package mc.red.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import mc.red.mods.Mod;
import mc.red.mods.ModInstances;
import mc.red.mods.setting.CycleSetting;
import mc.red.mods.setting.Setting;
import mc.red.mods.setting.SliderSetting;
import mc.red.mods.setting.ToggleSetting;

public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_PROFILE = "default";

    private static File configDir;
    private static File hiddenDir;
    private static final List<String> profiles = new ArrayList<>();
    private static String selectedProfile = DEFAULT_PROFILE;

    private ConfigManager() {
    }

    public static void init(File baseDirectory) {
        configDir = baseDirectory;
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        hiddenDir = new File(configDir, ".profiles");
        if (!hiddenDir.exists()) {
            hiddenDir.mkdirs();
        }
        reloadProfiles();
        if (profiles.isEmpty()) {
            profiles.add(DEFAULT_PROFILE);
            save(DEFAULT_PROFILE);
        }
        if (!profiles.contains(selectedProfile)) {
            selectedProfile = profiles.get(0);
        }
        load(selectedProfile);
    }

    public static void reloadProfiles() {
        profiles.clear();
        File scanDir = hiddenDir != null ? hiddenDir : configDir;
        if (scanDir == null || !scanDir.exists()) {
            return;
        }
        File[] files = scanDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".dat"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName().substring(0, file.getName().length() - 4);
                profiles.add(name);
            }
            Collections.sort(profiles, String.CASE_INSENSITIVE_ORDER);
        }
    }

    public static List<String> getProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    public static String getSelectedProfile() {
        return selectedProfile;
    }

    public static void cycleProfile() {
        if (profiles.isEmpty()) return;
        int current = profiles.indexOf(selectedProfile);
        if (current < 0) current = 0;
        current = (current + 1) % profiles.size();
        select(profiles.get(current));
    }

    public static void select(String name) {
        if (!profiles.contains(name)) return;
        selectedProfile = name;
        load(name);
    }

    private static File fileFor(String name) {
        File base = hiddenDir != null ? hiddenDir : configDir;
        return new File(base, name + ".dat");
    }

    public static void save(String rawName) {
        String name = sanitize(rawName);
        if (name.isEmpty()) name = DEFAULT_PROFILE;
        JsonObject root = new JsonObject();
        root.addProperty("name", name);
        JsonArray modsArray = new JsonArray();
        for (Mod mod : ModInstances.getAllMods()) {
            JsonObject modObject = new JsonObject();
            modObject.addProperty("mod", mod.name);
            modObject.addProperty("enabled", mod.isEnabled());
            modObject.addProperty("key", mod.getKeyCode());
            JsonObject settingsObject = new JsonObject();
            for (Setting setting : mod.getSettings()) {
                if (setting instanceof ToggleSetting) {
                    settingsObject.addProperty(setting.name, ((ToggleSetting) setting).isEnabled());
                } else if (setting instanceof SliderSetting) {
                    settingsObject.addProperty(setting.name, ((SliderSetting) setting).getValue());
                } else if (setting instanceof CycleSetting) {
                    settingsObject.addProperty(setting.name, ((CycleSetting) setting).getValue());
                }
            }
            modObject.add("settings", settingsObject);
            modsArray.add(modObject);
        }
        root.add("mods", modsArray);
        String json = GSON.toJson(root);
        String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileFor(name)), StandardCharsets.UTF_8)) {
            writer.write(encoded);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!profiles.contains(name)) {
            profiles.add(name);
            Collections.sort(profiles, String.CASE_INSENSITIVE_ORDER);
        }
        selectedProfile = name;
    }

    public static void load(String rawName) {
        String name = sanitize(rawName);
        if (name.isEmpty()) return;
        File file = fileFor(name);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            byte[] decoded = Base64.getDecoder().decode(builder.toString());
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(new String(decoded, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray mods = root.getAsJsonArray("mods");
            if (mods == null) return;
            for (JsonElement el : mods) {
                if (!el.isJsonObject()) continue;
                JsonObject modObj = el.getAsJsonObject();
                if (!modObj.has("mod")) continue;
                String modName = modObj.get("mod").getAsString();
                Mod mod = ModInstances.getModByName(modName);
                if (mod == null) continue;
                if (modObj.has("enabled")) {
                    mod.setEnabled(modObj.get("enabled").getAsBoolean());
                }
                if (modObj.has("key")) {
                    mod.setKeyCode(modObj.get("key").getAsInt());
                }
                JsonObject settingsObj = modObj.getAsJsonObject("settings");
                if (settingsObj == null) continue;
                for (Setting setting : mod.getSettings()) {
                    if (!settingsObj.has(setting.name)) continue;
                    if (setting instanceof ToggleSetting) {
                        ((ToggleSetting) setting).set(settingsObj.get(setting.name).getAsBoolean());
                    } else if (setting instanceof SliderSetting) {
                        ((SliderSetting) setting).setValue(settingsObj.get(setting.name).getAsDouble());
                    } else if (setting instanceof CycleSetting) {
                        ((CycleSetting) setting).setValue(settingsObj.get(setting.name).getAsString());
                    }
                }
            }
            selectedProfile = name;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String sanitize(String input) {
        if (input == null) return "";
        return input.replaceAll("[^A-Za-z0-9 _-]", "").trim();
    }
}
