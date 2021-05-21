package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.levels.LevelManager;
import com.parkourcraft.parkour.data.levels.RatingDB;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RateCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (a.length == 2) {
            String levelName = a[0].toLowerCase();
            LevelManager levelManager = Parkour.getLevelManager();
            Level level = levelManager.get(levelName);

            if (level != null) {
                if (Utils.isInteger(a[1])) {

                    int rating = Integer.parseInt(a[1]);
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);

                    if (playerStats.getLevelCompletionsCount(levelName) > 0) {
                        if (!RatingDB.hasRatedLevel(player.getUniqueId().toString(), level.getID())) {

                            level.addRatingAndCalc(rating);
                            RatingDB.addRating(player, level, rating);
                            player.sendMessage(Utils.translate("&7You rated &c" + level.getFormattedTitle() + " &7a &6" + rating + "&7! Thank you for rating!"));
                        } else {
                            player.sendMessage(Utils.translate("&cYou have already rated this level"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou have not yet completed &c" + level.getFormattedTitle()
                                + " &cyet to be able to rate it!"));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not a valid integer"));
                }
            } else {
                sender.sendMessage(Utils.translate("&cNo level named &c" + levelName + " &cexists"));
            }
        }
        return false;
    }
}
