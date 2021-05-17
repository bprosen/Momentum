package com.parkourcraft.parkour.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PlayerHider {

    private static Set<String> hiddenPlayers = new HashSet<>();

    public static void hidePlayer(Player player, boolean spectator) {
        hiddenPlayers.add(player.getName());
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.isOp() && !spectator)
                player.hidePlayer(online);
            // if they are op, but the person thats hiding everyone is a spectator
            else if (spectator)
                online.hidePlayer(player);
        }
    }

    public static void showPlayer(Player player, boolean spectator) {
        hiddenPlayers.remove(player.getName());
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.isOp() && !spectator)
                player.showPlayer(online);
            else if (spectator)
                online.showPlayer(player);
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