package org.encinet.betterban.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.encinet.betterban.Config;
import org.encinet.betterban.util.DateProcess;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * BanList 命令
 * 显示所有被封禁的玩家列表
 */
public class BanList {
    private static final int BANS_PER_PAGE = 10;

    public static LiteralCommandNode<CommandSourceStack> create() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("banlist")
                .requires(source -> source.getSender().hasPermission("bb.admin"))
                .executes(ctx -> {
                    showBanList(ctx.getSource().getSender(), 1);
                    return Command.SINGLE_SUCCESS;
                });

        // /banlist <page>
        command.then(Commands.argument("page", IntegerArgumentType.integer(1))
                .executes(ctx -> {
                    int page = ctx.getArgument("page", Integer.class);
                    showBanList(ctx.getSource().getSender(), page);
                    return Command.SINGLE_SUCCESS;
                }));

        return command.build();
    }

    /**
     * 显示封禁列表
     */
    private static void showBanList(CommandSender sender, int page) {
        Set<BanEntry<PlayerProfile>> banEntries = Bukkit.getBanList(BanListType.PROFILE).getEntries();

        if (banEntries.isEmpty()) {
            sender.sendMessage(Config.prefix.append(
                    Config.deserializeWithPlaceholders("<yellow>当前没有被封禁的玩家</yellow>")
            ));
            return;
        }

        List<BanEntry<PlayerProfile>> banList = new ArrayList<>(banEntries);
        int totalBans = banList.size();
        int totalPages = (int) Math.ceil((double) totalBans / BANS_PER_PAGE);

        // 验证页码
        if (page < 1 || page > totalPages) {
            sender.sendMessage(Config.prefix.append(
                    Config.deserializeWithPlaceholders("<red>无效的页码！总共 " + totalPages + " 页</red>")
            ));
            return;
        }

        // 计算分页
        int startIndex = (page - 1) * BANS_PER_PAGE;
        int endIndex = Math.min(startIndex + BANS_PER_PAGE, totalBans);

        // 发送标题
        sender.sendMessage(Config.deserializeWithPlaceholders(
                "<green><bold>━━━━━━━━━ 封禁列表 ━━━━━━━━━</bold></green>"
        ));
        sender.sendMessage(Config.deserializeWithPlaceholders(
                "<gray>第 </gray><white><bold>" + page + "</bold></white><gray>/</gray><white>" + totalPages + "</white> <gray>页 | 共 </gray><white>" + totalBans + "</white> <gray>条记录</gray>"
        ));
        sender.sendMessage(Component.empty());

        // 显示封禁条目
        for (int i = startIndex; i < endIndex; i++) {
            BanEntry<PlayerProfile> entry = banList.get(i);
            sendBanEntry(sender, i + 1, entry);
        }

        // 发送分页导航
        sender.sendMessage(Component.empty());
        sendPageNavigation(sender, page, totalPages);
    }

    /**
     * 发送单个封禁条目
     */
    private static void sendBanEntry(CommandSender sender, int index, BanEntry<PlayerProfile> entry) {
        String playerName = entry.getBanTarget().getName();
        if (playerName == null) {
            playerName = "未知玩家";
        }

        String reason = entry.getReason();
        if (reason == null || reason.isEmpty()) {
            reason = "无原因";
        }
        // 清理旧版格式代码
        reason = reason.replaceAll("§[0-9a-fk-or]", "");

        Date expiration = entry.getExpiration();
        String timeText = (expiration == null) ? "永久" : DateProcess.getDataText(expiration.getTime());

        String source = entry.getSource();
        if (source.isEmpty()) {
            source = "系统";
        }

        // 创建悬停信息
        Component hoverText = Config.deserializeWithPlaceholders(
                "<green>玩家: </green><white>" + playerName + "</white>\n" +
                "<green>原因: </green><white>" + reason + "</white>\n" +
                "<green>时长: </green><white>" + timeText + "</white>\n" +
                "<green>执行者: </green><white>" + source + "</white>\n" +
                "<yellow>✦ 点击复制玩家名</yellow>"
        );

        // 创建可点击的条目
        Component entry_component = Config.deserializeWithPlaceholders(
                "<dark_gray>" + index + ".</dark_gray> <white><bold>" + playerName + "</bold></white> " +
                "<dark_gray>│</dark_gray> <yellow>" + reason + "</yellow> " +
                "<dark_gray>│</dark_gray> <gray>" + timeText + "</gray>"
        )
        .hoverEvent(HoverEvent.showText(hoverText))
        .clickEvent(ClickEvent.copyToClipboard(playerName));

        sender.sendMessage(entry_component);
    }

    /**
     * 发送分页导航
     */
    private static void sendPageNavigation(CommandSender sender, int currentPage, int totalPages) {
        Component navigation = Component.empty();

        // 上一页按钮
        if (currentPage > 1) {
            Component prevButton = Config.deserializeWithPlaceholders("<green><bold>« 上一页</bold></green>")
                    .hoverEvent(HoverEvent.showText(Config.deserializeWithPlaceholders("<green>点击查看第 " + (currentPage - 1) + " 页</green>")))
                    .clickEvent(ClickEvent.runCommand("/banlist " + (currentPage - 1)));
            navigation = navigation.append(prevButton);
        } else {
            navigation = navigation.append(Config.deserializeWithPlaceholders("<dark_gray><strikethrough>« 上一页</strikethrough></dark_gray>"));
        }

        navigation = navigation.append(Config.deserializeWithPlaceholders("  <dark_gray>|</dark_gray>  "));

        // 页码显示
        navigation = navigation.append(Config.deserializeWithPlaceholders(
                "<white>第 </white><white><bold>" + currentPage + "</bold></white><white> / </white><gray>" + totalPages + "</gray><white> 页</white>"
        ));

        navigation = navigation.append(Config.deserializeWithPlaceholders("  <dark_gray>|</dark_gray>  "));

        // 下一页按钮
        if (currentPage < totalPages) {
            Component nextButton = Config.deserializeWithPlaceholders("<green><bold>下一页 »</bold></green>")
                    .hoverEvent(HoverEvent.showText(Config.deserializeWithPlaceholders("<green>点击查看第 " + (currentPage + 1) + " 页</green>")))
                    .clickEvent(ClickEvent.runCommand("/banlist " + (currentPage + 1)));
            navigation = navigation.append(nextButton);
        } else {
            navigation = navigation.append(Config.deserializeWithPlaceholders("<dark_gray><strikethrough>下一页 »</strikethrough></dark_gray>"));
        }

        sender.sendMessage(navigation);
    }
}
