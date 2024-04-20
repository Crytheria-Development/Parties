package com.cjcameron92.crytheria.parties.controller;

import com.cjcameron92.crytheria.parties.config.PartyConfig;
import com.cjcameron92.crytheria.parties.model.Party;
import com.cjcameron92.crytheria.parties.model.PlayerData;
import com.cjcameron92.crytheria.parties.service.PartyService;
import gg.supervisor.api.Component;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@Component
public class PartyController {

    private final PartyService partyService;
    private final PartyConfig partyConfig;

    public PartyController(PartyService partyService, PartyConfig partyConfig) {
        this.partyService = partyService;
        this.partyConfig = partyConfig;
    }



    /**
     * Call async to invite player
     * @param owner {@link Player}
     * @param target {@link Player}
     * @return {@link PartyResponse}
     */
    public PartyResponse invite(Player owner, Player target) {
        Party party = partyService.getParty(owner.getUniqueId());
        if (owner.equals(target)) return PartyResponse.SAME_PERSON;
        if (party == null && !partyService.hasParty(owner.getUniqueId())) {
            party = new Party(new PlayerData(owner.getUniqueId(), owner.getName()));
            partyService.saveParty(party);
        } else if (party == null) return PartyResponse.ERROR;
        if (!party.getOwner().getUuid().equals(owner.getUniqueId())) {
            return PartyResponse.NOT_PARTY_OWNER;
        } else if (party.contains(target.getUniqueId())) {
            return PartyResponse.ALREADY_IN_PARTY;
        } else if (party.getInvitees().contains(target.getUniqueId())) {
            return PartyResponse.ALREADY_INVITED;
        }
        party.getInvitees().add(target.getUniqueId());
        partyService.saveParty(party);
        return PartyResponse.SUCCESS;
    }


    public PartyResponse kick(Player owner, Player target) {
        Party party = partyService.getParty(owner.getUniqueId());
        if (owner.equals(target)) {
            return PartyResponse.SAME_PERSON;
        } else if (party == null) return PartyResponse.ERROR;
        if (!party.getOwner().getUuid().equals(owner.getUniqueId())) {
            return PartyResponse.NOT_PARTY_OWNER;
        } else if (!party.contains(target.getUniqueId())) {
            return PartyResponse.NOT_IN_PARTY;
        }
        party.getPlayers().removeIf(playerData -> playerData.getUuid().equals(target.getUniqueId()));
        party.getInvitees().removeIf(uuid -> uuid.equals(target.getUniqueId()));
        partyService.saveParty(party);
        return PartyResponse.SUCCESS;
    }

    public PartyResponse join(Player owner, Player target) {
        Party party = partyService.getParty(owner.getUniqueId());
        if (owner.equals(target)) {
            return PartyResponse.SAME_PERSON;
        } else if (party == null) {
            return PartyResponse.ERROR;
        } else if (party.contains(target.getUniqueId())) {
            return PartyResponse.ALREADY_IN_PARTY;
        } else if (!party.getInvitees().contains(target.getUniqueId())) {
            return PartyResponse.NOT_INVITED;
        } else if (party.getPlayers().size() >= partyConfig.maxPartySize) {
            return PartyResponse.MAX_PARTY_SIZE;
        }

        party.getInvitees().removeIf(uuid -> uuid.equals(target.getUniqueId()));
        party.addMember(new PlayerData(target.getUniqueId(), target.getName()));

        partyService.saveParty(party);
        return PartyResponse.SUCCESS;
    }

    public PartyResponse transfer(Player owner, Player target) {
        Party party = partyService.getParty(owner.getUniqueId());
        if (owner.equals(target)) {
            return PartyResponse.SAME_PERSON;
        } else if (party == null) {
            return PartyResponse.ERROR;
        } else if (!party.contains(target.getUniqueId())) {
            return PartyResponse.NOT_IN_PARTY;
        } else {

            partyService.transfer(party, new PlayerData(target.getUniqueId(), target.getName()));

            partyService.saveParty(party);
            return PartyResponse.SUCCESS;
        }
    }

    public PartyResponse leave(Player player) {
        final Party party = partyService.getParty(player.getUniqueId());
        if (party == null) return PartyResponse.ERROR;
        if (party.getOwner().getUuid().equals(player.getUniqueId())) {
            partyService.destroyParty(party);
        } else {
            party.getPlayers().removeIf(uuid -> uuid.getUuid().equals(player.getUniqueId()));
            partyService.saveParty(party);
        }
        return PartyResponse.SUCCESS;
    }

    public PartyResponse info(Player player, Consumer<Party> consumer) {
        Party party = partyService.from(player.getUniqueId());
        if (party == null) {
            return PartyResponse.ERROR;
        }

        consumer.accept(party);

        return PartyResponse.SUCCESS;
    }


    public enum PartyResponse {
        ERROR,
        SUCCESS,

        SAME_PERSON,

        NOT_PARTY_OWNER,
        ALREADY_INVITED,
        ALREADY_IN_PARTY,

        MAX_PARTY_SIZE,


        NOT_IN_PARTY,
        NOT_INVITED
    }
}
