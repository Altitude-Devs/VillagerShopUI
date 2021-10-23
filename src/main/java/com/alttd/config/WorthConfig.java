package com.alttd.config;

import com.alttd.VillagerUI;
import com.alttd.objects.Price;
import com.alttd.util.Utilities;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashMap;
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

    public static Object2ObjectOpenHashMap<Material, Price> prices = new Object2ObjectOpenHashMap<>();

    private static void loadWorth() {
        prices.clear();
        ConfigurationSection worth = config.getConfigurationSection("worth");
        Set<String> points = worth.getKeys(false);
        for (String point : points) {
            ConfigurationSection pointSection = worth.getConfigurationSection(point);
            Set<String> materials = worth.getConfigurationSection(point).getKeys(false);
            for (String key : materials) {
                Material material = Material.getMaterial(key);
                if (material == null) {
                    VillagerUI.getInstance().getLogger().warning("Invalid key in worth.yml -> " + key);
                    continue;
                }
                prices.put(Material.getMaterial(key), new Price(Utilities.round(pointSection.getDouble(key), 2), Integer.parseInt(point)));
            }
        }
    }
}
