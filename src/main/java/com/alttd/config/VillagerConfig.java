package com.alttd.config;

import com.alttd.objects.LoadedVillagers;
import com.alttd.objects.VillagerType;
import com.alttd.util.Logger;
import java.util.UUID;

public class VillagerConfig extends AbstractConfig {

    static VillagerConfig config;
    static int version;

    public VillagerConfig() {
        super("villagerConfig.yml");
    }

    public static void reload() {
        config = new VillagerConfig();

        version = config.getInt("config-version", 1);
        config.set("config-version", 1);

        config.readConfig(VillagerConfig.class, null);
    }

    private static void loadVillagers() {
        LoadedVillagers.clearLoadedVillagers();
        config.getConfigurationSection("").getKeys(false).forEach(key -> {
            VillagerType villagerType = VillagerType.getVillagerType(config.getString(key, ""));
            if (villagerType != null)
                LoadedVillagers.addLoadedVillager(UUID.fromString(key), villagerType);
            else
                Logger.warning("Invalid config entry %.", key);
        });
    }

    public static void addVillager(UUID uuid, VillagerType villagerType) {
        config.set(uuid.toString(), villagerType.getName());
    }

}
