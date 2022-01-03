package com.alttd.config;

import com.alttd.VillagerUI;
import com.alttd.objects.VillagerType;
import com.alttd.util.Logger;
import com.google.common.collect.Range;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Config extends AbstractConfig {

    static Config config;
    static int version;
    public Config() {
        super(new File(System.getProperty("user.home") + File.separator + "share" + File.separator + "configs" + File.separator + "VillagerShopUI"), "config.yml");
    }

    public static void reload() {
        config = new Config();

        version = config.getInt("config-version", 1);
        config.set("config-version", 1);

        config.readConfig(Config.class, null);
    }

    public static String DRIVER = "mysql";
    public static String IP = "localhost";
    public static String PORT = "3306";
    public static String DATABASE_NAME = "VillagerShopUI";
    public static String USERNAME = "";
    public static String PASSWORD = "";

    private static void loadDatabase() {
        DRIVER = config.getString("database.driver", DRIVER);
        IP = config.getString("database.ip", IP);
        PORT = config.getString("database.port", PORT);
        DATABASE_NAME = config.getString("database.name", DATABASE_NAME);
        USERNAME = config.getString("database.username", USERNAME);
        PASSWORD = config.getString("database.password", PASSWORD);
    }

    public static String INITIAL_VILLAGER_WINDOW = "<trader> points: <points>";
    public static String BUY_WINDOW = "<trader> points: <points>";
    public static String SELL_WINDOW = "<trader> points: <points>";

    private static void loadUI() {
        INITIAL_VILLAGER_WINDOW = config.getString("ui.initial-window-name", INITIAL_VILLAGER_WINDOW);
        BUY_WINDOW = config.getString("ui.buy-window-name", BUY_WINDOW);
        SELL_WINDOW = config.getString("ui.sell-window-name", SELL_WINDOW);
    }

    public static String HELP_MESSAGE_WRAPPER = "<gold>VillagerShopUI help:\n<commands></gold>";
    public static String HELP_MESSAGE = "<green>Show this menu: <gold>/villagerui help</gold></green>";
    public static String RELOAD_MESSAGE = "<green>Reload configs: <gold>/villagerui reload</gold></green>";
    public static String CREATE_VILLAGER_MESSAGE = "<green>Create a new trading villager: <gold>/villagerui createvillager <type> <biome> <x> <y> <z> <yaw> <pitch> <world></gold></green>";
    public static String REMOVE_VILLAGER_MESSAGE = "<green>Removes all existing trading villagers in a 2 block radius: <gold>/villagerui removevillager</gold></green>";

    private static void loadHelp() {
        HELP_MESSAGE_WRAPPER = config.getString("help.help-wrapper", HELP_MESSAGE_WRAPPER);
        HELP_MESSAGE = config.getString("help.help", HELP_MESSAGE);
        RELOAD_MESSAGE = config.getString("help.reload", RELOAD_MESSAGE);
        CREATE_VILLAGER_MESSAGE = config.getString("help.create-villager", CREATE_VILLAGER_MESSAGE);
        REMOVE_VILLAGER_MESSAGE = config.getString("help.remove-villager", REMOVE_VILLAGER_MESSAGE);
    }

    public static String NO_PERMISSION = "<red>You do not have permission to do that.</red>";
    public static String NO_CONSOLE = "<red>You cannot use this command from console.</red>";

    private static void loadGeneric() {
        NO_PERMISSION = config.getString("generic.no-permission", NO_PERMISSION);
        NO_CONSOLE = config.getString("generic.no-console", NO_CONSOLE);
    }

    public static String VILLAGER_NAME = "<green><name></green>";

    private static void loadIDKYET() {//TODO rename
        VILLAGER_NAME = config.getString("idkyet.villager-name", VILLAGER_NAME); //TODO change path
    }

    public static String NOT_ENOUGH_MONEY = "<red>You only have $<money>, you need at least $<price> for this purchase.</red>";
    public static String NOT_ENOUGH_ITEMS = "<red>You only have don't have enough <type> you need at least <amount>.</red>";
    public static String PURCHASED_ITEM = "<green>You bought <amount> <item> for <price> and got <points> points!</green>";
    public static String SOLD_ITEM = "<green>You sold <amount> <item> for <price>!</green>";
    public static String REMOVED_VILLAGER = "<green>Removed villager with uuid <uuid></green>";

    private static void loadMessages() {
        NOT_ENOUGH_MONEY = config.getString("messages.not-enough-money", NOT_ENOUGH_MONEY);
        NOT_ENOUGH_ITEMS = config.getString("messages.not-enough-items", NOT_ENOUGH_ITEMS);
        PURCHASED_ITEM = config.getString("messages.purchased-item", PURCHASED_ITEM);
        SOLD_ITEM = config.getString("messages.sold-item", SOLD_ITEM);
        REMOVED_VILLAGER = config.getString("messages.removed-villager", REMOVED_VILLAGER);
    }

    public static boolean DEBUG = false;

    private static void loadSettings() {
        DEBUG = config.getBoolean("settings.debug", DEBUG);
    }

    private static void loadVillagerTypes() {
        VillagerType.clearVillagerTypes();
        ConfigurationSection configurationSection = config.getConfigurationSection("villager-types");
        if (configurationSection == null) {
            Logger.warning("No villager types found in config.");
            return;
        }

        Set<String> keys = configurationSection.getKeys(false);
        if (keys.isEmpty())
            Logger.warning("No villager types found in config.");

        keys.forEach(key -> {
            ConfigurationSection villagerType = configurationSection.getConfigurationSection(key);
            if (villagerType == null)
                return;

            VillagerType.addVillagerType(new VillagerType(
                    key,
                    villagerType.getString("name"),
                    loadProducts(villagerType.getConfigurationSection("buying")),
                    loadProducts(villagerType.getConfigurationSection("selling")),
                    villagerType.getString("profession"))
            );
        });
    }

    private static HashSet<ItemStack> loadProducts(ConfigurationSection productsSection) {
        HashSet<ItemStack> products = new HashSet<>();
        if (productsSection == null)
            return products;

        productsSection.getKeys(false).forEach(item -> {
            Material material = Material.getMaterial(item);
            if (material == null) {
                Logger.warning("Invalid key in products -> " + item);
                return;
            }
            products.add(new ItemStack(material, productsSection.getInt(item)));
        });

        return products;
    }
}
