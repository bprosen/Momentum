package com.parkourcraft.parkour.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class PlayerHider {

    private static HashSet<String> hiddenPlayers = new HashSet<>();

    public static void hidePlayer(Player player) {
        hiddenPlayers.add(player.getName());
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!player.isOp())
                player.hidePlayer(online);
        }
    }

    public static void showPlayer(Player player) {
        hiddenPlayers.remove(player.getName());
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!player.isOp())
                player.showPlayer(online);
        }
    }

    public static boolean containsPlayer(Player player) {
        if (hiddenPlayers.contains(player.getName()))
            return true;
        return false;
    }

    public static void hideHiddenPlayersFromJoined(Player playerJoined) {
        // deprecated because "names can change"
        for (String playerName : hiddenPlayers)
            Bukkit.getPlayer(playerName).hidePlayer(playerJoined);
    }

    public static void addHiddenPlayer(Player player) {
        hiddenPlayers.add(player.getName());
    }

    public static void removeHiddenPlayer(Player player) {
        hiddenPlayers.remove(player.getName());
    }
}