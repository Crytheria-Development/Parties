package com.cjcameron92.crytheria.parties;

import gg.supervisor.loader.SupervisorLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class PartyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        SupervisorLoader.register(this);
    }
}
