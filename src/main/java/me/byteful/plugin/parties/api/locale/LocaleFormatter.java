package me.byteful.plugin.parties.api.locale;

import net.md_5.bungee.api.CommandSender;

public interface LocaleFormatter<T, V> {
  V getRaw(String key);

  T get(String key);

  T getArgs(String key, Object... args);

  void send(CommandSender sender, String key);

  void sendArgs(CommandSender sender, String key, Object... args);

  default V getRaw(Messages key) {
    return getRaw(key.getKey());
  }

  default void send(CommandSender sender, Messages key) {
    send(sender, key.getKey());
  }

  default void sendArgs(CommandSender sender, Messages key, Object... args) {
    sendArgs(sender, key.getKey(), args);
  }

  default T get(Messages key) {
    return get(key.getKey());
  }

  default T getArgs(Messages key, Object... args) {
    return getArgs(key.getKey(), args);
  }
}
