package com.semifinished.core.listener;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.DataSourceConfig;
import com.semifinished.core.config.DataSourceProperties;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 获取数据库表字段数据
 */
@Slf4j
@Component
@Order(-1000)
@AllArgsConstructor
public class ColumnListener implements ApplicationListener<RefreshCacheApplication> {
    private final SemiCache semiCache;
    private final SqlExecutorHolder sqlExecutorHolder;
    private final DataSourceProperties dataSourceProperties;

    /**
     * 对semi_column表中的数据进行梳理
     * 把项目的表结构保存到缓存中
     *
     * @param event ContextRefreshedEvent
     */
    @Override
    public void onApplicationEvent(RefreshCacheApplication event) {

        Map<String, DataSourceConfig> dataSource = dataSourceProperties.getDataSource();
        sqlExecutorHolder.getSqlExecutorMap().forEach((dataSourceName, executor) -> {

            ObjectNode objectNode = executor.get("select database() db");
            String db = objectNode.get("db").asText();

            String databaseProductName = executor.getDatabaseProductName();
            log.info("数据库产品名称{}",databaseProductName);
            List<Column> tables;
            if ("H2".equalsIgnoreCase(databaseProductName)) {
                tables = executor.list("SELECT  TABLE_NAME `table`,COLUMN_NAME `column`,TYPE_NAME  `type` ,CASE WHEN IS_NULLABLE = 'YES' THEN TRUE ELSE FALSE END AS NULL_ABLE FROM INFORMATION_SCHEMA.COLUMNS  WHERE  TABLE_SCHEMA = 'PUBLIC'", Column.class);
            } else if ("mysql".equalsIgnoreCase(databaseProductName)) {
                tables = executor.list("SELECT '" + db + "', col.TABLE_NAME `table`,col.COLUMN_NAME `column`,col.COLUMN_TYPE type,if(IS_NULLABLE='YES',true,false) null_able FROM information_schema.`COLUMNS` col  WHERE TABLE_SCHEMA='" + db + "'", Column.class);
            } else {
                throw new CodeException("无法识别的数据库");
            }


            DataSourceConfig config = dataSource.get(dataSourceName);
            validExcludes(tables, config);
            validMapping(tables, config);
            semiCache.initValue(CoreCacheKey.COLUMNS.getKey() + dataSourceName, tables);
        });


    }

    /**
     * 监测排除字段
     * todo 添加到columnList中
     *
     * @param columnList       数据源对应的数据库字段
     * @param dataSourceConfig 数据源配置
     */
    private void validExcludes(List<Column> columnList, DataSourceConfig dataSourceConfig) {

        Map<String, Set<String>> excludes = dataSourceConfig.getExcludes();
        if (excludes == null || excludes.isEmpty()) {
            return;
        }
        String tableMatch = excludes.keySet().stream().filter(tb -> columnList.stream().noneMatch(column -> column.getTable().equals(tb))).collect(Collectors.joining(","));
        Assert.hasText(tableMatch, () -> new ConfigException("请检查排除规则," + tableMatch + "表不存在"));

        excludes.forEach((table, columns) -> {
            String noneMatch = columns.stream()
                    .filter(column -> columnList.stream()
                            .filter(col -> table.equals(col.getTable()))
                            .noneMatch(col -> col.getColumn().equals(column))
                    )
                    .collect(Collectors.joining(","));

            Assert.hasText(noneMatch, () -> new ConfigException("请检查排除规则," + table + "表不存在" + noneMatch + "字段"));
        });

    }

    /**
     * 监测映射字段
     * todo 添加到columnList中
     *
     * @param columnList       数据源对应的数据库字段
     * @param dataSourceConfig 数据源配置
     */
    private void validMapping(List<Column> columnList, DataSourceConfig dataSourceConfig) {

        DataSourceConfig.Mapping mapping = dataSourceConfig.getMapping();
        if (mapping == null) {
            return;
        }
        Map<String, String> tableMap = mapping.getTable();
        if (tableMap == null) {
            return;
        }
        String tableMatch = tableMap.keySet().stream().filter(tb -> columnList.stream().noneMatch(column -> column.getTable().equals(tb))).collect(Collectors.joining(","));
        Assert.hasText(tableMatch, () -> new ConfigException("请检查表映射规则," + tableMatch + "表不存在"));

        Map<String, Map<String, String>> columnMap = mapping.getColumn();
        if (columnMap == null) {
            return;
        }
        String columnTableMatch = columnMap.keySet().stream().filter(tb -> columnList.stream().noneMatch(column -> column.getTable().equals(tb))).collect(Collectors.joining(","));
        Assert.hasText(columnTableMatch, () -> new ConfigException("请检查字段映射规则," + columnTableMatch + "表不存在"));
        columnMap.forEach((table, columns) -> {
            String columnMatch = columns.keySet().stream().filter(column -> columnList.stream().noneMatch(col -> table.equals(col.getTable()) && col.getColumn().equals(column))).collect(Collectors.joining(","));
            Assert.hasText(columnMatch, () -> new ConfigException("请检查表映射规则," + table + "表不存在" + columnMatch + "字段"));
        });

    }

}

