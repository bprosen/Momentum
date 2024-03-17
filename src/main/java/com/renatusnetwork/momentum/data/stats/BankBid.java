package com.renatusnetwork.momentum.data.stats;

public class BankBid
{
    private int bid;
    private long lastBidDateMillis;

    public BankBid(int bid, long lastBidDateMillis)
    {
        this.bid = bid;
        this.lastBidDateMillis = lastBidDateMillis;
    }

    public void setBid(int bid)
    {
        this.bid = bid;
    }

    public int getBid()
    {
        return bid;
    }

    public long getLastBidDateMillis()
    {
        return lastBidDateMillis;
    }

    public void setLastBidDateMillis(long lastBidDateMillis)
    {
        this.lastBidDateMillis = lastBidDateMillis;
    }
}
