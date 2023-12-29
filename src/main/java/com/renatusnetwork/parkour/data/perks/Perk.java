package com.renatusnetwork.parkour.data.perks;

import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Perk
{
    private String name;
    private String title;
    private String requiredPermission;
    private int price;
    private Material infiniteBlock;

    private HashMap<PerksArmorType, ItemStack> armorItems;
    private List<Level> requiredLevels;

    public Perk(String name)
    {
        this.requiredLevels = new ArrayList<>();
        this.armorItems = new HashMap<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) { this.title = title; }

    public boolean isInfiniteBlock() { return infiniteBlock != null; }

    public void setInfiniteBlock(Material infiniteBlock) { this.infiniteBlock = infiniteBlock; }

    public Material getInfiniteBlock() { return infiniteBlock; }

    public String getFormattedTitle() {
        return Utils.translate(title);
    }

    public HashMap<PerksArmorType, ItemStack> getItems() { return armorItems; }

    public boolean alreadyRequiresLevel(Level level) { return requiredLevels.contains(level); }

    public void setRequiredLevels(List<Level> requiredLevels) { this.requiredLevels = requiredLevels; }

    public void setArmorItems(HashMap<PerksArmorType, ItemStack> armorItems) { this.armorItems = armorItems; }

    public List<Level> getRequiredLevels() {
        return requiredLevels;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }

    public boolean hasRequiredPermission(PlayerStats playerStats)
    {
        return requiredPermission == null || playerStats.getPlayer().hasPermission(requiredPermission);
    }

    public void setPrice(int price) { this.price = price; }

    public int getPrice() {
        return price;
    }

    public boolean hasAccessTo(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();

        // if they are opped or have the perk, no need to check other requirements
        if (player.isOp() || playerStats.hasPerk(name))
            return true;

        // if it needs a level completion, check all the levels to see if they are missing one, otherwise keep checking
        for (Level level : requiredLevels)
            if (playerStats.getLevelCompletionsCount(level.getName()) < 1)
                return false;

        // if it can be purchased, immediately return false since we know they don't have access to this perk
        if (price > 0)
            return false;

        // simple check of if they have permission, return result
        return !hasRequiredPermission(playerStats);
    }
}
