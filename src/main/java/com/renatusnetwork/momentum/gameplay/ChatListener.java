package com.renatusnetwork.momentum.gameplay;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.clans.Clan;
import com.renatusnetwork.momentum.data.clans.ClansManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener
{
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        String msg = event.getMessage();
        ClansManager clansManager = Momentum.getClansManager();
        PlayerStats playerStats = Momentum.getStatsManager().get(player);

        if (playerStats != null)
        {
            event.setCancelled(true);

            // iterate through the smaller list first
            if (playerStats.getClan() != null && clansManager.isInClanChat(player.getName()))
            {
                event.getRecipients().clear();

                // cancel event, clear recipients, and send to clan members
                clansManager.sendMessageToMembers(playerStats.getClan(), "&6CC &e" + player.getDisplayName() + " &7" + msg, null);

                // log to console!
                Momentum.getPluginLogger().info("Clan Chat: " + playerStats.getClan().getTag() + " " + player.getName() + " " + ChatColor.stripColor(msg));

                // now send to spying players
                for (String spyPlayers : clansManager.getChatSpyMap())
                {
                    Player spyPlayer = Bukkit.getPlayer(spyPlayers);

                    // null check and make sure they will not be sent msgs from their own clan
                    if (spyPlayer != null)
                    {
                        PlayerStats spyStats = Momentum.getStatsManager().get(spyPlayer);
                        Clan spyClan = spyStats.getClan();

                        if (spyClan == null || !spyClan.equals(playerStats.getClan()))
                            spyPlayer.sendMessage(Utils.translate("&6CS " + playerStats.getClan().getTag() + " &e" +
                                    player.getDisplayName() + " &7" + msg));
                    }
                }
            }
            else
            {

                String formatted = String.format(event.getFormat(), player.getDisplayName(), msg);

                // create components
                TextComponent mainComponent = new TextComponent(TextComponent.fromLegacyText(formatted));
                mainComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Momentum.getStatsManager().createChatHover(playerStats))));
                mainComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile " + event.getPlayer().getName()));

                // doing it this way instead of using Bukkit#broadcast ensures any previous filtering (e.g. ignored players) is preserved
                event.getRecipients().forEach(p -> p.spigot().sendMessage(mainComponent));
                Bukkit.getServer().getConsoleSender().sendMessage(mainComponent.toLegacyText());

                // add to gg if they say it
                if (ChatColor.stripColor(msg).equalsIgnoreCase("gg"))
                    Momentum.getStatsManager().addGG(playerStats);
            }
        }
    }
}
