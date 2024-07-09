package com.alttd.objects;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VillagerType {

    String getName();

    String getDisplayName();

    Set<ItemStack> getBuying();

    Set<ItemStack> getSelling();

    Villager.Profession getProfession();

    String getPermission();

    Optional<Component> getRandomMessage();

    void addMessage(String message);

    void setMessages(List<String> messages);
}
