package com.semifinished.util;


import com.semifinished.cache.SemiCache;
import com.semifinished.cache.CoreCacheKey;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.util.IdGenerator;
import com.semifinished.pojo.Column;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TableUtils {

    /**
     * 校验 表名和字段名
     *
     * @param semiCache     缓存
     * @param sqlDefinition SQL定义数据
     * @param table         校验的表名
     * @param columns       校验的字段集合
     */
    public static void validColumnsName(SemiCache semiCache, SqlDefinition sqlDefinition, String table, List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            Assert.isFalse(validTableName(semiCache, sqlDefinition.getDataSource(), table), () -> new ParamsException(table + "参数错误"));
            return;
        }
        List<Column> tableColumnsName = getColumns(semiCache, sqlDefinition, table);
        Assert.isEmpty(tableColumnsName, () -> new ParamsException(table + "参数错误"));
        List<String> columnsName = tableColumnsName.stream().map(column -> ParamsUtils.hasText(column.getAlias(), column.getColumn())).collect(Collectors.toList());
        for (String column : columns) {
            Assert.hasNotText(column, () -> new ParamsException("字段名不能为空"));
            boolean flag = columnsName.stream()
                    .anyMatch(col -> col.equals(column));
            Assert.isFalse(flag, () -> new ParamsException(column + "参数错误"));
        }
    }

    /**
     * 校验 表名和字段名
     *
     * @param semiCache     缓存
     * @param sqlDefinition SQL定义数据
     * @param table         校验的表名
     * @param columns       校验的字段数组
     */
    public static void validColumnsName(SemiCache semiCache, SqlDefinition sqlDefinition, String table, String... columns) {
        validColumnsName(semiCache, sqlDefinition, table, Arrays.asList(columns));
    }

    /**
     * 获取表对应的所有字段
     *
     * @param semiCache     缓存
     * @param sqlDefinition SQL定义信息
     * @param table         表名
     * @return 表名对应的字段
     */
    public static List<Column> getColumns(SemiCache semiCache, SqlDefinition sqlDefinition, String table) {
        List<Column> tableColumnsName = getColumns(semiCache, sqlDefinition.getDataSource(), table);
        if (tableColumnsName.isEmpty()) {
            //在缓存中没有表名对应的字段的情况下，考虑是不是子查询，对应的字段就是子查询的返回字段
            String tb = sqlDefinition.getTable();
            if (!table.equals(tb)) {
                return tableColumnsName;
            }
            tableColumnsName = sqlDefinition.getSubTable().getColumns();
        }
        return tableColumnsName;
    }


    /**
     * 通过程序添加的字段为了避免与已经有的字段或别名重名，所以稍做处理,生成一个唯一的别名
     *
     * @param prefix 别名前缀，用于定位和区分别名
     * @return 处理后的别名
     */
    public static String uniqueAlias(IdGenerator idGenerator, String prefix) {
        return (prefix == null ? "" : (prefix + "_")) + idGenerator.getId();
    }

    public static String uniqueAlias(IdGenerator idGenerator) {
        return String.valueOf(idGenerator.getId());
    }

    /**
     * 获取非空数据
     *
     * @param defaultVal 默认值
     * @param supplier   如果默认值为空，获取数据的方法
     * @return 默认数据或从提供者中获取的数据
     */
    public static String getNotEmpty(String defaultVal, Supplier<String> supplier) {
        return StringUtils.hasText(defaultVal) ? defaultVal : supplier.get();
    }

    /**
     * 判断表是否存在
     *
     * @param semiCache 缓存
     * @param table     表名
     * @return true表示存在，false表示不存在
     */
    public static boolean validTableName(SemiCache semiCache, String dataSource, String table) {
        if (!StringUtils.hasText(table)) {
            return false;
        }

        List<Column> columns = semiCache.getValue(CoreCacheKey.COLUMNS.getKey() + dataSource);
        Assert.isEmpty(columns, () -> new ParamsException("数据源" + dataSource + "不存在"));

        return columns.stream().anyMatch(column -> table.equals(column.getTable()));
    }

    /**
     * 获取表对应的字段名列表
     *
     * @param semiCache 缓存
     * @param table     表名
     * @return 字段名列表
     */
    public static List<String> getColumnNames(SemiCache semiCache, String dataSource, String table) {
        return getColumns(semiCache, dataSource, table).stream()
                .map(Column::getColumn)
                .collect(Collectors.toList());
    }

    /**
     * 获取表对应的字段信息
     *
     * @param semiCache 缓存
     * @param table     表名
     * @return 字段信息
     */
    public static List<Column> getColumns(SemiCache semiCache, String dataSource, String table) {
        List<Column> columns = semiCache.getValue(CoreCacheKey.COLUMNS.getKey() + dataSource);

        if (columns == null || columns.isEmpty()) {
            return Collections.emptyList();
        }

        return columns.stream()
                .filter(column -> table.equals(column.getTable()))
                .collect(Collectors.toList());
    }



}
