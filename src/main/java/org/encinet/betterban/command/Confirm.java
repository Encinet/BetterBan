package org.encinet.betterban.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.encinet.betterban.until.BanData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.encinet.betterban.Config.*;

public class Confirm implements CommandExecutor {
    public static final Map<CommandSender, BanData> list = new ConcurrentHashMap<>();
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (list.containsKey(sender)) {
            sender.sendMessage(prefix + "暂无需确认封禁");
        } else {
            list.get(sender).ban(sender.getName());
        }
        return true;
    }
}