package com.renatusnetwork.parkour.data.races.gamemode;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.races.RaceManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RaceRequest
{
    private PlayerStats[] players;
    private Level level;
    private int bet;

    public RaceRequest(PlayerStats sender, PlayerStats requested, Level level, int bet)
    {
        this.players = new PlayerStats[]{sender, requested};
        this.level = level;
        this.bet = bet;
    }

    public PlayerStats getSender() { return players[0]; }
    private PlayerStats getRequested() { return players[1]; }

    public void send()
    {
        PlayerStats requested = getRequested();
        PlayerStats sender = getSender();

        // need to validate the sender to themselves and the sender to the requested
        if (validate(requested, sender) && validate(sender, sender))
        {
            boolean randomLevel = level == null;
            boolean hasBet = bet > 0;

            if (randomLevel)
            {
                List<Level> raceLevels = Parkour.getLevelManager().getRaceLevels();
                // picks random map
                level = raceLevels.get(ThreadLocalRandom.current().nextInt(raceLevels.size()));
            }

            if (level != null)
            {
                String senderString = "&7You sent &4" + requested.getDisplayName() + "&7 a race request on &c" + level.getTitle();
                String requestedString = "&4" + sender.getDisplayName() + "&7 sent you a race request on &c" + level.getTitle();

                if (hasBet)
                {
                    String formatted = Utils.formatDecimal(bet);

                    senderString += "&7 with a bet for &6" + formatted + " &eCoins";
                    requestedString += "&7 with a bet for &6" + formatted + " &eCoins";
                }

                TextComponent opponentComponent = new TextComponent(TextComponent.fromLegacyText(Utils.translate(requestedString + "&7. &c&nClick here to accept")));
                opponentComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Utils.translate("&aClick to accept!"))));
                opponentComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/race accept " + sender.getName()));

                // send made messages
                sender.sendMessage(Utils.translate(senderString));
                requested.getPlayer().spigot().sendMessage(opponentComponent); // send clickable
                Parkour.getRaceManager().addRequest(this);
            }
            else
                sender.sendMessage(Utils.translate("&cSomething went wrong finding a level"));
        }
    }

    public void accept()
    {
        PlayerStats requested = getRequested();
        PlayerStats sender = getSender();

        // need to validate both the sender and send to requested, and also requested to themselves
        if (validate(sender, requested) && validate(requested, requested))
        {
            new Race(sender, requested, level, bet).start();
            Parkour.getRaceManager().removeRequest(this);
        }
    }

    private boolean validate(PlayerStats validate, PlayerStats toSendTo)
    {
        RaceManager raceManager = Parkour.getRaceManager();
        boolean valid = false;

        if (!validate.isSpectating())
        {
            if (!validate.inPracticeMode())
            {
                // if accepting race while in race
                if (!validate.inRace())
                {
                    boolean passes = true;

                    if (validate.inLevel() && validate.getLevel().isElytra())
                    {
                        if (validate.getPlayer().isOnGround())
                            Parkour.getStatsManager().toggleOffElytra(validate);
                        else
                        {
                            toSendTo.sendMessage(Utils.translate("&cYou or them cannot race someone when you or them are not on the ground in an elytra level"));
                            passes = false;
                        }
                    }
                    // if elytra passes, continue
                    if (passes)
                    {
                        double balance = validate.getCoins();
                        if (balance >= this.bet)
                            valid = true;
                        else
                            toSendTo.sendMessage(Utils.translate("&cYou or them do not have enough money for this bet!"));
                    }
                }
                else
                    toSendTo.sendMessage(Utils.translate("&cYou or them cannot race someone else while in a race"));
            }
            else
                toSendTo.sendMessage(Utils.translate("&cYou or them cannot do this while in practice mode"));
        }
        else
            toSendTo.sendMessage(Utils.translate("&cYou or them cannot do this while in spectator"));

        raceManager.removeRequest(this);
        return valid;
    }

    public boolean equals(PlayerStats sender, PlayerStats requester)
    {
        return (sender.equals(getSender()) && requester.equals(getRequested())) || (sender.equals(getRequested()) && requester.equals(getSender()));
    }
}
