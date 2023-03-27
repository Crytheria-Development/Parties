package me.byteful.plugin.parties.api.locale;

public enum Messages {
  NO_PERMISSION("command.no_permission"),
  SUCCESS_RELOAD("command.success_reload"),
  ALREADY_IN_PARTY("command.party.already_in_party"),
  NOT_IN_PARTY("command.party.not_in_party"),
  SUCCESS_CREATE_PARTY("command.party.create.success_create"),
  CHAT_FORMAT("command.party.chat.format"),
  PARTY_LIST_HEADER("command.party.list.header"),
  PARTY_LIST_FOOTER("command.party.list.footer"),
  PARTY_LIST_LEADER("command.party.list.leader"),
  PARTY_LIST_MEMBERS("command.party.list.members"),
  KICK_SELF("command.party.kick.cannot_kick_self"),
  CANNOT_KICK("command.party.kick.cannot_kick"),
  PLAYER_NOT_IN_PARTY("command.party.kick.player_not_in_party"),
  SUCCESS_KICK("command.party.kick.success_kick"),
  CANNOT_LEAVE_PARTY("command.party.leave.cannot_leave"),
  SUCCESS_LEAVE("command.party.leave.success_leave"),
  CANNOT_DISBAND("command.party.disband.cannot_disband"),
  SUCCESS_DISBAND("command.party.disband.success_disbanded"),
  ALREADY_INVITED("command.party.invite.already_invited_player"),
  SUCCESS_INVITE("command.party.invite.success_invite"),
  INVITE_RECEIVED("command.party.invite.received_invite"),
  INVITE_TIMED_OUT("command.party.invite.invite_timed_out"),
  CANNOT_INVITE("command.party.invite.cannot_invite"),
  NOT_INVITED("command.party.not_invited"),
  SUCCESS_JOIN("command.party.join.success_join"),
  SUCCESS_DENY("command.party.deny.success_deny"),
  PARTY_WARPING("party_warping"),
  COMMAND_HELP("command.help"),
  CHAT_TOGGLE_OFF("command.party.chat.toggle_off"),
  CHAT_TOGGLE_ON("command.party.chat.toggle_on"),
  NOT_HUB("command.not_hub"),
  CANNOT_SELFINVITE("command.party.invite.cannot_invite_self");

  private final String key;

  Messages(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  @Override
  public String toString() {
    return key;
  }
}
