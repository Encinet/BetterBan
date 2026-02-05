package org.encinet.betterban.util;

public class Tool {
    /**
     * 检查字符串是否为纯数字
     * @param str 待检查的字符串
     * @return 如果是纯数字返回 true，否则返回 false
     */
    public static boolean isNum(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
