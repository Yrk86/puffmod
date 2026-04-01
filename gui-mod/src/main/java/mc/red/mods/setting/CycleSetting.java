package mc.red.mods.setting;

import java.util.List;

public class CycleSetting extends Setting {
    private final List<String> options;
    private int index;

    public CycleSetting(String name, List<String> options, int index) {
        super(name);
        this.options = options;
        this.index = Math.max(0, Math.min(options.size() - 1, index));
    }

    public String getValue() {
        return options.get(index);
    }

    public void next() {
        index = (index + 1) % options.size();
    }

    public void setValue(String value) {
        if (value == null) return;
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).equalsIgnoreCase(value)) {
                index = i;
                return;
            }
        }
    }
}
