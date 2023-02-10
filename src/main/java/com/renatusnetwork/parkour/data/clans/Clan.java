package com.renatusnetwork.parkour.data.clans;

import java.util.ArrayList;
import java.util.List;

public class Clan {

    private int ID;
    private String tag;
    private int ownerID;
    private int clanLevel;
    private int clanXP;
    private long totalGainedXP;

    private List<ClanMember> members = new ArrayList<>(); // Does not include the owner
    private List<String> invitedUUIDs = new ArrayList<>();

    public Clan(int clanID, String clanTag, int clanOwnerID, int clanLevel, int clanXP, long totalGainedXP) {
        this.ID = clanID;
        this.tag = clanTag;
        this.ownerID = clanOwnerID;
        this.clanLevel = clanLevel;
        this.clanXP = clanXP;
        this.totalGainedXP = totalGainedXP;
    }

    public void setID(int clanID) {
        this.ID = clanID;
    }

    public int getID() {
        return ID;
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

    public void setTotalGainedXP(long totalGainedXP) { this.totalGainedXP = totalGainedXP; }

    public long getTotalGainedXP() { return totalGainedXP; }

    public void addXP(long clanXP) { this.clanXP += clanXP; }

    public boolean isMaxLevel() {
        if (clanLevel >= ClansYAML.getMaxLevel())
            return true;
        return false;
    }

    public void setLevel(int level) {
        clanLevel = level;
    }

    public void setClanOwnerID(int clanOwnerID) {
        this.ownerID = clanOwnerID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public ClanMember getMemberFromUUID(String UUID) {
        for (ClanMember member : members)
            if (member.getUUID().equals(UUID))
                return member;

        return null;
    }

    public ClanMember getMemberFromName(String playerName) {
        for (ClanMember member : members)
            if (member.getPlayerName().equals(playerName))
                return member;

        return null;
    }

    public ClanMember getOwner() {
        for (ClanMember member : members)
            if (member.getPlayerID() == ownerID)
                return member;

        return null;
    }

    public void promoteOwner(String UUID) {
        ClanMember newOwner = getMemberFromUUID(UUID);

        if (newOwner != null)
            ownerID = newOwner.getPlayerID();
    }

    public void promoteOwnerFromName(String name) {
        ClanMember newOwner = getMemberFromName(name);

        if (newOwner != null)
            ownerID = newOwner.getPlayerID();
    }

    public boolean isMember(String UUID) {
        return getMemberFromUUID(UUID) != null;
    }

    public void addMember(ClanMember clanMember) {
        if (!isMember(clanMember.getUUID()))
            members.add(clanMember);
    }

    public void removeMemberFromUUID(String UUID) {
        ClanMember clanMember = getMemberFromUUID(UUID);

        if (clanMember != null)
            members.remove(clanMember);
    }

    public void removeMemberFromName(String playerName) {
        ClanMember clanMember = getMemberFromName(playerName);

        if (clanMember != null)
            members.remove(clanMember);
    }

    public void addInvite(String UUID) {
        if (!invitedUUIDs.contains(UUID))
            invitedUUIDs.add(UUID);
    }

    public void removeInvite(String UUID) {
        invitedUUIDs.remove(UUID);
    }

    public boolean isInvited(String UUID) {
        return invitedUUIDs.contains(UUID);
    }

    public List<ClanMember> getMembers() {
        return members;
    }

    public boolean equals(Clan clan)
    {
        return this.getOwner().getPlayerName().equalsIgnoreCase(clan.getOwner().getPlayerName());
    }
}