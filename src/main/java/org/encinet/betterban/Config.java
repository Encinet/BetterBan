package org.encinet.betterban;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置管理类
 * 使用 MiniMessage 格式处理文本
 */
public class Config {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static FileConfiguration config;

    public static Component prefix;
    public static boolean snEnable;
    public static String snText;
    public static List<Component> help;
    public static String banReason;

    /**
     * 加载配置
     * @param plugin 插件实例
     */
    public static void load(BetterBan plugin) {
        config = plugin.getConfig();

        prefix = deserialize("prefix", "<bold><gold>Better</gold><white>Ban</white></bold> <dark_gray>>></dark_gray> ");
        snEnable = config.getBoolean("Sentence-notice.enable", true);
        snText = config.getString("Sentence-notice.text", "<red>%executor% 封禁了 %executed%，原因: %reason%，时长: %time%</red>");
        help = deserializeList("help");
        banReason = config.getString("reason", "你已被封禁\n原因: %reason%\n时间: %time%");

        plugin.getLogger().info("配置加载完成");
    }

    /**
     * 反序列化 MiniMessage 字符串为 Component
     */
    private static Component deserialize(String path, String defaultValue) {
        String value = config.getString(path, defaultValue);
        return MINI_MESSAGE.deserialize(value);
    }

    /**
     * 反序列化 MiniMessage 字符串列表为 Component 列表
     */
    private static List<Component> deserializeList(String path) {
        List<Component> result = new ArrayList<>();
        List<String> rawList = config.getStringList(path);

        if (rawList.isEmpty()) {
            // 提供默认帮助信息
            result.add(MINI_MESSAGE.deserialize("<gold>=== BetterBan 帮助 ===</gold>"));
            result.add(MINI_MESSAGE.deserialize("<yellow>/bb <玩家> <时间> <原因></yellow> <gray>- 封禁玩家</gray>"));
            result.add(MINI_MESSAGE.deserialize("<yellow>/bb --confirm</yellow> <gray>- 确认封禁</gray>"));
            result.add(MINI_MESSAGE.deserialize("<gray>时间格式: d:2024/01/01 或 l:1d (s/m/h/d) 或 forever</gray>"));
        } else {
            for (String line : rawList) {
                result.add(MINI_MESSAGE.deserialize(line));
            }
        }

        return result;
    }

    /**
     * 解析带占位符的 MiniMessage 文本
     */
    public static Component deserializeWithPlaceholders(String template, String... replacements) {
        String text = template;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                text = text.replace(replacements[i], replacements[i + 1]);
            }
        }
        return MINI_MESSAGE.deserialize(text);
    }
}
