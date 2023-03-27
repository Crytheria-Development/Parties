package me.byteful.plugin.parties;

import me.byteful.plugin.parties.api.locale.LocaleManager;
import me.byteful.plugin.parties.api.locale.Messages;
import me.byteful.plugin.parties.data.parties.Party;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.bungee.exception.BungeeExceptionAdapter;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.NoPermissionException;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Command({"party", "p"})
public class Commands {
  private final PartiesPlugin plugin;

  public Commands(PartiesPlugin plugin) {
    this.plugin = plugin;
  }

  @DefaultFor({"party", "p"})
  public void onDefault(CommandSender sender, @Optional ProxiedPlayer target) {
    if (target == null || !(sender instanceof ProxiedPlayer)) {
      onHelp(sender);
      return;
    }

    onInvite((ProxiedPlayer) sender, target);
  }

  @Subcommand("help")
  public void onHelp(CommandSender sender) {
    plugin.getLocaleManager().split().send(sender, Messages.COMMAND_HELP);
  }

  @Subcommand("create")
  @CommandPermission("parties.create")
  @Description("Create a party.")
  public void onCreate(ProxiedPlayer sender) {
    if (!isInHub(sender)) {
      plugin.getLocaleManager().single().send(sender, Messages.NOT_HUB);

      return;
    }
    if (isInParty(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.ALREADY_IN_PARTY);

      return;
    }

    plugin.getPartyManager().createParty(sender.getUniqueId());
    plugin.getLocaleManager().single().send(sender, Messages.SUCCESS_CREATE_PARTY);
  }

  @Subcommand("invite")
  @CommandPermission("parties.invite")
  @Description("Invite a player to your party.")
  public void onInvite(ProxiedPlayer sender, ProxiedPlayer target) {
    if (!isInParty(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.NOT_IN_PARTY);

      return;
    }
    final Party party = getParty(sender.getUniqueId());
    if (!party.getLeader().equals(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.CANNOT_INVITE);

      return;
    }
    if (sender.getUniqueId().equals(target.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.CANNOT_SELFINVITE);

      return;
    }
    final Set<Party> invites = plugin.getPartyManager().getInvites(target.getUniqueId());
    if (invites.contains(party)) {
      plugin.getLocaleManager().single().send(sender, Messages.ALREADY_INVITED);

      return;
    }

    final String targetName = target.getDisplayName();
    plugin.getLocaleManager().single().sendArgs(sender, Messages.SUCCESS_INVITE, targetName);
    plugin.getLocaleManager().single().sendArgs(target, Messages.INVITE_RECEIVED, sender.getDisplayName(), sender.getName());
    invites.add(party);
    ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
      final Set<Party> parties = plugin.getPartyManager().getInvites(target.getUniqueId());
      if (parties == null) {
        return; // Not sure how this is possible, but better safe than sorry.
      }
      if (parties.remove(party) && sender.isConnected()) {
        plugin.getLocaleManager().single().sendArgs(sender, Messages.INVITE_TIMED_OUT, targetName);
      }
    }, plugin.getConfig().getInt("party_invite_timeout"), TimeUnit.SECONDS);
  }

  @Subcommand("join")
  @CommandPermission("parties.join")
  @Description("Join a party.")
  public void onJoin(ProxiedPlayer sender, ProxiedPlayer target) {
    if (!isInHub(sender)) {
      plugin.getLocaleManager().single().send(sender, Messages.NOT_HUB);

      return;
    }
    if (isInParty(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.ALREADY_IN_PARTY);

      return;
    }

    final Party party = getParty(target.getUniqueId());
    if (party == null || party.getLeader() == null || !plugin.getPartyManager().getInvites(sender.getUniqueId()).contains(party)) {
      plugin.getLocaleManager().single().send(sender, Messages.NOT_INVITED);

      return;
    }

    plugin.getPartyManager().addPlayerToParty(sender.getUniqueId(), party);
    plugin.getPartyManager().getInvites(sender.getUniqueId()).remove(party);
    party.sendPartyChat(plugin, plugin.getLocaleManager().split().getArgs(Messages.SUCCESS_JOIN, sender.getDisplayName()).toArray(Component[]::new));

    if (sender.getServer() != target.getServer()) {
      sender.connect(target.getServer().getInfo());
      plugin.getLocaleManager().single().send(sender, Messages.PARTY_WARPING);
    }
  }

  @Subcommand("disband")
  @CommandPermission("parties.disband")
  @Description("Disband your party.")
  public void onDisband(ProxiedPlayer sender) {
    if (!isInParty(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.NOT_IN_PARTY);

      return;
    }

    final Party party = getParty(sender.getUniqueId());
    if (!party.getLeader().equals(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.CANNOT_DISBAND);

      return;
    }

    plugin.getPartyManager().disbandParty(party);
    plugin.getLocaleManager().single().send(sender, Messages.SUCCESS_DISBAND);
  }

  @Subcommand("leave")
  @CommandPermission("parties.leave")
  @Description("Leave your current party.")
  public void onLeave(ProxiedPlayer sender) {
    if (!isInParty(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.NOT_IN_PARTY);

      return;
    }

    final Party party = getParty(sender.getUniqueId());
    if (party.getLeader().equals(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.CANNOT_LEAVE_PARTY);

      return;
    }

    plugin.getPartyManager().removePlayerFromParty(sender.getUniqueId());
    plugin.getLocaleManager().single().send(sender, Messages.SUCCESS_LEAVE);
  }

  @Subcommand("kick")
  @CommandPermission("parties.kick")
  @Description("Kick a player from your party.")
  public void onKick(ProxiedPlayer sender, String target) {
    if (!isInParty(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.NOT_IN_PARTY);

      return;
    }

    final Party party = getParty(sender.getUniqueId());
    if (!party.getLeader().equals(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.CANNOT_KICK);

      return;
    }

    if (target.equals(sender.getName())) {
      plugin.getLocaleManager().single().send(sender, Messages.KICK_SELF);

      return;
    }

    final UUID targetUUID = party.getUUID(target);
    if (targetUUID == null) {
      plugin.getLocaleManager().single().send(sender, Messages.PLAYER_NOT_IN_PARTY);

      return;
    }

    plugin.getPartyManager().removePlayerFromParty(targetUUID);
    plugin.getLocaleManager().single().sendArgs(sender, Messages.SUCCESS_KICK, target);
    // todo tell person they got kicked
  }

  @Subcommand({"chat", "c"})
  @CommandPermission("parties.chat")
  @Description("Send a party-only chat message or toggle party chat.")
  public void onChat(ProxiedPlayer sender, @Optional String message) {
    if (!isInParty(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.NOT_IN_PARTY);

      return;
    }

    if (message == null || message.isBlank() || message.isEmpty()) {
      if (plugin.getPartyManager().togglePartyChat(sender.getUniqueId())) {
        plugin.getLocaleManager().single().send(sender, Messages.CHAT_TOGGLE_ON);
      } else {
        plugin.getLocaleManager().single().send(sender, Messages.CHAT_TOGGLE_OFF);
      }

      return;
    }

    final Component toSend = plugin.getLocaleManager().single().getArgs(Messages.CHAT_FORMAT, sender.getDisplayName(), String.join(" ", message));
    plugin.getPartyManager().sendPartyChat(sender.getUniqueId(), toSend);
  }

  @Subcommand({"list", "members"})
  @CommandPermission("parties.list")
  @Description("See a list of party members.")
  public void onList(ProxiedPlayer sender) {
    final LocaleManager locale = plugin.getLocaleManager();
    if (!isInParty(sender.getUniqueId())) {
      locale.single().send(sender, Messages.NOT_IN_PARTY);

      return;
    }
    final String online = locale.single().getRaw("command.party.list.online");
    final String offline = locale.single().getRaw("command.party.list.offline");
    final Party party = getParty(sender.getUniqueId());
    locale.split().sendArgs(sender, Messages.PARTY_LIST_HEADER, party.getMembers().size() + 1);
    locale.single().sendArgs(sender, Messages.PARTY_LIST_LEADER, party.getUsername(party.getLeader()));
    final StringBuilder membersJoined = new StringBuilder();
    for (Party.PartyUsername username : party.getMemberUsernames()) {
      if (username.isOnline()) {
        membersJoined.append(username.getUsername()).append(online);
      } else {
        membersJoined.append(username.getUsername()).append(offline);
      }
    }
    locale.single().sendArgs(sender, Messages.PARTY_LIST_MEMBERS, membersJoined.toString());
    locale.split().sendArgs(sender, Messages.PARTY_LIST_FOOTER);
  }

  @Subcommand("deny")
  @CommandPermission("parties.deny")
  @Description("Deny a party invitation.")
  public void onDeny(ProxiedPlayer sender, ProxiedPlayer target) {
    if (isInParty(sender.getUniqueId())) {
      plugin.getLocaleManager().single().send(sender, Messages.ALREADY_IN_PARTY);

      return;
    }

    final Party party = getParty(target.getUniqueId());
    if (party == null || !plugin.getPartyManager().getInvites(sender.getUniqueId()).contains(party)) {
      plugin.getLocaleManager().single().send(sender, Messages.NOT_INVITED);

      return;
    }

    if (plugin.getPartyManager().getInvites(sender.getUniqueId()).remove(party)) {
      plugin.getLocaleManager().single().send(sender, Messages.SUCCESS_DENY);
    }
  }

  @Subcommand("reload")
  @CommandPermission("parties.reload")
  @Description("Reload plugin configuration.")
  public void onReload(CommandSender sender) {
    plugin.reloadConfig();
    plugin.getLocaleManager().reload();
    plugin.getLocaleManager().single().send(sender, Messages.SUCCESS_RELOAD);
  }

  private Party getParty(UUID member) {
    return plugin.getPartyManager().getPlayerParty(member);
  }

  private boolean isInParty(UUID player) {
    return getParty(player) != null;
  }

  private boolean isInHub(ProxiedPlayer player) {
    return plugin.getConfig().getStringList("hub_servers").stream().anyMatch(hub -> player.getServer().getInfo().getName().equals(hub));
  }

  static class CommandMessageHandler extends BungeeExceptionAdapter {
    private final PartiesPlugin plugin;

    CommandMessageHandler(PartiesPlugin plugin) {
      this.plugin = plugin;
    }

    @Override
    public void noPermission(@NotNull CommandActor actor, @NotNull NoPermissionException exception) {
      final CommandSender sender = ((BungeeCommandActor) actor).getSender();
      plugin.adventure().sender(sender).sendMessage(plugin.getLocaleManager().single().get(Messages.NO_PERMISSION));
    }
  }
}
