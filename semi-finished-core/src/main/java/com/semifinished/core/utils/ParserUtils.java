package com.semifinished.core.utils;


import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ValueCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParserUtils {


    /**
     * 解析查询规则的通用规则，并创建where查询的实体类<code>ColumnValue</code>
     * <code>!</code>表示非，如 !=, not null, not in,<code>|</code>表示或，表示SQL中的连接符 or
     *
     * @param table 表名
     * @param key   请求参数的key
     * @return 包含解析后通用规则的where条件实体类
     */
    public static ValueCondition columnValue(String table, String key) {
        char[] chars = key.trim().toCharArray();
        Assert.isFalse(chars.length == 0, () -> new ParamsException("key长度不能为0"));

        boolean eq = true;
        boolean and = true;
        boolean disabled = false;

        for (int index = 0; index < 3; index++) {
            if (chars.length > index) {
                if (chars[index] == '!') {
                    eq = false;
                } else if (chars[index] == '|') {
                    and = false;
                } else if (chars[index] == '~') {
                    disabled = true;
                }
            }
        }

        int begin = (eq ? 0 : 1) + (and ? 0 : 1) + (disabled ? 1 : 0);

        key = key.substring(begin);

        Assert.isFalse(key.length() == 0, () -> new ParamsException("key长度不能为0"));

        return ValueCondition.builder()
                .table(table)
                .column(key)
                .combination(and ? " and " : " or ")
                .conditionBoolean(eq)
                .disabled(disabled)
                .build();
    }

    /**
     * 匹配状态，任意一个匹配就返回true
     *
     * @param sqlDefinition sql定义信息
     * @param parserStatus  需要匹配的所有状态
     * @return 返回true表示至少有一个匹配，返回false表示所有都不匹配
     */
    public static boolean statusAnyMatch(SqlDefinition sqlDefinition, ParserStatus... parserStatus) {
        return Arrays.stream(parserStatus)
                .anyMatch(status -> status.getStatus() == sqlDefinition.getStatus());

    }

    public static <T> List<T> asList(T t) {
        return t == null ? Collections.emptyList() : Collections.singletonList(t);
    }

}
