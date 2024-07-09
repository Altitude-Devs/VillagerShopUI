package com.alttd.config;

import com.alttd.VillagerUI;
import com.alttd.objects.Price;
import com.alttd.objects.PriceRange;
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

    public static Object2ObjectOpenHashMap<Material, Price> buy = new Object2ObjectOpenHashMap<>();
    public static Object2ObjectOpenHashMap<Material, Price> sell = new Object2ObjectOpenHashMap<>();
    public static Object2ObjectOpenHashMap<Material, Double> trade = new Object2ObjectOpenHashMap<>();
    private static void loadWorth() { //TODO test after removing points
        loadWorth("buy", buy);
        loadWorth("sell", sell);
        loadTradeWorth("trade", trade);
    }

    private static void loadTradeWorth(String path, Object2ObjectOpenHashMap<Material, Double> map) {
        map.clear();
        ConfigurationSection worth = config.getConfigurationSection(path);
        if (worth == null) {
            Logger.severe("No ? in worth.yml! Stopping VillagerUI.", path);
            VillagerUI.getInstance().getServer().getPluginManager().disablePlugin(VillagerUI.getInstance());
            return;
        }
        Set<String> materials = worth.getKeys(false);
        for (String key : materials) {
            if (key == null) {
                Logger.severe("Null key in worth.yml?");
                continue;
            }

            Material material;
            material = Material.getMaterial(key);
            if (material == null) {
                Logger.warning("Invalid material % in trade worth", key);
                continue;
            }

            map.put(material, new PriceRange(Utilities.round(worth.getDouble(key + ".lower"), 2), Utilities.round(worth.getDouble(key + ".upper"), 2)).getRandomPrice());
        }
    }

    private static void loadWorth(String path, Object2ObjectOpenHashMap<Material, Price> map) {
        map.clear();
        ConfigurationSection worth = config.getConfigurationSection(path);
        if (worth == null) {
            Logger.severe("No ? in worth.yml! Stopping VillagerUI.", path);
            VillagerUI.getInstance().getServer().getPluginManager().disablePlugin(VillagerUI.getInstance());
            return;
        }
        Set<String> materials = worth.getKeys(false);
        for (String key : materials) {
            if (key == null) {
                Logger.severe("Null key in worth.yml?");
                continue;
            }

            Material material;
            material = Material.getMaterial(key);
            if (material == null) {
                Logger.warning("Invalid material % in trade worth", key);
                continue;
            }
            if (material == null) {
                Logger.warning("Invalid material % in trade worth", key);
                continue;
            }
            map.put(material, new Price(Utilities.round(worth.getDouble(key), 2), material));
        }
    }

    public static int POINT_MOD = 4;
    private static void loadOtherStuff() {
        POINT_MOD = config.getInt("point-mod", POINT_MOD);
    }
}
