package com.renatusnetwork.parkour.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PlayerHider {

    private static Set<String> hiddenPlayers = new HashSet<>();

    public static void hidePlayer(Player player) {
        hiddenPlayers.add(player.getName());

        for (Player online : Bukkit.getOnlinePlayers())
            if (!online.isOp())
                player.hidePlayer(online);
    }

    public static void showPlayer(Player player) {
        hiddenPlayers.remove(player.getName());

        for (Player online : Bukkit.getOnlinePlayers())
            if (!online.isOp())
                player.showPlayer(online);
    }

    public static boolean containsPlayer(Player player) {
        return hiddenPlayers.contains(player.getName());
    }

    public static void hideHiddenPlayersFromJoined(Player playerJoined)
    {
        for (String playerName : hiddenPlayers)
            Bukkit.getPlayer(playerName).hidePlayer(playerJoined);
    }
}