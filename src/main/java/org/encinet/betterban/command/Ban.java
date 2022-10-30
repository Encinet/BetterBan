package org.encinet.betterban.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.encinet.betterban.Config.*;

public class Ban implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("bb.admin")) {
            sender.sendMessage(message + "§c没有权限");
            return true;
        } else if (args.length == 0 || "help".equals(args[0])) {
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
        } else if (!player.hasPlayedBefore()) {
            sender.sendMessage(message + "此玩家未进入过服务器");
            return true;
        }
        try {
            switch (args.length) {
                case 1 -> sender.sendMessage(message + "请指定时间");
                case 2 -> sender.sendMessage(message + "请指定封禁原因");
                default -> {
                    StringBuilder reason = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        reason.append(args[i]).append(" ");
                    }
                    String sName = sender.getName();
                    String sReason = String.valueOf(reason);
                    String playerName = player.getName();

                    long l = getData(args[1]);
                    String dataText = getDataText(getData(args[1]));
                    if (l == 0) {
                        player.banPlayer(getReason(sReason, dataText), sName);// 永封
                    } else {
                        Date date = new Date(l);
                        player.banPlayer(getReason(sReason, dataText), date, sName);
                    }
                    sender.sendMessage(message + "封禁" + playerName + "成功");
                    sentenceNotice(sender.getName(), playerName, dataText, sReason);
                }
            }
        } catch (RuntimeException e) {
            sender.sendMessage(message + "出错了呢qwq");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                for (Player n : Bukkit.getOnlinePlayers()) {
                    String name = n.getName();
                    String now = args[0];
                    int length = now.length();
                    if (length > name.length()) {
                        continue;// 长度超出跳过本次循环
                    }
                    if (name.toLowerCase().startsWith(now.toLowerCase())) {
                        list.add(name);
                    }
                }
            }
            case 2 -> {
                if (args[1].startsWith("d")) {
                    list.add("d:2000/1/1");
                } else if (args[1].startsWith("l")) {
                    list.add("l:1s");
                    list.add("l:1m");
                    list.add("l:1h");
                    list.add("l:1d");
                } else {
                    list.add("d:2000/1/1");
                    list.add("forever");
                    list.add("l:1s");
                }
            }
            case 3 -> list.add("[reason]");
        }
        return list;
    }

    private static String getReason(String reason, String time) {
        return "\n" + banReason
        .replace("%reason%", reason)
        .replace("%time%", time);
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

    private static String getDataText(Long time) {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return (time == 0) ? "永封" : ft.format(time);
    }

    // 公开处刑
    private static void sentenceNotice(String executor, String executed, String time, String reason) {
        if (snEnable) {
            Bukkit.broadcast(Component.text(snText
                    .replace("%executor%", executor)
                    .replace("%executed%", executed)
                    .replace("%time%", time)
                    .replace("%reason%", reason)));
        }
    }
}
