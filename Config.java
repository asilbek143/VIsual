package uz.dexvisual;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("dexvisual.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            Map<String, Boolean> map = GSON.fromJson(Files.readString(p), new TypeToken<Map<String, Boolean>>() {}.getType());
            if (map == null) return;
            for (Module m : Modules.ALL) {
                Boolean v = map.get(m.name);
                if (v != null) m.enabled = v;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Map<String, Boolean> map = new LinkedHashMap<>();
            for (Module m : Modules.ALL) map.put(m.name, m.enabled);
            Files.writeString(path(), GSON.toJson(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Config() {}
}
