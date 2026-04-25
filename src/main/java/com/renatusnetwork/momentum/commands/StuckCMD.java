package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StuckCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerStats playerStats = Momentum.getStatsManager().get(player);

            if (playerStats.inLevel()) {
                Level level = playerStats.getLevel();

                if (level.hasStuckURL()) {
                    BaseComponent[] test = TextComponent.fromLegacyText(Utils.translate(
                            "\n&8> &cClick &nhere&c to open the guide for " + level.getTitle() + "&8 <\n"
                    ));

                    TextComponent component = new TextComponent(test);
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Utils.translate("&cClick to open the guide"))));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, level.getStuckURL()));

                    // send component
                    player.spigot().sendMessage(component);
                } else {
                    sender.sendMessage(Utils.translate("&cThis level does not have an available guide"));
                }
            } else {
                sender.sendMessage(Utils.translate("&cYou are not in a level to be stuck in"));
            }
        }
        return false;
    }
}
