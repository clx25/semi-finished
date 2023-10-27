package com.semifinished.util;


import com.semifinished.cache.SemiCache;
import com.semifinished.exception.CodeException;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.pojo.ValueCondition;
import org.springframework.util.StringUtils;

import java.util.Arrays;

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
        char[] chars = key.toCharArray();
        Assert.isTrue(chars.length == 0, () -> new ParamsException("key长度不能为0"));

        boolean eq = true;
        boolean and = true;

        if (chars[0] == '!') {
            eq = false;
        } else if (chars[0] == '|') {
            and = false;
        }
        if (chars.length > 1 && (eq ^ and)) {
            if (chars[1] == '!') {
                eq = false;
            } else if (chars[1] == '|') {
                and = false;
            }
        }
        if (eq ^ and) {
            key = key.substring(1);
        } else if (!eq) {
            key = key.substring(2);
        }
        Assert.isTrue(key.length() == 0, () -> new ParamsException("key长度不能为0"));

        return ValueCondition.builder().table(table).column(key).combination(and ? " and " : " or ").conditionBoolean(eq).build();
    }

    public static void addColumn(SqlDefinition sqlDefinition, SemiCache semiCache, String table, String columns) {
        Assert.hasNotText(table, () -> new CodeException("传入的table不能为空"));
        if (!StringUtils.hasText(columns)) {
            return;
        }
        String[] values = columns.split(",");

        String[] fieldArray = Arrays.stream(values).map(field -> {
            String[] fields = field.split(":");
            sqlDefinition.addColumn(table, fields[0], fields.length == 2 ? fields[1] : "");
            return fields[0];
        }).toArray(String[]::new);

        TableUtils.validColumnsName(semiCache, sqlDefinition, table, fieldArray);
    }


}
