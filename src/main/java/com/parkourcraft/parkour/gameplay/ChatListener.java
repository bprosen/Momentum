package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.clans.ClanMember;
import com.parkourcraft.parkour.data.clans.ClansManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler (ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        ClansManager clansManager = Parkour.getClansManager();

        // this means they are in a clan and in clan chat!
        if (playerStats != null && playerStats.getClan() != null && clansManager.isInClanChat(player.getName())) {

            // cancel event, clear recipients, and send to clan members
            event.setCancelled(true);
            event.getRecipients().clear();
            clansManager.sendMessageToMembers(playerStats.getClan(), "&6CC &e" + player.getDisplayName() + " &7" + msg, null);
            // log to console!
            Parkour.getPluginLogger().info("Clan Chat: " + playerStats.getClan().getTag() + " " + player.getName() + " " + ChatColor.stripColor(msg));

            // now send to spying players
            for (String spyPlayers : clansManager.getChatSpyMap()) {
                Player spyPlayer = Bukkit.getPlayer(spyPlayers);

                // null check and make sure they will not be sent msgs from their own clan
                if (spyPlayer != null) {

                    boolean sendMsg = true;

                    // loop through members, to check if they are in clan
                    for (ClanMember clanMember : playerStats.getClan().getMembers())
                        // if they arent, send message to online spying staff
                        if (clanMember.getPlayerName().equalsIgnoreCase(spyPlayer.getName())) {
                            sendMsg = false;
                            break;
                        }
                    // send msg if not found!
                    if (sendMsg)
                        spyPlayer.sendMessage(Utils.translate("&6CS " + playerStats.getClan().getTag() + " &e" +
                                player.getDisplayName() + " &7" + msg));
                }
            }
        }
    }
}
