package com.renatusnetwork.parkour.data.clans;

import java.util.*;

public class Clan {

    private String tag;
    private String ownerUUID;
    private int clanLevel;
    private int clanXP;
    private long totalXP;
    private int maxLevel;
    private int maxMembers;
    private HashMap<String, ClanMember> members;
    private HashSet<String> invitedPlayerNames;

    public Clan(String tag, String ownerUUID)
    {
        this.tag = tag;
        this.ownerUUID = ownerUUID;
        this.members = new HashMap<>();
        this.invitedPlayerNames = new HashSet<>();
    }

    public Clan(String tag, String ownerUUID, int clanLevel, int clanXP, long totalXP, int maxLevel, int maxMembers)
    {
        this(tag, ownerUUID);

        this.clanLevel = clanLevel;
        this.clanXP = clanXP;
        this.totalXP = totalXP;
        this.maxMembers = maxMembers;
        this.maxLevel = maxLevel;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public int getLevel() { return clanLevel; }

    public int getXP() { return clanXP; }

    public void resetXP() {
        clanXP = 0;
    }

    public void setXP(int clanXP) { this.clanXP = clanXP; }

    public void setTotalXP(long totalXP) { this.totalXP = totalXP; }

    public long getTotalXP() { return totalXP; }

    public void addXP(int clanXP) { this.clanXP += clanXP; }

    public boolean isMaxLevel()
    {
        return clanLevel == maxLevel;
    }

    public int getMaxLevel()
    {
        return maxLevel;
    }

    public int getMaxMembers()
    {
        return maxMembers;
    }

    public void setMaxLevel(int maxLevel)
    {
        this.maxLevel = maxLevel;
    }

    public void setMaxMembers(int maxMembers)
    {
        this.maxMembers = maxMembers;
    }

    public void setLevel(int level) {
        clanLevel = level;
    }

    public void setClanOwnerUUID(String ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public String getOwnerUUID() {
        return this.ownerUUID;
    }

    public boolean isMember(String playerName)
    {
        return members.containsKey(playerName);
    }

    public ClanMember getMember(String playerName) {
        return members.get(playerName);
    }

    public ClanMember getOwner()
    {
        return getMember(ownerUUID);
    }

    public boolean isOwner(String playerName) { return getMember(ownerUUID).getName().equalsIgnoreCase(playerName); }

    public void setOwner(String ownerUUID)
    {
        this.ownerUUID = ownerUUID;
    }

    public void addMember(ClanMember clanMember)
    {
        members.put(clanMember.getUUID(), clanMember);
    }

    public void removeMember(String playerUUID)
    {
        members.remove(playerUUID);
    }

    public void addInvite(String playerName)
    {
        invitedPlayerNames.add(playerName);
    }

    public void removeInvite(String playerName) {
        invitedPlayerNames.remove(playerName);
    }

    public boolean isInvited(String playerName)
    {
        return invitedPlayerNames.contains(playerName);
    }

    public Collection<ClanMember> getMembers()
    {
        return members.values();
    }

    public int numMembers() { return members.size(); }

    public boolean equals(Clan clan)
    {
        return this.getOwner().getName().equalsIgnoreCase(clan.getOwner().getName());
    }
}