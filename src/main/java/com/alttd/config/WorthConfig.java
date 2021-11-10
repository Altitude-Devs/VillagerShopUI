package com.alttd.config;

import com.alttd.VillagerUI;
import com.alttd.objects.Price;
import com.alttd.util.Logger;
import com.alttd.util.Utilities;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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

    public static Object2ObjectOpenHashMap<Material, Price> prices = new Object2ObjectOpenHashMap<>();
    private static void loadWorth() { //TODO test after removing points
        prices.clear();
        ConfigurationSection worth = config.getConfigurationSection("worth");
        if (worth == null) {
            Logger.severe("No worth in worth.yml! Stopping VillagerUI.");
            VillagerUI.getInstance().getServer().getPluginManager().disablePlugin(VillagerUI.getInstance());
            return;
        }
        Set<String> materials = worth.getKeys(false);
        for (String key : materials) {
            if (key == null) {
                Logger.severe("Null key in worth.yml?");
                continue;
            }
            prices.put(Material.getMaterial(key), new Price(Utilities.round(worth.getDouble(key), 2)));
        }
    }
}
