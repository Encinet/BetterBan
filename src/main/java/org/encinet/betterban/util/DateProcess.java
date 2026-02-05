package org.encinet.betterban.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateProcess {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    // 时间单位常量
    private static final long SECOND_MS = 1000L;
    private static final long MINUTE_MS = 60000L;
    private static final long HOUR_MS = 3600000L;
    private static final long DAY_MS = 86400000L;

    /**
     * @param text l:2000/1/1 d:1s
     * @return 封禁结束的毫秒数
     */
    public static Long getData(String text) {
        if (text.startsWith("d:")) {
            // d:2022/8/16 - 指定日期
            String dateStr = text.substring(2);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                Date date = sdf.parse(dateStr);
                return date.getTime();
            } catch (ParseException e) {
                throw new IllegalArgumentException("无效的日期格式: " + dateStr, e);
            }
        } else if (text.startsWith("l:")) {
            // l:1d - 相对时长
            if (text.length() < 4) {
                throw new IllegalArgumentException("无效的时长格式: " + text);
            }

            String numStr = text.substring(2, text.length() - 1);
            String unit = text.substring(text.length() - 1);

            try {
                long duration = Long.parseLong(numStr);
                long currentTime = System.currentTimeMillis();

                return switch (unit) {
                    case "s" -> currentTime + (duration * SECOND_MS);
                    case "m" -> currentTime + (duration * MINUTE_MS);
                    case "h" -> currentTime + (duration * HOUR_MS);
                    case "d" -> currentTime + (duration * DAY_MS);
                    default -> throw new IllegalArgumentException("无效的时间单位: " + unit);
                };
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("无效的数字: " + numStr, e);
            }
        } else if ("forever".equals(text)) {
            return 0L;
        }

        throw new IllegalArgumentException("无效的时间格式: " + text);
    }

    /**
     * @param time 毫秒数
     * @return 时间可读文本
     */
    public static String getDataText(Long time) {
        if (time == null || time == 0) {
            return "永久";
        }
        return DISPLAY_FORMATTER.format(Instant.ofEpochMilli(time));
    }

    /**
     * @return 现在的 年/月/日
     */
    public static String getNowTime() {
        return DATE_FORMATTER.format(Instant.now());
    }
}
