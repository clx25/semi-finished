package com.semifinished.core.utils.bean;


import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.util.IdGenerator;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TableUtils {
    private final SemiCache semiCache;
    private final IdGenerator idGenerator;

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
     * 校验 表名和字段名
     *
     * @param sqlDefinition SQL定义数据
     * @param table         校验的表名
     * @param columns       校验的字段数组
     */
    public void validColumnsName(SqlDefinition sqlDefinition, String table, String... columns) {
        validColumnsName(sqlDefinition, table, Arrays.asList(columns));
    }

    /**
     * 校验 表名和字段名
     *
     * @param sqlDefinition SQL定义数据
     * @param table         校验的表名
     * @param columns       校验的字段集合
     */
    public void validColumnsName(SqlDefinition sqlDefinition, String table, Collection<String> columns) {
        if (columns == null || columns.isEmpty()) {
            Assert.isFalse(validTableName(sqlDefinition.getDataSource(), table), () -> new ParamsException(table + "参数错误"));
            return;
        }
        List<Column> tableColumnsName = getColumns(sqlDefinition.getDataSource(), table);
        boolean sub = false;
        if (tableColumnsName.isEmpty()) {
            //如果是子查询，校验字段名称应该判断是否是子查询返回的字段
            SqlDefinition subTable = sqlDefinition.getSubTable();
            if (subTable != null) {
                tableColumnsName = QuerySqlCombiner.queryColumns(subTable);
                sub = true;
            }
        }

        Assert.isEmpty(tableColumnsName, () -> new ParamsException(table + "参数错误"));
        List<String> columnsName = tableColumnsName.stream().map(column -> ParamsUtils.hasText(column.getAlias(), column.getColumn())).collect(Collectors.toList());
        boolean finSub = sub;
        for (String column : columns) {
            Assert.hasNotText(column, () -> new ParamsException("字段名不能为空"));
            boolean flag = columnsName.stream()
                    .anyMatch(col -> col.equals(column));
            Assert.isFalse(flag, () -> new ParamsException("参数错误" + (finSub ? ",子查询外层应该使用内层返回的字段名" : "") + "：" + column));
        }


    }

    /**
     * 通过程序添加的字段为了避免与已经有的字段或别名重名，所以稍做处理,生成一个唯一的别名
     *
     * @param prefix 别名前缀，用于定位和区分别名
     * @return 处理后的别名
     */
    public String uniqueAlias(String prefix) {
        return (prefix == null ? "" : (prefix + "_")) + idGenerator.getId();
    }

    /**
     * 判断表是否存在
     *
     * @param table 表名
     * @return true表示存在，false表示不存在
     */
    public boolean validTableName(String dataSource, String table) {
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
     * @param table 表名
     * @return 字段名列表
     */
    public List<String> getColumnNames(String dataSource, String table) {
        return getColumns(dataSource, table).stream()
                .map(Column::getColumn)
                .collect(Collectors.toList());
    }

    /**
     * 获取表对应的字段信息
     *
     * @param table 表名
     * @return 字段信息
     */
    public List<Column> getColumns(String dataSource, String table) {
        List<Column> columns = semiCache.getValue(CoreCacheKey.COLUMNS.getKey() + dataSource);

        if (columns == null || columns.isEmpty()) {
            return Collections.emptyList();
        }

        return columns.stream()
                .filter(column -> table.equals(column.getTable()))
                .collect(Collectors.toList());
    }


}
