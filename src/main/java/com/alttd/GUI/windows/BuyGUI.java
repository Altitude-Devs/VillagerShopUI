package com.alttd.GUI.windows;

import com.alttd.GUI.GUIMerchant;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.config.WorthConfig;
import com.alttd.events.SpawnShopEvent;
import com.alttd.objects.EconUser;
import com.alttd.objects.Price;
import com.alttd.objects.Purchase;
import com.alttd.objects.VillagerType;
import com.alttd.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BuyGUI extends GUIMerchant {

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

    public BuyGUI(VillagerType villagerType, EconUser econUser, boolean bulk) {
        super(miniMessage.deserialize(Config.BUY_WINDOW, TemplateResolver.resolving(
                Template.template("trader", villagerType.getDisplayName()),
                Template.template("points", String.valueOf(Objects.requireNonNullElse(
                        econUser.getPointsMap().get(villagerType.getName()),
                        0)))
        )), villagerType);
        for (ItemStack is : villagerType.getBuying()) {
            ItemStack itemStack = is.clone();
            if (bulk)
                itemStack.setAmount(itemStack.getMaxStackSize());
            Price price = Utilities.getPrice(itemStack, WorthConfig.buy);
            if (price == null)
                continue;
            addItem(itemStack,
                    getPriceItem(price.getPrice(itemStack.getAmount())),
                    null,
                    player -> buy(villagerType, player, itemStack.getType(), itemStack.getAmount(), price)
            );
        }
    }

    private void buy(VillagerType villagerType, Player player, Material material, int amount, Price price) {
        int itemPts = price.getPoints();
        int transPts = itemPts * amount;
        EconUser econUser = EconUser.getUser(player.getUniqueId());
        int oldPoints = econUser.getPointsMap().getOrDefault(villagerType.getName(), 0);
        double cost = price.calculatePriceThing(oldPoints, transPts, true, itemPts);

        Purchase purchase = new Purchase(material, cost, itemPts, transPts, amount);

        ItemStack itemStack = new ItemStack(Material.CANDLE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize(String.valueOf(cost)));
        itemStack.setItemMeta(itemMeta);
        setItem(0, getBuyItemHover(purchase), null);
        setItem(1, confirm, player1 ->
                buy2(player1, purchase, econUser, villagerType, oldPoints, price));
        player.updateInventory();
    }

    private void buy2(Player player, Purchase purchase, EconUser econUser, VillagerType villagerType, int oldPoints, Price price) {
        long newTime = new Date().getTime();
        if ((newTime - 120) > lastClicked)
            lastClicked = newTime;
        else {
            player.sendMiniMessage(Config.CLICKING_TOO_FAST, null);
            return;
        }
        Economy econ = VillagerUI.getInstance().getEconomy();
        double balance = econ.getBalance(player);

        if (balance < purchase.price()) {
            player.sendMiniMessage(Config.NOT_ENOUGH_MONEY, List.of(
                    Template.template("money", String.valueOf(Utilities.round(balance, 2))),
                    Template.template("price", String.format("%,.2f", purchase.price()))
            ));
            return;
        }

        var ref = new Object() {
            int space = 0;
        };
        Arrays.stream(player.getInventory().getContents())
                .filter(itemStack -> itemStack == null || itemStack.getType().equals(purchase.material()))
                .forEach(itemStack -> {
                    if (itemStack == null)
                        ref.space += purchase.material().getMaxStackSize();
                    else
                        ref.space += itemStack.getMaxStackSize() - itemStack.getAmount();
                });
        if (ref.space < purchase.amount()) {
            player.sendMiniMessage(Config.NOT_ENOUGH_SPACE, List.of(
                    Template.template("space", String.valueOf(ref.space)),
                    Template.template("amount", String.valueOf(purchase.amount()))
            ));
            return;
        }

        econ.withdrawPlayer(player, purchase.price());
        econUser.addPoints(villagerType.getName(), purchase.totalPointCost());
        player.getInventory().addItem(new ItemStack(purchase.material(), purchase.amount()));

        int newPoints = econUser.getPointsMap().get(villagerType.getName());
        player.sendMiniMessage(Config.PURCHASED_ITEM, List.of(
                Template.template("amount", String.valueOf(purchase.amount())),
                Template.template("item", StringUtils.capitalize(purchase.material().name()
                        .toLowerCase().replaceAll("_", " "))),
                Template.template("price", String.format("%,.2f", purchase.price())),
                Template.template("points", String.valueOf(purchase.totalPointCost())),
                Template.template("total_points", String.valueOf(newPoints)),
                Template.template("villager_name", villagerType.getDisplayName())
        ));

        Bukkit.getServer().getPluginManager()
                .callEvent(new SpawnShopEvent(player, purchase,
                        oldPoints, newPoints, true));
        buy(villagerType, player, purchase.material(), purchase.amount(), price);
    }

    private ItemStack getBuyItemHover(Purchase purchase) {
        ItemStack itemStack = new ItemStack(purchase.material());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize(Config.TRANSACTION_ITEM_NAME, TemplateResolver.resolving(
                Template.template("item_name", purchase.material().name())
        )));
        List<Component> lore = new ArrayList<>();
        for (String entry : Config.TRANSACTION_ITEM_DESCRIPTION) {
            lore.add(miniMessage.deserialize(entry, TemplateResolver.resolving(
                    Template.template("amount", String.valueOf(purchase.amount())),
                    Template.template("price", String.format("%,.2f", purchase.price())),
                    Template.template("points", String.valueOf(purchase.totalPointCost()))
            )));
        }
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
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
        itemMeta.displayName(miniMessage.deserialize("<red>-" + price + "</red>")); //TODO configurable
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
