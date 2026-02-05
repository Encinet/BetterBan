package org.encinet.betterban.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.Component;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.encinet.betterban.Config;
import org.encinet.betterban.util.DateProcess;

import java.util.Date;

/**
 * 封禁监听器
 * 在玩家登录时和被踢出时应用封禁模板
 */
public class BanListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        // 直接检查玩家是否被封禁，不要检查 loginResult
        PlayerProfile profile = event.getPlayerProfile();

        BanEntry<PlayerProfile> banEntry = Bukkit.getBanList(BanListType.PROFILE)
                .getBanEntry(profile);

        if (banEntry == null) {
            return;  // 玩家没有被封禁，允许登录
        }

        // 玩家被封禁，设置自定义消息
        String originalReason = banEntry.getReason();
        Date expiration = banEntry.getExpiration();

        String timeText = (expiration == null) ? "永久" : DateProcess.getDataText(expiration.getTime());

        Component formattedMessage = Config.deserializeWithPlaceholders(
                "\n" + Config.banReason,
                "%reason%", originalReason,
                "%time%", timeText
        );

        // 设置封禁消息
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, formattedMessage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent event) {
        // 检查是否因为被ban而踢出
        if (!event.getPlayer().isBanned()) {
            return;
        }

        BanEntry<PlayerProfile> banEntry = Bukkit.getBanList(BanListType.PROFILE)
                .getBanEntry(event.getPlayer().getPlayerProfile());

        if (banEntry == null) {
            return;
        }

        String originalReason = banEntry.getReason();
        Date expiration = banEntry.getExpiration();

        // 计算剩余时间文本
        String timeText = (expiration == null) ? "永久" : DateProcess.getDataText(expiration.getTime());

        // 应用封禁模板并解析MiniMessage格式
        Component formattedMessage = Config.deserializeWithPlaceholders(
                "\n" + Config.banReason,
                "%reason%", originalReason,
                "%time%", timeText
        );

        // 更新踢出消息
        event.leaveMessage(Component.empty()); // 清空离开消息
        event.reason(formattedMessage);
    }
}
