package com.parkourcraft.parkour.data.clans;

import java.util.ArrayList;
import java.util.List;

public class Clan {

    private int ID;
    private String tag;
    private int ownerID;

    private List<ClanMember> members = new ArrayList<>(); // Does not include the owner
    private List<String> invitedUUIDs = new ArrayList<>();

    public Clan(int clanID, String clanTag, int clanOwnerID) {
        this.ID = clanID;
        this.tag = clanTag;
        this.ownerID = clanOwnerID;
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

    public void setClanOwnerID(int clanOwnerID) {
        this.ownerID = clanOwnerID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public ClanMember getMember(String UUID) {
        for (ClanMember member : members)
            if (member.getUUID().equals(UUID))
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
        ClanMember newOwner = getMember(UUID);

        if (newOwner != null)
            ownerID = newOwner.getPlayerID();
    }

    public boolean isMember(String UUID) {
        return getMember(UUID) != null;
    }

    public void addMember(ClanMember clanMember) {
        if (!isMember(clanMember.getUUID()))
            members.add(clanMember);
    }

    public void removeMember(String UUID) {
        ClanMember clanMember = getMember(UUID);

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
}