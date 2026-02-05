package org.encinet.betterban;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.encinet.betterban.command.Ban;
import org.encinet.betterban.command.BanList;
import org.encinet.betterban.listener.BanListener;

import java.util.List;
import java.util.logging.Logger;

/**
 * BetterBan 主类
 * 一个现代化的 Minecraft 封禁插件
 */
public final class BetterBan extends JavaPlugin {
    public static Logger logger;

    @Override
    public void onEnable() {
        logger = this.getLogger();
        logger.info("BetterBan 正在启动...");

        // 保存默认配置
        saveDefaultConfig();

        // 加载配置
        Config.load(this);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new BanListener(), this);
        logger.info("已注册事件监听器");

        // 注册 Brigadier 命令
        LifecycleEventManager<Plugin> lifecycleManager = this.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            // 注册 ban 命令（覆盖原版），别名 bb
            commands.register(Ban.create(), "BetterBan 封禁命令", List.of("bb"));

            // 注册 banlist 命令，别名 bblist
            commands.register(BanList.create(), "BetterBan 封禁列表命令", List.of("bblist"));

            logger.info("已注册 Brigadier 命令 (ban, bb, banlist, bblist)");
            logger.info("已覆盖原版 ban 和 ban-list 命令");
        });

        logger.info("BetterBan 启动完成！");
    }

    @Override
    public void onDisable() {
        logger.info("BetterBan 已关闭");
    }
}
