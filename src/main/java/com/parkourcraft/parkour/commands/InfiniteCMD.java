package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.infinite.InfinitePKDB;
import com.parkourcraft.parkour.data.infinite.InfinitePKLBPosition;
import com.parkourcraft.parkour.data.infinite.InfinitePKManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class InfiniteCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        InfinitePKManager infinitePKManager = Parkour.getInfinitePKManager();

        if (a.length >= 1 && a[0].equalsIgnoreCase("score")) {
            // other target
            if (a.length == 2) {

                if (InfinitePKDB.hasScore(a[1])) {

                    int score = InfinitePKDB.getScoreFromName(a[1]);
                    sender.sendMessage(Utils.translate("&c" + a[1] + " &7has a score of &6" + Utils.formatNumber(score)));
                } else {
                    sender.sendMessage(Utils.translate("&c" + a[1] + " &7has not played &6Infinite Parkour &7before (score of 0)"));
                }
            // self
            } else if (a.length == 1) {

                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (playerStats != null && playerStats.getInfinitePKScore() > 0)
                    sender.sendMessage(Utils.translate("&7You have a score of &6" + Utils.formatNumber(playerStats.getInfinitePKScore())));
                else
                    sender.sendMessage(Utils.translate("&7You have yet to play &6Infinite Parkour &7(score of 0)"));
            // leaderboard
            } else if (a.length == 3 && a[1].equalsIgnoreCase("lb")) {

                if (Utils.isInteger(a[2])) {
                    int position = Integer.parseInt(a[2]);
                    InfinitePKLBPosition lbPosition = (InfinitePKLBPosition) infinitePKManager.getLeaderboard().toArray()[position];

                    if (lbPosition != null) {
                        player.sendMessage(Utils.translate(
                                "&c" + lbPosition.getName() + " &7is at &6Spot " + position + " &7with a score of &6" + lbPosition.getScore())
                        );
                    } else {
                        player.sendMessage(Utils.translate("&7Something went wrong... invalid position &6" + position));
                    }
                } else {
                    player.sendMessage(Utils.translate("&c" + a[2] + " &7is not a position on the leaderboard! (1-10)"));
                }
            }
        // admin command for removing leaderboard position
        } else if (player.hasPermission("pc-parkour.admin") && (a.length == 3 && a[0].equalsIgnoreCase("setscore"))) {

            if (Utils.isInteger(a[2])) {
                if (InfinitePKDB.hasScore(a[1])) {
                    int score = Integer.parseInt(a[2]);

                    infinitePKManager.updateScore(a[1], score);
                    // can run in async
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            infinitePKManager.loadLeaderboard();
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin()); // load lb

                    player.sendMessage(Utils.translate("&7You have set &c" + a[1] + "&7's score to &6" + score));
                } else {
                    player.sendMessage(Utils.translate("&c" + a[1] + " &7has not joined the server yet"));
                }
            } else {
                player.sendMessage(Utils.translate("&c" + a[2] + " &7is not an integer"));
            }
        } else if (a.length == 1 && a[0].equalsIgnoreCase("start")) {

            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            if (!playerStats.isInInfinitePK()) {
                if (playerStats.getPlayerToSpectate() == null) {
                    if (!playerStats.isEventParticipant()) {
                        if (!playerStats.inRace()) {
                            if (playerStats.getPracticeLocation() == null) {
                                infinitePKManager.startPK(player);
                            } else {
                                player.sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot do this while you are in a race"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot do this while you are in an event"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot do this while you are spectating"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou are already in infinite parkour"));
            }
        } else if (a.length == 0 || (a.length == 1 && a[0].equalsIgnoreCase("help"))) {
            sendHelp(player);
        } else {
            sendHelp(player);
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&5/infinite score [IGN]  &7Tells you the score of yourself/someone else"));
        player.sendMessage(Utils.translate("&5/infinite score lb <position>  &7Tells you the score of someone in <position> on the leaderboard"));

        if (player.hasPermission("pc-parkour.admin")) {
            player.sendMessage(Utils.translate("&5/infinite setscore <IGN> <score>  &7Set the score of someone"));
        }

        player.sendMessage(Utils.translate("&5/infinite help  &7Shows you this display"));
    }
}
