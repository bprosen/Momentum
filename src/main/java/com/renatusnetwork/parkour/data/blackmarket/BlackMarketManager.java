package com.renatusnetwork.parkour.data.blackmarket;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class BlackMarketManager
{
    private BlackMarketEvent running;
    private ArrayList<BlackMarketItem> items;

    public BlackMarketManager()
    {
        items = new ArrayList<>();
        running = null;
    }

    public void start()
    {
        running = new BlackMarketEvent(getRandomItem());

        // TODO: announcement here and timer to let people join (5 mins)?
    }

    public void end()
    {

        // TODO: announcement here and reward winner, ending scene
        running = null;
    }

    public void increaseBid(String playerName, long bid)
    {
        if (isRunning())
            running.increaseBid(playerName, bid);

        // TODO: announcement here
    }

    public void playerJoined(String playerName)
    {
        if (isRunning())
            running.addPlayer(playerName);
    }

    public void playerLeft(String playerName)
    {
        if (isRunning())
            running.removePlayer(playerName);
    }

    public boolean isRunning()
    {
        return running != null;
    }

    public BlackMarketItem getRandomItem()
    {
        return items.get(ThreadLocalRandom.current().nextInt(items.size()));
    }

}
