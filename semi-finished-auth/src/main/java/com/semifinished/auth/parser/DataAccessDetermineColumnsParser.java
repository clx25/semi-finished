package com.semifinished.auth.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.query.keyvalueparser.KeyValueParamsParser;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限参数指定查询字段
 * 在数据权限中，指定字段以排除字段的形式体现
 * 数据权限中的别名规则不生效
 */
@Component
@AllArgsConstructor
public class DataAccessDetermineColumnsParser implements KeyValueParamsParser {
    private final TableUtils tableUtils;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!"@".equals(key)) {
            return false;
        }
        String columns = value.asText(null);
        List<String> columnName = tableUtils.getColumnNames(sqlDefinition.getDataSource(), table);
        //如果指定规则为空，那么就排除所有字段
        if (!StringUtils.hasText(columns)) {
            sqlDefinition.addExcludeColumns(table, columnName);
            return true;
        }
        String[] colArray = columns.split(",");
        for (int i = 0; i < colArray.length; i++) {
            //去除别名
            colArray[i] = colArray[i].split(":")[0];
        }
        tableUtils.validColumnsName(sqlDefinition, table, colArray);
        //筛选没有指定的字段为排除字段
        List<String> ex = columnName.stream().filter(col -> Arrays.asList(colArray).contains(col)).collect(Collectors.toList());
        sqlDefinition.addExcludeColumns(table, ex);
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
