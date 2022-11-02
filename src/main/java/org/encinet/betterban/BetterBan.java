package org.encinet.betterban;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

import org.encinet.betterban.command.*;

public final class BetterBan extends JavaPlugin {
    public static final Logger logger = Logger.getLogger("BetterBan");

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger.info("MAIN > Loading");
        saveDefaultConfig();
        Config.load();

        Objects.requireNonNull(Bukkit.getPluginCommand("bb")).setExecutor(new Ban());

        logger.info("COMMAND > Registered");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
