package me.byteful.plugin.parties.api.locale.impl;

import me.byteful.plugin.parties.PartiesPlugin;
import me.byteful.plugin.parties.api.locale.LocaleFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class SplitLocaleFormatter implements LocaleFormatter<List<Component>, List<String>> {
  private final Configuration config, def;
  private final PartiesPlugin plugin;

  public SplitLocaleFormatter(Configuration config, Configuration def, PartiesPlugin plugin) {
    this.def = def;
    this.config = config;
    this.plugin = plugin;
  }

  @Override
  public List<String> getRaw(String key) {
    if (!config.contains(key)) {
      return def.getStringList(key);
    }

    return config.getStringList(key);
  }

  @Override
  public List<Component> get(String key) {
    final List<String> stringList = getRaw(key);
    final List<Component> list = new ArrayList<>();

    for (String line : stringList) {
      list.add(MiniMessage.miniMessage().deserialize(line, TagResolver.standard()));
    }

    return list;
  }

  @Override
  public List<Component> getArgs(String key, Object... args) {
    final List<String> stringList = getRaw(key);
    final List<Component> list = new ArrayList<>();

    for (String line : stringList) {
      for (int i = 0; i < args.length; i++) {
        line = line.replace("{" + i + "}", args[i].toString());
      }
      list.add(MiniMessage.miniMessage().deserialize(line, TagResolver.standard()));
    }

    return list;
  }

  @Override
  public void send(CommandSender sender, String key) {
    for (Component line : get(key)) {
      plugin.adventure().sender(sender).sendMessage(line);
    }
  }

  @Override
  public void sendArgs(CommandSender sender, String key, Object... args) {
    for (Component line : getArgs(key, args)) {
      plugin.adventure().sender(sender).sendMessage(line);
    }
  }
}
