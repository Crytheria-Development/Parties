package com.cjcameron92.crytheria.parties.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter @Setter
public class Party {

    private PlayerData owner;
    private Set<PlayerData> players;
    private Set<UUID> invitees;

    public Party(PlayerData owner) {
        this.owner = owner;
        this.players = new HashSet<>();
        this.players.add(owner);
        this.invitees = new HashSet<>();
    }

    public void addMember(PlayerData playerData) {
        this.players.add(playerData);
    }

    public boolean contains(UUID uuid) {
        return players.stream().anyMatch(playerData -> playerData.getUuid().equals(uuid));
    }


}
