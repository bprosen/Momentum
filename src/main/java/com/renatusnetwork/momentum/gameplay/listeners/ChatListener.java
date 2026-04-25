<<<<<<<< HEAD:src/main/java/com/renatusnetwork/momentum/gameplay/listeners/ChatListener.java
package com.renatusnetwork.momentum.gameplay.listeners;
========
package com.renatusnetwork.momentum.gameplay;
>>>>>>>> master:src/main/java/com/renatusnetwork/momentum/gameplay/ChatListener.java

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.clans.Clan;
import com.renatusnetwork.momentum.data.clans.ClansManager;
<<<<<<<< HEAD:src/main/java/com/renatusnetwork/momentum/gameplay/listeners/ChatListener.java
import com.renatusnetwork.momentum.data.squads.SquadsManager;
========
>>>>>>>> master:src/main/java/com/renatusnetwork/momentum/gameplay/ChatListener.java
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

public class ChatListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage();
        ClansManager clansManager = Momentum.getClansManager();
<<<<<<<< HEAD:src/main/java/com/renatusnetwork/momentum/gameplay/listeners/ChatListener.java
        SquadsManager squadsManager = Momentum.getSquadsManager();
========
>>>>>>>> master:src/main/java/com/renatusnetwork/momentum/gameplay/ChatListener.java
        PlayerStats playerStats = Momentum.getStatsManager().get(player);

        if (playerStats != null) {
            event.setCancelled(true);

            if (squadsManager.isInSquadChat(playerStats) && playerStats.inSquad()) {
                event.getRecipients().clear();

                // SquadManager#sendMessage handles chatspy messages
                squadsManager.sendMessage(playerStats, "&9SC &3" + playerStats.getDisplayName() + " &b" + msg, true);
                Momentum.getPluginLogger().info("Squad Chat: " + playerStats.getDisplayName() + " - " + ChatColor.stripColor(msg));
            } else if (playerStats.getClan() != null && clansManager.isInClanChat(player.getName())) { // iterate through the smaller list first
                event.getRecipients().clear();

                // cancel event, clear recipients, and send to clan members
                clansManager.sendMessageToMembers(playerStats.getClan(), "&6CC &e" + player.getDisplayName() + " &7" + msg, null);

                // log to console!
                Momentum.getPluginLogger().info("Clan Chat: " + playerStats.getClan().getTag() + " " + player.getName() + " " + ChatColor.stripColor(msg));

                // now send to spying players
                for (String spyPlayers : clansManager.getChatSpyMap()) {
                    Player spyPlayer = Bukkit.getPlayer(spyPlayers);

                    // null check and make sure they will not be sent msgs from their own clan
<<<<<<<< HEAD:src/main/java/com/renatusnetwork/momentum/gameplay/listeners/ChatListener.java
                    if (spyPlayer != null) {
========
                    if (spyPlayer != null)
                    {
>>>>>>>> master:src/main/java/com/renatusnetwork/momentum/gameplay/ChatListener.java
                        PlayerStats spyStats = Momentum.getStatsManager().get(spyPlayer);
                        Clan spyClan = spyStats.getClan();

                        if (spyClan == null || !spyClan.equals(playerStats.getClan())) {
                            spyPlayer.sendMessage(Utils.translate("&6CS " + playerStats.getClan().getTag() + " &e" +
                                                                  player.getDisplayName() + " &7" + msg));
                        }
                    }
                }
            } else {

                String formatted = String.format(event.getFormat(), player.getDisplayName(), msg);

                // create components
                TextComponent mainComponent = new TextComponent(TextComponent.fromLegacyText(formatted));
                mainComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Momentum.getStatsManager().createChatHover(playerStats))));
                mainComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile " + event.getPlayer().getName()));

                // doing it this way instead of using Bukkit#broadcast ensures any previous filtering (e.g. ignored players) is preserved
                event.getRecipients().forEach(p -> p.spigot().sendMessage(mainComponent));
                Bukkit.getServer().getConsoleSender().sendMessage(mainComponent.toLegacyText());

                // add to gg if they say it
<<<<<<<< HEAD:src/main/java/com/renatusnetwork/momentum/gameplay/listeners/ChatListener.java
                if (ChatColor.stripColor(msg).equalsIgnoreCase("gg")) {
                    Momentum.getStatsManager().addGG(playerStats);
                }
========
                if (ChatColor.stripColor(msg).equalsIgnoreCase("gg"))
                    Momentum.getStatsManager().addGG(playerStats);
>>>>>>>> master:src/main/java/com/renatusnetwork/momentum/gameplay/ChatListener.java
            }
        }
    }
}
