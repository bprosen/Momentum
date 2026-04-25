package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.jackpot.Jackpot;
import com.renatusnetwork.momentum.data.jackpot.JackpotManager;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.menus.MenuItemAction;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class JackpotCMD implements CommandExecutor {
    private HashMap<String, BukkitTask> confirmMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            JackpotManager jackpotManager = Momentum.getJackpotManager();

            if (a.length == 1 && a[0].equalsIgnoreCase("play")) {
                if (jackpotManager.isJackpotRunning()) {
                    Jackpot jackpot = jackpotManager.getJackpot();

                    if (!jackpot.hasCompleted(player.getName()))
                    // tp to level
                    {
                        MenuItemAction.performLevelTeleport(Momentum.getStatsManager().get(player), jackpot.getLevel());
                    } else {
                        player.sendMessage(Utils.translate("&cYou have already completed the jackpot"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cThe jackpot is not running currently"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("force") && player.hasPermission("momentum.jackpot.force")) {
                // USED FOR BLACK MARKET!
                if (!jackpotManager.isJackpotRunning()) {
                    if (!confirmMap.containsKey(player.getName())) {
                        // otherwise, put them in and ask them to confirm within 5 seconds
                        player.sendMessage("");
                        player.sendMessage(Utils.translate("&cAre you sure you want to use &6&lJackpot &cforce? This will remove your one time use!"));
                        player.sendMessage(Utils.translate("&cType &4/jackpot force &cagain to confirm"));
                        player.sendMessage("");

                        confirmMap.put(player.getName(), new BukkitRunnable() {
                            public void run() {
                                if (confirmMap.containsKey(player.getName())) {
                                    confirmMap.remove(player.getName());
                                    player.sendMessage(Utils.translate("&cYou did not confirm in time"));
                                }
                            }
                        }.runTaskLater(Momentum.getPlugin(), 20 * 10));
                    } else {
                        confirmMap.get(player.getName()).cancel();
                        jackpotManager.startJackpot();
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Momentum.getSettingsManager().jackpot_force_remove_permission_cmd.replace("%player%", player.getName()));
                        confirmMap.remove(player.getName());
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot do this when a jackpot is already running"));
                }
            } else if (player.hasPermission("momentum.admin")) {
                if (a.length == 1 && a[0].equalsIgnoreCase("start")) {
                    if (!jackpotManager.isJackpotRunning()) {
                        jackpotManager.startJackpot();
                    } else {
                        player.sendMessage(Utils.translate("&cThere is already a jackpot running, do &4/jackpot end &cto stop it!"));
                    }
                } else if (a.length == 1 && a[0].equalsIgnoreCase("end")) {
                    if (jackpotManager.isJackpotRunning()) {
                        jackpotManager.endJackpot();
                    } else {
                        player.sendMessage(Utils.translate("&cThere is no jackpot running, do &4/jackpot start &cto start one!"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("choose")) {
                    String levelName = a[1];
                    String bonus = a[2];

                    Level level = Momentum.getLevelManager().get(levelName);

                    if (level != null) {
                        if (Utils.isInteger(bonus)) {
                            if (!level.isFeaturedLevel() && !level.isAscendance() && !level.isRankUpLevel()) {
                                int bonusAmount = Integer.parseInt(bonus);

                                if (!jackpotManager.isJackpotRunning()) {
                                    jackpotManager.chooseJackpot(level, bonusAmount);
                                } else {
                                    player.sendMessage(Utils.translate("&cThere is already a jackpot running, do &4/jackpot end &cto stop it!"));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&cThe level cannot be the featured, in ascendance or a rankup..."));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&4" + bonus + " &cis not a valid integer"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level"));
                    }
                } else {
                    sendHelp(player);
                }
            } else {
                sendHelp(player);
            }
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&2/jackpot play  &7Enters the currently running jackpot level"));

        int low = Momentum.getSettingsManager().jackpot_bonus_low_random_bound;
        int high = Momentum.getSettingsManager().jackpot_bonus_high_random_bound;
        if (player.hasPermission("momentum.admin")) {
            player.sendMessage(Utils.translate("&2/jackpot start  &7Automatically chooses and starts a jackpot based off configured amount (" + low + "x to " + high + "x)"));
            player.sendMessage(Utils.translate("&2/jackpot end  &7Ends any currently running jackpot"));
            player.sendMessage(Utils.translate("&2/jackpot choose <level name> <bonus amount>  &7Chooses a jackpot from level and bonus amount"));
        }

        if (player.hasPermission("momentum.jackpot.force")) {
            player.sendMessage(Utils.translate("&2/jackpot force  &7Allows you to force a jackpot &c&lONE TIME"));
        }

        player.sendMessage(Utils.translate("&2/jackpot help  &7Displays this screen"));
    }
}
