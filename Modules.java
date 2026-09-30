package uz.dexvisual;

import java.util.ArrayList;
import java.util.List;

public final class Modules {
    public static final List<Module> ALL = new ArrayList<>();

    public static final Module FULLBRIGHT   = reg("Fullbright", false);
    public static final Module ZOOM         = reg("Zoom", true);
    public static final Module ESP_PLAYERS  = reg("ESP Players", false);
    public static final Module ESP_HOSTILE  = reg("ESP Hostile", false);
    public static final Module ESP_ANIMALS  = reg("ESP Animals", false);
    public static final Module ESP_ITEMS    = reg("ESP Items", false);
    public static final Module TARGET_ESP   = reg("Target ESP", true);
    public static final Module HITBOXES     = reg("Hitboxes", false);
    public static final Module NO_HURT_CAM  = reg("No Hurt Cam", false);
    public static final Module WATERMARK    = reg("Watermark", true);
    public static final Module INFO_HUD     = reg("Info HUD", true);
    public static final Module MODULE_LIST  = reg("Module List", true);
    public static final Module KEYSTROKES   = reg("Keystrokes", false);
    public static final Module ARMOR_HUD    = reg("Armor HUD", true);

    private static Module reg(String name, boolean def) {
        Module m = new Module(name, def);
        ALL.add(m);
        return m;
    }

    private Modules() {}
}
