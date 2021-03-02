package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class Race_CMD implements CommandExecutor {

    private HashMap<String, BukkitTask> confirmMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (a.length == 0) {
            sendHelp(player);
        } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
            sendHelp(player);
        } else if (a.length == 1) {
            // send race request
            sendRequest(player, a[0], false, 0.0);
        } else if (a.length == 2 && a[0].equalsIgnoreCase("accept")) {
            // accept race request
            acceptRequest(a[1], player);
        } else if (a.length == 2) {
            // send race request with bet
            if (Utils.isDouble(a[1])) {
                double betAmount = Double.parseDouble(a[1]);
                sendRequest(player, a[0], true, betAmount);
            } else {
                player.sendMessage(Utils.translate("&cThat is not a valid amount to bet!"));
            }
        } else {
            sendHelp(player);
        }

        return false;
    }

    private void sendRequest(Player player, String victimName, boolean bet, double betAmount) {

        Player victim = Bukkit.getPlayer(victimName);
        if (victim == null) {
            player.sendMessage(Utils.translate("&4" + victimName + " &cis offline"));
            return;
        }

        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        if (playerStats.getPlayerToSpectate() != null) {
            player.sendMessage(Utils.translate("&cYou cannot do this while in spectator"));
            return;
        }

        if (playerStats.getPracticeLocation() != null) {
            player.sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
            return;
        }

        // make sure they have enough money for the bet
        double victimBalance = Parkour.getEconomy().getBalance(victim);
        double senderBalance = Parkour.getEconomy().getBalance(player);

        if (senderBalance < betAmount) {
            player.sendMessage(Utils.translate("&7You do not have enough money for this bet!" +
                    " Your Balance &4$" + senderBalance));
            return;
        }

        if (victimBalance < betAmount) {
            player.sendMessage(Utils.translate("&c" + victimName + " &7does not have enough to do this bet" +
                    " - &cTheir Balance &4$" + victimBalance));
            return;
        }

        if (inConfirmMap(player, victim)) {
            player.sendMessage(Utils.translate("&cYou have already send one to them!"));
        } else {
            // otherwise, put them in and ask them to confirm within 5 seconds
            if (bet) {
                victim.sendMessage(Utils.translate("&4" + player.getName() + " &7has sent you a race request with bet amount &4$" + betAmount));
                player.sendMessage(Utils.translate("&7You sent &4" + victim.getName() + " &7a race request with bet amount &4$" + betAmount));
            } else {
                victim.sendMessage(Utils.translate("&4" + player.getName() + " &7has sent you a race request"));
                player.sendMessage(Utils.translate("&7You sent &4" + victim.getName() + " &7a race request"));
            }

            victim.sendMessage(Utils.translate("&7Type &c/race accept " + player.getName() + " &7within &c15 seconds &7to accept"));

            confirmMap.put(player.getName() + ":" + victim.getName() + ":" + bet + ":" + betAmount, new BukkitRunnable() {
                public void run() {
                if (inConfirmMap(player, victim)) {
                    removeFromConfirmMap(player, victim);
                    player.sendMessage(Utils.translate("&4" + victim.getName() + " &7did not accept your race request in time"));
                }
                }
            }.runTaskLater(Parkour.getPlugin(), 20 * 15));
        }
    }

    private void acceptRequest(String challenger, Player accepter) {

        Player victim = Bukkit.getPlayer(challenger);
        if (victim == null) {
            accepter.sendMessage(Utils.translate("&4" + challenger + " &cis offline"));
            return;
        }

        // request exists
        if (inConfirmMap(victim, accepter)) {

            String[] split = getStringFromConfirmMap(victim, accepter).split(":");
            boolean doingBet = Boolean.parseBoolean(split[2]);
            double betAmount = Double.parseDouble(split[3]);

            // conditions to cancel
            PlayerStats playerStats = Parkour.getStatsManager().get(accepter);
            if (playerStats.getPlayerToSpectate() != null) {
                accepter.sendMessage(Utils.translate("&cYou cannot do this while in spectator"));
                removeFromConfirmMap(victim, accepter);
                return;
            }

            if (playerStats.getPracticeLocation() != null) {
                accepter.sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
                removeFromConfirmMap(victim, accepter);
                return;
            }

            // make sure they still have enough money for the bet
            double accepterBalance = Parkour.getEconomy().getBalance(accepter);
            double senderBalance = Parkour.getEconomy().getBalance(victim);
            if (accepterBalance < betAmount) {
                accepter.sendMessage(Utils.translate("&7You do not have enough money for this bet!" +
                        " Your Balance &4$" + senderBalance));
                removeFromConfirmMap(victim, accepter);
                return;
            }

            if (senderBalance < betAmount) {
                accepter.sendMessage(Utils.translate("&c" + victim.getName() + " &7does not have enough to do this bet" +
                        " - &cTheir Balance &4$" + senderBalance));
                removeFromConfirmMap(victim, accepter);
                return;
            }

            // otherwise do race
            Parkour.getRaceManager().startRace(victim, accepter, doingBet, betAmount);
            removeFromConfirmMap(victim, accepter);
        } else {
            accepter.sendMessage(Utils.translate("&cYou do not have a request from &4" + challenger));
        }
    }

    private boolean inConfirmMap(Player player1, Player player2) {
        for (Map.Entry<String, BukkitTask> entry : confirmMap.entrySet()) {
            if (entry.getKey().startsWith(player1.getName() + ":" + player2.getName())) {
                return true;
            }
        }
        return false;
    }

    private void removeFromConfirmMap(Player player1, Player player2) {

        String deleteString = null;

        for (Map.Entry<String, BukkitTask> entry : confirmMap.entrySet()) {
            if (entry.getKey().startsWith(player1.getName() + ":" + player2.getName())) {
                deleteString = entry.getKey();
            }
        }

        if (deleteString != null) {
            confirmMap.get(deleteString).cancel();
            confirmMap.remove(deleteString);
        }
    }

    private String getStringFromConfirmMap(Player player1, Player player2) {

        for (Map.Entry<String, BukkitTask> entry : confirmMap.entrySet()) {
            if (entry.getKey().startsWith(player1.getName() + ":" + player2.getName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&4&lRace Command Help"));
        player.sendMessage(Utils.translate("&c/race help &4- &7Displays this page"));
        player.sendMessage(Utils.translate("&c/race (IGN) &4- &7Send race request without a bet"));
        player.sendMessage(Utils.translate("&c/race (IGN) (Bet) &4- &7Send race request with a bet"));
        player.sendMessage(Utils.translate("&c/race accept (IGN) &4- &7Accept pending race request"));
    }
}
