package org.encinet.betterban;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static FileConfiguration config = YamlConfiguration.loadConfiguration(
            new File(JavaPlugin.getProvidingPlugin(BetterBan.class).getDataFolder(), "config.yml"));
    public static String message;

    public static List<String> help;
    public static String prefix;
    public static String reason;
    public static String suffix;

    public static void load() {
        message = get("message");
        help = listCP("help");
        prefix = get("prefix");
        reason = get("reason");
        suffix = get("suffix");
    }

    private static String get(String path, String def) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, def));
    }
    private static String get(String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path));
    }

    private static List<String> listCP(String path) {
        List<String> put = new ArrayList<>();
        for (String now : config.getStringList(path)) {
            put.add(ChatColor.translateAlternateColorCodes('&', now));
        }
        return put;
    }
}
