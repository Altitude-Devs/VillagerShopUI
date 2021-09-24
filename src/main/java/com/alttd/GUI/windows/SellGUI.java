package com.alttd.GUI.windows;

import com.alttd.GUI.GUIMerchant;
import com.alttd.config.Config;
import com.alttd.economy.Calculation;
import com.alttd.objects.VillagerType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SellGUI extends GUIMerchant {

    private static final MiniMessage miniMessage = MiniMessage.get();

    public SellGUI(VillagerType villagerType) {
        super(MiniMessage.get().parse(Config.SELL_WINDOW,
                Template.of("trader", villagerType.getDisplayName()),
                Template.of("percentage", "100")), villagerType); //TODO get percentage from player somehow
        for (ItemStack itemStack : villagerType.getSelling()) {
            double price = Calculation.price(itemStack);
            addItem(itemStack,
                    getPriceItem(price),
                    null,
                    player -> player.sendMessage(MiniMessage.get().parse("Hi! you sold: " + itemStack.getAmount() + " " + itemStack.getType().name() + " for " + price + "."))
            );
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
        itemMeta.displayName(miniMessage.parse("<red>" + price * -1 + "</red>")); //TODO configurable
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
