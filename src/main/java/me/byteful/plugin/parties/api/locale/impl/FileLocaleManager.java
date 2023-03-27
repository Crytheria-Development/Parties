package me.byteful.plugin.parties.api.locale.impl;

import me.byteful.plugin.parties.PartiesPlugin;
import me.byteful.plugin.parties.api.locale.LocaleFormatter;
import me.byteful.plugin.parties.api.locale.LocaleManager;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileLocaleManager implements LocaleManager {
  private final File file;
  private final PartiesPlugin plugin;
  private LocaleFormatter<Component, String> single;
  private LocaleFormatter<List<Component>, List<String>> split;

  public FileLocaleManager(PartiesPlugin plugin) {
    this.plugin = plugin;
    final String locale = plugin.getConfig().getString("locale", "en_us");
    this.file = new File(plugin.getLocaleFolder(), locale.endsWith(".yml") ? locale : locale + ".yml");
    if (!file.exists()) {
      try {
        if (!file.createNewFile()) {
          plugin.getLogger().severe("Failed to make locale file for: " + locale);
          return;
        }
        try (InputStream def = plugin.getResourceAsStream("default_locale.yml")) {
          assert def != null : "Failed to find default locale in plugin JAR!";
          Files.copy(def, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    reload();
  }

  @Override
  public void reload() {
    try {
      final Configuration loaded = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
      final Configuration def;
      try (InputStream input = plugin.getResourceAsStream("default_locale.yml")) {
        def = ConfigurationProvider.getProvider(YamlConfiguration.class).load(input);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      single = new SingleLocaleFormatter(loaded, def, plugin);
      split = new SplitLocaleFormatter(loaded, def, plugin);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public LocaleFormatter<Component, String> single() {
    return single;
  }

  @Override
  public LocaleFormatter<List<Component>, List<String>> split() {
    return split;
  }
}
