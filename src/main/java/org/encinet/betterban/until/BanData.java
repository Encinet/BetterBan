package org.encinet.betterban.until;

import org.bukkit.OfflinePlayer;

public record BanData(OfflinePlayer player, Long ms, String reason) {
};