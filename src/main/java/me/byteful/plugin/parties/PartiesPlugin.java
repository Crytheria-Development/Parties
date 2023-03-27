package me.byteful.plugin.parties;

import me.byteful.plugin.parties.api.data.DataManager;
import me.byteful.plugin.parties.api.data.impl.MongoDataManager;
import me.byteful.plugin.parties.api.locale.LocaleManager;
import me.byteful.plugin.parties.api.locale.impl.FileLocaleManager;
import me.byteful.plugin.parties.data.parties.PartyManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import revxrsal.commands.bungee.BungeeCommandHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class PartiesPlugin extends Plugin {
  private DataManager dataManager;
  private PartyManager partyManager;
  private LocaleManager localeManager;
  private BungeeCommandHandler commandHandler;
  private BungeeAudiences adventure;
  private Configuration config;

  @Override
  public void onEnable() {
    reloadConfig();
    getLocaleFolder();
    localeManager = new FileLocaleManager(this);
    getLogger().info("Loaded config and locale...");

    adventure = BungeeAudiences.create(this);
    commandHandler = BungeeCommandHandler.create(this);
    commandHandler.setExceptionHandler(new Commands.CommandMessageHandler(this));
    commandHandler.setHelpWriter((command, actor) -> String.format("- /%s %s: %s", command.getPath().toRealString(), command.getUsage(), command.getDescription()));
    commandHandler.register(new Commands(this));
    getLogger().info("Loaded commands...");

    loadDataManager();
    getLogger().info("Loaded data manager...");

    partyManager = new PartyManager(this);
    getLogger().info("Loaded party manager...");

    getLogger().info("Successfully started Parties!");
  }

  @Override
  public void onDisable() {
    if (adventure != null) {
      adventure.close();
    }
    try {
      dataManager.close();
      getLogger().info("Disconnected data manager...");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    commandHandler.unregisterAllCommands();
    getLogger().info("Unregistered commands...");

    getLogger().info("Successfully stopped Companies...");
  }

  private void loadDataManager() {
    final String URI = getConfig().getString("mongo.uri");
    final String database = getConfig().getString("mongo.database");
    dataManager = new MongoDataManager(URI, database);
  }

  public void reloadConfig() {
    try {
      final File file = new File(getDataFolder(), "config.yml");
      if (!file.exists()) {
        file.getParentFile().mkdirs();
        try (InputStream def = getResourceAsStream("config.yml")) {
          assert def != null : "Failed to find config in plugin JAR!";
          Files.copy(def, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
      }

      config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public BungeeAudiences adventure() {
    if (adventure == null) {
      throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
    }
    return adventure;
  }

  public DataManager getDataManager() {
    return dataManager;
  }

  public PartyManager getPartyManager() {
    return partyManager;
  }

  public LocaleManager getLocaleManager() {
    return localeManager;
  }

  public Configuration getConfig() {
    return config;
  }

  public File getLocaleFolder() {
    final File folder = new File(getDataFolder(), "locale");
    if (!folder.exists()) {
      folder.mkdirs();
    }
    return folder;
  }
}
