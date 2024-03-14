package com.renatusnetwork.momentum.data.races.gamemode;
import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
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
        if (validateOther(requested, sender) && validateSelf(sender))
        {
            boolean randomLevel = level == null;
            boolean hasBet = bet > 0;

            if (randomLevel)
            {
                List<Level> raceLevels = Momentum.getLevelManager().getRaceLevels();
                // picks random map
                level = raceLevels.get(ThreadLocalRandom.current().nextInt(raceLevels.size()));
            }

            if (level != null)
            {
                String senderString = "&7You sent &4" + requested.getDisplayName() + "&7 a race request on &c" + level.getTitle();
                String requestedString = "&4" + sender.getDisplayName() + "&7 sent you a race request on &c" + level.getTitle();

                if (hasBet)
                {
                    String formatted = Utils.formatNumber(bet);

                    senderString += "&7 with a bet for &6" + formatted + " &eCoins";
                    requestedString += "&7 with a bet for &6" + formatted + " &eCoins";
                }

                TextComponent opponentComponent = new TextComponent(TextComponent.fromLegacyText(Utils.translate(requestedString + "&7. &c&nClick here to accept")));
                opponentComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Utils.translate("&aClick to accept!"))));
                opponentComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/race accept " + sender.getName()));

                // send made messages
                sender.sendMessage(Utils.translate(senderString));
                requested.getPlayer().spigot().sendMessage(opponentComponent); // send clickable
                Momentum.getRaceManager().addRequest(this);
            }
            else
                sender.sendMessage(Utils.translate("&cSomething went wrong finding a level"));
        }
        else
            Momentum.getRaceManager().removeRequest(this);
    }

    public void accept() {
        PlayerStats requested = getRequested();
        PlayerStats sender = getSender();

        // need to validate both the sender and send to requested, and also requested to themselves
        if (validateOther(sender, requested) && validateSelf(requested))
        {
            new Race(sender, requested, level, bet).start();
            Momentum.getRaceManager().removeRequest(this);
        }
    }

    private boolean validateSelf(PlayerStats playerStats)
    {
        // send to self
        String validateReasonResult = validateResult(playerStats, null);

        if (validateReasonResult != null)
            playerStats.sendMessage(Utils.translate(validateReasonResult));

        return validateReasonResult == null;
    }

    private boolean validateOther(PlayerStats validateStats, PlayerStats toSendTo)
    {
        String validateReasonResult = validateResult(validateStats, toSendTo); // get other reason

        // send to other instead
        if (validateReasonResult != null)
            toSendTo.sendMessage(Utils.translate(validateReasonResult));

        return validateReasonResult == null;
    }

    private String validateResult(PlayerStats validateStats, PlayerStats sendToStats)
    {
        boolean selfCheck = sendToStats == null;

        if (validateStats.isSpectating())
            return selfCheck ? "&cYou cannot race while in spectator" : "&c" + validateStats.getDisplayName() + "&c cannot race while in spectator";
        else if (validateStats.inPracticeMode())
            return selfCheck ? "&cYou cannot race while in practice" : "&c" +  validateStats.getDisplayName() + "&c cannot race while in practice";
        else if (validateStats.inRace())
            return selfCheck ? "&cYou cannot race while in a race" : "&c" + validateStats.getDisplayName() + "&c cannot race while in a race";
        else if (validateStats.isInInfinite())
            return selfCheck ? "&cYou cannot race while in infinite" : "&c" + validateStats.getDisplayName() + "&c cannot race while in infinite";
        else if (validateStats.isInBlackMarket())
            return selfCheck ? "&cYou cannot race while in black market" : "&c" + validateStats.getDisplayName() + "&c is busy right now...";
        else if (validateStats.isEventParticipant())
            return selfCheck ? "&cYou cannot race while in an event" : "&c" + validateStats.getDisplayName() + "&c cannot race while in an event";
        else if (validateStats.getCoins() < bet)
            return selfCheck ?
                    "&cYou do not have enough coins for this bet, need &6" + Utils.formatNumber((bet - validateStats.getCoins())) + "&c more &eCoins" :
                    "&c" + validateStats.getDisplayName() + "&c does not have enough coins for this bet, need &6" + Utils.formatNumber((bet - validateStats.getCoins())) + "&c more &eCoins";
        else if (validateStats.getPlayer().getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world))
            return selfCheck ? "&cYou cannot race while in a plot" : "&c" + validateStats.getDisplayName() + "&c cannot race while in a plot";
        else if (validateStats.inLevel() && validateStats.getLevel().isElytra())
        {
            if (validateStats.getPlayer().isOnGround())
                Momentum.getStatsManager().toggleOffElytra(validateStats);
            else
                return selfCheck ? "&cCannot race when you are in the air in an elytra level" : "&c" + validateStats.getDisplayName() + "&c cannot race while you are in the air in an elytra level";
        }
        return null;
    }

    public boolean equals(PlayerStats sender, PlayerStats requester)
    {
        return (sender.equals(getSender()) && requester.equals(getRequested())) || (sender.equals(getRequested()) && requester.equals(getSender()));
    }
}
