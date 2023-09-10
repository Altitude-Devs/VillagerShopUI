package com.alttd.GUI.windows;

import com.alttd.GUI.GUIMerchant;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.config.WorthConfig;
import com.alttd.events.SpawnShopEvent;
import com.alttd.objects.BlackMarketVillagerType;
import com.alttd.objects.EconUser;
import com.alttd.util.Logger;
import com.alttd.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TradeGUI extends GUIMerchant {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final ItemStack confirm;

    private long lastClicked = 0;

    static {
        ItemStack itemStack = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(MiniMessage.miniMessage().deserialize(Config.CONFIRM_BUTTON));
        itemStack.setItemMeta(itemMeta);
        confirm = itemStack;
    }

    public TradeGUI(BlackMarketVillagerType villagerType, EconUser econUser) {
        super(MiniMessage.miniMessage().deserialize(Config.TRADE_WINDOW, TagResolver.resolver(
                Placeholder.unparsed("trader", villagerType.getDisplayName()),
                Placeholder.unparsed("remaining_trades", String.valueOf(villagerType.getRemainingTrades(econUser.getUuid()))))
        ), villagerType);
        for (ItemStack is : villagerType.getTrading(econUser.getUuid())) {
            ItemStack itemStack = is.clone();
            Double price = WorthConfig.trade.getOrDefault(itemStack.getType(), null);
            if (price == null) {
                Logger.warning("No price found for" + itemStack.getType().name());
                continue;
            }
            addItem(itemStack,
                    getPriceItem(Utilities.round(price, 2)),
                    null,
                    player -> trade(villagerType, player, itemStack.getType(), itemStack.getAmount(), price)
            );
        }
    }

    private void trade(BlackMarketVillagerType villagerType, Player player, Material material, int amount, Double price) {
        EconUser econUser = EconUser.getUser(player.getUniqueId());
        if (econUser == null) {
            player.sendMiniMessage(Config.LOADING_ECON_DATA, null);
            return;
        }

        setItem(0, getBuyItemHover(material, amount, price), null);
        setItem(1, confirm, player1 ->
                trade2(player1, material, amount, villagerType, price));
        player.updateInventory();
    }

    private ItemStack getBuyItemHover(Material material, int amount, double price) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize(Config.TRANSACTION_ITEM_NAME, TagResolver.resolver(
                Placeholder.unparsed("item_name", material.name())
        )));
        List<Component> lore = new ArrayList<>();
        for (String entry : Config.TRANSACTION_ITEM_DESCRIPTION_NO_POINTS) {
            lore.add(miniMessage.deserialize(entry, TagResolver.resolver(
                    Placeholder.unparsed("amount", String.valueOf(amount)),
                    Placeholder.unparsed("price", String.format("%,.2f", price))
            )));
        }
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void trade2(Player player, Material material, int amount, BlackMarketVillagerType villagerType, double price) {
        long newTime = new Date().getTime();
        if ((newTime - 120) > lastClicked)
            lastClicked = newTime;
        else {
            player.sendMiniMessage(Config.CLICKING_TOO_FAST, null);
            return;
        }

        UUID uuid = player.getUniqueId();
        if (villagerType.getRemainingTrades(uuid) <= 0) {
            player.sendMiniMessage(Config.NO_TRADES_REMAINING, null);
            return;
        }

        Economy econ = VillagerUI.getInstance().getEconomy();
        double balance = econ.getBalance(player);

        if (balance < price) {
            player.sendMiniMessage(Config.NOT_ENOUGH_MONEY, TagResolver.resolver(
                    Placeholder.unparsed("money", String.valueOf(Utilities.round(balance, 2))),
                    Placeholder.unparsed("price", String.format("%,.2f", price))
            ));
            return;
        }

        AtomicInteger atomicInteger = new AtomicInteger(0);
        Arrays.stream(player.getInventory().getContents())
                .filter(itemStack -> itemStack == null || itemStack.getType().equals(material))
                .forEach(itemStack -> {
                    if (itemStack == null)
                        atomicInteger.addAndGet(material.getMaxStackSize());
                    else
                        atomicInteger.addAndGet(itemStack.getMaxStackSize() - itemStack.getAmount());
                });
        if (atomicInteger.get() < amount) {
            player.sendMiniMessage(Config.NOT_ENOUGH_SPACE, TagResolver.resolver(
                    Placeholder.unparsed("space", String.valueOf(atomicInteger.get())),
                    Placeholder.unparsed("amount", String.valueOf(amount))
            ));
            return;
        }
        econ.withdrawPlayer(player, price);
        villagerType.makeTrade(uuid);
        ItemStack itemStack = new ItemStack(material, amount);
        randomizeMetaIfNeeded(material, itemStack);
        player.getInventory().addItem(itemStack);

        player.sendMiniMessage(Config.TRADED_ITEM, TagResolver.resolver(
                Placeholder.parsed("amount", String.valueOf(amount)),
                Placeholder.parsed("item", Utilities.capitalize(material.name()
                        .toLowerCase().replaceAll("_", " "))),
                Placeholder.parsed("price", String.format("%,.2f", price)),
                Placeholder.parsed("villager_name", villagerType.getDisplayName()),
                Placeholder.parsed("trades_remaining", String.valueOf(villagerType.getRemainingTrades(uuid)))
        ));

        Bukkit.getServer().getPluginManager()
                .callEvent(new SpawnShopEvent(player, material, amount, price, true));
        trade(villagerType, player, material, amount, price);
    }

    private void randomizeMetaIfNeeded(Material material, ItemStack itemStack) {
        if (material.equals(Material.AXOLOTL_BUCKET)) {
            AxolotlBucketMeta axolotlBucketMeta = (AxolotlBucketMeta) itemStack.getItemMeta();
            int value = new Random().nextInt(0, Axolotl.Variant.values().length - 1);
            axolotlBucketMeta.setVariant(Axolotl.Variant.values()[value]);
            itemStack.setItemMeta(axolotlBucketMeta);
        } else if (material.equals(Material.TROPICAL_FISH_BUCKET)) {
            TropicalFishBucketMeta tropicalFishBucketMeta = (TropicalFishBucketMeta) itemStack.getItemMeta();
            Random random = new Random();
            int bodyColor = random.nextInt(0, DyeColor.values().length - 1);
            int pattern = random.nextInt(0, TropicalFish.Pattern.values().length - 1);
            int patternColor = random.nextInt(0, DyeColor.values().length - 1);
            tropicalFishBucketMeta.setBodyColor(DyeColor.values()[bodyColor]);
            tropicalFishBucketMeta.setPattern(TropicalFish.Pattern.values()[pattern]);
            tropicalFishBucketMeta.setPatternColor(DyeColor.values()[patternColor]);
            itemStack.setItemMeta(tropicalFishBucketMeta);
        }
    }

    private ItemStack getPriceItem(double price) {
        if (price < 0) return nameItem(new ItemStack(Material.BARRIER), -1);
        else if (price <= 10) return nameItem(new ItemStack(Material.IRON_INGOT), price);
        else if (price <= 100) return nameItem(new ItemStack(Material.GOLD_INGOT), price);
        else if (price <= 500) return nameItem(new ItemStack(Material.DIAMOND), price);
        else return nameItem(new ItemStack(Material.NETHERITE_INGOT), price);
    }

    private ItemStack nameItem(ItemStack itemStack, double price) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<green>" + price + "</green>")); //TODO configurable
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
