package me.byteful.plugin.parties.api.locale;

import net.kyori.adventure.text.Component;

import java.util.List;

public interface LocaleManager {
  void reload();

  LocaleFormatter<Component, String> single();

  LocaleFormatter<List<Component>, List<String>> split();
}
