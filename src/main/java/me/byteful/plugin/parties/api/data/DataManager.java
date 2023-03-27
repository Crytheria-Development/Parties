package me.byteful.plugin.parties.api.data;

import me.byteful.plugin.parties.data.parties.Party;

import java.io.Closeable;
import java.util.UUID;

public interface DataManager extends Closeable {
  Party getParty(UUID member);

  void setParty(Party party);

  void removeParty(Party party);
}
