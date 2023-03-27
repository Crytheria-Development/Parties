package me.byteful.plugin.parties.data.parties;

import me.byteful.plugin.parties.PartiesPlugin;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

public class Party {
  private final UUID uniqueId;
  private final Set<UUID> members = new HashSet<>();
  private final Map<UUID, String> usernameCache = new HashMap<>();
  private UUID leader;

  public Party(UUID uniqueId, UUID leader) {
    this.uniqueId = uniqueId;
    this.leader = leader;
  }

  public boolean isPlayerInParty(UUID player) {
    return leader.equals(player) || members.contains(player);
  }

  public void sendPartyChat(PartiesPlugin plugin, Component... message) {
    final Set<UUID> plrs = new HashSet<>(members);
    plrs.add(leader);

    for (UUID sendTo : plrs) {
      final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(sendTo);
      if (player != null) {
        for (Component line : message) {
          plugin.adventure().player(player).sendMessage(line);
        }
      }
    }
  }

  public UUID getUniqueId() {
    return uniqueId;
  }

  public UUID getLeader() {
    return leader;
  }

  public void setLeader(UUID leader) {
    this.leader = leader;
  }

  public Set<UUID> getMembers() {
    return members;
  }

  public Map<UUID, String> getUsernameCache() {
    return usernameCache;
  }

  public Set<PartyUsername> getMemberUsernames() {
    final Set<PartyUsername> set = new HashSet<>();

    for (UUID uuid : getMembers()) {
      final ProxiedPlayer found = ProxyServer.getInstance().getPlayer(uuid);
      if (found != null) {
        set.add(new PartyUsername(found.getDisplayName(), true));
      } else {
        set.add(new PartyUsername(getUsernameCache().get(uuid), false));
      }
    }

    return set;
  }

  public String getUsername(UUID uuid) {
    final ProxiedPlayer found = ProxyServer.getInstance().getPlayer(uuid);
    if (found != null) {
      return found.getDisplayName();
    } else {
      return getUsernameCache().get(uuid);
    }
  }

  public UUID getUUID(String username) {
    for (Map.Entry<UUID, String> entry : usernameCache.entrySet()) {
      if (entry.getValue().equals(username)) {
        return entry.getKey();
      }
    }

    return null;
  }

  public static class PartyUsername {
    private final String username;
    private final boolean isOnline;

    public PartyUsername(String username, boolean isOnline) {
      this.username = username;
      this.isOnline = isOnline;
    }

    public String getUsername() {
      return username;
    }

    public boolean isOnline() {
      return isOnline;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      PartyUsername that = (PartyUsername) o;
      return isOnline == that.isOnline && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
      return Objects.hash(username, isOnline);
    }

    @Override
    public String toString() {
      return "PartyUsername{" + "username='" + username + '\'' + ", isOnline=" + isOnline + '}';
    }
  }
}
