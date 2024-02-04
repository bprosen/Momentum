package com.renatusnetwork.parkour.data.stats;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

public class PlayerHiderManager
{
    private Set<Player> hiddenPlayers;

    public PlayerHiderManager()
    {
        this.hiddenPlayers = new HashSet<>();
    }

    public void hidePlayer(Player player)
    {
        hiddenPlayers.add(player);

        for (Player online : Bukkit.getOnlinePlayers())
            if (!online.isOp())
                player.hidePlayer(Parkour.getPlugin(), online);
    }

    public void showPlayer(Player player)
    {
        hiddenPlayers.remove(player);

        for (Player online : Bukkit.getOnlinePlayers())
            if (!online.isOp())
                player.showPlayer(Parkour.getPlugin(), online);
    }

    public boolean containsPlayer(Player player)
    {
        return hiddenPlayers.contains(player);
    }

    public void hideHiddenPlayersFromJoined(Player playerJoined)
    {
        for (Player player : hiddenPlayers)
            player.hidePlayer(Parkour.getPlugin(), playerJoined);
    }

    public void toggleOff(Player player, int slot)
    {
        if (hiddenPlayers.contains(player))
        {
            showPlayer(player);

            ItemStack newItem = new ItemStack(Material.REDSTONE_TORCH_ON);
            ItemMeta meta = newItem.getItemMeta();
            meta.setDisplayName(Utils.translate("&2Players &7» &2Enabled"));
            newItem.setItemMeta(meta);
            player.getInventory().setItem(slot, newItem);

            player.sendMessage(Utils.translate("&aYou have turned on players"));
        }
    }

    public void toggleOn(Player player, int slot)
    {
        if (!hiddenPlayers.contains(player))
        {
            hidePlayer(player);

            ItemStack newItem = new ItemStack(Material.LEVER);
            ItemMeta meta = newItem.getItemMeta();
            meta.setDisplayName(Utils.translate("&2Players &7» &cDisabled"));
            newItem.setItemMeta(meta);
            player.getInventory().setItem(slot, newItem);

            player.sendMessage(Utils.translate("&cYou have turned off players"));
        }
    }
}
