package com.parkourcraft.Parkour.data.clans;


import java.util.ArrayList;
import java.util.List;

public class Clan {

    private int ID;
    private String name;

    private ClanMember owner;
    private List<ClanMember> members = new ArrayList<>(); // Does not include the owner
    private List<String> invitedUUIDs = new ArrayList<>();

    public Clan(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ClanMember getMember(String UUID) {
        for (ClanMember member : members)
            if (member.getUUID().equals(UUID))
                return member;

        return null;
    }

    public ClanMember getOwner() {
        return owner;
    }

    public void promoteOwner(String UUID) {
        ClanMember newOwner = getMember(UUID);

        if (newOwner != null) {
            members.add(owner);
            owner = newOwner;
            members.remove(newOwner);
        }
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

}
