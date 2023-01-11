package org.encinet.betterban.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.encinet.betterban.until.BanData;
import org.encinet.betterban.until.DateProcess;
import org.encinet.betterban.until.Tool;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.encinet.betterban.Config.*;

public class Ban implements TabExecutor {
    private static final Map<CommandSender, BanData> confirm = new ConcurrentHashMap<>();
    private static final String[] timeUnit = { "s", "m", "h", "d" };

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("bb.admin")) {
            sender.sendMessage(prefix + "§c没有权限");
            return true;
        } else if (args.length == 0) {
            for (String now : help) {
                sender.sendMessage(now);
            }
            return true;
        } else if (args[0].equals("--confirm")) {
            if (confirm.containsKey(sender)) {
                BanData data = confirm.get(sender);
                execute(sender, data.player(), data.reason(), data.ms());
                confirm.remove(sender);
            } else {
                sender.sendMessage(prefix + "暂无需确认的封禁");
            }
            return true;
        }

        // /bb <ID> <time> [reason]
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player.isBanned()) {
            sender.sendMessage(prefix + "此玩家已处于封禁状态");
            return true;
        }
        try {
            switch (args.length) {
                case 1 -> sender.sendMessage(prefix + "请指定时间");
                case 2 -> sender.sendMessage(prefix + "请指定封禁原因");
                default -> {
                    StringBuilder reasonSB = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        reasonSB.append(args[i]).append(" ");
                    }
                    String reason = String.valueOf(reasonSB);
                    long l = DateProcess.getData(args[1]);
                    if (player.hasPlayedBefore()) {
                        execute(sender, player, reason, l);
                    } else {
                        confirm.put(sender, new BanData(player, l, reason));
                        sender.sendMessage(prefix + "此玩家尚未进服 如需封禁请输入/bb --confirm确认");
                    }
                }
            }
        } catch (RuntimeException e) {
            sender.sendMessage(prefix + "出错了呢qwq");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            String[] args) {
        if (sender.hasPermission("bb.admin")) {
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
                    String d = "d:" + DateProcess.getNowTime();
                    if (args[1].startsWith("d")) {
                        list.add(d);
                    } else if (args[1].startsWith("l")) {
                        if (args[1].startsWith("l:") && args[1].length() > 2) {
                            String sub = args[1].substring(2);
                            int subLength = sub.length();
                            if (Tool.isNum(sub)) {
                                for (String n : timeUnit) {
                                    list.add("l:" + sub + n);
                                }
                            } else if (Arrays.asList(timeUnit).contains(sub.substring(subLength - 1)) && Tool.isNum(sub.substring(0, subLength - 1))) {
                                list.add(args[1]);
                            }
                        } else {
                            for (String n : timeUnit) {
                                list.add("l:1" + n);
                            }
                        }
                    } else {
                        list.add(d);
                        list.add("forever");
                        list.add("l:1s");
                    }
                }
                case 3 -> list.add("[reason]");
            }
            return list;
        } else {
            return null;
        }
    }

    private static String getReason(String reason, String time) {
        return "\n" + banReason
                .replace("%reason%", reason)
                .replace("%time%", time);
    }

    // 公开处刑
    public static void sentenceNotice(String executor, String executed, String time, String reason) {
        if (snEnable) {
            Bukkit.broadcast(Component.text(snText
                    .replace("%executor%", executor)
                    .replace("%executed%", executed)
                    .replace("%time%", time)
                    .replace("%reason%", reason)));
        }
    }

    public void execute(CommandSender sender, OfflinePlayer player, String reason, Long ms) {
        String senderName = sender.getName();
        String playerName = player.getName();
        String dataText = DateProcess.getDataText(ms);
        String formatReason = getReason(reason, dataText);

        if (ms == 0) {
            player.banPlayer(formatReason, senderName);// 永封
        } else {
            Date date = new Date(ms);
            player.banPlayer(formatReason, date, senderName);
        }
        sender.sendMessage(prefix + "封禁" + playerName + "成功");
        sentenceNotice(sender.getName(), playerName, dataText, reason);
    }
}
