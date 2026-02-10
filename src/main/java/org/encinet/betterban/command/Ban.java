package org.encinet.betterban.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.encinet.betterban.BetterBan;
import org.encinet.betterban.Config;
import org.encinet.betterban.util.DateProcess;
import org.encinet.betterban.util.Tool;

import java.time.Instant;
import java.util.List;

public class Ban {
    private static final List<String> TIME_UNITS = List.of("s", "m", "h", "d");

    public static LiteralCommandNode<CommandSourceStack> create() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("ban")
                .requires(source -> source.getSender().hasPermission("bb.admin"))
                .executes(ctx -> {
                    sendHelp(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                });

        // /ban <player> <args> - 支持在线和离线玩家
        command.then(Commands.argument("player", StringArgumentType.word())
                .suggests(playerSuggestions())
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .suggests(timeSuggestions())
                        .executes(Ban::executeBan)));

        return command.build();
    }

    private static int executeBan(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        try {
            String playerName = ctx.getArgument("player", String.class);
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

            if (player.isBanned()) {
                sender.sendMessage(Config.prefix.append(
                        Config.deserializeWithPlaceholders("<red>此玩家已处于封禁状态</red>")
                ));
                return 0;
            }

            // 手动解析 args: <time> <reason>
            String args = ctx.getArgument("args", String.class);
            int firstSpace = args.indexOf(' ');

            if (firstSpace == -1) {
                sender.sendMessage(Config.prefix.append(
                        Config.deserializeWithPlaceholders("<red>请提供封禁原因</red>")
                ));
                return 0;
            }

            String timeStr = args.substring(0, firstSpace);
            String reason = args.substring(firstSpace + 1);

            // 检查是否封禁自己
            if (sender instanceof Player && ((Player) sender).getUniqueId().equals(player.getUniqueId())) {
                sender.sendMessage(Config.prefix.append(
                        Config.deserializeWithPlaceholders("<red>你不能封禁自己</red>")
                ));
                return 0;
            }

            // 检查 reason 中是否包含 --confirm
            boolean confirmed = false;
            if (reason.contains("--confirm")) {
                confirmed = true;
                // 移除 --confirm 并清理多余空格
                reason = reason.replace("--confirm", "").trim().replaceAll("\\s+", " ");
            }

            long ms = DateProcess.getData(timeStr);

            // 检查 d: 格式的日期是否已过期
            if (timeStr.startsWith("d:") && ms != 0 && ms < System.currentTimeMillis()) {
                sender.sendMessage(Config.prefix.append(
                        Config.deserializeWithPlaceholders("<red>封禁日期不能是过去的时间</red>")
                ));
                return 0;
            }

            if (player.hasPlayedBefore()) {
                executeBan(sender, player, reason, ms);
            } else if (confirmed) {
                executeBan(sender, player, reason, ms);
            } else {
                sender.sendMessage(Config.prefix.append(
                        Config.deserializeWithPlaceholders("<yellow>此玩家尚未进服，如需封禁请在原因中添加 <white>--confirm</white></yellow>")
                ));
                return 0;
            }
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Config.prefix.append(
                    Config.deserializeWithPlaceholders("<red>" + e.getMessage() + "</red>")
            ));
            return 0;
        } catch (Exception e) {
            sender.sendMessage(Config.prefix.append(
                    Config.deserializeWithPlaceholders("<red>执行封禁时发生错误，请检查命令格式</red>")
            ));
            BetterBan.logger.warning("封禁命令执行失败: " + e.getMessage());
            return 0;
        }
    }

    private static SuggestionProvider<CommandSourceStack> playerSuggestions() {
        return (_, builder) -> {
            String input = builder.getRemaining().toLowerCase();
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    private static SuggestionProvider<CommandSourceStack> timeSuggestions() {
        return (_, builder) -> {
            String input = builder.getRemaining();
            String currentDate = "d:" + DateProcess.getNowTime();

            if (input.isEmpty()) {
                builder.suggest(currentDate);
                builder.suggest("forever");
                builder.suggest("l:1s");
            } else if (input.startsWith("d")) {
                builder.suggest(currentDate);
            } else if (input.startsWith("l:") && input.length() > 2) {
                suggestDurationFormats(builder, input);
            } else if (input.startsWith("l")) {
                TIME_UNITS.forEach(unit -> builder.suggest("l:1" + unit));
            } else {
                builder.suggest(currentDate);
                builder.suggest("forever");
                builder.suggest("l:1s");
            }
            return builder.buildFuture();
        };
    }

    private static void suggestDurationFormats(com.mojang.brigadier.suggestion.SuggestionsBuilder builder, String input) {
        String sub = input.substring(2);
        int subLength = sub.length();

        if (Tool.isNum(sub)) {
            // 纯数字，建议添加单位
            TIME_UNITS.forEach(unit -> builder.suggest("l:" + sub + unit));
        } else if (subLength > 0) {
            // 检查是否已有单位
            String lastChar = sub.substring(subLength - 1);
            String numPart = sub.substring(0, subLength - 1);
            if (TIME_UNITS.contains(lastChar) && Tool.isNum(numPart)) {
                builder.suggest(input);
            }
        }
    }

    private static void sendHelp(CommandSender sender) {
        Config.help.forEach(sender::sendMessage);
    }

    /**
     * 公开处刑通知
     */
    public static void sentenceNotice(String executor, String executed, String time, String reason) {
        if (Config.snEnable) {
            Component message = Config.deserializeWithPlaceholders(
                    Config.snText,
                    "%executor%", executor,
                    "%executed%", executed,
                    "%time%", time,
                    "%reason%", reason
            );
            Bukkit.broadcast(message);
        }
    }

    /**
     * 执行封禁
     */
    public static void executeBan(CommandSender sender, OfflinePlayer player, String reason, Long ms) {
        String senderName = sender.getName();
        String playerName = player.getName();
        String timeText = DateProcess.getDataText(ms);

        // 执行ban，保存原始原因
        // 如果玩家在线，ban() 会自动踢出，PlayerKickEvent 监听器会应用模板
        Instant expiration = (ms == 0) ? null : Instant.ofEpochMilli(ms);
        player.ban(reason, expiration, senderName);

        sender.sendMessage(Config.prefix.append(
                Config.deserializeWithPlaceholders("<green>成功封禁玩家 <white>" + playerName + "</white></green>")
        ));
        sentenceNotice(senderName, playerName, timeText, reason);
    }
}
