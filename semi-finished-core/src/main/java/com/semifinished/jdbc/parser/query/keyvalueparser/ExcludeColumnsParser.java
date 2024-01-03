package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.config.DataSourceConfig;
import com.semifinished.constant.ParserStatus;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.pojo.Column;
import com.semifinished.util.Assert;
import com.semifinished.util.ParserUtils;
import com.semifinished.util.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

/**
 * 在SQL层面排除字段
 * <pre>
 *     {
 *         "~":"col1,col2"
 *     }
 * </pre>
 * 以上规则的意思是排除col1和col2字段，只查询其他字段
 */
@Component
@AllArgsConstructor
public class ExcludeColumnsParser implements SelectParamsParser {

    private final TableUtils tableUtils;
    private final CommonParser commonParser;
    private final DataSourceConfig dataSourceConfig;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {

        if (!"~".equals(key.trim())) {
            return false;
        }
        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE,
                ParserStatus.JOIN, ParserStatus.DICTIONARY), () -> new ParamsException("排除规则位置错误"));


        String[] fields = value.asText().split(",");

        for (int i = 0; i < fields.length; i++) {
            fields[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, fields[i].trim());
        }

        tableUtils.validColumnsName(sqlDefinition, table, fields);
        List<Column> columns = sqlDefinition.getColumns();
        if (columns == null || columns.isEmpty()) {
            return true;
        }
        Set<String> excludes = excludesConfig(table);

        Stream.concat(excludes.stream(), Arrays.stream(fields))
                .forEach(field -> columns.stream()
                        .filter(column -> table.equals(column.getTable()))
                        .filter(column -> field.equals(column.getColumn()))
                        .forEach(column -> column.setDisabled(true))
                );


        return true;
    }

    private Set<String> excludesConfig(String table) {
        Map<String, Set<String>> excludes = dataSourceConfig.getExcludes();
        if (excludes == null || excludes.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> columns = excludes.get(table);

        return columns == null ? Collections.emptySet() : columns;
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
