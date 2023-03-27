package me.byteful.plugin.parties.api.locale.impl;

import me.byteful.plugin.parties.PartiesPlugin;
import me.byteful.plugin.parties.api.locale.LocaleFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.config.Configuration;

public class SingleLocaleFormatter implements LocaleFormatter<Component, String> {
  private final Configuration config, def;
  private final PartiesPlugin plugin;

  public SingleLocaleFormatter(Configuration config, Configuration def, PartiesPlugin plugin) {
    this.def = def;
    this.config = config;
    this.plugin = plugin;
  }

  @Override
  public String getRaw(String key) {
    return config.getString(key, def.getString(key, "MessageNotFound:" + key));
  }

  @Override
  public Component get(String key) {
    final String msg = getRaw(key);

    return MiniMessage.miniMessage().deserialize(msg, TagResolver.standard());
  }

  @Override
  public Component getArgs(String key, Object... args) {
    String msg = getRaw(key);

    for (int i = 0; i < args.length; i++) {
      msg = msg.replace("{" + i + "}", args[i].toString());
    }

    return MiniMessage.miniMessage().deserialize(msg, TagResolver.standard());
  }

  @Override
  public void send(CommandSender sender, String key) {
    plugin.adventure().sender(sender).sendMessage(get(key));
  }

  @Override
  public void sendArgs(CommandSender sender, String key, Object... args) {
    plugin.adventure().sender(sender).sendMessage(getArgs(key, args));
  }
}
