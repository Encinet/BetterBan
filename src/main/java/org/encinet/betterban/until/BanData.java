package org.encinet.betterban.until;

import java.util.Date;

import org.bukkit.OfflinePlayer;

public record BanData(OfflinePlayer player, Long ms, String reason) {
    public void ban(String senderName) {
        if (ms == 0) {
            player.banPlayer(reason, senderName);// 永封
        } else {
            Date date = new Date(ms);
            player.banPlayer(reason, date, senderName);
        }
    }
};