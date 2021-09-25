package com.alttd.config;

import com.alttd.VillagerUI;
import com.alttd.util.Utilities;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Set;

public class WorthConfig extends AbstractConfig {
    static WorthConfig config;
    static int version;

    public WorthConfig() {
        super(new File(System.getProperty("user.home") + File.separator + "share" + File.separator + "configs" + File.separator + "VillagerShopUI"), "worth.yml");
    }

    public static void reload() {
        config = new WorthConfig();

        version = config.getInt("config-version", 1);
        config.set("config-version", 1);

        config.readConfig(WorthConfig.class, null);
    }

    public static Object2DoubleMap<Material> prices = new Object2DoubleOpenHashMap<>();

    private static void loadWorth() {
        prices.clear();
        ConfigurationSection worth = config.getConfigurationSection("worth");
        Set<String> keys = worth.getKeys(false);
        for (String key : keys) {
            Material material = Material.getMaterial(key);
            if (material == null) {
                VillagerUI.getInstance().getLogger().warning("Invalid key in worth.yml -> " + key);
                continue;
            }
            prices.put(Material.getMaterial(key), Utilities.round(worth.getDouble(key), 2));
        }
    }
}
