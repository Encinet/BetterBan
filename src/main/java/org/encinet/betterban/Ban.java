package org.encinet.betterban;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.encinet.betterban.Config.*;

public class Ban implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("bb.admin")) {
            sender.sendMessage(message + "§c没有权限");
            return true;
        } else if (args.length < 1 || "help".equals(args[0])) {
            for (String now : help) {
                sender.sendMessage(now);
            }
            return true;
        }
        // /bb <ID> <time> [reason]
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player.isBanned()) {
            sender.sendMessage(message + "此玩家已处于封禁状态");
            return true;
        }
        try {
            switch (args.length) {
                case 1 -> sender.sendMessage(message + "请指定时间");
                case 2 -> sender.sendMessage(message + "请指定封禁原因");
                case 3 -> {
                    StringBuilder reason = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        reason.append(args[i]).append(" ");
                    }
                    long l = getData(args[1]);
                    String sName = sender.getName();
                    String sReason = String.valueOf(reason);
                    String playerName = player.getName();
                    if (l == 0) {
                        player.banPlayer(getReason(sReason, l), sName);// 永封
                    } else {
                        Date date = new Date(l);
                        player.banPlayer(getReason(sReason, l), date, sName);
                    }
                    sender.sendMessage(message + "封禁" + playerName + "成功");
                    sentenceNotice(sender.getName(), playerName, sReason);
                }
            }
        } catch (RuntimeException e) {
            sender.sendMessage(message + "出错了呢qwq");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return TabComplete.returnList(args, args.length, sender);
    }

    private static String getReason(String text, Long time) {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String textTime = (time == 0) ? "永封" : ft.format(time);
        if (Objects.equals(text, "")) {
            return (prefix + "\n" + suffix).replace("%time%", textTime);
        } else return (prefix + "\n" + reason.replace("%reason%", text) + "\n" + suffix).replace("%time%", textTime);
    }

    private static Long getData(String text) {
        // d:2022/8/16
        // l:1d
        if (text.startsWith("d:")) {
            text = text.substring(2);
            SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd");
            Date date;
            try {
                date = ft.parse(text);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return date.getTime();
        } else if (text.startsWith("l:")) {
            // s秒 m分 h小时 d天
            long l = Long.parseLong(text.substring(2, text.length() - 1));
            switch (text.substring(text.length() - 1)) {
                case "s":
                    return System.currentTimeMillis() + (l * 1000);
                case "m":
                    return System.currentTimeMillis() + (l * 60000);
                case "h":
                    return System.currentTimeMillis() + (l * 3600000);
                case "d":
                    return System.currentTimeMillis() + (l * 86400000);
            }
        } else if ("forever".equals(text)) {
            return (long) 0;
        }
        return (long) 0;
    }

    private static void sentenceNotice(String executor, String executed, String reason) {
        if (snEnable) {
            Bukkit.broadcast(Component.text(snText
                    .replace("%executor%", executor)
                    .replace("%executed%", executed)
                    .replace("%reason%", reason)));
        }
    }
}
