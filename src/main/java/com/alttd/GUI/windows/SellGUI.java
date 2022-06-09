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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SellGUI extends GUIMerchant {

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

    public SellGUI(VillagerType villagerType, EconUser econUser, boolean bulk) {
        super(MiniMessage.miniMessage().deserialize(Config.SELL_WINDOW, TagResolver.resolver(
                Placeholder.unparsed("trader", villagerType.getDisplayName()),
                Placeholder.unparsed("points", String.valueOf(Objects
                        .requireNonNullElse(econUser.getPointsMap().get(villagerType.getName())
                                , 0))))), villagerType);
        for (ItemStack is : villagerType.getSelling()) {
            ItemStack itemStack = is.clone();
            if (bulk)
                itemStack.setAmount(1);
            Price price = Utilities.getPrice(itemStack, WorthConfig.sell);
            if (price == null)
                continue;
            addItem(itemStack,
                    getPriceItem(price.getPrice(itemStack.getAmount())),
                    null,
                    player -> sell(villagerType, player, itemStack.getType(), itemStack.getAmount(), price, bulk)
            );
        }
    }

    private void sell(VillagerType villagerType, Player player, Material material, int amount, Price price, boolean bulk) {
        EconUser econUser = EconUser.getUser(player.getUniqueId());
        if (econUser == null) {
            player.sendMiniMessage(Config.LOADING_ECON_DATA, null);
            return;
        }

        PlayerInventory inventory = player.getInventory();
        if (bulk)
            amount = Arrays.stream(inventory.getContents())
                    .filter(Objects::nonNull)
                    .filter(itemStack -> itemStack.getType().equals(material))
                    .mapToInt(ItemStack::getAmount).sum();

        int oldPoints = econUser.getPointsMap().getOrDefault(villagerType.getName(), 0);
        int itemPts = price.getPoints();
        int transPts = (itemPts * amount) * -1;
        double cost = price.calculatePriceThing(oldPoints, transPts, false, itemPts);

        Purchase purchase = new Purchase(material, cost, itemPts, transPts, amount);
        setItem(0, getSellItemHover(purchase), null);
        setItem(1, confirm, player1 ->
                sell2(player1, purchase, econUser, villagerType, oldPoints, price, bulk));
        player.updateInventory();
    }

    private void sell2(Player player, Purchase purchase, EconUser econUser, VillagerType villagerType, int oldPoints, Price price, boolean bulk) {
        long newTime = new Date().getTime();
        if ((newTime - 120) > lastClicked)
            lastClicked = newTime;
        else {
            player.sendMiniMessage(Config.CLICKING_TOO_FAST, null);
            return;
        }
        PlayerInventory inventory = player.getInventory();
        if (Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(itemStack -> itemStack.getType().equals(purchase.material()))
                .mapToInt(ItemStack::getAmount).sum()
                < purchase.amount()) {
            player.sendMiniMessage(Config.NOT_ENOUGH_ITEMS, TagResolver.resolver(
                    Placeholder.unparsed("type", purchase.material().name()),
                    Placeholder.unparsed("amount", String.valueOf(purchase.amount()))));
            return;
        }

        Economy econ = VillagerUI.getInstance().getEconomy();
        econ.depositPlayer(player, purchase.price());
        econUser.addPoints(villagerType.getName(), purchase.totalPointCost());

        removeItems(inventory, purchase.material(), purchase.amount());

        int newPoints = econUser.getPointsMap().get(villagerType.getName());
        player.sendMiniMessage(Config.SOLD_ITEM, TagResolver.resolver(
                Placeholder.unparsed("amount", String.valueOf(purchase.amount())),
                Placeholder.unparsed("item", Utilities.capitalize(purchase.material().name()
                        .toLowerCase().replaceAll("_", " "))),
                Placeholder.unparsed("price", String.format("%,.2f", purchase.price())),
                Placeholder.unparsed("points", String.valueOf(-purchase.totalPointCost())),
                Placeholder.unparsed("total_points", String.valueOf(newPoints)),
                Placeholder.unparsed("villager_name", villagerType.getDisplayName())
        ));

        Bukkit.getServer().getPluginManager()
                .callEvent(new SpawnShopEvent(player, purchase, oldPoints, newPoints, false));
        sell(villagerType, player, purchase.material(), bulk ? 1 : purchase.amount(), price, bulk);
    }

    private ItemStack getSellItemHover(Purchase purchase) {
        ItemStack itemStack = new ItemStack(purchase.material());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize(Config.TRANSACTION_ITEM_NAME, TagResolver.resolver(
                Placeholder.unparsed("item_name", purchase.material().name())
        )));
        List<Component> lore = new ArrayList<>();
        for (String entry : Config.TRANSACTION_ITEM_DESCRIPTION) {
            lore.add(miniMessage.deserialize(entry, TagResolver.resolver(
                    Placeholder.unparsed("amount", String.valueOf(purchase.amount())),
                    Placeholder.unparsed("price", String.format("%,.2f", purchase.price())),
                    Placeholder.unparsed("points", String.valueOf(purchase.totalPointCost()))
            )));
        }
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void removeItems(Inventory inventory, Material material, int amount) {
        var ref = new Object() {
            int tmpAmount = amount;
        };

        Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(itemStack -> itemStack.getType().equals(material))
                .forEach(itemStack -> {
                    if (ref.tmpAmount == 0)
                        return;
                    if (itemStack.getAmount() > ref.tmpAmount) {
                        itemStack.setAmount(itemStack.getAmount() - ref.tmpAmount);
                        ref.tmpAmount = 0;
                    } else {
                        ref.tmpAmount -= itemStack.getAmount();
                        itemStack.setAmount(0);
                    }
                });
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
