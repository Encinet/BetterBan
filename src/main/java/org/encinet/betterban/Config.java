package org.encinet.betterban;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Config {
    private static final FileConfiguration config = YamlConfiguration.loadConfiguration(
            new File(JavaPlugin.getProvidingPlugin(BetterBan.class).getDataFolder(), "config.yml"));
    public static String message;
    public static boolean snEnable;
    public static String snText;
    public static List<String> help;
    public static String banReason;

    public static void load() {
        message = get("message", " &l&6Better&fBan &r&8>> &r");
        snEnable = config.getBoolean("Sentence-notice.enable", true);
        snText = get("Sentence-notice.text");
        help = listCP("help");
        banReason = get("reason");
    }

    private static String get(String path, String def) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, def));
    }
    private static String get(String path) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(path)));
    }

    private static List<String> listCP(String path) {
        List<String> put = new ArrayList<>();
        for (String now : config.getStringList(path)) {
            put.add(ChatColor.translateAlternateColorCodes('&', now));
        }
        return put;
    }
}
