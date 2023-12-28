package com.renatusnetwork.parkour.data.perks;

import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class Perk
{
    private String name;
    private String title;
    private HashMap<PerksArmorType, ItemStack> armorItems;
    private Material infiniteBlock;

    private List<Level> requiredLevels;

    private String requiredPermission;
    private int price;

    public Perk(String name)
    {
        this.armorItems = new HashMap<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public boolean isInfiniteBlock() { return infiniteBlock != null; }

    public Material getInfiniteBlock() { return infiniteBlock; }

    public String getFormattedTitle() {
        return Utils.translate(title);
    }

    public HashMap<PerksArmorType, ItemStack> getItems() { return armorItems; }

    public List<Level> getRequiredLevels() {
        return requiredLevels;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public boolean hasRequiredPermission(PlayerStats playerStats)
    {
        return requiredPermission == null || playerStats.getPlayer().hasPermission(requiredPermission);
    }

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
