package org.encinet.betterban;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Unit {
    public static List<String> getOnlinePlayers() {
        List<String> list = new ArrayList<>();
        for (Player n : Bukkit.getOnlinePlayers()) {
            list.add(n.getName());
        }
        return list;
    }
}
