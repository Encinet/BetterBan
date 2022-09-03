package org.encinet.betterban;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

enum TabComplete {
    PLAYER(Unit.getOnlinePlayers(), 0, null, new int[] { 1 }),
    DATA(Arrays.asList("d:2000/1/1", "forever", "l:1s", "l:1m", "l:1h", "l:1d"), 1, null, new int[] { 2 }),
    REASON(Arrays.asList("[reason]"), 2, null, new int[] { 3 }),
    ;

    private final List<String> list;// 返回的List
    private final int[] num;// 这个参数可以出现的位置
    private final int befPos;// 识别的上个参数的位置
    private final String bef;// 上个参数的内容

    TabComplete(List<String> list, int befPos, String bef, int[] num) {
        this.list = list;
        this.befPos = befPos;
        this.bef = bef;
        this.num = num.clone();
    }

    public int[] getNum() {
        return num;
    }

    public String getBef() {
        return bef;
    }

    public List<String> getList() {
        return list;
    }

    public int getBefPos() {
        return befPos;
    }

    public static List<String> returnList(String[] Para, int curNum, CommandSender sender) {
        for (TabComplete tab : TabComplete.values()) {
            if (tab.getBefPos() - 1 >= Para.length) {
                continue;
            }
            if ((tab.getBef() == null || tab.getBef().equalsIgnoreCase(Para[tab.getBefPos() - 1]))
                    && Arrays.binarySearch(tab.getNum(), curNum) >= 0) {
                List<String> list = new ArrayList<>();
                if (!(Para[tab.getNum()[0] - 1] == null)) { // 判断是否已经存在参数
                    int length = Para[tab.getNum()[0] - 1].length(); // 如果有就计算长度
                    String abc = Para[tab.getNum()[0] - 1]; // 存储刚刚的参数
                    for (String s : tab.getList()) {// 遍历所有的返回值
                        if (s.regionMatches(true, 0, abc, 0, length))
                            list.add(s); // 比对length长度的相似参数，并加入list
                    }
                    return list; // 返回相似的参数
                } else {
                    return tab.getList();
                }
            }
        }
        return null;
    }
}