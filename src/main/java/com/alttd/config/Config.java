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

    public static String INITIAL_VILLAGER_WINDOW = "<trader> price: <percentage>%";
    public static String BUY_WINDOW = "<trader> price: <percentage>%";
    public static String SELL_WINDOW = "<trader> price: <percentage>%";

    private static void loadUI() {
        INITIAL_VILLAGER_WINDOW = config.getString("ui.initial-window-name", INITIAL_VILLAGER_WINDOW);
        BUY_WINDOW = config.getString("ui.buy-window-name", BUY_WINDOW);
        SELL_WINDOW = config.getString("ui.sell-window-name", SELL_WINDOW);
    }

    public static String HELP_MESSAGE_WRAPPER = "<gold>VillagerShopUI help:\n<commands></gold>";
    public static String HELP_MESSAGE = "<green>Show this menu: <gold>/villagerui help</gold></green>";
    public static String RELOAD_MESSAGE = "<green>Reload configs: <gold>/villagerui reload</gold></green>";
    public static String CREATE_VILLAGER_MESSAGE = "<green>Create a new trading villager: <gold>/villagerui createvillager <type> <biome> <x> <y> <z> <yaw> <pitch> <world></gold></green>";

    private static void loadHelp() {
        HELP_MESSAGE_WRAPPER = config.getString("help.help-wrapper", HELP_MESSAGE_WRAPPER);
        HELP_MESSAGE = config.getString("help.help", HELP_MESSAGE);
        RELOAD_MESSAGE = config.getString("help.reload", RELOAD_MESSAGE);
        CREATE_VILLAGER_MESSAGE = config.getString("help.create-villager", CREATE_VILLAGER_MESSAGE);
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
    public static String PURCHASED_ITEM = "<green>You bought <amount> <item> for <price>!</green>";
    public static String SOLD_ITEM = "<green>You sold <amount> <item> for <price>!</green>";
    private static void loadMessages() {
        NOT_ENOUGH_MONEY = config.getString("messages.not-enough-money", NOT_ENOUGH_MONEY);
        NOT_ENOUGH_ITEMS = config.getString("messages.not-enough-items", NOT_ENOUGH_ITEMS);
        PURCHASED_ITEM = config.getString("messages.purchased-item", PURCHASED_ITEM);
        SOLD_ITEM = config.getString("messages.sold-item", SOLD_ITEM);
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
                    villagerType.getDouble("price-modifier"),
                    villagerType.getString("profession"))
            );
        });
    }

    public static Int2ObjectAVLTreeMap<Range<Double>> pointsRangeMap = new Int2ObjectAVLTreeMap<>();
    private static void loadPointRange() {
        pointsRangeMap.clear();
        Pattern pattern = Pattern.compile("(0|([1-9][0-9]{0,2}))(.[0-9]{1,2})?-(0|([1-9][0-9]{0,2}))(.[0-9]{1,2})?");

        ConfigurationSection configurationSection = config.getConfigurationSection("points");
        if (configurationSection == null) {
            Logger.severe("""
                    No point entries in config (see example). Please add them and restart the plugin.
                    points:
                    \t1: 0.5-1.5
                    \t3: 1.5-2.25
                    \t5: 2.25-0 #2.25 and higher""");
            VillagerUI.getInstance().getServer().getPluginManager().disablePlugin(VillagerUI.getInstance());
            return;
        }
        Set<String> keys = configurationSection.getKeys(false);
        for (String key : keys) {
            int points = Integer.parseInt(key);
            if (points == 0) {
                Logger.warning("Invalid point entry % in config", key);
                continue;
            }

            String range = configurationSection.getString(key);
            if (range == null) {
                Logger.warning("Invalid point value for % in config", key);
                continue;
            }
            Matcher matcher = pattern.matcher(range);
            if (!matcher.matches()) {
                Logger.warning("Invalid point value % for % in config " +
                        "should be double-double (0-2.05)", range, key);
                continue;
            }
            String[] split = range.split("-");
            if (split.length != 2) {
                Logger.severe("""
                        The logic for the regex failed when loading points.
                        key:%
                        value:%""", key, range);
                continue;
            }
            double d1 = Double.parseDouble(split[0]);
            double d2 = Double.parseDouble(split[1]);
            Range<Double> doubleRange;
            if (d2 == 0 && d1 > d2)
                doubleRange = Range.greaterThan(d1);
            else if (d2 > d1)
                doubleRange = Range.closed(d1, d2);
            else {
                Logger.warning("Invalid range d1:% to d2:%, can't be the same, d1 can't be bigger " +
                        "than d2 unless d2 is 0 (infinite)", String.valueOf(d1), String.valueOf(d2));
                continue;
            }
            pointsRangeMap.put(points, doubleRange);
        }
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
