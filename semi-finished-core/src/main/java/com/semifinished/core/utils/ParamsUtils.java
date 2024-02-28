package com.semifinished.core.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 对接收的参数进行处理的工具类
 */
public class ParamsUtils {

    /**
     * 把字符串从驼峰转蛇形
     *
     * @param str 要转换的字符串
     * @return 转换后的字符串
     */
    public static String snakeCase(String str) {
        return !StringUtils.hasText(str) ? str : str.replaceAll("[A-Z]", "_$0").toLowerCase();
    }

    /**
     * 判断是否合法名字，以英文字母开头，后续为英文字母，数字，下划线
     *
     * @param str 名称
     * @return true为符合标准的名称
     */
    public static boolean isLegalName(String str) {
        if (!StringUtils.hasText(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        if (!isLetter(chars[0])) {
            return false;
        }
        for (int i = 1; i < chars.length; i++) {
            if (!isLetterOrDigitOrUnderline(chars[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 合法的表字段名或别名，英文单词开头，后续为单词或下划线
     *
     * @param column 字段名或别名
     * @return true为符合规则
     */
    public static boolean isLegalColumn(String column) {
        char[] chars = column.toCharArray();
        if (!isLetter(chars[0])) {
            return false;
        }
        for (int i = 1; i < chars.length; i++) {
            if (!(isLetter(chars[i]) || chars[i] == '_')) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLetterOrDigitOrUnderline(char c) {
        return isLetter(c) || (48 <= c && c <= 57) || c == 95;
    }

    public static boolean isLetter(char c) {
        return (65 <= c && c <= 90) || (97 <= c && c <= 122);
    }

    public static Set<String> fields(List<ObjectNode> objectNodes) {
        Set<String> fields = new HashSet<>();

        for (ObjectNode objectNode : objectNodes) {
            Iterator<String> names = objectNode.fieldNames();
            while (names.hasNext()) {
                fields.add(names.next());
            }
        }

        return fields;
    }

    public static Set<String> fields(ObjectNode objectNode) {
        Set<String> fields = new HashSet<>();

        Iterator<String> names = objectNode.fieldNames();
        while (names.hasNext()) {
            fields.add(names.next());
        }

        return fields;
    }

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * 判断字符串是否为整数
     *
     * @param s 需要判断的字符串
     * @return 返回true表示是整数，返回false表示不是整数
     */
    public static boolean isInteger(String s) {
        return isNumber(s) && !s.contains(".");
    }


    /**
     * 判断是否为数字，包含了负数和非负数
     *
     * @param s 需要判断的字符串
     * @return 返回true表示是数字，返回false表示不是数字
     */
    public static boolean isNumber(String s) {
        return statement(s, 1);
    }

    /**
     * 判断字符串是否为非负数
     *
     * @param s 需要判断的字符串
     * @return 返回true表示非负数，返回false表示不是数字或不是非负数
     */
    public static boolean isNonNegativeNumber(String s) {
        return statement(s, 2);
    }

    /**
     * 判断字符串是否为非负整数
     *
     * @param s 需要判断的字符串
     * @return 返回true表示非负整数，返回false表示不是数字或不是非负整数
     */

    public static boolean isNonNegativeIntegerNumber(String s) {
        return statement(s, 3);
    }


    /**
     * 根据传入的type执行判断逻辑
     *
     * @param s    需要检测的字符串
     * @param type 1是判断是否为数字，2是判断是否为非负数，3是判断是否为非负整数,4是判断是否整数
     * @return 返回true表示符合type的类型，返回false表示不符合type的类型
     */
    public static boolean statement(String s, int type) {
        if (s == null || s.length() == 0) {
            return false;
        }
        if (s.startsWith("-") && (type == 1 || type == 4)) {
            s = s.substring(1);
        }
        int sz = s.length();

        if (!(Character.isDigit(s.charAt(0)) && Character.isDigit(s.charAt(sz - 1)))) {
            return false;
        }
        int point = 0;
        for (int i = 1; i < sz - 1; i++) {

            if ((type == 1 || type == 2) && '.' == s.charAt(i)) {
                if (point++ > 0) {
                    return false;
                }
                continue;
            }
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static <T> List<T> asList(T... arr) {
        return new ArrayList<>(Arrays.asList(arr));
    }

    public static boolean isEmpty(ObjectNode params) {
        return params == null || params.isEmpty();
    }

    public static Map<String, String> toMap(ObjectNode params) {
        Map<String, String> value = new HashMap<>();
        params.fields().forEachRemaining(entry -> {
            value.put(entry.getKey(), entry.getValue().asText());
        });
        return value;
    }

    public static String hasText(String text, String orFalse) {
        return StringUtils.hasText(text) ? text : orFalse;
    }


}
