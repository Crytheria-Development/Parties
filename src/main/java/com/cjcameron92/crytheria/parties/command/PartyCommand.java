package com.cjcameron92.crytheria.parties.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.cjcameron92.crytheria.parties.PartyPlugin;
import com.cjcameron92.crytheria.parties.controller.PartyController;
import com.cjcameron92.crytheria.parties.model.PlayerData;
import gg.supervisor.api.Component;
import gg.supervisor.api.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandAlias("party|p")
@Component
public class PartyCommand extends BaseCommand {

    private final PartyPlugin plugin;
    private final PartyController partyController;

    public PartyCommand(PartyPlugin plugin, PaperCommandManager commandManager, PartyController partyController) {
        commandManager.registerCommand(this);
        this.plugin = plugin;
        this.partyController = partyController;
    }

    @Subcommand("leave")
    public void onLeave(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            switch (partyController.leave(player)) {
                case SUCCESS -> player.sendMessage(Text.translate("&aYou have left your party."));
                case ERROR -> player.sendMessage(Text.translate("&cYou are not in a party"));
            }
        });
    }

    @Subcommand("invite")
    public void onInvite(Player player, OnlinePlayer onlinePlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            switch (partyController.invite(player, onlinePlayer.player)) {
                case SUCCESS -> {
                    player.sendMessage(Text.translate("&aYou have invited " + onlinePlayer.player.getName() + " to your party."));
                    onlinePlayer.player.sendMessage(Text.translate("&eYou have been invited to " + player.getName() + "s party!"));
                }
                case SAME_PERSON -> player.sendMessage(Text.translate("&cYou cannot preform this action on yourself!"));
                case NOT_PARTY_OWNER, ERROR -> player.sendMessage(Text.translate("&c/party leave to create invite to player"));
                case ALREADY_INVITED -> player.sendMessage(Text.translate("&cYou have already invited " + onlinePlayer.player.getName()));
                case ALREADY_IN_PARTY -> player.sendMessage(Text.translate("&c" + onlinePlayer.player.getName() + " is already in your party!"));
            }
        });
    }
    @Subcommand("kick")
    public void onKick(Player player, OnlinePlayer onlinePlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            switch (partyController.kick(player, onlinePlayer.player)) {
                case SUCCESS -> {
                    player.sendMessage(Text.translate("&aYou have kicked " + onlinePlayer.player.getName() + " from your party."));
                    onlinePlayer.player.sendMessage(Text.translate("&eYou have been kicked from " + player.getName() + "s party."));
                }
                case SAME_PERSON -> player.sendMessage(Text.translate("&cYou cannot preform this action on yourself!"));
                case NOT_PARTY_OWNER, ERROR -> player.sendMessage(Text.translate("&cYou are not the party leader!"));
                case NOT_IN_PARTY -> player.sendMessage(Text.translate("&c" + onlinePlayer.player.getName() + " is not in your party."));
            }
        });
    }

    @Subcommand("join")
    public void onJoin(Player player, OnlinePlayer onlinePlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            switch (partyController.join(onlinePlayer.player, player)) {
                case SUCCESS -> {
                    player.sendMessage(Text.translate("&aYou have joined " + onlinePlayer.player.getName() + "'s party."));
                    onlinePlayer.player.sendMessage(Text.translate("&e" + player.getName() + "has joined the party!"));
                }
                case SAME_PERSON -> player.sendMessage(Text.translate("&cYou cannot preform this action on yourself!"));
                case ERROR -> player.sendMessage(Text.translate("&cYou are not the party leader!"));
                case MAX_PARTY_SIZE -> player.sendMessage(Text.translate("&cYou cannot join a full party!"));
                case NOT_IN_PARTY -> player.sendMessage(Text.translate("&c" + onlinePlayer.player.getName() + " is not in your party."));
            }
        });
    }

    @Subcommand("transfer")
    public void onTransfer(Player player, OnlinePlayer onlinePlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            switch (partyController.transfer(onlinePlayer.player, player)) {
                case SUCCESS -> {
                    player.sendMessage(Text.translate("&aYou have joined " + onlinePlayer.player.getName() + "'s party."));
                    onlinePlayer.player.sendMessage(Text.translate("&e" + player.getName() + "has transferred the party!"));


                }
                case ERROR -> player.sendMessage(Text.translate("&cYou are not the party leader!"));
                case NOT_IN_PARTY -> player.sendMessage(Text.translate("&c" + onlinePlayer.player.getName() + " is not in your party."));
            }
        });
    }

    @Subcommand("info")
    public void onInfo(Player player, @Optional OnlinePlayer onlinePlayer) {
        Player target = player;
        if (onlinePlayer != null) {
            target = onlinePlayer.player;
        }
        if (partyController.info(target, party -> {
            player.sendMessage(Text.translate("&aParty Information &6" + party.getPlayers().size()));
            player.sendMessage(Text.translate("&aLeader: &f" + party.getOwner().getName()));
            player.sendMessage(Text.translate("&aMembers: " + party.getPlayers().stream().map(PlayerData::getName).collect(Collectors.joining(", "))));
        }) == PartyController.PartyResponse.ERROR) {
            player.sendMessage(Text.translate("&cThis player is not in a party"));
        }
    }
}
