package org.encinet.betterban.until;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateProcess {
    private static SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd");

    /**
     * @param text l:2000/1/1 d:1s
     * @return 封禁结束的毫秒数
     */
    public static Long getData(String text) {
        // d:2022/8/16
        // l:1d
        if (text.startsWith("d:")) {
            text = text.substring(2);
            Date date;
            try {
                date = ft.parse(text);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return date.getTime();
        } else if (text.startsWith("l:")) {
            // s秒 m分 h小时 d天
            long l = Long.parseLong(text.substring(2, text.length() - 1));
            switch (text.substring(text.length() - 1)) {
                case "s":
                    return System.currentTimeMillis() + (l * 1000);
                case "m":
                    return System.currentTimeMillis() + (l * 60000);
                case "h":
                    return System.currentTimeMillis() + (l * 3600000);
                case "d":
                    return System.currentTimeMillis() + (l * 86400000);
            }
        } else if ("forever".equals(text)) {
            return (long) 0;
        }
        return (long) 0;
    }

    /**
     * @param time 毫秒数
     * @return 时间可读文本
     */
    public static String getDataText(Long time) {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return (time == 0) ? "永封" : ft.format(time);
    }

    /**
     * @return 现在的 年/月/日
     */
    public static String getNowTime() {
        return ft.format(new Date());
    }
}
