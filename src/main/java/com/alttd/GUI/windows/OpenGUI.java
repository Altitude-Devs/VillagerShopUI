package com.alttd.GUI.windows;

import com.alttd.GUI.GUIInventory;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.objects.EconUser;
import com.alttd.objects.ShopVillagerType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class OpenGUI extends GUIInventory {

    private static final ItemStack BULK_BUY = new ItemStack(Material.GOLD_BLOCK);
    private static final ItemStack BUY = new ItemStack(Material.GOLD_INGOT);
    private static final ItemStack SELL = new ItemStack(Material.BUCKET);
    private static final ItemStack BULK_SELL = new ItemStack(Material.CAULDRON);
    private static final MiniMessage miniMessage;
    static {
        miniMessage = MiniMessage.miniMessage();
        ItemMeta itemMeta;
        {
            itemMeta = BUY.getItemMeta();
            itemMeta.displayName(miniMessage.deserialize("<green>Buy</green>"));
            BUY.setItemMeta(itemMeta);
        }
        {
            itemMeta = SELL.getItemMeta();
            itemMeta.displayName(miniMessage.deserialize("<green>Sell</green>"));
            SELL.setItemMeta(itemMeta);
        }
        {
            itemMeta = BULK_BUY.getItemMeta();
            itemMeta.displayName(miniMessage.deserialize("<green>Bulk Buy</green>"));
            BULK_BUY.setItemMeta(itemMeta);
        }
        {
            itemMeta = BULK_SELL.getItemMeta();
            itemMeta.displayName(miniMessage.deserialize("<green>Bulk Sell</green>"));
            BULK_SELL.setItemMeta(itemMeta);
        }
    }

    public OpenGUI(ShopVillagerType villagerType, EconUser econUser) {
        super(InventoryType.HOPPER, miniMessage.deserialize(Config.INITIAL_VILLAGER_WINDOW,
                TagResolver.resolver(Placeholder.unparsed("trader", villagerType.getDisplayName()),
                    Placeholder.unparsed("points", String.valueOf(Objects.requireNonNullElse(
                            econUser.getPointsMap().get(villagerType.getName()),
                            0)))))
        );
        if (!villagerType.getBuying().isEmpty()) {
            setItem(0, BULK_BUY, player -> new BukkitRunnable() {
                @Override
                public void run() {
                    BuyGUI buyGUI = new BuyGUI(villagerType, econUser, true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            buyGUI.open(player);
                        }
                    }.runTask(VillagerUI.getInstance());
                }
            }.runTaskAsynchronously(VillagerUI.getInstance()));
            setItem(1, BUY, player -> new BukkitRunnable() {
                @Override
                public void run() {
                    BuyGUI buyGUI = new BuyGUI(villagerType, econUser, false);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            buyGUI.open(player);
                        }
                    }.runTask(VillagerUI.getInstance());
                }
            }.runTaskAsynchronously(VillagerUI.getInstance()));
        }
        if (!villagerType.getSelling().isEmpty()) {
            setItem(3, SELL, player -> new BukkitRunnable() {
                @Override
                public void run() {
                    SellGUI sellGUI = new SellGUI(villagerType, econUser, false);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sellGUI.open(player);
                        }
                    }.runTask(VillagerUI.getInstance());
                }
            }.runTaskAsynchronously(VillagerUI.getInstance()));
            setItem(4, BULK_SELL, player -> new BukkitRunnable() {
                @Override
                public void run() {
                    SellGUI sellGUI = new SellGUI(villagerType, econUser, true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sellGUI.open(player);
                        }
                    }.runTask(VillagerUI.getInstance());
                }
            }.runTaskAsynchronously(VillagerUI.getInstance()));
        }
    }
}
