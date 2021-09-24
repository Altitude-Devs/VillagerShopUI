package com.alttd.GUI.windows;

import com.alttd.GUI.GUIInventory;
import com.alttd.config.Config;
import com.alttd.objects.VillagerType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OpenGUI extends GUIInventory {

    private static final ItemStack BUY = new ItemStack(Material.GOLD_INGOT);
    private static final ItemStack SELL = new ItemStack(Material.BUCKET);

    static {
        MiniMessage miniMessage = MiniMessage.get();
        ItemMeta itemMeta;
        {
            itemMeta = BUY.getItemMeta();
            itemMeta.displayName(miniMessage.parse("<green>Buy</green>"));
            BUY.setItemMeta(itemMeta);
        }
        {
            itemMeta = SELL.getItemMeta();
            itemMeta.displayName(miniMessage.parse("<green>Sell</green>"));
            SELL.setItemMeta(itemMeta);
        }
    }

    public OpenGUI(VillagerType villagerType) {
        super(InventoryType.HOPPER, MiniMessage.get().parse(Config.INITIAL_VILLAGER_WINDOW,
                Template.of("trader", villagerType.getDisplayName()),
                Template.of("percentage", "100"))); //TODO get percentage from player somehow
        setItem(1, BUY, player -> new BuyGUI(villagerType).open(player));
        setItem(3, SELL, player -> new SellGUI(villagerType).open(player));
    }
}
