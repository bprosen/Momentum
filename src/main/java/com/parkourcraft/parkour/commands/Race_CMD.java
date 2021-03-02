package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.Checkpoint_DB;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.StatsManager;
import com.parkourcraft.parkour.gameplay.SpectatorHandler;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

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
            // send/accept race request
            sendRequest(player, a[0], false, 0.0);
        } else if (a.length == 2) {
            // send race request with bet
            if (Utils.isDouble(a[1])) {
                double betAmount = Double.parseDouble(a[1]);
                sendRequest(player, a[0], true, betAmount);
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

        if (Parkour.getStatsManager().get(player).getPlayerToSpectate() != null) {
            player.sendMessage(Utils.translate("&cYou cannot do this while in spectator"));
            return;
        }

        if (confirmMap.containsKey(victim.getName() + ":" + player.getName())) {
            confirmMap.get(victim.getName() + ":" + player.getName()).cancel();
            confirmMap.remove(victim.getName() + ":" + player.getName());

            // start race
            Parkour.getRaceManager().startRace(player, victim, bet, betAmount);
        } else {
            // otherwise, put them in and ask them to confirm within 5 seconds
            if (bet) {
                victim.sendMessage(Utils.translate("&4" + player.getName() + " &7has sent you a race request with bet amount &4$" + betAmount));
                player.sendMessage(Utils.translate("&7You sent &4" + victim.getName() + " &7a race request with bet amount &4$" + betAmount));
            } else {
                victim.sendMessage(Utils.translate("&4" + player.getName() + " &7has sent you a race request"));
                player.sendMessage(Utils.translate("&7You sent &4" + victim.getName() + " &7a race request"));
            }

            victim.sendMessage(Utils.translate("&7Type &c/race " + player.getName() + " &7within &c15 seconds &7to accept"));

            confirmMap.put(player.getName() + ":" + victim.getName(), new BukkitRunnable() {
                public void run() {
                if (confirmMap.containsKey(player.getName() + ":" + victim.getName())) {
                    confirmMap.remove(player.getName() + ":" + victim.getName());
                    player.sendMessage(Utils.translate("&4" + victim.getName() + " &7did not accept your race request in time"));
                }
                }
            }.runTaskLater(Parkour.getPlugin(), 20 * 15));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&4&lRace Command Help"));
        player.sendMessage(Utils.translate("&c/race help &4- &7Displays this page"));
        player.sendMessage(Utils.translate("&c/race (IGN) &4- &7Send/accept race request without a bet"));
        player.sendMessage(Utils.translate("&c/race (IGN) (Bet) &4- &7Send/accept race request with a bet"));
    }
}
