package me.byteful.plugin.parties.data.parties;

import me.byteful.plugin.parties.PartiesPlugin;
import me.byteful.plugin.parties.api.locale.Messages;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PartyManager implements Listener {
  private final PartiesPlugin plugin;
  private final Map<UUID, Party> playerParty = new HashMap<>();
  private final Map<UUID, Set<Party>> invites = new HashMap<>();
  private final Set<UUID> partyChatToggle = new HashSet<>();

  public PartyManager(PartiesPlugin plugin) {
    this.plugin = plugin;
    ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
  }

  public boolean togglePartyChat(UUID uuid) {
    if (partyChatToggle.contains(uuid)) {
      partyChatToggle.remove(uuid);
      return false;
    } else {
      partyChatToggle.add(uuid);
      return true;
    }
  }

  public boolean isPartyChatToggled(UUID uuid) {
    return partyChatToggle.contains(uuid);
  }

  @EventHandler
  public void onChat(ChatEvent event) {
    if (!(event.getSender() instanceof final ProxiedPlayer player) || event.getMessage().startsWith("/")) {
      return;
    }

    if (isPartyChatToggled(player.getUniqueId()) && getPlayerParty(player.getUniqueId()) != null) {
      event.setCancelled(true);
      final Component toSend = plugin.getLocaleManager().single().getArgs(Messages.CHAT_FORMAT, player.getDisplayName(), event.getMessage());
      sendPartyChat(player.getUniqueId(), toSend);
    }
  }

//  @EventHandler
//  public void onLeaderServerSwitch(ServerSwitchEvent event) {
//    final UUID uniqueId = event.getPlayer().getUniqueId();
//    final Party party = getPlayerParty(uniqueId);
//    final Server to = event.getPlayer().getServer();
//    if (party != null && party.getLeader().equals(uniqueId)) {
//      for (UUID member : party.getMembers()) {
//        final ProxiedPlayer found = ProxyServer.getInstance().getPlayer(member);
//        if (found == null || found.getServer() == to) {
//          continue;
//        }
//
//        plugin.getLocaleManager().single().send(found, Messages.PARTY_WARPING);
//        found.connect(to.getInfo(), ServerConnectEvent.Reason.PLUGIN);
//      }
//    }
//  }

  public @Nullable Party getPlayerParty(UUID member) {
    return playerParty.get(member);
  }

  public void sendPartyChat(UUID member, Component message) {
    final Party party = getPlayerParty(member);
    if (party != null) {
      party.sendPartyChat(plugin, message);
    }
  }

  public void saveParty(Party party) {
    ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> plugin.getDataManager().setParty(party));
  }

  public Party createParty(UUID leader) {
    final Party party = new Party(UUID.randomUUID(), leader);
    saveParty(party);
    playerParty.put(leader, party);

    return party;
  }

  public void addPlayerToParty(UUID player, Party party) {
    playerParty.put(player, party);
    party.getMembers().add(player);
    party.getUsernameCache().put(player, ProxyServer.getInstance().getPlayer(player).getDisplayName());
    saveParty(party);
  }

  public void removePlayerFromParty(UUID player) {
    final Party party = playerParty.remove(player);
    if (party == null) {
      return;
    }

    if (player.equals(party.getLeader())) {
      disbandParty(party);
    } else {
      party.getMembers().remove(player);
      party.getUsernameCache().remove(player);
      saveParty(party);
    }
  }

  public void disbandParty(Party party) {
    for (UUID member : party.getMembers()) {
      playerParty.remove(member);
    }
    playerParty.remove(party.getLeader());
    party.setLeader(null);
    party.getMembers().clear();
    party.getUsernameCache().clear();
    for (Set<Party> invs : invites.values()) {
      invs.remove(party);
    }
    ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> plugin.getDataManager().removeParty(party));
  }

  public Map<UUID, Set<Party>> getInvites() {
    return invites;
  }

  public Set<Party> getInvites(UUID uuid) {
    if (!getInvites().containsKey(uuid)) {
      getInvites().put(uuid, new HashSet<>());
    }

    return getInvites().get(uuid);
  }
}
