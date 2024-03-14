package com.renatusnetwork.momentum.data.clans;

public class ClanMember
{
    private String uuid;
    private String name;

    public ClanMember(String uuid, String name)
    {
        this.uuid = uuid;
        this.name = name;
    }

    public String getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public boolean equals(ClanMember other) { return this.uuid.equals(other.getUUID()); }

}
