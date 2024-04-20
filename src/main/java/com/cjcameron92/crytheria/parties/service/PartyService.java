package com.cjcameron92.crytheria.parties.service;

import com.cjcameron92.crytheria.parties.model.Party;
import com.cjcameron92.crytheria.parties.model.PlayerData;
import com.cjcameron92.crytheria.parties.storage.JedisPartyStorage;
import gg.supervisor.api.Component;

import java.util.UUID;

@Component
public class PartyService {

    private final JedisPartyStorage partyStorage;

    public PartyService(JedisPartyStorage partyStorage) {
        this.partyStorage = partyStorage;
    }

    public boolean hasParty(UUID player) {
        return partyStorage.values().stream().anyMatch(party -> party.contains(player));
    }

    public boolean isOwner(PlayerData player) {
        return partyStorage.values().stream().anyMatch(party -> party.getOwner().equals(player) && party.getPlayers().contains(player));
    }

    public Party getParty(UUID uuid) {
        return partyStorage.get(uuid.toString());
    }

    public Party from(UUID uuid) {
        return partyStorage.values().stream().filter(party -> party.contains(uuid)).findFirst().orElse(null);
    }

    public void transfer(Party party, PlayerData to) {
        // ensure logic that the member is in the party!
        partyStorage.delete(party.getOwner().getUuid().toString());
        party.setOwner(to);
        partyStorage.save(to.getUuid().toString(), party);
    }

    public void destroyParty(Party party) {
        this.partyStorage.delete(party.getOwner().getUuid().toString());
    }

    public void saveParty(Party party) {
        this.partyStorage.save(party.getOwner().getUuid().toString(), party);
    }

}
